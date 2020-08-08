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
package org.apache.groovy.contracts.ast.visitor;

import org.apache.groovy.contracts.common.spi.ProcessingContextInformation;
import org.apache.groovy.contracts.domain.ClassInvariant;
import org.apache.groovy.contracts.domain.Contract;
import org.apache.groovy.contracts.domain.Postcondition;
import org.apache.groovy.contracts.domain.Precondition;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.generation.ClassInvariantGenerator;
import org.apache.groovy.contracts.generation.PostconditionGenerator;
import org.apache.groovy.contracts.generation.PreconditionGenerator;
import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

import java.util.Map;

/**
 * Visits the given {@link ClassNode} and injects the current {@link org.apache.groovy.contracts.domain.Contract} into the given AST
 * nodes.
 *
 * @see org.apache.groovy.contracts.domain.Contract
 */
public class DomainModelInjectionVisitor extends BaseVisitor {

    private final ProcessingContextInformation pci;
    private final Contract contract;

    public DomainModelInjectionVisitor(final SourceUnit sourceUnit, final ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);
        Validate.notNull(pci);
        Validate.notNull(pci.contract());

        this.pci = pci;
        this.contract = pci.contract();
    }

    @Override
    public void visitClass(ClassNode type) {
        injectClassInvariant(type, contract.classInvariant());

        for (Map.Entry<MethodNode, Precondition> entry : contract.preconditions()) {
            injectPrecondition(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<MethodNode, Postcondition> entry : contract.postconditions()) {
            injectPostcondition(entry.getKey(), entry.getValue());
        }
    }

    public void injectClassInvariant(final ClassNode type, final ClassInvariant classInvariant) {
        if (!pci.isClassInvariantsEnabled() || !CandidateChecks.isContractsCandidate(type)) return;

        final ReaderSource source = pci.readerSource();
        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);

        classInvariantGenerator.generateInvariantAssertionStatement(type, classInvariant);
    }

    public void injectPrecondition(final MethodNode method, final Precondition precondition) {
        if (!pci.isPreconditionsEnabled() || !CandidateChecks.isPreconditionCandidate(method.getDeclaringClass(), method))
            return;

        final ReaderSource source = pci.readerSource();
        final PreconditionGenerator preconditionGenerator = new PreconditionGenerator(source);

        preconditionGenerator.generatePreconditionAssertionStatement(method, precondition);
    }

    public void injectPostcondition(final MethodNode method, final Postcondition postcondition) {
        if (!pci.isPostconditionsEnabled() || !CandidateChecks.isPostconditionCandidate(method.getDeclaringClass(), method))
            return;

        final ReaderSource source = pci.readerSource();
        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(source);

        postconditionGenerator.generatePostconditionAssertionStatement(method, postcondition);
    }
}
