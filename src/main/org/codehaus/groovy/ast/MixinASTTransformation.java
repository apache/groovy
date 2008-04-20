package org.codehaus.groovy.ast;

import groovy.lang.Mixin;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTSingleNodeTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import java.util.Iterator;

@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class MixinASTTransformation implements ASTSingleNodeTransformation {
    private static final ClassNode useClassNode = new ClassNode(Mixin.class);

    public void visit(ASTNode nodes[], SourceUnit source, GeneratorContext generatorContext) {
        AnnotationNode node = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];

        if (!useClassNode.equals(node.getClassNode()))
          return;

        final Expression expr = node.getMember("value");
        if (expr == null) {
            return;
        }

        Expression useClasses = null;
        if (expr instanceof ClassExpression) {
            useClasses = expr;
        }

        if (expr instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) expr;
            for (Iterator it = listExpression.getExpressions().iterator(); it.hasNext(); ) {
                Expression ex = (Expression) it.next();
                if (!(ex instanceof ClassExpression))
                  return;
            }

            useClasses = expr;
        }

        if (useClasses == null)
          return;

        if (parent instanceof ClassNode) {
            ClassNode annotatedClass = (ClassNode) parent;

            final Parameter[] NOPARAMS = new Parameter[0];
            MethodNode clinit = annotatedClass.getDeclaredMethod("<clinit>", NOPARAMS);
            if (clinit == null) {
                clinit = annotatedClass.addMethod("<clinit>", Opcodes.ACC_PUBLIC| Opcodes.ACC_STATIC, ClassHelper.VOID_TYPE, NOPARAMS, null, new BlockStatement());
                clinit.setSynthetic(true);
            }

            final BlockStatement code = (BlockStatement) clinit.getCode();
            code.addStatement(
                    new ExpressionStatement(
                            new MethodCallExpression(
                                    new ClassExpression(annotatedClass),
                                    "mixin",
                                    useClasses
                            )
                    )
            );
        }
    }
}
