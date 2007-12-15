/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.metaclass;


import groovy.lang.Closure;
import groovy.lang.MetaBeanProperty;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This MetaBeanProperty will create a pseudo property whose value is bound to the current
 * Thread using soft references. The values will go out of scope and be garabage collected when
 * the Thread dies or when memory is required by the JVM
 * <p/>
 * The property uses an InheritableThreadLocal instance internally so child threads will still be able
 * to see the property
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class ThreadManagedMetaBeanProperty extends MetaBeanProperty {
    private static final CachedClass[] ZERO_ARGUMENT_LIST = new CachedClass[0];
    private static final ThreadLocal PROPERTY_INSTANCE_HOLDER = new InheritableThreadLocal();

    private Class declaringClass;
    private ThreadBoundGetter getter;
    private ThreadBoundSetter setter;
    private Object initialValue;
    private static final String PROPERTY_SET_PREFIX = "set";
    private Closure initialValueCreator;

    /**
     * Retrieves the initial value of the ThreadBound property
     *
     * @return The initial value
     */
    public synchronized Object getInitialValue() {
        return getInitialValue(null);
    }

    public synchronized Object getInitialValue(Object object) {
        if (initialValueCreator != null) {
            return initialValueCreator.call(object);
        }
        return initialValue;

    }

    /**
     * Closure responsible for creating the initial value of thread-managed bean properties
     *
     * @param callable The closure responsible for creating the initial value
     */
    public void setInitialValueCreator(Closure callable) {
        this.initialValueCreator = callable;
    }

    /**
     * Constructs a new ThreadManagedBeanProperty for the given arguments
     *
     * @param declaringClass The class that declares the property
     * @param name           The name of the property
     * @param type           The type of the property
     * @param iv             The properties initial value
     */
    public ThreadManagedMetaBeanProperty(Class declaringClass, String name, Class type, Object iv) {
        super(name, type, null, null);
        this.type = type;
        this.declaringClass = declaringClass;

        this.getter = new ThreadBoundGetter(name);
        this.setter = new ThreadBoundSetter(name);
        initialValue = iv;

    }

    /**
     * Constructs a new ThreadManagedBeanProperty for the given arguments
     *
     * @param declaringClass      The class that declares the property
     * @param name                The name of the property
     * @param type                The type of the property
     * @param initialValueCreator The closure responsible for creating the initial value
     */
    public ThreadManagedMetaBeanProperty(Class declaringClass, String name, Class type, Closure initialValueCreator) {
        super(name, type, null, null);
        this.type = type;
        this.declaringClass = declaringClass;

        this.getter = new ThreadBoundGetter(name);
        this.setter = new ThreadBoundSetter(name);
        this.initialValueCreator = initialValueCreator;

    }

    private static Object getThreadBoundPropertyValue(Object obj, String name, Object initialValue) {
        Map propertyMap = getThreadBoundPropertMap();
        String key = System.identityHashCode(obj) + name;
        if (propertyMap.containsKey(key)) {
            return propertyMap.get(key);
        } else {
            propertyMap.put(key, initialValue);
            return initialValue;
        }
    }

    private static Map getThreadBoundPropertMap() {
        Map propertyMap = (Map) PROPERTY_INSTANCE_HOLDER.get();
        if (propertyMap == null) {
            propertyMap = new WeakHashMap();
            PROPERTY_INSTANCE_HOLDER.set(propertyMap);
        }
        return propertyMap;
    }

    private static Object setThreadBoundPropertyValue(Object obj, String name, Object value) {
        Map propertyMap = getThreadBoundPropertMap();
        String key = System.identityHashCode(obj) + name;
        return propertyMap.put(key, value);
    }

    /* (non-Javadoc)
      * @see groovy.lang.MetaBeanProperty#getGetter()
      */
    public MetaMethod getGetter() {
        return this.getter;
    }

    /* (non-Javadoc)
      * @see groovy.lang.MetaBeanProperty#getSetter()
      */
    public MetaMethod getSetter() {
        return this.setter;
    }


    /**
     * Accesses the ThreadBound state of the property as a getter
     *
     * @author Graeme Rocher
     */
    class ThreadBoundGetter extends MetaMethod {


        private final String name, name0;


        public ThreadBoundGetter(String name) {
            setParametersTypes(new CachedClass[0]);
            this.name = getGetterName(name, type);
            this.name0 = name;

        }


        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        public String getName() {
            return name;
        }

        public Class getReturnType() {
            return type;
        }

        public CachedClass getDeclaringClass() {
            return ReflectionCache.getCachedClass(declaringClass);
        }

        /* (non-Javadoc)
           * @see groovy.lang.MetaMethod#invoke(java.lang.Object, java.lang.Object[])
           */
        public Object invoke(Object object, Object[] arguments) {
            return getThreadBoundPropertyValue(object, name0, getInitialValue());
        }
    }

    /**
     * Sets the ThreadBound state of the property like a setter
     *
     * @author Graeme Rocher
     */
    private class ThreadBoundSetter extends MetaMethod {


        private final String name, name0;

        public ThreadBoundSetter(String name) {
            setParametersTypes (new CachedClass [] {ReflectionCache.getCachedClass(type)} );
            this.name = getSetterName(name);
            this.name0 = name;
        }


        public int getModifiers() {
            return Modifier.PUBLIC;
        }/* (non-Javadoc)
		 * @see groovy.lang.MetaMethod#getName()
		 */

        public String getName() {
            return name;
        }

        public Class getReturnType() {
            return type;
        }

        public CachedClass getDeclaringClass() {
            return ReflectionCache.getCachedClass(declaringClass);
        }

        /* (non-Javadoc)
           * @see groovy.lang.MetaMethod#invoke(java.lang.Object, java.lang.Object[])
           */
        public Object invoke(Object object, Object[] arguments) {
            return setThreadBoundPropertyValue(object, name0, arguments[0]);
        }
    }

    private String getGetterName(String propertyName, Class type) {
        String prefix = type == boolean.class || type == Boolean.class ? "is" : "get";
        return prefix + Character.toUpperCase(propertyName.charAt(0))
                + propertyName.substring(1);
    }

    private String getSetterName(String propertyName) {
        return PROPERTY_SET_PREFIX + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

}
