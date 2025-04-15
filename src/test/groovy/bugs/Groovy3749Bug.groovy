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

import groovy.test.GroovyTestCase

class Groovy3749Bug extends GroovyTestCase {
    void testScriptsProvidingStaticMainMethod() {
        def scriptStr

        // test various signatures of static main()
        scriptStr = """
            static main(args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        scriptStr = """
            static def main(args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        scriptStr = """
            static void main(args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        scriptStr = """
            static main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        scriptStr = """
            static def main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        scriptStr = """
            static void main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        // if both main() and the loose statements are provided, then the loose statements should run and not main
        scriptStr = """
            static main(args) {
                throw new RuntimeException('main called')
            }
            throw new Error()
        """
        assertScriptFails(scriptStr, "Error")

        scriptStr = """
            static void main() {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        // if param type doesn't match, this main won't execute
        runScript """
            static main(Date args) {
                throw new RuntimeException('main called')
            }
        """
    }

    void testScriptsProvidingInstanceMainMethod() {
        def scriptStr

        // test various signatures of instance main()
        scriptStr = """
            def main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        scriptStr = """
            void main(args) {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        scriptStr = """
            void main() {
                throw new RuntimeException('main called')
            }
        """
        assertScriptFails(scriptStr, "RuntimeException")

        // if param type doesn't match, this main won't execute
        runScript """
            def main(Date args) {
                throw new RuntimeException('main called')
            }
        """
    }

    static void assertScriptFails(scriptText, expectedFailure) {
        try {
            runScript(scriptText)
        } catch (Throwable ex) {
            assert ex.class.name.contains(expectedFailure)
            return
        }
        fail("Expected script to fail with '$expectedFailure' but passed.")
    }

    private static void runScript(String scriptText) {
        new GroovyShell().run(scriptText, 'Groovy3749Snippet', [] as String[])
    }
}
