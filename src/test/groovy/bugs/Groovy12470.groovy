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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Under static compilation, {@code super.m()} invokes the superclass method, as
 * in Java and dynamic Groovy, rather than an extension method of the same name.
 */
final class Groovy12470 {

    @Test
    void testSuperToStringInPlainClass() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                def foo() {
                    super.toString()
                }
                @Override
                String toString() {
                    "FOO: ${super.toString()}"
                }
            }
            assert new Foo().foo() ==~ /Foo@\\p{XDigit}+/
            assert new Foo().toString() ==~ /FOO: Foo@\\p{XDigit}+/
        '''
    }

    @Test
    void testSuperToStringInCollectionSubclass() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic class SL extends ArrayList<Object> { String toString() { 'SL:' + super.toString() } }
            @CompileStatic class SM extends HashMap<String, Object> { String toString() { 'SM:' + super.toString() } }
            class DL extends ArrayList<Object> { String toString() { 'DL:' + super.toString() } }
            class DM extends HashMap<String, Object> { String toString() { 'DM:' + super.toString() } }

            assert (new SL() << [k: 1]).toString() == 'SL:[{k=1}]'
            assert (new DL() << [k: 1]).toString() == 'DL:[{k=1}]'
            assert new SM(k: [1]).toString() == 'SM:{k=[1]}'
            assert new DM(k: [1]).toString() == 'DM:{k=[1]}'
        '''
    }
}
