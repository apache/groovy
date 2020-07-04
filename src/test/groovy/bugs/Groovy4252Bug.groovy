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

import groovy.test.GroovyShellTestCase
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy4252Bug extends GroovyShellTestCase {
    void testClosureListExprUsageInvalid1() {
        try {
            shell.parse """
                [].bar(1;2;3)
            """
            fail("The compilation should have failed as expression list of form (a;b;c) is not supported in this context")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("Expression list of the form (a; b; c) is not supported in this context") ||
                    syntaxError.message.contains("Unexpected input: '('")
        }
    }

    void testClosureListExprUsageInvalid2() {
        try {
            shell.parse """
                def x = (1;2;3)
            """
            fail("The compilation should have failed as expression list of form (a;b;c) is not supported in this context")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("Expression list of the form (a; b; c) is not supported in this context") ||
                    syntaxError.message.contains("Unexpected input: ';'")
        }
    }

    void testClosureListExprUsageInvalid3() {
        try {
            shell.parse """
                [].for(1;2;3){println "in loop"}
            """
            fail("The compilation should have failed as expression list of form (a;b;c) is not supported in this context")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("Expression list of the form (a; b; c) is not supported in this context") ||
                    syntaxError.message.contains("Unexpected input: '('")
        }
    }

    void testClosureListExprUsageInvalid4() {
        try {
            shell.parse """
                class Crasher {
                    public void m() {
                        def fields = [1,2,3]
                        def expectedFieldNames = ["patentnumber", "status"].
                        for (int i=0; i<fields.size(); i++) {
                            Object f = fields[i] 
                            System.out.println(f); 
                        }
                    }
                }
            """
            fail("The compilation should have failed as expression list of form (a;b;c) is not supported in this context")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("Expression list of the form (a; b; c) is not supported in this context") ||
                    syntaxError.message.contains("Unexpected input:") || syntaxError.message.contains("Missing ')'")
        }
    }

    void testClosureListExprUsageValid() {
        shell.parse """
            for(int i = 0; i < 5; i++){println i}
        """
    }
}