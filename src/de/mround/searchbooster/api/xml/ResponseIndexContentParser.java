package de.mround.searchbooster.api.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

/**
 * ResponseIndexContentParser provides access to all fields of a valid
 * XML-Response of type IndexContent, defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class ResponseIndexContentParser {

    /**
     * Private constructor to avoid instantiation.
     */
    private ResponseIndexContentParser() {
    }

    /**
     * This method checks, if the given Document is a valid Response of type
     * IndexContent.
     * 
     * @param document
     *            - a Response of type IndexContent
     * @return true if the given Document is a valid Response of type
     *         IndexContent
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isResponseIndexContent(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	return ((ResponseParser.isResponse(document)) && (document
		.getElementsByTagName("IndexContent").getLength() == 1));
    }

    /**
     * Returns the ID of a given Response of type IndexContent.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response of type
     * IndexContent. No further checks are performed.
     * 
     * @param document
     *            - a Response of type IndexContent
     * @return the ID of a given Response of type IndexContent
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
     * Returns the Message of a given Response of type IndexContent.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response of type
     * IndexContent. No further checks are performed.
     * 
     * @param document
     *            - a Response of type IndexContent
     * @return the Message of a given Response of type IndexContent
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
     * This method returns all <code>&lt;Path&gt;</code>-Nodes as a List from a
     * given Response of type IndexContent.<br />
     * <br />
     * Warning: This method relies on document to be a valid Response of type
     * IndexContent. Before calling this method ensure validness by using
     * {@link ResponseIndexContentParser#isResponseIndexContent(Document)}.
     * 
     * @param document
     *            - a Response of type IndexContent
     * @return all <code>&lt;Path&gt;</code>-Nodes as a List from a given
     *         Response of type IndexContent
     * @throws NullPointerException
     *             if document is null
     */
    public static List<Node> getPathList(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}

	List<Node> nodeList = new ArrayList<Node>();
	NodeList Paths = document.getElementsByTagName("Path");

	for (int i = 0; i < Paths.getLength(); i++) {
	    nodeList.add(Paths.item(i));
	}

	return nodeList;
    }

    /**
     * Returns the URL of a given <code>&lt;Path&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Path&gt;</code>-Node of a valid Response of type IndexContent.
     * No further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Path&gt;</code>-Node of a valid Response of type
     *            IndexContent
     * @return the URL of a given <code>&lt;Path&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static String getPathURL(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return node.getChildNodes().item(0).getTextContent();
    }

    /**
     * Returns the recursion depth of a given <code>&lt;Path&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Path&gt;</code>-Node of a valid Response of type IndexContent.
     * No further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Path&gt;</code>-Node of a valid Response of type
     *            IndexContent
     * @return the recursion depth of a given <code>&lt;Path&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static int getPathRecursionDepth(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return Integer.parseInt(node.getChildNodes().item(1).getTextContent());
    }

}
