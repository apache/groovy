package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenStream;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.IOException;

public class Parser
{
    private TokenStream tokenStream;

    public Parser(TokenStream tokenStream)
    {
        this.tokenStream = tokenStream;
    }

    public TokenStream getTokenStream()
    {
        return this.tokenStream;
    }

    public void optionalSemicolon()
        throws IOException, SyntaxException
    {
        if ( lt() == Token.SEMICOLON )
        {
            consume( Token.SEMICOLON );
        }
    }

    public CSTNode compilationUnit()
        throws IOException, SyntaxException
    {
        CSTNode compilationUnit = new CSTNode();

        CSTNode packageDeclaration = null;

        if ( lt() == Token.KEYWORD_PACKAGE )
        {
            packageDeclaration = packageDeclaration();
            optionalSemicolon();
        }
        else
        {
            packageDeclaration = new CSTNode( Token.keyword( -1,
                                                             -1,
                                                             "package" ) );

            packageDeclaration.addChild( new CSTNode() );
        }

        compilationUnit.addChild( packageDeclaration );

        CSTNode imports = new CSTNode();

        compilationUnit.addChild( imports );

        while ( lt() == Token.KEYWORD_IMPORT )
        {
            imports.addChild( importStatement() );
            optionalSemicolon();
            //consume( Token.SEMICOLON );
        }

        while ( lt() != -1 )
        {
            compilationUnit.addChild( typeDeclaration() );
        }

        return compilationUnit;
    }

    public CSTNode packageDeclaration()
        throws IOException, SyntaxException
    {
        CSTNode packageDeclaration = rootNode( Token.KEYWORD_PACKAGE );

        CSTNode cur = rootNode( Token.IDENTIFIER );

        while ( lt() == Token.DOT )
        {
            CSTNode dot = rootNode( Token.DOT,
                                    cur );
            consume( dot,
                     Token.IDENTIFIER );

            cur = dot;
        }

        packageDeclaration.addChild( cur );

        return packageDeclaration;
    }

    public CSTNode importStatement()
        throws IOException, SyntaxException
    {
        CSTNode importStatement = rootNode( Token.KEYWORD_IMPORT );

        CSTNode cur = rootNode( Token.IDENTIFIER );

        while ( lt() == Token.DOT )
        {
            CSTNode dot = rootNode( Token.DOT,
                                    cur );
            consume( dot,
                     Token.IDENTIFIER ) ;

            cur = dot;
        }

        importStatement.addChild( cur );

        if ( lt() == Token.KEYWORD_AS )
        {
            CSTNode as = rootNode( Token.KEYWORD_AS );

            consume( as,
                     Token.IDENTIFIER );

            importStatement.addChild( as );
        }
        else
        {
            importStatement.addChild( new CSTNode() );
        }

        return importStatement;
    }

    public CSTNode typeDeclaration()
        throws IOException, SyntaxException
    {
        CSTNode declaration = null;

        CSTNode modifiers = new CSTNode();

        while ( isModifier( lt() ) )
        {
            consume( modifiers,
                     lt() );
        }

        switch ( lt() )
        {
            case ( Token.KEYWORD_CLASS ):
            {
                declaration = classDeclaration( modifiers );
                break;
            }
            case ( Token.KEYWORD_INTERFACE ):
            {
                declaration = interfaceDeclaration( modifiers );
                break;
            }
            default:
            {
                throwExpected( new int[] {
                    Token.KEYWORD_CLASS,
                    Token.KEYWORD_INTERFACE
                } );
            }
        }

        return declaration;
    }

