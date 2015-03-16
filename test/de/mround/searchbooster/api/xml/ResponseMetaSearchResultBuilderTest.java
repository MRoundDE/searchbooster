package de.mround.searchbooster.api.xml;

import static org.junit.Assert.*;

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

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

public class ResponseMetaSearchResultBuilderTest {

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
    public void testNewMetaSearchResultResponse() {
	System.out.println("");
	System.out.println("Test New Meta Search Result Response");
	System.out.println("------------------------------------");
	System.out.println("");

	Document document = ResponseSearchBuilder.create(
		StatusCodeID.SUCCESSFUL, "This is a Test message.");

	assertFalse(document.getElementsByTagName("IndexContent").getLength() > 0);

	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

    @Test(expected = RuntimeException.class)
    public void testAppendMetaSearchResultWrongType() {
	System.out.println("");
	System.out.println("Test Append Meta Search Result Wrong Type");
	System.out.println("-----------------------------------------");
	System.out.println("");

	XMLGregorianCalendar modificationdate1 = null;
	try {
	    modificationdate1 = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2000-01-20T12:00:00");
	} catch (DatatypeConfigurationException e) {
	    System.err.println(e.getMessage());
	}

	Document document = ResponseIndexContentBuilder.create(
		StatusCodeID.SUCCESSFUL, "This is a Test message.");
	// throws!!
	ResponseSearchBuilder.appendSearchResult(document,
		"/path/to/my/first/doc", modificationdate1, false);
    }

    @Test
    public void testAppendMetaSearchResult() {
	System.out.println("");
	System.out.println("Test Append Meta Search Result");
	System.out.println("------------------------------");
	System.out.println("");

	XMLGregorianCalendar modificationdate1 = null;
	XMLGregorianCalendar modificationdate2 = null;
	try {
	    modificationdate1 = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2000-01-20T12:00:00");
	    modificationdate2 = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2010-01-20T12:00:00");
	} catch (DatatypeConfigurationException e) {
	    System.err.println(e.getMessage());
	}

	Document document = ResponseSearchBuilder.create(
		StatusCodeID.SUCCESSFUL, "This is a Test message.");
	ResponseSearchBuilder.appendSearchResult(document,
		"/path/to/my/first/doc", modificationdate1, false);
	ResponseSearchBuilder.appendSearchResult(document,
		"/path/to/my/second/doc", modificationdate2, false);

	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

}
