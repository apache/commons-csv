package org.apache.commons.csv.issues;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.junit.Assert;
import org.junit.Test;

/**
 * JIRA: <a href="https://issues.apache.org/jira/browse/CSV-203">withNullString value is printed without quotes when QuoteMode.ALL is specified</a>
 */
public class JiraCsv203Test {

    @Test
    public void testQuoteModeAll() throws Exception {
        CSVFormat format = CSVFormat.EXCEL
                .withNullString("N/A")
                .withIgnoreSurroundingSpaces(true)
                .withQuoteMode(QuoteMode.ALL);

        StringBuffer buffer = new StringBuffer();
        CSVPrinter printer = new CSVPrinter(buffer, format);
        printer.printRecord(new Object[] { null, "Hello", null, "World" });

        Assert.assertEquals("\"N/A\",\"Hello\",\"N/A\",\"World\"\r\n", buffer.toString());
    }

    @Test
    public void testQuoteModeAllNonNull() throws Exception {
        CSVFormat format = CSVFormat.EXCEL
                .withNullString("N/A")
                .withIgnoreSurroundingSpaces(true)
                .withQuoteMode(QuoteMode.ALL_NON_NULL);

        StringBuffer buffer = new StringBuffer();
        CSVPrinter printer = new CSVPrinter(buffer, format);
        printer.printRecord(new Object[] { null, "Hello", null, "World" });

        Assert.assertEquals("N/A,\"Hello\",N/A,\"World\"\r\n", buffer.toString());
    }

    @Test
    public void testWithoutQuoteMode() throws Exception {
        CSVFormat format = CSVFormat.EXCEL
                .withNullString("N/A")
                .withIgnoreSurroundingSpaces(true);

        StringBuffer buffer = new StringBuffer();
        CSVPrinter printer = new CSVPrinter(buffer, format);
        printer.printRecord(new Object[] { null, "Hello", null, "World" });

        Assert.assertEquals("N/A,Hello,N/A,World\r\n", buffer.toString());
    }

    @Test
    public void testQuoteModeMinimal() throws Exception {
        CSVFormat format = CSVFormat.EXCEL
                .withNullString("N/A")
                .withIgnoreSurroundingSpaces(true)
                .withQuoteMode(QuoteMode.MINIMAL);

        StringBuffer buffer = new StringBuffer();
        CSVPrinter printer = new CSVPrinter(buffer, format);
        printer.printRecord(new Object[] { null, "Hello", null, "World" });

        Assert.assertEquals("N/A,Hello,N/A,World\r\n", buffer.toString());
    }

    @Test
    public void testQuoteModeNonNumeric() throws Exception {
        CSVFormat format = CSVFormat.EXCEL
                .withNullString("N/A")
                .withIgnoreSurroundingSpaces(true)
                .withQuoteMode(QuoteMode.NON_NUMERIC);

        StringBuffer buffer = new StringBuffer();
        CSVPrinter printer = new CSVPrinter(buffer, format);
        printer.printRecord(new Object[] { null, "Hello", null, "World" });

        Assert.assertEquals("N/A,\"Hello\",N/A,\"World\"\r\n", buffer.toString());
    }

    @Test
    public void testWithoutNullString() throws Exception {
        CSVFormat format = CSVFormat.EXCEL
                //.withNullString("N/A")
                .withIgnoreSurroundingSpaces(true)
                .withQuoteMode(QuoteMode.ALL);

        StringBuffer buffer = new StringBuffer();
        CSVPrinter printer = new CSVPrinter(buffer, format);
        printer.printRecord(new Object[] { null, "Hello", null, "World" });

        Assert.assertEquals(",\"Hello\",,\"World\"\r\n", buffer.toString());
    }

    @Test
    public void testWithEmptyValues() throws Exception {
        CSVFormat format = CSVFormat.EXCEL
                .withNullString("N/A")
                .withIgnoreSurroundingSpaces(true)
                .withQuoteMode(QuoteMode.ALL);

        StringBuffer buffer = new StringBuffer();
        CSVPrinter printer = new CSVPrinter(buffer, format);
        printer.printRecord(new Object[] { "", "Hello", "", "World" });
        //printer.printRecord(new Object[] { null, "Hello", null, "World" });

        Assert.assertEquals("\"\",\"Hello\",\"\",\"World\"\r\n", buffer.toString());
    }
}
