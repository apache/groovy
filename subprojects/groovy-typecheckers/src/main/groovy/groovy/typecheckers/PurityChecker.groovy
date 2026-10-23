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

import org.apache.groovy.ast.tools.ImmutablePropertyUtils
import org.apache.groovy.lang.annotation.Incubating
import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.stc.ExtensionMethodNode
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

import java.util.regex.Matcher

import static org.codehaus.groovy.syntax.Types.isAssignment

/**
 * A compile-time checker that verifies {@code @Pure} methods have no side effects.
 * <p>
 * By default, strict purity is enforced: no field mutations, no I/O, no logging,
 * no non-deterministic calls. The {@code allows} option declares which effect
 * categories are tolerated:
 * <pre>
 * // Strict: no side effects at all
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.PurityChecker')}
 *
 * // Tolerate logging and metrics
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.PurityChecker(allows: "LOGGING|METRICS")')}
 * </pre>
 * <p>
 * Effect categories:
 * <ul>
 *   <li>{@code LOGGING} — calls to logging frameworks (SLF4J, JUL, etc.) and {@code println}</li>
 *   <li>{@code METRICS} — calls to metrics instruments (Micrometer, OpenTelemetry, etc.)</li>
 *   <li>{@code IO} — file, network, database, and console I/O</li>
 *   <li>{@code NONDETERMINISM} — time-dependent, random, and environment-dependent calls</li>
 * </ul>
 * <p>
 * Also recognises:
 * <ul>
 *   <li>{@code @SideEffectFree} (Checker Framework) — treated as {@code @Pure} with implicit NONDETERMINISM allowed</li>
 *   <li>{@code @Contract(pure = true)} (JetBrains) — treated as {@code @Pure}</li>
 *   <li>{@code @Memoized} — treated as effectively pure</li>
 * </ul>
 * <p>
 * {@code @Pure} may also be placed on a parameter or local variable holding a closure, lambda
 * or method reference. An argument for a {@code @Pure} parameter, or a value assigned to a
 * {@code @Pure} local, must be a closure or lambda whose body is pure and writes no captured
 * variable, a method reference to a {@code @Pure} method, or another {@code @Pure} variable.
 * Inside a {@code @Pure} method, invoking a {@code @Pure} parameter or local is pure, as are
 * the closure-taking GDK and stream methods listed in {@code CLOSURE_PURE_METHODS} when
 * their functional arguments are pure.
 *
 * @since 6.0.0
 * @see groovy.transform.Pure
 */
