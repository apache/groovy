/*
 * This file is adapted from the Antlr4 Java grammar which has the following license
 *
 *  Copyright (c) 2013 Terence Parr, Sam Harwell
 *  All rights reserved.
 *  [The "BSD licence"]
 *
 *    http://www.opensource.org/licenses/bsd-license.php
 *
 * Subsequent modifications by the Groovy community have been done under the Apache License v2:
 *
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

/**
 * The Groovy grammar is based on the official grammar for Java:
 * https://github.com/antlr/grammars-v4/blob/master/java/Java.g4
 */
parser grammar GroovyParser;

options {
    tokenVocab = GroovyLexer;
    contextSuperClass = GroovyParserRuleContext;
    superClass = AbstractParser;
}

@header {
    import java.util.Map;
    import org.codehaus.groovy.ast.NodeMetaDataHandler;
}

@members {
    private int inSwitchExpressionLevel = 0;
    private int inAsyncClosureLevel = 0;

    public static class GroovyParserRuleContext extends ParserRuleContext implements NodeMetaDataHandler {
        private Map metaDataMap = null;

        public GroovyParserRuleContext() {}

        public GroovyParserRuleContext(ParserRuleContext parent, int invokingStateNumber) {
            super(parent, invokingStateNumber);
        }

        @Override
        public Map<?, ?> getMetaDataMap() {
            return this.metaDataMap;
        }

        @Override
        public void setMetaDataMap(Map<?, ?> metaDataMap) {
            this.metaDataMap = metaDataMap;
        }
    }

    @Override
    public int getSyntaxErrorSource() {
        return GroovySyntaxError.PARSER;
    }

    @Override
    public int getErrorLine() {
        Token token = _input.LT(-1);

        if (null == token) {
            return -1;
        }

        return token.getLine();
    }

    @Override
    public int getErrorColumn() {
        Token token = _input.LT(-1);

        if (null == token) {
            return -1;
        }

        return token.getCharPositionInLine() + 1 + token.getText().length();
    }
}

// starting point for parsing a groovy file
compilationUnit
    :   NL* (packageDeclaration sep?)? scriptStatements? EOF
    ;

scriptStatements
    :   scriptStatement (sep scriptStatement)* sep?
    ;

scriptStatement
    :   importDeclaration // Import statement.  Can be used in any scope.  Has "import x as y" also.
    |   typeDeclaration
    // validate the method in the AstBuilder#visitMethodDeclaration, e.g. method without method body is not allowed
    |   { !SemanticPredicates.isInvalidMethodDeclaration(_input) }?
        methodDeclaration[3, 9]
    |   statement
    ;

packageDeclaration
    :   annotationsOpt PACKAGE qualifiedName
    ;

importDeclaration
    :   annotationsOpt IMPORT
        (   MODULE qualifiedName
        |   STATIC? qualifiedName (DOT MUL | AS alias=identifier)?
        )
    ;


typeDeclaration
    :   classOrInterfaceModifiersOpt classDeclaration
    ;

modifier
    :   classOrInterfaceModifier
    |   m=(   NATIVE
          |   SYNCHRONIZED
          |   TRANSIENT
          |   VOLATILE
          |   DEF
          |   VAL
          |   VAR
          )
    ;

modifiersOpt
    :   (modifiers NL*)?
    ;

modifiers
    :   modifier (NL* modifier)*
    ;

classOrInterfaceModifiersOpt
    :   (classOrInterfaceModifiers NL*)?
    ;

classOrInterfaceModifiers
    :   classOrInterfaceModifier (NL* classOrInterfaceModifier)*
    ;

classOrInterfaceModifier
    :   annotation       // class or interface
    |   m=(   PUBLIC     // class or interface
          |   PROTECTED  // class or interface
          |   PRIVATE    // class or interface
          |   STATIC     // class or interface
          |   ABSTRACT   // class or interface
          |   SEALED     // class or interface
          |   NON_SEALED // class or interface
          |   FINAL      // class only -- does not apply to interfaces
          |   STRICTFP   // class or interface
          |   DEFAULT    // interface only -- does not apply to classes
          )
    ;

variableModifier
    :   annotation
    |   m=( FINAL
          | DEF
          | VAL
          | VAR
          // Groovy supports declaring local variables as instance/class fields,
          // e.g. import groovy.transform.*; @Field static List awe = [1, 2, 3]
          // e.g. import groovy.transform.*; def a = { @Field public List awe = [1, 2, 3] }
          // Notice: Groovy 2.4.7 just allows to declare local variables with the following modifiers when using annotations(e.g. @Field)
          // TODO check whether the following modifiers accompany annotations or not. Because the legacy codes(e.g. benchmark/bench/heapsort.groovy) allow to declare the special instance/class fields without annotations, we leave it as it is for the time being
          | PUBLIC
          | PROTECTED
          | PRIVATE
          | STATIC
          | ABSTRACT
          | STRICTFP
          )
    ;

