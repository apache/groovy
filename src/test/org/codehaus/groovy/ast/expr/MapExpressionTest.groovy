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

class MapExpressionTest extends GroovyTestCase {

    void testGetText_emptyMap() {
        def expression = buildFromString '[:]'
        assert expression.text == '[:]'
    }

    void testGetText_singleEntry() {
        def expression = buildFromString '[x: 1]'
        assert expression.text == '[x:1]'
    }

    void testGetText_multipleEntries() {
        def expression = buildFromString '[x: 1, y: 2]'
        assert expression.text == '[x:1, y:2]'
    }

    private MapExpression buildFromString(String source) {
        def ast = new AstBuilder().buildFromString(source)
        ast[0].statements[0].expression
    }
}
