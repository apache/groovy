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
package org.codehaus.groovy.ast.query

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.SourceUnit

final class AstQueryTest extends GroovyTestCase {

    private static ModuleNode parse(String src) {
        def su = SourceUnit.create('AstQueryTest', src)
        su.parse(); su.completePhase(); su.convert()
        su.AST
    }

    private static MethodNode method(ModuleNode m, String name) {
        m.classes*.methods.flatten().find { it.name == name }
    }

    private static ClassNode clazz(ModuleNode m, String name) {
        m.classes.find { it.nameWithoutPackage == name }
    }

    private static String callName(n) {
        n instanceof StaticMethodCallExpression ? n.method : n.methodAsString
    }

    void testTypeFilterAndCollect() {
        def code = method(parse('int fact(int n) { n <= 1 ? 1 : n * fact(n - 1) }'), 'fact').code
        def q = AstQuery.from(code).descendants(MethodCallExpression, StaticMethodCallExpression)
                .where { callName(it) == 'fact' }
        assert q.any()
        assert q.count() == 1
        assert q.list().size() == 1
    }

    void testPruningAtClosureBoundary() {
        def code = method(parse('def run() { foo(); [1,2].each { foo() } }'), 'run').code
        assert AstQuery.from(code).descendants(MethodCallExpression).where { callName(it) == 'foo' }.count() == 2
        assert AstQuery.from(code).descendants(MethodCallExpression).where { callName(it) == 'foo' }
                .notInto(ClosureExpression).count() == 1
    }

    void testShortCircuitStopsEarly() {
        def code = method(parse('def go() { foo(); foo(); foo(); foo() }'), 'go').code
        int evals = 0
        boolean found = AstQuery.from(code).descendants(MethodCallExpression)
                .where { evals++; callName(it) == 'foo' }.any()
        assert found
        assert evals == 1
        int all = 0
        AstQuery.from(code).descendants(MethodCallExpression).where { all++; callName(it) == 'foo' }.list()
        assert all == 4
    }

    void testIntoInnerClass() {
        def outer = clazz(parse('class Outer { def a(){ mark() }; static class Inner { def b(){ mark() } } }'), 'Outer')
        assert AstQuery.from(outer).descendants(MethodCallExpression).where { callName(it) == 'mark' }.count() == 1
        assert AstQuery.from(outer).descendants(MethodCallExpression).where { callName(it) == 'mark' }
                .into(ClassNode).count() == 2
    }

    void testContextualPredicate() {
        def c = clazz(parse('class C { def foo(){ x }; def bar(){ x } }'), 'C')
        def inBar = AstQuery.from(c).descendants(VariableExpression)
                .where { node, ctx -> node.name == 'x' && ctx.enclosingMethod()?.name == 'bar' }.list()
        assert inBar.size() == 1
        assert AstQuery.from(c).descendants(VariableExpression)
                .where { n, ctx -> n.name == 'x' && ctx.enclosingClass()?.name == 'C' }.count() == 2
    }

    void testDocumentOrderFindFirst() {
        def code = method(parse('def go() { alpha(); beta(); gamma() }'), 'go').code
        def first = AstQuery.from(code).descendants(MethodCallExpression).first()
        assert callName(first) == 'alpha'
    }

    void testForEachWithContext() {
        def c = clazz(parse('class C { def m(){ y } }'), 'C')
        def names = []
        AstQuery.from(c).descendants(VariableExpression)
                .forEach { node, ctx -> names << "${node.name}@${ctx.enclosingMethod()?.name}".toString() }
        assert names == ['y@m']
    }

    void testAndSelfIncludesRoot() {
        def code = method(parse('def go() { 1 + 2 }'), 'go').code.statements[0].expression // a BinaryExpression
        assert code instanceof BinaryExpression
        assert AstQuery.from(code).descendants(BinaryExpression).count() == 0
        assert AstQuery.from(code).andSelf().descendants(BinaryExpression).count() == 1
    }

    void testNoTypeMatchesEveryNode() {
        def code = method(parse('def go() { a = 1 }'), 'go').code
        // at least the block, expression statement, binary, variable and constant nodes
        assert AstQuery.from(code).descendants().count() >= 4
        assert AstQuery.from(code).descendants(ConstantExpression).any()
    }

    void testEmptyExpressionIsVisited() {
        def empty = new org.codehaus.groovy.ast.expr.EmptyExpression()
        assert AstQuery.from(empty).andSelf().descendants(org.codehaus.groovy.ast.expr.EmptyExpression).count() == 1
    }

    void testQueryIsImmutableAndReRunnable() {
        def code = method(parse('def go() { foo(); foo() }'), 'go').code
        def q = AstQuery.from(code).descendants(MethodCallExpression)
        assert q.count() == 2
        assert q.count() == 2        // same instance, fresh traversal each time
        assert q.list().size() == 2
        // refinement returns a new query, leaving the original unchanged
        assert q.where { false }.count() == 0
        assert q.count() == 2
    }
}
