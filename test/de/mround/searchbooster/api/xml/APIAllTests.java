package de.mround.searchbooster.api.xml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RequestAbortBuilderTest.class, RequestAbortParserTest.class,
	RequestIndexChangeBuilderTest.class,
	RequestIndexChangeParserTest.class, RequestIndexInfoBuilderTest.class,
	RequestIndexInfoParserTest.class, RequestSearchBuilderTest.class,
	RequestSearchParserTest.class, ResponseIndexContentBuilderTest.class,
	ResponseMetaSearchResultBuilderTest.class, SearchBoosterXMLTest.class })
public class APIAllTests {

}
