
parser grammar GroovyParser;

options { tokenVocab = GroovyLexer; }

@members {
    String currentClassName = null; // Used for correct constructor recognition.
}

compilationUnit: SHEBANG_COMMENT? (NL*) packageDefinition? (NL | SEMICOLON)* (importStatement | NL)* (NL | SEMICOLON)* (classDeclaration | enumDeclaration | NL)* (statement | NL)* EOF;

packageDefinition:
    (annotationClause (NL | annotationClause)*)? KW_PACKAGE (IDENTIFIER (DOT IDENTIFIER)*);
importStatement:
    (annotationClause (NL | annotationClause)*)? KW_IMPORT (IDENTIFIER (DOT IDENTIFIER)* (DOT MULT)?);
classDeclaration:
    ((annotationClause | classModifier) (NL | annotationClause | classModifier)*)? (AT KW_INTERFACE | KW_CLASS | KW_INTERFACE) IDENTIFIER { currentClassName = $IDENTIFIER.text; } genericDeclarationList? extendsClause? implementsClause? (NL)* classBody ;
enumDeclaration:
    ((annotationClause | classModifier) (NL | annotationClause | classModifier)*)? KW_ENUM IDENTIFIER { currentClassName = $IDENTIFIER.text; } implementsClause? (NL)* LCURVE (enumMember | NL | SEMICOLON)* RCURVE ;
classMember:
    constructorDeclaration | methodDeclaration | fieldDeclaration | objectInitializer | classInitializer | classDeclaration | enumDeclaration ;
enumMember:
    IDENTIFIER (COMMA | NL)
    | classMember
;
implementsClause:  KW_IMPLEMENTS genericClassNameExpression (COMMA genericClassNameExpression)* ;
extendsClause:  KW_EXTENDS genericClassNameExpression ;

// Members // FIXME Make more strict check for def keyword. It can't repeat.
methodDeclaration:
    (
        (memberModifier | annotationClause | KW_DEF) (memberModifier | annotationClause | KW_DEF | NL)* (
            (genericDeclarationList genericClassNameExpression) | typeDeclaration
        )?
    |
        genericClassNameExpression
    )
    IDENTIFIER LPAREN argumentDeclarationList RPAREN throwsClause? methodBody?
;

methodBody:
    LCURVE blockStatement? RCURVE
;

fieldDeclaration:
    (memberModifier | annotationClause | KW_DEF) (memberModifier | annotationClause | KW_DEF | NL)* genericClassNameExpression? IDENTIFIER ('=' expression)?
    | genericClassNameExpression IDENTIFIER
;
constructorDeclaration: { _input.LT(_input.LT(1).getType() == VISIBILITY_MODIFIER ? 2 : 1).getText().equals(currentClassName) }?
    VISIBILITY_MODIFIER? IDENTIFIER LPAREN argumentDeclarationList RPAREN throwsClause? LCURVE blockStatement? RCURVE ; // Inner NL 's handling.
objectInitializer: LCURVE blockStatement? RCURVE ;
classInitializer: KW_STATIC LCURVE blockStatement? RCURVE ;

typeDeclaration:
    (genericClassNameExpression | KW_DEF)
;

annotationClause: //FIXME handle assignment expression.
    AT genericClassNameExpression ( LPAREN ((annotationElementPair (COMMA annotationElementPair)*) | annotationElement)? RPAREN )?
;
annotationElementPair: IDENTIFIER ASSIGN annotationElement ;
annotationElement: annotationParameter | annotationClause ;

genericDeclarationList:
    LT genericsDeclarationElement (COMMA genericsDeclarationElement)* GT
;

genericsDeclarationElement: genericClassNameExpression (KW_EXTENDS genericClassNameExpression (BAND genericClassNameExpression)* )? ;

throwsClause: KW_THROWS classNameExpression (COMMA classNameExpression)*;

argumentDeclarationList:
    argumentDeclaration (COMMA argumentDeclaration)* | /* EMPTY ARGUMENT LIST */ ;
