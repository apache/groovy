/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast;

import groovy.lang.Mixin;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

/**
 * @deprecated static mixins have been deprecated in favour of traits (trait keyword).
 */
@Deprecated
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MixinASTTransformation extends AbstractASTTransformation {
    private static final ClassNode MY_TYPE = make(Mixin.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
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

            final Parameter[] noparams = Parameter.EMPTY_ARRAY;
            MethodNode clinit = annotatedClass.getDeclaredMethod("<clinit>", noparams);
            if (clinit == null) {
                clinit = annotatedClass.addMethod("<clinit>", ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, ClassHelper.VOID_TYPE, noparams, null, new BlockStatement());
                clinit.setSynthetic(true);
            }

            final BlockStatement code = (BlockStatement) clinit.getCode();
            code.addStatement(
                    stmt(callX(propX(classX(annotatedClass), "metaClass"), "mixin", useClasses))
            );
        }
    }
}
