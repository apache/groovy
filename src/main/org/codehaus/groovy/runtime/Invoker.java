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

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.Range;
import groovy.lang.Tuple;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A helper class to invoke methods or extract properties on arbitrary Java objects dynamically
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Invoker {

    protected static final Object[] EMPTY_ARGUMENTS = {
    };
    protected static final Class[] EMPTY_TYPES = {
    };

    private MetaClassRegistry metaRegistry = new MetaClassRegistry();


    
    public MetaClass getMetaClass(Object object) {
        return metaRegistry.getMetaClass(object.getClass());
    }

    /**
     * Invokes the given method on the object. 
     * 
     * @param object
     * @param methodName
     * @param arguments
     * @return
     */
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        /*
        System
            .out
            .println(
                "Invoker - Invoking method on object: "
                    + object
                    + " method: "
                    + methodName
                    + " arguments: "
                    + InvokerHelper.toString(arguments));
                    
                    */

        if (object == null) {
            throw new NullPointerException("Cannot invoke method: " + methodName + " on null object");
        }

        if (object instanceof GroovyObject) {
            GroovyObject groovy = (GroovyObject) object;
            return groovy.invokeMethod(methodName, arguments);
        }
        else {
            if (object instanceof Class) {
                Class theClass = (Class) object;

                MetaClass metaClass = metaRegistry.getMetaClass(theClass);
                return metaClass.invokeStaticMethod(object, methodName, asArray(arguments));
            }
            else {
                Class theClass = object.getClass();

                MetaClass metaClass = metaRegistry.getMetaClass(theClass);
                return metaClass.invokeMethod(object, methodName, asArray(arguments));
            }
        }
    }

    public Object invokeSuperMethod(Object object, String methodName, Object arguments) {
        if (object == null) {
            throw new NullPointerException("Cannot invoke method: " + methodName + " on null object");
        }

        Class theClass = object.getClass();

        MetaClass metaClass = metaRegistry.getMetaClass(theClass.getSuperclass());
        return metaClass.invokeMethod(object, methodName, asArray(arguments));
    }

    public Object invokeStaticMethod(String type, String method, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(loadClass(type));
        List argumentList = asList(arguments);
        return metaClass.invokeStaticMethod(null, method, asArray(arguments));
    }

    public Object invokeConstructor(String type, Object arguments) {
        //System.out.println("Invoking constructor of type: " + type);
        return invokeConstructorOf(loadClass(type), arguments);
    }

    public Object invokeConstructorOf(Class type, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(type);
        return metaClass.invokeConstructor(asArray(arguments));
    }

    /**
     * Converts the given object into an array; if its an array then just
     * cast otherwise wrap it in an array
     */
    public Object[] asArray(Object arguments) {
        if (arguments == null) {
            return EMPTY_ARGUMENTS;
        }
        if (arguments instanceof Tuple) {
            Tuple tuple = (Tuple) arguments;
            return tuple.toArray();
        }
        if (arguments instanceof Object[]) {
            return (Object[]) arguments;
        }
        else {
            return new Object[] { arguments };
        }
    }

    public List asList(Object value) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        else if (value instanceof List) {
            return (List) value;
        }
        else if (value.getClass().isArray()) {
            return Arrays.asList((Object[]) value);
        }
        else if (value instanceof Enumeration) {
            List answer = new ArrayList();
            for (Enumeration e = (Enumeration) value; e.hasMoreElements();) {
                answer.add(e.nextElement());
            }
            return answer;
        }
        else {
            // lets assume its a collection of 1
            return Collections.singletonList(value);
        }
    }

    /**
     * @param arguments
     * @return
     */
    public Collection asCollection(Object value) {
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
                return InvokerHelper.primitiveArrayToList(value);
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

    public Iterator asIterator(Object value) {
        if (value == null) {
            Collections.EMPTY_LIST.iterator();
        }
        if (value instanceof Iterator) {
            return (Iterator) value;
        }
        if (value instanceof NodeList) {
            final NodeList nodeList = (NodeList) value;
            return new Iterator() {
                private int current = 0;
                public boolean hasNext() {
                    	return current < nodeList.getLength();
                }
                public Object next() { 
                    	Node node = nodeList.item(current++);
                    	return node;
                }
                public void remove() {
                    throw new UnsupportedOperationException("Cannot remove() from an Enumeration");
                }
            };
        }
        else if (value instanceof Enumeration) {
            final Enumeration enumeration = (Enumeration) value;
            return new Iterator() {
                private Object last;

                public boolean hasNext() {
                    return enumeration.hasMoreElements();
                }

                public Object next() {
                    last = enumeration.nextElement();
                    return last;
                }

                public void remove() {
                    throw new UnsupportedOperationException("Cannot remove() from an Enumeration");
                }
            };
        }
        else if (value instanceof Matcher) {
            final Matcher matcher = (Matcher) value;
            return new Iterator() {
                private boolean found = false;
                private boolean done = false;
                public boolean hasNext() {
                    if (done)
                        return false;
                    if (!found) {
                        found = matcher.find();
                        if (!found)
                            done = true;
                    }
                    return found;
                }
                public Object next() {
                    if (!found) {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                    }
                    found = false;
                    return matcher.group();
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        else {
            try {
                // lets try see if there's an iterator() method
                final Method method = value.getClass().getMethod("iterator", EMPTY_TYPES);
                 
                if (method != null) {                  
	            		AccessController.doPrivileged(new PrivilegedAction() {
	    					public Object run() {
	    						method.setAccessible(true);
	    						return null;
	    					}
	    				});
            		
                    return (Iterator) method.invoke(value, EMPTY_ARGUMENTS);
                }
            }
            catch (Exception e) {
            		//  ignore
            }
        }
        return asCollection(value).iterator();
    }

    /**
     * @return true if the two objects are null or the objects are equal
     */
    public boolean objectsEqual(Object left, Object right) {
        if (left == right) {
            return true;
        }
        if (left != null) {
            if (right == null) {
                return false;
            }
            if (left instanceof Comparable) {
                return compareTo(left, right) == 0;
            }
            else {
                return left.equals(right);
            }
        }
        return false;
    }

    public String inspect(Object self) {
        return format(self, true);
    }

    /**
     * Compares the two objects handling nulls gracefully and performing numeric type coercion if required
     */
    public int compareTo(Object left, Object right) {
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
                    return asCharacter((Number) left).compareTo(asCharacter((String) right));
                }
                return DefaultGroovyMethods.compareTo((Number) left, asNumber(right));
            }
            else if (left instanceof Character) {
                if (isValidCharacterString(right)) {
                    return ((Character) left).compareTo(asCharacter((String) right));
                }
                else if (right instanceof Number) {
                    return ((Character) left).compareTo(asCharacter((Number) right));
                }
            }
            else if (right instanceof Number) {
                if (isValidCharacterString(left)) {
                    return asCharacter((String) left).compareTo(asCharacter((Number) right));
                }
                return DefaultGroovyMethods.compareTo(asNumber(left), (Number) right);
            }
            else if (left instanceof String && right instanceof Character) {
                return ((String)left).compareTo(right.toString());
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

    /**
     * A helper method to provide some better toString() behaviour such as turning arrays
     * into tuples
     */
    public String toString(Object arguments) {
        return format(arguments, false);
    }

    /**
	 * A helper method to format the arguments types as a comma-separated list
	 */
	public String toTypeString(Object[] arguments) {
		if (arguments == null) {
			return "null";
		}
	    StringBuffer argBuf = new StringBuffer();
	    for (int i = 0; i < arguments.length; i++) {
	    	if (i>0)
                argBuf.append(", ");
			argBuf.append(arguments[i] != null ? arguments[i].getClass().getName() : "null");
		}
	    return argBuf.toString();
	}
    
    protected String format(Object arguments, boolean verbose) {
        if (arguments == null) {
            return "null";
        }
        else if (arguments.getClass().isArray()) {
            return format(asCollection(arguments), verbose);
        }
        else if (arguments instanceof Range) {
            Range range = (Range) arguments;
            if (verbose) {
                return range.inspect();
            }
            else {
                return range.toString();
            }
        }
        else if (arguments instanceof List) {
            List list = (List) arguments;
            StringBuffer buffer = new StringBuffer("[");
            boolean first = true;
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                if (first) {
                    first = false;
                }
                else {
                    buffer.append(", ");
                }
                buffer.append(format(iter.next(), verbose));
            }
            buffer.append("]");
            return buffer.toString();
        }
        else if (arguments instanceof Map) {
            Map map = (Map) arguments;
            if (map.isEmpty()) {
                return "[:]";
            }
            StringBuffer buffer = new StringBuffer("[");
            boolean first = true;
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                if (first) {
                    first = false;
                }
                else {
                    buffer.append(", ");
                }
                Map.Entry entry = (Map.Entry) iter.next();
                buffer.append(format(entry.getKey(), verbose));
                buffer.append(":");
                buffer.append(format(entry.getValue(), verbose));
            }
            buffer.append("]");
            return buffer.toString();
        }
        else if (arguments instanceof Element) {
            Element node = (Element) arguments;
            OutputFormat format = new OutputFormat(node.getOwnerDocument());
            format.setOmitXMLDeclaration(true);
            format.setIndenting(true);
            format.setLineWidth(0);             
            format.setPreserveSpace(true);
            StringWriter sw = new StringWriter();
            XMLSerializer serializer = new XMLSerializer (sw, format);
            try {
                serializer.asDOMSerializer();
                serializer.serialize(node);
            } catch (IOException e) {
            }
            return sw.toString();
        }
        else if (arguments instanceof String) {
            if (verbose) {
                return "\"" + arguments + "\"";
            }
            else {
                return (String) arguments;
            }
        }
        else {
            return arguments.toString();
        }
    }

    /**
     * Sets the property on the given object
     * 
     * @param object
     * @param property
     * @param newValue
     * @return
     */
    public void setProperty(Object object, String property, Object newValue) {
        if (object == null) {
            throw new GroovyRuntimeException("Cannot set property on null object");
        }
        else if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            pogo.setProperty(property, newValue);
        }
        else if (object instanceof Map) {
            Map map = (Map) object;
            map.put(property, newValue);
        }
        else {
            metaRegistry.getMetaClass(object.getClass()).setProperty(object, property, newValue);
        }
    }

    /**
     * Looks up the given property of the given object
     * 
     * @param object
     * @param property
     * @return
     */
    public Object getProperty(Object object, String property) {
        if (object == null) {
            throw new NullPointerException("Cannot get property: " + property + " on null object");
        }
        else if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            return pogo.getProperty(property);
        }
        else if (object instanceof Map) {
            Map map = (Map) object;
            return map.get(property);
        }
        else {
            return metaRegistry.getMetaClass(object.getClass()).getProperty(object, property);
        }
    }

    public int asInt(Object value) {
        if (value instanceof Number) {
            Number n = (Number) value;
            return n.intValue();
        }
        throw new GroovyRuntimeException("Could not convert object: " + value + " into an int");
    }

    public Number asNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }
        else if (value instanceof String) {
            String s = (String) value;

            if (s.length() == 1)
                return new Integer(s.charAt(0));
            else
                return Double.valueOf(s);
        }
        else if (value instanceof Character) {
            return new Integer(((Character) value).charValue());
        }
        else {
            throw new GroovyRuntimeException("Could not convert object: " + value + " into a Number");
        }
    }

    /**
     * Attempts to load the given class via name using the current class loader
     * for this code or the thread context class loader
     */
    protected Class loadClass(String type) {
        try {
            return getClass().getClassLoader().loadClass(type);
        }
        catch (ClassNotFoundException e) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(type);
            }
            catch (ClassNotFoundException e2) {
                try {
                    return Class.forName(type);
                }
                catch (ClassNotFoundException e3) {
                }
            }
            throw new GroovyRuntimeException("Could not load type: " + type, e);
        }
    }

    /**
     * Find the right hand regex within the left hand string and return a matcher.
     * 
     * @param left string to compare
     * @param right regular expression to compare the string to
     * @return
     */
    public Matcher objectFindRegex(Object left, Object right) {
        String stringToCompare;
        if (left instanceof String) {
            stringToCompare = (String) left;
        }
        else {
            stringToCompare = toString(left);
        }
        String regexToCompareTo;
        if (right instanceof String) {
            regexToCompareTo = (String) right;
        }
        else if (right instanceof Pattern) {
            Pattern pattern = (Pattern) right;
            return pattern.matcher(stringToCompare);
        }
        else {
            regexToCompareTo = toString(right);
        }
        Matcher matcher = Pattern.compile(regexToCompareTo).matcher(stringToCompare);
        return matcher;
    }

    /**
     * Find the right hand regex within the left hand string and return a matcher.
     * 
     * @param left string to compare
     * @param right regular expression to compare the string to
     * @return
     */
    public boolean objectMatchRegex(Object left, Object right) {
        Pattern pattern;
        if (right instanceof Pattern) {
            pattern = (Pattern) right;
        }
        else {
            pattern = Pattern.compile(toString(right));
        }
        String stringToCompare = toString(left);
        Matcher matcher = pattern.matcher(stringToCompare);
        RegexSupport.setLastMatcher(matcher);
        return matcher.matches();
    }

    /**
     * Compile a regular expression from a string.
     * 
     * @param regex
     * @return
     */
    public Pattern regexPattern(Object regex) {
        return Pattern.compile(regex.toString());
    }

    public Object asType(Object object, Class type) {
        if (object == null) {
            return null;
        }
        if (type.isInstance(object)) {
            return object;
        }
        if (type.equals(String.class)) {
            return object.toString();
        }
        if (type.equals(Character.class)) {
            if (object instanceof Number) {
                return asCharacter((Number) object);
            }
            else {
                String text = object.toString();
                if (text.length() == 1) {
                    return new Character(text.charAt(0));
                }
                else {
                    throw new ClassCastException("Cannot cast: " + text + " to a Character");
                }
            }
        }
        if (Number.class.isAssignableFrom(type)) {
            if (object instanceof Character) {
                return new Integer(((Character) object).charValue());
            }
            else if (object instanceof String) {
                String c = (String) object;
                if (c.length() == 1) {
                    return new Integer(c.charAt(0));
                }
                else {
                    throw new ClassCastException("Cannot cast: '" + c + "' to an Integer");
                }
            }
        }
        if (object instanceof Number) {
            Number n = (Number) object;
            if (type.isPrimitive()) {
                if (type == byte.class) {
                    return new Byte(n.byteValue());
                }
                if (type == char.class) {
                    return new Character((char) n.intValue());
                }
                if (type == short.class) {
                    return new Short(n.shortValue());
                }
                if (type == int.class) {
                    return new Integer(n.intValue());
                }
                if (type == long.class) {
                    return new Long(n.longValue());
                }
                if (type == float.class) {
                    return new Float(n.floatValue());
                }
                if (type == double.class) {
                    Double answer = new Double(n.doubleValue());
                    //throw a runtime exception if conversion would be out-of-range for the type.
                    if (!(n instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                            || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                        throw new  GroovyRuntimeException("Automatic coercion of "+n.getClass().getName()
                                +" value "+n+" to double failed.  Value is out of range.");
                    }
                    return answer;
                }
            }
            else {
                if (Number.class.isAssignableFrom(type)) {
                    if (type == Byte.class) {
                        return new Byte(n.byteValue());
                    }
                    if (type == Character.class) {
                        return new Character((char) n.intValue());
                    }
                    if (type == Short.class) {
                        return new Short(n.shortValue());
                    }
                    if (type == Integer.class) {
                        return new Integer(n.intValue());
                    }
                    if (type == Long.class) {
                        return new Long(n.longValue());
                    }
                    if (type == Float.class) {
                        return new Float(n.floatValue());
                    }
                    if (type == Double.class) {
                        Double answer = new Double(n.doubleValue());
                        //throw a runtime exception if conversion would be out-of-range for the type.
                        if (!(n instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                                || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                            throw new  GroovyRuntimeException("Automatic coercion of "+n.getClass().getName()
                                    +" value "+n+" to double failed.  Value is out of range.");
                        }
                        return answer;
                    }

                }
            }
        }
        if (type == Boolean.class) {
            return asBool(object) ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public boolean asBool(Object object) {
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
        else if (object instanceof Number) {
            Number n = (Number) object;
            return n.intValue() != 0;
        }
        else {
            return object != null;
        }
    }

    protected Character asCharacter(Number value) {
        return new Character((char) value.intValue());
    }

    protected Character asCharacter(String text) {
        return new Character(text.charAt(0));
    }

    /**
     * @return true if the given value is a valid character string (i.e. has length of 1)
     */
    protected boolean isValidCharacterString(Object value) {
        if (value instanceof String) {
            String s = (String) value;
            if (s.length() == 1) {
                return true;
            }
        }
        return false;
    }

}
