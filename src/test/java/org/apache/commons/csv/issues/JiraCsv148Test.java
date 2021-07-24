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
package org.apache.commons.csv.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.junit.jupiter.api.Test;

public class JiraCsv148Test {

    @Test
    public void testWithIgnoreSurroundingSpacesEmpty() {
        // @formatter:off
        final CSVFormat format = CSVFormat.DEFAULT.builder()
            .setQuoteMode(QuoteMode.ALL)
            .setIgnoreSurroundingSpaces(true)
            .build();
        // @formatter:on
        assertEquals(
            "\"\",\" \",\" Single space on the left\",\"Single space on the right \","
                + "\" Single spaces on both sides \",\"   Multiple spaces on the left\","
                + "\"Multiple spaces on the right   \",\"  Multiple spaces on both sides     \"",
            format.format("", " ", " Single space on the left", "Single space on the right ",
                " Single spaces on both sides ", "   Multiple spaces on the left", "Multiple spaces on the right   ",
                "  Multiple spaces on both sides     "));
    }

    /**
     * The difference between withTrim()and withIgnoreSurroundingSpace()： difference: withTrim() can remove the leading
     * and trailing spaces and newlines in quotation marks, while withIgnoreSurroundingSpace() cannot The same point:
     * you can remove the leading and trailing spaces,tabs and other symbols.
     */
    @Test
    public void testWithTrimEmpty() {
        // @formatter:off
        final CSVFormat format = CSVFormat.DEFAULT.builder()
            .setQuoteMode(QuoteMode.ALL)
            .setTrim(true)
            .build();
        // @formatter:on
        assertEquals(
            "\"\",\"\",\"Single space on the left\",\"Single space on the right\","
                + "\"Single spaces on both sides\",\"Multiple spaces on the left\","
                + "\"Multiple spaces on the right\",\"Multiple spaces on both sides\"",
            format.format("", " ", " Single space on the left", "Single space on the right ",
                " Single spaces on both sides ", "   Multiple spaces on the left", "Multiple spaces on the right   ",
                "  Multiple spaces on both sides     "));
    }
}
