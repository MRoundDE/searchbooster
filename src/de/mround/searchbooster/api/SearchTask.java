package de.mround.searchbooster.api;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.Configuration.Index;
import de.mround.searchbooster.api.LuceneDirectory.IndexResult;
import de.mround.searchbooster.api.Utility.DatePrecision;
import de.mround.searchbooster.api.xml.RequestIndexChangeParser;
import de.mround.searchbooster.api.xml.RequestSearchParser;
import de.mround.searchbooster.api.xml.ResponseSearchBuilder;
import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * A SearchTask performs searches requests on the desktop system. A search
 * request will be processed as follows:
 * <ol>
 * <li>Check what the persistent and temporary indexes contain.</li>
 * <li>Analyze the search request to see what has to be added to the temporary
 * index.</li>
 * <li>Perform searches on both indexes.</li>
 * </ol>
 * 
 * @author Kai Torben Ohlhus
 */
public final class SearchTask implements Runnable {
    private final BlockingQueue<Document> responseQueue;
    private final LuceneDirectory persistent;
    private final LuceneDirectory temporary;
    private final Document request;

    // The item order will be the order in which the indexes are searched in
    private static enum IndexType {
	PERSISTENT, TEMPORARAY
    };

    /**
     * Constructor.
     * 
     * @param responseQueue
     *            - a {@link BlockingQueue} to put the response messages in
     * @param persistent
     *            - a reference to the temporary index
     * @param temporary
     *            - a reference to the persistent index
     * @param request
     *            - the search request to be performed
     */
    protected SearchTask(BlockingQueue<Document> responseQueue,
	    LuceneDirectory persistent, LuceneDirectory temporary,
	    Document request) {
	this.responseQueue = responseQueue;
	this.persistent = persistent;
	this.temporary = temporary;
	this.request = request;
    }

