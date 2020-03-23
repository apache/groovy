/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

header {
package org.codehaus.groovy.antlr.parser;

import java.io.*;
import java.util.*;

import antlr.CommonToken;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;
import antlr.TokenStreamRecognitionException;

import org.codehaus.groovy.antlr.*;
}

/** JSR-241 Groovy Recognizer.
 *
 * Run 'java Main [-showtree] directory-full-of-groovy-files'
 *
 * [The -showtree option pops up a Swing frame that shows
 *  the AST constructed from the parser.]
 *
 * Contributing authors:
 *              John Mitchell           johnm@non.net
 *              Terence Parr            parrt@magelang.com
 *              John Lilley             jlilley@empathy.com
 *              Scott Stanchfield       thetick@magelang.com
 *              Markus Mohnen           mohnen@informatik.rwth-aachen.de
 *              Peter Williams          pete.williams@sun.com
 *              Allan Jacobs            Allan.Jacobs@eng.sun.com
 *              Steve Messick           messick@redhills.com
 *              James Strachan          jstrachan@protique.com
 *              John Pybus              john@pybus.org
 *              John Rose               rose00@mac.com
 *              Jeremy Rayner           groovy@ross-rayner.com
 *              Alex Popescu            the.mindstorm@gmail.com
 *              Martin Kempf            mkempf@hsr.ch
 *              Reto Kleeb              rkleeb@hsr.ch
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *              fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *              added tree construction
 *              fixed definition of WS,comments for mac,pc,unix newlines
 *              added unary plus
 * Version 1.11 (Nov 20, 1998)
 *              Added "shutup" option to turn off last ambig warning.
 *              Fixed inner class def to allow named class defs as statements
 *              synchronized requires compound not simple statement
 *              add [] after builtInType DOT class in primaryExpression
 *              "const" is reserved but not valid..removed from modifiers
 * Version 1.12 (Feb 2, 1999)
 *              Changed LITERAL_xxx to xxx in tree grammar.
 *              Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *              Didn't have (stat)? for else clause in tree parser.
 *              Didn't gen ASTs for interface extends.  Updated tree parser too.
 *              Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *              Allowed final/abstract on local classes.
 *              Removed local interfaces from methods
 *              Put instanceof precedence where it belongs...in relationalExpr
 *                      It also had expr not type as arg; fixed it.
 *              Missing ! on SEMI in classBlock
 *              fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *              fixed: didn't like Object[].class in parser or tree parser
 * Version 1.15 (Jun 26, 1999)
 *              Screwed up rule with instanceof in it. :(  Fixed.
 *              Tree parser didn't like (expr).something; fixed.
 *              Allowed multiple inheritance in tree grammar. oops.
 * Version 1.16 (August 22, 1999)
 *              Extending an interface built a wacky tree: had extra EXTENDS.
 *              Tree grammar didn't allow multiple superinterfaces.
 *              Tree grammar didn't allow empty var initializer: {}
 * Version 1.17 (October 12, 1999)
 *              ESC lexer rule allowed 399 max not 377 max.
 *              java.tree.g didn't handle the expression of synchronized
 *              statements.
 * Version 1.18 (August 12, 2001)
 *              Terence updated to Java 2 Version 1.3 by
 *              observing/combining work of Allan Jacobs and Steve
 *              Messick.  Handles 1.3 src.  Summary:
 *              o  primary didn't include boolean.class kind of thing
 *              o  constructor calls parsed explicitly now:
 *                 see explicitConstructorInvocation
 *              o  add strictfp modifier
 *              o  missing objBlock after new expression in tree grammar
 *              o  merged local class definition alternatives, moved after declaration
 *              o  fixed problem with ClassName.super.field
 *              o  reordered some alternatives to make things more efficient
 *              o  long and double constants were not differentiated from int/float
 *              o  whitespace rule was inefficient: matched only one char
 *              o  add an examples directory with some nasty 1.3 cases
 *              o  made Main.java use buffered IO and a Reader for Unicode support
 *              o  supports UNICODE?
 *                 Using Unicode charVocabulary makes code file big, but only
 *                 in the bitsets at the end. I need to make ANTLR generate
 *                 unicode bitsets more efficiently.
 * Version 1.19 (April 25, 2002)
 *              Terence added in nice fixes by John Pybus concerning floating
 *              constants and problems with super() calls.  John did a nice
 *              reorg of the primary/postfix expression stuff to read better
 *              and makes f.g.super() parse properly (it was METHOD_CALL not
 *              a SUPER_CTOR_CALL).  Also:
 *
 *              o  "finally" clause was a root...made it a child of "try"
 *              o  Added stuff for asserts too for Java 1.4, but *commented out*
 *                 as it is not backward compatible.
 *
 * Version 1.20 (October 27, 2002)
 *
 *        Terence ended up reorging John Pybus' stuff to
 *        remove some nondeterminisms and some syntactic predicates.
 *        Note that the grammar is stricter now; e.g., this(...) must
 *      be the first statement.
 *
 *        Trinary ?: operator wasn't working as array name:
 *                (isBig ? bigDigits : digits)[i];
 *
 *        Checked parser/tree parser on source for
 *                Resin-2.0.5, jive-2.1.1, jdk 1.3.1, Lucene, antlr 2.7.2a4,
 *              and the 110k-line jGuru server source.
 *
 * Version 1.21 (October 17, 2003)
 *  Fixed lots of problems including:
 *  Ray Waldin: add typeDefinition to interfaceBlock in java.tree.g
 *  He found a problem/fix with floating point that start with 0
 *  Ray also fixed problem that (int.class) was not recognized.
 *  Thorsten van Ellen noticed that \n are allowed incorrectly in strings.
 *  TJP fixed CHAR_LITERAL analogously.
 *
 * Version 1.21.2 (March, 2003)
 *        Changes by Matt Quail to support generics (as per JDK1.5/JSR14)
 *        Notes:
 *        o We only allow the "extends" keyword and not the "implements"
 *              keyword, since that's what JSR14 seems to imply.
 *        o Thanks to Monty Zukowski for his help on the antlr-interest
 *              mail list.
 *        o Thanks to Alan Eliasen for testing the grammar over his
 *              Fink source base
 *
 * Version 1.22 (July, 2004)
 *        Changes by Michael Studman to support Java 1.5 language extensions
 *        Notes:
 *        o Added support for annotations types
 *        o Finished off Matt Quail's generics enhancements to support bound type arguments
 *        o Added support for new for statement syntax
 *        o Added support for static import syntax
 *        o Added support for enum types
 *        o Tested against JDK 1.5 source base and source base of jdigraph project
 *        o Thanks to Matt Quail for doing the hard part by doing most of the generics work
 *
 * Version 1.22.1 (July 28, 2004)
 *        Bug/omission fixes for Java 1.5 language support
 *        o Fixed tree structure bug with classOrInterface - thanks to Pieter Vangorpto for
 *              spotting this
 *        o Fixed bug where incorrect handling of SR and BSR tokens would cause type
 *              parameters to be recognised as type arguments.
 *        o Enabled type parameters on constructors, annotations on enum constants
 *              and package definitions
 *        o Fixed problems when parsing if ((char.class.equals(c))) {} - solution by Matt Quail at Cenqua
 *
 * Version 1.22.2 (July 28, 2004)
 *        Slight refactoring of Java 1.5 language support
 *        o Refactored for/"foreach" productions so that original literal "for" literal
 *          is still used but the for sub-clauses vary by token type
 *        o Fixed bug where type parameter was not included in generic constructor's branch of AST
 *
 * Version 1.22.3 (August 26, 2004)
 *        Bug fixes as identified by Michael Stahl; clean up of tabs/spaces
 *        and other refactorings
 *        o Fixed typeParameters omission in identPrimary and newStatement
 *        o Replaced GT reconcilliation code with simple semantic predicate
 *        o Adapted enum/assert keyword checking support from Michael Stahl's java15 grammar
 *        o Refactored typeDefinition production and field productions to reduce duplication
 *
 * Version 1.22.4 (October 21, 2004)
 *    Small bux fixes
 *    o Added typeArguments to explicitConstructorInvocation, e.g. new <String>MyParameterised()
 *    o Added typeArguments to postfixExpression productions for anonymous inner class super
 *      constructor invocation, e.g. new Outer().<String>super()
 *    o Fixed bug in array declarations identified by Geoff Roy
 *
 * Version 1.22.4.g.1
 *    o I have taken java.g for Java1.5 from Michael Studman (1.22.4)
 *      and have applied the groovy.diff from java.g (1.22) by John Rose
 *      back onto the new root (1.22.4) - Jeremy Rayner (Jan 2005)
 *
 * Version 1.22.4.g.2
 *    o mkempf, rkleeb, Dec 2007
 *    o fixed various rules so that they call the correct Create Method
 *      to make sure that the line information are correct
 *
 * Based on an original grammar released in the PUBLIC DOMAIN
 */

class GroovyRecognizer extends Parser;
options {
    k = 2;                            // two token lookahead
    exportVocab=Groovy;               // Call its vocabulary "Groovy"
    codeGenMakeSwitchThreshold = 2;   // Some optimizations
    codeGenBitsetTestThreshold = 3;
    defaultErrorHandler = false;      // Don't generate parser error handlers
    buildAST = true;
}

tokens {
    BLOCK; MODIFIERS; OBJBLOCK; SLIST; METHOD_DEF; VARIABLE_DEF;
    INSTANCE_INIT; STATIC_INIT; TYPE; CLASS_DEF; INTERFACE_DEF; TRAIT_DEF;
    PACKAGE_DEF; ARRAY_DECLARATOR; EXTENDS_CLAUSE; IMPLEMENTS_CLAUSE;
    PARAMETERS; PARAMETER_DEF; LABELED_STAT; TYPECAST; INDEX_OP;
    POST_INC; POST_DEC; METHOD_CALL; EXPR;
    IMPORT; UNARY_MINUS; UNARY_PLUS; CASE_GROUP; ELIST; FOR_INIT; FOR_CONDITION;
    FOR_ITERATOR; EMPTY_STAT; FINAL="final"; ABSTRACT="abstract";
    UNUSED_GOTO="goto"; UNUSED_CONST="const"; UNUSED_DO="do";
    STRICTFP="strictfp"; SUPER_CTOR_CALL; CTOR_CALL; CTOR_IDENT; VARIABLE_PARAMETER_DEF;
    STRING_CONSTRUCTOR; STRING_CTOR_MIDDLE;
    CLOSABLE_BLOCK; IMPLICIT_PARAMETERS;
    SELECT_SLOT; DYNAMIC_MEMBER;
    LABELED_ARG; SPREAD_ARG; SPREAD_MAP_ARG; //deprecated - SCOPE_ESCAPE;
    LIST_CONSTRUCTOR; MAP_CONSTRUCTOR;
    FOR_IN_ITERABLE;
    STATIC_IMPORT; ENUM_DEF; ENUM_CONSTANT_DEF; FOR_EACH_CLAUSE; ANNOTATION_DEF; ANNOTATIONS;
    ANNOTATION; ANNOTATION_MEMBER_VALUE_PAIR; ANNOTATION_FIELD_DEF; ANNOTATION_ARRAY_INIT;
    TYPE_ARGUMENTS; TYPE_ARGUMENT; TYPE_PARAMETERS; TYPE_PARAMETER; WILDCARD_TYPE;
    TYPE_UPPER_BOUNDS; TYPE_LOWER_BOUNDS; CLOSURE_LIST;MULTICATCH;MULTICATCH_TYPES;
}

