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

import groovy.transform.NoScript;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Reports a {@link NoScript} annotation that the compiler did not act on.
 * <p>
 * The conversion to a plain class happens when the script class is built
 * (see {@link ModuleNode}), before any transform runs,
 * and the annotations it honors are removed there. One that is still present
 * here is somewhere it has no effect, such as a file with no script code.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class NoScriptASTTransformation extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = ClassHelper.make(NoScript.class);

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        addError("Annotation @" + MY_TYPE.getNameWithoutPackage() + " can only be used in a script file"
                + " containing method definitions or declarations", node);
    }
}
