/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.classgen;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

/**
 * A helper class for bytecode generation with AsmClassGenerator.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:b55r@sina.com">Bing Ran</a>
 * @version $Revision$
 */
public class BytecodeHelper implements Opcodes {

    private MethodVisitor cv;

    public MethodVisitor getMethodVisitor() {
        return cv;
    }

    public BytecodeHelper(MethodVisitor cv) {
        this.cv = cv;
    }
    
    /**
     * box the primitive value on the stack
     * @param type
     */
    public void quickBoxIfNecessary(ClassNode type) {
        String descr = getTypeDescription(type);
        if (type == ClassHelper.boolean_TYPE) {
            boxBoolean();
        }
        else if (ClassHelper.isPrimitiveType(type) && type != ClassHelper.VOID_TYPE) {
            ClassNode wrapper = ClassHelper.getWrapper(type);
            String internName = getClassInternalName(wrapper);
            cv.visitTypeInsn(NEW, internName);
            cv.visitInsn(DUP);
            if (type==ClassHelper.double_TYPE || type==ClassHelper.long_TYPE) {
                cv.visitInsn(DUP2_X2);
                cv.visitInsn(POP2);
            } else {
                cv.visitInsn(DUP2_X1);
                cv.visitInsn(POP2);
            }
            cv.visitMethodInsn(INVOKESPECIAL, internName, "<init>", "(" + descr + ")V");
        }
    }
    
    public void quickUnboxIfNecessary(ClassNode type){
        if (ClassHelper.isPrimitiveType(type) && type != ClassHelper.VOID_TYPE) { // todo care when BigDecimal or BigIneteger on stack
            ClassNode wrapper = ClassHelper.getWrapper(type);
            String internName = getClassInternalName(wrapper);
            if (type == ClassHelper.boolean_TYPE) {
                cv.visitTypeInsn(CHECKCAST, internName);
                cv.visitMethodInsn(INVOKEVIRTUAL, internName, type.getName() + "Value", "()" + getTypeDescription(type));
            } else { // numbers
                cv.visitTypeInsn(CHECKCAST, "java/lang/Number");
                cv.visitMethodInsn(INVOKEVIRTUAL, /*internName*/"java/lang/Number", type.getName() + "Value", "()" + getTypeDescription(type));
            }
        }
    }
    
    /**
     * Generates the bytecode to autobox the current value on the stack
     */
    public void box(Class type) {
        if (type.isPrimitive() && type != void.class) {
            String returnString = "(" + getTypeDescription(type) + ")Ljava/lang/Object;";
            cv.visitMethodInsn(INVOKESTATIC, getClassInternalName(DefaultTypeTransformation.class.getName()), "box", returnString);
        }
    }

    public void box(ClassNode type) {
        if (type.isPrimaryClassNode()) return;
        box(type.getTypeClass());
    }

    /**
     * Generates the bytecode to unbox the current value on the stack
     */
    public void unbox(Class type) {
        if (type.isPrimitive() && type != Void.TYPE) {
            String returnString = "(Ljava/lang/Object;)" + getTypeDescription(type);
            cv.visitMethodInsn(
                INVOKESTATIC,
                getClassInternalName(DefaultTypeTransformation.class.getName()),
                type.getName() + "Unbox",
                returnString);
        }
    }
    
    public void unbox(ClassNode type) {
        if (type.isPrimaryClassNode()) return;
        unbox(type.getTypeClass());
    }

    public static String getClassInternalName(ClassNode t){
    	if (t.isPrimaryClassNode()){
    		return getClassInternalName(t.getName());
    	}
        return getClassInternalName(t.getTypeClass());
    }
    
    public static String getClassInternalName(Class t) {
        return org.objectweb.asm.Type.getInternalName(t);
    }
    
    /**
     * @return the ASM internal name of the type
     */
    public static String getClassInternalName(String name) {
        return name.replace('.', '/');
    }
    
