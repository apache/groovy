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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static helper class to make bytecode generation easier and act as a facade over the Invoker. 
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ScriptBytecodeAdapter {
    public static final Object[] EMPTY_ARGS = {
    };
/*
    private static final Object[] EMPTY_MAIN_ARGS = new Object[]{new String[0]};

    private static final Invoker singleton = new Invoker();

    private static final Integer ZERO = new Integer(0);
    private static final Integer MINUS_ONE = new Integer(-1);
    private static final Integer ONE = new Integer(1);*/

    
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

    public static Object invokeStaticMethod(String type, String methodName, Object arguments) throws Throwable{
        try {
            return InvokerHelper.invokeStaticMethod(type, methodName, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object invokeConstructor(String type, Object arguments) throws Throwable{
        try {
            return InvokerHelper.invokeConstructor(type, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }
    }

    public static Object invokeConstructorOf(Class type, Object arguments) throws Throwable{
        try {
            return InvokerHelper.invokeConstructorOf(type, arguments);
        } catch (GroovyRuntimeException gre) {
            return unwrap(gre);
        }  
    }
    
    public static Object invokeNoArgumentsConstructorOf(Class type) throws Throwable{
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
        return asBool(invokeMethod(caseExpression, "isCase", new Object[]{switchValue}));
    }
    
    public static Tuple createTuple(Object[] array) throws Throwable{
        return new Tuple(array);
    }

    public static List createList(Object[] values) throws Throwable{
        return InvokerHelper.createList(values);
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

    /*
    public static void removeClass(Class clazz) {
        getInstance().removeMetaClass(clazz);
        Introspector.flushFromCaches(clazz);
    }

    public static Invoker getInstance() {
        return singleton;
    }

    public static Collection asCollection(Object collection) {
        return getInstance().asCollection(collection);
    }

    public static List asList(Object args) {
        return getInstance().asList(args);
    }

    public static String toString(Object arguments) {
        return getInstance().toString(arguments);
    }

    public static String toTypeString(Object[] arguments) {
        return getInstance().toTypeString(arguments);
    }

    public static String inspect(Object self) {
        return getInstance().inspect(self);
    }



    public static Object runScript(Class scriptClass, String[] args) {
        Binding context = new Binding(args);
        Script script = createScript(scriptClass, context);
        return invokeMethod(script, "run", EMPTY_ARGS);
    }

    public static Script createScript(Class scriptClass, Binding context) {
        try {
            final GroovyObject object = (GroovyObject) scriptClass.newInstance();
            Script script = null;
            if (object instanceof Script) {
                script = (Script) object;
            } else {
                // it could just be a class, so lets wrap it in a Script wrapper
                // though the bindings will be ignored
                script = new Script() {
                    public Object run() {
                        object.invokeMethod("main", EMPTY_MAIN_ARGS);
                        return null;
                    }
                };
                setProperties(object, context.getVariables());
            }
            script.setBinding(context);
            return script;
        } catch (Exception e) {
            throw new GroovyRuntimeException("Failed to create Script instance for class: " + scriptClass + ". Reason: " + e,
                    e);
        }
    }
*/
    
    /**
     * Sets the properties on the given object
     *
     * @param object
     * @param map
     */
/*    public static void setProperties(Object object, Map map) {
        getMetaClass(object).setProperties(object, map);
    }

    public static String getVersion() {
        String version = null;
        Package p = Package.getPackage("groovy.lang");
        if (p != null) {
            version = p.getImplementationVersion();
        }
        if (version == null) {
            version = "";
        }
        return version;
    }*/

    /**
     * Allows conversion of arrays into a mutable List
     *
     * @return the array as a List
     */
    /*protected static List primitiveArrayToList(Object array) {
        int size = Array.getLength(array);
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }*/

    /**
     * Writes the given object to the given stream
     */
/*    public static void write(Writer out, Object object) throws IOException {
        if (object instanceof String) {
            out.write((String) object);
        } else if (object instanceof Writable) {
            Writable writable = (Writable) object;
            writable.writeTo(out);
        } else if (object instanceof InputStream || object instanceof Reader) {
            // Copy stream to stream
            Reader reader;
            if (object instanceof InputStream) {
                reader = new InputStreamReader((InputStream) object);
            } else {
                reader = (Reader) object;
            }
            char[] chars = new char[8192];
            int i;
            while ((i = reader.read(chars)) != -1) {
                out.write(chars, 0, i);
            }
            reader.close();
        } else {
            out.write(toString(object));
        }
    }

    public static int[] convertToIntArray(Object a) {
        int[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[I")) {
            ans = (int[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new int[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Number) ia[i]).intValue();
            }
        }
        return ans;
    }

    public static boolean[] convertToBooleanArray(Object a) {
        boolean[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[Z")) {
            ans = (boolean[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new boolean[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Boolean) ia[i]).booleanValue();
            }
        }
        return ans;
    }

    public static byte[] convertToByteArray(Object a) {
        byte[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[B")) {
            ans = (byte[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new byte[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] != null)
                    ans[i] = ((Number) ia[i]).byteValue();
            }
        }
        return ans;
    }

    public static short[] convertToShortArray(Object a) {
        short[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[S")) {
            ans = (short[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new short[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Number) ia[i]).shortValue();
            }
        }
        return ans;
    }

    public static char[] convertToCharArray(Object a) {
        char[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[C")) {
            ans = (char[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new char[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Character) ia[i]).charValue();
            }
        }
        return ans;
    }

    public static long[] convertToLongArray(Object a) {
        long[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[J")) {
            ans = (long[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new long[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Number) ia[i]).longValue();
            }
        }
        return ans;
    }

    public static float[] convertToFloatArray(Object a) {
        float[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[F")) {
            ans = (float[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new float[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Number) ia[i]).floatValue();
            }
        }
        return ans;
    }

    public static double[] convertToDoubleArray(Object a) {
        double[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[D")) {
            ans = (double[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new double[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Number) ia[i]).doubleValue();
            }
        }
        return ans;
    }
*/
    
    /*

    private static Integer[] SMALL_INTEGERS;
    private static int INT_CACHE_OFFSET = 128, INT_CACHE_LEN = 256;

    static {
        SMALL_INTEGERS = new Integer[INT_CACHE_LEN];
        for (int i = 0; i < SMALL_INTEGERS.length; i++) {
            SMALL_INTEGERS[i] = new Integer(i - INT_CACHE_OFFSET);
        }
    }*/
}
