package de.mround.searchbooster.api.xml;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RequestSearchBuilder builds XML-Requests of type Search, defined in the
 * build-in XML-Schema (see {@link SearchBoosterXML#getSchema()}).
 * 
 * @author Kai Torben Ohlhus
 */
public final class RequestSearchBuilder {

    /**
     * Private constructor avoids instantiation.
     */
    private RequestSearchBuilder() {
    }

    /**
     * Content is a nested class to generate a representation for special
     * content to be searched for.
     */
    public static class Content {
	protected final FileType type;
	protected final String s1; // Interpret or SearchString
	protected final String s2; // Album

	/**
	 * All available content types.
	 */
	public static enum FileType {
	    ALL, DOCUMENT, MUSIC, PICTURE, VIDEO
	};

	/**
	 * Private constructor avoids unwanted instantiation.
	 */
	private Content(FileType t, String s1, String s2) {
	    this.type = t;
	    this.s1 = s1;
	    this.s2 = s2;
	}

	/**
	 * Create new document content to search for.
	 * 
	 * @param searchstring
	 *            - a String to search for
	 * @return new document content to search for
	 */
	public static Content newDocumentContent(String searchstring) {
	    return new Content(FileType.DOCUMENT, searchstring, "");
	}

	/**
	 * Create new music content to search for.
	 * 
	 * @param interpret
	 *            - interpret to search for
	 * @param album
	 *            - album to search for
	 * @return new music content to search for
	 */
	public static Content newMusicContent(String interpret, String album) {
	    return new Content(FileType.MUSIC, interpret, album);
	}

	/**
	 * Create new picture content to search for.
	 * 
	 * @return new picture content to search for
	 */
	public static Content newPictureContent() {
	    return new Content(FileType.PICTURE, "", "");
	}

	/**
	 * Create new video content to search for.
	 * 
	 * @return new video content to search for
	 */
	public static Content newVideoContent() {
	    return new Content(FileType.VIDEO, "", "");
	}
    }

    /**
     * Private helper function to remove empty <code>&lt;Parameter&gt;</code>
     * -Nodes in a Search Request.
     * 
     * @param document
     *            - a Search Request
     * @return document without empty <code>&lt;Parameter&gt;</code>-Nodes
     */
    private static Document trimEmptyParameterNodes(Document document) {
	NodeList parameters = document.getElementsByTagName("Parameter");
	for (int i = 0; ((i < parameters.getLength()) && (parameters
		.getLength() > 1)); i++) {
	    if (!parameters.item(i).hasChildNodes()) {
		parameters.item(i).getParentNode()
			.removeChild(parameters.item(i));
	    }
	}

	return document;
    }

    /**
     * This method appends the following Node-structure to a Search Request.<br />
     * <br />
     * <code>
     * &lt;Path&gt;<br />
     * &nbsp;&nbsp;&lt;URL&gt;url&lt;URL&gt;<br />
     * &nbsp;&nbsp;&lt;RecursionDepth&gt;recursiondepth&lt;RecursionDepth&gt;<br />
     * &lt;/Path&gt;
     * </code>
     * 
     * @param document
     *            - a Search Request
     * @param url
     *            - (required) path to search in
     * @param recursiondepth
     *            - (required) file tree recursion depth. The value -1 means
     *            full recursive and the value 0 means no recursion
     * @throws NullPointerException
     *             if document is null or if url is null
     * @throws IllegalArgumentException
     *             if url is an empty String
     * @throws RuntimeException
     *             if document is invalid or if appending to document results in
     *             an invalid Document
     */
    public static void appendPath(Document document, String url,
	    int recursiondepth) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (url == null) {
	    throw new NullPointerException("Input parameter url is null.");
	}
	if (url.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Input parameter url is an empty String.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}

	Node Search = document.getElementsByTagName("Search").item(0);
	Node FirstParameter = document.getElementsByTagName("Parameter")
		.item(0);

	Element Path = document.createElement("Path");
	Element URL = document.createElement("URL");
	Element RecursionDepth = document.createElement("RecursionDepth");

	URL.setTextContent(url);
	RecursionDepth.setTextContent(Integer.toString(recursiondepth));

	Path.appendChild(URL);
	Path.appendChild(RecursionDepth);
	Search.insertBefore(Path, FirstParameter);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "Appending to document resulted in an invalid Document.");
	}
    }

    /**
     * This method appends the following Node-structure to a Search Request.<br />
     * <br />
     * <code>
     * &lt;Path&gt;<br />
     * &nbsp;&nbsp;&lt;FileName&gt;filename&lt;FileName&gt;<br />
     * &nbsp;&nbsp;&lt;ModificationDate&gt;<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;Begin&gt;modificationdatebegin&lt;/Begin&gt;<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;End&gt;modificationdateend&lt;/End&gt;<br />
     * &nbsp;&nbsp;&lt;/ModificationDate&gt;<br />
     * &nbsp;&nbsp;&lt;Content&gt;<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;...<br />
     * &nbsp;&nbsp;&lt;/Content&gt;<br />
     * &lt;/Path&gt;
     * </code>
     * 
     * @param document
     *            - a Search Request
     * @param filename
     *            - (optional, null allowed) name of the searched file
     * @param modificationdatebegin
     *            - (optional, null allowed) begin of the time interval of the
     *            last modification of the document
     * @param modificationdateend
     *            - (optional, null allowed) end of the time interval of the
     *            last modification of the document
     * @param content
     *            - (optional, null allowed) special content search options
     * @throws NullPointerException
     *             if document is null
     * @throws RuntimeException
     *             if document is invalid or if appending to document results in
     *             an invalid Document
     */
    public static void appendParameter(Document document, String filename,
	    XMLGregorianCalendar modificationdatebegin,
	    XMLGregorianCalendar modificationdateend, Content content) {
	if (document == null) {
	    throw new NullPointerException("Input parameter document is null.");
	}
	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException("Input parameter document is invalid.");
	}

	Node Search = document.getElementsByTagName("Search").item(0);
	Element Parameter = document.createElement("Parameter");

	if (filename != null && !filename.isEmpty()) {
	    Element FileName = document.createElement("FileName");
	    FileName.setTextContent(filename);
	    Parameter.appendChild(FileName);
	}

	if ((modificationdatebegin != null) || (modificationdateend != null)) {
	    Element ModificationDate = document
		    .createElement("ModificationDate");
	    if (modificationdatebegin != null) {
		Element Begin = document.createElement("Begin");
		Begin.setTextContent(modificationdatebegin.toXMLFormat());
		ModificationDate.appendChild(Begin);
	    }
	    if (modificationdateend != null) {
		Element End = document.createElement("End");
		End.setTextContent(modificationdateend.toXMLFormat());
		ModificationDate.appendChild(End);
	    }
	    Parameter.appendChild(ModificationDate);
	}

	if (content != null) {
	    Element Content = document.createElement("Content");

	    switch (content.type) {
	    case DOCUMENT:
		Element Document = document.createElement("Document");
		Element SearchString = document.createElement("SearchString");
		SearchString.setTextContent(content.s1);
		Document.appendChild(SearchString);
		Content.appendChild(Document);
		break;
	    case MUSIC:
		Element Music = document.createElement("Music");
		Element Interpret = document.createElement("Interpret");
		Element Album = document.createElement("Album");

		Interpret.setTextContent(content.s1);
		Album.setTextContent(content.s2);

		Music.appendChild(Interpret);
		Music.appendChild(Album);
		Content.appendChild(Music);
		break;
	    case PICTURE:
		Element Picture = document.createElement("Picture");
		Content.appendChild(Picture);
		break;
	    case VIDEO:
		Element Video = document.createElement("Video");
		Content.appendChild(Video);
		break;
	    default:
		break;
	    }

	    Parameter.appendChild(Content);
	}

	Search.appendChild(Parameter);

	trimEmptyParameterNodes(document);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "Appending to document resulted in an invalid Document.");
	}
    }

    /**
     * This method creates a Request to search for files in the file system.
     * 
     * @return a Request to search for files in the file system
     * @throws NullPointerException
     *             if url is null
     * @throws IllegalArgumentException
     *             if url is an empty String
     * @throws RuntimeException
     *             if the built Request is invalid or could not be created
     */
    public static Document create() {

	Document document = null;
	try {
	    document = RequestBuilder.create();
	} catch (ParserConfigurationException e) {
	    throw new RuntimeException(
		    "The Document skeleton could not be created.");
	}
	Node Request = document.getElementsByTagName("Request").item(0);

	Element Search = document.createElement("Search");

	Request.appendChild(Search);

	if (!SearchBoosterXML.isValid(document)) {
	    throw new RuntimeException(
		    "An invalid Request has been constructed.");
	}

	return document;
    }
}
