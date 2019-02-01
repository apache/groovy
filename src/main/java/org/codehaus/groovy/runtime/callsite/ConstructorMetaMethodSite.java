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
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site for invoking static methods
*   meta class  - cached
*   method - not cached
*/
public class ConstructorMetaMethodSite extends MetaMethodSite {

    private final int version;

    public ConstructorMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod method, Class [] params) {
        super(site, metaClass, method, params);
        this.version = metaClass.getVersion();
    }

    public final Object invoke(Object receiver, Object [] args) throws Throwable{
        MetaClassHelper.unwrap(args);
        try {
            return metaMethod.doMethodInvoke(metaClass.getTheClass(), args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, args) )  
        {
            MetaClassHelper.unwrap(args);
            try {
                return metaMethod.doMethodInvoke(metaClass.getTheClass(), args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
          return CallSiteArray.defaultCallConstructor(this, receiver, args);
        }
    }
}
