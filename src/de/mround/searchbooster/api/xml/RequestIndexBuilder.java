package de.mround.searchbooster.api.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * RequestIndexBuilder builds XML-Requests of type Index, defined in the
 * build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestIndexBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestIndexBuilder() {
    }

    /**
     * This method creates a new Document skeleton for all possible flavors of
     * Requests for the index.
     * 
     * @return a new Document skeleton for all possible flavors of Requests for
     *         the index
     * @throws ParserConfigurationException
     *             if the
     *             {@link javax.xml.parsers.DocumentBuilderFactory#newInstance()}
     *             implementation is not available or cannot be instantiated.
     */
    protected static Document create() throws ParserConfigurationException {
	Document document = RequestBuilder.create();

	Element Index = document.createElement("Index");
	document.getElementsByTagName("Request").item(0).appendChild(Index);

	return document;
    }
}
