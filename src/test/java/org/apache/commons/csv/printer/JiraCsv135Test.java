package org.apache.commons.csv.printer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.csv.Constants.BACKSLASH;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JiraCsv135Test extends AbstractCSVPrinterTest {

    @Test
    public void testCSV135() throws IOException {
        final List<String> list = new LinkedList<>();
        list.add("\"\"");   // ""
        list.add("\\\\");   // \\
        list.add("\\\"\\"); // \"\
        //
        // "",\\,\"\ (unchanged)
        tryFormat(list, null, null, "\"\",\\\\,\\\"\\");
        //
        // """""",\\,"\""\" (quoted, and embedded DQ doubled)
        tryFormat(list, '"',  null, "\"\"\"\"\"\",\\\\,\"\\\"\"\\\"");
        //
        // "",\\\\,\\"\\ (escapes escaped, not quoted)
        tryFormat(list, null, '\\', "\"\",\\\\\\\\,\\\\\"\\\\");
        //
        // "\"\"","\\\\","\\\"\\" (quoted, and embedded DQ & escape escaped)
        tryFormat(list, '"',  '\\', "\"\\\"\\\"\",\"\\\\\\\\\",\"\\\\\\\"\\\\\"");
        //
        // """""",\\,"\""\" (quoted, embedded DQ escaped)
        tryFormat(list, '"',  '"',  "\"\"\"\"\"\",\\\\,\"\\\"\"\\\"");
    }


    @Test
    @Disabled
    public void testJira135_part1() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (final CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\"");
            printer.printRecord(list);
        }
        final String expected = "\"\\\"\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    @Test
    @Disabled
    public void testJira135_part2() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (final CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\n");
            printer.printRecord(list);
        }
        final String expected = "\"\\n\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    @Test
    @Disabled
    public void testJira135_part3() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (final CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\\");
            printer.printRecord(list);
        }
        final String expected = "\"\\\\\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    @Test
    @Disabled
    public void testJira135All() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n').withQuote(DQUOTE_CHAR).withEscape(BACKSLASH);
        final StringWriter sw = new StringWriter();
        final List<String> list = new LinkedList<>();
        try (final CSVPrinter printer = new CSVPrinter(sw, format)) {
            list.add("\"");
            list.add("\n");
            list.add("\\");
            printer.printRecord(list);
        }
        final String expected = "\"\\\"\",\"\\n\",\"\\\"" + format.getRecordSeparator();
        assertEquals(expected, sw.toString());
        final String[] record0 = toFirstRecordValues(expected, format);
        assertArrayEquals(expectNulls(list.toArray(), format), record0);
    }

    private void tryFormat(final List<String> list, final Character quote, final Character escape, final String expected) throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuote(quote).withEscape(escape).withRecordSeparator(null);
        final Appendable out = new StringBuilder();
        try (final CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.printRecord(list);
        }
        assertEquals(expected, out.toString());
    }

}
