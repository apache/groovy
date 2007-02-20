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

import groovy.lang.Closure;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MissingMethodException;

import java.util.Map;

import org.codehaus.groovy.runtime.metaclass.MetaClassHelper;

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

    private final MetaClassRegistry metaRegistry = GroovySystem.metaClassRegistry;

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
            object = NullObject.getNullObject();
            //throw new NullPointerException("Cannot invoke method " + methodName + "() on null object");
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
                return invokePojoMethod(object, methodName, arguments);
            }
            // it's an object implementing GroovyObject
            else {
                return invokePogoMethod(object, methodName, arguments);
            }
        }
    }

    private Object invokePojoMethod(Object object, String methodName, Object arguments) {
        Class theClass = object.getClass();
        MetaClass metaClass = metaRegistry.getMetaClass(theClass);
        return metaClass.invokeMethod(object, methodName, asArray(arguments));
    }

    private Object invokePogoMethod(Object object, String methodName, Object arguments) {
        GroovyObject groovy = (GroovyObject) object;
        try {
            // if it's a pure interceptable object (even intercepting toString(), clone(), ...)
            if (groovy instanceof GroovyInterceptable) {
                return groovy.invokeMethod(methodName, asArray(arguments));
            }
            //else try a statically typed method or a GDK method
            return groovy.getMetaClass().invokeMethod(object, methodName, asArray(arguments));
        } catch (MissingMethodException e) {
            if (e.getMethod().equals(methodName) && object.getClass() == e.getType()) {
                // in case there's nothing else, invoke the object's own invokeMethod()
                return groovy.invokeMethod(methodName, asArray(arguments));
            }
            throw e;
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

    public Object invokeStaticMethod(Class type, String method, Object arguments) {
        MetaClass metaClass = metaRegistry.getMetaClass(type);
        return metaClass.invokeStaticMethod(type, method, asArray(arguments));
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
        if (arguments instanceof Object[]) {
            return (Object[]) arguments;
        }
        return new Object[]{arguments};
    }

    /**
     * Looks up the given property of the given object
     */
    public Object getProperty(Object object, String property) {
        if (object == null) {
            throw new NullPointerException("Cannot get property: " + property + " on null object");
        }
        if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            return pogo.getProperty(property);
        }
        if (object instanceof Class) {
            Class c = (Class) object;
            return metaRegistry.getMetaClass(c).getProperty(object, property);
        }
        return metaRegistry.getMetaClass(object.getClass()).getProperty(object, property);
    }
    
    /**
     * Sets the property on the given object
     */
    public void setProperty(Object object, String property, Object newValue) {
        if (object == null) {
            throw new GroovyRuntimeException("Cannot set property on null object");
        }
        if (object instanceof GroovyObject) {
            GroovyObject pogo = (GroovyObject) object;
            pogo.setProperty(property, newValue);
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
            if (object instanceof Class) {
                return metaRegistry.getMetaClass((Class) object).getAttribute(object, attribute);
            } else if (object instanceof GroovyObject) {
                return ((GroovyObject)object).getMetaClass().getAttribute(object, attribute);
            } else {
                return metaRegistry.getMetaClass(object.getClass()).getAttribute(object, attribute);
            }
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
            if (object instanceof Class) {
                metaRegistry.getMetaClass((Class) object).setAttribute(object, attribute, newValue);
            } else if (object instanceof GroovyObject) {
                ((GroovyObject)object).getMetaClass().setAttribute(object, attribute, newValue);
            } else {
                metaRegistry.getMetaClass(object.getClass()).setAttribute(object, attribute, newValue);
            }
	}
    }

    /**
     * Returns the method pointer for the given object name
     */
    public Closure getMethodPointer(Object object, String methodName) {
        if (object == null) {
            throw new NullPointerException("Cannot access method pointer for '" + methodName + "' on null object");
        }
        return MetaClassHelper.getMethodPointer(object, methodName);
    }

    public void removeMetaClass(Class clazz) {
        metaRegistry.removeMetaClass(clazz);
    }
}
