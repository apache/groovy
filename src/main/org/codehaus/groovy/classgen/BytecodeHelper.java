/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A helper class for bytecode generation with AsmClassGenerator.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:b55r@sina.com">Bing Ran</a>
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @version $Revision$
 */
public class BytecodeHelper implements Opcodes {

    private MethodVisitor mv;

    public MethodVisitor getMethodVisitor() {
        return mv;
    }

    public BytecodeHelper(MethodVisitor mv) {
        this.mv = mv;
    }

    /**
     * box the primitive value on the stack
     *
     * @param type
     */
    public void quickBoxIfNecessary(ClassNode type) {
        String descr = getTypeDescription(type);
        if (type == ClassHelper.boolean_TYPE) {
            boxBoolean();
        } else if (ClassHelper.isPrimitiveType(type) && type != ClassHelper.VOID_TYPE) {
            ClassNode wrapper = ClassHelper.getWrapper(type);
            String internName = getClassInternalName(wrapper);
            mv.visitTypeInsn(NEW, internName);
            mv.visitInsn(DUP);
            if (type == ClassHelper.double_TYPE || type == ClassHelper.long_TYPE) {
                mv.visitInsn(DUP2_X2);
                mv.visitInsn(POP2);
            } else {
                mv.visitInsn(DUP2_X1);
                mv.visitInsn(POP2);
            }
            mv.visitMethodInsn(INVOKESPECIAL, internName, "<init>", "(" + descr + ")V");
        }
    }

    public void quickUnboxIfNecessary(ClassNode type) {
        if (ClassHelper.isPrimitiveType(type) && type != ClassHelper.VOID_TYPE) { // todo care when BigDecimal or BigIneteger on stack
            ClassNode wrapper = ClassHelper.getWrapper(type);
            String internName = getClassInternalName(wrapper);
            if (type == ClassHelper.boolean_TYPE) {
                mv.visitTypeInsn(CHECKCAST, internName);
                mv.visitMethodInsn(INVOKEVIRTUAL, internName, type.getName() + "Value", "()" + getTypeDescription(type));
            } else { // numbers
                mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(INVOKEVIRTUAL, /*internName*/"java/lang/Number", type.getName() + "Value", "()" + getTypeDescription(type));
            }
        }
    }

    /**
     * Generates the bytecode to autobox the current value on the stack
     */
    public void box(Class type) {
        if (ReflectionCache.getCachedClass(type).isPrimitive && type != void.class) {
            String returnString = "(" + getTypeDescription(type) + ")Ljava/lang/Object;";
            mv.visitMethodInsn(INVOKESTATIC, getClassInternalName(DefaultTypeTransformation.class.getName()), "box", returnString);
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
            mv.visitMethodInsn(
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

    public static String getClassInternalName(ClassNode t) {
        if (t.isPrimaryClassNode()) {
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
     * Object:   classname: java.lang.Object
     * int[] :   classname: [I
     * unlike getTypeDescription '.' is not replaced by '/'.
     * it seems that makes problems for
     * the class loading if '.' is replaced by '/'
     *
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
                    if (array) buf.append(';');
                }
                return buf.toString();
            }
        }
    }

    /**
     * array types are special:
     * eg.: String[]: classname: [Ljava/lang/String;
     * int[]: [I
     *
     * @return the ASM type description
     */
    public static String getTypeDescription(ClassNode c) {
        return getTypeDescription(c, true);
    }

    /**
     * array types are special:
     * eg.: String[]: classname: [Ljava/lang/String;
     * int[]: [I
     *
     * @return the ASM type description
     */
    private static String getTypeDescription(ClassNode c, boolean end) {
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
                } else /* long */ {
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
                if (end) buf.append(';');
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
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
    }

    public void pushConstant(int value) {
        switch (value) {
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
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    mv.visitIntInsn(BIPUSH, value);
                } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    mv.visitIntInsn(SIPUSH, value);
                } else {
                    mv.visitLdcInsn(Integer.valueOf(value));
                }
        }
    }

    public void doCast(Class type) {
        if (type != Object.class) {
            if (type.isPrimitive() && type != Void.TYPE) {
                unbox(type);
            } else {
                mv.visitTypeInsn(
                        CHECKCAST,
                        type.isArray() ? getTypeDescription(type) : getClassInternalName(type.getName()));
            }
        }
    }

