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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.Verifier;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

public class ClosureWriter {

    public static final String OUTER_INSTANCE = "_outerInstance";
    public static final String THIS_OBJECT = "_thisObject";

    protected interface UseExistingReference {}

    protected final WriterController controller;
    private final Map<Expression,ClassNode> closureClasses = new HashMap<>();

    public ClosureWriter(final WriterController controller) {
        this.controller = controller;
    }

    public void writeClosure(final ClosureExpression expression) {
        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode classNode = controller.getClassNode();
        AsmClassGenerator acg = controller.getAcg();

        // generate closure as public class to make sure it can be properly invoked by classes of the
        // Groovy runtime without circumventing JVM access checks (see CachedMethod for example).
        int mods = ACC_PUBLIC | ACC_FINAL;
        if (classNode.isInterface()) {
            mods |= ACC_STATIC;
        }
        ClassNode closureClass = getOrAddClosureClass(expression, mods);
        String closureClassinternalName = BytecodeHelper.getClassInternalName(closureClass);
        List<ConstructorNode> constructors = closureClass.getDeclaredConstructors();
        ConstructorNode node = constructors.get(0);

        Parameter[] localVariableParams = node.getParameters();

        mv.visitTypeInsn(NEW, closureClassinternalName);
        mv.visitInsn(DUP);
        if (controller.isStaticMethod() || compileStack.isInSpecialConstructorCall()) {
            (new ClassExpression(classNode)).visit(acg);
            (new ClassExpression(controller.getOutermostClass())).visit(acg);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
            loadThis();
        }

        // now let's load the various parameters we're passing
        // we start at index 2 because the first variable we pass
        // is the owner instance and at this point it is already
        // on the stack
        for (int i = 2; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();
            loadReference(name, controller);
            if (param.getNodeMetaData(ClosureWriter.UseExistingReference.class)==null) {
                param.setNodeMetaData(ClosureWriter.UseExistingReference.class,Boolean.TRUE);
            }
        }

        // we may need to pass in some other constructors
        //cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", prototype + ")V");
        mv.visitMethodInsn(INVOKESPECIAL, closureClassinternalName, "<init>", BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, localVariableParams), false);
        controller.getOperandStack().replace(ClassHelper.CLOSURE_TYPE, localVariableParams.length);
    }

    public static void loadReference(final String name, final WriterController controller) {
        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode classNode = controller.getClassNode();
        AsmClassGenerator acg = controller.getAcg();

        // compileStack.containsVariable(name) means to ask if the variable is already declared
        // compileStack.getScope().isReferencedClassVariable(name) means to ask if the variable is a field
        // If it is no field and is not yet declared, then it is either a closure shared variable or
        // an already declared variable.
        if (!compileStack.containsVariable(name) && compileStack.getScope().isReferencedClassVariable(name)) {
            acg.visitFieldExpression(new FieldExpression(classNode.getDeclaredField(name)));
        } else {
            BytecodeVariable v = compileStack.getVariable(name, !classNodeUsesReferences(controller.getClassNode()));
            if (v == null) {
                // variable is not on stack because we are
                // inside a nested Closure and this variable
                // was not used before
                // then load it from the Closure field
                FieldNode field = classNode.getDeclaredField(name);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, controller.getInternalClassName(), name, BytecodeHelper.getTypeDescription(field.getType()));
            } else {
                mv.visitVarInsn(ALOAD, v.getIndex());
            }
            controller.getOperandStack().push(ClassHelper.REFERENCE_TYPE);
        }
    }

    public ClassNode getOrAddClosureClass(final ClosureExpression expression, final int modifiers) {
        ClassNode closureClass = closureClasses.get(expression);
        if (closureClass == null) {
            closureClass = createClosureClass(expression, modifiers);
            closureClasses.put(expression, closureClass);
            controller.getAcg().addInnerClass(closureClass);
            closureClass.addInterface(ClassHelper.GENERATED_CLOSURE_Type);
            closureClass.putNodeMetaData(WriterControllerFactory.class, (WriterControllerFactory) x -> controller);
        }
        return closureClass;
    }

    private static boolean classNodeUsesReferences(final ClassNode classNode) {
        boolean ret = classNode.getSuperClass() == ClassHelper.CLOSURE_TYPE;
        if (ret) return ret;
        if (classNode instanceof InnerClassNode) {
            InnerClassNode inner = (InnerClassNode) classNode;
            return inner.isAnonymous();
        }
        return false;
    }

    protected ClassNode createClosureClass(final ClosureExpression expression, final int modifiers) {
        ClassNode classNode = controller.getClassNode();
        ClassNode outerClass = controller.getOutermostClass();
        String name = genClosureClassName();
        boolean staticMethodOrInStaticClass = controller.isStaticMethod() || classNode.isStaticClass();

        Parameter[] parameters = expression.getParameters();
        if (parameters == null) {
            parameters = Parameter.EMPTY_ARRAY;
        } else if (parameters.length == 0) {
            // let's create a default 'it' parameter
            Parameter it = new Parameter(ClassHelper.OBJECT_TYPE, "it", ConstantExpression.NULL);
            parameters = new Parameter[]{it};
            Variable ref = expression.getVariableScope().getDeclaredVariable("it");
            if (ref!=null) it.setClosureSharedVariable(ref.isClosureSharedVariable());
        }

        Parameter[] localVariableParams = getClosureSharedVariables(expression);
        removeInitialValues(localVariableParams);

        InnerClassNode answer = new InnerClassNode(classNode, name, modifiers, ClassHelper.CLOSURE_TYPE.getPlainNodeReference());
        answer.setEnclosingMethod(controller.getMethodNode());
        answer.setSynthetic(true);
        answer.setUsingGenerics(outerClass.isUsingGenerics());
        answer.setSourcePosition(expression);

        if (staticMethodOrInStaticClass) {
            answer.setStaticClass(true);
        }
        if (controller.isInScriptBody()) {
            answer.setScriptBody(true);
        }
        MethodNode method =
                answer.addMethod("doCall", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, parameters, ClassNode.EMPTY_ARRAY, expression.getCode());
        method.setSourcePosition(expression);

        VariableScope varScope = expression.getVariableScope();
        if (varScope == null) {
            throw new RuntimeException(
                    "Must have a VariableScope by now! for expression: " + expression + " class: " + name);
        } else {
            method.setVariableScope(varScope.copy());
        }
        if (parameters.length > 1
                || (parameters.length == 1
                && parameters[0].getType() != null
                && parameters[0].getType() != ClassHelper.OBJECT_TYPE
                && !ClassHelper.OBJECT_TYPE.equals(parameters[0].getType().getComponentType())))
        {

            // let's add a typesafe call method
            MethodNode call = answer.addMethod(
                    "call",
                    ACC_PUBLIC,
                    ClassHelper.OBJECT_TYPE,
                    parameters,
                    ClassNode.EMPTY_ARRAY,
                    new ReturnStatement(
                            new MethodCallExpression(
                                    VariableExpression.THIS_EXPRESSION,
                                    "doCall",
                                    new ArgumentListExpression(parameters))));
            call.setSourcePosition(expression);
        }

        // let's make the constructor
        BlockStatement block = createBlockStatementForConstructor(expression, outerClass, classNode);

        // let's assign all the parameter fields from the outer context
        addFieldsAndGettersForLocalVariables(answer, localVariableParams);

        addConstructor(expression, localVariableParams, answer, block);

        correctAccessedVariable(answer,expression);

        return answer;
    }

    protected ConstructorNode addConstructor(final ClosureExpression expression, final Parameter[] localVariableParams, final InnerClassNode answer, final BlockStatement block) {
        Parameter[] params = new Parameter[2 + localVariableParams.length];
        params[0] = new Parameter(ClassHelper.OBJECT_TYPE, OUTER_INSTANCE);
        params[1] = new Parameter(ClassHelper.OBJECT_TYPE, THIS_OBJECT);
        System.arraycopy(localVariableParams, 0, params, 2, localVariableParams.length);

        ConstructorNode constructorNode = answer.addConstructor(ACC_PUBLIC, params, ClassNode.EMPTY_ARRAY, block);
        constructorNode.setSourcePosition(expression);

        return constructorNode;
    }

    protected void addFieldsAndGettersForLocalVariables(final InnerClassNode answer, final Parameter[] localVariableParams) {
        for (Parameter param : localVariableParams) {
            String paramName = param.getName();
            ClassNode type = param.getType();
            VariableExpression initialValue = new VariableExpression(paramName);
            initialValue.setAccessedVariable(param);
            initialValue.setUseReferenceDirectly(true);
            ClassNode realType = type;
            type = ClassHelper.makeReference();
            param.setType(ClassHelper.makeReference());
            FieldNode paramField = answer.addField(paramName, ACC_PRIVATE | ACC_SYNTHETIC, type, initialValue);
            paramField.setOriginType(ClassHelper.getWrapper(param.getOriginType()));
            paramField.setHolder(true);
            String methodName = Verifier.capitalize(paramName);

            // let's add a getter & setter
            Expression fieldExp = new FieldExpression(paramField);
            answer.addMethod(
                    "get" + methodName,
                    ACC_PUBLIC,
                    realType.getPlainNodeReference(),
                    Parameter.EMPTY_ARRAY,
                    ClassNode.EMPTY_ARRAY,
                    new ReturnStatement(fieldExp));
        }
    }

    protected BlockStatement createBlockStatementForConstructor(final ClosureExpression expression, final ClassNode outerClass, final ClassNode thisClassNode) {
        BlockStatement block = new BlockStatement();
        // this block does not get a source position, because we don't
        // want this synthetic constructor to show up in corbertura reports
        VariableExpression outer = new VariableExpression(OUTER_INSTANCE, outerClass);
        outer.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(outer);
        VariableExpression thisObject = new VariableExpression(THIS_OBJECT, thisClassNode);
        thisObject.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(thisObject);
        TupleExpression conArgs = new TupleExpression(outer, thisObject);
        block.addStatement(
                new ExpressionStatement(
                        new ConstructorCallExpression(
                                ClassNode.SUPER,
                                conArgs)));
        return block;
    }

    private String genClosureClassName() {
        ClassNode classNode = controller.getClassNode();
        ClassNode outerClass = controller.getOutermostClass();
        MethodNode methodNode = controller.getMethodNode();

        return classNode.getName() + "$"
                + controller.getContext().getNextClosureInnerName(outerClass, classNode, methodNode);
    }

    protected static class CorrectAccessedVariableVisitor extends CodeVisitorSupport {
        private InnerClassNode icn;

        public CorrectAccessedVariableVisitor(final InnerClassNode icn) {
            this.icn = icn;
        }

        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            Variable v = expression.getAccessedVariable();
            if (v == null) return;
            if (!(v instanceof FieldNode)) return;
            String name = expression.getName();
            FieldNode fn = icn.getDeclaredField(name);
            if (fn != null) { // only overwrite if we find something more specific
                expression.setAccessedVariable(fn);
            }
        }
    }

    private static void correctAccessedVariable(final InnerClassNode closureClass, final ClosureExpression ce) {
        new CorrectAccessedVariableVisitor(closureClass).visitClosureExpression(ce);
    }

    /*
     * this method is called for local variables shared between scopes.
     * These variables must not have init values because these would
     * then in later steps be used to create multiple versions of the
     * same method, in this case the constructor. A closure should not
     * have more than one constructor!
     */
    protected static void removeInitialValues(final Parameter[] params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].hasInitialExpression()) {
                Parameter p = new Parameter(params[i].getType(), params[i].getName());
                p.setOriginType(p.getOriginType());
                params[i] = p;
            }
        }
    }

    public boolean addGeneratedClosureConstructorCall(final ConstructorCallExpression call) {
        ClassNode classNode = controller.getClassNode();
        if (!classNode.declaresInterface(ClassHelper.GENERATED_CLOSURE_Type)) return false;

        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();

        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitVarInsn(ALOAD, 0);
        ClassNode callNode = classNode.getSuperClass();
        TupleExpression arguments = (TupleExpression) call.getArguments();
        if (arguments.getExpressions().size()!=2) throw new GroovyBugError("expected 2 arguments for closure constructor super call, but got"+arguments.getExpressions().size());
        arguments.getExpression(0).visit(acg);
        operandStack.box();
        arguments.getExpression(1).visit(acg);
        operandStack.box();
        //TODO: replace with normal String, p not needed
        Parameter p = new Parameter(ClassHelper.OBJECT_TYPE,"_p");
        String descriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, new Parameter[]{p,p});
        mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor, false);
        operandStack.remove(2);
        return true;
    }

    protected Parameter[] getClosureSharedVariables(final ClosureExpression ce) {
        VariableScope scope = ce.getVariableScope();
        Parameter[] ret = new Parameter[scope.getReferencedLocalVariablesCount()];
        int index = 0;
        for (Iterator<Variable> iter = scope.getReferencedLocalVariablesIterator(); iter.hasNext();) {
            Variable element = iter.next();
            Parameter p = new Parameter(element.getType(), element.getName());
            p.setOriginType(element.getOriginType());
            p.setClosureSharedVariable(element.isClosureSharedVariable());
            ret[index] = p;
            index++;
        }
        return ret;
    }

    protected void loadThis() {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitVarInsn(ALOAD, 0);
        if (controller.isInClosure()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Closure", "getThisObject", "()Ljava/lang/Object;", false);
            controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
        } else {
            controller.getOperandStack().push(controller.getClassNode());
        }
    }
}
