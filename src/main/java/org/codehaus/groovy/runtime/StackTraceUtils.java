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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

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
 * @since 1.5
 */
public class StackTraceUtils {

    public static final String STACK_LOG_NAME = "StackTrace";
    private static final Logger STACK_LOG;
    // set log to consume traces by default, end user can override later

    static {
        outer:
        do {
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

    private static final List<Closure> tests = new ArrayList<>();

    /**
     * Adds a groovy.lang.Closure to test whether the stack trace
     * element should be added or not.
     * <p>
     * The groovy.lang.Closure will be given the class name as parameter.
     * the return value decides if the element will be added or not.
     * <ul>
     * <li><b>true</b>  - trace element will be added to the trace
     * <li><b>false</b> - trace element will <b>not</b> be added to the trace
     * <li><b>null</b>  - continue with next test
     * </ul>
     * Groovy truth will be used to determine true and false, null is excluded from
     * defaulting to false here. If all tests have been executed and all of them skipped, then
     * the groovy standard filtering will take place.
     *
     * @param test the testing groovy.lang.Closure
     */
    public static void addClassTest(Closure test) {
        tests.add(test);
    }

    /**
     * Remove all apparently groovy-internal trace entries from the exception instance
     * <p>
     * This modifies the original instance and returns it, it does not clone
     *
     * @param t the Throwable whose stack trace we want to sanitize
     * @return The original Throwable but with a sanitized stack trace
     */
    public static Throwable sanitize(Throwable t) {
        // Note that this getBoolean access may well be synced...
        if (!SystemUtil.getBooleanSafe("groovy.full.stacktrace")) {
            StackTraceElement[] trace = t.getStackTrace();
            List<StackTraceElement> newTrace = new ArrayList<>();
            for (StackTraceElement stackTraceElement : trace) {
                if (isApplicationClass(stackTraceElement.getClassName())) {
                    newTrace.add(stackTraceElement);
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
        for (StackTraceElement stackTraceElement : trace) {
            p.println("at " + stackTraceElement.getClassName()
                    + "(" + stackTraceElement.getMethodName()
                    + ":" + stackTraceElement.getLineNumber() + ")");
        }
    }

    public static void printSanitizedStackTrace(Throwable t) {
        printSanitizedStackTrace(t, new PrintWriter(System.err));
    }

    public static boolean isApplicationClass(String className) {
        for (Closure test : tests) {
            Object result = test.call(className);
            if (result != null) {
                return DefaultTypeTransformation.castToBoolean(result);
            }
        }

        for (String groovyPackage : GROOVY_PACKAGES) {
            if (className.startsWith(groovyPackage)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts the root cause of the exception, no matter how nested it is
     *
     * @param t a Throwable
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
     * Get the root cause of an exception and sanitize it for display to the user
     * <p>
     * This will MODIFY the stacktrace of the root cause exception object and return it
     *
     * @param t a throwable
     * @return The root cause exception instance, with its stace trace modified to filter out groovy runtime classes
     */
    public static Throwable sanitizeRootCause(Throwable t) {
        return StackTraceUtils.sanitize(StackTraceUtils.extractRootCause(t));
    }

    /**
     * Sanitize the exception and ALL nested causes
     * <p>
     * This will MODIFY the stacktrace of the exception instance and all its causes irreversibly
     *
     * @param t a throwable
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
