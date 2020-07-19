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
package org.apache.groovy.contracts.common.impl.lc;

import org.apache.groovy.contracts.common.base.BaseLifecycle;
import org.apache.groovy.contracts.common.spi.ProcessingContextInformation;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.generation.ClassInvariantGenerator;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * Internal {@link org.apache.groovy.contracts.common.spi.Lifecycle} implementation for class-invariants.
 */
public class ClassInvariantLifecycle extends BaseLifecycle {

    @Override
    public void afterProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
        if (!CandidateChecks.isClassInvariantCandidate(classNode, methodNode)) return;
        if (processingContextInformation.contract().hasDefaultClassInvariant()) return;

        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(processingContextInformation.readerSource());
        classInvariantGenerator.addInvariantAssertionStatement(classNode, methodNode);
    }

    @Override
    public void afterProcessingConstructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {
        if (!CandidateChecks.isClassInvariantCandidate(classNode, constructorNode)) return;
        if (!processingContextInformation.isConstructorAssertionsEnabled()) return;
        if (processingContextInformation.contract().hasDefaultClassInvariant()) return;

        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(processingContextInformation.readerSource());
        classInvariantGenerator.addInvariantAssertionStatement(classNode, constructorNode);
    }
}
