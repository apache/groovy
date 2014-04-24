
grammar Groovy;

// LEXER

LINE_COMMENT: '//' .*? '\n' -> type(NL) ;
BLOCK_COMMENT: '/*' .*? '*/' -> type(NL) ;

WS: [ \t]+ -> skip;


KW_CLASS: 'class' ;
KW_PACKAGE: 'package' ;
KW_IMPORT: 'import' ;

KW_DEF: 'def' ;
KW_NULL: 'null' ;

STRING: QUOTE STRING_BODY QUOTE ;
fragment STRING_BODY: (~'\'')* ;
fragment QUOTE: '\'';
NUMBER: '-'?[0-9]+ ;

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


// Match both UNIX and Windows newlines
NL: '\r'? '\n';

IDENTIFIER: [A-Za-z][A-Za-z0-9_]*;

// PARSER

compilationUnit: (NL*) packageDefinition? (NL | ';')* (importStatement | NL)* (NL | ';')* (classDeclaration | NL)* EOF;

packageDefinition:
    KW_PACKAGE (IDENTIFIER ('.' IDENTIFIER)*);
importStatement:
    KW_IMPORT (IDENTIFIER ('.' IDENTIFIER)*);
classDeclaration:
    classModifiers KW_CLASS IDENTIFIER (NL)* '{' (classMember | NL | ';')* '}';
classMember:
    methodDeclaration | fieldDeclaration | constructorDeclaration;

// Members // FIXME Make more strict check for def keyword. There should be no way to ommit everything but IDENTIFIER.
methodDeclaration:
    (memberModifier+ typeDeclaration? | typeDeclaration)
    IDENTIFIER '(' argumentDeclarationList ')' '{' blockStatement? '}'; // Inner NL 's handling.
fieldDeclaration:
    (memberModifier+ typeDeclaration? | typeDeclaration) IDENTIFIER ;
constructorDeclaration:
    VISIBILITY_MODIFIER? IDENTIFIER '(' argumentDeclarationList ')' '{' blockStatement? '}'; // Inner NL 's handling.

memberModifier:
    VISIBILITY_MODIFIER | KW_STATIC | (KW_ABSTRACT | KW_FINAL) | KW_NATIVE | KW_SYNCHRONIZED | KW_TRANSIENT | KW_VOLATILE ;

typeDeclaration:
    (IDENTIFIER | KW_DEF)
;

argumentDeclarationList:
    argumentDeclaration (',' argumentDeclaration)* | /* EMPTY ARGUMENT LIST */ ;
argumentDeclaration:
    typeDeclaration? IDENTIFIER ;

blockStatement: (statement | NL)+ ;

statement:
    expression #expressionStatement
;

expression:
    expression ('.' | '?.' | '*.') IDENTIFIER '(' argumentList ')' #methodCallExpression
    | expression ('.' | '?.' | '*.' | '.@') IDENTIFIER #fieldAccessExpression
    | expression '(' argumentList? ')' #callExpression
    | expression ('--' | '++') #postfixExpression
    | ('!' | '~') expression #unaryExpression
    | ('+' | '-') expression #unaryExpression
    | ('--' | '++') expression #prefixExpression
    | expression ('**') expression #binaryExpression
    | expression ('*' | '/' | '%') expression #binaryExpression
    | expression ('+' | '-') expression #binaryExpression
    | expression ('<<' | '>>' | '>>>' | '..' | '..<') expression #binaryExpression
    | expression ((('<' | '<=' | '>' | '>=' | 'in') expression) | (('as' | 'instanceof') IDENTIFIER)) #binaryExpression
    | expression ('==' | '!=' | '<=>') expression #binaryExpression
    | expression ('=~' | '==~') expression #binaryExpression
    | expression ('&') expression #binaryExpression
    | expression ('^') expression #binaryExpression
    | expression ('|') expression #binaryExpression
    | expression ('&&') expression #binaryExpression
    | expression ('||') expression #binaryExpression
    | expression ('=') expression #assignmentExpression
    | typeDeclaration IDENTIFIER ('=' expression)? #declarationExpression
    | STRING #constantExpression
    | NUMBER #constantExpression
    | KW_NULL #nullExpression
    | IDENTIFIER #variableExpression ;

classModifiers: //JSL7 8.1 FIXME Now gramar allows modifier duplication. It's possible to make it more strict listing all 24 permutations.
(VISIBILITY_MODIFIER | KW_STATIC | (KW_ABSTRACT | KW_FINAL) | KW_STRICTFP)* ;

argumentList: expression (',' expression)* ;
