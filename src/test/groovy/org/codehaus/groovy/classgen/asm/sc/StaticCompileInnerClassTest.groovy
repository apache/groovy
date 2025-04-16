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

class StaticCompileInnerClassTest extends AbstractBytecodeTestCase {
    void testStaticCompileCallToOwnerField() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            class Config {
                String path
                class Inner {
                    String m() { path }
                }
                String foo() { new Inner().m() }
            }
            def c = new Config(path:'/tmp')
            assert c.foo() == '/tmp'
        ''')
        clazz.newInstance().main()

    }

    void testStaticCompileCallToOwnerMethod() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            class Config {
                String path
                class Inner {
                    String m() { bar() }
                }
                String bar() { path }
                String foo() { new Inner().m() }
            }
            def c = new Config(path:'/tmp')
            assert c.foo() == '/tmp'
        ''')
        clazz.newInstance().main()

    }

    void testStaticCompileCallToOwnerPrivateMethod() {

        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            class Config {
                String path
                class Inner {
                    String m() { bar() }
                }
                private String foo() { new Inner().m() }
                String bar() { path }
            }
            def c = new Config(path:'/tmp')
            assert c.foo() == '/tmp'
        ''')
        clazz.newInstance().main()

        /*assert bytecode.hasStrictSequence(
                ['public m()V', 'L0', 'RETURN']
        )*/
    }

    void testAccessPrivateMemberFromAnotherInnerClass() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                private static class Inner1 {
                    private static final int CONST_1 = 123
                }
                private static class Inner2 {
                    private static final int CONST_2 = 2*Inner1.CONST_1
                }
            }
            assert A.Inner2.CONST_2 == 246
        '''
    }
}
