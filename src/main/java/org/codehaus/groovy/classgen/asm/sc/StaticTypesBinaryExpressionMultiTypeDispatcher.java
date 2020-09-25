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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.asm.BinaryExpressionMultiTypeDispatcher;
import org.codehaus.groovy.classgen.asm.BinaryExpressionWriter;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.VariableSlotLoader;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.ARRAYLIST_ADD_METHOD;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.ARRAYLIST_CLASSNODE;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.ARRAYLIST_CONSTRUCTOR;

/**
 * A specialized version of the multi type binary expression dispatcher which is aware of static compilation.
 * It is able to generate optimized bytecode for some operations using JVM instructions when available.
 */
public class StaticTypesBinaryExpressionMultiTypeDispatcher extends BinaryExpressionMultiTypeDispatcher implements Opcodes {

    private final AtomicInteger labelCounter = new AtomicInteger();
    private static final MethodNode CLOSURE_GETTHISOBJECT_METHOD = CLOSURE_TYPE.getMethod("getThisObject", Parameter.EMPTY_ARRAY);


    public StaticTypesBinaryExpressionMultiTypeDispatcher(WriterController wc) {
        super(wc);
    }

    @Override
    protected void writePostOrPrefixMethod(int op, String method, Expression expression, Expression orig) {
        MethodNode mn = orig.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        WriterController controller = getController();
        OperandStack operandStack = controller.getOperandStack();
        if (mn!=null) {
            operandStack.pop();
            MethodCallExpression call = new MethodCallExpression(
                    expression,
                    method,
                    ArgumentListExpression.EMPTY_ARGUMENTS
            );
            call.setMethodTarget(mn);
            call.visit(controller.getAcg());
            return;
        }

        ClassNode top = operandStack.getTopOperand();
        if (ClassHelper.isPrimitiveType(top) && (ClassHelper.isNumberType(top)||char_TYPE.equals(top))) {
            MethodVisitor mv = controller.getMethodVisitor();
            visitInsnByType(top, mv, ICONST_1, LCONST_1, FCONST_1, DCONST_1);
            if ("next".equals(method)) {
                visitInsnByType(top, mv, IADD, LADD, FADD, DADD);
            } else {
                visitInsnByType(top, mv, ISUB, LSUB, FSUB, DSUB);
            }
            return;
        }
        super.writePostOrPrefixMethod(op, method, expression, orig);
    }

    private static void visitInsnByType(ClassNode top, MethodVisitor mv, int iInsn, int lInsn, int fInsn, int dInsn) {
        if (WideningCategories.isIntCategory(top) || char_TYPE.equals(top)) {
            mv.visitInsn(iInsn);
        } else if (long_TYPE.equals(top)) {
            mv.visitInsn(lInsn);
        } else if (float_TYPE.equals(top)) {
            mv.visitInsn(fInsn);
        } else if (double_TYPE.equals(top)) {
            mv.visitInsn(dInsn);
        }
    }

    @Override
    public void evaluateEqual(final BinaryExpression expression, final boolean defineVariable) {
        if (!defineVariable) {
            Expression leftExpression = expression.getLeftExpression();
            if (leftExpression instanceof PropertyExpression) {
                PropertyExpression pexp = (PropertyExpression) leftExpression;
                if (makeSetProperty(
                        pexp.getObjectExpression(),
                        pexp.getProperty(),
                        expression.getRightExpression(),
                        pexp.isSafe(),
                        pexp.isSpreadSafe(),
                        pexp.isImplicitThis(),
                        pexp instanceof AttributeExpression)) return;
            }
        }
        // GROOVY-5620: Spread safe/Null safe operator on LHS is not supported
        if (expression.getLeftExpression() instanceof PropertyExpression
                && ((PropertyExpression) expression.getLeftExpression()).isSpreadSafe()
                && StaticTypeCheckingSupport.isAssignment(expression.getOperation().getType())) {
            // rewrite it so that it can be statically compiled
            transformSpreadOnLHS(expression);
            return;
        }
        super.evaluateEqual(expression, defineVariable);
    }