variableModifiersOpt
    :   (variableModifiers NL*)?
    ;

variableModifiers
    :   variableModifier (NL* variableModifier)*
    ;

typeParameters
    :   LT NL* typeParameter (COMMA NL* typeParameter)* NL* GT
    ;

typeParameter
    :   annotationsOpt className (EXTENDS NL* typeBound)?
    ;

typeBound
    :   type (BITAND NL* type)*
    ;

typeList
    :   type (COMMA NL* type)*
    ;


/**
 *  t   0: class; 1: interface; 2: enum; 3: annotation; 4: trait; 5: record
 */
classDeclaration
locals[ int t ]
    :   (   CLASS        { $t = 0; }
        |   INTERFACE    { $t = 1; }
        |   ENUM         { $t = 2; }
        |   AT INTERFACE { $t = 3; }
        |   TRAIT        { $t = 4; }
        |   RECORD       { $t = 5; }
        )
        identifier
        (NL* typeParameters)?
        (NL* formalParameters)?
        (NL* EXTENDS NL* scs=typeList)?
        (NL* IMPLEMENTS NL* is=typeList)?
        (NL* PERMITS NL* ps=typeList)?
        NL* classBody[$t]
    ;

classBody[int t]
    :   LBRACE NL*
        (
            { $t == 2 }?
            enumConstants (
                (NL* COMMA)?
            |
                // GROOVY-7773, GROOVY-9306:
                ((NL* COMMA)? NL* SEMI)? NL*
                classBodyDeclaration[$t] (sep classBodyDeclaration[$t])*
            )
        |
            (classBodyDeclaration[$t] (sep classBodyDeclaration[$t])* )?
        )
        sep? RBRACE
    ;

enumConstants
    :   enumConstant (NL* COMMA NL* enumConstant)*
    ;

enumConstant
    :   annotationsOpt identifier arguments? anonymousInnerClassDeclaration[1]?
    ;

classBodyDeclaration[int t]
    :   (STATIC NL*)? block
    |   memberDeclaration[$t]
    ;

memberDeclaration[int t]
    :   methodDeclaration[0, $t]
    |   fieldDeclaration
    |   modifiersOpt (  classDeclaration
                     |  compactConstructorDeclaration
                     )
    ;

/**
 *  t   0: *class member* all kinds of method declaration AND constructor declaration,
 *      1: normal method declaration,
 *      2: abstract method declaration
 *      3: method declaration OR abstract method declaration
 *  ct  9: script, other see the comment of classDeclaration
 */
methodDeclaration[int t, int ct]
    :   modifiersOpt typeParameters? (returnType[$ct] NL*)?
        methodName formalParameters
        (   { $ct == 3 }? // GROOVY-11208: @interface only
            (DEFAULT NL* elementValue)
        |
            NL* THROWS NL* qualifiedClassNameList (NL* methodBody)?
        |
            NL* methodBody
        )?
    ;

compactConstructorDeclaration
    :   methodName NL* methodBody
    ;

methodName
    :   identifier
    |   stringLiteral
    ;

returnType[int ct]
    :
        standardType
    |   VOID
    ;

fieldDeclaration
    :   variableDeclaration[1]
    ;

variableDeclarators
    :   variableDeclarator (COMMA NL* variableDeclarator)*
    ;

variableDeclarator
    :   variableDeclaratorId (NL* ASSIGN NL* variableInitializer)?
    ;

variableDeclaratorId
    :   identifier
    ;

variableInitializer
    :   enhancedStatementExpression
    ;

type
    :   annotationsOpt
        (
            VOID // error
        |
            primitiveType
        |
            referenceType
        )
        dim0*
    ;

primitiveType
    :   BuiltInPrimitiveType
    ;

referenceType
    :   qualifiedClassName typeArguments?
    ;

matchingType // see: instanceof / !instanceof type patterns (JEP 394)
    :   standardType identifier?
    ;

// RHS of !instanceof: Type / Type name (pattern), or parenthesised (T) / rejected (A & B)
notInstanceofType
    :   matchingType
    |   castParExpression
    ;

standardType // see: returnType
options { baseContext = type; }
    :   annotationsOpt
        (
            primitiveType
        |
            standardClassOrInterfaceType
        )
        dim0*
    ;

standardClassOrInterfaceType
options { baseContext = referenceType; }
    :   qualifiedStandardClassName typeArguments?
    ;

