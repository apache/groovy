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
import groovy.lang.GroovyObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A helper class to invoke methods or extract properties on arbitrary Java objects dynamically
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Invoker {

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
        if (object == null) {
            throw new NullPointerException("Cannot invoke method: " + methodName + " on null object");
        }

        if (object instanceof GroovyObject) {
            GroovyObject groovy = (GroovyObject) object;
            return groovy.invokeMethod(methodName, arguments);
        }
        else {
            List argumentList = asList(arguments);
            if (object instanceof Class) {
                Class theClass = (Class) object;

                MetaClass metaClass = metaRegistry.getMetaClass(theClass);
                return metaClass.invokeStaticMethod(object, methodName, arguments, argumentList);
            }
            else {
                Class theClass = object.getClass();

                MetaClass metaClass = metaRegistry.getMetaClass(theClass);
                return metaClass.invokeMethod(object, methodName, arguments, argumentList);
            }
        }
    }

    public Object invokeStaticMethod(String type, String method, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(loadClass(type));
        List argumentList = asList(arguments);
        return metaClass.invokeStaticMethod(null, method, arguments, argumentList);
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
            return Arrays.asList((Object[]) value);
        }
        else {
            // lets assume its a collection of 1
            return Collections.singletonList(value);
        }
    }

    public Iterator asIterator(Object value) {
        if (value instanceof Iterator) {
            return (Iterator) value;
        }
        return asCollection(value).iterator();
    }

    /**
     * Converts the given value to a boolean value for use in a branch
     * statement or loop
     * 
     * @param value
     * @return
     */
    public Boolean asBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        else if (value instanceof String) {
            String s = (String) value;
            if (s.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            }
            else if (s.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            }
        }
        throw new InvokerException("Cannot convert value: " + value + " to a boolean");
    }

    /**
     * @param left
     * @param right
     * @return
     */
    public int compareTo(Object left, Object right) {
        //System.out.println("Comparing: " + left + " to: " + right);

        if (left instanceof Comparable) {
            Comparable comparable = (Comparable) left;
            return comparable.compareTo(right);
        }
        /** todo we might wanna do some type conversion here */
        throw new InvokerException("Cannot compare values: " + left + " and " + " right");
    }

    /**
     * @return true if the two objects are null or the objects are equal
     */
    public boolean objectsEqual(Object left, Object right) {
        if (left == right) {
            return true;
        }
        if (left != null) {
            return left.equals(right);
        }
        return false;
    }

    /**
     * A helper method to provide some better toString() behaviour such as turning arrays
     * into tuples
     */
    public String toString(Object arguments) {
        if (arguments == null) {
            return "null";
        }
        else if (arguments.getClass().isArray()) {
            Object[] array = (Object[]) arguments;
            StringBuffer buffer = new StringBuffer("[");
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(toString(array[i]));
            }
            buffer.append("]");
            return buffer.toString();
        }
        else if (arguments instanceof List) {
            List list = (List) arguments;
            StringBuffer buffer = new StringBuffer("[");
            boolean first = true;
            for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                if (first) {
                    first = false;
                }
                else {
                    buffer.append(", ");
                }
                buffer.append(toString(iter.next()));
            }
            buffer.append("]");
            return buffer.toString();
        }
        else if (arguments instanceof Map) {
            Map map = (Map) arguments;
            StringBuffer buffer = new StringBuffer("[");
            boolean first = true;
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
                if (first) {
                    first = false;
                }
                else {
                    buffer.append(", ");
                }
                Map.Entry entry = (Map.Entry) iter.next();
                buffer.append(toString(entry.getKey()));
                buffer.append(":");
                buffer.append(toString(entry.getValue()));
            }
            buffer.append("]");
            return buffer.toString();
        }
        else if (arguments instanceof String) {
            return "'" + arguments + "'";
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
            throw new InvokerException("Cannot set property on null object");
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
            throw new InvokerException("Cannot get property on null object");
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
        throw new InvokerException("Could not convert object: " + value + " into an int");
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
                throw new InvokerException("Could not load type: " + type, e);
            }
        }
    }

}
