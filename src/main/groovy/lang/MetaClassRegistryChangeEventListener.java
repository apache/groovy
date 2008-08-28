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

import java.util.EventListener;

/**
 * A listener called whenever a constant MetaClass is set, removed or replaced. 
 *
 * @see groovy.lang.MetaClassRegistry
 * @see groovy.lang.MetaClassRegistryChangeEvent
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
  *
 */
public interface MetaClassRegistryChangeEventListener extends EventListener{

    /**
     * Called when the a constant MetaClass is updated. If the new MetaClass is null, then the MetaClass
     * is removed. Be careful, while this method is executed other updates may happen. If you want this
     * method thread safe, you have to take care of that by yourself.
     *
     * @param cmcu - the change event
     */
    void updateConstantMetaClass(MetaClassRegistryChangeEvent cmcu);
}
