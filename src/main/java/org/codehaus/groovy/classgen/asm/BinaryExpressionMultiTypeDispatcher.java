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
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.runtime.BytecodeInterface8;

import java.util.HashMap;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;
import static org.codehaus.groovy.ast.tools.WideningCategories.isBigDecCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isDoubleCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isIntCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isLongCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isNumberCategory;
import static org.codehaus.groovy.syntax.TokenUtil.removeAssignment;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT;
import static org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED;

/**
 * This class is for internal use only!
 * This class will dispatch to the right type adapters according to the 
 * kind of binary expression that is provided.
 */
public class BinaryExpressionMultiTypeDispatcher extends BinaryExpressionHelper {
    
    private static class BinaryCharExpressionHelper extends BinaryIntExpressionHelper {
        public BinaryCharExpressionHelper(WriterController wc) {
            super(wc, charArraySet, charArrayGet);
        }
        private static final MethodCaller
            charArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "cArrayGet"),
            charArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "cArraySet");
        @Override protected ClassNode getArrayGetResultType() { return ClassHelper.char_TYPE; }
    }
    
    private static class BinaryByteExpressionHelper extends BinaryIntExpressionHelper {
        public BinaryByteExpressionHelper(WriterController wc) {
            super(wc, byteArraySet, byteArrayGet);
        }
        private static final MethodCaller
            byteArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "bArrayGet"),
            byteArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "bArraySet");
        @Override protected ClassNode getArrayGetResultType() { return ClassHelper.byte_TYPE; }
    }
    
    private static class BinaryShortExpressionHelper extends BinaryIntExpressionHelper {
        public BinaryShortExpressionHelper(WriterController wc) {
            super(wc, shortArraySet, shortArrayGet);
        }
        private static final MethodCaller
            shortArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "sArrayGet"),
            shortArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "sArraySet");
        @Override protected ClassNode getArrayGetResultType() { return ClassHelper.short_TYPE; }
    }
    
    protected BinaryExpressionWriter[] binExpWriter = initializeDelegateHelpers();

    protected BinaryExpressionWriter[] initializeDelegateHelpers() {
        return new BinaryExpressionWriter[]{
                /* 0: dummy  */ new BinaryObjectExpressionHelper(getController()),
                /* 1: int    */ new BinaryIntExpressionHelper(getController()),
                /* 2: long   */ new BinaryLongExpressionHelper(getController()),
                /* 3: double */ new BinaryDoubleExpressionHelper(getController()),
                /* 4: char   */ new BinaryCharExpressionHelper(getController()),
                /* 5: byte   */ new BinaryByteExpressionHelper(getController()),
                /* 6: short  */ new BinaryShortExpressionHelper(getController()),
                /* 7: float  */ new BinaryFloatExpressionHelper(getController()),
                /* 8: bool   */ new BinaryBooleanExpressionHelper(getController()),
        };
    }

    public static final Map<ClassNode,Integer> typeMap = new HashMap<>(14);
    static {
        typeMap.put(int_TYPE,       1); typeMap.put(long_TYPE,          2);
        typeMap.put(double_TYPE,    3); typeMap.put(char_TYPE,          4);
        typeMap.put(byte_TYPE,      5); typeMap.put(short_TYPE,         6);
        typeMap.put(float_TYPE,     7); typeMap.put(boolean_TYPE,       8);
    }
    public static final String[] typeMapKeyNames = {"dummy", "int", "long", "double", "char", "byte", "short", "float", "boolean"};

    public BinaryExpressionMultiTypeDispatcher(WriterController wc) {
        super(wc);
    }

    private static int getOperandConversionType(ClassNode leftType, ClassNode rightType) {
        if (isIntCategory(leftType) && isIntCategory(rightType)) return 1;
        if (isLongCategory(leftType) && isLongCategory(rightType)) return 2;
        if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) return 0;
        if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) return 3;
        return 0;
    }
    
    protected int getOperandType(ClassNode type) {
        Integer ret = typeMap.get(type);
        if (ret==null) return 0;
        return ret;
    }

    @Deprecated
    protected boolean doPrimtiveCompare(ClassNode leftType, ClassNode rightType, BinaryExpression binExp) {
        return doPrimitiveCompare(leftType, rightType, binExp);
    }

    protected boolean doPrimitiveCompare(ClassNode leftType, ClassNode rightType, BinaryExpression binExp) {
        Expression leftExp = binExp.getLeftExpression();
        Expression rightExp = binExp.getRightExpression();
        int operation = binExp.getOperation().getType();
        
        int operationType = getOperandConversionType(leftType,rightType);
        BinaryExpressionWriter bew = binExpWriter[operationType];

        if (!bew.write(operation, true)) return false;
            
        AsmClassGenerator acg = getController().getAcg();
        OperandStack os = getController().getOperandStack();
        leftExp.visit(acg);
        os.doGroovyCast(bew.getNormalOpResultType());
        rightExp.visit(acg);
        os.doGroovyCast(bew.getNormalOpResultType());
        bew.write(operation, false);
        
        return true;
    }
    
    @Override
    protected void evaluateCompareExpression(final MethodCaller compareMethod, BinaryExpression binExp) {
        ClassNode current =  getController().getClassNode();
        TypeChooser typeChooser = getController().getTypeChooser();
        
        Expression leftExp = binExp.getLeftExpression();
        ClassNode leftType = typeChooser.resolveType(leftExp, current);
        Expression rightExp = binExp.getRightExpression();
        ClassNode rightType = typeChooser.resolveType(rightExp, current);
        
        if (!doPrimitiveCompare(leftType, rightType, binExp)) {
            super.evaluateCompareExpression(compareMethod, binExp);
        }
    }
    
    @Override
    protected void evaluateBinaryExpression(final String message, BinaryExpression binExp) {
        int operation = removeAssignment(binExp.getOperation().getType());
        ClassNode current =  getController().getClassNode();

        Expression leftExp = binExp.getLeftExpression();
        ClassNode leftTypeOrig = getController().getTypeChooser().resolveType(leftExp, current);
        ClassNode leftType = leftTypeOrig;
        Expression rightExp = binExp.getRightExpression();
        ClassNode rightType = getController().getTypeChooser().resolveType(rightExp, current);
        
        AsmClassGenerator acg = getController().getAcg();
        OperandStack os = getController().getOperandStack();
        
        if (operation==LEFT_SQUARE_BRACKET) {
            leftType = leftTypeOrig.getComponentType();
            int operationType = getOperandType(leftType);
            BinaryExpressionWriter bew = binExpWriter[operationType];
            if (    leftTypeOrig.isArray() && isIntCastableType(rightExp) && 
                    bew.arrayGet(operation, true) &&
                    !binExp.isSafe())
            {
                leftExp.visit(acg);
                os.doGroovyCast(leftTypeOrig);
                rightExp.visit(acg);
                os.doGroovyCast(int_TYPE);
                bew.arrayGet(operation, false);
                os.replace(bew.getArrayGetResultType(),2);
            } else {
                super.evaluateBinaryExpression(message, binExp);
            }
        } else if (operation == DIVIDE) { 
            int operationType = getOperandType(getController().getTypeChooser().resolveType(binExp, current));
            BinaryExpressionWriter bew = binExpWriter[operationType];
            if (bew.writeDivision(true)) {
                leftExp.visit(acg);
                os.doGroovyCast(bew.getDevisionOpResultType());
                rightExp.visit(acg);
                os.doGroovyCast(bew.getDevisionOpResultType());
                bew.writeDivision(false);
            } else {
                super.evaluateBinaryExpression(message, binExp);
            }
        } else {
            int operationType = getOperandConversionType(leftType,rightType);
            BinaryExpressionWriter bew = binExpWriter[operationType];
            
            if ( isShiftOperation(operation) && isIntCastableType(rightExp) &&
                 bew.write(operation, true)) 
            {
                leftExp.visit(acg);
                os.doGroovyCast(bew.getNormalOpResultType());
                rightExp.visit(acg);
                os.doGroovyCast(int_TYPE);
                bew.write(operation, false);
            } else if (bew.write(operation, true)) {
                leftExp.visit(acg);
                os.doGroovyCast(bew.getNormalOpResultType());
                rightExp.visit(acg);
                os.doGroovyCast(bew.getNormalOpResultType());
                bew.write(operation, false);
            } else {
                super.evaluateBinaryExpression(message, binExp);
            }
        }
    }
    
    private boolean isIntCastableType(Expression rightExp) {
        ClassNode type = getController().getTypeChooser().resolveType(rightExp, getController().getClassNode());
        return isNumberCategory(type);
    }

    private static boolean isShiftOperation(int operation) {
        return  operation==LEFT_SHIFT   || 
                operation==RIGHT_SHIFT  ||
                operation==RIGHT_SHIFT_UNSIGNED;
    }

    private static boolean isAssignmentToArray(BinaryExpression binExp) {
        Expression leftExpression = binExp.getLeftExpression();
        if (!(leftExpression instanceof BinaryExpression)) return false;
        BinaryExpression leftBinExpr = (BinaryExpression) leftExpression;
        return leftBinExpr.getOperation().getType() == LEFT_SQUARE_BRACKET;
    }

    private boolean doAssignmentToArray(BinaryExpression binExp) {
        if (!isAssignmentToArray(binExp)) return false;
        // we need to handle only assignment to arrays combined with an operation
        // special here. e.g x[a] += b
        
        int operation = removeAssignment(binExp.getOperation().getType());
        ClassNode current =  getController().getClassNode();
        
        Expression leftExp = binExp.getLeftExpression();
        ClassNode leftType = getController().getTypeChooser().resolveType(leftExp, current);
        Expression rightExp = binExp.getRightExpression();
        ClassNode rightType = getController().getTypeChooser().resolveType(rightExp, current);
        
        int operationType = getOperandType(leftType);
        BinaryExpressionWriter bew = binExpWriter[operationType];
        
        boolean simulationSuccess = bew.arrayGet(LEFT_SQUARE_BRACKET, true);
        simulationSuccess = simulationSuccess && bew.write(operation, true);
        simulationSuccess = simulationSuccess && bew.arraySet(true);
        if (!simulationSuccess) return false;
        
        AsmClassGenerator acg = getController().getAcg();
        OperandStack operandStack = getController().getOperandStack();
        CompileStack compileStack = getController().getCompileStack();
               
        // for x[a] += b we have the structure:
        //   x = left(left(binExp))), b = right(binExp), a = right(left(binExp)))
        // for array set we need these values on stack: array, index, right 
        // for array get we need these values on stack: array, index
        // to eval the expression we need x[a] = x[a]+b
        // -> arraySet(x,a, x[a]+b) 
        // -> arraySet(x,a, arrayGet(x,a,b))
        // --> x,a, x,a, b as operands
        // --> load x, load a, DUP2, call arrayGet, load b, call operation,call arraySet
        // since we cannot DUP2 here easily we will save the subscript and DUP x
        // --> sub=a, load x, DUP, load sub, call arrayGet, load b, call operation, load sub, call arraySet
        
        BinaryExpression arrayWithSubscript = (BinaryExpression) leftExp;
        Expression subscript = arrayWithSubscript.getRightExpression();

        // load array index: sub=a [load x, DUP, load sub, call arrayGet, load b, call operation, load sub, call arraySet]
        subscript.visit(acg);
        operandStack.doGroovyCast(int_TYPE);
        int subscriptValueId = compileStack.defineTemporaryVariable("$sub", ClassHelper.int_TYPE, true);
        
        // load array: load x and DUP [load sub, call arrayGet, load b, call operation, load sub, call arraySet] 
        arrayWithSubscript.getLeftExpression().visit(acg);
        operandStack.doGroovyCast(leftType.makeArray());
        operandStack.dup();
        
        // array get: load sub, call arrayGet [load b, call operation, load sub, call arraySet]
        operandStack.load(ClassHelper.int_TYPE, subscriptValueId);
        bew.arrayGet(LEFT_SQUARE_BRACKET, false);
        operandStack.replace(leftType, 2);
        
        // complete rhs: load b, call operation [load sub, call arraySet]
        binExp.getRightExpression().visit(acg);
        if (! (bew instanceof BinaryObjectExpressionHelper)) {
            // in primopts we convert to the left type for supported binary operations
            operandStack.doGroovyCast(leftType);  
        }
        bew.write(operation, false);
        
        // let us save that value for the return
        operandStack.dup();
        int resultValueId = compileStack.defineTemporaryVariable("$result", rightType, true);

        // array set: load sub, call arraySet []
        operandStack.load(ClassHelper.int_TYPE, subscriptValueId);
        operandStack.swap();
        bew.arraySet(false);
        operandStack.remove(3); // 3 operands, the array, the index and the value!

        // load return value
        operandStack.load(rightType, resultValueId);
        
        // cleanup
        compileStack.removeVar(resultValueId);
        compileStack.removeVar(subscriptValueId);
        return true;
    }
    
    @Override
    protected void evaluateBinaryExpressionWithAssignment(String method, BinaryExpression binExp) {
        if (doAssignmentToArray(binExp)) return;
        if (doAssignmentToLocalVariable(method, binExp)) return;
        super.evaluateBinaryExpressionWithAssignment(method, binExp);
    }

    private boolean doAssignmentToLocalVariable(String method, BinaryExpression binExp) {
        Expression left = binExp.getLeftExpression();
        if (left instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) left;
            Variable v = ve.getAccessedVariable();
            if (v instanceof DynamicVariable) return false;
            if (v instanceof PropertyExpression) return false;
            /* field and declaration we don't return false */
        } else {
            return false;
        }
        
        evaluateBinaryExpression(method, binExp);
        getController().getOperandStack().dup();
        getController().getCompileStack().pushLHS(true);
        binExp.getLeftExpression().visit(getController().getAcg());
        getController().getCompileStack().popLHS();
        
        return true;
    }

    @Override
    protected void assignToArray(Expression orig, Expression receiver, Expression index, Expression rhsValueLoader, boolean safe) {
        ClassNode current = getController().getClassNode();
        ClassNode arrayType = getController().getTypeChooser().resolveType(receiver, current);
        ClassNode arrayComponentType = arrayType.getComponentType();
        int operationType = getOperandType(arrayComponentType);
        BinaryExpressionWriter bew = binExpWriter[operationType];
        AsmClassGenerator acg = getController().getAcg();
        
        if (bew.arraySet(true) && arrayType.isArray() && !safe) {
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
            super.assignToArray(orig, receiver, index, rhsValueLoader, safe);
        }
    }
    
    @Override
    protected void writePostOrPrefixMethod(int op, String method, Expression expression, Expression orig) {
        ClassNode type = getController().getTypeChooser().resolveType(orig, getController().getClassNode());
        int operationType = getOperandType(type);
        BinaryExpressionWriter bew = binExpWriter[operationType];
        if (bew.writePostOrPrefixMethod(op,true)) {
            OperandStack operandStack   =   getController().getOperandStack();
            // at this point the receiver will be already on the stack
            operandStack.doGroovyCast(type);
            bew.writePostOrPrefixMethod(op,false);
            operandStack.replace(bew.getNormalOpResultType());
        } else {
            super.writePostOrPrefixMethod(op, method, expression, orig);
        }
    }
}
