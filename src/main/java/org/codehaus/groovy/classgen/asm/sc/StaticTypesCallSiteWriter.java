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
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
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
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.classgen.AsmClassGenerator.samePackages;
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

    private static final ClassNode INVOKERHELPER_TYPE = ClassHelper.make(InvokerHelper.class);
    private static final MethodNode GROOVYOBJECT_GETPROPERTY_METHOD = GROOVY_OBJECT_TYPE.getMethod("getProperty", new Parameter[]{new Parameter(STRING_TYPE, "propertyName")});
    private static final MethodNode INVOKERHELPER_GETPROPERTY_METHOD = INVOKERHELPER_TYPE.getMethod("getProperty", new Parameter[]{new Parameter(OBJECT_TYPE, "object"), new Parameter(STRING_TYPE, "propertyName")});
    private static final MethodNode INVOKERHELPER_GETPROPERTYSAFE_METHOD = INVOKERHELPER_TYPE.getMethod("getPropertySafe", new Parameter[]{new Parameter(OBJECT_TYPE, "object"), new Parameter(STRING_TYPE, "propertyName")});
    private static final MethodNode CLOSURE_GETTHISOBJECT_METHOD = CLOSURE_TYPE.getMethod("getThisObject", Parameter.EMPTY_ARRAY);
    private static final ClassNode COLLECTION_TYPE = make(Collection.class);
    private static final MethodNode COLLECTION_SIZE_METHOD = COLLECTION_TYPE.getMethod("size", Parameter.EMPTY_ARRAY);
    private static final MethodNode MAP_GET_METHOD = MAP_TYPE.getMethod("get", new Parameter[] { new Parameter(OBJECT_TYPE, "key")});


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
    public void makeGetPropertySite(Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
        Object dynamic = receiver.getNodeMetaData(StaticCompilationMetadataKeys.RECEIVER_OF_DYNAMIC_PROPERTY);
        if (dynamic !=null) {
            makeDynamicGetProperty(receiver, methodName, safe);
            return;
        }
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode receiverType = receiver.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
        if (receiverType==null) {
            receiverType = typeChooser.resolveType(receiver, classNode);
        }
        Object type = receiver.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (type==null && receiver instanceof VariableExpression) {
            Variable variable = ((VariableExpression) receiver).getAccessedVariable();
            if (variable instanceof Expression) {
                type = ((Expression) variable).getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            }
        }
        if (type!=null) {
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

        if (receiverType.isArray() && methodName.equals("length")) {
            receiver.accept(controller.getAcg());
            ClassNode arrayGetReturnType = typeChooser.resolveType(receiver, classNode);
            controller.getOperandStack().doGroovyCast(arrayGetReturnType);
            mv.visitInsn(ARRAYLENGTH);
            controller.getOperandStack().replace(int_TYPE);
            return;
        } else if (
                (receiverType.implementsInterface(COLLECTION_TYPE)
                        || COLLECTION_TYPE.equals(receiverType)) && ("size".equals(methodName) || "length".equals(methodName))) {
            MethodCallExpression expr = new MethodCallExpression(
                    receiver,
                    "size",
                    ArgumentListExpression.EMPTY_ARGUMENTS
            );
            expr.setMethodTarget(COLLECTION_SIZE_METHOD);
            expr.setImplicitThis(implicitThis);
            expr.setSafe(safe);
            expr.accept(controller.getAcg());
            return;
        }

        boolean isStaticProperty = receiver instanceof ClassExpression
                && (receiverType.isDerivedFrom(receiver.getType()) || receiverType.implementsInterface(receiver.getType()));

        if (!isStaticProperty && (receiverType.implementsInterface(MAP_TYPE) || MAP_TYPE.equals(receiverType))) {
            // for maps, replace map.foo with map.get('foo')
            writeMapDotProperty(receiver, methodName, mv, safe);
            return;
        }
        if (makeGetPropertyWithGetter(receiver, receiverType, methodName, safe, implicitThis)) return;
        if (makeGetField(receiver, receiverType, methodName, safe, implicitThis, samePackages(receiverType.getPackageName(), classNode.getPackageName()))) return;
        if (receiver instanceof ClassExpression) {
            if (makeGetField(receiver, receiver.getType(), methodName, safe, implicitThis, samePackages(receiver.getType().getPackageName(), classNode.getPackageName()))) return;
            if (makeGetPropertyWithGetter(receiver, receiver.getType(), methodName, safe, implicitThis)) return;
            if (makeGetPrivateFieldWithBridgeMethod(receiver, receiver.getType(), methodName, safe, implicitThis)) return;
        }
        if (isClassReceiver) {
            // we are probably looking for a property of the class
            if (makeGetPropertyWithGetter(receiver, CLASS_Type, methodName, safe, implicitThis)) return;
            if (makeGetField(receiver, CLASS_Type, methodName, safe, false, true)) return;
        }
        if (receiverType.isEnum()) {
            mv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(receiverType), methodName, BytecodeHelper.getTypeDescription(receiverType));
            controller.getOperandStack().push(receiverType);
            return;
        }
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, methodName, safe, implicitThis)) return;

        // GROOVY-5580, it is still possible that we're calling a superinterface property
        String getterName = "get" + capitalize(methodName);
        String altGetterName = "is" + capitalize(methodName);
        if (receiverType.isInterface()) {
            Set<ClassNode> allInterfaces = receiverType.getAllInterfaces();
            MethodNode getterMethod = null;
            for (ClassNode anInterface : allInterfaces) {
                getterMethod = anInterface.getGetterMethod(getterName);
                if (getterMethod == null) getterMethod = anInterface.getGetterMethod(altGetterName);
                if (getterMethod != null) break;
            }
            // GROOVY-5585
            if (getterMethod == null) {
                getterMethod = OBJECT_TYPE.getGetterMethod(getterName);
            }

            if (getterMethod != null) {
                MethodCallExpression call = new MethodCallExpression(
                        receiver,
                        getterName,
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                call.setMethodTarget(getterMethod);
                call.setImplicitThis(false);
                call.setSourcePosition(receiver);
                call.setSafe(safe);
                call.accept(controller.getAcg());
                return;
            }

        }

        // GROOVY-5568, we would be facing a DGM call, but instead of foo.getText(), have foo.text
        List<MethodNode> methods = findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), receiverType, getterName, ClassNode.EMPTY_ARRAY);
        for (MethodNode m: findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), receiverType, altGetterName, ClassNode.EMPTY_ARRAY)) {
            if (Boolean_TYPE.equals(getWrapper(m.getReturnType()))) methods.add(m);
        }
        if (!methods.isEmpty()) {
            List<MethodNode> methodNodes = chooseBestMethod(receiverType, methods, ClassNode.EMPTY_ARRAY);
            if (methodNodes.size() == 1) {
                MethodNode getter = methodNodes.get(0);
                MethodCallExpression call = new MethodCallExpression(
                        receiver,
                        getter.getName(),
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                call.setMethodTarget(getter);
                call.setImplicitThis(false);
                call.setSafe(safe);
                call.setSourcePosition(receiver);
                call.accept(controller.getAcg());
                return;
            }
        }

        if (!isStaticProperty && (receiverType.implementsInterface(LIST_TYPE) || LIST_TYPE.equals(receiverType))) {
            writeListDotProperty(receiver, methodName, mv, safe);
            return;
        }

        controller.getSourceUnit().addError(
                new SyntaxException("Access to "+
                                                (receiver instanceof ClassExpression ?receiver.getType():receiverType).toString(false)
                                                +"#"+methodName+" is forbidden", receiver.getLineNumber(), receiver.getColumnNumber(), receiver.getLastLineNumber(), receiver.getLastColumnNumber())
        );
        controller.getMethodVisitor().visitInsn(ACONST_NULL);
        controller.getOperandStack().push(OBJECT_TYPE);
    }

    private void makeDynamicGetProperty(final Expression receiver, final String methodName, final boolean safe) {
        MethodNode target = safe?INVOKERHELPER_GETPROPERTYSAFE_METHOD:INVOKERHELPER_GETPROPERTY_METHOD;
        MethodCallExpression mce = new MethodCallExpression(
                new ClassExpression(INVOKERHELPER_TYPE),
                target.getName(),
                new ArgumentListExpression(receiver, new ConstantExpression(methodName))
        );
        mce.setSafe(false);
        mce.setImplicitThis(false);
        mce.setMethodTarget(target);
        mce.accept(controller.getAcg());
    }

    private void writeMapDotProperty(final Expression receiver, final String methodName, final MethodVisitor mv, final boolean safe) {
        receiver.accept(controller.getAcg()); // load receiver

        Label exit = new Label();
        if (safe) {
            Label doGet = new Label();
            mv.visitJumpInsn(IFNONNULL, doGet);
            controller.getOperandStack().remove(1);
            mv.visitInsn(ACONST_NULL);
            mv.visitJumpInsn(GOTO, exit);
            mv.visitLabel(doGet);
            receiver.accept(controller.getAcg());
        }

        mv.visitLdcInsn(methodName); // load property name
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        if (safe) {
            mv.visitLabel(exit);
        }
        controller.getOperandStack().replace(OBJECT_TYPE);
    }

    private void writeListDotProperty(final Expression receiver, final String methodName, final MethodVisitor mv, final boolean safe) {
        ClassNode componentType = receiver.getNodeMetaData(StaticCompilationMetadataKeys.COMPONENT_TYPE);
        if (componentType==null) {
            componentType = OBJECT_TYPE;
        }
        // for lists, replace list.foo with:
        // def result = new ArrayList(list.size())
        // for (e in list) { result.add (e.foo) }
        // result
        CompileStack compileStack = controller.getCompileStack();

        Label exit = new Label();
        if (safe) {
            receiver.accept(controller.getAcg());
            Label doGet = new Label();
            mv.visitJumpInsn(IFNONNULL, doGet);
            controller.getOperandStack().remove(1);
            mv.visitInsn(ACONST_NULL);
            mv.visitJumpInsn(GOTO, exit);
            mv.visitLabel(doGet);
        }

        Variable tmpList = new VariableExpression("tmpList", make(ArrayList.class));
        int var = compileStack.defineTemporaryVariable(tmpList, false);
        Variable iterator = new VariableExpression("iterator", Iterator_TYPE);
        int it = compileStack.defineTemporaryVariable(iterator, false);
        Variable nextVar = new VariableExpression("next", componentType);
        final int next = compileStack.defineTemporaryVariable(nextVar, false);

        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        receiver.accept(controller.getAcg());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
        controller.getOperandStack().remove(1);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V", false);
        mv.visitVarInsn(ASTORE, var);
        Label l1 = new Label();
        mv.visitLabel(l1);
        receiver.accept(controller.getAcg());
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
        final ClassNode finalComponentType = componentType;
        PropertyExpression pexp = new PropertyExpression(new BytecodeExpression() {
            @Override
            public void visit(final MethodVisitor mv) {
                mv.visitVarInsn(ALOAD, next);
            }

            @Override
            public ClassNode getType() {
                return finalComponentType;
            }
        }, methodName);
        pexp.accept(controller.getAcg());
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
        controller.getOperandStack().push(make(ArrayList.class));
        controller.getCompileStack().removeVar(next);
        controller.getCompileStack().removeVar(it);
        controller.getCompileStack().removeVar(var);
    }

    @SuppressWarnings("unchecked")
    private boolean makeGetPrivateFieldWithBridgeMethod(final Expression receiver, final ClassNode receiverType, final String fieldName, final boolean safe, final boolean implicitThis) {
        FieldNode field = receiverType.getField(fieldName);
        ClassNode outerClass = receiverType.getOuterClass();
        if (field==null && implicitThis && outerClass !=null && !receiverType.isStaticClass()) {
            Expression pexp;
            if (controller.isInClosure()) {
                MethodCallExpression mce = new MethodCallExpression(
                        new VariableExpression("this"),
                        "getThisObject",
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                mce.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, controller.getOutermostClass());
                mce.setImplicitThis(true);
                mce.setMethodTarget(CLOSURE_GETTHISOBJECT_METHOD);
                pexp = new CastExpression(controller.getOutermostClass(),mce);
            } else {
                pexp = new PropertyExpression(
                        new ClassExpression(outerClass),
                        "this"
                );
                ((PropertyExpression)pexp).setImplicitThis(true);
            }
            pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, outerClass);
            pexp.setSourcePosition(receiver);
            return makeGetPrivateFieldWithBridgeMethod(pexp, outerClass, fieldName, safe, true);
        }
        ClassNode classNode = controller.getClassNode();
        if (field!=null && Modifier.isPrivate(field.getModifiers())
                && (StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(receiverType, classNode) || StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(classNode,receiverType))
                && !receiverType.equals(classNode)) {
            Map<String, MethodNode> accessors = receiverType.redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_FIELDS_ACCESSORS);
            if (accessors!=null) {
                MethodNode methodNode = accessors.get(fieldName);
                if (methodNode!=null) {
                    MethodCallExpression mce = new MethodCallExpression(receiver, methodNode.getName(),
                            new ArgumentListExpression(field.isStatic()?new ConstantExpression(null):receiver));
                    mce.setMethodTarget(methodNode);
                    mce.setSafe(safe);
                    mce.setImplicitThis(implicitThis);
                    mce.accept(controller.getAcg());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void makeGroovyObjectGetPropertySite(final Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode receiverType = typeChooser.resolveType(receiver, classNode);
        if (receiver instanceof VariableExpression && ((VariableExpression) receiver).isThisExpression() && !controller.isInClosure()) {
            receiverType = classNode;
        }
        
        String property = methodName;
        if (implicitThis) {
            if (controller.getInvocationWriter() instanceof StaticInvocationWriter) {
                MethodCallExpression currentCall = ((StaticInvocationWriter) controller.getInvocationWriter()).getCurrentCall();
                if (currentCall != null && currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER) != null) {
                    property = currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
                    String[] props = property.split("\\.");
                    BytecodeExpression thisLoader = new BytecodeExpression() {
                        @Override
                        public void visit(final MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0); // load this
                        }
                    };
                    thisLoader.setType(CLOSURE_TYPE);
                    Expression pexp = new PropertyExpression(thisLoader, new ConstantExpression(props[0]), safe);
                    for (int i = 1, propsLength = props.length; i < propsLength; i++) {
                        final String prop = props[i];
                        pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, CLOSURE_TYPE);
                        pexp = new PropertyExpression(pexp, prop);
                    }
                    pexp.accept(controller.getAcg());
                    return;
                }
            }
        }

        if (makeGetPropertyWithGetter(receiver, receiverType, property, safe, implicitThis)) return;
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, property, safe, implicitThis)) return;
        if (makeGetField(receiver, receiverType, property, safe, implicitThis, samePackages(receiverType.getPackageName(), classNode.getPackageName()))) return;

        MethodCallExpression call = new MethodCallExpression(
                receiver,
                "getProperty",
                new ArgumentListExpression(new ConstantExpression(property))
        );
        call.setImplicitThis(implicitThis);
        call.setSafe(safe);
        call.setMethodTarget(GROOVYOBJECT_GETPROPERTY_METHOD);
        call.accept(controller.getAcg());
    }

    @Override
    public void makeCallSiteArrayInitializer() {
    }

    private boolean makeGetPropertyWithGetter(final Expression receiver, final ClassNode receiverType, final String methodName, final boolean safe, final boolean implicitThis) {
        // does a getter exists ?
        String getterName = "get" + capitalize(methodName);
        MethodNode getterNode = receiverType.getGetterMethod(getterName);
        if (getterNode==null) {
            getterName = "is" + capitalize(methodName);
            getterNode = receiverType.getGetterMethod(getterName);
        }
        if (getterNode!=null && receiver instanceof ClassExpression && !CLASS_Type.equals(receiverType) && !getterNode.isStatic()) {
            return false;
        }

        // GROOVY-5561: if two files are compiled in the same source unit
        // and that one references the other, the getters for properties have not been
        // generated by the compiler yet (generated by the Verifier)
        PropertyNode propertyNode = receiverType.getProperty(methodName);
        if (getterNode == null && propertyNode != null) {
            // it is possible to use a getter
            String prefix = "get";
            if (boolean_TYPE.equals(propertyNode.getOriginType())) {
                prefix = "is";
            }
            getterName = prefix + capitalize(methodName);
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
        if (getterNode!=null) {
            MethodCallExpression call = new MethodCallExpression(
                    receiver,
                    getterName,
                    ArgumentListExpression.EMPTY_ARGUMENTS
            );
            call.setSourcePosition(receiver);
            call.setMethodTarget(getterNode);
            call.setImplicitThis(implicitThis);
            call.setSafe(safe);
            call.accept(controller.getAcg());
            return true;
        }

        if (receiverType instanceof InnerClassNode && !receiverType.isStaticClass()) {
            if (makeGetPropertyWithGetter(receiver,  receiverType.getOuterClass(), methodName,  safe, implicitThis)) {
                return true;
            }
        }

        // check direct interfaces (GROOVY-7149)
        for (ClassNode node : receiverType.getInterfaces()) {
            if (makeGetPropertyWithGetter(receiver, node, methodName, safe, implicitThis)) {
                return true;
            }
        }
        // go upper level
        ClassNode superClass = receiverType.getSuperClass();
        if (superClass !=null) {
            return makeGetPropertyWithGetter(receiver, superClass, methodName, safe, implicitThis);
        }

        return false;
    }

    boolean makeGetField(final Expression receiver, final ClassNode receiverType, final String fieldName, final boolean safe, final boolean implicitThis, final boolean samePackage) {
        FieldNode field = receiverType.getField(fieldName);
        // direct access is allowed if we are in the same class as the declaring class
        // or we are in an inner class
        if (field !=null 
                && isDirectAccessAllowed(field, controller.getClassNode(), samePackage)) {
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
                }
                receiver.accept(controller.getAcg());
                if (implicitThis) compileStack.popImplicitThis();
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

        for (ClassNode intf : receiverType.getInterfaces()) {
            // GROOVY-7039
            if (intf!=receiverType && makeGetField(receiver, intf, fieldName, safe, implicitThis, false)) {
                return true;
            }
        }

        ClassNode superClass = receiverType.getSuperClass();
        if (superClass !=null) {
            return makeGetField(receiver, superClass, fieldName, safe, implicitThis, false);
        }
        return false;
    }

    private static boolean isDirectAccessAllowed(FieldNode a, ClassNode receiver, boolean isSamePackage) {
        ClassNode declaringClass = a.getDeclaringClass().redirect();
        ClassNode receiverType = receiver.redirect();

        // first, direct access from within the class or inner class nodes
        if (declaringClass.equals(receiverType)) return true;
        if (receiverType instanceof InnerClassNode) {
            while (receiverType instanceof InnerClassNode) {
                if (declaringClass.equals(receiverType)) return true;
                receiverType = receiverType.getOuterClass();
            }
        }

        // no getter
        return a.isPublic() || (a.isProtected() && isSamePackage);
    }

    @Override
    public void makeSiteEntry() {
    }

    @Override
    public void prepareCallSite(final String message) {
    }

    @Override
    public void makeSingleArgumentCall(final Expression receiver, final String message, final Expression arguments, boolean safe) {
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
                while (current!=null && getAtNode==null) {
                    getAtNode = current.getDeclaredMethod("getAt", new Parameter[]{new Parameter(aType, "index")});
                    if (getAtNode == null) {
                        getAtNode = getCompatibleMethod(current, "getAt", aType);
                    }
                    if (getAtNode==null && isPrimitiveType(aType)) {
                        getAtNode = current.getDeclaredMethod("getAt", new Parameter[]{new Parameter(getWrapper(aType), "index")});
                        if (getAtNode == null) {
                            getAtNode = getCompatibleMethod(current, "getAt", getWrapper(aType));
                        }
                    } else if (getAtNode==null && aType.isDerivedFrom(Number_TYPE)) {
                        getAtNode = current.getDeclaredMethod("getAt", new Parameter[]{new Parameter(getUnwrapper(aType), "index")});
                        if (getAtNode == null) {
                            getAtNode = getCompatibleMethod(current, "getAt", getUnwrapper(aType));
                        }
                    }
                    current = current.getSuperClass();
                }
                if (getAtNode!=null) {
                    MethodCallExpression call = new MethodCallExpression(
                            receiver,
                            "getAt",
                            arguments
                    );

                    call.setSafe(safe);
                    call.setSourcePosition(arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(getAtNode);
                    call.accept(controller.getAcg());
                    return true;
                }

                // make sure Map#getAt() and List#getAt handled with the bracket syntax are properly compiled
                ClassNode[] args = {aType};
                boolean acceptAnyMethod =
                        MAP_TYPE.equals(rType) || rType.implementsInterface(MAP_TYPE)
                        || LIST_TYPE.equals(rType) || rType.implementsInterface(LIST_TYPE);
                List<MethodNode> nodes = StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), rType, message, args);
                if (nodes.isEmpty()) {
                    // retry with raw types
                    rType = rType.getPlainNodeReference();
                    nodes = StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), rType, message, args);
                }
                nodes = StaticTypeCheckingSupport.chooseBestMethod(rType, nodes, args);
                if (nodes.size()==1 || nodes.size()>1 && acceptAnyMethod) {
                    MethodNode methodNode = nodes.get(0);
                    MethodCallExpression call = new MethodCallExpression(
                            receiver,
                            message,
                            arguments
                    );

                    call.setSafe(safe);
                    call.setSourcePosition(arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(methodNode);
                    call.accept(controller.getAcg());
                    return true;
                }
                if (implementsInterfaceOrIsSubclassOf(rType, MAP_TYPE)) {
                    // fallback to Map#get
                    MethodCallExpression call = new MethodCallExpression(
                            receiver,
                            "get",
                            arguments
                    );

                    call.setSafe(safe);
                    call.setMethodTarget(MAP_GET_METHOD);
                    call.setSourcePosition(arguments);
                    call.setImplicitThis(false);
                    call.accept(controller.getAcg());
                    return true;
                }
            }
        }
        return false;
    }

    private MethodNode getCompatibleMethod(ClassNode current, String getAt, ClassNode aType) {
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
        receiver.accept(controller.getAcg());
        // visit arguments as array index
        arguments.accept(controller.getAcg());
        operandStack.doGroovyCast(int_TYPE);
        int m2 = operandStack.getStackLength();
        // array access
        controller.getMethodVisitor().visitInsn(AALOAD);
        operandStack.replace(rType.getComponentType(), m2-m1);
    }

    private void writeOperatorCall(Expression receiver, Expression arguments, String operator) {
        prepareSiteAndReceiver(receiver, operator, false, controller.getCompileStack().isLHS());
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        visitBoxedArgument(arguments);
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/typehandling/NumberMath", operator, "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;", false);
        controller.getOperandStack().replace(Number_TYPE, 2);
    }

    private void writePowerCall(Expression receiver, Expression arguments, final ClassNode rType, ClassNode aType) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
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
        // todo: performance would be better if we created a StringBuilder
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
        prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
        visitBoxedArgument(arguments);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/DefaultGroovyMethods", "plus", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;", false);
        controller.getOperandStack().replace(STRING_TYPE, m2-m1);
    }

    private void writeNumberNumberCall(final Expression receiver, final String message, final Expression arguments) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
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
    public void fallbackAttributeOrPropertySite(PropertyExpression expression, Expression objectExpression, String name, MethodCallerMultiAdapter adapter) {
        if (name!=null &&
            (adapter == AsmClassGenerator.setField || adapter == AsmClassGenerator.setGroovyObjectField)
        ) {
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
    private boolean setField(PropertyExpression expression, Expression objectExpression, ClassNode rType, String name) {
        if (expression.isSafe()) return false;
        FieldNode fn = AsmClassGenerator.getDeclaredFieldOfCurrentClassOrAccessibleFieldOfSuper(controller.getClassNode(), rType, name, false);
        if (fn==null) return false;
        OperandStack stack = controller.getOperandStack();
        stack.doGroovyCast(fn.getType());

        MethodVisitor mv = controller.getMethodVisitor();
        String ownerName = BytecodeHelper.getClassInternalName(fn.getOwner());
        if (!fn.isStatic()) {
            controller.getCompileStack().pushLHS(false);
            objectExpression.accept(controller.getAcg());
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

    private boolean getField(PropertyExpression expression, Expression receiver, ClassNode receiverType, String name) {
        ClassNode classNode = controller.getClassNode();
        boolean safe = expression.isSafe();
        boolean implicitThis = expression.isImplicitThis();

        if (makeGetField(receiver, receiverType, name, safe, implicitThis, samePackages(receiverType.getPackageName(), classNode.getPackageName()))) return true;
        if (receiver instanceof ClassExpression) {
            if (makeGetField(receiver, receiver.getType(), name, safe, implicitThis, samePackages(receiver.getType().getPackageName(), classNode.getPackageName()))) return true;
            if (makeGetPrivateFieldWithBridgeMethod(receiver, receiver.getType(), name, safe, implicitThis)) return true;
        }
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, name, safe, implicitThis)) return true;

        boolean isClassReceiver = false;
        if (isClassClassNodeWrappingConcreteType(receiverType)) {
            isClassReceiver = true;
            receiverType = receiverType.getGenericsTypes()[0].getType();
        }
        if (isClassReceiver && makeGetField(receiver, CLASS_Type, name, safe, false, true)) return true;
        if (receiverType.isEnum()) {
            controller.getMethodVisitor().visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(receiverType), name, BytecodeHelper.getTypeDescription(receiverType));
            controller.getOperandStack().push(receiverType);
            return true;
        }
        return false;
    }
}
