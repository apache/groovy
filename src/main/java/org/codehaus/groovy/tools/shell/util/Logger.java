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
package org.codehaus.groovy.tools.shell.util;

import org.codehaus.groovy.tools.shell.IO;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Provides a very, very basic logging API.
 */
public final class Logger {
    public static IO io;
    public final String name;

    private Logger(final String name) {
        assert name != null;
        this.name = name;
    }
    
    private void log(final String level, Object msg, Throwable cause) {
        assert level != null;
        assert msg != null;

        if (io == null) {
            synchronized (Logger.class) {
                if (io == null) {
                    io = new IO();
                }
            }
        }

        // Allow the msg to be a Throwable, and handle it properly if no cause is given
        if (cause == null) {
            if (msg instanceof Throwable) {
                cause = (Throwable) msg;
                msg = cause.getMessage();
            }
        }

        if (io.ansiSupported) {
            logWithAnsi(level, msg);
        } else {
            logDefault(level, msg);
        }

        if (cause != null) {
            cause.printStackTrace(io.out);
        }

        io.flush();
    }

    private void logDefault(String level, Object msg) {
        io.out.println(level + " [" + name + "] " + msg);
    }

    private void logWithAnsi(String level, Object msg) {
        Color color = GREEN;
        if (WARN.equals(level) || ERROR.equals(level)) {
            color = RED;
        }
        io.out.println(ansi().a(INTENSITY_BOLD).fg(color).a(level).reset().a(" [").a(name).a("] ").a(msg));
    }

    //
    // Level helpers
    //
    
    private static final String DEBUG = "DEBUG";

    public boolean isDebugEnabled() {
        return Preferences.verbosity == IO.Verbosity.DEBUG;
    }

    public boolean isDebug() {
        return isDebugEnabled();
    }
    
    public void debug(final Object msg) {
        if (isDebugEnabled()) {
            log(DEBUG, msg, null);
        }
    }
    
    public void debug(final Object msg, final Throwable cause) {
        if (isDebugEnabled()) {
            log(DEBUG, msg, cause);
        }
    }

    private static final String WARN = "WARN";

    public void warn(final Object msg) {
        log(WARN, msg, null);
    }

    public void warn(final Object msg, final Throwable cause) {
        log(WARN, msg, cause);
    }
    
    private static final String ERROR = "ERROR";

    public void error(final Object msg) {
        log(ERROR, msg, null);
    }

    public void error(final Object msg, final Throwable cause) {
        log(ERROR, msg, cause);
    }

    //
    // Factory access
    //
    
    public static Logger create(final Class type) {
        return new Logger(type.getName());
    }

    public static Logger create(final Class type, final String suffix) {
        return new Logger(type.getName() + "." + suffix);
    }
}
