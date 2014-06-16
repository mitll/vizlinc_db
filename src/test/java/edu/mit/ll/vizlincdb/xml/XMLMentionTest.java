package edu.mit.ll.vizlincdb.xml;

import edu.mit.ll.vizlincdb.xml.XMLMention;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class XMLMentionTest {

    public XMLMentionTest() {
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

    /**
     * Test of toString method, of class XMLMention.
     */
    @Test
    public void testToString() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals("XMLMention{type=TYPE, text=text, index=3, startingCharOffset=33, endingCharOffset=44, tokenOffset=3, depth=1}", m.toString());
    }

    /**
     * Test of hashCode method, of class XMLMention.
     */
    @Test
    public void testHashCode() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals(-555599758, m.hashCode());
    }

    /**
     * Test of equals method, of class XMLMention.
     */
    @Test
    public void testEquals() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        XMLMention m1 = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals(m, m1);
        XMLMention mTypeDiff = new XMLMention("TYPEX", "text", 3, 33, 44, 3, 1);
        XMLMention mTextDiff = new XMLMention("TYPE", "textx", 3, 33, 44, 3, 1);
        XMLMention mIndexDiff = new XMLMention("TYPE", "text", 4, 33, 44, 3, 1);
        XMLMention mStartingCharOffsetDiff = new XMLMention("TYPE", "text", 3, 32, 44, 3, 1);
        XMLMention mEndingCharOffsetDiff = new XMLMention("TYPE", "text", 3, 33, 45, 3, 1);
        XMLMention mTokenOffsetDiff = new XMLMention("TYPE", "text", 3, 33, 45, 4, 1);
        XMLMention mNestingLevelDiff = new XMLMention("TYPE", "text", 3, 33, 44, 3, 2);
        assertFalse(m.equals(mTypeDiff));
        assertFalse(m.equals(mTextDiff));
        assertFalse(m.equals(mIndexDiff));
        assertFalse(m.equals(mStartingCharOffsetDiff));
        assertFalse(m.equals(mEndingCharOffsetDiff));
        assertFalse(m.equals(mTokenOffsetDiff));
        assertFalse(m.equals(mNestingLevelDiff));
    }

    /**
     * Test of getType method, of class XMLMention.
     */
    @Test
    public void testGetType() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals("TYPE", m.getType());
    }

    /**
     * Test of setType method, of class XMLMention.
     */
    @Test
    public void testSetType() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        m.setType("NEWTYPE");
        assertEquals("NEWTYPE", m.getType());
    }

    /**
     * Test of getText method, of class XMLMention.
     */
    @Test
    public void testGetText() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals("text", m.getText());

    }

    /**
     * Test of setText method, of class XMLMention.
     */
    @Test
    public void testSetText() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        m.setText("newtext");
        assertEquals("newtext", m.getText());
    }

    /**
     * Test of getIndex method, of class XMLMention.
     */
    @Test
    public void testGetIndex() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals(3, m.getIndex());
    }

    /**
     * Test of setIndex method, of class XMLMention.
     */
    @Test
    public void testSetIndex() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        m.setIndex(4);
        assertEquals(4, m.getIndex());
    }

    /**
     * Test of getStartingCharOffset method, of class XMLMention.
     */
    @Test
    public void testGetStartingCharOffset() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals(33, m.getStartingCharOffset());
    }

    /**
     * Test of setStartingCharOffset method, of class XMLMention.
     */
    @Test
    public void testSetStartingCharOffset() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        m.setStartingCharOffset(32);
        assertEquals(32, m.getStartingCharOffset());
    }

    /**
     * Test of getEndingCharOffset method, of class XMLMention.
     */
    @Test
    public void testGetEndingCharOffset() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals(44, m.getEndingCharOffset());
    }

    /**
     * Test of setEndingCharOffset method, of class XMLMention.
     */
    @Test
    public void testSetEndingCharOffset() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        m.setEndingCharOffset(45);
        assertEquals(45, m.getEndingCharOffset());
    }

    /**
     * Test of getTokenOffset method, of class XMLMention.
     */
    @Test
    public void testGetTokenOffset() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals(3, m.getTokenOffset());
    }

    /**
     * Test of setTokenOffset method, of class XMLMention.
     */
    @Test
    public void testSetTokenOffset() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        m.setTokenOffset(4);
        assertEquals(4, m.getTokenOffset());
    }

    /**
     * Test of getDepth method, of class XMLMention.
     */
    @Test
    public void testGetDepth() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        assertEquals(1, m.getDepth());
    }

    /**
     * Test of setDepth method, of class XMLMention.
     */
    @Test
    public void testSetDepth() {
        XMLMention m = new XMLMention("TYPE", "text", 3, 33, 44, 3, 1);
        m.setDepth(2);
        assertEquals(2, m.getDepth());
    }
}
