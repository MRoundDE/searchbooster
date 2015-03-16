package de.mround.searchbooster.gui.controller;

import java.util.Date;

import de.mround.searchbooster.api.xml.RequestSearchBuilder.Content.FileType;

/**
 * Search parameter
 * 
 * @author Tobias Schulz
 */
public class SearchParameter {
	
	/**
	 * Modification date begin bound
	 */
	public Date beginModificationDate = null;
	
	/**
	 * Modification date end bound
	 */
	public Date endModificationDate = null;
	
	/**
	 * File name
	 */
	public String fileNameString = "";
	
	/**
	 * File type
	 */
	public FileType fileType = FileType.ALL;
	
	/**
	 * Document content search string
	 */
	public String documentContentString = null;
	
	/**
	 * Music interpret search string
	 */
	public String musicInterpretString = null;
	
	/**
	 * Music album search string
	 */
	public String musicAlbumString = null;
}
