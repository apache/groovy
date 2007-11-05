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

/**
 * A MetaClassRegistry is an object that is responsible for managing the a cache of MetaClass instances. Each
 * java.lang.Class instance has an associated MetaClass and client code can query this interface for the MetaClass for
 * a given associated java.lang.Class
 *
 * @see groovy.lang.MetaClass
 *
 * @author John Wilson
 * @author Graeme Rocher
 *
 */
public interface MetaClassRegistry {
    /*
     * The main function of the Registry
     * If a Metaclass exists then return it
     * otherwise create one, put it in the Registry and return it
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
     * @param handle The hande instance
     */
    void setMetaClassCreationHandle(MetaClassCreationHandle handle);

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
                   final Constructor customMetaClassConstructor = customMetaClass.getConstructor(new Class[]{MetaClass.class});
                   MetaClass normalMetaClass = createNormalMetaClass(theClass, registry);
                   return (MetaClass)customMetaClassConstructor.newInstance(new Object[]{normalMetaClass});
               }
               else {
                   final Constructor customMetaClassConstructor = customMetaClass.getConstructor(new Class[]{MetaClassRegistry.class, Class.class});
                   return (MetaClass)customMetaClassConstructor.newInstance(new Object[]{registry, theClass});
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
