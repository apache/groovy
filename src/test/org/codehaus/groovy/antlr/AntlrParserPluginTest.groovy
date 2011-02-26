/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.antlr

import org.codehaus.groovy.ast.builder.AstBuilder

/**
 * Test for AntlrParserPlugin.
 *
 * @author Hamlet D'Arcy
 */
class AntlrParserPluginTest extends GroovyTestCase {

    void testInnerClassLineNumbers() {

        def result = new AstBuilder().buildFromString(org.codehaus.groovy.control.CompilePhase.CONVERSION, false,  '''
            new Object() {

            }
        ''')

        assert result[2].getClass() == org.codehaus.groovy.ast.InnerClassNode
        assert result[2].lineNumber == 2
        assert result[2].lastLineNumber == 4
        assert result[2].columnNumber == 26
        assert result[2].lastColumnNumber == 14

    }
}
