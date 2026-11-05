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
package bugs

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.util.concurrent.CopyOnWriteArrayList

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * {@code @ASTTest} on a declaration with several declarators: the declarators
 * share one annotation node, and the test closure runs for each of them.
 */
final class Groovy12701 {

    /** Names of the nodes the test closures were run for. */
    static final List<String> SEEN = new CopyOnWriteArrayList<>()

    @BeforeEach
    void clearSeen() {
        SEEN.clear()
    }

    @Test
    void testLocalVariableDeclarators() {
        assertScript '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.expr.DeclarationExpression

            def m() {
                @ASTTest(value={
                    assert node instanceof DeclarationExpression
                    bugs.Groovy12701.SEEN << node.variableExpression.name
                })
                def a = 1, b = 2
                a + b
            }
            assert m() == 3
        '''
        // without a phase the test runs in every phase from semantic analysis on
        assert SEEN.toSet() == ['a', 'b'] as Set && SEEN.count('a') == SEEN.count('b')
    }

    @Test
    void testFieldDeclarators() {
        assertScript '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.FieldNode

            class C {
                @ASTTest(value={
                    assert node instanceof FieldNode
                    bugs.Groovy12701.SEEN << node.name
                })
                int x = 1, y = 2
            }
            def c = new C()
            assert c.x + c.y == 3
        '''
        assert SEEN.toSet() == ['x', 'y'] as Set && SEEN.count('x') == SEEN.count('y')
    }

    @Test
    void testDeclaratorsWithPhase() {
        assertScript '''
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.control.CompilePhase.CANONICALIZATION

            def m() {
                @ASTTest(phase=CANONICALIZATION, value={
                    bugs.Groovy12701.SEEN << node.variableExpression.name
                })
                def a = 1, b = 2
                a + b
            }
            assert m() == 3
        '''
        assert SEEN == ['a', 'b']
    }

    @Test
    void testFailingTestForSecondDeclarator() {
        def err = shouldFail '''
            import groovy.transform.ASTTest

            def m() {
                @ASTTest(value={
                    assert node.variableExpression.name == 'a'
                })
                def a = 1, b = 2
            }
        '''
        assert err.message.contains("assert node.variableExpression.name == 'a'")
        assert !err.message.contains('BUG!')
    }
}
