package org.codehaus.groovy.syntax.parser;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;

import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Constants;

public class ASTBuilder
{
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String[] DEFAULT_IMPORTS = {
        "java.lang.",
        "groovy.lang.",
        "groovy.util."
    };

    private ClassLoader classLoader;
    private Map imports;

    private String packageName;

    public ASTBuilder(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
        this.imports     = new HashMap();
    }

    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    public ModuleNode build(CSTNode unitRoot) throws ParserException
    {
        ModuleNode answer = new ModuleNode();
        
        CSTNode[] children = unitRoot.getChildren();

        packageName = packageDeclaration( children[ 0 ] );

        answer.setPackageName(packageName);
        
        importStatements( answer, children[ 1 ] );

        ClassNode[] datatypes = new ClassNode[ children.length - 2 ];

        for ( int i = 2 ; i < children.length ; ++i )
        {
            datatypeDeclaration( answer, packageName, children[ i ] );
        }

        return answer;
    }

    protected String packageDeclaration(CSTNode packageRoot)
    {
        CSTNode nameRoot = packageRoot.getChild( 0 );

        if ( nameRoot.getToken() == null )
        {
            return null;
        }
             
        return qualifiedName( nameRoot );
    }

    protected void importStatements(ModuleNode node, CSTNode importsRoot)
    {
        CSTNode[] importStatements = importsRoot.getChildren();

        for ( int i = 0 ; i < importStatements.length ; ++i )
        {
            importStatement( node, importStatements[ i ] );
        }
    }

    protected void importStatement(ModuleNode node,CSTNode importRoot)
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

        node.addImport(asName, importName);
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
        else if ( name.equals( "void" )
                  ||
                  name.equals( "int" )
                  ||
                  name.equals( "float" ) )
        {
            return name;
        }

        if ( this.imports.containsKey( name ) )
        {
            return (String) this.imports.get( name );
        }

