package de.mround.searchbooster.api.xml;

import static org.junit.Assert.*;

import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

public class ResponseIndexContentBuilderTest {

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
    public void testCreate() {
	System.out.println("");
	System.out.println("Test New Index Content Response");
	System.out.println("-------------------------------");
	System.out.println("");

	Document document = ResponseIndexContentBuilder.create(
		StatusCodeID.SUCCESSFUL, "This is a Test message.");

	assertTrue(document.getElementsByTagName("IndexContent").getLength() > 0);

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
	System.out.println("Test Append Path");
	System.out.println("----------------");
	System.out.println("");

	Document document = ResponseIndexContentBuilder.create(
		StatusCodeID.SUCCESSFUL, "This is a Test message.");
	ResponseIndexContentBuilder.appendPath(document, "/root/is/good", -1);
	ResponseIndexContentBuilder.appendPath(document, "www.gojgle.de",
		10000043);
	ResponseIndexContentBuilder.appendPath(document, "Don't do this",
		-348789);
	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

    @Test(expected = RuntimeException.class)
    public void testAppendPathWrongType() {
	System.out.println("");
	System.out.println("Test Append Path Wrong Type");
	System.out.println("---------------------------");
	System.out.println("");

	Document document = ResponseSearchBuilder.create(
		StatusCodeID.SUCCESSFUL, "This is a Test message.");
	// throws!!!
	ResponseIndexContentBuilder.appendPath(document, "/root/is/good", -1);
    }

}