argumentDeclaration:
    annotationClause* typeDeclaration? IDENTIFIER ('=' expression)? ;

blockStatement: (statement | NL)+ ;

declarationRule: annotationClause* typeDeclaration IDENTIFIER (ASSIGN expression)? ;
newInstanceRule: KW_NEW genericClassNameExpression (LPAREN argumentList? RPAREN) (classBody)?;
newArrayRule: KW_NEW classNameExpression (LBRACK INTEGER RBRACK)* ;
classBody: LCURVE (classMember | NL | SEMICOLON)* RCURVE ;

statement:
    declarationRule #declarationStatement
    | newArrayRule #newArrayStatement
    | newInstanceRule #newInstanceStatement
    | cmdExpressionRule #commandExpressionStatement
    | expression #expressionStatement
    | KW_FOR LPAREN (expression)? SEMICOLON expression? SEMICOLON expression? RPAREN LCURVE (statement | SEMICOLON | NL)* RCURVE #classicForStatement
    | KW_FOR LPAREN typeDeclaration? IDENTIFIER KW_IN expression RPAREN LCURVE (statement | SEMICOLON | NL)* RCURVE #forInStatement
    | KW_IF LPAREN expression RPAREN LCURVE (statement | SEMICOLON | NL)*  RCURVE (KW_ELSE LCURVE (statement | SEMICOLON | NL)* RCURVE)? #ifStatement
    | KW_WHILE LPAREN expression RPAREN LCURVE (statement | SEMICOLON | NL)*  RCURVE  #whileStatement
    | KW_SWITCH LPAREN expression RPAREN LCURVE
        (
          (caseStatement | NL)*
          (KW_DEFAULT COLON (statement | SEMICOLON | NL)*)?
        )
      RCURVE #switchStatement
    |  tryBlock ((catchBlock+ finallyBlock?) | finallyBlock) #tryCatchFinallyStatement
    | (KW_CONTINUE | KW_BREAK) #controlStatement
    | KW_RETURN expression? #returnStatement
    | KW_THROW expression #throwStatement
;

tryBlock: KW_TRY LCURVE blockStatement? RCURVE NL*;
catchBlock: KW_CATCH LPAREN ((classNameExpression (BOR classNameExpression)* IDENTIFIER) | IDENTIFIER) RPAREN LCURVE blockStatement? RCURVE NL*;
finallyBlock: KW_FINALLY LCURVE blockStatement? RCURVE;

caseStatement: (KW_CASE expression COLON (statement | SEMICOLON | NL)* );

cmdExpressionRule: pathExpression ( argumentList IDENTIFIER)* argumentList IDENTIFIER? ;
pathExpression: (IDENTIFIER DOT)* IDENTIFIER ;
gstringPathExpression: IDENTIFIER (GSTRING_PATH_PART)* ;

closureExpressionRule: LCURVE (argumentDeclarationList CLOSURE_ARG_SEPARATOR)? blockStatement? RCURVE ;
gstring: GSTRING_START (gstringPathExpression | LCURVE expression? RCURVE) (GSTRING_PART (gstringPathExpression | LCURVE expression? RCURVE))* GSTRING_END ;

// Special cases.
// 1. Command expression(parenthesis-less expressions)
// 2. Annotation paramenthers.. (inline constant)
// 3. Constant expressions.
// 4. class ones, for instanceof and as (type specifier)

annotationParameter:
    LBRACK (annotationParameter (COMMA annotationParameter)*)? RBRACK #annotationParamArrayExpression
    | pathExpression #annotationParamPathExpression //class, enum or constant field
    | genericClassNameExpression #annotationParamClassExpression //class
    | STRING #annotationParamStringExpression //primitive
    | DECIMAL #annotationParamDecimalExpression //primitive
    | INTEGER #annotationParamIntegerExpression //primitive
    | KW_NULL #annotationParamNullExpression //primitive
    | (KW_TRUE | KW_FALSE) #annotationParamBoolExpression //primitive
