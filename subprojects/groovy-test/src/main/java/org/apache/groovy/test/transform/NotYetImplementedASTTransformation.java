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
package org.apache.groovy.test.transform;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.catchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;

/**
 * Generates code for the {@code @NotYetImplemented} annotation.
 *
 * @see groovy.test.NotYetImplemented
 */
@GroovyASTTransformation
public class NotYetImplementedASTTransformation extends AbstractASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        MethodNode methodNode = (MethodNode) nodes[1];
        AnnotationNode annotation = (AnnotationNode) nodes[0];

        ClassNode exception = getMemberClassValue(annotation, "exception");
        boolean   withCause = false;
        if (exception == null) {
            exception = ClassHelper.make(AssertionError.class);
            withCause = true; // AssertionError(String,Throwable) is public
        } else {
            Parameter message = new Parameter(ClassHelper.STRING_TYPE, "message");
            ConstructorNode ctor = exception.getDeclaredConstructor(new Parameter[]{message});
            if (ctor != null && ctor.isPublic()) {
                // all set
            } else {
                ctor = exception.getDeclaredConstructor(new Parameter[]{message, new Parameter(ClassHelper.THROWABLE_TYPE, "cause")});
                if (ctor != null && ctor.isPublic()) {
                    withCause = true;
                } else {
                    addError("Error during @NotYetImplemented processing: supplied exception " + exception.getNameWithoutPackage() + " doesn't have expected String constructor", methodNode);
                }
            }
        }

        if (methodNode.getCode() instanceof BlockStatement && !methodNode.getCode().isEmpty()) {
            // wrap code in try/catch with return for failure path followed by throws for success path

            TryCatchStatement tryCatchStatement = tryCatchS(
                    methodNode.getCode(),
                    EmptyStatement.INSTANCE,
                    catchS(param(ClassHelper.THROWABLE_TYPE.getPlainNodeReference(), "ignore"), ReturnStatement.RETURN_NULL_OR_VOID));

            Expression arguments = constX("Method is marked with @NotYetImplemented but passes unexpectedly");
            if (withCause) arguments = args(arguments, castX(ClassHelper.THROWABLE_TYPE, nullX()));
            ThrowStatement throwStatement = throwS(ctorX(exception, arguments));

            methodNode.setCode(block(tryCatchStatement, throwStatement));
        }
    }
}
