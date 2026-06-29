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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * The plain {@code withDefault} decorator grows a map for any looked-up key, which can
 * silently insert a key incompatible with the map's declared key type — only surfacing
 * later (e.g. when reading {@code keySet()}). The key-checked {@code withDefault(Map, Class, Closure)}
 * overload (built over {@code asChecked}) rejects an incompatible key at the point of misuse
 * (GROOVY-11807). The transparent, auto-supplied-{@code Class} form is tracked as GROOVY-12115.
 */
final class Groovy11807 {

    @Test
    void testKeyCheckedWithDefaultRejectsIncompatibleKey() {
        assertScript '''
            Map<Number,?> map = [:].withDefault(Number){ null }
            assert map[1] == null                  // a compatible key grows the map
            assert map.get(2) == null
            def err = groovy.test.GroovyAssert.shouldFail(ClassCastException) { map.get('x') }
            assert err.message.contains('Number')
            assert map.keySet().every { it instanceof Number }
            Number k = map.keySet()[0]             // the original repro: no longer a runtime cast failure
            assert k == 1
        '''
    }

    @Test
    void testKeyCheckedWithDefaultGuardsDirectPut() {
        assertScript '''
            def map = [:].withDefault(Number){ 0 }
            map[1] = 10
            groovy.test.GroovyAssert.shouldFail(ClassCastException) { map['x'] = 1 }
            assert map.keySet().every { it instanceof Number }
        '''
    }

    @Test
    void testKeyAndValueCheckedWithDefault() {
        assertScript '''
            def map = [:].withDefault(Number, String){ 'def' }
            assert map[1] == 'def'                                              // grows with compatible key/value
            groovy.test.GroovyAssert.shouldFail(ClassCastException) { map.get('x') }  // incompatible key
            groovy.test.GroovyAssert.shouldFail(ClassCastException) { map[2] = 99 }   // incompatible value
        '''
    }

    @Test
    void testPlainWithDefaultRemainsLenient() {
        assertScript '''
            def map = [:].withDefault{ null }
            map.get('x')                           // unchanged behaviour: silently grows, no exception
            assert map.containsKey('x')
        '''
    }

    @Test
    void testKeyCheckedWithDefaultUnderCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            String check() {
                Map<Number,?> map = [:].withDefault(Number){ null }
                assert map[1] == null
                groovy.test.GroovyAssert.shouldFail(ClassCastException) { map.get('x') }
                'ok'
            }
            assert check() == 'ok'
        '''
    }
}
