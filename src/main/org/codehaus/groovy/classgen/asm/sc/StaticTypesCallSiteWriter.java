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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.*;

import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.chooseBestMethod;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;

/**
 * A call site writer which replaces call site caching with static calls. This means that the generated code
 * looks more like Java code than dynamic Groovy code. Best effort is made to use JVM instructions instead of
 * calls to helper methods.
 *
 * @author Cedric Champeau
 */
public class StaticTypesCallSiteWriter extends CallSiteWriter implements Opcodes {

    private static final MethodNode GROOVYOBJECT_GETPROPERTY_METHOD = GROOVY_OBJECT_TYPE.getMethod("getProperty", new Parameter[]{new Parameter(STRING_TYPE, "propertyName")});
    private static final ClassNode COLLECTION_TYPE = make(Collection.class);
    private static final MethodNode COLLECTION_SIZE_METHOD = COLLECTION_TYPE.getMethod("size", Parameter.EMPTY_ARRAY);
    private static final MethodNode MAP_GET_METHOD = MAP_TYPE.getMethod("get", new Parameter[] { new Parameter(OBJECT_TYPE, "key")});

    private WriterController controller;

    public StaticTypesCallSiteWriter(final StaticTypesWriterController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public void generateCallSiteArray() {
        if (controller instanceof StaticTypesWriterController) {
            ((StaticTypesWriterController)controller).getRegularCallSiteWriter().generateCallSiteArray();
        } else {
            super.generateCallSiteArray();
        }
    }

    @Override
    public void makeCallSite(final Expression receiver, final String message, final Expression arguments, final boolean safe, final boolean implicitThis, final boolean callCurrent, final boolean callStatic) {
    }

    @Override
    public void makeGetPropertySite(Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode receiverType = (ClassNode) receiver.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
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
        if (receiverType.equals(CLASS_Type)
                && receiverType.getGenericsTypes()!=null
                && !receiverType.getGenericsTypes()[0].isPlaceholder()) {
            isClassReceiver = true;
            receiverType = receiverType.getGenericsTypes()[0].getType();
        }
        MethodVisitor mv = controller.getMethodVisitor();

        if (receiverType.isArray() && methodName.equals("length")) {
            receiver.visit(controller.getAcg());
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
            expr.visit(controller.getAcg());
            return;
        }
        if (makeGetPropertyWithGetter(receiver, receiverType, methodName, safe, implicitThis)) return;
        if (makeGetField(receiver, receiverType, methodName, implicitThis, samePackages(receiverType.getPackageName(), classNode.getPackageName()))) return;
        if (receiverType.isEnum()) {
            mv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(receiverType), methodName, BytecodeHelper.getTypeDescription(receiverType));
            controller.getOperandStack().push(receiverType);
            return;
        }
        if (receiver instanceof ClassExpression) {
            if (makeGetField(receiver, receiver.getType(), methodName, implicitThis, samePackages(receiver.getType().getPackageName(), classNode.getPackageName()))) return;
            if (makeGetPropertyWithGetter(receiver, receiver.getType(), methodName, safe, implicitThis)) return;
            if (makeGetPrivateFieldWithBridgeMethod(receiver, receiver.getType(), methodName, safe, implicitThis)) return;
        }
        if (isClassReceiver) {
            // we are probably looking for a property of the class
            if (makeGetPropertyWithGetter(receiver, CLASS_Type, methodName, safe, implicitThis)) return;
            if (makeGetField(receiver, CLASS_Type, methodName, false, true)) return;
        }
        if (makeGetPrivateFieldWithBridgeMethod(receiver, receiverType, methodName, safe, implicitThis)) return;

        // GROOVY-5580, it is still possible that we're calling a superinterface property
        String getterName = "get" + MetaClassHelper.capitalize(methodName);
        if (receiverType.isInterface()) {
            Set<ClassNode> allInterfaces = receiverType.getAllInterfaces();
            MethodNode getterMethod = null;
            for (ClassNode anInterface : allInterfaces) {
                getterMethod = anInterface.getGetterMethod(getterName);
                if (getterMethod!=null) break;
            }
            // GROOVY-5585
            if (getterMethod==null) {
                getterMethod = OBJECT_TYPE.getGetterMethod(getterName);
            }

            if (getterMethod!=null) {
                MethodCallExpression call = new MethodCallExpression(
                        receiver,
                        getterName,
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                call.setMethodTarget(getterMethod);
                call.setImplicitThis(false);
                call.setSourcePosition(receiver);
                call.visit(controller.getAcg());
                return;
            }

        }

        // GROOVY-5568, we would be facing a DGM call, but instead of foo.getText(), have foo.text
        List<MethodNode> methods = findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), receiverType, getterName, ClassNode.EMPTY_ARRAY);
        if (!methods.isEmpty()) {
            List<MethodNode> methodNodes = chooseBestMethod(receiverType, methods, ClassNode.EMPTY_ARRAY);
            if (methodNodes.size()==1) {
                MethodNode getter = methodNodes.get(0);
                MethodCallExpression call = new MethodCallExpression(
                        receiver,
                        getterName,
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                call.setMethodTarget(getter);
                call.setImplicitThis(false);
                call.setSourcePosition(receiver);
                call.visit(controller.getAcg());
                return;
            }
        }

        boolean isStaticProperty = receiver instanceof ClassExpression
                && (receiverType.isDerivedFrom(receiver.getType()) || receiverType.implementsInterface(receiver.getType()));

        if (!isStaticProperty) {
            if (receiverType.implementsInterface(MAP_TYPE) || MAP_TYPE.equals(receiverType)) {
                // for maps, replace map.foo with map.get('foo')
                writeMapDotProperty(receiver, methodName, mv, safe);
                return;
            }
            if (receiverType.implementsInterface(LIST_TYPE) || LIST_TYPE.equals(receiverType)) {
                writeListDotProperty(receiver, methodName, mv, safe);
                return;
            }
        }


        controller.getSourceUnit().addError(
                new SyntaxException("Access to "+
                                                (receiver instanceof ClassExpression ?receiver.getType():receiverType).toString(false)
                                                +"#"+methodName+" is forbidden", receiver.getLineNumber(), receiver.getColumnNumber(), receiver.getLastLineNumber(), receiver.getLastColumnNumber())
        );
        controller.getMethodVisitor().visitInsn(ACONST_NULL);
        controller.getOperandStack().push(OBJECT_TYPE);
    }

