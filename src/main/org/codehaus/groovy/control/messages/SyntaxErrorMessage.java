package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;



/**
 *  A class for error messages produced by the parser system.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class SyntaxErrorMessage extends Message
{
    protected SyntaxException cause = null;
    
    public SyntaxErrorMessage( SyntaxException cause )
    {
        this.cause = cause;
    }
    
    
   
   /**
    *  Returns the underlying SyntaxException.
    */
    
    public SyntaxException getCause()
    {
        return this.cause;
    }
    


   /**
    *  Writes out a nicely formatted summary of the syntax error. 
    */
    
    public void write( PrintWriter output, ProcessingUnit context, Janitor janitor )
    {
        SourceUnit source = (SourceUnit)context;   // This is reliably true

        String name   = source.getName();
        int    line   = getCause().getStartLine();
        int    column = getCause().getStartColumn();
        String sample = source.getSample( line, column, janitor );
        
        output.println( name + ": " + line + ": " + getCause().getMessage() );
        if( sample != null )
        {
            output.println( source.getSample(line, column, janitor) );
        }
        output.println("");
    }    
    
    
}



