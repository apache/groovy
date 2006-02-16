package groovy.mock.interceptor

import junit.framework.AssertionFailedError

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