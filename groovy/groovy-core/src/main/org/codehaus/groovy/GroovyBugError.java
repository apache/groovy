package org.codehaus.groovy;

public class GroovyBugError extends AssertionError
{
    private String    message;
    private Exception exception;

    public GroovyBugError( String message )
    {
        this.message = message;
    }
    
    public GroovyBugError( Exception exception )
    {
        this.exception = exception;
    }

    public String toString()
    {
        return getMessage();
    }

    public String getMessage()
    {
        if( message != null )
        {
            return message;
        }
        else
        {
            return "UNCAUGHT EXCEPTION: " + exception.getMessage();
        }
    }
    
    
    public Throwable getCause()
    {
        return this.exception;
    }
}
