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
import org.codehaus.groovy.control.*
import org.codehaus.groovy.control.messages.*

class Groovy5915Bug extends GroovyTestCase {
    def myTestCode = """void myMethod(){
  void wrongMethod() {
    return
  }
}"""

    void testCorrectEndColumn() {
        def unit = new CompilationUnit(CompilerConfiguration.DEFAULT, null, new GroovyClassLoader(getClass().classLoader))
        unit.addSource('mycode.groovy', myTestCode)

        try {
            unit.compile(Phases.INSTRUCTION_SELECTION)
            fail "Should have reported a compilation error"
        } catch (CompilationFailedException ignored) {
            def collector = unit.errorCollector
            assert collector.errorCount == 1
            def message = collector.getError(0)
            assert message instanceof SyntaxErrorMessage
            println message.cause.startColumn + ' ' + message.cause.startLine
            println message.cause.endColumn + ' ' + message.cause.endLine
            int ec = message.cause.endColumn
            assert ec == 4: "Invalid end column value. Expected: 4, Actual: $ec"
        }
    }
}
