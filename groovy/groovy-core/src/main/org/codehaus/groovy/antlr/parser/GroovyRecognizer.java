// $ANTLR 2.7.2: "groovy.g" -> "GroovyRecognizer.java"$

package org.codehaus.groovy.antlr.parser;
import org.codehaus.groovy.antlr.*;
import java.util.*;
	import java.io.InputStream;
	import java.io.Reader;
	import antlr.InputBuffer;
	import antlr.LexerSharedInputState;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

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
public class GroovyRecognizer extends antlr.LLkParser       implements GroovyTokenTypes
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

protected GroovyRecognizer(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public GroovyRecognizer(TokenBuffer tokenBuf) {
  this(tokenBuf,3);
}

protected GroovyRecognizer(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public GroovyRecognizer(TokenStream lexer) {
  this(lexer,3);
}

public GroovyRecognizer(ParserSharedInputState state) {
  super(state,3);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void compilationUnit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compilationUnit_AST = null;
		
		nls();
		{
		boolean synPredMatched636 = false;
		if (((LA(1)==LITERAL_package||LA(1)==AT) && (LA(2)==IDENT) && (_tokenSet_0.member(LA(3))))) {
			int _m636 = mark();
			synPredMatched636 = true;
			inputState.guessing++;
			try {
				{
				annotations();
				match(LITERAL_package);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched636 = false;
			}
			rewind(_m636);
			inputState.guessing--;
		}
		if ( synPredMatched636 ) {
			packageDefinition();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop639:
			do {
				if ((LA(1)==SEMI||LA(1)==NLS)) {
					sep();
					{
					switch ( LA(1)) {
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case DEF:
					case LITERAL_import:
					case LITERAL_static:
					case AT:
					case LBRACK:
					case IDENT:
					case LPAREN:
					case LITERAL_class:
					case LITERAL_super:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_any:
					case STAR:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					case LCURLY:
					case LITERAL_this:
					case STRING_LITERAL:
					case LITERAL_if:
					case LITERAL_while:
					case LITERAL_with:
					case LITERAL_switch:
					case LITERAL_for:
					case LITERAL_return:
					case LITERAL_break:
					case LITERAL_continue:
					case LITERAL_throw:
					case LITERAL_assert:
					case INC:
					case DEC:
					case LITERAL_try:
					case PLUS:
					case MINUS:
					case BNOT:
					case LNOT:
					case LITERAL_true:
					case LITERAL_false:
					case LITERAL_null:
					case STRING_CTOR_START:
					case LITERAL_new:
					case NUM_INT:
					case NUM_FLOAT:
					case NUM_LONG:
					case NUM_DOUBLE:
					case NUM_BIG_INT:
					case NUM_BIG_DECIMAL:
					{
						statement();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case SEMI:
					case NLS:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop639;
				}
				
			} while (true);
			}
			match(Token.EOF_TYPE);
		}
		else if ((_tokenSet_1.member(LA(1))) && (_tokenSet_2.member(LA(2))) && (_tokenSet_3.member(LA(3)))) {
			{
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case DEF:
			case LITERAL_import:
			case LITERAL_static:
			case AT:
			case LBRACK:
			case IDENT:
			case LPAREN:
			case LITERAL_class:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_any:
			case STAR:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			case LITERAL_this:
			case STRING_LITERAL:
			case LITERAL_if:
			case LITERAL_while:
			case LITERAL_with:
			case LITERAL_switch:
			case LITERAL_for:
			case LITERAL_return:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_throw:
			case LITERAL_assert:
			case INC:
			case DEC:
			case LITERAL_try:
			case PLUS:
			case MINUS:
			case BNOT:
			case LNOT:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			case STRING_CTOR_START:
			case LITERAL_new:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			{
				statement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case SEMI:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop643:
			do {
				if ((LA(1)==SEMI||LA(1)==NLS)) {
					sep();
					{
					switch ( LA(1)) {
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case DEF:
					case LITERAL_import:
					case LITERAL_static:
					case AT:
					case LBRACK:
					case IDENT:
					case LPAREN:
					case LITERAL_class:
					case LITERAL_super:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_any:
					case STAR:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					case LCURLY:
					case LITERAL_this:
					case STRING_LITERAL:
					case LITERAL_if:
					case LITERAL_while:
					case LITERAL_with:
					case LITERAL_switch:
					case LITERAL_for:
					case LITERAL_return:
					case LITERAL_break:
					case LITERAL_continue:
					case LITERAL_throw:
					case LITERAL_assert:
					case INC:
					case DEC:
					case LITERAL_try:
					case PLUS:
					case MINUS:
					case BNOT:
					case LNOT:
					case LITERAL_true:
					case LITERAL_false:
					case LITERAL_null:
					case STRING_CTOR_START:
					case LITERAL_new:
					case NUM_INT:
					case NUM_FLOAT:
					case NUM_LONG:
					case NUM_DOUBLE:
					case NUM_BIG_INT:
					case NUM_BIG_DECIMAL:
					{
						statement();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case SEMI:
					case NLS:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop643;
				}
				
			} while (true);
			}
			match(Token.EOF_TYPE);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(Token.EOF_TYPE);
		compilationUnit_AST = (AST)currentAST.root;
		returnAST = compilationUnit_AST;
	}
	
/** Zero or more insignificant newlines, all gobbled up and thrown away. */
	public final void nls() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nls_AST = null;
		
		{
		if ((LA(1)==NLS) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3)))) {
			match(NLS);
		}
		else if ((_tokenSet_4.member(LA(1))) && (_tokenSet_5.member(LA(2))) && (_tokenSet_5.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		returnAST = nls_AST;
	}
	
	public final void annotations() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotations_AST = null;
		
		{
		_loop714:
		do {
			if ((LA(1)==AT)) {
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else {
				break _loop714;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			annotations_AST = (AST)currentAST.root;
			annotations_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ANNOTATIONS,"ANNOTATIONS")).add(annotations_AST));
			currentAST.root = annotations_AST;
			currentAST.child = annotations_AST!=null &&annotations_AST.getFirstChild()!=null ?
				annotations_AST.getFirstChild() : annotations_AST;
			currentAST.advanceChildToEnd();
		}
		annotations_AST = (AST)currentAST.root;
		returnAST = annotations_AST;
	}
	
	public final void packageDefinition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST packageDefinition_AST = null;
		Token  p = null;
		AST p_AST = null;
		
		annotations();
		astFactory.addASTChild(currentAST, returnAST);
		p = LT(1);
		p_AST = astFactory.create(p);
		astFactory.makeASTRoot(currentAST, p_AST);
		match(LITERAL_package);
		if ( inputState.guessing==0 ) {
			p_AST.setType(PACKAGE_DEF);
		}
		identifier();
		astFactory.addASTChild(currentAST, returnAST);
		packageDefinition_AST = (AST)currentAST.root;
		returnAST = packageDefinition_AST;
	}
	
/** A statement separator is either a semicolon or a significant newline. 
 *  Any number of additional (insignificant) newlines may accompany it.
 *  (All the '!' signs simply suppress the default AST building.)
 */
	public final void sep() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST sep_AST = null;
		
		switch ( LA(1)) {
		case SEMI:
		{
			match(SEMI);
			nls();
			break;
		}
		case NLS:
		{
			match(NLS);
			{
			if ((LA(1)==SEMI) && (_tokenSet_6.member(LA(2))) && (_tokenSet_7.member(LA(3)))) {
				match(SEMI);
				nls();
			}
			else if ((_tokenSet_6.member(LA(1))) && (_tokenSet_7.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = sep_AST;
	}
	
	public final void statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST statement_AST = null;
		AST m_AST = null;
		Token  c = null;
		AST c_AST = null;
		Token  sp = null;
		AST sp_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_if:
		{
			AST tmp8_AST = null;
			tmp8_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp8_AST);
			match(LITERAL_if);
			match(LPAREN);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			nlsWarn();
			compatibleBodyStatement();
			astFactory.addASTChild(currentAST, returnAST);
			{
			boolean synPredMatched902 = false;
			if (((_tokenSet_9.member(LA(1))) && (_tokenSet_10.member(LA(2))) && (_tokenSet_11.member(LA(3))))) {
				int _m902 = mark();
				synPredMatched902 = true;
				inputState.guessing++;
				try {
					{
					{
					switch ( LA(1)) {
					case SEMI:
					case NLS:
					{
						sep();
						break;
					}
					case LITERAL_else:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(LITERAL_else);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched902 = false;
				}
				rewind(_m902);
				inputState.guessing--;
			}
			if ( synPredMatched902 ) {
				{
				switch ( LA(1)) {
				case SEMI:
				case NLS:
				{
					sep();
					break;
				}
				case LITERAL_else:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(LITERAL_else);
				nlsWarn();
				compatibleBodyStatement();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_12.member(LA(1))) && (_tokenSet_13.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			statement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_for:
		{
			forStatement();
			astFactory.addASTChild(currentAST, returnAST);
			statement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_while:
		{
			AST tmp12_AST = null;
			tmp12_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp12_AST);
			match(LITERAL_while);
			match(LPAREN);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			nlsWarn();
			compatibleBodyStatement();
			astFactory.addASTChild(currentAST, returnAST);
			statement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_with:
		{
			AST tmp15_AST = null;
			tmp15_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp15_AST);
			match(LITERAL_with);
			match(LPAREN);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			nlsWarn();
			compoundStatement();
			astFactory.addASTChild(currentAST, returnAST);
			statement_AST = (AST)currentAST.root;
			break;
		}
		case STAR:
		{
			sp = LT(1);
			sp_AST = astFactory.create(sp);
			astFactory.makeASTRoot(currentAST, sp_AST);
			match(STAR);
			nls();
			if ( inputState.guessing==0 ) {
				sp_AST.setType(SPREAD_ARG);
			}
			expressionStatement();
			astFactory.addASTChild(currentAST, returnAST);
			statement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_import:
		{
			importStatement();
			astFactory.addASTChild(currentAST, returnAST);
			statement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_switch:
		{
			AST tmp18_AST = null;
			tmp18_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp18_AST);
			match(LITERAL_switch);
			match(LPAREN);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			nlsWarn();
			match(LCURLY);
			nls();
			{
			_loop905:
			do {
				if ((LA(1)==LITERAL_default||LA(1)==LITERAL_case)) {
					casesGroup();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop905;
				}
				
			} while (true);
			}
			match(RCURLY);
			statement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_try:
		{
			tryBlock();
			astFactory.addASTChild(currentAST, returnAST);
			statement_AST = (AST)currentAST.root;
			break;
		}
		default:
			if ((LA(1)==LCURLY) && (_tokenSet_14.member(LA(2))) && (_tokenSet_11.member(LA(3)))) {
				compoundStatement();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					require(false,
					"ambiguous free-floating block head{...} needs context to determine if it's open or closed",
					"surround {...} with extra braces {{...}} or use it as a closure in an expression x={...}");
				}
				statement_AST = (AST)currentAST.root;
			}
			else {
				boolean synPredMatched894 = false;
				if (((_tokenSet_15.member(LA(1))) && (_tokenSet_16.member(LA(2))) && (_tokenSet_17.member(LA(3))))) {
					int _m894 = mark();
					synPredMatched894 = true;
					inputState.guessing++;
					try {
						{
						declarationStart();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched894 = false;
					}
					rewind(_m894);
					inputState.guessing--;
				}
				if ( synPredMatched894 ) {
					declaration();
					astFactory.addASTChild(currentAST, returnAST);
					statement_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_18.member(LA(1))) && (_tokenSet_19.member(LA(2))) && (_tokenSet_7.member(LA(3)))) {
					expressionStatement();
					astFactory.addASTChild(currentAST, returnAST);
					statement_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_21.member(LA(2))) && (_tokenSet_22.member(LA(3)))) {
					{
					switch ( LA(1)) {
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case LITERAL_static:
					case AT:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					{
						modifiers();
						m_AST = (AST)returnAST;
						break;
					}
					case LITERAL_class:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					classDefinition(m_AST);
					astFactory.addASTChild(currentAST, returnAST);
					statement_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==IDENT) && (LA(2)==COLON) && (_tokenSet_23.member(LA(3)))) {
					AST tmp23_AST = null;
					tmp23_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp23_AST);
					match(IDENT);
					c = LT(1);
					c_AST = astFactory.create(c);
					astFactory.makeASTRoot(currentAST, c_AST);
					match(COLON);
					if ( inputState.guessing==0 ) {
						c_AST.setType(LABELED_STAT);
					}
					{
					boolean synPredMatched898 = false;
					if (((LA(1)==LCURLY) && (_tokenSet_24.member(LA(2))) && (_tokenSet_25.member(LA(3))))) {
						int _m898 = mark();
						synPredMatched898 = true;
						inputState.guessing++;
						try {
							{
							match(LCURLY);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched898 = false;
						}
						rewind(_m898);
						inputState.guessing--;
					}
					if ( synPredMatched898 ) {
						openOrClosedBlock();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else if ((_tokenSet_23.member(LA(1))) && (_tokenSet_11.member(LA(2))) && (_tokenSet_26.member(LA(3)))) {
						statement();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					statement_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==LITERAL_synchronized) && (LA(2)==LPAREN)) {
					AST tmp24_AST = null;
					tmp24_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp24_AST);
					match(LITERAL_synchronized);
					match(LPAREN);
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					match(RPAREN);
					nlsWarn();
					compoundStatement();
					astFactory.addASTChild(currentAST, returnAST);
					statement_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_27.member(LA(1))) && (_tokenSet_28.member(LA(2))) && (_tokenSet_7.member(LA(3)))) {
					conditionalExpression();
					astFactory.addASTChild(currentAST, returnAST);
					statement_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}}
			returnAST = statement_AST;
		}
		
/** A Groovy script or simple expression.  Can be anything legal inside {...}. */
	public final void snippetUnit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST snippetUnit_AST = null;
		
		blockBody();
		astFactory.addASTChild(currentAST, returnAST);
		snippetUnit_AST = (AST)currentAST.root;
		returnAST = snippetUnit_AST;
	}
	
/** A block body is either a single expression, with no additional newlines or separators,
  * or else the usual parade of zero or more statements.
  */
	public final void blockBody() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST blockBody_AST = null;
		
		boolean synPredMatched876 = false;
		if (((_tokenSet_29.member(LA(1))) && (_tokenSet_2.member(LA(2))) && (_tokenSet_7.member(LA(3))))) {
			int _m876 = mark();
			synPredMatched876 = true;
			inputState.guessing++;
			try {
				{
				nls();
				assignmentExpression();
				nls();
				{
				switch ( LA(1)) {
				case RCURLY:
				{
					match(RCURLY);
					break;
				}
				case EOF:
				{
					match(Token.EOF_TYPE);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched876 = false;
			}
			rewind(_m876);
			inputState.guessing--;
		}
		if ( synPredMatched876 ) {
			nls();
			expressionNotBOR();
			astFactory.addASTChild(currentAST, returnAST);
			nls();
			blockBody_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_30.member(LA(1))) && (_tokenSet_7.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			nls();
			{
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case DEF:
			case LITERAL_import:
			case LITERAL_static:
			case AT:
			case LBRACK:
			case IDENT:
			case LPAREN:
			case LITERAL_class:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_any:
			case STAR:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			case LITERAL_this:
			case STRING_LITERAL:
			case LITERAL_if:
			case LITERAL_while:
			case LITERAL_with:
			case LITERAL_switch:
			case LITERAL_for:
			case LITERAL_return:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_throw:
			case LITERAL_assert:
			case INC:
			case DEC:
			case LITERAL_try:
			case PLUS:
			case MINUS:
			case BNOT:
			case LNOT:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			case STRING_CTOR_START:
			case LITERAL_new:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			{
				statement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case RCURLY:
			case SEMI:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop880:
			do {
				if ((LA(1)==SEMI||LA(1)==NLS)) {
					sep();
					{
					switch ( LA(1)) {
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case DEF:
					case LITERAL_import:
					case LITERAL_static:
					case AT:
					case LBRACK:
					case IDENT:
					case LPAREN:
					case LITERAL_class:
					case LITERAL_super:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_any:
					case STAR:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					case LCURLY:
					case LITERAL_this:
					case STRING_LITERAL:
					case LITERAL_if:
					case LITERAL_while:
					case LITERAL_with:
					case LITERAL_switch:
					case LITERAL_for:
					case LITERAL_return:
					case LITERAL_break:
					case LITERAL_continue:
					case LITERAL_throw:
					case LITERAL_assert:
					case INC:
					case DEC:
					case LITERAL_try:
					case PLUS:
					case MINUS:
					case BNOT:
					case LNOT:
					case LITERAL_true:
					case LITERAL_false:
					case LITERAL_null:
					case STRING_CTOR_START:
					case LITERAL_new:
					case NUM_INT:
					case NUM_FLOAT:
					case NUM_LONG:
					case NUM_DOUBLE:
					case NUM_BIG_INT:
					case NUM_BIG_DECIMAL:
					{
						statement();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case RCURLY:
					case SEMI:
					case NLS:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop880;
				}
				
			} while (true);
			}
			blockBody_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = blockBody_AST;
	}
	
	public final void identifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifier_AST = null;
		
		AST tmp27_AST = null;
		tmp27_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp27_AST);
		match(IDENT);
		{
		_loop700:
		do {
			if ((LA(1)==DOT)) {
				AST tmp28_AST = null;
				tmp28_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp28_AST);
				match(DOT);
				nls();
				AST tmp29_AST = null;
				tmp29_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp29_AST);
				match(IDENT);
			}
			else {
				break _loop700;
			}
			
		} while (true);
		}
		identifier_AST = (AST)currentAST.root;
		returnAST = identifier_AST;
	}
	
	public final void importStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST importStatement_AST = null;
		Token  i = null;
		AST i_AST = null;
		boolean isStatic = false;
		
		i = LT(1);
		i_AST = astFactory.create(i);
		astFactory.makeASTRoot(currentAST, i_AST);
		match(LITERAL_import);
		if ( inputState.guessing==0 ) {
			i_AST.setType(IMPORT);
		}
		{
		switch ( LA(1)) {
		case LITERAL_static:
		{
			match(LITERAL_static);
			if ( inputState.guessing==0 ) {
				i_AST.setType(STATIC_IMPORT);
			}
			break;
		}
		case IDENT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		identifierStar();
		astFactory.addASTChild(currentAST, returnAST);
		importStatement_AST = (AST)currentAST.root;
		returnAST = importStatement_AST;
	}
	
	public final void identifierStar() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifierStar_AST = null;
		
		AST tmp31_AST = null;
		tmp31_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp31_AST);
		match(IDENT);
		{
		_loop703:
		do {
			if ((LA(1)==DOT) && (LA(2)==IDENT||LA(2)==NLS) && (_tokenSet_31.member(LA(3)))) {
				AST tmp32_AST = null;
				tmp32_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp32_AST);
				match(DOT);
				nls();
				AST tmp33_AST = null;
				tmp33_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp33_AST);
				match(IDENT);
			}
			else {
				break _loop703;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case DOT:
		{
			AST tmp34_AST = null;
			tmp34_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp34_AST);
			match(DOT);
			nls();
			AST tmp35_AST = null;
			tmp35_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp35_AST);
			match(STAR);
			break;
		}
		case LITERAL_as:
		{
			AST tmp36_AST = null;
			tmp36_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp36_AST);
			match(LITERAL_as);
			nls();
			AST tmp37_AST = null;
			tmp37_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp37_AST);
			match(IDENT);
			break;
		}
		case EOF:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		identifierStar_AST = (AST)currentAST.root;
		returnAST = identifierStar_AST;
	}
	
	protected final void typeDefinitionInternal(
		AST mods
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeDefinitionInternal_AST = null;
		AST cd_AST = null;
		AST id_AST = null;
		AST ed_AST = null;
		AST ad_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_class:
		{
			classDefinition(mods);
			cd_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = cd_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_interface:
		{
			interfaceDefinition(mods);
			id_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = id_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_enum:
		{
			enumDefinition(mods);
			ed_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = ed_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		case AT:
		{
			annotationDefinition(mods);
			ad_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = ad_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeDefinitionInternal_AST;
	}
	
	public final void classDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classDefinition_AST = null;
		AST tp_AST = null;
		AST sc_AST = null;
		AST ic_AST = null;
		AST cb_AST = null;
		AST prevCurrentClass = currentClass;
		
		match(LITERAL_class);
		AST tmp39_AST = null;
		tmp39_AST = astFactory.create(LT(1));
		match(IDENT);
		nls();
		if ( inputState.guessing==0 ) {
			currentClass = tmp39_AST;
		}
		{
		switch ( LA(1)) {
		case LT:
		{
			typeParameters();
			tp_AST = (AST)returnAST;
			break;
		}
		case LITERAL_extends:
		case LCURLY:
		case LITERAL_implements:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		superClassClause();
		sc_AST = (AST)returnAST;
		implementsClause();
		ic_AST = (AST)returnAST;
		classBlock();
		cb_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			classDefinition_AST = (AST)currentAST.root;
			classDefinition_AST = (AST)astFactory.make( (new ASTArray(7)).add(astFactory.create(CLASS_DEF,"CLASS_DEF")).add(modifiers).add(tmp39_AST).add(tp_AST).add(sc_AST).add(ic_AST).add(cb_AST));
			currentAST.root = classDefinition_AST;
			currentAST.child = classDefinition_AST!=null &&classDefinition_AST.getFirstChild()!=null ?
				classDefinition_AST.getFirstChild() : classDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		if ( inputState.guessing==0 ) {
			currentClass = prevCurrentClass;
		}
		returnAST = classDefinition_AST;
	}
	
	public final void interfaceDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceDefinition_AST = null;
		AST tp_AST = null;
		AST ie_AST = null;
		AST ib_AST = null;
		
		match(LITERAL_interface);
		AST tmp41_AST = null;
		tmp41_AST = astFactory.create(LT(1));
		match(IDENT);
		nls();
		{
		switch ( LA(1)) {
		case LT:
		{
			typeParameters();
			tp_AST = (AST)returnAST;
			break;
		}
		case LITERAL_extends:
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		interfaceExtends();
		ie_AST = (AST)returnAST;
		interfaceBlock();
		ib_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			interfaceDefinition_AST = (AST)currentAST.root;
			interfaceDefinition_AST = (AST)astFactory.make( (new ASTArray(6)).add(astFactory.create(INTERFACE_DEF,"INTERFACE_DEF")).add(modifiers).add(tmp41_AST).add(tp_AST).add(ie_AST).add(ib_AST));
			currentAST.root = interfaceDefinition_AST;
			currentAST.child = interfaceDefinition_AST!=null &&interfaceDefinition_AST.getFirstChild()!=null ?
				interfaceDefinition_AST.getFirstChild() : interfaceDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = interfaceDefinition_AST;
	}
	
	public final void enumDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumDefinition_AST = null;
		AST ic_AST = null;
		AST eb_AST = null;
		
		match(LITERAL_enum);
		AST tmp43_AST = null;
		tmp43_AST = astFactory.create(LT(1));
		match(IDENT);
		implementsClause();
		ic_AST = (AST)returnAST;
		enumBlock();
		eb_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			enumDefinition_AST = (AST)currentAST.root;
			enumDefinition_AST = (AST)astFactory.make( (new ASTArray(5)).add(astFactory.create(ENUM_DEF,"ENUM_DEF")).add(modifiers).add(tmp43_AST).add(ic_AST).add(eb_AST));
			currentAST.root = enumDefinition_AST;
			currentAST.child = enumDefinition_AST!=null &&enumDefinition_AST.getFirstChild()!=null ?
				enumDefinition_AST.getFirstChild() : enumDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = enumDefinition_AST;
	}
	
	public final void annotationDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationDefinition_AST = null;
		AST ab_AST = null;
		
		AST tmp44_AST = null;
		tmp44_AST = astFactory.create(LT(1));
		match(AT);
		match(LITERAL_interface);
		AST tmp46_AST = null;
		tmp46_AST = astFactory.create(LT(1));
		match(IDENT);
		annotationBlock();
		ab_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			annotationDefinition_AST = (AST)currentAST.root;
			annotationDefinition_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(ANNOTATION_DEF,"ANNOTATION_DEF")).add(modifiers).add(tmp46_AST).add(ab_AST));
			currentAST.root = annotationDefinition_AST;
			currentAST.child = annotationDefinition_AST!=null &&annotationDefinition_AST.getFirstChild()!=null ?
				annotationDefinition_AST.getFirstChild() : annotationDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = annotationDefinition_AST;
	}
	
/** A declaration is the creation of a reference or primitive-type variable,
 *  or (if arguments are present) of a method.
 *  Generically, this is called a 'variable' definition, even in the case of a class field or method.
 *  It may start with the modifiers and a mandatory keyword "def".
 *  It may also start with the modifiers and a capitalized type name.
 *  <p>
 *  AST effect: Create a separate Type/Var tree for each var in the var list.
 *  Must be guarded, as in (declarationStart) => declaration.
 */
	public final void declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST declaration_AST = null;
		AST m_AST = null;
		AST v_AST = null;
		AST t2_AST = null;
		AST v2_AST = null;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case AT:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			modifiers();
			m_AST = (AST)returnAST;
			break;
		}
		case DEF:
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case DEF:
		{
			match(DEF);
			nls();
			variableDefinitions(m_AST, null);
			v_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				declaration_AST = (AST)currentAST.root;
				declaration_AST = v_AST;
				currentAST.root = declaration_AST;
				currentAST.child = declaration_AST!=null &&declaration_AST.getFirstChild()!=null ?
					declaration_AST.getFirstChild() : declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			typeSpec(false);
			t2_AST = (AST)returnAST;
			variableDefinitions(m_AST,t2_AST);
			v2_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				declaration_AST = (AST)currentAST.root;
				declaration_AST = v2_AST;
				currentAST.root = declaration_AST;
				currentAST.child = declaration_AST!=null &&declaration_AST.getFirstChild()!=null ?
					declaration_AST.getFirstChild() : declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = declaration_AST;
	}
	
	public final void modifiers() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST modifiers_AST = null;
		
		{
		int _cnt707=0;
		_loop707:
		do {
			if ((_tokenSet_32.member(LA(1)))) {
				modifier();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else if (((LA(1)==AT) && (LA(2)==IDENT) && (_tokenSet_33.member(LA(3))))&&(LA(1)==AT && !LT(2).getText().equals("interface"))) {
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else {
				if ( _cnt707>=1 ) { break _loop707; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt707++;
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			modifiers_AST = (AST)currentAST.root;
			modifiers_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(MODIFIERS,"MODIFIERS")).add(modifiers_AST));
			currentAST.root = modifiers_AST;
			currentAST.child = modifiers_AST!=null &&modifiers_AST.getFirstChild()!=null ?
				modifiers_AST.getFirstChild() : modifiers_AST;
			currentAST.advanceChildToEnd();
		}
		modifiers_AST = (AST)currentAST.root;
		returnAST = modifiers_AST;
	}
	
/** The tail of a declaration.
  * Either v1, v2, ... (with possible initializers) or else m(args){body}.
  * The two arguments are the modifier list (if any) and the declaration head (if any).
  * The declaration head is the variable type, or (for a method) the return type.
  * If it is missing, then the variable type is taken from its initializer (if there is one).
  * Otherwise, the variable type defaults to 'any'.
  * DECIDE:  Method return types default to the type of the method body, as an expression.
  */
	public final void variableDefinitions(
		AST mods, AST t
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST variableDefinitions_AST = null;
		Token  id = null;
		AST id_AST = null;
		Token  qid = null;
		AST qid_AST = null;
		AST param_AST = null;
		AST tc_AST = null;
		AST mb_AST = null;
		
		if ((LA(1)==IDENT) && (_tokenSet_34.member(LA(2)))) {
			variableDeclarator(getASTFactory().dupTree(mods),
                                                   getASTFactory().dupTree(t));
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop819:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					nls();
					variableDeclarator(getASTFactory().dupTree(mods),
                                                           getASTFactory().dupTree(t));
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop819;
				}
				
			} while (true);
			}
			variableDefinitions_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (LA(2)==LPAREN)) {
			{
			switch ( LA(1)) {
			case IDENT:
			{
				id = LT(1);
				id_AST = astFactory.create(id);
				astFactory.addASTChild(currentAST, id_AST);
				match(IDENT);
				break;
			}
			case STRING_LITERAL:
			{
				qid = LT(1);
				qid_AST = astFactory.create(qid);
				astFactory.addASTChild(currentAST, qid_AST);
				match(STRING_LITERAL);
				if ( inputState.guessing==0 ) {
					qid_AST.setType(IDENT);
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LPAREN);
			parameterDeclarationList();
			param_AST = (AST)returnAST;
			match(RPAREN);
			{
			switch ( LA(1)) {
			case LITERAL_throws:
			{
				throwsClause();
				tc_AST = (AST)returnAST;
				break;
			}
			case EOF:
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case DEF:
			case LITERAL_static:
			case AT:
			case IDENT:
			case LITERAL_class:
			case LITERAL_interface:
			case LITERAL_enum:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_any:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			case RCURLY:
			case SEMI:
			case LITERAL_default:
			case LITERAL_else:
			case LITERAL_case:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			nlsWarn();
			{
			if ((LA(1)==LCURLY) && (_tokenSet_14.member(LA(2))) && (_tokenSet_35.member(LA(3)))) {
				openBlock();
				mb_AST = (AST)returnAST;
			}
			else if ((_tokenSet_36.member(LA(1))) && (_tokenSet_13.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				variableDefinitions_AST = (AST)currentAST.root;
				if (qid_AST != null)  id_AST = qid_AST;
				variableDefinitions_AST =
				(AST)astFactory.make( (new ASTArray(7)).add(astFactory.create(METHOD_DEF,"METHOD_DEF")).add(mods).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(t))).add(id_AST).add(param_AST).add(tc_AST).add(mb_AST));
				
				currentAST.root = variableDefinitions_AST;
				currentAST.child = variableDefinitions_AST!=null &&variableDefinitions_AST.getFirstChild()!=null ?
					variableDefinitions_AST.getFirstChild() : variableDefinitions_AST;
				currentAST.advanceChildToEnd();
			}
			variableDefinitions_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = variableDefinitions_AST;
	}
	
	public final void typeSpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeSpec_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			classTypeSpec(addImagNode);
			astFactory.addASTChild(currentAST, returnAST);
			typeSpec_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			builtInTypeSpec(addImagNode);
			astFactory.addASTChild(currentAST, returnAST);
			typeSpec_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeSpec_AST;
	}
	
/** A declaration with one declarator and no initialization, like a parameterDeclaration.

*TODO* We must also audit the various occurrences of warning
suppressions like "options { greedy = true; }".
*/
	public final void singleDeclarationNoInit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST singleDeclarationNoInit_AST = null;
		AST m_AST = null;
		AST v_AST = null;
		AST t2_AST = null;
		AST v2_AST = null;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case AT:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			modifiers();
			m_AST = (AST)returnAST;
			break;
		}
		case DEF:
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case DEF:
		{
			match(DEF);
			nls();
			singleVariable(m_AST, null);
			v_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				singleDeclarationNoInit_AST = (AST)currentAST.root;
				singleDeclarationNoInit_AST = v_AST;
				currentAST.root = singleDeclarationNoInit_AST;
				currentAST.child = singleDeclarationNoInit_AST!=null &&singleDeclarationNoInit_AST.getFirstChild()!=null ?
					singleDeclarationNoInit_AST.getFirstChild() : singleDeclarationNoInit_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			typeSpec(false);
			t2_AST = (AST)returnAST;
			singleVariable(m_AST,t2_AST);
			v2_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				singleDeclarationNoInit_AST = (AST)currentAST.root;
				singleDeclarationNoInit_AST = v2_AST;
				currentAST.root = singleDeclarationNoInit_AST;
				currentAST.child = singleDeclarationNoInit_AST!=null &&singleDeclarationNoInit_AST.getFirstChild()!=null ?
					singleDeclarationNoInit_AST.getFirstChild() : singleDeclarationNoInit_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = singleDeclarationNoInit_AST;
	}
	
/** Used in cases where a declaration cannot have commas, or ends with the "in" operator instead of '='. */
	public final void singleVariable(
		AST mods, AST t
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST singleVariable_AST = null;
		AST id_AST = null;
		
		variableName();
		id_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			singleVariable_AST = (AST)currentAST.root;
			singleVariable_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(VARIABLE_DEF,"VARIABLE_DEF")).add(mods).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(t))).add(id_AST));
			currentAST.root = singleVariable_AST;
			currentAST.child = singleVariable_AST!=null &&singleVariable_AST.getFirstChild()!=null ?
				singleVariable_AST.getFirstChild() : singleVariable_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = singleVariable_AST;
	}
	
	public final void singleDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST singleDeclaration_AST = null;
		AST sd_AST = null;
		
		singleDeclarationNoInit();
		sd_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			singleDeclaration_AST = (AST)currentAST.root;
			singleDeclaration_AST = sd_AST;
			currentAST.root = singleDeclaration_AST;
			currentAST.child = singleDeclaration_AST!=null &&singleDeclaration_AST.getFirstChild()!=null ?
				singleDeclaration_AST.getFirstChild() : singleDeclaration_AST;
			currentAST.advanceChildToEnd();
		}
		varInitializer();
		astFactory.addASTChild(currentAST, returnAST);
		singleDeclaration_AST = (AST)currentAST.root;
		returnAST = singleDeclaration_AST;
	}
	
	public final void varInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST varInitializer_AST = null;
		
		{
		switch ( LA(1)) {
		case ASSIGN:
		{
			AST tmp52_AST = null;
			tmp52_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp52_AST);
			match(ASSIGN);
			nls();
			initializer();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case RBRACK:
		case IDENT:
		case RPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case BOR:
		case COLON:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		varInitializer_AST = (AST)currentAST.root;
		returnAST = varInitializer_AST;
	}
	
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
	public final void declarationStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST declarationStart_AST = null;
		
		switch ( LA(1)) {
		case DEF:
		{
			match(DEF);
			break;
		}
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			modifier();
			break;
		}
		case AT:
		{
			match(AT);
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			{
			switch ( LA(1)) {
			case IDENT:
			{
				upperCaseIdent();
				break;
			}
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_any:
			{
				builtInType();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop659:
			do {
				if ((LA(1)==LBRACK)) {
					AST tmp55_AST = null;
					tmp55_AST = astFactory.create(LT(1));
					match(LBRACK);
					balancedTokens();
					AST tmp56_AST = null;
					tmp56_AST = astFactory.create(LT(1));
					match(RBRACK);
				}
				else {
					break _loop659;
				}
				
			} while (true);
			}
			AST tmp57_AST = null;
			tmp57_AST = astFactory.create(LT(1));
			match(IDENT);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = declarationStart_AST;
	}
	
	public final void modifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST modifier_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_private:
		{
			AST tmp58_AST = null;
			tmp58_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp58_AST);
			match(LITERAL_private);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_public:
		{
			AST tmp59_AST = null;
			tmp59_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp59_AST);
			match(LITERAL_public);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_protected:
		{
			AST tmp60_AST = null;
			tmp60_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp60_AST);
			match(LITERAL_protected);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_static:
		{
			AST tmp61_AST = null;
			tmp61_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp61_AST);
			match(LITERAL_static);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_transient:
		{
			AST tmp62_AST = null;
			tmp62_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp62_AST);
			match(LITERAL_transient);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case FINAL:
		{
			AST tmp63_AST = null;
			tmp63_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp63_AST);
			match(FINAL);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case ABSTRACT:
		{
			AST tmp64_AST = null;
			tmp64_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp64_AST);
			match(ABSTRACT);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_native:
		{
			AST tmp65_AST = null;
			tmp65_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp65_AST);
			match(LITERAL_native);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_threadsafe:
		{
			AST tmp66_AST = null;
			tmp66_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp66_AST);
			match(LITERAL_threadsafe);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_synchronized:
		{
			AST tmp67_AST = null;
			tmp67_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp67_AST);
			match(LITERAL_synchronized);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_volatile:
		{
			AST tmp68_AST = null;
			tmp68_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp68_AST);
			match(LITERAL_volatile);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case STRICTFP:
		{
			AST tmp69_AST = null;
			tmp69_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp69_AST);
			match(STRICTFP);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = modifier_AST;
	}
	
/** An IDENT token whose spelling is required to start with an uppercase letter.
 *  In the case of a simple statement {UpperID name} the identifier is taken to be a type name, not a command name.
 */
	public final void upperCaseIdent() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST upperCaseIdent_AST = null;
		
		if (!(isUpperCase(LT(1))))
		  throw new SemanticException("isUpperCase(LT(1))");
		AST tmp70_AST = null;
		tmp70_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp70_AST);
		match(IDENT);
		upperCaseIdent_AST = (AST)currentAST.root;
		returnAST = upperCaseIdent_AST;
	}
	
	public final void builtInType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST builtInType_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_void:
		{
			AST tmp71_AST = null;
			tmp71_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp71_AST);
			match(LITERAL_void);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_boolean:
		{
			AST tmp72_AST = null;
			tmp72_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp72_AST);
			match(LITERAL_boolean);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_byte:
		{
			AST tmp73_AST = null;
			tmp73_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp73_AST);
			match(LITERAL_byte);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_char:
		{
			AST tmp74_AST = null;
			tmp74_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp74_AST);
			match(LITERAL_char);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_short:
		{
			AST tmp75_AST = null;
			tmp75_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp75_AST);
			match(LITERAL_short);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_int:
		{
			AST tmp76_AST = null;
			tmp76_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp76_AST);
			match(LITERAL_int);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_float:
		{
			AST tmp77_AST = null;
			tmp77_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp77_AST);
			match(LITERAL_float);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_long:
		{
			AST tmp78_AST = null;
			tmp78_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp78_AST);
			match(LITERAL_long);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_double:
		{
			AST tmp79_AST = null;
			tmp79_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp79_AST);
			match(LITERAL_double);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_any:
		{
			AST tmp80_AST = null;
			tmp80_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp80_AST);
			match(LITERAL_any);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = builtInType_AST;
	}
	
	public final void balancedTokens() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST balancedTokens_AST = null;
		
		{
		_loop1101:
		do {
			if ((_tokenSet_37.member(LA(1)))) {
				balancedBrackets();
			}
			else if ((_tokenSet_38.member(LA(1)))) {
				{
				match(_tokenSet_38);
				}
			}
			else {
				break _loop1101;
			}
			
		} while (true);
		}
		returnAST = balancedTokens_AST;
	}
	
/** Used to look ahead for a constructor 
 */
	public final void constructorStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constructorStart_AST = null;
		Token  id = null;
		AST id_AST = null;
		
		{
		_loop662:
		do {
			if ((_tokenSet_32.member(LA(1)))) {
				modifier();
			}
			else {
				break _loop662;
			}
			
		} while (true);
		}
		id = LT(1);
		id_AST = astFactory.create(id);
		match(IDENT);
		if (!(isConstructorIdent(id)))
		  throw new SemanticException("isConstructorIdent(id)");
		nls();
		match(LPAREN);
		balancedTokens();
		match(RPAREN);
		returnAST = constructorStart_AST;
	}
	
/** Used only as a lookahead predicate for nested type declarations. */
	public final void typeDeclarationStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeDeclarationStart_AST = null;
		
		{
		_loop665:
		do {
			if ((_tokenSet_32.member(LA(1)))) {
				modifier();
			}
			else {
				break _loop665;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case LITERAL_class:
		{
			match(LITERAL_class);
			break;
		}
		case LITERAL_interface:
		{
			match(LITERAL_interface);
			break;
		}
		case LITERAL_enum:
		{
			match(LITERAL_enum);
			break;
		}
		case AT:
		{
			AST tmp87_AST = null;
			tmp87_AST = astFactory.create(LT(1));
			match(AT);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = typeDeclarationStart_AST;
	}
	
	public final void classTypeSpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classTypeSpec_AST = null;
		AST t_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		
		classOrInterfaceType(false);
		t_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop671:
		do {
			if ((LA(1)==LBRACK) && (LA(2)==RBRACK) && (_tokenSet_39.member(LA(3)))) {
				lb = LT(1);
				lb_AST = astFactory.create(lb);
				astFactory.makeASTRoot(currentAST, lb_AST);
				match(LBRACK);
				if ( inputState.guessing==0 ) {
					lb_AST.setType(ARRAY_DECLARATOR);
				}
				match(RBRACK);
			}
			else {
				break _loop671;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			classTypeSpec_AST = (AST)currentAST.root;
			
				if ( addImagNode ) {
							classTypeSpec_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(classTypeSpec_AST));
						}
					
			currentAST.root = classTypeSpec_AST;
			currentAST.child = classTypeSpec_AST!=null &&classTypeSpec_AST.getFirstChild()!=null ?
				classTypeSpec_AST.getFirstChild() : classTypeSpec_AST;
			currentAST.advanceChildToEnd();
		}
		classTypeSpec_AST = (AST)currentAST.root;
		returnAST = classTypeSpec_AST;
	}
	
	public final void builtInTypeSpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST builtInTypeSpec_AST = null;
		AST t_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		
		builtInType();
		t_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop695:
		do {
			if ((LA(1)==LBRACK)) {
				lb = LT(1);
				lb_AST = astFactory.create(lb);
				astFactory.makeASTRoot(currentAST, lb_AST);
				match(LBRACK);
				if ( inputState.guessing==0 ) {
					lb_AST.setType(ARRAY_DECLARATOR);
				}
				match(RBRACK);
			}
			else {
				break _loop695;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			builtInTypeSpec_AST = (AST)currentAST.root;
			
						if ( addImagNode ) {
							builtInTypeSpec_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(builtInTypeSpec_AST));
						}
					
			currentAST.root = builtInTypeSpec_AST;
			currentAST.child = builtInTypeSpec_AST!=null &&builtInTypeSpec_AST.getFirstChild()!=null ?
				builtInTypeSpec_AST.getFirstChild() : builtInTypeSpec_AST;
			currentAST.advanceChildToEnd();
		}
		builtInTypeSpec_AST = (AST)currentAST.root;
		returnAST = builtInTypeSpec_AST;
	}
	
	public final void classOrInterfaceType(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classOrInterfaceType_AST = null;
		
		AST tmp90_AST = null;
		tmp90_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp90_AST);
		match(IDENT);
		{
		switch ( LA(1)) {
		case LT:
		{
			typeArguments();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case LBRACK:
		case RBRACK:
		case IDENT:
		case LPAREN:
		case RPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case DOT:
		case QUESTION:
		case LITERAL_extends:
		case LITERAL_super:
		case COMMA:
		case GT:
		case SR:
		case BSR:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case STAR:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case ASSIGN:
		case LCURLY:
		case RCURLY:
		case BAND:
		case SEMI:
		case LITERAL_default:
		case LITERAL_implements:
		case LITERAL_this:
		case STRING_LITERAL:
		case TRIPLE_DOT:
		case BOR:
		case LOR:
		case COLON:
		case LITERAL_else:
		case LITERAL_case:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		case LAND:
		case BXOR:
		case REGEX_FIND:
		case REGEX_MATCH:
		case NOT_EQUAL:
		case EQUAL:
		case COMPARE_TO:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop676:
		do {
			if ((LA(1)==DOT) && (LA(2)==IDENT) && (_tokenSet_40.member(LA(3)))) {
				AST tmp91_AST = null;
				tmp91_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp91_AST);
				match(DOT);
				AST tmp92_AST = null;
				tmp92_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp92_AST);
				match(IDENT);
				{
				switch ( LA(1)) {
				case LT:
				{
					typeArguments();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case DEF:
				case LITERAL_static:
				case AT:
				case LBRACK:
				case RBRACK:
				case IDENT:
				case LPAREN:
				case RPAREN:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case DOT:
				case QUESTION:
				case LITERAL_extends:
				case LITERAL_super:
				case COMMA:
				case GT:
				case SR:
				case BSR:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_any:
				case STAR:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case ASSIGN:
				case LCURLY:
				case RCURLY:
				case BAND:
				case SEMI:
				case LITERAL_default:
				case LITERAL_implements:
				case LITERAL_this:
				case STRING_LITERAL:
				case TRIPLE_DOT:
				case BOR:
				case LOR:
				case COLON:
				case LITERAL_else:
				case LITERAL_case:
				case PLUS_ASSIGN:
				case MINUS_ASSIGN:
				case STAR_ASSIGN:
				case DIV_ASSIGN:
				case MOD_ASSIGN:
				case SR_ASSIGN:
				case BSR_ASSIGN:
				case SL_ASSIGN:
				case BAND_ASSIGN:
				case BXOR_ASSIGN:
				case BOR_ASSIGN:
				case STAR_STAR_ASSIGN:
				case LAND:
				case BXOR:
				case REGEX_FIND:
				case REGEX_MATCH:
				case NOT_EQUAL:
				case EQUAL:
				case COMPARE_TO:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop676;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			classOrInterfaceType_AST = (AST)currentAST.root;
			
						if ( addImagNode ) {
							classOrInterfaceType_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(classOrInterfaceType_AST));
						}
					
			currentAST.root = classOrInterfaceType_AST;
			currentAST.child = classOrInterfaceType_AST!=null &&classOrInterfaceType_AST.getFirstChild()!=null ?
				classOrInterfaceType_AST.getFirstChild() : classOrInterfaceType_AST;
			currentAST.advanceChildToEnd();
		}
		classOrInterfaceType_AST = (AST)currentAST.root;
		returnAST = classOrInterfaceType_AST;
	}
	
	public final void typeArguments() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArguments_AST = null;
		int currentLtLevel = 0;
		
		if ( inputState.guessing==0 ) {
			currentLtLevel = ltCounter;
		}
		match(LT);
		if ( inputState.guessing==0 ) {
			ltCounter++;
		}
		typeArgument();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop686:
		do {
			if (((LA(1)==COMMA) && (_tokenSet_41.member(LA(2))) && (_tokenSet_40.member(LA(3))))&&(inputState.guessing !=0 || ltCounter == currentLtLevel + 1)) {
				match(COMMA);
				typeArgument();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop686;
			}
			
		} while (true);
		}
		{
		if (((LA(1) >= GT && LA(1) <= BSR)) && (_tokenSet_39.member(LA(2))) && (_tokenSet_42.member(LA(3)))) {
			typeArgumentsOrParametersEnd();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_39.member(LA(1))) && (_tokenSet_42.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if (!((currentLtLevel != 0) || ltCounter == currentLtLevel))
		  throw new SemanticException("(currentLtLevel != 0) || ltCounter == currentLtLevel");
		if ( inputState.guessing==0 ) {
			typeArguments_AST = (AST)currentAST.root;
			typeArguments_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE_ARGUMENTS,"TYPE_ARGUMENTS")).add(typeArguments_AST));
			currentAST.root = typeArguments_AST;
			currentAST.child = typeArguments_AST!=null &&typeArguments_AST.getFirstChild()!=null ?
				typeArguments_AST.getFirstChild() : typeArguments_AST;
			currentAST.advanceChildToEnd();
		}
		typeArguments_AST = (AST)currentAST.root;
		returnAST = typeArguments_AST;
	}
	
	public final void typeArgumentSpec() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgumentSpec_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			classTypeSpec(true);
			astFactory.addASTChild(currentAST, returnAST);
			typeArgumentSpec_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			builtInTypeArraySpec(true);
			astFactory.addASTChild(currentAST, returnAST);
			typeArgumentSpec_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeArgumentSpec_AST;
	}
	
	public final void builtInTypeArraySpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST builtInTypeArraySpec_AST = null;
		AST t_AST = null;
		AST ata_AST = null;
		
		builtInType();
		t_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((LA(1)==LBRACK) && (_tokenSet_43.member(LA(2))) && (_tokenSet_44.member(LA(3)))) {
			arrayOrTypeArgs(t_AST);
			ata_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_39.member(LA(1))) && (_tokenSet_42.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			builtInTypeArraySpec_AST = (AST)currentAST.root;
			
						if (ata_AST != null)  builtInTypeArraySpec_AST = ata_AST;
						if ( addImagNode ) {
							builtInTypeArraySpec_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(builtInTypeArraySpec_AST));
						}
					
			currentAST.root = builtInTypeArraySpec_AST;
			currentAST.child = builtInTypeArraySpec_AST!=null &&builtInTypeArraySpec_AST.getFirstChild()!=null ?
				builtInTypeArraySpec_AST.getFirstChild() : builtInTypeArraySpec_AST;
			currentAST.advanceChildToEnd();
		}
		builtInTypeArraySpec_AST = (AST)currentAST.root;
		returnAST = builtInTypeArraySpec_AST;
	}
	
	public final void typeArgument() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgument_AST = null;
		
		{
		switch ( LA(1)) {
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			typeArgumentSpec();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case QUESTION:
		{
			wildcardType();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			typeArgument_AST = (AST)currentAST.root;
			typeArgument_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE_ARGUMENT,"TYPE_ARGUMENT")).add(typeArgument_AST));
			currentAST.root = typeArgument_AST;
			currentAST.child = typeArgument_AST!=null &&typeArgument_AST.getFirstChild()!=null ?
				typeArgument_AST.getFirstChild() : typeArgument_AST;
			currentAST.advanceChildToEnd();
		}
		typeArgument_AST = (AST)currentAST.root;
		returnAST = typeArgument_AST;
	}
	
	public final void wildcardType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST wildcardType_AST = null;
		Token  q = null;
		AST q_AST = null;
		
		q = LT(1);
		q_AST = astFactory.create(q);
		astFactory.makeASTRoot(currentAST, q_AST);
		match(QUESTION);
		if ( inputState.guessing==0 ) {
			q_AST.setType(WILDCARD_TYPE);
		}
		{
		boolean synPredMatched683 = false;
		if (((LA(1)==LITERAL_extends||LA(1)==LITERAL_super) && (LA(2)==IDENT) && (_tokenSet_40.member(LA(3))))) {
			int _m683 = mark();
			synPredMatched683 = true;
			inputState.guessing++;
			try {
				{
				switch ( LA(1)) {
				case LITERAL_extends:
				{
					match(LITERAL_extends);
					break;
				}
				case LITERAL_super:
				{
					match(LITERAL_super);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched683 = false;
			}
			rewind(_m683);
			inputState.guessing--;
		}
		if ( synPredMatched683 ) {
			typeArgumentBounds();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_39.member(LA(1))) && (_tokenSet_45.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		wildcardType_AST = (AST)currentAST.root;
		returnAST = wildcardType_AST;
	}
	
	public final void typeArgumentBounds() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgumentBounds_AST = null;
		boolean isUpperBounds = false;
		
		{
		switch ( LA(1)) {
		case LITERAL_extends:
		{
			match(LITERAL_extends);
			if ( inputState.guessing==0 ) {
				isUpperBounds=true;
			}
			break;
		}
		case LITERAL_super:
		{
			match(LITERAL_super);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		classOrInterfaceType(false);
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			typeArgumentBounds_AST = (AST)currentAST.root;
			
						if (isUpperBounds)
						{
							typeArgumentBounds_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS")).add(typeArgumentBounds_AST));
						}
						else
						{
							typeArgumentBounds_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE_LOWER_BOUNDS,"TYPE_LOWER_BOUNDS")).add(typeArgumentBounds_AST));
						}
					
			currentAST.root = typeArgumentBounds_AST;
			currentAST.child = typeArgumentBounds_AST!=null &&typeArgumentBounds_AST.getFirstChild()!=null ?
				typeArgumentBounds_AST.getFirstChild() : typeArgumentBounds_AST;
			currentAST.advanceChildToEnd();
		}
		typeArgumentBounds_AST = (AST)currentAST.root;
		returnAST = typeArgumentBounds_AST;
	}
	
	protected final void typeArgumentsOrParametersEnd() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgumentsOrParametersEnd_AST = null;
		
		switch ( LA(1)) {
		case GT:
		{
			match(GT);
			if ( inputState.guessing==0 ) {
				ltCounter-=1;
			}
			typeArgumentsOrParametersEnd_AST = (AST)currentAST.root;
			break;
		}
		case SR:
		{
			match(SR);
			if ( inputState.guessing==0 ) {
				ltCounter-=2;
			}
			typeArgumentsOrParametersEnd_AST = (AST)currentAST.root;
			break;
		}
		case BSR:
		{
			match(BSR);
			if ( inputState.guessing==0 ) {
				ltCounter-=3;
			}
			typeArgumentsOrParametersEnd_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeArgumentsOrParametersEnd_AST;
	}
	
/** An expression may be followed by [...].
 *  Unlike Java, these brackets may contain a general argument list,
 *  which is passed to the array element operator, which can make of it what it wants.
 *  The brackets may also be empty, as in T[].  This is how Groovy names array types.
 *  <p>Returned AST is [INDEX_OP, indexee, ELIST].
 *
 * *TODO* (The arrayOrTypeArgs thing in 1.4 groovy.g is a placeholder which
 * anticipates the trouble of integrating Java 5 type arguments.)
 */
	public final void arrayOrTypeArgs(
		AST indexee
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arrayOrTypeArgs_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		boolean zz; /*ignore*/
		
		if ( inputState.guessing==0 ) {
			arrayOrTypeArgs_AST = (AST)currentAST.root;
			arrayOrTypeArgs_AST = indexee;
			currentAST.root = arrayOrTypeArgs_AST;
			currentAST.child = arrayOrTypeArgs_AST!=null &&arrayOrTypeArgs_AST.getFirstChild()!=null ?
				arrayOrTypeArgs_AST.getFirstChild() : arrayOrTypeArgs_AST;
			currentAST.advanceChildToEnd();
		}
		{
		int _cnt988=0;
		_loop988:
		do {
			if ((LA(1)==LBRACK) && (_tokenSet_43.member(LA(2))) && (_tokenSet_44.member(LA(3)))) {
				lb = LT(1);
				lb_AST = astFactory.create(lb);
				astFactory.makeASTRoot(currentAST, lb_AST);
				match(LBRACK);
				if ( inputState.guessing==0 ) {
					lb_AST.setType(INDEX_OP);
				}
				zz=argList();
				astFactory.addASTChild(currentAST, returnAST);
				match(RBRACK);
			}
			else {
				if ( _cnt988>=1 ) { break _loop988; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt988++;
		} while (true);
		}
		arrayOrTypeArgs_AST = (AST)currentAST.root;
		returnAST = arrayOrTypeArgs_AST;
	}
	
	public final void type() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST type_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			classOrInterfaceType(false);
			astFactory.addASTChild(currentAST, returnAST);
			type_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			builtInType();
			astFactory.addASTChild(currentAST, returnAST);
			type_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = type_AST;
	}
	
	public final void annotation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotation_AST = null;
		AST i_AST = null;
		AST args_AST = null;
		
		match(AT);
		identifier();
		i_AST = (AST)returnAST;
		{
		switch ( LA(1)) {
		case LPAREN:
		{
			match(LPAREN);
			{
			switch ( LA(1)) {
			case AT:
			case LBRACK:
			case IDENT:
			case LPAREN:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_any:
			case LCURLY:
			case LITERAL_this:
			case STRING_LITERAL:
			case INC:
			case DEC:
			case PLUS:
			case MINUS:
			case BNOT:
			case LNOT:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			case STRING_CTOR_START:
			case LITERAL_new:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			{
				annotationArguments();
				args_AST = (AST)returnAST;
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
			break;
		}
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_package:
		case LITERAL_static:
		case AT:
		case IDENT:
		case RPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case QUESTION:
		case LT:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case STAR:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			annotation_AST = (AST)currentAST.root;
			annotation_AST = (AST)astFactory.make( (new ASTArray(3)).add(astFactory.create(ANNOTATION,"ANNOTATION")).add(i_AST).add(args_AST));
			currentAST.root = annotation_AST;
			currentAST.child = annotation_AST!=null &&annotation_AST.getFirstChild()!=null ?
				annotation_AST.getFirstChild() : annotation_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = annotation_AST;
	}
	
	public final void annotationArguments() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationArguments_AST = null;
		
		if ((_tokenSet_46.member(LA(1))) && (_tokenSet_47.member(LA(2)))) {
			annotationMemberValueInitializer();
			astFactory.addASTChild(currentAST, returnAST);
			annotationArguments_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==IDENT) && (LA(2)==ASSIGN)) {
			anntotationMemberValuePairs();
			astFactory.addASTChild(currentAST, returnAST);
			annotationArguments_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = annotationArguments_AST;
	}
	
	public final void annotationMemberValueInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberValueInitializer_AST = null;
		
		if ((_tokenSet_27.member(LA(1))) && (_tokenSet_47.member(LA(2))) && (_tokenSet_48.member(LA(3)))) {
			conditionalExpression();
			astFactory.addASTChild(currentAST, returnAST);
			annotationMemberValueInitializer_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==AT)) {
			annotation();
			astFactory.addASTChild(currentAST, returnAST);
			nls();
			annotationMemberValueInitializer_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==LCURLY) && (_tokenSet_49.member(LA(2))) && (_tokenSet_47.member(LA(3)))) {
			annotationMemberArrayInitializer();
			astFactory.addASTChild(currentAST, returnAST);
			annotationMemberValueInitializer_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = annotationMemberValueInitializer_AST;
	}
	
	public final void anntotationMemberValuePairs() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST anntotationMemberValuePairs_AST = null;
		
		annotationMemberValuePair();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop718:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				annotationMemberValuePair();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop718;
			}
			
		} while (true);
		}
		anntotationMemberValuePairs_AST = (AST)currentAST.root;
		returnAST = anntotationMemberValuePairs_AST;
	}
	
	public final void annotationMemberValuePair() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberValuePair_AST = null;
		Token  i = null;
		AST i_AST = null;
		AST v_AST = null;
		
		i = LT(1);
		i_AST = astFactory.create(i);
		match(IDENT);
		match(ASSIGN);
		annotationMemberValueInitializer();
		v_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			annotationMemberValuePair_AST = (AST)currentAST.root;
			annotationMemberValuePair_AST = (AST)astFactory.make( (new ASTArray(3)).add(astFactory.create(ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR")).add(i_AST).add(v_AST));
			currentAST.root = annotationMemberValuePair_AST;
			currentAST.child = annotationMemberValuePair_AST!=null &&annotationMemberValuePair_AST.getFirstChild()!=null ?
				annotationMemberValuePair_AST.getFirstChild() : annotationMemberValuePair_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = annotationMemberValuePair_AST;
	}
	
	public final void conditionalExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conditionalExpression_AST = null;
		
		logicalOrExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case QUESTION:
		{
			AST tmp106_AST = null;
			tmp106_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp106_AST);
			match(QUESTION);
			nls();
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			match(COLON);
			nls();
			conditionalExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case RBRACK:
		case IDENT:
		case RPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case ASSIGN:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case BOR:
		case COLON:
		case LITERAL_else:
		case LITERAL_case:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		conditionalExpression_AST = (AST)currentAST.root;
		returnAST = conditionalExpression_AST;
	}
	
	public final void annotationMemberArrayInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberArrayInitializer_AST = null;
		Token  lc = null;
		AST lc_AST = null;
		
		lc = LT(1);
		lc_AST = astFactory.create(lc);
		astFactory.makeASTRoot(currentAST, lc_AST);
		match(LCURLY);
		if ( inputState.guessing==0 ) {
			lc_AST.setType(ANNOTATION_ARRAY_INIT);
		}
		{
		switch ( LA(1)) {
		case AT:
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case INC:
		case DEC:
		case PLUS:
		case MINUS:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			annotationMemberArrayValueInitializer();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop724:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_46.member(LA(2)))) {
					match(COMMA);
					annotationMemberArrayValueInitializer();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop724;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RCURLY);
		annotationMemberArrayInitializer_AST = (AST)currentAST.root;
		returnAST = annotationMemberArrayInitializer_AST;
	}
	
	public final void annotationMemberArrayValueInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberArrayValueInitializer_AST = null;
		
		switch ( LA(1)) {
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case INC:
		case DEC:
		case PLUS:
		case MINUS:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			conditionalExpression();
			astFactory.addASTChild(currentAST, returnAST);
			annotationMemberArrayValueInitializer_AST = (AST)currentAST.root;
			break;
		}
		case AT:
		{
			annotation();
			astFactory.addASTChild(currentAST, returnAST);
			nls();
			annotationMemberArrayValueInitializer_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = annotationMemberArrayValueInitializer_AST;
	}
	
	public final void superClassClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST superClassClause_AST = null;
		AST c_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_extends:
		{
			match(LITERAL_extends);
			classOrInterfaceType(false);
			c_AST = (AST)returnAST;
			break;
		}
		case LCURLY:
		case LITERAL_implements:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			superClassClause_AST = (AST)currentAST.root;
			superClassClause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE")).add(c_AST));
			currentAST.root = superClassClause_AST;
			currentAST.child = superClassClause_AST!=null &&superClassClause_AST.getFirstChild()!=null ?
				superClassClause_AST.getFirstChild() : superClassClause_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = superClassClause_AST;
	}
	
	public final void typeParameters() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeParameters_AST = null;
		int currentLtLevel = 0;
		
		if ( inputState.guessing==0 ) {
			currentLtLevel = ltCounter;
		}
		match(LT);
		if ( inputState.guessing==0 ) {
			ltCounter++;
		}
		typeParameter();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop737:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				typeParameter();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop737;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case GT:
		case SR:
		case BSR:
		{
			typeArgumentsOrParametersEnd();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case IDENT:
		case LITERAL_extends:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LCURLY:
		case LITERAL_implements:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if (!((currentLtLevel != 0) || ltCounter == currentLtLevel))
		  throw new SemanticException("(currentLtLevel != 0) || ltCounter == currentLtLevel");
		if ( inputState.guessing==0 ) {
			typeParameters_AST = (AST)currentAST.root;
			typeParameters_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE_PARAMETERS,"TYPE_PARAMETERS")).add(typeParameters_AST));
			currentAST.root = typeParameters_AST;
			currentAST.child = typeParameters_AST!=null &&typeParameters_AST.getFirstChild()!=null ?
				typeParameters_AST.getFirstChild() : typeParameters_AST;
			currentAST.advanceChildToEnd();
		}
		typeParameters_AST = (AST)currentAST.root;
		returnAST = typeParameters_AST;
	}
	
	public final void implementsClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST implementsClause_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_implements:
		{
			i = LT(1);
			i_AST = astFactory.create(i);
			match(LITERAL_implements);
			nls();
			classOrInterfaceType(false);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop791:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					nls();
					classOrInterfaceType(false);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop791;
				}
				
			} while (true);
			}
			nls();
			break;
		}
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			implementsClause_AST = (AST)currentAST.root;
			implementsClause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE")).add(implementsClause_AST));
			currentAST.root = implementsClause_AST;
			currentAST.child = implementsClause_AST!=null &&implementsClause_AST.getFirstChild()!=null ?
				implementsClause_AST.getFirstChild() : implementsClause_AST;
			currentAST.advanceChildToEnd();
		}
		implementsClause_AST = (AST)currentAST.root;
		returnAST = implementsClause_AST;
	}
	
	public final void classBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classBlock_AST = null;
		
		match(LCURLY);
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case IDENT:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		{
			classField();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop749:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case DEF:
				case LITERAL_static:
				case AT:
				case IDENT:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_any:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				{
					classField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop749;
			}
			
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			classBlock_AST = (AST)currentAST.root;
			classBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OBJBLOCK,"OBJBLOCK")).add(classBlock_AST));
			currentAST.root = classBlock_AST;
			currentAST.child = classBlock_AST!=null &&classBlock_AST.getFirstChild()!=null ?
				classBlock_AST.getFirstChild() : classBlock_AST;
			currentAST.advanceChildToEnd();
		}
		classBlock_AST = (AST)currentAST.root;
		returnAST = classBlock_AST;
	}
	
	public final void interfaceExtends() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceExtends_AST = null;
		Token  e = null;
		AST e_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_extends:
		{
			e = LT(1);
			e_AST = astFactory.create(e);
			match(LITERAL_extends);
			nls();
			classOrInterfaceType(false);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop787:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					nls();
					classOrInterfaceType(false);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop787;
				}
				
			} while (true);
			}
			nls();
			break;
		}
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			interfaceExtends_AST = (AST)currentAST.root;
			interfaceExtends_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE")).add(interfaceExtends_AST));
			currentAST.root = interfaceExtends_AST;
			currentAST.child = interfaceExtends_AST!=null &&interfaceExtends_AST.getFirstChild()!=null ?
				interfaceExtends_AST.getFirstChild() : interfaceExtends_AST;
			currentAST.advanceChildToEnd();
		}
		interfaceExtends_AST = (AST)currentAST.root;
		returnAST = interfaceExtends_AST;
	}
	
	public final void interfaceBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceBlock_AST = null;
		
		match(LCURLY);
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case IDENT:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			interfaceField();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop754:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case DEF:
				case LITERAL_static:
				case AT:
				case IDENT:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_any:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				{
					interfaceField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop754;
			}
			
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			interfaceBlock_AST = (AST)currentAST.root;
			interfaceBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OBJBLOCK,"OBJBLOCK")).add(interfaceBlock_AST));
			currentAST.root = interfaceBlock_AST;
			currentAST.child = interfaceBlock_AST!=null &&interfaceBlock_AST.getFirstChild()!=null ?
				interfaceBlock_AST.getFirstChild() : interfaceBlock_AST;
			currentAST.advanceChildToEnd();
		}
		interfaceBlock_AST = (AST)currentAST.root;
		returnAST = interfaceBlock_AST;
	}
	
	public final void enumBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumBlock_AST = null;
		
		match(LCURLY);
		{
		switch ( LA(1)) {
		case AT:
		case IDENT:
		{
			enumConstant();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop763:
			do {
				if ((LA(1)==COMMA) && (LA(2)==AT||LA(2)==IDENT) && (_tokenSet_50.member(LA(3)))) {
					match(COMMA);
					enumConstant();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop763;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case RCURLY:
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case RCURLY:
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case SEMI:
		{
			match(SEMI);
			{
			_loop767:
			do {
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case DEF:
				case LITERAL_static:
				case AT:
				case IDENT:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_any:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				{
					classField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case SEMI:
				{
					match(SEMI);
					break;
				}
				default:
				{
					break _loop767;
				}
				}
			} while (true);
			}
			break;
		}
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			enumBlock_AST = (AST)currentAST.root;
			enumBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OBJBLOCK,"OBJBLOCK")).add(enumBlock_AST));
			currentAST.root = enumBlock_AST;
			currentAST.child = enumBlock_AST!=null &&enumBlock_AST.getFirstChild()!=null ?
				enumBlock_AST.getFirstChild() : enumBlock_AST;
			currentAST.advanceChildToEnd();
		}
		enumBlock_AST = (AST)currentAST.root;
		returnAST = enumBlock_AST;
	}
	
	public final void annotationBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationBlock_AST = null;
		
		match(LCURLY);
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case AT:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			annotationField();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop759:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_static:
				case AT:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				{
					annotationField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop759;
			}
			
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			annotationBlock_AST = (AST)currentAST.root;
			annotationBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OBJBLOCK,"OBJBLOCK")).add(annotationBlock_AST));
			currentAST.root = annotationBlock_AST;
			currentAST.child = annotationBlock_AST!=null &&annotationBlock_AST.getFirstChild()!=null ?
				annotationBlock_AST.getFirstChild() : annotationBlock_AST;
			currentAST.advanceChildToEnd();
		}
		annotationBlock_AST = (AST)currentAST.root;
		returnAST = annotationBlock_AST;
	}
	
	public final void typeParameter() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeParameter_AST = null;
		Token  id = null;
		AST id_AST = null;
		
		{
		id = LT(1);
		id_AST = astFactory.create(id);
		astFactory.addASTChild(currentAST, id_AST);
		match(IDENT);
		}
		{
		if ((LA(1)==LITERAL_extends) && (LA(2)==IDENT) && (_tokenSet_51.member(LA(3)))) {
			typeParameterBounds();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_52.member(LA(1))) && (_tokenSet_53.member(LA(2))) && (_tokenSet_54.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			typeParameter_AST = (AST)currentAST.root;
			typeParameter_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE_PARAMETER,"TYPE_PARAMETER")).add(typeParameter_AST));
			currentAST.root = typeParameter_AST;
			currentAST.child = typeParameter_AST!=null &&typeParameter_AST.getFirstChild()!=null ?
				typeParameter_AST.getFirstChild() : typeParameter_AST;
			currentAST.advanceChildToEnd();
		}
		typeParameter_AST = (AST)currentAST.root;
		returnAST = typeParameter_AST;
	}
	
	public final void typeParameterBounds() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeParameterBounds_AST = null;
		
		match(LITERAL_extends);
		classOrInterfaceType(false);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop744:
		do {
			if ((LA(1)==BAND)) {
				match(BAND);
				classOrInterfaceType(false);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop744;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			typeParameterBounds_AST = (AST)currentAST.root;
			typeParameterBounds_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS")).add(typeParameterBounds_AST));
			currentAST.root = typeParameterBounds_AST;
			currentAST.child = typeParameterBounds_AST!=null &&typeParameterBounds_AST.getFirstChild()!=null ?
				typeParameterBounds_AST.getFirstChild() : typeParameterBounds_AST;
			currentAST.advanceChildToEnd();
		}
		typeParameterBounds_AST = (AST)currentAST.root;
		returnAST = typeParameterBounds_AST;
	}
	
	public final void classField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classField_AST = null;
		AST mc_AST = null;
		AST ctor_AST = null;
		AST d_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		AST s3_AST = null;
		AST s4_AST = null;
		
		boolean synPredMatched794 = false;
		if (((_tokenSet_55.member(LA(1))) && (_tokenSet_56.member(LA(2))) && (_tokenSet_57.member(LA(3))))) {
			int _m794 = mark();
			synPredMatched794 = true;
			inputState.guessing++;
			try {
				{
				constructorStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched794 = false;
			}
			rewind(_m794);
			inputState.guessing--;
		}
		if ( synPredMatched794 ) {
			{
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case AT:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			{
				modifiers();
				mc_AST = (AST)returnAST;
				break;
			}
			case IDENT:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			constructorDefinition(mc_AST);
			ctor_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				classField_AST = (AST)currentAST.root;
				classField_AST = ctor_AST;
				currentAST.root = classField_AST;
				currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
					classField_AST.getFirstChild() : classField_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			boolean synPredMatched797 = false;
			if (((_tokenSet_15.member(LA(1))) && (_tokenSet_16.member(LA(2))) && (_tokenSet_58.member(LA(3))))) {
				int _m797 = mark();
				synPredMatched797 = true;
				inputState.guessing++;
				try {
					{
					declarationStart();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched797 = false;
				}
				rewind(_m797);
				inputState.guessing--;
			}
			if ( synPredMatched797 ) {
				declaration();
				d_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					classField_AST = (AST)currentAST.root;
					classField_AST = d_AST;
					currentAST.root = classField_AST;
					currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
						classField_AST.getFirstChild() : classField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched799 = false;
				if (((_tokenSet_59.member(LA(1))) && (_tokenSet_60.member(LA(2))) && (_tokenSet_61.member(LA(3))))) {
					int _m799 = mark();
					synPredMatched799 = true;
					inputState.guessing++;
					try {
						{
						typeDeclarationStart();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched799 = false;
					}
					rewind(_m799);
					inputState.guessing--;
				}
				if ( synPredMatched799 ) {
					{
					if ((_tokenSet_62.member(LA(1))) && (_tokenSet_60.member(LA(2))) && (_tokenSet_63.member(LA(3)))) {
						modifiers();
						mods_AST = (AST)returnAST;
					}
					else if ((_tokenSet_64.member(LA(1))) && (LA(2)==IDENT||LA(2)==LITERAL_interface) && (_tokenSet_65.member(LA(3)))) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					{
					typeDefinitionInternal(mods_AST);
					td_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						classField_AST = (AST)currentAST.root;
						classField_AST = td_AST;
						currentAST.root = classField_AST;
						currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
							classField_AST.getFirstChild() : classField_AST;
						currentAST.advanceChildToEnd();
					}
					}
				}
				else if ((LA(1)==LITERAL_static) && (LA(2)==LCURLY)) {
					match(LITERAL_static);
					compoundStatement();
					s3_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						classField_AST = (AST)currentAST.root;
						classField_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(STATIC_INIT,"STATIC_INIT")).add(s3_AST));
						currentAST.root = classField_AST;
						currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
							classField_AST.getFirstChild() : classField_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else if ((LA(1)==LCURLY)) {
					compoundStatement();
					s4_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						classField_AST = (AST)currentAST.root;
						classField_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(INSTANCE_INIT,"INSTANCE_INIT")).add(s4_AST));
						currentAST.root = classField_AST;
						currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
							classField_AST.getFirstChild() : classField_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}}
				returnAST = classField_AST;
			}
			
	public final void interfaceField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceField_AST = null;
		AST d_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		
		boolean synPredMatched804 = false;
		if (((_tokenSet_15.member(LA(1))) && (_tokenSet_16.member(LA(2))) && (_tokenSet_66.member(LA(3))))) {
			int _m804 = mark();
			synPredMatched804 = true;
			inputState.guessing++;
			try {
				{
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched804 = false;
			}
			rewind(_m804);
			inputState.guessing--;
		}
		if ( synPredMatched804 ) {
			declaration();
			d_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				interfaceField_AST = (AST)currentAST.root;
				interfaceField_AST = d_AST;
				currentAST.root = interfaceField_AST;
				currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
					interfaceField_AST.getFirstChild() : interfaceField_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			boolean synPredMatched806 = false;
			if (((_tokenSet_59.member(LA(1))) && (_tokenSet_60.member(LA(2))) && (_tokenSet_61.member(LA(3))))) {
				int _m806 = mark();
				synPredMatched806 = true;
				inputState.guessing++;
				try {
					{
					typeDeclarationStart();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched806 = false;
				}
				rewind(_m806);
				inputState.guessing--;
			}
			if ( synPredMatched806 ) {
				{
				if ((_tokenSet_62.member(LA(1))) && (_tokenSet_60.member(LA(2))) && (_tokenSet_63.member(LA(3)))) {
					modifiers();
					mods_AST = (AST)returnAST;
				}
				else if ((_tokenSet_64.member(LA(1))) && (LA(2)==IDENT||LA(2)==LITERAL_interface) && (_tokenSet_65.member(LA(3)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				typeDefinitionInternal(mods_AST);
				td_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					interfaceField_AST = (AST)currentAST.root;
					interfaceField_AST = td_AST;
					currentAST.root = interfaceField_AST;
					currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
						interfaceField_AST.getFirstChild() : interfaceField_AST;
					currentAST.advanceChildToEnd();
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			returnAST = interfaceField_AST;
		}
		
	public final void annotationField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationField_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		AST t_AST = null;
		Token  i = null;
		AST i_AST = null;
		AST amvi_AST = null;
		AST v_AST = null;
		
		modifiers();
		mods_AST = (AST)returnAST;
		{
		switch ( LA(1)) {
		case AT:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		{
			typeDefinitionInternal(mods_AST);
			td_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				annotationField_AST = (AST)currentAST.root;
				annotationField_AST = td_AST;
				currentAST.root = annotationField_AST;
				currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
					annotationField_AST.getFirstChild() : annotationField_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			typeSpec(false);
			t_AST = (AST)returnAST;
			{
			if ((LA(1)==IDENT) && (LA(2)==LPAREN) && (LA(3)==RPAREN)) {
				i = LT(1);
				i_AST = astFactory.create(i);
				match(IDENT);
				match(LPAREN);
				match(RPAREN);
				{
				switch ( LA(1)) {
				case LITERAL_default:
				{
					match(LITERAL_default);
					annotationMemberValueInitializer();
					amvi_AST = (AST)returnAST;
					break;
				}
				case SEMI:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp134_AST = null;
				tmp134_AST = astFactory.create(LT(1));
				match(SEMI);
				if ( inputState.guessing==0 ) {
					annotationField_AST = (AST)currentAST.root;
					annotationField_AST =
										(AST)astFactory.make( (new ASTArray(5)).add(astFactory.create(ANNOTATION_FIELD_DEF,"ANNOTATION_FIELD_DEF")).add(mods_AST).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(t_AST))).add(i_AST).add(amvi_AST));
					currentAST.root = annotationField_AST;
					currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
						annotationField_AST.getFirstChild() : annotationField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (_tokenSet_67.member(LA(2))) && (_tokenSet_68.member(LA(3)))) {
				variableDefinitions(mods_AST,t_AST);
				v_AST = (AST)returnAST;
				AST tmp135_AST = null;
				tmp135_AST = astFactory.create(LT(1));
				match(SEMI);
				if ( inputState.guessing==0 ) {
					annotationField_AST = (AST)currentAST.root;
					annotationField_AST = v_AST;
					currentAST.root = annotationField_AST;
					currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
						annotationField_AST.getFirstChild() : annotationField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = annotationField_AST;
	}
	
	public final void enumConstant() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstant_AST = null;
		AST an_AST = null;
		Token  i = null;
		AST i_AST = null;
		AST a_AST = null;
		AST b_AST = null;
		boolean zz; /*ignored*/
		
		annotations();
		an_AST = (AST)returnAST;
		i = LT(1);
		i_AST = astFactory.create(i);
		match(IDENT);
		{
		switch ( LA(1)) {
		case LPAREN:
		{
			match(LPAREN);
			zz=argList();
			a_AST = (AST)returnAST;
			match(RPAREN);
			break;
		}
		case COMMA:
		case LCURLY:
		case RCURLY:
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case LCURLY:
		{
			enumConstantBlock();
			b_AST = (AST)returnAST;
			break;
		}
		case COMMA:
		case RCURLY:
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			enumConstant_AST = (AST)currentAST.root;
			enumConstant_AST = (AST)astFactory.make( (new ASTArray(5)).add(astFactory.create(ENUM_CONSTANT_DEF,"ENUM_CONSTANT_DEF")).add(an_AST).add(i_AST).add(a_AST).add(b_AST));
			currentAST.root = enumConstant_AST;
			currentAST.child = enumConstant_AST!=null &&enumConstant_AST.getFirstChild()!=null ?
				enumConstant_AST.getFirstChild() : enumConstant_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = enumConstant_AST;
	}
	
	public final boolean  argList() throws RecognitionException, TokenStreamException {
		boolean hasLabels = false;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argList_AST = null;
		boolean hl2;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_class:
		case QUESTION:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case STAR:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case INC:
		case DEC:
		case PLUS:
		case MINUS:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			hasLabels=argument();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop1078:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_69.member(LA(2))) && (_tokenSet_70.member(LA(3)))) {
					match(COMMA);
					hl2=argument();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						hasLabels |= hl2;
					}
				}
				else {
					break _loop1078;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				argList_AST = (AST)currentAST.root;
				argList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ELIST,"ELIST")).add(argList_AST));
				currentAST.root = argList_AST;
				currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
					argList_AST.getFirstChild() : argList_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case RBRACK:
		case RPAREN:
		case COMMA:
		{
			if ( inputState.guessing==0 ) {
				argList_AST = (AST)currentAST.root;
				argList_AST = astFactory.create(ELIST,"ELIST");
				currentAST.root = argList_AST;
				currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
					argList_AST.getFirstChild() : argList_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case COMMA:
		{
			match(COMMA);
			break;
		}
		case RBRACK:
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		argList_AST = (AST)currentAST.root;
		returnAST = argList_AST;
		return hasLabels;
	}
	
	public final void enumConstantBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstantBlock_AST = null;
		
		match(LCURLY);
		{
		_loop777:
		do {
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case AT:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			{
				enumConstantField();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SEMI:
			{
				match(SEMI);
				break;
			}
			default:
			{
				break _loop777;
			}
			}
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			enumConstantBlock_AST = (AST)currentAST.root;
			enumConstantBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OBJBLOCK,"OBJBLOCK")).add(enumConstantBlock_AST));
			currentAST.root = enumConstantBlock_AST;
			currentAST.child = enumConstantBlock_AST!=null &&enumConstantBlock_AST.getFirstChild()!=null ?
				enumConstantBlock_AST.getFirstChild() : enumConstantBlock_AST;
			currentAST.advanceChildToEnd();
		}
		enumConstantBlock_AST = (AST)currentAST.root;
		returnAST = enumConstantBlock_AST;
	}
	
	public final void enumConstantField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstantField_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		AST tp_AST = null;
		AST t_AST = null;
		AST param_AST = null;
		AST tc_AST = null;
		AST s2_AST = null;
		AST v_AST = null;
		AST s4_AST = null;
		
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case AT:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			modifiers();
			mods_AST = (AST)returnAST;
			{
			switch ( LA(1)) {
			case AT:
			case LITERAL_class:
			case LITERAL_interface:
			case LITERAL_enum:
			{
				typeDefinitionInternal(mods_AST);
				td_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					enumConstantField_AST = (AST)currentAST.root;
					enumConstantField_AST = td_AST;
					currentAST.root = enumConstantField_AST;
					currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
						enumConstantField_AST.getFirstChild() : enumConstantField_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case IDENT:
			case LT:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_any:
			{
				{
				switch ( LA(1)) {
				case LT:
				{
					typeParameters();
					tp_AST = (AST)returnAST;
					break;
				}
				case IDENT:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_any:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				typeSpec(false);
				t_AST = (AST)returnAST;
				{
				if ((LA(1)==IDENT) && (LA(2)==LPAREN) && (_tokenSet_71.member(LA(3)))) {
					AST tmp143_AST = null;
					tmp143_AST = astFactory.create(LT(1));
					match(IDENT);
					match(LPAREN);
					parameterDeclarationList();
					param_AST = (AST)returnAST;
					match(RPAREN);
					{
					switch ( LA(1)) {
					case LITERAL_throws:
					{
						throwsClause();
						tc_AST = (AST)returnAST;
						break;
					}
					case LCURLY:
					case SEMI:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					switch ( LA(1)) {
					case LCURLY:
					{
						compoundStatement();
						s2_AST = (AST)returnAST;
						break;
					}
					case SEMI:
					{
						AST tmp146_AST = null;
						tmp146_AST = astFactory.create(LT(1));
						match(SEMI);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						enumConstantField_AST = (AST)currentAST.root;
						enumConstantField_AST = (AST)astFactory.make( (new ASTArray(8)).add(astFactory.create(METHOD_DEF,"METHOD_DEF")).add(mods_AST).add(tp_AST).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(t_AST))).add(tmp143_AST).add(param_AST).add(tc_AST).add(s2_AST));
						currentAST.root = enumConstantField_AST;
						currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
							enumConstantField_AST.getFirstChild() : enumConstantField_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (_tokenSet_67.member(LA(2))) && (_tokenSet_68.member(LA(3)))) {
					variableDefinitions(mods_AST,t_AST);
					v_AST = (AST)returnAST;
					AST tmp147_AST = null;
					tmp147_AST = astFactory.create(LT(1));
					match(SEMI);
					if ( inputState.guessing==0 ) {
						enumConstantField_AST = (AST)currentAST.root;
						enumConstantField_AST = v_AST;
						currentAST.root = enumConstantField_AST;
						currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
							enumConstantField_AST.getFirstChild() : enumConstantField_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case LCURLY:
		{
			compoundStatement();
			s4_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				enumConstantField_AST = (AST)currentAST.root;
				enumConstantField_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(INSTANCE_INIT,"INSTANCE_INIT")).add(s4_AST));
				currentAST.root = enumConstantField_AST;
				currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
					enumConstantField_AST.getFirstChild() : enumConstantField_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = enumConstantField_AST;
	}
	
	public final void parameterDeclarationList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterDeclarationList_AST = null;
		
		{
		boolean synPredMatched837 = false;
		if (((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2))) && (_tokenSet_74.member(LA(3))))) {
			int _m837 = mark();
			synPredMatched837 = true;
			inputState.guessing++;
			try {
				{
				parameterDeclaration();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched837 = false;
			}
			rewind(_m837);
			inputState.guessing--;
		}
		if ( synPredMatched837 ) {
			parameterDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop839:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_75.member(LA(2))) && (_tokenSet_76.member(LA(3)))) {
					match(COMMA);
					nls();
					parameterDeclaration();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop839;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				nls();
				variableLengthParameterDeclaration();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RPAREN:
			case BOR:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else if ((_tokenSet_77.member(LA(1))) && (_tokenSet_78.member(LA(2))) && (_tokenSet_79.member(LA(3)))) {
			variableLengthParameterDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_80.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			parameterDeclarationList_AST = (AST)currentAST.root;
			parameterDeclarationList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(PARAMETERS,"PARAMETERS")).add(parameterDeclarationList_AST));
			currentAST.root = parameterDeclarationList_AST;
			currentAST.child = parameterDeclarationList_AST!=null &&parameterDeclarationList_AST.getFirstChild()!=null ?
				parameterDeclarationList_AST.getFirstChild() : parameterDeclarationList_AST;
			currentAST.advanceChildToEnd();
		}
		parameterDeclarationList_AST = (AST)currentAST.root;
		returnAST = parameterDeclarationList_AST;
	}
	
	public final void throwsClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST throwsClause_AST = null;
		
		AST tmp150_AST = null;
		tmp150_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp150_AST);
		match(LITERAL_throws);
		nls();
		identifier();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop833:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				identifier();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop833;
			}
			
		} while (true);
		}
		nls();
		throwsClause_AST = (AST)currentAST.root;
		returnAST = throwsClause_AST;
	}
	
	public final void compoundStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compoundStatement_AST = null;
		
		openBlock();
		astFactory.addASTChild(currentAST, returnAST);
		compoundStatement_AST = (AST)currentAST.root;
		returnAST = compoundStatement_AST;
	}
	
/** I've split out constructors separately; we could maybe integrate back into variableDefinitions 
 *  later on if we maybe simplified 'def' to be a type declaration?
 */
	public final void constructorDefinition(
		AST mods
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constructorDefinition_AST = null;
		Token  id = null;
		AST id_AST = null;
		AST param_AST = null;
		AST tc_AST = null;
		AST cb_AST = null;
		
		id = LT(1);
		id_AST = astFactory.create(id);
		astFactory.addASTChild(currentAST, id_AST);
		match(IDENT);
		match(LPAREN);
		parameterDeclarationList();
		param_AST = (AST)returnAST;
		match(RPAREN);
		{
		switch ( LA(1)) {
		case LITERAL_throws:
		{
			throwsClause();
			tc_AST = (AST)returnAST;
			break;
		}
		case LCURLY:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		nlsWarn();
		if ( inputState.guessing==0 ) {
			isConstructorIdent(id);
		}
		constructorBody();
		cb_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			constructorDefinition_AST = (AST)currentAST.root;
			constructorDefinition_AST =  (AST)astFactory.make( (new ASTArray(5)).add(astFactory.create(CTOR_IDENT,"CTOR_IDENT")).add(mods).add(param_AST).add(tc_AST).add(cb_AST));
			
			currentAST.root = constructorDefinition_AST;
			currentAST.child = constructorDefinition_AST!=null &&constructorDefinition_AST.getFirstChild()!=null ?
				constructorDefinition_AST.getFirstChild() : constructorDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		constructorDefinition_AST = (AST)currentAST.root;
		returnAST = constructorDefinition_AST;
	}
	
	public final void constructorBody() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constructorBody_AST = null;
		Token  lc = null;
		AST lc_AST = null;
		
		lc = LT(1);
		lc_AST = astFactory.create(lc);
		astFactory.makeASTRoot(currentAST, lc_AST);
		match(LCURLY);
		if ( inputState.guessing==0 ) {
			lc_AST.setType(SLIST);
		}
		nls();
		{
		boolean synPredMatched812 = false;
		if (((_tokenSet_81.member(LA(1))) && (_tokenSet_82.member(LA(2))) && (_tokenSet_83.member(LA(3))))) {
			int _m812 = mark();
			synPredMatched812 = true;
			inputState.guessing++;
			try {
				{
				explicitConstructorInvocation();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched812 = false;
			}
			rewind(_m812);
			inputState.guessing--;
		}
		if ( synPredMatched812 ) {
			explicitConstructorInvocation();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case SEMI:
			case NLS:
			{
				sep();
				blockBody();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else if ((_tokenSet_14.member(LA(1))) && (_tokenSet_84.member(LA(2))) && (_tokenSet_85.member(LA(3)))) {
			blockBody();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(RCURLY);
		constructorBody_AST = (AST)currentAST.root;
		returnAST = constructorBody_AST;
	}
	
/** Catch obvious constructor calls, but not the expr.super(...) calls */
	public final void explicitConstructorInvocation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST explicitConstructorInvocation_AST = null;
		Token  lp1 = null;
		AST lp1_AST = null;
		Token  lp2 = null;
		AST lp2_AST = null;
		boolean zz; /*ignored*/
		
		{
		switch ( LA(1)) {
		case LT:
		{
			typeArguments();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_super:
		case LITERAL_this:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case LITERAL_this:
		{
			match(LITERAL_this);
			lp1 = LT(1);
			lp1_AST = astFactory.create(lp1);
			astFactory.makeASTRoot(currentAST, lp1_AST);
			match(LPAREN);
			zz=argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				lp1_AST.setType(CTOR_CALL);
			}
			break;
		}
		case LITERAL_super:
		{
			match(LITERAL_super);
			lp2 = LT(1);
			lp2_AST = astFactory.create(lp2);
			astFactory.makeASTRoot(currentAST, lp2_AST);
			match(LPAREN);
			zz=argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				lp2_AST.setType(SUPER_CTOR_CALL);
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		explicitConstructorInvocation_AST = (AST)currentAST.root;
		returnAST = explicitConstructorInvocation_AST;
	}
	
/** Declaration of a variable. This can be a class/instance variable,
 *  or a local variable in a method
 *  It can also include possible initialization.
 */
	public final void variableDeclarator(
		AST mods, AST t
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST variableDeclarator_AST = null;
		AST id_AST = null;
		AST v_AST = null;
		
		variableName();
		id_AST = (AST)returnAST;
		varInitializer();
		v_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			variableDeclarator_AST = (AST)currentAST.root;
			variableDeclarator_AST = (AST)astFactory.make( (new ASTArray(5)).add(astFactory.create(VARIABLE_DEF,"VARIABLE_DEF")).add(mods).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(t))).add(id_AST).add(v_AST));
			currentAST.root = variableDeclarator_AST;
			currentAST.child = variableDeclarator_AST!=null &&variableDeclarator_AST.getFirstChild()!=null ?
				variableDeclarator_AST.getFirstChild() : variableDeclarator_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = variableDeclarator_AST;
	}
	
/** Zero or more insignificant newlines, all gobbled up and thrown away,
  * but a warning message is left for the user.
  */
	public final void nlsWarn() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nlsWarn_AST = null;
		
		{
		if ((LA(1)==NLS) && (_tokenSet_6.member(LA(2))) && (_tokenSet_7.member(LA(3)))) {
			match(NLS);
		}
		else if ((_tokenSet_6.member(LA(1))) && (_tokenSet_7.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			addWarning(
				  "A newline at this point does not follow the Groovy Coding Conventions.",
				                	  "Keep this statement on one line, or use curly braces to break across multiple lines."
				                	);
		}
		returnAST = nlsWarn_AST;
	}
	
/** An open block is not allowed to have closure arguments. */
	public final void openBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST openBlock_AST = null;
		Token  lc = null;
		AST lc_AST = null;
		
		lc = LT(1);
		lc_AST = astFactory.create(lc);
		astFactory.makeASTRoot(currentAST, lc_AST);
		match(LCURLY);
		if ( inputState.guessing==0 ) {
			lc_AST.setType(SLIST);
		}
		blockBody();
		astFactory.addASTChild(currentAST, returnAST);
		match(RCURLY);
		openBlock_AST = (AST)currentAST.root;
		returnAST = openBlock_AST;
	}
	
	public final void variableName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST variableName_AST = null;
		
		AST tmp161_AST = null;
		tmp161_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp161_AST);
		match(IDENT);
		variableName_AST = (AST)currentAST.root;
		returnAST = variableName_AST;
	}
	
	public final void initializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST initializer_AST = null;
		
		expression();
		astFactory.addASTChild(currentAST, returnAST);
		initializer_AST = (AST)currentAST.root;
		returnAST = initializer_AST;
	}
	
	public final void expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expression_AST = null;
		
		boolean synPredMatched958 = false;
		if (((_tokenSet_15.member(LA(1))) && (_tokenSet_86.member(LA(2))) && (_tokenSet_87.member(LA(3))))) {
			int _m958 = mark();
			synPredMatched958 = true;
			inputState.guessing++;
			try {
				{
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched958 = false;
			}
			rewind(_m958);
			inputState.guessing--;
		}
		if ( synPredMatched958 ) {
			singleDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			expression_AST = (AST)currentAST.root;
		}
		else if (((LA(1) >= LITERAL_return && LA(1) <= LITERAL_assert))) {
			branchExpression();
			astFactory.addASTChild(currentAST, returnAST);
			expression_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_27.member(LA(1))) && (_tokenSet_88.member(LA(2))) && (_tokenSet_89.member(LA(3)))) {
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				expression_AST = (AST)currentAST.root;
				expression_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXPR,"EXPR")).add(expression_AST));
				currentAST.root = expression_AST;
				currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
					expression_AST.getFirstChild() : expression_AST;
				currentAST.advanceChildToEnd();
			}
			expression_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = expression_AST;
	}
	
	public final void parameterDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterDeclaration_AST = null;
		AST pm_AST = null;
		AST t_AST = null;
		AST id_AST = null;
		AST exp_AST = null;
		
		{
		switch ( LA(1)) {
		case DEF:
		{
			match(DEF);
			break;
		}
		case FINAL:
		case AT:
		case IDENT:
		case QUESTION:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case STAR:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		parameterModifier();
		pm_AST = (AST)returnAST;
		{
		if ((_tokenSet_90.member(LA(1))) && (_tokenSet_91.member(LA(2))) && (_tokenSet_92.member(LA(3)))) {
			typeSpec(false);
			t_AST = (AST)returnAST;
		}
		else if ((_tokenSet_93.member(LA(1))) && (_tokenSet_94.member(LA(2))) && (_tokenSet_95.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		parameterIdent();
		id_AST = (AST)returnAST;
		{
		switch ( LA(1)) {
		case ASSIGN:
		{
			AST tmp163_AST = null;
			tmp163_AST = astFactory.create(LT(1));
			match(ASSIGN);
			expression();
			exp_AST = (AST)returnAST;
			break;
		}
		case RPAREN:
		case COMMA:
		case BOR:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			parameterDeclaration_AST = (AST)currentAST.root;
			parameterDeclaration_AST = (AST)astFactory.make( (new ASTArray(5)).add(astFactory.create(PARAMETER_DEF,"PARAMETER_DEF")).add(pm_AST).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(t_AST))).add(id_AST).add(exp_AST));
			currentAST.root = parameterDeclaration_AST;
			currentAST.child = parameterDeclaration_AST!=null &&parameterDeclaration_AST.getFirstChild()!=null ?
				parameterDeclaration_AST.getFirstChild() : parameterDeclaration_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = parameterDeclaration_AST;
	}
	
	public final void variableLengthParameterDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST variableLengthParameterDeclaration_AST = null;
		AST pm_AST = null;
		AST t_AST = null;
		Token  id = null;
		AST id_AST = null;
		
		parameterModifier();
		pm_AST = (AST)returnAST;
		typeSpec(false);
		t_AST = (AST)returnAST;
		match(TRIPLE_DOT);
		id = LT(1);
		id_AST = astFactory.create(id);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			variableLengthParameterDeclaration_AST = (AST)currentAST.root;
			variableLengthParameterDeclaration_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF")).add(pm_AST).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPE,"TYPE")).add(t_AST))).add(id_AST));
			currentAST.root = variableLengthParameterDeclaration_AST;
			currentAST.child = variableLengthParameterDeclaration_AST!=null &&variableLengthParameterDeclaration_AST.getFirstChild()!=null ?
				variableLengthParameterDeclaration_AST.getFirstChild() : variableLengthParameterDeclaration_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = variableLengthParameterDeclaration_AST;
	}
	
	public final void parameterModifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterModifier_AST = null;
		Token  f = null;
		AST f_AST = null;
		
		{
		_loop848:
		do {
			if ((LA(1)==AT) && (LA(2)==IDENT) && (_tokenSet_96.member(LA(3)))) {
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else {
				break _loop848;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case FINAL:
		{
			f = LT(1);
			f_AST = astFactory.create(f);
			astFactory.addASTChild(currentAST, f_AST);
			match(FINAL);
			break;
		}
		case AT:
		case IDENT:
		case QUESTION:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case STAR:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop851:
		do {
			if ((LA(1)==AT)) {
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else {
				break _loop851;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			parameterModifier_AST = (AST)currentAST.root;
			parameterModifier_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(MODIFIERS,"MODIFIERS")).add(parameterModifier_AST));
			currentAST.root = parameterModifier_AST;
			currentAST.child = parameterModifier_AST!=null &&parameterModifier_AST.getFirstChild()!=null ?
				parameterModifier_AST.getFirstChild() : parameterModifier_AST;
			currentAST.advanceChildToEnd();
		}
		parameterModifier_AST = (AST)currentAST.root;
		returnAST = parameterModifier_AST;
	}
	
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
	public final void parameterIdent() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterIdent_AST = null;
		Token  sp = null;
		AST sp_AST = null;
		Token  op = null;
		AST op_AST = null;
		
		{
		switch ( LA(1)) {
		case STAR:
		{
			sp = LT(1);
			sp_AST = astFactory.create(sp);
			astFactory.makeASTRoot(currentAST, sp_AST);
			match(STAR);
			if ( inputState.guessing==0 ) {
				sp_AST.setType(SPREAD_ARG);
			}
			break;
		}
		case QUESTION:
		{
			op = LT(1);
			op_AST = astFactory.create(op);
			astFactory.makeASTRoot(currentAST, op_AST);
			match(QUESTION);
			if ( inputState.guessing==0 ) {
				op_AST.setType(OPTIONAL_ARG);
			}
			break;
		}
		case IDENT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		AST tmp165_AST = null;
		tmp165_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp165_AST);
		match(IDENT);
		parameterIdent_AST = (AST)currentAST.root;
		returnAST = parameterIdent_AST;
	}
	
/** Closure parameters are exactly like method parameters,
  * except that they are enclosed in vertical bars instead of parentheses.
  * The first vertical bar is optional if the parameters are a simple list
  * of one or more names, with no additional syntax.
  * (An empty argument list must be spelled with two bars, <code>{|| ...}</code>)
  */
	public final void closureParameters() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closureParameters_AST = null;
		
		switch ( LA(1)) {
		case LPAREN:
		{
			match(LPAREN);
			nls();
			parameterDeclarationList();
			astFactory.addASTChild(currentAST, returnAST);
			nls();
			match(RPAREN);
			nls();
			match(BOR);
			closureParameters_AST = (AST)currentAST.root;
			break;
		}
		case LOR:
		{
			match(LOR);
			closureParameters_AST = (AST)currentAST.root;
			break;
		}
		default:
			boolean synPredMatched854 = false;
			if (((LA(1)==BOR) && (LA(2)==BOR||LA(2)==NLS) && (_tokenSet_97.member(LA(3))))) {
				int _m854 = mark();
				synPredMatched854 = true;
				inputState.guessing++;
				try {
					{
					match(BOR);
					nls();
					match(BOR);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched854 = false;
				}
				rewind(_m854);
				inputState.guessing--;
			}
			if ( synPredMatched854 ) {
				match(BOR);
				nls();
				match(BOR);
				closureParameters_AST = (AST)currentAST.root;
			}
			else {
				boolean synPredMatched857 = false;
				if (((LA(1)==BOR) && (_tokenSet_98.member(LA(2))) && (_tokenSet_99.member(LA(3))))) {
					int _m857 = mark();
					synPredMatched857 = true;
					inputState.guessing++;
					try {
						{
						match(BOR);
						nls();
						{
						if ((true)) {
							parameterDeclarationList();
						}
						else if ((LA(1)==LPAREN)) {
							match(LPAREN);
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched857 = false;
					}
					rewind(_m857);
					inputState.guessing--;
				}
				if ( synPredMatched857 ) {
					match(BOR);
					nls();
					{
					switch ( LA(1)) {
					case FINAL:
					case DEF:
					case AT:
					case IDENT:
					case QUESTION:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_any:
					case STAR:
					case BOR:
					case NLS:
					{
						parameterDeclarationList();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case LPAREN:
					{
						{
						match(LPAREN);
						nls();
						parameterDeclarationList();
						astFactory.addASTChild(currentAST, returnAST);
						nls();
						match(RPAREN);
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					match(BOR);
					closureParameters_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_100.member(LA(1))) && (_tokenSet_101.member(LA(2))) && (_tokenSet_88.member(LA(3)))) {
					{
					switch ( LA(1)) {
					case IDENT:
					case QUESTION:
					case STAR:
					{
						closureParameter();
						astFactory.addASTChild(currentAST, returnAST);
						{
						_loop862:
						do {
							if ((LA(1)==COMMA)) {
								match(COMMA);
								nls();
								closureParameter();
								astFactory.addASTChild(currentAST, returnAST);
							}
							else {
								break _loop862;
							}
							
						} while (true);
						}
						nls();
						break;
					}
					case BOR:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(BOR);
					if ( inputState.guessing==0 ) {
						closureParameters_AST = (AST)currentAST.root;
						closureParameters_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(PARAMETERS,"PARAMETERS")).add(closureParameters_AST));
						currentAST.root = closureParameters_AST;
						currentAST.child = closureParameters_AST!=null &&closureParameters_AST.getFirstChild()!=null ?
							closureParameters_AST.getFirstChild() : closureParameters_AST;
						currentAST.advanceChildToEnd();
					}
					closureParameters_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}}
			returnAST = closureParameters_AST;
		}
		
/** Simple names, as in {x|...}, are completely equivalent to {(def x)|...}.  Build the right AST. */
	public final void closureParameter() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closureParameter_AST = null;
		AST id_AST = null;
		
		parameterIdent();
		id_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			closureParameter_AST = (AST)currentAST.root;
			closureParameter_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(PARAMETER_DEF,"PARAMETER_DEF")).add((AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(MODIFIERS,"MODIFIERS")))).add((AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(TYPE,"TYPE")))).add(id_AST));
			currentAST.root = closureParameter_AST;
			currentAST.child = closureParameter_AST!=null &&closureParameter_AST.getFirstChild()!=null ?
				closureParameter_AST.getFirstChild() : closureParameter_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = closureParameter_AST;
	}
	
/** Lookahead for closureParameters. */
	public final void closureParametersStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closureParametersStart_AST = null;
		
		switch ( LA(1)) {
		case BOR:
		{
			AST tmp178_AST = null;
			tmp178_AST = astFactory.create(LT(1));
			match(BOR);
			break;
		}
		case LOR:
		{
			AST tmp179_AST = null;
			tmp179_AST = astFactory.create(LT(1));
			match(LOR);
			break;
		}
		case IDENT:
		case QUESTION:
		case STAR:
		{
			parameterIdent();
			nls();
			{
			switch ( LA(1)) {
			case BOR:
			{
				AST tmp180_AST = null;
				tmp180_AST = astFactory.create(LT(1));
				match(BOR);
				break;
			}
			case COMMA:
			{
				AST tmp181_AST = null;
				tmp181_AST = astFactory.create(LT(1));
				match(COMMA);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case LPAREN:
		{
			AST tmp182_AST = null;
			tmp182_AST = astFactory.create(LT(1));
			match(LPAREN);
			balancedTokens();
			AST tmp183_AST = null;
			tmp183_AST = astFactory.create(LT(1));
			match(RPAREN);
			nls();
			AST tmp184_AST = null;
			tmp184_AST = astFactory.create(LT(1));
			match(BOR);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = closureParametersStart_AST;
	}
	
	public final void balancedTokensNoSep() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST balancedTokensNoSep_AST = null;
		
		{
		_loop1105:
		do {
			if ((_tokenSet_37.member(LA(1)))) {
				balancedBrackets();
			}
			else if ((_tokenSet_102.member(LA(1)))) {
				{
				match(_tokenSet_102);
				}
			}
			else {
				break _loop1105;
			}
			
		} while (true);
		}
		returnAST = balancedTokensNoSep_AST;
	}
	
	public final void assignmentExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignmentExpression_AST = null;
		
		conditionalExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case ASSIGN:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		{
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				AST tmp186_AST = null;
				tmp186_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp186_AST);
				match(ASSIGN);
				break;
			}
			case PLUS_ASSIGN:
			{
				AST tmp187_AST = null;
				tmp187_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp187_AST);
				match(PLUS_ASSIGN);
				break;
			}
			case MINUS_ASSIGN:
			{
				AST tmp188_AST = null;
				tmp188_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp188_AST);
				match(MINUS_ASSIGN);
				break;
			}
			case STAR_ASSIGN:
			{
				AST tmp189_AST = null;
				tmp189_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp189_AST);
				match(STAR_ASSIGN);
				break;
			}
			case DIV_ASSIGN:
			{
				AST tmp190_AST = null;
				tmp190_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp190_AST);
				match(DIV_ASSIGN);
				break;
			}
			case MOD_ASSIGN:
			{
				AST tmp191_AST = null;
				tmp191_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp191_AST);
				match(MOD_ASSIGN);
				break;
			}
			case SR_ASSIGN:
			{
				AST tmp192_AST = null;
				tmp192_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp192_AST);
				match(SR_ASSIGN);
				break;
			}
			case BSR_ASSIGN:
			{
				AST tmp193_AST = null;
				tmp193_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp193_AST);
				match(BSR_ASSIGN);
				break;
			}
			case SL_ASSIGN:
			{
				AST tmp194_AST = null;
				tmp194_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp194_AST);
				match(SL_ASSIGN);
				break;
			}
			case BAND_ASSIGN:
			{
				AST tmp195_AST = null;
				tmp195_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp195_AST);
				match(BAND_ASSIGN);
				break;
			}
			case BXOR_ASSIGN:
			{
				AST tmp196_AST = null;
				tmp196_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp196_AST);
				match(BXOR_ASSIGN);
				break;
			}
			case BOR_ASSIGN:
			{
				AST tmp197_AST = null;
				tmp197_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp197_AST);
				match(BOR_ASSIGN);
				break;
			}
			case STAR_STAR_ASSIGN:
			{
				AST tmp198_AST = null;
				tmp198_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp198_AST);
				match(STAR_STAR_ASSIGN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			nls();
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case RBRACK:
		case IDENT:
		case RPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case BOR:
		case COLON:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		assignmentExpression_AST = (AST)currentAST.root;
		returnAST = assignmentExpression_AST;
	}
	
	public final void expressionNotBOR() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionNotBOR_AST = null;
		AST e_AST = null;
		
		expression();
		e_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			require(e_AST.getType() != BOR,
			"expression in block; cannot be of the form a|b",
			"enclose the expression parentheses (a|b)");
		}
		expressionNotBOR_AST = (AST)currentAST.root;
		returnAST = expressionNotBOR_AST;
	}
	
/** A block which is known to be a closure, even if it has no apparent arguments.
 */
	public final void closedBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closedBlock_AST = null;
		Token  lc = null;
		AST lc_AST = null;
		
		lc = LT(1);
		lc_AST = astFactory.create(lc);
		astFactory.makeASTRoot(currentAST, lc_AST);
		match(LCURLY);
		if ( inputState.guessing==0 ) {
			lc_AST.setType(CLOSED_BLOCK);
		}
		{
		boolean synPredMatched885 = false;
		if (((_tokenSet_103.member(LA(1))) && (_tokenSet_104.member(LA(2))) && (_tokenSet_88.member(LA(3))))) {
			int _m885 = mark();
			synPredMatched885 = true;
			inputState.guessing++;
			try {
				{
				nls();
				closureParametersStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched885 = false;
			}
			rewind(_m885);
			inputState.guessing--;
		}
		if ( synPredMatched885 ) {
			nls();
			astFactory.addASTChild(currentAST, returnAST);
			closureParameters();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_14.member(LA(1))) && (_tokenSet_88.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			implicitParameters();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		blockBody();
		astFactory.addASTChild(currentAST, returnAST);
		match(RCURLY);
		closedBlock_AST = (AST)currentAST.root;
		returnAST = closedBlock_AST;
	}
	
/** A block known to be a closure, but which omits its arguments, is given this placeholder.
 *  A subsequent pass is responsible for deciding if there is an implicit 'it' parameter,
 *  or if the parameter list should be empty.
 */
	public final void implicitParameters() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST implicitParameters_AST = null;
		
		if ( inputState.guessing==0 ) {
			implicitParameters_AST = (AST)currentAST.root;
			implicitParameters_AST = (AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(IMPLICIT_PARAMETERS,"IMPLICIT_PARAMETERS")));
			currentAST.root = implicitParameters_AST;
			currentAST.child = implicitParameters_AST!=null &&implicitParameters_AST.getFirstChild()!=null ?
				implicitParameters_AST.getFirstChild() : implicitParameters_AST;
			currentAST.advanceChildToEnd();
		}
		implicitParameters_AST = (AST)currentAST.root;
		returnAST = implicitParameters_AST;
	}
	
/** A block inside an expression is always assumed to be a closure.
 *  Only blocks which occur directly as substatements are kept open.
 */
	public final void expressionBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionBlock_AST = null;
		
		closedBlock();
		astFactory.addASTChild(currentAST, returnAST);
		expressionBlock_AST = (AST)currentAST.root;
		returnAST = expressionBlock_AST;
	}
	
/** An appended block follows a method name or method argument list.
 *  It is optionally labeled.  DECIDE:  A good rule?
 */
	public final void appendedBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST appendedBlock_AST = null;
		
		expressionBlock();
		astFactory.addASTChild(currentAST, returnAST);
		appendedBlock_AST = (AST)currentAST.root;
		returnAST = appendedBlock_AST;
	}
	
/** A sub-block of a block can be either open or closed.
 *  It is closed if and only if there are explicit closure arguments.
 *  Compare this to a block which is appended to a method call,
 *  which is given closure arguments, even if they are not explicit in the code.
 */
	public final void openOrClosedBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST openOrClosedBlock_AST = null;
		
		boolean synPredMatched891 = false;
		if (((LA(1)==LCURLY) && (_tokenSet_24.member(LA(2))) && (_tokenSet_25.member(LA(3))))) {
			int _m891 = mark();
			synPredMatched891 = true;
			inputState.guessing++;
			try {
				{
				match(LCURLY);
				nls();
				closureParametersStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched891 = false;
			}
			rewind(_m891);
			inputState.guessing--;
		}
		if ( synPredMatched891 ) {
			closedBlock();
			astFactory.addASTChild(currentAST, returnAST);
			openOrClosedBlock_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==LCURLY) && (_tokenSet_14.member(LA(2))) && (_tokenSet_11.member(LA(3)))) {
			openBlock();
			astFactory.addASTChild(currentAST, returnAST);
			openOrClosedBlock_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = openOrClosedBlock_AST;
	}
	
/** Any statement which begins with an expression, called the "head".
 *  The head can be followed by command arguments:  {x.y a,b}, {x[y] a,b}, even {f(x) y}.
 *  Or, the head can be followed by an assignment operator:  {x.y = z}, {x.y[a] ++}, {x.y(a) += z}, etc.
 *  To catch simple errors, expressions at statement level have a limited set of syntaxes.
 *  For example, {print x; +y} is a syntax error.  (Java does this trick also.)
 *  If you really want something weird, wrap it in parentheses or curly braces.
 */
	public final void expressionStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionStatement_AST = null;
		AST head_AST = null;
		boolean endBrackets = false;
		
		switch ( LA(1)) {
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_this:
		case STRING_LITERAL:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			endBrackets=pathExpression();
			head_AST = (AST)returnAST;
			{
			if (((_tokenSet_105.member(LA(1))) && (_tokenSet_11.member(LA(2))) && (_tokenSet_7.member(LA(3))))&&(!endBrackets)) {
				commandArguments(head_AST);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_106.member(LA(1))) && (_tokenSet_107.member(LA(2))) && (_tokenSet_7.member(LA(3)))) {
				assignmentTail(head_AST);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_12.member(LA(1)))) {
				if ( inputState.guessing==0 ) {
					expressionStatement_AST = (AST)currentAST.root;
					expressionStatement_AST = head_AST;
					currentAST.root = expressionStatement_AST;
					currentAST.child = expressionStatement_AST!=null &&expressionStatement_AST.getFirstChild()!=null ?
						expressionStatement_AST.getFirstChild() : expressionStatement_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				// Do an error check on the following token:
				switch (LA(1)) {
				case RCURLY: case RBRACK: case RPAREN: case SEMI: case NLS: case EOF:
				break;
				default:
				require(false,
				"command followed by garbage in f...",
				"parenthesize correct argument list f(...) or whole expression (f()...)");
				}
				
			}
			expressionStatement_AST = (AST)currentAST.root;
			break;
		}
		case INC:
		case DEC:
		{
			{
			switch ( LA(1)) {
			case INC:
			{
				AST tmp200_AST = null;
				tmp200_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp200_AST);
				match(INC);
				break;
			}
			case DEC:
			{
				AST tmp201_AST = null;
				tmp201_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp201_AST);
				match(DEC);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			endBrackets=pathExpression();
			astFactory.addASTChild(currentAST, returnAST);
			expressionStatement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		{
			branchExpression();
			astFactory.addASTChild(currentAST, returnAST);
			expressionStatement_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = expressionStatement_AST;
	}
	
/** In Java, "if", "while", and "for" statements can take random, non-braced statements as their bodies.
 *  Support this practice, even though it isn't very Groovy.
 */
	public final void compatibleBodyStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compatibleBodyStatement_AST = null;
		
		boolean synPredMatched917 = false;
		if (((LA(1)==LCURLY) && (_tokenSet_14.member(LA(2))) && (_tokenSet_11.member(LA(3))))) {
			int _m917 = mark();
			synPredMatched917 = true;
			inputState.guessing++;
			try {
				{
				match(LCURLY);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched917 = false;
			}
			rewind(_m917);
			inputState.guessing--;
		}
		if ( synPredMatched917 ) {
			compoundStatement();
			astFactory.addASTChild(currentAST, returnAST);
			compatibleBodyStatement_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_23.member(LA(1))) && (_tokenSet_11.member(LA(2))) && (_tokenSet_26.member(LA(3)))) {
			statement();
			astFactory.addASTChild(currentAST, returnAST);
			compatibleBodyStatement_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = compatibleBodyStatement_AST;
	}
	
	public final void forStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forStatement_AST = null;
		Token  f = null;
		AST f_AST = null;
		
		f = LT(1);
		f_AST = astFactory.create(f);
		astFactory.makeASTRoot(currentAST, f_AST);
		match(LITERAL_for);
		match(LPAREN);
		{
		boolean synPredMatched909 = false;
		if (((_tokenSet_108.member(LA(1))) && (_tokenSet_109.member(LA(2))) && (_tokenSet_110.member(LA(3))))) {
			int _m909 = mark();
			synPredMatched909 = true;
			inputState.guessing++;
			try {
				{
				forInit();
				match(SEMI);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched909 = false;
			}
			rewind(_m909);
			inputState.guessing--;
		}
		if ( synPredMatched909 ) {
			traditionalForClause();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_111.member(LA(2))) && (_tokenSet_112.member(LA(3)))) {
			forInClause();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(RPAREN);
		nlsWarn();
		compatibleBodyStatement();
		astFactory.addASTChild(currentAST, returnAST);
		forStatement_AST = (AST)currentAST.root;
		returnAST = forStatement_AST;
	}
	
	public final void casesGroup() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST casesGroup_AST = null;
		
		{
		int _cnt930=0;
		_loop930:
		do {
			if ((LA(1)==LITERAL_default||LA(1)==LITERAL_case)) {
				aCase();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt930>=1 ) { break _loop930; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt930++;
		} while (true);
		}
		caseSList();
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			casesGroup_AST = (AST)currentAST.root;
			casesGroup_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CASE_GROUP,"CASE_GROUP")).add(casesGroup_AST));
			currentAST.root = casesGroup_AST;
			currentAST.child = casesGroup_AST!=null &&casesGroup_AST.getFirstChild()!=null ?
				casesGroup_AST.getFirstChild() : casesGroup_AST;
			currentAST.advanceChildToEnd();
		}
		casesGroup_AST = (AST)currentAST.root;
		returnAST = casesGroup_AST;
	}
	
	public final void tryBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tryBlock_AST = null;
		
		AST tmp204_AST = null;
		tmp204_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp204_AST);
		match(LITERAL_try);
		nlsWarn();
		compoundStatement();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop945:
		do {
			if ((LA(1)==LITERAL_catch||LA(1)==NLS) && (LA(2)==LPAREN||LA(2)==LITERAL_catch) && (_tokenSet_113.member(LA(3)))) {
				nls();
				handler();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop945;
			}
			
		} while (true);
		}
		{
		if ((LA(1)==LITERAL_finally||LA(1)==NLS) && (_tokenSet_114.member(LA(2))) && (_tokenSet_14.member(LA(3)))) {
			nls();
			finallyClause();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_12.member(LA(1))) && (_tokenSet_13.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		tryBlock_AST = (AST)currentAST.root;
		returnAST = tryBlock_AST;
	}
	
	public final void forInit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forInit_AST = null;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LITERAL_this:
		case STRING_LITERAL:
		case INC:
		case DEC:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			controlExpressionList();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			forInit_AST = (AST)currentAST.root;
			forInit_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FOR_INIT,"FOR_INIT")).add(forInit_AST));
			currentAST.root = forInit_AST;
			currentAST.child = forInit_AST!=null &&forInit_AST.getFirstChild()!=null ?
				forInit_AST.getFirstChild() : forInit_AST;
			currentAST.advanceChildToEnd();
		}
		forInit_AST = (AST)currentAST.root;
		returnAST = forInit_AST;
	}
	
	public final void traditionalForClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST traditionalForClause_AST = null;
		
		forInit();
		astFactory.addASTChild(currentAST, returnAST);
		match(SEMI);
		forCond();
		astFactory.addASTChild(currentAST, returnAST);
		match(SEMI);
		forIter();
		astFactory.addASTChild(currentAST, returnAST);
		traditionalForClause_AST = (AST)currentAST.root;
		returnAST = traditionalForClause_AST;
	}
	
	public final void forInClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forInClause_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		{
		boolean synPredMatched914 = false;
		if (((_tokenSet_15.member(LA(1))) && (_tokenSet_86.member(LA(2))))) {
			int _m914 = mark();
			synPredMatched914 = true;
			inputState.guessing++;
			try {
				{
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched914 = false;
			}
			rewind(_m914);
			inputState.guessing--;
		}
		if ( synPredMatched914 ) {
			singleDeclarationNoInit();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((LA(1)==IDENT) && (LA(2)==LITERAL_in)) {
			AST tmp207_AST = null;
			tmp207_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp207_AST);
			match(IDENT);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		i = LT(1);
		i_AST = astFactory.create(i);
		astFactory.makeASTRoot(currentAST, i_AST);
		match(LITERAL_in);
		if ( inputState.guessing==0 ) {
			i_AST.setType(FOR_IN_ITERABLE);
		}
		shiftExpression();
		astFactory.addASTChild(currentAST, returnAST);
		forInClause_AST = (AST)currentAST.root;
		returnAST = forInClause_AST;
	}
	
	public final void forCond() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forCond_AST = null;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case INC:
		case DEC:
		case PLUS:
		case MINUS:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			forCond_AST = (AST)currentAST.root;
			forCond_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FOR_CONDITION,"FOR_CONDITION")).add(forCond_AST));
			currentAST.root = forCond_AST;
			currentAST.child = forCond_AST!=null &&forCond_AST.getFirstChild()!=null ?
				forCond_AST.getFirstChild() : forCond_AST;
			currentAST.advanceChildToEnd();
		}
		forCond_AST = (AST)currentAST.root;
		returnAST = forCond_AST;
	}
	
	public final void forIter() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forIter_AST = null;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LITERAL_this:
		case STRING_LITERAL:
		case INC:
		case DEC:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			controlExpressionList();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			forIter_AST = (AST)currentAST.root;
			forIter_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FOR_ITERATOR,"FOR_ITERATOR")).add(forIter_AST));
			currentAST.root = forIter_AST;
			currentAST.child = forIter_AST!=null &&forIter_AST.getFirstChild()!=null ?
				forIter_AST.getFirstChild() : forIter_AST;
			currentAST.advanceChildToEnd();
		}
		forIter_AST = (AST)currentAST.root;
		returnAST = forIter_AST;
	}
	
	public final void shiftExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST shiftExpression_AST = null;
		
		additiveExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1025:
		do {
			if ((_tokenSet_115.member(LA(1)))) {
				{
				switch ( LA(1)) {
				case SR:
				case BSR:
				case SL:
				{
					{
					switch ( LA(1)) {
					case SL:
					{
						AST tmp208_AST = null;
						tmp208_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp208_AST);
						match(SL);
						break;
					}
					case SR:
					{
						AST tmp209_AST = null;
						tmp209_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp209_AST);
						match(SR);
						break;
					}
					case BSR:
					{
						AST tmp210_AST = null;
						tmp210_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp210_AST);
						match(BSR);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					break;
				}
				case RANGE_INCLUSIVE:
				{
					AST tmp211_AST = null;
					tmp211_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp211_AST);
					match(RANGE_INCLUSIVE);
					break;
				}
				case TRIPLE_DOT:
				{
					AST tmp212_AST = null;
					tmp212_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp212_AST);
					match(TRIPLE_DOT);
					if ( inputState.guessing==0 ) {
						tmp212_AST.setType(RANGE_EXCLUSIVE);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				additiveExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1025;
			}
			
		} while (true);
		}
		shiftExpression_AST = (AST)currentAST.root;
		returnAST = shiftExpression_AST;
	}
	
