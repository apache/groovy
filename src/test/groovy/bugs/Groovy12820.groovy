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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript

/**
 * A subscript read on an instance of an inner class that has no getAt uses the getAt of its
 * enclosing instance, as dynamic Groovy does, also in a compound assignment (GROOVY-12820).
 */
final class Groovy12820 {

    private static final String BOX = '''
        class Box {
            String id
            Box(String id) { this.id = id }
            String getAt(int i) { 'g' + id + i }
            void putAt(int i, String v) { id = 'put(' + v + ')' }
            MODE
            class In {
                def read(In other) { other[2] }
                def safe(In other) { other?[2] }
                def self() { this[2] }
                void plusAssign(In other) { other[2] += 'w' }
                void increment(In other) { other[2]++ }
                void elvisAssign(In other) { other[2] ?= 'e' }
                class Deep {
                    def read(Deep other) { other[2] }
                }
            }
            In inner() { new In() }
            In.Deep deep() { new In.Deep(new In()) }
            def anonymous() {
                def c = { -> new Object() { def read() { this[2] } }.read() }
                c()
            }
        }
    '''

    @ParameterizedTest
    @ValueSource(strings=['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic'])
    void testSubscriptOfEnclosingInstance(String mode) {
        assertScript BOX.replace('MODE', mode) + '''
            def a = new Box('A'), b = new Box('B')
            assert a.inner().read(b.inner()) == 'gB2'
            assert a.inner().safe(b.inner()) == 'gB2'
            assert a.inner().safe(null) == null
            assert a.inner().self() == 'gA2'
            assert a.deep().read(b.deep()) == 'gB2'
            assert a.anonymous() == 'gA2'

            b = new Box('B'); a.inner().plusAssign(b.inner())
            assert b.id == 'put(gB2w)'
            b = new Box('B'); a.inner().increment(b.inner())
            assert b.id == 'put(gB3)'
            b = new Box('B'); a.inner().elvisAssign(b.inner())
            assert b.id == 'put(gB2)'
            assert a.id == 'A'
        '''
    }
}
