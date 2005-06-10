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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
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

public class JSRVariableScopeCodeVisitor extends CodeVisitorSupport implements GroovyClassVisitor {

    private static class Var {
        //TODO: support final and native
        boolean isStatic=false, isFinal=false, isDynamicTyped=false;
        String name, type=null;
        Class typeClass=null;
        boolean isInStaticContext=false;
        
        public boolean equals(Object o){
            Var v = (Var) o;
            return v.name.equals(name);
        }
        
        public int hashCode() {
            return name.hashCode();
        }
        
        public Var(String name) {
            // a Variable without type and other modifiers
            // make it dynamic type, non final and non static
            this.name=name;
        }
        
        public Var(VariableExpression ve, VarScope scope) {
            name = ve.getVariable();
            if(ve.isDynamic()) {
                isDynamicTyped=true;
            } else {
                type = ve.getType();
                typeClass = ve.getTypeClass();
            }
            isInStaticContext = scope.isInStaticContext;
        }
        
        public Var(Parameter par, boolean methodIsStatic) {
            name = par.getName();
            if (par.isDynamicType()) {
                isDynamicTyped=true;
            } else {
                type = par.getType();
            }
            isInStaticContext = methodIsStatic;
        }

        public Var(FieldNode f) {
            name = f.getName();
            if (f.isDynamicType()) {
                isDynamicTyped=true;
            } else {
                type = f.getType();
            }
            isStatic=f.isStatic();
            isInStaticContext = isStatic;
        }

        public Var(String pName, MethodNode f) {
            name = pName;
            if (f.isDynamicReturnType()) {
                isDynamicTyped=true;
            } else {
                type = f.getReturnType();
            }
            isStatic=f.isStatic();
            isInStaticContext = isStatic;
        }
        
        public Var(String pName, Method m) {
            name = pName;
            typeClass = m.getReturnType();            
            isStatic=Modifier.isStatic(m.getModifiers());
            isFinal=Modifier.isFinal(m.getModifiers());
            isInStaticContext = isStatic;
        }

        public Var(PropertyNode f) {
            //TODO: no static? What about read-/write-only? abstract?
            isInStaticContext = false;
            name = f.getName();
            if (f.isDynamicType()) {
                isDynamicTyped=true;
            } else {
                type = f.getType();
            }
            isInStaticContext = false;
        }

        public Var(Field f) {
            name = f.getName();
            typeClass = f.getType();            
            isStatic=Modifier.isStatic(f.getModifiers());
            isInStaticContext = isStatic;
            isFinal=Modifier.isFinal(f.getModifiers());
            isInStaticContext = isStatic;
        }

        public Var(Var v) {
            isStatic=v.isStatic;
            isFinal=v.isFinal;
            isDynamicTyped=v.isDynamicTyped;
            name=v.name;
            type=v.type;
            typeClass=v.typeClass;
            isInStaticContext=v.isInStaticContext;
        }        
    }
    
    private static class VarScope {
        boolean isClass=true;
        boolean isInStaticContext = false;
        
        VarScope parent;
        HashMap declares = new HashMap();
        HashMap visibles = new HashMap();
        
        public VarScope(boolean isClass, VarScope parent, boolean staticContext) {
            this.isClass=isClass;
            this.parent = parent;
            isInStaticContext = staticContext;
        }
        
        public VarScope(VarScope parent, boolean staticContext) {
            this(false,parent,staticContext);
        }
        
        public VarScope(VarScope parent) {
            this(false,parent,parent!=null?parent.isInStaticContext:false);
        }
    }
    
    private static class JRoseCheck  extends CodeVisitorSupport{
        boolean closureStarted=false;
        boolean itUsed=false;
        
        public void visitClosureExpression(ClosureExpression expression) {
            // don't visit subclosures if already in a closure
            if (closureStarted) return;
            closureStarted=true;
            Parameter[] param = expression.getParameters();
            for (int i=0; i<param.length; i++) {
                itUsed = (param[i].getName().equals("it")) && closureStarted || itUsed;
            }
            super.visitClosureExpression(expression);
        }
        
        public void visitVariableExpression(VariableExpression expression) {
            itUsed = (expression.getVariable().equals("it")) && closureStarted || itUsed;
        }
        
    }
    
    
    private VarScope currentScope = null;
    private CompileUnit unit;
    private SourceUnit source; 
    private boolean scriptMode=false;
    private ClassNode currentClass=null;
    
