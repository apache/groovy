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
package groovy.lang

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import java.util.logging.StreamHandler

/**
 * Helper class to spoof log entries as produced by calling arbitrary code.
 * This allows non-intrusive testing of dependent objects without
 * explicitly using Mock objects as long as those dependent objects
 * do some proper logging.
 * As a measure of last resort, it can be used on MetaClass to spoof
 * it's log entries on 'invokeMethod'.
 *
 * @see GroovyLogTestCaseTest
 */
@Deprecated
class GroovyLogTestCase extends GroovyTestCase {

    /**
     *      Execute the given Closure with the according level for the Logger that
     *      is qualified by the qualifier and return the log output as a String.
     *      Qualifiers are usually package or class names.
     *      Existing log level and handlers are restored after execution.
     */
    static String stringLog(Level level, String qualifier, Closure yield) {
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
     * Execute the given Closure with the according level for the Logger that
     * is qualified by the qualifier. Qualifiers are usually package or class names.
     * The log level is restored after execution.
     */
    static def withLevel(Level level, String qualifier, Closure yield) {
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