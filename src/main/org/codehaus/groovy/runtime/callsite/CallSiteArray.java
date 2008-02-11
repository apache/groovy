/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.callsite;

/**
 * All call site calls done via CallSiteArray
 * Groovy compiler creates static CallSiteArray field for each compiled class
 * One index in array correspond to one method or constructor call (non-spreaded, spreded ones dispatched regular way)
 *
 * CallSiteArray has several methods of the same type (call, callSafe, callCurrent, callStatic and callConstructor)
 * Each method does more or less the same
 * - ask if existing site is valid for receiver and arguments
 * - if necessary create new site and replace existing one
 * - ask call site to make the call
 *
 * @author Alex Tkachman
 */
public final class CallSiteArray {
    public final CallSite[] array;

    public static final Object [] NOPARAM = new Object[0];
    final Class owner;

    public CallSiteArray(Class owner, String [] names) {
        this.owner = owner;
        array = new CallSite[names.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = new CallSite.DummyCallSite(this, i, names[i]);
        }
    }
}
