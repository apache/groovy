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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.objectweb.asm.Opcodes;

/**
 * Performs various checks on code inside methods and constructors
 * including checking for valid field, variables names etc. that
 * would otherwise lead to invalid code.
 */
public class VerifierCodeVisitor extends CodeVisitorSupport implements Opcodes {

    private final Verifier verifier;

    VerifierCodeVisitor(Verifier verifier) {
        this.verifier = verifier;
    }

    public void visitForLoop(ForStatement expression) {
        assertValidIdentifier(expression.getVariable().getName(), "for loop variable name", expression);
        super.visitForLoop(expression);
    }

    public void visitFieldExpression(FieldExpression expression) {
        if (!expression.getField().isSynthetic()) {
            assertValidIdentifier(expression.getFieldName(), "field name", expression);
        }
        super.visitFieldExpression(expression);
    }

    public void visitVariableExpression(VariableExpression expression) {
        assertValidIdentifier(expression.getName(), "variable name", expression);
        super.visitVariableExpression(expression);
    }

    public void visitListExpression(ListExpression expression) {
        for (Expression element : expression.getExpressions()) {
            if (element instanceof MapEntryExpression) {
                throw new RuntimeParserException("No map entry allowed at this place", element);
            }
        }
        super.visitListExpression(expression);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        ClassNode callType = call.getType();
        if (callType.isEnum() && !callType.equals(verifier.getClassNode())) {
            throw new RuntimeParserException("Enum constructor calls are only allowed inside the enum class", call);
        }
    }

    public static void assertValidIdentifier(String name, String message, ASTNode node) {
        int size = name.length();
        if (size <= 0) {
            throw new RuntimeParserException("Invalid " + message + ". Identifier must not be empty", node);
        }
        char firstCh = name.charAt(0);
        if (size == 1 && firstCh == '$') {
            throw new RuntimeParserException("Invalid " + message + ". Must include a letter but only found: " + name, node);
        }
        if (!Character.isJavaIdentifierStart(firstCh)) {
            throw new RuntimeParserException("Invalid " + message + ". Must start with a letter but was: " + name, node);
        }

        for (int i = 1; i < size; i++) {
            char ch = name.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                throw new RuntimeParserException("Invalid " + message + ". Invalid character at position: " + (i + 1) + " of value:  " + ch + " in name: " + name, node);
            }
        }
    }
}
