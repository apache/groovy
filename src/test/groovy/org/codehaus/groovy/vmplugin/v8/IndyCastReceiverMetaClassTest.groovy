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
package org.codehaus.groovy.vmplugin.v8

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A cast decided by the class of the value must not call {@code getMetaClass()} on it: a class-based proxy of a
 * Groovy class forwards that call to its target, which may not be available, such as a scoped bean outside its scope.
 */
final class IndyCastReceiverMetaClassTest {

    static class Watched {
        static int metaClassCalls
        static boolean failMetaClass

        @Override
        MetaClass getMetaClass() {
            metaClassCalls++
            if (failMetaClass) {
                throw new IllegalStateException('getMetaClass() called')
            }
            InvokerHelper.getMetaClass(Watched)
        }
    }

    static class Flag {
    }

    @BeforeEach
    void reset() {
        Watched.metaClassCalls = 0
        Watched.failMetaClass = false
    }

    private static Watched castToWatched(Object value) {
        Watched watched = value
        watched
    }

    private static boolean castToBoolean(Object value) {
        boolean flag = value
        flag
    }

    private static CharSequence castToCharSequence(Object value) {
        (CharSequence) value
    }

    @Test
    void 'a cast of a value that has the target type does not call getMetaClass'() {
        Watched first = new Watched()
        Watched second = new Watched()
        Watched.metaClassCalls = 0

        assertSame first, castToWatched(first)
        assertSame second, castToWatched(second)
        assertEquals 0, Watched.metaClassCalls
    }

    @Test
    void 'a cast succeeds when getMetaClass of the value fails'() {
        Watched watched = new Watched()
        Watched.failMetaClass = true

        assertSame watched, castToWatched(watched)
    }

    @Test
    void 'a cast to boolean still selects asBoolean through the metaclass of each value'() {
        Flag falsy = new Flag()
        falsy.metaClass.asBoolean = { -> false }
        Flag truthy = new Flag()
        truthy.metaClass.asBoolean = { -> true }

        assertFalse castToBoolean(falsy)
        assertTrue castToBoolean(truthy)
        assertFalse castToBoolean(falsy)
    }

    @Test
    void 'a cast site still rejects a value of another class'() {
        assertEquals 'a', castToCharSequence('a')
        assertEquals 'b', castToCharSequence(new StringBuilder('b')).toString()
        assertThrows(GroovyCastException) { castToCharSequence(1) }
    }
}
