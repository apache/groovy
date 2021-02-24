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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy6938 {
    @Test
    void testPlaceholderResolutionForSuperMethodCall() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'J.java')
            a.write '''
                public class J <T extends Number> {
                    public T doSomething() {
                        return null;
                    }
                }
            '''
            def b = new File(parentDir, 'G.groovy')
            b.write '''
                import groovy.transform.ASTTest
                import groovy.transform.CompileStatic
                import org.codehaus.groovy.ast.expr.MethodCallExpression
                import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE

                @CompileStatic
                class G extends J<Integer> {
                    Integer doSomething() {
                        @ASTTest(phase=CLASS_GENERATION, value={
                            def expr = node.rightExpression
                            assert expr instanceof MethodCallExpression
                            assert expr.objectExpression.text == 'super'

                            def type = expr.objectExpression.getNodeMetaData(INFERRED_TYPE)
                            assert type.toString(false) == 'J <Integer>' // was "J<T>"

                            type = node.leftExpression.getNodeMetaData(INFERRED_TYPE)
                            assert type.toString(false) == 'java.lang.Integer'
                        })
                        def result = super.doSomething()
                        return result
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            def result = loader.loadClass('G').newInstance().doSomething()
            assert result == null
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
