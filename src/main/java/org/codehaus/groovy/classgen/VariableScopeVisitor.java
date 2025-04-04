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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
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
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
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

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getPropertyName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;

/**
 * Initializes the variable scopes for an AST.
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

    public VariableScopeVisitor(SourceUnit source, boolean recurseInnerClasses) {
        this.source = source;
        this.currentScope = new VariableScope();
        this.recurseInnerClasses = recurseInnerClasses;
    }

    public VariableScopeVisitor(SourceUnit source) {
        this(source, false);
    }

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

        for (ClassNode cn = node; cn != null && !ClassHelper.isObjectType(cn); cn = cn.getSuperClass()) {
            for (FieldNode fn : cn.getFields()) {
                if (name.equals(fn.getName())) return fn;
            }

            for (PropertyNode pn : cn.getProperties()) {
                if (name.equals(pn.getName())) return pn;
            }

            for (MethodNode mn : cn.getMethods()) {
                if ((abstractType || !mn.isAbstract()) && name.equals(getPropertyName(mn))) {
                    // check for super property before returning a pseudo-property
                    for (PropertyNode pn : getAllProperties(cn.getSuperClass())) {
                        if (name.equals(pn.getName())) return pn;
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

            for (ClassNode in : cn.getAllInterfaces()) {
                FieldNode fn = in.getDeclaredField(name);
                if (fn != null) return fn;
                PropertyNode pn = in.getProperty(name);
                if (pn != null) return pn;
            }
        }

        return null;
    }

    private final Map<VariableCacheKey, Variable> variableCache = new HashMap<>(64);
    private Variable findVariableDeclaration(final String name) {
        if ("this".equals(name) || "super".equals(name)) return null;
        return variableCache.computeIfAbsent(new VariableCacheKey(name, currentScope, inSpecialConstructorCall), k -> {
            final String variableName = k.name;
            final VariableScope currentScope = k.scope;
            final boolean inSpecialConstructorCall = k.inSpecialConstructorCall;

            Variable variable = null;
            VariableScope scope = currentScope;
            boolean crossingStaticContext = false;
            // try to find a declaration of a variable
            while (true) {
                crossingStaticContext = (crossingStaticContext || scope.isInStaticContext());

                Variable var = scope.getDeclaredVariable(variableName);
                if (var != null) {
                    variable = var;
                    break;
                }

                var = scope.getReferencedLocalVariable(variableName);
                if (var != null) {
                    variable = var;
                    break;
                }

                var = scope.getReferencedClassVariable(variableName);
                if (var != null) {
                    variable = var;
                    break;
                }

                ClassNode node = scope.getClassScope();
                if (node != null) {
                    Variable member = findClassMember(node, variableName);
                    boolean requireStatic = (crossingStaticContext || inSpecialConstructorCall);
                    while (member == null && node.getOuterClass() != null && !isAnonymous(node)) {
                        requireStatic = requireStatic || isStatic(node.getModifiers());
                        member = findClassMember((node = node.getOuterClass()), variableName);
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
                variable = new DynamicVariable(variableName, crossingStaticContext);
            }

            boolean isClassVariable = (scope.isClassScope() && !scope.isReferencedLocalVariable(variableName))
                || (scope.isReferencedClassVariable(variableName) && scope.getDeclaredVariable(variableName) == null);
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
        });
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
                if (isFinal(variable.getModifiers()) && variable instanceof Parameter) {
                    addError("Cannot assign a value to final variable '" + variable.getName() + "'", node);
                }
                // TODO: handle local variables
            }
        };

        if (expression instanceof VariableExpression) {
            checkForFinal.accept((VariableExpression) expression, expression);
        } else if (expression instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) expression;
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
        if (!(object instanceof VariableExpression)) return;
        VariableExpression ve = (VariableExpression) object;
        if (!"this".equals(ve.getName())) return;
        String name = expression.getPropertyAsString();
        if (name == null || "class".equals(name)) return;
        Variable member = findClassMember(currentClass, name);
        if (member == null) return;
        checkVariableContextAccess(member, expression);
    }

    private void checkVariableContextAccess(final Variable variable, final Expression expression) {
        if (variable.isInStaticContext()) {
            if (inConstructor && currentClass.isEnum() && variable instanceof FieldNode
                    && currentClass.equals(((FieldNode) variable).getDeclaringClass())) { // GROOVY-7025
                if (!isFinal(variable.getModifiers()) || !(ClassHelper.isStaticConstantInitializerType(variable.getOriginType())
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

    @Override
    public void visitField(final FieldNode node) {
        pushState(node.isStatic());
        visitTypeReference(node.getOriginType());
        super.visitField(node);
        popState();
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        pushState(node.isStatic());
        super.visitProperty(node);
        popState();
    }

    @Override
    protected void visitAnnotation(final AnnotationNode node) {
        visitTypeReference(node.getClassNode());
        super.visitAnnotation(node);
    }

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

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        pushState();
        super.visitAssertStatement(statement);
        popState();
    }

    @Override
    public void visitBlockStatement(final BlockStatement statement) {
        pushState();
        statement.setVariableScope(currentScope);
        super.visitBlockStatement(statement);
        popState();
    }

    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        pushState();
        Parameter parameter = statement.getVariable();
        parameter.setInStaticContext(currentScope.isInStaticContext());
        declare(parameter, statement);
        super.visitCatchStatement(statement);
        popState();
    }

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

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        boolean declaresVariable = statement.getExpression() instanceof DeclarationExpression;
        if (!declaresVariable) pushState(); // GROOVY-11229: instanceof variable in expression
        super.visitExpressionStatement(statement);
        if (!declaresVariable) popState();
    }

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

    @Override
    public void visitIfElse(final IfStatement statement) {
        pushState();
        visitStatement(statement);
        statement.getBooleanExpression().visit(this);
        pushState();
        statement.getIfBlock().visit(this);
        popState();
        popState();
        pushState();
        statement.getElseBlock().visit(this);
        popState();
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        pushState();
        super.visitReturnStatement(statement);
        popState();
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        pushState();
        super.visitSwitch(statement);
        popState();
    }

    @Override
    public void visitWhileLoop(final WhileStatement statement) {
        pushState();
        super.visitWhileLoop(statement);
        popState();
    }

    // expressions:

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        visitTypeReference(expression.getType());
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        super.visitBinaryExpression(expression);

        if (Types.isAssignment(expression.getOperation().getType())) {
            checkFinalFieldAccess(expression.getLeftExpression());
        }
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        visitTypeReference(expression.getType());
        super.visitCastExpression(expression);
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        visitTypeReference(expression.getType());
        super.visitClassExpression(expression);
    }

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

    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        if (expression instanceof AnnotationConstantExpression) {
            visitTypeReference(expression.getType());
        }
        super.visitConstantExpression(expression);
    }

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

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        String name = expression.getFieldName();
        //TODO: change that to get the correct scope
        Variable variable = findVariableDeclaration(name);
        checkVariableContextAccess(variable, expression);
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression expression) {
        if (expression.isImplicitThis() && expression.getMethod() instanceof ConstantExpression) {
            ConstantExpression methodNameConstant = (ConstantExpression) expression.getMethod();
            String methodName = methodNameConstant.getText();

            if (methodName == null) {
                throw new GroovyBugError("method name is null");
            }

            Variable variable = findVariableDeclaration(methodName);
            if (variable != null && !(variable instanceof DynamicVariable)) {
                checkVariableContextAccess(variable, expression);
            }

            if (variable instanceof VariableExpression || variable instanceof Parameter) {
                VariableExpression object = new VariableExpression(variable);
                object.setSourcePosition(methodNameConstant);
                expression.setObjectExpression(object);
                ConstantExpression method = new ConstantExpression("call");
                method.setSourcePosition(methodNameConstant); // important for GROOVY-4344
                expression.setImplicitThis(false);
                expression.setMethod(method);
            }
        } else if (expression.getGenericsTypes() != null) {
            visitTypeVariables(expression.getGenericsTypes());
        }
        super.visitMethodCallExpression(expression);
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
        checkPropertyOnExplicitThis(expression);
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        var variable = findVariableDeclaration(expression.getName());
        if (variable != null) {
            expression.setAccessedVariable(variable);
            checkVariableContextAccess(variable, expression);
        }
    }

    private static class VariableCacheKey {
        private static final int DEFAULT_HASH = 0;
        private final String name;
        private final VariableScope scope;
        private final boolean inSpecialConstructorCall;
        private int hash = DEFAULT_HASH;

        VariableCacheKey(final String name, final VariableScope scope, boolean inSpecialConstructorCall) {
            this.name = name;
            this.scope = scope;
            this.inSpecialConstructorCall = inSpecialConstructorCall;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof VariableCacheKey)) return false;
            VariableCacheKey that = (VariableCacheKey) obj;
            return name.equals(that.name) && scope.equals(that.scope) && inSpecialConstructorCall == that.inSpecialConstructorCall;
        }

        @Override
        public int hashCode() {
            return DEFAULT_HASH != hash ? hash : (hash = Objects.hash(name, scope, inSpecialConstructorCall));
        }
    }
}
