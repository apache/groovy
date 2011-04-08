/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;
import static org.codehaus.groovy.classgen.InnerClassVisitorHelper.*;

public class InnerClassCompletionVisitor {

    public void visitClass(ClassNode node) {
        InnerClassNode innerClass = null;
        if (!node.isEnum() && !node.isInterface() && node instanceof InnerClassNode) {
            innerClass = (InnerClassNode) node;
        }
        if (node.isEnum() || node.isInterface()) return;
        addDispatcherMethods(node);
        if (innerClass == null) return;

        addDefaultMethods(innerClass);
    }

    private String getTypeDescriptor(ClassNode node, boolean isStatic) {
        return BytecodeHelper.getTypeDescription(getClassNode(node, isStatic));
    }

    private String getInternalName(ClassNode node, boolean isStatic) {
        return BytecodeHelper.getClassInternalName(getClassNode(node, isStatic));
    }

    private void addDispatcherMethods(ClassNode classNode) {
        final int objectDistance = getObjectDistance(classNode);

        // since we added an anonymous inner class we should also
        // add the dispatcher methods

        // add method dispatcher
        Parameter[] parameters = new Parameter[]{
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "args")
        };
        MethodNode method = classNode.addSyntheticMethod(
                "this$dist$invoke$" + objectDistance,
                ACC_PUBLIC + ACC_SYNTHETIC,
                ClassHelper.OBJECT_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                null
        );

        BlockStatement block = new BlockStatement();
        setMethodDispatcherCode(block, VariableExpression.THIS_EXPRESSION, parameters);
        method.setCode(block);

        // add property setter
        parameters = new Parameter[]{
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "value")
        };
        method = classNode.addSyntheticMethod(
                "this$dist$set$" + objectDistance,
                ACC_PUBLIC + ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                null
        );

        block = new BlockStatement();
        setPropertySetterDispatcher(block, VariableExpression.THIS_EXPRESSION, parameters);
        method.setCode(block);

        // add property getter
        parameters = new Parameter[]{
                new Parameter(ClassHelper.STRING_TYPE, "name")
        };
        method = classNode.addSyntheticMethod(
                "this$dist$get$" + objectDistance,
                ACC_PUBLIC + ACC_SYNTHETIC,
                ClassHelper.OBJECT_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                null
        );

        block = new BlockStatement();
        setPropertyGetterDispatcher(block, VariableExpression.THIS_EXPRESSION, parameters);
        method.setCode(block);
    }
    
    private void addDefaultMethods(InnerClassNode node) {
        final boolean isStatic = isStatic(node);

        ClassNode outerClass = node.getOuterClass();
        final String classInternalName = org.codehaus.groovy.classgen.asm.BytecodeHelper.getClassInternalName(node);
        final String outerClassInternalName = getInternalName(outerClass, isStatic);
        final String outerClassDescriptor = getTypeDescriptor(outerClass, isStatic);
        final int objectDistance = getObjectDistance(outerClass);

        // add method dispatcher
        Parameter[] parameters = new Parameter[]{
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "args")
        };
        MethodNode method = node.addSyntheticMethod(
                "methodMissing",
                Opcodes.ACC_PUBLIC,
                ClassHelper.OBJECT_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                null
        );

        BlockStatement block = new BlockStatement();
        if (isStatic) {
            setMethodDispatcherCode(block, new ClassExpression(outerClass), parameters);
        } else {
            block.addStatement(
                    new BytecodeSequence(new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", outerClassDescriptor);
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitMethodInsn(INVOKEVIRTUAL,
                                    outerClassInternalName,
                                    "this$dist$invoke$" + objectDistance,
                                    "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");
                            mv.visitInsn(ARETURN);
                        }
                    })
            );
        }
        method.setCode(block);

        // add property getter dispatcher
        parameters = new Parameter[]{
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "val")
        };
        method = node.addSyntheticMethod(
                "propertyMissing",
                Opcodes.ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                null
        );

        block = new BlockStatement();
        if (isStatic) {
            setPropertySetterDispatcher(block, new ClassExpression(node.getOuterClass()), parameters);
        } else {
            block.addStatement(
                    new BytecodeSequence(new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", outerClassDescriptor);
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitMethodInsn(INVOKEVIRTUAL,
                                    outerClassInternalName,
                                    "this$dist$set$" + objectDistance,
                                    "(Ljava/lang/String;Ljava/lang/Object;)V");
                            mv.visitInsn(RETURN);
                        }
                    })
            );
        }
        method.setCode(block);

        // add property setter dispatcher
        parameters = new Parameter[]{
                new Parameter(ClassHelper.STRING_TYPE, "name")
        };
        method = node.addSyntheticMethod(
                "propertyMissing",
                Opcodes.ACC_PUBLIC,
                ClassHelper.OBJECT_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                null
        );

        block = new BlockStatement();
        if (isStatic) {
            setPropertyGetterDispatcher(block, new ClassExpression(node.getOuterClass()), parameters);
        } else {
            block.addStatement(
                    new BytecodeSequence(new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", outerClassDescriptor);
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitMethodInsn(INVOKEVIRTUAL,
                                    outerClassInternalName,
                                    "this$dist$get$" + objectDistance,
                                    "(Ljava/lang/String;)Ljava/lang/Object;");
                            mv.visitInsn(ARETURN);
                        }
                    })
            );
        }
        method.setCode(block);
    }
}
