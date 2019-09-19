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

class Groovy3749Bug extends GroovyTestCase {
    void testScriptsProvidingStaticMainMethod() {
        def scriptStr
        
        // test various signatures of main()
        scriptStr = """
            static main(args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")
        
        scriptStr = """
            static def main(args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")
        
        scriptStr = """
            static void main(args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")

        scriptStr = """
            static main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")

        scriptStr = """
            static def main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")

        scriptStr = """
            static void main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")
        
        // if both main() and the loose statements are provided, then the loose statements should run and not main
        scriptStr = """
            static main(args) {
                throw new RuntimeException('main called')
            }
            throw new Error()
        """
        verifyScriptRun(scriptStr, "Error")
        
        assertScript """
            def main(args) {
                throw new RuntimeException('main called')
            }
        """
    }
    
    void verifyScriptRun(scriptText, expectedFailure) {
        try{
            assertScript(scriptText)
        }catch(Throwable ex) {
            assertTrue ex.class.name.contains(expectedFailure) 
        }
    }
}
