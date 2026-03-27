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
package groovy.contracts;

import groovy.lang.annotation.ExtendedElementType;
import groovy.lang.annotation.ExtendedTarget;
import org.apache.groovy.contracts.annotations.meta.AnnotationProcessorImplementation;
import org.apache.groovy.contracts.annotations.meta.ClassInvariant;
import org.apache.groovy.contracts.common.impl.ClassInvariantAnnotationProcessor;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Represents a <b>class-invariant</b> or a <b>loop invariant</b>.
 * </p>
 * <p>
 * When applied to a class, defines assertions holding during the entire object's life-time.
 * Class-invariants are verified at runtime at the following pointcuts:
 * <ul>
 *  <li>after a constructor call</li>
 *  <li>before a method call</li>
 *  <li>after a method call</li>
 * </ul>
 * </p>
 * <p>
 * When applied to a {@code for}, {@code while}, or {@code do-while} loop, defines a
 * loop invariant that is asserted at the start of each iteration:
 * <pre>
 * int sum = 0
 * {@code @Invariant}({ 0 &lt;= i &amp;&amp; i &lt;= 4 })
 * for (int i in 0..4) {
 *     sum += i
 * }
 * </pre>
 * </p>
 * <p>
 * Whenever a class has a parent which itself specifies a class-invariant, that class-invariant expression is combined
 * with the actual class's invariant (by using a logical AND).
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Incubating
@ClassInvariant
@Repeatable(Invariants.class)
@ExtendedTarget(ExtendedElementType.LOOP)
@AnnotationProcessorImplementation(ClassInvariantAnnotationProcessor.class)
@GroovyASTTransformationClass("org.apache.groovy.contracts.ast.LoopInvariantASTTransformation")
public @interface Invariant {
    Class value();
}
