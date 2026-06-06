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

import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.ClassGeneratorException;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.Logger.Level.ERROR;

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveByte;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveChar;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveDouble;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveFloat;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveInt;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveLong;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveShort;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveVoid;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.D2F;
import static org.objectweb.asm.Opcodes.D2I;
import static org.objectweb.asm.Opcodes.D2L;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.DUP_X2;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.F2I;
import static org.objectweb.asm.Opcodes.F2L;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2C;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.I2F;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.L2F;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.SWAP;

/**
 * Tracks the JVM operand stack during bytecode generation, maintaining a
 * parallel list of {@link org.codehaus.groovy.ast.ClassNode} type descriptors for each slot.
 * Used to emit correct pop/cast/box instructions and to verify stack discipline.
 */
public class OperandStack {

    private static final System.Logger LOGGER = System.getLogger(OperandStack.class.getName());

    private final List<ClassNode> stack = new ArrayList<>();
    private final WriterController controller;

    /**
     * Creates an operand stack tracker backed by the given controller.
     *
     * @param controller the writer controller for the current compilation
     */
    public OperandStack(final WriterController controller) {
        this.controller = controller;
    }

    /** Returns the number of tracked entries on the operand stack. */
    public int getStackLength() {
        return stack.size();
    }

    /**
     * Pops all stack entries above {@code elements}, emitting the appropriate
     * {@code POP} or {@code POP2} instruction for each popped value.
     *
     * @param elements the target stack depth to reduce to
     */
    public void popDownTo(final int elements) {
        int last = stack.size();
        MethodVisitor mv = controller.getMethodVisitor();
        while (last > elements) {
            last -= 1;
            ClassNode element = popWithMessage(last);
            if (isTwoSlotType(element)) {
                mv.visitInsn(POP2);
            } else {
                mv.visitInsn(POP);
            }
        }
    }

    private ClassNode popWithMessage(final int last) {
        try {
            return stack.remove(last);
        } catch (IndexOutOfBoundsException e) { //GROOVY-10458
            String method = controller.getMethodNode() != null
                    ? controller.getMethodNode().getTypeDescriptor()
                    : controller.getConstructorNode().getTypeDescriptor();
            throw new GroovyBugError("Error while popping argument from operand stack tracker in class " + controller.getClassName() + " method " + method + ".");
        }
    }

    /**
     * returns true for long and double
     */
    private static boolean isTwoSlotType(final ClassNode type) {
        return isPrimitiveLong(type) || isPrimitiveDouble(type);
    }

    /**
     * ensure last marked parameter on the stack is a primitive boolean
     * if mark==stack size, we assume an empty expression or statement.
     * was used and we will use the value given in emptyDefault as boolean
     * if mark==stack.size()-1 the top element will be cast to boolean using
     * Groovy truth.
     * In other cases we throw a GroovyBugError
     */
    public void castToBool(final int mark, final boolean emptyDefault) {
        int size = stack.size();
        MethodVisitor mv = controller.getMethodVisitor();
        if (mark == size) {
            // no element, so use emptyDefault
            mv.visitLdcInsn(emptyDefault ? 1 : 0);
            stack.add(null);
        } else if (mark == size - 1) {
            ClassNode last = stack.get(size - 1);
            // nothing to do in that case
            if (isPrimitiveBoolean(last)) return;
            if (ClassHelper.isPrimitiveType(last)) {
                BytecodeHelper.convertPrimitiveToBoolean(mv, last);
            } else {
                controller.getInvocationWriter().castNonPrimitiveToBool(last);
            }
        } else {
            throw new GroovyBugError("operand stack contains " + size + " elements, but we expected only " + mark);
        }
        stack.set(mark, ClassHelper.boolean_TYPE);
    }

    /**
     * remove operand stack top element using bytecode pop
     */
    public void pop() {
        popDownTo(stack.size() - 1);
    }

    /**
     * Emits a conditional jump instruction and returns the newly created target label.
     * The boolean operand is removed from the tracked stack.
     *
     * @param ifIns the branch opcode (e.g. {@code IFEQ}, {@code IFNE})
     * @return the label that the jump will branch to
     */
    public Label jump(final int ifIns) {
        Label label = new Label();
        jump(ifIns,label);
        return label;
    }

