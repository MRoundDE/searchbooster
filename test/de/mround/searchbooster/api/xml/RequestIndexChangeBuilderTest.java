package de.mround.searchbooster.api.xml;

import static org.junit.Assert.*;

import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class RequestIndexChangeBuilderTest {

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
	System.out.println("Generate simple Change Index Request");
	System.out.println("------------------------------------");
	System.out.println("");

	Document document = RequestIndexChangeBuilder.create(true);
	assertTrue(Boolean.parseBoolean(document
		.getElementsByTagName("ForceRebuild").item(0).getTextContent()));
	document = RequestIndexChangeBuilder.create(false);
	assertFalse(Boolean.parseBoolean(document
		.getElementsByTagName("ForceRebuild").item(0).getTextContent()));
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
	System.out.println("Generate wrong Change Index Request");
	System.out.println("-----------------------------------");
	System.out.println("");

	Document document = RequestIndexInfoBuilder.create("Test");
	RequestIndexChangeBuilder.appendPath(document, "Test", -1);
    }

    @Test
    public void testAppendPath() {
	System.out.println("");
	System.out.println("Generate more advanced Change Index Request");
	System.out.println("-------------------------------------------");
	System.out.println("");

	Document document = RequestIndexChangeBuilder.create(true);
	RequestIndexChangeBuilder.appendPath(document, "/url/1", -1);
	RequestIndexChangeBuilder.appendPath(document, "/my/url/2", 0);
	RequestIndexChangeBuilder.appendPath(document, "/yet/another/url",
		23487);

	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

}
