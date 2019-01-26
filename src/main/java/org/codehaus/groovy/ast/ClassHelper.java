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
package org.codehaus.groovy.ast;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MetaClass;
import groovy.lang.Range;
import groovy.lang.Reference;
import groovy.lang.Script;
import groovy.lang.Tuple;
import groovy.lang.Tuple0;
import groovy.lang.Tuple1;
import groovy.lang.Tuple10;
import groovy.lang.Tuple11;
import groovy.lang.Tuple12;
import groovy.lang.Tuple13;
import groovy.lang.Tuple14;
import groovy.lang.Tuple15;
import groovy.lang.Tuple16;
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import groovy.lang.Tuple4;
import groovy.lang.Tuple5;
import groovy.lang.Tuple6;
import groovy.lang.Tuple7;
import groovy.lang.Tuple8;
import groovy.lang.Tuple9;
import org.apache.groovy.util.Maps;
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GeneratedLambda;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.groovy.util.ManagedConcurrentMap;
import org.codehaus.groovy.util.ReferenceBundle;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.ref.SoftReference;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class is a Helper for ClassNode and classes handling ClassNodes.
 * It does contain a set of predefined ClassNodes for the most used
 * types and some code for cached ClassNode creation and basic
 * ClassNode handling
 */
public class ClassHelper {

    private static final Class[] classes = new Class[]{
            Object.class, Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE,
            Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE, Void.TYPE,
            Closure.class, GString.class, List.class, Map.class, Range.class,
            Pattern.class, Script.class, String.class, Boolean.class,
            Character.class, Byte.class, Short.class, Integer.class, Long.class,
            Double.class, Float.class, BigDecimal.class, BigInteger.class,
            Number.class, Void.class, Reference.class, Class.class, MetaClass.class,
            Iterator.class, GeneratedClosure.class, GeneratedLambda.class, GroovyObjectSupport.class
    };

    public static final Class[] TUPLE_CLASSES = new Class[] {
            Tuple0.class, Tuple1.class, Tuple2.class, Tuple3.class, Tuple4.class, Tuple5.class, Tuple6.class,
            Tuple7.class, Tuple8.class, Tuple9.class, Tuple10.class, Tuple11.class, Tuple12.class, Tuple13.class,
            Tuple14.class, Tuple15.class, Tuple16.class
    };

    private static final String[] primitiveClassNames = new String[]{
            "", "boolean", "char", "byte", "short",
            "int", "long", "double", "float", "void"
    };

    public static final ClassNode
            DYNAMIC_TYPE = makeCached(Object.class), OBJECT_TYPE = DYNAMIC_TYPE,
            VOID_TYPE = makeCached(Void.TYPE),
            CLOSURE_TYPE = makeCached(Closure.class),
            GSTRING_TYPE = makeCached(GString.class), LIST_TYPE = makeWithoutCaching(List.class),
            TUPLE_TYPE = makeWithoutCaching(Tuple.class),
            MAP_TYPE = makeWithoutCaching(Map.class), RANGE_TYPE = makeCached(Range.class),
            PATTERN_TYPE = makeCached(Pattern.class), STRING_TYPE = makeCached(String.class),
            SCRIPT_TYPE = makeCached(Script.class), REFERENCE_TYPE = makeWithoutCaching(Reference.class),
            BINDING_TYPE = makeCached(Binding.class),

    boolean_TYPE = makeCached(boolean.class), char_TYPE = makeCached(char.class),
            byte_TYPE = makeCached(byte.class), int_TYPE = makeCached(int.class),
            long_TYPE = makeCached(long.class), short_TYPE = makeCached(short.class),
            double_TYPE = makeCached(double.class), float_TYPE = makeCached(float.class),
            Byte_TYPE = makeCached(Byte.class), Short_TYPE = makeCached(Short.class),
            Integer_TYPE = makeCached(Integer.class), Long_TYPE = makeCached(Long.class),
            Character_TYPE = makeCached(Character.class), Float_TYPE = makeCached(Float.class),
            Double_TYPE = makeCached(Double.class), Boolean_TYPE = makeCached(Boolean.class),
            BigInteger_TYPE = makeCached(java.math.BigInteger.class),
            BigDecimal_TYPE = makeCached(java.math.BigDecimal.class),
            Number_TYPE = makeCached(Number.class),

    void_WRAPPER_TYPE = makeCached(Void.class), METACLASS_TYPE = makeCached(MetaClass.class),
            Iterator_TYPE = makeCached(Iterator.class),

    Enum_Type = makeWithoutCaching(Enum.class),
            Annotation_TYPE = makeCached(Annotation.class),
            ELEMENT_TYPE_TYPE = makeCached(ElementType.class),

//    FunctionalInterface_Type = ClassHelper.makeCached(FunctionalInterface.class),