typeArguments
    :   LT NL* typeArgument (COMMA NL* typeArgument)* NL* GT
    ;

typeArgument
    :   type
    |   annotationsOpt QUESTION ((EXTENDS | SUPER) NL* type)?
    ;

annotatedQualifiedClassName
    :   annotationsOpt qualifiedClassName
    ;

qualifiedClassNameList
    :   annotatedQualifiedClassName (COMMA NL* annotatedQualifiedClassName)*
    ;

formalParameters
    :   LPAREN formalParameterList? RPAREN
    ;

formalParameterList
    :   (formalParameter | thisFormalParameter) (COMMA NL* formalParameter)*
    ;

thisFormalParameter
    :   type THIS
    ;

formalParameter
    :   variableModifiersOpt type? ELLIPSIS? variableDeclaratorId (NL* ASSIGN NL* expression)?
    ;

methodBody
    :   block
    ;

qualifiedName
    :   qualifiedNameElement (DOT qualifiedNameElement)*
    ;

/**
 *  Java doesn't have the keywords 'as', 'in', 'def', 'trait' so we make some allowances
 *  for them in package names for better integration with existing Java packages
 */
qualifiedNameElement
    :   identifier
    |   DEF
    |   IN
    |   AS
    |   TRAIT
    ;

qualifiedNameElements
    :   (qualifiedNameElement DOT)*
    ;

qualifiedClassName
    :   qualifiedNameElements identifier
    ;

qualifiedStandardClassName
    :   qualifiedNameElements className (DOT className)*
    ;

literal
    :   IntegerLiteral                                                                      #integerLiteralAlt
    |   FloatingPointLiteral                                                                #floatingPointLiteralAlt
    |   stringLiteral                                                                       #stringLiteralAlt
    |   BooleanLiteral                                                                      #booleanLiteralAlt
    |   NullLiteral                                                                         #nullLiteralAlt
    ;

// GSTRING

gstring
    :   GStringBegin gstringValue (GStringPart  gstringValue)* GStringEnd
    ;

gstringValue
    :   gstringPath
    |   closure
    ;

gstringPath
    :   identifier GStringPathPart*
    ;


// LAMBDA EXPRESSION
lambdaExpression
options { baseContext = standardLambdaExpression; }
    :   lambdaParameters NL* ARROW NL* lambdaBody
    ;

// JAVA STANDARD LAMBDA EXPRESSION
standardLambdaExpression
    :   standardLambdaParameters NL* ARROW NL* lambdaBody
    ;

lambdaParameters
options { baseContext = standardLambdaParameters; }
    :   formalParameters

    // { a -> a * 2 } can be parsed as a lambda expression in a block, but we expect a closure.
    // So it is better to put parameters in the parentheses and the following single parameter without parentheses is limited
//    |   variableDeclaratorId
    ;

standardLambdaParameters
    :   formalParameters
    |   variableDeclaratorId
    ;

lambdaBody
    :   block
    |   statementExpression
    ;

// CLOSURE
closure
    :   LBRACE (NL* (formalParameterList NL*)? ARROW)? sep? blockStatementsOpt RBRACE
    ;

// GROOVY-8991: Difference in behaviour with closure and lambda
closureOrLambdaExpression
    :   closure
    |   lambdaExpression
    ;

blockStatementsOpt
    :   blockStatements?
    ;

blockStatements
    :   blockStatement (sep blockStatement)* sep?
    ;

// ANNOTATIONS

annotationsOpt
    :   (annotation (NL* annotation)* NL*)?
    ;

annotation
    :   AT annotationName (NL* LPAREN elementValues? RPAREN)?
    ;

elementValues
    :   elementValuePairs
    |   elementValue
    ;

annotationName : qualifiedClassName ;

elementValuePairs
    :   elementValuePair (COMMA elementValuePair)*
    ;

elementValuePair
    :   elementValuePairName NL* ASSIGN NL* elementValue
    ;

elementValuePairName
    :   identifier
    |   keywords
    ;

// TODO verify the potential performance issue because rule expression contains sub-rule assignments(https://github.com/antlr/grammars-v4/issues/215)
elementValue
    :   elementValueArrayInitializer
    |   annotation
    |   expression
    ;

elementValueArrayInitializer
    :   LBRACK (elementValue (COMMA elementValue)* COMMA?)? RBRACK
    |   LBRACE (elementValue COMMA)+ elementValue? RBRACE // avoid ambiguity with closure
    ;

// STATEMENTS / BLOCKS

block
    :   LBRACE sep? blockStatementsOpt RBRACE
    ;

blockStatement
    :   statement
    ;

localVariableDeclaration
    :   { !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) }?
        variableDeclaration[0]
    ;

