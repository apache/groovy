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

// An implicitly created variable, such as a variable in a script that's doesn't have an explicit
// declaration, or the "it" argument to a closure.
public class DynamicVariable implements Variable {

    private final String name;
    private boolean closureShare = false;
    private boolean staticContext = false;

    public DynamicVariable(String name, boolean context) {
        this.name = name;
        staticContext = context;
    }

    @Override
    public ClassNode getType() {
        return ClassHelper.DYNAMIC_TYPE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Expression getInitialExpression() {
        return null;
    }

    @Override
    public boolean hasInitialExpression() {
        return false;
    }

    @Override
    public boolean isInStaticContext() {
        return staticContext;
    }

    @Override
    public boolean isDynamicTyped() {
        return true;
    }

    @Override
    public boolean isClosureSharedVariable() {
        return closureShare;
    }

    @Override
    public void setClosureSharedVariable(boolean inClosure) {
        closureShare = inClosure;
    }

    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    public ClassNode getOriginType() {
        return getType();
    }

}
