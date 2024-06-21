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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CallSiteWriter;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.VariableSlotLoader;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static org.apache.groovy.ast.tools.ClassNodeUtils.getField;
import static org.apache.groovy.ast.tools.ClassNodeUtils.getMethod;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.COLLECTION_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GROOVY_OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Iterator_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isBigDecimalType;
import static org.codehaus.groovy.ast.ClassHelper.isBigIntegerType;
import static org.codehaus.groovy.ast.ClassHelper.isClassType;
import static org.codehaus.groovy.ast.ClassHelper.isGeneratedFunction;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isStringType;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperInteger;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperLong;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isClassClassNodeWrappingConcreteType;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;

/**
 * A call site writer which replaces call site caching with static calls. This means that the generated code
 * looks more like Java code than dynamic Groovy code. Best effort is made to use JVM instructions instead of
 * calls to helper methods.
 */
public class StaticTypesCallSiteWriter extends CallSiteWriter {

    private static final ClassNode  INVOKERHELPER_TYPE = ClassHelper.make(InvokerHelper.class);
    private static final MethodNode COLLECTION_SIZE_METHOD = COLLECTION_TYPE.getMethod("size", Parameter.EMPTY_ARRAY);
    private static final MethodNode CLOSURE_GETTHISOBJECT_METHOD = CLOSURE_TYPE.getMethod("getThisObject", Parameter.EMPTY_ARRAY);
    private static final MethodNode MAP_GET_METHOD = MAP_TYPE.getMethod("get", new Parameter[]{new Parameter(OBJECT_TYPE, "key")});
    private static final MethodNode GROOVYOBJECT_GETPROPERTY_METHOD = GROOVY_OBJECT_TYPE.getMethod("getProperty", new Parameter[]{new Parameter(STRING_TYPE, "propertyName")});
    private static final MethodNode INVOKERHELPER_GETPROPERTY_METHOD = INVOKERHELPER_TYPE.getMethod("getProperty", new Parameter[]{new Parameter(OBJECT_TYPE, "object"), new Parameter(STRING_TYPE, "propertyName")});
    private static final MethodNode INVOKERHELPER_GETPROPERTYSAFE_METHOD = INVOKERHELPER_TYPE.getMethod("getPropertySafe", new Parameter[]{new Parameter(OBJECT_TYPE, "object"), new Parameter(STRING_TYPE, "propertyName")});

    private final StaticTypesWriterController controller;

    public StaticTypesCallSiteWriter(final StaticTypesWriterController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public void generateCallSiteArray() {
        CallSiteWriter regularCallSiteWriter = controller.getRegularCallSiteWriter();
        if (regularCallSiteWriter.hasCallSiteUse()) {
            regularCallSiteWriter.generateCallSiteArray();
        }
    }

    @Override
    public void makeCallSite(final Expression receiver, final String message, final Expression arguments, final boolean safe, final boolean implicitThis, final boolean callCurrent, final boolean callStatic) {
        throw new GroovyBugError(
                "at line " + receiver.getLineNumber() + " column " + receiver.getColumnNumber() + "\n" +
                "On receiver: " + receiver.getText() + " with message: " + message + " and arguments: " + arguments.getText() + "\n" +
                "StaticTypesCallSiteWriter#makeCallSite should not have been called. Call site lacked method target for static compilation.\n" +
                "Please try to create a simple example reproducing this error and file a bug report at https://issues.apache.org/jira/browse/GROOVY");
    }

    @Override
    public void makeGetPropertySite(final Expression receiver, final String propertyName, final boolean safe, final boolean implicitThis) {
        Object dynamic = receiver.getNodeMetaData(StaticCompilationMetadataKeys.RECEIVER_OF_DYNAMIC_PROPERTY);
        if (dynamic != null) {
            makeDynamicGetProperty(receiver, propertyName, safe);
            return;
        }

        boolean[] isClassReceiver = new boolean[1];
        ClassNode receiverType = getPropertyOwnerType(receiver, isClassReceiver);
        if (receiverType.isArray() && "length".equals(propertyName)) {
            receiver.visit(controller.getAcg());
            controller.getOperandStack().doGroovyCast(receiverType); // GROOVY-5683, GROOVY-11011
            controller.getMethodVisitor().visitInsn(ARRAYLENGTH);
            controller.getOperandStack().replace(int_TYPE);
            return;
        } else if (isOrImplements(receiverType, COLLECTION_TYPE) && ("size".equals(propertyName) || "length".equals(propertyName))) {
            MethodCallExpression expr = callX(receiver, "size");
            expr.setMethodTarget(COLLECTION_SIZE_METHOD);
            expr.setImplicitThis(implicitThis);
            expr.setSafe(safe);
            expr.visit(controller.getAcg());
            return;
        }

        if (makeGetPropertyWithGetter(receiver, receiverType, propertyName, safe, implicitThis)) return;

        boolean isStaticProperty = (receiver instanceof ClassExpression
                && (receiverType.isDerivedFrom(receiver.getType()) || receiverType.implementsInterface(receiver.getType())));

        // GROOVY-5001, GROOVY-5491, GROOVY-5517, GROOVY-6144, GROOVY-8788: for map types,
        // replace "map.foo" with "map.get('foo')" -- if no public field "foo" is declared
        if (!isStaticProperty && isOrImplements(receiverType, MAP_TYPE)
                && getField(receiverType, propertyName, FieldNode::isPublic) == null) {
            writeMapDotProperty(receiver, propertyName, safe);
            return;
        }

        if (makeGetField(receiver, receiverType, propertyName, safe, implicitThis)) return;
        if (isThisExpression(receiver) && receiverType.getOuterClass() != null) { // GROOVY-11198: outer field
            if (makeGetField(receiver, receiverType.getOuterClass(), propertyName, safe, implicitThis)) return;
        }
        if (isClassReceiver[0]) {
            if (makeGetPropertyWithGetter(receiver, CLASS_Type, propertyName, safe, implicitThis)) return;
        }
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, propertyName, safe, implicitThis)) return;

