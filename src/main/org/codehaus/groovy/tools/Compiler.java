package org.codehaus.groovy.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.ClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.syntax.SyntaxException;
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
    private boolean debug = false;

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

    public void setVerbose(boolean verbose) 
    {
        this.verbose = verbose;
    }
    
    public void setDebug(boolean debug) 
    {
        this.debug = debug;
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
                if ( e instanceof SyntaxException )
                {
                    ((SyntaxException)e).setSourceLocator( sources[ i ].getDescription() );
                }
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

        CompileUnit unit = new CompileUnit();
        
        for ( int i = 0 ; i < compilationUnits.length ; ++i )
        {
            stageThreeCompile( unit, compilationUnits[ i ], sources[ i ] );
        }
         
        stageFourCompile(results, unit);

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

    /**
     * Compiles the AST
     */
    protected void stageThreeCompile(CompileUnit unit, 
                                     CSTNode compilationUnit,
                                     CharStream charStream )
        throws Exception
    {
        ASTBuilder astBuilder = new ASTBuilder( getClassLoader() );
        ModuleNode module = astBuilder.build( compilationUnit );
        module.setDescription(charStream.getDescription());
        unit.addModule(module);
    }
    

    protected void stageFourCompile(List results, 
                                    CompileUnit unit)
        throws Exception
    {
        for ( Iterator iter = unit.getModules().iterator(); iter.hasNext(); )
        {
            ModuleNode module = (ModuleNode) iter.next();
            for ( Iterator iter2 = module.getClasses().iterator(); iter2.hasNext(); )
            {
                ClassNode classNode = (ClassNode) iter2.next();
                if (verbose)
                {
                    System.out.println("Generating class: " + classNode.getName());
                }
                GroovyClass[] classes = generateClasses( new GeneratorContext(unit),
                                                         classNode, 
                                                         module.getDescription() );
    
                for ( int j = 0 ; j < classes.length ; ++j )
                {
                    results.add( classes[ j ] );
                }
            }
        }
    }

    protected GroovyClass[] generateClasses(GeneratorContext context,
                                            ClassNode classNode,
                                            String sourceLocator)
        throws Exception
    {
        List results = new ArrayList();

        ClassGenerator classGenerator = null;
        
        verifier.visitClass(classNode);
        
        if ( debug ) 
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
