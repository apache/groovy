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

package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.BytecodeExpression;

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
    
    void visitShortTernaryExpression(ElvisOperatorExpression expression);

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

    void visitGStringExpression(GStringExpression expression);

    void visitArrayExpression(ArrayExpression expression);

    void visitSpreadExpression(SpreadExpression expression);

    void visitSpreadMapExpression(SpreadMapExpression expression);

    void visitNotExpression(NotExpression expression);

    void visitUnaryMinusExpression(UnaryMinusExpression expression);

    void visitUnaryPlusExpression(UnaryPlusExpression expression);

    void visitBitwiseNegationExpression(BitwiseNegationExpression expression);

    void visitCastExpression(CastExpression expression);

    void visitArgumentlistExpression(ArgumentListExpression expression);

    void visitClosureListExpression(ClosureListExpression closureListExpression);

    void visitBytecodeExpression(BytecodeExpression expression);
}

