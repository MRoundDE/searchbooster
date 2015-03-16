package de.mround.searchbooster.api.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RequestSearchParser provides access to all fields of a valid XML-Request of
 * type Search, defined in the build-in XML-Schema (see
 * {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestSearchParser {

    /**
     * Private constructor to avoid instantiation.
     */
    private RequestSearchParser() {
    }

    /**
     * This method checks, if the given Document is a valid Request of type
     * Search.
     * 
     * @param document
     *            - a Request of type Search
     * @return true if the given Document is a valid Request of type Search
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid
     */
    public static boolean isSearchRequest(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}
	return (RequestParser.isRequest(document) && document
		.getElementsByTagName("Search").getLength() == 1);
    }

    /**
     * This method returns all <code>&lt;Path&gt;</code>-Nodes as a List from a
     * given Request of type Search.<br />
     * <br />
     * Warning: This method relies on document to be a valid Request of type
     * Search. Before calling this method ensure validness by using
     * {@link RequestSearchParser#isSearchRequest(Document)}.
     * 
     * @param document
     *            - a Request of type Search
     * @return all <code>&lt;Path&gt;</code>-Nodes as a List from a given
     *         Request of type Search
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
     * <code>&lt;Path&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Path&gt;</code>-Node of a valid Request of
     *            type Search
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
     * Returns the RecursionDepth of a given <code>&lt;Path&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Path&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Path&gt;</code>-Node of a valid Request of
     *            type Search
     * @return the RecursionDepth of a given <code>&lt;Path&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static int getPathRecursionDepth(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return Integer.parseInt(node.getChildNodes().item(1).getTextContent());
    }

    /**
     * This method returns all <code>&lt;Parameter&gt;</code>-Nodes as a List
     * from a given Request of type Search.<br />
     * <br />
     * Warning: This method relies on document to be a valid Request of type
     * Search. Before calling this method ensure validness by using
     * {@link RequestSearchParser#isSearchRequest(Document)}.
     * 
     * @param document
     *            - a Request of type Search
     * @return all <code>&lt;Parameter&gt;</code>-Nodes as a List from a given
     *         Request of type Search
     * @throws NullPointerException
     *             if document is null
     */
    public static List<Node> getParameterList(Document document) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}

	List<Node> nodeList = new ArrayList<Node>();
	NodeList Parameters = document.getElementsByTagName("Parameter");

	for (int i = 0; i < Parameters.getLength(); i++) {
	    nodeList.add(Parameters.item(i));
	}

	return nodeList;
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code>-Node
     * contains an optional <code>&lt;FileName&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Parameter&gt;</code>-Node of a valid
     *            Request of type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;FileName&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterFileName(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return (node.hasChildNodes() && node.getChildNodes().item(0)
		.getNodeName().equals("FileName"));
    }

    /**
     * Returns the optional FileName of a given <code>&lt;Parameter&gt;</code>
     * -Node.<br />
     * <br />
     * Warning: This method relies on the existence of a
     * <code>&lt;FileName&gt;</code>-Node. Before calling this method ensure
     * validness by using {@link RequestSearchParser#hasParameterFileName(Node)}
     * .
     * 
     * @param node
     *            - a valid <code>&lt;Parameter&gt;</code>-Node of a valid
     *            Request of type Search
     * @return the optional FileName of a given <code>&lt;Parameter&gt;</code>
     *         -Node
     * @throws NullPointerException
     *             if node is null
     */
    public static String getParameterFileName(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return node.getChildNodes().item(0).getTextContent();
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code>-Node
     * contains an optional <code>&lt;ModificationDate&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Parameter&gt;</code>-Node of a valid
     *            Request of type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;ModificationDate&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    private static boolean hasParameterModificationDate(Node node) {
	return node.hasChildNodes()
		&& (node.getChildNodes().item(0).getNodeName()
			.equals("ModificationDate") || ((node.getChildNodes()
			.getLength() > 1) && node.getChildNodes().item(1)
			.getNodeName().equals("ModificationDate")));
    }

    /**
     * Returns the optional ModificationDate of a given
     * <code>&lt;Parameter&gt;</code> -Node.<br />
     * <br />
     * Warning: This method relies on the existence of a
     * <code>&lt;ModificationDate&gt;</code>-Node. Before calling this method
     * ensure validness by using
     * {@link RequestSearchParser#hasParameterModificationDate(Node)}.
     * 
     * @param node
     *            - a valid <code>&lt;Parameter&gt;</code>-Node of a valid
     *            Request of type Search
     * @return the optional ModificationDate of a given
     *         <code>&lt;Parameter&gt;</code> -Node
     * @throws NullPointerException
     *             if node is null
     */
    private static Node getParameterModificationDate(Node node) {
	Node ModificationDate = node.getChildNodes().item(0);

	if ((node.getChildNodes().getLength() > 1)
		&& (!ModificationDate.getNodeName().equals("ModificationDate"))) {
	    ModificationDate = node.getChildNodes().item(1);
	}

	return ModificationDate;
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code>-Node
     * contains an optional <code>&lt;Begin&gt;</code>-Node of an optional
     * <code>&lt;ModificationDate&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;Begin&gt;</code>-Node of an optional
     *         <code>&lt;ModificationDate&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterModificationDateBegin(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}
	return hasParameterModificationDate(node)
		&& getParameterModificationDate(node).getChildNodes().item(0)
			.getNodeName().equals("Begin");
    }

    /**
     * Returns the optional <code>&lt;Begin&gt;</code>-Node of an optional
     * <code>&lt;ModificationDate&gt;</code>-Node of a given
     * <code>&lt;Parameter&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on the existence of a valid
     * <code>&lt;Begin&gt;</code>-Node. Before calling this method ensure
     * validness by using
     * {@link RequestSearchParser#hasParameterModificationDateBegin(Node)} .
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return the optional <code>&lt;Begin&gt;</code>-Node of an optional
     *         <code>&lt;ModificationDate&gt;</code>-Node of a given
     *         <code>&lt;Parameter&gt;</code> -Node
     * @throws NullPointerException
     *             if node is null
     * @throws DatatypeConfigurationException
     *             if the implementation is not available or cannot be
     *             instantiated
     */
    public static XMLGregorianCalendar getParameterModificationDateBegin(
	    Node node) throws DatatypeConfigurationException {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return DatatypeFactory.newInstance().newXMLGregorianCalendar(
		getParameterModificationDate(node).getChildNodes().item(0)
			.getTextContent());
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code>-Node
     * contains an optional <code>&lt;End&gt;</code>-Node of an optional
     * <code>&lt;ModificationDate&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;End&gt;</code>-Node of an optional
     *         <code>&lt;ModificationDate&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterModificationDateEnd(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterModificationDate(node)
		&& (getParameterModificationDate(node).getChildNodes().item(0)
			.getNodeName().equals("End") || ((getParameterModificationDate(
			node).getChildNodes().getLength() > 1) && getParameterModificationDate(
			node).getChildNodes().item(1).getNodeName()
			.equals("End")));
    }

    /**
     * Returns the optional <code>&lt;End&gt;</code>-Node of an optional
     * <code>&lt;ModificationDate&gt;</code>-Node of a given
     * <code>&lt;Parameter&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on the existence of a valid
     * <code>&lt;End&gt;</code>-Node. Before calling this method ensure
     * validness by using
     * {@link RequestSearchParser#hasParameterModificationDateEnd(Node)} .
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return the optional <code>&lt;End&gt;</code>-Node of an optional
     *         <code>&lt;ModificationDate&gt;</code>-Node of a given
     *         <code>&lt;Parameter&gt;</code> -Node
     * @throws NullPointerException
     *             if node is null
     * @throws DatatypeConfigurationException
     *             if the implementation is not available or cannot be
     *             instantiated
     */
    public static XMLGregorianCalendar getParameterModificationDateEnd(Node node)
	    throws DatatypeConfigurationException {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	Node End = getParameterModificationDate(node).getChildNodes().item(0);
	if (getParameterModificationDate(node).getChildNodes().getLength() > 1) {
	    End = getParameterModificationDate(node).getChildNodes().item(1);
	}

	return DatatypeFactory.newInstance().newXMLGregorianCalendar(
		End.getTextContent());
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code>-Node
     * contains an optional <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a valid <code>&lt;Parameter&gt;</code>-Node of a valid
     *            Request of type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;Content&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    private static boolean hasParameterContent(Node node) {
	return node.hasChildNodes()
		&& (node.getChildNodes().item(0).getNodeName()
			.equals("Content")
			|| ((node.getChildNodes().getLength() > 1) && node
				.getChildNodes().item(1).getNodeName()
				.equals("Content")) || ((node.getChildNodes()
			.getLength() > 2) && node.getChildNodes().item(2)
			.getNodeName().equals("Content")));
    }

    /**
     * Returns the optional <code>&lt;Content&gt;</code>-Node of a given
     * <code>&lt;Parameter&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return the optional <code>&lt;Content&gt;</code>-Node of a given
     *         <code>&lt;Parameter&gt;</code>-Node
     */
    private static Node getParameterContent(Node node) {

	Node Content = node.getChildNodes().item(0);
	if (node.getChildNodes().getLength() > 1) {
	    Content = node.getChildNodes().item(1);
	}
	if (node.getChildNodes().getLength() > 2) {
	    Content = node.getChildNodes().item(2);
	}

	return Content;
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code>a-Node
     * contains an optional <code>&lt;Document&gt;</code>-Node of an optional
     * <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;Document&gt;</code>-Node of an optional
     *         <code>&lt;Content&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterContentDocument(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterContent(node)
		&& getParameterContent(node).getChildNodes().item(0)
			.getNodeName().equals("Document");
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code> -Node
     * contains an optional SearchString of an optional
     * <code>&lt;Document&gt;</code>-Node of an optional
     * <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true, if the <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional SearchString if it is a document-content-search
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterContentDocumentSearchString(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterContentDocument(node)
		&& getParameterContent(node).getChildNodes().item(0)
			.hasChildNodes()
		&& getParameterContent(node).getChildNodes().item(0)
			.getChildNodes().item(0).getNodeName()
			.equals("SearchString");
    }

    /**
     * Returns the optional SearchString of a given
     * <code>&lt;Parameter&gt;</code>-Node if it is a document-content-search.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed. Ensure this method to succeed by calling
     * {@link RequestSearchParser#hasParameterContentDocumentSearchString(Node)}
     * before calling this method.
     * 
     * @param node
     *            - <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return the optional SearchString of a given
     *         <code>&lt;Parameter&gt;</code>-Node if it is a
     *         document-content-search
     * @throws NullPointerException
     *             if node is null
     */
    public static String getParameterContentDocumentSearchString(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return getParameterContent(node).getChildNodes().item(0)
		.getChildNodes().item(0).getTextContent();
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code> -Node
     * contains an optional <code>&lt;Music&gt;</code>-Node of an optional
     * <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;Music&gt;</code>-Node of an optional
     *         <code>&lt;Content&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterContentMusic(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterContent(node)
		&& getParameterContent(node).getChildNodes().item(0)
			.getNodeName().equals("Music");
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code> -Node
     * contains an optional Interpret of an optional <code>&lt;Music&gt;</code>
     * -Node of an optional <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true, if the <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional Interpret if it is a music-content-search
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterContentMusicInterpret(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterContentMusic(node)
		&& getParameterContent(node).getChildNodes().item(0)
			.hasChildNodes()
		&& getParameterContent(node).getChildNodes().item(0)
			.getChildNodes().item(0).getNodeName()
			.equals("Interpret");
    }

    /**
     * Returns the optional Interpret of a given <code>&lt;Parameter&gt;</code>
     * -Node if it is a music-content-search.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed. Ensure this method to succeed by calling
     * {@link RequestSearchParser#hasParameterContentMusicInterpret(Node)}
     * before calling this method.
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return the optional Interpret of a given <code>&lt;Parameter&gt;</code>
     *         -Node if it is a music-content-search
     * @throws NullPointerException
     *             if node is null
     */
    public static String getParameterContentMusicInterpret(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return getParameterContent(node).getChildNodes().item(0)
		.getChildNodes().item(0).getTextContent();
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code> -Node
     * contains an optional Album of an optional <code>&lt;Music&gt;</code>
     * -Node of an optional <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true, if the <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional Album if it is a music-content-search
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterContentMusicAlbum(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterContentMusic(node)
		&& getParameterContent(node).getChildNodes().item(0)
			.hasChildNodes()
		&& (getParameterContent(node).getChildNodes().item(0)
			.getChildNodes().item(0).getNodeName().equals("Album") || (getParameterContent(
			node).getChildNodes().item(0).getChildNodes()
			.getLength() > 1 && getParameterContent(node)
			.getChildNodes().item(0).getChildNodes().item(1)
			.getNodeName().equals("Album")));
    }

    /**
     * Returns the optional Album of a given <code>&lt;Parameter&gt;</code>
     * -Node if it is a music-content-search.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed. Ensure this method to succeed by calling
     * {@link RequestSearchParser#hasParameterContentMusicAlbum(Node)} before
     * calling this method.
     * 
     * @param node
     *            - <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return the optional Album of a given <code>&lt;Parameter&gt;</code>
     *         -Node if it is a music-content-search
     * @throws NullPointerException
     *             if node is null
     */
    public static String getParameterContentMusicAlbum(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	Node Album = getParameterContent(node).getChildNodes().item(0)
		.getChildNodes().item(0);
	if (getParameterContent(node).getChildNodes().item(0).getChildNodes()
		.getLength() > 1) {
	    Album = getParameterContent(node).getChildNodes().item(0)
		    .getChildNodes().item(1);
	}

	return Album.getTextContent();
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code>-Node
     * contains an optional <code>&lt;Picture&gt;</code>-Node of an optional
     * <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;Picture&gt;</code>-Node of an optional
     *         <code>&lt;Content&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterContentPicture(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterContent(node)
		&& getParameterContent(node).getChildNodes().item(0)
			.getNodeName().equals("Picture");
    }

    /**
     * This method checks, if the given <code>&lt;Parameter&gt;</code> -Node
     * contains an optional <code>&lt;Video&gt;</code>-Node of an optional
     * <code>&lt;Content&gt;</code>-Node.<br />
     * <br />
     * Warning: This method relies on node to be a valid
     * <code>&lt;Parameter&gt;</code>-Node of a valid Request of type Search. No
     * further checks are performed.
     * 
     * @param node
     *            - a <code>&lt;Parameter&gt;</code>-Node of a valid Request of
     *            type Search
     * @return true if the given <code>&lt;Parameter&gt;</code>-Node contains an
     *         optional <code>&lt;Video&gt;</code>-Node of an optional
     *         <code>&lt;Content&gt;</code>-Node
     * @throws NullPointerException
     *             if node is null
     */
    public static boolean hasParameterContentVideo(Node node) {
	if (node == null) {
	    throw new NullPointerException("Input parameter node is null.");
	}

	return hasParameterContent(node)
		&& getParameterContent(node).getChildNodes().item(0)
			.getNodeName().equals("Video");
    }

}