/**
 *  t   0: local variable declaration; 1: field declaration
 */
variableDeclaration[int t]
    :   modifiers NL*
        (   type? variableDeclarators
        |   typeNamePairs NL* ASSIGN NL* variableInitializer
        )
    |
        type variableDeclarators
    ;

typeNamePairs
    :   LPAREN
        (   typeNamePair (COMMA typeNamePair)*
        |   keyedPair (COMMA keyedPair)*
        )
        RPAREN
    ;

typeNamePair
    :   (DEF | VAL | VAR | type)? MUL? variableDeclaratorId
    ;

keyedPair
    :   key=identifier COLON (DEF | VAL | VAR | type)? variableDeclaratorId
    ;

variableNames
    :   LPAREN variableDeclaratorId (COMMA variableDeclaratorId)+ RPAREN
    ;

conditionalStatement
    :   ifElseStatement
    |   switchStatement
    ;

ifElseStatement
    :   IF expressionInPar NL* tb=statement ((NL* | sep) ELSE NL* fb=statement)?
    ;

switchStatement
    :   SWITCH expressionInPar NL* LBRACE NL* (switchBlockStatementGroup+ NL*)? RBRACE
    ;

loopStatement
    :   annotationsOpt FOR AWAIT? LPAREN forControl RPAREN NL* statement                                      #forStmtAlt
    |   annotationsOpt WHILE expressionInPar NL* statement                                                    #whileStmtAlt
    |   annotationsOpt DO NL* statement NL* WHILE expressionInPar                                             #doWhileStmtAlt
    ;

continueStatement
    :   CONTINUE
        identifier?
    ;

breakStatement
    :   BREAK
        identifier?
    ;

yieldStatement
    :   YIELD
        expression
    ;

tryCatchStatement
    :   TRY resources? NL* block
        (NL* catchClause)*
        (NL* finallyBlock)?
    ;

assertStatement
    :   ASSERT ce=expression (NL* (COLON | COMMA) NL* me=expression)?
    ;

statement
    :   block                                                                                               #blockStmtAlt
    |   conditionalStatement                                                                                #conditionalStmtAlt
    |   loopStatement                                                                                       #loopStmtAlt
    |   tryCatchStatement                                                                                   #tryCatchStmtAlt
    |   SYNCHRONIZED expressionInPar NL* block                                                              #synchronizedStmtAlt
    |   RETURN expression?                                                                                  #returnStmtAlt
    |   THROW expression                                                                                    #throwStmtAlt
    |   breakStatement                                                                                      #breakStmtAlt
    |   continueStatement                                                                                   #continueStmtAlt
    |   { inSwitchExpressionLevel > 0 }?
        yieldStatement                                                                                      #yieldStmtAlt
    |   YIELD RETURN NL* expression                                                                         #yieldReturnStmtAlt
    |   { inAsyncClosureLevel > 0 }?
        DEFER NL* statementExpression                                                                       #deferStmtAlt
    |   identifier COLON NL* statement                                                                      #labeledStmtAlt
    |   assertStatement                                                                                     #assertStmtAlt
    |   localVariableDeclaration                                                                            #localVariableDeclarationStmtAlt
    |   statementExpression                                                                                 #expressionStmtAlt
    |   SEMI                                                                                                #emptyStmtAlt
    ;

catchClause
    :   CATCH LPAREN variableModifiersOpt catchType? identifier RPAREN NL* block
    ;

catchType
    :   qualifiedClassName (BITOR qualifiedClassName)*
    ;

finallyBlock
    :   FINALLY NL* block
    ;

resources
    :   LPAREN NL* resourceList sep? RPAREN
    ;

resourceList
    :   resource (sep resource)*
    ;

resource
    :   localVariableDeclaration
    |   expression
    ;


/** Matches cases then statements, both of which are mandatory.
 *  To handle empty cases at the end, we add switchLabel* to statement.
 */
switchBlockStatementGroup
    :   switchLabel (NL* switchLabel)* NL* blockStatements
    ;

switchLabel
    :   CASE expression COLON
    |   DEFAULT COLON
    ;

forControl
    :   enhancedForControl
    |   originalForControl
    ;

enhancedForControl
    :   (indexVariable COMMA)? variableModifiersOpt type? identifier (COLON | IN) expression
    ;

indexVariable
    :   (BuiltInPrimitiveType | DEF | VAL | VAR)? identifier
    ;

originalForControl
    :   forInit? SEMI expression? SEMI forUpdate?
    ;

forInit
    :   localVariableDeclaration
    |   expressionList[false]
    ;

forUpdate
    :   expressionList[false]
    ;


// EXPRESSIONS

castParExpression
    :   LPAREN intersectionType RPAREN
    ;

