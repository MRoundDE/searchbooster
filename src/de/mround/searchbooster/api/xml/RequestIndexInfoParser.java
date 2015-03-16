package de.mround.searchbooster.api.xml;

import org.w3c.dom.Document;

/**
 * RequestIndexChangeParser provides access to all fields of a valid XML-Request
 * of type Index and subtype Info, defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestIndexInfoParser {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestIndexInfoParser() {
    }

    /**
     * This method checks, if the given Document is a valid Request of type
     * Index and subtype Info.
     * 
     * @param document
     *            - a Request of type Index and subtype Info
     * @return true if the given Document is a valid Request of type Index and
     *         subtype Info
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isIndexInfoRequest(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	return (RequestParser.isRequest(document)
		&& (document.getElementsByTagName("Index").getLength() == 1) && (document
		.getElementsByTagName("Info").getLength() == 1));
    }

}
