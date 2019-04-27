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

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy4435Bug extends GroovyTestCase {
    void testAssignmentToAFinalFieldThroughPropertyAccess() {
        try {
            new GroovyShell().parse """
                class Test4435 {
                    private final String prop = ""
                    void setProp(val) {
                        this.prop = val
                    }
                }
            """
            fail('The compilation should have failed as a final field is being assigned to.')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("cannot modify final field 'prop' outside of constructor")
        }
    }
}