        if (!isStaticProperty && isOrImplements(receiverType, LIST_TYPE)) {
            writeListDotProperty(receiver, propertyName, safe);
            return;
        }

        addPropertyAccessError(receiver, propertyName, receiverType);
        controller.getMethodVisitor().visitInsn(ACONST_NULL);
        controller.getOperandStack().push(OBJECT_TYPE);
    }

    private void makeDynamicGetProperty(final Expression receiver, final String propertyName, final boolean safe) {
        MethodNode target = safe ? INVOKERHELPER_GETPROPERTYSAFE_METHOD : INVOKERHELPER_GETPROPERTY_METHOD;
        MethodCallExpression call = callX(
                classX(INVOKERHELPER_TYPE),
                target.getName(),
                args(receiver, constX(propertyName))
        );
        call.setImplicitThis(false);
        call.setMethodTarget(target);
        call.visit(controller.getAcg());
    }

    private void writeMapDotProperty(final Expression receiver, final String propertyName, final boolean safe) {
        MethodVisitor mv = controller.getMethodVisitor();

        // push receiver on stack
        receiver.visit(controller.getAcg());

        // check if receiver null
        Label skip = new Label();
        if (safe) {
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNULL, skip);
        }

        mv.visitLdcInsn(propertyName);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);

        if (safe) {
            mv.visitLabel(skip);
        }
        controller.getOperandStack().replace(OBJECT_TYPE);
    }

    private void writeListDotProperty(final Expression receiver, final String propertyName, final boolean safe) {
        // for lists, replace list.foo with:
        //   def result = new ArrayList(list.size())
        //   for (item in list) result.add(item.foo)
        //   result
        ClassNode componentType = receiver.getNodeMetaData(StaticCompilationMetadataKeys.COMPONENT_TYPE);
        if (componentType == null) {
            componentType = OBJECT_TYPE;
        }
        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();

        Label exit = new Label();
        if (safe) {
            receiver.visit(controller.getAcg());
            Label doGet = new Label();
            mv.visitJumpInsn(IFNONNULL, doGet);
            controller.getOperandStack().remove(1);
            mv.visitInsn(ACONST_NULL);
            mv.visitJumpInsn(GOTO, exit);
            mv.visitLabel(doGet);
        }

        Variable tmpList = varX("tmpList", ClassHelper.make(ArrayList.class));
        int var = compileStack.defineTemporaryVariable(tmpList, false);
        Variable iterator = varX("iterator", Iterator_TYPE);
        int it = compileStack.defineTemporaryVariable(iterator, false);
        Variable nextVar = varX("next", componentType);
        final int next = compileStack.defineTemporaryVariable(nextVar, false);

        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        receiver.visit(controller.getAcg());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
        controller.getOperandStack().remove(1);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V", false);
        mv.visitVarInsn(ASTORE, var);
        Label l1 = new Label();
        mv.visitLabel(l1);
        receiver.visit(controller.getAcg());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
        controller.getOperandStack().remove(1);
        mv.visitVarInsn(ASTORE, it);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, it);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        Label l3 = new Label();
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitVarInsn(ALOAD, it);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
        mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(componentType));
        mv.visitVarInsn(ASTORE, next);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, var);
        PropertyExpression pexp = propX(
                bytecodeX(componentType, v -> v.visitVarInsn(ALOAD, next)),
                propertyName
        );
        pexp.visit(controller.getAcg());
        controller.getOperandStack().box();
        controller.getOperandStack().remove(1);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(POP);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, var);
        if (safe) {
            mv.visitLabel(exit);
        }
        controller.getOperandStack().push(ClassHelper.make(ArrayList.class));
        controller.getCompileStack().removeVar(next);
        controller.getCompileStack().removeVar(it);
        controller.getCompileStack().removeVar(var);
    }

    private boolean makeGetPrivateFieldWithBridgeMethod(final Expression receiver, final ClassNode receiverType, final String fieldName, final boolean safe, final boolean implicitThis) {
        FieldNode field = receiverType.getField(fieldName);
        if (field != null) {
            ClassNode classNode = controller.getClassNode();
            if (field.isPrivate() && !receiverType.equals(classNode)
                    && StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(receiverType, classNode)) {
                Map<String, MethodNode> accessors = receiverType.redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_FIELDS_ACCESSORS);
                if (accessors != null) {
                    MethodNode methodNode = accessors.get(fieldName);
                    if (methodNode != null) {
                        Expression thisObject;
                        if (field.isStatic()) {
                            thisObject = nullX();
                        } else if (!isThisExpression(receiver)) {
                            thisObject = receiver;
                        } else { // GROOVY-7304, GROOVY-9771, GROOVY-9872
                            thisObject = propX(classX(receiverType), "this");
                        }

                        MethodCallExpression call = callX(classX(receiverType), methodNode.getName(), thisObject);
                        call.setMethodTarget(methodNode);
                        call.visit(controller.getAcg());
                        return true;
                    }
                }
            }
        } else if (implicitThis) {
            ClassNode outerClass = receiverType.getOuterClass();
            if (outerClass != null && (receiverType.getModifiers() & ACC_STATIC) == 0) {
                Expression expr;
                ClassNode thisType = outerClass;
                if (controller.isInGeneratedFunction()) {
                    while (isGeneratedFunction(thisType)) {
                        thisType = thisType.getOuterClass();
                        // TODO: stop if thisType is static?
                    }

                    MethodCallExpression call = callThisX("getThisObject");
                    call.setImplicitThis(true);
                    call.setMethodTarget(CLOSURE_GETTHISOBJECT_METHOD);
                    call.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, thisType);

                    expr = castX(thisType, call);
                } else {
                    expr = propX(classX(outerClass), "this");
                }
                expr.setSourcePosition(receiver);
                expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, thisType);
                // try again with "(Outer) getThisObject()" or "Outer.this" as receiver
                return makeGetPrivateFieldWithBridgeMethod(expr, outerClass, fieldName, safe, true);
            }
        }
        return false;
    }

    @Override
    public void makeGroovyObjectGetPropertySite(final Expression receiver, final String propertyName, final boolean safe, final boolean implicitThis) {
        ClassNode receiverType;
        if (!isThisExpression(receiver) || controller.isInGeneratedFunction()) {
            receiverType = getPropertyOwnerType(receiver); // GROOVY-9967, et al.
        } else {
            receiverType = controller.getClassNode();
        }

        if (implicitThis && controller.getInvocationWriter() instanceof StaticInvocationWriter) {
            Expression currentCall = ((StaticInvocationWriter) controller.getInvocationWriter()).getCurrentCall();
            if (currentCall != null) {
                String implicitReceiver = currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
                if (implicitReceiver != null) {
                    String[] pathElements = implicitReceiver.split("\\.");
                    BytecodeExpression thisLoader = bytecodeX(CLOSURE_TYPE, mv -> mv.visitVarInsn(ALOAD, 0));
                    PropertyExpression pexp = propX(thisLoader, constX(pathElements[0]), safe);
                    for (int i = 1, n = pathElements.length; i < n; i += 1) {
                        pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, CLOSURE_TYPE);
                        pexp = propX(pexp, pathElements[i]);
                    }
                    pexp.visit(controller.getAcg());
                    return;
                }
            }
        }

        if (makeGetPropertyWithGetter(receiver, receiverType, propertyName, safe, implicitThis)) return;

        boolean isStaticProperty = (receiver instanceof ClassExpression
                && (receiverType.isDerivedFrom(receiver.getType()) || receiverType.implementsInterface(receiver.getType())));
        boolean isMapDotProperty = !isStaticProperty && isOrImplements(receiverType, MAP_TYPE);

        // GROOVY-5001, GROOVY-5491, GROOVY-5517, GROOVY-6144, GROOVY-8788: for map types,
        // replace "map.foo" with "map.get('foo')" -- if no public field "foo" is declared
        if (isMapDotProperty
                && getField(receiverType, propertyName, FieldNode::isPublic) == null
                // GROOVY-11367, GROOVY-11402, GROOVY-11403: "this.name" outside closure includes non-public fields of lexical scope
                && (!isThisExpression(receiver) || controller.isInGeneratedFunction() || receiverType.getDeclaredField(propertyName) == null)) {
            writeMapDotProperty(receiver, propertyName, safe);
            return;
        }

        if (makeGetField(receiver, receiverType, propertyName, safe, implicitThis)) return;
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, propertyName, safe, implicitThis)) return;

        if (isMapDotProperty) {
            writeMapDotProperty(receiver, propertyName, safe);
            return;
        }

        boolean isScriptVariable = (receiverType.isScript() && receiver instanceof VariableExpression && ((VariableExpression) receiver).getAccessedVariable() == null);
        if (!isScriptVariable && controller.getClassNode().getOuterClass() == null) { // inner class still needs dynamic property sequence
            addPropertyAccessError(receiver, propertyName, receiverType);
        }

        MethodCallExpression call = callX(receiver, "getProperty", args(constX(propertyName)));
        call.setImplicitThis(implicitThis);
        call.setMethodTarget(GROOVYOBJECT_GETPROPERTY_METHOD);
        call.setSafe(safe);
        call.visit(controller.getAcg());
    }

    @Override
    public void makeCallSiteArrayInitializer() {
    }

    private boolean makeGetPropertyWithGetter(final Expression receiver, final ClassNode receiverType, final String propertyName, final boolean safe, final boolean implicitThis) {
        // check for an accessor method
        String getterName = "is" + capitalize(propertyName);
        MethodNode getterNode = receiverType.getGetterMethod(getterName);
        if (getterNode == null) {
            getterName = "get" + capitalize(propertyName);
            getterNode = receiverType.getGetterMethod(getterName);
        }
        // GROOVY-5561: if two files are compiled in the same source unit and
        // one references the other, the getters for properties have not been
        // generated by the compiler yet by Verifier
        PropertyNode propertyNode = receiverType.getProperty(propertyName);
        if (getterNode == null && propertyNode != null) {
            getterNode = new MethodNode(
                    propertyNode.getGetterNameOrDefault(), // GROOVY-10548
                    ACC_PUBLIC | (propertyNode.isStatic() ? ACC_STATIC : 0),
                    propertyNode.getOriginType(),
                    Parameter.EMPTY_ARRAY,
                    ClassNode.EMPTY_ARRAY,
                    EmptyStatement.INSTANCE);
            getterNode.setDeclaringClass(receiverType);
        }
        if (getterNode != null) {
            if (!getterNode.isStatic() && receiver instanceof ClassExpression && !isClassType(receiverType)) {
                return false;
            }
            if ((!getterNode.isPublic() || "class".equals(propertyName) || "empty".equals(propertyName)) && isOrImplements(receiverType, MAP_TYPE)) {
                return false; // GROOVY-11367
            }
            if (!AsmClassGenerator.isMemberDirectlyAccessible(getterNode.getModifiers(), getterNode.getDeclaringClass(), controller.getClassNode())) {
                return false; // GROOVY-6277
            }
            MethodCallExpression call = callX(receiver, getterName);
            call.setImplicitThis(implicitThis);
            call.setMethodTarget(getterNode);
            call.setSafe(safe);
            call.setSourcePosition(receiver);
            call.visit(controller.getAcg());
            return true;
        }

        // GROOVY-7149: check direct interfaces
        for (ClassNode node : receiverType.getInterfaces()) {
            if (makeGetPropertyWithGetter(receiver, node, propertyName, safe, implicitThis)) {
                return true;
            }
        }
        // check super class
        ClassNode superClass = receiverType.getSuperClass();
        if (superClass != null) {
            if (makeGetPropertyWithGetter(receiver, superClass, propertyName, safe, implicitThis)) {
                return true;
            }
        }
        // check outer class
        if (implicitThis && receiverType instanceof InnerClassNode && !receiverType.isStaticClass()) {
            if (makeGetPropertyWithGetter(receiver,  receiverType.getOuterClass(), propertyName,  safe, implicitThis)) {
                return true;
            }
        }

        return false;
    }

    boolean makeGetField(final Expression receiver, final ClassNode receiverType, final String fieldName, final boolean safe, final boolean implicitThis) {
        FieldNode field = getField(receiverType, fieldName); // GROOVY-7039: include interface constants
        if (field != null && AsmClassGenerator.isFieldDirectlyAccessible(field, controller.getClassNode())) {
            CompileStack compileStack = controller.getCompileStack();
            OperandStack operandStack = controller.getOperandStack();
            MethodVisitor mv = controller.getMethodVisitor();
            ClassNode resultType = field.getOriginType();
            if (field.isStatic()) {
                mv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(receiverType), fieldName, BytecodeHelper.getTypeDescription(resultType));
                operandStack.push(resultType);
            } else {
                if (implicitThis) {
                    compileStack.pushImplicitThis(true);
                    receiver.visit(controller.getAcg());
                    compileStack.popImplicitThis();
                } else {
                    receiver.visit(controller.getAcg());
                }
                Label skip = new Label();
                if (safe) {
                    mv.visitInsn(DUP);
                    Label doGet = new Label();
                    mv.visitJumpInsn(IFNONNULL, doGet);
                    mv.visitInsn(POP);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitJumpInsn(GOTO, skip);
                    mv.visitLabel(doGet);
                }
                if (!operandStack.getTopOperand().isDerivedFrom(field.getOwner())) {
                    mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(field.getOwner()));
                }
                mv.visitFieldInsn(GETFIELD, BytecodeHelper.getClassInternalName(field.getOwner()), fieldName, BytecodeHelper.getTypeDescription(resultType));
                if (safe) {
                    if (isPrimitiveType(resultType)) {
                        operandStack.replace(resultType);
                        operandStack.box();
                        resultType = operandStack.getTopOperand();
                    }
                    mv.visitLabel(skip);
                }
            }
            operandStack.replace(resultType);
            return true;
        }
        return false;
    }

    @Override
    public void makeSiteEntry() {
    }

    @Override
    public void prepareCallSite(final String message) {
    }

    @Override
    public void makeSingleArgumentCall(final Expression receiver, final String message, final Expression arguments, final boolean safe) {
        ClassNode classNode = controller.getClassNode();
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode rType = typeChooser.resolveType(receiver, classNode);
        ClassNode aType = typeChooser.resolveType(arguments, classNode);
        if (trySubscript(receiver, message, arguments, rType, aType, safe)) {
            return;
        }
        // now try with flow type instead of declaration type
        rType = receiver.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (receiver instanceof VariableExpression && rType == null) {
            // TODO: can STCV be made smarter to avoid this check?
            ASTNode node = (ASTNode) ((VariableExpression) receiver).getAccessedVariable();
            rType = node.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        }
        if (rType!=null && trySubscript(receiver, message, arguments, rType, aType, safe)) {
            return;
        }
        // TODO: more cases
        throw new GroovyBugError(
                "at line " + receiver.getLineNumber() + " column " + receiver.getColumnNumber() + "\n" +
                "On receiver: " + receiver.getText() + " with message: " + message + " and arguments: " + arguments.getText() + "\n" +
                "This method should not have been called. Please try to create a simple example reproducing " +
                "this error and file a bug report at https://issues.apache.org/jira/browse/GROOVY");
    }

    private boolean trySubscript(final Expression receiver, final String message, final Expression arguments, final ClassNode rType, final ClassNode aType, final boolean safe) {
        if (getWrapper(rType).isDerivedFrom(Number_TYPE)
                && getWrapper(aType).isDerivedFrom(Number_TYPE)) {
            if ("plus".equals(message) || "minus".equals(message) || "multiply".equals(message) || "div".equals(message)) {
                writeNumberNumberCall(receiver, message, arguments);
                return true;
            } else if ("power".equals(message)) {
                writePowerCall(receiver, arguments, rType, aType);
                return true;
            } else if ("remainder".equals(message) || "leftShift".equals(message)
                    || "rightShift".equals(message) || "rightShiftUnsigned".equals(message)
                    || "and".equals(message) || "or".equals(message) || "xor".equals(message) || "implies".equals(message)) {
                writeOperatorCall(receiver, arguments, message);
                return true;
            }
        } else if (isStringType(rType) && "plus".equals(message)) {
            writeStringPlusCall(receiver, message, arguments);
            return true;
        } else if ("getAt".equals(message)) {
            if (rType.isArray() && getWrapper(aType).isDerivedFrom(Number_TYPE) && !safe) {
                writeArrayGet(receiver, arguments, rType, aType);
                return true;
            } else { // check the receiver for a getAt method
                MethodNode getAtNode = findGetAt(rType, aType);
                if (getAtNode != null) {
                    MethodCallExpression call = callX(receiver, "getAt", arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(getAtNode);
                    call.setSafe(safe);
                    call.setSourcePosition(arguments);
                    call.visit(controller.getAcg());
                    return true;
                }
                if (isOrImplements(rType, MAP_TYPE)) { // fallback to Map#get
                    MethodCallExpression call = callX(receiver, "get", arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(MAP_GET_METHOD);
                    call.setSafe(safe);
                    call.setSourcePosition(arguments);
                    call.visit(controller.getAcg());
                    return true;
                }
            }
        }
        return false;
    }

    private MethodNode findGetAt(final ClassNode rType, final ClassNode aType) {
        // TODO: find "best" match or find all matches and deal with ambiguity
        // TODO: handle getAt with more than one parameter

        ClassNode classNode = rType;
        Predicate<MethodNode> compatible = methodNode -> (methodNode.getParameters().length == 1);
        if (isClassClassNodeWrappingConcreteType(rType)) { // GROOVY-9415
            classNode = rType.getGenericsTypes()[0].getType();
            compatible = compatible.and(MethodNode::isStatic);
        }

        MethodNode getAt = findGetAt(classNode, compatible, aType, true);
        if (getAt == null) getAt = findGetAt(classNode, compatible, aType, false);
        if (getAt == null) { // make sure Map#getAt and List#getAt handled with the bracket syntax are properly compiled
            List<MethodNode> nodes = findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), rType, "getAt", new ClassNode[]{aType});
            if (nodes.size() == 1 || (nodes.size() > 1 && (isOrImplements(rType, MAP_TYPE) || isOrImplements(rType, LIST_TYPE)))) {
                getAt = nodes.get(0);
            }
        }
        return getAt;
    }

    private MethodNode findGetAt(final ClassNode rType, final Predicate<MethodNode> mTest, final ClassNode aType, final boolean exact) {
        BiPredicate<MethodNode, ClassNode> pType = (methodNode, argumentType) -> {
            ClassNode parameterType = methodNode.getParameters()[0].getType();
            if (exact) {
                return argumentType.equals(parameterType);
            } else {
                return parameterType.isInterface() ? argumentType.implementsInterface(parameterType) : argumentType.isDerivedFrom(parameterType);
            }
        };

        MethodNode getAt = getMethod(rType, "getAt", mTest.and(mNode -> pType.test(mNode, aType)));
        if (getAt == null && isPrimitiveType(aType)) {
            getAt = getMethod(rType, "getAt", mTest.and(mNode -> pType.test(mNode, getWrapper(aType))));
        } else if (getAt == null && aType.isDerivedFrom(Number_TYPE)) {
            getAt = getMethod(rType, "getAt", mTest.and(mNode -> pType.test(mNode, getUnwrapper(aType))));
        }
        return getAt;
    }

    private void writeArrayGet(final Expression receiver, final Expression arguments, final ClassNode rType, final ClassNode aType) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        // visit receiver
        receiver.visit(controller.getAcg());
        operandStack.doGroovyCast(rType); // GROOVY-11011
        // visit arguments as array index
        arguments.visit(controller.getAcg());
        operandStack.doGroovyCast(int_TYPE);
        int m2 = operandStack.getStackLength();
        // array access
        controller.getMethodVisitor().visitInsn(AALOAD);
        operandStack.replace(rType.getComponentType(), m2 - m1);
    }

    private void writeOperatorCall(final Expression receiver, final Expression arguments, final String operator) {
        prepareSiteAndReceiver(receiver, operator, false, controller.getCompileStack().isLHS());
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        visitBoxedArgument(arguments);
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/typehandling/NumberMath", operator, "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;", false);
        controller.getOperandStack().replace(Number_TYPE, 2);
    }

    private void writePowerCall(final Expression receiver, final Expression arguments, final ClassNode rType, final ClassNode aType) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        // slow path
        prepareSiteAndReceiver(receiver, "power", false, controller.getCompileStack().isLHS());
        operandStack.doGroovyCast(getWrapper(rType));
        visitBoxedArgument(arguments);
        operandStack.doGroovyCast(getWrapper(aType));
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        if (isBigDecimalType(rType) && isWrapperInteger(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/math/BigDecimal;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else if (isBigIntegerType(rType) && isWrapperInteger(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/math/BigInteger;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else if (isWrapperLong(getWrapper(rType)) && isWrapperInteger(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/lang/Long;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else if (isWrapperInteger(getWrapper(rType)) && isWrapperInteger(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;", false);
        }
        operandStack.replace(Number_TYPE, m2 - m1);
    }

    private void writeStringPlusCall(final Expression receiver, final String message, final Expression arguments) {
        // TODO: performance would be better if we created a StringBuilder
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        // slow path
        prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
        visitBoxedArgument(arguments);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "plus", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;", false);
        operandStack.replace(STRING_TYPE, m2 - m1);
    }

    private void writeNumberNumberCall(final Expression receiver, final String message, final Expression arguments) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        // slow path
        prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
        operandStack.doGroovyCast(Number_TYPE);
        visitBoxedArgument(arguments);
        operandStack.doGroovyCast(Number_TYPE);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/dgmimpl/NumberNumber" + capitalize(message), message, "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;", false);
        operandStack.replace(Number_TYPE, m2 - m1);
    }

    @Override
    public void fallbackAttributeOrPropertySite(final PropertyExpression expression, final Expression objectExpression, final String name, final MethodCallerMultiAdapter adapter) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        if (name != null && compileStack.isLHS()) {
            boolean[] isClassReceiver = new boolean[1];
            ClassNode receiverType = getPropertyOwnerType(objectExpression, isClassReceiver);
            if (adapter == AsmClassGenerator.setField || adapter == AsmClassGenerator.setGroovyObjectField) {
                if (setField(expression, objectExpression, receiverType, name)) return;
            }
            if (isThisExpression(objectExpression)) {
                ClassNode classNode = controller.getClassNode();
                FieldNode fieldNode = receiverType.getField(name);
                if (fieldNode != null && fieldNode.isPrivate() && !receiverType.equals(classNode)
                        && StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(receiverType, classNode)) {
                    Map<String, MethodNode> mutators = receiverType.redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_FIELDS_MUTATORS);
                    if (mutators != null) {
                        MethodNode methodNode = mutators.get(name);
                        if (methodNode != null) {
                            ClassNode rhsType = operandStack.getTopOperand();
                            int i = compileStack.defineTemporaryVariable("$rhs", rhsType, true);
                            VariableSlotLoader rhsValue = new VariableSlotLoader(rhsType, i, operandStack);

                            MethodCallExpression call = callX(objectExpression, methodNode.getName(), args(fieldNode.isStatic() ? nullX() : objectExpression, rhsValue));
                            call.setImplicitThis(expression.isImplicitThis());
                            call.setSpreadSafe(expression.isSpreadSafe());
                            call.setSafe(expression.isSafe());
                            call.setMethodTarget(methodNode);
                            call.visit(controller.getAcg());

                            // GROOVY-9892: assuming that the mutator method has a return value, make sure the operand
                            // stack is not polluted with the result of the method call
                            operandStack.pop();

                            compileStack.removeVar(i);
                            return;
                        }
                    }
                }
            }
            // GROOVY-6954, GROOVY-11376: for map types, replace "map.foo = ..."
            // with "map.put('foo', ...)" if no public field exists
            if (!isClassReceiver[0] && isOrImplements(receiverType, MAP_TYPE)
                    && getField(receiverType, name, FieldNode::isPublic) == null) {
                MethodVisitor mv = controller.getMethodVisitor();

                // store value in temporary variable
                ClassNode rhsType = operandStack.getTopOperand();
                int rhs = compileStack.defineTemporaryVariable("$rhs", rhsType, true);

                // push receiver on stack
                compileStack.pushLHS(false);
                objectExpression.visit(controller.getAcg());
                compileStack.popLHS();

                // check if receiver null
                Label skip = new Label();
                if (expression.isSafe()) {
                    mv.visitInsn(DUP);
                    mv.visitJumpInsn(IFNULL, skip);
                }

                mv.visitLdcInsn(name);
                BytecodeHelper.load(mv, rhsType, rhs);
                if (isPrimitiveType(rhsType)) BytecodeHelper.doCastToWrappedType(mv, rhsType, getWrapper(rhsType));
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);

                if (expression.isSafe()) {
                    mv.visitLabel(skip);
                }
                // no return value
                operandStack.pop();
                compileStack.removeVar(rhs);
                return;
            }
        }

        super.fallbackAttributeOrPropertySite(expression, objectExpression, name, adapter);
    }

    private ClassNode getPropertyOwnerType(final Expression receiver, final boolean... isClassReceiver) {
        Object inferredType = receiver.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (inferredType == null && receiver instanceof VariableExpression) {
            Variable variable = ((VariableExpression) receiver).getAccessedVariable();
            if (variable instanceof Expression) {
                inferredType = ((Expression) variable).getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            }
        }
        ClassNode receiverType;
        if (inferredType instanceof ClassNode) {
            // in case a "flow type" is found, it is preferred to use it instead of the declaration type
            receiverType = (ClassNode) inferredType;
        } else {
            receiverType = receiver.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
            if (receiverType == null) {
                receiverType = controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
            }
        }
        if (isClassClassNodeWrappingConcreteType(receiverType)) {
            if (isClassReceiver.length > 0) isClassReceiver[0] = true;
            receiverType = receiverType.getGenericsTypes()[0].getType();
        }
        if (isPrimitiveType(receiverType)) {
            // GROOVY-6590: wrap primitive types
            receiverType = getWrapper(receiverType);
        }
        return receiverType;
    }

    // this is just a simple set field handling static and non-static, but not Closure and inner classes
    private boolean setField(final PropertyExpression expression, final Expression objectExpression, final ClassNode receiverType, final String name) {
        if (expression.isSafe()) return false;
        FieldNode fn = AsmClassGenerator.getDeclaredFieldOfCurrentClassOrAccessibleFieldOfSuper(controller.getClassNode(), receiverType, name, false);
        if (fn == null) return false;
        OperandStack stack = controller.getOperandStack();
        stack.doGroovyCast(fn.getType());

        MethodVisitor mv = controller.getMethodVisitor();
        if (!fn.isStatic()) {
            controller.getCompileStack().pushLHS(false);
            objectExpression.visit(controller.getAcg());
            controller.getCompileStack().popLHS();
            if (!receiverType.equals(stack.getTopOperand())) {
                BytecodeHelper.doCast(mv, receiverType);
                stack.replace(receiverType);
            }
            stack.swap();
            mv.visitFieldInsn(PUTFIELD, BytecodeHelper.getClassInternalName(fn.getOwner()), name, BytecodeHelper.getTypeDescription(fn.getType()));
            stack.remove(1);
        } else {
            mv.visitFieldInsn(PUTSTATIC, BytecodeHelper.getClassInternalName(receiverType), name, BytecodeHelper.getTypeDescription(fn.getType()));
        }

        return true;
    }

    /*private boolean getField(final PropertyExpression expression, final Expression receiver, ClassNode receiverType, final String name) {
        boolean safe = expression.isSafe();
        boolean implicitThis = expression.isImplicitThis();

        if (makeGetField(receiver, receiverType, name, safe, implicitThis)) return true;
        if (receiver instanceof ClassExpression) {
            if (makeGetField(receiver, receiver.getType(), name, safe, implicitThis)) return true;
            if (makeGetPrivateFieldWithBridgeMethod(receiver, receiver.getType(), name, safe, implicitThis)) return true;
        }
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, name, safe, implicitThis)) return true;

        boolean isClassReceiver = false;
        if (isClassClassNodeWrappingConcreteType(receiverType)) {
            isClassReceiver = true;
            receiverType = receiverType.getGenericsTypes()[0].getType();
        }
        if (isClassReceiver && makeGetField(receiver, CLASS_Type, name, safe, false)) return true;
        if (receiverType.isEnum()) {
            controller.getMethodVisitor().visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(receiverType), name, BytecodeHelper.getTypeDescription(receiverType));
            controller.getOperandStack().push(receiverType);
            return true;
        }
        return false;
    }*/

    private void addPropertyAccessError(final Expression receiver, final String propertyName, final ClassNode receiverType) {
        String receiverName = (receiver instanceof ClassExpression ? receiver.getType() : receiverType).toString(false);
        String message = "Access to " + receiverName + "#" + propertyName + " is forbidden";
        controller.getSourceUnit().addError(new SyntaxException(message, receiver));
    }
}
