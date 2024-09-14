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
import java.util.Formatter;
import java.util.IllegalFormatException;

/**
 * Signals a CSV exception. For example, this exception is thrown when parsing invalid input.
 *
 * @since 1.12.0
 */
public class CSVException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance with a formatted message.
     *
     * @param format A {@link Formatter} format string.
     * @param args   See {@link String#format(String, Object...)}.
     * @throws IllegalFormatException See {@link String#format(String, Object...)}.
     */
    public CSVException(final String format, final Object... args) {
        super(String.format(format, args));
    }

}
