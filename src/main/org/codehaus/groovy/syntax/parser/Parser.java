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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenStream;
import org.codehaus.groovy.syntax.Types;

/**
 *  Reads the source text and produces a Concrete Syntax Tree.  Exceptions
 *  are collected during processing, and parsing will continue for while
 *  possible, in order to report as many problems as possible.
 *  <code>module()</code> is the primary entry point.
 *
 *  @author Bob McWhirter
 *  @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
  */

public class Parser
{
    private SourceUnit  controller  = null;  // The controller to which we report errors
    private TokenStream tokenStream = null;  // Our token source
    private int         nestCount   = 1;     // Simplifies tracing of nested calls



  //---------------------------------------------------------------------------
  // CONSTRUCTION AND DATA ACCESS

   /**
    *  Sets the <code>Parser</code> to process a <code>TokenStream</code>,
    *  under control of the specified <code>SourceUnit</code>.
    */

    public Parser( SourceUnit controller, TokenStream tokenStream )
    {
        this.controller  = controller;
        this.tokenStream = tokenStream;
    }


   /**
    *  Synonym for module(), the primary entry point.
    */

    public Reduction parse() throws CompilationFailedException
    {
        try
        {
            return module();
        }
        catch( SyntaxException e )
        {
            controller.addFatalError( new SyntaxErrorMessage(e) );
        }

        throw new GroovyBugError( "this will never happen" );
    }



   /**
    *  Returns the <code>TokenStream</code> being parsed.
    */

    public TokenStream getTokenStream()
    {
        return this.tokenStream;
    }




  //---------------------------------------------------------------------------
  // PRODUCTION SUPPORT


   /**
    *  Eats any optional newlines.
    */

    public void optionalNewlines() throws SyntaxException, CompilationFailedException
    {
        while( lt(false) == Types.NEWLINE)
        {
            consume( Types.NEWLINE );
        }
    }



   /**
    *  Eats a required end-of-statement (semicolon or newline) from the stream.
    *  Throws an <code>UnexpectedTokenException</code> if anything else is found.
    */

    public void endOfStatement( boolean allowRightCurlyBrace ) throws SyntaxException, CompilationFailedException
    {
        Token next = la( true );

        if( next.isA(Types.GENERAL_END_OF_STATEMENT) )
        {
            consume( true );
        }
        else
        {
            if( allowRightCurlyBrace )
            {
                if( !next.isA(Types.RIGHT_CURLY_BRACE) )
                {
                    error( new int[] { Types.SEMICOLON, Types.NEWLINE, Types.RIGHT_CURLY_BRACE } );
                }
            }
            else
            {
                error( new int[] { Types.SEMICOLON, Types.NEWLINE } );
            }
        }
    }



   /**
    *  A synonym for <code>endOfStatement( true )</code>.
    */

    public void endOfStatement() throws SyntaxException, CompilationFailedException
    {
        endOfStatement( true );
    }




  //---------------------------------------------------------------------------
  // PRODUCTIONS: PRIMARY STRUCTURES


   /**
    *  Processes a dotted identifer.  Used all over the place.
    *  <p>
    *  Grammar: <pre>
    *     dottedIdentifier = <identifier> ("." <identifier>)*
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     dotted = { "." dotted <identifier> } | <identifier>
    *  </pre>
    */

    public CSTNode dottedIdentifier() throws SyntaxException, CompilationFailedException
    {
        CSTNode identifier = consume(Types.IDENTIFIER);

        while (lt() == Types.DOT)
        {
            identifier = consume(Types.DOT).asReduction( identifier, consume(Types.IDENTIFIER) );
        }

        return identifier;
    }



   /**
    *  The primary file-level parsing entry point.  The returned CST
    *  represents the content in a single class file.  Collects most
    *  exceptions and attempts to continue.
    *  <p>
    *  Grammar: <pre>
    *     module = [packageStatement]
    *              (usingStatement)*
    *              (topLevelStatement)*
    *              <eof>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     module = { <null> package imports (topLevelStatement)* }
    *
    *     package           see packageDeclaration()
    *     imports           see importStatement()
    *     topLevelStatement see topLevelStatement()
    *  </pre>
    *
    */

    public Reduction module() throws SyntaxException, CompilationFailedException
    {
        Reduction module = Reduction.newContainer();

        //
        // First up, the package declaration

        // XXX br: this is where i can do macro processing 
        
        Reduction packageDeclaration = null;

        if( lt() == Types.KEYWORD_PACKAGE )
        {
            try
            {
                packageDeclaration = packageDeclaration();
            }

            catch (SyntaxException e)
            {
                controller.addError(e);
                recover();
            }
        }

        if( packageDeclaration == null )
        {
            packageDeclaration = Reduction.EMPTY;
        }

        module.add( packageDeclaration );


        //
        // Next, handle import statements

        Reduction imports = (Reduction)module.add( Reduction.newContainer() );
        Object collector;

        while( lt() == Types.KEYWORD_IMPORT )
        {
            try
            {
                imports.add( importStatement() );
            }

            catch( SyntaxException e )
            {
                controller.addError(e);
                recover();
            }
        }


        //
        // With that taken care of, process everything else.

        while( lt() != Types.EOF )
        {
            try
            {
                module.add( topLevelStatement() );
            }
            catch (SyntaxException e)
            {
                controller.addError(e);
                recover();
            }
        }

        return module;
    }



   /**
    *  Processes a package declaration.  Called by <code>module()</code>.
    *  <p>
    *  Grammar: <pre>
    *     packageDeclaration = "package" dottedIdentifier <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     package = { "package" dottedIdentifier }
    *
    *     see dottedIdentifier()
    *  </pre>
    */

    public Reduction packageDeclaration() throws SyntaxException, CompilationFailedException
    {
        Reduction packageDeclaration = consume(Types.KEYWORD_PACKAGE).asReduction( dottedIdentifier() );
        endOfStatement( false );

        return packageDeclaration;
    }



   /**
    *  Processes an import statement.  Called by <code>module()</code>.
    *  <p>
    *  Grammar: <pre>
    *     importStatement = "import" (all|specific) <eos>
    *
    *     all      = package "." (package ".")* "*"
    *
    *     specific = (package "." (package ".")*)? classes
    *     classes  = class ["as" alias] ("," class ["as" alias])*
    *
    *     package  = <identifier>
    *     class    = <identifier>
    *     alias    = <identifier>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     import   = { "import" (package|{}) ({"*"} | clause+) }
    *
    *     package  = { "." package <identifier> } | <identifier>
    *     clause   = { <identifier> <alias>? }
    *  </pre>
    */

    public Reduction importStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction importStatement = consume(Types.KEYWORD_IMPORT).asReduction();

        //
        // First, process any package name.

        CSTNode packageNode = null;
        if( lt(2) == Types.DOT )
        {
            packageNode = consume(Types.IDENTIFIER).asReduction();

            while( lt(3) == Types.DOT )
            {
                packageNode = consume(Types.DOT).asReduction( packageNode );
                packageNode.add( consume(Types.IDENTIFIER) );
            }

            consume( Types.DOT );
        }

        if( packageNode == null )
        {
            packageNode = Reduction.EMPTY;
        }

        importStatement.add( packageNode );


        //
        // Then process the class list.

        if( !packageNode.isEmpty() && lt() == Types.STAR )
        {
            importStatement.add( consume(Types.STAR) );
        }
        else
        {
           boolean done = false;
           while( !done )
           {
               Reduction clause = consume(Types.IDENTIFIER).asReduction();
               if( lt() == Types.KEYWORD_AS )
               {
                   consume( Types.KEYWORD_AS );
                   clause.add( consume(Types.IDENTIFIER) );
               }

               importStatement.add( clause );

               if( lt() == Types.COMMA )
               {
                   consume( Types.COMMA );
               }
               else
               {
                   done = true;
               }
           }

        }

        //
        // End the statement and return.

