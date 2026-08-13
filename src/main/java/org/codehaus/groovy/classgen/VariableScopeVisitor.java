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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import groovy.transform.Internal;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.PlaceholderVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.isStatic;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getPropertyName;
import static org.apache.groovy.ast.tools.MethodNodeUtils.withDefaultArgumentMethods;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.maybeFallsThrough;
import static org.codehaus.groovy.transform.trait.Traits.isTrait;

/**
 * Initializes the variable scopes for an AST.
 * <p>
 * For JEP&nbsp;394 {@code instanceof} pattern variables (GROOVY-12242), this
 * class is the <strong>single authoritative source</strong> of scope decisions.
 * {@link InstanceofFlowBindings} answers which pattern variables are definitely
 * bound on each control-flow path; this visitor:
 * <ul>
 *   <li>declares each pattern variable only where it is live, so out-of-scope
 *       references become {@link org.codehaus.groovy.ast.DynamicVariable}
 *       (runtime {@link groovy.lang.MissingPropertyException} in dynamic
 *       Groovy — the same rule {@code @TypeChecked} enforces at compile
 *       time); and</li>
 *   <li>attaches the same {@link InstanceofFlowBindings} instance as AST
 *       metadata so later phases (classgen) can read path-live <em>names</em>
 *       without re-running the analysis.</li>
 * </ul>
 *
 * @see InstanceofFlowBindings
 */
public class VariableScopeVisitor extends ClassCodeVisitorSupport {

    private ClassNode currentClass;
    private VariableScope currentScope;
    private boolean inClosure, inConstructor, inSpecialConstructorCall;

    private final SourceUnit source;
    private final boolean recurseInnerClasses;
    private final Deque<StateStackElement> stateStack = new LinkedList<>();

    private static class StateStackElement {
        final ClassNode clazz;
        final VariableScope scope;
        final boolean inClosure, inConstructor;

        StateStackElement(final ClassNode currentClass, final VariableScope currentScope, final boolean inClosure, final boolean inConstructor) {
            clazz = currentClass;
            scope = currentScope;
            this.inClosure = inClosure;
            this.inConstructor = inConstructor;
        }
    }

    /**
     * Creates a new variable scope visitor with optional recursion into inner classes.
     *
     * @param source the source unit being processed
     * @param recurseInnerClasses whether to recurse into inner classes
     */
    public VariableScopeVisitor(SourceUnit source, boolean recurseInnerClasses) {
        this.source = source;
        this.currentScope = new VariableScope();
        this.recurseInnerClasses = recurseInnerClasses;
    }

