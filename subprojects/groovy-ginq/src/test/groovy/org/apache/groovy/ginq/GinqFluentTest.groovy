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
import groovy.ginq.Grouping
import groovy.ginq.Query
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Fluent GINQ queries, including queries compiled with {@code @CompileStatic}.
 */
@CompileStatic
final class GinqFluentTest {

    @Test
    void whereSelectAndOrder() {
        List<Person> people = [
                new Person(1, 'Ann', 20),
                new Person(2, 'Bob', 15),
                new Person(3, 'Cyd', 30)
        ]
        List<String> names = Ginq.from(people)
                .where { Person p -> p.age >= 18 }
                .orderBy { Person p -> p.name }
                .select { Person p -> p.name }
                .toList()
        assert names == ['Ann', 'Cyd']
    }

    @Test
    void implicitParameterIsTypedFromTheSource() {
        // tag::ginq_fluent_01[]
        List<Integer> doubled = Ginq.from([1, 2, 3, 4])
                .where { it > 1 }
                .orderByDescending { it }
                .select { it * 2 }
                .take(2)
                .toList()
        assert doubled == [8, 6]
        // end::ginq_fluent_01[]
    }

    @Test
    void thenByGroupsAndJoins() {
        List<String> names = Ginq.from([
                new Person(1, 'Zoe', 20),
                new Person(2, 'Ann', 20),
                new Person(3, 'Bob', 15)
        ]).orderBy { Person p -> p.age }.thenBy { Person p -> p.name }
                .select { Person p -> p.name }
                .toList()
        assert names == ['Bob', 'Ann', 'Zoe']

        List<Long> sizes = Ginq.from([
                new Person(1, 'Ann', 20),
                new Person(2, 'Bob', 15),
                new Person(3, 'Cyd', 30)
        ]).groupBy { Person p -> p.age >= 18 }
                .select { Grouping<Boolean, Person> g -> g.count() }
                .toList()
        assert sizes.toSorted() == [1L, 2L]

        List<Order> orders = [new Order(1, 5), new Order(1, 7), new Order(3, 4)]
        List<Integer> amounts = Ginq.from([
                new Person(1, 'Ann', 20),
                new Person(2, 'Bob', 15)
        ]).join(orders) { Person p -> p.id } { Order o -> o.personId } { Person p, Order o -> o.amount }
                .orderBy { it }
                .toList()
        assert amounts == [5, 7]
    }

    @Test
    void selectManySetOperatorsAndTerminalAggregates() {
        assert [1, 2, 3, 4] == Ginq.from([[1, 2], [3, 4]]).selectMany { List<Integer> row -> row }.toList()
        assert [1, 2, 3] == Ginq.of(1, 2).concat(Ginq.of(2, 3)).distinct().orderBy { it }.toList()
        assert [1] == Ginq.of(1, 2).intersect(Ginq.of(1, 3)).toList()
        assert [2] == Ginq.of(1, 2).except(Ginq.of(1)).toList()
        assert [2, 3] == Ginq.of(1, 2, 3).skip(1).toList()
        assert Ginq.empty().toList().isEmpty()

        assert Ginq.of(1, 2, 3).sum { it } == 6
        assert Ginq.of(1, 2, 3).aggregate(0) { int acc, int n -> acc + n } == 6
        assert Ginq.of(1, 2, 3).aggregate { Query<Integer> rows -> rows.max { it } } == 3
    }

    @Test
    void operatorsWaitUntilTheQueryIsRead() {
        List<Integer> seen = []
        Query<Integer> query = Ginq.from([1, 2, 3]).where { Integer n ->
            seen.add(n)
            return n > 1
        }
        assert seen.isEmpty()
        assert query.toList() == [2, 3]
        assert seen == [1, 2, 3]
    }

    @Test
    void thenByRequiresAnOrder() {
        shouldFail(IllegalStateException) {
            Ginq.of(1, 2).thenBy { it }
        }
    }

    @Test
    void leftJoinKeepsUnmatchedOuterElements() {
        List<String> labels = Ginq.of(1, 2).leftJoin([new Order(1, 9)]) { int id -> id } { Order o -> o.personId } { int id, Order o ->
            o == null ? 'none' : o.amount.toString()
        }.toList()
        assert labels == ['9', 'none']
    }

    static final class Person {
        final int id
        final String name
        final int age

        Person(int id, String name, int age) {
            this.id = id
            this.name = name
            this.age = age
        }
    }

    static final class Order {
        final int personId
        final int amount

        Order(int personId, int amount) {
            this.personId = personId
            this.amount = amount
        }
    }
}