intersectionType
    :   type (BITAND NL* type)*
    ;

coercionType
    :   castParExpression                                                                       // (T) or (A & B & ...)
    |   type                                                                                    // T
    ;

parExpression
    :   expressionInPar
    ;

expressionInPar
    :   LPAREN enhancedStatementExpression RPAREN
    ;

expressionList[boolean canSpread]
    :   expressionListElement[$canSpread] (COMMA NL* expressionListElement[$canSpread])*
    ;

expressionListElement[boolean canSpread]
    :   MUL? expression
    ;

enhancedExpression
    :   expression
    |   standardLambdaExpression
    ;

enhancedStatementExpression
    :   statementExpression
    |   standardLambdaExpression
    ;

statementExpression
    :   commandExpression                   #commandExprAlt
    ;

postfixExpression
    :   pathExpression op=(INC | DEC)?
    ;

switchExpression
@init {
    inSwitchExpressionLevel++;
}
@after {
    inSwitchExpressionLevel--;
}
    :   SWITCH expressionInPar NL* LBRACE NL* switchBlockStatementExpressionGroup* NL* RBRACE
    ;

switchBlockStatementExpressionGroup
    :   (switchExpressionLabel NL*)+ blockStatements
    ;

switchExpressionLabel
    :   (   CASE expressionList[true]
        |   DEFAULT
        ) ac=(ARROW | COLON)
    ;

expression
    // must come before postfixExpression to resolve the ambiguities between casting and call on parentheses expression, e.g. (int)(1 / 2)
    :   castParExpression castOperandExpression                                             #castExprAlt

    // async closure/lambda must come before postfixExpression to resolve ambiguity with method call, e.g. async { ... }
    |   ASYNC NL* { inAsyncClosureLevel++; } closureOrLambdaExpression { inAsyncClosureLevel--; }   #asyncClosureExprAlt

    // await expression: single-arg or multi-arg (parenthesized or unparenthesized)
    |   AWAIT NL* ( LPAREN expression (COMMA NL* expression)* RPAREN
                  | expression (COMMA NL* expression)*
                  )                                                                         #awaitExprAlt

    // qualified names, array expressions, method invocation, post inc/dec
    |   postfixExpression                                                                   #postfixExprAlt

    |   switchExpression                                                                    #switchExprAlt

    // ~(BNOT)/!(LNOT) (level 1)
    |   (BITNOT | NOT) NL* expression                                                       #unaryNotExprAlt

    // math power operator (**) (level 2)
    |   left=expression op=POWER NL* right=expression                                       #powerExprAlt

    // ++(prefix)/--(prefix)/+(unary)/-(unary) (level 3)
    |   op=(INC | DEC | ADD | SUB) expression                                               #unaryAddExprAlt

    // multiplication/division/modulo (level 4)
    |   left=expression NL* op=(MUL | DIV | MOD) NL* right=expression                       #multiplicativeExprAlt

    // binary addition/subtraction (level 5)
    |   left=expression op=(ADD | SUB) NL* right=expression                                 #additiveExprAlt

    // bit shift expressions (level 6)
    |   left=expression NL*
            (           (   dlOp=LT LT
                        |   tgOp=GT GT GT
                        |   dgOp=GT GT
                        )
            |   rangeOp=(    RANGE_INCLUSIVE
                        |    RANGE_EXCLUSIVE_LEFT
                        |    RANGE_EXCLUSIVE_RIGHT
                        |    RANGE_EXCLUSIVE_FULL
                        )
            ) NL*
        right=expression                                                                    #shiftExprAlt

    // boolean relational expressions (level 7)
    |   left=expression NL* op=INSTANCEOF NL* matchingType                                  #relationalExprAlt
    |   left=expression NL* op=NOT_INSTANCEOF NL* notInstanceofType                         #relationalExprAlt
    |   left=expression NL* op=AS NL* coercionType                                          #relationalExprAlt
    |   left=expression NL* op=(LE | GE | GT | LT | IN | NOT_IN) NL* right=expression       #relationalExprAlt

    // equality/inequality (==/!=) (level 8)
    |   left=expression NL*
            op=(    IDENTICAL
               |    NOT_IDENTICAL
               |    EQUAL
               |    NOTEQUAL
               |    SPACESHIP
               ) NL*
        right=expression                                                                    #equalityExprAlt

    // regex find and match (=~ and ==~) (level 8.5)
    // jez: moved =~ closer to precedence of == etc, as...
    // 'if (foo =~ "a.c")' is very close in intent to 'if (foo == "abc")'
    |   left=expression NL* op=(REGEX_FIND | REGEX_MATCH) NL* right=expression              #regexExprAlt

    // bitwise or non-short-circuiting and (&)  (level 9)
    |   left=expression NL* op=BITAND NL* right=expression                                  #andExprAlt

    // exclusive or (^)  (level 10)
    |   left=expression NL* op=XOR NL* right=expression                                     #exclusiveOrExprAlt

    // bitwise or non-short-circuiting or (|)  (level 11)
    |   left=expression NL* op=BITOR NL* right=expression                                   #inclusiveOrExprAlt

    // logical and (&&)  (level 12)
    |   left=expression NL* op=AND NL* right=expression                                     #logicalAndExprAlt

    // logical or (||)  (level 13)
    |   left=expression NL* op=OR NL* right=expression                                      #logicalOrExprAlt

    // implication (==>)  (level 13.5)
    |   <assoc=right> left=expression NL* op=IMPLIES NL* right=expression                   #implicationExprAlt

    // conditional test (level 14)
    |   <assoc=right> con=expression NL*
        (   QUESTION NL* tb=expression NL* COLON NL*
        |   ELVIS NL*
        )
        fb=expression                                                                       #conditionalExprAlt

    // assignment expression (level 15)
    // "(a) = [1]" is a special case of multipleAssignmentExprAlt, it will be handle by assignmentExprAlt
    |   <assoc=right> left=variableNames NL* op=ASSIGN NL* right=statementExpression        #multipleAssignmentExprAlt
    |   <assoc=right> left=expression NL*
                        op=(   ASSIGN
                           |   ADD_ASSIGN
                           |   SUB_ASSIGN
                           |   MUL_ASSIGN
                           |   DIV_ASSIGN
                           |   AND_ASSIGN
                           |   OR_ASSIGN
                           |   XOR_ASSIGN
                           |   RSHIFT_ASSIGN
                           |   URSHIFT_ASSIGN
                           |   LSHIFT_ASSIGN
                           |   MOD_ASSIGN
                           |   POWER_ASSIGN
                           |   ELVIS_ASSIGN
                           ) NL*
                     right=enhancedStatementExpression                                      #assignmentExprAlt
    ;

