package com.javanicus.bpwj;

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

    private Parser zeroOrOne(Parser parser1) {
        return new Alternation()
                .add(new Empty())
                .add(parser1)
            ;
    }

    // ------------------ Main Grammar -------------------------

    /** Identifier:
     *      IDENTIFIER
     */
    public Parser identifier() {
        //@todo
        Parser p = new Word();
        return p;
    }

    /** QualifiedIdentifier:
     *      Identifier {. Identifier }
     */
    public Parser qualifiedIdentifier() {
        Sequence s = new TrackSequence();
        s.add(identifier());
        s.add(new Repetition(
                    new TrackSequence()
                        .add(new Symbol('.'))
                        .add(identifier())
            ));
        return s;
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
        Alternation a = new Alternation();
        a.add(integerLiteral());
        a.add(floatingPointLiteral());
        a.add(characterLiteral());
        a.add(stringLiteral());
        a.add(booleanLiteral());
        a.add(nullLiteral());
        return a;
    }

    /** Expression:
     *      Expression1 [AssignmentOperator Expression1]
     */
    public Parser expression() {
        Sequence s = new TrackSequence();
        s.add(expression1());
        s.add(zeroOrOne(
                new TrackSequence()
                    .add(assignmentOperator())
                    .add(expression1())
            ));
        return s;
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
        Alternation a = new Alternation();
        a.add(new Symbol("="));
        a.add(new Symbol("+="));
        a.add(new Symbol("-="));
        a.add(new Symbol("*="));
        a.add(new Symbol("/="));
        a.add(new Symbol("&="));
        a.add(new Symbol("|="));
        a.add(new Symbol("^="));
        a.add(new Symbol("%="));
        a.add(new Symbol("<<="));
        a.add(new Symbol(">>="));
        a.add(new Symbol(">>>="));
        return a;
    }

    /** Type:
     *      Identifier [TypeArguments]{ .   Identifier [TypeArguments]} BracketsOpt
     *      BasicType
     */
    public Parser type() {
        Alternation a = new Alternation();
        a.add(new TrackSequence()
                .add(identifier())
                .add(zeroOrOne(typeArguments()))
                .add(new Repetition(
                    new TrackSequence()
                        .add(new Symbol("."))
                        .add(identifier())
                        .add(zeroOrOne(typeArguments()))
                ))
                .add(bracketsOpt())
            );
        a.add(basicType());
        return a;
    }

    /** TypeArguments:
     *      < TypeArgument {, TypeArgument}>
     */
    public Parser typeArguments() {
        Sequence s = new TrackSequence();
        s.add(new Symbol("<"));
        s.add(typeArgument());
        s.add(new Repetition(
                new TrackSequence()
                    .add(new Symbol(","))
                    .add(typeArgument())
            ));
        return s;
    }

    /** TypeArgument:
     *      Type
     *      ? [(extends |super ) Type]
     */
    private Parser typeArgument() {
        Alternation a = new Alternation();
        a.add(type());
        a.add(new TrackSequence()
                .add(new Symbol("?"))
                .add(zeroOrOne(
                    new TrackSequence()
                        .add(new Alternation()
                            .add(new Literal("extends"))
                            .add(new Literal("super"))
                        )
                        .add(type())
                ))
            )
        ;
        return a;
    }

    /** RawType:
     *      Identifier { .   Identifier } BracketsOpt
     */
    public Parser rawType() {
        Sequence s = new TrackSequence();
        s.add(identifier());
        s.add(new Repetition(
                new TrackSequence()
                    .add(new Symbol("."))
                    .add(identifier())
            ));
        s.add(bracketsOpt());
        return s;
    }

    /** StatementExpression:
     *      Expression
     */
    public Parser statementExpression() {
        Parser p = expression();
        return p;
    }

    /** ConstantExpression:
     *      Expression
     */
    public Parser constantExpression() {
        Parser p = expression();
        return p;
    }

    /** Expression1:
     *      Expression2 [Expression1Rest]
     */
    public Parser expression1() {
        Sequence s = new TrackSequence();
        s.add(expression2());
        s.add(zeroOrOne(expression1Rest()));
        return s;
    }

    /** Expression1Rest:
     *      [ ?   Expression :   Expression1]
     */
    public Parser expression1Rest() {
        Parser p = zeroOrOne(
            new TrackSequence()
                .add(new Symbol("?"))
                .add(expression())
                .add(new Symbol(":"))
                .add(expression1())
        );
        return p;
    }

    /** Expression2 :
     *      Expression3 [Expression2Rest]
     */
    public Parser expression2() {
        Sequence s = new TrackSequence();
        s.add(expression3());
        s.add(zeroOrOne(expression2Rest()));
        return s;
    }

    /** Expression2Rest:
     *      {Infixop Expression3}
     *      Expression3 instanceof Type
     */
    public Parser expression2Rest() {
        Alternation a = new Alternation();
        a.add(new Repetition(
                new TrackSequence()
                    .add(infixOp())
                    .add(expression3())
            ));
        a.add(new TrackSequence()
                .add(expression3())
                .add(new Literal("instanceof"))
                .add(type())
            );
        return a;
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
        Alternation a = new Alternation();
        a.add(new Symbol("||"));
        a.add(new Symbol("&&"));
        a.add(new Symbol("|"));
        a.add(new Symbol("^"));
        a.add(new Symbol("&"));
        a.add(new Symbol("=="));
        a.add(new Symbol("!="));
        a.add(new Symbol("<"));
        a.add(new Symbol(">"));
        a.add(new Symbol("<="));
        a.add(new Symbol(">="));
        a.add(new Symbol("<<"));
        a.add(new Symbol(">>"));
        a.add(new Symbol(">>>"));
        a.add(new Symbol("+"));
        a.add(new Symbol("*"));
        a.add(new Symbol("/"));
        a.add(new Symbol("%"));
        return a;
    }

    /** Expression3:
     *      PrefixOp Expression3
     *      (   (Expr|Type) )   Expression3
     *      Primary {Selector} {PostfixOp}
     */
    public Parser expression3() {
        Alternation a = new Alternation();
        a.add(new TrackSequence()
                .add(prefixOp())
                .add(expression3())
            );
        a.add(new TrackSequence()
                .add(new Symbol("("))
                .add(new Alternation()
                    .add(expr())
                    .add(type())
                )
                .add(new Symbol(")"))
                .add(expression3())
            );
        a.add(new TrackSequence()
                .add(primary())
                .add(new Repetition(selector()))
                .add(new Repetition(postfixOp()))
            );
        return a;
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
        Alternation a = new Alternation();
            // ( Expression)
            a.add(new TrackSequence()
                .add(new Symbol("("))
                .add(expression())
                .add(new Symbol(")"))
            );
            // NonWildcardTypeArguments (ExplicitGenericInvocationSuffix |this Arguments)
            a.add(new TrackSequence()
                .add(nonWildcardTypeArguments())
                .add(new Alternation()
                    .add(explicitGenericInvocationSuffix())
                    .add(new Literal("this"))
                )
            );
            // this [Arguments]
            a.add(new TrackSequence()
                .add(new Literal("this"))
                .add(zeroOrOne(arguments()))
            );
            // super SuperSuffix
            a.add(new TrackSequence()
                .add(new Literal("this"))
                .add(superSuffix())
            );
            // Literal
            a.add(literal());
            // new Creator
            a.add(new TrackSequence()
                .add(new Literal("new"))
                .add(creator())
            );
            // Identifier {. Identifier }[ IdentifierSuffix]
            a.add(new TrackSequence()
                .add(identifier())
                .add(new Repetition(new TrackSequence()
                    .add(new Symbol('.'))
                    .add(identifier())
                ))
                .add(zeroOrOne(identifierSuffix()))
            );
            // BasicType BracketsOpt.class
            a.add(new TrackSequence()
                .add(basicType())
                .add(bracketsOpt())
                .add(new Symbol('.'))
                .add(new Literal("class"))
            );
            // void.class
            a.add(new TrackSequence()
                .add(new Literal("void"))
                .add(new Symbol("."))
                .add(new Literal("class"))
            );
        return a;
    }

    /** IdentifierSuffix:
     *      [ (] BracketsOpt   . class | Expression])
     *      Arguments
     *      .   (class | ExplicitGenericInvocation |this |super Arguments |new [NonWildcardTypeArguments] InnerCreator )
     */
    public Parser identifierSuffix() {
        Alternation a = new Alternation();

                // [ (] BracketsOpt   . class | Expression])
                a.add(new TrackSequence()
                        .add(new Symbol('['))
                        .add(new Alternation()
                            .add(new TrackSequence()
                                .add(new Symbol(']'))
                                .add(bracketsOpt())
                                .add(new Symbol('.'))
                                .add(new Literal("class"))
                            )
                            .add(new TrackSequence()
                                .add(expression())
                                .add(new Symbol(']'))
                            )
                        )
                );

                // Arguments
                a.add(arguments());

                // .   (class | ExplicitGenericInvocation |this |super Arguments |new [NonWildcardTypeArguments] InnerCreator )
                a.add(new TrackSequence()
                    .add(new Symbol('.'))
                    .add(new Alternation()
                        .add(new Literal("class"))
                        .add(explicitGenericInvocation())
                        .add(new Literal("this"))
                        .add(new TrackSequence()
                            .add(new Literal("super"))
                            .add(arguments())
                        )
                        .add(new TrackSequence()
                            .add(new Literal("new"))
                            .add(zeroOrOne(nonWildcardTypeArguments()))
                            .add(innerCreator())
                        )
                    )
                );
        return a;
    }

    /** ExplicitGenericInvocation:
     *      NonWildcardTypeArguments ExplicitGenericInvocationSuffix
     */
    public Parser explicitGenericInvocation() {
        Sequence s = new TrackSequence();
        s.add(nonWildcardTypeArguments());
        s.add(explicitGenericInvocationSuffix());
        return s;
    }

    /** NonWildcardTypeArguments:
     *      < TypeList>
     */
    public Parser nonWildcardTypeArguments() {
        Sequence s = new TrackSequence();
        s.add(new Symbol("<"));
        s.add(typeList());
        s.add(new Symbol(">"));
        return s;
    }

    /** ExplicitGenericInvocationSuffix:
     *      super SuperSuffix
     *      Identifier Arguments
     */
    public Parser explicitGenericInvocationSuffix() {
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(new Literal("super"))
                .add(superSuffix()));
            a.add(new TrackSequence()
                .add(identifier())
                .add(arguments())
            );
        return a;
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
        Alternation a = new Alternation();
        a.add(new Symbol("++"));
        a.add(new Symbol("--"));
        a.add(new Symbol('!'));
        a.add(new Symbol('~'));
        a.add(new Symbol('+'));
        a.add(new Symbol('-'));
        return a;
    }

    /** PostfixOp:
     *      ++
     *      --
     */
    public Parser postfixOp() {
        Alternation a = new Alternation();
        a.add(new Symbol("++"));
        a.add(new Symbol("--"));
        return a;
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
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(arguments()))
            );
            a.add(new TrackSequence()
                .add(new Symbol('.'))
                .add(explicitGenericInvocation())
            );
            a.add(new TrackSequence()
                .add(new Symbol('.'))
                .add(new Literal("this"))
            );
            a.add(new TrackSequence()
                .add(new Symbol('.'))
                .add(new Literal("super"))
                .add(superSuffix())
            );
            a.add(new TrackSequence()
                .add(new Symbol('.'))
                .add(new Literal("new"))
                .add(zeroOrOne(nonWildcardTypeArguments()))
                .add(innerCreator())
            );
            a.add(new TrackSequence()
                .add(new Symbol('['))
                .add(expression())
                .add(new Symbol(']'))
            );
        return a;
    }

    /** SuperSuffix:
     *      Arguments
     *      . Identifier [Arguments]
     */
    public Parser superSuffix() {
        Alternation a = new Alternation();
            a.add(arguments());
            a.add(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(arguments()))
            );
        return a;
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
        Alternation a = new Alternation();
            a.add(new Literal("byte"));
            a.add(new Literal("short"));
            a.add(new Literal("char"));
            a.add(new Literal("int"));
            a.add(new Literal("long"));
            a.add(new Literal("float"));
            a.add(new Literal("double"));
            a.add(new Literal("boolean"));
        return a;
    }

    /** ArgumentsOpt:
     *      [ Arguments ]
     */
    public Parser argumentsOpt() {
        Parser p = zeroOrOne(arguments());
        return p;
    }

    /** Arguments:
     *      ( [Expression {, Expression }])
     */
    public Parser arguments() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('('));
            s.add(zeroOrOne(new TrackSequence()
                .add(expression())
                .add(new Repetition(new TrackSequence()
                    .add(new Symbol(','))
                    .add(expression())
                ))
            ));
            s.add(new Symbol(')'));
        return s;
    }

    /** BracketsOpt:
     *      {[]}
     */
    public Parser bracketsOpt() {
        Repetition r = new Repetition(new TrackSequence()
            .add(new Symbol('['))
            .add(new Symbol(']'))
        );
        return r;
    }

    /** Creator:
     *      [NonWildcardTypeArguments] CreatedName ( ArrayCreatorRest  | ClassCreatorRest )
     */
    public Parser creator() {
        Sequence s = new TrackSequence();
            s.add(zeroOrOne(nonWildcardTypeArguments()));
            s.add(createdName());
            s.add(new Alternation()
                .add(arrayCreatorRest())
                .add(classCreatorRest())
            );
        return s;
    }

    /** CreatedName:
     *      Identifier [NonWildcardTypeArguments] {. Identifier [NonWildcardTypeArguments]}
     */
    public Parser createdName() {
        Sequence s = new TrackSequence();
            s.add(identifier());
            s.add(zeroOrOne(nonWildcardTypeArguments()));
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(nonWildcardTypeArguments()))
            ));
        return s;
    }

    /** InnerCreator:
     *      Identifier ClassCreatorRest
     */
    public Parser innerCreator() {
        Sequence s = new TrackSequence();
            s.add(identifier());
            s.add(classCreatorRest());
        return s;
    }

    /** ArrayCreatorRest:
     *      [ (] BracketsOpt ArrayInitializer | Expression] {[ Expression]} BracketsOpt )
     */
    public Parser arrayCreatorRest() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('['));
            s.add(new Alternation()
                .add(new TrackSequence()
                    .add(new Symbol(']'))
                    .add(bracketsOpt())
                    .add(arrayInitializer())
                )
                .add(new TrackSequence()
                    .add(expression())
                    .add(new Symbol(']'))
                    .add(new Repetition(new TrackSequence()
                        .add(new Symbol('['))
                        .add(expression())
                        .add(new Symbol(']'))
                    ))
                    .add(bracketsOpt())
                )
            );
        return s;
    }

    /** ClassCreatorRest:
     *      Arguments [ClassBody]
     */
    public Parser classCreatorRest() {
        Sequence s = new TrackSequence();
            s.add(arguments());
            s.add(zeroOrOne(classBody()));
        return s;
    }

    /** ArrayInitializer:
     *      { [VariableInitializer {, VariableInitializer} [,]]}
     */
    public Parser arrayInitializer() {
        Sequence s = new TrackSequence();
            s.add(new Symbol("{"));
            s.add(zeroOrOne(new TrackSequence()
                .add(variableInitializer())
                .add(new Repetition(new TrackSequence()
                    .add(new Symbol(","))
                    .add(variableInitializer())
                ))
                .add(zeroOrOne(new Symbol(',')))
            ));
            s.add(new Symbol("}"));
        return s;
    }

    /** VariableInitializer:
     *      ArrayInitializer
     *      Expression
     */
    public Parser variableInitializer() {
        Alternation a = new Alternation();
            a.add(arrayInitializer());
            a.add(expression());
        return a;
    }

    /** ParExpression:
     *      ( Expression)
     */
    public Parser parExpression() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('('));
            s.add(expression());
            s.add(new Symbol(')'));
        return s;
    }

    /** Block:
     *      { BlockStatements}
     */
    public Parser block() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('{'));
            s.add(blockStatements());
            s.add(new Symbol('}'));
        return s;
    }

    /** BlockStatements:
     *      { BlockStatement }
     */
    public Parser blockStatements() {
        Repetition r = new Repetition(blockStatement());
        return r;
    }

    /** BlockStatement:
     *      LocalVariableDeclarationStatement
     *      ClassOrInterfaceDeclaration
     *      [Identifier:] Statement
     */
    public Parser blockStatement() {
        Alternation a = new Alternation();
            a.add(localVariableDeclarationStatement());
            a.add(classOrInterfaceDeclaration());
            a.add(new TrackSequence()
                .add(zeroOrOne(new TrackSequence()
                    .add(identifier())
                    .add(new Symbol(":"))
                ))
                .add(statement())
            );
        return a;
    }

    /** LocalVariableDeclarationStatement:
     *      [final] Type VariableDeclarators;
     */
    public Parser localVariableDeclarationStatement() {
        Sequence s = new TrackSequence();
            s.add(zeroOrOne(new Literal("final")));
            s.add(type());
            s.add(variableDeclarators());
            s.add(new Literal(";"));
        return s;
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
        Alternation a = new Alternation();
            //Block
             a.add(block());
            //assert Expression [: Expression];
            a.add(new TrackSequence()
                .add(new Literal("assert"))
                .add(expression())
                .add(zeroOrOne(
                    new TrackSequence()
                        .add(new Symbol(':'))
                        .add(expression())
                ))
                .add(new Symbol(';'))
            );
            //if ParExpression Statement [else Statement]
            a.add(new TrackSequence()
                .add(new Literal("if"))
                .add(parExpression())
                .add(statement())
                .add(zeroOrOne(
                    new TrackSequence()
                        .add(new Literal("else"))
                        .add(statement())
                ))
            );
            //for (ForControl) Statement
            a.add(new TrackSequence()
                .add(new Literal("for"))
                .add(new Symbol('('))
                .add(forControl())
                .add(new Symbol(')'))
                .add(statement())
            );
            //while ParExpression Statement
            a.add(new TrackSequence()
                .add(new Literal("while"))
                .add(parExpression())
                .add(statement())
            );
            //do Statement while ParExpression ;
            a.add(new TrackSequence()
                .add(new Literal("do"))
                .add(statement())
                .add(new Literal("while"))
                .add(parExpression())
                .add(new Symbol(';'))
            );
            //try Block ( Catches | [Catches] finally Block )
            a.add(new TrackSequence()
                .add(new Literal("try"))
                .add(block())
                .add(new Alternation()
                    .add(catches())
                    .add(new TrackSequence()
                        .add(zeroOrOne(catches()))
                        .add(new Literal("finally"))
                        .add(block())
                    )
                )
            );
            //switch ParExpression{ SwitchBlockStatementGroups}
            a.add(new TrackSequence()
                .add(new Literal("switch"))
                .add(parExpression())
                .add(new Symbol('{'))
                .add(switchBlockStatementGroups())
                .add(new Symbol('}'))
            );
            //synchronized ParExpression Block
            a.add(new TrackSequence()
                .add(new Literal("synchronized"))
                .add(parExpression())
                .add(block())
            );
            //return [Expression];
            a.add(new TrackSequence()
                .add(new Literal("return"))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
            );
            //throw Expression ;
            a.add(new TrackSequence()
                .add(new Literal("throw"))
                .add(expression())
                .add(new Symbol(';'))
            );
            //break [Identifier]
            a.add(new TrackSequence()
                .add(new Literal("break"))
                .add(zeroOrOne(identifier()))
            );
            //continue [Identifier]
            a.add(new TrackSequence()
                .add(new Literal("continue"))
                .add(zeroOrOne(identifier()))
            );
            //;
            a.add(new Symbol(';'));
            //ExpressionStatement
            a.add(expressionStatement());
            //Identifier :   Statement
            a.add(new TrackSequence()
                .add(identifier())
                .add(new Symbol(':'))
                .add(statement())
            );
        return a;
    }

    /** Catches:
     *      CatchClause {CatchClause}
     */
    public Parser catches() {
        Sequence s = new TrackSequence();
            s.add(catchClause());
            s.add(new Repetition(catchClause()));
        return s;
    }

    /** CatchClause:
     *      catch( FormalParameter) Block
     */
    public Parser catchClause() {
        Sequence s = new TrackSequence();
            s.add(new Literal("catch"));
            s.add(new Symbol('('));
            s.add(formalParameter());
            s.add(new Symbol(')'));
            s.add(block());
        return s;
    }

    /** SwitchBlockStatementGroups:
     *      { SwitchBlockStatementGroup }
     */
    public Parser switchBlockStatementGroups() {
        Repetition r = new Repetition(switchBlockStatementGroup());
        return r;
    }

    /** SwitchBlockStatementGroup:
     *      SwitchLabel BlockStatements
     */
    public Parser switchBlockStatementGroup() {
        Sequence s = new TrackSequence();
            s.add(switchLabel());
            s.add(blockStatements());
        return s;
    }

    /** SwitchLabel:
     *      case ConstantExpression :
     *      default:
     */
    public Parser switchLabel() {
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(new Literal("case"))
                .add(constantExpression())
                .add(new Symbol(":"))
            );
            a.add(new Literal("default"));
            a.add(new Symbol(":"));
        return a;
    }

    /** MoreStatementExpressions:
     *      {, StatementExpression }
     */
    public Parser moreStatementExpressions() {
        Repetition r = new Repetition(new TrackSequence()
            .add(new Symbol(','))
            .add(statementExpression())
        );
        return r;
    }

    /** ForControl:
     *      ;   [Expression] ;   ForUpdateOpt
     *      StatementExpression MoreStatementExpressions;   [Expression]; ForUpdateOpt
     *      [final] [Annotations] Type Identifier ForControlRest
     */
    public Parser forControl() {
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(new Symbol(";"))
                .add(zeroOrOne(expression()))
                .add(new Symbol(";"))
                .add(zeroOrOne(forUpdate()))
            );
            a.add(new TrackSequence()
                .add(statementExpression())
                .add(moreStatementExpressions())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            a.add(new TrackSequence()
                .add(zeroOrOne(new Literal("final")))
                .add(zeroOrOne(annotations()))
                .add(type())
                .add(identifier())
                .add(forControlRest())
            );
        return a;
    }

    /** ForControlRest:
     *      VariableDeclaratorsRest;   [Expression] ;   ForUpdateOpt
     *      : Expression
     */
    public Parser forControlRest() {
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(variableDeclaratorsRest())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            );
            a.add(new TrackSequence()
                .add(new Symbol(':'))
                .add(expression())
            );
        return a;
    }

    /** ForUpdate:
     *      StatementExpression MoreStatementExpressions
     */
    public Parser forUpdate() {
        Sequence s = new TrackSequence();
            s.add(statementExpression());
            s.add(moreStatementExpressions());
        return s;
    }


    /** ModifiersOpt:
     *      { Modifier }
     */
    public Parser modifiersOpt() {
        Repetition r = new Repetition(modifier());
        return r;
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
        Alternation a = new Alternation();
            a.add(annotation());
            a.add(new Literal("public"));
            a.add(new Literal("protected"));
            a.add(new Literal("private"));
            a.add(new Literal("static"));
            a.add(new Literal("abstract"));
            a.add(new Literal("final"));
            a.add(new Literal("native"));
            a.add(new Literal("synchronized"));
            a.add(new Literal("transient"));
            a.add(new Literal("volatile"));
            a.add(new Literal("strictfp"));
        return a;
    }

    /** VariableDeclarators:
     *      VariableDeclarator {,   VariableDeclarator }
     */
    public Parser variableDeclarators() {
        Sequence s = new TrackSequence();
            s.add(variableDeclarators());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(variableDeclarator())
            ));
        return s;
    }

    /** VariableDeclaratorsRest:
     *      VariableDeclaratorRest {,   VariableDeclarator }
     */
    public Parser variableDeclaratorsRest() {
        Sequence s = new TrackSequence();
            s.add(variableDeclaratorsRest());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(variableDeclarator())
            ));
        return s;
    }

    /** ConstantDeclaratorsRest:
     *      ConstantDeclaratorRest {,   ConstantDeclarator }
     */
    public Parser constantDeclaratorsRest() {
        Sequence s = new TrackSequence();
            s.add(constantDeclaratorsRest());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(constantDeclarator())
            ));
        return s;
    }

    /** VariableDeclarator:
     *      Identifier VariableDeclaratorRest
     */
    public Parser variableDeclarator() {
        Sequence s = new TrackSequence();
            s.add(identifier());
            s.add(variableDeclaratorsRest());
        return s;
    }

    /** ConstantDeclarator:
     *      Identifier ConstantDeclaratorRest
     */
    public Parser constantDeclarator() {
        Sequence s = new TrackSequence();
            s.add(identifier());
            s.add(constantDeclaratorsRest());
        return s;
    }

    /** VariableDeclaratorRest:
     *      BracketsOpt [ =   VariableInitializer]
     */
    public Parser variableDeclaratorRest() {
        Sequence s = new TrackSequence();
            s.add(bracketsOpt());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Symbol('='))
                .add(variableInitializer())
            ));
        return s;
    }

    /** ConstantDeclaratorRest:
     *      BracketsOpt =   VariableInitializer
     */
    public Parser constantDeclaratorRest() {
        Sequence s = new TrackSequence();
            s.add(bracketsOpt());
            s.add(new Symbol('='));
            s.add(variableInitializer());
        return s;
    }

    /** VariableDeclaratorId:
     *      Identifier BracketsOpt
     */
    public Parser variableDeclaratorId() {
        Sequence s =  new TrackSequence();
            s.add(identifier());
            s.add(bracketsOpt());
        return s;
    }

    /** CompilationUnit:
     *      [Annotations opt package QualifiedIdentifier ;  ] {ImportDeclaration} {TypeDeclaration}
     */
    public Parser compilationUnit() {
        Sequence s = new TrackSequence();
            s.add(zeroOrOne(
                new TrackSequence()
                    .add(zeroOrOne(annotations()))
                    .add(new Literal("package"))
                    .add(qualifiedIdentifier())
                    .add(new Symbol(';'))
            ));
            s.add(new Repetition(importDeclaration()));
            s.add(new Repetition(typeDeclaration()));
        return s;
    }

    /** ImportDeclaration:
     *      import [static] Identifier { .   Identifier } [  .*   ];
     */
    public Parser importDeclaration() {
        Sequence s = new TrackSequence();
            s.add(new Literal("import"));
            s.add(zeroOrOne(new Literal("static")));
            s.add(identifier());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
            ));
            s.add(zeroOrOne(new Symbol(".*")));
            s.add(new Symbol(';'));
        return s;
    }

    /** TypeDeclaration:
     *      ClassOrInterfaceDeclaration
     *      ;
     */
    public Parser typeDeclaration() {
        Alternation a = new Alternation();
            a.add(classOrInterfaceDeclaration());
            a.add(new Symbol(';'));
        return a;
    }

    /** ClassOrInterfaceDeclaration:
     *      ModifiersOpt (ClassDeclaration | InterfaceDeclaration)
     */
    public Parser classOrInterfaceDeclaration() {
        Sequence s = new TrackSequence();
            s.add(modifiersOpt());
            s.add(new Alternation()
                .add(classDeclaration())
                .add(interfaceDeclaration())
            );
        return s;
    }

    /** ClassDeclaration:
     *      NormalClassDeclaration           // note error in JLS¤18 makes this look like a Sequence not an Alternation
     *      EnumDeclaration
     */
    public Parser classDeclaration() {
        Alternation a = new Alternation();
            a.add(normalClassDeclaration());
            a.add(enumDeclaration());
        return a;
    }

    /** NormalClassDeclaration:
     *      class Identifier TypeParameters opt [extends Type] [implements TypeList] ClassBody
     */
    public Parser normalClassDeclaration() {
        Sequence s = new TrackSequence();
            s.add(new Literal("class"));
            s.add(identifier());
            s.add(zeroOrOne(typeParameters()));
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("extends"))
                .add(type())
            ));
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("implements"))
                .add(typeList())
            ));
            s.add(classBody());
        return s;
    }

    /** TypeParameters:
     *      < TypeParameter {, TypeParameter}>
     */
    public Parser typeParameters() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('<'));
            s.add(typeParameter());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(typeParameter())
            ));
            s.add(new Symbol('>'));
        return s;
    }

    /** TypeParameter:
     *      Identifier [extendsBound]
     */
    public Parser typeParameter() {
        Sequence s = new TrackSequence();
            s.add(identifier());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("extends"))
                .add(bound())
            ));
        return s;
    }

    /** Bound:
     *      Type {&Type}
     */
    public Parser bound() {
        Sequence s = new TrackSequence();
            s.add(type());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol('&'))
                .add(type())
            ));
        return s;
    }

    /** EnumDeclaration:
     *      ClassModifiers opt enum Identifier[implements TypeList] EnumBody
     */
    public Parser enumDeclaration() {
        Sequence s = new TrackSequence();
            s.add(zeroOrOne(classModifiers()));
            s.add(new Literal("enum"));
            s.add(identifier());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("implements"))
                .add(typeList())
            ));
            s.add(enumBody());
        return s;
    }

    /** EnumBody:
     *      { EnumConstants opt ,opt EnumBodyDeclarations opt }
     */
    public Parser enumBody() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('{'));
            s.add(zeroOrOne(enumConstants()));
            s.add(zeroOrOne(new Symbol(',')));
            s.add(zeroOrOne(enumBodyDeclarations()));
            s.add(new Symbol('}'));
        return s;
    }

    /** EnumConstants:
     *      EnumConstant
     *      EnumConstants , EnumConstant
     */
    public Parser enumConstants() {
        Alternation a = new Alternation();
            a.add(enumConstant());
            a.add(new TrackSequence()
                .add(enumConstants())
                .add(new Symbol(','))
                .add(enumConstant())
            );
        return a;
    }

    /** EnumConstant:
     *      Annotations Identifier EnumArguments opt ClassBody opt
     *
     * N.B. JRR changed to EnumArguments - check with JLSv3 spec revisions (bug reported in grammar)
     */
    public Parser enumConstant() {
        Sequence s = new TrackSequence();
            s.add(annotations());
            s.add(identifier());
            s.add(zeroOrOne(enumArguments()));
            s.add(zeroOrOne(classBody()));
        return s;
    }

    /** EnumArguments:
     *      ( ArgumentListopt )
     *
     * N.B. JRR changed to EnumArguments - check with JLSv3 spec revisions (bug reported in grammar)
     */
    public Parser enumArguments() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('('));
            s.add(zeroOrOne(argumentList()));
            s.add(new Symbol(')'));
        return s;
    }

    /** EnumBodyDeclarations:
     *      ; ClassBodyDeclarationsopt
     *
     * todo that semicolon looks a bit dodgy... jez
     */
    public Parser enumBodyDeclarations() {
        Sequence s = new TrackSequence();
            s.add(new Symbol(';'));
            s.add(zeroOrOne(classBodyDeclarations()));
        return s;
    }

    /** InterfaceDeclaration:
     *      NormalInterfaceDeclaration
     *      AnnotationTypeDeclaration
     */
    public Parser interfaceDeclaration() {
        Alternation a = new Alternation();
            a.add(normalInterfaceDeclaration());
            a.add(annotationTypeDeclaration());
        return a;
    }

    /** NormalInterfaceDeclaration:
     *      interface Identifier TypeParameters opt[extends TypeList] InterfaceBody
     */
    public Parser normalInterfaceDeclaration() {
        Sequence s = new TrackSequence();
            s.add(new Literal("interface"));
            s.add(identifier());
            s.add(zeroOrOne(typeParameters()));
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("extends"))
                .add(typeList())
            ));
            s.add(interfaceBody());
        return s;
    }

    /** TypeList:
     *      Type { ,   Type}
     */
    public Parser typeList() {
        Sequence s = new TrackSequence();
            s.add(type());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(type())
            ));
        return s;
    }

    /** AnnotationTypeDeclaration:
     *      InterfaceModifiers opt @ interface Identifier AnnotationTypeBody
     */
    public Parser annotationTypeDeclaration() {
        Sequence s = new TrackSequence();
            s.add(zeroOrOne(interfaceModifiers()));
            s.add(new Symbol('@'));
            s.add(new Literal("interface")); // @todo is this correct?
            s.add(identifier());
            s.add(annotationTypeBody());
        return s;
    }

    /** AnnotationTypeBody:
     *      { AnnotationTypeElementDeclarations }
     */
    public Parser annotationTypeBody() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('{'));
            s.add(annotationTypeElementDeclarations());
            s.add(new Symbol('}'));
        return s;
    }

    /** AnnotationTypeElementDeclarations:
     *      AnnotationTypeElementDeclaration
     *      AnnotationTypeElementDeclarations AnnotationTypeElementDeclaration
     */
    public Parser annotationTypeElementDeclarations() {
        Alternation a = new Alternation();
            a.add(annotationTypeElementDeclaration());
            a.add(new TrackSequence()
                .add(annotationTypeElementDeclarations())
                .add(annotationTypeElementDeclaration())
            );
        return a;
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
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(zeroOrOne(abstractMethodModifiers()))
                .add(type())
                .add(identifier())
                .add(new Symbol('('))   // @todo - is this right
                .add(new Symbol(')'))
                .add(zeroOrOne(defaultValue()))
                .add(new Symbol(';'))
            );
            a.add(constantDeclaration());
            a.add(classDeclaration());
            a.add(interfaceDeclaration());
            a.add(enumDeclaration());
            a.add(annotationTypeDeclaration());
            a.add(new Symbol(';'));
        return a;
    }

    /** DefaultValue:
     *      default ElementValue
     */
    public Parser defaultValue() {
        Sequence s = new TrackSequence();
            s.add(new Literal("default"));
            s.add(elementValue());
        return s;
    }

    /** ClassBody:
     *      { {ClassBodyDeclaration}}
     */
    public Parser classBody() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('{'));
            s.add(new Repetition(classBodyDeclaration()));
            s.add(new Symbol('}'));
        return s;
    }

    /** InterfaceBody:
     *      { {InterfaceBodyDeclaration}}
     */
    public Parser interfaceBody() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('{'));
            s.add(new Repetition(interfaceBodyDeclaration()));
            s.add(new Symbol('}'));
        return s;
    }

    /** ClassBodyDeclaration:
     *      ;
     *      [static] Block
     *      ModifiersOpt MemberDecl
     */
    public Parser classBodyDeclaration() {
        Alternation a = new Alternation();
            a.add(new Symbol(';'));
            a.add(new TrackSequence()
                .add(zeroOrOne(new Literal("static")))
                .add(block())
            );
            a.add(new TrackSequence()
                .add(modifiersOpt())
                .add(memberDecl())
            );
        return a;
    }

    /** MemberDecl:
     *      GenericMethodOrConstructorDecl
     *      MethodOrFieldDecl
     *      void Identifier MethodDeclaratorRest
     *      Identifier ConstructorDeclaratorRest
     *      ClassOrInterfaceDeclaration
     */
    public Parser memberDecl() {
        Alternation a = new Alternation();
            a.add(genericMethodOrConstructorDecl());
            a.add(methodOrFieldDecl());
            a.add(new TrackSequence()
                .add(new Literal("void"))
                .add(identifier())
                .add(methodDeclaratorRest())
            );
            a.add(new TrackSequence()
                .add(identifier())
                .add(constructorDeclaratorRest())
            );
            a.add(classOrInterfaceDeclaration());
        return a;
    }

    /** GenericMethodOrConstructorDecl:
     *      TypeParameters GenericMethodOrConstructorRest
     */
    public Parser genericMethodOrConstructorDecl() {
        Sequence s = new TrackSequence();
            s.add(typeParameters());
            s.add(genericMethodOrConstructorRest());
        return s;
    }

    /** GenericMethodOrConstructorRest:
     *      Type Identifier MethodDeclaratorRest
     *      Identifier ConstructorDeclaratorRest
     */
    public Parser genericMethodOrConstructorRest() {
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(type())
                .add(identifier())
                .add(methodDeclaratorRest())
            );
            a.add(new TrackSequence()
                .add(identifier())
                .add(constructorDeclaratorRest())
            );
        return a;
    }

    /** MethodOrFieldDecl:
     *      Type Identifier MethodOrFieldRest
     */
    public Parser methodOrFieldDecl() {
        Sequence s = new TrackSequence();
            s.add(type());
            s.add(identifier());
            s.add(methodOrFieldRest());
        return s;
    }

    /** MethodOrFieldRest:
     *      VariableDeclaratorRest
     *      MethodDeclaratorRest
     */
    public Parser methodOrFieldRest() {
        Alternation a = new Alternation();
            a.add(variableDeclaratorRest());
            a.add(methodDeclaratorRest());
        return a;
    }

    /** InterfaceBodyDeclaration:
     *      ;
     *      ModifiersOpt InterfaceMemberDecl
     */
    public Parser interfaceBodyDeclaration() {
        Alternation a = new Alternation();
            a.add(new Symbol(';'));
            a.add(new TrackSequence()
                .add(modifiersOpt())
                .add(interfaceMemberDecl())
            );
        return a;
    }

    /** InterfaceMemberDecl:
     *      InterfaceMethodOrFieldDecl
     *      InterfaceGenericMethodDecl
     *      void Identifier VoidInterfaceMethodDeclaratorRest
     *      ClassOrInterfaceDeclaration
     */
    public Parser interfaceMemberDecl() {
        Alternation a = new Alternation();
            a.add(interfaceMethodOrFieldDecl());
            a.add(interfaceGenericMethodDecl());
            a.add(new TrackSequence()
                .add(new Literal("void"))
                .add(identifier())
                .add(voidInterfaceMethodDeclaratorRest())
            );
            a.add(classOrInterfaceDeclaration());
        return a;
    }

    /** InterfaceMethodOrFieldDecl:
     *      Type Identifier InterfaceMethodOrFieldRest
     */
    public Parser interfaceMethodOrFieldDecl() {
        Sequence s = new TrackSequence();
            s.add(type());
            s.add(identifier());
            s.add(interfaceMethodOrFieldRest());
        return s;
    }

    /** InterfaceMethodOrFieldRest:
     *      ConstantDeclaratorsRest;
     *      InterfaceMethodDeclaratorRest
     */
    public Parser interfaceMethodOrFieldRest() {
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(constantDeclaratorsRest())
                .add(new Symbol(';'))
            );
            a.add(interfaceMethodDeclaratorRest());
        return a;
    }

    /** MethodDeclaratorRest:
     *      FormalParameters BracketsOpt[throwsQualifiedIdentifierList]( MethodBody |;  )
     */
    public Parser methodDeclaratorRest() {
        Sequence s = new TrackSequence();
            s.add(formalParameters());
            s.add(bracketsOpt());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            s.add(new Alternation()
                .add(methodBody())
                .add(new Symbol(';'))
            );
        return s;
    }

    /** VoidMethodDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList] ( MethodBody |;  )
     */
    public Parser voidMethodDeclaratorRest() {
        Sequence s = new TrackSequence();
            s.add(formalParameters());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            s.add(new Alternation()
                .add(methodBody())
                .add(new Symbol(';'))
            );
        return s;
    }

    /** InterfaceMethodDeclaratorRest:
     *      FormalParameters BracketsOpt [throws QualifiedIdentifierList];
     */
    public Parser interfaceMethodDeclaratorRest() {
        Sequence s = new TrackSequence();
            s.add(formalParameters());
            s.add(bracketsOpt());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
        return s;
    }

    /** InterfaceGenericMethodDecl:
     *      TypeParameters Type Identifier InterfaceMethodDeclaratorRest
     */
    public Parser interfaceGenericMethodDecl() {
        Sequence s = new TrackSequence();
            s.add(typeParameters());
            s.add(type());
            s.add(identifier());
            s.add(interfaceMethodDeclaratorRest());
        return s;
    }

    /** VoidInterfaceMethodDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList];
     */
    public Parser voidInterfaceMethodDeclaratorRest() {
        Sequence s = new TrackSequence();
            s.add(formalParameters());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            s.add(new Symbol(';'));
        return s;
    }

    /** ConstructorDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList] MethodBody
     */
    public Parser constructorDeclaratorRest() {
        Sequence s = new TrackSequence();
            s.add(formalParameters());
            s.add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ));
            s.add(methodBody());
        return s;
    }

    /** QualifiedIdentifierList:
     *      QualifiedIdentifier { ,   QualifiedIdentifier}
     */
    public Parser qualifiedIdentifierList() {
        Sequence s = new TrackSequence();
            s.add(qualifiedIdentifier());
            s.add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(qualifiedIdentifier())
            ));
        return s;
    }

    /** FormalParameters:
     *      ( [FormalParameterDecls])
     */
    public Parser formalParameters() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('('));
            s.add(zeroOrOne(formalParameterDecls()));
            s.add(new Symbol(')'));
        return s;
    }

    /** FormalParameterDecls:
     *      [final] [Annotations] Type FormalParameterDeclsRest]
     * todo - is last parameter optional!!!????
     */
    public Parser formalParameterDecls() {
        Sequence s = new TrackSequence();
            s.add(zeroOrOne(new Literal("final")));
            s.add(zeroOrOne(annotations()));
            s.add(type());
            s.add(formalParameterDeclsRest());
        return s;
    }

    /** FormalParameterDeclsRest:
     *      VariableDeclaratorId [, FormalParameterDecls]
     *      ... VariableDeclaratorId
     */
    public Parser formalParameterDeclsRest() {
        Alternation a = new Alternation();
            a.add(new TrackSequence()
                .add(variableDeclaratorId())
                .add(zeroOrOne(new TrackSequence()
                    .add(new Symbol(','))
                    .add(formalParameterDecls())
                ))
            );
            a.add(new TrackSequence()
                .add(new Symbol("..."))  //todo - this doesn't seem right :-)
                .add(variableDeclaratorId())
            );
        return a;
    }

    /** MethodBody:
     *      Block
     */
    public Parser methodBody() {
        Parser p = block();
        return p;
    }


    // -=-=-=-=-=-=-=-=-=- END OF JLS CHAPTER 18 -=-=-=-=-=-=-=-=-






    // ------ JLSv3¤8.1.1 ------
    /** ClassModifiers:
     *      ClassModifier
     *      ClassModifiers ClassModifier
     */
    public Parser classModifiers() {
        Alternation a = new Alternation();
            a.add(classModifier());
            a.add(new TrackSequence()
                .add(classModifiers())
                .add(classModifier())
            );
        return a;
    }
    /** ClassModifier: one of
     *      Annotation public protected private
     *      abstract static final strictfp
     */
    public Parser classModifier() {
        Alternation a = new Alternation();
            a.add(annotation());
            a.add(new Literal("public"));
            a.add(new Literal("protected"));
            a.add(new Literal("private"));
            a.add(new Literal("abstract"));
            a.add(new Literal("static"));
            a.add(new Literal("final"));
            a.add(new Literal("strictfp"));
        return a;
    }

    // ------ JLSv3¤8.1.6 ------
    /** ClassBodyDeclarations:
     *      ClassBodyDeclaration
     *      ClassBodyDeclarations ClassBodyDeclaration
     */
    public Parser classBodyDeclarations() {
        Alternation a = new Alternation();
            a.add(classBodyDeclaration());
            a.add(new TrackSequence()
                .add(classBodyDeclarations())
                .add(classBodyDeclaration())
            );
        return a;
    }

    // ------ JLSv3¤8.4.1 ------
    /** FormalParameter:
     *      VariableModifiers Type VariableDeclaratorId
     */
    public Parser formalParameter() {
        Sequence s = new TrackSequence();
            s.add(variableModifiers());
            s.add(type());
            s.add(variableDeclaratorId());
        return s;
    }

    /** VariableModifiers:
     *      VariableModifier
     *      VariableModifiers VariableModifier
     */
    public Parser variableModifiers() {
        Alternation a = new Alternation();
            a.add(variableModifier());
            a.add(new TrackSequence()
                .add(variableModifiers())
                .add(variableModifier())
            );
        return a;
    }

    /** VariableModifier: one of
     *      final Annotation
     */
    public Parser variableModifier() {
        Alternation a = new Alternation();
            a.add(new Literal("final"));
            a.add(annotation());
        return a;
    }

    // ---------- JLSv3¤9.1.1 -------------
    /** InterfaceModifiers:
     *      InterfaceModifier
     *      InterfaceModifiers InterfaceModifier
     */
    public Parser interfaceModifiers() {
        Alternation a = new Alternation();
            a.add(interfaceModifier());
            a.add(new TrackSequence()
                .add(interfaceModifiers())
                .add(interfaceModifier())
            );
        return a;
    }

    /** InterfaceModifier: one of
     *      Annotation public protected private
     *      abstract static strictfp
     */
    public Parser interfaceModifier() {
        Alternation a = new Alternation();
            a.add(annotation());
            a.add(new Literal("public"));
            a.add(new Literal("protected"));
            a.add(new Literal("private"));
            a.add(new Literal("abstract"));
            a.add(new Literal("static"));
            a.add(new Literal("strictfp"));
        return a;
    }

    // ---------- JLSv3¤9.3 ---------------
    /** ConstantDeclaration:
     *      ConstantModifiers opt Type VariableDeclarators ;
     */
    public Parser constantDeclaration() {
        Sequence s = new TrackSequence();
            s.add(zeroOrOne(constantModifiers()));
            s.add(type());
            s.add(variableDeclarators());
            s.add(new Symbol(';'));
        return s;
    }

    /** ConstantModifiers:
     *      ConstantModifier
     *      ConstantModifier ConstantModifers
     */
    public Parser constantModifiers() {
        Alternation a = new Alternation();
            a.add(constantModifier());
            a.add(new TrackSequence()
                .add(constantModifier())
                .add(constantModifiers())
            );
        return a;
    }

    /** ConstantModifier: one of
     *      Annotation public static final
     */
    public Parser constantModifier() {
        Alternation a = new Alternation();
            a.add(annotation());
            a.add(new Literal("public"));
            a.add(new Literal("static"));
            a.add(new Literal("final"));
        return a;
    }

    // ---------- JLSv3¤9.4 ---------------
    /** AbstractMethodModifiers:
     *      AbstractMethodModifier
     *      AbstractMethodModifiers AbstractMethodModifier
     */
    public Parser abstractMethodModifiers() {
        Alternation a = new Alternation();
            a.add(abstractMethodModifier());
            a.add(new TrackSequence()
                .add(abstractMethodModifiers())
                .add(abstractMethodModifier())
            );
        return a;
    }

    /** AbstractMethodModifier: one of
     *      Annotation public abstract
     */
    public Parser abstractMethodModifier() {
        Alternation a = new Alternation();
            a.add(annotation());
            a.add(new Literal("public"));
            a.add(new Literal("abstract"));
        return a;
    }

    // ---------- JLSv3¤9.7 ---------------
    /** Annotations:
     *      Annotation
     *      Annotations Annotation
     */
    public Parser annotations() {
        Alternation a = new Alternation();
            a.add(annotation());
            a.add(new TrackSequence()
                .add(annotations())
                .add(annotation())
            );
        return a;
    }

    /** Annotation:
     *      NormalAnnotation
     *      MarkerAnnotation
     *      SingleElementAnnotation
     */
    public Parser annotation() {
        Alternation a = new Alternation();
            a.add(normalAnnotation());
            a.add(markerAnnotation());
            a.add(singleElementAnnotation());
        return a;
    }

    /** NormalAnnotation:
     *      @ TypeName ( ElementValuePairs opt )
     */
    public Parser normalAnnotation() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('@'));
            s.add(typeName());
            s.add(new Symbol('('));
            s.add(zeroOrOne(elementValuePairs()));
            s.add(new Symbol(')'));
        return s;
    }

    /** ElementValuePairs:
     *      ElementValuePair
     *      ElementValuePairs , ElementValuePair
     */
    public Parser elementValuePairs() {
        Alternation a = new Alternation();
            a.add(elementValuePair());
            a.add(new TrackSequence()
                .add(elementValuePairs())
                .add(new Symbol(','))
                .add(elementValuePair())
            );
        return a;
    }

    /** ElementValuePair:
     *      Identifier = ElementValue
     */
    public Parser elementValuePair() {
        Sequence s = new TrackSequence();
            s.add(identifier());
            s.add(new Symbol('='));
            s.add(elementValue());
        return s;
    }

    /** ElementValue:
     *      ConditionalExpression
     *      Annotation
     *      ElementValueArrayInitializer
     */
    public Parser elementValue() {
        Alternation a = new Alternation();
            a.add(conditionalExpression());
            a.add(annotation());
            a.add(elementValueArrayInitializer());
        return a;
    }

    /** ElementValueArrayInitializer:
     *      { ElementValuesopt ,opt }
     */
    public Parser elementValueArrayInitializer() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('{'));
            s.add(zeroOrOne(elementValues()));
            s.add(zeroOrOne(new Symbol(',')));
            s.add(new Symbol('}'));
        return s;
    }

    /** ElementValues:
     *      ElementValue
     *      ElementValues , ElementValue
     */
    public Parser elementValues() {
        Alternation a = new Alternation();
            a.add(elementValue());
            a.add(new TrackSequence()
                .add(elementValues())
                .add(new Symbol(','))
                .add(elementValue())
            );
        return a;
    }

    /** MarkerAnnotation:
     *      @ TypeName
     */
    public Parser markerAnnotation() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('@'));
            s.add(typeName());
        return s;
    }

    /** SingleElementAnnotation:
     *      @ TypeName ( ElementValue )
     */
    public Parser singleElementAnnotation() {
        Sequence s = new TrackSequence();
            s.add(new Symbol('@'));
            s.add(typeName());
            s.add(new Symbol('('));
            s.add(elementValue());
            s.add(new Symbol(')'));
        return s;
    }

    // -------- JLSv3¤15.9 -----------
    /** ArgumentList:
     *      Expression
     *      ArgumentList ,Expression
     */
    public Parser argumentList() {
        Alternation a = new Alternation();
            a.add(expression());
            a.add(new TrackSequence()
                .add(argumentList())
                .add(new Symbol(','))
                .add(expression())
            );
        return a;
    }


    // todo ------- typos and abbreviations in spec ------------
    public Parser typeName() {
        Parser p = type();
        return p;
    }

    public Parser expr() {
        Parser p = expression();
        return p;
    }

    public Parser expressionStatement() {
        Parser p = statementExpression();
        return p;
    }

    // Annotations refers to ¤15.25, but this is replaced with Expressions
    // todo - conditionalexpressions in annotations may not work, or too much might be allowed in annotations...
    public Parser conditionalExpression() {
        Parser p = expression();
        return p;
    }


    // todo ---------------------------------------




    private Parser nullLiteral() {
        //todo
        Parser p = new Word();
        return p;
    }

    private Parser booleanLiteral() {
        //todo
        Parser p = new Word();
        return p;
    }

    private Parser stringLiteral() {
        //todo
        Parser p = new Word();
        return p;
    }

    private Parser characterLiteral() {
        //todo
        Parser p = new Word();
        return p;
    }

    private Parser floatingPointLiteral() {
        //todo
        Parser p = new Word();
        return p;
    }

    private Parser integerLiteral() {
        //todo
        Parser p = new Word();
        return p;
    }
}

