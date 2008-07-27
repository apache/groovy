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
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;

import java.util.HashSet;
import java.util.Set;

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

	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle#create(java.lang.Class, groovy.lang.MetaClassRegistry)
	 */
	protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
		if(theClass != ExpandoMetaClass.class) {
			ExpandoMetaClass emc = new ExpandoMetaClass(theClass, false ,true);
//			Set modifiedSuperExpandos = retrieveModifiedSuperExpandos(emc);
//            emc.refreshInheritedMethods(modifiedSuperExpandos);
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
		Set<MetaClass> modifiedSupers = new HashSet<MetaClass>();
        for (CachedClass c : child.getSuperClasses()) {
            Set<CachedClass> interfaces = c.getInterfaces();
            populateSupersFromInterfaces(modifiedSupers, interfaces);
            final ClassInfo info = c.classInfo;
            final ExpandoMetaClass expando = info.getModifiedExpando();
            if(expando != null) {
				modifiedSupers.add(expando);
			}
		}
        Set<CachedClass> interfaces = child.getTheCachedClass().getDeclaredInterfaces();
        populateSupersFromInterfaces(modifiedSupers, interfaces);
        return modifiedSupers;
	}

    /*
     * Searches through a given array of interfaces for modified ExpandoMetaClass instances for each interface
     */
    private void populateSupersFromInterfaces(Set<MetaClass> modifiedSupers, Set<CachedClass> interfaces) {
        for (CachedClass anInterface : interfaces) {
            final ClassInfo info = anInterface.classInfo;
            final ExpandoMetaClass expando = info.getModifiedExpando();
            if(expando != null) {
				modifiedSupers.add(expando);
			}

            final Set<CachedClass> superInterfaces = anInterface.getDeclaredInterfaces();
            if(superInterfaces.size() > 0)
                populateSupersFromInterfaces(modifiedSupers, superInterfaces);
        }
    }


    /**
     * Registers a modified ExpandoMetaClass with the creation handle
     *
     * @param emc The EMC
     */
    public void registerModifiedMetaClass(ExpandoMetaClass emc) {
        final Class klazz = emc.getJavaClass();
        GroovySystem.getMetaClassRegistry().setMetaClass(klazz,emc);
    }

	public boolean hasModifiedMetaClass(ExpandoMetaClass emc) {
		return emc.getClassInfo().getModifiedExpando() != null;
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
            ClassInfo.clearModifiedExpandos();
            metaClassRegistry.setMetaClassCreationHandle(instance);
        }
    }

    public static void disable() {
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        if (metaClassRegistry.getMetaClassCreationHandler() == instance) {
            ClassInfo.clearModifiedExpandos();
            metaClassRegistry.setMetaClassCreationHandle(new MetaClassCreationHandle());
        }
    }
}
