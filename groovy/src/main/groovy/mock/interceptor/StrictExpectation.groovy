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
    Expects demanded call cardinalities to match demanded ranges in the sequence of recording.
    @See LooseExpectation
    @author Dierk Koenig
*/

class StrictExpectation {
    Demand fDemand  = null
    int fCallSpecIdx = 0
    List fCalls      = []

    StrictExpectation(Demand demand) {
        fDemand = demand
    }

    /**
        Match the requested method name against eligible demands.
        Fail early if no match possible.
        Return the demand's behavior closure on match.
    */
    Closure match(String name) {
        if (!fCalls[fCallSpecIdx]) fCalls[fCallSpecIdx] = 0

        if (fCallSpecIdx >= fDemand.recorded.size()) {
            throw new AssertionFailedError("No more calls to '$name' expected at this point. End of demands.")
        }

        def call = fDemand.recorded[fCallSpecIdx]
        if (name != call.name) {                             // if name does not match...
            def open = call.range.from - fCalls[fCallSpecIdx]
            if ( open > 0) {                                 // ... if we haven't reached the minimum, yet -> Exception
                throw new AssertionFailedError("No call to '$name' expected at this point. "+
                "Still $open call(s) to '${call.name}' expected.")
            } else {                                         // ... proceed finding
                fCallSpecIdx++
                return match(name)
            }
        }

        // register the call
        fCalls[fCallSpecIdx] += 1

        // store the behavior for returning
        def result = call.behavior

        // proceed to next callSpec if we need to
        if (fCalls[fCallSpecIdx] >= call.range.to ) fCallSpecIdx++

        return result
    }

    /** verify all calls are in expected range */
    void verify() {
        fDemand.verify(fCalls)
    }

}