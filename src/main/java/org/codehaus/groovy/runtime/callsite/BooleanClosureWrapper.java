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

import groovy.lang.Closure;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper class for internal use only. This allows to call a {@link Closure} and 
 * convert the result to a boolean. It will do this by caching the possible "doCall"
 * as well as the "asBoolean" in CallSiteArray fashion. "asBoolean" will not be 
 * called if the result is null or a Boolean. In case of null we return false and
 * in case of a Boolean we simply unbox. This logic is designed after the one present 
 * in {@link org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation#castToBoolean(Object)}. The purpose of
 * this class is to avoid the slow "asBoolean" call in that method.
 * {@link BooleanReturningMethodInvoker} is used for caching.
 *
 */
public class BooleanClosureWrapper {
    private final BooleanReturningMethodInvoker bmi;
    private final Closure wrapped;
    private final int numberOfArguments;
    
    public BooleanClosureWrapper(Closure wrapped) {
        this.wrapped = wrapped;
        this.bmi = new BooleanReturningMethodInvoker("call");
        numberOfArguments = wrapped.getMaximumNumberOfParameters();
    }

    /**
     * normal closure call
     */
    public boolean call(Object... args) {
        return bmi.invoke(wrapped, args);
    }
    
    /**
     * Bridge for a call based on a map entry. If the call is done on a {@link Closure}
     * taking one argument, then we give in the {@link Entry}, otherwise we will
     * give in the key and value.
     */
    public <K,V> boolean callForMap(Map.Entry<K, V> entry) {
        if (numberOfArguments==2) {
            return call(entry.getKey(), entry.getValue());
        } else {
            return call(entry);
        }
    }
}
