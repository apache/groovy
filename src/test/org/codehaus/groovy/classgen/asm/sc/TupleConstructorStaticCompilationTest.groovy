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
package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class TupleConstructorStaticCompilationTest extends AbstractBytecodeTestCase {
    void testTupleConstructor1() {
        assertScript '''
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
            }

            @groovy.transform.CompileStatic
            Person m() {
                new Person('Cedric','Champeau')
            }
            def p = m()
            assert p.firstName == 'Cedric'
            assert p.lastName == 'Champeau'
        '''
    }

    void testTupleConstructor1WithMissingArgument() {
        assertScript '''
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
                Integer age
            }

            @groovy.transform.CompileStatic
            Person m() {
                new Person('Cedric','Champeau')
            }
            def p = m()
            assert p.firstName == 'Cedric'
            assert p.lastName == 'Champeau'
            assert p.age == null
        '''
    }

    void testTupleConstructorWithMissingArgumentOfSameTypeAsPrevious() {
        assertScript '''
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
                Integer age
                Integer priority
            }

            @groovy.transform.CompileStatic
            Person m() {
                new Person('Cedric','Champeau',32)
            }
            def p = m()
            assert p.firstName == 'Cedric'
            assert p.lastName == 'Champeau'
            assert p.age == 32
            assert p.priority == null
        '''
    }
    
    void testConstructorWithDefaultArgsAndPossibleMessup() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                String val
                Foo(String arg1='foo', String arg2) {
                    arg1+arg2
                }
            }
            new Foo('bar').val == 'foobar'
        '''
    }
}
