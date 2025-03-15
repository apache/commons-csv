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

import static org.apache.commons.csv.Token.Type.INVALID;

/**
 * Internal token representation.
 * <p>
 * This is used as a contract between the lexer and the parser.
 * </p>
 */
final class Token {

    enum Type {
        /** Token has no valid content, that is, is in its initialized state. */
        INVALID,

        /** Token with content, at the beginning or in the middle of a line. */
        TOKEN,

        /** Token (which can have content) when the end of file is reached. */
        EOF,

        /** Token with content when the end of a line is reached. */
        EORECORD,

        /** Token is a comment line. */
        COMMENT
    }

    /** Length of the initial token (content-)buffer */
    private static final int DEFAULT_CAPACITY = 50;

    /** Token type */
    Token.Type type = INVALID;

    /** The content buffer, never null. */
    final StringBuilder content = new StringBuilder(DEFAULT_CAPACITY);

    /** Token ready flag: indicates a valid token with content (ready for the parser). */
    boolean isReady;

    boolean isQuoted;

    void reset() {
        content.setLength(0);
        type = INVALID;
        isReady = false;
        isQuoted = false;
    }

    /**
     * Converts the token state to a string to ease debugging.
     *
     * @return a string helpful for debugging.
     */
    @Override
    public String toString() {
        return type + " [" + content.toString() + "]";
    }
}
