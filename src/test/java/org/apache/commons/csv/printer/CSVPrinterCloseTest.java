package org.apache.commons.csv.printer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;

import static org.mockito.Mockito.*;

public class CSVPrinterCloseTest {

    @Test
    public void testCloseBackwardCompatibility() throws IOException {
        try (final Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT;
            try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
                // empty
            }
            verify(writer, never()).flush();
            verify(writer, times(1)).close();
        }}

    @Test
    public void testCloseWithCsvFormatAutoFlushOff() throws IOException {
        try (final Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT.withAutoFlush(false);
            try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
                // empty
            }
            verify(writer, never()).flush();
            verify(writer, times(1)).close();
        }
    }

    @Test
    public void testCloseWithCsvFormatAutoFlushOn() throws IOException {
        // System.out.println("start method");
        try (final Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT.withAutoFlush(true);
            try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
                // empty
            }
            verify(writer, times(1)).flush();
            verify(writer, times(1)).close();
        }}

    @Test
    public void testCloseWithFlushOff() throws IOException {
        try (final Writer writer = mock(Writer.class)) {
            final CSVFormat csvFormat = CSVFormat.DEFAULT;
            @SuppressWarnings("resource")
            final CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
            csvPrinter.close(false);
            verify(writer, never()).flush();
            verify(writer, times(1)).close();
        }
    }

    @Test
    public void testCloseWithFlushOn() throws IOException {
        try (final Writer writer = mock(Writer.class)) {
            @SuppressWarnings("resource")
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
            csvPrinter.close(true);
            verify(writer, times(1)).flush();
        }
    }
}
