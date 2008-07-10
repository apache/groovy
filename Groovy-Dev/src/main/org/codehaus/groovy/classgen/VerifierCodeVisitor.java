/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.objectweb.asm.Opcodes;

/**
 * Verifies the method code
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class VerifierCodeVisitor extends CodeVisitorSupport implements Opcodes {

    private Verifier verifier;

    VerifierCodeVisitor(Verifier verifier) {
        this.verifier = verifier;
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        super.visitMethodCallExpression(call);
    }

    public void visitForLoop(ForStatement expression) {
        assertValidIdentifier(expression.getVariable().getName(), "for loop variable name", expression);
        super.visitForLoop(expression);
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        // assertValidIdentifier(expression.getProperty(), "property name", expression);  // This has been commented out to fix the issue Groovy-843
        super.visitPropertyExpression(expression);
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

    public void visitBinaryExpression(BinaryExpression expression) {
        /*
        if (verifier.getClassNode().isScript() && expression.getOperation().getType() == Token.EQUAL) {
            // lets turn variable assignments into property assignments
            Expression left = expression.getLeftExpression();
            if (left instanceof VariableExpression) {
                VariableExpression varExp = (VariableExpression) left;

                //System.out.println("Converting variable expression: " + varExp.getVariable());

                PropertyExpression propExp =
                    new PropertyExpression(VariableExpression.THIS_EXPRESSION, varExp.getVariable());
                expression.setLeftExpression(propExp);
            }
        }
        */
        super.visitBinaryExpression(expression);
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
    
    public void visitListExpression(ListExpression expression) {
        List expressions = expression.getExpressions();
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (element instanceof MapEntryExpression) {
                throw new RuntimeParserException ("no map entry allowed at this place",(Expression) element);
            }
        }
        super.visitListExpression(expression);
    }
}
