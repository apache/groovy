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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ASTNode;

/**
 * Interface defining common methods for method calls.
 *
 * @since 2.1.0
 */
public interface MethodCall {
    /**
     * Returns the receiver of the call.
     *
     * @return the receiver node
     */
    ASTNode getReceiver();

    /**
     * Returns the method name as source text.
     *
     * @return the name, or null when the call has no constant name
     */
    String getMethodAsString();

    /**
     * Returns the argument expression.
     *
     * @return the arguments
     */
    Expression getArguments();

    /**
     * Returns the source text of the call.
     *
     * @return the call text
     */
    String getText();
}
