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
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Function
import java.util.function.Predicate

import static org.apache.groovy.ginq.GinqFluentInferenceTest.Order
import static org.apache.groovy.ginq.GinqFluentInferenceTest.Person

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Closure parameters of the fluent operators are inferred from the query element
 * type. Callers do not write those parameter types.
 */
final class GinqFluentInferenceTest {

    @Test
    void untypedClosuresUnderCompileStatic() {
        assert new CompileStaticQueries().labels() == ['9', 'none']
        new CompileStaticQueries().operators()
    }

    @Test
    void untypedClosuresUnderTypeChecked() {
        assert new TypeCheckedQueries().labels() == ['9', 'none']
        assert new TypeCheckedQueries().projected() == ['Ann', 'Cyd']
    }

    @Test
    void untypedClosuresUnderDynamicCompilation() {
        assert new DynamicQueries().labels() == ['9', 'none']
        assert new DynamicQueries().projected() == ['Ann', 'Cyd']
    }

    @Test
    void samVariableAndDefQueryInferParameters() {
        assert new CompileStaticQueries().storedSam() == ['Ann', 'Ann:9']
        assert new CompileStaticQueries().defQuery() == ['Ann']
        assert new CompileStaticQueries().rawQuery() == ['Ann']
        assert new CompileStaticQueries().reassignedQuery() == ['9']
        assert new TypeCheckedQueries().storedSam() == ['Ann', 'Ann:9']
    }

