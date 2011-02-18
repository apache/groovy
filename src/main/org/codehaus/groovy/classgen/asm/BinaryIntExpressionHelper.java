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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter.StatementMeta;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.codehaus.groovy.syntax.Types.*;
import static org.objectweb.asm.Opcodes.*;

public class BinaryIntExpressionHelper extends BinaryExpressionHelper {
    
    private static final MethodCaller intArrayGet = MethodCaller.newStatic(BytecodeInterface8.class, "intArrayGet");
    private static final MethodCaller intArraySet = MethodCaller.newStatic(BytecodeInterface8.class, "intArraySet");
    
    
    private WriterController controller;
    
    /* from org.codehaus.groovy.syntax.Types
    public static final int COMPARE_NOT_EQUAL           = 120;   // !=
    public static final int COMPARE_IDENTICAL           = 121;   // ===
    public static final int COMPARE_NOT_IDENTICAL       = 122;   // !==
    public static final int COMPARE_EQUAL               = 123;   // ==
    public static final int COMPARE_LESS_THAN           = 124;   // <
    public static final int COMPARE_LESS_THAN_EQUAL     = 125;   // <=
    public static final int COMPARE_GREATER_THAN        = 126;   // >
    public static final int COMPARE_GREATER_THAN_EQUAL  = 127;   // >=
    */
    private static final int[] stdCompareCodes = {
        IF_ICMPEQ,      // COMPARE_NOT_EQUAL 
        IF_ICMPNE,      // COMPARE_IDENTICAL
        IF_ICMPEQ,      // COMPARE_NOT_IDENTICAL
        IF_ICMPNE,      // COMPARE_EQUAL
        IF_ICMPGE,      // COMPARE_LESS_THAN
        IF_ICMPGT,      // COMPARE_LESS_THAN_EQUAL
        IF_ICMPLE,      // COMPARE_GREATER_THAN
        IF_ICMPLT,      // COMPARE_GREATER_THAN_EQUAL
    };
    
    /* from org.codehaus.groovy.syntax.Types
    public static final int PLUS                        = 200;   // +
    public static final int MINUS                       = 201;   // -
    public static final int MULTIPLY                    = 202;   // *
    public static final int INTDIV                      = 204;   // \
    public static final int MOD                         = 205;   // %
    */    
    private static final int[] stdOperations = {
        IADD,           //  PLUS
        ISUB,           //  MINUS
        IMUL,           //  MULTIPLY
        0,              //  DIV, but we don't want that one
        IDIV,           //  INTDIV
        IREM,           //  MOD
    };
    
    /* from org.codehaus.groovy.syntax.Types
    public static final int PIPE                        = 340;   // |
    public static final int BITWISE_OR                  = PIPE;  // |
    public static final int BITWISE_AND                 = 341;   // &
    public static final int BITWISE_XOR                 = 342;   // ^
    */
    private static final int[] bitOp = {
        IOR,            //  BITWISE_OR / PIPE
        IAND,           //  BITWISE_AND
        IXOR,           //  BIWISE_XOR
    };    
    
    /* unhandled types from from org.codehaus.groovy.syntax.Types
    public static final int LOGICAL_OR                  = 162;   // ||
    public static final int LOGICAL_AND                 = 164;   // &&

    public static final int DIVIDE                      = 203;   // /
    public static final int STAR_STAR                   = 206;   // **
    public static final int POWER                       = STAR_STAR;   //
    
    public static final int PLUS_EQUAL                  = 210;   // +=
    public static final int MINUS_EQUAL                 = 211;   // -=
    public static final int MULTIPLY_EQUAL              = 212;   // *=
    public static final int DIVIDE_EQUAL                = 213;   // /=
    public static final int INTDIV_EQUAL                = 214;   // \=
    public static final int MOD_EQUAL                   = 215;   // %=
    public static final int POWER_EQUAL                 = 216;   // **=

    public static final int PLUS_PLUS                   = 250;   // ++
    public static final int PREFIX_PLUS_PLUS            = 251;   // ++
    public static final int POSTFIX_PLUS_PLUS           = 252;   // ++
    public static final int PREFIX_PLUS                 = 253;   // +

    public static final int MINUS_MINUS                 = 260;   // --
    public static final int PREFIX_MINUS_MINUS          = 261;   // --
    public static final int POSTFIX_MINUS_MINUS         = 262;   // --
    public static final int PREFIX_MINUS                = 263;   // - (negation)
*/
    public static final int LEFT_SHIFT                  = 280;   // <<
    public static final int RIGHT_SHIFT                 = 281;   // >>
    public static final int RIGHT_SHIFT_UNSIGNED        = 282;   // >>>

