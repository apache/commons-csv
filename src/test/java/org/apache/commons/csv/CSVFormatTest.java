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

import junit.framework.TestCase;

public class CSVFormatTest extends TestCase {

    public void testImmutalibity() {
        CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, true, true);
        
        format.withDelimiter('?');
        format.withEncapsulator('?');
        format.withCommentStart('?');
        format.withLineSeparator("?");
        format.withEscape('?');
        format.withLeadingSpacesIgnored(false);
        format.withTrailingSpacesIgnored(false);
        format.withEmptyLinesIgnored(false);
        format.withUnicodeEscapesInterpreted(false);
        
        assertEquals('!', format.getDelimiter());
        assertEquals('!', format.getEncapsulator());
        assertEquals('!', format.getCommentStart());
        assertEquals('!', format.getEscape());
        assertEquals("\r\n", format.getLineSeparator());
        
        assertEquals(true, format.isLeadingSpacesIgnored());
        assertEquals(true, format.isTrailingSpacesIgnored());
        assertEquals(true, format.isEmptyLinesIgnored());
        assertEquals(true, format.isUnicodeEscapesInterpreted());
    }

    public void testMutators() {
        CSVFormat format = new CSVFormat('!', '!', '!', '!', true, true, true, true);
        
        assertEquals('?', format.withDelimiter('?').getDelimiter());
        assertEquals('?', format.withEncapsulator('?').getEncapsulator());
        assertEquals('?', format.withCommentStart('?').getCommentStart());
        assertEquals("?", format.withLineSeparator("?").getLineSeparator());
        assertEquals('?', format.withEscape('?').getEscape());
        
        assertEquals(false, format.withLeadingSpacesIgnored(false).isLeadingSpacesIgnored());
        assertEquals(false, format.withTrailingSpacesIgnored(false).isTrailingSpacesIgnored());
        assertEquals(false, format.withSurroundingSpacesIgnored(false).isLeadingSpacesIgnored());
        assertEquals(false, format.withSurroundingSpacesIgnored(false).isTrailingSpacesIgnored());
        assertEquals(false, format.withEmptyLinesIgnored(false).isEmptyLinesIgnored());
        assertEquals(false, format.withUnicodeEscapesInterpreted(false).isUnicodeEscapesInterpreted());
    }

    public void testFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        
        assertEquals("", format.format());
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"x,y\",z", format.format("x,y", "z"));
    }
} 
