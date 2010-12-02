/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.codehaus.groovy.syntax.Types.*;
import static org.objectweb.asm.Opcodes.*;

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

    private WriterController controller;
    
    public BinaryExpressionHelper(WriterController wc) {
        this.controller = wc;
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

        default:
            throw new GroovyBugError("Operation: " + expression.getOperation() + " not supported");
        }
    }
    
    private void assignArray(Expression receiver, Expression index, Expression rhsValueLoader) {
        // let's replace this assignment to a subscript operator with a
        // method call
        // e.g. x[5] = 10
        // -> (x, [], 5), =, 10
        // -> methodCall(x, "putAt", [5, 10])
        ArgumentListExpression ae = new ArgumentListExpression(index,rhsValueLoader);
        controller.getCallSiteWriter().makeCallSite(receiver, "putAt", ae, false, false, false, false);
        controller.getOperandStack().pop();
        // return value of assignment
        rhsValueLoader.visit(controller.getAcg());
    }
    

    public void evaluateEqual(BinaryExpression expression, boolean defineVariable) {
        AsmClassGenerator acg = controller.getAcg();
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        
        // let's evaluate the RHS and store the result
        Expression rightExpression = expression.getRightExpression();
        rightExpression.visit(acg);
        ClassNode rhsType = getCastType(rightExpression);
        int rhsValueId = compileStack.defineTemporaryVariable("$rhs", rhsType, true);
        //TODO: if rhs is VariableSlotLoader already, then skip crating a new one
        BytecodeExpression rhsValueLoader = new VariableSlotLoader(rhsType,rhsValueId,operandStack); 
        
        // assignment for subscript
        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
                assignArray(leftBinExpr.getLeftExpression(), leftBinExpr.getRightExpression(), rhsValueLoader);
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
            VariableExpression var = (VariableExpression) leftExpression;
            rhsValueLoader.visit(acg);
            operandStack.doGroovyCast(var);
            compileStack.defineVariable(var, true);
            operandStack.remove(1);
        } 
        // normal assignment
        else {
            int mark = operandStack.getStackLength();
            // to leave a copy of the rightExpression value on the stack after the assignment.
            rhsValueLoader.visit(acg);
            leftExpression.visit(acg);
            operandStack.remove(operandStack.getStackLength()-mark);
        }
        compileStack.popLHS();
        
        // return value of assignment
        rhsValueLoader.visit(acg);
        compileStack.removeVar(rhsValueId);
    }
    
    private ClassNode getCastType(Expression exp) {
        if (exp instanceof ClassExpression) {
            return ClassHelper.CLASS_Type;
        } else if (exp instanceof ConstructorCallExpression) {
            return ClassHelper.OBJECT_TYPE;
        } else {
            return exp.getType();
        }
    }

    protected void evaluateCompareExpression(MethodCaller compareMethod, BinaryExpression expression) {
        Expression leftExp = expression.getLeftExpression();
        Expression rightExp = expression.getRightExpression();
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
    
    private ClassNode getType(Expression exp) {
        Variable v = null;
        if (exp instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) exp;
            v = ve.getAccessedVariable();
        } else if (exp instanceof FieldExpression) {
            FieldExpression fe = (FieldExpression) exp;
            v = fe.getField();
        }
        if (v!=null) {
            if (!v.isClosureSharedVariable()) {
                return v.getOriginType();
            } else {
                return ClassHelper.OBJECT_TYPE;
            }
        }
        return exp.getType();
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
        controller.getCallSiteWriter().makeInvocation(receiver, message, arguments);
        compileStack.popLHS();        
    }

    private void evaluateBinaryExpressionWithAssignment(String method, BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        MethodVisitor mv  = controller.getMethodVisitor();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();
        
        if (leftExpression instanceof BinaryExpression) {
            BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
            if (leftBinExpr.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
                // e.g. x[a] += b
                // -> subscript=a, x[subscript], =, x[subscript] + b
                // -> subscript=a, methodCall_3(x, "putAt", [subscript, methodCall_2(methodCall_1(x, "getAt", [subscript]), "plus", b)])
                
                Expression subscriptExpression = leftBinExpr.getRightExpression(); 
                subscriptExpression.visit(acg); // value(subscript)
                operandStack.box();
                int subscriptValueId = compileStack.defineTemporaryVariable("$subscript", ClassHelper.OBJECT_TYPE, true);

                // method calls from outer to inner (most inner will be called first):
                controller.getCallSiteWriter().prepareCallSite("putAt");
                controller.getCallSiteWriter().prepareCallSite(method);
                controller.getCallSiteWriter().prepareCallSite("getAt");
                
                // getAt call
                //x = receiver
                leftBinExpr.getLeftExpression().visit(acg);  
                operandStack.box();
                // we save that value for later
                operandStack.dup();
                int xValueId = compileStack.defineTemporaryVariable("$xValue", ClassHelper.OBJECT_TYPE, true);
                // subscript = argument to getAt call
                operandStack.load(ClassHelper.OBJECT_TYPE, subscriptValueId);
                // invoke getAt
                mv.visitMethodInsn(INVOKEINTERFACE, "org/codehaus/groovy/runtime/callsite/CallSite", "call","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                operandStack.replace(ClassHelper.OBJECT_TYPE, 2); 
                //x[subscript] with type Object now on stack

                // call with method (e.g. "plus")
                // receiver is the result from the getAt before
                // load b
                expression.getRightExpression().visit(acg);
                operandStack.box();
                // invoke "method"
                mv.visitMethodInsn(INVOKEINTERFACE, "org/codehaus/groovy/runtime/callsite/CallSite", "call","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                operandStack.replace(ClassHelper.OBJECT_TYPE, 2); 
                //RHS with type Object now on stack
                
                // let us save that value for the return
                int resultValueId = compileStack.defineTemporaryVariable("$result", ClassHelper.OBJECT_TYPE, true);               
                
                // call for putAt
                // receiver for putAt is x, the arguments will be the subscript
                // value and the value of the RHS.
                // load receiver x
                operandStack.load(ClassHelper.OBJECT_TYPE, xValueId);
                operandStack.load(ClassHelper.OBJECT_TYPE, subscriptValueId);
                operandStack.load(ClassHelper.OBJECT_TYPE, resultValueId);
                // invoke putAt
                mv.visitMethodInsn(INVOKEINTERFACE, "org/codehaus/groovy/runtime/callsite/CallSite", "call","(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                operandStack.replace(ClassHelper.OBJECT_TYPE, 3);

                // remove result of putAt and keep the result on stack
                operandStack.pop();
                operandStack.load(ClassHelper.OBJECT_TYPE, resultValueId);
                
                compileStack.removeVar(resultValueId);
                compileStack.removeVar(xValueId);
                compileStack.removeVar(subscriptValueId);
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

    private void evaluatePostfixMethod(String method, Expression expression) {
        CompileStack compileStack = controller.getCompileStack();
        final OperandStack operandStack = controller.getOperandStack();
        
        // load Expressions
        VariableSlotLoader usesSubscript = loadWithSubscript(expression);

        // save copy for later
        operandStack.dup();
        ClassNode expressionType = operandStack.getTopOperand(); 
        int tempIdx = compileStack.defineTemporaryVariable("postfix_" + method, expressionType, true);
        
        // execute Method
        execMethodAndStoreForSubscriptOperator(method,expression,usesSubscript);
        
        // remove the result of the method call
        operandStack.pop();        
        
        //reload saved value
        operandStack.load(expressionType, tempIdx);
        compileStack.removeVar(tempIdx);
        if (usesSubscript!=null) compileStack.removeVar(usesSubscript.getIndex());
    }

    public void evaluatePostfixMethod(PostfixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Types.PLUS_PLUS:
                evaluatePostfixMethod("next", expression.getExpression());
                break;
            case Types.MINUS_MINUS:
                evaluatePostfixMethod("previous", expression.getExpression());
                break;
        }
    }

    public void evaluatePrefixMethod(PrefixExpression expression) {
        switch (expression.getOperation().getType()) {
            case Types.PLUS_PLUS:
                evaluatePrefixMethod("next", expression.getExpression());
                break;
            case Types.MINUS_MINUS:
                evaluatePrefixMethod("previous", expression.getExpression());
                break;
        }
    }
    
    private void evaluatePrefixMethod(String method, Expression expression) {
        // load Expressions
        VariableSlotLoader usesSubscript = loadWithSubscript(expression);
        
        // execute Method
        execMethodAndStoreForSubscriptOperator(method,expression,usesSubscript);

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
                operandStack.box(); //TODO: maybe not box here anymore, but need subscript type then
                int id = controller.getCompileStack().defineTemporaryVariable("$subscript", true);
                VariableSlotLoader subscriptExpression = new VariableSlotLoader(id,operandStack);
                // do modified visit
                BinaryExpression newBe = new BinaryExpression(be.getLeftExpression(), be.getOperation(), subscriptExpression);
                newBe.setSourcePosition(be);
                newBe.visit(controller.getAcg());
                return subscriptExpression;
            } 
        } 
        
        // normal loading of expression
        expression.visit(controller.getAcg());
        return null;
    }
    
    private void execMethodAndStoreForSubscriptOperator(String method, Expression expression, VariableSlotLoader usesSubscript) {
        final OperandStack operandStack = controller.getOperandStack();
        // at this point the receiver will be already on the stack.
        // in a[1]++ the method will be "++" aka "next" and the receiver a[1]
        
        Expression callSiteReceiverSwap = new BytecodeExpression(getType(expression)) {
            @Override
            public void visit(MethodVisitor mv) {
                // CallSite is normally not showing up on the 
                // operandStack, so we place a dummy here with same
                // slot length.
                operandStack.push(ClassHelper.OBJECT_TYPE);
                // change (receiver,callsite) to (callsite,receiver)
                operandStack.swap();
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
        // After this call the JVM operand stack will contain the the result of
        // the method call... usually simply Object in operandStack
        controller.getCallSiteWriter().makeCallSite(
                callSiteReceiverSwap,
                method,
                MethodCallExpression.NO_ARGUMENTS,
                false, false, false, false);
        // now rhs is completely done and we need only to store. In a[1]++ this 
        // would be a.getAt(1).next() for the rhs, "lhs" code is a.putAt(1, rhs)
         
        // we need special code for arrays to store the result (like for a[1]++)
        if (usesSubscript!=null) {
            CompileStack compileStack = controller.getCompileStack();
            BinaryExpression be = (BinaryExpression) expression;
            
            ClassNode methodResultType = operandStack.getTopOperand();
            final int resultIdx = compileStack.defineTemporaryVariable("postfix_" + method, methodResultType, true);
            BytecodeExpression methodResultLoader = new VariableSlotLoader(methodResultType, resultIdx, operandStack);
            
            // execute the assignment, this will leave the right side 
            // (here the method call result) on the stack
            assignArray(be.getLeftExpression(), usesSubscript, methodResultLoader);

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
}
