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
package org.codehaus.groovy.tools;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ConfigurationException;

public class FileSystemCompiler  
{
    private CompilationUnit unit;

    
    public FileSystemCompiler( CompilerConfiguration configuration ) throws ConfigurationException
    {
        this.unit = new CompilationUnit( configuration );
    }

    
    public void compile( String[] paths ) throws Exception 
    {
        unit.addSources( paths );
        unit.compile( );
    }

    
    public void compile( File[] files ) throws Exception 
    {
        unit.addSources( files );
        unit.compile( );
    }


    public static void displayHelp() 
    {
        System.err.println("Usage: groovy <options> <source files>");
        System.err.println("where possible options include: ");
        System.err.println("  --classpath <path>        Specify where to find user class files");
        System.err.println("  -d <directory>            Specify where to place generated class files");
        System.err.println("  --encoding <encoding>     Specify the encoding of the user class files");
        System.err.println("  --strict                  Turn on strict type safety");
        System.err.println("  --version                 Print the verion");
        System.err.println("  --help                    Print a synopsis of standard options");
        System.err.println("  --exception               Print stack trace on error");
        System.err.println("");
    }

    public static void displayVersion() 
    {
        System.err.println("groovy compiler version 1.0-rc1");
        System.err.println("Copyright 2003-2004 The Codehaus. http://groovy.codehaus.org/");
        System.err.println("");
    }

    public static int checkFiles( String[] filenames ) 
    {
        int errors = 0;

        for(int i = 0; i < filenames.length; ++i ) 
        {
            File file = new File( filenames[i] );

            if( !file.exists() ) 
            {
                System.err.println( "error: file not found: " + file );
                ++errors;
            }
            else if( !file.canRead() ) 
            {
                System.err.println( "error: file not readable: " + file );
                ++errors;
            } else {
                String name = file.getName();
                int p = name.lastIndexOf(".");
                if ( p++ >= 0) {
                    if (name.substring(p).equals("java")) {
                        System.err.println( "error: cannot compile file with .java extension: " + file );
                        ++errors;
                    }
                }
            }
        }

        return errors;
    }

    
    
   /**
    *  Primary entry point for compiling from the command line
    *  (using the groovyc script).
    */
    
    public static void main( String[] args )
    {
        boolean displayStackTraceOnError = false;
        
        try
        {
            //
            // Parse the command line
            
            Options options = new Options();
    
            options.addOption(OptionBuilder.withLongOpt("classpath").hasArg().withArgName("classpath").create());
            options.addOption(OptionBuilder.withLongOpt("sourcepath").hasArg().withArgName("sourcepath").create());
            options.addOption(OptionBuilder.withLongOpt("encoding").hasArg().withArgName("encoding").create());
            options.addOption(OptionBuilder.hasArg().create('d'));
            options.addOption(OptionBuilder.withLongOpt("strict").create('s'));
            options.addOption(OptionBuilder.withLongOpt("help").create('h'));
            options.addOption(OptionBuilder.withLongOpt("version").create('v'));
            options.addOption(OptionBuilder.withLongOpt("exception").create('e'));
    
            PosixParser cliParser = new PosixParser();
    
            CommandLine cli = cliParser.parse(options, args);
    
            if( cli.hasOption('h') ) 
            {
                displayHelp();
                return;
            }
    
            if( cli.hasOption('v') ) 
            {
                displayVersion();
            }
    
            
            //
            // Setup the configuration data
            
            CompilerConfiguration configuration = new CompilerConfiguration();
    
            if( cli.hasOption("classpath") ) 
            {
                configuration.setClasspath( cli.getOptionValue("classpath") );
            }
    
            if( cli.hasOption('d') ) 
            {
                configuration.setTargetDirectory( cli.getOptionValue('d') );
            }

            if (cli.hasOption("encoding")) {
                configuration.setSourceEncoding(cli.getOptionValue("encoding"));
            }

            displayStackTraceOnError = cli.hasOption('e');
            
            
            //
            // Load the file name list
            
            String[] filenames = cli.getArgs();
            if( filenames.length == 0 ) 
            {
                displayHelp();
                return;
            }
    
            int errors = checkFiles( filenames );
    
            
            //
            // Create and start the compiler
            
            if( errors == 0 ) 
            {
                FileSystemCompiler compiler = new FileSystemCompiler( configuration );
                compiler.compile( filenames );
            }
        }
        catch( Throwable e ) 
        {
            new ErrorReporter( e, displayStackTraceOnError ).write( System.err );
        }
    }
    
}
