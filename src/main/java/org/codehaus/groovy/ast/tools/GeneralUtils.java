/*
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
package org.codehaus.groovy.ast.tools;

import groovy.lang.MetaProperty;
import groovy.transform.Internal;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.LoopingStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.codehaus.groovy.antlr.PrimitiveHelper.getDefaultValueForPrimitive;

/**
 * Handy methods when working with the Groovy AST. Provides factory methods for creating
 * common AST nodes and utility methods for expression and statement construction.
 *
 * <p>This utility class offers shorthand methods (often with abbreviated names) for building
 * AST structures:
 * <ul>
 *   <li>Statement constructors: {@code block()}, {@code expr()}, {@code assign()}, {@code ret()}, etc.</li>
 *   <li>Expression constructors: {@code var()}, {@code constX()}, {@code classX()}, {@code cast()}, etc.</li>
 *   <li>Operator expressions: {@code binX()}, {@code andX()}, {@code orX()}, {@code cmp()}, etc.</li>
 *   <li>Collection literals: {@code list()}, {@code map()}, {@code tuple()}, {@code array()}, etc.</li>
 *   <li>Method calls: {@code call()}, {@code invokeMethod()}, {@code staticCall()}, etc.</li>
 * </ul>
 *
 * <p>Common patterns use abbreviated names like {@code X} suffix for expression factories
 * (e.g., {@code varX()} for VariableExpression) and {@code S} suffix for statement factories
 * (e.g., {@code blockS()} for BlockStatement).
 *
 * <p><strong>Null Handling:</strong> Most methods handle null gracefully, often returning
 * empty or no-op structures rather than throwing exceptions.
 *
 * @see Expression for the base expression type
 * @see Statement for the base statement type
 * @see ClassNode for type information
 * @see GenericsUtils for generic type utilities
 */
public class GeneralUtils {
    /**
     * AST assignment operator token ("=").
     */
    public  static final Token ASSIGN        = Token.newSymbol(Types.ASSIGN                    , -1, -1);
    /**
     * AST compare-to operator token ("&lt;=&gt;").
     */
    public  static final Token CMP           = Token.newSymbol(Types.COMPARE_TO                , -1, -1);
    /**
     * AST equality operator token ("==").
     */
    public  static final Token EQ            = Token.newSymbol(Types.COMPARE_EQUAL             , -1, -1);
    /**
     * AST inequality operator token ("!=").
     */
    public  static final Token NE            = Token.newSymbol(Types.COMPARE_NOT_EQUAL         , -1, -1);
    /**
     * AST non-identity operator token ("!==").
     */
    public  static final Token NOT_IDENTICAL = Token.newSymbol(Types.COMPARE_NOT_IDENTICAL     , -1, -1);
    /**
     * AST greater-than-or-equal operator token (">=").
     */
    public  static final Token GE            = Token.newSymbol(Types.COMPARE_GREATER_THAN_EQUAL, -1, -1);
    /**
     * AST greater-than operator token (">").
     */
    public  static final Token GT            = Token.newSymbol(Types.COMPARE_GREATER_THAN      , -1, -1);
    /**
     * AST less-than-or-equal operator token ("&lt;=").
     */
    public  static final Token LE            = Token.newSymbol(Types.COMPARE_LESS_THAN_EQUAL   , -1, -1);
    /**
     * AST less-than operator token ("&lt;").
     */
    public  static final Token LT            = Token.newSymbol(Types.COMPARE_LESS_THAN         , -1, -1);
    /**
     * AST logical AND operator token ("&amp;&amp;").
     */
    public  static final Token AND           = Token.newSymbol(Types.LOGICAL_AND               , -1, -1);
    /**
     * AST logical OR operator token ("||").
     */
    public  static final Token OR            = Token.newSymbol(Types.LOGICAL_OR                , -1, -1);
    /**
     * AST instanceof keyword token.
     */
    public  static final Token INSTANCEOF    = Token.newSymbol(Types.KEYWORD_INSTANCEOF        , -1, -1);
    /**
     * AST subtraction operator token ("-").
     */
    public  static final Token MINUS         = Token.newSymbol(Types.MINUS                     , -1, -1);
    /**
     * AST addition operator token ("+").
     */
    public  static final Token PLUS          = Token.newSymbol(Types.PLUS                      , -1, -1);

    /**
     * Creates a binary expression with the AND operator joining two expressions.
     *
     * @param lhv the left-hand-side operand
     * @param rhv the right-hand-side operand
     * @return a BinaryExpression with AND operator
     * @see #binX(Expression, Token, Expression)
     */
    public static BinaryExpression andX(final Expression lhv, final Expression rhv) {
        return binX(lhv, AND, rhv);
    }

    /**
     * Creates an ArgumentListExpression from individual expression arguments.
     *
     * @param expressions the argument expressions (may be empty)
     * @return an ArgumentListExpression containing the arguments
     */
    public static ArgumentListExpression args(final Expression... expressions) {
        List<Expression> list = new ArrayList<>(expressions.length);
        Collections.addAll(list, expressions);
        return args(list);
    }

    /**
     * Creates an ArgumentListExpression from a list of expressions.
     *
     * @param expressions the list of argument expressions (may be empty)
     * @return an ArgumentListExpression containing the arguments
     */
    public static ArgumentListExpression args(final List<Expression> expressions) {
        return new ArgumentListExpression(expressions);
    }

    /**
     * Creates an ArgumentListExpression from parameter nodes, extracting their names as variable references.
     *
     * @param parameters the parameter nodes
     * @return an ArgumentListExpression with variable expressions for each parameter
     */
    public static ArgumentListExpression args(final Parameter... parameters) {
        return new ArgumentListExpression(parameters);
    }

    /**
     * Creates an ArgumentListExpression from variable names, converting each to a VariableExpression.
     *
     * @param names the variable names (non-null strings)
     * @return an ArgumentListExpression with variable expressions for each name
     */
    public static ArgumentListExpression args(final String... names) {
        return args(Arrays.stream(names).map(GeneralUtils::varX).toArray(Expression[]::new));
    }

    /**
     * Creates an ArrayExpression for the given element type and initial value expressions.
     *
     * @param elementType the {@link ClassNode} representing the array element type
     * @param initExpressions the expressions providing initial array values
     * @return an ArrayExpression with the given element type and values
     */
    public static ArrayExpression arrayX(final ClassNode elementType, List<Expression> initExpressions) {
        return new ArrayExpression(elementType, initExpressions);
    }

    /**
     * Creates an ArrayExpression for the given element type, dimensions, and initial values.
     *
     * @param elementType the {@link ClassNode} representing the array element type
     * @param initExpressions the expressions providing initial array values
     * @param sizeExpressions the expressions providing array dimensions
     * @return an ArrayExpression with the given element type, dimensions, and values
     */
    public static ArrayExpression arrayX(final ClassNode elementType, List<Expression> initExpressions, List<Expression> sizeExpressions) {
        return new ArrayExpression(elementType, initExpressions, sizeExpressions);
    }

    /**
     * Creates a CastExpression converting the given expression to the specified type.
     *
     * @param type the {@link ClassNode} to cast to
     * @param expression the expression to cast
     * @return a CastExpression with the given type and expression
     */
    public static CastExpression asX(final ClassNode type, final Expression expression) {
        return CastExpression.asExpression(type, expression);
    }

    /**
     * Creates a statement that assigns a null/empty value to the target expression.
     * Used to clear or reset variable values.
     *
     * @param target the expression to assign to
     * @return an ExpressionStatement assigning null to the target
     */
    public static Statement assignNullS(final Expression target) {
        return assignS(target, ConstantExpression.EMPTY_EXPRESSION);
    }
    /**
     * Creates an assignment statement for the supplied target and value.
     *
     * @param target the assignment target expression
     * @param value the value expression
     * @return the resulting Statement
     */
    public static Statement assignS(final Expression target, final Expression value) {
        return stmt(assignX(target, value));
    }
    /**
     * Creates an assignment expression using {@link #ASSIGN}.
     *
     * @param target the assignment target expression
     * @param value the value expression
     * @return the resulting Expression
     */
    public static Expression assignX(final Expression target, final Expression value) {
        return binX(target, ASSIGN, value);
    }

