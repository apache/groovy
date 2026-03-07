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
 * Groovy Reactor integration module providing an {@link groovy.concurrent.AwaitableAdapter}
 * for <a href="https://projectreactor.io/">Project Reactor</a> types.
 * <p>
 * This module bridges Reactor's {@link reactor.core.publisher.Mono} and
 * {@link reactor.core.publisher.Flux} into Groovy's native {@code async}/{@code await}
 * system, enabling seamless use of Reactor publishers with Groovy's async syntax:
 * <pre>
 * // Single-value: await a Mono
 * def result = await Mono.just("hello")
 *
 * // Multi-value: iterate a Flux
 * for await (item in Flux.range(1, 5)) {
 *     println item
 * }
 * </pre>
 * <p>
 * The adapter is auto-discovered via {@link java.util.ServiceLoader} — simply adding
 * {@code groovy-reactor} to the classpath is sufficient for transparent integration.
 *
 * @see org.apache.groovy.reactor.ReactorAwaitableAdapter
 * @since 6.0.0
 */
package org.apache.groovy.reactor;
