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
package org.apache.groovy.parser.antlr4

import org.junit.jupiter.api.Test

/**
 * Parse-and-run coverage for GEP-19 pattern syntax, driven from the
 * {@code src/test-resources} scripts.
 * <p>
 * These cases used to hang off {@code GroovyParserTest} and
 * {@code SyntaxErrorTest}, which GROOVY-12400 replaced with the generated
 * covering-set tests. The covering set exercises the grammar; these exercise
 * the feature, so they keep their own home rather than being folded into it.
 */
final class SwitchPatternSyntaxTest {

    @Test
    void testPatternSwitchParsesAndRuns() {
        for (name in ['SwitchExpression_27x', 'SwitchExpression_28x', 'SwitchExpression_29x',
                      'SwitchExpression_30x', 'SwitchExpression_31x']) {
            TestUtils.doRunAndTestAntlr4("core/${name}.groovy")
        }
    }

    @Test
    void testInvalidPatternSwitchIsRejected() {
        for (name in ['SwitchExpression_15x', 'SwitchExpression_16x', 'SwitchExpression_17x',
                      'SwitchExpression_18x', 'SwitchExpression_19x']) {
            TestUtils.doRunAndShouldFail("fail/${name}.groovy")
        }
    }
}
