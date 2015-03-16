package de.mround.searchbooster.gui.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.mround.searchbooster.api.xml.RequestSearchBuilder.Content.FileType;

/**
 * GuiConfigXml is a class to handle the XML based configuration used by the
 * SearchBooster GUI.
 * 
 * @author Tobias Schulz
 */
public final class GuiConfigXml {

	/**
	 * GUI configuration XML schema file path
	 */
	private final String guiConfigXmlSchemaPath = "/de/mround/searchbooster/gui/controller/GuiConfigXmlSchema.xsd";

	/**
	 * Handle to the GUI configuration XML schema
	 */
	private Schema guiConfigXmlSchema = null;

	/**
	 * GUI configuration XML schema file handle
	 */
	private File guiConfigXmlFile = null;

	/**
	 * GUI configuration XML validator handle
	 */
	private Validator guiConfigXmlValidator = null;

	/**
	 * GUI configuration XML document handle
	 */
	private Document guiConfigXmlDocument = null;

	/**
	 * GUI configuration XML root node handle
	 */
	private Node guiConfigRootNode = null;

	/**
	 * Singleton instance
	 */
	private static GuiConfigXml instance = null;

	/**
	 * Get the singleton instance of the GUI configuration XML class
	 * 
	 * @return Class singleton instance
	 */
	public synchronized static GuiConfigXml getInstance() {
		if (GuiConfigXml.instance == null) {
			GuiConfigXml.instance = new GuiConfigXml();
		}
		return GuiConfigXml.instance;
	}

	/**
	 * Constructor
	 */
	private GuiConfigXml() {
		/* Load the XSD-Schema build-in in this package */
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		InputStream guiConfigXmlSchemaInputStream = GuiConfigXml.class
				.getResourceAsStream(guiConfigXmlSchemaPath);
		StreamSource guiConfigXmlSchemaStreamSource = new StreamSource(
				guiConfigXmlSchemaInputStream);
		try {
			guiConfigXmlSchema = schemaFactory
					.newSchema(guiConfigXmlSchemaStreamSource);
		} catch (SAXException e) {
			System.err.println(e.getMessage());
		}

		/* Create a validator for the XSD-schema */
		guiConfigXmlValidator = this.guiConfigXmlSchema.newValidator();

		/* Load or create the XML-file */
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setSchema(this.guiConfigXmlSchema);
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println(e.getMessage());
		}

		/* Determine folder containing the executing JAR-file */
		String path = null;
		String decodedPath = null;
		try {
			path = GuiConfigXml.class.getProtectionDomain().getCodeSource()
					.getLocation().getPath();
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
		} catch (SecurityException e) {
			System.err.println(e.getMessage());
		}

		guiConfigXmlFile = new File(decodedPath + "GuiConfig.xml");

		/* Parse existing file and check for validity */
		boolean isValidXmlFile = false;
		try {
			if (this.guiConfigXmlFile.canWrite()) {
				this.guiConfigXmlDocument = documentBuilder
						.parse(guiConfigXmlFile);
				this.guiConfigRootNode = this.guiConfigXmlDocument
						.getDocumentElement();
				isValidXmlFile = this.isValid();
			}
		} catch (Exception e) {
			/* Parsing failed */
			isValidXmlFile = false;
		}

		/* Build minimal GUI configuration XML file */
		if (!isValidXmlFile) {
			try {
				this.guiConfigXmlFile.delete();
				this.guiConfigXmlFile.createNewFile();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			this.guiConfigXmlDocument = documentBuilder.newDocument();
			this.guiConfigRootNode = this.guiConfigXmlDocument
					.createElement("GuiConfig");
			Element searchPathConfigRootNode = this.guiConfigXmlDocument
					.createElement("SearchPathConfig");
			this.guiConfigRootNode.appendChild(searchPathConfigRootNode);
			Element searchParameterConfigRootNode = this.guiConfigXmlDocument
					.createElement("SearchParameterConfig");
			this.guiConfigRootNode.appendChild(searchParameterConfigRootNode);
			Element settingsConfigRootNode = this.guiConfigXmlDocument
					.createElement("SettingsConfig");
			Element showInvisibleFilesNode = this.guiConfigXmlDocument
					.createElement("ShowInvisibleFiles");
			showInvisibleFilesNode.setTextContent(Boolean.toString(false));
			settingsConfigRootNode.appendChild(showInvisibleFilesNode);
			Element defaultIndexSearch = this.guiConfigXmlDocument
					.createElement("DefaultIndexSearch");
			defaultIndexSearch.setTextContent(Boolean.toString(false));
			settingsConfigRootNode.appendChild(defaultIndexSearch);
			this.guiConfigRootNode.appendChild(settingsConfigRootNode);
			this.guiConfigXmlDocument.appendChild(this.guiConfigRootNode);
			this.saveToXmlFile();
		}
	}

