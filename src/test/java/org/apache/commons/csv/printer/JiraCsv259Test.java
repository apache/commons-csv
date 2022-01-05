package org.apache.commons.csv.printer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JiraCsv259Test {

    @Test
    public void testCSV259() throws IOException {
        final StringWriter sw = new StringWriter();
        try (final Reader reader = new FileReader("src/test/resources/org/apache/commons/csv/CSV-259/sample.txt");
             final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withEscape('!').withQuote(null))) {
            printer.print(reader);
            assertEquals("x!,y!,z", sw.toString());
        }
    }
}
