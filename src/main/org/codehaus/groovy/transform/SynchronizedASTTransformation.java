/*
 * Copyright 2008-2010 the original author or authors.
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

import groovy.transform.Synchronized;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;


/**
 * Handles generation of code for the {@code @}Synchronized annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class SynchronizedASTTransformation implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = Synchronized.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;
        Expression valueExpr = node.getMember("value");
        String value = null;
        if (valueExpr instanceof ConstantExpression) {
            ConstantExpression ce = (ConstantExpression) valueExpr;
            Object valueObject = ce.getValue();
            if (valueObject != null) value = valueObject.toString();
        }

        if (parent instanceof MethodNode) {
            MethodNode mNode = (MethodNode) parent;
            ClassNode cNode = mNode.getDeclaringClass();
            String lockExpr = determineLock(value, cNode, mNode.isStatic());
            Statement origCode = mNode.getCode();
            Statement newCode = new SynchronizedStatement(new VariableExpression(lockExpr), origCode);
            mNode.setCode(newCode);
        }
    }

    private String determineLock(String value, ClassNode cNode, boolean isStatic) {
        if (value != null && value.length() > 0 && !value.equalsIgnoreCase("$lock")) {
            if (cNode.getDeclaredField(value) == null) {
                throw new RuntimeException("Error during " + MY_TYPE_NAME + " processing: lock field with name '" + value + "' not found in class " + cNode.getName());
            }
            if (cNode.getDeclaredField(value).isStatic() != isStatic) {
                throw new RuntimeException("Error during " + MY_TYPE_NAME + " processing: lock field with name '" + value + "' should " + (isStatic ? "" : "not ") + "be static");
            }
            return value;
        }
        if (isStatic) {
            FieldNode field = cNode.getDeclaredField("$LOCK");
            if (field == null) {
                int visibility = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
                cNode.addField("$LOCK", visibility, ClassHelper.OBJECT_TYPE, zeroLengthObjectArray());
            } else if (!field.isStatic()) {
                throw new RuntimeException("Error during " + MY_TYPE_NAME + " processing: $LOCK field must be static");
            }
            return "$LOCK";
        }
        FieldNode field = cNode.getDeclaredField("$lock");
        if (field == null) {
            int visibility = ACC_PRIVATE | ACC_FINAL;
            cNode.addField("$lock", visibility, ClassHelper.OBJECT_TYPE, zeroLengthObjectArray());
        } else if (field.isStatic()) {
            throw new RuntimeException("Error during " + MY_TYPE_NAME + " processing: $lock field must not be static");
        }
        return "$lock";
    }

    private Expression zeroLengthObjectArray() {
        return new ArrayExpression(ClassHelper.OBJECT_TYPE, null, Arrays.asList((Expression)new ConstantExpression(0)));
    }

}
