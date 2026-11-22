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
package org.codehaus.groovy.transform.sc.transformers

import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

import static groovy.test.GroovyAssert.shouldFail

final class MethodCallExpressionTransformerTest {

    private final StaticCompilationTransformer transformer = newTransformer(new GroovyClassLoader())

    @Test
    void superMopCallRequiresDirectMethodCallTarget() {
        ClassNode base = new ClassNode('Base', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        ClassNode sub = new ClassNode('Sub', Opcodes.ACC_PUBLIC, base)
        MethodNode superMethod = base.addMethod('greet', Opcodes.ACC_PUBLIC, ClassHelper.STRING_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)

        MethodCallExpression withTarget = new MethodCallExpression(new VariableExpression('super'), 'greet', MethodCallExpression.NO_ARGUMENTS)
        withTarget.putNodeMetaData(StaticTypesMarker.SUPER_MOP_METHOD_REQUIRED, sub)
        withTarget.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, superMethod)

        def rewritten = transformer.transform(withTarget)
        assert rewritten instanceof MethodCallExpression
        assert ((MethodCallExpression) rewritten).methodAsString == 'super$2$greet'
        assert ((MethodCallExpression) rewritten).methodTarget.declaringClass == sub

        MethodCallExpression withoutTarget = new MethodCallExpression(new VariableExpression('super'), 'greet', MethodCallExpression.NO_ARGUMENTS)
        withoutTarget.putNodeMetaData(StaticTypesMarker.SUPER_MOP_METHOD_REQUIRED, sub)

        def err = shouldFail(GroovyBugError) {
            transformer.transform(withoutTarget)
        }
        assert err.message.contains('Missing direct method call target for super MOP call')
    }

    private static StaticCompilationTransformer newTransformer(final GroovyClassLoader loader) {
        CompilerConfiguration conf = new CompilerConfiguration()
        SourceUnit unit = new SourceUnit('MethodCall.groovy', '', conf, loader, new ErrorCollector(conf))
        ClassNode owner = new ClassNode('MethodCall', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        new StaticCompilationTransformer(unit, new StaticCompilationVisitor(unit, owner))
    }
}
