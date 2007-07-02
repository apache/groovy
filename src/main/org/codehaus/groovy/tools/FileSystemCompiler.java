/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ConfigurationException;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;

public class FileSystemCompiler  
{
    private CompilationUnit unit;

    public FileSystemCompiler( CompilerConfiguration configuration) throws ConfigurationException {
        if (configuration.getJointCompilationOptions()!=null) {
            this.unit = new JavaAwareCompilationUnit(configuration);
        } else {
            this.unit = new CompilationUnit(configuration);
        }
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


    public static void displayHelp() // todo: use HelpFormatter to avoid duplication between help and OptionBuilder
    {
        System.err.println("Usage: groovyc <options> <source files>");
        System.err.println("where possible options include: ");
        System.err.println("  --classpath <path>        Specify where to find user class files");
        System.err.println("  -d <directory>            Specify where to place generated class files");
        System.err.println("  --encoding <encoding>     Specify the encoding of the user class files");
//        System.err.println("  --strict                  Turn on strict type safety");
        System.err.println("  --version                 Print the verion");
        System.err.println("  --help                    Print a synopsis of standard options");
        System.err.println("  --exception               Print stack trace on error");
        System.err.println("  --jointCompilation        attach javac compiler to compile .java files");
        System.err.println("");
    }

    public static void displayVersion() 
    {
        System.err.println("Groovy compiler version 1.1-beta-2-SNAPSHOT");
        System.err.println("Copyright 2003-2007 The Codehaus. http://groovy.codehaus.org/");
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
        boolean jointCompilation = false;
        
        try
        {
            //
            // Parse the command line
            
            Options options = new Options();
    
            options.addOption(OptionBuilder.withLongOpt("classpath").hasArg().withArgName("classpath").create());
            options.addOption(OptionBuilder.withLongOpt("sourcepath").hasArg().withArgName("sourcepath").create());
            options.addOption(OptionBuilder.withLongOpt("temp").hasArg().withArgName("temp").create());
            options.addOption(OptionBuilder.withLongOpt("encoding").hasArg().withArgName("encoding").create());
            options.addOption(OptionBuilder.hasArg().create('d'));
//            options.addOption(OptionBuilder.withLongOpt("strict").create('s'));
            options.addOption(OptionBuilder.withLongOpt("help").create('h'));
            options.addOption(OptionBuilder.withLongOpt("version").create('v'));
            options.addOption(OptionBuilder.withLongOpt("exception").create('e'));
            options.addOption(OptionBuilder.withLongOpt("jointCompilation").create('j'));
    
            options.addOption(
                    OptionBuilder.withArgName( "property=value" )
                    .withValueSeparator()
                    .hasArgs(2)
                    .create( "J" ));
            options.addOption(
                    OptionBuilder.withArgName( "property=value" )
                    .hasArg()
                    .create( "F" ));
            
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
            
            // joint compilation parameters
            jointCompilation = cli.hasOption('j');
            if (jointCompilation) {
                Map compilerOptions =  new HashMap();
                
                String[] opts = cli.getOptionValues("J");
                compilerOptions.put("namedValues", opts);
                
                opts = cli.getOptionValues("F");
                compilerOptions.put("flags", opts);
                
                configuration.setJointCompilationOptions(compilerOptions);
            }            
            
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
                if (jointCompilation) {
                    File tmpDir = createTempDir();
                    configuration.getJointCompilationOptions().put("stubDir",tmpDir);
                }
                FileSystemCompiler compiler = new FileSystemCompiler(configuration);
                compiler.compile( filenames );
            }
        }
        catch( Throwable e ) 
        {
            new ErrorReporter( e, displayStackTraceOnError ).write( System.err );
        }
    }
    


    private static File createTempDir() throws IOException {
        File tempFile = File.createTempFile("generated-", "java-source");
        tempFile.delete();
        tempFile.mkdirs();
        return tempFile;
    }
    
    
}
