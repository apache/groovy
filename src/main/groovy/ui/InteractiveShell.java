/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

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
package groovy.ui;

import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.TokenStream;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.groovy.syntax.parser.Parser;
import org.codehaus.groovy.tools.ErrorReporter;
import org.codehaus.groovy.tools.ExceptionCollector;


/**
 *  A simple interactive shell for evaluating groovy expressions
 *  on the command line
 * 
 *  @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 *  @author <a href="mailto:cpoirier@dreaming.org"   >Chris Poirier</a>
 *  @version $Revision$
 */
public class InteractiveShell {

    GroovyShell shell = new GroovyShell();
    BufferedReader reader;


   /**
    *  Entry point when called directly.
    */

    public static void main(String args[]) {
        try {
            InteractiveShell groovy = new InteractiveShell();
            groovy.run(args);
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }


   /**
    *  Default constructor.
    */

    public InteractiveShell() {
    }



  //---------------------------------------------------------------------------
  // COMMAND LINE PROCESSING LOOP


   /**
    *  Reads commands and statements from input stream and processes them.
    */

    public void run(String[] args) throws Exception {
        reader = new BufferedReader(new InputStreamReader(System.in));

        String version = InvokerHelper.getVersion();
        
        System.out.println("Lets get Groovy!");
        System.out.println("================");
        System.out.println("Version: " + version + " JVM: " + System.getProperty("java.vm.version"));
        System.out.println("Type 'exit' to terminate the shell");
        System.out.println("Type 'help' for command help");
        
        int counter = 1;
        while( true ) {
            
            //
            // Read a single top-level statement from the command line, 
            // trapping errors as they happen.  We quit on null.

            String command = read();
            if( command == null ) {
                break;
            }

            reset();

            if( command.length() > 0 ) {
              
                //
                // We have a command that parses, so evaluate it.

                try {
                    Object answer = shell.evaluate(command, "CommandLine" + counter++ +".groovy");
                    // System.out.println(InvokerHelper.inspect(answer));
                }
                catch( Exception e ) {
                    new ErrorReporter( e, false ).write( System.err );
                }
                catch( Throwable e ) {
                    new ErrorReporter( e, false ).write( System.err );
                    System.err.println( ">>> exiting" );
                    System.exit(1);
                }
            }
        }
    }




  //---------------------------------------------------------------------------
  // COMMAND LINE PROCESSING MACHINERY


    private StringBuffer accepted = new StringBuffer();  // The statement text accepted to date
    private String       pending  = null;                // A line of statement text not yet accepted
    private int          line     = 1;                   // The current line number

    private boolean      stale    = false;               // Set to force clear of accepted

    private Parser       parser   = null;                // A Parser used to check the statement
    private TokenStream  stream   = null;                // The TokenStream that backs the Parser
    private Exception    error    = null;                // Any actual syntax error caught during parsing
    private CSTNode      tree     = null;                // The top-level statement when parsed

        

   /** 
    *  Resets the command-line processing machinery after use.
    */

    protected void reset() {
        stale   = true;
        pending = null;
        line    = 1;

        parser  = null;
        stream  = null;
        error   = null;
        tree    = null;
    }



   /**
    *  Reads a single statement from the command line.  Also identifies
    *  and processes command shell commands.  Returns the command text
    *  on success, or null when command processing is complete.
    *  <p>
    *  NOTE: Changed, for now, to read until 'execute' is issued.  At
    *  'execute', the statement must be complete.
    */

    protected String read() {

        reset();
        System.out.println( "" );

        boolean complete = false;
        boolean done     = false;

        while( /* !complete && */ !done ) {

            //
            // Prompt

            System.out.print( line + "> ");


            //
            // Read a line.  If IOException or null, or command "exit", terminate
            // processing.  

            try { pending = reader.readLine(); } catch( IOException e ) { }

            if( pending == null || (COMMAND_MAPPINGS.containsKey(pending) && ((Integer)COMMAND_MAPPINGS.get(pending)).intValue() == COMMAND_ID_EXIT) ) {
                return null;                                  // <<<< FLOW CONTROL <<<<<<<<
            }


            //
            // First up, try to process the line as a command and proceed accordingly.

            if( COMMAND_MAPPINGS.containsKey(pending) ) {

                int code = ((Integer)COMMAND_MAPPINGS.get(pending)).intValue();
                switch( code ) {

                  case COMMAND_ID_HELP:
                    displayHelp();
                    break;

                  case COMMAND_ID_DISCARD:
                    reset();
                    done = true;
                    break;

                  case COMMAND_ID_DISPLAY:
                    displayStatement();
                    break;

                  case COMMAND_ID_EXPLAIN:
                    explainStatement();
                    break;

                  case COMMAND_ID_EXECUTE:
                    if( complete ) {
                        done = true;
                    }
                    else {
                        System.err.println( "statement not complete" );
                    }
                    break;
                }

                continue;                                     // <<<< LOOP CONTROL <<<<<<<<

            }


            //
            // Otherwise, it's part of a statement.  If it's just whitespace,
            // we'll just accept it and move on.  Otherwise, parsing is attempted
            // on the cumulated statement text, and errors are reported.  The
            // pending input is accepted or rejected based on that parsing.

            freshen();

            if( pending.trim() == "" ) {
                accept();
                continue;                                     // <<<< LOOP CONTROL <<<<<<<<
            }

            String code = current();

            if( parse(code, 1) ) {
                accept();
                complete = true;
            }
            else if( error == null ) {
                accept();
            }
            else {
                report( code );
            }

        }


        //
        // Get and return the statement.

        return accepted( complete );
    }


       
   /**
    *  Returns the accepted statement as a string.  If not <code>complete</code>,
    *  returns the empty string.
    */

    private String accepted( boolean complete ) {
        if( complete ) {
            return accepted.toString();
        }
        return ""; 
    }



   /**
    *  Returns the current statement, including pending text.
    */

    private String current( ) {
        return accepted.toString() + pending + "\n";
    }



   /**
    *  Accepts the pending text into the statement.
    */

    private void accept() {
        accepted.append( pending ).append( "\n" );
        line += 1;
    }


   /**
    *  Clears accepted if stale.
    */

    private void freshen() {
        if( stale ) {
            accepted.setLength(0);
            stale = false;
        }
    }





  //---------------------------------------------------------------------------
  // SUPPORT ROUTINES


   /**
    *  Attempts to parse the specified code with the specified tolerance.
    *  Updates the <code>parser</code> and <code>error</code> members 
    *  appropriately.  Returns true if the text parsed, false otherwise.
    *  The attempts to identify and suppress errors resulting from the
    *  unfinished source text.
    */

    private boolean parse( String code, int tolerance ) {

        boolean parsed = false;

        parser = null;
        stream = null;
        error  = null;
        tree   = null;

        //
        // Create the parser and attempt to parse the text as a top-level statement.

        try {
            parser = Parser.create( code, tolerance );
            stream = parser.getTokenStream();
            tree   = parser.module();

            /* see note on read(): tree   = parser.topLevelStatement(); */

            if( stream.atEnd() ) {
                parsed = true;
            }
        }

        //
        // If we catch an exception from the parser or lexer, and the token stream
        // is empty, it means we aren't yet complete.  Otherwise, the exception
        // is real, and we report it. 

        catch( ExceptionCollector e ) {
            if( !stream.atEnd() ) {
                if( tolerance == 1 ) {
                    error = (Exception)e.get(0);
                }
                else {
                    error = e;
                }
            }
        }

        catch( SyntaxException e ) {
            if( !stream.atEnd() ) {
                error = e;
            }
        }

        catch( Exception e ) {
            error = e;
        }

        return parsed;
    }



   /** 
    *  Reports the last parsing error to the user.
    */

    private void report( String code ) {

        if( error instanceof SyntaxException ) {
            ((SyntaxException)error).setSourceLocator("line");
        }

        System.err.print( "discarding " );
        ErrorReporter reporter = new ErrorReporter( error, false );
        reporter.setSources( Collections.singletonMap("line", code) );
        reporter.write( System.err );
    }





  //-----------------------------------------------------------------------
  // COMMANDS

    private static final int COMMAND_ID_EXIT    = 0;
    private static final int COMMAND_ID_HELP    = 1;
    private static final int COMMAND_ID_DISCARD = 2;
    private static final int COMMAND_ID_DISPLAY = 3;
    private static final int COMMAND_ID_EXPLAIN = 4;
    private static final int COMMAND_ID_EXECUTE = 5;
    private static final int LAST_COMMAND_ID    = 5;

    private static final String[] COMMANDS = { "exit", "help", "discard", "display", "explain", "execute" };

    private static final Map COMMAND_MAPPINGS = new HashMap();

    static {
        for( int i = 0; i <= LAST_COMMAND_ID; i++ ) {
            COMMAND_MAPPINGS.put( COMMANDS[i], new Integer(i) );
        }

        // A few synonyms

        COMMAND_MAPPINGS.put( "quit", new Integer(COMMAND_ID_EXIT) );
        COMMAND_MAPPINGS.put( "go"  , new Integer(COMMAND_ID_EXECUTE) );
    }

    private static final Map COMMAND_HELP = new HashMap();

    static {
        COMMAND_HELP.put( COMMANDS[COMMAND_ID_EXIT   ], "exit/quit  - terminates processing"          );
        COMMAND_HELP.put( COMMANDS[COMMAND_ID_HELP   ], "help       - displays this help text"        );
        COMMAND_HELP.put( COMMANDS[COMMAND_ID_DISCARD], "discard    - discards the current statement" );
        COMMAND_HELP.put( COMMANDS[COMMAND_ID_DISPLAY], "display    - displays the current statement" );
        COMMAND_HELP.put( COMMANDS[COMMAND_ID_EXPLAIN], "explain    - explains the parsing of the current statement" );
        COMMAND_HELP.put( COMMANDS[COMMAND_ID_EXECUTE], "execute/go - temporary command to cause statement execution" );
    }



   /**
    *  Displays help text about available commands.
    */

    private void displayHelp() {
        System.out.println( "Available commands (must be entered without extraneous characters):" );
        for( int i = 0; i <= LAST_COMMAND_ID; i++ ) {
            System.out.println( (String)COMMAND_HELP.get(COMMANDS[i]) );
        }
    }



   /**
    *  Displays the accepted statement.
    */

    private void displayStatement() {
        String[] lines = accepted.toString().split("\n");
        for( int i = 0; i < lines.length; i++ ) {
            System.out.println( (i+1) + "> " + lines[i] );
        }
    }



   /**
    *  Attempts to parse the accepted statement and display the 
    *  parse tree for it.
    */

    private void explainStatement() {

        if( parse(accepted(true), 10) || error == null ) {
            System.out.println( "parse tree:" );
            System.out.println( tree );
        }
        else {
            System.out.println( "statement does not parse" );
        }
    }
}

