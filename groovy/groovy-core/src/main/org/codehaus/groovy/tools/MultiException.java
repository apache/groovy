package org.codehaus.groovy.tools;

import org.codehaus.groovy.GroovyException;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MultiException
    extends GroovyException
{
    private Exception[] errors;

    public MultiException(Exception[] errors)
    {
        this.errors = errors;
    }

    public void printStackTrace()
    {
        for ( int i = 0 ; i < this.errors.length ; ++i )
        {
            this.errors[ i ].printStackTrace();
        }
    }

    public void printStackTrace(PrintStream s)
    {
        for ( int i = 0 ; i < this.errors.length ; ++i )
        {
            this.errors[ i ].printStackTrace( s );
        }
    }

    public void printStackTrace(PrintWriter s)
    {
        for ( int i = 0 ; i < this.errors.length ; ++i )
        {
            this.errors[ i ].printStackTrace( s );
        }
    }

    public String getMessage()
    {
        StringBuffer buffer = new StringBuffer();

        for ( int i = 0 ; i < this.errors.length ; ++i )
        {
            buffer.append( this.errors[ i ].getMessage() );

            if ( i < this.errors.length - 1 )
            {
                buffer.append( ", " );
            }
        }

        return buffer.toString();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        for ( int i = 0 ; i < this.errors.length ; ++i )
        {
            buffer.append( this.errors[ i ].toString() );

            if ( i < this.errors.length - 1 )
            {
                buffer.append( ", " );
            }
        }
        
        return buffer.toString();
    }
}
