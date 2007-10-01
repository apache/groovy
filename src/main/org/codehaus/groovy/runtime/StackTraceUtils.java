/* Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Originally was grails.utils.GrailsUtils, removed some grails specific stuff.
 * Utility methods removing internal lines from stack traces
 *
 * @author Graeme Rocher
 * @since 0.2
 *
 * @version $Revision: 5544 $
 * First Created: 02-Jun-2006
 * Last Updated: $Date: 2007-09-21 13:53:07 -0500 (Fri, 21 Sep 2007) $
 */
public class StackTraceUtils {

    public static final String STACK_LOG_NAME = "StackTrace";
    private static final Logger STACK_LOG;
    // set log to consume traces by default, end user can override later
    static {
        outer: do {
            Enumeration existingLogs = LogManager.getLogManager().getLoggerNames();
            while (existingLogs.hasMoreElements()) {
                if (STACK_LOG_NAME.equals(existingLogs.nextElement())) {
                    STACK_LOG = Logger.getLogger(STACK_LOG_NAME);
                    break outer;
                }
            }
            STACK_LOG = Logger.getLogger(STACK_LOG_NAME);
            STACK_LOG.setUseParentHandlers(false);
        } while (false);
    }

    private static final String[] GROOVY_PACKAGES =
            System.getProperty("groovy.sanitized.stacktraces",
                "groovy.," +
                "org.codehaus.groovy.," +
                "java.," +
                "javax.," +
                "sun.," +
                "gjdk.groovy.,"
            ).split("(\\s|,)+");

    /**
     * <p>Remove all apparently groovy-internal trace entries from the exception instance<p>
     * <p>This modifies the original instance and returns it, it does not clone</p>
     * @param t
     * @return The exception passed in, after cleaning the stack trace
     */
    public static Throwable sanitize(Throwable t) {
        // Note that this getBoolean access may well be synced...
        if (!Boolean.getBoolean("groovy.full.stacktrace")) {
            StackTraceElement[] trace = t.getStackTrace();
            List newTrace = new ArrayList();
            for (int i = 0; i < trace.length; i++) {
                StackTraceElement stackTraceElement = trace[i];
                if (isApplicationClass(stackTraceElement.getClassName())) {
                    newTrace.add( stackTraceElement);
                }
            }
            // We don't want to lose anything, so log it
  	        STACK_LOG.log(Level.WARNING, "Sanitizing stacktrace:", t);


            StackTraceElement[] clean = new StackTraceElement[newTrace.size()];
            newTrace.toArray(clean);
            t.setStackTrace(clean);
        }
        return t;
    }

    public static void printSanitizedStackTrace(Throwable t, PrintWriter p) {
        t = StackTraceUtils.sanitize(t);

        StackTraceElement[] trace = t.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement stackTraceElement = trace[i];
            p.println(  "at "+stackTraceElement.getClassName()
                        +"("+stackTraceElement.getMethodName()
                        +":"+stackTraceElement.getLineNumber()+")");
        }
    }

    public static void printSanitizedStackTrace(Throwable t) {
        printSanitizedStackTrace(t, new PrintWriter(System.err));
    }

    public static boolean isApplicationClass(String className) {
        for (int i = 0; i < GROOVY_PACKAGES.length; i++) {
            String groovyPackage = GROOVY_PACKAGES[i];
            if (className.startsWith(groovyPackage)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Extracts the root cause of the exception, no matter how nested it is</p>
     * @param t
     * @return The deepest cause of the exception that can be found
     */
    public static Throwable extractRootCause(Throwable t) {
        Throwable result = t;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

    /**
     * <p>Get the root cause of an exception and sanitize it for display to the user</p>
     * <p>This will MODIFY the stacktrace of the root cause exception object and return it</p>
     * @param t
     * @return The root cause exception instance, with its stace trace modified to filter out groovy runtime classes
     */
    public static Throwable sanitizeRootCause(Throwable t) {
        return StackTraceUtils.sanitize(StackTraceUtils.extractRootCause(t));
    }

    /**
     * <p>Sanitize the exception and ALL nested causes</p>
     * <p>This will MODIFY the stacktrace of the exception instance and all its causes irreversibly</p>
     * @param t
     * @return The root cause exception instances, with stack trace modified to filter out groovy runtime classes
     */
    public static Throwable deepSanitize(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null) {
            current = StackTraceUtils.sanitize(current.getCause());
        }
        return StackTraceUtils.sanitize(t);
    }
}
