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

/**
 * Runtime support for Groovy's {@code async}/{@code await}/{@code defer}
 * language features.
 * <p>
 * Layout:
 * <ul>
 *   <li>{@link org.apache.groovy.runtime.async.AsyncSupport} — public entry
 *       point used by compiler-generated code and by
 *       {@link groovy.concurrent.Awaitable}</li>
 *   <li>{@code AsyncExecutors} — executor/scheduler configuration</li>
 *   <li>{@code AwaitCombinators} — {@code all}/{@code any}/{@code first}/
 *       {@code allSettled} joining algorithms</li>
 *   <li>{@link org.apache.groovy.runtime.async.GroovyPromise} — default
 *       {@link groovy.concurrent.Awaitable} implementation</li>
 *   <li>{@link org.apache.groovy.runtime.async.GeneratorBridge} —
 *       producer/consumer bridge for {@code yield return}</li>
 *   <li>{@link org.apache.groovy.runtime.async.DefaultAsyncScope},
 *       {@link org.apache.groovy.runtime.async.DefaultAsyncChannel} —
 *       structured concurrency and CSP channel implementations</li>
 * </ul>
 */
package org.apache.groovy.runtime.async;
