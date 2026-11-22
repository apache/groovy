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

import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

/**
 * GROOVY-8426: the BlockStatement generated for a method body must use the
 * line and column range of the whole block, not those of the first statement
 * inside it.
 */
final class Groovy8426 {

    @Test
    void testMethodBodyBlockCoversTheWholeBlock() {
        def src = '''
            def method() {
                'return value'

            }
        '''
        def cu = new CompilationUnit(new CompilerConfiguration())
        cu.addSource('Groovy8426Script.groovy', src)
        cu.compile(Phases.CONVERSION)
        def method = cu.ast.modules[0].classes[0].getDeclaredMethods('method')[0]
        def block = method.code
        def first = ((BlockStatement) block).statements[0]

        assert block.lineNumber == 2
        assert block.columnNumber == 26
        assert block.lastLineNumber == 5
        assert block.lastColumnNumber == 14
        assert first.lineNumber == 3
        assert block.lineNumber != first.lineNumber || block.columnNumber != first.columnNumber
        assert block.lastLineNumber > first.lastLineNumber
    }
}
