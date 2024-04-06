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

import groovy.transform.Sealed;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;

/**
 * Handles sealed class permitted subclass detection.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class SealedCompletionASTTransformation extends AbstractASTTransformation {

    private static final Class<Sealed> SEALED_CLASS = Sealed.class;
    private static final ClassNode SEALED_TYPE = make(SEALED_CLASS);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!SEALED_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            addDetectedSealedClasses((ClassNode) parent);
        }
    }

    private void addDetectedSealedClasses(ClassNode node) {
        boolean sealed = Boolean.TRUE.equals(node.getNodeMetaData(SEALED_CLASS));
        List<ClassNode> permitted = node.getPermittedSubclasses();
        if (!sealed || !permitted.isEmpty() || node.getModule() == null) return;
        for (ClassNode possibleSubclass : node.getModule().getClasses()) {
            if (possibleSubclass.getSuperClass().equals(node)) {
                permitted.add(possibleSubclass);
            }
            for (ClassNode iface : possibleSubclass.getInterfaces()) {
                if (iface.equals(node)) {
                    permitted.add(possibleSubclass);
                }
            }
        }
        List<Expression> names = new ArrayList<>();
        for (ClassNode next : permitted) {
            names.add(classX(ClassHelper.make(next.getName())));
        }
        AnnotationNode an = node.getAnnotations(SEALED_TYPE).get(0);
        an.addMember("permittedSubclasses", new ListExpression(names));
    }
}
