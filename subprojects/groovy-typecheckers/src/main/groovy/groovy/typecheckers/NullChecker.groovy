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
package groovy.typecheckers

import org.apache.groovy.lang.annotation.Incubating
import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType
import static org.codehaus.groovy.syntax.Types.isAssignment

/**
 * A compile-time type checker that detects potential null dereferences and null-safety violations
 * in code annotated with {@code @Nullable}, {@code @NonNull}, and {@code @MonotonicNonNull} annotations.
 * <p>
 * By default, this checker performs annotation-based null checking only. For additional flow-sensitive
 * analysis that tracks nullability through assignments and control flow (even in unannotated code),
 * enable the {@code strict} option:
 * <pre>
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.NullChecker(strict: true)')}
 * </pre>
 * <p>
 * Supported annotations are recognized by simple name from any package:
 * <ul>
 *     <li>Nullable: {@code @Nullable}, {@code @CheckForNull}, {@code @MonotonicNonNull}</li>
 *     <li>Non-null: {@code @NonNull}, {@code @NotNull}, {@code @Nonnull}</li>
 * </ul>
 * <p>
 * Detected errors include:
 * <ul>
 *     <li>Assigning {@code null} to a {@code @NonNull} variable</li>
 *     <li>Passing {@code null} or a {@code @Nullable} value to a {@code @NonNull} parameter</li>
 *     <li>Returning {@code null} or a {@code @Nullable} value from a {@code @NonNull} method</li>
 *     <li>Dereferencing a {@code @Nullable} variable without a null check or safe navigation ({@code ?.})</li>
 *     <li>Dereferencing the result of a {@code @Nullable}-returning method without a null check</li>
 *     <li>Re-assigning {@code null} to a {@code @MonotonicNonNull} field after initialization</li>
 *     <li>Dereferencing a variable known to be null through flow analysis ({@code strict} mode only)</li>
 * </ul>
 * <p>
 * The checker recognizes a range of null-guard patterns:
 * <ul>
 *     <li>Null comparisons: {@code if (x != null)}, {@code if (x == null)}</li>
 *     <li>Early exit patterns: {@code if (x == null) return/throw}</li>
 *     <li>Safe navigation: {@code ?.}</li>
 *     <li>Groovy-truth guards: {@code if (x)}, {@code if (!x) return}</li>
 *     <li>Boolean conjunctions and disjunctions with short-circuit semantics:
 *         {@code if (x != null && x.length() > 0)}, {@code if (x == null || x.isEmpty()) return}</li>
 *     <li>Type checks: {@code if (x instanceof Foo)}, {@code if (x !instanceof Foo) return}</li>
 *     <li>Utility methods: {@code Objects.nonNull(x)}, {@code Objects.isNull(x)}</li>
 *     <li>Assert statements: {@code assert x}, {@code assert x != null}</li>
 *     <li>Guard conditions of while loops and ternary expressions</li>
 * </ul>
 *
 * <pre>
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.NullChecker')}
 * void process(@Nullable String input) {
 *     // input.length()     // error: potential null dereference
 *     input?.length()       // ok: safe navigation
 *     if (input != null) {
 *         input.length()    // ok: null guard
 *     }
 * }
 * </pre>
 *
 * Over time, the idea would be to support more cases as per:
 * https://checkerframework.org/manual/#nullness-checker
 *
 * @since 6.0.0
 */
