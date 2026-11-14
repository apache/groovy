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

import groovy.lang.MetaClassImpl;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

class GetEffectivePojoFieldSite extends AbstractCallSite {
    private final MetaClassImpl metaClass;
    private final CachedField effective;
    private final int metaClassVersion;

    /**
     * Captures the meta class, the field to read, and the meta class version.
     *
     * @param site the site whose name and index are copied
     * @param metaClass the meta class this site was built from
     * @param effective the field read when the receiver still matches
     */
    public GetEffectivePojoFieldSite(final CallSite site, final MetaClassImpl metaClass, final CachedField effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective;
        this.metaClassVersion = metaClass.getVersion();
    }

    /**
     * Returns a newly created get-property site when a category is active on
     * the current thread, the receiver's class is not the meta class's class,
     * or the meta class version has changed. Otherwise returns this site.
     */
    @Override
    public final CallSite acceptGetProperty(final Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread()
            || metaClass.getTheClass() != receiver.getClass()
            || metaClass.getVersion() != metaClassVersion) {

            return createGetPropertySite(receiver);
        }
        return this;
    }

    /**
     * Returns the cached field's value for {@code receiver}.
     */
    @Override
    public final Object getProperty(final Object receiver) {
        return effective.getProperty(receiver);
    }
}