castOperandExpression
options { baseContext = expression; }
    :   castParExpression castOperandExpression                                             #castExprAlt

    |   postfixExpression                                                                   #postfixExprAlt

    // ~(BNOT)/!(LNOT)
    |   (BITNOT | NOT) NL* castOperandExpression                                            #unaryNotExprAlt

    // ++(prefix)/--(prefix)/+(unary)/-(unary)
    |   op=(INC | DEC | ADD | SUB) castOperandExpression                                    #unaryAddExprAlt
    ;

commandExpression
    :   expression
        (
            { !SemanticPredicates.isFollowingArgumentsOrClosure($expression.ctx) }?
            argumentList
        |
            /* if pathExpression is a method call, no need to have any more arguments */
        )

        commandArgument*
    ;

commandArgument
    :   commandPrimary
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
        (   pathElement+
        |   argumentList
        )?
    ;

/**
 *  A "path expression" is a name or other primary, possibly qualified by various
 *  forms of dot, and/or followed by various kinds of brackets.
 *  It can be used for value or assigned to, or else further qualified, indexed, or called.
 *  It is called a "path" because it looks like a linear path through a data structure.
 *  Examples:  x.y, x?.y, x*.y, x.@y; x[], x[y], x[y,z]; x(), x(y), x(y,z); x{s}; a.b[n].c(x).d{s}
 *  (Compare to a C lvalue, or LeftHandSide in the JLS section 15.26.)
 *  General expressions are built up from path expressions, using operators like '+' and '='.
 *
 *  t   0: primary, 1: namePart, 2: arguments, 3: closureOrLambdaExpression, 4: indexPropertyArgs, 5: namedPropertyArgs,
 *      6: non-static inner class creator
 */
pathExpression returns [int t]
    :   (
            primary
        |
            // if 'static' followed by DOT, we can treat them as identifiers, e.g. static.unused = { -> }
            { _input.LT(2).getType() == DOT }?
            STATIC
        ) (pathElement { $t = $pathElement.t; })*
    ;

