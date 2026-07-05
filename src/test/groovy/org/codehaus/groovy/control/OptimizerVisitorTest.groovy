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
package org.codehaus.groovy.control

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.stmt.Statement
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt

/**
 * GROOVY-12131: constant caching communicates the assigned {@code $const$} field name by
 * mutating the {@link ConstantExpression}, which assumes each node is reachable from exactly
 * one class. An AST transform that aliases a node into a second class makes the classes
 * overwrite each other's stamp, so one class silently reads the wrong cached constant at
 * runtime. The visitor now warns when it re-stamps a node with a different field name.
 */
class OptimizerVisitorTest {

    private static ClassNode classWithCode(String name, Statement... statements) {
        def cn = new ClassNode(name, Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        cn.addMethod('m', Opcodes.ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, block(statements))
        cn
    }

    private static List<String> optimize(ClassNode... classes) {
        def sourceUnit = SourceUnit.create('OptimizerVisitorTest', 'placeholder')
        def visitor = new OptimizerVisitor(null)
        classes.each { visitor.visitClass(it, sourceUnit) }
        def collector = sourceUnit.errorCollector
        (0..<collector.warningCount).collect { collector.getWarning(it).message }
    }

    @Test
    void sharedConstantNodeStampedWithDivergingNamesWarns() {
        // A numbers the shared 2.5 node $const$1 (behind -9.9); B re-stamps it $const$0
        def shared = new ConstantExpression(2.5G)
        def a = classWithCode('A', stmt(new ConstantExpression(-9.9G)), stmt(shared))
        def b = classWithCode('B', stmt(shared))

        def warnings = optimize(a, b)
        assert warnings.any { it.contains('$const$') && it.contains('B') }
    }

    @Test
    void distinctNodesWithEqualValuesDoNotWarn() {
        // nested/sibling classes share constant values, never nodes — each class caches its own
        def a = classWithCode('A', stmt(new ConstantExpression(-9.9G)), stmt(new ConstantExpression(2.5G)))
        def b = classWithCode('B', stmt(new ConstantExpression(2.5G)))

        assert optimize(a, b).isEmpty()
    }

    @Test
    void sharedNodeStampedWithSameNameDoesNotWarn() {
        // coincidentally identical numbering is harmless: each class reads its own field,
        // which holds the same value
        def shared = new ConstantExpression(2.5G)
        def a = classWithCode('A', stmt(shared))
        def b = classWithCode('B', stmt(shared))

        assert optimize(a, b).isEmpty()
    }
}
