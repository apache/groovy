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
package org.codehaus.groovy.transform;

import junit.framework.AssertionFailedError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.Arrays;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.catchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;

/**
 * Handles generation of code for the {@code @NotYetImplemented} annotation.
 * 
 * @see groovy.transform.NotYetImplemented
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class NotYetImplementedASTTransformation extends AbstractASTTransformation {

    private static final ClassNode CATCHED_THROWABLE_TYPE = ClassHelper.make(Throwable.class);
    private static final ClassNode ASSERTION_FAILED_ERROR_TYPE = ClassHelper.make(AssertionFailedError.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        ASTNode node = nodes[1];

        if (!(node instanceof MethodNode))  {
            addError("@NotYetImplemented must only be applied on test methods!",node);
            return;
        }

        MethodNode methodNode = (MethodNode) node;

        ArrayList<Statement> statements = new ArrayList<Statement>();
        Statement statement = methodNode.getCode();
        if (statement instanceof BlockStatement)  {
            statements.addAll(((BlockStatement) statement).getStatements());
        }

        if (statements.isEmpty()) return;

        BlockStatement rewrittenMethodCode = new BlockStatement();

        rewrittenMethodCode.addStatement(tryCatchAssertionFailedError(annotationNode, methodNode, statements));
        rewrittenMethodCode.addStatement(throwAssertionFailedError(annotationNode));

        methodNode.setCode(rewrittenMethodCode);
    }

    private TryCatchStatement tryCatchAssertionFailedError(AnnotationNode annotationNode, MethodNode methodNode, ArrayList<Statement> statements) {
        TryCatchStatement tryCatchStatement = new TryCatchStatement(
                block(methodNode.getVariableScope(), statements),
                EmptyStatement.INSTANCE);
        tryCatchStatement.addCatch(catchS(param(CATCHED_THROWABLE_TYPE, "ex"), ReturnStatement.RETURN_NULL_OR_VOID));
        return tryCatchStatement;
    }

    private Statement throwAssertionFailedError(AnnotationNode annotationNode) {
        Statement throwStatement = throwS(
                ctorX(ASSERTION_FAILED_ERROR_TYPE,
                        args(constX("Method is marked with @NotYetImplemented but passes unexpectedly"))));
        throwStatement.setSourcePosition(annotationNode);

        return throwStatement;
    }
}
