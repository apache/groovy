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
 * Structured concurrency abstractions for asynchronous and parallel programming.
 *
 * <p>
 * Language-facing async types:
 * <ul>
 *   <li>{@link groovy.concurrent.Awaitable} — async computation handle and
 *       static combinator surface used with the {@code await} keyword</li>
 *   <li>{@link groovy.concurrent.AwaitResult} — success/failure value from
 *       {@link groovy.concurrent.Awaitable#allSettled(Object...)}</li>
 *   <li>{@link groovy.concurrent.AsyncScope} — structured concurrency</li>
 *   <li>{@link groovy.concurrent.AsyncChannel} — CSP-style async channel</li>
 *   <li>{@link groovy.concurrent.AwaitableAdapter} /
 *       {@link groovy.concurrent.AwaitableAdapterRegistry} — SPI for
 *       third-party async types (RxJava, Reactor, …)</li>
 * </ul>
 * Additional high-level primitives: {@link groovy.concurrent.Actor}
 * (message-passing), {@link groovy.concurrent.Pool} (thread management),
 * and {@link groovy.concurrent.ParallelScope} (combined async+parallel).
 * Supports virtual threads (JDK 21+) and fail-fast exception handling.
 * Runtime implementation details live in {@code org.apache.groovy.runtime.async}.
 * </p>
 */
package groovy.concurrent;
