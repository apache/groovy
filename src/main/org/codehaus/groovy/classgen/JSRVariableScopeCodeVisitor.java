/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.codehaus.groovy.classgen;

import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;

import org.codehaus.groovy.syntax.SyntaxException;

import groovy.lang.GroovyRuntimeException;


public class JSRVariableScopeCodeVisitor extends CodeVisitorSupport implements GroovyClassVisitor {
    private VariableScope currentScope = null;  
    private CompileUnit unit;
    private SourceUnit source;
    
    public JSRVariableScopeCodeVisitor(VariableScope scope, SourceUnit source) {
        currentScope = scope;
        this.source = source;
        this.unit = source.getAST().getUnit();
    }
    
    public void visitBlockStatement(BlockStatement block) {
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        super.visitBlockStatement(block);
        currentScope = scope;
    }
    
    public void visitForLoop(ForStatement forLoop) {
        VariableScope scope = currentScope;
        //TODO: always define a varibale here?
        declare(forLoop.getVariable(),forLoop);
        currentScope = new VariableScope(currentScope);
        super.visitForLoop(forLoop);
        currentScope = scope;
    }
    
    public void visitWhileLoop(WhileStatement loop) {
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        super.visitWhileLoop(loop);
        currentScope = scope;
    }
    
    public void visitDoWhileLoop(DoWhileStatement loop) {
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        super.visitDoWhileLoop(loop);
        currentScope = scope;
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        // visit right side first
        expression.getRightExpression().visit(this);
        // no need to visit left side, just get the varibale name
        String variable = expression.getVariableExpression().getVariable();
        declare(variable,expression);
    }
    