/** In Groovy, return, break, continue, throw, and assert can be used in any expression context.
 *  Example:  println (x || return);  println assert x, "won't print a false value!"
 *  If an optional expression is missing, its value is void (this coerces to null when a value is required).
 */
	public final void branchExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST branchExpression_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_return:
		{
			AST tmp213_AST = null;
			tmp213_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp213_AST);
			match(LITERAL_return);
			{
			if ((_tokenSet_27.member(LA(1))) && (_tokenSet_88.member(LA(2))) && (_tokenSet_89.member(LA(3)))) {
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_116.member(LA(1))) && (_tokenSet_117.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			branchExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_break:
		case LITERAL_continue:
		{
			{
			switch ( LA(1)) {
			case LITERAL_break:
			{
				AST tmp214_AST = null;
				tmp214_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp214_AST);
				match(LITERAL_break);
				break;
			}
			case LITERAL_continue:
			{
				AST tmp215_AST = null;
				tmp215_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp215_AST);
				match(LITERAL_continue);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			if ((LA(1)==IDENT) && (LA(2)==COLON) && (_tokenSet_118.member(LA(3)))) {
				statementLabelPrefix();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_118.member(LA(1))) && (_tokenSet_89.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((_tokenSet_27.member(LA(1))) && (_tokenSet_88.member(LA(2))) && (_tokenSet_89.member(LA(3)))) {
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_116.member(LA(1))) && (_tokenSet_117.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			branchExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_throw:
		{
			AST tmp216_AST = null;
			tmp216_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp216_AST);
			match(LITERAL_throw);
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			branchExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_assert:
		{
			AST tmp217_AST = null;
			tmp217_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp217_AST);
			match(LITERAL_assert);
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			if ((LA(1)==COMMA) && (_tokenSet_27.member(LA(2))) && (_tokenSet_88.member(LA(3)))) {
				match(COMMA);
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_116.member(LA(1))) && (_tokenSet_117.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			branchExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = branchExpression_AST;
	}
	
	public final void statementLabelPrefix() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST statementLabelPrefix_AST = null;
		Token  c = null;
		AST c_AST = null;
		
		AST tmp219_AST = null;
		tmp219_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp219_AST);
		match(IDENT);
		c = LT(1);
		c_AST = astFactory.create(c);
		astFactory.makeASTRoot(currentAST, c_AST);
		match(COLON);
		if ( inputState.guessing==0 ) {
			c_AST.setType(LABELED_STAT);
		}
		statementLabelPrefix_AST = (AST)currentAST.root;
		returnAST = statementLabelPrefix_AST;
	}
	
/** A "path expression" is a name which can be used for value, assigned to, or called.
 *  Uses include assignment targets, commands, and types in declarations.
 *  It is called a "path" because it looks like a linear path through a data structure.
 *  Example:  a.b[n].c(x).d{s}
 *  (Compare to a C lvalue, or LeftHandSide in the JLS section 15.26.)
 *  General expressions are built up from path expressions, using operators like '+' and '='.
 *  Note:  A path expression cannot begin with a block or closure.
 */
	public final boolean  pathExpression() throws RecognitionException, TokenStreamException {
		boolean endBrackets = false;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathExpression_AST = null;
		AST pe_AST = null;
		
		primaryExpression();
		pe_AST = (AST)returnAST;
		endBrackets=pathExpressionTail(pe_AST);
		astFactory.addASTChild(currentAST, returnAST);
		pathExpression_AST = (AST)currentAST.root;
		returnAST = pathExpression_AST;
		return endBrackets;
	}
	
/** A member name (x.y) or element name (x[y]) can serve as a command name,
 *  which may be followed by a list of arguments.
 */
	public final void commandArguments(
		AST head
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST commandArguments_AST = null;
		
		if ( inputState.guessing==0 ) {
			commandArguments_AST = (AST)currentAST.root;
			commandArguments_AST = head;
			switch (LA(1)) {
			case PLUS: case MINUS: case INC: case DEC:
			case STAR: case DIV: case MOD:
			case SR: case BSR: case SL:
			case BAND: case BXOR: case BOR: case STAR_STAR:
			require(false,
			"garbage infix or prefix operator after command name f +x",
			"parenthesize either the whole expression (f+x) or the command arguments f(+x)");
			}
			
			currentAST.root = commandArguments_AST;
			currentAST.child = commandArguments_AST!=null &&commandArguments_AST.getFirstChild()!=null ?
				commandArguments_AST.getFirstChild() : commandArguments_AST;
			currentAST.advanceChildToEnd();
		}
		expression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop953:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				expression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop953;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			commandArguments_AST = (AST)currentAST.root;
			
			AST headid = getASTFactory().dup(head);
			headid.setType(METHOD_CALL);
			headid.setText("<command>");
			commandArguments_AST = (AST)astFactory.make( (new ASTArray(2)).add(headid).add(commandArguments_AST));
			
			currentAST.root = commandArguments_AST;
			currentAST.child = commandArguments_AST!=null &&commandArguments_AST.getFirstChild()!=null ?
				commandArguments_AST.getFirstChild() : commandArguments_AST;
			currentAST.advanceChildToEnd();
		}
		commandArguments_AST = (AST)currentAST.root;
		returnAST = commandArguments_AST;
	}
	
	public final void assignmentTail(
		AST head
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignmentTail_AST = null;
		Token  in = null;
		AST in_AST = null;
		Token  de = null;
		AST de_AST = null;
		
		switch ( LA(1)) {
		case ASSIGN:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		{
			if ( inputState.guessing==0 ) {
				assignmentTail_AST = (AST)currentAST.root;
				assignmentTail_AST = head;
				currentAST.root = assignmentTail_AST;
				currentAST.child = assignmentTail_AST!=null &&assignmentTail_AST.getFirstChild()!=null ?
					assignmentTail_AST.getFirstChild() : assignmentTail_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				AST tmp221_AST = null;
				tmp221_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp221_AST);
				match(ASSIGN);
				break;
			}
			case PLUS_ASSIGN:
			{
				AST tmp222_AST = null;
				tmp222_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp222_AST);
				match(PLUS_ASSIGN);
				break;
			}
			case MINUS_ASSIGN:
			{
				AST tmp223_AST = null;
				tmp223_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp223_AST);
				match(MINUS_ASSIGN);
				break;
			}
			case STAR_ASSIGN:
			{
				AST tmp224_AST = null;
				tmp224_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp224_AST);
				match(STAR_ASSIGN);
				break;
			}
			case DIV_ASSIGN:
			{
				AST tmp225_AST = null;
				tmp225_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp225_AST);
				match(DIV_ASSIGN);
				break;
			}
			case MOD_ASSIGN:
			{
				AST tmp226_AST = null;
				tmp226_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp226_AST);
				match(MOD_ASSIGN);
				break;
			}
			case SR_ASSIGN:
			{
				AST tmp227_AST = null;
				tmp227_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp227_AST);
				match(SR_ASSIGN);
				break;
			}
			case BSR_ASSIGN:
			{
				AST tmp228_AST = null;
				tmp228_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp228_AST);
				match(BSR_ASSIGN);
				break;
			}
			case SL_ASSIGN:
			{
				AST tmp229_AST = null;
				tmp229_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp229_AST);
				match(SL_ASSIGN);
				break;
			}
			case BAND_ASSIGN:
			{
				AST tmp230_AST = null;
				tmp230_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp230_AST);
				match(BAND_ASSIGN);
				break;
			}
			case BXOR_ASSIGN:
			{
				AST tmp231_AST = null;
				tmp231_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp231_AST);
				match(BXOR_ASSIGN);
				break;
			}
			case BOR_ASSIGN:
			{
				AST tmp232_AST = null;
				tmp232_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp232_AST);
				match(BOR_ASSIGN);
				break;
			}
			case STAR_STAR_ASSIGN:
			{
				AST tmp233_AST = null;
				tmp233_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp233_AST);
				match(STAR_STAR_ASSIGN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			nls();
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			assignmentTail_AST = (AST)currentAST.root;
			break;
		}
		case INC:
		{
			if ( inputState.guessing==0 ) {
				assignmentTail_AST = (AST)currentAST.root;
				assignmentTail_AST = head;
				currentAST.root = assignmentTail_AST;
				currentAST.child = assignmentTail_AST!=null &&assignmentTail_AST.getFirstChild()!=null ?
					assignmentTail_AST.getFirstChild() : assignmentTail_AST;
				currentAST.advanceChildToEnd();
			}
			in = LT(1);
			in_AST = astFactory.create(in);
			astFactory.makeASTRoot(currentAST, in_AST);
			match(INC);
			if ( inputState.guessing==0 ) {
				in_AST.setType(POST_INC);
			}
			assignmentTail_AST = (AST)currentAST.root;
			break;
		}
		case DEC:
		{
			if ( inputState.guessing==0 ) {
				assignmentTail_AST = (AST)currentAST.root;
				assignmentTail_AST = head;
				currentAST.root = assignmentTail_AST;
				currentAST.child = assignmentTail_AST!=null &&assignmentTail_AST.getFirstChild()!=null ?
					assignmentTail_AST.getFirstChild() : assignmentTail_AST;
				currentAST.advanceChildToEnd();
			}
			de = LT(1);
			de_AST = astFactory.create(de);
			astFactory.makeASTRoot(currentAST, de_AST);
			match(DEC);
			if ( inputState.guessing==0 ) {
				de_AST.setType(POST_DEC);
			}
			assignmentTail_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = assignmentTail_AST;
	}
	
	public final void aCase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST aCase_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_case:
		{
			AST tmp234_AST = null;
			tmp234_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp234_AST);
			match(LITERAL_case);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_default:
		{
			AST tmp235_AST = null;
			tmp235_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp235_AST);
			match(LITERAL_default);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(COLON);
		nls();
		aCase_AST = (AST)currentAST.root;
		returnAST = aCase_AST;
	}
	
	public final void caseSList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST caseSList_AST = null;
		
		statement();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop936:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case DEF:
				case LITERAL_import:
				case LITERAL_static:
				case AT:
				case LBRACK:
				case IDENT:
				case LPAREN:
				case LITERAL_class:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_any:
				case STAR:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_this:
				case STRING_LITERAL:
				case LITERAL_if:
				case LITERAL_while:
				case LITERAL_with:
				case LITERAL_switch:
				case LITERAL_for:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case INC:
				case DEC:
				case LITERAL_try:
				case PLUS:
				case MINUS:
				case BNOT:
				case LNOT:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				case STRING_CTOR_START:
				case LITERAL_new:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					statement();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case LITERAL_default:
				case LITERAL_case:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop936;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			caseSList_AST = (AST)currentAST.root;
			caseSList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SLIST,"SLIST")).add(caseSList_AST));
			currentAST.root = caseSList_AST;
			currentAST.child = caseSList_AST!=null &&caseSList_AST.getFirstChild()!=null ?
				caseSList_AST.getFirstChild() : caseSList_AST;
			currentAST.advanceChildToEnd();
		}
		caseSList_AST = (AST)currentAST.root;
		returnAST = caseSList_AST;
	}
	
	public final void controlExpressionList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST controlExpressionList_AST = null;
		
		controlExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop961:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				controlExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop961;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			controlExpressionList_AST = (AST)currentAST.root;
			controlExpressionList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ELIST,"ELIST")).add(controlExpressionList_AST));
			currentAST.root = controlExpressionList_AST;
			currentAST.child = controlExpressionList_AST!=null &&controlExpressionList_AST.getFirstChild()!=null ?
				controlExpressionList_AST.getFirstChild() : controlExpressionList_AST;
			currentAST.advanceChildToEnd();
		}
		controlExpressionList_AST = (AST)currentAST.root;
		returnAST = controlExpressionList_AST;
	}
	
	public final void handler() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST handler_AST = null;
		
		AST tmp238_AST = null;
		tmp238_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp238_AST);
		match(LITERAL_catch);
		match(LPAREN);
		parameterDeclaration();
		astFactory.addASTChild(currentAST, returnAST);
		match(RPAREN);
		nlsWarn();
		compoundStatement();
		astFactory.addASTChild(currentAST, returnAST);
		handler_AST = (AST)currentAST.root;
		returnAST = handler_AST;
	}
	
	public final void finallyClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST finallyClause_AST = null;
		
		AST tmp241_AST = null;
		tmp241_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp241_AST);
		match(LITERAL_finally);
		nlsWarn();
		compoundStatement();
		astFactory.addASTChild(currentAST, returnAST);
		finallyClause_AST = (AST)currentAST.root;
		returnAST = finallyClause_AST;
	}
	
	public final void assignmentOp() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignmentOp_AST = null;
		
		{
		switch ( LA(1)) {
		case ASSIGN:
		{
			AST tmp242_AST = null;
			tmp242_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp242_AST);
			match(ASSIGN);
			break;
		}
		case PLUS_ASSIGN:
		{
			AST tmp243_AST = null;
			tmp243_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp243_AST);
			match(PLUS_ASSIGN);
			break;
		}
		case MINUS_ASSIGN:
		{
			AST tmp244_AST = null;
			tmp244_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp244_AST);
			match(MINUS_ASSIGN);
			break;
		}
		case STAR_ASSIGN:
		{
			AST tmp245_AST = null;
			tmp245_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp245_AST);
			match(STAR_ASSIGN);
			break;
		}
		case DIV_ASSIGN:
		{
			AST tmp246_AST = null;
			tmp246_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp246_AST);
			match(DIV_ASSIGN);
			break;
		}
		case MOD_ASSIGN:
		{
			AST tmp247_AST = null;
			tmp247_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp247_AST);
			match(MOD_ASSIGN);
			break;
		}
		case SR_ASSIGN:
		{
			AST tmp248_AST = null;
			tmp248_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp248_AST);
			match(SR_ASSIGN);
			break;
		}
		case BSR_ASSIGN:
		{
			AST tmp249_AST = null;
			tmp249_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp249_AST);
			match(BSR_ASSIGN);
			break;
		}
		case SL_ASSIGN:
		{
			AST tmp250_AST = null;
			tmp250_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp250_AST);
			match(SL_ASSIGN);
			break;
		}
		case BAND_ASSIGN:
		{
			AST tmp251_AST = null;
			tmp251_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp251_AST);
			match(BAND_ASSIGN);
			break;
		}
		case BXOR_ASSIGN:
		{
			AST tmp252_AST = null;
			tmp252_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp252_AST);
			match(BXOR_ASSIGN);
			break;
		}
		case BOR_ASSIGN:
		{
			AST tmp253_AST = null;
			tmp253_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp253_AST);
			match(BOR_ASSIGN);
			break;
		}
		case STAR_STAR_ASSIGN:
		{
			AST tmp254_AST = null;
			tmp254_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp254_AST);
			match(STAR_STAR_ASSIGN);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		assignmentOp_AST = (AST)currentAST.root;
		returnAST = assignmentOp_AST;
	}
	
