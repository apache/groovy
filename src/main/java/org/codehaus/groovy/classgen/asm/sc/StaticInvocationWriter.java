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
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.groovy.ast.tools.ClassNodeUtils.samePackageName;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StaticInvocationWriter extends InvocationWriter {
    private static final ClassNode INVOKERHELPER_CLASSNODE = ClassHelper.make(InvokerHelper.class);
    private static final Expression INVOKERHELER_RECEIVER = new ClassExpression(INVOKERHELPER_CLASSNODE);
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

    public StaticInvocationWriter(WriterController wc) {
        super(wc);
        controller = wc;
    }

    @Override
    protected boolean makeDirectCall(final Expression origin, final Expression receiver, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean implicitThis, final boolean containsSpreadExpression) {
        if (origin instanceof MethodCallExpression &&
                receiver instanceof VariableExpression &&
                ((VariableExpression) receiver).isSuperExpression()) {
            ClassNode superClass = receiver.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
            if (superClass!=null && !controller.getCompileStack().isLHS()) {
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
                    ArgumentListExpression newArgs = new ArgumentListExpression(new ConstantExpression(null));
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
        if (mn==null) {
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
    protected boolean tryBridgeMethod(MethodNode target, Expression receiver, boolean implicitThis, TupleExpression args) {
        return tryBridgeMethod(target, receiver, implicitThis, args, null);
    }

    /**
     * Attempts to make a direct method call on a bridge method, if it exists.
     */
    protected boolean tryBridgeMethod(MethodNode target, Expression receiver, boolean implicitThis,
                                      TupleExpression args, ClassNode thisClass) {
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
        Map<MethodNode, MethodNode> bridges = lookupClassNode.getNodeMetaData(PRIVATE_BRIDGE_METHODS);
        MethodNode bridge = bridges==null?null:bridges.get(target);
        if (bridge != null) {
            Expression fixedReceiver = receiver;
            if (implicitThis) {
                if (!controller.isInClosure()) {
                    fixedReceiver = new PropertyExpression(new ClassExpression(lookupClassNode), "this");
                } else if (thisClass != null) {
                    ClassNode current = thisClass.getOuterClass();
                    fixedReceiver = new VariableExpression("thisObject", current);
                    // adjust for multiple levels of nesting if needed
                    while (current instanceof InnerClassNode && !lookupClassNode.equals(current)) {
                        FieldNode thisField = current.getField("this$0");
                        current = current.getOuterClass();
                        if (thisField != null) {
                            fixedReceiver = new PropertyExpression(fixedReceiver, "this$0");
                            fixedReceiver.setType(current);
                        }
                    }
                }
            }
            ArgumentListExpression newArgs = new ArgumentListExpression(target.isStatic() ? nullX() : fixedReceiver);
            for (Expression expression : args.getExpressions()) {
                newArgs.addExpression(expression);
            }
            return writeDirectMethodCall(bridge, implicitThis, fixedReceiver, newArgs);
        }
        return false;
    }

    @Override
    protected boolean writeDirectMethodCall(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args) {
        if (target==null) return false;

        if (target instanceof ExtensionMethodNode) {
            ExtensionMethodNode emn = (ExtensionMethodNode) target;
            MethodNode node = emn.getExtensionMethodNode();
            String methodName = target.getName();

            MethodVisitor mv = controller.getMethodVisitor();
            int argumentsToRemove = 0;
            List<Expression> argumentList = new LinkedList<Expression>(args.getExpressions());

            if (emn.isStaticExtension()) {
                // it's a static extension method
                argumentList.add(0, new ConstantExpression(null));
            } else {
                ClassNode classNode = controller.getClassNode();
                boolean isThisOrSuper = false;
                if (receiver instanceof VariableExpression) {
                    isThisOrSuper = ((VariableExpression) receiver).isThisExpression() || ((VariableExpression) receiver).isSuperExpression();
                }
                Expression fixedReceiver = null;
                if (isThisOrSuper && classNode instanceof InnerClassNode && controller.isInClosure()) {
                    ClassNode current = classNode.getOuterClass();
                    fixedReceiver = new VariableExpression("thisObject", current);
                    // adjust for multiple levels of nesting if needed
                    while (current instanceof InnerClassNode && !classNode.equals(current)) {
                        FieldNode thisField = current.getField("this$0");
                        current = current.getOuterClass();
                        if (thisField != null) {
                            fixedReceiver = new PropertyExpression(fixedReceiver, "this$0");
                            fixedReceiver.setType(current);
                        }
                    }
                }

                argumentList.add(0, fixedReceiver != null ? fixedReceiver : receiver);
            }

            Parameter[] parameters = node.getParameters();
            loadArguments(argumentList, parameters);

            String owner = BytecodeHelper.getClassInternalName(node.getDeclaringClass());
            String desc = BytecodeHelper.getMethodDescriptor(target.getReturnType(), parameters);
            mv.visitMethodInsn(INVOKESTATIC, owner, methodName, desc, false);
            ClassNode ret = target.getReturnType().redirect();
            if (ret == ClassHelper.VOID_TYPE) {
                ret = ClassHelper.OBJECT_TYPE;
                mv.visitInsn(ACONST_NULL);
            }
            argumentsToRemove += argumentList.size();
            controller.getOperandStack().remove(argumentsToRemove);
            controller.getOperandStack().push(ret);
            return true;
        } else {
            if (target == StaticTypeCheckingVisitor.CLOSURE_CALL_VARGS) {
                // wrap arguments into an array
                ArrayExpression arr = new ArrayExpression(ClassHelper.OBJECT_TYPE, args.getExpressions());
                return super.writeDirectMethodCall(target, implicitThis, receiver, new ArgumentListExpression(arr));
            }
            ClassNode classNode = controller.getClassNode();
            if (classNode.isDerivedFrom(ClassHelper.CLOSURE_TYPE)
                    && controller.isInClosure()
                    && !target.isPublic()
                    && target.getDeclaringClass() != classNode) {
                if (!tryBridgeMethod(target, receiver, implicitThis, args, classNode)) {
                    // replace call with an invoker helper call
                    ArrayExpression arr = new ArrayExpression(ClassHelper.OBJECT_TYPE, args.getExpressions());
                    MethodCallExpression mce = new MethodCallExpression(
                            INVOKERHELER_RECEIVER,
                            target.isStatic() ? "invokeStaticMethod" : "invokeMethodSafe",
                            new ArgumentListExpression(
                                    target.isStatic() ?
                                            new ClassExpression(target.getDeclaringClass()) :
                                            receiver,
                                    new ConstantExpression(target.getName()),
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
                ClassNode node = receiver==null?ClassHelper.OBJECT_TYPE:controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
                boolean isThisOrSuper = false;
                if (receiver instanceof VariableExpression) {
                    isThisOrSuper = ((VariableExpression) receiver).isThisExpression() || ((VariableExpression) receiver).isSuperExpression();
                }
                if (!implicitThis && !isThisOrSuper
                        && !samePackageName(node, classNode)
                        && StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(node,target.getDeclaringClass())) {
                    ASTNode src = receiver==null?args:receiver;
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
                        && classNode instanceof InnerClassNode && controller.isInClosure()) {
                    ClassNode current = classNode.getOuterClass();
                    fixedReceiver = new VariableExpression("thisObject", current);
                    // adjust for multiple levels of nesting if needed
                    while (current instanceof InnerClassNode && !target.getDeclaringClass().equals(current)) {
                        FieldNode thisField = current.getField("this$0");
                        current = current.getOuterClass();
                        if (thisField != null) {
                            fixedReceiver = new PropertyExpression(fixedReceiver, "this$0");
                            fixedReceiver.setType(current);
                            fixedImplicitThis = false;
                        }
                    }
                }
            }
            if (receiver != null) {
                boolean callToSuper = receiver instanceof VariableExpression && ((VariableExpression) receiver).isSuperExpression();
                if (!callToSuper) {
                    fixedReceiver = fixedReceiver == null ? receiver : fixedReceiver;
                    // in order to avoid calls to castToType, which is the dynamic behaviour, we make sure that we call CHECKCAST instead
                    // then replace the top operand type
                    Expression checkCastReceiver = new CheckcastReceiverExpression(fixedReceiver, target);
                    return super.writeDirectMethodCall(target, fixedImplicitThis, checkCastReceiver, args);
                }
            }
            return super.writeDirectMethodCall(target, implicitThis, receiver, args);
        }
    }

    private boolean tryPrivateMethod(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args, final ClassNode classNode) {
        ClassNode declaringClass = target.getDeclaringClass();
        if ((isPrivateBridgeMethodsCallAllowed(declaringClass, classNode) || isPrivateBridgeMethodsCallAllowed(classNode, declaringClass))
                && declaringClass.getNodeMetaData(PRIVATE_BRIDGE_METHODS) != null
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

    private void checkAndAddCannotCallPrivateMethodError(MethodNode target, Expression receiver, ClassNode classNode, ClassNode declaringClass) {
        if (declaringClass != classNode) {
            controller.getSourceUnit().addError(new SyntaxException("Cannot call private method " + (target.isStatic() ? "static " : "") +
                    declaringClass.toString(false) + "#" + target.getName() + " from class " + classNode.toString(false), receiver.getLineNumber(), receiver.getColumnNumber(), receiver.getLastLineNumber(), receiver.getLastColumnNumber()));
        }
    }

    protected static boolean isPrivateBridgeMethodsCallAllowed(ClassNode receiver, ClassNode caller) {
        if (receiver == null) return false;
        if (receiver.redirect() == caller) return true;
        if (caller.redirect() instanceof InnerClassNode) return
                isPrivateBridgeMethodsCallAllowed(receiver, caller.redirect().getOuterClass()) ||
                        isPrivateBridgeMethodsCallAllowed(receiver.getOuterClass(), caller);
        return false;
    }

    protected void loadArguments(List<Expression> argumentList, Parameter[] para) {
        if (para.length == 0) return;
        ClassNode lastParaType = para[para.length - 1].getOriginType();
        AsmClassGenerator acg = controller.getAcg();
        TypeChooser typeChooser = controller.getTypeChooser();
        OperandStack operandStack = controller.getOperandStack();
        int argumentListSize = argumentList.size();
        ClassNode lastArgType = argumentListSize > 0 ?
                typeChooser.resolveType(argumentList.get(argumentListSize -1), controller.getClassNode()):null;
        if (lastParaType.isArray()
                && ((argumentListSize > para.length)
                || ((argumentListSize == (para.length - 1)) && !lastParaType.equals(lastArgType))
                || ((argumentListSize == para.length && lastArgType!=null && !lastArgType.isArray())
                    && (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(lastArgType,lastParaType.getComponentType())))
                        || ClassHelper.GSTRING_TYPE.equals(lastArgType) && ClassHelper.STRING_TYPE.equals(lastParaType.getComponentType()))
                ) {
            int stackLen = operandStack.getStackLength() + argumentListSize;
            MethodVisitor mv = controller.getMethodVisitor();
            //mv = new org.objectweb.asm.util.TraceMethodVisitor(mv);
            controller.setMethodVisitor(mv);
            // varg call
            // first parameters as usual
            for (int i = 0; i < para.length - 1; i++) {
                Expression expression = argumentList.get(i);
                expression.visit(acg);
                if (!isNullConstant(expression)) {
                    operandStack.doGroovyCast(para[i].getType());
                }
            }
            // last parameters wrapped in an array
            List<Expression> lastParams = new LinkedList<Expression>();
            for (int i = para.length - 1; i < argumentListSize; i++) {
                lastParams.add(argumentList.get(i));
            }
            ArrayExpression array = new ArrayExpression(
                    lastParaType.getComponentType(),
                    lastParams
            );
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
                Expression expression = argumentList.get(i);
                expression.visit(acg);
                if (!isNullConstant(expression)) {
                    operandStack.doGroovyCast(para[i].getType());
                }
            }
        } else {
            // method call with default arguments
            ClassNode classNode = controller.getClassNode();
            Expression[] arguments = new Expression[para.length];
            for (int i = 0, j = 0; i < para.length; i++) {
                Parameter curParam = para[i];
                ClassNode curParamType = curParam.getType();
                Expression curArg = j < argumentListSize ? argumentList.get(j) : null;
                Expression initialExpression = curParam.getNodeMetaData(StaticTypesMarker.INITIAL_EXPRESSION);
                if (initialExpression == null && curParam.hasInitialExpression())
                    initialExpression = curParam.getInitialExpression();
                if (initialExpression == null && curParam.getNodeMetaData(Verifier.INITIAL_EXPRESSION)!=null) {
                    initialExpression = curParam.getNodeMetaData(Verifier.INITIAL_EXPRESSION);
                }
                ClassNode curArgType = curArg == null ? null : typeChooser.resolveType(curArg, classNode);

                if (initialExpression != null && !compatibleArgumentType(curArgType, curParamType)) {
                    // use default expression
                    arguments[i] = initialExpression;
                } else {
                    arguments[i] = curArg;
                    j++;
                }
            }
            for (int i = 0; i < arguments.length; i++) {
                Expression expression = arguments[i];
                expression.visit(acg);
                if (!isNullConstant(expression)) {
                    operandStack.doGroovyCast(para[i].getType());
                }
            }
        }
    }

    private static boolean isNullConstant(final Expression expression) {
        return (expression instanceof ConstantExpression && ((ConstantExpression) expression).getValue() == null);
    }

    private boolean compatibleArgumentType(ClassNode argumentType, ClassNode paramType) {
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
        if (dynamicCallReturnType !=null) {
            StaticTypesWriterController staticController = (StaticTypesWriterController) controller;
            if (origin instanceof MethodCallExpression) {
                ((MethodCallExpression) origin).setMethodTarget(null);
            }
            InvocationWriter dynamicInvocationWriter = staticController.getRegularInvocationWriter();
            dynamicInvocationWriter.
                    makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
            return;
        }
        if (tryImplicitReceiver(origin, message, arguments, adapter, safe, spreadSafe, implicitThis)) {
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
            ConstructorCallExpression cce = new ConstructorCallExpression(StaticCompilationVisitor.ARRAYLIST_CLASSNODE, ArgumentListExpression.EMPTY_ARGUMENTS);
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
            VariableExpression iteratorAsVar = new VariableExpression(iterator);
            MethodCallExpression origMCE = (MethodCallExpression) origin;
            MethodCallExpression newMCE = new MethodCallExpression(
                    iteratorAsVar,
                    origMCE.getMethodAsString(),
                    origMCE.getArguments()
            );
            newMCE.setImplicitThis(false);
            newMCE.setMethodTarget(origMCE.getMethodTarget());
            newMCE.setSafe(true);
            MethodCallExpression add = new MethodCallExpression(
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
                    new ExpressionStatement(add)
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
            MethodCallExpression newMCE = new MethodCallExpression(
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
            if ((adapter == AsmClassGenerator.getGroovyObjectField
                    || adapter == AsmClassGenerator.getField ) && origin instanceof AttributeExpression) {
                String pname = ((PropertyExpression) origin).getPropertyAsString();
                CallSiteWriter callSiteWriter = controller.getCallSiteWriter();
                if (pname!=null && callSiteWriter instanceof StaticTypesCallSiteWriter) {
                    StaticTypesCallSiteWriter stcsw = (StaticTypesCallSiteWriter) callSiteWriter;
                    TypeChooser typeChooser = controller.getTypeChooser();
                    if (stcsw.makeGetField(receiver, typeChooser.resolveType(receiver, controller.getClassNode()), pname, safe, false)) {
                        return;
                    }
                }
            }
            super.makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
        }
    }

    boolean tryImplicitReceiver(final Expression origin, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe, final boolean implicitThis) {
        Object implicitReceiver = origin.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        if (implicitThis && implicitReceiver==null && origin instanceof MethodCallExpression) {
            implicitReceiver = ((MethodCallExpression) origin).getObjectExpression().getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        }
        if (implicitThis && implicitReceiver != null) {
            String[] propertyPath = ((String) implicitReceiver).split("\\.");
            // GROOVY-6021
            PropertyExpression pexp = new PropertyExpression(new VariableExpression("this", CLOSURE_TYPE), propertyPath[0]);
            pexp.setImplicitThis(true);
            for (int i=1; i<propertyPath.length;i++) {
                pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, CLOSURE_TYPE);
                pexp = new PropertyExpression(pexp, propertyPath[i]);
            }
            pexp.putNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER, implicitReceiver);
            origin.removeNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
            if (origin instanceof PropertyExpression) {
                PropertyExpression rewritten = new PropertyExpression(
                        pexp,
                        ((PropertyExpression) origin).getProperty(),
                        ((PropertyExpression) origin).isSafe()
                );
                rewritten.setSpreadSafe(((PropertyExpression) origin).isSpreadSafe());
                rewritten.setImplicitThis(false);
                rewritten.visit(controller.getAcg());
                rewritten.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, origin.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE));
                return true;
            }
            makeCall(origin, pexp, message, arguments, adapter, safe, spreadSafe, false);
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
                controller.getMethodVisitor().visitTypeInsn(CHECKCAST, type.isArray() ?
                        BytecodeHelper.getTypeDescription(type) :
                        BytecodeHelper.getClassInternalName(type.getName()));
                controller.getOperandStack().replace(type);
            }
        }

        @Override
        public ClassNode getType() {
            if (resolvedType!=null) {
                return resolvedType;
            }
            ClassNode type;
            if (target instanceof ExtensionMethodNode) {
                type = ((ExtensionMethodNode) target).getExtensionMethodNode().getDeclaringClass();
            } else {
                type = getWrapper(controller.getTypeChooser().resolveType(receiver, controller.getClassNode()));
                ClassNode declaringClass = target.getDeclaringClass();
                if (type.getClass() != ClassNode.class
                        && type.getClass() != InnerClassNode.class
                        && type.getClass() != DecompiledClassNode.class) {
                    type = declaringClass; // ex: LUB type
                }
                if (OBJECT_TYPE.equals(type) && !OBJECT_TYPE.equals(declaringClass)) {
                    // can happen for compiler rewritten code, where type information is missing
                    type = declaringClass;
                }
                if (OBJECT_TYPE.equals(declaringClass)) {
                    // check cast not necessary because Object never evolves
                    // and it prevents a potential ClassCastException if the delegate of a closure
                    // is changed in a statically compiled closure
                    type = OBJECT_TYPE;
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
    protected boolean makeCachedCall(Expression origin, ClassExpression sender, Expression receiver, Expression message, Expression arguments, MethodCallerMultiAdapter adapter, boolean safe, boolean spreadSafe, boolean implicitThis, boolean containsSpreadExpression) {
        return false;
    }
}
