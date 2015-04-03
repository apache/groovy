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

import groovy.transform.stc.ConstructorsSTCTest

/**
 * Unit tests for static compilation: constructors.
 *
 * @author Cedric Champeau
 */
class StaticCompileConstructorsTest extends ConstructorsSTCTest implements StaticCompilationTestSupport {

    void testMapConstructorError() {
        assertScript '''import groovy.transform.Canonical

        class WTF {
            public static void main(String[] args) {
                new Person(name:"First")
                first(new Person(name:"First"))
            }

            static Person first(Person p) {
                p
            }

        }

        @Canonical
        class Person {
            String name
        }
        WTF.main()
        '''
    }
    
    void testMixedDynamicStaticConstructor() {
        shouldFailWithMessages("""
            class A{}
            class B {
                A a = new A();
                @groovy.transform.CompileStatic
                B(){}
            }
        """, "Cannot statically compile constructor implicitly including non static elements from object initializers, properties or fields")
    }
}