    /**
     * @return the ASM method type descriptor
     */
    public static String getMethodDescriptor(ClassNode returnType, Parameter[] parameters) {
        StringBuffer buffer = new StringBuffer("(");
        for (int i = 0; i < parameters.length; i++) {
            buffer.append(getTypeDescription(parameters[i].getType()));
        }
        buffer.append(")");
        buffer.append(getTypeDescription(returnType));
        return buffer.toString();
    }

    /**
     * @return the ASM method type descriptor
     */
    public static String getMethodDescriptor(Class returnType, Class[] paramTypes) {
        // lets avoid class loading
        StringBuffer buffer = new StringBuffer("(");
        for (int i = 0; i < paramTypes.length; i++) {
            buffer.append(getTypeDescription(paramTypes[i]));
        }
        buffer.append(")");
        buffer.append(getTypeDescription(returnType));
        return buffer.toString();
    }

    public static String getTypeDescription(Class c) {
        return org.objectweb.asm.Type.getDescriptor(c);
    }
    
    /**
     * array types are special:
     * eg.: String[]: classname: [Ljava.lang.String;
     *      Object:   classname: java.lang.Object
     *      int[] :   classname: [I
     * unlike getTypeDescription '.' is not replaces by '/'. 
     * it seems that makes problems for
     * the class loading if '.' is replaced by '/'
     * @return the ASM type description for class loading
     */
    public static String getClassLoadingTypeDescription(ClassNode c) {
        StringBuffer buf = new StringBuffer();
        boolean array = false;
        while (true) {
            if (c.isArray()) {
                buf.append('[');
                c = c.getComponentType();
                array = true;
            } else {
                if (ClassHelper.isPrimitiveType(c)) {
                    buf.append(getTypeDescription(c));
                } else {
                    if (array) buf.append('L');
                    buf.append(c.getName());
                    if(array) buf.append(';');
                }
                return buf.toString();
            }
        }
    }
    
    /**
     * array types are special:
     * eg.: String[]: classname: [Ljava/lang/String;
     *      int[]: [I
     * @return the ASM type description
     */
    public static String getTypeDescription(ClassNode c) {
        StringBuffer buf = new StringBuffer();
        ClassNode d = c;
        while (true) {
            if (ClassHelper.isPrimitiveType(d)) {
                char car;
                if (d == ClassHelper.int_TYPE) {
                    car = 'I';
                } else if (d == ClassHelper.VOID_TYPE) {
                    car = 'V';
                } else if (d == ClassHelper.boolean_TYPE) {
                    car = 'Z';
                } else if (d == ClassHelper.byte_TYPE) {
                    car = 'B';
                } else if (d == ClassHelper.char_TYPE) {
                    car = 'C';
                } else if (d == ClassHelper.short_TYPE) {
                    car = 'S';
                } else if (d == ClassHelper.double_TYPE) {
                    car = 'D';
                } else if (d == ClassHelper.float_TYPE) {
                    car = 'F';
                } else /* long */{
                    car = 'J';
                }
                buf.append(car);
                return buf.toString();
            } else if (d.isArray()) {
                buf.append('[');
                d = d.getComponentType();
            } else {
                buf.append('L');
                String name = d.getName();
                int len = name.length();
                for (int i = 0; i < len; ++i) {
                    char car = name.charAt(i);
                    buf.append(car == '.' ? '/' : car);
                }
                buf.append(';');
                return buf.toString();
            }
        }
    }

    /**
     * @return an array of ASM internal names of the type
     */
    public static String[] getClassInternalNames(ClassNode[] names) {
        int size = names.length;
        String[] answer = new String[size];
        for (int i = 0; i < size; i++) {
            answer[i] = getClassInternalName(names[i]);
        }
        return answer;
    }

    protected void pushConstant(boolean value) {
        if (value) {
            cv.visitInsn(ICONST_1);
        }
        else {
            cv.visitInsn(ICONST_0);
        }
    }

