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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.syntax.Types.ASSIGN;
import static org.codehaus.groovy.syntax.Types.BITWISE_AND;
import static org.codehaus.groovy.syntax.Types.BITWISE_AND_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN;
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_IDENTICAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN;
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_IDENTICAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_IN;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_INSTANCEOF;
import static org.codehaus.groovy.syntax.Types.COMPARE_TO;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.DIVIDE_EQUAL;
import static org.codehaus.groovy.syntax.Types.ELVIS_EQUAL;
import static org.codehaus.groovy.syntax.Types.EQUAL;
import static org.codehaus.groovy.syntax.Types.FIND_REGEX;
import static org.codehaus.groovy.syntax.Types.INTDIV;
import static org.codehaus.groovy.syntax.Types.INTDIV_EQUAL;
import static org.codehaus.groovy.syntax.Types.KEYWORD_IN;
import static org.codehaus.groovy.syntax.Types.KEYWORD_INSTANCEOF;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET;
import static org.codehaus.groovy.syntax.Types.LOGICAL_AND;
import static org.codehaus.groovy.syntax.Types.LOGICAL_OR;
import static org.codehaus.groovy.syntax.Types.MATCH_REGEX;
import static org.codehaus.groovy.syntax.Types.MINUS;
import static org.codehaus.groovy.syntax.Types.MINUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.MINUS_MINUS;
import static org.codehaus.groovy.syntax.Types.MOD;
import static org.codehaus.groovy.syntax.Types.MOD_EQUAL;
import static org.codehaus.groovy.syntax.Types.MULTIPLY;
import static org.codehaus.groovy.syntax.Types.MULTIPLY_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS;
import static org.codehaus.groovy.syntax.Types.PLUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS_PLUS;
import static org.codehaus.groovy.syntax.Types.POWER;
import static org.codehaus.groovy.syntax.Types.POWER_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED_EQUAL;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INSTANCEOF;

