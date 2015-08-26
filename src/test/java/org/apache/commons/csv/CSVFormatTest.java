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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Properties;

import static org.apache.commons.csv.CSVFormat.RFC4180;
import static org.apache.commons.csv.Constants.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 *
 * @version $Id$
 */
public class CSVFormatTest {

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

        final ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(CSVFormat.DEFAULT);
        oos.flush();
        oos.close();

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
    public void testWithEscape() throws Exception {
        final CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('&');
        assertEquals(Character.valueOf('&'), formatWithEscape.getEscapeCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCRThrowsExceptions() {
        CSVFormat.DEFAULT.withEscape(CR);
    }

    @Test
    public void testWithHeader() throws Exception {
        final String[] header = new String[]{"one", "two", "three"};
        // withHeader() makes a copy of the header array.
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, formatWithHeader.getHeader());
        assertNotSame(header, formatWithHeader.getHeader());
        header[0] = "A";
        header[1] = "B";
        header[2] = "C";
        assertFalse(Arrays.equals(formatWithHeader.getHeader(), header));
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
    public void testWithRecordSeparatorLF() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(LF);
        assertEquals(String.valueOf(LF), formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    public void testWithRecordSeparatorCRLF() throws Exception {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(CRLF);
        assertEquals(CRLF, formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    public void shouldReadDelimiterFromProperties() throws Exception {
        char expectedCharacter = ']';
        Properties properties = new Properties();
        properties.setProperty("org.apache.commons.csv.format.delimiter", String.valueOf(expectedCharacter));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getDelimiter(), is(equalTo(expectedCharacter)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getDelimiter(), is(equalTo(expectedCharacter)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailCreationFromPropertiesForMissingDelimiter() throws Exception {
        CSVFormat format = CSVFormat.from(new Properties());
    }

    @Test
    public void shouldReadQuoteCharacterFromProperties() throws Exception {
        char expectedCharacter = ']';
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.quoteCharacter", String.valueOf(expectedCharacter));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getQuoteCharacter(), is(equalTo(expectedCharacter)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getQuoteCharacter(), is(equalTo(expectedCharacter)));
    }

    @Test
    public void shouldReadCommentMarkerFromProperties() throws Exception {
        char expectedCharacter = ']';
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.commentMarker", String.valueOf(expectedCharacter));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getCommentMarker(), is(equalTo(expectedCharacter)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getCommentMarker(), is(equalTo(expectedCharacter)));
    }

    @Test
    public void shouldReadEscapeFromProperties() throws Exception {
        char expectedCharacter = '\t';
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.escape", String.valueOf(expectedCharacter));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getEscapeCharacter(), is(equalTo(expectedCharacter)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getEscapeCharacter(), is(equalTo(expectedCharacter)));
    }

    @Test
    public void shouldReadIgnoreSurroundingSpacesFromProperties() throws Exception {
        boolean expectedValue = true;
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.ignoreSurroundingSpaces", String.valueOf(expectedValue));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getIgnoreSurroundingSpaces(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getIgnoreSurroundingSpaces(), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldReadAllowMissingColumnNamesFromProperties() throws Exception {
        boolean expectedValue = true;
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.allowMissingColumnNames", String.valueOf(expectedValue));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getAllowMissingColumnNames(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getAllowMissingColumnNames(), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldReadIgnoreEmptyLinesNamesFromProperties() throws Exception {
        boolean expectedValue = true;
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.ignoreEmptyLines", String.valueOf(expectedValue));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getIgnoreEmptyLines(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getIgnoreEmptyLines(), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldReadRecordSeparatorFromProperties() throws Exception {
        String expectedSeparator = "\n";
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.recordSeparator", expectedSeparator);

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getRecordSeparator(), is(equalTo(expectedSeparator)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getRecordSeparator(), is(equalTo(expectedSeparator)));
    }

    @Test
    public void shouldReadNullStringFromProperties() throws Exception {
        String expectedValue = "<null>";
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.nullString", expectedValue);

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getNullString(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getNullString(), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldReadHeaderCommentsFromProperties() throws Exception {
        Object[] expectedValue = {
                "This",
                "is",
                "a",
                "comment"
        };
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.headerComments", asString(expectedValue));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getHeaderComments(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getHeaderComments(), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldReadHeaderFromProperties() throws Exception {
        Object[] expectedValue = {
                "This",
                "is",
                "a",
                "header"
        };
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.header", asString(expectedValue));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getHeader(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getHeader(), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldReadSkipHeaderRecordFromProperties() throws Exception {
        boolean expectedValue = true;
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.skipHeaderRecord", String.valueOf(expectedValue));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getSkipHeaderRecord(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getSkipHeaderRecord(), is(equalTo(expectedValue)));
    }

    @Test
    public void shouldReadQuoteModeFromProperties() throws Exception {
        QuoteMode expectedValue = QuoteMode.MINIMAL;
        Properties properties = propertiesWithDefaultDelimiter();
        properties.setProperty("org.apache.commons.csv.format.quoteMode", String.valueOf(expectedValue));

        CSVFormat format = CSVFormat.from(properties);

        assertThat(format.getQuoteMode(), is(equalTo(expectedValue)));

        format = CSVFormat.DEFAULT.withProperties(properties);
        assertThat(format.getQuoteMode(), is(equalTo(expectedValue)));
    }

    Properties propertiesWithDefaultDelimiter() {
        Properties properties = new Properties();
        properties.setProperty("org.apache.commons.csv.format.delimiter", String.valueOf(CSVFormat.DEFAULT.getDelimiter()));
        return properties;
    }

    String asString(Object[] array) {
        StringBuilder buffer = new StringBuilder();
        for (Object o : array) {
            buffer.append(String.valueOf(o))
            .append(",");
        }
        return buffer.substring(0, buffer.length()-1);
    }
}
