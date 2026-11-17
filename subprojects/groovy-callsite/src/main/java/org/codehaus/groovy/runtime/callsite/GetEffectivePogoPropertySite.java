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
import groovy.lang.MetaProperty;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
* User: applerestore
* Date: May 21, 2008
* Time: 10:37:37 AM
*/
class GetEffectivePogoPropertySite extends AbstractCallSite {
    private final MetaClass metaClass;
    private final MetaProperty effective;

    /**
     * Captures the meta class and property used when a later read still matches.
     *
     * @param site the site whose name and index are copied
     * @param metaClass the meta class the receiver must still report
     * @param effective the property read when the receiver still matches
     */
    public GetEffectivePogoPropertySite(CallSite site, MetaClass metaClass, MetaProperty effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective;
    }

    /**
     * Reads the property when {@code receiver} is a {@link GroovyObject} whose
     * meta class is still the one captured here and no category is active.
     * Otherwise a newly created get-property site performs the read.
     * A {@link GroovyRuntimeException} from the cached property is unwrapped.
     */
    @Override
    public final Object callGetProperty (Object receiver) throws Throwable {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver).getProperty(receiver);
        } else {
            try {
                return effective.getProperty(receiver);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        }
    }

    /**
     * Returns this site when {@code receiver} is a {@link GroovyObject} whose
     * meta class is still the one captured here and no category is active.
     * Otherwise returns a newly created get-property site.
     */
    @Override
    public final CallSite acceptGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver);
        } else {
            return this;
        }
    }

    /**
     * Reads the property under the same receiver and category checks as
     * {@link #callGetProperty(Object)}. A mismatch is handled by a newly
     * created get-property site. A {@link GroovyRuntimeException} from the
     * cached property is unwrapped.
     */
    @Override
    public final Object callGroovyObjectGetProperty (Object receiver) throws Throwable {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver).getProperty(receiver);
        } else {
            try {
                return effective.getProperty(receiver);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        }
    }

    /**
     * Returns this site under the same receiver and category checks as
     * {@link #acceptGetProperty(Object)}. Otherwise returns a site created
     * for a Groovy object property read.
     */
    @Override
    public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
            return createGroovyObjectGetPropertySite(receiver);
        } else {
            return this;
        }
    }

    /**
     * Reads the property through the cached {@link MetaProperty}.
     * A {@link GroovyRuntimeException} is unwrapped. The receiver's meta
     * class and any active category are not checked here.
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
