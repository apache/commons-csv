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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests parsing of duplicate column names in a CSV header.
 * The test verifies that headers are consistently handled by CSVFormat and CSVParser.
 */
public class CSVDuplicateHeaderTest {

    /**
     * Return test cases for duplicate header data for use in CSVFormat.
     * <p>
     * This filters the parsing test data to all cases where the allow missing column
     * names flag is true and ignore header case is false: these flags are exclusively for parsing.
     * CSVFormat validation applies to both parsing and writing and thus validation
     * is less strict and behaves as if the allow missing column names constraint and
     * the ignore header case behavior are absent.
     * The filtered data is then returned with the parser flags set to both true and false
     * for each test case.
     * </p>
     *
     * @return the stream of arguments
     */
    static Stream<Arguments> duplicateHeaderAllowsMissingColumnsNamesData() {
        return duplicateHeaderData()
            .filter(arg -> Boolean.TRUE.equals(arg.get()[1]) && Boolean.FALSE.equals(arg.get()[2]))
            .flatMap(arg -> {
                // Return test case with flags as all true/false combinations
                final Object[][] data = new Object[4][];
                final Boolean[] flags = {Boolean.TRUE, Boolean.FALSE};
                int i = 0;
                for (final Boolean a : flags) {
                    for (final Boolean b : flags) {
                        data[i] = arg.get().clone();
                        data[i][1] = a;
                        data[i][2] = b;
                        i++;
                    }
                }
                return Arrays.stream(data).map(Arguments::of);
            });
    }

