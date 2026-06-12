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
package org.apache.groovy.contracts.common.spi;

import org.apache.groovy.contracts.domain.Contract;
import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Holds all context-specific information which is needed during the transformation
 * phase of a single {@link Contract} and its related {@link ClassNode}.</p>
 */
public class ProcessingContextInformation {

    private final Contract contract;
    private final SourceUnit sourceUnit;
    private final ReaderSource source;

    private boolean constructorAssertionsEnabled = true;
    private boolean preconditionsEnabled = true;
    private boolean postconditionsEnabled = true;
    private boolean classInvariantsEnabled = true;

    private final Map<String, Object> extra = new HashMap<>();

    /**
     * Creates the processing context for one class and the contract model derived from it.
     *
     * @param classNode the class currently being transformed
     * @param sourceUnit the owning source unit
     * @param source the reader source backing the source unit
     */
    public ProcessingContextInformation(ClassNode classNode, SourceUnit sourceUnit, ReaderSource source) {
        Validate.notNull(classNode);

        this.contract = new Contract(classNode);
        this.sourceUnit = sourceUnit;
        this.source = source;
    }

    /**
     * Enables or disables constructor assertion generation for the current class.
     *
     * @param other {@code true} to keep constructor assertions enabled
     */
    public void setConstructorAssertionsEnabled(boolean other) {
        constructorAssertionsEnabled = other;
    }

    /**
     * Indicates whether constructor assertions are currently enabled.
     *
     * @return {@code true} if constructor assertions should be generated
     */
    public boolean isConstructorAssertionsEnabled() {
        return constructorAssertionsEnabled;
    }

    /**
     * Indicates whether precondition processing is currently enabled.
     *
     * @return {@code true} if preconditions should be generated
     */
    public boolean isPreconditionsEnabled() {
        return preconditionsEnabled;
    }

    /**
     * Indicates whether postcondition processing is currently enabled.
     *
     * @return {@code true} if postconditions should be generated
     */
    public boolean isPostconditionsEnabled() {
        return postconditionsEnabled;
    }

    /**
     * Indicates whether class invariant processing is currently enabled.
     *
     * @return {@code true} if class invariants should be generated
     */
    public boolean isClassInvariantsEnabled() {
        return classInvariantsEnabled;
    }

    /**
     * Returns the mutable contract domain model associated with the current class.
     *
     * @return the contract model
     */
    public Contract contract() {
        return contract;
    }

    /**
     * Returns the reader source used to look up source fragments during code generation.
     *
     * @return the reader source
     */
    public ReaderSource readerSource() {
        return source;
    }

    /**
     * Returns the source unit currently being transformed.
     *
     * @return the source unit
     */
    public SourceUnit sourceUnit() {
        return sourceUnit;
    }

    /**
     * Stores an auxiliary value in the per-class processing context.
     *
     * @param key the metadata key
     * @param value the value to store
     */
    public void put(String key, Object value) {
        Validate.notNull(key);

        extra.put(key, value);
    }

    /**
     * Returns an auxiliary value previously stored in the per-class processing context.
     *
     * @param key the metadata key
     * @return the associated value, or {@code null} if none has been stored
     */
    public Object get(String key) {
        Validate.notNull(key);

        return extra.get(key);
    }

    /**
     * Adds a syntax error tied to the given AST node while allowing compilation to continue collecting errors.
     *
     * @param msg the error message
     * @param expr the AST node providing source coordinates
     */
    public void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        SourceUnit source = sourceUnit();
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
    }
}
