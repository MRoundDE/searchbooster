package de.mround.searchbooster.api.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SearchBoosterXMLTest {

    private static File[] xmlTestFiles;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	System.out.println("XML Tests");
	System.out.println("---------");
	System.out.println("");
	String rootDir = "test" + File.separator + "de" + File.separator
		+ "mround" + File.separator + "searchbooster" + File.separator
		+ "api" + File.separator + "xml";
	xmlTestFiles = new File(rootDir).listFiles(new FileFilter() {

	    @Override
	    public boolean accept(File pathname) {
		return pathname.getName().endsWith(".xml");
	    }
	});
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    @Test
    public void setUp() throws Exception {
    }

    @After
    @Test
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetSchema() {
	System.out.println("Test Schema generation.");
	System.out.println("");
	assertNotNull(SearchBoosterXML.getSchema());
    }

    @Test
    public void testGetFormattedStringFromDocument() {

    }

    @Test
    public void testGetDocument() {
	System.out.println("Test various XML-documents.");
	System.out.println("");
	Validator v = SearchBoosterXML.getSchema().newValidator();

	for (File f : xmlTestFiles) {
	    System.out.print("\t" + f.getName() + "\t\t");
	    boolean parserExceptionCaught = false;

	    // 1.) Check the Validator
	    try {
		v.validate(new StreamSource(f));
	    } catch (SAXException e) {
		parserExceptionCaught = true;
		if (f.getName().contains("valid")) {
		    System.out.println("FAIL");
		    fail("Parsing File <" + f.getName() + "> failed: "
			    + e.getMessage());
		}
	    } catch (IOException e) {
		System.out.println("FAIL");
		fail("IOException in <" + f.getName() + ">: " + e.getMessage());
	    }

	    // 2.) Check if invalid File passed the Validator
	    if (!f.getName().contains("valid") && !parserExceptionCaught) {
		System.out.println("FAIL");
		fail("Invalid File <" + f.getName() + "> passed the Validator.");
	    }

	    // 3.) Check invalid Files to get their Document representation
	    if (f.getName().contains("valid")) {
		try {
		    SearchBoosterXML.getDocument(f);
		} catch (SAXException e) {
		    System.out.println("FAIL");
		    fail("Parsing File <" + f.getName() + "> failed: "
			    + e.getMessage());
		} catch (IOException e) {
		    System.out.println("FAIL");
		    fail("IOException in <" + f.getName() + ">: "
			    + e.getMessage());
		}
	    }
	    System.out.println("OK");
	}
    }
}