    private void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        source.addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException(msg+"\n",line,col)));
    }
    
    protected void declare(String name, ASTNode expr) {
        Set declares = currentScope.getDeclaredVariables();
        if (declares.contains(name)) {
            String msg = "The current scope does already contain a variable of the name "+name;
            addError(msg,expr);
        }
        declares.add(name);        
    }
    
    /*public void visitBinaryExpression(BinaryExpression expression) {
        // evaluate right first because for an expression like "def a = a"
        // we need first to know if the a on the rhs is defined, before 
        // defining a new a for the lhs
        Expression right = expression.getRightExpression();
        right.visit(this);
        Expression left = expression.getLeftExpression();
        left.visit(this);        
    }*/
    
    public void visitVariableExpression(VariableExpression expression) {
        checkVariableNameForDeclaration(expression.getVariable(),expression);
    }
    
    protected void checkVariableNameForDeclaration(String name, Expression expression) {
        if (expression==VariableExpression.THIS_EXPRESSION) return;
        //TODO: this line is not working
        //if (expression==VariableExpression.SUPER_EXPRESSION) return;
        if ("super".equals(name)) return;
        VariableScope scope = currentScope;
        while (scope!=null) {
            if (scope.getDeclaredVariables().contains(name)) break;
            if (scope.getReferencedVariables().contains(name)) break;
            //scope.getReferencedVariables().add(name);
            scope = scope.getParent();
        }
        VariableScope end = scope; 
        if (scope==null) {
            declare(name,expression);
            addError("The variable "+name+" is undefined in the current scope",expression);
        } else {
            scope = currentScope;
            while (scope!=end) {
                scope.getReferencedVariables().add(name);
                scope = scope.getParent();
            }
        }        
    }    
    
    public void visitClosureExpression(ClosureExpression expression) {
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        //TODO: set scope
        //expression.setVariableScope(currentScope);
        Set declares = currentScope.getDeclaredVariables();
        if (expression.isParameterSpecified()) {
            Parameter[] parameters = expression.getParameters();
            for (int i = 0; i<parameters.length; i++) {
                declares.add(parameters[i].getName());
            }
        } else {
            //TODO: when to add "it" and when not?
            declares.add("it");
        }
        //currentScope = new VariableScope(currentScope);
        super.visitClosureExpression(expression);
        currentScope = scope;
    }
    
    private String getPropertyName(String name) {
        if (! (name.startsWith("set") || name.startsWith("get")) ) return null;
        
        String pname = name.substring(3);
        if (pname.length()==0) return null;
        String s = pname.substring(0,1).toLowerCase();
        String rest = pname.substring(1);
        return s+rest;
    }        

    public void visitClass(ClassNode node) {
        //if (node instanceof InnerClassNode) return;
        //System.err.println("-------------"+hashCode()+":"+node.getName()+"-------------");
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        Set declares = currentScope.getDeclaredVariables();

        // first pass, add all possible variable names (properies and fields)
        //TODO: handle interfaces
        //TODO: handle static imports
        try {
            addVarNames(node,currentScope.getDeclaredVariables(),false);
            addVarNames(node.getOuterClass(),currentScope.getReferencedVariables(),true);
            addVarNames(node.getSuperClass(),currentScope.getReferencedVariables(),true);
        } catch (ClassNotFoundException cnfe) {
            //throw new GroovyRuntimeException("couldn't find super class",cnfe);
            cnfe.printStackTrace();
        }
        
        // second pass, check contents
        node.visitContents(this);
        currentScope = scope;
    }
    
    
    private void addVarNames(Class c, Set refs, boolean visitParent) throws ClassNotFoundException{
        if (c==null) return;
        // to prefer compiled code try to get a ClassNode via name first
        addVarNames(c.getName(),refs,visitParent);
    }
    
    private void addVarNames(ClassNode cn, Set refs, boolean visitParent) throws ClassNotFoundException{
        if (cn==null) return;
        List l = cn.getFields();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            FieldNode f = (FieldNode) iter.next();
            if (visitParent && Modifier.isPrivate(f.getModifiers())) continue;
            refs.add(f.getName());
        }

        l = cn.getMethods();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            MethodNode f = (MethodNode) iter.next();
            if (visitParent && Modifier.isPrivate(f.getModifiers())) continue;
            String name = getPropertyName(f.getName());
            if (name!=null) refs.add(name);             
        }
        
        l = cn.getProperties();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PropertyNode f = (PropertyNode) iter.next();
            if (visitParent && Modifier.isPrivate(f.getModifiers())) continue;
            refs.add(f.getName());
        }
           
        if (!visitParent) return; 
            
        addVarNames(cn.getSuperClass(),refs,visitParent);
        
        MethodNode enclosingMethod = cn.getEnclosingMethod();
        if (enclosingMethod==null) return;
        Parameter[] params = enclosingMethod.getParameters();
        for (int i = 0; i < params.length; i++) {
            refs.add(params[i].getName());
        }        
        if (visitParent) addVarNames(enclosingMethod.getDeclaringClass(),refs,visitParent);

        addVarNames(cn.getOuterClass(),refs,visitParent);
        
    }
    
    private void addVarNames(String superclassName, Set refs, boolean visitParent) throws ClassNotFoundException{
        if (superclassName==null) return;
        ClassNode cn = unit.getClass(superclassName);
        if (cn!=null) {
            addVarNames(cn,refs,visitParent);
            return;
        } 
        
        Class c = unit.getClassLoader().loadClass(superclassName);
        Field[] fields = c.getFields();
        for (int i=0; i<fields.length; i++) {
           Field f = fields[i];
           if (visitParent && Modifier.isPrivate(f.getModifiers())) continue;
           refs.add(f.getName());
        }
        Method[] methods = c.getMethods();
        for (int i=0; i<methods.length; i++) {
            Method m = methods[i];
            if (visitParent && Modifier.isPrivate(m.getModifiers())) continue;
            String name = getPropertyName(m.getName());
            if (name!=null) refs.add(name);
        }
        
        if (!visitParent) return;
            
        addVarNames(c.getSuperclass(),refs,visitParent);
        
        //it's not possible to know the variable names used for an enclosing method 
        addVarNames(c.getEnclosingClass(),refs,visitParent);
    }

    public void visitConstructor(ConstructorNode node) {
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        //TODO: set scope
        //node.setVariableScope(currentScope);
        Set declares = currentScope.getDeclaredVariables();
        Parameter[] parameters = node.getParameters();
        for (int i=0; i<parameters.length; i++) {
            declares.add(parameters[i].getName());
        }
        currentScope = new VariableScope(currentScope);
        Statement code = node.getCode();
        if (code!=null) code.visit(this);        
        currentScope = scope;
    }

    public void visitMethod(MethodNode node) {
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        //TODO: set scope
        //node.setVariableScope(currentScope);
        Set declares = currentScope.getDeclaredVariables();
        Parameter[] parameters = node.getParameters();
        for (int i=0; i<parameters.length; i++) {
            declares.add(parameters[i].getName());
        }        
        currentScope = new VariableScope(currentScope);
        node.getCode().visit(this);        
        currentScope = scope;
    }

    public void visitField(FieldNode node) {
        Expression init = node.getInitialValueExpression();
        if (init!=null) init.visit(this);
    }

    public void visitProperty(PropertyNode node) {
        Statement statement = node.getGetterBlock();
        if (statement!=null) statement.visit(this);
        statement = node.getSetterBlock();
        if (statement!=null) statement.visit(this);
        Expression init = node.getInitialValueExpression();
        if (init!=null) init.visit(this);
    }
    
    public void visitPropertyExpression(PropertyExpression expression) {

    }
    
    public void visitCatchStatement(CatchStatement statement) {
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        declare(statement.getVariable(),statement);
        super.visitCatchStatement(statement);
        currentScope = scope;
    }
}