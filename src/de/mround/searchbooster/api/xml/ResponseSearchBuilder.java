package de.mround.searchbooster.api.xml;

import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * ResponseSearchBuilder builds XML-Responses of type Search, defined in the
 * build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class ResponseSearchBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private ResponseSearchBuilder() {
    }

    /**
     * This method appends the following Node-structure to a Search Response.<br />
     * <br />
     * <code>
     * &lt;Result&gt;<br />
     * &nbsp;&nbsp;&lt;URL&gt;url&lt;URL&gt;<br />
     * &nbsp;&nbsp;&lt;ModificationDate&gt;modificationdate&lt;/ModificationDate&gt;<br />
     * &nbsp;&nbsp;&lt;ContentMatch&gt;contentmatch&lt;/ContentMatch&gt;<br />
     * &lt;/Result&gt;
     * </code>
     * 
     * @param document
     *            - a Search Response
     * @param url
     *            - (required) URL of the search result
     * @param modificationdate
     *            - (required) date of the last modification of the search
     *            result
     * @param contentmatch
     *            - (required) indicates if the search matched the content of
     *            the document
     * @throws NullPointerException
     *             if document is null or if url is null or if modificationdate
     *             is null
     * @throws IllegalArgumentException
     *             if url is an empty String
     * @throws RuntimeException
     *             if document is an invalid Document or if document is no
     *             Search Response or if appending to document results in an
     *             invalid document
     */
    public static void appendSearchResult(Document document, String url,
	    XMLGregorianCalendar modificationdate, boolean contentmatch) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (url == null) {
	    throw new NullPointerException("Input parameter url is null.");
	}
	if (modificationdate == null) {
	    throw new NullPointerException(
		    "Input parameter modificationdate is null.");
	}
	if (url.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Input parameter url is an empty String.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	if (document.getElementsByTagName("IndexContent").getLength() > 0) {
	    throw new RuntimeException(
		    "Input parameter document must be created with "
			    + "ResponseSearchBuilder.create(...).");
	}

	Node Search = document.getElementsByTagName("Search").item(0);

	Element Result = document.createElement("Result");
	Element URL = document.createElement("URL");
	Element ModificationDate = document.createElement("ModificationDate");
	Element ContentMatch = document.createElement("ContentMatch");

	URL.setTextContent(url);
	ModificationDate.setTextContent(modificationdate.toXMLFormat());
	ContentMatch.setTextContent(Boolean.toString(contentmatch));

	Result.appendChild(URL);
	Result.appendChild(ModificationDate);
	Result.appendChild(ContentMatch);
	Search.appendChild(Result);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "Appending to document resulted in an invalid Document.");
	}
    }

    /**
     * This method creates a Response with results of a search.
     * 
     * @param id
     *            - (required) id == 0 indicates success. id != 0 indicates a
     *            problem further described in message.
     * @param message
     *            - (required) an info massage
     * @return a Response with results of a search
     * @throws NullPointerException
     *             if message is null
     * @throws IllegalArgumentException
     *             if message is an empty String
     * @throws RuntimeException
     *             if the build Response-XML-Document is invalid or could not be
     *             created
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

	Element Search = document.createElement("Search");

	Response.appendChild(Search);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "An invalid Response has been constructed.");
	}

	return document;
    }

}
