package org.codehaus.groovy.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Category;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.ClassGenerator;
import org.codehaus.groovy.syntax.lexer.InputStreamCharStream;
import org.codehaus.groovy.syntax.lexer.Lexer;
import org.codehaus.groovy.syntax.lexer.LexerTokenStream;
import org.codehaus.groovy.syntax.parser.ASTBuilder;
import org.codehaus.groovy.syntax.parser.CSTNode;
import org.codehaus.groovy.syntax.parser.Parser;
import org.codehaus.groovy.syntax.parser.SemanticVerifier;
import org.objectweb.asm.ClassWriter;

public class Compiler
{
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    private static final Category LOG = Category.getInstance( Compiler.class );

    private CompilerClassLoader classLoader;
    private List sourceDirs;
    private File outputDir;

    public Compiler()
    {
        this.classLoader = new CompilerClassLoader();
        this.sourceDirs  = new ArrayList();
    }

    protected CompilerClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    public void setClasspath(String classpath)
        throws Exception
    {
        StringTokenizer paths = new StringTokenizer( classpath,
                                                     File.pathSeparator );

        while ( paths.hasMoreTokens() )
        {
            getClassLoader().addPath( paths.nextToken() );
        }
    }

    public void setSourcePath(String sourcePath)
    {
        StringTokenizer dirs = new StringTokenizer( sourcePath,
                                                    File.pathSeparator );

        while ( dirs.hasMoreTokens() )
        {
            addSourceDir( dirs.nextToken() );
        }
    }

    public void addSourceDir(String sourceDir)
    {
        addSourceDir( new File( sourceDir ) );
    }

    public void addSourceDir(File sourceDir)
    {
        this.sourceDirs.add( sourceDir );
    }

    public File[] getSourceDirs()
    {
        return (File[]) this.sourceDirs.toArray( EMPTY_FILE_ARRAY );
    }

    public void setOutputDir(String outputDir)
        throws Exception
    {
        setOutputDir( new File( outputDir ) );
    }

    public void setOutputDir(File outputDir)
        throws Exception
    {
        this.outputDir = outputDir;
        getClassLoader().addPath( outputDir.getPath() );
    }

    public File getOutputDir()
    {
        return this.outputDir;
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
        CSTNode[] compilationUnits = new CSTNode[ files.length ];

        for ( int i = 0 ; i < files.length ; ++i )
        {
            compilationUnits[ i ] = stageOneCompile( files[ i ] );
        }

        stageTwoCompile( compilationUnits );

        for ( int i = 0 ; i < compilationUnits.length ; ++i )
        {
            stageThreeCompile( compilationUnits[ i ],
                               files[ i ] );
        }
    }

    protected CSTNode stageOneCompile(File file)
        throws Exception
    {
        LOG.info( "stage-1 compiling: " + file );

        FileInputStream       fileIn     = new FileInputStream( file );
        BufferedInputStream   bufferedIn = new BufferedInputStream( fileIn );
        InputStreamCharStream charStream = new InputStreamCharStream( bufferedIn );

        try
        {
            Lexer lexer = new Lexer( charStream );
            Parser parser = new Parser( new LexerTokenStream( lexer ) );

            return parser.compilationUnit();
        }
        finally
        {
            charStream.close();
        }
    }

    protected void stageTwoCompile(CSTNode[] compilationUnits)
        throws Exception
    {
        LOG.info( "stage-2 compiling" );
        SemanticVerifier verifier = new SemanticVerifier();

        verifier.verify( compilationUnits );
    }

    protected void stageThreeCompile(CSTNode compilationUnit,
                                     File file )
        throws Exception
    {
        LOG.info( "stage-3 compiling: " + file );
        ASTBuilder astBuilder = new ASTBuilder( getClassLoader() );

        ClassNode[] classNodes = astBuilder.build( compilationUnit );

        for ( int i = 0 ; i < classNodes.length ; ++i )
        {
            dumpClass( classNodes[ i ],
                       file );
        }
    }

    protected void dumpClass(ClassNode classNode,
                             File file)
        throws Exception
    {
        ClassWriter    classWriter    = new ClassWriter( true );
        ClassGenerator classGenerator = new ClassGenerator( classWriter,
                                                            getClassLoader(),
                                                            file.getName() );

        classGenerator.visitClass( classNode );

        byte[] code = classWriter.toByteArray();

        File outputFile = createOutputFile( classNode.getName() );

        if ( ! outputFile.getParentFile().exists() )
        {
            outputFile.getParentFile().mkdirs();
        }

        LOG.info( "generating class to: " + outputFile );

        FileOutputStream out = new FileOutputStream( outputFile );

        try
        {
            out.write( code );
        }
        finally
        {
            out.close();
        }
        
        // now lets process inner classes
        LinkedList innerClasses = classGenerator.getInnerClasses();
        while (! innerClasses.isEmpty()) 
        {
            dumpClass((ClassNode) innerClasses.removeFirst(), file);
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
        System.err.println( "  --sourcepath <path>       Specify where to find input source files" );
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

        Compiler compiler = new Compiler();

        if ( cli.hasOption( "classpath" ) )
        {
            compiler.setClasspath( cli.getOptionValue( "classpath" ) );
        }
        else
        {
            compiler.setClasspath( System.getProperty( "java.class.path" ) );
        }

        if ( cli.hasOption( "sourcepath" ) )
        {
            compiler.setSourcePath( cli.getOptionValue( "sourcepath" ) );
        }
        else
        {
            compiler.setSourcePath( System.getProperty( "user.dir" ) );
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
