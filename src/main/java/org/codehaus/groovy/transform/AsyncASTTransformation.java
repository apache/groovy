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

import groovy.transform.Async;
import groovy.util.concurrent.async.AsyncHelper;
import groovy.util.concurrent.async.Promise;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;

/**
 * Handles generation of code for the {@link Async} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AsyncASTTransformation extends AbstractASTTransformation {
    private static final ClassNode MY_TYPE = make(Async.class);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode ASYNC_HELPER_TYPE = make(AsyncHelper.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) annotatedNode;
            if (methodNode.isAbstract()) {
                addError("Annotation " + MY_TYPE_NAME + " cannot be used for abstract methods.", methodNode);
                return;
            }

            Statement origCode = methodNode.getCode();
            BlockStatement newCode = GeneralUtils.asyncS(origCode);
            methodNode.setCode(newCode);
            ClassNode wrappedOrigReturnType = ClassHelper.getWrapper(methodNode.getReturnType());
            ClassNode promiseType = GenericsUtils.makeClassSafeWithGenerics(makeWithoutCaching(Promise.class), new GenericsType(wrappedOrigReturnType));
            methodNode.setReturnType(promiseType);
            VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit);
            variableScopeVisitor.visitClass(methodNode.getDeclaringClass());
        }
    }
}