    private void writeMapDotProperty(final Expression receiver, final String methodName, final MethodVisitor mv, final boolean safe) {
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

        mv.visitLdcInsn(methodName); // load property name
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
        if (safe) {
            mv.visitLabel(exit);
        }
        controller.getOperandStack().replace(OBJECT_TYPE);
    }

    private void writeListDotProperty(final Expression receiver, final String methodName, final MethodVisitor mv, final boolean safe) {
        ClassNode componentType = (ClassNode) receiver.getNodeMetaData(StaticCompilationMetadataKeys.COMPONENT_TYPE);
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
            receiver.visit(controller.getAcg());
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
        receiver.visit(controller.getAcg());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I");
        controller.getOperandStack().remove(1);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V");
        mv.visitVarInsn(ASTORE, var);
        Label l1 = new Label();
        mv.visitLabel(l1);
        receiver.visit(controller.getAcg());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;");
        controller.getOperandStack().remove(1);
        mv.visitVarInsn(ASTORE, it);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, it);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
        Label l3 = new Label();
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitVarInsn(ALOAD, it);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
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
        pexp.visit(controller.getAcg());
        controller.getOperandStack().box();
        controller.getOperandStack().remove(1);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
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
        ClassNode classNode = controller.getClassNode();
        if (field!=null && Modifier.isPrivate(field.getModifiers())
                && (StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(receiverType, classNode) || StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(classNode,receiverType))
                && !receiverType.equals(classNode)) {
            Map<String, MethodNode> accessors = (Map<String, MethodNode>) receiverType.redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_FIELDS_ACCESSORS);
            if (accessors!=null) {
                MethodNode methodNode = accessors.get(fieldName);
                if (methodNode!=null) {
                    MethodCallExpression mce = new MethodCallExpression(receiver, methodNode.getName(),
                            new ArgumentListExpression(field.isStatic()?new ConstantExpression(null):receiver));
                    mce.setMethodTarget(methodNode);
                    mce.setSafe(safe);
                    mce.setImplicitThis(implicitThis);
                    mce.visit(controller.getAcg());
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
                    property = (String) currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
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
                    pexp.visit(controller.getAcg());
                    return;
                }
            }
        }


        if (makeGetPropertyWithGetter(receiver, receiverType, property, safe, implicitThis)) return;
        if (makeGetField(receiver, receiverType, property, implicitThis, samePackages(receiverType.getPackageName(), classNode.getPackageName()))) return;

        MethodCallExpression call = new MethodCallExpression(
                receiver,
                "getProperty",
                new ArgumentListExpression(new ConstantExpression(property))
        );
        call.setImplicitThis(implicitThis);
        call.setSafe(safe);
        call.setMethodTarget(GROOVYOBJECT_GETPROPERTY_METHOD);
        call.visit(controller.getAcg());
        return;
    }

    @Override
    public void makeCallSiteArrayInitializer() {
    }

    private boolean makeGetPropertyWithGetter(final Expression receiver, final ClassNode receiverType, final String methodName, final boolean safe, final boolean implicitThis) {
        // does a getter exists ?
        String getterName = "get" + MetaClassHelper.capitalize(methodName);
        MethodNode getterNode = receiverType.getGetterMethod(getterName);
        if (getterNode==null) {
            getterName = "is" + MetaClassHelper.capitalize(methodName);
            getterNode = receiverType.getGetterMethod(getterName);
        }
        if (getterNode!=null && receiver instanceof ClassExpression && !CLASS_Type.equals(receiverType) && !getterNode.isStatic()) {
            return false;
        }

        // GROOVY-5561: if two files are compiled in the same source unit
        // and that one references the other, the getters for properties have not been
        // generated by the compiler yet (generated by the Verifier)
        PropertyNode propertyNode = receiverType.getProperty(methodName);
        if (propertyNode!=null) {
            // it is possible to use a getter
            String prefix = "get";
            if (boolean_TYPE.equals(propertyNode.getOriginType())) {
                prefix = "is";
            }
            getterName = prefix + MetaClassHelper.capitalize(methodName);
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
            call.visit(controller.getAcg());
            return true;
        }

        // go upper level
        ClassNode superClass = receiverType.getSuperClass();
        if (superClass !=null) {
            return makeGetPropertyWithGetter(receiver, superClass, methodName, safe, implicitThis);
        }
        return false;
    }

    boolean makeGetField(final Expression receiver, final ClassNode receiverType, final String fieldName, final boolean implicitThis, final boolean samePackage) {
        FieldNode field = receiverType.getField(fieldName);
        // direct access is allowed if we are in the same class as the declaring class
        // or we are in an inner class
        if (field !=null 
                && isDirectAccessAllowed(field, controller.getClassNode(), samePackage)) {
            CompileStack compileStack = controller.getCompileStack();
            MethodVisitor mv = controller.getMethodVisitor();
            if (field.isStatic()) {
                mv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(field.getOwner()), fieldName, BytecodeHelper.getTypeDescription(field.getOriginType()));
                controller.getOperandStack().push(field.getOriginType());
            } else {
                if (implicitThis) {
                    compileStack.pushImplicitThis(implicitThis);
                }
                receiver.visit(controller.getAcg());
                if (implicitThis) compileStack.popImplicitThis();
                if (!controller.getOperandStack().getTopOperand().isDerivedFrom(field.getOwner())) {
                    mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(field.getOwner()));
                }
                mv.visitFieldInsn(GETFIELD, BytecodeHelper.getClassInternalName(field.getOwner()), fieldName, BytecodeHelper.getTypeDescription(field.getOriginType()));
            }
            controller.getOperandStack().replace(field.getOriginType());
            return true;
        }
        ClassNode superClass = receiverType.getSuperClass();
        if (superClass !=null) {
            return makeGetField(receiver, superClass, fieldName, implicitThis, false);
        }
        return false;
    }

    private static boolean samePackages(final String pkg1, final String pkg2) {
        return (
                (pkg1 ==null && pkg2 ==null)
                || pkg1 !=null && pkg1.equals(pkg2)
                );
    }

    private static boolean isDirectAccessAllowed(FieldNode a, ClassNode receiver, boolean isSamePackage) {
        ClassNode declaringClass = a.getDeclaringClass().redirect();
        ClassNode receiverType = receiver.redirect();

        // first, direct access from within the class or inner class nodes
        if (declaringClass.equals(receiverType)) return true;
        if (receiverType instanceof InnerClassNode) {
            while (receiverType!=null && receiverType instanceof InnerClassNode) {
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
    public void makeSingleArgumentCall(final Expression receiver, final String message, final Expression arguments) {
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode rType = typeChooser.resolveType(receiver, classNode);
        ClassNode aType = typeChooser.resolveType(arguments, classNode);
        if (getWrapper(rType).isDerivedFrom(Number_TYPE)
                && getWrapper(aType).isDerivedFrom(Number_TYPE)) {
            if ("plus".equals(message) || "minus".equals(message) || "multiply".equals(message) || "div".equals(message)) {
                writeNumberNumberCall(receiver, message, arguments);
                return;
            } else if ("power".equals(message)) {
                writePowerCall(receiver, arguments, rType, aType);
                return;
            } else if ("mod".equals(message)) {
                writeModCall(receiver, arguments, rType, aType);
                return;
            }
        } else if (STRING_TYPE.equals(rType) && "plus".equals(message)) {
            writeStringPlusCall(receiver, message, arguments);
            return;
        } else if ("getAt".equals(message)) {
            if (rType.isArray() && getWrapper(aType).isDerivedFrom(Number_TYPE)) {
                writeArrayGet(receiver, arguments, rType, aType);
                return;
            } else {
                // check if a getAt method can be found on the receiver
                ClassNode current = rType;
                MethodNode getAtNode = null;
                while (current!=null && getAtNode==null) {
                    getAtNode = current.getMethod("getAt", new Parameter[]{new Parameter(aType, "index")});
                    current = current.getSuperClass();
                }
                if (getAtNode!=null) {
                    MethodCallExpression call = new MethodCallExpression(
                            receiver,
                            "getAt",
                            arguments
                    );
                    call.setSourcePosition(arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(getAtNode);
                    call.visit(controller.getAcg());
                    return;
                }
        
                // make sure Map#getAt() and List#getAt handled with the bracket syntax are properly compiled
                ClassNode[] args = {aType};
                boolean acceptAnyMethod =
                        MAP_TYPE.equals(rType) || rType.implementsInterface(MAP_TYPE)
                        || LIST_TYPE.equals(rType) || rType.implementsInterface(LIST_TYPE);
                List<MethodNode> nodes = StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), rType, message, args);
                nodes = StaticTypeCheckingSupport.chooseBestMethod(rType, nodes, args);
                if (nodes.size()==1 || nodes.size()>1 && acceptAnyMethod) {
                    MethodNode methodNode = nodes.get(0);
                    MethodCallExpression call = new MethodCallExpression(
                            receiver,
                            message,
                            arguments
                    );
                    call.setSourcePosition(arguments);
                    call.setImplicitThis(false);
                    call.setMethodTarget(methodNode);
                    call.visit(controller.getAcg());
                    return;
                }
                if (implementsInterfaceOrIsSubclassOf(rType, MAP_TYPE)) {
                    // fallback to Map#get
                    MethodCallExpression call = new MethodCallExpression(
                            receiver,
                            "get",
                            arguments
                    );
                    call.setMethodTarget(MAP_GET_METHOD);
                    call.setSourcePosition(arguments);
                    call.setImplicitThis(false);
                    call.visit(controller.getAcg());
                    return;
                }
            }
        }
        // todo: more cases
        throw new GroovyBugError(
                "At line "+receiver.getLineNumber() + " column " + receiver.getColumnNumber() + "\n" +
                "On receiver: "+receiver.getText() + " with message: "+message+" and arguments: "+arguments.getText()+"\n"+
                "This method should not have been called. Please try to create a simple example reproducing this error and file" +
                "a bug report at http://jira.codehaus.org/browse/GROOVY");
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
        operandStack.replace(rType.getComponentType(), m2-m1);
    }

    private void writeModCall(Expression receiver, Expression arguments, ClassNode rType, ClassNode aType) {
        prepareSiteAndReceiver(receiver, "mod", false, controller.getCompileStack().isLHS());
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        visitBoxedArgument(arguments);
        controller.getOperandStack().doGroovyCast(Number_TYPE);
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESTATIC,
                "org/codehaus/groovy/runtime/typehandling/NumberMath",
                "mod",
                "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;");
        controller.getOperandStack().replace(Number_TYPE, 2);
    }
    
    private void writePowerCall(Expression receiver, Expression arguments, final ClassNode rType, ClassNode aType) {
        OperandStack operandStack = controller.getOperandStack();
        int m1 = operandStack.getStackLength();
        //slow Path
        prepareSiteAndReceiver(receiver, "power", false, controller.getCompileStack().isLHS());
        visitBoxedArgument(arguments);
        int m2 = operandStack.getStackLength();
        MethodVisitor mv = controller.getMethodVisitor();
        if (BigDecimal_TYPE.equals(rType) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/math/BigDecimal;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else if (BigInteger_TYPE.equals(rType) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/math/BigInteger;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else if (Long_TYPE.equals(getWrapper(rType)) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/lang/Long;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else if (Integer_TYPE.equals(getWrapper(rType)) && Integer_TYPE.equals(getWrapper(aType))) {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Number;");
        } else {
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "power",
                    "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;");
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
        mv.visitMethodInsn(INVOKESTATIC,
                "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                "plus",
                "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;");
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
        mv.visitMethodInsn(INVOKESTATIC,
                "org/codehaus/groovy/runtime/dgmimpl/NumberNumber" + MetaClassHelper.capitalize(message),
                message,
                "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;");
        controller.getOperandStack().replace(Number_TYPE, m2 - m1);
    }


}
