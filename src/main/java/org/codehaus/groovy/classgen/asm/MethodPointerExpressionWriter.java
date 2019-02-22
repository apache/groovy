package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * A helper class used to generate bytecode for method pointer expressions.
 * @since 3.0.0
 */
public class MethodPointerExpressionWriter {
    // Closure
    static final MethodCaller getMethodPointer = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getMethodPointer");

    private final WriterController controller;

    public MethodPointerExpressionWriter(final WriterController controller) {
        this.controller = controller;
    }

    public void writeMethodPointerExpression(MethodPointerExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(controller.getAcg());
        controller.getOperandStack().box();
        controller.getOperandStack().pushDynamicName(expression.getMethodName());
        getMethodPointer.call(controller.getMethodVisitor());
        controller.getOperandStack().replace(ClassHelper.CLOSURE_TYPE,2);
    }
}
