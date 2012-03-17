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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import static org.junit.Assert.*;

public class CSVFormatTest {

    @Test
    public void testImmutalibity() {
        CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, true, "\r\n", null);
        
        format.withDelimiter('?');
        format.withEncapsulator('?');
        format.withCommentStart('?');
        format.withLineSeparator("?");
        format.withEscape('?');
        format.withLeadingSpacesIgnored(false);
        format.withTrailingSpacesIgnored(false);
        format.withEmptyLinesIgnored(false);
        
        assertEquals('!', format.getDelimiter());
        assertEquals('!', format.getEncapsulator());
        assertEquals('!', format.getCommentStart());
        assertEquals('!', format.getEscape());
        assertEquals("\r\n", format.getLineSeparator());
        
        assertTrue(format.isLeadingSpacesIgnored());
        assertTrue(format.isTrailingSpacesIgnored());
        assertTrue(format.isEmptyLinesIgnored());
    }

    @Test
    public void testMutators() {
        CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, true, "\r\n", null);
        
        assertEquals('?', format.withDelimiter('?').getDelimiter());
        assertEquals('?', format.withEncapsulator('?').getEncapsulator());
        assertEquals('?', format.withCommentStart('?').getCommentStart());
        assertEquals("?", format.withLineSeparator("?").getLineSeparator());
        assertEquals('?', format.withEscape('?').getEscape());
        
        assertFalse(format.withLeadingSpacesIgnored(false).isLeadingSpacesIgnored());
        assertFalse(format.withTrailingSpacesIgnored(false).isTrailingSpacesIgnored());
        assertFalse(format.withSurroundingSpacesIgnored(false).isLeadingSpacesIgnored());
        assertFalse(format.withSurroundingSpacesIgnored(false).isTrailingSpacesIgnored());
        assertFalse(format.withEmptyLinesIgnored(false).isEmptyLinesIgnored());
    }

    @Test
    public void testFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        
        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }
    
    @Test
    public void testValidation() {
        CSVFormat format = CSVFormat.DEFAULT;
        
        try {
            format.withDelimiter('\n');
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withEscape('\r');
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withEncapsulator('\n');
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withCommentStart('\r');
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withDelimiter('!').withEscape('!').validate();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withDelimiter('!').withCommentStart('!').validate();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withEncapsulator('!').withCommentStart('!').validate();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        format.withEncapsulator(CSVFormat.DISABLED).withCommentStart(CSVFormat.DISABLED).validate();
        
        try {
            format.withEscape('!').withCommentStart('!').validate();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        format.withEscape(CSVFormat.DISABLED).withCommentStart(CSVFormat.DISABLED).validate();
        
        
        try {
            format.withEncapsulator('!').withDelimiter('!').validate();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(CSVFormat.DEFAULT);
        oos.flush();
        oos.close();
        
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        CSVFormat format = (CSVFormat) in.readObject();
        
        assertNotNull(format);
        assertEquals("delimiter", CSVFormat.DEFAULT.getDelimiter(), format.getDelimiter());
        assertEquals("encapsulator", CSVFormat.DEFAULT.getEncapsulator(), format.getEncapsulator());
        assertEquals("comment start", CSVFormat.DEFAULT.getCommentStart(), format.getCommentStart());
        assertEquals("line separator", CSVFormat.DEFAULT.getLineSeparator(), format.getLineSeparator());
        assertEquals("escape", CSVFormat.DEFAULT.getEscape(), format.getEscape());
        assertEquals("trim left", CSVFormat.DEFAULT.isLeadingSpacesIgnored(), format.isLeadingSpacesIgnored());
        assertEquals("trim right", CSVFormat.DEFAULT.isTrailingSpacesIgnored(), format.isTrailingSpacesIgnored());
        assertEquals("empty lines", CSVFormat.DEFAULT.isEmptyLinesIgnored(), format.isEmptyLinesIgnored());
    }
} 