    @Override
    public void run() {
	System.out.println("[info] A new SearchTask has been started.");

	Document response = ResponseSearchBuilder.create(StatusCodeID.ERROR,
		"Searching failed.");

	try {
	    Query q = this.buildQueryFromSearchRequest();
	    System.out.println("[debug] " + q);

	    // get the contents of the persistent and temporary indexes
	    Map<String, Integer> persistentContentMap = this.persistent
		    .getIndexContentMap();
	    Map<String, Integer> temporaryContentMap = this.temporary
		    .getIndexContentMap();

	    // get the paths to search in without url duplicates
	    Map<String, Integer> searchPathMap = new HashMap<String, Integer>();
	    for (Node n : RequestIndexChangeParser.getPathList(this.request)) {
		String url = Utility.canonicalizePath(RequestIndexChangeParser
			.getPathURL(n));
		int r = RequestIndexChangeParser.getPathRecursionDepth(n);

		// if url already present and not recursive, prefer new entry
		if (searchPathMap.containsKey(url)) {
		    if ((r < 0) && (searchPathMap.get(url).intValue() >= 0)) {
			searchPathMap.remove(url);
		    } else {
			continue;
		    }
		}
		searchPathMap.put(url, r);
	    }

	    // get the paths to search in without subdirectories that will
	    // already be covered by recursive parent directories
	    Map<String, Integer> searchPathMapCopy = new HashMap<String, Integer>(
		    searchPathMap);
	    for (String s1 : searchPathMapCopy.keySet()) {
		for (String s2 : searchPathMapCopy.keySet()) {
		    // remove if a parent directory is already recursive
		    if (Utility.isSubDirectory(s2, s1)
			    && searchPathMapCopy.get(s2).intValue() < 0) {
			searchPathMap.remove(s1);
			break;
		    }
		}
	    }

	    // get a model of the temporary index without entries, that will
	    // already be covered by the persistent index => delete them from
	    // the temporary index
	    Map<String, Integer> toDelete = new HashMap<String, Integer>();
	    for (String s1 : temporaryContentMap.keySet()) {
		for (String s2 : persistentContentMap.keySet()) {
		    // recursive parent or peer in persistent index
		    if ((s1.equals(s2) || (Utility.isSubDirectory(s2, s1))
			    && (persistentContentMap.get(s2).intValue() < 0))) {
			toDelete.put(s1, temporaryContentMap.get(s1));
		    }
		    // equal peer in persistent index
		    if (s1.equals(s2)
			    && temporaryContentMap.get(s1).equals(
				    persistentContentMap.get(s2))) {
			toDelete.put(s1, temporaryContentMap.get(s1));
		    }
		}
	    }
	    for (String s : toDelete.keySet()) {
		temporaryContentMap.remove(s);
	    }

	    // get a model of the temporary index without entries, that have to
	    // be extended by the coming search request => delete them from
	    // the temporary index
	    for (String s1 : temporaryContentMap.keySet()) {
		for (String s2 : searchPathMap.keySet()) {
		    if (Utility.isSubDirectory(s2, s1)
			    && (searchPathMap.get(s2).intValue() < 0)) {
			toDelete.put(s1, temporaryContentMap.get(s1));
		    }
		}
	    }
	    for (String s : toDelete.keySet()) {
		temporaryContentMap.remove(s);
	    }

	    // perform deletions on the temporary index
	    if (toDelete.size() > 0) {
		for (String s : toDelete.keySet()) {
		    Query deleteQuery = new PrefixQuery(new Term(
			    Index.META_PATH, s));
		    this.temporary.deleteFromIndex(deleteQuery);
		    deleteQuery = new PrefixQuery(new Term(Index.URL, s));
		    this.temporary.deleteFromIndex(deleteQuery);
		}
	    }
	    toDelete.clear();

	    // get entries that really have to be indexed temporary
	    for (String s1 : searchPathMap.keySet()) {
		for (String s2 : persistentContentMap.keySet()) {
		    // recursive parent or peer in persistent index
		    if ((s1.equals(s2) || Utility.isSubDirectory(s2, s1))
			    && (persistentContentMap.get(s2).intValue() < 0)) {
			toDelete.put(s1, searchPathMap.get(s1));
		    }
		    // equal peer in persistent index
		    if (s1.equals(s2)
			    && searchPathMap.get(s1).equals(
				    persistentContentMap.get(s2))) {
			toDelete.put(s1, searchPathMap.get(s1));
		    }
		}
	    }
	    for (String s1 : searchPathMap.keySet()) {
		for (String s2 : temporaryContentMap.keySet()) {
		    // recursive parent or peer in temporary index
		    if ((s1.equals(s2) || Utility.isSubDirectory(s2, s1))
			    && (temporaryContentMap.get(s2).intValue() < 0)) {
			toDelete.put(s1, searchPathMap.get(s1));
		    }
		    // equal peer in temporary index
		    if (s1.equals(s2)
			    && searchPathMap.get(s1).equals(
				    temporaryContentMap.get(s2))) {
			toDelete.put(s1, searchPathMap.get(s1));
		    }
		}
	    }
	    for (String s : toDelete.keySet()) {
		searchPathMap.remove(s);
	    }

	    System.out.println("[info] Need to index at least "
		    + searchPathMap.size() + " directory(s).");

	    IndexResult indexResult = null;
	    try {
		indexResult = this.temporary
			.addContentMapToIndex(searchPathMap);
	    } catch (InterruptedException ignore) {
	    }

	    System.out.println("[info] Adding " + indexResult.getNumIndexed()
		    + " files to the temporary index took "
		    + indexResult.getTimeMillis() + " milliseconds. "
		    + indexResult.getNumIndexErrors()
		    + " files were erroneous.");

	    // get searchers
	    IndexSearcher persistentSearcher = this.persistent
		    .getIndexSearcher();
	    IndexSearcher temporarySearcher = this.temporary.getIndexSearcher();

	    // Checkpoint
	    if (Thread.currentThread().isInterrupted()) {
		throw new InterruptedException();
	    }

	    // Perform the search.
	    int persistentHits = 0;
	    int temporaryHits = 0;
	    long persistentTime = 0;
	    long temporaryTime = 0;
	    long start = 0;
	    long end = 0;

	    TopScoreDocCollector collector = null;

	    for (IndexType indexType : IndexType.values()) {

		// Set initial collector size
		int collectorSize = 100;
		boolean done = false;
		do {
		    collector = TopScoreDocCollector
			    .create(collectorSize, true);

		    start = System.currentTimeMillis();
		    try {
			if (indexType.equals(IndexType.PERSISTENT)
				&& (persistentSearcher != null)) {
			    persistentSearcher.search(q, collector);
			} else if (indexType.equals(IndexType.TEMPORARAY)
				&& (temporarySearcher != null)) {
			    temporarySearcher.search(q, collector);
			}
		    } catch (IOException ignore) {
			// Failure will be responded via BlockingQueue
		    }
		    end = System.currentTimeMillis();

		    // Time statistics
		    if (indexType.equals(IndexType.PERSISTENT)) {
			persistentHits = collector.getTotalHits();
			persistentTime += end - start;
		    } else if (indexType.equals(IndexType.TEMPORARAY)) {
			temporaryHits = collector.getTotalHits();
			temporaryTime += end - start;
		    }

		    // If there were more then collectorSize search results, the
		    // searching has to be repeated with a bigger collector with
		    // an appropriate size.
		    if (collectorSize < collector.getTotalHits()) {
			collectorSize = collector.getTotalHits();
		    } else {
			done = true;
		    }
		} while (!done);

		// Checkpoint
		if (Thread.currentThread().isInterrupted()) {
		    throw new InterruptedException();
		}

		TopDocs hits = collector.topDocs();
		int searchResults = (indexType.equals(IndexType.PERSISTENT) ? persistentHits
			: temporaryHits);
		done = false;

		response = ResponseSearchBuilder
			.create(((indexType.equals(IndexType.TEMPORARAY)) ? StatusCodeID.SUCCESSFUL
				: StatusCodeID.NOT_COMPLETED), "Found "
				+ (persistentHits + temporaryHits)
				+ " document(s) (in "
				+ (persistentTime + temporaryTime)
				+ " milliseconds)");

		for (int i = 0; i < searchResults; i++) {
		    // Checkpoint
		    if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		    }

		    // last result?
		    if ((i + 1) == searchResults) {
			done = true;
		    }

		    response = ResponseSearchBuilder
			    .create(((done && indexType
				    .equals(IndexType.TEMPORARAY)) ? StatusCodeID.SUCCESSFUL
				    : StatusCodeID.NOT_COMPLETED), "Found "
				    + (persistentHits + temporaryHits)
				    + " document(s) (in "
				    + (persistentTime + temporaryTime)
				    + " milliseconds)");

		    ScoreDoc hit = hits.scoreDocs[i];

		    org.apache.lucene.document.Document d = null;
		    try {
			if (indexType.equals(IndexType.PERSISTENT)
				&& (persistentSearcher != null)) {
			    d = persistentSearcher.doc(hit.doc);
			} else if (indexType.equals(IndexType.TEMPORARAY)
				&& (temporarySearcher != null)) {
			    d = temporarySearcher.doc(hit.doc);
			}
		    } catch (CorruptIndexException ignore) {
			continue;
		    } catch (IOException ignore) {
			continue;
		    }

		    long lastModified = Long.parseLong(d
			    .get(Index.LAST_MODIFIED));
		    lastModified = DatePrecision.convertPrecision(lastModified,
			    DatePrecision.Day, DatePrecision.Millisecond);
		    GregorianCalendar cal = new GregorianCalendar();
		    cal.setTimeInMillis(lastModified);
		    XMLGregorianCalendar modificationdate = null;
		    try {
			modificationdate = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(cal);
		    } catch (DatatypeConfigurationException ignore) {
			continue;
		    }

		    ResponseSearchBuilder.appendSearchResult(response,
			    d.get(Index.URL) + d.get(Index.FILE_NAME),
			    modificationdate, false);

		    this.responseQueue.put(response);
		}
	    }
	} catch (InterruptedException ignore) {
	} finally {
	    try {
		this.responseQueue.put(response);
	    } catch (InterruptedException ignore) {
	    }
	    System.out.println("[info] SearchTask completed.");
	}
    }

    /**
     * This method returns a Map-Representation of the path urls of a search
     * request, where searching should happen.
     * 
     * @return a Map-Representation of the path urls of a search request
     */
    private Map<String, Integer> getPathMap() {
	Map<String, Integer> resultMap = new HashMap<String, Integer>();
	List<Node> pathList = RequestSearchParser.getPathList(this.request);

	for (Node path : pathList) {
	    resultMap.put(RequestSearchParser.getPathURL(path),
		    RequestSearchParser.getPathRecursionDepth(path));
	}

	return resultMap;
    }

    /**
     * This method translates a XML-Search-Request into a Query, which the
     * Lucene index can process.
     * 
     * @return a Query, which the Lucene index can process
     */
    private Query buildQueryFromSearchRequest() {
	BooleanQuery rootQuery = new BooleanQuery();

	List<Node> parameterList = RequestSearchParser
		.getParameterList(this.request);

	if (!parameterList.isEmpty()) {
	    BooleanQuery parametersQuery = new BooleanQuery();
	    for (Node parameter : parameterList) {
		BooleanQuery parameterQuery = new BooleanQuery();

		// query for all file names, if search field is empty
		String search = "*";
		if (RequestSearchParser.hasParameterFileName(parameter)) {
		    search = RequestSearchParser
			    .getParameterFileName(parameter);
		}

		Term t = new Term(Index.FILE_NAME, search);
		parameterQuery.add(new WildcardQuery(t), Occur.MUST);

		// Check for selected type and metadata
		if (RequestSearchParser.hasParameterContentMusic(parameter)) {
		    Term t0 = new Term(Index.TYPE, "audio");
		    parameterQuery.add(new WildcardQuery(t0), Occur.MUST);

		    // Search artist/album tag
		    if (RequestSearchParser
			    .hasParameterContentMusicInterpret(parameter)) {
			Term t1 = new Term(Index.INTERPRET, "*"
				+ RequestSearchParser
					.getParameterContentMusicInterpret(
						parameter).toLowerCase() + "*");
			parameterQuery.add(new WildcardQuery(t1), Occur.MUST);
		    }
		    if (RequestSearchParser
			    .hasParameterContentMusicAlbum(parameter)) {
			Term t1 = new Term(Index.ALBUM, "*"
				+ RequestSearchParser
					.getParameterContentMusicAlbum(
						parameter).toLowerCase() + "*");
			parameterQuery.add(new WildcardQuery(t1), Occur.MUST);
		    }

		} else if (RequestSearchParser
			.hasParameterContentPicture(parameter)) {
		    Term t0 = new Term(Index.TYPE, "image");
		    parameterQuery.add(new TermQuery(t0), Occur.MUST);
		} else if (RequestSearchParser
			.hasParameterContentDocument(parameter)) {
		    Term t0 = new Term(Index.TYPE, "document");
		    parameterQuery.add(new TermQuery(t0), Occur.MUST);

		    // Search document content
		    if (RequestSearchParser
			    .hasParameterContentDocumentSearchString(parameter)) {
			Term t1 = new Term(
				Index.TEXT_CONTENT,
				"*"
					+ RequestSearchParser
						.getParameterContentDocumentSearchString(
							parameter)
						.toLowerCase() + "*");
			parameterQuery.add(new WildcardQuery(t1), Occur.MUST);
		    }

		} else if (RequestSearchParser
			.hasParameterContentVideo(parameter)) {
		    Term t0 = new Term(Index.TYPE, "video");
		    parameterQuery.add(new WildcardQuery(t0), Occur.MUST);
		}

		if (RequestSearchParser
			.hasParameterModificationDateBegin(parameter)
			|| RequestSearchParser
				.hasParameterModificationDateEnd(parameter)) {
		    NumericRangeQuery<Long> numericQuery = null;
		    long min = 0L;
		    long max = 0L;
		    if (RequestSearchParser
			    .hasParameterModificationDateBegin(parameter)) {
			try {
			    min = RequestSearchParser
				    .getParameterModificationDateBegin(
					    parameter).toGregorianCalendar()
				    .getTimeInMillis();
			    min = DatePrecision.convertPrecision(min,
				    DatePrecision.Millisecond,
				    DatePrecision.Day);
			} catch (DOMException ignore) {
			    min = 0L;
			} catch (DatatypeConfigurationException ignore) {
			    min = 0L;
			}
		    }
		    if (RequestSearchParser
			    .hasParameterModificationDateEnd(parameter)) {
			try {
			    max = RequestSearchParser
				    .getParameterModificationDateEnd(parameter)
				    .toGregorianCalendar().getTimeInMillis();
			    max = DatePrecision.convertPrecision(max,
				    DatePrecision.Millisecond,
				    DatePrecision.Day) + 1;
			} catch (DOMException ignore) {
			    max = 0L;
			} catch (DatatypeConfigurationException ignore) {
			    max = 0L;
			}
		    }

		    if ((min > 0L) && (max > 0L)) {
			numericQuery = NumericRangeQuery.newLongRange(
				Index.LAST_MODIFIED_SEARCH, 1, min, max, false,
				true);
		    } else if ((min > 0L) && (max == 0L)) {
			numericQuery = NumericRangeQuery.newLongRange(
				Index.LAST_MODIFIED_SEARCH, 1, min, null,
				false, true);
		    } else if ((min == 0L) && (max > 0L)) {
			numericQuery = NumericRangeQuery.newLongRange(
				Index.LAST_MODIFIED_SEARCH, 1, null, max,
				false, true);
		    }

		    parameterQuery.add(numericQuery, Occur.MUST);

		}
		parametersQuery.add(parameterQuery, Occur.SHOULD);
	    }
	    rootQuery.add(parametersQuery, Occur.MUST);
	}

	// Paths (allowed Files or directories)
	Map<String, Integer> paths = this.getPathMap();
	if (!paths.isEmpty()) {
	    BooleanQuery pathsQuery = new BooleanQuery();
	    for (String url : paths.keySet()) {
		BooleanQuery urlQuery = new BooleanQuery();
		File f = new File(url);
		if (!f.exists()) {
		    continue;
		}

		String path = url;
		String fileName = f.getName();

		if (f.isFile()) {
		    path = path.substring(0, path.lastIndexOf(File.separator))
			    + File.separator;
		    Term t = new Term(Index.FILE_NAME, fileName);
		    urlQuery.add(new TermQuery(t), Occur.MUST);
		}
		Term t = new Term(Index.URL, Utility.canonicalizePath(path));

		if (paths.get(url).intValue() < 0) {
		    urlQuery.add(new PrefixQuery(t), Occur.MUST);
		} else {
		    urlQuery.add(new TermQuery(t), Occur.MUST);
		}
		pathsQuery.add(urlQuery, Occur.SHOULD);
	    }
	    rootQuery.add(pathsQuery, Occur.MUST);
	}

	return rootQuery;
    }
}
