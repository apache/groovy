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

import java.lang.reflect.Method;

/**
 * An interface that defines methods that implementers of mutable Meta classes should specify. It provides operations to perform mutations
 * on the MetaClass instance.
 * <p>
 * Whether a MetaClass allows mutation is up to the MetaClass itself and considerations of Thread safety
 * need to be taken into account when making a MetaClass mutable
 * <p>
 * The default implementation allows mutation of MetaClass instances before initialisation (before the initialize() method is called)
 * but not after, thus ensuring Thread safety once a MetaClass has been constructed and placed in the registry
 *
 * @see MetaClassImpl
 * @see MetaClass
 * @since 1.5
 */
public interface MutableMetaClass extends MetaClass {

    /**
     * Return whether the MetaClass has been modified or not
     * @return True if it has
     */
    boolean isModified();

    /**
     * adds a new instance method to this MetaClass. Instance
     * methods are able to overwrite the original methods of the
     * class. Calling this method should not be done after
     * initialise was called.
     *
     * @param method the method to be added
     */
     void addNewInstanceMethod(Method method);

    /**
     * adds a new static method to this MetaClass. This is only
     * possible as long as initialise was not called.
     *
     * @param method the method to be added
     */
     void addNewStaticMethod(Method method);

    /**
     * Adds a new MetaMethod to the MetaClass
     *
     * @param metaMethod The MetaMethod to add
     */
    void addMetaMethod(MetaMethod metaMethod);

    /**
     * Adds a new MetaBeanProperty to the MetaClass
     *
     * @param metaBeanProperty The MetaBeanProperty instance
     */
    void addMetaBeanProperty(MetaBeanProperty metaBeanProperty);
    
    // TODO: Add methods like addMetaConstructor, addMetaAttribute, addMetaAnnotation etc.
}
