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
 * @author John Wilson
 * @author Graeme Rocher
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 *
 */
public interface MetaClassRegistry {
    /*
     * The main function of the registry
     * If a meta class exists then return it
     * otherwise create one, put it in the registry and return it
     */
    MetaClass getMetaClass(Class theClass);
    
    /*
     * Do we really want these two?
     */
    void setMetaClass(Class theClass, MetaClass theMetaClass);

    /**
     * Removes a cached MetaClass from the registry
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
     * adds a ConstantMetaClassChangeListener
     *
     * @param listener - the update listener
     */
    void addMetaClassRegistryChangeEventListener(MetaClassRegistryChangeEventListener listener);

    /**
     * removes a ConstantMetaClassChangeListener
     *
     * @param listener - the update listener
     */
    void removeMetaClassRegistryChangeEventListener(MetaClassRegistryChangeEventListener listener);

    /**
     * Returns all registered ConstantMetaClassChangeListener objects.
     *
     * @return an array containing all change listener
     */
    MetaClassRegistryChangeEventListener[] getMetaClassRegistryChangeEventListeners();

    /**
     * gets a snapshot of the current constant meta classes and returns it as Iterator.
     * Modifications done using this Iterator will not cause a ConcurrentMoidificationExcpetion.
     * If a MetaClass is removed using this Iterator, then the MetaClass will only
     * be removed if the MetaClass was not replaced by another MetaClass in the meantime.
     * If a MetaClass is added while using this Iterator, then it will be part of the Iteration.
     * If a MetaClass replaces another constant meta class, then the Iteration might show two
     * meta classes for the same class.<br/>
     * This Iterator may not used in multiple threads.
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
     * @author Jochen Theodorou
     */
    class MetaClassCreationHandle {
        public final MetaClass create(Class theClass, MetaClassRegistry registry) {
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
           } catch (final ClassNotFoundException e) {
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
    }

 }