pathElement returns [int t]
    :   NL*
        (
            DOT NL*
            (   NEW creator[1]
                { $t = 6; }
            |
                // AT: foo.@bar selects the field (or attribute), not property
                (AT | nonWildcardTypeArguments)?
                namePart
                { $t = 1; }
            )
        |
            // Non-DOT member selection operators (still share namePart tail)
            (   SPREAD_DOT          // Spread operator:  x*.y  ===  x?.collect{it.y}
            |   SAFE_DOT            // Optional-null operator:  x?.y  === (x==null)?null:x.y
            |   SAFE_CHAIN_DOT      // Optional-null chain operator:  x??.y.z  === x?.y?.z
            ) NL* (AT | nonWildcardTypeArguments)?
            namePart
            { $t = 1; }
        |
            METHOD_POINTER NL*      // Method pointer operator: foo.&y == foo.metaClass.getMethodPointer(foo, "y")
            namePart
            { $t = 1; }
        |
            METHOD_REFERENCE NL* (nonWildcardTypeArguments)?  // Method reference: System.out::println
            namePart
            { $t = 1; }

            // Can always append a block, as foo{bar}
        |   closureOrLambdaExpression
            { $t = 3; }
        )

    |   arguments
        { $t = 2; }

    // Element selection is always an option, too.
    // In Groovy, the stuff between brackets is a general argument list,
    // since the bracket operator is transformed into a method call.
    |   indexPropertyArgs
        { $t = 4; }

    |   namedPropertyArgs
        { $t = 5; }
    ;

/**
 *  This is the grammar for what can follow a dot:  x.a, x.@a, x.&a, x.'a', etc.
 */
namePart
    :
        (   identifier

        // foo.'bar' is in all ways same as foo.bar, except that bar can have an arbitrary spelling
        |   stringLiteral

        |   dynamicMemberName

        /* just a PROPOSAL, which has not been implemented yet!
        // PROPOSAL, DECIDE:  Is this inline form of the 'with' statement useful?
        // Definition:  a.{foo} === {with(a) {foo}}
        // May cover some path expression use-cases previously handled by dynamic scoping (closure delegates).
        |   block
        */

        // let's allow common keywords as property names
        |   keywords
        )
    ;

/**
 *  If a dot is followed by a parenthesized or quoted expression, the member is computed dynamically,
 *  and the member selection is done only at runtime.  This forces a statically unchecked member access.
 */
dynamicMemberName
    :   parExpression
    |   gstring
    ;

/** An expression may be followed by [...].
 *  Unlike Java, these brackets may contain a general argument list,
 *  which is passed to the array element operator, which can make of it what it wants.
 *  The brackets may also be empty, as in T[].  This is how Groovy names array types.
 */
indexPropertyArgs
    :   (SAFE_INDEX | LBRACK) expressionList[true]? RBRACK
    ;

namedPropertyArgs
    :   (SAFE_INDEX | LBRACK) (namedPropertyArgList | COLON) RBRACK
    ;

primary
    :
        // Append `typeArguments?` to `identifier` to support constructor reference with generics, e.g. HashMap<String, Integer>::new
        // Though this is not a graceful solution, it is much faster than replacing `builtInType` with `type`
        identifier typeArguments?                                                           #identifierPrmrAlt
    |   literal                                                                             #literalPrmrAlt
    |   gstring                                                                             #gstringPrmrAlt
    |   NEW NL* creator[0]                                                                  #newPrmrAlt
    |   THIS                                                                                #thisPrmrAlt
    |   SUPER                                                                               #superPrmrAlt
    |   parExpression                                                                       #parenPrmrAlt
    |   closureOrLambdaExpression                                                           #closureOrLambdaExpressionPrmrAlt
    |   list                                                                                #listPrmrAlt
    |   map                                                                                 #mapPrmrAlt
    |   builtInType                                                                         #builtInTypePrmrAlt
    ;

namedPropertyArgPrimary
options { baseContext = primary; }
    :   identifier                                                                          #identifierPrmrAlt
    |   literal                                                                             #literalPrmrAlt
    |   gstring                                                                             #gstringPrmrAlt
    |   parExpression                                                                       #parenPrmrAlt
    |   list                                                                                #listPrmrAlt
    |   map                                                                                 #mapPrmrAlt
    ;

namedArgPrimary
options { baseContext = primary; }
    :   identifier                                                                          #identifierPrmrAlt
    |   literal                                                                             #literalPrmrAlt
    |   gstring                                                                             #gstringPrmrAlt
    ;

commandPrimary
options { baseContext = primary; }
    :   identifier                                                                          #identifierPrmrAlt
    |   literal                                                                             #literalPrmrAlt
    |   gstring                                                                             #gstringPrmrAlt
    ;

list
    :   LBRACK expressionList[true]? COMMA? RBRACK
    ;

map
    :   LBRACK
        (   mapEntryList COMMA?
        |   COLON
        )
        RBRACK
    ;

mapEntryList
    :   mapEntry (COMMA mapEntry)*
    ;

namedPropertyArgList
options { baseContext = mapEntryList; }
    :   namedPropertyArg (COMMA namedPropertyArg)*
    ;

mapEntry
    :   mapEntryLabel COLON NL* enhancedExpression
    |   MUL COLON NL* enhancedExpression
    ;

