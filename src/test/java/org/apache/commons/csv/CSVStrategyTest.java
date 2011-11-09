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

public class CSVStrategyTest extends TestCase {

    public void testImmutalibity() {
        CSVStrategy strategy1 = new CSVStrategy('!', '!', '!', '!', true, true, true, true);
        CSVStrategy strategy2 = strategy1.withDelimiter('?')
                                         .withEncapsulator('?')
                                         .withCommentStart('?')
                                         .withLineSeparator("?")
                                         .withEscape('?')
                                         .withLeadingSpacesIgnored(false)
                                         .withTrailingSpacesIgnored(false)
                                         .withEmptyLinesIgnored(false)
                                         .withUnicodeEscapesInterpreted(false);

        assertNotSame(strategy1.getDelimiter(), strategy2.getDelimiter());
        assertNotSame(strategy1.getEncapsulator(), strategy2.getEncapsulator());
        assertNotSame(strategy1.getCommentStart(), strategy2.getCommentStart());
        assertNotSame(strategy1.getEscape(), strategy2.getEscape());
        assertNotSame(strategy1.getLineSeparator(), strategy2.getLineSeparator());
        
        assertNotSame(strategy1.isTrailingSpacesIgnored(), strategy2.isTrailingSpacesIgnored());
        assertNotSame(strategy1.isLeadingSpacesIgnored(), strategy2.isLeadingSpacesIgnored());
        assertNotSame(strategy1.isEmptyLinesIgnored(), strategy2.isEmptyLinesIgnored());
        assertNotSame(strategy1.isUnicodeEscapesInterpreted(), strategy2.isUnicodeEscapesInterpreted());
    }

} 
