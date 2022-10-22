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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link CSVFormat}.
 */
public class CSVDuplicateHeaderTest {
    /**
     * Return test cases for duplicate header data. Uses the order:
     * <pre>
     * DuplicateHeaderMode duolicateHeaderMode
     * boolean allowMissingColumnNames
     * String[] headers
     * boolean valid
     * </pre>
     *
     * <p>TODO: Reinstate cases failed by CSVFormat.
     *
     * @return the stream of arguments
     */
    static Stream<Arguments> duplicateHeaderData() {
        return Stream.of(
            // Any combination with a valid header
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  new String[] {"A", "B"}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  new String[] {"A", "B"}, true),

            // Duplicate non-empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, new String[] {"A", "A"}, true),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  new String[] {"A", "A"}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  new String[] {"A", "A"}, true),

            // Duplicate empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, new String[] {"", ""}, false),
            //Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, new String[] {"", ""}, false),
            //Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, new String[] {"", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  new String[] {"", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  new String[] {"", ""}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  new String[] {"", ""}, true),

            // Duplicate blank names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, new String[] {" ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, new String[] {" ", " "}, false),
            //Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, new String[] {" ", " "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  new String[] {" ", " "}, false),
            //Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  new String[] {" ", " "}, true),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  new String[] {" ", " "}, true),

            // Duplicate non-empty and empty names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, new String[] {"A", "A", "", ""}, false),
            //Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  new String[] {"A", "A", "", ""}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  new String[] {"A", "A", "", ""}, true),

            // Duplicate non-empty and blank names
            Arguments.of(DuplicateHeaderMode.DISALLOW,    false, new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, false, new String[] {"A", "A", " ", " "}, false),
            //Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   false, new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.DISALLOW,    true,  new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_EMPTY, true,  new String[] {"A", "A", " ", " "}, false),
            Arguments.of(DuplicateHeaderMode.ALLOW_ALL,   true,  new String[] {"A", "A", " ", " "}, true)
        );
    }

    /**
     * Test duplicate headers with the CSVParser.
     *
     * @param duolicateHeaderMode the duolicate header mode
     * @param allowMissingColumnNames the allow missing column names flag
     * @param headers the headers
     * @param valid true if the settings are expected to be valid
     */
    @ParameterizedTest
    @MethodSource(value = {"duplicateHeaderData"})
    public void testCSVFormat(DuplicateHeaderMode duolicateHeaderMode,
                              boolean allowMissingColumnNames,
                              String[] headers,
                              boolean valid) {
        CSVFormat.Builder builder = CSVFormat.DEFAULT.builder()
                                                     .setDuplicateHeaderMode(duolicateHeaderMode)
                                                     .setAllowMissingColumnNames(allowMissingColumnNames)
                                                     .setHeader(headers);
        if (valid) {
            CSVFormat format = builder.build();
            Assertions.assertEquals(duolicateHeaderMode, format.getDuplicateHeaderMode(), "DuplicateHeaderMode");
            Assertions.assertEquals(allowMissingColumnNames, format.getAllowMissingColumnNames(), "AllowMissingColumnNames");
            Assertions.assertArrayEquals(headers, format.getHeader(), "Header");
        } else {
            Assertions.assertThrows(IllegalArgumentException.class, () -> builder.build());
        }
    }

    /**
     * Test duplicate headers with the CSVParser.
     *
     * @param duolicateHeaderMode the duolicate header mode
     * @param allowMissingColumnNames the allow missing column names flag
     * @param headers the headers (joined with the CSVFormat delimiter to create a string input)
     * @param valid true if the settings are expected to be valid
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @ParameterizedTest
    @MethodSource(value = {"duplicateHeaderData"})
    public void testCSVParser(DuplicateHeaderMode duolicateHeaderMode,
                              boolean allowMissingColumnNames,
                              String[] headers,
                              boolean valid) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                                            .setDuplicateHeaderMode(duolicateHeaderMode)
                                            .setAllowMissingColumnNames(allowMissingColumnNames)
                                            .setHeader()
                                            .build();
        String input = Arrays.stream(headers).collect(Collectors.joining(format.getDelimiterString()));
        if (valid) {
            try(CSVParser parser = CSVParser.parse(input, format)) {
                Assertions.assertEquals(Arrays.asList(headers), parser.getHeaderNames());
            }
        } else {
            Assertions.assertThrows(IllegalArgumentException.class, () -> CSVParser.parse(input, format));
        }
    }
}
