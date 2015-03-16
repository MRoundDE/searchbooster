package de.mround.searchbooster.api;

import static de.mround.searchbooster.api.Configuration.SEARCH_BOOSTER_CONFIG_FILE;
import static de.mround.searchbooster.api.Configuration.SEARCH_BOOSTER_ROOT;
import static de.mround.searchbooster.api.Configuration.Index.EXPIRE_TIME_MILLIS;
import static de.mround.searchbooster.api.Configuration.Index.EXPIRE_TIME_PRECISION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.w3c.dom.Document;

import de.mround.searchbooster.api.xml.RequestAbortParser;
import de.mround.searchbooster.api.xml.RequestIndexChangeParser;
import de.mround.searchbooster.api.xml.RequestIndexInfoParser;
import de.mround.searchbooster.api.xml.RequestSearchParser;
import de.mround.searchbooster.api.xml.ResponseBuilder;
import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * SearchBoosterImplementation implements the {@link SearchBooster}-Interface.
 * 
 * @author Michael Kunert
 * @author Kai Torben Ohlhus
 */
public class SearchBoosterImplementation implements SearchBooster {
    // Singleton
    private static SearchBoosterImplementation uniqueInstance = new SearchBoosterImplementation();
    // Constants
    private final int responseQueueSize = 1;
    private final int threadPoolSize = 10;
    // Members
    private final Map<String, LuceneDirectory> indexes = Collections
	    .unmodifiableMap(new HashMap<String, LuceneDirectory>() {
		// anonymous inner class
		private static final long serialVersionUID = 1L;
		{
		    put("persistent", new LuceneDirectory("persistent"));
		    put("temporary", new LuceneDirectory("temporary"));
		}
	    });
    private final ArrayBlockingQueue<Document> responseQueue = new ArrayBlockingQueue<Document>(
	    this.responseQueueSize);
    private final ExecutorService executorService = Executors
	    .newFixedThreadPool(this.threadPoolSize);
    private Future<?> currentRequest = null;
    private TaskType currentRequestType = null;

    /**
     * TaskType is a private helper class to determine the type of a currently
     * running task.<br />
     * Tasks are:
     * <ul>
     * <li>ConfigurationTask</li>
     * <li>IndexChangeTask</li>
     * <li>IndexInfoTask</li>
     * <li>SearchTask</li>
     * </ul>
     * 
     * @author Kai Torben Ohlhus
     */
    private enum TaskType {
	Configuration("ConfigurationTask"), IndexChange("IndexChangeTask"), IndexInfo(
		"IndexInfoTask"), Search("SearchTask");

	private final String name;

	private TaskType(String name) {
	    this.name = name;
	}
    }

