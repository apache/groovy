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
package groovy

import groovy.test.GroovyTestCase

class PrimitiveTypeFieldTest extends GroovyTestCase {
    private long longField
    private static short shortField

    void setValue() {
        longField = 1
    }

    def getValue() {
        def x = longField
        return x
    }

    void testPrimitiveField() {
        setValue()

        def value = getValue()
        assert value == 1

        assert longField == 1
    }

    void testIntParamBug() {
        assert bugMethod(123) == 246
        assert bugMethod2(123) == 246

        // GROOVY-133
        def closure = {int x-> x * 2 }
        assert closure.call(123) == 246

    }

    int bugMethod(int x) {
        x * 2
    }

    def bugMethod2(int x) {
        x * 2
    }
    void testStaticPrimitiveField() {
        shortField = (Short) 123

        assert shortField == 123
    }

    void testIntLocalVariable() {
        int x = 123
        def y = x + 1
        assert y == 124
    }

    void testLongLocalVariable() {
        long x = 123
        def y = x + 1
        assert y == 124
    }
}
