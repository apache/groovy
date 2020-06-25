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
package org.codehaus.groovy.runtime;

import groovy.lang.Reference;
import groovy.lang.Script;

/**
 * Represents a reference to a variable in a script
 */
public class ScriptReference extends Reference {

    private static final long serialVersionUID = -2914281513576690336L;
    private final Script script;
    private final String variable;

    public ScriptReference(Script script, String variable) {
        this.script = script;
        this.variable = variable;
    }

    public Object get() {
        return script.getBinding().getVariable(variable);
    }

    public void set(Object value) {
        script.getBinding().setVariable(variable, value);
    }
}
