package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.SourceUnit;



/**
 *  A base class for compilation messages.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class SimpleMessage extends Message
{
    protected String message;  // Message text
    protected Object data;     // Data, when the message text is an I18N identifier
    
    
    public SimpleMessage( String message ) 
    {
        this( message, null );
    }
    
    public SimpleMessage( String message, Object data )
    {
        this.message = message;
        this.data    = null;
    }
    
    
    public void write( PrintWriter writer, ProcessingUnit owner, Janitor janitor )
    {
        if( owner instanceof SourceUnit )
        {
            String name = ((SourceUnit)owner).getName();
            writer.println( "" + name + ": " + message );
        }
        else
        {
            writer.println( message );
        }
    }
    
    
    public String getMessage()
    {
        return message;
    }
    
}




