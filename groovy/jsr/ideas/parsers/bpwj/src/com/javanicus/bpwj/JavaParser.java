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
        return new Word();
    }

    /** QualifiedIdentifier:
     *      Identifier {. Identifier }
     */
    public Parser qualifiedIdentifier() {
        return new TrackSequence()
            .add(identifier())
            .add(new Repetition(
                    new TrackSequence()
                        .add(new Symbol('.'))
                        .add(identifier())
            ));
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
        return new Alternation()
            .add(integerLiteral())
            .add(floatingPointLiteral())
            .add(characterLiteral())
            .add(stringLiteral())
            .add(booleanLiteral())
            .add(nullLiteral());
    }

    /** Expression:
     *      Expression1 [AssignmentOperator Expression1]
     */
    public Parser expression() {
        return new TrackSequence()
            .add(expression1())
            .add(zeroOrOne(
                new TrackSequence()
                    .add(assignmentOperator())
                    .add(expression1())
            ));
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
        return new Alternation()
            .add(new Symbol("="))
            .add(new Symbol("+="))
            .add(new Symbol("-="))
            .add(new Symbol("*="))
            .add(new Symbol("/="))
            .add(new Symbol("&="))
            .add(new Symbol("|="))
            .add(new Symbol("^="))
            .add(new Symbol("%="))
            .add(new Symbol("<<="))
            .add(new Symbol(">>="))
            .add(new Symbol(">>>="))
            ;
    }

    /** Type:
     *      Identifier [TypeArguments]{ .   Identifier [TypeArguments]} BracketsOpt
     *      BasicType
     */
    public Parser type() {
        return new Alternation()
            .add(new TrackSequence()
                .add(identifier())
                .add(zeroOrOne(typeArguments()))
                .add(new Repetition(
                    new TrackSequence()
                        .add(new Symbol("."))
                        .add(identifier())
                        .add(zeroOrOne(typeArguments()))
                ))
                .add(bracketsOpt())
            )
            .add(basicType())
            ;
    }

    /** TypeArguments:
     *      < TypeArgument {, TypeArgument}>
     */
    public Parser typeArguments() {
        return new TrackSequence()
            .add(new Symbol("<"))
            .add(typeArgument())
            .add(new Repetition(
                new TrackSequence()
                    .add(new Symbol(","))
                    .add(typeArgument())
            ));
    }

    /** TypeArgument:
     *      Type
     *      ? [(extends |super ) Type]
     */
    private Parser typeArgument() {
        return new Alternation()
            .add(type())
            .add(new TrackSequence()
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
    }

    /** RawType:
     *      Identifier { .   Identifier } BracketsOpt
     */
    public Parser rawType() {
        return new TrackSequence()
            .add(identifier())
            .add(new Repetition(
                new TrackSequence()
                    .add(new Symbol("."))
                    .add(identifier())
            ))
            .add(bracketsOpt());
    }

    /** StatementExpression:
     *      Expression
     */
    public Parser statementExpression() {
        return expression();
    }

    /** ConstantExpression:
     *      Expression
     */
    public Parser constantExpression() {
        return expression();
    }

    /** Expression1:
     *      Expression2 [Expression1Rest]
     */
    public Parser expression1() {
        return new TrackSequence()
            .add(expression2())
            .add(zeroOrOne(expression1Rest()));
    }

    /** Expression1Rest:
     *      [ ?   Expression :   Expression1]
     */
    public Parser expression1Rest() {
        return zeroOrOne(
            new TrackSequence()
                .add(new Symbol("?"))
                .add(expression())
                .add(new Symbol(":"))
                .add(expression1())
        );
    }

    /** Expression2 :
     *      Expression3 [Expression2Rest]
     */
    public Parser expression2() {
        return new TrackSequence()
            .add(expression3())
            .add(zeroOrOne(expression2Rest()));
    }

    /** Expression2Rest:
     *      {Infixop Expression3}
     *      Expression3 instanceof Type
     */
    public Parser expression2Rest() {
        return new Alternation()
            .add(new Repetition(
                new TrackSequence()
                    .add(infixOp())
                    .add(expression3())
            ))
            .add(new TrackSequence()
                .add(expression3())
                .add(new Literal("instanceof"))
                .add(type())
            );
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
        return new Alternation()
            .add(new Symbol("||"))
            .add(new Symbol("&&"))
            .add(new Symbol("|"))
            .add(new Symbol("^"))
            .add(new Symbol("&"))
            .add(new Symbol("=="))
            .add(new Symbol("!="))
            .add(new Symbol("<"))
            .add(new Symbol(">"))
            .add(new Symbol("<="))
            .add(new Symbol(">="))
            .add(new Symbol("<<"))
            .add(new Symbol(">>"))
            .add(new Symbol(">>>"))
            .add(new Symbol("+"))
            .add(new Symbol("*"))
            .add(new Symbol("/"))
            .add(new Symbol("%"))
            ;
    }

    /** Expression3:
     *      PrefixOp Expression3
     *      (   (Expr|Type) )   Expression3
     *      Primary {Selector} {PostfixOp}
     */
    public Parser expression3() {
        return new Alternation()
            .add(new TrackSequence()
                .add(prefixOp())
                .add(expression3())
            )
            .add(new TrackSequence()
                .add(new Symbol("("))
                .add(new Alternation()
                    .add(expr())
                    .add(type())
                )
                .add(new Symbol(")"))
                .add(expression3())
            )
            .add(new TrackSequence()
                .add(primary())
                .add(new Repetition(selector()))
                .add(new Repetition(postfixOp()))
            )
            ;
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
        return new Alternation()
            // ( Expression)
            .add(new TrackSequence()
                .add(new Symbol("("))
                .add(expression())
                .add(new Symbol(")"))
            )
            // NonWildcardTypeArguments (ExplicitGenericInvocationSuffix |this Arguments)
            .add(new TrackSequence()
                .add(nonWildcardTypeArguments())
                .add(new Alternation()
                    .add(explicitGenericInvocationSuffix())
                    .add(new Literal("this"))
                )
            )
            // this [Arguments]
            .add(new TrackSequence()
                .add(new Literal("this"))
                .add(zeroOrOne(arguments()))
            )
            // super SuperSuffix
            .add(new TrackSequence()
                .add(new Literal("this"))
                .add(superSuffix())
            )
            // Literal
            .add(literal())
            // new Creator
            .add(new TrackSequence()
                .add(new Literal("new"))
                .add(creator())
            )
            // Identifier {. Identifier }[ IdentifierSuffix]
            .add(new TrackSequence()
                .add(identifier())
                .add(new Repetition(new TrackSequence()
                    .add(new Symbol('.'))
                    .add(identifier())
                ))
                .add(zeroOrOne(identifierSuffix()))
            )
            // BasicType BracketsOpt.class
            .add(new TrackSequence()
                .add(basicType())
                .add(bracketsOpt())
                .add(new Symbol('.'))
                .add(new Literal("class"))
            )
            // void.class
            .add(new TrackSequence()
                .add(new Literal("void"))
                .add(new Symbol("."))
                .add(new Literal("class"))
            )
            ;
    }

    /** IdentifierSuffix:
     *      [ (] BracketsOpt   . class | Expression])
     *      Arguments
     *      .   (class | ExplicitGenericInvocation |this |super Arguments |new [NonWildcardTypeArguments] InnerCreator )
     */
    public Parser identifierSuffix() {
        return new Alternation()

                // [ (] BracketsOpt   . class | Expression])
                .add(new TrackSequence()
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
                )

                // Arguments
                .add(arguments())

                // .   (class | ExplicitGenericInvocation |this |super Arguments |new [NonWildcardTypeArguments] InnerCreator )
                .add(new TrackSequence()
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
                )
        ;
    }

    /** ExplicitGenericInvocation:
     *      NonWildcardTypeArguments ExplicitGenericInvocationSuffix
     */
    public Parser explicitGenericInvocation() {
        return new TrackSequence()
            .add(nonWildcardTypeArguments())
            .add(explicitGenericInvocationSuffix())
        ;
    }

    /** NonWildcardTypeArguments:
     *      < TypeList>
     */
    public Parser nonWildcardTypeArguments() {
        return new TrackSequence()
            .add(new Symbol("<"))
            .add(typeList())
            .add(new Symbol(">"))
        ;
    }

    /** ExplicitGenericInvocationSuffix:
     *      super SuperSuffix
     *      Identifier Arguments
     */
    public Parser explicitGenericInvocationSuffix() {
        return new Alternation()
            .add(new TrackSequence()
                .add(new Literal("super"))
                .add(superSuffix()))
            .add(new TrackSequence()
                .add(identifier())
                .add(arguments())
            );
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
        return new Alternation()
            .add(new Symbol("++"))
            .add(new Symbol("--"))
            .add(new Symbol('!'))
            .add(new Symbol('~'))
            .add(new Symbol('+'))
            .add(new Symbol('-'))
        ;
    }

    /** PostfixOp:
     *      ++
     *      --
     */
    public Parser postfixOp() {
        return new Alternation()
            .add(new Symbol("++"))
            .add(new Symbol("--"))
        ;
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
        return new Alternation()
            .add(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(arguments()))
            )
            .add(new TrackSequence()
                .add(new Symbol('.'))
                .add(explicitGenericInvocation())
            )
            .add(new TrackSequence()
                .add(new Symbol('.'))
                .add(new Literal("this"))
            )
            .add(new TrackSequence()
                .add(new Symbol('.'))
                .add(new Literal("super"))
                .add(superSuffix())
            )
            .add(new TrackSequence()
                .add(new Symbol('.'))
                .add(new Literal("new"))
                .add(zeroOrOne(nonWildcardTypeArguments()))
                .add(innerCreator())
            )
            .add(new TrackSequence()
                .add(new Symbol('['))
                .add(expression())
                .add(new Symbol(']'))
            )
            ;
    }

    /** SuperSuffix:
     *      Arguments
     *      . Identifier [Arguments]
     */
    public Parser superSuffix() {
        return new Alternation()
            .add(arguments())
            .add(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(arguments()))
            );
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
        return new Alternation()
            .add(new Literal("byte"))
            .add(new Literal("short"))
            .add(new Literal("char"))
            .add(new Literal("int"))
            .add(new Literal("long"))
            .add(new Literal("float"))
            .add(new Literal("double"))
            .add(new Literal("boolean"))
        ;
    }

    /** ArgumentsOpt:
     *      [ Arguments ]
     */
    public Parser argumentsOpt() {
        return zeroOrOne(arguments());
    }

    /** Arguments:
     *      ( [Expression {, Expression }])
     */
    public Parser arguments() {
        return new TrackSequence()
            .add(new Symbol('('))
            .add(zeroOrOne(new TrackSequence()
                .add(expression())
                .add(new Repetition(new TrackSequence()
                    .add(new Symbol(','))
                    .add(expression())
                ))
            ))
            .add(new Symbol(')'))
        ;
    }

    /** BracketsOpt:
     *      {[]}
     */
    public Parser bracketsOpt() {
        return new Repetition(new TrackSequence()
            .add(new Symbol('['))
            .add(new Symbol(']'))
        );
    }

    /** Creator:
     *      [NonWildcardTypeArguments] CreatedName ( ArrayCreatorRest  | ClassCreatorRest )
     */
    public Parser creator() {
        return new TrackSequence()
            .add(zeroOrOne(nonWildcardTypeArguments()))
            .add(createdName())
            .add(new Alternation()
                .add(arrayCreatorRest())
                .add(classCreatorRest())
            );
    }

    /** CreatedName:
     *      Identifier [NonWildcardTypeArguments] {. Identifier [NonWildcardTypeArguments]}
     */
    public Parser createdName() {
        return new TrackSequence()
            .add(identifier())
            .add(zeroOrOne(nonWildcardTypeArguments()))
            .add(new Repetition(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
                .add(zeroOrOne(nonWildcardTypeArguments()))
            ));
    }

    /** InnerCreator:
     *      Identifier ClassCreatorRest
     */
    public Parser innerCreator() {
        return new TrackSequence()
            .add(identifier())
            .add(classCreatorRest())
        ;
    }

    /** ArrayCreatorRest:
     *      [ (] BracketsOpt ArrayInitializer | Expression] {[ Expression]} BracketsOpt )
     */
    public Parser arrayCreatorRest() {
        return new TrackSequence()
            .add(new Symbol('['))
            .add(new Alternation()
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
    }

    /** ClassCreatorRest:
     *      Arguments [ClassBody]
     */
    public Parser classCreatorRest() {
        return new TrackSequence()
            .add(arguments())
            .add(zeroOrOne(classBody()))
        ;
    }

    /** ArrayInitializer:
     *      { [VariableInitializer {, VariableInitializer} [,]]}
     */
    public Parser arrayInitializer() {
        return new TrackSequence()
            .add(new Symbol("{"))
            .add(zeroOrOne(new TrackSequence()
                .add(variableInitializer())
                .add(new Repetition(new TrackSequence()
                    .add(new Symbol(","))
                    .add(variableInitializer())
                ))
                .add(zeroOrOne(new Symbol(',')))
            ))
            .add(new Symbol("}"))
        ;
    }

    /** VariableInitializer:
     *      ArrayInitializer
     *      Expression
     */
    public Parser variableInitializer() {
        return new Alternation()
            .add(arrayInitializer())
            .add(expression())
        ;
    }

    /** ParExpression:
     *      ( Expression)
     */
    public Parser parExpression() {
        return new TrackSequence()
            .add(new Symbol('('))
            .add(expression())
            .add(new Symbol(')'))
        ;
    }

    /** Block:
     *      { BlockStatements}
     */
    public Parser block() {
        return new TrackSequence()
            .add(new Symbol('{'))
            .add(blockStatements())
            .add(new Symbol('}'))
        ;
    }

    /** BlockStatements:
     *      { BlockStatement }
     */
    public Parser blockStatements() {
        return new Repetition(blockStatement());
    }

    /** BlockStatement:
     *      LocalVariableDeclarationStatement
     *      ClassOrInterfaceDeclaration
     *      [Identifier:] Statement
     */
    public Parser blockStatement() {
        return new Alternation()
            .add(localVariableDeclarationStatement())
            .add(classOrInterfaceDeclaration())
            .add(new TrackSequence()
                .add(zeroOrOne(new TrackSequence()
                    .add(identifier())
                    .add(new Symbol(":"))
                ))
                .add(statement())
            );
    }

    /** LocalVariableDeclarationStatement:
     *      [final] Type VariableDeclarators;
     */
    public Parser localVariableDeclarationStatement() {
        return new TrackSequence()
            .add(zeroOrOne(new Literal("final")))
            .add(type())
            .add(variableDeclarators())
            .add(new Literal(";"))
        ;
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
        return new Alternation()
            //Block
             .add(block())
            //assert Expression [: Expression];
            .add(new TrackSequence()
                .add(new Literal("assert"))
                .add(expression())
                .add(zeroOrOne(
                    new TrackSequence()
                        .add(new Symbol(':'))
                        .add(expression())
                ))
                .add(new Symbol(';'))
            )
            //if ParExpression Statement [else Statement]
            .add(new TrackSequence()
                .add(new Literal("if"))
                .add(parExpression())
                .add(statement())
                .add(zeroOrOne(
                    new TrackSequence()
                        .add(new Literal("else"))
                        .add(statement())
                ))
            )
            //for (ForControl) Statement
            .add(new TrackSequence()
                .add(new Literal("for"))
                .add(new Symbol('('))
                .add(forControl())
                .add(new Symbol(')'))
                .add(statement())
            )
            //while ParExpression Statement
            .add(new TrackSequence()
                .add(new Literal("while"))
                .add(parExpression())
                .add(statement())
            )
            //do Statement while ParExpression ;
            .add(new TrackSequence()
                .add(new Literal("do"))
                .add(statement())
                .add(new Literal("while"))
                .add(parExpression())
                .add(new Symbol(';'))
            )
            //try Block ( Catches | [Catches] finally Block )
            .add(new TrackSequence()
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
            )
            //switch ParExpression{ SwitchBlockStatementGroups}
            .add(new TrackSequence()
                .add(new Literal("switch"))
                .add(parExpression())
                .add(new Symbol('{'))
                .add(switchBlockStatementGroups())
                .add(new Symbol('}'))
            )
            //synchronized ParExpression Block
            .add(new TrackSequence()
                .add(new Literal("synchronized"))
                .add(parExpression())
                .add(block())
            )
            //return [Expression];
            .add(new TrackSequence()
                .add(new Literal("return"))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
            )
            //throw Expression ;
            .add(new TrackSequence()
                .add(new Literal("throw"))
                .add(expression())
                .add(new Symbol(';'))
            )
            //break [Identifier]
            .add(new TrackSequence()
                .add(new Literal("break"))
                .add(zeroOrOne(identifier()))
            )
            //continue [Identifier]
            .add(new TrackSequence()
                .add(new Literal("continue"))
                .add(zeroOrOne(identifier()))
            )
            //;
            .add(new Symbol(';'))
            //ExpressionStatement
            .add(expressionStatement())
            //Identifier :   Statement
            .add(new TrackSequence()
                .add(identifier())
                .add(new Symbol(':'))
                .add(statement())
            )
        ;
    }

    /** Catches:
     *      CatchClause {CatchClause}
     */
    public Parser catches() {
        return new TrackSequence()
            .add(catchClause())
            .add(new Repetition(catchClause()))
        ;
    }

    /** CatchClause:
     *      catch( FormalParameter) Block
     */
    public Parser catchClause() {
        return new TrackSequence()
            .add(new Literal("catch"))
            .add(new Symbol('('))
            .add(formalParameter())
            .add(new Symbol(')'))
            .add(block())
        ;
    }

    /** SwitchBlockStatementGroups:
     *      { SwitchBlockStatementGroup }
     */
    public Parser switchBlockStatementGroups() {
        return new Repetition(switchBlockStatementGroup());
    }

    /** SwitchBlockStatementGroup:
     *      SwitchLabel BlockStatements
     */
    public Parser switchBlockStatementGroup() {
        return new TrackSequence()
            .add(switchLabel())
            .add(blockStatements())
        ;
    }

    /** SwitchLabel:
     *      case ConstantExpression :
     *      default:
     */
    public Parser switchLabel() {
        return new Alternation()
            .add(new TrackSequence()
                .add(new Literal("case"))
                .add(constantExpression())
                .add(new Symbol(":"))
            )
            .add(new Literal("default"))
            .add(new Symbol(":"))
        ;
    }

    /** MoreStatementExpressions:
     *      {, StatementExpression }
     */
    public Parser moreStatementExpressions() {
        return new Repetition(new TrackSequence()
            .add(new Symbol(','))
            .add(statementExpression())
        );
    }

    /** ForControl:
     *      ;   [Expression] ;   ForUpdateOpt
     *      StatementExpression MoreStatementExpressions;   [Expression]; ForUpdateOpt
     *      [final] [Annotations] Type Identifier ForControlRest
     */
    public Parser forControl() {
        return new Alternation()
            .add(new TrackSequence()
                .add(new Symbol(";"))
                .add(zeroOrOne(expression()))
                .add(new Symbol(";"))
                .add(zeroOrOne(forUpdate()))
            )
            .add(new TrackSequence()
                .add(statementExpression())
                .add(moreStatementExpressions())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            )
            .add(new TrackSequence()
                .add(zeroOrOne(new Literal("final")))
                .add(zeroOrOne(annotations()))
                .add(type())
                .add(identifier())
                .add(forControlRest())
            )
        ;
    }

    /** ForControlRest:
     *      VariableDeclaratorsRest;   [Expression] ;   ForUpdateOpt
     *      : Expression
     */
    public Parser forControlRest() {
        return new Alternation()
            .add(new TrackSequence()
                .add(variableDeclaratorsRest())
                .add(new Symbol(';'))
                .add(zeroOrOne(expression()))
                .add(new Symbol(';'))
                .add(zeroOrOne(forUpdate()))
            )
            .add(new TrackSequence()
                .add(new Symbol(':'))
                .add(expression())
            )
        ;
    }

    /** ForUpdate:
     *      StatementExpression MoreStatementExpressions
     */
    public Parser forUpdate() {
        return new TrackSequence()
            .add(statementExpression())
            .add(moreStatementExpressions())
        ;
    }


    /** ModifiersOpt:
     *      { Modifier }
     */
    public Parser modifiersOpt() {
        return new Repetition(modifier());
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
        return new Alternation()
            .add(annotation())
            .add(new Literal("public"))
            .add(new Literal("protected"))
            .add(new Literal("private"))
            .add(new Literal("static"))
            .add(new Literal("abstract"))
            .add(new Literal("final"))
            .add(new Literal("native"))
            .add(new Literal("synchronized"))
            .add(new Literal("transient"))
            .add(new Literal("volatile"))
            .add(new Literal("strictfp"))
        ;
    }

    /** VariableDeclarators:
     *      VariableDeclarator {,   VariableDeclarator }
     */
    public Parser variableDeclarators() {
        return new TrackSequence()
            .add(variableDeclarators())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(variableDeclarator())
            ))
        ;
    }

    /** VariableDeclaratorsRest:
     *      VariableDeclaratorRest {,   VariableDeclarator }
     */
    public Parser variableDeclaratorsRest() {
        return new TrackSequence()
            .add(variableDeclaratorsRest())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(variableDeclarator())
            ))
        ;
    }

    /** ConstantDeclaratorsRest:
     *      ConstantDeclaratorRest {,   ConstantDeclarator }
     */
    public Parser constantDeclaratorsRest() {
        return new TrackSequence()
            .add(constantDeclaratorsRest())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(constantDeclarator())
            ))
        ;
    }

    /** VariableDeclarator:
     *      Identifier VariableDeclaratorRest
     */
    public Parser variableDeclarator() {
        return new TrackSequence()
            .add(identifier())
            .add(variableDeclaratorsRest())
        ;
    }

    /** ConstantDeclarator:
     *      Identifier ConstantDeclaratorRest
     */
    public Parser constantDeclarator() {
        return new TrackSequence()
            .add(identifier())
            .add(constantDeclaratorsRest())
        ;
    }

    /** VariableDeclaratorRest:
     *      BracketsOpt [ =   VariableInitializer]
     */
    public Parser variableDeclaratorRest() {
        return new TrackSequence()
            .add(bracketsOpt())
            .add(zeroOrOne(new TrackSequence()
                .add(new Symbol('='))
                .add(variableInitializer())
            ))
        ;
    }

    /** ConstantDeclaratorRest:
     *      BracketsOpt =   VariableInitializer
     */
    public Parser constantDeclaratorRest() {
        return new TrackSequence()
            .add(bracketsOpt())
            .add(new Symbol('='))
            .add(variableInitializer())
        ;
    }

    /** VariableDeclaratorId:
     *      Identifier BracketsOpt
     */
    public Parser variableDeclaratorId() {
        return new TrackSequence()
            .add(identifier())
            .add(bracketsOpt())
        ;
    }

    /** CompilationUnit:
     *      [Annotations opt package QualifiedIdentifier ;  ] {ImportDeclaration} {TypeDeclaration}
     */
    public Parser compilationUnit() {
        return new TrackSequence()
            .add(zeroOrOne(
                new TrackSequence()
                    .add(zeroOrOne(annotations()))
                    .add(new Literal("package"))
                    .add(qualifiedIdentifier())
                    .add(new Symbol(';'))
            ))
            .add(new Repetition(importDeclaration()))
            .add(new Repetition(typeDeclaration()))
        ;
    }

    /** ImportDeclaration:
     *      import [static] Identifier { .   Identifier } [  .*   ];
     */
    public Parser importDeclaration() {
        return new TrackSequence()
            .add(new Literal("import"))
            .add(zeroOrOne(new Literal("static")))
            .add(identifier())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol('.'))
                .add(identifier())
            ))
            .add(zeroOrOne(new Symbol(".*")))
            .add(new Symbol(';'))
        ;
    }

    /** TypeDeclaration:
     *      ClassOrInterfaceDeclaration
     *      ;
     */
    public Parser typeDeclaration() {
        return new Alternation()
            .add(classOrInterfaceDeclaration())
            .add(new Symbol(';'))
        ;
    }

    /** ClassOrInterfaceDeclaration:
     *      ModifiersOpt (ClassDeclaration | InterfaceDeclaration)
     */
    public Parser classOrInterfaceDeclaration() {
        return new TrackSequence()
            .add(modifiersOpt())
            .add(new Alternation()
                .add(classDeclaration())
                .add(interfaceDeclaration())
            )
        ;
    }

    /** ClassDeclaration:
     *      NormalClassDeclaration           // note error in JLS¤18 makes this look like a Sequence not an Alternation
     *      EnumDeclaration
     */
    public Parser classDeclaration() {
        return new Alternation()
            .add(normalClassDeclaration())
            .add(enumDeclaration())
        ;
    }

    /** NormalClassDeclaration:
     *      class Identifier TypeParameters opt [extends Type] [implements TypeList] ClassBody
     */
    public Parser normalClassDeclaration() {
        return new TrackSequence()
            .add(new Literal("class"))
            .add(identifier())
            .add(zeroOrOne(typeParameters()))
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("extends"))
                .add(type())
            ))
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("implements"))
                .add(typeList())
            ))
            .add(classBody())
        ;
    }

    /** TypeParameters:
     *      < TypeParameter {, TypeParameter}>
     */
    public Parser typeParameters() {
        return new TrackSequence()
            .add(new Symbol('<'))
            .add(typeParameter())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(typeParameter())
            ))
            .add(new Symbol('>'))
        ;
    }

    /** TypeParameter:
     *      Identifier [extendsBound]
     */
    public Parser typeParameter() {
        return new TrackSequence()
            .add(identifier())
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("extends"))
                .add(bound())
            ))
        ;
    }

    /** Bound:
     *      Type {&Type}
     */
    public Parser bound() {
        return new TrackSequence()
            .add(type())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol('&'))
                .add(type())
            ))
        ;
    }

    /** EnumDeclaration:
     *      ClassModifiers opt enum Identifier[implements TypeList] EnumBody
     */
    public Parser enumDeclaration() {
        return new TrackSequence()
            .add(zeroOrOne(classModifiers()))
            .add(new Literal("enum"))
            .add(identifier())
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("implements"))
                .add(typeList())
            ))
            .add(enumBody())
        ;
    }

    /** EnumBody:
     *      { EnumConstants opt ,opt EnumBodyDeclarations opt }
     */
    public Parser enumBody() {
        return new TrackSequence()
            .add(new Symbol('{'))
            .add(zeroOrOne(enumConstants()))
            .add(zeroOrOne(new Symbol(',')))
            .add(zeroOrOne(enumBodyDeclarations()))
            .add(new Symbol('}'))
        ;
    }

    /** EnumConstants:
     *      EnumConstant
     *      EnumConstants , EnumConstant
     */
    public Parser enumConstants() {
        return new Alternation()
            .add(enumConstant())
            .add(new TrackSequence()
                .add(enumConstants())
                .add(new Symbol(','))
                .add(enumConstant())
            )
        ;
    }

    /** EnumConstant:
     *      Annotations Identifier EnumArguments opt ClassBody opt
     *
     * N.B. JRR changed to EnumArguments - check with JLSv3 spec revisions (bug reported in grammar)
     */
    public Parser enumConstant() {
        return new TrackSequence()
            .add(annotations())
            .add(identifier())
            .add(zeroOrOne(enumArguments()))
            .add(zeroOrOne(classBody()))
        ;
    }

    /** EnumArguments:
     *      ( ArgumentListopt )
     *
     * N.B. JRR changed to EnumArguments - check with JLSv3 spec revisions (bug reported in grammar)
     */
    public Parser enumArguments() {
        return new TrackSequence()
            .add(new Symbol('('))
            .add(zeroOrOne(argumentList()))
            .add(new Symbol(')'))
        ;
    }

    /** EnumBodyDeclarations:
     *      ; ClassBodyDeclarationsopt
     *
     * todo that semicolon looks a bit dodgy... jez
     */
    public Parser enumBodyDeclarations() {
        return new TrackSequence()
            .add(new Symbol(';'))
            .add(zeroOrOne(classBodyDeclarations()))
        ;
    }

    /** InterfaceDeclaration:
     *      NormalInterfaceDeclaration
     *      AnnotationTypeDeclaration
     */
    public Parser interfaceDeclaration() {
        return new Alternation()
            .add(normalInterfaceDeclaration())
            .add(annotationTypeDeclaration())
        ;
    }

    /** NormalInterfaceDeclaration:
     *      interface Identifier TypeParameters opt[extends TypeList] InterfaceBody
     */
    public Parser normalInterfaceDeclaration() {
        return new TrackSequence()
            .add(new Literal("interface"))
            .add(identifier())
            .add(zeroOrOne(typeParameters()))
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("extends"))
                .add(typeList())
            ))
            .add(interfaceBody())
        ;
    }

    /** TypeList:
     *      Type { ,   Type}
     */
    public Parser typeList() {
        return new TrackSequence()
            .add(type())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(type())
            ))
        ;
    }

    /** AnnotationTypeDeclaration:
     *      InterfaceModifiers opt @ interface Identifier AnnotationTypeBody
     */
    public Parser annotationTypeDeclaration() {
        return new TrackSequence()
            .add(zeroOrOne(interfaceModifiers()))
            .add(new Symbol('@'))
            .add(new Literal("interface")) // @todo is this correct?
            .add(identifier())
            .add(annotationTypeBody())
        ;
    }

    /** AnnotationTypeBody:
     *      { AnnotationTypeElementDeclarations }
     */
    public Parser annotationTypeBody() {
        return new TrackSequence()
            .add(new Symbol('{'))
            .add(annotationTypeElementDeclarations())
            .add(new Symbol('}'))
        ;
    }

    /** AnnotationTypeElementDeclarations:
     *      AnnotationTypeElementDeclaration
     *      AnnotationTypeElementDeclarations AnnotationTypeElementDeclaration
     */
    public Parser annotationTypeElementDeclarations() {
        return new Alternation()
            .add(annotationTypeElementDeclaration())
            .add(new TrackSequence()
                .add(annotationTypeElementDeclarations())
                .add(annotationTypeElementDeclaration())
            )
        ;
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
        return new Alternation()
            .add(new TrackSequence()
                .add(zeroOrOne(abstractMethodModifiers()))
                .add(type())
                .add(identifier())
                .add(new Symbol('('))   // @todo - is this right
                .add(new Symbol(')'))
                .add(zeroOrOne(defaultValue()))
                .add(new Symbol(';'))
            )
            .add(constantDeclaration())
            .add(classDeclaration())
            .add(interfaceDeclaration())
            .add(enumDeclaration())
            .add(annotationTypeDeclaration())
            .add(new Symbol(';'))
        ;
    }

    /** DefaultValue:
     *      default ElementValue
     */
    public Parser defaultValue() {
        return new TrackSequence()
            .add(new Literal("default"))
            .add(elementValue())
        ;
    }

    /** ClassBody:
     *      { {ClassBodyDeclaration}}
     */
    public Parser classBody() {
        return new TrackSequence()
            .add(new Symbol('{'))
            .add(new Repetition(classBodyDeclaration()))
            .add(new Symbol('}'))
        ;
    }

    /** InterfaceBody:
     *      { {InterfaceBodyDeclaration}}
     */
    public Parser interfaceBody() {
        return new TrackSequence()
            .add(new Symbol('{'))
            .add(new Repetition(interfaceBodyDeclaration()))
            .add(new Symbol('}'))
        ;
    }

    /** ClassBodyDeclaration:
     *      ;
     *      [static] Block
     *      ModifiersOpt MemberDecl
     */
    public Parser classBodyDeclaration() {
        return new Alternation()
            .add(new Symbol(';'))
            .add(new TrackSequence()
                .add(zeroOrOne(new Literal("static")))
                .add(block())
            )
            .add(new TrackSequence()
                .add(modifiersOpt())
                .add(memberDecl())
            )
        ;
    }

    /** MemberDecl:
     *      GenericMethodOrConstructorDecl
     *      MethodOrFieldDecl
     *      void Identifier MethodDeclaratorRest
     *      Identifier ConstructorDeclaratorRest
     *      ClassOrInterfaceDeclaration
     */
    public Parser memberDecl() {
        return new Alternation()
            .add(genericMethodOrConstructorDecl())
            .add(methodOrFieldDecl())
            .add(new TrackSequence()
                .add(new Literal("void"))
                .add(identifier())
                .add(methodDeclaratorRest())
            )
            .add(new TrackSequence()
                .add(identifier())
                .add(constructorDeclaratorRest())
            )
            .add(classOrInterfaceDeclaration())
        ;
    }

    /** GenericMethodOrConstructorDecl:
     *      TypeParameters GenericMethodOrConstructorRest
     */
    public Parser genericMethodOrConstructorDecl() {
        return new TrackSequence()
            .add(typeParameters())
            .add(genericMethodOrConstructorRest())
        ;
    }

    /** GenericMethodOrConstructorRest:
     *      Type Identifier MethodDeclaratorRest
     *      Identifier ConstructorDeclaratorRest
     */
    public Parser genericMethodOrConstructorRest() {
        return new Alternation()
            .add(new TrackSequence()
                .add(type())
                .add(identifier())
                .add(methodDeclaratorRest())
            )
            .add(new TrackSequence()
                .add(identifier())
                .add(constructorDeclaratorRest())
            )
        ;
    }

    /** MethodOrFieldDecl:
     *      Type Identifier MethodOrFieldRest
     */
    public Parser methodOrFieldDecl() {
        return new TrackSequence()
            .add(type())
            .add(identifier())
            .add(methodOrFieldRest())
        ;
    }

    /** MethodOrFieldRest:
     *      VariableDeclaratorRest
     *      MethodDeclaratorRest
     */
    public Parser methodOrFieldRest() {
        return new Alternation()
            .add(variableDeclaratorRest())
            .add(methodDeclaratorRest())
        ;
    }

    /** InterfaceBodyDeclaration:
     *      ;
     *      ModifiersOpt InterfaceMemberDecl
     */
    public Parser interfaceBodyDeclaration() {
        return new Alternation()
            .add(new Symbol(';'))
            .add(new TrackSequence()
                .add(modifiersOpt())
                .add(interfaceMemberDecl())
            )
        ;
    }

    /** InterfaceMemberDecl:
     *      InterfaceMethodOrFieldDecl
     *      InterfaceGenericMethodDecl
     *      void Identifier VoidInterfaceMethodDeclaratorRest
     *      ClassOrInterfaceDeclaration
     */
    public Parser interfaceMemberDecl() {
        return new Alternation()
            .add(interfaceMethodOrFieldDecl())
            .add(interfaceGenericMethodDecl())
            .add(new TrackSequence()
                .add(new Literal("void"))
                .add(identifier())
                .add(voidInterfaceMethodDeclaratorRest())
            )
            .add(classOrInterfaceDeclaration())
        ;
    }

    /** InterfaceMethodOrFieldDecl:
     *      Type Identifier InterfaceMethodOrFieldRest
     */
    public Parser interfaceMethodOrFieldDecl() {
        return new TrackSequence()
            .add(type())
            .add(identifier())
            .add(interfaceMethodOrFieldRest())
        ;
    }

    /** InterfaceMethodOrFieldRest:
     *      ConstantDeclaratorsRest;
     *      InterfaceMethodDeclaratorRest
     */
    public Parser interfaceMethodOrFieldRest() {
        return new Alternation()
            .add(new TrackSequence()
                .add(constantDeclaratorsRest())
                .add(new Symbol(';'))
            )
            .add(interfaceMethodDeclaratorRest())
        ;
    }

    /** MethodDeclaratorRest:
     *      FormalParameters BracketsOpt[throwsQualifiedIdentifierList]( MethodBody |;  )
     */
    public Parser methodDeclaratorRest() {
        return new TrackSequence()
            .add(formalParameters())
            .add(bracketsOpt())
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ))
            .add(new Alternation()
                .add(methodBody())
                .add(new Symbol(';'))
            )
        ;
    }

    /** VoidMethodDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList] ( MethodBody |;  )
     */
    public Parser voidMethodDeclaratorRest() {
        return new TrackSequence()
            .add(formalParameters())
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ))
            .add(new Alternation()
                .add(methodBody())
                .add(new Symbol(';'))
            )
        ;
    }

    /** InterfaceMethodDeclaratorRest:
     *      FormalParameters BracketsOpt [throws QualifiedIdentifierList];
     */
    public Parser interfaceMethodDeclaratorRest() {
        return new TrackSequence()
            .add(formalParameters())
            .add(bracketsOpt())
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ))
        ;
    }

    /** InterfaceGenericMethodDecl:
     *      TypeParameters Type Identifier InterfaceMethodDeclaratorRest
     */
    public Parser interfaceGenericMethodDecl() {
        return new TrackSequence()
            .add(typeParameters())
            .add(type())
            .add(identifier())
            .add(interfaceMethodDeclaratorRest())
        ;
    }

    /** VoidInterfaceMethodDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList];
     */
    public Parser voidInterfaceMethodDeclaratorRest() {
        return new TrackSequence()
            .add(formalParameters())
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ))
            .add(new Symbol(';'))
        ;
    }

    /** ConstructorDeclaratorRest:
     *      FormalParameters [throws QualifiedIdentifierList] MethodBody
     */
    public Parser constructorDeclaratorRest() {
        return new TrackSequence()
            .add(formalParameters())
            .add(zeroOrOne(new TrackSequence()
                .add(new Literal("throws"))
                .add(qualifiedIdentifierList())
            ))
            .add(methodBody())
        ;
    }

    /** QualifiedIdentifierList:
     *      QualifiedIdentifier { ,   QualifiedIdentifier}
     */
    public Parser qualifiedIdentifierList() {
        return new TrackSequence()
            .add(qualifiedIdentifier())
            .add(new Repetition(new TrackSequence()
                .add(new Symbol(','))
                .add(qualifiedIdentifier())
            ));
    }

    /** FormalParameters:
     *      ( [FormalParameterDecls])
     */
    public Parser formalParameters() {
        return new TrackSequence()
            .add(new Symbol('('))
            .add(zeroOrOne(formalParameterDecls()))
            .add(new Symbol(')'))
        ;
    }

    /** FormalParameterDecls:
     *      [final] [Annotations] Type FormalParameterDeclsRest]
     * todo - is last parameter optional!!!????
     */
    public Parser formalParameterDecls() {
        return new TrackSequence()
            .add(zeroOrOne(new Literal("final")))
            .add(zeroOrOne(annotations()))
            .add(type())
            .add(formalParameterDeclsRest())
        ;
    }

    /** FormalParameterDeclsRest:
     *      VariableDeclaratorId [, FormalParameterDecls]
     *      ... VariableDeclaratorId
     */
    public Parser formalParameterDeclsRest() {
        return new Alternation()
            .add(new TrackSequence()
                .add(variableDeclaratorId())
                .add(zeroOrOne(new TrackSequence()
                    .add(new Symbol(','))
                    .add(formalParameterDecls())
                ))
            )
            .add(new TrackSequence()
                .add(new Symbol("..."))  //todo - this doesn't seem right :-)
                .add(variableDeclaratorId())
            )
        ;
    }

    /** MethodBody:
     *      Block
     */
    public Parser methodBody() {
        return block();
    }






    // ------ JLSv3¤8.1.1 ------
    /** ClassModifiers:
     *      ClassModifier
     *      ClassModifiers ClassModifier
     */
    public Parser classModifiers() {
        return new Alternation()
            .add(classModifier())
            .add(new TrackSequence()
                .add(classModifiers())
                .add(classModifier())
            )
        ;
    }
    /** ClassModifier: one of
     *      Annotation public protected private
     *      abstract static final strictfp
     */
    public Parser classModifier() {
        return new Alternation()
            .add(annotation())
            .add(new Literal("public"))
            .add(new Literal("protected"))
            .add(new Literal("private"))
            .add(new Literal("abstract"))
            .add(new Literal("static"))
            .add(new Literal("final"))
            .add(new Literal("strictfp"))
        ;
    }

    // ------ JLSv3¤8.1.6 ------
    /** ClassBodyDeclarations:
     *      ClassBodyDeclaration
     *      ClassBodyDeclarations ClassBodyDeclaration
     */
    public Parser classBodyDeclarations() {
        return new Alternation()
            .add(classBodyDeclaration())
            .add(new TrackSequence()
                .add(classBodyDeclarations())
                .add(classBodyDeclaration())
            )
        ;
    }

    // ------ JLSv3¤8.4.1 ------
    /** FormalParameter:
     *      VariableModifiers Type VariableDeclaratorId
     */
    public Parser formalParameter() {
        return new TrackSequence()
            .add(variableModifiers())
            .add(type())
            .add(variableDeclaratorId())
        ;
    }

    /** VariableModifiers:
     *      VariableModifier
     *      VariableModifiers VariableModifier
     */
    public Parser variableModifiers() {
        return new Alternation()
            .add(variableModifier())
            .add(new TrackSequence()
                .add(variableModifiers())
                .add(variableModifier())
            )
        ;
    }

    /** VariableModifier: one of
     *      final Annotation
     */
    public Parser variableModifier() {
        return new Alternation()
            .add(new Literal("final"))
            .add(annotation())
        ;
    }

    // ---------- JLSv3¤9.1.1 -------------
    /** InterfaceModifiers:
     *      InterfaceModifier
     *      InterfaceModifiers InterfaceModifier
     */
    public Parser interfaceModifiers() {
        return new Alternation()
            .add(interfaceModifier())
            .add(new TrackSequence()
                .add(interfaceModifiers())
                .add(interfaceModifier())
            )
        ;
    }

    /** InterfaceModifier: one of
     *      Annotation public protected private
     *      abstract static strictfp
     */
    public Parser interfaceModifier() {
        return new Alternation()
            .add(annotation())
            .add(new Literal("public"))
            .add(new Literal("protected"))
            .add(new Literal("private"))
            .add(new Literal("abstract"))
            .add(new Literal("static"))
            .add(new Literal("strictfp"))
        ;
    }

    // ---------- JLSv3¤9.3 ---------------
    /** ConstantDeclaration:
     *      ConstantModifiers opt Type VariableDeclarators ;
     */
    public Parser constantDeclaration() {
        return new TrackSequence()
            .add(zeroOrOne(constantModifiers()))
            .add(type())
            .add(variableDeclarators())
            .add(new Symbol(';'))
        ;
    }

    /** ConstantModifiers:
     *      ConstantModifier
     *      ConstantModifier ConstantModifers
     */
    public Parser constantModifiers() {
        return new Alternation()
            .add(constantModifier())
            .add(new TrackSequence()
                .add(constantModifier())
                .add(constantModifiers())
            )
        ;
    }

    /** ConstantModifier: one of
     *      Annotation public static final
     */
    public Parser constantModifier() {
        return new Alternation()
            .add(annotation())
            .add(new Literal("public"))
            .add(new Literal("static"))
            .add(new Literal("final"))
        ;
    }

    // ---------- JLSv3¤9.4 ---------------
    /** AbstractMethodModifiers:
     *      AbstractMethodModifier
     *      AbstractMethodModifiers AbstractMethodModifier
     */
    public Parser abstractMethodModifiers() {
        return new Alternation()
            .add(abstractMethodModifier())
            .add(new TrackSequence()
                .add(abstractMethodModifiers())
                .add(abstractMethodModifier())
            )
        ;
    }

    /** AbstractMethodModifier: one of
     *      Annotation public abstract
     */
    public Parser abstractMethodModifier() {
        return new Alternation()
            .add(annotation())
            .add(new Literal("public"))
            .add(new Literal("abstract"))
        ;
    }

    // ---------- JLSv3¤9.7 ---------------
    /** Annotations:
     *      Annotation
     *      Annotations Annotation
     */
    public Parser annotations() {
        return new Alternation()
            .add(annotation())
            .add(new TrackSequence()
                .add(annotations())
                .add(annotation())
            )
        ;
    }

    /** Annotation:
     *      NormalAnnotation
     *      MarkerAnnotation
     *      SingleElementAnnotation
     */
    public Parser annotation() {
        return new Alternation()
            .add(normalAnnotation())
            .add(markerAnnotation())
            .add(singleElementAnnotation())
        ;
    }

    /** NormalAnnotation:
     *      @ TypeName ( ElementValuePairs opt )
     */
    public Parser normalAnnotation() {
        return new TrackSequence()
            .add(new Symbol('@'))
            .add(typeName())
            .add(new Symbol('('))
            .add(zeroOrOne(elementValuePairs()))
            .add(new Symbol(')'))
        ;
    }

    /** ElementValuePairs:
     *      ElementValuePair
     *      ElementValuePairs , ElementValuePair
     */
    public Parser elementValuePairs() {
        return new Alternation()
            .add(elementValuePair())
            .add(new TrackSequence()
                .add(elementValuePairs())
                .add(new Symbol(','))
                .add(elementValuePair())
            )
        ;
    }

    /** ElementValuePair:
     *      Identifier = ElementValue
     */
    public Parser elementValuePair() {
        return new TrackSequence()
            .add(identifier())
            .add(new Symbol('='))
            .add(elementValue())
        ;
    }

    /** ElementValue:
     *      ConditionalExpression
     *      Annotation
     *      ElementValueArrayInitializer
     */
    public Parser elementValue() {
        return new Alternation()
            .add(conditionalExpression())
            .add(annotation())
            .add(elementValueArrayInitializer())
        ;
    }

    /** ElementValueArrayInitializer:
     *      { ElementValuesopt ,opt }
     */
    public Parser elementValueArrayInitializer() {
        return new TrackSequence()
            .add(new Symbol('{'))
            .add(zeroOrOne(elementValues()))
            .add(zeroOrOne(new Symbol(',')))
            .add(new Symbol('}'))
        ;
    }

    /** ElementValues:
     *      ElementValue
     *      ElementValues , ElementValue
     */
    public Parser elementValues() {
        return new Alternation()
            .add(elementValue())
            .add(new TrackSequence()
                .add(elementValues())
                .add(new Symbol(','))
                .add(elementValue())
            )
        ;
    }

    /** MarkerAnnotation:
     *      @ TypeName
     */
    public Parser markerAnnotation() {
        return new TrackSequence()
            .add(new Symbol('@'))
            .add(typeName())
        ;
    }

    /** SingleElementAnnotation:
     *      @ TypeName ( ElementValue )
     */
    public Parser singleElementAnnotation() {
        return new TrackSequence()
            .add(new Symbol('@'))
            .add(typeName())
            .add(new Symbol('('))
            .add(elementValue())
            .add(new Symbol(')'))
        ;
    }

    // -------- JLSv3¤15.9 -----------
    /** ArgumentList:
     *      Expression
     *      ArgumentList ,Expression
     */
    public Parser argumentList() {
        return new Alternation()
            .add(expression())
            .add(new TrackSequence()
                .add(argumentList())
                .add(new Symbol(','))
                .add(expression())
            )
        ;
    }


    // todo ------- typos and abbreviations in spec ------------
    public Parser typeName() {
        return type();
    }

    public Parser expr() {
        return expression();
    }

    public Parser expressionStatement() {
        return statementExpression();
    }

    // Annotations refers to ¤15.25, but this is replaced with Expressions
    // todo - conditionalexpressions in annotations may not work, or too much might be allowed in annotations...
    public Parser conditionalExpression() {
        return expression();
    }


    // todo ---------------------------------------




    private Parser nullLiteral() {
        //todo
        return new Word();
    }

    private Parser booleanLiteral() {
        //todo
        return new Word();
    }

    private Parser stringLiteral() {
        //todo
        return new Word();
    }

    private Parser characterLiteral() {
        //todo
        return new Word();
    }

    private Parser floatingPointLiteral() {
        //todo
        return new Word();
    }

    private Parser integerLiteral() {
        //todo
        return new Word();
    }
}

