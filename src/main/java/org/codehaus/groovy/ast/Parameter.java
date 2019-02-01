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
 * Represents a parameter on a constructor or method call. The type name is
 * optional - it defaults to java.lang.Object if unknown.
 */
public class Parameter extends AnnotatedNode implements Variable {

    public static final Parameter[] EMPTY_ARRAY = {
    };

    private ClassNode type;
    private final String name;
    private boolean dynamicTyped;
    private Expression defaultValue;
    private boolean hasDefaultValue;
    private boolean inStaticContext;
    private boolean closureShare=false;
    private int modifiers;
    private ClassNode originType=ClassHelper.DYNAMIC_TYPE;

    public Parameter(ClassNode type, String name) {
        this.name = name;
        this.setType(type);
        this.originType = type;
        this.hasDefaultValue = false;
    }
    
    public Parameter(ClassNode type, String name, Expression defaultValue) {
        this(type,name);
        this.defaultValue = defaultValue;
        this.hasDefaultValue = defaultValue != null;
    }

    public String toString() {
        return super.toString() + "[name:" + name + ((type == null) ? "" : " type: " + type.getName()) + ", hasDefaultValue: " + this.hasInitialExpression() + "]";
    }

    public String getName() {
        return name;
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(ClassNode type) {
        this.type = type;
        dynamicTyped |= type==ClassHelper.DYNAMIC_TYPE;
    }
    
    public boolean hasInitialExpression() {
        return this.hasDefaultValue;
    }
    
    /**
     * @return the default value expression for this parameter or null if
     * no default value is specified
     */
    public Expression getInitialExpression() {
        return defaultValue;
    }
    
    public void setInitialExpression(Expression init) {
        defaultValue = init;
        hasDefaultValue = defaultValue != null;
    }
    
    public boolean isInStaticContext() {
        return inStaticContext;
    }
    
    public void setInStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    public boolean isClosureSharedVariable() {
        return closureShare;
    }

    public void setClosureSharedVariable(boolean inClosure) {
        closureShare = inClosure;        
    }

    public int getModifiers() {
        return modifiers;
    }

    public ClassNode getOriginType() {
        return originType;
    }
    
    public void setOriginType(ClassNode cn) {
        originType = cn;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
}
