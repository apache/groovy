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

class SafeNavigationTest extends GroovyTestCase {

    void testNullNavigation() {
        def x = null
        def y = x?.bar
        assert y == null
    }

    void testNormalPropertyNavigation() {
        def x = ['a':456, 'foo':['bar':123, 'x':456], 'z':99]
        def y = x?.foo?.bar
        assert y == 123
    }

    void testNullPropertyNavigation() {
        def x = null
        def y = x?.foo?.bar
        assert y == null

        def Date d = null
        def t = d?.time
        assert t == null
    }
    
    void testNormalMethodCall() {
        def x = 1234
        def y = x?.toString()
        assert y == "1234"
    }

    void testNullMethodCall() {
        def x = null
        def y = x?.toString()
        assert y == null
    }

    void testNewLine() {
        def x = [ a:1, b:2 ]
        def y = x
                 .y
                 ?.toString()
        assert y == null
        def m = 'toString'
        def z = x
                 .a
                 ?."$m"()
        assert z == '1'
    }

    // ------------------------------------
    // GROOVY-5479
    private checkDouble(x) {
        x?.toString()
    }

    void testCachedSafeNavigation() {
        assert checkDouble(1234)!=null
        assert checkDouble(null)==null
    }
    // ------------------------------------

}
