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
package groovy

import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.classgen.InstanceofFlowBindings
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.junit.jupiter.api.Test

final class InstanceofFlowBindingsTest {

    @Test
    void testBindingsOfPositiveInstanceof() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s'))
        assert b.whenTrue()*.name == ['s']
        assert b.whenFalse().isEmpty()
        assert b.allNames() as List == ['s']
    }

    @Test
    void testBindingsOfNegatedInstanceof() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s)'))
        assert b.whenTrue().isEmpty()
        assert b.whenFalse()*.name == ['s']
        assert b.allNames() as List == ['s']
    }

    @Test
    void testBindingsOfAnd() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s && s.length() > 0'))
        assert b.whenTrue()*.name == ['s']
        assert b.whenFalse().isEmpty()
    }

    @Test
    void testBindingsOfOr() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String s || s.length() > 0'))
        assert b.whenTrue().isEmpty()
        assert b.whenFalse().isEmpty()
        assert b.isEmpty()
    }

    @Test
    void testBindingsOfNegatedOrFalsePath() {
        def b = InstanceofFlowBindings.of(parseCondition('!(o instanceof String s) || s.isEmpty()'))
        // left false-path binds s; OR false bindings = left.whenFalse ∪ right.whenFalse = {s}
        assert b.whenFalse()*.name == ['s']
        assert b.whenTrue().isEmpty()
    }

    @Test
    void testBindingsOfPlainInstanceofAreEmpty() {
        def b = InstanceofFlowBindings.of(parseCondition('o instanceof String'))
        assert b.isEmpty()
    }

    @Test
    void testOfNullIsEmpty() {
        assert InstanceofFlowBindings.of(null).isEmpty()
    }

    @Test
    void testContainsPattern() {
        assert InstanceofFlowBindings.containsPattern(parseCondition('o instanceof String s'))
        assert InstanceofFlowBindings.containsPattern(parseCondition('!(o instanceof String s)'))
        assert InstanceofFlowBindings.containsPattern(parseCondition('o instanceof String s && s'))
        assert !InstanceofFlowBindings.containsPattern(parseCondition('o instanceof String'))
        assert !InstanceofFlowBindings.containsPattern(null)
    }

    @Test
    void testContainsPatternNestedInCall() {
        def expr = parseMethodArg('m(o instanceof String s)')
        assert InstanceofFlowBindings.containsPattern(expr)
    }

    @Test
    void testRightOfOrIsDynamicVariable() {
        def src = '''
            class C {
                def m(Object o) {
                    if (o instanceof String s || s.length() > 0) {
                        return 1
                    }
                    return 0
                }
            }
        '''
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.SEMANTIC_ANALYSIS)
        def accesses = []
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitVariableExpression(VariableExpression ve) {
                if (ve.name == 's') {
                    accesses << ve.accessedVariable
                }
                super.visitVariableExpression(ve)
            }
        })
        assert accesses.any { it instanceof DynamicVariable } :
                "expected DynamicVariable for RHS of ||, got: ${accesses*.class*.simpleName}"
    }

    @Test
    void testNegatedIfBranchIsDynamicVariable() {
        def src = '''
            class C {
                def m(Object o) {
                    if (!(o instanceof String s)) {
                        return s
                    }
                    return null
                }
            }
        '''
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.SEMANTIC_ANALYSIS)
        def accesses = []
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitVariableExpression(VariableExpression ve) {
                if (ve.name == 's') {
                    accesses << ve.accessedVariable
                }
                super.visitVariableExpression(ve)
            }
        })
        // declaration + use in if-branch (must be DynamicVariable)
        assert accesses.count { it instanceof DynamicVariable } >= 1
    }

    private static Expression parseCondition(String condition) {
        def src = """
            class C {
                def m(Object o) {
                    if ($condition) return 1
                    return 0
                }
            }
        """
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.CONVERSION)
        IfStatement ifStmt = null
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitIfElse(IfStatement statement) {
                ifStmt = statement
            }
        })
        ifStmt.booleanExpression.expression
    }

    private static Expression parseMethodArg(String statement) {
        def src = """
            class C {
                def m(Object o) {
                    $statement
                }
            }
        """
        def cu = new CompilationUnit()
        cu.addSource('C.groovy', src)
        cu.compile(Phases.CONVERSION)
        Expression found = null
        cu.ast.classes[0].getMethods('m')[0].code.visit(new CodeVisitorSupport() {
            @Override
            void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression call) {
                if (call.methodAsString == 'm' || call.objectExpression.text == 'this') {
                    found = call
                }
                super.visitMethodCallExpression(call)
            }
            @Override
            void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement stmt) {
                found = stmt.expression
                super.visitExpressionStatement(stmt)
            }
        })
        found
    }
}
