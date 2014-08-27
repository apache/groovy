
lexer grammar GroovyLexer;

@members {
    enum Brace {
       ROUND,
       SQUARE,
       CURVE,
    };
    java.util.Deque<Brace> braceStack = new java.util.ArrayDeque<Brace>();
    Brace topBrace = null;
    int lastTokenType = 0;
    long tokenIndex = 0;
    long tlePos = 0;

    @Override public Token nextToken() {
        if (!(_interp instanceof PositionAdjustingLexerATNSimulator))
            _interp = new PositionAdjustingLexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);

        return super.nextToken();
    }

    public void emit(Token token) {
        tokenIndex++;
        lastTokenType = token.getType();
        //System.out.println("EM: " + tokenNames[lastTokenType != -1 ? lastTokenType : 0] + ": " + lastTokenType + " TLE = " + (tlePos == tokenIndex) + " " + tlePos + "/" + tokenIndex + " " + token.getText());
        if (token.getType() == ROLLBACK_ONE) {
           ((PositionAdjustingLexerATNSimulator)getInterpreter()).resetAcceptPosition(getInputStream(), _tokenStartCharIndex - 1, _tokenStartLine, _tokenStartCharPositionInLine - 1);
        }
        super.emit(token);
    }

    public void pushBrace(Brace b) {
        braceStack.push(b);
        topBrace = braceStack.peekFirst();
        //System.out.println("> " + topBrace);
    }

    public void popBrace() {
        braceStack.pop();
        topBrace = braceStack.peekFirst();
        //System.out.println("> " + topBrace);
    }

    public boolean isSlashyStringAlowed() {
        java.util.List<Integer> ints = java.util.Arrays.asList(PLUS, NOT, BNOT, MULT); // FIXME add more operators.
        //System.out.println("SP: " + " TLECheck = " + (tlePos == tokenIndex) + " " + tlePos + "/" + tokenIndex);
        boolean isLastTokenOp = ints.contains(Integer.valueOf(lastTokenType));
        boolean res = isLastTokenOp || tlePos == tokenIndex;
        //System.out.println("SP: " + tokenNames[lastTokenType] + ": " + lastTokenType + " res " + res + (res ? ( isLastTokenOp ? " op" : " tle") : ""));
        return res;
    }
}

LINE_COMMENT: '//' .*? '\n' -> type(NL) ;
BLOCK_COMMENT: '/*' .*? '*/' -> type(NL) ;
SHEBANG_COMMENT: { tokenIndex == 0 }? '#!' .*? '\n' -> skip ;

WS: [ \t]+ -> skip ;

LPAREN : '(' { pushBrace(Brace.ROUND); tlePos = tokenIndex + 1; } -> pushMode(DEFAULT_MODE) ;
RPAREN : ')' { popBrace(); } -> popMode ;
LBRACK : '[' { pushBrace(Brace.SQUARE); tlePos = tokenIndex + 1; } -> pushMode(DEFAULT_MODE) ;
RBRACK : ']' { popBrace(); } -> popMode ;
LCURVE : '{' { pushBrace(Brace.CURVE); tlePos = tokenIndex + 1; } -> pushMode(DEFAULT_MODE) ;
RCURVE : '}' { popBrace(); } -> popMode ;

MULTILINE_STRING:
    ('\'\'\'' STRING_ELEMENT*? '\'\'\''
    | '"""' STRING_ELEMENT*? '"""'
    | '\'' STRING_ELEMENT*? (NL | '\'')
    | '"' STRING_ELEMENT*? (NL | '"')) -> type(STRING)
;

SLASHY_STRING: '/' { isSlashyStringAlowed() }? SLASHY_STRING_ELEMENT*? '/' -> type(STRING) ;
STRING: '"' DQ_STRING_ELEMENT*? '"'  | '\'' QUOTED_STRING_ELEMENT*? '\'' ;

