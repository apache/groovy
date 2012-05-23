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

package org.codehaus.groovy.ast;

import groovy.lang.*;

import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.util.ManagedConcurrentMap;
import org.codehaus.groovy.util.ReferenceBundle;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Opcodes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.lang.ref.SoftReference;

/**
 * This class is a Helper for ClassNode and classes handling ClassNodes.
 * It does contain a set of predefined ClassNodes for the most used 
 * types and some code for cached ClassNode creation and basic 
 * ClassNode handling 
 * 
 * @author Jochen Theodorou
 */
public class ClassHelper {

    private static final Class[] classes = new Class[] {
        Object.class, Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE,
        Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE, Void.TYPE,
        Closure.class, GString.class, List.class, Map.class, Range.class,
        Pattern.class, Script.class, String.class,  Boolean.class, 
        Character.class, Byte.class, Short.class, Integer.class, Long.class,
        Double.class, Float.class, BigDecimal.class, BigInteger.class, 
        Number.class, Void.class, Reference.class, Class.class, MetaClass.class, 
        Iterator.class, GeneratedClosure.class, GroovyObjectSupport.class
    };

    private static final String[] primitiveClassNames = new String[] {
        "", "boolean", "char", "byte", "short", 
        "int", "long", "double", "float", "void"
    };
    
    public static final ClassNode 
        DYNAMIC_TYPE = makeCached(Object.class),  OBJECT_TYPE = DYNAMIC_TYPE,
        VOID_TYPE = makeCached(Void.TYPE),        CLOSURE_TYPE = makeCached(Closure.class),
        GSTRING_TYPE = makeCached(GString.class), LIST_TYPE = makeWithoutCaching(List.class),
        MAP_TYPE = makeWithoutCaching(Map.class), RANGE_TYPE = makeCached(Range.class),
        PATTERN_TYPE = makeCached(Pattern.class), STRING_TYPE = makeCached(String.class),
        SCRIPT_TYPE = makeCached(Script.class),   REFERENCE_TYPE = makeWithoutCaching(Reference.class),
        
        boolean_TYPE = makeCached(boolean.class),     char_TYPE = makeCached(char.class),
        byte_TYPE = makeCached(byte.class),           int_TYPE = makeCached(int.class),
        long_TYPE = makeCached(long.class),           short_TYPE = makeCached(short.class),
        double_TYPE = makeCached(double.class),       float_TYPE = makeCached(float.class),
        Byte_TYPE = makeCached(Byte.class),           Short_TYPE = makeCached(Short.class),
        Integer_TYPE = makeCached(Integer.class),     Long_TYPE = makeCached(Long.class),
        Character_TYPE = makeCached(Character.class), Float_TYPE = makeCached(Float.class),
        Double_TYPE = makeCached(Double.class),       Boolean_TYPE = makeCached(Boolean.class),
        BigInteger_TYPE =  makeCached(java.math.BigInteger.class),
        BigDecimal_TYPE = makeCached(java.math.BigDecimal.class),
        Number_TYPE = makeCached(Number.class),
        
        void_WRAPPER_TYPE = makeCached(Void.class),   METACLASS_TYPE = makeCached(MetaClass.class),
        Iterator_TYPE = makeCached(Iterator.class),

        // uncached constants.
        CLASS_Type = makeWithoutCaching(Class.class), COMPARABLE_TYPE = makeWithoutCaching(Comparable.class),        
        GENERATED_CLOSURE_Type = makeWithoutCaching(GeneratedClosure.class),
        GROOVY_OBJECT_SUPPORT_TYPE = makeWithoutCaching(GroovyObjectSupport.class),
        GROOVY_OBJECT_TYPE = makeWithoutCaching(GroovyObject.class),
        GROOVY_INTERCEPTABLE_TYPE = makeWithoutCaching(GroovyInterceptable.class),
        
        Enum_Type = new ClassNode("java.lang.Enum",0,OBJECT_TYPE),
        Annotation_TYPE = new ClassNode("java.lang.annotation.Annotation",0,OBJECT_TYPE),
        ELEMENT_TYPE_TYPE = new ClassNode("java.lang.annotation.ElementType",0,Enum_Type)
        ;
        
        
    static {
        Enum_Type.isPrimaryNode = false;
        Annotation_TYPE.isPrimaryNode = false;
    }
    
    private static ClassNode[] types = new ClassNode[] {
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
        Iterator_TYPE, GENERATED_CLOSURE_Type, GROOVY_OBJECT_SUPPORT_TYPE, 
        GROOVY_OBJECT_TYPE, GROOVY_INTERCEPTABLE_TYPE, Enum_Type, Annotation_TYPE
    };

    protected static final ClassNode[] EMPTY_TYPE_ARRAY = {};
    
    public static final String OBJECT = "java.lang.Object";

