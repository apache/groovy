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
import org.codehaus.groovy.runtime.metaclass.MemoryAwareConcurrentReadMap;

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

	private final Map modifiedExpandos = new HashMap();
	private final MemoryAwareConcurrentReadMap parentClassToChildMap = new MemoryAwareConcurrentReadMap();

	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle#create(java.lang.Class, groovy.lang.MetaClassRegistry)
	 */
	public MetaClass create(Class theClass, MetaClassRegistry registry) {
		if(theClass != ExpandoMetaClass.class) {
			ExpandoMetaClass emc = new ExpandoMetaClass(theClass);
			emc.setAllowChangesAfterInit(true);
			Set modifiedSuperExpandos = retrieveModifiedSuperExpandos(emc);

            emc.refreshInheritedMethods(modifiedSuperExpandos);
			emc.initialize();

            registerTrackedExpando(emc);
			return emc;
		}
		else {
			return super.create(theClass, registry);
		}
	}

	private void registerTrackedExpando(ExpandoMetaClass emc) {
        LinkedList superClassList = emc.getSuperClasses();
        Class[] superClasses = (Class[])superClassList.toArray(new Class[superClassList.size()]);
        for (int i = 0; i < superClasses.length; i++) {
            Class c = superClasses[i];
            registerWithParentToChildMap(emc, c);
        }
        Class[] interfaces = emc.getJavaClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            registerWithParentToChildMap(emc, interfaces[i]);
        }

    }

    private void registerWithParentToChildMap(ExpandoMetaClass emc, Class c) {
        synchronized(this) {
            Set children = (Set)parentClassToChildMap.get(c);
            if(children == null) {
                children = new HashSet();
                    parentClassToChildMap.put(c, children);
                }

            children.add(emc);
        }
    }

    /**
     * Notifies child classes or interface implementors when a parent class or interface changes
     *
     * @param changed The changed MetaClass
     */
    public void notifyOfMetaClassChange(ExpandoMetaClass changed) {
		Set subMetas = retrieveKnownSubclasses(changed);
		if(subMetas != null) {
			for (Iterator i = subMetas.iterator(); i.hasNext();) {
				ExpandoMetaClass child = (ExpandoMetaClass) i.next();

				Set modifiedSuperExpandos = retrieveModifiedSuperExpandos(child);
				child.refreshInheritedMethods(modifiedSuperExpandos);
			}
		}
	}

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

    private void populateSupersFromInterfaces(Set modifiedSupers, Class[] interfaces) {
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if(modifiedExpandos.containsKey(anInterface)) {
                modifiedSupers.add(modifiedExpandos.get(anInterface));
            }
        }
    }

    private Set retrieveKnownSubclasses(ExpandoMetaClass changed) {
		return (Set)parentClassToChildMap.get(changed.getJavaClass());
	}

    /**
     * Registers a modified ExpandoMetaClass with the creation handle
     *
     * @param emc The EMC
     */
    public void registerModifiedMetaClass(ExpandoMetaClass emc) {
        synchronized(this) {
            modifiedExpandos.put(emc.getJavaClass(), emc);
        }
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
        GroovySystem.getMetaClassRegistry().setMetaClassCreationHandle(new ExpandoMetaClassCreationHandle());
    }
}
