package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.CSTNode;



/**
 *  A base class for compilation messages.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class LocatedMessage extends SimpleMessage
{
    protected CSTNode context;  // The CSTNode that indicates the location to which the message applies
    
    
    public LocatedMessage( String message, CSTNode context ) 
    {
        super( message );
        this.context = context;
    }
    
    
    public LocatedMessage( String message, Object data, CSTNode context ) 
    {
        super( message, data );
        this.context = context;
    }
    
    
    public void write( PrintWriter writer, ProcessingUnit owner, Janitor janitor )
    {
        SourceUnit source = (SourceUnit)owner;   // This is reliably true
        
        String name   = source.getName();
        int    line   = context.getStartLine();
        int    column = context.getStartColumn();
        String sample = source.getSample( line, column, janitor );
        
        if( sample != null )
        {
            writer.println( source.getSample(line, column, janitor) );
        }
        
        writer.println( name + ": " + line + ": " + this.message );
        writer.println("");
    }
    
}




