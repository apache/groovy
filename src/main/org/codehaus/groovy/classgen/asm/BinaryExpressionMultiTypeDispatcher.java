/*
 * Copyright 2003-2010 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter.StatementMeta;

import static org.codehaus.groovy.ast.ClassHelper.*;

/**
 * This class is for internal use only!
 * This class will dispatch to the right type adapters according to the 
 * kind of binary expression that is provided.
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class BinaryExpressionMultiTypeDispatcher extends BinaryExpressionHelper {

    private static class DummyHelper implements BinaryExpressionWriter {
        @Override public boolean write(int operation, boolean simulate) {
            if (simulate) return false;
            throw new GroovyBugError("should not reach here");
        }
        @Override public boolean arrayGet(int operation, boolean simulate) {
            if (simulate) return false;
            throw new GroovyBugError("should not reach here");
        }
        @Override public boolean arraySet(boolean simulate) {
            if (simulate) return false;
            throw new GroovyBugError("should not reach here");
        }
    }
    
    private BinaryExpressionWriter[] binExpWriter = {
            new DummyHelper(),                              // 0: dummy
            new BinaryIntExpressionHelper(getController()), // 1: int
            new DummyHelper(),                              // 2: long
            new DummyHelper(),                              // 3: double
            new DummyHelper(),                              // 4: char
            new DummyHelper(),                              // 5: byte
            new DummyHelper(),                              // 6: short
            new DummyHelper(),                              // 7: float
    };
    
    private static Map<ClassNode,Integer> typeMap = new HashMap<ClassNode,Integer>(14);
    static {
        typeMap.put(int_TYPE,       1); typeMap.put(long_TYPE,      2);
        typeMap.put(double_TYPE,    3); typeMap.put(char_TYPE,      4);
        typeMap.put(byte_TYPE,      5); typeMap.put(short_TYPE,     6);
        typeMap.put(float_TYPE,     7); 
    }

    public BinaryExpressionMultiTypeDispatcher(WriterController wc) {
        super(wc);
    }

    /**
     * return the type of an expression, taking meta data into account 
     */
    protected static ClassNode getType(Expression exp, ClassNode current) {
        StatementMeta meta = (StatementMeta) exp.getNodeMetaData(StatementMeta.class);
        ClassNode type = null;
        if (meta!=null) type = meta.type;
        if (type!=null) return type;
        if (exp instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) exp;
            if (ve.isClosureSharedVariable()) return ve.getType();
            type = ve.getOriginType();
            if (ve.getAccessedVariable() instanceof FieldNode) {
                FieldNode fn = (FieldNode) ve.getAccessedVariable();
                if (!fn.getDeclaringClass().equals(current)) return OBJECT_TYPE;
            }
        } else if (exp instanceof Variable) {
            Variable v = (Variable) exp;
            type = v.getOriginType();
        } else {
            type = exp.getType();
        }
        return type.redirect();
    }
    
    private boolean isInt(ClassNode type) {
        return  type == int_TYPE    || type == char_TYPE    ||
                type == byte_TYPE   || type == short_TYPE;
    }
    
    private boolean isLong(ClassNode type) {
        return  type == long_TYPE   || isInt(type);
    }

    private boolean isDouble(ClassNode type) {
        return  type == float_TYPE  || type == double_TYPE  ||
                isLong(type);
    }
    
    private int getOperandConversionType(ClassNode leftType, ClassNode rightType) {
        if (isInt(leftType) && isInt(rightType)) return 1;
        if (isLong(leftType) && isLong(rightType)) return 2;
        if (isDouble(leftType) && isDouble(rightType)) return 3;
        return 0;
    }
    
    private int getOperandType(ClassNode type) {
        Integer ret = typeMap.get(type);
        if (ret==null) return 0;
        return ret;
    }
    
    @Override
    protected void evaluateCompareExpression(final MethodCaller compareMethod, BinaryExpression binExp) {
        ClassNode current =  getController().getClassNode();
        int operation = binExp.getOperation().getType();
        
        Expression leftExp = binExp.getLeftExpression();
        ClassNode leftType = getType(leftExp, current);
        Expression rightExp = binExp.getRightExpression();
        ClassNode rightType = getType(rightExp, current);
        
        int operationType = getOperandConversionType(leftType,rightType);
        BinaryExpressionWriter bew = binExpWriter[operationType];
        
        if (bew.write(operation, true)) {
            AsmClassGenerator acg = getController().getAcg();
            OperandStack os = getController().getOperandStack();
            leftExp.visit(acg);
            os.doGroovyCast(int_TYPE);
            rightExp.visit(acg);
            os.doGroovyCast(int_TYPE);
            bew.write(operation, false);
        } else {
            super.evaluateCompareExpression(compareMethod, binExp);
        }
    }
    
    @Override
    protected void evaluateBinaryExpression(final String message, BinaryExpression binExp) {
        int operation = binExp.getOperation().getType();
        ClassNode current =  getController().getClassNode();

        Expression leftExp = binExp.getLeftExpression();
        ClassNode leftType = getType(leftExp, current);
        Expression rightExp = binExp.getRightExpression();
        ClassNode rightType = getType(rightExp, current);

        int operationType = getOperandConversionType(leftType,rightType);
        BinaryExpressionWriter bew = binExpWriter[operationType];
        AsmClassGenerator acg = getController().getAcg();
        OperandStack os = getController().getOperandStack();
        
        if (bew.arrayGet(operation, true)) {
            leftExp.visit(acg);
            os.doGroovyCast(leftType);
            rightExp.visit(acg);
            os.doGroovyCast(int_TYPE);
            bew.arrayGet(operation, false);
        } else if (bew.write(operation, true)) {
            leftExp.visit(acg);
            os.doGroovyCast(int_TYPE);
            rightExp.visit(acg);
            os.doGroovyCast(int_TYPE);
            bew.write(operation, false);
        } else {
            super.evaluateBinaryExpression(message, binExp);
        }
    }
    
    @Override
    protected void assignToArray(Expression orig, Expression receiver, Expression index, Expression rhsValueLoader) {
        ClassNode current = getController().getClassNode();
        ClassNode arrayType = getType(receiver, current);
        int operationType = getOperandType(arrayType);
        BinaryExpressionWriter bew = binExpWriter[operationType];
        AsmClassGenerator acg = getController().getAcg();
        
        if (bew.arraySet(true)) {
            OperandStack operandStack   =   getController().getOperandStack();
            
            // load the array
            receiver.visit(acg);

            // load index
            index.visit(acg);
            operandStack.doGroovyCast(int_TYPE);
            
            // load rhs
            rhsValueLoader.visit(acg);
            operandStack.doGroovyCast(arrayType);
            
            // store value in array
            bew.arraySet(false);
            
            // load return value && correct operand stack stack
            operandStack.remove(3);
            rhsValueLoader.visit(acg);
        } else {        
            super.assignToArray(orig, receiver, index, rhsValueLoader);
        }
    }
}
