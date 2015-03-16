package de.mround.searchbooster.api.xml;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

public class RequestIndexInfoParserTest {
    private static Document indexInfoRequest = null;

    private static Document abortRequest = null;
    private static Document indexChangeRequest = null;
    private static Document searchRequest = null;
    private static Document metaSearchResultResponse = null;
    private static Document indexContentResponse = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	indexInfoRequest = RequestIndexInfoBuilder.create("Test");

	abortRequest = RequestAbortBuilder.create("Test");
	indexChangeRequest = RequestIndexChangeBuilder.create(true);
	searchRequest = RequestSearchBuilder.create();
	metaSearchResultResponse = ResponseSearchBuilder.create(
		StatusCodeID.SUCCESSFUL, "Test");
	indexContentResponse = ResponseIndexContentBuilder.create(
		StatusCodeID.SUCCESSFUL, "Test");
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
    public void testIsIndexInfoRequest() {
	assertTrue(RequestIndexInfoParser.isIndexInfoRequest(indexInfoRequest));

	assertFalse(RequestIndexInfoParser.isIndexInfoRequest(abortRequest));
	assertFalse(RequestIndexInfoParser
		.isIndexInfoRequest(indexChangeRequest));
	assertFalse(RequestIndexInfoParser.isIndexInfoRequest(searchRequest));
	assertFalse(RequestIndexInfoParser
		.isIndexInfoRequest(metaSearchResultResponse));
	assertFalse(RequestIndexInfoParser
		.isIndexInfoRequest(indexContentResponse));
    }

}
