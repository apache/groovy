/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.classgen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.SourceUnit;

/**
 * goes through an AST and initializes the scopes 
 * @author Jochen Theodorou
 */
public class VariableScopeVisitor extends ClassCodeVisitorSupport {
    private VariableScope currentScope = null;
    private VariableScope headScope = new VariableScope();
    private ClassNode currentClass=null;
    private SourceUnit source;
    private boolean inClosure=false;
    
    private LinkedList stateStack=new LinkedList();
    
    private class StateStackElement {
        VariableScope scope;
        ClassNode clazz;
        boolean dynamic;
        boolean closure;
        
        StateStackElement() {
            scope = VariableScopeVisitor.this.currentScope;
            clazz = VariableScopeVisitor.this.currentClass;
            closure = VariableScopeVisitor.this.inClosure;
        }
    }
    
    public VariableScopeVisitor(SourceUnit source) {
        this.source = source;
        currentScope  = headScope;
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
            declare(parameters[i],node);
        }
    }        
    
    private void declare(VariableExpression expr) {
        declare(expr,expr);
    }
    
    private void declare(Variable var, ASTNode expr) {
        String scopeType = "scope";
        String variableType = "variable";
        
        if (expr.getClass()==FieldNode.class){
            scopeType = "class"; 
            variableType = "field";
        } else if (expr.getClass()==PropertyNode.class){
            scopeType = "class"; 
            variableType = "property";
        }
        
        StringBuffer msg = new StringBuffer();
        msg.append("The current ").append(scopeType);
        msg.append(" does already contain a ").append(variableType);
        msg.append(" of the name ").append(var.getName());
        
        if (currentScope.getDeclaredVariable(var.getName())!=null) {
            addError(msg.toString(),expr);
            return;
        }
        
        for (VariableScope scope = currentScope.getParent(); scope!=null; scope = scope.getParent()) {
            // if we are in a class and no variable is declared until
            // now, then we can break the loop, because we are allowed
            // to declare a variable of the same name as a class member
            if (scope.getClassScope()!=null) break;
            
            Map declares = scope.getDeclaredVariables();
            if (declares.get(var.getName())!=null) {
                // variable already declared
                addError(msg.toString(), expr);
                break;
            }
        }
        // declare the variable even if there was an error to allow more checks
        currentScope.getDeclaredVariables().put(var.getName(),var);
    }
    
    protected SourceUnit getSourceUnit() {
        return source;
    }
    
    private Variable findClassMember(ClassNode cn, String name) {
        if (cn == null) return null;
        if (cn.isScript()) {
            return new DynamicVariable(name,false);
        }
        List l = cn.getFields();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            FieldNode f = (FieldNode) iter.next();
            if (f.getName().equals(name)) return f;
        }

        l = cn.getMethods();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            MethodNode f =(MethodNode) iter.next();
            String methodName = f.getName();
            String pName = getPropertyName(f);
            if (pName == null) continue; 
            if (!pName.equals(name)) continue;
            PropertyNode var = new PropertyNode(pName,f.getModifiers(),getPropertyType(f),cn,null,null,null);
            return var;
        }

        l = cn.getProperties();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PropertyNode f = (PropertyNode) iter.next();
            if (f.getName().equals(name)) return f;
        }
        
        Variable ret = findClassMember(cn.getSuperClass(),name);
        if (ret!=null) return ret;
        return findClassMember(cn.getOuterClass(),name); 
    }
    
    private ClassNode getPropertyType(MethodNode m) {
        String name = m.getName();
        if (m.getReturnType()!=ClassHelper.VOID_TYPE) {
            return m.getReturnType();
        }
        return m.getParameters()[0].getType();
    }

    private String getPropertyName(MethodNode m) {
        String name = m.getName();
        if (!(name.startsWith("set") || name.startsWith("get"))) return null;
        String pname = name.substring(3);
        if (pname.length() == 0) return null;
        String s = pname.substring(0, 1).toLowerCase();
        String rest = pname.substring(1);
        pname = s + rest;
        
        if (name.startsWith("get") && m.getReturnType()==ClassHelper.VOID_TYPE) {
            return null;
        }
        if (name.startsWith("set") && m.getParameters().length!=1) {
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
        Variable var = new DynamicVariable(name,currentScope.isInStaticContext());
        Variable dummyStart = var;
        // try to find a declaration of a variable
        VariableScope dynamicScope = null;
        while (!scope.isRoot()) {
            if (dynamicScope==null && scope.isResolvingDynamic()) {
                dynamicScope = scope;
            }
            
            Map declares = scope.getDeclaredVariables();
            if (declares.get(var.getName())!=null) {
                var = (Variable) declares.get(var.getName());
                break;
            }
            Map localReferenced = scope.getReferencedLocalVariables(); 
            if (localReferenced.get(var.getName())!=null) {
                var = (Variable) localReferenced.get(var.getName());
                break;
            }

            Map classReferenced = scope.getReferencedClassVariables(); 
            if (classReferenced.get(var.getName())!=null) {
                var = (Variable) classReferenced.get(var.getName());
                break;
            }
            
            ClassNode classScope = scope.getClassScope();
            if (classScope!=null) {
                Variable member = findClassMember(classScope,var.getName());
                if (member!=null && (currentScope.isInStaticContext() ^ member instanceof DynamicVariable)) var = member;
                break;
            }            
            scope = scope.getParent();
        }

        VariableScope end = scope;

        if (scope.isRoot() && dynamicScope==null) {
            // no matching scope found
            declare(var,expression);
            addError("The variable " + var.getName() +
                     " is undefined in the current scope", expression);
        } else if (scope.isRoot() && dynamicScope!=null) {
            // no matching scope found, but there was a scope that
            // resolves dynamic
            scope = dynamicScope;
        } 
        
        if (!scope.isRoot()) {
            scope = currentScope;
            while (scope != end) {
                Map references = null;
                if (end.isClassScope() || end.isRoot() || 
                        (end.isReferencedClassVariable(name) && end.getDeclaredVariable(name)==null)) 
                {
                    references = scope.getReferencedClassVariables();
                } else {
                    references = scope.getReferencedLocalVariables();
                    var.setClosureSharedVariable(var.isClosureSharedVariable() || inClosure);
                }
                references.put(var.getName(),var);
                scope = scope.getParent();
            }
            if (end.isResolvingDynamic()) {
                if (end.getDeclaredVariable(var.getName())==null) {
                    end.getDeclaredVariables().put(var.getName(),var);
                }
            }
        }
        
        return var;
    }
    
    private void checkVariableContextAccess(Variable v, Expression expr) {
        if (v.isInStaticContext() || !currentScope.isInStaticContext()) return;        
        
        String msg =  v.getName()+
                      " is declared in a dynamic context, but you tried to"+
                      " access it from a static context.";
        addError(msg,expr);
        
        // declare a static variable to be able to continue the check
        DynamicVariable v2 = new DynamicVariable(v.getName(),currentScope.isInStaticContext());
        currentScope.getDeclaredVariables().put(v.getName(),v2);
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
        declare(p, forLoop);        
        super.visitForLoop(forLoop);
        popState();
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        // visit right side first to avoid the usage of a 
        // variable before its declaration
        expression.getRightExpression().visit(this);
        // no need to visit left side, just get the variable name
        VariableExpression vex = expression.getVariableExpression();
        vex.setInStaticContext(currentScope.isInStaticContext());
        declare(vex);
        vex.setAccessedVariable(vex);
    }
    
    public void visitVariableExpression(VariableExpression expression) {
        String name = expression.getName();
        Variable v = checkVariableNameForDeclaration(name,expression);
        if (v==null) return;
        expression.setAccessedVariable(v);
        checkVariableContextAccess(v,expression);
    }
    
    public void visitClosureExpression(ClosureExpression expression) {
        pushState();

        inClosure=true;
        // as result of the Paris meeting Closure resolves
        // always dynamically
        currentScope.setDynamicResolving(true);
        
        expression.setVariableScope(currentScope);

        if (expression.isParameterSpecified()) {
            Parameter[] parameters = expression.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].setInStaticContext(currentScope.isInStaticContext());
                declare(parameters[i],expression);
            }
        } else if (expression.getParameters()!=null){
            DynamicVariable var = new DynamicVariable("it",currentScope.isInStaticContext());
            currentScope.getDeclaredVariables().put("it",var);
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
        Variable v = checkVariableNameForDeclaration(name,expression);
        checkVariableContextAccess(v,expression);  
    }
    
    // ------------------------------
    // class visit  
    // ------------------------------
    
    public void visitClass(ClassNode node) {
        pushState();
        boolean dynamicMode = node.isScript();
        currentScope.setDynamicResolving(dynamicMode);
        currentScope.setClassScope(node);
        
        super.visitClass(node);
        popState();
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        pushState(node.isStatic());
        
        node.setVariableScope(currentScope);
        declare(node.getParameters(),node);
        
        super.visitConstructorOrMethod(node, isConstructor);
        popState();
    }
    
    public void visitMethodCallExpression(MethodCallExpression call) {
    	if (call.isImplicitThis() && call.getMethod() instanceof ConstantExpression) {
            Object value = ((ConstantExpression) call.getMethod()).getText();
            if (! (value instanceof String)) {
                throw new GroovyBugError("tried to make a method call with an constant as"+
                                         " name, but the constant was no String.");
            }
            String methodName = (String) value;
	        Variable v = checkVariableNameForDeclaration(methodName,call);
	        if (v!=null && !(v instanceof DynamicVariable)) {
	            checkVariableContextAccess(v,call);
	        }
    	}
        super.visitMethodCallExpression(call);
    }

}
