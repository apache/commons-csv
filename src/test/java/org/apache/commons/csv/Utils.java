/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.csv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Utility methods for test cases
 */
final class Utils {

    /**
     * Checks if the 2d array has the same contents as the list of records.
     *
     * @param message  the message to be displayed
     * @param expected the 2d array of expected results
     * @param actual   the List of {@link CSVRecord} entries, each containing an array of values
     * @param maxRows  the maximum number of rows expected, less than or equal to zero means no limit.
     */
    public static void compare(final String message, final String[][] expected, final List<CSVRecord> actual, final long maxRows) {
        final long expectedLength = maxRows > 0 ? Math.min(maxRows, expected.length) : expected.length;
        assertEquals(expectedLength, actual.size(), message + "  - outer array size");
        for (int i = 0; i < expectedLength; i++) {
            assertArrayEquals(expected[i], actual.get(i).values(), message + " (entry " + i + ")");
        }
    }

    /**
     * Creates an input stream, with or without a BOM.
     */
    static InputStream createUtf8Input(final byte[] baseData, final boolean addBom) {
        byte[] data = baseData;
        if (addBom) {
            data = new byte[baseData.length + 3];
            data[0] = (byte) 0xEF;
            data[1] = (byte) 0xBB;
            data[2] = (byte) 0xBF;
            System.arraycopy(baseData, 0, data, 3, baseData.length);
        }
        return new ByteArrayInputStream(data);
    }

    private Utils() {
    }
}
