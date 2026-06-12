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
package bugs;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for a Java subclass of {@link Closure} that overrides
 * {@code call(Object)} (a documented public entry point) without providing
 * a {@code doCall} method. Such subclasses must remain dispatchable from the
 * DGM helpers that internally invoke the closure via {@code call(Object[])}.
 */
final class JavaClosureSubclassCallOverrideBug {

    static final class CallObjectOverride extends Closure<Object> {
        CallObjectOverride() { super(null, null); }
        @Override
        public Object call(Object arg) {
            return ((Integer) arg) % 2 == 1;
        }
    }

    static final class CallNoArgOverride extends Closure<Object> {
        int invocations;
        CallNoArgOverride() { super(null, null); }
        @Override
        public Object call() {
            invocations++;
            return invocations;
        }
    }

    static final class CallObjectOverrideWithDoCall extends Closure<Object> {
        CallObjectOverrideWithDoCall() { super(null, null); }
        @Override
        public Object call(Object arg) {
            // Wraps doCall: matches Groovy 5 dispatch where MOP "call" picked the most specific override.
            return "wrapped:" + doCall(arg);
        }
        public Object doCall(Object arg) {
            return arg;
        }
    }

    static final class DoCallOnly extends Closure<Object> {
        DoCallOnly() { super(null, null); }
        public Object doCall(Object arg) {
            return ((Integer) arg) > 2;
        }
    }

    @Test
    void countHonoursOverriddenCallObject() {
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        Number result = DefaultGroovyMethods.count((Iterable) nums, new CallObjectOverride());
        assertEquals(4, result.intValue());
    }

    @Test
    void findAllHonoursOverriddenCallObject() {
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> odd = DefaultGroovyMethods.findAll(nums, new CallObjectOverride());
        assertEquals(Arrays.asList(1, 3, 5), odd);
    }

    @Test
    void anyHonoursOverriddenCallObject() {
        assertTrue(DefaultGroovyMethods.any((Iterable) Arrays.asList(2, 4, 7), new CallObjectOverride()));
    }

    @Test
    void directVarargsCallHonoursOverriddenCallObject() {
        // BooleanClosureWrapper-style invocation: Java caller passes Object[] to call(Object...).
        Closure<Object> c = new CallObjectOverride();
        assertEquals(Boolean.TRUE, c.call(new Object[]{1}));
        assertEquals(Boolean.FALSE, c.call(new Object[]{2}));
    }

    @Test
    void directVarargsCallHonoursOverriddenNoArgCall() {
        CallNoArgOverride c = new CallNoArgOverride();
        c.call(new Object[0]);
        c.call(new Object[0]);
        assertEquals(2, c.invocations);
    }

    @Test
    void overriddenCallObjectIsPreferredOverDoCall() {
        // Matches Groovy 5 semantics: MOP "call" resolved to the most specific override
        // (call(Object)) and never reached doCall unless the override delegated to it.
        Closure<Object> c = new CallObjectOverrideWithDoCall();
        assertEquals("wrapped:42", c.call(new Object[]{42}));
    }

    @Test
    void doCallOnlySubclassUnaffected() {
        // No call override -> existing MOP doCall path must continue to work unchanged.
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);
        Number result = DefaultGroovyMethods.count((Iterable) nums, new DoCallOnly());
        assertEquals(3, result.intValue());
    }
}
