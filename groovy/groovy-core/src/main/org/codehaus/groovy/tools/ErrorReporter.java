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

import org.codehaus.groovy.GroovyException;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.tools.ExceptionCollector;
import org.codehaus.groovy.tools.CompilationFailuresException;

import java.io.PrintStream;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.util.Iterator;
import java.util.Map;



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
    private Source      source   = null;    // If set, a Source object representing the current source
                                            //  - use setSource() to set!

    private Map         sources  = null;    // If set, a map of String sources, keyed on description
    private PrintStream stream   = null;    // The stream to which to output


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
    *  Allows you to supply a map of String sources, if sources aren't files.
    *  The key must be the description that would have been used by the
    *  <code>Compiler</code> when storing an <code>ExceptionCollector</code>
    *  in the <code>CompilationFailuresException</code>.
    */

    public void setSources( Map sources )
    {
        this.sources = sources;
    }


   /**
    *  Writes the error to the specified <code>PrintStream</code>.
    */

    public void write( PrintStream stream )
    {
        this.stream = stream;

        if( base instanceof CompilationFailuresException )
        {
            report( (CompilationFailuresException)base, false );
        }
        else if( base instanceof ExceptionCollector )
        {
            report( (ExceptionCollector)base, false );
        }
        else
        {
            report( base, false );
        }

        cleanup();
    }



  //---------------------------------------------------------------------------
  // REPORTING ROUTINES


   /**
    *  For CompilationFailuresException.
    */

    protected void report( CompilationFailuresException e, boolean child )
    {
        Iterator sources = e.iterator();
        while( sources.hasNext() )
        {
            String             source     = (String)sources.next();
            ExceptionCollector collection = e.get( source );

            setSource( source );

            report( collection, true );
        }
    }


   /**
    *  For ExceptionCollector.
    */

    protected void report( ExceptionCollector e, boolean child )
    {
        Iterator iterator = e.iterator();
        while( iterator.hasNext() )
        {
            GroovyException error = (GroovyException)iterator.next();
            if( error instanceof SyntaxException )
            {
               report( (SyntaxException)error, true );
            }
            else
            {
               report( error, true );
            }
            println( "" );
        }
    }


   /**
    *  For SyntaxException.
    */

    protected void report( SyntaxException e, boolean child )
    {
        if( source != null )
        {
            String description = source.getDescriptor() + ":" + e.getLine() + ": " + e.getMessage();
            println( description );

            String sample = source.getSample( e.getLine(), e.getEndColumn() - 1 );
            if( sample != null )
            {
                println( sample );
            }
        }
        else
        {
           println( "unknown:" + e.getLine() + ": " + e.getMessage() );
        }

        stacktrace( e, false );
    }



   /**
    *  For GroovyException.
    */

    protected void report( GroovyException e, boolean child )
    {
        println( e.getMessage() );
        stacktrace( e, false );
    }



   /**
    *  For everything else.
    */

    protected void report( Throwable e, boolean child )
    {
        println( ">>> caught a bug:" );
        stacktrace( e, true );
    }



  //---------------------------------------------------------------------------
  // GENERAL SUPPORT ROUTINES


   /**
    *  Prints a line to the underlying <code>PrintStream</code>
    */

    protected void println( String line )
    {
        stream.println( line );
    }

    protected void println( StringBuffer line )
    {
        stream.println( line );
    }


   /**
    *  Displays an exception's stack trace, if <code>debug</code> or 
    *  <code>always</code>.
    */

    protected void stacktrace( Throwable e, boolean always )
    {
        if( debug || always )
        {
           stream.println( ">>> stacktrace:" );
           e.printStackTrace( stream );
        }
    }


   /**
    *  Returns a string made up of repetitions of the specified string.
    */

    protected String repeatString( String pattern, int repeats )
    {
        StringBuffer buffer = new StringBuffer( pattern.length() * repeats );
        for( int i = 0; i < repeats; i++ )
        {
            buffer.append( pattern );
        }

        return new String( buffer );
    }


   /**
    *  Returns the end-of-line marker.
    */

    protected String eol()
    {
        return System.getProperty( "line.separator", "\n" );
    }


   /**
    *  Sets the source, closing any old source.
    */

    protected void setSource( String path )
    {
       if( source != null )
       {
           source.close();
       }

       source = new Source( path, sources == null );
    }


   /**
    *  Cleans up any state information after a <code>write()</code> operation.
    */

    protected void cleanup()
    {
        if( source != null )
        { 
            source.close();
            source = null;
        }
    }




  //---------------------------------------------------------------------------
  // SOURCE ACCESS

    private class Source
    {
        private String         path   = null;    // The path to the source file 
        private boolean        isFile = false;   // If true, path is a file path to the source

        private BufferedReader file   = null;    // If set, a reader on the current source file
        private String         line   = null;    // The last line read from the current source file
        private int            number = 0;       // The last line number read 


       /**
        *  Initializes the <code>SourceFile</code> to the specified path.
        *  Doesn't do unnecessary work.
        */

        public Source( String descriptor, boolean isFile )
        {
            this.path   = descriptor;
            this.isFile = isFile;
        }


       /**
        *  Returns the descriptor specified on construction.
        */

        public String getDescriptor()
        {
            return this.path;
        }


       /**
        *  Returns a mark-up sample of a source line for error reporting.
        */

        public String getSample( int lineNumber, int column )
        {
            String sample = null;
    
            if( getLine(lineNumber) != null )
            {
                if( column > 0 )
                {
                    String marker = repeatString(" ", column-1) + "^";
    
                    if( column > 40 )
                    {
                        sample = "   " + line.substring( column - 30, column + 10 ) + eol() + "   " + marker.substring( column - 30, column + 10 );
                    }
                    else
                    {
                        sample = "   " + line + eol() + "   " + marker;
                    }
                }
                else
                {
                    sample = line;
                }
            }

            return sample;
        }


       /**
        *  Retrieves the specified line from the specified source file, if possible.
        *  Returns null if the line cannot be retrieved.
        */

        public String getLine( int lineNumber )
        {
            //
            // If the file is already past the requisite line, reopen the file

            if( open() && lineNumber < number )
            {
                reopen();
            }
    

            //
            // Read in the appropriate line, store it, and return it

            if( file != null )
            {
                boolean done = (lineNumber < 0);
                while( number < lineNumber && !done )
                {
                    try
                    {
                        line = file.readLine();
                        number++;
                    }
                    catch( Exception e )
                    {
                        close();
                        done = true;
                    }
                }
            }

            return line;
        }


       /**
        *  Opens the source file for access, if possible.  Won't re-open the file
        *  if already open.  Returns true if the specified file is open and ready 
        *  for use.
        */

        public boolean open( )
        {
            if( file == null )
            {
                try
                {
                    if( isFile )
                    {
                        file = new BufferedReader( new FileReader(path) ); 
                    }
                    else
                    {
                        if( sources.containsKey(path) )
                        {
                            file = new BufferedReader( new StringReader((String)sources.get(path)) );
                        }
                    }
                        
                } 
                catch( Exception e ) 
                { 
                   file = null;
                }
            }

            return file != null;
        }


       /**
        *  Closes the source file, if open.
        */

        public void close()
        {
            if( file != null )
            {
                try{ file.close(); } catch( Exception e ) {}
            }
    
            file   = null;
            line   = null;
            number = 0;
        }


       /**
        *  Reopens the source file.
        */

        protected boolean reopen()
        {
            close();
            return open();
        }
    }

}
