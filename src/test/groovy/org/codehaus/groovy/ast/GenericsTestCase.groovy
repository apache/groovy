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
package org.codehaus.groovy.ast

import groovy.test.GroovyTestCase
import groovy.transform.TupleConstructor
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer

/**
 * Adds several utility methods which are used in tests on generics.
 */
abstract class GenericsTestCase extends GroovyTestCase {

    protected Map<String, Object> extractTypesFromCode(String string) {
        def result = [generics:[], type:null]
        CompilerConfiguration config = new CompilerConfiguration()
        config.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                def visitor = new GenericsVisitorSupport(source)
                visitor.visitClass(classNode)
                result.clear()
                result.putAll(visitor.result)
            }
        })

        new GroovyShell(config).evaluate(string)

        result
    }

    @TupleConstructor(defaults=false)
    private static class GenericsVisitorSupport extends ClassCodeVisitorSupport {

        final SourceUnit sourceUnit
        private final Map<String, Object> result = [:]

        @Override
        void visitVariableExpression(VariableExpression expression) {
            super.visitVariableExpression(expression)
            if (expression.name == 'type') {
                result.type = expression.type
                result.generics = expression.type.genericsTypes
            }
        }

        @Override
        void visitMethod(MethodNode node) {
            super.visitMethod(node)
            if (node.name == 'type') {
                result.type = node.returnType
                result.generics = node.genericsTypes
            }
        }
    }
}
