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
package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.Test

final class NestHostTests {

    private List<Class> compileScript(String script) {
        new CompilationUnit().with {
            addSource('script', script)
            compile(Phases.CLASS_GENERATION)
            classes.collect { classLoader.defineClass(it.name, it.bytes) }
        }
    }

    @Test
    void testNestHost0() {
        def types = compileScript('print "hello world"')

        types.each { type ->
            assert type.nestHost.name == 'script'
            assert type.nestMembers*.name == ['script']
        }
    }

    @Test
    void testNestHost1() {
        def types = compileScript('class C { }')

        types.each { type ->
            assert type.nestHost.name == 'C'
            assert type.nestMembers*.name == ['C']
        }
    }

    @Test
    void testNestHost2() {
        def types = compileScript('class C { class D { } }')

        types.each { type ->
            assert type.nestHost.name == 'C'
            assert type.nestMembers*.name.sort() == ['C', 'C$D']
        }
    }

    @Test
    void testNestHost3() {
        def types = compileScript '''
            class C {
                static class D {
                    def aic = new Object() {}
                }
                interface I {
                    interface J {
                    }
                }
                enum E {
                }
            }
        '''

        types.each { type ->
            assert type.nestHost.name == 'C'
            assert type.nestMembers*.name.sort() == ['C', 'C$D', 'C$D$1', 'C$E', 'C$I', 'C$I$J']
        }
    }

    @Test
    void testNestHost4() {
        def types = compileScript '''
            class C {
                def closure = { -> }
                static class D {
                    def closure = { -> }
                }
            }
        '''

        types.init().each { type ->
            assert type.nestHost.name == 'C'
            if (Runtime.version().feature() >= 15)
            assert type.nestMembers*.name.sort() == ['C', 'C$D', 'C$_closure1', /* TODO: 'C$D$_closure1'*/]
        }
        types.last().with { type -> // TODO
            assert type.nestHost.name == 'C$D$_closure1'
            if (Runtime.version().feature() >= 15) assert type.nestMembers*.name.sort() == ['C$D$_closure1']
        }
    }
}
