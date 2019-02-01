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

import org.codehaus.groovy.ast.expr.Expression;

/**
 * interface to mark a AstNode as Variable. Typically these are 
 * VariableExpression, FieldNode, PropertyNode and Parameter
 */
public interface Variable {
    
    /**
     * the type of the variable
     */
    ClassNode getType();
    
    /**
     * the type before wrapping primitives type of the variable
     */
    ClassNode getOriginType();
    
    /**
     * the name of the variable
     */
    String getName();
    
    /**
     * expression used to initialize the variable or null of there
     * is no initialization.
     */
    Expression getInitialExpression();
    
    /**
     * returns true if there is an initialization expression
     */
    boolean hasInitialExpression();
    
    /**
     * returns true if this variable is used in a static context.
     * A static context is any static initializer block, when this variable
     * is declared as static or when this variable is used in a static method 
     */
    boolean isInStaticContext();

    boolean isDynamicTyped();
    boolean isClosureSharedVariable();
    void setClosureSharedVariable(boolean inClosure);

    int getModifiers();
}
