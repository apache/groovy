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
package groovy.transform

import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests the {@code groovy.asttest.enable} switch for the {@link ASTTest} AST transform.
 */
final class ASTTestEnableTest {

    /** Compiling this fails only if the test closure is actually evaluated. */
    private static final String SCRIPT_WITH_FAILING_AST_TEST = '''
        @groovy.transform.ASTTest(value = {
            assert false : 'test closure was evaluated'
        })
        class C {}
        new C()
    '''

    @Test
    void testEnabledByDefault() {
        assert System.getProperty('groovy.asttest.enable') == null

        def error = shouldFail(SCRIPT_WITH_FAILING_AST_TEST)
        assert error.message.contains('test closure was evaluated')
    }

    @Test
    @ForkedJvm(systemProperties = ['groovy.asttest.enable=false'])
    void testDisabledBySystemProperty() {
        assert System.getProperty('groovy.asttest.enable') == 'false'

        // the annotation is a no-op, so the failing closure never runs
        assertScript SCRIPT_WITH_FAILING_AST_TEST
    }

    @Test
    @ForkedJvm(systemProperties = ['groovy.asttest.enable=true'])
    void testExplicitlyEnabledBySystemProperty() {
        def error = shouldFail(SCRIPT_WITH_FAILING_AST_TEST)
        assert error.message.contains('test closure was evaluated')
    }
}
