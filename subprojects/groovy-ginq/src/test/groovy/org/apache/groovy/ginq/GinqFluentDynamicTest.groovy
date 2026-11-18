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

import groovy.ginq.Ginq
import groovy.ginq.Query
import groovy.lang.Closure
import org.junit.jupiter.api.Test

import java.util.function.BiPredicate

import static org.apache.groovy.ginq.GinqFluentTest.Order
import static org.apache.groovy.ginq.GinqFluentTest.Person

/**
 * Dynamic (non-{@code @CompileStatic}) coverage of the {@link Query} Closure
 * join defaults. {@link GinqFluentTest} is compiled statically and binds the
 * same calls to the {@link BiPredicate} overloads.
 */
final class GinqFluentDynamicTest {

    @Test
    void queryToQueryPredicateJoinsUseTheClosureDefaults() {
        Query<Person> people = Ginq.from([
                new Person(1, 'Ann', 20),
                new Person(2, 'Bob', 15)
        ])
        Query<Order> orders = Ginq.from([new Order(1, 5), new Order(1, 7), new Order(3, 4)])

        Closure on = { Person p, Order o ->
            assertCalledViaQueryDefault('truth')
            return p.id == o.personId
        }
        Closure leftSelect = { Person p, Order o ->
            assertCalledViaQueryDefault('invoke')
            return o == null ? 'none' : o.amount.toString()
        }
        Closure rightSelect = { Person p, Order o ->
            assertCalledViaQueryDefault('invoke')
            return p == null ? 'none' : o.amount.toString()
        }
        Closure fullSelect = { Person p, Order o ->
            assertCalledViaQueryDefault('invoke')
            return p == null ? 'none' : (o == null ? p.name : o.amount.toString())
        }

        List<String> left = people.leftJoin(orders, on, leftSelect).toList()
        assert left.toSet() == ['5', '7', 'none'] as Set

        List<String> right = people.rightJoin(orders, on, rightSelect).toList()
        assert right.toSet() == ['5', '7', 'none'] as Set

        List<String> full = people.fullJoin(orders, on, fullSelect).toList()
        assert full.toSet() == ['5', '7', 'none', 'Bob'] as Set
    }

    /**
     * The Closure defaults wrap the caller closures with {@code Query.truth}
     * and {@code Query.invoke}. A direct dispatch to the {@code BiPredicate}
     * overload would call the closures without those helpers.
     */
    private static void assertCalledViaQueryDefault(String helper) {
        boolean seen = Arrays.stream(new Throwable().stackTrace)
                .anyMatch { StackTraceElement frame ->
                    frame.className == Query.name && frame.methodName == helper
                }
        assert seen: "expected Query.${helper} on the stack (Closure default path)"
    }
}
