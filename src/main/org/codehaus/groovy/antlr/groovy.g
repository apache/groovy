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
 *		John Mitchell		johnm@non.net
 *		Terence Parr		parrt@magelang.com
 *		John Lilley		jlilley@empathy.com
 *		Scott Stanchfield	thetick@magelang.com
 *		Markus Mohnen		mohnen@informatik.rwth-aachen.de
 *		Peter Williams		pete.williams@sun.com
 *		Allan Jacobs		Allan.Jacobs@eng.sun.com
 *		Steve Messick		messick@redhills.com
 *		James Strachan		jstrachan@protique.com
 *		John Pybus		john@pybus.org
 *		John Rose		rose00@mac.com
 *		Jeremy Rayner		groovy@ross-rayner.com
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *		fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *		added tree construction
 *		fixed definition of WS,comments for mac,pc,unix newlines
 *		added unary plus
 * Version 1.11 (Nov 20, 1998)
 *		Added "shutup" option to turn off last ambig warning.
 *		Fixed inner class def to allow named class defs as statements
 *		synchronized requires compound not simple statement
 *		add [] after builtInType DOT class in primaryExpression
 *		"const" is reserved but not valid..removed from modifiers
 * Version 1.12 (Feb 2, 1999)
 *		Changed LITERAL_xxx to xxx in tree grammar.
 *		Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *		Didn't have (stat)? for else clause in tree parser.
 *		Didn't gen ASTs for interface extends.  Updated tree parser too.
 *		Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *		Allowed final/abstract on local classes.
 *		Removed local interfaces from methods
 *		Put instanceof precedence where it belongs...in relationalExpr
 *			It also had expr not type as arg; fixed it.
 *		Missing ! on SEMI in classBlock
 *		fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *		fixed: didn't like Object[].class in parser or tree parser
 * Version 1.15 (Jun 26, 1999)
 *		Screwed up rule with instanceof in it. :(  Fixed.
 *		Tree parser didn't like (expr).something; fixed.
 *		Allowed multiple inheritance in tree grammar. oops.
 * Version 1.16 (August 22, 1999)
 *		Extending an interface built a wacky tree: had extra EXTENDS.
 *		Tree grammar didn't allow multiple superinterfaces.
 *		Tree grammar didn't allow empty var initializer: {}
 * Version 1.17 (October 12, 1999)
 *		ESC lexer rule allowed 399 max not 377 max.
 *		java.tree.g didn't handle the expression of synchronized
 *		statements.
 * Version 1.18 (August 12, 2001)
 *	  	Terence updated to Java 2 Version 1.3 by
 *		observing/combining work of Allan Jacobs and Steve
 *		Messick.  Handles 1.3 src.  Summary:
 *		o  primary didn't include boolean.class kind of thing
 *	  	o  constructor calls parsed explicitly now:
 * 		   see explicitConstructorInvocation
 *		o  add strictfp modifier
 *	  	o  missing objBlock after new expression in tree grammar
 *		o  merged local class definition alternatives, moved after declaration
 *		o  fixed problem with ClassName.super.field
 *	  	o  reordered some alternatives to make things more efficient
 *		o  long and double constants were not differentiated from int/float
 *		o  whitespace rule was inefficient: matched only one char
 *		o  add an examples directory with some nasty 1.3 cases
 *		o  made Main.java use buffered IO and a Reader for Unicode support
 *		o  supports UNICODE?
 *		   Using Unicode charVocabulay makes code file big, but only
 *		   in the bitsets at the end. I need to make ANTLR generate
 *		   unicode bitsets more efficiently.
 * Version 1.19 (April 25, 2002)
 *		Terence added in nice fixes by John Pybus concerning floating
 *		constants and problems with super() calls.  John did a nice
 *		reorg of the primary/postfix expression stuff to read better
 *		and makes f.g.super() parse properly (it was METHOD_CALL not
 *		a SUPER_CTOR_CALL).  Also:
 *
 *		o  "finally" clause was a root...made it a child of "try"
 *		o  Added stuff for asserts too for Java 1.4, but *commented out*
 *		   as it is not backward compatible.
 *
 * Version 1.20 (October 27, 2002)
 *
 *	  Terence ended up reorging John Pybus' stuff to
 *	  remove some nondeterminisms and some syntactic predicates.
 *	  Note that the grammar is stricter now; e.g., this(...) must
 *	be the first statement.
 *
 *	  Trinary ?: operator wasn't working as array name:
 *		  (isBig ? bigDigits : digits)[i];
 *
 *	  Checked parser/tree parser on source for
 *		  Resin-2.0.5, jive-2.1.1, jdk 1.3.1, Lucene, antlr 2.7.2a4,
 *		and the 110k-line jGuru server source.
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
 *	  Changes by Matt Quail to support generics (as per JDK1.5/JSR14)
 *	  Notes:
 *	  o We only allow the "extends" keyword and not the "implements"
 *		keyword, since thats what JSR14 seems to imply.
 *	  o Thanks to Monty Zukowski for his help on the antlr-interest
 *		mail list.
 *	  o Thanks to Alan Eliasen for testing the grammar over his
 *		Fink source base
 *
 * Version 1.22 (July, 2004)
 *	  Changes by Michael Studman to support Java 1.5 language extensions
 *	  Notes:
 *	  o Added support for annotations types
 *	  o Finished off Matt Quail's generics enhancements to support bound type arguments
 *	  o Added support for new for statement syntax
 *	  o Added support for static import syntax
 *	  o Added support for enum types
 *	  o Tested against JDK 1.5 source base and source base of jdigraph project
 *	  o Thanks to Matt Quail for doing the hard part by doing most of the generics work
 *
 * Version 1.22.1 (July 28, 2004)
 *	  Bug/omission fixes for Java 1.5 language support
 *	  o Fixed tree structure bug with classOrInterface - thanks to Pieter Vangorpto for
 *		spotting this
 *	  o Fixed bug where incorrect handling of SR and BSR tokens would cause type
 *		parameters to be recognised as type arguments.
 *	  o Enabled type parameters on constructors, annotations on enum constants
 *		and package definitions
 *	  o Fixed problems when parsing if ((char.class.equals(c))) {} - solution by Matt Quail at Cenqua
 *
 * Version 1.22.2 (July 28, 2004)
 *	  Slight refactoring of Java 1.5 language support
 *	  o Refactored for/"foreach" productions so that original literal "for" literal
 *	    is still used but the for sub-clauses vary by token type
 *	  o Fixed bug where type parameter was not included in generic constructor's branch of AST
 *
 * Version 1.22.3 (August 26, 2004)
 *	  Bug fixes as identified by Michael Stahl; clean up of tabs/spaces
 *        and other refactorings
 *	  o Fixed typeParameters omission in identPrimary and newStatement
 *	  o Replaced GT reconcilliation code with simple semantic predicate
 *	  o Adapted enum/assert keyword checking support from Michael Stahl's java15 grammar
 *	  o Refactored typeDefinition production and field productions to reduce duplication
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
	k = 3;							// three token lookahead
	exportVocab=Groovy;				// Call its vocabulary "Groovy"
	codeGenMakeSwitchThreshold = 2;	// Some optimizations
	codeGenBitsetTestThreshold = 3;
	defaultErrorHandler = false;	// Don't generate parser error handlers
	buildAST = true;
//	ASTLabelType = "GroovyAST"; 
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
CLOSED_BLOCK; IMPLICIT_PARAMETERS; DEF="def";
SELECT_SLOT; REFLECT_MEMBER; DYNAMIC_MEMBER;
LABELED_ARG; SPREAD_ARG; OPTIONAL_ARG; SCOPE_ESCAPE;
LIST_CONSTRUCTOR; MAP_CONSTRUCTOR;
FOR_IN_ITERABLE; RANGE_EXCLUSIVE;
	STATIC_IMPORT; ENUM_DEF; ENUM_CONSTANT_DEF; FOR_EACH_CLAUSE; ANNOTATION_DEF; ANNOTATIONS;
	ANNOTATION; ANNOTATION_MEMBER_VALUE_PAIR; ANNOTATION_FIELD_DEF; ANNOTATION_ARRAY_INIT;
	TYPE_ARGUMENTS; TYPE_ARGUMENT; TYPE_PARAMETERS; TYPE_PARAMETER; WILDCARD_TYPE;
	TYPE_UPPER_BOUNDS; TYPE_LOWER_BOUNDS;
}

