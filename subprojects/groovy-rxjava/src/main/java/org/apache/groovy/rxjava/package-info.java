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
 * Groovy RxJava 3 integration module providing an {@link groovy.concurrent.AwaitableAdapter}
 * for <a href="https://github.com/ReactiveX/RxJava">RxJava 3</a> types.
 * <p>
 * This module bridges RxJava's {@link io.reactivex.rxjava3.core.Single},
 * {@link io.reactivex.rxjava3.core.Maybe}, {@link io.reactivex.rxjava3.core.Observable},
 * and {@link io.reactivex.rxjava3.core.Flowable} into Groovy's native
 * {@code async}/{@code await} system:
 * <pre>
 * // Single-value: await a Single or Maybe
 * def result = await Single.just(42)
 * def optional = await Maybe.just("hello")
 *
 * // Multi-value: iterate an Observable or Flowable
 * for await (item in Observable.range(1, 10)) {
 *     println item
 * }
 * </pre>
 * <p>
 * The adapter is auto-discovered via {@link java.util.ServiceLoader} — simply adding
 * {@code groovy-rxjava} to the classpath is sufficient for transparent integration.
 *
 * @see org.apache.groovy.rxjava.RxJavaAwaitableAdapter
 * @since 6.0.0
 */
package org.apache.groovy.rxjava;