    // uncached constants.
    CLASS_Type = makeWithoutCaching(Class.class), COMPARABLE_TYPE = makeWithoutCaching(Comparable.class),
            GENERATED_CLOSURE_Type = makeWithoutCaching(GeneratedClosure.class),
            GENERATED_LAMBDA_TYPE = makeWithoutCaching(GeneratedLambda.class),
            GROOVY_OBJECT_SUPPORT_TYPE = makeWithoutCaching(GroovyObjectSupport.class),
            GROOVY_OBJECT_TYPE = makeWithoutCaching(GroovyObject.class),
            GROOVY_INTERCEPTABLE_TYPE = makeWithoutCaching(GroovyInterceptable.class);

    private static final ClassNode[] types = new ClassNode[]{
            OBJECT_TYPE,
            boolean_TYPE, char_TYPE, byte_TYPE, short_TYPE,
            int_TYPE, long_TYPE, double_TYPE, float_TYPE,
            VOID_TYPE, CLOSURE_TYPE, GSTRING_TYPE,
            LIST_TYPE, MAP_TYPE, RANGE_TYPE, PATTERN_TYPE,
            SCRIPT_TYPE, STRING_TYPE, Boolean_TYPE, Character_TYPE,
            Byte_TYPE, Short_TYPE, Integer_TYPE, Long_TYPE,
            Double_TYPE, Float_TYPE, BigDecimal_TYPE, BigInteger_TYPE,
            Number_TYPE,
            void_WRAPPER_TYPE, REFERENCE_TYPE, CLASS_Type, METACLASS_TYPE,
            Iterator_TYPE, GENERATED_CLOSURE_Type, GENERATED_LAMBDA_TYPE, GROOVY_OBJECT_SUPPORT_TYPE,
            GROOVY_OBJECT_TYPE, GROOVY_INTERCEPTABLE_TYPE, Enum_Type, Annotation_TYPE
    };

    private static final int ABSTRACT_STATIC_PRIVATE =
            Modifier.ABSTRACT | Modifier.PRIVATE | Modifier.STATIC;
    private static final int VISIBILITY = 5; // public|protected

    protected static final ClassNode[] EMPTY_TYPE_ARRAY = {};

    public static final String OBJECT = "java.lang.Object";

    public static ClassNode makeCached(Class c) {
        final SoftReference<ClassNode> classNodeSoftReference = ClassHelperCache.classCache.get(c);
        ClassNode classNode;
        if (classNodeSoftReference == null || (classNode = classNodeSoftReference.get()) == null) {
            classNode = new ClassNode(c);
            ClassHelperCache.classCache.put(c, new SoftReference<>(classNode));

            VMPluginFactory.getPlugin().setAdditionalClassInformation(classNode);
        }

        return classNode;
    }

    /**
     * Creates an array of ClassNodes using an array of classes.
     * For each of the given classes a new ClassNode will be
     * created
     *
     * @param classes an array of classes used to create the ClassNodes
     * @return an array of ClassNodes
     * @see #make(Class)
     */
    public static ClassNode[] make(Class[] classes) {
        ClassNode[] cns = new ClassNode[classes.length];
        for (int i = 0; i < cns.length; i++) {
            cns[i] = make(classes[i]);
        }

        return cns;
    }

    /**
     * Creates a ClassNode using a given class.
     * A new ClassNode object is only created if the class
     * is not one of the predefined ones
     *
     * @param c class used to created the ClassNode
     * @return ClassNode instance created from the given class
     */
    public static ClassNode make(Class c) {
        return make(c, true);
    }

    public static ClassNode make(Class c, boolean includeGenerics) {
        for (int i = 0; i < classes.length; i++) {
            if (c == classes[i]) return types[i];
        }
        if (c.isArray()) {
            ClassNode cn = make(c.getComponentType(), includeGenerics);
            return cn.makeArray();
        }
        return makeWithoutCaching(c, includeGenerics);
    }

    public static ClassNode makeWithoutCaching(Class c) {
        return makeWithoutCaching(c, true);
    }

    public static ClassNode makeWithoutCaching(Class c, boolean includeGenerics) {
        if (c.isArray()) {
            ClassNode cn = makeWithoutCaching(c.getComponentType(), includeGenerics);
            return cn.makeArray();
        }

        final ClassNode cached = makeCached(c);
        if (includeGenerics) {
            return cached;
        } else {
            ClassNode t = makeWithoutCaching(c.getName());
            t.setRedirect(cached);
            return t;
        }
    }


