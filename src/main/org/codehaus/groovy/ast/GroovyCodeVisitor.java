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

import org.codehaus.groovy.ast.expr.*;

import org.codehaus.groovy.ast.stmt.*;

/**
 * An implementation of the visitor pattern for working with ASTNodes
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */

public interface GroovyCodeVisitor {

    // statements

    //-------------------------------------------------------------------------

    void visitBlockStatement(BlockStatement statement);

    void visitForLoop(ForStatement forLoop);

    void visitWhileLoop(WhileStatement loop);

    void visitDoWhileLoop(DoWhileStatement loop);

    void visitIfElse(IfStatement ifElse);

    void visitExpressionStatement(ExpressionStatement statement);

    void visitReturnStatement(ReturnStatement statement);

    void visitAssertStatement(AssertStatement statement);

    void visitTryCatchFinally(TryCatchStatement finally1);

    void visitSwitch(SwitchStatement statement);

    void visitCaseStatement(CaseStatement statement);

    void visitBreakStatement(BreakStatement statement);

    void visitContinueStatement(ContinueStatement statement);

    void visitThrowStatement(ThrowStatement statement);

    void visitSynchronizedStatement(SynchronizedStatement statement);
    
    void visitCatchStatement(CatchStatement statement);

    // expressions

    //-------------------------------------------------------------------------

    void visitMethodCallExpression(MethodCallExpression call);

    void visitStaticMethodCallExpression(StaticMethodCallExpression expression);

    void visitConstructorCallExpression(ConstructorCallExpression expression);

    void visitTernaryExpression(TernaryExpression expression);

    void visitBinaryExpression(BinaryExpression expression);

    void visitPrefixExpression(PrefixExpression expression);

    void visitPostfixExpression(PostfixExpression expression);

    void visitBooleanExpression(BooleanExpression expression);

    void visitClosureExpression(ClosureExpression expression);

    void visitTupleExpression(TupleExpression expression);

    void visitMapExpression(MapExpression expression);

    void visitMapEntryExpression(MapEntryExpression expression);

    void visitListExpression(ListExpression expression);

    void visitRangeExpression(RangeExpression expression);

    void visitPropertyExpression(PropertyExpression expression);

    void visitAttributeExpression(AttributeExpression attributeExpression);

    void visitFieldExpression(FieldExpression expression);

    void visitMethodPointerExpression(MethodPointerExpression expression);

    void visitConstantExpression(ConstantExpression expression);

    void visitClassExpression(ClassExpression expression);

    void visitVariableExpression(VariableExpression expression);

    void visitDeclarationExpression(DeclarationExpression expression);

    void visitRegexExpression(RegexExpression expression);

    void visitGStringExpression(GStringExpression expression);

    void visitArrayExpression(ArrayExpression expression);

    void visitSpreadExpression(SpreadExpression expression);

    void visitSpreadMapExpression(SpreadMapExpression expression);

    void visitNotExpression(NotExpression expression);

    void visitNegationExpression(NegationExpression expression);

    void visitBitwiseNegExpression(BitwiseNegExpression expression);

    void visitCastExpression(CastExpression expression);

    void visitArgumentlistExpression(ArgumentListExpression expression);
}