    public CSTNode classDeclaration(CSTNode modifiers)
        throws IOException, SyntaxException
    {
        CSTNode classDeclaration = rootNode( Token.KEYWORD_CLASS );

        classDeclaration.addChild( modifiers );

        consume( classDeclaration,
                 Token.IDENTIFIER );

        if ( lt() == Token.KEYWORD_EXTENDS )
        {
            CSTNode extendsNode = rootNode( Token.KEYWORD_EXTENDS );

            classDeclaration.addChild( extendsNode );

            CSTNode datatype = datatype();

            extendsNode.addChild( datatype );

        }
        else
        {
            classDeclaration.addChild( new CSTNode() );
        }

        if ( lt() == Token.KEYWORD_IMPLEMENTS )
        {
            CSTNode implementsNode = rootNode( Token.KEYWORD_IMPLEMENTS );

            classDeclaration.addChild( implementsNode );

            CSTNode datatype = datatype();

            implementsNode.addChild( datatype );

            while ( lt() == Token.COMMA )
            {
                consume( Token.COMMA );
                datatype = datatype();
                implementsNode.addChild( datatype );
            }
        }
        else
        {
            classDeclaration.addChild( new CSTNode() );
        }

        consume( Token.LEFT_CURLY_BRACE );

        CSTNode body = new CSTNode();

        classDeclaration.addChild( body );

      BODY_LOOP:
        while ( true )
        {
            switch ( lt() )
            {
                case ( -1 ):
                {
                    break BODY_LOOP;
                }
                case ( Token.RIGHT_CURLY_BRACE ):
                {
                    break BODY_LOOP;
                }
                default:
                {
                    body.addChild( bodyStatement() );
                }
            }
        }

        consume( Token.RIGHT_CURLY_BRACE );

        return classDeclaration;
    }

    public CSTNode interfaceDeclaration(CSTNode modifiers)
        throws IOException, SyntaxException
    {
        CSTNode interfaceDeclaration = rootNode( Token.KEYWORD_INTERFACE );

        interfaceDeclaration.addChild( modifiers );

        consume( interfaceDeclaration,
                 Token.IDENTIFIER );

        interfaceDeclaration.addChild( new CSTNode() );

        if ( lt() == Token.KEYWORD_EXTENDS )
        {
            CSTNode extendsNode = rootNode( Token.KEYWORD_EXTENDS );

            interfaceDeclaration.addChild( extendsNode );

            CSTNode datatype = datatype();

            extendsNode.addChild( datatype );

            while ( lt() == Token.COMMA )
            {
                consume( Token.COMMA );
                datatype = datatype();
                extendsNode.addChild( datatype );
            }
        }

        consume( Token.LEFT_CURLY_BRACE );
        consume( Token.RIGHT_CURLY_BRACE );

        return interfaceDeclaration;
    }

    public CSTNode bodyStatement()
        throws IOException, SyntaxException
    {
        CSTNode bodyStatement = null;

        CSTNode modifiers = new CSTNode();

        while( isModifier( lt() ) )
        {
            consume( modifiers,
                     lt() );
        }

        switch ( lt() )
        {
            case ( Token.KEYWORD_PROPERTY ):
            {
                bodyStatement = propertyDeclaration( modifiers );
                optionalSemicolon();
                //consume( Token.SEMICOLON );
                break;
            }
            case ( Token.IDENTIFIER ):
            case ( Token.KEYWORD_VOID ):
            {
                bodyStatement = methodDeclaration( modifiers );
                break;
            }
            default:
            {

            }
        }

        return bodyStatement;
    }

    public CSTNode propertyDeclaration(CSTNode modifiers)
        throws IOException, SyntaxException
    {
        CSTNode propertyDeclaration = rootNode( Token.KEYWORD_PROPERTY );

        propertyDeclaration.addChild( modifiers );

        // property | cheese;
        // property | foo.Bar cheese;
        // property | Foo cheese;

        CSTNode type = null;

        if ( lt( 2 ) == Token.DOT
             ||
             lt( 2 ) == Token.IDENTIFIER )
        {
            // has datatype
            type = datatype();
        }
        else
        {
            type = new CSTNode();
        }

        consume( propertyDeclaration,
                 Token.IDENTIFIER );

        propertyDeclaration.addChild( type );

        return propertyDeclaration;
    }

