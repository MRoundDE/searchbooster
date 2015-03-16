package de.mround.searchbooster.api;

import java.io.File;
import java.util.Date;

/**
 * This class provides some general utility methods, used by this package.
 * 
 * @author Kai Torben Ohlhus
 */
public final class Utility {
    /**
     * Private constructor to avoid instantiation.
     */
    private Utility() {
    }

    /**
     * Precision for timestamps in milliseconds as you receive from the common
     * methods:
     * <ul>
     * <li>{@link System#currentTimeMillis()},</li>
     * <li>{@link Date#getTime()} or</li>
     * <li>{@link File#lastModified()}</li>
     * </ul>
     */
    protected enum DatePrecision {
	Millisecond(1), Second(1000), Minute(1000 * 60), Hour(1000 * 60 * 60), Day(
		1000 * 60 * 60 * 24);

	private final long precisionInMilliseconds;

	/**
	 * Constructor.
	 * 
	 * @param precisionInMilliseconds
	 *            - the precision in milliseconds
	 */
	private DatePrecision(long precisionInMilliseconds) {
	    this.precisionInMilliseconds = precisionInMilliseconds;
	}

	/**
	 * Returns the precision in milliseconds.
	 * 
	 * @return the precision in milliseconds
	 */
	protected long getPrecisionInMilliseconds() {
	    return this.precisionInMilliseconds;
	}

	/**
	 * Modifies the precision of a date timestamp.
	 * 
	 * @param date
	 *            - a date timestamp
	 * @param currentPrecision
	 *            - date timestamp's current precision
	 * @param newPrecision
	 *            - the desired precision of date timestamp
	 * @return the date timestamp with precision newPrecision
	 */
	protected static long convertPrecision(long date,
		DatePrecision currentPrecision, DatePrecision newPrecision) {
	    // convert to milliseconds
	    date *= currentPrecision.getPrecisionInMilliseconds();
	    // convert to newPrecision
	    date /= newPrecision.getPrecisionInMilliseconds();

	    return date;
	}
    }

    /**
     * This method checks if a directory (subdir) is a subdirectory of another
     * (dir).<br />
     * <br />
     * <table border="1">
     * <tr>
     * <th>dir</th>
     * <th>subdir</th>
     * <th>isSubDirectory(dir, subdir)</th>
     * </tr>
     * <tr>
     * <td>C:\</td>
     * <td>C:\temp\</td>
     * <td>true</td>
     * </tr>
     * </tr>
     * <tr>
     * <td>C:\temp\</td>
     * <td>C:\</td>
     * <td>false</td>
     * </tr>
     * </table>
     * 
     * @param dir
     *            - a directory path
     * @param subdir
     *            - a directory path
     * @return true if a directory (subdir) is a subdirectory of another (dir)
     */
    protected static boolean isSubDirectory(String dir, String subdir) {
	// check parameter
	if (dir == null) {
	    throw new NullPointerException("Input parameter dir is null.");
	}
	if (subdir == null) {
	    throw new NullPointerException("Input parameter subdir is null.");
	}
	if (dir.isEmpty()) {
	    throw new IllegalArgumentException("Input parameter dir is empty.");
	}
	if (dir.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Input parameter subdir is empty.");
	}

	dir = canonicalizePath(dir);
	subdir = canonicalizePath(subdir);

	// check for subdirectory
	if ((dir.length() < subdir.length()) && subdir.startsWith(dir)) {
	    return true;
	}

	return false;
    }

    /**
     * This method appends a platform dependent file seperator
     * {@link File#separator} to a given path-String.<br />
     * <br />
     * Examples for canonical paths:<br />
     * (MS Windows)
     * <ul>
     * <li>C:\</li>
     * <li>C:\temp\</li>
     * </ul>
     * (Unix)
     * <ul>
     * <li>/</li>
     * <li>/tmp/</li>
     * </ul>
     * 
     * @param path
     *            - a non-empty path-String
     * @return the canonical path-String (see examples above)
     */
    protected static String canonicalizePath(String path) {
	// check parameter
	if (path == null) {
	    throw new NullPointerException("Input parameter path is null.");
	}
	if (path.isEmpty()) {
	    throw new IllegalArgumentException("Input parameter path is empty.");
	}

	if (!path.endsWith(File.separator)) {
	    path = path + File.separator;
	}

	return path;
    }
}
