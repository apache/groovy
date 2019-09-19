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
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * GROOVY-3463:
 * Spring/CGLIB proxies throw exception "object is not an instance of declaring class"
 */
class Groovy3464Bug extends GroovyTestCase {

    GroovyShell shell
    CompilerConfiguration config
    File targetDir, stubDir

    protected void setUp() {
        super.setUp()

        config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        def groovyFile = new File(config.targetDirectory, 'GroovyThing.groovy')
        def javaFile = new File(config.targetDirectory, 'JavaThing.java')

        groovyFile << '''
            class GroovyThing {
                String m1() { "thing.m1" }
                String m2() { m1() + " called from thing.m2"}
            }
            '''

        javaFile << '''
            public class JavaThing extends GroovyThing {
                public String m3() {
                    return "javaThing.m3 calling m2 " + m2();
                }
            }
            '''

        def loader = new GroovyClassLoader(this.class.classLoader)
        def cu = new JavaAwareCompilationUnit(config, loader)
        cu.addSources([groovyFile, javaFile] as File[])
        try {
            cu.compile()
        } catch (any) {
            any.printStackTrace()
            assert false, "Compilation of the Groovy and Java files should have succeeded"
        }

        this.shell = new GroovyShell(loader)

    }

    protected void tearDown() {
        config.targetDirectory?.deleteDir()
        config.jointCompilationOptions?.stubDir?.deleteDir()
        targetDir?.deleteDir()
        stubDir?.deleteDir()

        super.tearDown()
    }

    void testScenarioOne() {
        shouldFail(MissingMethodException) {
            shell.evaluate '''
                def t = new GroovyThing()
                assert t.m3() == "javaThing.m3 calling m2 thing.m1 called from thing.m2"

                t = new JavaThing()
                t.m3()
                assert false, "Method m3() should not be found"
            '''
        }
    }

    void testScenarioTwo() {
        shell.evaluate '''
            def t = new GroovyThing()
            assert t.m2() == "thing.m1 called from thing.m2"

            t = new JavaThing()
            assert t.m3() == "javaThing.m3 calling m2 thing.m1 called from thing.m2"

        '''
    }

    static File createTempDir() {
        File tempDirectory = File.createTempDir("Groovy3464Bug", Long.toString(System.currentTimeMillis()))
        return tempDirectory
    }
}
