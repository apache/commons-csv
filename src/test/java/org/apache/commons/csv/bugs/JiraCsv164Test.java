package org.apache.commons.csv.bugs;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Test;

public class JiraCsv164Test {

    @Test
    public void testJiraCsv154_withCommentMarker() throws IOException {
        final String comment = "This is a header comment";
        final CSVFormat format = CSVFormat.EXCEL.withHeader("H1", "H2").withCommentMarker('#').withHeaderComments(comment);
        final StringBuilder out = new StringBuilder();
        final CSVPrinter printer = format.print(out);
        printer.print("A");
        printer.print("B");
        printer.close();
        final String s = out.toString();
        assertTrue(s, s.contains(comment));
    }

    @Test
    public void testJiraCsv154_withHeaderComments() throws IOException {
        final String comment = "This is a header comment";
        final CSVFormat format = CSVFormat.EXCEL.withHeader("H1", "H2").withHeaderComments(comment).withCommentMarker('#');
        final StringBuilder out = new StringBuilder();
        final CSVPrinter printer = format.print(out);
        printer.print("A");
        printer.print("B");
        printer.close();
        final String s = out.toString();
        assertTrue(s, s.contains(comment));
    }

}
