package groovy.gdo;

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
import org.codehaus.groovy.syntax.Token;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class SqlWhereVisitor implements GroovyCodeVisitor {

    private StringBuffer buffer = new StringBuffer();

    public void visitForLoop(ForStatement forLoop) {
        // TODO Auto-generated method stub

    }

    public void visitWhileLoop(WhileStatement loop) {
        // TODO Auto-generated method stub

    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        // TODO Auto-generated method stub

    }

    public void visitIfElse(IfStatement ifElse) {
        // TODO Auto-generated method stub

    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        // TODO Auto-generated method stub

    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    public void visitAssertStatement(AssertStatement statement) {
        // TODO Auto-generated method stub

    }

    public void visitTryCatchFinally(TryCatchStatement finally1) {
        // TODO Auto-generated method stub

    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        // TODO Auto-generated method stub

    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitBinaryExpression(BinaryExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        left.visit(this);
        buffer.append(" ");
        Token token = expression.getOperation();
        switch (token.getType()) {
            case Token.COMPARE_EQUAL:
                buffer.append("=");
                break;
                
            default:
                buffer.append(token.getText());
        }
        buffer.append(" ");
        right.visit(this);
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitClosureExpression(ClosureExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitTupleExpression(TupleExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitMapExpression(MapExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitListExpression(ListExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitRangeExpression(RangeExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitConstantExpression(ConstantExpression expression) {
        Object value = expression.getValue();
        if (value instanceof Number) {
            buffer.append(value);
        }
        else {
            buffer.append("'");
            buffer.append(value);
            buffer.append("'");
        }
    }

    public void visitClassExpression(ClassExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitVariableExpression(VariableExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitPropertyExpression(PropertyExpression expression) {
        buffer.append(expression.getProperty());
    }

    public void visitFieldExpression(FieldExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitRegexExpression(RegexExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitGStringExpression(GStringExpression expression) {
        // TODO Auto-generated method stub

    }

    public void visitArrayExpression(ArrayExpression expression) {
        // TODO Auto-generated method stub

    }

    public String getWhere() {
        return buffer.toString();
    }

}
