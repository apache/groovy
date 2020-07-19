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

import org.apache.groovy.contracts.annotations.meta.AnnotationProcessorImplementation;
import org.apache.groovy.contracts.annotations.meta.ClassInvariant;
import org.apache.groovy.contracts.common.impl.ClassInvariantAnnotationProcessor;
import org.apache.groovy.lang.annotation.Incubating;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Represents a <b>class-invariant</b>.
 * </p>
 *
 * <p>
 * The class-invariant defines assertions holding during the entire objects life-time.
 * </p>
 * <p>
 * Class-invariants are verified at runtime at the following pointcuts:
 * <ul>
 *  <li>after a constructor call</li>
 *  <li>before a method call</li>
 *  <li>after a method call</li>
 * </ul>
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
@AnnotationProcessorImplementation(ClassInvariantAnnotationProcessor.class)
public @interface Invariant {
    Class value();
}