    /**
     * Private constructor, Singleton pattern.
     */
    private SearchBoosterImplementation() {
	try {
	    this.indexes.get("temporary").clearAllFilesInDirectory();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}

	// load config.txt (if available) or create it
	String configName = SEARCH_BOOSTER_ROOT + File.separator
		+ SEARCH_BOOSTER_CONFIG_FILE;
	File configFile = new File(configName);
	Properties properties = new Properties();

	if (configFile.exists() && configFile.canRead()) {
	    try {
		properties.loadFromXML(new FileInputStream(configFile));
	    } catch (InvalidPropertiesFormatException e) {
		System.err.println("[info] Configuraton file <" + configName
			+ "> is corrupted. "
			+ "Default configuration is used. "
			+ "Remove it manually.");
	    } catch (FileNotFoundException e) {
		System.err.println("[info] Configuraton file <" + configName
			+ "> is corrupted. "
			+ "Default configuration is used. "
			+ "Remove it manually.");
	    } catch (IOException e) {
		System.err.println("[info] Configuraton file <" + configName
			+ "> is corrupted. "
			+ "Default configuration is used. "
			+ "Remove it manually.");
	    }
	    // update EXPIRE_TIME_MILLIS (if available)
	    if (properties.getProperty("EXPIRE_TIME_MILLIS") != null) {
		long l = Long.parseLong(properties
			.getProperty("EXPIRE_TIME_MILLIS"));
		// ensure consistency
		if (l < EXPIRE_TIME_PRECISION.getPrecisionInMilliseconds()) {
		    l = EXPIRE_TIME_PRECISION.getPrecisionInMilliseconds();
		}
		EXPIRE_TIME_MILLIS.set(l);
	    }
	} else {
	    System.out.println("[info] Default configuration is used.");

	    // fetch default value
	    long l = EXPIRE_TIME_MILLIS.get();
	    // ensure consistency
	    if (l < EXPIRE_TIME_PRECISION.getPrecisionInMilliseconds()) {
		l = EXPIRE_TIME_PRECISION.getPrecisionInMilliseconds();
	    }
	    EXPIRE_TIME_MILLIS.set(l);

	    // save value for upcoming runs
	    properties.setProperty("EXPIRE_TIME_MILLIS",
		    Long.toString(EXPIRE_TIME_MILLIS.get()));
	    try {
		properties.storeToXML(new FileOutputStream(configFile),
			"SearchBooster Configuration");
	    } catch (FileNotFoundException ignore) {
	    } catch (IOException ignore) {
	    }
	}
    }

    /**
     * Returns a unique instance of SearchBooster.
     * 
     * @return a unique instance of SearchBooster
     */
    public synchronized static SearchBooster getInstance() {
	return uniqueInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void request(Document request) {
	System.out.println("");
	System.out.println("New request arrived.");
	System.out.println("");

	// 1. cancel currently running request via interrupt (if available)
	if ((this.currentRequest != null) && !this.currentRequest.isDone()) {
	    // give a last chance to finish
	    try {
		this.currentRequest.get(1, TimeUnit.SECONDS);
	    } catch (InterruptedException ignore) {
	    } catch (ExecutionException ignore) {
	    } catch (TimeoutException ignore) {
	    }
	    this.currentRequest.cancel(true);
	    System.out.println("[info] Current " + this.currentRequestType.name
		    + " interrupted.");
	    this.responseQueue.clear();

	    // perform task specific actions to beware consistency
	    if (this.currentRequestType.equals(TaskType.IndexChange)) {
		this.indexes.get("persistent").dropChanges();
	    } else if (this.currentRequestType.equals(TaskType.Search)) {
		this.indexes.get("temporary").dropChanges();
	    }
	    this.currentRequest = null;
	}

	// 2. determine incoming request and start necessary tasks
	if (RequestAbortParser.isAbortRequest(request)) {
	    System.out.println("[info] AbortRequest -> stopping everything.");
	    Document Response = ResponseBuilder.create(StatusCodeID.SUCCESSFUL,
		    "No jobs running.");
	    try {
		this.responseQueue.put(Response);
	    } catch (InterruptedException ignore) {
	    } finally {
		this.currentRequest = null;
	    }
	} else if (RequestSearchParser.isSearchRequest(request)) {
	    this.currentRequestType = TaskType.Search;
	    this.currentRequest = this.executorService.submit(new SearchTask(
		    this.responseQueue, this.indexes.get("persistent"),
		    this.indexes.get("temporary"), request));
	} else if (RequestIndexInfoParser.isIndexInfoRequest(request)) {
	    this.currentRequestType = TaskType.IndexInfo;
	    this.currentRequest = this.executorService
		    .submit(new IndexInfoTask(this.responseQueue, this.indexes
			    .get("persistent")));
	} else if (RequestIndexChangeParser.isIndexChangeRequest(request)) {
	    this.currentRequestType = TaskType.IndexChange;
	    this.currentRequest = this.executorService
		    .submit(new IndexChangeTask(this.responseQueue, request,
			    this.indexes.get("persistent")));
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockingQueue<Document> getResponseQueue() {
	return this.responseQueue;
    }
}
