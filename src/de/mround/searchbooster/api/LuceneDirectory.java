package de.mround.searchbooster.api;

import static de.mround.searchbooster.api.Configuration.SEARCH_BOOSTER_ROOT;
import static de.mround.searchbooster.api.Configuration.Index.META_PATH;
import static de.mround.searchbooster.api.Configuration.Index.META_RECURSIVE;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 * LuceneDirectory is an abstraction for an index realized with Lucene. Each
 * LuceneDirectory is assigned to an index in a seperate folder in the directory
 * {@link Configuration#SEARCH_BOOSTER_ROOT}.<br />
 * <br />
 * Warning: It is possible to open an index location more than once at the same
 * time by creating objects with the same index name in it's constructor
 * {@link #LuceneDirectory(String)} more than once . It will not corrupt the
 * index itself, but only the first opening instance will work properly.
 * 
 * @author Kai Torben Ohlhus
 */
public final class LuceneDirectory {
    // Members
    private final String name;
    private Directory directory = null;
    private IndexReader indexReader = null;
    private IndexSearcher indexSearcher = null;
    private IndexWriter indexWriter = null;

    /**
     * IndexJob is a private helper class to abstract an index job.
     * 
     * @author Kai Torben Ohlhus
     */
    private static class IndexJob {
	private final File url;
	private final int recursionDepth;

	/**
	 * Constructor.
	 * 
	 * @param url
	 *            - url of the item to index
	 * @param recursionDepth
	 *            - the recursion depth for this item
	 */
	private IndexJob(File url, int recursionDepth) {
	    this.url = url;
	    this.recursionDepth = recursionDepth;
	}
    }

    /**
     * IndexResult is a helper class to submit indexing statistics to the caller
     * of the indexing process.
     * 
     * @author Kai Torben Ohlhus
     */
    protected static class IndexResult {
	private final long timeMillis;
	private final long numIndexed;
	private final long numIndexErrors;

	/**
	 * Constructor.
	 * 
	 * @param timeMillis
	 *            - time needed for indexing in milliseconds
	 * @param numIndexed
	 *            - number of successfully indexed items
	 * @param numIndexErrors
	 *            - number of not successfully indexed items
	 */
	private IndexResult(long timeMillis, long numIndexed,
		long numIndexErrors) {
	    this.timeMillis = timeMillis;
	    this.numIndexed = numIndexed;
	    this.numIndexErrors = numIndexErrors;
	}

	/**
	 * Returns the time needed for indexing in milliseconds.
	 * 
	 * @return the time needed for indexing in milliseconds
	 */
	protected synchronized long getTimeMillis() {
	    return this.timeMillis;
	}

	/**
	 * Returns the number of successfully indexed items.
	 * 
	 * @return the number of successfully indexed items
	 */
	protected synchronized long getNumIndexed() {
	    return this.numIndexed;
	}

	/**
	 * Returns the number of not successfully indexed items.
	 * 
	 * @return the number of not successfully indexed items
	 */
	protected synchronized long getNumIndexErrors() {
	    return this.numIndexErrors;
	}
    }

    /**
     * Constructor.
     * 
     * @param name
     *            - the name of the index folder which will be used by this
     *            instance to maintain a Lucene index
     */
    protected LuceneDirectory(String name) {
	this.name = SEARCH_BOOSTER_ROOT + File.separator + name;
	// Create required file system structure (if necessary)
	File f = new File(this.name);
	if (!f.exists()) {
	    if (!f.mkdirs()) {
		System.err.println("Unable to create the directory <"
			+ this.name + ">.");
	    }
	}
    }

    /**
     * This method closes all dependencies to the underlying index by calling
     * {@link #close()} and removes all files, which the assigned index folder
     * contains, from the file system.
     * 
     * @throws IOException
     *             if a file in the directory cannot be deleted
     */
    protected synchronized void clearAllFilesInDirectory() throws IOException {
	this.close();

	int errors = 0;
	File dir = new File(this.name);
	if (dir.canRead() && dir.canWrite()) {
	    File[] fileList = dir.listFiles();
	    if (fileList != null) {
		for (File f : fileList) {
		    if (!f.delete()) {
			errors++;
		    }
		}
	    }
	}
	if (errors > 0) {
	    throw new IOException("Cannot delete " + errors + " Files in <"
		    + this.name + ">");
	}
    }

    /**
     * This method closes all dependencies to the underlying index by calling
     * {@link #close()} and will create a new IndexWriter, ignoring all previous
     * indexes in this directory.
     */
    protected synchronized void clearDirectory() {
	this.close();
	this.initalizeIndexWriter(OpenMode.CREATE);
    }

    /**
     * This method closes all dependencies to the underlying index and resets
     * this object's state as if it has just been constructed. All uncommitted
     * changes to the index will be lost.
     */
    protected synchronized void close() {
	if (this.indexWriter != null) {
	    this.dropChanges();
	}
	if (this.directory != null) {
	    try {
		this.directory.close();
	    } catch (IOException ignored) {
	    } finally {
		this.directory = null;
	    }
	}
	// Don't care about IndexReaders and IndexSearchers. They depend on the
	// existence of a IndexWriter.
    }

    /**
     * This method prepares the index folder to act as a Lucene index. If this
     * method fails, {@link #directory} will remain in it's previous state.
     */
    private synchronized void openDirectory() {
	if (this.directory == null) {
	    try {
		this.directory = SimpleFSDirectory.open(new File(this.name));
	    } catch (IOException e) {
		System.err.println("Cannot open the Lucene directory in <"
			+ this.name + ">. Reason: " + e.getMessage());
		this.directory = null;
	    }
	}
    }

    /**
     * This method prepares the unique IndexWriter instance of this Lucene
     * index. If this method fails, {@link #indexWriter} will remain in it's
     * previous state.
     */
    private synchronized void initalizeIndexWriter(OpenMode mode) {
	if (this.indexWriter == null) {
	    this.openDirectory();
	    if (this.directory == null) {
		return;
	    }
	    Analyzer analyser = new StandardAnalyzer(Version.LUCENE_35);
	    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
		    Version.LUCENE_35, analyser);
	    indexWriterConfig.setOpenMode(mode);

	    try {
		this.indexWriter = new IndexWriter(this.directory,
			indexWriterConfig);
	    } catch (CorruptIndexException e) {
		System.err.println("INDEX CORRUPTED!!!"
			+ "Trouble with the Lucene IndexWriter: "
			+ e.getMessage());
		this.indexWriter = null;
	    } catch (LockObtainFailedException e) {
		System.err.println("Trouble with the Lucene IndexWriter: "
			+ e.getMessage());
		this.indexWriter = null;
	    } catch (IOException e) {
		System.err.println("Trouble with the Lucene IndexWriter: "
			+ e.getMessage());
		this.indexWriter = null;
	    }
	}
    }

    /**
     * This method drops all changes made to the index since the last commit. If
     * nothing has been changed this method will do nothing.
     */
    protected synchronized void dropChanges() {
	if (this.indexWriter == null) {
	    return;
	}

	try {
	    this.indexWriter.rollback();
	} catch (IOException e) {
	    System.err.println("Cannot bring the Lucene index back "
		    + "into a save state. Reason: " + e.getMessage());
	} finally {
	    this.indexWriter = null;
	}
    }

    /**
     * This method commits all changes made to the index since the last commit.
     * If nothing has been changed this method will do nothing.
     */
    protected synchronized void commitChanges() {
	if (this.indexWriter == null) {
	    return;
	}

	try {
	    this.indexWriter.commit();
	} catch (CorruptIndexException e) {
	    System.err.println("Lucene index seems to be currupted! "
		    + "Cannot save changes to the Lucene index. Reason: "
		    + e.getMessage());
	} catch (IOException e) {
	    System.err.println("Cannot save changes to the Lucene index. "
		    + "Reason: " + e.getMessage());
	}
    }

    /**
     * This method returns a Map, containing information about the
     * {@link LuceneDirectory}, determined by the {@link IndexSearcher} searcher
     * object.<br />
     * The Map's key String contains a directory url and it's assigned Integer
     * value the recursion depth of the directory.<br />
     * If the index is empty, this method will return an empty Map.
     * 
     * @return a Map containing information about the {@link LuceneDirectory}
     */
    protected Map<String, Integer> getIndexContentMap() {
	Map<String, Integer> resultMap = new HashMap<String, Integer>();

	IndexSearcher searcher = this.getIndexSearcher();
	if (searcher == null) {
	    return resultMap;
	}

	// Build query
	Query q = new WildcardQuery(new Term(META_PATH, "*"));

	// Set initial collector size
	int collectorSize = 100;
	TopScoreDocCollector collector = null;

	// Perform the search.
	boolean done = false;
	do {
	    done = false;
	    collector = TopScoreDocCollector.create(collectorSize, true);

	    try {
		searcher.search(q, collector);
	    } catch (IOException ignore) {
		break;
	    }

	    // If there were more then collectorSize search results, the
	    // searching has to be repeated with a bigger collector with an
	    // appropriate size.
	    if (collectorSize < collector.getTotalHits()) {
		collectorSize = collector.getTotalHits();
	    } else {
		done = true;
	    }
	} while (!done);

	// build result
	TopDocs hits = collector.topDocs();
	for (ScoreDoc hit : hits.scoreDocs) {
	    Document d = null;
	    try {
		d = searcher.doc(hit.doc);
	    } catch (CorruptIndexException ignore) {
		continue;
	    } catch (IOException ignore) {
		continue;
	    }
	    resultMap.put(d.get(META_PATH),
		    Integer.valueOf(d.get(META_RECURSIVE)));
	}

	return resultMap;
    }

    /**
     * This method adds all items contained in the contentMap to the index. An
     * item of the contentMap consists of a String s indicating the path of a
     * directory to be added to the index and an Integer x indicating the
     * recursion depth of indexing.
     * <ul>
     * <li>x == 0: flat directory indexing of s</li>
     * <li>x < 0: full recursive directory indexing of s</li>
     * </ul>
     * No actions are performed to avoid double indexing (e.g. the item is
     * already present in the index).
     * 
     * @param contentMap
     *            - a Map of items to be added to the index
     * @return indexing statistics
     * @throws InterruptedException
     *             if the currently running Thread was interrupted
     */
    protected synchronized IndexResult addContentMapToIndex(
	    Map<String, Integer> contentMap) throws InterruptedException {
	initalizeIndexWriter(OpenMode.CREATE_OR_APPEND);

	// push indexing jobs on stack
	Stack<IndexJob> jobStack = new Stack<IndexJob>();
	for (String s : contentMap.keySet()) {
	    jobStack.push(new IndexJob(new File(s), contentMap.get(s)
		    .intValue()));
	    // save meta information
	    this.addDocumentToIndex(LuceneDirectoryUtility
		    .getIndexMetaDocument(Utility.canonicalizePath(s),
			    contentMap.get(s).intValue(),
			    System.currentTimeMillis()));
	}

	long start = System.currentTimeMillis();
	long numIndexed = 0;
	long numIndexErrors = 0;

	/*-
	 * This method uses a Stack (LIFO) to do it's job of recursive crawling.
	 * Steps performed in the LIFO loop until it is empty:
	 * 
	 * 1. POP top of Stack
	 * 2. Is directory? -> yes 2.1
	 *                  -> no  2.2
	 *    2.1 Is symbolic directory? -> yes 2.1.1
	 *                               -> no  2.1.2
	 *        2.1.1 IGNORE entry (following might cause loops!)
	 *        2.1.2 PUSH all containing Files to Stack
	 *    2.2 Is file readable? -> yes 2.2.1
	 *                          -> no  2.2.2
	 *        2.2.1 ADD document to Lucene index
	 *              (even if it is a symbolic link!)
	 *        2.2.2 IGNORE entry  
	 */
	while (!jobStack.isEmpty() && !Thread.currentThread().isInterrupted()) {
	    IndexJob job = jobStack.pop();
	    File file = job.url;

	    if (file.isDirectory()) {
		String fileCanonicalPath = "";
		try {
		    fileCanonicalPath = file.getCanonicalPath();
		} catch (IOException ignore) {
		}
		// Do not follow symbolic links (causes loops and
		// trouble!)
		if (file.getAbsolutePath().equals(fileCanonicalPath)) {
		    File[] subFiles = file.listFiles();
		    if (subFiles != null) {
			for (File subFile : subFiles) {
			    // only add directories, if recursion still wanted
			    if (!subFile.isDirectory()
				    || (job.recursionDepth != 0)) {
				jobStack.push(new IndexJob(subFile, -1));
			    }
			}
		    }
		}
	    } else if (file.isFile() && file.canRead()) {
		if (this.addDocumentToIndex(LuceneDirectoryUtility
			.getDocument(file))) {
		    numIndexed++;
		} else {
		    numIndexErrors++;
		}
	    } else {
		numIndexErrors++;
	    }
	}
	if (Thread.currentThread().isInterrupted()) {
	    throw new InterruptedException();
	}

	long end = System.currentTimeMillis();
	this.commitChanges();

	return new IndexResult((end - start), numIndexed, numIndexErrors);
    }

    /**
     * This method is a private wrapper method to add a {@link Document} to the
     * index.
     * 
     * @param doc
     *            - a {@link Document} to be added to the index
     * @return true if doc was added to the index without problems
     */
    private synchronized boolean addDocumentToIndex(Document doc) {
	boolean success = true;
	try {
	    this.indexWriter.addDocument(doc);
	} catch (CorruptIndexException ignore) {
	    success = false;
	} catch (IOException ignore) {
	    success = false;
	}

	return success;
    }

    /**
     * This method deletes index entries matching the {@link Query}.
     * 
     * @param query
     *            - query to delete entries from the index
     * @return true if the query was performed successfully
     */
    protected synchronized boolean deleteFromIndex(Query query) {
	boolean success = true;
	this.initalizeIndexWriter(OpenMode.CREATE_OR_APPEND);
	try {
	    this.indexWriter.deleteDocuments(query);
	} catch (CorruptIndexException ignore) {
	    success = false;
	} catch (IOException ignore) {
	    success = false;
	}

	return success;
    }

    /**
     * Returns an up-to-date {@link IndexSearcher} of this index.
     * 
     * @return an up-to-date {@link IndexSearcher} of this index
     */
    protected synchronized IndexSearcher getIndexSearcher() {
	if (this.indexWriter == null) {
	    this.initalizeIndexWriter(OpenMode.CREATE_OR_APPEND);
	    // references became obsolete
	    this.indexReader = null;
	    this.indexSearcher = null;
	}

	// if there was no seacher at all, or the index has changed
	try {
	    // if (this.indexReader == null) {
	    this.indexReader = IndexReader.open(this.directory, true);
	    // } else {
	    // IndexReader newReader = IndexReader.openIfChanged(
	    // this.indexReader, true);
	    // if (newReader != null) {
	    // this.indexReader = newReader;
	    // }
	    // }
	} catch (CorruptIndexException e) {
	    System.err.println("Lucene index seems to be currupted! "
		    + "Cannot create a new reader. Reason: " + e.getMessage());
	    this.indexReader = null;
	} catch (IOException ignore) {
	    // will be thrown if no segments* file was found in the index
	    this.indexReader = null;
	}
	if (this.indexReader == null) {
	    return null;
	}
	this.indexSearcher = new IndexSearcher(this.indexReader);

	return this.indexSearcher;
    }
}
