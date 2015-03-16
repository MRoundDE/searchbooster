package de.mround.searchbooster.api.xml;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

public class RequestIndexChangeParserTest {
    private static String[] urls = { "/test/url/1", "/test/url/2", "/" };
    private static int[] recursionDepths = { 48, 0, -1 };

    private static Document simpleIndexChangeRequestTrue = null;
    private static Document simpleIndexChangeRequestFalse = null;
    private static Document advancedIndexChangeRequest = null;

    private static Document abortRequest = null;
    private static Document indexInfoRequest = null;
    private static Document searchRequest = null;
    private static Document metaSearchResultResponse = null;
    private static Document indexContentResponse = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	simpleIndexChangeRequestTrue = RequestIndexChangeBuilder.create(true);
	simpleIndexChangeRequestFalse = RequestIndexChangeBuilder.create(false);
	advancedIndexChangeRequest = RequestIndexChangeBuilder.create(true);
	RequestIndexChangeBuilder.appendPath(advancedIndexChangeRequest,
		urls[0], recursionDepths[0]);
	RequestIndexChangeBuilder.appendPath(advancedIndexChangeRequest,
		urls[1], recursionDepths[1]);
	RequestIndexChangeBuilder.appendPath(advancedIndexChangeRequest,
		urls[2], recursionDepths[2]);

	abortRequest = RequestAbortBuilder.create("Test");
	indexInfoRequest = RequestIndexInfoBuilder.create("Test");
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
    public void testIsIndexChangeRequest() {
	assertTrue(RequestIndexChangeParser
		.isIndexChangeRequest(simpleIndexChangeRequestTrue));
	assertTrue(RequestIndexChangeParser
		.isIndexChangeRequest(simpleIndexChangeRequestFalse));
	assertTrue(RequestIndexChangeParser
		.isIndexChangeRequest(advancedIndexChangeRequest));

	assertFalse(RequestIndexChangeParser.isIndexChangeRequest(abortRequest));
	assertFalse(RequestIndexChangeParser
		.isIndexChangeRequest(indexInfoRequest));
	assertFalse(RequestIndexChangeParser
		.isIndexChangeRequest(searchRequest));
	assertFalse(RequestIndexChangeParser
		.isIndexChangeRequest(metaSearchResultResponse));
	assertFalse(RequestIndexChangeParser
		.isIndexChangeRequest(indexContentResponse));
    }

    @Test
    public void testGetForceRebuild() {
	assertTrue(RequestIndexChangeParser
		.getForceRebuild(simpleIndexChangeRequestTrue));
	assertFalse(RequestIndexChangeParser
		.getForceRebuild(simpleIndexChangeRequestFalse));
    }

    @Test
    public void testGetPathList() {
	assertEquals(
		0,
		RequestIndexChangeParser.getPathList(
			simpleIndexChangeRequestTrue).size());
	assertEquals(3,
		RequestIndexChangeParser
			.getPathList(advancedIndexChangeRequest).size());
    }

    @Test
    public void testGetPathURL() {
	int i = 0;
	for (Node node : RequestIndexChangeParser
		.getPathList(advancedIndexChangeRequest)) {
	    assertEquals(urls[i], RequestIndexChangeParser.getPathURL(node));
	    i++;
	}
    }

    @Test
    public void testGetPathRecursionDepth() {
	int i = 0;
	for (Node node : RequestIndexChangeParser
		.getPathList(advancedIndexChangeRequest)) {
	    assertEquals(recursionDepths[i],
		    RequestIndexChangeParser.getPathRecursionDepth(node));
	    i++;
	}
    }

}
