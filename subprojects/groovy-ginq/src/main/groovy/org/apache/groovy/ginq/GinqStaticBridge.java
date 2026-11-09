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
package org.apache.groovy.ginq;

import groovy.transform.Internal;
import org.apache.groovy.ginq.provider.collection.runtime.LazyQueryable;
import org.apache.groovy.ginq.provider.collection.runtime.Queryable;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Opcodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.indexX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Connects a lowered GINQ query to {@code @CompileStatic} and {@code @TypeChecked}.
 * <p>
 * The macro runs at {@code CONVERSION}, before either checker. The lowered tree is
 * ordinary {@link Queryable} calls whose clause bodies use Groovy's dynamic protocol.
 * This class does not type-check or emit bytecode. It does two things the checkers
 * already know how to consume:
 * <ul>
 * <li>Adds {@link GinqRowChecks} to the enclosing checking annotation, which is the
 * extension list {@code StaticCompileTransformation} reads at instruction selection.</li>
 * <li>Puts the lowered tree in a closure marked {@link StaticTypesMarker#DYNAMIC_RESOLUTION}.
 * The static closure writer emits that closure with the dynamic writer, so operators
 * and row calls stay dynamic. The enclosing method stays on the static writer.
 * The closure captures locals; this class does not.</li>
 * </ul>
 * A {@link Queryable} result is handed to {@link LazyQueryable#defer} so the
 * closure is built when the expression runs and called on the first read. {@code GQL}
 * and a {@code @GQ} method that returns a list call {@code toList} on that result,
 * which is the read.
 */
@Internal
public final class GinqStaticBridge {

    private static final String ROW_CHECKS = "org.apache.groovy.ginq.GinqRowChecks";
    private static final String HOLDER = "ginqShell$";
    private static final String CAPTURED = "ginqCaptured$";

    private GinqStaticBridge() {
    }

    /**
     * Adapts a macro result. A dynamic scope is returned unchanged.
     *
     * @param sourceUnit the source unit being compiled
     * @param call the original {@code GQ} or {@code GQL} call
     * @param query the lowered query
     * @return the expression that replaces the macro call
     */
    static Expression embed(SourceUnit sourceUnit, MethodCallExpression call, Expression query) {
        if (sourceUnit == null || sourceUnit.getAST() == null || call == null || query == null) {
            return query;
        }
        Host host = findHost(sourceUnit, call);
        if (host == null || !host.checked()) {
            return query;
        }
        attachRowChecks(host.owner, host.executable);
        return wrap(query, call, returnsQueryable(query), visibleLocals(host, call));
    }

    /**
     * Adapts the body of a {@code @GQ} method. {@code defer} is false when the
     * method returns a list or a coerced value, because that result is read
     * before the method returns.
     *
     * @param sourceUnit the source unit being compiled
     * @param method the annotated method
     * @param query the lowered query
     * @param defer whether a {@link Queryable} result should wait for its first read
     * @return the method body expression
     */
    public static Expression prepare(SourceUnit sourceUnit, MethodNode method, Expression query, boolean defer) {
        if (sourceUnit == null || method == null || query == null || !checked(method)) {
            return query;
        }
        attachRowChecks(method.getDeclaringClass(), method);
        Set<String> visible = new LinkedHashSet<>();
        for (Parameter parameter : method.getParameters()) {
            if (parameter.getName() != null) {
                visible.add(parameter.getName());
            }
        }
        return wrap(query, method, defer && returnsQueryable(query), visible);
    }

    private static Expression wrap(Expression query, ASTNode position, boolean defer, Set<String> visible) {
        boolean queryable = returnsQueryable(query);
        List<String> readOnly = defer ? readOnlyCaptured(query, visible) : List.of();
        Expression body = readOnly.isEmpty() ? query : rewriteReadOnly(query, readOnly);
        ClosureExpression shell = closureX(Parameter.EMPTY_ARRAY, returnS(body));
        shell.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, Boolean.TRUE);
        shell.setSourcePosition(position);
        if (!defer) {
            MethodCallExpression call = callX(shell, "call");
            call.setImplicitThis(false);
            call.setSourcePosition(position);
            return queryable ? castToQueryable(call) : call;
        }

        List<Statement> statements = new ArrayList<>();
        if (!readOnly.isEmpty()) {
            List<Expression> elements = new ArrayList<>();
            for (String name : readOnly) {
                elements.add(varX(name));
            }
            VariableExpression array = new VariableExpression(CAPTURED, ClassHelper.OBJECT_TYPE.makeArray());
            array.setModifiers(Opcodes.ACC_FINAL);
            statements.add(stmt(declX(array, new ArrayExpression(ClassHelper.OBJECT_TYPE, elements))));
        }
        VariableExpression holder = new VariableExpression(HOLDER, ClassHelper.CLOSURE_TYPE);
        holder.setModifiers(Opcodes.ACC_FINAL);
        DeclarationExpression decl = declX(holder, shell);
        decl.setSourcePosition(position);
        statements.add(stmt(decl));

        MethodCallExpression run = callX(new VariableExpression(holder), "call");
        run.setImplicitThis(false);
        run.setSourcePosition(position);
        ClosureExpression supplier = closureX(Parameter.EMPTY_ARRAY, returnS(castToQueryable(run)));
        supplier.setSourcePosition(position);

        MethodCallExpression deferred = callX(classX(makeWithoutCaching(LazyQueryable.class)), "defer", args(supplier));
        deferred.setImplicitThis(false);
        deferred.setSourcePosition(position);
        deferred.setType(rawQueryable());
        statements.add(returnS(deferred));

        ClosureExpression outer = closureX(Parameter.EMPTY_ARRAY, block(statements.toArray(Statement[]::new)));
        outer.setSourcePosition(position);
        MethodCallExpression invoked = callX(outer, "call");
        invoked.setImplicitThis(false);
        invoked.setSourcePosition(position);
        return castToQueryable(invoked);
    }

    private static Expression castToQueryable(Expression expression) {
        var cast = castX(rawQueryable(), expression);
        cast.setSourcePosition(expression);
        return cast;
    }

    private static boolean returnsQueryable(Expression query) {
        if (query instanceof MethodCallExpression call && "shutdown".equals(call.getMethodAsString())) {
            return false;
        }
        ClassNode type = query.getType();
        return type != null && Queryable.class.getName().equals(type.getName());
    }

    private static ClassNode rawQueryable() {
        ClassNode type = makeWithoutCaching(Queryable.class).getPlainNodeReference();
        type.setGenericsTypes(null);
        type.setUsingGenerics(false);
        return type;
    }

    private static void attachRowChecks(ClassNode owner, MethodNode executable) {
        AnnotatedNode carrier = null;
        for (ClassNode current = owner; current != null; current = current.getOuterClass()) {
            if (hasActiveCheck(current)) {
                carrier = current;
                break;
            }
        }
        if (carrier == null && executable != null && hasActiveCheck(executable)) {
            carrier = executable;
        }
        if (carrier == null) {
            return;
        }
        for (AnnotationNode annotation : carrier.getAnnotations()) {
            if (isStaticCheck(annotation) && !isSkip(annotation)) {
                addExtension(annotation);
            }
        }
    }

    private static boolean hasActiveCheck(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (isStaticCheck(annotation) && !isSkip(annotation)) {
                return true;
            }
        }
        return false;
    }

    private static void addExtension(AnnotationNode annotation) {
        Expression existing = annotation.getMember("extensions");
        if (existing == null) {
            annotation.addMember("extensions", constX(ROW_CHECKS));
            return;
        }
        if (existing instanceof ConstantExpression constant) {
            if (ROW_CHECKS.equals(String.valueOf(constant.getValue()))) {
                return;
            }
            ListExpression list = new ListExpression();
            list.addExpression(constant);
            list.addExpression(constX(ROW_CHECKS));
            annotation.setMember("extensions", list);
            return;
        }
        if (existing instanceof ListExpression list) {
            List<Expression> copy = new ArrayList<>(list.getExpressions());
            for (Expression item : copy) {
                if (item instanceof ConstantExpression constant && ROW_CHECKS.equals(String.valueOf(constant.getValue()))) {
                    return;
                }
            }
            copy.add(constX(ROW_CHECKS));
            annotation.setMember("extensions", new ListExpression(copy));
        }
    }

    private static boolean checked(MethodNode method) {
        Boolean mode = explicitMode(method);
        if (mode != null) {
            return mode;
        }
        for (ClassNode current = method.getDeclaringClass(); current != null; current = current.getOuterClass()) {
            mode = explicitMode(current);
            if (mode != null) {
                return mode;
            }
        }
        return false;
    }

    /**
     * @return {@code TRUE} when the node asks for static checks, {@code FALSE} when it
     * explicitly skips them, or {@code null} when it does not say
     */
    private static Boolean explicitMode(AnnotatedNode node) {
        if (node == null) {
            return null;
        }
        Boolean mode = null;
        for (AnnotationNode annotation : node.getAnnotations()) {
            String simple = simpleName(annotation.getClassNode());
            if ("CompileDynamic".equals(simple)) {
                return Boolean.FALSE;
            }
            if (!isStaticCheck(annotation)) {
                continue;
            }
            if (isSkip(annotation)) {
                return Boolean.FALSE;
            }
            mode = Boolean.TRUE;
        }
        return mode;
    }

    private static boolean isStaticCheck(AnnotationNode annotation) {
        String simple = simpleName(annotation.getClassNode());
        return "CompileStatic".equals(simple) || "TypeChecked".equals(simple);
    }

    private static boolean isSkip(AnnotationNode annotation) {
        Expression value = annotation.getMember("value");
        if (value instanceof PropertyExpression property) {
            return "SKIP".equals(property.getPropertyAsString());
        }
        if (value instanceof ConstantExpression constant) {
            return "SKIP".equals(String.valueOf(constant.getValue()));
        }
        return false;
    }

    private static String simpleName(ClassNode type) {
        String raw = type.getName();
        int dot = raw.lastIndexOf('.');
        return dot < 0 ? raw : raw.substring(dot + 1);
    }

    private static Host findHost(SourceUnit sourceUnit, MethodCallExpression call) {
        for (ClassNode type : sourceUnit.getAST().getClasses()) {
            for (MethodNode method : new ArrayList<>(type.getMethods())) {
                if (method.getDeclaringClass() == type && contains(method.getCode(), call)) {
                    return new Host(type, method, method.getCode());
                }
            }
            for (MethodNode constructor : type.getDeclaredConstructors()) {
                if (contains(constructor.getCode(), call)) {
                    return new Host(type, constructor, constructor.getCode());
                }
            }
            for (FieldNode field : type.getFields()) {
                if (field.getDeclaringClass() == type && contains(field.getInitialExpression(), call)) {
                    return new Host(type, null, field.getInitialExpression());
                }
            }
            for (Statement initializer : type.getObjectInitializerStatements()) {
                if (contains(initializer, call)) {
                    return new Host(type, null, initializer);
                }
            }
        }
        return null;
    }

    private static boolean contains(Statement statement, MethodCallExpression call) {
        if (statement == null) {
            return false;
        }
        Finder finder = new Finder(call);
        statement.visit(finder);
        return finder.found;
    }

    private static boolean contains(Expression expression, MethodCallExpression call) {
        if (expression == null) {
            return false;
        }
        Finder finder = new Finder(call);
        expression.visit(finder);
        return finder.found;
    }

    /**
     * Names the lowered tree reads but does not write. They are copied when the
     * expression runs. A local the query writes is left for closure capture, so
     * the write stays visible to the caller.
     */
    private static List<String> readOnlyCaptured(Expression query, Set<String> visible) {
        List<String> captured = capturedLocals(query, visible);
        Set<String> mutated = mutated(query, captured);
        List<String> readOnly = new ArrayList<>();
        for (String name : captured) {
            if (!mutated.contains(name)) {
                readOnly.add(name);
            }
        }
        return readOnly;
    }

    private static List<String> capturedLocals(Expression query, Set<String> visible) {
        Set<String> declared = new LinkedHashSet<>();
        Set<String> referenced = new LinkedHashSet<>();
        query.visit(new CodeVisitorSupport() {
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                if (expression.getParameters() != null) {
                    for (Parameter parameter : expression.getParameters()) {
                        if (parameter.getName() != null) {
                            declared.add(parameter.getName());
                        }
                    }
                }
                super.visitClosureExpression(expression);
            }

            @Override
            public void visitDeclarationExpression(DeclarationExpression expression) {
                if (expression.isMultipleAssignmentDeclaration()) {
                    for (Expression part : expression.getTupleExpression().getExpressions()) {
                        if (part instanceof VariableExpression variable && variable.getName() != null) {
                            declared.add(variable.getName());
                        }
                    }
                } else if (expression.getVariableExpression().getName() != null) {
                    declared.add(expression.getVariableExpression().getName());
                }
                super.visitDeclarationExpression(expression);
            }

            @Override
            public void visitVariableExpression(VariableExpression expression) {
                String name = expression.getName();
                if (name != null && !name.isEmpty() && !expression.isThisExpression() && !expression.isSuperExpression()) {
                    referenced.add(name);
                }
            }
        });
        List<String> captured = new ArrayList<>();
        for (String name : referenced) {
            if (!declared.contains(name) && visible.contains(name)) {
                captured.add(name);
            }
        }
        return captured;
    }

    private static Set<String> mutated(Expression query, List<String> captured) {
        Set<String> mutated = new LinkedHashSet<>();
        query.visit(new CodeVisitorSupport() {
            private void consider(Expression expression) {
                if (expression instanceof VariableExpression variable && captured.contains(variable.getName())) {
                    mutated.add(variable.getName());
                }
            }

            @Override
            public void visitPostfixExpression(PostfixExpression expression) {
                consider(expression.getExpression());
                super.visitPostfixExpression(expression);
            }

            @Override
            public void visitPrefixExpression(PrefixExpression expression) {
                consider(expression.getExpression());
                super.visitPrefixExpression(expression);
            }

            @Override
            public void visitBinaryExpression(BinaryExpression expression) {
                if (Types.isAssignment(expression.getOperation().getType())) {
                    consider(expression.getLeftExpression());
                }
                super.visitBinaryExpression(expression);
            }
        });
        return mutated;
    }

    private static Expression rewriteReadOnly(Expression query, List<String> readOnly) {
        ClassCodeExpressionTransformer transformer = new ClassCodeExpressionTransformer() {
            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            @Override
            public Expression transform(Expression expression) {
                if (expression instanceof ClosureExpression closure) {
                    closure.visit(this);
                    return closure;
                }
                if (expression instanceof VariableExpression variable && readOnly.contains(variable.getName())) {
                    int index = readOnly.indexOf(variable.getName());
                    var replaced = indexX(varX(CAPTURED, ClassHelper.OBJECT_TYPE.makeArray()), constX(index));
                    replaced.setSourcePosition(variable);
                    return replaced;
                }
                return super.transform(expression);
            }
        };
        return transformer.transform(query);
    }

    private static Set<String> visibleLocals(Host host, MethodCallExpression call) {
        Set<String> methodScope = new LinkedHashSet<>();
        if (host.executable != null) {
            for (Parameter parameter : host.executable.getParameters()) {
                if (parameter.getName() != null) {
                    methodScope.add(parameter.getName());
                }
            }
        }
        ScopeCollector collector = new ScopeCollector(call, methodScope);
        if (host.scopeRoot instanceof Statement statement) {
            statement.visit(collector);
        } else if (host.scopeRoot instanceof Expression expression) {
            expression.visit(collector);
        }
        return collector.visible == null ? Set.of() : collector.visible;
    }

    private static final class Host {
        private final ClassNode owner;
        private final MethodNode executable;
        private final ASTNode scopeRoot;

        private Host(ClassNode owner, MethodNode executable, ASTNode scopeRoot) {
            this.owner = owner;
            this.executable = executable;
            this.scopeRoot = scopeRoot;
        }

        private boolean checked() {
            if (executable != null) {
                Boolean mode = explicitMode(executable);
                if (mode != null) {
                    return mode;
                }
            }
            for (ClassNode current = owner; current != null; current = current.getOuterClass()) {
                Boolean mode = explicitMode(current);
                if (mode != null) {
                    return mode;
                }
            }
            return false;
        }
    }

    private static final class Finder extends CodeVisitorSupport {
        private final MethodCallExpression target;
        private boolean found;

        private Finder(MethodCallExpression target) {
            this.target = target;
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (call == target) {
                found = true;
                return;
            }
            super.visitMethodCallExpression(call);
        }
    }

    /**
     * Locals and parameters in scope at the query. A local declared after the
     * query is not visible. Sibling closures are not visible.
     */
    private static final class ScopeCollector extends CodeVisitorSupport {
        private final MethodCallExpression target;
        private final Deque<Set<String>> scopes = new ArrayDeque<>();
        private Set<String> visible;

        private ScopeCollector(MethodCallExpression target, Set<String> methodScope) {
            this.target = target;
            scopes.push(methodScope);
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            scopes.push(closureScope(expression));
            super.visitClosureExpression(expression);
            scopes.pop();
        }

        private static Set<String> closureScope(ClosureExpression expression) {
            Set<String> scope = new LinkedHashSet<>();
            if (expression.getParameters() == null) {
                scope.add("it");
            } else {
                for (Parameter parameter : expression.getParameters()) {
                    if (parameter.getName() != null) {
                        scope.add(parameter.getName());
                    }
                }
            }
            return scope;
        }

        @Override
        public void visitForLoop(ForStatement statement) {
            Set<String> scope = new LinkedHashSet<>();
            Parameter variable = statement.getValueVariable();
            if (variable != null && variable.getName() != null) {
                scope.add(variable.getName());
            }
            Parameter index = statement.getIndexVariable();
            if (index != null && index.getName() != null) {
                scope.add(index.getName());
            }
            scopes.push(scope);
            super.visitForLoop(statement);
            scopes.pop();
        }

        @Override
        public void visitCatchStatement(CatchStatement statement) {
            Set<String> scope = new LinkedHashSet<>();
            if (statement.getVariable() != null && statement.getVariable().getName() != null) {
                scope.add(statement.getVariable().getName());
            }
            scopes.push(scope);
            super.visitCatchStatement(statement);
            scopes.pop();
        }

        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            Set<String> scope = scopes.peek();
            if (expression.isMultipleAssignmentDeclaration()) {
                for (Expression part : expression.getTupleExpression().getExpressions()) {
                    if (part instanceof VariableExpression variable && variable.getName() != null) {
                        scope.add(variable.getName());
                    }
                }
            } else if (expression.getVariableExpression().getName() != null) {
                scope.add(expression.getVariableExpression().getName());
            }
            super.visitDeclarationExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (call == target) {
                visible = new LinkedHashSet<>();
                for (Set<String> scope : scopes) {
                    visible.addAll(scope);
                }
                return;
            }
            super.visitMethodCallExpression(call);
        }
    }

}
