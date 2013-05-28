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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.ClassGeneratorException;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class OperandStack {
    
    // type conversions
    private static final MethodCaller asTypeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "asType");
    private static final MethodCaller castToTypeMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "castToType");

    private WriterController controller;
    private ArrayList<ClassNode> stack = new ArrayList();

    public OperandStack(WriterController wc) {
        this.controller = wc;        
    }
    
    public int getStackLength() {
        return stack.size();
    }
    
    public void popDownTo(int elements) {
        int last = stack.size();
        MethodVisitor mv = controller.getMethodVisitor();
        while (last>elements) {
            last--;
            ClassNode element = popWithMessage(last);
            if (isTwoSlotType(element)) {
                mv.visitInsn(POP2);
            } else {
                mv.visitInsn(POP);
            }
        }   
    }
    
    private ClassNode popWithMessage(int last) {
        try {
            return stack.remove(last);
        } catch (ArrayIndexOutOfBoundsException ai) {
            String method = "";
            if (controller.getMethodNode()!=null) {
                method = controller.getMethodNode().getTypeDescriptor();
            } else {
                method = controller.getConstructorNode().getTypeDescriptor();
            }
            throw new GroovyBugError(
                    "Error while poping argument from operand stack tracker in"+
                    " class "+controller.getClassName()+" method "+method+".");
        }
    }

    /**
     * returns true for long and double
     */
    private boolean isTwoSlotType(ClassNode type) {
        return type==ClassHelper.long_TYPE || type==ClassHelper.double_TYPE;
    }

    /**
     * ensure last marked parameter on the stack is a primitive boolean
     * if mark==stack size, we assume an empty expression or statement.
     * was used and we will use the value given in emptyDefault as boolean 
     * if mark==stack.size()-1 the top element will be cast to boolean using
     * Groovy truth.
     * In other cases we throw a GroovyBugError
     */
    public void castToBool(int mark, boolean emptyDefault) {
        int size = stack.size();
        MethodVisitor mv = controller.getMethodVisitor();
        if (mark==size) {
            // no element, so use emptyDefault
            if (emptyDefault) {
                mv.visitIntInsn(BIPUSH, 1);
            } else {
                mv.visitIntInsn(BIPUSH, 0);
            }
            stack.add(null);
        } else if (mark==stack.size()-1) {
            ClassNode last =  stack.get(size-1);
            // nothing to do in that case
            if (last == ClassHelper.boolean_TYPE) return;
            // not a primitive type, so call booleanUnbox
            if (!ClassHelper.isPrimitiveType(last)) {
                BytecodeHelper.unbox(mv,ClassHelper.boolean_TYPE);
            } else {
                primitive2b(mv,last);
            }            
        } else { 
            throw new GroovyBugError(
                    "operand stack contains "+stack.size()+
                    " elements, but we expected only "+mark
                );
        }
        stack.set(mark,ClassHelper.boolean_TYPE);
    }
    
    /**
     * convert primitive (not boolean) to boolean or byte.
     * type needs to be a primitive type (not checked) 
     */
    private void primitive2b(MethodVisitor mv, ClassNode type) {
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        // for the various types we make first a 
        // kind of conversion to int using a compare
        // operation and then handle the result common
        // for all cases. In case of long that is LCMP,
        // for int nothing is to be done
        if (type==ClassHelper.double_TYPE) {
            mv.visitInsn(DCONST_0);
            mv.visitInsn(DCMPL);
        } else if (type==ClassHelper.long_TYPE) {
            mv.visitInsn(LCONST_0);
            mv.visitInsn(LCMP);
        } else if (type==ClassHelper.float_TYPE) {
            mv.visitInsn(FCONST_0);
            mv.visitInsn(FCMPL);
        } else if (type==ClassHelper.int_TYPE) {
            // nothing, see comment above
        }
        mv.visitJumpInsn(IFEQ, falseLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, trueLabel);
        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(trueLabel);
        // other cases can be used directly
    }
    
    /**
     * remove operand stack top element using bytecode pop
     */
    public void pop() {
        popDownTo(stack.size()-1);
    }

    public Label jump(int ifIns) {
        Label label = new Label();
        jump(ifIns,label);
        return label;
    }
    
    public void jump(int ifIns, Label label) {
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
        if (type == ClassHelper.double_TYPE || type == ClassHelper.long_TYPE) {
            mv.visitInsn(DUP2);
        } else {
            mv.visitInsn(DUP);
        }
    }

    public ClassNode box() {
        MethodVisitor mv = controller.getMethodVisitor();
        int size = stack.size();
        ClassNode type = stack.get(size-1);
        if (ClassHelper.isPrimitiveType(type) && ClassHelper.VOID_TYPE!=type) {
            ClassNode wrapper = ClassHelper.getWrapper(type);
            BytecodeHelper.doCastToWrappedType(mv, type, wrapper);
            type = wrapper;
        } else {
            if (BytecodeHelper.box(mv, type)) {
                type = ClassHelper.getWrapper(type);
                BytecodeHelper.doCast(mv, type);
            }
        }
        stack.set(size-1, type);
        return type;
    }

    /**
     * Remove amount elements from the operand stack, without using pop. 
     * For example after a method invocation
     */
    public void remove(int amount) {
        int size = stack.size();
        for (int i=size-1; i>size-1-amount; i--) {
            popWithMessage(i);
        }
    }

    /**
     * push operand on stack
     */
    public void push(ClassNode type) {
        stack.add(type);
    }

    /**
     * swap two top level operands
     */
    public void swap() {
        MethodVisitor mv = controller.getMethodVisitor();
        int size = stack.size();
        ClassNode b = stack.get(size-1);
        ClassNode a = stack.get(size-2);
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
        stack.set(size-1,a);
        stack.set(size-2,b);
    }

    /**
     * replace top level element with new element of given type
     */
    public void replace(ClassNode type) {
        int size = stack.size();
        try {
            if (size==0) throw new ArrayIndexOutOfBoundsException("size==0");
        } catch (ArrayIndexOutOfBoundsException ai) {
            System.err.println("index problem in "+controller.getSourceUnit().getName());
            throw ai;
        }
        stack.set(size-1, type);
    }
    
    /**
     * replace n top level elements with new element of given type
     */
    public void replace(ClassNode type, int n) {
        remove(n);
        push(type);
    }
    
    /**
     * do Groovy cast for top level element
     */
    public void doGroovyCast(ClassNode targetType) {
        doConvertAndCast(targetType,false);
    }
    
    public void doGroovyCast(Variable v) {
        ClassNode targetType = v.getOriginType();
        doConvertAndCast(targetType,false);
    }
    
    public void doAsType(ClassNode targetType) {
        doConvertAndCast(targetType,true);
    }
    
    private void doConvertAndCast(ClassNode targetType, boolean coerce) {
        int size = stack.size();
        try {
            if (size==0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Internal compiler error while compiling ").append(controller.getSourceUnit().getName()).append("\n");
                MethodNode methodNode = controller.getMethodNode();
                if (methodNode!=null) {
                    sb.append("Method: ");
                    sb.append(methodNode);
                    sb.append("\n");
                }
                ConstructorNode constructorNode = controller.getConstructorNode();
                if (constructorNode!=null) {
                    sb.append("Constructor: ");
                    sb.append(methodNode);
                    sb.append("\n");
                }
                sb.append("Line ").append(controller.getLineNumber()).append(",");
                sb.append(" expecting ").append(coerce ? "coercion" : "casting").append(" to ").append(targetType.toString(false));
                sb.append(" but operand stack is empty");
                throw new ArrayIndexOutOfBoundsException(sb.toString());
            }
        } catch (ArrayIndexOutOfBoundsException ai) {
            throw ai;
        }
        ClassNode top = stack.get(size-1);
        targetType = targetType.redirect();
        if (targetType == top) return;
        
        MethodVisitor mv = controller.getMethodVisitor();
        if (coerce) {
            if (top.isDerivedFrom(targetType)) return;
            box();
            (new ClassExpression(targetType)).visit(controller.getAcg());
            remove(1);
            asTypeMethod.call(mv);
            BytecodeHelper.doCast(mv,targetType);
            replace(targetType);
            return;
        }
        
        boolean primTarget = ClassHelper.isPrimitiveType(targetType);
        boolean primTop = ClassHelper.isPrimitiveType(top);

        if (primTop && primTarget) {
            //TODO: use jvm primitive conversions
            // here we box and unbox to get the goal type
            if (convertPrimitive(top, targetType)) {
                replace(targetType);
                return;
            }
            box();
        } else if (primTop) {
            // top is primitive, target is not
            // so box and do groovy cast
            ClassNode boxedType = box();
            castToTypeIfNecessary(boxedType, targetType);
        } else if (primTarget) {
            // top is not primitive so unbox
            // leave that BH#doCast later
        } else {
            castToTypeIfNecessary(top, targetType);
        }
        if (ClassHelper.isNumberType(top) && primTarget && ClassHelper.isNumberType(targetType)) {
            BytecodeHelper.doCastToPrimitive(mv, top, targetType);
        } else {
            top = stack.get(size-1);
            if (!implementsInterfaceOrSubclassOf(top, targetType)) {
                BytecodeHelper.doCast(mv,targetType);
            }
        }
        replace(targetType);
    }

    private void castToTypeIfNecessary(final ClassNode sourceType, final ClassNode targetType) {
        if (!implementsInterfaceOrSubclassOf(sourceType, targetType)) {
            MethodVisitor mv = controller.getMethodVisitor();
            (new ClassExpression(targetType)).visit(controller.getAcg());
            remove(1);
            castToTypeMethod.call(mv);
        }
    }

    /**
     * Determines if the source class implements an interface or subclasses the target type.
     * This method takes the {@link org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode lowest
     * upper bound class node} type into account, allowing to remove unnecessary casts.
     * @param source
     * @param targetType
     * @return
     */
    private static boolean implementsInterfaceOrSubclassOf(final ClassNode source, final ClassNode targetType) {
        if (source.isDerivedFrom(targetType) || source.implementsInterface(targetType)) return true;
        if (targetType instanceof WideningCategories.LowestUpperBoundClassNode) {
            WideningCategories.LowestUpperBoundClassNode lub = (WideningCategories.LowestUpperBoundClassNode) targetType;
            if (implementsInterfaceOrSubclassOf(source, lub.getSuperClass())) return true;
            for (ClassNode classNode : lub.getInterfaces()) {
                if (source.implementsInterface(classNode)) return true;
            }
        }
        return false;
    }

    private boolean convertFromInt(ClassNode target) {
        int convertCode = 0;
        if (target==ClassHelper.char_TYPE){
            convertCode = I2C;
        } else if (target==ClassHelper.byte_TYPE){
            convertCode = I2B;
        } else if (target==ClassHelper.short_TYPE){
            convertCode = I2S;
        } else if (target==ClassHelper.long_TYPE){
            convertCode = I2L;
        } else if (target==ClassHelper.float_TYPE){
            convertCode = I2F;
        } else if (target==ClassHelper.double_TYPE){
            convertCode = I2D;
        } else {
            return false;
        }
        controller.getMethodVisitor().visitInsn(convertCode);
        return true;
    }
    
    private boolean convertFromLong(ClassNode target) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (target==ClassHelper.int_TYPE){
            mv.visitInsn(L2I);
            return true;
        } else if ( target==ClassHelper.char_TYPE ||
                    target==ClassHelper.byte_TYPE ||
                    target==ClassHelper.short_TYPE)
        {
            mv.visitInsn(L2I);
            return convertFromInt(target);
        } else if (target==ClassHelper.double_TYPE){
            mv.visitInsn(L2D);
            return true;
        } else if (target==ClassHelper.float_TYPE){
            mv.visitInsn(L2F);
            return true;
        } 
        return false;
    }

    private boolean convertFromDouble(ClassNode target) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (target==ClassHelper.int_TYPE){
            mv.visitInsn(D2I);
            return true;
        } else if ( target==ClassHelper.char_TYPE ||
                    target==ClassHelper.byte_TYPE ||
                    target==ClassHelper.short_TYPE)
        {
            mv.visitInsn(D2I);
            return convertFromInt(target);
        } else if (target==ClassHelper.long_TYPE){
            mv.visitInsn(D2L);
            return true;
        } else if (target==ClassHelper.float_TYPE){
            mv.visitInsn(D2F);
            return true;
        } 
        return false;
    }    
    
    private boolean convertFromFloat(ClassNode target) {
        MethodVisitor mv = controller.getMethodVisitor();
        if (target==ClassHelper.int_TYPE){
            mv.visitInsn(F2I);
            return true;
        } else if ( target==ClassHelper.char_TYPE ||
                    target==ClassHelper.byte_TYPE ||
                    target==ClassHelper.short_TYPE)
        {
            mv.visitInsn(F2I);
            return convertFromInt(target);
        } else if (target==ClassHelper.long_TYPE){
            mv.visitInsn(F2L);
            return true;
        } else if (target==ClassHelper.double_TYPE){
            mv.visitInsn(F2D);
            return true;
        } 
        return false;
    }
    
    private boolean convertPrimitive(ClassNode top, ClassNode target) {
        if (top==target) return true;
        if (top==ClassHelper.int_TYPE) {
            return convertFromInt(target);
        } else if ( top==ClassHelper.char_TYPE || 
                    top==ClassHelper.byte_TYPE ||
                    top==ClassHelper.short_TYPE)
        {
            if (target==ClassHelper.int_TYPE) return true;
            return convertFromInt(target);
        } else if ( top==ClassHelper.float_TYPE) {
            return convertFromFloat(target);
        } else if ( top==ClassHelper.double_TYPE) {
            return convertFromDouble(target);
        } else if ( top==ClassHelper.long_TYPE) {
            return convertFromLong(target);
        }
        return false;
    }

    /**
     * load the constant on the operand stack. 
     */
    public void pushConstant(ConstantExpression expression) {
        MethodVisitor mv = controller.getMethodVisitor();
        Object value = expression.getValue();
        ClassNode type = expression.getType().redirect();
        boolean asPrimitive = ClassHelper.isPrimitiveType(type);
        
        if (value == null) {
            mv.visitInsn(ACONST_NULL);
        } else if (asPrimitive) {
            pushPrimitiveConstant(mv, value, type);
        } else if (value instanceof Character) {
            mv.visitLdcInsn(value);
            BytecodeHelper.box(mv, type); // does not change this.stack field contents
        } else if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                String className = BytecodeHelper.getClassInternalName(value.getClass().getName());
                mv.visitTypeInsn(NEW, className);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(value.toString());
                mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "(Ljava/lang/String;)V");
            } else if (value instanceof BigInteger) {
                String className = BytecodeHelper.getClassInternalName(value.getClass().getName());
                mv.visitTypeInsn(NEW, className);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(value.toString());
                mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "(Ljava/lang/String;)V");
            } else if (value instanceof Integer
                    || value instanceof Byte
                    || value instanceof Short
                    || value instanceof Long
                    || value instanceof Float
                    || value instanceof Double
                    ) {
                ClassNode primType = ClassHelper.getUnwrapper(type);
                pushPrimitiveConstant(mv, value, primType);
                type = primType;
            } else {
                mv.visitLdcInsn(value);
                BytecodeHelper.box(mv, ClassHelper.getUnwrapper(type)); // does not change this.stack field contents
                BytecodeHelper.doCast(mv, type);
            }
        } else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            String text = (bool.booleanValue()) ? "TRUE" : "FALSE";
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", text, "Ljava/lang/Boolean;");
        } else if (value instanceof String) {
            mv.visitLdcInsn(value);
        } else {
            throw new ClassGeneratorException(
                    "Cannot generate bytecode for constant: " + value + " of type: " + type.getName());
        }
        
        push(type);
    }

    private void pushPrimitiveConstant(final MethodVisitor mv, final Object value, final ClassNode type) {
        boolean isInt = ClassHelper.int_TYPE.equals(type);
        boolean isShort = ClassHelper.short_TYPE.equals(type);
        boolean isByte = ClassHelper.byte_TYPE.equals(type);
        if (isInt || isShort || isByte) {
            int val = isInt?(Integer)value:isShort?(Short)value:(Byte)value;
            switch (val) {
                case 0:
                    mv.visitInsn(ICONST_0);
                    break;
                case 1:
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    mv.visitInsn(ICONST_4);
                    break;
                case 5:
                    mv.visitInsn(ICONST_5);
                    break;
                default:
                    if (val>=Byte.MIN_VALUE && val<=Byte.MAX_VALUE) {
                        mv.visitIntInsn(BIPUSH, val);
                    } else if (val>=Short.MIN_VALUE && val<=Short.MAX_VALUE) {
                        mv.visitIntInsn(SIPUSH, val);
                    } else {
                        mv.visitLdcInsn(value);
                    }
            }
        } else if (ClassHelper.long_TYPE.equals(type)) {
            if ((Long)value==0L) {
                mv.visitInsn(LCONST_0);
            } else if ((Long)value==1L) {
                mv.visitInsn(LCONST_1);
            } else {
                mv.visitLdcInsn(value);
            }
        } else if (ClassHelper.float_TYPE.equals(type)) {
            if ((Float)value==0f) {
                mv.visitInsn(FCONST_0);
            } else if ((Float)value==1f) {
                mv.visitInsn(FCONST_1);
            } else if ((Float)value==2f) {
                mv.visitInsn(FCONST_2);
            } else {
                mv.visitLdcInsn(value);
            }
        } else if (ClassHelper.double_TYPE.equals(type)) {
            if ((Double)value==0d) {
                mv.visitInsn(DCONST_0);
            } else if ((Double)value==1d) {
                mv.visitInsn(DCONST_1);
            } else {
                mv.visitLdcInsn(value);
            }
        } else if (ClassHelper.boolean_TYPE.equals(type)) {
            boolean b = (Boolean) value;
            if (b) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public void pushDynamicName(Expression name) {
        if (name instanceof ConstantExpression) {
            ConstantExpression ce = (ConstantExpression) name;
            Object value = ce.getValue();
            if (value instanceof String) {
                pushConstant(ce);
                return;
            }
        }
        new CastExpression(ClassHelper.STRING_TYPE, name).visit(controller.getAcg());
    }

    public void loadOrStoreVariable(BytecodeVariable variable, boolean useReferenceDirectly) {
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
                    mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
                    BytecodeHelper.doCast(mv, type);
                    push(type);
                } else {
                    push(ClassHelper.REFERENCE_TYPE);
                }
            } else {
                load(type,idx);
            }
        }
    }

    public void storeVar(BytecodeVariable variable) {
        MethodVisitor mv = controller.getMethodVisitor();
        int idx = variable.getIndex();
        ClassNode type = variable.getType();
        // value is on stack
        if (variable.isHolder()) {
            doGroovyCast(type);
            box();
            mv.visitVarInsn(ALOAD, idx);
            mv.visitTypeInsn(CHECKCAST, "groovy/lang/Reference");
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        } else {
            doGroovyCast(type);
            if (type == ClassHelper.double_TYPE) {
                mv.visitVarInsn(DSTORE, idx);
            } else if (type == ClassHelper.float_TYPE) {
                mv.visitVarInsn(FSTORE, idx);
            } else if (type == ClassHelper.long_TYPE) {
                mv.visitVarInsn(LSTORE, idx);
            } else if (
                    type == ClassHelper.boolean_TYPE
                            || type == ClassHelper.char_TYPE
                            || type == ClassHelper.byte_TYPE
                            || type == ClassHelper.int_TYPE
                            || type == ClassHelper.short_TYPE) {
                mv.visitVarInsn(ISTORE, idx);
            } else {
                mv.visitVarInsn(ASTORE, idx);
            }
        }
        // remove RHS value from operand stack
        remove(1);
    }

    public void load(ClassNode type, int idx) {
        MethodVisitor mv = controller.getMethodVisitor();
        BytecodeHelper.load(mv, type, idx);
        push(type);
    }

    public void pushBool(boolean inclusive) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitLdcInsn(new Boolean(inclusive));
        push(ClassHelper.boolean_TYPE);
    }
    
    public String toString() {
        return "OperandStack(size="+stack.size()+":"+stack.toString()+")";
    }

    public ClassNode getTopOperand() {
        int size = stack.size();
        try {
            if (size==0) throw new ArrayIndexOutOfBoundsException("size==0");
        } catch (ArrayIndexOutOfBoundsException ai) {
            System.err.println("index problem in "+controller.getSourceUnit().getName());
            throw ai;
        }
        return stack.get(size-1);
    }
}
