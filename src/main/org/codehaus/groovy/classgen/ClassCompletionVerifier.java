/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/


package org.codehaus.groovy.classgen;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.control.SourceUnit;


/**
 * ClassCompletionVerifier
 * 
 */
public class ClassCompletionVerifier extends ClassCodeVisitorSupport {
    
    private ClassNode currentClass;
    private SourceUnit source;
    
    public ClassCompletionVerifier(SourceUnit source) {
        this.source = source;
    }
    
    public ClassNode getClassNode() {
        return currentClass;
    }
    
    public void visitClass(ClassNode node) {
        ClassNode oldClass = currentClass;
        currentClass = node;
        
        checkImplementsAndExtends(node);
        checkClassForOverwritingFinal(node);
        checkMethodsForOverwritingFinal(node);
        checkNoAbstractMethodsNonabstractClass(node);
        
        super.visitClass(node);
        
        currentClass = oldClass;
    }
    
    private void checkNoAbstractMethodsNonabstractClass(ClassNode node) {
        if (Modifier.isAbstract(node.getModifiers())) return;
        List abstractMethods = node.getAbstractMethods();
        if (abstractMethods==null) return;
        for (Iterator iter = abstractMethods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            String methodName = method.getTypeDescriptor();
            addError("Can't have an abstract method in a non abstract class."+
                     " The class '"+node.getName()+"' must be declared abstract or"+
                     " the method '"+methodName+"' must be implemented.",node);
        }
    }

    private void checkAbstractDeclaration(MethodNode methodNode) {
        if (!Modifier.isAbstract(methodNode.getModifiers())) return;
        if (Modifier.isAbstract(currentClass.getModifiers())) return;
        addError("Can't have an abstract method in a non abstract class." +
                 " The class '" + currentClass.getName() +  "' must be declared abstract or the method '" +
                 methodNode.getTypeDescriptor() + "' must not be abstract.",methodNode);
    }
    
    private void checkClassForOverwritingFinal(ClassNode cn) {
        ClassNode superCN = cn.getSuperClass();
        if (superCN==null) return;
        if (!Modifier.isFinal(superCN.getModifiers())) return;
        StringBuffer msg = new StringBuffer();
        msg.append("you are not allowed to overwrite the final class ");
        msg.append(superCN.getName());
        msg.append(".");
        addError(msg.toString(),cn);        
    }
    
    private void checkImplementsAndExtends(ClassNode node) {
        ClassNode cn = node.getSuperClass();
        if (cn.isInterface()) addError("you are not allowed to extend the Interface "+cn.getName()+", use implements instead", node);
        ClassNode[] interfaces = node.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            cn = interfaces[i];
            if (!cn.isInterface()) addError ("you are not allowed to implement the Class "+cn.getName()+", use extends instead", node); 
        }
    }

    private void checkMethodsForOverwritingFinal(ClassNode cn) {
        List l = cn.getMethods();     
        for (Iterator cnIter = l.iterator(); cnIter.hasNext();) {
            MethodNode method =(MethodNode) cnIter.next();
            Parameter[] parameters = method.getParameters();
            for (ClassNode superCN = cn.getSuperClass(); superCN!=null; superCN=superCN.getSuperClass()){
                List methods = superCN.getMethods(method.getName());
                for (Iterator iter = methods.iterator(); iter.hasNext();) {
                    MethodNode m = (MethodNode) iter.next();
                    Parameter[] np = m.getParameters();
                    if (!hasEqualParameterTypes(parameters,np)) continue;
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
    
    private boolean hasEqualParameterTypes(Parameter[] first, Parameter[] second) {
        if (first.length!=second.length) return false;
        for (int i=0; i<first.length; i++) {
            String ft = first[i].getType().getName();
            String st = second[i].getType().getName();
            if (ft.equals(st)) continue;
            return false;
        }        
        return true; 
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
    
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        ClassNode type = call.getType();
        if (Modifier.isAbstract(type.getModifiers())) {
            addError("cannot create an instance from the abstract class "+type.getName(),call);
        }
        super.visitConstructorCallExpression(call);
    }
    
    public void visitMethod(MethodNode node) {
        checkAbstractDeclaration(node);
        super.visitMethod(node);
    }

}
