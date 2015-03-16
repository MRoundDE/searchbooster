package de.mround.searchbooster.api.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RequestIndexChangeParser provides access to all fields of a valid XML-Request
 * of type Index and subtype Change, defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestIndexChangeParser {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestIndexChangeParser() {
    }

    /**
     * This method checks, if the given Document is a valid Request of type
     * Index and subtype Change.
     * 
     * @param document
     *            - a Request of type Index and subtype Change
     * @return true if the given Document is a valid Request of type Index and
     *         subtype Change
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isIndexChangeRequest(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	return (RequestParser.isRequest(document)
		&& (document.getElementsByTagName("Index").getLength() == 1) && (document
		.getElementsByTagName("Change").getLength() == 1));
    }

    /**
     * This method returns the boolean value of the
     * <code>&lt;ForceRebuild&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on document to be a valid Request of type
     * Index and subtype Change. Before calling this method ensure validness by
     * using {@link RequestIndexChangeParser#isIndexChangeRequest(Document)}.
     * 
     * @param document
     *            - a Request of type Index and subtype Change
     * @return the boolean value of the <code>&lt;ForceRebuild&gt;</code>-Node
     * @throws NullPointerException
     *             if document is null
     */
    public static boolean getForceRebuild(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}

	Node ForceRebuild = document.getElementsByTagName("ForceRebuild").item(
		0);

	return Boolean.parseBoolean(ForceRebuild.getTextContent());
    }

    /**
     * This method returns all <code>&lt;Path&gt;</code>-Nodes as a List from a
     * given Request of type Index and subtype Change.<br />
     * <br />
     * Warning: This method relies on document to be a valid Request of type
     * Index and subtype Change. Before calling this method ensure validness by
     * using {@link RequestIndexChangeParser#isIndexChangeRequest(Document)}.
     * 
     * @param document
     *            - a Request of type Index and subtype Change
     * @return all <code>&lt;Path&gt;</code>-Nodes as a List from a given
     *         Request of type Index and subtype Change
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
     * <code>&lt;Path&gt;</code>-Node of a valid Request of type Index and
     * subtype Change. No further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Path&gt;</code>-Node of a valid Request of type
     *            Index and subtype Change
     * @return the URL of a given <code>&lt;Path /&gt;</code>-Node
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
     * Returns the RecursionDepth of a given <code>&lt;Path&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Path&gt;</code>-Node of a valid Request of type Index and
     * subtype Change. No further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Path&gt;</code>-Node of a valid Request of type
     *            Index and subtype Change
     * @return the RecursionDepth of a given <code>&lt;Path /&gt;</code>-Node
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
