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

import groovy.transform.WithReadLock;
import groovy.transform.WithWriteLock;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;


/**
 * Handles generation of code for the {@code @}WithReadLock and {@code @}WithWriteLock annotation.<br>
 * This transformation adds an instance of ReentrantReadWriteLock to the class.<br>
 * Any method annotated with {@code @}WithReadLock will obtain a read lock and release it in a finally block.<br>
 * Any method annotated with {@code @}WithWriteLock will obtain a write lock and release it in a finally block.<br>
 * For more information see {@link WithReadLock} and {@link WithWriteLock}
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ReadWriteLockASTTransformation extends AbstractASTTransformation {

    private static final ClassNode READ_LOCK_TYPE = make(WithReadLock.class);
    private static final ClassNode WRITE_LOCK_TYPE = make(WithWriteLock.class);
    private static final ClassNode LOCK_TYPE = make(ReentrantReadWriteLock.class);
    public static final String DEFAULT_STATIC_LOCKNAME = "$REENTRANTLOCK";
    public static final String DEFAULT_INSTANCE_LOCKNAME = "$reentrantlock";

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        final boolean isWriteLock;
        if (READ_LOCK_TYPE.equals(node.getClassNode())) {
            isWriteLock = false;
        } else if (WRITE_LOCK_TYPE.equals(node.getClassNode())) {
            isWriteLock = true;
        } else {
            throw new GroovyBugError("Internal error: expecting [" + READ_LOCK_TYPE.getName() + ", " + WRITE_LOCK_TYPE.getName() + "]" + " but got: " + node.getClassNode().getName());
        }

        String myTypeName = "@" + node.getClassNode().getNameWithoutPackage();

        String value = getMemberStringValue(node, "value");

        if (parent instanceof MethodNode) {
            MethodNode mNode = (MethodNode) parent;
            ClassNode cNode = mNode.getDeclaringClass();
            FieldNode lockExpr = determineLock(value, cNode, mNode.isStatic(), myTypeName);
            if (lockExpr == null) return;

            // get lock type
            final MethodCallExpression lockType = callX(varX(lockExpr), isWriteLock ? "writeLock" : "readLock");
            lockType.setImplicitThis(false);

            MethodCallExpression acquireLock = callX(lockType, "lock");
            acquireLock.setImplicitThis(false);

            MethodCallExpression releaseLock = callX(lockType, "unlock");
            releaseLock.setImplicitThis(false);

            Statement originalCode = mNode.getCode();
            mNode.setCode(block(stmt(acquireLock), tryCatchS(originalCode, stmt(releaseLock))));
        }
    }

    private FieldNode determineLock(String value, ClassNode targetClass, boolean isStatic, String myTypeName) {
        if (value != null && value.length() > 0 && !value.equalsIgnoreCase(DEFAULT_INSTANCE_LOCKNAME)) {
            FieldNode existingLockField = targetClass.getDeclaredField(value);
            if (existingLockField == null) {
                addError("Error during " + myTypeName + " processing: lock field with name '" + value + "' not found in class " + targetClass.getName(), targetClass);
                return null;
            }
            if (existingLockField.isStatic() != isStatic) {
                addError("Error during " + myTypeName + " processing: lock field with name '" + value + "' should " + (isStatic ? "" : "not ") + "be static", existingLockField);
                return null;
            }
            return existingLockField;
        }
        if (isStatic) {
            FieldNode field = targetClass.getDeclaredField(DEFAULT_STATIC_LOCKNAME);
            if (field == null) {
                int visibility = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
                field = targetClass.addField(DEFAULT_STATIC_LOCKNAME, visibility, LOCK_TYPE, createLockObject());
            } else if (!field.isStatic()) {
                addError("Error during " + myTypeName + " processing: " + DEFAULT_STATIC_LOCKNAME + " field must be static", field);
                return null;
            }
            return field;
        }
        FieldNode field = targetClass.getDeclaredField(DEFAULT_INSTANCE_LOCKNAME);
        if (field == null) {
            int visibility = ACC_PRIVATE | ACC_FINAL;
            field = targetClass.addField(DEFAULT_INSTANCE_LOCKNAME, visibility, LOCK_TYPE, createLockObject());
        } else if (field.isStatic()) {
            addError("Error during " + myTypeName + " processing: " + DEFAULT_INSTANCE_LOCKNAME + " field must not be static", field);
            return null;
        }
        return field;
    }

    private static Expression createLockObject() {
        return ctorX(LOCK_TYPE);
    }
}