    protected void pushConstant(int value) {
        switch (value) {
            case 0 :
                cv.visitInsn(ICONST_0);
                break;
            case 1 :
                cv.visitInsn(ICONST_1);
                break;
            case 2 :
                cv.visitInsn(ICONST_2);
                break;
            case 3 :
                cv.visitInsn(ICONST_3);
                break;
            case 4 :
                cv.visitInsn(ICONST_4);
                break;
            case 5 :
                cv.visitInsn(ICONST_5);
                break;
            default :
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    cv.visitIntInsn(BIPUSH, value);
                }
                else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    cv.visitIntInsn(SIPUSH, value);
                }
                else {
                    cv.visitLdcInsn(new Integer(value));
                }
        }
    }

    public void doCast(Class type) {
        if (type!=Object.class) {
            if (type.isPrimitive() && type!=Void.TYPE) {
                unbox(type);
            }
            else {
                cv.visitTypeInsn(
                    CHECKCAST,
                    type.isArray() ? getTypeDescription(type) : getClassInternalName(type.getName()));
            }
        }
    }
    
    public void doCast(ClassNode type) {
        if (type==ClassHelper.OBJECT_TYPE) return;
        if (ClassHelper.isPrimitiveType(type) && type!=ClassHelper.VOID_TYPE) {
            unbox(type);
        }
        else {
            cv.visitTypeInsn(
                    CHECKCAST,
                    type.isArray() ? getTypeDescription(type) : getClassInternalName(type));
        }
    }

    public void load(ClassNode type, int idx) {
        if (type==ClassHelper.double_TYPE) {
            cv.visitVarInsn(DLOAD, idx);
        }
        else if (type==ClassHelper.float_TYPE) {
            cv.visitVarInsn(FLOAD, idx);
        }
        else if (type==ClassHelper.long_TYPE) {
            cv.visitVarInsn(LLOAD, idx);
        }
        else if (
            type==ClassHelper.boolean_TYPE
                || type==ClassHelper.char_TYPE
                || type==ClassHelper.byte_TYPE
                || type==ClassHelper.int_TYPE
                || type==ClassHelper.short_TYPE)
        {    
            cv.visitVarInsn(ILOAD, idx);
        }
        else {
            cv.visitVarInsn(ALOAD, idx);
        }
    }

    public void load(Variable v) {
    	load(v.getType(), v.getIndex());
    }

    public void store(Variable v, boolean markStart) {
        ClassNode type = v.getType();
        unbox(type);
        int idx = v.getIndex();

        if (type==ClassHelper.double_TYPE) {
            cv.visitVarInsn(DSTORE, idx);
        }
        else if (type==ClassHelper.float_TYPE) {
            cv.visitVarInsn(FSTORE, idx);
        }
        else if (type==ClassHelper.long_TYPE) {
            cv.visitVarInsn(LSTORE, idx);
        }
        else if (
                type==ClassHelper.boolean_TYPE
                || type==ClassHelper.char_TYPE
                || type==ClassHelper.byte_TYPE
                || type==ClassHelper.int_TYPE
                || type==ClassHelper.short_TYPE) {
            cv.visitVarInsn(ISTORE, idx);
        }
        else {
            cv.visitVarInsn(ASTORE, idx);
        }
    }

    public void store(Variable v) {
        store(v, false);
    }

    /**
     * load the constant on the operand stack. primitives auto-boxed.
     */
    void loadConstant (Object value) {
        if (value == null) {
            cv.visitInsn(ACONST_NULL);
        }
        else if (value instanceof String) {
            cv.visitLdcInsn(value);
        }
        else if (value instanceof Character) {
            String className = "java/lang/Character";
            cv.visitTypeInsn(NEW, className);
            cv.visitInsn(DUP);
            cv.visitLdcInsn(value);
            String methodType = "(C)V";
            cv.visitMethodInsn(INVOKESPECIAL, className, "<init>", methodType);
        }
        else if (value instanceof Number) {
            /** todo it would be more efficient to generate class constants */
            Number n = (Number) value;
            String className = BytecodeHelper.getClassInternalName(value.getClass().getName());
            cv.visitTypeInsn(NEW, className);
            cv.visitInsn(DUP);
            String methodType;
            if (n instanceof Integer) {
            	//pushConstant(n.intValue());
            	cv.visitLdcInsn(n);
            	methodType = "(I)V";
        	}
            else if (n instanceof Double) {
            	cv.visitLdcInsn(n);
            	methodType = "(D)V";
            }
            else if (n instanceof Float) {
            	cv.visitLdcInsn(n);
            	methodType = "(F)V";
            }
            else if (n instanceof Long) {
            	cv.visitLdcInsn(n);
            	methodType = "(J)V";
            }
            else if (n instanceof BigDecimal) {
            	cv.visitLdcInsn(n.toString());
            	methodType = "(Ljava/lang/String;)V";
            }
            else if (n instanceof BigInteger) {
            	cv.visitLdcInsn(n.toString());
            	methodType = "(Ljava/lang/String;)V";
            }
            else if (n instanceof Short) {
            	cv.visitLdcInsn(n);
            	methodType = "(S)V";
            }
            else if (n instanceof Byte) {
            	cv.visitLdcInsn(n);
            	methodType = "(B)V";
            }
            else {
        	throw new ClassGeneratorException(
                               "Cannot generate bytecode for constant: " + value
                             + " of type: " + value.getClass().getName()
                             + ".  Numeric constant type not supported.");
            }
            cv.visitMethodInsn(INVOKESPECIAL, className, "<init>", methodType);
        }
        else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            String text = (bool.booleanValue()) ? "TRUE" : "FALSE";
            cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", text, "Ljava/lang/Boolean;");
        }
        else if (value instanceof Class) {
            Class vc = (Class) value;
            if (vc.getName().equals("java.lang.Void")) {
                // load nothing here for void
            } else {
                throw new ClassGeneratorException(
                "Cannot generate bytecode for constant: " + value + " of type: " + value.getClass().getName());
            }
        }
        else {
            throw new ClassGeneratorException(
                "Cannot generate bytecode for constant: " + value + " of type: " + value.getClass().getName());
        }
    }


    /**
     * load the value of the variable on the operand stack. unbox it if it's a reference
     * @param variable
     */
    public void loadVar(Variable variable) {
		int index = variable.getIndex();
		if (variable.isHolder()) {
			cv.visitVarInsn(ALOAD, index);
			cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
		} else {
            load(variable);
            if (variable!=Variable.THIS_VARIABLE && variable!=Variable.SUPER_VARIABLE) {
                box(variable.getType());
            }
		}
	}
    
    public void storeVar(Variable variable) {
        String  type   = variable.getTypeName();
        int     index  = variable.getIndex();
        
    	if (variable.isHolder()) {
            cv.visitVarInsn(ALOAD, index);
            cv.visitInsn(SWAP);  
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        }
        else {
            store(variable,false);
        }
    }

    public void putField(FieldNode fld) {
    	putField(fld, getClassInternalName(fld.getOwner()));
    }

    public void putField(FieldNode fld, String ownerName) {
    	cv.visitFieldInsn(PUTFIELD, ownerName, fld.getName(), getTypeDescription(fld.getType()));
    }

    public void loadThis() {
        cv.visitVarInsn(ALOAD, 0);
    }
    
    public void swapObjectWith(ClassNode type) {
        if (type==ClassHelper.long_TYPE || type==ClassHelper.double_TYPE) {
            cv.visitInsn(DUP_X2);
            cv.visitInsn(POP);
        } else {
            cv.visitInsn(SWAP);
        }
    }
    
    public void swapWithObject(ClassNode type) {
        if (type==ClassHelper.long_TYPE || type==ClassHelper.double_TYPE) {
            cv.visitInsn(DUP2_X2);
            cv.visitInsn(POP);
        } else {
            cv.visitInsn(SWAP);
        }
    }

    public static ClassNode boxOnPrimitive(ClassNode type) {
        if (!type.isArray()) return ClassHelper.getWrapper(type);
        return boxOnPrimitive(type.getComponentType()).makeArray();
    }

    /**
     * convert boolean to Boolean
     */
    public void boxBoolean() {
        Label l0 = new Label();
        cv.visitJumpInsn(IFEQ, l0);
        cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
        Label l1 = new Label();
        cv.visitJumpInsn(GOTO, l1);
        cv.visitLabel(l0);
        cv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
        cv.visitLabel(l1);
    }

    /**
     * negate a boolean on stack. true->false, false->true
     */
    public void negateBoolean(){
        // code to negate the primitive boolean
        Label endLabel = new Label();
        Label falseLabel = new Label();
        cv.visitJumpInsn(IFNE,falseLabel);
        cv.visitInsn(ICONST_1);
        cv.visitJumpInsn(GOTO,endLabel);
        cv.visitLabel(falseLabel);
        cv.visitInsn(ICONST_0);
        cv.visitLabel(endLabel);
    }

    /**
     * load a message on the stack and remove it right away. Good for put a mark in the generated bytecode for debugging purpose.
     * @param msg
     */
    public void mark(String msg) {
        cv.visitLdcInsn(msg);
        cv.visitInsn(POP);
    }
    
    /**
     * returns a name that Class.forName() can take. Notablely for arrays:
     * [I, [Ljava.lang.String; etc
     * Regular object type:  java.lang.String
     * @param name
     */
    public static String formatNameForClassLoading(String name) {
        if (name.equals("int")
        		|| name.equals("long")
				|| name.equals("short")
				|| name.equals("float")
				|| name.equals("double")
				|| name.equals("byte")
				|| name.equals("char")
				|| name.equals("boolean")
				|| name.equals("void")
        	) {
            return name;
        }

        if (name == null) {
            return "java.lang.Object;";
        }

        if (name.startsWith("[")) {
            return name.replace('/', '.');
        }
        
        if (name.startsWith("L")) {
        	name = name.substring(1);
        	if (name.endsWith(";")) {
        		name = name.substring(0, name.length() - 1);
        	}
        	return name.replace('/', '.');
        }

        String prefix = "";
        if (name.endsWith("[]")) { // todo need process multi
            prefix = "[";
            name = name.substring(0, name.length() - 2);
            if (name.equals("int")) {
                return prefix + "I";
            }
            else if (name.equals("long")) {
                return prefix + "J";
            }
            else if (name.equals("short")) {
                return prefix + "S";
            }
            else if (name.equals("float")) {
                return prefix + "F";
            }
            else if (name.equals("double")) {
                return prefix + "D";
            }
            else if (name.equals("byte")) {
                return prefix + "B";
            }
            else if (name.equals("char")) {
                return prefix + "C";
            }
            else if (name.equals("boolean")) {
                return prefix + "Z";
            }
            else {
            	return prefix + "L" + name.replace('/', '.') + ";";
            }
        }
        return name.replace('/', '.');

    }

    public void dup() {
        cv.visitInsn(DUP);
    }

    public void doReturn(ClassNode returnType) {
        if (returnType==ClassHelper.double_TYPE) {
            cv.visitInsn(DRETURN);
        } else if (returnType==ClassHelper.float_TYPE) {
            cv.visitInsn(FRETURN);
        } else if (returnType==ClassHelper.long_TYPE) {
            cv.visitInsn(LRETURN);
        } else if (
                   returnType==ClassHelper.boolean_TYPE
                || returnType==ClassHelper.char_TYPE
                || returnType==ClassHelper.byte_TYPE
                || returnType==ClassHelper.int_TYPE
                || returnType==ClassHelper.short_TYPE) 
        { 
            //byte,short,boolean,int are all IRETURN
            cv.visitInsn(IRETURN);
        } else if (returnType==ClassHelper.VOID_TYPE){
            cv.visitInsn(RETURN);
        } else {
            cv.visitInsn(ARETURN);
        }
        
    }
    
}
