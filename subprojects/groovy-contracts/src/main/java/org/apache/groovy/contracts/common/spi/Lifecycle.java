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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * <p>Specifies life-cycle hook-ins for applying AST transformation logic before and
 * after the annotation processors have been run.</p>
 *
 * <p>During excution of GContracts AST transformations, the following process is applied on each {@link ClassNode}
 * instance which qualifies for contract annotations:</P>
 *
 * <ol>
 *    <li>Generation of closure classes.</li>
 *    <li>Handling of {@link AnnotationProcessor} implementation classes</li>
 *    <li>Domain Model Conversion and Injection</li>
 * </ol>
 *
 * <h3>Generation of closure classes</h3>
 *
 * <p>In order to support Groovy 1.7.x GContracts backported Groovy 1.8 handling of annotation closures. This is done
 * by extracting {@link org.codehaus.groovy.ast.expr.ClosureExpression} from annotations and creating {@link groovy.lang.Closure}
 * implementation classes.</p>
 *
 * <h3>Handling of AnnotationProcessor implementation classes</h3>
 *
 * <p>{@link AnnotationProcessor} implementatios are used to modify domain classes found in <tt>org.apache.groovy.contracts.domain</tt>. For that
 * reason, concrete annotation processor often don't modify AST nodes directly, but simply work with domain classes like
 * {@link org.apache.groovy.contracts.domain.Contract}. Whenever an annotation processor is done, it has finished its work on the
 * underlying domain model. </p>
 *
 * <p>{@link #beforeProcessingClassNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode)},
 * {@link #beforeProcessingMethodNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)},
 * {@link #beforeProcessingConstructorNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)} are fired
 * before annotation processors are executed.</p>
 *
 * <h3>Domain Model Conversion and Injection</h3>
 *
 * <p>Takes a look at the domain model instances and generates the corresponding AST transformation code.</p>
 *
 * <p>{@link #afterProcessingClassNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode)},
 * {@link #afterProcessingMethodNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)},
 * {@link #afterProcessingConstructorNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)} are fired
 * after domain model conversion and injection is done.</p>
 */
public interface Lifecycle {

    public void beforeProcessingClassNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode);

    public void afterProcessingClassNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode);

    public void beforeProcessingMethodNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode methodNode);

    public void afterProcessingMethodNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode methodNode);

    public void beforeProcessingConstructorNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode constructorNode);

    public void afterProcessingConstructorNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode constructorNode);
}
