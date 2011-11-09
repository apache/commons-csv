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
        CSVFormat format1 = new CSVFormat('!', '!', '!', '!', true, true, true, true);
        CSVFormat format2 = format1.withDelimiter('?')
                                         .withEncapsulator('?')
                                         .withCommentStart('?')
                                         .withLineSeparator("?")
                                         .withEscape('?')
                                         .withLeadingSpacesIgnored(false)
                                         .withTrailingSpacesIgnored(false)
                                         .withEmptyLinesIgnored(false)
                                         .withUnicodeEscapesInterpreted(false);

        assertNotSame(format1.getDelimiter(), format2.getDelimiter());
        assertNotSame(format1.getEncapsulator(), format2.getEncapsulator());
        assertNotSame(format1.getCommentStart(), format2.getCommentStart());
        assertNotSame(format1.getEscape(), format2.getEscape());
        assertNotSame(format1.getLineSeparator(), format2.getLineSeparator());
        
        assertNotSame(format1.isTrailingSpacesIgnored(), format2.isTrailingSpacesIgnored());
        assertNotSame(format1.isLeadingSpacesIgnored(), format2.isLeadingSpacesIgnored());
        assertNotSame(format1.isEmptyLinesIgnored(), format2.isEmptyLinesIgnored());
        assertNotSame(format1.isUnicodeEscapesInterpreted(), format2.isUnicodeEscapesInterpreted());
    }

} 
