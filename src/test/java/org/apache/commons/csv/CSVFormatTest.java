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
        final CSVFormat left = CSVFormat.newBuilder().build();

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
        final CSVFormat right = CSVFormat.newBuilder('!').build();
        final CSVFormat left = CSVFormat.newBuilder('?').build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsQuoteChar() {
        final CSVFormat right = CSVFormat.newBuilder('\'').withQuoteChar('"').build();
        final CSVFormat left = CSVFormat.newBuilder(right).withQuoteChar('!').build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsQuotePolicy() {
        final CSVFormat right = CSVFormat.newBuilder('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL)
                .build();
        final CSVFormat left = CSVFormat.newBuilder(right)
                .withQuotePolicy(Quote.MINIMAL)
                .build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsCommentStart() {
        final CSVFormat right = CSVFormat.newBuilder('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .build();
        final CSVFormat left = CSVFormat.newBuilder(right)
                .withCommentStart('!')
                .build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsEscape() {
        final CSVFormat right = CSVFormat.newBuilder('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .withEscape('+')
                .build();
        final CSVFormat left = CSVFormat.newBuilder(right)
                .withEscape('!')
                .build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsIgnoreSurroundingSpaces() {
        final CSVFormat right = CSVFormat.newBuilder('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces(true)
                .build();
        final CSVFormat left = CSVFormat.newBuilder(right)
                .withIgnoreSurroundingSpaces(false)
                .build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsIgnoreEmptyLines() {
        final CSVFormat right = CSVFormat.newBuilder('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(true)
                .build();
        final CSVFormat left = CSVFormat.newBuilder(right)
                .withIgnoreEmptyLines(false)
                .build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsRecordSeparator() {
        final CSVFormat right = CSVFormat.newBuilder('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(true)
                .withRecordSeparator('*')
                .build();
        final CSVFormat left = CSVFormat.newBuilder(right)
                .withRecordSeparator('!')
                .build();

        assertNotEquals(right, left);
    }

    @Test
    public void testEqualsHeader() {
        final CSVFormat right = CSVFormat.newBuilder('\'')
                .withQuoteChar('"')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .withEscape('+')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(true)
                .withRecordSeparator('*')
                .withHeader("One", "Two", "Three")
                .build();
        final CSVFormat left = CSVFormat.newBuilder(right)
                .withHeader("Three", "Two", "One")
                .build();
        
        assertNotEquals(right, left);
    }

    private static void assertNotEquals(final Object right, final Object left) {
        assertFalse(right.equals(left));
        assertFalse(left.equals(right));
    }
}
