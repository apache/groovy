package org.codehaus.groovy.syntax.parser;

import java.io.IOException;

import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenStream;
import org.codehaus.groovy.syntax.lexer.Lexer;
import org.codehaus.groovy.syntax.lexer.LexerTokenStream;
import org.codehaus.groovy.syntax.lexer.StringCharStream;

public class Parser {
    private static final int[] identifierTokens = {Token.IDENTIFIER, Token.KEYWORD_CLASS, Token.KEYWORD_DEF };
    
    private TokenStream tokenStream;
    private boolean lastTokenStatementSeparator;

    public Parser(TokenStream tokenStream) {
        this.tokenStream = tokenStream;
    }

    public TokenStream getTokenStream() {
        return this.tokenStream;
    }

    public void optionalSemicolon() throws IOException, SyntaxException {
        while (lt_bare() == Token.SEMICOLON || lt_bare() == Token.NEWLINE) {
            consume_bare(lt_bare());
        }
    }

    public void optionalNewlines() throws IOException, SyntaxException {
        while (lt_bare() == Token.NEWLINE) {
            consume_bare(lt_bare());
        }
    }

    public CSTNode compilationUnit() throws IOException, SyntaxException {
        CSTNode compilationUnit = new CSTNode();

        CSTNode packageDeclaration = null;

        if (lt() == Token.KEYWORD_PACKAGE) {
            packageDeclaration = packageDeclaration();
            optionalSemicolon();
        }
        else {
            packageDeclaration = new CSTNode(Token.keyword(-1, -1, "package"));

            packageDeclaration.addChild(new CSTNode());
        }

        compilationUnit.addChild(packageDeclaration);

        CSTNode imports = new CSTNode();

        compilationUnit.addChild(imports);

        while (lt() == Token.KEYWORD_IMPORT) {
            imports.addChild(importStatement());
            optionalSemicolon();
        }

        while (lt() != -1) {
            compilationUnit.addChild(typeDeclaration());
        }

        return compilationUnit;
    }

    public CSTNode packageDeclaration() throws IOException, SyntaxException {
        CSTNode packageDeclaration = rootNode(Token.KEYWORD_PACKAGE);

        CSTNode cur = rootNode(Token.IDENTIFIER);

        while (lt() == Token.DOT) {
            CSTNode dot = rootNode(Token.DOT, cur);

            consume(dot, Token.IDENTIFIER);

            cur = dot;
        }

        packageDeclaration.addChild(cur);

        return packageDeclaration;
    }

    public CSTNode importStatement() throws IOException, SyntaxException {
        CSTNode importStatement = rootNode(Token.KEYWORD_IMPORT);

        CSTNode cur = rootNode(Token.IDENTIFIER);

        while (lt() == Token.DOT) {
            CSTNode dot = rootNode(Token.DOT, cur);
            consume(dot, Token.IDENTIFIER);

            cur = dot;
        }

        importStatement.addChild(cur);

        if (lt() == Token.KEYWORD_AS) {
            CSTNode as = rootNode(Token.KEYWORD_AS);

            consume(as, Token.IDENTIFIER);

            importStatement.addChild(as);
        }
        else {
            importStatement.addChild(new CSTNode());
        }

        return importStatement;
    }

    public CSTNode typeDeclaration() throws IOException, SyntaxException {
        CSTNode declaration = null;

        CSTNode modifiers = new CSTNode();

        if (lt() == Token.KEYWORD_DEF) {
            consume(lt());

            while (isModifier(lt())) {
                consume(modifiers, lt());
            }
            CSTNode type = methodReturnType();
            CSTNode identifier = methodIdentifier();

            return methodDeclaration(modifiers, type, identifier);
        }

        while (isModifier(lt())) {
            consume(modifiers, lt());
        }

        switch (lt()) {
            case (Token.KEYWORD_CLASS) :
                {
                    declaration = classDeclaration(modifiers);
                    break;
                }
            case (Token.KEYWORD_INTERFACE) :
                {
                    declaration = interfaceDeclaration(modifiers);
                    break;
                }
            default :
                {
                    declaration = statement();
                    /*                
                                    throwExpected( new int[] {
                                        Token.KEYWORD_CLASS,
                                        Token.KEYWORD_INTERFACE
                                    } );
                    */
                }
        }

        return declaration;
    }

    public CSTNode classDeclaration(CSTNode modifiers) throws IOException, SyntaxException {
        CSTNode classDeclaration = rootNode(Token.KEYWORD_CLASS);

        classDeclaration.addChild(modifiers);

        consume(classDeclaration, Token.IDENTIFIER);

        if (lt() == Token.KEYWORD_EXTENDS) {
            CSTNode extendsNode = rootNode(Token.KEYWORD_EXTENDS);

            classDeclaration.addChild(extendsNode);

            CSTNode datatype = datatype();

            extendsNode.addChild(datatype);

        }
        else {
            classDeclaration.addChild(new CSTNode());
        }

        if (lt() == Token.KEYWORD_IMPLEMENTS) {
            CSTNode implementsNode = rootNode(Token.KEYWORD_IMPLEMENTS);

            classDeclaration.addChild(implementsNode);

            CSTNode datatype = datatype();

            implementsNode.addChild(datatype);

            while (lt() == Token.COMMA) {
                consume(Token.COMMA);
                datatype = datatype();
                implementsNode.addChild(datatype);
            }
        }
        else {
            classDeclaration.addChild(new CSTNode());
        }

        consume(Token.LEFT_CURLY_BRACE);

        CSTNode body = new CSTNode();

        classDeclaration.addChild(body);

        BODY_LOOP : while (true) {
            switch (lt()) {
                case (-1) :
                    {
                        break BODY_LOOP;
                    }
                case (Token.RIGHT_CURLY_BRACE) :
                    {
                        break BODY_LOOP;
                    }
                default :
                    {
                        body.addChild(bodyStatement());
                    }
            }
        }

        consume(Token.RIGHT_CURLY_BRACE);

        return classDeclaration;
    }

