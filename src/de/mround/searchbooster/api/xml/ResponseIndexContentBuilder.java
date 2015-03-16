package de.mround.searchbooster.api.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * ResponseIndexContentBuilder builds XML-Responses of type IndexContent,
 * defined in the build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}
 * ).
 * 
 * @author Kai Torben Ohlhus
 */
public final class ResponseIndexContentBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private ResponseIndexContentBuilder() {
    }

    /**
     * This method appends the following Node-structure to a IndexContent
     * Response.<br />
     * <br />
     * <code>
     * &lt;Path&gt;<br />
     * &nbsp;&nbsp;&lt;URL&gt;url&lt;URL&gt;<br />
     * &nbsp;&nbsp;&lt;RecursionDepth&gt;recursiondepth&lt;RecursionDepth&gt;<br />
     * &lt;/Path&gt;
     * </code>
     * 
     * @param document
     *            - a IndexContent Response
     * @param url
     *            - (required) path in the index
     * @param recursiondepth
     *            - (required) file tree recursion depth. The value -1 means
     *            full recursive and the value 0 means no recursion
     * @throws NullPointerException
     *             if document is null or if url is null
     * @throws IllegalArgumentException
     *             if url is an empty String
     * @throws RuntimeException
     *             if document is invalid or if document is no IndexContent
     *             Response or if appending to document results in an invalid
     *             Document
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
	if (document.getElementsByTagName("IndexContent").getLength() != 1) {
	    throw new RuntimeException(
		    "Input parameter document must be created with "
			    + "ResponseIndexContentBuilder.create(...).");
	}

	Node IndexContent = document.getElementsByTagName("IndexContent").item(
		0);

	Element Path = document.createElement("Path");
	Element URL = document.createElement("URL");
	Element RecursionDepth = document.createElement("RecursionDepth");

	URL.setTextContent(url);
	RecursionDepth.setTextContent(Integer.toString(recursiondepth));

	Path.appendChild(URL);
	Path.appendChild(RecursionDepth);
	IndexContent.appendChild(Path);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "Appending to document resulted in an invalid Document.");
	}
    }

    /**
     * This method creates a Response with information about the persisten
     * index.
     * 
     * @param id
     *            - (required) id == 0 indicates success. id != 0 indicates a
     *            problem further described in message.
     * @param message
     *            - (required) an info massage
     * @return a Response with information about the persisten index
     * @throws NullPointerException
     *             if message is null
     * @throws IllegalArgumentException
     *             if message is an empty String
     * @throws RuntimeException
     *             if the created document is invalid or could not be created
     */
    public static Document create(StatusCodeID id, String message) {
	if (message == null) {
	    throw new NullPointerException("Input parameter message is null.");
	}
	if (message.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Input parameter message is an empty String.");
	}

	Document document = ResponseBuilder.create(id, message);

	Node Response = document.getElementsByTagName("Response").item(0);
	Element IndexContent = document.createElement("IndexContent");
	Response.appendChild(IndexContent);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "An invalid Response has been constructed.");
	}

	return document;
    }

}
