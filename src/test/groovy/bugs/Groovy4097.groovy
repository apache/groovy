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

import groovy.test.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy4097 {

    @Test
    void testMethodSelection1() {
        assertScript '''
            class C {
                String getSomething(Class<?> clazz) {
                    'Class: ' + clazz.simpleName
                }
                String getSomething(String string) {
                    'String: ' + string
                }
            }

            C c = new C()

            def method = c.getMetaClass().getMetaMethod('getSomething', String)
            def result = method.invoke(c, 'string')
            assert result == 'String: string'

            method = c.getMetaClass().getMetaMethod('getSomething', Class)
            result = method.invoke(c, String)
            assert result == 'Class: String'
        '''
    }

    @NotYetImplemented @Test
    void testMethodSelection2() {
        assertScript '''
            abstract class A {}
            class C extends A {
                static String getSuperclass() {
                    'This was the wrong method'
                }
            }

            C c = new C()
            def registry = GroovySystem.getMetaClassRegistry()
            MetaClass mc = registry.getMetaClass(c.getClass())
            registry.setMetaClass(c.getClass(), new DelegatingMetaClass(mc))

            Class  cls = c.getClass()
            assert cls.getSuperclass() == A.class
        '''
    }
}
