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
package org.apache.groovy.ast.tools

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.Assert.assertEquals

/**
 * Testing expressions of ExpressionUtils.
 */
final class ExpressionUtilsTest {
    @Test
    void 'test transformBinaryConstantExpression - null + string'() {
        ConstantExpression left = new ConstantExpression(null)
        ConstantExpression right = new ConstantExpression('abc')
        Token token = new Token(Types.PLUS, '+', 1, 1)
        BinaryExpression be = new BinaryExpression(left, token, right)
        ClassNode targetType = ClassHelper.make(String)
        ConstantExpression actual = ExpressionUtils.transformBinaryConstantExpression(be, targetType)
        assertEquals('nullabc', actual.value)
    }

    @Test
    void 'test transformBinaryConstantExpression - string + null'() {
        ConstantExpression left = new ConstantExpression('abc')
        ConstantExpression right = new ConstantExpression(null)
        Token token = new Token(Types.PLUS, '+', 1, 1)
        BinaryExpression be = new BinaryExpression(left, token, right)
        ClassNode targetType = ClassHelper.make(String)
        ConstantExpression actual = ExpressionUtils.transformBinaryConstantExpression(be, targetType)
        assertEquals('abcnull', actual.value)
    }

    @Test
    void 'test transformBinaryConstantExpression - string + string'() {
        ConstantExpression left = new ConstantExpression('hello, ')
        ConstantExpression right = new ConstantExpression('world!')
        Token token = new Token(Types.PLUS, '+', 1, 1)
        BinaryExpression be = new BinaryExpression(left, token, right)
        ClassNode targetType = ClassHelper.make(String)
        ConstantExpression actual = ExpressionUtils.transformBinaryConstantExpression(be, targetType)
        assertEquals('hello, world!', actual.value)
    }

    @Test
    void 'test transformBinaryConstantExpression, integer + integer with target type string'() {
        ConstantExpression left = new ConstantExpression(1)
        ConstantExpression right = new ConstantExpression(1)
        Token token = new Token(Types.PLUS, '+', 1, 1)
        BinaryExpression be = new BinaryExpression(left, token, right)
        ClassNode targetType = ClassHelper.make(String)
        ConstantExpression actual = ExpressionUtils.transformBinaryConstantExpression(be, targetType)
        assert !actual // null indicates it could not be transformed (simplified) at compile time
        // but should still succeed at runtime as per below script
        assertScript '''
            class Foo {
                static final String bar = 1 + 1
            }
            assert Foo.bar == '2'
        '''
    }

    @Test
    void 'test transformBinaryConstantExpression - long + long'() {
        ConstantExpression left = new ConstantExpression(11111111L)
        ConstantExpression right = new ConstantExpression(11111111L)
        Token token = new Token(Types.PLUS, '+', 1, 1)
        BinaryExpression be = new BinaryExpression(left, token, right)
        ClassNode targetType = ClassHelper.make(Long)
        ConstantExpression actual = ExpressionUtils.transformBinaryConstantExpression(be, targetType)
        assertEquals(22222222L, actual.value)
    }
}