    /**
     * Creates a new variable scope visitor that does not recurse into inner classes.
     *
     * @param source the source unit being processed
     */
    public VariableScopeVisitor(SourceUnit source) {
        this(source, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    //----------------------------------
    // helper methods
    //----------------------------------

    private void pushState(final boolean isStatic) {
        stateStack.push(new StateStackElement(currentClass, currentScope, inClosure, inConstructor));
        currentScope = new VariableScope(currentScope);
        currentScope.setInStaticContext(isStatic);
    }

    private void pushState() {
        pushState(currentScope.isInStaticContext());
    }

    private void popState() {
        StateStackElement state = stateStack.pop();
        this.currentClass  = state.clazz;
        this.currentScope  = state.scope;
        this.inClosure     = state.inClosure;
        this.inConstructor = state.inConstructor;
    }

    private void declare(final VariableExpression variable) {
        variable.setInStaticContext(currentScope.isInStaticContext());
        declare(variable, variable);
        variable.setAccessedVariable(variable);
    }

    private void declare(final Variable variable, final ASTNode context) {
        if (PlaceholderVisitor.isPlaceholder((ASTNode) variable)) {
            return;
        }
        visitTypeReference(variable.getOriginType());
        final String variableName = variable.getName();

        final Supplier<String> msgSupplier = () -> {
            String scopeType    = "scope";
            String variableType = "variable";
            final Class<? extends ASTNode> clazz = context.getClass();
            if (clazz == FieldNode.class) {
                scopeType    = "class";
                variableType = "field";
            } else if (clazz == PropertyNode.class) {
                scopeType    = "class";
                variableType = "property";
            } else if (clazz == ClosureExpression.class) {
                scopeType    = "parameter list";
                variableType = "parameter";
            }

            String msg = "The current " + scopeType + " already contains a " + variableType + " of the name " + variableName;
            return msg;
        };

        if (currentScope.getDeclaredVariable(variableName) != null) {
            addError(msgSupplier.get(), context);
            return;
        }

        for (VariableScope scope = currentScope.getParent(); scope != null; scope = scope.getParent()) {
            // if we are in a class and no variable is declared until
            // now, then we can break the loop, because we are allowed
            // to declare a variable of the same name as a class member
            if (scope.getClassScope() != null && !isAnonymous(scope.getClassScope())) break;

            if (scope.getDeclaredVariable(variableName) != null) {
                // variable already declared
                addError(msgSupplier.get(), context);
                break;
            }
        }
        // declare the variable even if there was an error to allow more checks
        currentScope.putDeclaredVariable(variable);
    }

    private Variable findClassMember(final ClassNode node, final String name) {
        final boolean abstractType = node.isAbstract();
        Deque<ClassNode> interfaces = new LinkedList<>();
        Consumer<ClassNode[]> interfacesAndTraits = (next) -> {
            for (int i = 0; i < next.length; i += 1) {
                if (!isTrait(next[i])) interfaces.add(next[i]);
            }
            for (int i = next.length - 1; i >= 0; i -= 1) {
                if ( isTrait(next[i])) interfaces.add(next[i]);
            }
        };

        for (ClassNode cn = node; cn != null && !ClassHelper.isObjectType(cn); cn = cn.getSuperClass()) {
            for (FieldNode fn : cn.getFields()) {
                if (name.equals(fn.getName())) {
                    return fn;
                }
            }

            for (PropertyNode pn : cn.getProperties()) {
                if (name.equals(pn.getName())) {
                    return pn;
                }
            }

            for (MethodNode mn : withDefaultArgumentMethods(cn.getMethods())) { // GROOVY-11827
                if ((abstractType || !mn.isAbstract()) && name.equals(getPropertyName(mn))) {
                    // check for super property before returning a pseudo-property
                    for (PropertyNode pn : getAllProperties(cn.getSuperClass())) {
                        if (name.equals(pn.getName())) {
                            return pn;
                        }
                    }

                    FieldNode fn = new FieldNode(name, mn.getModifiers() & 0xF, ClassHelper.dynamicType(), cn, null);
                    fn.setHasNoRealSourcePosition(true);
                    fn.setDeclaringClass(cn);
                    fn.setSynthetic(true);

                    PropertyNode pn = new PropertyNode(fn, fn.getModifiers(), null, null);
                    pn.putNodeMetaData("access.method", mn);
                    pn.setDeclaringClass(cn);
                    return pn;
                }
            }

            interfacesAndTraits.accept(cn.getInterfaces());
        }

        Set<ClassNode> done = new HashSet<>();
        while (!interfaces.isEmpty()) {
            ClassNode i = interfaces.remove();
            if (done.add(i)) {
                FieldNode fn = i.getDeclaredField(name);
                if (fn != null && !isTrait(i)) {
                    return fn;
                }
                PropertyNode pn = i.getProperty(name);
                if (pn != null) {
                    return pn;
                }

                interfacesAndTraits.accept(i.getInterfaces());
            }
        }

        return null;
    }

    private Variable findVariableDeclaration(final String name) {
        if ("super".equals(name) || "this".equals(name)) return null;

        Variable variable = null;
        VariableScope scope = currentScope;
        boolean crossingStaticContext = false;
        // try to find a declaration of a variable
        while (true) {
            crossingStaticContext = (crossingStaticContext || scope.isInStaticContext());

            Variable var = scope.getDeclaredVariable(name);
            if (var != null) {
                variable = var;
                break;
            }

            var = scope.getReferencedLocalVariable(name);
            if (var != null) {
                variable = var;
                break;
            }

            var = scope.getReferencedClassVariable(name);
            if (var != null) {
                variable = var;
                break;
            }

            ClassNode node = scope.getClassScope();
            if (node != null) {
                Variable member = findClassMember(node, name);
                boolean requireStatic = (crossingStaticContext || inSpecialConstructorCall);
                while (member == null && node.getOuterClass() != null && !isAnonymous(node)) {
                    requireStatic = requireStatic || isStatic(node.getModifiers());
                    member = findClassMember((node = node.getOuterClass()), name);
                }
                if (member != null) {
                    // prevent a static context (e.g. a static method) from accessing a non-static member (e.g. a non-static field)
                    if (requireStatic ? member.isInStaticContext() : !node.isScript()) {
                        variable = member;
                    }
                }

                if (!isAnonymous(scope.getClassScope())) break; // GROOVY-5961
            }
            scope = scope.getParent();
        }
        if (variable == null) {
            variable = new DynamicVariable(name, crossingStaticContext);
        }

        boolean isClassVariable = (scope.isClassScope() && !scope.isReferencedLocalVariable(name))
            || (scope.isReferencedClassVariable(name) && scope.getDeclaredVariable(name) == null);
        VariableScope end = scope;
        scope = currentScope;
        while (scope != end) {
            if (isClassVariable) {
                scope.putReferencedClassVariable(variable);
            } else {
                scope.putReferencedLocalVariable(variable);
            }
            scope = scope.getParent();
        }

        return variable;
    }

    private void visitTypeVariables(final GenericsType[] types) {
        for (GenericsType type : types) {
            visitTypeReference(type.getType());
            if (type.getLowerBound() != null) {
                visitTypeReference(type.getLowerBound());
            }
            if (type.getUpperBounds() != null) {
                for (ClassNode bound : type.getUpperBounds()) {
                    if (bound.getLineNumber() > 0) {
                        visitTypeReference(bound);
                    }
                }
            }
        }
    }

    private void visitTypeReference(final ClassNode node) {
        visitAnnotations(node.getTypeAnnotations());
        if (node.isArray()) {
            visitTypeReference(node.getComponentType());
        } else if (node.getGenericsTypes() != null && !node.isGenericsPlaceHolder()
                && (node.isRedirectNode() || (!node.isResolved() && !node.isPrimaryClassNode()))) {
            visitTypeVariables(node.getGenericsTypes()); // "String" from "List<String> -> List<E>"
        }
    }

    private boolean isAnonymous(final ClassNode node) {
        return (node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous() && !node.isEnum());
    }

    private void markClosureSharedVariables() {
        for (Iterator<Variable> it = currentScope.getReferencedLocalVariablesIterator(); it.hasNext(); ) {
            Variable variable = it.next();
            variable.setClosureSharedVariable(true);
        }
    }

    //----------------------------------
    // variable checks
    //----------------------------------

    private void checkFinalFieldAccess(final Expression expression) {
        BiConsumer<VariableExpression, ASTNode> checkForFinal = (expr, node) -> {
            Variable variable = expr.getAccessedVariable();
            if (variable != null) {
                if (variable.isFinal() && variable instanceof Parameter) {
                    addError("Cannot assign a value to final variable '" + variable.getName() + "'", node);
                }
                // TODO: handle local variables
            }
        };

        if (expression instanceof VariableExpression) {
            checkForFinal.accept((VariableExpression) expression, expression);
        } else if (expression instanceof TupleExpression tuple) {
            for (Expression tupleExpression : tuple.getExpressions()) {
                checkForFinal.accept((VariableExpression) tupleExpression, expression);
            }
        }
        // currently not looking for PropertyExpression: dealt with at runtime using ReadOnlyPropertyException
    }

    /**
     * A property on "this", like this.x is transformed to a direct field access,
     * so we need to check the static context here.
     */
    private void checkPropertyOnExplicitThis(final PropertyExpression expression) {
        if (!currentScope.isInStaticContext()) return;
        Expression object = expression.getObjectExpression();
        if (!(object instanceof VariableExpression ve)) return;
        if (!"this".equals(ve.getName())) return;
        String name = expression.getPropertyAsString();
        if (name == null || "class".equals(name)) return;
        Variable member = findClassMember(currentClass, name);
        if (member != null) checkVariableContextAccess(member, expression);
    }

    private void checkVariableContextAccess(final Variable variable, final Expression expression) {
        if (variable.isInStaticContext()) {
            if (inConstructor && currentClass.isEnum() && variable instanceof FieldNode
                    && currentClass.equals(((FieldNode) variable).getDeclaringClass())) { // GROOVY-7025
                if (!variable.isFinal() || !(ClassHelper.isStaticConstantInitializerType(variable.getOriginType())
                        || "String".equals(variable.getOriginType().getName()))) { // TODO: String requires constant initializer
                    addError("Cannot refer to the static enum field '" + variable.getName() + "' within an initializer", expression);
                }
            }
        } else if (currentScope.isInStaticContext()) {
            // declare a static variable to be able to continue the check
            currentScope.putDeclaredVariable(new DynamicVariable(variable.getName(), currentScope.isInStaticContext()));
            addError(variable.getName() + " is declared in a dynamic context, but you tried to access it from a static context.", expression);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Sets the current class node context.
     */
    public void prepareVisit(final ClassNode node) {
        currentClass = node;
        currentScope.setClassScope(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitClass(final ClassNode node) {
        // AIC are already done, doing them here again will lead to wrong scopes
        if (isAnonymous(node)) return;

        pushState();
        inClosure = false;
        currentClass = node;
        currentScope.setClassScope(node);

        if (node.getGenericsTypes() != null) {
            visitTypeVariables(node.getGenericsTypes());
        }
        ClassNode sc = node.getUnresolvedSuperClass();
        if (sc != null && sc != ClassHelper.OBJECT_TYPE) {
            visitTypeReference(sc);
        }
        for (ClassNode i : node.getUnresolvedInterfaces()) {
            visitTypeReference(i);
        }
        // permitted subclasses exist in @Sealed annotations

        super.visitClass(node);
        if (recurseInnerClasses) {
            for (Iterator<InnerClassNode> innerClasses = node.getInnerClasses(); innerClasses.hasNext(); ) {
                visitClass(innerClasses.next());
            }
        }
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitField(final FieldNode node) {
        pushState(node.isStatic());
        visitTypeReference(node.getOriginType());
        super.visitField(node);
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitProperty(final PropertyNode node) {
        pushState(node.isStatic());
        super.visitProperty(node);
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitAnnotation(final AnnotationNode node) {
        visitTypeReference(node.getClassNode());
        super.visitAnnotation(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        pushState(node.isStatic());
        inConstructor = isConstructor;
        node.setVariableScope(currentScope);

        visitAnnotations(node);
        if (node.getGenericsTypes() != null) {
            visitTypeVariables(node.getGenericsTypes());
        }
        visitTypeReference(node.getReturnType());
        for (Parameter parameter : node.getParameters()) {
            visitAnnotations(parameter);
        }
        // add parameters to scope and visit init expressions after annotations
        // to prevent the use of parameters in annotation attributes
        for (Parameter parameter : node.getParameters()) {
            if (parameter.hasInitialExpression()) {
                parameter.getInitialExpression().visit(this);
            }
            declare(parameter, node);
        }
        for (ClassNode e : node.getExceptions()) {
            visitTypeReference(e);
        }
        visitClassCodeContainer(node.getCode());

        popState();
    }

    // statements:

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        pushState();
        super.visitAssertStatement(statement);
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitBlockStatement(final BlockStatement statement) {
        pushState();
        statement.setVariableScope(currentScope);
        super.visitBlockStatement(statement);
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        pushState();
        Parameter parameter = statement.getVariable();
        parameter.setInStaticContext(currentScope.isInStaticContext());
        declare(parameter, statement);
        super.visitCatchStatement(statement);
        popState();
    }

    /**
     * Visits a {@code do}/{@code while} loop (GROOVY-12242).
     * <p>
     * The body runs before the condition, so pattern variables from the
     * condition are <em>not</em> in scope in the body (same as Java). The
     * condition is visited in a nested scope so short-circuit RHS works and
     * pattern names do not leak after the loop. Like {@link #visitWhileLoop},
     * there is no after-loop introduction of {@code whenFalse} bindings.
     */
    @Override
    public void visitDoWhileLoop(final DoWhileStatement statement) {
        pushState();
        visitStatement(statement);
        statement.getLoopBlock().visit(this);
        pushState();
        statement.getBooleanExpression().visit(this);
        popState();
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        boolean declaresVariable = statement.getExpression() instanceof DeclarationExpression;
        if (!declaresVariable) pushState(); // GROOVY-11229: instanceof variable in expression
        super.visitExpressionStatement(statement);
        if (!declaresVariable) popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitForLoop(final ForStatement statement) {
        pushState();
        statement.setVariableScope(currentScope);
        Consumer<Parameter> define = (parameter) -> {
            parameter.setInStaticContext(currentScope.isInStaticContext());
            declare(parameter, statement);
        };
        Optional.ofNullable(statement.getIndexVariable()).ifPresent(define);
        Optional.ofNullable(statement.getValueVariable()).ifPresent(define);
        super.visitForLoop(statement);
        popState();
    }

    /**
     * Visits an {@code if}/{@code else} statement, establishing correct
     * lexical scopes for JEP&nbsp;394 {@code instanceof} pattern variables
     * (GROOVY-12242) and attaching {@link InstanceofFlowBindings} metadata so
     * later phases need not re-derive the flow analysis.
     * <p>
     * Rules applied (JLS §6.3.2.2 / JEP 394):
     * <ul>
     *   <li>§6.3.2.2-200-A: {@code e.whenTrue()} are in scope in the then-block (S).</li>
     *   <li>§6.3.2.2-200-B: {@code e.whenFalse()} are in scope in the else-block (T).</li>
     *   <li>§6.3.2.2-200-C-A: if T cannot complete normally and S can, and the var
     *       is in {@code e.whenTrue()}, the var is introduced after the if-else.</li>
     *   <li>§6.3.2.2-200-C-B: if S cannot complete normally and T can (or there is
     *       no T), and the var is in {@code e.whenFalse()}, the var is introduced
     *       after the if-else.</li>
     * </ul>
     * In dynamic Groovy, undeclared references resolve to
     * {@link org.codehaus.groovy.ast.DynamicVariable} (runtime
     * {@link groovy.lang.MissingPropertyException}). {@code @TypeChecked}
     * enforces the same rules at compile time; both modes share this visitor.
     */
    @Override
    public void visitIfElse(final IfStatement statement) {
        InstanceofFlowBindings bindings = InstanceofFlowBindings.of(statement.getBooleanExpression());
        // Same analysis result: declare into scopes + enrich AST for classgen.
        InstanceofFlowBindings.put(statement, bindings);

        // Condition: pattern vars are available for short-circuit RHS (e.g. &&).
        pushState();
        visitStatement(statement);
        statement.getBooleanExpression().visit(this);
        popState();

        // Then-block: §6.3.2.2-200-A — only e.whenTrue() bindings.
        pushState();
        declarePatternVariables(bindings.whenTrue());
        statement.getIfBlock().visit(this);
        popState();

        // Else-block: §6.3.2.2-200-B — only e.whenFalse() bindings.
        pushState();
        declarePatternVariables(bindings.whenFalse());
        statement.getElseBlock().visit(this);
        popState();

        // After the if-else:
        // §6.3.2.2-200-C-B: if-block (S) is abrupt → e.whenFalse() survive after.
        if (!maybeFallsThrough(statement.getIfBlock())) {
            declarePatternVariables(bindings.whenFalse());
        }
        // §6.3.2.2-200-C-A: else-block (T) is abrupt → e.whenTrue() survive after.
        if (!statement.getElseBlock().isEmpty() && !maybeFallsThrough(statement.getElseBlock())) {
            declarePatternVariables(bindings.whenTrue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        pushState();
        super.visitReturnStatement(statement);
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitSwitch(final SwitchStatement statement) {
        pushState();
        super.visitSwitch(statement);
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitSwitchExpression(final org.codehaus.groovy.ast.expr.SwitchExpression expression) {
        pushState();
        super.visitSwitchExpression(expression);
        popState();
    }

    /**
     * Visits a {@code while} loop with <em>partial</em> JEP&nbsp;394 flow scoping
     * for {@code instanceof} pattern variables (GROOVY-12242).
     * <p>
     * <strong>What is supported (aligned with the if-then rule for the body):</strong>
     * <ul>
     *   <li>Short-circuit visibility inside the condition ({@code &&} / {@code ||}).</li>
     *   <li>{@code e.whenTrue()} pattern variables are in scope in the loop body.</li>
     * </ul>
     * <p>
     * <strong>Intentional divergence from JLS §6.3.2.3 (while):</strong>
     * Groovy does <em>not</em> introduce {@code e.whenFalse()} after the loop when
     * the body cannot complete normally. Full after-loop introduction would need
     * definite abrupt-completion analysis of every exit path (including
     * {@code break}/{@code continue} of nested loops) and is left out for 6.0 —
     * pattern variables never leak past the loop. Documented as a deliberate
     * partial implementation, not an oversight.
     */
    @Override
    public void visitWhileLoop(final WhileStatement statement) {
        InstanceofFlowBindings bindings = InstanceofFlowBindings.of(statement.getBooleanExpression());

        // Condition: short-circuit RHS only; discard pattern decls after the visit.
        pushState();
        visitStatement(statement);
        statement.getBooleanExpression().visit(this);
        popState();

        // Body: § if-then analogue — only e.whenTrue() (no after-loop whenFalse).
        pushState();
        declarePatternVariables(bindings.whenTrue());
        statement.getLoopBlock().visit(this);
        popState();
    }

    /**
     * Declares {@code instanceof} pattern variables into the current scope so
     * that subsequent visits resolve them as locals. Skips names already present
     * in this scope (re-declaring the same pattern variable object after a
     * condition visit is a no-op).
     */
    private void declarePatternVariables(final List<VariableExpression> patternVariables) {
        for (VariableExpression variable : patternVariables) {
            if (currentScope.getDeclaredVariable(variable.getName()) == null) {
                declare(variable);
            }
        }
    }

    // expressions:

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        visitTypeReference(expression.getType());
        super.visitArrayExpression(expression);
    }

    /**
     * Visits binary expressions with flow-aware scoping for {@code &&} / {@code ||}
     * so pattern variables follow Java short-circuit rules (GROOVY-12242 / JEP 394):
     * <ul>
     *   <li>{@code a && b} — true-path bindings of {@code a} are in scope in {@code b}</li>
     *   <li>{@code a || b} — true-path bindings of {@code a} are <em>not</em> in scope in {@code b};
     *       false-path bindings of {@code a} are</li>
     *   <li>{@code e !instanceof T t} / {@code !(e instanceof T t)} — pattern declare is
     *       isolated on the left of short-circuit ops; only flow-live sets are re-introduced</li>
     * </ul>
     */
    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        int op = expression.getOperation().getType();
        if (op == Types.LOGICAL_AND) {
            // Symmetric to ||: isolate left's declares, then expose only whenTrue on the right.
            // Fixes `e !instanceof T t && t.m()` and `!(e instanceof T t) && t.m()` (whenTrue={}).
            InstanceofFlowBindings leftBindings = InstanceofFlowBindings.of(expression.getLeftExpression());
            pushState();
            expression.getLeftExpression().visit(this);
            popState();
            pushState();
            declarePatternVariables(leftBindings.whenTrue());
            expression.getRightExpression().visit(this);
            popState();
        } else if (op == Types.LOGICAL_OR) {
            // Left's true-path bindings must not leak into the right (Java rejects
            // `o instanceof String s || s.isEmpty()`). False-path bindings of the
            // left are in scope on the right (`o !instanceof String s || s.isEmpty()`).
            InstanceofFlowBindings leftBindings = InstanceofFlowBindings.of(expression.getLeftExpression());
            pushState();
            expression.getLeftExpression().visit(this);
            popState();
            pushState();
            declarePatternVariables(leftBindings.whenFalse());
            expression.getRightExpression().visit(this);
            popState();
        } else if (op == Types.COMPARE_NOT_INSTANCEOF) {
            // Defence in depth: pattern declare on the RHS must not stick to the
            // enclosing scope (whenTrue of !instanceof is empty). Live paths
            // re-introduce via declarePatternVariables / short-circuit handlers.
            expression.getLeftExpression().visit(this);
            pushState();
            expression.getRightExpression().visit(this);
            popState();
        } else {
            super.visitBinaryExpression(expression);
        }

        if (Types.isAssignment(op)) {
            checkFinalFieldAccess(expression.getLeftExpression());
        }
    }

    /**
     * Visits a ternary / Elvis expression with flow scoping for pattern variables:
     * true-path bindings are in scope in the then-branch; false-path bindings in
     * the else-branch (GROOVY-12242 / JEP 394). Also attaches
     * {@link InstanceofFlowBindings} metadata for later phases.
     */
    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        InstanceofFlowBindings bindings = InstanceofFlowBindings.of(expression.getBooleanExpression());
        InstanceofFlowBindings.put(expression, bindings);

        pushState();
        expression.getBooleanExpression().visit(this);
        popState();

        pushState();
        declarePatternVariables(bindings.whenTrue());
        expression.getTrueExpression().visit(this);
        popState();

        pushState();
        declarePatternVariables(bindings.whenFalse());
        expression.getFalseExpression().visit(this);
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitCastExpression(final CastExpression expression) {
        visitTypeReference(expression.getType());
        super.visitCastExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitClassExpression(final ClassExpression expression) {
        visitTypeReference(expression.getType());
        super.visitClassExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        pushState();
        expression.setVariableScope(currentScope);
        inClosure = !isAnonymous(currentScope.getParent().getClassScope());

        if (expression.isParameterSpecified()) {
            for (Parameter parameter : expression.getParameters()) {
                parameter.setInStaticContext(currentScope.isInStaticContext());
                declare(parameter, expression);
            }
        } else if (expression.getParameters() != null) {
            Parameter implicit = new Parameter(ClassHelper.dynamicType(), "it");
            implicit.setInStaticContext(currentScope.isInStaticContext());
            currentScope.putDeclaredVariable(implicit);
        }

        super.visitClosureExpression(expression);
        markClosureSharedVariables();
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        if (expression instanceof AnnotationConstantExpression) {
            visitTypeReference(expression.getType());
        }
        super.visitConstantExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression expression) {
        if (!expression.isSpecialCall()) visitTypeReference(expression.getType());
        boolean oldInSpecialCtorFlag = inSpecialConstructorCall;
        inSpecialConstructorCall |= expression.isSpecialCall();
        super.visitConstructorCallExpression(expression);
        inSpecialConstructorCall = oldInSpecialCtorFlag;

        if (!expression.isUsingAnonymousInnerClass()) return;

        pushState();
        InnerClassNode innerClass = (InnerClassNode) expression.getType();
        innerClass.setVariableScope(currentScope);
        currentScope.setClassScope(innerClass);
        currentScope.setInStaticContext(false);
        for (MethodNode method : innerClass.getMethods()) {
            visitAnnotations(method); // GROOVY-7033
            Parameter[] parameters = method.getParameters();
            for (Parameter p : parameters) visitAnnotations(p); // GROOVY-7033
            if (parameters.length == 0) parameters = null; // disable implicit "it"
            visitClosureExpression(new ClosureExpression(parameters, method.getCode()));
        }

        for (FieldNode field : innerClass.getFields()) {
            visitAnnotations(field); // GROOVY-7033
            Expression initExpression = field.getInitialExpression();
            if (initExpression != null) {
                pushState(field.isStatic());
                if (initExpression.isSynthetic() && initExpression instanceof VariableExpression
                        && ((VariableExpression) initExpression).getAccessedVariable() instanceof Parameter) {
                    // GROOVY-6834: accessing a parameter which is not yet seen in scope
                    popState();
                    continue;
                }
                initExpression.visit(this);
                popState();
            }
        }

        for (Statement initStatement : innerClass.getObjectInitializerStatements()) {
            initStatement.visit(this);
        }
        markClosureSharedVariables();
        popState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        pushState(); // GROOVY-11229
        visitAnnotations(expression);
        // visit right side first to prevent the use of a variable before its declaration
        expression.getRightExpression().visit(this);
        popState();

        if (expression.isMultipleAssignmentDeclaration()) {
            TupleExpression list = expression.getTupleExpression();
            for (Expression listExpression : list.getExpressions()) {
                declare((VariableExpression) listExpression);
            }
        } else {
            declare(expression.getVariableExpression());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        String name = expression.getFieldName();
        // TODO: change that to get the correct scope
        Variable variable = findVariableDeclaration(name);
        if (variable != null) checkVariableContextAccess(variable, expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMethodCallExpression(final MethodCallExpression expression) {
        String methodName = expression.getMethodAsString();
        if (methodName != null && expression.isImplicitThis()) {
            // GROOVY-3069, GROOVY-11677: variable or parameter call
            Variable variable = findVariableDeclaration(methodName);
            // if "name" resolves to a variable, replace "name(...)" with "name.call(...)"
            if (variable instanceof VariableExpression || variable instanceof Parameter) {
                Expression object = new VariableExpression(variable);
                object.setSourcePosition(expression.getMethod());
                expression.setObjectExpression(object);
                expression.setImplicitThis(false);

                Expression method = new ConstantExpression("call");
                // GROOVY-4344: ensure result of "call" for assert
                method.setSourcePosition(expression.getMethod());
                expression.setMethod(method);

                checkVariableContextAccess(variable, expression);
                expression.getArguments().visit(this);
                return;
            }
        } else if (expression.getGenericsTypes() != null) {
            visitTypeVariables(expression.getGenericsTypes());
        }
        super.visitMethodCallExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
        checkPropertyOnExplicitThis(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        var variable = findVariableDeclaration(expression.getName());
        if (variable != null) {
            expression.setAccessedVariable(variable);
            checkVariableContextAccess(variable, expression);
        }
    }

    // =========================================================================
    // Nested class: InstanceofFlowBindings
    // =========================================================================

    /**
     * Flow-sensitive result for JEP&nbsp;394 {@code instanceof} pattern
     * bindings (GROOVY-12242) — the Groovy equivalent of the JLS §6.3.1
     * &ldquo;introduced by&rdquo; sets.
     * <p>
     * One type, two views of the same analysis:
     * <ul>
     *   <li>{@link #whenTrue()}/{@link #whenFalse()} — pattern
     *       {@link VariableExpression}s for {@link VariableScopeVisitor} to
     *       declare into lexical scopes;</li>
     *   <li>{@link #whenTrueNames()}/{@link #whenFalseNames()} — the same
     *       sets as names for classgen to path-hide CompileStack slots.</li>
     * </ul>
     * {@link VariableScopeVisitor} runs the analysis once via {@link #of},
     * declares from the variable lists, and attaches this instance as AST
     * metadata ({@link #put}/{@link #get}). Later phases must use
     * {@link #get} only — never re-call {@link #of} on the condition.
     * <p>
     * Covered shapes: {@code e instanceof T t}, {@code e !instanceof T t}
     * (native form and {@code !(e instanceof T t)}), {@code !expr},
     * {@code a && b}, {@code a || b}. All other shapes yield {@link #EMPTY}
     * (conservative: no definite bindings).
     *
     * @see VariableScopeVisitor
     * @since 6.0.0
     */
    @Internal
    public static final class InstanceofFlowBindings {

        /** Metadata key for {@link ASTNode#putNodeMetaData}/{@code getNodeMetaData}. */
        public static final Object KEY = InstanceofFlowBindings.class;

        /** Singleton for "no pattern variables on either path". */
        public static final InstanceofFlowBindings EMPTY =
                new InstanceofFlowBindings(List.of(), List.of());

        private final List<VariableExpression> whenTrue;
        private final List<VariableExpression> whenFalse;
        /** Cached name view of {@link #whenTrue}; never reallocated. */
        private final Set<String> whenTrueNames;
        /** Cached name view of {@link #whenFalse}; never reallocated. */
        private final Set<String> whenFalseNames;

        private InstanceofFlowBindings(final List<VariableExpression> whenTrue,
                                       final List<VariableExpression> whenFalse) {
            this.whenTrue = whenTrue;
            this.whenFalse = whenFalse;
            this.whenTrueNames = namesOf(whenTrue);
            this.whenFalseNames = namesOf(whenFalse);
        }

        /**
         * Pattern variables definitely assigned when the condition is {@code true}.
         * Used by {@link VariableScopeVisitor} to declare into scopes.
         */
        public List<VariableExpression> whenTrue() { return whenTrue; }

        /**
         * Pattern variables definitely assigned when the condition is {@code false}.
         * Used by {@link VariableScopeVisitor} to declare into scopes.
         */
        public List<VariableExpression> whenFalse() { return whenFalse; }

        /**
         * Names of pattern variables bound when the condition is {@code true}.
         * Used by classgen for CompileStack path-hide; same set as {@link #whenTrue()}.
         */
        public Set<String> whenTrueNames() { return whenTrueNames; }

        /**
         * Names of pattern variables bound when the condition is {@code false}.
         * Used by classgen for CompileStack path-hide; same set as {@link #whenFalse()}.
         */
        public Set<String> whenFalseNames() { return whenFalseNames; }

        /** Whether any pattern variable is bound on either path. */
        public boolean isEmpty() {
            return whenTrue.isEmpty() && whenFalse.isEmpty();
        }

        /**
         * All pattern-variable names in either path (stable encounter order).
         */
        public Set<String> allNames() {
            if (isEmpty()) return Collections.emptySet();
            if (whenFalseNames.isEmpty()) return whenTrueNames;
            if (whenTrueNames.isEmpty()) return whenFalseNames;
            Set<String> names = new LinkedHashSet<>(whenTrueNames.size() + whenFalseNames.size());
            names.addAll(whenTrueNames);
            names.addAll(whenFalseNames);
            return Collections.unmodifiableSet(names);
        }

        // -----------------------------------------------------------------
        // AST metadata (enrichment for later phases)
        // -----------------------------------------------------------------

        /**
         * Attaches this analysis result to {@code node} for later phases.
         * No-op when {@code bindings} is null or {@link #EMPTY}.
         */
        public static void put(final ASTNode node, final InstanceofFlowBindings bindings) {
            if (node == null || bindings == null || bindings.isEmpty()) return;
            node.putNodeMetaData(KEY, bindings);
        }

        /**
         * Returns the analysis result previously attached to {@code node}, or
         * {@link #EMPTY} if none (no path-live pattern variables).
         */
        public static InstanceofFlowBindings get(final ASTNode node) {
            if (node == null) return EMPTY;
            InstanceofFlowBindings bindings = node.getNodeMetaData(KEY);
            return bindings != null ? bindings : EMPTY;
        }

        // -----------------------------------------------------------------
        // Analysis entry points
        // -----------------------------------------------------------------

        /**
         * Analyses {@code expression} for definite {@code instanceof} pattern
         * bindings. Call only from {@link VariableScopeVisitor} (or tests);
         * classgen must use {@link #get(ASTNode)}.
         *
         * @param expression a boolean condition (may be a
         *                   {@link BooleanExpression} wrapper); {@code null}
         *                   yields {@link #EMPTY}
         * @return the true/false binding sets; never {@code null}
         */
        public static InstanceofFlowBindings of(final Expression expression) {
            return expression == null ? EMPTY : analyse(expression);
        }

        /**
         * Returns {@code true} if {@code expression} contains any JEP&nbsp;394
         * type-pattern node at any depth. Full subtree walk (unlike {@link #of}).
         * Used by tests and diagnostics; classgen has its own structural check
         * for expression-statement isolation.
         */
        public static boolean containsPattern(final Expression expression) {
            if (expression == null) return false;
            boolean[] found = {false};
            expression.visit(new CodeVisitorSupport() {
                @Override
                public void visitBinaryExpression(final BinaryExpression be) {
                    if (found[0]) return;
                    int op = be.getOperation().getType();
                    if ((op == Types.KEYWORD_INSTANCEOF || op == Types.COMPARE_NOT_INSTANCEOF)
                            && isTypePattern(be.getRightExpression())) {
                        found[0] = true;
                        return;
                    }
                    super.visitBinaryExpression(be);
                }
            });
            return found[0];
        }

        /**
         * Names of <em>all</em> pattern variables in {@code expression}, regardless
         * of definite-assignment path. Full tree walk; for tests/diagnostics.
         * Classgen tracks allocated slots via CompileStack instead.
         */
        public static Set<String> allPatternNames(final Expression expression) {
            if (expression == null) return Collections.emptySet();
            Set<String> names = new LinkedHashSet<>();
            expression.visit(new CodeVisitorSupport() {
                @Override
                public void visitBinaryExpression(final BinaryExpression be) {
                    int op = be.getOperation().getType();
                    if ((op == Types.KEYWORD_INSTANCEOF || op == Types.COMPARE_NOT_INSTANCEOF)
                            && isTypePattern(be.getRightExpression())) {
                        names.add(((DeclarationExpression) be.getRightExpression())
                                .getVariableExpression().getName());
                        return;
                    }
                    super.visitBinaryExpression(be);
                }
            });
            return Collections.unmodifiableSet(names);
        }

        // -----------------------------------------------------------------
        // Internal recursive descent
        // -----------------------------------------------------------------

        /**
         * Recursive descent over the boolean algebra of the condition.
         * Only operators that propagate definite-assignment are followed
         * ({@code instanceof}, {@code !instanceof}, {@code !}, {@code &&},
         * {@code ||}). Other shapes return {@link #EMPTY} conservatively.
         */
        private static InstanceofFlowBindings analyse(final Expression expression) {
            Expression expr = expression;
            while (expr instanceof BooleanExpression && !(expr instanceof NotExpression)) {
                expr = ((BooleanExpression) expr).getExpression();
            }
            if (expr instanceof NotExpression not) {
                return analyse(not.getExpression()).negated();
            }
            if (expr instanceof BinaryExpression binary) {
                int op = binary.getOperation().getType();
                if (op == Types.KEYWORD_INSTANCEOF) {
                    return ofInstanceof(binary);
                }
                if (op == Types.COMPARE_NOT_INSTANCEOF) {
                    return ofInstanceof(binary).negated();
                }
                if (op == Types.LOGICAL_AND) {
                    InstanceofFlowBindings left  = analyse(binary.getLeftExpression());
                    InstanceofFlowBindings right = analyse(binary.getRightExpression());
                    // True only when both sides are true → union true-sets.
                    return new InstanceofFlowBindings(union(left.whenTrue, right.whenTrue), List.of());
                }
                if (op == Types.LOGICAL_OR) {
                    InstanceofFlowBindings left  = analyse(binary.getLeftExpression());
                    InstanceofFlowBindings right = analyse(binary.getRightExpression());
                    // False only when both sides are false → union false-sets.
                    return new InstanceofFlowBindings(List.of(), union(left.whenFalse, right.whenFalse));
                }
            }
            return EMPTY;
        }

        private static InstanceofFlowBindings ofInstanceof(final BinaryExpression binary) {
            Expression right = binary.getRightExpression();
            if (isTypePattern(right)) {
                VariableExpression patternVar =
                        ((DeclarationExpression) right).getVariableExpression();
                return new InstanceofFlowBindings(List.of(patternVar), List.of());
            }
            return EMPTY;
        }

        private InstanceofFlowBindings negated() {
            return isEmpty() ? this : new InstanceofFlowBindings(whenFalse, whenTrue);
        }

        private static List<VariableExpression> union(final List<VariableExpression> a,
                                                      final List<VariableExpression> b) {
            if (a.isEmpty()) return b;
            if (b.isEmpty()) return a;
            List<VariableExpression> result = new ArrayList<>(a.size() + b.size());
            Set<String> seen = new LinkedHashSet<>();
            for (VariableExpression ve : a) if (seen.add(ve.getName())) result.add(ve);
            for (VariableExpression ve : b) if (seen.add(ve.getName())) result.add(ve);
            return List.copyOf(result);
        }

        private static Set<String> namesOf(final List<VariableExpression> vars) {
            if (vars.isEmpty()) return Collections.emptySet();
            if (vars.size() == 1) return Set.of(vars.get(0).getName());
            Set<String> result = new LinkedHashSet<>(vars.size());
            for (VariableExpression ve : vars) result.add(ve.getName());
            return Collections.unmodifiableSet(result);
        }

        private static boolean isTypePattern(final Expression right) {
            return right instanceof DeclarationExpression decl
                    && !decl.isMultipleAssignmentDeclaration()
                    && decl.getVariableExpression() != null;
        }
    }
}
