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
package groovy.mock.interceptor

import junit.framework.AssertionFailedError
import org.codehaus.groovy.runtime.DefaultGroovyMethods

/**
 *  Expects demanded call cardinalities to match demanded ranges.
 *  The calls are allowed to be out of the recorded sequence.
 *  If a method is demanded multiple times, the ranges are filled by order of recording.
 * 
 *  @see StrictExpectation
 */
class LooseExpectation {
    Demand fDemand  = null
    List fCalls      = []

    LooseExpectation(Demand demand) {
        fDemand = demand
    }

    /**
     * Match the requested method name against eligible demands.
     * Fail early if no match possible.
     * Return the demand's behavior closure on match.
     * Also skips over names matching ignore filter, if any.
     */
    Closure match(String name) {
        def filter = fDemand.ignore.keySet().find{ DefaultGroovyMethods.grep([name], it) }
        if (filter) return fDemand.ignore.get(filter)
        def callIndex = 0
        // find first eligible callSpec
        while (! isEligible(name, callIndex) ) callIndex++

        // register the call
        fCalls[callIndex] += 1

        return fDemand.recorded[callIndex].behavior
    }

    boolean isEligible(String name, int i) {
        def calls = fDemand.recorded
        if (i >= calls.size())  {
            throw new AssertionFailedError("No more calls to '$name' expected at this point. End of demands.")
        }
        if (calls[i].name != name)              return false
        if (null == fCalls[i])                  fCalls[i] = 0
        if (fCalls[i] >= calls[i].range.to)     return false
        return true
    }

    /** verify all calls are in expected range */ 
    void verify() {
        fDemand.verify(fCalls)
    }
}