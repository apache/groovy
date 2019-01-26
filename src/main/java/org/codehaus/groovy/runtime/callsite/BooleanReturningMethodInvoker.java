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

import org.apache.groovy.internal.util.UncheckedThrow;

/**
 * Helper class for internal use only. This allows to call a given method and
 * convert the result to a boolean. It will do this by caching the method call
 * as well as the "asBoolean" in {@link CallSiteArray} fashion. "asBoolean" will not be 
 * called if the result is null or a Boolean. In case of null we return false and
 * in case of a Boolean we simply unbox. This logic is designed after the one present 
 * in {@link org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation#castToBoolean(Object)}. The purpose of
 * this class is to avoid the slow "asBoolean" call in that method. 
 * 
 * The nature of this class allows a per instance caching instead of a per class
 * caching like the normal {@link CallSiteArray} logic.
 * 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class BooleanReturningMethodInvoker {
    private final CallSiteArray csa;
    
    public BooleanReturningMethodInvoker() {
        this(null);
    }
    
    public BooleanReturningMethodInvoker(String methodName) {
        csa = new CallSiteArray(BooleanReturningMethodInvoker.class, new String[]{methodName, "asBoolean"});
    }
    
    public boolean invoke(Object receiver, Object... args) {
        try {
            // make cached call for given method
            Object ret = csa.array[0].call(receiver, args);
            return convertToBoolean(ret);
        } catch (Throwable t) {
            // UncheckedThrow allows throwing checked exceptions without declaring a throws
            UncheckedThrow.rethrow(t);
            return false;
        }
    }
    
    public boolean convertToBoolean(Object arg) {
        // handle conversion to boolean
        if (arg == null) return false;
        if (arg instanceof Boolean) {
            return (Boolean) arg;
        }
        // it was not null and not boolean, so call asBoolean
        try {
            arg = csa.array[1].call(arg, CallSiteArray.NOPARAM);
        } catch (Throwable t) {
            // UncheckedThrow allows throwing checked exceptions without declaring a throws
            UncheckedThrow.rethrow(t);
        }
        return (Boolean) arg;
    }

}
