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

import java.util.Map;

/**
 * A mutable version of CSVRecord
 * <p>
 * As mutation is generally done within the parent {@link CSVRecord}, this 
 * package-private class just includes more efficient versions of 
 * mutator functions, bypassing the need to make copies 
 * (except in {@link #immutable()}).
 * <p>
 * To enable generating CSVMutableRecord by default, set 
 * {@link CSVFormat#withMutableRecords(boolean)} to <code>true</code>.
 *
 */
final class CSVMutableRecord extends CSVRecord {

    private static final long serialVersionUID = 1L;

    CSVMutableRecord(String[] values, Map<String, Integer> mapping, String comment, long recordNumber,
            long characterPosition) {
        super(values, mapping, comment, recordNumber, characterPosition);
    }

    @Override
    public final CSVMutableRecord withValue(int index, String value) {
    	put(index, value);
    	return this;
    }
    
    @Override
    public final CSVMutableRecord withValue(String name, String value) {
    	put(name, value);
    	return this;
    }
    
    @Override
    final boolean isMutable() {
    	return true;
    }
}
