// Note: Please don't use physical tabs.  Logical tabs for indent are width 4.
header {
package org.codehaus.groovy.antlr.parser;
import org.codehaus.groovy.antlr.*;
import java.util.*;
import java.io.InputStream;
import java.io.Reader;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;
}
 
/** JSR-241 Groovy Recognizer
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
 *                 Using Unicode charVocabulay makes code file big, but only
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
 *              keyword, since thats what JSR14 seems to imply.
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
 *    o for a map of the task see... 
 *      http://groovy.javanicus.com/java-g.png
 *
 * This grammar is in the PUBLIC DOMAIN
 */

class GroovyRecognizer extends Parser;
options {
    k = 3;                            // three token lookahead
    exportVocab=Groovy;               // Call its vocabulary "Groovy"
    codeGenMakeSwitchThreshold = 2;   // Some optimizations
    codeGenBitsetTestThreshold = 3;
    defaultErrorHandler = false;      // Don't generate parser error handlers
    buildAST = true;
//  ASTLabelType = "GroovyAST";
}

tokens {
    BLOCK; MODIFIERS; OBJBLOCK; SLIST; METHOD_DEF; VARIABLE_DEF;
    INSTANCE_INIT; STATIC_INIT; TYPE; CLASS_DEF; INTERFACE_DEF;
    PACKAGE_DEF; ARRAY_DECLARATOR; EXTENDS_CLAUSE; IMPLEMENTS_CLAUSE;
    PARAMETERS; PARAMETER_DEF; LABELED_STAT; TYPECAST; INDEX_OP;
    POST_INC; POST_DEC; METHOD_CALL; EXPR;
    IMPORT; UNARY_MINUS; UNARY_PLUS; CASE_GROUP; ELIST; FOR_INIT; FOR_CONDITION;
    FOR_ITERATOR; EMPTY_STAT; FINAL="final"; ABSTRACT="abstract";
    UNUSED_GOTO="goto"; UNUSED_CONST="const"; UNUSED_DO="do";
    STRICTFP="strictfp"; SUPER_CTOR_CALL; CTOR_CALL; CTOR_IDENT; VARIABLE_PARAMETER_DEF;
    STRING_CONSTRUCTOR; STRING_CTOR_MIDDLE;
    CLOSED_BLOCK; IMPLICIT_PARAMETERS;
    SELECT_SLOT; DYNAMIC_MEMBER;
    LABELED_ARG; SPREAD_ARG; SPREAD_MAP_ARG; SCOPE_ESCAPE;
    LIST_CONSTRUCTOR; MAP_CONSTRUCTOR;
    FOR_IN_ITERABLE;
    STATIC_IMPORT; ENUM_DEF; ENUM_CONSTANT_DEF; FOR_EACH_CLAUSE; ANNOTATION_DEF; ANNOTATIONS;
    ANNOTATION; ANNOTATION_MEMBER_VALUE_PAIR; ANNOTATION_FIELD_DEF; ANNOTATION_ARRAY_INIT;
    TYPE_ARGUMENTS; TYPE_ARGUMENT; TYPE_PARAMETERS; TYPE_PARAMETER; WILDCARD_TYPE;
    TYPE_UPPER_BOUNDS; TYPE_LOWER_BOUNDS;
}

