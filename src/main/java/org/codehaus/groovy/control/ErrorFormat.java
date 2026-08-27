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
package org.codehaus.groovy.control;

import org.codehaus.groovy.control.messages.Diagnostic;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;

import java.io.PrintWriter;
import java.util.List;

/**
 * How compilation errors and warnings are rendered (GROOVY-12312).
 * <p>
 * Each constant is a renderer which owns the layout of a whole report; the
 * {@link Message} classes only say what went wrong and where, via
 * {@link Message#toDiagnostic()}. A format is selected with
 * {@link CompilerConfiguration#setErrorFormat(ErrorFormat)} and applied by
 * {@link ErrorCollector#write(PrintWriter, Janitor)}. Adding a layout means adding
 * a constant here, not touching the messages.
 *
 * @since 6.0.0
 */
public enum ErrorFormat {

    /**
     * The default, human-oriented rendering: each message as
     * {@link Message#write(PrintWriter, Janitor)} lays it out, typically with the
     * offending source line and a caret marking the column, followed by a blank
     * line; then a count per severity.
     */
    FULL {
        @Override
        public void write(final PrintWriter writer, final Janitor janitor, final List<? extends Message> warnings,
                          final List<? extends Message> errors, final boolean debug) {
            writeFull(writer, janitor, warnings, "warning", debug);
            writeFull(writer, janitor, errors, "error", debug);
        }
    },

    /**
     * One line per diagnostic, in the {@code file:line:column: severity: message}
     * form understood by most editors and CI log parsers; then a count per severity.
     * The source line and caret are omitted, and a message spanning several lines
     * is joined into one. Parts a message does not have are dropped rather than
     * guessed: no file when there is no source unit, no line and column when there
     * is no position.
     */
    SHORT {
        @Override
        public void write(final PrintWriter writer, final Janitor janitor, final List<? extends Message> warnings,
                          final List<? extends Message> errors, final boolean debug) {
            writeShort(writer, warnings, "warning", debug);
            writeShort(writer, errors, "error", debug);
        }
    };

    /**
     * Renders a complete report: the warnings, then the errors.
     *
     * @param writer   the destination
     * @param janitor  the cleanup helper for temporary source access, may be {@code null}
     * @param warnings the warnings, may be {@code null} or empty
     * @param errors   the errors, may be {@code null} or empty
     * @param debug    whether to follow each syntax error with its stack trace
     */
    public abstract void write(PrintWriter writer, Janitor janitor, List<? extends Message> warnings,
                               List<? extends Message> errors, boolean debug);

    //--------------------------------------------------------------------------

    private static void writeFull(final PrintWriter writer, final Janitor janitor,
                                  final List<? extends Message> messages, final String severity, final boolean debug) {
        if (messages == null || messages.isEmpty()) return;

        for (Message message : messages) {
            message.write(writer, janitor);
            writeTrace(writer, message, debug);
            writer.println();
        }
        writeCount(writer, messages.size(), severity);
    }

    private static void writeShort(final PrintWriter writer,
                                   final List<? extends Message> messages, final String severity, final boolean debug) {
        if (messages == null || messages.isEmpty()) return;

        for (Message message : messages) {
            writer.println(shortLine(message.toDiagnostic(), severity));
            writeTrace(writer, message, debug);
        }
        writeCount(writer, messages.size(), severity);
    }

    /**
     * {@code file:line:column: severity: text}, dropping the parts the diagnostic does not have.
     */
    static String shortLine(final Diagnostic diagnostic, final String severity) {
        StringBuilder text = new StringBuilder();
        if (diagnostic.file() != null) {
            text.append(diagnostic.file());
            if (diagnostic.line() > 0) {
                text.append(':').append(diagnostic.line());
                if (diagnostic.column() > 0) text.append(':').append(diagnostic.column());
            }
            text.append(": ");
        }
        return text.append(severity).append(": ").append(oneLine(diagnostic.text())).toString();
    }

    /**
     * Joins text spanning several lines into one, so that a short-format
     * diagnostic really does occupy a single line.
     */
    private static String oneLine(final String text) {
        return text == null ? "" : text.strip().replaceAll("\\s*\\R\\s*", " ");
    }

    private static void writeTrace(final PrintWriter writer, final Message message, final boolean debug) {
        if (debug && message instanceof SyntaxErrorMessage syntaxError) {
            syntaxError.getCause().printStackTrace(writer);
        }
    }

    private static void writeCount(final PrintWriter writer, final int count, final String severity) {
        writer.print(count);
        writer.print(" " + severity);
        if (count > 1) {
            writer.print("s");
        }
        writer.println();
    }
}
