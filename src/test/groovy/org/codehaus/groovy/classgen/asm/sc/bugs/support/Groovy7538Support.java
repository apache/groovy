/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen.asm.sc.bugs.support;

import java.util.Objects;

/*
 * Test classes extracted and adapted from the AssertJ project.
 */
public class Groovy7538Support {
    public static AbstractCharSequenceAssert<?, String> assertThat(String actual) {
        return new StringAssert(actual);
    }

    public static class StringAssert extends AbstractCharSequenceAssert<StringAssert, String> {
        protected StringAssert(String actual) {
            super(actual, StringAssert.class);
        }
    }

    public static abstract class AbstractCharSequenceAssert<S extends AbstractCharSequenceAssert<S, A>, A extends CharSequence>
            extends AbstractAssert<S, A> {
        protected AbstractCharSequenceAssert(A actual, Class<?> selfType) {
            super(actual, selfType);
        }

        public S isNotEmpty() {
            assertNotEmpty(actual);
            return myself;
        }

        public void assertNotEmpty(CharSequence actual) {
            assertNotNull(actual);
            if (hasContents(actual)) {
                return;
            }
            throw new IllegalArgumentException("Expecting actual not to be empty");
        }

        private void assertNotNull(CharSequence actual) {
            if (actual != null) {
                return;
            }
            throw new IllegalArgumentException("Expecting actual not to be null");
        }

        private static boolean hasContents(CharSequence s) {
            return s.length() > 0;
        }
    }

    public static abstract class AbstractAssert<S extends AbstractAssert<S, A>, A> {
        protected final A actual;
        protected final S myself;

        protected AbstractAssert(A actual, Class<?> selfType) {
            myself = (S) selfType.cast(this);
            this.actual = actual;
        }

        public S isNotEqualTo(Object other) {
            assertNotEqual(actual, other);
            return myself;
        }

        private void assertNotEqual(Object actual, Object other) {
            if (!equal(actual, other)) {
                return;
            }
            throw new IllegalArgumentException("Expecting actual not to be equal to other");
        }

        private static boolean equal(Object actual, Object other) {
            return Objects.equals(actual, other);
        }
    }

    public void assertString() {
        assertThat("true").isNotEmpty().isNotEqualTo("false");
        assertThat("true").isNotEqualTo("false").isNotEmpty();
    }
}