GSTRING_START: '"' DQ_STRING_ELEMENT*? '$' -> pushMode(DOUBLE_QUOTED_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE) ;
SLASHY_GSTRING_START: '/' SLASHY_STRING_ELEMENT*? '$' -> type(GSTRING_START), pushMode(SLASHY_GSTRING_MODE), pushMode(GSTRING_TYPE_SELECTOR_MODE) ;

fragment SLASHY_STRING_ELEMENT: SLASHY_ESCAPE | ~('$' | '/') ;
fragment STRING_ELEMENT: ESC_SEQUENCE | ~('$') ;
fragment QUOTED_STRING_ELEMENT: ESC_SEQUENCE | ~('\'') ;
fragment DQ_STRING_ELEMENT: ESC_SEQUENCE | ~('"' | '$') ;

mode DOUBLE_QUOTED_GSTRING_MODE ;
    GSTRING_END: '"' -> popMode ;
    GSTRING_PART: '$' -> pushMode(GSTRING_TYPE_SELECTOR_MODE) ;
    GSTRING_ELEMENT: (ESC_SEQUENCE | ~('$' | '"')) -> more ;

mode SLASHY_GSTRING_MODE ;
    SLASHY_GSTRING_END: '/' -> type(GSTRING_END), popMode ;
    SLASHY_GSTRING_PART: '$' -> type(GSTRING_PART), pushMode(GSTRING_TYPE_SELECTOR_MODE) ;
    SLASHY_GSTRING_ELEMENT: (SLASHY_ESCAPE | ~('$' | '/')) -> more ;

mode GSTRING_TYPE_SELECTOR_MODE ; // We drop here after exiting curved brace?
    GSTRING_BRACE_L: '{' { pushBrace(Brace.CURVE); tlePos = tokenIndex + 1; } -> type(LCURVE), popMode, pushMode(DEFAULT_MODE) ;
    GSTRING_ID: [A-Za-z_][A-Za-z0-9_]* -> type(IDENTIFIER), popMode, pushMode(GSTRING_PATH) ;

mode GSTRING_PATH ;
    GSTRING_PATH_PART: '.' [A-Za-z_][A-Za-z0-9_]* ;
    ROLLBACK_ONE: . -> popMode, channel(HIDDEN) ; // This magic is for exit this state if

mode DEFAULT_MODE ;

