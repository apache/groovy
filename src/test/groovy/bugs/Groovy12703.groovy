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
 * {@code @ASTTest} recompiles its test closure from the source text; code that
 * follows the closure on the closure's last line is not part of it.
 */
final class Groovy12703 {

    /** Names of the nodes the test closures were run for. */
    static final List<String> SEEN = new CopyOnWriteArrayList<>()

    @BeforeEach
    void clearSeen() {
        SEEN.clear()
    }

    @Test
    void testBraceLaterOnTheLine() {
        assertScript '''
            @groovy.transform.ASTTest(value={ bugs.Groovy12703.SEEN << node.variableExpression.name }) def a = [1].collect { it }
            assert a == [1]
        '''
        assert SEEN.toSet() == ['a'] as Set
    }

    @Test
    void testMethodClosedOnTheSameLine() {
        assertScript '''
            def m() { @groovy.transform.ASTTest(value={ bugs.Groovy12703.SEEN << node.variableExpression.name }) def a = 1; a }
            assert m() == 1
        '''
        assert SEEN.toSet() == ['a'] as Set
    }

    @Test
    void testClosureWithBracesOfItsOwn() {
        assertScript '''
            @groovy.transform.ASTTest(value={ assert [1].every { it > 0 }; bugs.Groovy12703.SEEN << 'every' }) def a = [1].collect { it }
            assert a == [1]
        '''
        assert SEEN.toSet() == ['every'] as Set
    }

    @Test
    void testMultiLineClosureFollowedByCode() {
        assertScript '''
            def m() {
                @groovy.transform.ASTTest(value={
                    bugs.Groovy12703.SEEN << node.variableExpression.name
                }) def b = [2].collect { it }; b }
            assert m() == [2]
        '''
        assert SEEN.toSet() == ['b'] as Set
    }

    @Test
    void testFailingTestIsReported() {
        def err = shouldFail '''
            @groovy.transform.ASTTest(value={ assert node.variableExpression.name == 'z' }) def a = [1].collect { it }
        '''
        assert err.message.contains("assert node.variableExpression.name == 'z'")
    }
}
