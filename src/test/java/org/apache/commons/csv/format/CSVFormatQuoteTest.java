package org.apache.commons.csv.format;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.apache.commons.csv.Constants.LF;
import static org.junit.jupiter.api.Assertions.*;

public class CSVFormatQuoteTest {

    @Test
    public void testEqualsLeftNoQuoteRightQuote() {
        final CSVFormat left = CSVFormat.newFormat(',').builder().setQuote(null).build();
        final CSVFormat right = left.builder().setQuote('#').build();

        assertNotEquals(left, right);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEqualsLeftNoQuoteRightQuote_Deprecated() {
        final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
        final CSVFormat right = left.withQuote('#');

        assertNotEquals(left, right);
    }

    @Test
    public void testEqualsNoQuotes() {
        final CSVFormat left = CSVFormat.newFormat(',').builder().setQuote(null).build();
        final CSVFormat right = left.builder().setQuote(null).build();

        assertEquals(left, right);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEqualsNoQuotes_Deprecated() {
        final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
        final CSVFormat right = left.withQuote(null);

        assertEquals(left, right);
    }


    @Test
    public void testEqualsQuoteChar() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setQuote('"').build();
        final CSVFormat left = right.builder().setQuote('!').build();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEqualsQuoteChar_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuote('"');
        final CSVFormat left = right.withQuote('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsQuotePolicy() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder()
                .setQuote('"')
                .setQuoteMode(QuoteMode.ALL)
                .build();
        final CSVFormat left = right.builder()
                .setQuoteMode(QuoteMode.MINIMAL)
                .build();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEqualsQuotePolicy_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withQuoteMode(QuoteMode.MINIMAL);

        assertNotEquals(right, left);
    }

    @Test
    public void testPrintWithoutQuotes() throws IOException {
        final Reader in = new StringReader("");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withDelimiter(',').withQuote('"').withEscape('?').withQuoteMode(QuoteMode.NON_NUMERIC);
        format.print(in, out, true);
        assertEquals("\"\"", out.toString());
    }

    @Test
    public void testPrintWithQuoteModeIsNONE() throws IOException {
        final Reader in = new StringReader("a,b,c");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withDelimiter(',').withQuote('"').withEscape('?').withQuoteMode(QuoteMode.NONE);
        format.print(in, out, true);
        assertEquals("a?,b?,c", out.toString());
    }

    @Test
    public void testPrintWithQuotes() throws IOException {
        final Reader in = new StringReader("\"a,b,c\r\nx,y,z");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withDelimiter(',').withQuote('"').withEscape('?').withQuoteMode(QuoteMode.NON_NUMERIC);
        format.print(in, out, true);
        assertEquals("\"\"\"a,b,c\r\nx,y,z\"", out.toString());
    }

    @Test
    public void testQuoteCharSameAsCommentStartThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setQuote('!').setCommentMarker('!').build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testQuoteCharSameAsCommentStartThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!'));
    }

    @Test
    public void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.builder().setQuote(Character.valueOf('!')).setCommentMarker('!').build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType_Deprecated() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.withQuote(Character.valueOf('!')).withCommentMarker('!'));
    }

    @Test
    public void testQuoteCharSameAsDelimiterThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setQuote('!').setDelimiter('!').build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testQuoteCharSameAsDelimiterThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withQuote('!').withDelimiter('!'));
    }

    @Test
    public void testQuotePolicyNoneWithoutEscapeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.newFormat('!').builder().setQuoteMode(QuoteMode.NONE).build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testQuotePolicyNoneWithoutEscapeThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.newFormat('!').withQuoteMode(QuoteMode.NONE));
    }

    @Test
    public void testWithQuoteChar() {
        final CSVFormat formatWithQuoteChar = CSVFormat.DEFAULT.withQuote('"');
        assertEquals(Character.valueOf('"'), formatWithQuoteChar.getQuoteCharacter());
    }

    @Test
    public void testWithQuoteLFThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withQuote(LF));
    }

    @Test
    public void testWithQuotePolicy() {
        final CSVFormat formatWithQuotePolicy = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, formatWithQuotePolicy.getQuoteMode());
    }
}
