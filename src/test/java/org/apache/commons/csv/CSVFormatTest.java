/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.csv;

import static org.apache.commons.csv.CSVFormat.RFC4180;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.LF;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat.Builder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CSVFormat}.
 */
class CSVFormatTest {

    public enum EmptyEnum {
        // empty enum.
    }

    public enum Header {
        Name, Email, Phone
    }

    private static void assertNotEquals(final Object right, final Object left) {
        Assertions.assertNotEquals(right, left);
        Assertions.assertNotEquals(left, right);
    }

    private static CSVFormat copy(final CSVFormat format) {
        return format.builder().setDelimiter(format.getDelimiter()).get();
    }

    private void assertNotEquals(final String name, final String type, final Object left, final Object right) {
        if (left.equals(right) || right.equals(left)) {
            fail("Objects must not compare equal for " + name + "(" + type + ")");
        }
        if (left.hashCode() == right.hashCode()) {
            fail("Hash code should not be equal for " + name + "(" + type + ")");
        }
    }

    @Test
    void testBuildVsGet() {
        final Builder builder = CSVFormat.DEFAULT.builder();
        assertNotSame(builder.get(), builder.build());
    }

    @Test
    void testDelimiterCharLineBreakCrThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter(Constants.CR).get());
    }

    @Test
    void testDelimiterCharLineBreakLfThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter(Constants.LF).get());
    }

    @Test
    void testDelimiterEmptyStringThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter("").get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDelimiterSameAsCommentStartThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withDelimiter('!').withCommentMarker('!'));
    }

    @Test
    void testDelimiterSameAsCommentStartThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter('!').setCommentMarker('!').get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDelimiterSameAsEscapeThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withDelimiter('!').withEscape('!'));
    }

    @Test
    void testDelimiterSameAsEscapeThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter('!').setEscape('!').get());
    }

    @Test
    void testDelimiterSameAsRecordSeparatorThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.newFormat(CR));
    }

    @Test
    void testDelimiterStringLineBreakCrThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter(String.valueOf(Constants.CR)).get());
    }

    @Test
    void testDelimiterStringLineBreakLfThrowsException1() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setDelimiter(String.valueOf(Constants.LF)).get());
    }

    @Test
    void testDuplicateHeaderElements() {
        final String[] header = { "A", "A" };
        final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(header).get();
        assertEquals(2, format.getHeader().length);
        assertArrayEquals(header, format.getHeader());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDuplicateHeaderElements_Deprecated() {
        final String[] header = { "A", "A" };
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        assertEquals(2, format.getHeader().length);
        assertArrayEquals(header, format.getHeader());
    }

    @Test
    void testDuplicateHeaderElementsFalse() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(false).setHeader("A", "A").get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDuplicateHeaderElementsFalse_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withAllowDuplicateHeaderNames(false).withHeader("A", "A"));
    }

    @Test
    void testDuplicateHeaderElementsTrue() {
        CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(true).setHeader("A", "A").get();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDuplicateHeaderElementsTrue_Deprecated() {
        CSVFormat.DEFAULT.withAllowDuplicateHeaderNames(true).withHeader("A", "A");
    }

    @Test
    void testDuplicateHeaderElementsTrueContainsEmpty1() {
        CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(false).setHeader("A", "", "B", "").get();
    }

    @Test
    void testDuplicateHeaderElementsTrueContainsEmpty2() {
        CSVFormat.DEFAULT.builder().setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_EMPTY).setHeader("A", "", "B", "").get();
    }

    @Test
    void testDuplicateHeaderElementsTrueContainsEmpty3() {
        CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(false).setAllowMissingColumnNames(true).setHeader("A", "", "B", "").get();
    }

    @Test
    void testEquals() {
        final CSVFormat right = CSVFormat.DEFAULT;
        final CSVFormat left = copy(right);
        Assertions.assertNotEquals(null, right);
        Assertions.assertNotEquals("A String Instance", right);
        assertEquals(right, right);
        assertEquals(right, left);
        assertEquals(left, right);
        assertEquals(right.hashCode(), right.hashCode());
        assertEquals(right.hashCode(), left.hashCode());
    }

    @Test
    void testEqualsCommentStart() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setQuote('"').setCommentMarker('#').setQuoteMode(QuoteMode.ALL).get();
        final CSVFormat left = right.builder().setCommentMarker('!').get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsCommentStart_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuote('"').withCommentMarker('#').withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right.withCommentMarker('!');

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsDelimiter() {
        final CSVFormat right = CSVFormat.newFormat('!');
        final CSVFormat left = CSVFormat.newFormat('?');

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsEscape() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setQuote('"').setCommentMarker('#').setEscape('+').setQuoteMode(QuoteMode.ALL).get();
        final CSVFormat left = right.builder().setEscape('!').get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsEscape_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuote('"').withCommentMarker('#').withEscape('+').withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right.withEscape('!');

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsHash() throws Exception {
        final Method[] methods = CSVFormat.class.getDeclaredMethods();
        for (final Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                final String name = method.getName();
                if (name.startsWith("with")) {
                    for (final Class<?> cls : method.getParameterTypes()) {
                        final String type = cls.getCanonicalName();
                        switch (type) {
                        case "boolean": {
                            final Object defTrue = method.invoke(CSVFormat.DEFAULT, Boolean.TRUE);
                            final Object defFalse = method.invoke(CSVFormat.DEFAULT, Boolean.FALSE);
                            assertNotEquals(name, type, defTrue, defFalse);
                            break;
                        }
                        case "char": {
                            final Object a = method.invoke(CSVFormat.DEFAULT, 'a');
                            final Object b = method.invoke(CSVFormat.DEFAULT, 'b');
                            assertNotEquals(name, type, a, b);
                            break;
                        }
                        case "java.lang.Character": {
                            final Object a = method.invoke(CSVFormat.DEFAULT, new Object[] { null });
                            final Object b = method.invoke(CSVFormat.DEFAULT, Character.valueOf('d'));
                            assertNotEquals(name, type, a, b);
                            break;
                        }
                        case "java.lang.String": {
                            final Object a = method.invoke(CSVFormat.DEFAULT, new Object[] { null });
                            final Object b = method.invoke(CSVFormat.DEFAULT, "e");
                            assertNotEquals(name, type, a, b);
                            break;
                        }
                        case "java.lang.String[]": {
                            final Object a = method.invoke(CSVFormat.DEFAULT, new Object[] { new String[] { null, null } });
                            final Object b = method.invoke(CSVFormat.DEFAULT, new Object[] { new String[] { "f", "g" } });
                            assertNotEquals(name, type, a, b);
                            break;
                        }
                        case "org.apache.commons.csv.QuoteMode": {
                            final Object a = method.invoke(CSVFormat.DEFAULT, QuoteMode.MINIMAL);
                            final Object b = method.invoke(CSVFormat.DEFAULT, QuoteMode.ALL);
                            assertNotEquals(name, type, a, b);
                            break;
                        }
                        case "org.apache.commons.csv.DuplicateHeaderMode": {
                            final Object a = method.invoke(CSVFormat.DEFAULT, DuplicateHeaderMode.ALLOW_ALL);
                            final Object b = method.invoke(CSVFormat.DEFAULT, DuplicateHeaderMode.DISALLOW);
                            assertNotEquals(name, type, a, b);
                            break;
                        }
                        case "java.lang.Object[]": {
                            final Object a = method.invoke(CSVFormat.DEFAULT, new Object[] { new Object[] { null, null } });
                            final Object b = method.invoke(CSVFormat.DEFAULT, new Object[] { new Object[] { new Object(), new Object() } });
                            assertNotEquals(name, type, a, b);
                            break;
                        }
                        default:
                            if ("withHeader".equals(name)) { // covered above by String[]
                                // ignored
                            } else {
                                fail("Unhandled method: " + name + "(" + type + ")");
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @Test
    void testEqualsHeader() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setRecordSeparator(CR).setCommentMarker('#').setEscape('+').setHeader("One", "Two", "Three")
                .setIgnoreEmptyLines(true).setIgnoreSurroundingSpaces(true).setQuote('"').setQuoteMode(QuoteMode.ALL).get();
        final CSVFormat left = right.builder().setHeader("Three", "Two", "One").get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsHeader_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withRecordSeparator(CR).withCommentMarker('#').withEscape('+').withHeader("One", "Two", "Three")
                .withIgnoreEmptyLines().withIgnoreSurroundingSpaces().withQuote('"').withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right.withHeader("Three", "Two", "One");

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsIgnoreEmptyLines() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setCommentMarker('#').setEscape('+').setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true).setQuote('"').setQuoteMode(QuoteMode.ALL).get();
        final CSVFormat left = right.builder().setIgnoreEmptyLines(false).get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsIgnoreEmptyLines_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withCommentMarker('#').withEscape('+').withIgnoreEmptyLines().withIgnoreSurroundingSpaces()
                .withQuote('"').withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right.withIgnoreEmptyLines(false);

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsIgnoreSurroundingSpaces() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setCommentMarker('#').setEscape('+').setIgnoreSurroundingSpaces(true).setQuote('"')
                .setQuoteMode(QuoteMode.ALL).get();
        final CSVFormat left = right.builder().setIgnoreSurroundingSpaces(false).get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsIgnoreSurroundingSpaces_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withCommentMarker('#').withEscape('+').withIgnoreSurroundingSpaces().withQuote('"')
                .withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right.withIgnoreSurroundingSpaces(false);

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsLeftNoQuoteRightQuote() {
        final CSVFormat left = CSVFormat.newFormat(',').builder().setQuote(null).get();
        final CSVFormat right = left.builder().setQuote('#').get();

        assertNotEquals(left, right);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsLeftNoQuoteRightQuote_Deprecated() {
        final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
        final CSVFormat right = left.withQuote('#');

        assertNotEquals(left, right);
    }

    @Test
    void testEqualsNoQuotes() {
        final CSVFormat left = CSVFormat.newFormat(',').builder().setQuote(null).get();
        final CSVFormat right = left.builder().setQuote(null).get();

        assertEquals(left, right);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsNoQuotes_Deprecated() {
        final CSVFormat left = CSVFormat.newFormat(',').withQuote(null);
        final CSVFormat right = left.withQuote(null);

        assertEquals(left, right);
    }

    @Test
    void testEqualsNullString() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setRecordSeparator(CR).setCommentMarker('#').setEscape('+').setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true).setQuote('"').setQuoteMode(QuoteMode.ALL).setNullString("null").get();
        final CSVFormat left = right.builder().setNullString("---").get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsNullString_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withRecordSeparator(CR).withCommentMarker('#').withEscape('+').withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces().withQuote('"').withQuoteMode(QuoteMode.ALL).withNullString("null");
        final CSVFormat left = right.withNullString("---");

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsOne() {

        final CSVFormat csvFormatOne = CSVFormat.INFORMIX_UNLOAD;
        final CSVFormat csvFormatTwo = CSVFormat.MYSQL;

        assertEquals('\\', (char) csvFormatOne.getEscapeCharacter());
        assertEquals('\\', csvFormatOne.getEscapeChar());
        assertNull(csvFormatOne.getQuoteMode());

        assertTrue(csvFormatOne.getIgnoreEmptyLines());
        assertFalse(csvFormatOne.getSkipHeaderRecord());

        assertFalse(csvFormatOne.getIgnoreHeaderCase());
        assertNull(csvFormatOne.getCommentMarker());

        assertFalse(csvFormatOne.isCommentMarkerSet());
        assertTrue(csvFormatOne.isQuoteCharacterSet());

        assertEquals('|', csvFormatOne.getDelimiter());
        assertFalse(csvFormatOne.getAllowMissingColumnNames());

        assertTrue(csvFormatOne.isEscapeCharacterSet());
        assertEquals("\n", csvFormatOne.getRecordSeparator());

        assertEquals('\"', (char) csvFormatOne.getQuoteCharacter());
        assertFalse(csvFormatOne.getTrailingDelimiter());

        assertFalse(csvFormatOne.getTrim());
        assertFalse(csvFormatOne.isNullStringSet());

        assertNull(csvFormatOne.getNullString());
        assertFalse(csvFormatOne.getIgnoreSurroundingSpaces());

        assertTrue(csvFormatTwo.isEscapeCharacterSet());
        assertNull(csvFormatTwo.getQuoteCharacter());

        assertFalse(csvFormatTwo.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, csvFormatTwo.getQuoteMode());

        assertEquals('\t', csvFormatTwo.getDelimiter());
        assertArrayEquals(new char[] { '\t' }, csvFormatTwo.getDelimiterCharArray());
        assertEquals("\t", csvFormatTwo.getDelimiterString());
        assertEquals("\n", csvFormatTwo.getRecordSeparator());

        assertFalse(csvFormatTwo.isQuoteCharacterSet());
        assertTrue(csvFormatTwo.isNullStringSet());

        assertEquals('\\', (char) csvFormatTwo.getEscapeCharacter());
        assertFalse(csvFormatTwo.getIgnoreHeaderCase());

        assertFalse(csvFormatTwo.getTrim());
        assertFalse(csvFormatTwo.getIgnoreEmptyLines());

        assertEquals("\\N", csvFormatTwo.getNullString());
        assertFalse(csvFormatTwo.getIgnoreSurroundingSpaces());

        assertFalse(csvFormatTwo.getTrailingDelimiter());
        assertFalse(csvFormatTwo.getSkipHeaderRecord());

        assertNull(csvFormatTwo.getCommentMarker());
        assertFalse(csvFormatTwo.isCommentMarkerSet());

        assertNotSame(csvFormatTwo, csvFormatOne);
        Assertions.assertNotEquals(csvFormatTwo, csvFormatOne);

        assertEquals('\\', (char) csvFormatOne.getEscapeCharacter());
        assertNull(csvFormatOne.getQuoteMode());

        assertTrue(csvFormatOne.getIgnoreEmptyLines());
        assertFalse(csvFormatOne.getSkipHeaderRecord());

        assertFalse(csvFormatOne.getIgnoreHeaderCase());
        assertNull(csvFormatOne.getCommentMarker());

        assertFalse(csvFormatOne.isCommentMarkerSet());
        assertTrue(csvFormatOne.isQuoteCharacterSet());

        assertEquals('|', csvFormatOne.getDelimiter());
        assertFalse(csvFormatOne.getAllowMissingColumnNames());

        assertTrue(csvFormatOne.isEscapeCharacterSet());
        assertEquals("\n", csvFormatOne.getRecordSeparator());

        assertEquals('\"', (char) csvFormatOne.getQuoteCharacter());
        assertFalse(csvFormatOne.getTrailingDelimiter());

        assertFalse(csvFormatOne.getTrim());
        assertFalse(csvFormatOne.isNullStringSet());

        assertNull(csvFormatOne.getNullString());
        assertFalse(csvFormatOne.getIgnoreSurroundingSpaces());

        assertTrue(csvFormatTwo.isEscapeCharacterSet());
        assertNull(csvFormatTwo.getQuoteCharacter());

        assertFalse(csvFormatTwo.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, csvFormatTwo.getQuoteMode());

        assertEquals('\t', csvFormatTwo.getDelimiter());
        assertEquals("\n", csvFormatTwo.getRecordSeparator());

        assertFalse(csvFormatTwo.isQuoteCharacterSet());
        assertTrue(csvFormatTwo.isNullStringSet());

        assertEquals('\\', (char) csvFormatTwo.getEscapeCharacter());
        assertFalse(csvFormatTwo.getIgnoreHeaderCase());

        assertFalse(csvFormatTwo.getTrim());
        assertFalse(csvFormatTwo.getIgnoreEmptyLines());

        assertEquals("\\N", csvFormatTwo.getNullString());
        assertFalse(csvFormatTwo.getIgnoreSurroundingSpaces());

        assertFalse(csvFormatTwo.getTrailingDelimiter());
        assertFalse(csvFormatTwo.getSkipHeaderRecord());

        assertNull(csvFormatTwo.getCommentMarker());
        assertFalse(csvFormatTwo.isCommentMarkerSet());

        assertNotSame(csvFormatOne, csvFormatTwo);
        assertNotSame(csvFormatTwo, csvFormatOne);

        Assertions.assertNotEquals(csvFormatOne, csvFormatTwo);
        Assertions.assertNotEquals(csvFormatTwo, csvFormatOne);

        Assertions.assertNotEquals(csvFormatTwo, csvFormatOne);

    }

    @Test
    void testEqualsQuoteChar() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setQuote('"').get();
        final CSVFormat left = right.builder().setQuote('!').get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsQuoteChar_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuote('"');
        final CSVFormat left = right.withQuote('!');

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsQuotePolicy() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setQuote('"').setQuoteMode(QuoteMode.ALL).get();
        final CSVFormat left = right.builder().setQuoteMode(QuoteMode.MINIMAL).get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsQuotePolicy_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withQuote('"').withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right.withQuoteMode(QuoteMode.MINIMAL);

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsRecordSeparator() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setRecordSeparator(CR).setCommentMarker('#').setEscape('+').setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true).setQuote('"').setQuoteMode(QuoteMode.ALL).get();
        final CSVFormat left = right.builder().setRecordSeparator(LF).get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsRecordSeparator_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withRecordSeparator(CR).withCommentMarker('#').withEscape('+').withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces().withQuote('"').withQuoteMode(QuoteMode.ALL);
        final CSVFormat left = right.withRecordSeparator(LF);

        assertNotEquals(right, left);
    }

    void testEqualsSkipHeaderRecord() {
        final CSVFormat right = CSVFormat.newFormat('\'').builder().setRecordSeparator(CR).setCommentMarker('#').setEscape('+').setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true).setQuote('"').setQuoteMode(QuoteMode.ALL).setNullString("null").setSkipHeaderRecord(true).get();
        final CSVFormat left = right.builder().setSkipHeaderRecord(false).get();

        assertNotEquals(right, left);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEqualsSkipHeaderRecord_Deprecated() {
        final CSVFormat right = CSVFormat.newFormat('\'').withRecordSeparator(CR).withCommentMarker('#').withEscape('+').withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces().withQuote('"').withQuoteMode(QuoteMode.ALL).withNullString("null").withSkipHeaderRecord();
        final CSVFormat left = right.withSkipHeaderRecord(false);

        assertNotEquals(right, left);
    }

    @Test
    void testEqualsWithNull() {

        final CSVFormat csvFormat = CSVFormat.POSTGRESQL_TEXT;

        assertEquals('\\', (char) csvFormat.getEscapeCharacter());
        assertFalse(csvFormat.getIgnoreSurroundingSpaces());

        assertFalse(csvFormat.getTrailingDelimiter());
        assertFalse(csvFormat.getTrim());

        assertFalse(csvFormat.isQuoteCharacterSet());
        assertEquals("\\N", csvFormat.getNullString());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertTrue(csvFormat.isEscapeCharacterSet());

        assertFalse(csvFormat.isCommentMarkerSet());
        assertNull(csvFormat.getCommentMarker());

        assertFalse(csvFormat.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, csvFormat.getQuoteMode());

        assertEquals('\t', csvFormat.getDelimiter());
        assertFalse(csvFormat.getSkipHeaderRecord());

        assertEquals("\n", csvFormat.getRecordSeparator());
        assertFalse(csvFormat.getIgnoreEmptyLines());

        assertNull(csvFormat.getQuoteCharacter());
        assertTrue(csvFormat.isNullStringSet());

        assertEquals('\\', (char) csvFormat.getEscapeCharacter());
        assertFalse(csvFormat.getIgnoreSurroundingSpaces());

        assertFalse(csvFormat.getTrailingDelimiter());
        assertFalse(csvFormat.getTrim());

        assertFalse(csvFormat.isQuoteCharacterSet());
        assertEquals("\\N", csvFormat.getNullString());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertTrue(csvFormat.isEscapeCharacterSet());

        assertFalse(csvFormat.isCommentMarkerSet());
        assertNull(csvFormat.getCommentMarker());

        assertFalse(csvFormat.getAllowMissingColumnNames());
        assertEquals(QuoteMode.ALL_NON_NULL, csvFormat.getQuoteMode());

        assertEquals('\t', csvFormat.getDelimiter());
        assertFalse(csvFormat.getSkipHeaderRecord());

        assertEquals("\n", csvFormat.getRecordSeparator());
        assertFalse(csvFormat.getIgnoreEmptyLines());

        assertNull(csvFormat.getQuoteCharacter());
        assertTrue(csvFormat.isNullStringSet());

        Assertions.assertNotEquals(null, csvFormat);

    }

    @Test
    void testEscapeSameAsCommentStartThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setEscape('!').setCommentMarker('!').get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEscapeSameAsCommentStartThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!'));
    }

    @Test
    void testEscapeSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(IllegalArgumentException.class,
                () -> CSVFormat.DEFAULT.builder().setEscape(Character.valueOf('!')).setCommentMarker(Character.valueOf('!')).get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testEscapeSameAsCommentStartThrowsExceptionForWrapperType_Deprecated() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withEscape(Character.valueOf('!')).withCommentMarker(Character.valueOf('!')));
    }

    @Test
    void testFormat() {
        final CSVFormat format = CSVFormat.DEFAULT;

        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }

    @Test // I assume this to be a defect.
    void testFormatThrowsNullPointerException() {

        final CSVFormat csvFormat = CSVFormat.MYSQL;

        final NullPointerException e = assertThrows(NullPointerException.class, () -> csvFormat.format((Object[]) null));
        assertEquals(Objects.class.getName(), e.getStackTrace()[0].getClassName());
    }

    @Test
    void testFormatToString() {
        // @formatter:off
        final CSVFormat format = CSVFormat.RFC4180
                .withEscape('?')
                .withDelimiter(',')
                .withQuoteMode(QuoteMode.MINIMAL)
                .withRecordSeparator(CRLF)
                .withQuote('"')
                .withNullString("")
                .withIgnoreHeaderCase(true)
                .withHeaderComments("This is HeaderComments")
                .withHeader("col1", "col2", "col3");
        // @formatter:on
        assertEquals(
                "Delimiter=<,> Escape=<?> QuoteChar=<\"> QuoteMode=<MINIMAL> NullString=<> RecordSeparator=<" + CRLF +
                        "> IgnoreHeaderCase:ignored SkipHeaderRecord:false HeaderComments:[This is HeaderComments] Header:[col1, col2, col3]",
                format.toString());
    }

    @Test
    void testGetAllowDuplicateHeaderNames() {
        final Builder builder = CSVFormat.DEFAULT.builder();
        assertTrue(builder.get().getAllowDuplicateHeaderNames());
        assertTrue(builder.setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL).get().getAllowDuplicateHeaderNames());
        assertFalse(builder.setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_EMPTY).get().getAllowDuplicateHeaderNames());
        assertFalse(builder.setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW).get().getAllowDuplicateHeaderNames());
    }

    @Test
    void testGetDuplicateHeaderMode() {
        final Builder builder = CSVFormat.DEFAULT.builder();

        assertEquals(DuplicateHeaderMode.ALLOW_ALL, builder.get().getDuplicateHeaderMode());
        assertEquals(DuplicateHeaderMode.ALLOW_ALL, builder.setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL).get().getDuplicateHeaderMode());
        assertEquals(DuplicateHeaderMode.ALLOW_EMPTY, builder.setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_EMPTY).get().getDuplicateHeaderMode());
        assertEquals(DuplicateHeaderMode.DISALLOW, builder.setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW).get().getDuplicateHeaderMode());
    }

    @Test
    void testGetHeader() {
        final String[] header = { "one", "two", "three" };
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
    void testHashCodeAndWithIgnoreHeaderCase() {

        final CSVFormat csvFormat = CSVFormat.INFORMIX_UNLOAD_CSV;
        final CSVFormat csvFormatTwo = csvFormat.withIgnoreHeaderCase();
        csvFormatTwo.hashCode();

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertTrue(csvFormatTwo.getIgnoreHeaderCase()); // now different
        assertFalse(csvFormatTwo.getTrailingDelimiter());

        Assertions.assertNotEquals(csvFormatTwo, csvFormat); // CSV-244 - should not be equal
        assertFalse(csvFormatTwo.getAllowMissingColumnNames());

        assertFalse(csvFormatTwo.getTrim());

    }

    @Test
    void testJiraCsv236() {
        CSVFormat.DEFAULT.builder().setAllowDuplicateHeaderNames(true).setHeader("CC", "VV", "VV").get();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testJiraCsv236__Deprecated() {
        CSVFormat.DEFAULT.withAllowDuplicateHeaderNames().withHeader("CC", "VV", "VV");
    }

    @Test
    void testNewFormat() {

        final CSVFormat csvFormat = CSVFormat.newFormat('X');

        assertFalse(csvFormat.getSkipHeaderRecord());
        assertFalse(csvFormat.isEscapeCharacterSet());

        assertNull(csvFormat.getRecordSeparator());
        assertNull(csvFormat.getQuoteMode());

        assertNull(csvFormat.getCommentMarker());
        assertFalse(csvFormat.getIgnoreHeaderCase());

        assertFalse(csvFormat.getAllowMissingColumnNames());
        assertFalse(csvFormat.getTrim());

        assertFalse(csvFormat.isNullStringSet());
        assertNull(csvFormat.getEscapeCharacter());

        assertFalse(csvFormat.getIgnoreSurroundingSpaces());
        assertFalse(csvFormat.getTrailingDelimiter());

        assertEquals('X', csvFormat.getDelimiter());
        assertNull(csvFormat.getNullString());

        assertFalse(csvFormat.isQuoteCharacterSet());
        assertFalse(csvFormat.isCommentMarkerSet());

        assertNull(csvFormat.getQuoteCharacter());
        assertFalse(csvFormat.getIgnoreEmptyLines());

        assertFalse(csvFormat.getSkipHeaderRecord());
        assertFalse(csvFormat.isEscapeCharacterSet());

        assertNull(csvFormat.getRecordSeparator());
        assertNull(csvFormat.getQuoteMode());

        assertNull(csvFormat.getCommentMarker());
        assertFalse(csvFormat.getIgnoreHeaderCase());

        assertFalse(csvFormat.getAllowMissingColumnNames());
        assertFalse(csvFormat.getTrim());

        assertFalse(csvFormat.isNullStringSet());
        assertNull(csvFormat.getEscapeCharacter());

        assertFalse(csvFormat.getIgnoreSurroundingSpaces());
        assertFalse(csvFormat.getTrailingDelimiter());

        assertEquals('X', csvFormat.getDelimiter());
        assertNull(csvFormat.getNullString());

        assertFalse(csvFormat.isQuoteCharacterSet());
        assertFalse(csvFormat.isCommentMarkerSet());

        assertNull(csvFormat.getQuoteCharacter());
        assertFalse(csvFormat.getIgnoreEmptyLines());

    }

    @Test
    void testNullRecordSeparatorCsv106() {
        final CSVFormat format = CSVFormat.newFormat(';').builder().setSkipHeaderRecord(true).setHeader("H1", "H2").get();
        final String formatStr = format.format("A", "B");
        assertNotNull(formatStr);
        assertFalse(formatStr.endsWith("null"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testNullRecordSeparatorCsv106__Deprecated() {
        final CSVFormat format = CSVFormat.newFormat(';').withSkipHeaderRecord().withHeader("H1", "H2");
        final String formatStr = format.format("A", "B");
        assertNotNull(formatStr);
        assertFalse(formatStr.endsWith("null"));
    }

    @Test
    void testPrintRecord() throws IOException {
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180;
        format.printRecord(out, "a", "b", "c");
        assertEquals("a,b,c" + format.getRecordSeparator(), out.toString());
    }

    @Test
    void testPrintRecordEmpty() throws IOException {
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180;
        format.printRecord(out);
        assertEquals(format.getRecordSeparator(), out.toString());
    }

    @Test
    void testPrintWithEscapesEndWithCRLF() throws IOException {
        final Reader in = new StringReader("x,y,x\r\na,?b,c\r\n");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withEscape('?').withDelimiter(',').withQuote(null).withRecordSeparator(CRLF);
        format.print(in, out, true);
        assertEquals("x?,y?,x?r?na?,??b?,c?r?n", out.toString());
    }

    @Test
    void testPrintWithEscapesEndWithoutCRLF() throws IOException {
        final Reader in = new StringReader("x,y,x");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withEscape('?').withDelimiter(',').withQuote(null).withRecordSeparator(CRLF);
        format.print(in, out, true);
        assertEquals("x?,y?,x", out.toString());
    }

    @Test
    void testPrintWithoutQuotes() throws IOException {
        final Reader in = new StringReader("");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withDelimiter(',').withQuote('"').withEscape('?').withQuoteMode(QuoteMode.NON_NUMERIC);
        format.print(in, out, true);
        assertEquals("\"\"", out.toString());
    }

    @Test
    void testPrintWithQuoteModeIsNONE() throws IOException {
        final Reader in = new StringReader("a,b,c");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withDelimiter(',').withQuote('"').withEscape('?').withQuoteMode(QuoteMode.NONE);
        format.print(in, out, true);
        assertEquals("a?,b?,c", out.toString());
    }

    @Test
    void testPrintWithQuotes() throws IOException {
        final Reader in = new StringReader("\"a,b,c\r\nx,y,z");
        final Appendable out = new StringBuilder();
        final CSVFormat format = CSVFormat.RFC4180.withDelimiter(',').withQuote('"').withEscape('?').withQuoteMode(QuoteMode.NON_NUMERIC);
        format.print(in, out, true);
        assertEquals("\"\"\"a,b,c\r\nx,y,z\"", out.toString());
    }

    @Test
    void testQuoteCharSameAsCommentStartThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setQuote('!').setCommentMarker('!').get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testQuoteCharSameAsCommentStartThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!'));
    }

    @Test
    void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setQuote(Character.valueOf('!')).setCommentMarker('!').get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType_Deprecated() {
        // Cannot assume that callers won't use different Character objects
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withQuote(Character.valueOf('!')).withCommentMarker('!'));
    }

    @Test
    void testQuoteCharSameAsDelimiterThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.builder().setQuote('!').setDelimiter('!').get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testQuoteCharSameAsDelimiterThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withQuote('!').withDelimiter('!'));
    }

    @Test
    void testQuoteModeNoneShouldReturnMeaningfulExceptionMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () ->
        // @formatter:off
            CSVFormat.DEFAULT.builder()
                    .setHeader("Col1", "Col2", "Col3", "Col4")
                    .setQuoteMode(QuoteMode.NONE)
                    .get()
        // @formatter:on
        );
        final String actualMessage = exception.getMessage();
        final String expectedMessage = "Quote mode set to NONE but no escape character is set";
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testQuotePolicyNoneWithoutEscapeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.newFormat('!').builder().setQuoteMode(QuoteMode.NONE).get());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testQuotePolicyNoneWithoutEscapeThrowsException_Deprecated() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.newFormat('!').withQuoteMode(QuoteMode.NONE));
    }

    @Test
    void testRFC4180() {
        assertNull(RFC4180.getCommentMarker());
        assertEquals(',', RFC4180.getDelimiter());
        assertNull(RFC4180.getEscapeCharacter());
        assertFalse(RFC4180.getIgnoreEmptyLines());
        assertEquals(Character.valueOf('"'), RFC4180.getQuoteCharacter());
        assertNull(RFC4180.getQuoteMode());
        assertEquals("\r\n", RFC4180.getRecordSeparator());
    }

    @SuppressWarnings("boxing") // no need to worry about boxing here
    @Test
    void testSerialization() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(CSVFormat.DEFAULT);
            oos.flush();
        }

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        final CSVFormat format = (CSVFormat) in.readObject();

        assertNotNull(format);
        assertEquals(CSVFormat.DEFAULT.getDelimiter(), format.getDelimiter(), "delimiter");
        assertEquals(CSVFormat.DEFAULT.getQuoteCharacter(), format.getQuoteCharacter(), "encapsulator");
        assertEquals(CSVFormat.DEFAULT.getCommentMarker(), format.getCommentMarker(), "comment start");
        assertEquals(CSVFormat.DEFAULT.getRecordSeparator(), format.getRecordSeparator(), "record separator");
        assertEquals(CSVFormat.DEFAULT.getEscapeCharacter(), format.getEscapeCharacter(), "escape");
        assertEquals(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces(), format.getIgnoreSurroundingSpaces(), "trim");
        assertEquals(CSVFormat.DEFAULT.getIgnoreEmptyLines(), format.getIgnoreEmptyLines(), "empty lines");
    }

    @Test
    void testToString() {

        final String string = CSVFormat.INFORMIX_UNLOAD.toString();

        assertEquals("Delimiter=<|> Escape=<\\> QuoteChar=<\"> RecordSeparator=<\n> EmptyLines:ignored SkipHeaderRecord:false", string);

    }

    @Test
    void testToStringAndWithCommentMarkerTakingCharacter() {

        final CSVFormat.Predefined csvFormatPredefined = CSVFormat.Predefined.Default;
        final CSVFormat csvFormat = csvFormatPredefined.getFormat();

        assertNull(csvFormat.getEscapeCharacter());
        assertTrue(csvFormat.isQuoteCharacterSet());

        assertFalse(csvFormat.getTrim());
        assertFalse(csvFormat.getIgnoreSurroundingSpaces());

        assertFalse(csvFormat.getTrailingDelimiter());
        assertEquals(',', csvFormat.getDelimiter());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", csvFormat.getRecordSeparator());

        assertFalse(csvFormat.isCommentMarkerSet());
        assertNull(csvFormat.getCommentMarker());

        assertFalse(csvFormat.isNullStringSet());
        assertFalse(csvFormat.getAllowMissingColumnNames());

        assertFalse(csvFormat.isEscapeCharacterSet());
        assertFalse(csvFormat.getSkipHeaderRecord());

        assertNull(csvFormat.getNullString());
        assertNull(csvFormat.getQuoteMode());

        assertTrue(csvFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char) csvFormat.getQuoteCharacter());

        final Character character = Character.valueOf('n');

        final CSVFormat csvFormatTwo = csvFormat.withCommentMarker(character);

        assertNull(csvFormat.getEscapeCharacter());
        assertTrue(csvFormat.isQuoteCharacterSet());

        assertFalse(csvFormat.getTrim());
        assertFalse(csvFormat.getIgnoreSurroundingSpaces());

        assertFalse(csvFormat.getTrailingDelimiter());
        assertEquals(',', csvFormat.getDelimiter());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", csvFormat.getRecordSeparator());

        assertFalse(csvFormat.isCommentMarkerSet());
        assertNull(csvFormat.getCommentMarker());

        assertFalse(csvFormat.isNullStringSet());
        assertFalse(csvFormat.getAllowMissingColumnNames());

        assertFalse(csvFormat.isEscapeCharacterSet());
        assertFalse(csvFormat.getSkipHeaderRecord());

        assertNull(csvFormat.getNullString());
        assertNull(csvFormat.getQuoteMode());

        assertTrue(csvFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char) csvFormat.getQuoteCharacter());

        assertFalse(csvFormatTwo.isNullStringSet());
        assertFalse(csvFormatTwo.getAllowMissingColumnNames());

        assertEquals('\"', (char) csvFormatTwo.getQuoteCharacter());
        assertNull(csvFormatTwo.getNullString());

        assertEquals(',', csvFormatTwo.getDelimiter());
        assertFalse(csvFormatTwo.getTrailingDelimiter());

        assertTrue(csvFormatTwo.isCommentMarkerSet());
        assertFalse(csvFormatTwo.getIgnoreHeaderCase());

        assertFalse(csvFormatTwo.getTrim());
        assertNull(csvFormatTwo.getEscapeCharacter());

        assertTrue(csvFormatTwo.isQuoteCharacterSet());
        assertFalse(csvFormatTwo.getIgnoreSurroundingSpaces());

        assertEquals("\r\n", csvFormatTwo.getRecordSeparator());
        assertNull(csvFormatTwo.getQuoteMode());

        assertEquals('n', (char) csvFormatTwo.getCommentMarker());
        assertFalse(csvFormatTwo.getSkipHeaderRecord());

        assertFalse(csvFormatTwo.isEscapeCharacterSet());
        assertTrue(csvFormatTwo.getIgnoreEmptyLines());

        assertNotSame(csvFormat, csvFormatTwo);
        assertNotSame(csvFormatTwo, csvFormat);

        Assertions.assertNotEquals(csvFormatTwo, csvFormat);

        assertNull(csvFormat.getEscapeCharacter());
        assertTrue(csvFormat.isQuoteCharacterSet());

        assertFalse(csvFormat.getTrim());
        assertFalse(csvFormat.getIgnoreSurroundingSpaces());

        assertFalse(csvFormat.getTrailingDelimiter());
        assertEquals(',', csvFormat.getDelimiter());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertEquals("\r\n", csvFormat.getRecordSeparator());

        assertFalse(csvFormat.isCommentMarkerSet());
        assertNull(csvFormat.getCommentMarker());

        assertFalse(csvFormat.isNullStringSet());
        assertFalse(csvFormat.getAllowMissingColumnNames());

        assertFalse(csvFormat.isEscapeCharacterSet());
        assertFalse(csvFormat.getSkipHeaderRecord());

        assertNull(csvFormat.getNullString());
        assertNull(csvFormat.getQuoteMode());

        assertTrue(csvFormat.getIgnoreEmptyLines());
        assertEquals('\"', (char) csvFormat.getQuoteCharacter());

        assertFalse(csvFormatTwo.isNullStringSet());
        assertFalse(csvFormatTwo.getAllowMissingColumnNames());

        assertEquals('\"', (char) csvFormatTwo.getQuoteCharacter());
        assertNull(csvFormatTwo.getNullString());

        assertEquals(',', csvFormatTwo.getDelimiter());
        assertFalse(csvFormatTwo.getTrailingDelimiter());

        assertTrue(csvFormatTwo.isCommentMarkerSet());
        assertFalse(csvFormatTwo.getIgnoreHeaderCase());

        assertFalse(csvFormatTwo.getTrim());
        assertNull(csvFormatTwo.getEscapeCharacter());

        assertTrue(csvFormatTwo.isQuoteCharacterSet());
        assertFalse(csvFormatTwo.getIgnoreSurroundingSpaces());

        assertEquals("\r\n", csvFormatTwo.getRecordSeparator());
        assertNull(csvFormatTwo.getQuoteMode());

        assertEquals('n', (char) csvFormatTwo.getCommentMarker());
        assertFalse(csvFormatTwo.getSkipHeaderRecord());

        assertFalse(csvFormatTwo.isEscapeCharacterSet());
        assertTrue(csvFormatTwo.getIgnoreEmptyLines());

        assertNotSame(csvFormat, csvFormatTwo);
        assertNotSame(csvFormatTwo, csvFormat);

        Assertions.assertNotEquals(csvFormat, csvFormatTwo);

        Assertions.assertNotEquals(csvFormatTwo, csvFormat);
        assertEquals("Delimiter=<,> QuoteChar=<\"> CommentStart=<n> " + "RecordSeparator=<\r\n> EmptyLines:ignored SkipHeaderRecord:false",
                csvFormatTwo.toString());

    }

    @Test
    void testTrim() throws IOException {
        final CSVFormat formatWithTrim = CSVFormat.DEFAULT.withDelimiter(',').withTrim().withQuote(null).withRecordSeparator(CRLF);

        CharSequence in = "a,b,c";
        final StringBuilder out = new StringBuilder();
        formatWithTrim.print(in, out, true);
        assertEquals("a,b,c", out.toString());

        in = new StringBuilder(" x,y,z");
        out.setLength(0);
        formatWithTrim.print(in, out, true);
        assertEquals("x,y,z", out.toString());

        in = new StringBuilder("");
        out.setLength(0);
        formatWithTrim.print(in, out, true);
        assertEquals("", out.toString());

        in = new StringBuilder("header\r\n");
        out.setLength(0);
        formatWithTrim.print(in, out, true);
        assertEquals("header", out.toString());
    }

    @Test
    void testWithCommentStart() {
        final CSVFormat formatWithCommentStart = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), formatWithCommentStart.getCommentMarker());
    }

    @Test
    void testWithCommentStartCRThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withCommentMarker(CR));
    }

    @Test
    void testWithDelimiter() {
        final CSVFormat formatWithDelimiter = CSVFormat.DEFAULT.withDelimiter('!');
        assertEquals('!', formatWithDelimiter.getDelimiter());
    }

    @Test
    void testWithDelimiterLFThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withDelimiter(LF));
    }

    @Test
    void testWithEmptyDuplicates() {
        final CSVFormat formatWithEmptyDuplicates = CSVFormat.DEFAULT.builder().setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_EMPTY).get();

        assertEquals(DuplicateHeaderMode.ALLOW_EMPTY, formatWithEmptyDuplicates.getDuplicateHeaderMode());
        assertFalse(formatWithEmptyDuplicates.getAllowDuplicateHeaderNames());
    }

    @Test
    void testWithEmptyEnum() {
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(EmptyEnum.class);
        assertEquals(0, formatWithHeader.getHeader().length);
    }

    @Test
    void testWithEscape() {
        final CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('&');
        assertEquals(Character.valueOf('&'), formatWithEscape.getEscapeCharacter());
    }

    @Test
    void testWithEscapeCRThrowsExceptions() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withEscape(CR));
    }

    @Test
    void testWithFirstRecordAsHeader() {
        final CSVFormat formatWithFirstRecordAsHeader = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(formatWithFirstRecordAsHeader.getSkipHeaderRecord());
        assertEquals(0, formatWithFirstRecordAsHeader.getHeader().length);
    }

    @Test
    void testWithHeader() {
        final String[] header = { "one", "two", "three" };
        // withHeader() makes a copy of the header array.
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(header);
        assertArrayEquals(header, formatWithHeader.getHeader());
        assertNotSame(header, formatWithHeader.getHeader());
    }

    @Test
    void testWithHeaderComments() {

        final CSVFormat csvFormat = CSVFormat.DEFAULT;

        assertEquals('\"', (char) csvFormat.getQuoteCharacter());
        assertFalse(csvFormat.isCommentMarkerSet());

        assertFalse(csvFormat.isEscapeCharacterSet());
        assertTrue(csvFormat.isQuoteCharacterSet());

        assertFalse(csvFormat.getSkipHeaderRecord());
        assertNull(csvFormat.getQuoteMode());

        assertEquals(',', csvFormat.getDelimiter());
        assertTrue(csvFormat.getIgnoreEmptyLines());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertNull(csvFormat.getCommentMarker());

        assertEquals("\r\n", csvFormat.getRecordSeparator());
        assertFalse(csvFormat.getTrailingDelimiter());

        assertFalse(csvFormat.getAllowMissingColumnNames());
        assertFalse(csvFormat.getTrim());

        assertFalse(csvFormat.isNullStringSet());
        assertNull(csvFormat.getNullString());

        assertFalse(csvFormat.getIgnoreSurroundingSpaces());
        assertNull(csvFormat.getEscapeCharacter());

        final Object[] objectArray = new Object[8];
        final CSVFormat csvFormatTwo = csvFormat.withHeaderComments(objectArray);

        assertEquals('\"', (char) csvFormat.getQuoteCharacter());
        assertFalse(csvFormat.isCommentMarkerSet());

        assertFalse(csvFormat.isEscapeCharacterSet());
        assertTrue(csvFormat.isQuoteCharacterSet());

        assertFalse(csvFormat.getSkipHeaderRecord());
        assertNull(csvFormat.getQuoteMode());

        assertEquals(',', csvFormat.getDelimiter());
        assertTrue(csvFormat.getIgnoreEmptyLines());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertNull(csvFormat.getCommentMarker());

        assertEquals("\r\n", csvFormat.getRecordSeparator());
        assertFalse(csvFormat.getTrailingDelimiter());

        assertFalse(csvFormat.getAllowMissingColumnNames());
        assertFalse(csvFormat.getTrim());

        assertFalse(csvFormat.isNullStringSet());
        assertNull(csvFormat.getNullString());

        assertFalse(csvFormat.getIgnoreSurroundingSpaces());
        assertNull(csvFormat.getEscapeCharacter());

        assertFalse(csvFormatTwo.getIgnoreHeaderCase());
        assertNull(csvFormatTwo.getQuoteMode());

        assertTrue(csvFormatTwo.getIgnoreEmptyLines());
        assertFalse(csvFormatTwo.getIgnoreSurroundingSpaces());

        assertNull(csvFormatTwo.getEscapeCharacter());
        assertFalse(csvFormatTwo.getTrim());

        assertFalse(csvFormatTwo.isEscapeCharacterSet());
        assertTrue(csvFormatTwo.isQuoteCharacterSet());

        assertFalse(csvFormatTwo.getSkipHeaderRecord());
        assertEquals('\"', (char) csvFormatTwo.getQuoteCharacter());

        assertFalse(csvFormatTwo.getAllowMissingColumnNames());
        assertNull(csvFormatTwo.getNullString());

        assertFalse(csvFormatTwo.isNullStringSet());
        assertFalse(csvFormatTwo.getTrailingDelimiter());

        assertEquals("\r\n", csvFormatTwo.getRecordSeparator());
        assertEquals(',', csvFormatTwo.getDelimiter());

        assertNull(csvFormatTwo.getCommentMarker());
        assertFalse(csvFormatTwo.isCommentMarkerSet());

        assertNotSame(csvFormat, csvFormatTwo);
        assertNotSame(csvFormatTwo, csvFormat);

        Assertions.assertNotEquals(csvFormatTwo, csvFormat); // CSV-244 - should not be equal

        final String string = csvFormatTwo.format(objectArray);

        assertEquals('\"', (char) csvFormat.getQuoteCharacter());
        assertFalse(csvFormat.isCommentMarkerSet());

        assertFalse(csvFormat.isEscapeCharacterSet());
        assertTrue(csvFormat.isQuoteCharacterSet());

        assertFalse(csvFormat.getSkipHeaderRecord());
        assertNull(csvFormat.getQuoteMode());

        assertEquals(',', csvFormat.getDelimiter());
        assertTrue(csvFormat.getIgnoreEmptyLines());

        assertFalse(csvFormat.getIgnoreHeaderCase());
        assertNull(csvFormat.getCommentMarker());

        assertEquals("\r\n", csvFormat.getRecordSeparator());
        assertFalse(csvFormat.getTrailingDelimiter());

        assertFalse(csvFormat.getAllowMissingColumnNames());
        assertFalse(csvFormat.getTrim());

        assertFalse(csvFormat.isNullStringSet());
        assertNull(csvFormat.getNullString());

        assertFalse(csvFormat.getIgnoreSurroundingSpaces());
        assertNull(csvFormat.getEscapeCharacter());

        assertFalse(csvFormatTwo.getIgnoreHeaderCase());
        assertNull(csvFormatTwo.getQuoteMode());

        assertTrue(csvFormatTwo.getIgnoreEmptyLines());
        assertFalse(csvFormatTwo.getIgnoreSurroundingSpaces());

        assertNull(csvFormatTwo.getEscapeCharacter());
        assertFalse(csvFormatTwo.getTrim());

        assertFalse(csvFormatTwo.isEscapeCharacterSet());
        assertTrue(csvFormatTwo.isQuoteCharacterSet());

        assertFalse(csvFormatTwo.getSkipHeaderRecord());
        assertEquals('\"', (char) csvFormatTwo.getQuoteCharacter());

        assertFalse(csvFormatTwo.getAllowMissingColumnNames());
        assertNull(csvFormatTwo.getNullString());

        assertFalse(csvFormatTwo.isNullStringSet());
        assertFalse(csvFormatTwo.getTrailingDelimiter());

        assertEquals("\r\n", csvFormatTwo.getRecordSeparator());
        assertEquals(',', csvFormatTwo.getDelimiter());

        assertNull(csvFormatTwo.getCommentMarker());
        assertFalse(csvFormatTwo.isCommentMarkerSet());

        assertNotSame(csvFormat, csvFormatTwo);
        assertNotSame(csvFormatTwo, csvFormat);

        assertNotNull(string);
        Assertions.assertNotEquals(csvFormat, csvFormatTwo); // CSV-244 - should not be equal

        Assertions.assertNotEquals(csvFormatTwo, csvFormat); // CSV-244 - should not be equal
        assertEquals(",,,,,,,", string);

    }

    @Test
    void testWithHeaderEnum() {
        final CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(Header.class);
        assertArrayEquals(new String[] { "Name", "Email", "Phone" }, formatWithHeader.getHeader());
    }

    @Test
    void testWithHeaderEnumNull() {
        final CSVFormat format = CSVFormat.DEFAULT;
        final Class<Enum<?>> simpleName = null;
        format.withHeader(simpleName);
    }

    @Test
    void testWithHeaderResultSetNull() throws SQLException {
        final CSVFormat format = CSVFormat.DEFAULT;
        final ResultSet resultSet = null;
        format.withHeader(resultSet);
    }

    @Test
    void testWithIgnoreEmptyLines() {
        assertFalse(CSVFormat.DEFAULT.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
        assertTrue(CSVFormat.DEFAULT.withIgnoreEmptyLines().getIgnoreEmptyLines());
    }

    @Test
    void testWithIgnoreSurround() {
        assertFalse(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false).getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces().getIgnoreSurroundingSpaces());
    }

    @Test
    void testWithNullString() {
        final CSVFormat formatWithNullString = CSVFormat.DEFAULT.withNullString("null");
        assertEquals("null", formatWithNullString.getNullString());
    }

    @Test
    void testWithQuoteChar() {
        final CSVFormat formatWithQuoteChar = CSVFormat.DEFAULT.withQuote('"');
        assertEquals(Character.valueOf('"'), formatWithQuoteChar.getQuoteCharacter());
    }

    @Test
    void testWithQuoteLFThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CSVFormat.DEFAULT.withQuote(LF));
    }

    @Test
    void testWithQuotePolicy() {
        final CSVFormat formatWithQuotePolicy = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, formatWithQuotePolicy.getQuoteMode());
    }

    @Test
    void testWithRecordSeparatorCR() {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(CR);
        assertEquals(String.valueOf(CR), formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    void testWithRecordSeparatorCRLF() {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(CRLF);
        assertEquals(CRLF, formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    void testWithRecordSeparatorLF() {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withRecordSeparator(LF);
        assertEquals(String.valueOf(LF), formatWithRecordSeparator.getRecordSeparator());
    }

    @Test
    void testWithSystemRecordSeparator() {
        final CSVFormat formatWithRecordSeparator = CSVFormat.DEFAULT.withSystemRecordSeparator();
        assertEquals(System.lineSeparator(), formatWithRecordSeparator.getRecordSeparator());
    }
}
