/*
 * ClassHelper.java created on 25.10.2005
 *
 */
package org.codehaus.groovy.ast;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.Range;
import groovy.lang.Reference;
import groovy.lang.Script;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;

/**
 * @author Jochen Theodorou
 */
public class ClassHelper {
    

    private static String[] names = new String[] {
        boolean.class.getName(),    char.class.getName(), 
        byte.class.getName(),       short.class.getName(),
        int.class.getName(),        long.class.getName(),
        double.class.getName(),     float.class.getName(),
        Object.class.getName(),     Void.TYPE.getName(),
        Closure.class.getName(),    GString.class.getName(),
        List.class.getName(),       Map.class.getName(),
        Range.class.getName(),      Pattern.class.getName(),
        Script.class.getName(),     String.class.getName(),
        Boolean.class.getName(),    Character.class.getName(),
        Byte.class.getName(),       Short.class.getName(),
        Integer.class.getName(),    Long.class.getName(),
        Double.class.getName(),     Float.class.getName(),
        BigDecimal.class.getName(), BigInteger.class.getName(),
        Void.class.getName()
    };
    
    private static Class[] classes = new Class[] {
        Object.class, Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE,
        Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE, Void.TYPE,
        Closure.class, GString.class, List.class, Map.class, Range.class,
        Pattern.class, Script.class, String.class,  Boolean.class, 
        Character.class, Byte.class, Short.class, Integer.class, Long.class,
        Double.class, Float.class, BigDecimal.class, BigInteger.class, Void.class
    };
    
    public static final ClassNode 
        DYNAMIC_TYPE = new ClassNode(Object.class),  OBJECT_TYPE = DYNAMIC_TYPE,
        VOID_TYPE = new ClassNode(Void.TYPE),        CLOSURE_TYPE = new ClassNode(Closure.class),
        GSTRING_TYPE = new ClassNode(GString.class), LIST_TYPE = new ClassNode(List.class),
        MAP_TYPE = new ClassNode(Map.class),         RANGE_TYPE = new ClassNode(Range.class),
        PATTERN_TYPE = new ClassNode(Pattern.class), STRING_TYPE = new ClassNode(String.class),
        SCRIPT_TYPE = new ClassNode(Script.class),
        
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
        void_WRAPPER_TYPE = new ClassNode(Void.class);
    
    private static ClassNode[] types = new ClassNode[] {
        OBJECT_TYPE,
        boolean_TYPE, char_TYPE, byte_TYPE, short_TYPE,
        int_TYPE, long_TYPE, double_TYPE, float_TYPE,
        VOID_TYPE, CLOSURE_TYPE, GSTRING_TYPE,
        LIST_TYPE, MAP_TYPE, RANGE_TYPE, PATTERN_TYPE,
        SCRIPT_TYPE, STRING_TYPE, Boolean_TYPE, Character_TYPE,
        Byte_TYPE, Short_TYPE, Integer_TYPE, Long_TYPE,
        Double_TYPE, Float_TYPE, BigDecimal_TYPE, BigInteger_TYPE, void_WRAPPER_TYPE
    };

    
    private static ClassNode[] numbers = new ClassNode[] {
        char_TYPE, byte_TYPE, short_TYPE, int_TYPE, long_TYPE, 
        double_TYPE, float_TYPE, Short_TYPE, Byte_TYPE, Character_TYPE,
        Integer_TYPE, Float_TYPE, Long_TYPE, Double_TYPE, BigInteger_TYPE,
        BigDecimal_TYPE
    };

    protected static final ClassNode[] EMPTY_TYPE_ARRAY = {};
    
    public static final String OBJECT = "java.lang.Object";    
    
    public static ClassNode make(Class c) {
        for (int i=0; i<classes.length; i++) {
            if (c==classes[i]) return types[i];
        }
        if (c.isArray()) {
            ClassNode cn = make(c.getComponentType());
            return cn.makeArray();
        }
        ClassNode t = new ClassNode(c);
        return t;
    }
    
    public static ClassNode makeWithoutCaching(String name) { 
        ClassNode cn = new ClassNode(name,Opcodes.ACC_PUBLIC,OBJECT_TYPE);
        cn.resolved = false;
        cn.isPrimaryNode = false;
        return cn;
    }
    
    public static ClassNode make(String name) {
        if (name == null || name.length() == 0) return DYNAMIC_TYPE;
        
        for (int i=0; i<classes.length; i++) {
            String cname = classes[i].getName();
            if (name.equals(cname)) return types[i];
        }        
        return makeWithoutCaching(name);
    }
    
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
    
    public static ClassNode makeReference() {
        return make(Reference.class);
    }

}