fragment SLASHY_ESCAPE: '\\' '/' ;
fragment ESC_SEQUENCE: '\\' [btnfr"'\\] | OCTAL_ESC_SEQ ;
fragment OCTAL_ESC_SEQ: '\\' [0-3]? [0-7]? [0-7] ;

// Numbers
DECIMAL: SIGN? DIGITS ('.' DIGITS EXP_PART? | EXP_PART) DECIMAL_TYPE_MODIFIER? ;
INTEGER: SIGN? (('0x' | '0X') HEX_DIGITS | '0' OCT_DIGITS | DEC_DIGITS) INTEGER_TYPE_MODIFIER? ;

fragment DIGITS: [0-9] | [0-9][0-9_]*[0-9] ;
fragment DEC_DIGITS: [0-9] | [1-9][0-9_]*[0-9] ;
fragment OCT_DIGITS: [0-7] | [0-7][0-7_]*[0-7] ;
fragment HEX_DIGITS: [0-9abcdefABCDEF] | [0-9abcdefABCDEF][0-9abcdefABCDEF_]*[0-9abcdefABCDEF] ;  // Simplify by extracting one digit element?

fragment SIGN: ('-'|'+') ;
fragment EXP_PART: ([eE] SIGN? [0-9]+) ;

fragment INTEGER_TYPE_MODIFIER: ('G' | 'L' | 'I' | 'g' | 'l' | 'i') ;
fragment DECIMAL_TYPE_MODIFIER: ('G' | 'D' | 'F' | 'g' | 'd' | 'f') ;

KW_CLASS: 'class' ;
KW_INTERFACE: 'interface' ;
KW_ENUM: 'enum' ;

KW_PACKAGE: 'package' ;
KW_IMPORT: 'import' ;
KW_EXTENDS: 'extends' ;
KW_IMPLEMENTS: 'implements' ;

KW_DEF: 'def' ;
KW_NULL: 'null' ;
KW_TRUE: 'true' ;
KW_FALSE: 'false' ;
KW_NEW: 'new' ;
KW_SUPER: 'super' ;

KW_IN: 'in' ;
KW_FOR: 'for' ;
KW_IF: 'if' ;
KW_ELSE: 'else' ;
KW_WHILE: 'while' ;
KW_SWITCH: 'switch' ;
KW_CASE: 'case' ;
KW_DEFAULT: 'default' ;
KW_CONTINUE: 'continue' ;
KW_BREAK: 'break' ;
KW_RETURN: 'return' ;
KW_TRY: 'try' ;
KW_CATCH: 'catch' ;
KW_FINALLY: 'finally' ;
KW_THROW: 'throw' ;
KW_THROWS: 'throws' ;

RUSHIFT_ASSIGN: '>>>=' ;
RSHIFT_ASSIGN: '>>=' ;
LSHIFT_ASSIGN: '<<=' ;
//RUSHIFT: '>>>' ;
SPACESHIP: '<=>' ;
ELVIS: '?:' ;
SAFE_DOT: '?.' ;
STAR_DOT: '*.' ;
ATTR_DOT: '.@' ;
LTE: '<=' ;
GTE: '>=' ;
CLOSURE_ARG_SEPARATOR: '->' ;
DECREMENT: '--' ;
INCREMENT: '++' ;
POWER: '**' ;
LSHIFT: '<<' ;
//RSHIFT: '>>' ;
RANGE: '..' ;
ORANGE: '..<' ;
EQUAL: '==' ;
UNEQUAL: '!=' ;
MATCH: '==~' ;
FIND: '=~' ;
AND: '&&' ;
OR: '||' ;
PLUS_ASSIGN: '+=' ;
MINUS_ASSIGN: '-=' ;
MULT_ASSIGN: '*=' ;
DIV_ASSIGN: '/=' ;
MOD_ASSIGN: '%=' ;
BAND_ASSIGN: '&=' ;
XOR_ASSIGN: '^=' ;
BOR_ASSIGN: '|=' ;

SEMICOLON: ';' ;
DOT: '.' ;
COMMA: ',' ;
AT: '@' ;
ASSIGN: '=' ;
LT: '<' ;
GT: '>' ;
COLON: ':' ;
BOR: '|' ;
NOT: '!' ;
BNOT: '~' ;
MULT: '*' ;
DIV: '/' ;
MOD: '%' ;
PLUS: '+' ;
MINUS: '-' ;
BAND: '&' ;
XOR: '^' ;
QUESTION: '?' ;
KW_AS: 'as' ;
KW_INSTANCEOF: 'instanceof' ;

// Modifiers
VISIBILITY_MODIFIER: (KW_PUBLIC | KW_PROTECTED | KW_PRIVATE) ;
fragment KW_PUBLIC: 'public' ;
fragment KW_PROTECTED: 'protected' ;
fragment KW_PRIVATE: 'private' ;

KW_ABSTRACT: 'abstract' ;
KW_STATIC: 'static' ;
KW_FINAL: 'final' ; // Class
KW_TRANSIENT: 'transient' ; // methods and fields
KW_NATIVE: 'native' ; // Methods and fields, as fields are accesors in Groovy.
KW_VOLATILE: 'volatile' ; // Fields only
KW_SYNCHRONIZED: 'synchronized' ; // Methods and fields.
KW_STRICTFP: 'strictfp';

/** Nested newline within a (..) or [..] are ignored. */
IGNORE_NEWLINE : '\r'? '\n' { topBrace == Brace.ROUND || topBrace == Brace.SQUARE }? -> skip ;

// Match both UNIX and Windows newlines
NL: '\r'? '\n';

IDENTIFIER: [A-Za-z_$][A-Za-z0-9_$]*;
