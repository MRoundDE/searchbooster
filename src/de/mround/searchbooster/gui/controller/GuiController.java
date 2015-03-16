package de.mround.searchbooster.gui.controller;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

//import javax.swing.SwingUtilities;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mround.searchbooster.api.SearchBooster;
import de.mround.searchbooster.api.SearchBoosterImplementation;
import de.mround.searchbooster.api.xml.RequestAbortBuilder;
import de.mround.searchbooster.api.xml.RequestIndexChangeBuilder;
import de.mround.searchbooster.api.xml.RequestIndexInfoBuilder;
import de.mround.searchbooster.api.xml.RequestSearchBuilder;
import de.mround.searchbooster.api.xml.RequestSearchBuilder.Content;
import de.mround.searchbooster.api.xml.ResponseIndexContentParser;
import de.mround.searchbooster.api.xml.ResponseParser;
import de.mround.searchbooster.api.xml.ResponseSearchParser;
import de.mround.searchbooster.api.xml.SearchBoosterXML;
import de.mround.searchbooster.api.xml.SearchBoosterXML.StatusCodeID;
import de.mround.searchbooster.gui.model.ResultTableModel;
import de.mround.searchbooster.gui.view.GuiView;

/**
 * GUI controller
 * 
 * The controller is realizing the MVC pattern of the Swing GUI for more complex
 * events. Simple events are handled directly in the GUI view event handlers.
 * 
 * @author Tobias Schulz
 */
public class GuiController {

	/*
	 * GUI-API communication uses an API-function to accept requests. Requests
	 * are aborted if a new request arrives prior to the complete execution of
	 * the running one.
	 */

	/**
	 * Request types
	 */
	private enum RequestType {
		NONE, INDEX_INFO, INDEX_CHANGE, CONFIG, SEARCH, ABORT
	};

	/**
	 * State of running request
	 */
	private RequestType runningRequest = RequestType.NONE;

	/**
	 * Handle to API-GUI communication event queue
	 */
	private BlockingQueue<Document> responseQueue;

	/**
	 * API-GUI communication event
	 */
	private static Document event;

	/**
	 * Handle to SearchBooster API
	 */
	private SearchBooster searchBooster;

	/**
	 * Handle to GUI view
	 */
	private GuiView guiView;

	/**
	 * Table model for search results
	 */
	private final ResultTableModel resultTableModel = new ResultTableModel();

	/**
	 * Constructor
	 * 
	 * Setting up the GUI view and SearchBooster API.
	 */
	public GuiController() {
		this.searchBooster = SearchBoosterImplementation.getInstance();
		this.responseQueue = this.searchBooster.getResponseQueue();
		this.guiView = new GuiView(this);
	}

