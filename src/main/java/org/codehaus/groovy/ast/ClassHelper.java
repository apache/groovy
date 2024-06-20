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
import groovy.transform.Sealed;
import org.apache.groovy.util.Maps;
import org.apache.groovy.util.concurrent.ManagedIdentityConcurrentMap;
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GeneratedLambda;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Opcodes;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.SerializedLambda;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;

/**
 * Helper for {@link ClassNode} and classes handling them.  Contains a set of
 * pre-defined instances for the most used types and some code for cached node
 * creation and basic handling.
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

    public static final Class[] TUPLE_CLASSES = new Class[]{
            Tuple0.class, Tuple1.class, Tuple2.class, Tuple3.class, Tuple4.class, Tuple5.class, Tuple6.class,
            Tuple7.class, Tuple8.class, Tuple9.class, Tuple10.class, Tuple11.class, Tuple12.class, Tuple13.class,
            Tuple14.class, Tuple15.class, Tuple16.class
    };

    private static final String[] primitiveClassNames = new String[]{
            "", "boolean", "char", "byte", "short", "int", "long", "double", "float", "void"
    };

    public static final ClassNode
            OBJECT_TYPE = makeCached(Object.class),
            CLOSURE_TYPE = makeCached(Closure.class),
            GSTRING_TYPE = makeCached(GString.class),
            RANGE_TYPE = makeCached(Range.class),
            PATTERN_TYPE = makeCached(Pattern.class),
            STRING_TYPE = makeCached(String.class),
            SCRIPT_TYPE = makeCached(Script.class),
            BINDING_TYPE = makeCached(Binding.class),
            THROWABLE_TYPE = makeCached(Throwable.class),

            boolean_TYPE = makeCached(boolean.class),
            char_TYPE = makeCached(char.class),
            byte_TYPE = makeCached(byte.class),
            int_TYPE = makeCached(int.class),
            long_TYPE = makeCached(long.class),
            short_TYPE = makeCached(short.class),
            double_TYPE = makeCached(double.class),
            float_TYPE = makeCached(float.class),
            Byte_TYPE = makeCached(Byte.class),
            Short_TYPE = makeCached(Short.class),
            Integer_TYPE = makeCached(Integer.class),
            Long_TYPE = makeCached(Long.class),
            Character_TYPE = makeCached(Character.class),
            Float_TYPE = makeCached(Float.class),
            Double_TYPE = makeCached(Double.class),
            Boolean_TYPE = makeCached(Boolean.class),
            BigInteger_TYPE = makeCached(java.math.BigInteger.class),
            BigDecimal_TYPE = makeCached(java.math.BigDecimal.class),
            Number_TYPE = makeCached(Number.class),

            VOID_TYPE = makeCached(Void.TYPE),
            void_WRAPPER_TYPE = makeCached(Void.class),
            METACLASS_TYPE = makeCached(MetaClass.class),
            Iterator_TYPE = makeCached(Iterator.class),
            Annotation_TYPE = makeCached(Annotation.class),
            ELEMENT_TYPE_TYPE = makeCached(ElementType.class),
            AUTOCLOSEABLE_TYPE = makeCached(AutoCloseable.class),
            CLONEABLE_TYPE = makeCached(Cloneable.class),
            SERIALIZABLE_TYPE = makeCached(Serializable.class),
            SERIALIZEDLAMBDA_TYPE = makeCached(SerializedLambda.class),
            SEALED_TYPE = makeCached(Sealed.class),
            OVERRIDE_TYPE = makeCached(Override.class),
            DEPRECATED_TYPE = makeCached(Deprecated.class),

            // uncached constants
            MAP_TYPE = makeWithoutCaching(Map.class),
            SET_TYPE = makeWithoutCaching(Set.class),
            LIST_TYPE = makeWithoutCaching(List.class),
            Enum_Type = makeWithoutCaching(Enum.class),
            CLASS_Type = makeWithoutCaching(Class.class),
            TUPLE_TYPE = makeWithoutCaching(Tuple.class),
            STREAM_TYPE = makeWithoutCaching(Stream.class),
            ITERABLE_TYPE = makeWithoutCaching(Iterable.class),
            REFERENCE_TYPE = makeWithoutCaching(Reference.class),
            COLLECTION_TYPE = makeWithoutCaching(Collection.class),
            COMPARABLE_TYPE = makeWithoutCaching(Comparable.class),
            GROOVY_OBJECT_TYPE = makeWithoutCaching(GroovyObject.class),
            GENERATED_LAMBDA_TYPE = makeWithoutCaching(GeneratedLambda.class),
            GENERATED_CLOSURE_Type = makeWithoutCaching(GeneratedClosure.class),
            GROOVY_INTERCEPTABLE_TYPE = makeWithoutCaching(GroovyInterceptable.class),
            GROOVY_OBJECT_SUPPORT_TYPE = makeWithoutCaching(GroovyObjectSupport.class);

    @Deprecated
    public static final ClassNode DYNAMIC_TYPE = OBJECT_TYPE;

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

    private static final String DYNAMIC_TYPE_METADATA = "_DYNAMIC_TYPE_METADATA_";

    protected static final ClassNode[] EMPTY_TYPE_ARRAY = ClassNode.EMPTY_ARRAY;

    public static final String OBJECT = "java.lang.Object";

    public static ClassNode dynamicType() {
        ClassNode node = OBJECT_TYPE.getPlainNodeReference();
        node.putNodeMetaData(DYNAMIC_TYPE_METADATA, Boolean.TRUE);
        return node;
    }

    public static ClassNode makeCached(Class c) {
        ClassNode classNode;
        final SoftReference<ClassNode> classNodeSoftReference = ClassHelperCache.classCache.get(c);
        if (classNodeSoftReference == null || (classNode = classNodeSoftReference.get()) == null) {
            classNode = new ClassNode(c);
            ClassHelperCache.classCache.put(c, new SoftReference<ClassNode>(classNode));
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
     * @param c class used to create the ClassNode
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
        if (name == null || name.length() == 0) return dynamicType();

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
        if (result == null) {
            result = PRIMITIVE_TYPE_TO_WRAPPER_TYPE_MAP.get(cn.redirect());
        }

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
    public static boolean isStaticConstantInitializerType(final ClassNode cn) {
        return isPrimitiveInt(cn) ||
                isPrimitiveFloat(cn) ||
                isPrimitiveLong(cn) ||
                isPrimitiveDouble(cn) ||
                isStringType(cn) ||
                // the next items require conversion to int when initializing
                isPrimitiveByte(cn) ||
                isPrimitiveChar(cn) ||
                isPrimitiveShort(cn);
    }

    public static boolean isNumberType(final ClassNode cn) {
        return isWrapperByte(cn) ||
                isWrapperShort(cn) ||
                isWrapperInteger(cn) ||
                isWrapperLong(cn) ||
                isWrapperFloat(cn) ||
                isWrapperDouble(cn) ||
                isPrimitiveByte(cn) ||
                isPrimitiveShort(cn) ||
                isPrimitiveInt(cn) ||
                isPrimitiveLong(cn) ||
                isPrimitiveFloat(cn) ||
                isPrimitiveDouble(cn);
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

    public static boolean isDynamicTyped(ClassNode type) {
        return type != null && Boolean.TRUE.equals(type.getNodeMetaData(DYNAMIC_TYPE_METADATA));
    }

    public static boolean isPrimitiveBoolean(ClassNode type) {
        return type.redirect() == boolean_TYPE;
    }

    public static boolean isPrimitiveChar(ClassNode type) {
        return type.redirect() == char_TYPE;
    }

    public static boolean isPrimitiveByte(ClassNode type) {
        return type.redirect() == byte_TYPE;
    }

    public static boolean isPrimitiveInt(ClassNode type) {
        return type.redirect() == int_TYPE;
    }

    public static boolean isPrimitiveLong(ClassNode type) {
        return type.redirect() == long_TYPE;
    }

    public static boolean isPrimitiveShort(ClassNode type) {
        return type.redirect() == short_TYPE;
    }

    public static boolean isPrimitiveDouble(ClassNode type) {
        return type.redirect() == double_TYPE;
    }

    public static boolean isPrimitiveFloat(ClassNode type) {
        return type.redirect() == float_TYPE;
    }

    public static boolean isPrimitiveVoid(ClassNode type) {
        return type.redirect() == VOID_TYPE;
    }

    public static boolean isWrapperBoolean(ClassNode type) {
        return type != null && type.redirect() == Boolean_TYPE;
    }

    public static boolean isWrapperCharacter(ClassNode type) {
        return type != null && type.redirect() == Character_TYPE;
    }

    public static boolean isWrapperByte(ClassNode type) {
        return type != null && type.redirect() == Byte_TYPE;
    }

    public static boolean isWrapperInteger(ClassNode type) {
        return type != null && type.redirect() == Integer_TYPE;
    }

    public static boolean isWrapperLong(ClassNode type) {
        return type != null && type.redirect() == Long_TYPE;
    }

    public static boolean isWrapperShort(ClassNode type) {
        return type != null && type.redirect() == Short_TYPE;
    }

    public static boolean isWrapperDouble(ClassNode type) {
        return type != null && type.redirect() == Double_TYPE;
    }

    public static boolean isWrapperFloat(ClassNode type) {
        return type != null && type.redirect() == Float_TYPE;
    }

    public static boolean isWrapperVoid(ClassNode type) {
        return type != null && type.redirect() == void_WRAPPER_TYPE;
    }

    public static boolean isBigIntegerType(ClassNode type) {
        return BigInteger_TYPE.equals(type);
    }

    public static boolean isBigDecimalType(ClassNode type) {
        return BigDecimal_TYPE.equals(type);
    }

    public static boolean isStringType(ClassNode type) {
        return STRING_TYPE.equals(type);
    }

    public static boolean isGStringType(ClassNode type) {
        return GSTRING_TYPE.equals(type);
    }

    public static boolean isObjectType(ClassNode type) {
        return OBJECT_TYPE.equals(type);
    }

    public static boolean isGroovyObjectType(ClassNode type) {
        return GROOVY_OBJECT_TYPE.equals(type);
    }

    public static boolean isClassType(ClassNode type) {
        return CLASS_Type.equals(type);
    }

    static class ClassHelperCache {
        static ManagedIdentityConcurrentMap<Class, SoftReference<ClassNode>> classCache = new ManagedIdentityConcurrentMap<>(128);
    }

    public static boolean isSAMType(final ClassNode type) {
        return findSAM(type) != null;
    }

    public static boolean isFunctionalInterface(final ClassNode type) {
        // Functional interface must be an interface at first, or the following exception will occur:
        // java.lang.invoke.LambdaConversionException: Functional interface SamCallable is not an interface
        return type != null && type.isInterface() && isSAMType(type);
    }

    /**
     * Checks if the type is a generated function, i.e. closure or lambda.
     *
     * @since 3.0.0
     */
    public static boolean isGeneratedFunction(final ClassNode type) {
        return type.implementsAnyInterfaces(GENERATED_CLOSURE_Type, GENERATED_LAMBDA_TYPE);
    }

    /**
     * Returns the single abstract method of a class node, if it is a SAM type, or null otherwise.
     *
     * @param type a type for which to search for a single abstract method
     * @return the method node if type is a SAM type, null otherwise
     */
    public static MethodNode findSAM(final ClassNode type) {
        if (type == null) return null;
        if (type.isInterface()) {
            MethodNode sam = null;
            for (MethodNode mn : type.getAbstractMethods()) {
                // ignore methods that will have an implementation
                if (Traits.hasDefaultImplementation(mn)) continue;

                final String name = mn.getName();
                if (OBJECT_METHOD_NAME_SET.contains(name)) {
                    // Avoid unnecessary checking for `Object` methods as possible as we could
                    if (OBJECT_TYPE.getDeclaredMethod(name, mn.getParameters()) != null) continue;
                }

                // we have two methods, so no SAM
                if (sam != null) return null;
                sam = mn;
            }
            return sam;
        }
        if (type.isAbstract()) {
            MethodNode sam = null;
            for (MethodNode mn : type.getAbstractMethods()) {
                if (!hasUsableImplementation(type, mn)) {
                    if (sam != null) return null;
                    sam = mn;
                }
            }
            return sam;
        }
        return null;
    }

    private static boolean hasUsableImplementation(final ClassNode c, final MethodNode m) {
        ClassNode declaringClass = m.getDeclaringClass();
        if (c.equals(declaringClass)) return false;
        // GROOVY-10540: GroovyObject declared and Verifier not run yet
        if (isGroovyObjectType(declaringClass) && c.getCompileUnit() != null) return true;

        MethodNode found = c.getDeclaredMethod(m.getName(), m.getParameters());
        if (found == null) return false;

        int modifiers = (found.getModifiers() & 0x40F);//ACC_ABSTRACT|ACC_STATIC|ACC_PROTECTED|ACC_PRIVATE|ACC_PUBLIC
        if (modifiers == Opcodes.ACC_PUBLIC || modifiers == Opcodes.ACC_PROTECTED) return true;

        return !isObjectType(c) && hasUsableImplementation(c.getSuperClass(), m);
    }

    /**
     * Returns a super class or interface for a given class depending on supplied
     * target. If the target is not a super class or interface, then null will be
     * returned. For a non-primitive array type -- if the target is also an array
     * -- returns an array of the component type's super class or interface.
     */
    public static ClassNode getNextSuperClass(final ClassNode source, final ClassNode target) {
        if (source.isArray()) {
            if (!target.isArray()) return null;

            ClassNode cn = getNextSuperClass(source.getComponentType(), target.getComponentType());
            if (cn != null) cn = cn.makeArray();
            return cn;
        }

        if (target.isInterface()) {
            for (ClassNode face : source.getUnresolvedInterfaces()) {
                if (implementsInterfaceOrIsSubclassOf(face, target)) {
                    return face;
                }
            }
        } else if (source.isInterface()) {
            return OBJECT_TYPE;
        }

        return source.getUnresolvedSuperClass();
    }

    private static final Set<String> OBJECT_METHOD_NAME_SET =
            Collections.unmodifiableSet(Arrays.stream(Object.class.getMethods()).map(m -> m.getName()).collect(Collectors.toSet()));
}
