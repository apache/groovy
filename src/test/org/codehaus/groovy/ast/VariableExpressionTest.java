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
/**
 *
 */
package org.codehaus.groovy.ast;

import junit.framework.TestCase;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * Tests the VariableExpressionNode
 */
public class VariableExpressionTest extends TestCase {

    public void testPrimitiveOriginType() {
        VariableExpression boolExpression = new VariableExpression("fo", ClassHelper.boolean_TYPE);
        VariableExpression intExpression = new VariableExpression("foo", ClassHelper.int_TYPE);
        assertEquals(boolExpression.getOriginType().getName(), "boolean");
        assertEquals(intExpression.getOriginType().getName(), "int");
    }

    public void testNonPrimitiveOriginType() {
        VariableExpression boolExpression = new VariableExpression("foo", ClassHelper.Boolean_TYPE);
        VariableExpression intExpression = new VariableExpression("foo", ClassHelper.Integer_TYPE);
        assertEquals(boolExpression.getOriginType().getName(), "java.lang.Boolean");
        assertEquals(intExpression.getOriginType().getName(), "java.lang.Integer");
    }

    public void testPrimitiveOriginTypeConstructorVariableExpression() {
        VariableExpression boolExpression = new VariableExpression("foo", ClassHelper.boolean_TYPE);
        VariableExpression intExpression = new VariableExpression("foo", ClassHelper.int_TYPE);
        VariableExpression newBoolExpression = new VariableExpression(boolExpression);
        VariableExpression newIntExpression = new VariableExpression(intExpression);
        assertEquals(newBoolExpression.getOriginType().getName(), "boolean");
        assertEquals(newIntExpression.getOriginType().getName(), "int");
    }

    public void testPrimitiveOriginTypeConstructorParameter() {
        Parameter boolParameter = new Parameter(ClassHelper.boolean_TYPE, "foo");
        Parameter intParameter = new Parameter(ClassHelper.int_TYPE, "foo");
        VariableExpression newBoolExpression = new VariableExpression(boolParameter);
        VariableExpression newIntExpression = new VariableExpression(intParameter);
        assertEquals(newBoolExpression.getOriginType().getName(), "boolean");
        assertEquals(newIntExpression.getOriginType().getName(), "int");
    }

    public void testPrimitiveOriginTypeConstructorDynVariable() {
        DynamicVariable dynVariable = new DynamicVariable("foo", false);
        assertEquals(dynVariable.getOriginType().getName(), "java.lang.Object");
    }

    public void testIsDynamicTypedExplicitObject() {
        VariableExpression intExpression = new VariableExpression("foo", new ClassNode(Object.class));
        assertFalse(intExpression.isDynamicTyped());
    }

    public void testIsDynamicTyped_DYNMAMIC_TYPE() {
        VariableExpression intExpression = new VariableExpression("foo", ClassHelper.DYNAMIC_TYPE);
        assertTrue(intExpression.isDynamicTyped());
    }

    public void testIsDynamicTyped_DynamicVariable() {
        VariableExpression intExpression = new VariableExpression(new DynamicVariable("foo", false));
        assertTrue(intExpression.isDynamicTyped());
    }
}
