package edu.mit.ll.vizlincdb.xml;

import com.ctc.wstx.stax.WstxInputFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.LocationInfo;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * XMLEntityParser parses true XML entity-marked transcripts and returns a
 * List<XMLMention>.
 */
public class XMLEntityParser {

    /**
     * Allowed entity tags. These tags always have one attribute, "TYPE"
     */
    Set<String> entityTags = new HashSet<String>();

    /**
     * An entity parser that handles PERSON, ORGANIZATION, LOCATION, DATE
     */
    public XMLEntityParser() {
        this(new String[]{"PERSON", "ORGANIZATION", "LOCATION", "DATE"});
    }

    /**
     * Handle the entities given in the entityTags list.
     *
     * @param tags
     */
    public XMLEntityParser(String[] tags) {
        Collections.addAll(this.entityTags, tags);
    }

    public List<XMLMention> getXMLMentions(File file) throws IOException, ParseException {
        return getXMLMentions(new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")));
    }

    public List<XMLMention> getXMLMentions(String s) throws ParseException {
        return getXMLMentions(new StringReader(s));
    }

    public List<XMLMention> getXMLMentions(Reader reader) throws ParseException {
        try {
            // Matches <SOMETHING TYPE="WHATEVER"> or </SOMETHING>
            // where SOMETHING is PERSON|ORGANIZATION|LOCATION|DATE.
            // There may be other entityTags in the document but they are ignored. Phoenix adds these, for instance:
            // DOC DOCID DOCTYPE BODY TEXT

            XMLInputFactory2 factory = new WstxInputFactory();
            factory.configureForConvenience();
            // Woodstox
            XMLStreamReader2 xmlReader = (XMLStreamReader2) factory.createXMLStreamReader(reader);

            // Build up a list of mentions to return.
            List<XMLMention> mentions = new ArrayList<XMLMention>();

            // Keep track of the currently unclosed tags.
            // The named entities are not supposed to be nested, but they might be.
            LinkedList<XMLMention> mentionStack = new LinkedList<XMLMention>();

            // Count the named entities, starting at 0.
            int index = -1;

           // Count how many tokens (whitespace-separated strings) we've encountered.
            int currentTokenOffset = -1;

            while (xmlReader.hasNext()) {
                int eventType = xmlReader.next();
                switch (eventType) {
                    case XMLStreamConstants.START_ELEMENT:
                        String name = xmlReader.getLocalName();
                        if (name.equals("TEXT")) {
                            // Don't start counting tokens until we're actually in the document <TEXT> section.
                            currentTokenOffset = 0;
                        } else if (entityTags.contains(name)) {
                            index++;
                            // This is a tag we care about.
                            XMLMention mention = new XMLMention();
                            mention.setType(name);
                            // Ignore TYPE="..." attribute, even if it exists. We used to save this as the "subtype".
                            // -1 means we don't have the text offset yet.
                            mention.setStartingCharOffset(-1);
                            mention.setEndingCharOffset(-1);
                            mention.setIndex(index);
                            mention.setDepth(mentionStack.size());
                            mentionStack.push(mention);
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                    case XMLStreamConstants.CDATA:
                        // Add the characters onto any existing unclosed entities. Remember character position if this is the first
                        // bunch of characters after the tag.
                        String text = xmlReader.getText();
                        for (XMLMention mention : mentionStack) {
                            mention.setText(mention.getText() + text);
                            LocationInfo loc = xmlReader.getLocationInfo();
                            // Remember the beginning if this is the first text we've encountered.
                            if (mention.getStartingCharOffset() == -1) {
                                mention.setStartingCharOffset(loc.getStartingCharOffset());
                                mention.setTokenOffset(currentTokenOffset);
                            }
                            // Keep moving the end marker as we accumulate text.
                            mention.setEndingCharOffset(loc.getEndingCharOffset());
                        }

                        // Keep track of how many tokens have gone by.
                        currentTokenOffset += text.trim().split("\\s+").length;
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        // If this is a tag we care about, it's closed, and we have all the text inside it.
                        if (entityTags.contains(xmlReader.getLocalName())) {
                            mentions.add(mentionStack.pop());
                        }
                        break;

                    // We don't care about all other states.
                    default:
                        break;
                }
            }

            // Reorder mentions by index.
            XMLMention sortedMentions[] = new XMLMention[mentions.size()];
            for (XMLMention m : mentions) {
                sortedMentions[m.getIndex()] = m;
            }
            return Arrays.asList(sortedMentions);

        } catch (XMLStreamException ex) {
            System.err.println(ex.getMessage());
            throw new ParseException(ex.getMessage(), ex.getLocation().getCharacterOffset());

        }
    }
}