    /**
     * Creates a ClassNode using a given class.
     * Unlike make(String) this method will not use the cache
     * to create the ClassNode. This means the ClassNode created
     * from this method using the same name will have a different
     * reference
     *
     * @param name of the class the ClassNode is representing
     * @see #make(String)
     */
    public static ClassNode makeWithoutCaching(String name) {
        ClassNode cn = new ClassNode(name, Opcodes.ACC_PUBLIC, OBJECT_TYPE);
        cn.isPrimaryNode = false;
        return cn;
    }

    /**
     * Creates a ClassNode using a given class.
     * If the name is one of the predefined ClassNodes then the
     * corresponding ClassNode instance will be returned. If the
     * name is null or of length 0 the dynamic type is returned
     *
     * @param name of the class the ClassNode is representing
     */
    public static ClassNode make(String name) {
        if (name == null || name.length() == 0) return DYNAMIC_TYPE;

        for (int i = 0; i < primitiveClassNames.length; i++) {
            if (primitiveClassNames[i].equals(name)) return types[i];
        }

        for (int i = 0; i < classes.length; i++) {
            String cname = classes[i].getName();
            if (name.equals(cname)) return types[i];
        }
        return makeWithoutCaching(name);
    }

    private static final Map<ClassNode, ClassNode> PRIMITIVE_TYPE_TO_WRAPPER_TYPE_MAP = Maps.of(
            boolean_TYPE, Boolean_TYPE,
            byte_TYPE, Byte_TYPE,
            char_TYPE, Character_TYPE,
            short_TYPE, Short_TYPE,
            int_TYPE, Integer_TYPE,
            long_TYPE, Long_TYPE,
            float_TYPE, Float_TYPE,
            double_TYPE, Double_TYPE,
            VOID_TYPE, void_WRAPPER_TYPE
    );

    /**
     * Creates a ClassNode containing the wrapper of a ClassNode
     * of primitive type. Any ClassNode representing a primitive
     * type should be created using the predefined types used in
     * class. The method will check the parameter for known
     * references of ClassNode representing a primitive type. If
     * Reference is found, then a ClassNode will be contained that
     * represents the wrapper class. For example for boolean, the
     * wrapper class is java.lang.Boolean.
     * <p>
     * If the parameter is no primitive type, the redirected
     * ClassNode will be returned
     *
     * @param cn the ClassNode containing a possible primitive type
     * @see #make(Class)
     * @see #make(String)
     */
    public static ClassNode getWrapper(ClassNode cn) {
        cn = cn.redirect();
        if (!isPrimitiveType(cn)) return cn;

        ClassNode result = PRIMITIVE_TYPE_TO_WRAPPER_TYPE_MAP.get(cn);

        if (null != result) {
            return result;
        }

        return cn;
    }

    private static final Map<ClassNode, ClassNode> WRAPPER_TYPE_TO_PRIMITIVE_TYPE_MAP = Maps.inverse(PRIMITIVE_TYPE_TO_WRAPPER_TYPE_MAP);

    public static ClassNode getUnwrapper(ClassNode cn) {
        cn = cn.redirect();
        if (isPrimitiveType(cn)) return cn;

        ClassNode result = WRAPPER_TYPE_TO_PRIMITIVE_TYPE_MAP.get(cn);

        if (null != result) {
            return result;
        }

        return cn;
    }


    /**
     * Test to determine if a ClassNode is a primitive type.
     * Note: this only works for ClassNodes created using a
     * predefined ClassNode
     *
     * @param cn the ClassNode containing a possible primitive type
     * @return true if the ClassNode is a primitive type
     * @see #make(Class)
     * @see #make(String)
     */
    public static boolean isPrimitiveType(ClassNode cn) {
        return TypeUtil.isPrimitiveType(cn);
    }

    /**
     * Test to determine if a ClassNode is a type belongs to the list of types which
     * are allowed to initialize constants directly in bytecode instead of using &lt;cinit&gt;
     * <p>
     * Note: this only works for ClassNodes created using a
     * predefined ClassNode
     *
     * @param cn the ClassNode to be tested
     * @return true if the ClassNode is of int, float, long, double or String type
     * @see #make(Class)
     * @see #make(String)
     */
    public static boolean isStaticConstantInitializerType(ClassNode cn) {
        return cn == int_TYPE ||
                cn == float_TYPE ||
                cn == long_TYPE ||
                cn == double_TYPE ||
                cn == STRING_TYPE ||
                // the next items require conversion to int when initializing
                cn == byte_TYPE ||
                cn == char_TYPE ||
                cn == short_TYPE;
    }

