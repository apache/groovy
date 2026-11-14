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

/**
 * Call site for a Groovy object whose meta-class is cached and whose method is not.
 * Dispatch uses that meta-class while {@link #checkCall(Object)} still matches,
 * and the default call path otherwise. A missing method for the receiver's own
 * type is retried through {@link GroovyObject#invokeMethod(String, Object)}.
 */
public class PogoMetaClassSite extends MetaClassSite {

    /**
     * Caches {@code metaClass} for later dispatch from this site.
     */
    public PogoMetaClassSite(final CallSite site, final MetaClass metaClass) {
        super(site, metaClass);
    }

    /**
     * When {@link #checkCall(Object)} matches, invokes {@code name} on
     * {@code receiver} through the cached meta-class. A
     * {@link MissingMethodExecutionFailed} is rethrown as its cause. A
     * {@link MissingMethodException} for this name on the receiver's class
     * is retried with {@link GroovyObject#invokeMethod(String, Object)}.
     * A {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Otherwise falls back to {@link CallSiteArray#defaultCall(CallSite, Object, Object[])}.
     */
    @Override
    public final Object call(final Object receiver, final Object[] args) throws Throwable {
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

    /**
     * When {@link #checkCall(Object)} matches, invokes {@code name} through the
     * cached meta-class as a call from inside the call-site array's owner
     * ({@code isCallToSuper} false, {@code fromInsideClass} true).
     * A {@link MissingMethodExecutionFailed} is rethrown as its cause. A
     * {@link MissingMethodException} for this name on the receiver's class is
     * retried with {@link GroovyObject#invokeMethod(String, Object)}; if that
     * also misses and is not a {@link MissingMethodExecutionFailed}, the cached
     * meta-class is invoked again without the sender. A
     * {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Otherwise falls back to
     * {@link CallSiteArray#defaultCallCurrent(CallSite, GroovyObject, Object[])}.
     */
    @Override
    public final Object callCurrent(final GroovyObject receiver, final Object[] args) throws Throwable {
        if (checkCall(receiver)) {
            try {
                try {
                    return metaClass.invokeMethod(array.owner, receiver, name, args, false, true);
                } catch (MissingMethodException e) {
                    if (e instanceof MissingMethodExecutionFailed) {
                        throw (MissingMethodException) e.getCause();
                    } else if (receiver.getClass() == e.getType() && e.getMethod().equals(name)) {
                        // in case there's nothing else, invoke the receiver's own invokeMethod()
                        try {
                            return receiver.invokeMethod(name, args);
                        } catch (MissingMethodException mme) {
                            if (mme instanceof MissingMethodExecutionFailed)
                                throw (MissingMethodException) mme.getCause();
                            // GROOVY-9387: in rare cases, this form still works
                            return metaClass.invokeMethod(receiver, name, args);
                        }
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

    /**
     * Returns whether {@code receiver} is a {@link GroovyObject} whose current
     * meta-class is still the one cached by this site.
     */
    protected final boolean checkCall(final Object receiver) {
        return (receiver instanceof GroovyObject && ((GroovyObject) receiver).getMetaClass() == metaClass);
    }
}
