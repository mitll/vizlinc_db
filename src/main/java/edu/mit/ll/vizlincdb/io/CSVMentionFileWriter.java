package edu.mit.ll.vizlincdb.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.supercsv.io.CsvBeanWriter;
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
 *
 */
public class CSVMentionFileWriter {

    CsvBeanWriter csvBeanWriter;

     public CSVMentionFileWriter(File file) throws IOException  {
        csvBeanWriter = new CsvBeanWriter(new FileWriter(file), CsvPreference.STANDARD_PREFERENCE);
    }

    public void write(CSVMention csvMention) throws IOException {
        csvBeanWriter.write(csvMention, CSVMentionFileReader.FIELDS, CSVMentionFileReader.PROCESSORS);
    }

    public void close() throws IOException {
        csvBeanWriter.close();
    }
}
