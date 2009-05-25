/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.transform.powerassert;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites the truth expression of an assertion statement. Implements
 * GroovyCodeVisitor rather than extending from CodeVisitorSupport to
 * make sure that all kinds of expressions are handled.
 *
 * @author Peter Niederwieser
 */
public class TruthExpressionRewriter implements GroovyCodeVisitor {
    private final SourceText sourceText;
    private final AssertionRewriter assertionRewriter;
    private Expression result; // return value of visitXXX() methods

    private TruthExpressionRewriter(SourceText sourceText, AssertionRewriter assertionRewriter) {
        this.sourceText = sourceText;
        this.assertionRewriter = assertionRewriter;
    }

    public static Expression rewrite(Expression truthExpr, SourceText sourceText, AssertionRewriter assertionRewriter) {
        return new TruthExpressionRewriter(sourceText, assertionRewriter).rewrite(truthExpr);
    }

    public void visitMethodCallExpression(MethodCallExpression expr) {
        MethodCallExpression conversion =
                new MethodCallExpression(
                        expr.isImplicitThis() ?
                                expr.getObjectExpression() :
                                rewrite(expr.getObjectExpression()),
                        rewrite(expr.getMethod()),
                        rewrite(expr.getArguments())
                );
        conversion.setSafe(expr.isSafe());
        conversion.setSpreadSafe(expr.isSpreadSafe());
        conversion.setSourcePosition(expr);
        result = record(conversion, expr.getMethod());
    }

