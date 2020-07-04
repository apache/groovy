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
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.ClassHelper.BigDecimal_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigInteger_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GROOVY_OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Integer_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Iterator_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isGeneratedFunction;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
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
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.chooseBestMethod;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isClassClassNodeWrappingConcreteType;

/**
 * A call site writer which replaces call site caching with static calls. This means that the generated code
 * looks more like Java code than dynamic Groovy code. Best effort is made to use JVM instructions instead of
 * calls to helper methods.
 */
public class StaticTypesCallSiteWriter extends CallSiteWriter implements Opcodes {

    private static final ClassNode COLLECTION_TYPE = ClassHelper.make(Collection.class);
    private static final ClassNode INVOKERHELPER_TYPE = ClassHelper.make(InvokerHelper.class);
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
    }

    @Override
    public void makeGetPropertySite(final Expression receiver, final String propertyName, final boolean safe, final boolean implicitThis) {
        Object dynamic = receiver.getNodeMetaData(StaticCompilationMetadataKeys.RECEIVER_OF_DYNAMIC_PROPERTY);
        if (dynamic != null) {
            makeDynamicGetProperty(receiver, propertyName, safe);
            return;
        }
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode receiverType = receiver.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
        if (receiverType == null) {
            receiverType = typeChooser.resolveType(receiver, classNode);
        }
        Object type = receiver.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (type == null && receiver instanceof VariableExpression) {
            Variable variable = ((VariableExpression) receiver).getAccessedVariable();
            if (variable instanceof Expression) {
                type = ((Expression) variable).getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            }
        }
        if (type != null) {
            // in case a "flow type" is found, it is preferred to use it instead of
            // the declaration type
            receiverType = (ClassNode) type;
        }
        boolean isClassReceiver = false;
        if (isClassClassNodeWrappingConcreteType(receiverType)) {
            isClassReceiver = true;
            receiverType = receiverType.getGenericsTypes()[0].getType();
        }

        if (isPrimitiveType(receiverType)) {
            // GROOVY-6590: wrap primitive types
            receiverType = getWrapper(receiverType);
        }

        MethodVisitor mv = controller.getMethodVisitor();

        if (receiverType.isArray() && "length".equals(propertyName)) {
            receiver.visit(controller.getAcg());
            ClassNode arrayGetReturnType = typeChooser.resolveType(receiver, classNode);
            controller.getOperandStack().doGroovyCast(arrayGetReturnType);
            mv.visitInsn(ARRAYLENGTH);
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

        boolean isStaticProperty = receiver instanceof ClassExpression
                && (receiverType.isDerivedFrom(receiver.getType()) || receiverType.implementsInterface(receiver.getType()));

        if (!isStaticProperty && isOrImplements(receiverType, MAP_TYPE)) {
            // for maps, replace map.foo with map.get('foo')
            writeMapDotProperty(receiver, propertyName, mv, safe);
            return;
        }
        if (makeGetPropertyWithGetter(receiver, receiverType, propertyName, safe, implicitThis)) return;
        if (makeGetField(receiver, receiverType, propertyName, safe, implicitThis)) return;
        if (receiver instanceof ClassExpression) {
            if (makeGetField(receiver, receiver.getType(), propertyName, safe, implicitThis)) return;
            if (makeGetPropertyWithGetter(receiver, receiver.getType(), propertyName, safe, implicitThis)) return;
            if (makeGetPrivateFieldWithBridgeMethod(receiver, receiver.getType(), propertyName, safe, implicitThis)) return;
        }
        if (isClassReceiver) {
            // we are probably looking for a property of the class
            if (makeGetPropertyWithGetter(receiver, CLASS_Type, propertyName, safe, implicitThis)) return;
            if (makeGetField(receiver, CLASS_Type, propertyName, safe, false)) return;
        }
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, propertyName, safe, implicitThis)) return;

        // GROOVY-5580: it is still possible that we're calling a superinterface property
        String getterName = "get" + capitalize(propertyName);
        String altGetterName = "is" + capitalize(propertyName);
        if (receiverType.isInterface()) {
            MethodNode getterMethod = null;
            for (ClassNode anInterface : receiverType.getAllInterfaces()) {
                getterMethod = anInterface.getGetterMethod(getterName);
                if (getterMethod == null) getterMethod = anInterface.getGetterMethod(altGetterName);
                if (getterMethod != null) break;
            }
            // GROOVY-5585
            if (getterMethod == null) {
                getterMethod = OBJECT_TYPE.getGetterMethod(getterName);
            }
            if (getterMethod != null) {
                MethodCallExpression call = callX(receiver, getterName);
                call.setImplicitThis(false);
                call.setMethodTarget(getterMethod);
                call.setSafe(safe);
                call.setSourcePosition(receiver);
                call.visit(controller.getAcg());
                return;
            }
        }

        // GROOVY-5568: we would be facing a DGM call, but instead of foo.getText(), have foo.text
        List<MethodNode> methods = findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), receiverType, getterName, ClassNode.EMPTY_ARRAY);
        for (MethodNode dgm : findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), receiverType, altGetterName, ClassNode.EMPTY_ARRAY)) {
            if (Boolean_TYPE.equals(getWrapper(dgm.getReturnType()))) {
                methods.add(dgm);
            }
        }
        if (!methods.isEmpty()) {
            List<MethodNode> methodNodes = chooseBestMethod(receiverType, methods, ClassNode.EMPTY_ARRAY);
            if (methodNodes.size() == 1) {
                MethodNode getter = methodNodes.get(0);
                MethodCallExpression call = callX(receiver, getter.getName());
                call.setImplicitThis(false);
                call.setMethodTarget(getter);
                call.setSafe(safe);
                call.setSourcePosition(receiver);
                call.visit(controller.getAcg());
                return;
            }
        }

        if (!isStaticProperty && isOrImplements(receiverType, LIST_TYPE)) {
            writeListDotProperty(receiver, propertyName, mv, safe);
            return;
        }

        String receiverName = (receiver instanceof ClassExpression ? receiver.getType() : receiverType).toString(false);
        controller.getSourceUnit().addError(
                new SyntaxException("Access to " + receiverName + "#" + propertyName + " is forbidden", receiver));
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
        call.setSafe(false);
        call.visit(controller.getAcg());
    }

    private void writeMapDotProperty(final Expression receiver, final String propertyName, final MethodVisitor mv, final boolean safe) {
        receiver.visit(controller.getAcg()); // load receiver

        Label exit = new Label();
        if (safe) {
            Label doGet = new Label();
            mv.visitJumpInsn(IFNONNULL, doGet);
            controller.getOperandStack().remove(1);
            mv.visitInsn(ACONST_NULL);
            mv.visitJumpInsn(GOTO, exit);
            mv.visitLabel(doGet);
            receiver.visit(controller.getAcg());
        }

        mv.visitLdcInsn(propertyName); // load property name
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        if (safe) {
            mv.visitLabel(exit);
        }
        controller.getOperandStack().replace(OBJECT_TYPE);
    }

    private void writeListDotProperty(final Expression receiver, final String propertyName, final MethodVisitor mv, final boolean safe) {
        ClassNode componentType = receiver.getNodeMetaData(StaticCompilationMetadataKeys.COMPONENT_TYPE);
        if (componentType == null) {
            componentType = OBJECT_TYPE;
        }
        // for lists, replace list.foo with:
        // def result = new ArrayList(list.size())
        // for (e in list) { result.add (e.foo) }
        // result
        CompileStack compileStack = controller.getCompileStack();

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
                    && (StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(receiverType, classNode)
                        || StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(classNode, receiverType))) {
                Map<String, MethodNode> accessors = receiverType.redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_FIELDS_ACCESSORS);
                if (accessors != null) {
                    MethodNode methodNode = accessors.get(fieldName);
                    if (methodNode != null) {
                        MethodCallExpression call = callX(receiver, methodNode.getName(), args(field.isStatic() ? nullX() : receiver));
                        call.setImplicitThis(implicitThis);
                        call.setMethodTarget(methodNode);
                        call.setSafe(safe);
                        call.visit(controller.getAcg());
                        return true;
                    }
                }
            }
        } else if (implicitThis) {
            ClassNode outerClass = receiverType.getOuterClass();
            if (outerClass != null && !receiverType.isStaticClass()) {
                Expression expr;
                ClassNode thisType = outerClass;
                if (controller.isInGeneratedFunction()) {
                    while (isGeneratedFunction(thisType)) {
                        thisType = thisType.getOuterClass();
                    }

                    MethodCallExpression call = callThisX("getThisObject");
                    call.setImplicitThis(true);
                    call.setMethodTarget(CLOSURE_GETTHISOBJECT_METHOD);
                    call.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, thisType);

                    expr = castX(thisType, call);
                } else {
                    expr = propX(classX(outerClass), "this");
                    ((PropertyExpression) expr).setImplicitThis(true);
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
        ClassNode receiverType = controller.getClassNode();
        if (!AsmClassGenerator.isThisExpression(receiver) || controller.isInGeneratedFunction()) {
            receiverType = controller.getTypeChooser().resolveType(receiver, receiverType);
        }

        String property = propertyName;
        if (implicitThis && controller.getInvocationWriter() instanceof StaticInvocationWriter) {
            Expression currentCall = ((StaticInvocationWriter) controller.getInvocationWriter()).getCurrentCall();
            if (currentCall != null && currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER) != null) {
                property = currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
                String[] props = property.split("\\.");
                BytecodeExpression thisLoader = bytecodeX(CLOSURE_TYPE, mv -> mv.visitVarInsn(ALOAD, 0));
                PropertyExpression pexp = propX(thisLoader, constX(props[0]), safe);
                for (int i = 1, n = props.length; i < n; i += 1) {
                    pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, CLOSURE_TYPE);
                    pexp = propX(pexp, props[i]);
                }
                pexp.visit(controller.getAcg());
                return;
            }
        }

        if (makeGetPropertyWithGetter(receiver, receiverType, property, safe, implicitThis)) return;
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, property, safe, implicitThis)) return;
        if (makeGetField(receiver, receiverType, property, safe, implicitThis)) return;

        MethodCallExpression call = callX(receiver, "getProperty", args(constX(property)));
        call.setImplicitThis(implicitThis);
        call.setMethodTarget(GROOVYOBJECT_GETPROPERTY_METHOD);
        call.setSafe(safe);
        call.visit(controller.getAcg());
    }

    @Override
    public void makeCallSiteArrayInitializer() {
    }

    private boolean makeGetPropertyWithGetter(final Expression receiver, final ClassNode receiverType, final String propertyName, final boolean safe, final boolean implicitThis) {
        // does a getter exist?
        String getterName = "get" + capitalize(propertyName);
        MethodNode getterNode = receiverType.getGetterMethod(getterName);
        if (getterNode == null) {
            getterName = "is" + capitalize(propertyName);
            getterNode = receiverType.getGetterMethod(getterName);
        }
        if (getterNode != null && receiver instanceof ClassExpression && !CLASS_Type.equals(receiverType) && !getterNode.isStatic()) {
            return false;
        }

        // GROOVY-5561: if two files are compiled in the same source unit
        // and that one references the other, the getters for properties have not been
        // generated by the compiler yet (generated by the Verifier)
        PropertyNode propertyNode = receiverType.getProperty(propertyName);
        if (getterNode == null && propertyNode != null) {
            // it is possible to use a getter
            String prefix = "get";
            if (boolean_TYPE.equals(propertyNode.getOriginType())) {
                prefix = "is";
            }
            getterName = prefix + capitalize(propertyName);
            getterNode = new MethodNode(
                    getterName,
                    ACC_PUBLIC,
                    propertyNode.getOriginType(),
                    Parameter.EMPTY_ARRAY,
                    ClassNode.EMPTY_ARRAY,
                    EmptyStatement.INSTANCE);
            getterNode.setDeclaringClass(receiverType);
            if (propertyNode.isStatic()) getterNode.setModifiers(ACC_PUBLIC + ACC_STATIC);
        }
        if (getterNode != null) {
            MethodCallExpression call = callX(receiver, getterName);
            call.setImplicitThis(implicitThis);
            call.setMethodTarget(getterNode);
            call.setSafe(safe);
            call.setSourcePosition(receiver);
            call.visit(controller.getAcg());
            return true;
        }

        if (receiverType instanceof InnerClassNode && !receiverType.isStaticClass()) {
            if (makeGetPropertyWithGetter(receiver,  receiverType.getOuterClass(), propertyName,  safe, implicitThis)) {
                return true;
            }
        }

        // check direct interfaces (GROOVY-7149)
        for (ClassNode node : receiverType.getInterfaces()) {
            if (makeGetPropertyWithGetter(receiver, node, propertyName, safe, implicitThis)) {
                return true;
            }
        }
        // go upper level
        ClassNode superClass = receiverType.getSuperClass();
        if (superClass != null) {
            return makeGetPropertyWithGetter(receiver, superClass, propertyName, safe, implicitThis);
        }

        return false;
    }

    boolean makeGetField(final Expression receiver, final ClassNode receiverType, final String fieldName, final boolean safe, final boolean implicitThis) {
        FieldNode field = receiverType.getField(fieldName);

        if (field != null && isDirectAccessAllowed(field, controller.getClassNode())) {
            CompileStack compileStack = controller.getCompileStack();
            MethodVisitor mv = controller.getMethodVisitor();
            ClassNode replacementType = field.getOriginType();
            OperandStack operandStack = controller.getOperandStack();
            if (field.isStatic()) {
                mv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(field.getOwner()), fieldName, BytecodeHelper.getTypeDescription(replacementType));
                operandStack.push(replacementType);
            } else {
                if (implicitThis) {
                    compileStack.pushImplicitThis(implicitThis);
                    receiver.visit(controller.getAcg());
                    compileStack.popImplicitThis();
                } else {
                    receiver.visit(controller.getAcg());
                }
                Label exit = new Label();
                if (safe) {
                    mv.visitInsn(DUP);
                    Label doGet = new Label();
                    mv.visitJumpInsn(IFNONNULL, doGet);
                    mv.visitInsn(POP);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitJumpInsn(GOTO, exit);
                    mv.visitLabel(doGet);
                }
                if (!operandStack.getTopOperand().isDerivedFrom(field.getOwner())) {
                    mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(field.getOwner()));
                }
                mv.visitFieldInsn(GETFIELD, BytecodeHelper.getClassInternalName(field.getOwner()), fieldName, BytecodeHelper.getTypeDescription(replacementType));
                if (safe) {
                    if (ClassHelper.isPrimitiveType(replacementType)) {
                        operandStack.replace(replacementType);
                        operandStack.box();
                        replacementType = operandStack.getTopOperand();
                    }
                    mv.visitLabel(exit);
                }
            }
            operandStack.replace(replacementType);
            return true;
        }

        for (ClassNode face : receiverType.getInterfaces()) {
            // GROOVY-7039
            if (face != receiverType && makeGetField(receiver, face, fieldName, safe, implicitThis)) {
                return true;
            }
        }

        ClassNode superClass = receiverType.getSuperClass();
        if (superClass != null && !OBJECT_TYPE.equals(superClass)) {
            return makeGetField(receiver, superClass, fieldName, safe, implicitThis);
        }
        return false;
    }

    /**
     * Direct access is allowed from the declaring class of the field and sometimes from inner and peer types.
     *
     * @return {@code true} if GETFIELD or GETSTATIC is safe for given field and receiver
     */
    private static boolean isDirectAccessAllowed(final FieldNode field, final ClassNode receiver) {
        // first, direct access from anywhere for public fields
        if (field.isPublic()) return true;

        ClassNode declaringType = field.getDeclaringClass().redirect(), receiverType = receiver.redirect();

        // next, direct access from within the declaring class
        if (receiverType.equals(declaringType)) return true;

        if (field.isPrivate()) return false;

        // next, direct access from within the declaring package
        if (Objects.equals(receiver.getPackageName(), declaringType.getPackageName())) return true;

        // last, inner class access to outer class fields
        receiverType = receiverType.getOuterClass();
        while (receiverType != null) {
            if (receiverType.equals(declaringType)) {
                return true;
            }
            receiverType = receiverType.getOuterClass();
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
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode rType = typeChooser.resolveType(receiver, classNode);
        ClassNode aType = typeChooser.resolveType(arguments, classNode);
        if (trySubscript(receiver, message, arguments, rType, aType, safe)) {
            return;
        }
        // now try with flow type instead of declaration type
        rType = receiver.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (receiver instanceof VariableExpression && rType == null) {
            // TODO: can STCV be made smarter to avoid this check?
            VariableExpression ve = (VariableExpression) ((VariableExpression)receiver).getAccessedVariable();
            rType = ve.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        }
        if (rType!=null && trySubscript(receiver, message, arguments, rType, aType, safe)) {
            return;
        }
        // todo: more cases
        throw new GroovyBugError(
                "At line " + receiver.getLineNumber() + " column " + receiver.getColumnNumber() + "\n" +
                "On receiver: " + receiver.getText() + " with message: " + message + " and arguments: " + arguments.getText() + "\n" +
                "This method should not have been called. Please try to create a simple example reproducing\n" +
                "this error and file a bug report at https://issues.apache.org/jira/browse/GROOVY");
    }

    private boolean trySubscript(final Expression receiver, final String message, final Expression arguments, ClassNode rType, final ClassNode aType, boolean safe) {
        if (getWrapper(rType).isDerivedFrom(Number_TYPE)
                && getWrapper(aType).isDerivedFrom(Number_TYPE)) {
            if ("plus".equals(message) || "minus".equals(message) || "multiply".equals(message) || "div".equals(message)) {
                writeNumberNumberCall(receiver, message, arguments);
                return true;
            } else if ("power".equals(message)) {
                writePowerCall(receiver, arguments, rType, aType);
                return true;
            } else if ("mod".equals(message) || "leftShift".equals(message) || "rightShift".equals(message) || "rightShiftUnsigned".equals(message)
                    || "and".equals(message) || "or".equals(message) || "xor".equals(message)) {
                writeOperatorCall(receiver, arguments, message);
                return true;
            }
        } else if (STRING_TYPE.equals(rType) && "plus".equals(message)) {
            writeStringPlusCall(receiver, message, arguments);
            return true;
        } else if ("getAt".equals(message)) {
            if (rType.isArray() && getWrapper(aType).isDerivedFrom(Number_TYPE) && !safe) {
                writeArrayGet(receiver, arguments, rType, aType);
                return true;
            } else {
                // check if a getAt method can be found on the receiver
                ClassNode current = rType;
                MethodNode getAtNode = null;
                while (current != null && getAtNode == null) {
                    getAtNode = current.getDeclaredMethod("getAt", new Parameter[]{new Parameter(aType, "index")});
                    if (getAtNode == null) {
                        getAtNode = getCompatibleMethod(current, "getAt", aType);
                    }
                    if (getAtNode == null && isPrimitiveType(aType)) {
                        getAtNode = current.getDeclaredMethod("getAt", new Parameter[]{new Parameter(getWrapper(aType), "index")});
                        if (getAtNode == null) {
                            getAtNode = getCompatibleMethod(current, "getAt", getWrapper(aType));
                        }
                    } else if (getAtNode == null && aType.isDerivedFrom(Number_TYPE)) {
                        getAtNode = current.getDeclaredMethod("getAt", new Parameter[]{new Parameter(getUnwrapper(aType), "index")});
                        if (getAtNode == null) {
                            getAtNode = getCompatibleMethod(current, "getAt", getUnwrapper(aType));
                        }
                    }
                    current = current.getSuperClass();
                }
                if (getAtNode != null) {
                    MethodCallExpression call = callX(receiver, "getAt", arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(getAtNode);
                    call.setSafe(safe);
                    call.setSourcePosition(arguments);
                    call.visit(controller.getAcg());
                    return true;
                }

                // make sure Map#getAt() and List#getAt handled with the bracket syntax are properly compiled
                ClassNode[] args = {aType};
                boolean acceptAnyMethod =
                        MAP_TYPE.equals(rType) || rType.implementsInterface(MAP_TYPE)
                        || LIST_TYPE.equals(rType) || rType.implementsInterface(LIST_TYPE);
                List<MethodNode> nodes = findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), rType, message, args);
                if (nodes.isEmpty()) {
                    // retry with raw types
                    rType = rType.getPlainNodeReference();
                    nodes = findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), rType, message, args);
                }
                if (nodes.size() == 1 || (nodes.size() > 1 && acceptAnyMethod)) {
                    MethodCallExpression call = callX(receiver, message, arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(nodes.get(0));
                    call.setSafe(safe);
                    call.setSourcePosition(arguments);
                    call.visit(controller.getAcg());
                    return true;
                }
                if (implementsInterfaceOrIsSubclassOf(rType, MAP_TYPE)) {
                    // fallback to Map#get
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

    private MethodNode getCompatibleMethod(final ClassNode current, final String getAt, final ClassNode aType) {
        // TODO this really should find "best" match or find all matches and complain about ambiguity if more than one
        // TODO handle getAt with more than one parameter
        // TODO handle default getAt methods on Java 8 interfaces
        for (MethodNode methodNode : current.getDeclaredMethods("getAt")) {
            if (methodNode.getParameters().length == 1) {
                ClassNode paramType = methodNode.getParameters()[0].getType();
                if (aType.isDerivedFrom(paramType) || aType.declaresInterface(paramType)) {
                    return methodNode;
                }
            }
        }
        return null;
    }

    private void writeArrayGet(final Expression receiver, final Expression arguments, final ClassNode rType, final ClassNode aType) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        // visit receiver
        receiver.visit(controller.getAcg());
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
        if (BigDecimal_TYPE.equals(rType) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/math/BigDecimal;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else if (BigInteger_TYPE.equals(rType) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/math/BigInteger;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else if (Long_TYPE.equals(getWrapper(rType)) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/lang/Long;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else if (Integer_TYPE.equals(getWrapper(rType)) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Number;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "power", "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;", false);
        }
        controller.getOperandStack().replace(Number_TYPE, m2 - m1);
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
        controller.getOperandStack().replace(STRING_TYPE, m2 - m1);
    }

    private void writeNumberNumberCall(final Expression receiver, final String message, final Expression arguments) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        // slow path
        prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        visitBoxedArgument(arguments);
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/dgmimpl/NumberNumber" + capitalize(message), message, "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;", false);
        controller.getOperandStack().replace(Number_TYPE, m2 - m1);
    }

    @Override
    public void fallbackAttributeOrPropertySite(final PropertyExpression expression, final Expression objectExpression, final String name, final MethodCallerMultiAdapter adapter) {
        if (name != null && (adapter == AsmClassGenerator.setField || adapter == AsmClassGenerator.setGroovyObjectField)) {
            TypeChooser typeChooser = controller.getTypeChooser();
            ClassNode classNode = controller.getClassNode();
            ClassNode rType = typeChooser.resolveType(objectExpression, classNode);
            if (controller.getCompileStack().isLHS()) {
                if (setField(expression, objectExpression, rType, name)) return;
            } else {
                if (getField(expression, objectExpression, rType, name)) return;
            }
        }
        super.fallbackAttributeOrPropertySite(expression, objectExpression, name, adapter);
    }

    // this is just a simple set field handling static and non-static, but not Closure and inner classes
    private boolean setField(final PropertyExpression expression, final Expression objectExpression, final ClassNode rType, final String name) {
        if (expression.isSafe()) return false;
        FieldNode fn = AsmClassGenerator.getDeclaredFieldOfCurrentClassOrAccessibleFieldOfSuper(controller.getClassNode(), rType, name, false);
        if (fn == null) return false;
        OperandStack stack = controller.getOperandStack();
        stack.doGroovyCast(fn.getType());

        MethodVisitor mv = controller.getMethodVisitor();
        String ownerName = BytecodeHelper.getClassInternalName(fn.getOwner());
        if (!fn.isStatic()) {
            controller.getCompileStack().pushLHS(false);
            objectExpression.visit(controller.getAcg());
            controller.getCompileStack().popLHS();
            if (!rType.equals(stack.getTopOperand())) {
                BytecodeHelper.doCast(mv, rType);
                stack.replace(rType);
            }
            stack.swap();
            mv.visitFieldInsn(PUTFIELD, ownerName, name, BytecodeHelper.getTypeDescription(fn.getType()));
            stack.remove(1);
        } else {
            mv.visitFieldInsn(PUTSTATIC, ownerName, name, BytecodeHelper.getTypeDescription(fn.getType()));
        }

        //mv.visitInsn(ACONST_NULL);
        //stack.replace(OBJECT_TYPE);
        return true;
    }

    private boolean getField(final PropertyExpression expression, final Expression receiver, ClassNode receiverType, final String name) {
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
    }
}
