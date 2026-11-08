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
package org.apache.groovy.ginq

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.provider.collection.runtime.LazyQueryable
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Function

/**
 * Exercises the deferred query wrapper and the {@link Queryable} defaults
 * that a static {@code for} loop and coercion call.
 */
@CompileStatic
class GinqDeferredQueryTest {

    private static Queryable<Integer> numbers() {
        (Queryable<Integer>) GQ {
            from n in [1, 2, 3]
            select n
        }
    }

    @Test
    void deferredOperationsMaterializeTheSameElements() {
        BiPredicate<Integer, Integer> same = (Integer a, Integer b) -> a == b
        Function<Integer, Integer> id = (Integer n) -> n
        assert numbers().innerJoin(Queryable.from(List.of(2, 3)), same).toList().size() == 2
        assert numbers().innerHashJoin(Queryable.from(List.of(2, 3)), id, id).toList().size() == 2
        assert numbers().leftJoin(Queryable.from(List.of(2, 3)), same).toList().size() == 3
        assert numbers().leftHashJoin(Queryable.from(List.of(2, 3)), id, id).toList().size() == 3
        assert numbers().rightJoin(Queryable.from(List.of(2, 3)), same).toList().size() == 2
        assert numbers().rightHashJoin(Queryable.from(List.of(2, 3)), id, id).toList().size() == 2
        assert numbers().fullJoin(Queryable.from(List.of(2, 3)), same).toList().size() == 3
        assert numbers().fullHashJoin(Queryable.from(List.of(2, 3)), id, id).toList().size() == 3
        assert numbers().crossJoin(Queryable.from(List.of(2, 3))).toList().size() == 6

        Function<Integer, Integer> key = (Integer n) -> n
        assert numbers().groupBy(key, (Object group) -> true).toList().size() == 3
        assert numbers().groupByInto(key, (Object group) -> true).toList().size() == 3
        assert numbers().orderBy(new Queryable.Order<>(id, true)).toList() == [1, 2, 3]
        assert numbers().limit(1L, 1L).toList() == [2]
        assert numbers().unionAll(Queryable.from(List.of(2, 3))).toList() == [1, 2, 3, 2, 3]
        assert numbers().intersect(Queryable.from(List.of(2, 3))).toList() == [2, 3]
        assert numbers().minus(Queryable.from(List.of(2, 3))).toList() == [1]

        Function<Integer, Integer> value = (Integer n) -> n
        assert numbers().count() == 3L
        assert numbers().count(value) == 3L
        assert numbers().sum(value) == 6
        assert numbers().avg(value) == 2
        assert numbers().min(value) == 1
        assert numbers().max(value) == 3
        assert numbers().list(value) == [1, 2, 3]
        assert numbers().median(value) == 2
        Queryable<Integer> reusable = LazyQueryable.defer(new Object[0], (Object[] args) -> Queryable.from(List.of(1, 2, 3)))
        assert reusable.stdev(value) > 0
        assert reusable.stdevp(value) > 0
        assert reusable.var(value) > 0
        assert reusable.varp(value) > 0
        assert numbers().agg((Queryable rows) -> rows.count()) == 3L
        assert numbers().size() == 3L
        assert numbers().getAt(1) == 2
        Queryable<Integer> once = numbers()
        assert once.iterator().toList() == [1, 2, 3]
        assert (once as List) == [1, 2, 3]
        assert (numbers() as Set) == [1, 2, 3] as Set
        assert (numbers() as Iterable).iterator().next() == 1
        assert numbers().asType(Queryable) != null
        assert numbers().asType(LazyQueryable) != null

        Queryable<Integer> again = numbers()
        assert once.equals(once)
        assert once.equals(again)
        assert !once.equals(List.of(1, 2, 3))
        assert once.hashCode() == again.hashCode()
        assert once.toString() != null

        BiFunction<Integer, Queryable<Integer>, Integer> select = (Integer n, Queryable<Integer> rows) -> n
        assert numbers().select(select).toList() == [1, 2, 3]
    }

    @Test
    void nullSupplierIsRejectedAndDefaultsRun() {
        Queryable lazy = LazyQueryable.defer(null, (Object[] args) -> null)
        IllegalStateException error = null
        try {
            lazy.toList()
        } catch (IllegalStateException caught) {
            error = caught
        }
        assert error != null
        assert error.message.contains('null')

        Queryable<Integer> plain = (Queryable<Integer>) Proxy.newProxyInstance(
                Queryable.classLoader,
                new Class[]{Queryable},
                (InvocationHandler) (proxy, method, args) -> {
                    if (method.name == 'toList') {
                        return List.of(4, 5)
                    }
                    if (method.default) {
                        return InvocationHandler.invokeDefault(proxy, method, args)
                    }
                    throw new UnsupportedOperationException(method.name)
                })
        assert plain.iterator().toList() == [4, 5]
        assert plain.getAt(0) == 4
        assert plain.asType(List) == [4, 5]
        assert plain.asType(Queryable) == plain
        assert plain.asType(String) == plain

        Queryable wrapped = LazyQueryable.defer(new Object[0], (Object[] args) -> plain)
        assert wrapped.asType(String) == plain
    }

    @Test
    void writtenLocalsAndExistingExtensions() {
        assert BoxedInteger.ids() == [0]
        assert AlreadyExtended.ids() == [1]
        assert ExtraExtension.ids() == [1]
        assert ListedExtension.ids() == [1]
    }

    @CompileStatic
    static class BoxedInteger {
        static List<Integer> ids() {
            Integer cnt = 0
            (List<Integer>) GQ {
                from n in [1]
                select cnt++
            }.toList()
        }
    }

    @CompileStatic(extensions = 'org.apache.groovy.ginq.GinqRowChecks')
    static class AlreadyExtended {
        static List<Integer> ids() {
            (List<Integer>) GQ {
                from n in [1]
                select n
            }.toList()
        }
    }

    @CompileStatic(extensions = 'org.codehaus.groovy.transform.stc.TraitTypeCheckingExtension')
    static class ExtraExtension {
        static List<Integer> ids() {
            (List<Integer>) GQ {
                from n in [1]
                select n
            }.toList()
        }
    }

    @CompileStatic(extensions = ['org.codehaus.groovy.transform.stc.TraitTypeCheckingExtension'])
    static class ListedExtension {
        static List<Integer> ids() {
            (List<Integer>) GQ {
                from n in [1]
                select n
            }.toList()
        }
    }
}
