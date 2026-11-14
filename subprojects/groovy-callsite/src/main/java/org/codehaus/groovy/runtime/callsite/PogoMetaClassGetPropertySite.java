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
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site that reads a property through a {@link GroovyObject}'s
 * {@link MetaClass} while that meta class is still the one captured here.
 */
public class PogoMetaClassGetPropertySite extends AbstractCallSite {
    private final MetaClass metaClass;

    /**
     * Copies the parent site and remembers the meta class used for property reads.
     *
     * @param parent the site whose name and index are copied
     * @param metaClass the meta class this site accepts
     */
    public PogoMetaClassGetPropertySite(CallSite parent, MetaClass metaClass) {
        super(parent);
        this.metaClass = metaClass;
    }

    @Override
    /**
     * Returns a newly created get-property site when {@code receiver} is not a
     * {@link GroovyObject} or its meta class is not the one captured here.
     * Otherwise returns this site.
     */
    public final CallSite acceptGetProperty(Object receiver) {
        if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
            return createGetPropertySite(receiver);
        else
          return this;
    }

    @Override
    /**
     * Returns a newly created Groovy-object get-property site when
     * {@code receiver} is not a {@link GroovyObject} or its meta class is not
     * the one captured here. Otherwise returns this site.
     */
    public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
        if (!(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass)
            return createGroovyObjectGetPropertySite(receiver);
        else
          return this;
    }

    @Override
    /**
     * Reads this site's property through the captured meta class.
     * A {@link GroovyRuntimeException} is unwrapped.
     */
    public final Object getProperty(Object receiver) throws Throwable {
        try {
            return metaClass.getProperty(receiver, name);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
