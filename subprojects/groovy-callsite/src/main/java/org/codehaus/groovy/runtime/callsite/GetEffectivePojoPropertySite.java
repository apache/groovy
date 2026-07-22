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

public class GetEffectivePojoPropertySite extends AbstractCallSite {
    private final MetaClassImpl metaClass;
    private final MetaProperty effective;
    private final int version;

    public GetEffectivePojoPropertySite(final CallSite site, final MetaClassImpl metaClass, final MetaProperty effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective;
        version = metaClass.getVersion();
    }

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

    @Override
    public final Object getProperty(Object receiver) throws Throwable {
        try {
            return effective.getProperty(receiver);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }
}
