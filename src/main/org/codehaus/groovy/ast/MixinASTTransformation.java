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

package org.codehaus.groovy.ast;

import groovy.lang.Mixin;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MixinASTTransformation implements ASTTransformation {
    private static final ClassNode MY_TYPE = ClassHelper.make(Mixin.class);

    // TODO would it be better to actually statically mixin the methods?
    public void visit(ASTNode nodes[], SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }
        AnnotationNode node = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];

        if (!MY_TYPE.equals(node.getClassNode()))
            return;

        final Expression expr = node.getMember("value");
        if (expr == null) {
            return;
        }

        Expression useClasses = null;
        if (expr instanceof ClassExpression) {
            useClasses = expr;
        } else if (expr instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) expr;
            for (Expression ex : listExpression.getExpressions()) {
                if (!(ex instanceof ClassExpression))
                    return;
            }
            useClasses = expr;
        }

        if (useClasses == null)
            return;

        if (parent instanceof ClassNode) {
            ClassNode annotatedClass = (ClassNode) parent;

            final Parameter[] noparams = new Parameter[0];
            MethodNode clinit = annotatedClass.getDeclaredMethod("<clinit>", noparams);
            if (clinit == null) {
                clinit = annotatedClass.addMethod("<clinit>", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, ClassHelper.VOID_TYPE, noparams, null, new BlockStatement());
                clinit.setSynthetic(true);
            }

            final BlockStatement code = (BlockStatement) clinit.getCode();
            code.addStatement(
                    new ExpressionStatement(
                            new MethodCallExpression(
                                    new PropertyExpression(new ClassExpression(annotatedClass), "metaClass"),
                                    "mixin",
                                    useClasses
                            )
                    )
            );
        }
    }
}
