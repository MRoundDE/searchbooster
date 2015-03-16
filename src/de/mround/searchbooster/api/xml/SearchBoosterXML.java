package de.mround.searchbooster.api.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
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
import org.xml.sax.SAXException;

/**
 * SearchBoosterXML is the basic noninstantiable class to handle the XML-based
 * communication (see {@link SearchBoosterXML#getSchema()}) used by the
 * {@link de.mround.searchbooster.api.SearchBooster} -API.
 * 
 * @author Kai Torben Ohlhus
 * 
 */
public final class SearchBoosterXML {
    private final static String searchBoosterXSDSchemaPath = "/de/mround/searchbooster/api/xml/SearchBoosterSchema.xsd";
    private static Schema searchBoosterSchema = null;

    public static enum StatusCodeID {
	ERROR, SUCCESSFUL, NOT_COMPLETED;
    }

    /**
     * Private constructor to avoid instantiation.
     */
    private SearchBoosterXML() {
    }

    /**
     * This method returns the statically build in {@link Schema} representation
     * of the XSD-File "SearchBoosterSchema.xsd".
     * 
     * @return a {@link Schema} representation of the XSD-Schema
     *         "SearchBoosterSchema.xsd" or {@code null} in case of a schema
     *         loading error
     */
    public static Schema getSchema() {
	if (searchBoosterSchema == null) {
	    // Load the XSD-Schema build-in in this package
	    SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    InputStream searchBoosterXSDSchemaInputStream = SearchBoosterXML.class
		    .getResourceAsStream(searchBoosterXSDSchemaPath);
	    StreamSource searchBoosterXSDSchemaStreamSource = new StreamSource(
		    searchBoosterXSDSchemaInputStream);
	    try {
		searchBoosterSchema = schemaFactory
			.newSchema(searchBoosterXSDSchemaStreamSource);
	    } catch (SAXException e) {
		System.err.println(e.getMessage());
	    }
	}
	return searchBoosterSchema;
    }

    /**
     * This convenience method uses the {@link Validator} retrieved by
     * {@link SearchBoosterXML#getSchema()} to quickly tell if it is valid.
     * 
     * @param document
     *            the {@link Document} to validate
     * @return true, if document is valid
     * @throws NullPointerException
     *             If document is null.
     */
    public static boolean isValid(Document document) {
	// Check parameter
	if (document == null) {
	    throw new NullPointerException(
		    "The passed parameter document is null.");
	}

	boolean valid = true;
	Validator validator = getSchema().newValidator();
	try {
	    validator.validate(new DOMSource(document.getFirstChild()));
	} catch (SAXException e) {
	    valid = false;
	} catch (IOException e) {
	    valid = false;
	}
	return valid;
    }

    /**
     * This method returns a {@link Document} representation of a XML-File. This
     * {@link Document} representation is already validated against the build-in
     * XSD-Schema. See {@link SearchBoosterXML#getSchema()}.
     * 
     * @param xmlFile
     *            - file to convert to a {@link Document}
     * @return a {@link Document} representation of a XML-File
     * @throws SAXException
     *             If any parse errors occur.
     * @throws IOException
     *             If any IO errors occur.
     * @throws NullPointerException
     *             If xmlFile is null or internal errors occur.
     */
    public static Document getDocument(File xmlFile) throws SAXException,
	    IOException {
	// Check parameter
	if (xmlFile == null) {
	    throw new NullPointerException(
		    "The passed parameter xmlFile is null.");
	}

	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	dbFactory.setSchema(getSchema());
	dbFactory.setNamespaceAware(true);

	DocumentBuilder builder = null;
	try {
	    builder = dbFactory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    System.err.println(e.getMessage());
	}
	if (builder == null) {
	    throw new NullPointerException(
		    "Unable to initalize DocumentBuilder.");
	}

	Document document = null;
	document = builder.parse(xmlFile);

	return document;
    }

    /**
     * Convenience method. See {@link SearchBoosterXML#getDocument(File)}.
     * 
     * @param xmlFile
     *            - file to convert to a {@link Document}
     * @return a {@link Document} representation of a XML-File
     * @throws SAXException
     *             If any parse errors occur.
     * @throws IOException
     *             If any IO errors occur.
     * @throws NullPointerException
     *             If xmlFile is null or internal errors occur.
     */
    public static Document getDocument(String xmlFile) throws SAXException,
	    IOException {
	// Check parameter
	if (xmlFile == null) {
	    throw new NullPointerException(
		    "The passed parameter xmlFile is null.");
	}
	return getDocument(new File(xmlFile));
    }

    /**
     * This method returns a formatted {@link String} representation of a
     * {@link Document}.
     * 
     * @param document
     *            - the {@link Document} to transform into a formatted
     *            {@link String}
     * @return a formatted {@link String} representation of document
     * @throws TransformerException
     *             If an unrecoverable error occurs during the course of the
     *             transformation.
     * @throws NullPointerException
     *             If document is null or internal errors occur.
     */
    public static String getFormattedStringFromDocument(Document document)
	    throws TransformerException {
	// Check parameter
	if (document == null) {
	    throw new NullPointerException(
		    "The passed parameter document is null.");
	}

	Transformer transformer = null;
	try {
	    transformer = TransformerFactory.newInstance().newTransformer();
	} catch (TransformerConfigurationException e) {
	    System.err.println(e.getMessage());
	} catch (TransformerFactoryConfigurationError e) {
	    System.err.println(e.getMessage());
	}
	if (transformer == null) {
	    throw new NullPointerException("Unable to initalize Transformer.");
	}
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

	StreamResult result = new StreamResult(new StringWriter());
	DOMSource domSource = new DOMSource(document.getFirstChild());

	transformer.transform(domSource, result);

	return result.getWriter().toString();
    }
}
