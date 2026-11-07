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

import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import groovy.transform.TypeCheckingMode;
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
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.lambdaX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Moves a GINQ query that is compiled inside a statically checked scope into a
 * dynamically compiled helper on the same class.
 * <p>
 * The {@code GQ} macro runs before static type checking and lowers the SQL-like
 * syntax to {@link Queryable} calls. Those calls use Groovy's dynamic object
 * protocol for property access, operators, and aggregate functions, so checking
 * the lowered tree as ordinary Java source fails. The helper keeps that runtime
 * behaviour and gives the call site a real return type ({@link Queryable}, or
 * {@code Object} for {@code shutdown}).
 * <p>
 * The helper is not called at the query expression. {@link LazyQueryable} calls it
 * the first time the query is read, so sources are not opened and grouping does
 * not run until then. Locals are copied when the expression is evaluated and that
 * copy is what the helper receives, matching a dynamic {@code GQ} expression.
 * Fields are read when the helper runs. {@code GQL} calls {@code toList}
 * immediately, so it still materializes at the expression.
 */
final class GinqStaticBridge {

    private static ClassNode rawQueryable() {
        // A fresh raw type. Reusing Queryable's generic ClassNode as a method
        // return type makes the compiler build a bad method descriptor.
        ClassNode type = makeWithoutCaching(Queryable.class, false).getPlainNodeReference();
        type.setGenericsTypes(null);
        return type;
    }
    private static final ClassNode COMPILE_STATIC_TYPE = make(CompileStatic.class);
    private static final ClassNode TYPE_CHECKED_TYPE = make(TypeChecked.class);
    private static final ClassNode TYPE_CHECKING_MODE_TYPE = make(TypeCheckingMode.class);
    private static final String SEQ = "ginqQuerySeq";
    private static final Map<CompilationUnit, List<Pending>> PENDING =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private GinqStaticBridge() {
    }

    /**
     * Returns {@code query} unchanged when the enclosing scope is dynamic, otherwise
     * a call to a new dynamically compiled method that evaluates {@code query}.
     * The method is attached after macro expansion finishes, because the macro
     * visitor is iterating the class's method list at this point.
     *
     * @param unit the compilation unit that owns {@code sourceUnit}
     * @param sourceUnit the source unit being compiled
     * @param call the original {@code GQ} / {@code GQL} macro call
     * @param query the lowered query expression
     * @return the expression that should replace the macro call
     */
    static Expression embed(CompilationUnit unit, SourceUnit sourceUnit, MethodCallExpression call, Expression query) {
        if (sourceUnit == null || sourceUnit.getAST() == null || call == null || query == null) {
            return query;
        }
        Host host = findHost(sourceUnit, call);
        if (host == null || !host.staticallyChecked()) {
            return query;
        }

        List<String> captured = capturedLocals(query, visibleLocals(host, call));
        Parameter[] parameters = new Parameter[captured.size()];
        Expression[] arguments = new Expression[captured.size()];
        for (int i = 0; i < captured.size(); i++) {
            String name = captured.get(i);
            parameters[i] = param(ClassHelper.dynamicType(), name);
            arguments[i] = varX(name);
        }

        ClassNode returnType = returnType(query);
        int modifiers = Opcodes.ACC_PRIVATE | (host.staticContext ? Opcodes.ACC_STATIC : 0);
        MethodNode helper = new MethodNode(nextName(host.owner), modifiers, returnType, parameters, ClassNode.EMPTY_ARRAY, block(returnS(query)));
        helper.setSourcePosition(call);
        helper.addAnnotation(skipAnnotation(COMPILE_STATIC_TYPE));
        // @TypeChecked looks at its own annotation, not @CompileStatic, when deciding to skip.
        helper.addAnnotation(skipAnnotation(TYPE_CHECKED_TYPE));
        schedule(unit, host.owner, helper);

        Expression receiver = host.staticContext ? classX(host.owner) : varX("this");
        if (!isQueryable(returnType)) {
            MethodCallExpression immediate = helperCall(receiver, helper.getName(), arguments);
            immediate.setSourcePosition(call);
            immediate.setType(returnType);
            return immediate;
        }
        // The array is evaluated now, so locals keep the values from this expression.
        // The lambda, and therefore the helper, runs on the first read of the query.
        ClassNode objectArray = ClassHelper.OBJECT_TYPE.makeArray();
        CastExpression snapshot = castX(objectArray, listX(Arrays.asList(arguments)));
        snapshot.setCoerce(true);
        Expression[] delayed = new Expression[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            delayed[i] = callX(varX("args"), "getAt", constX(i));
        }
        MethodCallExpression invocation = helperCall(receiver, helper.getName(), delayed);
        invocation.setType(returnType);
        LambdaExpression body = lambdaX(new Parameter[]{param(objectArray, "args")}, returnS(invocation));
        body.setSourcePosition(call);
        MethodCallExpression deferred = callX(classX(makeWithoutCaching(LazyQueryable.class, false)), "defer", args(snapshot, body));
        deferred.setImplicitThis(false);
        deferred.setSourcePosition(call);
        deferred.setType(returnType);
        return deferred;
    }

    private static MethodCallExpression helperCall(Expression receiver, String name, Expression[] arguments) {
        MethodCallExpression invocation = arguments.length == 0
                ? callX(receiver, name)
                : callX(receiver, name, args(arguments));
        invocation.setImplicitThis(false);
        return invocation;
    }

    private static boolean isQueryable(ClassNode type) {
        return type != null && Queryable.class.getName().equals(type.getName());
    }