    public CSTNode interfaceDeclaration(CSTNode modifiers) throws IOException, SyntaxException {
        CSTNode interfaceDeclaration = rootNode(Token.KEYWORD_INTERFACE);

        interfaceDeclaration.addChild(modifiers);

        consume(interfaceDeclaration, Token.IDENTIFIER);

        interfaceDeclaration.addChild(new CSTNode());

        if (lt() == Token.KEYWORD_EXTENDS) {
            CSTNode extendsNode = rootNode(Token.KEYWORD_EXTENDS);

            interfaceDeclaration.addChild(extendsNode);

            CSTNode datatype = datatype();

            extendsNode.addChild(datatype);

            while (lt() == Token.COMMA) {
                consume(Token.COMMA);
                datatype = datatype();
                extendsNode.addChild(datatype);
            }
        }

        consume(Token.LEFT_CURLY_BRACE);
        consume(Token.RIGHT_CURLY_BRACE);

        return interfaceDeclaration;
    }

    public CSTNode bodyStatement() throws IOException, SyntaxException {
        CSTNode bodyStatement = null;

        CSTNode modifiers = new CSTNode();

        while (isModifier(lt())) {
            consume(modifiers, lt());
        }

        // lets consume a property keyword until we deprecate it
        if (lt() == Token.KEYWORD_PROPERTY) {
            consume(lt());
        }

        // lets consume any newlines
        while (lt_bare() == Token.NEWLINE) {
            consume_bare(lt_bare());
        }

        CSTNode type = methodReturnType();
        CSTNode identifier = methodIdentifier();

        // now we must be either a property or method
        // not that after the identifier, the left parenthesis *must* be on the same line
        switch (lt_bare()) {
            case Token.LEFT_PARENTHESIS :
                bodyStatement = methodDeclaration(modifiers, type, identifier);
                break;

            case Token.EQUAL :
            case Token.SEMICOLON :
            case Token.NEWLINE :
            case Token.RIGHT_CURLY_BRACE :
            case -1 :
                bodyStatement = propertyDeclaration(modifiers, type, identifier);
                optionalSemicolon();
                break;

            default :
                throwExpected(
                    new int[] {
                        Token.LEFT_PARENTHESIS,
                        Token.EQUAL,
                        Token.SEMICOLON,
                        Token.NEWLINE,
                        Token.RIGHT_CURLY_BRACE });
        }

        return bodyStatement;
    }

    protected CSTNode methodIdentifier() throws IOException, SyntaxException {
        return new CSTNode(consume_bare(Token.IDENTIFIER));
    }

    protected CSTNode methodReturnType() throws IOException, SyntaxException {
        // lets consume the type if present
        // either an identifier, void or foo.bar.whatnot
        CSTNode type = new CSTNode();
        if (lt_bare() == Token.IDENTIFIER) {
            // could be method name or could be part of datatype
            switch (lt_bare(2)) {
                case Token.DOT :
                    {
                        // has datatype
                        type = datatype();
                        break;
                    }
                case (Token.IDENTIFIER) :
                    {
                        type = new CSTNode(consume_bare(lt()));
                        break;
                    }
            }
        }
        else {
            switch (lt_bare()) {
                case (Token.KEYWORD_VOID) :
                case (Token.KEYWORD_INT) :
                case (Token.KEYWORD_FLOAT) :
                case (Token.KEYWORD_DOUBLE) :
                case (Token.KEYWORD_CHAR) :
                case (Token.KEYWORD_BYTE) :
                case (Token.KEYWORD_SHORT) :
                case (Token.KEYWORD_LONG) :
                case (Token.KEYWORD_BOOLEAN) :
                    {
                        type = new CSTNode(consume_bare(lt_bare()));
                    }
            }
        }
        return type;
    }

    public CSTNode propertyDeclaration(CSTNode modifiers, CSTNode type, CSTNode identifier)
        throws IOException, SyntaxException {
        CSTNode propertyDeclaration = new CSTNode(Token.keyword(-1, -1, "property"));

        propertyDeclaration.addChild(modifiers);
        propertyDeclaration.addChild(identifier);
        propertyDeclaration.addChild(type);

        if (lt() == Token.EQUAL) {
            consume(lt());
            propertyDeclaration.addChild(expression());
        }
        return propertyDeclaration;
    }

    public CSTNode methodDeclaration(CSTNode modifiers, CSTNode type, CSTNode identifier)
        throws IOException, SyntaxException {
        CSTNode methodDeclaration = new CSTNode(Token.syntheticMethod());

        methodDeclaration.addChild(modifiers);
        methodDeclaration.addChild(identifier);
        methodDeclaration.addChild(type);

        CSTNode paramsRoot = rootNode(Token.LEFT_PARENTHESIS);

        methodDeclaration.addChild(paramsRoot);

        while (lt() != Token.RIGHT_PARENTHESIS) {
            switch (lt(2)) {
                case (Token.DOT) :
                case (Token.IDENTIFIER) :
                case (Token.LEFT_SQUARE_BRACKET) :
                    {
                        type = datatype();
                        break;
                    }
                default :
                    {
                        type = new CSTNode();
                    }
            }

            CSTNode param = new CSTNode();

            paramsRoot.addChild(param);

            consume(param, Token.IDENTIFIER);

            param.addChild(type);

            if (lt() == Token.COMMA) {
                consume(Token.COMMA);
            }
        }

        consume(Token.RIGHT_PARENTHESIS);

        methodDeclaration.addChild(statementBlock());

        return methodDeclaration;
    }

    protected CSTNode parameterDeclarationList() throws IOException, SyntaxException {
        CSTNode parameterDeclarationList = new CSTNode();

        while (lt() == Token.IDENTIFIER) {
            parameterDeclarationList.addChild(parameterDeclaration());

            if (lt() == Token.COMMA) {
                consume(Token.COMMA);
            }
            else {
                break;
            }
        }

        return parameterDeclarationList;
    }

    protected CSTNode parameterDeclaration() throws IOException, SyntaxException {
        CSTNode parameterDeclaration = null;

        switch (lt(2)) {
            case (Token.IDENTIFIER) :
            case (Token.DOT) :
                {
                    parameterDeclaration = parameterDeclarationWithDatatype();
                    break;
                }
            default :
                {
                    parameterDeclaration = parameterDeclarationWithoutDatatype();
                    break;
                }
        }

        return parameterDeclaration;
    }

    protected CSTNode parameterDeclarationWithDatatype() throws IOException, SyntaxException {
        CSTNode parameterDeclaration = new CSTNode(Token.syntheticParameterDeclaration());

        CSTNode datatype = datatype();

        parameterDeclaration.addChild(datatype);

        consume(parameterDeclaration, Token.IDENTIFIER);

        return parameterDeclaration;
    }

