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
package org.apache.groovy.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Helpers that return {@link java.util.function} types.
 * <p>
 * For variants whose results are also {@link groovy.lang.Closure}
 * instances, see {@link Closures}.
 *
 * @since 6.0.0
 */
public class Lambdas {

    private Lambdas() {}

    /**
     * Right-partials a {@link BiPredicate} with the supplied parameter,
     * returning a {@link Predicate} that fixes the second argument.
     * <pre class="language-groovy">
     * BiPredicate&lt;Integer,Integer&gt; divisibleBy = (n, d) -&gt; n % d == 0
     * assert [1, 2, 3, 4, 5].stream().filter(curryWith(divisibleBy, 2)).toList() == [2, 4]
     * </pre>
     *
     * @since 6.0.0
     */
    public static <T, P> Predicate<T> curryWith(BiPredicate<? super T, ? super P> bp, P p) {
        return t -> bp.test(t, p);
    }

    /**
     * Right-partials a {@link BiFunction} with the supplied parameter,
     * returning a {@link Function} that fixes the second argument.
     * <pre class="language-groovy">
     * BiFunction&lt;String,Integer,String&gt; repeat = (s, n) -&gt; s * n
     * assert ['a', 'b', 'c'].stream().map(curryWith(repeat, 3)).toList() == ['aaa', 'bbb', 'ccc']
     * </pre>
     *
     * @since 6.0.0
     */
    public static <T, P, R> Function<T, R> curryWith(BiFunction<? super T, ? super P, ? extends R> bf, P p) {
        return t -> bf.apply(t, p);
    }

    /**
     * Right-partials a {@link BiConsumer} with the supplied parameter,
     * returning a {@link Consumer} that fixes the second argument.
     * <pre class="language-groovy">
     * def sink = []
     * BiConsumer&lt;String,List&gt; addTo = (s, list) -&gt; list &lt;&lt; s
     * ['a', 'b', 'c'].stream().forEach(curryWith(addTo, sink))
     * assert sink == ['a', 'b', 'c']
     * </pre>
     *
     * @since 6.0.0
     */
    public static <T, P> Consumer<T> curryWith(BiConsumer<? super T, ? super P> bc, P p) {
        return t -> bc.accept(t, p);
    }
}
