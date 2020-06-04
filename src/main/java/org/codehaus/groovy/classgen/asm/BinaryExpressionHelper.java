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

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
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
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

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
import static org.codehaus.groovy.syntax.Types.COMPARE_TO;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.DIVIDE_EQUAL;
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
import static org.codehaus.groovy.syntax.Types.MOD;
import static org.codehaus.groovy.syntax.Types.MOD_EQUAL;
import static org.codehaus.groovy.syntax.Types.MULTIPLY;
import static org.codehaus.groovy.syntax.Types.MULTIPLY_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS;
import static org.codehaus.groovy.syntax.Types.PLUS_EQUAL;
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
    //compare
    private static final MethodCaller compareEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareEqual");
    private static final MethodCaller compareNotEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareNotEqual");
    private static final MethodCaller compareToMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareTo");
    private static final MethodCaller compareLessThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThan");
    private static final MethodCaller compareLessThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareLessThanEqual");
    private static final MethodCaller compareGreaterThanMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThan");
    private static final MethodCaller compareGreaterThanEqualMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "compareGreaterThanEqual");
    //regexpr
    private static final MethodCaller findRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "findRegex");
    private static final MethodCaller matchRegexMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "matchRegex");
    // isCase
    private static final MethodCaller isCaseMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "isCase");

    private final WriterController controller;
    
    public BinaryExpressionHelper(WriterController wc) {
        this.controller = wc;
    }
    
    public WriterController getController(){
        return controller;
    }
    
    public void eval(BinaryExpression expression) {
        switch (expression.getOperation().getType()) {
        case EQUAL: // = assignment
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

        case COMPARE_IDENTICAL:
        case COMPARE_NOT_IDENTICAL:
            Token op = expression.getOperation();
            Throwable cause = new SyntaxException("Operator " + op + " not supported", op.getStartLine(), op.getStartColumn(), op.getStartLine(), op.getStartColumn()+3);
            throw new GroovyRuntimeException(cause);

        default:
            throw new GroovyBugError("Operation: " + expression.getOperation() + " not supported");
        }
    }
    
    protected void assignToArray(Expression parent, Expression receiver, Expression index, Expression rhsValueLoader) {
        // let's replace this assignment to a subscript operator with a
        // method call
        // e.g. x[5] = 10
        // -> (x, [], 5), =, 10
        // -> methodCall(x, "putAt", [5, 10])
        ArgumentListExpression ae = new ArgumentListExpression(index,rhsValueLoader);
        controller.getInvocationWriter().makeCall(
                parent, receiver, new ConstantExpression("putAt"),
                ae, InvocationWriter.invokeMethod, false, false, false);
        controller.getOperandStack().pop();
        // return value of assignment
        rhsValueLoader.visit(controller.getAcg());
    }

    private static boolean isNull(Expression exp) {
        if (exp instanceof ConstantExpression){
            return ((ConstantExpression) exp).getValue()==null;
        } else {
            return false;
        }
    }

    public void evaluateEqual(BinaryExpression expression, boolean defineVariable) {
        AsmClassGenerator acg = controller.getAcg();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        Expression rightExpression = expression.getRightExpression();
        Expression leftExpression = expression.getLeftExpression();

        if (    defineVariable &&
                rightExpression instanceof EmptyExpression && 
                !(leftExpression instanceof TupleExpression) )
        {
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
            loadInitValue(leftExpression.getType());
        } else {
            rightExpression.visit(acg);
        }

        ClassNode rhsType = operandStack.getTopOperand();
        boolean directAssignment = defineVariable && !(leftExpression instanceof TupleExpression);
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
            if (var.isClosureSharedVariable() && ClassHelper.isPrimitiveType(var.getOriginType()) && isNull(rightExpression)) {
                operandStack.doGroovyCast(var.getOriginType());
                // these two are never reached in bytecode and only there 
                // to avoid verifyerrors and compiler infrastructure hazzle
                operandStack.box(); 
                operandStack.doGroovyCast(lhsType);
            }
            // normal type transformation 
            if (!ClassHelper.isPrimitiveType(lhsType) && isNull(rightExpression)) {
                operandStack.replace(lhsType);
            } else {
                operandStack.doGroovyCast(lhsType);
            }
            rhsType = lhsType;
            rhsValueId = compileStack.defineVariable(var, lhsType, true).getIndex();
        } else {
            rhsValueId = compileStack.defineTemporaryVariable("$rhs", rhsType, true);
        }
        //TODO: if rhs is VariableSlotLoader already, then skip crating a new one
        BytecodeExpression rhsValueLoader = new VariableSlotLoader(rhsType,rhsValueId,operandStack); 
        
        // assignment for subscript
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
                assignToArray(expression, leftBinExpr.getLeftExpression(), leftBinExpr.getRightExpression(), rhsValueLoader);
            }
            compileStack.removeVar(rhsValueId);
            return;
        }
        
        compileStack.pushLHS(true);

        // multiple declaration
        if (leftExpression instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) leftExpression;
            int i = 0;
            for (Expression e : tuple.getExpressions()) {
                VariableExpression var = (VariableExpression) e;
                MethodCallExpression call = new MethodCallExpression(
                        rhsValueLoader, "getAt",
                        new ArgumentListExpression(new ConstantExpression(i)));
                call.visit(acg);
                i++;
                if (defineVariable) {
                    operandStack.doGroovyCast(var);
                    compileStack.defineVariable(var, true);
                    operandStack.remove(1);
                } else {
                    acg.visitVariableExpression(var);
                }
            }
        } 
        // single declaration
        else if (defineVariable) {
            rhsValueLoader.visit(acg);
            operandStack.remove(1);
            compileStack.popLHS();
            return;
        } 
        // normal assignment
        else {
            int mark = operandStack.getStackLength();
            rhsValueLoader.visit(acg);
            leftExpression.visit(acg);
            operandStack.remove(operandStack.getStackLength()-mark);
        }
        compileStack.popLHS();
        
        // return value of assignment
        rhsValueLoader.visit(acg);
        compileStack.removeVar(rhsValueId);
    }

    private void loadInitValue(ClassNode type) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (ClassHelper.isPrimitiveType(type)) {
            mv.visitLdcInsn(0);
        } else {
            mv.visitInsn(ACONST_NULL);
        }
        controller.getOperandStack().push(type);
    }

    protected void evaluateCompareExpression(MethodCaller compareMethod, BinaryExpression expression) {
        Expression leftExp = expression.getLeftExpression();
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode cn = controller.getClassNode();
        ClassNode leftType = typeChooser.resolveType(leftExp,cn);
        Expression rightExp = expression.getRightExpression();
        ClassNode rightType = typeChooser.resolveType(rightExp,cn);

        boolean done = false;
        if (    ClassHelper.isPrimitiveType(leftType) &&
                ClassHelper.isPrimitiveType(rightType)) 
        {
            BinaryExpressionMultiTypeDispatcher helper = new BinaryExpressionMultiTypeDispatcher(getController());
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
            if (compareMethod==findRegexMethod) {
                resType = ClassHelper.OBJECT_TYPE;
            } 
            operandStack.replace(resType,2);
        }
    }
    
    private void evaluateCompareTo(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();
        
        leftExpression.visit(acg);
        operandStack.box();

        // if the right hand side is a boolean expression, we need to autobox
        Expression rightExpression = expression.getRightExpression();
        rightExpression.visit(acg);
        operandStack.box();

        compareToMethod.call(controller.getMethodVisitor());
        operandStack.replace(ClassHelper.Integer_TYPE,2);
    }

    private void evaluateLogicalAndExpression(BinaryExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();

        expression.getLeftExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        Label falseCase = operandStack.jump(IFEQ);

        expression.getRightExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        operandStack.jump(IFEQ,falseCase);

        ConstantExpression.PRIM_TRUE.visit(acg);
        Label trueCase = new Label();
        mv.visitJumpInsn(GOTO, trueCase);

        mv.visitLabel(falseCase);
        ConstantExpression.PRIM_FALSE.visit(acg);

        mv.visitLabel(trueCase);
        operandStack.remove(1); // have to remove 1 because of the GOTO
    }
    
    private void evaluateLogicalOrExpression(BinaryExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();

        Label end = new Label();

        expression.getLeftExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        Label trueCase = operandStack.jump(IFNE);
        
        expression.getRightExpression().visit(acg);
        operandStack.doGroovyCast(ClassHelper.boolean_TYPE);
        Label falseCase = operandStack.jump(IFEQ);
        
        mv.visitLabel(trueCase);
        ConstantExpression.PRIM_TRUE.visit(acg);
        operandStack.jump(GOTO, end);

        mv.visitLabel(falseCase);
        ConstantExpression.PRIM_FALSE.visit(acg);
        
        mv.visitLabel(end);
    }
    
    protected void evaluateBinaryExpression(String message, BinaryExpression binExp) {
        CompileStack compileStack = controller.getCompileStack();

        Expression receiver = binExp.getLeftExpression();
        Expression arguments = binExp.getRightExpression();

        // ensure VariableArguments are read, not stored
        compileStack.pushLHS(false);
        controller.getInvocationWriter().makeSingleArgumentCall(receiver, message, arguments);
        compileStack.popLHS();        
    }

    protected void evaluateArrayAssignmentWithOperator(String method, BinaryExpression expression, BinaryExpression leftBinExpr) {
        CompileStack        compileStack    = getController().getCompileStack();
        AsmClassGenerator   acg             = getController().getAcg();
        OperandStack        os              = getController().getOperandStack();

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
        MethodCallExpression getAt = new MethodCallExpression(receiver, "getAt", new ArgumentListExpression(subscript));
        MethodCallExpression operation = new MethodCallExpression(getAt, method, expression.getRightExpression());
        ExpressionAsVariableSlot ret = new ExpressionAsVariableSlot(controller, operation, "ret");
        MethodCallExpression putAt = new MethodCallExpression(receiver, "putAt", new ArgumentListExpression(subscript, ret));

        putAt.visit(acg);
        os.pop();
        os.load(ret.getType(), ret.getIndex());

        compileStack.removeVar(ret.getIndex());
        compileStack.removeVar(subscript.getIndex());
        compileStack.removeVar(receiver.getIndex());
    }

    protected void evaluateBinaryExpressionWithAssignment(String method, BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();
        
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
                evaluateArrayAssignmentWithOperator(method, expression, leftBinExpr);
                return;
            }
        } 

        evaluateBinaryExpression(method, expression);

        // br to leave a copy of rvalue on the stack. see also isPopRequired()
        operandStack.dup();
        
        controller.getCompileStack().pushLHS(true);
        leftExpression.visit(acg);
        controller.getCompileStack().popLHS();
    }
    
    private void evaluateInstanceof(BinaryExpression expression) {
        OperandStack operandStack = controller.getOperandStack();
        
        expression.getLeftExpression().visit(controller.getAcg());
        operandStack.box();
        Expression rightExp = expression.getRightExpression();
        ClassNode classType;
        if (rightExp instanceof ClassExpression) {
            ClassExpression classExp = (ClassExpression) rightExp;
            classType = classExp.getType();
        } else {
            throw new RuntimeException(
                    "Right hand side of the instanceof keyword must be a class name, not: " + rightExp);
        }
        String classInternalName = BytecodeHelper.getClassInternalName(classType);
        controller.getMethodVisitor().visitTypeInsn(INSTANCEOF, classInternalName);
        operandStack.replace(ClassHelper.boolean_TYPE);
    }

    public MethodCaller getIsCaseMethod() {
        return isCaseMethod;
    }

    private void evaluatePostfixMethod(int op, String method, Expression expression, Expression orig) {
        CompileStack compileStack = controller.getCompileStack();
        final OperandStack operandStack = controller.getOperandStack();
        
        // load Expressions
        VariableSlotLoader usesSubscript = loadWithSubscript(expression);

        // save copy for later
        operandStack.dup();
        ClassNode expressionType = operandStack.getTopOperand(); 
        int tempIdx = compileStack.defineTemporaryVariable("postfix_" + method, expressionType, true);
        
        // execute Method
        execMethodAndStoreForSubscriptOperator(op,method,expression,usesSubscript,orig);
        
        // remove the result of the method call
        operandStack.pop();        
        
        //reload saved value
        operandStack.load(expressionType, tempIdx);
        compileStack.removeVar(tempIdx);
        if (usesSubscript!=null) compileStack.removeVar(usesSubscript.getIndex());
    }

    public void evaluatePostfixMethod(PostfixExpression expression) {
        int op = expression.getOperation().getType();
        switch (op) {
            case Types.PLUS_PLUS:
                evaluatePostfixMethod(op, "next", expression.getExpression(), expression);
                break;
            case Types.MINUS_MINUS:
                evaluatePostfixMethod(op, "previous", expression.getExpression(), expression);
                break;
        }
    }

    public void evaluatePrefixMethod(PrefixExpression expression) {
        int type = expression.getOperation().getType();
        switch (type) {
            case Types.PLUS_PLUS:
                evaluatePrefixMethod(type, "next", expression.getExpression(), expression);
                break;
            case Types.MINUS_MINUS:
                evaluatePrefixMethod(type, "previous", expression.getExpression(), expression);
                break;
        }
    }
    
    private void evaluatePrefixMethod(int op, String method, Expression expression, Expression orig) {
        // load Expressions
        VariableSlotLoader usesSubscript = loadWithSubscript(expression);
        
        // execute Method
        execMethodAndStoreForSubscriptOperator(op,method,expression,usesSubscript,orig);

        // new value is already on stack, so nothing to do here
        if (usesSubscript!=null) controller.getCompileStack().removeVar(usesSubscript.getIndex());
    }
    
    private VariableSlotLoader loadWithSubscript(Expression expression) {
        final OperandStack operandStack = controller.getOperandStack();
        // if we have a BinaryExpression, let us check if it is with
        // subscription
        if (expression instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expression;
            if (be.getOperation().getType()==Types.LEFT_SQUARE_BRACKET) {
                // right expression is the subscript expression
                // we store the result of the subscription on the stack
                Expression subscript = be.getRightExpression();
                subscript.visit(controller.getAcg());
                ClassNode subscriptType = operandStack.getTopOperand();
                int id = controller.getCompileStack().defineTemporaryVariable("$subscript", subscriptType, true);
                VariableSlotLoader subscriptExpression = new VariableSlotLoader(subscriptType, id, operandStack);
                // do modified visit
                BinaryExpression newBe = new BinaryExpression(be.getLeftExpression(), be.getOperation(), subscriptExpression);
                newBe.copyNodeMetaData(be);
                newBe.setSourcePosition(be);
                newBe.visit(controller.getAcg());
                return subscriptExpression;
            } 
        } 
        
        // normal loading of expression
        expression.visit(controller.getAcg());
        return null;
    }
    
    private void execMethodAndStoreForSubscriptOperator(int op, String method, Expression expression, VariableSlotLoader usesSubscript, Expression orig) {
        final OperandStack operandStack = controller.getOperandStack();
        writePostOrPrefixMethod(op,method,expression,orig);

        // we need special code for arrays to store the result (like for a[1]++)
        if (usesSubscript!=null) {
            CompileStack compileStack = controller.getCompileStack();
            BinaryExpression be = (BinaryExpression) expression;
            
            ClassNode methodResultType = operandStack.getTopOperand();
            final int resultIdx = compileStack.defineTemporaryVariable("postfix_" + method, methodResultType, true);
            BytecodeExpression methodResultLoader = new VariableSlotLoader(methodResultType, resultIdx, operandStack);
            
            // execute the assignment, this will leave the right side 
            // (here the method call result) on the stack
            assignToArray(be, be.getLeftExpression(), usesSubscript, methodResultLoader);

            compileStack.removeVar(resultIdx);
        } 
        // here we handle a.b++ and a++
        else if (expression instanceof VariableExpression ||
            expression instanceof FieldExpression || 
            expression instanceof PropertyExpression)
        {
            operandStack.dup();
            controller.getCompileStack().pushLHS(true);
            expression.visit(controller.getAcg());
            controller.getCompileStack().popLHS();
        }
        // other cases don't need storing, so nothing to be done for them
    }

    protected void writePostOrPrefixMethod(int op, String method, Expression expression, Expression orig) {
        final OperandStack operandStack = controller.getOperandStack();
        // at this point the receiver will be already on the stack.
        // in a[1]++ the method will be "++" aka "next" and the receiver a[1]
        
        ClassNode BEType = controller.getTypeChooser().resolveType(expression, controller.getClassNode());
        Expression callSiteReceiverSwap = new BytecodeExpression(BEType) {
            @Override
            public void visit(MethodVisitor mv) {
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
    
    private void evaluateElvisOperatorExpression(ElvisOperatorExpression expression) {
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
        operandStack.castToBool(mark,true);
        
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
        controller.getOperandStack().replace(common, 2);        
        
    }

    private void evaluateNormalTernary(TernaryExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        TypeChooser typeChooser = controller.getTypeChooser();
        
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
        operandStack.castToBool(mark,true);
        
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
        controller.getOperandStack().replace(common, 2);        
        
    }

    public void evaluateTernary(TernaryExpression expression) {
        if (expression instanceof ElvisOperatorExpression) {
            evaluateElvisOperatorExpression((ElvisOperatorExpression) expression);
        } else {
            evaluateNormalTernary(expression);
        }
    }
}
