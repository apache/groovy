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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ConfigurationException;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Command-line compiler (aka. <tt>groovyc</tt>).
 * 
 * @version $Id$
 */
public class FileSystemCompiler
{
    private final CompilationUnit unit;

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


    public static void displayHelp(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter ( ) ;
        formatter.printHelp ( 80 , "groovyc [options] <source-files>" , "options:", options , "" ) ;
    }

    public static void displayVersion() 
    {
        String version = InvokerHelper.getVersion();
        System.err.println("Groovy compiler version " + version);
        System.err.println("Copyright 2003-2008 The Codehaus. http://groovy.codehaus.org/");
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
        boolean jointCompilation;
        
        try
        {
            //
            // Parse the command line
            
            Options options = new Options();
    
            options.addOption(OptionBuilder.withLongOpt("classpath").hasArg().withArgName("path").withDescription("Specify where to find the class files.").create());
            options.addOption(OptionBuilder.withLongOpt("sourcepath").hasArg().withArgName("path").withDescription("Specify where to find the source files.").create());
            options.addOption(OptionBuilder.withLongOpt("temp").hasArg().withArgName("temp").withDescription("").create());
            options.addOption(OptionBuilder.withLongOpt("encoding").hasArg().withArgName("encoding").withDescription("Specify the encoding of the user class files.").create());
            options.addOption(OptionBuilder.hasArg().withDescription("Specify where to place generated class files.").create('d'));
//            options.addOption(OptionBuilder.withLongOpt("strict").withDescription("Turn on strict type safety.").create('s'));
            options.addOption(OptionBuilder.withLongOpt("help").withDescription("Print a synopsis of standard options.").create('h'));
            options.addOption(OptionBuilder.withLongOpt("version").withDescription("Print the version.").create('v'));
            options.addOption(OptionBuilder.withLongOpt("exception").withDescription("Print stack trace on error.").create('e'));
            options.addOption(OptionBuilder.withLongOpt("jointCompilation").withDescription("Attach javac compiler to compile .java files.").create('j'));
    
            options.addOption(
                    OptionBuilder.withArgName( "property=value" )
                    .withValueSeparator()
                    .hasArgs(2)
                    .withDescription("")
                    .create( "J" ));
            options.addOption(
                    OptionBuilder.withArgName( "flag" )
                    .hasArg()
                    .withDescription("")
                    .create( "F" ));
            
            PosixParser cliParser = new PosixParser();
    
            CommandLine cli = cliParser.parse(options, args);
    
            if( cli.hasOption('h') ) 
            {
                displayHelp(options);
                return;
            }
    
            if( cli.hasOption('v') ) 
            {
                displayVersion();
                return;
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
                displayHelp(options);
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

    public static CompilerConfiguration generateCompilerConfigurationFromOptions(CommandLine cli) {
        //
        // Setup the configuration data

        CompilerConfiguration configuration = new CompilerConfiguration();

        if (cli.hasOption("classpath")) {
            configuration.setClasspath(cli.getOptionValue("classpath"));
        }

        if (cli.hasOption('d')) {
            configuration.setTargetDirectory(cli.getOptionValue('d'));
        }

        if (cli.hasOption("encoding")) {
            configuration.setSourceEncoding(cli.getOptionValue("encoding"));
        }

        // joint compilation parameters
        if (cli.hasOption('j')) {
            Map compilerOptions = new HashMap();

            String[] opts = cli.getOptionValues("J");
            compilerOptions.put("namedValues", opts);

            opts = cli.getOptionValues("F");
            compilerOptions.put("flags", opts);

            configuration.setJointCompilationOptions(compilerOptions);
        }
        return configuration;
    }

    public static Options createCompilationOptions() {
        //
        // Parse the command line

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("classpath").hasArg().withArgName("path").withDescription("Specify where to find the class files.").create());
        options.addOption(OptionBuilder.withLongOpt("sourcepath").hasArg().withArgName("path").withDescription("Specify where to find the source files.").create());
        options.addOption(OptionBuilder.withLongOpt("temp").hasArg().withArgName("temp").withDescription("").create());
        options.addOption(OptionBuilder.withLongOpt("encoding").hasArg().withArgName("encoding").withDescription("Specify the encoding of the user class files.").create());
        options.addOption(OptionBuilder.hasArg().withDescription("Specify where to place generated class files.").create('d'));
//            options.addOption(OptionBuilder.withLongOpt("strict").withDescription("Turn on strict type safety.").create('s'));
        options.addOption(OptionBuilder.withLongOpt("help").withDescription("Print a synopsis of standard options.").create('h'));
        options.addOption(OptionBuilder.withLongOpt("version").withDescription("Print the version.").create('v'));
        options.addOption(OptionBuilder.withLongOpt("exception").withDescription("Print stack trace on error.").create('e'));
        options.addOption(OptionBuilder.withLongOpt("jointCompilation").withDescription("Attach javac compiler to compile .java files.").create('j'));

        options.addOption(
                OptionBuilder.withArgName("property=value")
                        .withValueSeparator()
                        .hasArgs(2)
                        .withDescription("")
                        .create("J"));
        options.addOption(
                OptionBuilder.withArgName("flag")
                        .hasArg()
                        .withDescription("")
                        .create("F"));
        return options;
    }

    public static File createTempDir() throws IOException {
        final int MAXTRIES = 3;
        int accessDeniedCounter = 0;
        File tempFile=null;
        for (int i=0; i<MAXTRIES; i++) {
            try {
                tempFile = File.createTempFile("groovy-generated-", "-java-source");
                tempFile.delete();
                tempFile.mkdirs();
                break;
            } catch (IOException ioe) {
                if (ioe.getMessage().startsWith("Access is denied")) {
                    accessDeniedCounter++;
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
                if (i==MAXTRIES-1) {
                    if (accessDeniedCounter==MAXTRIES) {
                        String msg = 
                            "Access is denied.\nWe tried " +
                            + accessDeniedCounter+
                            " times to create a temporary directory"+
                            " and failed each time. If you are on Windows"+
                            " you are possibly victim to"+
                            " http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6325169. "+
                            " this is no bug in Groovy.";
                        throw new IOException(msg);
                    } else {
                        throw ioe;
                    }
                }
                continue;
            }
        }
        return tempFile;
    }
}
