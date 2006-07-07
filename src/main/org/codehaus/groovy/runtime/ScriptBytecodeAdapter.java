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
package org.codehaus.groovy.runtime;

import groovy.lang.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.wrappers.GroovyObjectWrapper;
import org.codehaus.groovy.runtime.wrappers.PojoWrapper;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

/**
 * A static helper class to make bytecode generation easier and act as a facade over the Invoker. 
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ScriptBytecodeAdapter {
    public static final Object[] EMPTY_ARGS = {
    };
    
    private static Object unwrap(GroovyRuntimeException gre) throws Throwable{
        Throwable th = gre;
        if (th.getCause()!=null && th.getCause()!=gre) th=th.getCause();
        if (th!=gre && (th instanceof GroovyRuntimeException)) unwrap((GroovyRuntimeException) th);
        throw th;
    }

    public static Object invokeMethod(Object object, String methodName, Object arguments)  throws Throwable{
        try {
            return InvokerHelper.invokeMethod(object, methodName, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }
    
    public static Object invokeMethodSafe(Object object, String methodName, Object arguments) throws Throwable{
        if (object != null) return invokeMethod(object, methodName, arguments);
        return null;
    }    

    public static Object invokeMethodSpreadSafe(Object object, String methodName, Object arguments) throws Throwable{
        if (object != null) {
            if (object instanceof List) {
                List list = (List) object;
                List answer = new ArrayList();
                Iterator it = list.iterator();
                for (; it.hasNext();) {
                    answer.add(invokeMethodSafe(it.next(), methodName, arguments));
                }
                return answer;
            }
            else
                return invokeMethodSafe(object, methodName, arguments);
        }
        return null;
    }    

    public static Object invokeStaticMethod(String type, String methodName, Object arguments) throws Throwable{
        try {
            return InvokerHelper.invokeStaticMethod(type, methodName, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object invokeConstructorAt(Class at, Class type, Object arguments) throws Throwable{
        try {
            return InvokerHelper.invokeConstructorAt(at, type, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object invokeNoArgumentsConstructorAt(Class at, Class type) throws Throwable {
        return invokeConstructorAt(at, type, EMPTY_ARGS);
    }
    
    
    public static Object invokeConstructorOf(Class type, Object arguments) throws Throwable{
        try {
            return InvokerHelper.invokeConstructorOf(type, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }  
    }
    
    public static Object invokeNoArgumentsConstructorOf(Class type) throws Throwable {
        return invokeConstructorOf(type, EMPTY_ARGS);
    }
    
    public static Object invokeClosure(Object closure, Object arguments) throws Throwable {
        return invokeMethod(closure, "doCall", arguments);
    }    
    
    public static Object invokeSuperMethod(Object object, String methodName, Object arguments) throws Throwable{
        try {
            return InvokerHelper.invokeSuperMethod(object, methodName, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        } 
    }
    
    public static Object invokeNoArgumentsMethod(Object object, String methodName) throws Throwable {
        return invokeMethod(object, methodName, EMPTY_ARGS);
    }
    
    public static Object invokeNoArgumentsMethodSafe(Object object, String methodName) throws Throwable {
        if (object != null) return invokeNoArgumentsMethod(object, methodName);
        return null;
    }
    
    public static Object invokeNoArgumentsMethodSpreadSafe(Object object, String methodName) throws Throwable {
        if (object != null) {
            if (object instanceof List) {
                List list = (List) object;
                List answer = new ArrayList();
                Iterator it = list.iterator();
                for (; it.hasNext();) {
                    answer.add(invokeNoArgumentsMethod(it.next(), methodName));
                }
                return answer;
            }
            else
                return invokeNoArgumentsMethod(object, methodName);
        }
        return null;
    }
    
    public static Object invokeStaticNoArgumentsMethod(String type, String methodName) throws Throwable {
        return invokeStaticMethod(type, methodName, EMPTY_ARGS);
    }
    
    public static int asInt(Object value) throws Throwable {
        try {
            return InvokerHelper.asInt(value);
        } catch (GroovyRuntimeException gre) {
           unwrap(gre);
           // return never reached
           return -1;
        }
    }
    
    /**
     * Provides a hook for type coercion of the given object to the required type
     *
     * @param type   of object to convert the given object to
     * @param object the object to be converted
     * @return the original object or a new converted value
     * @throws Throwable 
     */
    public static Object asType(Object object, Class type) throws Throwable {
        try {
            return InvokerHelper.asType(object, type);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }



    // Attributes
    //-------------------------------------------------------------------------
    public static Object getAttribute(Object object, String attribute) throws Throwable {
        try {
            return InvokerHelper.getAttribute(object, attribute);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object getAttributeSafe(Object object, String attribute) throws Throwable {
        if (object != null) return getAttribute(object, attribute);
        return null;
    }

    public static Object getAttributeSpreadSafe(Object object, String attribute) throws Throwable {
        if (object != null) {
            if (object instanceof List) {
                List list = (List) object;
                List answer = new ArrayList();
                Iterator it = list.iterator();
                for (; it.hasNext(); ) {
                    answer.add(getAttributeSafe(it.next(), attribute));
                }
                return answer;
            }
            else
                return getAttributeSafe(object, attribute);
        }
        return null;
    }

    public static void setAttribute(Object object, String attribute, Object newValue) throws Throwable {
        try {
            InvokerHelper.setAttribute(object, attribute, newValue);
        } catch (GroovyRuntimeException gre) {
            unwrap(gre);
        }
    }
    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     * @throws Throwable
     */
    public static void setAttribute2(Object newValue, Object object, String property) throws Throwable {
        setAttribute(object, property, newValue);
    }

    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     * @throws Throwable
     */
    public static void setAttributeSafe2(Object newValue, Object object, String property) throws Throwable {
        setAttribute2(newValue, object, property);
    }



    // Properties
    //-------------------------------------------------------------------------
    public static Object getProperty(Object object, String property) throws Throwable {
        try {
            return InvokerHelper.getProperty(object, property);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object getPropertySafe(Object object, String property) throws Throwable {
        if (object != null) return getProperty(object, property);
        return null;
    }

    public static Object getPropertySpreadSafe(Object object, String property) throws Throwable {
        if (object != null) {
            if (object instanceof List) {
                List list = (List) object;
                List answer = new ArrayList();
                Iterator it = list.iterator();
                for (; it.hasNext(); ) {
                    answer.add(getPropertySafe(it.next(), property));
                }
                return answer;
            }
            else
                return getPropertySafe(object, property);
        }
        return null;
    }

    public static void setProperty(Object object, String property, Object newValue) throws Throwable {
        try {
            InvokerHelper.setProperty(object, property, newValue);
        } catch (GroovyRuntimeException gre) {
            unwrap(gre);
        }
    }
    
    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     * @throws Throwable 
     */
    public static void setProperty2(Object newValue, Object object, String property) throws Throwable {
        setProperty(object, property, newValue);
    }

    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     * @throws Throwable 
     */
    public static void setPropertySafe2(Object newValue, Object object, String property) throws Throwable {
        setProperty2(newValue, object, property);
    }


    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     * @throws Throwable 
     */
    public static void setGroovyObjectProperty(Object newValue, GroovyObject object, String property) throws Throwable {
        try {
            object.setProperty(property, newValue);
        } catch (GroovyRuntimeException gre) {
            unwrap(gre);
        }
    }

    public static Object getGroovyObjectProperty(GroovyObject object, String property) throws Throwable {
        try {
            return object.getProperty(property);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }


    /**
     * Returns the method pointer for the given object name
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        return InvokerHelper.getMethodPointer(object, methodName);
    }

    // Coercions
    //-------------------------------------------------------------------------
    public static Iterator asIterator(Object collection) throws Throwable {
        try {
            return InvokerHelper.asIterator(collection);
        } catch (GroovyRuntimeException gre) {
            return (Iterator) unwrap(gre);
        }
    }    
    
    public static boolean asBool(Object object) throws Throwable {
        try {
            return InvokerHelper.asBool(object);
        } catch (GroovyRuntimeException gre) {
            unwrap(gre);
            //return never reached
            return false;
        }
    }
    
    public static boolean notBoolean(boolean bool) {
        return !bool;
    }    
    
    public static boolean notObject(Object object) throws Throwable {
        return !asBool(object);
    }
    
    public static Pattern regexPattern(Object regex) throws Throwable {
        try {
            return InvokerHelper.regexPattern(regex);
        } catch (GroovyRuntimeException gre) {
            return (Pattern) unwrap(gre);
        }
    }
    
    public static Object spreadList(Object value) throws Throwable {
        try {
            return InvokerHelper.spreadList(value);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object spreadMap(Object value) throws Throwable {
        try {
            return InvokerHelper.spreadMap(value);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object negate(Object value) throws Throwable {
        try {
            return InvokerHelper.negate(value);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }
    
    public static Object bitNegate(Object value) throws Throwable {
        try {
            return InvokerHelper.bitNegate(value);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }
    
    /**
     * @param a    array of primitives
     * @param type component type of the array
     * @return
     * @throws Throwable 
     */
    public static Object[] convertPrimitiveArray(Object a, Class type) throws Throwable {
        try {
            return InvokerHelper.convertPrimitiveArray(a,type);
        } catch (GroovyRuntimeException gre) {
            return (Object[])unwrap(gre);
        }
    }
    
    public static Object convertToPrimitiveArray(Object a, Class type) throws Throwable {
        try {
            return InvokerHelper.convertToPrimitiveArray(a,type);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static boolean compareIdentical(Object left, Object right) {
        return left == right;
    }
    
    public static boolean compareEqual(Object left, Object right) throws Throwable{
        try {
            return InvokerHelper.compareEqual(left, right);
        } catch (GroovyRuntimeException gre) {
            unwrap(gre);
            // return never reached
            return false;
        }
    }
    
    public static boolean compareNotEqual(Object left, Object right) throws Throwable{
        return !compareEqual(left, right);
    }
    
    public static Integer compareTo(Object left, Object right) throws Throwable{
        try {
            return InvokerHelper.compareTo(left, right);
        } catch (GroovyRuntimeException gre) {
            return (Integer) unwrap(gre);
        }
    }    

    public static Matcher findRegex(Object left, Object right) throws Throwable{
        try {
            return InvokerHelper.findRegex(left, right);
        } catch (GroovyRuntimeException gre) {
            return (Matcher) unwrap(gre);
        }
    }
    
    public static boolean matchRegex(Object left, Object right) throws Throwable{
        try {
            return InvokerHelper.matchRegex(left, right);
        } catch (GroovyRuntimeException gre) {
            unwrap(gre);
            // return never reached
            return false;
        }
    }

    public static boolean compareLessThan(Object left, Object right) throws Throwable{
        return compareTo(left, right).intValue() < 0;
    }
    
    public static boolean compareLessThanEqual(Object left, Object right) throws Throwable{
        return compareTo(left, right).intValue() <= 0;
    }
    
    public static boolean compareGreaterThan(Object left, Object right) throws Throwable{
        return compareTo(left, right).intValue() > 0;
    }

    public static boolean compareGreaterThanEqual(Object left, Object right) throws Throwable{
        return compareTo(left, right).intValue() >= 0;
    }

    public static boolean isCase(Object switchValue, Object caseExpression) throws Throwable{
    	if (caseExpression == null) {
    		return switchValue == null;
    	}
        return asBool(invokeMethod(caseExpression, "isCase", new Object[]{switchValue}));
    }
    
    public static Tuple createTuple(Object[] array) throws Throwable{
        return new Tuple(array);
    }

    public static List createList(Object[] values) throws Throwable{
        return InvokerHelper.createList(values);
    }
    
    public static Wrapper createPojoWrapper(Object val, Class clazz) {
        return new PojoWrapper(val,clazz);
    }

    public static Wrapper createGroovyObjectWrapper(GroovyObject val, Class clazz) {
        return new GroovyObjectWrapper(val,clazz);
    }
    
    public static Map createMap(Object[] values) throws Throwable{
        return InvokerHelper.createMap(values);
    }
    
    public static List createRange(Object from, Object to, boolean inclusive) throws Throwable{
        try {
            return InvokerHelper.createRange(from,to,inclusive);
        } catch (GroovyRuntimeException gre) {
            return (List) unwrap(gre);
        }
    }
    
    public static void assertFailed(Object expression, Object message) {
        InvokerHelper.assertFailed(expression,message);
    }
    
    public static Object box(boolean value) {
        return value ? Boolean.TRUE : Boolean.FALSE;
    }

    public static Object box(byte value) {
        return new Byte(value);
    }

    public static Object box(char value) {
        return new Character(value);
    }

    public static Object box(short value) {
        return new Short(value);
    }

    public static Object box(int value) {
        return integerValue(value);
    }

    public static Object box(long value) {
        return new Long(value);
    }

    public static Object box(float value) {
        return new Float(value);
    }

    public static Object box(double value) {
        return new Double(value);
    }
    
    /**
     * get the Integer object from an int. Cached version is used for small ints.
     *
     * @param v
     * @return
     */
    public static Integer integerValue(int v) {
        return InvokerHelper.integerValue(v);
    }    

    public static byte byteUnbox(Object value) throws Throwable {
        Number n = (Number) asType(value, Byte.class);
        return n.byteValue();
    }

    public static char charUnbox(Object value) throws Throwable {
        Character n = (Character) asType(value, Character.class);
        return n.charValue();
    }

    public static short shortUnbox(Object value) throws Throwable {
        Number n = (Number) asType(value, Short.class);
        return n.shortValue();
    }

    public static int intUnbox(Object value) throws Throwable {
        Number n = (Number) asType(value, Integer.class);
        return n.intValue();
    }

    public static boolean booleanUnbox(Object value) throws Throwable {
        Boolean n = (Boolean) asType(value, Boolean.class);
        return n.booleanValue();
    }

    public static long longUnbox(Object value) throws Throwable {
        Number n = (Number) asType(value, Long.class);
        return n.longValue();
    }

    public static float floatUnbox(Object value) throws Throwable {
        Number n = (Number) asType(value, Float.class);
        return n.floatValue();
    }

    public static double doubleUnbox(Object value) throws Throwable {
        Number n = (Number) asType(value, Double.class);
        return n.doubleValue();
    }    
    
    public static MetaClass getMetaClass(Object object) {
        return InvokerHelper.getMetaClass(object);
    }

}
