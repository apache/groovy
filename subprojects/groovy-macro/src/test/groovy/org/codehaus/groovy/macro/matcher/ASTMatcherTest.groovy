/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.macro.matcher

import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.control.CompilePhase

class ASTMatcherTest extends GroovyTestCase {
    void testMatchesSimpleVar() {
        def ast = macro { a }
        assert ASTMatcher.matches(ast, ast)
    }
    void testMatchesSimpleVarNeg() {
        def ast = macro { a }
        def ast2 = macro { b }
        assert !ASTMatcher.matches(ast, ast2)
        assert !ASTMatcher.matches(ast2, ast)
    }

    void testBinaryExpression() {
        def ast1 = macro { a+b }
        def ast2 = macro { a+b }
        def ast3 = macro { b+a }
        def ast4 = macro { a-b }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
    }

    void testMethodCallExpression() {
        def ast1 = macro { foo() }
        def ast2 = macro { foo() }
        def ast3 = macro { this.foo() }
        def ast4 = macro { bar() }
        def ast5 = macro { foo(bar) }
        def ast6 = macro { foo(bar())}
        def ast7 = macro { foo(bar())}
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
        assert !ASTMatcher.matches(ast1, ast5)
        assert ASTMatcher.matches(ast6, ast7)
    }

    void testPropertyExpression() {
        def ast1 = macro { this.p }
        def ast2 = macro { this.p }
        def ast3 = macro { that.p }
        def ast4 = macro { this.p2 }
        def ast5 = macro { this?.p }
        def ast6 = macro { this*.p }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
        assert !ASTMatcher.matches(ast1, ast5)
        assert !ASTMatcher.matches(ast1, ast6)
    }

    void testClassExpression() {
        def ast1 = macro(CompilePhase.SEMANTIC_ANALYSIS) { String }
        def ast2 = macro(CompilePhase.SEMANTIC_ANALYSIS) { String }
        def ast3 = macro(CompilePhase.SEMANTIC_ANALYSIS) { Boolean }
        assert ast1 instanceof ClassExpression
        assert ast2 instanceof ClassExpression
        assert ast3 instanceof ClassExpression
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
    }

    void testTernaryExpression() {
        def ast1 = macro { a?b:c }
        def ast2 = macro { a?b:c }
        def ast3 = macro { b?b:c }
        def ast4 = macro { a?a:c }
        def ast5 = macro { a?b:a }
        def ast6 = macro { a?(a+b):a }
        def ast7 = macro { a?(a+b):a }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
        assert !ASTMatcher.matches(ast1, ast5)
        assert ASTMatcher.matches(ast6, ast7)
    }

    void testElvis() {
        def ast1 = macro { a?:c }
        def ast2 = macro { a?:c }
        def ast3 = macro { b?:c }
        def ast4 = macro { a?:a }
        def ast5 = macro { a?:(a+b) }
        def ast6 = macro { a?:(a+b) }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
        assert !ASTMatcher.matches(ast1, ast5)
        assert ASTMatcher.matches(ast5, ast6)
    }

    void testPrefixExpression() {
        def ast1 = macro { ++a }
        def ast2 = macro { ++a }
        def ast3 = macro { ++b }
        def ast4 = macro { --a }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
    }

    void testPostfixExpression() {
        def ast1 = macro { a++ }
        def ast2 = macro { a++ }
        def ast3 = macro { b++ }
        def ast4 = macro { a-- }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
    }

    void testConstructorCall() {
        def ast1 = macro { new Foo() }
        def ast2 = macro { new Foo() }
        def ast3 = macro { new Bar() }
        def ast4 = macro { new Foo(bar) }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
    }

    void testDeclaration() {
        def ast1 = macro { def x = 1 }
        def ast2 = macro { def x = 1 }
        def ast3 = macro { int x = 1 }
        def ast4 = macro { def y = 1 }
        def ast5 = macro { def x = 2 }
        assert ASTMatcher.matches(ast1, ast1)
        assert ASTMatcher.matches(ast1, ast2)
        assert ASTMatcher.matches(ast2, ast1)
        assert !ASTMatcher.matches(ast1, ast3)
        assert !ASTMatcher.matches(ast1, ast4)
        assert !ASTMatcher.matches(ast1, ast5)
    }
}
