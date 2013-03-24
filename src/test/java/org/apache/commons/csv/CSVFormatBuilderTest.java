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
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.LF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.csv.CSVFormat.CSVFormatBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @version $Id$
 */
public class CSVFormatBuilderTest {

    private CSVFormatBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new CSVFormatBuilder('+', Character.valueOf('!'), null, Character.valueOf('#'), Character.valueOf('!'), true, true, CRLF, null);
    }

    @Test
    public void testCommentStart() {
        assertEquals('?', builder.withCommentStart('?').build().getCommentStart().charValue());
    }
    
    @Test
    public void testCopiedFormatIsEqualToOriginal() {
        final CSVFormat copyOfRCF4180 = CSVFormat.newBuilder(RFC4180).build();
        assertEquals(RFC4180, copyOfRCF4180);
        final CSVFormat copy2OfRCF4180 = RFC4180.toBuilder().build();
        assertEquals(RFC4180, copy2OfRCF4180);
    }

    @Test
    public void testCopiedFormatWithChanges() {
        final CSVFormat newFormat = CSVFormat.newBuilder(RFC4180).withDelimiter('!').build();
        assertTrue(newFormat.getDelimiter() != RFC4180.getDelimiter());
        final CSVFormat newFormat2 = RFC4180.toBuilder().withDelimiter('!').build();
        assertTrue(newFormat2.getDelimiter() != RFC4180.getDelimiter());
    }
    
    @Test
    public void testDelimiter() {
        assertEquals('?', builder.withDelimiter('?').build().getDelimiter());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testDelimiterSameAsCommentStartThrowsException() {
        builder.withDelimiter('!').withCommentStart('!').build();
    }

    @Test(expected = IllegalStateException.class)
    public void testDelimiterSameAsEscapeThrowsException() {
        builder.withDelimiter('!').withEscape('!').build();
    }

    @Test
    public void testEscape() {
        assertEquals('?', builder.withEscape('?').build().getEscape().charValue());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEscapeSameAsCommentStartThrowsException() {
        builder.withEscape('!').withCommentStart('!').build();
    }

    @Test(expected = IllegalStateException.class)
    public void testEscapeSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        builder.withEscape(new Character('!')).withCommentStart(new Character('!')).build();
    }

    @Test
    public void testHeaderReferenceCannotEscape() {
        final String[] header = new String[]{"one", "tow", "three"};
        builder.withHeader(header);
        
        final CSVFormat firstFormat = builder.build();
        final CSVFormat secondFormat = builder.build();
        assertNotSame(header, firstFormat.getHeader());
        assertNotSame(firstFormat, secondFormat.getHeader());
    }

    @Test
    public void testIgnoreEmptyLines() {
        assertFalse(builder.withIgnoreEmptyLines(false).build().getIgnoreEmptyLines());
    }

    @Test
    public void testIgnoreSurroundingSpaces() {
        assertFalse(builder.withIgnoreSurroundingSpaces(false).build().getIgnoreSurroundingSpaces());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatCRThrowsException() {
        CSVFormat.newBuilder(CR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewFormatLFThrowsException() {
        CSVFormat.newBuilder(LF);
    }
    
    @Test
    public void testQuoteChar() {
        assertEquals('?', builder.withQuoteChar('?').build().getQuoteChar().charValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testQuoteCharSameAsCommentStartThrowsException() {
        builder.withQuoteChar('!').withCommentStart('!').build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testQuoteCharSameAsCommentStartThrowsExceptionForWrapperType() {
        // Cannot assume that callers won't use different Character objects
        builder.withQuoteChar(new Character('!')).withCommentStart('!').build();
    }

    @Test(expected = IllegalStateException.class)
    public void testQuoteCharSameAsDelimiterThrowsException() {
        builder.withQuoteChar('!').withDelimiter('!').build();
    }

    @Test
    public void testQuotePolicy() {
        assertEquals(Quote.ALL, builder.withQuotePolicy(Quote.ALL).build().getQuotePolicy());
    }

    @Test(expected = IllegalStateException.class)
    public void testQuotePolicyNoneWithoutEscapeThrowsException() {
        CSVFormat.newBuilder('!').withQuotePolicy(Quote.NONE).build();
    }

    @Test
    public void testRecoardSeparator() {
        assertEquals("?", builder.withRecordSeparator("?").build().getRecordSeparator());
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
        builder.withCommentStart(CR).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLFThrowsException() {
        builder.withDelimiter(LF).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCRThrowsExceptions() {
        builder.withEscape(CR).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteLFThrowsException() {
        builder.withQuoteChar(LF).build();
    }
}
