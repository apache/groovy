/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.BugsSTCTest

/**
 * Unit tests for static type checking : bugs.
 *
 * @author Cedric Champeau
 */
@Mixin(StaticCompilationTestSupport)
class BugsStaticCompileTest extends BugsSTCTest {

    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }

    void testGroovy5498PropertyAccess() {
        assertScript '''
            class Test {

              List getListVar() {
                new ArrayList()
              }

              void someMethod() {
                 def t = new Object()
                 t = this

                 t.getListVar()     //No error here
                 t.listVar          //error is being reported here
                 assert t.listVar == t.getListVar()
              }
            }
            new Test().someMethod()
        '''
    }

    // GROOVY-5512
    void testCreateRangeInInnerClass() {
        def shell = new GroovyShell()
        shell.evaluate '''
            class Outer {
                static class Inner {
                    @groovy.transform.CompileStatic
                    int m() {
                        int x = 0
                        for (int i in 1..10) {x++}
                        x
                    }
                }
            }
            assert new Outer.Inner().m() == 10
        '''
    }

    // GROOVY-5526
    void testAssertEqualsShouldNotThrowVerifyError() {
        assertScript '''
            import static org.junit.Assert.*;
            import groovy.transform.CompileStatic;

            class CompilerBugs {

              public static void main(String[] args) {
                int expected = 0
                assertEquals(expected, args.length)
              }

            }
        '''
    }

    // GROOVY-5529
    void testStaticCompilationOfClosureWhenSingleMethodAnnotated() {
        new GroovyShell().evaluate '''import groovy.transform.ASTTest
        import static org.codehaus.groovy.control.CompilePhase.*

        interface Row {
            int getKey()
        }

        class RowImpl implements Row {
            int getKey() { 1 }
        }

        @groovy.transform.CompileStatic
        def test() {
            def rows = [new RowImpl(), new RowImpl(), new RowImpl()]

            rows.each { Row row ->
                println row.key
            }
        }

        test()
        '''
    }

    // GROOVY-5536
    void testShouldNotThrowVerifyErrorWithNullDereferenceInIf() {
        assertScript '''
                boolean getDescriptorForPlugin(File pluginDir) {
                    if (pluginDir?.exists()) { true } else { false }
                }
                assert getDescriptorForPlugin(null) == false
        '''
    }

    // GROOVY-
    void testPowerShouldNotThrowVerifyError() {
        assertScript '''int squarePlusOne(int num) {
                num ** num + 1
            }
            assert squarePlusOne(2) == 5
        '''
    }
}

