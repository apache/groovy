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
    :   nls (packageDeclaration sep?)? scriptStatements? EOF
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
    :   annotationsOpt IMPORT STATIC? qualifiedName (DOT MUL | AS alias=identifier)?
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
          |   VAR
          )
    ;

modifiersOpt
    :   (modifiers nls)?
    ;

modifiers
    :   modifier (nls modifier)*
    ;

classOrInterfaceModifiersOpt
    :   (classOrInterfaceModifiers
            NL* /* Use `NL*` here for better performance, so DON'T replace it with `nls` */
        )?
    ;

classOrInterfaceModifiers
    :   classOrInterfaceModifier (nls classOrInterfaceModifier)*
    ;

classOrInterfaceModifier
    :   annotation       // class or interface
    |   m=(   PUBLIC     // class or interface
          |   PROTECTED  // class or interface
          |   PRIVATE    // class or interface
          |   STATIC     // class or interface
          |   ABSTRACT   // class or interface
          |   FINAL      // class only -- does not apply to interfaces
          |   STRICTFP   // class or interface
          |   DEFAULT    // interface only -- does not apply to classes
          )
    ;

variableModifier
    :   annotation
    |   m=( FINAL
          | DEF
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
    :   (variableModifiers nls)?
    ;

variableModifiers
    :   variableModifier (nls variableModifier)*
    ;

typeParameters
    :   LT nls typeParameter (COMMA nls typeParameter)* nls GT
    ;

typeParameter
    :   className (EXTENDS nls typeBound)?
    ;

typeBound
    :   type (BITAND nls type)*
    ;

typeList
    :   type (COMMA nls type)*
    ;


/**
 *  t   0: class; 1: interface; 2: enum; 3: annotation; 4: trait
 */
classDeclaration
locals[ int t ]
    :   (   CLASS { $t = 0; }
        |   INTERFACE { $t = 1; }
        |   ENUM { $t = 2; }
        |   AT INTERFACE { $t = 3; }
        |   TRAIT { $t = 4; }
        )
        identifier
        (nls typeParameters)?
        (nls EXTENDS nls scs=typeList)?
        (nls IMPLEMENTS nls is=typeList)?
        nls classBody[$t]
    ;

// t    see the comment of classDeclaration
classBody[int t]
    :   LBRACE nls
        (
            /* Only enum can have enum constants */
            { 2 == $t }?
            enumConstants (nls COMMA)? sep?
        |
        )
        (classBodyDeclaration[$t] (sep classBodyDeclaration[$t])*)? sep? RBRACE
    ;

enumConstants
    :   enumConstant (nls COMMA nls enumConstant)*
    ;

enumConstant
    :   annotationsOpt identifier arguments? anonymousInnerClassDeclaration[1]?
    ;

classBodyDeclaration[int t]
    :   (STATIC nls)? block
    |   memberDeclaration[$t]
    ;

memberDeclaration[int t]
    :   methodDeclaration[0, $t]
    |   fieldDeclaration
    |   modifiersOpt classDeclaration
    ;

/**
 *  t   0: *class member* all kinds of method declaration AND constructor declaration,
 *      1: normal method declaration, 2: abstract method declaration
 *      3: normal method declaration OR abstract method declaration
 *  ct  9: script, other see the comment of classDeclaration
 */
methodDeclaration[int t, int ct]
    :   modifiersOpt
        (   { 3 == $ct }?
            returnType[$ct] methodName LPAREN rparen (DEFAULT nls elementValue)?
        |
            typeParameters? returnType[$ct]?
            methodName formalParameters (nls THROWS nls qualifiedClassNameList)?
            (nls methodBody)?
        )
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
    :   variableDeclarator (COMMA nls variableDeclarator)*
    ;

variableDeclarator
    :   variableDeclaratorId (nls ASSIGN nls variableInitializer)?
    ;

variableDeclaratorId
    :   identifier
    ;

variableInitializer
    :   enhancedStatementExpression
    ;

variableInitializers
    :   variableInitializer (nls COMMA nls variableInitializer)* nls COMMA?
    ;

emptyDims
    :   (annotationsOpt LBRACK RBRACK)+
    ;

emptyDimsOpt
    :   emptyDims?
    ;

standardType
options { baseContext = type; }
    :   annotationsOpt
        (
            primitiveType
        |
            standardClassOrInterfaceType
        )
        emptyDimsOpt
    ;

type
    :   annotationsOpt
        (
            (
                primitiveType
            |
                // !!! Error Alternative !!!
                 VOID
            )
        |
                generalClassOrInterfaceType
        )
        emptyDimsOpt
    ;

classOrInterfaceType
    :   (   qualifiedClassName
        |   qualifiedStandardClassName
        ) typeArguments?
    ;

generalClassOrInterfaceType
options { baseContext = classOrInterfaceType; }
    :   qualifiedClassName typeArguments?
    ;

standardClassOrInterfaceType
options { baseContext = classOrInterfaceType; }
    :   qualifiedStandardClassName typeArguments?
    ;

primitiveType
    :   BuiltInPrimitiveType
    ;

typeArguments
    :   LT nls typeArgument (COMMA nls typeArgument)* nls GT
    ;

typeArgument
    :   type
    |   annotationsOpt QUESTION ((EXTENDS | SUPER) nls type)?
    ;

annotatedQualifiedClassName
    :   annotationsOpt qualifiedClassName
    ;

qualifiedClassNameList
    :   annotatedQualifiedClassName (COMMA nls annotatedQualifiedClassName)*
    ;

formalParameters
    :   LPAREN formalParameterList? rparen
    ;

formalParameterList
    :   (formalParameter | thisFormalParameter) (COMMA nls formalParameter)*
    ;

thisFormalParameter
    :   type THIS
    ;

formalParameter
    :   variableModifiersOpt type? ELLIPSIS? variableDeclaratorId (nls ASSIGN nls expression)?
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
	:	lambdaParameters nls ARROW nls lambdaBody
	;

// JAVA STANDARD LAMBDA EXPRESSION
standardLambdaExpression
	:	standardLambdaParameters nls ARROW nls lambdaBody
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
	:	block
	|	statementExpression
	;

// CLOSURE
closure
    :   LBRACE (nls (formalParameterList nls)? ARROW)? sep? blockStatementsOpt RBRACE
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
    :   (annotation (nls annotation)* nls)?
    ;

annotation
    :   AT annotationName (nls LPAREN elementValues? rparen)?
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
    :   elementValuePairName nls ASSIGN nls elementValue
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
    ;

// STATEMENTS / BLOCKS

block
    :   LBRACE sep? blockStatementsOpt RBRACE
    ;

blockStatement
    :   localVariableDeclaration
    |   statement
    ;

localVariableDeclaration
    :   { !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) }?
        variableDeclaration[0]
    ;

