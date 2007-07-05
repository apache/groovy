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

package groovy.mock.interceptor

import junit.framework.AssertionFailedError

/**
    Expects demanded call cardinalities to match demanded ranges.
    The calls are allowed to be out of the recorded sequence.
    If a method is demanded multiple times, the ranges are filled by order of recording.
    @See StrictExpectation
    @author Dierk Koenig
*/

class LooseExpectation {
    Demand fDemand  = null
    List fCalls      = []

    LooseExpectation(Demand demand) {
        fDemand = demand
    }

    /**
        Match the requested method name against eligible demands.
        Fail early if no match possible.
        Return the demand's behavior closure on match.
    */
    Closure match(String name) {
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

    /** verify all calls are in expected range */ // todo: duplication with StrictExpectation
    void verify() {
        for (i in 0 ..< fDemand.recorded.size()) {
            def call = fDemand.recorded[i]
            def msg = "verify[$i]: expected ${call.range.toString()} call(s) to '${call.name}' but was "
            if ( null == fCalls[i] )
                throw new AssertionFailedError(msg + "never called.")
            if (! call.range.contains( fCalls[i] ) )
                throw new AssertionFailedError(msg + "called ${fCalls[i]} time(s).")
        }
    }

}