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
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

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

    @Override
    Object run() {
        Set<String> baseAllows = parseAllows(options?.allows as String)

        afterVisitMethod { MethodNode mn ->
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

            mn.code?.visit(makeVisitor(allows, mn))
        }
    }

    private static Set<String> parseAllows(String allowsStr) {
        if (!allowsStr) return Collections.emptySet()
        allowsStr.split('\\|')*.trim()*.toUpperCase() as Set<String>
    }

    private static boolean hasPureAnno(MethodNode method) {
        method.annotations?.any { it.classNode?.nameWithoutPackage in PURE_ANNOS } ?: false
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

    private CheckingVisitor makeVisitor(Set<String> allows, MethodNode methodNode) {
        boolean allowLogging = 'LOGGING' in allows
        boolean allowMetrics = 'METRICS' in allows
        boolean allowIO = 'IO' in allows
        boolean allowNondeterminism = 'NONDETERMINISM' in allows

        new CheckingVisitor() {

            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                super.visitDeclarationExpression(decl)
                // Local variable declarations are always fine
            }

            @Override
            void visitBinaryExpression(BinaryExpression expression) {
                super.visitBinaryExpression(expression)
                if (isAssignment(expression.operation.type)) {
                    checkFieldWrite(expression.leftExpression, expression)
                }
            }

            @Override
            void visitPostfixExpression(PostfixExpression expression) {
                super.visitPostfixExpression(expression)
                checkFieldWrite(expression.expression, expression)
            }

            @Override
            void visitPrefixExpression(PrefixExpression expression) {
                super.visitPrefixExpression(expression)
                checkFieldWrite(expression.expression, expression)
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                checkInstanceCall(call)
            }

            @Override
            void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                super.visitStaticMethodCallExpression(call)
                checkStaticCall(call)
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
                        addStaticTypeError("@Pure violation: field assignment to 'this.${target.propertyAsString}'", context)
                    } else if (objExpr instanceof VariableExpression && !objExpr.isThisExpression()) {
                        // Writing to a property on a parameter or local variable (e.g., param.x = 1)
                        addStaticTypeError("@Pure violation: property assignment to '${objExpr.name}.${target.propertyAsString}'", context)
                    }
                } else if (target instanceof VariableExpression) {
                    Variable accessedVar = findTargetVariable(target)
                    if (accessedVar instanceof FieldNode) {
                        addStaticTypeError("@Pure violation: field assignment to '${accessedVar.name}'", context)
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

                // Check logging by method name (println, print, printf — including implicit this)
                if (methodName in LOGGING_METHOD_NAMES) {
                    if (!allowLogging) {
                        addStaticTypeError("@Pure violation: '${methodName}()' is a logging/output call (allow with LOGGING)", call)
                    }
                    return
                }

                // Check receiver type for logging/metrics/IO
                ClassNode receiverType = getType(receiver)
                if (receiverType) {
                    String typeName = receiverType.name

                    // Check logging by receiver type
                    if (LOGGING_TYPE_PREFIXES.any { typeName.startsWith(it) }) {
                        if (!allowLogging) {
                            addStaticTypeError("@Pure violation: call to '${methodName}()' on logging type (allow with LOGGING)", call)
                        }
                        return
                    }

                    // Check metrics by receiver type
                    if (METRICS_TYPE_PREFIXES.any { typeName.startsWith(it) }) {
                        if (!allowMetrics) {
                            addStaticTypeError("@Pure violation: call to '${methodName}()' on metrics type (allow with METRICS)", call)
                        }
                        return
                    }

                    // Check I/O by receiver type (but not PrintStream used for logging)
                    if (IO_TYPE_PREFIXES.any { typeName.startsWith(it) }) {
                        if (!allowIO) {
                            addStaticTypeError("@Pure violation: call to '${methodName}()' is an I/O operation (allow with IO)", call)
                        }
                        return
                    }

                    // Check non-deterministic instance methods
                    Set<String> nonDetMethods = NONDETERMINISTIC_INSTANCE_METHODS[typeName]
                    if (nonDetMethods && methodName in nonDetMethods) {
                        if (!allowNondeterminism) {
                            addStaticTypeError("@Pure violation: '${typeName}.${methodName}()' is non-deterministic (allow with NONDETERMINISM)", call)
                        }
                        return
                    }

                    // Immutable receiver — all calls are pure
                    if (ImmutablePropertyUtils.isBuiltinImmutable(typeName)) return
                }

                // Check callee annotations
                def targetMethod = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                if (targetMethod instanceof MethodNode) {
                    if (hasPureAnno(targetMethod) || hasSideEffectFreeAnno(targetMethod) || hasContractPureAnno(targetMethod) || hasMemoizedAnno(targetMethod)) return
                }

                // Known pure method name
                if (methodName in KNOWN_PURE_METHODS) return

                // Unknown — warn
                String receiverName = resolveReceiverName(receiver)
                if (receiverName) {
                    addStaticTypeError("@Pure warning: call to '${receiverName}.${methodName}()' — purity cannot be verified (consider adding @Pure)", call)
                } else if (!(receiver instanceof VariableExpression && receiver.isThisExpression())) {
                    addStaticTypeError("@Pure warning: call to '${methodName}()' — purity cannot be verified", call)
                } else {
                    // Call on this — check callee
                    if (targetMethod instanceof MethodNode) {
                        addStaticTypeError("@Pure warning: call to 'this.${methodName}()' — purity cannot be verified (consider adding @Pure)", call)
                    }
                }
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
                        addStaticTypeError("@Pure violation: '${ownerName}.${methodName}()' is non-deterministic (allow with NONDETERMINISM)", call)
                    }
                    return
                }

                // I/O static methods
                if (IO_TYPE_PREFIXES.any { ownerName.startsWith(it) }) {
                    if (!allowIO) {
                        addStaticTypeError("@Pure violation: '${ownerName}.${methodName}()' is an I/O operation (allow with IO)", call)
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
                        addStaticTypeError("@Pure violation: 'new ${call.type.nameWithoutPackage}()' is non-deterministic (allow with NONDETERMINISM)", call)
                    }
                    return
                }

                // I/O constructors (new File(...), new Socket(...), etc.)
                if (IO_CONSTRUCTOR_PREFIXES.any { typeName.startsWith(it) }) {
                    if (!allowIO) {
                        addStaticTypeError("@Pure violation: 'new ${call.type.nameWithoutPackage}(...)' is an I/O operation (allow with IO)", call)
                    }
                }
            }

            // ---- helpers ----

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
