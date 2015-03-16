package de.mround.searchbooster.api;

//import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.xml.RequestAbortBuilder;
import de.mround.searchbooster.api.xml.RequestIndexChangeBuilder;
import de.mround.searchbooster.api.xml.RequestIndexInfoBuilder;
import de.mround.searchbooster.api.xml.RequestSearchBuilder;
import de.mround.searchbooster.api.xml.ResponseIndexContentParser;
import de.mround.searchbooster.api.xml.ResponseParser;
import de.mround.searchbooster.api.xml.ResponseSearchParser;
import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;

public class SearchBoosterTest {

    private static SearchBooster mySearchBooster = null;
    private static BlockingQueue<Document> myResponseQueue = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	mySearchBooster = SearchBoosterImplementation.getInstance();
	myResponseQueue = mySearchBooster.getResponseQueue();
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
    public void testAbortRequest() {
	Document abortRequest = null;
	Document response = null;
	abortRequest = RequestAbortBuilder.create("Test");

	for (int i = 0; i < 5; i++) {
	    mySearchBooster.request(abortRequest);
	    try {
		response = myResponseQueue.take();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    System.out.println("-----");
	    System.out.println(ResponseParser.getStatusCodeID(response) + " - "
		    + ResponseParser.getStatusCodeMessage(response));
	    System.out.println("-----");
	}
    }

    @Test
    public void testIndexInfoRequest() {
	@SuppressWarnings("unused")
	Document abortRequest = null;
	Document indexInfoRequest = null;
	Document response = null;

	abortRequest = RequestAbortBuilder.create("Test");
	indexInfoRequest = RequestIndexInfoBuilder.create("Test");

	mySearchBooster.request(indexInfoRequest);
	// mySearchBooster.request(abortRequest);
	try {
	    response = myResponseQueue.take();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	System.out.println("-----");
	System.out.println(ResponseParser.getStatusCodeID(response) + " - "
		+ ResponseParser.getStatusCodeMessage(response));
	for (Node n : ResponseIndexContentParser.getPathList(response)) {
	    System.out.println(ResponseIndexContentParser.getPathURL(n) + " - "
		    + ResponseIndexContentParser.getPathRecursionDepth(n));
	}
	System.out.println("-----");
    }

    public void indexInfoRequest() {
	Document indexInfoRequest = null;
	Document response = null;

	indexInfoRequest = RequestIndexInfoBuilder.create("Test");

	mySearchBooster.request(indexInfoRequest);
	try {
	    response = myResponseQueue.take();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	System.out.println("-----");
	System.out.println(ResponseParser.getStatusCodeID(response) + " - "
		+ ResponseParser.getStatusCodeMessage(response));
	for (Node n : ResponseIndexContentParser.getPathList(response)) {
	    System.out.println(ResponseIndexContentParser.getPathURL(n) + " - "
		    + ResponseIndexContentParser.getPathRecursionDepth(n));
	}
	System.out.println("-----");
    }

    @Test
    public void testIndexChangeRequest() {
	Document indexChangeRequest1 = null;
	Document indexChangeRequest2 = null;
	Document response = null;

	indexChangeRequest1 = RequestIndexChangeBuilder.create(true);
	RequestIndexChangeBuilder.appendPath(indexChangeRequest1,
		"/home/siko1056/Downloads", -1);
	RequestIndexChangeBuilder.appendPath(indexChangeRequest1,
		"/home/siko1056", 0);

	indexChangeRequest2 = RequestIndexChangeBuilder.create(false);
	RequestIndexChangeBuilder.appendPath(indexChangeRequest2,
		"/home/siko1056/Downloads", -1);
	RequestIndexChangeBuilder.appendPath(indexChangeRequest2,
		"/home/siko1056/Dropbox", -1);
	RequestIndexChangeBuilder.appendPath(indexChangeRequest2,
		"/home/siko1056", 0);

	mySearchBooster.request(indexChangeRequest1);
	try {
	    response = myResponseQueue.take();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	System.out.println("-----");
	System.out.println(ResponseParser.getStatusCodeID(response) + " - "
		+ ResponseParser.getStatusCodeMessage(response));
	System.out.println("-----");

	this.indexInfoRequest();

	System.out.println("");
	System.out.println("");
	System.out.println("");

	mySearchBooster.request(indexChangeRequest2);
	try {
	    response = myResponseQueue.take();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	System.out.println("-----");
	System.out.println(ResponseParser.getStatusCodeID(response) + " - "
		+ ResponseParser.getStatusCodeMessage(response));
	System.out.println("-----");

	this.indexInfoRequest();
    }

    @Test
    public void testSearchRequest() {
	Document searchRequest = null;
	Document response = null;

	this.indexInfoRequest();

	searchRequest = RequestSearchBuilder.create();
	RequestSearchBuilder.appendPath(searchRequest,
		"/home/siko1056/Desktop", -1);
	RequestSearchBuilder.appendPath(searchRequest,
		"/home/siko1056/Downloads", -1);
	RequestSearchBuilder.appendPath(searchRequest,
		"/home/siko1056/Dropbox", 0);

	mySearchBooster.request(searchRequest);
	boolean done = false;
	while (!done) {
	    try {
		response = myResponseQueue.take();
		System.out.println("-----");
		System.out
			.println(ResponseParser.getStatusCodeID(response)
				+ " - "
				+ ResponseParser.getStatusCodeMessage(response));
		System.out.println("-----");

		for (Node n : ResponseSearchParser.getResultList(response)) {

		    XMLGregorianCalendar cal = null;
		    try {
			cal = ResponseSearchParser.getResultModificationDate(n);
		    } catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		    }

		    System.out.println(cal.getYear() + "-" + cal.getMonth()
			    + "-" + cal.getDay() + " - "
			    + ResponseSearchParser.getResultURL(n));
		}
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    } finally {
		done = (ResponseParser.getStatusCodeID(response).equals(
			StatusCodeID.ERROR) || ResponseParser.getStatusCodeID(
			response).equals(StatusCodeID.SUCCESSFUL));
	    }
	}
    }
}
