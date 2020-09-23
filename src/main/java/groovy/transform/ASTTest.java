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
package groovy.transform;

import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This AST transformation aims at helping in debugging other AST transformations. It provides a basic
 * infrastructure for performing tests on AST nodes. You can place this annotation on any node which
 * accepts an annotation (types, methods, annotations, constructors, fields, local variables, packages
 * or parameters), then use a script which is run against this AST node at a specific phase. For example,
 * you could test the {@link Field} AST transformation this way:
 *
 * <pre class="groovyTestCase">
 * import groovy.transform.*
 *
 * {@code @ASTTest}(value = {
 *    def owner = node.declaringClass
 *    assert owner.fields.any { it.name == 'x' }
 *  })
 * {@code @Field int x}
 * </pre>
 *
 * The closure code is executed after the specified phase has completed. If no phase is provided, then the
 * code is executed after the {@link org.codehaus.groovy.control.CompilePhase#SEMANTIC_ANALYSIS semantic analysis} phase
 * and each subsequent phase.
 * <p>
 * The <code>node</code> variable refers to the AST node where the AST test annotation is put. In the previous example,
 * it means that <i>node</i> refers to the declaration "int x".
 *
 * @since 2.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ASTTestTransformation")
public @interface ASTTest {
    /**
     * The compile phase after which the test code should run.
     */
    CompilePhase phase() default CompilePhase.SEMANTIC_ANALYSIS;

    /**
     * A closure which is executed against the annotated node after the specified phase has completed.
     */
    Class value();
}
