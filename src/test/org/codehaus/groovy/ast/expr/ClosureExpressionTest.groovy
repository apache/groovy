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

class ClosureExpressionTest extends GroovyTestCase {

    void testGetText_Simple() {
        def expression = buildFromString 'return { it * it }'
        assert expression.text == '{ -> ... }'
    }

    void testGetText_Parameter() {
        def expression = buildFromString 'return { x -> x * x }'
        assert expression.text == '{ java.lang.Object x -> ... }'
    }

    void testGetText_MultipleParameters() {
        def expression = buildFromString 'return { x, y -> x * y }'
        assert expression.text == '{ java.lang.Object x, java.lang.Object y -> ... }'
    }

    void testGetText_TypedParameter() {
        def expression = buildFromString 'return { String x -> x * x }'
        assert expression.text == '{ java.lang.String x -> ... }'
    }

    void testGetText_MultipleTypedParameters() {
        def expression = buildFromString 'return { String x, Integer y -> x * y }'
        assert expression.text == '{ java.lang.String x, java.lang.Integer y -> ... }'
    }

    private Expression buildFromString(String source) {
        def ast = new AstBuilder().buildFromString(source)
        ast[0].statements[0].expression
    }
}
