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
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;

import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveByte;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveChar;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveDouble;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveFloat;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveInt;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveLong;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveShort;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveVoid;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DCMPL;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.FCMPL;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;

/**
 * A helper class for bytecode generation with AsmClassGenerator.
 */
public class BytecodeHelper {

    private static final String DTT_CLASSNAME = BytecodeHelper.getClassInternalName(DefaultTypeTransformation.class);

    /**
     * @return the ASM internal name of the type
     */
    public static String getClassInternalName(ClassNode t) {
        if (t.isArray()) {
            return TypeUtil.getDescriptionByType(t);
        }
        return getClassInternalName(t.getName());
    }

    /**
     * @return the ASM internal name of the type
     */
    public static String getClassInternalName(Class t) {
        return Type.getInternalName(t);
    }

    /**
     * @return the ASM internal name of the type
     */
    public static String getClassInternalName(String name) {
        return name.replace('.', '/');
    }

    /**
     * Returns a method descriptor for the given {@link org.codehaus.groovy.ast.MethodNode}.
     *
     * @param methodNode the method node for which to create the descriptor
     * @return a method descriptor as defined in section JVMS section 4.3.3
     */
    public static String getMethodDescriptor(MethodNode methodNode) {
        return getMethodDescriptor(methodNode.getReturnType(), methodNode.getParameters());
    }

    /**
     * @return the ASM method type descriptor
     */
    public static String getMethodDescriptor(ClassNode returnType, Parameter[] parameters) {
        ClassNode[] parameterTypes = new ClassNode[parameters.length];
        for (int i = 0, n = parameters.length; i < n; i += 1) {
            parameterTypes[i] = parameters[i].getType();
        }
        return getMethodDescriptor(returnType, parameterTypes);
    }

    /**
     * @return the ASM method type descriptor
     */
    public static String getMethodDescriptor(ClassNode returnType, ClassNode[] parameterTypes) {
        StringBuilder buffer = new StringBuilder(100);
        buffer.append('(');
        for (ClassNode parameterType : parameterTypes) {
            buffer.append(getTypeDescription(parameterType));
        }
        buffer.append(')');
        buffer.append(getTypeDescription(returnType));
        return buffer.toString();
    }

    /**
     * @return the ASM method type descriptor
     */
    public static String getMethodDescriptor(Class returnType, Class[] paramTypes) {
        // lets avoid class loading
        StringBuilder buffer = new StringBuilder(100);
        buffer.append('(');
        for (Class paramType : paramTypes) {
            buffer.append(getTypeDescription(paramType));
        }
        buffer.append(')');
        buffer.append(getTypeDescription(returnType));
        return buffer.toString();
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
        String desc = TypeUtil.getDescriptionByType(c);
        if (!c.isArray()) {
            if (desc.startsWith("L") && desc.endsWith(";")) {
                desc = desc.substring(1, desc.length() - 1); // remove "L" and ";"
            }
        }
        return desc.replace('/', '.');
    }

    public static String getTypeDescription(Class c) {
        return Type.getDescriptor(c);
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
        ClassNode d = c;
        if (isPrimitiveType(d.redirect())) {
            d = d.redirect();
        }
        String desc = TypeUtil.getDescriptionByType(d);
        if (!end && desc.endsWith(";")) {
            desc = desc.substring(0, desc.length() - 1);
        }
        return desc;
    }

    /**
     * @return an array of ASM internal names of the type
     */
    public static String[] getClassInternalNames(ClassNode[] names) {
        int size = names.length;
        String[] answer = new String[size];
        for (int i = 0; i < size; i += 1) {
            answer[i] = getClassInternalName(names[i]);
        }
        return answer;
    }

