package edu.mit.ll.vizlincdb.io;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public class VizLincGraphPopulatorTest {

    static File sample_txt_file;
    static File sample_mentions_file;

    public VizLincGraphPopulatorTest() {
    }

    @ClassRule
    public static TemporaryFolder temp_dir = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws IOException {
        sample_txt_file = temp_dir.newFile();
        FileUtils.writeStringToFile(sample_txt_file,
                "abc Pedro Almodóvar def Nezahualcóyotl ghi",
                "UTF-8");
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
     * Test of main method, of class VizLincGraphPopulator.
     */
    @Test
    public void testMain() throws IOException {
        String[] args = new String[]{
            "-d", temp_dir.newFolder().getAbsolutePath(),
            sample_txt_file.getAbsolutePath()
        };

        VizLincGraphPopulator.main(args);
    }

}