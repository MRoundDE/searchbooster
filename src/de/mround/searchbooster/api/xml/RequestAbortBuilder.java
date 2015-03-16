package de.mround.searchbooster.api.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * AbortRequestBuilder builds XML-Requests of type Abort, defined in the
 * build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestAbortBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestAbortBuilder() {
    }

    /**
     * This method creates a Request to abort any previous requested action.
     * 
     * @param str
     *            - (optional, null allowed) info message
     * @return a Request to abort any previous requested action
     * @throws RuntimeException
     *             if the built Request is invalid or could not be created
     */
    public static Document create(String str) {
	if (str == null) {
	    str = "";
	}

	Document document = null;
	try {
	    document = RequestBuilder.create();
	} catch (ParserConfigurationException e) {
	    throw new RuntimeException(
		    "The Document skeleton could not be created.");
	}

	Node Request = document.getElementsByTagName("Request").item(0);

	Element Abort = document.createElement("Abort");
	Abort.setTextContent(str);

	Request.appendChild(Abort);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "An invalid Request has been constructed.");
	}

	return document;
    }

}
