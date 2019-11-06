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
package groovy.annotations;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Traces @MyIntegerAnno annotation
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MyIntegerAnnoTraceASTTransformation implements ASTTransformation {
    public static List<String> trace = new ArrayList<>();

    public void visit(ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        File f = new File("temp/log.txt");
        try {
            ResourceGroovyMethods.append(f, parent.getClass().getSimpleName() + " " +
                    parent.getAnnotations().get(0).getMember("value").getText() + " ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}