
grammar Groovy;

// LEXER

LINE_COMMENT: '//' .*? '\n' -> type(NL) ;
BLOCK_COMMENT: '/*' .*? '*/' -> type(NL) ;

WS: [ \t]+ -> skip;


KW_CLASS: 'class' ;
KW_PACKAGE: 'package' ;
KW_IMPORT: 'import' ;

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
    KW_CLASS IDENTIFIER (NL)* '{' NL* /* MEMBERS */ '}';
