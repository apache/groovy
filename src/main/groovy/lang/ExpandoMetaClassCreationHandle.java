/*
 * Copyright 2004-2006 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang;

import groovy.lang.MetaClassRegistry.MetaClassCreationHandle;
import org.apache.commons.collections.map.ReferenceMap;

import java.util.*;

/**
 * A handle for the MetaClassRegistry that changes all classes loaded into the Grails VM
 * to use ExpandoMetaClass instances
 *
 * @author Graeme Rocher
 * @since 0.4
 */
public class ExpandoMetaClassCreationHandle extends MetaClassCreationHandle {

	private static Map modifiedExpandos = Collections.synchronizedMap(new HashMap());
	private static Map parentClassToChildMap = Collections.synchronizedMap(new ReferenceMap(ReferenceMap.SOFT, ReferenceMap.SOFT));

	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle#create(java.lang.Class, groovy.lang.MetaClassRegistry)
	 */
	public MetaClass create(Class theClass, MetaClassRegistry registry) {
		if(theClass != ExpandoMetaClass.class) {
			ExpandoMetaClass emc = new ExpandoMetaClass(theClass);
			emc.setAllowChangesAfterInit(true);
			List modifiedSuperExpandos = retrieveModifiedSuperExpandos(emc);
			emc.refreshInheritedMethods(modifiedSuperExpandos);

			registerTrackedExpando(emc);
			return emc;
		}
		else {
			return super.create(theClass, registry);
		}
	}

	private void registerTrackedExpando(ExpandoMetaClass emc) {
		List superClasses = emc.getSuperClasses();
		for (Iterator i = superClasses.iterator(); i.hasNext();) {
			Class c = (Class) i.next();
			Set children = (Set)parentClassToChildMap.get(c);
			if(children == null) {
				children = new HashSet();
				parentClassToChildMap.put(c, children);
			}
			children.add(emc);

		}
	}

	public void notifyOfMetaClassChange(ExpandoMetaClass changed) {
		Set subMetas = retrieveKnownSubclasses(changed);
		if(subMetas != null) {
			for (Iterator i = subMetas.iterator(); i.hasNext();) {
				ExpandoMetaClass child = (ExpandoMetaClass) i.next();

				List modifiedSuperExpandos = retrieveModifiedSuperExpandos(child);
				child.refreshInheritedMethods(modifiedSuperExpandos);
			}
		}
	}

	private List retrieveModifiedSuperExpandos(ExpandoMetaClass child) {
		List modifiedSupers = new LinkedList();
		List superClasses = child.getSuperClasses();
		for (Iterator i = superClasses.iterator(); i.hasNext();) {
			Class c = (Class) i.next();
			if(modifiedExpandos.containsKey(c)) {
				modifiedSupers.add(modifiedExpandos.get(c));
			}
		}
		return modifiedSupers;
	}

	private static Set retrieveKnownSubclasses(ExpandoMetaClass changed) {
		return (Set)parentClassToChildMap.get(changed.getJavaClass());
	}

	public void registerModifiedMetaClass(ExpandoMetaClass emc) {
		modifiedExpandos.put(emc.getJavaClass(), emc);
	}

	public boolean hasModifiedMetaClass(ExpandoMetaClass emc) {
		return modifiedExpandos.containsKey(emc.getJavaClass());
	}

}
