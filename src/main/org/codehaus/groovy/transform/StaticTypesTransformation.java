/*
 * Copyright 2003-2010 the original author or authors.
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
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

/**
 * Handles the implementation of the {@link groovy.transform.TypeChecked} transformation.
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @author Cedric Champeau
 * @author Guillaume Laforge
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class StaticTypesTransformation implements ASTTransformation {

    public static final String STATIC_ERROR_PREFIX = "[Static type checking] - ";

//    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
//        AnnotationNode annotationInformation = (AnnotationNode) nodes[0];
        AnnotatedNode node = (AnnotatedNode) nodes[1];
        if (node instanceof ClassNode) {
            StaticTypeCheckingVisitor visitor = newVisitor(source, (ClassNode) node);
            visitor.visitClass((ClassNode) node);
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode)node;
            StaticTypeCheckingVisitor visitor = newVisitor(source, methodNode.getDeclaringClass());
            visitor.visitMethod(methodNode);
        } else {
            source.addError(new SyntaxException(STATIC_ERROR_PREFIX + "Unimplemented node type", node.getLineNumber(), node.getColumnNumber()));
        }
    }

    /**
     * Allows subclasses to provide their own visitor. This is useful for example for transformations relying
     * on the static type checker.
     * @param unit the source unit
     * @param node the current classnode
     * @return a static type checking visitor
     */
    protected StaticTypeCheckingVisitor newVisitor(SourceUnit unit, ClassNode node) {
        return new StaticTypeCheckingVisitor(unit, node);
    }

    public static enum StaticTypesMarker {
        INFERRED_TYPE, // used to store type information on class nodes
        DECLARATION_INFERRED_TYPE, // in flow analysis, represents the type of the declaration node lhs
        CLOSURE_INFERRED_RETURN_TYPE, // used to store closure argument types on a variable expression
        CLOSURE_ARGUMENTS // used to store closure argument types on a variable expression
    }
}
