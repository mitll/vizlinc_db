package edu.mit.ll.vizlincdb.xml;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 */
public class XMLEntityParserTest {

    public XMLEntityParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<DOC>\n"
            + "<DOCID> somedoc </DOCID>\n"
            + "<DOCTYPE SOURCE=\"interview\"> INTERVIEW </DOCTYPE>\n"
            + "<BODY>\n"
            + "<TEXT>\n";
    String FOOTER = "</TEXT>\n"
            + "</BODY>\n"
            + "</DOC>\n";

    /**
     * Test of getXMLMentions method, of class XMLEntityParser.
     */
    //@Test
    public void testParse_File() throws Exception {
        File file = null;
        XMLEntityParser instance = new XMLEntityParser();
        List expResult = null;
        List result = instance.getXMLMentions(file);
        //assertEquals(expResult, result);
    }

    @Test
    public void testParse_StringOneEntity() throws Exception {
        String contents = HEADER + "abc <ENA TYPE=\"SUBA\">def</ENA> ghi" + FOOTER;
        XMLEntityParser instance = new XMLEntityParser(new String[] {"ENA"});
        List expResult = new ArrayList();
        expResult.add(new XMLMention("ENA", "def", 0, contents.indexOf("def"), contents.indexOf("def") + 3, 1, 0));
        List result = instance.getXMLMentions(contents);
        System.out.println(">>>");
        System.out.println(contents);
        System.out.println("<<<");
        assertEquals(expResult, result);
    }

    @Test
    public void testParse_StringTwoEntities() throws Exception {
        String contents = HEADER + "abc <ENA TYPE=\"SUBA\">def</ENA> ghi <ENB TYPE=\"SUBB\">jkl</ENB> mno" + FOOTER;
        XMLEntityParser instance = new XMLEntityParser(new String[] {"ENA", "ENB"});
        List expResult = new ArrayList();
        expResult.add(new XMLMention("ENA", "def", 0, contents.indexOf("def"), contents.indexOf("def") + 3, 1, 0));
        expResult.add(new XMLMention("ENB", "jkl", 1, contents.indexOf("jkl"), contents.indexOf("jkl") + 3, 3, 0));
        List result = instance.getXMLMentions(contents);
        assertEquals(expResult, result);
    }

    @Test
    public void testParse_StringTwoNestedEntities() throws Exception {
        String contents = HEADER + "abc <ENA TYPE=\"SUBA\">def <ENB TYPE=\"SUBB\">ghi</ENB> jkl</ENA> mno" + FOOTER;
        XMLEntityParser instance = new XMLEntityParser(new String[] {"ENA", "ENB"});
        List expResult = new ArrayList();
        expResult.add(new XMLMention("ENA", "def ghi jkl", 0, contents.indexOf("def"), contents.indexOf("jkl") + 3, 1, 0));
        expResult.add(new XMLMention("ENB", "ghi", 1, contents.indexOf("ghi"), contents.indexOf("ghi") + 3, 2, 1));
        List result = instance.getXMLMentions(contents);
        assertEquals(expResult, result);
    }


    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testParse_StringMismatchedEndTag() throws Exception {
        exception.expect(ParseException.class);
        String contents = HEADER + "012<ABC TYPE=\"SUBTYPE\">abctext</DEF>end" + FOOTER;
        XMLEntityParser instance = new XMLEntityParser(new String[] {"ABC"});
        instance.getXMLMentions(contents);
    }

    @Test
    public void testParse_StringMissingEndTag() throws Exception {
        exception.expect(ParseException.class);
        String contents = HEADER + "012<ABC TYPE=\"SUBTYPE\">abctext" + FOOTER;
        XMLEntityParser instance = new XMLEntityParser(new String[] {"ABC"});
        instance.getXMLMentions(contents);
    }
}
