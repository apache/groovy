/*
 * AutoBoxing.java created on 13.09.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.codehaus.groovy.runtime.typehandling;

import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.IteratorClosureAdapter;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.RegexSupport;

public class DefaultTypeTransformation {
    
    protected static final Object[] EMPTY_ARGUMENTS = {};
    
    //  --------------------------------------------------------
    //                  unboxing methods
    //  --------------------------------------------------------       
    
    public static byte byteUnbox(Object value) {
        Number n = castToNumber(value);
        return n.byteValue();
    }

    public static char charUnbox(Object value) {
        return castToChar(value);
    }

    public static short shortUnbox(Object value) {
        Number n = castToNumber(value);
        return n.shortValue();
    }

    public static int intUnbox(Object value) {
        Number n = castToNumber(value);
        return n.intValue();
    }

    public static boolean booleanUnbox(Object value) {
        return castToBoolean(value);
    }

    public static long longUnbox(Object value) {
        Number n = castToNumber(value);
        return n.longValue();
    }

    public static float floatUnbox(Object value) {
        Number n = castToNumber(value);
        return n.floatValue();
    }

    public static double doubleUnbox(Object value) {
        Number n = castToNumber(value);
        return n.doubleValue();
    } 

    //  --------------------------------------------------------
    //                  boxing methods
    //  --------------------------------------------------------       
    
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
        return IntegerCache.integerValue(value);
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
    
    public static Number castToNumber(Object object) {
        if (object instanceof Number) return (Number) object;
        if (object instanceof Character) {
            return new Integer(((Character) object).charValue());
        } else if (object instanceof String) {
            String c = (String) object;
            if (c.length() == 1) {
                return new Integer(c.charAt(0));
            }
            else {
                throw new ClassCastException("Cannot cast: '" + c + "' to an Integer");
            }
        }
        throw new ClassCastException("Cannot cast: '" + object + "' to an Number");
    }
    
    public static boolean castToBoolean(Object object) {
        if (object instanceof Boolean) {
            Boolean booleanValue = (Boolean) object;
            return booleanValue.booleanValue();
        }
        else if (object instanceof Matcher) {
            Matcher matcher = (Matcher) object;
            RegexSupport.setLastMatcher(matcher);
            return matcher.find();
        }
        else if (object instanceof Collection) {
            Collection collection = (Collection) object;
            return !collection.isEmpty();
        }
        else if (object instanceof Map) {
            Map map = (Map) object;
            return !map.isEmpty();
        }
        else if (object instanceof String || object instanceof GString) {
            String string =  object.toString();
            return string.length() > 0;
        } 
        else if (object instanceof Character) {
            Character c = (Character) object;
            return c.charValue() != 0;
        }
        else if (object instanceof Number) {
            Number n = (Number) object;
            return n.doubleValue() != 0;
        }
        else {
            return object != null;
        }
    }
    
    public static char castToChar(Object object) {
        if (object instanceof Character) {
            return ((Character) object).charValue();            
        } else if (object instanceof Number) {
            Number value = (Number) object;
            return (char) value.intValue();
        } else {
            String text = object.toString();
            if (text.length() == 1) {
                return text.charAt(0);
            }
            else {
                throw new ClassCastException("Cannot cast: " + text + " to a Character");
            }
        }
    }
    
    public static Object castToType(Object object, Class type) {
        if (object == null) {
            return null;
        }
        
        if (type == object.getClass()) return object;
        
        // TODO we should move these methods to groovy method, like g$asType() so that
        // we can use operator overloading to customize on a per-type basis
        if (type.isArray()) {
            return asArray(object, type);

        }
        if (type.isInstance(object)) {
            return object;
        }
        if (type.isAssignableFrom(Collection.class)) {
            if (object.getClass().isArray()) {
                // lets call the collections constructor
                // passing in the list wrapper
                Collection answer = null;
                try {
                    answer = (Collection) type.newInstance();
                }
                catch (Exception e) {
                    throw new ClassCastException("Could not instantiate instance of: " + type.getName() + ". Reason: " + e);
                }

                // we cannot just wrap in a List as we support primitive type arrays
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(object, i);
                    answer.add(element);
                }
                return answer;
            }
        }
        if (type == String.class) {
            return object.toString();
        } else if (type == Character.class) {
            return box(castToChar(object));
        } else if (type == Boolean.class) {
            return box(castToBoolean(object));
        } else if (Number.class.isAssignableFrom(type)) {
            Number n = castToNumber(object);
            if (type == Byte.class) {
                return new Byte(n.byteValue());
            } else if (type == Character.class) {
                return new Character((char) n.intValue());
            } else if (type == Short.class) {
                return new Short(n.shortValue());
            } else if (type == Integer.class) {
                return new Integer(n.intValue());
            } else if (type == Long.class) {
                return new Long(n.longValue());
            } else if (type == Float.class) {
                return new Float(n.floatValue());
            } else if (type == Double.class) {
                Double answer = new Double(n.doubleValue());
                //throw a runtime exception if conversion would be out-of-range for the type.
                if (!(n instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                        || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                    throw new GroovyRuntimeException("Automatic coercion of " + n.getClass().getName()
                            + " value " + n + " to double failed.  Value is out of range.");
                }
                return answer;
            } else if (type == BigDecimal.class) {
                return new BigDecimal(n.toString());
            } else if (type == BigInteger.class) {
                return new BigInteger(n.toString());
            }
        } else if (type.isPrimitive()) {
            if (type == byte.class) {
                return box(byteUnbox(object));
            } else if (type == char.class) {
                return box(charUnbox(object));
            } else if (type == short.class) {
                return box(shortUnbox(object));
            } else if (type == int.class) {
                return box(intUnbox(object));
            } else if (type == long.class) {
                return box(longUnbox(object));
            } else if (type == float.class) {
                return box(floatUnbox(object));
            } else if (type == double.class) {
                Double answer = new Double(doubleUnbox(object));
                //throw a runtime exception if conversion would be out-of-range for the type.
                if (!(object instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                        || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                    throw new GroovyRuntimeException("Automatic coercion of " + object.getClass().getName()
                            + " value " + object + " to double failed.  Value is out of range.");
                }
                return answer;
            }
        }
        Object[] args = null;
        if (object instanceof Collection) {
            Collection list = (Collection) object;
            args = list.toArray();
        } else if (object instanceof Object[]) {
            args = (Object[]) object;
        }
        if (args != null) {
            // lets try invoke the constructor with the list as arguments
            // such as for creating a Dimension, Point, Color etc.
            try {
                return InvokerHelper.invokeConstructorOf(type, args);
            } catch (InvokerInvocationException iie){
                throw iie;
            } catch (Exception e) {
                // lets ignore exception and return the original object
                // as the caller has more context to be able to throw a more
                // meaningful exception
            }
        }
        throw new ClassCastException("unable to cast "+object.getClass()+" to "+type);
    }
    
    public static Object asArray(Object object, Class type) {
        Collection list = asCollection(object);
        int size = list.size();
        Class elementType = type.getComponentType();
        Object array = Array.newInstance(elementType, size);
        int idx = 0;

        if (boolean.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setBoolean(array, idx, booleanUnbox(element));
            }
        }
        else if (byte.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setByte(array, idx, byteUnbox(element));
            }
        }
        else if (char.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setChar(array, idx, charUnbox(element));
            }
        }
        else if (double.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setDouble(array, idx, doubleUnbox(element));
            }
        }
        else if (float.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setFloat(array, idx, floatUnbox(element));
            }
        }
        else if (int.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setInt(array, idx, intUnbox(element));
            }
        }
        else if (long.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setLong(array, idx, longUnbox(element));
            }
        }
        else if (short.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setShort(array, idx, shortUnbox(element));
            }
        }
        else {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Object coercedElement = castToType(element, elementType);
                Array.set(array, idx, coercedElement);
            }
        }
        return array;
    }
    
    public static Collection asCollection(Object value) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        else if (value instanceof Collection) {
            return (Collection) value;
        }
        else if (value instanceof Map) {
            Map map = (Map) value;
            return map.entrySet();
        }
        else if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().isPrimitive()) {
                return primitiveArrayToList(value);
            }
            return Arrays.asList((Object[]) value);
        }
        else if (value instanceof MethodClosure) {
            MethodClosure method = (MethodClosure) value;
            IteratorClosureAdapter adapter = new IteratorClosureAdapter(method.getDelegate());
            method.call(adapter);
            return adapter.asList();
        }
        else if (value instanceof String) {
            return DefaultGroovyMethods.toList((String) value);
        }
        else if (value instanceof File) {
            try {
                return DefaultGroovyMethods.readLines((File) value);
            }
            catch (IOException e) {
                throw new GroovyRuntimeException("Error reading file: " + value, e);
            }
        }
        else {
            // lets assume its a collection of 1
            return Collections.singletonList(value);
        }
    }
    
    /**
     * Allows conversion of arrays into a mutable List
     *
     * @return the array as a List
     */
    public static List primitiveArrayToList(Object array) {
        int size = Array.getLength(array);
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }
    
    /**
     * Compares the two objects handling nulls gracefully and performing numeric type coercion if required
     */
    public static int compareTo(Object left, Object right) {
        //System.out.println("Comparing: " + left + " to: " + right);
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        else if (right == null) {
            return 1;
        }
        if (left instanceof Comparable) {
            if (left instanceof Number) {
                if (isValidCharacterString(right)) {
                    return castToChar(left) - castToChar(right);
                }
                return DefaultGroovyMethods.compareTo((Number) left, castToNumber(right));
            }
            else if (left instanceof Character) {
                if (isValidCharacterString(right)) {
                    return castToChar(left) - castToChar(right);
                }
                else if (right instanceof Number) {
                    return castToChar(left) - castToChar(right);
                }
            }
            else if (right instanceof Number) {
                if (isValidCharacterString(left)) {
                    return castToChar(left) - castToChar(right);
                }
                return DefaultGroovyMethods.compareTo(castToNumber(left), (Number) right);
            }
            else if (left instanceof String && right instanceof Character) {
                return ((String) left).compareTo(right.toString());
            }
            else if (left instanceof String && right instanceof GString) {
                return ((String) left).compareTo(right.toString());
            }
            Comparable comparable = (Comparable) left;
            return comparable.compareTo(right);
        }

        if (left.getClass().isArray()) {
            Collection leftList = asCollection(left);
            if (right.getClass().isArray()) {
                right = asCollection(right);
            }
            return ((Comparable) leftList).compareTo(right);
        }
        /** todo we might wanna do some type conversion here */
        throw new GroovyRuntimeException("Cannot compare values: " + left + " and " + right);
    }
    
    public static boolean compareEqual(Object left, Object right) {
        if (left == right) return true;
        if (left == null || right == null) return false;
        if (left instanceof Comparable) {
            return compareTo(left, right) == 0;
        } else if (left instanceof List && right instanceof List) {
            return DefaultGroovyMethods.equals((List) left, (List) right);
        } else {
            return left.equals(right);
        }
    }
    
    /**
     * @return true if the given value is a valid character string (i.e. has length of 1)
     */
    private static boolean isValidCharacterString(Object value) {
        if (value instanceof String) {
            String s = (String) value;
            if (s.length() == 1) {
                return true;
            }
        }
        return false;
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
                    ans[i] = IntegerCache.integerValue(e);
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
    
}
