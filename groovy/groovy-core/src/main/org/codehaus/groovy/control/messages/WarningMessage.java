package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;

import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.ProcessingUnit;
import org.codehaus.groovy.syntax.CSTNode;



/**
 *  A class for warning messages.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class WarningMessage extends LocatedMessage
{
  //---------------------------------------------------------------------------
  // WARNING LEVELS

    public static final int NONE            = 0;  // For querying, ignore all errors
    public static final int LIKELY_ERRORS   = 1;  // Warning indicates likely error
    public static final int POSSIBLE_ERRORS = 2;  // Warning indicates possible error
    public static final int PARANOIA        = 3;  // Warning indicates paranoia on the part of the compiler
    
    
   /**
    *  Returns true if a warning would be relevant to the specified level.
    */
    
    public static boolean isRelevant( int actual, int limit )
    {
        return actual <= limit;
    }
    
    
    
   /**
    *  Returns true if this message is as or more important than the 
    *  specified importance level.
    */
    
    public boolean isRelevant( int importance )
    {
        return isRelevant( this.importance, importance );
    }
    
    
    
  //---------------------------------------------------------------------------
  // CONSTRUCTION AND DATA ACCESS

    private int importance;  // The warning level, for filtering
    
    
   /**
    *  Creates a new warning message.
    * 
    *  @param importance the warning level 
    *  @param message    the message text
    *  @param context    context information for locating the offending source text
    */
     
    public WarningMessage( int importance, String message, CSTNode context )
    {
        super( message, context );
        this.importance = importance;
    }

    
    
   /**
    *  Creates a new warning message.
    *
    *  @param importance the warning level 
    *  @param message    the message text
    *  @param data       additional data needed when generating the message
    *  @param context    context information for locating the offending source text
    */
     
    public WarningMessage( int importance, String message, Object data, CSTNode context )
    {
        super( message, data, context );
        this.importance = importance;
    }
    
    
    public void write( PrintWriter writer, ProcessingUnit owner, Janitor janitor )
    {
        writer.print( "warning: " );
        super.write( writer, owner, janitor );
    }

     
     
}



