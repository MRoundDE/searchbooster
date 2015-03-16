package de.mround.searchbooster.api.xml;

import static org.junit.Assert.assertEquals;

import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class RequestAbortBuilderTest {

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
    public void testNewAbortRequest() {
	System.out.println("");
	System.out.println("Generate Abort Request");
	System.out.println("----------------------");
	System.out.println("");

	Document document = RequestAbortBuilder.create("Test");
	assertEquals("Test", document.getFirstChild().getFirstChild()
		.getTextContent());
	try {
	    System.out.println(SearchBoosterXML
		    .getFormattedStringFromDocument(document));
	} catch (TransformerException e) {
	    System.err.println(e.getMessage());
	}
    }

}
