package groovy.mock.interceptor

import junit.framework.AssertionFailedError

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