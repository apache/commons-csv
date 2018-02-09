/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.csv;

import static junit.framework.TestCase.assertNull;
import static org.apache.commons.csv.CSVFormat.RFC4180;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.LF;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CSVFormatTest {

    public enum EmptyEnum {
    }

    public enum Header {
        Name, Email, Phone
    }

    private static void assertNotEquals(final Object right, final Object left) {
        assertFalse(right.equals(left));
        assertFalse(left.equals(right));
    }

    private static CSVFormat copy(final CSVFormat format) {
        return format.withDelimiter(format.getDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterSameAsEscapeThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withEscape('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateHeaderElements() {
        CSVFormat.DEFAULT.withHeader("A", "A");
    }

    @Test
    public void testEquals() {
        final CSVFormat right = CSVFormat.DEFAULT;
        final CSVFormat left = copy(right);

        assertFalse(right.equals(null));
        assertFalse(right.equals("A String Instance"));

        assertEquals(right, right);
        assertEquals(right, left);
        assertEquals(left, right);

        assertEquals(right.hashCode(), right.hashCode());
        assertEquals(right.hashCode(), left.hashCode());
    }

    @Test
    public void testEqualsCommentStart() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuote('"')
                .withCommentMarker('#')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withCommentMarker('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsDelimiter() {
        final CSVFormat right = CSVFormat.newFormat('!');
        final CSVFormat left = CSVFormat.newFormat('?');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsEscape() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuote('"')
                .withCommentMarker('#')
                .withEscape('+')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withEscape('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsHeader() {
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

    @Test
    public void testEqualsIgnoreEmptyLines() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withIgnoreEmptyLines(false);

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsIgnoreSurroundingSpaces() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withIgnoreSurroundingSpaces(false);

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsLeftNoQuoteRightQuote() {
    	final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
    	final CSVFormat right = left.withQuote('#');

    	assertNotEquals(left, right);
    }

    @Test
    public void testEqualsNoQuotes() {
    	final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
    	final CSVFormat right = left.withQuote(null);

    	assertEquals(left, right);
    }

    @Test
    public void testEqualsNullString() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL)
                .withNullString("null");
        final CSVFormat left = right
                .withNullString("---");

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsOne() {

        final CSVFormat cSVFormatOne = CSVFormat.INFORMIX_UNLOAD;
        final CSVFormat cSVFormatTwo = CSVFormat.MYSQL;


        assertEquals('\\', (char)cSVFormatOne.getEscapeCharacter());
        assertNull(cSVFormatOne.getQuoteMode());

        assertTrue(cSVFormatOne.getIgnoreEmptyLines());
        assertFalse(cSVFormatOne.getSkipHeaderRecord());

        assertFalse(cSVFormatOne.getIgnoreHeaderCase());
        assertNull(cSVFormatOne.getCommentMarker());

        assertFalse(cSVFormatOne.isCommentMarkerSet());
        assertTrue(cSVFormatOne.isQuoteCharacterSet());

        assertEquals('|', cSVFormatOne.getDelimiter());
        assertFalse(cSVFormatOne.getAllowMissingColumnNames());

        assertTrue(cSVFormatOne.isEscapeCharacterSet());
        assertEquals("\n", cSVFormatOne.getRecordSeparator());

        assertEquals('\"', (char)cSVFormatOne.getQuoteCharacter());
        assertFalse(cSVFormatOne.getTrailingDelimiter());

        assertFalse(cSVFormatOne.getTrim());
        assertFalse(cSVFormatOne.isNullStringSet());

        assertNull(cSVFormatOne.getNullString());
        assertFalse(cSVFormatOne.getIgnoreSurroundingSpaces());


        assertTrue(cSVFormatTwo.isEscapeCharacterSet());
        assertNull(cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormatTwo.getQuoteMode());

        assertEquals('\t', cSVFormatTwo.getDelimiter());
        assertEquals("\n", cSVFormatTwo.getRecordSeparator());

        assertFalse(cSVFormatTwo.isQuoteCharacterSet());
        assertTrue(cSVFormatTwo.isNullStringSet());

        assertEquals('\\', (char)cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertFalse(cSVFormatTwo.getIgnoreEmptyLines());

        assertEquals("\\N", cSVFormatTwo.getNullString());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormatTwo.getTrailingDelimiter());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormatTwo, cSVFormatOne);
        assertFalse(cSVFormatTwo.equals(cSVFormatOne));

        assertEquals('\\', (char)cSVFormatOne.getEscapeCharacter());
        assertNull(cSVFormatOne.getQuoteMode());

        assertTrue(cSVFormatOne.getIgnoreEmptyLines());
        assertFalse(cSVFormatOne.getSkipHeaderRecord());

        assertFalse(cSVFormatOne.getIgnoreHeaderCase());
        assertNull(cSVFormatOne.getCommentMarker());

        assertFalse(cSVFormatOne.isCommentMarkerSet());
        assertTrue(cSVFormatOne.isQuoteCharacterSet());

        assertEquals('|', cSVFormatOne.getDelimiter());
        assertFalse(cSVFormatOne.getAllowMissingColumnNames());

        assertTrue(cSVFormatOne.isEscapeCharacterSet());
        assertEquals("\n", cSVFormatOne.getRecordSeparator());

        assertEquals('\"', (char)cSVFormatOne.getQuoteCharacter());
        assertFalse(cSVFormatOne.getTrailingDelimiter());

        assertFalse(cSVFormatOne.getTrim());
        assertFalse(cSVFormatOne.isNullStringSet());

        assertNull(cSVFormatOne.getNullString());
        assertFalse(cSVFormatOne.getIgnoreSurroundingSpaces());

        assertTrue(cSVFormatTwo.isEscapeCharacterSet());
        assertNull(cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormatTwo.getQuoteMode());

        assertEquals('\t', cSVFormatTwo.getDelimiter());
        assertEquals("\n", cSVFormatTwo.getRecordSeparator());

        assertFalse(cSVFormatTwo.isQuoteCharacterSet());
        assertTrue(cSVFormatTwo.isNullStringSet());

        assertEquals('\\', (char)cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertFalse(cSVFormatTwo.getIgnoreEmptyLines());

        assertEquals("\\N", cSVFormatTwo.getNullString());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormatTwo.getTrailingDelimiter());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormatOne, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormatOne);

        assertFalse(cSVFormatOne.equals(cSVFormatTwo));
        assertFalse(cSVFormatTwo.equals(cSVFormatOne));

        assertFalse(cSVFormatTwo.equals(cSVFormatOne));

    }

    @Test
    public void testEqualsQuoteChar() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuote('"');
        final CSVFormat left = right.withQuote('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsQuotePolicy() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withQuoteMode(QuoteMode.MINIMAL);

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsRecordSeparator() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator(CR)
                .withCommentMarker('#')
                .withEscape('+')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right
                .withRecordSeparator(LF);

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsSkipHeaderRecord() {
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
    public void testEqualsWithNull() {

        final CSVFormat cSVFormat = CSVFormat.POSTGRESQL_TEXT;

        assertEquals('\"', (char)cSVFormat.getEscapeCharacter());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertFalse(cSVFormat.getTrim());

        assertTrue(cSVFormat.isQuoteCharacterSet());
        assertEquals("\\N", cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertTrue(cSVFormat.isEscapeCharacterSet());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormat.getQuoteMode());

        assertEquals('\t', cSVFormat.getDelimiter());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertEquals("\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertTrue(cSVFormat.isNullStringSet());

        assertEquals('\"', (char)cSVFormat.getEscapeCharacter());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertFalse(cSVFormat.getTrim());

        assertTrue(cSVFormat.isQuoteCharacterSet());
        assertEquals("\\N", cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertTrue(cSVFormat.isEscapeCharacterSet());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, cSVFormat.getQuoteMode());

        assertEquals('\t', cSVFormat.getDelimiter());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertEquals("\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertTrue(cSVFormat.isNullStringSet());

        assertFalse(cSVFormat.equals( null));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        CSVFormat.DEFAULT.withEscape(new Character('!')).withCommentMarker(new Character('!'));
    }

    @Test
    public void testFormat() {
        final CSVFormat format = CSVFormat.DEFAULT;

        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }

    @Test  //I assume this to be a defect.
    public void testFormatThrowsNullPointerException() {

        final CSVFormat cSVFormat = CSVFormat.MYSQL;

        try {
            cSVFormat.format(null);
            fail("Expecting exception: NullPointerException");
        } catch(final NullPointerException e) {
            assertEquals(CSVFormat.class.getName(), e.getStackTrace()[0].getClassName());
        }

    }

    @Test
    public void testGetHeader() throws Exception {
        final String[] header = new String[]{"one", "two", "three"};
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
    public void testHashCodeAndWithIgnoreHeaderCase() {

        final CSVFormat cSVFormat = CSVFormat.INFORMIX_UNLOAD_CSV;
        final CSVFormat cSVFormatTwo = cSVFormat.withIgnoreHeaderCase();
        cSVFormatTwo.hashCode();

        assertTrue(cSVFormatTwo.getIgnoreHeaderCase());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertTrue(cSVFormatTwo.equals(cSVFormat));
        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());

        assertFalse(cSVFormatTwo.getTrim());

    }

    @Test
    public void testNewFormat() {

        final CSVFormat cSVFormat = CSVFormat.newFormat('X');

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertFalse(cSVFormat.isEscapeCharacterSet());

        assertNull(cSVFormat.getRecordSeparator());
        assertNull(cSVFormat.getQuoteMode());

        assertNull(cSVFormat.getCommentMarker());
        assertFalse(cSVFormat.getIgnoreHeaderCase());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertEquals('X', cSVFormat.getDelimiter());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.isQuoteCharacterSet());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertNull(cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertFalse(cSVFormat.isEscapeCharacterSet());

        assertNull(cSVFormat.getRecordSeparator());
        assertNull(cSVFormat.getQuoteMode());

        assertNull(cSVFormat.getCommentMarker());
        assertFalse(cSVFormat.getIgnoreHeaderCase());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertEquals('X', cSVFormat.getDelimiter());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.isQuoteCharacterSet());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertNull(cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.getIgnoreEmptyLines());

    }

    @Test
    public void testNullRecordSeparatorCsv106() {
        final CSVFormat format = CSVFormat.newFormat(';').withSkipHeaderRecord().withHeader("H1", "H2");
        final String formatStr = format.format("A", "B");
        assertNotNull(formatStr);
        assertFalse(formatStr.endsWith("null"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        CSVFormat.DEFAULT.withQuote(new Character('!')).withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharSameAsDelimiterThrowsException() {
        CSVFormat.DEFAULT.withQuote('!').withDelimiter('!');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuotePolicyNoneWithoutEscapeThrowsException() {
        CSVFormat.newFormat('!').withQuoteMode(QuoteMode.NONE);
    }

    @Test
    public void testRFC4180() {
        assertEquals(null, RFC4180.getCommentMarker());
        assertEquals(',', RFC4180.getDelimiter());
        assertEquals(null, RFC4180.getEscapeCharacter());
        assertFalse(RFC4180.getIgnoreEmptyLines());
        assertEquals(Character.valueOf('"'), RFC4180.getQuoteCharacter());
        assertEquals(null, RFC4180.getQuoteMode());
        assertEquals("\r\n", RFC4180.getRecordSeparator());
    }

    @SuppressWarnings("boxing") // no need to worry about boxing here
    @Test
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (final ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(CSVFormat.DEFAULT);
            oos.flush();
        }

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        final CSVFormat format = (CSVFormat) in.readObject();

        assertNotNull(format);
        assertEquals("delimiter", CSVFormat.DEFAULT.getDelimiter(), format.getDelimiter());
        assertEquals("encapsulator", CSVFormat.DEFAULT.getQuoteCharacter(), format.getQuoteCharacter());
        assertEquals("comment start", CSVFormat.DEFAULT.getCommentMarker(), format.getCommentMarker());
        assertEquals("record separator", CSVFormat.DEFAULT.getRecordSeparator(), format.getRecordSeparator());
        assertEquals("escape", CSVFormat.DEFAULT.getEscapeCharacter(), format.getEscapeCharacter());
        assertEquals("trim", CSVFormat.DEFAULT.getIgnoreSurroundingSpaces(), format.getIgnoreSurroundingSpaces());
        assertEquals("empty lines", CSVFormat.DEFAULT.getIgnoreEmptyLines(), format.getIgnoreEmptyLines());
    }

    @Test
    public void testToString() {

        final CSVFormat cSVFormat = CSVFormat.POSTGRESQL_TEXT;
        final String string = CSVFormat.INFORMIX_UNLOAD.toString();

        assertEquals("Delimiter=<|> Escape=<\\> QuoteChar=<\"> RecordSeparator=<\n> EmptyLines:ignored SkipHeaderRecord:false", string);

    }

    @Test
    public void testToStringAndWithCommentMarkerTakingCharacter() {

        final CSVFormat.Predefined cSVFormat_Predefined = CSVFormat.Predefined.Default;
        final CSVFormat cSVFormat = cSVFormat_Predefined.getFormat();

        assertNull(cSVFormat.getEscapeCharacter());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getTrim());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertEquals(',', cSVFormat.getDelimiter());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", cSVFormat.getRecordSeparator());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.isNullStringSet());
        assertFalse(cSVFormat.getAllowMissingColumnNames());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertNull(cSVFormat.getNullString());
        assertNull(cSVFormat.getQuoteMode());

        assertTrue(cSVFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());

        final Character character = Character.valueOf('n');

        final CSVFormat cSVFormatTwo = cSVFormat.withCommentMarker(character);

        assertNull(cSVFormat.getEscapeCharacter());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getTrim());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertEquals(',', cSVFormat.getDelimiter());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", cSVFormat.getRecordSeparator());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.isNullStringSet());
        assertFalse(cSVFormat.getAllowMissingColumnNames());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertNull(cSVFormat.getNullString());
        assertNull(cSVFormat.getQuoteMode());

        assertTrue(cSVFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());

        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());
        assertNull(cSVFormatTwo.getNullString());

        assertEquals(',', cSVFormatTwo.getDelimiter());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertTrue(cSVFormatTwo.isCommentMarkerSet());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertNull(cSVFormatTwo.getEscapeCharacter());

        assertTrue(cSVFormatTwo.isQuoteCharacterSet());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertEquals('n', (char)cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertFalse(cSVFormatTwo.equals(cSVFormat));

        assertNull(cSVFormat.getEscapeCharacter());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getTrim());
        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());

        assertFalse(cSVFormat.getTrailingDelimiter());
        assertEquals(',', cSVFormat.getDelimiter());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", cSVFormat.getRecordSeparator());

        assertFalse(cSVFormat.isCommentMarkerSet());
        assertNull(cSVFormat.getCommentMarker());

        assertFalse(cSVFormat.isNullStringSet());
        assertFalse(cSVFormat.getAllowMissingColumnNames());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertFalse(cSVFormat.getSkipHeaderRecord());

        assertNull(cSVFormat.getNullString());
        assertNull(cSVFormat.getQuoteMode());

        assertTrue(cSVFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());

        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());
        assertNull(cSVFormatTwo.getNullString());

        assertEquals(',', cSVFormatTwo.getDelimiter());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertTrue(cSVFormatTwo.isCommentMarkerSet());
        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());

        assertFalse(cSVFormatTwo.getTrim());
        assertNull(cSVFormatTwo.getEscapeCharacter());

        assertTrue(cSVFormatTwo.isQuoteCharacterSet());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertEquals('n', (char)cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.getSkipHeaderRecord());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertFalse(cSVFormat.equals(cSVFormatTwo));

        assertFalse(cSVFormatTwo.equals(cSVFormat));
        assertEquals("Delimiter=<,> QuoteChar=<\"> CommentStart=<n> " +
                        "RecordSeparator=<\r\n> EmptyLines:ignored SkipHeaderRecord:false"
                , cSVFormatTwo.toString());

    }

    @Test
    public void testWithCommentStart() throws Exception {
        final CSVFormat formatWithCommentStart = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals( Character.valueOf('#'), formatWithCommentStart.getCommentMarker());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStartCRThrowsException() {
        CSVFormat.DEFAULT.withCommentMarker(CR);
    }

    @Test
    public void testWithDelimiter() throws Exception {
        final CSVFormat formatWithDelimiter = CSVFormat.DEFAULT.withDelimiter('!');
        assertEquals('!', formatWithDelimiter.getDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLFThrowsException() {
        CSVFormat.DEFAULT.withDelimiter(LF);
    }

    @Test
    public void testWithEmptyEnum() throws Exception {
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(EmptyEnum.class);
        Assert.assertTrue(formatWithHeader.getHeader().length == 0);
    }

    @Test
    public void testWithEscape() throws Exception {
        final CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('&');
        assertEquals(Character.valueOf('&'), formatWithEscape.getEscapeCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCRThrowsExceptions() {
        CSVFormat.DEFAULT.withEscape(CR);
    }

    @Test
    public void testWithFirstRecordAsHeader() throws Exception {
        final CSVFormat formatWithFirstRecordAsHeader = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(formatWithFirstRecordAsHeader.getSkipHeaderRecord());
        assertTrue(formatWithFirstRecordAsHeader.getHeader().length == 0);
    }

    @Test
    public void testWithHeader() throws Exception {
        final String[] header = new String[]{"one", "two", "three"};
        // withHeader() makes a copy of the header array.
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, formatWithHeader.getHeader());
        assertNotSame(header, formatWithHeader.getHeader());
    }

    @Test
    public void testWithHeaderComments() {

        final CSVFormat cSVFormat = CSVFormat.DEFAULT;

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertNull(cSVFormat.getQuoteMode());

        assertEquals(',', cSVFormat.getDelimiter());
        assertTrue(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertNull(cSVFormat.getCommentMarker());

        assertEquals("\r\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertNull(cSVFormat.getEscapeCharacter());

        final Object[] objectArray = new Object[8];
        final CSVFormat cSVFormatTwo = cSVFormat.withHeaderComments(objectArray);

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertNull(cSVFormat.getQuoteMode());

        assertEquals(',', cSVFormat.getDelimiter());
        assertTrue(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertNull(cSVFormat.getCommentMarker());

        assertEquals("\r\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertNull(cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getTrim());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.isQuoteCharacterSet());

        assertFalse(cSVFormatTwo.getSkipHeaderRecord());
        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertNull(cSVFormatTwo.getNullString());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertEquals(',', cSVFormatTwo.getDelimiter());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertTrue(cSVFormatTwo.equals(cSVFormat));

        final String string = cSVFormatTwo.format(objectArray);

        assertEquals('\"', (char)cSVFormat.getQuoteCharacter());
        assertFalse(cSVFormat.isCommentMarkerSet());

        assertFalse(cSVFormat.isEscapeCharacterSet());
        assertTrue(cSVFormat.isQuoteCharacterSet());

        assertFalse(cSVFormat.getSkipHeaderRecord());
        assertNull(cSVFormat.getQuoteMode());

        assertEquals(',', cSVFormat.getDelimiter());
        assertTrue(cSVFormat.getIgnoreEmptyLines());

        assertFalse(cSVFormat.getIgnoreHeaderCase());
        assertNull(cSVFormat.getCommentMarker());

        assertEquals("\r\n", cSVFormat.getRecordSeparator());
        assertFalse(cSVFormat.getTrailingDelimiter());

        assertFalse(cSVFormat.getAllowMissingColumnNames());
        assertFalse(cSVFormat.getTrim());

        assertFalse(cSVFormat.isNullStringSet());
        assertNull(cSVFormat.getNullString());

        assertFalse(cSVFormat.getIgnoreSurroundingSpaces());
        assertNull(cSVFormat.getEscapeCharacter());

        assertFalse(cSVFormatTwo.getIgnoreHeaderCase());
        assertNull(cSVFormatTwo.getQuoteMode());

        assertTrue(cSVFormatTwo.getIgnoreEmptyLines());
        assertFalse(cSVFormatTwo.getIgnoreSurroundingSpaces());

        assertNull(cSVFormatTwo.getEscapeCharacter());
        assertFalse(cSVFormatTwo.getTrim());

        assertFalse(cSVFormatTwo.isEscapeCharacterSet());
        assertTrue(cSVFormatTwo.isQuoteCharacterSet());

        assertFalse(cSVFormatTwo.getSkipHeaderRecord());
        assertEquals('\"', (char)cSVFormatTwo.getQuoteCharacter());

        assertFalse(cSVFormatTwo.getAllowMissingColumnNames());
        assertNull(cSVFormatTwo.getNullString());

        assertFalse(cSVFormatTwo.isNullStringSet());
        assertFalse(cSVFormatTwo.getTrailingDelimiter());

        assertEquals("\r\n", cSVFormatTwo.getRecordSeparator());
        assertEquals(',', cSVFormatTwo.getDelimiter());

        assertNull(cSVFormatTwo.getCommentMarker());
        assertFalse(cSVFormatTwo.isCommentMarkerSet());

        assertNotSame(cSVFormat, cSVFormatTwo);
        assertNotSame(cSVFormatTwo, cSVFormat);

        assertNotNull(string);
        assertTrue(cSVFormat.equals(cSVFormatTwo));

        assertTrue(cSVFormatTwo.equals(cSVFormat));
        assertEquals(",,,,,,,", string);

    }

    @Test
    public void testWithHeaderEnum() throws Exception {
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(Header.class);
        assertArrayEquals(new String[]{ "Name", "Email", "Phone" }, formatWithHeader.getHeader());
    }


    @Test
    public void testWithIgnoreEmptyLines() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
        assertTrue(CSVFormat.DEFAULT.withIgnoreEmptyLines().getIgnoreEmptyLines());
    }


    @Test
    public void testWithIgnoreSurround() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false).getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces().getIgnoreSurroundingSpaces());
    }


    @Test
    public void testWithNullString() throws Exception {
        final CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("null");
        assertEquals("null", formatWithNullString.getNullString());
    }


    @Test
    public void testWithQuoteChar() throws Exception {
        final CSVFormat formatWithQuoteChar = CSVFormat.DEFAULT.withQuote('"');
        assertEquals(Character.valueOf('"'), formatWithQuoteChar.getQuoteCharacter());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLFThrowsException() {
        CSVFormat.DEFAULT.withQuote(LF);
    }


    @Test
    public void testWithQuotePolicy() throws Exception {
        final CSVFormat formatWithQuotePolicy = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, formatWithQuotePolicy.getQuoteMode());
    }


    @Test
    public void testWithRecordSeparatorCR() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(CR);
        assertEquals(String.valueOf(CR), formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    public void testWithRecordSeparatorCRLF() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(CRLF);
        assertEquals(CRLF, formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    public void testWithRecordSeparatorLF() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(LF);
        assertEquals(String.valueOf(LF), formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    public void testWithSystemRecordSeparator() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withSystemRecordSeparator();
        assertEquals(System.getProperty("line.separator"), formatWithRecordSeparator.getRecordSeparator());
    }

}