/**
 *  t   0: local variable declaration; 1: field declaration
 */
variableDeclaration[int t]
    :   modifiers nls
        (   type? variableDeclarators
        |   typeNamePairs nls ASSIGN nls variableInitializer
        )
    |
        type variableDeclarators
    ;

typeNamePairs
    :   LPAREN typeNamePair (COMMA typeNamePair)* rparen
    ;

typeNamePair
    :   type? variableDeclaratorId
    ;

variableNames
    :   LPAREN variableDeclaratorId (COMMA variableDeclaratorId)+ rparen
    ;

conditionalStatement
    :   ifElseStatement
    |   switchStatement
    ;

ifElseStatement
    :   IF expressionInPar nls tb=statement ((nls | sep) ELSE nls fb=statement)?
    ;

switchStatement
    :   SWITCH expressionInPar nls LBRACE nls (switchBlockStatementGroup+ nls)? RBRACE
    ;

loopStatement
    :   FOR LPAREN forControl rparen nls statement                                                            #forStmtAlt
    |   WHILE expressionInPar nls statement                                                                   #whileStmtAlt
    |   DO nls statement nls WHILE expressionInPar                                                            #doWhileStmtAlt
    ;

continueStatement
    :   CONTINUE
        identifier?
    ;

breakStatement
    :   BREAK
        identifier?
    ;

tryCatchStatement
    :   TRY resources? nls block
        (nls catchClause)*
        (nls finallyBlock)?
    ;

assertStatement
    :   ASSERT ce=expression (nls (COLON | COMMA) nls me=expression)?
    ;

statement
    :   block                                                                                               #blockStmtAlt
    |   conditionalStatement                                                                                #conditionalStmtAlt
    |   loopStatement                                                                                       #loopStmtAlt
    |   tryCatchStatement                                                                                   #tryCatchStmtAlt
    |   SYNCHRONIZED expressionInPar nls block                                                              #synchronizedStmtAlt
    |   RETURN expression?                                                                                  #returnStmtAlt
    |   THROW expression                                                                                    #throwStmtAlt
    |   breakStatement                                                                                      #breakStmtAlt
    |   continueStatement                                                                                   #continueStmtAlt
    |   identifier COLON nls statement                                                                      #labeledStmtAlt
    |   assertStatement                                                                                     #assertStmtAlt
    |   localVariableDeclaration                                                                            #localVariableDeclarationStmtAlt
    |   statementExpression                                                                                 #expressionStmtAlt
    |   SEMI                                                                                                #emptyStmtAlt
    ;