    private static void schedule(CompilationUnit unit, ClassNode owner, MethodNode helper) {
        if (unit == null) {
            owner.addMethod(helper);
            return;
        }
        List<Pending> pending = PENDING.computeIfAbsent(unit, key -> {
            unit.addNewPhaseOperation(source -> {
                List<Pending> methods = PENDING.remove(unit);
                if (methods == null) return;
                for (Pending item : methods) {
                    item.owner.addMethod(item.method);
                }
            }, Phases.CONVERSION);
            return new ArrayList<>();
        });
        pending.add(new Pending(owner, helper));
    }

    private static final class Pending {
        private final ClassNode owner;
        private final MethodNode method;

        private Pending(ClassNode owner, MethodNode method) {
            this.owner = owner;
            this.method = method;
        }
    }

    private static AnnotationNode skipAnnotation(ClassNode annotationType) {
        AnnotationNode annotation = new AnnotationNode(annotationType);
        annotation.addMember("value", propX(classX(TYPE_CHECKING_MODE_TYPE), "SKIP"));
        return annotation;
    }

    private static ClassNode returnType(Expression query) {
        if (query instanceof MethodCallExpression call && "shutdown".equals(call.getMethodAsString())) {
            return ClassHelper.OBJECT_TYPE;
        }
        return rawQueryable();
    }

    private static String nextName(ClassNode owner) {
        Integer seq = (Integer) owner.getNodeMetaData(SEQ);
        int next = seq == null ? 0 : seq;
        String name;
        do {
            name = "ginqQuery$" + next++;
        } while (declares(owner, name));
        owner.putNodeMetaData(SEQ, next);
        return name;
    }

    private static boolean declares(ClassNode owner, String name) {
        for (MethodNode method : owner.getMethods()) {
            if (name.equals(method.getName()) && method.getDeclaringClass() == owner) {
                return true;
            }
        }
        return false;
    }

    private static List<String> capturedLocals(Expression query, Set<String> visible) {
        Set<String> declared = new LinkedHashSet<>();
        Set<String> referenced = new LinkedHashSet<>();
        query.visit(new CodeVisitorSupport() {
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                if (expression.getParameters() != null) {
                    for (Parameter parameter : expression.getParameters()) {
                        if (parameter.getName() != null) declared.add(parameter.getName());
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

    private static Set<String> visibleLocals(Host host, MethodCallExpression call) {
        Set<String> methodScope = new LinkedHashSet<>();
        if (host.executable != null) {
            for (Parameter parameter : host.executable.getParameters()) {
                if (parameter.getName() != null) methodScope.add(parameter.getName());
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

    private static Host findHost(SourceUnit sourceUnit, MethodCallExpression call) {
        for (ClassNode type : sourceUnit.getAST().getClasses()) {
            for (MethodNode method : new ArrayList<>(type.getMethods())) {
                if (method.getDeclaringClass() == type && contains(method.getCode(), call)) {
                    return new Host(type, method, method.getCode(), method.isStatic());
                }
            }
            for (MethodNode constructor : type.getDeclaredConstructors()) {
                if (contains(constructor.getCode(), call)) {
                    return new Host(type, constructor, constructor.getCode(), false);
                }
            }
            for (FieldNode field : type.getFields()) {
                if (field.getDeclaringClass() == type && contains(field.getInitialExpression(), call)) {
                    return new Host(type, null, field.getInitialExpression(), field.isStatic());
                }
            }
            for (Statement initializer : type.getObjectInitializerStatements()) {
                if (contains(initializer, call)) {
                    return new Host(type, null, initializer, false);
                }
            }
        }
        return null;
    }

    private static boolean contains(Statement statement, MethodCallExpression call) {
        if (statement == null) return false;
        Finder finder = new Finder(call);
        statement.visit(finder);
        return finder.found;
    }

    private static boolean contains(Expression expression, MethodCallExpression call) {
        if (expression == null) return false;
        Finder finder = new Finder(call);
        expression.visit(finder);
        return finder.found;
    }

    private static final class Host {
        private final ClassNode owner;
        private final MethodNode executable;
        private final ASTNode scopeRoot;
        private final boolean staticContext;

        private Host(ClassNode owner, MethodNode executable, ASTNode scopeRoot, boolean staticContext) {
            this.owner = owner;
            this.executable = executable;
            this.scopeRoot = scopeRoot;
            this.staticContext = staticContext;
        }

        private boolean staticallyChecked() {
            if (executable != null) {
                Boolean mode = explicitMode(executable);
                if (mode != null) return mode;
            }
            for (ClassNode current = owner; current != null; current = current.getOuterClass()) {
                Boolean mode = explicitMode(current);
                if (mode != null) return mode;
            }
            return false;
        }
    }

    /**
     * @return {@code TRUE} when the node asks for static checks, {@code FALSE} when it
     * explicitly skips them, or {@code null} when it does not say
     */
    private static Boolean explicitMode(AnnotatedNode node) {
        Boolean mode = null;
        for (AnnotationNode annotation : node.getAnnotations()) {
            String raw = annotation.getClassNode().getName();
            int dot = raw.lastIndexOf('.');
            String simple = dot < 0 ? raw : raw.substring(dot + 1);
            if ("CompileDynamic".equals(simple)) {
                return Boolean.FALSE;
            }
            if (!"CompileStatic".equals(simple) && !"TypeChecked".equals(simple)) {
                continue;
            }
            if (isSkip(annotation)) {
                return Boolean.FALSE;
            }
            mode = Boolean.TRUE;
        }
        return mode;
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
     * Locals, parameters, and closure parameters in scope at the query.
     * Sibling closures are not visible. A local declared after the query is not captured.
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
                    if (parameter.getName() != null) scope.add(parameter.getName());
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
