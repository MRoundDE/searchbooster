package de.mround.searchbooster.api.xml;

import org.w3c.dom.Document;

/**
 * RequestParser provides access to all fields of a valid XML-Request, defined
 * in the build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestParser {

    /**
     * Private constructor to avoid instantiation.
     */
    private RequestParser() {
    }

    /**
     * This method checks, if the given Document is a valid Request.
     * 
     * @param document
     *            - a Request
     * @return true if the given Document is a valid Request
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isRequest(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	return (document.getElementsByTagName("Request").getLength() == 1);
    }

}
