/*
 * Copyright 2003-2012 the original author or authors.
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
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class InnerClassCompletionVisitor extends InnerClassVisitorHelper implements Opcodes {

    private final SourceUnit sourceUnit;
    private ClassNode classNode;
    private FieldNode thisField = null;

    public InnerClassCompletionVisitor(CompilationUnit cu, SourceUnit su) {
        sourceUnit = su;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitClass(ClassNode node) {
        this.classNode = node;
        thisField = null;
        InnerClassNode innerClass = null;
        if (!node.isEnum() && !node.isInterface() && node instanceof InnerClassNode) {
            innerClass = (InnerClassNode) node;
            thisField = innerClass.getField("this$0");
            if (innerClass.getVariableScope() == null && innerClass.getDeclaredConstructors().isEmpty()) {
                // add dummy constructor
                innerClass.addConstructor(ACC_PUBLIC, new Parameter[0], null, null);
            }
        }
        if (node.isEnum() || node.isInterface()) return;
        addDispatcherMethods(node);
        if (innerClass == null) return;
        super.visitClass(node);
        addDefaultMethods(innerClass);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        addThisReference(node);
        super.visitConstructor(node);
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

    private boolean shouldHandleImplicitThisForInnerClass(ClassNode cn) {
        if (cn.isEnum() || cn.isInterface()) return false;
        if ((cn.getModifiers() & Opcodes.ACC_STATIC) != 0) return false;
        if (!(cn instanceof InnerClassNode)) return false;
        InnerClassNode innerClass = (InnerClassNode) cn;
        // scope != null means aic, we don't handle that here
        if (innerClass.getVariableScope() != null) return false;
        // static inner classes don't need this$0
        return (innerClass.getModifiers() & ACC_STATIC) == 0;
    }

    private void addThisReference(ConstructorNode node) {
        if (!shouldHandleImplicitThisForInnerClass(classNode)) return;
        Statement code = node.getCode();

        // add "this$0" field init

        //add this parameter to node
        Parameter[] params = node.getParameters();
        Parameter[] newParams = new Parameter[params.length + 1];
        System.arraycopy(params, 0, newParams, 1, params.length);
        String name = getUniqueName(params, node);

        Parameter thisPara = new Parameter(classNode.getOuterClass().getPlainNodeReference(), name);
        newParams[0] = thisPara;
        node.setParameters(newParams);

        BlockStatement block = null;
        if (code == null) {
            block = new BlockStatement();
        } else if (!(code instanceof BlockStatement)) {
            block = new BlockStatement();
            block.addStatement(code);
        } else {
            block = (BlockStatement) code;
        }
        BlockStatement newCode = new BlockStatement();
        addFieldInit(thisPara, thisField, newCode);
        ConstructorCallExpression cce = getFirstIfSpecialConstructorCall(block);
        if (cce == null) {
            cce = new ConstructorCallExpression(ClassNode.SUPER, new TupleExpression());
            block.getStatements().add(0, new ExpressionStatement(cce));
        }
        if (shouldImplicitlyPassThisPara(cce)) {
            // add thisPara to this(...)
            TupleExpression args = (TupleExpression) cce.getArguments();
            List<Expression> expressions = args.getExpressions();
            VariableExpression ve = new VariableExpression(thisPara.getName());
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

    private ConstructorCallExpression getFirstIfSpecialConstructorCall(BlockStatement code) {
        if (code == null) return null;

        final List<Statement> statementList = code.getStatements();
        if (statementList.isEmpty()) return null;

        final Statement statement = statementList.get(0);
        if (!(statement instanceof ExpressionStatement)) return null;

        Expression expression = ((ExpressionStatement) statement).getExpression();
        if (!(expression instanceof ConstructorCallExpression)) return null;
        ConstructorCallExpression cce = (ConstructorCallExpression) expression;
        if (cce.isSpecialCall()) return cce;
        return null;
    }
}
