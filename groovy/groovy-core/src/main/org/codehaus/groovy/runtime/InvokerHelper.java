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

import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import groovy.lang.MetaClass;
import groovy.lang.ObjectRange;
import groovy.lang.Script;
import groovy.lang.Tuple;

import java.lang.reflect.Array;
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

    private static final Invoker singleton = new Invoker();

    public static MetaClass getMetaClass(Object object) {
        return getInstance().getMetaClass(object);
    }

    public static Invoker getInstance() {
        return singleton;
    }

    public static Object invokeVoidMethod(Object object, String methodName) {
        return getInstance().invokeMethod(object, methodName, EMPTY_ARGS);
    }

    public static Object invokeMethod(Object object, String methodName, Object arguments) {
        return getInstance().invokeMethod(object, methodName, arguments);
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

    public static Object invokeConstructor(String type, Object arguments) {
        return getInstance().invokeConstructor(type, arguments);
    }

    public static Object invokeConstructorOf(Class type, Object arguments) {
        return getInstance().invokeConstructorOf(type, arguments);
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
        return getInstance().toString(arguments);
    }

    public static String inspect(Object self) {
        return getInstance().inspect(self);
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
    public static void setPropertySafe2(Object newValue, Object object, String property) {
        if (object != null) {
            setProperty2(newValue, object, property);
        }
    }

    /**
     * Provides a hook for type coercion of the given object to the required type
     * @param type of object to convert the given object to
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

    public static Number negate(Number number) {
    	if (number instanceof Double) {
    		return new Double(-number.doubleValue());
    	}
   		return new Integer(-number.intValue());
    }
    
    public static boolean isCase(Object switchValue, Object caseExpression) {
        return asBool(invokeMethod(caseExpression, "isCase", new Object[] { switchValue }));
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

    public static Pattern regexPattern(String regex) {
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

    public static Tuple createTuple(Object[] array) {
        return new Tuple(array);
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
        while (i < values.length) {
            answer.put(values[i++], values[i++]);
        }
        return answer;
    }

    public static List createRange(Object from, Object to) {
        if (from instanceof Integer && to instanceof Integer) {
            return new IntRange(asInt(from), asInt(to));
        }
        return new ObjectRange((Comparable) from, (Comparable) to);
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
                        object.invokeMethod("main", new Object[] { new String[0] });
                        return null;
                    } };
            }
            script.setBinding(context);
            return script;
        }
        catch (Exception e) {
            throw new GroovyRuntimeException(
                "Failed to create Script instance for class: " + scriptClass + ". Reason: " + e,
                e);
        }
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
     * @returns the array as a List
     */
    protected static List primitiveArrayToList(Object array) {
        int size = Array.getLength(array);
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }
}
