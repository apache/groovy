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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
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
            ClassNode element = stack.remove(last);
            if (isTwoSlotType(element)) {
                mv.visitInsn(POP2);
            } else {
                mv.visitInsn(POP);
            }
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
        if (type==ClassHelper.double_TYPE) {
            mv.visitInsn(D2I);
            mv.visitInsn(I2B);
        } else if (type==ClassHelper.long_TYPE) {
            mv.visitInsn(L2I);
            mv.visitInsn(I2B);
        } else if (type==ClassHelper.float_TYPE) {
            mv.visitInsn(F2I);
            mv.visitInsn(I2B);
        } else if (type==ClassHelper.int_TYPE) {
            mv.visitInsn(I2B);
        } 
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
        if (BytecodeHelper.box(mv, type)) {
            type = ClassHelper.getWrapper(type);
            BytecodeHelper.doCast(mv, type);
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
            try {
                stack.remove(i);
            } catch (ArrayIndexOutOfBoundsException ai) {
                System.err.println("index problem in "+controller.getSourceUnit().getName());
                throw ai;
            }
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
    
    public void doAsType(ClassNode targetType) {
        doConvertAndCast(targetType,true);
    }
    
    private void doConvertAndCast(ClassNode targetType, boolean coerce) {
        int size = stack.size();
        try {
            if (size==0) throw new ArrayIndexOutOfBoundsException("size==0");
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
            box();
        } else if (primTop) {
            // top is primitive, target is not
            // so box and do groovy cast
            box();
            (new ClassExpression(targetType)).visit(controller.getAcg());
            remove(1);
            castToTypeMethod.call(mv);
        } else if (primTarget) {
            // top is not primitive so unbox
            // leave that BH#doCast later
        } else if (!(top.isDerivedFrom(targetType))) {
            // top and target are not primitive and top is not derived from target
            (new ClassExpression(targetType)).visit(controller.getAcg());
            remove(1);
            castToTypeMethod.call(mv);
        }
        BytecodeHelper.doCast(mv,targetType);
        replace(targetType);
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
            mv.visitLdcInsn(value);
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
//                mv.visitTypeInsn(CHECKCAST, "groovy/lang/Reference");
                if (!useReferenceDirectly) {
                    mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
                    push(ClassHelper.OBJECT_TYPE);
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