namedPropertyArg
options { baseContext = mapEntry; }
    :   namedPropertyArgLabel COLON NL* enhancedExpression
    |   MUL COLON NL* enhancedExpression
    ;

namedArg
options { baseContext = mapEntry; }
    :   namedArgLabel COLON NL* enhancedExpression
    |   MUL COLON NL* enhancedExpression
    ;

mapEntryLabel
    :   keywords
    |   primary
    ;

namedPropertyArgLabel
options { baseContext = mapEntryLabel; }
    :   keywords
    |   namedPropertyArgPrimary
    ;

namedArgLabel
options { baseContext = mapEntryLabel; }
    :   keywords
    |   namedArgPrimary
    ;

/**
 *  t 0: general creation; 1: non-static inner class creation
 */
creator[int t]
    :   createdName
        (   NL* arguments anonymousInnerClassDeclaration[0]?
        |   dim0+ NL* arrayInitializer
        |   dim1+ dim0*
        )
    ;

dim0
    :   annotationsOpt LBRACK RBRACK
    ;

dim1
    :   annotationsOpt LBRACK expression RBRACK
    ;

arrayInitializer
    :   LBRACE NL* (
            (arrayInitializer | variableInitializer) NL*
          (COMMA NL*
            (arrayInitializer | variableInitializer) NL*
          )*
        )? COMMA? NL* RBRACE
    ;

/**
 *  t   0: anonymous inner class; 1: anonymous enum
 */
anonymousInnerClassDeclaration[int t]
    :   classBody[0]
    ;

createdName
    :   annotationsOpt
        (   primitiveType
        |   qualifiedClassName typeArgumentsOrDiamond?
        )
    ;

nonWildcardTypeArguments
    :   LT NL* typeList NL* GT
    ;

typeArgumentsOrDiamond
    :   LT GT
    |   typeArguments
    ;

arguments
    :   LPAREN enhancedArgumentListInPar? COMMA? RPAREN
    ;

argumentList
options { baseContext = enhancedArgumentListInPar; }
    :   firstArgumentListElement
        (   COMMA NL*
            argumentListElement
        )*
    ;

enhancedArgumentListInPar
    :   enhancedArgumentListElement
        (   COMMA NL*
            enhancedArgumentListElement
        )*
    ;

firstArgumentListElement
options { baseContext = enhancedArgumentListElement; }
    :   expressionListElement[true]
    |   namedArg
    ;

argumentListElement
options { baseContext = enhancedArgumentListElement; }
    :   expressionListElement[true]
    |   namedPropertyArg
    ;

enhancedArgumentListElement
    :   expressionListElement[true]
    |   standardLambdaExpression
    |   namedPropertyArg
    ;

stringLiteral
    :   StringLiteral
    ;

className
    :   CapitalizedIdentifier
    ;

identifier
    :   Identifier
    |   CapitalizedIdentifier
    |   AS
    |   ASYNC
    |   AWAIT
    |   DEFER
    |   IN
    |   MODULE
    |   PERMITS
    |   RECORD
    |   SEALED
    |   TRAIT
    |   VAL
    |   VAR
    |   YIELD
    ;

builtInType
    :   BuiltInPrimitiveType
    |   VOID
    ;

keywords
    :   ABSTRACT
    |   AS
    |   ASSERT
    |   ASYNC
    |   AWAIT
    |   BREAK
    |   CASE
    |   CATCH
    |   CLASS
    |   CONST
    |   CONTINUE
    |   DEF
    |   DEFAULT
    |   DEFER
    |   DO
    |   ELSE
    |   ENUM
    |   EXTENDS
    |   FINAL
    |   FINALLY
    |   FOR
    |   GOTO
    |   IF
    |   IMPLEMENTS
    |   IMPORT
    |   IN
    |   INSTANCEOF
    |   INTERFACE
    |   NATIVE
    |   NEW
    |   NON_SEALED
    |   PACKAGE
    |   PERMITS
    |   RECORD
    |   RETURN
    |   SEALED
    |   STATIC
    |   STRICTFP
    |   SUPER
    |   SWITCH
    |   SYNCHRONIZED
    |   THIS
    |   THROW
    |   THROWS
    |   TRANSIENT
    |   TRAIT
    |   THREADSAFE
    |   TRY
    |   VAL
    |   VAR
    |   VOLATILE
    |   WHILE
    |   YIELD

    |   NullLiteral
    |   BooleanLiteral

    |   BuiltInPrimitiveType
    |   VOID

    |   PUBLIC
    |   PROTECTED
    |   PRIVATE
    ;

sep :   (NL | SEMI)+
    ;