    public CSTNode methodDeclaration(CSTNode modifiers)
        throws IOException, SyntaxException
    {
        CSTNode methodDeclaration = new CSTNode( Token.syntheticMethod() );

        methodDeclaration.addChild( modifiers );

        // foo(...)
        // void foo(...)
        // com.Cheese foo(...)

        CSTNode type = null;


        switch ( lt( 2 ) )
        {
            case ( Token.LEFT_PARENTHESIS ):
            {
                type = new CSTNode();
                break;
            }
            default:
            {
                type = datatype();
            }
        }

        consume( methodDeclaration,
                 Token.IDENTIFIER );

        methodDeclaration.addChild( type );

        CSTNode paramsRoot = rootNode( Token.LEFT_PARENTHESIS );

        methodDeclaration.addChild( paramsRoot );

        while ( lt() != Token.RIGHT_PARENTHESIS )
        {
            switch ( lt( 2 ) )
            {
                case ( Token.DOT ):
                case ( Token.IDENTIFIER ):
                {
                    type = datatype();
                    break;
                }
                default:
                {
                    type = new CSTNode();
                }
            }

            CSTNode param = new CSTNode();

            paramsRoot.addChild( param );

            consume( param,
                     Token.IDENTIFIER );

            param.addChild( type );

            if ( lt() == Token.COMMA )
            {
                consume( Token.COMMA );
            }
        }

        consume( Token.RIGHT_PARENTHESIS );

        methodDeclaration.addChild( statementBlock() );

        return methodDeclaration;
    }

    protected CSTNode parameterDeclarationList()
        throws IOException, SyntaxException
    {
        CSTNode parameterDeclarationList = new CSTNode();

        while ( lt() == Token.IDENTIFIER )
        {
            parameterDeclarationList.addChild( parameterDeclaration() );

            if ( lt() == Token.COMMA )
            {
                consume( Token.COMMA );
            }
            else
            {
                break;
            }
        }

        return parameterDeclarationList;
    }

    protected CSTNode parameterDeclaration()
        throws IOException, SyntaxException
    {
        CSTNode parameterDeclaration = null;

        switch ( lt( 2 ) )
        {
            case ( Token.IDENTIFIER ):
            case ( Token.DOT ):
            {
                parameterDeclaration = parameterDeclarationWithDatatype();
                break;
            }
            default:
            {
                parameterDeclaration = parameterDeclarationWithoutDatatype();
                break;
            }
        }

        return parameterDeclaration;
    }

    protected CSTNode parameterDeclarationWithDatatype()
        throws IOException, SyntaxException
    {
        CSTNode parameterDeclaration = new CSTNode( Token.syntheticParameterDeclaration() );

        CSTNode datatype = datatype();

        parameterDeclaration.addChild( datatype );

        consume( parameterDeclaration,
                 Token.IDENTIFIER );

        return parameterDeclaration;
    }

    protected CSTNode parameterDeclarationWithoutDatatype()
        throws IOException, SyntaxException
    {
        CSTNode parameterDeclaration = new CSTNode( Token.syntheticParameterDeclaration() );

        consume( parameterDeclaration,
                 Token.IDENTIFIER );

        parameterDeclaration.addChild( new CSTNode() );

        return parameterDeclaration;
    }

    protected CSTNode datatype()
        throws IOException, SyntaxException
    {
        CSTNode datatype = null;

        switch ( lt() )
        {
            case( Token.KEYWORD_VOID ):
            case( Token.KEYWORD_INT ):
            case( Token.KEYWORD_FLOAT ):
            {
                datatype = rootNode( lt() );
                break;
            }
            default:
            {
                datatype = rootNode( Token.IDENTIFIER );
                
                while ( lt() == Token.DOT )
                {
                    CSTNode dot = rootNode( Token.DOT,
                                            datatype );
                    consume( dot,
                             Token.IDENTIFIER );
                    
                    datatype = dot;
                }
            }
        }

        return datatype;
    }

    protected CSTNode statementBlock()
        throws IOException, SyntaxException
    {
        CSTNode statementBlock = rootNode( Token.LEFT_CURLY_BRACE );

        statementsUntilRightCurly( statementBlock );

        consume( Token.RIGHT_CURLY_BRACE );

        return statementBlock;
    }

