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
package groovy.lang;

import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass;

import java.lang.reflect.Constructor;
import java.util.Iterator;

/**
 * A MetaClassRegistry is an object that is responsible for managing the a cache of MetaClass instances. Each
 * java.lang.Class instance has an associated MetaClass and client code can query this interface for the MetaClass for
 * a given associated java.lang.Class
 *
 * @see groovy.lang.MetaClass
 *
 */
public interface MetaClassRegistry {
    
    /**
     * The main function of the registry
     * If a meta class exists then return it
     * otherwise create one, put it in the registry and return it
     */
    MetaClass getMetaClass(Class theClass);
    
    /**
     * Adds a metaclass to the registry for the given class
     *
     * @param theClass The class
     * @param theMetaClass The MetaClass for theClass
     */
    void setMetaClass(Class theClass, MetaClass theMetaClass);

    /**
     * Removes a cached MetaClass from the registry
     *
     * @param theClass The Java class of the MetaClass to remove
     */
    void removeMetaClass(Class theClass);

    /**
     * Retrieves the MetaClassCreationHandle that is responsible for constructing MetaClass instances
     *
     * @return The MetaClassCreationHandle instance
     */
    MetaClassCreationHandle getMetaClassCreationHandler();

    /**
     * Sets the MetaClassCreationHandle instance that is responsible for constructing instances
     *
     * @param handle The handle instance
     */
    void setMetaClassCreationHandle(MetaClassCreationHandle handle);

    /**
     * Adds a meta class change listener for constant meta classes
     *
     * @param listener - the update listener
     */
    void addMetaClassRegistryChangeEventListener(MetaClassRegistryChangeEventListener listener);
    
    /**
     * Adds a meta class change listener for constant meta classes. 
     * This listener cannot be removed!
     *
     * @param listener - the update listener
     */
    void addNonRemovableMetaClassRegistryChangeEventListener(MetaClassRegistryChangeEventListener listener);

    /**
     * Removes a meta class change listener for constant meta classes
     *
     * @param listener - the update listener
     */
    void removeMetaClassRegistryChangeEventListener(MetaClassRegistryChangeEventListener listener);

    /**
     * Returns all registered class change listener for constant meta classes.
     *
     * @return an array containing all change listener
     */
    MetaClassRegistryChangeEventListener[] getMetaClassRegistryChangeEventListeners();

    /**
     * Gets a snapshot of the current constant meta classes and returns it as Iterator.
     * Modifications done using this Iterator will not cause a ConcurrentModificationException.
     * If a MetaClass is removed using this Iterator, then the MetaClass will only
     * be removed if the MetaClass was not replaced by another MetaClass in the meantime.
     * If a MetaClass is added while using this Iterator, then it will be part of the Iteration.
     * If a MetaClass replaces another constant meta class, then the Iteration might show two
     * meta classes for the same class.
     * <p>
     * Note: This Iterator may not used with multiple threads.
     *
     * @return Iterator for the constant meta classes
     */
    Iterator iterator();    
    
    /**
     * Class used as base for the creation of MetaClass implementations.
     * The Class defaults to MetaClassImpl, if the class loading fails to
     * find a special meta class. The name for such a meta class would be
     * the class name it is created for with the prefix
     * "groovy.runtime.metaclass." By replacing the handle in the registry
     * you can have any control over the creation of what MetaClass is used
     * for a class that you want to have. 
     * WARNING: experimental code, likely to change soon
     */
    class MetaClassCreationHandle {
        private boolean disableCustomMetaClassLookup;
         
        /**
         * Creates a metaclass implementation for theClass.
         * @param theClass The class to create a metaclass for
         * @param registry The metaclass registry the metaclass we be registered in.
         */
        public final MetaClass create(Class theClass, MetaClassRegistry registry) {
           if (disableCustomMetaClassLookup)
               return createNormalMetaClass(theClass, registry);

            return createWithCustomLookup(theClass, registry);
        }

        private MetaClass createWithCustomLookup(Class theClass, MetaClassRegistry registry) {
            try {
                final Class customMetaClass = Class.forName("groovy.runtime.metaclass." + theClass.getName() + "MetaClass");
                if (DelegatingMetaClass.class.isAssignableFrom(customMetaClass)) {
                    final Constructor customMetaClassConstructor = customMetaClass.getConstructor(MetaClass.class);
                    MetaClass normalMetaClass = createNormalMetaClass(theClass, registry);
                    return (MetaClass)customMetaClassConstructor.newInstance(normalMetaClass);
                }
                else {
                    final Constructor customMetaClassConstructor = customMetaClass.getConstructor(MetaClassRegistry.class, Class.class);
                    return (MetaClass)customMetaClassConstructor.newInstance(registry, theClass);
                }
            }
            catch (final ClassNotFoundException e) {
                return createNormalMetaClass(theClass, registry);
            } catch (final Exception e) {
                throw new GroovyRuntimeException("Could not instantiate custom Metaclass for class: " + theClass.getName() + ". Reason: " + e, e);
            }
        }

        protected MetaClass createNormalMetaClass(Class theClass,MetaClassRegistry registry) {
            if (GeneratedClosure.class.isAssignableFrom(theClass)) {
                return new ClosureMetaClass(registry,theClass);
            } else {
                return new MetaClassImpl(registry, theClass);
            }
        }

        /**
         * Returns whether custom meta classes are disabled.
         */
        public boolean isDisableCustomMetaClassLookup() {
            return disableCustomMetaClassLookup;
        }

        /**
         * Set flag saying to disable lookup of custom meta classes
         * It's enough to call this method only once in your application for handle which was set in to registry
         * as every new handle will inherit this property
         * @param disableCustomMetaClassLookup flag saying to disable lookup of custom meta classes
         */
        public void setDisableCustomMetaClassLookup(boolean disableCustomMetaClassLookup) {
            this.disableCustomMetaClassLookup = disableCustomMetaClassLookup;
        }
    }
 }
