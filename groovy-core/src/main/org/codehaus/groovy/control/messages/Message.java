package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;



/**
 *  A base class for compilation messages.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public abstract class Message
{
    
    
   /**
    *  Writes the message to the specified PrintWriter.  The supplied
    *  ProcessingUnit is the unit that holds this Message.
    */
    
    public abstract void write( PrintWriter writer, Janitor janitor );
    
    
   /**
    *  A synonyn for write( writer, owner, null ).
    */
    
    public final void write( PrintWriter writer)
    {
        write( writer,  null );
    }
    
    
    
  //---------------------------------------------------------------------------
  // FACTORY METHODS
    
    
   /**
    *  Creates a new Message from the specified text.
    */
    
    public static Message create( String text, ProcessingUnit owner )
    {
        return new SimpleMessage( text, owner );
    }
    
    
          
   /**
    *  Creates a new Message from the specified text.
    */
     
    public static Message create( String text, Object data, ProcessingUnit owner  )
    {
        return new SimpleMessage( text, data, owner);
    }
     
     
           
   /**
    *  Creates a new Message from the specified SyntaxException.
    */
      
    public static Message create( SyntaxException error, SourceUnit owner )
    {
        return new SyntaxErrorMessage( error, owner );
    }
      
    
      
    
}




