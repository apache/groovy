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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Constants;

/**
 * Verifies the method code
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class VerifierCodeVisitor extends CodeVisitorSupport implements Constants {

    private Verifier verifier;

    VerifierCodeVisitor(Verifier verifier) {
        this.verifier = verifier;
    }

    public void visitBlockStatement(BlockStatement block) {
        mergeClassExpressionAndVariableExpression(block.getStatements());
        super.visitBlockStatement(block);
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        if (verifier.getClassNode().isScriptClass() && expression.getOperation().getType() == Token.EQUAL) {
            // lets turn variable assignments into property assignments
            Expression left = expression.getLeftExpression();
            if (left instanceof VariableExpression) {
                VariableExpression varExp = (VariableExpression) left;

                System.out.println("Converting varriable expression: " + varExp.getVariable());

                PropertyExpression propExp =
                    new PropertyExpression(VariableExpression.THIS_EXPRESSION, varExp.getVariable());
                expression.setLeftExpression(propExp);
            }
        }
        super.visitBinaryExpression(expression);
    }

    /**  
     * lets look for a ClassExpression followed by a BinaryExpression with a
     * VariableExpression on the LHS and associate the type with the VariableExpression
     * and remove the ClassExpression.
     * 
     * The parser should output this correctly really.
     */
    protected void mergeClassExpressionAndVariableExpression(List list) {
        ClassExpression classExpr = null;
        ExpressionStatement lastExpStmt = null;
        List removalList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Statement statement = (Statement) iter.next();
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expStmt = (ExpressionStatement) statement;
                Expression exp = expStmt.getExpression();
                if (classExpr != null) {
                    if (exp instanceof BinaryExpression) {
                        BinaryExpression binExpr = (BinaryExpression) exp;
                        Expression lhs = binExpr.getLeftExpression();
                        if (lhs instanceof VariableExpression) {
                            VariableExpression varExp = (VariableExpression) lhs;
                            varExp.setType(classExpr.getType());
                            removalList.add(lastExpStmt);
                        }
                    }
                }
                if (exp instanceof ClassExpression) {
                    classExpr = (ClassExpression) exp;
                    lastExpStmt = expStmt;
                }
                else {
                    classExpr = null;
                    lastExpStmt = null;
                }
            }
        }
        list.removeAll(removalList);
    }
}
