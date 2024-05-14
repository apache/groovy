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
package groovy.bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

final class Groovy6954 extends AbstractBytecodeTestCase {

    // GROOVY-11376
    void testSetMapDotField() {
        assertScript '''import groovy.transform.*
            @CompileStatic
            class M extends TreeMap<String,String> {
                def           a
                public        b
                protected     c
                @PackageScope d
                private       e

                def testThis() {
                    this.a = 'a'
                    this.b = 'b'
                    this.c = 'c'
                    this.d = 'd'
                    this.e = 'e'
                }
                def testThat(M that) {
                    that.a = 'a'
                    that.b = 'b'
                    that.c = 'c'
                    that.d = 'd'
                    that.e = 'e'
                }
            }

            def map = new M()
            map.testThis()
            assert map.isEmpty()

            def other = new M()
            map.testThat(other)
            assert map.isEmpty()
            assert other.toString() == '[d:d, e:e]'
        '''
    }

    void testSetMapDotProperty() {
        extractionOptions.method = 'put'

        assertScript '''
            @groovy.transform.CompileStatic
            def put(Map<String, ?> map) {
                if (false) map.boo = -1
                map.foo = 'bar'
            }
            def map = [:]
            assert put(map) == 'bar'
            assert map.foo  == 'bar'
            assert !map.containsKey('boo')
        '''

        assert sequence.hasSequence([
            'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;', // boxing -1
            'INVOKEINTERFACE java/util/Map.put ' // not ScriptBytecodeAdapter.setProperty
        ], sequence.indexOf('--BEGIN--'))
    }

    void testSafeSetMapDotProperty() {
        extractionOptions.method = 'put'

        assertScript '''
            @groovy.transform.CompileStatic
            def put(Map<String, ?> map) {
                map?.foo = 'bar'
            }
            assert put(null) == 'bar'
        '''

        assert sequence.hasStrictSequence([
            'IFNULL L1',
            'LDC "foo"',
            'ALOAD 3',
            'INVOKEINTERFACE java/util/Map.put ',
            'L1'
        ])
    }

    void testChainSetMapDotProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            def put(Map<String, ?> map) {
                map.foo = map.bar = 'baz'
            }
            def map = [:]
            assert put(map) == 'baz'
            assert map.foo  == 'baz'
            assert map.bar  == 'baz'
        '''
    }
}