    public static boolean isNumberType(ClassNode cn) {
        return cn == Byte_TYPE ||
                cn == Short_TYPE ||
                cn == Integer_TYPE ||
                cn == Long_TYPE ||
                cn == Float_TYPE ||
                cn == Double_TYPE ||
                cn == byte_TYPE ||
                cn == short_TYPE ||
                cn == int_TYPE ||
                cn == long_TYPE ||
                cn == float_TYPE ||
                cn == double_TYPE;
    }

    public static ClassNode makeReference() {
        return REFERENCE_TYPE.getPlainNodeReference();
    }

    public static boolean isCachedType(ClassNode type) {
        for (ClassNode cachedType : types) {
            if (cachedType == type) return true;
        }
        return false;
    }

    static class ClassHelperCache {
        static ManagedConcurrentMap<Class, SoftReference<ClassNode>> classCache = new ManagedConcurrentMap<>(ReferenceBundle.getWeakBundle());
    }

    public static boolean isSAMType(ClassNode type) {
        return findSAM(type) != null;
    }

    public static boolean isFunctionalInterface(ClassNode type) {
        // Functional interface must be an interface at first, or the following exception will occur:
        // java.lang.invoke.LambdaConversionException: Functional interface SamCallable is not an interface
        return type.isInterface() && isSAMType(type);
    }

    /**
     * Returns the single abstract method of a class node, if it is a SAM type, or null otherwise.
     *
     * @param type a type for which to search for a single abstract method
     * @return the method node if type is a SAM type, null otherwise
     */
    public static MethodNode findSAM(ClassNode type) {
        if (!Modifier.isAbstract(type.getModifiers())) return null;
        if (type.isInterface()) {
            List<MethodNode> methods;
            if (type.isInterface()) {
                // e.g. BinaryOperator extends BiFunction, BinaryOperator contains no abstract method, but it is really a SAM
                methods = type.redirect().getAllDeclaredMethods();
            } else {
                methods = type.getMethods();
            }

            MethodNode found = null;
            for (MethodNode mi : methods) {
                // ignore methods, that are not abstract and from Object
                if (!Modifier.isAbstract(mi.getModifiers())) continue;
                // ignore trait methods which have a default implementation
                if (Traits.hasDefaultImplementation(mi)) continue;
                if (mi.getDeclaringClass().equals(OBJECT_TYPE)) continue;
                if (OBJECT_TYPE.getDeclaredMethod(mi.getName(), mi.getParameters()) != null) continue;

                // we have two methods, so no SAM
                if (found != null) return null;
                found = mi;
            }
            return found;

        } else {

            List<MethodNode> methods = type.getAbstractMethods();
            MethodNode found = null;
            if (methods != null) {
                for (MethodNode mi : methods) {
                    if (!hasUsableImplementation(type, mi)) {
                        if (found != null) return null;
                        found = mi;
                    }
                }
            }
            return found;
        }
    }

    private static boolean hasUsableImplementation(ClassNode c, MethodNode m) {
        if (c == m.getDeclaringClass()) return false;
        MethodNode found = c.getDeclaredMethod(m.getName(), m.getParameters());
        if (found == null) return false;
        int asp = found.getModifiers() & ABSTRACT_STATIC_PRIVATE;
        int visible = found.getModifiers() & VISIBILITY;
        if (visible != 0 && asp == 0) return true;
        if (c.equals(OBJECT_TYPE)) return false;
        return hasUsableImplementation(c.getSuperClass(), m);
    }

    /**
     * Returns a super class or interface for a given class depending on a given target.
     * If the target is no super class or interface, then null will be returned.
     * For a non-primitive array type, returns an array of the componentType's super class
     * or interface if the target is also an array.
     *
     * @param clazz     the start class
     * @param goalClazz the goal class
     * @return the next super class or interface
     */
    public static ClassNode getNextSuperClass(ClassNode clazz, ClassNode goalClazz) {
        if (clazz.isArray()) {
            if (!goalClazz.isArray()) return null;
            ClassNode cn = getNextSuperClass(clazz.getComponentType(), goalClazz.getComponentType());
            if (cn != null) cn = cn.makeArray();
            return cn;
        }

        if (!goalClazz.isInterface()) {
            if (clazz.isInterface()) {
                if (OBJECT_TYPE.equals(clazz)) return null;
                return OBJECT_TYPE;
            } else {
                return clazz.getUnresolvedSuperClass();
            }
        }

        ClassNode[] interfaces = clazz.getUnresolvedInterfaces();
        for (ClassNode anInterface : interfaces) {
            if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(anInterface, goalClazz)) {
                return anInterface;
            }
        }
        //none of the interfaces here match, so continue with super class
        return clazz.getUnresolvedSuperClass();
    }
}
