package org.apache.commons.csv.format;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.apache.commons.csv.Constants.CR;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CSVFormatHeaderTest {

    public enum Header {
        Name, Email, Phone
    }

    @Test
    public void testDuplicateHeaderElements() {
        final String[] header = { "A", "A" };
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(header).build();
        assertEquals(2, format.getHeader().length);
        assertArrayEquals(header, format.getHeader());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDuplicateHeaderElements_Deprecated() {
        final String[] header = { "A", "A" };
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        assertEquals(2, format.getHeader().length);
        assertArrayEquals(header, format.getHeader());
    }

    @Test
    public void testDuplicateHeaderElementsFalse() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(false).setHeader("A", "A").build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDuplicateHeaderElementsFalse_Deprecated() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.withAllowDuplicateHeaderNames(false).withHeader("A", "A"));
    }

    public void testDuplicateHeaderElementsTrue() {
        CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(true).setHeader("A", "A").build();
    }

    @SuppressWarnings("deprecation")
    public void testDuplicateHeaderElementsTrue_Deprecated() {
        CSVFormat.DEFAULT.withAllowDuplicateHeaderNames(true).withHeader("A", "A");
    }

    @Test
    public void testEqualsHeader() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder()
                .setRecordSeparator(CR)
                .setCommentMarker('#')
                .setEscape('+')
                .setHeader("One", "Two", "Three")
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setQuote('"')
                .setQuoteMode(QuoteMode.ALL)
                .build();
        final CSVFormat left = right.builder()
                .setHeader("Three", "Two", "One")
                .build();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEqualsHeader_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withHeader("One", "Two", "Three")
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withHeader("Three", "Two", "One");

        assertNotEquals(right, left);
    }


    public void testEqualsSkipHeaderRecord() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder()
                .setRecordSeparator(CR)
                .setCommentMarker('#')
                .setEscape('+')
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setQuote('"')
                .setQuoteMode(QuoteMode.ALL)
                .setNullString("null")
                .setSkipHeaderRecord(true)
                .build();
        final CSVFormat left = right.builder()
                .setSkipHeaderRecord(false)
                .build();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEqualsSkipHeaderRecord_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL)
                .withNullString("null")
                .withSkipHeaderRecord();
        final CSVFormat left = right
                .withSkipHeaderRecord(false);

        assertNotEquals(right, left);
    }

    @Test
    public void testGetHeader() {
        final String[] header = {"one", "two", "three"};
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        // getHeader() makes a copy of the header array.
        final String[] headerCopy = formatWithHeader.getHeader();
        headerCopy[0] = "A";
        headerCopy[1] = "B";
        headerCopy[2] = "C";
        assertFalse(Arrays.equals(formatWithHeader.getHeader(), headerCopy));
        assertNotSame(formatWithHeader.getHeader(), headerCopy);
    }

    @Test
    public void testWithHeaderEnum() {
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(Header.class);
        assertArrayEquals(new String[]{ "Name", "Email", "Phone" }, formatWithHeader.getHeader());
    }
}
