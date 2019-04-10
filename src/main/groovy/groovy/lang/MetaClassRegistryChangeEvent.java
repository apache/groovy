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

import java.util.EventObject;

/**
 * An event used to propagate meta class updates
 */
public class MetaClassRegistryChangeEvent extends EventObject {
    private static final long serialVersionUID = 1647849176793457976L;
    private final Class clazz;
    private final Object instance;
    private final MetaClass metaClass;
    private final MetaClass oldMetaClass;

    /**
     *Constructs a new MetaClassRegistryChangeEvent Object
     *
     * @param source The object the event originates at.
     * @param instance Object instance  the MetaClass change is on.
     * @param clazz  The class that is affected by the registry change
     * @param oldMetaClass The old MetaClass
     * @param newMetaClass The new MetaClass
     */
    public MetaClassRegistryChangeEvent(Object source, Object instance, Class clazz, MetaClass oldMetaClass, MetaClass newMetaClass) {
        super(source);
        this.clazz = clazz;
        this.metaClass = newMetaClass;
        this.oldMetaClass = oldMetaClass;
        this.instance = instance;
    }

    /**
     * Get the class that is updated.
     *
     *@return The updated class
     */
    public Class getClassToUpdate() {
        return clazz;
    }

    /**
     * Get the new MetaClass
     *
     * @return The new MetaClass
     */
    public MetaClass getNewMetaClass() {
        return metaClass;
    }

    /**
     * Get the old MetaClass
     *
     * @return The old MetaClass
     */
    public MetaClass getOldMetaClass() {
        return oldMetaClass;
    }

    /**
     * Determines if this event is for a change for a single instance or all instances of the Class.
     *
     * @return whether this event is for a single instance
     */
    public boolean isPerInstanceMetaClassChange() {
        return instance!=null;
    }

    /**
     * Returns the instance this event is for.
     *
     * @return the instance or null if this event is for a change for all instances of a class
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * Get the MetaClassRegistry that originates this change
     *
     * @return the source MetaClassRegistry
     */
    public MetaClassRegistry getRegistry() {
        return (MetaClassRegistry) source;
    }
}
