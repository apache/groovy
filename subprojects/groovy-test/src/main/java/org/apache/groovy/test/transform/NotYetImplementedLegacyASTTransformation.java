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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.Arrays;
import junit.framework.AssertionFailedError;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.catchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;

/**
 * Generates code for the legacy {@code @NotYetImplemented} annotation.
 *
 * @see groovy.transform.NotYetImplemented
 */
@Deprecated
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class NotYetImplementedLegacyASTTransformation extends AbstractASTTransformation {

    private static final ClassNode CATCH_TYPE = ClassHelper.make(Throwable.class);
    private static final ClassNode THROW_TYPE = ClassHelper.make(AssertionFailedError.class); // TODO: java.lang.AssertionError

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes.length == 2 && nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, MethodNode] but got: " + Arrays.toString(nodes));
        }

        MethodNode methodNode = (MethodNode) nodes[1];

        if (methodNode.getCode() instanceof BlockStatement && !((BlockStatement) methodNode.getCode()).isEmpty()) {
            // wrap code in try/catch with return for failure path followed by throws for success path

            TryCatchStatement tryCatchStatement = tryCatchS(methodNode.getCode());
            tryCatchStatement.addCatch(catchS(param(CATCH_TYPE, "ignore"), ReturnStatement.RETURN_NULL_OR_VOID));

            ThrowStatement throwStatement = throwS(ctorX(THROW_TYPE, args(constX("Method is marked with @NotYetImplemented but passes unexpectedly"))));

            methodNode.setCode(block(tryCatchStatement, throwStatement));
        }
    }
}
