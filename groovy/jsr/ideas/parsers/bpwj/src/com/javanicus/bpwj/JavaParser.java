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

    protected Alternation alternation(String name) {
        return new Alternation(name);
    }

    protected Sequence sequence(String name) {
        return new TrackSequence(name);
    }

    protected Repetition repetition(Parser parser1) {
        return new Repetition(parser1);
    }

    protected Parser zeroOrOne(Parser parser1) {
        return alternation("zero or one")
                .add(new Empty())
                .add(parser1)
            ;
    }

    protected Parser oneOrMore(Parser parser1) {
        return sequence("one or more " + parser1.getName())
            .add(parser1)
            .add(repetition(parser1));
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
        qualifiedIdentifier = sequence("<qualifiedIdentifier>");
        qualifiedIdentifier.add(identifier());
        qualifiedIdentifier.add(repetition(
                    sequence("<qualifiedIdentifier$1>")
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
        literal = alternation("<literal>");
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
        expression = sequence("<expression>");
        expression.add(expression1());
        expression.add(zeroOrOne(
                sequence("<expression$1>")
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
        assignmentOperator = alternation("<assignmentOperator>");
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
        type = alternation("<type>");
        type.add(sequence("<type$1>")
                .add(identifier())
                .add(zeroOrOne(typeArguments()))
                .add(repetition(
                    sequence("<type$2>")
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
        typeArguments = sequence("<typeArguments>");
        typeArguments.add(new Symbol("<"));
        typeArguments.add(typeArgument());
        typeArguments.add(repetition(
                sequence("<typeArguments$1>")
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
        typeArgument = alternation("<typeArgument>");
        typeArgument.add(type());
        typeArgument.add(sequence("<typeArgument$1>")
                .add(new Symbol("?"))
                .add(zeroOrOne(
                    sequence("<typeArgument$2>")
                        .add(alternation("<typeArgument$a>")
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
        rawType = sequence("<rawType>");
        rawType.add(identifier());
        rawType.add(repetition(
                sequence("<rawType$1>")
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
        expression1 = sequence("<expression1>");
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
            sequence("<expression1Rest$1>")
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
        expression2 = sequence("<expression2>");
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
        expression2Rest = alternation("<expression2Rest$a>");
        expression2Rest.add(repetition(
                sequence("<expression2Rest$1>")
                    .add(infixOp())
                    .add(expression3())
            ));
        expression2Rest.add(sequence("<expression2Rest$2>")
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
        infixOp = alternation("<infixOp>");
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
        expression3 = alternation("<expression3>");
        expression3.add(sequence("<expression3$1>")
                .add(prefixOp())
                .add(expression3())
            );
        expression3.add(sequence("<expression3$2>")
                .add(new Symbol("("))
                .add(alternation("<expression3$a")
                    .add(expr())
                    .add(type())
                )
                .add(new Symbol(")"))
                .add(expression3())
            );
        expression3.add(sequence("<expression3$3>")
                .add(primary())
                .add(repetition(selector()))
                .add(repetition(postfixOp()))
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
        primary = alternation("<primary>");
            // ( Expression)
            primary.add(sequence("<primary$1>")
                .add(new Symbol("("))
                .add(expression())
                .add(new Symbol(")"))
            );
            // NonWildcardTypeArguments (ExplicitGenericInvocationSuffix |this Arguments)
            primary.add(sequence("<primary$2>")
                .add(nonWildcardTypeArguments())
                .add(alternation("<primary$a>")
                    .add(explicitGenericInvocationSuffix())
                    .add(new Literal("this"))
                )
            );
            // this [Arguments]
            primary.add(sequence("<primary$3>")
                .add(new Literal("this"))
                .add(zeroOrOne(arguments()))
            );
            // super SuperSuffix
            primary.add(sequence("<primary$4>")
                .add(new Literal("this"))
                .add(superSuffix())
            );
            // Literal
            primary.add(literal());
            // new Creator
            primary.add(sequence("<primary$5>")
                .add(new Literal("new"))
                .add(creator())
            );
            // Identifier {. Identifier }[ IdentifierSuffix]
            primary.add(sequence("<primary$6>")
                .add(identifier())
                .add(repetition(sequence("<primary$7>")
                    .add(new Symbol('.'))
                    .add(identifier())
                ))
                .add(zeroOrOne(identifierSuffix()))
            );
            // BasicType BracketsOpt.class
            primary.add(sequence("<primary$8>")
                .add(basicType())
                .add(bracketsOpt())
                .add(new Symbol('.'))
                .add(new Literal("class"))
            );
            // void.class
            primary.add(sequence("<primary$9>")
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
        identifierSuffix = alternation("<identifierSuffix>");

                // [ (] BracketsOpt   . class | Expression])
                identifierSuffix.add(sequence("<identifierSuffix$1>")
                        .add(new Symbol('['))
                        .add(alternation("<identifierSuffix$a")
                            .add(sequence("<identifierSuffix$1>")
                                .add(new Symbol(']'))
                                .add(bracketsOpt())
                                .add(new Symbol('.'))
                                .add(new Literal("class"))
                            )
                            .add(sequence("<identifierSuffix$2>")
                                .add(expression())
                                .add(new Symbol(']'))
                            )
                        )
                );

                // Arguments
                identifierSuffix.add(arguments());

                // .   (class | ExplicitGenericInvocation |this |super Arguments |new [NonWildcardTypeArguments] InnerCreator )
                identifierSuffix.add(sequence("<identifierSuffix$3>")
                    .add(new Symbol('.'))
                    .add(alternation("<identifierySuffix$a>")
                        .add(new Literal("class"))
                        .add(explicitGenericInvocation())
                        .add(new Literal("this"))
                        .add(sequence("<identifierSuffix$4>")
                            .add(new Literal("super"))
                            .add(arguments())
                        )
                        .add(sequence("<identifierSuffix$5>")
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
        explicitGenericInvocation = sequence("<explicitGenericInvocation>");
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
        nonWildcardTypeArguments = sequence("<nonWildcardTypeArguments>");
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
        explicitGenericInvocationSuffix = alternation("<explicitGenericInvocationSuffix$a>");
            explicitGenericInvocationSuffix.add(sequence("<explicitGenericInvocationSuffix$1>")
                .add(new Literal("super"))
                .add(superSuffix()));
            explicitGenericInvocationSuffix.add(sequence("<explicitGenericInvocationSuffix$2>")
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
        prefixOp = alternation("<prefixOp>");
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
        postfixOp = alternation("<postfixOp>");
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
        selector = alternation("<selector>");
            selector.add(sequence("<selector$1>")
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(arguments()))
            );
            selector.add(sequence("<selector$2>")
                .add(new Symbol('.'))
                .add(explicitGenericInvocation())
            );
            selector.add(sequence("<selector$3>")
                .add(new Symbol('.'))
                .add(new Literal("this"))
            );
            selector.add(sequence("<selector$4>")
                .add(new Symbol('.'))
                .add(new Literal("super"))
                .add(superSuffix())
            );
            selector.add(sequence("<selector$5>")
                .add(new Symbol('.'))
                .add(new Literal("new"))
                .add(zeroOrOne(nonWildcardTypeArguments()))
                .add(innerCreator())
            );
            selector.add(sequence("<selector$6>")
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
        superSuffix = alternation("<superSuffix>");
            superSuffix.add(arguments());
            superSuffix.add(sequence("<superSuffix$1>")
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
        basicType = alternation("<basicType>");
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
        arguments = sequence("<arguments>");
            arguments.add(new Symbol('('));
            arguments.add(zeroOrOne(sequence("<arguments$1>")
                .add(expression())
                .add(repetition(sequence("<arguments$2>")
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
        bracketsOpt = repetition(sequence("<bracketsOpt$1>")
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
        creator = sequence("<creator>");
            creator.add(zeroOrOne(nonWildcardTypeArguments()));
            creator.add(createdName());
            creator.add(alternation("<creator$a>")
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
        createdName = sequence("<createdName>");
            createdName.add(identifier());
            createdName.add(zeroOrOne(nonWildcardTypeArguments()));
            createdName.add(repetition(sequence("<createdName$1>")
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
        innerCreator = sequence("<innerCreator>");
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
        arrayCreatorRest = sequence("<arrayCreatorRest>");
            arrayCreatorRest.add(new Symbol('['));
            arrayCreatorRest.add(alternation("<arrayCreatorRest$a>")
                .add(sequence("<arrayCreatorRest$1>")
                    .add(new Symbol(']'))
                    .add(bracketsOpt())
                    .add(arrayInitializer())
                )
                .add(sequence("<arrayCreator$2>")
                    .add(expression())
                    .add(new Symbol(']'))
                    .add(repetition(sequence("<arrayCreator$3>")
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
        classCreatorRest = sequence("<classCreatorRest>");
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
        arrayInitializer = sequence("<arrayInitializer>");
        arrayInitializer.add(new Symbol("{"));
        arrayInitializer.add(zeroOrOne(sequence("<arrayInitializer$1>")
                .add(variableInitializer())
                .add(repetition(sequence("<arrayInitializer$2>")
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
        variableInitializer = alternation("<variableInitializer>");
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
        parExpression = sequence("<parExpression>");
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
        block = sequence("<block>");
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
        blockStatements = repetition(blockStatement());
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
        blockStatement = alternation("<blockStatement>");
            blockStatement.add(localVariableDeclarationStatement());
            blockStatement.add(classOrInterfaceDeclaration());
            blockStatement.add(sequence("<blockStatement$1>")
                .add(zeroOrOne(sequence("<blockStatement$2>")
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
        localVariableDeclarationStatement = sequence("<localVariableDeclarationStatement>");
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
        statement = alternation("<statement>");
            //Block
             statement.add(block());
            //assert Expression [: Expression];
            statement.add(sequence("<statement$1>")
                .add(new Literal("assert"))
                .add(expression())
                .add(zeroOrOne(
                    sequence("<statement$2>")
                        .add(new Symbol(':'))
                        .add(expression())
                ))
                .add(new Symbol(';'))
            );
            //if ParExpression Statement [else Statement]
            statement.add(sequence("<statement$3>")
                .add(new Literal("if"))
                .add(parExpression())
                .add(statement())
                .add(zeroOrOne(
                    sequence("<statement$4>")
                        .add(new Literal("else"))
                        .add(statement())
                ))
            );
            //for (ForControl) Statement
            statement.add(sequence("<statement$5>")
                .add(new Literal("for"))
                .add(new Symbol('('))
                .add(forControl())
                .add(new Symbol(')'))
                .add(statement())
            );
            //while ParExpression Statement
            statement.add(sequence("<statement$6>")
                .add(new Literal("while"))
                .add(parExpression())
                .add(statement())
            );
            //do Statement while ParExpression ;
            statement.add(sequence("<statement$7>")
                .add(new Literal("do"))
                .add(statement())
                .add(new Literal("while"))
                .add(parExpression())
                .add(new Symbol(';'))
            );
            //try Block ( Catches | [Catches] finally Block )
            statement.add(sequence("<statement$8>")
                .add(new Literal("try"))
                .add(block())
                .add(alternation("<statement$a>")
                    .add(catches())
                    .add(sequence("<statement$9>")
                        .add(zeroOrOne(catches()))
                        .add(new Literal("finally"))
                        .add(block())
                    )
                )
            );
            //switch ParExpression{ SwitchBlockStatementGroups}
            statement.add(sequence("<statement$10>")
                .add(new Literal("switch"))
                .add(parExpression())
                .add(new Symbol('{'))
                .add(switchBlockStatementGroups())
                .add(new Symbol('}'))
            );
            //synchronized ParExpression Block
            statement.add(sequence("<statement$11>")
                .add(new Literal("synchronized"))
                .add(parExpression())
                .add(block())
            );
            //return [Expression];
            statement.add(sequence("<statement$12>")
                .add(new Literal("return"))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
            );
            //throw Expression ;
            statement.add(sequence("<statement$13>")
                .add(new Literal("throw"))
                .add(expression())
                .add(new Symbol(';'))
            );
            //break [Identifier]
            statement.add(sequence("<statement$14>")
                .add(new Literal("break"))
                .add(zeroOrOne(identifier()))
            );
            //continue [Identifier]
            statement.add(sequence("<statement$15>")
                .add(new Literal("continue"))
                .add(zeroOrOne(identifier()))
            );
            //;
            statement.add(new Symbol(';'));
            //ExpressionStatement
            statement.add(expressionStatement());
            //Identifier :   Statement
            statement.add(sequence("<statement$16>")
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
        catches = sequence("<catches>");
            catches.add(catchClause());
            catches.add(repetition(catchClause()));
        return catches;
    }

    /** CatchClause:
     *      catch( FormalParameter) Block
     */
    public Parser catchClause() {
        log("catchClause");
        if (catchClause != null) return catchClause;
        catchClause = sequence("<catchClause>");
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
        switchBlockStatementGroups = repetition(switchBlockStatementGroup());
        return switchBlockStatementGroups;
    }

    /** SwitchBlockStatementGroup:
     *      SwitchLabel BlockStatements
     */
    public Parser switchBlockStatementGroup() {
        log("switchBlockStatementGroup");
        if (switchBlockStatementGroup != null) return switchBlockStatementGroup;
        switchBlockStatementGroup = sequence("<switchBlockStatementGroup>");
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
        switchLabel = alternation("<switchLabel>");
            switchLabel.add(sequence("<switchLabel$1>")
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
        moreStatementExpressions = repetition(sequence("<moreStatementExpressions$1>")
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
        forControl = alternation("<forControl>");
            forControl.add(sequence("<forControl$1>")
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            forControl.add(sequence("<forControl$2>")
                .add(statementExpression())
                .add(moreStatementExpressions())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            forControl.add(sequence("<forControl$3>")
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
        forControlRest = alternation("<forControlRest>");
            forControlRest.add(sequence("<forControlRest$1>")
                .add(variableDeclaratorsRest())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            forControlRest.add(sequence("<forControlRest$2>")
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
        forUpdate = sequence("<forUpdate>");
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
        modifiersOpt = repetition(modifier());
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
        modifier = alternation("<modifier>");
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
        variableDeclarators = sequence("<variableDeclarators>");
            variableDeclarators.add(variableDeclarator());
            variableDeclarators.add(repetition(sequence("<variableDeclarators$1>")
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
        variableDeclaratorsRest = sequence("<variableDeclaratorsRest>");
            variableDeclaratorsRest.add(variableDeclaratorRest());
            variableDeclaratorsRest.add(repetition(sequence("<variableDeclaratorsRest$1>")
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
        constantDeclaratorsRest = sequence("<constantDeclaratorsRest>");
            constantDeclaratorsRest.add(constantDeclaratorRest());
            constantDeclaratorsRest.add(repetition(sequence("<constantDeclaratorsRest$1>")
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
        variableDeclarator = sequence("<variableDeclarator>");
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
        constantDeclarator = sequence("<constantDeclarator>");
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
        variableDeclaratorRest = sequence("<variableDeclaratorRest>");
            variableDeclaratorRest.add(bracketsOpt());
            variableDeclaratorRest.add(zeroOrOne(sequence("<variableDeclaratorRest$1>")
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
        constantDeclaratorRest = sequence("<constantDeclaratorRest>");
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
        variableDeclaratorId =  sequence("<variableDeclaratorId>");
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
        compilationUnit = sequence("<compilationUnit>");
            compilationUnit.add(zeroOrOne(
                sequence("<compilationUnit$1>")
                    //todo .add(zeroOrOne(annotations()))
                    .add(new Literal("package"))
                    .add(qualifiedIdentifier())
                    .add(new Symbol(';'))
            ));
            compilationUnit.add(repetition(importDeclaration()));
            compilationUnit.add(repetition(typeDeclaration()));
        return compilationUnit;
    }

    /** ImportDeclaration:
     *      import [static] Identifier { .   Identifier } [  .*   ];
     */
    public Parser importDeclaration() {
        log("importDeclaration");
        if (importDeclaration != null) return importDeclaration;
        importDeclaration = sequence("<importDeclaration>");
            importDeclaration.add(new Literal("import"));
            importDeclaration.add(zeroOrOne(new Literal("static")));
            importDeclaration.add(identifier());
            importDeclaration.add(repetition(sequence("<importDeclaration$1>")
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
        typeDeclaration = alternation("<typeDeclaration>");
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
        classOrInterfaceDeclaration = sequence("<classOrInterfaceDeclaration>");
        classOrInterfaceDeclaration.add(modifiersOpt());
        classOrInterfaceDeclaration.add(alternation("<classOrInterfaceDeclaration$a>")
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
        classDeclaration = alternation("<classDeclaration>");
        classDeclaration.add(normalClassDeclaration());
        //todo classDeclaration.add(enumDeclaration());
        return classDeclaration;
    }

    /** NormalClassDeclaration:
     *      class Identifier TypeParameters opt [extends Type] [implements TypeList] ClassBody
     */
    public Parser normalClassDeclaration() {
        log("normalClassDeclaration");
        if (normalClassDeclaration != null) return normalClassDeclaration;
        normalClassDeclaration = sequence("<normalClassDeclaration>");
            normalClassDeclaration.add(new Literal("class"));
            normalClassDeclaration.add(identifier());
            normalClassDeclaration.add(zeroOrOne(typeParameters()));
            normalClassDeclaration.add(zeroOrOne(sequence("<normalClassDeclaration$1>")
                .add(new Literal("extends"))
                .add(type())
            ));
            normalClassDeclaration.add(zeroOrOne(sequence("<normalClassDeclaration$2>")
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
        typeParameters = sequence("<typeParameters>");
            typeParameters.add(new Symbol('<'));
            typeParameters.add(typeParameter());
            typeParameters.add(repetition(sequence("<typeParameters$1>")
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
        typeParameter = sequence("<typeParameter>");
            typeParameter.add(identifier());
            typeParameter.add(zeroOrOne(sequence("<typeParameter$1>")
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
        bound = sequence("<bound>");
            bound.add(type());
            bound.add(repetition(sequence("<bound$1>")
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
        enumDeclaration = sequence("<enumDeclaration>");
            enumDeclaration.add(zeroOrOne(classModifiers()));
            enumDeclaration.add(new Literal("enum"));
            enumDeclaration.add(identifier());
            enumDeclaration.add(zeroOrOne(sequence("<enumDeclaration$1>")
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
        enumBody = sequence("<enumBody>");
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
        enumConstants = alternation("<enumConstants>");
            enumConstants.add(enumConstant());
            enumConstants.add(sequence("<enumConstants$1>")
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
        enumConstant = sequence("<enumConstant>");
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
        enumArguments = sequence("<enumArguments>");
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
        enumBodyDeclarations = sequence("<enumBodyDeclarations>");
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
        interfaceDeclaration = alternation("<interfaceDeclaration>");
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
        normalInterfaceDeclaration = sequence("<normalInterfaceDeclaration>");
            normalInterfaceDeclaration.add(new Literal("interface"));
            normalInterfaceDeclaration.add(identifier());
            normalInterfaceDeclaration.add(zeroOrOne(typeParameters()));
            normalInterfaceDeclaration.add(zeroOrOne(sequence("<normalInterfaceDeclaration$1>")
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
        typeList = sequence("<typeList>");
            typeList.add(type());
            typeList.add(repetition(sequence("<typeList$1>")
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
        annotationTypeDeclaration = sequence("<annotationTypeDeclaration>");
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
        annotationTypeBody = sequence("<annotationTypeBody>");
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
        annotationTypeElementDeclarations = alternation("<annotationTypeElementDeclarations>");
            annotationTypeElementDeclarations.add(annotationTypeElementDeclaration());
            annotationTypeElementDeclarations.add(sequence("<annotationTypeElementDeclarations$1>")
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
        annotationTypeElementDeclaration = alternation("<annotationTypeElementDeclaration>");
            annotationTypeElementDeclaration.add(sequence("<annotationTypeElementDeclaration$1>")
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
        defaultValue = sequence("<defaultValue>");
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
        classBody = sequence("<classBody>");
            classBody.add(new Symbol('{'));
            //todo classBody.add(repetition(classBodyDeclaration()));
            classBody.add(new Symbol('}'));
        return classBody;
    }

    /** InterfaceBody:
     *      { {InterfaceBodyDeclaration}}
     */
    public Parser interfaceBody() {
        log("interfaceBody");
        if (interfaceBody != null) return interfaceBody;
        interfaceBody = sequence("<interfaceBody>");
            interfaceBody.add(new Symbol('{'));
            interfaceBody.add(repetition(interfaceBodyDeclaration()));
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
        classBodyDeclaration = alternation("<classBodyDeclaration>");
            classBodyDeclaration.add(new Symbol(';'));
            classBodyDeclaration.add(sequence("<classBodyDeclaration$1>")
                .add(zeroOrOne(new Literal("static")))
                .add(block())
            );
            classBodyDeclaration.add(sequence("<classBodyDeclaration$2>")
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
        memberDecl = alternation("<memberDecl>");
            memberDecl.add(genericMethodOrConstructorDecl());
            memberDecl.add(methodOrFieldDecl());
            memberDecl.add(sequence("<memberDecl$1>")
                .add(new Literal("void"))
                .add(identifier())
                .add(methodDeclaratorRest())
            );
            memberDecl.add(sequence("<memberDecl$2>")
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
        genericMethodOrConstructorDecl = sequence("<genericMethodOrConstructorDecl>");
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
        genericMethodOrConstructorRest = alternation("<genericMethodOrConstructorRest>");
            genericMethodOrConstructorRest.add(sequence("<genericMethodOrConstructorRest$1>")
                .add(type())
                .add(identifier())
                .add(methodDeclaratorRest())
            );
            genericMethodOrConstructorRest.add(sequence("<genericMethodOrConstructorRest$2>")
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
        methodOrFieldDecl = sequence("<methodOrFieldDecl>");
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
        methodOrFieldRest = alternation("<methodOrFieldRest>");
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
        interfaceBodyDeclaration = alternation("<interfaceBodyDeclaration>");
            interfaceBodyDeclaration.add(new Symbol(';'));
            interfaceBodyDeclaration.add(sequence("<interfaceBodyDeclaration$1>")
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
        interfaceMemberDecl = alternation("<interfaceMemberDecl>");
            interfaceMemberDecl.add(interfaceMethodOrFieldDecl());
            interfaceMemberDecl.add(interfaceGenericMethodDecl());
            interfaceMemberDecl.add(sequence("<interfaceMemberDecl$1>")
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
        interfaceMethodOrFieldDecl = sequence("<interfaceMethodOrFieldDecl>");
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
        interfaceMethodOrFieldRest = alternation("<interfaceMethodOrFieldRest>");
            interfaceMethodOrFieldRest.add(sequence("<interfaceMethodOrFieldRest$1>")
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
        methodDeclaratorRest = sequence("<methodDeclaratorRest>");
            methodDeclaratorRest.add(formalParameters());
            methodDeclaratorRest.add(bracketsOpt());
            methodDeclaratorRest.add(zeroOrOne(sequence("<methodDeclaratorRest$1>")
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            methodDeclaratorRest.add(alternation("<methodDeclaratorRest$a>")
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
        voidMethodDeclaratorRest = sequence("<voidMethodDeclaratorRest>");
            voidMethodDeclaratorRest.add(formalParameters());
            voidMethodDeclaratorRest.add(zeroOrOne(sequence("<voidMethodDeclaratorRest$1>")
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            voidMethodDeclaratorRest.add(alternation("<voidMethodDeclaratorRest$a>")
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
        interfaceMethodDeclaratorRest = sequence("<interfaceMethodDeclaratorRest>");
            interfaceMethodDeclaratorRest.add(formalParameters());
            interfaceMethodDeclaratorRest.add(bracketsOpt());
            interfaceMethodDeclaratorRest.add(zeroOrOne(sequence("<interfaceMethodDeclaratorRest$1>")
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
        interfaceGenericMethodDecl = sequence("<interfaceGenericMethodDecl>");
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
        voidInterfaceMethodDeclaratorRest = sequence("<voidInterfaceMethodDeclaratorRest>");
            voidInterfaceMethodDeclaratorRest.add(formalParameters());
            voidInterfaceMethodDeclaratorRest.add(zeroOrOne(sequence("<voidInterfaceMethodDeclaratorRest$1>")
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
        constructorDeclaratorRest = sequence("<constructorDeclaratorRest>");
            constructorDeclaratorRest.add(formalParameters());
            constructorDeclaratorRest.add(zeroOrOne(sequence("<constructorDeclaratorRest$1>")
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
        qualifiedIdentifierList = sequence("<qualifiedIdentifierList>");
        qualifiedIdentifierList.add(qualifiedIdentifier());
        qualifiedIdentifierList.add(repetition(sequence("<qualifiedIdentifierList$1>")
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
        formalParameters = sequence("<formalParameters>");
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
        formalParameterDecls = sequence("<formalParameterDecls>");
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
        formalParameterDeclsRest = alternation("<formalParameterDeclsRest>");
        formalParameterDeclsRest.add(sequence("<formalParameterDeclsRest$1>")
                .add(variableDeclaratorId())
                .add(zeroOrOne(sequence("<formalParameterDeclsRest$2>")
                    .add(new Symbol(','))
                    .add(formalParameterDecls())
                ))
            );
        formalParameterDeclsRest.add(sequence("<formalParameterDeclsRest$3>")
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






    // ------ altered JLSv3¤8.1.1 ------
    /** ClassModifiers:
     *      ClassModifier {ClassModifier}
     */
    public Parser classModifiers() {
        log("classModifiers");
        if (classModifiers != null) return classModifiers;
        classModifiers = oneOrMore(classModifier());
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

    // ------ altered JLSv3¤8.1.6 ------
    /** ClassBodyDeclarations:
     *      ClassBodyDeclaration {ClassBodyDeclaration}
     */
    public Parser classBodyDeclarations() {
        log("classBodyDeclarations");
        if (classBodyDeclarations != null) return classBodyDeclarations;
        classBodyDeclarations = oneOrMore(classBodyDeclaration());
        return classBodyDeclarations;
    }

    // ------ JLSv3¤8.4.1 ------
    /** FormalParameter:
     *      VariableModifiers Type VariableDeclaratorId
     */
    public Parser formalParameter() {
        log("formalParameter");
        if (formalParameter != null) return formalParameter;
        formalParameter = sequence("<formalParameter>");
            formalParameter.add(variableModifiers());
            formalParameter.add(type());
            formalParameter.add(variableDeclaratorId());
        return formalParameter;
    }

    /** ------- altered JLSv3¤8.4.1 --------
     *  VariableModifiers:
     *      VariableModifier {VariableModifier}
     */
    public Parser variableModifiers() {
        log("variableModifiers");
        if (variableModifiers != null) return variableModifiers;
        variableModifiers = oneOrMore(variableModifier());
        return variableModifiers;
    }

    /** VariableModifier: one of
     *      final Annotation
     */
    public Parser variableModifier() {
        log("variableModifier");
        if (variableModifier != null) return variableModifier;
        variableModifier = alternation("<variableModifier>");
            variableModifier.add(new Literal("final"));
            variableModifier.add(annotation());
        return variableModifier;
    }

    // ---------- altered JLSv3¤9.1.1 -------------
    /** InterfaceModifiers:
     *      InterfaceModifier {InterfaceModifier}
     */
    public Parser interfaceModifiers() {
        log("interfaceModifiers");
        if (interfaceModifiers != null) return interfaceModifiers;
        interfaceModifiers = oneOrMore(interfaceModifier());
        return interfaceModifiers;
    }

    /** InterfaceModifier: one of
     *      Annotation public protected private
     *      abstract static strictfp
     */
    public Parser interfaceModifier() {
        log("interfaceModifier");
        if (interfaceModifier != null) return interfaceModifier;
        interfaceModifier = alternation("<interfaceModifier>");
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
        constantDeclaration = sequence("<constantDeclaration>");
            constantDeclaration.add(zeroOrOne(constantModifiers()));
            constantDeclaration.add(type());
            constantDeclaration.add(variableDeclarators());
            constantDeclaration.add(new Symbol(';'));
        return constantDeclaration;
    }

    /** ---------- altered JLSv3¤9.3 ---------------
     * ConstantModifiers:
     *      ConstantModifier {ConstantModifier}
     */
    public Parser constantModifiers() {
        log("constantModifiers");
        if (constantModifiers != null) return constantModifiers;
        constantModifiers = oneOrMore(constantModifier());
        return constantModifiers;
    }

    /** ConstantModifier: one of
     *      Annotation public static final
     */
    public Parser constantModifier() {
        log("constantModifier");
        if (constantModifier != null) return constantModifier;
        constantModifier = alternation("<constantModifier>");
            constantModifier.add(annotation());
            constantModifier.add(new Literal("public"));
            constantModifier.add(new Literal("static"));
            constantModifier.add(new Literal("final"));
        return constantModifier;
    }

    // ---------- altered JLSv3¤9.4 ---------------
    /** AbstractMethodModifiers:
     *      AbstractMethodModifier {AbstractMethodModifier}
     */
    public Parser abstractMethodModifiers() {
        log("abstractMethodModifiers");
        if (abstractMethodModifiers != null) return abstractMethodModifiers;
        abstractMethodModifiers = oneOrMore(abstractMethodModifier());
        return abstractMethodModifiers;
    }

    /** AbstractMethodModifier: one of
     *      Annotation public abstract
     */
    public Parser abstractMethodModifier() {
        log("abstractMethodModifier");
        if (abstractMethodModifier != null) return abstractMethodModifier;
        abstractMethodModifier = alternation("<abstractMethodModifier>");
            abstractMethodModifier.add(annotation());
            abstractMethodModifier.add(new Literal("public"));
            abstractMethodModifier.add(new Literal("abstract"));
        return abstractMethodModifier;
    }

    // ---------- altered JLSv3¤9.7 ---------------
    /** Annotations:
     *      Annotation {Annotation}
     */
    public Parser annotations() {
        log("annotations");
        if (annotations != null) return annotations;
        annotations = oneOrMore(annotation());
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
        annotation = alternation("<annotation>");
        annotation.add(normalAnnotation());
        annotation.add(markerAnnotation());
        annotation.add(singleElementAnnotation());
        return annotation;
    }

    /** NormalAnnotation:
     *      @ TypeName ( ElementValuePairs opt )
     */
    public Parser normalAnnotation() {
        log("normalAnnotation");
        if (normalAnnotation != null) return normalAnnotation;
        normalAnnotation = sequence("<normalAnnotation>");
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
        elementValuePairs = alternation("<elementValuePairs>");
        elementValuePairs.add(elementValuePair());
        elementValuePairs.add(sequence("<elementValuePairs$1>")
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
        elementValuePair = sequence("<elementValuePair>");
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
        elementValue = alternation("<elementValue>");
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
        elementValueArrayInitializer = sequence("<elementValueArrayInitializer>");
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
        elementValues = alternation("<elementValues>");
        elementValues.add(elementValue());
        elementValues.add(sequence("<elementValues$1>")
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
        markerAnnotation = sequence("<markerAnnotation>");
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
        singleElementAnnotation = sequence("<singleElementAnnotation>");
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
        argumentList = alternation("<argumentList>");
        argumentList.add(expression());
        argumentList.add(sequence("<argumentList$1>")
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
        floatingPointLiteral = new Num();
        return floatingPointLiteral;
    }

    public Parser integerLiteral() {
        log("integerLiteral");
        if (integerLiteral != null) return integerLiteral;
        //todo
        integerLiteral = new Num();
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
    protected Parser classBodyDeclarations;
    protected Sequence formalParameter;
    protected Parser variableModifiers;
    protected Alternation variableModifier;
    protected Parser interfaceModifiers;
    protected Alternation interfaceModifier;
    protected Sequence constantDeclaration;
    protected Parser constantModifiers;
    protected Alternation constantModifier;
    protected Parser abstractMethodModifiers;
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
    protected Parser annotations;
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

