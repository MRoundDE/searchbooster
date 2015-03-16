package de.mround.searchbooster.api.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * ResponseParser provides access to all fields of a valid XML-Response, defined
 * in the build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class ResponseParser {

    /**
     * Private constructor to avoid instantiation.
     */
    private ResponseParser() {
    }

    /**
     * This method checks, if the given Document is a valid Response.
     * 
     * @param document
     *            - a Response
     * @return true if the given Document is a valid Response
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isResponse(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	return (document.getElementsByTagName("Response").getLength() == 1);
    }

    /**
     * Returns the ID of a given Response.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response. No
     * further checks are performed.
     * 
     * @param document
     *            - a Response
     * @return the ID of a given Response
     */
    public static SearchBoosterXML.StatusCodeID getStatusCodeID(
	    Document document) {
	Node StatusCode = document.getElementsByTagName("StatusCode").item(0);
	return SearchBoosterXML.StatusCodeID.valueOf(StatusCode.getChildNodes()
		.item(0).getTextContent());
    }

    /**
     * Returns the Message of a given Response.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response. No
     * further checks are performed.
     * 
     * @param document
     *            - a Response
     * @return the Message of a given Response
     */
    public static String getStatusCodeMessage(Document document) {
	Node StatusCode = document.getElementsByTagName("StatusCode").item(0);
	return StatusCode.getChildNodes().item(1).getTextContent();
    }

}