	/**
	 * This convenience method uses the {@link Validator} to quickly tell if the
	 * actual XML-document is valid.
	 * 
	 * @return Validity of the document
	 */
	public boolean isValid() {
		boolean valid = false;
		if (guiConfigXmlDocument != null) {
			try {
				guiConfigXmlValidator.validate(new DOMSource(
						guiConfigXmlDocument));
				valid = true;
			} catch (Exception e) {
			}
		}
		return valid;
	}

	/**
	 * Save GUI configuration XML document to file
	 */
	public void saveToXmlFile() {

		/* Check validity of XML document */
		if (!this.isValid()) {
			// this.testPrint();
			throw new RuntimeException("Invalid GUI-Config XML-file created.");
		}

		/* Save document to file */
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			System.err.println(e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			System.err.println(e.getMessage());
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "no");

		StreamResult streamResult = new StreamResult(this.guiConfigXmlFile);
		DOMSource domSource = new DOMSource(this.guiConfigXmlDocument);

		try {
			transformer.transform(domSource, streamResult);
		} catch (TransformerException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Get search path set names from XML document
	 * 
	 * @return Search path set names
	 */
	public List<String> getSearchPathSetNames() {

		/* Traverse XML document */
		List<String> searchPathSetNames = new Vector<String>();
		Node searchPathConfigRootNode = this.guiConfigRootNode.getFirstChild();
		NodeList searchPathConfigNodes = searchPathConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchPathConfigNodes.getLength(); i < length; i++) {
			if (searchPathConfigNodes.item(i).getNodeName()
					.equals("SearchPathSet")) {
				/* Add search path set name to list */
				searchPathSetNames.add(searchPathConfigNodes.item(i)
						.getFirstChild().getTextContent());
			}
		}
		return searchPathSetNames;
	}

	/**
	 * Add an empty search path set
	 * 
	 * @param name Search path set name
	 */
	public void addSearchPathSet(String name) {

		/* Check if name is a null pointer */
		if (name == null) {
			throw new NullPointerException("Path set name is a null pointer.");
		}

		char[] pathSetName = name.toCharArray();

		/* Check if name is not empty */
		if (pathSetName.length == 0) {
			throw new IllegalArgumentException("Path set name is empty.");
		}

		/* Check name for invalid characters */
		for (char c : pathSetName) {
			if ((!Character.isLetterOrDigit(c)) && (c != ' ')) {
				throw new IllegalArgumentException("Invalid path set name.");
			}
		}

		String pathSetNameString = new String(pathSetName);

		/* Check name for duplicate */
		Node searchPathConfigRootNode = this.guiConfigRootNode.getFirstChild();
		NodeList searchPathConfigNodes = searchPathConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchPathConfigNodes.getLength(); i < length; i++) {
			if (searchPathConfigNodes.item(i).getNodeName()
					.equals("SearchPathSet")) {
				if (searchPathConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(pathSetNameString)) {
					/* Duplicate found */
					throw new IllegalArgumentException(
							"Path set name already exist.");
				}
			}
		}

		/* Create search path set */
		Element searchPathSet = this.guiConfigXmlDocument
				.createElement("SearchPathSet");
		Element searchPathSetName = this.guiConfigXmlDocument
				.createElement("Name");
		searchPathSetName.setTextContent(pathSetNameString);
		searchPathSet.appendChild(searchPathSetName);
		searchPathConfigRootNode.appendChild(searchPathSet);

	}

