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
package org.apache.groovy.lsp.internal.diagnostic;

import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.JavaMessage;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Groovy {@link Message} instances onto LSP diagnostics.
 */
public final class DiagnosticConverter {

    public static final String SOURCE = "groovy";

    /**
     * Converts errors and warnings of {@code document} to LSP diagnostics.
     *
     * @param document compiled document
     * @param encoding negotiated encoding
     * @return diagnostics, never {@code null}
     */
    public List<Diagnostic> convert(final CompiledDocument document, final PositionEncoding encoding) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (document == null) {
            return diagnostics;
        }
        for (Message message : document.getErrors()) {
            if (belongsTo(message, document)) {
                diagnostics.add(toDiagnostic(message, document.getText(), encoding, DiagnosticSeverity.Error));
            }
        }
        for (WarningMessage warning : document.getWarnings()) {
            if (belongsTo(warning, document)) {
                diagnostics.add(toDiagnostic(warning, document.getText(), encoding, DiagnosticSeverity.Warning));
            }
        }
        for (JavaMessage javaMessage : document.getJavaMessages()) {
            diagnostics.add(toJavaDiagnostic(javaMessage, document.getText(), encoding));
        }
        return diagnostics;
    }

    static boolean belongsTo(final Message message, final CompiledDocument document) {
        if (document == null || message == null) {
            return false;
        }
        org.codehaus.groovy.control.messages.Diagnostic groovy;
        try {
            groovy = message.toDiagnostic();
        } catch (RuntimeException ignored) {
            return false;
        }
        String file = groovy.file();
        if (file == null || file.isBlank()) {
            return false;
        }
        String uri = document.getUri() == null ? "" : document.getUri().toString();
        String unit = document.getSourceUnit() == null ? "" : document.getSourceUnit().getName();
        return file.equals(uri) || file.equals(unit);
    }

    Diagnostic toDiagnostic(final Message message, final String text, final PositionEncoding encoding,
                            final DiagnosticSeverity fallback) {
        org.codehaus.groovy.control.messages.Diagnostic groovy;
        try {
            groovy = message.toDiagnostic();
        } catch (RuntimeException ignored) {
            groovy = new org.codehaus.groovy.control.messages.Diagnostic(null, -1, -1, message.toString());
        }
        Range range = rangeOf(message, groovy, text, encoding);
        String msg = messageText(message, groovy);
        Diagnostic diagnostic = new Diagnostic(range, msg, fallback, SOURCE);
        tag(diagnostic, msg, fallback);
        return diagnostic;
    }

    private static Range rangeOf(final Message message, final org.codehaus.groovy.control.messages.Diagnostic groovy,
                                 final String text, final PositionEncoding encoding) {
        Range range;
        if (message instanceof SyntaxErrorMessage syntax) {
            SyntaxException cause = syntax.getCause();
            range = Positions.toRange(cause.getStartLine(), cause.getStartColumn(),
                    cause.getEndLine(), cause.getEndColumn(), text, encoding);
        } else {
            int line = groovy.line() > 0 ? groovy.line() : 1;
            int column = groovy.column() > 0 ? groovy.column() : 1;
            range = Positions.toRange(line, column, line, column + 1, text, encoding);
        }
        return range == null ? new Range(new Position(0, 0), new Position(0, 0)) : range;
    }

    private static String messageText(final Message message, final org.codehaus.groovy.control.messages.Diagnostic groovy) {
        String msg;
        if (message instanceof SyntaxErrorMessage syntax) {
            msg = syntax.getCause().getOriginalMessage();
        } else {
            msg = groovy.text();
        }
        return msg == null || msg.isBlank() ? message.toString() : msg;
    }

    private static Diagnostic toJavaDiagnostic(final JavaMessage message,
                                               final String text, final PositionEncoding encoding) {
        Range range = Positions.toRange(message.line(), message.column(), message.endLine(), message.endColumn(),
                text, encoding);
        if (range == null) {
            range = new Range(new Position(0, 0), new Position(0, 0));
        }
        DiagnosticSeverity severity = message.error() ? DiagnosticSeverity.Error : DiagnosticSeverity.Warning;
        Diagnostic diagnostic = new Diagnostic(range, message.text(), severity, "javac");
        tag(diagnostic, message.text(), severity);
        return diagnostic;
    }

    private static void tag(final Diagnostic diagnostic, final String msg, final DiagnosticSeverity fallback) {
        if (msg != null && msg.toLowerCase().contains("deprecated")) {
            diagnostic.setTags(List.of(DiagnosticTag.Deprecated));
        }
        if (msg != null && msg.toLowerCase().contains("unused")) {
            diagnostic.setTags(List.of(DiagnosticTag.Unnecessary));
            if (fallback == DiagnosticSeverity.Warning) {
                diagnostic.setSeverity(DiagnosticSeverity.Hint);
            }
        }
    }

    public static String diagnosticMessage(final Diagnostic diagnostic) {
        if (diagnostic == null || diagnostic.getMessage() == null) {
            return null;
        }
        var message = diagnostic.getMessage();
        if (message.isLeft()) {
            return message.getLeft();
        }
        if (message.isRight()) {
            return message.getRight().getValue();
        }
        return message.toString();
    }

}