@Incubating
class PurityChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    private static final Set<String> PURE_ANNOS = Set.of('Pure')
    private static final Set<String> SIDE_EFFECT_FREE_ANNOS = Set.of('SideEffectFree')
    private static final Set<String> CONTRACT_ANNOS = Set.of('Contract')
    private static final Set<String> MEMOIZED_ANNOS = Set.of('Memoized')

    // Methods on mutable types known to be pure (no mutation, no effects, no closures)
    private static final Set<String> KNOWN_PURE_METHODS = Set.of(
            // Object fundamentals
            'toString', 'hashCode', 'equals', 'compareTo', 'getClass',
            // Collection/Map queries
            'size', 'length', 'isEmpty', 'contains', 'containsKey', 'containsValue',
            'get', 'getAt', 'getOrDefault', 'indexOf', 'lastIndexOf',
            'iterator', 'listIterator', 'spliterator', 'stream', 'parallelStream',
            'toArray', 'subList', 'keySet', 'values', 'entrySet',
            'first', 'last', 'head', 'tail', 'init',
            'asBoolean', 'is', 'isCase',
            // Type info
            'getMetaClass', 'respondsTo', 'hasProperty',
    )

    // GDK and java.util.stream methods that are pure whenever their functional arguments are pure.
    // Mutating forms such as sort and unique are deliberately absent (use toSorted and toUnique).
    private static final Set<String> CLOSURE_PURE_METHODS = Set.of(
            // GDK
            'collect', 'collectMany', 'collectEntries', 'collectNested', 'inject',
            'find', 'findAll', 'findResult', 'findResults', 'findIndexOf', 'findIndexValues',
            'any', 'every', 'count', 'countBy', 'sum', 'max', 'min', 'groupBy',
            'toSorted', 'toUnique', 'each', 'eachWithIndex', 'reverseEach', 'withIndex', 'indexed',
            'takeWhile', 'dropWhile',
            // java.util.stream
            'map', 'filter', 'flatMap', 'reduce', 'mapToInt', 'mapToLong', 'mapToDouble', 'mapToObj',
            'sorted', 'distinct', 'limit', 'skip', 'boxed', 'toList', 'anyMatch', 'allMatch', 'noneMatch',
            'findFirst', 'findAny', 'average',
    )

    // Node metadata key marking the declared variable of a @Pure local
    private static final String PURE_LOCAL = 'groovy.typecheckers.PurityChecker.PURE_LOCAL'

    // Known non-deterministic static methods (class.method)
    private static final Map<String, Set<String>> NONDETERMINISTIC_STATIC_METHODS = [
            'java.lang.System'        : Set.of('nanoTime', 'currentTimeMillis', 'getProperty', 'getenv'),
            'java.lang.Math'          : Set.of('random'),
            'java.util.UUID'          : Set.of('randomUUID'),
            'java.time.Instant'       : Set.of('now'),
            'java.time.LocalDateTime' : Set.of('now'),
            'java.time.LocalDate'     : Set.of('now'),
            'java.time.LocalTime'     : Set.of('now'),
            'java.time.ZonedDateTime' : Set.of('now'),
            'java.time.OffsetDateTime': Set.of('now'),
            'java.time.OffsetTime'    : Set.of('now'),
            'java.time.Year'          : Set.of('now'),
            'java.time.YearMonth'     : Set.of('now'),
            'java.time.MonthDay'      : Set.of('now'),
    ]

    // Non-deterministic no-arg constructors
    private static final Set<String> NONDETERMINISTIC_CONSTRUCTORS = Set.of(
            'java.util.Date',
            'java.util.Random',
    )

    // Instance method that is non-deterministic
    private static final Map<String, Set<String>> NONDETERMINISTIC_INSTANCE_METHODS = [
            'java.util.concurrent.ThreadLocalRandom': Set.of('current'),
    ]

    // Logging receiver type prefixes
    private static final List<String> LOGGING_TYPE_PREFIXES = [
            'org.slf4j.Logger',
            'java.util.logging.Logger',
            'org.apache.commons.logging.Log',
            'org.apache.log4j.Logger',
            'org.apache.logging.log4j.Logger',
            'java.lang.System.Logger',
    ]

    // Logging method names (on implicit this or any receiver)
    private static final Set<String> LOGGING_METHOD_NAMES = Set.of(
            'println', 'print', 'printf',
    )

    // Metrics receiver type prefixes
    private static final List<String> METRICS_TYPE_PREFIXES = [
            'io.micrometer.core.instrument',
            'io.opentelemetry.api.metrics',
            'com.codahale.metrics',
            'org.eclipse.microprofile.metrics',
    ]

    // I/O type prefixes
    private static final List<String> IO_TYPE_PREFIXES = [
            'java.io.',
            'java.nio.',
            'java.net.',
            'java.sql.',
            'javax.sql.',
            'groovy.io.',
    ]

    // I/O class names for constructor detection
    private static final List<String> IO_CONSTRUCTOR_PREFIXES = [
            'java.io.',
            'java.nio.',
            'java.net.',
            'java.sql.',
    ]

    /**
     * Registers purity checks for annotated methods encountered during type checking.
     */
    @Override
    Object run() {
        Set<String> baseAllows = parseAllows(options?.allows as String)

        afterVisitMethod { MethodNode mn ->
            // closures already checked as arguments for @Pure parameters or values of @Pure locals
            Set<ClosureExpression> checked = Collections.newSetFromMap(new IdentityHashMap<>())
            markPureLocals(mn)
            mn.code?.visit(makeArgumentVisitor(baseAllows, checked))

            Set<String> allows = baseAllows

            if (hasPureAnno(mn) || hasMemoizedAnno(mn) || hasContractPureAnno(mn)) {
                // strict purity (or whatever baseAllows says)
            } else if (hasSideEffectFreeAnno(mn)) {
                // @SideEffectFree implies NONDETERMINISM is allowed
                allows = new HashSet<>(baseAllows)
                allows.add('NONDETERMINISM')
            } else {
                return // no purity annotation — nothing to check
            }

            mn.code?.visit(makeVisitor(allows, null, null, checked))
        }
    }

    private static Set<String> parseAllows(String allowsStr) {
        if (!allowsStr) return Collections.emptySet()
        allowsStr.split('\\|')*.trim()*.toUpperCase(Locale.ROOT) as Set<String>
    }

    private static boolean hasPureAnno(AnnotatedNode node) {
        node.annotations?.any { it.classNode?.nameWithoutPackage in PURE_ANNOS } ?: false
    }

    private static boolean hasAnyPureAnno(MethodNode method) {
        hasPureAnno(method) || hasSideEffectFreeAnno(method) || hasContractPureAnno(method) || hasMemoizedAnno(method)
    }

    private static Variable targetVariable(VariableExpression ve) {
        Variable v = ve
        while (v instanceof VariableExpression && v.accessedVariable != null && !v.accessedVariable.is(v)) {
            v = v.accessedVariable
        }
        v
    }

    /**
     * A parameter declared {@code @Pure}, or a local whose declaration is annotated {@code @Pure}.
     */
    private static boolean isPureVariable(Expression expr) {
        if (!(expr instanceof VariableExpression)) return false
        Variable v = targetVariable(expr)
        if (v instanceof Parameter) return hasPureAnno(v)
        v instanceof VariableExpression && v.getNodeMetaData(PURE_LOCAL)
    }

    private static void markPureLocals(MethodNode mn) {
        mn.code?.visit(new CheckingVisitor() {
            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                super.visitDeclarationExpression(decl)
                if (!decl.isMultipleAssignmentDeclaration() && hasPureAnno(decl)) {
                    decl.variableExpression.putNodeMetaData(PURE_LOCAL, Boolean.TRUE)
                }
            }
        })
    }

    private boolean isPureMethodPointer(MethodPointerExpression pointer) {
        if (!(pointer.methodName instanceof ConstantExpression)) return false
        String name = pointer.methodName.text
        Expression receiver = pointer.expression
        ClassNode type = receiver instanceof ClassExpression ? receiver.type : getType(receiver)
        if (type == null) return false
        if (ImmutablePropertyUtils.isBuiltinImmutable(type.name)) return true
        List<MethodNode> candidates = []
        for (ClassNode c = type; c != null; c = c.superClass) {
            candidates.addAll(c.getMethods(name))
        }
        candidates && candidates.every { hasAnyPureAnno(it) }
    }

    /**
     * Checks a value supplied for a {@code @Pure} parameter or assigned to a {@code @Pure} local.
     */
    private void checkPureValue(Expression value, String role, Set<String> allows, Set<ClosureExpression> checked) {
        if (value instanceof ClosureExpression) { // includes lambdas
            if (checked.add(value)) {
                value.code?.visit(makeVisitor(allows, role, value, checked))
            }
        } else if (value instanceof MethodPointerExpression) { // includes method references
            if (!isPureMethodPointer(value)) {
                addStaticTypeError("@Pure violation: method reference ${role} does not refer to a @Pure method", value)
            }
        } else if (value instanceof VariableExpression) {
            if (!isPureVariable(value)) {
                addStaticTypeError("@Pure violation: '${value.name}' ${role} is not declared @Pure", value)
            }
        } else if (!(value instanceof ConstantExpression && value.isNullExpression())) {
            addStaticTypeError("@Pure violation: value ${role} cannot be verified as pure", value)
        }
    }

    /**
     * Visits any method body, checking arguments for {@code @Pure} parameters and values assigned to {@code @Pure} locals.
     */
    private CheckingVisitor makeArgumentVisitor(Set<String> allows, Set<ClosureExpression> checked) {
        new CheckingVisitor() {

            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                super.visitDeclarationExpression(decl)
                def var = decl.isMultipleAssignmentDeclaration() ? null : decl.variableExpression
                if (var?.getNodeMetaData(PURE_LOCAL)) {
                    checkPureValue(decl.rightExpression, "assigned to @Pure variable '${var.name}'", allows, checked)
                }
            }

            @Override
            void visitBinaryExpression(BinaryExpression expression) {
                super.visitBinaryExpression(expression)
                if (!(expression instanceof DeclarationExpression) && expression.operation.type == Types.ASSIGN
                        && isPureVariable(expression.leftExpression)) {
                    checkPureValue(expression.rightExpression, "assigned to @Pure variable '${expression.leftExpression.text}'", allows, checked)
                }
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                checkArguments(call, call.arguments)
            }

            @Override
            void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                super.visitStaticMethodCallExpression(call)
                checkArguments(call, call.arguments)
            }

            @Override
            void visitConstructorCallExpression(ConstructorCallExpression call) {
                super.visitConstructorCallExpression(call)
                checkArguments(call, call.arguments)
            }

            private void checkArguments(Expression call, Expression arguments) {
                def target = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                if (!(target instanceof MethodNode)) return
                Parameter[] params = target.parameters
                if (!params?.any { hasPureAnno(it) }) return
                List<Expression> args = arguments instanceof TupleExpression ? arguments.expressions : [arguments]
                args.eachWithIndex { Expression arg, int i ->
                    Parameter param = i < params.length ? params[i] : (params[-1].type.isArray() ? params[-1] : null)
                    if (param != null && hasPureAnno(param)) {
                        checkPureValue(arg, "passed to @Pure parameter '${param.name}'", allows, checked)
                    }
                }
            }
        }
    }

    private static boolean hasSideEffectFreeAnno(MethodNode method) {
        method.annotations?.any { it.classNode?.nameWithoutPackage in SIDE_EFFECT_FREE_ANNOS } ?: false
    }

    /**
     * Checks for {@code @Contract(pure = true)} (JetBrains annotations).
     * Works with CLASS retention since annotation nodes are available during type checking.
     */
    private static boolean hasContractPureAnno(MethodNode method) {
        method.annotations?.any { anno ->
            anno.classNode?.nameWithoutPackage in CONTRACT_ANNOS &&
                    anno.getMember('pure')?.text == 'true'
        } ?: false
    }

    private static boolean hasMemoizedAnno(MethodNode method) {
        method.annotations?.any { it.classNode?.nameWithoutPackage in MEMOIZED_ANNOS } ?: false
    }

    /**
     * Creates the visitor checking a {@code @Pure} method body, or (when {@code root} is given) the body of a
     * closure that must be pure, where {@code role} describes where the closure is passed or assigned.
     */
    private CheckingVisitor makeVisitor(Set<String> allows, String role, ClosureExpression root, Set<ClosureExpression> checked) {
        boolean allowLogging = 'LOGGING' in allows
        boolean allowMetrics = 'METRICS' in allows
        boolean allowIO = 'IO' in allows
        boolean allowNondeterminism = 'NONDETERMINISM' in allows
        // variables declared within root; writes to any other local are writes to captured variables
        Set<Variable> rootLocals = Collections.newSetFromMap(new IdentityHashMap<>())
        if (root?.parameters) rootLocals.addAll(root.parameters)

        new CheckingVisitor() {

            private void report(String message, Expression node) {
                if (role) {
                    message = message.replaceFirst(/^@Pure (violation|warning): /, '@Pure $1 in closure ' + Matcher.quoteReplacement(role) + ': ')
                }
                addStaticTypeError(message, node)
            }

            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                // Local variable declarations are always fine; record them before the initializing assignment is visited
                if (root != null) {
                    if (decl.isMultipleAssignmentDeclaration()) {
                        rootLocals.addAll(decl.tupleExpression.expressions)
                    } else {
                        rootLocals.add(decl.variableExpression)
                    }
                }
                super.visitDeclarationExpression(decl)
            }

            @Override
            void visitClosureExpression(ClosureExpression expression) {
                if (checked.contains(expression)) return // checked against its own @Pure parameter or local
                if (root != null && expression.parameters) rootLocals.addAll(expression.parameters)
                super.visitClosureExpression(expression)
            }

            @Override
            void visitBinaryExpression(BinaryExpression expression) {
                super.visitBinaryExpression(expression)
                int op = expression.operation.type
                Expression left = expression.leftExpression
                if (isAssignment(op)) {
                    if (isSubscript(left)) {
                        // a[i] = v and a[i] += v store through a.putAt(i, ...)
                        checkOperatorCall(((BinaryExpression) left).leftExpression, 'putAt', null, '[]' + expression.operation.text, expression)
                    } else if (expression.getNodeMetaData(StaticTypesMarker.COMPOUND_ASSIGN_TARGET) instanceof MethodNode) {
                        // GEP-15: a += v resolved to a *Assign method mutates a in place rather than reassigning it
                        MethodNode target = expression.getNodeMetaData(StaticTypesMarker.COMPOUND_ASSIGN_TARGET)
                        checkOperatorCall(left, target.name, target, expression.operation.text, expression)
                    } else {
                        checkFieldWrite(left, expression)
                    }
                } else if (op == Types.LEFT_SHIFT) {
                    checkOperatorCall(left, 'leftShift', expression.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET), '<<', expression)
                }
            }

            @Override
            void visitPostfixExpression(PostfixExpression expression) {
                super.visitPostfixExpression(expression)
                checkIncrementTarget(expression.expression, expression.operation.text, expression)
            }

            @Override
            void visitPrefixExpression(PrefixExpression expression) {
                super.visitPrefixExpression(expression)
                checkIncrementTarget(expression.expression, expression.operation.text, expression)
            }

            private void checkIncrementTarget(Expression target, String operator, Expression context) {
                if (isSubscript(target)) {
                    checkOperatorCall(((BinaryExpression) target).leftExpression, 'putAt', null, '[]' + operator, context)
                } else {
                    checkFieldWrite(target, context)
                }
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                checkInstanceCall(call)
            }

            @Override
            void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                super.visitStaticMethodCallExpression(call)
                if (isRenamedCompoundAssign(call)) {
                    // @OperatorRename(plusAssign = 'addInPlace') turns a += v into compoundAssign(a, v, 'addInPlace', ...)
                    List<Expression> args = ((TupleExpression) call.arguments).expressions
                    Expression receiver = args[0]
                    String methodName = args[2].text
                    ClassNode receiverType = getType(receiver)
                    List<MethodNode> candidates = receiverType ? receiverType.getMethods(methodName) : []
                    checkOperatorCall(receiver, methodName, candidates.size() == 1 ? candidates[0] : null, 'compound assignment', call)
                    return
                }
                checkStaticCall(call)
            }

            private boolean isRenamedCompoundAssign(StaticMethodCallExpression call) {
                call.ownerType.name == 'org.codehaus.groovy.runtime.ScriptBytecodeAdapter' && call.method == 'compoundAssign'
                        && call.arguments instanceof TupleExpression && ((TupleExpression) call.arguments).expressions.size() == 4
                        && ((TupleExpression) call.arguments).expressions[2] instanceof ConstantExpression
            }

            @Override
            void visitConstructorCallExpression(ConstructorCallExpression call) {
                super.visitConstructorCallExpression(call)
                checkConstructorCall(call)
            }

            // ---- field mutation check ----

            private void checkFieldWrite(Expression target, Expression context) {
                if (target instanceof PropertyExpression) {
                    def objExpr = target.objectExpression
                    if (objExpr instanceof VariableExpression && objExpr.isThisExpression()) {
                        report("@Pure violation: field assignment to 'this.${target.propertyAsString}'", context)
                    } else if (objExpr instanceof VariableExpression && !objExpr.isThisExpression()) {
                        // Writing to a property on a parameter or local variable (e.g., param.x = 1)
                        report("@Pure violation: property assignment to '${objExpr.name}.${target.propertyAsString}'", context)
                    }
                } else if (target instanceof VariableExpression) {
                    Variable accessedVar = findTargetVariable(target)
                    if (accessedVar instanceof FieldNode) {
                        report("@Pure violation: field assignment to '${accessedVar.name}'", context)
                    } else if (root != null && !rootLocals.contains(accessedVar) && target.name != 'it') {
                        report("@Pure violation: write to captured variable '${target.name}'", context)
                    }
                }
            }

            // ---- instance method call check ----

            private void checkInstanceCall(MethodCallExpression call) {
                String methodName = call.methodAsString
                Expression receiver = call.objectExpression

                // Static call via ClassExpression (e.g., System.nanoTime())
                if (receiver instanceof ClassExpression) {
                    checkStaticCallOnClass(receiver.type.name, methodName, call)
                    return
                }

                // Invoking a @Pure parameter or local (f(x), f.call(x), f.apply(x))
                if (isPureVariable(receiver) && isInvocation(receiver, methodName)) return

                // Check logging by method name (println, print, printf — including implicit this)
                if (methodName in LOGGING_METHOD_NAMES) {
                    if (!allowLogging) {
                        report("@Pure violation: '${methodName}()' is a logging/output call (allow with LOGGING)", call)
                    }
                    return
                }

                // Check receiver type for logging/metrics/IO
                if (checkReceiverType(getType(receiver), methodName, "call to '${methodName}()'", call)) return

                // Check callee annotations
                def targetMethod = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                if (targetMethod instanceof MethodNode && hasAnyPureAnno(targetMethod)) return

                // Known pure method name
                if (methodName in KNOWN_PURE_METHODS) return

                // GDK or stream method that is pure when its functional arguments are pure
                if (isClosurePolymorphic(targetMethod, methodName) && functionalArguments(call).every { isPureFunctionalArgument(it) }) return

                // Unknown — warn
                String receiverName = resolveReceiverName(receiver)
                if (receiverName) {
                    report("@Pure warning: call to '${receiverName}.${methodName}()' — purity cannot be verified (consider adding @Pure)", call)
                } else if (!(receiver instanceof VariableExpression && receiver.isThisExpression())) {
                    report("@Pure warning: call to '${methodName}()' — purity cannot be verified", call)
                } else {
                    // Call on this — check callee
                    if (targetMethod instanceof MethodNode) {
                        report("@Pure warning: call to 'this.${methodName}()' — purity cannot be verified (consider adding @Pure)", call)
                    }
                }
            }

            /**
             * Applies the checks that depend only on the receiver's type: logging, metrics, I/O and
             * non-deterministic types report (unless allowed), and immutable types are pure.
             *
             * @param what how the call is described in a report, e.g. {@code call to 'add()'}
             * @return whether the receiver type decided the call, so no further checks apply
             */
            private boolean checkReceiverType(ClassNode receiverType, String methodName, String what, Expression node) {
                if (!receiverType) return false
                String typeName = receiverType.name

                // Check logging by receiver type
                if (LOGGING_TYPE_PREFIXES.any { typeName.startsWith(it) }) {
                    if (!allowLogging) {
                        report("@Pure violation: ${what} on logging type (allow with LOGGING)", node)
                    }
                    return true
                }

                // Check metrics by receiver type
                if (METRICS_TYPE_PREFIXES.any { typeName.startsWith(it) }) {
                    if (!allowMetrics) {
                        report("@Pure violation: ${what} on metrics type (allow with METRICS)", node)
                    }
                    return true
                }

                // Check I/O by receiver type (but not PrintStream used for logging)
                if (IO_TYPE_PREFIXES.any { typeName.startsWith(it) }) {
                    if (!allowIO) {
                        report("@Pure violation: ${what} is an I/O operation (allow with IO)", node)
                    }
                    return true
                }

                // Check non-deterministic instance methods
                Set<String> nonDetMethods = NONDETERMINISTIC_INSTANCE_METHODS[typeName]
                if (nonDetMethods && methodName in nonDetMethods) {
                    if (!allowNondeterminism) {
                        report("@Pure violation: '${typeName}.${methodName}()' is non-deterministic (allow with NONDETERMINISM)", node)
                    }
                    return true
                }

                // Immutable receiver — all calls are pure
                ImmutablePropertyUtils.isBuiltinImmutable(typeName)
            }

            // ---- mutating operator check ----

            /**
             * Checks an operator that stands for a method call on {@code receiver}, such as
             * {@code a << b} ({@code leftShift}), {@code a[i] = v} ({@code putAt}) or a GEP-15
             * {@code a += v} resolved to {@code plusAssign}, as that call. Only receivers outside
             * the method's own state are checked; building up a local value is left alone.
             */
            private void checkOperatorCall(Expression receiver, String methodName, Object targetMethod, String operator, Expression node) {
                if (!isNonLocalReceiver(receiver)) return
                if (checkReceiverType(getType(receiver), methodName, "'${operator}' (${methodName}())", node)) return
                if (targetMethod instanceof MethodNode && hasAnyPureAnno(targetMethod)) return
                String receiverName = resolveReceiverName(receiver) ?: receiver.text
                report("@Pure warning: '${operator}' on '${receiverName}' calls ${methodName}() — purity cannot be verified", node)
            }

            /**
             * Whether mutating {@code receiver} changes state the method does not own: a caller's
             * argument, a field or property, {@code this}, or a variable captured by a checked closure.
             */
            private boolean isNonLocalReceiver(Expression receiver) {
                if (receiver instanceof PropertyExpression) return true
                if (!(receiver instanceof VariableExpression)) return false
                if (receiver.isThisExpression()) return true
                Variable v = findTargetVariable(receiver)
                if (v instanceof Parameter || v instanceof FieldNode || v instanceof PropertyNode) return true
                root != null && !rootLocals.contains(v)
            }

            // ---- static method call check ----

            private void checkStaticCall(StaticMethodCallExpression call) {
                checkStaticCallOnClass(call.ownerType.name, call.methodAsString, call)
            }

            private void checkStaticCallOnClass(String ownerName, String methodName, Expression call) {
                // Check non-deterministic static methods
                Set<String> nonDetMethods = NONDETERMINISTIC_STATIC_METHODS[ownerName]
                if (nonDetMethods && methodName in nonDetMethods) {
                    if (!allowNondeterminism) {
                        report("@Pure violation: '${ownerName}.${methodName}()' is non-deterministic (allow with NONDETERMINISM)", call)
                    }
                    return
                }

                // I/O static methods
                if (IO_TYPE_PREFIXES.any { ownerName.startsWith(it) }) {
                    if (!allowIO) {
                        report("@Pure violation: '${ownerName}.${methodName}()' is an I/O operation (allow with IO)", call)
                    }
                    return
                }

                // Immutable type — all static methods are pure
                if (ImmutablePropertyUtils.isBuiltinImmutable(ownerName)) return

                // Check callee annotations
                def targetMethod = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                if (targetMethod instanceof MethodNode) {
                    if (hasPureAnno(targetMethod) || hasSideEffectFreeAnno(targetMethod) || hasContractPureAnno(targetMethod) || hasMemoizedAnno(targetMethod)) return
                }

                // Known pure method name on known type
                if (methodName in KNOWN_PURE_METHODS) return
            }

            // ---- constructor call check ----

            private void checkConstructorCall(ConstructorCallExpression call) {
                String typeName = call.type.name

                // Non-deterministic constructors (new Date(), new Random())
                if (typeName in NONDETERMINISTIC_CONSTRUCTORS) {
                    if (!allowNondeterminism) {
                        report("@Pure violation: 'new ${call.type.nameWithoutPackage}()' is non-deterministic (allow with NONDETERMINISM)", call)
                    }
                    return
                }

                // I/O constructors (new File(...), new Socket(...), etc.)
                if (IO_CONSTRUCTOR_PREFIXES.any { typeName.startsWith(it) }) {
                    if (!allowIO) {
                        report("@Pure violation: 'new ${call.type.nameWithoutPackage}(...)' is an I/O operation (allow with IO)", call)
                    }
                }
            }

            // ---- helpers ----

            private static boolean isSubscript(Expression expr) {
                expr instanceof BinaryExpression && expr.operation.type == Types.LEFT_SQUARE_BRACKET
            }

            private boolean isInvocation(Expression receiver, String methodName) {
                if (methodName == 'call' || methodName == 'doCall') return true
                ClassNode type = getType(receiver)
                MethodNode sam = type != null ? ClassHelper.findSAM(type) : null
                sam != null && sam.name == methodName
            }

            private boolean isClosurePolymorphic(Object target, String methodName) {
                if (!(methodName in CLOSURE_PURE_METHODS) || !(target instanceof MethodNode)) return false
                if (target instanceof ExtensionMethodNode) {
                    return target.extensionMethodNode.declaringClass.name.startsWith('org.codehaus.groovy.runtime.')
                }
                target.declaringClass.name.startsWith('java.util.stream.')
            }

            private List<Expression> functionalArguments(MethodCallExpression call) {
                List<Expression> args = call.arguments instanceof TupleExpression ? ((TupleExpression) call.arguments).expressions : [call.arguments]
                args.findAll { Expression arg ->
                    if (arg instanceof ClosureExpression || arg instanceof MethodPointerExpression) return true
                    ClassNode type = getType(arg)
                    type != null && (type.name == 'groovy.lang.Closure' || ClassHelper.isSAMType(type))
                }
            }

            private boolean isPureFunctionalArgument(Expression arg) {
                if (arg instanceof ClosureExpression) return true // its body is visited as part of this method
                if (arg instanceof MethodPointerExpression) return isPureMethodPointer(arg)
                isPureVariable(arg)
            }

            private String resolveReceiverName(Expression receiver) {
                if (receiver instanceof VariableExpression) return receiver.name
                if (receiver instanceof PropertyExpression) {
                    def obj = receiver.objectExpression
                    if (obj instanceof VariableExpression && obj.isThisExpression()) {
                        return receiver.propertyAsString
                    }
                }
                null
            }
        }
    }
}