    private void transformSpreadOnLHS(BinaryExpression origin) {
        PropertyExpression spreadExpression = (PropertyExpression) origin.getLeftExpression();
        Expression value = origin.getRightExpression();
        WriterController controller = getController();
        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();
        TypeChooser typeChooser = controller.getTypeChooser();
        OperandStack operandStack = controller.getOperandStack();
        ClassNode classNode = controller.getClassNode();
        int counter = labelCounter.incrementAndGet();
        Expression receiver = spreadExpression.getObjectExpression();

        // create an empty arraylist
        VariableExpression result = new VariableExpression(
                this.getClass().getSimpleName()+"$spreadresult" + counter,
                ARRAYLIST_CLASSNODE
        );
        ConstructorCallExpression cce = new ConstructorCallExpression(ARRAYLIST_CLASSNODE, ArgumentListExpression.EMPTY_ARGUMENTS);
        cce.setNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, ARRAYLIST_CONSTRUCTOR);
        DeclarationExpression declr = new DeclarationExpression(
                result,
                Token.newSymbol("=", spreadExpression.getLineNumber(), spreadExpression.getColumnNumber()),
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
        PropertyExpression pexp = spreadExpression instanceof AttributeExpression?
                new AttributeExpression(iteratorAsVar, spreadExpression.getProperty(), true):
                new PropertyExpression(iteratorAsVar, spreadExpression.getProperty(), true);
        pexp.setImplicitThis(spreadExpression.isImplicitThis());
        pexp.setSourcePosition(spreadExpression);
        BinaryExpression assignment = new BinaryExpression(
                pexp,
                origin.getOperation(),
                value
        );
        MethodCallExpression add = new MethodCallExpression(
                result,
                "add",
                assignment
        );
        add.setMethodTarget(ARRAYLIST_ADD_METHOD);
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

    }

