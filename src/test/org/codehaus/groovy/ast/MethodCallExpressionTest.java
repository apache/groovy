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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the MethodCallExpression
 */
public class MethodCallExpressionTest extends ASTTest {

    private boolean isImplicitThis;

    /*
     * To make sure the MethodCallExpression is visited and we do not test against
     * the default value of isImplicitThis
     */
    private boolean visited;

    private List<String> defaultScriptMethods = new ArrayList<String>();

    private ClassCodeVisitorSupport methodCallVisitor = new ClassCodeVisitorSupport() {

        public void visitMethodCallExpression(MethodCallExpression methodCall) {
            if (defaultScriptMethods.contains(methodCall.getMethodAsString())) {
                visited = true;
                isImplicitThis = methodCall.isImplicitThis();
            }
        }

        protected SourceUnit getSourceUnit() {
            return null;
        }
    };

    public MethodCallExpressionTest() {
        defaultScriptMethods.add("substring");
        defaultScriptMethods.add("println");
    }

    protected void setUp() throws Exception {
        visited = false;
    }

    public void testIsImplicitThisOnObject() {
        ModuleNode root = getAST("string.substring(2)", Phases.SEMANTIC_ANALYSIS);
        methodCallVisitor.visitClass(root.getClasses().get(0));
        assertTrue(visited);
        assertFalse(isImplicitThis);
    }

    public void testIsImplicitThisExplicitThis() {
        ModuleNode root = getAST("this.println()", Phases.SEMANTIC_ANALYSIS);
        methodCallVisitor.visitClass(root.getClasses().get(0));
        assertTrue(visited);
        assertFalse(isImplicitThis);
    }

    public void testIsImplicitThisNoObject() {
        ModuleNode root = getAST("println()", Phases.SEMANTIC_ANALYSIS);
        methodCallVisitor.visitClass(root.getClasses().get(0));
        assertTrue(visited);
        assertTrue(isImplicitThis);
    }
}
