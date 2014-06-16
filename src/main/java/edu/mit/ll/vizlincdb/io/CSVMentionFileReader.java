package edu.mit.ll.vizlincdb.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Read entity mentions from a CSV file with the following format:
 * entityType,startPosition,stopPosition,indexInDocument,globalId,"mentionText"
 *
 *
 * entity_type:     PERSON, LOCATION, etc.
 * startPosition:   character offset for start of string in the document for this mention
 * stopPosition:    character offset for end of string in the document for this mention
 * indexInDocument: this is the i-th entity mention in this document (unique id for the document)
 * globalId:        a global integer id for this mention, if known. Empty if not known.
 *                  This might be used to indicate a well-known entity, a topic id, etc.
 * "mentionText":   the text of the mention as found in the document. This is redundant but will make
 *                  debugging and examination of NER results easier.
 *                  May be omitted if unwieldy (for instance, if it's the whole document)
 *
 * globalId and mentionText are definitely optional, and will be null if not present.
 * TODO: It's a design question whether indexInDocument and the others are optional.
 */
public class CSVMentionFileReader {


    public final static String[] FIELDS = {"entityType", "startPosition", "stopPosition", "indexInDocument", "globalId", "mentionText"};
    // TODO: It's a design question about which fields might be optional.
    public final static CellProcessor[] PROCESSORS = {
        new NotNull(),  // entityType
        new ParseInt(), // startPosition
        new ParseInt(), // stopPosition
        new NotNull(new ParseInt()),  // indexInDocument
        new Optional(new ParseInt()), // globalId
        new Optional() // mentionText
    };

    CsvBeanReader csvBeanReader;

     public CSVMentionFileReader(File file) throws FileNotFoundException {
        csvBeanReader = new CsvBeanReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);
    }

    public CSVMention next() throws IOException {
        return csvBeanReader.read(CSVMention.class, FIELDS, PROCESSORS);
    }

}
