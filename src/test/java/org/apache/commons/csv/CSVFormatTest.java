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

import static org.apache.commons.csv.Constants.CRLF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * 
 * 
 * @version $Id: $
 */
public class CSVFormatTest {

    @Test
    public void testImmutalibity() {
        final CSVFormat format = new CSVFormat('!', '!', Quote.MINIMAL, '!', '!', true, true, CRLF, null);

        format.withDelimiter('?');
        format.withQuoteChar('?');
        format.withQuotePolicy(Quote.ALL);
        format.withCommentStart('?');
        format.withLineSeparator("?");
        format.withEscape('?');
        format.withIgnoreSurroundingSpaces(false);
        format.withIgnoreEmptyLines(false);

        assertEquals('!', format.getDelimiter());
        assertEquals('!', format.getQuoteChar().charValue());
        assertEquals('!', format.getCommentStart().charValue());
        assertEquals('!', format.getEscape().charValue());
        assertEquals(CRLF, format.getLineSeparator());

        assertTrue(format.getIgnoreSurroundingSpaces());
        assertTrue(format.getIgnoreEmptyLines());

        assertEquals(Quote.MINIMAL, format.getQuotePolicy());
    }

    @Test
    public void testMutators() {
        final CSVFormat format = new CSVFormat('!', '!', null, '!', '!', true, true, CRLF, null);

        assertEquals('?', format.withDelimiter('?').getDelimiter());
        assertEquals('?', format.withQuoteChar('?').getQuoteChar().charValue());
        assertEquals(Quote.ALL, format.withQuotePolicy(Quote.ALL).getQuotePolicy());
        assertEquals('?', format.withCommentStart('?').getCommentStart().charValue());
        assertEquals("?", format.withLineSeparator("?").getLineSeparator());
        assertEquals('?', format.withEscape('?').getEscape().charValue());

        assertFalse(format.withIgnoreSurroundingSpaces(false).getIgnoreSurroundingSpaces());
        assertFalse(format.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
    }

    @Test
    public void testFormat() {
        final CSVFormat format = CSVFormat.DEFAULT;

        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }

    @Test
    public void testValidation() {
        final CSVFormat format = CSVFormat.DEFAULT;

        try {
            format.withDelimiter('\n');
            fail();
        } catch (final IllegalArgumentException e) {
            // expected
        }

        try {
            format.withEscape('\r');
            fail();
        } catch (final IllegalArgumentException e) {
            // expected
        }

        try {
            format.withQuoteChar('\n');
            fail();
        } catch (final IllegalArgumentException e) {
            // expected
        }

        try {
            format.withCommentStart('\r');
            fail();
        } catch (final IllegalArgumentException e) {
            // expected
        }

        try {
            format.withDelimiter('!').withEscape('!').validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }

        try {
            format.withDelimiter('!').withCommentStart('!').validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }

        try {
            format.withQuoteChar('!').withCommentStart('!').validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }

        // Cannot assume that callers won't use different Character objects
        try {
            format.withQuoteChar(new Character('!')).withCommentStart('!').validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }

        format.withQuoteChar(null).withCommentStart(null).validate();

        try {
            format.withEscape('!').withCommentStart('!').validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }

        // Cannot assume that callers won't use different Character objects
        try {
            format.withEscape(new Character('!')).withCommentStart(new Character('!')).validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }

        format.withEscape(null).withCommentStart(null).validate();


        try {
            format.withQuoteChar('!').withDelimiter('!').validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }

        try {
            format.withQuoteChar('!').withQuotePolicy(Quote.NONE).validate();
            fail();
        } catch (final IllegalStateException e) {
            // expected
        }
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
        assertEquals("line separator", CSVFormat.DEFAULT.getLineSeparator(), format.getLineSeparator());
        assertEquals("escape", CSVFormat.DEFAULT.getEscape(), format.getEscape());
        assertEquals("trim", CSVFormat.DEFAULT.getIgnoreSurroundingSpaces(), format.getIgnoreSurroundingSpaces());
        assertEquals("empty lines", CSVFormat.DEFAULT.getIgnoreEmptyLines(), format.getIgnoreEmptyLines());
    }
}
