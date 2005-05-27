
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

import groovy.lang.MetaMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
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
     * Generates the bytecode to autobox the current value on the stack
     */
    public void box(Class type) {
        if (type.isPrimitive() && type != void.class) {
            String returnString = "(" + getTypeDescription(type.getName()) + ")Ljava/lang/Object;";
            cv.visitMethodInsn(INVOKESTATIC, getClassInternalName(ScriptBytecodeAdapter.class.getName()), "box", returnString);
        }
    }

    /**
     * box the primitive value on the stack
     * @param cls
     */
    public void quickBoxIfNecessary(Class cls) {
        String type = cls.getName();
        String descr = getTypeDescription(type);
        if (cls == boolean.class) {
            boxBoolean();
        }
        else if (cls.isPrimitive() && cls != void.class) {
            // use a special integer pool in the invokerhelper
            if (cls == Integer.TYPE) {
                cv.visitMethodInsn(
                        INVOKESTATIC,
                        getClassInternalName(ScriptBytecodeAdapter.class.getName()),
                        "integerValue",
                        "(I)Ljava/lang/Integer;"
                );
                return;
            }

            String wrapperName = getObjectTypeForPrimitive(type);
            String internName = getClassInternalName(wrapperName);
            cv.visitTypeInsn(NEW, internName);
            cv.visitInsn(DUP);
            if (type.equals("double") || type.equals("long")) {
                cv.visitInsn(DUP2_X2);
                cv.visitInsn(POP2);
            } else {
                cv.visitInsn(DUP2_X1);
                cv.visitInsn(POP2);
            }
            cv.visitMethodInsn(INVOKESPECIAL, internName, "<init>", "(" + descr + ")V");

//            Operand opr = new Operand(ITEM_Object, wrapperName, "", "");
//            _safePop();
//            push(opr);
        }
    }

    /**
     * unbox the ref on the stack
     * @param cls
     */
    public void quickUnboxIfNecessary(Class cls) {
        String type = cls.getName();

        if (cls.isPrimitive() && cls != void.class) { // todo care when BigDecimal or BigIneteger on stack
            String wrapperName = getObjectTypeForPrimitive(type);
            String internName = getClassInternalName(wrapperName);
            if (cls == boolean.class) {
                cv.visitTypeInsn(CHECKCAST, internName);
                cv.visitMethodInsn(INVOKEVIRTUAL, internName, type + "Value", "()" + getTypeDescription(type));
            } else { // numbers
                cv.visitTypeInsn(CHECKCAST, "java/lang/Number");
                cv.visitMethodInsn(INVOKEVIRTUAL, /*internName*/"java/lang/Number", type + "Value", "()" + getTypeDescription(type));
            }
        }

    }

    public void box(String type) {
        if (isPrimitiveType(type) && !type.equals("void")) {
            String returnString = "(" + getTypeDescription(type) + ")Ljava/lang/Object;";
            cv.visitMethodInsn(INVOKESTATIC, getClassInternalName(ScriptBytecodeAdapter.class.getName()), "box", returnString);
            // todo optimize this
        }
    }

    /**
     * Generates the bytecode to unbox the current value on the stack
     */
    public void unbox(Class type) {
        if (type.isPrimitive() && type != void.class) {
            String returnString = "(Ljava/lang/Object;)" + getTypeDescription(type.getName());
            cv.visitMethodInsn(
                INVOKESTATIC,
                getClassInternalName(ScriptBytecodeAdapter.class.getName()),
                type.getName() + "Unbox",
                returnString);
        }
    }

    /**
     * Generates the bytecode to unbox the current value on the stack
     */
    public void unbox(String type) {
        if (isPrimitiveType(type) && !type.equals("void")) {
            String returnString = "(Ljava/lang/Object;)" + getTypeDescription(type);
            cv.visitMethodInsn(INVOKESTATIC, getClassInternalName(ScriptBytecodeAdapter.class.getName()), type + "Unbox", returnString);
        }
    }

    public static boolean isPrimitiveType(String type) {
        return type != null
            && (type.equals("boolean")
                || type.equals("byte")
                || type.equals("char")
                || type.equals("short")
                || type.equals("int")
                || type.equals("long")
                || type.equals("float")
                || type.equals("double"));
    }

    /**
     * array types are special:
     * eg.: String[]: classname: [Ljava/lang/String;
     *      int[]: [I
     * @return the ASM type description
     */
    public static String getTypeDescription(String name) {
        // lets avoid class loading
        // return getType(name).getDescriptor();
        if (name == null) {
            return "Ljava/lang/Object;";
        }
        if (name.equals("void")) {
            return "V";
        }

        if (name.startsWith("[")) { // todo need to take care of multi-dimentional array
            return name.replace('.', '/');
        }

        String prefix = "";
        if (name.endsWith("[]")) {
            prefix = "[";
            name = name.substring(0, name.length() - 2);
        }

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
        return prefix + "L" + name.replace('.', '/') + ";";
    }

    /**
     * @return the ASM internal name of the type
     */
    public static String getClassInternalName(String name) {
        if (name == null) {
            return "java/lang/Object";
        }
        String answer = name.replace('.', '/');
        if (answer.endsWith("[]")) {
            return "[" + answer.substring(0, answer.length() - 2);
        }
        return answer;
    }

    /**
     * @return the regular class name of the type
     */
    public static String getClassRegularName(String name) {
        if (name == null) {
            return "java.lang.Object";
        }
        if (name.startsWith("L")) {
            name = name.substring(1);
            if (name.endsWith(";"))
                name = name.substring(0, name.length() - 1);
        }
        String answer = name.replace('/', '.');
        return answer;
    }

    /**
     * @return the ASM method type descriptor
     */
    public static String getMethodDescriptor(String returnTypeName, Parameter[] paramTypeNames) {
        // lets avoid class loading
        StringBuffer buffer = new StringBuffer("(");
        for (int i = 0; i < paramTypeNames.length; i++) {
            buffer.append(getTypeDescription(paramTypeNames[i].getType()));
        }
        buffer.append(")");
        buffer.append(getTypeDescription(returnTypeName));
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

    public static String getMethodDescriptor(Method meth) {
        return getMethodDescriptor(meth.getReturnType(), meth.getParameterTypes());
    }

    public static String getTypeDescription(Class type) {
        if (type.isArray()) {
            return type.getName().replace('.', '/');
        }
        else {
            return getTypeDescription(type.getName());
        }
    }

    /**
     * @return an array of ASM internal names of the type
     */
    public static String[] getClassInternalNames(String[] names) {
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

    public void doCast(String type) {
        if (!type.equals("java.lang.Object")) {
            if (isPrimitiveType(type) && !type.equals("void")) {
                unbox(type);
            }
            else {
                cv.visitTypeInsn(
                    CHECKCAST,
                    type.endsWith("[]") ? getTypeDescription(type) : getClassInternalName(type));
            }
        }
    }

    public void doCast(Class type) {
        String name = type.getName();
        if (type.isArray()) {
            name = type.getComponentType().getName() + "[]";
        }
        doCast(name);
    }

    public void load(String type, int idx) {
        if (type.equals("double")) {
            cv.visitVarInsn(DLOAD, idx);
        }
        else if (type.equals("float")) {
            cv.visitVarInsn(FLOAD, idx);
        }
        else if (type.equals("long")) {
            cv.visitVarInsn(LLOAD, idx);
        }
        else if (
            type.equals("boolean")
                || type.equals("char")
                || type.equals("byte")
                || type.equals("int")
                || type.equals("short")) {
            cv.visitVarInsn(ILOAD, idx);
        }
        else {
            cv.visitVarInsn(ALOAD, idx);
        }
    }

    public void load(Variable v) {
    	load(v.getTypeName(), v.getIndex());
    }

    public void store(String type, int idx) {
        if (type.equals("double")) {
            cv.visitVarInsn(DSTORE, idx);
        }
        else if (type.equals("float")) {
            cv.visitVarInsn(FSTORE, idx);
        }
        else if (type.equals("long")) {
            cv.visitVarInsn(LSTORE, idx);
        }
        else if (
            type.equals("boolean")
                || type.equals("char")
                || type.equals("byte")
                || type.equals("int")
                || type.equals("short")) {
            cv.visitVarInsn(ISTORE, idx);
        }
        else {
            cv.visitVarInsn(ASTORE, idx);
        }
    }

    public void store(Variable v, boolean markStart) {
        String type = v.getTypeName();
        int idx = v.getIndex();

        if (type.equals("double")) {
            cv.visitVarInsn(DSTORE, idx);
        }
        else if (type.equals("float")) {
            cv.visitVarInsn(FSTORE, idx);
        }
        else if (type.equals("long")) {
            cv.visitVarInsn(LSTORE, idx);
        }
        else if (
            type.equals("boolean")
                || type.equals("char")
                || type.equals("byte")
                || type.equals("int")
                || type.equals("short")) {
            cv.visitVarInsn(ISTORE, idx);
        }
        else {
            cv.visitVarInsn(ASTORE, idx);
        }
        if (AsmClassGenerator.CREATE_DEBUG_INFO && markStart) {
            Label l = v.getStartLabel();
            if (l != null) {
                cv.visitLabel(l);
            } else {
                System.out.println("start label == null! what to do about this?");
            }
        }
    }

    public void store(Variable v) {
        store(v, false);
    }


    public static String getObjectTypeForPrimitive(String type) {
        if (type.equals("boolean")) {
            return Boolean.class.getName();
        }
        else if (type.equals("byte")) {
            return Byte.class.getName();
        }
        else if (type.equals("char")) {
            return Character.class.getName();
        }
        else if (type.equals("short")) {
            return Short.class.getName();
        }
        else if (type.equals("int")) {
            return Integer.class.getName();
        }
        else if (type.equals("long")) {
            return Long.class.getName();
        }
        else if (type.equals("float")) {
            return Float.class.getName();
        }
        else if (type.equals("double")) {
            return Double.class.getName();
        }
        else {
            return type;
        }
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
        else if (value instanceof Number) {
            /** todo it would be more efficient to generate class constants */
            Number n = (Number) value;
            String className = BytecodeHelper.getClassInternalName(value.getClass().getName());
            cv.visitTypeInsn(NEW, className);
            cv.visitInsn(DUP);
            String methodType;
            if (n instanceof Double) {
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
            else if (n instanceof Integer){
            	//cv.visitLdcInsn(n);
                pushConstant(n.intValue());
            	methodType = "(I)V";
        	}
            else
            {
        		throw new ClassGeneratorException(
        				"Cannot generate bytecode for constant: " + value
        				+ " of type: " + value.getClass().getName()
        				+".  Numeric constant type not supported.");
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
     * @param holder
     */
    public void loadVar(Variable variable, boolean holder) {
		String type = variable.getTypeName();
		int index = variable.getIndex();
		if (holder) {
			cv.visitVarInsn(ALOAD, index);
			cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
		} else {
			cv.visitVarInsn(ALOAD, index); // todo? shall xload based on the type?
		}
	}
    
    public void storeVar(Variable variable, boolean holder) {
        String  type   = variable.getTypeName();
        int     index  = variable.getIndex();
        
    	if (holder) {
            //int tempIndex = visitASTOREInTemp("reference", type);
            cv.visitVarInsn(ALOAD, index);
            cv.visitInsn(SWAP);  // assuming the value on stack is single word
            //cv.visitVarInsn(ALOAD, tempIndex);
            cv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        }
        else {
            store(variable.deriveBoxedVersion()); // todo br seems right hand values on the stack are always object refs, primitives boxed
//            if (!varStored) {
//                //visitVariableStartLabel(variable);
//                varStored = true;
//            }
        }
    }
    
//    private int visitASTOREInTemp(String name, String type) {
//        Variable var  = defineVariable(createVariableName(name), type, false);
//        int varIdx = var.getIndex();
//        cv.visitVarInsn(ASTORE, varIdx);
//        if (CREATE_DEBUG_INFO) cv.visitLabel(var.getStartLabel());
//        return varIdx;
//    }

    public void putField(FieldNode fld) {
    	putField(fld, getClassInternalName(fld.getOwner()));
    }

    public void putField(FieldNode fld, String ownerName) {
    	cv.visitFieldInsn(PUTFIELD, ownerName, fld.getName(), getTypeDescription(fld.getType()));
    }

    public void loadThis() {
        cv.visitVarInsn(ALOAD, 0);
    }

    public static Class boxOnPrimitive(Class cls) {
        Class ans = cls;
        if (ans == null)
            return null;

        if (cls.isPrimitive() && cls != void.class) {
            if (cls == int.class) {
                ans = Integer.class;
            }
            else if (cls == byte.class) {
                ans = Byte.class;
            }
            else if (cls == char.class) {
                ans = Character.class;
            }
            else if (cls == short.class) {
                ans = Short.class;
            }
            else if (cls == boolean.class) {
                ans = Boolean.class;
            }
            else if (cls == float.class) {
                ans = Float.class;
            }
            else if (cls == long.class) {
                ans = Long.class;
            }
            else if (cls == double.class) {
                ans = Double.class;
            }
        }
        else if (cls.isArray()){
            // let's convert primitive array too
            int dimension = 0;
            Class elemType = null;
            do {
                ++dimension;
                elemType = cls.getComponentType();
            } while(elemType.isArray());

            if (elemType.isPrimitive()) {
                Class boxElem = null;
                if (elemType == int.class) {
                    boxElem = Integer.class;
                }
                else if (elemType == byte.class) {
                    boxElem = Byte.class;
                }
                else if (elemType == char.class) {
                    boxElem = Character.class;
                }
                else if (elemType == short.class) {
                    boxElem = Short.class;
                }
                else if (elemType == boolean.class) {
                    boxElem = Boolean.class;
                }
                else if (elemType == float.class) {
                    boxElem = Float.class;
                }
                else if (elemType == long.class) {
                    boxElem = Long.class;
                }
                else if (elemType == double.class) {
                    boxElem = Double.class;
                }
                // I need to construct a new array type for the box version
                String typeName = "";
                for (int i = 0; i < dimension; i++){
                    typeName += "[";
                }
                typeName += "L" + boxElem.getName() + ";";
                try {
                    return Class.forName(typeName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e); // should never have come here
                }
            }
        }
        return ans;
    }

    /**
     * create the bytecode to invoke a method
     * @param meth the method object to invoke
     */
    public void invoke(Method meth) {
        int op = Modifier.isStatic(meth.getModifiers()) ?
                    INVOKESTATIC :
                    (meth.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL);

        cv.visitMethodInsn(
                op,
                getClassInternalName(meth.getDeclaringClass().getName()),
                meth.getName(),
                getMethodDescriptor(meth)
                );
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

    public static String getMethodDescriptor(MetaMethod metamethod) {
        return getMethodDescriptor(metamethod.getReturnType(), metamethod.getParameterTypes());
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
     * @return
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
}
