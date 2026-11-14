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
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * POJO call site
 *   metaclass - cached
 *   method - not cached
 */
public class PojoMetaClassSite extends MetaClassSite {

    private final ClassInfo classInfo;
    private final int version;

    /**
     * Caches {@code metaClass} and the {@link ClassInfo} version of its class.
     */
    public PojoMetaClassSite(final CallSite site, final MetaClass metaClass) {
        super(site, metaClass);
        classInfo = ClassInfo.getClassInfo(metaClass.getTheClass());
        version = classInfo.getVersion();
    }

    /**
     * When {@link #checkCall(Object)} matches, invokes {@code name} through the
     * cached meta-class. A {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Otherwise falls back to {@link CallSiteArray#defaultCall(CallSite, Object, Object[])}.
     */
    @Override
    public Object call(final Object receiver, final Object[] args) throws Throwable {
        if (checkCall(receiver)) {
            try {
                return metaClass.invokeMethod(receiver, name, args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCall(this, receiver, args);
        }
    }

    /**
     * Returns whether {@code receiver} is non-null, its runtime class is the
     * meta-class's class, and that class's {@link ClassInfo} version is still
     * the version recorded when this site was created.
     */
    protected final boolean checkCall(final Object receiver) {
        return receiver != null
            && receiver.getClass() == metaClass.getTheClass()
            && version == classInfo.getVersion(); // metaClass is still valid
    }
}