    /**
     * Return test cases for duplicate header data for use in parsing (CSVParser). Uses the order:
     * <pre>
     * DuplicateHeaderMode duplicateHeaderMode
     * boolean allowMissingColumnNames
     * String[] headers
     * boolean valid
     * </pre>
     *
     * @return the stream of arguments
     */
    static Stream<Arguments> duplicateHeaderData() {
        return Stream.of(
            // Any combination with a valid header
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", "B"}, true),

            // Any combination with a valid header including empty
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", ""}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", ""}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", ""}, true),

            // Any combination with a valid header including blank (1 space)
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", " "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", " "}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", " "}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", " "}, true),

            // Any combination with a valid header including null
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", null}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", null}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", null}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", null}, true),

            // Duplicate non-empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", "A"}, true),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", "A"}, true),

            // Duplicate empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"", ""}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"", ""}, true),

            // Duplicate blank names (1 space)
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {" ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {" ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {" ", " "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {" ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {" ", " "}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {" ", " "}, true),

            // Duplicate blank names (3 spaces)
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"   ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"   ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"   ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"   ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"   ", "   "}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"   ", "   "}, true),

            // Duplicate null names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {null, null}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {null, null}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {null, null}, true),

            // Duplicate blank names (1+3 spaces)
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {" ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {" ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {" ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {" ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {" ", "   "}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {" ", "   "}, true),

            // Duplicate blank names and null names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {" ", null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {" ", null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {" ", null}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {" ", null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {" ", null}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {" ", null}, true),

            // Duplicate non-empty and empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", "A", "", ""}, true),

            // Non-duplicate non-empty and duplicate empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", "B", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", "B", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", "B", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", "B", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", "B", "", ""}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", "B", "", ""}, true),

            // Duplicate non-empty and blank names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", "A", " ", " "}, true),

            // Duplicate non-empty and null names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", "A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", "A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", "A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", "A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", "A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", "A", null, null}, true),

            // Duplicate blank names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", "", ""}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", "", ""}, true),

            // Duplicate null names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", null, null}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", null, null}, true),

            // Duplicate blank names (1+3 spaces)
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, false, new String[] {"A", " ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, false, new String[] {"A", " ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, false, new String[] {"A", " ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  false, new String[] {"A", " ", "   "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  false, new String[] {"A", " ", "   "}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  false, new String[] {"A", " ", "   "}, true),

            // Duplicate names (case insensitive)
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, true , new String[] {"A", "a"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, true , new String[] {"A", "a"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, true , new String[] {"A", "a"}, true),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  true , new String[] {"A", "a"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  true , new String[] {"A", "a"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  true , new String[] {"A", "a"}, true),

            // Duplicate non-empty (case insensitive) and empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, true, new String[] {"A", "a", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, true, new String[] {"A", "a", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, true, new String[] {"A", "a", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  true, new String[] {"A", "a", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  true, new String[] {"A", "a", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  true, new String[] {"A", "a", "", ""}, true),

            // Duplicate non-empty (case insensitive) and blank names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, true, new String[] {"A", "a", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, true, new String[] {"A", "a", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, true, new String[] {"A", "a", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  true, new String[] {"A", "a", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  true, new String[] {"A", "a", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  true, new String[] {"A", "a", " ", " "}, true),

            // Duplicate non-empty (case insensitive) and null names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, true, new String[] {"A", "a", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, true, new String[] {"A", "a", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, true, new String[] {"A", "a", null, null}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  true, new String[] {"A", "a", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  true, new String[] {"A", "a", null, null}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  true, new String[] {"A", "a", null, null}, true)
        );
    }

    /**
     * Tests duplicate headers with the CSVFormat.
     *
     * @param duplicateHeaderMode the duplicate header mode
     * @param allowMissingColumnNames the allow missing column names flag (only used for parsing)
     * @param ignoreHeaderCase the ignore header case flag (only used for parsing)
     * @param headers the headers
     * @param valid true if the settings are expected to be valid, otherwise expect a IllegalArgumentException
     */
    @ParameterizedTest
    @MethodSource(value = {"duplicateHeaderAllowsMissingColumnsNamesData"})
    public void testCSVFormat(final DuplicateHeaderMode duplicateHeaderMode,
                              final boolean allowMissingColumnNames,
                              final boolean ignoreHeaderCase,
                              final String[] headers,
                              final boolean valid) {
        final CSVFormat.Builder builder =
            CSVFormat.DEFAULT.builder()
                             .setDuplicateHeaderMode(duplicateHeaderMode)
                             .setAllowMissingColumnNames(allowMissingColumnNames)
                             .setIgnoreHeaderCase(ignoreHeaderCase)
                             .setHeader(headers);
        if (valid) {
            final CSVFormat format = builder.build();
            Assertions.assertEquals(duplicateHeaderMode, format.getDuplicateHeaderMode(), "DuplicateHeaderMode");
            Assertions.assertEquals(allowMissingColumnNames, format.getAllowMissingColumnNames(), "AllowMissingColumnNames");
            Assertions.assertArrayEquals(headers, format.getHeader(), "Header");
        } else {
            Assertions.assertThrows(IllegalArgumentException.class, builder::build);
        }
    }

    /**
     * Tests duplicate headers with the CSVParser.
     *
     * @param duplicateHeaderMode the duplicate header mode
     * @param allowMissingColumnNames the allow missing column names flag (only used for parsing)
     * @param ignoreHeaderCase the ignore header case flag (only used for parsing)
     * @param headers the headers (joined with the CSVFormat delimiter to create a string input)
     * @param valid true if the settings are expected to be valid, otherwise expect a IllegalArgumentException
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @ParameterizedTest
    @MethodSource(value = {"duplicateHeaderData"})
    public void testCSVParser(final DuplicateHeaderMode duplicateHeaderMode,
                              final boolean allowMissingColumnNames,
                              final boolean ignoreHeaderCase,
                              final String[] headers,
                              final boolean valid) throws IOException {
        final CSVFormat format =
            CSVFormat.DEFAULT.builder()
                             .setDuplicateHeaderMode(duplicateHeaderMode)
                             .setAllowMissingColumnNames(allowMissingColumnNames)
                             .setIgnoreHeaderCase(ignoreHeaderCase)
                             .setNullString("NULL")
                             .setHeader()
                             .build();
        final String input = Arrays.stream(headers)
                .map(s -> s == null ? format.getNullString() : s)
                .collect(Collectors.joining(format.getDelimiterString()));
        if (valid) {
            try(CSVParser parser = CSVParser.parse(input, format)) {
                // Parser ignores null headers
                final List<String> expected =
                    Arrays.stream(headers)
                          .filter(s -> s != null)
                          .collect(Collectors.toList());
                Assertions.assertEquals(expected, parser.getHeaderNames(), "HeaderNames");
            }
        } else {
            Assertions.assertThrows(IllegalArgumentException.class, () -> CSVParser.parse(input, format));
        }
    }
}
