package de.mround.searchbooster.api.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * ResponseSearchParser provides access to all fields of a valid XML-Response of
 * type Search, defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class ResponseSearchParser {

    /**
     * Private constructor to avoid instantiation.
     */
    private ResponseSearchParser() {
    }

    /**
     * This method checks, if the given Document is a valid Response of type
     * Search.
     * 
     * @param document
     *            - a Response of type Search
     * @return true if the given Document is a valid
     *         Response-MetaSearchResult-XML-Document
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isResponseSearch(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	return ((ResponseParser.isResponse(document)) && (document
		.getElementsByTagName("Search").getLength() == 1));
    }

    /**
     * Returns the ID of a given Response of type Search.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response of type
     * Search. No further checks are performed.
     * 
     * @param document
     *            - a Response of type Search
     * @return the ID of a given Response of type Search
     * @throws NullPointerException
     *             if document is null
     */
    public static StatusCodeID getStatusCodeID(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}

	return ResponseParser.getStatusCodeID(document);
    }

    /**
     * Returns the Message of a given Response of type Search.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response of type
     * Search. No further checks are performed.
     * 
     * @param document
     *            - a Response of type Search
     * @return the Message of a given Response of type Search
     * @throws NullPointerException
     *             if document is null
     */
    public static String getStatusCodeMessage(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}

	return ResponseParser.getStatusCodeMessage(document);
    }

    /**
     * Returns a list with access to all <code>&lt;Result&gt;</code>-Nodes from
     * a given Response of type Search.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response of type
     * Search. No further checks are performed.
     * 
     * @param document
     *            - a Response of type Search
     * @return a list with access to all <code>&lt;Result&gt;</code>-Nodes from
     *         a given Response of type Search
     * @throws NullPointerException
     *             if document is null
     */
    public static List<Node> getResultList(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}

	List<Node> nodeList = new ArrayList<Node>();

	NodeList Results = document.getElementsByTagName("Result");
	for (int i = 0; i < Results.getLength(); i++) {
	    nodeList.add(Results.item(i));
	}

	return nodeList;
    }

    /**
     * Returns the URL of a given <code>&lt;Result&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Result&gt;</code>-Node of a valid Response of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Result&gt;</code>-Node of a valid Response
     *            of type Search
     * @return the URL of a given <code>&lt;Result&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static String getResultURL(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return node.getChildNodes().item(0).getTextContent();
    }

    /**
     * Returns the ModificationDate of a given <code>&lt;Result&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Result&gt;</code>-Node of a valid Response of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Result&gt;</code>-Node of a valid Response
     *            of type Search
     * @return the ModificationDate of a given <code>&lt;Result&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     * @throws DatatypeConfigurationException
     *             if the implementation is not available or cannot be
     *             instantiated.
     */
    public static XMLGregorianCalendar getResultModificationDate(Node node)
	    throws DatatypeConfigurationException {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return DatatypeFactory.newInstance().newXMLGregorianCalendar(
		node.getChildNodes().item(1).getTextContent());
    }

    /**
     * Returns the ContentMatch of a given <code>&lt;Result&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Result&gt;</code>-Node of a valid Response of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Result&gt;</code>-Node of a valid Response
     *            of type Search
     * @return the ContentMatch of a given <code>&lt;Result&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean getResultContentMatch(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return Boolean.parseBoolean(node.getChildNodes().item(2)
		.getTextContent());
    }
}
