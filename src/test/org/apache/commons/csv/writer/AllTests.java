package org.apache.commons.csv.writer;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.apache.commons.csv.writer");
        //$JUnit-BEGIN$
        suite.addTestSuite(CSVConfigGuesserTest.class);
        suite.addTestSuite(CSVConfigTest.class);
        suite.addTestSuite(CSVFieldTest.class);
        suite.addTestSuite(CSVWriterTest.class);
        //$JUnit-END$
        return suite;
    }

}
