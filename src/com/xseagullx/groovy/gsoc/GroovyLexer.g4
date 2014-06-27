
lexer grammar GroovyLexer;

@lexer::members {
    enum Brace {
       ROUND,
       SQUARE,
       CURVE,
    };
    java.util.Deque<Brace> braceStack = new java.util.ArrayDeque<Brace>();
    Brace topBrace = null;
}

LINE_COMMENT: '//' .*? '\n' -> type(NL) ;
BLOCK_COMMENT: '/*' .*? '*/' -> type(NL) ;

WS: [ \t]+ -> skip;

KW_CLASS: 'class' ;
KW_PACKAGE: 'package' ;
KW_IMPORT: 'import' ;
KW_EXTENDS: 'extends' ;
KW_IMPLEMENTS: 'implements' ;

KW_DEF: 'def' ;
KW_NULL: 'null' ;
KW_TRUE: 'true' ;
KW_FALSE: 'false' ;

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
RUSHIFT: '>>>' ;
SPACESHIP: '<=>' ;
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
RSHIFT: '>>' ;
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
KW_AS: 'as' ;
KW_INSTANCEOF: 'instanceof' ;

STRING:
    '\'\'\'' STRING_ELEMENT*? '\'\'\''
    | '"""' STRING_ELEMENT*? '"""'
    | '\'' STRING_ELEMENT*? (NL | '\'')
    | '"' STRING_ELEMENT*? (NL | '"')
;

fragment STRING_ELEMENT: ESC_SEQUENCE | . ;
fragment ESC_SEQUENCE: '\\' [btnfr"'\\] | OCTAL_ESC_SEQ | UNICODE_ESC_SEQ ;
fragment OCTAL_ESC_SEQ: '\\' [0-3]? [0-7]? [0-7] ;
fragment UNICODE_ESC_SEQ: '\\u' [0-9abcdefABCDEF] [0-9abcdefABCDEF] [0-9abcdefABCDEF] [0-9abcdefABCDEF] ;

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

LPAREN : '(' { braceStack.push(Brace.ROUND); topBrace = braceStack.peekFirst(); } ;
RPAREN : ')' { braceStack.pop(); topBrace = braceStack.peekFirst(); } ;
LBRACK : '[' { braceStack.push(Brace.SQUARE); topBrace = braceStack.peekFirst(); } ;
RBRACK : ']' { braceStack.pop(); topBrace = braceStack.peekFirst(); } ;
LCURVE : '{' { braceStack.push(Brace.CURVE); topBrace = braceStack.peekFirst(); } ;
RCURVE : '}' { braceStack.pop(); topBrace = braceStack.peekFirst(); } ;

/** Nested newline within a (..) or [..] are ignored. */
IGNORE_NEWLINE : '\r'? '\n' { topBrace == Brace.ROUND || topBrace == Brace.SQUARE }? -> skip ;

// Match both UNIX and Windows newlines
NL: '\r'? '\n';

IDENTIFIER: [A-Za-z][A-Za-z0-9_]*;
