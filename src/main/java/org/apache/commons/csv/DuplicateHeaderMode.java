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

/**
 * Determines how duplicate header fields should be handled
 * if {@link CSVFormat.Builder#setHeader(Class)} is not null.
 *
 * @since 1.10.0
 */
public enum DuplicateHeaderMode {

    /**
     * Allows all duplicate headers.
     */
    ALLOW_ALL,

    /**
     * Allows duplicate headers only if they're empty, blank, or null strings.
     */
    ALLOW_EMPTY,

    /**
     * Disallows duplicate headers entirely.
     */
    DISALLOW
}
