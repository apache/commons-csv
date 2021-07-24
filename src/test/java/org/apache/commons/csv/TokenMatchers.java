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

import static org.hamcrest.core.AllOf.allOf;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Collection of matchers for asserting the type and content of tokens.
 */
final class TokenMatchers {

    public static Matcher<Token> hasContent(final String expectedContent) {
        return new TypeSafeDiagnosingMatcher<Token>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("token has content ");
                description.appendValue(expectedContent);
            }

            @Override
            protected boolean matchesSafely(final Token item,
                    final Description mismatchDescription) {
                mismatchDescription.appendText("token content is ");
                mismatchDescription.appendValue(item.content.toString());
                return expectedContent.equals(item.content.toString());
            }
        };
    }

    public static Matcher<Token> hasType(final Token.Type expectedType) {
        return new TypeSafeDiagnosingMatcher<Token>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("token has type ");
                description.appendValue(expectedType);
            }

            @Override
            protected boolean matchesSafely(final Token item,
                    final Description mismatchDescription) {
                mismatchDescription.appendText("token type is ");
                mismatchDescription.appendValue(item.type);
                return item.type == expectedType;
            }
        };
    }

    public static Matcher<Token> isReady() {
        return new TypeSafeDiagnosingMatcher<Token>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("token is ready ");
            }

            @Override
            protected boolean matchesSafely(final Token item,
                    final Description mismatchDescription) {
                mismatchDescription.appendText("token is not ready ");
                return item.isReady;
            }
        };
    }

    public static Matcher<Token> matches(final Token.Type expectedType, final String expectedContent) {
        return allOf(hasType(expectedType), hasContent(expectedContent));
    }

}
