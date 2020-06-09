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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CallSiteWriter;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.ExpressionAsVariableSlot;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.VariableSlotLoader;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;
import org.codehaus.groovy.transform.sc.TemporaryVariableExpression;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.groovy.ast.tools.ClassNodeUtils.samePackageName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.classgen.AsmClassGenerator.isNullConstant;
import static org.codehaus.groovy.classgen.AsmClassGenerator.isSuperExpression;
import static org.codehaus.groovy.classgen.AsmClassGenerator.isThisExpression;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.missesGenericsTypes;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StaticInvocationWriter extends InvocationWriter {

    private static final ClassNode INVOKERHELPER_CLASSNODE = ClassHelper.make(InvokerHelper.class);
    private static final MethodNode INVOKERHELPER_INVOKEMETHOD = INVOKERHELPER_CLASSNODE.getMethod(
            "invokeMethodSafe",
            new Parameter[]{
                    new Parameter(ClassHelper.OBJECT_TYPE, "object"),
                    new Parameter(ClassHelper.STRING_TYPE, "name"),
                    new Parameter(ClassHelper.OBJECT_TYPE, "args")
            }
    );
    private static final MethodNode INVOKERHELPER_INVOKESTATICMETHOD = INVOKERHELPER_CLASSNODE.getMethod(
            "invokeStaticMethod",
            new Parameter[]{
                    new Parameter(ClassHelper.CLASS_Type, "clazz"),
                    new Parameter(ClassHelper.STRING_TYPE, "name"),
                    new Parameter(ClassHelper.OBJECT_TYPE, "args")
            }
    );

    private final AtomicInteger labelCounter = new AtomicInteger();

    final WriterController controller;

    private MethodCallExpression currentCall;

    public StaticInvocationWriter(final WriterController wc) {
        super(wc);
        controller = wc;
    }

    @Override
    protected boolean makeDirectCall(final Expression origin, final Expression receiver, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean implicitThis, final boolean containsSpreadExpression) {
        if (origin instanceof MethodCallExpression && isSuperExpression(receiver)) {
            ClassNode superClass = receiver.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
            if (superClass != null && !controller.getCompileStack().isLHS()) {
                // GROOVY-7300
                MethodCallExpression mce = (MethodCallExpression) origin;
                MethodNode node = superClass.getDeclaredMethod(mce.getMethodAsString(), Parameter.EMPTY_ARRAY);
                mce.setMethodTarget(node);
            }
        }
        return super.makeDirectCall(origin, receiver, message, arguments, adapter, implicitThis, containsSpreadExpression);
    }

    @Override
    public void writeInvokeMethod(final MethodCallExpression call) {
        MethodCallExpression old = currentCall;
        currentCall = call;
        super.writeInvokeMethod(call);
        currentCall = old;
    }

    @Override
    public void writeInvokeConstructor(final ConstructorCallExpression call) {
        MethodNode mn = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mn == null) {
            super.writeInvokeConstructor(call);
            return;
        }
        if (writeAICCall(call)) return;
        ConstructorNode cn;
        if (mn instanceof ConstructorNode) {
            cn = (ConstructorNode) mn;
        } else {
            cn = new ConstructorNode(mn.getModifiers(), mn.getParameters(), mn.getExceptions(), mn.getCode());
            cn.setDeclaringClass(mn.getDeclaringClass());
        }
        TupleExpression args = makeArgumentList(call.getArguments());
        if (cn.isPrivate()) {
            ClassNode classNode = controller.getClassNode();
            ClassNode declaringClass = cn.getDeclaringClass();
            if (declaringClass != classNode) {
                MethodNode bridge = null;
                if (call.getNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS) != null) {
                    Map<MethodNode, MethodNode> bridgeMethods = declaringClass.getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS);
                    bridge = bridgeMethods != null ? bridgeMethods.get(cn) : null;
                }
                if (bridge instanceof ConstructorNode) {
                    ArgumentListExpression newArgs = args(nullX());
                    for (Expression arg: args) {
                        newArgs.addExpression(arg);
                    }
                    cn = (ConstructorNode) bridge;
                    args = newArgs;
                } else {
                    controller.getSourceUnit().addError(new SyntaxException("Cannot call private constructor for " + declaringClass.toString(false) +
                            " from class " + classNode.toString(false), call.getLineNumber(), call.getColumnNumber(), mn.getLastLineNumber(), call.getLastColumnNumber()));
                }
            }
        }

        String ownerDescriptor = prepareConstructorCall(cn);
        int before = controller.getOperandStack().getStackLength();
        loadArguments(args.getExpressions(), cn.getParameters());
        finnishConstructorCall(cn, ownerDescriptor, controller.getOperandStack().getStackLength() - before);
    }

    @Override
    public void writeSpecialConstructorCall(final ConstructorCallExpression call) {
        MethodNode mn = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mn == null) {
            super.writeSpecialConstructorCall(call);
            return;
        }
        controller.getCompileStack().pushInSpecialConstructorCall();
        ConstructorNode cn;
        if (mn instanceof ConstructorNode) {
            cn = (ConstructorNode) mn;
        } else {
            cn = new ConstructorNode(mn.getModifiers(), mn.getParameters(), mn.getExceptions(), mn.getCode());
            cn.setDeclaringClass(mn.getDeclaringClass());
        }
        // load "this"
        controller.getMethodVisitor().visitVarInsn(ALOAD, 0);
        String ownerDescriptor = BytecodeHelper.getClassInternalName(cn.getDeclaringClass());
        TupleExpression args = makeArgumentList(call.getArguments());
        int before = controller.getOperandStack().getStackLength();
        loadArguments(args.getExpressions(), cn.getParameters());
        finnishConstructorCall(cn, ownerDescriptor, controller.getOperandStack().getStackLength() - before);
        // on a special call, there's no object on stack
        controller.getOperandStack().remove(1);
        controller.getCompileStack().pop();
    }

    /**
     * Attempts to make a direct method call on a bridge method, if it exists.
     */
    @Deprecated
    protected boolean tryBridgeMethod(final MethodNode target, final Expression receiver, final boolean implicitThis, final TupleExpression args) {
        return tryBridgeMethod(target, receiver, implicitThis, args, null);
    }

    /**
     * Attempts to make a direct method call on a bridge method, if it exists.
     */
    protected boolean tryBridgeMethod(final MethodNode target, final Expression receiver, final boolean implicitThis, final TupleExpression args, final ClassNode thisClass) {
        ClassNode lookupClassNode;
        if (target.isProtected()) {
            lookupClassNode = controller.getClassNode();
            while (lookupClassNode != null && !lookupClassNode.isDerivedFrom(target.getDeclaringClass())) {
                lookupClassNode = lookupClassNode.getOuterClass();
            }
            if (lookupClassNode == null) {
                return false;
            }
        } else {
            lookupClassNode = target.getDeclaringClass().redirect();
        }
        Map<MethodNode, MethodNode> bridges = lookupClassNode.getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS);
        MethodNode bridge = bridges == null ? null : bridges.get(target);
        if (bridge != null) {
            Expression fixedReceiver = receiver;
            if (implicitThis) {
                if (!controller.isInGeneratedFunction()) {
                    fixedReceiver = propX(classX(lookupClassNode), "this");
                } else if (thisClass != null) {
                    ClassNode current = thisClass.getOuterClass();
                    fixedReceiver = varX("thisObject", current);
                    // adjust for multiple levels of nesting if needed
                    while (current.getOuterClass() != null && !lookupClassNode.equals(current)) {
                        FieldNode thisField = current.getField("this$0");
                        current = current.getOuterClass();
                        if (thisField != null) {
                            fixedReceiver = propX(fixedReceiver, "this$0");
                            fixedReceiver.setType(current);
                        }
                    }
                }
            }
            ArgumentListExpression newArgs = args(target.isStatic() ? nullX() : fixedReceiver);
            for (Expression expression : args.getExpressions()) {
                newArgs.addExpression(expression);
            }
            return writeDirectMethodCall(bridge, implicitThis, fixedReceiver, newArgs);
        }
        return false;
    }

    @Override
    protected boolean writeDirectMethodCall(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args) {
        if (target == null) return false;

        if (target instanceof ExtensionMethodNode) {
            ExtensionMethodNode emn = (ExtensionMethodNode) target;
            MethodVisitor mv = controller.getMethodVisitor();
            MethodNode node = emn.getExtensionMethodNode();
            Parameter[] parameters = node.getParameters();
            ClassNode returnType = node.getReturnType();

            List<Expression> argumentList = new ArrayList<>();
            if (emn.isStaticExtension()) {
                argumentList.add(nullX());
            } else {
                boolean isThisOrSuper = isThisExpression(receiver) || isSuperExpression(receiver);
                ClassNode classNode = controller.getClassNode();
                Expression fixedReceiver = null;
                if (isThisOrSuper && classNode.getOuterClass() != null && controller.isInGeneratedFunction()) {
                    ClassNode current = classNode.getOuterClass();
                    fixedReceiver = varX("thisObject", current);
                    // adjust for multiple levels of nesting if needed
                    while (current.getOuterClass() != null && !classNode.equals(current)) {
                        FieldNode thisField = current.getField("this$0");
                        current = current.getOuterClass();
                        if (thisField != null) {
                            fixedReceiver = propX(fixedReceiver, "this$0");
                            fixedReceiver.setType(current);
                        }
                    }
                }
                argumentList.add(fixedReceiver != null ? fixedReceiver : receiver);
            }
            argumentList.addAll(args.getExpressions());
            loadArguments(argumentList, parameters);

            String owner = BytecodeHelper.getClassInternalName(node.getDeclaringClass());
            String desc = BytecodeHelper.getMethodDescriptor(returnType, parameters);
            mv.visitMethodInsn(INVOKESTATIC, owner, target.getName(), desc, false);
            controller.getOperandStack().remove(argumentList.size());

            if (ClassHelper.VOID_TYPE.equals(returnType)) {
                returnType = ClassHelper.OBJECT_TYPE;
                mv.visitInsn(ACONST_NULL);
            } else if (missesGenericsTypes(returnType)) {
                returnType = returnType.redirect();
            }
            controller.getOperandStack().push(returnType);
            return true;
        } else {
            if (target == StaticTypeCheckingVisitor.CLOSURE_CALL_VARGS) {
                // wrap arguments into an array
                ArrayExpression arr = new ArrayExpression(ClassHelper.OBJECT_TYPE, args.getExpressions());
                return super.writeDirectMethodCall(target, implicitThis, receiver, args(arr));
            }
            ClassNode classNode = controller.getClassNode();
            if (classNode.isDerivedFrom(ClassHelper.CLOSURE_TYPE)
                    && controller.isInGeneratedFunction()
                    && !target.isPublic()
                    && target.getDeclaringClass() != classNode) {
                if (!tryBridgeMethod(target, receiver, implicitThis, args, classNode)) {
                    // replace call with an invoker helper call
                    ArrayExpression arr = new ArrayExpression(ClassHelper.OBJECT_TYPE, args.getExpressions());
                    MethodCallExpression mce = callX(
                            classX(INVOKERHELPER_CLASSNODE),
                            target.isStatic() ? "invokeStaticMethod" : "invokeMethodSafe",
                            args(
                                    target.isStatic() ? classX(target.getDeclaringClass()) : receiver,
                                    constX(target.getName()),
                                    arr
                            )
                    );
                    mce.setMethodTarget(target.isStatic() ? INVOKERHELPER_INVOKESTATICMETHOD : INVOKERHELPER_INVOKEMETHOD);
                    mce.visit(controller.getAcg());
                    return true;
                }
                return true;
            }
            Expression fixedReceiver = null;
            boolean fixedImplicitThis = implicitThis;
            if (target.isPrivate()) {
                if (tryPrivateMethod(target, implicitThis, receiver, args, classNode)) return true;
            } else if (target.isProtected()) {
                ClassNode node = receiver == null ? ClassHelper.OBJECT_TYPE : controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
                if (!implicitThis && !isThisExpression(receiver) && !isSuperExpression(receiver) && !samePackageName(node, classNode)
                        && StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(node,target.getDeclaringClass())) {
                    ASTNode src = receiver == null ? args : receiver;
                    controller.getSourceUnit().addError(
                            new SyntaxException("Method " + target.getName() + " is protected in " + target.getDeclaringClass().toString(false),
                                    src.getLineNumber(), src.getColumnNumber(), src.getLastLineNumber(), src.getLastColumnNumber()));
                } else if (!node.isDerivedFrom(target.getDeclaringClass()) && tryBridgeMethod(target, receiver, implicitThis, args, classNode)) {
                    return true;
                }
            } else if (target.isPublic() && receiver != null) {
                if (implicitThis
                        && !classNode.isDerivedFrom(target.getDeclaringClass())
                        && !classNode.implementsInterface(target.getDeclaringClass())
                        && classNode.getOuterClass() != null && controller.isInGeneratedFunction()) {
                    ClassNode current = classNode.getOuterClass();
                    fixedReceiver = varX("thisObject", current);
                    // adjust for multiple levels of nesting if needed
                    while (current.getOuterClass() != null && !target.getDeclaringClass().equals(current)) {
                        FieldNode thisField = current.getField("this$0");
                        current = current.getOuterClass();
                        if (thisField != null) {
                            fixedReceiver = propX(fixedReceiver, "this$0");
                            fixedReceiver.setType(current);
                            fixedImplicitThis = false;
                        }
                    }
                }
            }
            if (receiver != null && !isSuperExpression(receiver)) {
                // in order to avoid calls to castToType, which is the dynamic behaviour, we make sure that we call CHECKCAST instead then replace the top operand type
                return super.writeDirectMethodCall(target, fixedImplicitThis, new CheckcastReceiverExpression(fixedReceiver != null ? fixedReceiver : receiver, target), args);
            }
            return super.writeDirectMethodCall(target, implicitThis, receiver, args);
        }
    }

    private boolean tryPrivateMethod(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args, final ClassNode classNode) {
        ClassNode declaringClass = target.getDeclaringClass();
        if ((isPrivateBridgeMethodsCallAllowed(declaringClass, classNode) || isPrivateBridgeMethodsCallAllowed(classNode, declaringClass))
                && declaringClass.getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS) != null
                && !declaringClass.equals(classNode)) {
            if (tryBridgeMethod(target, receiver, implicitThis, args, classNode)) {
                return true;
            } else {
                checkAndAddCannotCallPrivateMethodError(target, receiver, classNode, declaringClass);
            }
        }
        checkAndAddCannotCallPrivateMethodError(target, receiver, classNode, declaringClass);
        return false;
    }

    private void checkAndAddCannotCallPrivateMethodError(final MethodNode target, final Expression receiver, final ClassNode classNode, final ClassNode declaringClass) {
        if (declaringClass != classNode) {
            controller.getSourceUnit().addError(new SyntaxException("Cannot call private method " + (target.isStatic() ? "static " : "") +
                    declaringClass.toString(false) + "#" + target.getName() + " from class " + classNode.toString(false), receiver.getLineNumber(), receiver.getColumnNumber(), receiver.getLastLineNumber(), receiver.getLastColumnNumber()));
        }
    }

    protected static boolean isPrivateBridgeMethodsCallAllowed(final ClassNode receiver, final ClassNode caller) {
        if (receiver == null) return false;
        if (receiver.redirect() == caller) return true;
        if (isPrivateBridgeMethodsCallAllowed(receiver.getOuterClass(), caller)) return true;
        if (caller.getOuterClass() != null && isPrivateBridgeMethodsCallAllowed(receiver, caller.getOuterClass())) return true;
        return false;
    }

    protected void loadArguments(final List<Expression> argumentList, final Parameter[] para) {
        if (para.length == 0) return;
        ClassNode lastParaType = para[para.length - 1].getOriginType();
        AsmClassGenerator acg = controller.getAcg();
        TypeChooser typeChooser = controller.getTypeChooser();
        OperandStack operandStack = controller.getOperandStack();
        int argumentListSize = argumentList.size();
        ClassNode lastArgType = argumentListSize > 0 ?
                typeChooser.resolveType(argumentList.get(argumentListSize -1), controller.getClassNode()) : null;
        if (lastParaType.isArray()
                && ((argumentListSize > para.length)
                || ((argumentListSize == (para.length - 1)) && !lastParaType.equals(lastArgType))
                || ((argumentListSize == para.length && lastArgType!=null && !lastArgType.isArray())
                    && (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(lastArgType,lastParaType.getComponentType())))
                        || ClassHelper.GSTRING_TYPE.equals(lastArgType) && ClassHelper.STRING_TYPE.equals(lastParaType.getComponentType()))
                ) {
            int stackLen = operandStack.getStackLength() + argumentListSize;
            MethodVisitor mv = controller.getMethodVisitor();
            controller.setMethodVisitor(mv);
            // varg call
            // first parameters as usual
            for (int i = 0; i < para.length - 1; i += 1) {
                visitArgument(argumentList.get(i), para[i].getType());
            }
            // last parameters wrapped in an array
            List<Expression> lastParams = new ArrayList<>();
            for (int i = para.length - 1; i < argumentListSize; i += 1) {
                lastParams.add(argumentList.get(i));
            }
            ArrayExpression array = new ArrayExpression(lastParaType.getComponentType(), lastParams);
            array.visit(acg);
            // adjust stack length
            while (operandStack.getStackLength() < stackLen) {
                operandStack.push(ClassHelper.OBJECT_TYPE);
            }
            if (argumentListSize == para.length - 1) {
                operandStack.remove(1);
            }
        } else if (argumentListSize == para.length) {
            for (int i = 0; i < argumentListSize; i++) {
                visitArgument(argumentList.get(i), para[i].getType());
            }
        } else {
            // method call with default arguments
            ClassNode classNode = controller.getClassNode();
            Expression[] arguments = new Expression[para.length];
            for (int i = 0, j = 0, n = para.length; i < n; i += 1) {
                Parameter curParam = para[i];
                ClassNode curParamType = curParam.getType();
                Expression curArg = j < argumentListSize ? argumentList.get(j) : null;
                Expression initialExpression = curParam.getNodeMetaData(StaticTypesMarker.INITIAL_EXPRESSION);
                if (initialExpression == null && curParam.hasInitialExpression())
                    initialExpression = curParam.getInitialExpression();
                if (initialExpression == null && curParam.getNodeMetaData(Verifier.INITIAL_EXPRESSION) != null) {
                    initialExpression = curParam.getNodeMetaData(Verifier.INITIAL_EXPRESSION);
                }
                ClassNode curArgType = curArg == null ? null : typeChooser.resolveType(curArg, classNode);

                if (initialExpression != null && !compatibleArgumentType(curArgType, curParamType)) {
                    // use default expression
                    arguments[i] = initialExpression;
                } else {
                    arguments[i] = curArg;
                    j += 1;
                }
            }
            for (int i = 0, n = arguments.length; i < n; i += 1) {
                visitArgument(arguments[i], para[i].getType());
            }
        }
    }

    private void visitArgument(final Expression argumentExpr, final ClassNode parameterType) {
        argumentExpr.putNodeMetaData(StaticTypesMarker.PARAMETER_TYPE, parameterType);
        argumentExpr.visit(controller.getAcg());
        if (!isNullConstant(argumentExpr)) {
            controller.getOperandStack().doGroovyCast(parameterType);
        }
    }

    private boolean compatibleArgumentType(final ClassNode argumentType, final ClassNode paramType) {
        if (argumentType == null) return false;
        if (ClassHelper.getWrapper(argumentType).equals(ClassHelper.getWrapper(paramType))) return true;
        if (paramType.isInterface()) return argumentType.implementsInterface(paramType);
        if (paramType.isArray() && argumentType.isArray())
            return compatibleArgumentType(argumentType.getComponentType(), paramType.getComponentType());
        return ClassHelper.getWrapper(argumentType).isDerivedFrom(ClassHelper.getWrapper(paramType));
    }

    @Override
    public void makeCall(final Expression origin, final Expression receiver, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe, final boolean implicitThis) {
        ClassNode dynamicCallReturnType = origin.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION);
        if (dynamicCallReturnType != null) {
            StaticTypesWriterController staticController = (StaticTypesWriterController) controller;
            if (origin instanceof MethodCallExpression) {
                ((MethodCallExpression) origin).setMethodTarget(null);
            }
            InvocationWriter dynamicInvocationWriter = staticController.getRegularInvocationWriter();
            dynamicInvocationWriter.makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
            return;
        }
        if (implicitThis && tryImplicitReceiver(origin, message, arguments, adapter, safe, spreadSafe)) {
            return;
        }
        // if call is spread safe, replace it with a for in loop
        if (spreadSafe && origin instanceof MethodCallExpression) {
            // receiver expressions with side effects should not be visited twice, avoid by using a temporary variable
            Expression tmpReceiver = receiver;
            if (!(receiver instanceof VariableExpression) && !(receiver instanceof ConstantExpression)) {
                tmpReceiver = new TemporaryVariableExpression(receiver);
            }
            MethodVisitor mv = controller.getMethodVisitor();
            CompileStack compileStack = controller.getCompileStack();
            TypeChooser typeChooser = controller.getTypeChooser();
            OperandStack operandStack = controller.getOperandStack();
            ClassNode classNode = controller.getClassNode();
            int counter = labelCounter.incrementAndGet();

            // use a temporary variable for the arraylist in which the results of the spread call will be stored
            ConstructorCallExpression cce = ctorX(StaticCompilationVisitor.ARRAYLIST_CLASSNODE);
            cce.setNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, StaticCompilationVisitor.ARRAYLIST_CONSTRUCTOR);
            TemporaryVariableExpression result = new TemporaryVariableExpression(cce);
            result.visit(controller.getAcg());
            operandStack.pop();
            // if (receiver != null)
            tmpReceiver.visit(controller.getAcg());
            Label ifnull = compileStack.createLocalLabel("ifnull_" + counter);
            mv.visitJumpInsn(IFNULL, ifnull);
            operandStack.remove(1); // receiver consumed by if()
            Label nonull = compileStack.createLocalLabel("nonull_" + counter);
            mv.visitLabel(nonull);
            ClassNode componentType = StaticTypeCheckingVisitor.inferLoopElementType(typeChooser.resolveType(tmpReceiver, classNode));
            Parameter iterator = new Parameter(componentType, "for$it$" + counter);
            VariableExpression iteratorAsVar = varX(iterator);
            MethodCallExpression origMCE = (MethodCallExpression) origin;
            MethodCallExpression newMCE = callX(
                    iteratorAsVar,
                    origMCE.getMethodAsString(),
                    origMCE.getArguments()
            );
            newMCE.setImplicitThis(false);
            newMCE.setMethodTarget(origMCE.getMethodTarget());
            newMCE.setSafe(true);
            MethodCallExpression add = callX(
                    result,
                    "add",
                    newMCE
            );
            add.setImplicitThis(false);
            add.setMethodTarget(StaticCompilationVisitor.ARRAYLIST_ADD_METHOD);
            // for (e in receiver) { result.add(e?.method(arguments) }
            ForStatement stmt = new ForStatement(
                    iterator,
                    tmpReceiver,
                    stmt(add)
            );
            stmt.visit(controller.getAcg());
            // else { empty list }
            mv.visitLabel(ifnull);

            // end of if/else
            // return result list
            result.visit(controller.getAcg());

            // cleanup temporary variables
            if (tmpReceiver instanceof TemporaryVariableExpression) {
                ((TemporaryVariableExpression) tmpReceiver).remove(controller);
            }
            result.remove(controller);
        } else if (safe && origin instanceof MethodCallExpression) {
            // wrap call in an IFNULL check
            MethodVisitor mv = controller.getMethodVisitor();
            CompileStack compileStack = controller.getCompileStack();
            OperandStack operandStack = controller.getOperandStack();
            int counter = labelCounter.incrementAndGet();
            // if (receiver != null)
            ExpressionAsVariableSlot slot = new ExpressionAsVariableSlot(controller, receiver);
            slot.visit(controller.getAcg());
            operandStack.box();
            Label ifnull = compileStack.createLocalLabel("ifnull_" + counter);
            mv.visitJumpInsn(IFNULL, ifnull);
            operandStack.remove(1); // receiver consumed by if()
            Label nonull = compileStack.createLocalLabel("nonull_" + counter);
            mv.visitLabel(nonull);
            MethodCallExpression origMCE = (MethodCallExpression) origin;
            MethodCallExpression newMCE = callX(
                    new VariableSlotLoader(slot.getType(), slot.getIndex(), controller.getOperandStack()),
                    origMCE.getMethodAsString(),
                    origMCE.getArguments()
            );
            MethodNode methodTarget = origMCE.getMethodTarget();
            newMCE.setMethodTarget(methodTarget);
            newMCE.setSafe(false);
            newMCE.setImplicitThis(origMCE.isImplicitThis());
            newMCE.setSourcePosition(origMCE);
            newMCE.visit(controller.getAcg());
            compileStack.removeVar(slot.getIndex());
            ClassNode returnType = operandStack.getTopOperand();
            if (ClassHelper.isPrimitiveType(returnType) && !ClassHelper.VOID_TYPE.equals(returnType)) {
                operandStack.box();
            }
            Label endof = compileStack.createLocalLabel("endof_" + counter);
            mv.visitJumpInsn(GOTO, endof);
            mv.visitLabel(ifnull);
            // else { null }
            mv.visitInsn(ACONST_NULL);
            mv.visitLabel(endof);
        } else {
            if (origin instanceof AttributeExpression && (adapter == AsmClassGenerator.getField || adapter == AsmClassGenerator.getGroovyObjectField)) {
                CallSiteWriter callSiteWriter = controller.getCallSiteWriter();
                String fieldName = ((AttributeExpression) origin).getPropertyAsString();
                if (fieldName != null && callSiteWriter instanceof StaticTypesCallSiteWriter) {
                    ClassNode receiverType = controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
                    if (((StaticTypesCallSiteWriter) callSiteWriter).makeGetField(receiver, receiverType, fieldName, safe, false)) {
                        return;
                    }
                }
            }
            super.makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
        }
    }

    private boolean tryImplicitReceiver(final Expression origin, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe) {
        Object implicitReceiver = origin.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        if (implicitReceiver == null && origin instanceof MethodCallExpression) {
            implicitReceiver = ((MethodCallExpression) origin).getObjectExpression().getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        }
        if (implicitReceiver != null) {
            String[] path = ((String) implicitReceiver).split("\\.");
            // GROOVY-6021
            PropertyExpression pexp = propX(varX("this", ClassHelper.CLOSURE_TYPE), path[0]);
            pexp.setImplicitThis(true);
            for (int i = 1, n = path.length; i < n; i += 1) {
                pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.CLOSURE_TYPE);
                pexp = propX(pexp, path[i]);
            }
            pexp.putNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER, implicitReceiver);
            origin.removeNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
            if (origin instanceof PropertyExpression) {
                PropertyExpression rewritten = propX(pexp, ((PropertyExpression) origin).getProperty(), ((PropertyExpression) origin).isSafe());
                rewritten.setSpreadSafe(((PropertyExpression) origin).isSpreadSafe());
                rewritten.visit(controller.getAcg());

                rewritten.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, origin.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE));
            } else {
                makeCall(origin, pexp, message, arguments, adapter, safe, spreadSafe, false);
            }
            return true;
        }
        return false;
    }

    private class CheckcastReceiverExpression extends Expression {
        private final Expression receiver;
        private final MethodNode target;

        private ClassNode resolvedType;

        public CheckcastReceiverExpression(final Expression receiver, final MethodNode target) {
            this.receiver = receiver;
            this.target = target;
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            return this;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            receiver.visit(visitor);
            if (visitor instanceof AsmClassGenerator) {
                ClassNode topOperand = controller.getOperandStack().getTopOperand();
                ClassNode type = getType();
                if (ClassHelper.GSTRING_TYPE.equals(topOperand) && ClassHelper.STRING_TYPE.equals(type)) {
                    // perform regular type conversion
                    controller.getOperandStack().doGroovyCast(type);
                    return;
                }
                if (ClassHelper.isPrimitiveType(topOperand) && !ClassHelper.isPrimitiveType(type)) {
                    controller.getOperandStack().box();
                } else if (!ClassHelper.isPrimitiveType(topOperand) && ClassHelper.isPrimitiveType(type)) {
                    controller.getOperandStack().doGroovyCast(type);
                }
                if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(topOperand, type)) return;
                controller.getMethodVisitor().visitTypeInsn(CHECKCAST, type.isArray()
                        ? BytecodeHelper.getTypeDescription(type)
                        : BytecodeHelper.getClassInternalName(type.getName()));
                controller.getOperandStack().replace(type);
            }
        }

        @Override
        public ClassNode getType() {
            if (resolvedType != null) {
                return resolvedType;
            }
            ClassNode type;
            if (target instanceof ExtensionMethodNode) {
                type = ((ExtensionMethodNode) target).getExtensionMethodNode().getDeclaringClass();
            } else {
                type = ClassHelper.getWrapper(controller.getTypeChooser().resolveType(receiver, controller.getClassNode()));
                ClassNode declaringClass = target.getDeclaringClass();
                if (type.getClass() != ClassNode.class
                        && type.getClass() != InnerClassNode.class
                        && type.getClass() != DecompiledClassNode.class) {
                    type = declaringClass; // ex: LUB type
                }
                if (ClassHelper.OBJECT_TYPE.equals(type) && !ClassHelper.OBJECT_TYPE.equals(declaringClass)) {
                    // can happen for compiler rewritten code, where type information is missing
                    type = declaringClass;
                }
                if (ClassHelper.OBJECT_TYPE.equals(declaringClass)) {
                    // check cast not necessary because Object never evolves
                    // and it prevents a potential ClassCastException if the delegate of a closure
                    // is changed in a statically compiled closure
                    type = ClassHelper.OBJECT_TYPE;
                }
            }
            resolvedType = type;
            return type;
        }
    }

    public MethodCallExpression getCurrentCall() {
        return currentCall;
    }

    @Override
    protected boolean makeCachedCall(final Expression origin, final ClassExpression sender, final Expression receiver, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe, final boolean implicitThis, final boolean containsSpreadExpression) {
        return false;
    }
}
