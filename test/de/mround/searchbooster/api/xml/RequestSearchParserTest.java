package de.mround.searchbooster.api.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.xml.RequestSearchBuilder.Content;
import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

public class RequestSearchParserTest {
    private static XMLGregorianCalendar modificationdatebegin1 = null;
    private static XMLGregorianCalendar modificationdatebegin2 = null;
    private static XMLGregorianCalendar modificationdateend1 = null;
    private static XMLGregorianCalendar modificationdateend2 = null;
    private static Content contentDocument = null;
    private static Content contentMusic = null;
    private static Content contentPicture = null;
    private static Content contentVideo = null;

    private static Document simpleSearchRequest = null;
    private static Document advancedSearchRequest = null;

    private static Document abortRequest = null;
    private static Document indexChangeRequest = null;
    private static Document indexInfoRequest = null;
    private static Document metaSearchResultResponse = null;
    private static Document indexContentResponse = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	try {
	    modificationdatebegin1 = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2001-01-01T01:01:01");
	    modificationdatebegin2 = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2002-02-02T02:02:02");
	    modificationdateend1 = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2010-10-10T10:10:10");
	    modificationdateend2 = DatatypeFactory.newInstance()
		    .newXMLGregorianCalendar("2020-12-20T20:20:20");
	} catch (DatatypeConfigurationException e) {
	    System.err.println(e.getMessage());
	}
	contentDocument = RequestSearchBuilder.Content
		.newDocumentContent("Doc Test");
	contentMusic = RequestSearchBuilder.Content.newMusicContent(
		"Metallica", "Master of Puppets");
	contentPicture = RequestSearchBuilder.Content.newPictureContent();
	contentVideo = RequestSearchBuilder.Content.newVideoContent();

	simpleSearchRequest = RequestSearchBuilder.create();
	advancedSearchRequest = RequestSearchBuilder.create();
	RequestSearchBuilder.appendPath(advancedSearchRequest, "/test/url/2",
		-1);
	RequestSearchBuilder.appendParameter(advancedSearchRequest, "*",
		modificationdatebegin1, modificationdateend1, contentDocument);
	RequestSearchBuilder.appendPath(advancedSearchRequest, "/test/url/3",
		-1);
	// should not be visible
	RequestSearchBuilder.appendParameter(advancedSearchRequest, null, null,
		null, null);
	RequestSearchBuilder.appendParameter(advancedSearchRequest, "BlaBla",
		modificationdatebegin2, modificationdateend2, contentMusic);
	RequestSearchBuilder.appendParameter(advancedSearchRequest, "*", null,
		null, null);
	RequestSearchBuilder.appendParameter(advancedSearchRequest, null, null,
		null, contentVideo);
	RequestSearchBuilder.appendParameter(advancedSearchRequest, null, null,
		null, contentPicture);

	abortRequest = RequestAbortBuilder.create("Test");
	indexChangeRequest = RequestIndexChangeBuilder.create(true);
	indexInfoRequest = RequestIndexInfoBuilder.create("Test");
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
    public void testIsSearchRequest() {
	assertTrue(RequestSearchParser.isSearchRequest(simpleSearchRequest));
	assertTrue(RequestSearchParser.isSearchRequest(advancedSearchRequest));

	assertFalse(RequestSearchParser.isSearchRequest(abortRequest));
	assertFalse(RequestSearchParser.isSearchRequest(indexChangeRequest));
	assertFalse(RequestSearchParser.isSearchRequest(indexInfoRequest));
	assertFalse(RequestSearchParser
		.isSearchRequest(metaSearchResultResponse));
	assertFalse(RequestSearchParser.isSearchRequest(indexContentResponse));
    }

    @Test
    public void testGetPathList() {
	assertEquals(0, RequestSearchParser.getPathList(simpleSearchRequest)
		.size());
	assertEquals(2, RequestSearchParser.getPathList(advancedSearchRequest)
		.size());
    }

    @Test
    public void testGetPathURL() {
	List<Node> nodeList = RequestSearchParser
		.getPathList(advancedSearchRequest);
	assertEquals("/test/url/3",
		RequestSearchParser.getPathURL(nodeList.get(1)));
    }

    @Test
    public void testGetPathRecursionDepth() {
	List<Node> nodeList = RequestSearchParser
		.getPathList(advancedSearchRequest);
	assertEquals(-1,
		RequestSearchParser.getPathRecursionDepth(nodeList.get(1)));
    }

    @Test
    public void testGetParameterList() {
	assertEquals(0,
		RequestSearchParser.getParameterList(simpleSearchRequest)
			.size());
	assertEquals(5,
		RequestSearchParser.getParameterList(advancedSearchRequest)
			.size());
    }

    @Test
    public void testHasParameterFileName() {
	List<Node> nodeList = RequestSearchParser
		.getParameterList(advancedSearchRequest);
	assertTrue(RequestSearchParser.hasParameterFileName(nodeList.get(0)));
	assertFalse(RequestSearchParser.hasParameterFileName(nodeList.get(3)));
    }

    @Test
    public void testHasParameterModificationDateBegin() {
	List<Node> nodeList = RequestSearchParser
		.getParameterList(advancedSearchRequest);
	assertTrue(RequestSearchParser
		.hasParameterModificationDateBegin(nodeList.get(0)));
	assertFalse(RequestSearchParser
		.hasParameterModificationDateBegin(nodeList.get(2)));
    }

    @Test
    public void testHasParameterModificationDateEnd() {
	List<Node> nodeList = RequestSearchParser
		.getParameterList(advancedSearchRequest);
	assertTrue(RequestSearchParser.hasParameterModificationDateEnd(nodeList
		.get(0)));
	assertFalse(RequestSearchParser
		.hasParameterModificationDateEnd(nodeList.get(2)));
    }

    @Test
    public void testHasParameterContentDocument() {

	List<Node> nodeList = RequestSearchParser
		.getParameterList(advancedSearchRequest);

	RequestSearchParser.hasParameterContentDocument(nodeList.get(0));

	assertTrue(RequestSearchParser.hasParameterContentDocument(nodeList
		.get(0)));
	assertFalse(RequestSearchParser.hasParameterContentDocument(nodeList
		.get(1)));
    }

}