    /**
     * @since 5.0.0
     */
    public static AttributeExpression attrX(final Expression owner, final String attribute) {
        return new AttributeExpression(owner, constX(attribute));
    }
    /**
     * Creates an attribute access expression.
     *
     * @param owner the owner expression
     * @param attribute the attribute expression
     * @return the resulting AttributeExpression
     */
    public static AttributeExpression attrX(final Expression owner, final Expression attribute) {
        return new AttributeExpression(owner, attribute);
    }
    /**
     * Deprecated compatibility bridge for {@link #attrX(Expression, Expression)}.
     *
     * @param owner the owner expression
     * @param attribute the attribute expression
     * @return the resulting Expression
     */
    @Deprecated
    public static Expression  attrX$$bridge(final Expression owner, final Expression attribute) {
        return new AttributeExpression(owner, attribute);
    }
    /**
     * Creates a binary expression from operands and an operator token.
     *
     * @param left the left-hand operand
     * @param token the operator token
     * @param right the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression binX(final Expression left, final Token token, final Expression right) {
        return new BinaryExpression(left, token, right);
    }
    /**
     * Creates a block statement from statements and optional scope.
     *
     * @param scope the variable scope for the block
     * @param stmts the statements to include in the block
     * @return the resulting BlockStatement
     */
    public static BlockStatement block(final VariableScope scope, final Statement... stmts) {
        BlockStatement block = new BlockStatement();
        block.setVariableScope(scope);
        for (Statement stmt : stmts) {
            block.addStatement(stmt);
        }
        return block;
    }
    /**
     * Creates a block statement from statements and optional scope.
     *
     * @param scope the variable scope for the block
     * @param stmts the statements to include in the block
     * @return the resulting BlockStatement
     */
    public static BlockStatement block(final VariableScope scope, final List<Statement> stmts) {
        BlockStatement block = new BlockStatement();
        block.setVariableScope(scope);
        for (Statement stmt : stmts) {
            block.addStatement(stmt);
        }
        return block;
    }
    /**
     * Creates a block statement from statements and optional scope.
     *
     * @param stmts the statements to include in the block
     * @return the resulting BlockStatement
     */
    public static BlockStatement block(final Statement... stmts) {
        BlockStatement block = new BlockStatement();
        for (Statement stmt : stmts) {
            block.addStatement(stmt);
        }
        return block;
    }
    /**
     * Wraps an expression in a {@link BooleanExpression}.
     *
     * @param expr the expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression boolX(final Expression expr) {
        return new BooleanExpression(expr);
    }
    /**
     * Creates a custom {@link BytecodeExpression}.
     *
     * @param writer the bytecode writer callback
     * @return the resulting BytecodeExpression
     */
    public static BytecodeExpression bytecodeX(final Consumer<MethodVisitor> writer) {
        return new BytecodeExpression() {
            @Override
            public void visit(final MethodVisitor visitor) {
                writer.accept(visitor);
            }
        };
    }
    /**
     * Creates a custom {@link BytecodeExpression}.
     *
     * @param type the target type
     * @param writer the bytecode writer callback
     * @return the resulting BytecodeExpression
     */
    public static BytecodeExpression bytecodeX(final ClassNode type, final Consumer<MethodVisitor> writer) {
        BytecodeExpression expression = bytecodeX(writer);
        expression.setType(type);
        return expression;
    }
    /**
     * Creates a method call expression on {@code super}.
     *
     * @param methodName the method name
     * @return the resulting MethodCallExpression
     */
    public static MethodCallExpression callSuperX(final String methodName) {
        return callSuperX(methodName, MethodCallExpression.NO_ARGUMENTS);
    }
    /**
     * Creates a method call expression on {@code super}.
     *
     * @param methodName the method name
     * @param args the argument expression(s)
     * @return the resulting MethodCallExpression
     */
    public static MethodCallExpression callSuperX(final String methodName, final Expression args) {
        return callX(varX("super"), methodName, args);
    }
    /**
     * Creates a method call expression on {@code this}.
     *
     * @param methodName the method name
     * @return the resulting MethodCallExpression
     */
    public static MethodCallExpression callThisX(final String methodName) {
        return callThisX(methodName, MethodCallExpression.NO_ARGUMENTS);
    }
    /**
     * Creates a method call expression on {@code this}.
     *
     * @param methodName the method name
     * @param args the argument expression(s)
     * @return the resulting MethodCallExpression
     */
    public static MethodCallExpression callThisX(final String methodName, final Expression args) {
        return callX(varX("this"), methodName, args);
    }
    /**
     * Creates a method call expression.
     *
     * @param receiver the call receiver
     * @param methodName the method name
     * @return the resulting MethodCallExpression
     */
    public static MethodCallExpression callX(final Expression receiver, final String methodName) {
        return callX(receiver, methodName, MethodCallExpression.NO_ARGUMENTS);
    }
    /**
     * Creates a method call expression.
     *
     * @param receiver the call receiver
     * @param methodName the method name
     * @param args the argument expression(s)
     * @return the resulting MethodCallExpression
     */
    public static MethodCallExpression callX(final Expression receiver, final String methodName, final Expression args) {
        return new MethodCallExpression(receiver, methodName, args);
    }
    /**
     * Creates a method call expression.
     *
     * @param receiver the call receiver
     * @param method the method expression
     * @param args the argument expression(s)
     * @return the resulting MethodCallExpression
     */
    public static MethodCallExpression callX(final Expression receiver, final Expression method, final Expression args) {
        return new MethodCallExpression(receiver, method, args);
    }
    /**
     * Creates a method call expression.
     *
     * @param receiver the call receiver
     * @param methodName the method name
     * @return the resulting StaticMethodCallExpression
     */
    public static StaticMethodCallExpression callX(final ClassNode receiver, final String methodName) {
        return callX(receiver, methodName, MethodCallExpression.NO_ARGUMENTS);
    }
    /**
     * Creates a method call expression.
     *
     * @param receiver the call receiver
     * @param methodName the method name
     * @param args the argument expression(s)
     * @return the resulting StaticMethodCallExpression
     */
    public static StaticMethodCallExpression callX(final ClassNode receiver, final String methodName, final Expression args) {
        return new StaticMethodCallExpression(receiver, methodName, args);
    }
    /**
     * Creates a switch-case statement.
     *
     * @param expression the expression
     * @param code the statement body
     * @return the resulting CaseStatement
     */
    public static CaseStatement caseS(final Expression expression, Statement code) {
        return new CaseStatement(expression, code);
    }
    /**
     * Creates a cast expression.
     *
     * @param type the target type
     * @param expression the expression
     * @return the resulting CastExpression
     */
    public static CastExpression castX(final ClassNode type, final Expression expression) {
        return new CastExpression(type, expression);
    }
    /**
     * Creates a cast expression.
     *
     * @param type the target type
     * @param expression the expression
     * @param ignoreAutoboxing whether autoboxing should be ignored
     * @return the resulting CastExpression
     */
    public static CastExpression castX(final ClassNode type, final Expression expression, final boolean ignoreAutoboxing) {
        return new CastExpression(type, expression, ignoreAutoboxing);
    }
    /**
     * Creates a catch statement.
     *
     * @param variable the loop or catch parameter
     * @param code the statement body
     * @return the resulting CatchStatement
     */
    public static CatchStatement catchS(final Parameter variable, final Statement code) {
        return new CatchStatement(variable, code);
    }
    /**
     * Creates a class literal expression.
     *
     * @param clazz the class reference
     * @return the resulting ClassExpression
     */
    public static ClassExpression classX(final ClassNode clazz) {
        return new ClassExpression(clazz);
    }
    /**
     * Creates a class literal expression.
     *
     * @param clazz the class reference
     * @return the resulting ClassExpression
     */
    public static ClassExpression classX(final Class<?> clazz) {
        return classX(ClassHelper.make(clazz).getPlainNodeReference());
    }
    /**
     * Creates a closure expression.
     *
     * @param params the parameter array
     * @param code the statement body
     * @return the resulting ClosureExpression
     */
    public static ClosureExpression closureX(final Parameter[] params, final Statement code) {
        return new ClosureExpression(params, code);
    }
    /**
     * Creates a closure expression.
     *
     * @param code the statement body
     * @return the resulting ClosureExpression
     */
    public static ClosureExpression closureX(final Statement code) {
        return closureX(Parameter.EMPTY_ARRAY, code);
    }

    /**
     * Builds a lambda expression
     *
     * @param params lambda parameters
     * @param code lambda code
     * @return the lambda expression
     */
    public static LambdaExpression lambdaX(final Parameter[] params, final Statement code) {
        return new LambdaExpression(params, code);
    }

    /**
     * Builds a lambda expression with no parameters
     *
     * @param code lambda code
     * @return the lambda expression
     */
    public static LambdaExpression lambdaX(final Statement code) {
        return lambdaX(Parameter.EMPTY_ARRAY, code);
    }

    /**
     * Builds a binary expression that compares two values.
     *
     * @param lhv expression for the value to compare from
     * @param rhv expression for the value to compare to
     * @return the expression comparing two values
     */
    public static BinaryExpression cmpX(final Expression lhv, final Expression rhv) {
        return binX(lhv, CMP, rhv);
    }
    /**
     * Creates a constant expression.
     *
     * @param val the val
     * @return the resulting ConstantExpression
     */
    public static ConstantExpression constX(final Object val) {
        return new ConstantExpression(val);
    }
    /**
     * Creates a constant expression.
     *
     * @param val the val
     * @param keepPrimitive whether primitive-wrapper constants should be preserved
     * @return the resulting ConstantExpression
     */
    public static ConstantExpression constX(final Object val, final boolean keepPrimitive) {
        return new ConstantExpression(val, keepPrimitive);
    }
    /**
     * Creates a constructor call expression.
     *
     * @param type the target type
     * @param args the argument expression(s)
     * @return the resulting ConstructorCallExpression
     */
    public static ConstructorCallExpression ctorX(final ClassNode type, final Expression args) {
        return new ConstructorCallExpression(type, args);
    }
    /**
     * Creates a constructor call expression.
     *
     * @param type the target type
     * @return the resulting ConstructorCallExpression
     */
    public static ConstructorCallExpression ctorX(final ClassNode type) {
        return ctorX(type, ArgumentListExpression.EMPTY_ARGUMENTS);
    }
    /**
     * Creates a {@code super(...)} constructor call statement.
     *
     * @param args the argument expression(s)
     * @return the resulting Statement
     */
    public static Statement ctorSuperS(final Expression args) {
        return stmt(ctorSuperX(args));
    }
    /**
     * Creates a {@code super(...)} constructor call expression.
     *
     * @param args the argument expression(s)
     * @return the resulting ConstructorCallExpression
     */
    public static ConstructorCallExpression ctorSuperX(Expression args) {
        return ctorX(ClassNode.SUPER, args);
    }
    /**
     * Creates a {@code this(...)} constructor call statement.
     *
     * @param args the argument expression(s)
     * @return the resulting Statement
     */
    public static Statement ctorThisS(final Expression args) {
        return stmt(ctorThisX(args));
    }
    /**
     * Creates a {@code this(...)} constructor call expression.
     *
     * @param args the argument expression(s)
     * @return the resulting ConstructorCallExpression
     */
    public static ConstructorCallExpression ctorThisX(Expression args) {
        return ctorX(ClassNode.THIS, args);
    }
    /**
     * Creates a {@code super(...)} constructor call statement.
     * @return the resulting Statement
     */
    public static Statement ctorSuperS() {
        return stmt(ctorSuperX());
    }
    /**
     * Creates a {@code super(...)} constructor call expression.
     * @return the resulting ConstructorCallExpression
     */
    public static ConstructorCallExpression ctorSuperX() {
        return ctorX(ClassNode.SUPER);
    }
    /**
     * Creates a {@code this(...)} constructor call statement.
     * @return the resulting Statement
     */
    public static Statement ctorThisS() {
        return stmt(ctorThisX());
    }
    /**
     * Creates a {@code this(...)} constructor call expression.
     * @return the resulting ConstructorCallExpression
     */
    public static ConstructorCallExpression ctorThisX() {
        return ctorX(ClassNode.THIS);
    }
    /**
     * Creates a variable declaration statement.
     *
     * @param target the assignment target expression
     * @param init the initializer expression
     * @return the resulting Statement
     */
    public static Statement declS(final Expression target, final Expression init) {
        return stmt(declX(target, init));
    }
    /**
     * Creates a variable declaration expression.
     *
     * @param target the assignment target expression
     * @param init the initializer expression
     * @return the resulting DeclarationExpression
     */
    public static DeclarationExpression declX(final Expression target, final Expression init) {
        return new DeclarationExpression(target, ASSIGN, init);
    }

