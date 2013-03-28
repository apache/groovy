/*
 * Copyright 2008-2013 the original author or authors.
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

import groovy.transform.WithReadLock;
import groovy.transform.WithWriteLock;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Handles generation of code for the {@code @}WithReadLock and {@code @}WithWriteLock annotation.<br>
 * This transformation adds an instance of ReentrantReadWriteLock to the class.<br>
 * Any method annotated with {@code @}WithReadLock will obtain a read lock and release it in a finally block.<br>
 * Any method annotated with {@code @}WithWriteLock will obtain a write lock and release it in a finally block.<br>
 * For more information see {@link WithReadLock} and {@link WithWriteLock}
 *
 * @author Hamlet D'Arcy
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ReadWriteLockASTTransformation implements ASTTransformation, Opcodes {

    private static final ClassNode READ_LOCK_TYPE = ClassHelper.make(WithReadLock.class);
    private static final ClassNode WRITE_LOCK_TYPE = ClassHelper.make(WithWriteLock.class);
    private static final ClassNode LOCK_TYPE = ClassHelper.make(ReentrantReadWriteLock.class);
    private static final Token ASSIGN = Token.newSymbol("=", -1, -1);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        final boolean isWriteLock;
        if (READ_LOCK_TYPE.equals(node.getClassNode())) {
            isWriteLock = false;
        } else if (WRITE_LOCK_TYPE.equals(node.getClassNode())) {
            isWriteLock = true;
        } else {
            throw new RuntimeException("Internal error: expecting [" + READ_LOCK_TYPE.getName() + ", " + WRITE_LOCK_TYPE.getName() + "]" + " but got: " + node.getClassNode().getName());
        }

        String myTypeName = "@" + node.getClassNode().getNameWithoutPackage();

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
            String lockExpr = determineLock(value, cNode, mNode.isStatic(), myTypeName);

            // get lock type
            final Expression lockType;
            if (isWriteLock) {
                lockType = new MethodCallExpression(new VariableExpression(lockExpr), "writeLock", ArgumentListExpression.EMPTY_ARGUMENTS);
            } else {
                lockType = new MethodCallExpression(new VariableExpression(lockExpr), "readLock", ArgumentListExpression.EMPTY_ARGUMENTS);
            }

            MethodCallExpression acquireLock = new MethodCallExpression(lockType, "lock", ArgumentListExpression.EMPTY_ARGUMENTS);
            MethodCallExpression releaseLock = new MethodCallExpression(lockType, "unlock", ArgumentListExpression.EMPTY_ARGUMENTS);
            Statement originalCode = mNode.getCode();

            BlockStatement body = new BlockStatement();
            body.addStatement(new ExpressionStatement(acquireLock));
            body.addStatement(new TryCatchStatement(originalCode, new ExpressionStatement(releaseLock)));
            mNode.setCode(body);
        }
    }

    private String determineLock(String value, ClassNode targetClass, boolean isStatic, String myTypeName) {
        if (value != null && value.length() > 0 && !value.equalsIgnoreCase("$reentrantlock")) {
            if (targetClass.getDeclaredField(value) == null) {
                throw new RuntimeException("Error during " + myTypeName + " processing: lock field with name '" + value + "' not found in class " + targetClass.getName());
            }
            if (targetClass.getDeclaredField(value).isStatic() != isStatic) {
                throw new RuntimeException("Error during " + myTypeName + " processing: lock field with name '" + value + "' should " + (isStatic ? "" : "not ") + "be static");
            }
            return value;
        }
        if (isStatic) {
            FieldNode field = targetClass.getDeclaredField("$REENTRANTLOCK");
            if (field == null) {
                int visibility = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
                targetClass.addField("$REENTRANTLOCK", visibility, LOCK_TYPE, createLockObject());
            } else if (!field.isStatic()) {
                throw new RuntimeException("Error during " + myTypeName + " processing: $REENTRANTLOCK field must be static");
            }
            return "$REENTRANTLOCK";
        }
        FieldNode field = targetClass.getDeclaredField("$reentrantlock");
        if (field == null) {
            int visibility = ACC_PRIVATE | ACC_FINAL;
            targetClass.addField("$reentrantlock", visibility, LOCK_TYPE, createLockObject());
        } else if (field.isStatic()) {
            throw new RuntimeException("Error during " + myTypeName + " processing: $reentrantlock field must not be static");
        }
        return "$reentrantlock";
    }

    private Expression createLockObject() {
        return new ConstructorCallExpression(LOCK_TYPE, MethodCallExpression.NO_ARGUMENTS);
    }
}
