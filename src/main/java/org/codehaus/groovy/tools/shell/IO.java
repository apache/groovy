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

import org.codehaus.groovy.tools.shell.util.Preferences;
import org.jline.jansi.AnsiConsole;
import org.jline.jansi.AnsiMode;
import org.jline.jansi.AnsiPrintStream;

import java.io.Closeable;
import java.io.IOError;
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
    private static final String ANSI_CONSOLE = "org.jline.jansi.AnsiConsole";

    private static final boolean JANSI_AVAILABLE = isJansiAvailable();

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
     * Whether {@link #out} renders ANSI escape sequences: the output stream is the JVM's standard
     * output, JLine's Jansi is on the class path, and the output goes to a terminal (or the
     * {@code jansi.mode} system property is {@code force}).
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
        PrintWriter out = tryConstructRenderWriter(outputStream);
        PrintWriter err = tryConstructRenderWriter(errorStream);
        this.ansiSupported = out != null;
        if (out == null) {
            out = new PrintWriter(outputStream, true);
        }
        if (err == null) {
            err = new PrintWriter(errorStream, true);
        }
        this.out = out;
        this.err = err;
    }

    /**
     * Attempts to create an ANSI-aware writer for the supplied stream.
     *
     * <p>Only the JVM's own standard output and error streams can render ANSI, through JLine's Jansi
     * {@code AnsiConsole}, which also enables virtual terminal processing on Windows. A stream that is
     * redirected to a file or a pipe gets no ANSI writer, so no escape sequences reach it.</p>
     *
     * @param stream the output stream to wrap
     * @return an ANSI render writer, or {@code null} if ANSI support is
     * unavailable
     */
    protected PrintWriter tryConstructRenderWriter(OutputStream stream) {
        if (!JANSI_AVAILABLE) {
            return null;
        }
        try {
            return JansiConsole.writerFor(stream);
        } catch (IOError | RuntimeException | LinkageError e) {
            return null;
        }
    }

    private static boolean isJansiAvailable() {
        // JLine's Jansi is optional for groovy, so look it up before JansiConsole links against it
        try {
            Class.forName(ANSI_CONSOLE, false, IO.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    /**
     * Refers to JLine's Jansi, and is loaded only once {@link #isJansiAvailable()} found it.
     */
    private static final class JansiConsole {
        static PrintWriter writerFor(OutputStream stream) {
            AnsiPrintStream console;
            // AnsiConsole writes to the streams the JVM started with, so a replaced System.out keeps its own
            if (stream == System.out && System.out == AnsiConsole.sysOut()) {
                console = AnsiConsole.out();
            } else if (stream == System.err && System.err == AnsiConsole.sysErr()) {
                console = AnsiConsole.err();
            } else {
                return null;
            }
            return console.getMode() == AnsiMode.Strip ? null : new PrintWriter(console, true);
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
    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        err.close();
    }

    /**
     * Verbosity for simple logging: QUIET, INFO, VERBOSE, DEBUG
     */
    public static final class Verbosity {
        /**
         * Minimal output.
         */
        public static final Verbosity QUIET = new Verbosity("QUIET");

        /**
         * Normal informational output.
         */
        public static final Verbosity INFO = new Verbosity("INFO");

        /**
         * Extra informational output.
         */
        public static final Verbosity VERBOSE = new Verbosity("VERBOSE");

        /**
         * Debug-level output.
         */
        public static final Verbosity DEBUG = new Verbosity("DEBUG");

        /**
         * The symbolic name of this verbosity level.
         */
        public final String name;

        private Verbosity(final String name) {
            this.name = name;
        }

        /**
         * Returns the symbolic name of this verbosity level.
         *
         * @return the verbosity name
         */
        @Override
        public String toString() {
            return name;
        }

        /**
         * Resolves a verbosity level by name.
         *
         * @param name the name to resolve
         * @return the matching verbosity level
         * @throws IllegalArgumentException if the name does not match a known
         * level
         */
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
