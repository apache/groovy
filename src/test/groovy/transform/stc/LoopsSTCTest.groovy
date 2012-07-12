/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassNode

/**
 * Unit tests for static type checking : loops.
 *
 * @author Cedric Champeau
 */
class LoopsSTCTest extends StaticTypeCheckingTestCase {

    void testMethodCallInLoop() {
        assertScript '''
            int foo(int x) { x+1 }
            int x = 0
            for (int i=0;i<10;i++) {
                x = foo(x)
            }
        '''
    }

    void testMethodCallInLoopAndDef() {
        assertScript '''
            int foo(int x) { x+1 }
            def x = 0
            for (int i=0;i<10;i++) {
                x = foo(x)
            }
        '''
    }


    void testMethodCallWithEachAndDefAndTwoFooMethods() {
        shouldFailWithMessages '''
            Date foo(Integer x) { new Date() }
            Integer foo(Date x) { 1 }
            def x = 0
            10.times {
                 // there are two possible target methods. This is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethods() {
        shouldFailWithMessages '''
            Date foo(Integer x) { new Date() }
            Integer foo(Date x) { 1 }
            def x = 0
            for (int i=0;i<10;i++) {
                 // there are two possible target methods. This is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadType() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            for (int i=0;i<10;i++) {
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            for (int i=0;i<10;i++) {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = y
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallWithEachAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            10.times {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = y
            }
        ''', 'Cannot find matching method'
    }

    // GROOVY-5587
    void testMapEntryInForInLoop() {
        assertScript '''
            void test() {
                def result = ""
                def sum = 0
                forLoop:
                for ( Map.Entry<String, Integer> it in [a:1, b:3].entrySet() ) {
                   result += it.getKey()
                   sum += it.getValue()
                }
                assert result == "ab"
                assert sum == 4
            }
            test()
        '''
    }

    public static class LabelFinder extends ClassCodeVisitorSupport {


        public static List<Statement> lookup(MethodNode node, String label) {
            LabelFinder finder = new LabelFinder(label, null)
            node.code.visit(finder)

            finder.targets
        }

        public static List<Statement> lookup(ClassNode node, String label) {
            LabelFinder finder = new LabelFinder(label, null)
            node.methods*.code*.visit(finder)
            node.declaredConstructors*.code*.visit(finder)

            finder.targets
        }

        private final String label
        private final SourceUnit unit

        private List<Statement> targets = new LinkedList<Statement>();

        LabelFinder(final String label, final SourceUnit unit) {
            this.label = label
            this.unit = unit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            unit
        }

        @Override
        protected void visitStatement(final Statement statement) {
            super.visitStatement(statement)
            if (statement.statementLabel==label) targets << statement
        }

        List<Statement> getTargets() {
            return Collections.unmodifiableList(targets)
        }
    }
}