    protected void statementsUntilRightCurly(CSTNode root)
        throws IOException, SyntaxException
    {
        while ( lt() != Token.RIGHT_CURLY_BRACE )
        {
            root.addChild( statement() );

            if ( lt() == Token.RIGHT_CURLY_BRACE )
            {
                break;
            }
            else if ( lt() == -1 )
            {
                throwExpected( new int[] { Token.RIGHT_CURLY_BRACE } );
            }
        }
    }

    protected CSTNode statement()
        throws IOException, SyntaxException
    {
        CSTNode statement = null;

        switch ( lt() )
        {
            case ( Token.KEYWORD_FOR ):
            {
                statement = forStatement();
                break;
            }
            case ( Token.KEYWORD_WHILE ):
            {
                break;
            }
            case ( Token.KEYWORD_CONTINUE ):
            {
                break;
            }
            case ( Token.KEYWORD_BREAK ):
            {
                break;
            }
            case ( Token.KEYWORD_IF ):
            {
                statement = ifStatement();
                break;
            }
            case ( Token.KEYWORD_TRY ):
            {
                statement = tryStatement();
                break;
            }
            case ( Token.KEYWORD_THROW ):
            {
                break;
            }
            case ( Token.KEYWORD_SYNCHRONIZED ):
            {
                break;
            }
            case ( Token.KEYWORD_SWITCH ):
            {
                break;                
            }
            case ( Token.KEYWORD_RETURN ):
            {
                statement = returnStatement();
                optionalSemicolon();
                //consume( Token.SEMICOLON );
                break;
            }
            case ( Token.KEYWORD_ASSERT ):
            {
                statement = assertStatement();
                optionalSemicolon();
                //consume( Token.SEMICOLON );
                break;
            }
            default:
            {
                statement = expression();
                optionalSemicolon();
                //consume( Token.SEMICOLON );
            }
        }

        return statement;
    }

    protected CSTNode ifStatement()
        throws IOException, SyntaxException
    {
        CSTNode statement = rootNode( Token.KEYWORD_IF );

        consume( Token.LEFT_PARENTHESIS );

        statement.addChild( expression() );

        consume( Token.RIGHT_PARENTHESIS );

        statement.addChild( statementBlock() );

        CSTNode cur = statement;

        while ( lt() == Token.KEYWORD_ELSE
                &&
                lt( 2 ) == Token.KEYWORD_IF )
        {
            consume( Token.KEYWORD_ELSE );
            CSTNode next = ifStatement();
            cur.addChild( next );
            cur = next;
        }

        if ( lt() == Token.KEYWORD_ELSE )
        {
            CSTNode next = rootNode( Token.KEYWORD_ELSE );
            next.addChild( statementBlock() );
            cur.addChild( next );
        }

        return statement;
    }

    protected CSTNode tryStatement()
        throws IOException, SyntaxException
    {
        CSTNode statement = rootNode( Token.KEYWORD_TRY );

        statement.addChild( statementBlock() );

        CSTNode catches = new CSTNode();

        while ( lt() == Token.KEYWORD_CATCH )
        {
            CSTNode catchBlock = rootNode( Token.KEYWORD_CATCH );

            consume( Token.LEFT_PARENTHESIS );

            catchBlock.addChild( datatype() );

            consume( catchBlock,
                     Token.IDENTIFIER );

            consume( Token.RIGHT_PARENTHESIS );

            catchBlock.addChild( statementBlock() );

            catches.addChild( catchBlock );
        }

        if ( lt() == Token.KEYWORD_FINALLY )
        {
            consume( Token.KEYWORD_FINALLY );
            statement.addChild( statementBlock() );
        }
        else
        {
            statement.addChild( new CSTNode() );
        }

        statement.addChild( catches );

        return statement;
    }

    protected CSTNode returnStatement()
        throws IOException, SyntaxException
    {
        CSTNode statement = rootNode( Token.KEYWORD_RETURN );

        statement.addChild( expression() );

        return statement;
    }

    protected CSTNode forStatement()
        throws IOException, SyntaxException
    {
        CSTNode statement = rootNode( Token.KEYWORD_FOR );

        consume( Token.LEFT_PARENTHESIS );

        consume( statement,
                 Token.IDENTIFIER );

        Token potentialIn = consume( Token.IDENTIFIER );

        if ( ! potentialIn.getText().equals( "in" ) )
        {
            throw new UnexpectedTokenException( potentialIn,
                                                new int[] { } );
        }

        CSTNode expr = expression();

        statement.addChild( expr );

        consume( Token.RIGHT_PARENTHESIS );

        statement.addChild( statementBlock() );

        return statement;
    }

