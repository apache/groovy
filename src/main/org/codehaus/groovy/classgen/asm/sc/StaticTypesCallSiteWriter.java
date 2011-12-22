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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A call site writer which replaces call site caching with static calls. This means that the generated code
 * looks more like Java code than dynamic Groovy code. Best effort is made to use JVM instructions instead of
 * calls to helper methods.
 *
 * @author Cedric Champeau
 */
public class StaticTypesCallSiteWriter extends CallSiteWriter implements Opcodes {

    private static final MethodNode GROOVYOBJECT_GETPROPERTY_METHOD = ClassHelper.GROOVY_OBJECT_TYPE.getMethod("getProperty", new Parameter[]{new Parameter(ClassHelper.STRING_TYPE, "propertyName")});
    private WriterController controller;

    public StaticTypesCallSiteWriter(final StaticTypesWriterController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public void generateCallSiteArray() {
    }

    @Override
    public void makeCallSite(final Expression receiver, final String message, final Expression arguments, final boolean safe, final boolean implicitThis, final boolean callCurrent, final boolean callStatic) {
    }

    @Override
    public void makeGetPropertySite(final Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode receiverType = typeChooser.resolveType(receiver, classNode);
        MethodVisitor mv = controller.getMethodVisitor();
        if (receiverType.isArray() && methodName.equals("length")) {
            receiver.visit(controller.getAcg());
            mv.visitInsn(ARRAYLENGTH);
            controller.getOperandStack().replace(ClassHelper.int_TYPE);
            return;
        }
        if (makeGetPublicField(receiver, receiverType, methodName, implicitThis, samePackages(receiverType.getPackageName(), classNode.getPackageName()))) return;
        if (makeGetPropertyWithGetter(receiver, receiverType, methodName)) return;

        throw new UnsupportedOperationException("Operation not yet implemented: "+receiver.getText()+"."+methodName);
    }

    @Override
    public void makeGroovyObjectGetPropertySite(final Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode receiverType = typeChooser.resolveType(receiver, classNode);
        if (makeGetPublicField(receiver, receiverType, methodName, implicitThis, samePackages(receiverType.getPackageName(), classNode.getPackageName()))) return;
        if (makeGetPropertyWithGetter(receiver, receiverType, methodName)) return;

        MethodCallExpression call = new MethodCallExpression(
                receiver,
                "getProperty",
                new ArgumentListExpression(new ConstantExpression(methodName))
        );
        call.setMethodTarget(GROOVYOBJECT_GETPROPERTY_METHOD);
        call.visit(controller.getAcg());
        return;
    }

    private boolean makeGetPropertyWithGetter(final Expression receiver, final ClassNode receiverType, final String methodName) {
        // does a getter exists ?
        String getterName = "get" + MetaClassHelper.capitalize(methodName);
        MethodNode getterNode = receiverType.getGetterMethod(getterName);
        if (getterNode==null) {
            getterName = "is" + MetaClassHelper.capitalize(methodName);
            getterNode = receiverType.getGetterMethod(getterName);
        }
        if (getterNode!=null) {
            MethodCallExpression call = new MethodCallExpression(
                    receiver,
                    getterName,
                    new ArgumentListExpression()
            );
            call.setMethodTarget(getterNode);
            call.visit(controller.getAcg());
            return true;
        }
        return false;
    }

    private boolean makeGetPublicField(final Expression receiver, final ClassNode receiverType, final String fieldName, final boolean implicitThis, final boolean samePackage) {
        FieldNode field = receiverType.getField(fieldName);
        // is direct access possible ?
        if (field !=null && (field.isPublic() || (samePackage && field.isProtected()))) {
            CompileStack compileStack = controller.getCompileStack();
            if (implicitThis) {
                compileStack.pushImplicitThis(implicitThis);
            }
            receiver.visit(controller.getAcg());
            if (implicitThis) compileStack.popImplicitThis();
            MethodVisitor mv = controller.getMethodVisitor();
            mv.visitFieldInsn(GETFIELD, BytecodeHelper.getClassInternalName(receiverType), fieldName, BytecodeHelper.getTypeDescription(field.getOriginType()));
            controller.getOperandStack().replace(field.getOriginType());
            return true;
        }
        ClassNode superClass = receiverType.getSuperClass();
        if (superClass !=null) {
            String receiverTypePackageName = receiverType.getPackageName();
            String superClassPackageName = superClass.getPackageName();
            boolean same = samePackage && samePackages(receiverTypePackageName, superClassPackageName);
            return makeGetPublicField(receiver, superClass, fieldName, implicitThis, same);
        }
        return false;
    }

    private static boolean samePackages(final String pkg1, final String pkg2) {
        return (
                (pkg1 ==null && pkg2 ==null)
                || pkg1 !=null && pkg1.equals(pkg2)
                );
    }

    @Override
    public void makeSiteEntry() {
    }

    @Override
    public void prepareCallSite(final String message) {
    }

    @Override
    public void makeSingleArgumentCall(final Expression receiver, final String message, final Expression arguments) {
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode rType = typeChooser.resolveType(receiver, classNode);
        ClassNode aType = typeChooser.resolveType(arguments, classNode);
        if (ClassHelper.getWrapper(rType).isDerivedFrom(ClassHelper.Number_TYPE)
                && ClassHelper.getWrapper(aType).isDerivedFrom(ClassHelper.Number_TYPE)) {
            if ("plus".equals(message) || "minus".equals(message) || "multiply".equals(message) || "div".equals(message)) {
                writeNumberNumberCall(receiver, message, arguments);
                return;
            } else if ("power".equals(message)) {
                writePowerCall(receiver, arguments, rType, aType);
                return;
            }
        } else if (ClassHelper.STRING_TYPE.equals(rType) && "plus".equals(message)) {
            writeStringPlusCall(receiver, message, arguments);
            return;
        } else if (rType.isArray() && "getAt".equals(message)) {
            writeArrayGet(receiver, arguments, rType, aType);
            return;
        }

        // todo: more cases
        throw new GroovyBugError("This method should not have been called. Please try to create a simple example reproducing this error and file" +
                "a bug report at http://jira.codehaus.org/browse/GROOVY");
    }

    private void writeArrayGet(final Expression receiver, final Expression arguments, final ClassNode rType, final ClassNode aType) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        // visit receiver
        receiver.visit(controller.getAcg());
        // visit arguments as array index
        arguments.visit(controller.getAcg());
        operandStack.doGroovyCast(ClassHelper.int_TYPE);
        int m2 = operandStack.getStackLength();
        // array access
        controller.getMethodVisitor().visitInsn(AALOAD);
        operandStack.replace(rType.getComponentType(), m2-m1);
    }

