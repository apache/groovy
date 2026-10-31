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
import org.codehaus.groovy.ast.EnumConstantClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeInstruction;
import org.codehaus.groovy.classgen.BytecodeSequence;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.BytecodeVariable;
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.groovy.ast.tools.ClassNodeUtils.formatTypeName;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isSubtype;
import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.apache.groovy.ast.tools.ExpressionUtils.isSuperExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisOrSuper;
import static org.apache.groovy.ast.tools.ExpressionUtils.mayHaveSideEffects;
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
import static org.codehaus.groovy.ast.tools.GenericsUtils.erasure;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe0;
import static org.codehaus.groovy.transform.trait.Traits.isTrait;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;

/**
 * Invocation writer for statically compiled code paths.
 */
public class StaticInvocationWriter extends InvocationWriter {

    private final AtomicInteger labelCounter = new AtomicInteger();

    /**
     * Creates an invocation writer that favors direct bytecode calls over dynamic dispatch.
     */
    public StaticInvocationWriter(final WriterController wc) {
        super(wc);
    }

    /**
     * Returns the current call expression being emitted, if any.
     */
    Expression getCurrentCall() {
        return currentCall;
    }

    /** {@inheritDoc} */
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

    /**
     * Determines if the bytecode of the accessing class can call the constructor
     * with {@code NEW} and {@code INVOKESPECIAL}.
     * <p>
     * A protected or package-private constructor needs the same run-time package
     * (see {@link AsmClassGenerator#isInSameRuntimePackage}). Outside its package,
     * a protected constructor can only be called by {@code super(...)} or an
     * anonymous inner class.
     * <p>
     * Unlike {@link AsmClassGenerator#isMemberDirectlyAccessible}, there is no
     * subclass rule: the verifier rejects {@code NEW} and {@code INVOKESPECIAL}
     * of a protected constructor of another package in a subclass too, so a
     * constructor reference to it creates the object through the meta-class.
     */
    static boolean isDirectlyAccessible(final ConstructorNode ctor, final ClassNode accessingClass) {
        if (ctor.isPublic()) return true;
        ClassNode declaringClass = ctor.getDeclaringClass();
        if (ctor.isPrivate()) { // nestmates can call a private constructor
            return AsmClassGenerator.isMemberDirectlyAccessible(ctor.getModifiers(), declaringClass, accessingClass);
        }
        return AsmClassGenerator.isInSameRuntimePackage(declaringClass, accessingClass);
    }

