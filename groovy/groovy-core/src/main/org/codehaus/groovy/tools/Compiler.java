package org.codehaus.groovy.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.ClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.syntax.lexer.CharStream;
import org.codehaus.groovy.syntax.lexer.Lexer;
import org.codehaus.groovy.syntax.lexer.LexerTokenStream;
import org.codehaus.groovy.syntax.parser.ASTBuilder;
import org.codehaus.groovy.syntax.parser.CSTNode;
import org.codehaus.groovy.syntax.parser.Parser;
import org.codehaus.groovy.syntax.parser.SemanticVerifier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.DumpClassVisitor;

public class Compiler
{
    private static final File[] EMPTY_FILE_ARRAY = new File[0];
    private static final Exception[] EMPTY_EXCEPTION_ARRAY = new Exception[0];

    private Verifier verifier;
    private CompilerClassLoader classLoader;
    private List errors;
    private boolean verbose = false;

    public Compiler()
    {
        this.verifier    = new Verifier();
        this.classLoader = new CompilerClassLoader();
        this.errors      = new ArrayList();
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

    public GroovyClass[] compile(CharStream source)
        throws Exception
    {
        return compile( new CharStream[] { source } );
    }

    public GroovyClass[] compile(CharStream[] sources)
        throws Exception
    {
        CSTNode[] compilationUnits = new CSTNode[ sources.length ];

        for ( int i = 0 ; i < sources.length ; ++i )
        {
            try
            {
                compilationUnits[ i ] = stageOneCompile( sources[ i ] );
            }
            catch (Exception e)
            {
                this.errors.add( e );
            }
            finally
            {
                try
                {
                    sources[ i ].close();
                }
                catch (IOException e)
                {
                    // swallow?
                }
            }
        }

        if ( ! this.errors.isEmpty() )
        {
            throw new MultiException( (Exception[]) this.errors.toArray( EMPTY_EXCEPTION_ARRAY ) );
        }

        stageTwoCompile( compilationUnits );

        List results = new ArrayList();

        for ( int i = 0 ; i < compilationUnits.length ; ++i )
        {
            GroovyClass[] classes = stageThreeCompile( compilationUnits[ i ],
                                                       sources[ i ] );
                                                       
            for ( int j = 0 ; j < classes.length ; ++j )
            {
                results.add( classes[ j ] );
            }
        }

        return (GroovyClass[]) results.toArray( GroovyClass.EMPTY_ARRAY );
    }
    
    protected CSTNode stageOneCompile(CharStream charStream)
        throws Exception
    {
        try
        {
            if (verbose)
            {
                System.out.println("Parsing: " + charStream.getDescription());
            }
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
        SemanticVerifier verifier = new SemanticVerifier();

        verifier.verify( compilationUnits );
    }

    protected GroovyClass[] stageThreeCompile(CSTNode compilationUnit,
                                              CharStream charStream )
        throws Exception
    {
        ASTBuilder astBuilder = new ASTBuilder( getClassLoader() );

        ClassNode[] classNodes = astBuilder.build( compilationUnit );

        List results = new ArrayList();

        for ( int i = 0 ; i < classNodes.length ; ++i )
        {
            GroovyClass[] classes = generateClasses( new GeneratorContext(),
                                                     classNodes[ i ],
                                                     charStream.getDescription() );

            for ( int j = 0 ; j < classes.length ; ++j )
            {
                results.add( classes[ j ] );
            }
        }

        return (GroovyClass[]) results.toArray( GroovyClass.EMPTY_ARRAY );
    }

    protected GroovyClass[] generateClasses(GeneratorContext context,
                                            ClassNode classNode,
                                            String sourceLocator)
        throws Exception
    {
        List results = new ArrayList();

        ClassGenerator classGenerator = null;
        
        verifier.visitClass(classNode);
        
        if ( false ) 
        {
            DumpClassVisitor dumpVisitor = new DumpClassVisitor(new PrintWriter(new OutputStreamWriter(System.out)));

            classGenerator = new ClassGenerator( context,
                                                 dumpVisitor,
                                                 getClassLoader(),
                                                 sourceLocator );
            classGenerator.visitClass( classNode );
        }
        else 
        {
            ClassWriter classWriter = new ClassWriter( true );

            classGenerator = new ClassGenerator( context, 
                                                 classWriter,
                                                 getClassLoader(),
                                                 sourceLocator );
    
            classGenerator.visitClass( classNode );
    
            byte[] bytes = classWriter.toByteArray();
            
            results.add( new GroovyClass( classNode.getName(),
                                          bytes ) );
        }
    
        
        LinkedList innerClasses = classGenerator.getInnerClasses();

        while ( ! innerClasses.isEmpty() ) 
        {
            GroovyClass[] classes = generateClasses( context,
                                                     (ClassNode)
                                                     innerClasses.removeFirst(),
                                                     sourceLocator );

            for ( int i = 0 ; i < classes.length ; ++i )
            {
                results.add( classes[ i ] );
            }
        }

        return (GroovyClass[]) results.toArray( GroovyClass.EMPTY_ARRAY );
    }
}
