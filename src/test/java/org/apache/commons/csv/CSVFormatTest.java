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

import static org.apache.commons.csv.CSVFormat.RFC4180;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.LF;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 *
 *
 * @version $Id$
 */
public class CSVFormatTest {

    @Test
    public void testFormat() {
        final CSVFormat format = CSVFormat.DEFAULT;

        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
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
        assertEquals("encapsulator", CSVFormat.DEFAULT.getQuoteChar(), format.getQuoteChar());
        assertEquals("comment start", CSVFormat.DEFAULT.getCommentStart(), format.getCommentStart());
        assertEquals("line separator", CSVFormat.DEFAULT.getRecordSeparator(), format.getRecordSeparator());
        assertEquals("escape", CSVFormat.DEFAULT.getEscape(), format.getEscape());
        assertEquals("trim", CSVFormat.DEFAULT.getIgnoreSurroundingSpaces(), format.getIgnoreSurroundingSpaces());
        assertEquals("empty lines", CSVFormat.DEFAULT.getIgnoreEmptyLines(), format.getIgnoreEmptyLines());
    }

    @Test
    public void testEquals() {
        final CSVFormat right = CSVFormat.DEFAULT;
        final CSVFormat left = CSVFormat.copy(right);

        assertFalse(right.equals(null));
        assertFalse(right.equals("A String Instance"));

        assertEquals(right, right);
        assertEquals(right, left);
        assertEquals(left, right);

        assertEquals(right.hashCode(), right.hashCode());
        assertEquals(right.hashCode(), left.hashCode());
    }

    @Test
    public void testEqualsDelimiter() {
        final CSVFormat right = CSVFormat.newFormat('!');
        final CSVFormat left = CSVFormat.newFormat('?');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsQuoteChar() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuoteChar('"');
        final CSVFormat left = CSVFormat.copy(right).withQuoteChar('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsQuotePolicy() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = CSVFormat.copy(right)
                .withQuotePolicy(Quote.MINIMAL);

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsCommentStart() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuoteChar('"')
                .withCommentStart('#')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = CSVFormat.copy(right)
                .withCommentStart('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsEscape() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withQuoteChar('"')
                .withCommentStart('#')
                .withEscape('+')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = CSVFormat.copy(right)
                .withEscape('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsIgnoreSurroundingSpaces() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = CSVFormat.copy(right)
                .withIgnoreSurroundingSpaces(false);

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsIgnoreEmptyLines() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = CSVFormat.copy(right)
                .withIgnoreEmptyLines(false);

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsRecordSeparator() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator('*')
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = CSVFormat.copy(right)
                .withRecordSeparator('!');

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsHeader() {
        final CSVFormat right = CSVFormat.newFormat('\'')
                .withRecordSeparator('*')
                .withCommentStart('#')
                .withEscape('+')
                .withHeader("One", "Two", "Three")
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL);
        final CSVFormat left = CSVFormat.copy(right)
                .withHeader("Three", "Two", "One");

        assertNotEquals(right, left);
    }

    @Test
    public void testWithCommentStart() throws Exception {
        CSVFormat formatWithCommentStart = CSVFormat.DEFAULT.withCommentStart('#');
        assertEquals( Character.valueOf('#'), formatWithCommentStart.getCommentStart());
    }

    @Test
    public void testWithDelimiter() throws Exception {
        CSVFormat formatWithDelimiter = CSVFormat.DEFAULT.withDelimiter('!');
        assertEquals('!', formatWithDelimiter.getDelimiter());
    }

    @Test
    public void testWithEscape() throws Exception {
        CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('&');
        assertEquals(Character.valueOf('&'), formatWithEscape.getEscape());
    }

    @Test
    public void testWithHeader() throws Exception {
        String[] header = new String[]{"one", "two", "three"};
        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, formatWithHeader.getHeader());
        assertNotSame(header, formatWithHeader.getHeader());
    }

    @Test
    public void testWithIgnoreEmptyLines() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
        assertTrue(CSVFormat.DEFAULT.withIgnoreEmptyLines(true).getIgnoreEmptyLines());
    }

    @Test
    public void testWithIgnoreSurround() throws Exception {
        assertFalse(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false).getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true).getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithNullString() throws Exception {
        CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("null");
        assertEquals("null", formatWithNullString.getNullString());
    }

    @Test
    public void testWithQuoteChar() throws Exception {
        CSVFormat formatWithQuoteChar = CSVFormat.DEFAULT.withQuoteChar('"');
        assertEquals(Character.valueOf('"'), formatWithQuoteChar.getQuoteChar());
    }

    @Test
    public void testWithQuotePolicy() throws Exception {
        CSVFormat formatWithQuotePolicy = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        assertEquals(Quote.ALL, formatWithQuotePolicy.getQuotePolicy());
    }

    @Test
    public void testWithRecordSeparator() throws Exception {
        CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator('!');
        assertEquals("!", formatWithRecordSeparator.getRecordSeparator());
    }

    @Test(expected = IllegalStateException.class)
    public void testDelimiterSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withCommentStart('!').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testDelimiterSameAsEscapeThrowsException() {
        CSVFormat.DEFAULT.withDelimiter('!').withEscape('!').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testEscapeSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withEscape('!').withCommentStart('!').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testEscapeSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        CSVFormat.DEFAULT.withEscape(new Character('!')).withCommentStart(new Character('!')).validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testQuoteCharSameAsCommentStartThrowsException() {
        CSVFormat.DEFAULT.withQuoteChar('!').withCommentStart('!').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        CSVFormat.DEFAULT.withQuoteChar(new Character('!')).withCommentStart('!').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testQuoteCharSameAsDelimiterThrowsException() {
        CSVFormat.DEFAULT.withQuoteChar('!').withDelimiter('!').validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testQuotePolicyNoneWithoutEscapeThrowsException() {
        CSVFormat.newFormat('!').withQuotePolicy(Quote.NONE).validate();
    }

    @Test
    public void testRFC4180() {
        assertEquals(null, RFC4180.getCommentStart());
        assertEquals(',', RFC4180.getDelimiter());
        assertEquals(null, RFC4180.getEscape());
        assertFalse(RFC4180.getIgnoreEmptyLines());
        assertEquals(Character.valueOf('"'), RFC4180.getQuoteChar());
        assertEquals(null, RFC4180.getQuotePolicy());
        assertEquals("\r\n", RFC4180.getRecordSeparator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStartCRThrowsException() {
        CSVFormat.DEFAULT.withCommentStart(CR).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLFThrowsException() {
        CSVFormat.DEFAULT.withDelimiter(LF).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCRThrowsExceptions() {
        CSVFormat.DEFAULT.withEscape(CR).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLFThrowsException() {
        CSVFormat.DEFAULT.withQuoteChar(LF).validate();
    }

    private static void assertNotEquals(final Object right, final Object left) {
        assertFalse(right.equals(left));
        assertFalse(left.equals(right));
    }
}
