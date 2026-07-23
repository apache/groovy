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

import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

/**
 * Checks at compile-time for a common SQL injection mistake when using {@code groovy.sql.Sql}:
 * surrounding an interpolated GString expression with SQL quotes, for example:
 * <pre>
 *     sql.rows("select * from Book where title = '${title}'")   // '${...}' &rarr; injection risk
 * </pre>
 * Quoting a dynamic expression prevents Groovy from binding the value through a JDBC
 * {@code PreparedStatement} placeholder, so the value would have to be inlined into the SQL text,
 * reopening a SQL injection hole (CWE-89). The correct form omits the quotes so the value is bound
 * as a parameter:
 * <pre>
 *     sql.rows("select * from Book where title = $title")       // bound as a '?' placeholder
 * </pre>
 *
 * <p>This is a compile-time complement to the runtime protection in {@code Sql.asSql(GString, List)}
 * (GROOVY-12118). The runtime guard only fires when a <em>GString</em> reaches the query method. It
 * cannot see the value once the GString has been coerced to a plain {@code String} before the call,
 * because the interpolated value is then already inlined and the {@code String} overload is selected.
 * This checker covers exactly those coercion cases, which are otherwise silent:
 * <pre>
 *     String query = "... where title = '${title}'"; sql.rows(query)   // assignment coercion
 *     sql.rows("... where title = '${title}'".toString())              // explicit toString()
 *     sql.rows("... where title = '${title}'" as String)              // cast coercion
 * </pre>
 * as well as flagging the direct GString case earlier (at compile time rather than at runtime).
 *
 * <h4>Flow tracking</h4>
 * Within a method the checker follows local-variable reassignment (last write wins), alias chains
 * ({@code String b = a}), and merges {@code if}/{@code else} branches pessimistically (unsafe in
 * either branch is unsafe afterwards). Taint reaching a sink through a <em>local</em> is reported as a
 * compile error; taint reaching a sink through a <em>field</em> initializer is reported as a warning
 * only, because a field may be reassigned elsewhere before the query runs.
 *
 * <h4>Suppression</h4>
 * A reviewed call site can opt out by annotating the enclosing method (or class) with
 * {@code @SuppressWarnings("groovy.sql.injection")}.
 *
 * <h4>Limitations</h4>
 * The analysis is intra-procedural and opt-in via {@code @TypeChecked}, so it is a defence-in-depth
 * aid rather than a guarantee: strings built across method boundaries, by raw concatenation, or read
 * from external sources are not tracked, and dynamic (non type-checked) code still relies on the
 * runtime guard.
 *
 * <p>Usage:
 * <pre>
 *     {@code @TypeChecked}(extensions='groovy.typecheckers.SqlInjectionChecker')
 * </pre>
 *
 * @since 6.0.0
 */
class SqlInjectionChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    private static final String SQL_CLASS_NAME = 'groovy.sql.Sql'
    private static final String SUPPRESS_KEY = 'groovy.sql.injection'

    /** Query-executing methods on {@code groovy.sql.Sql} that accept a SQL string/GString argument. */
    private static final List<String> SINK_METHODS =
        ['execute', 'executeInsert', 'firstRow', 'rows', 'eachRow', 'query', 'call']

    /** Current RHS expression bound to each local variable (last write wins); reset per method. */
    private final Map<Variable, Expression> bindings = [:]

    @Override
    Object run() {
        // Run after the method has been type-checked so inferred receiver types (e.g. `def sql = ...`)
        // are available; the GString shape check itself is purely syntactic.
        afterVisitMethod { MethodNode method ->
            if (isSuppressed(method) || isSuppressed(method.declaringClass)) return
            bindings.clear()
            method.code?.visit(newSinkVisitor())
        }
    }

    private CheckingVisitor newSinkVisitor() {
        new CheckingVisitor() {
            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                super.visitDeclarationExpression(decl)
                if (decl.variableExpression != null) {
                    bindings.put(findTargetVariable(decl.variableExpression), decl.rightExpression)
                }
            }

            @Override
            void visitBinaryExpression(BinaryExpression expr) {
                super.visitBinaryExpression(expr)
                if (expr.operation.type == Types.EQUAL && expr.leftExpression instanceof VariableExpression) {
                    bindings.put(findTargetVariable(expr.leftExpression), expr.rightExpression)
                }
            }

            @Override
            void visitIfElse(IfStatement stmt) {
                stmt.booleanExpression.visit(this)
                def before = new LinkedHashMap<Variable, Expression>(bindings)
                stmt.ifBlock.visit(this)
                def afterThen = new LinkedHashMap<Variable, Expression>(bindings)
                bindings.clear(); bindings.putAll(before)
                stmt.elseBlock.visit(this)
                def afterElse = new LinkedHashMap<Variable, Expression>(bindings)
                // pessimistic merge: a variable that is unsafe in either branch stays unsafe afterwards
                bindings.clear()
                for (var key : (afterThen.keySet() + afterElse.keySet())) {
                    def fromThen = afterThen.get(key)
                    bindings.put(key, isDangerous(fromThen, afterThen) ? fromThen : afterElse.get(key))
                }
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                if (isSqlReceiver(call.objectExpression) && call.methodAsString in SINK_METHODS) {
                    for (arg in call.arguments.expressions) {
                        boolean[] viaField = new boolean[1]
                        def gstring = resolveDangerousGString(arg, bindings, viaField, new HashSet<Variable>())
                        if (gstring != null) {
                            report(gstring, arg, viaField[0])
                        }
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Resolves the underlying <em>dangerous</em> GString reachable from an argument expression,
     * unwrapping the coercions that hide it from the runtime guard (cast, {@code toString()}) and
     * following local-variable bindings and alias chains. Sets {@code viaField[0]} when resolution
     * passes through a field initializer (a lower-confidence path). The {@code visiting} set guards
     * against alias cycles.
     */
    private GStringExpression resolveDangerousGString(Expression exp, Map<Variable, Expression> binds,
                                                      boolean[] viaField, Set<Variable> visiting) {
        if (exp instanceof GStringExpression) {
            return firstQuotedInterpolationIndex(exp) >= 0 ? exp : null
        }
        if (exp instanceof CastExpression) {
            return resolveDangerousGString(exp.expression, binds, viaField, visiting)
        }
        if (exp instanceof MethodCallExpression && exp.methodAsString == 'toString' && !exp.arguments.expressions) {
            return resolveDangerousGString(exp.objectExpression, binds, viaField, visiting)
        }
        if (exp instanceof VariableExpression) {
            def var = findTargetVariable(exp)
            if (!visiting.add(var)) return null // alias cycle
            if (binds.containsKey(var)) {
                return resolveDangerousGString(binds.get(var), binds, viaField, visiting)
            }
            if (var instanceof FieldNode && var.hasInitialExpression()) {
                viaField[0] = true
                return resolveDangerousGString(var.initialExpression, binds, viaField, visiting)
            }
        }
        null
    }

    private boolean isDangerous(Expression exp, Map<Variable, Expression> binds) {
        exp != null && resolveDangerousGString(exp, binds, new boolean[1], new HashSet<Variable>()) != null
    }

    private void report(GStringExpression gstring, Expression target, boolean viaField) {
        int idx = firstQuotedInterpolationIndex(gstring)
        String valueText = idx >= 0 ? exprText(gstring.values[idx]) : '?'
        String msg = 'Possible SQL injection: the interpolated value ${' + valueText + '} is surrounded by SQL ' +
            'quotes, which prevents it from being bound as a JDBC PreparedStatement parameter. ' +
            'Remove the surrounding quotes so the value is bound safely.'
        if (viaField) {
            // lower confidence: a field may be reassigned elsewhere before the query runs, so warn only
            context.source.addWarning(WarningMessage.LIKELY_ERRORS, msg +
                ' (warning only: value originates from a field that may be reassigned elsewhere; ' +
                'annotate the method with @SuppressWarnings("' + SUPPRESS_KEY + '") to silence)', target)
        } else {
            addStaticTypeError(msg, target)
        }
    }

    /**
     * Mirrors the detection in {@code Sql.asSql(GString, List)}: an interpolated value is unsafe when
     * the preceding string ends with a SQL quote and the following string starts with one, i.e. the
     * value sits inside {@code '...'} or {@code "..."} rather than at a placeholder position.
     *
     * @return index into {@code getValues()} of the first such value, or {@code -1} if none
     */
    private static int firstQuotedInterpolationIndex(GStringExpression gstring) {
        def strings = gstring.strings
        def values = gstring.values
        for (int i = 0; i < values.size() && i + 1 < strings.size(); i++) {
            String before = stringValue(strings[i])
            String after = stringValue(strings[i + 1])
            if (before && after
                    && (before.endsWith("'") || before.endsWith('"'))
                    && (after.startsWith("'") || after.startsWith('"'))) {
                return i
            }
        }
        -1
    }

    private boolean isSqlReceiver(Expression exp) {
        exp != null && (isSqlType(exp.type)
            || isSqlType(exp.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE))
            || isSqlType(exp.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE)))
    }

    private static boolean isSqlType(ClassNode type) {
        for (ClassNode cn = type; cn != null; cn = cn.superClass) {
            if (cn.name == SQL_CLASS_NAME) return true
        }
        false
    }

    private static boolean isSuppressed(AnnotatedNode node) {
        node?.annotations?.any { AnnotationNode anno ->
            anno.classNode.name == 'java.lang.SuppressWarnings' && SUPPRESS_KEY in suppressValues(anno.getMember('value'))
        }
    }

    private static List<String> suppressValues(Expression member) {
        if (member instanceof ConstantExpression) return [member.value?.toString()]
        if (member instanceof ListExpression) {
            return member.expressions.findResults { it instanceof ConstantExpression ? it.value?.toString() : null }
        }
        []
    }

    private static Variable findTargetVariable(VariableExpression ve) {
        def accessed = ve.accessedVariable
        if (accessed != null && accessed != ve) {
            return accessed instanceof VariableExpression ? findTargetVariable(accessed) : accessed
        }
        ve
    }

    private static String stringValue(Expression exp) {
        exp instanceof ConstantExpression && exp.value instanceof String ? (String) exp.value : null
    }

    private static String exprText(Expression exp) {
        exp instanceof VariableExpression ? exp.name : exp.text
    }
}
