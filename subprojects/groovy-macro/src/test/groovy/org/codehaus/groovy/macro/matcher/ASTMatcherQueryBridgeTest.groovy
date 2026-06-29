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
package org.codehaus.groovy.macro.matcher

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests {@link ASTMatcher#matching(org.codehaus.groovy.ast.ASTNode)} — the bridge that lets an
 * exemplar pattern be used as a predicate inside the core {@code AstQuery} pipeline.
 */
final class ASTMatcherQueryBridgeTest {

    @Test
    void exactPatternFiltersBySelectedShape() {
        assertScript '''
            import org.codehaus.groovy.ast.query.AstQuery
            import org.codehaus.groovy.ast.expr.MethodCallExpression
            import static org.codehaus.groovy.macro.matcher.ASTMatcher.matching

            def code = macro { foo(); bar(); foo() }

            assert AstQuery.from(code).descendants(MethodCallExpression).where(matching(macro { foo() })).count() == 2
            assert AstQuery.from(code).descendants(MethodCallExpression).where(matching(macro { bar() })).count() == 1
            assert AstQuery.from(code).descendants(MethodCallExpression).where(matching(macro { foo() })).any()
            assert AstQuery.from(code).descendants(MethodCallExpression).where(matching(macro { baz() })).none()
        '''
    }

    @Test
    void wildcardPatternMatchesAnyCall() {
        assertScript '''
            import org.codehaus.groovy.ast.query.AstQuery
            import org.codehaus.groovy.ast.expr.MethodCallExpression
            import static org.codehaus.groovy.macro.matcher.ASTMatcher.matching

            def code = macro { foo(); bar(); baz() }

            assert AstQuery.from(code).descendants(MethodCallExpression).where(matching(macro { _() })).count() == 3
        '''
    }

    @Test
    void composesWithPruning() {
        assertScript '''
            import org.codehaus.groovy.ast.query.AstQuery
            import org.codehaus.groovy.ast.expr.MethodCallExpression
            import org.codehaus.groovy.ast.expr.ClosureExpression
            import static org.codehaus.groovy.macro.matcher.ASTMatcher.matching

            def code = macro { foo(); [1].each { foo() } }

            assert AstQuery.from(code).descendants(MethodCallExpression).where(matching(macro { foo() })).count() == 2
            assert AstQuery.from(code).descendants(MethodCallExpression).where(matching(macro { foo() }))
                    .notInto(ClosureExpression).count() == 1
        '''
    }
}
