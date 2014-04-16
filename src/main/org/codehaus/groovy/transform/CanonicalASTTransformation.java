/*
 * Copyright 2008-2014 the original author or authors.
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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
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
    static final ClassNode MY_TYPE = make(MY_CLASS);
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
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            List<String> excludes = getMemberList(anno, "excludes");
            List<String> includes = getMemberList(anno, "includes");
            if (!checkIncludeExclude(anno, excludes, includes, MY_TYPE_NAME)) return;
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
