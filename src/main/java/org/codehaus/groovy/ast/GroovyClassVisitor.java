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
package org.codehaus.groovy.ast;

/**
 * A special visitor for working with the structure of a class. In general, your 
 * will want to use the Abstract class based on this class {@link ClassCodeVisitorSupport}. 
 * 
 * @see org.codehaus.groovy.ast.ClassNode
 * @see org.codehaus.groovy.ast.ClassCodeVisitorSupport
 */
public interface GroovyClassVisitor {

    /**
    * Visit a ClassNode. 
    */ 
    void visitClass(ClassNode node);

    /**
    * Visit a ConstructorNode. 
    */ 
    void visitConstructor(ConstructorNode node);

    /**
    * Visit a MethodNode. 
    */ 
    void visitMethod(MethodNode node);

    /**
    * Visit a FieldNode. 
    */ 
    void visitField(FieldNode node);

    /**
    * Visit a PropertyNode. 
    */ 
    void visitProperty(PropertyNode node);
}
