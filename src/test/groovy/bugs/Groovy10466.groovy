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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

class Groovy10466 {

    protected field

    @Test
    void testClassOrder() {
        def config = new CompilerConfiguration(targetDirectory: File.createTempDir())

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                import org.codehaus.groovy.ast.FieldNode

                class A extends B {
                    @groovy.transform.ASTTest(value={
                        def mce = node.code.statements[0].expression
                        def var = mce.arguments[0] // "field" variable
                        assert var.accessedVariable instanceof FieldNode
                    })
                    void test() {
                        print field
                    }
                }
            '''
            def b = new File(parentDir, 'B.groovy')
            b.write '''
                class B extends groovy.bugs.Groovy10466 {
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
