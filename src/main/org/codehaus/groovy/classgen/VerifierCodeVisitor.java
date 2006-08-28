/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

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
import org.codehaus.groovy.ast.expr.MapExpression;
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
        assertValidIdentifier(expression.getFieldName(), "field name", expression);
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
        if (!Character.isJavaIdentifierStart(firstCh) || firstCh == '$') {
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
