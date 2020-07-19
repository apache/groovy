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

    private Contract contract;
    private SourceUnit sourceUnit;
    private ReaderSource source;

    private boolean constructorAssertionsEnabled = true;
    private boolean preconditionsEnabled = true;
    private boolean postconditionsEnabled = true;
    private boolean classInvariantsEnabled = true;

    private Map<String, Object> extra = new HashMap<String, Object>();

    public ProcessingContextInformation(ClassNode classNode, SourceUnit sourceUnit, ReaderSource source) {
        Validate.notNull(classNode);

        this.contract = new Contract(classNode);
        this.sourceUnit = sourceUnit;
        this.source = source;
    }

    public void setConstructorAssertionsEnabled(boolean other) {
        constructorAssertionsEnabled = other;
    }

    public boolean isConstructorAssertionsEnabled() {
        return constructorAssertionsEnabled;
    }

    public boolean isPreconditionsEnabled() {
        return preconditionsEnabled;
    }

    public boolean isPostconditionsEnabled() {
        return postconditionsEnabled;
    }

    public boolean isClassInvariantsEnabled() {
        return classInvariantsEnabled;
    }

    public Contract contract() {
        return contract;
    }

    public ReaderSource readerSource() {
        return source;
    }

    public SourceUnit sourceUnit() {
        return sourceUnit;
    }

    public void put(String key, Object value) {
        Validate.notNull(key);

        extra.put(key, value);
    }

    public Object get(String key) {
        Validate.notNull(key);

        return extra.get(key);
    }

    public void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        SourceUnit source = sourceUnit();
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
    }
}
