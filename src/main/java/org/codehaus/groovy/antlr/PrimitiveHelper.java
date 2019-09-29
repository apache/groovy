package org.codehaus.groovy.antlr;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;

public class PrimitiveHelper {
    private PrimitiveHelper() {
    }

    public static Expression getDefaultValueForPrimitive(ClassNode type) {
        if (type == ClassHelper.int_TYPE) {
            return new ConstantExpression(0);
        }
        if (type == ClassHelper.long_TYPE) {
            return new ConstantExpression(0L);
        }
        if (type == ClassHelper.double_TYPE) {
            return new ConstantExpression(0.0);
        }
        if (type == ClassHelper.float_TYPE) {
            return new ConstantExpression(0.0F);
        }
        if (type == ClassHelper.boolean_TYPE) {
            return ConstantExpression.FALSE;
        }
        if (type == ClassHelper.short_TYPE) {
            return new ConstantExpression((short) 0);
        }
        if (type == ClassHelper.byte_TYPE) {
            return new ConstantExpression((byte) 0);
        }
        if (type == ClassHelper.char_TYPE) {
            return new ConstantExpression((char) 0);
        }
        return null;
    }
}
