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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-6242: the inferred type of {@code BigInteger++} used to be
 * {@code Number} (the declared return type of {@code next(Number)}).
 * Incrementing a {@code BigInteger} must be inferred as {@code BigInteger}.
 *
 * Related coverage also exists in
 * {@code BugsStaticCompileTest#testGetAtBigInt}.
 */
final class Groovy6242 {

    @Test
    void testInferredTypeOfBigIntegerPostfixIncrementIsBigInteger() {
        assertScript '''
            import groovy.transform.TypeChecked
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION
            import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE

            @TypeChecked
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def expr = node.code.statements[1].expression
                def inferred = expr.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert inferred.name == 'java.math.BigInteger'
            })
            void test() {
                BigInteger bi = 1G
                def x = bi++
                BigInteger y = bi++
                y = ++bi
                assert x == 1G
            }

            test()
        '''
    }
}
