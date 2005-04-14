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

    public void visitBlockStatement(BlockStatement statement);

    public void visitForLoop(ForStatement forLoop);

    public void visitWhileLoop(WhileStatement loop);

    public void visitDoWhileLoop(DoWhileStatement loop);

    public void visitIfElse(IfStatement ifElse);

    public void visitExpressionStatement(ExpressionStatement statement);

    public void visitReturnStatement(ReturnStatement statement);

    public void visitAssertStatement(AssertStatement statement);

    public void visitTryCatchFinally(TryCatchStatement finally1);

    public void visitSwitch(SwitchStatement statement);

    public void visitCaseStatement(CaseStatement statement);

    public void visitBreakStatement(BreakStatement statement);

    public void visitContinueStatement(ContinueStatement statement);

    public void visitThrowStatement(ThrowStatement statement);

    public void visitSynchronizedStatement(SynchronizedStatement statement);
    
    public void visitCatchStatement(CatchStatement statement);

    

    // expressions

    //-------------------------------------------------------------------------

    public void visitMethodCallExpression(MethodCallExpression call);

    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression);

    public void visitConstructorCallExpression(ConstructorCallExpression expression);

    public void visitTernaryExpression(TernaryExpression expression);

    public void visitBinaryExpression(BinaryExpression expression);

    public void visitPrefixExpression(PrefixExpression expression);

    public void visitPostfixExpression(PostfixExpression expression);

    public void visitBooleanExpression(BooleanExpression expression);

    public void visitClosureExpression(ClosureExpression expression);

    

    public void visitTupleExpression(TupleExpression expression);

    public void visitMapExpression(MapExpression expression);

    public void visitMapEntryExpression(MapEntryExpression expression);

    public void visitListExpression(ListExpression expression);

    public void visitRangeExpression(RangeExpression expression);

    

    public void visitPropertyExpression(PropertyExpression expression);

    public void visitAttributeExpression(AttributeExpression attributeExpression);

    public void visitFieldExpression(FieldExpression expression);

    public void visitMethodPointerExpression(MethodPointerExpression expression);



    public void visitConstantExpression(ConstantExpression expression);

    public void visitClassExpression(ClassExpression expression);

    public void visitVariableExpression(VariableExpression expression);

    public void visitDeclarationExpression(DeclarationExpression expression);

    public void visitRegexExpression(RegexExpression expression);

    public void visitGStringExpression(GStringExpression expression);

    public void visitArrayExpression(ArrayExpression expression);



    public void visitSpreadExpression(SpreadExpression expression);

    public void visitNotExpression(NotExpression expression);

    public void visitNegationExpression(NegationExpression expression);

    public void visitBitwiseNegExpression(BitwiseNegExpression expression);

    public void visitCastExpression(CastExpression expression);



}

