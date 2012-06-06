/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Handles generation of code for the {@code @NotYetImplemented} annotation.
 * 
 * @see groovy.transform.NotYetImplemented
 *
 * @author Dierk König
 * @author Andre Steingress
 * @author Ilinca V. Hallberg
 * @author Björn Westlin
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class NotYetImplementedASTTransformation extends AbstractASTTransformation {

    private static final ClassNode CATCHED_THROWABLE_TYPE = ClassHelper.make(Throwable.class);
    private static final ClassNode ASSERTION_FAILED_ERROR_TYPE = ClassHelper.make("junit.framework.AssertionFailedError");

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

        if (statements.size() == 0) return;

        BlockStatement rewrittenMethodCode = new BlockStatement();

        rewrittenMethodCode.addStatement(tryCatchAssertionFailedError(annotationNode, methodNode, statements));
        rewrittenMethodCode.addStatement(throwAssertionFailedError(annotationNode));

        methodNode.setCode(rewrittenMethodCode);
    }

    private TryCatchStatement tryCatchAssertionFailedError(AnnotationNode annotationNode, MethodNode methodNode, ArrayList<Statement> statements) {
        TryCatchStatement tryCatchStatement = new TryCatchStatement(new BlockStatement(statements, methodNode.getVariableScope()), EmptyStatement.INSTANCE);
        tryCatchStatement.addCatch(new CatchStatement(new Parameter(CATCHED_THROWABLE_TYPE, "ex"), ReturnStatement.RETURN_NULL_OR_VOID));
        return tryCatchStatement;
    }

    private Statement throwAssertionFailedError(AnnotationNode annotationNode) {
        ThrowStatement throwStatement = new ThrowStatement(
                new ConstructorCallExpression(ASSERTION_FAILED_ERROR_TYPE,
                        new ArgumentListExpression(
                                new ConstantExpression("Method is marked with @NotYetImplemented but passes unexpectedly"))));

        throwStatement.setSourcePosition(annotationNode);

        return throwStatement;
    }
}