/** Used for backward compatibility, in a few places where
 *  Java expresion statements and declarations are required.
 */
	public final void controlExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST controlExpression_AST = null;
		AST head_AST = null;
		boolean zz; /*ignore*/
		
		boolean synPredMatched964 = false;
		if (((_tokenSet_15.member(LA(1))) && (_tokenSet_86.member(LA(2))) && (_tokenSet_119.member(LA(3))))) {
			int _m964 = mark();
			synPredMatched964 = true;
			inputState.guessing++;
			try {
				{
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched964 = false;
			}
			rewind(_m964);
			inputState.guessing--;
		}
		if ( synPredMatched964 ) {
			singleDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			controlExpression_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_120.member(LA(1))) && (_tokenSet_121.member(LA(2))) && (_tokenSet_110.member(LA(3)))) {
			zz=pathExpression();
			head_AST = (AST)returnAST;
			{
			switch ( LA(1)) {
			case ASSIGN:
			case INC:
			case DEC:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case DIV_ASSIGN:
			case MOD_ASSIGN:
			case SR_ASSIGN:
			case BSR_ASSIGN:
			case SL_ASSIGN:
			case BAND_ASSIGN:
			case BXOR_ASSIGN:
			case BOR_ASSIGN:
			case STAR_STAR_ASSIGN:
			{
				assignmentTail(head_AST);
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RPAREN:
			case COMMA:
			case SEMI:
			{
				if ( inputState.guessing==0 ) {
					controlExpression_AST = (AST)currentAST.root;
					controlExpression_AST = head_AST;
					currentAST.root = controlExpression_AST;
					currentAST.child = controlExpression_AST!=null &&controlExpression_AST.getFirstChild()!=null ?
						controlExpression_AST.getFirstChild() : controlExpression_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			controlExpression_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==INC||LA(1)==DEC)) {
			{
			switch ( LA(1)) {
			case INC:
			{
				AST tmp255_AST = null;
				tmp255_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp255_AST);
				match(INC);
				break;
			}
			case DEC:
			{
				AST tmp256_AST = null;
				tmp256_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp256_AST);
				match(DEC);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			zz=pathExpression();
			astFactory.addASTChild(currentAST, returnAST);
			controlExpression_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = controlExpression_AST;
	}
	
	public final void primaryExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST primaryExpression_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp257_AST = null;
			tmp257_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp257_AST);
			match(IDENT);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case STRING_LITERAL:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			constant();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_true:
		{
			AST tmp258_AST = null;
			tmp258_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp258_AST);
			match(LITERAL_true);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_false:
		{
			AST tmp259_AST = null;
			tmp259_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp259_AST);
			match(LITERAL_false);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_null:
		{
			AST tmp260_AST = null;
			tmp260_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp260_AST);
			match(LITERAL_null);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_new:
		{
			newExpression();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_this:
		{
			AST tmp261_AST = null;
			tmp261_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp261_AST);
			match(LITERAL_this);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_super:
		{
			AST tmp262_AST = null;
			tmp262_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp262_AST);
			match(LITERAL_super);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LPAREN:
		{
			parenthesizedExpression();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case STRING_CTOR_START:
		{
			stringConstructorExpression();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			builtInType();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = primaryExpression_AST;
	}
	
	public final boolean  pathExpressionTail(
		AST result
	) throws RecognitionException, TokenStreamException {
		boolean endBrackets = false;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathExpressionTail_AST = null;
		AST pe_AST = null;
		
		{
		_loop970:
		do {
			if ((_tokenSet_122.member(LA(1))) && (_tokenSet_123.member(LA(2))) && (_tokenSet_88.member(LA(3)))) {
				endBrackets=pathElement(result);
				pe_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					result = pe_AST;
				}
			}
			else {
				break _loop970;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			pathExpressionTail_AST = (AST)currentAST.root;
			pathExpressionTail_AST = result;
			currentAST.root = pathExpressionTail_AST;
			currentAST.child = pathExpressionTail_AST!=null &&pathExpressionTail_AST.getFirstChild()!=null ?
				pathExpressionTail_AST.getFirstChild() : pathExpressionTail_AST;
			currentAST.advanceChildToEnd();
		}
		pathExpressionTail_AST = (AST)currentAST.root;
		returnAST = pathExpressionTail_AST;
		return endBrackets;
	}
	
	public final boolean  pathElement(
		AST prefix
	) throws RecognitionException, TokenStreamException {
		boolean endBrackets = false;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathElement_AST = null;
		Token  sp = null;
		AST sp_AST = null;
		Token  op = null;
		AST op_AST = null;
		AST mca_AST = null;
		AST ata_AST = null;
		boolean zz; /*ignore*/
		
		switch ( LA(1)) {
		case DOT:
		case STAR_DOT:
		case QUESTION_DOT:
		{
			if ( inputState.guessing==0 ) {
				pathElement_AST = (AST)currentAST.root;
				pathElement_AST = prefix;
				currentAST.root = pathElement_AST;
				currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
					pathElement_AST.getFirstChild() : pathElement_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case STAR_DOT:
			{
				sp = LT(1);
				sp_AST = astFactory.create(sp);
				astFactory.makeASTRoot(currentAST, sp_AST);
				match(STAR_DOT);
				if ( inputState.guessing==0 ) {
					sp_AST.setType(SPREAD_ARG);
				}
				break;
			}
			case QUESTION_DOT:
			{
				op = LT(1);
				op_AST = astFactory.create(op);
				astFactory.makeASTRoot(currentAST, op_AST);
				match(QUESTION_DOT);
				if ( inputState.guessing==0 ) {
					op_AST.setType(OPTIONAL_ARG);
				}
				break;
			}
			case DOT:
			{
				AST tmp263_AST = null;
				tmp263_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp263_AST);
				match(DOT);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			nls();
			namePart();
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				endBrackets = false;
			}
			pathElement_AST = (AST)currentAST.root;
			break;
		}
		case LPAREN:
		case LCURLY:
		{
			methodCallArgs(prefix);
			mca_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				pathElement_AST = (AST)currentAST.root;
				pathElement_AST = mca_AST; endBrackets = true;
				currentAST.root = pathElement_AST;
				currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
					pathElement_AST.getFirstChild() : pathElement_AST;
				currentAST.advanceChildToEnd();
			}
			pathElement_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		{
			arrayOrTypeArgs(prefix);
			ata_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				pathElement_AST = (AST)currentAST.root;
				pathElement_AST = ata_AST; endBrackets = false;
				currentAST.root = pathElement_AST;
				currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
					pathElement_AST.getFirstChild() : pathElement_AST;
				currentAST.advanceChildToEnd();
			}
			pathElement_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = pathElement_AST;
		return endBrackets;
	}
	
	public final void pathExpressionFromBrackets() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathExpressionFromBrackets_AST = null;
		AST pe_AST = null;
		AST pe2_AST = null;
		boolean zz; /*ignore*/
		
		switch ( LA(1)) {
		case LCURLY:
		{
			expressionBlock();
			pe_AST = (AST)returnAST;
			zz=pathExpressionTail(pe_AST);
			astFactory.addASTChild(currentAST, returnAST);
			pathExpressionFromBrackets_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		{
			listOrMapConstructorExpression();
			pe2_AST = (AST)returnAST;
			zz=pathExpressionTail(pe2_AST);
			astFactory.addASTChild(currentAST, returnAST);
			pathExpressionFromBrackets_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = pathExpressionFromBrackets_AST;
	}
	
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
	public final void listOrMapConstructorExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST listOrMapConstructorExpression_AST = null;
		Token  lcon = null;
		AST lcon_AST = null;
		boolean hasLabels = false, hal;
		
		lcon = LT(1);
		lcon_AST = astFactory.create(lcon);
		astFactory.makeASTRoot(currentAST, lcon_AST);
		match(LBRACK);
		{
		switch ( LA(1)) {
		case COLON:
		{
			match(COLON);
			if ( inputState.guessing==0 ) {
				hasLabels |= true;
			}
			break;
		}
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case LBRACK:
		case RBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_class:
		case QUESTION:
		case LITERAL_super:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case STAR:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case INC:
		case DEC:
		case PLUS:
		case MINUS:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		hal=argList();
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			hasLabels |= hal;
		}
		match(RBRACK);
		if ( inputState.guessing==0 ) {
			lcon_AST.setType(hasLabels ? MAP_CONSTRUCTOR : LIST_CONSTRUCTOR);
		}
		listOrMapConstructorExpression_AST = (AST)currentAST.root;
		returnAST = listOrMapConstructorExpression_AST;
	}
	
/** This is the grammar for what can follow a dot:  x.a, x.@a, x.&a, x.'a', etc.
 */
	public final void namePart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST namePart_AST = null;
		Token  amp = null;
		AST amp_AST = null;
		Token  ats = null;
		AST ats_AST = null;
		Token  sl = null;
		AST sl_AST = null;
		AST dn_AST = null;
		
		{
		switch ( LA(1)) {
		case LAND:
		{
			amp = LT(1);
			amp_AST = astFactory.create(amp);
			astFactory.makeASTRoot(currentAST, amp_AST);
			match(LAND);
			if ( inputState.guessing==0 ) {
				amp_AST.setType(REFLECT_MEMBER);
			}
			break;
		}
		case AT:
		{
			ats = LT(1);
			ats_AST = astFactory.create(ats);
			astFactory.makeASTRoot(currentAST, ats_AST);
			match(AT);
			if ( inputState.guessing==0 ) {
				ats_AST.setType(SELECT_SLOT);
			}
			break;
		}
		case DEF:
		case IDENT:
		case LPAREN:
		case LITERAL_class:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LCURLY:
		case STRING_LITERAL:
		case LITERAL_in:
		case STRING_CTOR_START:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp266_AST = null;
			tmp266_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp266_AST);
			match(IDENT);
			break;
		}
		case STRING_LITERAL:
		{
			sl = LT(1);
			sl_AST = astFactory.create(sl);
			astFactory.addASTChild(currentAST, sl_AST);
			match(STRING_LITERAL);
			if ( inputState.guessing==0 ) {
				sl_AST.setType(IDENT);
			}
			break;
		}
		case LPAREN:
		case STRING_CTOR_START:
		{
			dynamicMemberName();
			dn_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				namePart_AST = (AST)currentAST.root;
				namePart_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(DYNAMIC_MEMBER,"DYNAMIC_MEMBER")).add(dn_AST));
				currentAST.root = namePart_AST;
				currentAST.child = namePart_AST!=null &&namePart_AST.getFirstChild()!=null ?
					namePart_AST.getFirstChild() : namePart_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case LCURLY:
		{
			openBlock();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case DEF:
		case LITERAL_class:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_in:
		{
			keywordPropertyNames();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		namePart_AST = (AST)currentAST.root;
		returnAST = namePart_AST;
	}
	
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
	public final void methodCallArgs(
		AST callee
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST methodCallArgs_AST = null;
		Token  lp = null;
		AST lp_AST = null;
		AST cb_AST = null;
		boolean zz; /*ignore*/
		
		switch ( LA(1)) {
		case LPAREN:
		{
			if ( inputState.guessing==0 ) {
				methodCallArgs_AST = (AST)currentAST.root;
				methodCallArgs_AST = callee;
				currentAST.root = methodCallArgs_AST;
				currentAST.child = methodCallArgs_AST!=null &&methodCallArgs_AST.getFirstChild()!=null ?
					methodCallArgs_AST.getFirstChild() : methodCallArgs_AST;
				currentAST.advanceChildToEnd();
			}
			lp = LT(1);
			lp_AST = astFactory.create(lp);
			astFactory.makeASTRoot(currentAST, lp_AST);
			match(LPAREN);
			if ( inputState.guessing==0 ) {
				lp_AST.setType(METHOD_CALL);
			}
			zz=argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			{
			if ((LA(1)==LCURLY) && (_tokenSet_24.member(LA(2))) && (_tokenSet_88.member(LA(3)))) {
				appendedBlock();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_124.member(LA(1))) && (_tokenSet_8.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			methodCallArgs_AST = (AST)currentAST.root;
			break;
		}
		case LCURLY:
		{
			if ( inputState.guessing==0 ) {
				methodCallArgs_AST = (AST)currentAST.root;
				methodCallArgs_AST = callee;
				currentAST.root = methodCallArgs_AST;
				currentAST.child = methodCallArgs_AST!=null &&methodCallArgs_AST.getFirstChild()!=null ?
					methodCallArgs_AST.getFirstChild() : methodCallArgs_AST;
				currentAST.advanceChildToEnd();
			}
			appendedBlock();
			cb_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				methodCallArgs_AST = (AST)currentAST.root;
				AST lbrace = getASTFactory().dup(cb_AST);
				lbrace.setType(METHOD_CALL);
				methodCallArgs_AST = (AST)astFactory.make( (new ASTArray(2)).add(lbrace).add(methodCallArgs_AST));
				
				currentAST.root = methodCallArgs_AST;
				currentAST.child = methodCallArgs_AST!=null &&methodCallArgs_AST.getFirstChild()!=null ?
					methodCallArgs_AST.getFirstChild() : methodCallArgs_AST;
				currentAST.advanceChildToEnd();
			}
			methodCallArgs_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = methodCallArgs_AST;
	}
	
/** Lookahead pattern for pathElement. */
	public final void pathElementStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathElementStart_AST = null;
		
		{
		switch ( LA(1)) {
		case LPAREN:
		{
			AST tmp268_AST = null;
			tmp268_AST = astFactory.create(LT(1));
			match(LPAREN);
			break;
		}
		case LBRACE:
		{
			AST tmp269_AST = null;
			tmp269_AST = astFactory.create(LT(1));
			match(LBRACE);
			break;
		}
		case LBRACK:
		{
			AST tmp270_AST = null;
			tmp270_AST = astFactory.create(LT(1));
			match(LBRACK);
			break;
		}
		case DOT:
		{
			AST tmp271_AST = null;
			tmp271_AST = astFactory.create(LT(1));
			match(DOT);
			break;
		}
		case QUESTION:
		case STAR:
		{
			{
			switch ( LA(1)) {
			case STAR:
			{
				AST tmp272_AST = null;
				tmp272_AST = astFactory.create(LT(1));
				match(STAR);
				break;
			}
			case QUESTION:
			{
				AST tmp273_AST = null;
				tmp273_AST = astFactory.create(LT(1));
				match(QUESTION);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			AST tmp274_AST = null;
			tmp274_AST = astFactory.create(LT(1));
			match(DOT);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = pathElementStart_AST;
	}
	
/** If a dot is followed by a parenthesized or quoted expression, the member is computed dynamically,
 *  and the member selection is done only at runtime.  This forces a statically unchecked member access.
 */
	public final void dynamicMemberName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST dynamicMemberName_AST = null;
		
		{
		switch ( LA(1)) {
		case LPAREN:
		{
			parenthesizedExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case STRING_CTOR_START:
		{
			stringConstructorExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			dynamicMemberName_AST = (AST)currentAST.root;
			dynamicMemberName_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(DYNAMIC_MEMBER,"DYNAMIC_MEMBER")).add(dynamicMemberName_AST));
			currentAST.root = dynamicMemberName_AST;
			currentAST.child = dynamicMemberName_AST!=null &&dynamicMemberName_AST.getFirstChild()!=null ?
				dynamicMemberName_AST.getFirstChild() : dynamicMemberName_AST;
			currentAST.advanceChildToEnd();
		}
		dynamicMemberName_AST = (AST)currentAST.root;
		returnAST = dynamicMemberName_AST;
	}
	
/** Allowed keywords after dot
*/
	public final void keywordPropertyNames() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keywordPropertyNames_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_class:
		{
			AST tmp275_AST = null;
			tmp275_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp275_AST);
			match(LITERAL_class);
			break;
		}
		case LITERAL_in:
		{
			AST tmp276_AST = null;
			tmp276_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp276_AST);
			match(LITERAL_in);
			break;
		}
		case DEF:
		{
			AST tmp277_AST = null;
			tmp277_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp277_AST);
			match(DEF);
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			builtInType();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			keywordPropertyNames_AST = (AST)currentAST.root;
			keywordPropertyNames_AST.setType(IDENT);
		}
		keywordPropertyNames_AST = (AST)currentAST.root;
		returnAST = keywordPropertyNames_AST;
	}
	
	public final void parenthesizedExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parenthesizedExpression_AST = null;
		Token  lp = null;
		AST lp_AST = null;
		
		lp = LT(1);
		lp_AST = astFactory.create(lp);
		astFactory.makeASTRoot(currentAST, lp_AST);
		match(LPAREN);
		expression();
		astFactory.addASTChild(currentAST, returnAST);
		match(RPAREN);
		if ( inputState.guessing==0 ) {
			lp_AST.setType(EXPR);
		}
		parenthesizedExpression_AST = (AST)currentAST.root;
		returnAST = parenthesizedExpression_AST;
	}
	
	public final void stringConstructorExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST stringConstructorExpression_AST = null;
		Token  cs = null;
		AST cs_AST = null;
		Token  cm = null;
		AST cm_AST = null;
		Token  ce = null;
		AST ce_AST = null;
		
		cs = LT(1);
		cs_AST = astFactory.create(cs);
		astFactory.addASTChild(currentAST, cs_AST);
		match(STRING_CTOR_START);
		if ( inputState.guessing==0 ) {
			cs_AST.setType(STRING_LITERAL);
		}
		stringConstructorValuePart();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1068:
		do {
			if ((LA(1)==STRING_CTOR_MIDDLE)) {
				cm = LT(1);
				cm_AST = astFactory.create(cm);
				astFactory.addASTChild(currentAST, cm_AST);
				match(STRING_CTOR_MIDDLE);
				if ( inputState.guessing==0 ) {
					cm_AST.setType(STRING_LITERAL);
				}
				stringConstructorValuePart();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1068;
			}
			
		} while (true);
		}
		ce = LT(1);
		ce_AST = astFactory.create(ce);
		astFactory.addASTChild(currentAST, ce_AST);
		match(STRING_CTOR_END);
		if ( inputState.guessing==0 ) {
			stringConstructorExpression_AST = (AST)currentAST.root;
			ce_AST.setType(STRING_LITERAL);
			stringConstructorExpression_AST =
			(AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(STRING_CONSTRUCTOR,"STRING_CONSTRUCTOR")).add(stringConstructorExpression_AST));
			
			currentAST.root = stringConstructorExpression_AST;
			currentAST.child = stringConstructorExpression_AST!=null &&stringConstructorExpression_AST.getFirstChild()!=null ?
				stringConstructorExpression_AST.getFirstChild() : stringConstructorExpression_AST;
			currentAST.advanceChildToEnd();
		}
		stringConstructorExpression_AST = (AST)currentAST.root;
		returnAST = stringConstructorExpression_AST;
	}
	
	public final void logicalOrExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalOrExpression_AST = null;
		
		logicalAndExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop996:
		do {
			if ((LA(1)==LOR)) {
				AST tmp279_AST = null;
				tmp279_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp279_AST);
				match(LOR);
				nls();
				logicalAndExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop996;
			}
			
		} while (true);
		}
		logicalOrExpression_AST = (AST)currentAST.root;
		returnAST = logicalOrExpression_AST;
	}
	
	public final void logicalAndExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalAndExpression_AST = null;
		
		inclusiveOrExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop999:
		do {
			if ((LA(1)==LAND)) {
				AST tmp280_AST = null;
				tmp280_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp280_AST);
				match(LAND);
				nls();
				inclusiveOrExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop999;
			}
			
		} while (true);
		}
		logicalAndExpression_AST = (AST)currentAST.root;
		returnAST = logicalAndExpression_AST;
	}
	
	public final void inclusiveOrExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST inclusiveOrExpression_AST = null;
		
		exclusiveOrExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1002:
		do {
			if ((LA(1)==BOR) && (_tokenSet_125.member(LA(2))) && (_tokenSet_88.member(LA(3)))) {
				AST tmp281_AST = null;
				tmp281_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp281_AST);
				match(BOR);
				nls();
				exclusiveOrExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1002;
			}
			
		} while (true);
		}
		inclusiveOrExpression_AST = (AST)currentAST.root;
		returnAST = inclusiveOrExpression_AST;
	}
	
	public final void exclusiveOrExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST exclusiveOrExpression_AST = null;
		
		andExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1005:
		do {
			if ((LA(1)==BXOR)) {
				AST tmp282_AST = null;
				tmp282_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp282_AST);
				match(BXOR);
				nls();
				andExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1005;
			}
			
		} while (true);
		}
		exclusiveOrExpression_AST = (AST)currentAST.root;
		returnAST = exclusiveOrExpression_AST;
	}
	
	public final void andExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST andExpression_AST = null;
		
		regexExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1008:
		do {
			if ((LA(1)==BAND)) {
				AST tmp283_AST = null;
				tmp283_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp283_AST);
				match(BAND);
				nls();
				regexExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1008;
			}
			
		} while (true);
		}
		andExpression_AST = (AST)currentAST.root;
		returnAST = andExpression_AST;
	}
	
	public final void regexExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST regexExpression_AST = null;
		
		equalityExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1012:
		do {
			if ((LA(1)==REGEX_FIND||LA(1)==REGEX_MATCH)) {
				{
				switch ( LA(1)) {
				case REGEX_FIND:
				{
					AST tmp284_AST = null;
					tmp284_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp284_AST);
					match(REGEX_FIND);
					break;
				}
				case REGEX_MATCH:
				{
					AST tmp285_AST = null;
					tmp285_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp285_AST);
					match(REGEX_MATCH);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				equalityExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1012;
			}
			
		} while (true);
		}
		regexExpression_AST = (AST)currentAST.root;
		returnAST = regexExpression_AST;
	}
	
	public final void equalityExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST equalityExpression_AST = null;
		
		relationalExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1016:
		do {
			if (((LA(1) >= NOT_EQUAL && LA(1) <= COMPARE_TO))) {
				{
				switch ( LA(1)) {
				case NOT_EQUAL:
				{
					AST tmp286_AST = null;
					tmp286_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp286_AST);
					match(NOT_EQUAL);
					break;
				}
				case EQUAL:
				{
					AST tmp287_AST = null;
					tmp287_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp287_AST);
					match(EQUAL);
					break;
				}
				case COMPARE_TO:
				{
					AST tmp288_AST = null;
					tmp288_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp288_AST);
					match(COMPARE_TO);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				relationalExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1016;
			}
			
		} while (true);
		}
		equalityExpression_AST = (AST)currentAST.root;
		returnAST = equalityExpression_AST;
	}
	
	public final void relationalExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relationalExpression_AST = null;
		
		shiftExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case EOF:
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case RBRACK:
		case IDENT:
		case RPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case QUESTION:
		case LT:
		case COMMA:
		case GT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case ASSIGN:
		case LCURLY:
		case RCURLY:
		case BAND:
		case SEMI:
		case LITERAL_default:
		case BOR:
		case LOR:
		case COLON:
		case LITERAL_else:
		case LITERAL_in:
		case LITERAL_case:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		case LAND:
		case BXOR:
		case REGEX_FIND:
		case REGEX_MATCH:
		case NOT_EQUAL:
		case EQUAL:
		case COMPARE_TO:
		case LE:
		case GE:
		case NLS:
		{
			{
			switch ( LA(1)) {
			case LT:
			case GT:
			case LITERAL_in:
			case LE:
			case GE:
			{
				{
				switch ( LA(1)) {
				case LT:
				{
					AST tmp289_AST = null;
					tmp289_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp289_AST);
					match(LT);
					break;
				}
				case GT:
				{
					AST tmp290_AST = null;
					tmp290_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp290_AST);
					match(GT);
					break;
				}
				case LE:
				{
					AST tmp291_AST = null;
					tmp291_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp291_AST);
					match(LE);
					break;
				}
				case GE:
				{
					AST tmp292_AST = null;
					tmp292_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp292_AST);
					match(GE);
					break;
				}
				case LITERAL_in:
				{
					AST tmp293_AST = null;
					tmp293_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp293_AST);
					match(LITERAL_in);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				shiftExpression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case DEF:
			case LITERAL_static:
			case AT:
			case RBRACK:
			case IDENT:
			case RPAREN:
			case LITERAL_class:
			case LITERAL_interface:
			case LITERAL_enum:
			case QUESTION:
			case COMMA:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_any:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case ASSIGN:
			case LCURLY:
			case RCURLY:
			case BAND:
			case SEMI:
			case LITERAL_default:
			case BOR:
			case LOR:
			case COLON:
			case LITERAL_else:
			case LITERAL_case:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case DIV_ASSIGN:
			case MOD_ASSIGN:
			case SR_ASSIGN:
			case BSR_ASSIGN:
			case SL_ASSIGN:
			case BAND_ASSIGN:
			case BXOR_ASSIGN:
			case BOR_ASSIGN:
			case STAR_STAR_ASSIGN:
			case LAND:
			case BXOR:
			case REGEX_FIND:
			case REGEX_MATCH:
			case NOT_EQUAL:
			case EQUAL:
			case COMPARE_TO:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case LITERAL_instanceof:
		{
			AST tmp294_AST = null;
			tmp294_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp294_AST);
			match(LITERAL_instanceof);
			nls();
			typeSpec(true);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_as:
		{
			AST tmp295_AST = null;
			tmp295_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp295_AST);
			match(LITERAL_as);
			nls();
			typeSpec(true);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		relationalExpression_AST = (AST)currentAST.root;
		returnAST = relationalExpression_AST;
	}
	
	public final void additiveExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST additiveExpression_AST = null;
		
		multiplicativeExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1029:
		do {
			if ((LA(1)==PLUS||LA(1)==MINUS)) {
				{
				switch ( LA(1)) {
				case PLUS:
				{
					AST tmp296_AST = null;
					tmp296_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp296_AST);
					match(PLUS);
					break;
				}
				case MINUS:
				{
					AST tmp297_AST = null;
					tmp297_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp297_AST);
					match(MINUS);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				multiplicativeExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1029;
			}
			
		} while (true);
		}
		additiveExpression_AST = (AST)currentAST.root;
		returnAST = additiveExpression_AST;
	}
	
	public final void multiplicativeExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multiplicativeExpression_AST = null;
		
		switch ( LA(1)) {
		case INC:
		{
			{
			AST tmp298_AST = null;
			tmp298_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp298_AST);
			match(INC);
			nls();
			powerExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop1034:
			do {
				if ((_tokenSet_126.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp299_AST = null;
						tmp299_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp299_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp300_AST = null;
						tmp300_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp300_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp301_AST = null;
						tmp301_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp301_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop1034;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case DEC:
		{
			{
			AST tmp302_AST = null;
			tmp302_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp302_AST);
			match(DEC);
			nls();
			powerExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop1038:
			do {
				if ((_tokenSet_126.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp303_AST = null;
						tmp303_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp303_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp304_AST = null;
						tmp304_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp304_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp305_AST = null;
						tmp305_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp305_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop1038;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case MINUS:
		{
			{
			AST tmp306_AST = null;
			tmp306_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp306_AST);
			match(MINUS);
			if ( inputState.guessing==0 ) {
				tmp306_AST.setType(UNARY_MINUS);
			}
			nls();
			powerExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop1042:
			do {
				if ((_tokenSet_126.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp307_AST = null;
						tmp307_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp307_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp308_AST = null;
						tmp308_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp308_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp309_AST = null;
						tmp309_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp309_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop1042;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case PLUS:
		{
			{
			AST tmp310_AST = null;
			tmp310_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp310_AST);
			match(PLUS);
			if ( inputState.guessing==0 ) {
				tmp310_AST.setType(UNARY_PLUS);
			}
			nls();
			powerExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop1046:
			do {
				if ((_tokenSet_126.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp311_AST = null;
						tmp311_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp311_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp312_AST = null;
						tmp312_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp312_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp313_AST = null;
						tmp313_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp313_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop1046;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			{
			powerExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop1050:
			do {
				if ((_tokenSet_126.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp314_AST = null;
						tmp314_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp314_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp315_AST = null;
						tmp315_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp315_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp316_AST = null;
						tmp316_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp316_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop1050;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = multiplicativeExpression_AST;
	}
	
	public final void powerExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST powerExpression_AST = null;
		
		unaryExpressionNotPlusMinus();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop1053:
		do {
			if ((LA(1)==STAR_STAR)) {
				AST tmp317_AST = null;
				tmp317_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp317_AST);
				match(STAR_STAR);
				nls();
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop1053;
			}
			
		} while (true);
		}
		powerExpression_AST = (AST)currentAST.root;
		returnAST = powerExpression_AST;
	}
	
	public final void unaryExpressionNotPlusMinus() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpressionNotPlusMinus_AST = null;
		Token  lpb = null;
		AST lpb_AST = null;
		Token  lp = null;
		AST lp_AST = null;
		
		switch ( LA(1)) {
		case BNOT:
		{
			AST tmp318_AST = null;
			tmp318_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp318_AST);
			match(BNOT);
			nls();
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
			break;
		}
		case LNOT:
		{
			AST tmp319_AST = null;
			tmp319_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp319_AST);
			match(LNOT);
			nls();
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			{
			boolean synPredMatched1058 = false;
			if (((LA(1)==LPAREN) && ((LA(2) >= LITERAL_void && LA(2) <= LITERAL_any)) && (LA(3)==LBRACK||LA(3)==RPAREN))) {
				int _m1058 = mark();
				synPredMatched1058 = true;
				inputState.guessing++;
				try {
					{
					match(LPAREN);
					builtInTypeSpec(true);
					match(RPAREN);
					unaryExpression();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched1058 = false;
				}
				rewind(_m1058);
				inputState.guessing--;
			}
			if ( synPredMatched1058 ) {
				lpb = LT(1);
				lpb_AST = astFactory.create(lpb);
				astFactory.makeASTRoot(currentAST, lpb_AST);
				match(LPAREN);
				if ( inputState.guessing==0 ) {
					lpb_AST.setType(TYPECAST);
				}
				builtInTypeSpec(true);
				astFactory.addASTChild(currentAST, returnAST);
				match(RPAREN);
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				boolean synPredMatched1060 = false;
				if (((LA(1)==LPAREN) && (LA(2)==IDENT) && (_tokenSet_127.member(LA(3))))) {
					int _m1060 = mark();
					synPredMatched1060 = true;
					inputState.guessing++;
					try {
						{
						match(LPAREN);
						classTypeSpec(true);
						match(RPAREN);
						unaryExpressionNotPlusMinus();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched1060 = false;
					}
					rewind(_m1060);
					inputState.guessing--;
				}
				if ( synPredMatched1060 ) {
					lp = LT(1);
					lp_AST = astFactory.create(lp);
					astFactory.makeASTRoot(currentAST, lp_AST);
					match(LPAREN);
					if ( inputState.guessing==0 ) {
						lp_AST.setType(TYPECAST);
					}
					classTypeSpec(true);
					astFactory.addASTChild(currentAST, returnAST);
					match(RPAREN);
					unaryExpressionNotPlusMinus();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((_tokenSet_128.member(LA(1))) && (_tokenSet_129.member(LA(2))) && (_tokenSet_8.member(LA(3)))) {
					postfixExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			returnAST = unaryExpressionNotPlusMinus_AST;
		}
		
	public final void unaryExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpression_AST = null;
		
		switch ( LA(1)) {
		case INC:
		{
			AST tmp322_AST = null;
			tmp322_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp322_AST);
			match(INC);
			nls();
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case DEC:
		{
			AST tmp323_AST = null;
			tmp323_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp323_AST);
			match(DEC);
			nls();
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case MINUS:
		{
			AST tmp324_AST = null;
			tmp324_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp324_AST);
			match(MINUS);
			if ( inputState.guessing==0 ) {
				tmp324_AST.setType(UNARY_MINUS);
			}
			nls();
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case PLUS:
		{
			AST tmp325_AST = null;
			tmp325_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp325_AST);
			match(PLUS);
			if ( inputState.guessing==0 ) {
				tmp325_AST.setType(UNARY_PLUS);
			}
			nls();
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case DOLLAR:
		{
			AST tmp326_AST = null;
			tmp326_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp326_AST);
			match(DOLLAR);
			if ( inputState.guessing==0 ) {
				tmp326_AST.setType(SCOPE_ESCAPE);
			}
			nls();
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			unaryExpressionNotPlusMinus();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = unaryExpression_AST;
	}
	
	public final void postfixExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST postfixExpression_AST = null;
		Token  in = null;
		AST in_AST = null;
		Token  de = null;
		AST de_AST = null;
		boolean zz; /*ignored*/
		
		{
		switch ( LA(1)) {
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_this:
		case STRING_LITERAL:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			zz=pathExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LBRACK:
		case LCURLY:
		{
			pathExpressionFromBrackets();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case INC:
		{
			in = LT(1);
			in_AST = astFactory.create(in);
			astFactory.makeASTRoot(currentAST, in_AST);
			match(INC);
			if ( inputState.guessing==0 ) {
				in_AST.setType(POST_INC);
			}
			break;
		}
		case DEC:
		{
			de = LT(1);
			de_AST = astFactory.create(de);
			astFactory.makeASTRoot(currentAST, de_AST);
			match(DEC);
			if ( inputState.guessing==0 ) {
				de_AST.setType(POST_DEC);
			}
			break;
		}
		case EOF:
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case RBRACK:
		case IDENT:
		case RPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case QUESTION:
		case LT:
		case COMMA:
		case GT:
		case SR:
		case BSR:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case STAR:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case ASSIGN:
		case LCURLY:
		case RCURLY:
		case BAND:
		case SEMI:
		case LITERAL_default:
		case TRIPLE_DOT:
		case BOR:
		case LOR:
		case COLON:
		case LITERAL_else:
		case LITERAL_in:
		case LITERAL_case:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		case LAND:
		case BXOR:
		case REGEX_FIND:
		case REGEX_MATCH:
		case NOT_EQUAL:
		case EQUAL:
		case COMPARE_TO:
		case LE:
		case GE:
		case LITERAL_instanceof:
		case SL:
		case RANGE_INCLUSIVE:
		case PLUS:
		case MINUS:
		case DIV:
		case MOD:
		case STAR_STAR:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		postfixExpression_AST = (AST)currentAST.root;
		returnAST = postfixExpression_AST;
	}
	
	public final void constant() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constant_AST = null;
		
		switch ( LA(1)) {
		case NUM_INT:
		{
			AST tmp327_AST = null;
			tmp327_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp327_AST);
			match(NUM_INT);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case STRING_LITERAL:
		{
			AST tmp328_AST = null;
			tmp328_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp328_AST);
			match(STRING_LITERAL);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_FLOAT:
		{
			AST tmp329_AST = null;
			tmp329_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp329_AST);
			match(NUM_FLOAT);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_LONG:
		{
			AST tmp330_AST = null;
			tmp330_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp330_AST);
			match(NUM_LONG);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_DOUBLE:
		{
			AST tmp331_AST = null;
			tmp331_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp331_AST);
			match(NUM_DOUBLE);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_BIG_INT:
		{
			AST tmp332_AST = null;
			tmp332_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp332_AST);
			match(NUM_BIG_INT);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_BIG_DECIMAL:
		{
			AST tmp333_AST = null;
			tmp333_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp333_AST);
			match(NUM_BIG_DECIMAL);
			constant_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = constant_AST;
	}
	
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
	public final void newExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST newExpression_AST = null;
		boolean zz; /*ignored*/
		
		AST tmp334_AST = null;
		tmp334_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp334_AST);
		match(LITERAL_new);
		{
		switch ( LA(1)) {
		case LT:
		{
			typeArguments();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		type();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case LPAREN:
		{
			match(LPAREN);
			zz=argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			break;
		}
		case LBRACK:
		{
			newArrayDeclarator();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		newExpression_AST = (AST)currentAST.root;
		returnAST = newExpression_AST;
	}
	
	public final void stringConstructorValuePart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST stringConstructorValuePart_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			identifier();
			astFactory.addASTChild(currentAST, returnAST);
			stringConstructorValuePart_AST = (AST)currentAST.root;
			break;
		}
		case LCURLY:
		{
			openBlock();
			astFactory.addASTChild(currentAST, returnAST);
			stringConstructorValuePart_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = stringConstructorValuePart_AST;
	}
	
	public final void newArrayDeclarator() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST newArrayDeclarator_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		
		{
		int _cnt1095=0;
		_loop1095:
		do {
			if ((LA(1)==LBRACK) && (_tokenSet_130.member(LA(2))) && (_tokenSet_88.member(LA(3)))) {
				lb = LT(1);
				lb_AST = astFactory.create(lb);
				astFactory.makeASTRoot(currentAST, lb_AST);
				match(LBRACK);
				if ( inputState.guessing==0 ) {
					lb_AST.setType(ARRAY_DECLARATOR);
				}
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case DEF:
				case LITERAL_static:
				case AT:
				case LBRACK:
				case IDENT:
				case LPAREN:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_any:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_this:
				case STRING_LITERAL:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case INC:
				case DEC:
				case PLUS:
				case MINUS:
				case BNOT:
				case LNOT:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				case STRING_CTOR_START:
				case LITERAL_new:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RBRACK:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RBRACK);
			}
			else {
				if ( _cnt1095>=1 ) { break _loop1095; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt1095++;
		} while (true);
		}
		newArrayDeclarator_AST = (AST)currentAST.root;
		returnAST = newArrayDeclarator_AST;
	}
	
/** A single argument in (...) or [...].  Corresponds to to a method or closure parameter.
 *  May be labeled.  May be modified by the spread or optionality operators *, ?.
 */
	public final boolean  argument() throws RecognitionException, TokenStreamException {
		boolean hasLabel = false;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argument_AST = null;
		Token  c = null;
		AST c_AST = null;
		Token  sp = null;
		AST sp_AST = null;
		Token  op = null;
		AST op_AST = null;
		
		{
		boolean synPredMatched1083 = false;
		if (((_tokenSet_131.member(LA(1))) && (_tokenSet_132.member(LA(2))) && (_tokenSet_110.member(LA(3))))) {
			int _m1083 = mark();
			synPredMatched1083 = true;
			inputState.guessing++;
			try {
				{
				argumentLabelStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched1083 = false;
			}
			rewind(_m1083);
			inputState.guessing--;
		}
		if ( synPredMatched1083 ) {
			argumentLabel();
			astFactory.addASTChild(currentAST, returnAST);
			c = LT(1);
			c_AST = astFactory.create(c);
			astFactory.makeASTRoot(currentAST, c_AST);
			match(COLON);
			if ( inputState.guessing==0 ) {
				c_AST.setType(LABELED_ARG);
			}
		}
		else if ((_tokenSet_133.member(LA(1))) && (_tokenSet_70.member(LA(2))) && (_tokenSet_85.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		{
		switch ( LA(1)) {
		case STAR:
		{
			sp = LT(1);
			sp_AST = astFactory.create(sp);
			astFactory.makeASTRoot(currentAST, sp_AST);
			match(STAR);
			if ( inputState.guessing==0 ) {
				sp_AST.setType(SPREAD_ARG);
			}
			break;
		}
		case QUESTION:
		{
			op = LT(1);
			op_AST = astFactory.create(op);
			astFactory.makeASTRoot(currentAST, op_AST);
			match(QUESTION);
			if ( inputState.guessing==0 ) {
				op_AST.setType(OPTIONAL_ARG);
			}
			break;
		}
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case DEF:
		case LITERAL_static:
		case AT:
		case LBRACK:
		case IDENT:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case STRING_LITERAL:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case INC:
		case DEC:
		case PLUS:
		case MINUS:
		case BNOT:
		case LNOT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case STRING_CTOR_START:
		case LITERAL_new:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		expression();
		astFactory.addASTChild(currentAST, returnAST);
		argument_AST = (AST)currentAST.root;
		returnAST = argument_AST;
		return hasLabel;
	}
	
/** For lookahead only.  Fast approximate parse of a statementLabel followed by a colon. */
	public final void argumentLabelStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argumentLabelStart_AST = null;
		
		{
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp338_AST = null;
			tmp338_AST = astFactory.create(LT(1));
			match(IDENT);
			break;
		}
		case DEF:
		case LITERAL_class:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_any:
		case LITERAL_in:
		{
			keywordPropertyNames();
			break;
		}
		case STRING_LITERAL:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			constant();
			break;
		}
		case LBRACK:
		case LPAREN:
		case LCURLY:
		case STRING_CTOR_START:
		{
			balancedBrackets();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		AST tmp339_AST = null;
		tmp339_AST = astFactory.create(LT(1));
		match(COLON);
		returnAST = argumentLabelStart_AST;
	}
	
/** A label for an argument is of the form a:b, 'a':b, "a":b, (a):b, etc..
 *      The labels in (a:b), ('a':b), and ("a":b) are in all ways equivalent,
 *      except that the quotes allow more spellings.
 *  Equivalent dynamically computed labels are (('a'):b) and ("${'a'}":b)
 *  but not ((a):b) or "$a":b, since the latter cases evaluate (a) as a normal identifier.
 *      Bottom line:  If you want a truly variable label, use parens and say ((a):b).
 */
	public final void argumentLabel() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argumentLabel_AST = null;
		Token  id = null;
		AST id_AST = null;
		AST kw_AST = null;
		
		boolean synPredMatched1087 = false;
		if (((LA(1)==IDENT) && (LA(2)==COLON) && (_tokenSet_133.member(LA(3))))) {
			int _m1087 = mark();
			synPredMatched1087 = true;
			inputState.guessing++;
			try {
				{
				match(IDENT);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched1087 = false;
			}
			rewind(_m1087);
			inputState.guessing--;
		}
		if ( synPredMatched1087 ) {
			id = LT(1);
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(IDENT);
			if ( inputState.guessing==0 ) {
				id_AST.setType(STRING_LITERAL);
			}
			argumentLabel_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_134.member(LA(1))) && (LA(2)==COLON) && (_tokenSet_133.member(LA(3)))) {
			keywordPropertyNames();
			kw_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				kw_AST.setType(STRING_LITERAL);
			}
			argumentLabel_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_120.member(LA(1))) && (_tokenSet_132.member(LA(2))) && (_tokenSet_110.member(LA(3)))) {
			primaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			argumentLabel_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = argumentLabel_AST;
	}
	
/** Fast lookahead across balanced brackets of all sorts. */
	public final void balancedBrackets() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST balancedBrackets_AST = null;
		
		switch ( LA(1)) {
		case LPAREN:
		{
			AST tmp340_AST = null;
			tmp340_AST = astFactory.create(LT(1));
			match(LPAREN);
			balancedTokens();
			AST tmp341_AST = null;
			tmp341_AST = astFactory.create(LT(1));
			match(RPAREN);
			break;
		}
		case LBRACK:
		{
			AST tmp342_AST = null;
			tmp342_AST = astFactory.create(LT(1));
			match(LBRACK);
			balancedTokens();
			AST tmp343_AST = null;
			tmp343_AST = astFactory.create(LT(1));
			match(RBRACK);
			break;
		}
		case LCURLY:
		{
			AST tmp344_AST = null;
			tmp344_AST = astFactory.create(LT(1));
			match(LCURLY);
			balancedTokens();
			AST tmp345_AST = null;
			tmp345_AST = astFactory.create(LT(1));
			match(RCURLY);
			break;
		}
		case STRING_CTOR_START:
		{
			AST tmp346_AST = null;
			tmp346_AST = astFactory.create(LT(1));
			match(STRING_CTOR_START);
			balancedTokens();
			AST tmp347_AST = null;
			tmp347_AST = astFactory.create(LT(1));
			match(STRING_CTOR_END);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = balancedBrackets_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"BLOCK",
		"MODIFIERS",
		"OBJBLOCK",
		"SLIST",
		"METHOD_DEF",
		"VARIABLE_DEF",
		"INSTANCE_INIT",
		"STATIC_INIT",
		"TYPE",
		"CLASS_DEF",
		"INTERFACE_DEF",
		"PACKAGE_DEF",
		"ARRAY_DECLARATOR",
		"EXTENDS_CLAUSE",
		"IMPLEMENTS_CLAUSE",
		"PARAMETERS",
		"PARAMETER_DEF",
		"LABELED_STAT",
		"TYPECAST",
		"INDEX_OP",
		"POST_INC",
		"POST_DEC",
		"METHOD_CALL",
		"EXPR",
		"IMPORT",
		"UNARY_MINUS",
		"UNARY_PLUS",
		"CASE_GROUP",
		"ELIST",
		"FOR_INIT",
		"FOR_CONDITION",
		"FOR_ITERATOR",
		"EMPTY_STAT",
		"\"final\"",
		"\"abstract\"",
		"\"goto\"",
		"\"const\"",
		"\"do\"",
		"\"strictfp\"",
		"SUPER_CTOR_CALL",
		"CTOR_CALL",
		"CTOR_IDENT",
		"VARIABLE_PARAMETER_DEF",
		"STRING_CONSTRUCTOR",
		"STRING_CTOR_MIDDLE",
		"CLOSED_BLOCK",
		"IMPLICIT_PARAMETERS",
		"\"def\"",
		"SELECT_SLOT",
		"REFLECT_MEMBER",
		"DYNAMIC_MEMBER",
		"LABELED_ARG",
		"SPREAD_ARG",
		"OPTIONAL_ARG",
		"SCOPE_ESCAPE",
		"LIST_CONSTRUCTOR",
		"MAP_CONSTRUCTOR",
		"FOR_IN_ITERABLE",
		"RANGE_EXCLUSIVE",
		"STATIC_IMPORT",
		"ENUM_DEF",
		"ENUM_CONSTANT_DEF",
		"FOR_EACH_CLAUSE",
		"ANNOTATION_DEF",
		"ANNOTATIONS",
		"ANNOTATION",
		"ANNOTATION_MEMBER_VALUE_PAIR",
		"ANNOTATION_FIELD_DEF",
		"ANNOTATION_ARRAY_INIT",
		"TYPE_ARGUMENTS",
		"TYPE_ARGUMENT",
		"TYPE_PARAMETERS",
		"TYPE_PARAMETER",
		"WILDCARD_TYPE",
		"TYPE_UPPER_BOUNDS",
		"TYPE_LOWER_BOUNDS",
		"\"package\"",
		"\"import\"",
		"\"static\"",
		"AT",
		"LBRACK",
		"RBRACK",
		"IDENT",
		"LPAREN",
		"RPAREN",
		"\"class\"",
		"\"interface\"",
		"\"enum\"",
		"DOT",
		"QUESTION",
		"\"extends\"",
		"\"super\"",
		"LT",
		"COMMA",
		"GT",
		"SR",
		"BSR",
		"\"void\"",
		"\"boolean\"",
		"\"byte\"",
		"\"char\"",
		"\"short\"",
		"\"int\"",
		"\"float\"",
		"\"long\"",
		"\"double\"",
		"\"any\"",
		"STAR",
		"\"as\"",
		"\"private\"",
		"\"public\"",
		"\"protected\"",
		"\"transient\"",
		"\"native\"",
		"\"threadsafe\"",
		"\"synchronized\"",
		"\"volatile\"",
		"ASSIGN",
		"LCURLY",
		"RCURLY",
		"BAND",
		"SEMI",
		"\"default\"",
		"\"implements\"",
		"\"this\"",
		"STRING_LITERAL",
		"\"throws\"",
		"TRIPLE_DOT",
		"BOR",
		"LOR",
		"COLON",
		"\"if\"",
		"\"else\"",
		"\"while\"",
		"\"with\"",
		"\"switch\"",
		"\"for\"",
		"\"in\"",
		"\"return\"",
		"\"break\"",
		"\"continue\"",
		"\"throw\"",
		"\"assert\"",
		"INC",
		"DEC",
		"\"case\"",
		"\"try\"",
		"\"finally\"",
		"\"catch\"",
		"PLUS_ASSIGN",
		"MINUS_ASSIGN",
		"STAR_ASSIGN",
		"DIV_ASSIGN",
		"MOD_ASSIGN",
		"SR_ASSIGN",
		"BSR_ASSIGN",
		"SL_ASSIGN",
		"BAND_ASSIGN",
		"BXOR_ASSIGN",
		"BOR_ASSIGN",
		"STAR_STAR_ASSIGN",
		"STAR_DOT",
		"QUESTION_DOT",
		"LBRACE",
		"LAND",
		"BXOR",
		"REGEX_FIND",
		"REGEX_MATCH",
		"NOT_EQUAL",
		"EQUAL",
		"COMPARE_TO",
		"LE",
		"GE",
		"\"instanceof\"",
		"SL",
		"RANGE_INCLUSIVE",
		"PLUS",
		"MINUS",
		"DIV",
		"MOD",
		"STAR_STAR",
		"DOLLAR",
		"BNOT",
		"LNOT",
		"\"true\"",
		"\"false\"",
		"\"null\"",
		"STRING_CTOR_START",
		"STRING_CTOR_END",
		"\"new\"",
		"NUM_INT",
		"NUM_FLOAT",
		"NUM_LONG",
		"NUM_DOUBLE",
		"NUM_BIG_INT",
		"NUM_BIG_DECIMAL",
		"NLS",
		"WS",
		"ONE_NL",
		"SL_COMMENT",
		"ML_COMMENT",
		"STRING_CH",
		"ESC",
		"HEX_DIGIT",
		"VOCAB",
		"LETTER",
		"DIGIT",
		"EXPONENT",
		"FLOAT_SUFFIX",
		"BIG_SUFFIX"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=2305843009491107840L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=2737906963221184512L;
		data[2]=-274719577263317373L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=4611686017135411200L;
		data[2]=-549783077125L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=-4611686018628845568L;
		data[2]=-549783077125L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=-1297318288189095936L;
		data[2]=-130603289648562253L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[16];
		data[0]=-14L;
		for (int i = 1; i<=2; i++) { data[i]=-1L; }
		data[3]=4194303L;
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=7926053734153322496L;
		data[2]=-274719577261219965L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=9223372035780902912L;
		data[2]=-549755813893L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=-65536L;
		data[2]=-549755813889L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[8];
		data[1]=2305843009213693952L;
		for (int i = 2; i<=3; i++) { data[i]=256L; }
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=2737906963221184512L;
		data[2]=-274719577263317117L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=9223372035562799104L;
		data[2]=-549780979717L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=7493989779944505344L;
		data[2]=2097408L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=9223372035780902912L;
		data[2]=-144115737831669765L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3314367715524608000L;
		data[2]=-274719577263317373L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 2256610177056768L, 143692838176817152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=143692842741268480L;
		data[2]=2L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=7781797819898920960L;
		data[2]=2097410L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = new long[8];
		data[1]=140602209468416L;
		data[2]=-1152921504604766205L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=8070028189069213696L;
		data[2]=-274719027543162621L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 4810363371520L, 143552238156775424L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143552238160969728L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=-8791589416896561152L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=432063954007490560L;
		data[2]=-274719577263317373L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3314367716061478912L;
		data[2]=-274719577263317325L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=9223372035579576320L;
		data[2]=-549780979717L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=-131072L;
		data[2]=-549755813893L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = new long[8];
		data[1]=288370978362228736L;
		data[2]=-274719577268027389L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=9079256847486943232L;
		data[2]=-687186378757L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=431923216485449728L;
		data[2]=-274719577267519485L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=3314367715524608000L;
		data[2]=-274719577263317373L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=7494271255193845760L;
		data[2]=2097408L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 4810363371520L, 143552238122696704L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=143692842983489536L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=8070028191173705728L;
		data[2]=2097408L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=9223372035764125696L;
		data[2]=-549780979717L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=7925912994507915264L;
		data[2]=2097408L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 0L, 288230376161148928L, -9223372036854775808L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = new long[16];
		data[0]=-16L;
		data[1]=-864691128483446785L;
		data[2]=9223372036854775807L;
		data[3]=4194302L;
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=-281479271940096L;
		data[2]=139775384224123L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=-281474976972800L;
		data[2]=139775384224123L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 0L, 140600590467072L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=-65536L;
		data[2]=-144115737831669761L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=432063963136262144L;
		data[2]=-274719577267511293L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=-131072L;
		data[2]=-549780979717L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=-281474976972800L;
		data[2]=-274579801883287173L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = new long[8];
		data[1]=288370978362753024L;
		data[2]=-274719577268027389L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=4467570829076332544L;
		data[2]=-687188476165L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = new long[8];
		data[0]=2538085153767424L;
		data[1]=4611686017353580544L;
		data[2]=-549783077125L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = new long[8];
		data[1]=864831730666176512L;
		data[2]=-274719577268027389L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 0L, 3170534146271346688L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 0L, -7782079421556457472L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { 0L, -8935000930726707200L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=-5909145055136055296L;
		data[2]=2L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=-1153202979600465920L;
		data[2]=-274719577261219965L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 4810363371520L, 143552238127415296L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143552238135803904L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=143833576495644672L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3458342177858125824L;
		data[2]=2L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { 4810363371520L, 143552238358102016L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143552238362296320L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=-8791589416695234560L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { 4810363371520L, 143552238123220992L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143552238639120384L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 0L, 235405312L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = new long[8];
		data[1]=-8935141655330160640L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3170111801471533056L;
		data[2]=2L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { 0L, 2449958205887873024L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3314367716044570624L;
		data[2]=-274719577267519485L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=432063954544230400L;
		data[2]=-274719577267511293L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=4611686017152188416L;
		data[2]=-549783077125L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { 2251937252638720L, 281338096123904L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = { 2251937252638720L, 281338079346688L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	private static final long[] mk_tokenSet_73() {
		long[] data = new long[8];
		data[0]=137438953472L;
		data[1]=144396539326365696L;
		data[2]=16L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_73 = new BitSet(mk_tokenSet_73());
	private static final long[] mk_tokenSet_74() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=8070168935938260992L;
		data[2]=-274719577261219945L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_74 = new BitSet(mk_tokenSet_74());
	private static final long[] mk_tokenSet_75() {
		long[] data = new long[8];
		data[0]=2251937252638720L;
		data[1]=281338079346688L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_75 = new BitSet(mk_tokenSet_75());
	private static final long[] mk_tokenSet_76() {
		long[] data = new long[8];
		data[0]=2251937252638720L;
		data[1]=144396539326365696L;
		data[2]=16L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_76 = new BitSet(mk_tokenSet_76());
	private static final long[] mk_tokenSet_77() {
		long[] data = { 137438953472L, 140600054120448L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_77 = new BitSet(mk_tokenSet_77());
	private static final long[] mk_tokenSet_78() {
		long[] data = { 0L, 140604618571776L, 8L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_78 = new BitSet(mk_tokenSet_78());
	private static final long[] mk_tokenSet_79() {
		long[] data = new long[8];
		data[0]=137438953472L;
		data[1]=140605165928448L;
		data[2]=8L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_79 = new BitSet(mk_tokenSet_79());
	private static final long[] mk_tokenSet_80() {
		long[] data = new long[8];
		data[1]=16777216L;
		data[2]=16L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_80 = new BitSet(mk_tokenSet_80());
	private static final long[] mk_tokenSet_81() {
		long[] data = { 0L, 6442450944L, 1L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_81 = new BitSet(mk_tokenSet_81());
	private static final long[] mk_tokenSet_82() {
		long[] data = { 0L, 140600598855680L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_82 = new BitSet(mk_tokenSet_82());
	private static final long[] mk_tokenSet_83() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=432064089047171072L;
		data[2]=-274719577267511293L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_83 = new BitSet(mk_tokenSet_83());
	private static final long[] mk_tokenSet_84() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=4611686017336737792L;
		data[2]=-549783077125L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_84 = new BitSet(mk_tokenSet_84());
	private static final long[] mk_tokenSet_85() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=-131072L;
		data[2]=-549780979717L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_85 = new BitSet(mk_tokenSet_85());
	private static final long[] mk_tokenSet_86() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=143692842741268480L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_86 = new BitSet(mk_tokenSet_86());
	private static final long[] mk_tokenSet_87() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=8070028196302290944L;
		data[2]=2097488L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_87 = new BitSet(mk_tokenSet_87());
	private static final long[] mk_tokenSet_88() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=9223372035780902912L;
		data[2]=-549780979717L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_88 = new BitSet(mk_tokenSet_88());
	private static final long[] mk_tokenSet_89() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=-131072L;
		data[2]=-549755813889L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_89 = new BitSet(mk_tokenSet_89());
	private static final long[] mk_tokenSet_90() {
		long[] data = { 0L, 140600053596160L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_90 = new BitSet(mk_tokenSet_90());
	private static final long[] mk_tokenSet_91() {
		long[] data = { 0L, 140742593871872L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_91 = new BitSet(mk_tokenSet_91());
	private static final long[] mk_tokenSet_92() {
		long[] data = new long[8];
		data[1]=144255797275131904L;
		data[2]=16L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_92 = new BitSet(mk_tokenSet_92());
	private static final long[] mk_tokenSet_93() {
		long[] data = { 0L, 140738029420544L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_93 = new BitSet(mk_tokenSet_93());
	private static final long[] mk_tokenSet_94() {
		long[] data = new long[8];
		data[1]=144115196686761984L;
		data[2]=16L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_94 = new BitSet(mk_tokenSet_94());
	private static final long[] mk_tokenSet_95() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=8070168931372761088L;
		data[2]=-274719577261219945L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_95 = new BitSet(mk_tokenSet_95());
	private static final long[] mk_tokenSet_96() {
		long[] data = new long[8];
		data[0]=137438953472L;
		data[1]=281338356170752L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_96 = new BitSet(mk_tokenSet_96());
	private static final long[] mk_tokenSet_97() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3314367715524608000L;
		data[2]=-274719577263317357L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_97 = new BitSet(mk_tokenSet_97());
	private static final long[] mk_tokenSet_98() {
		long[] data = new long[8];
		data[0]=2251937252638720L;
		data[1]=281338087735296L;
		data[2]=16L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_98 = new BitSet(mk_tokenSet_98());
	private static final long[] mk_tokenSet_99() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3458482917307449344L;
		data[2]=-274719577263317349L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_99 = new BitSet(mk_tokenSet_99());
	private static final long[] mk_tokenSet_100() {
		long[] data = { 0L, 140738029420544L, 16L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_100 = new BitSet(mk_tokenSet_100());
	private static final long[] mk_tokenSet_101() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3314367724114542592L;
		data[2]=-274719577263317357L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_101 = new BitSet(mk_tokenSet_101());
	private static final long[] mk_tokenSet_102() {
		long[] data = new long[16];
		data[0]=-16L;
		data[1]=-3170534137697140737L;
		data[2]=9223372036854775807L;
		data[3]=4194046L;
		return data;
	}
	public static final BitSet _tokenSet_102 = new BitSet(mk_tokenSet_102());
	private static final long[] mk_tokenSet_103() {
		long[] data = new long[8];
		data[1]=140738037809152L;
		data[2]=48L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_103 = new BitSet(mk_tokenSet_103());
	private static final long[] mk_tokenSet_104() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3314367724668190720L;
		data[2]=-274719577263317325L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_104 = new BitSet(mk_tokenSet_104());
	private static final long[] mk_tokenSet_105() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=431923216485449728L;
		data[2]=-274719577267519485L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_105 = new BitSet(mk_tokenSet_105());
	private static final long[] mk_tokenSet_106() {
		long[] data = { 0L, 144115188075855872L, 137406971904L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_106 = new BitSet(mk_tokenSet_106());
	private static final long[] mk_tokenSet_107() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=7782360758306734080L;
		data[2]=-274719577265929981L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_107 = new BitSet(mk_tokenSet_107());
	private static final long[] mk_tokenSet_108() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=2449535849546383360L;
		data[2]=-1152921504605274109L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_108 = new BitSet(mk_tokenSet_108());
	private static final long[] mk_tokenSet_109() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=2881881426928336896L;
		data[2]=-274719027545260029L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_109 = new BitSet(mk_tokenSet_109());
	private static final long[] mk_tokenSet_110() {
		long[] data = new long[8];
		data[0]=2538085153767424L;
		data[1]=4611686017152188416L;
		data[2]=-549783077125L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_110 = new BitSet(mk_tokenSet_110());
	private static final long[] mk_tokenSet_111() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=143692842741268480L;
		data[2]=8192L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_111 = new BitSet(mk_tokenSet_111());
	private static final long[] mk_tokenSet_112() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=431923221587820544L;
		data[2]=-274719577268019197L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_112 = new BitSet(mk_tokenSet_112());
	private static final long[] mk_tokenSet_113() {
		long[] data = { 2251937252638720L, 281338087735296L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_113 = new BitSet(mk_tokenSet_113());
	private static final long[] mk_tokenSet_114() {
		long[] data = new long[8];
		data[1]=288230376151711744L;
		data[2]=8388608L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_114 = new BitSet(mk_tokenSet_114());
	private static final long[] mk_tokenSet_115() {
		long[] data = { 0L, 103079215104L, 3377699720527880L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_115 = new BitSet(mk_tokenSet_115());
	private static final long[] mk_tokenSet_116() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=7925913003116724224L;
		data[2]=2097488L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_116 = new BitSet(mk_tokenSet_116());
	private static final long[] mk_tokenSet_117() {
		long[] data = new long[8];
		data[0]=2538085153767426L;
		data[1]=-131072L;
		data[2]=-144115737831669761L;
		data[3]=511L;
		return data;
	}
	public static final BitSet _tokenSet_117 = new BitSet(mk_tokenSet_117());
	private static final long[] mk_tokenSet_118() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=7925913005273645056L;
		data[2]=-274719577265929901L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_118 = new BitSet(mk_tokenSet_118());
	private static final long[] mk_tokenSet_119() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=2593651049184886784L;
		data[3]=256L;
		return data;
	}
	public static final BitSet _tokenSet_119 = new BitSet(mk_tokenSet_119());
	private static final long[] mk_tokenSet_120() {
		long[] data = new long[8];
		data[1]=140602209468416L;
		data[2]=-1152921504606846973L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_120 = new BitSet(mk_tokenSet_120());
	private static final long[] mk_tokenSet_121() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=2881881426945114112L;
		data[2]=-274719027545260029L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_121 = new BitSet(mk_tokenSet_121());
	private static final long[] mk_tokenSet_122() {
		long[] data = { 0L, 288230376429584384L, 412316860416L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_122 = new BitSet(mk_tokenSet_122());
	private static final long[] mk_tokenSet_123() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=3314367724670287872L;
		data[2]=-274718477751681357L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_123 = new BitSet(mk_tokenSet_123());
	private static final long[] mk_tokenSet_124() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=9223372035780771840L;
		data[2]=-144115737861037701L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_124 = new BitSet(mk_tokenSet_124());
	private static final long[] mk_tokenSet_125() {
		long[] data = new long[8];
		data[1]=288370978362228736L;
		data[2]=-274719577268027389L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_125 = new BitSet(mk_tokenSet_125());
	private static final long[] mk_tokenSet_126() {
		long[] data = { 0L, 140737488355328L, 54043195528445952L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_126 = new BitSet(mk_tokenSet_126());
	private static final long[] mk_tokenSet_127() {
		long[] data = { 0L, 4581228544L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_127 = new BitSet(mk_tokenSet_127());
	private static final long[] mk_tokenSet_128() {
		long[] data = new long[8];
		data[1]=288370978362228736L;
		data[2]=-1152921504606846973L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_128 = new BitSet(mk_tokenSet_128());
	private static final long[] mk_tokenSet_129() {
		long[] data = new long[8];
		data[0]=2256610177056770L;
		data[1]=9223372035780902912L;
		data[2]=-144115737856835589L;
		data[3]=510L;
		return data;
	}
	public static final BitSet _tokenSet_129 = new BitSet(mk_tokenSet_129());
	private static final long[] mk_tokenSet_130() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=431923216487546880L;
		data[2]=-274719577267519485L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_130 = new BitSet(mk_tokenSet_130());
	private static final long[] mk_tokenSet_131() {
		long[] data = new long[8];
		data[0]=2251799813685248L;
		data[1]=140602243022848L;
		data[2]=-1152921504606838781L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_131 = new BitSet(mk_tokenSet_131());
	private static final long[] mk_tokenSet_132() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=431923220780417024L;
		data[2]=-274719577267519421L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_132 = new BitSet(mk_tokenSet_132());
	private static final long[] mk_tokenSet_133() {
		long[] data = new long[8];
		data[0]=2256610177056768L;
		data[1]=432063954510675968L;
		data[2]=-274719577267519485L;
		data[3]=254L;
		return data;
	}
	public static final BitSet _tokenSet_133 = new BitSet(mk_tokenSet_133());
	private static final long[] mk_tokenSet_134() {
		long[] data = { 2251799813685248L, 140600082956288L, 8192L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_134 = new BitSet(mk_tokenSet_134());
	
	}
