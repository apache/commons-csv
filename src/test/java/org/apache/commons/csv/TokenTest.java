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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link Token}.
 */
public class TokenTest {

    @ParameterizedTest
    @EnumSource(Token.Type.class)
    void testToString(final Token.Type type) {
        // Should never blow up
        final Token token = new Token();
        final String resetName = Token.Type.INVALID.name();
        assertTrue(token.toString().contains(resetName));
        token.reset();
        assertTrue(token.toString().contains(resetName));
        token.type = null;
        assertFalse(token.toString().isEmpty());
        token.reset();
        token.type = type;
        assertTrue(token.toString().contains(type.name()));
        token.content.setLength(1000);
        assertTrue(token.toString().contains(type.name()));
    }
}
