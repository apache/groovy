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

import groovy.transform.InheritConstructors;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersEqual;
import static org.codehaus.groovy.transform.StubberSupport.addStubConstructor;

/**
 * Joint-compilation stubber for {@link InheritConstructors}. Walks the
 * superclass's declared constructors and adds a placeholder constructor on
 * the annotated subclass for each non-private signature not already present
 * — mirroring the surface the full transform produces at CANONICALIZATION,
 * but at CONVERSION so the joint-compilation stub reflects it.
 *
 * <p>The placeholder body is {@code super(args)} to ensure the generated
 * stub Java file compiles cleanly even when the super class has no no-arg
 * constructor; the full transform discards the stubber-tagged placeholder
 * before installing its real body.
 *
 * <p><b>Recursive case.</b> If the superclass also carries
 * {@code @InheritConstructors}, this stubber pre-processes it (the same
 * idiom the full transform uses), so chains like Sub → Mid → Base flatten
 * correctly within the stubber pass.
 *
 * <p><b>Boundary.</b> When the super class is in the same compilation unit
 * and its constructors come from another CONVERSION-phase stubber
 * ({@code @TupleConstructor}, {@code @MapConstructor}), visibility depends
 * on within-phase iteration order: super-declared-first sees those
 * constructors, sub-declared-first does not. The stub remains a subset of
 * the runtime — Java callers reaching for sub's stub get a compile error
 * for non-stubbed signatures rather than a stub/runtime divergence. The
 * full transform at CANONICALIZATION still produces the complete surface
 * at runtime.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class InheritConstructorsASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(InheritConstructors.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        processClass(classNode);
    }

    private void processClass(final ClassNode cNode) {
        ClassNode sNode = cNode.getSuperClass();
        if (sNode == null || ClassHelper.isObjectType(sNode)) return;

        // Recursive: pre-process the super if it also carries @InheritConstructors,
        // so its inherited surface is in place before we copy from it. Mirrors the
        // runtime transform's processClass; the parametersEqual guard below makes
        // re-application benign.
        if (!sNode.getAnnotations(MY_TYPE).isEmpty()) {
            processClass(sNode);
        }

        Map<String, ClassNode> genericsSpec = createGenericsSpec(cNode.getUnresolvedSuperClass());
        for (ConstructorNode cn : sNode.getDeclaredConstructors()) {
            if (cn.isPrivate()) continue;
            Parameter[] origParams = cn.getParameters();
            Parameter[] newParams = new Parameter[origParams.length];
            List<Expression> theArgs = new ArrayList<>(origParams.length);
            for (int i = 0; i < origParams.length; i++) {
                Parameter p = origParams[i];
                ClassNode newType = correctToGenericsSpecRecurse(genericsSpec, p.getType());
                newParams[i] = p.hasInitialExpression()
                        ? param(newType, p.getName(), p.getInitialExpression())
                        : param(newType, p.getName());
                theArgs.add(varX(p.getName(), newType));
            }
            // Skip if any constructor with this signature already exists
            // (hand-written, stubber-tagged, or added by a sibling stubber).
            if (cNode.getDeclaredConstructors().stream().anyMatch(c -> parametersEqual(newParams, c.getParameters()))) continue;
            addStubConstructor(cNode, cn.getModifiers(), newParams, cn.getExceptions(),
                    block(ctorSuperS(args(theArgs))));
        }
    }
}