{
	/** This factory is the correct way to wire together a Groovy parser and lexer. */
	public static GroovyRecognizer make(GroovyLexer lexer) {
		GroovyRecognizer parser = new GroovyRecognizer(lexer.plumb());
		// TO DO: set up a common error-handling control block, to avoid excessive tangle between these guys
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

	List warningList;
	public List getWarningList() { return warningList; }

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
		// TO DO: Needs more work.
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


/**
	 * Counts the number of LT seen in the typeArguments production.
	 * It is used in semantic predicates to ensure we have seen
	 * enough closing '>' characters; which actually may have been
	 * either GT, SR or BSR tokens.
	 */
	private int ltCounter = 0;
}

// Compilation Unit: In Groovy, this is a single file or script. This is the start
// rule for this parser
compilationUnit
	:	
		// we can have comments at the top of a file
		nls!
		// A compilation unit starts with an optional package definition
		(	(annotations "package")=> packageDefinition
                // The main part of the script is a sequence of any number of statements.
                // Semicolons and/or significant newlines serve as separators.
                ( sep! (statement)? )*
                EOF!
		|   (statement)? ( sep! (statement)? )*
            EOF!
		)

		// Next we have a series of zero or more import statements
		// TODO REMOVE ( importDefinition )*

		// Wrapping things up with any number of class or interface
		// definitions
		// TODO REMOVE ( typeDefinition )*

		EOF!
	;

/** A Groovy script or simple expression.  Can be anything legal inside {...}. */
snippetUnit
    :       blockBody
    ;


// Package statement: optional annotations followed by "package" then the package identifier.
packageDefinition
	//TODO? options {defaultErrorHandler = true;} // let ANTLR handle errors
	:	annotations p:"package"^ {#p.setType(PACKAGE_DEF);} identifier
	;


// Import statement: import followed by a package or class name
importStatement
	//TODO? options {defaultErrorHandler = true;}
	{ boolean isStatic = false; }
	:	i:"import"^ {#i.setType(IMPORT);} ( "static"! {#i.setType(STATIC_IMPORT);} )? identifierStar
	;

// TODO REMOVE
// A type definition is either a class, interface, enum or annotation with possible additional semis.
//typeDefinition
//	options {defaultErrorHandler = true;}
//	:	m:modifiers!
//		typeDefinitionInternal[#m]
//	|	SEMI!
//	;

// Added this production, even though 'typeDefinition' seems to be obsolete,
// as this is referenced by many other parts of the grammar.
// Protected type definitions production for reuse in other productions
protected typeDefinitionInternal[AST mods]
	:	cd:classDefinition[#mods]		// inner class
	  {#typeDefinitionInternal = #cd;}
	|	id:interfaceDefinition[#mods]	// inner interface
	  {#typeDefinitionInternal = #id;}
	|	ed:enumDefinition[#mods]		// inner enum
	  {#typeDefinitionInternal = #ed;}
	|	ad:annotationDefinition[#mods]	// inner annotation
	  {#typeDefinitionInternal = #ad;}
	;

/** A declaration is the creation of a reference or primitive-type variable,
 *  or (if arguments are present) of a method.
 *  Generically, this is called a 'variable' definition, even in the case of a class field or method.
 *  It may start with the modifiers and a mandatory keyword "def".
 *  It may also start with the modifiers and a capitalized type name.
 *  <p>
 *  AST effect: Create a separate Type/Var tree for each var in the var list.
 *  Must be guarded, as in (declarationStart) => declaration.
 */
declaration!
      : 
      						// method/variable using def
            (m:modifiers)?
            
            (
        						DEF! nls! v:variableDefinitions[#m, null]	
        						{#declaration = #v;}
      						
	        				|

  	    						 // method/variable using a type
 	      						t2:typeSpec[false] v2:variableDefinitions[#m,#t2]
  		          {#declaration = #v2;}
  		        )
      ; 

/** A declaration with one declarator and no initialization, like a parameterDeclaration.

*TODO* We must also audit the various occurrences of warning
suppressions like "options { greedy = true; }".
*/

singleDeclarationNoInit!
:      						// method/variable using def
            (m:modifiers)?
            
            (
        						DEF! nls! v:singleVariable[#m, null]	
        						{#singleDeclarationNoInit = #v;}
      						
	        				|

  	    						 // method/variable using a type
 	      						t2:typeSpec[false] v2:singleVariable[#m,#t2]
  		          {#singleDeclarationNoInit = #v2;}
  		        )
    ;

singleDeclaration
    :   sd:singleDeclarationNoInit!
        { #singleDeclaration = #sd; }
                varInitializer
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
 *  just put a TO DO comment in.
 *   
 *  *TODO* possibly need to eliminate @interface from matching the current AT! alternation
 */
declarationStart!
    :   DEF!
    |   modifier!
    |   AT!
    |   (   upperCaseIdent!
        |   builtInType!
        ) (LBRACK balancedTokens! RBRACK)* IDENT
					;        

/** Used to look ahead for a constructor 
 */
constructorStart!:
				(modifier!)* id:IDENT! {isConstructorIdent(id)}? nls! LPAREN! balancedTokens! RPAREN!
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
    :   (modifier!)* ("class" | "interface" | "enum" | AT )
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
	: classTypeSpec[addImagNode]
	|	builtInTypeSpec[addImagNode]
	;

//TODO - URGENT - check that 'arrayOrTypeArgs' is ok instead of 'ARRAY_DECLARATOR'
// also check that 'classOrInterfaceType[false]' is a suitable substitution for 'identifier'

// A class type specification is a class type with either:
// - possible brackets afterwards
//   (which would make it an array type).
// - generic type arguments after
classTypeSpec[boolean addImagNode]
	:	t:classOrInterfaceType[false]
 	(options{greedy=true;}: // match as many as possible
	 		lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!
		 )*
		{
	if ( addImagNode ) {
				#classTypeSpec = #(#[TYPE,"TYPE"], #classTypeSpec);
			}
		}
	;

// A non-built in type name, with possible type parameters
classOrInterfaceType[boolean addImagNode]
	:	IDENT^ (typeArguments)?
		(options{greedy=true;}: // match as many as possible
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
	:	classTypeSpec[true]
	|	builtInTypeArraySpec[true]
	;

// A generic type argument is a class type, a possibly bounded wildcard type or a built-in type array
typeArgument
	:	(	typeArgumentSpec
		|	wildcardType
		)
		{#typeArgument = #(#[TYPE_ARGUMENT,"TYPE_ARGUMENT"], #typeArgument);}
	;

// Wildcard type indicating all types (with possible constraint)
wildcardType
	:	q:QUESTION^ {#q.setType(WILDCARD_TYPE);}
		(("extends" | "super")=> typeArgumentBounds)?
	;

// Type arguments to a class or interface type
typeArguments
{int currentLtLevel = 0;}
	:
		{currentLtLevel = ltCounter;}
		LT! {ltCounter++;}
		typeArgument
		(options{greedy=true;}: // match as many as possible
			{inputState.guessing !=0 || ltCounter == currentLtLevel + 1}?
			COMMA! typeArgument
		)*

		(	// turn warning off since Antlr generates the right code,
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
	:	GT! {ltCounter-=1;}
	|	SR! {ltCounter-=2;}
	|	BSR! {ltCounter-=3;}
	;

// Restriction on wildcard types based on super class or derrived class
typeArgumentBounds
	{boolean isUpperBounds = false;}
	:
		( "extends"! {isUpperBounds=true;} | "super"! ) classOrInterfaceType[false]
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

//TODO - check that arrayOrTypeArgs is suitable here
// A builtin type array specification is a builtin type with brackets afterwards
builtInTypeArraySpec[boolean addImagNode]
	:	t:builtInType
	(ata:arrayOrTypeArgs[#t])?
		{
			if (#ata != null)  #builtInTypeArraySpec = #ata;
			if ( addImagNode ) {
				#builtInTypeArraySpec = #(#[TYPE,"TYPE"], #builtInTypeArraySpec);
			}
		}
	;

//TODO - check that arrayOrTypeArgs is suitable here
// A builtin type specification is a builtin type with possible brackets
// afterwards (which would make it an array type).
builtInTypeSpec[boolean addImagNode]
	:	t:builtInType
	 	(options{greedy=true;}: // match as many as possible
	 		lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!
		 )*
		{
			if ( addImagNode ) {
				#builtInTypeSpec = #(#[TYPE,"TYPE"], #builtInTypeSpec);
			}
		}
	;

// A type name. which is either a (possibly qualified and parameterized)
// class name or a primitive (builtin) type
type
	:	classOrInterfaceType[false]
	|	builtInType
	;

// The primitive types.
builtInType
	:	"void"
	|	"boolean"
	|	"byte"
	|	"char"
	|	"short"
	|	"int"
	|	"float"
	|	"long"
	|	"double"
    |   "any"
    // PROPOSE:  Add "list", "map", "closure"??
	;

// A (possibly-qualified) java identifier. We start with the first IDENT
// and expand its name by adding dots and following IDENTS
identifier
    :       IDENT
        (   options { greedy = true; } :
                        DOT^ nls! IDENT )*
    ;

identifierStar
    :       IDENT
        (   options { greedy = true; } :
                        DOT^  nls! IDENT )*
        (   DOT^  nls! STAR
        |   "as"^ nls! IDENT
        )?
    ;

// A list of zero or more modifiers. We could have used (modifier)* in
// place of a call to modifiers, but I thought it was a good idea to keep
// this rule separate so they can easily be collected in a Vector if
// someone so desires
modifiers
	:
		(
			//hush warnings since the semantic check for "@interface" solves the non-determinism
			options{generateAmbigWarnings=false;}:

			modifier nls!
			|
			//Semantic check that we aren't matching @interface as this is not an annotation
			//A nicer way to do this would be nice
			{LA(1)==AT && !LT(2).getText().equals("interface")}? annotation nls!
        )+

		{#modifiers = #([MODIFIERS, "MODIFIERS"], #modifiers);}
	;

// modifiers for Java classes, interfaces, class/instance vars and methods
modifier
	:	"private"
	|	"public"
	|	"protected"
	|	"static"
	|	"transient"
	|	"final"
	|	"abstract"
	|	"native"
	|	"threadsafe"
	|	"synchronized"
	|	"volatile"
	|	"strictfp"
	;

annotation!
	:	AT! i:identifier ( LPAREN! ( args:annotationArguments )? RPAREN! )?
		{#annotation = #(#[ANNOTATION,"ANNOTATION"], i, args);}
	;

annotations
    :   (annotation nls!)*
		{#annotations = #([ANNOTATIONS, "ANNOTATIONS"], #annotations);}
    ;

annotationArguments
	:	annotationMemberValueInitializer | anntotationMemberValuePairs
	;

anntotationMemberValuePairs
	:	annotationMemberValuePair ( COMMA! annotationMemberValuePair )*
	;

annotationMemberValuePair!
	:	i:IDENT ASSIGN! v:annotationMemberValueInitializer
		{#annotationMemberValuePair = #(#[ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR"], i, v);}
	;

annotationMemberValueInitializer
	:
		conditionalExpression | annotation nls!| annotationMemberArrayInitializer
	;

// This is an initializer used to set up an annotation member array.
annotationMemberArrayInitializer
	:	lc:LCURLY^ {#lc.setType(ANNOTATION_ARRAY_INIT);}
			(	annotationMemberArrayValueInitializer
				(
					// CONFLICT: does a COMMA after an initializer start a new
					// initializer or start the option ',' at end?
					// ANTLR generates proper code by matching
					// the comma as soon as possible.
					options {
						warnWhenFollowAmbig = false;
					}
				:
					COMMA! annotationMemberArrayValueInitializer
				)*
				(COMMA!)?
			)?
		RCURLY!
	;

// The two things that can initialize an annotation array element are a conditional expression
// and an annotation (nested annotation array initialisers are not valid)
annotationMemberArrayValueInitializer
	:	conditionalExpression
	|	annotation nls!
	;

superClassClause!
	:	( "extends" c:classOrInterfaceType[false] )?
		{#superClassClause = #(#[EXTENDS_CLAUSE,"EXTENDS_CLAUSE"],c);}
	;

// Definition of a Java class
classDefinition![AST modifiers]
        { AST prevCurrentClass = currentClass; }
	:	"class" IDENT nls!
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
	:	"interface" IDENT nls!
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
	:	"enum" IDENT
		// it might implement some interfaces...
		ic:implementsClause
		// now parse the body of the enum
		eb:enumBlock
		{#enumDefinition = #(#[ENUM_DEF,"ENUM_DEF"],
								modifiers,IDENT,ic,eb);}
	;

annotationDefinition![AST modifiers]
	:	AT "interface" IDENT
		// now parse the body of the annotation
		ab:annotationBlock
		{#annotationDefinition = #(#[ANNOTATION_DEF,"ANNOTATION_DEF"],
									modifiers,IDENT,ab);}
	;

typeParameters
{int currentLtLevel = 0;}
	:
		{currentLtLevel = ltCounter;}
		LT! {ltCounter++;}
		typeParameter (COMMA! typeParameter)*
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
		"extends"! classOrInterfaceType[false]
		(BAND! classOrInterfaceType[false])*
		{#typeParameterBounds = #(#[TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS"], #typeParameterBounds);}
	;

// This is the body of a class. You can have classFields and extra semicolons.
classBlock
	:	LCURLY!
            ( classField )? ( sep! ( classField )? )*
		RCURLY!
		{#classBlock = #([OBJBLOCK, "OBJBLOCK"], #classBlock);}
	;

// This is the body of an interface. You can have interfaceField and extra semicolons.
interfaceBlock
	:	LCURLY!
            ( interfaceField )? ( sep! ( interfaceField )? )*
		RCURLY!
		{#interfaceBlock = #([OBJBLOCK, "OBJBLOCK"], #interfaceBlock);}
	;
	
// This is the body of an annotation. You can have annotation fields and extra semicolons,
// That's about it (until you see what an annoation field is...)
annotationBlock
	:	LCURLY!
            ( annotationField )? ( sep! ( annotationField )? )*
		RCURLY!
		{#annotationBlock = #([OBJBLOCK, "OBJBLOCK"], #annotationBlock);}
	;

// This is the body of an enum. You can have zero or more enum constants
// followed by any number of fields like a regular class
enumBlock
	:	LCURLY!
			( enumConstant ( options{greedy=true;}: COMMA! enumConstant )* ( COMMA! )? )?
			( SEMI! ( classField | SEMI! )* )?
		RCURLY!
		{#enumBlock = #([OBJBLOCK, "OBJBLOCK"], #enumBlock);}
	;

// An annotation field
annotationField!
	:	mods:modifiers
		(	td:typeDefinitionInternal[#mods]
			{#annotationField = #td;}
		|	t:typeSpec[false]		// annotation field
			(	i:IDENT				// the name of the field

				LPAREN! RPAREN!

				/*OBS* rt:declaratorBrackets[#t] *OBS*/

				( "default" amvi:annotationMemberValueInitializer )?

				SEMI
				{#annotationField =
					#(#[ANNOTATION_FIELD_DEF,"ANNOTATION_FIELD_DEF"],
						 mods,
						 #(#[TYPE,"TYPE"],t),
						 i,amvi
						 );}
			|	v:variableDefinitions[#mods,#t] SEMI	// variable
				{#annotationField = #v;}
			)
		)
	;

//An enum constant may have optional parameters and may have a
//a class body
enumConstant!
	        {boolean zz; /*ignored*/ }
	:	an:annotations
		i:IDENT
		(	LPAREN!
			zz=a:argList
			RPAREN!
		)?
		( b:enumConstantBlock )?
		{#enumConstant = #([ENUM_CONSTANT_DEF, "ENUM_CONSTANT_DEF"], an, i, a, b);}
	;

//The class-like body of an enum constant
enumConstantBlock
	:	LCURLY!
		( enumConstantField | SEMI! )*
		RCURLY!
		{#enumConstantBlock = #([OBJBLOCK, "OBJBLOCK"], #enumConstantBlock);}
	;

//An enum constant field is just like a class field but without
//the posibility of a constructor definition or a static initializer

// TODO - maybe allow 'declaration' production within this production, 
// but how to disallow constructors and static initializers...
enumConstantField!
	:	mods:modifiers
		(	td:typeDefinitionInternal[#mods]
			{#enumConstantField = #td;}
	|	// A generic method has the typeParameters before the return type.
			// This is not allowed for variable definitions, but this production
			// allows it, a semantic check could be used if you wanted.
			(tp:typeParameters)? t:typeSpec[false]		// method or variable declaration(s)
			(	IDENT									// the name of the method

				// parse the formal parameter declarations.
				LPAREN! param:parameterDeclarationList RPAREN!

	/*OBS*			rt:declaratorBrackets[#t] *OBS*/

				// get the list of exceptions that this method is
				// declared to throw
				(tc:throwsClause)?

				( s2:compoundStatement | SEMI )
			 // TODO - verify that 't' is useful/correct here, used to be 'rt'
				{#enumConstantField = #(#[METHOD_DEF,"METHOD_DEF"],
							 mods,
							 tp,
							 #(#[TYPE,"TYPE"],t),
							 IDENT,
							 param,
							 tc,
							 s2);}
			
			 |	v:variableDefinitions[#mods,#t] SEMI
				{#enumConstantField = #v;}
			)
		)

	// "{ ... }" instance initializer
	|	s4:compoundStatement
		{#enumConstantField = #(#[INSTANCE_INIT,"INSTANCE_INIT"], s4);}
	;

// An interface can extend several other interfaces...
interfaceExtends
	:	(
		e:"extends"! nls!
		classOrInterfaceType[false] ( COMMA! nls! classOrInterfaceType[false] )* nls!
		)?
		{#interfaceExtends = #(#[EXTENDS_CLAUSE,"EXTENDS_CLAUSE"],
								#interfaceExtends);}
	;

// A class can implement several interfaces...
implementsClause
	:	(
			i:"implements"! nls! classOrInterfaceType[false] ( COMMA! nls! classOrInterfaceType[false] )* nls!
		)?
		{#implementsClause = #(#[IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE"],
								 #implementsClause);}
	;

// Now the various things that can be defined inside a class
classField!
	:	// method, constructor, or variable declaration
	
 (constructorStart)=>
     	 	(mc:modifiers)? ctor:constructorDefinition[#mc]
      	 {#classField = #ctor;}

	    |
	
		(declarationStart)=>
        d:declaration
        {#classField = #d;}
    |
        //TODO - unify typeDeclaration and typeDefinitionInternal names
        // type declaration
        (typeDeclarationStart)=>
        ( mods:modifiers )?
		(	td:typeDefinitionInternal[#mods]
			{#classField = #td;}
		)

	// "static { ... }" class initializer
	|	"static" s3:compoundStatement
		{#classField = #(#[STATIC_INIT,"STATIC_INIT"], s3);}

	// "{ ... }" instance initializer
	|	s4:compoundStatement
		{#classField = #(#[INSTANCE_INIT,"INSTANCE_INIT"], s4);}
	;

// Now the various things that can be defined inside a interface
interfaceField!
	:	// method, constructor, or variable declaration
        (declarationStart)=>
        d:declaration
        {#interfaceField = #d;}
    |
        //TODO - unify typeDeclaration and typeDefinitionInternal names
        // type declaration
        (typeDeclarationStart)=>
        ( mods:modifiers )?

		(	td:typeDefinitionInternal[#mods]
			{#interfaceField = #td;}
		)
	;

constructorBody
	:	lc:LCURLY^ {#lc.setType(SLIST);} nls!
        (   (explicitConstructorInvocation) =>   // Java compatibility hack
                explicitConstructorInvocation (sep! blockBody)?
            |   blockBody
        )
        RCURLY!
    ;


/** Catch obvious constructor calls, but not the expr.super(...) calls */
explicitConstructorInvocation
        {boolean zz; /*ignored*/ }
	:	(typeArguments)?
		(	"this"! lp1:LPAREN^ zz=argList RPAREN!
			{#lp1.setType(CTOR_CALL);}
		|	"super"! lp2:LPAREN^ zz=argList RPAREN!
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
    :       variableDeclarator[getASTFactory().dupTree(mods),
                                                   getASTFactory().dupTree(t)]
        (       COMMA! nls!
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
        (       tc:throwsClause!  )? nlsWarn!

                // the method body is an open block
                // but, it may have an optional constructor call (for constructors only)
                // this constructor clause is only used for constructors using 'def'
                // which look like method declarations
        (
                mb:openBlock!
        |   /*or nothing at all*/
        )
        {               if (#qid != null)  #id = #qid;
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
        (       tc:throwsClause!  )? nlsWarn!

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
                v:varInitializer
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

/*OBS*
declaratorBrackets[AST typ]
	:	{#declaratorBrackets=typ;}
		(lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!)*
	;
*OBS*/

varInitializer
	:	( ASSIGN^ nls! initializer )?
	;

/*OBS*
// This is an initializer used to set up an array.
arrayInitializer
	:	lc:LCURLY^ {#lc.setType(ARRAY_INIT);}
			(	initializer
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
	:	expression
/*OBS*  // Use [...] for initializing all sorts of sequences, including arrays.
	|	arrayInitializer
*OBS*/
	;

/*OBS???
// This is the header of a method. It includes the name and parameters
// for the method.
// This also watches for a list of exception classes in a "throws" clause.
ctorHead
	:	IDENT // the name of the method

		// parse the formal parameter declarations.
		LPAREN! parameterDeclarationList RPAREN!

		// get the list of exceptions that this method is declared to throw
		(throwsClause)?
	;
*OBS*/

// This is a list of exception classes that the method is declared to throw
throwsClause
	:	"throws"^ nls! identifier ( COMMA! nls! identifier )* nls!
	;

// A list of formal parameters
//	 Zero or more parameters
//	 If a parameter is variable length (e.g. String... myArg) it is the right-most parameter
parameterDeclarationList
	// The semantic check in ( .... )* block is flagged as superfluous, and seems superfluous but
	// is the only way I could make this work. If my understanding is correct this is a known bug
	:	(	( parameterDeclaration )=> parameterDeclaration
			( options {warnWhenFollowAmbig=false;} : COMMA! nls! parameterDeclaration )*
			( COMMA! nls! variableLengthParameterDeclaration )?
		|
			variableLengthParameterDeclaration
		)?
		{#parameterDeclarationList = #(#[PARAMETERS,"PARAMETERS"],
									#parameterDeclarationList);}
	;

// A formal parameter.
parameterDeclaration!
        :       ("def"!)?  // useless but permitted for symmetry
                pm:parameterModifier ( options {greedy=true;} : t:typeSpec[false])? id:parameterIdent
                
                // allow an optional default value expression
                ( ASSIGN exp:expression )?
                
	                /*OBS*pd:declaratorBrackets[#t]*/
                {#parameterDeclaration = #(#[PARAMETER_DEF,"PARAMETER_DEF"],
                                                                        pm, #([TYPE,"TYPE"],t), id, exp);}
        ;

// TODO - possibly add nls! somewhere within variableLengthParameterDeclaration
variableLengthParameterDeclaration!
	:	pm:parameterModifier t:typeSpec[false] TRIPLE_DOT! id:IDENT
	
		/*OBS* pd:declaratorBrackets[#t]*/
		{#variableLengthParameterDeclaration = #(#[VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF"],
												pm, #([TYPE,"TYPE"],t), id);}
	;

parameterModifier
	//final can appear amongst annotations in any order - greedily consume any preceding
	//annotations to shut nond-eterminism warnings off
	:	(options{greedy=true;} : annotation nls!)* (f:"final")? (annotation nls!)*
		{#parameterModifier = #(#[MODIFIERS,"MODIFIERS"], #parameterModifier);}
	;

/** Closure parameters are exactly like method parameters,
  * except that they are enclosed in vertical bars instead of parentheses.
  * The first vertical bar is optional if the parameters are a simple list
  * of one or more names, with no additional syntax.
  * (An empty argument list must be spelled with two bars, <code>{|| ...}</code>)
  */
closureParameters
        : (BOR! nls! BOR!)=> 
           BOR! nls! BOR!
									| (BOR! nls! (parameterDeclarationList|LPAREN))=> 
									   BOR! nls! 
        							(parameterDeclarationList | (LPAREN! nls! parameterDeclarationList nls! RPAREN!))
        							nls! BOR!
        |   LPAREN! nls! parameterDeclarationList nls! RPAREN!	nls! BOR!
                // Yes, you can have a full parameter declaration list.
                // they can be wrapped in parens to allow complex expressions
                
									| LOR!
																	// allow empty double bar if folks wanna explicitly mean a zero parameter closure
																	
        |   (closureParameter (COMMA! nls! closureParameter)* nls!)? BOR!
                {#closureParameters = #(#[PARAMETERS,"PARAMETERS"], #closureParameters);}
        ;

/** Lookahead for closureParameters. */
closureParametersStart!
        :   BOR
        |   LOR // for empty parameter declaration
        |   parameterIdent! nls! (BOR | COMMA)
        |   LPAREN balancedTokens! RPAREN nls! BOR
        ;

/** Simple names, as in {x|...}, are completely equivalent to {(def x)|...}.  Build the right AST. */
closureParameter!
        :   id:parameterIdent!
                {#closureParameter = #(#[PARAMETER_DEF,"PARAMETER_DEF"],
                                                                #(#[MODIFIERS,"MODIFIERS"]), #([TYPE,"TYPE"]),
                                                                id);}
        ;

/** A formal parameter name can be decorated with the optionality operator, meaning that
 *  the argument can be omitted by the caller.
 *  (This has the same effect as creating a new wrapper overloading that
 *  passes the appropriate null value in place of the missing argument.)
 *  <p>
 *  A formal parameter name can be decorated with a spread operator f(*x),
 *  which means the name will be bound to a list of argument values.
 *  This spread argument must be the last argument, except perhaps for
 *  a Map argument (which may also be spread), and except perhaps for
 *  a final Closure argument.  The Map and Closure parameters must be
 *  explicitly typed, in order to provide a clear basis for dividing
 *  incoming arguments into unlabeled, labeled, and closure categories.
 *  <p>
 *  Examples:
 *    {(*al) | al}(0,1,2)  ===  [0,1,2]
 *    {(*al) | al}(0,a:1,b:2)  ===  [0,[a:1,b:2]]
 *    {(*al) | al}(0){s}  ===  [0,{s}]
 *    {(*al,Closure c) | al}(0){s}  ===  [0]
 *    {(*al,Map m) | al}(0,a:1,b:2)  ===  [0]
 *    {(*al,Map m) | m}(0)  ===  [:]
 */
parameterIdent
        :   (   // Spread operator:  {(*y)|y}(1,2,3)  ===  [1,2,3]
                        sp:STAR^                                {#sp.setType(SPREAD_ARG);}
                |   // Optional-null operator:  {(?x,?y)|[x,y]}(1)  ===  [1,null]
                        op:QUESTION^                    {#op.setType(OPTIONAL_ARG);}
                )?
                IDENT
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
	:	openBlock
	;

/** An open block is not allowed to have closure arguments. */
openBlock
        :       lc:LCURLY^ {#lc.setType(SLIST);}   // AST type of SLIST means "never gonna be a closure"
                blockBody
                RCURLY!
        ;

/** A block body is either a single expression, with no additional newlines or separators,
  * or else the usual parade of zero or more statements.
  */
blockBody
        :   // Allow almost any expression, as long as it stands completely alone in the block.
                (nls balancedTokensNoSep nls (RCURLY | EOF)) =>
                (nls assignmentExpression nls (RCURLY | EOF)) =>
                nls! expressionNotBOR nls!

        |       // include the (possibly-empty) list of statements
                nls! (statement)? (sep! (statement)?)*
        ;

// Special restriction:  Logical OR expressions cannot occur at statement level, not even as {a...|b...}.
// This restriction helps programmers avoid inadvertantly creating closure arguments.
// Note pernicious case #1:  {it|1}.  Does this mean set the low-order bit of the argument, or return constant 1?
// Note pernicious case #2:  {xx|1}.  Does this mean add the low-order bit into xx, or return constant 1?
// In both cases, the bar is "eagerly" taken to be a closure argument delimiter.
expressionNotBOR
        :   e:expression
                {require(#e.getType() != BOR,
                                "expression in block; cannot be of the form a|b",
                                "enclose the expression parentheses (a|b)");}
        ;

/** A block which is known to be a closure, even if it has no apparent arguments.
 */
closedBlock
        :       lc:LCURLY^ {#lc.setType(CLOSED_BLOCK);}
                ( ( nls closureParametersStart ) =>
                        nls closureParameters
                |
                        implicitParameters  // implicit {it|...} or {?noname|...}
                )
                blockBody
                RCURLY!
        ;

/** A block inside an expression is always assumed to be a closure.
 *  Only blocks which occur directly as substatements are kept open.
 */
expressionBlock
    	:   closedBlock
    	;

/** An appended block follows a method name or method argument list.
 *  It is optionally labeled.  DECIDE:  A good rule?
 */
appendedBlock
        :
        /*
            (IDENT COLON nls LCURLY)=>
                IDENT c:COLON^ {#c.setType(LABELED_ARG);} nls!
                expressionBlock
        |
        */
                expressionBlock
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
        :   (LCURLY nls closureParametersStart ) =>
                closedBlock
        |       openBlock
        ;

statement
	// A list of statements in curly braces -- start a new scope!
	// Free-floating blocks must be labeled, to defend against typos.
        :       compoundStatement
		{require(false,
                                "ambiguous free-floating block head{...} needs context to determine if it's open or closed",
                                "surround {...} with extra braces {{...}} or use it as a closure in an expression x={...}");}

	// declarations are ambiguous with "ID DOT" relative to expression
	// statements. Must backtrack to be sure. Could use a semantic
	// predicate to test symbol table to see what the type was coming
	// up, but that's pretty hard without a symbol table ;)
	|	(declarationStart)=> 
                declaration

	// An expression statement. This could be a method call,
	// assignment statement, or any other expression evaluated for
	// side-effects.
	|	expressionStatement 

	//TODO: what abour interfaces, enums and annotations
	// class definition
	|	(m:modifiers!)? classDefinition[#m]

	// Attach a label to the front of a statement
        // This block is executed for effect, unless it has an explicit closure argument.
        |       IDENT c:COLON^ {#c.setType(LABELED_STAT);}
                (   (LCURLY) => openOrClosedBlock
                |   statement
                )

	// If-else statement
        // Note:  There is no nls next to either paren, even though it would be increase compatibility
        // with various styles of Java indenting.  The "if" statement has the same constraints on white
        // space as a method call statement like "iflikemethod (args) { body }".  By restricting
        // newlines in both constructs in similar ways, we make Groovy internally consistent,
        // at a minor cost to compatibility with Java.
        // However we will allow newlines to not stop the parse, we just raise a warning instead.
        |       "if"^ LPAREN! expression RPAREN! nlsWarn! compatibleBodyStatement
                (
                        // CONFLICT: the old "dangling-else" problem...
                        //           ANTLR generates proper code matching
                        //                       as soon as possible.  Hush warning.
                        options {
                                warnWhenFollowAmbig = false;
                        }
                : // lookahead to check if we're entering an 'else' clause
                  ( (sep!)? "else"! )=>
                        (sep!)?  // allow SEMI here for compatibility with Java
                        "else"! nlsWarn! compatibleBodyStatement
                )?

	// For statement
	|	forStatement

	// While statement
	|	"while"^ LPAREN! expression RPAREN! nlsWarn! compatibleBodyStatement

	/*OBS* no do-while statement in Groovy (too ambiguous)
	// do-while statement
	|	"do"^ statement "while"! LPAREN! expression RPAREN! SEMI!
	*OBS*/
	// With statement
        // (This is the Groovy scope-shift mechanism, used for builders.)
        |       "with"^ LPAREN! expression RPAREN! nlsWarn! compoundStatement
        
        // Splice statement, meaningful only inside a "with" expression.
        // PROPOSED, DECIDE.  Prevents the namespace pollution of a "text" method or some such.
        |   sp:STAR^ nls!                       {#sp.setType(SPREAD_ARG);}
                expressionStatement
        // Example:  with(htmlbuilder) { head{} body{ *"some text" } }
        // Equivalent to:  { htmlbuilder.head{} htmlbuilder.body{ (htmlbuilder as Collection).add("some text") } }

        // Import statement.  Can be used in any scope.  Has "import x as y" also.
        |   importStatement

	// switch/case statement
	|	"switch"^ LPAREN! expression RPAREN! nlsWarn! LCURLY! nls!
			( casesGroup )*
		RCURLY!

	// exception try-catch block
	|	tryBlock

	// synchronize a statement
	|	"synchronized"^ LPAREN! expression RPAREN! nlsWarn! compoundStatement


	/*OBS*
	// empty statement
	|	s:SEMI {#s.setType(EMPTY_STAT);}
	*OBS*/

// removed as it conflicts with the 3 alternations of 'statement': compoundStatement, expressionStatement, a labeled statement
// todo: suggest fixing 'expressionStatement' to correctly accept the SL token '<<'
        /// Patch for x = []; x << 5 
        | conditionalExpression
	
	// NOTE: some alternations have been moved to 'branchExpression'
	;

forStatement
	:	f:"for"^
		LPAREN!
			(	(forInit SEMI)=>traditionalForClause
			// *OBS* 
			// There's no need at all for squeezing in the new Java 5 "for"
  // syntax, since Groovy's is a suitable alternative.
		 // |	(parameterDeclaration COLON)=> forEachClause
	  // *OBS*
			|       // the coast is clear; it's a modern Groovy for statement
				forInClause
			)
		RPAREN! nlsWarn!
		compatibleBodyStatement					 // statement to loop over
	;

traditionalForClause
	:
		forInit SEMI!	// initializer
		forCond SEMI!	// condition test
		forIter			// updater
	;

/*OBS*
forEachClause
	:
		p:parameterDeclaration COLON! expression
		{#forEachClause = #(#[FOR_EACH_CLAUSE,"FOR_EACH_CLAUSE"], #forEachClause);}
	;
*OBS*/

forInClause
        :       (   (declarationStart)=>
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
                statement
        ;

/* TODO - QUESTION - why do we use 'assignmentExpression' instead of 'expression' inside each branchExpression production */
/** In Groovy, return, break, continue, throw, and assert can be used in any expression context.
 *  Example:  println (x || return);  println assert x, "won't print a false value!"
 *  If an optional expression is missing, its value is void (this coerces to null when a value is required).
 */
branchExpression
        :
        // Return an expression
                "return"^ (assignmentExpression)?

        // break:  get out of a loop, or switch, or method call
        // continue:  do next iteration of a loop, or leave a closure
        |       ("break"^ | "continue"^)
                (   options {greedy=true;} :
                    statementLabelPrefix
                )?
                (assignmentExpression)?
                
        // throw an exception
        |       "throw"^ assignmentExpression


	// TODO - decide on definitive 'assert' statement in groovy (1.4 and|or groovy)
	// asserts
	// 1.4+ ...
	//      |	"assert"^ expression ( COLON! expression )?
	
	// groovy assertion...
        |       "assert"^ assignmentExpression
                (   options {greedy=true;} :
                        COMMA! assignmentExpression
                )?
	

        // Note:  The colon is too special in Groovy; we modify the FOR and ASSERT syntax to dispense with it.
        ;

// TO DO: Use this in the statement grammar also.
statementLabelPrefix
	:	IDENT c:COLON^ {#c.setType(LABELED_STAT);}
	;



/** Any statement which begins with an expression, called the "head".
 *  The head can be followed by command arguments:  {x.y a,b}, {x[y] a,b}, even {f(x) y}.
 *  Or, the head can be followed by an assignment operator:  {x.y = z}, {x.y[a] ++}, {x.y(a) += z}, etc.
 *  To catch simple errors, expressions at statement level have a limited set of syntaxes.
 *  For example, {print x; +y} is a syntax error.  (Java does this trick also.)
 *  If you really want something weird, wrap it in parentheses or curly braces.
 */
//  id(a), x.id(a), (x).id(a), etc.; id{...}, x.id{...}, (x).id{...}, etc.
expressionStatement
                { boolean endBrackets = false; }
        :   endBrackets=
                head:pathExpression!
                (       {!endBrackets}?
                        commandArguments[#head]
                |   assignmentTail[#head]
                |   {#expressionStatement = #head;}  // no command arguments
                )
                {   // Do an error check on the following token:
                        switch (LA(1)) {
                        case RCURLY: case RBRACK: case RPAREN: case SEMI: case NLS: case EOF:
                                break;
                        default:
                                require(false,
                                                "command followed by garbage in f...",
                                                "parenthesize correct argument list f(...) or whole expression (f()...)");
                        }
                }
        |
                // Prefix increment; a special case of assignment statement.
                (INC^ | DEC^) endBrackets=pathExpression
        |
                branchExpression
        ;

casesGroup
	:	(	// CONFLICT: to which case group do the statements bind?
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
	:	("case"^ expression | "default") COLON! nls!
	;

caseSList
	:	statement (sep! (statement)?)*
		{#caseSList = #(#[SLIST,"SLIST"],#caseSList);}
	;

// The initializer for a for loop
// The controlExpressionList production includes declarations as a possibility
forInit
        :       (controlExpressionList)?
                {#forInit = #(#[FOR_INIT,"FOR_INIT"],#forInit);}
        ;

forCond
	:	(expression)?
		{#forCond = #(#[FOR_CONDITION,"FOR_CONDITION"],#forCond);}
	;

forIter
	:	(controlExpressionList)?
		{#forIter = #(#[FOR_ITERATOR,"FOR_ITERATOR"],#forIter);}
	;

// an exception handler try/catch block
tryBlock
        :       "try"^ nlsWarn! compoundStatement
                ( options {greedy=true;} :  nls! handler)*
                ( options {greedy=true;} :  nls! finallyClause)?
        ;

finallyClause
	:	"finally"^ nlsWarn! compoundStatement
	;

// an exception handler
handler
	:	"catch"^ LPAREN! parameterDeclaration RPAREN! nlsWarn! compoundStatement
	;


assignmentTail[AST head]
        :
                {#assignmentTail = head;}
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
        |
                {#assignmentTail = head;}
                in:INC^ {#in.setType(POST_INC);}
        |
                {#assignmentTail = head;}
                de:DEC^ {#de.setType(POST_DEC);}
        ;

/** A member name (x.y) or element name (x[y]) can serve as a command name,
 *  which may be followed by a list of arguments.
 */
commandArguments[AST head]
        :   {   #commandArguments = head;
                        switch (LA(1)) {
                        case PLUS: case MINUS: case INC: case DEC:
                        case STAR: case DIV: case MOD:
                        case SR: case BSR: case SL:
                        case BAND: case BXOR: case BOR: case STAR_STAR:
                        require(false,
                                        "garbage infix or prefix operator after command name f +x",
                                        "parenthesize either the whole expression (f+x) or the command arguments f(+x)");
                        }
                }
                expression (options { greedy=true; } : COMMA! nls! expression)*
                // println 2+2 //BAD
                // println(2+2) //OK
                // println (2)+2 //BAD
                // println((2)+2) //OK
                // (println(2)+2) //OK
                // compare (2), 2 //BAD
                // compare( (2), 2 ) //OK
                // foo.bar baz{bat}, bang{boz} //OK?!
                {
                        AST headid = getASTFactory().dup(#head);
                        headid.setType(METHOD_CALL);
                        headid.setText("<command>");
                        #commandArguments = #(headid, #commandArguments);
                }
        ;

assignmentOp
        :
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
        ;

// expressions
// Note that most of these expressions follow the pattern
//   thisLevelExpression :
//	   nextHigherPrecedenceExpression
//		   (OPERATOR nextHigherPrecedenceExpression)*
// which is a standard recursive definition for a parsing an expression.
// The operators in java have the following precedences:
//	lowest  (15)  = **= *= /= %= += -= <<= >>= >>>= &= ^= |=
//			(14)  ?:
//			(13)  ||
//			(12)  &&
//			(11)  |
//			(10)  ^
//			( 9)  &
//			( 8)  == != <=>
//			( 7)  < <= > >=
//			( 6)  << >>
//			( 5)  +(binary) -(binary)
//			( 4)  * / %
//			( 3)  ++ -- +(unary) -(unary)
//			( 2)  **(power)
//			( 1)  ~  !  (type)
//				  []   () (method call)  . (dot -- identifier qualification)
//				  new   ()  (explicit parenthesis)
//
// the last two are not usually on a precedence chart; I put them in
// to point out that new has a higher precedence than '.', so you
// can validy use
//	 new Frame().show()
//
// Note that the above precedence levels map to the rules below...
// Once you have a precedence chart, writing the appropriate rules as below
//   is usually very straightfoward



// the mother of all expressions
// This nonterminal is not used for expression statements, which have a more restricted syntax
// due to possible ambiguities with other kinds of statements.  This nonterminal is used only
// in contexts where we know we have an expression.  It allows general Java-type expressions.
expression
        :   (declarationStart)=> singleDeclaration
        |   branchExpression
        |       assignmentExpression
                {#expression = #(#[EXPR,"EXPR"],#expression);}
        ;
// This is a list of expressions.
controlExpressionList
        :       controlExpression (COMMA! nls! controlExpression)*
                {#controlExpressionList = #(#[ELIST,"ELIST"], controlExpressionList);}
        ;

/** Used for backward compatibility, in a few places where
 *  Java expresion statements and declarations are required.
 */
controlExpression
                {boolean zz; /*ignore*/ }
        :       // if it looks like a declaration, it is
                (declarationStart)=> singleDeclaration
        |   // otherwise it's a plain statement expression
                zz=head:pathExpression!
                (       assignmentTail[#head]
                |   {#controlExpression = #head;}  // no command syntax in this context
                )
        |
                // Prefix increment; a special case of assignment statement.
                (INC^ | DEC^) zz=pathExpression
        ;

/** A "path expression" is a name which can be used for value, assigned to, or called.
 *  Uses include assignment targets, commands, and types in declarations.
 *  It is called a "path" because it looks like a linear path through a data structure.
 *  Example:  a.b[n].c(x).d{s}
 *  (Compare to a C lvalue, or LeftHandSide in the JLS section 15.26.)
 *  General expressions are built up from path expressions, using operators like '+' and '='.
 *  Note:  A path expression cannot begin with a block or closure.
 */
pathExpression
returns [boolean endBrackets = false]
        :   pe:primaryExpression!
                endBrackets=
                pathExpressionTail[#pe]
        ;

pathExpressionTail[AST result]
returns [boolean endBrackets = false]
        :
                // The primary can then be followed by a chain of .id, (a), [a], and {...}
                (
                        // Parsing of this chain is greedy.  For example, a pathExpression may be a command name
                        // followed by a command argument, but that command argument cannot begin with an LPAREN,
                        // since a parenthesized expression is greedily attached to the pathExpression as a method argument.
                        options { greedy=true; }
                        :

                        endBrackets=
                        pe:pathElement[result]!
                        { result = #pe; }
                )*
                {   #pathExpressionTail = result;  }
        ;

pathExpressionFromBrackets
                {boolean zz; /*ignore*/ }
        :   pe:expressionBlock!
                zz=pathExpressionTail[#pe]
        |   pe2:listOrMapConstructorExpression!
                zz=pathExpressionTail[#pe2]
        ;

pathElement[AST prefix]
returns [boolean endBrackets = false]
                {boolean zz; /*ignore*/ }
        :
                {   #pathElement = prefix;  }
                (   // Spread operator:  x*.y  ===  x?.collect{it.y}
                    sp:STAR_DOT^                                {#sp.setType(SPREAD_ARG);}
                |   // Optional-null operator:  x?.y  === (x==null)?null:x.y
                    op:QUESTION_DOT^                    {#op.setType(OPTIONAL_ARG);}
                |   // The all-powerful dot.
                    DOT^
                ) nls! namePart
                {   endBrackets = false; }
        |
                mca:methodCallArgs[prefix]
                {   #pathElement = #mca; endBrackets = true;  }
        |
                // Element selection is always an option, too.
                // In Groovy, the stuff between brackets is a general argument list,
                // since the bracket operator is transformed into a method call.
                // This can also be a declaration head; square brackets are used to parameterize array types.
                //todo: check that multiple arrayOrTypeArgs are valid here
                ata:arrayOrTypeArgs[prefix]
                {   #pathElement = #ata; endBrackets = false;  }

/*NYI*
        |       DOT^ nls! "this"

        |       DOT^ nls! "super"
                (   // (new Outer()).super()  (create enclosing instance)
                        lp3:LPAREN^ argList RPAREN!
                        {#lp3.setType(SUPER_CTOR_CALL);}
                |   DOT^ IDENT
                        (       lps:LPAREN^ {#lps.setType(METHOD_CALL);}
                                argList
                                RPAREN!
                        )?
                )
        |       DOT^ nls! newExpression
*NYI*/
        ;

/** Lookahead pattern for pathElement. */
pathElementStart!
        :   (LPAREN | LBRACE | LBRACK | DOT | (STAR|QUESTION) DOT)
        ;

/** This is the grammar for what can follow a dot:  x.a, x.@a, x.&a, x.'a', etc.
 */
namePart
        :
                (   amp:LAND^   {#amp.setType(REFLECT_MEMBER);} // foo.&bar reflects the 'bar' member of foo
                |       ats:AT^ {#ats.setType(SELECT_SLOT);}        // foo.@bar selects the field (or attribute), not property
                )?

            (   IDENT
            				|   sl:STRING_LITERAL {#sl.setType(IDENT);}
                        // foo.'bar' is in all ways same as foo.bar, except that bar can have an arbitrary spelling
                |   dn:dynamicMemberName!
                        {   #namePart = #(#[DYNAMIC_MEMBER, "DYNAMIC_MEMBER"], #dn);  }
                        // DECIDE PROPOSAL:  foo.(bar), x.(p?'a':'b') means dynamic lookup on a dynamic name
                |
                        openBlock
                        // PROPOSAL, DECIDE:  Is this inline form of the 'with' statement useful?
                        // Definition:  a.{foo} === {with(a) {foo}}
                        // May cover some path expression use-cases previously handled by dynamic scoping (closure delegates).
                
                								// lets allow common keywords as property names
                | 						keywordPropertyNames
                							
/* lets allow some common keywords for properties like 'in', 'class', 'def' etc                        
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

/** Allowed keywords after dot
*/
keywordPropertyNames
					: 	("class" | "in" | "def" | builtInType)
								{ #keywordPropertyNames.setType(IDENT); }
								;
						
/** If a dot is followed by a parenthesized or quoted expression, the member is computed dynamically,
 *  and the member selection is done only at runtime.  This forces a statically unchecked member access.
 */
dynamicMemberName
        :   (   parenthesizedExpression
                |   stringConstructorExpression
                )
        {   #dynamicMemberName = #(#[DYNAMIC_MEMBER, "DYNAMIC_MEMBER"], #dynamicMemberName);  }
        ;

/** An expression may be followed by one or both of (...) and {...}.
 *  Note: If either is (...) or {...} present, it is a method call.
 *  The {...} is appended to the argument list, and matches a formal of type Closure.
 *  If there is no method member, a property (or field) is used instead, and must itself be callable.
 *  <p>
 *  If the methodCallArgs are absent, it is a property (or field) reference, if possible.
 *  If there is no property or field, it is treated as a method call (nullary) after all.
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
 */
methodCallArgs[AST callee]
        {boolean zz; /*ignore*/ }
        :
                {#methodCallArgs = callee;}
                lp:LPAREN^ {#lp.setType(METHOD_CALL);}
                zz=argList
                RPAREN!
                ( options {greedy=true;} : appendedBlock )?             // maybe append a closure
        |
                // else use a closure alone
                {#methodCallArgs = callee;}
                cb:appendedBlock
                {   AST lbrace = getASTFactory().dup(#cb);
                        lbrace.setType(METHOD_CALL);
                        #methodCallArgs = #(lbrace, #methodCallArgs);
                }
        ;

/** An expression may be followed by [...].
 *  Unlike Java, these brackets may contain a general argument list,
 *  which is passed to the array element operator, which can make of it what it wants.
 *  The brackets may also be empty, as in T[].  This is how Groovy names array types.
 *  <p>Returned AST is [INDEX_OP, indexee, ELIST].
 *
 * *TODO* (The arrayOrTypeArgs thing in 1.4 groovy.g is a placeholder which
 * anticipates the trouble of integrating Java 5 type arguments.)
 */
arrayOrTypeArgs[AST indexee]
        {boolean zz; /*ignore*/ }
        :
                {#arrayOrTypeArgs = indexee;}
                (
                        // it's convenient to be greedy here, though it doesn't affect correctness
                        options { greedy = true; } :
                        lb:LBRACK^ {#lb.setType(INDEX_OP);}
                        zz=argList
                        RBRACK!
                )+
        ;

// assignment expression (level 15)
assignmentExpression
        :       conditionalExpression
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
                        assignmentExpression
                )?
        ;

// conditional test (level 14)
conditionalExpression
        :       logicalOrExpression
                ( QUESTION^ nls! assignmentExpression COLON! nls! conditionalExpression )?
        ;


// logical or (||)  (level 13)
logicalOrExpression
        :       logicalAndExpression (LOR^ nls! logicalAndExpression)*
        ;


// logical and (&&)  (level 12)
logicalAndExpression
        :       inclusiveOrExpression (LAND^ nls! inclusiveOrExpression)*
        ;

// bitwise or non-short-circuiting or (|)  (level 11)
inclusiveOrExpression
        :       exclusiveOrExpression (BOR^ nls! exclusiveOrExpression)*
        ;


// exclusive or (^)  (level 10)
exclusiveOrExpression
        :       andExpression (BXOR^ nls! andExpression)*
        ;


// bitwise or non-short-circuiting and (&)  (level 9)
andExpression
        :       regexExpression (BAND^ nls! regexExpression)*
        ;

// regex find and match (=~ and ==~) (level 8.5)
// jez: moved =~ closer to precedence of == etc, as...
// 'if (foo =~ "a.c")' is very close in intent to 'if (foo == "abc")'
regexExpression
        :       equalityExpression ((REGEX_FIND^ | REGEX_MATCH^) nls! equalityExpression)*
        ;

// equality/inequality (==/!=) (level 8)
equalityExpression
        :       relationalExpression ((NOT_EQUAL^ | EQUAL^ | COMPARE_TO^) nls! relationalExpression)*
        ;

// boolean relational expressions (level 7)
relationalExpression
        :       shiftExpression
                (       (       (       LT^
                                |       GT^
                                |       LE^
                                |       GE^
                                |       "in"^
                                )
                                nls!
                                shiftExpression
                        )?
                |       "instanceof"^ nls! typeSpec[true]
                |       "as"^         nls! typeSpec[true] //TO DO: Rework to allow type expression?
                )
        ;



// bit shift expressions (level 6)
shiftExpression
        :       additiveExpression
                (   ((SL^ | SR^ | BSR^) nls!
                        |   RANGE_INCLUSIVE^
                        |   TRIPLE_DOT^ {#TRIPLE_DOT.setType(RANGE_EXCLUSIVE);}
                        )
                        additiveExpression
                )*
        ;


// binary addition/subtraction (level 5)
additiveExpression
        :       multiplicativeExpression ((PLUS^ | MINUS^) nls! multiplicativeExpression)*
        ;


// multiplication/division/modulo (level 4)
multiplicativeExpression
        : ( INC^ nls!  powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
        | ( DEC^ nls!  powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
        | ( MINUS^ {#MINUS.setType(UNARY_MINUS);} nls!   powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
        | ( PLUS^ {#PLUS.setType(UNARY_PLUS);} nls!   powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
        | (      powerExpression ((STAR^ | DIV^ | MOD^ )  nls!  powerExpression)* )
        ;

// math power operator (**) (level 3)
powerExpression
        :       unaryExpressionNotPlusMinus (STAR_STAR^ nls! unaryExpression)*
        ;

// ++(prefix)/--(prefix)/+(unary)/-(unary)/$(GString expression) (level 2)
unaryExpression
        :       INC^ nls! unaryExpression
        |       DEC^ nls! unaryExpression
        |       MINUS^ {#MINUS.setType(UNARY_MINUS);} nls! unaryExpression
        |       PLUS^  {#PLUS.setType(UNARY_PLUS);} nls! unaryExpression
        |       DOLLAR^  {#DOLLAR.setType(SCOPE_ESCAPE);} nls! unaryExpression
        |       unaryExpressionNotPlusMinus
        ;

// The SCOPE_ESCAPE operator pops its operand out of the scope of a "with" block.
// If not within a "with" block, it pops the operand out of the static global scope,
// into whatever dynamic (unchecked) global scope is available when the script is run,
// regardless of package and imports.
// Example of SCOPE_ESCAPE:  def x=1; with ([x:2,y:-1]) { def y=3; println [$x, x, y] }  =>  "[1, 2, 3]"

// ~(BNOT)/!(LNOT)/(type casting) (level 1)
unaryExpressionNotPlusMinus
	:	BNOT^ nls! unaryExpression
	|	LNOT^ nls! unaryExpression
	|	(	// subrule allows option to shut off warnings
			options {
				// "(int" ambig with postfixExpr due to lack of sequence
				// info in linear approximate LL(k). It's ok. Shut up.
				generateAmbigWarnings=false;
			}
		:	// If typecast is built in type, must be numeric operand
			// Have to backtrack to see if operator follows
		(LPAREN builtInTypeSpec[true] RPAREN unaryExpression)=>
		lpb:LPAREN^ {#lpb.setType(TYPECAST);} builtInTypeSpec[true] RPAREN!
		unaryExpression

		// Have to backtrack to see if operator follows. If no operator
		// follows, it's a typecast. No semantic checking needed to parse.
		// if it _looks_ like a cast, it _is_ a cast; else it's a "(expr)"
		// TO DO:  Rework this mess for Groovy.
	|	(LPAREN classTypeSpec[true] RPAREN unaryExpressionNotPlusMinus)=>
		lp:LPAREN^ {#lp.setType(TYPECAST);} classTypeSpec[true] RPAREN!
		unaryExpressionNotPlusMinus

	|	postfixExpression
	)
	;

// qualified names, array expressions, method invocation, post inc/dec
postfixExpression
		{boolean zz; /*ignored*/}
	:
		(   zz=pathExpression                           // x, x.f, x(), x{}, etc.
                |   pathExpressionFromBrackets          // {c}, {c}(), [x].f, etc.
                )

                (
                        options {greedy=true;} :
                        // possibly add on a post-increment or post-decrement.
			// allows INC/DEC on too much, but semantics can check
			in:INC^ {#in.setType(POST_INC);}
	 	|	de:DEC^ {#de.setType(POST_DEC);}
		)?
 	;

// the basic element of an expression
primaryExpression
        :       IDENT
        /*OBS*  //class names work fine as expressions, no need for T.class in Groovy
                ( options {greedy=true;} : DOT^ "class" )?
        *OBS*/
    |   constant
        |       "true"
        |       "false"
        |       "null"
    |   newExpression
        |       "this"
        |       "super"
        |       parenthesizedExpression                 // (general stuff..,.)
        |   stringConstructorExpression         // "foo $bar baz"; presented as multiple tokens
        |   builtInType                                         // type expressions work in Groovy
        /*OBS*  //class names work fine as expressions
                // look for int.class and int[].class
        |       builtInType
                ( lbt:LBRACK^ {#lbt.setType(ARRAY_DECLARATOR);} RBRACK! )*
                DOT^ nls! "class"
        *OBS*/
        ;

parenthesizedExpression
        :   lp:LPAREN^ expression RPAREN!                       { #lp.setType(EXPR); }
        ;

// Groovy syntax for "$x $y".
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
        :   identifier          // aka, pathExpression
        |   openBlock
        ;

/**
 * A list constructor is a argument list enclosed in square brackets, without labels.
 * Any argument can be decorated with a spread or optional operator (*x, ?x), but not a label (a:x).
 * Examples:  [], [1], [1,2], [1,*l1,2], [*l1,*l2], [1,?x,2].
 * (The l1, l2 must be a sequence or null.)
 * <p>
 * A map constructor is an argument list enclosed in square brackets, with labels everywhere,
 * except possibly on spread arguments, which stand for whole maps spliced in.
 * A colon immediately after the left bracket also forces the expression to be a map constructor.
 * Examples: [:], [a:1], [: a:1], [a:1,b:2], [a:1,*m1,b:2], [:*m1,*m2], [a:1,q:?x,b:2], [a:1,a:*x,b:2]
 * (The m1, m2 must be a map or null.)
 * Values associated with identical keys overwrite from left to right:
 * [a:1,a:2]  ===  [a:2]
 * <p>
 * Some malformed constructor expressions are not detected in the parser, but in a post-pass.
 * Bad examples: [1,b:2], [a:1,2], [:1], [a:1,?x], [a:1, b:*x].
 * (Note that method call arguments, by contrast, can be a mix of keyworded and non-keyworded arguments.)
 */
listOrMapConstructorExpression
                { boolean hasLabels = false, hal; }
        :   lcon:LBRACK^
                (  COLON!               { hasLabels |= true; }  )?  // [:], [:*x], [:*x,*y] are map constructors
                hal=argList             { hasLabels |= hal;  }      // any argument label implies a map
                RBRACK!
                {   #lcon.setType(hasLabels ? MAP_CONSTRUCTOR : LIST_CONSTRUCTOR);  }
        ;

/*OBS*
/** Match a, a.b.c refs, a.b.c(...) refs, a.b.c[], a.b.c[].class,
 *  and a.b.c.class refs. Also this(...) and super(...). Match
 *  this or super.
 */
/*OBS*
identPrimary
	:	(ta1:typeArguments!)?
		IDENT
		// Syntax for method invocation with type arguments is
		// <String>foo("blah")
		(
			options {
				// .ident could match here or in postfixExpression.
				// We do want to match here. Turn off warning.
				greedy=true;
				// This turns the ambiguity warning of the second alternative
				// off. See below. (The "false" predicate makes it non-issue)
				warnWhenFollowAmbig=false;
			}
			// we have a new nondeterminism because of
			// typeArguments... only a syntactic predicate will help...
			// The problem is that this loop here conflicts with
			// DOT typeArguments "super" in postfixExpression (k=2)
			// A proper solution would require a lot of refactoring...
		:	(DOT (typeArguments)? IDENT) =>
				DOT^ (ta2:typeArguments!)? IDENT
		|	{false}?	// FIXME: this is very ugly but it seems to work...
						// this will also produce an ANTLR warning!
				// Unfortunately a syntactic predicate can only select one of
				// multiple alternatives on the same level, not break out of
				// an enclosing loop, which is why this ugly hack (a fake
				// empty alternative with always-false semantic predicate)
				// is necessary.
		)*
		(
			options {
				// ARRAY_DECLARATOR here conflicts with INDEX_OP in
				// postfixExpression on LBRACK RBRACK.
				// We want to match [] here, so greedy. This overcomes
				// limitation of linear approximate lookahead.
				greedy=true;
			}
		:	(	lp:LPAREN^ {#lp.setType(METHOD_CALL);}
				// if the input is valid, only the last IDENT may
				// have preceding typeArguments... rather hacky, this is...
				{if (#ta2 != null) astFactory.addASTChild(currentAST, #ta2);}
				{if (#ta2 == null) astFactory.addASTChild(currentAST, #ta1);}
				argList RPAREN!
			)
		|	( options {greedy=true;} :
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
 *		   |
 *		  arg1 -- arg2 -- .. -- argn
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
 *								  |
 *								EXPR -- EXPR
 *								  |	  |
 *								  1	  2
 *
 *  new int[3]
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *				|
 *			  EXPR
 *				|
 *				3
 *
 *  new int[1][2]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *			   |
 *		 ARRAY_DECLARATOR -- EXPR
 *			   |			  |
 *			 EXPR			 1
 *			   |
 *			   2
 *
 */
newExpression
		{boolean zz; /*ignored*/}
	:	"new"^ (typeArguments)? type
		(	LPAREN! zz=argList RPAREN! 
			/*TODO - NYI* (anonymousInnerClassBlock)? *NYI*/

		//java 1.1
			// Note: This will allow bad constructs like
			//	new int[4][][3] {exp,exp}.
			//	There needs to be a semantic check here...
			// to make sure:
			//   a) [ expr ] and [ ] are not mixed
			//   b) [ expr ] and an init are not used together

		|	newArrayDeclarator //(arrayInitializer)?
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
returns [boolean hasLabels = false]
{ boolean hl2; }
        :       (
                        hasLabels=argument
                        (
                                options { greedy=true; }:
                                COMMA! hl2=argument             { hasLabels |= hl2; }
                        )*
                        {#argList = #(#[ELIST,"ELIST"], argList);}
                |       /*nothing*/
                        {#argList = #[ELIST,"ELIST"];}
                )

                // DECIDE: Allow an extra trailing comma, for easy editing of long lists.
                // This applies uniformly to [x,y,] and (x,y,).  It is inspired by Java's a[] = {x,y,}.
                (   COMMA!  )?
        ;

/** A single argument in (...) or [...].  Corresponds to to a method or closure parameter.
 *  May be labeled.  May be modified by the spread or optionality operators *, ?.
 */
argument
returns [boolean hasLabel = false]
        :
                // Optional argument label.
                // Usage:  Specifies a map key, or a keyworded argument.
                (   (argumentLabelStart) =>
                        argumentLabel c:COLON^                  {#c.setType(LABELED_ARG);}
                )?

                (       // Spread operator:  f(*[a,b,c])  ===  f(a,b,c);  f(1,*null,2)  ===  f(1,2).
                        sp:STAR^                                {#sp.setType(SPREAD_ARG);}
                |       // Optional-null operator:  f(1,?x,2)  ===  (x==null)?f(1,2):f(1,x,2)
                        op:QUESTION^                    {#op.setType(OPTIONAL_ARG);}
                )?

                expression
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
            id:IDENT	           			{#id.setType(STRING_LITERAL);}  // identifiers are self-quoting in this context
        | 		kw:keywordPropertyNames 	 {#kw.setType(STRING_LITERAL);}  // identifiers are self-quoting in this context
        |   primaryExpression                                           // dynamic expression
        ;

/** For lookahead only.  Fast approximate parse of a statementLabel followed by a colon. */
argumentLabelStart!
									// allow number and string literals as labels for maps
        :   ( IDENT | keywordPropertyNames | constant | (LPAREN | STRING_CTOR_START)=> balancedBrackets ) COLON
        ;

newArrayDeclarator
	:	(
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

constant
        :       NUM_INT
        |       STRING_LITERAL
        |       NUM_FLOAT
        |       NUM_LONG
        |       NUM_DOUBLE
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

balancedTokensNoSep!
        :   (   balancedBrackets
                |   ~(LPAREN|LBRACK|LCURLY | STRING_CTOR_START
                     |RPAREN|RBRACK|RCURLY | STRING_CTOR_END     | SEMI|NLS)
                )*
        ;

/** A statement separator is either a semicolon or a significant newline. 
 *  Any number of additional (insignificant) newlines may accompany it.
 *  (All the '!' signs simply suppress the default AST building.)
 */
sep!
        :   SEMI! nls!
        |   NLS!                // this newline is significant!
                (
                        options { greedy=true; }:
                        SEMI! nls!      // this superfluous semicolon is gobbled
                )?
        ;

/** Zero or more insignificant newlines, all gobbled up and thrown away. */
nls!    :
                (options { greedy=true; }: NLS!)?
        ;

/** Zero or more insignificant newlines, all gobbled up and thrown away,
  * but a warning message is left for the user.
  */
nlsWarn!    :
                (options { greedy=true; }: NLS!)?
	                { addWarning(
                	  "A newline at this point does not follow the Groovy Coding Conventions.",
	                	  "Keep this statement on one line, or use curly braces to break across multiple lines."
	                	); }
        ;


// TO DO: declarations in expression position

//----------------------------------------------------------------------------
// The Groovy scanner
//----------------------------------------------------------------------------
class GroovyLexer extends Lexer;

options {
	exportVocab=Groovy;		// call the vocabulary "Groovy"
	testLiterals=false;		// don't automatically test for literals
	k=4;					// four characters of lookahead
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

	/** Enable the "assert" keyword */
	public void enableAssert(boolean shouldEnable) { assertEnabled = shouldEnable; }
	/** Query the "assert" keyword state */
	public boolean isAssertEnabled() { return assertEnabled; }
	/** Enable the "enum" keyword */
	public void enableEnum(boolean shouldEnable) { enumEnabled = shouldEnable; }
	/** Query the "enum" keyword state */
	public boolean isEnumEnabled() { return enumEnabled; }


/** Bumped when inside '[x]' or '(x)', reset inside '{x}'.  See ONE_NL.  */
	protected int parenLevel = 0;
	protected int suppressNewline = 0;  // be really mean to newlines inside strings
	protected static final int SCS_TRIPLE = 1, SCS_VAL = 2, SCS_LIT = 4, SCS_LIMIT = 8;
	protected int stringCtorState = 0;  // hack string constructor boundaries
	/** Push parenLevel here and reset whenever inside '{x}'. */
	protected ArrayList parenLevelStack = new ArrayList();
	protected Token lastToken = Token.badToken;

	protected void pushParenLevel() {
		parenLevelStack.add(new Integer(parenLevel*8 + stringCtorState));
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
			stringCtorState = (expectLiteral? SCS_LIT: SCS_VAL) + (stringCtorState & SCS_TRIPLE);
		}
	}

	void newlineCheck() throws RecognitionException {
		if (suppressNewline > 0) {
			suppressNewline = 0;
			require(suppressNewline == 0,
				"end of line reached within a simple string 'x' or \"x\"",
				"for multi-line literals, use triple quotes '''x''' or \"\"\"x\"\"\"");
		}
		newline();
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
					boolean tripleQuote = (stringCtorState & SCS_TRIPLE) != 0;
					stringCtorState = 0;  // get out of this mode, now
					resetText();
					try {
						mSTRING_CTOR_END(true, /*fromStart:*/false, tripleQuote);
						return lastToken = _returnToken;
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
				return lastToken = GroovyLexer.this.nextToken();
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

	protected GroovyRecognizer parser;  // little-used link; TO DO: get rid of
	private void require(boolean z, String problem, String solution) throws SemanticException {
		// TO DO: Direct to a common error handler, rather than through the parser.
		if (!z)  parser.requireFailed(problem, solution);
	}
}

// TO DO:  Regexp ops, range ops, Borneo-style ops.

/* *TODO*
DOT (with double and triple dot) is an ordinary operator
token; it doesn't need to be commented out.
*/

// OPERATORS
QUESTION		:	'?'		;
LPAREN			:	'('		{++parenLevel;};
RPAREN			:	')'		{--parenLevel;};
LBRACK			:	'['		{++parenLevel;};
RBRACK			:	']'		{--parenLevel;};
LCURLY			:	'{'		{pushParenLevel();};
RCURLY			:	'}'		{popParenLevel(); if(stringCtorState!=0) restartStringCtor(true);};
COLON			:	':'		;
COMMA			:	','		;
DOT			:	'.'		;
ASSIGN			:	'='		;
COMPARE_TO		:	"<=>"	;
EQUAL			:	"=="	;
LNOT			:	'!'		;
BNOT			:	'~'		;
NOT_EQUAL		:	"!="	;
DIV				:	'/'		;
DIV_ASSIGN		:	"/="	;
PLUS			:	'+'		;
PLUS_ASSIGN		:	"+="	;
INC				:	"++"	;
MINUS			:	'-'		;
MINUS_ASSIGN	:	"-="	;
DEC				:	"--"	;
STAR			:	'*'		;
STAR_ASSIGN		:	"*="	;
MOD				:	'%'		;
MOD_ASSIGN		:	"%="	;
SR				:	">>"	;
SR_ASSIGN		:	">>="	;
BSR				:	">>>"	;
BSR_ASSIGN		:	">>>="	;
GE				:	">="	;
GT				:	">"		;
SL				:	"<<"	;
SL_ASSIGN		:	"<<="	;
LE				:	"<="	;
LT				:	'<'		;
BXOR			:	'^'		;
BXOR_ASSIGN		:	"^="	;
BOR				:	'|'		;
BOR_ASSIGN		:	"|="	;
LOR				:	"||"	;
BAND			:	'&'		;
BAND_ASSIGN		:	"&="	;
LAND			:	"&&"	;
SEMI			:	';'		;
DOLLAR          :   '$'     ;
RANGE_INCLUSIVE	:   ".."    ;
TRIPLE_DOT		:   "..."   ;
STAR_DOT        :   "*."    ;
QUESTION_DOT    :   "?."    ;
REGEX_FIND      :   "=~"    ;
REGEX_MATCH     :   "==~"   ;
STAR_STAR		:	"**"		;
STAR_STAR_ASSIGN	:	"**="	;


// Whitespace -- ignored
WS      :       (
                options { greedy=true; }:
                        ' '
                |       '\t'
                |       '\f'
                )+
                { _ttype = Token.SKIP; }
        ;

protected
ONE_NL! :       // handle newlines, which are significant in Groovy
                (       options {generateAmbigWarnings=false;}
                :       "\r\n"  // Evil DOS
                |       '\r'    // Macintosh
                |       '\n'    // Unix (the right way)
                )
                {
                        // update current line number for error reporting
                        newlineCheck();
                }
        ;
        
// Group any number of newlines (with comments and whitespace) into a single token.
// This reduces the amount of parser lookahead required to parse around newlines.
// It is an invariant that the parser never sees NLS tokens back-to-back.
NLS     :       
                (       ONE_NL
                        (WS | SL_COMMENT | ML_COMMENT)*
                        // (gobble, gobble)*
                )+
                // Inside (...) and [...] but not {...}, ignore newlines.
                {   if (parenLevel != 0) {
                                $setType(Token.SKIP);
                        } else {
                                $setText("<lineterm>");
                        }
                }
        ;

// Single-line comments
SL_COMMENT
        :       "//"
                (
                        options {  greedy = true;  }:
                        ~('\n'|'\r')
                )*
                {$setType(Token.SKIP);}
                //This might be significant, so don't swallow it inside the comment:
                //ONE_NL
        ;

// multiple-line comments
ML_COMMENT
	:	"/*"
		(	/*	'\r' '\n' can be matched in one alternative or by matching
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
		|	'\r' '\n'		{newlineCheck();}
		|	'\r'			{newlineCheck();}
		|	'\n'			{newlineCheck();}
		|	~('*'|'\n'|'\r')
		)*
		"*/"
		{$setType(Token.SKIP);}
	;


// string literals
STRING_LITERAL
                {int tt=0;}
        :   ("'''") =>  //...shut off ambiguity warning
                "'''"!
                (   STRING_CH | ESC | '"' | '$'
                |       ('\'' (~'\'' | '\'' ~'\'')) => '\''  // allow 1 or 2 close quotes
                )*
                "'''"!
        |       '\''!
                                        {++suppressNewline;}
                (   STRING_CH | ESC | '"' | '$'  )*
                                        {--suppressNewline;}
                '\''!
        |   ("\"\"\"") =>  //...shut off ambiguity warning
                "\"\"\""!
                tt=STRING_CTOR_END[true, /*tripleQuote:*/ true]
                {$setType(tt);}
        |       '"'!
                                        {++suppressNewline;}
                tt=STRING_CTOR_END[true, /*tripleQuote:*/ false]
                {$setType(tt);}
        ;

protected
STRING_CTOR_END[boolean fromStart, boolean tripleQuote]
returns [int tt=STRING_CTOR_END]
        :
                (
                        options {  greedy = true;  }:
                    STRING_CH | ESC | '\''
                |   ('"' (~'"' | '"' ~'"'))=> {tripleQuote}? '"'  // allow 1 or 2 close quotes
                )*
                (       (   { !tripleQuote }? "\""!
                        |   {  tripleQuote }? "\"\"\""!
                        )
                        {
                                if (fromStart)      tt = STRING_LITERAL;  // plain string literal!
                                if (!tripleQuote)       {--suppressNewline;}
                                // done with string constructor!
                                //assert(stringCtorState == 0);
                        }
                |   '$'!
                        {
                                // (('*')? ('{' | LETTER)) =>
                                int k = 1;
                                char lc = LA(k);
                                if (lc == '*')  lc = LA(++k);
                                require(lc == '{' || (lc != '$' && Character.isJavaIdentifierStart(lc)),
                                                "illegal string body character after dollar sign",
                                                "either escape a literal dollar sign \"\\$5\" or bracket the value expression \"${5}\"");
                                // Yes, it's a string constructor, and we've got a value part.
                                tt = (fromStart ? STRING_CTOR_START : STRING_CTOR_MIDDLE);
                                stringCtorState = SCS_VAL + (tripleQuote? SCS_TRIPLE: 0);
                        }
                )
                {   $setType(tt);  }
        ;

protected
STRING_CH
        :   ~('"'|'\''|'\\'|'$'|'\n'|'\r')
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
	:	'\\'!
		(	'n'     {$setText("\n");}
		|	'r'     {$setText("\r");}
		|	't'     {$setText("\t");}
		|	'b'     {$setText("\b");}
		|	'f'     {$setText("\f");}
		|	'"'
		|	'\''
		|	'\\'
		|	'$'	//escape Groovy $ operator uniformly also
		|	('u')+ {$setText("");}
					HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
					{char ch = (char)Integer.parseInt($getText,16); $setText(ch);}
		|	'0'..'3'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
				(
					options {
						warnWhenFollowAmbig = false;
					}
				:	'0'..'7'
				)?
			)?
					{char ch = (char)Integer.parseInt($getText.substring(1),8); $setText(ch);}
		|	'4'..'7'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
			)?
				{char ch = (char)Integer.parseInt($getText.substring(1),8); $setText(ch);}
		)
	|!      '\\' ONE_NL
        |!      ONE_NL          { $setText('\n'); }             // always normalize to newline
	;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
	:	('0'..'9'|'A'..'F'|'a'..'f')
	;


// a dummy rule to force vocabulary to be all characters (except special
// ones that ANTLR uses internally (0 to 2)
protected
VOCAB
	:	'\3'..'\377'
	;


// an identifier. Note that testLiterals is set to true! This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
	options {testLiterals=true;}
	:	LETTER(LETTER|DIGIT)*
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
                        if (ttype != IDENT && lastToken.getType() == DOT) {
                                // A few keywords can follow a dot:
                                switch (ttype) {
                                case LITERAL_this: case LITERAL_super: case LITERAL_class:
                                        break;
                                default:
                                        ttype = LITERAL_in;  // the poster child for bad dotted names
                                }
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
        :   'a'..'z'|'A'..'Z'|'_'
        // TO DO:  Recognize all the Java identifier starts here (except '$').
        ;

protected
DIGIT
        :   '0'..'9'
        // TO DO:  Recognize all the Java identifier parts here (except '$').
        ;

// a numeric literal
NUM_INT
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
	// TO DO:  This complex pattern seems wrong.  Verify or fix.
	(	'0' {isDecimal = true;} // special case for just '0'
			(	('x'|'X')
			    {isDecimal = false;}
				(											// hex
					// the 'e'|'E' and float suffix stuff look
					// like hex digits, hence the (...)+ doesn't
					// know when to stop: ambig. ANTLR resolves
					// it correctly by matching immediately. It
					// is therefor ok to hush warning.
					options {
						warnWhenFollowAmbig=false;
					}
				:	HEX_DIGIT
				)+

			|	//float or double with leading zero
				(('0'..'9')+ ('.'('0'..'9')|EXPONENT|FLOAT_SUFFIX)) => ('0'..'9')+

			|	('0'..'7')+									// octal
			    {isDecimal = false;}
			)?
		|	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non-zero decimal
		)
		(	('l'|'L') { _ttype = NUM_LONG; }
		|	('i'|'I') { _ttype = NUM_INT; }
		|	BIG_SUFFIX { _ttype = NUM_BIG_INT; }

		// only check to see if it's a float if looks like decimal so far
		|
            (~'.' | '.' ('0'..'9')) =>
		    {isDecimal}?
			(	'.' ('0'..'9')+ (EXPONENT)? (f2:FLOAT_SUFFIX {t=f2;} | g2:BIG_SUFFIX {t=g2;})?
			|	EXPONENT (f3:FLOAT_SUFFIX {t=f3;} | g3:BIG_SUFFIX {t=g3;})?
			|	f4:FLOAT_SUFFIX {t=f4;}
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
	:	'@'
	;

// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
	:	('e'|'E') ('+'|'-')? ('0'..'9')+
	;


protected
FLOAT_SUFFIX
	:	'f'|'F'|'d'|'D'
	;

protected
BIG_SUFFIX
        :       'g'|'G'
        ;

// Here's a little hint for you, Emacs:
// Local Variables:
// mode: java
// tab-width: 4
// End:
