package de.mround.searchbooster.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.NumericField;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import de.mround.searchbooster.api.Configuration.Index;
import de.mround.searchbooster.api.Utility.DatePrecision;

/**
 * LuceneDirectoryUtility provides many useful methods to operate on a
 * {@link LuceneDirectory}.
 * 
 * @author Kai Torben Ohlhus
 */
public final class LuceneDirectoryUtility {
    // List of prohibited well-known file extensions
    private final static List<String> whitelist = Collections
	    .unmodifiableList(new LinkedList<String>() {
		// anonymous inner class
		private static final long serialVersionUID = 1L;
		{
		    add(".doc");
		    add(".docx");
		    add(".odt");
		    add(".pdf");
		}
	    });

    /**
     * Private constructor to avoid instantiation.
     */
    private LuceneDirectoryUtility() {
    }

    /**
     * This method generates a {@link Document} representation of a
     * Meta-Index-Entry, that will be stored in a {@link LuceneDirectory}.
     * 
     * @param path
     *            - a path url to a directory that was in the index
     * @param recursionDepth
     *            - the recursion depth of indexing
     * @param millis
     *            - a timestamp in milliseconds indicating the age of this
     *            Meta-Index-Entry
     * @return a {@link Document} representation of a Meta-Index-Entry
     */
    protected static Document getIndexMetaDocument(String path,
	    int recursionDepth, long millis) {
	Document doc = new Document();

	doc.add(new NumericField(Index.META_DATE_INDEXED)
		.setLongValue(DatePrecision.convertPrecision(millis,
			DatePrecision.Millisecond,
			Index.META_DATE_INDEXED_PRECISION)));
	doc.add(new Field(Index.META_PATH, path, Field.Store.YES,
		Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
	doc.add(new Field(Index.META_RECURSIVE, Integer
		.toString(recursionDepth), Field.Store.YES,
		Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));

	return doc;
    }

    /**
     * Returns a {@link Document} representation of the {@link File} f, that
     * will be stored in a {@link LuceneDirectory}. The stored fields are
     * described in {@link Index}.
     * 
     * @param f
     *            - the {@link File} that should have a {@link Document}
     *            representation
     * @return a {@link Document} representation of the {@link File} f. Ready to
     *         insert it into the Lucene index
     */
    protected static Document getDocument(File f) {
	Document doc = new Document();

	// flat file info
	doc.add(new Field(Index.FILE_NAME, f.getName(), Field.Store.YES,
		Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
	String url = f.getAbsolutePath().substring(0,
		f.getAbsolutePath().lastIndexOf(File.separator))
		+ File.separator;
	doc.add(new Field(Index.URL, Utility.canonicalizePath(url),
		Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
		TermVector.NO));
	doc.add(new Field(Index.LAST_MODIFIED, Long.toString(DatePrecision
		.convertPrecision(f.lastModified(), DatePrecision.Millisecond,
			DatePrecision.Day)), Field.Store.YES,
		Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
	doc.add(new NumericField(Index.LAST_MODIFIED_SEARCH, 1,
		Field.Store.YES, true).setLongValue(DatePrecision
		.convertPrecision(f.lastModified(), DatePrecision.Millisecond,
			DatePrecision.Day)));

	if (f.getAbsolutePath().equals("/tmp/gedit-fifo.EARB8V")) {
	    System.err.println("DEBUG " + f.canRead() + " " + f.canWrite());
	}

	// advanced file info using Tika
	FileInputStream is = null;
	String type = "";
	String ext = "";
	try {
	    is = new FileInputStream(f);

	    // Get file extension (if period exists)
	    if (f.getName().contains(".")) {
		ext = f.getName().substring(f.getName().lastIndexOf("."))
			.toLowerCase();
	    }

	    ContentHandler contenthandler = new BodyContentHandler();
	    Metadata metadata = new Metadata();
	    metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
	    Parser parser = new AutoDetectParser();

	    // Parse file
	    if (f.isFile() && f.canRead()) {
		parser.parse(is, contenthandler, metadata, new ParseContext());
	    }

	    // Print metadata
	    // for (String n : metadata.names()) {
	    // System.out.println(n + ": " + metadata.get(n));
	    // }

	    type = metadata.get("Content-Type");

	    // get MIME-type, if file is no byte stream
	    // MIME-type in form xxx/xxx
	    if (!type.equals("application/octet-stream") && type.contains("/")) {
		type = type.substring(0, type.indexOf("/"));
	    }

	    // System.out.println(f.getName() + ": " + type);

	    if (type.equals("text")) {
		// mostly text files, octet-stream (byte stream) not
		// wanted
		doc.add(new Field(Index.TYPE, "document", Field.Store.YES,
			Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
		// Add content
		doc.add(new Field(Index.TEXT_CONTENT,
			contenthandler.toString(), Field.Store.NO,
			Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
	    } else if (type.equals("application")) {
		// mostly text files, octet-stream (byte stream) not
		// wanted
		doc.add(new Field(Index.TYPE, "document", Field.Store.YES,
			Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));

		if (whitelist.contains(ext)) {
		    doc.add(new Field(Index.TEXT_CONTENT, contenthandler
			    .toString().toLowerCase(), Field.Store.NO,
			    Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
		}
	    } else if (type.equals("image")) {
		doc.add(new Field(Index.TYPE, "image", Field.Store.YES,
			Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
	    } else if (type.equals("audio")) {
		doc.add(new Field(Index.TYPE, "audio", Field.Store.YES,
			Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));

		// Store artist or author
		if (metadata.get("xmpDM:artist") != null) {
		    doc.add(new Field(Index.INTERPRET, metadata.get(
			    "xmpDM:artist").toLowerCase(), Field.Store.YES,
			    Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
		} else if (metadata.get("author") != null) {
		    doc.add(new Field(Index.INTERPRET, metadata.get("author")
			    .toLowerCase(), Field.Store.YES,
			    Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
		}
		// Store album
		if (metadata.get("xmpDM:album") != null) {
		    doc.add(new Field(Index.ALBUM, metadata.get("xmpDM:album")
			    .toLowerCase(), Field.Store.YES,
			    Field.Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
		}
	    } else {
		// no entry
	    }

	} catch (FileNotFoundException ignore) {
	} catch (IOException ignore) {
	} catch (SAXException ignore) {
	} catch (TikaException ignore) {
	}

	return doc;
    }
}
