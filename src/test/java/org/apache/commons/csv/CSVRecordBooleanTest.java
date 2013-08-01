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

public class CSVRecordBooleanTest {

    private CSVRecord record;

    @Before
    public void setUp() throws IOException {
        this.record = createTestRecord();
    }

    @Test
    public void testGetBooleanByString() {
        Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(record.getBoolean("A")));
        Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(record.getBoolean("B")));
        Assert.assertEquals(Boolean.FALSE, Boolean.valueOf(record.getBoolean("C")));
        Assert.assertEquals(Boolean.FALSE, Boolean.valueOf(record.getBoolean("D")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBooleanByMissingString() {
        Assert.assertEquals(null, Boolean.valueOf(record.getBoolean("ABSENT")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBooleanByNullString() {
        Assert.assertEquals(null, Boolean.valueOf(record.getBoolean(null)));
    }

    /**
     * @return
     * @throws IOException
     */
    private CSVRecord createTestRecord() throws IOException {
        String csv = "A,B,C,D\ntrue, TRUE, false, foo";
        CSVRecord record = CSVParser.parseString(csv, CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true))
                .iterator().next();
        return record;
    }

}