;

expression:
    declarationRule #declarationExpression
    | newArrayRule #newArrayExpression
    | newInstanceRule #newInstanceExpression
    | closureExpressionRule #closureExpression
    | LBRACK (expression (COMMA expression)*)?RBRACK #listConstructor
    | LBRACK (COLON | (mapEntry (COMMA mapEntry)*) )RBRACK #mapConstructor
    | expression (DOT | SAFE_DOT | STAR_DOT) IDENTIFIER LPAREN argumentList? RPAREN #methodCallExpression
    | expression (DOT | SAFE_DOT | STAR_DOT | ATTR_DOT) IDENTIFIER #fieldAccessExpression
    | pathExpression (LPAREN argumentList? RPAREN)? closureExpressionRule* #callExpression
    | LPAREN expression RPAREN #parenthesisExpression
    | expression (DECREMENT | INCREMENT)  #postfixExpression
    | (NOT | BNOT) expression #unaryExpression
//  | (PLUS | MINUS) expression #unaryExpression // FIXME: return unary minus and plus expressions.
    | (DECREMENT | INCREMENT) expression #prefixExpression
    | expression POWER expression #binaryExpression
    | expression (MULT | DIV | MOD) expression #binaryExpression
    | expression (PLUS | MINUS) expression #binaryExpression
    | expression (LSHIFT | GT GT | GT GT GT | RANGE | ORANGE) expression #binaryExpression
    | expression (((LT | LTE | GT | GTE | KW_IN) expression) | ((KW_AS | KW_INSTANCEOF) genericClassNameExpression)) #binaryExpression
    | expression (EQUAL | UNEQUAL | SPACESHIP) expression #binaryExpression
    | expression (FIND | MATCH) expression #binaryExpression
    | expression BAND expression #binaryExpression
    |<assoc=right> expression XOR expression #binaryExpression
    | expression BOR expression #binaryExpression
    | expression AND expression #binaryExpression
    | expression OR expression #binaryExpression
    | expression (ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | MULT_ASSIGN | DIV_ASSIGN | MOD_ASSIGN | BAND_ASSIGN | XOR_ASSIGN | BOR_ASSIGN | LSHIFT_ASSIGN | RSHIFT_ASSIGN | RUSHIFT_ASSIGN) expression #assignmentExpression
    | STRING #constantExpression
    | gstring #gstringExpression
    | DECIMAL #constantDecimalExpression
    | INTEGER #constantIntegerExpression
    | KW_NULL #nullExpression
    | (KW_TRUE | KW_FALSE) #boolExpression
    | IDENTIFIER #variableExpression
;

classNameExpression: { GrammarPredicates.isClassName(_input) }? IDENTIFIER (DOT IDENTIFIER)* ;

genericClassNameExpression: classNameExpression (genericList | (LBRACK RBRACK))?;

genericList:
    LT genericListElement (COMMA genericListElement)* GT
;

genericListElement:
    genericClassNameExpression #genericsConcreteElement
    | (IDENTIFIER | QUESTION) (KW_EXTENDS genericClassNameExpression | KW_SUPER genericClassNameExpression)? #genericsWildcardElement
;

mapEntry:
    STRING COLON expression
    | IDENTIFIER COLON expression
    | LPAREN expression RPAREN COLON expression
;

classModifier: //JSL7 8.1 FIXME Now gramar allows modifier duplication. It's possible to make it more strict listing all 24 permutations.
VISIBILITY_MODIFIER | KW_STATIC | (KW_ABSTRACT | KW_FINAL) | KW_STRICTFP ;

memberModifier:
    VISIBILITY_MODIFIER | KW_STATIC | (KW_ABSTRACT | KW_FINAL) | KW_NATIVE | KW_SYNCHRONIZED | KW_TRANSIENT | KW_VOLATILE ;

argumentList: ( (closureExpressionRule)+ | expression (COMMA expression)*) ;
