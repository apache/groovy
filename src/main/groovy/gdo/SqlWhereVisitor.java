package groovy.gdo;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.syntax.Token;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class SqlWhereVisitor extends CodeVisitorSupport {

    private StringBuffer buffer = new StringBuffer();

    public String getWhere() {
        return buffer.toString();
    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        left.visit(this);
        buffer.append(" ");
        
        Token token = expression.getOperation();
        buffer.append(tokenAsSql(token));
        
        buffer.append(" ");
        right.visit(this);
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
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

    public void visitPropertyExpression(PropertyExpression expression) {
        buffer.append(expression.getProperty());
    }

    protected String tokenAsSql(Token token) {
        switch (token.getType()) {
            case Token.COMPARE_EQUAL:
                return "=";
            case Token.LOGICAL_AND:
                return "and";
            case Token.LOGICAL_OR:
                return "or";
            default:
                return token.getText();
        }
    }
}
