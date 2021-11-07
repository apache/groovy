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

import groovy.transform.RecordBase;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;

/**
 * Handles completion of code for the @RecordType annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class RecordCompletionASTTransformation extends AbstractASTTransformation {

    private static final Class<? extends Annotation> MY_CLASS = RecordBase.class;
    public static final ClassNode MY_TYPE = makeWithoutCaching(MY_CLASS, false);
    private static final String MY_TYPE_NAME = MY_TYPE.getNameWithoutPackage();

    @Override
    public String getAnnotationName() {
        return MY_TYPE_NAME;
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            MethodNode copyWith = cNode.getMethod("copyWith", params(param(MAP_TYPE, "namedArgs")));
            if (copyWith != null) {
                adjustCopyWith(cNode, copyWith);
            }
        }
    }

    // when the record classnode was processed, the tuple constructor hadn't been added yet, so manually adjust here
    private void adjustCopyWith(ClassNode cNode, MethodNode copyWith) {
        final List<Parameter> params = new ArrayList<>();
        final List<PropertyNode> pList = getInstanceProperties(cNode);
        for (int i = 0; i < pList.size(); i++) {
            PropertyNode pNode = pList.get(i);
            params.add(param(pNode.getType(), "arg" + i));
        }

        ConstructorNode consNode = cNode.getDeclaredConstructor(params.toArray(Parameter.EMPTY_ARRAY));
        if (consNode != null) {
            Statement code = copyWith.getCode();
            if (code instanceof ReturnStatement) {
                ReturnStatement rs = (ReturnStatement) code;
                Expression expr = rs.getExpression();
                if (expr instanceof ConstructorCallExpression) {
                    expr.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, consNode);
                }
            }
        }
    }
}
