// $ANTLR 2.7.2: "groovy.g" -> "GroovyLexer.java"$

package org.codehaus.groovy.antlr.parser;
import org.codehaus.groovy.antlr.*;
import java.util.*;
import java.io.InputStream;
import java.io.Reader;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class GroovyLexer extends antlr.CharScanner implements GroovyTokenTypes, TokenStream
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

    protected GroovyRecognizer parser;  // little-used link; TODO: get rid of
    private void require(boolean z, String problem, String solution) throws SemanticException {
        // TODO: Direct to a common error handler, rather than through the parser.
        if (!z)  parser.requireFailed(problem, solution);
    }
public GroovyLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public GroovyLexer(Reader in) {
	this(new CharBuffer(in));
}
public GroovyLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public GroovyLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("byte", this), new Integer(103));
	literals.put(new ANTLRHashString("public", this), new Integer(114));
	literals.put(new ANTLRHashString("case", this), new Integer(152));
	literals.put(new ANTLRHashString("short", this), new Integer(105));
	literals.put(new ANTLRHashString("break", this), new Integer(146));
	literals.put(new ANTLRHashString("while", this), new Integer(140));
	literals.put(new ANTLRHashString("new", this), new Integer(192));
	literals.put(new ANTLRHashString("instanceof", this), new Integer(181));
	literals.put(new ANTLRHashString("implements", this), new Integer(129));
	literals.put(new ANTLRHashString("synchronized", this), new Integer(119));
	literals.put(new ANTLRHashString("const", this), new Integer(40));
	literals.put(new ANTLRHashString("float", this), new Integer(107));
	literals.put(new ANTLRHashString("package", this), new Integer(80));
	literals.put(new ANTLRHashString("return", this), new Integer(145));
	literals.put(new ANTLRHashString("throw", this), new Integer(148));
	literals.put(new ANTLRHashString("null", this), new Integer(195));
	literals.put(new ANTLRHashString("def", this), new Integer(83));
	literals.put(new ANTLRHashString("threadsafe", this), new Integer(118));
	literals.put(new ANTLRHashString("protected", this), new Integer(115));
	literals.put(new ANTLRHashString("class", this), new Integer(89));
	literals.put(new ANTLRHashString("throws", this), new Integer(132));
	literals.put(new ANTLRHashString("do", this), new Integer(41));
	literals.put(new ANTLRHashString("strictfp", this), new Integer(42));
	literals.put(new ANTLRHashString("super", this), new Integer(95));
	literals.put(new ANTLRHashString("with", this), new Integer(141));
	literals.put(new ANTLRHashString("transient", this), new Integer(116));
	literals.put(new ANTLRHashString("native", this), new Integer(117));
	literals.put(new ANTLRHashString("interface", this), new Integer(90));
	literals.put(new ANTLRHashString("final", this), new Integer(37));
	literals.put(new ANTLRHashString("any", this), new Integer(110));
	literals.put(new ANTLRHashString("if", this), new Integer(138));
	literals.put(new ANTLRHashString("double", this), new Integer(109));
	literals.put(new ANTLRHashString("volatile", this), new Integer(120));
	literals.put(new ANTLRHashString("as", this), new Integer(112));
	literals.put(new ANTLRHashString("assert", this), new Integer(149));
	literals.put(new ANTLRHashString("catch", this), new Integer(155));
	literals.put(new ANTLRHashString("try", this), new Integer(153));
	literals.put(new ANTLRHashString("goto", this), new Integer(39));
	literals.put(new ANTLRHashString("enum", this), new Integer(91));
	literals.put(new ANTLRHashString("int", this), new Integer(106));
	literals.put(new ANTLRHashString("for", this), new Integer(143));
	literals.put(new ANTLRHashString("extends", this), new Integer(94));
	literals.put(new ANTLRHashString("boolean", this), new Integer(102));
	literals.put(new ANTLRHashString("char", this), new Integer(104));
	literals.put(new ANTLRHashString("private", this), new Integer(113));
	literals.put(new ANTLRHashString("default", this), new Integer(128));
	literals.put(new ANTLRHashString("false", this), new Integer(194));
	literals.put(new ANTLRHashString("this", this), new Integer(130));
	literals.put(new ANTLRHashString("static", this), new Integer(82));
	literals.put(new ANTLRHashString("abstract", this), new Integer(38));
	literals.put(new ANTLRHashString("continue", this), new Integer(147));
	literals.put(new ANTLRHashString("finally", this), new Integer(154));
	literals.put(new ANTLRHashString("else", this), new Integer(139));
	literals.put(new ANTLRHashString("import", this), new Integer(81));
	literals.put(new ANTLRHashString("in", this), new Integer(144));
	literals.put(new ANTLRHashString("void", this), new Integer(101));
	literals.put(new ANTLRHashString("switch", this), new Integer(142));
	literals.put(new ANTLRHashString("true", this), new Integer(193));
	literals.put(new ANTLRHashString("long", this), new Integer(108));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '(':
				{
					mLPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mLBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mRBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case '{':
				{
					mLCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case '}':
				{
					mRCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case ':':
				{
					mCOLON(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '~':
				{
					mBNOT(true);
					theRetToken=_returnToken;
					break;
				}
				case ';':
				{
					mSEMI(true);
					theRetToken=_returnToken;
					break;
				}
				case '$':
				{
					mDOLLAR(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\u000c':  case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				case '\n':  case '\r':
				{
					mNLS(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':  case '\'':
				{
					mSTRING_LITERAL(true);
					theRetToken=_returnToken;
					break;
				}
				case 'A':  case 'B':  case 'C':  case 'D':
				case 'E':  case 'F':  case 'G':  case 'H':
				case 'I':  case 'J':  case 'K':  case 'L':
				case 'M':  case 'N':  case 'O':  case 'P':
				case 'Q':  case 'R':  case 'S':  case 'T':
				case 'U':  case 'V':  case 'W':  case 'X':
				case 'Y':  case 'Z':  case '_':  case 'a':
				case 'b':  case 'c':  case 'd':  case 'e':
				case 'f':  case 'g':  case 'h':  case 'i':
				case 'j':  case 'k':  case 'l':  case 'm':
				case 'n':  case 'o':  case 'p':  case 'q':
				case 'r':  case 's':  case 't':  case 'u':
				case 'v':  case 'w':  case 'x':  case 'y':
				case 'z':
				{
					mIDENT(true);
					theRetToken=_returnToken;
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					mNUM_INT(true);
					theRetToken=_returnToken;
					break;
				}
				case '@':
				{
					mAT(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (LA(4)=='=')) {
						mBSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='=') && (LA(3)=='>')) {
						mCOMPARE_TO(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='=')) {
						mSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (true)) {
						mBSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (LA(3)=='=')) {
						mSL_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (LA(2)=='.') && (LA(3)=='.')) {
						mTRIPLE_DOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='=') && (LA(3)=='~')) {
						mREGEX_MATCH(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='*') && (LA(3)=='=')) {
						mSTAR_STAR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='=') && (true)) {
						mEQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (LA(2)=='=')) {
						mNOT_EQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='=')) {
						mDIV_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='=')) {
						mPLUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='+')) {
						mINC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='=')) {
						mMINUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='-')) {
						mDEC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='=')) {
						mSTAR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (LA(2)=='=')) {
						mMOD_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (true)) {
						mSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='=')) {
						mGE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (true)) {
						mSL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='=') && (true)) {
						mLE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (LA(2)=='=')) {
						mBXOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='=')) {
						mBOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='|')) {
						mLOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='=')) {
						mBAND_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='&')) {
						mLAND(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (LA(2)=='.') && (true)) {
						mRANGE_INCLUSIVE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='.')) {
						mSTAR_DOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='?') && (LA(2)=='.')) {
						mQUESTION_DOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='~')) {
						mREGEX_FIND(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='*') && (true)) {
						mSTAR_STAR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='>')) {
						mCLOSURE_OP(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='/')) {
						mSL_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='*')) {
						mML_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='?') && (true)) {
						mQUESTION(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (true)) {
						mDOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (true)) {
						mASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (true)) {
						mLNOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (true)) {
						mDIV(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (true)) {
						mPLUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (true)) {
						mMINUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (true)) {
						mSTAR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (true)) {
						mMOD(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (true)) {
						mGT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (true)) {
						mLT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (true)) {
						mBXOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (true)) {
						mBOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (true)) {
						mBAND(true);
						theRetToken=_returnToken;
					}
					else if (((LA(1)=='#'))&&(getLine() == 1 && getColumn() == 1)) {
						mSH_COMMENT(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mQUESTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION;
		int _saveIndex;
		
		match('?');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if ( inputState.guessing==0 ) {
			++parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if ( inputState.guessing==0 ) {
			--parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LBRACK;
		int _saveIndex;
		
		match('[');
		if ( inputState.guessing==0 ) {
			++parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RBRACK;
		int _saveIndex;
		
		match(']');
		if ( inputState.guessing==0 ) {
			--parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LCURLY;
		int _saveIndex;
		
		match('{');
		if ( inputState.guessing==0 ) {
			pushParenLevel();
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RCURLY;
		int _saveIndex;
		
		match('}');
		if ( inputState.guessing==0 ) {
			popParenLevel(); if(stringCtorState!=0) restartStringCtor(true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(':');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOT;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ASSIGN;
		int _saveIndex;
		
		match('=');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMPARE_TO(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMPARE_TO;
		int _saveIndex;
		
		match("<=>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQUAL;
		int _saveIndex;
		
		match("==");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LNOT;
		int _saveIndex;
		
		match('!');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BNOT;
		int _saveIndex;
		
		match('~');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_EQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NOT_EQUAL;
		int _saveIndex;
		
		match("!=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIV(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV;
		int _saveIndex;
		
		match('/');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIV_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV_ASSIGN;
		int _saveIndex;
		
		match("/=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS_ASSIGN;
		int _saveIndex;
		
		match("+=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INC;
		int _saveIndex;
		
		match("++");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		match('-');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS_ASSIGN;
		int _saveIndex;
		
		match("-=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDEC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DEC;
		int _saveIndex;
		
		match("--");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR;
		int _saveIndex;
		
		match('*');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_ASSIGN;
		int _saveIndex;
		
		match("*=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD;
		int _saveIndex;
		
		match('%');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD_ASSIGN;
		int _saveIndex;
		
		match("%=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR;
		int _saveIndex;
		
		match(">>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR_ASSIGN;
		int _saveIndex;
		
		match(">>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR;
		int _saveIndex;
		
		match(">>>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR_ASSIGN;
		int _saveIndex;
		
		match(">>>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GE;
		int _saveIndex;
		
		match(">=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GT;
		int _saveIndex;
		
		match(">");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL;
		int _saveIndex;
		
		match("<<");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_ASSIGN;
		int _saveIndex;
		
		match("<<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LE;
		int _saveIndex;
		
		match("<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LT;
		int _saveIndex;
		
		match('<');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR;
		int _saveIndex;
		
		match('^');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR_ASSIGN;
		int _saveIndex;
		
		match("^=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR;
		int _saveIndex;
		
		match('|');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR_ASSIGN;
		int _saveIndex;
		
		match("|=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LOR;
		int _saveIndex;
		
		match("||");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND;
		int _saveIndex;
		
		match('&');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND_ASSIGN;
		int _saveIndex;
		
		match("&=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LAND;
		int _saveIndex;
		
		match("&&");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOLLAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOLLAR;
		int _saveIndex;
		
		match('$');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRANGE_INCLUSIVE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RANGE_INCLUSIVE;
		int _saveIndex;
		
		match("..");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mTRIPLE_DOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = TRIPLE_DOT;
		int _saveIndex;
		
		match("...");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_DOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_DOT;
		int _saveIndex;
		
		match("*.");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mQUESTION_DOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION_DOT;
		int _saveIndex;
		
		match("?.");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mREGEX_FIND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = REGEX_FIND;
		int _saveIndex;
		
		match("=~");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mREGEX_MATCH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = REGEX_MATCH;
		int _saveIndex;
		
		match("==~");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_STAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_STAR;
		int _saveIndex;
		
		match("**");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_STAR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_STAR_ASSIGN;
		int _saveIndex;
		
		match("**=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCLOSURE_OP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CLOSURE_OP;
		int _saveIndex;
		
		match("->");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		int _cnt551=0;
		_loop551:
		do {
			if ((LA(1)==' ') && (true) && (true) && (true)) {
				match(' ');
			}
			else if ((LA(1)=='\t') && (true) && (true) && (true)) {
				match('\t');
			}
			else if ((LA(1)=='\u000c') && (true) && (true) && (true)) {
				match('\f');
			}
			else {
				if ( _cnt551>=1 ) { break _loop551; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt551++;
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			if (!whitespaceIncluded)  _ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mONE_NL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ONE_NL;
		int _saveIndex;
		
		{
		if ((LA(1)=='\r') && (LA(2)=='\n') && (true) && (true)) {
			_saveIndex=text.length();
			match("\r\n");
			text.setLength(_saveIndex);
		}
		else if ((LA(1)=='\r') && (true) && (true) && (true)) {
			_saveIndex=text.length();
			match('\r');
			text.setLength(_saveIndex);
		}
		else if ((LA(1)=='\n')) {
			_saveIndex=text.length();
			match('\n');
			text.setLength(_saveIndex);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( inputState.guessing==0 ) {
			
			// update current line number for error reporting
			newlineCheck();
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNLS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NLS;
		int _saveIndex;
		
		mONE_NL(false);
		{
		if (((LA(1)=='\t'||LA(1)=='\n'||LA(1)=='\u000c'||LA(1)=='\r'||LA(1)==' '||LA(1)=='/'))&&(!whitespaceIncluded)) {
			{
			int _cnt557=0;
			_loop557:
			do {
				switch ( LA(1)) {
				case '\n':  case '\r':
				{
					mONE_NL(false);
					break;
				}
				case '\t':  case '\u000c':  case ' ':
				{
					mWS(false);
					break;
				}
				default:
					if ((LA(1)=='/') && (LA(2)=='/')) {
						mSL_COMMENT(false);
					}
					else if ((LA(1)=='/') && (LA(2)=='*')) {
						mML_COMMENT(false);
					}
				else {
					if ( _cnt557>=1 ) { break _loop557; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				_cnt557++;
			} while (true);
			}
		}
		else {
		}
		
		}
		if ( inputState.guessing==0 ) {
			if (whitespaceIncluded) {
			// keep the token as-is
			} else if (parenLevel != 0) {
			// when directly inside parens, all newlines are ignored here
			_ttype = Token.SKIP;
			} else {
			// inside {...}, newlines must be explicitly matched as 'nls!'
			text.setLength(_begin); text.append("<newline>");
			}
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_COMMENT;
		int _saveIndex;
		
		match("//");
		{
		_loop561:
		do {
			if ((_tokenSet_0.member(LA(1))) && (true) && (true) && (true)) {
				{
				match(_tokenSet_0);
				}
			}
			else {
				break _loop561;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			if (!whitespaceIncluded)  _ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mML_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ML_COMMENT;
		int _saveIndex;
		
		match("/*");
		{
		_loop568:
		do {
			if ((LA(1)=='\r') && (LA(2)=='\n') && ((LA(3) >= '\u0003' && LA(3) <= '\uffff')) && ((LA(4) >= '\u0003' && LA(4) <= '\uffff'))) {
				match('\r');
				match('\n');
				if ( inputState.guessing==0 ) {
					newlineCheck();
				}
			}
			else {
				boolean synPredMatched566 = false;
				if (((LA(1)=='*') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && ((LA(3) >= '\u0003' && LA(3) <= '\uffff')) && (true))) {
					int _m566 = mark();
					synPredMatched566 = true;
					inputState.guessing++;
					try {
						{
						match('*');
						matchNot('/');
						}
					}
					catch (RecognitionException pe) {
						synPredMatched566 = false;
					}
					rewind(_m566);
					inputState.guessing--;
				}
				if ( synPredMatched566 ) {
					match('*');
				}
				else if ((LA(1)=='\r') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && ((LA(3) >= '\u0003' && LA(3) <= '\uffff')) && (true)) {
					match('\r');
					if ( inputState.guessing==0 ) {
						newlineCheck();
					}
				}
				else if ((LA(1)=='\n')) {
					match('\n');
					if ( inputState.guessing==0 ) {
						newlineCheck();
					}
				}
				else if ((_tokenSet_1.member(LA(1)))) {
					{
					match(_tokenSet_1);
					}
				}
				else {
					break _loop568;
				}
				}
			} while (true);
			}
			match("*/");
			if ( inputState.guessing==0 ) {
				if (!whitespaceIncluded)  _ttype = Token.SKIP;
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
		}
		
	public final void mSH_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SH_COMMENT;
		int _saveIndex;
		
		if (!(getLine() == 1 && getColumn() == 1))
		  throw new SemanticException("getLine() == 1 && getColumn() == 1");
		match("#!");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_LITERAL;
		int _saveIndex;
		int tt=0;
		
		boolean synPredMatched571 = false;
		if (((LA(1)=='\'') && (LA(2)=='\'') && (LA(3)=='\'') && ((LA(4) >= '\u0003' && LA(4) <= '\uffff')))) {
			int _m571 = mark();
			synPredMatched571 = true;
			inputState.guessing++;
			try {
				{
				match("'''");
				}
			}
			catch (RecognitionException pe) {
				synPredMatched571 = false;
			}
			rewind(_m571);
			inputState.guessing--;
		}
		if ( synPredMatched571 ) {
			_saveIndex=text.length();
			match("'''");
			text.setLength(_saveIndex);
			{
			_loop576:
			do {
				switch ( LA(1)) {
				case '\n':  case '\r':  case '\\':
				{
					mESC(false);
					break;
				}
				case '"':
				{
					match('"');
					break;
				}
				case '$':
				{
					match('$');
					break;
				}
				default:
					boolean synPredMatched575 = false;
					if (((LA(1)=='\'') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && ((LA(3) >= '\u0003' && LA(3) <= '\uffff')) && ((LA(4) >= '\u0003' && LA(4) <= '\uffff')))) {
						int _m575 = mark();
						synPredMatched575 = true;
						inputState.guessing++;
						try {
							{
							match('\'');
							{
							if ((_tokenSet_2.member(LA(1)))) {
								matchNot('\'');
							}
							else if ((LA(1)=='\'')) {
								match('\'');
								matchNot('\'');
							}
							else {
								throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
							}
							
							}
							}
						}
						catch (RecognitionException pe) {
							synPredMatched575 = false;
						}
						rewind(_m575);
						inputState.guessing--;
					}
					if ( synPredMatched575 ) {
						match('\'');
					}
					else if ((_tokenSet_3.member(LA(1)))) {
						mSTRING_CH(false);
					}
				else {
					break _loop576;
				}
				}
			} while (true);
			}
			_saveIndex=text.length();
			match("'''");
			text.setLength(_saveIndex);
		}
		else {
			boolean synPredMatched580 = false;
			if (((LA(1)=='"') && (LA(2)=='"') && (LA(3)=='"') && ((LA(4) >= '\u0003' && LA(4) <= '\uffff')))) {
				int _m580 = mark();
				synPredMatched580 = true;
				inputState.guessing++;
				try {
					{
					match("\"\"\"");
					}
				}
				catch (RecognitionException pe) {
					synPredMatched580 = false;
				}
				rewind(_m580);
				inputState.guessing--;
			}
			if ( synPredMatched580 ) {
				_saveIndex=text.length();
				match("\"\"\"");
				text.setLength(_saveIndex);
				tt=mSTRING_CTOR_END(false,true, /*tripleQuote:*/ true);
				if ( inputState.guessing==0 ) {
					_ttype = tt;
				}
			}
			else if ((LA(1)=='\'') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && (true) && (true)) {
				_saveIndex=text.length();
				match('\'');
				text.setLength(_saveIndex);
				if ( inputState.guessing==0 ) {
					++suppressNewline;
				}
				{
				_loop578:
				do {
					switch ( LA(1)) {
					case '\n':  case '\r':  case '\\':
					{
						mESC(false);
						break;
					}
					case '"':
					{
						match('"');
						break;
					}
					case '$':
					{
						match('$');
						break;
					}
					default:
						if ((_tokenSet_3.member(LA(1)))) {
							mSTRING_CH(false);
						}
					else {
						break _loop578;
					}
					}
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					--suppressNewline;
				}
				_saveIndex=text.length();
				match('\'');
				text.setLength(_saveIndex);
			}
			else if ((LA(1)=='"') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && (true) && (true)) {
				_saveIndex=text.length();
				match('"');
				text.setLength(_saveIndex);
				if ( inputState.guessing==0 ) {
					++suppressNewline;
				}
				tt=mSTRING_CTOR_END(false,true, /*tripleQuote:*/ false);
				if ( inputState.guessing==0 ) {
					_ttype = tt;
				}
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
		}
		
	protected final void mSTRING_CH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_CH;
		int _saveIndex;
		
		{
		match(_tokenSet_3);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC;
		int _saveIndex;
		
		if ((LA(1)=='\\') && (LA(2)=='"'||LA(2)=='$'||LA(2)=='\''||LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='\\'||LA(2)=='b'||LA(2)=='f'||LA(2)=='n'||LA(2)=='r'||LA(2)=='t'||LA(2)=='u')) {
			_saveIndex=text.length();
			match('\\');
			text.setLength(_saveIndex);
			{
			switch ( LA(1)) {
			case 'n':
			{
				match('n');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\n");
				}
				break;
			}
			case 'r':
			{
				match('r');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\r");
				}
				break;
			}
			case 't':
			{
				match('t');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\t");
				}
				break;
			}
			case 'b':
			{
				match('b');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\b");
				}
				break;
			}
			case 'f':
			{
				match('f');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\f");
				}
				break;
			}
			case '"':
			{
				match('"');
				break;
			}
			case '\'':
			{
				match('\'');
				break;
			}
			case '\\':
			{
				match('\\');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			case 'u':
			{
				{
				int _cnt594=0;
				_loop594:
				do {
					if ((LA(1)=='u')) {
						match('u');
					}
					else {
						if ( _cnt594>=1 ) { break _loop594; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt594++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("");
				}
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				if ( inputState.guessing==0 ) {
					char ch = (char)Integer.parseInt(new String(text.getBuffer(),_begin,text.length()-_begin),16); text.setLength(_begin); text.append(ch);
				}
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			{
				matchRange('0','3');
				{
				if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && (true) && (true)) {
					matchRange('0','7');
					{
					if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && (true) && (true)) {
						matchRange('0','7');
					}
					else if (((LA(1) >= '\u0003' && LA(1) <= '\uffff')) && (true) && (true) && (true)) {
					}
					else {
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					
					}
				}
				else if (((LA(1) >= '\u0003' && LA(1) <= '\uffff')) && (true) && (true) && (true)) {
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				if ( inputState.guessing==0 ) {
					char ch = (char)Integer.parseInt(new String(text.getBuffer(),_begin,text.length()-_begin),8); text.setLength(_begin); text.append(ch);
				}
				break;
			}
			case '4':  case '5':  case '6':  case '7':
			{
				matchRange('4','7');
				{
				if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && (true) && (true)) {
					matchRange('0','7');
				}
				else if (((LA(1) >= '\u0003' && LA(1) <= '\uffff')) && (true) && (true) && (true)) {
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				if ( inputState.guessing==0 ) {
					char ch = (char)Integer.parseInt(new String(text.getBuffer(),_begin,text.length()-_begin),8); text.setLength(_begin); text.append(ch);
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
		}
		else if ((LA(1)=='\\') && (LA(2)=='\n'||LA(2)=='\r')) {
			_saveIndex=text.length();
			match('\\');
			text.setLength(_saveIndex);
			_saveIndex=text.length();
			mONE_NL(false);
			text.setLength(_saveIndex);
		}
		else if ((LA(1)=='\n'||LA(1)=='\r')) {
			_saveIndex=text.length();
			mONE_NL(false);
			text.setLength(_saveIndex);
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append('\n');
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final int  mSTRING_CTOR_END(boolean _createToken,
		boolean fromStart, boolean tripleQuote
	) throws RecognitionException, CharStreamException, TokenStreamException {
		int tt=STRING_CTOR_END;
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_CTOR_END;
		int _saveIndex;
		
		{
		_loop586:
		do {
			switch ( LA(1)) {
			case '\n':  case '\r':  case '\\':
			{
				mESC(false);
				break;
			}
			case '\'':
			{
				match('\'');
				break;
			}
			default:
				boolean synPredMatched585 = false;
				if ((((LA(1)=='"') && ((LA(2) >= '\u0003' && LA(2) <= '\uffff')) && (true) && (true))&&(tripleQuote))) {
					int _m585 = mark();
					synPredMatched585 = true;
					inputState.guessing++;
					try {
						{
						match('"');
						{
						if ((_tokenSet_4.member(LA(1)))) {
							matchNot('"');
						}
						else if ((LA(1)=='"')) {
							match('"');
							matchNot('"');
						}
						else {
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched585 = false;
					}
					rewind(_m585);
					inputState.guessing--;
				}
				if ( synPredMatched585 ) {
					match('"');
				}
				else if ((_tokenSet_3.member(LA(1)))) {
					mSTRING_CH(false);
				}
			else {
				break _loop586;
			}
			}
		} while (true);
		}
		{
		switch ( LA(1)) {
		case '"':
		{
			{
			if (((LA(1)=='"') && (LA(2)=='"'))&&(  tripleQuote )) {
				_saveIndex=text.length();
				match("\"\"\"");
				text.setLength(_saveIndex);
			}
			else if (((LA(1)=='"') && (true))&&( !tripleQuote )) {
				_saveIndex=text.length();
				match("\"");
				text.setLength(_saveIndex);
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				if (fromStart)      tt = STRING_LITERAL;  // plain string literal!
				if (!tripleQuote)       {--suppressNewline;}
				// done with string constructor!
				//assert(stringCtorState == 0);
				
			}
			break;
		}
		case '$':
		{
			_saveIndex=text.length();
			match('$');
			text.setLength(_saveIndex);
			if ( inputState.guessing==0 ) {
				
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
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			_ttype = tt;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
		return tt;
	}
	
	protected final void mHEX_DIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEX_DIGIT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			matchRange('0','9');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':
		{
			matchRange('A','F');
			break;
		}
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':
		{
			matchRange('a','f');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mVOCAB(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = VOCAB;
		int _saveIndex;
		
		matchRange('\3','\377');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mIDENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENT;
		int _saveIndex;
		
		mLETTER(false);
		{
		_loop603:
		do {
			switch ( LA(1)) {
			case 'A':  case 'B':  case 'C':  case 'D':
			case 'E':  case 'F':  case 'G':  case 'H':
			case 'I':  case 'J':  case 'K':  case 'L':
			case 'M':  case 'N':  case 'O':  case 'P':
			case 'Q':  case 'R':  case 'S':  case 'T':
			case 'U':  case 'V':  case 'W':  case 'X':
			case 'Y':  case 'Z':  case '_':  case 'a':
			case 'b':  case 'c':  case 'd':  case 'e':
			case 'f':  case 'g':  case 'h':  case 'i':
			case 'j':  case 'k':  case 'l':  case 'm':
			case 'n':  case 'o':  case 'p':  case 'q':
			case 'r':  case 's':  case 't':  case 'u':
			case 'v':  case 'w':  case 'x':  case 'y':
			case 'z':
			{
				mLETTER(false);
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				mDIGIT(false);
				break;
			}
			default:
			{
				break _loop603;
			}
			}
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			
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
			_ttype = ttype;
			
			// check if "assert" keyword is enabled
			if (assertEnabled && "assert".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
			_ttype = LITERAL_assert; // set token type for the rule in the parser
			}
			// check if "enum" keyword is enabled
			if (enumEnabled && "enum".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
			_ttype = LITERAL_enum; // set token type for the rule in the parser
			}
			
		}
		_ttype = testLiteralsTable(_ttype);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLETTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LETTER;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('A','Z');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIGIT;
		int _saveIndex;
		
		matchRange('0','9');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNUM_INT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUM_INT;
		int _saveIndex;
		Token f2=null;
		Token g2=null;
		Token f3=null;
		Token g3=null;
		Token f4=null;
		boolean isDecimal=false; Token t=null;
		
		{
		switch ( LA(1)) {
		case '0':
		{
			match('0');
			if ( inputState.guessing==0 ) {
				isDecimal = true;
			}
			{
			if ((LA(1)=='X'||LA(1)=='x')) {
				{
				switch ( LA(1)) {
				case 'x':
				{
					match('x');
					break;
				}
				case 'X':
				{
					match('X');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					isDecimal = false;
				}
				{
				int _cnt611=0;
				_loop611:
				do {
					if ((_tokenSet_5.member(LA(1))) && (true) && (true) && (true)) {
						mHEX_DIGIT(false);
					}
					else {
						if ( _cnt611>=1 ) { break _loop611; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt611++;
				} while (true);
				}
			}
			else {
				boolean synPredMatched617 = false;
				if ((((LA(1) >= '0' && LA(1) <= '9')) && (true) && (true) && (true))) {
					int _m617 = mark();
					synPredMatched617 = true;
					inputState.guessing++;
					try {
						{
						{
						int _cnt614=0;
						_loop614:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9'))) {
								matchRange('0','9');
							}
							else {
								if ( _cnt614>=1 ) { break _loop614; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
							}
							
							_cnt614++;
						} while (true);
						}
						{
						switch ( LA(1)) {
						case '.':
						{
							match('.');
							{
							matchRange('0','9');
							}
							break;
						}
						case 'E':  case 'e':
						{
							mEXPONENT(false);
							break;
						}
						case 'D':  case 'F':  case 'd':  case 'f':
						{
							mFLOAT_SUFFIX(false);
							break;
						}
						default:
						{
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						}
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched617 = false;
					}
					rewind(_m617);
					inputState.guessing--;
				}
				if ( synPredMatched617 ) {
					{
					int _cnt619=0;
					_loop619:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9'))) {
							matchRange('0','9');
						}
						else {
							if ( _cnt619>=1 ) { break _loop619; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt619++;
					} while (true);
					}
				}
				else if (((LA(1) >= '0' && LA(1) <= '7')) && (true) && (true) && (true)) {
					{
					int _cnt621=0;
					_loop621:
					do {
						if (((LA(1) >= '0' && LA(1) <= '7'))) {
							matchRange('0','7');
						}
						else {
							if ( _cnt621>=1 ) { break _loop621; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt621++;
					} while (true);
					}
					if ( inputState.guessing==0 ) {
						isDecimal = false;
					}
				}
				else {
				}
				}
				}
				break;
			}
			case '1':  case '2':  case '3':  case '4':
			case '5':  case '6':  case '7':  case '8':
			case '9':
			{
				{
				matchRange('1','9');
				}
				{
				_loop624:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						break _loop624;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					isDecimal=true;
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			{
			switch ( LA(1)) {
			case 'L':  case 'l':
			{
				{
				switch ( LA(1)) {
				case 'l':
				{
					match('l');
					break;
				}
				case 'L':
				{
					match('L');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					_ttype = NUM_LONG;
				}
				break;
			}
			case 'I':  case 'i':
			{
				{
				switch ( LA(1)) {
				case 'i':
				{
					match('i');
					break;
				}
				case 'I':
				{
					match('I');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					_ttype = NUM_INT;
				}
				break;
			}
			case 'G':  case 'g':
			{
				mBIG_SUFFIX(false);
				if ( inputState.guessing==0 ) {
					_ttype = NUM_BIG_INT;
				}
				break;
			}
			default:
				boolean synPredMatched630 = false;
				if ((((LA(1)=='.'||LA(1)=='D'||LA(1)=='E'||LA(1)=='F'||LA(1)=='d'||LA(1)=='e'||LA(1)=='f'))&&(isDecimal))) {
					int _m630 = mark();
					synPredMatched630 = true;
					inputState.guessing++;
					try {
						{
						if ((_tokenSet_6.member(LA(1)))) {
							matchNot('.');
						}
						else if ((LA(1)=='.')) {
							match('.');
							{
							matchRange('0','9');
							}
						}
						else {
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						
						}
					}
					catch (RecognitionException pe) {
						synPredMatched630 = false;
					}
					rewind(_m630);
					inputState.guessing--;
				}
				if ( synPredMatched630 ) {
					{
					switch ( LA(1)) {
					case '.':
					{
						match('.');
						{
						int _cnt633=0;
						_loop633:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9'))) {
								matchRange('0','9');
							}
							else {
								if ( _cnt633>=1 ) { break _loop633; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
							}
							
							_cnt633++;
						} while (true);
						}
						{
						if ((LA(1)=='E'||LA(1)=='e')) {
							mEXPONENT(false);
						}
						else {
						}
						
						}
						{
						switch ( LA(1)) {
						case 'D':  case 'F':  case 'd':  case 'f':
						{
							mFLOAT_SUFFIX(true);
							f2=_returnToken;
							if ( inputState.guessing==0 ) {
								t=f2;
							}
							break;
						}
						case 'G':  case 'g':
						{
							mBIG_SUFFIX(true);
							g2=_returnToken;
							if ( inputState.guessing==0 ) {
								t=g2;
							}
							break;
						}
						default:
							{
							}
						}
						}
						break;
					}
					case 'E':  case 'e':
					{
						mEXPONENT(false);
						{
						switch ( LA(1)) {
						case 'D':  case 'F':  case 'd':  case 'f':
						{
							mFLOAT_SUFFIX(true);
							f3=_returnToken;
							if ( inputState.guessing==0 ) {
								t=f3;
							}
							break;
						}
						case 'G':  case 'g':
						{
							mBIG_SUFFIX(true);
							g3=_returnToken;
							if ( inputState.guessing==0 ) {
								t=g3;
							}
							break;
						}
						default:
							{
							}
						}
						}
						break;
					}
					case 'D':  case 'F':  case 'd':  case 'f':
					{
						mFLOAT_SUFFIX(true);
						f4=_returnToken;
						if ( inputState.guessing==0 ) {
							t=f4;
						}
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						
						String txt = (t == null ? "" : t.getText().toUpperCase());
						if (txt.indexOf('F') >= 0) {
						_ttype = NUM_FLOAT;
						} else if (txt.indexOf('G') >= 0) {
						_ttype = NUM_BIG_DECIMAL;
						} else {
						_ttype = NUM_DOUBLE; // assume double
						}
						
					}
				}
				else {
				}
			}
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
		}
		
	protected final void mEXPONENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EXPONENT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'e':
		{
			match('e');
			break;
		}
		case 'E':
		{
			match('E');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case '+':
		{
			match('+');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		int _cnt642=0;
		_loop642:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9'))) {
				matchRange('0','9');
			}
			else {
				if ( _cnt642>=1 ) { break _loop642; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt642++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mFLOAT_SUFFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLOAT_SUFFIX;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'f':
		{
			match('f');
			break;
		}
		case 'F':
		{
			match('F');
			break;
		}
		case 'd':
		{
			match('d');
			break;
		}
		case 'D':
		{
			match('D');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBIG_SUFFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BIG_SUFFIX;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'g':
		{
			match('g');
			break;
		}
		case 'G':
		{
			match('G');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT;
		int _saveIndex;
		
		match('@');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[2048];
		data[0]=-9224L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[2048];
		data[0]=-4398046520328L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[2048];
		data[0]=-549755813896L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[2048];
		data[0]=-635655169032L;
		data[1]=-268435457L;
		for (int i = 2; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[2048];
		data[0]=-17179869192L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=541165879422L;
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[2048];
		data[0]=-70368744177672L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	
	}