public class BinaryExpressionHelper {
    // compare
    private static final MethodCaller compareIdenticalMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareIdentical");
    private static final MethodCaller compareNotIdenticalMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareNotIdentical");
    private static final MethodCaller compareEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareEqual");
    private static final MethodCaller compareNotEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareNotEqual");
    private static final MethodCaller compareToMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareTo");
    private static final MethodCaller compareLessThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThan");
    private static final MethodCaller compareLessThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThanEqual");
    private static final MethodCaller compareGreaterThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThan");
    private static final MethodCaller compareGreaterThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThanEqual");
    // regexp
    private static final MethodCaller findRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "findRegex");
    private static final MethodCaller matchRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "matchRegex");
    // isCase/isNotCase
    private static final MethodCaller isCaseMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "isCase");
    private static final MethodCaller isNotCaseMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "isNotCase");

    protected final WriterController controller;
    private final UnaryExpressionHelper unaryExpressionHelper;

    public BinaryExpressionHelper(final WriterController wc) {
        this.controller = wc;
        this.unaryExpressionHelper = new UnaryExpressionHelper(this.controller);
    }

    public WriterController getController() {
        return controller;
    }

    public MethodCaller getIsCaseMethod() {
        return isCaseMethod;
    }

    public void eval(final BinaryExpression expression) {
        switch (expression.getOperation().getType()) {
        case EQUAL: // = (aka assignment)
            evaluateEqual(expression, false);
            break;

        case COMPARE_EQUAL: // ==
            evaluateCompareExpression(compareEqualMethod, expression);
            break;

        case COMPARE_NOT_EQUAL:
            evaluateCompareExpression(compareNotEqualMethod, expression);
            break;

        case COMPARE_TO:
            evaluateCompareTo(expression);
            break;

        case COMPARE_GREATER_THAN:
            evaluateCompareExpression(compareGreaterThanMethod, expression);
            break;

        case COMPARE_GREATER_THAN_EQUAL:
            evaluateCompareExpression(compareGreaterThanEqualMethod, expression);
            break;

        case COMPARE_LESS_THAN:
            evaluateCompareExpression(compareLessThanMethod, expression);
            break;

        case COMPARE_LESS_THAN_EQUAL:
            evaluateCompareExpression(compareLessThanEqualMethod, expression);
            break;

        case LOGICAL_AND:
            evaluateLogicalAndExpression(expression);
            break;

        case LOGICAL_OR:
            evaluateLogicalOrExpression(expression);
            break;

        case BITWISE_AND:
            evaluateBinaryExpression("and", expression);
            break;

        case BITWISE_AND_EQUAL:
            evaluateBinaryExpressionWithAssignment("and", expression);
            break;

        case BITWISE_OR:
            evaluateBinaryExpression("or", expression);
            break;

        case BITWISE_OR_EQUAL:
            evaluateBinaryExpressionWithAssignment("or", expression);
            break;

        case BITWISE_XOR:
            evaluateBinaryExpression("xor", expression);
            break;

        case BITWISE_XOR_EQUAL:
            evaluateBinaryExpressionWithAssignment("xor", expression);
            break;

        case PLUS:
            evaluateBinaryExpression("plus", expression);
            break;

        case PLUS_EQUAL:
            evaluateBinaryExpressionWithAssignment("plus", expression);
            break;

        case MINUS:
            evaluateBinaryExpression("minus", expression);
            break;

        case MINUS_EQUAL:
            evaluateBinaryExpressionWithAssignment("minus", expression);
            break;

        case MULTIPLY:
            evaluateBinaryExpression("multiply", expression);
            break;

        case MULTIPLY_EQUAL:
            evaluateBinaryExpressionWithAssignment("multiply", expression);
            break;

        case DIVIDE:
            evaluateBinaryExpression("div", expression);
            break;

        case DIVIDE_EQUAL:
            //SPG don't use divide since BigInteger implements directly
            //and we want to dispatch through DefaultGroovyMethods to get a BigDecimal result
            evaluateBinaryExpressionWithAssignment("div", expression);
            break;

        case INTDIV:
            evaluateBinaryExpression("intdiv", expression);
            break;

        case INTDIV_EQUAL:
            evaluateBinaryExpressionWithAssignment("intdiv", expression);
            break;

        case MOD:
            evaluateBinaryExpression("mod", expression);
            break;

        case MOD_EQUAL:
            evaluateBinaryExpressionWithAssignment("mod", expression);
            break;

        case POWER:
            evaluateBinaryExpression("power", expression);
            break;

        case POWER_EQUAL:
            evaluateBinaryExpressionWithAssignment("power", expression);
            break;

        case ELVIS_EQUAL:
            evaluateElvisEqual(expression);
            break;

        case LEFT_SHIFT:
            evaluateBinaryExpression("leftShift", expression);
            break;

        case LEFT_SHIFT_EQUAL:
            evaluateBinaryExpressionWithAssignment("leftShift", expression);
            break;

        case RIGHT_SHIFT:
            evaluateBinaryExpression("rightShift", expression);
            break;

        case RIGHT_SHIFT_EQUAL:
            evaluateBinaryExpressionWithAssignment("rightShift", expression);
            break;

        case RIGHT_SHIFT_UNSIGNED:
            evaluateBinaryExpression("rightShiftUnsigned", expression);
            break;

        case RIGHT_SHIFT_UNSIGNED_EQUAL:
            evaluateBinaryExpressionWithAssignment("rightShiftUnsigned", expression);
            break;

        case KEYWORD_INSTANCEOF:
            evaluateInstanceof(expression);
            break;

        case COMPARE_NOT_INSTANCEOF:
            evaluateNotInstanceof(expression);
            break;

        case FIND_REGEX:
            evaluateCompareExpression(findRegexMethod, expression);
            break;

        case MATCH_REGEX:
            evaluateCompareExpression(matchRegexMethod, expression);
            break;

        case LEFT_SQUARE_BRACKET:
            if (controller.getCompileStack().isLHS()) {
                evaluateEqual(expression, false);
            } else {
                evaluateBinaryExpression("getAt", expression);
            }
            break;

        case KEYWORD_IN:
            evaluateCompareExpression(isCaseMethod, expression);
            break;

        case COMPARE_NOT_IN:
            evaluateCompareExpression(isNotCaseMethod, expression);
            break;

        case COMPARE_IDENTICAL:
            evaluateCompareExpression(compareIdenticalMethod, expression);
            break;

        case COMPARE_NOT_IDENTICAL:
            evaluateCompareExpression(compareNotIdenticalMethod, expression);
            break;

        default:
            throw new GroovyBugError("Operation: " + expression.getOperation() + " not supported");
        }
    }

    @Deprecated
    protected void assignToArray(final Expression parent, final Expression receiver, final Expression index, final Expression rhsValueLoader) {
        assignToArray(parent, receiver, index, rhsValueLoader, false);
    }

    protected void assignToArray(final Expression parent, final Expression receiver, final Expression index, final Expression rhsValueLoader, final boolean safe) {
        // let's replace this assignment to a subscript operator with a
        // method call
        // e.g. x[5] = 10
        // -> (x, [], 5), =, 10
        // -> methodCall(x, "putAt", [5, 10])
        ArgumentListExpression ae = new ArgumentListExpression(index,rhsValueLoader);
        controller.getInvocationWriter().makeCall(parent, receiver, constX("putAt"), ae, InvocationWriter.invokeMethod, safe, false, false);
        controller.getOperandStack().pop();
        // return value of assignment
        rhsValueLoader.visit(controller.getAcg());
    }

    public void evaluateElvisEqual(final BinaryExpression expression) {
        Token operation = expression.getOperation();
        BinaryExpression elvisAssignmentExpression = binX(
                expression.getLeftExpression(),
                Token.newSymbol(ASSIGN, operation.getStartLine(), operation.getStartColumn()),
                new ElvisOperatorExpression(expression.getLeftExpression(), expression.getRightExpression())
        );
        this.evaluateEqual(elvisAssignmentExpression, false);
    }

    public void evaluateEqual(final BinaryExpression expression, final boolean defineVariable) {
        AsmClassGenerator acg = controller.getAcg();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        Expression leftExpression = expression.getLeftExpression();
        Expression rightExpression = expression.getRightExpression();
        boolean directAssignment = defineVariable && !(leftExpression instanceof TupleExpression);

        if (directAssignment && rightExpression instanceof EmptyExpression) {
            VariableExpression ve = (VariableExpression) leftExpression;
            BytecodeVariable var = compileStack.defineVariable(ve, controller.getTypeChooser().resolveType(ve, controller.getClassNode()), false);
            operandStack.loadOrStoreVariable(var, false);
            return;
        }
        // evaluate the RHS and store the result
        // TODO: LHS has not been visited, it could be a variable in a closure and type chooser is not aware.
        ClassNode lhsType = controller.getTypeChooser().resolveType(leftExpression, controller.getClassNode());
        if (rightExpression instanceof ListExpression && lhsType.isArray()) {
            ListExpression list = (ListExpression) rightExpression;
            ArrayExpression array = new ArrayExpression(lhsType.getComponentType(), list.getExpressions());
            array.setSourcePosition(list);
            array.visit(acg);
        } else if (rightExpression instanceof EmptyExpression) {
            loadInitValue(leftExpression.getType()); // TODO: lhsType?
        } else {
            rightExpression.visit(acg);
        }

        ClassNode rhsType = operandStack.getTopOperand();
        int rhsValueId;

        if (directAssignment) {
            VariableExpression var = (VariableExpression) leftExpression;
            if (var.isClosureSharedVariable() && ClassHelper.isPrimitiveType(rhsType)) {
                // GROOVY-5570: if a closure shared variable is a primitive type, it must be boxed
                rhsType = ClassHelper.getWrapper(rhsType);
                operandStack.box();
            }

            // ensure we try to unbox null to cause a runtime NPE in case we assign
            // null to a primitive typed variable, even if it is used only in boxed
            // form as it is closure shared
            if (var.isClosureSharedVariable() && ClassHelper.isPrimitiveType(var.getOriginType()) && isNullConstant(rightExpression)) {
                operandStack.doGroovyCast(var.getOriginType());
                // these two are never reached in bytecode and only there
                // to avoid verify errors and compiler infrastructure hazzle
                operandStack.box();
                operandStack.doGroovyCast(lhsType);
            }
            // normal type transformation
            if (!ClassHelper.isPrimitiveType(lhsType) && isNullConstant(rightExpression)) {
                operandStack.replace(lhsType);
            } else {
                operandStack.doGroovyCast(lhsType);
            }
            rhsType = lhsType;
            rhsValueId = compileStack.defineVariable(var, lhsType, true).getIndex();
        } else {
            rhsValueId = compileStack.defineTemporaryVariable("$rhs", rhsType, true);
        }
        // TODO: if rhs is VariableSlotLoader already, then skip crating a new one
        BytecodeExpression rhsValueLoader = new VariableSlotLoader(rhsType,rhsValueId,operandStack);

        // assignment for subscript
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == LEFT_SQUARE_BRACKET) {
                assignToArray(expression, leftBinExpr.getLeftExpression(), leftBinExpr.getRightExpression(), rhsValueLoader, leftBinExpr.isSafe());
            }
            compileStack.removeVar(rhsValueId);
            return;
        }

        compileStack.pushLHS(true);

        if (leftExpression instanceof TupleExpression) {
            // multiple declaration
            TupleExpression tuple = (TupleExpression) leftExpression;
            int i = 0;
            for (Expression e : tuple.getExpressions()) {
                callX(rhsValueLoader, "getAt", args(constX(i++))).visit(acg);
                if (defineVariable) {
                    Variable v = (Variable) e;
                    operandStack.doGroovyCast(v);
                    compileStack.defineVariable(v, true);
                    operandStack.remove(1);
                } else {
                    e.visit(acg);
                }
            }
        } else if (defineVariable) {
            // single declaration
            rhsValueLoader.visit(acg);
            operandStack.remove(1);
            compileStack.popLHS();
            return;
        } else {
            // normal assignment
            int mark = operandStack.getStackLength();
            rhsValueLoader.visit(acg);
            leftExpression.visit(acg);
            operandStack.remove(operandStack.getStackLength() - mark);
        }
        compileStack.popLHS();

        // return value of assignment
        rhsValueLoader.visit(acg);
        compileStack.removeVar(rhsValueId);
    }

    private void loadInitValue(final ClassNode type) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (ClassHelper.isPrimitiveType(type)) {
            mv.visitLdcInsn(0);
        } else {
            mv.visitInsn(ACONST_NULL);
        }
        controller.getOperandStack().push(type);
    }

    protected void evaluateCompareExpression(final MethodCaller compareMethod, final BinaryExpression expression) {
        ClassNode classNode = controller.getClassNode();
        Expression leftExp = expression.getLeftExpression();
        Expression rightExp = expression.getRightExpression();
        ClassNode leftType = controller.getTypeChooser().resolveType(leftExp, classNode);
        ClassNode rightType = controller.getTypeChooser().resolveType(rightExp, classNode);

        boolean done = false;
        if (ClassHelper.isPrimitiveType(leftType) && ClassHelper.isPrimitiveType(rightType)) {
            BinaryExpressionMultiTypeDispatcher helper = new BinaryExpressionMultiTypeDispatcher(controller);
            done = helper.doPrimitiveCompare(leftType, rightType, expression);
        }
        if (!done) {
            AsmClassGenerator acg = controller.getAcg();
            OperandStack operandStack = controller.getOperandStack();

            leftExp.visit(acg);
            operandStack.box();
            rightExp.visit(acg);
            operandStack.box();

            compareMethod.call(controller.getMethodVisitor());
            ClassNode resType = ClassHelper.boolean_TYPE;
            if (compareMethod == findRegexMethod) {
                resType = ClassHelper.OBJECT_TYPE;
            }
            operandStack.replace(resType, 2);
        }
    }

    private void evaluateCompareTo(final BinaryExpression expression) {
        AsmClassGenerator acg = controller.getAcg();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        expression.getLeftExpression().visit(acg);
        operandStack.box();

        // if the right hand side is a boolean expression, we need to autobox
        expression.getRightExpression().visit(acg);
        operandStack.box();

        compareToMethod.call(mv);
        operandStack.replace(ClassHelper.Integer_TYPE, 2);
    }

    private void evaluateLogicalAndExpression(final BinaryExpression expression) {
        AsmClassGenerator acg = controller.getAcg();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        expression.getLeftExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        Label falseCase = operandStack.jump(IFEQ);

        expression.getRightExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        operandStack.jump(IFEQ, falseCase);

        ConstantExpression.PRIM_TRUE.visit(acg);
        Label trueCase = new Label();
        mv.visitJumpInsn(GOTO, trueCase);

        mv.visitLabel(falseCase);
        ConstantExpression.PRIM_FALSE.visit(acg);

        mv.visitLabel(trueCase);
        operandStack.remove(1); // have to remove 1 because of the GOTO
    }

    private void evaluateLogicalOrExpression(final BinaryExpression expression) {
        AsmClassGenerator acg = controller.getAcg();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        expression.getLeftExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        Label trueCase = operandStack.jump(IFNE);

        expression.getRightExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        Label falseCase = operandStack.jump(IFEQ);

        mv.visitLabel(trueCase);
        ConstantExpression.PRIM_TRUE.visit(acg);
        Label end = new Label();
        operandStack.jump(GOTO, end);

        mv.visitLabel(falseCase);
        ConstantExpression.PRIM_FALSE.visit(acg);

        mv.visitLabel(end);
    }

    protected void evaluateBinaryExpression(final String message, final BinaryExpression expression) {
        CompileStack compileStack = controller.getCompileStack();
        // ensure VariableArguments are read, not stored
        compileStack.pushLHS(false);
        controller.getInvocationWriter().makeSingleArgumentCall(
                expression.getLeftExpression(),
                message,
                expression.getRightExpression(),
                expression.isSafe()
        );
        compileStack.popLHS();
    }

    protected void evaluateArrayAssignmentWithOperator(final String method, final BinaryExpression expression, final BinaryExpression leftBinExpr) {
        // e.g. x[a] += b
        // to avoid loading x and a twice we transform the expression to use
        // ExpressionAsVariableSlot
        // -> subscript=a, receiver=x, receiver[subscript]+b, =, receiver[subscript]
        // -> subscript=a, receiver=x, receiver#getAt(subscript)#plus(b), =, receiver#putAt(subscript)
        // -> subscript=a, receiver=x, receiver#putAt(subscript, receiver#getAt(subscript)#plus(b))
        // the result of x[a] += b is x[a]+b, thus:
        // -> subscript=a, receiver=x, receiver#putAt(subscript, ret=receiver#getAt(subscript)#plus(b)), ret
        ExpressionAsVariableSlot subscript = new ExpressionAsVariableSlot(controller, leftBinExpr.getRightExpression(), "subscript");
        ExpressionAsVariableSlot receiver  = new ExpressionAsVariableSlot(controller, leftBinExpr.getLeftExpression(), "receiver");
        MethodCallExpression getAt = callX(receiver, "getAt", args(subscript));
        MethodCallExpression operation = callX(getAt, method, expression.getRightExpression());
        ExpressionAsVariableSlot ret = new ExpressionAsVariableSlot(controller, operation, "ret");
        MethodCallExpression putAt = callX(receiver, "putAt", args(subscript, ret));

        AsmClassGenerator acg = controller.getAcg();
        putAt.visit(acg);
        OperandStack os = controller.getOperandStack();
        os.pop();
        os.load(ret.getType(), ret.getIndex());

        CompileStack compileStack = controller.getCompileStack();
        compileStack.removeVar(ret.getIndex());
        compileStack.removeVar(subscript.getIndex());
        compileStack.removeVar(receiver.getIndex());
    }

    protected void evaluateBinaryExpressionWithAssignment(final String method, final BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression bexp = (BinaryExpression) leftExpression;
            if (bexp.getOperation().getType() == LEFT_SQUARE_BRACKET) {
                evaluateArrayAssignmentWithOperator(method, expression, bexp);
                return;
            }
        }

        evaluateBinaryExpression(method, expression);

        // br to leave a copy of rvalue on the stack; see also isPopRequired()
        controller.getOperandStack().dup();
        controller.getCompileStack().pushLHS(true);
        leftExpression.visit(controller.getAcg());
        controller.getCompileStack().popLHS();
    }

    private void evaluateInstanceof(final BinaryExpression expression) {
        expression.getLeftExpression().visit(controller.getAcg());
        controller.getOperandStack().box();
        Expression rightExp = expression.getRightExpression();
        if (!(rightExp instanceof ClassExpression)) {
            throw new RuntimeException("RHS of the instanceof keyword must be a class name, not: " + rightExp);
        }
        String classInternalName = BytecodeHelper.getClassInternalName(rightExp.getType());
        controller.getMethodVisitor().visitTypeInsn(INSTANCEOF, classInternalName);
        controller.getOperandStack().replace(ClassHelper.boolean_TYPE);
    }

    private void evaluateNotInstanceof(final BinaryExpression expression) {
        unaryExpressionHelper.writeNotExpression(
                new NotExpression(
                        new BinaryExpression(
                                expression.getLeftExpression(),
                                GeneralUtils.INSTANCEOF,
                                expression.getRightExpression()
                        )
                )
        );
    }

    private void evaluatePostfixMethod(final int op, final String method, final Expression expression, final Expression orig) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();

        // load Expressions
        VariableSlotLoader usesSubscript = loadWithSubscript(expression);

        // save copy for later
        operandStack.dup();
        ClassNode expressionType = operandStack.getTopOperand();
        int tempIdx = compileStack.defineTemporaryVariable("postfix_" + method, expressionType, true);

        // execute method
        execMethodAndStoreForSubscriptOperator(op, method, expression, usesSubscript, orig);

        // remove the result of the method call
        operandStack.pop();

        // reload saved value
        operandStack.load(expressionType, tempIdx);
        compileStack.removeVar(tempIdx);
        if (usesSubscript != null) compileStack.removeVar(usesSubscript.getIndex());
    }

    public void evaluatePostfixMethod(final PostfixExpression expression) {
        int op = expression.getOperation().getType();
        switch (op) {
            case PLUS_PLUS:
                evaluatePostfixMethod(op, "next", expression.getExpression(), expression);
                break;
            case MINUS_MINUS:
                evaluatePostfixMethod(op, "previous", expression.getExpression(), expression);
                break;
        }
    }

    public void evaluatePrefixMethod(final PrefixExpression expression) {
        int type = expression.getOperation().getType();
        switch (type) {
            case PLUS_PLUS:
                evaluatePrefixMethod(type, "next", expression.getExpression(), expression);
                break;
            case MINUS_MINUS:
                evaluatePrefixMethod(type, "previous", expression.getExpression(), expression);
                break;
        }
    }

    private void evaluatePrefixMethod(final int op, final String method, final Expression expression, final Expression orig) {
        // load expressions
        VariableSlotLoader usesSubscript = loadWithSubscript(expression);

        // execute method
        execMethodAndStoreForSubscriptOperator(op, method, expression, usesSubscript, orig);

        // new value is already on stack, so nothing to do here
        if (usesSubscript != null) controller.getCompileStack().removeVar(usesSubscript.getIndex());
    }

    private VariableSlotLoader loadWithSubscript(final Expression expression) {
        AsmClassGenerator acg = controller.getAcg();
        // if we have a BinaryExpression, check if it is with subscription
        if (expression instanceof BinaryExpression) {
            BinaryExpression bexp = (BinaryExpression) expression;
            if (bexp.getOperation().getType() == LEFT_SQUARE_BRACKET) {
                // right expression is the subscript expression
                // we store the result of the subscription on the stack
                Expression subscript = bexp.getRightExpression();
                subscript.visit(acg);
                OperandStack operandStack = controller.getOperandStack();
                ClassNode subscriptType = operandStack.getTopOperand();
                if (subscriptType.isGenericsPlaceHolder() || GenericsUtils.hasPlaceHolders(subscriptType)) {
                    subscriptType = controller.getTypeChooser().resolveType(bexp, controller.getClassNode());
                }
                int id = controller.getCompileStack().defineTemporaryVariable("$subscript", subscriptType, true);
                VariableSlotLoader subscriptExpression = new VariableSlotLoader(subscriptType, id, operandStack);
                BinaryExpression rewrite = binX(bexp.getLeftExpression(), bexp.getOperation(), subscriptExpression);
                rewrite.copyNodeMetaData(bexp);
                rewrite.setSourcePosition(bexp);
                rewrite.visit(acg);
                return subscriptExpression;
            }
        }

        // normal loading of expression
        expression.visit(acg);
        return null;
    }

    private void execMethodAndStoreForSubscriptOperator(final int op, String method, final Expression expression, final VariableSlotLoader usesSubscript, final Expression orig) {
        writePostOrPrefixMethod(op, method, expression, orig);

        // we need special code for arrays to store the result (like for a[1]++)
        if (usesSubscript != null) {
            BinaryExpression be = (BinaryExpression) expression;
            CompileStack compileStack = controller.getCompileStack();
            OperandStack operandStack = controller.getOperandStack();
            ClassNode methodResultType = operandStack.getTopOperand();
            int resultIdx = compileStack.defineTemporaryVariable("postfix_" + method, methodResultType, true);
            BytecodeExpression methodResultLoader = new VariableSlotLoader(methodResultType, resultIdx, operandStack);

            // execute the assignment, this will leave the right side (here the method call result) on the stack
            assignToArray(be, be.getLeftExpression(), usesSubscript, methodResultLoader, be.isSafe());

            compileStack.removeVar(resultIdx);

        } else if (expression instanceof VariableExpression || expression instanceof PropertyExpression || expression instanceof FieldExpression) {
            // here we handle a++ and a.b++
            controller.getOperandStack().dup();
            controller.getCompileStack().pushLHS(true);
            expression.visit(controller.getAcg());
            controller.getCompileStack().popLHS();
        }
        // other cases don't need storing, so nothing to be done for them
    }

    protected void writePostOrPrefixMethod(final int op, final String method, final Expression expression, final Expression orig) {
        // at this point the receiver will be already on the stack
        // in a[1]++ the method will be "++" aka "next" and the receiver a[1]
        ClassNode exprType = controller.getTypeChooser().resolveType(expression, controller.getClassNode());
        Expression callSiteReceiverSwap = new BytecodeExpression(exprType) {
            @Override
            public void visit(MethodVisitor mv) {
                OperandStack operandStack = controller.getOperandStack();
                // CallSite is normally not showing up on the
                // operandStack, so we place a dummy here with same
                // slot length.
                operandStack.push(ClassHelper.OBJECT_TYPE);
                // change (receiver,callsite) to (callsite,receiver)
                operandStack.swap();

                setType(operandStack.getTopOperand());

                // no need to keep any of those on the operand stack
                // after this expression is processed, the operand stack
                // will contain callSiteReceiverSwap.getType()
                operandStack.remove(2);
            }
        };
        // execute method
        // this will load the callsite and the receiver normally in the wrong
        // order since the receiver is already present, but before the callsite
        // Therefore we use callSiteReceiverSwap to correct the order.
        // After this call the JVM operand stack will contain the result of
        // the method call... usually simply Object in operandStack
        controller.getCallSiteWriter().makeCallSite(
                callSiteReceiverSwap,
                method,
                MethodCallExpression.NO_ARGUMENTS,
                false, false, false, false);
        // now rhs is completely done and we need only to store. In a[1]++ this
        // would be a.getAt(1).next() for the rhs, "lhs" code is a.putAt(1, rhs)
    }

    private void evaluateElvisOperatorExpression(final ElvisOperatorExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        TypeChooser typeChooser = controller.getTypeChooser();

        Expression boolPart = expression.getBooleanExpression().getExpression();
        Expression falsePart = expression.getFalseExpression();

        ClassNode truePartType = typeChooser.resolveType(boolPart, controller.getClassNode());
        ClassNode falsePartType = typeChooser.resolveType(falsePart, controller.getClassNode());
        ClassNode common = WideningCategories.lowestUpperBound(truePartType, falsePartType);

        // x?:y is equal to x?x:y, which evals to
        //      var t=x; boolean(t)?t:y
        // first we load x, dup it, convert the dupped to boolean, then
        // jump depending on the value. For true we are done, for false we
        // have to load y, thus we first remove x and then load y.
        // But since x and y may have different stack lengths, this cannot work
        // Thus we have to have to do the following:
        // Be X the type of x, Y the type of y and S the common supertype of
        // X and Y, then we have to see x?:y as
        //      var t=x;boolean(t)?S(t):S(y)
        // so we load x, dup it, store the value in a local variable (t), then
        // do boolean conversion. In the true part load t and cast it to S,
        // in the false part load y and cast y to S

        // load x, dup it, store one in $t and cast the remaining one to boolean
        int mark = operandStack.getStackLength();
        boolPart.visit(controller.getAcg());
        operandStack.dup();
        if (ClassHelper.isPrimitiveType(truePartType) && !ClassHelper.isPrimitiveType(operandStack.getTopOperand())) {
            truePartType = ClassHelper.getWrapper(truePartType);
        }
        int retValueId = compileStack.defineTemporaryVariable("$t", truePartType, true);
        operandStack.castToBool(mark, true);

        Label l0 = operandStack.jump(IFEQ);
        // true part: load $t and cast to S
        operandStack.load(truePartType, retValueId);
        operandStack.doGroovyCast(common);
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);

        // false part: load false expression and cast to S
        mv.visitLabel(l0);
        falsePart.visit(controller.getAcg());
        operandStack.doGroovyCast(common);

        // finish and cleanup
        mv.visitLabel(l1);
        compileStack.removeVar(retValueId);
        operandStack.replace(common, 2);
    }

    private void evaluateNormalTernary(final TernaryExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        TypeChooser typeChooser = controller.getTypeChooser();
        OperandStack operandStack = controller.getOperandStack();

        Expression boolPart = expression.getBooleanExpression();
        Expression truePart = expression.getTrueExpression();
        Expression falsePart = expression.getFalseExpression();

        ClassNode truePartType = typeChooser.resolveType(truePart, controller.getClassNode());
        ClassNode falsePartType = typeChooser.resolveType(falsePart, controller.getClassNode());
        ClassNode common = WideningCategories.lowestUpperBound(truePartType, falsePartType);

        // we compile b?x:y as
        //      boolean(b)?S(x):S(y), S = common super type of x,y
        // so we load b, do boolean conversion.
        // In the true part load x and cast it to S,
        // in the false part load y and cast y to S

        // load b and convert to boolean
        int mark = operandStack.getStackLength();
        boolPart.visit(controller.getAcg());
        operandStack.castToBool(mark, true);

        Label l0 = operandStack.jump(IFEQ);
        // true part: load x and cast to S
        truePart.visit(controller.getAcg());
        operandStack.doGroovyCast(common);
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);

        // false part: load y and cast to S
        mv.visitLabel(l0);
        falsePart.visit(controller.getAcg());
        operandStack.doGroovyCast(common);

        // finish and cleanup
        mv.visitLabel(l1);
        operandStack.replace(common, 2);
    }

    public void evaluateTernary(final TernaryExpression expression) {
        if (expression instanceof ElvisOperatorExpression) {
            evaluateElvisOperatorExpression((ElvisOperatorExpression) expression);
        } else {
            evaluateNormalTernary(expression);
        }
    }
}