    /**
     * Returns a constant expression with the default value for the given type
     * (i.e., {@code false} for boolean, {@code 0} for numbers or {@code null}).
     *
     * @since 4.0.0
     */
    public static ConstantExpression defaultValueX(final ClassNode type) {
        return Optional.ofNullable((ConstantExpression) getDefaultValueForPrimitive(type)).orElse(nullX());
    }
    /**
     * Creates an Elvis operator expression.
     *
     * @param base the base expression
     * @param otherwise the fallback expression
     * @return the resulting ElvisOperatorExpression
     */
    public static ElvisOperatorExpression elvisX(final Expression base, final Expression otherwise) {
        return new ElvisOperatorExpression(base, otherwise);
    }
    /**
     * Creates a map entry expression.
     *
     * @param key the key expression
     * @param value the value expression
     * @return the resulting MapEntryExpression
     */
    public static MapEntryExpression entryX(final Expression key, final Expression value) {
        return new MapEntryExpression(key, value);
    }
    /**
     * Creates an equality comparison expression.
     *
     * @param left the left-hand operand
     * @param right the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression eqX(final Expression left, final Expression right) {
        return binX(left, EQ, right);
    }
    /**
     * Creates a null-equality test expression.
     *
     * @param expr the expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression equalsNullX(final Expression expr) {
        return boolX(eqX(nullX(), expr));
    }
    /**
     * Creates a field expression.
     *
     * @param fieldNode the fieldNode
     * @return the resulting FieldExpression
     */
    public static FieldExpression fieldX(final FieldNode fieldNode) {
        return new FieldExpression(fieldNode);
    }
    /**
     * Creates a field expression.
     *
     * @param owner the owner expression
     * @param fieldName the field name
     * @return the resulting FieldExpression
     */
    public static FieldExpression fieldX(final ClassNode owner, final String fieldName) {
        return new FieldExpression(owner.getField(fieldName));
    }
    /**
     * Creates an expression that resolves an entry from script args.
     *
     * @param argName the argument name
     * @return the resulting Expression
     */
    public static Expression findArg(final String argName) {
        return propX(varX("args"), argName);
    }
    /**
     * Creates a for-loop statement.
     *
     * @param variable the loop or catch parameter
     * @param collectionExpression the collection expression
     * @param loopS the loop body statement
     * @return the resulting ForStatement
     */
    public static ForStatement forS(Parameter variable, Expression collectionExpression, Statement loopS) {
        return new ForStatement(variable, collectionExpression, loopS);
    }
    /**
     * Collects methods declared on a class and its super classes.
     *
     * @param type the target type
     * @return the resulting List<MethodNode>
     */
    public static List<MethodNode> getAllMethods(final ClassNode type) {
        ClassNode node = type;
        List<MethodNode> result = new ArrayList<>();
        while (node != null) {
            result.addAll(node.getMethods());
            node = node.getSuperClass();
        }
        return result;
    }
    /**
     * Collects properties according to the supplied inclusion flags.
     *
     * @param type the target type
     * @return the resulting List<PropertyNode>
     */
    public static List<PropertyNode> getAllProperties(final ClassNode type) {
        ClassNode node = type;
        List<PropertyNode> result = new ArrayList<>();
        while (node != null) {
            result.addAll(node.getProperties());
            node = node.getSuperClass();
        }
        return result;
    }
    /**
     * Collects instance fields that are not exposed as properties.
     *
     * @param cNode the class node
     * @return the resulting List<FieldNode>
     */
    public static List<FieldNode> getInstanceNonPropertyFields(final ClassNode cNode) {
        List<FieldNode> result = new ArrayList<>();
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }
    /**
     * Collects names of instance fields that are not exposed as properties.
     *
     * @param cNode the class node
     * @return the resulting List<String>
     */
    public static List<String> getInstanceNonPropertyFieldNames(final ClassNode cNode) {
        List<FieldNode> fList = getInstanceNonPropertyFields(cNode);
        List<String> result = new ArrayList<>(fList.size());
        for (FieldNode fNode : fList) {
            result.add(fNode.getName());
        }
        return result;
    }
    /**
     * Collects instance (non-static) properties.
     *
     * @param cNode the class node
     * @return the resulting List<PropertyNode>
     */
    public static List<PropertyNode> getInstanceProperties(final ClassNode cNode) {
        List<PropertyNode> result = new ArrayList<>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode);
            }
        }
        return result;
    }
    /**
     * Collects names of instance properties.
     *
     * @param cNode the class node
     * @return the resulting List<String>
     */
    public static List<String> getInstancePropertyNames(final ClassNode cNode) {
        List<PropertyNode> pList = BeanUtils.getAllProperties(cNode, false, false, true);
        List<String> result = new ArrayList<>(pList.size());
        for (PropertyNode pNode : pList) {
            result.add(pNode.getName());
        }
        return result;
    }
    /**
     * Collects backing fields for instance properties.
     *
     * @param cNode the class node
     * @return the resulting List<FieldNode>
     */
    public static List<FieldNode> getInstancePropertyFields(final ClassNode cNode) {
        List<FieldNode> result = new ArrayList<>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }

    private static void addAllInterfaces(final Set<ClassNode> result, final ClassNode source) {
        UnaryOperator<ClassNode> sourceTypeArgs = (ClassNode cn) -> {
            if (source.isRedirectNode() && GenericsUtils.hasUnresolvedGenerics(cn)) { // GROOVY-11707
                if (source.getGenericsTypes() == null && source.redirect().getGenericsTypes() != null) {
                    cn = cn.getPlainNodeReference(); // GROOVY-11736
                } else {
                    cn = GenericsUtils.parameterizeType(source, cn);
                }
            }
            return cn;
        };

        for (ClassNode in : source.getInterfaces()) {
            in = sourceTypeArgs.apply(in);
            if (result.add(in)) {
                addAllInterfaces(result, in);
            }
        }

        ClassNode sc = source.redirect().getUnresolvedSuperClass(false);
        if (sc != null && !ClassHelper.isObjectType(sc)) {
            addAllInterfaces(result, sourceTypeArgs.apply(sc));
        }
    }
    /**
     * Collects interfaces implemented directly or transitively.
     *
     * @param cNode the class node
     * @return the resulting Set<ClassNode>
     */
    public static Set<ClassNode> getInterfacesAndSuperInterfaces(final ClassNode cNode) {
        Set<ClassNode> result = new LinkedHashSet<>();
        if (cNode.isInterface()) result.add(cNode);
        addAllInterfaces(result, cNode);
        return result;
    }
    /**
     * Collects inherited instance fields that are not properties.
     *
     * @param cNode the class node
     * @return the resulting List<FieldNode>
     */
    public static List<FieldNode> getSuperNonPropertyFields(final ClassNode cNode) {
        List<FieldNode> result;
        if (ClassHelper.isObjectType(cNode)) {
            result = new ArrayList<>();
        } else {
            result = getSuperNonPropertyFields(cNode.getSuperClass());
        }
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }
    /**
     * Collects inherited property backing fields.
     *
     * @param cNode the class node
     * @return the resulting List<FieldNode>
     */
    public static List<FieldNode> getSuperPropertyFields(final ClassNode cNode) {
        List<FieldNode> result;
        if (ClassHelper.isObjectType(cNode)) {
            result = new ArrayList<>();
        } else {
            result = getSuperPropertyFields(cNode.getSuperClass());
        }
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }
    /**
     * Collects properties according to the supplied inclusion flags.
     *
     * @param names the set tracking already-added property names
     * @param cNode the class node
     * @param includeProperties whether declared properties are included
     * @param includeFields whether fields are exposed as properties
     * @param includePseudoGetters whether pseudo-getter properties are included
     * @param includePseudoSetters whether pseudo-setter properties are included
     * @param traverseSuperClasses whether super classes are traversed
     * @param skipReadonly whether read-only initialized fields are excluded
     * @return the resulting List<PropertyNode>
     */
    public static List<PropertyNode> getAllProperties(final Set<String> names, final ClassNode cNode, final boolean includeProperties, final boolean includeFields, final boolean includePseudoGetters, final boolean includePseudoSetters, final boolean traverseSuperClasses, final boolean skipReadonly) {
        return getAllProperties(names, cNode, cNode, includeProperties, includeFields, includePseudoGetters, includePseudoSetters, traverseSuperClasses, skipReadonly);
    }
    /**
     * Collects properties according to the supplied inclusion flags.
     *
     * @param names the set tracking already-added property names
     * @param origType the original class node used for pseudo-property resolution
     * @param cNode the class node
     * @param includeProperties whether declared properties are included
     * @param includeFields whether fields are exposed as properties
     * @param includePseudoGetters whether pseudo-getter properties are included
     * @param includePseudoSetters whether pseudo-setter properties are included
     * @param traverseSuperClasses whether super classes are traversed
     * @param skipReadonly whether read-only initialized fields are excluded
     * @return the resulting List<PropertyNode>
     */
    public static List<PropertyNode> getAllProperties(final Set<String> names, final ClassNode origType, final ClassNode cNode, final boolean includeProperties, final boolean includeFields, final boolean includePseudoGetters, final boolean includePseudoSetters, final boolean traverseSuperClasses, final boolean skipReadonly) {
        return getAllProperties(names, origType, cNode, includeProperties, includeFields, includePseudoGetters, includePseudoSetters, traverseSuperClasses, skipReadonly, false, false, false);
    }
    /**
     * Collects properties according to the supplied inclusion flags.
     *
     * @param names the set tracking already-added property names
     * @param origType the original class node used for pseudo-property resolution
     * @param cNode the class node
     * @param includeProperties whether declared properties are included
     * @param includeFields whether fields are exposed as properties
     * @param includePseudoGetters whether pseudo-getter properties are included
     * @param includePseudoSetters whether pseudo-setter properties are included
     * @param traverseSuperClasses whether super classes are traversed
     * @param skipReadonly whether read-only initialized fields are excluded
     * @param reverse whether superclass traversal order is reversed
     * @param allNames whether special/internal field names are included
     * @param includeStatic whether static members are included
     * @return the resulting List<PropertyNode>
     */
    public static List<PropertyNode> getAllProperties(final Set<String> names, final ClassNode origType, final ClassNode cNode, final boolean includeProperties,
                                                      final boolean includeFields, final boolean includePseudoGetters, final boolean includePseudoSetters,
                                                      final boolean traverseSuperClasses, final boolean skipReadonly, final boolean reverse, final boolean allNames, final boolean includeStatic) {
        List<PropertyNode> result = new ArrayList<>();
        if (!(ClassHelper.isObjectType(cNode)) && traverseSuperClasses && !reverse) {
            result.addAll(getAllProperties(names, origType, cNode.getSuperClass(), includeProperties, includeFields, includePseudoGetters, includePseudoSetters, true, skipReadonly));
        }
        if (includeProperties) {
            for (PropertyNode pNode : cNode.getProperties()) {
                if ((!pNode.isStatic() || includeStatic) && !names.contains(pNode.getName())) {
                    result.add(pNode);
                    names.add(pNode.getName());
                }
            }
            if (includePseudoGetters || includePseudoSetters) {
                BeanUtils.addPseudoProperties(origType, cNode, result, names, includeStatic, includePseudoGetters, includePseudoSetters, traverseSuperClasses);
            }
        }
        if (includeFields) {
            for (FieldNode fNode : cNode.getFields()) {
                if ((fNode.isStatic() && !includeStatic) || fNode.isSynthetic()) {
                    continue;
                }
                if (fNode.isPrivate() && !cNode.equals(origType)) {
                    continue;
                }
                String fName = fNode.getName();
                if (names.contains(fName) || cNode.getProperty(fName) != null) {
                    continue;
                }
                if (!allNames && (fName.contains("$") || fName.contains("__"))) { // "special"
                    continue;
                }
                if (fNode.isFinal() && fNode.getInitialExpression() != null && skipReadonly) {
                    continue;
                }
                names.add(fName);
                result.add(new PropertyNode(fNode, fNode.getModifiers() & 0x1F, null, null));
            }
        }
        if (!(ClassHelper.isObjectType(cNode)) && traverseSuperClasses && reverse) {
            result.addAll(getAllProperties(names, origType, cNode.getSuperClass(), includeProperties, includeFields, includePseudoGetters, includePseudoSetters, true, skipReadonly));
        }
        return result;
    }

    /**
     * This method is similar to {@link #propX(Expression, Expression)} but will make sure that if the property
     * being accessed is defined inside the classnode provided as a parameter, then a getter call is generated
     * instead of a field access.
     * @param annotatedNode the class node where the property node is accessed from
     * @param pNode the property being accessed
     * @return a method call expression or a property expression
     */
    public static Expression getterThisX(final ClassNode annotatedNode, final PropertyNode pNode) {
        ClassNode owner = pNode.getDeclaringClass();
        if (annotatedNode.equals(owner)) {
            return callThisX(pNode.getGetterNameOrDefault());
        }
        return propX(varX("this"), pNode.getName());
    }

    /**
     * This method is similar to {@link #propX(Expression, Expression)} but will make sure that if the property
     * being accessed is defined inside the classnode provided as a parameter, then a getter call is generated
     * instead of a field access.
     * @param annotatedNode the class node where the property node is accessed from
     * @param receiver the object having the property
     * @param pNode the property being accessed
     * @return a method call expression or a property expression
     */
    public static Expression getterX(final ClassNode annotatedNode, final Expression receiver, final PropertyNode pNode) {
        ClassNode owner = pNode.getDeclaringClass();
        if (annotatedNode.equals(owner)) {
            return callX(receiver, pNode.getGetterNameOrDefault());
        }
        return propX(receiver, pNode.getName());
    }
    /**
     * Creates a greater-than-or-equal comparison expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression geX(final Expression lhv, final Expression rhv) {
        return binX(lhv, GE, rhv);
    }
    /**
     * Creates a greater-than comparison expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression gtX(final Expression lhv, final Expression rhv) {
        return binX(lhv, GT, rhv);
    }
    /**
     * Creates an expression testing whether {@code instance.getClass()} equals the supplied class.
     *
     * @param instance the instance expression
     * @param cNode the class node
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression hasClassX(final Expression instance, final ClassNode cNode) {
        return eqX(classX(cNode), callX(instance, "getClass"));
    }
    /**
     * Creates an equality comparison against another object's field value.
     *
     * @param fNode the field node
     * @param other the other object/value expression
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression hasEqualFieldX(final FieldNode fNode, final Expression other) {
        return eqX(varX(fNode), propX(other, fNode.getName()));
    }
    /**
     * Creates an equality comparison against another object's property value.
     *
     * @param cNode the class node
     * @param pNode the property node
     * @param other the other object/value expression
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression hasEqualPropertyX(final ClassNode cNode, final PropertyNode pNode, final VariableExpression other) {
        return eqX(getterThisX(cNode, pNode), getterX(other.getOriginType(), other, pNode));
    }
    /**
     * Creates an equality comparison against another object's property value.
     *
     * @param pNode the property node
     * @param other the other object/value expression
     * @return the resulting BinaryExpression
     */
    @Deprecated
    public static BinaryExpression hasEqualPropertyX(final PropertyNode pNode, final Expression other) {
        String getterName = pNode.getGetterNameOrDefault();
        return eqX(callThisX(getterName), callX(other, getterName));
    }
    /**
     * Creates an identity comparison against another object's field value.
     *
     * @param fNode the field node
     * @param other the other object/value expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression hasSameFieldX(final FieldNode fNode, final Expression other) {
        return sameX(varX(fNode), propX(other, fNode.getName()));
    }
    /**
     * Creates an identity comparison against another object's property value.
     *
     * @param pNode the property node
     * @param other the other object/value expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression hasSamePropertyX(final PropertyNode pNode, final Expression other) {
        ClassNode cNode = pNode.getDeclaringClass();
        return sameX(getterThisX(cNode, pNode), getterX(cNode, other, pNode));
    }
    /**
     * Deprecated compatibility bridge for {@link #ifElseS(Expression, Statement, Statement)}.
     *
     * @param cond the condition expression
     * @param thenStmt the true-branch statement
     * @param elseStmt the false-branch statement
     * @return the resulting Statement
     */
    @Deprecated
    public static Statement ifElseS$$bridge(final Expression cond, final Statement thenStmt, final Statement elseStmt) {
        return ifElseS(cond, thenStmt, elseStmt);
    }
    /**
     * Creates an if/else statement.
     *
     * @param cond the condition expression
     * @param thenStmt the true-branch statement
     * @param elseStmt the false-branch statement
     * @return the resulting IfStatement
     */
    public static IfStatement ifElseS(final Expression cond, final Statement thenStmt, final Statement elseStmt) {
        return new IfStatement(
                cond instanceof BooleanExpression ? (BooleanExpression) cond : boolX(cond),
                thenStmt,
                elseStmt
        );
    }
    /**
     * Deprecated compatibility bridge for the {@code ifS} overloads.
     *
     * @param cond the condition expression
     * @param trueExpr the true-branch expression
     * @return the resulting Statement
     */
    @Deprecated
    public static Statement ifS$$bridge(final Expression cond, final Expression trueExpr) {
        return ifS(cond, trueExpr);
    }
    /**
     * Creates an if statement with an empty else branch.
     *
     * @param cond the condition expression
     * @param trueExpr the true-branch expression
     * @return the resulting IfStatement
     */
    public static IfStatement ifS(final Expression cond, final Expression trueExpr) {
        return ifElseS(cond, stmt(trueExpr), EmptyStatement.INSTANCE);
    }
    /**
     * Deprecated compatibility bridge for the {@code ifS} overloads.
     *
     * @param cond the condition expression
     * @param trueStmt the true-branch statement
     * @return the resulting Statement
     */
    @Deprecated
    public static Statement ifS$$bridge(final Expression cond, final Statement trueStmt) {
        return ifS(cond, trueStmt);
    }
    /**
     * Creates an if statement with an empty else branch.
     *
     * @param cond the condition expression
     * @param trueStmt the true-branch statement
     * @return the resulting IfStatement
     */
    public static IfStatement ifS(final Expression cond, final Statement trueStmt) {
        return ifElseS(cond, trueStmt, EmptyStatement.INSTANCE);
    }
    /**
     * Creates an index access expression.
     *
     * @param target the assignment target expression
     * @param value the value expression
     * @return the resulting Expression
     */
    public static Expression indexX(final Expression target, final Expression value) {
        return binX(target, Token.newSymbol(Types.LEFT_SQUARE_BRACKET, -1, -1), value);
    }
    /**
     * Creates an {@code instanceof} test expression.
     *
     * @param expr the expression
     * @param type the target type
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression isInstanceOfX(final Expression expr, final ClassNode type) {
        return boolX(binX(expr, INSTANCEOF, classX(type)));
    }

    /**
     * @since 4.0.8
     */
    public static BooleanExpression isNullOrInstanceOfX(final Expression expr, final ClassNode type) {
        return boolX(orX(binX(nullX(), Token.newSymbol(Types.COMPARE_IDENTICAL, -1, -1), expr), binX(expr, INSTANCEOF, classX(type))));
    }

    /**
     * Alias for {@link #equalsNullX(Expression)}
     */
    public static BooleanExpression isNullX(final Expression expr) {
        return equalsNullX(expr);
    }
    /**
     * Creates an equality test against numeric one.
     *
     * @param expr the expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression isOneX(final Expression expr) {
        return boolX(binX(expr, EQ, constX(1)));
    }
    /**
     * Creates an equality test against {@link Boolean#TRUE}.
     *
     * @param argExpr the argument expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression isTrueX(final Expression argExpr) {
        return boolX(binX(argExpr, EQ, constX(Boolean.TRUE)));
    }
    /**
     * Creates an equality test against numeric zero.
     *
     * @param expr the expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression isZeroX(final Expression expr) {
        return boolX(binX(expr, EQ, constX(0)));
    }
    /**
     * Creates a list expression.
     *
     * @param args the argument expression(s)
     * @return the resulting ListExpression
     */
    public static ListExpression listX(final List<Expression> args) {
        return new ListExpression(args);
    }
    /**
     * Converts values into a constant-based list expression.
     *
     * @param args the argument expression(s)
     * @return the resulting ListExpression
     */
    public static ListExpression list2args(final List<?> args) {
        ListExpression result = new ListExpression();
        for (Object o : args) {
            result.addExpression(constX(o));
        }
        return result;
    }
    /**
     * Converts class-name strings into class-literal expressions.
     *
     * @param args the argument expression(s)
     * @return the resulting ListExpression
     */
    public static ListExpression classList2args(final List<String> args) {
        ListExpression result = new ListExpression();
        for (Object o : args) {
            result.addExpression(classX(ClassHelper.make(o.toString())));
        }
        return result;
    }
    /**
     * Creates a local variable expression and marks it as the accessed variable.
     *
     * @param name the property name
     * @return the resulting VariableExpression
     */
    public static VariableExpression localVarX(final String name) {
        VariableExpression result = varX(name);
        result.setAccessedVariable(result);
        return result;
    }
    /**
     * Creates a local variable expression and marks it as the accessed variable.
     *
     * @param name the property name
     * @param type the target type
     * @return the resulting VariableExpression
     */
    public static VariableExpression localVarX(final String name, final ClassNode type) {
        VariableExpression result = varX(name, type);
        result.setAccessedVariable(result);
        return result;
    }
    /**
     * Creates a less-than-or-equal comparison expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression leX(final Expression lhv, final Expression rhv) {
        return binX(lhv, LE, rhv);
    }
    /**
     * Creates a less-than comparison expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression ltX(final Expression lhv, final Expression rhv) {
        return binX(lhv, LT, rhv);
    }
    /**
     * Creates a map entry expression.
     *
     * @param keyExpr the map-entry key expression
     * @param valueExpr the map-entry value expression
     * @return the resulting MapEntryExpression
     */
    public static MapEntryExpression mapEntryX(final Expression keyExpr, final Expression valueExpr) {
        return new MapEntryExpression(keyExpr, valueExpr);
    }
    /**
     * Creates a map entry expression.
     *
     * @param key the key expression
     * @param valueExpr the map-entry value expression
     * @return the resulting MapEntryExpression
     */
    public static MapEntryExpression mapEntryX(final String key, final Expression valueExpr) {
        return new MapEntryExpression(constX(key), valueExpr);
    }
    /**
     * Creates a map expression.
     * @return the resulting MapExpression
     */
    public static MapExpression mapX() {
        return new MapExpression();
    }
    /**
     * Creates a map expression.
     *
     * @param expressions the map-entry expressions
     * @return the resulting MapExpression
     */
    public static MapExpression mapX(final List<MapEntryExpression> expressions) {
        return new MapExpression(expressions);
    }
    /**
     * Creates a subtraction expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression minusX(final Expression lhv, final Expression rhv) {
        return binX(lhv, MINUS, rhv);
    }
    /**
     * Creates an inequality comparison expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression neX(final Expression lhv, final Expression rhv) {
        return binX(lhv, NE, rhv);
    }
    /**
     * Creates a non-identity comparison expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression notIdenticalX(final Expression lhv, final Expression rhv) {
        return binX(lhv, NOT_IDENTICAL, rhv);
    }
    /**
     * Creates a non-null test expression.
     *
     * @param expr the expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression notNullX(final Expression expr) {
        return boolX(binX(nullX(), NE, expr));
    }
    /**
     * Creates a logical NOT expression.
     *
     * @param expr the expression
     * @return the resulting NotExpression
     */
    public static NotExpression notX(final Expression expr) {
        return new NotExpression(expr instanceof BooleanExpression ? expr : boolX(expr));
    }
    /**
     * Creates a {@code null} constant expression.
     * @return the resulting ConstantExpression
     */
    public static ConstantExpression nullX() {
        return constX(null);
    }
    /**
     * Creates a logical OR expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression orX(final Expression lhv, final Expression rhv) {
        return binX(lhv, OR, rhv);
    }
    /**
     * Creates a parameter node.
     *
     * @param type the target type
     * @param name the property name
     * @return the resulting Parameter
     */
    public static Parameter param(final ClassNode type, final String name) {
        return param(type, name, null);
    }
    /**
     * Creates a parameter node.
     *
     * @param type the target type
     * @param name the property name
     * @param initialExpression the initial expression for the parameter
     * @return the resulting Parameter
     */
    public static Parameter param(final ClassNode type, final String name, final Expression initialExpression) {
        Parameter param = new Parameter(type, name);
        if (initialExpression != null) {
            param.setInitialExpression(initialExpression);
        }
        return param;
    }
    /**
     * Returns the input parameters or {@link Parameter#EMPTY_ARRAY} when input is {@code null}.
     *
     * @param params the parameter array
     * @return the resulting Parameter[]
     */
    public static Parameter[] params(final Parameter... params) {
        return (params != null ? params : Parameter.EMPTY_ARRAY);
    }
    /**
     * Creates an addition expression.
     *
     * @param lhv the left-hand operand
     * @param rhv the right-hand operand
     * @return the resulting BinaryExpression
     */
    public static BinaryExpression plusX(final Expression lhv, final Expression rhv) {
        return binX(lhv, PLUS, rhv);
    }
    /**
     * Creates a property access expression.
     *
     * @param owner the owner expression
     * @param property the property name/expression
     * @return the resulting PropertyExpression
     */
    public static PropertyExpression propX(final Expression owner, final String property) {
        return new PropertyExpression(owner, property);
    }
    /**
     * Deprecated compatibility bridge for the {@code propX} overloads.
     *
     * @param owner the owner expression
     * @param property the property name/expression
     * @return the resulting Expression
     */
    @Deprecated
    public static Expression propX$$bridge(final Expression owner, final String property) {
        return propX(owner, property);
    }
    /**
     * Creates a property access expression.
     *
     * @param owner the owner expression
     * @param property the property name/expression
     * @return the resulting PropertyExpression
     */
    public static PropertyExpression propX(final Expression owner, final Expression property) {
        return new PropertyExpression(owner, property);
    }
    /**
     * Deprecated compatibility bridge for the {@code propX} overloads.
     *
     * @param owner the owner expression
     * @param property the property name/expression
     * @return the resulting Expression
     */
    @Deprecated
    public static Expression propX$$bridge(final Expression owner, final Expression property) {
        return propX(owner, property);
    }
    /**
     * Creates a property access expression.
     *
     * @param owner the owner expression
     * @param property the property name/expression
     * @param safe whether safe-navigation is enabled
     * @return the resulting PropertyExpression
     */
    public static PropertyExpression propX(final Expression owner, final Expression property, final boolean safe) {
        return new PropertyExpression(owner, property, safe);
    }
    /**
     * Creates a return statement.
     *
     * @param expr the expression
     * @return the resulting Statement
     */
    public static Statement returnS(final Expression expr) {
        return new ReturnStatement(expr);
    }
    /**
     * Creates a null-guarded statement sequence.
     *
     * @param fieldExpr the field expression used in the guard
     * @param expression the expression
     * @return the resulting Statement
     */
    public static Statement safeExpression(final Expression fieldExpr, final Expression expression) {
        return new IfStatement(isNullX(fieldExpr), stmt(fieldExpr), stmt(expression));
    }
    /**
     * Creates an identity test expression using Groovy's {@code is} method.
     *
     * @param self the left-hand expression
     * @param other the other object/value expression
     * @return the resulting BooleanExpression
     */
    public static BooleanExpression sameX(final Expression self, final Expression other) {
        return boolX(callX(self, "is", args(other)));
    }
    /**
     * Wraps an expression in an {@link ExpressionStatement}.
     *
     * @param expr the expression
     * @return the resulting Statement
     */
    public static Statement stmt(final Expression expr) {
        return new ExpressionStatement(expr);
    }
    /**
     * Creates a spread expression.
     *
     * @param expr the expression
     * @return the resulting SpreadExpression
     */
    public static SpreadExpression spreadX(final Expression expr) {
        return new SpreadExpression(expr);
    }
    /**
     * Creates a switch statement.
     *
     * @param expr the expression
     * @return the resulting SwitchStatement
     */
    public static SwitchStatement switchS(final Expression expr) {
        return new SwitchStatement(expr);
    }
    /**
     * Creates a switch statement.
     *
     * @param expr the expression
     * @param defaultStatement the default branch statement
     * @return the resulting SwitchStatement
     */
    public static SwitchStatement switchS(final Expression expr, final Statement defaultStatement) {
        return new SwitchStatement(expr, defaultStatement);
    }
    /**
     * Creates a switch statement.
     *
     * @param expr the expression
     * @param caseStatements the case statements
     * @param defaultStatement the default branch statement
     * @return the resulting SwitchStatement
     */
    public static SwitchStatement switchS(final Expression expr, final List<CaseStatement> caseStatements, final Statement defaultStatement) {
        return new SwitchStatement(expr, caseStatements, defaultStatement);
    }
    /**
     * Creates a ternary expression.
     *
     * @param cond the condition expression
     * @param trueExpr the true-branch expression
     * @param elseExpr the false-branch expression
     * @return the resulting TernaryExpression
     */
    public static TernaryExpression ternaryX(final Expression cond, final Expression trueExpr, final Expression elseExpr) {
        return new TernaryExpression(
                cond instanceof BooleanExpression ? (BooleanExpression) cond : boolX(cond),
                trueExpr,
                elseExpr);
    }
    /**
     * Creates a property expression on {@code this} with configurable implicit-this behavior.
     *
     * @param implicit whether the receiver is implicit this
     * @param property the property name/expression
     * @return the resulting PropertyExpression
     */
    public static PropertyExpression thisPropX(final boolean implicit, final String property) {
        PropertyExpression pexp = propX(varX("this"), property);
        pexp.setImplicitThis(implicit);
        return pexp;
    }
    /**
     * Creates a throw statement.
     *
     * @param expr the expression
     * @return the resulting ThrowStatement
     */
    public static ThrowStatement throwS(final Expression expr) {
        return new ThrowStatement(expr);
    }
    /**
     * Creates a try/catch/finally statement.
     *
     * @param tryStatement the try-block statement
     * @return the resulting TryCatchStatement
     */
    public static TryCatchStatement tryCatchS(final Statement tryStatement) {
        return tryCatchS(tryStatement, EmptyStatement.INSTANCE);
    }
    /**
     * Creates a try/catch/finally statement.
     *
     * @param tryStatement the try-block statement
     * @param finallyStatement the finally-block statement
     * @return the resulting TryCatchStatement
     */
    public static TryCatchStatement tryCatchS(final Statement tryStatement, final Statement finallyStatement) {
        return new TryCatchStatement(tryStatement, finallyStatement);
    }
    /**
     * Creates a try/catch/finally statement.
     *
     * @param tryStatement the try-block statement
     * @param finallyStatement the finally-block statement
     * @param catchStatements the catch statements
     * @return the resulting TryCatchStatement
     */
    public static TryCatchStatement tryCatchS(final Statement tryStatement, final Statement finallyStatement, final CatchStatement... catchStatements) {
        TryCatchStatement result = new TryCatchStatement(tryStatement, finallyStatement);
        for (CatchStatement catchStatement : catchStatements) {
            result.addCatch(catchStatement);
        }
        return result;
    }
    /**
     * Creates a variable expression.
     *
     * @param name the property name
     * @return the resulting VariableExpression
     */
    public static VariableExpression varX(final String name) {
        return new VariableExpression(name);
    }
    /**
     * Creates a variable expression.
     *
     * @param variable the loop or catch parameter
     * @return the resulting VariableExpression
     */
    public static VariableExpression varX(final Variable variable) {
        return new VariableExpression(variable);
    }
    /**
     * Creates a variable expression.
     *
     * @param name the property name
     * @param type the target type
     * @return the resulting VariableExpression
     */
    public static VariableExpression varX(final String name, final ClassNode type) {
        return new VariableExpression(name, type);
    }

    //--------------------------------------------------------------------------
    /**
     * Clones parameter metadata as new {@link Parameter} instances with original type and name.
     *
     * @param parameters the parameters to clone
     * @return the resulting Parameter[]
     */
    public static Parameter[] cloneParams(final Parameter[] parameters) {
        if (parameters == null || parameters.length == 0) return parameters;
        return Arrays.stream(parameters).map(p -> param(p.getOriginType(), p.getName())).toArray(Parameter[]::new);
    }

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported at present.
     */
    public static void copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, final List<AnnotationNode> copied, final List<AnnotationNode> notCopied) {
        copyAnnotatedNodeAnnotations(annotatedNode, copied, notCopied, true);
    }

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * {@link groovy.transform.Generated} annotations will be copied if {@code includeGenerated} is true.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported at present.
     */
    public static void copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, final List<AnnotationNode> copied, final List<AnnotationNode> notCopied, final boolean includeGenerated) {
        List<AnnotationNode> annotationList = annotatedNode.getAnnotations();
        for (AnnotationNode annotation : annotationList)  {
            List<AnnotationNode> annotations = annotation.getClassNode().getAnnotations(AbstractASTTransformation.RETENTION_CLASSNODE);
            if (annotations.isEmpty()) continue;

            if (hasClosureMember(annotation)) {
                notCopied.add(annotation);
                continue;
            }

            if (!includeGenerated && "groovy.transform.Generated".equals(annotation.getClassNode().getName())) {
                continue;
            }

            AnnotationNode retentionPolicyAnnotation = annotations.get(0);
            Expression valueExpression = retentionPolicyAnnotation.getMember("value");
            if (!(valueExpression instanceof PropertyExpression propertyExpression)) continue;

            boolean processAnnotation = propertyExpression.getProperty() instanceof ConstantExpression
                    && ("RUNTIME".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue())
                        || "CLASS".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue()));
            if (processAnnotation)  {
                AnnotationNode newAnnotation = new AnnotationNode(annotation.getClassNode());
                for (Map.Entry<String, Expression> member : annotation.getMembers().entrySet())  {
                    newAnnotation.addMember(member.getKey(), member.getValue());
                }
                newAnnotation.setSourcePosition(annotatedNode);

                copied.add(newAnnotation);
            }
        }
    }
    /**
     * Builds default constructor-assignment logic for a field and matching argument.
     *
     * @param fNode the field node
     * @return the resulting Statement
     */
    public static Statement createConstructorStatementDefault(final FieldNode fNode) {
        final String name = fNode.getName();
        final ClassNode fType = fNode.getType();
        final Expression fieldExpr = propX(varX("this"), name);
        Expression initExpr = fNode.getInitialValueExpression();
        Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            if (ClassHelper.isPrimitiveType(fType)) {
                assignInit = EmptyStatement.INSTANCE;
            } else {
                assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
            }
        } else {
            assignInit = assignS(fieldExpr, initExpr);
        }
        fNode.setInitialValueExpression(null);
        Expression value = findArg(name);
        return ifElseS(isNullX(value), assignInit, assignS(fieldExpr, castX(fType, value)));
    }

    /**
     * Generally preferred to use {@link PropertyNode#getGetterNameOrDefault()} directly.
     */
    public static String getGetterName(final PropertyNode pNode) {
        return pNode.getGetterNameOrDefault();
    }

    /**
     * WARNING: Avoid this method unless just the name and type are available.
     * Use {@link #getGetterName(PropertyNode)} if the propertyNode is available.
     */
    public static String getGetterName(final String name, final Class<?> type) {
        return MetaProperty.getGetterName(name, type);
    }

    /**
     * WARNING: Avoid this method unless just the name is available.
     * Use {@link #getGetterName(PropertyNode)} if the propertyNode is available.
     * Use {@link #getGetterName(String, Class)} if the type is available.
     */
    public static String getGetterName(final String name) {
        return MetaProperty.getGetterName(name, Object.class);
    }
    /**
     * Computes the JavaBean setter name for a property.
     *
     * @param name the property name
     * @return the resulting String
     */
    public static String getSetterName(final String name) {
        return MetaProperty.getSetterName(name);
    }

    /**
     * Converts an expression into the String source. Only some specific expressions like closure expression
     * support this.
     *
     * @param readerSource a source
     * @param expression an expression. Can't be null
     * @return the source the closure was created from
     * @throws java.lang.IllegalArgumentException when expression is null
     * @throws java.lang.Exception when closure can't be read from source
     */
    public static String convertASTToSource(final ReaderSource readerSource, final ASTNode expression) throws Exception {
        if (expression == null) throw new IllegalArgumentException("Null: expression");

        StringBuilder result = new StringBuilder();
        for (int x = expression.getLineNumber(), y = expression.getLastLineNumber(); x <= y; x += 1) {
            String line = readerSource.getLine(x, null);
            if (line == null) {
                throw new Exception(
                        "Error calculating source code for expression. Trying to read line " + x + " from " + readerSource.getClass()
                );
            }
            if (x == expression.getLastLineNumber()) {
                line = line.substring(0, expression.getLastColumnNumber() - 1);
            }
            if (x == expression.getLineNumber()) {
                line = line.substring(expression.getColumnNumber() - 1);
            }
            //restoring line breaks is important b/c of lack of semicolons
            result.append(line).append('\n');
        }

        String source = result.toString().trim();

        return source;
    }
    /**
     * Copies closure statements into a block and rewrites a leading {@code super(...)} call when present.
     *
     * @param pre the closure containing statements to copy
     * @param body the destination block
     * @return true if a leading super call was rewritten; false otherwise
     */
    public static boolean copyStatementsWithSuperAdjustment(final ClosureExpression pre, final BlockStatement body) {
        Statement preCode = pre.getCode();
        boolean changed = false;
        if (preCode instanceof BlockStatement block) {
            List<Statement> statements = block.getStatements();
            for (int i = 0, n = statements.size(); i < n; i += 1) {
                Statement statement = statements.get(i);
                // adjust the first statement if it's a super call
                if (i == 0 && statement instanceof ExpressionStatement es) {
                    Expression preExp = es.getExpression();
                    if (preExp instanceof MethodCallExpression mce) {
                        String name = mce.getMethodAsString();
                        if ("super".equals(name)) {
                            es.setExpression(new ConstructorCallExpression(ClassNode.SUPER, mce.getArguments()));
                            changed = true;
                        }
                    }
                }
                body.addStatement(statement);
            }
        }
        return changed;
    }

    private static boolean hasClosureMember(final AnnotationNode annotation) {
        Map<String, Expression> members = annotation.getMembers();
        for (Map.Entry<String, Expression> member : members.entrySet())  {
            if (member.getValue() instanceof ClosureExpression) return true;

            if (member.getValue() instanceof ClassExpression classExpression)  {
                Class<?> typeClass = classExpression.getType().isResolved() ? classExpression.getType().redirect().getTypeClass() : null;
                if (typeClass != null && GeneratedClosure.class.isAssignableFrom(typeClass)) return true;
            }
        }
        return false;
    }
    /**
     * Checks whether a class declares a method with the specified name and arity.
     *
     * @param cNode the class node
     * @param name the property name
     * @param argsCount the expected argument count
     * @return true if a matching declared method exists; false otherwise
     */
    public static boolean hasDeclaredMethod(final ClassNode cNode, final String name, final int argsCount) {
        List<MethodNode> methods = cNode.getDeclaredMethods(name);
        for (MethodNode method : methods) {
            Parameter[] params = method.getParameters();
            if (params != null && params.length == argsCount) {
                return true;
            }
        }
        return false;
    }
    /**
     * Finds the first declared method with the specified name and arity.
     *
     * @param cNode the class node
     * @param name the property name
     * @param argsCount the expected argument count
     * @return the matching method node, or {@code null} if not found
     */
    public static MethodNode findDeclaredMethod(final ClassNode cNode, final String name, final int argsCount) {
        // TODO ignore bridge methods?
        List<MethodNode> methods = cNode.getDeclaredMethods(name);
        for (MethodNode method : methods) {
            Parameter[] params = method.getParameters();
            if (params != null && params.length == argsCount) {
                return method;
            }
        }
        return null;
    }
    /**
     * Checks whether two classes belong to the same package.
     *
     * @param first the first class/type
     * @param second the second class/type
     * @return true if both classes are in the same package; false otherwise
     */
    public static boolean inSamePackage(final ClassNode first, final ClassNode second) {
        return Objects.equals(first.getPackageName(), second.getPackageName());
    }
    /**
     * Checks whether two classes belong to the same package.
     *
     * @param first the first class/type
     * @param second the second class/type
     * @return true if both classes are in the same package; false otherwise
     */
    public static boolean inSamePackage(final Class<?>  first, final Class<?>  second) {
        Package firstPackage = first.getPackage(), secondPackage = second.getPackage();
        return (firstPackage == secondPackage || (firstPackage != null && secondPackage != null
                                                  && firstPackage.getName().equals(secondPackage.getName())));
    }
    /**
     * Checks whether modifier bits indicate package-private visibility.
     *
     * @param modifiers the modifier bit mask
     * @return true if no public/protected/private visibility flag is set; false otherwise
     */
    public static boolean isDefaultVisibility(final int modifiers) {
        return (modifiers & (Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED)) == 0;
    }
    /**
     * Checks whether a type equals or implements an interface type.
     *
     * @param type the target type
     * @param interfaceType the interface to test against
     * @return true if the type equals or implements the interface type; false otherwise
     */
    public static boolean isOrImplements(final ClassNode type, final ClassNode interfaceType) {
        return type.equals(interfaceType) || type.implementsInterface(interfaceType);
    }

    /**
     * @deprecated use MethodNodeUtils#methodDescriptorWithoutReturnType(MethodNode) instead
     */
    @Deprecated
    public static String makeDescriptorWithoutReturnType(final MethodNode mn) {
        StringBuilder sb = new StringBuilder();
        sb.append(mn.getName()).append(':');
        for (Parameter p : mn.getParameters()) {
            sb.append(p.getType()).append(',');
        }
        return sb.toString();
    }
    /**
     * Determines whether execution may continue past the supplied statement.
     *
     * @param statement the statement to analyze
     * @return true if execution may continue after the statement; false otherwise
     */
    public static boolean maybeFallsThrough(final Statement statement) {
        if (statement.isEmpty()) return true;

        if (statement instanceof ReturnStatement) {
            return false;
        } else if (statement instanceof ThrowStatement) {
            return false;
        } else if (statement instanceof BlockStatement blockStmt) {
            List<Statement> list = blockStmt.getStatements();
            final int last = list.size() - 1;
            if (!maybeFallsThrough(list.get(last))) return false;
            for (int i = 0; i < last; i += 1)
                if (!maybeFallsThrough(list.get(i))) return false;
        } else if (statement instanceof IfStatement ifStmt) {
            return maybeFallsThrough(ifStmt.getElseBlock())
                || maybeFallsThrough(ifStmt.getIfBlock());
        } else if (statement instanceof LoopingStatement loopingStmt) {
            return maybeFallsThroughLoop(loopingStmt);
        } else if (statement instanceof SwitchStatement switchStmt) {
            return maybeFallsThroughSwitch(switchStmt);
        } else if (statement instanceof TryCatchStatement tryCatchStmt) {
            if (!maybeFallsThrough(tryCatchStmt.getFinallyStatement())) return false;
            for (CatchStatement cs : tryCatchStmt.getCatchStatements())
                if (maybeFallsThrough(cs.getCode())) return true;
            return maybeFallsThrough(tryCatchStmt.getTryStatement());
        } else if (statement instanceof SynchronizedStatement syncStmt) {
            return maybeFallsThrough(syncStmt.getCode());
        }

        return true;
    }

    /**
     * Returns {@code true} if the given statement is effectively empty — i.e. it is {@code null},
     * an {@link EmptyStatement}, or a {@link BlockStatement} whose every nested statement is itself
     * empty (recursively).  This is subtly different from {@link Statement#isEmpty()}, which only
     * checks whether a {@link BlockStatement} has zero entries, not whether those entries are all
     * empty.
     */
    @Internal
    public static boolean isEmptyStatement(final Statement statement) {
        if (statement == null || statement.isEmpty()) return true;
        if (statement instanceof BlockStatement blockStatement) {
            for (Statement subStatement : blockStatement.getStatements()) {
                if (!isEmptyStatement(subStatement)) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the compile-time constant boolean value of a loop condition expression.
     * Handles nested {@link BooleanExpression} / {@link NotExpression} wrappers and
     * an absent (empty) condition, which is always {@code true} (e.g. {@code for(;;)}).
     * Returns {@link ConditionValue#UNKNOWN} when the value cannot be determined statically.
     */
    @Internal
    public static ConditionValue constantBooleanValue(Expression expression) {
        if (expression instanceof EmptyExpression) {
            return ConditionValue.ALWAYS_TRUE; // empty for-loop condition is always true (e.g. for(;;))
        }
        boolean reverse = false;
        while (expression instanceof BooleanExpression boolExpr) {
            if (boolExpr instanceof NotExpression) reverse = !reverse;
            expression = boolExpr.getExpression();
        }
        if (expression instanceof ConstantExpression ce && ce.getValue() instanceof Boolean) {
            return ((Boolean) ce.getValue()) != reverse ? ConditionValue.ALWAYS_TRUE : ConditionValue.ALWAYS_FALSE;
        }
        return ConditionValue.UNKNOWN;
    }

    /**
     * Indicates whether switch-case code may fall through into the next case body.
     */
    @Internal
    public static boolean maybeFallsThroughToNextSwitchCase(final Statement statement, final SwitchStatement switchStatement) {
        Objects.requireNonNull(switchStatement);
        return analyzeStatementFlow(statement).mayContinue;
    }

    private static boolean maybeFallsThroughSwitch(final SwitchStatement statement) {
        return analyzeStatementFlow(statement).mayContinue;
    }

    private static boolean maybeFallsThroughLoop(final LoopingStatement statement) {
        return analyzeLoopFlow(statement).mayContinue;
    }

    /**
     * Indicates whether execution of a loop body may reach the loop's condition/update step.
     */
    @Internal
    public static boolean mayReachLoopCondition(final LoopingStatement statement) {
        StatementFlow flow = analyzeStatementFlow(statement.getLoopBlock());
        Set<String> loopLabels = labelSet(((Statement) statement).getStatementLabels());
        return mayReachLoopCondition(flow, loopLabels);
    }

    private static boolean mayReachLoopCondition(final StatementFlow flow, final Set<String> loopLabels) {
        return flow.mayContinue || flow.mayContinueUnlabeled || flow.continuesToAny(loopLabels);
    }

    private static boolean mayBreakTo(final StatementFlow flow, final Set<String> labels) {
        return flow.mayBreakUnlabeled || flow.breaksToAny(labels);
    }

    private static StatementFlow analyzeStatementFlow(final Statement statement) {
        if (statement.isEmpty()) return StatementFlow.FALL_THROUGH;

        if (statement instanceof BreakStatement breakStatement) {
            return StatementFlow.breakFlow(breakStatement.getLabel());
        } else if (statement instanceof ContinueStatement continueStatement) {
            return StatementFlow.continueFlow(continueStatement.getLabel());
        } else if (statement instanceof ReturnStatement || statement instanceof ThrowStatement) {
            return StatementFlow.ABRUPT;
        } else if (statement instanceof BlockStatement block) {
            StatementFlow flow = StatementFlow.FALL_THROUGH;
            for (Statement subStatement : block.getStatements()) {
                flow = flow.then(analyzeStatementFlow(subStatement));
                if (!flow.mayContinue) break;
            }
            return flow.consumeLabels(labelSet(block.getStatementLabels()));
        } else if (statement instanceof IfStatement ifStatement) {
            StatementFlow thenFlow = analyzeStatementFlow(ifStatement.getIfBlock());
            StatementFlow elseFlow = analyzeStatementFlow(ifStatement.getElseBlock());
            return thenFlow.or(elseFlow);
        } else if (statement instanceof TryCatchStatement tryCatch) {
            StatementFlow finallyFlow = analyzeStatementFlow(tryCatch.getFinallyStatement());
            StatementFlow bodyFlow = analyzeStatementFlow(tryCatch.getTryStatement());
            for (CatchStatement catchStatement : tryCatch.getCatchStatements()) {
                bodyFlow = bodyFlow.or(analyzeStatementFlow(catchStatement.getCode()));
            }
            return bodyFlow.thenFinally(finallyFlow);
        } else if (statement instanceof SynchronizedStatement synchronizedStatement) {
            return analyzeStatementFlow(synchronizedStatement.getCode());
        } else if (statement instanceof LoopingStatement loopingStatement) {
            return analyzeLoopFlow(loopingStatement);
        } else if (statement instanceof SwitchStatement switchStatement) {
            return analyzeNestedSwitchFlow(switchStatement);
        }

        return StatementFlow.of(maybeFallsThrough(statement), Set.of(), Set.of());
    }

    private static StatementFlow analyzeLoopFlow(final LoopingStatement statement) {
        StatementFlow flow = analyzeStatementFlow(statement.getLoopBlock());
        ConditionValue condition = loopCondition(statement);
        Set<String> loopLabels = labelSet(((Statement) statement).getStatementLabels());
        boolean mayBreakOutOfLoop = mayBreakTo(flow, loopLabels);
        boolean mayReachLoopCondition = mayReachLoopCondition(flow, loopLabels);
        boolean mayExitViaCondition = mayExitLoopViaCondition(statement, condition, mayReachLoopCondition);
        boolean mayRunLoopBlock = mayRunLoopBlock(statement, condition);
        Set<String> breakLabels = mayRunLoopBlock ? withoutLabels(flow.breakLabels, loopLabels) : Set.of();
        Set<String> continueLabels = mayRunLoopBlock ? withoutLabels(flow.continueLabels, loopLabels) : Set.of();
        return StatementFlow.of(mayBreakOutOfLoop || mayExitViaCondition, breakLabels, continueLabels);
    }

    private static boolean mayRunLoopBlock(final LoopingStatement statement, final ConditionValue condition) {
        return !(statement instanceof WhileStatement || isClassicForLoop(statement))
            || condition != ConditionValue.ALWAYS_FALSE;
    }

    private static boolean mayExitLoopViaCondition(final LoopingStatement statement, final ConditionValue condition, final boolean bodyMayContinue) {
        if (statement instanceof DoWhileStatement) {
            return bodyMayContinue && condition != ConditionValue.ALWAYS_TRUE;
        }
        if (statement instanceof ForStatement forStatement && !isClassicForLoop(forStatement)) {
            return true;
        }
        return condition != ConditionValue.ALWAYS_TRUE;
    }

    private static boolean isClassicForLoop(final LoopingStatement statement) {
        return statement instanceof ForStatement
            && ((ForStatement) statement).getCollectionExpression() instanceof ClosureListExpression;
    }

    private static ConditionValue loopCondition(final LoopingStatement statement) {
        if (statement instanceof WhileStatement whileStatement) {
            return constantBooleanValue(whileStatement.getBooleanExpression());
        }
        if (statement instanceof DoWhileStatement doWhileStatement) {
            return constantBooleanValue(doWhileStatement.getBooleanExpression());
        }
        if (statement instanceof ForStatement forStatement
                && forStatement.getCollectionExpression() instanceof ClosureListExpression closureListExpression) {
            List<Expression> expressions = closureListExpression.getExpressions();
            if (!expressions.isEmpty()) {
                return constantBooleanValue(expressions.get((expressions.size() - 1) / 2));
            }
        }
        return ConditionValue.UNKNOWN;
    }

    private static Set<String> labelSet(final List<String> labels) {
        if (labels == null || labels.isEmpty()) return Set.of();
        return new LinkedHashSet<>(labels);
    }

    private static StatementFlow analyzeNestedSwitchFlow(final SwitchStatement statement) {
        Set<String> switchLabels = labelSet(statement.getStatementLabels());
        StatementFlow nextFlow = flowAfterSwitch(statement.getDefaultStatement(), switchLabels);
        StatementFlow switchFlow = nextFlow;

        List<CaseStatement> caseStatements = statement.getCaseStatements();
        for (int i = caseStatements.size() - 1; i >= 0; i -= 1) {
            StatementFlow flow = analyzeStatementFlow(caseStatements.get(i).getCode());
            boolean mayBreakSwitch = mayBreakTo(flow, switchLabels);
            Set<String> breakLabels = withoutLabels(flow.breakLabels, switchLabels);
            nextFlow = StatementFlow.of(
                mayBreakSwitch || (flow.mayContinue && nextFlow.mayContinue),
                union(breakLabels, flow.mayContinue ? nextFlow.breakLabels : Set.of()),
                union(flow.continueLabels, flow.mayContinue ? nextFlow.continueLabels : Set.of())
            );
            switchFlow = nextFlow.or(switchFlow);
        }

        return switchFlow;
    }

    private static StatementFlow flowAfterSwitch(final Statement statement, final Set<String> switchLabels) {
        StatementFlow flow = analyzeStatementFlow(statement);
        return StatementFlow.of(
            mayBreakTo(flow, switchLabels) || flow.mayContinue,
            withoutLabels(flow.breakLabels, switchLabels),
            flow.continueLabels
        );
    }

    private static Set<String> union(final Set<String> left, final Set<String> right) {
        if (left.isEmpty()) return right;
        if (right.isEmpty()) return left;

        Set<String> labels = new LinkedHashSet<>(left);
        labels.addAll(right);
        return labels;
    }

    private static Set<String> withoutLabels(final Set<String> labels, final Set<String> removedLabels) {
        if (labels.isEmpty() || removedLabels.isEmpty()) return labels;

        Set<String> remainingLabels = new LinkedHashSet<>(labels);
        remainingLabels.removeAll(removedLabels);
        return remainingLabels;
    }

    /**
     * Represents the flow of control through a switch case or loop body, tracking whether execution may continue past the end of the block
     * @param mayContinue whether execution may continue past the end of the block (i.e. not all paths return, throw, or break out of the block)
     * @param mayBreakUnlabeled whether the block may be exited via an unlabeled break statement
     * @param breakLabels the set of labels of break statements that may exit the block (if any)
     */
    private record StatementFlow(boolean mayContinue, boolean mayBreakUnlabeled, Set<String> breakLabels,
                                 boolean mayContinueUnlabeled, Set<String> continueLabels) {
        /**
         * Code always terminates abruptly via {@code return}, {@code throw}, or {@code continue}.
         */
        static final StatementFlow ABRUPT = new StatementFlow(false, false, Set.of(), false, Set.of());
        /**
         * Code may continue after this statement and does not leave unresolved breaks behind.
         */
        static final StatementFlow FALL_THROUGH = new StatementFlow(true, false, Set.of(), false, Set.of());

        static StatementFlow breakFlow(final String label) {
            if (label == null) return flow(false, true, Set.of(), false, Set.of());
            return flow(false, false, Set.of(label), false, Set.of());
        }

        static StatementFlow continueFlow(final String label) {
            if (label == null) return flow(false, false, Set.of(), true, Set.of());
            return flow(false, false, Set.of(), false, Set.of(label));
        }

        static StatementFlow of(final boolean mayContinue, final Set<String> breakLabels, final Set<String> continueLabels) {
            return flow(mayContinue, false, breakLabels, false, continueLabels);
        }

        private static StatementFlow flow(final boolean mayContinue, final boolean mayBreakUnlabeled, final Set<String> breakLabels,
                                          final boolean mayContinueUnlabeled, final Set<String> continueLabels) {
            if (!mayContinue && !mayBreakUnlabeled && breakLabels.isEmpty() && !mayContinueUnlabeled && continueLabels.isEmpty()) {
                return ABRUPT;
            }
            if (mayContinue && !mayBreakUnlabeled && breakLabels.isEmpty() && !mayContinueUnlabeled && continueLabels.isEmpty()) {
                return FALL_THROUGH;
            }
            return new StatementFlow(mayContinue, mayBreakUnlabeled, breakLabels, mayContinueUnlabeled, continueLabels);
        }

        StatementFlow then(final StatementFlow next) {
            if (!mayContinue) return this;
            if (!mayBreakUnlabeled && breakLabels.isEmpty() && !mayContinueUnlabeled && continueLabels.isEmpty()) return next;
            return flow(
                next.mayContinue,
                mayBreakUnlabeled || next.mayBreakUnlabeled,
                union(breakLabels, next.breakLabels),
                mayContinueUnlabeled || next.mayContinueUnlabeled,
                union(continueLabels, next.continueLabels)
            );
        }

        StatementFlow or(final StatementFlow other) {
            return flow(
                mayContinue || other.mayContinue,
                mayBreakUnlabeled || other.mayBreakUnlabeled,
                union(breakLabels, other.breakLabels),
                mayContinueUnlabeled || other.mayContinueUnlabeled,
                union(continueLabels, other.continueLabels)
            );
        }

        StatementFlow thenFinally(final StatementFlow finallyFlow) {
            return flow(
                finallyFlow.mayContinue && mayContinue,
                finallyFlow.mayBreakUnlabeled || (finallyFlow.mayContinue && mayBreakUnlabeled),
                union(finallyFlow.breakLabels, finallyFlow.mayContinue ? breakLabels : Set.of()),
                finallyFlow.mayContinueUnlabeled || (finallyFlow.mayContinue && mayContinueUnlabeled),
                union(finallyFlow.continueLabels, finallyFlow.mayContinue ? continueLabels : Set.of())
            );
        }

        StatementFlow consumeLabels(final Set<String> labels) {
            if (labels.isEmpty() || breakLabels.isEmpty()) return this;

            Set<String> remainingBreakLabels = withoutLabels(breakLabels, labels);
            boolean consumedBreak = remainingBreakLabels.size() != breakLabels.size();
            return flow(mayContinue || consumedBreak, mayBreakUnlabeled, remainingBreakLabels, mayContinueUnlabeled, continueLabels);
        }

        boolean breaksToAny(final Set<String> labels) {
            if (labels.isEmpty() || breakLabels.isEmpty()) return false;

            for (String label : labels) {
                if (breakLabels.contains(label)) return true;
            }
            return false;
        }

        boolean continuesToAny(final Set<String> labels) {
            if (labels.isEmpty() || continueLabels.isEmpty()) return false;

            for (String label : labels) {
                if (continueLabels.contains(label)) return true;
            }
            return false;
        }
    }

    /**
     * Represents the compile-time constant value of a boolean loop-condition or branch expression.
     * Used by flow analysis to avoid emitting unreachable bytecode.
     */
    @Internal
    public enum ConditionValue {
        /** The boolean expression always evaluates to {@code false}. */
        ALWAYS_FALSE,
        /** The boolean expression always evaluates to {@code true}. */
        ALWAYS_TRUE,
        /** The value cannot be determined at compile time. */
        UNKNOWN
    }
}
