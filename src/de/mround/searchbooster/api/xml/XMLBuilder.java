package de.mround.searchbooster.api.xml;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XMLBuilder is the root builder of all possible flavors of Requests and
 * Responses defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class XMLBuilder {

    /*
     * Current Version String
     */
    private static final String currentVersion = "2011-12-12_1";

    /**
     * Private constructor avoids instantiation.
     */
    private XMLBuilder() {
    }

    /**
     * This method creates a new XML-Document-skeleton for all possible flavors
     * of requests and responses defined in the build-in XML-Schema (see
     * {@link SearchBoosterXML#getSchema()}).
     * 
     * @return a new XML-skeleton for all possible flavors of Requests and
     *         Responses
     * @throws ParserConfigurationException
     *             if the
     *             {@link javax.xml.parsers.DocumentBuilderFactory#newInstance()}
     *             implementation is not available or cannot be instantiated.
     */
    protected static Document create() throws ParserConfigurationException {
	Document document = DocumentBuilderFactory.newInstance()
		.newDocumentBuilder().newDocument();
	Element SearchBooster = document.createElement("SearchBooster");
	SearchBooster.setAttribute("Version", currentVersion);
	document.appendChild(SearchBooster);
	return document;
    }

}
