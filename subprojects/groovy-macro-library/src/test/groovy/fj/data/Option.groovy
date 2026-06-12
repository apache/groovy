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
package fj.data

import fj.F

/**
 * Test-only stand-in for Functional Java's {@code fj.data.Option}. It uses FJ's
 * conventions deliberately: {@code bind}/{@code map} (not {@code flatMap}),
 * arguments typed {@code fj.F} (neither {@code Closure} nor
 * {@code java.util.function.Function}), and concrete subclasses so the registry's
 * supertype walk ({@code fj.data.Some} &rarr; {@code fj.data.Option}) is exercised.
 */
abstract class Option<A> {
    abstract boolean defined()
    abstract A get()

    static <A> Option<A> some(A a) { new Some<A>(a) }
    static <A> Option<A> none() { new None<A>() }

    Option bind(F f) { defined() ? (Option) f.f(get()) : this }
    Option map(F f) { defined() ? some(f.f(get())) : this }
}

final class Some<A> extends Option<A> {
    private final A v
    Some(A v) { this.v = v }
    boolean defined() { true }
    A get() { v }
}

final class None<A> extends Option<A> {
    boolean defined() { false }
    A get() { throw new NoSuchElementException() }
}
