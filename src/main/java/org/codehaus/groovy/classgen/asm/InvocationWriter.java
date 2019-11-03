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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter.StatementMeta;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.ShortTypeHandling;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.SWAP;

public class InvocationWriter {

    // method invocation
    public static final MethodCallerMultiAdapter invokeMethodOnCurrent = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "invokeMethodOnCurrent", true, false);
    public static final MethodCallerMultiAdapter invokeMethodOnSuper = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "invokeMethodOnSuper", true, false);
    public static final MethodCallerMultiAdapter invokeMethod = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "invokeMethod", true, false);
    public static final MethodCallerMultiAdapter invokeStaticMethod = MethodCallerMultiAdapter.newStatic(ScriptBytecodeAdapter.class, "invokeStaticMethod", true, true);
    public static final MethodCaller invokeClosureMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "invokeClosure");
    public static final MethodCaller castToVargsArray = MethodCaller.newStatic(DefaultTypeTransformation.class, "castToVargsArray");
    private static final MethodNode CLASS_FOR_NAME_STRING = ClassHelper.CLASS_Type.getDeclaredMethod("forName", new Parameter[]{new Parameter(ClassHelper.STRING_TYPE,"name")});

    // type conversions
    private static final MethodCaller
        asTypeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "asType"),
        castToTypeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "castToType"),
        castToClassMethod = MethodCaller.newStatic(ShortTypeHandling.class, "castToClass"),
        castToStringMethod = MethodCaller.newStatic(ShortTypeHandling.class, "castToString"),
        castToEnumMethod = MethodCaller.newStatic(ShortTypeHandling.class, "castToEnum");

    // constructor calls with this() and super()
    static final MethodCaller selectConstructorAndTransformArguments = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "selectConstructorAndTransformArguments");

    private final WriterController controller;
    
    public InvocationWriter(WriterController wc) {
        this.controller = wc;
    }

    private void makeInvokeMethodCall(MethodCallExpression call, boolean useSuper, MethodCallerMultiAdapter adapter) {
        // receiver
        // we operate on GroovyObject if possible
        Expression objectExpression = call.getObjectExpression();
        // message name
        Expression messageName = new CastExpression(ClassHelper.STRING_TYPE, call.getMethod());
        if (useSuper) {
            ClassNode classNode = controller.isInClosure() ? controller.getOutermostClass() : controller.getClassNode(); // GROOVY-4035 
            ClassNode superClass = classNode.getSuperClass();
            makeCall(call, new ClassExpression(superClass),
                    objectExpression, messageName,
                    call.getArguments(), adapter,
                    call.isSafe(), call.isSpreadSafe(),
                    false
            );
        } else {
            makeCall(call, objectExpression, messageName,
                    call.getArguments(), adapter,
                    call.isSafe(), call.isSpreadSafe(),
                    call.isImplicitThis()
            );
        }
    }
    
    public void makeCall(
            Expression origin,
            Expression receiver, Expression message, Expression arguments,
            MethodCallerMultiAdapter adapter,
            boolean safe, boolean spreadSafe, boolean implicitThis
    ) {
        ClassNode cn = controller.getClassNode();
        if (controller.isInClosure() && !implicitThis && AsmClassGenerator.isThisExpression(receiver)) cn=cn.getOuterClass();
        makeCall(origin, new ClassExpression(cn), receiver, message, arguments,
                adapter, safe, spreadSafe, implicitThis);
    }
    
    protected boolean writeDirectMethodCall(MethodNode target, boolean implicitThis,  Expression receiver, TupleExpression args) {
        if (target==null) return false;
        
        String methodName = target.getName();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        ClassNode declaringClass = target.getDeclaringClass();
        ClassNode classNode = controller.getClassNode();

        MethodVisitor mv = controller.getMethodVisitor();
        int opcode = INVOKEVIRTUAL;
        if (target.isStatic()) {
            opcode = INVOKESTATIC;
        } else if (declaringClass.isInterface()) {
            opcode = INVOKEINTERFACE;
        } else if (target.isPrivate() || ((receiver instanceof VariableExpression && ((VariableExpression) receiver).isSuperExpression()))) {
            opcode = INVOKESPECIAL;
        }

        // handle receiver
        int argumentsToRemove = 0;
        if (opcode!=INVOKESTATIC) {
            if (receiver!=null) {
                // load receiver if not static invocation
                // todo: fix inner class case
                if (implicitThis
                        && !classNode.isDerivedFrom(declaringClass)
                        && !classNode.implementsInterface(declaringClass)
                        && classNode instanceof InnerClassNode) {
                    // we are calling an outer class method
                    compileStack.pushImplicitThis(false);
                    if (controller.isInClosure()) {
                        new VariableExpression("thisObject").visit(controller.getAcg());
                    } else {
                        Expression expr = new PropertyExpression(new ClassExpression(declaringClass), "this");
                        expr.visit(controller.getAcg());
                    }
                } else {
                    compileStack.pushImplicitThis(implicitThis);
                    receiver.visit(controller.getAcg());
                }
                operandStack.doGroovyCast(declaringClass);
                compileStack.popImplicitThis();
                argumentsToRemove++;
            } else {
                mv.visitIntInsn(ALOAD,0);
                operandStack.push(classNode);
                argumentsToRemove++;
            }
        }

        int stackSize = operandStack.getStackLength();

        String owner = BytecodeHelper.getClassInternalName(declaringClass);
        ClassNode receiverType = receiver!=null?controller.getTypeChooser().resolveType(receiver, classNode):declaringClass;
        if (opcode == INVOKEVIRTUAL && ClassHelper.OBJECT_TYPE.equals(declaringClass)) {
            // avoid using a narrowed type if the method is defined on object because it can interfere
            // with delegate type inference in static compilation mode and trigger a ClassCastException
            receiverType = declaringClass;
        }
        if (opcode == INVOKEVIRTUAL) {
            if (!receiverType.equals(declaringClass)
                    && !ClassHelper.OBJECT_TYPE.equals(declaringClass)
                    && !receiverType.isArray()
                    && !receiverType.isInterface()
                    && !ClassHelper.isPrimitiveType(receiverType) // e.g int.getClass()
                    && receiverType.isDerivedFrom(declaringClass)) {

                owner = BytecodeHelper.getClassInternalName(receiverType);
                ClassNode top = operandStack.getTopOperand();
                if (!receiverType.equals(top)) {
                    mv.visitTypeInsn(CHECKCAST, owner);
                }
            } else if (target.isPublic()
                    && (!Modifier.isPublic(declaringClass.getModifiers())
                    && !receiverType.equals(declaringClass))
                    && receiverType.isDerivedFrom(declaringClass)
                    && !receiverType.getPackageName().equals(classNode.getPackageName())) {
                // package private class, public method
                // see GROOVY-6962
                owner = BytecodeHelper.getClassInternalName(receiverType);
            }
        }

        loadArguments(args.getExpressions(), target.getParameters());

        String desc = BytecodeHelper.getMethodDescriptor(target.getReturnType(), target.getParameters());
        mv.visitMethodInsn(opcode, owner, methodName, desc, declaringClass.isInterface());
        ClassNode ret = target.getReturnType().redirect();
        if (ret==ClassHelper.VOID_TYPE) {
            ret = ClassHelper.OBJECT_TYPE;
            mv.visitInsn(ACONST_NULL);
        }
        argumentsToRemove += (operandStack.getStackLength()-stackSize);
        controller.getOperandStack().remove(argumentsToRemove);
        controller.getOperandStack().push(ret);
        return true;
    }

    private boolean lastIsArray(List<Expression> argumentList, int pos) {
        Expression last = argumentList.get(pos);
        ClassNode type = controller.getTypeChooser().resolveType(last, controller.getClassNode());
        return type.isArray();
    }
    
    // load arguments
    protected void loadArguments(List<Expression> argumentList, Parameter[] para) {
        if (para.length==0) return;
        ClassNode lastParaType = para[para.length - 1].getOriginType();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();
        if (lastParaType.isArray()
                && (argumentList.size()>para.length || argumentList.size()==para.length-1 || !lastIsArray(argumentList, para.length-1))) {
            int stackLen = operandStack.getStackLength()+argumentList.size();
            MethodVisitor mv = controller.getMethodVisitor();
            //mv = new org.objectweb.asm.util.TraceMethodVisitor(mv);
            controller.setMethodVisitor(mv);
            // varg call
            // first parameters as usual
            for (int i = 0; i < para.length-1; i++) {
                argumentList.get(i).visit(acg);
                operandStack.doGroovyCast(para[i].getType());
            }
            // last parameters wrapped in an array
            List<Expression> lastParams = new LinkedList<Expression>();
            for (int i=para.length-1; i<argumentList.size();i++) {
                lastParams.add(argumentList.get(i));
            }
            ArrayExpression array = new ArrayExpression(
                    lastParaType.getComponentType(),
                    lastParams
            );
            array.visit(acg);
            // adjust stack length
            while (operandStack.getStackLength()<stackLen) {
                operandStack.push(ClassHelper.OBJECT_TYPE);
            }
            if (argumentList.size()==para.length-1) {
                operandStack.remove(1);
            }
        } else {
            for (int i = 0; i < argumentList.size(); i++) {
                argumentList.get(i).visit(acg);
                operandStack.doGroovyCast(para[i].getType());
            }
        }
    }

    protected boolean makeDirectCall(
        Expression origin, Expression receiver, 
        Expression message, Expression arguments,
        MethodCallerMultiAdapter adapter,
        boolean implicitThis, boolean containsSpreadExpression
    ) {
        if (makeClassForNameCall(origin, receiver, message, arguments)) return true;

        // optimization path
        boolean fittingAdapter =   adapter == invokeMethodOnCurrent ||
                                    adapter == invokeStaticMethod;
        if (fittingAdapter && controller.optimizeForInt && controller.isFastPath()) {
            String methodName = getMethodName(message);
            if (methodName != null) {
                TupleExpression args;
                if (arguments instanceof TupleExpression) {
                    args = (TupleExpression) arguments;
                } else {
                    args = new TupleExpression(receiver);
                }

                StatementMeta meta = null;
                if (origin!=null) meta = origin.getNodeMetaData(StatementMeta.class);
                MethodNode mn = null;
                if (meta!=null) mn = meta.target;

                if (writeDirectMethodCall(mn, true, null, args)) return true;
            }
        }

        if (containsSpreadExpression) return false;
        if (origin instanceof MethodCallExpression) {
            MethodCallExpression mce = (MethodCallExpression) origin;
            MethodNode target = mce.getMethodTarget();
            return writeDirectMethodCall(target, implicitThis, receiver, makeArgumentList(arguments));
        }
        return false;
    }

    protected boolean makeCachedCall(
            Expression origin, ClassExpression sender,
            Expression receiver, Expression message, Expression arguments,
            MethodCallerMultiAdapter adapter,
            boolean safe, boolean spreadSafe, boolean implicitThis,
            boolean containsSpreadExpression
    ) {
        // prepare call site
        if ((adapter == invokeMethod || adapter == invokeMethodOnCurrent || adapter == invokeStaticMethod) && !spreadSafe) {
            String methodName = getMethodName(message);

            if (methodName != null) {
                controller.getCallSiteWriter().makeCallSite(
                        receiver, methodName, arguments, safe, implicitThis, 
                        adapter == invokeMethodOnCurrent, 
                        adapter == invokeStaticMethod);
                return true;
            }
        }
        return false;
    }
    
    protected void makeUncachedCall(
            Expression origin, ClassExpression sender,
            Expression receiver, Expression message, Expression arguments,
            MethodCallerMultiAdapter adapter,
            boolean safe, boolean spreadSafe, boolean implicitThis,
            boolean containsSpreadExpression
    ) {
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();
        AsmClassGenerator acg = controller.getAcg();
        
        // ensure VariableArguments are read, not stored
        compileStack.pushLHS(false);

        // sender only for call sites
        if (adapter == AsmClassGenerator.setProperty) {
            ConstantExpression.NULL.visit(acg);
        } else {
            sender.visit(acg);
        }

        String methodName = getMethodName(message);
        if (adapter == invokeMethodOnSuper && methodName != null) {
            controller.getSuperMethodNames().add(methodName);
        }
        
        // receiver
        compileStack.pushImplicitThis(implicitThis);
        receiver.visit(acg);
        operandStack.box();
        compileStack.popImplicitThis();
        
        
        int operandsToRemove = 2;
        // message
        if (message != null) {
            message.visit(acg);
            operandStack.box();
            operandsToRemove++;
        }

        // arguments
        int numberOfArguments = containsSpreadExpression ? -1 : AsmClassGenerator.argumentSize(arguments);
        if (numberOfArguments > MethodCallerMultiAdapter.MAX_ARGS || containsSpreadExpression) {
            ArgumentListExpression ae = makeArgumentList(arguments);
            if (containsSpreadExpression) {
                acg.despreadList(ae.getExpressions(), true);
            } else {
                ae.visit(acg);
            }
        } else if (numberOfArguments > 0) {
            operandsToRemove += numberOfArguments;
            TupleExpression te = (TupleExpression) arguments;
            for (int i = 0; i < numberOfArguments; i++) {
                Expression argument = te.getExpression(i);
                argument.visit(acg);
                operandStack.box();
                if (argument instanceof CastExpression) acg.loadWrapper(argument);
            }
        }

        if (adapter==null) adapter = invokeMethod;
        adapter.call(controller.getMethodVisitor(), numberOfArguments, safe, spreadSafe);

        compileStack.popLHS();
        operandStack.replace(ClassHelper.OBJECT_TYPE,operandsToRemove);
    }
    
    protected void makeCall(
            Expression origin, ClassExpression sender,
            Expression receiver, Expression message, Expression arguments,
            MethodCallerMultiAdapter adapter,
            boolean safe, boolean spreadSafe, boolean implicitThis
    ) {
        // direct method call paths
        boolean containsSpreadExpression = AsmClassGenerator.containsSpreadExpression(arguments);

        if (makeDirectCall(origin, receiver, message, arguments, adapter, implicitThis, containsSpreadExpression)) return;

        // normal path
        if (makeCachedCall(origin, sender, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis, containsSpreadExpression)) return;

        // path through ScriptBytecodeAdapter
        makeUncachedCall(origin, sender, receiver, message, arguments, adapter, safe, spreadSafe, implicitThis, containsSpreadExpression);
    }

    /**
     * if Class.forName(x) is recognized, make a direct method call
     */
    protected boolean makeClassForNameCall(Expression origin, Expression receiver, Expression message, Expression arguments) {
        if (! (receiver instanceof ClassExpression)) return false;
        ClassExpression ce = (ClassExpression) receiver;
        if (!ClassHelper.CLASS_Type.equals(ce.getType())) return false;
        String msg = getMethodName(message);
        if (!"forName".equals(msg)) return false;
        ArgumentListExpression ae = makeArgumentList(arguments);
        if (ae.getExpressions().size()!=1) return false;
        return writeDirectMethodCall(CLASS_FOR_NAME_STRING,false, receiver, ae);
    }

    public static ArgumentListExpression makeArgumentList(Expression arguments) {
        ArgumentListExpression ae;
        if (arguments instanceof ArgumentListExpression) {
            ae = (ArgumentListExpression) arguments;
        } else if (arguments instanceof TupleExpression) {
            TupleExpression te = (TupleExpression) arguments;
            ae = new ArgumentListExpression(te.getExpressions());
        } else {
            ae = new ArgumentListExpression();
            ae.addExpression(arguments);
        }
        return ae;
    }

    protected String getMethodName(Expression message) {
        String methodName = null;
        if (message instanceof CastExpression) {
            CastExpression msg = (CastExpression) message;
            if (msg.getType() == ClassHelper.STRING_TYPE) {
                final Expression methodExpr = msg.getExpression();
                if (methodExpr instanceof ConstantExpression)
                  methodName = methodExpr.getText();
            }
        }

        if (methodName == null && message instanceof ConstantExpression) {
            ConstantExpression constantExpression = (ConstantExpression) message;
            methodName = constantExpression.getText();
        }
        return methodName;
    }

    private boolean isFunctionInterfaceCall(MethodCallExpression call) {
        if (!"call".equals(call.getMethodAsString())) {
            return false;
        }

        Expression objectExpression = call.getObjectExpression();

        if (null == objectExpression) {
            return false;
        }

        if (AsmClassGenerator.isThisExpression(objectExpression)) {
            return false;
        }

        if (ClassHelper.isFunctionalInterface(objectExpression.getType())) {
            return true;
        }

        return false;
    }

    public void writeInvokeMethod(MethodCallExpression call) {
        if (isClosureCall(call)) {
            // let's invoke the closure method
            invokeClosure(call.getArguments(), call.getMethodAsString());
        } else {
            if (isFunctionInterfaceCall(call)) {
                call = transformToRealMethodCall(call);
            }

            boolean isSuperMethodCall = usesSuper(call);
            MethodCallerMultiAdapter adapter = invokeMethod;
            if (isSuperMethodCall && call.isSafe()) {
                // safe is not necessary here because "super" is always not null
                // but keeping the flag would trigger a VerifyError (see GROOVY-6045)
                call.setSafe(false);
            }
            if (AsmClassGenerator.isThisExpression(call.getObjectExpression())) adapter = invokeMethodOnCurrent;
            if (isSuperMethodCall) adapter = invokeMethodOnSuper;
            if (isStaticInvocation(call)) adapter = invokeStaticMethod;
            makeInvokeMethodCall(call, isSuperMethodCall, adapter);
        }
    }

    private MethodCallExpression transformToRealMethodCall(MethodCallExpression call) {
        ClassNode type = call.getObjectExpression().getType();
        final MethodNode methodNode = ClassHelper.findSAM(type);

        call = (MethodCallExpression) call.transformExpression(expression -> {
            if (!(expression instanceof ConstantExpression)) {
                return expression;
            }

            return new ConstantExpression(methodNode.getName());
        });
        call.setMethodTarget(methodNode);
        return call;
    }

    private boolean isClosureCall(MethodCallExpression call) {
        // are we a local variable?
        // it should not be an explicitly "this" qualified method call
        // and the current class should have a possible method

        ClassNode classNode = controller.getClassNode();
        String methodName = call.getMethodAsString();
        if (methodName==null) return false;
        if (!call.isImplicitThis()) return false;
        if (!AsmClassGenerator.isThisExpression(call.getObjectExpression())) return false;
        FieldNode field = classNode.getDeclaredField(methodName);
        if (field == null) return false;
        if (isStaticInvocation(call) && !field.isStatic()) return false;
        Expression arguments = call.getArguments();
        return ! classNode.hasPossibleMethod(methodName, arguments);
    }

    private void invokeClosure(Expression arguments, String methodName) {
        AsmClassGenerator acg = controller.getAcg();
        acg.visitVariableExpression(new VariableExpression(methodName));
        controller.getOperandStack().box();
        if (arguments instanceof TupleExpression) {
            arguments.visit(acg);
        } else {
            new TupleExpression(arguments).visit(acg);
        }
        invokeClosureMethod.call(controller.getMethodVisitor());
        controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
    }

    private boolean isStaticInvocation(MethodCallExpression call) {
        if (!AsmClassGenerator.isThisExpression(call.getObjectExpression())) return false;
        if (controller.isStaticMethod()) return true;
        return controller.isStaticContext() && !call.isImplicitThis();
    }
    
    private static boolean usesSuper(MethodCallExpression call) {
        Expression expression = call.getObjectExpression();
        if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            String variable = varExp.getName();
            return variable.equals("super");
        }
        return false;
    }

    public void writeInvokeStaticMethod(StaticMethodCallExpression call) {
        makeCall(call,
                new ClassExpression(call.getOwnerType()),
                new ConstantExpression(call.getMethod()),
                call.getArguments(),
                InvocationWriter.invokeStaticMethod,
                false, false, false);
    }
    
    private boolean writeDirectConstructorCall(ConstructorCallExpression call) {
        if (!controller.isFastPath()) return false;
        
        StatementMeta meta = call.getNodeMetaData(StatementMeta.class);
        ConstructorNode cn = null;
        if (meta!=null) cn = (ConstructorNode) meta.target;
        if (cn==null) return false;
        
        String ownerDescriptor = prepareConstructorCall(cn);
        TupleExpression args = makeArgumentList(call.getArguments());
        loadArguments(args.getExpressions(), cn.getParameters());
        finnishConstructorCall(cn, ownerDescriptor, args.getExpressions().size());
        
        return true;
    }
    
    protected String prepareConstructorCall(ConstructorNode cn) {
        String owner = BytecodeHelper.getClassInternalName(cn.getDeclaringClass());
        MethodVisitor mv = controller.getMethodVisitor();
        
        mv.visitTypeInsn(NEW, owner);
        mv.visitInsn(DUP);
        return owner;
    }
    
    protected void finnishConstructorCall(ConstructorNode cn, String ownerDescriptor, int argsToRemove) {
        String desc = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, cn.getParameters());
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitMethodInsn(INVOKESPECIAL, ownerDescriptor, "<init>", desc, false);
        
        controller.getOperandStack().remove(argsToRemove);
        controller.getOperandStack().push(cn.getDeclaringClass());
    }

    protected void writeNormalConstructorCall(ConstructorCallExpression call) {
        Expression arguments = call.getArguments();
        if (arguments instanceof TupleExpression) {
            TupleExpression tupleExpression = (TupleExpression) arguments;
            int size = tupleExpression.getExpressions().size();
            if (size == 0) {
                arguments = MethodCallExpression.NO_ARGUMENTS;
            }
        }

        Expression receiverClass = new ClassExpression(call.getType());
        controller.getCallSiteWriter().makeCallSite(
                receiverClass, CallSiteWriter.CONSTRUCTOR,
                arguments, false, false, false,
                false);
    }
    
    public void writeInvokeConstructor(ConstructorCallExpression call) {
        if (writeDirectConstructorCall(call)) return;
        if (writeAICCall(call)) return;
        writeNormalConstructorCall(call);
    }

    protected boolean writeAICCall(ConstructorCallExpression call) {
        if (!call.isUsingAnonymousInnerClass()) return false;
        ConstructorNode cn = call.getType().getDeclaredConstructors().get(0);
        OperandStack os = controller.getOperandStack();
        
        String ownerDescriptor = prepareConstructorCall(cn);
        
        List<Expression> args = makeArgumentList(call.getArguments()).getExpressions();
        Parameter[] params = cn.getParameters();
        // if a this appears as parameter here, then it should be
        // not static, unless we are in a static method. But since 
        // ACG#visitVariableExpression does the opposite for this case, we
        // push here an explicit this. This should not have any negative effect
        // sine visiting a method call or property with implicit this will push
        // a new value for this again.
        controller.getCompileStack().pushImplicitThis(true);
        for (int i=0; i<params.length; i++) {
            Parameter p = params[i];
            Expression arg = args.get(i);
            if (arg instanceof VariableExpression) {
                VariableExpression var = (VariableExpression) arg;
                loadVariableWithReference(var);
            } else {
                arg.visit(controller.getAcg());
            }
            os.doGroovyCast(p.getType());
        }
        controller.getCompileStack().popImplicitThis();
        finnishConstructorCall(cn, ownerDescriptor, args.size());
        return true;
    }
    
    private void loadVariableWithReference(VariableExpression var) {
        if (!var.isUseReferenceDirectly()) {
            var.visit(controller.getAcg());
        } else {
            ClosureWriter.loadReference(var.getName(), controller);
        }
    }

    public final void makeSingleArgumentCall(Expression receiver, String message, Expression arguments) {
        makeSingleArgumentCall(receiver, message, arguments, false);
    }

    public void makeSingleArgumentCall(Expression receiver, String message, Expression arguments, boolean safe) {
        controller.getCallSiteWriter().makeSingleArgumentCall(receiver, message, arguments, safe);
    }

    public void writeSpecialConstructorCall(final ConstructorCallExpression call) {
        controller.getCompileStack().pushInSpecialConstructorCall();
        visitSpecialConstructorCall(call);
        controller.getCompileStack().pop();
    }

    private void visitSpecialConstructorCall(ConstructorCallExpression call) {
        if (controller.getClosureWriter().addGeneratedClosureConstructorCall(call)) return;
        ClassNode callNode = controller.getClassNode();
        if (call.isSuperCall()) callNode = callNode.getSuperClass();
        List<ConstructorNode> constructors = sortConstructors(call, callNode);
        if (!makeDirectConstructorCall(constructors, call, callNode)) {
            makeMOPBasedConstructorCall(constructors, call, callNode);
        }
    }

    private static List<ConstructorNode> sortConstructors(ConstructorCallExpression call, ClassNode callNode) {
        // sort in a new list to prevent side effects
        List<ConstructorNode> constructors = new ArrayList<ConstructorNode>(callNode.getDeclaredConstructors());
        Comparator comp = (arg0, arg1) -> {
            ConstructorNode c0 = (ConstructorNode) arg0;
            ConstructorNode c1 = (ConstructorNode) arg1;
            String descriptor0 = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, c0.getParameters());
            String descriptor1 = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, c1.getParameters());
            return descriptor0.compareTo(descriptor1);
        };
        Collections.sort(constructors, comp);
        return constructors;
    }

    private boolean makeDirectConstructorCall(List<ConstructorNode> constructors, ConstructorCallExpression call, ClassNode callNode) {
        if (!controller.isConstructor()) return false;

        Expression arguments = call.getArguments();
        List<Expression> argumentList;
        if (arguments instanceof TupleExpression) {
            argumentList = ((TupleExpression) arguments).getExpressions();
        } else {
            argumentList = new ArrayList<>();
            argumentList.add(arguments);
        }
        for (Expression expression : argumentList) {
            if (expression instanceof SpreadExpression) return false;
        }

        ConstructorNode cn = getMatchingConstructor(constructors, argumentList);
        if (cn==null) return false;
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        Parameter[] params = cn.getParameters();

        mv.visitVarInsn(ALOAD, 0);
        for (int i=0; i<params.length; i++) {
            Expression expression = argumentList.get(i);
            expression.visit(controller.getAcg());
            if (!AsmClassGenerator.isNullConstant(expression)) {
                operandStack.doGroovyCast(params[i].getType());
            }
            operandStack.remove(1);
        }
        String descriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, params);
        mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor, false);

        return true;
    }

    private void makeMOPBasedConstructorCall(List<ConstructorNode> constructors, ConstructorCallExpression call, ClassNode callNode) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        call.getArguments().visit(controller.getAcg());
        // keep Object[] on stack
        mv.visitInsn(DUP);
        // to select the constructor we need also the number of
        // available constructors and the class we want to make
        // the call on
        BytecodeHelper.pushConstant(mv, -1);
        controller.getAcg().visitClassExpression(new ClassExpression(callNode));
        operandStack.remove(1);
        // removes one Object[] leaves the int containing the
        // call flags and the constructor number
        selectConstructorAndTransformArguments.call(mv);
        //load "this"
        if (controller.isConstructor()) {
            mv.visitVarInsn(ALOAD, 0);
        } else {
            mv.visitTypeInsn(NEW, BytecodeHelper.getClassInternalName(callNode));
        }
        mv.visitInsn(SWAP);
        TreeMap<Integer,ConstructorNode> sortedConstructors = new TreeMap<Integer, ConstructorNode>();
        for (ConstructorNode constructor : constructors) {
            String typeDescriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, constructor.getParameters());
            int hash = BytecodeHelper.hashCode(typeDescriptor);
            ConstructorNode sameHashNode = sortedConstructors.put(hash, constructor);
            if (sameHashNode!=null) {
                controller.getSourceUnit().addError(
                        new SyntaxException("Unable to compile class "+controller.getClassNode().getName() + " due to hash collision in constructors", call.getLineNumber(), call.getColumnNumber()));
            }
        }
        Label[] targets = new Label[constructors.size()];
        int[] indices = new int[constructors.size()];
        Iterator<Integer> hashIt = sortedConstructors.keySet().iterator();
        Iterator<ConstructorNode> constructorIt = sortedConstructors.values().iterator();
        for (int i = 0; i < targets.length; i++) {
            targets[i] = new Label();
            indices[i] = hashIt.next();
        }

        // create switch targets
        Label defaultLabel = new Label();
        Label afterSwitch = new Label();
        mv.visitLookupSwitchInsn(defaultLabel, indices, targets);
        for (int i = 0; i < targets.length; i++) {
            mv.visitLabel(targets[i]);
            // to keep the stack height, we need to leave
            // one Object[] on the stack as last element. At the
            // same time, we need the Object[] on top of the stack
            // to extract the parameters.
            if (controller.isConstructor()) {
                // in this case we need one "this", so a SWAP will exchange
                // "this" and Object[], a DUP_X1 will then copy the Object[]
                /// to the last place in the stack:
                //     Object[],this -SWAP-> this,Object[]
                //     this,Object[] -DUP_X1-> Object[],this,Object[]
                mv.visitInsn(SWAP);
                mv.visitInsn(DUP_X1);
            } else {
                // in this case we need two "this" in between and the Object[]
                // at the bottom of the stack as well as on top for our invokeSpecial
                // So we do DUP_X1, DUP2_X1, POP
                //     Object[],this -DUP_X1-> this,Object[],this
                //     this,Object[],this -DUP2_X1-> Object[],this,this,Object[],this
                //     Object[],this,this,Object[],this -POP->  Object[],this,this,Object[]
                mv.visitInsn(DUP_X1);
                mv.visitInsn(DUP2_X1);
                mv.visitInsn(POP);
            }

            ConstructorNode cn = constructorIt.next();
            String descriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, cn.getParameters());

            // unwrap the Object[] and make transformations if needed
            // that means, to duplicate the Object[], make a cast with possible
            // unboxing and then swap it with the Object[] for each parameter
            // vargs need special attention and transformation though
            Parameter[] parameters = cn.getParameters();
            int lengthWithoutVargs = parameters.length;
            if (parameters.length>0 && parameters[parameters.length-1].getType().isArray()) {
                lengthWithoutVargs--;
            }
            for (int p = 0; p < lengthWithoutVargs; p++) {
                loadAndCastElement(operandStack, mv, parameters, p);
            }
            if (parameters.length>lengthWithoutVargs) {
                ClassNode type = parameters[lengthWithoutVargs].getType();
                BytecodeHelper.pushConstant(mv, lengthWithoutVargs);
                controller.getAcg().visitClassExpression(new ClassExpression(type));
                operandStack.remove(1);
                castToVargsArray.call(mv);
                BytecodeHelper.doCast(mv, type);
            } else {
                // at the end we remove the Object[]
                // the vargs case simply the last swap so no pop is needed
                mv.visitInsn(POP);
            }
            // make the constructor call
            mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor, false);
            mv.visitJumpInsn(GOTO, afterSwitch);
        }
        mv.visitLabel(defaultLabel);
        // this part should never be reached!
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("This class has been compiled with a super class which is binary incompatible with the current super class found on classpath. You should recompile this class with the new version.");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(afterSwitch);

        // For a special constructor call inside a constructor we don't need
        // any result object on the stack, for outside the constructor we do.
        // to keep the stack height for the able we kept one object as dummy
        // result on the stack, which we can remove now if inside a constructor.
        if (!controller.isConstructor()) {
            // in case we are not in a constructor we have an additional
            // object on the stack, the result of our constructor call
            // which we want to keep, so we swap with the dummy object and
            // do normal removal of it. In the end, the call result will be
            // on the stack then
            mv.visitInsn(SWAP);
            operandStack.push(callNode); // for call result
        }
        mv.visitInsn(POP);
    }

    private static void loadAndCastElement(OperandStack operandStack, MethodVisitor mv, Parameter[] parameters, int p) {
        operandStack.push(ClassHelper.OBJECT_TYPE);
        mv.visitInsn(DUP);
        BytecodeHelper.pushConstant(mv, p);
        mv.visitInsn(AALOAD);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        ClassNode type = parameters[p].getType();
        operandStack.doGroovyCast(type);
        operandStack.swap();
        operandStack.remove(2);
    }

    // we match only on the number of arguments, not anything else
    private static ConstructorNode getMatchingConstructor(List<ConstructorNode> constructors, List<Expression> argumentList) {
        ConstructorNode lastMatch = null;
        for (int i=0; i<constructors.size(); i++) {
            ConstructorNode cn = constructors.get(i);
            Parameter[] params = cn.getParameters();
            // if number of parameters does not match we have no match
            if (argumentList.size()!=params.length) continue;
            if (lastMatch==null) {
                lastMatch = cn;
            } else {
                // we already had a match so we don't make a direct call at all
                return null;
            }
        }
        return lastMatch;
    }

    /**
     * This converts sourceType to a non primitive by using Groovy casting.
     * sourceType might be a primitive
     * This might be done using SBA#castToType
     */
    public void castToNonPrimitiveIfNecessary(final ClassNode sourceType, final ClassNode targetType) {
        OperandStack os = controller.getOperandStack();
        ClassNode boxedType = os.box();
        if (WideningCategories.implementsInterfaceOrSubclassOf(boxedType, targetType)) return;
        MethodVisitor mv = controller.getMethodVisitor();
        if (ClassHelper.CLASS_Type.equals(targetType)) {
            castToClassMethod.call(mv);
        } else if (ClassHelper.STRING_TYPE.equals(targetType)) {
            castToStringMethod.call(mv);
        } else if (targetType.isDerivedFrom(ClassHelper.Enum_Type)) {
            (new ClassExpression(targetType)).visit(controller.getAcg());
            os.remove(1);
            castToEnumMethod.call(mv);
            BytecodeHelper.doCast(mv, targetType);
        } else {
            (new ClassExpression(targetType)).visit(controller.getAcg());
            os.remove(1);
            castToTypeMethod.call(mv);
        }
    }

    public void castNonPrimitiveToBool(ClassNode last) {
        MethodVisitor mv = controller.getMethodVisitor();
        BytecodeHelper.unbox(mv, ClassHelper.boolean_TYPE);
    }

    public void coerce(ClassNode from, ClassNode target) {
        if (from.isDerivedFrom(target)) return;
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack os = controller.getOperandStack();
        os.box();
        (new ClassExpression(target)).visit(controller.getAcg());
        os.remove(1);
        asTypeMethod.call(mv);
        BytecodeHelper.doCast(mv,target);
        os.replace(target);
    }
}
