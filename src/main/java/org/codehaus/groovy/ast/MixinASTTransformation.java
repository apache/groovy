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

import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @deprecated static mixins have been deprecated in favour of traits (trait keyword).
 */
@Deprecated
@GroovyASTTransformation
public class MixinASTTransformation extends AbstractASTTransformation {

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotationNode node = (AnnotationNode) nodes[0];
        AnnotatedNode target = (AnnotatedNode) nodes[1];
        if (!node.getClassNode().getName().equals("groovy.lang.Mixin"))
            return;

        Expression value = node.getMember("value");
        if (value == null) {
            return;
        }

        Expression useClasses = null;
        if (value instanceof ClassExpression) {
            useClasses = value;
        } else if (value instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) value;
            for (Expression ex : listExpression.getExpressions()) {
                if (!(ex instanceof ClassExpression))
                    return;
            }
            useClasses = value;
        }
        if (useClasses == null) {
            return;
        }

        if (target instanceof ClassNode) {
            ClassNode targetClass = (ClassNode) target;

            MethodNode clinit = targetClass.getDeclaredMethod("<clinit>", Parameter.EMPTY_ARRAY);
            if (clinit == null) {
                clinit = targetClass.addSyntheticMethod("<clinit>", ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, null, new BlockStatement());
            }

            ((BlockStatement) clinit.getCode()).addStatement(
                    stmt(callX(callX(targetClass, "getMetaClass"), "mixin", useClasses))
            );
        }
    }
}
