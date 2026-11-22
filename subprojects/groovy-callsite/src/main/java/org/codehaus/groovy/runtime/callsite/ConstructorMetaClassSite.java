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
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site that invokes a constructor through a {@link MetaClass} when
 * the receiver is that meta class's class.
 */
public class ConstructorMetaClassSite extends MetaClassSite {
    /**
     * Copies the parent site and the meta class used for construction.
     *
     * @param site the site whose name and index are copied
     * @param metaClass the meta class whose class this site accepts as the receiver
     */
    public ConstructorMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    /**
     * Invokes the meta class constructor when {@code receiver} is that meta
     * class's class. A {@link GroovyRuntimeException} is unwrapped. Any other
     * receiver is handled by the default constructor call.
     */
    @Override
    public Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (receiver == metaClass.getTheClass()) {
            try {
                return metaClass.invokeConstructor(args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
          return CallSiteArray.defaultCallConstructor(this, receiver, args);
        }
    }
}
