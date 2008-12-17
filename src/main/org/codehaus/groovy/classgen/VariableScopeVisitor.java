/*
 * Copyright 2003-2007 the original author or authors.
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
import org.codehaus.groovy.control.SourceUnit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private LinkedList stateStack = new LinkedList();

    private class StateStackElement {
        VariableScope scope;
        ClassNode clazz;
        boolean closure;

        StateStackElement() {
            scope = VariableScopeVisitor.this.currentScope;
            clazz = VariableScopeVisitor.this.currentClass;
            closure = VariableScopeVisitor.this.inClosure;
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
    }

    private void declare(Parameter[] parameters, ASTNode node) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].hasInitialExpression()) {
                parameters[i].getInitialExpression().visit(this);
            }
            declare(parameters[i], node);
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
        List l = cn.getFields();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            FieldNode f = (FieldNode) iter.next();
            if (f.getName().equals(name)) return f;
        }

        l = cn.getMethods();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            MethodNode f = (MethodNode) iter.next();
            String methodName = f.getName();
            String pName = getPropertyName(f);
            if (pName == null) continue;
            if (!pName.equals(name)) continue;
            PropertyNode var = new PropertyNode(pName, f.getModifiers(), getPropertyType(f), cn, null, null, null);
            return var;
        }

        l = cn.getProperties();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PropertyNode f = (PropertyNode) iter.next();
            if (f.getName().equals(name)) return f;
        }

        Variable ret = findClassMember(cn.getSuperClass(), name);
        if (ret != null) return ret;
        return findClassMember(cn.getOuterClass(), name);
    }

    private ClassNode getPropertyType(MethodNode m) {
        String name = m.getName();
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

        if (name.startsWith("get") && m.getReturnType() == ClassHelper.VOID_TYPE) {
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
        Variable dummyStart = var;
        // try to find a declaration of a variable
        VariableScope dynamicScope = null;
        while (!scope.isRoot()) {
            if (dynamicScope == null && scope.isResolvingDynamic()) {
                dynamicScope = scope;
            }

            Variable var1;
            var1 = scope.getDeclaredVariable(var.getName());

            if (var1 != null) {
                var = var1;
                break;
            }

            var1 = (Variable) scope.getReferencedLocalVariable(var.getName());
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
                    boolean cc = currentScope.isInStaticContext() || isSpecialConstructorCall;
                    boolean cm = member.isInStaticContext();
                    //
                    // we don't allow access from dynamic context to static context
                    //
                    // cm==cc: 
                    //   we always allow access if the context is in both cases static 
                    //   or dynamic
                    // cm==true: 
                    //   the member is static, which means access is always allowed
                    // cm||cm==cc:
                    //   is false only for the case cc==true and cm==false, which means
                    //   the member is a dynamic context, but the current scope is static.
                    //
                    // One example for (cm||cm==cc)==false is a static method trying to 
                    // access a non static field.
                    //
                    if (cm || cm == cc) var = member;
                }
                break;
            }
            scope = scope.getParent();
        }

        VariableScope end = scope;

        if (scope.isRoot() && dynamicScope == null) {
            // no matching scope found
            declare(var, expression);
            addError("The variable " + var.getName() +
                    " is undefined in the current scope", expression);
        } else if (scope.isRoot() && dynamicScope != null) {
            // no matching scope found, but there was a scope that
            // resolves dynamic
            scope = dynamicScope;
        }

        if (!scope.isRoot()) {
            scope = currentScope;
            while (scope != end) {
                Map references = null;
                if (end.isClassScope() || end.isRoot() ||
                        (end.isReferencedClassVariable(name) && end.getDeclaredVariable(name) == null)) {
                    scope.putReferencedClassVariable(var);
                } else {
                    var.setClosureSharedVariable(var.isClosureSharedVariable() || inClosure);
                    scope.putReferencedLocalVariable(var);
                }
                scope = scope.getParent();
            }
            if (end.isResolvingDynamic()) {
                if (end.getDeclaredVariable(var.getName()) == null) {
                    end.putDeclaredVariable(var);
                }
            }
        }

        return var;
    }

    /**
     * a property on "this", like this.x is transformed to a
     * direct field access, so we need to check the
     * static context here
     */
    private void checkPropertyOnExplicitThis(PropertyExpression pe) {
        if (!currentScope.isInStaticContext()) return;
        Expression object = pe.getObjectExpression();
        if (!(object instanceof VariableExpression)) return;
        VariableExpression ve = (VariableExpression) object;
        if (!ve.getName().equals("this")) return;
        String name = pe.getPropertyAsString();
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
        Parameter p = (Parameter) forLoop.getVariable();
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
            for (Iterator it=list.getExpressions().iterator(); it.hasNext();) {
                VariableExpression exp = (VariableExpression) it.next();
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
        // as result of the Paris meeting Closure resolves
        // always dynamically
        currentScope.setDynamicResolving(true);

        expression.setVariableScope(currentScope);

        if (expression.isParameterSpecified()) {
            Parameter[] parameters = expression.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].setInStaticContext(currentScope.isInStaticContext());
                if (parameters[i].hasInitialExpression()) {
                    parameters[i].getInitialExpression().visit(this);
                }
                declare(parameters[i], expression);                
            }
        } else if (expression.getParameters() != null) {
            Parameter var = new Parameter(ClassHelper.OBJECT_TYPE,"it");
            var.setInStaticContext(currentScope.isInStaticContext());
            currentScope.putDeclaredVariable(var);
        }

        super.visitClosureExpression(expression);
        popState();
    }

    public void visitCatchStatement(CatchStatement statement) {
        pushState();
        Parameter p = (Parameter) statement.getVariable();
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
        pushState();

        currentClass = node;
        boolean dynamicMode = node.isScript();
        currentScope.setDynamicResolving(dynamicMode);
        currentScope.setClassScope(node);

        super.visitClass(node);
        popState();
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        pushState(node.isStatic());

        node.setVariableScope(currentScope);

        // GROOVY-2156
        Parameter[] parameters = node.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
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
