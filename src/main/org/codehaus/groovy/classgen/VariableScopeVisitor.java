/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;

/**
 * goes through an AST and initializes the scopes
 *
 * @author Jochen Theodorou
 */
public class VariableScopeVisitor extends ClassCodeVisitorSupport {

    private static final Expression CALL = new ConstantExpression("call");

    private VariableScope currentScope = null;
    private VariableScope headScope = new VariableScope();
    private ClassNode currentClass = null;
    private SourceUnit source;
    private boolean inClosure = false;
    private boolean inPropertyExpression = false;
    private boolean isSpecialConstructorCall = false;
    private boolean inConstructor = false;
    private boolean inStaticConstructor = false;

    private LinkedList stateStack = new LinkedList();

    private class StateStackElement {
        VariableScope scope;
        ClassNode clazz;
        boolean closure;
        boolean inConstructor; 

        StateStackElement() {
            scope = VariableScopeVisitor.this.currentScope;
            clazz = VariableScopeVisitor.this.currentClass;
            closure = VariableScopeVisitor.this.inClosure;
            inConstructor = VariableScopeVisitor.this.inConstructor;
        }
    }

    public VariableScopeVisitor(SourceUnit source) {
        this.source = source;
        currentScope = headScope;
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
        // a scope in a closure is never really static
        // the checking needs this to be as the surrounding
        // method to correctly check the access to variables.
        // But a closure and all nested scopes are a result
        // of calling a non static method, so the context
        // is not static.
        if (inClosure) currentScope.setInStaticContext(false);

        StateStackElement element = (StateStackElement) stateStack.removeLast();
        currentScope = element.scope;
        currentClass = element.clazz;
        inClosure = element.closure;
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

        StringBuffer msg = new StringBuffer();
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
            if (scope.getClassScope() != null) break;

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
            if (pName != null && pName.equals(name))
                return new PropertyNode(pName, mn.getModifiers(), getPropertyType(mn), cn, null, null, null);
        }

        for (PropertyNode pn : cn.getProperties()) {
            if (pn.getName().equals(name)) return pn;
        }

        Variable ret = findClassMember(cn.getSuperClass(), name);
        if (ret != null) return ret;
        return findClassMember(cn.getOuterClass(), name);
    }

    private ClassNode getPropertyType(MethodNode m) {
        if (m.getReturnType() != ClassHelper.VOID_TYPE) {
            return m.getReturnType();
        }
        return m.getParameters()[0].getType();
    }

    private String getPropertyName(MethodNode m) {
        String name = m.getName();
        if (!(name.startsWith("set") || name.startsWith("get"))) return null;
        String pname = name.substring(3);
        if (pname.length() == 0) return null;
        pname = java.beans.Introspector.decapitalize(pname);

        if (name.startsWith("get") && (m.getReturnType() == ClassHelper.VOID_TYPE || m.getParameters().length != 0)) {
            return null;
        }
        if (name.startsWith("set") && m.getParameters().length != 1) {
            return null;
        }
        return pname;
    }

    // -------------------------------
    // different Variable based checks
    // -------------------------------

    private Variable checkVariableNameForDeclaration(String name, Expression expression) {
        if ("super".equals(name) || "this".equals(name)) return null;

        VariableScope scope = currentScope;
        Variable var = new DynamicVariable(name, currentScope.isInStaticContext());
        // try to find a declaration of a variable
        while (true) {
            Variable var1;
            var1 = scope.getDeclaredVariable(var.getName());

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
                    boolean staticScope = currentScope.isInStaticContext() || isSpecialConstructorCall;
                    boolean staticMember = member.isInStaticContext();
                    // We don't allow a static context (e.g. a static method) to access
                    // a non-static variable (e.g. a non-static field).
                    if (! (staticScope && ! staticMember))
                        var = member;
                }
                break;
            }
            scope = scope.getParent();
        }

        VariableScope end = scope;

        scope = currentScope;
        while (scope != end) {
            if (end.isClassScope() ||
                    (end.isReferencedClassVariable(name) && end.getDeclaredVariable(name) == null)) {
                scope.putReferencedClassVariable(var);
            } else {
                var.setClosureSharedVariable(var.isClosureSharedVariable() || inClosure);
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
        if (name.equals("class")) return;
        if (name == null) return;
        Variable member = findClassMember(currentClass, name);
        if (member == null) return;
        checkVariableContextAccess(member, pe);
    }

    private void checkVariableContextAccess(Variable v, Expression expr) {
        if (inPropertyExpression || v.isInStaticContext() || !currentScope.isInStaticContext()) return;

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

    public void visitDeclarationExpression(DeclarationExpression expression) {
        // visit right side first to avoid the usage of a
        // variable before its declaration
        expression.getRightExpression().visit(this);

        // no need to visit left side, just get the variable name
        if (expression.isMultipleAssignmentDeclaration()) {
            ArgumentListExpression list = (ArgumentListExpression) expression.getLeftExpression();
            for (Expression e : list.getExpressions()) {
                VariableExpression exp = (VariableExpression) e;
                declare(exp);
            }
        } else {
            declare(expression.getVariableExpression());
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
        boolean ipe = inPropertyExpression;
        inPropertyExpression = true;
        expression.getObjectExpression().visit(this);
        inPropertyExpression = false;
        expression.getProperty().visit(this);
        checkPropertyOnExplicitThis(expression);
        inPropertyExpression = ipe;
    }

    public void visitClosureExpression(ClosureExpression expression) {
        pushState();

        inClosure = true;

        expression.setVariableScope(currentScope);

        if (expression.isParameterSpecified()) {
            Parameter[] parameters = expression.getParameters();
            for (Parameter parameter : parameters) {
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
        popState();
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
    	// AIC are already done, doing them here again will lead
    	// to wrong scopes
    	if (node instanceof InnerClassNode) {
    		InnerClassNode in = (InnerClassNode) node;
    		if (in.isAnonymous()) return;
    	}
    	
        pushState();

        currentClass = node;
        currentScope.setClassScope(node);

        super.visitClass(node);
        popState();
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        pushState(node.isStatic());
        inConstructor = isConstructor;
        inStaticConstructor = node.isStaticConstructor();
        node.setVariableScope(currentScope);
        
        // GROOVY-2156
        Parameter[] parameters = node.getParameters();
        for (Parameter parameter : parameters) {
            visitAnnotations(parameter);
        }

        declare(node.getParameters(), node);

        super.visitConstructorOrMethod(node, isConstructor);
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
                call.setMethod(CALL);
            }

        }
        super.visitMethodCallExpression(call);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        isSpecialConstructorCall = call.isSpecialCall();
        super.visitConstructorCallExpression(call);
        isSpecialConstructorCall = false;
        if (!call.isUsingAnnonymousInnerClass()) return;

        pushState();
        InnerClassNode innerClass = (InnerClassNode) call.getType();
        innerClass.setVariableScope(currentScope);
        for (MethodNode method : innerClass.getMethods()) {
            Parameter[] parameters = method.getParameters();
            if (parameters.length == 0) parameters = null; // null means no implicit "it"
            ClosureExpression cl = new ClosureExpression(parameters, method.getCode());
            visitClosureExpression(cl);
        }

        boolean ic = inClosure;
        inClosure = true;
        for (FieldNode field : innerClass.getFields()) {
            final Expression expression = field.getInitialExpression();
            if (expression != null) {
                expression.visit(this);
            }
        }

        for (Statement statement : innerClass.getObjectInitializerStatements()) {
            statement.visit(this);
        }
        inClosure = ic;
        popState();
    }
    
    public void visitBinaryExpression(BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        switch (expression.getOperation().getType()){
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
                checkFinalFieldAccess(expression.getLeftExpression());
                break;
            default: break;
        }
        
    }

    private void checkFinalFieldAccess(Expression expression) {
        if (!(expression instanceof VariableExpression)) return;
        boolean isFinal, isStatic, error;
        int modifiers = 0;
        VariableExpression ve = (VariableExpression) expression;
        Variable v = ve.getAccessedVariable();
        if (v instanceof PropertyNode) {
            PropertyNode pn = (PropertyNode) v;
            modifiers = pn.getModifiers();
        } else if (v instanceof FieldNode) {
            FieldNode fn = (FieldNode) v;
            modifiers = fn.getModifiers();
        }
        /*
         *  if it is static final but not accessed inside a static constructor, or,
         *  if it is an instance final but not accessed inside a instance constructor, it is an error
         */
        isFinal = (modifiers & Opcodes.ACC_FINAL) != 0;
        isStatic = (modifiers & Opcodes.ACC_STATIC) != 0;
        error = isFinal && ((isStatic && !inStaticConstructor) || (!isStatic && !inConstructor));

        if (error) addError("cannnot access final field or property " +
                            "outside of constructor.", expression);
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

}