	/**
	 * SearchBooster start
	 * 
	 * Enabling the GUI view and controlling the communication between GUI and
	 * SerachBooster API. This method will return only if an unfixable error
	 * occurs or the GUI is terminated.
	 */
	public void start() {

		/*
		 * Enable GUI (all changes to GUI related elements must be assigned to
		 * the GUI thread by passing a runnable object to the method
		 * invokeLater)
		 */
		this.guiView.setVisible(true);

		/* API-GUI communication */
		while (true) {
			try {
				/* Wait for event from API */
				event = this.responseQueue.take();
				if (event == null) {
					continue;
				}

//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {

						/* Handle different API events */
						if (!SearchBoosterXML.isValid(event)
								|| !ResponseParser.isResponse(event)) {
							guiView.setStatusLabelText(
									"Error - Invalid response received", false);
							continue;
						}

						RequestType runningRequestType;
						synchronized (runningRequest) {
							runningRequestType = runningRequest;
						}

						/* Response status */
						StatusCodeID responseId = ResponseParser.getStatusCodeID(event);
						if (responseId == StatusCodeID.SUCCESSFUL) {
							guiView.setStatusLabelText(
									"Done - "
											+ ResponseParser
											.getStatusCodeMessage(event), false);
							requestCompleted();
						} else if (responseId == StatusCodeID.NOT_COMPLETED) {
							guiView.setStatusLabelText("Searching... - "
									+ ResponseParser.getStatusCodeMessage(event), true);
						} else {
							/* Unknown error ID */
							guiView.setStatusLabelText(
									"Error - "
											+ ResponseParser
											.getStatusCodeMessage(event), false);
							requestCompleted();
							continue;
						}

						/* Handle different responses */
						if (ResponseSearchParser.isResponseSearch(event)) {
							if (runningRequestType == RequestType.SEARCH) {
								for (Node searchResult : ResponseSearchParser
										.getResultList(event)) {
									File file = new File(
											ResponseSearchParser
											.getResultURL(searchResult));
									XMLGregorianCalendar xmlModificationDate = null;
									try {
										xmlModificationDate = ResponseSearchParser
												.getResultModificationDate(searchResult);
									} catch (DOMException e) {
										System.err.println(e.getMessage());
									} catch (DatatypeConfigurationException e) {
										System.err.println(e.getMessage());
									}
									Date modificationDate = xmlModificationDate
											.toGregorianCalendar().getTime();
									resultTableModel
									.addRow(file,
											modificationDate,
											ResponseSearchParser
											.getResultContentMatch(searchResult));
								}
							}
						} else if (ResponseIndexContentParser
								.isResponseIndexContent(event)) {
							if ((runningRequestType == RequestType.INDEX_CHANGE)
									|| (runningRequestType == RequestType.INDEX_INFO)) {
								List<File> recursiveSelectedFiles = new Vector<File>();
								List<File> flatSelectedFiles = new Vector<File>();
								for (Node indexContent : ResponseIndexContentParser
										.getPathList(event)) {
									if (ResponseIndexContentParser
											.getPathRecursionDepth(indexContent) == 0) {
										flatSelectedFiles.add(new File(
												ResponseIndexContentParser
												.getPathURL(indexContent)));
									} else {
										recursiveSelectedFiles.add(new File(
												ResponseIndexContentParser
												.getPathURL(indexContent)));
									}
								}
								if (guiView.getIndexTree() != null) {
									guiView.getIndexTree().setSelectedFiles(
											recursiveSelectedFiles, flatSelectedFiles);
								}
							}
						} else {
							/* Status response */
							if ((runningRequestType == RequestType.CONFIG)
									|| (runningRequestType == RequestType.ABORT)) {
							}
						}
//					}
//				});
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Main function
	 * 
	 * Invoking the GUI controller constructor and starting SearchBooster.
	 * 
	 * @param args
	 *            Unused
	 */
	public static void main(String[] args) {
		GuiController guiController = new GuiController();
		guiController.start();
	}

	/**
	 * Search start (API call)
	 * 
	 * @param recursiveSelectedFiles
	 *            List of recursive selected search paths
	 * @param flatSelectedFiles
	 *            List of flat (not recursive) selected search paths
	 * @param searchParameters
	 *            Search parameter
	 */
	public void search(List<File> recursiveSelectedFiles,
			List<File> flatSelectedFiles, List<SearchParameter> searchParameters) {

		if (recursiveSelectedFiles == null) {
			throw new NullPointerException(
					"Recursive selected search path list is a null pointer.");
		}
		if (flatSelectedFiles == null) {
			throw new NullPointerException(
					"Flat selected search path list is a null pointer.");
		}

		/* Create search request and add search paths */
		Document searchRequest = RequestSearchBuilder.create();

		try {
			for (File recursiveSearchPath : recursiveSelectedFiles) {
				RequestSearchBuilder.appendPath(searchRequest,
						recursiveSearchPath.getCanonicalPath(), -1);
			}
			for (File flatSearchPath : flatSelectedFiles) {
				RequestSearchBuilder.appendPath(searchRequest,
						flatSearchPath.getCanonicalPath(), 0);
			}
		} catch (Exception e) {
			/* Search failed */
			guiView.setStatusLabelText("Error - Searching failed", false);
			return;
		}

		/* Search parameter */
		for (SearchParameter searchParameter : searchParameters) {
			/* Modification date */
			GregorianCalendar gregorianCalendar = new GregorianCalendar();
			XMLGregorianCalendar xmlBeginModificationDate = null;
			XMLGregorianCalendar xmlEndModificationDate = null;

			if (searchParameter.beginModificationDate != null) {
				gregorianCalendar
				.setTime(searchParameter.beginModificationDate);
				try {
					xmlBeginModificationDate = DatatypeFactory.newInstance()
							.newXMLGregorianCalendar(gregorianCalendar);
				} catch (DatatypeConfigurationException e) {
					/* Search failed */
					guiView.setStatusLabelText("Error - Searching failed",
							false);
					return;
				}
			}

			if (searchParameter.endModificationDate != null) {
				gregorianCalendar.setTime(searchParameter.endModificationDate);
				try {
					xmlEndModificationDate = DatatypeFactory.newInstance()
							.newXMLGregorianCalendar(gregorianCalendar);
				} catch (DatatypeConfigurationException e) {
					/* Search failed */
					guiView.setStatusLabelText("Error - Searching failed",
							false);
					return;
				}
			}

			/* Content */
			Content content = null;
			switch (searchParameter.fileType) {
			case DOCUMENT:
				content = Content
				.newDocumentContent(searchParameter.documentContentString);
				break;
			case MUSIC:
				content = Content.newMusicContent(
						searchParameter.musicInterpretString,
						searchParameter.musicAlbumString);
				break;
			case PICTURE:
				content = Content.newPictureContent();
				break;
			case VIDEO:
				content = Content.newVideoContent();
				break;
			case ALL:
			default:
				break;
			}

			/* File name */
			RequestSearchBuilder.appendParameter(searchRequest,
					searchParameter.fileNameString, xmlBeginModificationDate,
					xmlEndModificationDate, content);
		}

		guiView.setStatusLabelText("Searching...", true);
		this.sendRequest(searchRequest, RequestType.SEARCH);
	}

	/**
	 * Set index (API call)
	 * 
	 * @param recursiveSelectedFiles
	 *            List of recursive selected index paths
	 * @param flatSelectedFiles
	 *            List of flat (not recursive) selected index paths
	 * @param forceRebuild
	 *            Specifies if the index is rebuild or incremental updated
	 */
	public void setIndex(List<File> recursiveSelectedFiles,
			List<File> flatSelectedFiles, boolean forceRebuild) {

		if (recursiveSelectedFiles == null) {
			throw new NullPointerException(
					"Recursive selected index path list is a null pointer.");
		}
		if (flatSelectedFiles == null) {
			throw new NullPointerException(
					"Flat selected index path list is a null pointer.");
		}

		/* Create index request and add index paths */
		Document indexRequest = null;
		try {
			indexRequest = RequestIndexChangeBuilder.create(forceRebuild);

			for (File file : recursiveSelectedFiles) {
				RequestIndexChangeBuilder.appendPath(indexRequest,
						file.getCanonicalPath(), -1);
			}

			for (File file : flatSelectedFiles) {
				RequestIndexChangeBuilder.appendPath(indexRequest,
						file.getCanonicalPath(), 0);
			}
		} catch (Exception e) {
			/* Indexing failed */
			guiView.setStatusLabelText("Error - Indexing failed", false);
			return;
		}

		guiView.setStatusLabelText("Indexing...", true);
		this.sendRequest(indexRequest, RequestType.INDEX_CHANGE);
	}

	/**
	 * Get index (API call)
	 */
	public void getIndex() {

		Document indexInfoRequest = null;
		try {
			indexInfoRequest = RequestIndexInfoBuilder.create("");
		} catch (Exception e) {
			/* Getting index failed */
			guiView.setStatusLabelText("Error - Unable to get index", false);
			return;
		}

		guiView.setStatusLabelText("Fetching index...", true);
		this.sendRequest(indexInfoRequest, RequestType.INDEX_INFO);
	}

//	/**
//	 * Set API configuration
//	 * 
//	 * @param indexExpireTime
//	 *            Duration in days of index to expire
//	 */
//	public void config(int indexExpireTime) {
//
//		Document configRequest = null;
//		try {
//			configRequest = RequestConfigurationBuilder.create();
//			RequestConfigurationBuilder.setIndexExpireTime(
//					configRequest,
//					DatatypeFactory.newInstance().newDurationDayTime(true,
//							indexExpireTime, 0, 0, 0));
//		} catch (Exception e) {
//			/* Getting index failed */
//			guiView.setStatusLabelText("Error - Unable to configure settings",
//					false);
//			return;
//		}
//
//		guiView.setStatusLabelText("Configuring settings...", true);
//		this.sendRequest(configRequest, RequestType.CONFIG);
//	}

	/**
	 * Cancels a running request
	 */
	public void cancelRunningRequest() {

		/* Create cancel request */

		Document cancelRequest = RequestAbortBuilder
				.create("Cancel running request.");
		guiView.setStatusLabelText("Canceling running request...", true);
		this.sendRequest(cancelRequest, RequestType.ABORT);
	}

	/**
	 * Getter for GUI model ResultTableModel
	 * 
	 * @return ResultTableModel
	 */
	public final ResultTableModel getResultTableModel() {

		return resultTableModel;
	}

	/**
	 * Forward request to API
	 * 
	 * Forwards request to API and checks abortability of running request
	 * 
	 * @param request
	 *            API request
	 * @param requestType
	 *            Type of API request
	 */
	private void sendRequest(Document request, RequestType requestType) {

		synchronized (this.runningRequest) {
			this.runningRequest = requestType;
		}

		this.searchBooster.request(request);
	}

	/**
	 * Complete a running request
	 */
	private void requestCompleted() {
		synchronized (this.runningRequest) {
			runningRequest = RequestType.NONE;
		}
	}

}
