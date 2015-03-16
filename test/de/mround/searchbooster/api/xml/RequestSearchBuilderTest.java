package de.mround.searchbooster.api.xml;

import static org.junit.Assert.assertEquals;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class RequestSearchBuilderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNewSearchRequest() {
	System.out.println("");
	System.out.println("Test New Search Request");
	System.out.println("-----------------------");
	System.out.println("");

	Document document = RequestSearchBuilder.create();
	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}

	document = RequestSearchBuilder.create();
	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}

	document = RequestSearchBuilder.create();
	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

    @Test
    public void testAppendParameter() {
	System.out.println("");
	System.out.println("Test AppendParameter");
	System.out.println("--------------------");
	System.out.println("");

	XMLGregorianCalendar modificationdatebegin = null;
	XMLGregorianCalendar modificationdateend = null;
	try {
	    modificationdatebegin = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2000-01-20T12:00:00");
	    modificationdateend = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2010-01-20T12:00:00");
	} catch (DatatypeConfigurationException e) {
	    System.err.println(e.getMessage());
	}

	Document document = RequestSearchBuilder.create();
	RequestSearchBuilder.appendParameter(document, "/root/1/test",
		modificationdatebegin, modificationdateend,
		RequestSearchBuilder.Content.newPictureContent());
	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
	RequestSearchBuilder.appendParameter(document, "/root/2/test", null,
		null, null);
	RequestSearchBuilder.appendParameter(document, null,
		modificationdatebegin, null, null);
	RequestSearchBuilder.appendParameter(document, null, null,
		modificationdateend, null);
	RequestSearchBuilder.appendParameter(document, null, null, null,
		RequestSearchBuilder.Content.newPictureContent());
	// This will be trimmed
	RequestSearchBuilder.appendParameter(document, null, null, null, null);
	assertEquals(5, document.getElementsByTagName("Parameter").getLength());

	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

    @Test
    public void testAppendPath() {
	System.out.println("");
	System.out.println("Test appendPath");
	System.out.println("---------------");
	System.out.println("");

	Document document = RequestSearchBuilder.create();
	RequestSearchBuilder.appendPath(document, "/root/is/good", -1);
	RequestSearchBuilder.appendPath(document, "www.google.de", 10000043);
	RequestSearchBuilder.appendPath(document, "Don't do this", -348789);
	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

}
