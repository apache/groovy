/*
$Id$

Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

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

        if (this.errors.size() >= configuration.getTolerance()) {
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
        if (hasErrors()) throw new MultipleCompilationErrorsException(this);
    }
    
    //---------------------------------------------------------------------------
    // OUTPUT


    /**
     * Writes error messages to the specified PrintWriter.
     */

    public void write(PrintWriter writer, Janitor janitor) {
        if (this.warnings != null) {
            Iterator iterator = this.warnings.iterator();
            while (iterator.hasNext()) {
                WarningMessage warning = (WarningMessage) iterator.next();
                warning.write(writer, janitor);
            }

            writer.println();
            writer.print(warnings.size());
            writer.println(" Warnings");
            
            this.warnings = null;
        }

        if (this.errors != null) {
            Iterator iterator = this.errors.iterator();
            while (iterator.hasNext()) {
                Message message = (Message) iterator.next();
                message.write(writer, janitor);
                
                if (configuration.getDebug() && (message instanceof SyntaxErrorMessage)) {
                    SyntaxErrorMessage sem = (SyntaxErrorMessage) message;
                    SyntaxException se = sem.getCause();
                    se.printStackTrace(writer);
                }
            }

            writer.println();
            writer.print(errors.size());
            writer.println(" Errors");
        }
    }

}