    @Test
    void untypedClosureVariableIsCheckedWhereItIsWritten() {
        def message = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                import groovy.ginq.Ginq
                import groovy.transform.CompileStatic
                import org.apache.groovy.ginq.GinqFluentInferenceTest.Person

                @CompileStatic
                class Probe {
                    static Object run() {
                        List<Person> people = [new Person(1, 'Ann', 20, [])]
                        Closure adults = { p -> p.age >= 18 }
                        Ginq.from(people).where(adults)
                    }
                }
            '''
        }.message
        assert message.contains('No such property: age for class: java.lang.Object')
    }

    @Test
    void rawQueryParameterHasNoElementType() {
        def message = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                import groovy.ginq.Ginq
                import groovy.ginq.Query
                import groovy.transform.CompileStatic

                @CompileStatic
                class Probe {
                    static Object run(Query query) {
                        query.where { p -> p.age >= 18 }
                    }
                }
            '''
        }.message
        assert message.contains('No such property: age for class: java.lang.Object')
    }

    @Test
    void gqNestedClosureStaysDynamic() {
        assert new CompileStaticQueries().nestedClosureInsideGq() == ['Ann']
    }

    @Test
    void typeCheckedGqStillBindsRangeVariables() {
        assertScript '''
            import groovy.ginq.transform.GQ
            import groovy.transform.TypeChecked
            import org.apache.groovy.ginq.GinqFluentInferenceTest.Person
            import org.apache.groovy.ginq.provider.collection.runtime.Queryable

            @TypeChecked
            class Probe {
                static List<String> run() {
                    List<Person> people = [new Person(1, 'Ann', 20, [])]
                    GQ {
                        from p in people
                        where p.age >= 18
                        select p.name
                    }.toList()
                }

                @GQ
                Queryable<String> adults(List<Person> people) {
                    from p in people
                    where p.age >= 18
                    select p.name
                }
            }
            assert Probe.run() == ['Ann']
            assert new Probe().adults([new Person(1, 'Ann', 20, []), new Person(2, 'Bob', 15, [])]).toList() == ['Ann']
        '''
    }

    static final class Person {
        final int id
        final String name
        final int age
        final List<Order> orders

        Person(int id, String name, int age, List<Order> orders) {
            this.id = id
            this.name = name
            this.age = age
            this.orders = orders
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

@CompileStatic
final class CompileStaticQueries {

    List<String> labels() {
        List<String> labels = Ginq.of(1, 2).leftJoin([new Order(1, 9)]) { id -> id } { o -> o.personId } { id, o ->
            o == null ? 'none' : o.amount.toString()
        }.toList()
        return labels
    }

    void operators() {
        List<Person> people = [
                new Person(1, 'Ann', 20, [new Order(1, 9), new Order(1, 8)]),
                new Person(2, 'Bob', 15, []),
                new Person(3, 'Cyd', 30, [new Order(3, 4)])
        ]
        List<Order> orders = [new Order(1, 9), new Order(3, 4), new Order(9, 1)]

        List<String> names = Ginq.from(people)
                .where { p -> p.age >= 18 }
                .orderByDescending { p -> p.age }
                .thenByDescending { p -> p.name }
                .select { p -> p.name }
                .where { name -> name.length() > 0 }
                .toList()
        assert names == ['Cyd', 'Ann']

        Person[] array = new Person[] { people[0], people[1], people[2] }
        assert Ginq.from(array).any { p -> p.age < 18 }
        assert Ginq.from(people.stream()).all { p -> p.name != null }
        Query<Person> continued = Ginq.from(Ginq.from(people))
        assert continued.count { p -> p.name } == 3L
        assert continued.sum { p -> p.age } == 65
        assert continued.avg { p -> p.age } > 21
        assert continued.min { p -> p.name } == 'Ann'
        assert continued.max { p -> p.age } == 30
        assert continued.median { p -> p.age } == 20
        assert continued.aggregate(0) { acc, p -> acc + p.age } == 65
        assert continued.aggregate { rows -> rows.where { p -> p.age >= 18 }.count() } == 2L

        List<String> flat = Ginq.from(people)
                .selectMany({ p -> p.orders }, { p, o -> p.name + ':' + o.amount })
                .toList()
        assert flat == ['Ann:9', 'Ann:8', 'Cyd:4']

        List<String> keys = Ginq.from(people)
                .groupBy({ p -> p.name }, { g -> g.key.length() == 3 && g.count() > 0L })
                .select { g -> g.key }
                .toList()
        assert keys.toSorted() == ['Ann', 'Bob', 'Cyd']

        List<String> equi = Ginq.from(people)
                .rightJoin(orders) { p -> p.id } { o -> o.personId } { p, o ->
                    p == null ? 'none:' + o.amount : p.name + ':' + o.amount
                }.toList()
        assert equi.toSorted() == ['Ann:9', 'Cyd:4', 'none:1']

        List<String> full = Ginq.from(people)
                .fullJoin(orders) { p -> p.id } { o -> o.personId } { p, o ->
                    (p == null ? 'none' : p.name) + ':' + (o == null ? 'none' : o.amount.toString())
                }.toList()
        assert full.toSorted() == ['Ann:9', 'Bob:none', 'Cyd:4', 'none:1']

        List<String> cross = Ginq.from(people).crossJoin(orders) { p, o -> p.name + ':' + o.personId }.toList()
        assert cross.size() == 9
        assert cross.contains('Bob:9')

        List<String> predicated = Ginq.from(people)
                .join(orders) { p, o -> p.id == o.personId } { p, o -> p.name + ':' + o.amount }
                .toList()
        assert predicated.toSorted() == ['Ann:9', 'Cyd:4']

        List<String> leftOn = Ginq.from(people)
                .leftJoin(orders) { p, o -> p.id == o.personId } { p, o ->
                    o == null ? p.name + ':none' : p.name + ':' + o.amount
                }.toList()
        assert leftOn.toSorted() == ['Ann:9', 'Bob:none', 'Cyd:4']

        List<Integer> amounts = Ginq.from(people)
                .selectMany { p -> p.orders }
                .select { o -> o.amount }
                .toList()
        assert amounts == [9, 8, 4]
    }

    List<String> storedSam() {
        List<Person> people = [
                new Person(1, 'Ann', 20, []),
                new Person(2, 'Bob', 15, [])
        ]
        List<Order> orders = [new Order(1, 9)]
        Predicate<Person> adults = { p -> p.age >= 18 }
        Function<Person, String> names = { p -> p.name }
        BiPredicate<Person, Order> same = { p, o -> p.id == o.personId }
        BiFunction<Person, Order, String> label = { p, o -> p.name + ':' + o.amount }
        List<String> projected = Ginq.from(people).where(adults).select(names).toList()
        List<String> joined = Ginq.from(people).join(orders, same, label).toList()
        return projected + joined
    }

    List<String> defQuery() {
        List<Person> people = [
                new Person(1, 'Ann', 20, []),
                new Person(2, 'Bob', 15, [])
        ]
        def query = Ginq.from(people)
        return query.where { p -> p.age >= 18 }.select { p -> p.name }.toList()
    }

    List rawQuery() {
        List<Person> people = [new Person(1, 'Ann', 20, [])]
        Query query = Ginq.from(people)
        return query.where { p -> p.age >= 18 }.select { p -> p.name }.toList()
    }

    List reassignedQuery() {
        List<Person> people = [new Person(1, 'Ann', 20, [])]
        List<Order> orders = [new Order(1, 9)]
        Query query = Ginq.from(people)
        query = Ginq.from(orders)
        return query.select { o -> o.amount.toString() }.toList()
    }

    List<String> nestedClosureInsideGq() {
        List<Person> people = [new Person(1, 'Ann', 20, [new Order(1, 9)]), new Person(2, 'Bob', 15, [])]
        GQ {
            from p in people
            where p.orders.any { o -> o.amount > 0 }
            select p.name
        }.toList()
    }
}

@TypeChecked
final class TypeCheckedQueries {

    List<String> labels() {
        Ginq.of(1, 2).leftJoin([new Order(1, 9)]) { id -> id } { o -> o.personId } { id, o ->
            o == null ? 'none' : o.amount.toString()
        }.toList()
    }

    List<String> projected() {
        List<Person> people = [
                new Person(1, 'Ann', 20, []),
                new Person(2, 'Bob', 15, []),
                new Person(3, 'Cyd', 30, [])
        ]
        Ginq.from(people)
                .where { p -> p.age >= 18 }
                .orderBy { p -> p.name }
                .select { p -> p.name }
                .toList()
    }

    List<String> storedSam() {
        List<Person> people = [new Person(1, 'Ann', 20, []), new Person(2, 'Bob', 15, [])]
        List<Order> orders = [new Order(1, 9)]
        Predicate<Person> adults = { p -> p.age >= 18 }
        Function<Person, String> names = { p -> p.name }
        BiPredicate<Person, Order> same = { p, o -> p.id == o.personId }
        BiFunction<Person, Order, String> label = { p, o -> p.name + ':' + o.amount }
        Ginq.from(people).where(adults).select(names).toList() +
                Ginq.from(people).join(orders, same, label).toList()
    }
}

final class DynamicQueries {

    List<String> labels() {
        Ginq.of(1, 2).leftJoin([new Order(1, 9)]) { id -> id } { o -> o.personId } { id, o ->
            o == null ? 'none' : o.amount.toString()
        }.toList()
    }

    List<String> projected() {
        List<Person> people = [
                new Person(1, 'Ann', 20, []),
                new Person(2, 'Bob', 15, []),
                new Person(3, 'Cyd', 30, [])
        ]
        Ginq.from(people)
                .where { p -> p.age >= 18 }
                .orderBy { p -> p.name }
                .select { p -> p.name }
                .toList()
    }
}
