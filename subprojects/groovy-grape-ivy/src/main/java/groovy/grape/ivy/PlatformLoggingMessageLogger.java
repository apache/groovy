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
package groovy.grape.ivy;

import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.Message;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * An Ivy {@link org.apache.ivy.util.MessageLogger} that delegates to JDK Platform Logging
 * ({@code System.Logger}), so that Ivy log output can be controlled through
 * {@code logging.properties} alongside other Groovy diagnostic messages.
 *
 * <p>Ivy message levels are mapped to Platform Logging levels as follows:</p>
 * <ul>
 *   <li>{@code MSG_ERR}     &rarr; {@code ERROR}</li>
 *   <li>{@code MSG_WARN}    &rarr; {@code WARNING}</li>
 *   <li>{@code MSG_INFO}    &rarr; {@code INFO}</li>
 *   <li>{@code MSG_VERBOSE} &rarr; {@code DEBUG}</li>
 *   <li>{@code MSG_DEBUG}   &rarr; {@code TRACE}</li>
 * </ul>
 *
 * @since 6.0.0
 */
class PlatformLoggingMessageLogger extends AbstractMessageLogger {

    private static final String LOGGER_NAME = "groovy.grape.ivy";
    private static final System.Logger LOGGER = System.getLogger(LOGGER_NAME);

    /**
     * Creates a message logger configured with a warning default level.
     */
    PlatformLoggingMessageLogger() {
        // Default to WARNING to match the previous behaviour (DefaultMessageLogger
        // at level -1 suppressed everything). Users can raise verbosity via
        // ~/.groovy/logging.properties or the grape -i/-V/-d flags.
        var julLogger = java.util.logging.Logger.getLogger(LOGGER_NAME);
        if (julLogger.getLevel() == null) {
            julLogger.setLevel(java.util.logging.Level.WARNING);
        }
    }

    /**
     * Logs an Ivy message through platform logging.
     *
     * @param msg the message to log
     * @param level the Ivy message level
     */
    @Override
    public void log(String msg, int level) {
        LOGGER.log(toSystemLevel(level), msg);
    }

    /**
     * Logs a raw Ivy message through platform logging.
     *
     * @param msg the message to log
     * @param level the Ivy message level
     */
    @Override
    public void rawlog(String msg, int level) {
        LOGGER.log(toSystemLevel(level), msg);
    }

    /**
     * Ignores Ivy progress updates.
     */
    @Override
    protected void doProgress() {
        // no-op — progress dots are not useful in structured logging
    }

    /**
     * Ends a progress section, logging the final message when present.
     *
     * @param msg the trailing progress message
     */
    @Override
    protected void doEndProgress(String msg) {
        if (msg != null && !msg.isEmpty()) {
            LOGGER.log(DEBUG, msg);
        }
    }

    private static System.Logger.Level toSystemLevel(int ivyLevel) {
        return switch (ivyLevel) {
            case Message.MSG_ERR -> ERROR;
            case Message.MSG_WARN -> WARNING;
            case Message.MSG_INFO -> INFO;
            case Message.MSG_VERBOSE -> DEBUG;
            case Message.MSG_DEBUG -> TRACE;
            default -> INFO;
        };
    }
}
