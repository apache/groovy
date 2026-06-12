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
package groovy.lang;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;

import java.io.Serial;

/**
 * An exception thrown by the interpreter
 */
public class GroovyRuntimeException extends RuntimeException {

    @Serial private static final long serialVersionUID = -193137033604506378L;
    private ModuleNode module;
    private ASTNode node;

    /**
     * Creates an empty runtime exception.
     */
    public GroovyRuntimeException() {
    }

    /**
     * Creates a runtime exception with the supplied message.
     *
     * @param message the detail message
     */
    public GroovyRuntimeException(String message) {
        super(message);
    }

    /**
     * Creates a runtime exception with the supplied message and AST node.
     *
     * @param message the detail message
     * @param node the related AST node
     */
    public GroovyRuntimeException(String message, ASTNode node) {
        super(message);
        this.node = node;
    }

    /**
     * Creates a runtime exception with the supplied message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public GroovyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a runtime exception with the supplied cause.
     *
     * @param t the underlying cause
     */
    public GroovyRuntimeException(Throwable t) {
        super();
        initCause(t);
    }

    /**
     * Sets the module used when reporting location information.
     *
     * @param module the related module
     */
    public void setModule(ModuleNode module) {
        this.module = module;
    }

    /**
     * Returns the related module, if any.
     *
     * @return the related module
     */
    public ModuleNode getModule() {
        return module;
    }

    /**
     * Returns the message including location information when available.
     *
     * @return the formatted message
     */
    @Override
    public String getMessage() {
        String messageWithoutLocationText = getMessageWithoutLocationText();
        String locationText = getLocationText();
        if (messageWithoutLocationText == null && locationText.isEmpty()) {
            return null; // not "null"
        }
        return messageWithoutLocationText + locationText;
    }

    /**
     * Returns the related AST node, if any.
     *
     * @return the related AST node
     */
    public ASTNode getNode() {
        return node;
    }

    /**
     * Returns the detail message without appended location information.
     *
     * @return the raw detail message
     */
    public String getMessageWithoutLocationText() {
        return super.getMessage();
    }

    /**
     * Builds the source-location suffix appended to this exception's message when location data is available.
     *
     * @return the formatted location text, or an empty string if no location is known
     */
    protected String getLocationText() {
        String answer = ". ";
        if (node != null) {
            answer += "At [" + node.getLineNumber() + ":" + node.getColumnNumber() + "] ";
        }
        if (module != null) {
            answer += module.getDescription();
        }
        if (". ".equals(answer)) {
            return "";
        }
        return answer;
    }
}
