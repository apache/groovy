/*
 * JSRVariableScopeCodeVisitor.java created on 11.04.2005
 *
 */
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

public class JSRVariableScopeCodeVisitor extends CodeVisitorSupport implements GroovyClassVisitor {
    private VariableScope currentScope = null;  
    private ClassLoader loader;
    
    public JSRVariableScopeCodeVisitor(VariableScope scope, ClassLoader classLoader) {
        currentScope = scope;
        loader = classLoader;
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
    
    protected void declare(String name, ASTNode expr) {
        Set declares = currentScope.getDeclaredVariables();
        if (declares.contains(name)) {
            throw new GroovyRuntimeException("The current scope does already contain a variable of the name "+name,expr);
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
        if (name.equals("this")) return;
        if (name.equals("super")) return;
        VariableScope scope = currentScope;
        while (scope!=null) {
            if (scope.getDeclaredVariables().contains(name)) break;
            if (scope.getReferencedVariables().contains(name)) break;
            scope.getReferencedVariables().add(name);
            scope = scope.getParent();
        }
        if (scope==null) {
            throw new GroovyRuntimeException("The variable "+name+" is undefined in the current scope",expression);
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

    public void visitClass(ClassNode node) {
        //System.err.println("-------------"+hashCode()+":"+node.getName()+"-------------");
        VariableScope scope = currentScope;
        currentScope = new VariableScope(currentScope);
        Set declares = currentScope.getDeclaredVariables();

        // first pass, add all possible variable names (properies and fields)
        for (Iterator iter = node.getProperties().iterator(); iter.hasNext();) {
            PropertyNode element = (PropertyNode) iter.next();
            //System.err.println(node.getName()+"#"+element.getName());
            declares.add(element.getName());
        }
        for (Iterator iter = node.getFields().iterator(); iter.hasNext();) {
            FieldNode element = (FieldNode) iter.next();
            declares.add(element.getName());
        }
        for (Iterator iter = node.getAllDeclaredMethods().iterator(); iter.hasNext();) {
            MethodNode element = (MethodNode) iter.next();
            if (element.getName().startsWith("set") || element.getName().startsWith("get")) {
                String name = element.getName().substring(3);
                if (name.length()==0) continue;
                String s = name.substring(0,1).toLowerCase();
                String rest = name.substring(1);
                declares.add(s+rest);
            }
        }        
        
        //TODO: handle interfaces
        //TODO: handle static imports
        Set refs = currentScope.getReferencedVariables();
        //System.err.println("checking superclasses for "+node.getName());
        try {
            Class c = loader.loadClass(node.getSuperClass());
            while (c!=null) {
                //System.err.println("superclass:"+c.getName());
                Field[] fields = c.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    if (Modifier.isPrivate(f.getModifiers())) continue;
                    refs.add(f.getName());
                }
                Method[] methods = c.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method m = methods[i];
                    if (Modifier.isPrivate(m.getModifiers())) continue;
                    if (m.getName().startsWith("set") || m.getName().startsWith("get")) {
                        String name = m.getName().substring(3);
                        if (name.length()==0) continue;
                        String s = name.substring(0,1).toLowerCase();
                        String rest = name.substring(1);
                        refs.add(s+rest);
                    }
                }
                
                c = c.getSuperclass();
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        
        // second pass, check contents
        node.visitContents(this);
        currentScope = scope;
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