catchClause
    :   CATCH LPAREN variableModifiersOpt catchType? identifier rparen nls block
    ;

catchType
    :   qualifiedClassName (BITOR qualifiedClassName)*
    ;

finallyBlock
    :   FINALLY nls block
    ;

resources
    :   LPAREN nls resourceList sep? rparen
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
    :   switchLabel (nls switchLabel)* nls blockStatements
    ;

switchLabel
    :   CASE expression COLON
    |   DEFAULT COLON
    ;

forControl
    :   enhancedForControl
    |   classicalForControl
    ;

enhancedForControl
    :   variableModifiersOpt type? variableDeclaratorId (COLON | IN) expression
    ;

classicalForControl
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
    :   LPAREN type rparen
    ;

parExpression
    :   expressionInPar
    ;

expressionInPar
    :   LPAREN enhancedStatementExpression rparen
    ;

expressionList[boolean canSpread]
    :   expressionListElement[$canSpread] (COMMA expressionListElement[$canSpread])*
    ;

expressionListElement[boolean canSpread]
    :   MUL? expression
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

expression
    // qualified names, array expressions, method invocation, post inc/dec, type casting (level 1)
    // The cast expression must be put before pathExpression to resovle the ambiguities between type casting and call on parentheses expression, e.g. (int)(1 / 2)
    :   castParExpression castOperandExpression                                             #castExprAlt
    |   postfixExpression                                                                   #postfixExprAlt

    // ~(BNOT)/!(LNOT) (level 1)
    |   (BITNOT | NOT) nls expression                                                       #unaryNotExprAlt

    // math power operator (**) (level 2)
    |   left=expression op=POWER nls right=expression                                       #powerExprAlt

    // ++(prefix)/--(prefix)/+(unary)/-(unary) (level 3)
    |   op=(INC | DEC | ADD | SUB) expression                                               #unaryAddExprAlt

    // multiplication/division/modulo (level 4)
    |   left=expression nls op=(MUL | DIV | MOD) nls right=expression                       #multiplicativeExprAlt

    // binary addition/subtraction (level 5)
    |   left=expression op=(ADD | SUB) nls right=expression                                 #additiveExprAlt

    // bit shift expressions (level 6)
    |   left=expression nls
            (           (   dlOp=LT LT
                        |   tgOp=GT GT GT
                        |   dgOp=GT GT
                        )
            |   rangeOp=(    RANGE_INCLUSIVE
                        |    RANGE_EXCLUSIVE
                        )
            ) nls
        right=expression                                                                    #shiftExprAlt

    // boolean relational expressions (level 7)
    |   left=expression nls op=(AS | INSTANCEOF | NOT_INSTANCEOF) nls type                  #relationalExprAlt
    |   left=expression nls op=(LE | GE | GT | LT | IN | NOT_IN)  nls right=expression      #relationalExprAlt

    // equality/inequality (==/!=) (level 8)
    |   left=expression nls
            op=(    IDENTICAL
               |    NOT_IDENTICAL
               |    EQUAL
               |    NOTEQUAL
               |    SPACESHIP
               ) nls
        right=expression                                                                    #equalityExprAlt

    // regex find and match (=~ and ==~) (level 8.5)
    // jez: moved =~ closer to precedence of == etc, as...
    // 'if (foo =~ "a.c")' is very close in intent to 'if (foo == "abc")'
    |   left=expression nls op=(REGEX_FIND | REGEX_MATCH) nls right=expression              #regexExprAlt

    // bitwise or non-short-circuiting and (&)  (level 9)
    |   left=expression nls op=BITAND nls right=expression                                  #andExprAlt

    // exclusive or (^)  (level 10)
    |   left=expression nls op=XOR nls right=expression                                     #exclusiveOrExprAlt

    // bitwise or non-short-circuiting or (|)  (level 11)
    |   left=expression nls op=BITOR nls right=expression                                   #inclusiveOrExprAlt

    // logical and (&&)  (level 12)
    |   left=expression nls op=AND nls right=expression                                     #logicalAndExprAlt

    // logical or (||)  (level 13)
    |   left=expression nls op=OR nls right=expression                                      #logicalOrExprAlt

    // conditional test (level 14)
    |   <assoc=right> con=expression nls
        (   QUESTION nls tb=expression nls COLON nls
        |   ELVIS nls
        )
        fb=expression                                                                       #conditionalExprAlt

    // assignment expression (level 15)
    // "(a) = [1]" is a special case of multipleAssignmentExprAlt, it will be handle by assignmentExprAlt
    |   <assoc=right> left=variableNames nls op=ASSIGN nls right=statementExpression        #multipleAssignmentExprAlt
    |   <assoc=right> left=expression nls
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
                           ) nls
                     enhancedStatementExpression                                            #assignmentExprAlt
    ;


