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

import groovy.transform.CompileStatic;
import groovy.transform.TypeCheckingMode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Collections;
import java.util.List;

public class CompileDynamicProcessor extends AnnotationCollectorTransform {

    private static final ClassNode COMPILESTATIC_NODE = ClassHelper.make(CompileStatic.class);
    private static final ClassNode TYPECHECKINGMODE_NODE = ClassHelper.make(TypeCheckingMode.class);

    @Override
    public List<AnnotationNode> visit(AnnotationNode collector, AnnotationNode aliasAnnotationUsage, AnnotatedNode aliasAnnotated, SourceUnit source) {
        AnnotationNode node = new AnnotationNode(COMPILESTATIC_NODE);
        node.addMember("value", new PropertyExpression(new ClassExpression(TYPECHECKINGMODE_NODE), "SKIP"));
        return Collections.singletonList(node);
    }
}
