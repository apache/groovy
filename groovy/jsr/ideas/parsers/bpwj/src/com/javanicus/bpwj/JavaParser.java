/*
  $Id$

   Copyright (c) 2004 Jeremy Rayner. All Rights Reserved.

   Jeremy Rayner makes no representations or warranties about
   the fitness of this software for any particular purpose,
   including the implied warranty of merchantability.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.javanicus.bpwj;

import sjm.parse.*;
import sjm.parse.tokens.*;

/**
 * This class provides a Java parser as specified in JLSv3.
 *
 * @author Jeremy Rayner
 *
 */
class JavaParser {

    public Parser start() {
        return compilationUnit();
    }

    // -----  utility methods  ---------
    protected void log(String s) {
        //System.out.println(s);
    }


    public Parser zeroOrOne(Parser parser1) {
        return new Alternation()
                .add(new Empty())
                .add(parser1)
            ;
    }

    public Parser oneOrMore(Parser parser1) {
        return new TrackSequence("one or more " + parser1.getName())
            .add(parser1)
            .add(new Repetition(parser1));
    }


    // ------------------ Main Grammar -------------------------

    /** Identifier:
     *      IDENTIFIER
     */
    public Parser identifier() {
        log("identifier");
        //@todo
        Parser p = new Word();
        return p;
    }

    /** QualifiedIdentifier:
     *      Identifier {. Identifier }
     */
    public Parser qualifiedIdentifier() {
        log("qualifierIdentifier");
        if (qualifiedIdentifier != null) return qualifiedIdentifier;
        qualifiedIdentifier = new TrackSequence("<qualifiedIdentifier>");
        qualifiedIdentifier.add(identifier());
        qualifiedIdentifier.add(new Repetition(
                    new TrackSequence("<qualifiedIdentifier$1>")
                        .add(new Symbol('.'))
                        .add(identifier())
            ));
        return qualifiedIdentifier;
    }

    /** Literal:
     *      IntegerLiteral
     *      FloatingPointLiteral
     *      CharacterLiteral
     *      StringLiteral
     *      BooleanLiteral
     *      NullLiteral
     */
    public Parser literal() {
        log("literal");
        if (literal != null) return literal;
        literal = new Alternation();
        literal.add(integerLiteral());
        literal.add(floatingPointLiteral());
        literal.add(characterLiteral());
        literal.add(stringLiteral());
        literal.add(booleanLiteral());
        literal.add(nullLiteral());
        return literal;
    }

    /** Expression:
     *      Expression1 [AssignmentOperator Expression1]
     */
    public Parser expression() {
        log("expression");
        if (expression != null) return expression;
        expression = new TrackSequence("<expression>");
        expression.add(expression1());
        expression.add(zeroOrOne(
                new TrackSequence("<expression$1>")
                    .add(assignmentOperator())
                    .add(expression1())
            ));
        return expression;
    }


    /** AssignmentOperator:
     *      =
     *      +=
     *      -=
     *      *=
     *      /=
     *      &=
     *      |=
     *      ^=
     *      %=
     *      <<=
     *      >>=
     *      >>>=
     */
    public Parser assignmentOperator() {
        log("assignmentOperator");
        if (assignmentOperator != null) return assignmentOperator;
        assignmentOperator = new Alternation();
        assignmentOperator.add(new Symbol("="));
        assignmentOperator.add(new Symbol("+="));
        assignmentOperator.add(new Symbol("-="));
        assignmentOperator.add(new Symbol("*="));
        assignmentOperator.add(new Symbol("/="));
        assignmentOperator.add(new Symbol("&="));
        assignmentOperator.add(new Symbol("|="));
        assignmentOperator.add(new Symbol("^="));
        assignmentOperator.add(new Symbol("%="));
        assignmentOperator.add(new Symbol("<<="));
        assignmentOperator.add(new Symbol(">>="));
        assignmentOperator.add(new Symbol(">>>="));
        return assignmentOperator;
    }

    /** Type:
     *      Identifier [TypeArguments]{ .   Identifier [TypeArguments]} BracketsOpt
     *      BasicType
     */
    public Parser type() {
        log("type");
        if (type != null) return type;
        type = new Alternation();
        type.add(new TrackSequence("<type$1>")
                .add(identifier())
                .add(zeroOrOne(typeArguments()))
                .add(new Repetition(
                    new TrackSequence("<type$2>")
                        .add(new Symbol("."))
                        .add(identifier())
                        .add(zeroOrOne(typeArguments()))
                ))
                .add(bracketsOpt())
            );
        type.add(basicType());
        return type;
    }

    /** TypeArguments:
     *      < TypeArgument {, TypeArgument}>
     */
    public Parser typeArguments() {
        log("typeArguments");
        if (typeArguments != null) return typeArguments;
        typeArguments = new TrackSequence("<typeArguments>");
        typeArguments.add(new Symbol("<"));
        typeArguments.add(typeArgument());
        typeArguments.add(new Repetition(
                new TrackSequence("<typeArguments$1>")
                    .add(new Symbol(","))
                    .add(typeArgument())
            ));
        return typeArguments;
    }

    /** TypeArgument:
     *      Type
     *      ? [(extends |super ) Type]
     */
    public Parser typeArgument() {
        log("typeArgument");
        if (typeArgument != null) return typeArgument;
        typeArgument = new Alternation();
        typeArgument.add(type());
        typeArgument.add(new TrackSequence("<typeArgument$1>")
                .add(new Symbol("?"))
                .add(zeroOrOne(
                    new TrackSequence("<typeArgument$2>")
                        .add(new Alternation()
                            .add(new Literal("extends"))
                            .add(new Literal("super"))
                        )
                        .add(type())
                ))
            )
        ;
        return typeArgument;
    }

    /** RawType:
     *      Identifier { .   Identifier } BracketsOpt
     */
    public Parser rawType() {
        log("rawType");
        if (rawType != null) return rawType;
        rawType = new TrackSequence("<rawType>");
        rawType.add(identifier());
        rawType.add(new Repetition(
                new TrackSequence("<rawType$1>")
                    .add(new Symbol("."))
                    .add(identifier())
            ));
        rawType.add(bracketsOpt());
        return rawType;
    }

    /** StatementExpression:
     *      Expression
     */
    public Parser statementExpression() {
        log("statementExpression");
        if (statementExpression != null) return statementExpression;
        statementExpression = expression();
        return statementExpression;
    }

    /** ConstantExpression:
     *      Expression
     */
    public Parser constantExpression() {
        log("constantExpression");
        if (constantExpression != null) return constantExpression;
        constantExpression = expression();
        return constantExpression;
    }

    /** Expression1:
     *      Expression2 [Expression1Rest]
     */
    public Parser expression1() {
        log("expression1");
        if (expression1 != null) return expression1;
        expression1 = new TrackSequence("<expression1>");
        expression1.add(expression2());
        expression1.add(zeroOrOne(expression1Rest()));
        return expression1;
    }

    /** Expression1Rest:
     *      [ ?   Expression :   Expression1]
     */
    public Parser expression1Rest() {
        log("expression1Rest");
        if (expression1Rest != null) return expression1Rest;
        expression1Rest = zeroOrOne(
            new TrackSequence("<expression1Rest$1>")
                .add(new Symbol("?"))
                .add(expression())
                .add(new Symbol(":"))
                .add(expression1())
        );
        return expression1Rest;
    }

    /** Expression2 :
     *      Expression3 [Expression2Rest]
     */
    public Parser expression2() {
        log("expression2");
        if (expression2 != null) return expression2;
        expression2 = new TrackSequence("<expression2>");
        expression2.add(expression3());
        expression2.add(zeroOrOne(expression2Rest()));
        return expression2;
    }

    /** Expression2Rest:
     *      {Infixop Expression3}
     *      Expression3 instanceof Type
     */
    public Parser expression2Rest() {
        log("expression2Rest");
        if (expression2Rest != null) return expression2Rest;
        expression2Rest = new Alternation();
        expression2Rest.add(new Repetition(
                new TrackSequence("<expression2Rest$1>")
                    .add(infixOp())
                    .add(expression3())
            ));
        expression2Rest.add(new TrackSequence("<expression2Rest$2>")
                .add(expression3())
                .add(new Literal("instanceof"))
                .add(type())
            );
        return expression2Rest;
    }

    /** Infixop:
     *      ||
     *      &&
     *      |
     *      ^
     *      &
     *      ==
     *      !=
     *      <
     *      >
     *      <=
     *      >=
     *      <<
     *      >>
     *      >>>
     *      +
     *      *
     *      /
     *      %
     */
    public Parser infixOp() {
        log("infixOp");
        if (infixOp != null) return infixOp;
        infixOp = new Alternation();
        infixOp.add(new Symbol("||"));
        infixOp.add(new Symbol("&&"));
        infixOp.add(new Symbol("|"));
        infixOp.add(new Symbol("^"));
        infixOp.add(new Symbol("&"));
        infixOp.add(new Symbol("=="));
        infixOp.add(new Symbol("!="));
        infixOp.add(new Symbol("<"));
        infixOp.add(new Symbol(">"));
        infixOp.add(new Symbol("<="));
        infixOp.add(new Symbol(">="));
        infixOp.add(new Symbol("<<"));
        infixOp.add(new Symbol(">>"));
        infixOp.add(new Symbol(">>>"));
        infixOp.add(new Symbol("+"));
        infixOp.add(new Symbol("*"));
        infixOp.add(new Symbol("/"));
        infixOp.add(new Symbol("%"));
        return infixOp;
    }

    /** Expression3:
     *      PrefixOp Expression3
     *      (   (Expr|Type) )   Expression3
     *      Primary {Selector} {PostfixOp}
     */
    public Parser expression3() {
        log("expression3");
        if (expression3 != null) return expression3;
        expression3 = new Alternation();
        expression3.add(new TrackSequence("<expression3$1>")
                .add(prefixOp())
                .add(expression3())
            );
        expression3.add(new TrackSequence("<expression3$2>")
                .add(new Symbol("("))
                .add(new Alternation()
                    .add(expr())
                    .add(type())
                )
                .add(new Symbol(")"))
                .add(expression3())
            );
        expression3.add(new TrackSequence("<expression3$3>")
                .add(primary())
                .add(new Repetition(selector()))
                .add(new Repetition(postfixOp()))
            );
        return expression3;
    }

