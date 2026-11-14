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

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site that reads a property from a {@link GroovyObject} whose runtime
 * class is the class captured when the site was built.
 */
public class PogoGetPropertySite extends AbstractCallSite {
    private final Class aClass;

    /**
     * Copies the parent site and remembers the receiver class it handles.
     *
     * @param parent the site whose name and index are copied
     * @param aClass the runtime class this site accepts
     */
    public PogoGetPropertySite(CallSite parent, Class aClass) {
        super(parent);
        this.aClass = aClass;
    }

    /**
     * Returns a newly created get-property site when {@code receiver} is
     * null or its runtime class is not the class captured here. Otherwise
     * returns this site.
     */
    @Override
    public CallSite acceptGetProperty(Object receiver) {
        if (receiver== null || receiver.getClass() != aClass)
            return createGetPropertySite(receiver);
        else
          return this;
    }

    /**
     * Returns a site created for a Groovy object property read when
     * {@code receiver} is null or its runtime class is not the class
     * captured here. Otherwise returns this site.
     */
    @Override
    public CallSite acceptGroovyObjectGetProperty(Object receiver) {
        if (receiver == null || receiver.getClass() != aClass)
            return createGroovyObjectGetPropertySite(receiver);
        else
          return this;
    }

    /**
     * Reads this site's property name through {@link GroovyObject#getProperty(String)}.
     * A {@link GroovyRuntimeException} is unwrapped.
     */
    @Override
    public Object getProperty(Object receiver) throws Throwable {
        try{
            return ((GroovyObject)receiver).getProperty(name);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
