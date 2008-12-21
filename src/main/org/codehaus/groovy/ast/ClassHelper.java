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
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Opcodes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        Double.class, Float.class, BigDecimal.class, BigInteger.class, Void.class,
        Reference.class, Class.class, MetaClass.class,     
    };

    private static final String[] primitiveClassNames = new String[] {
        "", "boolean", "char", "byte", "short", 
        "int", "long", "double", "float", "void"
    };
    
    public static final ClassNode 
        DYNAMIC_TYPE = new ClassNode(Object.class),  OBJECT_TYPE = DYNAMIC_TYPE,
        VOID_TYPE = new ClassNode(Void.TYPE),        CLOSURE_TYPE = new ClassNode(Closure.class),
        GSTRING_TYPE = new ClassNode(GString.class), LIST_TYPE = makeWithoutCaching(List.class),
        MAP_TYPE = new ClassNode(Map.class),         RANGE_TYPE = new ClassNode(Range.class),
        PATTERN_TYPE = new ClassNode(Pattern.class), STRING_TYPE = new ClassNode(String.class),
        SCRIPT_TYPE = new ClassNode(Script.class),   REFERENCE_TYPE = makeWithoutCaching(Reference.class),
        
        boolean_TYPE = new ClassNode(boolean.class),     char_TYPE = new ClassNode(char.class),
        byte_TYPE = new ClassNode(byte.class),           int_TYPE = new ClassNode(int.class),
        long_TYPE = new ClassNode(long.class),           short_TYPE = new ClassNode(short.class),
        double_TYPE = new ClassNode(double.class),       float_TYPE = new ClassNode(float.class),
        Byte_TYPE = new ClassNode(Byte.class),           Short_TYPE = new ClassNode(Short.class),
        Integer_TYPE = new ClassNode(Integer.class),     Long_TYPE = new ClassNode(Long.class),
        Character_TYPE = new ClassNode(Character.class), Float_TYPE = new ClassNode(Float.class),
        Double_TYPE = new ClassNode(Double.class),       Boolean_TYPE = new ClassNode(Boolean.class),
        BigInteger_TYPE =  new ClassNode(java.math.BigInteger.class),
        BigDecimal_TYPE = new ClassNode(java.math.BigDecimal.class),
        void_WRAPPER_TYPE = new ClassNode(Void.class),   
        
        CLASS_Type = new ClassNode(Class.class),        METACLASS_TYPE = new ClassNode(MetaClass.class),
        GENERATED_CLOSURE_Type = new ClassNode(GeneratedClosure.class),
        Enum_Type = new ClassNode("java.lang.Enum",0,OBJECT_TYPE),
        Annotation_TYPE = new ClassNode("java.lang.annotation.Annotation",0,OBJECT_TYPE);
        
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
        void_WRAPPER_TYPE, REFERENCE_TYPE, CLASS_Type, METACLASS_TYPE,
        GENERATED_CLOSURE_Type, Enum_Type, Annotation_TYPE
    };

    
    private static ClassNode[] numbers = new ClassNode[] {
        char_TYPE, byte_TYPE, short_TYPE, int_TYPE, long_TYPE, 
        double_TYPE, float_TYPE, Short_TYPE, Byte_TYPE, Character_TYPE,
        Integer_TYPE, Float_TYPE, Long_TYPE, Double_TYPE, BigInteger_TYPE,
        BigDecimal_TYPE
    };

    protected static final ClassNode[] EMPTY_TYPE_ARRAY = {};
    
    public static final String OBJECT = "java.lang.Object";    
    
    
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
        ClassNode t = new ClassNode(c);
        if (includeGenerics) VMPluginFactory.getPlugin().setAdditionalClassInformation(t);
        return t;
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
     * represents the wrapper class. For exmaple for boolean, the 
     * wrapper class is java.lang.Boolean.
     * 
     * If the parameter is no primitve type, the redirected 
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
     * Test to determine if a ClasNode is a primitve type. 
     * Note: this only works for ClassNodes created using a
     * predefined ClassNode
     * 
     * @see #make(Class)
     * @see #make(String)
     * @param cn the ClassNode containing a possible primitive type
     * @return true if the ClassNode is a primitve type
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
        return make(Reference.class);
    }

    public static boolean isCachedType(ClassNode type) {
        for (int i=0;i<types.length; i++) {
            if (types[i] == type) return true;
        }
        return false;
    }
}
