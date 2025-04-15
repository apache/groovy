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
package org.codehaus.groovy.runtime.typehandling

import groovy.test.GroovyTestCase

import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToClass
import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToString
import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToChar
import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToEnum

class ShortTypeHandlingTest extends GroovyTestCase {

    void testCastToClass() {
        assert castToClass(null) == null
        assert castToClass(Integer.class) == Integer.class
        assert castToClass('java.lang.String') == String.class
        shouldFail(GroovyCastException) {
            castToClass(Collections.emptyList())
        }
    }

    void testCastToString() {
        assert castToString(null) == null
        assert castToString(String.class) == 'class java.lang.String'
        assert castToString(List.class) == 'interface java.util.List'
    }

    void testCastToCharacter() {
        assert castToChar(null) == null
        char c = (char)'c'
        assert castToChar((Object)c) == c
        assert castToChar(Integer.valueOf(99)) == c
        assert castToChar("${c}") == c
        assert castToChar('c') == c
        shouldFail(GroovyCastException) {
            castToChar(new Date())
        }
    }

    void testCastToEnum() {
        assert castToEnum(null, TestStages) == null
        assert castToEnum((Object)TestStages.AFTER_CLASS, TestStages) == TestStages.AFTER_CLASS
        assert castToEnum("BEFORE_TEST", TestStages) == TestStages.BEFORE_TEST
    }

    enum TestStages {
        BEFORE_CLASS, BEFORE_TEST, TEST, AFTER_TEST, AFTER_CLASS
    }
}