    /** {@inheritDoc} */
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
     * Returns the public instance method of {@code Object} that the given
     * method is or overrides, like {@code toString()}, or {@code null}.
     */
    private static MethodNode getObjectMethod(final MethodNode method) {
        if (method.isStatic() || !method.isPublic()) return null;
        MethodNode objectMethod = ClassHelper.OBJECT_TYPE.getDeclaredMethod(method.getName(), method.getParameters());
        return objectMethod != null && objectMethod.isPublic() && !objectMethod.isStatic() ? objectMethod : null;
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

    /** {@inheritDoc} */
    @Override
    protected boolean writeDirectMethodCall(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args) {
        if (target == null) return false;

        ClassNode enclosingClass = controller.getClassNode();
        ClassNode declaringClass = target.getDeclaringClass();

        if (target instanceof ExtensionMethodNode emn) {
            MethodVisitor mv = controller.getMethodVisitor();
            MethodNode mn = emn.getExtensionMethodNode();
            Parameter[] parameters = mn.getParameters();
            ClassNode returnType = mn.getReturnType();

            List<Expression> argumentList = new ArrayList<>();
            if (emn.isStaticExtension()) {
                if (!implicitThis && mayHaveSideEffects(receiver)) {
                    // a static member reached through an expression still evaluates it (JLS 15.12.4.1)
                    receiver.visit(controller.getAcg());
                    controller.getOperandStack().pop();
                }
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
            MethodNode theTarget = target;
            if (implicitThis && isThisExpression(receiver)
                    && (!isSubtype(declaringClass, enclosingClass)
                        // a method of Object called in a closure or lambda is not called on it,
                        // unless it was resolved against the closure itself (TO_SELF)
                        || (ClassHelper.isObjectType(declaringClass) && controller.isInGeneratedFunction()
                            && !isSubtype(ClassHelper.CLOSURE_TYPE, receiverType)))) {
                if (target.isStatic()) {
                    theReceiver = classX(declaringClass);
                } else if (!controller.isInGeneratedFunction()) {
                    ClassNode outerType = getImplicitReceiverType(enclosingClass, declaringClass);
                    theReceiver = propX(classX(outerType != null ? outerType : declaringClass), "this");
                } else {
                    theReceiver = thisObjectExpression(enclosingClass, declaringClass);
                    MethodNode objectMethod = getObjectMethod(target);
                    if (objectMethod != null && !enclosingClass.implementsInterface(ClassHelper.GENERATED_LAMBDA_TYPE)) {
                        // a closure without a this object, e.g. after dehydrate(), uses its own method
                        theReceiver = new ThisObjectOrClosureExpression(theReceiver);
                        theTarget = objectMethod;
                    }
                }
                if (!(theReceiver instanceof VariableExpression)) isThisImplicit = false;
                theReceiver.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, theTarget.getDeclaringClass());
            }
            if (theReceiver != null && !target.isStatic() && !isSuperExpression(receiver) && !isClassWithSuper(receiver)
                    && currentCall.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER) == null) {
                // call CHECKCAST in order to avoid calls to castToType (aka dynamic behaviour)
                ClassNode storedType = storedTypeIfWiderThanInferred(theReceiver, receiverType);
                MethodNode storedTypeTarget = storedType != null ? findSameMethodOf(storedType, theTarget) : null;
                if (storedTypeTarget != null) theTarget = storedTypeTarget;
                CheckcastReceiverExpression checkcastReceiver = new CheckcastReceiverExpression(theReceiver, theTarget);
                if (storedTypeTarget != null) checkcastReceiver.setType(storedType); // no cast to the narrower type
                theReceiver = checkcastReceiver;
            }
            return super.writeDirectMethodCall(theTarget, isThisImplicit, theReceiver, args);

        } else if (tryBridgeMethod(target, receiver, implicitThis, args, enclosingClass)) {
            return true;
        } else {
            writeMethodAccessError(target, receiver != null ? receiver : args);
            return false;
        }
    }

    /**
     * A local variable may be stored as its declared type while the type checker
     * knows a narrower type for it (flow typing). Returns the stored type when
     * that is the case for the receiver, else {@code null}.
     */
    private ClassNode storedTypeIfWiderThanInferred(final Expression receiver, final ClassNode inferredType) {
        if (!(receiver instanceof VariableExpression ve) || ve.isThisExpression() || ve.isSuperExpression()) return null;
        Variable accessedVariable = ve.getAccessedVariable();
        if (!(accessedVariable instanceof VariableExpression || accessedVariable instanceof Parameter)) return null;
        BytecodeVariable variable = controller.getCompileStack().getVariable(ve.getName(), false);
        if (variable == null) return null;
        ClassNode storedType = variable.getType();
        if (storedType == null || ClassHelper.isPrimitiveType(storedType) || ClassHelper.isPrimitiveType(inferredType)
                || storedType.equals(inferredType) || !StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(inferredType, storedType)) {
            return null;
        }
        return storedType;
    }

    /**
     * Finds the method that {@code target} implements or overrides as seen from
     * {@code type}, so that it can be called without casting a receiver of that
     * type to the narrower type that declares {@code target}. Like javac, the call
     * is then bound to the type the receiver is held as; virtual dispatch selects
     * the same implementation.
     */
    static MethodNode findSameMethodOf(final ClassNode type, final MethodNode target) {
        if (target.isStatic() || target.isPrivate() || target instanceof ExtensionMethodNode) return null;
        if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(type, target.getDeclaringClass())) return target;

