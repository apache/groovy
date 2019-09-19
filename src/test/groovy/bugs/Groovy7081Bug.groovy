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

import groovy.test.GroovyTestCase

class Groovy7081Bug extends GroovyTestCase {
    void testShouldSeeThatMethodIsNotImplemented() {
        def msg = shouldFail '''
            interface DefinesProperty {
                String getName()
            }
            class Foo implements DefinesProperty {
                static String name = 'Foo'
            }
            new Foo().name
            '''

        assert msg.contains("The method 'java.lang.String getName()' is already defined in class 'Foo'")
    }

    void testShouldSeeConflictInTypeSignature() {
        def msg = shouldFail '''
            interface DefinesProperty {
                String getName()
            }
            class Foo implements DefinesProperty {
                static int name = 666
            }
            new Foo().name
            '''

        assert msg.contains("Abstract method 'java.lang.String getName()' is not implemented but a method of the same name but different return type is defined: static method 'int getName()'")
    }

    void testShouldSeeConflictUsingTrait() {
        def msg = shouldFail '''
            trait SomeTrait {
                int getMagicNumber() {
                    42
                }
            }
            class SomeClass implements SomeTrait {
                static magicNumber = 'Forty Two'
            }
        '''
        assert msg.contains("Abstract method 'int getMagicNumber()' is not implemented but a method of the same name but different return type is defined: static method 'java.lang.Object getMagicNumber()'")
    }
}
