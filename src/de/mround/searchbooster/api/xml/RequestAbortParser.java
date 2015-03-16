package de.mround.searchbooster.api.xml;

import org.w3c.dom.Document;

/**
 * RequestAbortParser provides access to all fields of a valid XML-Request of
 * type Abort, defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestAbortParser {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestAbortParser() {
    }

    /**
     * This method checks, if the given Document is a valid Request of type
     * Abort.
     * 
     * @param document
     *            - a Request of type Abort
     * @return true if the given Document is a valid Request of type Abort
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isAbortRequest(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "Input parameter document is an invalid Document.");
	}
	return (RequestParser.isRequest(document) && document
		.getElementsByTagName("Abort").getLength() == 1);
    }

}
