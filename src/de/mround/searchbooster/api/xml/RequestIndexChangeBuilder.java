package de.mround.searchbooster.api.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * RequestIndexChangeBuilder builds XML-Requests of type Index and subtype
 * Change, defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()} ).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestIndexChangeBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestIndexChangeBuilder() {
    }

    /**
     * This method appends the following Node-structure to a Index Change
     * Request.<br />
     * <br />
     * <code>
     * &lt;Path&gt;<br />
     * &nbsp;&nbsp;&lt;URL&gt;url&lt;URL&gt;<br />
     * &nbsp;&nbsp;&lt;RecursionDepth&gt;recursiondepth&lt;RecursionDepth&gt;<br />
     * &lt;/Path&gt;
     * </code>
     * 
     * @param document
     *            - a Index Change Request
     * @param url
     *            - (required) path to be indexed
     * @param recursiondepth
     *            - (required) file tree recursion depth. The value -1 means
     *            full recursive and the value 0 means no recursion
     * @throws NullPointerException
     *             if document is null or if url is null
     * @throws IllegalArgumentException
     *             if url is an empty String
     * @throws RuntimeException
     *             if document is invalid or if document does not contain a
     *             &lt;Change&gt;-Node or if appending to document results in an
     *             invalid Document
     */
    public static void appendPath(Document document, String url,
	    int recursiondepth) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (url == null) {
	    throw new NullPointerException("Input parameter url is null.");
	}
	if (url.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Input parameter url is an empty String.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	if (document.getElementsByTagName("Change").getLength() != 1) {
	    throw new RuntimeException(
		    "Input parameter document must be created with "
			    + "RequestIndexChangeBuilder.create(...).");
	}

	Node Change = document.getElementsByTagName("Change").item(0);

	Element Path = document.createElement("Path");
	Element URL = document.createElement("URL");
	Element RecursionDepth = document.createElement("RecursionDepth");

	URL.setTextContent(url);
	RecursionDepth.setTextContent(Integer.toString(recursiondepth));

	Path.appendChild(URL);
	Path.appendChild(RecursionDepth);
	Change.appendChild(Path);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "Appending to document resulted in an invalid Document.");
	}
    }

    /**
     * This method creates a Request to change the content of the persistent
     * index.<br />
     * <br />
     * <b>Warning</b>: If no &lt;Path /&gt;-Nodes were appended, this Request
     * will delete the whole index. All paths in the index, this Request does
     * not contain, will be deleted from the index.
     * 
     * @param forcerebuild
     *            - (required) indicates, if the whole index should be rebuild
     *            from scratch with the paths given in this Request. Otherwise
     *            the index will be updated to the given path information.
     * @return a Request to change the content of the persistent index
     * @throws RuntimeException
     *             if the built Request is invalid or could not be created
     */
    public static Document create(boolean forcerebuild) {

	Document document = null;
	try {
	    document = RequestIndexBuilder.create();
	} catch (ParserConfigurationException e) {
	    throw new RuntimeException(
		    "The Document skeleton could not be created.");
	}

	Node Index = document.getElementsByTagName("Index").item(0);

	Element Change = document.createElement("Change");
	Element ForceRebuild = document.createElement("ForceRebuild");
	ForceRebuild.setTextContent(Boolean.toString(forcerebuild));

	Change.appendChild(ForceRebuild);
	Index.appendChild(Change);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "An invalid Request has been constructed.");
	}

	return document;
    }

}
