package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Expression;
import org.codehaus.groovy.ast.BinaryExpression;
import org.codehaus.groovy.ast.BooleanExpression;
import org.codehaus.groovy.ast.ConstantExpression;
import org.codehaus.groovy.ast.Statement;
import org.codehaus.groovy.ast.StatementBlock;
import org.codehaus.groovy.ast.ExpressionStatement;
import org.codehaus.groovy.ast.AssertStatement;
import org.codehaus.groovy.ast.ForLoop;
import org.codehaus.groovy.syntax.Token;

import org.objectweb.asm.Constants;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class ASTBuilder
{
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private ClassLoader classLoader;
    private Map imports;

    public ASTBuilder(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
        this.imports     = new HashMap();
    }

    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    public ClassNode[] build(CSTNode unitRoot)
    {
        CSTNode[] children = unitRoot.getChildren();

        String packageName = packageDeclaration( children[ 0 ] );

        importStatements( children[ 1 ] );

        ClassNode[] datatypes = new ClassNode[ children.length - 2 ];

        for ( int i = 2 ; i < children.length ; ++i )
        {
            datatypes[ i - 2 ] = datatypeDeclaration( packageName,
                                                      children[ i ] );
        }

        return datatypes;
    }

    protected String packageDeclaration(CSTNode packageRoot)
    {
        return qualifiedName( packageRoot.getChild( 0 ) );
    }

    protected void importStatements(CSTNode importsRoot)
    {
        CSTNode[] importStatements = importsRoot.getChildren();

        for ( int i = 0 ; i < importStatements.length ; ++i )
        {
            importStatement( importStatements[ i ] );
        }
    }

    protected void importStatement(CSTNode importRoot)
    {
        String importName = qualifiedName( importRoot.getChild( 0 ) );

        String asName = null;

        if ( matches( importRoot.getChild( 1 ),
                      Token.KEYWORD_AS ) )
        {
            asName = identifier( importRoot.getChild( 1 ).getChild( 0 ) );
        }
        else
        {
            int lastDot = importName.lastIndexOf( "." );

            if ( lastDot < 0 )
            {
                asName = importName;
            }
            else
            {
                asName = importName.substring( lastDot + 1 );
            }
        }

        addImport( importName,
                   asName );
    }

    protected void addImport(String importName,
                             String asName)
    {
        this.imports.put( asName,
                          importName );
    }

    protected String resolveName(String name)
    {
        if ( name.indexOf( "." ) >= 0 )
        {
            return name;
        }

        if ( "void".equals( name ) )
        {
            return name;
        }

        if ( this.imports.containsKey( name ) )
        {
            return (String) this.imports.get( name );
        }
        
        try
        {
            getClassLoader().loadClass( "java.lang." + name );

            return "java.lang." + name;
        }
        catch (ClassNotFoundException e)
        {
            try
            {
                getClassLoader().loadClass( "groovy.lang." + name );

                return "groovy.lang." + name;
            }
            catch (ClassNotFoundException ee)
            {
                // swallow
            }
        }

        return null;
    }

    protected String qualifiedName(CSTNode nameRoot)
    {
        String qualifiedName = "";

        if ( matches( nameRoot,
                      Token.DOT ) )
        {
            CSTNode cur = nameRoot;
            
            while ( matches( cur,
                             Token.DOT ) )
            {
                qualifiedName = "." + cur.getChild( 1 ).getToken().getText() + qualifiedName;
                cur = cur.getChild( 0 );
            }
            
            qualifiedName = cur.getToken().getText() + qualifiedName;
        }
        else
        {
            Token token = nameRoot.getToken();

            if ( token == null )
            {
                qualifiedName = "java.lang.Object";
            }
            else
            {
                qualifiedName = token.getText();
            }
        }

        return qualifiedName;
    }

    protected String resolvedQualifiedName(CSTNode nameRoot)
    {
        return resolveName( qualifiedName( nameRoot ) );
    }

    protected String[] qualifiedNames(CSTNode[] nameRoots)
    {
        String[] qualifiedNames = new String[ nameRoots.length ];

        for ( int i = 0 ; i < nameRoots.length ; ++i )
        {
            qualifiedNames[ i ] = qualifiedName( nameRoots[ i ] );
        }

        return qualifiedNames;
    }

    protected String[] resolvedQualifiedNames(CSTNode[] nameRoots)
    {
        String[] qualifiedNames = qualifiedNames( nameRoots );

        for ( int i = 0 ; i < qualifiedNames.length ; ++i )
        {
            qualifiedNames[ i ] = resolveName( qualifiedNames[ i ] );
        }

        return qualifiedNames;
    }

    protected ClassNode datatypeDeclaration(String packageName,
                                            CSTNode datatypeCst)
    {
        if ( matches( datatypeCst,
                      Token.KEYWORD_CLASS ) )
        {
            return classDeclaration( packageName,
                                     datatypeCst );
        }
        else
        {
            return interfaceDeclaration( packageName,
                                         datatypeCst );
        }
    }

    protected ClassNode classDeclaration(String packageName,
                                         CSTNode classRoot)
    {
        int modifiers = modifiers( classRoot.getChild( 0 ) );

        String className = identifier( classRoot.getChild( 1 ) );

        String superClassName = "java.lang.Object";

        if ( matches( classRoot.getChild( 2 ),
                      Token.KEYWORD_EXTENDS ) )
        {
            superClassName = resolvedQualifiedName( classRoot.getChild( 2 ).getChild( 0 ) );
        }
        else
        {
            superClassName = "java.lang.Object";
        }

        String[] interfaceNames = EMPTY_STRING_ARRAY;
        
        if ( matches( classRoot.getChild( 3 ),
                      Token.KEYWORD_IMPLEMENTS ) )
        {
            interfaceNames = resolvedQualifiedNames( classRoot.getChild( 3 ).getChildren() );
        }

        ClassNode classNode = new ClassNode( packageName + "." + className,
                                             modifiers,
                                             superClassName,
                                             interfaceNames );

        CSTNode[] bodyRoots = classRoot.getChild( 4 ).getChildren();

        for ( int i = 0 ; i < bodyRoots.length ; ++i )
        {
            if ( matches( bodyRoots[i],
                          Token.KEYWORD_PROPERTY ) )
            {
                classNode.addProperty( propertyDeclaration( bodyRoots[i] ) );
            }
        }

        for ( int i = 0 ; i < bodyRoots.length ; ++i )
        {
            if ( matches( bodyRoots[i],
                          Token.SYNTH_METHOD ) )
            {
                classNode.addMethod( methodDeclaration( bodyRoots[i] ) );
            }
        }

        return classNode;
    }

    protected PropertyNode propertyDeclaration(CSTNode propertyRoot)
    {
        int modifiers = modifiers( propertyRoot.getChild( 0 ) );

        String identifier = propertyRoot.getChild( 1 ).getToken().getText();

        String typeName = resolvedQualifiedName( propertyRoot.getChild( 2 ) );

        PropertyNode propertyNode = new PropertyNode( identifier,
                                                      modifiers,
                                                      typeName,
                                                      null,
                                                      null,
                                                      null );

        return propertyNode;
    }

    protected MethodNode methodDeclaration(CSTNode methodRoot)
    {
        int modifiers = modifiers( methodRoot.getChild( 0 ) );

        String identifier = methodRoot.getChild( 1 ).getToken().getText();

        String returnType = resolvedQualifiedName( methodRoot.getChild( 2 ) );

        Parameter[] parameters = parameters( methodRoot.getChild( 3 ).getChildren() );

        System.err.println( "paramarmaa: " + Arrays.asList( parameters ) );

        MethodNode methodNode = new MethodNode( identifier,
                                                modifiers,
                                                returnType,
                                                parameters,
                                                statementBlock( methodRoot.getChild( 4 ) ) );

        return methodNode;
    }

    protected Parameter[] parameters(CSTNode[] paramRoots)
    {
        Parameter[] parameters = new Parameter[ paramRoots.length ];

        for ( int i = 0 ; i < paramRoots.length ; ++i )
        {
            String identifier = paramRoots[ i ].getChild( 0 ).getToken().getText();
            String type = resolvedQualifiedName( paramRoots[ i ].getChild( 1 ) );

            parameters[i] = new Parameter( type,
                                           identifier );

            System.err.println( "PARAM: " + parameters[i] );
        }

        return parameters;
    }

    protected ClassNode interfaceDeclaration(String packageName,
                                             CSTNode interfaceRoot)
    {
        return null;
    }

    protected StatementBlock statementBlock(CSTNode blockRoot)
    {
        StatementBlock statementBlock = new StatementBlock();

        CSTNode[] statementRoots = blockRoot.getChildren();

        for ( int i = 0 ; i < statementRoots.length ; ++i )
        {
            statementBlock.addStatement( statement( statementRoots[ i ] ) );
        }

        return statementBlock;
    }

    protected Statement statement(CSTNode statementRoot)
    {
        Statement statement = null;

        switch ( statementRoot.getToken().getType() )
        {
            case ( Token.KEYWORD_ASSERT ):
            {
                statement = assertStatement( statementRoot );
                break;
            }
            case ( Token.KEYWORD_FOR ):
            {
                statement = forStatement( statementRoot );
                break;
            }
            default:
            {
                statement = expressionStatement( statementRoot );
            }
        }

        return statement;
    }

    protected ForLoop forStatement(CSTNode statementRoot)
    {
        String variable = statementRoot.getChild( 0 ).getToken().getText();

        Expression collectionExpr = expression( statementRoot.getChild( 1 ) );

        StatementBlock bodyBlock = statementBlock( statementRoot.getChild( 2 ) );

        return new ForLoop( variable,
                            collectionExpr,
                            bodyBlock );
    }

    protected AssertStatement assertStatement(CSTNode statementRoot)
    {
        BooleanExpression assertExpr = new BooleanExpression( expression( statementRoot.getChild( 0 ) ) );

        CSTNode messageRoot = statementRoot.getChild( 1 );
        Expression messageExpr = null;

        if ( messageRoot.getToken() == null )
        {
            messageExpr = new ConstantExpression( assertExpr.getText() );
        }
        else
        {
            messageExpr = expression( messageRoot );
        }

        return new AssertStatement( assertExpr,
                                    messageExpr );
    }

    protected Statement expressionStatement(CSTNode statementRoot)
    {
        return new ExpressionStatement( expression( statementRoot ) );
    }

    protected Expression expression(CSTNode expressionRoot)
    {
        Expression expression = null;

        switch ( expressionRoot.getToken().getType() )
        {
            case ( Token.COMPARE_EQUAL ):
            case ( Token.COMPARE_NOT_EQUAL ):
            case ( Token.COMPARE_IDENTICAL ):
            case ( Token.COMPARE_LESS_THAN ):
            case ( Token.COMPARE_LESS_THAN_EQUAL ):
            case ( Token.COMPARE_GREATER_THAN ):
            case ( Token.COMPARE_GREATER_THAN_EQUAL ):
            case ( Token.PLUS ):
            case ( Token.MINUS ):
            case ( Token.DIVIDE ):
            case ( Token.MULTIPLY ):
            case ( Token.MOD ):
            case ( Token.EQUAL ):
            {
                expression = binaryExpression( expressionRoot );
                break;
            }
            case ( Token.SINGLE_QUOTE_STRING ):
            case ( Token.DOUBLE_QUOTE_STRING ):
            case ( Token.INTEGER_NUMBER ):
            case ( Token.FLOAT_NUMBER ):
            {
                expression = constantExpression( expressionRoot );
                break;
            }
        }

        return expression;
    }

    protected ConstantExpression constantExpression(CSTNode expressionRoot)
    {
        Object value = null;

        switch ( expressionRoot.getToken().getType() )
        {
            case ( Token.SINGLE_QUOTE_STRING ):
            case ( Token.DOUBLE_QUOTE_STRING ):
            {
                value = expressionRoot.getToken().getText();
                break;
            }
            case ( Token.INTEGER_NUMBER ):
            {
                value = new Long( expressionRoot.getToken().getText() );
                break;
            }
            case ( Token.FLOAT_NUMBER ):
            {
                value = new Double( expressionRoot.getToken().getText() );
                break;
            }
        }

        return new ConstantExpression( value );
    }

    protected BinaryExpression binaryExpression(CSTNode expressionRoot)
    {
        Expression lhsExpression = expression( expressionRoot.getChild( 0 ) );
        Expression rhsExpression = expression( expressionRoot.getChild( 1 ) );

        return new BinaryExpression( lhsExpression,
                                     expressionRoot.getToken(),
                                     rhsExpression );
    }

    protected int modifiers(CSTNode modifiersRoot)
    {
        CSTNode[] modifierNodes = modifiersRoot.getChildren();

        int modifiers = 0;

        for ( int i = 0 ; i < modifierNodes.length ; ++i )
        {
          SWITCH:
            switch ( modifierNodes[i].getToken().getType() )
            {
                case( Token.KEYWORD_PUBLIC ):
                {
                    modifiers |= Constants.ACC_PUBLIC;
                    break SWITCH;
                }
                case( Token.KEYWORD_PROTECTED ):
                {
                    modifiers |= Constants.ACC_PROTECTED;
                    break SWITCH;
                }
                case( Token.KEYWORD_PRIVATE ):
                {
                    modifiers |= Constants.ACC_PRIVATE;
                    break SWITCH;
                }
                case( Token.KEYWORD_STATIC ):
                {
                    modifiers |= Constants.ACC_STATIC;
                    break SWITCH;
                }
                case( Token.KEYWORD_ABSTRACT ):
                {
                    modifiers |= Constants.ACC_ABSTRACT;
                    break SWITCH;
                }
            }
        }

        if ( modifiers == 0 )
        {
            modifiers = Constants.ACC_PUBLIC;
        }

        return modifiers;
    }

    protected String identifier(CSTNode identifierRoot)
    {
        return identifierRoot.getToken().getText();
    }

    boolean matches(CSTNode root,
                    int rootType)
    {
        return ( ( root.getToken() != null )
                 &&
                 ( root.getToken().getType() == rootType ) );
    }

    boolean matches(CSTNode root,
                    int rootType,
                    int c1Type)
    {
        return ( ( matches( root,
                            rootType ) )
                 && 
                 ( ( root.getChild( 1 ) != null )
                   &&
                   ( root.getChild( 1 ).getToken() != null )
                   &&
                   ( root.getChild( 1 ).getToken().getType() == c1Type ) ) );
    }
    
    boolean matches(CSTNode root,
                    int rootType,
                    int c1Type, int c2Type)
    {
        return ( ( matches( root,
                            rootType,
                            c1Type ) )
                 && 
                 ( ( root.getChild( 2 ) != null )
                   &&
                   ( root.getChild( 2 ).getToken() != null )
                   &&
                   ( root.getChild( 2 ).getToken().getType() == c2Type ) ) );
    }
    
    boolean matches(CSTNode root,
                    int rootType,
                    int c1Type, int c2Type, int c3Type)
    {
        return ( ( matches( root,
                            rootType,
                            c1Type ) )
                 && 
                 ( ( root.getChild( 3 ) != null )
                   &&
                   ( root.getChild( 3 ).getToken() != null )
                   &&
                   ( root.getChild( 3 ).getToken().getType() == c3Type ) ) );
    }
}