        if (packageName != null && packageName.length() > 0)
        {
            try
            {
                getClassLoader().loadClass( packageName + "." + name );

                return packageName + "." + name;
            }
            catch (Exception e)
            {
                // swallow
            }
            catch (Error e)
            {
                // swallow
            }
        }        
        for (int i = 0; i < DEFAULT_IMPORTS.length; i++)
        {
            try
            {
                String fullName = DEFAULT_IMPORTS[i] + name;
                getClassLoader().loadClass( fullName );
                return fullName;
            }
            catch (Exception e)
            {
                // swallow
            }
            catch (Error e)
            {
                // swallow
            }
        }
        return null;
    }

    protected boolean isDatatype(String name)
    {
        return resolveName(name) != null;
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
        else if ( matches( nameRoot,
                           Token.KEYWORD_VOID ) )
        {
            return "void";
        }
        else if ( matches( nameRoot,
                           Token.KEYWORD_INT ) )
        {
            return "int";
        }
        else if ( matches( nameRoot,
                           Token.KEYWORD_FLOAT ) )
        {
            return "float";
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

    protected String resolvedQualifiedNameNotNull(CSTNode child) throws ParserException 
    {
        String answer = resolvedQualifiedName(child);
        if (answer == null) 
        {
            throw new ParserException("Unknown type: " + child.getToken().getText() + " could not be resolved", child.getToken());
        }
        return answer;
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

    protected String[] resolvedQualifiedNamesNotNull(CSTNode[] nameRoots) throws ParserException
    {
        String[] qualifiedNames = qualifiedNames( nameRoots );

        for ( int i = 0 ; i < qualifiedNames.length ; ++i )
        {
            qualifiedNames[ i ] = resolveName( qualifiedNames[ i ] );
            if (qualifiedNames[ i ] == null) 
            {
                Token token = nameRoots[i].getToken();
                throw new ParserException("Unknown type: " + token.getText() + " could not be resolved", token);
            }
        }

        return qualifiedNames;
    }

    protected void datatypeDeclaration(ModuleNode module, String packageName,
                                            CSTNode datatypeCst) throws ParserException
    {
        if ( matches( datatypeCst,
                      Token.KEYWORD_CLASS ) )
        {
            module.addClass( classDeclaration( packageName, datatypeCst ) );
        }
        else if ( matches( datatypeCst,
                      Token.KEYWORD_INTERFACE ) )
        {
            module.addClass( interfaceDeclaration( packageName, datatypeCst ) );
        }
        else
        {
            module.addStatement( statement(datatypeCst) );
        }
    }

    protected ClassNode classDeclaration(String packageName,
                                         CSTNode classRoot) throws ParserException
    {
        int modifiers = modifiers( classRoot.getChild( 0 ) );

        String className = identifier( classRoot.getChild( 1 ) );

        String superClassName = "java.lang.Object";

        if ( matches( classRoot.getChild( 2 ),
                      Token.KEYWORD_EXTENDS ) )
        {
            superClassName = resolvedQualifiedNameNotNull( classRoot.getChild( 2 ).getChild( 0 ) );
        }
        else
        {
            superClassName = "java.lang.Object";
        }

        String[] interfaceNames = EMPTY_STRING_ARRAY;
        
        if ( matches( classRoot.getChild( 3 ),
                      Token.KEYWORD_IMPLEMENTS ) )
        {
            interfaceNames = resolvedQualifiedNamesNotNull( classRoot.getChild( 3 ).getChildren() );
        }

        /** @todo parser 
         * do we think the parser can iterate through the 
         * interface names and find any MixinNodes avaiilable
         * and pass those into the AST?
         * 
         * Maybe loading the class for the interface name and then
         * we could check if it extends the groovy.lang.Mixin
         * interface
         */
        
        MixinNode[] mixinNames = MixinNode.EMPTY_ARRAY;
        
        String fqClassName = null;

        if ( packageName == null
             ||
             packageName.trim().equals( "" ) )
        {
            fqClassName = className;
        }
        else
        {
            fqClassName = packageName.trim() + "." + className;
        }

        ClassNode classNode = new ClassNode( fqClassName,
                                             modifiers,
                                             superClassName,
                                             interfaceNames,
                                             mixinNames );

        CSTNode[] bodyRoots = classRoot.getChild( 4 ).getChildren();

        for ( int i = 0 ; i < bodyRoots.length ; ++i )
        {
            if ( matches( bodyRoots[i],
                          Token.KEYWORD_PROPERTY ) )
            {
                addPropertyDeclaration( classNode, bodyRoots[i] );
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

    protected PropertyNode addPropertyDeclaration(ClassNode classNode, CSTNode propertyRoot)
    {
        int modifiers = modifiers( propertyRoot.getChild( 0 ) );

        String identifier = propertyRoot.getChild( 1 ).getToken().getText();

        String typeName = resolvedQualifiedName( propertyRoot.getChild( 2 ) );

        PropertyNode propertyNode = classNode.addProperty( identifier,
                                                      modifiers,
                                                      typeName,
                                                      null,
                                                      null,
                                                      null );
        setLineNumber(propertyNode, propertyRoot);
        return propertyNode;
    }

    protected MethodNode methodDeclaration(CSTNode methodRoot) throws ParserException
    {
        int modifiers = modifiers( methodRoot.getChild( 0 ) );

        String identifier = methodRoot.getChild( 1 ).getToken().getText();

        String returnType = resolvedQualifiedName( methodRoot.getChild( 2 ) );

        Parameter[] parameters = parameters( methodRoot.getChild( 3 ).getChildren() );

        MethodNode methodNode = new MethodNode( identifier,
                                                modifiers,
                                                returnType,
                                                parameters,
                                                statementBlock( methodRoot.getChild( 4 ) ) );

        setLineNumber(methodNode, methodRoot);
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
        }

        return parameters;
    }

    protected ClassNode interfaceDeclaration(String packageName,
                                             CSTNode interfaceRoot)
    {
        return null;
    }

    protected BlockStatement statementBlock(CSTNode blockRoot) throws ParserException
    {
        BlockStatement statementBlock = new BlockStatement();

        CSTNode[] statementRoots = blockRoot.getChildren();

        for ( int i = 0 ; i < statementRoots.length ; ++i )
        {
            CSTNode statementRoot = statementRoots[ i ];
            Statement statement = statement( statementRoot );
            statementBlock.addStatement( statement );
            setLineNumber( statement,
                           statementRoot);
        }

        return statementBlock;
    }

    protected Statement statement(CSTNode statementRoot) throws ParserException
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
            case ( Token.KEYWORD_WHILE ):
            {
                statement = whileStatement( statementRoot );
                break;
            }
            case ( Token.KEYWORD_TRY ):
            {
                statement = tryStatement( statementRoot );
                break;
            }
            case ( Token.KEYWORD_RETURN ):
            {
                statement = returnStatement( statementRoot );
                break;
            }
            case ( Token.KEYWORD_IF ):
            {
                statement = ifStatement( statementRoot );
                break;
            }
            default:
            {
                statement = expressionStatement( statementRoot );
            }
        }
        setLineNumber(statement, statementRoot);

        return statement;
    }

    protected WhileStatement whileStatement(CSTNode statementRoot) throws ParserException
    {
        Expression expr = expression( statementRoot.getChild( 0 ) );
        BlockStatement statementBlock = statementBlock( statementRoot.getChild( 1 ) );

        return new WhileStatement( new BooleanExpression( expr ),
                                   statementBlock );
    }

    protected IfStatement ifStatement(CSTNode statementRoot) throws ParserException
    {
        CSTNode[]         children   = statementRoot.getChildren();

        BooleanExpression expression = new BooleanExpression( expression( children[ 0 ] ) );
        BlockStatement    ifBlock    = statementBlock( children[ 1 ] );

        Statement elseBlock = null;

        if ( children.length == 3
             &&
             matches( children[ 2 ],
                      Token.KEYWORD_IF ) )
        {
            elseBlock = ifStatement( children[ 2 ] );
        }
        else if ( children.length == 3 )
        {
            elseBlock = statementBlock( children[ 2 ].getChild( 0 ) );
        }
        else
        {
            elseBlock = EmptyStatement.INSTANCE;
        }

        return new IfStatement( expression,
                           ifBlock,
                           elseBlock );
    }

    protected TryCatchStatement tryStatement(CSTNode statementRoot) throws ParserException
    {
        TryCatchStatement tcf = new TryCatchStatement( statementBlock( statementRoot.getChild( 0 ) ),
                                                   statementBlock( statementRoot.getChild( 1 ) ) );

        CSTNode[] catchRoots = statementRoot.getChild( 2 ).getChildren();

        for ( int i = 0 ; i < catchRoots.length ; ++i )
        {
            String exceptionType = resolvedQualifiedNameNotNull( catchRoots[ i ].getChild( 0 ) );
            String identifier = identifier( catchRoots[ i ].getChild( 1 ) );
            Statement block = statementBlock( catchRoots[ i ].getChild( 2 ) );
            
            tcf.addCatch( new CatchStatement( exceptionType,
                                              identifier,
                                              block ) );
        }
        
        return tcf;
    }

    protected ReturnStatement returnStatement(CSTNode statementRoot) throws ParserException
    {
        return new ReturnStatement( expression( statementRoot.getChild( 0 ) ) );
    }

    protected ForStatement forStatement(CSTNode statementRoot) throws ParserException
    {
        String variable = statementRoot.getChild( 0 ).getToken().getText();

        Expression collectionExpr = expression( statementRoot.getChild( 1 ) );

        BlockStatement bodyBlock = statementBlock( statementRoot.getChild( 2 ) );

        return new ForStatement( variable,
                            collectionExpr,
                            bodyBlock );
    }

    protected AssertStatement assertStatement(CSTNode statementRoot) throws ParserException
    {
        BooleanExpression assertExpr = new BooleanExpression( expression( statementRoot.getChild( 0 ) ) );

        CSTNode messageRoot = statementRoot.getChild( 1 );
        Expression messageExpr = null;

        if ( messageRoot.getToken() == null )
        {
            // lets pass in null since we know the expression string
            messageExpr = ConstantExpression.NULL;
        }
        else
        {
            messageExpr = expression( messageRoot );
        }

        return new AssertStatement( assertExpr,
                                    messageExpr );
    }

    protected Statement expressionStatement(CSTNode statementRoot) throws ParserException
    {
        return new ExpressionStatement( expression( statementRoot ) );
    }

    protected Expression expression(CSTNode expressionRoot) throws ParserException
    {
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
            case ( Token.KEYWORD_INSTANCEOF ):
            {
                return binaryExpression( expressionRoot );
            }
            case ( Token.DOT_DOT ):
            {
                return rangeExpression( expressionRoot );
            }
            case ( Token.SINGLE_QUOTE_STRING ):
            case ( Token.DOUBLE_QUOTE_STRING ):
            case ( Token.INTEGER_NUMBER ):
            case ( Token.FLOAT_NUMBER ):
            case ( Token.KEYWORD_NULL ):
            case ( Token.KEYWORD_TRUE ):
            case ( Token.KEYWORD_FALSE ):
            {
                return constantExpression( expressionRoot );
            }
            case ( Token.IDENTIFIER ):
            {
                return  variableOrClassExpression( expressionRoot );
            }
            case ( Token.KEYWORD_THIS ):
            case ( Token.KEYWORD_SUPER ):
            {
                return variableExpression( expressionRoot );
            }
            case ( Token.DOT ):
            {
                return propertyExpression( expressionRoot );
            }
            case ( Token.SYNTH_LIST ):
            {
                return listExpression( expressionRoot );
            }
            case ( Token.SYNTH_MAP ):
            {
                return mapExpression( expressionRoot );
            }
            case ( Token.LEFT_PARENTHESIS ):
            {
                return methodCallExpression( expressionRoot );
            }
            case ( Token.LEFT_CURLY_BRACE ):
            {
                return closureExpression( expressionRoot );
            }
            case ( Token.KEYWORD_NEW ):
            {
                return newExpression( expressionRoot );
            }
        }

        throw new RuntimeException( expressionRoot.getToken().getStartLine() + ": cannot create expression for node: " + expressionRoot );
    }

    protected ConstructorCallExpression newExpression(CSTNode expressionRoot) throws ParserException
    {
        //String datatype = resolvedQualifiedNameNotNull( expressionRoot.getChild( 0 ) );
        String datatype = resolvedQualifiedName( expressionRoot.getChild( 0 ) );

        TupleExpression args = tupleExpression( expressionRoot.getChild( 1 ) );

        return new ConstructorCallExpression( datatype,
                                              args );
    }

    protected ClosureExpression closureExpression(CSTNode expressionRoot) throws ParserException
    {
        Parameter[] parameters = parameters( expressionRoot.getChild( 0 ).getChildren() );

        return new ClosureExpression( parameters,
                                      statementBlock( expressionRoot.getChild( 1 ) ) );
    }

    protected MethodCallExpression methodCallExpression(CSTNode expressionRoot) throws ParserException
    {
        CSTNode objectExpressionRoot = expressionRoot.getChild( 0 );

        Expression objectExpression = null;

        if ( objectExpressionRoot.getToken() == null )
        {
            objectExpression = new VariableExpression( "this" );
        }
        else
        {
            objectExpression = expression( expressionRoot.getChild( 0 ) );
        }

        String methodName = expressionRoot.getChild( 1 ).getToken().getText();

        Expression paramList = actualParameterList( expressionRoot.getChild( 2 ) );

        return new MethodCallExpression( objectExpression,
                                         methodName,
                                         paramList );
    }

    protected Expression actualParameterList(CSTNode paramRoot) throws ParserException
    {
        Expression paramList = null;

        CSTNode[] paramRoots = paramRoot.getChildren();

        if ( paramRoots.length > 0
             &&
             matches( paramRoots[ 0 ],
                      Token.COLON ) )
        {
            paramList = namedActualParameterList( paramRoot );
        }
        else
        {
            paramList = nonNamedActualParameterList( paramRoot );
        }

        return paramList;
    }

    protected Expression namedActualParameterList(CSTNode paramRoot) throws ParserException
    {
        //NamedArgumentListExpression paramList = new NamedArgumentListExpression();
        //ListExpression paramList = new ListExpression();
        TupleExpression paramList = new TupleExpression();

        CSTNode[] paramRoots = paramRoot.getChildren();

        MapExpression map = new MapExpression();

        paramList.addExpression( map );

        for ( int i = 0 ; i < paramRoots.length ; ++i )
        {
            if ( matches( paramRoots[ i ],
                          Token.COLON ) )
            {
                CSTNode keyRoot = paramRoots[ i ].getChild( 0 );
                CSTNode valueRoot = paramRoots[ i ].getChild( 1 );
                
                //System.err.println( "param: " + paramRoots[i] );
                
                Expression keyExpr   = new ConstantExpression( keyRoot.getToken().getText() );
                Expression valueExpr = expression( valueRoot );
                
                //paramList.addMapEntryExpression( keyExpr,
                //valueExpr );
                
                map.addMapEntryExpression( keyExpr,
                                           valueExpr );
            }
            else
            {
                if ( matches( paramRoots[ i ],
                              Token.LEFT_CURLY_BRACE ) )
                {
                    paramList.addExpression( closureExpression( paramRoots[ i ] ) );
                }
                              
                break;
            }
        }

        // System.err.println( "paramList: " + paramList );

        return paramList;
    }

    protected Expression nonNamedActualParameterList(CSTNode paramRoot) throws ParserException
    {
        return tupleExpression( paramRoot );
    }

    protected TupleExpression tupleExpression(CSTNode expressionRoot) throws ParserException
    {
        TupleExpression tupleExpression = new TupleExpression();

        CSTNode[] exprRoots = expressionRoot.getChildren();

        for ( int i = 0 ; i < exprRoots.length ; ++i )
        {
            tupleExpression.addExpression( expression( exprRoots[ i ] ) );
        }

        return tupleExpression;
    }

    protected ListExpression listExpression(CSTNode expressionRoot) throws ParserException
    {
        ListExpression listExpression = new ListExpression();

        CSTNode[] exprRoots = expressionRoot.getChildren();

        for ( int i = 0 ; i < exprRoots.length ; ++i )
        {
            listExpression.addExpression( expression( exprRoots[ i ] ) );
        }

        return listExpression;
    }

    protected MapExpression mapExpression(CSTNode expressionRoot) throws ParserException
    {
        MapExpression mapExpression = new MapExpression();

        CSTNode[] entryRoots = expressionRoot.getChildren();

        for ( int i = 0 ; i < entryRoots.length ; ++i )
        {
            Expression keyExpression   = expression( entryRoots[ i ].getChild( 0 ) );
            Expression valueExpression = expression( entryRoots[ i ].getChild( 1 ) );

            MapEntryExpression entryExpression = new MapEntryExpression( keyExpression,
                                                                         valueExpression );
            mapExpression.addMapEntryExpression( entryExpression );
        }

        return mapExpression;
    }

    protected RangeExpression rangeExpression(CSTNode expressionRoot) throws ParserException
    {
        return new RangeExpression( expression( expressionRoot.getChild( 0 ) ),
                                    expression( expressionRoot.getChild( 1 ) ) );
    }

    protected Expression propertyExpression(CSTNode expressionRoot) throws ParserException
    {
        Expression objectExpression = expression( expressionRoot.getChild( 0 ) );

        String propertyName = expressionRoot.getChild( 1 ).getToken().getText();

        return new PropertyExpression ( objectExpression,
                                        propertyName );
    }

    protected Expression variableOrClassExpression(CSTNode expressionRoot)
    {
        String text = expressionRoot.getToken().getText();

        if ( isDatatype( text ) ) {
            return new ClassExpression( resolveName( text ) );
        }

        return variableExpression( expressionRoot );
    }

    protected VariableExpression variableExpression(CSTNode expressionRoot)
    {
        return new VariableExpression( expressionRoot.getToken().getText() );
    }

    protected ConstantExpression constantExpression(CSTNode expressionRoot)
    {
        ConstantExpression expr = null;

        switch ( expressionRoot.getToken().getType() )
        {
            case ( Token.KEYWORD_NULL ):
            {
                expr = ConstantExpression.NULL;
                break;
            }
            case ( Token.KEYWORD_TRUE ):
            {
                expr = ConstantExpression.TRUE;
                break;
            }
            case( Token.KEYWORD_FALSE ):
            {
                expr = ConstantExpression.FALSE;
                break;
            }
            case ( Token.SINGLE_QUOTE_STRING ):
            {
                expr = new ConstantExpression( expressionRoot.getToken().getText() );
                break;
            }
            case ( Token.DOUBLE_QUOTE_STRING ):
            {
                // FIXME... not really a constant expr
                // after building string-concat expr from it
                // but this is just a convenient place for it now.
                expr = new ConstantExpression( expressionRoot.getToken().getText() );
                break;
            }
            case ( Token.INTEGER_NUMBER ):
            {
                expr = new ConstantExpression( createInteger( expressionRoot.getToken().getText() ) );
                break;
            }
            case ( Token.FLOAT_NUMBER ):
            {
                expr = new ConstantExpression( new Double( expressionRoot.getToken().getText() ) );
                break;
            }
        }

        return expr;
    }
  
    /**
     * Chooses the right Number implementation for the given integer number.
     * Typically this is an Integer for efficiency or a Long if the number is very large
     * 
     * @param text
     * @return
     */
    protected Number createInteger(String text) 
    {
        // lets use an Integer if it will fit as it makes for more efficient bytecode
        Long answer = new Long(text);
        long l = answer.longValue();
        if (l > Integer.MIN_VALUE && l < Integer.MAX_VALUE) 
        {
            return new Integer((int) l);
        }
        return answer;
    }
    
    protected BinaryExpression binaryExpression(CSTNode expressionRoot) throws ParserException
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

        // if not protected or private lets default to public
        if ( (modifiers & (Constants.ACC_PROTECTED | Constants.ACC_PRIVATE)) == 0 )
        {
            modifiers |= Constants.ACC_PUBLIC;
        }

        return modifiers;
    }

    protected String identifier(CSTNode identifierRoot)
    {
        return identifierRoot.getToken().getText();
    }

    /**
     * Stores the line number information in the new AST node
     * @param text
     * @return
     */
    protected void setLineNumber(ASTNode astNode, CSTNode cstNode) 
    {
        astNode.setLineNumber(cstNode.getToken().getStartLine());
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