    protected CSTNode assertStatement()
        throws IOException, SyntaxException
    {
        CSTNode statement = rootNode( Token.KEYWORD_ASSERT );

        statement.addChild( conditionalExpression() );

        if ( lt() == Token.COLON )
        {
            consume( Token.COLON );

            statement.addChild( expression() );
        }
        else
        {
            statement.addChild( new CSTNode() );
        }

        return statement;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    protected CSTNode expression()
        throws IOException, SyntaxException
    {
        return assignmentExpression();
    }

    protected CSTNode assignmentExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = conditionalExpression();

        switch ( lt() )
        {
            case ( Token.EQUAL ):
            case ( Token.PLUS_EQUAL ):
            case ( Token.MINUS_EQUAL ):
            case ( Token.DIVIDE_EQUAL ):
            case ( Token.MULTIPLY_EQUAL ):
            case ( Token.MOD_EQUAL ):
            {
                expr = rootNode( lt(),
                                 expr );
                expr.addChild( conditionalExpression() );
                break;
            }
        }

        return expr;
    }

    protected CSTNode conditionalExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = logicalOrExpression();

        if ( lt() == Token.QUESTION )
        {
            rootNode( Token.QUESTION,
                      expr );
            expr.addChild( assignmentExpression() );

            consume( Token.COLON );

            expr.addChild( conditionalExpression() );
        }

