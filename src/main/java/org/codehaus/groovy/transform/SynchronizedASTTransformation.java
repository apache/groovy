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

import groovy.transform.Synchronized;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Collections;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the {@code @Synchronized} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class SynchronizedASTTransformation extends AbstractASTTransformation {

    private static final Class MY_CLASS = Synchronized.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;
        String value = getMemberStringValue(node, "value");

        if (parent instanceof MethodNode) {
            MethodNode mNode = (MethodNode) parent;
            if (mNode.isAbstract()) {
                addError("Error during " + MY_TYPE_NAME + " processing: annotation not allowed on abstract method '" + mNode.getName() + "'", mNode);
                return;
            }
            ClassNode cNode = mNode.getDeclaringClass();
            String lockExpr = determineLock(value, cNode, mNode);
            if (lockExpr == null) return;
            Statement origCode = mNode.getCode();
            Statement newCode = new SynchronizedStatement(varX(lockExpr), origCode);
            mNode.setCode(newCode);
        }
    }

    private String determineLock(String value, ClassNode cNode, MethodNode mNode) {
        boolean isStatic = mNode.isStatic();
        if (value != null && value.length() > 0 && !value.equalsIgnoreCase("$lock")) {
            if (cNode.getDeclaredField(value) == null) {
                addError("Error during " + MY_TYPE_NAME + " processing: lock field with name '" + value + "' not found in class " + cNode.getName(), mNode);
                return null;
            }
            FieldNode field = cNode.getDeclaredField(value);
            if (isStatic && !field.isStatic()) {
                addError("Error during " + MY_TYPE_NAME + " processing: lock field with name '" + value + "' must be static for static method '" + mNode.getName() + "'", field);
                return null;
            }
            return value;
        }
        if (isStatic) {
            FieldNode field = cNode.getDeclaredField("$LOCK");
            if (field == null) {
                int visibility = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
                cNode.addField("$LOCK", visibility, ClassHelper.OBJECT_TYPE, zeroLengthObjectArray());
            } else if (!field.isStatic()) {
                addError("Error during " + MY_TYPE_NAME + " processing: $LOCK field must be static", field);
            }
            return "$LOCK";
        }
        FieldNode field = cNode.getDeclaredField("$lock");
        if (field == null) {
            int visibility = ACC_PRIVATE | ACC_FINAL;
            cNode.addField("$lock", visibility, ClassHelper.OBJECT_TYPE, zeroLengthObjectArray());
        } else if (field.isStatic()) {
            addError("Error during " + MY_TYPE_NAME + " processing: $lock field must not be static", field);
        }
        return "$lock";
    }

    private static Expression zeroLengthObjectArray() {
        return new ArrayExpression(ClassHelper.OBJECT_TYPE, null, Collections.singletonList((Expression) constX(0)));
    }

}
