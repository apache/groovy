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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.LocatedMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A base class for collecting messages and errors during processing.
 * Each CompilationUnit should have an ErrorCollector, and the SourceUnits
 * should share their ErrorCollector with the CompilationUnit.
 */
public class ErrorCollector implements Serializable {

    private static final long serialVersionUID = 2844774170905056755L;

    /**
     * ErrorMessages collected during processing
     */
    protected LinkedList<Message> errors;

    /**
     * WarningMessages collected during processing
     */
    protected LinkedList<WarningMessage> warnings;

    /**
     * Configuration and other settings that control processing
     */
    protected final CompilerConfiguration configuration;

    /**
     * Initialize the ErrorReporter.
     */
    public ErrorCollector(final CompilerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void addCollectorContents(final ErrorCollector that) {
        if (that.errors != null) {
            if (this.errors == null) {
                this.errors = that.errors;
            } else {
                this.errors.addAll(that.errors);
            }
        }
        if (that.warnings != null) {
            if (this.warnings == null) {
                this.warnings = that.warnings;
            } else {
                this.warnings.addAll(that.warnings);
            }
        }
    }

    public void addErrorAndContinue(final SyntaxException error, final SourceUnit source) throws CompilationFailedException {
        addErrorAndContinue(Message.create(error, source));
    }

    /**
     * Adds an error to the message set, but does not cause a failure. The message is not required to have a source
     * line and column specified, but it is best practice to try and include that information.
     */
    public void addErrorAndContinue(final Message message) {
        if (errors == null) {
            errors = new LinkedList<>();
        }
        errors.add(message);
    }

    public void addErrorAndContinue(String error, ASTNode node, SourceUnit source) {
        addErrorAndContinue(new SyntaxErrorMessage(
                new SyntaxException(error,
                        node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()),
                source));
    }

    /**
     * Adds a non-fatal error to the message set, which may cause a failure if the error threshold is exceeded.
     * The message is not required to have a source line and column specified, but it is best practice to try
     * and include that information.
     */
    public void addError(final Message message) throws CompilationFailedException {
        addErrorAndContinue(message);

        if (errors != null && errors.size() >= configuration.getTolerance()) {
            failIfErrors();
        }
    }

    /**
     * Adds an optionally-fatal error to the message set.
     * The message is not required to have a source line and column specified, but it is best practice to try
     * and include that information.
     * @param fatal
     *      if true then then processing will stop
     */
    public void addError(final Message message, final boolean fatal) throws CompilationFailedException {
        if (fatal) {
            addFatalError(message);
        } else {
            addError(message);
        }
    }

    public void addError(final SyntaxException error, final SourceUnit source) throws CompilationFailedException {
        addError(Message.create(error, source), error.isFatal());
    }

    public void addError(final String error, final CSTNode context, final SourceUnit source) throws CompilationFailedException {
        addError(new LocatedMessage(error, context, source));
    }

    public void addException(final Exception exception, final SourceUnit source) throws CompilationFailedException {
        addError(new ExceptionMessage(exception, configuration.getDebug(), source));
        failIfErrors();
    }

    /**
     * Adds an error to the message set and throws CompilationFailedException.
     */
    public void addFatalError(final Message message) throws CompilationFailedException {
        addError(message);
        failIfErrors();
    }

    /**
     * Adds a warning to the message set.
     */
    public void addWarning(final WarningMessage message) {
        if (message.isRelevant(configuration.getWarningLevel())) {
            if (warnings == null) {
                warnings = new LinkedList<>();
            }
            warnings.add(message);
        }
    }

    /**
     * Adds a warning to the message set if it is relevant.
     */
    public void addWarning(final int importance, final String text, final CSTNode context, final SourceUnit source) {
        if (WarningMessage.isRelevant(importance, configuration.getWarningLevel())) {
            addWarning(new WarningMessage(importance, text, context, source));
        }
    }

    /**
     * Adds a warning to the message set if it is relevant.
     */
    public void addWarning(final int importance, final String text, final Object data, final CSTNode context, final SourceUnit source) {
        if (WarningMessage.isRelevant(importance, configuration.getWarningLevel())) {
            addWarning(new WarningMessage(importance, text, data, context, source));
        }
    }

    /**
     * @return the compiler configuration used to create this error collector
     */
    public CompilerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the number of errors.
     */
    public int getErrorCount() {
        return (hasErrors() ? errors.size() : 0);
    }

    /**
     * Returns the specified error message, or null.
     */
    public Message getError(final int index) {
        if (index < getErrorCount()) {
            return errors.get(index);
        }
        return null;
    }

    /**
     * Returns the list of errors, or null if there are none.
     */
    public List<? extends Message> getErrors() {
        return errors;
    }

    /**
     * Returns true if there are any errors pending.
     */
    public boolean hasErrors() {
        return (errors != null);
    }

    /**
     * Returns the number of warnings.
     */
    public int getWarningCount() {
        return (hasWarnings() ? warnings.size() : 0);
    }

    /**
     * Returns the specified warning message, or null.
     */
    public WarningMessage getWarning(final int index) {
        if (index < getWarningCount()) {
            return warnings.get(index);
        }
        return null;
    }

    /**
     * Returns the list of warnings, or null if there are none.
     */
    public List<WarningMessage> getWarnings() {
        return warnings;
    }

    /**
     * Returns true if there are any warnings pending.
     */
    public boolean hasWarnings() {
        return (warnings != null);
    }

    //

    /**
     * Returns the last error reported.
     */
    public Message getLastError() {
        return errors.getLast();
    }

    /**
     * Returns the specified error's underlying Exception, or null if it isn't one.
     */
    public Exception getException(final int index) {
        Exception exception = null;
        Message message = getError(index);
        if (message != null) {
            if (message instanceof ExceptionMessage) {
                exception = ((ExceptionMessage) message).getCause();
            } else if (message instanceof SyntaxErrorMessage) {
                exception = ((SyntaxErrorMessage) message).getCause();
            }
        }
        return exception;
    }

    /**
     * Returns the specified error's underlying SyntaxException, or null if it isn't one.
     */
    public SyntaxException getSyntaxError(final int index) {
        SyntaxException exception = null;
        Message message = getError(index);
        if (message instanceof SyntaxErrorMessage) {
            exception = ((SyntaxErrorMessage) message).getCause();
        }
        return exception;
    }

    /**
     * Causes the current phase to fail by throwing a CompilationFailedException.
     */
    protected void failIfErrors() throws CompilationFailedException {
        if (hasErrors()) {
            throw new MultipleCompilationErrorsException(this);
        }
    }

    //---------------------------------------------------------------------------
    // OUTPUT

    private void write(final PrintWriter writer, final Janitor janitor, final List<? extends Message> messages, final String txt) {
        if (messages == null || messages.isEmpty()) return;

        for (Message message : messages) {
            message.write(writer, janitor);
            if (configuration.getDebug() && (message instanceof SyntaxErrorMessage)) {
                ((SyntaxErrorMessage) message).getCause().printStackTrace(writer);
            }
            writer.println();
        }

        writer.print(messages.size());
        writer.print(" " + txt);
        if (messages.size() > 1) {
            writer.print("s");
        }
        writer.println();
    }

    /**
     * Writes error messages to the specified PrintWriter.
     */
    public void write(final PrintWriter writer, final Janitor janitor) {
        write(writer, janitor, warnings, "warning");
        write(writer, janitor, errors, "error");
    }
}