{
    /** This factory is the correct way to wire together a Groovy parser and lexer. */
    public static GroovyRecognizer make(GroovyLexer lexer) {
        GroovyRecognizer parser = new GroovyRecognizer(lexer.plumb());
        parser.lexer = lexer;
        lexer.parser = parser;
        parser.getASTFactory().setASTNodeClass(GroovySourceAST.class);
        return parser;
    }
    // Create a scanner that reads from the input stream passed to us...
    public static GroovyRecognizer make(InputStream in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(Reader in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(InputBuffer in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(LexerSharedInputState in) { return make(new GroovyLexer(in)); }

    @SuppressWarnings("unused")
    private static GroovySourceAST dummyVariableToForceClassLoaderToFindASTClass = new GroovySourceAST();

    List warningList = new ArrayList();
    public List getWarningList() { return warningList; }

    GroovyLexer lexer;
    public GroovyLexer getLexer() { return lexer; }
    public void setFilename(String f) { lexer.setFilename(f); super.setFilename(f); }

    @Deprecated
    public void setSourceBuffer(SourceBuffer sourceBuffer) {
    }

    /**
     * Creates an AST node with the token type and text passed in, but
     * with the same background information as another supplied Token (e.g.&nbsp;line numbers).
     * To be used in place of antlr tree construction syntax,
     * i.e. #[TOKEN,"text"]  becomes  create(TOKEN,"text",anotherToken)
     */
    public AST create(int type, String txt, AST first) {
        AST t = astFactory.create(type,txt);
        if ( t != null && first != null) {
            // first copy details from first token
            t.initialize(first);
            // then ensure that type and txt are specific to this new node
            t.initialize(type,txt);
        }
        return t;
    }

    private AST attachLast(AST t, Object last) {
        if ((t instanceof GroovySourceAST) && (last instanceof SourceInfo)) {
            SourceInfo lastInfo = (SourceInfo) last;
            GroovySourceAST node = (GroovySourceAST)t;
            node.setColumnLast(lastInfo.getColumn());
            node.setLineLast(lastInfo.getLine());
            // This is a good point to call node.setSnippet(),
            // but it bulks up the AST too much for production code.
        }
        return t;
    }

    public AST create(int type, String txt, Token first, Token last) {
        return attachLast(create(type, txt, astFactory.create(first)), last);
    }

    public AST create(int type, String txt, AST first, Token last) {
        return attachLast(create(type, txt, first), last);
    }

    public AST create(int type, String txt, AST first, AST last) {
        return attachLast(create(type, txt, first), last);
    }

    public Token cloneToken(Token t) {
        CommonToken clone = new CommonToken(t.getType(),t.getText());
        clone.setLine(t.getLine());
        clone.setColumn(t.getColumn());
        return clone;
    }

    // stuff to adjust ANTLR's tracing machinery
    public static boolean tracing = false;  // only effective if antlr.Tool is run with -traceParser
    public void traceIn(String rname) throws TokenStreamException {
        if (!GroovyRecognizer.tracing)  return;
        super.traceIn(rname);
    }
    public void traceOut(String rname) throws TokenStreamException {
        if (!GroovyRecognizer.tracing)  return;
        if (returnAST != null)  rname += returnAST.toStringList();
        super.traceOut(rname);
    }

    // Error handling.  This is a funnel through which parser errors go, when the parser can suggest a solution.
    public void requireFailed(String problem, String solution) throws SemanticException {
        // TODO: Needs more work.
        Token lt = null;
        int lineNum = Token.badToken.getLine(), colNum = Token.badToken.getColumn();
        try {
            lt = LT(1);
            if(lt != null) {
                lineNum = lt.getLine();
                colNum = lt.getColumn();
            }
        }
        catch (TokenStreamException ee) {
            if(ee instanceof TokenStreamRecognitionException) {
                lineNum = ((TokenStreamRecognitionException) ee).recog.getLine();
                colNum = ((TokenStreamRecognitionException) ee).recog.getColumn();
            }
        }
        throw new SemanticException(problem + ";\n   solution: " + solution,
                                    getFilename(), lineNum, colNum);
    }

    public void addWarning(String warning, String solution) {
        Token lt = null;
        try { lt = LT(1); }
        catch (TokenStreamException ee) { }
        if (lt == null)  lt = Token.badToken;

        Map row = new HashMap();
        row.put("warning",  warning);
        row.put("solution", solution);
        row.put("filename", getFilename());
        row.put("line",     Integer.valueOf(lt.getLine()));
        row.put("column",   Integer.valueOf(lt.getColumn()));

        warningList.add(row);
    }

    // Convenience method for checking of expected error syndromes.
    private void require(boolean z, String problem, String solution) throws SemanticException {
        if (!z)  requireFailed(problem, solution);
    }

    private boolean matchGenericTypeBrackets(boolean z, String problem, String solution) throws SemanticException {
        if (!z)  matchGenericTypeBracketsFailed(problem, solution);
        return z;
    }

    public void matchGenericTypeBracketsFailed(String problem, String solution) throws SemanticException {
        Token lt = null;
        int lineNum = Token.badToken.getLine(), colNum = Token.badToken.getColumn();

        try {
            lt = LT(1);
            if(lt != null) {
                lineNum = lt.getLine();
                colNum = lt.getColumn();
            }
        }
        catch (TokenStreamException ee) {
            if(ee instanceof TokenStreamRecognitionException) {
                lineNum = ((TokenStreamRecognitionException) ee).recog.getLine();
                colNum = ((TokenStreamRecognitionException) ee).recog.getColumn();
            }
        }

        throw new SemanticException(problem + ";\n   solution: " + solution,
                                    getFilename(), lineNum, colNum);
   }

    // Query a name token to see if it begins with a capital letter.
    // This is used to tell the difference (w/o symbol table access) between {String x} and {println x}.
    private boolean isUpperCase(Token x) {
        if (x == null || x.getType() != IDENT)  return false;  // cannot happen?
        String xtext = x.getText();
        return (xtext.length() > 0 && Character.isUpperCase(xtext.charAt(0)));
    }

    private AST currentClass = null;  // current enclosing class (for constructor recognition)
    // Query a name token to see if it is identical with the current class name.
    // This is used to distinguish constructors from other methods.
    private boolean isConstructorIdent(Token x) {
        if (currentClass == null)  return false;
        if (currentClass.getType() != IDENT)  return false;  // cannot happen?
        String cname = currentClass.getText();

        if (x == null || x.getType() != IDENT)  return false;  // cannot happen?
        return cname.equals(x.getText());
    }

    // Scratch variable for last 'sep' token.
    // Written by the 'sep' rule, read only by immediate callers of 'sep'.
    // (Not entirely clean, but better than a million xx=sep occurrences.)
    private int sepToken = EOF;

    // Scratch variable for last argument list; tells whether there was a label.
    // Written by 'argList' rule, read only by immediate callers of 'argList'.
    private boolean argListHasLabels = false;

    // Scratch variable, holds most recently completed pathExpression.
    // Read only by immediate callers of 'pathExpression' and 'expression'.
    private AST lastPathExpression = null;

    // Inherited attribute pushed into most expression rules.
    // If not zero, it means that the left context of the expression
    // being parsed is a statement boundary or an initializer sign '='.
    // Only such expressions are allowed to reach across newlines
    // to pull in an LCURLY and appended block.
    private final int LC_STMT = 1, LC_INIT = 2;

    /**
     * Counts the number of LT seen in the typeArguments production.
     * It is used in semantic predicates to ensure we have seen
     * enough closing '>' characters; which actually may have been
     * either GT, SR or BSR tokens.
     */
    private int ltCounter = 0;

    /* This symbol is used to work around a known ANTLR limitation.
     * In a loop with syntactic predicate, ANTLR needs help knowing
     * that the loop exit is a second alternative.
     * Example usage:  ( (LCURLY)=> block | {ANTLR_LOOP_EXIT}? )*
     * Probably should be an ANTLR RFE.
     */
    ////// Original comment in Java grammar:
    // Unfortunately a syntactic predicate can only select one of
    // multiple alternatives on the same level, not break out of
    // an enclosing loop, which is why this ugly hack (a fake
    // empty alternative with always-false semantic predicate)
    // is necessary.
    @SuppressWarnings("unused")
    private static final boolean ANTLR_LOOP_EXIT = false;
}

// Compilation Unit: In Groovy, this is a single file or script. This is the start
// rule for this parser
compilationUnit
    :
        // The very first characters of the file may be "#!".  If so, ignore the first line.
        (SH_COMMENT!)?

        // we can have comments at the top of a file
        nls!

        // A compilation unit starts with an optional package definition
        (   (annotationsOpt "package") => packageDefinition
        |   (statement[EOF])?
        )

        // The main part of the script is a sequence of any number of statements.
        // Semicolons and/or significant newlines serve as separators.
        ( sep! (statement[sepToken])? )*
        EOF!
    ;

/** A Groovy script or simple expression.  Can be anything legal inside {...}. */
snippetUnit
    :   nls! blockBody[EOF]
    ;

// Package statement: optional annotations followed by "package" then the package identifier
packageDefinition
        {Token first = LT(1);}
    :   an:annotationsOpt! "package"! id:identifier!
        {
            #packageDefinition = #(create(PACKAGE_DEF,"package",first,LT(1)), an, id);
        }
    ;

// Import statement: import followed by a package or class name
importStatement
        {Token first = LT(1); boolean isStatic = false;}
    :   an:annotationsOpt "import"! ("static"! {isStatic = true;})? is:identifierStar!
        {
            if (!isStatic) {
                #importStatement = #(create(IMPORT,"import",first,LT(1)), an, is);
            } else {
                #importStatement = #(create(STATIC_IMPORT,"static_import",first,LT(1)), an, is);
            }
        }
    ;

// Protected type definitions production for reuse in other productions
protected typeDefinitionInternal[AST mods]
    :   cd:classDefinition[#mods]       // inner class
        {#typeDefinitionInternal = #cd;}
    |   td:traitDefinition[#mods]       // inner trait
        {#typeDefinitionInternal = #td;}
    |   id:interfaceDefinition[#mods]   // inner interface
        {#typeDefinitionInternal = #id;}
    |   ed:enumDefinition[#mods]        // inner enum
        {#typeDefinitionInternal = #ed;}
    |   ad:annotationDefinition[#mods]  // inner annotation
        {#typeDefinitionInternal = #ad;}
    ;

/** A declaration is the creation of a reference or primitive-type variable,
 *  or (if arguments are present) of a method.
 *  Generically, this is called a 'variable' definition, even in the case of a class field or method.
 *  It may start with the modifiers and/or a declaration keyword "def".
 *  It may also start with the modifiers and a capitalized type name.
 *  <p>
 *  AST effect: Create a separate Type/Var tree for each var in the var list.
 *  Must be guarded, as in (declarationStart) => declaration.
 */
declaration!
    :
        // method/variable using a 'def' or a modifier; type is optional
        m:modifiers
        (t:typeSpec[false])?
        v:variableDefinitions[#m, #t]
        {#declaration = #v;}
    |
        // method/variable using a type only
        t2:typeSpec[false]
        v2:variableDefinitions[null,#t2]
        {#declaration = #v2;}
    ;

genericMethod!
    :
        // method using a 'def' or a modifier; type is optional
        m:modifiers
        p:typeParameters
        t:typeSpec[false]
        v:variableDefinitions[#m, #t]
        {
            #genericMethod = #v;
            AST old = #v.getFirstChild();
            #genericMethod.setFirstChild(#p);
            #p.setNextSibling(old);
        }
    ;

/** A declaration with one declarator and no initialization, like a parameterDeclaration.
 *  Used to parse loops like <code>for (int x in y)</code> (up to the <code>in</code> keyword).
 */
singleDeclarationNoInit!
    :
        // method/variable using a 'def' or a modifier; type is optional
        m:modifiers
        (t:typeSpec[false])?
        v:singleVariable[#m, #t]
        {#singleDeclarationNoInit = #v;}
    |
        // method/variable using a type only
        t2:typeSpec[false]
        v2:singleVariable[null,#t2]
        {#singleDeclarationNoInit = #v2;}
    ;

/** A declaration with one declarator and optional initialization, like a parameterDeclaration.
 *  Used to parse declarations used for both binding and effect, in places like argument
 *  lists and <code>while</code> statements.
 */
singleDeclaration
    :   sd:singleDeclarationNoInit!
        { #singleDeclaration = #sd; }
        (varInitializer)?
    ;

/** Used only as a lookahead predicate, before diving in and parsing a declaration.
 *  A declaration can be unambiguously introduced with "def", an annotation or a modifier token like "final".
 *  It may also be introduced by a simple identifier whose first character is an uppercase letter,
 *  as in {String x}.  A declaration can also be introduced with a built in type like 'int' or 'void'.
 *  Brackets (array and generic) are allowed, as in {List[] x} or {int[][] y}.
 *  Anything else is parsed as a statement of some sort (expression or command).
 *  <p>
 *  (In the absence of explicit method-call parens, we assume a capitalized name is a type name.
 *  Yes, this is a little hacky.  Alternatives are to complicate the declaration or command
 *  syntaxes, or to have the parser query the symbol table.  Parse-time queries are evil.
 *  And we want both {String x} and {println x}.  So we need a syntactic razor-edge to slip
 *  between 'println' and 'String'.)
 */
declarationStart!
    :   (     ("def" nls)
            | modifier nls
            | annotation nls
            | (   upperCaseIdent
                |   builtInType
                |   qualifiedTypeName
              ) (typeArguments)? (LBRACK balancedTokens RBRACK)*
        )+
        ( IDENT | STRING_LITERAL )
    ;

/**
 * lookahead predicate for usage of generics in methods
 * as parameter for the method. Example:
 * static <T> T foo(){}
 * <T> must be first after the modifier.
 * This rule allows more and does no exact match, but it
 * is only a lookahead, not the real rule.
 */
genericMethodStart!
    :   (     "def" nls
            | modifier nls
            | annotation nls
        )+ LT
    ;

qualifiedTypeName!
    :
        IDENT DOT (IDENT DOT)* upperCaseIdent
    ;

/** Used to look ahead for a constructor
 */
constructorStart!
    :
        modifiersOpt! id:IDENT! {isConstructorIdent(id)}? nls! LPAREN!
    ;

/** Used only as a lookahead predicate for nested type definitions. */
typeDefinitionStart!
    :   modifiersOpt! ("class" | "interface" | "enum" | "trait" | AT "interface")
    ;

/** An IDENT token whose spelling is required to start with an uppercase letter.
 *  In the case of a simple statement {UpperID name} the identifier is taken to be a type name, not a command name.
 */
upperCaseIdent
    :   {isUpperCase(LT(1))}?
        IDENT
    ;

// A type specification is a type name with possible brackets afterwards
// (which would make it an array type).
// Set addImagNode true for types inside expressions, not declarations.
typeSpec[boolean addImagNode]
    :    classTypeSpec[addImagNode]
    |    builtInTypeSpec[addImagNode]
    ;

// also check that 'classOrInterfaceType[false]' is a suitable substitution for 'identifier'

// A class type specification is a class type with either:
// - possible brackets afterwards
//   (which would make it an array type).
// - generic type arguments after
classTypeSpec[boolean addImagNode]  {Token first = LT(1);}
    :   ct:classOrInterfaceType[false]!
        declaratorBrackets[#ct]
        {
            if ( addImagNode ) {
                #classTypeSpec = #(create(TYPE,"TYPE",first,LT(1)), #classTypeSpec);
            }
        }
    ;

// A non-built in type name, with possible type parameters
classOrInterfaceType[boolean addImagNode]  {Token first = LT(1);}
    :   i1:IDENT^ (typeArguments|typeArgumentsDiamond)?

        (   options{greedy=true;}: // match as many as possible
            d:DOT!
            i2:IDENT! (ta:typeArguments!)?
            {#i1 = #(create(DOT,".",first,LT(1)), i1, i2, ta);}
        )*
        {
            #classOrInterfaceType = #i1;
            if ( addImagNode ) {
                #classOrInterfaceType = #(create(TYPE,"TYPE",first,LT(1)), #classOrInterfaceType);
            }
        }
    ;

// A specialised form of typeSpec where built in types must be arrays
typeArgumentSpec
    :   classTypeSpec[true]
    |   builtInTypeArraySpec[true]
    ;

// A generic type argument is a class type, a possibly bounded wildcard type or a built-in type array
typeArgument  {Token first = LT(1);}
    :   (   typeArgumentSpec
        |   wildcardType
        )
        {#typeArgument = #(create(TYPE_ARGUMENT,"TYPE_ARGUMENT",first,LT(1)), #typeArgument);}
    ;

// Wildcard type indicating all types (with possible constraint)
wildcardType
    :   QUESTION
        (("extends" | "super") => typeArgumentBounds)?
        {#wildcardType.setType(WILDCARD_TYPE);}
    ;

typeArgumentsDiamond
{Token first = LT(1);}
    :   LT! GT! nls!
    {#typeArgumentsDiamond = #(create(TYPE_ARGUMENTS,"TYPE_ARGUMENTS",first,LT(1)), #typeArgumentsDiamond);}
    ;

// Type arguments to a class or interface type
typeArguments
{Token first = LT(1);
int currentLtLevel = 0;}
    :
        {currentLtLevel = ltCounter;}
        LT! {ltCounter++;} nls!
        typeArgument
        (   options{greedy=true;}: // match as many as possible
            {inputState.guessing !=0 || ltCounter == currentLtLevel + 1}?
            COMMA! nls! typeArgument
        )*
        nls!
        (   // turn warning off since Antlr generates the right code,
            // plus we have our semantic predicate below
            options{generateAmbigWarnings=false;}:
            typeArgumentsOrParametersEnd
        )?

        // make sure we have gobbled up enough '>' characters
        // if we are at the "top level" of nested typeArgument productions
        {matchGenericTypeBrackets(((currentLtLevel != 0) || ltCounter == currentLtLevel),
        "Missing closing bracket '>' for generics types", "Please specify the missing bracket!")}?

        {#typeArguments = #(create(TYPE_ARGUMENTS,"TYPE_ARGUMENTS",first,LT(1)), #typeArguments);}
    ;

// this gobbles up *some* amount of '>' characters, and counts how many
// it gobbled.
protected typeArgumentsOrParametersEnd
    :   GT! {ltCounter-=1;}
    |   SR! {ltCounter-=2;}
    |   BSR! {ltCounter-=3;}
    ;

// Restriction on wildcard types based on super class or derived class
typeArgumentBounds
    {Token first = LT(1);boolean isUpperBounds = false;}
    :
        ( "extends"! {isUpperBounds=true;} | "super"! ) nls! classOrInterfaceType[true] nls!
        {
            if (isUpperBounds)
            {
                #typeArgumentBounds = #(create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS",first,LT(1)), #typeArgumentBounds);
            }
            else
            {
                #typeArgumentBounds = #(create(TYPE_LOWER_BOUNDS,"TYPE_LOWER_BOUNDS",first,LT(1)), #typeArgumentBounds);
            }
        }
    ;

// A builtin type array specification is a builtin type with brackets afterwards
builtInTypeArraySpec[boolean addImagNode]  {Token first = LT(1);}
    :   bt:builtInType!
        (   (LBRACK)=>   // require at least one []
            declaratorBrackets[#bt]
        |   {require(false,
                          "primitive type parameters not allowed here",
                           "use the corresponding wrapper type, such as Integer for int"
                           );}
        )
        {
            if ( addImagNode ) {
                #builtInTypeArraySpec = #(create(TYPE,"TYPE",first,LT(1)), #builtInTypeArraySpec);
            }
        }
    ;

// A builtin type specification is a builtin type with possible brackets
// afterwards (which would make it an array type).
builtInTypeSpec[boolean addImagNode]  {Token first = LT(1);}
    :   bt:builtInType!
        declaratorBrackets[#bt]
        {
            if ( addImagNode ) {
                #builtInTypeSpec = #(create(TYPE,"TYPE",first,LT(1)), #builtInTypeSpec);
            }
        }
    ;

// A type name. which is either a (possibly qualified and parameterized)
// class name or a primitive (builtin) type
type
    :   classOrInterfaceType[false]
    |   builtInType
    ;

// The primitive types.
builtInType
    :   "void"
    |   "boolean"
    |   "byte"
    |   "char"
    |   "short"
    |   "int"
    |   "float"
    |   "long"
    |   "double"
    ;

// A (possibly-qualified) java identifier. We start with the first IDENT
// and expand its name by adding dots and following IDENTS
identifier {Token first = LT(1);}
    :   i1:IDENT!
        (   options { greedy = true; } :
            d:DOT! nls! i2:IDENT!
            {#i1 = #(create(DOT,".",first,LT(1)), i1, i2);}
        )*
        {#identifier = #i1;}
    ;

identifierStar {Token first = LT(1);}
    :   i1:IDENT!
        (   options { greedy = true; } :
            d1:DOT! nls! i2:IDENT!
            {#i1 = #(create(DOT,".",first,LT(1)), i1, i2);}
        )*
        (   d2:DOT!  nls! s:STAR!
            {#i1 = #(create(DOT,".",first,LT(1)), i1, s);}
        |   "as"! nls! alias:IDENT!
            {#i1 = #(create(LITERAL_as,"as",first,LT(1)), i1, alias);}
        )?
        {#identifierStar = #i1;}
    ;

modifiersInternal
        { int seenDef = 0; }
    :
        (
            // Without this hush, there is a warning that @IDENT and @interface
            // can follow modifiersInternal.  But how is @IDENT possible after
            // modifiersInternal?  And how is @interface possible inside modifiersInternal?
            // Is there an antlr bug?
            options{generateAmbigWarnings=false;}:

            // 'def' is an empty modifier, for disambiguating declarations
            {seenDef++ == 0}?       // do not allow multiple "def" tokens
            "def"! nls!
        |
            // Note: Duplication of modifiers is detected when walking the AST.
            modifier nls!
        |
            {break; /* go out of the ()+ loop*/}
            AT "interface"
        |
            annotation nls!
        )+
    ;

/** A list of one or more modifier, annotation, or "def". */
modifiers  {Token first = LT(1);}
    :   modifiersInternal
        {#modifiers = #(create(MODIFIERS,"MODIFIERS",first,LT(1)), #modifiers);}
    ;

/** A list of zero or more modifiers, annotations, or "def". */
modifiersOpt  {Token first = LT(1);}
    :   (
            // See comment above on hushing warnings.
            options{generateAmbigWarnings=false;}:
            modifiersInternal
        )?
        {#modifiersOpt = #(create(MODIFIERS,"MODIFIERS",first,LT(1)), #modifiersOpt);}
    ;

// modifiers for Java classes, interfaces, class/instance vars and methods
modifier
    :   "private"
    |   "public"
    |   "protected"
    |   "static"
    |   "transient"
    |   "final"
    |   "abstract"
    |   "native"
    |   "threadsafe"
    |   "synchronized"
    |   "volatile"
    |   "strictfp"
    ;

annotation!  {Token first = LT(1);}
    :   AT! i:identifier nls! (options{greedy=true;}: LPAREN! (args:annotationArguments)? RPAREN!)?
        {#annotation = #(create(ANNOTATION,"ANNOTATION",first,LT(1)), i, args);}
    ;

annotationsInternal
    :   (
            options{generateAmbigWarnings=false;}:
            {break; /* go out of the ()* loop*/}
            AT "interface"
        |
            annotation nls!
        )*
    ;

annotationsOpt  {Token first = LT(1);}
    :   (
            // See comment above on hushing warnings.
            options{generateAmbigWarnings=false;}:
            annotationsInternal
        )?
        {#annotationsOpt = #(create(ANNOTATIONS,"ANNOTATIONS",first,LT(1)), #annotationsOpt);}
    ;

annotationArguments
    :   v:annotationMemberValueInitializer
        {Token itkn = new Token(IDENT,"value"); AST i; #i = #(create(IDENT,"value",itkn,itkn));
         #annotationArguments = #(create(ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR",LT(1),LT(1)), i, v);}
        | annotationMemberValuePairs
    ;

annotationMemberValuePairs
    :   annotationMemberValuePair (
            COMMA! nls! annotationMemberValuePair
        )*
    ;

annotationMemberValuePair!  {Token first = LT(1);}
    :   i:annotationIdent ASSIGN! nls! v:annotationMemberValueInitializer
        {#annotationMemberValuePair = #(create(ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR",first,LT(1)), i, v);}
    ;

annotationIdent
    :   IDENT
    |   keywordPropertyNames
    ;

annotationMemberValueInitializer
    :   conditionalExpression[0] | annotation
    ;

superClassClause!
    {Token first = LT(1);}
    :
        ( "extends" nls! c:classOrInterfaceType[false] nls! )?
        {#superClassClause = #(create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE",first,LT(1)), c);}
    ;

// Definition of a Java class
classDefinition![AST modifiers]
{Token first = cloneToken(LT(1));AST prevCurrentClass = currentClass;
if (modifiers != null) {
     first.setLine(modifiers.getLine());
     first.setColumn(modifiers.getColumn());
}}
    :   "class" IDENT nls!
       { currentClass = #IDENT; }
        // it _might_ have type parameters
        (tp:typeParameters nls!)?
        // it _might_ have a superclass...
        sc:superClassClause
        // it might implement some interfaces...
        ic:implementsClause
        // now parse the body of the class
        cb:classBlock
        {#classDefinition = #(create(CLASS_DEF,"CLASS_DEF",first,LT(1)), modifiers, IDENT, tp, sc, ic, cb);}
        { currentClass = prevCurrentClass; }
    ;

// Definition of a Trait
traitDefinition![AST modifiers]
{Token first = cloneToken(LT(1));AST prevCurrentClass = currentClass;
if (modifiers != null) {
     first.setLine(modifiers.getLine());
     first.setColumn(modifiers.getColumn());
}}
    :   "trait" IDENT nls!
       { currentClass = #IDENT; }
        // it _might_ have type parameters
        (tp:typeParameters nls!)?
        // it _might_ have a superclass...
        sc:superClassClause
        // it might implement some interfaces...
        ic:implementsClause
        // now parse the body of the class
        cb:classBlock
        {#traitDefinition = #(create(TRAIT_DEF,"TRAIT_DEF",first,LT(1)), modifiers, IDENT, tp, sc, ic, cb);}
        { currentClass = prevCurrentClass; }
    ;

// Definition of a Java Interface
interfaceDefinition![AST modifiers]  {Token first = cloneToken(LT(1));
                                      if (modifiers != null) {
                                          first.setLine(modifiers.getLine());
                                          first.setColumn(modifiers.getColumn());
                                      }}
    :   "interface" IDENT nls!
        // it _might_ have type parameters
        (tp:typeParameters nls!)?
        // it might extend some other interfaces
        ie:interfaceExtends
        // now parse the body of the interface (looks like a class...)
        ib:interfaceBlock
        {#interfaceDefinition = #(create(INTERFACE_DEF,"INTERFACE_DEF",first,LT(1)), modifiers, IDENT, tp, ie, ib);}
    ;

enumDefinition![AST modifiers]  {Token first = cloneToken(LT(1)); AST prevCurrentClass = currentClass;
                                 if (modifiers != null) {
                                     first.setLine(modifiers.getLine());
                                     first.setColumn(modifiers.getColumn());
                                 }}
    :   "enum" IDENT
            { currentClass = #IDENT; }
        nls!
        // it might implement some interfaces...
        ic:implementsClause
        nls!
        // now parse the body of the enum
        eb:enumBlock
        {#enumDefinition = #(create(ENUM_DEF,"ENUM_DEF",first,LT(1)), modifiers, IDENT, ic, eb);}
        { currentClass = prevCurrentClass; }
    ;

annotationDefinition![AST modifiers]  {Token first = cloneToken(LT(1));
                                      if (modifiers != null) {
                                          first.setLine(modifiers.getLine());
                                          first.setColumn(modifiers.getColumn());
                                      }}
    :   AT "interface" IDENT nls!
        // now parse the body of the annotation
        ab:annotationBlock
        {#annotationDefinition = #(create(ANNOTATION_DEF,"ANNOTATION_DEF",first,LT(1)), modifiers, IDENT, ab);}
    ;

typeParameters
{Token first = LT(1);int currentLtLevel = 0;}
    :
        {currentLtLevel = ltCounter;}
        LT! {ltCounter++;} nls!
        typeParameter (COMMA! nls! typeParameter)*
        nls!
        (typeArgumentsOrParametersEnd)?

        // make sure we have gobbled up enough '>' characters
        // if we are at the "top level" of nested typeArgument productions
        {matchGenericTypeBrackets(((currentLtLevel != 0) || ltCounter == currentLtLevel),
        "Missing closing bracket '>' for generics types", "Please specify the missing bracket!")}?

        {#typeParameters = #(create(TYPE_PARAMETERS,"TYPE_PARAMETERS",first,LT(1)), #typeParameters);}
    ;

typeParameter  {Token first = LT(1);}
    :
        // I'm pretty sure Antlr generates the right thing here:
        (id:IDENT) ( options{generateAmbigWarnings=false;}: typeParameterBounds )?
        {#typeParameter = #(create(TYPE_PARAMETER,"TYPE_PARAMETER",first,LT(1)), #typeParameter);}
    ;

typeParameterBounds  {Token first = LT(1);}
    :
        "extends"! nls! classOrInterfaceType[true]
        (BAND! nls! classOrInterfaceType[true])*
        {#typeParameterBounds = #(create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS",first,LT(1)), #typeParameterBounds);}
    ;

// This is the body of a class. You can have classFields and extra semicolons.
classBlock  {Token first = LT(1);}
    :   LCURLY!
        ( classField )? ( sep! ( classField )? )*
        RCURLY!
        {#classBlock = #(create(OBJBLOCK,"OBJBLOCK",first,LT(1)), #classBlock);}
    ;

// This is the body of an interface. You can have interfaceField and extra semicolons.
interfaceBlock  {Token first = LT(1);}
    :   LCURLY!
        ( interfaceField )? ( sep! ( interfaceField )? )*
        RCURLY!
        {#interfaceBlock = #(create(OBJBLOCK,"OBJBLOCK",first,LT(1)), #interfaceBlock);}
    ;

// This is the body of an annotation. You can have annotation fields and extra semicolons,
// That's about it (until you see what an annotation field is...)
annotationBlock  {Token first = LT(1);}
    :   LCURLY!
        ( annotationField )? ( sep! ( annotationField )? )*
        RCURLY!
        {#annotationBlock = #(create(OBJBLOCK,"OBJBLOCK",first,LT(1)), #annotationBlock);}
    ;

// This is the body of an enum. You can have zero or more enum constants
// followed by any number of fields like a regular class
enumBlock  {Token first = LT(1);}
    :   LCURLY! nls!
        (
            // Need a syntactic predicate, since enumConstants
            // can start with foo() as well as classField.
            // (It's a true ambiguity, visible in the specification.
            // To resolve in practice, use "def" before a real method.)
            (enumConstantsStart) => enumConstants
        |   (classField)?
        )
        ( sep! (classField)? )*
        RCURLY!
        {#enumBlock = #(create(OBJBLOCK,"OBJBLOCK",first,LT(1)), #enumBlock);}
    ;

/** Guard for enumConstants. */
enumConstantsStart
    :   annotationsOpt IDENT (LCURLY | LPAREN | nls (SEMI | COMMA | declarationStart | RCURLY))
    ;

/** Comma-separated list of one or more enum constant definitions. */
enumConstants
    :
        enumConstant
        ( options {generateAmbigWarnings=false;} :
            (nls enumConstantsEnd) => {break;} // GROOVY-4438, GROOVY-9184
        |
            nls! COMMA! (
                (nls enumConstantsEnd) => {break;} // GROOVY-8507, GROOVY-9301
            |
                (nls annotationsOpt IDENT) => nls! enumConstant
            |
                (nls classField) => {break;}
            )
        )*
    ;

enumConstantsEnd
    options {generateAmbigWarnings=false;}
    :   SEMI | RCURLY | declarationStart | constructorStart | typeDefinitionStart
    ;

// An annotation field
annotationField!  {Token first = LT(1);}
    :   mods:modifiersOpt!
        (   td:typeDefinitionInternal[#mods]
            {#annotationField = #td;}
        |   t:typeSpec[false]               // annotation field
            (
                // Need a syntactic predicate, since variableDefinitions
                // can start with foo() also.  Since method defs are not legal
                // in this context, there's no harm done.
                (IDENT LPAREN)=>
                i:IDENT              // the name of the field
                LPAREN! RPAREN!

                ( "default" nls! amvi:annotationMemberValueInitializer )?

                {#annotationField = #(create(ANNOTATION_FIELD_DEF,"ANNOTATION_FIELD_DEF",first,LT(1)), mods, #(create(TYPE,"TYPE",first,LT(1)), t), i, amvi);}
            |   v:variableDefinitions[#mods,#t]    // variable
                {#annotationField = #v;}
            )
        )
    ;

//An enum constant may have optional parameters and may have a
//a class body
enumConstant!  {Token first = LT(1);}
    :   an:annotationsOpt // Note:  Cannot start with "def" or another modifier.
        i:IDENT
        (   LPAREN!
            a:argList
            RPAREN!
        )?
        ( b:enumConstantBlock )?
        {#enumConstant = #(create(ENUM_CONSTANT_DEF,"ENUM_CONSTANT_DEF",first,LT(1)), an, i, a, b);}
    ;

//The class-like body of an enum constant
enumConstantBlock  {Token first = LT(1);}
    :   LCURLY!
        (enumConstantField)? ( sep! (enumConstantField)? )*
        RCURLY!
        {#enumConstantBlock = #(create(OBJBLOCK,"OBJBLOCK",first,LT(1)), #enumConstantBlock);}
    ;

//An enum constant field is just like a class field but without
//the possibility of a constructor definition or a static initializer

// TODO - maybe allow 'declaration' production within this production,
// but how to disallow constructors and static initializers...
enumConstantField! {Token first = LT(1);}
    :   (
            (typeDefinitionStart)=>
            mods:modifiersOpt!
            td:typeDefinitionInternal[#mods]
            {#enumConstantField = #td;}
        |
            (modifiers)=>
            m1:modifiers
            (tp1:typeParameters)? (t1:typeSpec[false])?
            e1:enumConstantFieldInternal[#m1, #tp1, #t1, #first]
            {#enumConstantField = #e1;}
        |
            m2:modifiersOpt!
            (tp2:typeParameters)? t2:typeSpec[false]
            e2:enumConstantFieldInternal[#m2, #tp2, #t2, #first]
            {#enumConstantField = #e2;}
        )
    |   cs:compoundStatement
        {#enumConstantField = #(create(INSTANCE_INIT,"INSTANCE_INIT",first,LT(1)), cs);}
    ;

protected enumConstantFieldInternal![AST mods, AST tp, AST t, Token first]
    :
        // Need a syntactic predicate to avoid potential ambiguity
        (IDENT LPAREN)=>
        IDENT

        // parse the formal parameter declarations.
        LPAREN! param:parameterDeclarationList RPAREN!

        // get the list of declared exceptions
        ((nls "throws") => tc:throwsClause)?

        ( s2:compoundStatement )?
        {
            #enumConstantFieldInternal = #(create(METHOD_DEF,"METHOD_DEF",first,LT(1)),
                    mods,
                    #(create(TYPE,"TYPE",first,LT(1)), t),
                    IDENT,
                    param,
                    tc,
                    s2);
            if (tp != null) {
                AST old = #enumConstantFieldInternal.getFirstChild();
                #enumConstantFieldInternal.setFirstChild(#tp);
                #tp.setNextSibling(old);
            }
        }

    |   v:variableDefinitions[#mods,#t]
        {#enumConstantFieldInternal = #v;}
    ;

// An interface can extend several other interfaces...
interfaceExtends  {Token first = LT(1);}
    :   (
            e:"extends"! nls!
            classOrInterfaceType[true] ( COMMA! nls! classOrInterfaceType[true] )* nls!
        )?
        {#interfaceExtends = #(create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE",first,LT(1)), #interfaceExtends);}
    ;

// A class can implement several interfaces...
implementsClause  {Token first = LT(1);}
    :   (
            i:"implements"! nls!
            classOrInterfaceType[true] ( COMMA! nls! classOrInterfaceType[true] )* nls!
        )?
        {#implementsClause = #(create(IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE",first,LT(1)), #implementsClause);}
    ;

// Now the various things that can be defined inside a class
classField!  {Token first = LT(1);}
    :   // method, constructor, or variable declaration
        (constructorStart)=>
        mc:modifiersOpt! ctor:constructorDefinition[#mc]
        {#classField = #ctor;}
    |
        (genericMethodStart)=>
        dg:genericMethod
        {#classField = #dg;}
    |
        (multipleAssignmentDeclarationStart)=>
        mad:multipleAssignmentDeclaration
        {#classField = #mad;}
    |
        (declarationStart)=>
        dd:declaration
        {#classField = #dd;}
    |
        // type definition
        (typeDefinitionStart)=>
        mods:modifiersOpt!
        (   td:typeDefinitionInternal[#mods]
                {#classField = #td;}
        )

    // "static { ... }" class initializer
    |   "static" nls! s3:compoundStatement
        {#classField = #(create(STATIC_INIT,"STATIC_INIT",first,LT(1)), s3);}

    // "{ ... }" instance initializer
    |   s4:compoundStatement
        {#classField = #(create(INSTANCE_INIT,"INSTANCE_INIT",first,LT(1)), s4);}
    ;

// Now the various things that can be defined inside an interface
interfaceField!
    :   // method or variable declaration or inner interface
        (declarationStart)=>
        d:declaration
        {#interfaceField = #d;}
    |
        (genericMethodStart)=>
        dg:genericMethod
        {#interfaceField = #dg;}
    |
        // type definition
        (typeDefinitionStart)=>
        mods:modifiersOpt
        (   td:typeDefinitionInternal[#mods]
            {#interfaceField = #td;}
        )
    ;

constructorBody  {Token first = LT(1);}
     :   LCURLY! nls!
         (   (explicitConstructorInvocation) =>   // Java compatibility hack
                 eci:explicitConstructorInvocation! (sep! bb1:blockBody[sepToken]!)?
             |   bb2:blockBody[EOF]!
         )
         RCURLY!
         {LT(0).setColumn(LT(0).getColumn() + 1);
          if (#eci != null)
              #constructorBody = #(create(SLIST,"{",first,LT(0)), eci, bb1);
          else
              #constructorBody = #(create(SLIST,"{",first,LT(0)), bb2);
         }
     ;

/** Catch obvious constructor calls, but not the expr.super(...) calls */
explicitConstructorInvocation
    :   (typeArguments)?
        (   "this"! lp1:LPAREN^ argList RPAREN!
            {#lp1.setType(CTOR_CALL);}
        |   "super"! lp2:LPAREN^ argList RPAREN!
            {#lp2.setType(SUPER_CTOR_CALL);}
        )
    ;

listOfVariables[AST mods, AST t, Token first]
    :
        variableDeclarator[getASTFactory().dupTree(mods),
                           getASTFactory().dupTree(t),first]
        (   COMMA! nls!
            {first = LT(1);}
            variableDeclarator[getASTFactory().dupTree(mods),
                               getASTFactory().dupTree(t),first]
        )*
    ;

multipleAssignmentDeclarationStart
    :
        (modifier nls | annotation nls)* "def" nls LPAREN
    ;

typeNamePairs[AST mods, Token first]
    :
        (t:typeSpec[false]!)?
        singleVariable[getASTFactory().dupTree(mods),#t]
        (   COMMA! nls!
            {first = LT(1);}
            (tn:typeSpec[false]!)?
            singleVariable[getASTFactory().dupTree(mods),#tn]
        )*
    ;

multipleAssignmentDeclaration {Token first = cloneToken(LT(1));}
    :
        mods:modifiers!
        (t:typeSpec[false]!)?
        LPAREN^ nls! typeNamePairs[#mods,first] RPAREN!
        ASSIGN^ nls!
        (
          (LPAREN nls IDENT (COMMA nls IDENT)* RPAREN ASSIGN) => multipleAssignment[0]
          | assignmentExpression[0]
        )
        {#multipleAssignmentDeclaration=#(create(VARIABLE_DEF,"VARIABLE_DEF",first,LT(1)), #mods, #(create(TYPE,"TYPE",first,LT(1)), #t), #multipleAssignmentDeclaration);}
    ;

/** The tail of a declaration.
  * Either v1, v2, ... (with possible initializers) or else m(args){body}.
  * The two arguments are the modifier list (if any) and the declaration head (if any).
  * The declaration head is the variable type, or (for a method) the return type.
  * If it is missing, then the variable type is taken from its initializer (if there is one).
  * Otherwise, the variable type defaults to 'any'.
  * DECIDE:  Method return types default to the type of the method body, as an expression.
  */
variableDefinitions[AST mods, AST t] {Token first = cloneToken(LT(1));
                       if (mods != null) {
                           first.setLine(mods.getLine());
                           first.setColumn(mods.getColumn());
                       } else if (t != null) {
                           first.setLine(t.getLine());
                           first.setColumn(t.getColumn());
                       }}
    :
        listOfVariables[mods,t,first]
    |
        // The parser allows a method definition anywhere a variable definition is accepted.

        (   id:IDENT
        |   qid:STRING_LITERAL          {#qid.setType(IDENT);}  // use for operator definitions, etc.
        )

        // parse the formal parameter declarations.
        LPAREN! param:parameterDeclarationList! RPAREN!

        // get the list of exceptions that this method is
        // declared to throw
        ((nls "throws") => tc:throwsClause!)?

        // the method body is an open block
        // but, it may have an optional constructor call (for constructors only)
        // this constructor clause is only used for constructors using 'def'
        // which look like method declarations
        // since the block is optional and nls is part of sep we have to be sure
        // a newline is followed by a block or ignore the nls too
        ((nls! LCURLY) => (nlsWarn! mb:openBlock!))?

        {
         if (#qid != null) #id = #qid;
         #variableDefinitions = #(create(METHOD_DEF,"METHOD_DEF",first,LT(1)), mods, #(create(TYPE,"TYPE",first,LT(1)), t), id, param, tc, mb);
        }
    ;

/** I've split out constructors separately; we could maybe integrate back into variableDefinitions
 *  later on if we maybe simplified 'def' to be a type declaration?
 */
constructorDefinition[AST mods]  {Token first = cloneToken(LT(1));
                                  if (mods != null) {
                                           first.setLine(mods.getLine());
                                           first.setColumn(mods.getColumn());
                                  }}
    :
        id:IDENT

        // parse the formal parameter declarations
        LPAREN! param:parameterDeclarationList! RPAREN!

        // get the list of exceptions that this method is
        // declared to throw
        ((nls "throws") => tc:throwsClause!)? nlsWarn!

        cb:constructorBody!
        {#constructorDefinition = #(create(CTOR_IDENT,"CTOR_IDENT",first,LT(0)), mods, param, tc, cb);}
     ;

/** Declaration of a variable. This can be a class/instance variable,
 *  or a local variable in a method
 *  It can also include possible initialization.
 */
variableDeclarator![AST mods, AST t,Token first]
    :
        id:variableName
        (v:varInitializer)?
        {#variableDeclarator = #(create(VARIABLE_DEF,"VARIABLE_DEF",first,LT(1)), mods, #(create(TYPE,"TYPE",first,LT(1)), t), id, v);}
    ;

/** Used in cases where a declaration cannot have commas, or ends with the "in" operator instead of '='. */
singleVariable![AST mods, AST t]  {Token first = LT(1);}
    :
        id:variableName
        {#singleVariable = #(create(VARIABLE_DEF,"VARIABLE_DEF",first,LT(1)), mods, #(create(TYPE,"TYPE",first,LT(1)),t), id);}
    ;

variableName
    :   IDENT
    ;

/** After some type names, where zero or more empty bracket pairs are allowed.
 *  We use ARRAY_DECLARATOR to represent this.
 */
declaratorBrackets[AST typ]
    :   {#declaratorBrackets=typ;}
        (
            // A following list constructor might conflict with index brackets; prefer the declarator.
            options {greedy=true;} :
            LBRACK!
            RBRACK!
            {#declaratorBrackets = #(create(ARRAY_DECLARATOR,"[",typ,LT(1)), #declaratorBrackets);}
        )*
    ;

/** An assignment operator '=' followed by an expression.  (Never empty.) */
varInitializer
    :   ASSIGN^ nls! expressionStatementNoCheck
        // In {T x = y}, the left-context of y is that of an initializer.
    ;

// This is a list of exception classes that the method is declared to throw
throwsClause
    :   nls! "throws"^ nls! identifier ( COMMA! nls! identifier )*
    ;

/** A list of zero or more formal parameters.
 *  If a parameter is variable length (e.g. String... myArg) it should be
 *  to the right of any other parameters of the same kind.
 *  General form:  (req, ..., opt, ..., [rest], key, ..., [restKeys], [block]
 *  This must be sorted out after parsing, since the various declaration forms
 *  are impossible to tell apart without backtracking.
 */
parameterDeclarationList  {Token first = LT(1);}
    :
        (
            parameterDeclaration
            (   COMMA! nls!
                parameterDeclaration
            )*
        )?
        {#parameterDeclarationList = #(create(PARAMETERS,"PARAMETERS",first,LT(1)), #parameterDeclarationList);}
    ;

/** A formal parameter for a method or closure. */
parameterDeclaration!
        { Token first = LT(1);boolean spreadParam = false; }
    :
        pm:parameterModifiersOpt
        (   options {greedy=true;} :
            t:typeSpec[false]
        )?

        // TODO:  What do formal parameters for keyword arguments look like?

        // Java-style var args
        ( TRIPLE_DOT! { spreadParam = true; } )?

        id:IDENT

        // allow an optional default value expression
        (exp:varInitializer)?

        {
            if (spreadParam) {
                #parameterDeclaration = #(create(VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF",first,LT(1)), pm, #(create(TYPE,"TYPE",first,LT(1)), t), id, exp);
            } else {
                #parameterDeclaration = #(create(PARAMETER_DEF,"PARAMETER_DEF",first,LT(1)), pm, #(create(TYPE,"TYPE",first,LT(1)), t), id, exp);
            }
        }
    ;

multicatch_types
{Token first = LT(1);}
    :
        nls!
        classOrInterfaceType[false]
        (
            BOR! nls! classOrInterfaceType[false]
        )*

        {#multicatch_types = #(create(MULTICATCH_TYPES,"MULTICATCH_TYPES",first,LT(1)), #multicatch_types);}
    ;

multicatch
{Token first = LT(1);}
    :   nls! (FINAL)? ("def")? (m:multicatch_types)? id:IDENT!
        {
          #multicatch = #(create(MULTICATCH,"MULTICATCH",first,LT(1)), m, id);
        }
    ;

parameterModifiersOpt
        { Token first = LT(1);int seenDef = 0; }
        //final and/or def can appear amongst annotations in any order
    :   (   {seenDef++ == 0}?       // do not allow multiple "def" tokens
            "def"!  nls!            // redundant, but allowed for symmetry
        |   "final" nls!
        |   annotation nls!
        )*
        {#parameterModifiersOpt = #(create(MODIFIERS,"MODIFIERS",first,LT(1)), #parameterModifiersOpt);}
    ;

/** Closure parameters are exactly like method parameters,
 *  except that they are not enclosed in parentheses, but rather
 *  are prepended to the front of a block, just after the brace.
 *  They are separated from the closure body by a CLOSABLE_BLOCK_OP token '->'.
 */
// With '|' there would be restrictions on bitwise-or expressions.
closableBlockParamsOpt[boolean addImplicit]
    :   (parameterDeclarationList nls CLOSABLE_BLOCK_OP)=>
        parameterDeclarationList nls! CLOSABLE_BLOCK_OP! nls!
    |   {addImplicit}?
        implicitParameters
    |
        /* else do not parse any parameters at all */
    ;

/** Lookahead to check whether a block begins with explicit closure arguments. */
closableBlockParamsStart!
    :
        nls parameterDeclarationList nls CLOSABLE_BLOCK_OP
    ;

/** Simple names, as in {x|...}, are completely equivalent to {(def x)|...}.  Build the right AST. */
closableBlockParam!  {Token first = LT(1);}
    :   id:IDENT!
        {#closableBlockParam = #(create(PARAMETER_DEF,"PARAMETER_DEF",first,LT(1)), #(create(MODIFIERS,"MODIFIERS",first,LT(1))), #(create(TYPE,"TYPE",first,LT(1))), id);}
    ;

// Compound statement. This is used in many contexts:
// Inside a class definition prefixed with "static":
// it is a class initializer
// Inside a class definition without "static":
// it is an instance initializer
// As the body of a method
// As a completely independent braced block of code inside a method
// it starts a new scope for variable definitions
// In Groovy, this is called an "open block".  It cannot have closure arguments.

compoundStatement
    :   openBlock
    ;

/** An open block is not allowed to have closure arguments. */
openBlock  {Token first = LT(1);}
    :   LCURLY! nls!
        // AST type of SLIST means "never gonna be a closure"
        bb:blockBody[EOF]!
        RCURLY!
        {#openBlock = #(create(SLIST,"{",first,LT(1)), bb);}

    ;

/** A block body is a parade of zero or more statements or expressions. */
blockBody[int prevToken]
    :
        (statement[prevToken])? (sep! (statement[sepToken])?)*
    ;

/** A block which is known to be a closure, even if it has no apparent arguments.
 *  A block inside an expression or after a method call is always assumed to be a closure.
 *  Only labeled, unparameterized blocks which occur directly as substatements are kept open.
 */
closableBlock  {Token first = LT(1);}
    :   LCURLY! nls!
        cbp:closableBlockParamsOpt[true]!
        bb:blockBody[EOF]!
        RCURLY!
        {#closableBlock = #(create(CLOSABLE_BLOCK,"{",first,LT(1)), cbp, bb);}
    ;

/** A block known to be a closure, but which omits its arguments, is given this placeholder.
 *  A subsequent pass is responsible for deciding if there is an implicit 'it' parameter,
 *  or if the parameter list should be empty.
 */
implicitParameters  {Token first = LT(1);}
    :   {   #implicitParameters = #(create(IMPLICIT_PARAMETERS,"IMPLICIT_PARAMETERS",first,LT(1)));  }
    ;

/** A sub-block of a block can be either open or closable.
 *  It is closable if and only if there are explicit closure arguments.
 *  Compare this to a block which is appended to a method call,
 *  which is given closure arguments, even if they are not explicit in the code.
 */
openOrClosableBlock  {Token first = LT(1);}
    :   LCURLY! nls!
        cp:closableBlockParamsOpt[false]!
        bb:blockBody[EOF]!
        RCURLY!
        {
            if (#cp == null)
                #openOrClosableBlock = #(create(SLIST,"{",first,LT(1)), bb);
            else
                #openOrClosableBlock = #(create(CLOSABLE_BLOCK,"{",first,LT(1)), cp, bb);
        }
    ;

/** A statement is an element of a block.
 *  Typical statements are declarations (which are scoped to the block)
 *  and expressions.
 */
statement[int prevToken]
{boolean sce = false; Token first = LT(1); AST casesGroup_AST = null;}
    // prevToken is NLS if previous statement is separated only by a newline

    :  (genericMethodStart)=>
        genericMethod

    |  (multipleAssignmentDeclarationStart)=>
        multipleAssignmentDeclaration

    // declarations are ambiguous with "ID DOT" relative to expression
    // statements. Must backtrack to be sure. Could use a semantic
    // predicate to test symbol table to see what the type was coming
    // up, but that's pretty hard without a symbol table ;)
    |  (declarationStart)=>
        declaration

    // Attach a label to the front of a statement
    // This block is executed for effect, unless it has an explicit closure argument.
    |
       (IDENT COLON)=>
        pfx:statementLabelPrefix!
        {#statement = #pfx;}  // nest it all under the label prefix
        (   (LCURLY) => openOrClosableBlock
        |   statement[COLON]
        )

    // An expression statement. This could be a method call,
    // assignment statement, or any other expression evaluated for
    // side-effects.
    // The prevToken is used to check for dumb expressions like +1.
    |    es:expressionStatement[prevToken]
        //{#statement = #(create(EXPR,"EXPR",first,LT(1)), es);}

    // If-else statement
    |   "if"! LPAREN! ale:assignmentLessExpression! RPAREN! nlsWarn! ifCbs:compatibleBodyStatement!
        (
            // CONFLICT: the old "dangling-else" problem...
            //           ANTLR generates proper code matching
            //                       as soon as possible.  Hush warning.
            options {
                    warnWhenFollowAmbig = false;
            }
        :   // lookahead to check if we're entering an 'else' clause
            ( (sep!)? "else"! )=>
            (sep!)?  // allow SEMI here for compatibility with Java
            "else"! nlsWarn! elseCbs:compatibleBodyStatement!
        )?
        {#statement = #(create(LITERAL_if,"if",first,LT(1)), ale, ifCbs, elseCbs);}

    // For statement
    |   forStatement

    // While statement
    |   "while"! LPAREN! sce=while_sce:strictContextExpression[false]! RPAREN! nlsWarn!
        (s:SEMI! | while_cbs:compatibleBodyStatement!)
        {
            if (#s != null)
                #statement = #(create(LITERAL_while,"Literal_while",first,LT(1)), while_sce, s);
            else
                #statement = #(create(LITERAL_while,"Literal_while",first,LT(1)), while_sce, while_cbs);
        }

    // Import statement.  Can be used in any scope.  Has "import x as y" also.
    |   (annotationsOpt "import") => importStatement

    // class definition
    |   m:modifiersOpt! typeDefinitionInternal[#m]

    // switch/case statement
    |   "switch"! LPAREN! sce=switchSce:strictContextExpression[false]! RPAREN! nlsWarn! LCURLY! nls!
        ( cg:casesGroup!
          //expand the list of nodes for each catch statement
              {casesGroup_AST = #(null, casesGroup_AST, cg);})*
        RCURLY!
        {#statement = #(create(LITERAL_switch,"switch",first,LT(1)),switchSce,casesGroup_AST);}

    // exception try-catch block
    |   tryBlock

    // synchronize a statement
    |   "synchronized"! LPAREN! sce=synch_sce:strictContextExpression[false]! RPAREN! nlsWarn! synch_cs:compoundStatement!
        {#statement = #(create(LITERAL_synchronized,"synchronized",first,LT(1)), synch_sce, synch_cs);}

    |   branchStatement
    ;

forStatement {Token first = LT(1);}
    :   "for"!
        LPAREN!
        (   (SEMI | (strictContextExpression[true] SEMI)) => cl:closureList!
        |   // the coast is clear; it's a modern Groovy for statement
            fic:forInClause!
        )
        RPAREN! nls!
        (s:SEMI! | forCbs:compatibleBodyStatement!)                                  // statement to loop over
        {
            if (#cl != null) {
                if (#s != null)
                    #forStatement = #(create(LITERAL_for,"for",first,LT(1)), cl, s);
                else
                    #forStatement = #(create(LITERAL_for,"for",first,LT(1)), cl, forCbs);
            } else {
                if (#s != null)
                    #forStatement = #(create(LITERAL_for,"for",first,LT(1)), fic, s);
                else
                    #forStatement = #(create(LITERAL_for,"for",first,LT(1)), fic, forCbs);
            }
        }

    ;

closureList
    {Token first = LT(1); boolean sce=false;}
    :

        (   sce=strictContextExpression[true]
            |    {astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT"));}
        )
        (
                SEMI! sce=strictContextExpression[true]
           |    SEMI! {astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT"));}
        )+
        {#closureList = #(create(CLOSURE_LIST,"CLOSURE_LIST",first,LT(1)), #closureList);}
    ;

forInClause
    :   (   (declarationStart)=>
            decl:singleDeclarationNoInit
        |   IDENT
        )
        (
            i:"in"^         {#i.setType(FOR_IN_ITERABLE);}
            shiftExpression[0]
        |
            { addWarning(
              "A colon at this point is legal Java but not recommended in Groovy.",
              "Use the 'in' keyword."
              );
            require(#decl != null,
                "Java-style for-each statement requires a type declaration."
                ,
                "Use the 'in' keyword, as for (x in y) {...}"
                );
            }
            c:COLON^         {#c.setType(FOR_IN_ITERABLE);}
            expression[0]
        )
    ;

/** In Java, "if", "while", and "for" statements can take random, non-braced statements as their bodies.
 *  Support this practice, even though it isn't very Groovy.
 */
compatibleBodyStatement {Token first = LT(1);}
    :   (LCURLY)=>
        compoundStatement
    // comma sep decl case converted to multiple statements so must be wrapped in SLIST when single statement occurs after if/while/for
    |  (declarationStart (varInitializer)? COMMA)=>
        de:declaration
        {#compatibleBodyStatement = #(create(SLIST,"CBSLIST",first,LT(1)), de);}
    |
        statement[EOF]
    ;

/** In Groovy, return, break, continue, throw, and assert can be used in a parenthesized expression context.
 *  Example:  println (x || (return));  println assert x, "won't print a false value!"
 *  If an optional expression is missing, its value is void (this coerces to null when a value is required).
 */
branchStatement {Token first = LT(1);}
    :
    // Return an expression
        "return"!
        ( returnE:expression[0]! )?
        {#branchStatement = #(create(LITERAL_return,"return",first,LT(1)), returnE);}


    // break:  get out of a loop, or switch, or method call
    // continue:  do next iteration of a loop, or leave a closure
    |   "break"!
        ( breakI:IDENT! )?
        {#branchStatement = #(create(LITERAL_break,"break",first,LT(1)), breakI);}

    | "continue"!
        ( contI:IDENT! )?
        {#branchStatement = #(create(LITERAL_continue,"continue",first,LT(1)), contI);}

    // throw an exception
    |   "throw"! throwE:expression[0]!
        {#branchStatement = #(create(LITERAL_throw,"throw",first,LT(1)), throwE);}


    // groovy assertion...
    |   "assert"! assertAle: assignmentLessExpression!
        (   options {greedy=true;} :
            (   COMMA! nls! // TODO:  gratuitous change caused failures
            |   COLON! nls! // standard Java syntax, but looks funny in Groovy
            )
            assertE:expression[0]!
        )?
        {#branchStatement = #(create(LITERAL_assert,"assert",first,LT(1)), assertAle, assertE);}
    ;

/** A labeled statement, consisting of a vanilla identifier followed by a colon. */
// Note:  Always use this lookahead, to keep antlr from panicking: (IDENT COLON)=>
statementLabelPrefix
    :   IDENT c:COLON^ {#c.setType(LABELED_STAT);} nls!
    ;

/** An expression statement can be any general expression.
 *  <p>
 *  An expression statement can also be a <em>command</em>,
 *  which is a simple method call in which the outermost parentheses are omitted.
 *  <p>
 *  Certain "suspicious" looking forms are flagged for the user to disambiguate.
 */
// DECIDE: A later semantic pass can flag dumb expressions that don't occur in
//         positions where their value is not used, e.g., <code>{1+1;println}</code>
expressionStatement[int prevToken]
        { Token first = LT(1); }
    :
        ( (suspiciousExpressionStatementStart) =>
            checkSuspiciousExpressionStatement[prevToken]
        )?
        esn:expressionStatementNoCheck
        { #expressionStatement = #(create(EXPR,"EXPR",first,LT(1)), #esn); }
    ;

expressionStatementNoCheck
        { boolean isPathExpr = true; }
    :
        // Checks are now out of the way; here's the real rule:
        head:expression[LC_STMT]
        { isPathExpr = (#head == lastPathExpression); }
        (
            // A path expression (e.g., System.out.print) can take arguments.
            {LA(1)!=LITERAL_else && isPathExpr}?
            cmd:commandArgumentsGreedy[#head]!
            {
                #expressionStatementNoCheck = #cmd;
            }
        )?
    ;

/**
 *  If two statements are separated by newline (not SEMI), the second had
 *  better not look like the latter half of an expression.  If it does, issue a warning.
 *  <p>
 *  Also, if the expression starts with a closure, it needs to
 *  have an explicit parameter list, in order to avoid the appearance of a
 *  compound statement.  This is a hard error.
 *  <p>
 *  These rules are different from Java's "dumb expression" restriction.
 *  Unlike Java, Groovy blocks can end with arbitrary (even dumb) expressions,
 *  as a consequence of optional 'return' and 'continue' tokens.
 * <p>
 *  To make the programmer's intention clear, a leading closure must have an
 *  explicit parameter list, and must not follow a previous statement separated
 *  only by newlines.
 */
checkSuspiciousExpressionStatement[int prevToken]
    :
        (~LCURLY | LCURLY closableBlockParamsStart)=>  //FIXME too much lookahead
        // Either not a block, or a block with an explicit closure parameter list.
        (   {prevToken == NLS}?
            {   addWarning(
                "Expression statement looks like it may continue a previous statement",
                "Either remove the previous newline, or add an explicit semicolon ';'.");
            }
        )?
    |
        // Else we have a block without any visible closure parameters.
        {prevToken == NLS}?
        // if prevToken is NLS, we have double trouble; issue a double warning
        // Example:  obj.foo \n {println x}
        // Might be appended block:  obj.foo {println x}
        // Might be closure expression:  obj.foo ; {x->println x}
        // Might be open block:  obj.foo ; L:{println x}
        {   require(false,
            "Ambiguous expression could be a parameterless closure expression, "+
            "an isolated open code block, or it may continue a previous statement",
            "Add an explicit parameter list, e.g. {it -> ...}, or force it to be treated "+
            "as an open block by giving it a label, e.g. L:{...}, "+
            "and also either remove the previous newline, or add an explicit semicolon ';'"
            );
        }
    |
        {prevToken != NLS}?
        // If prevToken is SEMI or something else, issue a single warning:
        // Example:  obj.foo ; {println x}
        // Might be closure expression:  obj.foo ; {x->println x}
        // Might be open block:  obj.foo ; L:{println x}
        {   require(false,
            "Ambiguous expression could be either a parameterless closure expression or "+
            "an isolated open code block",
            "Add an explicit closure parameter list, e.g. {it -> ...}, or force it to "+
            "be treated as an open block by giving it a label, e.g. L:{...}");
        }
    ;

/** Lookahead for suspicious statement warnings and errors. */
suspiciousExpressionStatementStart
    :
        (   (PLUS | MINUS)
        |   (LBRACK | LPAREN | LCURLY)
        )
        // TODO:  Expand this set?
    ;

// Support for switch/case:
casesGroup  {Token first = LT(1);}
    :   (   // CONFLICT: to which case group do the statements bind?
            // ANTLR generates proper code: it groups the
            // many "case"/"default" labels together then
            // follows them with the statements
            options {
                greedy = true;
            }
            :
            aCase
        )+
        caseSList
        {#casesGroup = #(create(CASE_GROUP,"CASE_GROUP",first,LT(1)), #casesGroup);}
    ;

aCase
    :   ("case"^ expression[0] | "default") COLON! nls!
    ;

caseSList  {Token first = LT(1);}
    :   statement[COLON] (sep! (statement[sepToken])?)*
        {#caseSList = #(create(SLIST,"SLIST",first,LT(1)), #caseSList);}
    ;

// The initializer for a for loop
forInit  {Token first = LT(1);}
    :   // if it looks like a declaration, it is
        (declarationStart)=> declaration
    |   // else it's a comma-separated list of expressions
        (controlExpressionList)?
        {#forInit = #(create(FOR_INIT,"FOR_INIT",first,LT(1)), #forInit);}
    ;

forCond  {Token first = LT(1); boolean sce=false;}
    :   (sce=strictContextExpression[false])?
        {#forCond = #(create(FOR_CONDITION,"FOR_CONDITION",first,LT(1)), #forCond);}
    ;

forIter  {Token first = LT(1);}
    :   (controlExpressionList)?
        {#forIter = #(create(FOR_ITERATOR,"FOR_ITERATOR",first,LT(1)), #forIter);}
    ;

// an exception handler try/catch block
tryBlock {Token first = LT(1);List catchNodes = new ArrayList();AST newHandler_AST = null;}
    :   "try"! nlsWarn! tryCs:compoundStatement!
            ( options {greedy=true;} : {!(LA(1) == NLS && LA(2) == LPAREN)}? nls! h:handler!

              //expand the list of nodes for each catch statement
              {newHandler_AST = #(null,newHandler_AST,h);}   )*
            ( options {greedy=true;} :  nls! fc:finallyClause!)?

        {#tryBlock = #(create(LITERAL_try,"try",first,LT(1)), tryCs, newHandler_AST, fc);}
    ;

finallyClause {Token first = LT(1);}
    :   "finally"! nlsWarn! finallyCs:compoundStatement!
        {#finallyClause = #(create(LITERAL_finally,"finally",first,LT(1)), finallyCs);}
    ;

// an exception handler
handler {Token first = LT(1);}
    :   "catch"! LPAREN! pd:multicatch! RPAREN! nlsWarn! handlerCs:compoundStatement!
        {#handler = #(create(LITERAL_catch,"catch",first,LT(1)), pd, handlerCs);}
    ;

/** A member name (x.y) or element name (x[y]) can serve as a command name,
 *  which may be followed by a list of arguments.
 *  Unlike parenthesized arguments, these must be plain expressions,
 *  without labels or spread operators.
 */
commandArguments[AST head]
{
    Token first = LT(1);
}
    :
        commandArgument ( options {greedy=true;}: COMMA! nls! commandArgument )*
        // println 2+2 //OK
        // println(2+2) //OK
        // println (2)+2 //BAD
        // println((2)+2) //OK
        // (println(2)+2) //OK
        // compare (2), 2 //BAD
        // compare( (2), 2 ) //OK
        // foo.bar baz{bat}, bang{boz} //OK
        {
            AST elist = #(create(ELIST,"ELIST",first,LT(1)), #commandArguments);
            AST headid = #(create(METHOD_CALL,"<command>",first,LT(1)), head, elist);
            #commandArguments = headid;
        }
    ;

commandArgumentsGreedy[AST head]
{
    AST prev = #head;
}
    :
        // argument to the already existing method name
        (
            ({#prev==null || #prev.getType()!=METHOD_CALL}? commandArgument) => (
                first : commandArguments[head]!
                { #prev = #first; }
            )
            |
        )
        // we start a series of methods and arguments
        (   options { greedy = true; } :
            (   options { greedy = true; } :
                // method name
                pre:primaryExpression!
                { #prev = #(create(DOT, ".", #prev), #prev, #pre); }
                // what follows is either a normal argument, parens,
                // an appended block, an index operation, or nothing
                // parens (a b already processed):
                //      a b c() d e -> a(b).c().d(e)
                //      a b c()() d e -> a(b).c().call().d(e)
                // index (a b already processed):
                //      a b c[x] d e -> a(b).c[x].d(e)
                //      a b c[x][y] d e -> a(b).c[x][y].d(e)
                // block (a b already processed):
                //      a b c {x} d e -> a(b).c({x}).d(e)
                //
                // parens/block completes method call
                // index makes method call to property get with index
                //
                (options {greedy=true;}:
                (pathElementStart) =>
                    (
                        pc:pathChain[LC_STMT,#prev]!
                        { #prev = #pc; }
                    )
                |
                    (   ca:commandArguments[#prev]!
                        { #prev = #ca; }
                    )
                )?
            )*
        )
        { #commandArgumentsGreedy = prev; }
    ;

commandArgument
    :
        (argumentLabel COLON nls!) => (
            argumentLabel c:COLON^ nls! expression[0]  { #c.setType(LABELED_ARG); }
        )
        | expression[0]
    ;

// expressions
// Note that most of these expressions follow the pattern
//   thisLevelExpression :
//         nextHigherPrecedenceExpression
//                 (OPERATOR nextHigherPrecedenceExpression)*
// which is a standard recursive definition for a parsing an expression.
// The operators have the following precedences:
//      lowest  ( 15)  = **= *= /= %= += -= <<= >>= >>>= &= ^= |= (assignments)
//              ( 14)  ?: (conditional expression and elvis)
//              ( 13)  || (logical or)
//              ( 12)  && (logical and)
//              ( 11)  | ()binary or
//              ( 10)  ^ (binary xor)
//              (  9)  & (binary and)
//              (8.5)  =~ ==~ (regex find/match)
//              (  8)  == != <=> === !== (equals, not equals, compareTo)
//              (  7)  < <= > >= instanceof as in (relational, in, instanceof, type coercion)
//              (  6)  << >> >>> .. ..< (shift, range)
//              (  5)  + - (addition, subtraction)
//              (  4)  * / % (multiply div modulo)
//              (  3)  ++ -- + - (pre dec/increment, unary signs)
//              (  2)  ** (power)
//              (  1)  ~ ! $ (type) (negate, not, typecast)
//                     ?. * *. *: (safe dereference, spread, spread-dot, spread-map)
//                     . .& .@ (member access, method closure, field/attribute access)
//                     [] ++ -- (list/map/array index, post inc/decrement)
//                     () {} [] (method call, closableBlock, list/map literal)
//                     new () (object creation, explicit parenthesis)
//
// the last two are not usually on a precedence chart; I put them in
// to point out that new has a higher precedence than '.', so you
// can validly use
//       new Frame().show()
//
// Note that the above precedence levels map to the rules below...
// Once you have a precedence chart, writing the appropriate rules as below
// is usually very straightforward


// the mother of all expressions
// This nonterminal is not used for expression statements, which have a more restricted syntax
// due to possible ambiguities with other kinds of statements.  This nonterminal is used only
// in contexts where we know we have an expression.  It allows general Java-type expressions.
expression[int lc_stmt]
    :
//        (LPAREN typeSpec[true] RPAREN expression[lc_stmt])=>
//            lp:LPAREN^ {#lp.setType(TYPECAST);} typeSpec[true] RPAREN!
//            expression[lc_stmt]
//    |
       (LPAREN nls IDENT (COMMA nls IDENT)* RPAREN ASSIGN) =>
        m:multipleAssignment[lc_stmt] {#expression=#m;}
    |   assignmentExpression[lc_stmt]
    ;

multipleAssignment[int lc_stmt] {Token first = cloneToken(LT(1));}
    :   LPAREN^ nls! listOfVariables[null,null,first] RPAREN!
        ASSIGN^ nls!
        (
          (LPAREN nls IDENT (COMMA nls IDENT)* RPAREN ASSIGN) => multipleAssignment[lc_stmt]
          | assignmentExpression[lc_stmt]
        )
    ;

// This is a list of expressions.
// Used for backward compatibility, in a few places where
// comma-separated lists of Java expression statements and declarations are required.
controlExpressionList  {Token first = LT(1); boolean sce=false;}
    :   sce=strictContextExpression[false] (COMMA! nls! sce=strictContextExpression[false])*
        {#controlExpressionList = #(create(ELIST,"ELIST",first,LT(1)), controlExpressionList);}
    ;

pathChain[int lc_stmt, AST prefix]
    :
        (
            options {
                // \n{foo} could match here or could begin a new statement
                // We do want to match here. Turn off warning.
                greedy=true;
                // This turns the ambiguity warning of the second alternative
                // off. See below. (The "ANTLR_LOOP_EXIT" predicate makes it non-issue)
                //@@ warnWhenFollowAmbig=false;
            }
            // Parsing of this chain is greedy.  For example, a pathExpression may be a command name
            // followed by a command argument, but that command argument cannot begin with an LPAREN,
            // since a parenthesized expression is greedily attached to the pathExpression as a method argument.
            // The lookahead is also necessary to reach across newline in foo \n {bar}.
            // (Apparently antlr's basic approximate LL(k) lookahead is too weak for this.)
        :   (pathElementStart)=>
            nls!
            pe:pathElement[prefix]!
            { prefix = #pe; }
        |
            {lc_stmt == LC_STMT || lc_stmt == LC_INIT}?
            (nls LCURLY)=>
            nlsWarn!
            apb:appendedBlock[prefix]!
            { prefix = #apb; }
        )+

        { #pathChain = prefix; }
    ;

/** A "path expression" is a name or other primary, possibly qualified by various
 *  forms of dot, and/or followed by various kinds of brackets.
 *  It can be used for value or assigned to, or else further qualified, indexed, or called.
 *  It is called a "path" because it looks like a linear path through a data structure.
 *  Examples:  x.y, x?.y, x*.y, x.@y; x[], x[y], x[y,z]; x(), x(y), x(y,z); x{s}; a.b[n].c(x).d{s}
 *  (Compare to a C lvalue, or LeftHandSide in the JLS section 15.26.)
 *  General expressions are built up from path expressions, using operators like '+' and '='.
 */
pathExpression[int lc_stmt]
        { AST prefix = null; }
    :
        pre:primaryExpression!
        { prefix = #pre; }
        (
            options {
                // \n{foo} could match here or could begin a new statement
                // We do want to match here. Turn off warning.
                greedy=true;
                // This turns the ambiguity warning of the second alternative
                // off. See below. (The "ANTLR_LOOP_EXIT" predicate makes it non-issue)
                //@@ warnWhenFollowAmbig=false;
            }
            // Parsing of this chain is greedy.  For example, a pathExpression may be a command name
            // followed by a command argument, but that command argument cannot begin with an LPAREN,
            // since a parenthesized expression is greedily attached to the pathExpression as a method argument.
            // The lookahead is also necessary to reach across newline in foo \n {bar}.
            // (Apparently antlr's basic approximate LL(k) lookahead is too weak for this.)
        :   (pathElementStart)=>
            nls!
            pe:pathElement[prefix]!
            { prefix = #pe; }
        |
            {lc_stmt == LC_STMT || lc_stmt == LC_INIT}?
            (nls LCURLY)=>
            nlsWarn!
            apb:appendedBlock[prefix]!
            { prefix = #apb; }
        )*
        {
            #pathExpression = prefix;
            lastPathExpression = #pathExpression;
        }
    ;

pathElement[AST prefix] {Token operator = LT(1);}
        // The primary can then be followed by a chain of .id, (a), [a], and {...}
    :
        { #pathElement = prefix; }
        ( nls!
            ( SPREAD_DOT!     // Spread operator:  x*.y  ===  x?.collect{it.y}
            |
              OPTIONAL_DOT!   // Optional-null operator:  x?.y  === (x==null)?null:x.y
            |
              MEMBER_POINTER! // Member pointer operator: foo.&y == foo.metaClass.getMethodPointer(foo, "y")
            |
              DOT!            // The all-powerful dot.
            )
        ) nls!
        (ta:typeArguments!)?
        np:namePart!
        { #pathElement = #(create(operator.getType(),operator.getText(),prefix,LT(1)), prefix, ta, np); }

    |
        mca:methodCallArgs[prefix]!
        {   #pathElement = #mca;  }
    |
        // Can always append a block, as foo{bar}
        apb:appendedBlock[prefix]!
        {   #pathElement = #apb;  }
    |
        // Element selection is always an option, too.
        // In Groovy, the stuff between brackets is a general argument list,
        // since the bracket operator is transformed into a method call.
        ipa:indexPropertyArgs[prefix]!
        {   #pathElement = #ipa;  }
    ;

pathElementStart!
    :   (nls! ( DOT
                |   SPREAD_DOT
                |   OPTIONAL_DOT
                |   MEMBER_POINTER ) )
        |   LBRACK
        |   LPAREN
        |   LCURLY
    ;

/** This is the grammar for what can follow a dot:  x.a, x.@a, x.&a, x.'a', etc.
 *  Note: <code>typeArguments</code> is handled by the caller of <code>namePart</code>.
 */
namePart  {Token first = LT(1);}
    :
        (   ats:AT^     {#ats.setType(SELECT_SLOT);}  )?
        // foo.@bar selects the field (or attribute), not property

        (   IDENT
        |   sl:STRING_LITERAL {#sl.setType(IDENT);}
            // foo.'bar' is in all ways same as foo.bar, except that bar can have an arbitrary spelling
        |   dynamicMemberName
        |
            openBlock
            // PROPOSAL, DECIDE:  Is this inline form of the 'with' statement useful?
            // Definition:  a.{foo} === {with(a) {foo}}
            // May cover some path expression use-cases previously handled by dynamic scoping (closure delegates).

            // let's allow common keywords as property names
        |   keywordPropertyNames
        )

        // (No, x.&@y is not needed; just say x.&y as Slot or some such.)
    ;

/*
 * Allowed keywords after dot (as a member name) and before colon (as a label).
 * Includes all Java keywords plus "as", "def", "in", and "trait".
 */
keywordPropertyNames
    :   (
          "as"
        | "assert"
        | "break"
        | "case"
        | "catch"
        | "class"
        | "const"
        | "continue"
        | "def"
        | "default"
        | "do"
        | "else"
        | "enum"
        | "extends"
        | "false"
        | "finally"
        | "for"
        | "goto"
        | "if"
        | "implements"
        | "import"
        | "in"
        | "instanceof"
        | "interface"
        | "new"
        | "null"
        | "package"
        | "return"
        | "super"
        | "switch"
        | "this"
        | "throw"
        | "throws"
        | "trait"
        | "true"
        | "try"
        | "while"
        | modifier
        | builtInType
        )
        { #keywordPropertyNames.setType(IDENT); }
    ;

/** If a dot is followed by a parenthesized or quoted expression, the member is computed dynamically,
 *  and the member selection is done only at runtime.  This forces a statically unchecked member access.
 */
dynamicMemberName  {Token first = LT(1);}
    :   (   pe:parenthesizedExpression!
            {#dynamicMemberName = #(create(EXPR,"EXPR",first,LT(1)),pe);}
        |   stringConstructorExpression
        )
        { #dynamicMemberName = #(create(DYNAMIC_MEMBER, "DYNAMIC_MEMBER",first,LT(1)), #dynamicMemberName); }
    ;

/** An expression may be followed by one or both of (...) and {...}.
 *  Note: If either is (...) or {...} present, it is a method call.
 *  The {...} is appended to the argument list, and matches a formal of type Closure.
 *  If there is no method member, a property (or field) is used instead, and must itself be callable.
 *  <p>
 *  If the methodCallArgs are absent, it is a property reference.
 *  If there is no property, it is treated as a field reference, but never a method reference.
 *  <p>
 *  Arguments in the (...) can be labeled, and the appended block can be labeled also.
 *  If there is a mix of unlabeled and labeled arguments,
 *  all the labeled arguments must follow the unlabeled arguments,
 *  except that the closure (labeled or not) is always a separate final argument.
 *  Labeled arguments are collected up and passed as a single argument to a formal of type Map.
 *  <p>
 *  Therefore, f(x,y, a:p, b:q) {s} is equivalent in all ways to f(x,y, [a:p,b:q], {s}).
 *  Spread arguments of sequence type count as unlabeled arguments,
 *  while spread arguments of map type count as labeled arguments.
 *  (This distinction must sometimes be checked dynamically.)
 *
 *  A plain unlabeled argument is allowed to match a trailing Map or Closure argument:
 *  f(x, a:p) {s}  ===  f(*[ x, [a:p], {s} ])
 */
// AST is [METHOD_CALL, callee, ELIST? CLOSABLE_BLOCK?].
// Note that callee is often of the form x.y but not always.
// If the callee is not of the form x.y, then an implicit .call is needed.
// Parameter callee is only "null" when called from newExpression
methodCallArgs[AST callee]
    :
        LPAREN!
        al:argList!
        RPAREN!
        { if (callee != null && callee.getFirstChild() != null) {
              //method call like obj.method()
              #methodCallArgs = #(create(METHOD_CALL,"(",callee.getFirstChild(),LT(1)), callee, al);
          } else {
              //method call like method() or new Expr(), in the latter case "callee" is null
              #methodCallArgs = #(create(METHOD_CALL,"(",callee, LT(1)), callee, al);
          }
        }
    ;

/** An appended block follows any expression.
 *  If the expression is not a method call, it is given an empty argument list.
 */
appendedBlock[AST callee]
    :
        /*  FIXME DECIDE: should appended blocks accept labels?
        (   (IDENT COLON nls LCURLY)=>
            IDENT c:COLON^ {#c.setType(LABELED_ARG);} nls!
        )? */
        cb:closableBlock!
        {
            // If the callee is itself a call, flatten the AST.
            if (callee != null && callee.getType() == METHOD_CALL) {
                #appendedBlock = #(create(METHOD_CALL, "(",callee,LT(1)),
                                   callee.getFirstChild(), cb);
            } else {
                #appendedBlock = #(create(METHOD_CALL, "{",callee,LT(1)), callee, cb);
            }
        }
    ;

/** An expression may be followed by [...].
 *  Unlike Java, these brackets may contain a general argument list,
 *  which is passed to the array element operator, which can make of it what it wants.
 *  The brackets may also be empty, as in T[].  This is how Groovy names array types.
 *  <p>Returned AST is [INDEX_OP, indexee, ELIST].
 */
indexPropertyArgs[AST indexee]
    :
        lb:LBRACK
        al:argList!
        RBRACK!
        { if (indexee != null && indexee.getFirstChild() != null) {
              //expression like obj.index[]
              #indexPropertyArgs = #(create(INDEX_OP, "INDEX_OP",indexee.getFirstChild(),LT(1)), lb, indexee, al);
          } else {
              //expression like obj[]
              #indexPropertyArgs = #(create(INDEX_OP, "INDEX_OP",indexee,LT(1)), lb, indexee, al);
          }
        }
    ;

// assignment expression (level 15)
assignmentExpression[int lc_stmt]
    :   conditionalExpression[lc_stmt]
        (
            (   ASSIGN^
            |   PLUS_ASSIGN^
            |   MINUS_ASSIGN^
            |   STAR_ASSIGN^
            |   DIV_ASSIGN^
            |   MOD_ASSIGN^
            |   SR_ASSIGN^
            |   BSR_ASSIGN^
            |   SL_ASSIGN^
            |   BAND_ASSIGN^
            |   BXOR_ASSIGN^
            |   BOR_ASSIGN^
            |   STAR_STAR_ASSIGN^
            )
            nls!
            expressionStatementNoCheck
            // If left-context of {x = y} is a statement boundary,
            // define the left-context of y as an initializer.
        )?
    ;

// conditional test (level 14)
conditionalExpression[int lc_stmt]
    :   logicalOrExpression[lc_stmt]
        (
          (nls! ELVIS_OPERATOR)=> nls! ELVIS_OPERATOR^ nls! conditionalExpression[0]
          | (nls! QUESTION)=> nls! QUESTION^ nls! assignmentExpression[0] nls! COLON! nls! conditionalExpression[0]
        )?
    ;

// logical or (||)  (level 13)
logicalOrExpression[int lc_stmt]
    :   logicalAndExpression[lc_stmt] (LOR^ nls! logicalAndExpression[0])*
    ;

// logical and (&&)  (level 12)
logicalAndExpression[int lc_stmt]
    :   inclusiveOrExpression[lc_stmt] (LAND^ nls! inclusiveOrExpression[0])*
    ;

// bitwise or non-short-circuiting or (|)  (level 11)
inclusiveOrExpression[int lc_stmt]
    :   exclusiveOrExpression[lc_stmt] (BOR^ nls! exclusiveOrExpression[0])*
    ;

// exclusive or (^)  (level 10)
exclusiveOrExpression[int lc_stmt]
    :   andExpression[lc_stmt] (BXOR^ nls! andExpression[0])*
    ;

// bitwise or non-short-circuiting and (&)  (level 9)
andExpression[int lc_stmt]
    :   regexExpression[lc_stmt] (BAND^ nls! regexExpression[0])*
    ;

// regex find and match (=~ and ==~) (level 8.5)
// jez: moved =~ closer to precedence of == etc, as...
// 'if (foo =~ "a.c")' is very close in intent to 'if (foo == "abc")'
regexExpression[int lc_stmt]
    :   equalityExpression[lc_stmt] ((REGEX_FIND^ | REGEX_MATCH^) nls! equalityExpression[0])*
    ;

// equality/inequality (==/!=) (level 8)
equalityExpression[int lc_stmt]
    :   relationalExpression[lc_stmt] ((NOT_EQUAL^ | EQUAL^ |IDENTICAL^ |NOT_IDENTICAL^ | COMPARE_TO^) nls! relationalExpression[0])*
    ;

// boolean relational expressions (level 7)
relationalExpression[int lc_stmt]
    :   shiftExpression[lc_stmt]
        (   options {greedy=true;} : (
                (   LT^
                |   GT^
                |   LE^
                |   GE^
                |   "in"^
                )
                nls!
                shiftExpression[0]

            )
            |   "instanceof"^ nls! typeSpec[true]
            |   "as"^         nls! typeSpec[true] //TODO: Rework to allow type expression?
        )?
    ;

// bit shift expressions (level 6)
shiftExpression[int lc_stmt]
    :   additiveExpression[lc_stmt]
        (
            ((SL^ | SR^ | BSR^)
            |   RANGE_INCLUSIVE^
            |   RANGE_EXCLUSIVE^
            )
            nls!
            additiveExpression[0]
        )*
    ;

// binary addition/subtraction (level 5)
additiveExpression[int lc_stmt]
    :   multiplicativeExpression[lc_stmt]
        (
            options {greedy=true;} :
            // Be greedy here, to favor {x+y} instead of {print +value}
            (PLUS^ | MINUS^) nls!
            multiplicativeExpression[0]
        )*
    ;

// multiplication/division/modulo (level 4)
multiplicativeExpression[int lc_stmt]
    :    ( INC^ nls!  powerExpressionNotPlusMinus[0] ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression[0])* )
    |    ( DEC^ nls!  powerExpressionNotPlusMinus[0] ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression[0])* )
    |    ( MINUS^ {#MINUS.setType(UNARY_MINUS);} nls!   powerExpressionNotPlusMinus[0] ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression[0])* )
    |    ( PLUS^ {#PLUS.setType(UNARY_PLUS);} nls!   powerExpressionNotPlusMinus[0] ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression[0])* )
    |    (  powerExpressionNotPlusMinus[lc_stmt] ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression[0])* )
    ;

// ++(prefix)/--(prefix)/+(unary)/-(unary) (level 3)
unaryExpression[int lc_stmt]
    :   INC^ nls! unaryExpression[0]
    |   DEC^ nls! unaryExpression[0]
    |   MINUS^   {#MINUS.setType(UNARY_MINUS);}   nls! unaryExpression[0]
    |   PLUS^    {#PLUS.setType(UNARY_PLUS);}     nls! unaryExpression[0]
    |   unaryExpressionNotPlusMinus[lc_stmt]
    ;

// math power operator (**) (level 2)
powerExpression[int lc_stmt]
    :   unaryExpression[lc_stmt] (STAR_STAR^ nls! unaryExpression[0])*
    ;

// math power operator (**) (level 2)
// (without ++(prefix)/--(prefix)/+(unary)/-(unary))
// The different rules are needed to avoid ambiguous selection
// of alternatives.
powerExpressionNotPlusMinus[int lc_stmt]
    :   unaryExpressionNotPlusMinus[lc_stmt] (STAR_STAR^ nls! unaryExpression[0])*
    ;

// ~(BNOT)/!(LNOT)/(type casting) (level 1)
unaryExpressionNotPlusMinus[int lc_stmt]
    :   BNOT^ nls! unaryExpression[0]
    |   LNOT^ nls! unaryExpression[0]
    |   (   // subrule allows option to shut off warnings
            options {
                    // "(int" ambig with postfixExpr due to lack of sequence
                    // info in linear approximate LL(k). It's ok. Shut up.
                    generateAmbigWarnings=false;
            }
        :   // If typecast is built in type, must be numeric operand
            // Have to backtrack to see if operator follows
            // FIXME: DECIDE: This syntax is wormy.  Can we deprecate or remove?
            (LPAREN builtInTypeSpec[true] RPAREN unaryExpression[0])=>
            lpb:LPAREN^ {#lpb.setType(TYPECAST);} builtInTypeSpec[true] RPAREN!
            unaryExpression[0]

            // Have to backtrack to see if operator follows. If no operator
            // follows, it's a typecast. No semantic checking needed to parse.
            // if it _looks_ like a cast, it _is_ a cast; else it's a "(expr)"
            // FIXME: DECIDE: This syntax is wormy.  Can we deprecate or remove?
            // TODO:  Rework this mess for Groovy.
        |   (LPAREN classTypeSpec[true] RPAREN unaryExpressionNotPlusMinus[0])=>
            lp:LPAREN^ {#lp.setType(TYPECAST);} classTypeSpec[true] RPAREN!
            unaryExpressionNotPlusMinus[0]

        |   postfixExpression[lc_stmt]
        )
    ;

// qualified names, array expressions, method invocation, post inc/dec (level 1)
postfixExpression[int lc_stmt]
    :
        pathExpression[lc_stmt]
        (
            options {greedy=true;} :
            // possibly add on a post-increment or post-decrement.
            // allows INC/DEC on too much, but semantics can check
            in:INC^ {#in.setType(POST_INC);}
        |   de:DEC^ {#de.setType(POST_DEC);}
        )?
    ;

// the basic element of an expression
primaryExpression {Token first = LT(1);}
    :   IDENT
    |   constant
    |   newExpression
    |   "this"
    |   "super"
    |   pe:parenthesizedExpression!             // (general stuff...)
        {#primaryExpression = #(create(EXPR,"EXPR",first,LT(1)),pe);}
    |   closableBlockConstructorExpression
    |   listOrMapConstructorExpression
    |   stringConstructorExpression         // "foo $bar baz"; presented as multiple tokens
    |   builtInType
    ;

// Note:  This is guaranteed to be an EXPR AST.
// That is, parentheses are preserved, in case the walker cares about them.
// They are significant sometimes, as in (f(x)){y} vs. f(x){y}.
parenthesizedExpression
{   Token first = LT(1);
    Token declaration = null;
    boolean hasClosureList=false;
    boolean firstContainsDeclaration=false;
    boolean sce=false;
}
    :   LPAREN!
           { declaration=LT(1); }
           firstContainsDeclaration = strictContextExpression[true]
           (SEMI!
             {hasClosureList=true;}
             (sce=strictContextExpression[true] | { astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT")); })
           )*
           // if the first expression contained a declaration,
           // but we are having only one expression at all, then
           // the first declaration is of the kind (def a=b)
           // which is invalid. Therefore if there was no closure
           // list we let the compiler throw an error if the
           // the first declaration exists
           {
            if (firstContainsDeclaration && !hasClosureList)
               throw new NoViableAltException(declaration, getFilename());
           }
        RPAREN!
        {
            if (hasClosureList) {
                #parenthesizedExpression = #(create(CLOSURE_LIST,"CLOSURE_LIST",first,LT(1)), #parenthesizedExpression);
            }
        }
    ;

/** Things that can show up as expressions, but only in strict
 *  contexts like inside parentheses, argument lists, and list constructors.
 */
strictContextExpression[boolean allowDeclaration]
returns [boolean hasDeclaration=false]
{Token first = LT(1);}
    :
        (   ({allowDeclaration}? declarationStart) =>
            {hasDeclaration=true;} singleDeclaration  // used for both binding and value, as: while (String xx = nextln()) { println xx }
        |   expression[0]
        |   branchStatement // useful to embed inside expressions (cf. C++ throw)
        |   annotation      // creates an annotation value
        )
        // For the sake of the AST walker, mark nodes like this very clearly.
        {#strictContextExpression = #(create(EXPR,"EXPR",first,LT(1)), #strictContextExpression);}
    ;

assignmentLessExpression  {Token first = LT(1);}
    :
        (   conditionalExpression[0]
        )
        // For the sake of the AST walker, mark nodes like this very clearly.
        {#assignmentLessExpression = #(create(EXPR,"EXPR",first,LT(1)), #assignmentLessExpression);}
    ;

closableBlockConstructorExpression
    :   closableBlock
    ;

// Groovy syntax for "$x $y" or /$x $y/.
stringConstructorExpression  {Token first = LT(1);}
    :   cs:STRING_CTOR_START
        { #cs.setType(STRING_LITERAL); }

        stringConstructorValuePart

        (   cm:STRING_CTOR_MIDDLE
            { #cm.setType(STRING_LITERAL); }
            stringConstructorValuePart
        )*

        ce:STRING_CTOR_END
        { #ce.setType(STRING_LITERAL);
          #stringConstructorExpression = #(create(STRING_CONSTRUCTOR,"STRING_CONSTRUCTOR",first,LT(1)), stringConstructorExpression);
        }
    ;

stringConstructorValuePart
    :
    (   identifier
    |   "this" |   "super"
    |   openOrClosableBlock
    )
    ;

/**
 * A list constructor is a argument list enclosed in square brackets, without labels.
 * Any argument can be decorated with a spread operator (*x), but not a label (a:x).
 * Examples:  [], [1], [1,2], [1,*l1,2], [*l1,*l2].
 * (The l1, l2 must be a sequence or null.)
 * <p>
 * A map constructor is an argument list enclosed in square brackets, with labels everywhere,
 * except on spread arguments, which stand for whole maps spliced in.
 * A colon alone between the brackets also forces the expression to be an empty map constructor.
 * Examples: [:], [a:1], [a:1,b:2], [a:1,*:m1,b:2], [*:m1,*:m2]
 * (The m1, m2 must be a map or null.)
 * Values associated with identical keys overwrite from left to right:
 * [a:1,a:2]  ===  [a:2]
 * <p>
 * Some malformed constructor expressions are not detected in the parser, but in a post-pass.
 * Bad examples: [1,b:2], [a:1,2], [:1].
 * (Note that method call arguments, by contrast, can be a mix of keyworded and non-keyworded arguments.)
 */
// The parser allows a mix of labeled and unlabeled arguments, but there must be a semantic check that
// the arguments are all labeled (or SPREAD_MAP_ARG) or all unlabeled (and not SPREAD_MAP_ARG).
listOrMapConstructorExpression
        { boolean hasLabels = false; }
    :   lcon:LBRACK!
        args:argList                 { hasLabels |= argListHasLabels;  }  // any argument label implies a map
        RBRACK!
        {   int type = hasLabels ? MAP_CONSTRUCTOR : LIST_CONSTRUCTOR;
            #listOrMapConstructorExpression = #(create(type,"[",lcon,LT(1)), args);
        }
    |
        /* Special case:  [:] is an empty map constructor. */
        emcon:LBRACK^ COLON! RBRACK!   {#emcon.setType(MAP_CONSTRUCTOR);}
    ;

/** object instantiation.
 *  Trees are built as illustrated by the following input/tree pairs:
 *
 *  new T()
 *
 *  new
 *   |
 *   T --  ELIST
 *           |
 *          arg1 -- arg2 -- .. -- argn
 *
 *  new int[]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *
 *  new int[] {1,2}
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR -- ARRAY_INIT
 *                                  |
 *                                EXPR -- EXPR
 *                                  |       |
 *                                  1       2
 *
 *  new int[3]
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *               |
 *             EXPR
 *               |
 *               3
 *
 *  new int[1][2]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *               |
 *         ARRAY_DECLARATOR -- EXPR
 *               |               |
 *             EXPR              1
 *               |
 *               2
 *
 */
newExpression {Token first = LT(1);}
    :   "new"! nls! (ta:typeArguments!)? t:type!
        (   nls!
            mca:methodCallArgs[null]!

            (
                options { greedy=true; }:
                cb:classBlock
            )?

            {#mca = #mca.getFirstChild();
            #newExpression = #(create(LITERAL_new,"new",first,LT(1)), #ta, #t, #mca, #cb);}

            //java 1.1
            // Note: This will allow bad constructs like
            //      new int[4][][3] {exp,exp}.
            //      There needs to be a semantic check here...
            // to make sure:
            //   a) [ expr ] and [ ] are not mixed
            //   b) [ expr ] and an init are not used together
        |   ad:newArrayDeclarator! //(arrayInitializer)?
            // Groovy does not support Java syntax for initialized new arrays.
            // Use sequence constructors instead.
            {#newExpression = #(create(LITERAL_new,"new",first,LT(1)), #ta, #t, #ad);}
        )
    ;

argList
    {
        Token first = LT(1);
        Token lastComma = null;
        int hls=0, hls2=0;
        boolean hasClosureList=false;
        boolean trailingComma=false;
        boolean sce=false;
    }
    :
        // Note:  nls not needed, since we are inside parens,
        // and those insignificant newlines are suppressed by the lexer.
        (hls=argument
        ((
            (
                SEMI! {hasClosureList=true;}
                (
                    sce=strictContextExpression[true]
                    | { astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT")); }
                )
            )+
            {#argList = #(create(CLOSURE_LIST,"CLOSURE_LIST",first,LT(1)), #argList);}
        ) | (
                (   {lastComma = LT(1);}
                    COMMA!
                    (
                        (hls2=argument {hls |= hls2;})
                        |
                        (
                         {  if (trailingComma) throw new NoViableAltException(lastComma, getFilename());
                            trailingComma=true;
                         }
                        )
                    )

                )*
                {#argList = #(create(ELIST,"ELIST",first,LT(1)), argList);}
            )
        ) | (
            {#argList = create(ELIST,"ELIST",first,LT(1));}
        )
        )
        {argListHasLabels = (hls&1)!=0; }
    ;

/** A single argument in (...) or [...].  Corresponds to to a method or closure parameter.
 *  May be labeled.  May be modified by the spread operator '*' ('*:' for keywords).
 */
argument
returns [byte hasLabelOrSpread = 0]
{boolean sce=false;}
    :
        // Optional argument label.
        // Usage:  Specifies a map key, or a keyworded argument.
        (   (argumentLabelStart) =>
            argumentLabel c:COLON^          {#c.setType(LABELED_ARG);}

            {   hasLabelOrSpread |= 1;  }  // signal to caller the presence of a label

        |   // Spread operator:  f(*[a,b,c])  ===  f(a,b,c);  f(1,*null,2)  ===  f(1,2).
            sp:STAR^                        {#sp.setType(SPREAD_ARG);}
            {   hasLabelOrSpread |= 2;  }  // signal to caller the presence of a spread operator
            // spread maps are marked, as f(*:m) for f(a:x, b:y) if m==[a:x, b:y]
            (
                COLON!                      {#sp.setType(SPREAD_MAP_ARG);}
                { hasLabelOrSpread |= 1; }  // signal to caller the presence of a label
            )?
        )?

        sce=strictContextExpression[true]
        {
            require(LA(1) != COLON,
                "illegal colon after argument expression",
                "a complex label expression before a colon must be parenthesized");
        }
    ;

/** A label for an argument is of the form a:b, 'a':b, "a":b, (a):b, etc..
 *      The labels in (a:b), ('a':b), and ("a":b) are in all ways equivalent,
 *      except that the quotes allow more spellings.
 *  Equivalent dynamically computed labels are (('a'):b) and ("${'a'}":b)
 *  but not ((a):b) or "$a":b, since the latter cases evaluate (a) as a normal identifier.
 *      Bottom line:  If you want a truly variable label, use parens and say ((a):b).
 */
argumentLabel
    :   (IDENT) =>
        id:IDENT                  {#id.setType(STRING_LITERAL);}  // identifiers are self-quoting in this context
    |   (keywordPropertyNames) =>
        kw:keywordPropertyNames   {#kw.setType(STRING_LITERAL);}  // identifiers are self-quoting in this context
    |   primaryExpression                                         // dynamic expression
    ;

/** For lookahead only.  Fast approximate parse of an argumentLabel followed by a colon. */
argumentLabelStart!
        // allow number and string literals as labels for maps
    :   (
            IDENT | keywordPropertyNames
        |   constantNumber | STRING_LITERAL
        |   (LPAREN | STRING_CTOR_START)=> balancedBrackets
        )
        COLON
    ;

newArrayDeclarator
    :   (
            // CONFLICT:
            // newExpression is a primaryExpression which can be
            // followed by an array index reference. This is ok,
            // as the generated code will stay in this loop as
            // long as it sees an LBRACK (proper behavior)
            options {
                warnWhenFollowAmbig = false;
            }
        :
            lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);}
                (expression[0])?
            RBRACK!
        )+
    ;

/** Numeric, string, regexp, boolean, or null constant. */
constant
    :   constantNumber
    |   STRING_LITERAL
    |   "true"
    |   "false"
    |   "null"
    ;

/** Numeric constant. */
constantNumber
    :   NUM_INT
    |   NUM_FLOAT
    |   NUM_LONG
    |   NUM_DOUBLE
    |   NUM_BIG_INT
    |   NUM_BIG_DECIMAL
    ;

/** Fast lookahead across balanced brackets of all sorts. */
balancedBrackets!
    :   LPAREN balancedTokens RPAREN
    |   LBRACK balancedTokens RBRACK
    |   LCURLY balancedTokens RCURLY
    |   STRING_CTOR_START balancedTokens STRING_CTOR_END
    ;

balancedTokens!
    :   (   balancedBrackets
        |   ~(LPAREN|LBRACK|LCURLY | STRING_CTOR_START
             |RPAREN|RBRACK|RCURLY | STRING_CTOR_END)
        )*
    ;

/** A statement separator is either a semicolon or a significant newline.
 *  Any number of additional (insignificant) newlines may accompany it.
 */
//  (All the '!' signs simply suppress the default AST building.)
//  Returns the type of the separator in this.sepToken, in case it matters.
sep!
    :   SEMI!
        (options { greedy=true; }: NLS!)*
        { sepToken = SEMI; }
    |   NLS!                // this newline is significant!
        { sepToken = NLS; }
        (
            options { greedy=true; }:
            SEMI!           // this superfluous semicolon is gobbled
            (options { greedy=true; }: NLS!)*
            { sepToken = SEMI; }
        )*
    ;

/** Zero or more insignificant newlines, all gobbled up and thrown away. */
nls!
    :
        (options { greedy=true; }: NLS!)?
        // Note:  Use '?' rather than '*', relying on the fact that the lexer collapses
        // adjacent NLS tokens, always.  This lets the parser use its LL(3) lookahead
        // to "see through" sequences of newlines.  If there were a '*' here, the lookahead
        // would be weaker, since the parser would have to be prepared for long sequences
        // of NLS tokens.
    ;

/** Zero or more insignificant newlines, all gobbled up and thrown away,
 *  but a warning message is left for the user, if there was a newline.
 */
nlsWarn!
    :
        (   (NLS)=>
            { addWarning(
              "A newline at this point does not follow the Groovy Coding Conventions.",
              "Keep this statement on one line, or use curly braces to break across multiple lines."
            ); }
        )?
        nls!
    ;

//----------------------------------------------------------------------------
// The Groovy scanner
//----------------------------------------------------------------------------
class GroovyLexer extends Lexer;

options {
    exportVocab=Groovy;             // call the vocabulary "Groovy"
    testLiterals=false;             // don't automatically test for literals
    k=4;                                    // four characters of lookahead
    charVocabulary='\u0000'..'\uFFFF';
    // without inlining some bitset tests, couldn't do unicode;
    // I need to make ANTLR generate smaller bitsets; see
    // bottom of GroovyLexer.java
    codeGenBitsetTestThreshold=20;
}

{
    /** flag for enabling the "assert" keyword */
    private boolean assertEnabled = true;
    /** flag for enabling the "enum" keyword */
    private boolean enumEnabled = true;
    /** flag for including whitespace tokens (for IDE preparsing) */
    private boolean whitespaceIncluded = false;

    /** Enable the "assert" keyword */
    public void enableAssert(boolean shouldEnable) { assertEnabled = shouldEnable; }
    /** Query the "assert" keyword state */
    public boolean isAssertEnabled() { return assertEnabled; }
    /** Enable the "enum" keyword */
    public void enableEnum(boolean shouldEnable) { enumEnabled = shouldEnable; }
    /** Query the "enum" keyword state */
    public boolean isEnumEnabled() { return enumEnabled; }

    /** Include whitespace tokens.  Note that this breaks the parser.   */
    public void setWhitespaceIncluded(boolean z) { whitespaceIncluded = z; }
    /** Are whitespace tokens included? */
    public boolean isWhitespaceIncluded() { return whitespaceIncluded; }

    {
        // Initialization actions performed on construction.
        setTabSize(1);  // get rid of special tab interpretation, for IDEs and general clarity
    }

    /** Bumped when inside '[x]' or '(x)', reset inside '{x}'.  See ONE_NL.  */
    protected int parenLevel = 0;
    protected int suppressNewline = 0;  // be really mean to newlines inside strings
    protected static final int SCS_TYPE = 3, SCS_VAL = 4, SCS_LIT = 8, SCS_LIMIT = 16;
    protected static final int SCS_SQ_TYPE = 0, SCS_TQ_TYPE = 1, SCS_RE_TYPE = 2, SCS_DRE_TYPE = 3;
    protected int stringCtorState = 0;  // hack string and regexp constructor boundaries
    /** Push parenLevel here and reset whenever inside '{x}'. */
    protected ArrayList parenLevelStack = new ArrayList();
    protected int lastSigTokenType = EOF;  // last returned non-whitespace token

    public void setTokenObjectClass(String name) {/*ignore*/}

    protected Token makeToken(int t) {
        GroovySourceToken tok = new GroovySourceToken(t);
        tok.setColumn(inputState.getTokenStartColumn());
        tok.setLine(inputState.getTokenStartLine());
        tok.setColumnLast(inputState.getColumn());
        tok.setLineLast(inputState.getLine());
        return tok;
    }

    protected void pushParenLevel() {
        parenLevelStack.add(Integer.valueOf(parenLevel*SCS_LIMIT + stringCtorState));
        parenLevel = 0;
        stringCtorState = 0;
    }

    protected void popParenLevel() {
        int npl = parenLevelStack.size();
        if (npl == 0)  return;
        int i = ((Integer) parenLevelStack.remove(--npl)).intValue();
        parenLevel      = i / SCS_LIMIT;
        stringCtorState = i % SCS_LIMIT;
    }

    protected void restartStringCtor(boolean expectLiteral) {
        if (stringCtorState != 0) {
            stringCtorState = (expectLiteral? SCS_LIT: SCS_VAL) + (stringCtorState & SCS_TYPE);
        }
    }

    protected boolean allowRegexpLiteral() {
        return !isExpressionEndingToken(lastSigTokenType);
    }

    /** Return true for an operator or punctuation which can end an expression.
     *  Return true for keywords, identifiers, and literals.
     *  Return true for tokens which can end expressions (right brackets, ++, --).
     *  Return false for EOF and all other operator and punctuation tokens.
     *  Used to suppress the recognition of /foo/ as opposed to the simple division operator '/'.
     */
    // Cf. 'constant' and 'balancedBrackets' rules in the grammar.)
    protected static boolean isExpressionEndingToken(int ttype) {
        switch (ttype) {
        case INC:               // x++ / y
        case DEC:               // x-- / y
        case RPAREN:            // (x) / y
        case RBRACK:            // f[x] / y
        case RCURLY:            // f{x} / y
        case STRING_LITERAL:    // "x" / y
        case STRING_CTOR_END:   // "$x" / y
        case NUM_INT:           // 0 / y
        case NUM_FLOAT:         // 0f / y
        case NUM_LONG:          // 0l / y
        case NUM_DOUBLE:        // 0.0 / y
        case NUM_BIG_INT:       // 0g / y
        case NUM_BIG_DECIMAL:   // 0.0g / y
        case IDENT:             // x / y
        // and a bunch of keywords (all of them; no sense picking and choosing):
        case LITERAL_as:
        case LITERAL_assert:
        case LITERAL_boolean:
        case LITERAL_break:
        case LITERAL_byte:
        case LITERAL_case:
        case LITERAL_catch:
        case LITERAL_char:
        case LITERAL_class:
        case LITERAL_continue:
        case LITERAL_def:
        case LITERAL_default:
        case LITERAL_double:
        case LITERAL_else:
        case LITERAL_enum:
        case LITERAL_extends:
        case LITERAL_false:
        case LITERAL_finally:
        case LITERAL_float:
        case LITERAL_for:
        case LITERAL_if:
        case LITERAL_implements:
        case LITERAL_import:
        case LITERAL_in:
        case LITERAL_instanceof:
        case LITERAL_int:
        case LITERAL_interface:
        case LITERAL_long:
        case LITERAL_native:
        case LITERAL_new:
        case LITERAL_null:
        case LITERAL_package:
        case LITERAL_private:
        case LITERAL_protected:
        case LITERAL_public:
        case LITERAL_return:
        case LITERAL_short:
        case LITERAL_static:
        case LITERAL_super:
        case LITERAL_switch:
        case LITERAL_synchronized:
        case LITERAL_trait:
        case LITERAL_this:
        case LITERAL_threadsafe:
        case LITERAL_throw:
        case LITERAL_throws:
        case LITERAL_transient:
        case LITERAL_true:
        case LITERAL_try:
        case LITERAL_void:
        case LITERAL_volatile:
        case LITERAL_while:
            return true;
        default:
            return false;
        }
    }

    protected void newlineCheck(boolean check) throws RecognitionException {
        if (check && suppressNewline > 0) {
            require(suppressNewline == 0,
                "end of line reached within a simple string 'x' or \"x\" or /x/",
                "for multi-line literals, use triple quotes '''x''' or \"\"\"x\"\"\" or /x/ or $/x/$");
            suppressNewline = 0;  // shut down any flood of errors
        }
        newline();
    }

    protected boolean atValidDollarEscape() throws CharStreamException {
        // '$' (('{' | LETTER) =>
        int k = 1;
        char lc = LA(k++);
        if (lc != '$')  return false;
        lc = LA(k++);
        return (lc == '{' || (lc != '$' && Character.isJavaIdentifierStart(lc)));
    }

    protected boolean atDollarDollarEscape() throws CharStreamException {
        return LA(1) == '$' && LA(2) == '$';
    }

    protected boolean atMultiCommentStart() throws CharStreamException {
        return LA(1) == '/' && LA(2) == '*';
    }

    protected boolean atDollarSlashEscape() throws CharStreamException {
        return LA(1) == '$' && LA(2) == '/';
    }

    /** This is a bit of plumbing which resumes collection of string constructor bodies,
     *  after an embedded expression has been parsed.
     *  Usage:  new GroovyRecognizer(new GroovyLexer(in).plumb()).
     */
    public TokenStream plumb() {
        return new TokenStream() {
            public Token nextToken() throws TokenStreamException {
                if (stringCtorState >= SCS_LIT) {
                    // This goo is modeled upon the ANTLR code for nextToken:
                    int quoteType = (stringCtorState & SCS_TYPE);
                    stringCtorState = 0;  // get out of this mode, now
                    resetText();
                    try {
                        switch (quoteType) {
                        case SCS_SQ_TYPE:
                            mSTRING_CTOR_END(true, /*fromStart:*/false, false); break;
                        case SCS_TQ_TYPE:
                            mSTRING_CTOR_END(true, /*fromStart:*/false, true); break;
                        case SCS_RE_TYPE:
                            mREGEXP_CTOR_END(true, /*fromStart:*/false); break;
                        case SCS_DRE_TYPE:
                            mDOLLAR_REGEXP_CTOR_END(true, /*fromStart:*/false); break;
                        default:  throw new AssertionError(false);
                        }
                        lastSigTokenType = _returnToken.getType();
                        return _returnToken;
                    } catch (RecognitionException e) {
                        throw new TokenStreamRecognitionException(e);
                    } catch (CharStreamException cse) {
                        if ( cse instanceof CharStreamIOException ) {
                            throw new TokenStreamIOException(((CharStreamIOException)cse).io);
                        }
                        else {
                            throw new TokenStreamException(cse.getMessage());
                        }
                    }
                }
                Token token = GroovyLexer.this.nextToken();
                int lasttype = token.getType();
                if (whitespaceIncluded) {
                    switch (lasttype) {  // filter out insignificant types
                    case WS:
                    case ONE_NL:
                    case SL_COMMENT:
                    case ML_COMMENT:
                        lasttype = lastSigTokenType;  // back up!
                    }
                }
                lastSigTokenType = lasttype;
                return token;
            }
        };
    }

    // stuff to adjust ANTLR's tracing machinery
    public static boolean tracing = false;  // only effective if antlr.Tool is run with -traceLexer
    public void traceIn(String rname) throws CharStreamException {
        if (!GroovyLexer.tracing)  return;
        super.traceIn(rname);
    }
    public void traceOut(String rname) throws CharStreamException {
        if (!GroovyLexer.tracing)  return;
        if (_returnToken != null)  rname += tokenStringOf(_returnToken);
        super.traceOut(rname);
    }
    private static java.util.HashMap ttypes;
    private static String tokenStringOf(Token t) {
        if (ttypes == null) {
            java.util.HashMap map = new java.util.HashMap();
            java.lang.reflect.Field[] fields = GroovyTokenTypes.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getType() != int.class)  continue;
                try {
                    map.put(fields[i].get(null), fields[i].getName());
                } catch (IllegalAccessException ee) {
                }
            }
            ttypes = map;
        }
        Integer tt = Integer.valueOf(t.getType());
        Object ttn = ttypes.get(tt);
        if (ttn == null)  ttn = "<"+tt+">";
        return "["+ttn+",\""+t.getText()+"\"]";
    }

    protected GroovyRecognizer parser;  // little-used link; TODO: get rid of
    private void require(boolean z, String problem, String solution) throws SemanticException {
        // TODO: Direct to a common error handler, rather than through the parser.
        if (!z && parser!=null)  parser.requireFailed(problem, solution);
        if (!z) {
            int lineNum = inputState.getLine(), colNum = inputState.getColumn();
            throw new SemanticException(problem + ";\n   solution: " + solution, getFilename(), lineNum, colNum);
        }
    }
}

// OPERATORS
QUESTION          options {paraphrase="'?'";}           :   '?'             ;
LPAREN            options {paraphrase="'('";}           :   '('             {++parenLevel;};
RPAREN            options {paraphrase="')'";}           :   ')'             {--parenLevel;};
LBRACK            options {paraphrase="'['";}           :   '['             {++parenLevel;};
RBRACK            options {paraphrase="']'";}           :   ']'             {--parenLevel;};
LCURLY            options {paraphrase="'{'";}           :   '{'             {pushParenLevel();};
RCURLY            options {paraphrase="'}'";}           :   '}'             {popParenLevel(); if(stringCtorState!=0) restartStringCtor(true);};
COLON             options {paraphrase="':'";}           :   ':'             ;
COMMA             options {paraphrase="','";}           :   ','             ;
DOT               options {paraphrase="'.'";}           :   '.'             ;
ASSIGN            options {paraphrase="'='";}           :   '='             ;
COMPARE_TO        options {paraphrase="'<=>'";}         :   "<=>"           ;
EQUAL             options {paraphrase="'=='";}          :   "=="            ;
IDENTICAL         options {paraphrase="'==='";}         :   "==="           ;
LNOT              options {paraphrase="'!'";}           :   '!'             ;
BNOT              options {paraphrase="'~'";}           :   '~'             ;
NOT_EQUAL         options {paraphrase="'!='";}          :   "!="            ;
NOT_IDENTICAL     options {paraphrase="'!=='";}         :   "!=="           ;
protected  //switched from combined rule
DIV               options {paraphrase="'/'";}           :   '/'             ;
protected  //switched from combined rule
DIV_ASSIGN        options {paraphrase="'/='";}          :   "/="            ;
PLUS              options {paraphrase="'+'";}           :   '+'             ;
PLUS_ASSIGN       options {paraphrase="'+='";}          :   "+="            ;
INC               options {paraphrase="'++'";}          :   "++"            ;
MINUS             options {paraphrase="'-'";}           :   '-'             ;
MINUS_ASSIGN      options {paraphrase="'-='";}          :   "-="            ;
DEC               options {paraphrase="'--'";}          :   "--"            ;
STAR              options {paraphrase="'*'";}           :   '*'             ;
STAR_ASSIGN       options {paraphrase="'*='";}          :   "*="            ;
MOD               options {paraphrase="'%'";}           :   '%'             ;
MOD_ASSIGN        options {paraphrase="'%='";}          :   "%="            ;
SR                options {paraphrase="'>>'";}          :   ">>"            ;
SR_ASSIGN         options {paraphrase="'>>='";}         :   ">>="           ;
BSR               options {paraphrase="'>>>'";}         :   ">>>"           ;
BSR_ASSIGN        options {paraphrase="'>>>='";}        :   ">>>="          ;
GE                options {paraphrase="'>='";}          :   ">="            ;
GT                options {paraphrase="'>'";}           :   ">"             ;
SL                options {paraphrase="'<<'";}          :   "<<"            ;
SL_ASSIGN         options {paraphrase="'<<='";}         :   "<<="           ;
LE                options {paraphrase="'<='";}          :   "<="            ;
LT                options {paraphrase="'<'";}           :   '<'             ;
BXOR              options {paraphrase="'^'";}           :   '^'             ;
BXOR_ASSIGN       options {paraphrase="'^='";}          :   "^="            ;
BOR               options {paraphrase="'|'";}           :   '|'             ;
BOR_ASSIGN        options {paraphrase="'|='";}          :   "|="            ;
LOR               options {paraphrase="'||'";}          :   "||"            ;
BAND              options {paraphrase="'&'";}           :   '&'             ;
BAND_ASSIGN       options {paraphrase="'&='";}          :   "&="            ;
LAND              options {paraphrase="'&&'";}          :   "&&"            ;
SEMI              options {paraphrase="';'";}           :   ';'             ;
protected
DOLLAR            options {paraphrase="'$'";}           :   '$'             ;
RANGE_INCLUSIVE   options {paraphrase="'..'";}          :   ".."            ;
RANGE_EXCLUSIVE   options {paraphrase="'..<'";}         :   "..<"           ;
TRIPLE_DOT        options {paraphrase="'...'";}         :   "..."           ;
SPREAD_DOT        options {paraphrase="'*.'";}          :   "*."            ;
OPTIONAL_DOT      options {paraphrase="'?.'";}          :   "?."            ;
ELVIS_OPERATOR    options {paraphrase="'?:'";}          :   "?:"            ;
MEMBER_POINTER    options {paraphrase="'.&'";}          :   ".&"            ;
REGEX_FIND        options {paraphrase="'=~'";}          :   "=~"            ;
REGEX_MATCH       options {paraphrase="'==~'";}         :   "==~"           ;
STAR_STAR         options {paraphrase="'**'";}          :   "**"            ;
STAR_STAR_ASSIGN  options {paraphrase="'**='";}         :   "**="           ;
CLOSABLE_BLOCK_OP options {paraphrase="'->'";}          :   "->"            ;

// Whitespace -- ignored
WS
options {
    paraphrase="whitespace";
}
    :
        (
            options { greedy=true; }:
            ' '
        |   '\t'
        |   '\f'
        |   '\\' ONE_NL[false]
        )+
        { if (!whitespaceIncluded)  _ttype = Token.SKIP; }
    ;

protected
ONE_NL![boolean check]
options {
    paraphrase="a newline";
}
 :   // handle newlines, which are significant in Groovy
        (   options {generateAmbigWarnings=false;}
        :   "\r\n"  // Evil DOS
        |   '\r'    // Macintosh
        |   '\n'    // Unix (the right way)
        )
        {
            // update current line number for error reporting
            newlineCheck(check);
        }
    ;

// Group any number of newlines (with comments and whitespace) into a single token.
// This reduces the amount of parser lookahead required to parse around newlines.
// It is an invariant that the parser never sees NLS tokens back-to-back.
NLS
options {
    paraphrase="some newlines, whitespace or comments";
}
    :   ONE_NL[true]
        (   {!whitespaceIncluded}?
            (ONE_NL[true] | WS | SL_COMMENT | ML_COMMENT)+
            // (gobble, gobble)*
        )?
        // Inside (...) and [...] but not {...}, ignore newlines.
        {   if (whitespaceIncluded) {
                // keep the token as-is
            } else if (parenLevel != 0) {
                // when directly inside parens, all newlines are ignored here
                $setType(Token.SKIP);
            } else {
                // inside {...}, newlines must be explicitly matched as 'nls!'
                $setText("<newline>");
            }
        }
    ;

// Single-line comments
SL_COMMENT
options {
    paraphrase="a single line comment";
}
    :   "//"
        (
            options {  greedy = true;  }:
            // '\uffff' means the EOF character.
            // This will fix the issue GROOVY-766 (infinite loop).
            ~('\n'|'\r'|'\uffff')
        )*
        {
            if (!whitespaceIncluded) $setType(Token.SKIP);
        }
        //This might be significant, so don't swallow it inside the comment:
        //ONE_NL
    ;

// Script-header comments
SH_COMMENT
options {
    paraphrase="a script header";
}
    :   {getLine() == 1 && getColumn() == 1}?  "#!"
        (
            options {  greedy = true;  }:
            // '\uffff' means the EOF character.
            // This will fix the issue GROOVY-766 (infinite loop).
            ~('\n'|'\r'|'\uffff')
        )*
        ONE_NL[true]
        ("#!"
          (
              options {  greedy = true;  }:
              // '\uffff' means the EOF character.
              // This will fix the issue GROOVY-766 (infinite loop).
              ~('\n'|'\r'|'\uffff')
          )* ONE_NL[true]
        )*
        { if (!whitespaceIncluded)  $setType(Token.SKIP); }
    ;

// multiple-line comments
ML_COMMENT
options {
    paraphrase="a multi-line comment";
}
    :   { atMultiCommentStart() }? "/*"
        (   /*  '\r' '\n' can be matched in one alternative or by matching
                '\r' in one iteration and '\n' in another. I am trying to
                handle any flavor of newline that comes in, but the language
                that allows both "\r\n" and "\r" and "\n" to all be valid
                newline is ambiguous. Consequently, the resulting grammar
                must be ambiguous. I'm shutting this warning off.
             */
            options {
                    generateAmbigWarnings=false;
            }
        :
            ( '*' ~'/' ) => '*'
        |   ONE_NL[true]
        |   ~('*'|'\n'|'\r'|'\uffff')
        )*
        "*/"
        {
            if (!whitespaceIncluded) $setType(Token.SKIP);
        }
    ;

// string literals
STRING_LITERAL
options {
    paraphrase="a string literal";
}
        {int tt=0;}
    :   ("'''") =>  //...shut off ambiguity warning
        "'''"!
        (   STRING_CH | ESC | '"' | '$' | STRING_NL[true]
        |   ('\'' (~'\'' | '\'' ~'\'')) => '\''  // allow 1 or 2 close quotes
        )*
        "'''"!
    |   '\''!
                                {++suppressNewline;}
        (   STRING_CH | ESC | '"' | '$'  )*
                                {--suppressNewline;}
        '\''!
    |   ("\"\"\"") =>  //...shut off ambiguity warning
        "\"\"\""!
        tt=STRING_CTOR_END[true, /*tripleQuote:*/ true]
        {$setType(tt);}
    |   '"'!
                                {++suppressNewline;}
        tt=STRING_CTOR_END[true, /*tripleQuote:*/ false]
        {$setType(tt);}
    ;

protected
STRING_CTOR_END[boolean fromStart, boolean tripleQuote]
returns [int tt=STRING_CTOR_END]
options {
    paraphrase="a string literal end";
}
        { boolean dollarOK = false; }
    :
        (
            options {  greedy = true;  }:
            STRING_CH | ESC | '\'' | STRING_NL[tripleQuote]
        |   ('"' (~'"' | '"' ~'"'))=> {tripleQuote}? '"'  // allow 1 or 2 close quotes
        )*
        (   (   { !tripleQuote }? "\""!
            |   {  tripleQuote }? "\"\"\""!
            )
            {
                if (fromStart)      tt = STRING_LITERAL;  // plain string literal!
                if (!tripleQuote)   {--suppressNewline;}
                // done with string constructor!
                //assert(stringCtorState == 0);
            }
        |   {dollarOK = atValidDollarEscape();}
            '$'!
            {
                require(dollarOK,
                    "illegal string body character after dollar sign",
                    "either escape a literal dollar sign \"\\$5\" or bracket the value expression \"${5}\"");
                // Yes, it's a string constructor, and we've got a value part.
                tt = (fromStart ? STRING_CTOR_START : STRING_CTOR_MIDDLE);
                stringCtorState = SCS_VAL + (tripleQuote? SCS_TQ_TYPE: SCS_SQ_TYPE);
            }
        )
        {   $setType(tt);  }
    ;

protected
STRING_CH
options {
    paraphrase="a string character";
}
    :   ~('"'|'\''|'\\'|'$'|'\n'|'\r'|'\uffff')
    ;

REGEXP_LITERAL
options {
    paraphrase="a multiline regular expression literal";
}
        {int tt=0;}
    :   { !atMultiCommentStart() }?
        (   {allowRegexpLiteral()}?
            '/'!
            {++suppressNewline;}
            //Do this, but require it to be non-trivial:  REGEXP_CTOR_END[true]
            // There must be at least one symbol or $ escape, lest the regexp collapse to '//'.
            // (This should be simpler, but I don't know how to do it w/o ANTLR warnings vs. '//' comments.)
            (
                REGEXP_SYMBOL
                tt=REGEXP_CTOR_END[true]
            |   {!atValidDollarEscape()}? '$'
                tt=REGEXP_CTOR_END[true]
            |   '$'!
                {
                    // Yes, it's a regexp constructor, and we've got a value part.
                    tt = STRING_CTOR_START;
                    stringCtorState = SCS_VAL + SCS_RE_TYPE;
                }
            )
            {$setType(tt);}

        |   ( '/' ~'=' ) => DIV {$setType(DIV);}
        |   DIV_ASSIGN {$setType(DIV_ASSIGN);}
        )
    ;

DOLLAR_REGEXP_LITERAL
options {
    paraphrase="a multiline dollar escaping regular expression literal";
}
        {int tt=0;}
    :   {allowRegexpLiteral()}? "$/"!
        // Do this, but require it to be non-trivial:  DOLLAR_REGEXP_CTOR_END[true]
        // There must be at least one symbol or $ escape, otherwise the regexp collapses.
        (
            DOLLAR_REGEXP_SYMBOL
            tt=DOLLAR_REGEXP_CTOR_END[true]
        | {!atValidDollarEscape() && !atDollarSlashEscape() && !atDollarDollarEscape()}? '$'
            tt=DOLLAR_REGEXP_CTOR_END[true]
        |
            ('$' '/') => ESCAPED_SLASH
            tt=DOLLAR_REGEXP_CTOR_END[true]
        |
            ('$' '$') => ESCAPED_DOLLAR
            tt=DOLLAR_REGEXP_CTOR_END[true]
        |
            '$'!
            {
                // Yes, it's a regexp constructor, and we've got a value part.
                tt = STRING_CTOR_START;
                stringCtorState = SCS_VAL + SCS_DRE_TYPE;
            }
        )
        {$setType(tt);}
    ;

protected
REGEXP_CTOR_END[boolean fromStart]
returns [int tt=STRING_CTOR_END]
options {
    paraphrase="a multiline regular expression literal end";
}
    :
        (
            options {  greedy = true;  }:
            REGEXP_SYMBOL
        |
            {!atValidDollarEscape()}? '$'
        )*
        (   '/'!
            {
                if (fromStart)      tt = STRING_LITERAL;  // plain regexp literal!
                {--suppressNewline;}
                // done with regexp constructor!
                //assert(stringCtorState == 0);
            }
        |   '$'!
            {
                // Yes, it's a regexp constructor, and we've got a value part.
                tt = (fromStart ? STRING_CTOR_START : STRING_CTOR_MIDDLE);
                stringCtorState = SCS_VAL + SCS_RE_TYPE;
            }
        )
        {   $setType(tt);  }
    ;

protected
DOLLAR_REGEXP_CTOR_END[boolean fromStart]
returns [int tt=STRING_CTOR_END]
options {
    paraphrase="a multiline dollar escaping regular expression literal end";
}
    :
        (
            options {  greedy = true;  }:
            { !(LA(1) == '/' && LA(2) == '$') }? DOLLAR_REGEXP_SYMBOL
        |
            ('$' '/') => ESCAPED_SLASH
        |
            ('$' '$') => ESCAPED_DOLLAR
        |
            {!atValidDollarEscape() && !atDollarSlashEscape() && !atDollarDollarEscape()}? '$'
        )*
        (
            "/$"!
            {
                if (fromStart)      tt = STRING_LITERAL;  // plain regexp literal!
            }
        |   '$'!
            {
                // Yes, it's a regexp constructor, and we've got a value part.
                tt = (fromStart ? STRING_CTOR_START : STRING_CTOR_MIDDLE);
                stringCtorState = SCS_VAL + SCS_DRE_TYPE;
            }
        )
        {   $setType(tt);  }
    ;

protected ESCAPED_SLASH  : '$' '/' { $setText('/'); };

protected ESCAPED_DOLLAR : '$' '$' { $setText('$'); };

protected
REGEXP_SYMBOL
options {
    paraphrase="a multiline regular expression character";
}
    :
        ~('/'|'$'|'\\'|'\n'|'\r'|'\uffff')
    |   { LA(2)!='/' && LA(2)!='\n' && LA(2)!='\r' }? '\\' // backslash only escapes '/' and EOL
    |   '\\' '/'                   { $setText('/'); }
    |   STRING_NL[true]
    |!  '\\' ONE_NL[false]
    ;

protected
DOLLAR_REGEXP_SYMBOL
options {
    paraphrase="a multiline dollar escaping regular expression character";
}
    :
        ~('$' | '\\' | '/' | '\n' | '\r' | '\uffff')
    |   { LA(2)!='\n' && LA(2)!='\r' }? '\\'               // backslash only escapes EOL
    |   ('/' ~'$') => '/'                                  // allow a slash if not followed by a $
    |   STRING_NL[true]
    |!  '\\' ONE_NL[false]
    ;

// escape sequence -- note that this is protected; it can only be called
// from another lexer rule -- it will not ever directly return a token to
// the parser
// There are various ambiguities hushed in this rule. The optional
// '0'...'9' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched. ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC
options {
    paraphrase="an escape sequence";
}
    :   '\\'!
        (   'n'     {$setText("\n");}
        |   'r'     {$setText("\r");}
        |   't'     {$setText("\t");}
        |   'b'     {$setText("\b");}
        |   'f'     {$setText("\f");}
        |   '"'
        |   '\''
        |   '\\'
        |   '$'     //escape Groovy $ operator uniformly also
        |   ('u')+ {$setText("");}
            HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            {char ch = (char)Integer.parseInt($getText,16); $setText(ch);}
        |   '0'..'3'
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   '0'..'7'
                (
                    options {
                        warnWhenFollowAmbig = false;
                    }
                :   '0'..'7'
                )?
            )?
            {char ch = (char)Integer.parseInt($getText,8); $setText(ch);}
        |   '4'..'7'
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   '0'..'7'
            )?
            {char ch = (char)Integer.parseInt($getText,8); $setText(ch);}
        )
    |!  '\\' ONE_NL[false]
    ;

protected
STRING_NL[boolean allowNewline]
options {
    paraphrase="a newline inside a string";
}
    :  {if (!allowNewline) throw new MismatchedCharException('\n', '\n', true, this); }
       ONE_NL[false] { $setText('\n'); }
    ;

// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
options {
    paraphrase="a hexadecimal digit";
}
    :   ('0'..'9'|'A'..'F'|'a'..'f')
    ;

// a dummy rule to force vocabulary to be all characters (except special
// ones that ANTLR uses internally (0 to 2)
protected
VOCAB
options {
    paraphrase="a character";
}
    :   '\3'..'\377'
    ;

// an identifier. Note that testLiterals is set to true! This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifier
IDENT
options {
    paraphrase="an identifier";
}
    //options {testLiterals=true;}  // Actually, this is done manually in the actions below.
    :   ( {stringCtorState == 0}? (DOLLAR|LETTER) (LETTER|DIGIT|DOLLAR)* | LETTER (LETTER|DIGIT)* )
        {
            if (stringCtorState != 0) {
                if (LA(1) == '.' && LA(2) != '$' &&
                        Character.isJavaIdentifierStart(LA(2))) {
                    // pick up another name component before going literal again:
                    restartStringCtor(false);
                } else {
                    // go back to the string
                    restartStringCtor(true);
                }
            }
            int ttype = testLiteralsTable(IDENT);
            // Java doesn't have the keywords 'as', 'in' or 'def so we make some allowances
            // for them in package names for better integration with existing Java packages
            if ((ttype == LITERAL_as || ttype == LITERAL_def || ttype == LITERAL_in || ttype == LITERAL_trait) &&
                (LA(1) == '.' || lastSigTokenType == DOT || lastSigTokenType == LITERAL_package)) {
                ttype = IDENT;
            }
            // allow access to classes with the name package
            if ((ttype == LITERAL_package) &&
                (LA(1) == '.' || lastSigTokenType == DOT || lastSigTokenType == LITERAL_import
                || (LA(1) == ')' && lastSigTokenType == LPAREN))) {
                ttype = IDENT;
            }
            if (ttype == LITERAL_static && LA(1) == '.') {
                ttype = IDENT;
            }

            $setType(ttype);

            // check if "assert" keyword is enabled
            if (assertEnabled && "assert".equals($getText)) {
                $setType(LITERAL_assert); // set token type for the rule in the parser
            }
            // check if "enum" keyword is enabled
            if (enumEnabled && "enum".equals($getText)) {
                $setType(LITERAL_enum); // set token type for the rule in the parser
            }
        }
    ;

protected
LETTER
options {
    paraphrase="a letter";
}
    :   'a'..'z'|'A'..'Z'|'\u00C0'..'\u00D6'|'\u00D8'..'\u00F6'|'\u00F8'..'\u00FF'|'\u0100'..'\uFFFE'|'_'
    // TODO:  Recognize all the Java identifier starts here (except '$').
    ;

protected
DIGIT
options {
    paraphrase="a digit";
}
    :   '0'..'9'
    // TODO:  Recognize all the Java identifier parts here (except '$').
    ;

protected
DIGITS_WITH_UNDERSCORE
options {
    paraphrase="a sequence of digits and underscores, bordered by digits";
}
    :   DIGIT (DIGITS_WITH_UNDERSCORE_OPT)?
    ;

protected
DIGITS_WITH_UNDERSCORE_OPT
options {
    paraphrase="a sequence of digits and underscores with maybe underscore starting";
}
    :   (DIGIT | '_')* DIGIT
    ;

// a numeric literal
NUM_INT
options {
    paraphrase="a numeric literal";
}
    {boolean isDecimal=false; Token t=null;}
    :
        // TODO:  This complex pattern seems wrong.  Verify or fix.
        (   '0' {isDecimal = true;} // special case for just '0'
            (   // hex digits
                ('x'|'X')
                {isDecimal = false;}
                HEX_DIGIT
                (   options { warnWhenFollowAmbig=false; }
                    : (options { warnWhenFollowAmbig=false; } : HEX_DIGIT | '_')*
                    HEX_DIGIT
                )?

            |   //binary literal
                ('b'|'B') ('0'|'1') (('0'|'1'|'_')* ('0'|'1'))?
                {isDecimal = false;}

            |   //float or double with leading zero
                (   DIGITS_WITH_UNDERSCORE
                    ( '.' DIGITS_WITH_UNDERSCORE | EXPONENT | FLOAT_SUFFIX)
                ) => DIGITS_WITH_UNDERSCORE

            |   // octal
                ('0'..'7') (('0'..'7'|'_')* ('0'..'7'))?
                {isDecimal = false;}
            )?
        |   ('1'..'9') (DIGITS_WITH_UNDERSCORE_OPT)? {isDecimal=true;}               // non-zero decimal
        )
        (   ('l'|'L') { _ttype = NUM_LONG; }
        |   ('i'|'I') { _ttype = NUM_INT; }
        |   BIG_SUFFIX { _ttype = NUM_BIG_INT; }

        // only check to see if it's a float if looks like decimal so far
        |
            (~'.' | '.' ('0'..'9')) =>
            {isDecimal}?
            (   '.' DIGITS_WITH_UNDERSCORE (e:EXPONENT)? (f2:FLOAT_SUFFIX {t=f2;} | g2:BIG_SUFFIX {t=g2;})?
            |   EXPONENT (f3:FLOAT_SUFFIX {t=f3;} | g3:BIG_SUFFIX {t=g3;})?
            |   f4:FLOAT_SUFFIX {t=f4;}
            )
            {
                String txt = (t == null ? "" : t.getText().toUpperCase());
                if (txt.indexOf('F') >= 0) {
                    _ttype = NUM_FLOAT;
                } else if (txt.indexOf('G') >= 0) {
                    _ttype = NUM_BIG_DECIMAL;
                } else {
                    _ttype = NUM_DOUBLE; // assume double
                }
            }
        )?
    ;

// JDK 1.5 token for annotations and their declarations
// also a groovy operator for actual field access e.g. 'telson.@age'
AT
options {
    paraphrase="'@'";
}
    :   '@'
    ;

// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
options {
    paraphrase="an exponent";
}
    :   ('e'|'E') ('+'|'-')? ('0'..'9'|'_')* ('0'..'9')
    ;

protected
FLOAT_SUFFIX
options {
    paraphrase="a float or double suffix";
}
    :   'f'|'F'|'d'|'D'
    ;

protected
BIG_SUFFIX
options {
    paraphrase="a big decimal suffix";
}
    :   'g'|'G'
    ;

// Note: Please don't use physical tabs.  Logical tabs for indent are width 4.
// Here's a little hint for you, Emacs:
// Local Variables:
// tab-width: 4
// mode: antlr-mode
// indent-tabs-mode: nil
// End:
