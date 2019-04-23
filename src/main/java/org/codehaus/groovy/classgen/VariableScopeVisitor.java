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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
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
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Modifier.isFinal;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getPropertyName;

/**
 * Goes through an AST and initializes the scopes.
 */
public class VariableScopeVisitor extends ClassCodeVisitorSupport {

    private VariableScope currentScope = null;
    private final VariableScope headScope = new VariableScope();
    private ClassNode currentClass = null;
    private final SourceUnit source;
    private boolean isSpecialConstructorCall = false;
    private boolean inConstructor = false;
    private final boolean recurseInnerClasses;

    private final LinkedList stateStack = new LinkedList();

    private class StateStackElement {
        final VariableScope scope;
        final ClassNode clazz;
        final boolean inConstructor;

        StateStackElement() {
            scope = VariableScopeVisitor.this.currentScope;
            clazz = VariableScopeVisitor.this.currentClass;
            inConstructor = VariableScopeVisitor.this.inConstructor;
        }
    }

    public VariableScopeVisitor(SourceUnit source, boolean recurseInnerClasses) {
        this.source = source;
        currentScope = headScope;
        this.recurseInnerClasses = recurseInnerClasses;
    }


    public VariableScopeVisitor(SourceUnit source) {
        this(source, false);
    }

    // ------------------------------
    // helper methods
    //------------------------------

    private void pushState(boolean isStatic) {
        stateStack.add(new StateStackElement());
        currentScope = new VariableScope(currentScope);
        currentScope.setInStaticContext(isStatic);
    }

    private void pushState() {
        pushState(currentScope.isInStaticContext());
    }

    private void popState() {
        StateStackElement element = (StateStackElement) stateStack.removeLast();
        currentScope = element.scope;
        currentClass = element.clazz;
        inConstructor = element.inConstructor;
    }

    private void declare(Parameter[] parameters, ASTNode node) {
        for (Parameter parameter : parameters) {
            if (parameter.hasInitialExpression()) {
                parameter.getInitialExpression().visit(this);
            }
            declare(parameter, node);
        }
    }

    private void declare(VariableExpression vex) {
        vex.setInStaticContext(currentScope.isInStaticContext());
        declare(vex, vex);
        vex.setAccessedVariable(vex);
    }

    private void declare(Variable var, ASTNode expr) {
        String scopeType = "scope";
        String variableType = "variable";

        if (expr.getClass() == FieldNode.class) {
            scopeType = "class";
            variableType = "field";
        } else if (expr.getClass() == PropertyNode.class) {
            scopeType = "class";
            variableType = "property";
        }

        StringBuilder msg = new StringBuilder();
        msg.append("The current ").append(scopeType);
        msg.append(" already contains a ").append(variableType);
        msg.append(" of the name ").append(var.getName());

        if (currentScope.getDeclaredVariable(var.getName()) != null) {
            addError(msg.toString(), expr);
            return;
        }

        for (VariableScope scope = currentScope.getParent(); scope != null; scope = scope.getParent()) {
            // if we are in a class and no variable is declared until
            // now, then we can break the loop, because we are allowed
            // to declare a variable of the same name as a class member
            if (scope.getClassScope() != null && !isAnonymous(scope.getClassScope())) break;

            if (scope.getDeclaredVariable(var.getName()) != null) {
                // variable already declared
                addError(msg.toString(), expr);
                break;
            }
        }
        // declare the variable even if there was an error to allow more checks
        currentScope.putDeclaredVariable(var);
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    private Variable findClassMember(ClassNode cn, String name) {
        if (cn == null) return null;
        if (cn.isScript()) {
            return new DynamicVariable(name, false);
        }

        for (FieldNode fn : cn.getFields()) {
            if (fn.getName().equals(name)) return fn;
        }

        for (MethodNode mn : cn.getMethods()) {
            String pName = getPropertyName(mn);
            if (name.equals(pName)) {
                PropertyNode property = new PropertyNode(name, mn.getModifiers(), ClassHelper.OBJECT_TYPE, cn, null, null, null);
                property.getField().setHasNoRealSourcePosition(true);
                property.getField().setSynthetic(true);
                property.getField().setDeclaringClass(cn);
                property.setDeclaringClass(cn);
                return property;
            }
        }

        for (PropertyNode pn : cn.getProperties()) {
            if (pn.getName().equals(name)) return pn;
        }

        Variable ret = findClassMember(cn.getSuperClass(), name);
        if (ret != null) return ret;
        if (isAnonymous(cn)) return null;
        return findClassMember(cn.getOuterClass(), name);
    }

    private static boolean isAnonymous(ClassNode node) {
        return (!node.isEnum() && node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous());
    }

    // -------------------------------
    // different Variable based checks
    // -------------------------------

    private Variable checkVariableNameForDeclaration(String name, Expression expression) {
        if ("super".equals(name) || "this".equals(name)) return null;

        VariableScope scope = currentScope;
        Variable var = new DynamicVariable(name, currentScope.isInStaticContext());
        Variable orig = var;
        // try to find a declaration of a variable
        boolean crossingStaticContext = false;
        while (true) {
            crossingStaticContext = crossingStaticContext || scope.isInStaticContext();

            Variable var1 = scope.getDeclaredVariable(var.getName());
            if (var1 != null) {
                var = var1;
                break;
            }

            var1 = scope.getReferencedLocalVariable(var.getName());
            if (var1 != null) {
                var = var1;
                break;
            }

            var1 = scope.getReferencedClassVariable(var.getName());
            if (var1 != null) {
                var = var1;
                break;
            }

            ClassNode classScope = scope.getClassScope();
            if (classScope != null) {
                Variable member = findClassMember(classScope, var.getName());
                if (member != null) {
                    boolean staticScope = crossingStaticContext || isSpecialConstructorCall;
                    boolean staticMember = member.isInStaticContext();
                    // We don't allow a static context (e.g. a static method) to access
                    // a non-static variable (e.g. a non-static field).
                    if (!(staticScope && !staticMember))
                        var = member;
                }
                // GROOVY-5961
                if (!isAnonymous(classScope))
                    break;
            }
            scope = scope.getParent();
        }
        if (var == orig && crossingStaticContext) {
            var = new DynamicVariable(var.getName(), true);
        }

        VariableScope end = scope;

        scope = currentScope;
        while (scope != end) {
            if (end.isClassScope() ||
                    (end.isReferencedClassVariable(name) && end.getDeclaredVariable(name) == null)) {
                scope.putReferencedClassVariable(var);
            } else {
                scope.putReferencedLocalVariable(var);
            }
            scope = scope.getParent();
        }

        return var;
    }

    /**
     * a property on "this", like this.x is transformed to a
     * direct field access, so we need to check the
     * static context here
     *
     * @param pe the property expression to check
     */
    private void checkPropertyOnExplicitThis(PropertyExpression pe) {
        if (!currentScope.isInStaticContext()) return;
        Expression object = pe.getObjectExpression();
        if (!(object instanceof VariableExpression)) return;
        VariableExpression ve = (VariableExpression) object;
        if (!ve.getName().equals("this")) return;
        String name = pe.getPropertyAsString();
        if (name == null || name.equals("class")) return;
        Variable member = findClassMember(currentClass, name);
        if (member == null) return;
        checkVariableContextAccess(member, pe);
    }

    private void checkVariableContextAccess(Variable v, Expression expr) {
        if (v.isInStaticContext() || !currentScope.isInStaticContext()) return;

        String msg = v.getName() +
                " is declared in a dynamic context, but you tried to" +
                " access it from a static context.";
        addError(msg, expr);

        // declare a static variable to be able to continue the check
        DynamicVariable v2 = new DynamicVariable(v.getName(), currentScope.isInStaticContext());
        currentScope.putDeclaredVariable(v2);
    }

    // ------------------------------
    // code visit
    // ------------------------------

    public void visitBlockStatement(BlockStatement block) {
        pushState();
        block.setVariableScope(currentScope);
        super.visitBlockStatement(block);
        popState();
    }

    public void visitForLoop(ForStatement forLoop) {
        pushState();
        forLoop.setVariableScope(currentScope);
        Parameter p = forLoop.getVariable();
        p.setInStaticContext(currentScope.isInStaticContext());
        if (p != ForStatement.FOR_LOOP_DUMMY) declare(p, forLoop);
        super.visitForLoop(forLoop);
        popState();
    }

    public void visitIfElse(IfStatement ifElse) {
        ifElse.getBooleanExpression().visit(this);
        pushState();
        ifElse.getIfBlock().visit(this);
        popState();
        pushState();
        ifElse.getElseBlock().visit(this);
        popState();
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitAnnotations(expression);
        // visit right side first to avoid the usage of a
        // variable before its declaration
        expression.getRightExpression().visit(this);

        if (expression.isMultipleAssignmentDeclaration()) {
            TupleExpression list = expression.getTupleExpression();
            for (Expression e : list.getExpressions()) {
                declare((VariableExpression) e);
            }
        } else {
            declare(expression.getVariableExpression());
        }
    }

    @Override
    public void visitBinaryExpression(BinaryExpression be) {
        super.visitBinaryExpression(be);
        switch (be.getOperation().getType()) {
            case Types.EQUAL: // = assignment
            case Types.BITWISE_AND_EQUAL:
            case Types.BITWISE_OR_EQUAL:
            case Types.BITWISE_XOR_EQUAL:
            case Types.PLUS_EQUAL:
            case Types.MINUS_EQUAL:
            case Types.MULTIPLY_EQUAL:
            case Types.DIVIDE_EQUAL:
            case Types.INTDIV_EQUAL:
            case Types.MOD_EQUAL:
            case Types.POWER_EQUAL:
            case Types.LEFT_SHIFT_EQUAL:
            case Types.RIGHT_SHIFT_EQUAL:
            case Types.RIGHT_SHIFT_UNSIGNED_EQUAL:
                checkFinalFieldAccess(be.getLeftExpression());
                break;
            default:
                break;
        }
    }

    private void checkFinalFieldAccess(Expression expression) {
        // currently not looking for PropertyExpression: dealt with at runtime using ReadOnlyPropertyException
        if (!(expression instanceof VariableExpression) && !(expression instanceof TupleExpression)) return;
        if (expression instanceof TupleExpression) {
            TupleExpression list = (TupleExpression) expression;
            for (Expression e : list.getExpressions()) {
                checkForFinal(expression, (VariableExpression) e);
            }
        } else {
            checkForFinal(expression, (VariableExpression) expression);
        }
    }

    // TODO handle local variables
    private void checkForFinal(final Expression expression, VariableExpression ve) {
        Variable v = ve.getAccessedVariable();
        if (v != null) {
            boolean isFinal = isFinal(v.getModifiers());
            boolean isParameter = v instanceof Parameter;
            if (isFinal && isParameter) {
                addError("Cannot assign a value to final variable '" + v.getName() + "'", expression);
            }
        }
    }

    public void visitVariableExpression(VariableExpression expression) {
        String name = expression.getName();
        Variable v = checkVariableNameForDeclaration(name, expression);
        if (v == null) return;
        expression.setAccessedVariable(v);
        checkVariableContextAccess(v, expression);
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
        checkPropertyOnExplicitThis(expression);
    }

    public void visitClosureExpression(ClosureExpression expression) {
        pushState();

        expression.setVariableScope(currentScope);

        if (expression.isParameterSpecified()) {
            for (Parameter parameter : expression.getParameters()) {
                parameter.setInStaticContext(currentScope.isInStaticContext());
                if (parameter.hasInitialExpression()) {
                    parameter.getInitialExpression().visit(this);
                }
                declare(parameter, expression);
            }
        } else if (expression.getParameters() != null) {
            Parameter var = new Parameter(ClassHelper.OBJECT_TYPE, "it");
            var.setInStaticContext(currentScope.isInStaticContext());
            currentScope.putDeclaredVariable(var);
        }

        super.visitClosureExpression(expression);
        markClosureSharedVariables();

        popState();
    }

    private void markClosureSharedVariables() {
        VariableScope scope = currentScope;
        for (Iterator<Variable> it = scope.getReferencedLocalVariablesIterator(); it.hasNext(); ) {
            it.next().setClosureSharedVariable(true);
        }
    }

    public void visitCatchStatement(CatchStatement statement) {
        pushState();
        Parameter p = statement.getVariable();
        p.setInStaticContext(currentScope.isInStaticContext());
        declare(p, statement);
        super.visitCatchStatement(statement);
        popState();
    }

    public void visitFieldExpression(FieldExpression expression) {
        String name = expression.getFieldName();
        //TODO: change that to get the correct scope
        Variable v = checkVariableNameForDeclaration(name, expression);
        checkVariableContextAccess(v, expression);
    }

    // ------------------------------
    // class visit
    // ------------------------------

    public void visitClass(ClassNode node) {
        // AIC are already done, doing them here again will lead to wrong scopes
        if (isAnonymous(node)) return;

        pushState();

        prepareVisit(node);

        super.visitClass(node);
        if (recurseInnerClasses) {
            Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
            while (innerClasses.hasNext()) {
                visitClass(innerClasses.next());
            }
        }
        popState();
    }

    /**
     * Sets the current class node context.
     */
    public void prepareVisit(ClassNode node) {
        currentClass = node;
        currentScope.setClassScope(node);
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        pushState(node.isStatic());
        inConstructor = isConstructor;
        node.setVariableScope(currentScope);
        visitAnnotations(node);

        // GROOVY-2156
        Parameter[] parameters = node.getParameters();
        for (Parameter parameter : parameters) {
            visitAnnotations(parameter);
        }

        declare(node.getParameters(), node);
        visitClassCodeContainer(node.getCode());

        popState();
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        if (call.isImplicitThis() && call.getMethod() instanceof ConstantExpression) {
            ConstantExpression methodNameConstant = (ConstantExpression) call.getMethod();
            Object value = methodNameConstant.getText();

            if (!(value instanceof String)) {
                throw new GroovyBugError("tried to make a method call with a non-String constant method name.");
            }

            String methodName = (String) value;
            Variable v = checkVariableNameForDeclaration(methodName, call);
            if (v != null && !(v instanceof DynamicVariable)) {
                checkVariableContextAccess(v, call);
            }

            if (v instanceof VariableExpression || v instanceof Parameter) {
                VariableExpression object = new VariableExpression(v);
                object.setSourcePosition(methodNameConstant);
                call.setObjectExpression(object);
                ConstantExpression method = new ConstantExpression("call");
                method.setSourcePosition(methodNameConstant); // important for GROOVY-4344
                call.setImplicitThis(false);
                call.setMethod(method);
            }

        }
        super.visitMethodCallExpression(call);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        isSpecialConstructorCall = call.isSpecialCall();
        super.visitConstructorCallExpression(call);
        isSpecialConstructorCall = false;
        if (!call.isUsingAnonymousInnerClass()) return;

        pushState();
        InnerClassNode innerClass = (InnerClassNode) call.getType();
        innerClass.setVariableScope(currentScope);
        currentScope.setClassScope(innerClass);
        currentScope.setInStaticContext(false);
        for (MethodNode method : innerClass.getMethods()) {
            Parameter[] parameters = method.getParameters();
            if (parameters.length == 0) parameters = null; // null means no implicit "it"
            ClosureExpression cl = new ClosureExpression(parameters, method.getCode());
            visitClosureExpression(cl);
        }

        for (FieldNode field : innerClass.getFields()) {
            final Expression expression = field.getInitialExpression();
            pushState(field.isStatic());
            if (expression != null) {
                if (expression instanceof VariableExpression) {
                    VariableExpression vexp = (VariableExpression) expression;
                    if (vexp.getAccessedVariable() instanceof Parameter) {
                        // workaround for GROOVY-6834: accessing a parameter which is not yet seen in scope
                        popState();
                        continue;
                    }
                }
                expression.visit(this);
            }
            popState();
        }

        for (Statement statement : innerClass.getObjectInitializerStatements()) {
            statement.visit(this);
        }
        markClosureSharedVariables();
        popState();
    }

    public void visitProperty(PropertyNode node) {
        pushState(node.isStatic());
        super.visitProperty(node);
        popState();
    }

    public void visitField(FieldNode node) {
        pushState(node.isStatic());
        super.visitField(node);
        popState();
    }

    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotations = node.getAnnotations();
        if (annotations.isEmpty()) return;
        for (AnnotationNode an : annotations) {
            // skip built-in properties
            if (an.isBuiltIn()) continue;
            for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
                Expression annMemberValue = member.getValue();
                annMemberValue.visit(this);
            }
        }
    }
}