    // only used for statically imported methods that are called by their simple name    
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expr) {
        StaticMethodCallExpression conversion =
                new StaticMethodCallExpression(
                        expr.getOwnerType(),
                        expr.getMethod(),
                        rewrite(expr.getArguments())
                );
        conversion.setSourcePosition(expr);
        conversion.setMetaMethod(expr.getMetaMethod());
        result = record(conversion);
    }

    public void visitBytecodeExpression(BytecodeExpression expr) {
        unsupported(); // should not occur in assertion
    }

    @SuppressWarnings("unchecked")
    public void visitArgumentlistExpression(ArgumentListExpression expr) {
        ArgumentListExpression conversion =
                new ArgumentListExpression(
                        rewriteAll(expr.getExpressions())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitPropertyExpression(PropertyExpression expr) {
        PropertyExpression conversion =
                new PropertyExpression(
                        expr.isImplicitThis() ?
                                expr.getObjectExpression() :
                                rewrite(expr.getObjectExpression()),
                        expr.getProperty(),
                        expr.isSafe()
                );
        conversion.setSourcePosition(expr);
        conversion.setSpreadSafe(expr.isSpreadSafe());
        conversion.setStatic(expr.isStatic());
        conversion.setImplicitThis(expr.isImplicitThis());
        result = record(conversion, expr.getProperty());
    }

    public void visitAttributeExpression(AttributeExpression expr) {
        AttributeExpression conversion =
                new AttributeExpression(
                        expr.isImplicitThis() ?
                                expr.getObjectExpression() :
                                rewrite(expr.getObjectExpression()),
                        expr.getProperty(),
                        expr.isSafe()
                );
        conversion.setSourcePosition(expr);
        conversion.setSpreadSafe(expr.isSpreadSafe());
        conversion.setStatic(expr.isStatic());
        conversion.setImplicitThis(expr.isImplicitThis());
        result = record(conversion, expr.getProperty());
    }

    public void visitFieldExpression(FieldExpression expr) {
        result = record(expr);
    }

    public void visitMethodPointerExpression(MethodPointerExpression expr) {
        MethodPointerExpression conversion =
                new MethodPointerExpression(
                        rewrite(expr.getExpression()),
                        rewrite(expr.getMethodName())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitVariableExpression(VariableExpression expr) {
        result = record(expr);
    }

    public void visitDeclarationExpression(DeclarationExpression expr) {
        unsupported(); // cannot occur in an assertion statement
    }

    public void visitRegexExpression(RegexExpression expr) {
        unsupported(); // class RegexExpression doesn't seem to be used
    }

    public void visitBinaryExpression(BinaryExpression expr) {
        Expression left = expr.getLeftExpression();
        Expression right = expr.getRightExpression();
        Token op = expr.getOperation();

        BinaryExpression conversion =
                new BinaryExpression(
                        // only rewrite LHS if not an assignment expr
                        Types.ofType(op.getType(), Types.ASSIGNMENT_OPERATOR) ? left : rewrite(left),
                        op,
                        rewrite(right)
                );
        conversion.setSourcePosition(expr);

        result = Types.ofType(op.getType(), Types.MATCHED_CONTAINER) ?
                // compensate for wrong source position of operator in "foo[bar]"
                record(conversion, sourceText.getNormalizedColumn(op.getText(), right.getLineNumber(), right.getColumnNumber())) :
                record(conversion, op);
    }

    public void visitConstantExpression(ConstantExpression expr) {
        result = expr;
    }

    public void visitClassExpression(ClassExpression expr) {
        result = expr;
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression expr) {
        UnaryMinusExpression conversion =
                new UnaryMinusExpression(
                        rewrite(expr.getExpression())
                );
        conversion.setSourcePosition(expr);
        result = record(conversion);
    }

    public void visitUnaryPlusExpression(UnaryPlusExpression expr) {
        UnaryPlusExpression conversion =
                new UnaryPlusExpression(
                        rewrite(expr.getExpression())
                );
        conversion.setSourcePosition(expr);
        result = record(conversion);
    }

    public void visitBitwiseNegationExpression(BitwiseNegationExpression expr) {
        BitwiseNegationExpression conversion =
                new BitwiseNegationExpression(
                        rewrite(expr.getExpression())
                );
        conversion.setSourcePosition(expr);
        result = record(conversion);
    }

    public void visitCastExpression(CastExpression expr) {
        CastExpression conversion =
                new CastExpression(
                        expr.getType(),
                        rewrite(expr.getExpression()),
                        expr.isIgnoringAutoboxing()
                );
        conversion.setSourcePosition(expr);
        conversion.setCoerce(expr.isCoerce());
        result = conversion;
    }

    public void visitClosureListExpression(ClosureListExpression expr) {
        result = expr;
    }

    public void visitNotExpression(NotExpression expr) {
        NotExpression conversion =
                new NotExpression(
                        rewrite(expr.getExpression())
                );
        conversion.setSourcePosition(expr);
        result = record(conversion);
    }

    @SuppressWarnings("unchecked")
    public void visitListExpression(ListExpression expr) {
        ListExpression conversion =
                new ListExpression(
                        rewriteAll(expr.getExpressions())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitRangeExpression(RangeExpression expr) {
        RangeExpression conversion =
                new RangeExpression(
                        rewrite(expr.getFrom()),
                        rewrite(expr.getTo()),
                        expr.isInclusive()
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    @SuppressWarnings("unchecked")
    public void visitMapExpression(MapExpression expr) {
        MapExpression conversion =
                new MapExpression(
                        rewriteAllCompatibly(expr.getMapEntryExpressions())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitMapEntryExpression(MapEntryExpression expr) {
        MapEntryExpression conversion =
                new MapEntryExpression(
                        rewrite(expr.getKeyExpression()),
                        rewrite(expr.getValueExpression())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitConstructorCallExpression(ConstructorCallExpression expr) {
        ConstructorCallExpression conversion =
                new ConstructorCallExpression(
                        expr.getType(),
                        rewrite(expr.getArguments())
                );
        conversion.setSourcePosition(expr);
        result = record(conversion);
    }

    @SuppressWarnings("unchecked")
    public void visitGStringExpression(GStringExpression expr) {
        GStringExpression conversion = new GStringExpression(
                expr.getText(),
                expr.getStrings(),
                rewriteAll(expr.getValues())
        );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    @SuppressWarnings("unchecked")
    public void visitArrayExpression(ArrayExpression expr) {
        ArrayExpression conversion = new ArrayExpression(
                expr.getElementType(),
                rewriteAll(expr.getExpressions()),
                rewriteAll(expr.getSizeExpression())
        );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitSpreadExpression(SpreadExpression expr) {
        SpreadExpression conversion =
                new SpreadExpression(
                        rewrite(expr.getExpression())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitSpreadMapExpression(SpreadMapExpression expr) {
        // to not record the underlying MapExpression twice, we do nothing here
        // see http://jira.codehaus.org/browse/GROOVY-3421
        result = expr;
    }

    public void visitTernaryExpression(TernaryExpression expr) {
        TernaryExpression conversion =
                new TernaryExpression(
                        new BooleanExpression(
                                rewrite(expr.getBooleanExpression().getExpression())
                        ),
                        rewrite(expr.getTrueExpression()),
                        rewrite(expr.getFalseExpression())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitShortTernaryExpression(ElvisOperatorExpression expr) {
        ElvisOperatorExpression conversion =
                new ElvisOperatorExpression(
                        rewrite(expr.getBooleanExpression().getExpression()),
                        rewrite(expr.getFalseExpression())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    public void visitPrefixExpression(PrefixExpression expr) {
        PrefixExpression conversion =
                new PrefixExpression(
                        expr.getOperation(),
                        // make sure that we don't accidentally turn an lvalue into
                        // an rvalue, thereby losing the operation's side effect
                        unrecord(rewrite(expr.getExpression()))
                );
        conversion.setSourcePosition(expr);
        result = record(conversion);
    }

    public void visitPostfixExpression(PostfixExpression expr) {
        PostfixExpression conversion =
                new PostfixExpression(
                        // make sure that we don't accidentally turn an lvalue into
                        // an rvalue, thereby losing the operation's side effect
                        unrecord(rewrite(expr.getExpression())),
                        expr.getOperation()
                );
        conversion.setSourcePosition(expr);
        result = record(conversion);
    }

    public void visitBooleanExpression(BooleanExpression expr) {
        result = rewrite(expr.getExpression()); // implicit conversion so don't record
    }

    public void visitClosureExpression(ClosureExpression expr) {
        expr.getCode().visit(assertionRewriter); // look for assertions within closure body
        result = expr;
    }

    @SuppressWarnings("unchecked")
    public void visitTupleExpression(TupleExpression expr) {
        TupleExpression conversion =
                new TupleExpression(
                        rewriteAll(expr.getExpressions())
                );
        conversion.setSourcePosition(expr);
        result = conversion;
    }

    private Expression record(Expression value, int column) {
        return new MethodCallExpression(
                AssertionRewriter.recorderVariable,
                ValueRecorder.RECORD_METHOD_NAME,
                new ArgumentListExpression(
                        value,
                        new ConstantExpression(column)
                )
        );
    }

    private Expression record(Expression value) {
        return record(value, sourceText.getNormalizedColumn(value.getLineNumber(), value.getColumnNumber()));
    }

    private Expression record(Expression value, ASTNode node) {
        return record(value, sourceText.getNormalizedColumn(node.getLineNumber(), node.getColumnNumber()));
    }

    private Expression record(Expression value, Token token) {
        return record(value, sourceText.getNormalizedColumn(token.getStartLine(), token.getStartColumn()));
    }

    // unrecord(record(expr)) == expr
    private Expression unrecord(Expression expr) {
        if (!(expr instanceof MethodCallExpression)) return expr;
        MethodCallExpression methodExpr = (MethodCallExpression) expr;
        Expression targetExpr = methodExpr.getObjectExpression();
        if (!(targetExpr instanceof VariableExpression)) return expr;
        VariableExpression var = (VariableExpression) targetExpr;
        if (var != AssertionRewriter.recorderVariable) return expr;
        if (!methodExpr.getMethodAsString().equals(ValueRecorder.RECORD_METHOD_NAME)) return expr;
        return ((ArgumentListExpression) methodExpr.getArguments()).getExpression(0);
    }

    /*
     * Rewrites an expression to an expression of the same (static) type (e.g. BinaryExpression -> BinaryExpression).
     */
    @SuppressWarnings("unchecked")
    private <T extends Expression> T rewriteCompatibly(T expr) {
        expr.visit(this);
        return (T) result;
    }

    /*
     * Rewrites an expression to any other expression (e.g. BinaryExpression -> MethodCallExpression).
     */
    private Expression rewrite(Expression expr) {
        return rewriteCompatibly(expr);
    }

    /*
     * Rewrites multiple expressions to expressions of the same (static) type.
     */
    private <T extends Expression> List<T> rewriteAllCompatibly(List<T> exprs) {
        List<T> result = new ArrayList<T>(exprs.size());
        for (T expr : exprs) result.add(rewriteCompatibly(expr));
        return result;
    }

    /*
     * Rewrites multiple expressions to any other expressions.
     */
    private List<Expression> rewriteAll(List<Expression> exprs) {
        return rewriteAllCompatibly(exprs);
    }

    private static void unsupported() {
        throw new UnsupportedOperationException();
    }

    public void visitBlockStatement(BlockStatement stat) {
        unsupported();
    }

    public void visitForLoop(ForStatement stat) {
        unsupported();
    }

    public void visitWhileLoop(WhileStatement stat) {
        unsupported();
    }

    public void visitDoWhileLoop(DoWhileStatement stat) {
        unsupported();
    }

    public void visitIfElse(IfStatement stat) {
        unsupported();
    }

    public void visitExpressionStatement(ExpressionStatement stat) {
        unsupported();
    }

    public void visitReturnStatement(ReturnStatement stat) {
        unsupported();
    }

    public void visitAssertStatement(AssertStatement stat) {
        unsupported();
    }

    public void visitTryCatchFinally(TryCatchStatement stat) {
        unsupported();
    }

    public void visitSwitch(SwitchStatement stat) {
        unsupported();
    }

    public void visitCaseStatement(CaseStatement stat) {
        unsupported();
    }

    public void visitBreakStatement(BreakStatement stat) {
        unsupported();
    }

    public void visitContinueStatement(ContinueStatement stat) {
        unsupported();
    }

    public void visitThrowStatement(ThrowStatement stat) {
        unsupported();
    }

    public void visitSynchronizedStatement(SynchronizedStatement stat) {
        unsupported();
    }

    public void visitCatchStatement(CatchStatement stat) {
        unsupported();
    }
}