    /**
     * Emits a conditional jump to the given label.
     * The boolean operand is removed from the tracked stack.
     *
     * @param ifIns the branch opcode (e.g. {@code IFEQ}, {@code IFNE})
     * @param label the branch target
     */
    public void jump(final int ifIns, final Label label) {
        controller.getMethodVisitor().visitJumpInsn(ifIns, label);
        // remove the boolean from the operand stack tracker
        remove(1);
    }

    /**
     * duplicate top element
     */
    public void dup() {
        ClassNode type = getTopOperand();
        stack.add(type);
        MethodVisitor mv = controller.getMethodVisitor();
        if (isTwoSlotType(type)) {
            mv.visitInsn(DUP2);
        } else {
            mv.visitInsn(DUP);
        }
    }

    /**
     * Boxes the top operand if it is a primitive type, emitting the appropriate
     * wrapper-creation bytecode, and returns the resulting (possibly wrapped) type.
     *
     * @return the type of the top operand after boxing
     */
    public ClassNode box() {
        MethodVisitor mv = controller.getMethodVisitor();
        int size = stack.size();
        ClassNode type = stack.get(size - 1);
        if (ClassHelper.isPrimitiveType(type) && !isPrimitiveVoid(type)) {
            ClassNode wrapper = ClassHelper.getWrapper(type);
            BytecodeHelper.doCastToWrappedType(mv, type, wrapper);
            type = wrapper;
        } // else nothing to box
        stack.set(size - 1, type);
        return type;
    }

    /**
     * Remove amount elements from the operand stack, without using pop.
     * For example after a method invocation
     */
    public void remove(final int amount) {
        int size = stack.size();
        for (int i = size - 1, n = size - 1 - amount; i > n; i -= 1) {
            popWithMessage(i);
        }
    }

    /**
     * push operand on stack
     */
    public void push(final ClassNode type) {
        stack.add(type);
    }

    /**
     * swap two top level operands
     */
    public void swap() {
        MethodVisitor mv = controller.getMethodVisitor();
        int size = stack.size();
        ClassNode b = stack.get(size - 1);
        ClassNode a = stack.get(size - 2);
        //        dup_x1:     ---
        //        dup_x2:     aab  -> baab
        //        dup2_x1:    abb  -> bbabb
        //        dup2_x2:    aabb -> bbaabb
        //        b = top element, a = element under b
        //        top element at right
        if (isTwoSlotType(a)) { // aa
            if (isTwoSlotType(b)) { // aabb
                // aabb -> bbaa
                mv.visitInsn(DUP2_X2);   // bbaabb
                mv.visitInsn(POP2);      // bbaa
            } else {
                // aab -> baa
                mv.visitInsn(DUP_X2);   // baab
                mv.visitInsn(POP);      // baa
            }
        } else { // a
            if (isTwoSlotType(b)) { //abb
                // abb -> bba
                mv.visitInsn(DUP2_X1);   // bbabb
                mv.visitInsn(POP2);      // bba
            } else {
                // ab -> ba
                mv.visitInsn(SWAP);
            }
        }
        stack.set(size - 1, a);
        stack.set(size - 2, b);
    }

    /**
     * replace top level element with new element of given type
     */
    public void replace(final ClassNode type) {
        int size = ensureStackNotEmpty(stack);
        stack.set(size - 1, type);
    }

    private int ensureStackNotEmpty(final List<ClassNode> stack) {
        int size = stack.size();

        try {
            if (size == 0) throw new ArrayIndexOutOfBoundsException("size==0");
        } catch (ArrayIndexOutOfBoundsException ai) {
            LOGGER.log(ERROR, "Index problem in {0}", controller.getSourceUnit().getName());
            throw ai;
        }

        return size;
    }

    /**
     * replace n top level elements with new element of given type
     */
    public void replace(final ClassNode type, final int n) {
        remove(n);
        push(type);
    }

