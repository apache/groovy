package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

/**
 * Walks the code performing any code validation logic along the way
 * 
 * @author James Strachan
 * @version $Revision$
 */
public class CodeVisitor implements GroovyCodeVisitor {

    public void visitForLoop(ForStatement forLoop) {
    }

    public void visitWhileLoop(WhileStatement loop) {
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
    }

    public void visitIfElse(IfStatement ifElse) {
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    public void visitAssertStatement(AssertStatement statement) {
    }

    public void visitTryCatchFinally(TryCatchStatement finally1) {
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
    }

    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        left.visit(this);

        right.visit(this);
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitClosureExpression(ClosureExpression expression) {
    }

    public void visitMapExpression(MapExpression expression) {
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
    }

    public void visitTupleExpression(TupleExpression expression) {
    }

    public void visitListExpression(ListExpression expression) {
    }

    public void visitRangeExpression(RangeExpression expression) {
    }

    public void visitConstantExpression(ConstantExpression expression) {
    }

    public void visitClassExpression(ClassExpression expression) {
    }

    public void visitVariableExpression(VariableExpression expression) {
    }

    public void visitPropertyExpression(PropertyExpression expression) {
    }

    public void visitFieldExpression(FieldExpression expression) {
    }

    public void visitRegexExpression(RegexExpression expression) {
    }

    public void visitGStringExpression(GStringExpression expression) {
    }

    public void visitArrayExpression(ArrayExpression expression) {
    }
}
