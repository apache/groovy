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
    private Exception cause = null;   // The exception source of the message, if any
    

    public ExceptionMessage( Exception cause )
    {
        this.cause = cause;
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
    
    public void write( PrintWriter output, ProcessingUnit context, Janitor janitor )
    {
        String description = "General error during " + context.getPhaseDescription() + ": "; 
        
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
    }    
    
    
}



