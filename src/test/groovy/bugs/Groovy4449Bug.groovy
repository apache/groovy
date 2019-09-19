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
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy4449Bug extends GroovyTestCase {
    void testVarArgAsNotTheLastParameter() {
        try {
            new GroovyShell().parse """
                def foo(String... strs, int i) { println i }
                
                foo("me", "you", 42)
            """
            fail('The compilation should have failed as the var-arg parameter is not the last one.')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("The var-arg parameter strs must be the last parameter")
        }
    }

    void testVarArgAsTheLastParameter() {
        new GroovyShell().parse """
            def foo(int i, String... strs) { println i }
            
            foo(42, "me", "you")
        """
    }
}