    private boolean makeSetProperty(final Expression receiver, final Expression message, final Expression arguments, final boolean safe, final boolean spreadSafe, final boolean implicitThis, final boolean isAttribute) {
        WriterController controller = getController();
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode receiverType = typeChooser.resolveType(receiver, controller.getClassNode());
        String property = message.getText();
        boolean isThisExpression = receiver instanceof VariableExpression && ((VariableExpression) receiver).isThisExpression();
        if (isAttribute
                || (isThisExpression &&
                    receiverType.getDeclaredField(property)!=null)) {
            ClassNode current = receiverType;
            FieldNode fn = null;
            while (fn==null && current!=null) {
                fn = current.getDeclaredField(property);
                if (fn==null){
                    current = current.getSuperClass();
                }
            }
            if (fn!=null && receiverType!=current && !fn.isPublic()) {
                // check that direct access is allowed
                if (!fn.isProtected()) {
                    return false;
                }
                if (!Objects.equals(receiverType.getPackageName(), current.getPackageName())) {
                    return false;
                }
                OperandStack operandStack = controller.getOperandStack();
                MethodVisitor mv = controller.getMethodVisitor();
                if (!fn.isStatic()) {
                    receiver.visit(controller.getAcg());
                }
                arguments.visit(controller.getAcg());
                operandStack.doGroovyCast(fn.getOriginType());
                mv.visitFieldInsn(fn.isStatic() ? PUTSTATIC : PUTFIELD,
                        BytecodeHelper.getClassInternalName(fn.getOwner()),
                        property,
                        BytecodeHelper.getTypeDescription(fn.getOriginType()));
                operandStack.remove(fn.isStatic()?1:2);
                return true;
            }
        }
        if (!isAttribute) {
            String setter = "set" + MetaClassHelper.capitalize(property);
            MethodNode setterMethod = receiverType.getSetterMethod(setter, false);
            ClassNode declaringClass = setterMethod!=null?setterMethod.getDeclaringClass():null;
            if (isThisExpression && declaringClass!=null && declaringClass.equals(controller.getClassNode())) {
                // this.x = ... shouldn't use a setter if in the same class
                setterMethod = null;
            } else if (setterMethod == null) {
                PropertyNode propertyNode = receiverType.getProperty(property);
                if (propertyNode != null) {
                    int mods = propertyNode.getModifiers();
                    if (!Modifier.isFinal(mods)) {
                        setterMethod = new MethodNode(
                                setter,
                                ACC_PUBLIC,
                                ClassHelper.VOID_TYPE,
                                new Parameter[]{new Parameter(propertyNode.getOriginType(), "value")},
                                ClassNode.EMPTY_ARRAY,
                                EmptyStatement.INSTANCE
                        );
                        setterMethod.setDeclaringClass(receiverType);
                    }
                }
            }
            if (setterMethod != null) {
                Expression call = StaticPropertyAccessHelper.transformToSetterCall(
                        receiver,
                        setterMethod,
                        arguments,
                        implicitThis,
                        safe,
                        spreadSafe,
                        true, // to be replaced with a proper test whether a return value should be used or not
                        message
                );
                call.visit(controller.getAcg());
                return true;
            }
            if (isThisExpression && !controller.isInClosure()) {
                receiverType = controller.getClassNode();
            }
            if (makeSetPrivateFieldWithBridgeMethod(receiver, receiverType, property, arguments, safe, spreadSafe, implicitThis)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean makeSetPrivateFieldWithBridgeMethod(final Expression receiver, final ClassNode receiverType, final String fieldName, final Expression arguments, final boolean safe, final boolean spreadSafe, final boolean implicitThis) {
        WriterController controller = getController();
        FieldNode field = receiverType.getField(fieldName);
        ClassNode outerClass = receiverType.getOuterClass();
        if (field == null && implicitThis && outerClass != null && !receiverType.isStaticClass()) {
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
            return makeSetPrivateFieldWithBridgeMethod(pexp, outerClass, fieldName, arguments, safe, spreadSafe, true);
        }
        ClassNode classNode = controller.getClassNode();
        if (field != null && Modifier.isPrivate(field.getModifiers())
            && (StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(receiverType, classNode) || StaticInvocationWriter.isPrivateBridgeMethodsCallAllowed(classNode,receiverType))
            && !receiverType.equals(classNode)) {
            Map<String, MethodNode> mutators = receiverType.redirect().getNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_FIELDS_MUTATORS);
            if (mutators != null) {
                MethodNode methodNode = mutators.get(fieldName);
                if (methodNode != null) {
                    MethodCallExpression mce = new MethodCallExpression(receiver, methodNode.getName(),
                        new ArgumentListExpression(field.isStatic()?new ConstantExpression(null):receiver, arguments));
                        mce.setMethodTarget(methodNode);
                        mce.setSafe(safe);
                        mce.setSpreadSafe(spreadSafe);
                    mce.setImplicitThis(implicitThis);
                    mce.visit(controller.getAcg());
                    return true;
                }
            }
        }
        return false;
    }

    protected void assignToArray(Expression parent, Expression receiver, Expression index, Expression rhsValueLoader) {
        ClassNode current = getController().getClassNode();
        ClassNode arrayType = getController().getTypeChooser().resolveType(receiver, current);
        ClassNode arrayComponentType = arrayType.getComponentType();
        int operationType = getOperandType(arrayComponentType);
        BinaryExpressionWriter bew = binExpWriter[operationType];

        if (bew.arraySet(true) && arrayType.isArray()) {
            super.assignToArray(parent, receiver, index, rhsValueLoader);
        } else {
            /******
            / This code path is needed because ACG creates array access expressions
            *******/

            WriterController controller = getController();
            // let's replace this assignment to a subscript operator with a
            // method call
            // e.g. x[5] = 10
            // -> (x, [], 5), =, 10
            // -> methodCall(x, "putAt", [5, 10])
            ArgumentListExpression ae = new ArgumentListExpression(index, rhsValueLoader);
            if (rhsValueLoader instanceof VariableSlotLoader && parent instanceof BinaryExpression) {
                // GROOVY-6061
                rhsValueLoader.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE,
                        controller.getTypeChooser().resolveType(parent, controller.getClassNode()));
            }
            MethodCallExpression mce = new MethodCallExpression(receiver, "putAt", ae);
            mce.setSourcePosition(parent);

            OperandStack operandStack = controller.getOperandStack();
            int height = operandStack.getStackLength();
            mce.visit(controller.getAcg());
            operandStack.pop();
            operandStack.remove(operandStack.getStackLength()-height);

            // return value of assignment
            rhsValueLoader.visit(controller.getAcg());
        }
    }

}