    protected CSTNode parameterDeclarationWithoutDatatype() throws IOException, SyntaxException {
        CSTNode parameterDeclaration = new CSTNode(Token.syntheticParameterDeclaration());

        consume(parameterDeclaration, Token.IDENTIFIER);

        parameterDeclaration.addChild(new CSTNode());

        return parameterDeclaration;
    }

    protected CSTNode datatype() throws IOException, SyntaxException {
        CSTNode datatype = datatypeWithoutArray();

        if (datatype != null) {
            if (lt_bare() == Token.LEFT_SQUARE_BRACKET && lt_bare(2) == Token.RIGHT_SQUARE_BRACKET) {
                CSTNode newRoot = new CSTNode(Token.leftSquareBracket(-1, -1));
                newRoot.addChild(datatype);
                datatype = newRoot;

                //datatype = rootNode( Token.LEFT_SQUARE_BRACKET, datatype );

                consume_bare(datatype, Token.LEFT_SQUARE_BRACKET);
                consume_bare(Token.RIGHT_SQUARE_BRACKET);
            }
        }
        return datatype;
    }

    protected CSTNode datatypeWithoutArray() throws IOException, SyntaxException {
        CSTNode datatype = null;

        switch (lt()) {
            case (Token.KEYWORD_VOID) :
            case (Token.KEYWORD_INT) :
            case (Token.KEYWORD_FLOAT) :
            case (Token.KEYWORD_DOUBLE) :
            case (Token.KEYWORD_CHAR) :
            case (Token.KEYWORD_BYTE) :
            case (Token.KEYWORD_SHORT) :
            case (Token.KEYWORD_LONG) :
            case (Token.KEYWORD_BOOLEAN) :
                {
                    datatype = rootNode(lt());
                    break;
                }
            default :
                {
                    datatype = rootNode(Token.IDENTIFIER);

                    while (lt() == Token.DOT) {
                        CSTNode dot = rootNode(Token.DOT, datatype);
                        consume(dot, Token.IDENTIFIER);

                        datatype = dot;
                    }
                }
        }
        return datatype;
    }

    protected CSTNode statementOrStatementBlock() throws IOException, SyntaxException {
        if (la().getType() == Token.LEFT_CURLY_BRACE) {
            return statementBlock();
        }
        else {
            return statement();
        }
    }

    protected CSTNode statementBlock() throws IOException, SyntaxException {
        CSTNode statementBlock = rootNode(Token.LEFT_CURLY_BRACE);

        statementsUntilRightCurly(statementBlock);

        consume(Token.RIGHT_CURLY_BRACE);

        return statementBlock;
    }

    protected void statementsUntilRightCurly(CSTNode root) throws IOException, SyntaxException {
        while (lt() != Token.RIGHT_CURLY_BRACE) {
            root.addChild(statement());

            if (lt_bare() == Token.RIGHT_CURLY_BRACE) {
                break;
            }
            else {
                optionalSemicolon();

                if (lt_bare() == -1) {
                    throwExpected(new int[] { Token.RIGHT_CURLY_BRACE });
                }
                if (!lastTokenStatementSeparator) {
                    throwExpected(new int[] { Token.NEWLINE, Token.SEMICOLON, Token.RIGHT_CURLY_BRACE });
                }
            }
        }
    }

    protected CSTNode statement() throws IOException, SyntaxException {
        CSTNode statement = null;

        switch (lt()) {
            case (Token.KEYWORD_FOR) :
                {
                    statement = forStatement();
                    break;
                }
            case (Token.KEYWORD_WHILE) :
                {
                    statement = whileStatement();
                    break;
                }
            case (Token.KEYWORD_CONTINUE) :
                {
                    statement = continueStatement();
                    break;
                }
            case (Token.KEYWORD_BREAK) :
                {
                    statement = breakStatement();
                    break;
                }
            case (Token.KEYWORD_IF) :
                {
                    statement = ifStatement();
                    break;
                }
            case (Token.KEYWORD_TRY) :
                {
                    statement = tryStatement();
                    break;
                }
            case (Token.KEYWORD_THROW) :
                {
                    statement = throwStatement();
                    break;
                }
            case (Token.KEYWORD_SYNCHRONIZED) :
                {
                    statement = synchronizedStatement();
                    break;
                }
            case (Token.KEYWORD_SWITCH) :
                {
                    statement = switchStatement();
                    break;
                }
            case (Token.KEYWORD_RETURN) :
                {
                    statement = returnStatement();
                    optionalSemicolon();
                    break;
                }
            case (Token.KEYWORD_ASSERT) :
                {
                    statement = assertStatement();
                    optionalSemicolon();
                    break;
                }
            default :
                {
                    statement = expression();
                    optionalSemicolon();
                }
        }

        return statement;
    }