    /** Primary:
     *      ( Expression)
     *      NonWildcardTypeArguments (ExplicitGenericInvocationSuffix |this Arguments)
     *      this [Arguments]
     *      super SuperSuffix
     *      Literal
     *      new Creator
     *      Identifier {. Identifier }[ IdentifierSuffix]
     *      BasicType BracketsOpt.class
     *      void.class
     */
    public Parser primary() {
        log("primary");
        if (primary != null) return primary;
        primary = new Alternation();
            // ( Expression)
            primary.add(new TrackSequence("<primary$1>")
                .add(new Symbol("("))
                .add(expression())
                .add(new Symbol(")"))
            );
            // NonWildcardTypeArguments (ExplicitGenericInvocationSuffix |this Arguments)
            primary.add(new TrackSequence("<primary$2>")
                .add(nonWildcardTypeArguments())
                .add(new Alternation()
                    .add(explicitGenericInvocationSuffix())
                    .add(new Literal("this"))
                )
            );
            // this [Arguments]
            primary.add(new TrackSequence("<primary$3>")
                .add(new Literal("this"))
                .add(zeroOrOne(arguments()))
            );
            // super SuperSuffix
            primary.add(new TrackSequence("<primary$4>")
                .add(new Literal("this"))
                .add(superSuffix())
            );
            // Literal
            primary.add(literal());
            // new Creator
            primary.add(new TrackSequence("<primary$5>")
                .add(new Literal("new"))
                .add(creator())
            );
            // Identifier {. Identifier }[ IdentifierSuffix]
            primary.add(new TrackSequence("<primary$6>")
                .add(identifier())
                .add(new Repetition(new TrackSequence("<primary$7>")
                    .add(new Symbol('.'))
                    .add(identifier())
                ))
                .add(zeroOrOne(identifierSuffix()))
            );
            // BasicType BracketsOpt.class
            primary.add(new TrackSequence("<primary$8>")
                .add(basicType())
                .add(bracketsOpt())
                .add(new Symbol('.'))
                .add(new Literal("class"))
            );
            // void.class
            primary.add(new TrackSequence("<primary$9>")
                .add(new Literal("void"))
                .add(new Symbol("."))
                .add(new Literal("class"))
            );
        return primary;
    }

    /** IdentifierSuffix:
     *      [ (] BracketsOpt   . class | Expression])
     *      Arguments
     *      .   (class | ExplicitGenericInvocation |this |super Arguments |new [NonWildcardTypeArguments] InnerCreator )
     */
    public Parser identifierSuffix() {
        log("identifierSuffix");
        if (identifierSuffix != null) return identifierSuffix;
        identifierSuffix = new Alternation();

                // [ (] BracketsOpt   . class | Expression])
                identifierSuffix.add(new TrackSequence("<identifierSuffix$1>")
                        .add(new Symbol('['))
                        .add(new Alternation()
                            .add(new TrackSequence("<identifierSuffix$1>")
                                .add(new Symbol(']'))
                                .add(bracketsOpt())
                                .add(new Symbol('.'))
                                .add(new Literal("class"))
                            )
                            .add(new TrackSequence("<identifierSuffix$2>")
                                .add(expression())
                                .add(new Symbol(']'))
                            )
                        )
                );

                // Arguments
                identifierSuffix.add(arguments());

                // .   (class | ExplicitGenericInvocation |this |super Arguments |new [NonWildcardTypeArguments] InnerCreator )
                identifierSuffix.add(new TrackSequence("<identifierSuffix$3>")
                    .add(new Symbol('.'))
                    .add(new Alternation()
                        .add(new Literal("class"))
                        .add(explicitGenericInvocation())
                        .add(new Literal("this"))
                        .add(new TrackSequence("<identifierSuffix$4>")
                            .add(new Literal("super"))
                            .add(arguments())
                        )
                        .add(new TrackSequence("<identifierSuffix$5>")
                            .add(new Literal("new"))
                            .add(zeroOrOne(nonWildcardTypeArguments()))
                            .add(innerCreator())
                        )
                    )
                );
        return identifierSuffix;
    }

    /** ExplicitGenericInvocation:
     *      NonWildcardTypeArguments ExplicitGenericInvocationSuffix
     */
    public Parser explicitGenericInvocation() {
        log("explicitGenericInvocation");
        if (explicitGenericInvocation != null) return explicitGenericInvocation;
        explicitGenericInvocation = new TrackSequence("<explicitGenericInvocation>");
        explicitGenericInvocation.add(nonWildcardTypeArguments());
        explicitGenericInvocation.add(explicitGenericInvocationSuffix());
        return explicitGenericInvocation;
    }

    /** NonWildcardTypeArguments:
     *      < TypeList>
     */
    public Parser nonWildcardTypeArguments() {
        log("nonWildcardTypeArguments");
        if (nonWildcardTypeArguments != null) return nonWildcardTypeArguments;
        nonWildcardTypeArguments = new TrackSequence("<nonWildcardTypeArguments>");
        nonWildcardTypeArguments.add(new Symbol("<"));
        nonWildcardTypeArguments.add(typeList());
        nonWildcardTypeArguments.add(new Symbol(">"));
        return nonWildcardTypeArguments;
    }

    /** ExplicitGenericInvocationSuffix:
     *      super SuperSuffix
     *      Identifier Arguments
     */
    public Parser explicitGenericInvocationSuffix() {
        log("explicitGenericInvocationSuffix");
        if (explicitGenericInvocationSuffix != null) return explicitGenericInvocationSuffix;
        explicitGenericInvocationSuffix = new Alternation();
            explicitGenericInvocationSuffix.add(new TrackSequence("<explicitGenericInvocationSuffix$1>")
                .add(new Literal("super"))
                .add(superSuffix()));
            explicitGenericInvocationSuffix.add(new TrackSequence("<explicitGenericInvocationSuffix$2>")
                .add(identifier())
                .add(arguments())
            );
        return explicitGenericInvocationSuffix;
    }

    /** PrefixOp:
     *      ++
     *      --
     *      !
     *      ~
     *      +
     *      -
     */
    public Parser prefixOp() {
        log("prefixOp");
        if (prefixOp != null) return prefixOp;
        prefixOp = new Alternation();
        prefixOp.add(new Symbol("++"));
        prefixOp.add(new Symbol("--"));
        prefixOp.add(new Symbol('!'));
        prefixOp.add(new Symbol('~'));
        prefixOp.add(new Symbol('+'));
        prefixOp.add(new Symbol('-'));
        return prefixOp;
    }

    /** PostfixOp:
     *      ++
     *      --
     */
    public Parser postfixOp() {
        log("postfixOp");
        if (postfixOp != null) return postfixOp;
        postfixOp = new Alternation();
        postfixOp.add(new Symbol("++"));
        postfixOp.add(new Symbol("--"));
        return postfixOp;
    }