    private boolean jroseRule=false;
    
    public JSRVariableScopeCodeVisitor(VarScope scope, SourceUnit source) {
        //System.out.println("scope check enabled");
        if ("true".equals(System.getProperty("groovy.jsr.check.rule.jrose"))) {
            jroseRule=true;
            //System.out.println("jrose check enabled");
        }
        currentScope = scope;
        this.source = source;
        if (source.getAST() == null) return;
        this.unit = source.getAST().getUnit();
    }

    public void visitBlockStatement(BlockStatement block) {
        VarScope scope = currentScope;
        currentScope = new VarScope(currentScope);
        super.visitBlockStatement(block);
        currentScope = scope;
    }

    public void visitForLoop(ForStatement forLoop) {
        VarScope scope = currentScope;
        // TODO: always define a variable here? What about type?
        currentScope = new VarScope(currentScope);
        declare(new Var(forLoop.getVariable()), forLoop);
        super.visitForLoop(forLoop);
        currentScope = scope;
    }

    public void visitWhileLoop(WhileStatement loop) {
        //TODO: check while loop variables
        VarScope scope = currentScope;
        currentScope = new VarScope(currentScope);
        super.visitWhileLoop(loop);
        currentScope = scope;
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        //TODO: still existant?
        VarScope scope = currentScope;
        currentScope = new VarScope(currentScope);
        super.visitDoWhileLoop(loop);
        currentScope = scope;
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        // visit right side first to avoid the usage of a 
        // variable before its declaration
        expression.getRightExpression().visit(this);
        // no need to visit left side, just get the variable name
        VariableExpression vex = expression.getVariableExpression();
        if (!jroseRule && "it".equals(vex.getVariable())) {
            // we are not in jrose mode, so don't allow variables 
            // of the name 'it'
            addError("'it' is a keyword in this mode.",vex);
        } else {
            declare(vex);
        }
    }
    
    private void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        source.getErrorCollector().addErrorAndContinue(
          new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
    }

    private void declare(VariableExpression expr) {
        declare(new Var(expr,currentScope),expr);
    }
    
    private void declare(Var var, ASTNode expr) {
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
        msg.append(" of the name ").append(var.name);
        
        if (currentScope.declares.get(var.name)!=null) {
            addError(msg.toString(),expr);
            return;
        }
        
        //TODO: this case is not visited I think
        if (currentScope.isClass) {
            currentScope.declares.put(var.name,var);
        }
        
        for (VarScope scope = currentScope.parent; scope!=null; scope = scope.parent) {
            HashMap declares = scope.declares;
            if (scope.isClass) break;
            if (declares.get(var.name)!=null) {
                // variable already declared
                addError(msg.toString(), expr);
                break;
            }
        }
        // declare the variable even if there was an error to allow more checks
        currentScope.declares.put(var.name,var);
        var.isInStaticContext = currentScope.isInStaticContext;
    }

    /*
     * public void visitBinaryExpression(BinaryExpression expression) {
     *  // evaluate right first because for an expression like "def a = a"
     *  // we need first to know if the a on the rhs is defined, before
     *  // defining a new a for the lhs
     * 
     * Expression right = expression.getRightExpression();
     * 
     * right.visit(this);
     * 
     * Expression left = expression.getLeftExpression();
     * 
     * left.visit(this);
     *  }
     */
    
    public void visitVariableExpression(VariableExpression expression) {
        String name = expression.getVariable();
        Var v = checkVariableNameForDeclaration(name,expression);
        if (v==null) return;
        checkVariableContextAccess(v,expression);
    }
    
    
    public void visitFieldExpression(FieldExpression expression) {
        String name = expression.getFieldName();
        //TODO: change that to get the correct scope
        Var v = checkVariableNameForDeclaration(name,expression);
        checkVariableContextAccess(v,expression);  
    }
    
    private void checkAbstractDeclaration(MethodNode methodNode) {
        if (!Modifier.isAbstract(methodNode.getModifiers())) return;
        if (Modifier.isAbstract(currentClass.getModifiers())) return;
        addError("Can't have an abstract method in a non abstract class." +
                 " The class '" + currentClass.getName() +  "' must be declared abstract or the method '" +
                 methodNode.getName() + "' must not be abstract.",methodNode);
    }
    
