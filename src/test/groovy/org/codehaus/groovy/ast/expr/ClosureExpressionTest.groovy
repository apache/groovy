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
package org.codehaus.groovy.ast.expr

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.junit.Test

final class ClosureExpressionTest {

    private static Expression fromString(String source) {
        List nodes = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, source)
        nodes[0].statements[0].expression
    }

    @Test
    void testGetText_Simple() {
        def expression = fromString 'return { it * it }'
        assert expression.text == '{ ... }'
    }

    @Test
    void testGetText_Parameter() {
        def expression = fromString 'return { x -> x * x }'
        assert expression.text == '{ java.lang.Object x -> ... }'
    }

    @Test
    void testGetText_Parameters() {
        def expression = fromString 'return { x, y -> x * y }'
        assert expression.text == '{ java.lang.Object x, java.lang.Object y -> ... }'
    }

    @Test
    void testGetText_TypedParameter() {
        def expression = fromString 'return { String x -> x * x }'
        assert expression.text == '{ java.lang.String x -> ... }'
    }

    @Test
    void testGetText_TypedParameters() {
        def expression = fromString 'return { String x, Integer y -> x * y }'
        assert expression.text == '{ java.lang.String x, java.lang.Integer y -> ... }'
    }

    @Test
    void testGetText_ParameterizedType() {
        def expression = fromString 'return { List<String> x -> }'
        assert expression.text == '{ java.util.List<java.lang.String> x -> ... }'
    }
}
