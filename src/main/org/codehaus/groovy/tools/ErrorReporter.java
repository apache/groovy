/*
 $Id$

 Copyright 2004 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */


package org.codehaus.groovy.tools;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.codehaus.groovy.GroovyExceptionInterface;
import org.codehaus.groovy.control.CompilationFailedException;



/**
 *  Provides services for reporting compilation errors to the
 *  user.  Primary entry point is <code>write()</code>.
 *
 *  @author <a href="mailto:cpoirier%20AT%20tapestry_os%20DOT%20org">Chris Poirier</a>
 *  @version $Revision$
 */

public class ErrorReporter
{
    private Throwable   base     = null;    // The exception on which to report
    private boolean     debug    = false;   // If true, stack traces are always output

    private Object      output   = null;    // The stream/writer to which to output


   /**
    *  Configures a new Reporter.  Default mode is not to report a stack trace unless
    *  the error was not of one of the supported types.
    *
    *  @param e  the exception on which to report
    */

    public ErrorReporter( Throwable e )
    { 
        this.base     = e;
    }


   /**
    *  Configures a new Reporter.  
    *
    *  @param e      the exception on which to report
    *  @param debug  if set, stack traces will be output for all reports
    */

    public ErrorReporter( Throwable e, boolean debug )
    {
        this.base  = e;
        this.debug = debug;
    }


   /**
    *  Writes the error to the specified <code>PrintStream</code>.
    */

    public void write( PrintStream stream )
    {
        this.output = stream;
        dispatch( base, false );
        stream.flush();
    }


   /**
    *  Writes the error to the specified <code>PrintWriter</code>.
    */

    public void write( PrintWriter writer )
    {
        this.output = writer;
        dispatch( base, false );
        writer.flush();
    }


   /**
    *  Runs the report once all initialization is complete.
    */

    protected void dispatch( Throwable object, boolean child )
    {
        if( object instanceof CompilationFailedException )
        {
            report( (CompilationFailedException)object, child );
        }
        else if( object instanceof GroovyExceptionInterface )
        {
            report( (GroovyExceptionInterface)object, child );
        }
        else if( object instanceof Exception )
        {
            report( (Exception)object, child );
        }
        else
        {
            report( object, child );
        }

    }



  //---------------------------------------------------------------------------
  // REPORTING ROUTINES


   /**
    *  For CompilationFailedException.
    */

    protected void report( CompilationFailedException e, boolean child )
    {
        println( e.toString() );
        stacktrace( e, false );
    }



   /**
    *  For GroovyException.
    */

    protected void report( GroovyExceptionInterface e, boolean child )
    {
        println( ((Exception)e).getMessage() );
        stacktrace( (Exception)e, false );
    }



   /**
    *  For Exception.
    */

    protected void report( Exception e, boolean child )
    {
        println( e.getMessage() );
        stacktrace( e, false );
    }



   /**
    *  For everything else.
    */

    protected void report( Throwable e, boolean child )
    {
        println( ">>> a serious error occurred: " + e.getMessage() );
        stacktrace( e, true );
    }



  //---------------------------------------------------------------------------
  // GENERAL SUPPORT ROUTINES


   /**
    *  Prints a line to the underlying <code>PrintStream</code>
    */

    protected void println( String line )
    {
        if( output instanceof PrintStream )
        {
            ((PrintStream)output).println( line );
        }
        else
        {
            ((PrintWriter)output).println( line );
        }
    }

    protected void println( StringBuffer line )
    {
        if( output instanceof PrintStream )
        {
            ((PrintStream)output).println( line );
        }
        else
        {
            ((PrintWriter)output).println( line );
        }
    }


   /**
    *  Displays an exception's stack trace, if <code>debug</code> or 
    *  <code>always</code>.
    */

    protected void stacktrace( Throwable e, boolean always )
    {
        if( debug || always )
        {
            println( ">>> stacktrace:" );
            if( output instanceof PrintStream )
            {
                e.printStackTrace( (PrintStream)output );
            }
            else
            {
                e.printStackTrace( (PrintWriter)output );
            }
        }
    }



}
