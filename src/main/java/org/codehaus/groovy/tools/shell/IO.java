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
package org.codehaus.groovy.tools.shell;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.tools.shell.util.Preferences;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Container for input/output handles.
 */
public class IO implements Closeable {
    private static final String ANSI_RENDER_WRITER = "org.fusesource.jansi.AnsiRenderWriter";

    /**
     * Raw input stream.
     */
    public final InputStream inputStream;

    /**
     * Raw output stream.
     */
    public final OutputStream outputStream;

    /**
     * Raw error output stream.
     */
    public final OutputStream errorStream;

    /**
     * Preferred input reader.
     */
    public final Reader in;

    /**
     * Preferred output writer.
     */
    public final PrintWriter out;

    /**
     * Preferred error output writer.
     */
    public final PrintWriter err;

    /**
     * Whether ansi support is available
     */
    public final boolean ansiSupported;

    /**
     * Construct a new IO container.
     */
    public IO(final InputStream inputStream, final OutputStream outputStream, final OutputStream errorStream) {
        assert inputStream != null;
        assert outputStream != null;
        assert errorStream != null;

        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.errorStream = errorStream;

        this.in = new InputStreamReader(inputStream);
        boolean ansiSupported = false;
        try {
            Class.forName(ANSI_RENDER_WRITER, false, IO.class.getClassLoader());
            ansiSupported = true;
        } catch (ClassNotFoundException ignore) {
        }
        this.ansiSupported = ansiSupported;
        PrintWriter out = null;
        PrintWriter err = null;
        if (ansiSupported) {
            out = tryConstructRenderWriter(outputStream);
            err = tryConstructRenderWriter(errorStream);
        }
        if (out == null) {
            out = new PrintWriter(outputStream, true);
        }
        if (err == null) {
            err = new PrintWriter(errorStream, true);
        }
        this.out = out;
        this.err = err;
    }

    protected PrintWriter tryConstructRenderWriter(OutputStream stream) {
        // load via reflection to avoid hard-coded dependency on jansi jar
        try {
            return (PrintWriter) InvokerHelper.invokeConstructorOf(ANSI_RENDER_WRITER, new Object[]{stream, true});
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    /**
     * Construct a new IO container using system streams.
     */
    public IO() {
        this(System.in, System.out, System.err);
    }

    /**
     * Set the verbosity level.
     */
    public void setVerbosity(final Verbosity verbosity) {
        assert verbosity != null;

        Preferences.verbosity = verbosity;
    }

    /**
     * Returns the verbosity level.
     */
    public Verbosity getVerbosity() {
        return Preferences.verbosity;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#QUIET}.
     */
    public boolean isQuiet() {
        return getVerbosity() == Verbosity.QUIET;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#INFO}.
     */
    public boolean isInfo() {
        return getVerbosity() == Verbosity.INFO;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#VERBOSE}.
     */
    public boolean isVerbose() {
        return getVerbosity() == Verbosity.VERBOSE;
    }

    /**
     * Check if the verbosity level is set to {@link Verbosity#DEBUG}.
     *
     * <p>For general usage, when debug output is required, it is better
     * to use the logging facility instead.
     */
    public boolean isDebug() {
        return getVerbosity() == Verbosity.DEBUG;
    }

    /**
     * Flush both output streams.
     */
    public void flush() {
        out.flush();
        err.flush();
    }

    /**
     * Close all streams.
     */
    public void close() throws IOException {
        in.close();
        out.close();
        err.close();
    }

    /**
     * Verbosity for simple logging: QUIET, INFO, VERBOSE, DEBUG
     */
    public static final class Verbosity {
        public static final Verbosity QUIET = new Verbosity("QUIET");

        public static final Verbosity INFO = new Verbosity("INFO");

        public static final Verbosity VERBOSE = new Verbosity("VERBOSE");

        public static final Verbosity DEBUG = new Verbosity("DEBUG");

        public final String name;

        private Verbosity(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static Verbosity forName(final String name) {
            assert name != null;

            if (QUIET.name.equalsIgnoreCase(name)) {
                return QUIET;
            }
            if (INFO.name.equalsIgnoreCase(name)) {
                return INFO;
            }
            if (VERBOSE.name.equalsIgnoreCase(name)) {
                return VERBOSE;
            }
            if (DEBUG.name.equalsIgnoreCase(name)) {
                return DEBUG;
            }

            throw new IllegalArgumentException("Invalid verbosity name: " + name);
        }
    }
}
