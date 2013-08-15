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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS;
import static org.objectweb.asm.Opcodes.*;

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

    private final WriterController controller;

    private MethodCallExpression currentCall;

    public StaticInvocationWriter(WriterController wc) {
        super(wc);
        controller = wc;
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
        MethodNode mn = (MethodNode) call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mn == null) {
            super.writeInvokeConstructor(call);
            return;
        }
        ConstructorNode cn;
        if (mn instanceof ConstructorNode) {
            cn = (ConstructorNode) mn;
        } else {
            cn = new ConstructorNode(mn.getModifiers(), mn.getParameters(), mn.getExceptions(), mn.getCode());
            cn.setDeclaringClass(mn.getDeclaringClass());
        }

        String ownerDescriptor = prepareConstructorCall(cn);
        TupleExpression args = makeArgumentList(call.getArguments());
        int before = controller.getOperandStack().getStackLength();
        loadArguments(args.getExpressions(), cn.getParameters());
        finnishConstructorCall(cn, ownerDescriptor, controller.getOperandStack().getStackLength() - before);

    }

    @Override
    protected boolean writeDirectMethodCall(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args) {
        if (target instanceof ExtensionMethodNode) {
            ExtensionMethodNode emn = (ExtensionMethodNode) target;
            MethodNode node = emn.getExtensionMethodNode();
            String methodName = target.getName();

            MethodVisitor mv = controller.getMethodVisitor();
            int argumentsToRemove = 0;
            List<Expression> argumentList = new LinkedList<Expression>(args.getExpressions());

            if (emn.isStaticExtension()) {
                // it's a static extension method
                argumentList.add(0, ConstantExpression.NULL);
            } else {
                argumentList.add(0, receiver);
            }

            Parameter[] parameters = node.getParameters();
            loadArguments(argumentList, parameters);

            String owner = BytecodeHelper.getClassInternalName(node.getDeclaringClass());
            String desc = BytecodeHelper.getMethodDescriptor(target.getReturnType(), parameters);
            mv.visitMethodInsn(INVOKESTATIC, owner, methodName, desc);
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
            if (target != null
                    && classNode.isDerivedFrom(ClassHelper.CLOSURE_TYPE)
                    && controller.isInClosure()
                    && !(target.isPublic() || target.isProtected())
                    && target.getDeclaringClass() != classNode) {
                // replace call with an invoker helper call
                // todo: use MOP generated methods instead
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
            if (target != null && target.isPrivate()) {
                ClassNode declaringClass = target.getDeclaringClass();
                if ((isPrivateBridgeMethodsCallAllowed(declaringClass, classNode) || isPrivateBridgeMethodsCallAllowed(classNode, declaringClass))
                        && declaringClass.getNodeMetaData(PRIVATE_BRIDGE_METHODS) != null
                        && !declaringClass.equals(classNode)) {
                    @SuppressWarnings("unchecked")
                    Map<MethodNode, MethodNode> bridges = (Map<MethodNode, MethodNode>) declaringClass.redirect().getNodeMetaData(PRIVATE_BRIDGE_METHODS);
                    MethodNode bridge = bridges.get(target);
                    if (bridge != null) {
                        ArgumentListExpression newArgs = new ArgumentListExpression(target.isStatic()?new ConstantExpression(null):receiver);
                        for (Expression expression : args.getExpressions()) {
                            newArgs.addExpression(expression);
                        }
                        return writeDirectMethodCall(bridge, implicitThis, receiver, newArgs);
                    }
                }
                if (declaringClass != classNode) {
                    controller.getSourceUnit().addError(new SyntaxException("Cannot call private method " + (target.isStatic() ? "static " : "") +
                                                        declaringClass.toString(false) + "#" + target.getName() + " from class " + classNode.toString(false), receiver.getLineNumber(), receiver.getColumnNumber(), receiver.getLastLineNumber(), receiver.getLastColumnNumber()));
                }
            }
            if (target != null && receiver != null) {
                if (!(receiver instanceof VariableExpression) || !((VariableExpression) receiver).isSuperExpression()) {
                    // in order to avoid calls to castToType, which is the dynamic behaviour, we make sure that we call CHECKCAST instead
                    // then replace the top operand type
                    Expression checkCastReceiver = new CheckcastReceiverExpression(receiver, target);
                    return super.writeDirectMethodCall(target, implicitThis, checkCastReceiver, args);
                }
            }
            return super.writeDirectMethodCall(target, implicitThis, receiver, args);
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
        ClassNode lastArgType = argumentList.size()>0?
                typeChooser.resolveType(argumentList.get(argumentList.size()-1), controller.getClassNode()):null;
        if (lastParaType.isArray()
                && ((argumentList.size() > para.length)
                || ((argumentList.size() == (para.length - 1)) && !lastParaType.equals(lastArgType))
                || ((argumentList.size() == para.length && lastArgType!=null && !lastArgType.isArray())
                    && (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(lastArgType,lastParaType.getComponentType())))
                        || ClassHelper.GSTRING_TYPE.equals(lastArgType) && ClassHelper.STRING_TYPE.equals(lastParaType.getComponentType()))
                ) {
            int stackLen = operandStack.getStackLength() + argumentList.size();
            MethodVisitor mv = controller.getMethodVisitor();
            MethodVisitor orig = mv;
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
            for (int i = para.length - 1; i < argumentList.size(); i++) {
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
            if (argumentList.size() == para.length - 1) {
                operandStack.remove(1);
            }
        } else if (argumentList.size() == para.length) {
            for (int i = 0; i < argumentList.size(); i++) {
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
                Expression curArg = j < argumentList.size() ? argumentList.get(j) : null;
                Expression initialExpression = (Expression) curParam.getNodeMetaData(StaticTypesMarker.INITIAL_EXPRESSION);
                if (initialExpression == null && curParam.hasInitialExpression())
                    initialExpression = curParam.getInitialExpression();
                if (initialExpression == null && curParam.getNodeMetaData(Verifier.INITIAL_EXPRESSION)!=null) {
                    initialExpression = (Expression) curParam.getNodeMetaData(Verifier.INITIAL_EXPRESSION);
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

    private boolean isNullConstant(final Expression expression) {
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
        Object implicitReceiver = origin.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        if (implicitReceiver !=null && implicitThis) {
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
            makeCall(origin, pexp, message, arguments, adapter, safe, spreadSafe, false);
            return;
        }
        // if call is spread safe, replace it with a for in loop
        if (spreadSafe && origin instanceof MethodCallExpression) {
            MethodVisitor mv = controller.getMethodVisitor();
            CompileStack compileStack = controller.getCompileStack();
            TypeChooser typeChooser = controller.getTypeChooser();
            OperandStack operandStack = controller.getOperandStack();
            ClassNode classNode = controller.getClassNode();
            int counter = labelCounter.incrementAndGet();

            // create an empty arraylist
            VariableExpression result = new VariableExpression(
                    "spreadresult" + counter,
                    StaticCompilationVisitor.ARRAYLIST_CLASSNODE
            );
            ConstructorCallExpression cce = new ConstructorCallExpression(StaticCompilationVisitor.ARRAYLIST_CLASSNODE, ArgumentListExpression.EMPTY_ARGUMENTS);
            cce.setNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, StaticCompilationVisitor.ARRAYLIST_CONSTRUCTOR);
            DeclarationExpression declr = new DeclarationExpression(
                    result,
                    Token.newSymbol("=", origin.getLineNumber(), origin.getColumnNumber()),
                    cce
            );
            declr.visit(controller.getAcg());
            // if (receiver != null)
            receiver.visit(controller.getAcg());
            Label ifnull = compileStack.createLocalLabel("ifnull_" + counter);
            mv.visitJumpInsn(IFNULL, ifnull);
            operandStack.remove(1); // receiver consumed by if()
            Label nonull = compileStack.createLocalLabel("nonull_" + counter);
            mv.visitLabel(nonull);
            ClassNode componentType = StaticTypeCheckingVisitor.inferLoopElementType(typeChooser.resolveType(receiver, classNode));
            Parameter iterator = new Parameter(componentType, "for$it$" + counter);
            VariableExpression iteratorAsVar = new VariableExpression(iterator);
            MethodCallExpression origMCE = (MethodCallExpression) origin;
            MethodCallExpression newMCE = new MethodCallExpression(
                    iteratorAsVar,
                    origMCE.getMethodAsString(),
                    origMCE.getArguments()
            );
            newMCE.setMethodTarget(origMCE.getMethodTarget());
            newMCE.setSafe(true);
            MethodCallExpression add = new MethodCallExpression(
                    result,
                    "add",
                    newMCE
            );
            add.setMethodTarget(StaticCompilationVisitor.ARRAYLIST_ADD_METHOD);
            // for (e in receiver) { result.add(e?.method(arguments) }
            ForStatement stmt = new ForStatement(
                    iterator,
                    receiver,
                    new ExpressionStatement(add)
            );
            stmt.visit(controller.getAcg());
            // else { empty list }
            mv.visitLabel(ifnull);

            // end of if/else
            // return result list
            result.visit(controller.getAcg());
        } else if (safe && origin instanceof MethodCallExpression) {
            // wrap call in an IFNULL check
            MethodVisitor mv = controller.getMethodVisitor();
            CompileStack compileStack = controller.getCompileStack();
            OperandStack operandStack = controller.getOperandStack();
            int counter = labelCounter.incrementAndGet();
            // if (receiver != null)
            receiver.visit(controller.getAcg());
            Label ifnull = compileStack.createLocalLabel("ifnull_" + counter);
            mv.visitJumpInsn(IFNULL, ifnull);
            operandStack.remove(1); // receiver consumed by if()
            Label nonull = compileStack.createLocalLabel("nonull_" + counter);
            mv.visitLabel(nonull);
            MethodCallExpression origMCE = (MethodCallExpression) origin;
            MethodCallExpression newMCE = new MethodCallExpression(
                    origMCE.getObjectExpression(),
                    origMCE.getMethodAsString(),
                    origMCE.getArguments()
            );
            MethodNode methodTarget = origMCE.getMethodTarget();
            newMCE.setMethodTarget(methodTarget);
            newMCE.setSafe(false);
            newMCE.setImplicitThis(origMCE.isImplicitThis());
            newMCE.setSourcePosition(origMCE);
            newMCE.visit(controller.getAcg());
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
                    if (stcsw.makeGetField(receiver, typeChooser.resolveType(receiver, controller.getClassNode()), pname, false, true)) {
                        return;
                    }
                }
            }
            super.makeCall(origin, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis);
        }
    }

    private static void pushZero(final MethodVisitor mv, final ClassNode type) {
        boolean isInt = ClassHelper.int_TYPE.equals(type);
        boolean isShort = ClassHelper.short_TYPE.equals(type);
        boolean isByte = ClassHelper.byte_TYPE.equals(type);
        if (isInt || isShort || isByte) {
            mv.visitInsn(ICONST_0);
        } else if (ClassHelper.long_TYPE.equals(type)) {
            mv.visitInsn(LCONST_0);
        } else if (ClassHelper.float_TYPE.equals(type)) {
            mv.visitInsn(FCONST_0);
        } else if (ClassHelper.double_TYPE.equals(type)) {
            mv.visitInsn(DCONST_0);
        } else if (ClassHelper.boolean_TYPE.equals(type)) {
            mv.visitInsn(ICONST_0);
        } else {
            mv.visitLdcInsn(0);
        }
    }

    private class CheckcastReceiverExpression extends Expression {
        private final Expression receiver;
        private final MethodNode target;

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
                ClassNode type;
                if (target instanceof ExtensionMethodNode) {
                    type = ((ExtensionMethodNode) target).getExtensionMethodNode().getDeclaringClass();
                } else {
                    type = target.getDeclaringClass();
                }
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
    }

    public MethodCallExpression getCurrentCall() {
        return currentCall;
    }
}
