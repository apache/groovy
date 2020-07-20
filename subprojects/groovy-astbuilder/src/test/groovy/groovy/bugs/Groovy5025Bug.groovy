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

class Groovy5025Bug extends GroovyTestCase {
    void testDisableAstBuilder() {
        def config = new org.codehaus.groovy.control.CompilerConfiguration()
        config.disabledGlobalASTTransformations = ['org.apache.groovy.ast.builder.AstBuilderTransformation']
        def script = '''
            new org.codehaus.groovy.ast.builder.AstBuilder().buildFromCode { "Hello" }
        '''

        def shell = new GroovyShell()
        assert shell.evaluate(script).class == ArrayList

        shell = new GroovyShell(config)
        shouldFail(IllegalStateException) {
            shell.evaluate(script)
        }
    }
}
