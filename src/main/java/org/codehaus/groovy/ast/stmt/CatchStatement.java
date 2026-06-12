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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;


/**
 * Represents a catch (Exception var) { } statement that handles exceptions in a try-catch block.
 * Each catch statement specifies an exception type to handle and a code block to execute when
 * an exception of that type is caught. The exception is bound to a variable that can be
 * referenced within the catch block.
 *
 * @see {@link TryCatchStatement}
 * @see {@link Parameter}
 * @see {@link Statement}
 */
public class CatchStatement extends Statement {

    private Parameter variable;

    private Statement code;

    /**
     * Constructs a catch statement with the given exception parameter and code block.
     *
     * @param variable the {@link Parameter} that declares the caught exception and binds it to a variable name
     * @param code the {@link Statement} to execute when the exception is caught
     */
    public CatchStatement(Parameter variable, Statement code) {
        this.variable = variable;
        this.code = code;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitCatchStatement(this);
    }

    /**
     * Returns the statement executed when the exception is caught.
     *
     * @return the catch block {@link Statement}
     */
    public Statement getCode() {
        return code;
    }

    /**
     * Returns the exception type of this catch statement.
     *
     * @return the {@link ClassNode} representing the exception type
     */
    public ClassNode getExceptionType() {
        return variable.getType();
    }

    /**
     * Returns the parameter that declares the caught exception variable.
     *
     * @return the exception {@link Parameter}
     */
    public Parameter getVariable() {
        return variable;
    }

    /**
     * Sets the statement executed when the exception is caught.
     *
     * @param code the catch block {@link Statement}
     */
    public void setCode(Statement code) {
        this.code = code;
    }
}
