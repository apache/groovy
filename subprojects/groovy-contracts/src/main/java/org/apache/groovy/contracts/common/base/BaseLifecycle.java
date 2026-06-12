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
package org.apache.groovy.contracts.common.base;

import org.apache.groovy.contracts.common.spi.Lifecycle;
import org.apache.groovy.contracts.common.spi.ProcessingContextInformation;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * Base implementation class for interface {@link Lifecycle}. This class is supposed
 * tp be extended by {@link Lifecycle} implementation classes and provides empty
 * method bodies for all interface methods.
 *
 * @see Lifecycle
 */
public abstract class BaseLifecycle implements Lifecycle {

    /**
     * No-op default hook invoked before class-level contract processing begins.
     *
     * @param processingContextInformation the shared processing context
     * @param classNode the class about to be processed
     */
    @Override
    public void beforeProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {
    }

    /**
     * No-op default hook invoked after class-level contract processing completes.
     *
     * @param processingContextInformation the shared processing context
     * @param classNode the class that was processed
     */
    @Override
    public void afterProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {
    }

    /**
     * No-op default hook invoked before a method is processed.
     *
     * @param processingContextInformation the shared processing context
     * @param classNode the declaring class
     * @param methodNode the method about to be processed
     */
    @Override
    public void beforeProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
    }

    /**
     * No-op default hook invoked after a method is processed.
     *
     * @param processingContextInformation the shared processing context
     * @param classNode the declaring class
     * @param methodNode the processed method
     */
    @Override
    public void afterProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
    }

    /**
     * No-op default hook invoked before a constructor is processed.
     *
     * @param processingContextInformation the shared processing context
     * @param classNode the declaring class
     * @param constructorNode the constructor about to be processed
     */
    @Override
    public void beforeProcessingConstructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {
    }

    /**
     * No-op default hook invoked after a constructor is processed.
     *
     * @param processingContextInformation the shared processing context
     * @param classNode the declaring class
     * @param constructorNode the processed constructor
     */
    @Override
    public void afterProcessingConstructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {
    }
}
