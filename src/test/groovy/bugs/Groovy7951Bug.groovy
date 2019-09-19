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
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.classgen.GeneratorContext

import static org.codehaus.groovy.control.CompilePhase.CANONICALIZATION
import static org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION

class Groovy7951Bug extends GroovyTestCase {

    void testTransformMethodCallExpressionPassesGenericTypes() {

        def config = new CompilerConfiguration()

        // Just visiting the transform method on each expression is enough to verify
        // that the checker is able to see the generic types in the later phase.
        def transformer = new CompilationCustomizer(CANONICALIZATION) {
            @Override void call(SourceUnit src, GeneratorContext ctxt, ClassNode cn) {
                new ClassCodeExpressionTransformer() {
                    @Override SourceUnit getSourceUnit() { src }
                }.visitClass(cn)
            }
        }

        boolean assertWasChecked
        def checker = new CompilationCustomizer(INSTRUCTION_SELECTION) {
            void call(SourceUnit src, GeneratorContext ctxt, ClassNode cn) {
                new ClassCodeVisitorSupport() {
                    @Override void visitMethodCallExpression(MethodCallExpression mce) {
                        if (mce.objectExpression.text == 'java.util.Collections' &&
                                mce.method.text == 'emptyList') {
                            assert mce.genericsTypes != null && mce.genericsTypes*.name == ['Date']
                            assertWasChecked = true
                        }
                        super.visitMethodCallExpression(mce)
                    }
                    @Override SourceUnit getSourceUnit() { src }
                }.visitClass(cn)
            }
        }

        config.addCompilationCustomizers(transformer, checker)

        new GroovyShell(config).parse '''
            Collections.<Date>emptyList()
        '''

        assert assertWasChecked
    }

}