    private String getTypeName(String name) {
        if (!name.endsWith("[]")) return name;
        
        String prefix = "";
        while (name.endsWith("[]")) {
            name = name.substring(0,name.length()-2);
            prefix = "[";
        }
        
        if (name.equals("int")) {
            return prefix + "I";
        }
        else if (name.equals("long")) {
            return prefix + "J";
        }
        else if (name.equals("short")) {
            return prefix + "S";
        }
        else if (name.equals("float")) {
            return prefix + "F";
        }
        else if (name.equals("double")) {
            return prefix + "D";
        }
        else if (name.equals("byte")) {
            return prefix + "B";
        }
        else if (name.equals("char")) {
            return prefix + "C";
        }
        else if (name.equals("boolean")) {
            return prefix + "Z";
        }
        
        throw new AssertionError(false);
    }
    
    private boolean hasEqualParameterTypes(Parameter[] first, Parameter[] second) {
        if (first.length!=second.length) return false;
        for (int i=0; i<first.length; i++) {
            String ft = getTypeName(first[i].getType());
            String st = getTypeName(second[i].getType());
            if (ft.equals(st)) continue;
            return false;
        }        
        return true; 
    }
    
    private void checkClassForOverwritingFinal(ClassNode cn) {
        ClassNode superCN = cn.getSuperClassNode();
        if (superCN==null) return;
        if (!Modifier.isFinal(superCN.getModifiers())) return;
        StringBuffer msg = new StringBuffer();
        msg.append("you are not allowed to overwrite the final class ");
        msg.append(superCN.getName());
        msg.append(".");
        addError(msg.toString(),cn);
        
    }
    
    private void checkMethodsForOverwritingFinal(ClassNode cn) {
        List l = cn.getMethods();     
        for (Iterator cnIter = l.iterator(); cnIter.hasNext();) {
            MethodNode method =(MethodNode) cnIter.next();
            Parameter[] parameters = method.getParameters();
            for (ClassNode superCN = cn.getSuperClassNode(); superCN!=null; superCN=superCN.getSuperClassNode()){
                List methods = superCN.getMethods(method.getName());
                for (Iterator iter = methods.iterator(); iter.hasNext();) {
                    MethodNode m = (MethodNode) iter.next();
                    Parameter[] np = m.getParameters();
                    if (hasEqualParameterTypes(parameters,np)) continue;
                    if (!Modifier.isFinal(m.getModifiers())) return;
                    
                    StringBuffer msg = new StringBuffer();
                    msg.append("you are not allowed to overwrite the final method ").append(method.getName());
                    msg.append("(");
                    boolean semi = false;
                    for (int i=0; i<parameters.length;i++) {
                        if (semi) {
                            msg.append(",");
                        } else {
                            semi = true;
                        }
                        msg.append(parameters[i].getType());
                    }
                    msg.append(")");
                    msg.append(" from class ").append(superCN.getName()); 
                    msg.append(".");
                    addError(msg.toString(),method);
                    return;
                }
            }
        }        
    }
    
    private void checkVariableContextAccess(Var v, Expression expr) {
        if (v.isInStaticContext || !currentScope.isInStaticContext) return;        
        
        String msg =  v.name+
                      " is declared in a dynamic context, but you tried to"+
                      " access it from a static context.";
        addError(msg,expr);
        
        // decalre a static variable to be able to continue the check
        Var v2 = new Var(v);
        v2.isInStaticContext = true;
        currentScope.declares.put(v2.name,v2);
    }
    
    private Var checkVariableNameForDeclaration(VariableExpression expression) {
        if (expression == VariableExpression.THIS_EXPRESSION) return null;
        String name = expression.getVariable();
        return checkVariableNameForDeclaration(name,expression);
    }
    
    private Var checkVariableNameForDeclaration(String name, Expression expression) {
        Var var = new Var(name);
        
        // TODO: this line is not working
        // if (expression==VariableExpression.SUPER_EXPRESSION) return;
        if ("super".equals(var.name) || "this".equals(var.name)) return null;
        
        VarScope scope = currentScope;
        while (scope != null) {
            if (scope.declares.get(var.name)!=null) {
                var = (Var) scope.declares.get(var.name);
                break;
            }
            if (scope.visibles.get(var.name)!=null) {
                var = (Var) scope.visibles.get(var.name);
                break;
            }
            // scope.getReferencedVariables().add(name);
            scope = scope.parent;
        }

        VarScope end = scope;

        if (scope == null) {
            declare(var,expression);
            // don't create an error when inside a script body 
            if (!scriptMode) addError("The variable " + var.name +
                                      " is undefined in the current scope", expression);
        } else {
            scope = currentScope;
            while (scope != end) {
                scope.visibles.put(var.name,var);
                scope = scope.parent;
            }
        }
        
        return var;
    }

