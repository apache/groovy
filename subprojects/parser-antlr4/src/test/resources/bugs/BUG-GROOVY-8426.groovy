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


import groovy.transform.CompileDynamic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import static org.codehaus.groovy.control.CompilePhase.CONVERSION

@CompileDynamic
class Groovy8426Test {
    void testMethodBlockStatement() {
        def result = new AstBuilder().buildFromString CONVERSION, false, '''
            def method() {
                'return value'
                
            }
        '''

        ClassNode classNode = result[1]
        MethodNode method = classNode.getMethods('method')[0]
        BlockStatement statement = method.code

        assert statement.lineNumber == 2
        assert statement.lastLineNumber == 5
        assert statement.columnNumber == 26
        assert statement.lastColumnNumber == 14
    }
}

new Groovy8426Test().testMethodBlockStatement()