        String descriptor = BytecodeHelper.getMethodDescriptor(target.getReturnType(), target.getParameters());
        Set<ClassNode> candidates = new LinkedHashSet<>();
        for (ClassNode c = type; c != null; c = c.getSuperClass()) candidates.add(c);
        candidates.addAll(type.getAllInterfaces());
        candidates.add(ClassHelper.OBJECT_TYPE);
        for (ClassNode candidate : candidates) {
            if (!Modifier.isPublic(candidate.getModifiers())) continue;
            for (MethodNode method : candidate.getDeclaredMethods(target.getName())) {
                if (method.isPublic() && !method.isStatic()
                        && descriptor.equals(BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameters()))) {
                    return method;
                }
            }
        }
        return null;
    }

    private void writeMethodAccessError(final MethodNode target, final Expression origin) {
        var descriptor = new StringJoiner(", ", target.getName() + "(", ")");
        for (Parameter parameter : target.getParameters()) {
            descriptor.add(formatTypeName(parameter.getOriginType()));
        }
        String message = "Cannot access method: " + descriptor + " of class: " + formatTypeName(target.getDeclaringClass());

        controller.getSourceUnit().addError(new SyntaxException(message, origin));
    }

    private boolean isClassWithSuper(Expression receiver) {
        if (receiver instanceof PropertyExpression pexp) {
            return pexp.getObjectExpression() instanceof ClassExpression && "super".equals(pexp.getPropertyAsString());
        }
        return false;
    }

    /**
     * @deprecated Use bridge metadata on the declaring class instead of probing outer-class nesting manually.
     */
    @Deprecated(since = "6.0.0")
    protected static boolean isPrivateBridgeMethodsCallAllowed(final ClassNode receiver, final ClassNode caller) {
        if (receiver == null) return false;
        if (receiver.redirect() == caller) return true;
        if (isPrivateBridgeMethodsCallAllowed(receiver.getOuterClass(), caller)) return true;
        if (caller.getOuterClass() != null && isPrivateBridgeMethodsCallAllowed(receiver, caller.getOuterClass())) return true;
        return false;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    protected boolean makeCachedCall(final Expression origin, final ClassExpression sender, final Expression receiver, final Expression message, final Expression arguments, final MethodCallerMultiAdapter adapter, final boolean safe, final boolean spreadSafe, final boolean implicitThis, final boolean containsSpreadExpression) {
        return false;
    }

    /** {@inheritDoc} */
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
            if (origin instanceof MethodCallExpression oldMCE) {
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
            boolean tryGetField = (adapter == AsmClassGenerator.getField
                    || adapter == AsmClassGenerator.getGroovyObjectField);
            if (!tryGetField || !makeGetField(origin, receiver, safe)) { // GETFIELD or GETSTATIC
                super.makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
            }
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

    private boolean makeGetField(final Expression origin, final Expression receiver, final boolean safe) {
        if (origin instanceof AttributeExpression && controller.getCallSiteWriter() instanceof StaticTypesCallSiteWriter) {
            String fieldName = ((AttributeExpression) origin).getPropertyAsString();
            if (fieldName != null) {
                ClassNode receiverType = receiver.getType();
                if (!(receiver instanceof ClassExpression)) { // GROOVY-11840
                    receiverType = controller.getTypeChooser().resolveType(receiver, controller.getClassNode());
                }
                return ((StaticTypesCallSiteWriter) controller.getCallSiteWriter()).makeGetField(receiver, receiverType, fieldName, safe, /*implicitThis:*/false);
            }
        }
        return false;
    }

    /**
     * Whether a receiver held as the given type and used as a {@code String} could be a
     * {@code GString}. A {@code List<String>} filled by dynamic code may hold a {@code GString},
     * so a type derived from generics is not a guarantee; a type check such as {@code instanceof}
     * that narrowed a variable to {@code String} is.
     */
    static boolean mayHoldGString(final Expression receiver, final ClassNode heldType) {
        // a value held as the common type of several types is only checked against its class part
        ClassNode checkedType = heldType instanceof WideningCategories.LowestUpperBoundClassNode ? heldType.getUnresolvedSuperClass() : heldType;
        if (!StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(ClassHelper.GSTRING_TYPE, checkedType)) return false;
        Variable variable = receiver instanceof VariableExpression ve ? ve.getAccessedVariable() : null;
        if (variable instanceof VariableExpression || variable instanceof Parameter) {
            ClassNode variableType = ((ASTNode) variable).getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            if (variableType == null) variableType = variable.getOriginType();
            // a variable not typed as String itself is only String in this scope because of a type check
            return StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(variableType, ClassHelper.STRING_TYPE);
        }
        return true;
    }

    /**
     * Casts the value on top of the stack to {@code String}, converting a {@code GString}
     * to its text first, as Groovy does wherever a {@code String} is required.
     */
    static void castToStringConvertingGString(final MethodVisitor mv) {
        Label isString = new Label();
        mv.visitInsn(DUP);
        mv.visitTypeInsn(INSTANCEOF, "groovy/lang/GString");
        mv.visitJumpInsn(IFEQ, isString);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
        mv.visitLabel(isString);
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
    }

    /**
     * Loads the this object of the closure being generated, or the closure
     * itself when the this object is {@code null}.
     */
    private class ThisObjectOrClosureExpression extends Expression {
        private final Expression thisObject;

        ThisObjectOrClosureExpression(final Expression thisObject) {
            this.thisObject = thisObject;
            setType(ClassHelper.OBJECT_TYPE);
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            return this;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            thisObject.visit(visitor);
            if (visitor instanceof AsmClassGenerator) {
                MethodVisitor mv = controller.getMethodVisitor();
                Label done = new Label();
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNONNULL, done);
                mv.visitInsn(POP);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitLabel(done);
                controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
            }
        }
    }

    /**
     * Marker on a functional-interface parameter whose instantiated type is {@code String}
     * but whose erased type is wider, so a {@code GString} may arrive at the call.
     */
    static final String GSTRING_BRIDGE = StaticInvocationWriter.class.getName() + ".gstringBridge";

    /**
     * Erased type to use for a parameter instantiated to {@code String} from a type variable,
     * when a {@code GString} can still be passed through that erasure. {@code LambdaMetafactory}
     * otherwise check-casts to {@code String} before Groovy can convert the value.
     * Returns {@code null} when the parameter should keep its instantiated type.
     */
    static ClassNode gstringBridgeErasure(final ClassNode exactType, final ClassNode samType) {
        if (exactType == null || samType == null || !ClassHelper.isStringType(exactType)) return null;
        ClassNode erased = erasure(samType);
        if (erased == null || ClassHelper.isStringType(erased)) return null;
        if (!StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(ClassHelper.GSTRING_TYPE, erased)) return null;
        return erased;
    }

    /**
     * Receives {@code parameter} as {@code erased}. A prologue converts a {@code GString} to {@code String}
     * before the body uses the value. The origin type is the erasure, so later casts are plain check-casts.
     */
    static void markGStringBridge(final Parameter parameter, final ClassNode erased) {
        parameter.setOriginType(erased);
        parameter.putNodeMetaData(GSTRING_BRIDGE, Boolean.TRUE);
        parameter.setType(erased);
    }

    /**
     * A new parameter received as {@code erased}, converted from {@code GString} to {@code String} by the prologue.
     */
    static Parameter newGStringBridgeParameter(final String name, final ClassNode erased) {
        Parameter parameter = new Parameter(erased, name);
        parameter.putNodeMetaData(GSTRING_BRIDGE, Boolean.TRUE);
        return parameter;
    }

    /**
     * Statement that converts each bridged parameter from {@code GString} to {@code String}
     * in place, so the rest of the method sees a {@code String}. Non-{@code GString} values
     * are still check-cast to {@code String}.
     */
    static Statement gstringBridgePrologue(final WriterController controller, final Parameter[] parameters) {
        boolean any = false;
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (Boolean.TRUE.equals(parameter.getNodeMetaData(GSTRING_BRIDGE))) {
                    any = true;
                    break;
                }
            }
        }
        if (!any) return null;
        return new BytecodeSequence(new BytecodeInstruction() {
            @Override
            public void visit(final MethodVisitor mv) {
                for (Parameter parameter : parameters) {
                    if (!Boolean.TRUE.equals(parameter.getNodeMetaData(GSTRING_BRIDGE))) continue;
                    BytecodeVariable variable = controller.getCompileStack().getVariable(parameter.getName());
                    convertBridgedParameter(controller, mv, variable);
                }
            }
        });
    }

    private static void convertBridgedParameter(final WriterController controller, final MethodVisitor mv, final BytecodeVariable variable) {
        OperandStack operandStack = controller.getOperandStack();
        if (variable.isHolder()) {
            mv.visitVarInsn(ALOAD, variable.getIndex());
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;", false);
            operandStack.push(ClassHelper.OBJECT_TYPE);
        } else {
            operandStack.load(variable.getType(), variable.getIndex());
        }
        castToStringConvertingGString(mv);
        operandStack.replace(ClassHelper.STRING_TYPE);
        operandStack.storeVar(variable);
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
                if (ClassHelper.isStringType(type) && mayHoldGString(receiver, topOperand)) {
                    castToStringConvertingGString(controller.getMethodVisitor());
                    controller.getOperandStack().replace(type);
                    return;
                }
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
}
