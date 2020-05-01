package groovy.util.logging.vm9

import groovy.transform.AutoImplement
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

@PackageScope
@AutoImplement
@CompileStatic
class LoggerSpy implements System.Logger {
    String warningParameter = null
    String infoParameter = null
    String traceParameter = null
    String errorParameter = null
    String debugParameter = null

    void reset() {
        warningParameter = null
        infoParameter = null
        traceParameter = null
        errorParameter = null
        debugParameter = null
    }

    @Override
    void log(Level lev, String s) {
        switch (lev) {
            case Level.WARNING:
                if (warningParameter) throwAssertionError("Warning already called once with parameter $warningParameter")
                warningParameter = s
                break
            case Level.INFO:
                if (infoParameter) throwAssertionError("Info already called once with parameter $infoParameter")
                infoParameter = s
                break
            case Level.ERROR:
                if (errorParameter) throwAssertionError("Error already called once with parameter $errorParameter")
                errorParameter = s
                break
            case Level.DEBUG:
                if (debugParameter) throwAssertionError("Debug already called once with parameter $debugParameter")
                debugParameter = s
                break
            case Level.TRACE:
                if (traceParameter) throwAssertionError("Trace already called once with parameter $traceParameter")
                traceParameter = s
                break
        }
    }

    private static void throwAssertionError(Object detailMessage) {
        throw new AssertionError(detailMessage)
    }
}