    protected CSTNode switchStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_SWITCH);
        consume(Token.LEFT_PARENTHESIS);

        statement.addChild(expression());

        consume(Token.RIGHT_PARENTHESIS);

        consume(Token.LEFT_CURLY_BRACE);

        while (lt() == Token.KEYWORD_CASE) {
            CSTNode caseBlock = rootNode(Token.KEYWORD_CASE);

            caseBlock.addChild(expression());

            consume(Token.COLON);

            while (lt() != Token.RIGHT_CURLY_BRACE && lt() != Token.KEYWORD_CASE && lt() != Token.KEYWORD_DEFAULT) {
                caseBlock.addChild(statement());

                if (lt() == Token.RIGHT_CURLY_BRACE) {
                    break;
                }
                else if (lt() == -1) {
                    throwExpected(new int[] { Token.RIGHT_CURLY_BRACE });
                }
            }

            statement.addChild(caseBlock);
        }

        if (lt() == Token.KEYWORD_DEFAULT) {
            CSTNode caseBlock = rootNode(Token.KEYWORD_DEFAULT);

            consume(Token.COLON);

            while (lt() != Token.RIGHT_CURLY_BRACE) {
                caseBlock.addChild(statement());

                if (lt() == Token.RIGHT_CURLY_BRACE) {
                    break;
                }
                else if (lt() == -1) {
                    throwExpected(new int[] { Token.RIGHT_CURLY_BRACE });
                }
            }

            statement.addChild(caseBlock);
        }

        consume(Token.RIGHT_CURLY_BRACE);

        return statement;
    }

    protected CSTNode breakStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_BREAK);

        if (lt() == Token.IDENTIFIER) {
            statement.addChild(rootNode(lt()));
        }

        optionalSemicolon();

        return statement;
    }

    protected CSTNode continueStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_CONTINUE);

        if (lt() == Token.IDENTIFIER) {
            statement.addChild(rootNode(lt()));
        }

        optionalSemicolon();

        return statement;
    }

    protected CSTNode throwStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_THROW);

        statement.addChild(expression());

        return statement;
    }

    protected CSTNode synchronizedStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_SYNCHRONIZED);

        statement.addChild(expression());

        statement.addChild(statementBlock());

        return statement;
    }

    protected CSTNode ifStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_IF);

        consume(Token.LEFT_PARENTHESIS);

        statement.addChild(expression());

        consume(Token.RIGHT_PARENTHESIS);

        statement.addChild(statementOrStatementBlock());

        CSTNode cur = statement;

        while (lt() == Token.KEYWORD_ELSE && lt(2) == Token.KEYWORD_IF) {
            consume(Token.KEYWORD_ELSE);
            CSTNode next = ifStatement();
            cur.addChild(next);
            cur = next;
        }

        if (lt() == Token.KEYWORD_ELSE) {
            CSTNode next = rootNode(Token.KEYWORD_ELSE);
            next.addChild(statementOrStatementBlock());
            cur.addChild(next);
        }

        return statement;
    }

    protected CSTNode tryStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_TRY);

        statement.addChild(statementBlock());

        CSTNode catches = new CSTNode();

        while (lt() == Token.KEYWORD_CATCH) {
            CSTNode catchBlock = rootNode(Token.KEYWORD_CATCH);

            consume(Token.LEFT_PARENTHESIS);

            catchBlock.addChild(datatype());

            consume(catchBlock, Token.IDENTIFIER);

            consume(Token.RIGHT_PARENTHESIS);

            catchBlock.addChild(statementBlock());

            catches.addChild(catchBlock);
        }

        if (lt() == Token.KEYWORD_FINALLY) {
            consume(Token.KEYWORD_FINALLY);
            statement.addChild(statementBlock());
        }
        else {
            statement.addChild(new CSTNode());
        }

        statement.addChild(catches);

        return statement;
    }

    protected CSTNode returnStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_RETURN);

        statement.addChild(expression());

        return statement;
    }

    protected CSTNode whileStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_WHILE);

        consume(Token.LEFT_PARENTHESIS);

        statement.addChild(expression());

        consume(Token.RIGHT_PARENTHESIS);

        statement.addChild(statementOrStatementBlock());

        return statement;
    }

    protected CSTNode forStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_FOR);

        consume(Token.LEFT_PARENTHESIS);

        consume(statement, Token.IDENTIFIER);

        Token potentialIn = consume(Token.IDENTIFIER);

        if (!potentialIn.getText().equals("in")) {
            throw new UnexpectedTokenException(potentialIn, new int[] {
            });
        }

        CSTNode expr = expression();

        statement.addChild(expr);

        consume(Token.RIGHT_PARENTHESIS);

        statement.addChild(statementOrStatementBlock());

        return statement;
    }

    protected CSTNode assertStatement() throws IOException, SyntaxException {
        CSTNode statement = rootNode(Token.KEYWORD_ASSERT);

        statement.addChild(conditionalExpression());

        if (lt() == Token.COLON) {
            consume(Token.COLON);

            statement.addChild(expression());
        }
        else {
            statement.addChild(new CSTNode());
        }

        return statement;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    protected CSTNode expression() throws IOException, SyntaxException {
        optionalNewlines();

        return assignmentExpression();
    }

    protected CSTNode assignmentExpression() throws IOException, SyntaxException {
        CSTNode expr = null;

        if (lt_bare() == Token.IDENTIFIER && lt_bare(2) == Token.IDENTIFIER && lt_bare(3) == Token.EQUAL) {
            // a typed variable declaration
            CSTNode typeExpr = new CSTNode(consume_bare(lt_bare()));
            expr = new CSTNode(consume_bare(lt_bare()));
            expr.addChild(typeExpr);
        }
        else {
            expr = conditionalExpression();
        }

        switch (lt_bare()) {
            case (Token.EQUAL) :
            case (Token.PLUS_EQUAL) :
            case (Token.MINUS_EQUAL) :
            case (Token.DIVIDE_EQUAL) :
            case (Token.MULTIPLY_EQUAL) :
            case (Token.MOD_EQUAL) :
                {
                    expr = rootNode(lt_bare(), expr);
                    optionalNewlines();
                    expr.addChild(conditionalExpression());
                    break;
                }
        }

        return expr;
    }

    protected CSTNode conditionalExpression() throws IOException, SyntaxException {
        CSTNode expr = logicalOrExpression();

        if (lt_bare() == Token.QUESTION) {
            rootNode(Token.QUESTION, expr);
            optionalNewlines();
            expr.addChild(assignmentExpression());

            optionalNewlines();

            consume(Token.COLON);

            optionalNewlines();
            expr.addChild(conditionalExpression());
        }

        return expr;
    }

    protected CSTNode logicalOrExpression() throws IOException, SyntaxException {
        CSTNode expr = logicalAndExpression();

        while (lt_bare() == Token.LOGICAL_OR) {
            expr = rootNode(Token.LOGICAL_OR, expr);
            optionalNewlines();
            expr.addChild(logicalAndExpression());
        }

        return expr;
    }

    protected CSTNode logicalAndExpression() throws IOException, SyntaxException {
        CSTNode expr = equalityExpression();

        while (lt_bare() == Token.LOGICAL_AND) {
            expr = rootNode(Token.LOGICAL_AND, expr);
            optionalNewlines();
            expr.addChild(equalityExpression());
        }

        return expr;
    }

    protected CSTNode equalityExpression() throws IOException, SyntaxException {
        CSTNode expr = relationalExpression();

        switch (lt_bare()) {
            case (Token.COMPARE_EQUAL) :
            case (Token.COMPARE_NOT_EQUAL) :
            case (Token.COMPARE_IDENTICAL) :
                {
                    expr = rootNode(lt_bare(), expr);
                    optionalNewlines();
                    expr.addChild(relationalExpression());
                    break;
                }
        }

        return expr;
    }

    protected CSTNode relationalExpression() throws IOException, SyntaxException {
        CSTNode expr = rangeExpression();

        switch (lt_bare()) {
            case (Token.COMPARE_LESS_THAN) :
            case (Token.COMPARE_LESS_THAN_EQUAL) :
            case (Token.COMPARE_GREATER_THAN) :
            case (Token.COMPARE_GREATER_THAN_EQUAL) :
            case (Token.FIND_REGEX) :
            case (Token.MATCH_REGEX) :
            case (Token.KEYWORD_INSTANCEOF) :
                {
                    expr = rootNode(lt_bare(), expr);
                    optionalNewlines();
                    expr.addChild(rangeExpression());
                    break;
                }
        }

        return expr;
    }

    protected CSTNode rangeExpression() throws IOException, SyntaxException {
        CSTNode expr = additiveExpression();

        if (lt_bare() == Token.DOT_DOT) {
            expr = rootNode(Token.DOT_DOT, expr);
            optionalNewlines();
            expr.addChild(additiveExpression());
        }
        else if (lt_bare() == Token.DOT_DOT_DOT) {
            expr = rootNode(Token.DOT_DOT_DOT, expr);
            optionalNewlines();
            expr.addChild(additiveExpression());
        }
        return expr;
    }

    protected CSTNode additiveExpression() throws IOException, SyntaxException {
        CSTNode expr = multiplicativeExpression();

        LOOP : while (true) {
            SWITCH : switch (lt_bare()) {
                case (Token.PLUS) :
                case (Token.MINUS) :
                case (Token.LEFT_SHIFT) :
                case (Token.RIGHT_SHIFT) :
                    {
                        expr = rootNode(lt_bare(), expr);
                        optionalNewlines();
                        expr.addChild(multiplicativeExpression());
                        break SWITCH;
                    }
                default :
                    {
                        break LOOP;
                    }
            }
        }

        return expr;
    }

    protected CSTNode multiplicativeExpression() throws IOException, SyntaxException {
        CSTNode expr = unaryExpression();

        LOOP : while (true) {
            SWITCH : switch (lt_bare()) {
                case (Token.MULTIPLY) :
                case (Token.DIVIDE) :
                case (Token.MOD) :
                case (Token.COMPARE_TO) :
                    {
                        expr = rootNode(lt_bare(), expr);
                        optionalNewlines();
                        expr.addChild(unaryExpression());
                        break SWITCH;
                    }
                default :
                    {
                        break LOOP;
                    }
            }
        }

        return expr;
    }

    protected CSTNode unaryExpression() throws IOException, SyntaxException {
        CSTNode expr = null;

        switch (lt_bare()) {
            case (Token.MINUS) :
            case (Token.NOT) :
            case (Token.PLUS) :
                {
                    expr = rootNode(lt_bare());
                    expr.addChild(postfixExpression());
                    break;
                }
            case (Token.PLUS_PLUS) :
            case (Token.MINUS_MINUS) :
                {
                    expr = new CSTNode(Token.syntheticPrefix());
                    CSTNode prefixExpr = rootNode(lt_bare());
                    expr.addChild(prefixExpr);
                    prefixExpr.addChild(primaryExpression());
                    break;
                }
            default :
                {
                    expr = postfixExpression();
                    break;
                }
        }

        return expr;
    }

    protected CSTNode postfixExpression() throws IOException, SyntaxException {
        CSTNode expr = primaryExpression();

        Token laToken = la();
        if (laToken != null) {
            switch (laToken.getType()) {
                case (Token.PLUS_PLUS) :
                case (Token.MINUS_MINUS) :
                    {
                        CSTNode primaryExpr = expr;
                        expr = new CSTNode(Token.syntheticPostfix());
                        expr.addChild(rootNode(lt_bare(), primaryExpr));
                    }
            }
        }

        return expr;
    }

    protected CSTNode primaryExpression() throws IOException, SyntaxException {
        CSTNode expr = null;
        CSTNode identifier = null;

        PREFIX_SWITCH : switch (lt_bare()) {
            case (Token.KEYWORD_TRUE) :
            case (Token.KEYWORD_FALSE) :
            case (Token.KEYWORD_NULL) :
                {
                    expr = rootNode(lt_bare());
                    break PREFIX_SWITCH;
                }
            case (Token.KEYWORD_NEW) :
                {
                    expr = newExpression();
                    break PREFIX_SWITCH;
                }
            case (Token.LEFT_PARENTHESIS) :
                {
                    expr = parentheticalExpression();
                    break PREFIX_SWITCH;
                }
            case (Token.INTEGER_NUMBER) :
            case (Token.FLOAT_NUMBER) :
            case (Token.SINGLE_QUOTE_STRING) :
                {
                    expr = rootNode(lt_bare());
                    break PREFIX_SWITCH;
                }
            case (Token.DOUBLE_QUOTE_STRING) :
                {
                    expr = doubleQuotedString();
                    break PREFIX_SWITCH;
                }
            case (Token.LEFT_SQUARE_BRACKET) :
                {
                    expr = listOrMapExpression();
                    break PREFIX_SWITCH;
                }
            case (Token.LEFT_CURLY_BRACE) :
                {
                    expr = closureExpression();
                    break PREFIX_SWITCH;
                }
            case (Token.KEYWORD_THIS) :
                {
                    expr = thisExpression();
                    identifier = rootNode(lt_bare());
                    break PREFIX_SWITCH;
                }
            case (Token.KEYWORD_SUPER) :
                {
                    expr = new CSTNode(Token.keyword(-1, -1, "super"));
                    identifier = rootNode(lt_bare());
                    break PREFIX_SWITCH;
                }
            case (Token.IDENTIFIER) :
                {
                    //expr       = identifier;
                    //expr       = new CSTNode( Token.keyword( -1,
                    //                                        -1,
                    //                                        "this" ) );
                    identifier = rootNode(lt_bare());
                    expr = identifier;

                    break PREFIX_SWITCH;
                }
            case (Token.PATTERN_REGEX) :
                {
                    expr = regexPattern();
                    break PREFIX_SWITCH;
                }
            default :
                {
                    throwExpected(new int[] {
                    });
                }
        }

        if (identifier != null) {
            if (lt_bare() == Token.LEFT_PARENTHESIS || lt_bare() == Token.LEFT_CURLY_BRACE) {
                if (expr == identifier) {
                    CSTNode replacementExpr = new CSTNode();
                    CSTNode resultExpr = sugaryMethodCallExpression(replacementExpr, identifier, null);
                    if (resultExpr != replacementExpr) {
                        expr = resultExpr;
                    }
                }
                else {
                    expr = sugaryMethodCallExpression(expr, identifier, null);
                }
            }
            else {
                CSTNode methodCall = tryParseMethodCallWithoutParenthesis(thisExpression(), identifier);
                if (methodCall != null) {
                    expr = methodCall;
                }
            }
        }

        while (lt_bare() == Token.LEFT_SQUARE_BRACKET || lookAheadForMethodCall()) {
            if (lt_bare() == Token.LEFT_SQUARE_BRACKET) {
                expr = subscriptExpression(expr);
            }
            else {
                expr = methodCallOrPropertyExpression(expr);
            }
        }

        return expr;
    }

    protected CSTNode thisExpression() {
        return new CSTNode(Token.keyword(-1, -1, "this"));
    }

    protected CSTNode subscriptExpression(CSTNode expr) throws SyntaxException, IOException {
        expr = rootNode_bare(lt_bare(), expr);

        optionalNewlines();
        CSTNode rangeExpr = rangeExpression();

        // lets support the list notation inside subscript operators
        if (lt_bare() != Token.COMMA) {
            expr.addChild(rangeExpr);
        }
        else {
            consume_bare(Token.COMMA);
            CSTNode listExpr = new CSTNode(Token.syntheticList());
            expr.addChild(listExpr);
            listExpr.addChild(rangeExpr);

            while (true) {
                optionalNewlines();
                listExpr.addChild(rangeExpression());
                if (lt_bare() == Token.COMMA) {
                    consume_bare(Token.COMMA);
                }
                else {
                    break;
                }
            }
        }
        optionalNewlines();
        consume(Token.RIGHT_SQUARE_BRACKET);
        return expr;
    }

    protected CSTNode methodCallOrPropertyExpression(CSTNode expr) throws SyntaxException, IOException {
        CSTNode dotExpr = rootNode_bare(lt_bare());
        CSTNode identifier = rootNode_bare(lt_bare());

        switch (lt_bare()) {
            case (Token.LEFT_PARENTHESIS) :
            case (Token.LEFT_CURLY_BRACE) :
                {
                    expr = sugaryMethodCallExpression(expr, identifier, dotExpr);
                    break;
                }
            default :
                {
                    // lets try parse a method call
                    CSTNode methodCall = tryParseMethodCallWithoutParenthesis(expr, identifier);
                    if (methodCall != null) {
                        expr = methodCall;
                    }
                    else {
                        dotExpr.addChild(expr);
                        dotExpr.addChild(identifier);
                        expr = dotExpr;
                    }
                    break;
                }
        }

        return expr;
    }

    protected CSTNode sugaryMethodCallExpression(CSTNode expr, CSTNode identifier, CSTNode dotExpr)
        throws IOException, SyntaxException {
        CSTNode methodExpr = null;
        CSTNode paramList = null;

        if (lt_bare() == Token.LEFT_PARENTHESIS) {
            methodExpr = rootNode_bare(Token.LEFT_PARENTHESIS);
            optionalNewlines();
            methodExpr.addChild(expr);
            methodExpr.addChild(identifier);
            paramList = parameterList(Token.RIGHT_PARENTHESIS);
            methodExpr.addChild(paramList);
            optionalNewlines();
            consume_bare(Token.RIGHT_PARENTHESIS);
        }

        if (lt_bare() == Token.LEFT_CURLY_BRACE) {
            if (methodExpr == null) {
                methodExpr = new CSTNode(Token.leftParenthesis(-1, -1));
                methodExpr.addChild(expr);
                methodExpr.addChild(identifier);
                paramList = parameterList(Token.LEFT_CURLY_BRACE);
                methodExpr.addChild(paramList);
            }

            paramList.addChild(closureExpression());
        }

        if (methodExpr != null) {
            expr = methodExpr;
            if (dotExpr != null) {
                expr.addChild(dotExpr);
            }
        }

        return expr;
    }

    protected CSTNode tryParseMethodCallWithoutParenthesis(CSTNode expr, CSTNode identifier)
        throws SyntaxException, IOException {
        switch (lt_bare()) {
            case Token.IDENTIFIER :
            case Token.DOUBLE_QUOTE_STRING :
            case Token.SINGLE_QUOTE_STRING :
            case Token.FLOAT_NUMBER :
            case Token.INTEGER_NUMBER :
            case Token.KEYWORD_NEW :
                // lets try parse a method call
                getTokenStream().checkpoint();
                try {
                    return methodCallWithoutParenthesis(expr, identifier);
                }
                catch (Exception e) {
                    getTokenStream().restore();
                }
        }
        return null;
    }

    protected CSTNode methodCallWithoutParenthesis(CSTNode expr, CSTNode identifier)
        throws SyntaxException, IOException {
        CSTNode methodExpr = new CSTNode(Token.leftParenthesis(-1, -1));
        methodExpr.addChild(expr);
        methodExpr.addChild(identifier);

        CSTNode parameterList = new CSTNode(Token.syntheticList());

        parameterList.addChild(expression());

        while (lt_bare() == Token.COMMA) {
            consume_bare(lt_bare());
            optionalNewlines();
            parameterList.addChild(expression());
        }

        methodExpr.addChild(parameterList);
        return methodExpr;
    }

    protected boolean lookAheadForMethodCall() throws IOException, SyntaxException {
        return (lt_bare() == Token.DOT || lt_bare() == Token.NAVIGATE)
            && (isIdentifier(lt(2)));
    }

    protected CSTNode regexPattern() throws IOException, SyntaxException {
        Token token = consume(Token.PATTERN_REGEX);
        CSTNode expr = new CSTNode(token);
        CSTNode regexString = doubleQuotedString();
        expr.addChild(regexString);
        return expr;
    }

    protected CSTNode doubleQuotedString() throws IOException, SyntaxException {
        Token token = consume(Token.DOUBLE_QUOTE_STRING);
        String text = token.getText();

        CSTNode expr = new CSTNode(token);

        int textStart = 0;
        int cur = 0;
        int len = text.length();

        while (cur < len) {
            int exprStart = text.indexOf("${", cur);

            if (exprStart < 0) {
                break;
            }

            if (exprStart > 0) {
                if (text.charAt(exprStart - 1) == '$') {
                    StringBuffer buf = new StringBuffer(text);
                    buf.replace(exprStart - 1, exprStart, "");
                    text = buf.toString();
                    cur = exprStart + 1;
                    continue;
                }
            }

            if (exprStart != textStart) {
                expr.addChild(
                    new CSTNode(
                        Token.singleQuoteString(
                            token.getStartLine(),
                            token.getStartColumn() + cur + 1,
                            text.substring(textStart, exprStart))));
            }

            int exprEnd = text.indexOf("}", exprStart);

            String exprText = text.substring(exprStart + 2, exprEnd);

            StringCharStream exprStream = new StringCharStream(exprText);

            Lexer lexer = new Lexer(exprStream);
            Parser parser = new Parser(new LexerTokenStream(lexer));

            CSTNode embeddedExpr = parser.expression();

            expr.addChild(embeddedExpr);

            cur = exprEnd + 1;
            textStart = cur;
        }

        if (textStart < len) {
            expr.addChild(
                new CSTNode(
                    Token.singleQuoteString(
                        token.getStartLine(),
                        token.getStartColumn() + textStart + 1,
                        text.substring(textStart))));
        }

        return expr;

    }

    protected CSTNode parentheticalExpression() throws IOException, SyntaxException {
        consume(Token.LEFT_PARENTHESIS);

        if (lt_bare() == Token.IDENTIFIER && lt_bare(2) == Token.RIGHT_PARENTHESIS) {
            // we could be a cast
            boolean valid = true;
            switch (lt_bare(3)) {
                case Token.SEMICOLON :
                case Token.NEWLINE :
                case Token.RIGHT_CURLY_BRACE :
                case -1 :
                    valid = false;
            }
            if (valid) {
                // lets assume we're a cast expression
                CSTNode castExpr = new CSTNode(Token.syntheticCast());
                castExpr.addChild(new CSTNode(consume_bare(lt_bare())));
                consume_bare(lt_bare());
                castExpr.addChild(expression());
                return castExpr;
            }
        }

        CSTNode expr = expression();

        consume(Token.RIGHT_PARENTHESIS);

        return expr;
    }

    protected CSTNode parameterList(int endOfListDemarc) throws IOException, SyntaxException {
        if (isIdentifier(lt_bare()) && lt_bare(2) == Token.COLON) {
            return namedParameterList(endOfListDemarc);
        }

        CSTNode parameterList = new CSTNode(Token.syntheticList());

        while (lt_bare() != endOfListDemarc) {
            parameterList.addChild(expression());

            if (lt_bare() == Token.COMMA) {
                consume_bare(Token.COMMA);
                optionalNewlines();
            }
            else {
                break;
            }
        }

        return parameterList;
    }
    
    protected boolean isIdentifier(int type) {
        for (int i = 0; i < identifierTokens.length; i++ ) {
            if (type == identifierTokens[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests that the next token is an identifier
     */
    protected void expectIdentifier() throws SyntaxException, IOException {
        if (! isIdentifier(lt_bare())) {
            throwExpected(identifierTokens);
        }
       }

    protected CSTNode namedParameterList(int endOfListDemarc) throws IOException, SyntaxException {
        CSTNode parameterList = new CSTNode(Token.syntheticList());

        while (lt() != endOfListDemarc) {
            expectIdentifier();
            CSTNode name = rootNode(lt_bare());

            CSTNode namedParam = rootNode_bare(Token.COLON, name);

            namedParam.addChild(expression());

            parameterList.addChild(namedParam);

            if (lt_bare() == Token.COMMA) {
                consume_bare(Token.COMMA);
                optionalNewlines();
            }
            else {
                break;
            }
        }

        return parameterList;
    }

    protected CSTNode newExpression() throws IOException, SyntaxException {
        CSTNode expr = rootNode(Token.KEYWORD_NEW);

        expr.addChild(datatypeWithoutArray());

        /*
        consume( Token.LEFT_PARENTHESIS );
        
        expr.addChild( parameterList( Token.RIGHT_PARENTHESIS ) );
        
        consume( Token.RIGHT_PARENTHESIS );
        */

        if (lt_bare() == Token.LEFT_PARENTHESIS) {
            consume_bare(Token.LEFT_PARENTHESIS);

            expr.addChild(parameterList(Token.RIGHT_PARENTHESIS));

            consume(Token.RIGHT_PARENTHESIS);
        }
        else if (lt_bare() == Token.LEFT_CURLY_BRACE) {
            consume_bare(Token.LEFT_CURLY_BRACE);

            CSTNode paramList = parameterList(Token.RIGHT_CURLY_BRACE);
            expr.addChild(paramList);
            paramList.addChild(closureExpression());
        }
        else if (lt_bare() == Token.LEFT_SQUARE_BRACKET) {
            expr.addChild(new CSTNode(consume_bare(Token.LEFT_SQUARE_BRACKET)));

            if (lt_bare() == Token.RIGHT_SQUARE_BRACKET) {
                // no size so must use a size expression
                consume_bare(Token.RIGHT_SQUARE_BRACKET);
                expr.addChild(new CSTNode(consume_bare(Token.LEFT_CURLY_BRACE)));
                CSTNode paramList = parameterList(Token.RIGHT_CURLY_BRACE);
                consume(Token.RIGHT_CURLY_BRACE);

                expr.addChild(paramList);
            }
            else {
                expr.addChild(expression());
                consume_bare(Token.RIGHT_SQUARE_BRACKET);
            }
        }
        return expr;
    }

    protected CSTNode closureExpression() throws IOException, SyntaxException {
        CSTNode expr = rootNode(Token.LEFT_CURLY_BRACE);

        boolean pipeRequired = false;

        // { statement();
        // { a |
        // { a, b |
        // { A a |
        // { A a, B b |

        if (lt(2) == Token.PIPE || lt(2) == Token.COMMA || lt(3) == Token.PIPE || lt(3) == Token.COMMA) {
            expr.addChild(parameterDeclarationList());
            pipeRequired = true;
        }
        else {
            expr.addChild(new CSTNode());
        }

        if (pipeRequired || lt() == Token.PIPE) {
            consume(Token.PIPE);
        }

        CSTNode block = new CSTNode();

        statementsUntilRightCurly(block);

        consume(Token.RIGHT_CURLY_BRACE);

        expr.addChild(block);

        return expr;
    }

    protected CSTNode listOrMapExpression() throws IOException, SyntaxException {
        CSTNode expr = null;

        consume(Token.LEFT_SQUARE_BRACKET);

        if (lt() == Token.COLON) {
            // it is an empty map
            consume(Token.COLON);
            expr = new CSTNode(Token.syntheticMap());
        }
        else if (lt() == Token.RIGHT_SQUARE_BRACKET) {
            // it is an empty list
            expr = new CSTNode(Token.syntheticList());
        }
        else {
            CSTNode firstExpr = expression();

            if (lt() == Token.COLON) {
                expr = mapExpression(firstExpr);
            }
            else {
                expr = listExpression(firstExpr);
            }
        }

        consume(Token.RIGHT_SQUARE_BRACKET);

        return expr;
    }

    protected CSTNode mapExpression(CSTNode key) throws IOException, SyntaxException {
        CSTNode expr = new CSTNode(Token.syntheticMap());

        CSTNode entry = rootNode(Token.COLON, key);

        CSTNode value = expression();

        entry.addChild(value);

        expr.addChild(entry);

        while (lt() == Token.COMMA) {
            consume(Token.COMMA);

            key = expression();

            entry = rootNode(Token.COLON, key);

            entry.addChild(expression());

            expr.addChild(entry);
        }

        return expr;
    }

    protected CSTNode listExpression(CSTNode entry) throws IOException, SyntaxException {
        CSTNode expr = new CSTNode(Token.syntheticList());

        expr.addChild(entry);

        while (lt() == Token.COMMA) {
            consume(Token.COMMA);

            entry = expression();

            expr.addChild(entry);
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

    protected CSTNode argumentList() throws IOException, SyntaxException {
        CSTNode argumentList = new CSTNode();

        while (lt() != Token.RIGHT_PARENTHESIS) {
            argumentList.addChild(expression());

            if (lt() == Token.COMMA) {
                consume(Token.COMMA);
            }
            else {
                break;
            }
        }

        return argumentList;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    protected static boolean isModifier(int type) {
        switch (type) {
            case (Token.KEYWORD_PUBLIC) :
            case (Token.KEYWORD_PROTECTED) :
            case (Token.KEYWORD_PRIVATE) :
            case (Token.KEYWORD_STATIC) :
            case (Token.KEYWORD_FINAL) :
            case (Token.KEYWORD_SYNCHRONIZED) :
                {
                    return true;
                }
            default :
                {
                    return false;
                }
        }
    }

    protected void throwExpected(int[] expectedTypes) throws IOException, SyntaxException {
        throw new UnexpectedTokenException(la(), expectedTypes);
    }

    protected void consumeUntil(int type) throws IOException, SyntaxException {
        while (lt() != -1) {
            consume(lt());

            if (lt() == type) {
                consume(lt());
                break;
            }
        }
    }

    protected Token la() throws IOException, SyntaxException {
        return la(1);
    }

    protected int lt() throws IOException, SyntaxException {
        Token token = la();

        if (token == null) {
            return -1;
        }

        return token.getType();
    }

    protected Token la(int k) throws IOException, SyntaxException {
        Token token = null;
        for (int pivot = 1, count = 0; count < k; pivot++) {
            token = getTokenStream().la(pivot);
            if (token == null || token.getType() != Token.NEWLINE) {
                count++;
            }
        }
        return token;
    }

    protected int lt(int k) throws IOException, SyntaxException {
        Token token = la(k);

        if (token == null) {
            return -1;
        }

        return token.getType();
    }

    protected Token consume(int type) throws IOException, SyntaxException {
        if (lt() != type) {
            throw new UnexpectedTokenException(la(), type);
        }

        while (true) {
            Token token = getTokenStream().la();
            if (token == null || token.getType() != Token.NEWLINE) {
                break;
            }
            getTokenStream().consume(Token.NEWLINE);
        }

        return getTokenStream().consume(type);
    }

    protected void consume(CSTNode root, int type) throws IOException, SyntaxException {
        root.addChild(new CSTNode(consume(type)));
    }

    // bare versions of the token methods which don't ignore newlines
    protected void consumeUntil_bare(int type) throws IOException, SyntaxException {
        while (lt_bare() != -1) {
            consume_bare(lt_bare());

            if (lt_bare() == type) {
                consume(lt_bare());
                break;
            }
        }
    }

    protected Token la_bare() throws IOException, SyntaxException {
        return la_bare(1);
    }

    protected int lt_bare() throws IOException, SyntaxException {
        Token token = la_bare();

        if (token == null) {
            return -1;
        }

        return token.getType();
    }

    protected Token la_bare(int k) throws IOException, SyntaxException {
        return getTokenStream().la(k);
    }

    protected int lt_bare(int k) throws IOException, SyntaxException {
        Token token = la_bare(k);

        if (token == null) {
            return -1;
        }

        return token.getType();
    }

    protected Token consume_bare(int type) throws IOException, SyntaxException {
        if (lt_bare() != type) {
            throw new UnexpectedTokenException(la_bare(), type);
        }

        lastTokenStatementSeparator = type == Token.SEMICOLON || type == Token.NEWLINE;

        return getTokenStream().consume(type);
    }

    protected void consume_bare(CSTNode root, int type) throws IOException, SyntaxException {
        root.addChild(new CSTNode(consume_bare(type)));
    }

    protected CSTNode rootNode(int type) throws IOException, SyntaxException {
        return new CSTNode(consume(type));
    }

    protected CSTNode rootNode_bare(int type) throws IOException, SyntaxException {
        return new CSTNode(consume_bare(type));
    }

    protected CSTNode rootNode(int type, CSTNode child) throws IOException, SyntaxException {
        CSTNode root = new CSTNode(consume(type));
        root.addChild(child);
        return root;
    }

    protected CSTNode rootNode_bare(int type, CSTNode child) throws IOException, SyntaxException {
        CSTNode root = new CSTNode(consume_bare(type));
        root.addChild(child);
        return root;
    }
}
