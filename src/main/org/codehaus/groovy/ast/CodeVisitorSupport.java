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
package org.codehaus.groovy.ast;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

/**
 * Abstract base class for any GroovyCodeVisitory which by default
 * just walks the code and expression tree
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class CodeVisitorSupport implements GroovyCodeVisitor {

    public void visitBlockStatement(BlockStatement block) {
        List statements = block.getStatements();
        for (Iterator iter = statements.iterator(); iter.hasNext(); ) {
            Statement statement = (Statement) iter.next();
            statement.visit(this);
        }
    }

    public void visitForLoop(ForStatement forLoop) {
        forLoop.getCollectionExpression().visit(this);
        forLoop.getLoopBlock().visit(this);
    }

    public void visitWhileLoop(WhileStatement loop) {
        loop.getBooleanExpression().visit(this);
        loop.getLoopBlock().visit(this);
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        loop.getLoopBlock().visit(this);
        loop.getBooleanExpression().visit(this);
    }

    public void visitIfElse(IfStatement ifElse) {
        ifElse.getBooleanExpression().visit(this);
        ifElse.getIfBlock().visit(this);
        ifElse.getElseBlock().visit(this);
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        statement.getExpression().visit(this);
    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    public void visitAssertStatement(AssertStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getMessageExpression().visit(this);
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        statement.getTryStatement().visit(this);
        List list = statement.getCatchStatements();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            CatchStatement catchStatement = (CatchStatement) iter.next();
            catchStatement.visit(this);
        }
        statement.getFinallyStatement().visit(this);
    }

    public void visitSwitch(SwitchStatement statement) {
        statement.getExpression().visit(this);
        List list = statement.getCaseStatements();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            CaseStatement caseStatement = (CaseStatement) iter.next();
            caseStatement.visit(this);
        }
        statement.getDefaultStatement().visit(this);
    }

    public void visitCaseStatement(CaseStatement statement) {
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    public void visitBreakStatement(BreakStatement statement) {
    }

    public void visitContinueStatement(ContinueStatement statement) {
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    public void visitThrowStatement(ThrowStatement statement) {
        statement.getExpression().visit(this);
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        call.getObjectExpression().visit(this);
        call.getArguments().visit(this);
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        call.getArguments().visit(this);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        call.getArguments().visit(this);
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    public void visitTernaryExpression(TernaryExpression expression) {
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        expression.getFalseExpression().visit(this);
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitBooleanExpression(BooleanExpression expression) {
		expression.getExpression().visit(this);
	}

	public void visitNotExpression(NotExpression expression) {
		expression.getExpression().visit(this);
	}

    public void visitClosureExpression(ClosureExpression expression) {
        expression.getCode().visit(this);
    }
    
    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    public void visitListExpression(ListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    public void visitArrayExpression(ArrayExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }
    
    public void visitMapExpression(MapExpression expression) {
        visitListOfExpressions(expression.getMapEntryExpressions());
        
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        expression.getKeyExpression().visit(this);
        expression.getValueExpression().visit(this);
        
    }

    public void visitRangeExpression(RangeExpression expression) {
        expression.getFrom().visit(this);
        expression.getTo().visit(this);
    }

    public void visitSpreadExpression(SpreadExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitNegationExpression(NegationExpression expression) {
        expression.getExpression().visit(this);
    }
    
    public void visitBitwiseNegExpression(BitwiseNegExpression expression) {
        expression.getExpression().visit(this);
    }
    
    public void visitCastExpression(CastExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitConstantExpression(ConstantExpression expression) {
    }

    public void visitClassExpression(ClassExpression expression) {
    }

    public void visitVariableExpression(VariableExpression expression) {
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        expression.getObjectExpression().visit(this);
    }

    public void visitAttributeExpression(AttributeExpression expression) {
        expression.getObjectExpression().visit(this);
    }

    public void visitFieldExpression(FieldExpression expression) {
    }

    public void visitRegexExpression(RegexExpression expression) {
    }

    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getStrings());
        visitListOfExpressions(expression.getValues());
    }

    protected void visitListOfExpressions(List list) {
        Expression expression, expr2, expr3;
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            expression = (Expression) iter.next();
            if (expression instanceof SpreadExpression) {
                expr2 = ((SpreadExpression) expression).getExpression();
                expr2.visit(this);
            }
            else {
                expression.visit(this);
            }
        }
    }

}
