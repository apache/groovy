package org.codehaus.groovy.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.syntax.lexer.FileCharStream;

public class FileSystemCompiler
{
    private Compiler compiler;
    private File outputDir;

    public FileSystemCompiler()
    {
        this.compiler = new Compiler();
    }

    public void setVerbose(boolean verbose)
    {
        compiler.setVerbose(verbose);
    }
    
    protected Compiler getCompiler()
    {
        return this.compiler;
    }

    public void setOutputDir(String outputDir)
    {
        setOutputDir( new File( outputDir ) );
    }

    public void setOutputDir(File outputDir)
    {
        this.outputDir = outputDir;
    }

    public File getOutputDir()
    {
        return this.outputDir;
    }

    public void setClasspath(String classpath)
        throws Exception
    {
        getCompiler().setClasspath( classpath );
    }

    public void compile(String[] paths)
        throws Exception
    {
        File[] files = new File[ paths.length ];

        for ( int i = 0 ; i < paths.length ; ++i )
        {
            files[ i ] = new File( paths[ i ] );
        }

        compile( files );
    }

    public void compile(File[] files)
        throws Exception
    {
        FileCharStream[] fileCharStreams = new FileCharStream[ files.length ];

        for ( int i = 0 ; i < fileCharStreams.length ; ++i )
        {
            fileCharStreams[ i ] = new FileCharStream( files[ i ] );
        }

        GroovyClass[] classes = getCompiler().compile( fileCharStreams );

        for ( int i = 0 ; i < classes.length ; ++i )
        {
            dumpClassFile( classes[ i ] );
        }
    }

    protected void dumpClassFile(GroovyClass groovyClass)
        throws IOException
    {
        byte[] bytes = groovyClass.getBytes();

        File outputFile = createOutputFile( groovyClass.getName() );

        if ( ! outputFile.getParentFile().exists() )
        {
            outputFile.getParentFile().mkdirs();
        }


        FileOutputStream outputStream = new FileOutputStream( outputFile );
        
        try
        {
            outputStream.write( bytes,
                                0,
                                bytes.length );
        }
        finally
        {
            outputStream.close();
        }
    }

    protected File createOutputFile(String className)
    {
        String path = className.replace( '.',
                                         File.separatorChar ) + ".class" ;

        return new File( getOutputDir(),
                         path );
    }

    public static void displayHelp()
    {
        System.err.println( "Usage: groovy <options> <source files>" );
        System.err.println( "where possible options include: " );
        System.err.println( "  --classpath <path>        Specify where to find user class files" );
        System.err.println( "  -d <directory>            Specify where to place generated class files" );
        System.err.println( "  --strict                  Turn on strict type safety" );
        System.err.println( "  --version                 Print the verion" );
        System.err.println( "  --help                    Print a synopsis of standard options" );
        System.err.println( "" );
    }

    public static void displayVersion()
    {
        System.err.println( "groovy compiler version 1.0-alpha-1" );
        System.err.println( "Copyright 2003 The Codehaus. http://groovy.codehaus.org/" );
        System.err.println( "" );
    }

    public static int checkFiles(String[] filenames)
    {
        int errors = 0;

        for ( int i = 0 ; i < filenames.length ; ++i )
        {
            File file = new File( filenames[i] );

            if ( ! file.exists() )
            {
                System.err.println( "error: file not found: " + file );
                ++errors;
            }
            else if ( ! file.canRead() )
            {
                System.err.println( "error: file not readable: " + file );
                ++errors;
            }
        }

        return errors;
    }

    public static void main(String[] args)
        throws Exception
    {
        Options options = new Options();

        options.addOption( OptionBuilder.withLongOpt( "classpath" ).hasArg().withArgName( "classpath" ).create() );
        options.addOption( OptionBuilder.withLongOpt( "sourcepath" ).hasArg().withArgName( "sourcepath" ).create() );
        options.addOption( OptionBuilder.hasArg().create( 'd' ) );
        options.addOption( OptionBuilder.withLongOpt( "strict" ).create( 's' ) );
        options.addOption( OptionBuilder.withLongOpt( "help" ).create( 'h' ) );
        options.addOption( OptionBuilder.withLongOpt( "version" ).create( 'v' ) );

        PosixParser cliParser = new PosixParser();

        CommandLine cli = cliParser.parse( options,
                                           args );

        if ( cli.hasOption( 'h' ) )
        {
            displayHelp();
            return;
        }

        if ( cli.hasOption( 'v' ) )
        {
            displayVersion();
        }

        FileSystemCompiler compiler = new FileSystemCompiler();

        if ( cli.hasOption( "classpath" ) )
        {
            compiler.getCompiler().setClasspath( cli.getOptionValue( "classpath" ) );
        }
        else
        {
            compiler.getCompiler().setClasspath( System.getProperty( "java.class.path" ) );
        }

        if ( cli.hasOption( 'd' ) )
        {
            compiler.setOutputDir( cli.getOptionValue( 'd' ) );
        }
        else
        {
            compiler.setOutputDir( System.getProperty( "user.dir" ) );
        }

        String[] filenames = cli.getArgs();

        if ( filenames.length == 0 )
        {
            displayHelp();
            return;
        }

        int errors = checkFiles( filenames );

        if ( errors == 0 )
        {
            compiler.compile( filenames );
        }
        else
        {
            if ( errors == 1 )
            {
                System.err.println( "1 error" );
            }
            else
            {
                System.err.println( errors + " errors" );
            }
        }
    }
}
