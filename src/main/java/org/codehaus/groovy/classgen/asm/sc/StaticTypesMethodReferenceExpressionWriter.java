package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.classgen.asm.MethodReferenceExpressionWriter;
import org.codehaus.groovy.classgen.asm.WriterController;

/**
 * Writer responsible for generating method reference in statically compiled mode.
 * @since 3.0.0
 */
public class StaticTypesMethodReferenceExpressionWriter extends MethodReferenceExpressionWriter {
    public StaticTypesMethodReferenceExpressionWriter(WriterController controller) {
        super(controller);
    }

    @Override
    public void writeMethodReferenceExpression(MethodReferenceExpression expression) {
        super.writeMethodReferenceExpression(expression); // TODO generate native method reference bytecode here
    }
}
