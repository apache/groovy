package org.codehaus.groovy.syntax;

import org.codehaus.groovy.GroovyException;
import java.io.IOException;

/**
 *  Encapsulates non-specific i/o exceptions.
 */

public class ReadException extends GroovyException
{
    private IOException cause = null;

    public ReadException( IOException cause )
    {
        super();
        this.cause = cause;
    }

    public ReadException( String message, IOException cause )
    {
        super( message );
        this.cause = cause;
    }

    public IOException getIOCause()
    {
        return this.cause;
    }

    public String toString()
    {
       String message = super.getMessage();
       if( message == null || message.trim() == "" )
       {
          message = cause.getMessage();
       }

       return message;
    }

    public String getMessage()
    {
       return toString();
    }
}
