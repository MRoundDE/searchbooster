package de.mround.searchbooster.api;

import java.util.concurrent.BlockingQueue;

import org.w3c.dom.Document;

/**
 * SearchBooster is a simple desktop document search tool. It offers it's users
 * an XML-Based-Request-Response-API, specified in the package
 * {@code de.mround.searchbooster.api.xml}, to:
 * <ul>
 * <li>maintain a persistent search index</li>
 * <li>search for files on the local computer</li>
 * </ul>
 * Responses to each request are handed over by a {@link BlockingQueue}.<br />
 * <br />
 * The only currently available implementation of this API is
 * {@link SearchBoosterImplementation}.<br />
 * <br />
 * 
 * @version API Version 2011-11-22_1
 * 
 * @author Michael Kunert
 * @author Kai Torben Ohlhus
 */
public interface SearchBooster {

    /**
     * This method forwards a request to SearchBooster. Only one request can be
     * processed at the same time. If there was passed more than one request at
     * the same time, all running requests will be dropped and only the most
     * recent request will be processed.<br />
     * <br />
     * To avoid this interruption wait for the response, that the currently
     * running request has finished.<br />
     * <br />
     * The format of this {@link org.w3c.dom.Document} is defined by a XSD-
     * {@link javax.xml.validation.Schema}. For more information check the
     * package {@code de.mround.searchbooster.api.xml}.
     * 
     * @param request
     *            - XML-Request for SearchBooster
     */
    public void request(Document request);

    /**
     * This method returns the XML-Responses to a XML-Request
     * 
     * @return XML-Responses to a XML-Request
     */
    public BlockingQueue<Document> getResponseQueue();
}
