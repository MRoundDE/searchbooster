package de.mround.searchbooster.api;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.w3c.dom.Document;

import de.mround.searchbooster.api.xml.ResponseIndexContentBuilder;
import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * An IndexInfoTask collects information about the content of the persistent
 * index.
 * 
 * @author Kai Torben Ohlhus
 */
public final class IndexInfoTask implements Runnable {
    private final BlockingQueue<Document> responseQueue;
    private final LuceneDirectory persistent;

    /**
     * Constructor.
     * 
     * @param responseQueue
     *            - a {@link BlockingQueue} to put the response messages in
     * @param persistent
     *            - a reference to the persistent index
     */
    protected IndexInfoTask(BlockingQueue<Document> responseQueue,
	    LuceneDirectory persistent) {
	this.responseQueue = responseQueue;
	this.persistent = persistent;
    }

    @Override
    public void run() {
	System.out.println("[info] A new IndexInfoTask has been started.");

	Document response = ResponseIndexContentBuilder.create(
		StatusCodeID.ERROR, "Failed to retrieve index information.");

	try {
	    Map<String, Integer> contentMap = this.persistent
		    .getIndexContentMap();

	    if (contentMap.isEmpty()) {
		response = ResponseIndexContentBuilder.create(
			StatusCodeID.SUCCESSFUL,
			"The persistent index is empty.");
	    } else {
		response = ResponseIndexContentBuilder.create(
			StatusCodeID.SUCCESSFUL,
			"The persistent index contains " + contentMap.size()
				+ " path(s).");
		for (String url : contentMap.keySet()) {
		    ResponseIndexContentBuilder.appendPath(response, url,
			    contentMap.get(url).intValue());
		}
	    }
	} finally {
	    try {
		this.responseQueue.put(response);
	    } catch (InterruptedException ignore) {
	    }
	    System.out.println("[info] IndexInfoTask completed.");
	}
    }
}
