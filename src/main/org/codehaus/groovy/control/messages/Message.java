package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;
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
    
    public abstract void write( PrintWriter writer, ProcessingUnit owner, Janitor janitor );
    
    
   /**
    *  A synonyn for write( writer, owner, null ).
    */
    
    public final void write( PrintWriter writer, ProcessingUnit owner )
    {
        write( writer, owner, null );
    }
    
    
    
  //---------------------------------------------------------------------------
  // FACTORY METHODS
    
    
   /**
    *  Creates a new Message from the specified text.
    */
    
    public static Message create( String text )
    {
        return new SimpleMessage( text );
    }
    
    
          
   /**
    *  Creates a new Message from the specified text.
    */
     
    public static Message create( String text, Object data )
    {
        return new SimpleMessage( text, data );
    }
     
     
           
   /**
    *  Creates a new Message from the specified SyntaxException.
    */
      
    public static Message create( SyntaxException error )
    {
        return new SyntaxErrorMessage( error );
    }
      
      
      
    
}