        endOfStatement( false );
        return importStatement;
    }



   /**
    *  Processes a top level statement (classes, interfaces, unattached methods, and
    *  unattached code).  Called by <code>module()</code>.
    *  <p>
    *  Grammar: <pre>
    *     topLevelStatement
    *       = methodDeclaration
    *       | typeDeclaration
    *       | statement
    *
    *     typeDeclaration = classDeclaration | interfaceDeclaration
    *  </pre>
    *  <p>
    *  Recognition: <pre>
    *     "def"                    => methodDeclaration
    *     "synchronized" "("       => synchronizedStatement
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
    *     see synchronizedStatement()
    *  </pre>
    */

    public CSTNode topLevelStatement() throws SyntaxException, CompilationFailedException
    {
        CSTNode result = null;

        //
        // If it starts "def", it's a method declaration.  Methods
        // declared this way cannot be abstract.  Note that "def"
        // is required because the return type is not, and it would
        // be very hard to tell the difference between a function
        // def and a function invokation with closure...

        if (lt() == Types.KEYWORD_DEF)
        {
            consume();

            Reduction modifiers  = modifierList( false, false );
            CSTNode   type       = optionalDatatype( false, true );
            Token     identifier = nameDeclaration( false );

            result = methodDeclaration(modifiers, type, identifier, false);
        }

        else if (lt() == Types.KEYWORD_DEFMACRO)
        {
        	// XXX add my logic here
        }

        //
        // If it starts "synchronized(", it's a statement.  This check
        // is necessary because "synchronized" is also a class modifier.

        else if( lt() == Types.KEYWORD_SYNCHRONIZED && lt(2) == Types.LEFT_PARENTHESIS )
        {
            result = synchronizedStatement();
        }

        //
        // If it starts with a modifier, "class", or "interface",
        // it's a type declaration.

        else if( la().isA(Types.DECLARATION_MODIFIER) || la().isA(Types.TYPE_DECLARATION) )
        {
            Reduction modifiers = modifierList( true, true );

            switch( lt() )
            {
                case Types.KEYWORD_CLASS:
                {
                    result = classDeclaration( modifiers );
                    break;
                }

                case Types.KEYWORD_INTERFACE:
                {
                    result = interfaceDeclaration( modifiers );
                    break;
                }

                default:
                {
                    error( new int[] { Types.KEYWORD_CLASS, Types.KEYWORD_INTERFACE } );
                    break;
                }
            }
        }

        //
        // Otherwise, it's a statement.

        else
        {
            result = statement();
        }

        return result;
    }



   /**
    *  A synomym for <code>topLevelStatement()</code>.
    */

    public CSTNode typeDeclaration() throws SyntaxException, CompilationFailedException
    {
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
    *     modifiers = { <null> <modifier>* }
    *  </pre>
    */

    public Reduction modifierList(boolean allowStatic, boolean allowAbstract) throws CompilationFailedException, SyntaxException
    {
        Reduction modifiers = Reduction.newContainer();

        while( la().isA(Types.DECLARATION_MODIFIER) )
        {
            if( lt() == Types.KEYWORD_ABSTRACT && !allowAbstract)
            {
                controller.addError( "keyword 'abstract' not valid in this setting", la() );
            }
            else if (lt() == Types.KEYWORD_STATIC && !allowStatic)
            {
                controller.addError( "keyword 'static' not valid in this setting", la() );
            }
            modifiers.add( consume() );
        }

        return modifiers;
    }



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
    *     class      = { <identifier>:SYNTH_CLASS modifiers extends implements body }
    *     extends    = { "extends"    datatype  } | {}
    *     implements = { "implements" datatype* } | {}
    *
    *     modifiers see modifierList()
    *     datatype  see datatype()
    *     body      see typeBody()
    *  </pre>
    */

    public Reduction classDeclaration( Reduction modifiers ) throws SyntaxException, CompilationFailedException
    {
        consume( Types.KEYWORD_CLASS );

        Reduction classDeclaration = consume(Types.IDENTIFIER).asReduction( modifiers );
        classDeclaration.setMeaning( Types.SYNTH_CLASS );


        //
        // Process any extends clause.

        try
        {
            classDeclaration.add( typeList(Types.KEYWORD_EXTENDS, true, 1) );
        }
        catch (SyntaxException e)
        {
            controller.addError(e);
            classDeclaration.add( Reduction.EMPTY );
        }


        //
        // Process any implements clause.

        try
        {
            classDeclaration.add( typeList(Types.KEYWORD_IMPLEMENTS, true, 0) );
        }
        catch (SyntaxException e)
        {
            controller.addError(e);
            classDeclaration.add( Reduction.EMPTY );
        }


        //
        // Process the declaration body.  We currently ignore the abstract keyword.

        classDeclaration.add( typeBody(true, true, false) );

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
    *     interface  = { <identifier>:SYNTH_INTERFACE modifiers {} extends body }
    *     extends    = { "extends" datatype* } | {}
    *
    *     modifiers see modifierList()
    *     datatype  see datatype()
    *     body      see typeBody()
    *  </pre>
    */

    public Reduction interfaceDeclaration( Reduction modifiers ) throws SyntaxException, CompilationFailedException
    {
        consume( Types.KEYWORD_INTERFACE );

        Reduction interfaceDeclaration = consume(Types.IDENTIFIER).asReduction( modifiers, Reduction.EMPTY );
        interfaceDeclaration.setMeaning( Types.SYNTH_INTERFACE );


        //
        // Process any extends clause.

        try
        {
            interfaceDeclaration.add( typeList(Types.KEYWORD_EXTENDS, true, 0) );
        }
        catch (SyntaxException e)
        {
            controller.addError(e);
            interfaceDeclaration.add( Reduction.EMPTY );
        }


        //
        // Process the declaration body.  All methods must be abstract.
        // Static methods are not allowed.

        interfaceDeclaration.add( typeBody(false, true, true) );
        return interfaceDeclaration;
    }



   /**
    *  Processes a type list, like the ones that occur after "extends" or
    *  implements.  If the list is optional, the returned CSTNode will
    *  be empty.
    *  <p>
    *  Grammar: <pre>
    *     typeList = <declarator> datatype (, datatype)*
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     typeList = { <declarator> datatype+ } | {}
    *
    *     datatype see datatype()
    *  </pre>
    */

    public Reduction typeList(int declarator, boolean optional, int limit) throws SyntaxException, CompilationFailedException
    {
        Reduction typeList = null;

        if( lt() == declarator )
        {
            typeList = consume(declarator).asReduction();

            //
            // Loop, reading one datatype at a time.  On error, attempt
            // recovery until the end of the clause is found.

            while( limit == 0 || typeList.children() < limit )
            {
                //
                // Try for a list entry, and correct if missing

                try
                {
                    if( typeList.children() > 0)
                    {
                        consume( Types.COMMA );
                    }

                    typeList.add( datatype(false) );
                }
                catch (SyntaxException e)
                {
                    controller.addError(e);
                    recover( Types.TYPE_LIST_TERMINATORS );
                }

                //
                // Check if we have reached the end point.  It is
                // done at the bottom of the loop to ensure that there
                // is at least one datatype in the list

                if( !la().isA(Types.COMMA) )
                {
                    break;
                }
            }
        }

        else
        {
            if (optional)
            {
                typeList = Reduction.EMPTY;
            }
            else
            {
                error( declarator );
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

    public Reduction typeBody(boolean allowStatic, boolean allowAbstract, boolean requireAbstract) throws SyntaxException, CompilationFailedException
    {
        Reduction body = Reduction.newContainer();

        consume( Types.LEFT_CURLY_BRACE );

        while( lt() != Types.EOF && lt() != Types.RIGHT_CURLY_BRACE )
        {
            try
            {
                body.add( typeBodyStatement(allowStatic, allowAbstract, requireAbstract) );
            }
            catch( SyntaxException e )
            {
                controller.addError(e);
                recover();
            }
        }

        consume( Types.RIGHT_CURLY_BRACE );

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

    public Reduction typeBodyStatement(boolean allowStatic, boolean allowAbstract, boolean requireAbstract) throws SyntaxException, CompilationFailedException
    {
        Reduction statement = null;

        //
        // As "static" can be both a modifier and a static initializer, we
        // handle the static initializer first.

        if( lt() == Types.KEYWORD_STATIC && lt(2) == Types.LEFT_CURLY_BRACE )
        {
            if (!allowStatic)
            {
                controller.addError( "static initializers not valid in this context", la() );
            }

            Reduction modifiers  = modifierList( true, false );
            Token     identifier = Token.NULL;
            statement = methodDeclaration( modifiers, Reduction.EMPTY, identifier, false );
        }

        //
        // Otherwise, it is a property, constructor, method, class, or interface.

        else
        {
            Reduction modifiers = modifierList( allowStatic, allowAbstract );

            //
            // Check for inner types

            if( lt() == Types.KEYWORD_CLASS )
            {
                statement = classDeclaration( modifiers );
            }

            else if( lt() == Types.KEYWORD_INTERFACE )
            {
                statement = interfaceDeclaration( modifiers );
            }

            //
            // Otherwise, it is a property, constructor, or method.

            else
            {
                //
                // Ignore any property keyword, if present (it's deprecated)

                if( lt() == Types.KEYWORD_PROPERTY )
                {
                    consume();
                }

                //
                // All processing here is whitespace sensitive, in order
                // to be consistent with the way "def" functions work (due
                // to the optionality of the semicolon).  One of the
                // consequences is that the left parenthesis of a
                // method declaration /must/ appear on the same line.

                while( lt(true) == Types.NEWLINE)
                {
                    consume( Types.NEWLINE );
                }

                //
                // We don't yet know about void, so we err on the side of caution

                CSTNode   type       = optionalDatatype( true, true );
                Token     identifier = nameDeclaration( true );

                switch( lt(true) )
                {
                    case Types.LEFT_PARENTHESIS :
                    {
                        //
                        // We require abstract if specified on call or the
                        // "abstract" modifier was supplied.

                        boolean methodIsAbstract = requireAbstract;

                        if( !methodIsAbstract )
                        {
                            for( int i = 1; i < modifiers.size(); i++ )
                            {
                                if( modifiers.get(i).getMeaning() == Types.KEYWORD_ABSTRACT )
                                {
                                    methodIsAbstract = true;
                                    break;
                                }
                            }
                        }

                        statement = methodDeclaration( modifiers, type, identifier, methodIsAbstract );
                        break;
                    }

                    case Types.EQUAL:
                    case Types.SEMICOLON:
                    case Types.NEWLINE:
                    case Types.RIGHT_CURLY_BRACE:
                    case Types.EOF:
                        statement = propertyDeclaration( modifiers, type, identifier );
                        break;

                    default:
                        error( new int[] { Types.LEFT_PARENTHESIS, Types.EQUAL, Types.SEMICOLON, Types.NEWLINE, Types.RIGHT_CURLY_BRACE } );
                }
            }
        }

        return statement;
    }



   /**
    *  A synonym for <code>typeBodyStatement( true, true, false )</code>.
    */

    public Reduction bodyStatement() throws SyntaxException, CompilationFailedException
    {
        return typeBodyStatement( true, true, false );
    }



   /**
    *  Processes a name that is valid for declarations.  Newlines can be made
    *  significant, if required for disambiguation.
    *  <p>
    *  Grammar: <pre>
    *     nameDeclaration = <identifier>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     name = <identifier>
    *  </pre>
    */

    protected Token nameDeclaration( boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {
        return consume( Types.IDENTIFIER, significantNewlines );
    }



   /**
    *  Processes a reference to a declared name.  Newlines can be made significant,
    *  if required for disambiguation.
    *  <p>
    *  Grammar: <pre>
    *     nameReference = <identifier> | <various keywords>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     name = <identifier>
    *  </pre>
    */

    protected Token nameReference( boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {

        Token token = la( significantNewlines );
        if( !token.canMean(Types.IDENTIFIER) )
        {
            error( Types.IDENTIFIER );
        }

        consume();
        token.setMeaning( Types.IDENTIFIER );

        return token;

    }



   /**
    *  Processes an optional data type marker (for a parameter, method return type,
    *  etc.).  Newlines can be made significant, if required for disambiguation.
    *  <p>
    *  Grammar: <pre>
    *     optionalDatatype = datatype? (?=<identifier>)
    *  </pre>h
    *  <p>
    *  CST: <pre>
    *     result = datatype | {}
    *
    *     see datatype()
    *  </pre>
    */

    protected CSTNode optionalDatatype( boolean significantNewlines, boolean allowVoid ) throws SyntaxException, CompilationFailedException
    {
        CSTNode type = Reduction.EMPTY;
        Token   next = la(significantNewlines);

        //
        // If the next token is an identifier, it could be an untyped
        // variable/method name.  If it is followed by another identifier,
        // we'll assume type.  Otherwise, we'll attempt a datatype and
        // restore() the stream if there is a problem.

        if( next.isA(Types.IDENTIFIER) )
        {
            if( lt(2, significantNewlines) == Types.IDENTIFIER )
            {
                type = datatype( allowVoid );
            }
            else
            {
                getTokenStream().checkpoint();

                try
                {
                    type = datatype( allowVoid );
                    if( lt(significantNewlines) != Types.IDENTIFIER )
                    {
                        throw new Exception();
                    }
                }
                catch( Exception e )
                {
                    getTokenStream().restore();
                    type = Reduction.EMPTY;
                }
            }
        }

        //
        // If it is a primitive type name, it must be a datatype.  If void
        // is present but not allowed, it is an error, and we let datatype()
        // catch it.

        else if( next.isA(Types.PRIMITIVE_TYPE) )
        {
            type = datatype( allowVoid );
        }

        return type;
    }




   /**
    *  Processes a class/interface property, including the optional initialization
    *  clause.  The modifiers, type, and identifier have already been identified
    *  by the caller, and are passed in.
    *  <p>
    *  Grammar: <pre>
    *     propertyDeclaration = (modifierList optionalDatatype nameDeclaration ["=" expression]) <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     property = { <identifier>:SYNTH_PROPERTY modifierList optionalDatatype expression? }
    *
    *     see modifierList()
    *     see optionalDatatype()
    *     see expression()
    *  </pre>
    */

    public Reduction propertyDeclaration( Reduction modifiers, CSTNode type, Token identifier ) throws SyntaxException, CompilationFailedException
    {
        Reduction property = identifier.asReduction( modifiers, type );
        property.setMeaning( Types.SYNTH_PROPERTY );

        if( lt() == Types.EQUAL )
        {
            consume();
            property.add( expression() );
        }

        endOfStatement();
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
    *                         ( statementBody | <eos> )
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     method = { <identifier>:SYNTH_METHOD modifierList optionalDatatype
    *                 parameterDeclarationList throwsClause statementBody }
    *
    *     throwsClause = { "throws" datatype+ } | {}
    *
    *     see modifierList()
    *     see optionalDatatype()
    *     see parameterDeclarationList()
    *     see statementBody()
    *  </pre>
    */

    public Reduction methodDeclaration( Reduction modifiers, CSTNode type, Token identifier, boolean emptyOnly) throws SyntaxException, CompilationFailedException
    {
        Reduction method = identifier.asReduction( modifiers, type );
        method.setMeaning( Types.SYNTH_METHOD );

        //
        // Process the parameter list

        consume(Types.LEFT_PARENTHESIS);
        method.add( parameterDeclarationList() );
        consume(Types.RIGHT_PARENTHESIS);

        //
        // Process the optional "throws" clause

        try
        {
            method.add( typeList( Types.KEYWORD_THROWS, true, 0 ) );
        }
        catch (SyntaxException e)
        {
            controller.addError(e);
            method.add( Reduction.EMPTY );
        }

        //
        // And the body.  If it isn't supposed to be there, report the
        // error, but process it anyway, for the point of recovering.

        CSTNode body = null;

        if( emptyOnly )
        {
            if( lt() == Types.LEFT_CURLY_BRACE )
            {
                controller.addError( "abstract and interface methods cannot have a body", la() );
            }
            else
            {
                body = Reduction.EMPTY;
                endOfStatement();
            }

        }

        if( body == null )
        {
            body = statementBody(true);
        }

        method.add( body );


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
    *     parameter  = { <identifier>:SYNTH_PARAMETER_DECLARATION optionalDatatype default? }
    *     default    = expression
    *  </pre>
    */

    protected Reduction parameterDeclarationList() throws SyntaxException, CompilationFailedException
    {
        Reduction list = Reduction.newContainer();

        boolean expectDefaults = false;
        while( la().isA(Types.TYPE_NAME) )  // TYPE_NAME includes <identifier>, and so does double duty
        {

            //
            // Get the declaration

            Reduction parameter = (Reduction)list.add( parameterDeclaration() );

            //
            // Process any default parameter (it is required on every parameter
            // after the first occurrance).

            if( expectDefaults || lt() == Types.EQUAL )
            {
                expectDefaults = true;
                consume( Types.EQUAL );

                parameter.add( expression() );
            }

            //
            // Check if we are done.

            if( lt() == Types.COMMA )
            {
                consume( Types.COMMA );
            }
            else
            {
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
    *     parameter = { <identifier>:SYNTH_PARAMETER_DECLARATION optionalDatatype }
    *
    *     see optionalDatatype()
    *  </pre>
    */

    protected Reduction parameterDeclaration() throws SyntaxException, CompilationFailedException
    {
        CSTNode   type      = optionalDatatype( false, false );
        Reduction parameter = nameDeclaration( false ).asReduction( type );
        parameter.setMeaning( Types.SYNTH_PARAMETER_DECLARATION );

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
    *     scalarDatatype = dottedIdentifier | "void" | "int" | ...
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     datatype  = { "[" datatype } | scalar
    *     scalar    = dottedIdentifier | primitive
    *     primitive = "void" | "int" | ...
    *
    *     see dottedIdentifier()
    *  </pre>
    */

    protected CSTNode datatype( boolean allowVoid ) throws SyntaxException, CompilationFailedException
    {
        CSTNode datatype = scalarDatatype(allowVoid);

        while( lt(true) == Types.LEFT_SQUARE_BRACKET )
        {
            datatype = consume(Types.LEFT_SQUARE_BRACKET).asReduction( datatype );
            consume( Types.RIGHT_SQUARE_BRACKET );
        }

        return datatype;
    }



   /**
    *  A synonym for <code>datatype( true )</code>.
    */

    protected CSTNode datatype() throws SyntaxException, CompilationFailedException
    {
        return datatype(true);
    }



   /**
    *  Processes a scalar datatype specification.
    *  <p>
    *  Grammar: <pre>
    *     scalarDatatype = dottedIdentifier | "void" | "int" | ...
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     scalar    = dottedIdentifier | primitive
    *     primitive = "void" | "int" | ...
    *
    *     see dottedIdentifier()
    *  </pre>
    */

    protected CSTNode scalarDatatype( boolean allowVoid ) throws SyntaxException, CompilationFailedException
    {
        CSTNode datatype = null;

        if( la().isA(allowVoid ? Types.PRIMITIVE_TYPE : Types.CREATABLE_PRIMITIVE_TYPE) )
        {
            datatype = consume();
        }
        else
        {
            datatype = dottedIdentifier();
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

    protected CSTNode statementBody( boolean requireBraces ) throws SyntaxException, CompilationFailedException
    {
        CSTNode body = null;

        if (lt() == Types.LEFT_CURLY_BRACE)
        {
            Token brace = consume( Types.LEFT_CURLY_BRACE );
            brace.setMeaning( Types.SYNTH_BLOCK );

            body = statementsUntilRightCurly();
            body.set( 0, brace );

            consume( Types.RIGHT_CURLY_BRACE );
        }
        else
        {
            if( requireBraces )
            {
                error( Types.LEFT_CURLY_BRACE );
            }
            else
            {
               body = statement();
            }
        }

        return body;
    }



   /**
    *  Reads statements until a "}" is met.
    *  <p>
    *  Grammar: <pre>
    *     statementsUntilRightCurly = statement* (?= "}")
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statements = { <null> statement* }
    *  </pre>
    */

    protected Reduction statementsUntilRightCurly( ) throws SyntaxException, CompilationFailedException
    {
        Reduction block = Reduction.newContainer();

        while( lt() != Types.EOF && lt() != Types.RIGHT_CURLY_BRACE )
        {
            try
            {
                block.add( statement() );
            }
            catch( SyntaxException e )
            {
                controller.addError( e );
                recover();
            }
        }


        return block;
    }



  //---------------------------------------------------------------------------
  // PRODUCTIONS: STATEMENTS


   /**
    *  Processes a single statement.  Statements include: loop constructs, branch
    *  constructs, flow control constructs, exception constructs, expressions of
    *  a variety of types, and pretty much anything you can put inside a method.
    *  <p>
    *  Grammar: <pre>
    *     statement      = (label ":")? bareStatement
    *     bareStatement  = (emptyStatement|basicStatement|blockStatement)
    *
    *     basicStatement = forStatement
    *                    | whileStatement
    *                    | doStatement
    *                    | continueStatement
    *                    | breakStatement
    *                    | ifStatement
    *                    | tryStatement
    *                    | throwStatement
    *                    | synchronizedStatement
    *                    | switchStatement
    *                    | returnStatement
    *                    | assertStatement
    *                    | expression <eos>
    *
    *     label          = <identifier>
    *     blockStatement = "{" statement* "}"
    *     emptyStatement = ";"
    *  </pre>
    *  <p>
    *  Recognition: <pre>
    *     ";"       => emptyStatement
    *     <keyword> => <keyword>Statement
    *     "{"       => expression, then:
    *                    if it is a closureExpression and has no parameters => blockStatement
    *
    *     *         => expression
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     labelled       = { <identifier>:SYNTH_LABEL bareStatement }
    *     bareStatement  = emptyStatement | blockStatement | basicStatement
    *     emptyStatement = { "{" }
    *     blockStatement = { "{" statement* }
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

    protected CSTNode statement( boolean allowUnlabelledBlocks ) throws SyntaxException, CompilationFailedException
    {
        CSTNode statement = null;

        //
        // Check for and grab any label for the statement

        CSTNode label = null;
        if( lt() == Types.IDENTIFIER && lt(2) == Types.COLON )
        {
            label = consume( Types.IDENTIFIER ).asReduction();
            label.setMeaning( Types.SYNTH_LABEL );

            consume( Types.COLON );
        }

        //
        // Process the statement

        switch( lt() )
        {
            case Types.KEYWORD_ASSERT:
            {
                statement = assertStatement();
                break;
            }

            case Types.KEYWORD_BREAK:
            {
                statement = breakStatement();
                break;
            }

            case Types.KEYWORD_CONTINUE:
            {
                statement = continueStatement();
                break;
            }

            case Types.KEYWORD_IF:
            {
                statement = ifStatement();
                break;
            }

            case Types.KEYWORD_RETURN:
            {
                statement = returnStatement();
                break;
            }

            case Types.KEYWORD_SWITCH:
            {
                statement = switchStatement();
                break;
            }

            case Types.KEYWORD_SYNCHRONIZED:
            {
                statement = synchronizedStatement();
                break;
            }

            case Types.KEYWORD_THROW:
            {
                statement = throwStatement();
                break;
            }

            case Types.KEYWORD_TRY:
            {
                statement = tryStatement();
                break;
            }

            case Types.KEYWORD_FOR:
            {
                statement = forStatement();
                break;
            }

            case Types.KEYWORD_DO:
            {
                statement = doWhileStatement();
                break;
            }

            case Types.KEYWORD_WHILE:
            {
                statement = whileStatement();
                break;
            }

            case Types.SEMICOLON:
            {
                statement = consume().asReduction();
                statement.setMeaning( Types.SYNTH_BLOCK );
                break;
            }

            case Types.LEFT_CURLY_BRACE:
            {

                //
                // Bare blocks are no longer generally supported, due to the ambiguity
                // with closures.  Further, closures and blocks can look identical
                // until after parsing, so we process first as a closure expression,
                // then, if the expression is a parameter-less, bare closure, rebuild
                // it as a block (which generally requires a label).  Joy.

                statement = expression();
                if( statement.isA(Types.SYNTH_CLOSURE) )
                {
                    if( !statement.get(1).hasChildren() )
                    {
                        Reduction block = statement.getRoot().asReduction();
                        block.setMeaning( Types.SYNTH_BLOCK );
                        block.addChildrenOf( statement.get(2) );

                        if( label == null && !allowUnlabelledBlocks )
                        {
                            controller.addError( "groovy does not support anonymous blocks; please add a label", statement.getRoot() );
                        }

                        statement = block;
                    }
                }
                else
                {
                   //
                   // It's a closure expression, and must be a statement

                   endOfStatement();
                }

                break;
            }

            default:
            {
                try
                {
                    statement = expression();
                    endOfStatement();
                }
                catch (SyntaxException e)
                {
                    controller.addError(e);
                    recover();
                }
            }
        }


        //
        // Wrap the statement in the label, if necessary.

        if( label != null )
        {
            label.add( statement );
            statement = label;
        }

        return statement;
    }



   /**
    *  Synonym for <code>statement( false )</code>.
    */

    protected CSTNode statement( ) throws SyntaxException, CompilationFailedException
    {
        return statement( false );
    }



   /**
    *  Processes an assert statement.
    *  <p>
    *  Grammar: <pre>
    *     assertStatement = "assert" expression (":" expression) <eos>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     assert = { "assert" expression expression? }
    *
    *     see expression()
    *  </pre>
    */

    protected Reduction assertStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume( Types.KEYWORD_ASSERT ).asReduction( expression() );

        if( lt() == Types.COLON )
        {
            consume( Types.COLON );
            statement.add( expression() );
        }

        endOfStatement();

        return statement;
    }



   /**
    *  Processes a break statement.  We require the label on the same line.
    *  <p>
    *  Grammar: <pre>
    *     breakStatement = "break" label? <eos>
    *
    *     label = <identifier>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement = { "break" label? }
    *     label     = <identifier>
    *  </pre>
    */

    protected Reduction breakStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_BREAK).asReduction();
        if( lt(true) == Types.IDENTIFIER )
        {
            statement.add( consume() );
        }

        endOfStatement();
        return statement;

    }



   /**
    *  Processes a continue statement.  We require the label on the same line.
    *  <p>
    *  Grammar: <pre>
    *     continueStatement = "continue" label? <eos>
    *
    *     label = <identifier>
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement = { "continue" label? }
    *     label     = <identifier>
    *  </pre>
    */

    protected Reduction continueStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_CONTINUE).asReduction();
        if( lt(true) == Types.IDENTIFIER )
        {
            statement.add( consume() );
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

    protected Reduction throwStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_THROW).asReduction( expression() );
        endOfStatement();
        return statement;

    }



   /**
    *  Processes an if statement.
    *  <p>
    *  Grammar: <pre>
    *     ifStatement  = ifClause elseIfClause* elseClause?
    *
    *     ifClause     = "if" "(" expression ")" statementBody
    *     elseIfClause = "else" "if" "(" expression ")" statementBody
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

    protected Reduction ifStatement() throws SyntaxException, CompilationFailedException
    {
        //
        // Process the if clause

        Reduction statement = consume(Types.KEYWORD_IF).asReduction();

        consume( Types.LEFT_PARENTHESIS );

        try
        {
            statement.add( expression() );
        }
        catch( SyntaxException e )
        {
            controller.addError( e );
            recover( Types.RIGHT_PARENTHESIS );
        }

        consume( Types.RIGHT_PARENTHESIS );

        statement.add( statementBody(false) );


        //
        // If the else clause is present:
        //   if it is an else if, recurse
        //   otherwise, build the else node directly.

        if( lt() == Types.KEYWORD_ELSE )
        {
            if( lt(2) == Types.KEYWORD_IF )
            {
                consume( Types.KEYWORD_ELSE );
                statement.add( ifStatement() );
            }
            else
            {
                Reduction last = (Reduction)statement.add( consume(Types.KEYWORD_ELSE).asReduction() );
                last.add( statementBody(false) );
            }
        }

        return statement;
    }



   /**
    *  Processes a return statement.  Any expression must start on the same line
    *  as the "return".
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

    protected Reduction returnStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_RETURN).asReduction();

        if( !la(true).isA(Types.ANY_END_OF_STATEMENT) )
        {
            statement.add( expression() );
        }

        endOfStatement();
        return statement;

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
    *     switch = { "switch" expression case* }
    *     case   = { "case" expression statement* }
    *            | { "default" statement* }
    *
    *     see expression()
    *     see statement()
    *  </pre>
    */

    protected Reduction switchStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_SWITCH).asReduction();
        consume( Types.LEFT_PARENTHESIS );
        statement.add( expression() );
        consume( Types.RIGHT_PARENTHESIS );

        //
        // Process the switch body.  Labels can be pretty much anything,
        // but we'll duplicate-check for default.

        consume( Types.LEFT_CURLY_BRACE );

        boolean defaultFound = false;
        while( lt() == Types.KEYWORD_CASE || lt() == Types.KEYWORD_DEFAULT )
        {
            //
            // Read the label

            Reduction caseBlock = null;
            if( lt() == Types.KEYWORD_CASE )
            {
                caseBlock = consume( Types.KEYWORD_CASE ).asReduction( expression() );
            }
            else if( lt() == Types.KEYWORD_DEFAULT )
            {
                if( defaultFound )
                {
                    controller.addError( "duplicate default entry in switch", la() );
                }

                caseBlock = consume( Types.KEYWORD_DEFAULT ).asReduction();
                defaultFound = true;
            }
            else
            {
                error( new int[] { Types.KEYWORD_DEFAULT, Types.KEYWORD_CASE } );
                recover( Types.SWITCH_ENTRIES );
            }

            consume( Types.COLON );


            //
            // Process the statements, if any

            boolean first = true;
            while( !la().isA(Types.SWITCH_BLOCK_TERMINATORS) )
            {
                caseBlock.add( statement(first) );
                first = false;
            }

            statement.add( caseBlock );
        }

        consume( Types.RIGHT_CURLY_BRACE );

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
    *     statement = { "synchronized" expression statementBody }
    *
    *     see expression()
    *     see statementBody()
    *  </pre>
    */

    protected Reduction synchronizedStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_SYNCHRONIZED).asReduction();

        consume( Types.LEFT_PARENTHESIS );
        statement.add( expression() );
        consume( Types.RIGHT_PARENTHESIS );

        statement.add( statementBody(true) );

        return statement;

    }



   /**
    *  Processes a try statement.
    *  <p>
    *  Grammar: <pre>
    *     tryStatement  = "try" statementBody catchClause* finallyClause?
    *
    *     catchClause   = "catch" "(" datatype identifier ")" statementBody
    *     finallyClause = "finally" statementBody
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     try     = { "try" statementBody catches finally }
    *
    *     catches = { <null> catch* }
    *
    *     catch   = { "catch" datatype <identifier> statementBody }
    *
    *     finally = {} | statementBody
    *
    *     see datatype()
    *     see identifier()
    *     see statementBody()
    *  </pre>
    */

    protected Reduction tryStatement() throws SyntaxException, CompilationFailedException
    {

        //
        // Set up the statement with the try clause

        Reduction statement = consume(Types.KEYWORD_TRY).asReduction();
        statement.add( statementBody(true) );


        //
        // Process the catch clauses

        Reduction catches = (Reduction)statement.add( Reduction.newContainer() );
        while( lt() == Types.KEYWORD_CATCH )
        {
            try
            {
                Reduction catchBlock = (Reduction)catches.add( consume(Types.KEYWORD_CATCH).asReduction() );

                consume( Types.LEFT_PARENTHESIS );
                try
                {
                    catchBlock.add( datatype(false) );
                    catchBlock.add( nameDeclaration(false) );
                }
                catch( SyntaxException e )
                {
                    controller.addError( e );
                    recover( Types.RIGHT_PARENTHESIS );
                }
                consume( Types.RIGHT_PARENTHESIS );

                catchBlock.add( statementBody(true) );
            }
            catch( SyntaxException e )
            {
                controller.addError( e );
                recover();
            }
        }

        //
        // Process the finally clause, if available.

        if( lt() == Types.KEYWORD_FINALLY )
        {
            consume( Types.KEYWORD_FINALLY );
            statement.add( statementBody(true) );
        }
        else
        {
            statement.add( Reduction.EMPTY );
        }

        return statement;
    }



  //---------------------------------------------------------------------------
  // PRODUCTIONS: LOOP STATEMENTS


   /**
    *  Processes a for statement.
    *  <p>
    *  Grammar: <pre>
    *     forStatement = "for" "(" normal | each ")" statementBody
    *
    *     normal = multi ";" expression ";" multi
    *     multi  = (expression ["," expression]*)
    *
    *     each   = optionalDatatype nameDeclaration ("in"|":") expression
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     for    = { "for" header statementBody }
    *
    *     header = normal | each
    *     each   = { ("in"|":") optionalDatatype nameDeclaration expression }
    *
    *     normal = { <null> init test incr }
    *     init   = { <null> expression* }
    *     test   = expression
    *     incr   = { <null> expression* }
    *
    *     see expression()
    *     see nameDeclaration()
    *     see statementBody()
    *  </pre>
    */

    protected Reduction forStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume( Types.KEYWORD_FOR ).asReduction();

        //
        // The for loop is a little tricky.  There are three forms,
        // and the first two can't be processed with expression().
        // In order to avoid complications, we are going to checkpoint()
        // the stream before processing optionalDatatype(), then restore
        // it if we need to use expression().
        //
        // Anyway, after processing the optionalDatatype(), if KEYWORD_IN
        // or a COLON is at la(2), it's an each loop.  Otherwise, it's the
        // standard for loop.

        consume( Types.LEFT_PARENTHESIS );

        getTokenStream().checkpoint();

        Reduction header   = null;
        CSTNode   datatype = optionalDatatype( false, false );

        if( lt(2) == Types.KEYWORD_IN || lt(2) == Types.COLON )
        {
            Token name = nameDeclaration( false );
            header = consume().asReduction( datatype, name, expression() );
        }
        else
        {
            getTokenStream().restore();
            header = Reduction.newContainer();

            Reduction init = Reduction.newContainer();
            while( lt() != Types.SEMICOLON && lt() != Types.EOF )
            {
                init.add( expression() );

                if( lt() != Types.SEMICOLON )
                {
                    consume( Types.COMMA );
                }
            }

            consume( Types.SEMICOLON );

            header.add( init );


            //
            // Next up, a single expression is the test clause, followed
            // by a semicolon.

            header.add( expression() );
            consume( Types.SEMICOLON );


            //
            // Finally, the increment section is a (possibly empty) comma-
            // separated list of expressions followed by the RIGHT_PARENTHESIS.

            Reduction incr = (Reduction)header.add( Reduction.newContainer() );

            while( lt() != Types.RIGHT_PARENTHESIS && lt() != Types.EOF )
            {
                incr.add( expression() );

                if( lt() != Types.RIGHT_PARENTHESIS )
                {
                    consume( Types.COMMA );
                }
            }
        }

        consume( Types.RIGHT_PARENTHESIS );

        statement.add( header );
        statement.add( statementBody(false) );

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

    protected Reduction doWhileStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_DO).asReduction();
        statement.add( statementBody(false) );
        consume( Types.KEYWORD_WHILE );

        consume( Types.LEFT_PARENTHESIS );
        try
        {
            statement.add( expression() );
        }
        catch( SyntaxException e )
        {
            controller.addError( e );
            recover( Types.RIGHT_PARENTHESIS );
        }
        consume( Types.RIGHT_PARENTHESIS );

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

    protected Reduction whileStatement() throws SyntaxException, CompilationFailedException
    {
        Reduction statement = consume(Types.KEYWORD_WHILE).asReduction();

        consume( Types.LEFT_PARENTHESIS );

        try
        {
            statement.add( expression() );
        }
        catch( SyntaxException e )
        {
            controller.addError( e );
            recover( Types.RIGHT_PARENTHESIS );
        }
        consume( Types.RIGHT_PARENTHESIS );

        statement.add( statementBody(false) );
        return statement;

    }




  //---------------------------------------------------------------------------
  // PRODUCTIONS: EXPRESSIONS


   /**
    *  Processes a single (sub-)expression into a CSTNode.  No assumption is
    *  made about what follows the expression.
    *  <p>
    *  Note that the expression parser is rather stupid, in that it cannot
    *  resolve names.  Therefore it is little more than a pre-filter, removing
    *  statements that can't possibly be right, but leaving everything that
    *  might be right for semantic analysis by the <code>Analyzer</code> (which
    *  has access to the symbol table.  There was some thought given to eliminating
    *  the CSTs and going right to ASTs, but that option was rejected because
    *  inner classes mean that class name resolution won't work before parsing
    *  is complete.
    */

    protected CSTNode expression( ) throws SyntaxException, CompilationFailedException
    {
        // int id = nestCount++;
        // System.out.println( "ENTERING EXPRESSION " + id );

        ExpressionStack stack = new ExpressionStack( this );
        CSTNode expression = null;

        boolean bareMode = false;

        MAIN_LOOP: do
        {
            //
            // Valid at the start of an (sub)expression, a typed variable declaration
            // is handled separately.  It has the form

            //
            // In the SHIFT phase, we move stuff onto the stack that can have
            // multiple meanings and/or precedence issues, and leave the interpretation
            // for a later REDUCE.  No lookahead is used.  When structures are found that
            // have a consistent form, we use LL techniques (the new operator, for instance).

            Token next = la(stack);
            int type = next.getMeaningAs( EXPRESSION_SHIFT_HANDLERS );

            // System.out.println( "expression() status:" );
            // System.out.println( stack.toString() );
            // System.out.println( "next: " );
            // System.out.println( next.toString() );
            // System.out.println( la(2).toString() );

            SHIFT: switch( type )
            {
                case Types.GSTRING_START:
                {
                    if( stack.topIsAnExpression() )
                    {
                        error( "gstring cannot directly follow another expression" );
                    }

                    stack.push( gstring() );
                    break;
                }


                case Types.CREATABLE_PRIMITIVE_TYPE:
                {
                    stack.shiftIf( stack.atStartOfExpression(), "type name not valid in this context" );
                    break;
                }


                case Types.SIMPLE_EXPRESSION:
                {

                    //
                    // Method parameters don't make it here (see REDUCE)...

                    stack.shiftUnlessTopIsAnExpression( "literal cannot directly follow another expression" );
                    break;
                }


                case Types.KEYWORD_IDENTIFIER:
                {
                    if( stack.top().isA(Types.DEREFERENCE_OPERATOR) && stack.topIsAnOperator() )
                    {
                        la().setMeaning( Types.IDENTIFIER );
                        stack.shift();
                    }
                    else
                    {
                        error( "not valid as an identifier in this context" );
                    }
                    break;
                }


                case Types.ASSIGNMENT_OPERATOR:
                {
                    stack.shiftIf( stack.topIsAModifiableExpression(), "left-hand-side of assignment must be modifiable" );
                    break;
                }


                case Types.PREFIX_OR_INFIX_OPERATOR:
                {
                    if( stack.topIsAnOperator(0, true) )
                    {
                        Types.makePrefix( next, false );
                    }
                    stack.shift( );
                    break;
                }


                case Types.PREFIX_OPERATOR:
                {
                    Types.makePrefix( next, false );
                    stack.shift( );
                    break;
                }


                case Types.QUESTION:
                case Types.INFIX_OPERATOR:
                {
                    stack.shiftIfTopIsAnExpression( "infix operators may only follow expressions" );
                    break;
                }


                case Types.LEFT_PARENTHESIS:
                {
                    //
                    // Method calls don't make it here (see REDUCE).  It is
                    // either a sub-expression or a cast.

                    boolean condition = stack.atStartOfExpression() || (stack.topIsAnOperator() && !stack.top().isA(Types.DEREFERENCE_OPERATOR));
                    stack.shiftIf( condition, "sub-expression not valid at this position" );
                    break;
                }



                case Types.LEFT_CURLY_BRACE:
                {
                    if( stack.atStartOfExpression() || stack.topIsAnOperator() || stack.top().isA(Types.SYNTH_METHOD_CALL) )
                    {
                        stack.push( closureExpression() );
                    }
                    else
                    {
                        error( "closure not valid in this context" );
                    }
                    break;
                }


                case Types.LEFT_SQUARE_BRACKET:
                {
                    boolean isMap = false, insist = false;
                    if( stack.topIsAnExpression() )
                    {
                        insist = true;
                    }
                    stack.push( listOrMapExpression(isMap, insist) );
                    break;
                }


                case Types.KEYWORD_NEW:
                {
                    if( stack.atStartOfExpression() || stack.topIsAnOperator() )
                    {
                        stack.push( newExpression() );
                    }
                    else
                    {
                        error( "new can follow the start of an expression or another operator" );
                    }
                    break;
                }


                case Types.KEYWORD_INSTANCEOF:
                {
                    stack.shiftIf( stack.topIsAnExpression(), "instanceof may only follow an expression" );
                    break;
                }


                default:
                {

                    //
                    // All other operators are caught during REDUCE, so if it makes
                    // it here, it's either the end of the expression, or an error.

                    if( stack.size() == 1 && stack.topIsAnExpression() )
                    {
                        break MAIN_LOOP;                          // <<< FLOW CONTROL <<<<<<<<<
                    }
                    else
                    {
                        error();
                    }
                }


            }



            //
            // In the REDUCE phase, we try to find ways to convert several stack
            // elements (and maybe one lookahead token) into a single expression.
            // We retry the REDUCE as long as it succeeds.  Note that reductions
            // are ONLY possible when the top of the stack is an expression.

            boolean checkAgain = false, skipPatterns = false;
            CSTNode top0 = null, top1 = null, top2 = null;
            int nextPrecedence = 0, top1Precedence = 0;

            REDUCE: do
            {
                if( !stack.topIsAnExpression() && !ExpressionSupport.isAPotentialTypeName(stack.top(), false) )
                {
                    break;
                }


                //
                // We reduce at most once per iteration, so we collect info here.

                checkAgain   = false;
                skipPatterns = false;

                top0 = stack.top();
                top1 = stack.top(1);
                top2 = stack.top(2);

                next = la( stack );

                // System.out.println( "expression() stack for reduce: " + stack );
                // System.out.println( "expression() next token for reduce: " + next );

                nextPrecedence = Types.getPrecedence( next.getMeaning(), false );
                top1Precedence = Types.getPrecedence( top1.getMeaning(), false );



              //---------------------------------------------------------------
              // UNUSUAL STUFF FIRST


                //
                // Not really an operator at all, if top1 is a "(" and next is an ")",
                // we should reduce.  Extra processing is needed because the "(" is not
                // the type of an expression.

                if( top1.isA(Types.LEFT_PARENTHESIS) )
                {
                    if( next.isA(Types.RIGHT_PARENTHESIS) )
                    {
                        consume();

                        //
                        // To simplify things, cast operators MUST appear on the same line
                        // as the start of their operands.  Without name lookup, we can't
                        // be sure that even things that look like casts are, but we assume
                        // they are and let later phases correct, where necessary.

                        next = la(true); // XXX the precludes is true for GString. Seems wrong
                        boolean castPrecluded = next.isA(Types.NEWLINE) || next.isA(Types.PRECLUDES_CAST_OPERATOR);

                        if( ExpressionSupport.isAPotentialTypeName(top0, false) && !castPrecluded )
                        {
                            CSTNode   name = stack.pop();
                            Reduction cast = ((Token)stack.pop()).asReduction( name );
                            cast.setMeaning( Types.SYNTH_CAST );
                            stack.push( cast );
                        }
                        else
                        {
                            CSTNode subexpression = stack.pop();
                            stack.pop();
                            stack.push( subexpression );
                        }

                        checkAgain = true;
                        continue;                             // <<< LOOP CONTROL <<<<<<<<<
                    }
                    else
                    {
                        skipPatterns = true;
                    }
                }


                //
                // Highest precedence: "new".  If it is preceeded on the stack by
                // a ".", what preceeds the "." is the context for the new, and
                // we'll have to do some rewriting....  Note that SHIFT will only
                // shift a "new" if it is preceeded by nothing or an operator,
                // and it will only shift a "." if it is preceeded by an expression.
                // Therefore, we can assume any preceeding "." is an operator.

                if( top0.isA(Types.KEYWORD_NEW) && !top0.isAnExpression() )
                {
                    top0.markAsExpression();

                    if( top1.isA(Types.DOT) )
                    {
                        CSTNode theNew  = stack.pop();
                        CSTNode theDot  = stack.pop();
                        CSTNode context = stack.pop();

                        theNew.set( 1, context );
                        stack.push( theNew );

                        checkAgain = true;
                        continue;                             // <<< LOOP CONTROL <<<<<<<<<
                    }
                }


                //
                // Not unusual, but handled here to simplify precendence handling for
                // the rest of the unusual stuff: dereference operators are left-associative.

                if( top1.isA(Types.DEREFERENCE_OPERATOR) && !top0.hasChildren() )
                {
                    stack.reduce( 3, 1, true );

                    checkAgain = true;
                    continue;                                 // <<< LOOP CONTROL <<<<<<<<<
                }



                //
                // Next precedence, array offsets.  Because we allow lists and ranges
                // and such inside list expressions, all lists will have been processed
                // to a SYNTH_LISTH during SHIFT.  Here we do some rewriting, where
                // necessary.  Empty array offsets are only allowed on types, and we
                // run the appropriate conversion in that case.

                if( top0.isA(Types.SYNTH_LIST) && top1.isAnExpression() || ExpressionSupport.isAPotentialTypeName(top1, false) )
                {
                    //
                    // Empty list is an array type

                    if( !top0.hasChildren() )
                    {
                        boolean typePreceeds   = ExpressionSupport.isAPotentialTypeName(top1, false);
                        boolean potentialCast  = top2.isA(Types.LEFT_PARENTHESIS);
                        boolean potentialDecl  = top2.isA(Types.LEFT_PARENTHESIS) || top2.isA(Types.UNKNOWN);
                        boolean classReference = next.isA(Types.DOT) && la(2).isA(Types.KEYWORD_CLASS);
                        if( !(typePreceeds && (potentialCast || potentialDecl || classReference)) )
                        {
                            error( "empty square brackets are only valid on type names" );
                        }

                        //
                        // Okay, we have an array type.  We now convert the list and
                        // expression to an array type, and slurp any further dimensions
                        // off the lookahead.

                        Reduction array = stack.pop().asReduction();
                        array.setMeaning( Types.LEFT_SQUARE_BRACKET );
                        array.add( stack.pop() );

                        while( lt(true) == Types.LEFT_SQUARE_BRACKET )
                        {
                            array = consume( Types.LEFT_SQUARE_BRACKET ).asReduction( array );
                            consume( Types.RIGHT_SQUARE_BRACKET );
                        }


                        //
                        // One last decision: variable type declaration, or
                        // cast, or class reference...

                        if( classReference )
                        {
                            CSTNode reference = consume(Types.DOT).asReduction(array, consume(Types.KEYWORD_CLASS));
                            reference.markAsExpression();
                            stack.push( reference );

                        }
                        else if( lt(true) == Types.IDENTIFIER && lt(2) == Types.EQUAL )
                        {
                            stack.push( variableDeclarationExpression(array) );
                        }
                        else if( stack.top().isA(Types.LEFT_PARENTHESIS) && la(true).isA(Types.RIGHT_PARENTHESIS) )
                        {
                            CSTNode cast = ((Token)stack.pop()).asReduction( array );
                            cast.setMeaning( Types.SYNTH_CAST );
                            stack.push( cast );
                            consume( Types.RIGHT_PARENTHESIS );
                        }
                        else
                        {
                            error( "found array type where none expected" );
                        }
                    }


                    //
                    // Non-empty list is an offset (probably)

                    else
                    {
                        CSTNode list = stack.pop();
                        CSTNode base = stack.pop();

                        Reduction result = ((Token)list.get(0)).dup().asReduction();
                        result.setMeaning( Types.LEFT_SQUARE_BRACKET );
                        result.add( base );

                        if( list.children() == 1 )
                        {
                            result.add( list.get(1) );
                        }
                        else
                        {
                            result.add( list );
                        }

                        result.markAsExpression();
                        stack.push( result );
                    }

                    checkAgain = true;
                    continue;                                 // <<< LOOP CONTROL <<<<<<<<<

                }



                //
                // Next precedence: typed variable declarations.  If the top of stack
                // isAPotentialTypeName(), la(true) is an identifier, and la(2) is
                // an "=", it's a declaration.

                if( la(true).isA(Types.IDENTIFIER) && lt(2) == Types.EQUALS && ExpressionSupport.isAPotentialTypeName(top0, false) )
                {
                    stack.push( variableDeclarationExpression(stack.pop()) );

                    checkAgain = true;
                    continue;                                 // <<< LOOP CONTROL <<<<<<<<<
                }


                //
                // Before getting to method call handling proper, we should check for any
                // pending bookkeeping.  If the top of stack is a closure and the element
                // before it is a method call, the closure is either one of its parameters
                // or an error.

                if( top1.isA(Types.SYNTH_METHOD_CALL) && top0.isA(Types.SYNTH_CLOSURE) )
                {
                    CSTNode parameters = top1.get(2);

                    int last = parameters.size() - 1;
                    if( last > 0 && parameters.get(last).isA(Types.SYNTH_CLOSURE) )
                    {
                        error( "you may only pass one closure to a method implicitly" );
                    }

                    parameters.add( stack.pop() );

                    checkAgain = true;
                    continue;                                 // <<< LOOP CONTROL <<<<<<<<<
                }


                //
                // Next precedence: method calls and typed declarations.  If the top of stack
                // isInvokable() and la(stack) is an "(", an "{", or a simple expression, it's
                // a method call.  We leave the closure for the next SHIFT.

                if( ExpressionSupport.isInvokable(top0) && (next.isA(Types.LEFT_CURLY_BRACE) || la(true).isA(Types.METHOD_CALL_STARTERS)) )
                {
                    // System.out.println( "making a method call of " + top0 );

                    CSTNode name = stack.pop();

                    Reduction method = null;
                    switch( next.getMeaning() )
                    {
                        case Types.LEFT_PARENTHESIS:
                            method = consume().asReduction();
                            method.add( name );
                            method.add( la().isA(Types.RIGHT_PARENTHESIS) ? Reduction.newContainer() : parameterList() );
                            consume( Types.RIGHT_PARENTHESIS );
                            break;

                        case Types.LEFT_CURLY_BRACE:
                            method = Token.newSymbol( Types.LEFT_PARENTHESIS, next.getStartLine(), next.getStartColumn() ).asReduction();
                            method.add( name );
                            method.add( Reduction.newContainer() );
                            break;

                        default:
                            method = Token.newSymbol( Types.LEFT_PARENTHESIS, next.getStartLine(), next.getStartColumn() ).asReduction();
                            method.add( name );
                            method.add( parameterList() );
                            break;
                    }

                    method.setMeaning( Types.SYNTH_METHOD_CALL );
                    method.markAsExpression();

                    stack.push( method );

                    if( lt() != Types.LEFT_CURLY_BRACE )
                    {
                        checkAgain = true;
                    }

                    continue;                                 // <<< LOOP CONTROL <<<<<<<<<
                }


                //
                // Handle postfix operators next.  We have to check for acceptable
                // precedence before doing it.  All the higher precedence reductions
                // have already been checked.

                if( next.isA(Types.POSTFIX_OPERATOR) && stack.topIsAnExpression() )
                {
                    if( !ExpressionSupport.isAVariable(stack.top()) )
                    {
                        error( "increment/decrement operators can only be applied to variables" );
                    }

                    Types.makePostfix( next, true );

                    stack.shift();
                    stack.reduce( 2, 0, true );

                    checkAgain = true;
                    continue;                                 // <<< LOOP CONTROL <<<<<<<<<
                }


                //
                // The ternary operator will be seen twice.  The first reduction is
                // infix when there is a ":" on lookahed.  The second reduction is
                // prefix when there is a lower-precedence operator on lookahed.
                // The ternary operator is right-associative.  Note that
                // Types.getPrecedence() on a ternary operator returns 10.

                if( top1.isA(Types.QUESTION) )
                {
                    boolean reduce = false;

                    if( la().isA(Types.COLON) )
                    {
                        if( top1.hasChildren() )
                        {
                            error( "ternary operator can have only three clauses" );
                        }

                        consume();
                        stack.reduce( 3, 1, false );
                        checkAgain = true;
                    }
                    else if( Types.getPrecedence(next.getMeaning(), false) < 10 )
                    {
                        stack.reduce( 2, 1, false );
                        stack.top().setMeaning( Types.SYNTH_TERNARY );
                        checkAgain = true;
                    }


                    continue;                                 // <<< LOOP CONTROL <<<<<<<<<
                }




              //---------------------------------------------------------------
              // PATTERN STUFF SECOND


                //
                // Note that because of the loop control above, we get here only if none
                // of the above options matched.
                //
                // So, everything else we handle generically: top1 will be an operator, and
                // will be reduced or not with top0 and possibly top2, depending on the
                // cardinality and associativity of the operator, and the type of la().

                if( skipPatterns || !ExpressionSupport.isAnOperator(top1, false) )
                {
                    break;                                    // <<< LOOP CONTROL <<<<<<<<<
                }


                switch( top1.getMeaningAs(EXPRESSION_REDUCE_HANDLERS) )
                {
                    //
                    // Prefix increment/decrement operators aren't always valid, so we
                    // handle the separately from the other prefix operators.

                    case Types.PREFIX_PLUS_PLUS:
                    case Types.PREFIX_MINUS_MINUS:
                    {
                        if( nextPrecedence < top1Precedence )
                        {
                            if( !ExpressionSupport.isAVariable(stack.top()) )
                            {
                                error( "increment/decrement operators can only be applied to variables" );
                            }

                            stack.reduce( 2, 1, true );
                            checkAgain = true;
                       }

                       break;
                    }


                    //
                    // All other prefix operators.  They are all right-associative.

                    case Types.PURE_PREFIX_OPERATOR:
                    {
                        if( nextPrecedence < top1Precedence )
                        {
                            stack.reduce( 2, 1, true );
                            checkAgain = true;
                        }

                        break;
                    }


                    //
                    // Handle the assignment operators.  They are all right-associative.

                    case Types.ASSIGNMENT_OPERATOR:
                    {
                        if( nextPrecedence < top1Precedence )
                        {
                            stack.reduce( 3, 1, true );
                            checkAgain = true;
                        }

                        break;
                    }


                    //
                    // Handle the instenceof keyword.  The rhs has to be a potential type.

                    case Types.KEYWORD_INSTANCEOF:
                    {
                        if( nextPrecedence < top1Precedence )
                        {
                            if( !ExpressionSupport.isAPotentialTypeName(top0, false) )
                            {
                                error( "instanceof right-hand side must be a valid type name" );
                            }

                            stack.reduce( 3, 1, true );
                            checkAgain = true;
                        }

                        break;
                    }


                    //
                    // Handle all other infix operators.  They are all left-associative.

                    case Types.INFIX_OPERATOR:
                    {
                        if( nextPrecedence <= top1Precedence )
                        {
                            stack.reduce( 3, 1, true );
                            checkAgain = true;
                        }

                        break;
                    }


                    //
                    // Anything else in top1 is an bug.

                    default:
                    {
                        throw new GroovyBugError( "found unexpected token during REDUCE [" + top1.getMeaning() + "]" );
                    }
                }

            } while( checkAgain );

        } while( true );


        if( stack.size() == 1 && stack.topIsAnExpression() )
        {
            expression = stack.pop();
        }
        else
        {
            error( "expression incomplete" );
        }


        // System.out.println( "EXITING EXPRESSION " + id );
        return expression;
    }


    private static final int EXPRESSION_SHIFT_HANDLERS[] = {
          Types.GSTRING_START
        , Types.CREATABLE_PRIMITIVE_TYPE
        , Types.SIMPLE_EXPRESSION
        , Types.KEYWORD_IDENTIFIER
        , Types.ASSIGNMENT_OPERATOR
        , Types.PREFIX_OR_INFIX_OPERATOR
        , Types.PREFIX_OPERATOR
        , Types.QUESTION
        , Types.INFIX_OPERATOR
        , Types.LEFT_PARENTHESIS
        , Types.LEFT_CURLY_BRACE
        , Types.LEFT_SQUARE_BRACKET
        , Types.KEYWORD_NEW
        , Types.KEYWORD_INSTANCEOF
    };

    private static final int EXPRESSION_REDUCE_HANDLERS[] = {
          Types.PREFIX_PLUS_PLUS
        , Types.PREFIX_MINUS_MINUS
        , Types.PURE_PREFIX_OPERATOR
        , Types.ASSIGNMENT_OPERATOR
        , Types.KEYWORD_INSTANCEOF
        , Types.INFIX_OPERATOR
    };



   /**
    *  Processes a typed variable declaration.  Without the type, it's a
    *  assignment expression instead (no comma support).  The datatype
    *  has already been identified and is passed in.
    *  <p>
    *  Grammar: <pre>
    *     variableDeclarationExpression
    *        = datatype (nameDeclaration "=" expression)
    *                   ("," nameDeclaration "=" expression)*
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     statement   = { :<SYNTH_VARIABLE_DECLARATION> datatype declaration+ }
    *     declaration = { <identifier> expression }
    *
    *     see expression()
    *  </pre>
    */

    protected Reduction variableDeclarationExpression( CSTNode datatype ) throws SyntaxException, CompilationFailedException
    {
        Reduction expression = ((Token)datatype.get(0)).dup().asReduction( datatype );  // done for line number on SYNTH
        expression.setMeaning( Types.SYNTH_VARIABLE_DECLARATION );

        boolean done = false;
        do
        {
            try
            {
                Reduction declaration = (Reduction)expression.add( nameDeclaration(false).asReduction() );
                consume( Types.EQUAL );
                declaration.add( expression() );
            }
            catch( SyntaxException e )
            {
                controller.addError( e );
                recover( Types.ANY_END_OF_STATEMENT );
            }

            if( lt() == Types.COMMA )
            {
                consume( Types.COMMA );
            }
            else
            {
                done = true;
            }

        } while( !done );


        return expression;
    }



   /**
    *  Processes a GString.
    *  <p>
    *  Grammar: <pre>
    *     gstring = (<text>? "$" "{" expression "}" <text>?)*
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     gstring = { <full-text>:SYNTH_GSTRING (segment|expression)* }
    *
    *     see expression()
    *  </pre>
    */

    protected Reduction gstring() throws SyntaxException, CompilationFailedException
    {
        // int id = nestCount++;
        // System.out.println( "ENTERING GSTRING " + id );

        Reduction data = Reduction.newContainer();

        consume( Types.GSTRING_START );

        while( lt() != Types.GSTRING_END && lt() != Types.EOF )
        {
            switch( lt() )
            {
                case Types.STRING:
                    data.add( consume() );
                    break;

                case Types.GSTRING_EXPRESSION_START:
                    consume();
                    data.add( expression() );
                    consume( Types.GSTRING_EXPRESSION_END );
                    break;

                default:
                    throw new GroovyBugError( "gstring found invalid token: " + la() );
            }
        }

        Reduction complete = consume( Types.GSTRING_END ).asReduction();
        complete.addChildrenOf( data );

        complete.setMeaning( Types.SYNTH_GSTRING );

        // System.out.println( "EXITING GSTRING " + id );
        return complete;
    }




   /**
    *  Processes a NON-EMPTY parameter list, as supplied on either a method invokation or
    *  a closure invokation.  Reads parameters until something that doesn't belong
    *  is found.
    *  <p>
    *  Grammar: <pre>
    *     parameterList = (regular "," named) | named
    *     regular = parameter ("," parameter)*
    *     named   = nameReference ":" parameter ("," nameReference ":" parameter)*
    *
    *     parameter = expression
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     parameterList = { <null> regular* named* }
    *     regular = expression
    *     named   = { ":" <identifier> expression }
    *
    *     see expression()
    *  </pre>
    */

    protected Reduction parameterList() throws SyntaxException, CompilationFailedException
    {
        // int id = nestCount++;
        // System.out.println( "ENTERING PARAMETER LIST " + id );

        Reduction list  = Reduction.newContainer();
        Reduction named = null;

        boolean done = false;

        do
        {
            if( la().canMean(Types.IDENTIFIER) && la(2).isA(Types.COLON) )
            {
                if( named == null )
                {
                    named = Token.newPlaceholder(Types.SYNTH_MAP).asReduction();
                    list.add( named );
                }

                Token name = nameReference(false);
                name.setMeaning( Types.STRING );

                named.add( consume(Types.COLON).asReduction(name, expression()) );
            }
            else
            {
                list.add( expression() );
            }


            if( lt() == Types.COMMA )
            {
                consume();
            }
            else
            {
                done = true;
            }


        } while( !done );

        // System.out.println( "EXITING PARAMETER LIST " + id );
        return list;
    }



   /**
    *  Processes a "new" expression.  Handles optional constructors, array
    *  initializations, closure arguments, and anonymous classes.  In order
    *  to support anonymous classes, anonymous closures are not allowed.
    *  <p>
    *  Grammar: <pre>
    *     newExpression = "new" scalarDatatype (array|init)?
    *     array = ( "[" expression "]" )+ | ( ("[" "]")+ newArrayInitializer )
    *     init  = "(" parameterList? ")" (typeBody | closureExpression)?
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     new = { "new" arrayType     dimensions newArrayInitializer? }
    *         | { "new" scalarDataype (parameterList|{<null>}) typeBody? }
    *
    *     arrayType  = { "{" (arrayType | scalarDatatype) }
    *     dimensions = { <null> expression+ } | {}
    *
    *     see expression()
    *     see scalarDatatype()
    *     see typeBody()
    *  </pre>
    */

    protected Reduction newExpression() throws SyntaxException, CompilationFailedException
    {
        // int id = nestCount++;
        // System.out.println( "ENTERING NEW " + id );

        Reduction expression = consume(Types.KEYWORD_NEW).asReduction();
        CSTNode   scalarType = scalarDatatype(false);

        if( lt(true) == Types.LEFT_SQUARE_BRACKET )
        {
            //
            // First up, figure out the actual type and any
            // stated dimensions.

            boolean   implicit   = (lt(2) == Types.RIGHT_SQUARE_BRACKET);
            Reduction dimensions = implicit ? Reduction.EMPTY : Reduction.newContainer();
            int       count      = 0;
            CSTNode   arrayType  = scalarType;

            while( lt(true) == Types.LEFT_SQUARE_BRACKET )
            {
                arrayType = consume(Types.LEFT_SQUARE_BRACKET).asReduction( arrayType );
                count++;

                if( !implicit )
                {
                    dimensions.add( expression() );
                }

                consume(Types.RIGHT_SQUARE_BRACKET);
            }

            expression.add( arrayType );
            expression.add( dimensions );

            //
            // If implicit, there must be initialization data

            if( implicit )
            {
                expression.add( tupleExpression(0, count) );
            }

        }

        else
        {
            expression.add( scalarType );


            //
            // Process the constructor call

            Reduction parameters = null;

            consume( Types.LEFT_PARENTHESIS );
            parameters = (lt() == Types.RIGHT_PARENTHESIS ? Reduction.newContainer() : parameterList());
            consume( Types.RIGHT_PARENTHESIS );

            expression.add( parameters );


            //
            // If a "{" follows, it's a class body or a closure...

            if( lt() == Types.LEFT_CURLY_BRACE )
            {
                if( lt(2) == Types.PIPE || lt(2) == Types.DOUBLE_PIPE )
                {
                    parameters.add( closureExpression() );
                }
                else
                {
                    expression.add( typeBody(true, false, false) );
                }
            }
        }

        // System.out.println( "EXITING NEW " + id );
        return expression;
    }



   /**
    *  Processes a "new" array initializer expression.
    *  <p>
    *  Grammar: <pre>
    *     tupleExpression = "{" (tupleExpression | (expression ("," expression))? "}"
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     initializer = { "{":SYNTH_TUPLE (initializer*|expression*) }
    *
    *     see expression()
    *  </pre>
    */

    protected Reduction tupleExpression( int level, int depth ) throws SyntaxException, CompilationFailedException
    {
        Reduction data = consume(Types.LEFT_CURLY_BRACE).asReduction();
        data.setMeaning( Types.SYNTH_TUPLE );

        if( lt() != Types.RIGHT_CURLY_BRACE )
        {
            int    child = level + 1;
            boolean leaf = (child == depth);

            do
            {
                data.add( leaf ? expression() : tupleExpression(child, depth) );

            } while( lt() == Types.COMMA && (consume() != null) );
        }

        consume( Types.RIGHT_CURLY_BRACE );

        return data;
    }



   /**
    *  Processes a closure expression.
    *  <p>
    *  Grammar: <pre>
    *     closureExpression = "{" parameters statement* "}"
    *     parameters = ("|" parameterDeclarationList "|")?
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     initializer = { "{":SYNTH_CLOSURE parameters statements }
    *     parameters  = parameterDeclarationList | { <null> }
    *     statements  = { <null> statement* }
    *
    *     see parameterDeclarationList()
    *     see statement()
    *  </pre>
    */

    protected Reduction closureExpression( ) throws SyntaxException, CompilationFailedException
    {
        // int id = nestCount++;
        // System.out.println( "ENTERING CLOSURE EXPRESSION " + id );

        Reduction closure = consume(Types.LEFT_CURLY_BRACE).asReduction();
        closure.setMeaning( Types.SYNTH_CLOSURE );
        boolean specified = (lt() == Types.PIPE) || (lt() == Types.DOUBLE_PIPE);

        //
        // DEPRECATED: the old syntax for parameters had a | only
        // at the end of the parameter list.  The new syntax has
        // two pipes or none.  For now, we attempt to support the
        // old syntax.  It can mistake a variable declaration
        // for a parameter declaration, though, so it may cause more
        // trouble than it's worth.  This if() and the one below
        // (also marked) should be removed before v1.0.

        if( !specified )
        {
            getTokenStream().checkpoint();
            CSTNode type = optionalDatatype( true, false );
            if( lt() == Types.IDENTIFIER && (lt(2) == Types.PIPE || lt(2) == Types.COMMA) )
            {
                specified = true;
            }

            getTokenStream().restore();
        }


        //
        // If the parameter list is specified, process it.

        if( specified )
        {
            if( lt() == Types.DOUBLE_PIPE )
            {
                consume( Types.DOUBLE_PIPE );
                closure.add( Reduction.newContainer() );
            }
            else
            {
                //
                // DEPRECATED: further support for note above, this consume()
                // should not be conditional after the above code is removed.

                if( lt() == Types.PIPE )
                {
                    consume(Types.PIPE);
                }

                closure.add( parameterDeclarationList() );
                consume(Types.PIPE);
            }
        }
        else
        {
            closure.add( Reduction.newContainer() );
        }


        //
        // Finally, process the statements.

        closure.add( statementsUntilRightCurly() );
        consume( Types.RIGHT_CURLY_BRACE );

        // System.out.println( "EXITING CLOSURE EXPRESSION " + id );
        return closure;
    }



   /**
    *  Processes a list or map expression.
    *  <p>
    *  Grammar: <pre>
    *     listOrMapExpression = list | map
    *
    *     list = "[" (expression ("," expression)*)? "]"
    *
    *     map     = "[" (":" | mapping+) "]"
    *     mapping = expression ":" expression
    *  </pre>
    *  <p>
    *  CST: <pre>
    *     list    = { "[":SYNTH_LIST expression* }
    *     map     = { "[":SYNTH_MAP  mapping* }
    *     mapping = { ":" expression expression }
    *
    *     see expression()
    *  </pre>
    */

    protected Reduction listOrMapExpression( boolean isMap, boolean insist ) throws SyntaxException, CompilationFailedException
    {
        Reduction expression = consume(Types.LEFT_SQUARE_BRACKET).asReduction();
        expression.setMeaning( Types.SYNTH_LIST );

        if( lt() == Types.COLON )
        {
            if( !isMap && insist )
            {
                error( "expected list" );
            }

            isMap = true;
            expression.setMeaning( Types.SYNTH_MAP );
            consume();
            if( lt() != Types.RIGHT_SQUARE_BRACKET )
            {
                error( "expected empty map" );
            }
        }


        //
        // Process the data.  On the first one, check if we are
        // processing a map.  We assume not going in, as the empty
        // map isn't relevant...

        boolean done = (lt() == Types.RIGHT_SQUARE_BRACKET);

        while( !done )
        {
            CSTNode element = expression();

            if( !insist )
            {
                insist = true;
                if( lt() == Types.COLON )
                {
                    isMap = true;
                    expression.setMeaning(Types.SYNTH_MAP);
                }
            }

            if( isMap )
            {
                element = consume(Types.COLON).asReduction( element, expression() );
            }

            expression.add( element );

            if( lt() == Types.COMMA ) { consume(); } else { done = true; }
        }

        consume(Types.RIGHT_SQUARE_BRACKET);

        return expression;
    }



   /**
    *  Synonym for <code>listOrMapExpression( false, false )</code>.
    */

    protected Reduction listOrMapExpression( ) throws SyntaxException, CompilationFailedException
    {
        return listOrMapExpression( false, false );
    }






  //---------------------------------------------------------------------------
  // ERROR REPORTING


   /**
    *  Reports an error assembled from parts.
    */

    protected UnexpectedTokenException error( Token found, int[] expectedTypes, boolean throwIt, String comment ) throws SyntaxException
    {
        UnexpectedTokenException e = new UnexpectedTokenException( found, expectedTypes, comment );

        if( throwIt )
        {
            throw e;
        }

        return e;
    }


   /**
    *  Reports an error by generating and optionally throwing an
    *  <code>UnexpectedTokenException</code>.
    */

    protected UnexpectedTokenException error( int[] expectedTypes, boolean throwIt, int k, String comment ) throws SyntaxException, CompilationFailedException
    {
        return error( la(k), expectedTypes, throwIt, comment );
    }



   /**
    *  A synonym for <code>error( expectedTypes, throwIt, k, null )</code>.
    */

    protected UnexpectedTokenException error( int[] expectedTypes, boolean throwIt, int k ) throws SyntaxException, CompilationFailedException
    {
        return error( expectedTypes, throwIt, k, null );
    }



   /**
    *  A synonym for <code>error( expectedTypes, true, 1, null )</code>.
    */

    protected void error( int[] expectedTypes ) throws SyntaxException, CompilationFailedException
    {
        throw error( expectedTypes, false, 1, null );
    }



   /**
    *  A synonym for <code>error( null, true, 1, null )</code>.
    */

    protected void error() throws SyntaxException, CompilationFailedException
    {
        throw error( null, true, 1, null );
    }



   /**
    *  A synonym for <code>error( null, true, 1, comment )</code>.
    */

    protected void error( String comment ) throws SyntaxException, CompilationFailedException
    {
        throw error( null, true, 1, comment );
    }



   /**
    *  A synonym for <code>error( found, null, true, comment )</code>.
    */

    protected void error( Token found, String comment ) throws SyntaxException
    {
        throw error( found, null, true, comment );
    }



   /**
    *  A scalar synonym of <code>error( expectedTypes )</code>.
    */

    protected void error( int expectedType ) throws SyntaxException, CompilationFailedException
    {
        error( new int[] { expectedType } );
    }




  //---------------------------------------------------------------------------
  // ERROR RECOVERY


   /**
    *  Attempts to recover from an error by discarding input until a
    *  known token is found.  It further guarantees that /at least/
    *  one token will be eaten.
    */

    public void recover( int[] safe, boolean ignoreNewlines ) throws SyntaxException, CompilationFailedException
    {
        Token leading = la( ignoreNewlines );

        while( true )
        {
            Token next = la( ignoreNewlines );
            if( next.isA(Types.EOF) || next.isOneOf(safe) )
            {
                break;
            }
            else
            {
                consume( ignoreNewlines );
            }
        }

        if( la(ignoreNewlines) == leading )
        {
            consume( ignoreNewlines );
        }
    }



   /**
    *  A scalar version of <code>recover( int[], boolean )</code>.
    */

    public void recover( int safe, boolean ignoreNewlines ) throws SyntaxException, CompilationFailedException
    {
        Token leading = la( ignoreNewlines );

        while( true )
        {
            Token next = la( ignoreNewlines );
            if( next.isA(Types.EOF) || next.isA(safe) )
            {
                break;
            }
            else
            {
                consume( ignoreNewlines );
            }
        }

        if( la(ignoreNewlines) == leading )
        {
            consume( ignoreNewlines );
        }
    }



   /**
    *  A synonym for <code>recover( safe, false )</code>.
    */

    public void recover( int[] safe ) throws SyntaxException, CompilationFailedException
    {
        recover( safe, false );
    }



   /**
    *  A synonm for the scalar <code>recover( safe, false )</code>.
    */

    public void recover( int safe ) throws SyntaxException, CompilationFailedException
    {
        recover( safe, false );
    }



   /**
    *  A synonym for <code>recover( Types.ANY_END_OF_STATMENT, true )</code>.
    */

    public void recover( ) throws SyntaxException, CompilationFailedException
    {
        recover( Types.ANY_END_OF_STATEMENT, true );
    }




  //---------------------------------------------------------------------------
  // TOKEN LOOKAHEAD


   /**
    *  Returns (without consuming) the next kth token in the underlying
    *  token stream.  You can make newlines significant as needed.
    *  Returns Token.EOF on end of stream.  k is counted from 1.
    */

    protected Token la( int k, boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {
        Token token = Token.NULL;

        //
        // Count down on k while counting up on streamK.
        // NOTE: k starting at less than 1 is a mistake...
        // This routine will reliably NOT return Token.NULL
        // /unless/ it is actually in the stream.

        try
        {
            int streamK = 1;
            while( k > 0 && token.getMeaning() != Types.EOF )
            {
                token = getTokenStream().la( streamK );
                streamK += 1;

                if( token == null  )
                {
                    token = Token.EOF;
                }
                else if( token.getMeaning() == Types.NEWLINE )
                {
                    if( significantNewlines )
                    {
                        k -= 1;
                    }
                }
                else
                {
                    k -= 1;
                }
            }
        }
        catch( ReadException e )
        {
            controller.addFatalError( new SimpleMessage(e.getMessage()) );
        }

        return token;
    }



   /**
    *  Synonym for <code>la( k, false )</code>.
    */

    protected Token la( int k ) throws SyntaxException, CompilationFailedException
    {
        return la( k, false );
    }



   /**
    *  Synonym for <code>la( 1, significantNewlines )</code>.
    */

    protected Token la( boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {
        return la( 1, significantNewlines );
    }



   /**
    * Synonym for <code>la( 1, false )</code>.
    */

    protected Token la() throws SyntaxException, CompilationFailedException
    {
        return la( 1, false );
    }



   /**
    *  Special <code>la()</code> used by the expression parser.  It will get the next token
    *  in the current statement.  If the next token is past a line boundary and might be
    *  the start of the next statement, it won't cross the line to get it.
    */

    protected Token la( ExpressionStack stack ) throws SyntaxException, CompilationFailedException
    {
        Token next = la();

        if( stack.canComplete() && next.isA(Types.UNSAFE_OVER_NEWLINES) )
        {
            if( la(true).getMeaning() == Types.NEWLINE )
            {
                next = la(true);
            }
        }

        return next;
    }




   /**
    *  Returns the meaning of the <code>la( k, significantNewlines )</code> token.
    */

    protected int lt( int k, boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {
        return la(k, significantNewlines).getMeaning();
    }



   /**
    *  Returns the meaning of the <code>la( k )</code> token.
    */

    protected int lt( int k ) throws SyntaxException, CompilationFailedException
    {
        return la(k).getMeaning();
    }



   /**
    *  Returns the meaning of the <code>la( significantNewlines )</code> token.
    */

    protected int lt( boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {
        return la(significantNewlines).getMeaning();
    }



   /**
    *  Returns the meaning of the <code>la()</code> token.
    */

    protected int lt() throws SyntaxException, CompilationFailedException
    {
        return la().getMeaning();
    }




  //---------------------------------------------------------------------------
  // TOKEN CONSUMPTION


   /**
    *  Consumes (and returns) the next token if it is of the specified type.
    *  If <code>significantNewlines</code> is set, newlines will not automatically
    *  be consumed; otherwise they will.  Throws <code>UnexpectedTokenException</code>
    *  if the type doesn't match.
    */

    protected Token consume( int type, boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {
        try
        {
            if( !la(significantNewlines).isA(type) )
            {
                error( type );
            }

            if( !significantNewlines )
            {
                while( lt(true) == Types.NEWLINE )
                {
                    getTokenStream().consume(Types.NEWLINE);
                }
            }

            return getTokenStream().consume(type);
        }
        catch( ReadException e )
        {
            controller.addFatalError( new SimpleMessage(e.getMessage()) );
        }

        throw new GroovyBugError( "this should never happen" );
    }



   /**
    *  A synonym for <code>consume( type, false )</code>.  If type is Types.NEWLINE,
    *  equivalent to <code>consume( Types.NEWLINE, true )</code>.
    */

    protected Token consume( int type ) throws SyntaxException, CompilationFailedException
    {
        return consume( type, type == Types.NEWLINE );
    }



   /**
    *  A synonym for <code>consume( Types.ANY, false )</code>.
    */

    protected Token consume() throws SyntaxException, CompilationFailedException
    {
        return consume( lt(), false );
    }



   /**
    *  A synonym for <code>consume( Types.ANY, significantNewlines )</code>.
    *  If you pass true, it will consume exactly the next token from the
    *  stream.
    */

    protected Token consume( boolean significantNewlines ) throws SyntaxException, CompilationFailedException
    {
        return consume( lt(significantNewlines), significantNewlines );
    }

}
