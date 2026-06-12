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

import groovy.lang.annotation.ExtendedElementType;
import groovy.lang.annotation.ExtendedTarget;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Runs each iteration of an annotated {@code for} loop in parallel
 * using the current pool or default executor, with structured completion.
 * <p>
 * Uses {@link groovy.concurrent.Pool#current()} if inside a
 * {@link groovy.concurrent.ParallelScope#withPool} block, otherwise
 * falls back to {@code ForkJoinPool.commonPool()}.
 * <p>
 * While {@code @Parallel} provides a convenient way to parallelise
 * {@code for} loops, using the {@code *Parallel} collection methods
 * (e.g. {@code collectParallel}, {@code eachParallel}) directly may
 * offer a better debugging experience, as {@code @Parallel} performs
 * internal variable renaming in its generated code.
 *
 * @since 6.0.0
 * @see org.codehaus.groovy.transform.ParallelASTTransformation
 */
@Incubating
@Documented
@Retention(RetentionPolicy.SOURCE)
@ExtendedTarget(ExtendedElementType.LOOP)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ParallelASTTransformation")
public @interface Parallel {
}
