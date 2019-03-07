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
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;

public class PogoMetaClassSite extends MetaClassSite {
    public PogoMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public final Object call(Object receiver, Object[] args) throws Throwable {
        if (checkCall(receiver)) {
            try {
                try {
                    return metaClass.invokeMethod(receiver, name, args);
                } catch (MissingMethodException e) {
                    if (e instanceof MissingMethodExecutionFailed) {
                        throw (MissingMethodException)e.getCause();
                    } else if (receiver.getClass() == e.getType() && e.getMethod().equals(name)) {
                        // in case there's nothing else, invoke the object's own invokeMethod()
                        return ((GroovyObject)receiver).invokeMethod(name, args);
                    } else {
                        throw e;
                    }
                }
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
          return CallSiteArray.defaultCall(this, receiver, args);
        }
    }

    protected final boolean checkCall(Object receiver) {
        return receiver instanceof GroovyObject && ((GroovyObject)receiver).getMetaClass() == metaClass;
    }

    public final Object callCurrent(GroovyObject receiver, Object[] args) throws Throwable {
        if (checkCall(receiver)) {
            try {
                try {
                    return metaClass.invokeMethod(array.owner, receiver, name, args, false, true);
                } catch (MissingMethodException e) {
                    if (e instanceof MissingMethodExecutionFailed) {
                        throw (MissingMethodException)e.getCause();
                    } else if (receiver.getClass() == e.getType() && e.getMethod().equals(name)) {
                        // in case there's nothing else, invoke the object's own invokeMethod()
                        return ((GroovyObject)receiver).invokeMethod(name, args);
                    } else {
                        throw e;
                    }
                }
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
          return CallSiteArray.defaultCallCurrent(this, receiver, args);
        }
    }
}
