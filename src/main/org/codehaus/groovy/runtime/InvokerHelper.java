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
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.IntegerCache;
import org.w3c.dom.Element;

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

    public static Object invokeStaticMethod(Class type, String methodName, Object arguments) {
        return getInstance().invokeStaticMethod(type, methodName, arguments);
    }
    
    public static Object invokeStaticMethod(String klass, String methodName, Object arguments) throws ClassNotFoundException {
        Class type = InvokerHelper.class.forName(klass);
        return getInstance().invokeStaticMethod(type, methodName, arguments);
    }
    

    public static Object invokeStaticNoArgumentsMethod(Class type, String methodName) {
        return getInstance().invokeStaticMethod(type, methodName, EMPTY_ARGS);
    }

    public static Object invokeConstructorAt(Class at, Class type, Object arguments) {
        return getInstance().invokeConstructorAt(at, type, arguments);
    }

    public static Object invokeNoArgumentsConstructorAt(Class at, Class type) {
        return getInstance().invokeConstructorAt(at, type, EMPTY_ARGS);
    }

    public static Object invokeConstructorOf(Class type, Object arguments) {
        return getInstance().invokeConstructorOf(type, arguments);
    }
    
    public static Object invokeConstructorOf(String klass, Object arguments) throws ClassNotFoundException {
        Class type = InvokerHelper.class.forName(klass);
        return getInstance().invokeConstructorOf(type, arguments);
    }

    public static Object invokeNoArgumentsConstructorOf(Class type) {
        return getInstance().invokeConstructorOf(type, EMPTY_ARGS);
    }

    public static Object invokeClosure(Object closure, Object arguments) {
        return getInstance().invokeMethod(closure, "doCall", arguments);
    }

    public static List asList(Object value) {
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

    public static String toString(Object arguments) {
        if (arguments instanceof Object[])
            return toArrayString((Object[])arguments);
        else if (arguments instanceof Collection)
            return toListString((Collection)arguments);
        else if (arguments instanceof Map)
            return toMapString((Map)arguments);
        else if (arguments instanceof Collection)
            return format(arguments, true);
        else
            return format(arguments, false);
    }

    public static String inspect(Object self) {
        return format(self, true);
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

    public static Object negate(Object value) {
        if (value instanceof Integer) {
            Integer number = (Integer) value;
            return IntegerCache.integerValue(-number.intValue());
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

    /**
     * Find the right hand regex within the left hand string and return a matcher.
     *
     * @param left  string to compare
     * @param right regular expression to compare the string to
     * @return
     */
    public static Matcher findRegex(Object left, Object right) {
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
    public static boolean matchRegex(Object left, Object right) {
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

    public static Tuple createTuple(Object[] array) {
        return new Tuple(array);
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
            answer.add(values[i]);
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
    
    public static Iterator asIterator(Object o) {
        return (Iterator) invokeMethod(o,"iterator",EMPTY_ARGS);
    }
    
    protected static String format(Object arguments, boolean verbose) {
        if (arguments == null) {
            return "null";
        }
        else if (arguments.getClass().isArray()) {
            return format(DefaultTypeTransformation.asCollection(arguments), verbose);
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
     * A helper method to format the arguments types as a comma-separated list
     */
    public static String toTypeString(Object[] arguments) {
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
    public static String toMapString(Map arg) {
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
    public static String toListString(Collection arg) {
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
    public static String toArrayString(Object[] arguments) {
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
    
    public static List createRange(Object from, Object to, boolean inclusive) {
        try {
            return ScriptBytecodeAdapter.createRange(from,to,inclusive);
        } catch (RuntimeException re) {
            throw re;
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
    
    public static Object bitNegate(Object value) {
        if (value instanceof Integer) {
            Integer number = (Integer) value;
            return new Integer(~number.intValue());
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
            return DefaultGroovyMethods.negate(value.toString());
        }
        else if (value instanceof GString) {
            // value is a regular expression.
            return DefaultGroovyMethods.negate(value.toString());
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

}
