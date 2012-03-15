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

import org.junit.Assert;
import org.junit.Test;

public class CSVFormatTest {

    @Test
    public void testImmutalibity() {
        CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, true, true, "\r\n");
        
        format.withDelimiter('?');
        format.withEncapsulator('?');
        format.withCommentStart('?');
        format.withLineSeparator("?");
        format.withEscape('?');
        format.withLeadingSpacesIgnored(false);
        format.withTrailingSpacesIgnored(false);
        format.withEmptyLinesIgnored(false);
        format.withUnicodeEscapesInterpreted(false);
        
        Assert.assertEquals('!', format.getDelimiter());
        Assert.assertEquals('!', format.getEncapsulator());
        Assert.assertEquals('!', format.getCommentStart());
        Assert.assertEquals('!', format.getEscape());
        Assert.assertEquals("\r\n", format.getLineSeparator());
        
        Assert.assertEquals(true, format.isLeadingSpacesIgnored());
        Assert.assertEquals(true, format.isTrailingSpacesIgnored());
        Assert.assertEquals(true, format.isEmptyLinesIgnored());
        Assert.assertEquals(true, format.isUnicodeEscapesInterpreted());
    }

    @Test
    public void testMutators() {
        CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, true, true, "\r\n");
        
        Assert.assertEquals('?', format.withDelimiter('?').getDelimiter());
        Assert.assertEquals('?', format.withEncapsulator('?').getEncapsulator());
        Assert.assertEquals('?', format.withCommentStart('?').getCommentStart());
        Assert.assertEquals("?", format.withLineSeparator("?").getLineSeparator());
        Assert.assertEquals('?', format.withEscape('?').getEscape());
        
        Assert.assertEquals(false, format.withLeadingSpacesIgnored(false).isLeadingSpacesIgnored());
        Assert.assertEquals(false, format.withTrailingSpacesIgnored(false).isTrailingSpacesIgnored());
        Assert.assertEquals(false, format.withSurroundingSpacesIgnored(false).isLeadingSpacesIgnored());
        Assert.assertEquals(false, format.withSurroundingSpacesIgnored(false).isTrailingSpacesIgnored());
        Assert.assertEquals(false, format.withEmptyLinesIgnored(false).isEmptyLinesIgnored());
        Assert.assertEquals(false, format.withUnicodeEscapesInterpreted(false).isUnicodeEscapesInterpreted());
    }

    @Test
    public void testFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        
        Assert.assertEquals("", format.format());
        Assert.assertEquals("a,b,c", format.format("a", "b", "c"));
        Assert.assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }
    
    @Test
    public void testValidation() {
        CSVFormat format = CSVFormat.DEFAULT;
        
        try {
            format.withDelimiter('\n');
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withEscape('\r');
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withEncapsulator('\n');
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withCommentStart('\r');
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withDelimiter('!').withEscape('!').validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withDelimiter('!').withCommentStart('!').validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            format.withEncapsulator('!').withCommentStart('!').validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        format.withEncapsulator(CSVFormat.DISABLED).withCommentStart(CSVFormat.DISABLED).validate();
        
        try {
            format.withEscape('!').withCommentStart('!').validate();
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        format.withEscape(CSVFormat.DISABLED).withCommentStart(CSVFormat.DISABLED).validate();
        
        
        try {
            format.withEncapsulator('!').withDelimiter('!').validate();
            Assert.fail();
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
        
        Assert.assertNotNull(format);
        Assert.assertEquals("delimiter", CSVFormat.DEFAULT.getDelimiter(), format.getDelimiter());
        Assert.assertEquals("encapsulator", CSVFormat.DEFAULT.getEncapsulator(), format.getEncapsulator());
        Assert.assertEquals("comment start", CSVFormat.DEFAULT.getCommentStart(), format.getCommentStart());
        Assert.assertEquals("line separator", CSVFormat.DEFAULT.getLineSeparator(), format.getLineSeparator());
        Assert.assertEquals("escape", CSVFormat.DEFAULT.getEscape(), format.getEscape());
        Assert.assertEquals("unicode escape", CSVFormat.DEFAULT.isUnicodeEscapesInterpreted(), format.isUnicodeEscapesInterpreted());
        Assert.assertEquals("trim left", CSVFormat.DEFAULT.isLeadingSpacesIgnored(), format.isLeadingSpacesIgnored());
        Assert.assertEquals("trim right", CSVFormat.DEFAULT.isTrailingSpacesIgnored(), format.isTrailingSpacesIgnored());
        Assert.assertEquals("empty lines", CSVFormat.DEFAULT.isEmptyLinesIgnored(), format.isEmptyLinesIgnored());
    }
} 