    public static void pushConstant(MethodVisitor mv, int value) {
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
                mv.visitLdcInsn(value);
            }
        }
    }

    /**
     * Negates a boolean on stack.
     */
    public static void negateBoolean(final MethodVisitor mv) {
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IXOR);
    }

    /**
     * returns a name that Class.forName() can take. Notably for arrays:
     * [I, [Ljava.lang.String; etc
     * Regular object type:  java.lang.String
     */
    public static String formatNameForClassLoading(String name) {
        if (name == null) {
            return "java.lang.Object;";
        }

        if (TypeUtil.isPrimitiveType(name)) {
            return name;
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

            return prefix + TypeUtil.getDescriptionByName(name);
        }

        return name.replace('/', '.');
    }

    private static boolean hasGenerics(Parameter[] param) {
        if (param.length == 0) return false;
        for (Parameter parameter : param) {
            ClassNode type = parameter.getType();
            if (hasGenerics(type)) return true;
        }
        return false;
    }

    private static boolean hasGenerics(ClassNode type) {
        return type.isArray() ? hasGenerics(type.getComponentType()) : type.getGenericsTypes() != null;
    }

    public static String getGenericsMethodSignature(MethodNode node) {
        GenericsType[] generics = node.getGenericsTypes();
        Parameter[] param = node.getParameters();
        ClassNode returnType = node.getReturnType();

        if (generics == null && !hasGenerics(param) && !hasGenerics(returnType)) return null;

        StringBuilder ret = new StringBuilder(100);
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
        addSubTypes(ret, new GenericsType[]{new GenericsType(returnType)}, "", "");
        return ret.toString();
    }

    private static boolean usesGenericsInClassSignature(ClassNode node) {
        if (!node.isUsingGenerics()) return false;
        if (hasGenerics(node)) return true;
        ClassNode sclass = node.getUnresolvedSuperClass(false);
        if (sclass.isUsingGenerics()) return true;
        ClassNode[] interfaces = node.getInterfaces();
        if (interfaces != null) {
            for (ClassNode anInterface : interfaces) {
                if (anInterface.isUsingGenerics()) return true;
            }
        }
        return false;
    }

    public static String getTypeGenericsSignature(ClassNode node) {
        if (!usesGenericsInTypeSignature(node)) return null;
        StringBuilder ret = new StringBuilder(100);
        ret.append(getTypeDescription(node.getPlainNodeReference(), false));
        addSubTypes(ret, node.getGenericsTypes(), "<", ">");
        ret.append(";");

        return ret.toString();
    }

    private static boolean usesGenericsInTypeSignature(ClassNode node) {
        if (!node.isUsingGenerics()) return false;
        if (hasGenerics(node)) return true;

        return false;
    }

    public static String getGenericsSignature(ClassNode node) {
        if (!usesGenericsInClassSignature(node)) return null;
        GenericsType[] genericsTypes = node.getGenericsTypes();
        StringBuilder ret = new StringBuilder(100);
        getGenericsTypeSpec(ret, genericsTypes);
        GenericsType extendsPart = new GenericsType(node.getUnresolvedSuperClass(false));
        writeGenericsBounds(ret, extendsPart, true);
        ClassNode[] interfaces = node.getInterfaces();
        for (ClassNode anInterface : interfaces) {
            GenericsType interfacePart = new GenericsType(anInterface);
            writeGenericsBounds(ret, interfacePart, false);
        }
        return ret.toString();
    }

    private static void getGenericsTypeSpec(StringBuilder ret, GenericsType[] genericsTypes) {
        if (genericsTypes == null) return;
        ret.append('<');
        for (GenericsType genericsType : genericsTypes) {
            String name = genericsType.getName();
            ret.append(name);
            ret.append(':');
            writeGenericsBounds(ret, genericsType, true);
        }
        ret.append('>');
    }

    public static String getGenericsBounds(ClassNode type) {
        GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes == null) return null;
        StringBuilder ret = new StringBuilder(100);
        if (type.isGenericsPlaceHolder()) {
            addSubTypes(ret, type.getGenericsTypes(), "", "");
        } else {
            GenericsType gt = new GenericsType(type);
            writeGenericsBounds(ret, gt, false);
        }
        return ret.toString();
    }

    private static void writeGenericsBoundType(StringBuilder ret, ClassNode printType, boolean writeInterfaceMarker) {
        if (writeInterfaceMarker && printType.isInterface()) ret.append(":");
        if (printType.isGenericsPlaceHolder() && printType.getGenericsTypes() != null) {
            ret.append("T");
            ret.append(printType.getGenericsTypes()[0].getName());
            ret.append(";");
        } else {
            ret.append(getTypeDescription(printType, false));
            addSubTypes(ret, printType.getGenericsTypes(), "<", ">");
            if (!isPrimitiveType(printType)) ret.append(";");
        }
    }

    private static void writeGenericsBounds(StringBuilder ret, GenericsType type, boolean writeInterfaceMarker) {
        if (type.getUpperBounds() != null) {
            ClassNode[] bounds = type.getUpperBounds();
            for (ClassNode bound : bounds) {
                writeGenericsBoundType(ret, bound, writeInterfaceMarker);
            }
        } else if (type.getLowerBound() != null) {
            writeGenericsBoundType(ret, type.getLowerBound(), writeInterfaceMarker);
        } else {
            writeGenericsBoundType(ret, type.getType(), writeInterfaceMarker);
        }
    }

    private static void addSubTypes(StringBuilder ret, GenericsType[] types, String start, String end) {
        if (types == null) return;
        ret.append(start);
        for (GenericsType type : types) {
            if (type.getType().isArray()) {
                ret.append("[");
                addSubTypes(ret, new GenericsType[]{new GenericsType(type.getType().getComponentType())}, "", "");
            } else {
                if (type.isPlaceholder()) {
                    ret.append('T');
                    String name = type.getName();
                    ret.append(name);
                    ret.append(';');
                } else if (type.isWildcard()) {
                    if (type.getUpperBounds() != null) {
                        ret.append('+');
                        writeGenericsBounds(ret, type, false);
                    } else if (type.getLowerBound() != null) {
                        ret.append('-');
                        writeGenericsBounds(ret, type, false);
                    } else {
                        ret.append('*');
                    }
                } else {
                    writeGenericsBounds(ret, type, false);
                }
            }
        }
        ret.append(end);
    }

    public static void doCast(MethodVisitor mv, ClassNode type) {
        if (isObjectType(type)) return;
        if (isPrimitiveType(type) && !isPrimitiveVoid(type)) {
            unbox(mv, type);
        } else {
            mv.visitTypeInsn(
                    CHECKCAST,
                    type.isArray() ?
                            BytecodeHelper.getTypeDescription(type) :
                            BytecodeHelper.getClassInternalName(type.getName()));
        }
    }

    /**
     * Given a wrapped number type (Byte, Integer, Short, ...), generates bytecode
     * to convert it to a primitive number (int, long, double) using calls to
     * wrapped.[targetType]Value()
     *
     * @param mv         method visitor
     * @param sourceType the wrapped number type
     * @param targetType the primitive target type
     */
    public static void doCastToPrimitive(MethodVisitor mv, ClassNode sourceType, ClassNode targetType) {
        mv.visitMethodInsn(INVOKEVIRTUAL, BytecodeHelper.getClassInternalName(sourceType), targetType.getName() + "Value", "()" + BytecodeHelper.getTypeDescription(targetType), false);
    }

    /**
     * Given a primitive number type (byte, integer, short, ...), generates bytecode
     * to convert it to a wrapped number (Integer, Long, Double) using calls to
     * [WrappedType].valueOf
     *
     * @param mv         method visitor
     * @param sourceType the primitive number type
     * @param targetType the wrapped target type
     */
    public static void doCastToWrappedType(MethodVisitor mv, ClassNode sourceType, ClassNode targetType) {
        mv.visitMethodInsn(INVOKESTATIC, getClassInternalName(targetType), "valueOf", "(" + getTypeDescription(sourceType) + ")" + getTypeDescription(targetType), false);
    }

    public static void doCast(MethodVisitor mv, Class type) {
        if (type == Object.class) return;
        if (type.isPrimitive() && type != Void.TYPE) {
            unbox(mv, type);
        } else {
            mv.visitTypeInsn(
                    CHECKCAST,
                    type.isArray() ?
                            BytecodeHelper.getTypeDescription(type) :
                            BytecodeHelper.getClassInternalName(type.getName()));
        }
    }

    /**
     * Generates the bytecode to autobox the current value on the stack.
     */
    @Deprecated
    public static boolean box(MethodVisitor mv, ClassNode type) {
        if (isPrimitiveType(type) && !isPrimitiveVoid(type)) {
            box(mv, BytecodeHelper.getTypeDescription(type));
            return true;
        }
        return false;
    }

    /**
     * Generates the bytecode to autobox the current value on the stack.
     */
    @Deprecated
    public static boolean box(MethodVisitor mv, Class type) {
        if (ReflectionCache.getCachedClass(type).isPrimitive && type != void.class) {
            box(mv, BytecodeHelper.getTypeDescription(type));
            return true;
        }
        return false;
    }

    private static void box(MethodVisitor mv, String typeDescription) {
        mv.visitMethodInsn(INVOKESTATIC, DTT_CLASSNAME, "box", "(" + typeDescription + ")Ljava/lang/Object;", false);
    }

    /**
     * Generates the bytecode to unbox the current value on the stack.
     */
    public static void unbox(MethodVisitor mv, ClassNode type) {
        if (isPrimitiveType(type) && !isPrimitiveVoid(type)) {
            unbox(mv, type.getName(), BytecodeHelper.getTypeDescription(type));
        }
    }

    /**
     * Generates the bytecode to unbox the current value on the stack.
     */
    public static void unbox(MethodVisitor mv, Class type) {
        if (type.isPrimitive() && type != Void.TYPE) {
            unbox(mv, type.getName(), BytecodeHelper.getTypeDescription(type));
        }
    }

    private static void unbox(MethodVisitor mv, String typeName, String typeDescription) {
        mv.visitMethodInsn(INVOKESTATIC, DTT_CLASSNAME, typeName + "Unbox", "(Ljava/lang/Object;)" + typeDescription, false);
    }

    /**
     * Visits a class literal. If the type of the classnode is a primitive type,
     * the generated bytecode will be a GETSTATIC Integer.TYPE.
     * If the classnode is not a primitive type, we will generate a LDC instruction.
     */
    public static void visitClassLiteral(MethodVisitor mv, ClassNode classNode) {
        if (isPrimitiveType(classNode)) {
            mv.visitFieldInsn(
                    GETSTATIC,
                    getClassInternalName(ClassHelper.getWrapper(classNode)),
                    "TYPE",
                    "Ljava/lang/Class;");
        } else {
            mv.visitLdcInsn(Type.getType(getTypeDescription(classNode)));
        }
    }

    /**
     * Tells if a class node is candidate for class literal bytecode optimization. If so,
     * bytecode may use LDC instructions instead of static constant Class fields to retrieve
     * class literals.
     *
     * @param classNode the classnode for which we want to know if bytecode optimization is possible
     * @return true if the bytecode can be optimized
     */
    public static boolean isClassLiteralPossible(ClassNode classNode) {
        // the current implementation only checks for public modifier, because Groovy used to allow
        // handles on classes even if they are package protected and not in the same package.
        // There are situations where we could make more fine-grained checks, but be careful of
        // potential breakage of existing code.
        return Modifier.isPublic(classNode.getModifiers());
    }

    /**
     * Returns true if the two classes share the same compilation unit.
     *
     * @param a class a
     * @param b class b
     * @return true if both classes share the same compilation unit
     */
    public static boolean isSameCompilationUnit(ClassNode a, ClassNode b) {
        CompileUnit cu1 = a.getCompileUnit();
        CompileUnit cu2 = b.getCompileUnit();
        return cu1 != null && cu1 == cu2;
    }

    /**
     * Computes a hash code for a string. The purpose of this hashcode is to be constant independently of
     * the JDK being used.
     *
     * @param str the string for which to compute the hashcode
     * @return hashcode of the string
     */
    public static int hashCode(String str) {
        final char[] chars = str.toCharArray();
        int h = 0;
        for (char aChar : chars) {
            h = 31 * h + aChar;
        }
        return h;
    }

    /**
     * Converts a primitive type to boolean.
     *
     * @param mv   method visitor
     * @param type primitive type to convert
     */
    public static void convertPrimitiveToBoolean(MethodVisitor mv, ClassNode type) {
        if (isPrimitiveBoolean(type)) {
            return;
        }
        // Special handling is done for floating point types in order to
        // handle checking for 0 or NaN values.
        if (isPrimitiveDouble(type)) {
            convertDoubleToBoolean(mv);
            return;
        } else if (isPrimitiveFloat(type)) {
            convertFloatToBoolean(mv);
            return;
        }
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        // Convert long to int for IFEQ comparison using LCMP
        if (isPrimitiveLong(type)) {
            mv.visitInsn(LCONST_0);
            mv.visitInsn(LCMP);
        }
        // This handles byte, short, char and int
        mv.visitJumpInsn(IFEQ, falseLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, trueLabel);
        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(trueLabel);
    }

    private static void convertDoubleToBoolean(MethodVisitor mv) {
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        Label falseLabelWithPop = new Label();
        mv.visitInsn(DUP2); // will need the extra for isNaN call if required
        mv.visitInsn(DCONST_0);
        mv.visitInsn(DCMPL);
        mv.visitJumpInsn(IFEQ, falseLabelWithPop);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "isNaN", "(D)Z", false);
        mv.visitJumpInsn(IFNE, falseLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, trueLabel);
        mv.visitLabel(falseLabelWithPop);
        mv.visitInsn(POP2);
        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(trueLabel);
    }

    private static void convertFloatToBoolean(MethodVisitor mv) {
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        Label falseLabelWithPop = new Label();
        mv.visitInsn(DUP); // will need the extra for isNaN call if required
        mv.visitInsn(FCONST_0);
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(IFEQ, falseLabelWithPop);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "isNaN", "(F)Z", false);
        mv.visitJumpInsn(IFNE, falseLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, trueLabel);
        mv.visitLabel(falseLabelWithPop);
        mv.visitInsn(POP);
        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(trueLabel);
    }

    public static void doReturn(MethodVisitor mv, ClassNode type) {
        new ReturnVarHandler(mv, type).handle();
    }

    public static void load(MethodVisitor mv, ClassNode type, int idx) {
        new LoadVarHandler(mv, type, idx).handle();
    }

    public static void store(MethodVisitor mv, ClassNode type, int idx) {
        new StoreVarHandler(mv, type, idx).handle();
    }

    private static class ReturnVarHandler extends PrimitiveTypeHandler {
        private MethodVisitor mv;

        ReturnVarHandler(MethodVisitor mv, ClassNode type) {
            super(type);
            this.mv = mv;
        }

        @Override
        protected void handleDoubleType() {
            mv.visitInsn(DRETURN);
        }

        @Override
        protected void handleFloatType() {
            mv.visitInsn(FRETURN);
        }

        @Override
        protected void handleLongType() {
            mv.visitInsn(LRETURN);
        }

        @Override
        protected void handleIntType() {
            mv.visitInsn(IRETURN);
        }

        @Override
        protected void handleVoidType() {
            mv.visitInsn(RETURN);
        }

        @Override
        protected void handleRefType() {
            mv.visitInsn(ARETURN);
        }
    }

    private static class LoadVarHandler extends PrimitiveTypeHandler {
        private MethodVisitor mv;
        private int idx;

        LoadVarHandler(MethodVisitor mv, ClassNode type, int idx) {
            super(type);
            this.mv = mv;
            this.idx = idx;
        }

        @Override
        protected void handleDoubleType() {
            mv.visitVarInsn(DLOAD, idx);
        }

        @Override
        protected void handleFloatType() {
            mv.visitVarInsn(FLOAD, idx);
        }

        @Override
        protected void handleLongType() {
            mv.visitVarInsn(LLOAD, idx);
        }

        @Override
        protected void handleIntType() {
            mv.visitVarInsn(ILOAD, idx);
        }

        @Override
        protected void handleVoidType() {
            // do nothing
        }

        @Override
        protected void handleRefType() {
            mv.visitVarInsn(ALOAD, idx);
        }
    }

    private static class StoreVarHandler extends PrimitiveTypeHandler {
        private MethodVisitor mv;
        private int idx;

        StoreVarHandler(MethodVisitor mv, ClassNode type, int idx) {
            super(type);
            this.mv = mv;
            this.idx = idx;
        }

        @Override
        protected void handleDoubleType() {
            mv.visitVarInsn(DSTORE, idx);
        }

        @Override
        protected void handleFloatType() {
            mv.visitVarInsn(FSTORE, idx);
        }

        @Override
        protected void handleLongType() {
            mv.visitVarInsn(LSTORE, idx);
        }

        @Override
        protected void handleIntType() {
            mv.visitVarInsn(ISTORE, idx);
        }

        @Override
        protected void handleVoidType() {
            // do nothing
        }

        @Override
        protected void handleRefType() {
            mv.visitVarInsn(ASTORE, idx);
        }
    }

    private static abstract class PrimitiveTypeHandler {
        private ClassNode type;

        PrimitiveTypeHandler(ClassNode type) {
            this.type = type;
        }

        public void handle() {
            if (isPrimitiveDouble(type)) {
                handleDoubleType();
            } else if (isPrimitiveFloat(type)) {
                handleFloatType();
            } else if (isPrimitiveLong(type)) {
                handleLongType();
            } else if (
                    isPrimitiveBoolean(type)
                            || isPrimitiveChar(type)
                            || isPrimitiveByte(type)
                            || isPrimitiveInt(type)
                            || isPrimitiveShort(type)) {
                handleIntType();
            } else if (isPrimitiveVoid(type)) {
                handleVoidType();
            } else {
                handleRefType();
            }
        }

        protected abstract void handleDoubleType();

        protected abstract void handleFloatType();

        protected abstract void handleLongType();

        /**
         * boolean, char, byte, int, short types are handle in the same way
         */
        protected abstract void handleIntType();

        protected abstract void handleVoidType();

        protected abstract void handleRefType();
    }
}
