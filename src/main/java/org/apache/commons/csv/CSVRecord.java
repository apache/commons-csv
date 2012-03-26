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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * A CSV record
 */
public class CSVRecord implements Serializable, Iterable<String> {
    
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /** The values of the record */
    private final String[] values;

    /** The column name to index mapping. */
    private final Map<String, Integer> mapping;

    CSVRecord(String[] values, Map<String, Integer> mapping) {
        this.values = values != null ? values : EMPTY_STRING_ARRAY;
        this.mapping = mapping;
    }

    /**
     * Returns a value by index.
     *
     * @param i the index of the column retrieved
     */
    public String get(int i) {
        return values[i];
    }

    /**
     * Returns a value by name.
     *
     * @param name the name of the column to be retrieved
     * @return the column value, or {@code null} if the column name is not found
     * @throws IllegalStateException if no header mapping was provided
     */
    public String get(String name) {
        if (mapping == null) {
            throw new IllegalStateException("No header was specified, the record values can't be accessed by name");
        }

        Integer index = mapping.get(name);

        return index != null ? values[index.intValue()] : null;
    }

    public Iterator<String> iterator() {
        return Arrays.asList(values).iterator();
    }

    String[] values() {
        return values;
    }

    /**
     * Returns the number of values in this record.
     */
    public int size() {
        return values.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
