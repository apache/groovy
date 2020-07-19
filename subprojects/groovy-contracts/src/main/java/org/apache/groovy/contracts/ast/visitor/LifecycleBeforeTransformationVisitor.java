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

import org.apache.groovy.contracts.common.spi.Lifecycle;
import org.apache.groovy.contracts.common.spi.ProcessingContextInformation;
import org.apache.groovy.contracts.util.LifecycleImplementationLoader;
import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>AST transformation visitor which is triggered before applying {@link org.apache.groovy.contracts.common.spi.AnnotationProcessor}
 * related transformations.</p>
 *
 * @see AnnotationProcessorVisitor
 */
public class LifecycleBeforeTransformationVisitor extends BaseVisitor {

    private final ProcessingContextInformation pci;

    public LifecycleBeforeTransformationVisitor(SourceUnit sourceUnit, ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);

        Validate.notNull(pci);
        this.pci = pci;
    }

    @Override
    public void visitClass(ClassNode node) {
        super.visitClass(node);

        List<MethodNode> methods = new ArrayList<>(node.getMethods());
        List<MethodNode> constructors = new ArrayList<>(node.getDeclaredConstructors());

        for (Lifecycle lifecyle : LifecycleImplementationLoader.load(Lifecycle.class, getClass().getClassLoader())) {
            lifecyle.beforeProcessingClassNode(pci, node);

            for (MethodNode constructor : constructors) {
                lifecyle.beforeProcessingConstructorNode(pci, node, constructor);
            }

            for (MethodNode method : methods) {
                lifecyle.beforeProcessingMethodNode(pci, node, method);
            }
        }
    }
}
