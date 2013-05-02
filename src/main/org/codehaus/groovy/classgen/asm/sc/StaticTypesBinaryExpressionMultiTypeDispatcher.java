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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.concurrent.atomic.AtomicInteger;

import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.*;

/**
 * A specialized version of the multi type binary expression dispatcher which is aware of static compilation.
 * It is able to generate optimized bytecode for some operations using JVM instructions when available.
 *
 * @author Cedric Champeau
 */
public class StaticTypesBinaryExpressionMultiTypeDispatcher extends BinaryExpressionMultiTypeDispatcher implements Opcodes {

    private final AtomicInteger labelCounter = new AtomicInteger();


    public StaticTypesBinaryExpressionMultiTypeDispatcher(WriterController wc) {
        super(wc);
    }

    private int incValue(Token token) {
        switch (token.getType()) {
            case Types.PLUS_PLUS:
                return 1;
            case Types.MINUS_MINUS:
                return -1;
        }
        return 0;
    }

 /*   @Override
    public void evaluatePrefixMethod(final PrefixExpression expression) {
        final Expression src = expression.getExpression();
        ListOfExpressionsExpression list = new ListOfExpressionsExpression();
        list.addExpression(new BinaryExpression(
                src,
                Token.newSymbol(Types.EQUAL, -1, -1),
                new BinaryExpression(
                        src,
                        Token.newSymbol(Types.MINUS, -1, -1),
                        new ConstantExpression(1)
                )
        ));
        list.addExpression(src);
        list.setSourcePosition(expression);
        list.visit(getController().getAcg());
        if (true) return;
        if (src instanceof VariableExpression) {
            final WriterController controller = getController();
            final ClassNode type = controller.getTypeChooser().resolveType(src, controller.getClassNode());
            if (ClassHelper.isPrimitiveType(type) && ClassHelper.isNumberType(type)) {
                BytecodeExpression bytecode = new BytecodeExpression() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        BytecodeVariable variable = controller.getCompileStack().getVariable(((VariableExpression) src).getName(), true);
                        if (WideningCategories.isIntCategory(type)) {
                            mv.visitIincInsn(variable.getIndex(), incValue(expression.getOperation()));
                            mv.visitIntInsn(ILOAD, variable.getIndex());
                        } else if (WideningCategories.isLongCategory(type)) {
                            mv.visitIntInsn(LLOAD, variable.getIndex());
                            mv.visitInsn(LCONST_1);
                            mv.visitInsn(incValue(expression.getOperation())<0?LSUB:LADD);
                            mv.visitVarInsn(LSTORE, variable.getIndex());
                            mv.visitVarInsn(LLOAD, variable.getIndex());
                        }
                    }
                };
                bytecode.setType(type);
                bytecode.visit(controller.getAcg());
                return;
            }
        }
        super.evaluatePrefixMethod(expression);
    }

    @Override
    public void evaluatePostfixMethod(final PostfixExpression expression) {
        final Expression src = expression.getExpression();
        if (src instanceof VariableExpression) {
            final WriterController controller = getController();
            final ClassNode type = controller.getTypeChooser().resolveType(src, controller.getClassNode());
            if (ClassHelper.isPrimitiveType(type) && ClassHelper.isNumberType(type)) {
                BytecodeExpression bytecode = new BytecodeExpression() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        BytecodeVariable variable = controller.getCompileStack().getVariable(((VariableExpression) src).getName(), true);
                        if (WideningCategories.isIntCategory(type)) {
                            mv.visitIntInsn(ILOAD, variable.getIndex());
                            mv.visitIincInsn(variable.getIndex(), incValue(expression.getOperation()));
                        } else if (WideningCategories.isLongCategory(type)) {
                            mv.visitIntInsn(LLOAD, variable.getIndex());
                            mv.visitInsn(DUP2);
                            mv.visitInsn(LCONST_1);
                            mv.visitInsn(incValue(expression.getOperation())<0?LSUB:LADD);
                            mv.visitVarInsn(LSTORE, variable.getIndex());
                        }
                    }
                };
                bytecode.setType(type);
                bytecode.visit(controller.getAcg());
                return;
            }
        }
        super.evaluatePostfixMethod(expression);
    }
*/
    @Override
    protected void writePostOrPrefixMethod(int op, String method, Expression expression, Expression orig) {
        MethodNode mn = (MethodNode) orig.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
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
            if (WideningCategories.isIntCategory(top) || char_TYPE.equals(top)) {
                mv.visitInsn(ICONST_1);
            } else if (long_TYPE.equals(top)) {
                mv.visitInsn(LCONST_1);
            } else if (float_TYPE.equals(top)) {
                mv.visitInsn(FCONST_1);
            } else if (double_TYPE.equals(top)) {
                mv.visitInsn(DCONST_1);
            }
            if ("next".equals(method)) {
                if (WideningCategories.isIntCategory(top) || char_TYPE.equals(top)) {
                    mv.visitInsn(IADD);
                } else if (long_TYPE.equals(top)) {
                    mv.visitInsn(LADD);
                } else if (float_TYPE.equals(top)) {
                    mv.visitInsn(FADD);
                } else if (double_TYPE.equals(top)) {
                    mv.visitInsn(DADD);
                }
            } else {
                if (WideningCategories.isIntCategory(top) || char_TYPE.equals(top)) {
                    mv.visitInsn(ISUB);
                } else if (long_TYPE.equals(top)) {
                    mv.visitInsn(LSUB);
                } else if (float_TYPE.equals(top)) {
                    mv.visitInsn(FSUB);
                } else if (double_TYPE.equals(top)) {
                    mv.visitInsn(DSUB);
                }
            }
            return;
        }
        super.writePostOrPrefixMethod(op, method, expression, orig);
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
        if (isAttribute
                || ((receiver instanceof VariableExpression && ((VariableExpression) receiver).isThisExpression()) &&
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
                String pkg1 = receiverType.getPackageName();
                String pkg2 = current.getPackageName();
                if (pkg1!=pkg2 && !pkg1.equals(pkg2)) {
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
        } else {
            String setter = "set" + MetaClassHelper.capitalize(property);
            MethodNode setterMethod = receiverType.getSetterMethod(setter);
            if (setterMethod == null) {
                PropertyNode propertyNode = receiverType.getProperty(property);
                if (propertyNode != null) {
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
            if (setterMethod != null) {
                MethodCallExpression call = new MethodCallExpression(
                        receiver,
                        setter,
                        arguments
                );
                call.setImplicitThis(implicitThis);
                call.setSafe(safe);
                call.setSpreadSafe(spreadSafe);
                call.setMethodTarget(setterMethod);
                call.visit(controller.getAcg());
                return true;
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
        AsmClassGenerator acg = getController().getAcg();

        if (bew.arraySet(true) && arrayType.isArray()) {
            OperandStack operandStack   =   getController().getOperandStack();

            // load the array
            receiver.visit(acg);
            operandStack.doGroovyCast(arrayType);

            // load index
            index.visit(acg);
            operandStack.doGroovyCast(int_TYPE);

            // load rhs
            rhsValueLoader.visit(acg);
            operandStack.doGroovyCast(arrayComponentType);

            // store value in array
            bew.arraySet(false);

            // load return value && correct operand stack stack
            operandStack.remove(3);
            rhsValueLoader.visit(acg);
        } else {
            WriterController controller = getController();
            StaticTypeCheckingVisitor visitor = new StaticCompilationVisitor(controller.getSourceUnit(), controller.getClassNode());
            // let's replace this assignment to a subscript operator with a
            // method call
            // e.g. x[5] = 10
            // -> (x, [], 5), =, 10
            // -> methodCall(x, "putAt", [5, 10])
            ArgumentListExpression ae = new ArgumentListExpression(index, rhsValueLoader);
            if (rhsValueLoader instanceof VariableSlotLoader && parent instanceof BinaryExpression) {
                // GROOVY-6061
                Expression right = ((BinaryExpression) parent).getRightExpression();
                rhsValueLoader.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE,
                        controller.getTypeChooser().resolveType(right, controller.getClassNode()));
            }
            MethodCallExpression mce = new MethodCallExpression(
                    receiver,
                    "putAt",
                    ae
            );
            mce.setSourcePosition(parent);
            visitor.visitMethodCallExpression(mce);
            mce.visit(controller.getAcg());
            // return value of assignment
            rhsValueLoader.visit(controller.getAcg());
        }
    }

}
