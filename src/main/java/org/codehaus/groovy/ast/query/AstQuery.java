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
package org.codehaus.groovy.ast.query;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A small, read-only, fluent query API over a Groovy AST subtree.
 *
 * <p>An {@code AstQuery} selects descendant nodes of a root, optionally filtered by type and by an
 * arbitrary predicate, and exposes terminal operations that collect or test the matches. It is a
 * declarative alternative to hand-writing a {@link ClassCodeVisitorSupport} subclass for the common
 * "find / collect / detect" cases.
 *
 * <pre class="groovyTestCase">
 * import org.codehaus.groovy.ast.query.AstQuery
 * import org.codehaus.groovy.ast.expr.MethodCallExpression
 * import org.codehaus.groovy.ast.builder.AstBuilder
 *
 * def code = new AstBuilder().buildFromString('foo(); bar()')[0]
 * assert AstQuery.from(code).descendants(MethodCallExpression).count() == 2
 * </pre>
 *
 * <h2>Traversal and pruning</h2>
 * By default the traversal descends through closures but does <em>not</em> descend into nested
 * classes, mirroring {@link ClassCodeVisitorSupport}. Use {@link #notInto(Class[])} to stop
 * descending at a boundary type (for example {@code ClosureExpression}) and {@link #into(Class[])}
 * to opt back in past a default boundary. Nested classes are currently the only default boundary,
 * so {@code into(ClassNode.class)} is the only case in which {@link #into(Class[])} has an effect.
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>The query is read-only; it never mutates the AST.</li>
 *   <li>Matches are produced in document (pre-)order.</li>
 *   <li>Instances are immutable and re-runnable: each builder method returns a new query and each
 *       terminal performs a fresh traversal, short-circuiting where it can (for example
 *       {@link #any()} and {@link #findFirst()}).</li>
 * </ul>
 *
 * @param <T> the node type produced by this query
 * @since 6.0.0
 */
public final class AstQuery<T extends ASTNode> {

    private final ASTNode root;
    private final List<Class<? extends ASTNode>> types;          // empty => any type
    private final Predicate<ASTNode> predicate;                  // applied after the type filter
    private final BiPredicate<ASTNode, AstContext> ctxPredicate; // nullable; applied last
    private final Set<Class<? extends ASTNode>> notInto;         // extra pruning boundaries
    private final Set<Class<? extends ASTNode>> into;            // opt-in past default boundaries
    private final boolean includeSelf;

    private AstQuery(ASTNode root, List<Class<? extends ASTNode>> types, Predicate<ASTNode> predicate,
                     BiPredicate<ASTNode, AstContext> ctxPredicate, Set<Class<? extends ASTNode>> notInto,
                     Set<Class<? extends ASTNode>> into, boolean includeSelf) {
        this.root = root;
        this.types = types;
        this.predicate = predicate;
        this.ctxPredicate = ctxPredicate;
        this.notInto = notInto;
        this.into = into;
        this.includeSelf = includeSelf;
    }

    /**
     * Starts a query rooted at the given node.
     *
     * @param root the subtree root to query; must not be {@code null}
     * @return a query selecting (by default) all descendants of {@code root}
     */
    public static AstQuery<ASTNode> from(final ASTNode root) {
        Objects.requireNonNull(root, "root");
        return new AstQuery<>(root, List.of(), n -> true, null, Set.of(), Set.of(), false);
    }

    private <U extends ASTNode> AstQuery<U> with(List<Class<? extends ASTNode>> t, Predicate<ASTNode> p,
                                                 BiPredicate<ASTNode, AstContext> cp, Set<Class<? extends ASTNode>> ni,
                                                 Set<Class<? extends ASTNode>> in, boolean self) {
        return new AstQuery<>(root, t, p, cp, ni, in, self);
    }

    // ---- selection -------------------------------------------------------------------------------

    /**
     * Selects all descendant nodes regardless of type.
     *
     * @return a query over every descendant
     */
    public AstQuery<ASTNode> descendants() {
        return with(List.of(), predicate, ctxPredicate, notInto, into, includeSelf);
    }

    /**
     * Selects descendant nodes that are instances of the given type.
     *
     * @param type the node type to match
     * @param <U>  the node type
     * @return a query over descendants of that type
     */
    public <U extends ASTNode> AstQuery<U> descendants(final Class<U> type) {
        Objects.requireNonNull(type, "type");
        return with(List.of(type), predicate, ctxPredicate, notInto, into, includeSelf);
    }

    /**
     * Selects descendant nodes that are instances of any of the given types.
     *
     * @param types the node types to match (logical OR)
     * @return a query over descendants matching any type
     */
    @SafeVarargs
    public final AstQuery<ASTNode> descendants(final Class<? extends ASTNode>... types) {
        return with(List.copyOf(Arrays.asList(types)), predicate, ctxPredicate, notInto, into, includeSelf);
    }

    /**
     * Includes the root node itself as a candidate (it is excluded by default).
     *
     * @return a query that also considers the root
     */
    public AstQuery<T> andSelf() {
        return with(types, predicate, ctxPredicate, notInto, into, true);
    }

    // ---- refinement ------------------------------------------------------------------------------

    /**
     * Restricts the selection to nodes satisfying the predicate. Multiple {@code where} calls are
     * combined with logical AND.
     *
     * @param p the predicate (a Groovy {@code Closure} coerces to this functional type)
     * @return the refined query
     */
    @SuppressWarnings("unchecked")
    public AstQuery<T> where(final Predicate<? super T> p) {
        Objects.requireNonNull(p, "predicate");
        Predicate<ASTNode> np = predicate.and(n -> p.test((T) n));
        return with(types, np, ctxPredicate, notInto, into, includeSelf);
    }

    /**
     * Restricts the selection to nodes satisfying a predicate that also inspects the enclosing
     * {@link AstContext}. Combined with logical AND with any other refinements.
     *
     * @param p the contextual predicate (a two-argument Groovy {@code Closure} coerces to it)
     * @return the refined query
     */
    @SuppressWarnings("unchecked")
    public AstQuery<T> where(final BiPredicate<? super T, AstContext> p) {
        Objects.requireNonNull(p, "predicate");
        BiPredicate<ASTNode, AstContext> prev = ctxPredicate;
        BiPredicate<ASTNode, AstContext> np = (n, c) -> (prev == null || prev.test(n, c)) && p.test((T) n, c);
        return with(types, predicate, np, notInto, into, includeSelf);
    }

    // ---- pruning ---------------------------------------------------------------------------------

    /**
     * Stops the traversal from descending into the subtrees rooted at nodes of the given types. The
     * boundary nodes themselves are still candidates; only their contents are skipped.
     *
     * @param boundary the boundary types (for example {@code ClosureExpression.class})
     * @return the query with the added boundaries
     */
    @SafeVarargs
    public final AstQuery<T> notInto(final Class<? extends ASTNode>... boundary) {
        Set<Class<? extends ASTNode>> ni = new LinkedHashSet<>(notInto);
        ni.addAll(Arrays.asList(boundary));
        return with(types, predicate, ctxPredicate, ni, into, includeSelf);
    }

    /**
     * Opts the traversal in to descending past a default boundary. Currently the only default
     * boundary is nested classes, so {@code into(ClassNode.class)} makes the traversal recurse into
     * inner classes.
     *
     * @param types the boundary types to descend into
     * @return the query with the boundaries opened
     */
    @SafeVarargs
    public final AstQuery<T> into(final Class<? extends ASTNode>... types) {
        Set<Class<? extends ASTNode>> in = new LinkedHashSet<>(into);
        in.addAll(Arrays.asList(types));
        return with(this.types, predicate, ctxPredicate, notInto, in, includeSelf);
    }

    // ---- terminals -------------------------------------------------------------------------------

    /**
     * Collects all matches in document order.
     *
     * @return the matching nodes
     */
    public List<T> list() {
        return run(false);
    }

    /**
     * @return {@code true} if at least one node matches; stops at the first match
     */
    public boolean any() {
        return !run(true).isEmpty();
    }

    /**
     * @return {@code true} if no node matches; stops at the first match
     */
    public boolean none() {
        return run(true).isEmpty();
    }

    /**
     * @return the number of matching nodes
     */
    public long count() {
        return run(false).size();
    }

    /**
     * @return the first match in document order, if any; stops at the first match
     */
    public Optional<T> findFirst() {
        List<T> hits = run(true);
        return hits.isEmpty() ? Optional.empty() : Optional.of(hits.get(0));
    }

    /**
     * @return the first match in document order, or {@code null}; stops at the first match
     */
    public T first() {
        return findFirst().orElse(null);
    }

    /**
     * Performs the given action for each match in document order.
     *
     * @param action the action
     */
    public void forEach(final Consumer<? super T> action) {
        run(false).forEach(action);
    }

    /**
     * Performs the given action, with enclosing context, for each match in document order.
     *
     * @param action the action receiving the node and its {@link AstContext}
     */
    public void forEach(final BiConsumer<? super T, AstContext> action) {
        QueryVisitor v = new QueryVisitor(false, action);
        try {
            v.start(root);
        } catch (Stop ignored) {
            // not reachable for a non short-circuit run
        }
    }

    /**
     * @return a sequential {@link Stream} of the matches in document order
     */
    public Stream<T> stream() {
        return run(false).stream();
    }

    @SuppressWarnings("unchecked")
    private List<T> run(final boolean shortCircuit) {
        QueryVisitor v = new QueryVisitor(shortCircuit, null);
        try {
            v.start(root);
        } catch (Stop ignored) {
            // short-circuit terminal reached its first match
        }
        return (List<T>) v.hits;
    }

    /** Control-flow signal used to abort the traversal for short-circuiting terminals. */
    private static final class Stop extends RuntimeException {
        static final Stop INSTANCE = new Stop();
        private Stop() {
            super(null, null, false, false);
        }
    }

    /**
     * Walks the subtree applying the configured type filter, predicate and pruning. The visitor
     * overrides only the "sink" visit methods of {@link ClassCodeVisitorSupport}; the delegating
     * wrappers ({@code visitArgumentlistExpression}, {@code visitLambdaExpression},
     * {@code visitShortTernaryExpression}, {@code visitMethodReferenceExpression},
     * {@code visitDeclarationExpression}, {@code visitMethod}/{@code visitConstructor}) funnel into
     * those sinks, so every node is tested exactly once.
     */
    private final class QueryVisitor extends ClassCodeVisitorSupport {
        final boolean shortCircuit;
        final BiConsumer<? super T, AstContext> sink; // when non-null, stream-and-consume mode
        final List<ASTNode> hits = new ArrayList<>();
        private final Deque<ASTNode> stack = new ArrayDeque<>();

        QueryVisitor(final boolean shortCircuit, final BiConsumer<? super T, AstContext> sink) {
            this.shortCircuit = shortCircuit;
            this.sink = sink;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null; // queries are source-independent and never report errors
        }

        void start(final ASTNode node) {
            if (node instanceof ClassNode) {
                visitClass((ClassNode) node);
            } else if (node instanceof MethodNode) {
                visitMethod((MethodNode) node);
            } else if (node instanceof FieldNode) {
                visitField((FieldNode) node);
            } else if (node instanceof PropertyNode) {
                visitProperty((PropertyNode) node);
            } else if (node instanceof ModuleNode) {
                ModuleNode module = (ModuleNode) node;
                for (ClassNode cn : module.getClasses()) {
                    if (!(cn instanceof InnerClassNode)) visitClass(cn);
                }
                if (module.getStatementBlock() != null) module.getStatementBlock().visit(this);
            } else if (node instanceof Statement) {
                ((Statement) node).visit(this);
            } else if (node instanceof Expression) {
                ((Expression) node).visit(this);
            }
        }

        private AstContext context() {
            final Deque<ASTNode> snapshot = stack;
            return new AstContext() {
                @Override public ASTNode parent() { return snapshot.peek(); }
                @Override public List<ASTNode> ancestors() { return List.copyOf(snapshot); }
                @Override public MethodNode enclosingMethod() {
                    for (ASTNode a : snapshot) if (a instanceof MethodNode) return (MethodNode) a;
                    return null;
                }
                @Override public ClassNode enclosingClass() {
                    for (ASTNode a : snapshot) if (a instanceof ClassNode) return (ClassNode) a;
                    return null;
                }
            };
        }

        private boolean matchesType(final ASTNode n) {
            if (types.isEmpty()) return true;
            for (Class<? extends ASTNode> c : types) if (c.isInstance(n)) return true;
            return false;
        }

        private boolean descendInto(final ASTNode n) {
            for (Class<? extends ASTNode> c : notInto) if (c.isInstance(n)) return false;
            return true;
        }

        @SuppressWarnings("unchecked")
        private void test(final ASTNode n) {
            if (n == root && !includeSelf) return;
            if (!matchesType(n)) return;
            if (!predicate.test(n)) return;
            if (ctxPredicate != null && !ctxPredicate.test(n, context())) return;
            if (sink != null) {
                sink.accept((T) n, context());
            } else {
                hits.add(n);
                if (shortCircuit) throw Stop.INSTANCE;
            }
        }

        /** Tests {@code n} then, unless pruned, descends into it while tracking context. */
        private void enter(final ASTNode n, final Runnable descend) {
            test(n);
            if (descendInto(n)) {
                stack.push(n);
                descend.run();
                stack.pop();
            }
        }

        // ---- structural nodes ----

        @Override
        public void visitClass(final ClassNode node) {
            test(node);
            if (descendInto(node)) {
                stack.push(node);
                super.visitClass(node);
                if (into.contains(ClassNode.class)) {
                    for (Iterator<InnerClassNode> it = node.getInnerClasses(); it.hasNext(); ) {
                        visitClass(it.next());
                    }
                }
                stack.pop();
            }
        }

        @Override
        protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
            enter(node, () -> super.visitConstructorOrMethod(node, isConstructor));
        }

        @Override
        public void visitField(final FieldNode node) {
            enter(node, () -> super.visitField(node));
        }

        @Override
        public void visitProperty(final PropertyNode node) {
            enter(node, () -> super.visitProperty(node));
        }

        // ---- statements ----

        @Override public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement s) { enter(s, () -> super.visitBlockStatement(s)); }
        @Override public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement s) { enter(s, () -> super.visitForLoop(s)); }
        @Override public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement s) { enter(s, () -> super.visitWhileLoop(s)); }
        @Override public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement s) { enter(s, () -> super.visitDoWhileLoop(s)); }
        @Override public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement s) { enter(s, () -> super.visitIfElse(s)); }
        @Override public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement s) { enter(s, () -> super.visitExpressionStatement(s)); }
        @Override public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement s) { enter(s, () -> super.visitReturnStatement(s)); }
        @Override public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement s) { enter(s, () -> super.visitAssertStatement(s)); }
        @Override public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement s) { enter(s, () -> super.visitTryCatchFinally(s)); }
        @Override public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement s) { enter(s, () -> super.visitSwitch(s)); }
        @Override public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement s) { enter(s, () -> super.visitCaseStatement(s)); }
        @Override public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement s) { enter(s, () -> super.visitBreakStatement(s)); }
        @Override public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement s) { enter(s, () -> super.visitContinueStatement(s)); }
        @Override public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement s) { enter(s, () -> super.visitThrowStatement(s)); }
        @Override public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement s) { enter(s, () -> super.visitSynchronizedStatement(s)); }
        @Override public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement s) { enter(s, () -> super.visitCatchStatement(s)); }
        @Override public void visitEmptyStatement(org.codehaus.groovy.ast.stmt.EmptyStatement s) { enter(s, () -> super.visitEmptyStatement(s)); }

        // ---- expressions (sinks only; wrappers funnel into these) ----

        @Override public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression e) { enter(e, () -> super.visitMethodCallExpression(e)); }
        @Override public void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression e) { enter(e, () -> super.visitStaticMethodCallExpression(e)); }
        @Override public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression e) { enter(e, () -> super.visitConstructorCallExpression(e)); }
        @Override public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression e) { enter(e, () -> super.visitBinaryExpression(e)); }
        @Override public void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression e) { enter(e, () -> super.visitTernaryExpression(e)); }
        @Override public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression e) { enter(e, () -> super.visitPrefixExpression(e)); }
        @Override public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression e) { enter(e, () -> super.visitPostfixExpression(e)); }
        @Override public void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression e) { enter(e, () -> super.visitBooleanExpression(e)); }
        @Override public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression e) { enter(e, () -> super.visitClosureExpression(e)); }
        @Override public void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression e) { enter(e, () -> super.visitTupleExpression(e)); }
        @Override public void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression e) { enter(e, () -> super.visitListExpression(e)); }
        @Override public void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression e) { enter(e, () -> super.visitMapExpression(e)); }
        @Override public void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression e) { enter(e, () -> super.visitMapEntryExpression(e)); }
        @Override public void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression e) { enter(e, () -> super.visitRangeExpression(e)); }
        @Override public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression e) { enter(e, () -> super.visitPropertyExpression(e)); }
        @Override public void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression e) { enter(e, () -> super.visitAttributeExpression(e)); }
        @Override public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression e) { enter(e, () -> super.visitFieldExpression(e)); }

        @Override public void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression e) { enter(e, () -> super.visitMethodPointerExpression(e)); }
        @Override public void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression e) { enter(e, () -> super.visitConstantExpression(e)); }
        @Override public void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression e) { enter(e, () -> super.visitClassExpression(e)); }
        @Override public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression e) { enter(e, () -> super.visitVariableExpression(e)); }
        @Override public void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression e) { enter(e, () -> super.visitGStringExpression(e)); }
        @Override public void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression e) { enter(e, () -> super.visitArrayExpression(e)); }
        @Override public void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression e) { enter(e, () -> super.visitSpreadExpression(e)); }
        @Override public void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression e) { enter(e, () -> super.visitSpreadMapExpression(e)); }
        @Override public void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression e) { enter(e, () -> super.visitNotExpression(e)); }
        @Override public void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression e) { enter(e, () -> super.visitUnaryMinusExpression(e)); }
        @Override public void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression e) { enter(e, () -> super.visitUnaryPlusExpression(e)); }
        @Override public void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression e) { enter(e, () -> super.visitBitwiseNegationExpression(e)); }
        @Override public void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression e) { enter(e, () -> super.visitCastExpression(e)); }
        @Override public void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression e) { enter(e, () -> super.visitClosureListExpression(e)); }
        @Override public void visitBytecodeExpression(org.codehaus.groovy.classgen.BytecodeExpression e) { enter(e, () -> super.visitBytecodeExpression(e)); }
        @Override public void visitEmptyExpression(org.codehaus.groovy.ast.expr.EmptyExpression e) { test(e); /* leaf: no children */ }
    }
}
