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
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaProperty;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site that reads a POJO property through a cached {@link MetaProperty}
 * while the {@link MetaClassImpl} version is unchanged and no category is active.
 */
public class GetEffectivePojoPropertySite extends AbstractCallSite {
    private final MetaClassImpl metaClass;
    private final MetaProperty effective;
    private final int version;

    /**
     * Captures the meta class, the property to read, and the meta class version.
     *
     * @param site the site whose name and index are copied
     * @param metaClass the meta class this site was built from
     * @param effective the property read when the receiver still matches
     */
    public GetEffectivePojoPropertySite(final CallSite site, final MetaClassImpl metaClass, final MetaProperty effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective;
        version = metaClass.getVersion();
    }

    /**
     * Returns a newly created get-property site when {@code receiver} is null,
     * its class is not the meta class's class, the meta class version has
     * changed, or a category is active on the current thread. Otherwise
     * returns this site.
     */
    @Override
    public final CallSite acceptGetProperty(final Object receiver) {
        if (receiver == null || receiver.getClass() != metaClass.getTheClass()
                || version != metaClass.getVersion() // metaClass is invalid
                || GroovyCategorySupport.hasCategoryInCurrentThread()) {
            return createGetPropertySite(receiver);
        } else {
            return this;
        }
    }

    /**
     * Reads the property through the cached {@link MetaProperty}.
     * A {@link GroovyRuntimeException} is unwrapped.
     */
    @Override
    public final Object getProperty(Object receiver) throws Throwable {
        try {
            return effective.getProperty(receiver);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
