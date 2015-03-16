package de.mround.searchbooster.api.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * RequestIndexInfoBuilder builds XML-Requests of type Index and subtype Info,
 * defined in the build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}
 * ).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestIndexInfoBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestIndexInfoBuilder() {
    }

    /**
     * This method creates a Request to get information about the content of the
     * persistent index.
     * 
     * @param str
     *            - (optional, null allowed) not specified
     * @return a Request to get information about the content of the persistent
     *         index
     * @throws RuntimeException
     *             if the built Request is invalid or could not be created
     */
    public static Document create(String str) {
	if (str == null) {
	    str = "";
	}

	Document document = null;
	try {
	    document = RequestIndexBuilder.create();
	} catch (ParserConfigurationException e) {
	    throw new RuntimeException(
		    "The Document skeleton could not be created.");
	}

	Node Index = document.getElementsByTagName("Index").item(0);

	Element Info = document.createElement("Info");
	Info.setTextContent(str);

	Index.appendChild(Info);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "An invalid Request has been constructed.");
	}

	return document;
    }

}
