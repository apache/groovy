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

import groovy.lang.MetaClassRegistry.MetaClassCreationHandle;
import org.codehaus.groovy.runtime.metaclass.ConcurrentReaderHashMap;

import java.util.*;

/**
 * <p>A handle for the MetaClassRegistry that changes all classes loaded into the Grails VM
 * to use ExpandoMetaClass instances
 *
 * <p>The handle should be registered with the Groovy runtime <strong>before</before> Groovy loads, for example
 * in your main method.
 *
 * <code>GroovySystem.metaClassRegistry.metaClassCreationHandle = new ExpandoMetaClassCreationHandle()</code>
 *
 * @see groovy.lang.MetaClassRegistry
 * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle
 * @see org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl#setMetaClassCreationHandle(groovy.lang.MetaClassRegistry.MetaClassCreationHandle)
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class ExpandoMetaClassCreationHandle extends MetaClassCreationHandle {

    public static final ExpandoMetaClassCreationHandle instance = new ExpandoMetaClassCreationHandle();

	private final Map modifiedExpandos = new ConcurrentReaderHashMap();


	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle#create(java.lang.Class, groovy.lang.MetaClassRegistry)
	 */
	protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
		if(theClass != ExpandoMetaClass.class) {
			ExpandoMetaClass emc = new ExpandoMetaClass(theClass, false ,true);
			Set modifiedSuperExpandos = retrieveModifiedSuperExpandos(emc);
            emc.refreshInheritedMethods(modifiedSuperExpandos);
			return emc;
		}
		else {
			return super.create(theClass, registry);
		}
	}

    /*
     * Looks for modified super class ExpandoMetaClass instances for the given child ExpandoMetaClass
     */
    private Set retrieveModifiedSuperExpandos(ExpandoMetaClass child) {
		Set modifiedSupers = new HashSet();
		List superClasses = child.getSuperClasses();
		for (Iterator i = superClasses.iterator(); i.hasNext();) {
			Class c = (Class) i.next();
            Class[] interfaces = c.getInterfaces();
            populateSupersFromInterfaces(modifiedSupers, interfaces);
            if(modifiedExpandos.containsKey(c)) {
				modifiedSupers.add(modifiedExpandos.get(c));
			}
		}
        Class[] interfaces = child.getJavaClass().getInterfaces();
        populateSupersFromInterfaces(modifiedSupers, interfaces);
        return modifiedSupers;
	}

    /*
     * Searches through a given array of interfaces for modified ExpandoMetaClass instances for each interface
     */
    private void populateSupersFromInterfaces(Set modifiedSupers, Class[] interfaces) {
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            final Class[] superInterfaces = anInterface.getInterfaces();
            if(modifiedExpandos.containsKey(anInterface)) {
                modifiedSupers.add(modifiedExpandos.get(anInterface));
            }
            if(superInterfaces.length > 0)
                populateSupersFromInterfaces(modifiedSupers, superInterfaces);
        }
    }


    /**
     * Registers a modified ExpandoMetaClass with the creation handle
     *
     * @param emc The EMC
     */
    public void registerModifiedMetaClass(ExpandoMetaClass emc) {
        modifiedExpandos.put(emc.getJavaClass(), emc);
	}

	public boolean hasModifiedMetaClass(ExpandoMetaClass emc) {
		return modifiedExpandos.containsKey(emc.getJavaClass());
	}

    /**
     * <p>Enables the ExpandoMetaClassCreationHandle with the registry
     *
     * <code>ExpandoMetaClassCreationHandle.enable();</code>
     *
     */
    public static void enable() {
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        if (metaClassRegistry.getMetaClassCreationHandler() != instance) {
           instance.modifiedExpandos.clear();
           metaClassRegistry.setMetaClassCreationHandle(instance);
        }
    }

    public static void disable() {
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        if (metaClassRegistry.getMetaClassCreationHandler() == instance) {
            instance.modifiedExpandos.clear();
            metaClassRegistry.setMetaClassCreationHandle(new MetaClassCreationHandle());
        }
    }
}
