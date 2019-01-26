/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.Closure;
import groovy.lang.MetaBeanProperty;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.util.ManagedConcurrentMap;
import org.codehaus.groovy.util.ReferenceBundle;

import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This MetaBeanProperty will create a pseudo property whose value is bound to an object
 * using weak references. The values will go out of scope and be garbage collected when
 * the the object is collected
 *
 * In fact, this class should be called ExpandoProperty.
 *
 * @author Graeme Rocher
 * @author Alex Tkachman
 * @since 1.5
 */
public class ThreadManagedMetaBeanProperty extends MetaBeanProperty {
    private static final ConcurrentHashMap<String,ManagedConcurrentMap> PROPNAME_TO_MAP = new ConcurrentHashMap<>();

    private final ManagedConcurrentMap instance2Prop;

    private final Class declaringClass;
    private final ThreadBoundGetter getter;
    private final ThreadBoundSetter setter;
    private Object initialValue;
    private Closure initialValueCreator;
    
    private static final ReferenceBundle SOFT_BUNDLE = ReferenceBundle.getSoftBundle();

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

        instance2Prop = getInstance2PropName(name);
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

        instance2Prop = getInstance2PropName(name);
    }

    private static ManagedConcurrentMap getInstance2PropName(String name) {
        ManagedConcurrentMap res = PROPNAME_TO_MAP.get(name);
        if (res == null) {
            res = new ManagedConcurrentMap(SOFT_BUNDLE);
            ManagedConcurrentMap ores = PROPNAME_TO_MAP.putIfAbsent(name, res);
            if (ores != null)
              return ores;
        }
        return res;
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
        private final String name;


        public ThreadBoundGetter(String name) {
            setParametersTypes(CachedClass.EMPTY_ARRAY);
            this.name = getGetterName(name, type);
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
            return instance2Prop.getOrPut(object, getInitialValue()).getValue();
        }
    }

    /**
     * Sets the ThreadBound state of the property like a setter
     */
    private class ThreadBoundSetter extends MetaMethod {
        private final String name;

        public ThreadBoundSetter(String name) {
            setParametersTypes (new CachedClass [] {ReflectionCache.getCachedClass(type)} );
            this.name = getSetterName(name);
        }


        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        /* (non-Javadoc)
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
            instance2Prop.put(object, arguments[0]);
            return null;
        }
    }
}
