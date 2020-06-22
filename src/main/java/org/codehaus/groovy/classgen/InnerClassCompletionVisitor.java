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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.function.BiConsumer;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.ast.tools.ConstructorNodeUtils.getFirstIfSpecialConstructorCall;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getCodeAsBlock;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public class InnerClassCompletionVisitor extends InnerClassVisitorHelper implements Opcodes {

    private ClassNode classNode;
    private FieldNode thisField;
    private final SourceUnit sourceUnit;

    private static final String
            CLOSURE_INTERNAL_NAME   = BytecodeHelper.getClassInternalName(CLOSURE_TYPE),
            CLOSURE_DESCRIPTOR      = BytecodeHelper.getTypeDescription(CLOSURE_TYPE);

    public InnerClassCompletionVisitor(CompilationUnit cu, SourceUnit su) {
        sourceUnit = su;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitClass(ClassNode node) {
        classNode = node;
        thisField = null;
        InnerClassNode innerClass = null;
        if (!node.isEnum() && !node.isInterface() && node instanceof InnerClassNode) {
            innerClass = (InnerClassNode) node;
            thisField = innerClass.getField("this$0");
            if (innerClass.getVariableScope() == null && innerClass.getDeclaredConstructors().isEmpty()) {
                // add empty default constructor
                addGeneratedConstructor(innerClass, ACC_PUBLIC, Parameter.EMPTY_ARRAY, null, null);
            }
        }
        if (node.isEnum() || node.isInterface()) return;
        // use Iterator.hasNext() to check for available inner classes
        if (node.getInnerClasses().hasNext()) addDispatcherMethods(node);
        if (innerClass == null) return;
        super.visitClass(node);
        addMopMethods(innerClass);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        addThisReference(node);
        super.visitConstructor(node);
    }

    private static String getTypeDescriptor(ClassNode node, boolean isStatic) {
        return BytecodeHelper.getTypeDescription(getClassNode(node, isStatic));
    }

    private static String getInternalName(ClassNode node, boolean isStatic) {
        return BytecodeHelper.getClassInternalName(getClassNode(node, isStatic));
    }

    private static void addDispatcherMethods(ClassNode classNode) {
        final int objectDistance = getObjectDistance(classNode);

        // since we added an anonymous inner class we should also
        // add the dispatcher methods

        // add method dispatcher
        BlockStatement block = new BlockStatement();
        MethodNode method = classNode.addSyntheticMethod(
                "this$dist$invoke$" + objectDistance,
                ACC_PUBLIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "args")),
                ClassNode.EMPTY_ARRAY,
                block
        );
        setMethodDispatcherCode(block, VariableExpression.THIS_EXPRESSION, method.getParameters());

        // add property setter
        block = new BlockStatement();
        method = classNode.addSyntheticMethod(
                "this$dist$set$" + objectDistance,
                ACC_PUBLIC,
                VOID_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "value")),
                ClassNode.EMPTY_ARRAY,
                block
        );
        setPropertySetterDispatcher(block, VariableExpression.THIS_EXPRESSION, method.getParameters());

        // add property getter
        block = new BlockStatement();
        method = classNode.addSyntheticMethod(
                "this$dist$get$" + objectDistance,
                ACC_PUBLIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name")),
                ClassNode.EMPTY_ARRAY,
                block
        );
        setPropertyGetterDispatcher(block, VariableExpression.THIS_EXPRESSION, method.getParameters());
    }

    private void getThis(MethodVisitor mv, String classInternalName, String outerClassDescriptor, String innerClassInternalName) {
        mv.visitVarInsn(ALOAD, 0);
        if (thisField != null && CLOSURE_TYPE.equals(thisField.getType())) {
            mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", CLOSURE_DESCRIPTOR);
            mv.visitMethodInsn(INVOKEVIRTUAL, CLOSURE_INTERNAL_NAME, "getThisObject", "()Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, innerClassInternalName);
        } else {
            mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", outerClassDescriptor);
        }
    }

    private void addMopMethods(final InnerClassNode node) {
        final boolean isStatic = isStatic(node);
        final ClassNode outerClass = node.getOuterClass();
        final int outerClassDistance = getObjectDistance(outerClass);
        final String classInternalName = BytecodeHelper.getClassInternalName(node);
        final String outerClassInternalName = getInternalName(outerClass, isStatic);
        final String outerClassDescriptor = getTypeDescriptor(outerClass, isStatic);

        addSyntheticMethod(node,
                "methodMissing",
                ACC_PUBLIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "args")),
                (methodBody, parameters) -> {
                    if (isStatic) {
                        setMethodDispatcherCode(methodBody, classX(outerClass), parameters);
                    } else {
                        methodBody.addStatement(
                                new BytecodeSequence(new BytecodeInstruction() {
                                    @Override
                                    public void visit(final MethodVisitor mv) {
                                        getThis(mv, classInternalName, outerClassDescriptor, outerClassInternalName);
                                        mv.visitVarInsn(ALOAD, 1);
                                        mv.visitVarInsn(ALOAD, 2);
                                        mv.visitMethodInsn(INVOKEVIRTUAL, outerClassInternalName, "this$dist$invoke$" + outerClassDistance, "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", false);
                                        mv.visitInsn(ARETURN);
                                    }
                                })
                        );
                    }
                }
        );

        addSyntheticMethod(node,
                "$static_methodMissing",
                ACC_PUBLIC | ACC_STATIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "args")),
                (methodBody, parameters) -> {
                    setMethodDispatcherCode(methodBody, classX(outerClass), parameters);
                }
        );

        addSyntheticMethod(node,
                "propertyMissing",
                ACC_PUBLIC,
                VOID_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "value")),
                (methodBody, parameters) -> {
                    if (isStatic) {
                        setPropertySetterDispatcher(methodBody, classX(outerClass), parameters);
                    } else {
                        methodBody.addStatement(
                                new BytecodeSequence(new BytecodeInstruction() {
                                    @Override
                                    public void visit(final MethodVisitor mv) {
                                        getThis(mv, classInternalName, outerClassDescriptor, outerClassInternalName);
                                        mv.visitVarInsn(ALOAD, 1);
                                        mv.visitVarInsn(ALOAD, 2);
                                        mv.visitMethodInsn(INVOKEVIRTUAL, outerClassInternalName, "this$dist$set$" + outerClassDistance, "(Ljava/lang/String;Ljava/lang/Object;)V", false);
                                        mv.visitInsn(RETURN);
                                    }
                                })
                        );
                    }
                }
        );

        addSyntheticMethod(node,
                "$static_propertyMissing",
                ACC_PUBLIC | ACC_STATIC,
                VOID_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "value")),
                (methodBody, parameters) -> {
                    setPropertySetterDispatcher(methodBody, classX(outerClass), parameters);
                }
        );

        addSyntheticMethod(node,
                "propertyMissing",
                ACC_PUBLIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name")),
                (methodBody, parameters) -> {
                    if (isStatic) {
                        setPropertyGetterDispatcher(methodBody, classX(outerClass), parameters);
                    } else {
                        methodBody.addStatement(
                                new BytecodeSequence(new BytecodeInstruction() {
                                    @Override
                                    public void visit(final MethodVisitor mv) {
                                        getThis(mv, classInternalName, outerClassDescriptor, outerClassInternalName);
                                        mv.visitVarInsn(ALOAD, 1);
                                        mv.visitMethodInsn(INVOKEVIRTUAL, outerClassInternalName, "this$dist$get$" + outerClassDistance, "(Ljava/lang/String;)Ljava/lang/Object;", false);
                                        mv.visitInsn(ARETURN);
                                    }
                                })
                        );
                    }
                }
        );

        addSyntheticMethod(node,
                "$static_propertyMissing",
                ACC_PUBLIC | ACC_STATIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name")),
                (methodBody, parameters) -> {
                    setPropertyGetterDispatcher(methodBody, classX(outerClass), parameters);
                }
        );
    }

    private void addSyntheticMethod(final InnerClassNode node, final String methodName, final int modifiers,
            final ClassNode returnType, final Parameter[] parameters, final BiConsumer<BlockStatement, Parameter[]> consumer) {
        MethodNode method = node.getMethod(methodName, parameters);
        if (method != null) {
            // GROOVY-8914: pre-compiled classes lose synthetic boolean - TODO fix earlier as per GROOVY-4346 then remove extra check here
            if (isStatic(node) && !method.isSynthetic() && (method.getModifiers() & ACC_SYNTHETIC) == 0) {
                // if there is a user-defined methodNode, add compiler error and continue
                addError("\"" + methodName + "\" implementations are not supported on static inner classes as " +
                    "a synthetic version of \"" + methodName + "\" is added during compilation for the purpose " +
                    "of outer class delegation.",
                    method);
            }
            return;
        }

        BlockStatement methodBody = block();
        consumer.accept(methodBody, parameters);
        node.addSyntheticMethod(methodName, modifiers, returnType, parameters, ClassNode.EMPTY_ARRAY, methodBody);
    }

    private void addThisReference(ConstructorNode node) {
        if (!shouldHandleImplicitThisForInnerClass(classNode)) return;

        // add "this$0" field init

        //add this parameter to node
        Parameter[] params = node.getParameters();
        Parameter[] newParams = new Parameter[params.length + 1];
        System.arraycopy(params, 0, newParams, 1, params.length);
        String name = getUniqueName(params, node);

        Parameter thisPara = new Parameter(classNode.getOuterClass().getPlainNodeReference(), name);
        newParams[0] = thisPara;
        node.setParameters(newParams);

        BlockStatement block = getCodeAsBlock(node);
        BlockStatement newCode = block();
        addFieldInit(thisPara, thisField, newCode);
        ConstructorCallExpression cce = getFirstIfSpecialConstructorCall(block);
        if (cce == null) {
            cce = ctorSuperX(new TupleExpression());
            block.getStatements().add(0, stmt(cce));
        }
        if (shouldImplicitlyPassThisPara(cce)) {
            // add thisPara to this(...)
            TupleExpression args = (TupleExpression) cce.getArguments();
            List<Expression> expressions = args.getExpressions();
            VariableExpression ve = varX(thisPara.getName());
            ve.setAccessedVariable(thisPara);
            expressions.add(0, ve);
        }
        if (cce.isSuperCall()) {
            // we have a call to super here, so we need to add
            // our code after that
            block.getStatements().add(1, newCode);
        }
        node.setCode(block);
    }

    private boolean shouldImplicitlyPassThisPara(ConstructorCallExpression cce) {
        boolean pass = false;
        ClassNode superCN = classNode.getSuperClass();
        if (cce.isThisCall()) {
            pass = true;
        } else if (cce.isSuperCall()) {
            // if the super class is another non-static inner class in the same outer class hierarchy, implicit this
            // needs to be passed
            if (!superCN.isEnum() && !superCN.isInterface() && superCN instanceof InnerClassNode) {
                InnerClassNode superInnerCN = (InnerClassNode) superCN;
                if (!isStatic(superInnerCN) && classNode.getOuterClass().isDerivedFrom(superCN.getOuterClass())) {
                    pass = true;
                }
            }
        }
        return pass;
    }

    private String getUniqueName(Parameter[] params, ConstructorNode node) {
        String namePrefix = "$p";
        outer:
        for (int i = 0; i < 100; i++) {
            namePrefix = namePrefix + "$";
            for (Parameter p : params) {
                if (p.getName().equals(namePrefix)) continue outer;
            }
            return namePrefix;
        }
        addError("unable to find a unique prefix name for synthetic this reference in inner class constructor", node);
        return namePrefix;
    }
}
