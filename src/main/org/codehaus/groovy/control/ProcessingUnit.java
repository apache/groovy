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

import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * A base class for data structures that can collect messages and errors
 * during processing.
 *
 * @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 * @version $Id$
 */

public abstract class ProcessingUnit {

    /**
     * The current phase
     */
    protected int phase;
    /**
     * Set true if phase is finished
     */
    protected boolean phaseComplete;

    /**
     * Configuration and other settings that control processing
     */
    protected CompilerConfiguration configuration;
  
    /**
     * The ClassLoader to use during processing
     */
    protected ClassLoader classLoader;
    
    /**
     * a helper to share errors and report them
     */
    protected ErrorCollector errorCollector;


    /**
     * Initialize the ProcessingUnit to the empty state.
     */

    public ProcessingUnit(CompilerConfiguration configuration, ClassLoader classLoader, ErrorCollector er) {

        this.phase = Phases.INITIALIZATION;
        this.classLoader = (classLoader == null ? new CompilerClassLoader() : classLoader);

        this.errorCollector = er;
        configure((configuration == null ? new CompilerConfiguration() : configuration));
    }


    /**
     * Reconfigures the ProcessingUnit.
     */
    public void configure(CompilerConfiguration configuration) {
        this.configuration = configuration;
    }


    public CompilerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(CompilerConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the class loader in use by this ProcessingUnit.
     */

    public ClassLoader getClassLoader() {
        return classLoader;
    }


    /**
     * Sets the class loader for use by this ProcessingUnit.
     */

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }


    /**
     * Returns the current phase.
     */

    public int getPhase() {
        return this.phase;
    }


    /**
     * Returns the description for the current phase.
     */

    public String getPhaseDescription() {
        return Phases.getDescription(this.phase);
    }

    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }
    
    //---------------------------------------------------------------------------
    // PROCESSING


    /**
     * Marks the current phase complete and processes any
     * errors.
     */

    public void completePhase() throws CompilationFailedException {       
        errorCollector.failIfErrors();
        phaseComplete = true;
    }


    /**
     * A synonym for <code>gotoPhase( phase + 1 )</code>.
     */
    public void nextPhase() throws CompilationFailedException {
        gotoPhase(this.phase + 1);
    }


    /**
     * Wraps up any pending operations for the current phase
     * and switches to the next phase.
     */
    public void gotoPhase(int phase) throws CompilationFailedException {
        if (!this.phaseComplete) {
            completePhase();
        }

        this.phase = phase;
        this.phaseComplete = false;
    }

}




