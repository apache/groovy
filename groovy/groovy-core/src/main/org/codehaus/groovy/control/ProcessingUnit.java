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
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;


/**
 *  A base class for data structures that can collect messages and errors
 *  during processing.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public abstract class ProcessingUnit
{

  //---------------------------------------------------------------------------
  // CONSTRUCTION AND SUCH

    protected LinkedList    warnings;       // WarningMessages collected during processing
    protected LinkedList    errors;         // ErrorMessages collected during processing
    protected boolean       fatal;          // Set on the first fatal error

    protected int           phase;          // The current phase
    protected boolean       phaseComplete;  // Set true if phase is finished

    protected CompilerConfiguration configuration;  // Configuration and other settings that control processing
    protected int           warningLevel;   // Warnings will be filtered on this level
    protected PrintWriter   output;         // A place to send warning output
    protected int           tolerance;      // The number of non-fatal errors to allow before fail()

    protected ClassLoader   classLoader;    // The ClassLoader to use during processing



   /**
    *  Initialize the ProcessingUnit to the empty state.
    */

    public ProcessingUnit( CompilerConfiguration configuration, ClassLoader classLoader )
    {
        this.warnings      = null;
        this.errors        = null;
        this.fatal         = false;

        this.phase         = Phases.INITIALIZATION;
        this.classLoader   = (classLoader == null ? new CompilerClassLoader() : classLoader);

        configure( (configuration == null ? new CompilerConfiguration() : configuration) );
    }



   /**
    *  Reconfigures the ProcessingUnit.
    */

    public void configure( CompilerConfiguration configuration )
    {
        this.configuration = configuration;
        this.warningLevel  = configuration.getWarningLevel();
        this.output        = configuration.getOutput();
        this.tolerance     = configuration.getTolerance();
    }



   /**
    *  Returns the class loader in use by this ProcessingUnit.
    */

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }



   /**
    *  Sets the class loader for use by this ProcessingUnit.
    */

    public void setClassLoader( ClassLoader loader )
    {
        this.classLoader = loader;
    }



   /**
    *  Returns the current phase.
    */

    public int getPhase()
    {
        return this.phase;
    }



   /**
    *  Returns the description for the current phase.
    */

    public String getPhaseDescription()
    {
        return Phases.getDescription( this.phase );
    }



   /**
    *  Returns the list of warnings, or null if there are none.
    */

    public List getWarnings()
    {
        return this.warnings;
    }



   /**
    *  Returns the list of errors, or null if there are none.
    */

    public List getErrors()
    {
        return this.errors;
    }



   /**
    *  Returns the number of warnings.
    */

    public int getWarningCount()
    {
        return ((this.warnings == null) ? 0 : this.warnings.size());
    }



   /**
    *  Returns the number of errors.
    */

    public int getErrorCount()
    {
        return ((this.errors == null) ? 0 : this.errors.size());
    }



   /**
    *  Returns the specified warning message, or null.
    */

    public WarningMessage getWarning( int index )
    {
        if( index < getWarningCount() )
        {
            return (WarningMessage)this.warnings.get(index);
        }

        return null;
    }



   /**
    *  Returns the specified error message, or null.
    */

    public Message getError( int index )
    {
        if( index < getErrorCount() )
        {
            return (Message)this.errors.get(index);
        }

        return null;
    }



   /**
    *  Convenience routine to return the specified error's
    *  underlying SyntaxException, or null if it isn't one.
    */

    public SyntaxException getSyntaxError( int index )
    {
        SyntaxException exception = null;

        Message message = getError( index );
        if( message != null && message instanceof SyntaxErrorMessage )
        {
            exception = ((SyntaxErrorMessage)message).getCause();
        }

        return exception;
    }



   /**
    *  Convenience routine to return the specified error's
    *  underlying Exception, or null if it isn't one.
    */

    public Exception getException( int index )
    {
        Exception exception = null;

        Message message = getError( index );
        if( message != null )
        {
            if( message instanceof ExceptionMessage )
            {
                exception = ((ExceptionMessage)message).getCause();
            }
            else if( message instanceof SyntaxErrorMessage )
            {
                exception = ((SyntaxErrorMessage)message).getCause();
            }
        }

        return exception;
    }




  //---------------------------------------------------------------------------
  // MESSAGES


   /**
    *  Adds a WarningMessage to the message set.
    */

    public void addWarning( WarningMessage message )
    {
        if( message.isRelevant(this.warningLevel) )
        {
            if( this.warnings == null )
            {
                this.warnings = new LinkedList();
            }

            this.warnings.add( message );
        }
    }



   /**
    *  Adds a non-fatal error to the message set.
    */

    public void addError( Message message ) throws CompilationFailedException
    {
        if( this.errors == null )
        {
            this.errors = new LinkedList();
        }

        this.errors.add( message );

        if( this.errors.size() >= this.tolerance )
        {
            fail();
        }
    }



   /**
    *  Adds an optionally-fatal error to the message set.  Throws
    *  the unit as a PhaseFailedException, if the error is fatal.
    */

    public void addError( Message message, boolean fatal ) throws CompilationFailedException
    {
        if( fatal )
        {
            addFatalError( message );
        }
        else
        {
            addError( message );
        }
    }



   /**
    *  Adds a fatal exception to the message set and throws
    *  the unit as a PhaseFailedException.
    */

    public void addFatalError( Message message ) throws CompilationFailedException
    {
        addError( message );
        fail();
    }


    public void addException(Exception cause) throws CompilationFailedException {
        addError(new ExceptionMessage(cause));
        fail();
    }


  //---------------------------------------------------------------------------
  // PROCESSING


   /**
    *  Returns true if there are any errors pending.
    */

    public boolean hasErrors()
    {
        return this.errors != null;
    }



   /**
    *  Marks the current phase complete and processes any
    *  errors.
    */

    public void completePhase() throws CompilationFailedException
    {
        //
        // First up, display and clear any pending warnings.

        if( this.warnings != null )
        {
            Janitor janitor = new Janitor();

            try
            {
                Iterator iterator = this.warnings.iterator();
                while( iterator.hasNext() )
                {
                    WarningMessage warning = (WarningMessage)iterator.next();
                    warning.write( output, this, janitor );
                }

                this.warnings = null;
            }
            finally
            {
                janitor.cleanup();
            }
        }

        //
        // Then either fail() or update the phase and return

        if( this.hasErrors() )
        {
            fail();
        }
        else
        {
            phaseComplete = true;
        }
    }



   /**
    *  A synonym for <code>gotoPhase( phase + 1 )</code>.
    */

    public void nextPhase() throws CompilationFailedException
    {
        gotoPhase( this.phase + 1 );
    }



   /**
    *  Wraps up any pending operations for the current phase
    *  and switches to the next phase.
    */

    public void gotoPhase( int phase ) throws CompilationFailedException
    {
        if( !this.phaseComplete )
        {
            completePhase();
        }

        this.phase = phase;
        this.phaseComplete = false;
    }



   /**
    *  Causes the current phase to fail by throwing a
    *  CompilationFailedException.
    */

    protected void fail() throws CompilationFailedException
    {
       // lets find the first error exception
       Throwable firstException = null;
       for (Iterator iter = errors.iterator(); iter.hasNext();) {
           Message message = (Message) iter.next();
           if (message instanceof ExceptionMessage) {
               ExceptionMessage exceptionMessage = (ExceptionMessage) message;
               firstException = exceptionMessage.getCause();
               if (firstException != null) {
                   break;
               }
           }
       }

       if (firstException != null) {
           throw new CompilationFailedException( phase, this, firstException );
       }
       else {
           throw new CompilationFailedException( phase, this );
       }
    }




    //---------------------------------------------------------------------------
  // OUTPUT


   /**
    *  Writes error messages to the specified PrintWriter.
    */

    public void write( PrintWriter writer, Janitor janitor )
    {
        if( this.warnings != null )
        {
            Iterator iterator = this.warnings.iterator();
            while( iterator.hasNext() )
            {
                WarningMessage warning = (WarningMessage)iterator.next();
                warning.write( writer, this, janitor );
            }

            this.warnings = null;
        }

        if( this.errors != null )
        {
            Iterator iterator = this.errors.iterator();
            while( iterator.hasNext() )
            {
                Message message = (Message)iterator.next();
                message.write( writer, this, janitor );
            }

            //why? this nukes the errors once a getString call is made
            //this.errors = null;
        }
    }


}




