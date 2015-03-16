package de.mround.searchbooster.api;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import de.mround.searchbooster.api.Utility.DatePrecision;

/**
 * This is a noninstantiable constant utility class.
 * 
 * @author Kai Torben Ohlhus
 */
public final class Configuration {
    /**
     * Private constructor to avoid instantiation.
     */
    private Configuration() {
    }

    protected static final int NUMBER_OF_CPUS = Runtime.getRuntime()
	    .availableProcessors();
    public static final String SEARCH_BOOSTER_ROOT = System
	    .getProperty("user.home") + File.separator + ".search_booster";
    public static final String SEARCH_BOOSTER_CONFIG_FILE = "config.txt";

    /**
     * This class contains the fields that will be stored in a
     * {@link LuceneDirectory}.
     */
    public static final class Index {
	/**
	 * Private constructor to avoid instantiation.
	 */
	private Index() {
	}

	// Configuration
	protected static final DatePrecision EXPIRE_TIME_PRECISION = Utility.DatePrecision.Second;
	// EXPIRE_TIME_MILLIS is mutable
	protected static final AtomicLong EXPIRE_TIME_MILLIS = new AtomicLong(
		Utility.DatePrecision.Day.getPrecisionInMilliseconds());

	// Index Meta
	protected static final String META_PATH = "MetaPath";
	protected static final String META_RECURSIVE = "MetaRecursive";
	protected static final String META_DATE_INDEXED = "MetaDateTimeIndexed";
	protected static final DatePrecision META_DATE_INDEXED_PRECISION = Utility.DatePrecision.Second;

	// Entry Meta
	public static final String FILE_NAME = "FileName";
	public static final String URL = "URL";
	public static final String LAST_MODIFIED = "LastModified";
	protected static final String LAST_MODIFIED_SEARCH = "LastModifiedSearch";
	// Entry Tika
	public static final String TYPE = "Type";
	public static final String TEXT_CONTENT = "TextContent";
	public static final String INTERPRET = "Interpret";
	public static final String ALBUM = "Album";
    }
}