    private static final int[] shiftOp = {
        ISHL,           // LEFT_SHIFT
        ISHR,           // RIGHT_SHIFT
        IUSHR          // RIGHT_SHIFT_UNSIGNED
    };

/*
    public static final int LEFT_SHIFT_EQUAL            = 285;   // <<=
    public static final int RIGHT_SHIFT_EQUAL           = 286;   // >>=
    public static final int RIGHT_SHIFT_UNSIGNED_EQUAL  = 287;   // >>>=

    public static final int BITWISE_OR_EQUAL            = 350;   // |=
    public static final int BITWISE_AND_EQUAL           = 351;   // &=
    public static final int BITWISE_XOR_EQUAL           = 352;   // ^=
    public static final int BITWISE_NEGATION            = REGEX_PATTERN;    // ~
    */
    
    public BinaryIntExpressionHelper(WriterController wc) {
        super(wc);
        controller = wc;
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
            type = ve.getOriginType();
            if (ve.getAccessedVariable() instanceof FieldNode) {
                FieldNode fn = (FieldNode) ve.getAccessedVariable();
                if (!fn.getDeclaringClass().equals(current)) return ClassHelper.OBJECT_TYPE;
            }
        } else if (exp instanceof Variable) {
            Variable v = (Variable) exp;
            type = v.getOriginType();
        } else {
            type = exp.getType();
        }
        return type.redirect();
    }
    
    /**
     * @return true if expression is an evals to an int
     */
    protected static boolean isIntOperand(Expression exp, ClassNode current) {
        return getType(exp,current) == ClassHelper.int_TYPE;
    }
    
    @Override
    protected void evaluateCompareExpression(final MethodCaller compareMethod, BinaryExpression binExp) {
        int type = binExp.getOperation().getType();

        Expression left = binExp.getLeftExpression();
        boolean leftIsInt = isIntOperand(left, controller.getClassNode());
        Expression right = binExp.getRightExpression();
        boolean rightIsInt = isIntOperand(right, controller.getClassNode());
       
        if (leftIsInt && rightIsInt && writeIntXInt(type, true)) {
            left.visit(controller.getAcg());
            right.visit(controller.getAcg());
            writeIntXInt(type, false);
        } else {
            super.evaluateCompareExpression(compareMethod, binExp);
        }
    }
    
    @Override
    protected void evaluateBinaryExpression(final String message, BinaryExpression binExp) {
        int type = binExp.getOperation().getType();

        Expression left = binExp.getLeftExpression();
        boolean leftIsInt = isIntOperand(left, controller.getClassNode());
        Expression right = binExp.getRightExpression();
        boolean rightIsInt = isIntOperand(right, controller.getClassNode());
        OperandStack operandStack = controller.getOperandStack();
        
        if (leftIsInt && rightIsInt && writeIntXInt(type, true)) {
            left.visit(controller.getAcg());
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            right.visit(controller.getAcg());
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            writeIntXInt(type, false);
            return;
        } else if ( rightIsInt && type==LEFT_SQUARE_BRACKET &&
                    getType(left,controller.getClassNode()).getComponentType()==ClassHelper.int_TYPE)
        {
            left.visit(controller.getAcg());
            operandStack.doGroovyCast(getType(left,controller.getClassNode()));
            right.visit(controller.getAcg());
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            MethodVisitor mv = controller.getMethodVisitor();
            intArrayGet.call(mv);
            operandStack.replace(ClassHelper.int_TYPE,2);
        } else {
            super.evaluateBinaryExpression(message, binExp);
        }
    }
    
    @Override
    protected void assignToArray(Expression orig, Expression receiver, Expression index, Expression rhsValueLoader) {
        if (OptimizingStatementWriter.shouldOptimize(orig)) {
            OperandStack operandStack = controller.getOperandStack();
            MethodVisitor mv = controller.getMethodVisitor(); 
            
            // load the array
            receiver.visit(controller.getAcg());
            //operandStack.doGroovyCast(ClassHelper.int_TYPE.makeArray());
            
            // load index
            index.visit(controller.getAcg());
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            
            // load rhs
            rhsValueLoader.visit(controller.getAcg());
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            
            // store value in array
            intArraySet.call(mv);
            
            
            // load return value && correct operand stack stack
            operandStack.remove(3);
            rhsValueLoader.visit(controller.getAcg());
        } else {        
            super.assignToArray(orig, receiver, index, rhsValueLoader);
        }
    }
    
    /**
     * writes a std compare. This involves the tokens IF_ICMPEQ, IF_ICMPNE, 
     * IF_ICMPEQ, IF_ICMPNE, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE and IF_ICMPLT
     * @param type the token type
     * @return true if a successful std compare write
     */
    private boolean writeStdCompare(int type, boolean simulate) {
        type = type-COMPARE_NOT_EQUAL;
        // look if really compare
        if (type<0||type>7) return false;

        if (!simulate) {
            MethodVisitor mv = controller.getMethodVisitor();
            OperandStack operandStack = controller.getOperandStack();
            // operands are on the stack already
            int bytecode = stdCompareCodes[type];
            Label l1 = new Label();
            mv.visitJumpInsn(bytecode,l1);
            mv.visitInsn(ICONST_1);
            Label l2 = new Label();;
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l2);
            operandStack.replace(ClassHelper.boolean_TYPE, 2);
        }
        return true;
    }
    
    /**
     * writes the spaceship operator, type should be COMPARE_TO
     * @param type the token type
     * @return true if a successful spaceship operator write
     */
    private boolean writeSpaceship(int type, boolean simulate) {
        if (type != COMPARE_TO) return false;
        /*  
           we will actually do
         
          (x < y) ? -1 : ((x == y) ? 0 : 1)
          which is the essence of what the call with Integers would do
          this compiles to something along
          
              <x>
              <y>
              IF_ICMPGE L1
              ICONST_M1
              GOTO L2
          L1
              <x>
              <y>
              IF_ICMPNE L3
              ICONST_0
              GOTO L2
          L3
              ICONST_1
          L2
          
          since the operators are already on the stack and we don't want
          to load them again, we will instead duplicate them. This will
          require some pop actions in the branches!
          
              DUP2          (operands: IIII) 
              IF_ICMPGE L1  (operands: II)
              ICONST_M1     (operands: III)
              GOTO L2
          L1
              -----         (operands: II)
              IF_ICMPNE L3  (operands: -)
              ICONST_0      (operands: I)
              GOTO L2
          L3
              - jump from L1 branch to here (operands: -)
              ICONST_1      (operands: I)
          L2  
          - if jump from GOTO L2 we have III, but need only I
          - if from L3 branch we get only I
          
          this means we have to pop of II before loading -1
          
        */
        if (!simulate) {
            MethodVisitor mv = controller.getMethodVisitor();
            // duplicate int arguments
            mv.visitInsn(DUP2);
            
            Label l1 = new Label();
            mv.visitJumpInsn(IF_ICMPGE,l1);
            // no jump, so -1, need to pop off surplus II
            mv.visitInsn(POP2);
            mv.visitInsn(ICONST_M1);
            Label l2 = new Label();;
            mv.visitJumpInsn(GOTO, l2);
            
            mv.visitLabel(l1);
            Label l3 = new Label();
            mv.visitJumpInsn(IF_ICMPNE,l3);
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO,l2);
            
            mv.visitLabel(l3);
            mv.visitInsn(ICONST_1);
            
            controller.getOperandStack().replace(ClassHelper.int_TYPE, 2);
        }
        return true;
    }
    
    /**
     * writes some int standard operations. type is one of IADD, ISUB, IMUL,
     * IDIV or IREM
     * @param type the token type
     * @return true if a successful std operator write
     */
    private boolean writeStdOperators(int type, boolean simulate) {
        type = type-PLUS;
        if (type<0 || type>5 || type == 3 /*DIV*/) return false;
        
        if (!simulate) {
            int bytecode = stdOperations[type];
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(ClassHelper.int_TYPE, 2);
        }
        return true;
    }

    /**
     * writes some the bitwise operations. type is one of BITWISE_OR, 
     * BITWISE_AND, BIWISE_XOR
     * @param type the token type
     * @return true if a successful bitwise operation write
     */
    private boolean writeBitwiseOp(int type, boolean simulate) {
        type = type-BITWISE_OR;
        if (type<0 || type>2) return false;

        if (!simulate) {
            int bytecode = bitOp[type];
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(ClassHelper.int_TYPE, 2);
        }
        return true;
    }

    /**
     * Write shifting operations.
     * Type is one of LEFT_SHIFT, RIGHT_SHIFT, or RIGHT_SHIFT_UNSIGNED
     *
     * @param type the token type
     * @return true on a successful shift operation write
     */
    private boolean writeShiftOp(int type, boolean simulate) {
        type = type - LEFT_SHIFT;
        if (type < 0 || type > 2) return false;

        if (!simulate) {
            int bytecode = shiftOp[type];
            controller.getMethodVisitor().visitInsn(bytecode);
            controller.getOperandStack().replace(ClassHelper.int_TYPE, 2);
        }
        return true;
    }


    private boolean writeIntXInt(int type, boolean simulate) {
        return  writeStdCompare(type, simulate)         ||
                writeSpaceship(type, simulate)          ||
                writeStdOperators(type, simulate)       ||
                writeBitwiseOp(type, simulate)          ||
                writeShiftOp(type, simulate);
    }

}