@Incubating
class NullChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    private static final Set<String> NULLABLE_ANNOS = Set.of('Nullable', 'CheckForNull', 'MonotonicNonNull')
    private static final Set<String> NONNULL_ANNOS = Set.of('NonNull', 'NotNull', 'Nonnull')
    private static final Set<String> MONOTONIC_ANNOS = Set.of('MonotonicNonNull', 'Lazy')
    private static final Set<String> NULLCHECK_ANNOS = Set.of('NullCheck', 'ParametersAreNonnullByDefault', 'ParametersAreNonNullByDefault')
    private static final Set<String> NONNULL_BY_DEFAULT_ANNOS = Set.of('NonNullByDefault', 'NonnullByDefault', 'NullMarked')
    private static final Set<String> NULL_UNMARKED_ANNOS = Set.of('NullUnmarked')

    /**
     * Registers null-safety checks for each visited method body.
     */
    @Override
    Object run() {
        boolean strict = options?.strict ?: false
        afterVisitMethod { MethodNode method ->
            method.code?.visit(makeVisitor(strict, method))
        }
    }

    private CheckingVisitor makeVisitor(boolean flowSensitive, MethodNode method) {
        boolean classNonNullByDefault = method.declaringClass != null && hasNonNullByDefaultAnno(method.declaringClass)
        boolean methodNonNull = method.returnType != VOID_TYPE && (hasNonNullAnno(method) || (classNonNullByDefault && !hasNullableAnno(method)))
        if (methodNonNull) {
            def stash = method.getNodeMetaData(StaticTypesMarker.INFERRED_NON_NULL_RETURN_VIOLATIONS)
            if (stash instanceof List) {
                stash.each { node ->
                    addStaticTypeError("Cannot return null from @NonNull method '${method.name}'", node)
                }
            }
        }
        def initialNullable = method.parameters.findAll { hasNullableAnno(it) } as Set<Variable>

        new CheckingVisitor() {
            private final Set<Variable> nullableVars = new HashSet<>(initialNullable)
            private final Set<Variable> monotonicInitialized = new HashSet<>()
            private final Set<Variable> guardedVars = new HashSet<>()

            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                super.visitDeclarationExpression(decl)
                def ve = decl.variableExpression
                if (ve == null) return
                if (decl.rightExpression instanceof ConstantExpression) {
                    localConstVars.put(ve, decl.rightExpression)
                }
                if (hasNonNullAnno(ve) && isNullExpr(decl.rightExpression)) {
                    addStaticTypeError("Cannot assign null to @NonNull variable '${ve.name}'", decl)
                }
                // Uninitialized non-primitive declaration (e.g. "String x") is implicitly null
                boolean implicitlyNull = decl.rightExpression instanceof EmptyExpression && !isPrimitiveType(ve.type)
                if (hasNullableAnno(ve) || isNullExpr(decl.rightExpression) || (flowSensitive && (implicitlyNull || canBeNull(decl.rightExpression) || isKnownNullable(decl.rightExpression)))) {
                    nullableVars.add(ve)
                }
                if (flowSensitive) {
                    trackNullableReturn(ve, decl.rightExpression)
                }
            }

            @Override
            void visitBinaryExpression(BinaryExpression expression) {
                int op = expression.operation.type
                if (op == Types.LOGICAL_AND || op == Types.LOGICAL_OR) {
                    // apply short-circuit guard semantics wherever the expression appears
                    visitCondition(expression)
                    return
                }
                super.visitBinaryExpression(expression)
                if (isAssignment(expression.operation.type) && expression.leftExpression instanceof VariableExpression) {
                    def target = findTargetVariable(expression.leftExpression)
                    boolean fieldNonNull = target instanceof AnnotatedNode && hasNonNullAnno(target)
                    if (!fieldNonNull && target instanceof FieldNode && !hasNullableAnno(target)) {
                        fieldNonNull = target.declaringClass != null && hasNonNullByDefaultAnno(target.declaringClass)
                    }
                    if (fieldNonNull && isNullExpr(expression.rightExpression)) {
                        addStaticTypeError("Cannot assign null to @NonNull variable '${expression.leftExpression.name}'", expression)
                    }
                    // @MonotonicNonNull: once initialized with non-null, cannot assign null again
                    if (target instanceof AnnotatedNode && hasMonotonicAnno(target)) {
                        if (!isNullExpr(expression.rightExpression)) {
                            monotonicInitialized.add(target)
                        } else if (monotonicInitialized.contains(target)) {
                            addStaticTypeError("Cannot assign null to @MonotonicNonNull variable '${expression.leftExpression.name}' after non-null assignment", expression)
                        }
                    }
                    if (isNullExpr(expression.rightExpression)) {
                        nullableVars.add(target)
                        guardedVars.remove(target)
                    } else if (flowSensitive && (canBeNull(expression.rightExpression) || isKnownNullable(expression.rightExpression))) {
                        nullableVars.add(target)
                        guardedVars.remove(target)
                    } else {
                        nullableVars.remove(target)
                        if (flowSensitive) {
                            trackNullableReturn(target, expression.rightExpression)
                        }
                    }
                }
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                if (!call.safe && !call.implicitThis) {
                    checkDereference(call.objectExpression, call)
                }
                checkMethodArguments(call)
            }

            @Override
            void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                super.visitStaticMethodCallExpression(call)
                checkMethodArguments(call)
            }

            @Override
            void visitPropertyExpression(PropertyExpression expression) {
                super.visitPropertyExpression(expression)
                if (!expression.safe) {
                    checkDereference(expression.objectExpression, expression)
                }
            }

            @Override
            void visitReturnStatement(ReturnStatement statement) {
                super.visitReturnStatement(statement)
                if (methodNonNull) {
                    if (isNullExpr(statement.expression)) {
                        addStaticTypeError("Cannot return null from @NonNull method '${method.name}'", statement)
                    } else if (isKnownNullable(statement.expression)) {
                        addStaticTypeError("Cannot return @Nullable value from @NonNull method '${method.name}'", statement)
                    }
                }
            }

            @Override
            void visitIfElse(IfStatement ifElse) {
                def facts = visitCondition(ifElse.booleanExpression.expression)
                withGuards(facts.whenTrue) { ifElse.ifBlock.visit(this) }
                withGuards(facts.whenFalse) { ifElse.elseBlock.visit(this) }
                // Early exit: if (x == null) return/throw → x is non-null after (and vice versa)
                if (isEarlyExit(ifElse.ifBlock)) {
                    applyFacts(facts.whenFalse)
                }
                if (isEarlyExit(ifElse.elseBlock)) {
                    applyFacts(facts.whenTrue)
                }
            }

            @Override
            void visitWhileLoop(WhileStatement loop) {
                def facts = visitCondition(loop.booleanExpression.expression)
                withGuards(facts.whenTrue) { loop.loopBlock.visit(this) }
            }

            @Override
            void visitTernaryExpression(TernaryExpression expression) {
                def condition = expression.booleanExpression.expression
                def facts = visitCondition(condition)
                // for elvis, the true expression is the (already visited) condition
                if (!condition.is(expression.trueExpression) && !expression.booleanExpression.is(expression.trueExpression)) {
                    withGuards(facts.whenTrue) { expression.trueExpression.visit(this) }
                }
                withGuards(facts.whenFalse) { expression.falseExpression.visit(this) }
            }

            @Override
            void visitAssertStatement(AssertStatement statement) {
                def facts = visitCondition(statement.booleanExpression.expression)
                statement.messageExpression?.visit(this)
                // assert x/assert x != null → x is non-null for the rest of the block
                applyFacts(facts.whenTrue)
            }

            //------------------------------------------------------------------

            /**
             * Visits a condition applying short-circuit guard semantics: in
             * {@code x != null && x.foo()} the right operand is protected by the
             * left operand's null check (and similarly after {@code x == null ||}).
             * Returns the sets of variables known to be non-null when the condition
             * evaluates true and when it evaluates false.
             */
            private GuardFacts visitCondition(Expression condition) {
                def facts = new GuardFacts()
                if (condition instanceof NotExpression) {
                    def inner = visitCondition(condition.expression)
                    facts.whenTrue = inner.whenFalse
                    facts.whenFalse = inner.whenTrue
                } else if (condition instanceof BooleanExpression) {
                    facts = visitCondition(condition.expression)
                } else if (condition instanceof BinaryExpression && condition.operation.type == Types.LOGICAL_AND) {
                    def left = visitCondition(condition.leftExpression)
                    def right = withGuards(left.whenTrue) { visitCondition(condition.rightExpression) }
                    facts.whenTrue = left.whenTrue + right.whenTrue
                    facts.whenFalse = left.whenFalse.intersect(right.whenFalse)
                } else if (condition instanceof BinaryExpression && condition.operation.type == Types.LOGICAL_OR) {
                    def left = visitCondition(condition.leftExpression)
                    def right = withGuards(left.whenFalse) { visitCondition(condition.rightExpression) }
                    facts.whenTrue = left.whenTrue.intersect(right.whenTrue)
                    facts.whenFalse = left.whenFalse + right.whenFalse
                } else {
                    condition.visit(this)
                    facts = analyzeLeafCondition(condition)
                }
                facts
            }

            /**
             * Analyzes a leaf condition (null comparison, instanceof check, Groovy-truth
             * variable, or {@code Objects.nonNull/isNull} call) into guard facts.
             */
            private GuardFacts analyzeLeafCondition(Expression condition) {
                def facts = new GuardFacts()
                if (condition instanceof BinaryExpression) {
                    int op = condition.operation.type
                    if (op == Types.KEYWORD_INSTANCEOF) {
                        def var = guardableVariable(condition.leftExpression)
                        if (var != null) facts.whenTrue.add(var)
                    } else if (op == Types.COMPARE_NOT_INSTANCEOF) {
                        def var = guardableVariable(condition.leftExpression)
                        if (var != null) facts.whenFalse.add(var)
                    } else {
                        boolean isNotEqual = (op == Types.COMPARE_NOT_EQUAL || op == Types.COMPARE_NOT_IDENTICAL)
                        boolean isEqual = (op == Types.COMPARE_EQUAL || op == Types.COMPARE_IDENTICAL)
                        if (isNotEqual || isEqual) {
                            Variable var = null
                            if (isNullExpr(condition.rightExpression)) {
                                var = guardableVariable(condition.leftExpression)
                            } else if (isNullExpr(condition.leftExpression)) {
                                var = guardableVariable(condition.rightExpression)
                            }
                            if (var != null) (isNotEqual ? facts.whenTrue : facts.whenFalse).add(var)
                        }
                    }
                } else if (condition instanceof VariableExpression) {
                    // Groovy truth: a truthy reference is non-null
                    def var = guardableVariable(condition)
                    if (var != null) facts.whenTrue.add(var)
                } else if (condition instanceof MethodCallExpression || condition instanceof StaticMethodCallExpression) {
                    def target = condition.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                    if (target instanceof MethodNode && target.declaringClass?.name == 'java.util.Objects'
                            && target.name in ['nonNull', 'isNull']) {
                        def args = condition.arguments
                        if (args instanceof TupleExpression && args.expressions.size() == 1) {
                            def var = guardableVariable(args.getExpression(0))
                            if (var != null) (target.name == 'nonNull' ? facts.whenTrue : facts.whenFalse).add(var)
                        }
                    }
                }
                facts
            }

            private Variable guardableVariable(Expression expr) {
                if (expr instanceof VariableExpression && !expr.isThisExpression() && !expr.isSuperExpression()) {
                    return findTargetVariable(expr)
                }
                null
            }

            private <T> T withGuards(Set<Variable> vars, Closure<T> body) {
                if (!vars) {
                    return body()
                }
                def saved = new HashSet<>(guardedVars)
                guardedVars.addAll(vars)
                def result = body()
                guardedVars.clear()
                guardedVars.addAll(saved)
                result
            }

            private void applyFacts(Set<Variable> vars) {
                for (var in vars) {
                    nullableVars.remove(var)
                    guardedVars.add(var)
                }
            }

            private void checkDereference(Expression receiver, Expression context) {
                if (isNullExpr(receiver)) {
                    addStaticTypeError('Cannot dereference null', context)
                    return
                }
                if (receiver instanceof VariableExpression) {
                    if (receiver.isThisExpression() || receiver.isSuperExpression()) return
                    def target = findTargetVariable(receiver)
                    boolean isMonotonicAndInitialized = target instanceof AnnotatedNode && hasMonotonicAnno(target) && monotonicInitialized.contains(target)
                    if (target instanceof AnnotatedNode && hasNullableAnno(target) && !guardedVars.contains(target) && !isMonotonicAndInitialized) {
                        addStaticTypeError("Potential null dereference: '${receiver.name}' is @Nullable", context)
                    } else if (flowSensitive && nullableVars.contains(target) && !guardedVars.contains(target)) {
                        addStaticTypeError("Potential null dereference: '${receiver.name}' may be null", context)
                    }
                } else if (receiver instanceof MethodCallExpression || receiver instanceof StaticMethodCallExpression) {
                    def targetMethod = receiver.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                    if (targetMethod instanceof MethodNode && hasNullableAnno(targetMethod)) {
                        addStaticTypeError("Potential null dereference: '${targetMethod.name}()' may return null", context)
                    }
                }
            }

            private void checkMethodArguments(call) {
                def target = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                if (!(target instanceof MethodNode)) return
                def args = call.arguments
                if (!(args instanceof TupleExpression)) return
                def params = target.parameters
                // @NullCheck/@ParametersAreNonnullByDefault/@NonNullByDefault on method or class makes non-primitive params effectively @NonNull
                def declaringClass = target.declaringClass
                boolean nullChecked = hasNullCheckAnno(target) || hasNonNullByDefaultAnno(target) ||
                    (declaringClass != null && (hasNullCheckAnno(declaringClass) || hasNonNullByDefaultAnno(declaringClass)))
                int limit = Math.min(args.expressions.size(), params.length)
                for (int i = 0; i < limit; i++) {
                    def arg = args.getExpression(i)
                    boolean paramIsNonNull = hasNonNullAnno(params[i]) || (nullChecked && !isPrimitiveType(params[i].type) && !hasNullableAnno(params[i]))
                    if (paramIsNonNull) {
                        if (isNullExpr(arg)) {
                            addStaticTypeError("Cannot pass null to @NonNull parameter '${params[i].name}' of '${target.name}'", call)
                        } else if (isKnownNullable(arg)) {
                            addStaticTypeError("Cannot pass @Nullable value to @NonNull parameter '${params[i].name}' of '${target.name}'", call)
                        }
                    }
                }
            }

            private boolean isKnownNullable(Expression expr) {
                if (expr instanceof VariableExpression) {
                    def target = findTargetVariable(expr)
                    if (target instanceof AnnotatedNode && hasNullableAnno(target)) return true
                    if (nullableVars.contains(target)) return true
                }
                false
            }

            private void trackNullableReturn(Variable variable, Expression rhs) {
                if (rhs instanceof MethodCallExpression || rhs instanceof StaticMethodCallExpression) {
                    def target = rhs.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                    if (target instanceof MethodNode && hasNullableAnno(target)) {
                        nullableVars.add(variable)
                    }
                }
            }

        }
    }

    //--------------------------------------------------------------------------

    private static boolean hasNullableAnno(AnnotatedNode node) {
        hasAnno(node, NULLABLE_ANNOS)
    }

    private static boolean hasNonNullAnno(AnnotatedNode node) {
        if (node.getNodeMetaData(StaticTypesMarker.INFERRED_NON_NULL) == Boolean.TRUE) return true
        hasAnno(node, NONNULL_ANNOS)
    }

    private static boolean hasMonotonicAnno(AnnotatedNode node) {
        hasAnno(node, MONOTONIC_ANNOS)
    }

    /**
     * Checks the node's declaration annotations and, for methods, parameters and fields,
     * also the type-use annotations of the declared type. Type-use only annotations
     * (e.g. JSpecify's) appear on the type rather than the declaration, in particular
     * for classes read from bytecode or via reflection.
     */
    private static boolean hasAnno(AnnotatedNode node, Set<String> annoNames) {
        if (node.annotations?.any { it.classNode?.nameWithoutPackage in annoNames }) return true
        typeOf(node)?.typeAnnotations?.any { it.classNode?.nameWithoutPackage in annoNames } ?: false
    }

    private static ClassNode typeOf(AnnotatedNode node) {
        switch (node) {
            case MethodNode: return ((MethodNode) node).returnType
            case Parameter: return ((Parameter) node).type
            case FieldNode: return ((FieldNode) node).type
            default: return null
        }
    }

    private static boolean hasNullCheckAnno(AnnotatedNode node) {
        node.annotations?.any { it.classNode?.nameWithoutPackage in NULLCHECK_ANNOS } ?: false
    }

    private static boolean hasNonNullByDefaultAnno(AnnotatedNode node) {
        if (hasNullUnmarkedAnno(node)) return false
        node.annotations?.any { it.classNode?.nameWithoutPackage in NONNULL_BY_DEFAULT_ANNOS } ?: false
    }

    private static boolean hasNullUnmarkedAnno(AnnotatedNode node) {
        node.annotations?.any { it.classNode?.nameWithoutPackage in NULL_UNMARKED_ANNOS } ?: false
    }

    private static boolean isNullExpr(Expression expr) {
        if (expr instanceof ConstantExpression) return ((ConstantExpression) expr).isNullExpression()
        if (expr instanceof CastExpression) return isNullExpr(((CastExpression) expr).expression)
        false
    }

    private static boolean canBeNull(Expression expr) {
        if (isNullExpr(expr)) return true
        if (expr instanceof TernaryExpression) {
            return canBeNull(expr.trueExpression) || canBeNull(expr.falseExpression)
        }
        false
    }

    private static boolean isEarlyExit(Statement stmt) {
        if (stmt instanceof ReturnStatement || stmt instanceof ThrowStatement) return true
        if (stmt instanceof BlockStatement) {
            def stmts = stmt.statements
            return stmts && isEarlyExit(stmts.last())
        }
        false
    }

    /**
     * The variables known to be non-null when a condition evaluates true and when it evaluates false.
     */
    private static class GuardFacts {
        Set<Variable> whenTrue = new HashSet<>()
        Set<Variable> whenFalse = new HashSet<>()
    }
}
