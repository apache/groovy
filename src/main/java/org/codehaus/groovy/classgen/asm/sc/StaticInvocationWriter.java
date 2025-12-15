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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.EnumConstantClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
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
import org.codehaus.groovy.ast.expr.SpreadExpression;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.groovy.ast.tools.ClassNodeUtils.formatTypeName;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isSubtype;
import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.apache.groovy.ast.tools.ExpressionUtils.isSuperExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisOrSuper;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.attrX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.inSamePackage;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe0;
import static org.codehaus.groovy.transform.trait.Traits.isTrait;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StaticInvocationWriter extends InvocationWriter {

    private final AtomicInteger labelCounter = new AtomicInteger();

    public StaticInvocationWriter(final WriterController wc) {
        super(wc);
    }

    Expression getCurrentCall() {
        return currentCall;
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
        ClassNode declaringClass = cn.getDeclaringClass();
        ClassNode enclosingClass = controller.getClassNode();
        List<Expression> argList = call.getArguments() instanceof TupleExpression
                ? ((TupleExpression) call.getArguments()).getExpressions() : List.of(call.getArguments());
        if (!AsmClassGenerator.isMemberDirectlyAccessible(cn.getModifiers(), declaringClass, enclosingClass)) {
            MethodNode bridge = null;
            if (call.getNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS) != null) {
                Map<MethodNode, MethodNode> bridgeMethods = declaringClass.getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS);
                if (bridgeMethods != null) bridge = bridgeMethods.get(cn);
            }
            if (bridge instanceof ConstructorNode) {
                cn = (ConstructorNode) bridge;
                argList = new ArrayList<>(argList);
                argList.add(0, nullX());
            } else {
                controller.getSourceUnit().addError(new SyntaxException(
                        "Cannot call private constructor for " + declaringClass.toString(false) + " from class " + enclosingClass.toString(false),
                        call
                ));
            }
        }

        String ownerDescriptor = prepareConstructorCall(cn);
        int before = controller.getOperandStack().getStackLength();
        loadArguments(argList, cn.getParameters());
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

    private Expression thisObjectExpression(final ClassNode source, final ClassNode target) {
        ClassNode thisType = source;
        while (ClassHelper.isGeneratedFunction(thisType)) {
            thisType = thisType.getOuterClass();
        }
        Expression thisExpr;
        if (isTrait(thisType.getOuterClass())) {
            thisExpr = varX("thisObject"); // GROOVY-7242, GROOVY-8127
        } else if (controller.isStaticContext()) {
            thisExpr = varX("thisObject", makeClassSafe0(ClassHelper.CLASS_Type, thisType.asGenericsType()));
        } else {
            thisExpr = varX("thisObject", thisType);
            // adjust for multiple levels of nesting
            while (!isSubtype(target, thisType)) {
                FieldNode thisZero = thisType.getDeclaredField("this$0");
                if (thisZero != null) {
                    thisExpr = attrX(thisExpr, "this$0");
                    thisExpr.setType(thisZero.getType());
                    thisType = thisType.getOuterClass();
                    if (thisType != null) continue;
                }
                break;
            }
        }
        return thisExpr;
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
            while (lookupClassNode != null && !isSubtype(target.getDeclaringClass(), lookupClassNode)) {
                lookupClassNode = lookupClassNode.getOuterClass();
            }
            if (lookupClassNode == null) {
                return false;
            }
        } else {
            lookupClassNode = target.getDeclaringClass().redirect();
        }
        Map<MethodNode, MethodNode> bridges = lookupClassNode.getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS);
        MethodNode bridge = (bridges == null ? null : bridges.get(target));
        if (bridge != null) {
            Expression newReceiver = receiver;
            if (implicitThis) {
                if (!controller.isInGeneratedFunction()) {
                    if (!isSubtype(lookupClassNode, thisClass))
                        newReceiver = propX(classX(lookupClassNode), "this");
                } else if (thisClass != null) {
                    newReceiver = thisObjectExpression(thisClass, lookupClassNode);
                }
            }
            ArgumentListExpression newArguments = args(target.isStatic() ? nullX() : newReceiver);
            for (Expression expression : args.getExpressions()) {
                newArguments.addExpression(expression);
            }
            return writeDirectMethodCall(bridge, implicitThis, newReceiver, newArguments);
        }
        return false;
    }

    @Override
    protected boolean writeDirectMethodCall(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args) {
        if (target == null) return false;

        ClassNode enclosingClass = controller.getClassNode();
        ClassNode declaringClass = target.getDeclaringClass();

        if (target instanceof ExtensionMethodNode) {
            ExtensionMethodNode emn = (ExtensionMethodNode) target;
            MethodVisitor mv = controller.getMethodVisitor();
            MethodNode mn = emn.getExtensionMethodNode();
            Parameter[] parameters = mn.getParameters();
            ClassNode returnType = mn.getReturnType();

            List<Expression> argumentList = new ArrayList<>();
            if (emn.isStaticExtension()) {
                argumentList.add(nullX());
            } else if (!isThisOrSuper(receiver) || !controller.isInGeneratedFunction()) {
                argumentList.add(receiver);
            } else {
                argumentList.add(thisObjectExpression(enclosingClass, declaringClass));
            }
            argumentList.addAll(args.getExpressions());
            loadArguments(argumentList, parameters);

            String owner = BytecodeHelper.getClassInternalName(mn.getDeclaringClass());
            String desc = BytecodeHelper.getMethodDescriptor(returnType, parameters);
            mv.visitMethodInsn(INVOKESTATIC, owner, target.getName(), desc, false);
            controller.getOperandStack().remove(argumentList.size());

            if (ClassHelper.isPrimitiveVoid(returnType)) {
                if (currentCall != null && currentCall.getNodeMetaData(AsmClassGenerator.ELIDE_EXPRESSION_VALUE) != null) {
                    return true; // do not load value
                }
                returnType = ClassHelper.OBJECT_TYPE;
                mv.visitInsn(ACONST_NULL);
            }
            controller.getOperandStack().push(returnType);
            return true;
        }

        if (target == StaticTypeCheckingVisitor.CLOSURE_CALL_VARGS) { // wrap arguments in an array
            Expression array = new ArrayExpression(ClassHelper.OBJECT_TYPE, args.getExpressions());
            return super.writeDirectMethodCall(target, implicitThis, receiver, args(array));
        }

        ClassNode receiverType = receiver == null ? ClassHelper.OBJECT_TYPE : controller.getTypeChooser().resolveType(receiver, controller.getThisType());
        if (StaticTypeCheckingSupport.isClassClassNodeWrappingConcreteType(receiverType) && !ClassHelper.isClassType(declaringClass)) {
            receiverType = receiverType.getGenericsTypes()[0].getType(); // GROOVY-11694
        }

        if (AsmClassGenerator.isMemberDirectlyAccessible(target.getModifiers(), declaringClass, enclosingClass)
                && !(target.isProtected() && !inSamePackage(declaringClass, enclosingClass) && !isSubtype(receiverType, enclosingClass))) { // GROOVY-7325
            boolean isThisImplicit = implicitThis;
            Expression theReceiver = receiver;
            if (implicitThis && isThisExpression(receiver)
                    && !isSubtype(declaringClass, enclosingClass)) {
                if (target.isStatic()) {
                    theReceiver = classX(declaringClass);
                } else if (!controller.isInGeneratedFunction()) {
                    theReceiver = propX(classX(declaringClass), "this");
                } else {
                    theReceiver = thisObjectExpression(enclosingClass, declaringClass);
                }
                if (!(theReceiver instanceof VariableExpression)) isThisImplicit = false;
                theReceiver.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, declaringClass);
            }
            if (theReceiver != null && !isSuperExpression(receiver) && !isClassWithSuper(receiver)
                    && currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER) == null) {
                // call CHECKCAST in order to avoid calls to castToType (aka dynamic behaviour)
                theReceiver = new CheckcastReceiverExpression(theReceiver, target);
            }
            return super.writeDirectMethodCall(target, isThisImplicit, theReceiver, args);

        } else if (tryBridgeMethod(target, receiver, implicitThis, args, enclosingClass)) {
            return true;
        } else {
            writeMethodAccessError(target, receiver != null ? receiver : args);
            return false;
        }
    }

    private void writeMethodAccessError(final MethodNode target, final Expression origin) {
        var descriptor = new java.util.StringJoiner(", ", target.getName() + "(", ")");
        for (Parameter parameter : target.getParameters()) {
            descriptor.add(formatTypeName(parameter.getOriginType()));
        }
        String message = "Cannot access method: " + descriptor + " of class: " + formatTypeName(target.getDeclaringClass());

        controller.getSourceUnit().addError(new SyntaxException(message, origin));
    }

    private boolean isClassWithSuper(Expression receiver) {
        if (receiver instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) receiver;
            return pexp.getObjectExpression() instanceof ClassExpression && "super".equals(pexp.getPropertyAsString());
        }
        return false;
    }

    protected static boolean isPrivateBridgeMethodsCallAllowed(final ClassNode receiver, final ClassNode caller) {
        if (receiver == null) return false;
        if (receiver.redirect() == caller) return true;
        if (isPrivateBridgeMethodsCallAllowed(receiver.getOuterClass(), caller)) return true;
        if (caller.getOuterClass() != null && isPrivateBridgeMethodsCallAllowed(receiver, caller.getOuterClass())) return true;
        return false;
    }

    @Override
    protected void loadArguments(final List<Expression> argumentList, final Parameter[] parameters) {
        final int nArgs = argumentList.size(), nPrms = parameters.length; if (nPrms == 0) return;

        ClassNode classNode = controller.getClassNode();
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode lastArgType = nArgs == 0 ? null : typeChooser.resolveType(argumentList.get(nArgs - 1), classNode);
        ClassNode lastPrmType = parameters[nPrms - 1].getType();

        // target is variadic and args are too many or one short or just enough with array compatibility
        if (lastPrmType.isArray() && (nArgs > nPrms || nArgs == nPrms - 1
                || (nArgs == nPrms && !lastArgType.isArray()
                    && (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(lastArgType, lastPrmType.getComponentType())
                        || ClassHelper.isGStringType(lastArgType) && ClassHelper.isStringType(lastPrmType.getComponentType())))
        )) {
            OperandStack operandStack = controller.getOperandStack();
            // first arguments/parameters as usual
            for (int i = 0; i < nPrms - 1; i += 1) {
                visitArgument(argumentList.get(i), parameters[i].getType());
            }
            // wrap remaining arguments in an array for last parameter
            boolean spread = false;
            List<Expression> lastArgs = new ArrayList<>();
            for (int i = nPrms - 1; i < nArgs; i += 1) {
                Expression arg = argumentList.get(i);
                lastArgs.add(arg);
                spread = spread || arg instanceof SpreadExpression;
            }
            if (spread) { // GROOVY-10597
                controller.getAcg().despreadList(lastArgs, true);
                operandStack.push(ClassHelper.OBJECT_TYPE.makeArray());
                controller.getInvocationWriter().coerce(operandStack.getTopOperand(), lastPrmType);
            } else {
                controller.getAcg().visitArrayExpression(new ArrayExpression(lastPrmType.getComponentType(), lastArgs));
            }
            // adjust operand stack
            if (nArgs == nPrms - 1) {
                operandStack.remove(1);
            } else {
                for (int n = lastArgs.size(); n > 1; n -= 1)
                    operandStack.push(ClassHelper.OBJECT_TYPE);
            }
        } else if (nArgs == nPrms) {
            for (int i = 0; i < nArgs; i += 1) {
                visitArgument(argumentList.get(i), parameters[i].getType());
            }
        } else { // call with default arguments
            Expression[] arguments = new Expression[nPrms];
            for (int i = 0, j = 0; i < nPrms; i += 1) {
                Parameter p = parameters[i];
                ClassNode pType = p.getType();
                Expression a = (j < nArgs ? argumentList.get(j) : null);
                ClassNode aType = (a == null ? null : typeChooser.resolveType(a, classNode));

                Expression expression = getInitialExpression(p); // default argument
                if (expression != null && !isCompatibleArgumentType(aType, pType)) {
                    arguments[i] = expression;
                } else if (a != null) {
                    arguments[i] = a;
                    j += 1;
                } else {
                    String errorMessage = "Binding failed for arguments [" +
                            argumentList.stream().map(arg -> typeChooser.resolveType(arg, classNode).toString(false)).collect(Collectors.joining(", ")) +
                            "] and parameters [" +
                            Arrays.stream(parameters).map(prm -> prm.getType().toString(false)).collect(Collectors.joining(", ")) +
                            "]";
                    controller.getSourceUnit().addFatalError(errorMessage, currentCall);
                }
            }
            for (int i = 0; i < nArgs; i += 1) {
                visitArgument(arguments[i], parameters[i].getType());
            }
        }
    }

    private static Expression getInitialExpression(final Parameter parameter) {
        Expression initialExpression = parameter.getNodeMetaData(StaticTypesMarker.INITIAL_EXPRESSION);
        if (initialExpression == null && parameter.hasInitialExpression()) {
            initialExpression = parameter.getInitialExpression();
        }
        if (initialExpression == null && parameter.getNodeMetaData(Verifier.INITIAL_EXPRESSION) != null) {
            initialExpression = parameter.getNodeMetaData(Verifier.INITIAL_EXPRESSION);
        }
        return initialExpression;
    }

    private static boolean isCompatibleArgumentType(final ClassNode argumentType, final ClassNode parameterType) {
        if (argumentType == null)
            return false;
        if (ClassHelper.getWrapper(argumentType).equals(ClassHelper.getWrapper(parameterType)))
            return true;
        if (parameterType.isInterface())
            return argumentType.implementsInterface(parameterType);
        if (parameterType.isArray() && argumentType.isArray())
            return isCompatibleArgumentType(argumentType.getComponentType(), parameterType.getComponentType());
        return ClassHelper.getWrapper(argumentType).isDerivedFrom(ClassHelper.getWrapper(parameterType));
    }

    private void visitArgument(final Expression argument, final ClassNode parameterType) {
        argument.visit(controller.getAcg());
        if (!isNullConstant(argument)) {
            controller.getOperandStack().doGroovyCast(parameterType);
        }
    }

    @Override
    public void makeCall(final Expression origin, final Expression receiver, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe, final boolean implicitThis) {
        if (origin.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION) != null) {
            if (origin instanceof MethodCallExpression) ((MethodCallExpression) origin).setMethodTarget(null); // ensure dynamic resolve
            InvocationWriter dynamicInvocationWriter = ((StaticTypesWriterController) controller).getRegularInvocationWriter();
            dynamicInvocationWriter.makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
            return;
        }
        if (implicitThis && tryImplicitReceiver(origin, message, arguments, adapter, safe, spreadSafe)) {
            return;
        }
        // if call is spread safe, replace it with a for in loop
        if (spreadSafe && (origin instanceof MethodCallExpression || (origin instanceof PropertyExpression && !controller.getCompileStack().isLHS()))) {
            // receiver expressions with side-effects should not be re-visited; avoid by using a temporary variable
            Expression tmpReceiver = receiver;
            if (!(receiver instanceof VariableExpression || receiver instanceof ConstantExpression)) {
                tmpReceiver = new TemporaryVariableExpression(receiver);
            }

            Label nonNull = new Label();
            Label allDone = new Label();
            MethodVisitor mv = controller.getMethodVisitor();
            OperandStack operandStack = controller.getOperandStack();
            boolean produceResultList = origin.getNodeMetaData(AsmClassGenerator.ELIDE_EXPRESSION_VALUE) == null;

            // if (receiver == null)
            tmpReceiver.visit(controller.getAcg());
            mv.visitJumpInsn(IFNONNULL, nonNull);
            operandStack.remove(1);

            // result is null
            if (produceResultList)
                mv.visitInsn(ACONST_NULL);
            mv.visitJumpInsn(GOTO, allDone);

            // else
            mv.visitLabel(nonNull);

            ClassNode resultType = origin.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            ClassNode valuesType = origin.getNodeMetaData(StaticCompilationMetadataKeys.COMPONENT_TYPE);
            if (valuesType == null) valuesType = StaticTypeCheckingVisitor.inferLoopElementType(resultType);

            // def result = new ArrayList()
            ConstructorCallExpression cce = ctorX(StaticCompilationVisitor.ARRAYLIST_CLASSNODE);
            cce.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET,
                    StaticCompilationVisitor.ARRAYLIST_CONSTRUCTOR);
            var result = new TemporaryVariableExpression(cce);
            if (produceResultList) result.visit(controller.getAcg());

            ClassNode elementType = StaticTypeCheckingVisitor.inferLoopElementType(controller.getTypeChooser().resolveType(receiver, controller.getClassNode()));
            Parameter element = new Parameter(elementType, "for$it$" + labelCounter.incrementAndGet());

            VariableScope varScope = new VariableScope(controller.getCompileStack().getScope());
            varScope.setInStaticContext(varScope.getParent().isInStaticContext());
            element .setInStaticContext(varScope.getParent().isInStaticContext());
            varScope.putDeclaredVariable(element);

            Expression nextValue;
            if (origin instanceof MethodCallExpression) {
                MethodCallExpression oldMCE = (MethodCallExpression) origin;
                MethodCallExpression newMCE = callX(
                        varX(element),
                        oldMCE.getMethod(),
                        oldMCE.getArguments()
                );
                newMCE.setGenericsTypes(oldMCE.getGenericsTypes());
                newMCE.setImplicitThis(false);
                MethodNode target = oldMCE.getMethodTarget();
                newMCE.setMethodTarget(target);
                if (target == null || !target.isVoidMethod())
                    newMCE.setNodeMetaData(StaticTypesMarker.INFERRED_TYPE, valuesType);
                newMCE.setSafe(true);
                nextValue = newMCE;
            } else {
                PropertyExpression oldPE = (PropertyExpression) origin;
                PropertyExpression newPE = origin instanceof AttributeExpression
                    ? new AttributeExpression(varX(element), oldPE.getProperty(), true)
                    : new  PropertyExpression(varX(element), oldPE.getProperty(), true);
                newPE.setImplicitThis(false);
                newPE.setNodeMetaData(StaticTypesMarker.INFERRED_TYPE, valuesType);
                nextValue = newPE;
            }

            MethodCallExpression addNextValue = callX(result, "add", /*castX(valuesType, */nextValue/*)*/);
            addNextValue.setImplicitThis(false);
            addNextValue.setMethodTarget(StaticCompilationVisitor.ARRAYLIST_ADD_METHOD);

            // for (element in receiver) result.add(element?.method(arguments))
            var loop = new ForStatement(element, tmpReceiver, stmt(produceResultList ? addNextValue : nextValue));
            loop.setVariableScope(varScope); // GROOVY-11816
            loop.visit(controller.getAcg());
            if (produceResultList) {
                result.remove(controller);
            }
            // end of if/else
            mv.visitLabel(allDone);

            if (tmpReceiver instanceof TemporaryVariableExpression) {
                ((TemporaryVariableExpression) tmpReceiver).remove(controller);
            }
        } else if (safe && origin instanceof MethodCallExpression) {
            CompileStack compileStack = controller.getCompileStack();
            OperandStack operandStack = controller.getOperandStack();
            MethodVisitor mv = controller.getMethodVisitor();
            int counter = labelCounter.incrementAndGet();
            // (receiver != null) ? receiver.name(args) : null
            Label ifnull = compileStack.createLocalLabel("ifnull_" + counter);
            Label nonull = compileStack.createLocalLabel("nonull_" + counter);
            Label theEnd = compileStack.createLocalLabel("ending_" + counter);
            var slot = new ExpressionAsVariableSlot(controller, receiver);
            slot.visit(controller.getAcg());
            operandStack.box();
            mv.visitJumpInsn(IFNULL, ifnull);
            operandStack.remove(1); // receiver consumed
            mv.visitLabel(nonull);
            var newMCE = (MethodCallExpression) origin.transformExpression((expression) -> expression);
            newMCE.setObjectExpression(new VariableSlotLoader(slot.getType(), slot.getIndex(), operandStack));
            newMCE.getObjectExpression().setSourcePosition(((MethodCallExpression) origin).getObjectExpression());
            newMCE.setSafe(false);
            int osl = operandStack.getStackLength();
            newMCE.visit(controller.getAcg());
            compileStack.removeVar(slot.getIndex());
            if (operandStack.getStackLength() > osl) {
                operandStack.box(); // non-void method
                mv.visitJumpInsn(GOTO, theEnd);
                mv.visitLabel(ifnull);
                mv.visitInsn(ACONST_NULL);
                mv.visitLabel(theEnd);
            } else {
                mv.visitLabel(ifnull);
            }
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

        public CheckcastReceiverExpression(final Expression receiver, final MethodNode target) {
            this.receiver = receiver;
            this.target = target;
            setType(null);
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
                if (ClassHelper.isGStringType(topOperand) && ClassHelper.isStringType(type)) {
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
            ClassNode type = super.getType();
            if (type == null) {
                if (target instanceof ExtensionMethodNode) {
                    type = ((ExtensionMethodNode) target).getExtensionMethodNode().getDeclaringClass();
                } else {
                    type = controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
                    if (ClassHelper.isPrimitiveType(type)) {
                        type = ClassHelper.getWrapper(type);
                    }
                    ClassNode declaringClass = target.getDeclaringClass();
                    Class<?> typeClass = type.getClass();
                    if (typeClass != ClassNode.class
                            && typeClass != InnerClassNode.class
                            && typeClass != DecompiledClassNode.class
                            && typeClass != EnumConstantClassNode.class) {
                        type = declaringClass; // ex: LUB type
                    }
                    if (ClassHelper.isObjectType(declaringClass)) {
                        // checkcast not necessary because Object never evolves
                        // and it prevents a potential ClassCastException if the
                        // delegate of a closure is changed in an SC closure
                        type = ClassHelper.OBJECT_TYPE;
                    } else if (ClassHelper.isObjectType(type)) {
                        // can happen for compiler rewritten code, where type information is missing
                        type = declaringClass;
                    }
                }
                setType(type);
            }
            return type;
        }
    }

    @Override
    protected boolean makeCachedCall(final Expression origin, final ClassExpression sender, final Expression receiver, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe, final boolean implicitThis, final boolean containsSpreadExpression) {
        return false;
    }
}
