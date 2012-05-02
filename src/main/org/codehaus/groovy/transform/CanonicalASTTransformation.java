/*
 * Copyright 2008-2012 the original author or authors.
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

import groovy.transform.Canonical;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.codehaus.groovy.transform.EqualsAndHashCodeASTTransformation.createEquals;
import static org.codehaus.groovy.transform.EqualsAndHashCodeASTTransformation.createHashCode;
import static org.codehaus.groovy.transform.ToStringASTTransformation.createToString;
import static org.codehaus.groovy.transform.TupleConstructorASTTransformation.createConstructor;

/**
 * Handles generation of code for the @Canonical annotation.
 *
 * @author Paulo Poiati
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class CanonicalASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = Canonical.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            // TODO remove - let other validation steps pick this up
            if (hasAnnotation(cNode, ImmutableASTTransformation.MY_TYPE)) {
                addError(MY_TYPE_NAME + " class '" + cNode.getName() + "' can't also be " + ImmutableASTTransformation.MY_TYPE_NAME, parent);
            }
            checkNotInterface(cNode, MY_TYPE_NAME);
            List<String> excludes = getMemberList(anno, "excludes");
            List<String> includes = getMemberList(anno, "includes");
            if (includes != null && !includes.isEmpty() && excludes != null && !excludes.isEmpty()) {
                addError("Error during " + MY_TYPE_NAME + " processing: Only one of 'includes' and 'excludes' should be supplied not both.", anno);
            }
            if (!hasAnnotation(cNode, TupleConstructorASTTransformation.MY_TYPE)) {
                createConstructor(cNode, false, true, false, false, false, false, excludes, includes);
            }
            if (!hasAnnotation(cNode, EqualsAndHashCodeASTTransformation.MY_TYPE)) {
                createHashCode(cNode, false, false, false, excludes, includes);
                createEquals(cNode, false, false, true, excludes, includes);
            }
            if (!hasAnnotation(cNode, ToStringASTTransformation.MY_TYPE)) {
                createToString(cNode, false, false, excludes, includes, false);
            }
        }
    }

}