        return expr;
    }

    protected CSTNode logicalOrExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = logicalAndExpression();

        if ( lt() == Token.LOGICAL_OR )
        {
            rootNode( Token.LOGICAL_OR,
                      expr );
            expr.addChild( logicalAndExpression() );
        }

        return expr;
    }

    protected CSTNode logicalAndExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = equalityExpression();

        if ( lt() == Token.LOGICAL_AND )
        {
            rootNode( Token.LOGICAL_AND,
                      expr );
            expr.addChild( equalityExpression() );
        }

        return expr;
    }

    protected CSTNode equalityExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = relationalExpression();

        switch ( lt() )
        {
            case ( Token.COMPARE_EQUAL ):
            case ( Token.COMPARE_NOT_EQUAL ):
            case ( Token.COMPARE_IDENTICAL ):
            {
                expr = rootNode( lt(),
                                 expr );
                expr.addChild( relationalExpression() );
                break;
            }
        }

        return expr;
    }

    protected CSTNode relationalExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = rangeExpression();

        switch ( lt() )
        {
            case ( Token.COMPARE_LESS_THAN ):
            case ( Token.COMPARE_LESS_THAN_EQUAL ):
            case ( Token.COMPARE_GREATER_THAN ):
            case ( Token.COMPARE_GREATER_THAN_EQUAL ):
            case ( Token.KEYWORD_INSTANCEOF ):
            {
                expr = rootNode( lt(),
                                 expr );
                expr.addChild( rangeExpression() );
                break;
            }
        }

        return expr;
    }

    protected CSTNode rangeExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = additiveExpression();

        if ( lt() == Token.DOT_DOT )
        {
            expr = rootNode( Token.DOT_DOT,
                             expr );
            expr.addChild( additiveExpression() );
        }

        return expr;
    }

    protected CSTNode additiveExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = multiplicativeExpression();

        switch ( lt() )
        {
            case ( Token.PLUS ):
            case ( Token.MINUS ):
            {
                expr = rootNode( lt(),
                                 expr );
                expr.addChild( multiplicativeExpression() );
                break;
            }
        }

        return expr;
    }

    protected CSTNode multiplicativeExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = unaryExpression();

        switch ( lt() )
        {
            case ( Token.MULTIPLY ):
            case ( Token.DIVIDE ):
            case ( Token.MOD ):
            {
                expr = rootNode( lt(),
                                 expr );
                expr.addChild( unaryExpression() );
                break;
            }
        }

        return expr;
    }

    protected CSTNode unaryExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = null;

        switch ( lt() )
        {
            case ( Token.PLUS_PLUS ):
            case ( Token.PLUS ):
            case ( Token.MINUS_MINUS ):
            case ( Token.MINUS ):
            {
                expr = rootNode( lt() );
                expr.addChild( unaryExpression() );
                break;
            }
            default:
            {
                expr = primaryExpression();
                break;
            }
        }

        return expr;
    }

    protected CSTNode postfixExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = primaryExpression();

        switch ( lt() )
        {
            case ( Token.PLUS_PLUS ):
            case ( Token.MINUS_MINUS ):
            {
                expr = rootNode( lt(),
                                 expr );
                break;
            }
        }

        return expr;
    }

    protected CSTNode primaryExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr       = null;
        CSTNode identifier = null;

      PREFIX_SWITCH:
        switch ( lt() )
        {
            case ( Token.KEYWORD_TRUE ):
            case ( Token.KEYWORD_FALSE ):
            case ( Token.KEYWORD_NULL ):
            {
                expr = rootNode( lt() );
                break PREFIX_SWITCH;
            }
            case ( Token.KEYWORD_NEW ):
            {
                expr = newExpression();
                break PREFIX_SWITCH;
            }
            case ( Token.LEFT_PARENTHESIS ):
            {
                expr = parentheticalExpression();
                break PREFIX_SWITCH;
            }
            case ( Token.INTEGER_NUMBER ):
            case ( Token.FLOAT_NUMBER ):
            case ( Token.DOUBLE_QUOTE_STRING ):
            case ( Token.SINGLE_QUOTE_STRING ):
            {
                expr = rootNode( lt() );
                break PREFIX_SWITCH;
            }
            case ( Token.LEFT_SQUARE_BRACKET ):
            {
                expr = listOrMapExpression();
                break PREFIX_SWITCH;
            }
            case ( Token.LEFT_CURLY_BRACE ):
            {
                expr = closureExpression();
                break PREFIX_SWITCH;
            }
            case ( Token.KEYWORD_THIS ):
            {
                expr = new CSTNode( Token.keyword( -1,
                                                   -1,
                                                   "this" ) );
                identifier = rootNode( lt() );
                break PREFIX_SWITCH;
            }
            case ( Token.KEYWORD_SUPER ):
            {
                expr = new CSTNode( Token.keyword( -1,
                                                   -1,
                                                   "super" ) );
                identifier = rootNode( lt() );
                break PREFIX_SWITCH;
            }
            case ( Token.IDENTIFIER ):
            {
                //expr       = identifier;
                //expr       = new CSTNode( Token.keyword( -1,
                 //                                        -1,
                 //                                        "this" ) );
                identifier = rootNode( lt() );
                expr = identifier;
                break PREFIX_SWITCH;
            }
            default:
            {
                throwExpected( new int[] { } );
            }
        }

        if ( identifier != null
             &&
             ( lt() == Token.LEFT_PARENTHESIS
               ||
               lt() == Token.LEFT_CURLY_BRACE ) )
        {
            if ( expr == identifier )
            {
                CSTNode replacementExpr = new CSTNode();
                CSTNode resultExpr      = sugaryMethodCallExpression( replacementExpr,
                                                                      identifier );
                if ( resultExpr != replacementExpr )
                {
                    expr = resultExpr;
                }
            }
            else
            {
                expr = sugaryMethodCallExpression( expr,
                                                   identifier );
            }
        }

      DOT_LOOP:
        while ( lt() == Token.DOT
                &&
                lt( 2 ) == Token.IDENTIFIER )
        {
            CSTNode dotExpr = rootNode( Token.DOT );

            identifier = rootNode( Token.IDENTIFIER );

          DOT_TYPE_SWITCH:
            switch ( lt() )
            {
                case ( Token.LEFT_PARENTHESIS ):
                case ( Token.LEFT_CURLY_BRACE ):
                {
                    expr = sugaryMethodCallExpression( expr,
                                                       identifier );
                    break DOT_TYPE_SWITCH;
                }
                default:
                {
                    dotExpr.addChild( expr );
                    dotExpr.addChild( identifier );
                    expr = dotExpr;
                    break DOT_TYPE_SWITCH;
                }
            }
        }

        return expr;
    }

    protected CSTNode sugaryMethodCallExpression(CSTNode expr,
                                                 CSTNode identifier)
        throws IOException,SyntaxException
    {
        CSTNode methodExpr = null;
        CSTNode paramList  = null;
        
        if ( lt() == Token.LEFT_PARENTHESIS )
        {
            methodExpr = rootNode( Token.LEFT_PARENTHESIS );
            methodExpr.addChild( expr );
            methodExpr.addChild( identifier );
            paramList = parameterList( Token.RIGHT_PARENTHESIS );
            methodExpr.addChild( paramList );
            consume( Token.RIGHT_PARENTHESIS );
        }

        if ( lt() == Token.LEFT_CURLY_BRACE )
        {
            if ( methodExpr == null )
            {
                methodExpr = new CSTNode( Token.leftParenthesis( -1,
                                                                 -1 ) );
                methodExpr.addChild( expr );
                methodExpr.addChild( identifier );
                paramList = parameterList( Token.LEFT_CURLY_BRACE );
                methodExpr.addChild( paramList );
            }

            paramList.addChild( closureExpression() );
        }

        if ( methodExpr != null )
        {
            expr = methodExpr;
        }

        return expr;
    }

    protected CSTNode parentheticalExpression()
        throws IOException, SyntaxException
    {
        consume( Token.LEFT_PARENTHESIS );
        
        CSTNode expr = expression();
        
        consume( Token.RIGHT_PARENTHESIS );

        return expr;
    }

    protected CSTNode parameterList(int endOfListDemarc)
        throws IOException, SyntaxException
    {
        CSTNode parameterList = new CSTNode( Token.syntheticList() );

        while ( lt() != endOfListDemarc )
        {
            parameterList.addChild( expression() );

            if ( lt() == Token.COMMA )
            {
                consume( Token.COMMA );
            }
            else
            {
                break;
            }
        }

        return parameterList;
    }

    protected CSTNode newExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = rootNode( Token.KEYWORD_NEW );

        expr.addChild( datatype() );

        consume( Token.LEFT_PARENTHESIS );

        expr.addChild( parameterList( Token.RIGHT_PARENTHESIS ) );

        consume( Token.RIGHT_PARENTHESIS );

        return expr;
    }

    protected CSTNode closureExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = rootNode( Token.LEFT_CURLY_BRACE );

        boolean pipeRequired = false;

        // { statement();
        // { a |
        // { a, b |
        // { A a |
        // { A a, B b |
        
        if ( lt( 2 ) == Token.PIPE
             ||
             lt( 2 ) == Token.COMMA
             ||
             lt( 3 ) == Token.PIPE
             ||
             lt( 3 ) == Token.COMMA )
        {
            expr.addChild( parameterDeclarationList() );
            pipeRequired = true;
        }
        else
        {
            expr.addChild( new CSTNode() );
        }

        if ( pipeRequired
             ||
             lt() == Token.PIPE )
        {
            consume( Token.PIPE );
        }

        CSTNode block = new CSTNode();

        statementsUntilRightCurly( block );

        consume( Token.RIGHT_CURLY_BRACE );

        expr.addChild( block );

        return expr;
    }

    protected CSTNode listOrMapExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = null;

        consume( Token.LEFT_SQUARE_BRACKET );

        if ( lt() == Token.COLON )
        {
            // it is an empty map
            consume( Token.COLON );
            expr = new CSTNode( Token.syntheticMap() );
        }
        else if ( lt() == Token.RIGHT_SQUARE_BRACKET )
        {
            // it is an empty list
            expr = new CSTNode( Token.syntheticList() );
        }
        else
        {
            CSTNode firstExpr = expression();

            if ( lt() == Token.COLON )
            {
                expr = mapExpression( firstExpr );
            }
            else
            {
                expr = listExpression( firstExpr );
            }
        }

        consume( Token.RIGHT_SQUARE_BRACKET );

        return expr;
    }

    protected CSTNode mapExpression(CSTNode key)
        throws IOException, SyntaxException
    {
        CSTNode expr = new CSTNode( Token.syntheticMap() );

        CSTNode entry = rootNode( Token.COLON,
                                  key );

        CSTNode value = expression();

        entry.addChild( value );

        expr.addChild( entry );

        while ( lt() == Token.COMMA )
        {
            consume( Token.COMMA );

            key = expression();

            entry = rootNode( Token.COLON,
                              key );

            entry.addChild( expression() );

            expr.addChild( entry );
        }

        return expr;
    }

    protected CSTNode listExpression(CSTNode entry)
        throws IOException, SyntaxException
    {
        CSTNode expr = new CSTNode( Token.syntheticList() );

        expr.addChild( entry );

        while ( lt() == Token.COMMA )
        {
            consume( Token.COMMA );

            entry = expression();

            expr.addChild( entry );
        }

        return expr;
    }
    
    /*
    protected CSTNode listExpression()
        throws IOException, SyntaxException
    {
        CSTNode expr = rootNode( Token.LEFT_SQUARE_BRACKET );

        while ( lt() != Token.RIGHT_SQUARE_BRACKET )
        {
            expr.addChild( expression() );

            if ( lt() == Token.COMMA )
            {
                consume( Token.COMMA );
            }
            else
            {
                break;
            }
        }

        consume( Token.RIGHT_SQUARE_BRACKET );

        return expr;
    }
    */

    protected CSTNode argumentList()
        throws IOException, SyntaxException
    {
        CSTNode argumentList = new CSTNode();

        while ( lt() != Token.RIGHT_PARENTHESIS )
        {
            argumentList.addChild( expression() );

            if ( lt() == Token.COMMA )
            {
                consume( Token.COMMA );
            }
            else
            {
                break;
            }
        }

        return argumentList;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    protected static boolean isModifier(int type)
    {
        switch ( type )
        {
            case ( Token.KEYWORD_PUBLIC ):
            case ( Token.KEYWORD_PROTECTED ):
            case ( Token.KEYWORD_PRIVATE ):
            case ( Token.KEYWORD_STATIC ):
            case ( Token.KEYWORD_FINAL ):
            case ( Token.KEYWORD_SYNCHRONIZED ):
            {
                return true;
            }
            default:
            {
                return false;
            }
        }
    }

    protected void throwExpected(int[] expectedTypes)
        throws IOException, SyntaxException
    {
        throw new UnexpectedTokenException( la(),
                                            expectedTypes );
    }

    protected void consumeUntil(int type)
        throws IOException, SyntaxException
    {
        while ( lt() != -1 )
        {
            consume( lt() );

            if ( lt() == type )
            {
                consume( lt() );
                break;
            }
        }
    }

    protected Token la()
        throws IOException, SyntaxException
    {
        return la( 1 );
    }

    protected int lt()
        throws IOException, SyntaxException
    {
        Token token = la();

        if ( token == null )
        {
            return -1;
        }

        return token.getType();
    }

    protected Token la(int k)
        throws IOException, SyntaxException
    {
        return getTokenStream().la( k );
    }

    protected int lt(int k)
        throws IOException, SyntaxException
    {
        Token token = la( k );

        if ( token == null )
        {
            return -1;
        }

        return token.getType();
    }

    protected Token consume(int type)
        throws IOException, SyntaxException
    {
        if ( lt() != type )
        {
            throw new UnexpectedTokenException( la(),
                                                type );
        }

        return getTokenStream().consume( type );
    }

    protected void consume(CSTNode root,
                           int type)
        throws IOException, SyntaxException
    {
        root.addChild( new CSTNode( consume( type ) ) );
    }

    protected CSTNode rootNode(int type)
        throws IOException, SyntaxException
    {
        return new CSTNode( consume( type ) );
    }

    protected CSTNode rootNode(int type,
                               CSTNode child)
        throws IOException, SyntaxException
    {
        CSTNode root = new CSTNode( consume( type ) );
        root.addChild( child );
        return root;
    }
}