    private void writePowerCall(Expression receiver, Expression arguments, final ClassNode rType, ClassNode aType) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
        prepareSiteAndReceiver(receiver, "power", false, controller.getCompileStack().isLHS());
        visitBoxedArgument(arguments);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        if (ClassHelper.BigDecimal_TYPE.equals(rType) && ClassHelper.Integer_TYPE.equals(ClassHelper.getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/math/BigDecimal;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else if (ClassHelper.BigInteger_TYPE.equals(rType) && ClassHelper.Integer_TYPE.equals(ClassHelper.getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/math/BigInteger;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else if (ClassHelper.Long_TYPE.equals(ClassHelper.getWrapper(rType)) && ClassHelper.Integer_TYPE.equals(ClassHelper.getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else if (ClassHelper.Integer_TYPE.equals(ClassHelper.getWrapper(rType)) && ClassHelper.Integer_TYPE.equals(ClassHelper.getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/lang/Long;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;");
        }
        controller.getOperandStack().replace(ClassHelper.Number_TYPE, m2 - m1);
    }

    private void writeStringPlusCall(final Expression receiver, final String message, final Expression arguments) {
        // todo: performance would be better if we created a StringBuilder
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
        prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
        visitBoxedArgument(arguments);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC,
                "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                "plus",
                "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;");
        controller.getOperandStack().replace(ClassHelper.STRING_TYPE, m2-m1);
    }

    private void writeNumberNumberCall(final Expression receiver, final String message, final Expression arguments) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
        prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
        visitBoxedArgument(arguments);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC,
                "org/codehaus/groovy/runtime/dgmimpl/NumberNumber" + MetaClassHelper.capitalize(message),
                message,
                "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;");
        controller.getOperandStack().replace(ClassHelper.Number_TYPE, m2 - m1);
    }
}