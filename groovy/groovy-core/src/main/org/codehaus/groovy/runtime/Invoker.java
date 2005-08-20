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

import org.codehaus.groovy.classgen.AsmClassGenerator;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MissingMethodException;
import groovy.lang.Range;
import groovy.lang.SpreadList;
import groovy.lang.Tuple;
import groovy.lang.GroovyInterceptable;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
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

    public MetaClassRegistry getMetaRegistry() {
        return metaRegistry;
    }

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
            throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
        }

        // if the object is a Class, call a static method from that class
        if (object instanceof Class) {
            Class theClass = (Class) object;
            MetaClass metaClass = metaRegistry.getMetaClass(theClass);
            return metaClass.invokeStaticMethod(object, methodName, asArray(arguments));
        }
        else // it's an instance
        {
            // if it's not an object implementing GroovyObject (thus not builder, nor a closure)
            if (!(object instanceof GroovyObject)) {
                Class theClass = object.getClass();
                MetaClass metaClass = metaRegistry.getMetaClass(theClass);
                return metaClass.invokeMethod(object, methodName, asArray(arguments));
            }
            // it's an object implementing GroovyObject
            else {
                // if it's a closure, use the closure's invokeMethod()
                if (object instanceof Closure) {
                    Closure closure = (Closure) object;
                    return closure.invokeMethod(methodName, asArray(arguments));
                }
                // it's some kind of wacky object that overrides invokeMethod() to do some groovy stuff
                // (like a proxy, a builder, some custom funny object which controls the invokation mechanism)
                else {
                    GroovyObject groovy = (GroovyObject) object;
                    try {
                        // if it's a pure interceptable object (even intercepting toString(), clone(), ...)
                        if (groovy instanceof GroovyInterceptable) {
                            return groovy.invokeMethod(methodName, asArray(arguments));
                        }
                        // else if there's a statically typed method or a GDK method
                        else {
                            return groovy.getMetaClass().invokeMethod(object, methodName, asArray(arguments));
                        }
                    }
                    catch (MissingMethodException e) {
                        if (e.getMethod().equals(methodName) && object.getClass() == e.getType()) {
                            // in case there's nothing else, invoke the object's own invokeMethod()
                            return groovy.invokeMethod(methodName, asArray(arguments));
                        }
                        else {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    public Object invokeSuperMethod(Object object, String methodName, Object arguments) {
        if (object == null) {
            throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
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
        else if ((arguments instanceof Object[]) && ((Object[]) arguments).length == 0) {
            return (Object[]) arguments;
        }
        else if (arguments instanceof Tuple) {
            Tuple tuple = (Tuple) arguments;
            Object[] objects = tuple.toArray();
            ArrayList array = new ArrayList();
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof SpreadList) {
                    SpreadList slist = (SpreadList) objects[i];
                    for (int j = 0; j < slist.size(); j++) {
                        array.add(slist.get(j));
                    }
                }
                else {
                    array.add(objects[i]);
                }
            }
            return array.toArray();
        }
        else if (arguments instanceof Object[]) {
            Object[] objects = (Object[]) arguments;
            ArrayList array = new ArrayList();
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof SpreadList) {
                    SpreadList slist = (SpreadList) objects[i];
                    for (int j = 0; j < slist.size(); j++) {
                        array.add(slist.get(j));
                    }
                }
                else {
                    array.add(objects[i]);
                }
            }
            return array.toArray();
        }
        else if (arguments instanceof SpreadList) {
            ArrayList array = new ArrayList();
            SpreadList slist = (SpreadList) arguments;
            for (int j = 0; j < slist.size(); j++) {
                array.add(slist.get(j));
            }
            return array.toArray();
        }
        else {
            return new Object[]{arguments};
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
     * Converts the value parameter into a <code>Collection</code>.
     *
     * @param value value to convert
     * @return a Collection
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
            return Collections.EMPTY_LIST.iterator();
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
                    if (done) {
                        return false;
                    }
                    if (!found) {
                        found = matcher.find();
                        if (!found) {
                            done = true;
                        }
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
            else if (left instanceof List && right instanceof List) {
                return DefaultGroovyMethods.equals((List) left, (List) right);
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

    /**
     * A helper method to provide some better toString() behaviour such as turning arrays
     * into tuples
     */
    public String toString(Object arguments) {
        if (arguments instanceof Object[])
            return toArrayString((Object[]) arguments);
        else if (arguments instanceof Map)
            return toMapString((Map) arguments);
        else if (arguments instanceof Collection)
            return format(arguments, true);
        else
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
            if (i > 0) {
                argBuf.append(", ");
            }
            argBuf.append(arguments[i] != null ? arguments[i].getClass().getName() : "null");
        }
        return argBuf.toString();
    }

    /**
     * A helper method to return the string representation of a map with bracket boundaries "[" and "]".
     */
    public String toMapString(Map arg) {
        if (arg == null) {
            return "null";
        }
        if (arg.isEmpty()) {
            return "[:]";
        }
        String sbdry = "[";
        String ebdry = "]";
        StringBuffer buffer = new StringBuffer(sbdry);
        boolean first = true;
        for (Iterator iter = arg.entrySet().iterator(); iter.hasNext();) {
            if (first)
                first = false;
            else
                buffer.append(", ");
            Map.Entry entry = (Map.Entry) iter.next();
            buffer.append(format(entry.getKey(), true));
            buffer.append(":");
            buffer.append(format(entry.getValue(), true));
        }
        buffer.append(ebdry);
        return buffer.toString();
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     */
    public String toListString(Collection arg) {
        if (arg == null) {
            return "null";
        }
        if (arg.isEmpty()) {
            return "[]";
        }
        String sbdry = "[";
        String ebdry = "]";
        StringBuffer buffer = new StringBuffer(sbdry);
        boolean first = true;
        for (Iterator iter = arg.iterator(); iter.hasNext();) {
            if (first)
                first = false;
            else
                buffer.append(", ");
            Object elem = iter.next();
            buffer.append(format(elem, true));
        }
        buffer.append(ebdry);
        return buffer.toString();
    }

    /**
     * A helper method to return the string representation of an arrray of objects
     * with brace boundaries "{" and "}".
     */
    public String toArrayString(Object[] arguments) {
        if (arguments == null) {
            return "null";
        }
        String sbdry = "{";
        String ebdry = "}";
        StringBuffer argBuf = new StringBuffer(sbdry);
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                argBuf.append(", ");
            }
            argBuf.append(format(arguments[i], true));
        }
        argBuf.append(ebdry);
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
            XMLSerializer serializer = new XMLSerializer(sw, format);
            try {
                serializer.asDOMSerializer();
                serializer.serialize(node);
            }
            catch (IOException e) {
            }
            return sw.toString();
        }
        else if (arguments instanceof String) {
            if (verbose) {
                String arg = ((String)arguments).replaceAll("\\n", "\\\\n");    // line feed
                arg = arg.replaceAll("\\r", "\\\\r");      // carriage return
                arg = arg.replaceAll("\\t", "\\\\t");      // tab
                arg = arg.replaceAll("\\f", "\\\\f");      // form feed
                arg = arg.replaceAll("\\\"", "\\\\\"");    // double quotation amrk
                arg = arg.replaceAll("\\\\", "\\\\");      // back slash
                return "\"" + arg + "\"";
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
     * Looks up the given property of the given object
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
    
    /**
     * Sets the property on the given object
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
            if (object instanceof Class)
                metaRegistry.getMetaClass((Class) object).setProperty((Class) object, property, newValue);
            else
                metaRegistry.getMetaClass(object.getClass()).setProperty(object, property, newValue);
        }
    }

    /**
     * Looks up the given attribute (field) on the given object
     */
    public Object getAttribute(Object object, String attribute) {
        if (object == null) {
            throw new NullPointerException("Cannot get attribute: " + attribute + " on null object");

            /**
             } else if (object instanceof GroovyObject) {
             GroovyObject pogo = (GroovyObject) object;
             return pogo.getAttribute(attribute);
             } else if (object instanceof Map) {
             Map map = (Map) object;
             return map.get(attribute);
             */
        }
        else {
            if (object instanceof Class)
                return metaRegistry.getMetaClass((Class) object).getAttribute(object, attribute);
            else
                return metaRegistry.getMetaClass(object.getClass()).getAttribute(object, attribute);
	}
    }

    /**
     * Sets the given attribute (field) on the given object
     */
    public void setAttribute(Object object, String attribute, Object newValue) {
        if (object == null) {
            throw new GroovyRuntimeException("Cannot set attribute on null object");
            /*
        } else if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            pogo.setProperty(attribute, newValue);
        } else if (object instanceof Map) {
            Map map = (Map) object;
            map.put(attribute, newValue);
            */
        }
        else {
            if (object instanceof Class)
                metaRegistry.getMetaClass((Class) object).setAttribute((Class) object, attribute, newValue);
            else
                metaRegistry.getMetaClass(object.getClass()).setAttribute(object, attribute, newValue);
	}
    }

    /**
     * Returns the method pointer for the given object name
     */
    public Closure getMethodPointer(Object object, String methodName) {
        if (object == null) {
            throw new NullPointerException("Cannot access method pointer for '" + methodName + "' on null object");
        }
        return metaRegistry.getMetaClass(object.getClass()).getMethodPointer(object, methodName);
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
     * @param left  string to compare
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
     * @param left  string to compare
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
                        throw new GroovyRuntimeException("Automatic coercion of " + n.getClass().getName()
                                + " value " + n + " to double failed.  Value is out of range.");
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
                            throw new GroovyRuntimeException("Automatic coercion of " + n.getClass().getName()
                                    + " value " + n + " to double failed.  Value is out of range.");
                        }
                        return answer;
                    }

                }
            }
        }
        if (type == Boolean.class) {
            return asBool(object) ? Boolean.TRUE : Boolean.FALSE;
        }
        Object[] args = null;
        if (object instanceof Collection) {
            Collection list = (Collection) object;
            args = list.toArray();
        }
        else if (object instanceof Object[]) {
            args = (Object[]) object;
        }
        if (args != null) {
            // lets try invoke the constructor with the list as arguments
            // such as for creating a Dimension, Point, Color etc.
            try {
                return invokeConstructorOf(type, args);
            }
            catch (Exception e) {
                // lets ignore exception and return the original object
                // as the caller has more context to be able to throw a more
                // meaningful exception
            }

        }
        return object;
    }

    public Object asArray(Object object, Class type) {
        Collection list = asCollection(object);
        int size = list.size();
        Class elementType = type.getComponentType();
        Object array = Array.newInstance(elementType, size);
        int idx = 0;

        if (boolean.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setBoolean(array, idx, asBool(element));
            }
        }
        else if (byte.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setByte(array, idx, asByte(element));
            }
        }
        else if (char.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setChar(array, idx, asChar(element));
            }
        }
        else if (double.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setDouble(array, idx, asDouble(element));
            }
        }
        else if (float.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setFloat(array, idx, asFloat(element));
            }
        }
        else if (int.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setInt(array, idx, asInt(element));
            }
        }
        else if (long.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setLong(array, idx, asLong(element));
            }
        }
        else if (short.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setShort(array, idx, asShort(element));
            }
        }
        else {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Object coercedElement = asType(element, elementType);
                Array.set(array, idx, coercedElement);
            }
        }
        return array;
    }

    public Number asNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }
        else if (value instanceof String) {
            String s = (String) value;

            if (s.length() == 1) {
                return new Integer(s.charAt(0));
            }
            else {
                return new BigDecimal(s);
            }
        }
        else if (value instanceof Character) {
            return new Integer(((Character) value).charValue());
        }
        else {
            throw new GroovyRuntimeException("Could not convert object: " + value + " into a Number");
        }
    }

    public byte asByte(Object element) {
        return asNumber(element).byteValue();
    }

    public char asChar(Object element) {
        if (element instanceof String) {
            return asCharacter((String) element).charValue();
        }
        return asCharacter(asNumber(element)).charValue();
    }

    public float asFloat(Object element) {
        return asNumber(element).floatValue();
    }

    public double asDouble(Object element) {
        return asNumber(element).doubleValue();
    }

    public short asShort(Object element) {
        return asNumber(element).shortValue();
    }

    public int asInt(Object element) {
        return asNumber(element).intValue();
    }

    public long asLong(Object element) {
        return asNumber(element).longValue();
    }

    public boolean asBool(Object object) {
        /*
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
        else if (object instanceof String) {
            String string = (String) object;
            return string.length() > 0;
        }
        else if (object instanceof Number) {
            Number n = (Number) object;
            return n.doubleValue() != 0;
        }
        else {
            return object != null;
        }
        */
        return AsmClassGenerator.asBool(object);
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

    public void removeMetaClass(Class clazz) {
        getMetaRegistry().removeMetaClass(clazz);
    }
}