    /**
     * do Groovy cast for top level element
     */
    public void doGroovyCast(final ClassNode targetType) {
        doConvertAndCast(targetType, false);
    }

    /**
     * Performs a Groovy cast of the top operand to the declared origin type of {@code v}.
     *
     * @param v the variable whose origin type is the cast target
     */
    public void doGroovyCast(final Variable v) {
        doConvertAndCast(v.getOriginType(), false);
    }

    /**
     * Performs an {@code as}-coercion of the top operand to {@code targetType}.
     *
     * @param targetType the target type
     */
    public void doAsType(final ClassNode targetType) {
        doConvertAndCast(targetType,true);
    }

    private String missingOperand(final ClassNode targetType, final boolean coerce) {
        var sb = new StringBuilder("Internal compiler error while compiling ");
        sb.append(controller.getSourceUnit().getName());
        sb.append("\n");
        var constructorNode = controller.getConstructorNode();
        if (constructorNode != null) {
            sb.append("Constructor: ");
            sb.append(constructorNode);
            sb.append("\n");
        } else {
            var methodNode = controller.getMethodNode();
            if (methodNode != null) {
                sb.append("Method: ");
                sb.append(methodNode);
                sb.append("\n");
            }
        }
        sb.append("Line ").append(controller.getLineNumber()).append(", expecting ").append(coerce ? "coercion" : "casting");
        sb.append(" to ").append(ClassNodeUtils.formatTypeName(targetType));
        sb.append(" but operand stack is empty");
        return sb.toString();
    }

    private void doConvertAndCast(ClassNode targetType, final boolean coerce) {
        int size = stack.size();
        if (size == 0) {
            throw new ArrayIndexOutOfBoundsException(missingOperand(targetType, coerce));
        }

        ClassNode top = stack.get(size - 1);
        if (top == (targetType = targetType.redirect()) // quick check
                || ClassNodeUtils.isCompatibleWith(top, targetType)) {
            return;
        }

        if (coerce) {
            controller.getInvocationWriter().coerce(top, targetType);
            return;
        }

        boolean primTarget = ClassHelper.isPrimitiveType(targetType);
        boolean primTop = ClassHelper.isPrimitiveType(top);

        if (primTop && primTarget) {
            // here we box and unbox to get the goal type
            if (convertPrimitive(top, targetType)) {
                replace(targetType);
                return;
            }
            box();
        } else if (primTarget) {
            // top is not primitive so unbox
            // leave that BH#doCast later
        } else {
            // top might be primitive, target is not
            // so let invocation writer box if needed and do groovy cast otherwise
            controller.getInvocationWriter().castToNonPrimitiveIfNecessary(top, targetType);
        }

        MethodVisitor mv = controller.getMethodVisitor();
        if (primTarget && !isPrimitiveBoolean(targetType)
                && !primTop && ClassHelper.getWrapper(targetType).equals(top)) {
            BytecodeHelper.doCastToPrimitive(mv, top, targetType);
        } else {
            top = stack.get(size - 1);
            if (!WideningCategories.implementsInterfaceOrSubclassOf(top, targetType)) {
                BytecodeHelper.doCast(mv, targetType);
            }
        }
        replace(targetType);
    }

    private boolean convertFromInt(final ClassNode target) {
        int convertCode;
        if (isPrimitiveChar(target)) {
            convertCode = I2C;
        } else if (isPrimitiveByte(target)) {
            convertCode = I2B;
        } else if (isPrimitiveShort(target)) {
            convertCode = I2S;
        } else if (isPrimitiveLong(target)) {
            convertCode = I2L;
        } else if (isPrimitiveFloat(target)) {
            convertCode = I2F;
        } else if (isPrimitiveDouble(target)) {
            convertCode = I2D;
        } else {
            return false;
        }
        controller.getMethodVisitor().visitInsn(convertCode);
        return true;
    }

