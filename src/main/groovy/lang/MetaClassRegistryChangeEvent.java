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

import java.util.EventObject;

/**
 * An event used to propagate meta class updates
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 */
public class MetaClassRegistryChangeEvent extends EventObject {
    private final Class clazz;
    private final Object instance;
    private final MetaClass metaClass;
    private final MetaClass oldMetaClass;

    public MetaClassRegistryChangeEvent(Object source, Object instance, Class clazz, MetaClass oldMetaClass, MetaClass newMetaClass) {
        super(source);
        this.clazz = clazz;
        this.metaClass = newMetaClass;
        this.oldMetaClass = oldMetaClass;
        this.instance = instance;
    }

    public Class getClassToUpdate() {
        return clazz;
    }

    public MetaClass getNewMetaClass() {
        return metaClass;
    }

    public MetaClass getOldMetaClass() {
        return oldMetaClass;
    }

    public boolean isPerInstanceMetaClassChange() {
        return instance!=null;
    }

    public Object getInstance() {
        return instance;
    }

    public MetaClassRegistry getRegistry() {
        return (MetaClassRegistry) source;
    }
}
