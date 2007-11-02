/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.control;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.LocatedMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * A base class for collecting messages and errors during processing.
 * Each CompilationUnit should have one and SourceUnits should share
 * their ErrorCollector with the CompilationUnit.
 *
 * @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @version $Id$
 */
public class ErrorCollector {
    
    /**
     * WarningMessages collected during processing
     */
    protected LinkedList warnings;
    /**
     * ErrorMessages collected during processing
     */
    protected LinkedList errors;
    /**
     * Configuration and other settings that control processing
     */
    protected CompilerConfiguration configuration;

    /**
     * Initialize the ErrorReporter.
     */
    public ErrorCollector(CompilerConfiguration configuration) {
        this.warnings = null;
        this.errors = null;
        
        this.configuration = configuration;
    }
    
    public void addCollectorContents(ErrorCollector er) {
        if (er.errors!=null) {
            if (errors==null) {
                errors = er.errors;
            } else {
                errors.addAll(er.errors);
            }
        }
        if (er.warnings!=null) {
            if (warnings==null) {
                warnings = er.warnings;
            } else {
                warnings.addAll(er.warnings);
            }            
        }
    }
    
    
    
    /**
     * Adds an error to the message set, but don't fail.
     */
    public void addErrorAndContinue(Message message) {
        if (this.errors == null) {
            this.errors = new LinkedList();
        }

        this.errors.add(message);
    }
    
    /**
     * Adds a non-fatal error to the message set.
     */
    public void addError(Message message) throws CompilationFailedException {
        addErrorAndContinue(message);

        if (errors!=null && this.errors.size() >= configuration.getTolerance()) {
            failIfErrors();
        }
    }
    
    /**
     * Adds an optionally-fatal error to the message set.  Throws
     * the unit as a PhaseFailedException, if the error is fatal.
     */
    public void addError(Message message, boolean fatal) throws CompilationFailedException {
        if (fatal) {
            addFatalError(message);
        }
        else {
            addError(message);
        }
    }

    
    /**
     * Convenience wrapper for addError().
     */
    public void addError(SyntaxException error, SourceUnit source) throws CompilationFailedException {
        addError(Message.create(error, source), error.isFatal());
    }


    /**
     * Convenience wrapper for addError().
     */
    public void addError(String text, CSTNode context, SourceUnit source) throws CompilationFailedException {
        addError(new LocatedMessage(text, context, source));
    }
    
    
    /**
     * Adds a fatal exception to the message set and throws
     * the unit as a PhaseFailedException.
     */
    public void addFatalError(Message message) throws CompilationFailedException {
        addError(message);
        failIfErrors();
    }


    public void addException(Exception cause, SourceUnit source) throws CompilationFailedException {
        addError(new ExceptionMessage(cause,configuration.getDebug(),source));
        failIfErrors();
    }

    /**
     * Returns true if there are any errors pending.
     */
    public boolean hasErrors() {
        return this.errors != null;
    }
    
    /**
     * Returns true if there are any warnings pending.
     */
    public boolean hasWarnings() {
        return this.warnings != null;
    }
    
    /**
     * Returns the list of warnings, or null if there are none.
     */
    public List getWarnings() {
        return this.warnings;
    }

    /**
     * Returns the list of errors, or null if there are none.
     */
    public List getErrors() {
        return this.errors;
    }

    /**
     * Returns the number of warnings.
     */
    public int getWarningCount() {
        return ((this.warnings == null) ? 0 : this.warnings.size());
    }

    /**
     * Returns the number of errors.
     */
    public int getErrorCount() {
        return ((this.errors == null) ? 0 : this.errors.size());
    }

    /**
     * Returns the specified warning message, or null.
     */
    public WarningMessage getWarning(int index) {
        if (index < getWarningCount()) {
            return (WarningMessage) this.warnings.get(index);
        }
        return null;
    }

    /**
     * Returns the specified error message, or null.
     */
    public Message getError(int index) {
        if (index < getErrorCount()) {
            return (Message) this.errors.get(index);
        }
        return null;
    }

    /**
     * Returns the last error reported
     */
    public Message getLastError() {
        return (Message) this.errors.getLast();
    }
    
    /**
     * Convenience routine to return the specified error's
     * underlying SyntaxException, or null if it isn't one.
     */
    public SyntaxException getSyntaxError(int index) {
        SyntaxException exception = null;

        Message message = getError(index);
        if (message != null && message instanceof SyntaxErrorMessage) {
            exception = ((SyntaxErrorMessage) message).getCause();
        }
        return exception;
    }

    /**
     * Convenience routine to return the specified error's
     * underlying Exception, or null if it isn't one.
     */
    public Exception getException(int index) {
        Exception exception = null;

        Message message = getError(index);
        if (message != null) {
            if (message instanceof ExceptionMessage) {
                exception = ((ExceptionMessage) message).getCause();
            }
            else if (message instanceof SyntaxErrorMessage) {
                exception = ((SyntaxErrorMessage) message).getCause();
            }
        }
        return exception;
    }

    /**
     * Adds a WarningMessage to the message set.
     */
    public void addWarning(WarningMessage message) {
        if (message.isRelevant(configuration.getWarningLevel())) {
            if (this.warnings == null) {
                this.warnings = new LinkedList();
            }

            this.warnings.add(message);
        }
    }


    /**
     * Convenience wrapper for addWarning() that won't create an object
     * unless it is relevant.
     */
    public void addWarning(int importance, String text, CSTNode context, SourceUnit source) {
        if (WarningMessage.isRelevant(importance, configuration.getWarningLevel())) {
            addWarning(new WarningMessage(importance, text, context, source));
        }
    }
    
    
    /**
     * Convenience wrapper for addWarning() that won't create an object
     * unless it is relevant.
     */
    public void addWarning(int importance, String text, Object data, CSTNode context, SourceUnit source) {
        if (WarningMessage.isRelevant(importance, configuration.getWarningLevel())) {
            addWarning(new WarningMessage(importance, text, data, context, source));
        }
    }
   

    /**
     * Causes the current phase to fail by throwing a
     * CompilationFailedException.
     */
    protected void failIfErrors() throws CompilationFailedException {
        if (hasErrors()) {
            throw new MultipleCompilationErrorsException(this);
        }
    }
    
    //---------------------------------------------------------------------------
    // OUTPUT


    private void write(PrintWriter writer, Janitor janitor, List messages, String txt) {
        if (messages==null || messages.size()==0) return;
        Iterator iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message message = (Message) iterator.next();
            message.write(writer, janitor);
            
            if (configuration.getDebug() && (message instanceof SyntaxErrorMessage)){
                SyntaxErrorMessage sem = (SyntaxErrorMessage) message;
                sem.getCause().printStackTrace(writer);
            } 
        }

        writer.println();
        writer.print(messages.size());
        writer.print(" "+txt);
        if (messages.size()>1) writer.print("s");
        writer.println();
    }
    
    /**
     * Writes error messages to the specified PrintWriter.
     */
    public void write(PrintWriter writer, Janitor janitor) {
        write(writer,janitor,warnings,"warning");
        write(writer,janitor,errors,"error");
    }

}
