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

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static helper class to make bytecode generation easier and act as a facade over the Invoker
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InvokerHelper {
    public static final Object[] EMPTY_ARGS = {
    };

    private static final Object[] EMPTY_MAIN_ARGS = new Object[]{new String[0]};

    private static final Invoker singleton = new Invoker();

    private static final Integer ZERO = new Integer(0);
    private static final Integer MINUS_ONE = new Integer(-1);
    private static final Integer ONE = new Integer(1);

    public static MetaClass getMetaClass(Object object) {
        return getInstance().getMetaClass(object);
    }

    public static void removeClass(Class clazz) {
        getInstance().removeMetaClass(clazz);
        Introspector.flushFromCaches(clazz);
    }

    public static Invoker getInstance() {
        return singleton;
    }

    public static Object invokeNoArgumentsMethod(Object object, String methodName) {
        return getInstance().invokeMethod(object, methodName, EMPTY_ARGS);
    }

    public static Object invokeMethod(Object object, String methodName, Object arguments) {
        return getInstance().invokeMethod(object, methodName, arguments);
    }

    public static Object invokeSuperMethod(Object object, String methodName, Object arguments) {
        return getInstance().invokeSuperMethod(object, methodName, arguments);
    }

    public static Object invokeMethodSafe(Object object, String methodName, Object arguments) {
        if (object != null) {
            return getInstance().invokeMethod(object, methodName, arguments);
        }
        return null;
    }

    public static Object invokeStaticMethod(String type, String methodName, Object arguments) {
        return getInstance().invokeStaticMethod(type, methodName, arguments);
    }

    public static Object invokeStaticNoArgumentsMethod(String type, String methodName) {
        return getInstance().invokeStaticMethod(type, methodName, EMPTY_ARGS);
    }

    public static Object invokeConstructor(String type, Object arguments) {
        return getInstance().invokeConstructor(type, arguments);
    }

    public static Object invokeConstructorOf(Class type, Object arguments) {
        return getInstance().invokeConstructorOf(type, arguments);
    }

    public static Object invokeNoArgumentsConstructorOf(Class type) {
        return getInstance().invokeConstructorOf(type, EMPTY_ARGS);
    }

    public static Object invokeClosure(Object closure, Object arguments) {
        return getInstance().invokeMethod(closure, "doCall", arguments);
    }

    public static Iterator asIterator(Object collection) {
        return getInstance().asIterator(collection);
    }

    public static Collection asCollection(Object collection) {
        return getInstance().asCollection(collection);
    }

    public static List asList(Object args) {
        return getInstance().asList(args);
    }

    public static String toString(Object arguments) {
        if (arguments instanceof Object[])
            return getInstance().toArrayString((Object[])arguments);
        else if (arguments instanceof Collection)
            return getInstance().toListString((Collection)arguments);
        else if (arguments instanceof Map)
            return getInstance().toMapString((Map)arguments);
        else
            return getInstance().toString(arguments);
    }

    public static String toTypeString(Object[] arguments) {
        return getInstance().toTypeString(arguments);
    }

    public static String toMapString(Map arg) {
        return getInstance().toMapString(arg);
    }

    public static String toListString(Collection arg) {
        return getInstance().toListString(arg);
    }

    public static String toArrayString(Object[] arguments) {
        return getInstance().toArrayString(arguments);
    }

    public static String inspect(Object self) {
        return getInstance().inspect(self);
    }

    public static Object getAttribute(Object object, String attribute) {
        return getInstance().getAttribute(object, attribute);
    }

    public static void setAttribute(Object object, String attribute, Object newValue) {
        getInstance().setAttribute(object, attribute, newValue);
    }

    public static Object getProperty(Object object, String property) {
        return getInstance().getProperty(object, property);
    }

    public static Object getPropertySafe(Object object, String property) {
        if (object != null) {
            return getInstance().getProperty(object, property);
        }
        return null;
    }

    public static void setProperty(Object object, String property, Object newValue) {
        getInstance().setProperty(object, property, newValue);
    }

    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    public static void setProperty2(Object newValue, Object object, String property) {
        getInstance().setProperty(object, property, newValue);
    }


    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    public static void setGroovyObjectProperty(Object newValue, GroovyObject object, String property) {
        object.setProperty(property, newValue);
    }

    public static Object getGroovyObjectProperty(GroovyObject object, String property) {
        return object.getProperty(property);
    }


    /**
     * This is so we don't have to reorder the stack when we call this method.
     * At some point a better name might be in order.
     */
    public static void setPropertySafe2(Object newValue, Object object, String property) {
        if (object != null) {
            setProperty2(newValue, object, property);
        }
    }

    /**
     * Returns the method pointer for the given object name
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        return getInstance().getMethodPointer(object, methodName);
    }

    /**
     * Provides a hook for type coercion of the given object to the required type
     *
     * @param type   of object to convert the given object to
     * @param object the object to be converted
     * @return the original object or a new converted value
     */
    public static Object asType(Object object, Class type) {
        return getInstance().asType(object, type);
    }

    public static boolean asBool(Object object) {
        return getInstance().asBool(object);
    }

    public static boolean notObject(Object object) {
        return !asBool(object);
    }

    public static boolean notBoolean(boolean bool) {
        return !bool;
    }

    public static Object negate(Object value) {
        if (value instanceof Integer) {
            Integer number = (Integer) value;
            return integerValue(-number.intValue());
        }
        else if (value instanceof Long) {
            Long number = (Long) value;
            return new Long(-number.longValue());
        }
        else if (value instanceof BigInteger) {
            return ((BigInteger) value).negate();
        }
        else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).negate();
        }
        else if (value instanceof Double) {
            Double number = (Double) value;
            return new Double(-number.doubleValue());
        }
        else if (value instanceof Float) {
            Float number = (Float) value;
            return new Float(-number.floatValue());
        }
        else if (value instanceof ArrayList) {
            // value is an list.
            ArrayList newlist = new ArrayList();
            Iterator it = ((ArrayList) value).iterator();
            for (; it.hasNext();) {
                newlist.add(negate(it.next()));
            }
            return newlist;
        }
        else {
            throw new GroovyRuntimeException("Cannot negate type " + value.getClass().getName() + ", value " + value);
        }
    }

    public static Object bitNegate(Object value) {
        if (value instanceof Integer) {
            Integer number = (Integer) value;
            return integerValue(~number.intValue());
        }
        else if (value instanceof Long) {
            Long number = (Long) value;
            return new Long(~number.longValue());
        }
        else if (value instanceof BigInteger) {
            return ((BigInteger) value).not();
        }
        else if (value instanceof String) {
            // value is a regular expression.
            return getInstance().regexPattern(value);
        }
        else if (value instanceof GString) {
            // value is a regular expression.
            return getInstance().regexPattern(value.toString());
        }
        else if (value instanceof ArrayList) {
            // value is an list.
            ArrayList newlist = new ArrayList();
            Iterator it = ((ArrayList) value).iterator();
            for (; it.hasNext();) {
                newlist.add(bitNegate(it.next()));
            }
            return newlist;
        }
        else {
            throw new BitwiseNegateEvaluatingException("Cannot bitwise negate type " + value.getClass().getName() + ", value " + value);
        }
    }

    public static boolean isCase(Object switchValue, Object caseExpression) {
        return asBool(invokeMethod(caseExpression, "isCase", new Object[]{switchValue}));
    }

    public static boolean compareIdentical(Object left, Object right) {
        return left == right;
    }

    public static boolean compareEqual(Object left, Object right) {
        return getInstance().objectsEqual(left, right);
    }

    public static Matcher findRegex(Object left, Object right) {
        return getInstance().objectFindRegex(left, right);
    }

    public static boolean matchRegex(Object left, Object right) {
        return getInstance().objectMatchRegex(left, right);
    }

    public static Pattern regexPattern(Object regex) {
        return getInstance().regexPattern(regex);
    }

    public static boolean compareNotEqual(Object left, Object right) {
        return !getInstance().objectsEqual(left, right);
    }

    public static boolean compareLessThan(Object left, Object right) {
        return getInstance().compareTo(left, right) < 0;
    }

    public static boolean compareLessThanEqual(Object left, Object right) {
        return getInstance().compareTo(left, right) <= 0;
    }

    public static boolean compareGreaterThan(Object left, Object right) {
        return getInstance().compareTo(left, right) > 0;
    }

    public static boolean compareGreaterThanEqual(Object left, Object right) {
        return getInstance().compareTo(left, right) >= 0;
    }

    public static Integer compareTo(Object left, Object right) {
        int answer = getInstance().compareTo(left, right);
        if (answer == 0) {
            return ZERO;
        }
        else {
            return answer > 0 ? ONE : MINUS_ONE;
        }
    }

    public static Tuple createTuple(Object[] array) {
        return new Tuple(array);
    }

    public static SpreadList spreadList(Object value) {
        if (value instanceof List) {
            // value is a list.
            Object[] values = new Object[((List) value).size()];
            int index = 0;
            Iterator it = ((List) value).iterator();
            for (; it.hasNext();) {
                values[index++] = it.next();
            }
            return new SpreadList(values);
        }
        else {
            throw new SpreadListEvaluatingException("Cannot spread the type " + value.getClass().getName() + ", value " + value);
        }
    }

    public static SpreadMap spreadMap(Object value) {
        if (value instanceof Map) {
            Object[] values = new Object[((Map) value).keySet().size() * 2];
            int index = 0;
            Iterator it = ((Map) value).keySet().iterator();
            for (; it.hasNext();) {
                Object key = it.next();
                values[index++] = key;
                values[index++] = ((Map) value).get(key);
            }
            return new SpreadMap(values);
        }
        else {
            throw new SpreadMapEvaluatingException("Cannot spread the map " + value.getClass().getName() + ", value " + value);
        }
    }

    public static List createList(Object[] values) {
        ArrayList answer = new ArrayList(values.length);
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof SpreadList) {
                SpreadList slist = (SpreadList) values[i];
                for (int j = 0; j < slist.size(); j++) {
                    answer.add(slist.get(j));
                }
            }
            else {
                answer.add(values[i]);
            }
        }
        return answer;
    }

    public static Map createMap(Object[] values) {
        Map answer = new HashMap(values.length / 2);
        int i = 0;
        while (i < values.length - 1) {
            if ((values[i] instanceof SpreadMap) && (values[i+1] instanceof Map)) {
                Map smap = (Map) values[i+1];
                Iterator iter = smap.keySet().iterator();
                for (; iter.hasNext(); ) {
                    Object key = (Object) iter.next();
                    answer.put(key, smap.get(key));
                }
                i+=2;
            }
            else {
                answer.put(values[i++], values[i++]);
            }
        }
        return answer;
    }

    public static List createRange(Object from, Object to, boolean inclusive) {
        if (!inclusive) {
            if (compareEqual(from,to)){
                return new EmptyRange((Comparable)from);
            }
            if (compareGreaterThan(from, to)) {
                to = invokeMethod(to, "next", EMPTY_ARGS);
            }
            else {
                to = invokeMethod(to, "previous", EMPTY_ARGS);
            }
        }
        if (from instanceof Integer && to instanceof Integer) {
            return new IntRange(asInt(from), asInt(to));
        }
        else {
            return new ObjectRange((Comparable) from, (Comparable) to);
        }
    }

    public static int asInt(Object value) {
        return getInstance().asInt(value);
    }

    public static void assertFailed(Object expression, Object message) {
        if (message == null || "".equals(message)) {
            throw new AssertionError("Expression: " + expression);
        }
        else {
            throw new AssertionError("" + message + ". Expression: " + expression);
        }
    }

    public static Object runScript(Class scriptClass, String[] args) {
        Binding context = new Binding(args);
        Script script = createScript(scriptClass, context);
        return invokeMethod(script, "run", EMPTY_ARGS);
    }

    public static Script createScript(Class scriptClass, Binding context) {
        // for empty scripts
        if (scriptClass == null) {
            return new Script() {
                public Object run() {
                    return null;
                }
            };
        }
        try {
            final GroovyObject object = (GroovyObject) scriptClass.newInstance();
            Script script = null;
            if (object instanceof Script) {
                script = (Script) object;
            }
            else {
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
        }
        catch (Exception e) {
            throw new GroovyRuntimeException("Failed to create Script instance for class: " + scriptClass + ". Reason: " + e,
                    e);
        }
    }

    /**
     * Sets the properties on the given object
     *
     * @param object
     * @param map
     */
    public static void setProperties(Object object, Map map) {
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
    }

    /**
     * Allows conversion of arrays into a mutable List
     *
     * @return the array as a List
     */
    protected static List primitiveArrayToList(Object array) {
        int size = Array.getLength(array);
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }

    /**
     * Writes the given object to the given stream
     */
    public static void write(Writer out, Object object) throws IOException {
        if (object instanceof String) {
            out.write((String) object);
        }
        else if (object instanceof Object[]) {
            out.write(toArrayString((Object[]) object));
        }
        else if (object instanceof Map) {
            out.write(toMapString((Map) object));
        }
        else if (object instanceof Collection) {
            out.write(toListString((Collection) object));
        }
        else if (object instanceof Writable) {
            Writable writable = (Writable) object;
            writable.writeTo(out);
        }
        else if (object instanceof InputStream || object instanceof Reader) {
            // Copy stream to stream
            Reader reader;
            if (object instanceof InputStream) {
                reader = new InputStreamReader((InputStream) object);
            }
            else {
                reader = (Reader) object;
            }
            char[] chars = new char[8192];
            int i;
            while ((i = reader.read(chars)) != -1) {
                out.write(chars, 0, i);
            }
            reader.close();
        }
        else {
            out.write(toString(object));
        }
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

    public static byte byteUnbox(Object value) {
        Number n = (Number) asType(value, Byte.class);
        return n.byteValue();
    }

    public static char charUnbox(Object value) {
        Character n = (Character) asType(value, Character.class);
        return n.charValue();
    }

    public static short shortUnbox(Object value) {
        Number n = (Number) asType(value, Short.class);
        return n.shortValue();
    }

    public static int intUnbox(Object value) {
        Number n = (Number) asType(value, Integer.class);
        return n.intValue();
    }

    public static boolean booleanUnbox(Object value) {
        Boolean n = (Boolean) asType(value, Boolean.class);
        return n.booleanValue();
    }

    public static long longUnbox(Object value) {
        Number n = (Number) asType(value, Long.class);
        return n.longValue();
    }

    public static float floatUnbox(Object value) {
        Number n = (Number) asType(value, Float.class);
        return n.floatValue();
    }

    public static double doubleUnbox(Object value) {
        Number n = (Number) asType(value, Double.class);
        return n.doubleValue();
    }

    /**
     * @param a    array of primitives
     * @param type component type of the array
     * @return
     */
    public static Object[] convertPrimitiveArray(Object a, Class type) {
//        System.out.println("a.getClass() = " + a.getClass());
        Object[] ans = null;
        String elemType = type.getName();
        if (elemType.equals("int")) {
            // conservative coding
            if (a.getClass().getName().equals("[Ljava.lang.Integer;")) {
                ans = (Integer[]) a;
            }
            else {
                int[] ia = (int[]) a;
                ans = new Integer[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    int e = ia[i];
                    ans[i] = integerValue(e);
                }
            }
        }
        else if (elemType.equals("char")) {
            if (a.getClass().getName().equals("[Ljava.lang.Character;")) {
                ans = (Character[]) a;
            }
            else {
                char[] ia = (char[]) a;
                ans = new Character[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    char e = ia[i];
                    ans[i] = new Character(e);
                }
            }
        }
        else if (elemType.equals("boolean")) {
            if (a.getClass().getName().equals("[Ljava.lang.Boolean;")) {
                ans = (Boolean[]) a;
            }
            else {
                boolean[] ia = (boolean[]) a;
                ans = new Boolean[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    boolean e = ia[i];
                    ans[i] = new Boolean(e);
                }
            }
        }
        else if (elemType.equals("byte")) {
            if (a.getClass().getName().equals("[Ljava.lang.Byte;")) {
                ans = (Byte[]) a;
            }
            else {
                byte[] ia = (byte[]) a;
                ans = new Byte[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    byte e = ia[i];
                    ans[i] = new Byte(e);
                }
            }
        }
        else if (elemType.equals("short")) {
            if (a.getClass().getName().equals("[Ljava.lang.Short;")) {
                ans = (Short[]) a;
            }
            else {
                short[] ia = (short[]) a;
                ans = new Short[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    short e = ia[i];
                    ans[i] = new Short(e);
                }
            }
        }
        else if (elemType.equals("float")) {
            if (a.getClass().getName().equals("[Ljava.lang.Float;")) {
                ans = (Float[]) a;
            }
            else {
                float[] ia = (float[]) a;
                ans = new Float[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    float e = ia[i];
                    ans[i] = new Float(e);
                }
            }
        }
        else if (elemType.equals("long")) {
            if (a.getClass().getName().equals("[Ljava.lang.Long;")) {
                ans = (Long[]) a;
            }
            else {
                long[] ia = (long[]) a;
                ans = new Long[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    long e = ia[i];
                    ans[i] = new Long(e);
                }
            }
        }
        else if (elemType.equals("double")) {
            if (a.getClass().getName().equals("[Ljava.lang.Double;")) {
                ans = (Double[]) a;
            }
            else {
                double[] ia = (double[]) a;
                ans = new Double[ia.length];
                for (int i = 0; i < ia.length; i++) {
                    double e = ia[i];
                    ans[i] = new Double(e);
                }
            }
        }
        return ans;
    }

    public static int[] convertToIntArray(Object a) {
        int[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[I")) {
            ans = (int[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new int[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
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
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new boolean[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
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
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new byte[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] != null) {
                    ans[i] = ((Number) ia[i]).byteValue();
                }
            }
        }
        return ans;
    }

    public static short[] convertToShortArray(Object a) {
        short[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[S")) {
            ans = (short[]) a;
        }
        else {
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
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new char[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
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
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new long[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
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
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new float[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
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
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new double[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = ((Number) ia[i]).doubleValue();
            }
        }
        return ans;
    }

    public static Object convertToPrimitiveArray(Object a, Class type) {
        if (type == Byte.TYPE) {
            return convertToByteArray(a);
        }
        if (type == Boolean.TYPE) {
            return convertToBooleanArray(a);
        }
        if (type == Short.TYPE) {
            return convertToShortArray(a);
        }
        if (type == Character.TYPE) {
            return convertToCharArray(a);
        }
        if (type == Integer.TYPE) {
            return convertToIntArray(a);
        }
        if (type == Long.TYPE) {
            return convertToLongArray(a);
        }
        if (type == Float.TYPE) {
            return convertToFloatArray(a);
        }
        if (type == Double.TYPE) {
            return convertToDoubleArray(a);
        }
        else {
            return a;
        }
    }

    /**
     * get the Integer object from an int. Cached version is used for small ints.
     *
     * @param v
     * @return
     */
    public static Integer integerValue(int v) {
        int index = v + INT_CACHE_OFFSET;
        if (index >= 0 && index < INT_CACHE_LEN) {
            return SMALL_INTEGERS[index];
        }
        else {
            return new Integer(v);
        }
    }

    private static Integer[] SMALL_INTEGERS;
    private static int INT_CACHE_OFFSET = 128, INT_CACHE_LEN = 256;

    static {
        SMALL_INTEGERS = new Integer[INT_CACHE_LEN];
        for (int i = 0; i < SMALL_INTEGERS.length; i++) {
            SMALL_INTEGERS[i] = new Integer(i - INT_CACHE_OFFSET);
        }
    }
}
