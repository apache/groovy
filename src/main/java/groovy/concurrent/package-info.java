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
 * Provides high-level concurrency primitives: {@link groovy.concurrent.Actor} (message-passing),
 * {@link groovy.concurrent.AsyncScope} (structured concurrency), {@link groovy.concurrent.Pool} (thread management),
 * and {@link groovy.concurrent.ParallelScope} (combined async+parallel).
 * Supports virtual threads (JDK 21+) and fail-fast exception handling.
 * </p>
 */
package groovy.concurrent;