castOperandExpression
options { baseContext = expression; }
    :   castParExpression castOperandExpression                                             #castExprAlt
    |   postfixExpression                                                                   #postfixExprAlt

    // ~(BNOT)/!(LNOT) (level 1)
    |   (BITNOT | NOT) nls castOperandExpression                                            #unaryNotExprAlt

    // ++(prefix)/--(prefix)/+(unary)/-(unary) (level 3)
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
    :   primary (pathElement { $t = $pathElement.t; })*
    ;

pathElement returns [int t]
    :   nls
        (
            // AT: foo.@bar selects the field (or attribute), not property
            (
                (   DOT                 // The all-powerful dot.
                |   SPREAD_DOT          // Spread operator:  x*.y  ===  x?.collect{it.y}
                |   SAFE_DOT            // Optional-null operator:  x?.y  === (x==null)?null:x.y
                |   SAFE_CHAIN_DOT      // Optional-null chain operator:  x??.y.z  === x?.y?.z
                ) nls (AT | nonWildcardTypeArguments)?
            |
                METHOD_POINTER nls      // Method pointer operator: foo.&y == foo.metaClass.getMethodPointer(foo, "y")
            |
                METHOD_REFERENCE nls    // Method reference: System.out::println
            )
            namePart
            { $t = 1; }
        |
            DOT nls NEW creator[1]
            { $t = 6; }

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
    :   QUESTION? LBRACK expressionList[true]? RBRACK
    ;

namedPropertyArgs
    :   QUESTION? LBRACK (namedPropertyArgList | COLON) RBRACK
    ;

primary
    :
        // Append `typeArguments?` to `identifier` to support constructor reference with generics, e.g. HashMap<String, Integer>::new
        // Though this is not a graceful solution, it is much faster than replacing `builtInType` with `type`
        identifier typeArguments?                                                           #identifierPrmrAlt
    |   literal                                                                             #literalPrmrAlt
    |   gstring                                                                             #gstringPrmrAlt
    |   NEW nls creator[0]                                                                  #newPrmrAlt
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
    :   mapEntryLabel COLON nls expression
    |   MUL COLON nls expression
    ;

namedPropertyArg
options { baseContext = mapEntry; }
    :   namedPropertyArgLabel COLON nls expression
    |   MUL COLON nls expression
    ;

namedArg
options { baseContext = mapEntry; }
    :   namedArgLabel COLON nls expression
    |   MUL COLON nls expression
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
        (   nls arguments anonymousInnerClassDeclaration[0]?
        |   dim+ (nls arrayInitializer)?
        )
    ;

dim
    :   annotationsOpt LBRACK expression? RBRACK
    ;

arrayInitializer
    :   LBRACE nls (variableInitializers nls)? RBRACE
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
    :   LT nls typeList nls GT
    ;

typeArgumentsOrDiamond
    :   LT GT
    |   typeArguments
    ;

arguments
    :   LPAREN enhancedArgumentListInPar? COMMA? rparen
    ;

argumentList
options { baseContext = enhancedArgumentListInPar; }
    :   firstArgumentListElement
        (   COMMA nls
            argumentListElement
        )*
    ;

enhancedArgumentList
options { baseContext = enhancedArgumentListInPar; }
    :   firstEnhancedArgumentListElement
        (   COMMA nls
            enhancedArgumentListElement
        )*
    ;

enhancedArgumentListInPar
    :   enhancedArgumentListElement
        (   COMMA nls
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

firstEnhancedArgumentListElement
options { baseContext = enhancedArgumentListElement; }
    :   expressionListElement[true]
    |   standardLambdaExpression
    |   namedArg
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
    |   VAR
    |   IN
//    |   DEF
    |   TRAIT
    |   AS
    |
        // if 'static' followed by DOT, we can treat them as identifiers, e.g. static.unused = { -> }
        { DOT == _input.LT(2).getType() }?
        STATIC
    ;

builtInType
    :   BuiltInPrimitiveType
    |   VOID
    ;

keywords
    :   ABSTRACT
    |   AS
    |   ASSERT
    |   BREAK
    |   CASE
    |   CATCH
    |   CLASS
    |   CONST
    |   CONTINUE
    |   DEF
    |   DEFAULT
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
    |   PACKAGE
    |   RETURN
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
    |   VAR
    |   VOLATILE
    |   WHILE

    |   NullLiteral
    |   BooleanLiteral

    |   BuiltInPrimitiveType
    |   VOID

    |   PUBLIC
    |   PROTECTED
    |   PRIVATE
    ;

rparen
    :   RPAREN
    ;

nls
    :   NL*
    ;

sep :   (NL | SEMI)+
    ;