    public void doCast(ClassNode type) {
        if (type == ClassHelper.OBJECT_TYPE) return;
        if (ClassHelper.isPrimitiveType(type) && type != ClassHelper.VOID_TYPE) {
            unbox(type);
        } else {
            mv.visitTypeInsn(
                    CHECKCAST,
                    type.isArray() ? getTypeDescription(type) : getClassInternalName(type));
        }
    }

    public void load(ClassNode type, int idx) {
        if (type == ClassHelper.double_TYPE) {
            mv.visitVarInsn(DLOAD, idx);
        } else if (type == ClassHelper.float_TYPE) {
            mv.visitVarInsn(FLOAD, idx);
        } else if (type == ClassHelper.long_TYPE) {
            mv.visitVarInsn(LLOAD, idx);
        } else if (
                type == ClassHelper.boolean_TYPE
                        || type == ClassHelper.char_TYPE
                        || type == ClassHelper.byte_TYPE
                        || type == ClassHelper.int_TYPE
                        || type == ClassHelper.short_TYPE) {
            mv.visitVarInsn(ILOAD, idx);
        } else {
            mv.visitVarInsn(ALOAD, idx);
        }
    }

    public void load(Variable v) {
        load(v.getType(), v.getIndex());
    }

    public void store(Variable v, boolean markStart) {
        ClassNode type = v.getType();
        unbox(type);
        int idx = v.getIndex();

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

    public void store(Variable v) {
        store(v, false);
    }

    /**
     * load the constant on the operand stack. primitives auto-boxed.
     */
    void loadConstant(Object value) {
        if (value == null) {
            mv.visitInsn(ACONST_NULL);
        } else if (value instanceof String) {
            mv.visitLdcInsn(value);
        } else if (value instanceof Character) {
            String className = "java/lang/Character";
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(value);
            String methodType = "(C)V";
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", methodType);
        } else if (value instanceof Number) {
            /** todo it would be more efficient to generate class constants */
            Number n = (Number) value;
            String className = BytecodeHelper.getClassInternalName(value.getClass().getName());
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            String methodType;
            if (n instanceof Integer) {
                //pushConstant(n.intValue());
                mv.visitLdcInsn(n);
                methodType = "(I)V";
            } else if (n instanceof Double) {
                mv.visitLdcInsn(n);
                methodType = "(D)V";
            } else if (n instanceof Float) {
                mv.visitLdcInsn(n);
                methodType = "(F)V";
            } else if (n instanceof Long) {
                mv.visitLdcInsn(n);
                methodType = "(J)V";
            } else if (n instanceof BigDecimal) {
                mv.visitLdcInsn(n.toString());
                methodType = "(Ljava/lang/String;)V";
            } else if (n instanceof BigInteger) {
                mv.visitLdcInsn(n.toString());
                methodType = "(Ljava/lang/String;)V";
            } else if (n instanceof Short) {
                mv.visitLdcInsn(n);
                methodType = "(S)V";
            } else if (n instanceof Byte) {
                mv.visitLdcInsn(n);
                methodType = "(B)V";
            } else {
                throw new ClassGeneratorException(
                        "Cannot generate bytecode for constant: " + value
                                + " of type: " + value.getClass().getName()
                                + ".  Numeric constant type not supported.");
            }
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", methodType);
        } else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            String text = (bool.booleanValue()) ? "TRUE" : "FALSE";
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", text, "Ljava/lang/Boolean;");
        } else if (value instanceof Class) {
            Class vc = (Class) value;
            if (vc.getName().equals("java.lang.Void")) {
                // load nothing here for void
            } else {
                throw new ClassGeneratorException(
                        "Cannot generate bytecode for constant: " + value + " of type: " + value.getClass().getName());
            }
        } else {
            throw new ClassGeneratorException(
                    "Cannot generate bytecode for constant: " + value + " of type: " + value.getClass().getName());
        }
    }