    /** Selector:
     *      . Identifier [Arguments]
     *      . ExplicitGenericInvocation
     *      .this
     *      .super SuperSuffix
     *      .new [NonWildcardTypeArguments] InnerCreator
     *      [ Expression]
     */
    public Parser selector() {
        log("selector");
        if (selector != null) return selector;
        selector = new Alternation();
            selector.add(new TrackSequence("<selector$1>")
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(arguments()))
            );
            selector.add(new TrackSequence("<selector$2>")
                .add(new Symbol('.'))
                .add(explicitGenericInvocation())
            );
            selector.add(new TrackSequence("<selector$3>")
                .add(new Symbol('.'))
                .add(new Literal("this"))
            );
            selector.add(new TrackSequence("<selector$4>")
                .add(new Symbol('.'))
                .add(new Literal("super"))
                .add(superSuffix())
            );
            selector.add(new TrackSequence("<selector$5>")
                .add(new Symbol('.'))
                .add(new Literal("new"))
                .add(zeroOrOne(nonWildcardTypeArguments()))
                .add(innerCreator())
            );
            selector.add(new TrackSequence("<selector$6>")
                .add(new Symbol('['))
                .add(expression())
                .add(new Symbol(']'))
            );
        return selector;
    }

    /** SuperSuffix:
     *      Arguments
     *      . Identifier [Arguments]
     */
    public Parser superSuffix() {
        log("superSuffix");
        if (superSuffix != null) return superSuffix;
        superSuffix = new Alternation();
            superSuffix.add(arguments());
            superSuffix.add(new TrackSequence("<superSuffix$1>")
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(arguments()))
            );
        return superSuffix;
    }

    /** BasicType:
     *      byte
     *      short
     *      char
     *      int
     *      long
     *      float
     *      double
     *      boolean
     */
    public Parser basicType() {
        log("basicType");
        if (basicType != null) return basicType;
        basicType = new Alternation();
            basicType.add(new Literal("byte"));
            basicType.add(new Literal("short"));
            basicType.add(new Literal("char"));
            basicType.add(new Literal("int"));
            basicType.add(new Literal("long"));
            basicType.add(new Literal("float"));
            basicType.add(new Literal("double"));
            basicType.add(new Literal("boolean"));
        return basicType;
    }

    /** ArgumentsOpt:
     *      [ Arguments ]
     */
    public Parser argumentsOpt() {
        log("argumentsOpt");
        if (argumentsOpt != null) return argumentsOpt;
        argumentsOpt = zeroOrOne(arguments());
        return argumentsOpt;
    }

    /** Arguments:
     *      ( [Expression {, Expression }])
     */
    public Parser arguments() {
        log("arguments");
        if (arguments != null) return arguments;
        arguments = new TrackSequence("<arguments>");
            arguments.add(new Symbol('('));
            arguments.add(zeroOrOne(new TrackSequence("<arguments$1>")
                .add(expression())
                .add(new Repetition(new TrackSequence("<arguments$2>")
                    .add(new Symbol(','))
                    .add(expression())
                ))
            ));
            arguments.add(new Symbol(')'));
        return arguments;
    }

    /** BracketsOpt:
     *      {[]}
     */
    public Parser bracketsOpt() {
        log("bracketsOpt");
        if (bracketsOpt != null) return bracketsOpt;
        bracketsOpt = new Repetition(new TrackSequence("<bracketsOpt$1>")
            .add(new Symbol('['))
            .add(new Symbol(']'))
        );
        return bracketsOpt;
    }

    /** Creator:
     *      [NonWildcardTypeArguments] CreatedName ( ArrayCreatorRest  | ClassCreatorRest )
     */
    public Parser creator() {
        log("creator");
        if (creator != null) return creator;
        creator = new TrackSequence("<creator>");
            creator.add(zeroOrOne(nonWildcardTypeArguments()));
            creator.add(createdName());
            creator.add(new Alternation()
                .add(arrayCreatorRest())
                .add(classCreatorRest())
            );
        return creator;
    }

    /** CreatedName:
     *      Identifier [NonWildcardTypeArguments] {. Identifier [NonWildcardTypeArguments]}
     */
    public Parser createdName() {
        log("createdName");
        if (createdName != null) return createdName;
        createdName = new TrackSequence("<createdName>");
            createdName.add(identifier());
            createdName.add(zeroOrOne(nonWildcardTypeArguments()));
            createdName.add(new Repetition(new TrackSequence("<createdName$1>")
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(nonWildcardTypeArguments()))
            ));
        return createdName;
    }

    /** InnerCreator:
     *      Identifier ClassCreatorRest
     */
    public Parser innerCreator() {
        log("innerCreator");
        if (innerCreator != null) return innerCreator;
        innerCreator = new TrackSequence("<innerCreator>");
            innerCreator.add(identifier());
            innerCreator.add(classCreatorRest());
        return innerCreator;
    }

    /** ArrayCreatorRest:
     *      [ (] BracketsOpt ArrayInitializer | Expression] {[ Expression]} BracketsOpt )
     */
    public Parser arrayCreatorRest() {
        log("arrayCreatorRest");
        if (arrayCreatorRest != null) return arrayCreatorRest;
        arrayCreatorRest = new TrackSequence("<arrayCreatorRest>");
            arrayCreatorRest.add(new Symbol('['));
            arrayCreatorRest.add(new Alternation()
                .add(new TrackSequence("<arrayCreatorRest$1>")
                    .add(new Symbol(']'))
                    .add(bracketsOpt())
                    .add(arrayInitializer())
                )
                .add(new TrackSequence("<arrayCreator$2>")
                    .add(expression())
                    .add(new Symbol(']'))
                    .add(new Repetition(new TrackSequence("<arrayCreator$3>")
                        .add(new Symbol('['))
                        .add(expression())
                        .add(new Symbol(']'))
                    ))
                    .add(bracketsOpt())
                )
            );
        return arrayCreatorRest;
    }

    /** ClassCreatorRest:
     *      Arguments [ClassBody]
     */
    public Parser classCreatorRest() {
        log("classCreatorRest");
        if (classCreatorRest != null) return classCreatorRest;
        classCreatorRest = new TrackSequence("<classCreatorRest>");
            classCreatorRest.add(arguments());
            classCreatorRest.add(zeroOrOne(classBody()));
        return classCreatorRest;
    }

    /** ArrayInitializer:
     *      { [VariableInitializer {, VariableInitializer} [,]]}
     */
    public Parser arrayInitializer() {
        log("arrayInitializer");
        if (arrayInitializer != null) return arrayInitializer;
        arrayInitializer = new TrackSequence("<arrayInitializer>");
        arrayInitializer.add(new Symbol("{"));
        arrayInitializer.add(zeroOrOne(new TrackSequence("<arrayInitializer$1>")
                .add(variableInitializer())
                .add(new Repetition(new TrackSequence("<arrayInitializer$2>")
                    .add(new Symbol(","))
                    .add(variableInitializer())
                ))
                .add(zeroOrOne(new Symbol(',')))
            ));
        arrayInitializer.add(new Symbol("}"));
        return arrayInitializer;
    }

    /** VariableInitializer:
     *      ArrayInitializer
     *      Expression
     */
    public Parser variableInitializer() {
        log("variableInitializer");
        if (variableInitializer != null) return variableInitializer;
        variableInitializer = new Alternation();
            variableInitializer.add(arrayInitializer());
            variableInitializer.add(expression());
        return variableInitializer;
    }

    /** ParExpression:
     *      ( Expression)
     */
    public Parser parExpression() {
        log("parExpression");
        if (parExpression != null) return parExpression;
        parExpression = new TrackSequence("<parExpression>");
            parExpression.add(new Symbol('('));
            parExpression.add(expression());
            parExpression.add(new Symbol(')'));
        return parExpression;
    }

    /** Block:
     *      { BlockStatements}
     */
    public Parser block() {
        log("block");
        if (block != null) return block;
        block = new TrackSequence("<block>");
        block.add(new Symbol('{'));
        block.add(blockStatements());
        block.add(new Symbol('}'));
        return block;
    }

    /** BlockStatements:
     *      { BlockStatement }
     */
    public Parser blockStatements() {
        log("blockStatements");
        if (blockStatements != null) return blockStatements;
        blockStatements = new Repetition(blockStatement());
        return blockStatements;
    }

    /** BlockStatement:
     *      LocalVariableDeclarationStatement
     *      ClassOrInterfaceDeclaration
     *      [Identifier:] Statement
     */
    public Parser blockStatement() {
        log("blockStatement");
        if (blockStatement != null) return blockStatement;
        blockStatement = new Alternation();
            blockStatement.add(localVariableDeclarationStatement());
            blockStatement.add(classOrInterfaceDeclaration());
            blockStatement.add(new TrackSequence("<blockStatement$1>")
                .add(zeroOrOne(new TrackSequence("<blockStatement$2>")
                    .add(identifier())
                    .add(new Symbol(":"))
                ))
                .add(statement())
            );
        return blockStatement;
    }

    /** LocalVariableDeclarationStatement:
     *      [final] Type VariableDeclarators;
     */
    public Parser localVariableDeclarationStatement() {
        log("localVariableDeclarationStatement");
        if (localVariableDeclarationStatement != null) return localVariableDeclarationStatement;
        localVariableDeclarationStatement = new TrackSequence("<localVariableDeclarationStatement>");
            localVariableDeclarationStatement.add(zeroOrOne(new Literal("final")));
            localVariableDeclarationStatement.add(type());
            localVariableDeclarationStatement.add(variableDeclarators());
            localVariableDeclarationStatement.add(new Symbol(';'));
        return localVariableDeclarationStatement;
    }

    /** Statement:
     *      Block
     *      assert Expression [: Expression];
     *      if ParExpression Statement [else Statement]
     *      for(ForControl) Statement
     *      while ParExpression Statement
     *      do Statement while ParExpression ;
     *      try Block ( Catches | [Catches] finally Block )
     *      switch ParExpression{ SwitchBlockStatementGroups}
     *      synchronized ParExpression Block
     *      return [Expression];
     *      throw Expression ;
     *      break [Identifier]
     *      continue [Identifier]
     *      ;
     *      ExpressionStatement
     *      Identifier :   Statement
     */
    public Parser statement() {
        log("statement");
        if (statement != null) return statement;
        statement = new Alternation();
            //Block
             statement.add(block());
            //assert Expression [: Expression];
            statement.add(new TrackSequence("<statement$1>")
                .add(new Literal("assert"))
                .add(expression())
                .add(zeroOrOne(
                    new TrackSequence("<statement$2>")
                        .add(new Symbol(':'))
                        .add(expression())
                ))
                .add(new Symbol(';'))
            );
            //if ParExpression Statement [else Statement]
            statement.add(new TrackSequence("<statement$3>")
                .add(new Literal("if"))
                .add(parExpression())
                .add(statement())
                .add(zeroOrOne(
                    new TrackSequence("<statement$4>")
                        .add(new Literal("else"))
                        .add(statement())
                ))
            );
            //for (ForControl) Statement
            statement.add(new TrackSequence("<statement$5>")
                .add(new Literal("for"))
                .add(new Symbol('('))
                .add(forControl())
                .add(new Symbol(')'))
                .add(statement())
            );
            //while ParExpression Statement
            statement.add(new TrackSequence("<statement$6>")
                .add(new Literal("while"))
                .add(parExpression())
                .add(statement())
            );
            //do Statement while ParExpression ;
            statement.add(new TrackSequence("<statement$7>")
                .add(new Literal("do"))
                .add(statement())
                .add(new Literal("while"))
                .add(parExpression())
                .add(new Symbol(';'))
            );
            //try Block ( Catches | [Catches] finally Block )
            statement.add(new TrackSequence("<statement$8>")
                .add(new Literal("try"))
                .add(block())
                .add(new Alternation()
                    .add(catches())
                    .add(new TrackSequence("<statement$9>")
                        .add(zeroOrOne(catches()))
                        .add(new Literal("finally"))
                        .add(block())
                    )
                )
            );
            //switch ParExpression{ SwitchBlockStatementGroups}
            statement.add(new TrackSequence("<statement$10>")
                .add(new Literal("switch"))
                .add(parExpression())
                .add(new Symbol('{'))
                .add(switchBlockStatementGroups())
                .add(new Symbol('}'))
            );
            //synchronized ParExpression Block
            statement.add(new TrackSequence("<statement$11>")
                .add(new Literal("synchronized"))
                .add(parExpression())
                .add(block())
            );
            //return [Expression];
            statement.add(new TrackSequence("<statement$12>")
                .add(new Literal("return"))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
            );
            //throw Expression ;
            statement.add(new TrackSequence("<statement$13>")
                .add(new Literal("throw"))
                .add(expression())
                .add(new Symbol(';'))
            );
            //break [Identifier]
            statement.add(new TrackSequence("<statement$14>")
                .add(new Literal("break"))
                .add(zeroOrOne(identifier()))
            );
            //continue [Identifier]
            statement.add(new TrackSequence("<statement$15>")
                .add(new Literal("continue"))
                .add(zeroOrOne(identifier()))
            );
            //;
            statement.add(new Symbol(';'));
            //ExpressionStatement
            statement.add(expressionStatement());
            //Identifier :   Statement
            statement.add(new TrackSequence("<statement$16>")
                .add(identifier())
                .add(new Symbol(':'))
                .add(statement())
            );
        return statement;
    }

    /** Catches:
     *      CatchClause {CatchClause}
     */
    public Parser catches() {
        log("catches");
        if (catches != null) return catches;
        catches = new TrackSequence("<catches>");
            catches.add(catchClause());
            catches.add(new Repetition(catchClause()));
        return catches;
    }

    /** CatchClause:
     *      catch( FormalParameter) Block
     */
    public Parser catchClause() {
        log("catchClause");
        if (catchClause != null) return catchClause;
        catchClause = new TrackSequence("<catchClause>");
            catchClause.add(new Literal("catch"));
            catchClause.add(new Symbol('('));
            catchClause.add(formalParameter());
            catchClause.add(new Symbol(')'));
            catchClause.add(block());
        return catchClause;
    }

    /** SwitchBlockStatementGroups:
     *      { SwitchBlockStatementGroup }
     */
    public Parser switchBlockStatementGroups() {
        log("switchBlockStatementGroups");
        if (switchBlockStatementGroups != null) return switchBlockStatementGroups;
        switchBlockStatementGroups = new Repetition(switchBlockStatementGroup());
        return switchBlockStatementGroups;
    }

    /** SwitchBlockStatementGroup:
     *      SwitchLabel BlockStatements
     */
    public Parser switchBlockStatementGroup() {
        log("switchBlockStatementGroup");
        if (switchBlockStatementGroup != null) return switchBlockStatementGroup;
        switchBlockStatementGroup = new TrackSequence("<switchBlockStatementGroup>");
            switchBlockStatementGroup.add(switchLabel());
            switchBlockStatementGroup.add(blockStatements());
        return switchBlockStatementGroup;
    }

    /** SwitchLabel:
     *      case ConstantExpression :
     *      default:
     */
    public Parser switchLabel() {
        log("switchLabel");
        if (switchLabel != null) return switchLabel;
        switchLabel = new Alternation();
            switchLabel.add(new TrackSequence("<switchLabel$1>")
                .add(new Literal("case"))
                .add(constantExpression())
                .add(new Symbol(":"))
            );
            switchLabel.add(new Literal("default"));
            switchLabel.add(new Symbol(":"));
        return switchLabel;
    }

    /** MoreStatementExpressions:
     *      {, StatementExpression }
     */
    public Parser moreStatementExpressions() {
        log("moreStatementExpressions");
        if (moreStatementExpressions != null) return moreStatementExpressions;
        moreStatementExpressions = new Repetition(new TrackSequence("<moreStatementExpressions$1>")
            .add(new Symbol(','))
            .add(statementExpression())
        );
        return moreStatementExpressions;
    }

    /** ForControl:
     *      ;   [Expression] ;   ForUpdateOpt
     *      StatementExpression MoreStatementExpressions;   [Expression]; ForUpdateOpt
     *      [final] [Annotations] Type Identifier ForControlRest
     */
    public Parser forControl() {
        log("forControl");
        if (forControl != null) return forControl;
        forControl = new Alternation();
            forControl.add(new TrackSequence("<forControl$1>")
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            forControl.add(new TrackSequence("<forControl$2>")
                .add(statementExpression())
                .add(moreStatementExpressions())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            forControl.add(new TrackSequence("<forControl$3>")
                .add(zeroOrOne(new Literal("final")))
                .add(zeroOrOne(annotations()))
                .add(type())
                .add(identifier())
                .add(forControlRest())
            );
        return forControl;
    }

    /** ForControlRest:
     *      VariableDeclaratorsRest;   [Expression] ;   ForUpdateOpt
     *      : Expression
     */
    public Parser forControlRest() {
        log("forControlRest");
        if (forControlRest != null) return forControlRest;
        forControlRest = new Alternation();
            forControlRest.add(new TrackSequence("<forControlRest$1>")
                .add(variableDeclaratorsRest())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            forControlRest.add(new TrackSequence("<forControlRest$2>")
                .add(new Symbol(':'))
                .add(expression())
            );
        return forControlRest;
    }

    /** ForUpdate:
     *      StatementExpression MoreStatementExpressions
     */
    public Parser forUpdate() {
        log("forUpdate");
        if (forUpdate != null) return forUpdate;
        forUpdate = new TrackSequence("<forUpdate>");
            forUpdate.add(statementExpression());
            forUpdate.add(moreStatementExpressions());
        return forUpdate;
    }


    /** ModifiersOpt:
     *      { Modifier }
     */
    public Parser modifiersOpt() {
        log("modifiersOpt");
        if (modifiersOpt != null) return modifiersOpt;
        modifiersOpt = new Repetition(modifier());
        return modifiersOpt;
    }

    /** Modifier:
     *      Annotation
     *      public
     *      protected
     *      private
     *      static
     *      abstract
     *      final
     *      native
     *      synchronized
     *      transient
     *      volatile
     *      strictfp
     */
    public Parser modifier() {
        log("modifier");
        if (modifier != null) return modifier;
        modifier = new Alternation();
            modifier.add(annotation());
            modifier.add(new Literal("public"));
            modifier.add(new Literal("protected"));
            modifier.add(new Literal("private"));
            modifier.add(new Literal("static"));
            modifier.add(new Literal("abstract"));
            modifier.add(new Literal("final"));
            modifier.add(new Literal("native"));
            modifier.add(new Literal("synchronized"));
            modifier.add(new Literal("transient"));
            modifier.add(new Literal("volatile"));
            modifier.add(new Literal("strictfp"));
        return modifier;
    }

    /** VariableDeclarators:
     *      VariableDeclarator {,   VariableDeclarator }
     */
    public Parser variableDeclarators() {
        log("variableDeclarators");
        if (variableDeclarators != null) return variableDeclarators;
        variableDeclarators = new TrackSequence("<variableDeclarators>");
            variableDeclarators.add(variableDeclarator());
            variableDeclarators.add(new Repetition(new TrackSequence("<variableDeclarators$1>")
                .add(new Symbol(','))
                .add(variableDeclarator())
            ));
        return variableDeclarators;
    }

    /** VariableDeclaratorsRest:
     *      VariableDeclaratorRest {,   VariableDeclarator }
     */
    public Parser variableDeclaratorsRest() {
        log("variableDeclaratorsRest");
        if (variableDeclaratorsRest != null) return variableDeclaratorsRest;
        variableDeclaratorsRest = new TrackSequence("<variableDeclaratorsRest>");
            variableDeclaratorsRest.add(variableDeclaratorRest());
            variableDeclaratorsRest.add(new Repetition(new TrackSequence("<variableDeclaratorsRest$1>")
                .add(new Symbol(','))
                .add(variableDeclarator())
            ));
        return variableDeclaratorsRest;
    }

    /** ConstantDeclaratorsRest:
     *      ConstantDeclaratorRest {,   ConstantDeclarator }
     */
    public Parser constantDeclaratorsRest() {
        log("constantDeclaratorsRest");
        if (constantDeclaratorsRest != null) return constantDeclaratorsRest;
        constantDeclaratorsRest = new TrackSequence("<constantDeclaratorsRest>");
            constantDeclaratorsRest.add(constantDeclaratorRest());
            constantDeclaratorsRest.add(new Repetition(new TrackSequence("<constantDeclaratorsRest$1>")
                .add(new Symbol(','))
                .add(constantDeclarator())
            ));
        return constantDeclaratorsRest;
    }

    /** VariableDeclarator:
     *      Identifier VariableDeclaratorRest
     */
    public Parser variableDeclarator() {
        log("variableDeclarator");
        if (variableDeclarator != null) return variableDeclarator;
        variableDeclarator = new TrackSequence("<variableDeclarator>");
        variableDeclarator.add(identifier());
        variableDeclarator.add(variableDeclaratorRest());
        return variableDeclarator;
    }

    /** ConstantDeclarator:
     *      Identifier ConstantDeclaratorRest
     */
    public Parser constantDeclarator() {
        log("constantDeclarator");
        if (constantDeclarator != null) return constantDeclarator;
        constantDeclarator = new TrackSequence("<constantDeclarator>");
            constantDeclarator.add(identifier());
            constantDeclarator.add(constantDeclaratorRest());
        return constantDeclarator;
    }

    /** VariableDeclaratorRest:
     *      BracketsOpt [ =   VariableInitializer]
     */
    public Parser variableDeclaratorRest() {
        log("variableDeclaratorRest");
        if (variableDeclaratorRest != null) return variableDeclaratorRest;
        variableDeclaratorRest = new TrackSequence("<variableDeclaratorRest>");
            variableDeclaratorRest.add(bracketsOpt());
            variableDeclaratorRest.add(zeroOrOne(new TrackSequence("<variableDeclaratorRest$1>")
                .add(new Symbol('='))
                .add(variableInitializer())
            ));
        return variableDeclaratorRest;
    }

    /** ConstantDeclaratorRest:
     *      BracketsOpt =   VariableInitializer
     */
    public Parser constantDeclaratorRest() {
        log("constantDeclaratorRest");
        if (constantDeclaratorRest != null) return constantDeclaratorRest;
        constantDeclaratorRest = new TrackSequence("<constantDeclaratorRest>");
            constantDeclaratorRest.add(bracketsOpt());
            constantDeclaratorRest.add(new Symbol('='));
            constantDeclaratorRest.add(variableInitializer());
        return constantDeclaratorRest;
    }

    /** VariableDeclaratorId:
     *      Identifier BracketsOpt
     */
    public Parser variableDeclaratorId() {
        log("variableDeclaratorId");
        if (variableDeclaratorId != null) return variableDeclaratorId;
        variableDeclaratorId =  new TrackSequence("<variableDeclaratorId>");
            variableDeclaratorId.add(identifier());
            variableDeclaratorId.add(bracketsOpt());
        return variableDeclaratorId;
    }

    /** CompilationUnit:
     *      [Annotations opt package QualifiedIdentifier ;  ] {ImportDeclaration} {TypeDeclaration}
     */
    public Parser compilationUnit() {
        log("compilationUnit");
        if (compilationUnit != null) return compilationUnit;
        compilationUnit = new TrackSequence("<compilationUnit>");
            compilationUnit.add(zeroOrOne(
                new TrackSequence("<compilationUnit$1>")
                    .add(zeroOrOne(annotations()))
                    .add(new Literal("package"))
                    .add(qualifiedIdentifier())
                    .add(new Symbol(';'))
            ));
            compilationUnit.add(new Repetition(importDeclaration()));
            compilationUnit.add(new Repetition(typeDeclaration()));
        return compilationUnit;
    }

    /** ImportDeclaration:
     *      import [static] Identifier { .   Identifier } [  .*   ];
     */
    public Parser importDeclaration() {
        log("importDeclaration");
        if (importDeclaration != null) return importDeclaration;
        importDeclaration = new TrackSequence("<importDeclaration>");
            importDeclaration.add(new Literal("import"));
            importDeclaration.add(zeroOrOne(new Literal("static")));
            importDeclaration.add(identifier());
            importDeclaration.add(new Repetition(new TrackSequence("<importDeclaration$1>")
                .add(new Symbol('.'))
                .add(identifier())
            ));
            importDeclaration.add(zeroOrOne(new Symbol(".*")));
            importDeclaration.add(new Symbol(';'));
        return importDeclaration;
    }

    /** TypeDeclaration:
     *      ClassOrInterfaceDeclaration
     *      ;
     */
    public Parser typeDeclaration() {
        log("typeDeclaration");
        if (typeDeclaration != null) return typeDeclaration;
        typeDeclaration = new Alternation();
            typeDeclaration.add(classOrInterfaceDeclaration());
            typeDeclaration.add(new Symbol(';'));
        return typeDeclaration;
    }

    /** ClassOrInterfaceDeclaration:
     *      ModifiersOpt (ClassDeclaration | InterfaceDeclaration)
     */
    public Parser classOrInterfaceDeclaration() {
        log("classOrInterfaceDeclaration");
        if (classOrInterfaceDeclaration != null) return classOrInterfaceDeclaration;
        classOrInterfaceDeclaration = new TrackSequence("<classOrInterfaceDeclaration>");
        classOrInterfaceDeclaration.add(modifiersOpt());
        classOrInterfaceDeclaration.add(new Alternation()
                .add(classDeclaration())
                .add(interfaceDeclaration())
            );
        return classOrInterfaceDeclaration;
    }

    /** ClassDeclaration:
     *      NormalClassDeclaration           // note error in JLS¤18 makes this look like a Sequence not an Alternation
     *      EnumDeclaration
     */
    public Parser classDeclaration() {
        log("classDeclaration");
        if (classDeclaration != null) return classDeclaration;
        classDeclaration = new Alternation();
        classDeclaration.add(normalClassDeclaration());
        classDeclaration.add(enumDeclaration());
        return classDeclaration;
    }

    /** NormalClassDeclaration:
     *      class Identifier TypeParameters opt [extends Type] [implements TypeList] ClassBody
     */
    public Parser normalClassDeclaration() {
        log("normalClassDeclaration");
        if (normalClassDeclaration != null) return normalClassDeclaration;
        normalClassDeclaration = new TrackSequence("<normalClassDeclaration>");
            normalClassDeclaration.add(new Literal("class"));
            normalClassDeclaration.add(identifier());
            normalClassDeclaration.add(zeroOrOne(typeParameters()));
            normalClassDeclaration.add(zeroOrOne(new TrackSequence("<normalClassDeclaration$1>")
                .add(new Literal("extends"))
                .add(type())
            ));
            normalClassDeclaration.add(zeroOrOne(new TrackSequence("<normalClassDeclaration$2>")
                .add(new Literal("implements"))
                .add(typeList())
            ));
            normalClassDeclaration.add(classBody());
        return normalClassDeclaration;
    }

    /** TypeParameters:
     *      < TypeParameter {, TypeParameter}>
     */
    public Parser typeParameters() {
        log("typeParameters");
        if (typeParameters != null) return typeParameters;
        typeParameters = new TrackSequence("<typeParameters>");
            typeParameters.add(new Symbol('<'));
            typeParameters.add(typeParameter());
            typeParameters.add(new Repetition(new TrackSequence("<typeParameters$1>")
                .add(new Symbol(','))
                .add(typeParameter())
            ));
            typeParameters.add(new Symbol('>'));
        return typeParameters;
    }

    /** TypeParameter:
     *      Identifier [extendsBound]
     */
    public Parser typeParameter() {
        log("typeParameter");
        if (typeParameter != null) return typeParameter;
        typeParameter = new TrackSequence("<typeParameter>");
            typeParameter.add(identifier());
            typeParameter.add(zeroOrOne(new TrackSequence("<typeParameter$1>")
                .add(new Literal("extends"))
                .add(bound())
            ));
        return typeParameter;
    }

    /** Bound:
     *      Type {&Type}
     */
    public Parser bound() {
        log("bound");
        if (bound != null) return bound;
        bound = new TrackSequence("<bound>");
            bound.add(type());
            bound.add(new Repetition(new TrackSequence("<bound$1>")
                .add(new Symbol('&'))
                .add(type())
            ));
        return bound;
    }

    /** EnumDeclaration:
     *      ClassModifiers opt enum Identifier[implements TypeList] EnumBody
     */
    public Parser enumDeclaration() {
        log("enumDeclaration");
        if (enumDeclaration != null) return enumDeclaration;
        enumDeclaration = new TrackSequence("<enumDeclaration>");
            enumDeclaration.add(zeroOrOne(classModifiers()));
            enumDeclaration.add(new Literal("enum"));
            enumDeclaration.add(identifier());
            enumDeclaration.add(zeroOrOne(new TrackSequence("<enumDeclaration$1>")
                .add(new Literal("implements"))
                .add(typeList())
            ));
            enumDeclaration.add(enumBody());
        return enumDeclaration;
    }

    /** EnumBody:
     *      { EnumConstants opt ,opt EnumBodyDeclarations opt }
     */
    public Parser enumBody() {
        log("enumBody");
        if (enumBody != null) return enumBody;
        enumBody = new TrackSequence("<enumBody>");
            enumBody.add(new Symbol('{'));
            enumBody.add(zeroOrOne(enumConstants()));
            enumBody.add(zeroOrOne(new Symbol(',')));
            enumBody.add(zeroOrOne(enumBodyDeclarations()));
            enumBody.add(new Symbol('}'));
        return enumBody;
    }

    /** EnumConstants:
     *      EnumConstant
     *      EnumConstants , EnumConstant
     */
    public Parser enumConstants() {
        log("enumConstants");
        if (enumConstants != null) return enumConstants;
        enumConstants = new Alternation();
            enumConstants.add(enumConstant());
            enumConstants.add(new TrackSequence("<enumConstants$1>")
                .add(enumConstants())
                .add(new Symbol(','))
                .add(enumConstant())
            );
        return enumConstants;
    }

    /** EnumConstant:
     *      Annotations Identifier EnumArguments opt ClassBody opt
     *
     * N.B. JRR changed to EnumArguments - check with JLSv3 spec revisions (bug reported in grammar)
     */
    public Parser enumConstant() {
        log("enumConstant");
        if (enumConstant != null) return enumConstant;
        enumConstant = new TrackSequence("<enumConstant>");
            enumConstant.add(annotations());
            enumConstant.add(identifier());
            enumConstant.add(zeroOrOne(enumArguments()));
            enumConstant.add(zeroOrOne(classBody()));
        return enumConstant;
    }

    /** EnumArguments:
     *      ( ArgumentListopt )
     *
     * N.B. JRR changed to EnumArguments - check with JLSv3 spec revisions (bug reported in grammar)
     */
    public Parser enumArguments() {
        log("enumArguments");
        if (enumArguments != null) return enumArguments;
        enumArguments = new TrackSequence("<enumArguments>");
            enumArguments.add(new Symbol('('));
            enumArguments.add(zeroOrOne(argumentList()));
            enumArguments.add(new Symbol(')'));
        return enumArguments;
    }

    /** EnumBodyDeclarations:
     *      ; ClassBodyDeclarationsopt
     *
     * todo that semicolon looks a bit dodgy... jez
     */
    public Parser enumBodyDeclarations() {
        log("enumBodyDeclarations");
        if (enumBodyDeclarations != null) return enumBodyDeclarations;
        enumBodyDeclarations = new TrackSequence("<enumBodyDeclarations>");
            enumBodyDeclarations.add(new Symbol(';'));
            enumBodyDeclarations.add(zeroOrOne(classBodyDeclarations()));
        return enumBodyDeclarations;
    }

    /** InterfaceDeclaration:
     *      NormalInterfaceDeclaration
     *      AnnotationTypeDeclaration
     */
    public Parser interfaceDeclaration() {
        log("interfaceDeclaration");
        if (interfaceDeclaration != null) return interfaceDeclaration;
        interfaceDeclaration = new Alternation();
            interfaceDeclaration.add(normalInterfaceDeclaration());
            interfaceDeclaration.add(annotationTypeDeclaration());
        return interfaceDeclaration;
    }

    /** NormalInterfaceDeclaration:
     *      interface Identifier TypeParameters opt[extends TypeList] InterfaceBody
     */
    public Parser normalInterfaceDeclaration() {
        log("normalInterfaceDeclaration");
        if (normalInterfaceDeclaration != null) return normalInterfaceDeclaration;
        normalInterfaceDeclaration = new TrackSequence("<normalInterfaceDeclaration>");
            normalInterfaceDeclaration.add(new Literal("interface"));
            normalInterfaceDeclaration.add(identifier());
            normalInterfaceDeclaration.add(zeroOrOne(typeParameters()));
            normalInterfaceDeclaration.add(zeroOrOne(new TrackSequence("<normalInterfaceDeclaration$1>")
                .add(new Literal("extends"))
                .add(typeList())
            ));
            normalInterfaceDeclaration.add(interfaceBody());
        return normalInterfaceDeclaration;
    }

    /** TypeList:
     *      Type { ,   Type}
     */
    public Parser typeList() {
        log("typeList");
        if (typeList != null) return typeList;
        typeList = new TrackSequence("<typeList>");
            typeList.add(type());
            typeList.add(new Repetition(new TrackSequence("<typeList$1>")
                .add(new Symbol(','))
                .add(type())
            ));
        return typeList;
    }

    /** AnnotationTypeDeclaration:
     *      InterfaceModifiers opt @ interface Identifier AnnotationTypeBody
     */
    public Parser annotationTypeDeclaration() {
        log("annotationTypeDeclaration");
        if (annotationTypeDeclaration != null) return annotationTypeDeclaration;
        annotationTypeDeclaration = new TrackSequence("<annotationTypeDeclaration>");
            annotationTypeDeclaration.add(zeroOrOne(interfaceModifiers()));
            annotationTypeDeclaration.add(new Symbol('@'));
            annotationTypeDeclaration.add(new Literal("interface")); // @todo is this correct?
            annotationTypeDeclaration.add(identifier());
            annotationTypeDeclaration.add(annotationTypeBody());
        return annotationTypeDeclaration;
    }

    /** AnnotationTypeBody:
     *      { AnnotationTypeElementDeclarations }
     */
    public Parser annotationTypeBody() {
        log("annotationTypeBody");
        if (annotationTypeBody != null) return annotationTypeBody;
        annotationTypeBody = new TrackSequence("<annotationTypeBody>");
            annotationTypeBody.add(new Symbol('{'));
            annotationTypeBody.add(annotationTypeElementDeclarations());
            annotationTypeBody.add(new Symbol('}'));
        return annotationTypeBody;
    }

    /** AnnotationTypeElementDeclarations:
     *      AnnotationTypeElementDeclaration
     *      AnnotationTypeElementDeclarations AnnotationTypeElementDeclaration
     */
    public Parser annotationTypeElementDeclarations() {
        log("annotationTypeElementDeclarations");
        if (annotationTypeElementDeclarations != null) return annotationTypeElementDeclarations;
        annotationTypeElementDeclarations = new Alternation();
            annotationTypeElementDeclarations.add(annotationTypeElementDeclaration());
            annotationTypeElementDeclarations.add(new TrackSequence("<annotationTypeElementDeclarations$1>")
                .add(annotationTypeElementDeclarations())
                .add(annotationTypeElementDeclaration())
            );
        return annotationTypeElementDeclarations;
    }

    /** AnnotationTypeElementDeclaration:
     *      AbstractMethodModifiers opt Type Identifier ( ) DefaultValueopt ;
     *      ConstantDeclaration
     *      ClassDeclaration
     *      InterfaceDeclaration
     *      EnumDeclaration
     *      AnnotationTypeDeclaration
     *      ;
     */
    public Parser annotationTypeElementDeclaration() {
        log("annotationTypeElementDeclaration");
        if (annotationTypeElementDeclaration != null) return annotationTypeElementDeclaration;
        annotationTypeElementDeclaration = new Alternation();
            annotationTypeElementDeclaration.add(new TrackSequence("<annotationTypeElementDeclaration$1>")
                .add(zeroOrOne(abstractMethodModifiers()))
                .add(type())
                .add(identifier())
                .add(new Symbol('('))   // @todo - is this right
                .add(new Symbol(')'))
                .add(zeroOrOne(defaultValue()))
                .add(new Symbol(';'))
            );
            annotationTypeElementDeclaration.add(constantDeclaration());
            annotationTypeElementDeclaration.add(classDeclaration());
            annotationTypeElementDeclaration.add(interfaceDeclaration());
            annotationTypeElementDeclaration.add(enumDeclaration());
            annotationTypeElementDeclaration.add(annotationTypeDeclaration());
            annotationTypeElementDeclaration.add(new Symbol(';'));
        return annotationTypeElementDeclaration;
    }

    /** DefaultValue:
     *      default ElementValue
     */
    public Parser defaultValue() {
        log("defaultValue");
        if (defaultValue != null) return defaultValue;
        defaultValue = new TrackSequence("<defaultValue>");
            defaultValue.add(new Literal("default"));
            defaultValue.add(elementValue());
        return defaultValue;
    }

    /** ClassBody:
     *      { {ClassBodyDeclaration}}
     */
    public Parser classBody() {
        log("classBody");
        if (classBody != null) return classBody;
        classBody = new TrackSequence("<classBody>");
            classBody.add(new Symbol('{'));
            classBody.add(new Repetition(classBodyDeclaration()));
            classBody.add(new Symbol('}'));
        return classBody;
    }

    /** InterfaceBody:
     *      { {InterfaceBodyDeclaration}}
     */
    public Parser interfaceBody() {
        log("interfaceBody");
        if (interfaceBody != null) return interfaceBody;
        interfaceBody = new TrackSequence("<interfaceBody>");
            interfaceBody.add(new Symbol('{'));
            interfaceBody.add(new Repetition(interfaceBodyDeclaration()));
            interfaceBody.add(new Symbol('}'));
        return interfaceBody;
    }

    /** ClassBodyDeclaration:
     *      ;
     *      [static] Block
     *      ModifiersOpt MemberDecl
     */
    public Parser classBodyDeclaration() {
        log("classBodyDeclaration");
        if (classBodyDeclaration != null) return classBodyDeclaration;
        classBodyDeclaration = new Alternation();
            classBodyDeclaration.add(new Symbol(';'));
            classBodyDeclaration.add(new TrackSequence("<classBodyDeclaration$1>")
                .add(zeroOrOne(new Literal("static")))
                .add(block())
            );
            classBodyDeclaration.add(new TrackSequence("<classBodyDeclaration$2>")
                .add(modifiersOpt())
                .add(memberDecl())
            );
        return classBodyDeclaration;
    }

    /** MemberDecl:
     *      GenericMethodOrConstructorDecl
     *      MethodOrFieldDecl
     *      void Identifier MethodDeclaratorRest
     *      Identifier ConstructorDeclaratorRest
     *      ClassOrInterfaceDeclaration
     */
    public Parser memberDecl() {
        log("memberDecl");
        if (memberDecl != null) return memberDecl;
        memberDecl = new Alternation();
            memberDecl.add(genericMethodOrConstructorDecl());
            memberDecl.add(methodOrFieldDecl());
            memberDecl.add(new TrackSequence("<memberDecl$1>")
                .add(new Literal("void"))
                .add(identifier())
                .add(methodDeclaratorRest())
            );
            memberDecl.add(new TrackSequence("<memberDecl$2>")
                .add(identifier())
                .add(constructorDeclaratorRest())
            );
            memberDecl.add(classOrInterfaceDeclaration());
        return memberDecl;
    }

    /** GenericMethodOrConstructorDecl:
     *      TypeParameters GenericMethodOrConstructorRest
     */
    public Parser genericMethodOrConstructorDecl() {
        log("genericMethodOrConstructorDecl");
        if (genericMethodOrConstructorDecl != null) return genericMethodOrConstructorDecl;
        genericMethodOrConstructorDecl = new TrackSequence("<genericMethodOrConstructorDecl>");
            genericMethodOrConstructorDecl.add(typeParameters());
            genericMethodOrConstructorDecl.add(genericMethodOrConstructorRest());
        return genericMethodOrConstructorDecl;
    }

    /** GenericMethodOrConstructorRest:
     *      Type Identifier MethodDeclaratorRest
     *      Identifier ConstructorDeclaratorRest
     */
    public Parser genericMethodOrConstructorRest() {
        log("genericMethodOrConstructorRest");
        if (genericMethodOrConstructorRest != null) return genericMethodOrConstructorRest;
        genericMethodOrConstructorRest = new Alternation();
            genericMethodOrConstructorRest.add(new TrackSequence("<genericMethodOrConstructorRest$1>")
                .add(type())
                .add(identifier())
                .add(methodDeclaratorRest())
            );
            genericMethodOrConstructorRest.add(new TrackSequence("<genericMethodOrConstructorRest$2>")
                .add(identifier())
                .add(constructorDeclaratorRest())
            );
        return genericMethodOrConstructorRest;
    }

    /** MethodOrFieldDecl:
     *      Type Identifier MethodOrFieldRest
     */
    public Parser methodOrFieldDecl() {
        log("methodOrFieldDecl");
        if (methodOrFieldDecl != null) return methodOrFieldDecl;
        methodOrFieldDecl = new TrackSequence("<methodOrFieldDecl>");
            methodOrFieldDecl.add(type());
            methodOrFieldDecl.add(identifier());
            methodOrFieldDecl.add(methodOrFieldRest());
        return methodOrFieldDecl;
    }

    /** MethodOrFieldRest:
     *      VariableDeclaratorRest
     *      MethodDeclaratorRest
     */
    public Parser methodOrFieldRest() {
        log("methodOrFieldRest");
        if (methodOrFieldRest != null) return methodOrFieldRest;
        methodOrFieldRest = new Alternation();
            methodOrFieldRest.add(variableDeclaratorRest());
            methodOrFieldRest.add(methodDeclaratorRest());
        return methodOrFieldRest;
    }

    /** InterfaceBodyDeclaration:
     *      ;
     *      ModifiersOpt InterfaceMemberDecl
     */
    public Parser interfaceBodyDeclaration() {
        log("interfaceBodyDeclaration");
        if (interfaceBodyDeclaration != null) return interfaceBodyDeclaration;
        interfaceBodyDeclaration = new Alternation();
            interfaceBodyDeclaration.add(new Symbol(';'));
            interfaceBodyDeclaration.add(new TrackSequence("<interfaceBodyDeclaration$1>")
                .add(modifiersOpt())
                .add(interfaceMemberDecl())
            );
        return interfaceBodyDeclaration;
    }

    /** InterfaceMemberDecl:
     *      InterfaceMethodOrFieldDecl
     *      InterfaceGenericMethodDecl
     *      void Identifier VoidInterfaceMethodDeclaratorRest
     *      ClassOrInterfaceDeclaration
     */
    public Parser interfaceMemberDecl() {
        log("interfaceMemberDecl");
        if (interfaceMemberDecl != null) return interfaceMemberDecl;
        interfaceMemberDecl = new Alternation();
            interfaceMemberDecl.add(interfaceMethodOrFieldDecl());
            interfaceMemberDecl.add(interfaceGenericMethodDecl());
            interfaceMemberDecl.add(new TrackSequence("<interfaceMemberDecl$1>")
                .add(new Literal("void"))
                .add(identifier())
                .add(voidInterfaceMethodDeclaratorRest())
            );
            interfaceMemberDecl.add(classOrInterfaceDeclaration());
        return interfaceMemberDecl;
    }

    /** InterfaceMethodOrFieldDecl:
     *      Type Identifier InterfaceMethodOrFieldRest
     */
    public Parser interfaceMethodOrFieldDecl() {
        log("interfaceMethodOrFieldDecl");
        if (interfaceMethodOrFieldDecl != null) return interfaceMethodOrFieldDecl;
        interfaceMethodOrFieldDecl = new TrackSequence("<interfaceMethodOrFieldDecl>");
            interfaceMethodOrFieldDecl.add(type());
            interfaceMethodOrFieldDecl.add(identifier());
            interfaceMethodOrFieldDecl.add(interfaceMethodOrFieldRest());
        return interfaceMethodOrFieldDecl;
    }

    /** InterfaceMethodOrFieldRest:
     *      ConstantDeclaratorsRest;
     *      InterfaceMethodDeclaratorRest
     */
    public Parser interfaceMethodOrFieldRest() {
        log("interfaceMethodOrFieldRest");
        if (interfaceMethodOrFieldRest != null) return interfaceMethodOrFieldRest;
        interfaceMethodOrFieldRest = new Alternation();
            interfaceMethodOrFieldRest.add(new TrackSequence("<interfaceMethodOrFieldRest$1>")
                .add(constantDeclaratorsRest())
                .add(new Symbol(';'))
            );
            interfaceMethodOrFieldRest.add(interfaceMethodDeclaratorRest());
        return interfaceMethodOrFieldRest;
    }

    /** MethodDeclaratorRest:
     *      FormalParameters BracketsOpt[throwsQualifiedIdentifierList]( MethodBody |;  )
     */
    public Parser methodDeclaratorRest() {
        log("methodDeclaratorRest");
        if (methodDeclaratorRest != null) return methodDeclaratorRest;
        methodDeclaratorRest = new TrackSequence("<methodDeclaratorRest>");
            methodDeclaratorRest.add(formalParameters());
            methodDeclaratorRest.add(bracketsOpt());
            methodDeclaratorRest.add(zeroOrOne(new TrackSequence("<methodDeclaratorRest$1>")
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            methodDeclaratorRest.add(new Alternation()
                .add(methodBody())
                .add(new Symbol(';'))
            );
        return methodDeclaratorRest;
    }

    /** VoidMethodDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList] ( MethodBody |;  )
     */
    public Parser voidMethodDeclaratorRest() {
        log("voidMethodDeclaratorRest");
        if (voidMethodDeclaratorRest != null) return voidMethodDeclaratorRest;
        voidMethodDeclaratorRest = new TrackSequence("<voidMethodDeclaratorRest>");
            voidMethodDeclaratorRest.add(formalParameters());
            voidMethodDeclaratorRest.add(zeroOrOne(new TrackSequence("<voidMethodDeclaratorRest$1>")
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            voidMethodDeclaratorRest.add(new Alternation()
                .add(methodBody())
                .add(new Symbol(';'))
            );
        return voidMethodDeclaratorRest;
    }

    /** InterfaceMethodDeclaratorRest:
     *      FormalParameters BracketsOpt [throws QualifiedIdentifierList];
     */
    public Parser interfaceMethodDeclaratorRest() {
        log("interfaceMethodDeclaratorRest");
        if (interfaceMethodDeclaratorRest != null) return interfaceMethodDeclaratorRest;
        interfaceMethodDeclaratorRest = new TrackSequence("<interfaceMethodDeclaratorRest>");
            interfaceMethodDeclaratorRest.add(formalParameters());
            interfaceMethodDeclaratorRest.add(bracketsOpt());
            interfaceMethodDeclaratorRest.add(zeroOrOne(new TrackSequence("<interfaceMethodDeclaratorRest$1>")
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
        return interfaceMethodDeclaratorRest;
    }

    /** InterfaceGenericMethodDecl:
     *      TypeParameters Type Identifier InterfaceMethodDeclaratorRest
     */
    public Parser interfaceGenericMethodDecl() {
        log("interfaceGenericMethodDecl");
        if (interfaceGenericMethodDecl != null) return interfaceGenericMethodDecl;
        interfaceGenericMethodDecl = new TrackSequence("<interfaceGenericMethodDecl>");
            interfaceGenericMethodDecl.add(typeParameters());
            interfaceGenericMethodDecl.add(type());
            interfaceGenericMethodDecl.add(identifier());
            interfaceGenericMethodDecl.add(interfaceMethodDeclaratorRest());
        return interfaceGenericMethodDecl;
    }

    /** VoidInterfaceMethodDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList];
     */
    public Parser voidInterfaceMethodDeclaratorRest() {
        log("voidInterfaceMethodDeclaratorRest");
        if (voidInterfaceMethodDeclaratorRest != null) return voidInterfaceMethodDeclaratorRest;
        voidInterfaceMethodDeclaratorRest = new TrackSequence("<voidInterfaceMethodDeclaratorRest>");
            voidInterfaceMethodDeclaratorRest.add(formalParameters());
            voidInterfaceMethodDeclaratorRest.add(zeroOrOne(new TrackSequence("<voidInterfaceMethodDeclaratorRest$1>")
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            voidInterfaceMethodDeclaratorRest.add(new Symbol(';'));
        return voidInterfaceMethodDeclaratorRest;
    }

    /** ConstructorDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList] MethodBody
     */
    public Parser constructorDeclaratorRest() {
        log("constructorDeclaratorRest");
        if (constructorDeclaratorRest != null) return constructorDeclaratorRest;
        constructorDeclaratorRest = new TrackSequence("<constructorDeclaratorRest>");
            constructorDeclaratorRest.add(formalParameters());
            constructorDeclaratorRest.add(zeroOrOne(new TrackSequence("<constructorDeclaratorRest$1>")
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            constructorDeclaratorRest.add(methodBody());
        return constructorDeclaratorRest;
    }

    /** QualifiedIdentifierList:
     *      QualifiedIdentifier { ,   QualifiedIdentifier}
     */
    public Parser qualifiedIdentifierList() {
        log("qualifiedIdentifierList");
        if (qualifiedIdentifierList != null) return qualifiedIdentifierList;
        qualifiedIdentifierList = new TrackSequence("<qualifiedIdentifierList>");
        qualifiedIdentifierList.add(qualifiedIdentifier());
        qualifiedIdentifierList.add(new Repetition(new TrackSequence("<qualifiedIdentifierList$1>")
                .add(new Symbol(','))
                .add(qualifiedIdentifier())
            ));
        return qualifiedIdentifierList;
    }

    /** FormalParameters:
     *      ( [FormalParameterDecls])
     */
    public Parser formalParameters() {
        log("formalParameters");
        if (formalParameters != null) return formalParameters;
        formalParameters = new TrackSequence("<formalParameters>");
            formalParameters.add(new Symbol('('));
            formalParameters.add(zeroOrOne(formalParameterDecls()));
            formalParameters.add(new Symbol(')'));
        return formalParameters;
    }

    /** FormalParameterDecls:
     *      [final] [Annotations] Type FormalParameterDeclsRest]
     * todo - is last parameter optional!!!????
     */
    public Parser formalParameterDecls() {
        log("formalParameterDecls");
        if (formalParameterDecls != null) return formalParameterDecls;
        formalParameterDecls = new TrackSequence("<formalParameterDecls>");
            formalParameterDecls.add(zeroOrOne(new Literal("final")));
            formalParameterDecls.add(zeroOrOne(annotations()));
            formalParameterDecls.add(type());
            formalParameterDecls.add(formalParameterDeclsRest());
        return formalParameterDecls;
    }

    /** FormalParameterDeclsRest:
     *      VariableDeclaratorId [, FormalParameterDecls]
     *      ... VariableDeclaratorId
     */
    public Parser formalParameterDeclsRest() {
        log("formalParameterDeclsRest");
        if (formalParameterDeclsRest != null) return formalParameterDeclsRest;
        formalParameterDeclsRest = new Alternation();
        formalParameterDeclsRest.add(new TrackSequence("<formalParameterDeclsRest$1>")
                .add(variableDeclaratorId())
                .add(zeroOrOne(new TrackSequence("<formalParameterDeclsRest$2>")
                    .add(new Symbol(','))
                    .add(formalParameterDecls())
                ))
            );
        formalParameterDeclsRest.add(new TrackSequence("<formalParameterDeclsRest$3>")
                .add(new Symbol("..."))  //todo - this doesn't seem right :-)
                .add(variableDeclaratorId())
            );
        return formalParameterDeclsRest;
    }

    /** MethodBody:
     *      Block
     */
    public Parser methodBody() {
        log("methodBody");
        if (methodBody != null) return methodBody;
        methodBody = block();
        return methodBody;
    }


    // -=-=-=-=-=-=-=-=-=- END OF JLS CHAPTER 18 -=-=-=-=-=-=-=-=-






    // ------ JLSv3¤8.1.1 ------
    /** ClassModifiers:
     *      ClassModifier
     *      ClassModifiers ClassModifier
     */
    public Parser classModifiers() {
        log("classModifiers");
        if (classModifiers != null) return classModifiers;
        classModifiers = oneOrMore(classModifier());
        /*classModifiers = new Alternation();
        classModifiers.add(classModifier());
        classModifiers.add(new TrackSequence("<classModifiers$1>")
                .add(classModifiers())
                .add(classModifier())
            );*/
        return classModifiers;
    }
    /** ClassModifier: one of
     *      Annotation public protected private
     *      abstract static final strictfp
     */
    public Parser classModifier() {
        log("classModifier");
        if (classModifier != null) return classModifier;
        classModifier = new Alternation("<class modifier>");
            classModifier.add(annotation());
            classModifier.add(new Literal("public"));
            classModifier.add(new Literal("protected"));
            classModifier.add(new Literal("private"));
            classModifier.add(new Literal("abstract"));
            classModifier.add(new Literal("static"));
            classModifier.add(new Literal("final"));
            classModifier.add(new Literal("strictfp"));
        return classModifier;
    }

    // ------ JLSv3¤8.1.6 ------
    /** ClassBodyDeclarations:
     *      ClassBodyDeclaration
     *      ClassBodyDeclarations ClassBodyDeclaration
     */
    public Parser classBodyDeclarations() {
        log("classBodyDeclarations");
        if (classBodyDeclarations != null) return classBodyDeclarations;
        classBodyDeclarations = new Alternation();
            classBodyDeclarations.add(classBodyDeclaration());
            classBodyDeclarations.add(new TrackSequence("<classBodyDeclarations$1>")
                .add(classBodyDeclarations())
                .add(classBodyDeclaration())
            );
        return classBodyDeclarations;
    }

    // ------ JLSv3¤8.4.1 ------
    /** FormalParameter:
     *      VariableModifiers Type VariableDeclaratorId
     */
    public Parser formalParameter() {
        log("formalParameter");
        if (formalParameter != null) return formalParameter;
        formalParameter = new TrackSequence("<formalParameter>");
            formalParameter.add(variableModifiers());
            formalParameter.add(type());
            formalParameter.add(variableDeclaratorId());
        return formalParameter;
    }

    /** VariableModifiers:
     *      VariableModifier
     *      VariableModifiers VariableModifier
     */
    public Parser variableModifiers() {
        log("variableModifiers");
        if (variableModifiers != null) return variableModifiers;
        variableModifiers = new Alternation();
            variableModifiers.add(variableModifier());
            variableModifiers.add(new TrackSequence("<variableModifiers$1>")
                .add(variableModifiers())
                .add(variableModifier())
            );
        return variableModifiers;
    }

    /** VariableModifier: one of
     *      final Annotation
     */
    public Parser variableModifier() {
        log("variableModifier");
        if (variableModifier != null) return variableModifier;
        variableModifier = new Alternation();
            variableModifier.add(new Literal("final"));
            variableModifier.add(annotation());
        return variableModifier;
    }

    // ---------- JLSv3¤9.1.1 -------------
    /** InterfaceModifiers:
     *      InterfaceModifier
     *      InterfaceModifiers InterfaceModifier
     */
    public Parser interfaceModifiers() {
        log("interfaceModifiers");
        if (interfaceModifiers != null) return interfaceModifiers;
        interfaceModifiers = new Alternation();
            interfaceModifiers.add(interfaceModifier());
            interfaceModifiers.add(new TrackSequence("<interfaceModifiers$1>")
                .add(interfaceModifiers())
                .add(interfaceModifier())
            );
        return interfaceModifiers;
    }

    /** InterfaceModifier: one of
     *      Annotation public protected private
     *      abstract static strictfp
     */
    public Parser interfaceModifier() {
        log("interfaceModifier");
        if (interfaceModifier != null) return interfaceModifier;
        interfaceModifier = new Alternation();
            interfaceModifier.add(annotation());
            interfaceModifier.add(new Literal("public"));
            interfaceModifier.add(new Literal("protected"));
            interfaceModifier.add(new Literal("private"));
            interfaceModifier.add(new Literal("abstract"));
            interfaceModifier.add(new Literal("static"));
            interfaceModifier.add(new Literal("strictfp"));
        return interfaceModifier;
    }

    // ---------- JLSv3¤9.3 ---------------
    /** ConstantDeclaration:
     *      ConstantModifiers opt Type VariableDeclarators ;
     */
    public Parser constantDeclaration() {
        log("constantDeclaration");
        if (constantDeclaration != null) return constantDeclaration;
        constantDeclaration = new TrackSequence("<constantDeclaration>");
            constantDeclaration.add(zeroOrOne(constantModifiers()));
            constantDeclaration.add(type());
            constantDeclaration.add(variableDeclarators());
            constantDeclaration.add(new Symbol(';'));
        return constantDeclaration;
    }

    /** ConstantModifiers:
     *      ConstantModifier
     *      ConstantModifier ConstantModifers
     */
    public Parser constantModifiers() {
        log("constantModifiers");
        if (constantModifiers != null) return constantModifiers;
        constantModifiers = new Alternation();
            constantModifiers.add(constantModifier());
            constantModifiers.add(new TrackSequence("<constantModifiers$1>")
                .add(constantModifier())
                .add(constantModifiers())
            );
        return constantModifiers;
    }

    /** ConstantModifier: one of
     *      Annotation public static final
     */
    public Parser constantModifier() {
        log("constantModifier");
        if (constantModifier != null) return constantModifier;
        constantModifier = new Alternation();
            constantModifier.add(annotation());
            constantModifier.add(new Literal("public"));
            constantModifier.add(new Literal("static"));
            constantModifier.add(new Literal("final"));
        return constantModifier;
    }

    // ---------- JLSv3¤9.4 ---------------
    /** AbstractMethodModifiers:
     *      AbstractMethodModifier
     *      AbstractMethodModifiers AbstractMethodModifier
     */
    public Parser abstractMethodModifiers() {
        log("abstractMethodModifiers");
        if (abstractMethodModifiers != null) return abstractMethodModifiers;
        abstractMethodModifiers = new Alternation();
            abstractMethodModifiers.add(abstractMethodModifier());
            abstractMethodModifiers.add(new TrackSequence("<abstractMethodModifiers$1>")
                .add(abstractMethodModifiers())
                .add(abstractMethodModifier())
            );
        return abstractMethodModifiers;
    }

    /** AbstractMethodModifier: one of
     *      Annotation public abstract
     */
    public Parser abstractMethodModifier() {
        log("abstractMethodModifier");
        if (abstractMethodModifier != null) return abstractMethodModifier;
        abstractMethodModifier = new Alternation();
            abstractMethodModifier.add(annotation());
            abstractMethodModifier.add(new Literal("public"));
            abstractMethodModifier.add(new Literal("abstract"));
        return abstractMethodModifier;
    }

    // ---------- JLSv3¤9.7 ---------------
    /** Annotations:
     *      Annotation
     *      Annotations Annotation
     */
    public Parser annotations() {
        log("annotations");
        if (annotations != null) return annotations;
        annotations = new Alternation();
        //jrr - todo - line below just junk
        annotations.add(new Word());
        //jrr - todo - line above just junk
        /*annotations.add(annotation());
        annotations.add(new TrackSequence("<annotations$1>")
                .add(annotations())
                .add(annotation())
            );*/
        return annotations;
    }

    /** Annotation:
     *      NormalAnnotation
     *      MarkerAnnotation
     *      SingleElementAnnotation
     */
    public Parser annotation() {
        log("annotation");
        if (annotation != null) return annotation;
        annotation = new Alternation();
        //jrr - todo - line below just junk
        annotation.add(new Word());
        //jrr - todo - line above just junk
        /*annotation.add(normalAnnotation());
        annotation.add(markerAnnotation());
        annotation.add(singleElementAnnotation());*/
        return annotation;
    }

    /** NormalAnnotation:
     *      @ TypeName ( ElementValuePairs opt )
     */
    public Parser normalAnnotation() {
        log("normalAnnotation");
        if (normalAnnotation != null) return normalAnnotation;
        normalAnnotation = new TrackSequence("<normalAnnotation>");
            normalAnnotation.add(new Symbol('@'));
            normalAnnotation.add(typeName());
            normalAnnotation.add(new Symbol('('));
            normalAnnotation.add(zeroOrOne(elementValuePairs()));
            normalAnnotation.add(new Symbol(')'));
        return normalAnnotation;
    }

    /** ElementValuePairs:
     *      ElementValuePair
     *      ElementValuePairs , ElementValuePair
     */
    public Parser elementValuePairs() {
        log("elementValuePairs");
        if (elementValuePairs != null) return elementValuePairs;
        elementValuePairs = new Alternation();
        elementValuePairs.add(elementValuePair());
        elementValuePairs.add(new TrackSequence("<elementValuePairs$1>")
                .add(elementValuePairs())
                .add(new Symbol(','))
                .add(elementValuePair())
            );
        return elementValuePairs;
    }

    /** ElementValuePair:
     *      Identifier = ElementValue
     */
    public Parser elementValuePair() {
        log("elementValuePair");
        if (elementValuePair != null) return elementValuePair;
        elementValuePair = new TrackSequence("<elementValuePair>");
            elementValuePair.add(identifier());
            elementValuePair.add(new Symbol('='));
            elementValuePair.add(elementValue());
        return elementValuePair;
    }

    /** ElementValue:
     *      ConditionalExpression
     *      Annotation
     *      ElementValueArrayInitializer
     */
    public Parser elementValue() {
        log("elementValue");
        if (elementValue != null) return elementValue;
        elementValue = new Alternation();
        elementValue.add(conditionalExpression());
        elementValue.add(annotation());
        elementValue.add(elementValueArrayInitializer());
        return elementValue;
    }

    /** ElementValueArrayInitializer:
     *      { ElementValuesopt ,opt }
     */
    public Parser elementValueArrayInitializer() {
        log("elementValueArrayInitializer");
        if (elementValueArrayInitializer != null) return elementValueArrayInitializer;
        elementValueArrayInitializer = new TrackSequence("<elementValueArrayInitializer>");
            elementValueArrayInitializer.add(new Symbol('{'));
            elementValueArrayInitializer.add(zeroOrOne(elementValues()));
            elementValueArrayInitializer.add(zeroOrOne(new Symbol(',')));
            elementValueArrayInitializer.add(new Symbol('}'));
        return elementValueArrayInitializer;
    }

    /** ElementValues:
     *      ElementValue
     *      ElementValues , ElementValue
     */
    public Parser elementValues() {
        log("elementValues");
        if (elementValues != null) return elementValues;
        elementValues = new Alternation();
        elementValues.add(elementValue());
        elementValues.add(new TrackSequence("<elementValues$1>")
                .add(elementValues())
                .add(new Symbol(','))
                .add(elementValue())
            );
        return elementValues;
    }

    /** MarkerAnnotation:
     *      @ TypeName
     */
    public Parser markerAnnotation() {
        log("markerAnnotation");
        if (markerAnnotation != null) return markerAnnotation;
        markerAnnotation = new TrackSequence("<markerAnnotation>");
            markerAnnotation.add(new Symbol('@'));
            markerAnnotation.add(typeName());
        return markerAnnotation;
    }

    /** SingleElementAnnotation:
     *      @ TypeName ( ElementValue )
     */
    public Parser singleElementAnnotation() {
        log("singleElementAnnotation");
        if (singleElementAnnotation != null) return singleElementAnnotation;
        singleElementAnnotation = new TrackSequence("<singleElementAnnotation>");
            singleElementAnnotation.add(new Symbol('@'));
            singleElementAnnotation.add(typeName());
            singleElementAnnotation.add(new Symbol('('));
            singleElementAnnotation.add(elementValue());
            singleElementAnnotation.add(new Symbol(')'));
        return singleElementAnnotation;
    }

    // -------- JLSv3¤15.9 -----------
    /** ArgumentList:
     *      Expression
     *      ArgumentList ,Expression
     */
    public Parser argumentList() {
        log("argumentList");
        if (argumentList != null) return argumentList;
        argumentList = new Alternation();
        argumentList.add(expression());
        argumentList.add(new TrackSequence("<argumentList$1>")
                .add(argumentList())
                .add(new Symbol(','))
                .add(expression())
            );
        return argumentList;
    }


    // todo ------- typos and abbreviations in spec ------------
    public Parser typeName() {
        log("typeName");
        if (typeName != null) return typeName;
        typeName = type();
        return typeName;
    }

    public Parser expr() {
        log("expr");
        if (expr != null) return expr;
        expr = expression();
        return expr;
    }

    public Parser expressionStatement() {
        log("expressionStatement");
        if (expressionStatement != null) return expressionStatement;
        expressionStatement = statementExpression();
        return expressionStatement;
    }

    // Annotations refers to ¤15.25, but this is replaced with Expressions
    // todo - conditionalexpressions in annotations may not work, or too much might be allowed in annotations...
    public Parser conditionalExpression() {
        log("conditionalExpression");
        if (conditionalExpression != null) return conditionalExpression;
        conditionalExpression = expression();
        return conditionalExpression;
    }


    // todo ---------------------------------------




    public Parser nullLiteral() {
        log("nullLiteral");
        if (nullLiteral != null) return nullLiteral;
        //todo
        nullLiteral = new Word();
        return nullLiteral;
    }

    public Parser booleanLiteral() {
        log("booleanLiteral");
        if (booleanLiteral != null) return booleanLiteral;
        //todo
        booleanLiteral = new Word();
        return booleanLiteral;
    }

    public Parser stringLiteral() {
        log("stringLiteral");
        if (stringLiteral != null) return stringLiteral;
        //todo
        stringLiteral = new Word();
        return stringLiteral;
    }

    public Parser characterLiteral() {
        log("characterLiteral");
        if (characterLiteral != null) return characterLiteral;
        //todo
        characterLiteral = new Word();
        return characterLiteral;
    }

    public Parser floatingPointLiteral() {
        log("floatingPointLiteral");
        if (floatingPointLiteral != null) return floatingPointLiteral;
        //todo
        floatingPointLiteral = new Word();
        return floatingPointLiteral;
    }

    public Parser integerLiteral() {
        log("integerLiteral");
        if (integerLiteral != null) return integerLiteral;
        //todo
        integerLiteral = new Word();
        return integerLiteral;
    }

    // The following singletons are used to prevent left recursion in grammar
    protected Sequence qualifiedIdentifier;
    protected Alternation literal;
    protected Alternation assignmentOperator;
    protected Sequence typeArguments;
    protected Alternation typeArgument;
    protected Sequence rawType;
    protected Parser statementExpression;
    protected Parser constantExpression;
    protected Sequence expression1;
    protected Parser expression1Rest;
    protected Sequence expression2;
    protected Alternation expression2Rest;
    protected Alternation infixOp;
    protected Alternation primary;
    protected Alternation identifierSuffix;
    protected Sequence explicitGenericInvocation;
    protected Sequence nonWildcardTypeArguments;
    protected Alternation explicitGenericInvocationSuffix;
    protected Alternation prefixOp;
    protected Alternation postfixOp;
    protected Alternation selector;
    protected Alternation superSuffix;
    protected Alternation basicType;
    protected Parser argumentsOpt;
    protected Sequence arguments;
    protected Repetition bracketsOpt;
    protected Sequence creator;
    protected Sequence createdName;
    protected Sequence innerCreator;
    protected Sequence arrayCreatorRest;
    protected Sequence classCreatorRest;
    protected Alternation variableInitializer;
    protected Sequence parExpression;
    protected Repetition blockStatements;
    protected Alternation blockStatement;
    protected Sequence localVariableDeclarationStatement;
    protected Alternation statement;
    protected Sequence catches;
    protected Sequence catchClause;
    protected Repetition switchBlockStatementGroups;
    protected Sequence switchBlockStatementGroup;
    protected Alternation switchLabel;
    protected Repetition moreStatementExpressions;
    protected Alternation forControl;
    protected Alternation forControlRest;
    protected Sequence forUpdate;
    protected Repetition modifiersOpt;
    protected Alternation modifier;
    protected Sequence variableDeclarators;
    protected Sequence variableDeclaratorsRest;
    protected Sequence constantDeclaratorsRest;
    protected Sequence constantDeclarator;
    protected Sequence variableDeclaratorRest;
    protected Sequence constantDeclaratorRest;
    protected Sequence variableDeclaratorId;
    protected Sequence compilationUnit;
    protected Sequence importDeclaration;
    protected Alternation typeDeclaration;
    protected Sequence normalClassDeclaration;
    protected Sequence typeParameters;
    protected Sequence typeParameter;
    protected Sequence bound;
    protected Sequence enumDeclaration;
    protected Sequence enumBody;
    protected Alternation enumConstants;
    protected Sequence enumConstant;
    protected Sequence enumArguments;
    protected Sequence enumBodyDeclarations;
    protected Alternation interfaceDeclaration;
    protected Sequence normalInterfaceDeclaration;
    protected Sequence typeList;
    protected Sequence annotationTypeDeclaration;
    protected Sequence annotationTypeBody;
    protected Alternation annotationTypeElementDeclarations;
    protected Alternation annotationTypeElementDeclaration;
    protected Sequence defaultValue;
    protected Sequence classBody;
    protected Sequence interfaceBody;
    protected Alternation classBodyDeclaration;
    protected Alternation memberDecl;
    protected Sequence genericMethodOrConstructorDecl;
    protected Alternation genericMethodOrConstructorRest;
    protected Sequence methodOrFieldDecl;
    protected Alternation methodOrFieldRest;
    protected Alternation interfaceBodyDeclaration;
    protected Alternation interfaceMemberDecl;
    protected Sequence interfaceMethodOrFieldDecl;
    protected Alternation interfaceMethodOrFieldRest;
    protected Sequence methodDeclaratorRest;
    protected Sequence voidMethodDeclaratorRest;
    protected Sequence interfaceMethodDeclaratorRest;
    protected Sequence interfaceGenericMethodDecl;
    protected Sequence voidInterfaceMethodDeclaratorRest;
    protected Sequence constructorDeclaratorRest;
    protected Sequence formalParameters;
    protected Sequence formalParameterDecls;
    protected Parser methodBody;
    protected Alternation classModifier;
    protected Alternation classBodyDeclarations;
    protected Sequence formalParameter;
    protected Alternation variableModifiers;
    protected Alternation variableModifier;
    protected Alternation interfaceModifiers;
    protected Alternation interfaceModifier;
    protected Sequence constantDeclaration;
    protected Alternation constantModifiers;
    protected Alternation constantModifier;
    protected Alternation abstractMethodModifiers;
    protected Alternation abstractMethodModifier;
    protected Sequence normalAnnotation;
    protected Sequence elementValuePair;
    protected Sequence elementValueArrayInitializer;
    protected Sequence markerAnnotation;
    protected Sequence singleElementAnnotation;
    protected Sequence expression;
    protected Alternation type;
    protected Alternation expression3;
    protected Sequence arrayInitializer;
    protected Sequence variableDeclarator;
    protected Alternation classDeclaration;
    protected Alternation annotations;
    protected Alternation annotation;
    protected Alternation elementValue;
    protected Alternation elementValues;
    protected Alternation elementValuePairs;
    protected Alternation formalParameterDeclsRest;
    protected Sequence qualifiedIdentifierList;
    protected Sequence classOrInterfaceDeclaration;
    protected Sequence block;
    protected Parser classModifiers;
    protected Alternation argumentList;
    protected Parser typeName;
    protected Parser expr;
    protected Parser expressionStatement;
    protected Parser conditionalExpression;
    protected Parser nullLiteral;
    protected Parser booleanLiteral;
    protected Parser stringLiteral;
    protected Parser characterLiteral;
    protected Parser floatingPointLiteral;
    protected Parser integerLiteral;

}

