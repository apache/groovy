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
package org.apache.groovy.contracts.ast;

import org.apache.groovy.contracts.ast.visitor.AnnotationClosureVisitor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.Set;

/**
 * Validates that {@code @Ensures} postconditions on a method only reference
 * fields via {@code old.xxx} that are declared as modifiable by {@code @Modifies}.
 * <p>
 * Runs at {@link CompilePhase#INSTRUCTION_SELECTION} after both:
 * <ul>
 *   <li>{@link ModifiesASTTransformation} has stored the modification set, and</li>
 *   <li>{@link AnnotationClosureVisitor} has recorded the {@code old} references</li>
 * </ul>
 * Then simply compares the two metadata sets.
 *
 * @since 6.0.0
 * @see groovy.contracts.Modifies
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class ModifiesEnsuresValidationTransformation implements ASTTransformation {

    @Override
    @SuppressWarnings("unchecked")
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode annotation)) return;
        if (!(nodes[1] instanceof MethodNode methodNode)) return;

        Set<String> modifiesSet = (Set<String>) methodNode.getNodeMetaData(ModifiesASTTransformation.MODIFIES_FIELDS_KEY);
        if (modifiesSet == null || modifiesSet.isEmpty()) return;

        Set<String> oldRefs = (Set<String>) methodNode.getNodeMetaData(AnnotationClosureVisitor.OLD_REFERENCES_KEY);
        if (oldRefs == null || oldRefs.isEmpty()) return;

        for (String ref : oldRefs) {
            if (!modifiesSet.contains(ref)) {
                source.addError(new SyntaxException(
                        "@Ensures references old." + ref + " but @Modifies does not declare '" + ref + "' as modifiable",
                        annotation.getLineNumber(), annotation.getColumnNumber()));
            }
        }
    }
}
