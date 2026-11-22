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
import groovy.lang.MetaClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

/**
 * Call site that reads a field through a cached {@link CachedField} while the
 * receiver is a {@link GroovyObject} whose meta class is the one captured
 * here and no category is active on the current thread.
 */
public class GetEffectivePogoFieldSite extends AbstractCallSite {
    private final MetaClass metaClass;
    private final CachedField effective;

    /**
     * Copies the parent site and remembers the meta class and field to read.
     *
     * @param site the site whose name and index are copied
     * @param metaClass the meta class this site was built from
     * @param effective the field read when the receiver still matches
     */
    public GetEffectivePogoFieldSite(final CallSite site, final MetaClass metaClass, final CachedField effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective;
    }

    /**
     * Returns a newly created get-property site when {@code receiver} is not a
     * {@link GroovyObject} with {@code metaClass}, or a category is active.
     * Otherwise returns this site.
     */
    @Override
    public final CallSite acceptGetProperty(final Object receiver) {
        return isEffective(receiver) ? this : createGetPropertySite(receiver);
    }

    /**
     * Returns a newly created Groovy-object get-property site when
     * {@code receiver} is not a {@link GroovyObject} with {@code metaClass},
     * or a category is active. Otherwise returns this site.
     */
    @Override
    public final CallSite acceptGroovyObjectGetProperty(final Object receiver) {
        return isEffective(receiver) ? this : createGroovyObjectGetPropertySite(receiver);
    }

    /**
     * Reads the cached field when {@code receiver} still matches this site.
     * Otherwise creates a get-property site and reads through that.
     */
    @Override
    public final Object callGetProperty(final Object receiver) throws Throwable {
        return isEffective(receiver) ? getProperty(receiver) : createGetPropertySite(receiver).getProperty(receiver);
    }

    /**
     * Reads the cached field when {@code receiver} still matches this site.
     * Otherwise creates a Groovy-object get-property site and reads through that.
     */
    @Override
    public final Object callGroovyObjectGetProperty (final Object receiver) throws Throwable {
        return isEffective(receiver) ? getProperty(receiver) : createGroovyObjectGetPropertySite(receiver).getProperty(receiver);
    }

    /**
     * Reads the field through the cached {@link CachedField}.
     */
    @Override
    public final Object getProperty(final Object receiver) {
        return effective.getProperty(receiver);
    }

    private boolean isEffective(final Object receiver) {
        return !GroovyCategorySupport.hasCategoryInCurrentThread() && receiver instanceof GroovyObject && ((GroovyObject) receiver).getMetaClass() == metaClass;
    }
}