    /**
     * load the value of the variable on the operand stack. unbox it if it's a reference
     *
     * @param variable
     */
    public void loadVar(Variable variable) {
        int index = variable.getIndex();
        if (variable.isHolder()) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "get", "()Ljava/lang/Object;");
        } else {
            load(variable);
            if (variable != Variable.THIS_VARIABLE && variable != Variable.SUPER_VARIABLE) {
                box(variable.getType());
            }
        }
    }

    public void storeVar(Variable variable) {
        String type = variable.getTypeName();
        int index = variable.getIndex();

        if (variable.isHolder()) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Reference", "set", "(Ljava/lang/Object;)V");
        } else {
            store(variable, false);
        }
    }

    public void putField(FieldNode fld) {
        putField(fld, getClassInternalName(fld.getOwner()));
    }

    public void putField(FieldNode fld, String ownerName) {
        mv.visitFieldInsn(PUTFIELD, ownerName, fld.getName(), getTypeDescription(fld.getType()));
    }

    public void swapObjectWith(ClassNode type) {
        if (type == ClassHelper.long_TYPE || type == ClassHelper.double_TYPE) {
            mv.visitInsn(DUP_X2);
            mv.visitInsn(POP);
        } else {
            mv.visitInsn(SWAP);
        }
    }

    public void swapWithObject(ClassNode type) {
        if (type == ClassHelper.long_TYPE || type == ClassHelper.double_TYPE) {
            mv.visitInsn(DUP2_X1);
            mv.visitInsn(POP2);
        } else {
            mv.visitInsn(SWAP);
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
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l0);
        mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
        mv.visitLabel(l1);
    }

    /**
     * negate a boolean on stack. true->false, false->true
     */
    public void negateBoolean() {
        // code to negate the primitive boolean
        Label endLabel = new Label();
        Label falseLabel = new Label();
        mv.visitJumpInsn(IFNE, falseLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(endLabel);
    }

    /**
     * load a message on the stack and remove it right away. Good for put a mark in the generated bytecode for debugging purpose.
     *
     * @param msg
     */
    public void mark(String msg) {
        mv.visitLdcInsn(msg);
        mv.visitInsn(POP);
    }

    /**
     * returns a name that Class.forName() can take. Notablely for arrays:
     * [I, [Ljava.lang.String; etc
     * Regular object type:  java.lang.String
     *
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
            } else if (name.equals("long")) {
                return prefix + "J";
            } else if (name.equals("short")) {
                return prefix + "S";
            } else if (name.equals("float")) {
                return prefix + "F";
            } else if (name.equals("double")) {
                return prefix + "D";
            } else if (name.equals("byte")) {
                return prefix + "B";
            } else if (name.equals("char")) {
                return prefix + "C";
            } else if (name.equals("boolean")) {
                return prefix + "Z";
            } else {
                return prefix + "L" + name.replace('/', '.') + ";";
            }
        }
        return name.replace('/', '.');

    }

    public void dup() {
        mv.visitInsn(DUP);
    }

    public void doReturn(ClassNode returnType) {
        if (returnType == ClassHelper.double_TYPE) {
            mv.visitInsn(DRETURN);
        } else if (returnType == ClassHelper.float_TYPE) {
            mv.visitInsn(FRETURN);
        } else if (returnType == ClassHelper.long_TYPE) {
            mv.visitInsn(LRETURN);
        } else if (
                returnType == ClassHelper.boolean_TYPE
                        || returnType == ClassHelper.char_TYPE
                        || returnType == ClassHelper.byte_TYPE
                        || returnType == ClassHelper.int_TYPE
                        || returnType == ClassHelper.short_TYPE) {
            //byte,short,boolean,int are all IRETURN
            mv.visitInsn(IRETURN);
        } else if (returnType == ClassHelper.VOID_TYPE) {
            mv.visitInsn(RETURN);
        } else {
            mv.visitInsn(ARETURN);
        }

    }

    private static boolean hasGenerics(Parameter[] param) {
        if (param.length == 0) return false;
        for (int i = 0; i < param.length; i++) {
            ClassNode type = param[i].getType();
            if (type.getGenericsTypes() != null) return true;
        }
        return false;
    }

    public static String getGenericsMethodSignature(MethodNode node) {
        GenericsType[] generics = node.getGenericsTypes();
        Parameter[] param = node.getParameters();
        ClassNode returnType = node.getReturnType();

        if (generics == null && !hasGenerics(param) && returnType.getGenericsTypes() == null) return null;

        StringBuffer ret = new StringBuffer(100);
        getGenericsTypeSpec(ret, generics);

        GenericsType[] paramTypes = new GenericsType[param.length];
        for (int i = 0; i < param.length; i++) {
            ClassNode pType = param[i].getType();
            if (pType.getGenericsTypes() == null || !pType.isGenericsPlaceHolder()) {
                paramTypes[i] = new GenericsType(pType);
            } else {
                paramTypes[i] = pType.getGenericsTypes()[0];
            }
        }
        addSubTypes(ret, paramTypes, "(", ")");
        if (returnType.isGenericsPlaceHolder()) {
            addSubTypes(ret, returnType.getGenericsTypes(), "", "");
        } else {
            writeGenericsBounds(ret, new GenericsType(returnType), false);
        }
        return ret.toString();
    }

    private static boolean usesGenericsInClassSignature(ClassNode node) {
        if (!node.isUsingGenerics()) return false;
        if (node.getGenericsTypes() != null) return true;
        ClassNode sclass = node.getUnresolvedSuperClass(false);
        if (sclass.isUsingGenerics()) return true;
        ClassNode[] interfaces = node.getInterfaces();
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].isUsingGenerics()) return true;
            }
        }

        return false;
    }

    public static String getGenericsSignature(ClassNode node) {
        if (!usesGenericsInClassSignature(node)) return null;
        GenericsType[] genericsTypes = node.getGenericsTypes();
        StringBuffer ret = new StringBuffer(100);
        getGenericsTypeSpec(ret, genericsTypes);
        GenericsType extendsPart = new GenericsType(node.getUnresolvedSuperClass(false));
        writeGenericsBounds(ret, extendsPart, true);
        ClassNode[] interfaces = node.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            GenericsType interfacePart = new GenericsType(interfaces[i]);
            writeGenericsBounds(ret, interfacePart, false);
        }
        return ret.toString();
    }

    private static void getGenericsTypeSpec(StringBuffer ret, GenericsType[] genericsTypes) {
        if (genericsTypes == null) return;
        ret.append('<');
        for (int i = 0; i < genericsTypes.length; i++) {
            String name = genericsTypes[i].getName();
            ret.append(name);
            ret.append(':');
            writeGenericsBounds(ret, genericsTypes[i], true);
        }
        ret.append('>');
    }

    public static String getGenericsBounds(ClassNode type) {
        GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes == null) return null;
        StringBuffer ret = new StringBuffer(100);
        if (type.isGenericsPlaceHolder()) {
            addSubTypes(ret, type.getGenericsTypes(), "", "");
        } else {
            GenericsType gt = new GenericsType(type);
            writeGenericsBounds(ret, gt, false);
        }

        return ret.toString();
    }

    private static void writeGenericsBoundType(StringBuffer ret, ClassNode printType, boolean writeInterfaceMarker) {
        if (writeInterfaceMarker && printType.isInterface()) ret.append(":");
        ret.append(getTypeDescription(printType, false));
        addSubTypes(ret, printType.getGenericsTypes(), "<", ">");
        if (!ClassHelper.isPrimitiveType(printType)) ret.append(";");
    }

    private static void writeGenericsBounds(StringBuffer ret, GenericsType type, boolean writeInterfaceMarker) {
        if (type.getUpperBounds() != null) {
            ClassNode[] bounds = type.getUpperBounds();
            for (int i = 0; i < bounds.length; i++) {
                writeGenericsBoundType(ret, bounds[i], writeInterfaceMarker);
            }
        } else if (type.getLowerBound() != null) {
            writeGenericsBoundType(ret, type.getLowerBound(), writeInterfaceMarker);
        } else {
            writeGenericsBoundType(ret, type.getType(), writeInterfaceMarker);
        }
    }

    private static void addSubTypes(StringBuffer ret, GenericsType[] types, String start, String end) {
        if (types == null) return;
        ret.append(start);
        for (int i = 0; i < types.length; i++) {
            String name = types[i].getName();
            if (types[i].isPlaceholder()) {
                ret.append('T');
                ret.append(name);
                ret.append(';');
            } else if (types[i].isWildcard()) {
                if (types[i].getUpperBounds() != null) {
                    ret.append('+');
                    writeGenericsBounds(ret, types[i], false);
                } else if (types[i].getLowerBound() != null) {
                    ret.append('-');
                    writeGenericsBounds(ret, types[i], false);
                } else {
                    ret.append('*');
                }
            } else {
                writeGenericsBounds(ret, types[i], false);
            }
        }
        ret.append(end);
    }

}
