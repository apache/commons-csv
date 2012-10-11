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

public class CSVFormatTest {

    @Test
    public void testImmutalibity() {
        final CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, CSVFormat.CRLF, null);

        format.withDelimiter('?');
        format.withEncapsulator('?');
        format.withCommentStart('?');
        format.withLineSeparator("?");
        format.withEscape('?');
        format.withIgnoreSurroundingSpaces(false);
        format.withIgnoreEmptyLines(false);

        assertEquals('!', format.getDelimiter());
        assertEquals('!', format.getEncapsulator());
        assertEquals('!', format.getCommentStart());
        assertEquals('!', format.getEscape());
        assertEquals(CSVFormat.CRLF, format.getLineSeparator());

        assertTrue(format.getIgnoreSurroundingSpaces());
        assertTrue(format.getIgnoreEmptyLines());
    }

    @Test
    public void testMutators() {
        final CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, CSVFormat.CRLF, null);

        assertEquals('?', format.withDelimiter('?').getDelimiter());
        assertEquals('?', format.withEncapsulator('?').getEncapsulator());
        assertEquals('?', format.withCommentStart('?').getCommentStart());
        assertEquals("?", format.withLineSeparator("?").getLineSeparator());
        assertEquals('?', format.withEscape('?').getEscape());

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
            format.withEncapsulator('\n');
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
        } catch (final IllegalArgumentException e) {
            // expected
        }

        try {
            format.withDelimiter('!').withCommentStart('!').validate();
            fail();
        } catch (final IllegalArgumentException e) {
            // expected
        }

        try {
            format.withEncapsulator('!').withCommentStart('!').validate();
            fail();
        } catch (final IllegalArgumentException e) {
            // expected
        }

        format.withEncapsulator(CSVFormat.DISABLED).withCommentStart(CSVFormat.DISABLED).validate();

        try {
            format.withEscape('!').withCommentStart('!').validate();
            fail();
        } catch (final IllegalArgumentException e) {
            // expected
        }

        format.withEscape(CSVFormat.DISABLED).withCommentStart(CSVFormat.DISABLED).validate();


        try {
            format.withEncapsulator('!').withDelimiter('!').validate();
            fail();
        } catch (final IllegalArgumentException e) {
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
        assertEquals("encapsulator", CSVFormat.DEFAULT.getEncapsulator(), format.getEncapsulator());
        assertEquals("comment start", CSVFormat.DEFAULT.getCommentStart(), format.getCommentStart());
        assertEquals("line separator", CSVFormat.DEFAULT.getLineSeparator(), format.getLineSeparator());
        assertEquals("escape", CSVFormat.DEFAULT.getEscape(), format.getEscape());
        assertEquals("trim", CSVFormat.DEFAULT.getIgnoreSurroundingSpaces(), format.getIgnoreSurroundingSpaces());
        assertEquals("empty lines", CSVFormat.DEFAULT.getIgnoreEmptyLines(), format.getIgnoreEmptyLines());
    }
}
