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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.objectweb.asm.Opcodes;


/**
 * ClassCompletionVerifier
 * 
 */
public class ClassCompletionVerifier implements Opcodes, GroovyClassVisitor {
    
    ClassNode classNode;
    
    public ClassNode getClassNode() {
        return classNode;
    }
    

    /* (non-Javadoc)
     * @see org.codehaus.groovy.ast.GroovyClassVisitor#visitClass(org.codehaus.groovy.ast.ClassNode)
     */
    public void visitClass(ClassNode a_node) {
        classNode = a_node;
        if ((classNode.getModifiers() & Opcodes.ACC_ABSTRACT) == 0 ) {
            List abstractMethods = classNode.getAbstractMethods();
            if (abstractMethods != null) {
                List methodNames = new ArrayList();
                for (Iterator iter = abstractMethods.iterator(); iter.hasNext();) {
                    MethodNode method = (MethodNode) iter.next();
                    String methodName = method.getTypeDescriptor();
                    methodNames.add(methodName);                
                }
                throw new RuntimeIncompleteClassException(methodNames, classNode);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.ast.GroovyClassVisitor#visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
     */
    public void visitConstructor(ConstructorNode a_node) {
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.ast.GroovyClassVisitor#visitMethod(org.codehaus.groovy.ast.MethodNode)
     */
    public void visitMethod(MethodNode a_node) {
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.ast.GroovyClassVisitor#visitField(org.codehaus.groovy.ast.FieldNode)
     */
    public void visitField(FieldNode a_node) {
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.ast.GroovyClassVisitor#visitProperty(org.codehaus.groovy.ast.PropertyNode)
     */
    public void visitProperty(PropertyNode a_node) {
    }

}
