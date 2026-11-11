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
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
 * The macro runs at conversion, before either checker. This class does not type-check
 * or emit bytecode. It asks the same question {@code StaticTypeCheckingVisitor.isSkipMode}
 * asks, registers {@link GinqRowChecks} on the annotation that visitor will read, and
 * marks the lowered closures so the extension can see them. The lowered query closure
 * stays on the static writer. A call the checker selects is emitted as that target.
 * A comparison or arithmetic operator the checker can prove stays an operator, so
 * a number compared with a number is the static comparison. The enclosing method
 * stays static.
 * <p>
 * A {@link Queryable} result is handed to {@link LazyQueryable#defer}. Read-only locals
 * are copied when the expression runs. Which names those are comes from
 * {@link VariableScopeVisitor}, not from a walk of declared names.
 */
@Internal
public final class GinqStaticBridge {

    private static final String ROW_CHECKS = "org.apache.groovy.ginq.GinqRowChecks";
    private static final String HOLDER = "ginqShell$";
    private static final String CAPTURED = "ginqCaptured$";
    private static final String COMPILE_STATIC = "CompileStatic";
    private static final String TYPE_CHECKED = "TypeChecked";
    private static final String COMPILE_DYNAMIC = "CompileDynamic";

    private GinqStaticBridge() {
    }

    /**
     * Adapts a macro result. A scope the type checker will not visit is returned unchanged.
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
        attachRowChecks(sourceUnit, host.owner, host.executable);
        boolean defer = returnsQueryable(query);
        List<Variable> readOnly = defer ? capture(sourceUnit, host.owner, call, query) : List.of();
        return wrap(query, call, defer, readOnly);
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
        attachRowChecks(sourceUnit, method.getDeclaringClass(), method);
        boolean deferQuery = defer && returnsQueryable(query);
        List<Variable> readOnly = deferQuery ? captureMethod(sourceUnit, method, query) : List.of();
        return wrap(query, method, deferQuery, readOnly);
    }

    private static Expression wrap(Expression query, ASTNode position, boolean defer, List<Variable> readOnly) {
        boolean queryable = returnsQueryable(query);
        Expression body = readOnly.isEmpty() ? query : rewriteReadOnly(query, readOnly);
        ClosureExpression shell = closureX(Parameter.EMPTY_ARRAY, returnS(body));
        shell.setSourcePosition(position);
        // The closure stays on the static writer. Row checks use the QUERY mark.
        // A Boolean.TRUE DYNAMIC_RESOLUTION on this closure would compile every
        // call inside it dynamically, including one another extension selected.
        markQuery(shell);
        if (!defer) {
            MethodCallExpression call = callX(shell, "call");
            call.setImplicitThis(false);
            call.setSourcePosition(position);
            return queryable ? castToQueryable(call) : call;
        }

        List<Statement> statements = new ArrayList<>();
        if (!readOnly.isEmpty()) {
            List<Expression> elements = new ArrayList<>();
            for (Variable variable : readOnly) {
                elements.add(varX(variable));
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

    private static void markQuery(Expression expression) {
        expression.visit(new CodeVisitorSupport() {
            @Override
            public void visitClosureExpression(ClosureExpression closure) {
                closure.putNodeMetaData(GinqRowChecks.QUERY, Boolean.TRUE);
                super.visitClosureExpression(closure);
            }
        });
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

    // -----------------------------------------------------------------------
    // Checking mode. Mirrors StaticTypeCheckingVisitor.isSkipMode.
    // @CompileDynamic and @CompileStatic(SKIP) skip the static-compilation
    // visitor only. A @TypeChecked visitor does not look at those annotations,
    // so a method that carries them is still type-checked when the class is
    // @TypeChecked. @TypeChecked(SKIP) skips both visitors.
    // -----------------------------------------------------------------------

    private static boolean checked(MethodNode method) {
        if (method == null) {
            return false;
        }
        ClassNode owner = method.getDeclaringClass();
        if (staticVisitorReaches(owner) && !isSkipMode(method, true)) {
            return true;
        }
        if (typeCheckedVisitorReaches(owner) && !isSkipMode(method, false)) {
            return true;
        }
        return methodChecksItself(method);
    }

    private static boolean checkedClass(ClassNode type) {
        return staticVisitorReaches(type) || typeCheckedVisitorReaches(type);
    }

    private static boolean methodChecksItself(MethodNode method) {
        if (hasAnnotation(method, COMPILE_DYNAMIC) || isSkipAnnotation(method, COMPILE_STATIC)) {
            return false;
        }
        if (isSkipAnnotation(method, TYPE_CHECKED)) {
            return false;
        }
        return hasAnnotation(method, COMPILE_STATIC) || hasAnnotation(method, TYPE_CHECKED);
    }

    /**
     * A non-skip {@code @CompileStatic} on {@code type} or an outer class, whose visitor
     * is not stopped by skip mode on the way in.
     */
    private static boolean staticVisitorReaches(ClassNode type) {
        for (ClassNode current = type; current != null; current = current.getOuterClass()) {
            if (winning(current) == Winner.COMPILE_STATIC && !skippedAlong(current, type, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A non-skip {@code @TypeChecked} that is the annotation the collector will run.
     */
    private static boolean typeCheckedVisitorReaches(ClassNode type) {
        for (ClassNode current = type; current != null; current = current.getOuterClass()) {
            if (winning(current) == Winner.TYPE_CHECKED && !skippedAlong(current, type, false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean skippedAlong(ClassNode start, ClassNode type, boolean staticVisitor) {
        for (ClassNode current = type; current != null; current = current.getOuterClass()) {
            if (isSkipMode(current, staticVisitor)) {
                return true;
            }
            if (current == start) {
                return false;
            }
        }
        return false;
    }

    /**
     * Same priority {@code ASTTransformationVisitor.distinctAnnotations} uses:
     * {@code @CompileDynamic} and {@code @CompileStatic(SKIP)} outrank
     * {@code @CompileStatic}, which outranks {@code @TypeChecked}.
     */
    private static Winner winning(AnnotatedNode node) {
        if (hasAnnotation(node, COMPILE_DYNAMIC) || isSkipAnnotation(node, COMPILE_STATIC)) {
            return Winner.SKIP_STATIC;
        }
        if (hasAnnotation(node, COMPILE_STATIC)) {
            return Winner.COMPILE_STATIC;
        }
        if (isSkipAnnotation(node, TYPE_CHECKED)) {
            return Winner.SKIP_TYPE;
        }
        if (hasAnnotation(node, TYPE_CHECKED)) {
            return Winner.TYPE_CHECKED;
        }
        return Winner.NONE;
    }

    /**
     * {@code StaticTypeCheckingVisitor.isSkipMode}, with {@code staticVisitor} selecting
     * the annotation set. The static-compilation visitor sees {@code @TypeChecked} and
     * {@code @CompileStatic}. The type-checked visitor sees only {@code @TypeChecked}.
     * A skip value wins over another annotation on the same node, which is what
     * {@code isSkipMode} does.
     */
    private static boolean isSkipMode(AnnotatedNode node, boolean staticVisitor) {
        if (node == null) {
            return false;
        }
        boolean explicit = false;
        if (hasAnnotation(node, TYPE_CHECKED)) {
            if (isSkipAnnotation(node, TYPE_CHECKED)) {
                return true;
            }
            explicit = true;
        }
        if (staticVisitor && hasAnnotation(node, COMPILE_STATIC)) {
            if (isSkipAnnotation(node, COMPILE_STATIC)) {
                return true;
            }
            explicit = true;
        }
        if (staticVisitor && hasAnnotation(node, COMPILE_DYNAMIC)) {
            return true;
        }
        if (explicit) {
            return false;
        }
        if (node instanceof MethodNode method) {
            return isSkipMode(method.getDeclaringClass(), staticVisitor);
        }
        if (node instanceof ClassNode type && type.getOuterClass() != null) {
            MethodNode enclosing = type.getEnclosingMethod();
            if (enclosing != null && isSkipMode(enclosing, staticVisitor)) {
                return true;
            }
        }
        return false;
    }

    private enum Winner {
        NONE, TYPE_CHECKED, COMPILE_STATIC, SKIP_TYPE, SKIP_STATIC
    }

    private static void attachRowChecks(SourceUnit source, ClassNode owner, MethodNode executable) {
        if (executable != null && methodChecksItself(executable)) {
            addToActiveAnnotations(source, executable);
        }
        for (ClassNode current = owner; current != null; current = current.getOuterClass()) {
            Winner winner = winning(current);
            if (winner == Winner.COMPILE_STATIC || winner == Winner.TYPE_CHECKED) {
                addToActiveAnnotations(source, current);
                return;
            }
        }
    }

    private static void addToActiveAnnotations(SourceUnit source, AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            String simple = simpleName(annotation.getClassNode());
            if ((COMPILE_STATIC.equals(simple) || TYPE_CHECKED.equals(simple)) && !isSkip(annotation)) {
                addExtension(source, annotation);
            }
        }
    }

    /**
     * Appends {@link GinqRowChecks} to an {@code extensions} member.
     * A string, a class literal, or a list or array of those is updated.
     * Any other shape is reported and left unchanged.
     *
     * @param source the source unit that receives the error when the member cannot be updated
     * @param annotation the checking annotation
     */
    static void addExtension(SourceUnit source, AnnotationNode annotation) {
        Expression existing = annotation.getMember("extensions");
        if (existing == null) {
            annotation.setMember("extensions", constX(ROW_CHECKS));
            return;
        }
        List<Expression> items = extensionItems(existing);
        if (items == null) {
            if (source != null) {
                source.addErrorAndContinue(new SyntaxException(
                        "Cannot add the GINQ type-checking extension because the extensions member is not a string, a class, or a list of those",
                        annotation));
            }
            return;
        }
        for (Expression item : items) {
            if (ROW_CHECKS.equals(extensionName(item))) {
                return;
            }
        }
        List<Expression> copy = new ArrayList<>();
        for (Expression item : items) {
            copy.add(normalizeExtension(item));
        }
        copy.add(constX(ROW_CHECKS));
        annotation.setMember("extensions", new ListExpression(copy));
    }

    private static List<Expression> extensionItems(Expression existing) {
        if (existing instanceof ConstantExpression || existing instanceof ClassExpression) {
            return List.of(existing);
        }
        if (existing instanceof ListExpression list) {
            return supported(list.getExpressions()) ? list.getExpressions() : null;
        }
        if (existing instanceof ArrayExpression array) {
            return supported(array.getExpressions()) ? array.getExpressions() : null;
        }
        return null;
    }

    private static boolean supported(List<Expression> expressions) {
        for (Expression expression : expressions) {
            if (!(expression instanceof ConstantExpression || expression instanceof ClassExpression)) {
                return false;
            }
        }
        return true;
    }

    private static Expression normalizeExtension(Expression item) {
        if (item instanceof ClassExpression type) {
            return constX(type.getType().getName());
        }
        return item;
    }

    private static String extensionName(Expression item) {
        if (item instanceof ConstantExpression constant) {
            return String.valueOf(constant.getValue());
        }
        if (item instanceof ClassExpression type) {
            return type.getType().getName();
        }
        return null;
    }

    private static boolean hasAnnotation(AnnotatedNode node, String simple) {
        if (node == null) {
            return false;
        }
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (simple.equals(simpleName(annotation.getClassNode()))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSkipAnnotation(AnnotatedNode node, String simple) {
        if (node == null) {
            return false;
        }
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (simple.equals(simpleName(annotation.getClassNode())) && isSkip(annotation)) {
                return true;
            }
        }
        return false;
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

    // -----------------------------------------------------------------------
    // Captured locals. VariableScopeVisitor links each reference to the
    // declaration that is actually in scope, so a shadowing local, a closure
    // parameter, and a class name (a dynamic variable, or a field) are not
    // mistaken for the enclosing local.
    // -----------------------------------------------------------------------

    private static List<Variable> capture(SourceUnit source, ClassNode owner, Expression call, Expression query) {
        boolean swapped = SlotReplacer.swap(owner, call, query);
        try {
            if (swapped) {
                new SilentScopeVisitor(source).visitClass(owner);
            }
        } finally {
            if (swapped) {
                SlotReplacer.swap(owner, query, call);
            }
        }
        return swapped ? readOnlyCaptured(query) : List.of();
    }

    private static List<Variable> captureMethod(SourceUnit source, MethodNode method, Expression query) {
        Statement original = method.getCode();
        method.setCode(returnS(query));
        try {
            new SilentScopeVisitor(source).visitClass(method.getDeclaringClass());
        } finally {
            method.setCode(original);
        }
        return readOnlyCaptured(query);
    }

    private static List<Variable> readOnlyCaptured(Expression query) {
        List<Variable> captured = new ArrayList<>();
        query.visit(new CodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression expression) {
                Variable accessed = expression.getAccessedVariable();
                if (outerLocal(accessed, query) && !containsSame(captured, accessed)) {
                    captured.add(accessed);
                }
            }
        });
        List<Variable> mutated = mutated(query, captured);
        List<Variable> readOnly = new ArrayList<>();
        for (Variable variable : captured) {
            if (!containsSame(mutated, variable)) {
                readOnly.add(variable);
            }
        }
        return readOnly;
    }

    private static boolean outerLocal(Variable variable, Expression query) {
        if (variable == null || variable instanceof DynamicVariable || variable instanceof FieldNode || variable instanceof PropertyNode) {
            return false;
        }
        if (!(variable instanceof Parameter) && !(variable instanceof VariableExpression)) {
            return false;
        }
        return !containsNode(query, (ASTNode) variable);
    }

    private static List<Variable> mutated(Expression query, List<Variable> captured) {
        List<Variable> mutated = new ArrayList<>();
        query.visit(new CodeVisitorSupport() {
            private void consider(Expression expression) {
                if (expression instanceof VariableExpression variable && containsSame(captured, variable.getAccessedVariable())) {
                    if (!containsSame(mutated, variable.getAccessedVariable())) {
                        mutated.add(variable.getAccessedVariable());
                    }
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

    private static Expression rewriteReadOnly(Expression query, List<Variable> readOnly) {
        return new ClassCodeExpressionTransformer() {
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
                if (expression instanceof VariableExpression variable) {
                    int index = indexOf(readOnly, variable.getAccessedVariable());
                    if (index >= 0) {
                        Expression read = indexX(varX(CAPTURED, ClassHelper.OBJECT_TYPE.makeArray()), constX(index));
                        ClassNode type = variable.getAccessedVariable().getOriginType();
                        if (type != null && !ClassHelper.isDynamicTyped(type) && !ClassHelper.OBJECT_TYPE.equals(type)) {
                            CastExpression cast = castX(type, read);
                            cast.setSourcePosition(variable);
                            return cast;
                        }
                        read.setSourcePosition(variable);
                        return read;
                    }
                }
                return super.transform(expression);
            }
        }.transform(query);
    }

    private static boolean containsSame(List<Variable> variables, Variable target) {
        return indexOf(variables, target) >= 0;
    }

    private static int indexOf(List<Variable> variables, Variable target) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private static boolean containsNode(Expression root, ASTNode target) {
        if (root == target) {
            return true;
        }
        final boolean[] found = {false};
        root.visit(new CodeVisitorSupport() {
            private void hit(ASTNode node) {
                if (node == target) {
                    found[0] = true;
                }
            }

            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                hit(expression);
                if (expression.getParameters() != null) {
                    for (Parameter parameter : expression.getParameters()) {
                        hit(parameter);
                    }
                }
                if (!found[0]) {
                    super.visitClosureExpression(expression);
                }
            }

            @Override
            public void visitVariableExpression(VariableExpression expression) {
                hit(expression);
            }
        });
        return found[0];
    }

    private static final class SilentScopeVisitor extends VariableScopeVisitor {
        private SilentScopeVisitor(SourceUnit source) {
            super(source);
        }

        @Override
        public void addError(String msg, ASTNode node) {
            // Linking only. The ordinary scope visitor reports errors later.
        }
    }

    /**
     * Replaces one expression in a class without copying its neighbours.
     * A copying transformer would detach the call the macro visitor is updating.
     */
    private static final class SlotReplacer extends ClassCodeVisitorSupport {
        private final Expression from;
        private final Expression to;
        private boolean done;

        private SlotReplacer(Expression from, Expression to) {
            this.from = from;
            this.to = to;
        }

        private static boolean swap(ClassNode owner, Expression from, Expression to) {
            SlotReplacer replacer = new SlotReplacer(from, to);
            replacer.visitClass(owner);
            return replacer.done;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private boolean replace(ASTNode node) {
            if (done || node == null) {
                return false;
            }
            if (node instanceof TupleExpression tuple) {
                List<Expression> expressions = tuple.getExpressions();
                for (int i = 0; i < expressions.size(); i++) {
                    if (expressions.get(i) == from) {
                        expressions.set(i, to);
                        done = true;
                        return true;
                    }
                }
            }
            for (Method getter : node.getClass().getMethods()) {
                if (getter.getParameterCount() != 0 || !Expression.class.isAssignableFrom(getter.getReturnType())) {
                    continue;
                }
                if (!getter.getName().startsWith("get") && !getter.getName().startsWith("is")) {
                    continue;
                }
                try {
                    if (getter.invoke(node) != from) {
                        continue;
                    }
                    String suffix = getter.getName().startsWith("get") ? getter.getName().substring(3) : getter.getName().substring(2);
                    Method setter = node.getClass().getMethod("set" + suffix, getter.getReturnType());
                    setter.invoke(node, to);
                    done = true;
                    return true;
                } catch (ReflectiveOperationException ignored) {
                    // this getter has no matching setter
                }
            }
            return false;
        }

        @Override
        public void visitField(FieldNode node) {
            if (!done && node.getInitialExpression() == from) {
                node.setInitialValueExpression(to);
                done = true;
                return;
            }
            super.visitField(node);
        }

        @Override
        public void visitProperty(PropertyNode node) {
            if (replace(node)) {
                return;
            }
            super.visitProperty(node);
        }

        @Override
        protected void visitStatement(Statement statement) {
            if (replace(statement)) {
                return;
            }
            super.visitStatement(statement);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (replace(call)) {
                return;
            }
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            if (replace(expression)) {
                return;
            }
            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            if (replace(expression)) {
                return;
            }
            super.visitDeclarationExpression(expression);
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            if (replace(expression)) {
                return;
            }
            super.visitClosureExpression(expression);
        }

        @Override
        public void visitPropertyExpression(PropertyExpression expression) {
            if (replace(expression)) {
                return;
            }
            super.visitPropertyExpression(expression);
        }

        @Override
        public void visitTupleExpression(TupleExpression expression) {
            if (replace(expression)) {
                return;
            }
            super.visitTupleExpression(expression);
        }
    }

    private static Host findHost(SourceUnit sourceUnit, MethodCallExpression call) {
        for (ClassNode type : sourceUnit.getAST().getClasses()) {
            for (MethodNode method : new ArrayList<>(type.getMethods())) {
                if (method.getDeclaringClass() == type && contains(method.getCode(), call)) {
                    return new Host(type, method);
                }
            }
            for (MethodNode constructor : type.getDeclaredConstructors()) {
                if (contains(constructor.getCode(), call)) {
                    return new Host(type, constructor);
                }
            }
            for (FieldNode field : type.getFields()) {
                if (field.getDeclaringClass() == type && contains(field.getInitialExpression(), call)) {
                    return new Host(type, null);
                }
            }
            for (Statement initializer : type.getObjectInitializerStatements()) {
                if (contains(initializer, call)) {
                    return new Host(type, null);
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

    private static final class Host {
        private final ClassNode owner;
        private final MethodNode executable;

        private Host(ClassNode owner, MethodNode executable) {
            this.owner = owner;
            this.executable = executable;
        }

        private boolean checked() {
            if (executable != null) {
                return GinqStaticBridge.checked(executable);
            }
            return checkedClass(owner);
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
}