    public static ClassNode makeCached(Class c){
        final SoftReference<ClassNode> classNodeSoftReference = ClassHelperCache.classCache.get(c);
        ClassNode classNode;
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
     * @see #make(Class)
     * @param classes an array of classes used to create the ClassNodes
     * @return an array of ClassNodes
     */
    public static ClassNode[] make(Class[] classes) {
        ClassNode[] cns = new ClassNode[classes.length];
        for (int i=0; i<cns.length; i++) {
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
        return make(c,true);
    }
    
    public static ClassNode make(Class c, boolean includeGenerics) {
        for (int i=0; i<classes.length; i++) {
            if (c==classes[i]) return types[i];
        }
        if (c.isArray()) {
            ClassNode cn = make(c.getComponentType(),includeGenerics);
            return cn.makeArray();
        }
        return makeWithoutCaching(c,includeGenerics);
    }
    
    public static ClassNode makeWithoutCaching(Class c){
        return makeWithoutCaching(c,true);
    }
    
    public static ClassNode makeWithoutCaching(Class c, boolean includeGenerics){
        if (c.isArray()) {
            ClassNode cn = makeWithoutCaching(c.getComponentType(),includeGenerics);
            return cn.makeArray();
        }

        final ClassNode cached = makeCached(c);
        if (includeGenerics) {
            return cached;
        }
        else {
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
     * @see #make(String)
     * @param name of the class the ClassNode is representing
     */
    public static ClassNode makeWithoutCaching(String name) { 
        ClassNode cn = new ClassNode(name,Opcodes.ACC_PUBLIC,OBJECT_TYPE);
        cn.isPrimaryNode = false;
        return cn;
    }
    
    /**     * Creates a ClassNode using a given class.
     * If the name is one of the predefined ClassNodes then the 
     * corresponding ClassNode instance will be returned. If the
     * name is null or of length 0 the dynamic type is returned
     * 
     * @param name of the class the ClassNode is representing
     */
    public static ClassNode make(String name) {
        if (name == null || name.length() == 0) return DYNAMIC_TYPE;
        
        for (int i=0; i<primitiveClassNames.length; i++) {
            if (primitiveClassNames[i].equals(name)) return types[i];
        }
        
        for (int i=0; i<classes.length; i++) {
            String cname = classes[i].getName();
            if (name.equals(cname)) return types[i];
        }        
        return makeWithoutCaching(name);
    }
    
    /**
     * Creates a ClassNode containing the wrapper of a ClassNode 
     * of primitive type. Any ClassNode representing a primitive
     * type should be created using the predefined types used in
     * class. The method will check the parameter for known 
     * references of ClassNode representing a primitive type. If
     * Reference is found, then a ClassNode will be contained that
     * represents the wrapper class. For example for boolean, the
     * wrapper class is java.lang.Boolean.
     * 
     * If the parameter is no primitive type, the redirected 
     * ClassNode will be returned 
     *   
     * @see #make(Class)
     * @see #make(String)
     * @param cn the ClassNode containing a possible primitive type
     */
    public static ClassNode getWrapper(ClassNode cn) {
        cn = cn.redirect();
        if (!isPrimitiveType(cn)) return cn;
        if (cn==boolean_TYPE) {
            return Boolean_TYPE;
        } else if (cn==byte_TYPE) {
            return Byte_TYPE;
        } else if (cn==char_TYPE) {
            return Character_TYPE;
        } else if (cn==short_TYPE) {
            return Short_TYPE;
        } else if (cn==int_TYPE) {
            return Integer_TYPE;
        } else if (cn==long_TYPE) {
            return Long_TYPE;
        } else if (cn==float_TYPE) {
            return Float_TYPE;
        } else if (cn==double_TYPE) {
            return Double_TYPE;
        } else if (cn==VOID_TYPE) {
            return void_WRAPPER_TYPE;
        }
        else {
            return cn;
        }
    }

    public static ClassNode getUnwrapper(ClassNode cn) {
        cn = cn.redirect();
        if (isPrimitiveType(cn)) return cn;
        if (cn==Boolean_TYPE) {
            return boolean_TYPE;
        } else if (cn==Byte_TYPE) {
            return byte_TYPE;
        } else if (cn==Character_TYPE) {
            return char_TYPE;
        } else if (cn==Short_TYPE) {
            return short_TYPE;
        } else if (cn==Integer_TYPE) {
            return int_TYPE;
        } else if (cn==Long_TYPE) {
            return long_TYPE;
        } else if (cn==Float_TYPE) {
            return float_TYPE;
        } else if (cn==Double_TYPE) {
            return double_TYPE;
        }
        else {
            return cn;
        }
    }


    /**
     * Test to determine if a ClassNode is a primitive type.
     * Note: this only works for ClassNodes created using a
     * predefined ClassNode
     * 
     * @see #make(Class)
     * @see #make(String)
     * @param cn the ClassNode containing a possible primitive type
     * @return true if the ClassNode is a primitive type
     */
    public static boolean isPrimitiveType(ClassNode cn) {
        return  cn == boolean_TYPE ||
                cn == char_TYPE ||
                cn == byte_TYPE ||
                cn == short_TYPE ||
                cn == int_TYPE ||
                cn == long_TYPE ||
                cn == float_TYPE ||
                cn == double_TYPE ||
                cn == VOID_TYPE;
    }

    /**
     * Test to determine if a ClassNode is a type belongs to the list of types which
     * are allowed to initialize constants directly in bytecode instead of using &lt;cinit&gt;
     *
     * Note: this only works for ClassNodes created using a
     * predefined ClassNode
     *
     * @see #make(Class)
     * @see #make(String)
     * @param cn the ClassNode to be tested
     * @return true if the ClassNode is of int, float, long, double or String type
     */
    public static boolean isStaticConstantInitializerType(ClassNode cn) {
        return  cn == int_TYPE ||
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
        return  cn == Byte_TYPE ||
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
        static ManagedConcurrentMap<Class, SoftReference<ClassNode>> classCache = new ManagedConcurrentMap<Class, SoftReference<ClassNode>>(ReferenceBundle.getWeakBundle());
    }
}
