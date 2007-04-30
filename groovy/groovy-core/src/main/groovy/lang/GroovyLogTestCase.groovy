package groovy.lang

import java.util.logging.*

/**
Helper class to spoof log entries as produced by calling arbitrary code.
This allows non-intrusive testing of dependent objects without
explicitly using Mock objects as long as those dependent objects
do some proper logging.
As a measure of last resort, it can be used on MetaClass to spoof
it's log entries on 'invokeMethod'.

@author Dierk Koenig
@see GroovyLogTestCaseTest
**/

class GroovyLogTestCase extends GroovyTestCase {

    /**
     Execute the given Closure with the according level for the Logger that
     is qualified by the qualifier and return the log output as a String.
     Qualifiers are usually package or class names.
     Existing log level and handlers are restored after execution.
    **/
    static String stringLog (Level level, String qualifier, Closure yield){
        // store old values
        Logger logger = Logger.getLogger(qualifier)
        def usesParentHandlers = logger.useParentHandlers
        // set new values
        logger.useParentHandlers = false
        def out = new ByteArrayOutputStream(1024)
        Handler stringHandler = new StreamHandler(out, new SimpleFormatter())
        stringHandler.level = Level.ALL
        logger.addHandler(stringHandler) // any old handlers remain

        withLevel(level, qualifier, yield)

        // restore old values
        logger.level = Level.OFF    // temporarily, to avoid logging the 3 stmts below
        stringHandler.flush()
        out.close()
        logger.removeHandler(stringHandler)
        logger.useParentHandlers = usesParentHandlers
        return out.toString()
    }

    /**
     Execute the given Closure with the according level for the Logger that
     is qualified by the qualifier. Qualifiers are usually package or class
     names.
     The log level is restored after execution.
    **/
    static def withLevel(Level level, String qualifier, Closure yield){
        // store old values
        Logger logger = Logger.getLogger(qualifier)
        def loglevel = logger.level
        // set new values
        if (!logger.isLoggable(level)) logger.level = level // use min value

        def result = yield()

        // restore old values
        logger.level = loglevel
        return result
    }
}