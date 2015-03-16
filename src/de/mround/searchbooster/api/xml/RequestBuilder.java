package de.mround.searchbooster.api.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * RequestBuilder is the root builder of all possible flavors of XML-Requests
 * defined in the build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}
 * ).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestBuilder() {
    }

    /**
     * This method creates a new Document skeleton for all possible flavors of
     * Requests.
     * 
     * @return a new Document skeleton for all possible flavors of Requests
     * @throws ParserConfigurationException
     *             if the
     *             {@link javax.xml.parsers.DocumentBuilderFactory#newInstance()}
     *             implementation is not available or cannot be instantiated.
     */
    protected static Document create() throws ParserConfigurationException {
	Document document = XMLBuilder.create();

	Element Response = document.createElement("Request");
	document.getElementsByTagName("SearchBooster").item(0)
		.appendChild(Response);

	return document;
    }

}