{
        /** This factory is the correct way to wire together a Groovy parser and lexer. */
    public static GroovyRecognizer make(GroovyLexer lexer) {
        GroovyRecognizer parser = new GroovyRecognizer(lexer.plumb());
        // TODO: set up a common error-handling control block, to avoid excessive tangle between these guys
        parser.lexer = lexer;
        lexer.parser = parser;
        parser.setASTNodeClass("org.codehaus.groovy.antlr.GroovySourceAST");
        parser.warningList = new ArrayList();
        return parser;
    }
    // Create a scanner that reads from the input stream passed to us...
    public static GroovyRecognizer make(InputStream in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(Reader in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(InputBuffer in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(LexerSharedInputState in) { return make(new GroovyLexer(in)); }
    
    private static GroovySourceAST dummyVariableToforceClassLoaderToFindASTClass = new GroovySourceAST();

    List warningList;
    public List getWarningList() { return warningList; }
    
    boolean compatibilityMode = true;  // for now
    public boolean isCompatibilityMode() { return compatibilityMode; }
    public void setCompatibilityMode(boolean z) { compatibilityMode = z; }

    GroovyLexer lexer;
    public GroovyLexer getLexer() { return lexer; }
    public void setFilename(String f) { super.setFilename(f); lexer.setFilename(f); }

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
        try { lt = LT(1); }
        catch (TokenStreamException ee) { }
        if (lt == null)  lt = Token.badToken;
        throw new SemanticException(problem + ";\n   solution: " + solution,
                                    getFilename(), lt.getLine(), lt.getColumn());
    }

    public void addWarning(String warning, String solution) {
        Token lt = null;
        try { lt = LT(1); }
        catch (TokenStreamException ee) { }
        if (lt == null)  lt = Token.badToken;

        Map row = new HashMap();
        row.put("warning" ,warning);
        row.put("solution",solution);
        row.put("filename",getFilename());
        row.put("line"    ,new Integer(lt.getLine()));
        row.put("column"  ,new Integer(lt.getColumn()));
        // System.out.println(row);
        warningList.add(row);
    }

    // Convenience method for checking of expected error syndromes.
    private void require(boolean z, String problem, String solution) throws SemanticException {
        if (!z)  requireFailed(problem, solution);
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
        (   (annotationsOpt "package")=> packageDefinition
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


// Package statement: optional annotations followed by "package" then the package identifier.
packageDefinition
        //TODO? options {defaultErrorHandler = true;} // let ANTLR handle errors
    :   annotationsOpt p:"package"^ {#p.setType(PACKAGE_DEF);} identifier
    ;


// Import statement: import followed by a package or class name
importStatement
        //TODO? options {defaultErrorHandler = true;}
        { boolean isStatic = false; }
    :   i:"import"^ {#i.setType(IMPORT);} ( "static"! {#i.setType(STATIC_IMPORT);} )? identifierStar
    ;

// TODO REMOVE
// A type definition is either a class, interface, enum or annotation with possible additional semis.
//typeDefinition
//      options {defaultErrorHandler = true;}
//      :       m:modifiers!
//              typeDefinitionInternal[#m]
//      |       SEMI!
//      ;

// Added this production, even though 'typeDefinition' seems to be obsolete,
// as this is referenced by many other parts of the grammar.
// Protected type definitions production for reuse in other productions
protected typeDefinitionInternal[AST mods]
    :   cd:classDefinition[#mods]       // inner class
        {#typeDefinitionInternal = #cd;}
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


// *TODO* We must also audit the various occurrences of warning
// suppressions like "options { greedy = true; }".

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
 *  
 *   *TODO* The declarationStart production needs to be strengthened to recognize
 *  things like {List<String> foo}.
 *  Right now it only knows how to skip square brackets after the type, not
 *  angle brackets.
 *  This probably turns out to be tricky because of >> vs. > >. If so,
 *  just put a TODO comment in.
 */
declarationStart!
    :   "def"
    |   modifier
    |   AT IDENT  // IDENT != "interface"
    |   (   upperCaseIdent
        |   builtInType
        |   qualifiedTypeName
        ) (LBRACK balancedTokens RBRACK)* IDENT
    ;

/** Not yet used - but we could use something like this to look for fully qualified type names 
 */
qualifiedTypeName!
				 :
				 			 IDENT (DOT IDENT)* DOT upperCaseIdent
				 ;
	
/** Used to look ahead for a constructor 
 */
constructorStart!
    :
        modifiersOpt! id:IDENT! {isConstructorIdent(id)}? nls! LPAREN! //...
    ;


/** Used only as a lookahead predicate for nested type declarations. */

/*TODO* The lookahead in typeDeclarationStart needs to skip annotations, not
just stop at '@', because variable and method declarations can also be
annotated.
> typeDeclarationStart!
>     :   (modifier!)* ("class" | "interface" | "enum" | AT )
S.B. something like
>     :   (modifier! | annotationTokens!)* ("class" | "interface" |
> "enum" )
(And maybe @interface, if Java 5 allows nested annotation types? Don't
know offhand.)
Where annotationTokens can be a quick paren-skipper, as in other
places: '@' ident '(' balancedTokens ')'.
*/

typeDeclarationStart!
    :   modifiersOpt! ("class" | "interface" | "enum" | AT "interface")
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
classTypeSpec[boolean addImagNode]
    :   ct:classOrInterfaceType[false]!
        declaratorBrackets[#ct]
        {
            if ( addImagNode ) {
                #classTypeSpec = #(#[TYPE,"TYPE"], #classTypeSpec);
            }
        }
    ;

// A non-built in type name, with possible type parameters
classOrInterfaceType[boolean addImagNode]
    :   IDENT^ (typeArguments)?
        (   options{greedy=true;}: // match as many as possible
            DOT^
            IDENT (typeArguments)?
        )*
        {
            if ( addImagNode ) {
                #classOrInterfaceType = #(#[TYPE,"TYPE"], #classOrInterfaceType);
            }
        }
    ;

// A specialised form of typeSpec where built in types must be arrays
typeArgumentSpec
    :   classTypeSpec[true]
    |   builtInTypeArraySpec[true]
    ;

// A generic type argument is a class type, a possibly bounded wildcard type or a built-in type array
typeArgument
    :   (   typeArgumentSpec
        |   wildcardType
        )
        {#typeArgument = #(#[TYPE_ARGUMENT,"TYPE_ARGUMENT"], #typeArgument);}
    ;

// Wildcard type indicating all types (with possible constraint)
wildcardType
    :   q:QUESTION^ {#q.setType(WILDCARD_TYPE);}
        (("extends" | "super")=> typeArgumentBounds)?
    ;

// Type arguments to a class or interface type
typeArguments
{int currentLtLevel = 0;}
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
        {(currentLtLevel != 0) || ltCounter == currentLtLevel}?

        {#typeArguments = #(#[TYPE_ARGUMENTS, "TYPE_ARGUMENTS"], #typeArguments);}
    ;

// this gobbles up *some* amount of '>' characters, and counts how many
// it gobbled.
protected typeArgumentsOrParametersEnd
    :   GT! {ltCounter-=1;} nls!
    |   SR! {ltCounter-=2;} nls!
    |   BSR! {ltCounter-=3;} nls!
    ;

// Restriction on wildcard types based on super class or derrived class
typeArgumentBounds
    {boolean isUpperBounds = false;}
    :
        ( "extends"! {isUpperBounds=true;} | "super"! ) nls! classOrInterfaceType[false] nls!
        {
            if (isUpperBounds)
            {
                #typeArgumentBounds = #(#[TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS"], #typeArgumentBounds);
            }
            else
            {
                #typeArgumentBounds = #(#[TYPE_LOWER_BOUNDS,"TYPE_LOWER_BOUNDS"], #typeArgumentBounds);
            }
        }
    ;

// A builtin type array specification is a builtin type with brackets afterwards
builtInTypeArraySpec[boolean addImagNode]
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
                #builtInTypeArraySpec = #(#[TYPE,"TYPE"], #builtInTypeArraySpec);
            }
        }
    ;

// A builtin type specification is a builtin type with possible brackets
// afterwards (which would make it an array type).
builtInTypeSpec[boolean addImagNode]
    :   bt:builtInType!
        declaratorBrackets[#bt]
        {
            if ( addImagNode ) {
                #builtInTypeSpec = #(#[TYPE,"TYPE"], #builtInTypeSpec);
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
    |   "any"
    ;

// A (possibly-qualified) java identifier. We start with the first IDENT
// and expand its name by adding dots and following IDENTS
identifier
    :   IDENT
        (   options { greedy = true; } :
            DOT^ nls! IDENT )*
    ;

identifierStar
    :   IDENT
        (   options { greedy = true; } :
            DOT^  nls! IDENT )*
        (   DOT^  nls! STAR
        |   "as"^ nls! IDENT
        )?
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
            {LA(1)==AT && !LT(2).getText().equals("interface")}?
            annotation nls!
        )+
    ;

/** A list of one or more modifier, annotation, or "def". */
modifiers
    :   modifiersInternal
        {#modifiers = #([MODIFIERS, "MODIFIERS"], #modifiers);}
    ;

/** A list of zero or more modifiers, annotations, or "def". */
modifiersOpt
    :   (
            // See comment above on hushing warnings.
            options{generateAmbigWarnings=false;}:

            modifiersInternal
        )?
        {#modifiersOpt = #([MODIFIERS, "MODIFIERS"], #modifiersOpt);}
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

annotation!
    :   AT! i:identifier ( LPAREN! ( args:annotationArguments )? RPAREN! )?
        {#annotation = #(#[ANNOTATION,"ANNOTATION"], i, args);}
    ;

annotationsOpt
    :   (annotation nls!)*
        {#annotationsOpt = #([ANNOTATIONS, "ANNOTATIONS"], #annotationsOpt);}
;

annotationArguments
    :   annotationMemberValueInitializer | anntotationMemberValuePairs
    ;

anntotationMemberValuePairs
    :   annotationMemberValuePair ( COMMA! nls! annotationMemberValuePair )*
    ;

annotationMemberValuePair!
    :   i:IDENT ASSIGN! nls! v:annotationMemberValueInitializer
            {#annotationMemberValuePair = #(#[ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR"], i, v);}
    ;

annotationMemberValueInitializer
    :   conditionalExpression | annotation
    ;

/*OBS*
// This is an initializer used to set up an annotation member array.
annotationMemberArrayInitializer
    :   lc:LCURLY^ {#lc.setType(ANNOTATION_ARRAY_INIT);}
        (   annotationMemberArrayValueInitializer
            (
                // CONFLICT: does a COMMA after an initializer start a new
                // initializer or start the option ',' at end?
                // ANTLR generates proper code by matching
                // the comma as soon as possible.
                options {
                        warnWhenFollowAmbig = false;
                }
            :
                COMMA! nls! annotationMemberArrayValueInitializer
            )*
            (COMMA! nls!)?
        )?
        RCURLY!
    ;
*OBS*/

// The two things that can initialize an annotation array element are a conditional expression
// and an annotation (nested annotation array initialisers are not valid)
annotationMemberArrayValueInitializer
    :   conditionalExpression
    |   annotation nls!
    ;

superClassClause!
    :   ( "extends" nls! c:classOrInterfaceType[false] nls! )?
        {#superClassClause = #(#[EXTENDS_CLAUSE,"EXTENDS_CLAUSE"],c);}
    ;

// Definition of a Java class
classDefinition![AST modifiers]
    { AST prevCurrentClass = currentClass; }
    :   "class" IDENT nls!
       { currentClass = #IDENT; }
        // it _might_ have type paramaters
        (tp:typeParameters)?
        // it _might_ have a superclass...
        sc:superClassClause
        // it might implement some interfaces...
        ic:implementsClause
        // now parse the body of the class
        cb:classBlock
        {#classDefinition = #(#[CLASS_DEF,"CLASS_DEF"],
                                                            modifiers,IDENT,tp,sc,ic,cb);}
        { currentClass = prevCurrentClass; }
    ;

//TODO - where has superClassClause! production gone???

// Definition of a Java Interface
interfaceDefinition![AST modifiers]
    :   "interface" IDENT nls!
        // it _might_ have type paramaters
        (tp:typeParameters)?
        // it might extend some other interfaces
        ie:interfaceExtends
        // now parse the body of the interface (looks like a class...)
        ib:interfaceBlock
        {#interfaceDefinition = #(#[INTERFACE_DEF,"INTERFACE_DEF"],
                                  modifiers,IDENT,tp,ie,ib);}
    ;

enumDefinition![AST modifiers]
    :   "enum" IDENT
        // it might implement some interfaces...
        ic:implementsClause
        // now parse the body of the enum
        eb:enumBlock
        {#enumDefinition = #(#[ENUM_DEF,"ENUM_DEF"],
                             modifiers,IDENT,ic,eb);}
    ;

annotationDefinition![AST modifiers]
    :   AT "interface" IDENT
        // now parse the body of the annotation
        ab:annotationBlock
        {#annotationDefinition = #(#[ANNOTATION_DEF,"ANNOTATION_DEF"],
                                   modifiers,IDENT,ab);}
    ;

typeParameters
{int currentLtLevel = 0;}
    :
        {currentLtLevel = ltCounter;}
        LT! {ltCounter++;} nls!
        typeParameter (COMMA! nls! typeParameter)*
        nls!
        (typeArgumentsOrParametersEnd)?

        // make sure we have gobbled up enough '>' characters
        // if we are at the "top level" of nested typeArgument productions
        {(currentLtLevel != 0) || ltCounter == currentLtLevel}?

        {#typeParameters = #(#[TYPE_PARAMETERS, "TYPE_PARAMETERS"], #typeParameters);}
    ;

typeParameter
    :
        // I'm pretty sure Antlr generates the right thing here:
        (id:IDENT) ( options{generateAmbigWarnings=false;}: typeParameterBounds )?
        {#typeParameter = #(#[TYPE_PARAMETER,"TYPE_PARAMETER"], #typeParameter);}
    ;

typeParameterBounds
    :
        "extends"! nls! classOrInterfaceType[false]
        (BAND! nls! classOrInterfaceType[false])*
        {#typeParameterBounds = #(#[TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS"], #typeParameterBounds);}
    ;

// This is the body of a class. You can have classFields and extra semicolons.
classBlock
    :   LCURLY!
        ( classField )? ( sep! ( classField )? )*
        RCURLY!
        {#classBlock = #([OBJBLOCK, "OBJBLOCK"], #classBlock);}
    ;

// This is the body of an interface. You can have interfaceField and extra semicolons.
interfaceBlock
    :   LCURLY!
        ( interfaceField )? ( sep! ( interfaceField )? )*
        RCURLY!
        {#interfaceBlock = #([OBJBLOCK, "OBJBLOCK"], #interfaceBlock);}
    ;

// This is the body of an annotation. You can have annotation fields and extra semicolons,
// That's about it (until you see what an annoation field is...)
annotationBlock
    :   LCURLY!
        ( annotationField )? ( sep! ( annotationField )? )*
        RCURLY!
        {#annotationBlock = #([OBJBLOCK, "OBJBLOCK"], #annotationBlock);}
    ;

// This is the body of an enum. You can have zero or more enum constants
// followed by any number of fields like a regular class
enumBlock
    :   LCURLY!
        (
            // Need a syntactic predicate, since enumConstants
            // can start with foo() as well as classField.
            // (It's a true ambiguity, visible in the specification.
            // To resolve in practice, use "def" before a real method.)
            (enumConstantsStart)=> enumConstants
        |   (classField)?
        )
        ( sep! (classField)? )*
        RCURLY!
        {#enumBlock = #([OBJBLOCK, "OBJBLOCK"], #enumBlock);}
    ;

/** Guard for enumConstants.  */
enumConstantsStart
    :   enumConstant (COMMA | SEMI | NLS | RCURLY)
    ;

/** Comma-separated list of one or more enum constant definitions.  */
enumConstants
    :
        enumConstant
        ( options{greedy=true;}: COMMA! nls! enumConstant )*
        ( COMMA! nls! )?            // trailing extra comma is OK
    ;

// An annotation field
annotationField!
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

                /*OBS* rt:declaratorBrackets[#t] *OBS*/

                ( "default" nls! amvi:annotationMemberValueInitializer )?

                {#annotationField =
                        #(#[ANNOTATION_FIELD_DEF,"ANNOTATION_FIELD_DEF"],
                                 mods,
                                 #(#[TYPE,"TYPE"],t),
                                 i,amvi
                                 );}
            |   v:variableDefinitions[#mods,#t]    // variable
                {#annotationField = #v;}
            )
        )
    ;

//An enum constant may have optional parameters and may have a
//a class body
enumConstant!
    :   an:annotationsOpt // Note:  Cannot start with "def" or another modifier.
        i:IDENT
        (   LPAREN!
            a:argList
            RPAREN!
        )?
        ( b:enumConstantBlock )?
        {#enumConstant = #([ENUM_CONSTANT_DEF, "ENUM_CONSTANT_DEF"], an, i, a, b);}
    ;

//The class-like body of an enum constant
enumConstantBlock
    :   LCURLY!
        (enumConstantField)? ( sep! (enumConstantField)? )*
        RCURLY!
        {#enumConstantBlock = #([OBJBLOCK, "OBJBLOCK"], #enumConstantBlock);}
    ;

//An enum constant field is just like a class field but without
//the posibility of a constructor definition or a static initializer

// TODO - maybe allow 'declaration' production within this production, 
// but how to disallow constructors and static initializers...
enumConstantField!
    :   mods:modifiersOpt!
        (   td:typeDefinitionInternal[#mods]
            {#enumConstantField = #td;}
        |   // A generic method has the typeParameters before the return type.
            // This is not allowed for variable definitions, but this production
            // allows it, a semantic check could be used if you wanted.
            (tp:typeParameters)? t:typeSpec[false]          // method or variable declaration(s)
            (
                // Need a syntactic predicate, since variableDefinitions
                // can start with foo() also.  Since method defs are not legal
                // in this context, there's no harm done.
                (IDENT LPAREN)=>

                IDENT                                     // the name of the method

                // parse the formal parameter declarations.
                LPAREN! param:parameterDeclarationList RPAREN!

                /*OBS* rt:declaratorBrackets[#t] *OBS*/

                // get the list of exceptions that this method is
                // declared to throw
                (tc:throwsClause)?

                ( s2:compoundStatement )?
                // TODO - verify that 't' is useful/correct here, used to be 'rt'
                {#enumConstantField = #(#[METHOD_DEF,"METHOD_DEF"],
                                         mods,
                                         tp,
                                         #(#[TYPE,"TYPE"],t),
                                         IDENT,
                                         param,
                                         tc,
                                         s2);}

            |   v:variableDefinitions[#mods,#t]
                {#enumConstantField = #v;}
            )
        )

        // "{ ... }" instance initializer
    |   s4:compoundStatement
        {#enumConstantField = #(#[INSTANCE_INIT,"INSTANCE_INIT"], s4);}
    ;

// An interface can extend several other interfaces...
interfaceExtends
    :   (
            e:"extends"! nls!
            classOrInterfaceType[false] ( COMMA! nls! classOrInterfaceType[false] )* nls!
        )?
        {#interfaceExtends = #(#[EXTENDS_CLAUSE,"EXTENDS_CLAUSE"],
                               #interfaceExtends);}
    ;

// A class can implement several interfaces...
implementsClause
    :   (
            i:"implements"! nls!
            classOrInterfaceType[false] ( COMMA! nls! classOrInterfaceType[false] )* nls!
        )?
        {#implementsClause = #(#[IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE"],
                               #implementsClause);}
    ;

// Now the various things that can be defined inside a class
classField!
    :   // method, constructor, or variable declaration
        (constructorStart)=>
        mc:modifiersOpt! ctor:constructorDefinition[#mc]
        {#classField = #ctor;}
    |
        (declarationStart)=>
        d:declaration
        {#classField = #d;}
    |
        //TODO - unify typeDeclaration and typeDefinitionInternal names
        // type declaration
        (typeDeclarationStart)=>
        mods:modifiersOpt!
        (   td:typeDefinitionInternal[#mods]
                {#classField = #td;}
        )

    // "static { ... }" class initializer
    |   "static" s3:compoundStatement
        {#classField = #(#[STATIC_INIT,"STATIC_INIT"], s3);}

    // "{ ... }" instance initializer
    |   s4:compoundStatement
        {#classField = #(#[INSTANCE_INIT,"INSTANCE_INIT"], s4);}
    ;

// Now the various things that can be defined inside a interface
interfaceField!
    :   // method, constructor, or variable declaration
        (declarationStart)=>
        d:declaration
        {#interfaceField = #d;}
    |
        //TODO - unify typeDeclaration and typeDefinitionInternal names
        // type declaration
        (typeDeclarationStart)=>
        mods:modifiersOpt

        (   td:typeDefinitionInternal[#mods]
            {#interfaceField = #td;}
        )
    ;

constructorBody
    :   lc:LCURLY^ nls!         {#lc.setType(SLIST);}
        (   (explicitConstructorInvocation) =>   // Java compatibility hack
                explicitConstructorInvocation (sep! blockBody[sepToken])?
            |   blockBody[EOF]
        )
        RCURLY!
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

/** The tail of a declaration.
  * Either v1, v2, ... (with possible initializers) or else m(args){body}.
  * The two arguments are the modifier list (if any) and the declaration head (if any).
  * The declaration head is the variable type, or (for a method) the return type.
  * If it is missing, then the variable type is taken from its initializer (if there is one).
  * Otherwise, the variable type defaults to 'any'.
  * DECIDE:  Method return types default to the type of the method body, as an expression.
  */
variableDefinitions[AST mods, AST t]
    :   variableDeclarator[getASTFactory().dupTree(mods),
                           getASTFactory().dupTree(t)]
        (   COMMA! nls!
            variableDeclarator[getASTFactory().dupTree(mods),
                               getASTFactory().dupTree(t)]
        )*
    |
        // The parser allows a method definition anywhere a variable definition is accepted.

        (   id:IDENT
        |   qid:STRING_LITERAL          {#qid.setType(IDENT);}  // use for operator defintions, etc.
        )

        // parse the formal parameter declarations.
        LPAREN! param:parameterDeclarationList! RPAREN!

        /*OBS*rt:declaratorBrackets[#t]*/

        // get the list of exceptions that this method is
        // declared to throw
        (   tc:throwsClause!  )? nlsWarn!

        // the method body is an open block
        // but, it may have an optional constructor call (for constructors only)
        // this constructor clause is only used for constructors using 'def'
        // which look like method declarations
        (
                mb:openBlock!
        |   /*or nothing at all*/
        )
        {   if (#qid != null)  #id = #qid;
            #variableDefinitions =
                    #(#[METHOD_DEF,"METHOD_DEF"],
                      mods, #(#[TYPE,"TYPE"],t), id, param, tc, mb);
        }
    ;

/** I've split out constructors separately; we could maybe integrate back into variableDefinitions 
 *  later on if we maybe simplified 'def' to be a type declaration?
 */
constructorDefinition[AST mods]
    :
        id:IDENT

        // parse the formal parameter declarations.
        LPAREN! param:parameterDeclarationList! RPAREN!

        /*OBS*rt:declaratorBrackets[#t]*/

        // get the list of exceptions that this method is
        // declared to throw
        (   tc:throwsClause!  )? nlsWarn!

        // the method body is an open block
        // but, it may have an optional constructor call (for constructors only)

        // TODO assert that the id matches the class
        { isConstructorIdent(id); }

        cb:constructorBody!
        {   #constructorDefinition =  #(#[CTOR_IDENT,"CTOR_IDENT"],  mods, param, tc, cb);
        }
     ;

/** Declaration of a variable. This can be a class/instance variable,
 *  or a local variable in a method
 *  It can also include possible initialization.
 */
variableDeclarator![AST mods, AST t]
    :
        id:variableName
        /*OBS*d:declaratorBrackets[t]*/
        (v:varInitializer)?
        {#variableDeclarator = #(#[VARIABLE_DEF,"VARIABLE_DEF"], mods, #(#[TYPE,"TYPE"],t), id, v);}
    ;

/** Used in cases where a declaration cannot have commas, or ends with the "in" operator instead of '='. */
singleVariable![AST mods, AST t]
    :
        id:variableName
        {#singleVariable = #(#[VARIABLE_DEF,"VARIABLE_DEF"], mods, #(#[TYPE,"TYPE"],t), id);}
    ;

variableName
    :   IDENT
    ;

/** After some type names, where zero or more empty bracket pairs are allowed.
 *  We use ARRAY_DECLARATOR to represent this.
 *  TODO:  Is there some more Groovy way to view this in terms of the indexed property syntax?
 */
declaratorBrackets[AST typ]
    :   {#declaratorBrackets=typ;}
        (
            // A following list constructor might conflict with index brackets; prefer the declarator.
            options {greedy=true;} :
            lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!
        )*
    ;

/** An assignment operator '=' followed by an expression.  (Never empty.) */
varInitializer
    :   ASSIGN^ nls! initializer
    ;

/*OBS*
// This is an initializer used to set up an array.
arrayInitializer
    :   lc:LCURLY^ {#lc.setType(ARRAY_INIT);}
        (   initializer
            (
                // CONFLICT: does a COMMA after an initializer start a new
                // initializer or start the option ',' at end?
                // ANTLR generates proper code by matching
                // the comma as soon as possible.
                options {
                        warnWhenFollowAmbig = false;
                }
            :
                COMMA! initializer
            )*
            (COMMA!)?
        )?
        RCURLY!
    ;
*OBS*/

// The two "things" that can initialize an array element are an expression
// and another (nested) array initializer.
initializer
    :   expression
/*OBS*  // Use [...] for initializing all sorts of sequences, including arrays.
    |   arrayInitializer
*OBS*/
    ;

/*OBS???
// This is the header of a method. It includes the name and parameters
// for the method.
// This also watches for a list of exception classes in a "throws" clause.
ctorHead
    :   IDENT // the name of the method

        // parse the formal parameter declarations.
        LPAREN! parameterDeclarationList RPAREN!

        // get the list of exceptions that this method is declared to throw
        (throwsClause)?
    ;
*OBS*/

// This is a list of exception classes that the method is declared to throw
throwsClause
    :   "throws"^ nls! identifier ( COMMA! nls! identifier )* nls!
    ;

/** A list of zero or more formal parameters.
 *  If a parameter is variable length (e.g. String... myArg) it should be
 *  to the right of any other parameters of the same kind.
 *  General form:  (req, ..., opt, ..., [rest], key, ..., [restKeys], [block]
 *  This must be sorted out after parsing, since the various declaration forms
 *  are impossible to tell apart without backtracking.
 */
parameterDeclarationList
    :
        (
            parameterDeclaration
            (   COMMA! nls!
                parameterDeclaration
            )*
        )?
        {#parameterDeclarationList = #(#[PARAMETERS,"PARAMETERS"],
                                       #parameterDeclarationList);}
    ;

/** A formal parameter for a method or closure. */
parameterDeclaration!
        { boolean spreadParam = false; }
    :
        pm:parameterModifiersOpt
        (   options {greedy=true;} :
            t:typeSpec[false]
        )?

        // TODO:  What do formal parameters for keyword arguments look like?

        // TODO:  Should this be SPREAD_ARG instead?
        ( TRIPLE_DOT! { spreadParam = true; } )?

        id:IDENT

        // allow an optional default value expression
        (exp:varInitializer)?

        /*OBS*pd:declaratorBrackets[#t]*/
        {
            if (spreadParam) {
                #parameterDeclaration = #(#[VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF"],
                      pm, #([TYPE,"TYPE"],t), id, exp);
            } else {
                #parameterDeclaration = #(#[PARAMETER_DEF,"PARAMETER_DEF"],
                      pm, #([TYPE,"TYPE"],t), id, exp);
            }
        }
    ;

/*OBS*
variableLengthParameterDeclaration!
    :   pm:parameterModifier t:typeSpec[false] TRIPLE_DOT! id:IDENT

        /*OBS* pd:declaratorBrackets[#t]* /
        {#variableLengthParameterDeclaration = #(#[VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF"],
                                                                                            pm, #([TYPE,"TYPE"],t), id);}
    ;
*OBS*/

/** A simplified formal parameter for closures, can occur outside parens.
 *  It is not confused by a lookahead of BOR.
 *  DECIDE:  Is thie necessary, or do we change the closure-bar syntax?
 */
simpleParameterDeclaration!
    :   ( options {greedy=true;} : t:typeSpec[false])?
        id:IDENT
        {#simpleParameterDeclaration = #(#[PARAMETER_DEF,"PARAMETER_DEF"],
              #(#[MODIFIERS,"MODIFIERS"]), #([TYPE,"TYPE"],t), id);}
    ;

/** Simplified formal parameter list for closures.  Never empty. */
simpleParameterDeclarationList
    :
        simpleParameterDeclaration
        (   COMMA! nls!
            simpleParameterDeclaration
        )*
        {#simpleParameterDeclarationList = #(#[PARAMETERS,"PARAMETERS"],
                                             #simpleParameterDeclarationList);}
    ;

parameterModifiersOpt
        { int seenDef = 0; }
        //final and/or def can appear amongst annotations in any order
    :   (   {seenDef++ == 0}?       // do not allow multiple "def" tokens
            "def"!  nls!            // redundant, but allowed for symmetry
        |   "final" nls!
        |   annotation nls!
        )*
        {#parameterModifiersOpt = #(#[MODIFIERS,"MODIFIERS"], #parameterModifiersOpt);}
    ;

/** Closure parameters are exactly like method parameters,
 *  except that they are not enclosed in parentheses, but rather
 *  are prepended to the front of a block, just after the brace.
 *  They are separated from the closure body by a CLOSURE_OP token '->'.
 */
// With '|' there would be restrictions on bitwise-or expressions.
closureParametersOpt[boolean addImplicit]
    :   (parameterDeclarationList nls CLOSURE_OP)=>
        parameterDeclarationList nls! CLOSURE_OP! nls!
    |   {compatibilityMode}? (oldClosureParametersStart)=>
        oldClosureParameters
    |   {addImplicit}?
        implicitParameters
    |
        /* else do not parse any parameters at all */
    ;

/** Lookahead to check whether a block begins with explicit closure arguments. */
closureParametersStart!
    :
        {compatibilityMode}? (oldClosureParametersStart)=>
        oldClosureParametersStart
    |
        parameterDeclarationList nls CLOSURE_OP
    ;

/** Provisional definition of old-style closure params based on BOR '|'.
 *  Going away soon, perhaps... */
oldClosureParameters
    :   LOR! nls!  // '||' operator is a null param list
        {#oldClosureParameters = #(#[PARAMETERS,"PARAMETERS"]);}
    |   (BOR nls BOR)=>
        BOR! nls! BOR! nls!
        {#oldClosureParameters = #(#[PARAMETERS,"PARAMETERS"]);}
    |   ((BOR nls)? LPAREN parameterDeclarationList RPAREN nls BOR)=>
        (BOR! nls!)? LPAREN! parameterDeclarationList RPAREN! nls! BOR! nls!
    |   ((BOR nls)? simpleParameterDeclarationList nls BOR)=>
        (BOR! nls!)? simpleParameterDeclarationList nls! BOR! nls!
    ;

/** Lookahead for oldClosureParameters. */
oldClosureParametersStart!
    :   BOR
    |   LOR // for empty parameter declaration
    |   LPAREN balancedTokens RPAREN nls BOR
    //| (IDENT nls (BOR | COMMA))=>
    |   simpleParameterDeclarationList BOR
    ;

/** Simple names, as in {x|...}, are completely equivalent to {(def x)|...}.  Build the right AST. */
closureParameter!
    :   id:IDENT!
        {#closureParameter = #(#[PARAMETER_DEF,"PARAMETER_DEF"],
                               #(#[MODIFIERS,"MODIFIERS"]), #([TYPE,"TYPE"]),
                               id);}
    ;

// Compound statement. This is used in many contexts:
// Inside a class definition prefixed with "static":
// it is a class initializer
// Inside a class definition without "static":
// it is an instance initializer
// As the body of a method
// As a completely indepdent braced block of code inside a method
// it starts a new scope for variable definitions
// In Groovy, this is called an "open block".  It cannot have closure arguments.

compoundStatement
    :   openBlock
    ;

/** An open block is not allowed to have closure arguments. */
openBlock
    :   lc:LCURLY^ nls!     {#lc.setType(SLIST);}
        // AST type of SLIST means "never gonna be a closure"
        blockBody[EOF]
        RCURLY!
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
closedBlock
    :   lc:LCURLY^ nls!     {#lc.setType(CLOSED_BLOCK);}
        closureParametersOpt[true]
        blockBody[EOF]
        RCURLY!
    ;

/** An appended block follows a method name or method argument list. */
appendedBlock
    :
    /*  FIXME!
        DECIDE: should appended blocks accept labels?
        (IDENT COLON nls LCURLY)=>
        IDENT c:COLON^ {#c.setType(LABELED_ARG);} nls!
        closedBlock
    |
    */
        nlsWarn!
        closedBlock
    ;

appendedBlockStart!
    :
    /*
        IDENT COLON nls LCURLY
    |
    */
        (NLS)? LCURLY
    ;

/** A block known to be a closure, but which omits its arguments, is given this placeholder.
 *  A subsequent pass is responsible for deciding if there is an implicit 'it' parameter,
 *  or if the parameter list should be empty.
 */
implicitParameters
    :   {   #implicitParameters = #(#[IMPLICIT_PARAMETERS,"IMPLICIT_PARAMETERS"]);  }
    ;

/** A sub-block of a block can be either open or closed.
 *  It is closed if and only if there are explicit closure arguments.
 *  Compare this to a block which is appended to a method call,
 *  which is given closure arguments, even if they are not explicit in the code.
 */
openOrClosedBlock
    :   lc:LCURLY^ nls!
        cp:closureParametersOpt[false]
        {   if (#cp == null)    #lc.setType(SLIST);
            else                #lc.setType(CLOSED_BLOCK);
        }
        blockBody[EOF]
        RCURLY!
    ;

/** A statement is an element of a block.
 *  Typical statements are declarations (which are scoped to the block)
 *  and expressions.
 */
statement[int prevToken]
    // prevToken is NLS if previous statement is separated only by a newline

    // declarations are ambiguous with "ID DOT" relative to expression
    // statements. Must backtrack to be sure. Could use a semantic
    // predicate to test symbol table to see what the type was coming
    // up, but that's pretty hard without a symbol table ;)
    :   (declarationStart)=>
        declaration

    // Attach a label to the front of a statement
    // This block is executed for effect, unless it has an explicit closure argument.
    |
        (IDENT COLON)=>
        pfx:statementLabelPrefix!
        {#statement = #pfx;}  // nest it all under the label prefix
        (   (LCURLY) => openOrClosedBlock
        |   statement[COLON]
        )

    // An expression statement. This could be a method call,
    // assignment statement, or any other expression evaluated for
    // side-effects.
    // The prevToken is used to check for dumb expressions like +1.
    |    expressionStatement[prevToken]

    // class definition
    |    m:modifiersOpt! typeDefinitionInternal[#m]

    // If-else statement
    |   "if"^ LPAREN! strictContextExpression RPAREN! nlsWarn! compatibleBodyStatement
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
            "else"! nlsWarn! compatibleBodyStatement
        )?

    // For statement
    |   forStatement

    // While statement
    |   "while"^ LPAREN! strictContextExpression RPAREN! nlsWarn! compatibleBodyStatement

    /*OBS* no do-while statement in Groovy (too ambiguous)
    // do-while statement
    |   "do"^ statement "while"! LPAREN! expression RPAREN! SEMI!
    *OBS*/
    // With statement
    // (This is the Groovy scope-shift mechanism, used for builders.)
    |   "with"^ LPAREN! strictContextExpression RPAREN! nlsWarn! compoundStatement

    // Splice statement, meaningful only inside a "with" expression.
    // PROPOSED, DECIDE.  Prevents the namespace pollution of a "text" method or some such.
    |   sp:STAR^ nls!                       {#sp.setType(SPREAD_ARG);}
        expressionStatement[EOF]
    // Example:  with(htmlbuilder) { head{} body{ *"some text" } }
    // Equivalent to:  { htmlbuilder.head{} htmlbuilder.body{ (htmlbuilder as Collection).add("some text") } }

    // Import statement.  Can be used in any scope.  Has "import x as y" also.
    |   importStatement

    // switch/case statement
    |   "switch"^ LPAREN! strictContextExpression RPAREN! nlsWarn! LCURLY! nls!
        ( casesGroup )*
        RCURLY!

    // exception try-catch block
    |   tryBlock

    // synchronize a statement
    |   "synchronized"^ LPAREN! expression RPAREN! nlsWarn! compoundStatement


    /*OBS*
    // empty statement
    |   s:SEMI {#s.setType(EMPTY_STAT);}
    *OBS*/

    |   branchStatement
    ;

forStatement
    :   f:"for"^
        LPAREN!
        (   (forInit SEMI)=>traditionalForClause
            // *OBS*
            // There's no need at all for squeezing in the new Java 5 "for"
            // syntax, since Groovy's is a suitable alternative.
            // |   (parameterDeclaration COLON)=> forEachClause
            // *OBS*
        |   // the coast is clear; it's a modern Groovy for statement
            forInClause
        )
        RPAREN! nlsWarn!
        compatibleBodyStatement                                  // statement to loop over
    ;

traditionalForClause
    :
        forInit SEMI!   // initializer
        forCond SEMI!   // condition test
        forIter         // updater
    ;

/*OBS*
forEachClause
    :
        p:parameterDeclaration COLON! expression
        {#forEachClause = #(#[FOR_EACH_CLAUSE,"FOR_EACH_CLAUSE"], #forEachClause);}
    ;
*OBS*/

forInClause
    :   (   (declarationStart)=>
            singleDeclarationNoInit
        |   IDENT
        )
        i:"in"^         {#i.setType(FOR_IN_ITERABLE);}
        shiftExpression
    ;

/** In Java, "if", "while", and "for" statements can take random, non-braced statements as their bodies.
 *  Support this practice, even though it isn't very Groovy.
 */
compatibleBodyStatement
    :   (LCURLY)=>
        compoundStatement
    |
        statement[EOF]
    ;

/** In Groovy, return, break, continue, throw, and assert can be used in a parenthesized expression context.
 *  Example:  println (x || (return));  println assert x, "won't print a false value!"
 *  If an optional expression is missing, its value is void (this coerces to null when a value is required).
 */
branchStatement
    :
    // Return an expression
        "return"^
        ( expression )?

    // break:  get out of a loop, or switch, or method call
    // continue:  do next iteration of a loop, or leave a closure
    |   ("break"^ | "continue"^)
        (
            (IDENT COLON)=>
            statementLabelPrefix
        )?
        ( expression )?

    // throw an exception
    |   "throw"^ expression


    // TODO - decide on definitive 'assert' statement in groovy (1.4 and|or groovy)
    // asserts
    // 1.4+ ...
    //      |   "assert"^ expression ( COLON! expression )?

    // groovy assertion...
    |   "assert"^ expression
        (   options {greedy=true;} :
            COMMA!  // TODO:  s/COMMA/COLON/; gratuitous change causes BSFTest failures
            expression
        )?
    ;

/** A labeled statement, consisting of a vanilla identifier followed by a colon. */
// Note:  Always use this lookahead, to keep antlr from panicking: (IDENT COLON)=>
statementLabelPrefix
    :   IDENT c:COLON^ {#c.setType(LABELED_STAT);}
    ;

/** An expression statement can be any general expression.
 *  <p>
 *  An expression statement can also be a <em>command</em>,
 *  which is a simple method call in which the outermost parentheses are omitted.
 *  <p>
 *  Certain "suspicious" looking forms are flagged for the user to disambiguate.
 */
// DECIDE: A later semantic pass can flag dumb expressions that dot occur in
//         positions where their value is not used, e.g., <code>{1+1;println}</code>
expressionStatement[int prevToken]
            {boolean zz; /*ignore*/ }
    : 
        (   (suspiciousExpressionStatementStart)=>
            checkSuspiciousExpressionStatement[prevToken]
        )?
        // Checks are now out of the way; here's the real rule:
        (
            (expression (SEMI | NLS | RCURLY | EOF))=>  //FIXME: too much lookahead
            expression
        |
            head:pathExpression!
            commandArguments[#head]
            {#expressionStatement = #(#[EXPR,"EXPR"],#expressionStatement);}
        )
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
        (~LCURLY | LCURLY closureParametersStart)=>  //FIXME too much lookahead
        // Either not a block, or a block with an explicit closure parameter list.
        (   {prevToken == NLS}?
            {   addWarning(
                "Expression statement looks like it may continue a previous statement.",
                "Either remove previous newline, or add an explicit semicolon ';'.");
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
            "Closure expression looks like it may be an isolated open block, "+
            "or it may continue a previous statement."
            ,
            "Add an explicit parameter list, as in {it -> ...}, or label it as L:{...}, "+
            "and also either remove previous newline, or add an explicit semicolon ';'."
            );
        }
    |
        {prevToken != NLS}?
        // If prevToken is SEMI or something else, issue a single warning:
        // Example:  obj.foo ; {println x}
        // Might be closure expression:  obj.foo ; {x->println x}
        // Might be open block:  obj.foo ; L:{println x}
        {   require(false,
            "Closure expression looks like it may be an isolated open block.",
            "Add an explicit parameter list, as in {it -> ...}, or label it as L:{...}.");
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
casesGroup
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
        {#casesGroup = #([CASE_GROUP, "CASE_GROUP"], #casesGroup);}
    ;

aCase
    :   ("case"^ expression | "default") COLON! nls!
    ;

caseSList
    :   statement[COLON] (sep! (statement[sepToken])?)*
        {#caseSList = #(#[SLIST,"SLIST"],#caseSList);}
    ;

// The initializer for a for loop
forInit
    :   // if it looks like a declaration, it is
        (declarationStart)=> declaration
    |   // else it's a comma-separated list of expressions
        (controlExpressionList)?
        {#forInit = #(#[FOR_INIT,"FOR_INIT"],#forInit);}
    ;

forCond
    :   (strictContextExpression)?
        {#forCond = #(#[FOR_CONDITION,"FOR_CONDITION"],#forCond);}
    ;

forIter
    :   (controlExpressionList)?
        {#forIter = #(#[FOR_ITERATOR,"FOR_ITERATOR"],#forIter);}
    ;

// an exception handler try/catch block
tryBlock
    :   "try"^ nlsWarn! compoundStatement
            ( options {greedy=true;} :  nls! handler)*
            ( options {greedy=true;} :  nls! finallyClause)?
    ;

finallyClause
    :   "finally"^ nlsWarn! compoundStatement
    ;

// an exception handler
handler
    :   "catch"^ LPAREN! parameterDeclaration RPAREN! nlsWarn! compoundStatement
    ;

/** A member name (x.y) or element name (x[y]) can serve as a command name,
 *  which may be followed by a list of arguments.
 *  Unlike parenthesized arguments, these must be plain expressions,
 *  without labels or spread operators.
 */
commandArguments[AST head]
    :
        expression ( COMMA! nls! expression )*
        // println 2+2 //OK
        // println(2+2) //OK
        // println (2)+2 //BAD
        // println((2)+2) //OK
        // (println(2)+2) //OK
        // compare (2), 2 //BAD
        // compare( (2), 2 ) //OK
        // foo.bar baz{bat}, bang{boz} //OK?!
        {
            AST elist = #(#[ELIST,"ELIST"], #commandArguments);
            AST headid = getASTFactory().dup(#head);
            headid.setType(METHOD_CALL);
            headid.setText("<command>");
            #commandArguments = #(headid, head, elist);
        }
    ;

// expressions
// Note that most of these expressions follow the pattern
//   thisLevelExpression :
//         nextHigherPrecedenceExpression
//                 (OPERATOR nextHigherPrecedenceExpression)*
// which is a standard recursive definition for a parsing an expression.
// The operators in java have the following precedences:
//      lowest  (15)  = **= *= /= %= += -= <<= >>= >>>= &= ^= |=
//                      (14)  ?:
//                      (13)  ||
//                      (12)  &&
//                      (11)  |
//                      (10)  ^
//                      ( 9)  &
//                      ( 8)  == != <=>
//                      ( 7)  < <= > >= instanceof as
//                      ( 6)  << >> .. ...
//                      ( 5)  +(binary) -(binary)
//                      ( 4)  * / %
//                      ( 3)  ++(pre/post) --(pre/post) +(unary) -(unary)
//                      ( 2)  **(power)
//                      ( 1)  ~  ! $ (type)
//                            . ?. *. (dot -- identifier qualification)
//                            []   () (method call)  {} (closure)  [] (list/map)
//                            new  () (explicit parenthesis)
//                            $x (scope escape)
//
// the last two are not usually on a precedence chart; I put them in
// to point out that new has a higher precedence than '.', so you
// can validy use
//       new Frame().show()
//
// Note that the above precedence levels map to the rules below...
// Once you have a precedence chart, writing the appropriate rules as below
//   is usually very straightfoward


// the mother of all expressions
// This nonterminal is not used for expression statements, which have a more restricted syntax
// due to possible ambiguities with other kinds of statements.  This nonterminal is used only
// in contexts where we know we have an expression.  It allows general Java-type expressions.
expression
    :   assignmentExpression
        {#expression = #(#[EXPR,"EXPR"],#expression);}
    ;

// This is a list of expressions.
// Used for backward compatibility, in a few places where
// comma-separated lists of Java expression statements and declarations are required.
controlExpressionList
    :   strictContextExpression (COMMA! nls! strictContextExpression)*
        {#controlExpressionList = #(#[ELIST,"ELIST"], controlExpressionList);}
    ;

/** A "path expression" is a name or other primary, possibly qualified by various
 *  forms of dot, and/or followed by various kinds of brackets.
 *  It can be used for value or assigned to, or else further qualified, indexed, or called.
 *  It is called a "path" because it looks like a linear path through a data structure.
 *  Examples:  x.y, x?.y, x*.y, x.@y; x[], x[y], x[y,z]; x(), x(y), x(y,z); x{s}; a.b[n].c(x).d{s}
 *  (Compare to a C lvalue, or LeftHandSide in the JLS section 15.26.)
 *  General expressions are built up from path expressions, using operators like '+' and '='.
 */
pathExpression
        { AST prefix = null; }
    :
        pre:primaryExpression!
        { prefix = #pre; }

        (   // FIXME: There will be a harmless ANTLR warning on this line.  Can't make it go away??
            options {
                // \n{foo} could match here or could begin a new statement
                // We do want to match here. Turn off warning.
                greedy=true;
                // This turns the ambiguity warning of the second alternative
                // off. See below. (The "ANTLR_LOOP_EXIT" predicate makes it non-issue)
                warnWhenFollowAmbig=false;
            }
            // Parsing of this chain is greedy.  For example, a pathExpression may be a command name
            // followed by a command argument, but that command argument cannot begin with an LPAREN,
            // since a parenthesized expression is greedily attached to the pathExpression as a method argument.
            // The lookahead is also necessary to reach across newline in foo \n {bar}.
            // (Apparently antlr's basic approximate LL(k) lookahead is too weak for this.)
        :   (pathElementStart)=>
            pe:pathElement[prefix]!
            { prefix = #pe; }
        |
            {ANTLR_LOOP_EXIT}?
        )*

        { #pathExpression = prefix; }
    ;

pathElement[AST prefix]
        // The primary can then be followed by a chain of .id, (a), [a], and {...}
    :
        {   #pathElement = prefix;  }
        (   // Spread operator:  x*.y  ===  x?.collect{it.y}
            SPREAD_DOT^
        |   // Optional-null operator:  x?.y  === (x==null)?null:x.y
            OPTIONAL_DOT^
        |   // Member pointer operator: foo.&y == foo.metaClass.getMethodPointer(foo, "y")
            MEMBER_POINTER^
        |   // The all-powerful dot.
            DOT^
        ) nls!
        (typeArguments)?   // TODO: Java 5 type argument application via prefix x.<Integer>y
        namePart
    |
        mca:methodCallArgs[prefix]!
        {   #pathElement = #mca;  }
    |
        // Element selection is always an option, too.
        // In Groovy, the stuff between brackets is a general argument list,
        // since the bracket operator is transformed into a method call.
        ipa:indexPropertyArgs[prefix]!
        {   #pathElement = #ipa;  }

/*NYI*
    |   DOT^ nls! "this"

    |   DOT^ nls! "super"
        (   // (new Outer()).super()  (create enclosing instance)
            lp3:LPAREN^ argList RPAREN!
            {#lp3.setType(SUPER_CTOR_CALL);}
        |   DOT^ IDENT
            (   lps:LPAREN^ {#lps.setType(METHOD_CALL);}
                argList
                RPAREN!
            )?
        )
    |   DOT^ nls! newExpression
*NYI*/
    ;

pathElementStart!
    :   DOT
    |   SPREAD_DOT
    |   OPTIONAL_DOT
    |   MEMBER_POINTER
    |   LBRACK
    |   LPAREN
    |   appendedBlockStart
    ;

/** This is the grammar for what can follow a dot:  x.a, x.@a, x.&a, x.'a', etc.
 *  Note: <code>typeArguments</code> is handled by the caller of <code>namePart</code>.
 */
namePart
    :
        (   ats:AT^     {#ats.setType(SELECT_SLOT);}  )?
        // foo.@bar selects the field (or attribute), not property

        (   IDENT
        |   sl:STRING_LITERAL {#sl.setType(IDENT);}
            // foo.'bar' is in all ways same as foo.bar, except that bar can have an arbitrary spelling
        |   dn:dynamicMemberName!
            { #namePart = #(#[DYNAMIC_MEMBER, "DYNAMIC_MEMBER"], #dn); }
            // DECIDE PROPOSAL:  foo.(bar), x.(p?'a':'b') means dynamic lookup on a dynamic name
        |
            openBlock
            // PROPOSAL, DECIDE:  Is this inline form of the 'with' statement useful?
            // Definition:  a.{foo} === {with(a) {foo}}
            // May cover some path expression use-cases previously handled by dynamic scoping (closure delegates).

                                                                    // lets allow common keywords as property names
        |   keywordPropertyNames

/* lets allow some common keywords for properties like 'in', 'class', 'def' etc
 * TODO: Reinstate this logic if we change or remove keywordPropertyNames.
 * See also LITERAL_in logic in the lexer.
        // Recover with a good diagnostic from a common error:
        |   "in"  // poster child; the lexer makes all keywords after dot look like "in"
            {   String kwd = LT(1).getText();
                require(false,
                    "illegal keyword after dot in x."+kwd,
                    "put the keyword in quotes, as in x.'"+kwd+"'");
                // This helps the user recover from ruined Java identifiers, as in System.'in'.
                // DECIDE: Shall we just define foo.in to DTRT automagically, or do we want the syntax check?
            }
*/                        
        )

        // (No, x.&@y is not needed; just say x.&y as Slot or some such.)
    ;

/** Allowed keywords after dot (as a member name) and before colon (as a label).
 *  TODO: What's the rationale for these?
 */
keywordPropertyNames
    :   (   "class" | "in" | "as" | "def"
        |   "if" | "else" | "for" | "while" | "do" | "switch" | "try" | "catch" | "finally"
        |   builtInType
        )
        { #keywordPropertyNames.setType(IDENT); }
    ;
                                                
/** If a dot is followed by a parenthesized or quoted expression, the member is computed dynamically,
 *  and the member selection is done only at runtime.  This forces a statically unchecked member access.
 */
dynamicMemberName
    :   (   parenthesizedExpression
        |   stringConstructorExpression
        )
        { #dynamicMemberName = #(#[DYNAMIC_MEMBER, "DYNAMIC_MEMBER"], #dynamicMemberName); }
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
 *  <p>
 *  Returned AST is [METHOD_CALL, callee, ELIST?, CLOSED_BLOCK?].
 *  Note that callee is often of the form x.y but not always.
 */
methodCallArgs[AST callee]
    :
        {#methodCallArgs = callee;}
        lp:LPAREN^ {#lp.setType(METHOD_CALL);}
        argList
        RPAREN!
        (   (appendedBlockStart)=>   // eagerly reach across newline to {...}
            appendedBlock
        )?  // maybe append a closure
    |
        // else use a closure alone
        {   AST lbrace = getASTFactory().create(LT(1));
            lbrace.setType(METHOD_CALL);
            lbrace.addChild(callee);
            #methodCallArgs = lbrace;
        }
        appendedBlock
    ;

/** An expression may be followed by [...].
 *  Unlike Java, these brackets may contain a general argument list,
 *  which is passed to the array element operator, which can make of it what it wants.
 *  The brackets may also be empty, as in T[].  This is how Groovy names array types.
 *  <p>Returned AST is [INDEX_OP, indexee, ELIST].
 */
indexPropertyArgs[AST indexee]
    :
        {#indexPropertyArgs = indexee;}
        lb:LBRACK^ {#lb.setType(INDEX_OP);}
        argList
        RBRACK!
    ;

// assignment expression (level 15)
assignmentExpression
    :   conditionalExpression
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
            //|   USEROP_13^  //DECIDE: This is how user-define ops would show up.
            )
            nls!
            assignmentExpression
        )?
    ;

// conditional test (level 14)
conditionalExpression
    :   logicalOrExpression
        ( QUESTION^ nls! assignmentExpression COLON! nls! conditionalExpression )?
    ;


// logical or (||)  (level 13)
logicalOrExpression
    :   logicalAndExpression (LOR^ nls! logicalAndExpression)*
    ;


// logical and (&&)  (level 12)
logicalAndExpression
    :   inclusiveOrExpression (LAND^ nls! inclusiveOrExpression)*
    ;

// bitwise or non-short-circuiting or (|)  (level 11)
inclusiveOrExpression
    :   exclusiveOrExpression (BOR^ nls! exclusiveOrExpression)*
    ;


// exclusive or (^)  (level 10)
exclusiveOrExpression
    :   andExpression (BXOR^ nls! andExpression)*
    ;


// bitwise or non-short-circuiting and (&)  (level 9)
andExpression
    :   regexExpression (BAND^ nls! regexExpression)*
    ;

// regex find and match (=~ and ==~) (level 8.5)
// jez: moved =~ closer to precedence of == etc, as...
// 'if (foo =~ "a.c")' is very close in intent to 'if (foo == "abc")'
regexExpression
    :   equalityExpression ((REGEX_FIND^ | REGEX_MATCH^) nls! equalityExpression)*
    ;

// equality/inequality (==/!=) (level 8)
equalityExpression
    :   relationalExpression ((NOT_EQUAL^ | EQUAL^ | COMPARE_TO^) nls! relationalExpression)*
    ;

// boolean relational expressions (level 7)
relationalExpression
    :   shiftExpression
        (   (   (   LT^
                |   GT^
                |   LE^
                |   GE^
                |   "in"^
                )
                nls!
                shiftExpression
            )?
        |   "instanceof"^ nls! typeSpec[true]
        |   "as"^         nls! typeSpec[true] //TODO: Rework to allow type expression?
        )
    ;



// bit shift expressions (level 6)
shiftExpression
    :   additiveExpression
        (
            ((SL^ | SR^ | BSR^)
            |   RANGE_INCLUSIVE^
            |   RANGE_EXCLUSIVE^
            |   td:TRIPLE_DOT^ {#td.setType(RANGE_EXCLUSIVE);} /* backward compat: FIXME REMOVE */
            )
            nls!
            additiveExpression
        )*
    ;


// binary addition/subtraction (level 5)
additiveExpression
    :   multiplicativeExpression
        (
            (PLUS^ | MINUS^) nls!
            multiplicativeExpression
        )*
    ;


// multiplication/division/modulo (level 4)
multiplicativeExpression
    :    ( INC^ nls!  powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
    |    ( DEC^ nls!  powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
    |    ( MINUS^ {#MINUS.setType(UNARY_MINUS);} nls!   powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
    |    ( PLUS^ {#PLUS.setType(UNARY_PLUS);} nls!   powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
    |    (  powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
    ;

// math power operator (**) (level 3)
powerExpression
    :   unaryExpressionNotPlusMinus (STAR_STAR^ nls! unaryExpression)*
    ;

// ++(prefix)/--(prefix)/+(unary)/-(unary)/$(GString expression) (level 2)
unaryExpression
    :   INC^ nls! unaryExpression
    |   DEC^ nls! unaryExpression
    |   MINUS^   {#MINUS.setType(UNARY_MINUS);}   nls! unaryExpression
    |   PLUS^    {#PLUS.setType(UNARY_PLUS);}     nls! unaryExpression
    |   unaryExpressionNotPlusMinus
    ;

// ~(BNOT)/!(LNOT)/(type casting) (level 1)
unaryExpressionNotPlusMinus
    :   BNOT^ nls! unaryExpression
    |   LNOT^ nls! unaryExpression
    |   (   // subrule allows option to shut off warnings
            options {
                    // "(int" ambig with postfixExpr due to lack of sequence
                    // info in linear approximate LL(k). It's ok. Shut up.
                    generateAmbigWarnings=false;
            }
        :   // If typecast is built in type, must be numeric operand
            // Have to backtrack to see if operator follows
            (LPAREN builtInTypeSpec[true] RPAREN unaryExpression)=>
            lpb:LPAREN^ {#lpb.setType(TYPECAST);} builtInTypeSpec[true] RPAREN!
            unaryExpression

            // Have to backtrack to see if operator follows. If no operator
            // follows, it's a typecast. No semantic checking needed to parse.
            // if it _looks_ like a cast, it _is_ a cast; else it's a "(expr)"
            // TODO:  Rework this mess for Groovy.
        |   (LPAREN classTypeSpec[true] RPAREN unaryExpressionNotPlusMinus)=>
            lp:LPAREN^ {#lp.setType(TYPECAST);} classTypeSpec[true] RPAREN!
            unaryExpressionNotPlusMinus

        |   postfixExpression
        )
    ;

// qualified names, array expressions, method invocation, post inc/dec
postfixExpression
    :
        pathExpression
        (
            options {greedy=true;} :
            // possibly add on a post-increment or post-decrement.
            // allows INC/DEC on too much, but semantics can check
            in:INC^ {#in.setType(POST_INC);}
        |   de:DEC^ {#de.setType(POST_DEC);}
        )?
    ;
    
// TODO:  Move pathExpression to this point in the file.

// the basic element of an expression
primaryExpression
    :   IDENT
        /*OBS*  //keywords can follow dot in Groovy; no need for this special case
        ( options {greedy=true;} : DOT^ "class" )?
        *OBS*/
    |   constant
    |   newExpression
    |   "this"
    |   "super"
    |   parenthesizedExpression             // (general stuff...)
    |   closureConstructorExpression
    |   listOrMapConstructorExpression
    |   stringConstructorExpression         // "foo $bar baz"; presented as multiple tokens
    |   scopeEscapeExpression               // $x
    |   builtInType
    /*OBS*  //class names work fine as expressions
            // look for int.class and int[].class
    |   bt:builtInType!
        declaratorBrackets[bt]
        DOT^ nls! "class"
    *OBS*/
    ;

parenthesizedExpression
    :   lp:LPAREN^ strictContextExpression RPAREN!
        { #lp.setType(EXPR); }
    ;

scopeEscapeExpression
    :   DOLLAR^  {#DOLLAR.setType(SCOPE_ESCAPE);} (IDENT | scopeEscapeExpression)
        // PROPOSE: The SCOPE_ESCAPE operator pops its operand out of the scope of a "with" block.
        // If not within a "with" block, it pops the operand out of the static global scope,
        // into whatever dynamic (unchecked) global scope is available when the script is run,
        // regardless of package and imports.
        // Example of SCOPE_ESCAPE:  def x=1; with ([x:2,y:-1]) { def y=3; println [$x, x, y] }  =>  "[1, 2, 3]"
    ;

/** Things that can show up as expressions, but only in strict
 *  contexts like inside parentheses, argument lists, and list constructors.
 */
strictContextExpression
    :   (declarationStart)=>
        singleDeclaration  // used for both binding and value, as: while (String xx = nextln()) { println xx }
    |   expression
    |   branchStatement // useful to embed inside expressions (cf. C++ throw)
    |   annotation      // creates an annotation value
    ;

closureConstructorExpression
    :   closedBlock
    ;

// Groovy syntax for "$x $y" or /$x $y/.
stringConstructorExpression
    :   cs:STRING_CTOR_START
        { #cs.setType(STRING_LITERAL); }

        stringConstructorValuePart

        (   cm:STRING_CTOR_MIDDLE
            { #cm.setType(STRING_LITERAL); }
            stringConstructorValuePart
        )*

        ce:STRING_CTOR_END
        { #ce.setType(STRING_LITERAL);
          #stringConstructorExpression =
            #(#[STRING_CONSTRUCTOR,"STRING_CONSTRUCTOR"], stringConstructorExpression);
        }
    ;

stringConstructorValuePart
    :
    (
        // PROPOSE: allow spread markers on string constructor arguments
        sp:STAR^                        {#sp.setType(SPREAD_ARG);}
    )?
    (   identifier
    |   openOrClosedBlock
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
    :   lcon:LBRACK^
        argList                 { hasLabels |= argListHasLabels;  }  // any argument label implies a map
        RBRACK!
        { #lcon.setType(hasLabels ? MAP_CONSTRUCTOR : LIST_CONSTRUCTOR); }
    |
        /* Special case:  [:] is an empty map constructor. */
        emcon:LBRACK^ COLON! RBRACK!   {#emcon.setType(MAP_CONSTRUCTOR);}
    ;

/*OBS*
/** Match a, a.b.c refs, a.b.c(...) refs, a.b.c[], a.b.c[].class,
 *  and a.b.c.class refs. Also this(...) and super(...). Match
 *  this or super.
 */
/*OBS*
identPrimary
    :   (ta1:typeArguments!)?
        IDENT
        // Syntax for method invocation with type arguments is
        // <String>foo("blah")
        (
            options {
                // .ident could match here or in postfixExpression.
                // We do want to match here. Turn off warning.
                greedy=true;
                // This turns the ambiguity warning of the second alternative
                // off. See below. (The "ANTLR_LOOP_EXIT" predicate makes it non-issue)
                warnWhenFollowAmbig=false;
            }
            // we have a new nondeterminism because of
            // typeArguments... only a syntactic predicate will help...
            // The problem is that this loop here conflicts with
            // DOT typeArguments "super" in postfixExpression (k=2)
            // A proper solution would require a lot of refactoring...
        :   (DOT (typeArguments)? IDENT) =>
            DOT^ (ta2:typeArguments!)? IDENT
        |   {ANTLR_LOOP_EXIT}?  //(see documentation above)
        )*
        (
            options {
                // ARRAY_DECLARATOR here conflicts with INDEX_OP in
                // postfixExpression on LBRACK RBRACK.
                // We want to match [] here, so greedy. This overcomes
                // limitation of linear approximate lookahead.
                greedy=true;
            }
        :   (   lp:LPAREN^ {#lp.setType(METHOD_CALL);}
                // if the input is valid, only the last IDENT may
                // have preceding typeArguments... rather hacky, this is...
                {if (#ta2 != null) astFactory.addASTChild(currentAST, #ta2);}
                {if (#ta2 == null) astFactory.addASTChild(currentAST, #ta1);}
                argList RPAREN!
            )
        |   (    options {greedy=true;} :
                lbc:LBRACK^ {#lbc.setType(ARRAY_DECLARATOR);} RBRACK!
            )+
        )?
    ;
*OBS*/

/** object instantiation.
 *  Trees are built as illustrated by the following input/tree pairs:
 *
 *  new T()
 *
 *  new
 *   |
 *   T --  ELIST
 *                 |
 *                arg1 -- arg2 -- .. -- argn
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
 *                                                                |
 *                                                              EXPR -- EXPR
 *                                                                |   |
 *                                                                1       2
 *
 *  new int[3]
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *                              |
 *                        EXPR
 *                              |
 *                              3
 *
 *  new int[1][2]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *                         |
 *               ARRAY_DECLARATOR -- EXPR
 *                         |                  |
 *                       EXPR                    1
 *                         |
 *                         2
 *
 */
newExpression
    :   "new"^ (typeArguments)? type
        (   mca:methodCallArgs[null]!
        
            {#newExpression.addChild(#mca.getFirstChild());}

            /*TODO - NYI* (anonymousInnerClassBlock)? *NYI*/

            //java 1.1
            // Note: This will allow bad constructs like
            //      new int[4][][3] {exp,exp}.
            //      There needs to be a semantic check here...
            // to make sure:
            //   a) [ expr ] and [ ] are not mixed
            //   b) [ expr ] and an init are not used together

        |   newArrayDeclarator //(arrayInitializer)?
            // Groovy does not support Java syntax for initialized new arrays.
            // Use sequence constructors instead.

        )
        // DECIDE:  Keep 'new x()' syntax?
    ;

/*NYI*
anonymousInnerClassBlock
    :   classBlock
    ;
*NYI*/

argList
    { boolean hl = false, hl2; }
    :   (
            hl=argument
            (
                options { greedy=true; }:
                COMMA! hl2=argument             { hl |= hl2; }
                // Note:  nls not needed, since we are inside parens,
                // and those insignificant newlines are suppressed by the lexer.
            )*
            {#argList = #(#[ELIST,"ELIST"], argList);}
        |   /*nothing*/
            {#argList = #[ELIST,"ELIST"];}
        )

        // DECIDE: Allow an extra trailing comma, for easy editing of long lists.
        // This applies uniformly to [x,y,] and (x,y,).  It is inspired by Java's a[] = {x,y,}.
        (   COMMA!  )?
        { argListHasLabels = hl; }  // return the value
    ;

/** A single argument in (...) or [...].  Corresponds to to a method or closure parameter.
 *  May be labeled.  May be modified by the spread operator '*' ('*:' for keywords).
 */
argument
returns [boolean hasLabel = false]
    :
        // Optional argument label.
        // Usage:  Specifies a map key, or a keyworded argument.
        (   (argumentLabelStart) =>
            argumentLabel c:COLON^          {#c.setType(LABELED_ARG);}

            {   hasLabel = true;  }  // signal to caller the presence of a label

        |   // Spread operator:  f(*[a,b,c])  ===  f(a,b,c);  f(1,*null,2)  ===  f(1,2).
            sp:STAR^                        {#sp.setType(SPREAD_ARG);}
            // spread maps are marked, as f(*:m) for f(a:x, b:y) if m==[a:x, b:y]
            (
                COLON!                      {#sp.setType(SPREAD_MAP_ARG);}
                { hasLabel = true; }  // signal to caller the presence of a label
            )?
        )?

        strictContextExpression
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
                (expression)?
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
    charVocabulary='\u0003'..'\uFFFF';
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
    protected static final int SCS_SQ_TYPE = 0, SCS_TQ_TYPE = 1, SCS_RE_TYPE = 2;
    protected int stringCtorState = 0;  // hack string and regexp constructor boundaries
    /** Push parenLevel here and reset whenever inside '{x}'. */
    protected ArrayList parenLevelStack = new ArrayList();
    protected int lastSigTokenType = EOF;  // last returned non-whitespace token

    protected void pushParenLevel() {
        parenLevelStack.add(new Integer(parenLevel*SCS_LIMIT + stringCtorState));
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
        case LITERAL_any:
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
        case LITERAL_with:
            return true;
        default:
            return false;
        }
    }

    protected void newlineCheck() throws RecognitionException {
        if (suppressNewline > 0) {
            suppressNewline = 0;
            require(suppressNewline == 0,
                "end of line reached within a simple string 'x' or \"x\"",
                "for multi-line literals, use triple quotes '''x''' or \"\"\"x\"\"\"");
        }
        newline();
    }
    
    protected boolean atValidDollarEscape() throws CharStreamException {
        // '$' (('*')? ('{' | LETTER)) =>
        int k = 1;
        char lc = LA(k++);
        if (lc != '$')  return false;
        lc = LA(k++);
        if (lc == '*')  lc = LA(k++);
        return (lc == '{' || (lc != '$' && Character.isJavaIdentifierStart(lc)));
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
                        default:  assert(false);
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
        Integer tt = new Integer(t.getType());
        Object ttn = ttypes.get(tt);
        if (ttn == null)  ttn = "<"+tt+">";
        return "["+ttn+",\""+t.getText()+"\"]";
    }

    protected GroovyRecognizer parser;  // little-used link; TODO: get rid of
    private void require(boolean z, String problem, String solution) throws SemanticException {
        // TODO: Direct to a common error handler, rather than through the parser.
        if (!z)  parser.requireFailed(problem, solution);
    }    
}

// TODO:  Borneo-style ops.

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
LNOT              options {paraphrase="'!'";}           :   '!'             ;
BNOT              options {paraphrase="'~'";}           :   '~'             ;
NOT_EQUAL         options {paraphrase="'!='";}          :   "!="            ;
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
DOLLAR            options {paraphrase="'$'";}           :   '$'             ;
RANGE_INCLUSIVE   options {paraphrase="'..'";}          :   ".."            ;
RANGE_EXCLUSIVE   options {paraphrase="'..<'";}         :   "..<"           ;
TRIPLE_DOT        options {paraphrase="'...'";}         :   "..."           ;
SPREAD_DOT        options {paraphrase="'*.'";}          :   "*."            ;
OPTIONAL_DOT      options {paraphrase="'?.'";}          :   "?."            ;
MEMBER_POINTER    options {paraphrase="'.&'";}          :   ".&"            ;
REGEX_FIND        options {paraphrase="'=~'";}          :   "=~"            ;
REGEX_MATCH       options {paraphrase="'==~'";}         :   "==~"           ;
STAR_STAR         options {paraphrase="'**'";}          :   "**"            ;
STAR_STAR_ASSIGN  options {paraphrase="'**='";}         :   "**="           ;
CLOSURE_OP        options {paraphrase="'->'";}          :   "->"            ;

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
        )+
        { if (!whitespaceIncluded)  _ttype = Token.SKIP; }
    ;

protected
ONE_NL!
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
            newlineCheck();
        }
    ;
        
// Group any number of newlines (with comments and whitespace) into a single token.
// This reduces the amount of parser lookahead required to parse around newlines.
// It is an invariant that the parser never sees NLS tokens back-to-back.
NLS
options {
    paraphrase="some newlines, whitespace or comments";
}
    :   ONE_NL
        (   {!whitespaceIncluded}?
            (ONE_NL | WS | SL_COMMENT | ML_COMMENT)+
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
        { if (!whitespaceIncluded)  $setType(Token.SKIP); }
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
            ~('\n'|'\r')
        )*
        { if (!whitespaceIncluded)  $setType(Token.SKIP); }
        //ONE_NL  //Never a significant newline, but might as well separate it.
    ;

// multiple-line comments
ML_COMMENT
options {
    paraphrase="a comment";
}
    :   "/*"
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
        |   '\r' '\n'               {newlineCheck();}
        |   '\r'                    {newlineCheck();}
        |   '\n'                    {newlineCheck();}
        |   ~('*'|'\n'|'\r')
        )*
        "*/"
        { if (!whitespaceIncluded)  $setType(Token.SKIP); }
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
     :  { if (LA(1) == EOF_CHAR) throw new MismatchedCharException(LA(1), EOF_CHAR, true, this);} 
       ~('"'|'\''|'\\'|'$'|'\n'|'\r')
    ;

REGEXP_LITERAL
options {
    paraphrase="a regular expression literal";
}
        {int tt=0;}
    :   {allowRegexpLiteral()}?
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

    |   DIV                 {$setType(DIV);}
    |   DIV_ASSIGN          {$setType(DIV_ASSIGN);}
    ;

protected
REGEXP_CTOR_END[boolean fromStart]
returns [int tt=STRING_CTOR_END]
options {
    paraphrase="a regular expression literal end";
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
REGEXP_SYMBOL
options {
    paraphrase="a regular expression character";
}
    :
        (
            ~('*'|'/'|'$'|'\\'|'\n'|'\r')
        |   '\\' ~('\n'|'\r')   // most backslashes are passed through unchanged
        |!  '\\' ONE_NL         { $setText('\n'); }     // always normalize to newline
        )
        ('*')*      // stars handled specially to avoid ambig. on /**/
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
    |!  '\\' ONE_NL
    //|!  ONE_NL          { $setText('\n'); }             // always normalize to newline
    ;

protected 
STRING_NL[boolean allowNewline]
options {
    paraphrase="a newline inside a string";
}
    :  {if (!allowNewline) throw new MismatchedCharException('\n', '\n', true, this); } 
       ONE_NL { $setText('\n'); }
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
// if it's a literal or really an identifer
IDENT
options {
    paraphrase="an identifier";
}
    //options {testLiterals=true;}  // Actually, this is done manually in the actions below.
    :   LETTER(LETTER|DIGIT)*
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
        /* The grammar allows a few keywords to follow dot.
         * TODO: Reinstate this logic if we change or remove keywordPropertyNames.
            if (ttype != IDENT && lastSigTokenType == DOT) {
                // A few keywords can follow a dot:
                switch (ttype) {
                case LITERAL_this: case LITERAL_super: case LITERAL_class:
                    break;
                default:
                    ttype = LITERAL_in;  // the poster child for bad dotted names
                }
            }
        */
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
    :   'a'..'z'|'A'..'Z'|'_'
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

// a numeric literal
NUM_INT
options {
    paraphrase="a numeric literal";
}
    {boolean isDecimal=false; Token t=null;}
    :
/*OBS*
        '.' {_ttype = DOT;}
        (
            (('0'..'9')+ (EXPONENT)? (f1:FLOAT_SUFFIX {t=f1;})?
            {
                if (t != null && t.getText().toUpperCase().indexOf('F')>=0) {
                    _ttype = NUM_FLOAT;
                }
                else {
                    _ttype = NUM_DOUBLE; // assume double
                }
            })
        |
            // JDK 1.5 token for variable length arguments
            (".." {_ttype = TRIPLE_DOT;})
        )?
    |
*OBS*/
        // TODO:  This complex pattern seems wrong.  Verify or fix.
        (   '0' {isDecimal = true;} // special case for just '0'
            (   ('x'|'X')
                {isDecimal = false;}
                (                                                                                   // hex
                    // the 'e'|'E' and float suffix stuff look
                    // like hex digits, hence the (...)+ doesn't
                    // know when to stop: ambig. ANTLR resolves
                    // it correctly by matching immediately. It
                    // is therefor ok to hush warning.
                    options {
                        warnWhenFollowAmbig=false;
                    }
                :   HEX_DIGIT
                )+

            |   //float or double with leading zero
                (('0'..'9')+ ('.'('0'..'9')|EXPONENT|FLOAT_SUFFIX)) => ('0'..'9')+

            |   ('0'..'7')+                                                                     // octal
                {isDecimal = false;}
            )?
        |   ('1'..'9') ('0'..'9')*  {isDecimal=true;}               // non-zero decimal
        )
        (   ('l'|'L') { _ttype = NUM_LONG; }
        |   ('i'|'I') { _ttype = NUM_INT; }
        |   BIG_SUFFIX { _ttype = NUM_BIG_INT; }

        // only check to see if it's a float if looks like decimal so far
        |
            (~'.' | '.' ('0'..'9')) =>
            {isDecimal}?
            (   '.' ('0'..'9')+ (EXPONENT)? (f2:FLOAT_SUFFIX {t=f2;} | g2:BIG_SUFFIX {t=g2;})?
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
    :   ('e'|'E') ('+'|'-')? ('0'..'9')+
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
// mode: java
// tab-width: 4
// indent-tabs-mode: nil
// End:
