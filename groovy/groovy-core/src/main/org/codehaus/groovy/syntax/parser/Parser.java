/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.syntax.parser;

import java.util.Iterator;

import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenStream;
import org.codehaus.groovy.syntax.lexer.Lexer;
import org.codehaus.groovy.syntax.lexer.LexerTokenStream;
import org.codehaus.groovy.syntax.lexer.StringCharStream;
import org.codehaus.groovy.tools.ExceptionCollector;

/**
 *  Reads the source text and produces a hierarchy of Concrete Syntax Trees
 *  (CSTs).  Exceptions are collected during processing, and parsing will
 *  continue for while possible, in order to report as many problems as 
 *  possible.   <code>compilationUnit()</code> is the primary entry point.
 */

public class Parser {

    private TokenStream tokenStream = null;
    private ExceptionCollector collector = null;



  //---------------------------------------------------------------------------
  // CONSTRUCTION AND DATA ACCESS

   /**
    *  Sets the <code>Parser</code> to process a <code>TokenStream</code>.
    */

    public Parser(TokenStream tokenStream) {
        this.tokenStream = tokenStream;
        this.collector = new ExceptionCollector(1);
    }


   /**
    *  Sets the <code>Parser</code> to process a <code>TokenStream</code>.
    *  Exceptions will be collected in the specified collector.
    */

    public Parser(TokenStream tokenStream, ExceptionCollector collector) {
        this.tokenStream = tokenStream;
        this.collector = collector;
    }


   /**
    *  Returns the <code>TokenStream</code> being parsed.
    */

    public TokenStream getTokenStream() {
        return this.tokenStream;
    }




  //---------------------------------------------------------------------------
  // PRODUCTION SUPPORT


   /**
    *  Eats any optional newlines.
    */

    public void optionalNewlines() throws ReadException, SyntaxException {
        while (lt_bare() == Token.NEWLINE) {
            consume_bare(lt_bare());
        }
    }



   /**
    *  Eats a required end-of-statement (semicolon or newline) from
    *  the stream.  Throws an <code>UnexpectedTokenException</code> 
    *  if anything else is found.
    */

    public void endOfStatement( boolean allowRightCurlyBrace ) throws ReadException, SyntaxException {
        int lt_bare = lt_bare();

        if (lt_bare == Token.SEMICOLON || lt_bare == Token.NEWLINE) {
            consume_bare(lt_bare);
        }
        else {
            if( allowRightCurlyBrace ) {
                if( lt_bare != -1 && lt_bare != Token.RIGHT_CURLY_BRACE ) {
                    throwExpected(new int[] { Token.SEMICOLON, Token.NEWLINE, Token.RIGHT_CURLY_BRACE });
                }
            }
            else {
                if( lt_bare != -1 ) {
                    throwExpected(new int[] { Token.SEMICOLON, Token.NEWLINE });
                }
            }
        }
    }


    
   /**
    *  A synonym for <code>endOfStatement( true )</code>.
    */

    public void endOfStatement() throws ReadException, SyntaxException {
        endOfStatement( true );
    }



   /**
    *  Attempts to recover from an error by discarding input until a
    *  known token is found.  It further guarantees that /at least/ 
    *  one token will be eaten.
    */

    public void recover( int[] safe, boolean useBare ) throws ReadException, SyntaxException {
        Token leading = (useBare ? la_bare() : la());

        while( (useBare ? lt_bare() : lt()) != -1 && !(useBare ? la_bare() : la()).isA(safe) ) {
            if( useBare ) { consume_bare(lt_bare()); } else { consume(lt()); }
        }

        if( (useBare ? la_bare() : la()) == leading ) {

            if( useBare ) { consume_bare(lt_bare()); } else { consume(lt()); }
        }
    }


 
   /**
    *  A synonym for <code>recover( safe, false )</code>.
    */

    public void recover( int[] safe ) throws ReadException, SyntaxException {
        recover( safe, false );
    }



   /**
    *  A synonym for <code>recover( STATEMENT_TERMNINATORS, true )</code>.
    */

    public void recover( ) throws ReadException, SyntaxException {
        recover( STATEMENT_TERMINATORS, true );
    }



   /**
    *  Various generally useful type sets.
    */

    protected static final int[] TYPE_DEFINERS         = new int[] { Token.KEYWORD_CLASS, Token.KEYWORD_INTERFACE };
    protected static final int[] STATEMENT_TERMINATORS = new int[] { Token.SEMICOLON, Token.NEWLINE, Token.RIGHT_CURLY_BRACE };

    protected static final int[] GENERAL_CLAUSE_TERMINATOR = new int[] { Token.RIGHT_PARENTHESIS };
    protected static final int[] PARAMETER_TERMINATORS     = new int[] { Token.RIGHT_PARENTHESIS, Token.COMMA };
    protected static final int[] ARRAY_ITEM_TERMINATORS    = new int[] { Token.RIGHT_SQUARE_BRACKET, Token.COMMA };




  //---------------------------------------------------------------------------
  // PRODUCTIONS


   /**
    *  The primary file-level parsing entry point.  The returned CST
    *  represents the content in a single class file.  Collects most
    *  exceptions and attempts to continue.
    *  <p>
    *  Grammar: <pre>
    *     compilationUnit = [packageStatement]
    *                       (usingStatement)*
    *                       (topLevelStatement)*
    *                       <eof>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     compilationUnit = { <null> package imports (topLevelStatement)* }
    *     
    *     package           see packageDeclaration()
    *     imports           see importStatement()
    *     topLevelStatement see topLevelStatement()
    *  </pre>
    * 
    */

    public CSTNode compilationUnit() throws ReadException, SyntaxException, ExceptionCollector, ExceptionCollector {

        CSTNode compilationUnit = new CSTNode();

        //
        // First up, the package declaration

        CSTNode packageDeclaration = null;

        if (lt() == Token.KEYWORD_PACKAGE) {

            try {
                packageDeclaration = packageDeclaration();
            }

            catch (SyntaxException e) {
                collector.add(e);
                recover();
            }
        }

        if (packageDeclaration == null) {
            packageDeclaration = new CSTNode(Token.keyword(-1, -1, "package"));
            packageDeclaration.addChild(new CSTNode());
        }

        compilationUnit.addChild(packageDeclaration);

        //
        // Next, handle import statements

        CSTNode imports = new CSTNode();
        compilationUnit.addChild(imports);

        while (lt() == Token.KEYWORD_IMPORT) {

            try {
                imports.addChild(importStatement());
            }

            catch (SyntaxException e) {
                collector.add(e);
                recover();
            }
        }

        //
        // With that taken care of, process everything else.  

        while (lt() != -1) {

            try {
                compilationUnit.addChild(topLevelStatement());
            }

            catch (SyntaxException e) {
                collector.add(e);
                recover();
            }
        }

        return compilationUnit;
    }



   /**
    *  Processes a package declaration.  Called by <code>compilationUnit()</code>.
    *  <p>
    *  Grammar: <pre>
    *     packageDeclaration = "package" <identifier> ("." <identifier>)* <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     package = { "package" classes }
    *     classes = { "." classes class } | class
    *     class   = { <identifier> }
    *  </pre>
    */

    public CSTNode packageDeclaration() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode packageDeclaration = rootNode(Token.KEYWORD_PACKAGE);

        CSTNode cur = rootNode(Token.IDENTIFIER);

        while (lt() == Token.DOT) {
            CSTNode dot = rootNode(Token.DOT, cur);

            consume(dot, Token.IDENTIFIER);

            cur = dot;
        }

        endOfStatement( false );

        packageDeclaration.addChild(cur);
        return packageDeclaration;
    }



   /**
    *  Processes an import statement.  Called by <code>compilationUnit()</code>.
    *  <p>
    *  Grammar: <pre>
    *     importStatement = "import" <identifier> ("."<identifier>)* ["as" <identifier] <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     import  = { "import" classes as }
    *     classes = { "." classes class } | class
    *     class   = { <identifier> }
    *     as      = { "as" class } | {}
    *  </pre>
    */

    public CSTNode importStatement() throws ReadException, SyntaxException, ExceptionCollector {
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

        endOfStatement( false );

        return importStatement;
    }



   /**
    *  Processes a top level statement (classes, interfaces, unattached methods, and 
    *  unattached code).  Called by <code>compilationUnit()</code>.
    *  <p>
    *  Grammar: <pre>
    *     topLevelStatement
    *       = methodDeclaration 
    *       | typeDeclaration
    *       | statement
    *     
    *     typeDeclaration = classDefinition | interfaceDeclaration
    *  </pre>
    *  <p>
    *  Recognition: <pre>
    *     "def"                    => methodDeclaration
    *     modifierList "class"     => classDeclaration
    *     modifierList "interface" => interfaceDeclaration
    *     modifierList             => <error>
    *     *                        => statement
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     see methodDeclaration()
    *     see classDeclaration()
    *     see interfaceDeclaration()
    *     see statement()
    *  </pre>
    */

    public CSTNode topLevelStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode result = null;

        //
        // If it starts "def", it's a method declaration.  Methods
        // declared this way cannot be abstract.  Note that "def" 
        // is required because the return type is not, and it would
        // be very hard to tell the difference between a function 
        // def and a function invokation with clusure...

        if (lt() == Token.KEYWORD_DEF) {
            consume(lt());

            CSTNode modifiers = modifierList(false, false);
            CSTNode type = optionalDatatype(false, true);
            CSTNode identifier = nameDeclaration(false);

            result = methodDeclaration(modifiers, type, identifier, false);
        }

        //
        // If it starts with a modifier, "class", or "interface", 
        // it's a type declaration.

        else if( lt() != -1 && (la().isModifier() || lt() == Token.KEYWORD_CLASS || lt() == Token.KEYWORD_INTERFACE) ) {

            CSTNode modifiers = modifierList(true, true);

            switch (lt()) {
                case Token.KEYWORD_CLASS :
                    {
                        result = classDeclaration(modifiers);
                        break;
                    }

                case Token.KEYWORD_INTERFACE :
                    {
                        result = interfaceDeclaration(modifiers);
                        break;
                    }

                default :
                    {
                        throwExpected(new int[] { Token.KEYWORD_CLASS, Token.KEYWORD_INTERFACE });
                        break;
                    }
            }
        }

        //
        // Otherwise, it's a statement.

        else {
            result = statement();
        }

        return result;
    }



   /**
    *  A synomym for <code>topLevelStatement()</code>.
    */

    public CSTNode typeDeclaration() throws ReadException, SyntaxException, ExceptionCollector {
        return topLevelStatement();
    }



   /**
    *  Processes the modifiers list that can appear on top- and class-level
    *  method and class-level variable names (public, private, abstract, etc.).
    *  <p>
    *  Grammar: <pre>
    *     modifierList = <modifier>*
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     modifiers = { <null> {<modifier>}* }
    *  </pre>
    */

    public CSTNode modifierList(boolean allowStatic, boolean allowAbstract)
        throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode modifiers = new CSTNode();

        while( lt() != -1 && la().isModifier() ) {
            if (lt() == Token.KEYWORD_ABSTRACT && !allowAbstract) {
                collector.add(new ParserException("keyword 'abstract' not valid in this setting", la()));
            }
            else if (lt() == Token.KEYWORD_STATIC && !allowStatic) {
                collector.add(new ParserException("keyword 'static' not valid in this setting", la()));
            }
            consume(modifiers, lt());
        }

        return modifiers;
    }



   /**
    *  End markers for use by classDeclaration(), interfaceDeclaration(),
    *  and methodDeclaration().
    */

    protected static final int[] EXTENDS_CLAUSE_TERMINATORS    = new int[] { Token.KEYWORD_IMPLEMENTS, Token.LEFT_CURLY_BRACE };
    protected static final int[] IMPLEMENTS_CLAUSE_TERMINATORS = new int[] { Token.LEFT_CURLY_BRACE };
    protected static final int[] THROWS_CLAUSE_TERMINATORS     = new int[] { Token.LEFT_CURLY_BRACE };



   /**
    *  Processes a class declaration.  Caller has already processed the declaration
    *  modifiers, and passes them in.
    *  <p>
    *  Grammar: <pre>
    *     classDeclaration = <modifier>* "class" <identifier>
    *                        ["extends" datatype]
    *                        ["implements" datatype (, datatype)*]
    *                        typeBody
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     class      = { "class" modifiers {<identifier>} extends implements body }
    *     extends    = { "extends"    datatype  } | {}
    *     implements = { "implements" datatype* } | {}
    *
    *     modifiers see modifierList()
    *     datatype  see datatype()
    *     body      see typeBody()
    *  </pre>
    */

    public CSTNode classDeclaration(CSTNode modifiers) throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode classDeclaration = rootNode(Token.KEYWORD_CLASS);
        classDeclaration.addChild(modifiers);
        consume(classDeclaration, Token.IDENTIFIER);

        //
        // Process any extends clause.

        try {
            CSTNode extendsNode = typeList(Token.KEYWORD_EXTENDS, EXTENDS_CLAUSE_TERMINATORS, true, 1);
            classDeclaration.addChild(extendsNode);
        }
        catch (SyntaxException e) {
            collector.add(e);
            classDeclaration.addChild(new CSTNode());
        }

        //
        // Process any implements clause.

        try {
            CSTNode implementsNode = typeList(Token.KEYWORD_IMPLEMENTS, IMPLEMENTS_CLAUSE_TERMINATORS, true, 0);
            classDeclaration.addChild(implementsNode);
        }
        catch (SyntaxException e) {
            collector.add(e);
            classDeclaration.addChild(new CSTNode());
        }

        //
        // Process the declaration body.  We currently ignore the abstract keyword.

        classDeclaration.addChild(typeBody(true, true, false));

        return classDeclaration;
    }



   /**
    *  Processes a interface declaration.  Caller has already processed the 
    *  declaration modifiers, and passes them in.
    *  <p>
    *  Grammar: <pre>
    *     interfaceDeclaration = <modifier>* "interface" <identifier>
    *                            ["extends" typeList]
    *                            typeBody
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     class      = { "interface" modifiers {<identifier>} {} extends body }
    *     extends    = { "extends" datatype* } | {}
    *
    *     modifiers see modifierList()
    *     datatype  see datatype()
    *     body      see typeBody()
    *  </pre>
    */

    public CSTNode interfaceDeclaration(CSTNode modifiers) throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode interfaceDeclaration = rootNode(Token.KEYWORD_INTERFACE);
        interfaceDeclaration.addChild(modifiers);
        consume(interfaceDeclaration, Token.IDENTIFIER);
        interfaceDeclaration.addChild(new CSTNode());

        //
        // Process any extends clause.

        try {
            CSTNode extendsNode = typeList(Token.KEYWORD_EXTENDS, IMPLEMENTS_CLAUSE_TERMINATORS, true, 0);
            interfaceDeclaration.addChild(extendsNode);
        }
        catch (SyntaxException e) {
            collector.add(e);
            interfaceDeclaration.addChild(new CSTNode());
        }

        //
        // Process the declaration body.  All methods must be abstract.
        // Static methods are not allowed.

        interfaceDeclaration.addChild(typeBody(false, true, true));
        return interfaceDeclaration;
    }



   /**
    *  Processes a type list, like the ones that occur after "extends" or 
    *  implements.  If the list is optional, the returned CSTNode will
    *  be empty.
    *  <p>
    *  Grammar: <pre>
    *     typeList = datatype (, datatype)*
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     typeList = { <declarator> datatype+ } | {}
    *
    *     datatype see datatype()
    *  </pre>
    */

    public CSTNode typeList(int declarator, int[] until, boolean optional, int limit)
        throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode typeList = null;

        if (lt() == declarator) {
            typeList = rootNode(declarator);

            //
            // Loop, reading one datatype at a time.  On error, attempt
            // recovery until the end of the clause is found.

            while (limit == 0 || typeList.children() < limit) {

                //
                // Try for a list entry, and correct if missing

                try {

                    if (typeList.children() > 0) {
                        consume(Token.COMMA);
                    }

                    CSTNode datatype = datatype(false);
                    typeList.addChild(datatype);
                }
                catch (SyntaxException e) {
                    collector.add(e);

                    //
                    // If we are at the limit, consume until the end of the clause

                    if (limit > 0 && typeList.children() >= limit) {
                        recover( until );
                    }

                    //
                    // Otherwise, consume until the end of the clause or a comma

                    else {
                        int[] safe = new int[until.length + 1];
                        safe[0] = Token.COMMA;
                        for( int i = 0; i < until.length; i++ ) {
                            safe[i+1] = until[i];
                        }

                        recover( safe );
                    }
                }

                //
                // Check if we have reached the end point.  It is
                // done at the bottom of the loop to ensure that there
                // is at least one datatype in the list

                if( lt() == -1 || la().isA(until) ) {
                    break;
                }
            }
        }

        else {
            if (optional) {
                typeList = new CSTNode();
            }
            else {
                throwExpected(new int[] { declarator });
            }
        }

        return typeList;
    }



   /**
    *  Processes the body of an interface or class.
    *  <p>
    *  Grammar: <pre>
    *     typeBody = "{" typeBodyStatement* "}"
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     body = { <null> typeBodyStatement* }
    *
    *     typeBodyStatement see typeBodyStatement()
    *  </pre>
    */

    public CSTNode typeBody(boolean allowStatic, boolean allowAbstract, boolean requireAbstract)
        throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode body = new CSTNode();

        consume(Token.LEFT_CURLY_BRACE);

        while (lt() != -1 && lt() != Token.RIGHT_CURLY_BRACE) {
            try {
                body.addChild(typeBodyStatement(allowStatic, allowAbstract, requireAbstract));
            }
            catch (SyntaxException e) {
                collector.add(e);
                recover();
            }
        }

        consume(Token.RIGHT_CURLY_BRACE);

        return body;
    }



   /**
    *  Processes a single entry in the the body of an interface or class.
    *  Valid objects are constructors, methods, properties, static initializers,
    *  and inner classes or interfaces.
    *  <p>
    *  Grammar: <pre>
    *     typeBodyStatement
    *       = staticInitializer 
    *       | classDeclaration
    *       | interfaceDeclaration
    *       | propertyDeclaration
    *       | methodDeclaration
    *
    *     staticInitializer = ("static" "{" statement* "}")
    *  </pre>
    *  <p>
    *  Recognition: <pre>
    *     "static" "{"             => staticInitializer
    *     modifierList "class"     => classDeclaration
    *     modifierList "interface" => interfaceDeclaration
    *     modifierList ["property"] optionalDatatype identifier "("               => methodDeclaration
    *     modifierList ["property"] optionalDatatype identifier ("="|";"|"\n"|"}" => propertyDeclaration
    *     *                        => <error>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     see classDeclaration()
    *     see interfaceDeclaration()
    *     see methodDeclaration()
    *     see propertyDeclaration()
    *  </pre>
    */

    public CSTNode typeBodyStatement(boolean allowStatic, boolean allowAbstract, boolean requireAbstract)
        throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode statement = null;

        //
        // As "static" can be both a modifier and a static initializer, we
        // handle the static initializer first.

        if (lt() == Token.KEYWORD_STATIC && lt(2) == Token.LEFT_CURLY_BRACE) {

            if (!allowStatic) {
                collector.add(new ParserException("static initializers not valid in this context", la()));
            }

            CSTNode modifiers = modifierList(true, false);
            CSTNode identifier = new CSTNode(Token.identifier(-1, -1, ""));
            statement = methodDeclaration(modifiers, new CSTNode(), identifier, false);
        }

        //
        // Otherwise, it is a property, constructor, method, class, or interface.

        else {

            CSTNode modifiers = modifierList(allowStatic, allowAbstract);

            //
            // Check for inner types

            if (lt() == Token.KEYWORD_CLASS) {
                statement = classDeclaration(modifiers);
            }

            else if (lt() == Token.KEYWORD_INTERFACE) {
                statement = interfaceDeclaration(modifiers);
            }

            //
            // Otherwise, it is a property, constructor, or method.

            else {

                //
                // Ignore any property keyword, if present (it's deprecated)

                if (lt() == Token.KEYWORD_PROPERTY) {
                    consume(lt());
                }

                //
                // All processing here is whitespace sensitive, in order
                // to be consistent with the way "def" functions work (due
                // to the optionality of the semicolon).  One of the
                // consequences is that the left parenthesis of a 
                // method declaration /must/ appear on the same line.

                while (lt_bare() == Token.NEWLINE) {
                    consume_bare(lt_bare());
                }

                //
                // We don't yet know about void, so we err on the side of caution

                CSTNode type = optionalDatatype(true, true);
                CSTNode identifier = nameDeclaration(true);

                switch (lt_bare()) {
                    case Token.LEFT_PARENTHESIS :
                        {

                            //
                            // We require abstract if specified on call or the 
                            // "abstract" modifier was supplied.

                            boolean methodIsAbstract = requireAbstract;

                            if (!methodIsAbstract) {
                                Iterator iterator = modifiers.childIterator();
                                while (iterator.hasNext()) {
                                    CSTNode child = (CSTNode) iterator.next();
                                    if (child.getToken().getType() == Token.KEYWORD_ABSTRACT) {
                                        methodIsAbstract = true;
                                        break;
                                    }
                                }
                            }

                            statement = methodDeclaration(modifiers, type, identifier, methodIsAbstract);
                            break;
                        }

                    case Token.EQUAL :
                    case Token.SEMICOLON :
                    case Token.NEWLINE :
                    case Token.RIGHT_CURLY_BRACE :
                    case -1 :
                        statement = propertyDeclaration(modifiers, type, identifier);
                        endOfStatement();
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
            }
        }

        return statement;
    }



   /**
    *  A synonym for <code>typeBodyStatement( true, true, false )</code>.
    */

    public CSTNode bodyStatement() throws ReadException, SyntaxException, ExceptionCollector {
        return typeBodyStatement(true, true, false);
    }



   /**
    *  Processes a name that is valid for declarations.  Newlines can be made 
    *  significant, if required for disambiguation.
    *  <p>
    *  Grammar: <pre>
    *     identifier = <identifier>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     name = { <identifier> }
    *  </pre>
    */

    protected CSTNode nameDeclaration(boolean useBare) throws ReadException, SyntaxException {
        if (useBare) {
            return new CSTNode(consume_bare(Token.IDENTIFIER));
        }
        else {
            return new CSTNode(consume(Token.IDENTIFIER));
        }
    }



   /**
    *  Processes a reference to a declared name.  Newlines can be made significant, 
    *  if required for disambiguation.
    *  <p>
    *  Grammar: <pre>
    *     nameReference = <identifier> | "class" | "interface" 
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     name = { <identifier> }
    *  </pre>
    */

    protected CSTNode nameReference(boolean useBare) throws ReadException, SyntaxException {

        Token token = (useBare ? la_bare() : la());

        if( token == null || !token.isValidNameReference() ) {
            throwExpected( new int[] { Token.IDENTIFIER } );
        }

        return new CSTNode( consume(lt()).toIdentifier() );

    }



   /**
    *  Processes an optional data type marker (for a parameter, method return type, 
    *  etc.).  Newlines can be made significant, if required for disambiguation.
    *  <p>
    *  Grammar: <pre>
    *     optionalDatatype = datatype? (?=<identifier>)
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     result = datatype | {}
    *
    *     see datatype()
    *  </pre>
    */

    protected CSTNode optionalDatatype(boolean useBare, boolean allowVoid)
        throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode type = new CSTNode();
        Token la = (useBare ? la_bare() : la());

        if( la == null ) {
            // no op
        }

        else if( la.isA(Token.IDENTIFIER) ) {

            //
            // If it is an identifier, it could be an untyped variable/method
            // name.  We test this by verifying that it a) isn't a simple
            // identifier or b) that it is followed by another identifier.

            if( lt_bare(2) != -1 && la_bare(2).isA(OPTIONAL_DATATYPE_FOLLOWER) ) {
                type = datatype(allowVoid);
            }
        }

        else if( la.isPrimitiveTypeKeyword(true) ) {
            type = datatype(allowVoid);
        }

        return type;
    }

    public static final int[] OPTIONAL_DATATYPE_FOLLOWER = { Token.IDENTIFIER, Token.LEFT_SQUARE_BRACKET, Token.DOT };



   /**
    *  Processes a class/interface property, including the optional initialization
    *  clause.  The modifiers, type, and identifier have already been identified
    *  by the caller, and are passed in.
    *  <p>
    *  Grammar: <pre>
    *     propertyDeclaration = (modifierList optionalDatatype identifier ["=" expression])
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     property = { "property" modifierList methodIdentifier methodReturnType expression? }
    *     
    *     see modifierList()
    *     see methodIdentifier()
    *     see methodReturnType()
    *     see expression()
    *  </pre>
    */

    public CSTNode propertyDeclaration(CSTNode modifiers, CSTNode type, CSTNode identifier)
        throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode property = new CSTNode(Token.keyword(-1, -1, "property"));

        property.addChild(modifiers);
        property.addChild(identifier);
        property.addChild(type);

        if (lt() == Token.EQUAL) {
            consume(lt());
            property.addChild(expression());
        }

        return property;
    }



   /**
    *  Processes a class/interface method.  The modifiers, type, and identifier have 
    *  already been identified by the caller, and are passed in.  If <code>emptyOnly</code>
    *  is set, no method body will be allowed.
    *  <p>
    *  Grammar: <pre>
    *     methodDeclaration = modifierList optionalDatatype identifier 
    *                         "(" parameterDeclarationList ")" 
    *                         [ "throws" typeList ]
    *                         ( statementBody | ";" )?
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     property = { "method" modifierList methodIdentifier methodReturnType 
    *                   parameterDeclarationList statementBody throwsClause }
    *
    *     throwsClause = { "throws" datatype+ } | {}
    *     
    *     see modifierList()
    *     see methodIdentifier()
    *     see methodReturnType()
    *     see parameterDeclarationList()
    *     see statementBody()
    *  </pre>
    */

    public CSTNode methodDeclaration(CSTNode modifiers, CSTNode type, CSTNode identifier, boolean emptyOnly)
        throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode method = new CSTNode(Token.syntheticMethod());
        method.addChild(modifiers);
        method.addChild(identifier);
        method.addChild(type);

        //
        // Process the parameter list

        Token label = consume(Token.LEFT_PARENTHESIS);
        CSTNode parameters = parameterDeclarationList();
        parameters.setToken(label);

        method.addChild(parameters);
        consume(Token.RIGHT_PARENTHESIS);

        //
        // Process the optional "throws" clause

        CSTNode throwsClause = new CSTNode();
        try {
            throwsClause = typeList(Token.KEYWORD_THROWS, THROWS_CLAUSE_TERMINATORS, true, 0);
        }
        catch (SyntaxException e) {
            collector.add(e);
        }

        //
        // And the body, but only if allowed.

        if (emptyOnly) {
            method.addChild(new CSTNode());

            if (lt() == Token.LEFT_CURLY_BRACE) {
                collector.add(new ParserException("abstract and interface methods cannot have a body", la()));
            }
            else {
                endOfStatement();
            }

        }
        else {
            method.addChild( statementBody(true) );
        }

        //
        // Finally, tack on the throws clause and return.

        method.addChild(throwsClause);

        return method;
    }



   /**
    *  Processes a parameter declaration list, which can occur on methods and closures.
    *  It loops as long as it finds a comma as the next token.
    *  <p>
    *  Grammar: <pre>
    *     parameterDeclarationList 
    *        = (parameterDeclaration ("," parameterDeclaration)* ("," parameterDeclaration "=" expression)* )?
    *        | (parameterDeclaration "=" expression ("," parameterDeclaration "=" expression)* )?
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     parameters = { <null> parameter* }
    *     parameter  = { <null> datatype nameDeclaration default }
    *     default    = { } | expression
    *  </pre>
    */

    protected CSTNode parameterDeclarationList() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode list = new CSTNode();

        boolean expectDefaults = false;
        while( lt() != -1 && la().isIdentifierOrPrimitiveTypeKeyword() ) {
            
            //
            // Get the declaration

            CSTNode parameter = parameterDeclaration();
            list.addChild(parameter);

            //
            // Process any default parameter (it is required on every parameter
            // after the first occurrance).

            CSTNode value = new CSTNode();
            if( expectDefaults || lt() == Token.EQUAL ) {
                expectDefaults = true;
                consume( Token.EQUAL );

                value = expression();
            }

            parameter.addChild( value );

            //
            // Check if we are done.

            if (lt() == Token.COMMA) {
                consume(Token.COMMA);
            }
            else {
                break;
            }
        }

        return list;
    }



   /**
    *  Processes a single parameter declaration, which can occur on methods and closures.
    *  <p>
    *  Grammar: <pre>
    *     parameterDeclaration = optionalDatatype nameDeclaration
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     parameter = { <null> datatype nameDeclaration }
    *     
    *     see datatype()
    *     see nameDeclaration()
    *  </pre>
    */

    protected CSTNode parameterDeclaration() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode parameter = new CSTNode(Token.syntheticParameterDeclaration());
        parameter.addChild(optionalDatatype(false, false));
        parameter.addChild(nameDeclaration(false));

        return parameter;
    }



   /**
    *  Processes a datatype specification.  For reasons of disambiguation,
    *  the array marker ([]) must never be on a separate line from the 
    *  base datatype.
    *  <p>
    *  Grammar: <pre>
    *     datatype = scalarDatatype ( "[" "]" )*
    *     
    *     scalarDatatype 
    *       = (<identifier> ("." <identifier>)*)
    *       | "void" | "int" | ...
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     datatype  = { "[" datatype } | scalar
    *     scalar    = typename | primitive
    *     typename  = { "." typename name } | name 
    *     name      = { <identifier> }
    *     primitive = { "void" } | { "int" } | ...
    *  </pre>
    */

    protected CSTNode datatype(boolean allowVoid) throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode datatype = scalarDatatype(allowVoid);

        //
        // Then handle any number of array dimensions

        while (lt_bare() == Token.LEFT_SQUARE_BRACKET) {
            CSTNode array = rootNode(Token.LEFT_SQUARE_BRACKET, datatype);
            consume_bare(Token.RIGHT_SQUARE_BRACKET);

            datatype = array;
        }

        return datatype;
    }



   /**
    *  A synonym for <code>datatype( true )</code>.
    */

    protected CSTNode datatype() throws ReadException, SyntaxException, ExceptionCollector {
        return datatype(true);
    }



   /**
    *  Processes a scalar datatype specification.  
    *  <p>
    *  Grammar: <pre>
    *     scalarDatatype 
    *       = (<identifier> ("." <identifier>)*)
    *       | "void" | "int" | ...
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     scalar    = typename | primitive
    *     typename  = { "." typename name } | name 
    *     name      = { <identifier> }
    *     primitive = { "void" } | { "int" } | ...
    *  </pre>
    */

    protected CSTNode scalarDatatype(boolean allowVoid) throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode datatype = null;

        if( lt() != -1 && la().isPrimitiveTypeKeyword(allowVoid) ) {
            datatype = rootNode(lt());
        }
        else {
            datatype = rootNode(Token.IDENTIFIER);

            while (lt() == Token.DOT) {
                CSTNode dot = rootNode(Token.DOT, datatype);
                consume(dot, Token.IDENTIFIER);
                datatype = dot;
            }
        }

        return datatype;
    }



   /**
    *  Processes the body of a complex statement (like "if", "for", etc.).
    *  Set <code>requireBraces</code> if the body must not be just a single
    *  statement.
    *  <p>
    *  Grammar: <pre>
    *     statementBody = ("{" statement* "}")
    *                   | statement
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     complex = { "{" statement* }
    *     simple  = statement
    *
    *     see statement()
    *  </pre>
    */

    protected CSTNode statementBody( boolean requireBraces ) throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode body = null;


        if (lt() == Token.LEFT_CURLY_BRACE) {
            body = statementsUntilRightCurly( rootNode(Token.LEFT_CURLY_BRACE) );
            body.getToken().setInterpretation( Token.SYNTH_BLOCK );
            consume(Token.RIGHT_CURLY_BRACE);
        }
        else {
            if( requireBraces ) {
                throwExpected( new int[] { Token.LEFT_CURLY_BRACE } );
            }
            else {
               body = statement();
            }
        }

        return body;
    }



   /**
    *  Reads statements until a "}" is met.  Adds each statement as a child of the
    *  root noe, which you must supply.
    *  <p>
    *  Grammar: <pre>
    *     statementsUntilRightCurly = statement* (?= "}")
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statements added as children of supplied root node
    *  </pre>
    */

    protected CSTNode statementsUntilRightCurly(CSTNode root) throws ReadException, SyntaxException, ExceptionCollector {


        while (lt() != Token.RIGHT_CURLY_BRACE) {
            try {
                root.addChild(statement());
            }
            catch( SyntaxException e ) {
                collector.add( e );
                recover();
            }
        }


        return root;
    }


   /**
    *  Processes a single statement.  Statements include: loop constructs, branch
    *  constructs, flow control constructs, exception constructs, expressions of
    *  a variety of types, and pretty much anything you can put inside a method.
    *  <p>
    *  Grammar: <pre>
    *     statement = (label ":")? pureStatement
    *
    *     pureStatement = forStatement 
    *                   | whileStatement
    *                   | doStatement
    *                   | continueStatement
    *                   | breakStatement
    *                   | ifStatement
    *                   | tryStatement
    *                   | throwStatement
    *                   | synchronizedStatement
    *                   | switchStatement
    *                   | returnStatement
    *                   | assertStatement
    *                   | expression <eos>
    *                   | ";"
    *
    *     label = <identifier>
    *  </pre>
    *  <p>
    *  Recognition: <pre>
    *     <keyword>  => <keyword>Statement
    *     *          => expression
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     labelled = { ":" <identifier> pureStatement }
    *     empty = { "{" }
    *
    *     see forStatement()
    *     see whileStatement()
    *     see doStatement()
    *     see continueStatement()
    *     see breakStatement()
    *     see ifStatement()
    *     see tryStatement()
    *     see throwStatement()
    *     see synchronizedStatement()
    *     see switchStatement()
    *     see returnStatement()
    *     see assertStatement()
    *     see expression()
    *  </pre>
    */

    protected CSTNode statement( boolean allowUnlabelledBlocks ) throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode statement = null;

        //
        // Check for and grab any label for the statement

        CSTNode label = null;
        if( lt() == Token.IDENTIFIER && lt(2) == Token.COLON )
        {
            label = rootNode( Token.COLON, rootNode(lt()) );
            label.getToken().setInterpretation( Token.SYNTH_LABEL );
        }

        //
        // Process the statement

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
            case (Token.KEYWORD_DO) :
                {
                    statement = doWhileStatement();
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
                    break;
                }
            case (Token.KEYWORD_ASSERT) :
                {
                    statement = assertStatement();
                    break;
                }
            case (Token.SEMICOLON) :
                {
                    Token token = consume(lt());
                    token.setInterpretation( Token.SYNTH_BLOCK );
                    statement = new CSTNode( token );
                }
            case (Token.LEFT_CURLY_BRACE) :
                {
                    //
                    // Bare blocks are no longer generally supported, due to the ambiguity 
                    // with closures.  Further, closures and blocks can look identical 
                    // until after parsing, so we process first as a closure expression,
                    // then, if the expression is a parameter-less, bare closure, recast
                    // it as a block (which generally requires a label).  Joy.

                    statement = expression();
                    if( statement.getToken().isA(Token.LEFT_CURLY_BRACE) ) {

                        if( statement.getChild(0).isEmpty() ) {

                            CSTNode block = new CSTNode( statement.getToken() );
                            block.getToken().setInterpretation( Token.SYNTH_BLOCK );

                            Iterator children = statement.getChild(1).childIterator();
                            while( children.hasNext() ) {
                                block.addChild( (CSTNode)children.next() );
                            }

                            statement = block;

                            if( label == null && !allowUnlabelledBlocks ) {
                                collector.add( new ParserException("groovy does not support anonymous blocks; please add a label", statement.getToken()) );
                            }
                        }
                    }
                    else {

                       //
                       // It's a closure expression, and must be a statement

                       endOfStatement();
                    }

                    break;
                }
            default :
                {
                    try {
                        statement = expression();
                        endOfStatement();
                    }

                    catch (SyntaxException e) {
                        collector.add(e);
                        recover();
                    }
                }
        }

        //
        // Wrap in the statement in the label, in necessary.

        if( label != null ) {
            label.addChild( statement );
            statement = label;
        }

        return statement;
    }


   /**
    *  Synonym for <code>statement( false )</code>.
    */

    protected CSTNode statement( ) throws ReadException, SyntaxException, ExceptionCollector {
        return statement(false);
    }


   /**
    *  Processes a switch statement.  
    *  <p>
    *  Grammar: <pre>
    *     switchStatment = "switch" "(" expression ")" "{" switchBody "}"
    *
    *     switchBody = caseSet*
    *     caseSet = (("case" expression ":")+ | ("default" ":")) statement+
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statements added as children of supplied root node
    *  </pre>
    */

    protected CSTNode switchStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_SWITCH);
        consume(Token.LEFT_PARENTHESIS);
        statement.addChild(expression());
        consume(Token.RIGHT_PARENTHESIS);

        //
        // Process the switch body.  Labels can be pretty much anything,
        // but we'll duplicate-check for default.

        consume(Token.LEFT_CURLY_BRACE);

        boolean defaultFound = false;
        while( lt() == Token.KEYWORD_CASE || lt() == Token.KEYWORD_DEFAULT ) {

            //
            // Read the label

            CSTNode caseBlock = null;
            if( lt() == Token.KEYWORD_CASE ) {
                caseBlock = rootNode( Token.KEYWORD_CASE );
                caseBlock.addChild( expression() );
            }
            else {
                if( defaultFound ) {
                    throw new ParserException( "duplicate default entry in switch", la() );
                }
                     
                caseBlock = rootNode( Token.KEYWORD_DEFAULT );
                defaultFound = true;
            }

            consume( Token.COLON );

            //
            // Process the statements, if any

            boolean first = true;
            while( lt() != -1 && !la().isA(SWITCH_STATEMENT_BLOCK_TERMINATORS) ) {
                caseBlock.addChild(statement(first));
                first = false;
            }

            statement.addChild(caseBlock);
        }

        consume(Token.RIGHT_CURLY_BRACE);

        return statement;
    }

    public static final int[] SWITCH_STATEMENT_BLOCK_TERMINATORS = { Token.RIGHT_CURLY_BRACE, Token.KEYWORD_CASE, Token.KEYWORD_DEFAULT };



   /**
    *  Processes a break statement.  
    *  <p>
    *  Grammar: <pre>
    *     breakStatement = "break" label? <eos>
    *     
    *     label = <identifier>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement = { "break" label? }
    *     
    *     label = { <identifier> }
    *  </pre>
    */

    protected CSTNode breakStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_BREAK);
        if (lt() == Token.IDENTIFIER) {
            statement.addChild(rootNode(lt()));
        }

        endOfStatement();
        return statement;

    }



   /**
    *  Processes a continue statement.  
    *  <p>
    *  Grammar: <pre>
    *     continueStatement = "continue" label? <eos>
    *     
    *     label = <identifier>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement = { "continue" label? }
    *     
    *     label = { <identifier> }
    *  </pre>
    */

    protected CSTNode continueStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_CONTINUE);
        if (lt() == Token.IDENTIFIER) {
            statement.addChild(rootNode(lt()));
        }

        endOfStatement();
        return statement;

    }



   /**
    *  Processes a throw statement.  
    *  <p>
    *  Grammar: <pre>
    *     throwStatement = "throw" expression <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement = { "throw" expression }
    *
    *     see expression()
    *  </pre>
    */

    protected CSTNode throwStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_THROW);
        statement.addChild(expression());

        endOfStatement();
        return statement;

    }



   /**
    *  Processes a synchronized statement.  
    *  <p>
    *  Grammar: <pre>
    *     synchronizedStatement = "synchronized" "(" expression ")" statementBody 
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement = { "throw" expression }
    *
    *     see expression()
    *  </pre>
    */

    protected CSTNode synchronizedStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_SYNCHRONIZED);

        consume( Token.LEFT_PARENTHESIS );
        statement.addChild(expression());
        consume( Token.RIGHT_PARENTHESIS );

        statement.addChild( statementBody(true) );

        return statement;

    }



   /**
    *  Processes an if statement.  
    *  <p>
    *  Grammar: <pre>
    *     ifStatement  = ifClause elseIfClause* elseClause?
    *
    *     ifClause     = "if" "(" expression ")" statementBody
    *
    *     elseIfClause = "else" "if" "(" expression ")" statementBody
    *
    *     elseClause   = "else" statementBody
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     if   = { "if" expression statementBody else? }
    *     else = if | { "else" statementBody } 
    *
    *     see expression()
    *     see statementBody()
    *  </pre>
    */

    protected CSTNode ifStatement() throws ReadException, SyntaxException, ExceptionCollector {

        //
        // Process the if clause

        CSTNode statement = rootNode(Token.KEYWORD_IF);

        consume(Token.LEFT_PARENTHESIS);
        try {
            statement.addChild(expression());
        }
        catch( SyntaxException e ) {
            collector.add( e );
            recover( GENERAL_CLAUSE_TERMINATOR );
        }
        consume(Token.RIGHT_PARENTHESIS);

        statement.addChild( statementBody(false) );


        //
        // If the else clause is present:
        //   if it is an else if, recurse
        //   otherwise, build the else node directly.

        if( lt() == Token.KEYWORD_ELSE ) {
            if( lt(2) == Token.KEYWORD_IF ) {
                consume(Token.KEYWORD_ELSE);
                statement.addChild( ifStatement() );
            }
            else {
                CSTNode last = rootNode(Token.KEYWORD_ELSE);
                statement.addChild( last );
                last.addChild( statementBody(false) );
            }
        }

        return statement;
    }



   /**
    *  Processes an try statement.  
    *  <p>
    *  Grammar: <pre>
    *     tryStatement  = "try" statementBody catchClause* finallyClause?
    *
    *     catchClause   = "catch" "(" datatype identifier ")" statementBody
    *
    *     finallyClause = "finally" statementBody
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     try     = { "try" statementBody finally catches }
    *
    *     catches = { <null> catch* }
    *
    *     catch   = { "catch" datatype identifier statementBody }
    *
    *     finally = {} | statementBody
    *
    *     see datatype()
    *     see identifier()
    *     see statementBody()
    *  </pre>
    */

    protected CSTNode tryStatement() throws ReadException, SyntaxException, ExceptionCollector {
    
        //
        // Set up the statement with the try clause

        CSTNode statement = rootNode(Token.KEYWORD_TRY);
        statement.addChild( statementBody(true) );

        //
        // Process the catch clauses, saving them in a node for 
        // later appending (it goes in /after/ the finally clause.

        CSTNode catches = new CSTNode();
        while (lt() == Token.KEYWORD_CATCH) {
            try {
                CSTNode catchBlock = rootNode(Token.KEYWORD_CATCH);

                consume(Token.LEFT_PARENTHESIS);
                try {
                    catchBlock.addChild(datatype(false));
                    consume(catchBlock, Token.IDENTIFIER);
                }
                catch( SyntaxException e ) {
                    collector.add( e );
                    recover( GENERAL_CLAUSE_TERMINATOR );
                }
                consume(Token.RIGHT_PARENTHESIS);

                catchBlock.addChild( statementBody(true) );

                catches.addChild(catchBlock);
            }
            catch( SyntaxException e ) {
                collector.add( e );
            }
        }

        //
        // Process the finally clause, if available.

        if (lt() == Token.KEYWORD_FINALLY) {
            consume(Token.KEYWORD_FINALLY);
            statement.addChild( statementBody(true) );
        }
        else {
            statement.addChild(new CSTNode());
        }

        statement.addChild(catches);

        return statement;
    }



   /**
    *  Processes a return statement.  
    *  <p>
    *  Grammar: <pre>
    *     returnStatement = "return" expression? <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement = { "return" expression? }
    *
    *     see expression()
    *  </pre>
    */

    protected CSTNode returnStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_RETURN);

        if( lt_bare() != -1 && !la_bare().isA(STATEMENT_TERMINATORS) ) {
            statement.addChild(expression());
        }

        endOfStatement();
        return statement;

    }



   /**
    *  Processes a while statement.  
    *  <p>
    *  Grammar: <pre>
    *     whileStatement = "while" "(" expression ")" statementBody
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     while = { "while" expression statementBody }
    *
    *     see expression()
    *     see statementBody()
    *  </pre>
    */

    protected CSTNode whileStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_WHILE);

        consume(Token.LEFT_PARENTHESIS);
        try {
            statement.addChild(expression());
        }
        catch( SyntaxException e ) {
            collector.add( e );
            recover( GENERAL_CLAUSE_TERMINATOR );
        }
        consume(Token.RIGHT_PARENTHESIS);

        statement.addChild( statementBody(false) );
        return statement;

    }



   /**
    *  Processes a do ... while statement.  
    *  <p>
    *  Grammar: <pre>
    *     doWhileStatement = "do" statementBody "while" "(" expression ")" <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     do = { "do" statementBody expression }
    *
    *     see expression()
    *     see statementBody()
    *  </pre>
    */

    protected CSTNode doWhileStatement() throws ReadException, SyntaxException, ExceptionCollector {

        CSTNode statement = rootNode(Token.KEYWORD_DO);
        statement.addChild( statementBody(false) );
        consume(Token.KEYWORD_WHILE);

        consume(Token.LEFT_PARENTHESIS);
        try {
            statement.addChild(expression());
        } 
        catch( SyntaxException e ) {
            collector.add( e );
            recover( GENERAL_CLAUSE_TERMINATOR );
        }
        consume(Token.RIGHT_PARENTHESIS);

        return statement;

    }



   /**
    *  Processes a for statement.  
    *  <p>
    *  Grammar: <pre>
    *     forStatement = "for" statementBody "while" "(" expression ")" <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     do = { "do" statementBody expression }
    *
    *     see expression()
    *     see statementBody()
    *  </pre>
    */

    protected CSTNode forStatement() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode statement = rootNode(Token.KEYWORD_FOR);

        consume(Token.LEFT_PARENTHESIS);

        CSTNode identifierOrType = new CSTNode(consume(Token.IDENTIFIER));
        if (la_bare().getType() == Token.COLON) {
            consume(Token.COLON);
        }
        else {
            Token potentialIn = consume(Token.IDENTIFIER);
            if (!potentialIn.getText().equals("in")) {
                // we could be a type declaration

                // our next token must either be a colon or 'in'
                if (la_bare().getType() == Token.COLON) {
                    consume(Token.COLON);
                }
                else {
                    // must be followed by 'in'
                    Token inToken = consume(Token.IDENTIFIER);
                    if (!inToken.getText().equals("in")) {
                        throw new UnexpectedTokenException(inToken, new int[] { Token.COLON, Token.IDENTIFIER });
                    }
                }
                CSTNode identifier = new CSTNode(potentialIn);
                identifier.addChild(identifierOrType);
                identifierOrType = identifier;
            }
        }

        statement.addChild(identifierOrType);

        CSTNode expr = expression();

        statement.addChild(expr);

        consume(Token.RIGHT_PARENTHESIS);

        statement.addChild( statementBody(false) );

        return statement;
    }

    protected CSTNode assertStatement() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode statement = rootNode(Token.KEYWORD_ASSERT);

        statement.addChild(ternaryExpression());

        if (lt() == Token.COLON) {
            consume(Token.COLON);

            statement.addChild(expression());
        }
        else {
            statement.addChild(new CSTNode());
        }

        endOfStatement();

        return statement;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    protected CSTNode expression() throws ReadException, SyntaxException, ExceptionCollector {
        optionalNewlines();

        return assignmentExpression();
    }

    protected CSTNode assignmentExpression() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode expr = null;

        if (lt_bare() == Token.IDENTIFIER && lt_bare(2) == Token.IDENTIFIER && lt_bare(3) == Token.EQUAL) {
            // a typed variable declaration
            CSTNode typeExpr = new CSTNode(consume_bare(lt_bare()));
            expr = new CSTNode(consume_bare(lt_bare()));
            expr.addChild(typeExpr);
        }
        else {
            expr = ternaryExpression();
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
                    expr.addChild(ternaryExpression());
                    break;
                }
        }

        return expr;
    }

    protected CSTNode ternaryExpression() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode expr = logicalOrExpression();

        if (lt_bare() == Token.QUESTION) {
            expr = rootNode(Token.QUESTION, expr);
            optionalNewlines();
            expr.addChild(assignmentExpression());

            optionalNewlines();

            consume(Token.COLON);

            optionalNewlines();
            expr.addChild(ternaryExpression());
        }

        return expr;
    }

    protected CSTNode logicalOrExpression() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode expr = logicalAndExpression();

        while (lt_bare() == Token.LOGICAL_OR) {
            expr = rootNode(Token.LOGICAL_OR, expr);
            optionalNewlines();
            expr.addChild(logicalAndExpression());
        }

        return expr;
    }

    protected CSTNode logicalAndExpression() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode expr = equalityExpression();

        while (lt_bare() == Token.LOGICAL_AND) {
            expr = rootNode(Token.LOGICAL_AND, expr);
            optionalNewlines();
            expr.addChild(equalityExpression());
        }

        return expr;
    }

    protected CSTNode equalityExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode relationalExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode rangeExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode additiveExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode multiplicativeExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode unaryExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode postfixExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode primaryExpression() throws ReadException, SyntaxException, ExceptionCollector {
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
            case (Token.PATTERN_REGEX) :
                {
                    expr = regexPattern();
                    break PREFIX_SWITCH;
                }
            default :
                {
                    identifier = nameReference(true);
                    expr = identifier;

                    break PREFIX_SWITCH;
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

    protected CSTNode subscriptExpression(CSTNode expr) throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode methodCallOrPropertyExpression(CSTNode expr)
        throws ReadException, SyntaxException, ExceptionCollector {
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
        throws ReadException, SyntaxException, ExceptionCollector {
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
        throws SyntaxException, ReadException {
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
        throws ReadException, SyntaxException, ExceptionCollector {
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

    protected boolean lookAheadForMethodCall() throws ReadException, SyntaxException, ExceptionCollector {
        return (lt_bare() == Token.DOT || lt_bare() == Token.NAVIGATE) && (lt(2) != -1 && la(2).isValidNameReference());
    }


    protected CSTNode regexPattern() throws ReadException, SyntaxException, ExceptionCollector {
        Token token = consume(Token.PATTERN_REGEX);
        CSTNode expr = new CSTNode(token);
        CSTNode regexString = doubleQuotedString();
        expr.addChild(regexString);
        return expr;
    }

    protected CSTNode doubleQuotedString() throws ReadException, SyntaxException, ExceptionCollector {
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

            expr.addChild(
                new CSTNode(
                    Token.singleQuoteString(
                        token.getStartLine(),
                        token.getStartColumn() + cur + 1,
                        text.substring(textStart, exprStart))));

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

    protected CSTNode parentheticalExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode parameterList(int endOfListDemarc) throws ReadException, SyntaxException, ExceptionCollector {
        if( lt_bare() != -1 && la_bare().isValidNameReference() && lt_bare(2) == Token.COLON ) {
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

    protected CSTNode namedParameterList(int endOfListDemarc)
        throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode parameterList = new CSTNode(Token.syntheticList());

        while (lt() != endOfListDemarc) {
            CSTNode name = nameReference(true);

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

    protected CSTNode newExpression() throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode expr = rootNode(Token.KEYWORD_NEW);

        expr.addChild(scalarDatatype(false));

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
        else {
           // STUFF GOES HERE FOR THE new Object; CASE!!!
            
        }

        return expr;
    }

    // 
    // **TEMPORARY**

    protected CSTNode closureExpression() throws ReadException, SyntaxException, ExceptionCollector {
        return closureExpression(false);
    }

    protected CSTNode closureExpression( boolean pipeRequired ) throws ReadException, SyntaxException, ExceptionCollector {
        CSTNode expr = rootNode(Token.LEFT_CURLY_BRACE);
        expr.getToken().setInterpretation( Token.SYNTH_CLOSURE );

        // { statement();
        // { a |
        // { a, b |
        // { A a |
        // { A a, B b |
        // { int[] a, char b |
        // but not { a }, 

        int value = lt(1);

        boolean canBeParamList = false;

        if( lt() != -1 && la().isIdentifierOrPrimitiveTypeKeyword() ) {
            if( lt(2) != -1 && la(2).isIdentifierOrPrimitiveTypeKeyword() ) {
                canBeParamList = lt(3) == Token.PIPE || lt(3) == Token.COMMA;
            }
            else {
                canBeParamList = lt(2) == Token.PIPE || lt(2) == Token.COMMA;
            }
        }
        if (canBeParamList) {
            expr.addChild(parameterDeclarationList());
            pipeRequired = true;
        }
        else {
            expr.addChild(new CSTNode());
        }

        CSTNode block = new CSTNode();

        if (lt_bare() != Token.RIGHT_CURLY_BRACE) {
            if (pipeRequired || lt() == Token.PIPE) {
                consume(Token.PIPE);
            }

            statementsUntilRightCurly(block);
        }

        consume(Token.RIGHT_CURLY_BRACE);

        expr.addChild(block);

        return expr;
    }

    protected CSTNode listOrMapExpression() throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode mapExpression(CSTNode key) throws ReadException, SyntaxException, ExceptionCollector {
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

    protected CSTNode listExpression(CSTNode entry) throws ReadException, SyntaxException, ExceptionCollector {
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
        throws ReadException, SyntaxException, ExceptionCollector
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

    protected CSTNode argumentList() throws ReadException, SyntaxException, ExceptionCollector {
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



  //---------------------------------------------------------------------------
  // SUPPORT ROUTINES


   /**
    *  Throws an <code>UnexpectedTokenException</code>.
    */

    protected void throwExpected(int[] expectedTypes) throws ReadException, SyntaxException {
        throw new UnexpectedTokenException(la(), expectedTypes);
    }


   /** 
    *  Ensures that a <code>Token</code> is not null.  Throws a 
    *  <code>ParserException</code> on error.
    */

    protected Token require( Token token ) throws ReadException, SyntaxException {
        if( token == null ) {
            throw new ParserException( "unexpected end of stream", null );
        }

        return token;
    }




  //---------------------------------------------------------------------------
  // VANILLA TOKEN LOOKAHEAD -- NEWLINES IGNORED


   /**
    *  Returns (without consuming) the next non-newline token in the 
    *  underlying token stream.  
    */

    protected Token la() throws ReadException, SyntaxException {
        return la(1);
    }



   /**
    *  Returns (without consuming any tokens) the next <code>k</code>th
    *  non-newline token from the underlying token stream.
    */

    protected Token la(int k) throws ReadException, SyntaxException {
        Token token = null;
        for (int pivot = 1, count = 0; count < k; pivot++) {
            token = getTokenStream().la(pivot);
            if (token == null || token.getType() != Token.NEWLINE) {
                count++;
            }
        }
        return token;
    }



   /**
    *  Returns the type of the <code>la()</code> token, or -1.
    */

    protected int lt() throws ReadException, SyntaxException {
        return lt(1);
    }



   /**
    *  Returns the type of the <code>la(k)</code> token, or -1.
    */

    protected int lt(int k) throws ReadException, SyntaxException {
        Token token = la(k);

        if (token == null) {
            return -1;
        }

        return token.getType();
    }




  //---------------------------------------------------------------------------
  // VANILLA TOKEN CONSUMPTION -- NEWLINES IGNORED


   /**
    *  Consumes tokens until one of the specified type is consumed.
    */

    protected void consumeUntil(int type) throws ReadException, SyntaxException {
        boolean done = false;

        while (lt() != -1 && !done) {

            if (lt() == type) {
                done = true;
            }

            consume(lt());
        }
    }



   /**
    *  Consumes (and returns) the next token if it is of the specified type, or 
    *  throws an <code>UnexpectedTokenException</code>.  If the specified type 
    *  is Token.NEWLINE, eats all available newlines, and consumes (and returns)
    *  the next (non-newline) token.
    */

    protected Token consume(int type) throws ReadException, SyntaxException {
        if (lt() != type) {
            throw new UnexpectedTokenException(la(), type);
        }

        //
        // lt() has told us there is a valid token somewhere, but it ignores
        // newlines, and we have to consume them from the underlying stream
        // before getting to the token lt() found.

        while (true) {
            Token token = getTokenStream().la();
            if (token == null || token.getType() != Token.NEWLINE) {
                break;
            }
            getTokenStream().consume(Token.NEWLINE);
        }

        return getTokenStream().consume(type);
    }



   /**
    *  Adds a <code>CSTNode</code> of the result of <code>consume(type)</code> 
    *  as a child of <code>root</code>.  Throws <code>UnexpectedTokenException</code>
    *  if the next token is not of the correct type.
    */

    protected void consume(CSTNode root, int type) throws ReadException, SyntaxException {
        root.addChild(new CSTNode(consume(type)));
    }



   /**
    *  Returns a new <code>CSTNode</code> that holds the result of 
    *  <code>consume(type)</code>.   Throws <code>UnexpectedTokenException</code>
    *  if the next token is not of the correct type.
    */

    protected CSTNode rootNode(int type) throws ReadException, SyntaxException {
        return new CSTNode(consume(type));
    }



   /**
    *  Identical to <code>rootNode(type)</code>, but adds <code>child</child> as
    *  a child of the newly created node.
    */

    protected CSTNode rootNode(int type, CSTNode child) throws ReadException, SyntaxException {
        CSTNode root = new CSTNode(consume(type));
        root.addChild(child);
        return root;
    }



  //---------------------------------------------------------------------------
  // RAW TOKEN LOOKAHEAD -- NEWLINES INCLUDED


   /**
    *  Returns (without consuming) the next token in the underlying token 
    *  stream (newlines included).  
    */

    protected Token la_bare() throws ReadException, SyntaxException {
        return la_bare(1);
    }



   /**
    *  Returns (without consuming any tokens) the next <code>k</code>th
    *  token from the underlying token stream (newlines included).
    */

    protected Token la_bare(int k) throws ReadException, SyntaxException {
        return getTokenStream().la(k);
    }



   /**
    *  Returns the type of the <code>la_bare()</code> token, or -1.
    */

    protected int lt_bare() throws ReadException, SyntaxException {
        return lt_bare(1);
    }



   /**
    *  Returns the type of the <code>la_bare(k)</code> token, or -1.
    */

    protected int lt_bare(int k) throws ReadException, SyntaxException {
        Token token = la_bare(k);

        if (token == null) {
            return -1;
        }

        return token.getType();
    }




  //---------------------------------------------------------------------------
  // RAW TOKEN CONSUMPTION -- NEWLINES INCLUDED


   /**
    *  Consumes tokens until one of the specified type is consumed.  Newlines
    *  are treated as normal tokens.
    */

    protected void consumeUntil_bare(int type) throws ReadException, SyntaxException {
        while (lt_bare() != -1) {
            consume_bare(lt_bare());

            if (lt_bare() == type) {
                consume(lt_bare());
                break;
            }
        }
    }



   /**
    *  Consumes (and returns) the next token if it is of the specified type, or 
    *  throws <code>UnexpectedTokenException</code>.  
    */

    protected Token consume_bare(int type) throws ReadException, SyntaxException {
        if (lt_bare() != type) {
            throw new UnexpectedTokenException(la_bare(), type);
        }

        return getTokenStream().consume(type);
    }



   /**
    *  Analogous to <code>consume(root, type)</code>, exception consumes with
    *  <code>consume_bare</code>.
    */

    protected void consume_bare(CSTNode root, int type) throws ReadException, SyntaxException {
        root.addChild(new CSTNode(consume_bare(type)));
    }



   /**
    *  Analagous to <code>rootNode(type)</code>, except consumes with 
    *  <code>consume_bare</code>.
    */

    protected CSTNode rootNode_bare(int type) throws ReadException, SyntaxException {
        return new CSTNode(consume_bare(type));
    }



   /**
    *  Analagous to <code>rootNode(type, child)</code>, except consumes with
    *  <code>consume_bare()</code>.
    */

    protected CSTNode rootNode_bare(int type, CSTNode child) throws ReadException, SyntaxException {
        CSTNode root = new CSTNode(consume_bare(type));
        root.addChild(child);
        return root;
    }
}
