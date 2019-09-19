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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.ResourceGroovyMethods

/**
 * Test for GROOVY-4386: Using static imports to a script on the classpath.
 */
class Groovy4386_Bug extends GroovyTestCase {
    private static final scriptSource = """
        package foo
        class Constants {
            public static final PI = 3.14
            static final TWOPI = 6.28
        }
    """
    File tmpDir

    @Override protected void setUp() {
        tmpDir = File.createTempDir()
        File dir = new File(tmpDir, 'foo')
        dir.mkdir()
        File file = new File(dir, "Constants.groovy")
        ResourceGroovyMethods.setText(file, scriptSource);
    }

    @Override protected void tearDown() {
        tmpDir.deleteDir()
    }

    void testAccessPublicStaticField() {
        assertScript """
            import static foo.Constants.PI
            class Test {
                static test() {
                    assert PI == 3.14
                }
            }
            Test.test()
        """
    }

    void testAccessStaticProperty() {
        assertScript """
            import static foo.Constants.TWOPI
            class Test {
                static test() {
                    assert TWOPI == 6.28
                }
            }
            Test.test()
        """
    }

    void testStarImport() {
        assertScript """
            import static foo.Constants.*
            class Test {
                static test() {
                    assert TWOPI == PI + PI
                }
            }
            Test.test()
        """
    }

    void assertScript(String script) {
        GroovyShell shell = new GroovyShell(new CompilerConfiguration(classpath: tmpDir.path))
        shell.evaluate(script, getTestClassName())
    }
}
