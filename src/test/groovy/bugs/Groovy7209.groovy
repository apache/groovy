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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-7209: a {@code metaClass} override of an instance method was
 * ignored when that method was invoked from the constructor; the
 * original method body still ran.
 */
final class Groovy7209 {

    @Test
    void testMetaClassOverrideHonoredInConstructor() {
        assertScript '''
            class Hello {
                Hello() {
                    Foo()
                }
                void Foo() {
                    throw new AssertionError('original Foo should not run')
                }
            }

            boolean methodFooWasCalled = false
            Hello.metaClass.Foo = { -> methodFooWasCalled = true }
            new Hello()
            assert methodFooWasCalled
        '''
    }

    @Test
    void testMetaClassOverrideHonoredInConstructorOfPrecompiledClass() {
        assertScript '''
            def parentDir = File.createTempDir()
            try {
                new File(parentDir, 'Hello.groovy').text = \'\'\'
                    class Hello {
                        Hello() { Foo() }
                        void Foo() {
                            throw new AssertionError('original Foo should not run')
                        }
                    }
                \'\'\'
                def gcl = new GroovyClassLoader()
                gcl.addClasspath(parentDir.absolutePath)
                gcl.parseClass(new File(parentDir, 'Hello.groovy'))
                def shell = new GroovyShell(gcl)
                shell.evaluate(\'''
                    boolean methodFooWasCalled = false
                    Hello.metaClass.Foo = { -> methodFooWasCalled = true }
                    new Hello()
                    assert methodFooWasCalled
                \''')
            } finally {
                parentDir.deleteDir()
            }
        '''
    }
}
