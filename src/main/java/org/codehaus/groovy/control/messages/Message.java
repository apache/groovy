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
package org.codehaus.groovy.control.messages;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A base class for compilation messages.
 */
public abstract class Message {

    /**
     * Creates a new {@code Message} from the specified text.
     */
    public static Message create(final String text, final ProcessingUnit owner) {
        return new SimpleMessage(text, owner);
    }

    /**
     * Creates a new {@code Message} from the specified text and data.
     */
    public static Message create(final String text, final Object data, final ProcessingUnit owner) {
        return new SimpleMessage(text, data, owner);
    }

    /**
     * Creates a new {@code Message} from the specified {@code SyntaxException}.
     */
    public static Message create(final SyntaxException error, final SourceUnit owner) {
        return new SyntaxErrorMessage(error, owner);
    }

    //--------------------------------------------------------------------------

    /**
     * Writes this message to the specified {@link PrintWriter}. This is the full,
     * human-oriented rendering; other layouts are produced by
     * {@link org.codehaus.groovy.control.ErrorFormat} from {@link #toDiagnostic()}.
     */
    public abstract void write(PrintWriter writer, Janitor janitor);

    /**
     * Writes this message to the specified {@link PrintWriter}.
     */
    public final void write(final PrintWriter writer) {
        write(writer, null);
    }

    /**
     * Returns a format-neutral view of this message: its text and, when known, the
     * source and position it applies to. Layouts other than the full one are rendered
     * from this rather than from the message classes (GROOVY-12312).
     * <p>
     * The default captures the output of {@link #write(PrintWriter)} as the text, with
     * no file or position, so that a subclass which does not override this still
     * renders in every format.
     *
     * @return the diagnostic view, never {@code null}
     * @since 6.0.0
     */
    public Diagnostic toDiagnostic() {
        StringWriter text = new StringWriter();
        PrintWriter writer = new PrintWriter(text);
        write(writer);
        writer.flush();
        return new Diagnostic(null, -1, -1, text.toString());
    }

    /**
     * The name of {@code unit} when it is a {@link SourceUnit}, else {@code null}.
     */
    static String sourceName(final ProcessingUnit unit) {
        return (unit instanceof SourceUnit source) ? source.getName() : null;
    }
}
