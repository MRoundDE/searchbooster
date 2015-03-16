package de.mround.searchbooster.api.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * ResponseBuilder is the root builder of all possible flavors of XML-Responses
 * defined in the build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}
 * ).
 * 
 * @author Kai Torben Ohlhus
 */
public final class ResponseBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private ResponseBuilder() {
    }

    /**
     * This method creates a generic Response.
     * 
     * @param id
     *            - (required) id of the response.
     * @param message
     *            - (required) an info massage
     * @return a generic Response
     * @throws NullPointerException
     *             if message is null
     * @throws IllegalArgumentException
     *             if message is an empty String
     * @throws RuntimeException
     *             if the built Request is invalid or could not be created
     */
    public static Document create(SearchBoosterXML.StatusCodeID id,
	    String message) {
	if (message == null) {
	    throw new NullPointerException("Input parameter message is null.");
	}
	if (id == null) {
	    throw new NullPointerException("Input parameter id is null.");
	}
	if (message.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Input parameter message is an empty String.");
	}
	Document document = null;
	try {
	    document = XMLBuilder.create();
	} catch (ParserConfigurationException e) {
	    throw new RuntimeException(
		    "The Document skeleton could not be created.");
	}

	Element Response = document.createElement("Response");
	Element StatusCode = document.createElement("StatusCode");
	Element ID = document.createElement("ID");
	Element Message = document.createElement("Message");

	ID.setTextContent(id.toString());
	Message.setTextContent(message);

	StatusCode.appendChild(ID);
	StatusCode.appendChild(Message);
	Response.appendChild(StatusCode);

	document.getElementsByTagName("SearchBooster").item(0)
		.appendChild(Response);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "An invalid Response has been constructed.");
	}

	return document;
    }
}
