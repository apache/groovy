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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an iterator-style method as cooperating with the loop-control protocol
 * (GROOVY-12126): {@code break} and {@code continue} may be used inside a
 * closure passed directly as an argument to the method, with these semantics:
 * <ul>
 * <li>value-ignoring iterators (e.g. {@code each}, {@code times}, {@code upto}):
 *     {@code break} stops the iteration, {@code continue} proceeds to the next
 *     element;</li>
 * <li>value-consuming iterators (e.g. {@code collect}, {@code findAll},
 *     {@code inject}): {@code break} stops the iteration excluding the current
 *     element's contribution, {@code continue} skips the current element's
 *     contribution.</li>
 * </ul>
 * Cooperating methods catch the compiler-internal
 * {@link org.codehaus.groovy.runtime.LoopControl} signals around each
 * per-element closure invocation. A signal thrown inside a closure passed to a
 * method without this annotation propagates to the caller.
 *
 * @since 6.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SupportsLoopControl {
}
