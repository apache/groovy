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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class DirectMethodCallWithVargsTest {

    @Test
    void testDirectMethodCallWithVargs() {
        assertScript shell, '''
            def foo(String... args) {
                args.join(',')
            }

            assert foo() == ''
            assert foo('1') == '1'
            assert foo('1','2','3') == '1,2,3'
            assert foo('1','2','3','4') == '1,2,3,4'
            assert foo(new String[]{'1','2','3','4'}) == '1,2,3,4'

            def a = '1'
            def b = '2'
            def c = '3'
            assert foo(a,b,c) == '1,2,3'

            shouldFail(NullPointerException) {
                foo(null)
            }
        '''
    }

    @Test
    void testDirectMethodCallWithPrimitiveVargs() {
        assertScript shell, '''
            def foo(int... args) {
                args.join(',')
            }

            assert foo() == ''
            assert foo(0) == '0'
            assert foo(1) == '1'
            assert foo(1,2,3) == '1,2,3'
            assert foo(1,2,3,4) == '1,2,3,4'
            assert foo(new int[]{1,2,3,4}) == '1,2,3,4'

            shouldFail(NullPointerException) {
                foo(null)
            }
        '''
    }

    @Test
    void testDirectMethodCallWithArgumentAndVargs() {
        assertScript shell, '''
            def foo(String prefix, String... args) {
                '' + prefix + args.join(',')
            }

            assert foo('A') == 'A'
            assert foo(null) == 'null'
            assert foo('A','1') == 'A1'
            assert foo('A','1','2','3') == 'A1,2,3'
            assert foo('A','1','2','3','4') == 'A1,2,3,4'
            assert foo('A','1','2','3','4') == 'A1,2,3,4'
            assert foo('A',new String[]{'1','2','3','4'}) == 'A1,2,3,4'

            def a = '1'
            def b = '2'
            def c = '3'
            assert foo('A',a,b,c) == 'A1,2,3'

            shouldFail(NullPointerException) {
                foo('A', null)
            }
        '''
    }

    @Test
    void testDirectMethodCallWithArgumentAndPrimitiveVargs() {
        assertScript shell, '''
            def foo(int prefix, int... args) {
                '' + prefix + args.join(',')
            }

            assert foo(1) == '1'
            assert foo(1,0) == '10'
            assert foo(1,1) == '11'
            assert foo(1,1,2,3) == '11,2,3'
            assert foo(1,1,2,3,4) == '11,2,3,4'
            assert foo(0,new int[]{1,2,3,4}) == '01,2,3,4'

            shouldFail(NullPointerException) {
                foo(1, null)
            }
        '''
    }

    //--------------------------------------------------------------------------

    private final GroovyShell shell = GroovyShell.withConfig {
        imports{ staticMember 'groovy.test.GroovyAssert','shouldFail' }
        inline(phase: 'CANONICALIZATION') { sourceUnit, x, classNode ->
            def visitor = new MethodCallVisitor(sourceUnit)
            classNode.methods.each(visitor.&acceptMethod)
            visitor.visitClass(classNode)
        }
    }

    private static class MethodCallVisitor extends ClassCodeVisitorSupport {
        private MethodNode fooMethod
        final SourceUnit sourceUnit

        MethodCallVisitor(final SourceUnit unit) {
            sourceUnit = unit
        }

        void acceptMethod(final MethodNode node) {
            if (node.name == 'foo') {
                fooMethod = node
            }
        }

        @Override
        void visitMethodCallExpression(final MethodCallExpression call) {
            super.visitMethodCallExpression(call)
            if (call.methodAsString == 'foo') {
                call.methodTarget = fooMethod
            }
        }
    }
}
