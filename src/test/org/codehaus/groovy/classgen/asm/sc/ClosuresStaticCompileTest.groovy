/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ClosuresSTCTest

/**
 * Unit tests for static compilation: closures.
 *
 * @author Cedric Champeau
 */
@Mixin(StaticCompilationTestSupport)
class ClosuresStaticCompileTest extends ClosuresSTCTest {

    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }

    // GROOVY-5584
    void testEachOnMapClosure() {
        assertScript '''
                def test() {
                    def result = ""
                    [a:1, b:3].each { key, value -> result += "$key$value" }
                    assert result == "a1b3"
                }
                test()'''
    }

    // GROOVY-5811
    void testClosureExceptionUnwrap() {
        assertScript '''
            @groovy.transform.InheritConstructors
            public class MyException extends Exception {}

            void foo() {
                throw new MyException()
            }

            void bar() {
                boolean caught = false
                try {
                    def cl = { foo() }
                    cl()
                } catch (MyException e) {
                    caught = true
                } finally {
                    assert caught
                }
            }

            bar()
        '''
    }
}