    private boolean convertFromLong(final ClassNode target) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (isPrimitiveInt(target)) {
            mv.visitInsn(L2I);
            return true;
        } else if (isPrimitiveChar(target)
                || isPrimitiveByte(target)
                || isPrimitiveShort(target)) {
            mv.visitInsn(L2I);
            return convertFromInt(target);
        } else if (isPrimitiveDouble(target)) {
            mv.visitInsn(L2D);
            return true;
        } else if (isPrimitiveFloat(target)) {
            mv.visitInsn(L2F);
            return true;
        }
        return false;
    }

    private boolean convertFromDouble(final ClassNode target) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (isPrimitiveInt(target)) {
            mv.visitInsn(D2I);
            return true;
        } else if (isPrimitiveChar(target)
                || isPrimitiveByte(target)
                || isPrimitiveShort(target)) {
            mv.visitInsn(D2I);
            return convertFromInt(target);
        } else if (isPrimitiveLong(target)) {
            mv.visitInsn(D2L);
            return true;
        } else if (isPrimitiveFloat(target)) {
            mv.visitInsn(D2F);
            return true;
        }
        return false;
    }

    private boolean convertFromFloat(final ClassNode target) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (isPrimitiveInt(target)) {
            mv.visitInsn(F2I);
            return true;
        } else if (isPrimitiveChar(target)
                || isPrimitiveByte(target)
                || isPrimitiveShort(target)) {
            mv.visitInsn(F2I);
            return convertFromInt(target);
        } else if (isPrimitiveLong(target)) {
            mv.visitInsn(F2L);
            return true;
        } else if (isPrimitiveDouble(target)) {
            mv.visitInsn(F2D);
            return true;
        }
        return false;
    }

    private boolean convertPrimitive(final ClassNode top, final ClassNode target) {
        if (top == target)
            return true;
        if (isPrimitiveInt(top)) {
            return convertFromInt(target);
        } else if (isPrimitiveChar(top)
                || isPrimitiveByte(top)
                || isPrimitiveShort(top)) {
            return isPrimitiveInt(target) || convertFromInt(target);
        } else if (isPrimitiveFloat(top)) {
            return convertFromFloat(target);
        } else if (isPrimitiveDouble(top)) {
            return convertFromDouble(target);
        } else if (isPrimitiveLong(top)) {
            return convertFromLong(target);
        }
        return false;
    }

    /**
     * load the constant on the operand stack.
     */
    public void pushConstant(final ConstantExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        Object value = expression.getValue();
        ClassNode exprType = expression.getType();
        ClassNode type = ClassHelper.getUnwrapper(exprType);
        boolean boxing = !exprType.equals(type);
        boolean primitive = boxing || ClassHelper.isPrimitiveType(type);

        if (value == null) {
            mv.visitInsn(ACONST_NULL);
            type = ClassHelper.OBJECT_TYPE;
        } else if (boxing && value instanceof Boolean) { // load static value
            String text = (Boolean) value ? "TRUE" : "FALSE";
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", text, "Ljava/lang/Boolean;");
            boxing = false;
            type = exprType;
        } else if (primitive) {
            pushPrimitiveConstant(mv, value, type);
        } else if (value instanceof BigDecimal) {
            newInstance(mv, value);
        } else if (value instanceof BigInteger) {
            newInstance(mv, value);
        } else if (value instanceof String) {
            mv.visitLdcInsn(value);
        } else {
            throw new ClassGeneratorException(
                    "Cannot generate bytecode for constant: " + value + " of type: " + type.getName());
        }

        push(type);
        if (boxing) box();
    }

    private static void newInstance(final MethodVisitor mv, final Object value) {
        String className = BytecodeHelper.getClassInternalName(value.getClass().getName());
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(value.toString());
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "(Ljava/lang/String;)V", false);
    }

    private static void pushPrimitiveConstant(final MethodVisitor mv, final Object value, final ClassNode type) {
        boolean isInt = isPrimitiveInt(type);
        boolean isShort = isPrimitiveShort(type);
        boolean isByte = isPrimitiveByte(type);
        boolean isChar = isPrimitiveChar(type);
        if (isInt || isShort || isByte || isChar) {
            int val = isInt ? (Integer) value : isShort ? (Short) value : isChar ? (Character) value : (Byte) value;
            mv.visitLdcInsn(val);
        } else if (isPrimitiveLong(type) || isPrimitiveFloat(type) || isPrimitiveDouble(type)) {
            mv.visitLdcInsn(value);
        } else if (isPrimitiveBoolean(type)) {
            mv.visitLdcInsn((Boolean) value ? 1 : 0);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    /**
     * Pushes the name expression as a {@code String} onto the operand stack.
     * Constant string expressions are pushed as LDC literals; other expressions
     * are cast to {@code String} via the Groovy runtime.
     *
     * @param name the expression representing the method or property name
     */
    public void pushDynamicName(final Expression name) {
        if (name instanceof ConstantExpression ce) {
            Object value = ce.getValue();
            if (value instanceof String) {
                pushConstant(ce);
                return;
            }
        }
        new CastExpression(ClassHelper.STRING_TYPE, name).visit(controller.getAcg());
    }

    /**
     * Either loads or stores a local variable depending on the current LHS flag.
     * If in LHS context, stores the top of the operand stack into {@code variable};
     * otherwise loads the variable's value onto the stack.
     *
     * @param variable              the bytecode variable to load or store
     * @param useReferenceDirectly  if {@code true}, loads the {@link groovy.lang.Reference}
     *                              wrapper directly rather than unboxing it
     */
    public void loadOrStoreVariable(final BytecodeVariable variable, final boolean useReferenceDirectly) {
        CompileStack compileStack = controller.getCompileStack();
        if (compileStack.isLHS()) {
            storeVar(variable);
        } else {
            MethodVisitor mv = controller.getMethodVisitor();
            int idx = variable.getIndex();
            ClassNode type = variable.getType();

            if (variable.isHolder()) {
                mv.visitVarInsn(ALOAD, idx);
                if (!useReferenceDirectly) {
                    mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;", false);
                    BytecodeHelper.doCast(mv, type);
                    push(type);
                } else {
                    push(ClassHelper.REFERENCE_TYPE);
                }
            } else {
                load(type, idx);
            }
        }
    }

    /**
     * Stores the top operand stack value into {@code variable}, casting to the variable's
     * declared type. Handles both plain variables and {@link groovy.lang.Reference}-wrapped
     * closure-shared variables.
     *
     * @param variable the bytecode variable slot to store into
     */
    public void storeVar(final BytecodeVariable variable) {
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode type = variable.getType();

        doGroovyCast(type);
        if (variable.isHolder()) {
            box();
            mv.visitVarInsn(ALOAD, variable.getIndex());
            mv.visitTypeInsn(CHECKCAST, "groovy/lang/Reference");
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V", false);
        } else {
            if (!getTopOperand().equals(type) && controller.getCompileStack().hasBlockRecorder()) {
                // GROOVY-9805: force type in the stackmap
                mv.visitTypeInsn(CHECKCAST, type.isArray()
                        ? BytecodeHelper.getTypeDescription(type)
                        : BytecodeHelper.getClassInternalName(type.getName()));
            }
            BytecodeHelper.store(mv, type, variable.getIndex());
        }

        remove(1); // remove RHS value from operand stack
    }

    /**
     * Emits a load instruction for the given type from the specified local variable slot
     * and pushes the type onto the tracked stack.
     *
     * @param type the type of the value to load
     * @param idx  the local variable slot index
     */
    public void load(final ClassNode type, final int idx) {
        MethodVisitor mv = controller.getMethodVisitor();
        BytecodeHelper.load(mv, type, idx);
        push(type);
    }

    /**
     * Pushes an integer literal ({@code 0} or {@code 1}) representing a boolean value
     * and tracks it as {@code boolean} on the operand stack.
     *
     * @param value the boolean value to push
     */
    public void pushBool(final boolean value) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitLdcInsn(value ? 1 : 0);
        push(ClassHelper.boolean_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "OperandStack(size=" + stack.size() + ":" + stack.toString() + ")";
    }

    /** Returns the type of the top element on the tracked operand stack without removing it. */
    public ClassNode getTopOperand() {
        int size = ensureStackNotEmpty(stack);
        return stack.get(size - 1);
    }
}