	/**
	 * Remove a search path set from XML document
	 * 
	 * @param searchPathSetName Search path set name
	 */
	public void removeSearchPathSet(String searchPathSetName) {

		/* Traverse XML document */
		Node searchPathConfigRootNode = this.guiConfigRootNode.getFirstChild();
		NodeList searchPathConfigNodes = searchPathConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchPathConfigNodes.getLength(); i < length; i++) {
			if (searchPathConfigNodes.item(i).getNodeName()
					.equals("SearchPathSet")) {
				if (searchPathConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(searchPathSetName)) {
					/* Remove search path set */
					searchPathConfigRootNode.removeChild(searchPathConfigNodes
							.item(i));
					return;
				}
			}
		}
		
		return;
	}

	/**
	 * Add a search path to an existing search path set
	 * 
	 * @param searchPathSetName Search path set name
	 * @param searchPath Search path
	 */
	public void addSearchPathToSet(String searchPathSetName,
			SearchPath searchPath) {

		/* Traverse XML document */
		Node searchPathConfigRootNode = this.guiConfigRootNode.getFirstChild();
		NodeList searchPathConfigNodes = searchPathConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchPathConfigNodes.getLength(); i < length; i++) {
			if (searchPathConfigNodes.item(i).getNodeName()
					.equals("SearchPathSet")) {
				if (searchPathConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(searchPathSetName)) {
					/* Add search path to search path set */
					Element searchPathElement = this.guiConfigXmlDocument
							.createElement("SearchPath");
					Element searchPathPathElement = this.guiConfigXmlDocument
							.createElement("Path");
					searchPathPathElement.setTextContent(searchPath.path);
					searchPathElement.appendChild(searchPathPathElement);
					Element searchPathRecursionDepth = this.guiConfigXmlDocument
							.createElement("RecursionDepth");
					searchPathRecursionDepth.setTextContent(String
							.valueOf(searchPath.recursionDepth));
					searchPathElement.appendChild(searchPathRecursionDepth);
					searchPathConfigNodes.item(i)
					.appendChild(searchPathElement);
				}
			}
		}
	}

	/**
	 * Get search paths from existing set
	 * 
	 * @param searchPathSetName Search path set name
	 * 
	 * @return Search paths
	 */
	public List<SearchPath> getSearchPathsFromSet(String searchPathSetName) {

		/* Traverse XML document */
		List<SearchPath> searchPaths = new Vector<SearchPath>();
		Node searchPathConfigRootNode = this.guiConfigRootNode.getFirstChild();
		NodeList searchPathConfigNodes = searchPathConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchPathConfigNodes.getLength(); i < length; i++) {
			if (searchPathConfigNodes.item(i).getNodeName()
					.equals("SearchPathSet")) {
				if (searchPathConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(searchPathSetName)) {
					/* Add search path to list */
					NodeList searchPathNodes = searchPathConfigNodes.item(i)
							.getChildNodes();
					for (int k = 1, length2 = searchPathNodes.getLength(); k < length2; k++) {
						SearchPath searchPath = new SearchPath();
						searchPath.path = searchPathNodes.item(k)
								.getFirstChild().getTextContent();
						searchPath.recursionDepth = Integer
								.parseInt(searchPathNodes.item(k)
										.getLastChild().getTextContent());
						searchPaths.add(searchPath);
					}
				}
			}
		}
		return searchPaths;
	}

	/**
	 * Get search parameter DNF sets names from XML document
	 * 
	 * @return Search parameter DNF sets names
	 */
	public List<String> getSearchParameterDnfSetsNames() {

		/* Traverse XML document */
		List<String> searchParameterDnfSetsNames = new Vector<String>();
		NodeList configRootNodes = this.guiConfigRootNode.getChildNodes();
		Node searchParameterConfigRootNode = configRootNodes.item(1);
		NodeList searchParameterConfigNodes = searchParameterConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchParameterConfigNodes.getLength(); i < length; i++) {
			if (searchParameterConfigNodes.item(i).getNodeName()
					.equals("SearchParameterDnfSets")) {
				/* Add search parameter DNF sets name to list */
				searchParameterDnfSetsNames.add(searchParameterConfigNodes
						.item(i).getFirstChild().getTextContent());
			}
		}
		return searchParameterDnfSetsNames;
	}

	/**
	 * Add empty search parameter DNF sets
	 * 
	 * @param name Search parameter DNF sets name
	 */
	public void addSearchParameterDnfSets(String name) {

		/* Check if name is a null pointer */
		if (name == null) {
			throw new NullPointerException(
					"Parameter DNF sets name is a null pointer.");
		}

		char[] parameterDnfSetsName = name.toCharArray();

		/* Check if name is not empty */
		if (parameterDnfSetsName.length == 0) {
			throw new IllegalArgumentException(
					"Parameter DNF sets name is empty.");
		}

		/* Check name for invalid characters */
		for (char c : parameterDnfSetsName) {
			if ((!Character.isLetterOrDigit(c)) && (c != ' ')) {
				throw new IllegalArgumentException(
						"Invalid parameter DNF sets name.");
			}
		}

		String parameterDnfSetsNameString = new String(parameterDnfSetsName);

		/* Check name for duplicate */
		NodeList configRootNodes = this.guiConfigRootNode.getChildNodes();
		Node searchParameterConfigRootNode = configRootNodes.item(1);
		NodeList searchParameterConfigNodes = searchParameterConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchParameterConfigNodes.getLength(); i < length; i++) {
			if (searchParameterConfigNodes.item(i).getNodeName()
					.equals("SearchParameterDnfSets")) {
				if (searchParameterConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(parameterDnfSetsNameString)) {
					/* Duplicate found */
					throw new IllegalArgumentException(
							"Parameter DNF sets name already exist.");
				}
			}
		}

		/* Create search parameter DNF sets */
		Element searchParameterDnfSets = this.guiConfigXmlDocument
				.createElement("SearchParameterDnfSets");
		Element searchParameterDnfSetsName = this.guiConfigXmlDocument
				.createElement("Name");
		searchParameterDnfSetsName.setTextContent(parameterDnfSetsNameString);
		searchParameterDnfSets.appendChild(searchParameterDnfSetsName);
		searchParameterConfigRootNode.appendChild(searchParameterDnfSets);
	}

	/**
	 * Remove search parameter DNF sets from XML document
	 * 
	 * @param searchParameterDnfSetsName Search parameter DNF sets name
	 */
	public void removeSearchParameterDnfSets(String searchParameterDnfSetsName) {

		/* Traverse XML document */
		NodeList configRootNodes = this.guiConfigRootNode.getChildNodes();
		Node searchParameterConfigRootNode = configRootNodes.item(1);
		NodeList searchParameterConfigNodes = searchParameterConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchParameterConfigNodes.getLength(); i < length; i++) {
			if (searchParameterConfigNodes.item(i).getNodeName()
					.equals("SearchParameterDnfSets")) {
				if (searchParameterConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(searchParameterDnfSetsName)) {
					/* Remove search parameter DNF sets */
					searchParameterConfigRootNode
					.removeChild(searchParameterConfigNodes.item(i));
					return;
				}
			}
		}
		
		return;
	}

	/**
	 * Add a search parameter DNF set to existing search parameter DNF sets
	 * 
	 * @param searchParameterDnfSetsName Search parameter DNF sets name
	 * @param searchParameterSet Search parameter DNF set
	 */
	public void addSearchParameterSetToDnfSets(
			String searchParameterDnfSetsName,
			SearchParameter searchParameterSet) {

		/* Traverse XML document */
		NodeList configRootNodes = this.guiConfigRootNode.getChildNodes();
		Node searchParameterConfigRootNode = configRootNodes.item(1);
		NodeList searchParameterConfigNodes = searchParameterConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchParameterConfigNodes.getLength(); i < length; i++) {
			if (searchParameterConfigNodes.item(i).getNodeName()
					.equals("SearchParameterDnfSets")) {
				if (searchParameterConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(searchParameterDnfSetsName)) {
					/* Add search parameter DNF set */
					Element searchParameterSetElement = this.guiConfigXmlDocument
							.createElement("SearchParameterSet");
					/* Add file name parameter */
					if (searchParameterSet.fileNameString != null) {
						Element searchParameterFileNameElement = this.guiConfigXmlDocument
								.createElement("FileName");
						searchParameterFileNameElement
						.setTextContent(searchParameterSet.fileNameString);
						searchParameterSetElement
						.appendChild(searchParameterFileNameElement);
					}
					/* Add begin and end modification date parameter */
					if ((searchParameterSet.beginModificationDate != null)
							|| (searchParameterSet.endModificationDate != null)) {
						/* Modification date */
						GregorianCalendar gregorianCalendar = new GregorianCalendar();
						XMLGregorianCalendar xmlBeginModificationDate = null;
						XMLGregorianCalendar xmlEndModificationDate = null;
						Element searchParameterModificationDateElement = this.guiConfigXmlDocument
								.createElement("ModificationDate");
						if (searchParameterSet.beginModificationDate != null) {
							Element searchParameterModificationDateBeginElement = this.guiConfigXmlDocument
									.createElement("Begin");
							gregorianCalendar
							.setTime(searchParameterSet.beginModificationDate);
							try {
								xmlBeginModificationDate = DatatypeFactory
										.newInstance().newXMLGregorianCalendar(
												gregorianCalendar);
							} catch (DatatypeConfigurationException e) {
								System.err.println(e.getMessage());
							}
							searchParameterModificationDateBeginElement
							.setTextContent(xmlBeginModificationDate
									.toXMLFormat());
							searchParameterModificationDateElement
							.appendChild(searchParameterModificationDateBeginElement);
						}
						if (searchParameterSet.endModificationDate != null) {
							Element searchParameterModificationDateEndElement = this.guiConfigXmlDocument
									.createElement("End");
							gregorianCalendar
							.setTime(searchParameterSet.endModificationDate);
							try {
								xmlEndModificationDate = DatatypeFactory
										.newInstance().newXMLGregorianCalendar(
												gregorianCalendar);
							} catch (DatatypeConfigurationException e) {
								System.err.println(e.getMessage());
							}
							searchParameterModificationDateEndElement
							.setTextContent(xmlEndModificationDate
									.toXMLFormat());
							searchParameterModificationDateElement
							.appendChild(searchParameterModificationDateEndElement);
						}
						searchParameterSetElement
						.appendChild(searchParameterModificationDateElement);
					}
					/* Add file type parameter */
					if (searchParameterSet.fileType != FileType.ALL) {
						Element searchParameterContentElement = this.guiConfigXmlDocument
								.createElement("Content");
						switch (searchParameterSet.fileType) {
						case DOCUMENT:
							Element searchParameterContentDocumentElement = this.guiConfigXmlDocument
							.createElement("Document");
							if (searchParameterSet.documentContentString != null) {
								/* Add document content parameter */
								Element searchParameterContentDocumentSearchStringElement = this.guiConfigXmlDocument
										.createElement("SearchString");
								searchParameterContentDocumentSearchStringElement
								.setTextContent(searchParameterSet.documentContentString);
								searchParameterContentDocumentElement
								.appendChild(searchParameterContentDocumentSearchStringElement);
							}
							searchParameterContentElement
							.appendChild(searchParameterContentDocumentElement);
							break;
						case MUSIC:
							Element searchParameterContentMusicElement = this.guiConfigXmlDocument
							.createElement("Music");
							if (searchParameterSet.musicInterpretString != null) {
								/* Add music interpret parameter */
								Element searchParameterContentMusicInterpretElement = this.guiConfigXmlDocument
										.createElement("Interpret");
								searchParameterContentMusicInterpretElement
								.setTextContent(searchParameterSet.musicInterpretString);
								searchParameterContentMusicElement
								.appendChild(searchParameterContentMusicInterpretElement);
							}
							if (searchParameterSet.musicAlbumString != null) {
								/* Add music album parameter */
								Element searchParameterContentMusicAlbumElement = this.guiConfigXmlDocument
										.createElement("Album");
								searchParameterContentMusicAlbumElement
								.setTextContent(searchParameterSet.musicAlbumString);
								searchParameterContentMusicElement
								.appendChild(searchParameterContentMusicAlbumElement);
							}
							searchParameterContentElement
							.appendChild(searchParameterContentMusicElement);
							break;
						case PICTURE:
							Element searchParameterContentPictureElement = this.guiConfigXmlDocument
							.createElement("Picture");
							searchParameterContentElement
							.appendChild(searchParameterContentPictureElement);
							break;
						case VIDEO:
						default:
							Element searchParameterContentVideoElement = this.guiConfigXmlDocument
							.createElement("Video");
							searchParameterContentElement
							.appendChild(searchParameterContentVideoElement);
							break;
						}
						searchParameterSetElement
						.appendChild(searchParameterContentElement);
					}
					searchParameterConfigNodes.item(i).appendChild(
							searchParameterSetElement);
				}
			}
		}
	}

	/**
	 * Get search parameter DNF set from existing sets
	 * 
	 * @param searchParameterDnfSetsName Search parameter DNF sets name
	 * 
	 * @return Search parameter DNF set
	 */
	public List<SearchParameter> getSearchParameterDnfSets(
			String searchParameterDnfSetsName) {

		/* Traverse XML document */
		List<SearchParameter> searchParameterSets = new Vector<SearchParameter>();
		NodeList configRootNodes = this.guiConfigRootNode.getChildNodes();
		Node searchParameterConfigRootNode = configRootNodes.item(1);
		NodeList searchParameterConfigNodes = searchParameterConfigRootNode
				.getChildNodes();
		for (int i = 0, length = searchParameterConfigNodes.getLength(); i < length; i++) {
			if (searchParameterConfigNodes.item(i).getNodeName()
					.equals("SearchParameterDnfSets")) {
				if (searchParameterConfigNodes.item(i).getFirstChild()
						.getTextContent().equals(searchParameterDnfSetsName)) {
					/* Add search parameter DNF set to list */
					NodeList searchParameterSetNodes = searchParameterConfigNodes
							.item(i).getChildNodes();
					for (int k = 1, length2 = searchParameterSetNodes
							.getLength(); k < length2; k++) {
						SearchParameter searchParameterSet = new SearchParameter();
						int parameterIndex = 0;
						NodeList searchParameterNodes = searchParameterSetNodes
								.item(k).getChildNodes();
						int childCount = searchParameterNodes.getLength();
						for (int childIndex = 0; childIndex < childCount; parameterIndex++) {
							if (parameterIndex == 0) {
								if (searchParameterNodes.item(childIndex)
										.getNodeName().equals("FileName")) {
									/* File name parameter */
									searchParameterSet.fileNameString = searchParameterNodes
											.item(childIndex).getTextContent();
									childIndex++;
								}
							} else if (parameterIndex == 1) {
								if (searchParameterNodes.item(childIndex)
										.getNodeName()
										.equals("ModificationDate")) {
									/* Modification date parameter */
									NodeList modificationDateNodes = searchParameterNodes
											.item(childIndex).getChildNodes();
									int modificationDateCount = modificationDateNodes
											.getLength();
									for (int m = 0; m < modificationDateCount; m++) {
										if (modificationDateNodes.item(m)
												.getNodeName().equals("Begin")) {
											try {
												XMLGregorianCalendar xmlBeginModificationDate = DatatypeFactory
														.newInstance()
														.newXMLGregorianCalendar(
																modificationDateNodes
																.item(m)
																.getTextContent());
												searchParameterSet.beginModificationDate = xmlBeginModificationDate
														.toGregorianCalendar()
														.getTime();
											} catch (Exception e) {
											}
										} else if (modificationDateNodes
												.item(m).getNodeName()
												.equals("End")) {
											try {
												XMLGregorianCalendar xmlEndModificationDate = DatatypeFactory
														.newInstance()
														.newXMLGregorianCalendar(
																modificationDateNodes
																.item(m)
																.getTextContent());
												searchParameterSet.endModificationDate = xmlEndModificationDate
														.toGregorianCalendar()
														.getTime();
											} catch (Exception e) {
											}
										}
									}
									childIndex++;
								}
							} else {
								if (searchParameterNodes.item(childIndex)
										.getNodeName().equals("Content")) {
									/* File type parameter */
									if (searchParameterNodes.item(childIndex)
											.hasChildNodes()) {
										Node contentElement = searchParameterNodes
												.item(childIndex)
												.getFirstChild();
										String contentTypeString = contentElement
												.getNodeName();
										if (contentTypeString
												.equals("Document")) {
											/* File type document */
											searchParameterSet.fileType = FileType.DOCUMENT;
											if (contentElement.hasChildNodes()) {
												if (contentElement
														.getFirstChild()
														.getNodeName()
														.equals("SearchString")) {
													/* Document content parameter */
													searchParameterSet.documentContentString = contentElement
															.getFirstChild()
															.getTextContent();
												}
											}
										} else if (contentTypeString
												.equals("Music")) {
											/* File type music */
											searchParameterSet.fileType = FileType.MUSIC;
											NodeList musicElements = contentElement
													.getChildNodes();
											int musicElementCount = musicElements
													.getLength();
											for (int n = 0; n < musicElementCount; n++) {
												if (musicElements.item(n)
														.getNodeName()
														.equals("Album")) {
													/* Music album parameter */
													searchParameterSet.musicAlbumString = musicElements
															.item(n)
															.getTextContent();
												} else if (musicElements
														.item(n).getNodeName()
														.equals("Interpret")) {
													/* Music interpret parameter */
													searchParameterSet.musicInterpretString = musicElements
															.item(n)
															.getTextContent();
												}
											}
										} else if (contentTypeString
												.equals("Picture")) {
											/* File type picture */
											searchParameterSet.fileType = FileType.PICTURE;
										} else if (contentTypeString
												.equals("Video")) {
											/* File type video */
											searchParameterSet.fileType = FileType.VIDEO;
										}
									}
									childIndex++;
								} else {
									childIndex++;
								}
							}
						}
						searchParameterSets.add(searchParameterSet);
					}
				}
			}
		}
		return searchParameterSets;
	}

	/**
	 * Get configuration settings from XML document
	 * 
	 * @param settings GUI configuration settings
	 */
	public void getConfigurationSettings(ConfigurationSetting settings) {
		if (settings == null) {
			settings = new ConfigurationSetting();
		}

		/* Traverse XML document */
		NodeList configRootNodes = this.guiConfigRootNode.getChildNodes();
		Node settingsConfigRootNode = configRootNodes.item(2);
		NodeList configSettingNodes = settingsConfigRootNode.getChildNodes();
		for (int i = 0; i < configSettingNodes.getLength(); i++) {
			/* Get configuration settings */
			if (configSettingNodes.item(i).getNodeName()
					.equals("ShowInvisibleFiles")) {
				settings.showInvisibleFiles = Boolean
						.parseBoolean(configSettingNodes.item(i)
								.getTextContent());
			} else if (configSettingNodes.item(i).getNodeName()
					.equals("DefaultIndexSearch")) {
				settings.defaultIndexSearch = Boolean
						.parseBoolean(configSettingNodes.item(i)
								.getTextContent());
			}
		}
	}

	/**
	 * Set GUI configuration settings
	 * 
	 * @param settings GUI configuration settings
	 */
	public void setConfigurationSettings(ConfigurationSetting settings) {

		/* Traverse XML document */
		NodeList configRootNodes = this.guiConfigRootNode.getChildNodes();
		Node settingsConfigRootNode = configRootNodes.item(2);
		NodeList configSettingNodes = settingsConfigRootNode.getChildNodes();
		for (int i = 0; i < configSettingNodes.getLength(); i++) {
			/* Set configuration settings */
			if (configSettingNodes.item(i).getNodeName()
					.equals("ShowInvisibleFiles")) {
				configSettingNodes.item(i).setTextContent(
						Boolean.toString(settings.showInvisibleFiles));
			} else if (configSettingNodes.item(i).getNodeName()
					.equals("DefaultIndexSearch")) {
				configSettingNodes.item(i).setTextContent(
						Boolean.toString(settings.defaultIndexSearch));
			}
		}
	}

	/**
	 * Print the XML document
	 */
	@SuppressWarnings("unused")
	private void testPrint() {
		this.printNode(guiConfigRootNode, 0);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
		return;
	}

	/**
	 * Print a XML document node
	 * 
	 * @param node XML document node
	 * @param depth XML document node tree depth
	 */
	private void printNode(Node node, int depth) {
		if (node == null) {
			return;
		}
		for (int i = 0; i < depth; i++) {
			System.out.print("\t");
		}
		if (node.getNodeName().equals("#text")) {
			System.out.println(node.getNodeName() + ": "
					+ node.getTextContent());
		} else {
			System.out.println(node.getNodeName() + ":");
		}
		for (int k = 0, length = node.getChildNodes().getLength(); k < length; k++) {
			this.printNode(node.getChildNodes().item(k), depth + 1);
		}
	}

}
