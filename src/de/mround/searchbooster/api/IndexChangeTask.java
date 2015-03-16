package de.mround.searchbooster.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.Configuration.Index;
import de.mround.searchbooster.api.xml.RequestIndexChangeParser;
import de.mround.searchbooster.api.xml.ResponseBuilder;
import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * An IndexChangeTask changes the content of the persistent index.
 * 
 * @author Kai Torben Ohlhus
 */
public final class IndexChangeTask implements Runnable {
    private final BlockingQueue<Document> responseQueue;
    private final Document request;
    private final LuceneDirectory persistent;

    /**
     * Constructor.
     * 
     * @param responseQueue
     *            - a {@link BlockingQueue} to put the response messages in
     * @param request
     *            - the index change request to be performed
     * @param persistent
     *            - a reference to the persistent index
     */
    protected IndexChangeTask(BlockingQueue<Document> responseQueue,
	    Document request, LuceneDirectory persistent) {
	this.responseQueue = responseQueue;
	this.request = request;
	this.persistent = persistent;
    }

    @Override
    public void run() {
	System.out.println("[info] A new IndexChangeTask has been started.");

	Document response = ResponseBuilder.create(StatusCodeID.ERROR,
		"Failed to change the index content.");

	try {
	    // get a model of the future index content without url duplicates
	    Map<String, Integer> futureIndexMap = new HashMap<String, Integer>();
	    for (Node n : RequestIndexChangeParser.getPathList(this.request)) {
		String url = Utility.canonicalizePath(RequestIndexChangeParser
			.getPathURL(n));
		int r = RequestIndexChangeParser.getPathRecursionDepth(n);

		// if url already present and not recursive, prefer new entry
		if (futureIndexMap.containsKey(url)) {
		    if ((r < 0) && (futureIndexMap.get(url).intValue() >= 0)) {
			futureIndexMap.remove(url);
		    } else {
			continue;
		    }
		}
		futureIndexMap.put(url, r);
	    }

	    // get a model of the future index content without subdirectories
	    // that will already be covered by recursive parent directories
	    Map<String, Integer> futureIndexMapCopy = new HashMap<String, Integer>(
		    futureIndexMap);
	    for (String s1 : futureIndexMapCopy.keySet()) {
		for (String s2 : futureIndexMapCopy.keySet()) {
		    // remove if a parent directory is already recursive
		    if (Utility.isSubDirectory(s2, s1)
			    && futureIndexMapCopy.get(s2).intValue() < 0) {
			futureIndexMap.remove(s1);
			break;
		    }
		}
	    }

	    // get a model of the current index content (if a rebuild is forced,
	    // the map will just stay empty)
	    Map<String, Integer> alreadyInIndexMap = this.persistent
		    .getIndexContentMap();

	    if (RequestIndexChangeParser.getForceRebuild(this.request)) {
		System.out.println("[info] Rebuilding of the persistent index"
			+ " has been requested.");
		// TODO maybe a backup, in case of cancellation?!
		this.persistent.clearDirectory();
		alreadyInIndexMap.clear();
	    }

	    // get a model of the current index content without
	    // fully-recursive-entries => delete them from the index
	    Map<String, Integer> toDeleteMap = new HashMap<String, Integer>();
	    for (String s : alreadyInIndexMap.keySet()) {
		if (alreadyInIndexMap.get(s).intValue() >= 0) {
		    toDeleteMap.put(s, alreadyInIndexMap.get(s));
		}
	    }
	    for (String s : toDeleteMap.keySet()) {
		alreadyInIndexMap.remove(s);
	    }

	    // get a model of the current index content without
	    // entries, that are not present in the future index => delete them
	    // from the index
	    for (String s : alreadyInIndexMap.keySet()) {
		if (!futureIndexMap.containsKey(s)) {
		    toDeleteMap.put(s, alreadyInIndexMap.get(s));
		}
	    }
	    for (String s : toDeleteMap.keySet()) {
		alreadyInIndexMap.remove(s);
	    }

	    // perform current index deletion
	    if (toDeleteMap.size() > 0) {
		for (String s : toDeleteMap.keySet()) {
		    Query q = null;
		    if (toDeleteMap.get(s).intValue() < 0) {
			q = new PrefixQuery(new Term(Index.META_PATH, s));
		    } else {
			q = new TermQuery(new Term(Index.META_PATH, s));
		    }

		    this.persistent.deleteFromIndex(q);
		    if (toDeleteMap.get(s).intValue() < 0) {
			q = new PrefixQuery(new Term(Index.URL, s));
		    } else {
			q = new TermQuery(new Term(Index.URL, s));
		    }
		    this.persistent.deleteFromIndex(q);
		}
	    }

	    // get the final model of the future index content without entries,
	    // that are already completely contained in the index
	    for (String s : alreadyInIndexMap.keySet()) {
		futureIndexMap.remove(s);
	    }

	    System.out.println("[info] Need to index at least "
		    + futureIndexMap.size() + " directory(s).");

	    LuceneDirectory.IndexResult indexResult = null;
	    try {
		indexResult = this.persistent
			.addContentMapToIndex(futureIndexMap);
	    } catch (InterruptedException ignore) {
	    }

	    response = ResponseBuilder.create(StatusCodeID.SUCCESSFUL,
		    "Adding " + indexResult.getNumIndexed()
			    + " files to the permanent index took "
			    + indexResult.getTimeMillis() + " milliseconds. "
			    + indexResult.getNumIndexErrors()
			    + " files were erroneous.");
	} finally {
	    try {
		this.responseQueue.put(response);
	    } catch (InterruptedException ignore) {
	    }
	    System.out.println("[info] IndexChangeTask completed.");
	}
    }
}
