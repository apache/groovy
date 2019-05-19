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

import groovy.lang.MetaClassRegistry.MetaClassCreationHandle;
import org.codehaus.groovy.reflection.ClassInfo;

/**
 * <p>A handle for the MetaClassRegistry that changes all classes loaded into the Grails VM
 * to use ExpandoMetaClass instances
 *
 * <p>The handle should be registered with the Groovy runtime <strong>before</strong> Groovy loads, for example
 * in your main method.
 *
 * <code>GroovySystem.metaClassRegistry.metaClassCreationHandle = new ExpandoMetaClassCreationHandle()</code>
 *
 * @see groovy.lang.MetaClassRegistry
 * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle
 * @see org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl#setMetaClassCreationHandle(groovy.lang.MetaClassRegistry.MetaClassCreationHandle)
 * @since 1.5
 */
public class ExpandoMetaClassCreationHandle extends MetaClassCreationHandle {

    public static final ExpandoMetaClassCreationHandle instance = new ExpandoMetaClassCreationHandle();

    /* (non-Javadoc)
     * @see groovy.lang.MetaClassRegistry.MetaClassCreationHandle#create(java.lang.Class, groovy.lang.MetaClassRegistry)
     */
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if(theClass != ExpandoMetaClass.class) {
            return new ExpandoMetaClass(theClass, true, true);
        }
        else {
            return super.createNormalMetaClass(theClass, registry);
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
        synchronized (metaClassRegistry) {
            if (metaClassRegistry.getMetaClassCreationHandler() != instance) {
                ClassInfo.clearModifiedExpandos();
                metaClassRegistry.setMetaClassCreationHandle(instance);
            }
        }
    }

    public static void disable() {
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        synchronized (metaClassRegistry) {
            if (metaClassRegistry.getMetaClassCreationHandler() == instance) {
                ClassInfo.clearModifiedExpandos();
                metaClassRegistry.setMetaClassCreationHandle(new MetaClassCreationHandle());
            }
        }
    }
}
