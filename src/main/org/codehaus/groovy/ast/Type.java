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
package org.codehaus.groovy.ast;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Range;
import groovy.lang.Script;
import groovy.lang.Reference;


/**
 * Represents a type, either a dynamic type or statically defined type
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Type {

    public static final Type 
        DYNAMIC_TYPE = new Type(),              OBJECT_TYPE = DYNAMIC_TYPE,
        VOID_TYPE = new Type(Void.TYPE),        CLOSURE_TYPE = new Type(Closure.class),
        GSTRING_TYPE = new Type(GString.class), LIST_TYPE = new Type(List.class),
        MAP_TYPE = new Type(Map.class),         RANGE_TYPE = new Type(Range.class),
        PATTERN_TYPE = new Type(Pattern.class), STRING_TYPE = new Type(String.class),
        SCRIPT_TYPE = new Type(Script.class),
        
        boolean_TYPE = new Type(boolean.class),     char_TYPE = new Type(char.class),
        byte_TYPE = new Type(byte.class),           int_TYPE = new Type(int.class),
        long_TYPE = new Type(long.class),           short_TYPE = new Type(short.class),
        double_TYPE = new Type(double.class),       float_TYPE = new Type(float.class),
        Byte_TYPE = new Type(Byte.class),           Short_TYPE = new Type(Short.class),
        Integer_TYPE = new Type(Integer.class),     Long_TYPE = new Type(Long.class),
        Character_TYPE = new Type(Character.class), Float_TYPE = new Type(Float.class),
        Double_TYPE = new Type(Double.class),       Boolean_TYPE = new Type(Boolean.class),
        BigInteger_TYPE =  new Type(java.math.BigInteger.class),
        BigDecimal_TYPE = new Type(java.math.BigDecimal.class);
    
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
            BigDecimal.class.getName(), BigInteger.class.getName()
    };
    
    private static Type[] types = new Type[] {
            OBJECT_TYPE,
            boolean_TYPE, char_TYPE, byte_TYPE, short_TYPE,
            int_TYPE, long_TYPE, double_TYPE, float_TYPE,
            VOID_TYPE, CLOSURE_TYPE, GSTRING_TYPE,
            LIST_TYPE, MAP_TYPE, RANGE_TYPE, PATTERN_TYPE,
            SCRIPT_TYPE, STRING_TYPE, Boolean_TYPE, Character_TYPE,
            Byte_TYPE, Short_TYPE, Integer_TYPE, Long_TYPE,
            Double_TYPE, Float_TYPE, BigDecimal_TYPE, BigInteger_TYPE
    };
    
    private static Class[] classes = new Class[] {
            Object.class, Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE,
            Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE, Void.TYPE,
            Closure.class, GString.class, List.class, Map.class, Range.class,
            Pattern.class, Script.class, String.class,  Boolean.class, 
            Character.class, Byte.class, Short.class, Integer.class, Long.class,
            Double.class, Float.class, BigDecimal.class, BigInteger.class
    };
    
    private static Type[] numbers = new Type[] {
            char_TYPE, byte_TYPE, short_TYPE, int_TYPE, long_TYPE, 
            double_TYPE, float_TYPE, Short_TYPE, Byte_TYPE, Character_TYPE,
            Integer_TYPE, Float_TYPE, Long_TYPE, Double_TYPE, BigInteger_TYPE,
            BigDecimal_TYPE
    };
    
    public static final String OBJECT = "java.lang.Object";
    
    private String name;
    private boolean dynamic;
    transient private Type realType;
    private Class typeClass=null;
    private boolean typeResolved = false;

    private Type() {
        this.name = OBJECT;
        this.typeClass = Object.class;
        this.dynamic = true;
        realType=this;
    }

    private Type(String name) {
        //setName(ensureJavaTypeNameSyntax(name));
        setName(name);
        this.dynamic = false;
        realType=this;
    }
    
    private Type(Class c) {
        this.name = c.getName();
        this.dynamic = false;
        this.typeClass = c;
        realType=this;
    }

    private Type(String name, boolean isDynamic) {
        this(name);
        this.dynamic = isDynamic;
        realType=this;
    }
    
    public static Type makeType() {
        return DYNAMIC_TYPE;
    }
    
    public static Type makeType(String name) {
        if (name == null || name.length() == 0) return DYNAMIC_TYPE;
        
        for (int i=0; i<classes.length; i++) {
            String cname = classes[i].getName();
            if (name.equals(cname)) return types[i];
        }        
        return new Type(name);
    }
    
    public static Type makeType(Class c) {
        for (int i=0; i<classes.length; i++) {
            if (c==classes[i]) return types[i];
        }
        if (c.isArray()) return makeType(c.getComponentType()).makeArray();
        Type t = new Type(c.getName(),false);
        t.setTypeClass(c);
        return t;
    }
    
    public static Type makeType(String name, boolean isDynamic) {
        Type t = makeType(name);
        t.setDynamic(isDynamic);
        return t;
    }
    
    public Type makeArray() {
        if (typeClass!=null) {
            return new Type(Array.newInstance(typeClass,0).getClass());
        } else {
            return new Type(name+"[]");
        }
    }

    public String toString() {
        return "[name:" + name + " dynamic: " + dynamic + " real name: " + realType.getName() + "]"+super.toString();
    }

    public String getName() {
        return name;
    }

	/**
	 * @return Returns the realName.
	 */
	public Type getRealType() {
		return realType;
	}
    
	/**
	 * @param realName The realName to set.
	 */
	public void setRealType(Type real) {
		this.realType = real;
	}

    public void setDynamic(boolean b) {
        dynamic = b;
    }

    String failure = "";
    private boolean resolveFailed = false;

    public Class getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(Class typeClass) {
        if (typeClass != null) {
            this.typeClass = typeClass;
            name = typeClass.getName();
            setTypeResolved(true);
        }
    }

    /**
     * true if the datatype can be changed, false otherwise.
     * @return
     */
    public boolean isDynamic() {
        return true;
    }

   
    public void setName(String name) {
        if (name == null)
            throw new GroovyRuntimeException("cannot set null on type");
        // handle primitives first
        if (name.equals("int")) {
            setTypeClass(Integer.TYPE);
            return;
        }
        else if (name.equals("long")) {
            setTypeClass(Long.TYPE);
            return;
        }
        else if (name.equals("short")) {
            setTypeClass(Short.TYPE);
            return;
        }
        else if (name.equals("float")) {
            setTypeClass(Float.TYPE);
            return;
        }
        else if (name.equals("double")) {
            setTypeClass(Double.TYPE);
            return;
        }
        else if (name.equals("byte")) {
            setTypeClass(Byte.TYPE);
            return;
        }
        else if (name.equals("char")) {
            setTypeClass(Character.TYPE);
            return;
        }
        else if (name.equals("boolean")) {
            setTypeClass(Boolean.TYPE);
            return;
        }

        if (name.endsWith("[]")) {
            String prefix = "[";
            name = name.substring(0, name.length() - 2);

            if (name.equals("int")) {
                this.name = prefix + "I";
            }
            else if (name.equals("long")) {
                this.name = prefix + "J";
            }
            else if (name.equals("short")) {
                this.name = prefix + "S";
            }
            else if (name.equals("float")) {
                this.name = prefix + "F";
            }
            else if (name.equals("double")) {
                this.name = prefix + "D";
            }
            else if (name.equals("byte")) {
                this.name = prefix + "B";
            }
            else if (name.equals("char")) {
                this.name = prefix + "C";
            }
            else if (name.equals("boolean")) {
                this.name = prefix + "Z";
            } else {
                this.name = prefix + "L" + name + ";";
            }
        }
        else {
            this.name = name;
        }
        if (this.name == null) {
            System.out.println("Expression.setType(): null");
            System.out.println("name = " + name);
        }
        try {
            this.setTypeClass(Class.forName(this.name, false, this.getClass().getClassLoader()));
        } catch (Throwable e) {
            this.typeResolved = false;
        }
    }

    public boolean isTypeResolved() {
        return typeResolved;
    }

    public void setTypeResolved(boolean b) {
        this.typeResolved = b;
        this.resolveFailed = false;
    }
    
    public static boolean isNumber(String type) {
        if (type==null) return false;
        if (    type.equals("int") ||
                type.equals("short") ||
                type.equals("byte") ||
                type.equals("char") ||
                type.equals("float") ||
                type.equals("long") ||
                type.equals("double") ||
                type.equals("java.lang.Short") ||
                type.equals("java.lang.Byte") ||
                type.equals("java.lang.Character") ||
                type.equals("java.lang.Integer") ||
                type.equals("java.lang.Float") ||
                type.equals("java.lang.Long") ||
                type.equals("java.lang.Double") ||
                type.equals("java.math.BigInteger") ||
                type.equals("java.math.BigDecimal"))
        {
            return true;
        }
        return false;
    }
    
    public boolean isNumber() {
        return isNumber(name);
    }
    
    public static String ensureJavaTypeNameSyntax(String typename) {
        // if the typename begins with "[", ends with ";", or is
        // one character long, it's in .class syntax.
        if (typename.charAt(0) == '[') {
            return ensureJavaTypeNameSyntax(typename.substring(1)) + "[]";
        }
        if (typename.length() == 1) {
            switch (typename.charAt(0)) {
                case 'B':
                    return "byte";
                case 'C':
                    return "char";
                case 'D':
                    return "double";
                case 'F':
                    return "float";
                case 'J':
                    return "long";
                case 'I':
                    return "int";
                case 'S':
                    return "short";
                case 'V':
                    return "void";
                case 'Z':
                    return "boolean";
            }
        }
        if (typename.endsWith(";")) {
            // Type should be "Lclassname;"
            return typename.substring(1, typename.length() - 1);
        }
        return typename;
    }
    
    public boolean isArray(){
        if (typeClass!=null) return typeClass.isArray();
        return name.endsWith("[]");
    }
    
    public boolean equals(Object o) {
        Type type = (Type) o;
        if (this==o) return true;
        Class otherTypeClass = type.getTypeClass();
        if (typeClass!=null && otherTypeClass!=null && typeClass==otherTypeClass) return true;
        return name.equals(type.getName());
    }
    
    public boolean isPrimitiveType() {
        return (typeClass!=null && typeClass.isPrimitive());
    }
    
    public Type getComponentType() {
        if (!isArray()) return this;
        Class c = getTypeClass();
        if (c!=null) {
            c = c.getComponentType();
            return makeType(c);
        }
        String name = getName();
        name = name.substring(0,name.length()-2);
        return makeType(name,isDynamic());
    }
    
    public Type makeReference() {
        Type t = makeType(Reference.class);
        t.setRealType(this);
        return t;
    }
    
    public Type getWrapper() {
        if (!isPrimitiveType()) return this;
        if (this==boolean_TYPE) {
            return Boolean_TYPE;
        } else if (this==byte_TYPE) {
            return Byte_TYPE;
        } else if (this==char_TYPE) {
            return Character_TYPE;
        } else if (this==short_TYPE) {
            return Short_TYPE;
        } else if (this==int_TYPE) {
            return Integer_TYPE;
        } else if (this==long_TYPE) {
            return Long_TYPE;
        } else if (this==float_TYPE) {
            return Float_TYPE;
        } else if (this==double_TYPE) {
            return Double_TYPE;
        } else {
            return this;
        }
    }
}
