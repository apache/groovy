package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;



/**
 *  A class for error messages produced by the parser system.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class ExceptionMessage extends Message
{
    protected boolean verbose = true;

    private Exception cause = null;   // The exception source of the message, if any
    ProcessingUnit owner = null;

    public ExceptionMessage( Exception cause, boolean v, ProcessingUnit owner )
    {
        this.verbose = v;
        this.cause = cause;
        this.owner = owner;
    }
    
    
   
   /**
    *  Returns the underlying Exception.
    */

    public Exception getCause()
    {
        return this.cause;
    }
    


   /**
    *  Writes out a nicely formatted summary of the exception. 
    */
    
    public void write( PrintWriter output, Janitor janitor )
    {
        String description = "General error during " + owner.getPhaseDescription() + ": "; 
        
        String message = cause.getMessage();
        if( message != null )
        {
            output.println( description + message );
        }
        else
        {
            output.println( description + cause );
        }
        output.println("");

        if (verbose) {
            cause.printStackTrace(output);
        }
    }
    
}



