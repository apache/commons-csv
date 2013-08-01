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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CSVRecordLongTest {

    private CSVRecord record;

    /**
     * @return
     * @throws IOException
     */
    private CSVRecord createTestRecord() throws IOException {
        String csv = "A, B, C, D, E\n-1, 0, 1, " + Long.MAX_VALUE + ", " + Long.MIN_VALUE;
        CSVRecord record = CSVParser.parseString(csv, CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true))
                .iterator().next();
        return record;
    }

    @Before
    public void setUp() throws IOException {
        this.record = createTestRecord();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLongByMissingString() {
        Assert.assertEquals(null, Long.valueOf(record.getLong("ABSENT")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLongByNullString() {
        Assert.assertEquals(null, Long.valueOf(record.getLong(null)));
    }

    @Test
    public void testGetLongByString() {
        Assert.assertEquals(-1, record.getLong("A"));
        Assert.assertEquals(0, record.getLong("B"));
        Assert.assertEquals(1, record.getLong("C"));
        Assert.assertEquals(Long.MAX_VALUE, record.getLong("D"));
        Assert.assertEquals(Long.MIN_VALUE, record.getLong("E"));
    }

}