    public void visitClosureExpression(ClosureExpression expression) {
        VarScope scope = currentScope;
        currentScope = new VarScope(!jroseRule,currentScope,scope.isInStaticContext);
    
        // TODO: set scope
        // expression.setVarScope(currentScope);

        if (expression.isParameterSpecified()) {
            Parameter[] parameters = expression.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                declare(new Var(parameters[i],false),expression);
            }
        } else {
            // TODO: when to add "it" and when not?
            // John's rule is to add it only to the closures using 'it'
            // and only to the closure itself, not to subclosures
            if (jroseRule) {
                JRoseCheck check = new JRoseCheck();
                expression.visit(check);
                if (check.itUsed) declare(new Var("it"),expression);
            } else {
                currentScope.declares.put("it",new Var("it"));
            }
        }

        // currentScope = new VarScope(currentScope);
        super.visitClosureExpression(expression);
        currentScope = scope;
    }

    public void visitClass(ClassNode node) {
        checkClassForOverwritingFinal(node);
        checkMethodsForOverwritingFinal(node);
        VarScope scope = currentScope;
        currentScope = new VarScope(true,currentScope,false);
        boolean scriptModeBackup = scriptMode;
        scriptMode = node.isScript();
        ClassNode classBackup = currentClass;
        currentClass = node;
        
        HashMap declares = currentScope.declares;
        // first pass, add all possible variable names (properies and fields)
        // TODO: handle interfaces
        // TODO: handle static imports
        try {
            addVarNames(node);
            addVarNames(node.getOuterClass(), currentScope.visibles, true);
            addVarNames(node.getSuperClass(), currentScope.visibles, true);
        } catch (ClassNotFoundException cnfe) {
            //TODO: handle this case properly
            // throw new GroovyRuntimeException("couldn't find super
            // class",cnfe);
            cnfe.printStackTrace();
        }
       
        // second pass, check contents
        node.visitContents(this);
        
        currentClass = classBackup;
        currentScope = scope;
        scriptMode = scriptModeBackup;
    }

    private void addVarNames(Class c, HashMap refs, boolean visitParent)
            throws ClassNotFoundException 
    {
        if (c == null) return;
        // to prefer compiled code try to get a ClassNode via name first
        addVarNames(c.getName(), refs, visitParent);
    }
    
    private void addVarNames(ClassNode cn) {
        //TODO: change test for currentScope.declares
        //TODO: handle indexed properties
        if (cn == null) return;
        List l = cn.getFields();
        Set fields = new HashSet();        
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            FieldNode f = (FieldNode) iter.next();
            Var var = new Var(f);
            if (fields.contains(var)) {
                declare(var,f);
            } else {
                fields.add(var);
                currentScope.declares.put(var.name,var);
            }            
        }

        //TODO: ignore double delcaration of methods for the moment
        l = cn.getMethods();
        Set setter = new HashSet();
        Set getter = new HashSet();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            MethodNode f =(MethodNode) iter.next();
            String methodName = f.getName();
            String pName = getPropertyName(methodName);
            if (pName == null) continue; 
            Var var = new Var(pName,f);
            currentScope.declares.put(var.name,var);
        }

        l = cn.getProperties();
        Set props = new HashSet();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PropertyNode f = (PropertyNode) iter.next();
            Var var = new Var(f);
            if (props.contains(var)) {
                declare(var,f);
            } else {
                props.add(var);
                currentScope.declares.put(var.name,var);
            } 
        }
    }

    private void addVarNames(ClassNode cn, HashMap refs, boolean visitParent)
            throws ClassNotFoundException {
        if (cn == null) return;
        List l = cn.getFields();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            FieldNode f = (FieldNode) iter.next();
            if (visitParent && Modifier.isPrivate(f.getModifiers()))
                continue;
            refs.put(f.getName(),new Var(f));
        }
        l = cn.getMethods();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            MethodNode f = (MethodNode) iter.next();
            if (visitParent && Modifier.isPrivate(f.getModifiers()))
                continue;
            String name = getPropertyName(f.getName());
            if (name == null) continue;
            refs.put(name, new Var(name,f));
        }

        l = cn.getProperties();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            PropertyNode f = (PropertyNode) iter.next();
            if (visitParent && Modifier.isPrivate(f.getModifiers()))
                continue;
            refs.put(f.getName(),new Var(f));
        }

        if (!visitParent) return;

        addVarNames(cn.getSuperClass(), refs, visitParent);
        MethodNode enclosingMethod = cn.getEnclosingMethod();

        if (enclosingMethod == null) return;

        Parameter[] params = enclosingMethod.getParameters();
        for (int i = 0; i < params.length; i++) {
            refs.put(params[i].getName(),new Var(params[i],enclosingMethod.isStatic()));
        }

        if (visitParent)
            addVarNames(enclosingMethod.getDeclaringClass(), refs, visitParent);

        addVarNames(cn.getOuterClass(), refs, visitParent);
    }

    private void addVarNames(String superclassName, HashMap refs, boolean visitParent) 
      throws ClassNotFoundException 
    {

        if (superclassName == null) return;

        ClassNode cn = unit.getClass(superclassName);
        if (cn != null) {
            addVarNames(cn, refs, visitParent);
            return;
        }

        Class c = unit.getClassLoader().loadClass(superclassName);
        Field[] fields = c.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (visitParent && Modifier.isPrivate(f.getModifiers()))
                continue;
            refs.put(f.getName(),new Var(f));
        }

        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (visitParent && Modifier.isPrivate(m.getModifiers()))
                continue;
            String name = getPropertyName(m.getName());
            if (name == null) continue;
            refs.put(name,new Var(name,m));
        }

        if (!visitParent) return;

        addVarNames(c.getSuperclass(), refs, visitParent);

        // it's not possible to know the variable names used for an enclosing
        // method

        // addVarNames(c.getEnclosingClass(),refs,visitParent);
    }
    
    private String getPropertyName(String name) {
        if (!(name.startsWith("set") || name.startsWith("get"))) return null;
        String pname = name.substring(3);
        if (pname.length() == 0) return null;
        String s = pname.substring(0, 1).toLowerCase();
        String rest = pname.substring(1);
        return s + rest;
    }    

    public void visitConstructor(ConstructorNode node) {
        VarScope scope = currentScope;
        currentScope = new VarScope(currentScope);
        
        // TODO: set scope
        // node.setVarScope(currentScope);
        
        HashMap declares = currentScope.declares;
        Parameter[] parameters = node.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            // a constructor is never static
            declare(new Var(parameters[i],false),node);
        }
        currentScope = new VarScope(currentScope);
        Statement code = node.getCode();
        if (code != null) code.visit(this);
        currentScope = scope;
    }
    
    public void visitMethod(MethodNode node) {
        checkAbstractDeclaration(node);
        
        VarScope scope = currentScope;
        currentScope = new VarScope(currentScope,node.isStatic());
        
        // TODO: set scope
        // node.setVarScope(currentScope);
        
        HashMap declares = currentScope.declares;
        Parameter[] parameters = node.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            declares.put(parameters[i].getName(),new Var(parameters[i],node.isStatic()));
        }

        currentScope = new VarScope(currentScope);
        Statement code = node.getCode();
        if (code!=null) code.visit(this);
        currentScope = scope;
    }

    public void visitField(FieldNode node) {
        Expression init = node.getInitialValueExpression();
        if (init != null) init.visit(this);
    }

    public void visitProperty(PropertyNode node) {
        Statement statement = node.getGetterBlock();
        if (statement != null) statement.visit(this);
        
        statement = node.getSetterBlock();
        if (statement != null) statement.visit(this);
        
        Expression init = node.getInitialValueExpression();
        if (init != null) init.visit(this);
    }

    public void visitPropertyExpression(PropertyExpression expression) {

    }

    public void visitCatchStatement(CatchStatement statement) {
        VarScope scope = currentScope;
        currentScope = new VarScope(currentScope);
        declare(new Var(statement.getVariable()), statement);
        super.visitCatchStatement(statement);
        currentScope = scope;
    }
}
