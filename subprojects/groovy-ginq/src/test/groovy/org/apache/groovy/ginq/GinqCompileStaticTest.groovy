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

import groovy.ginq.transform.GQ
import groovy.transform.CompileStatic
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GINQ SQL-like queries compiled with {@code @CompileStatic}.
 */
@CompileStatic
final class GinqCompileStaticTest {

    private final int minimum = 18

    @Test
    void macroFiltersAndProjects() {
        List<Integer> numbers = [1, 2, 3, 4]
        assert [4, 6, 8] == GQ {
            from n in numbers
            where n > 1
            select n * 2
        }.toList()
    }

    @Test
    void macroAsList() {
        assert [2, 3] == GQL {
            from n in [1, 2, 3]
            where n > 1
            select n
        }
    }

    @Test
    void readsFieldsAndLocals() {
        int extra = 1
        assert [18, 21] == GQ {
            from n in [15, 18, 21]
            where n >= minimum - extra
            select n
        }.toList()
    }

    @Test
    void localIsReadWhenTheQueryIsBuilt() {
        int extra = 1
        Queryable query = GQ {
            from n in [1, 2, 3, 4]
            where n > extra
            select n
        }
        extra = 3
        // Locals are copied when the expression is evaluated. The copy is what runs later.
        assert [2, 3, 4] == query.toList()
    }

    @Test
    void joinsProjectsAndGroups() {
        List<Item> items = [new Item(1, 'Orange'), new Item(2, 'Apple')]
        List<Sale> sales = [new Sale(1), new Sale(1), new Sale(2)]
        assert ['Orange', 'Orange', 'Apple'] == GQ {
            from s in sales
            join i in items on i.id == s.item
            select i.name
        }.toList()

        assert [[0, 6.0], [1, 4.0]] == GQ {
            from n in [1, 2, 3, 4]
            groupby n % 2 as parity
            orderby parity
            select parity, sum(n)
        }.toList()
    }

    @Test
    void windowRowNumber() {
        assert [[1, 0], [2, 1], [3, 2]] == GQ {
            from n in [1, 2, 3]
            select n, (rowNumber() over(orderby n))
        }.toList()
    }

    @Test
    void nestedQueryAndMapRows() {
        assert [3] == GQ {
            from n in [1, 2, 3]
            where n > (from m in [1, 2] select max(m))
            select n
        }.toList()

        List<Map<String, Integer>> rows = [[a: 1, b: 10], [a: 3, b: 30]]
        assert [30] == GQ {
            from r in rows
            where r.a > 1
            select r.b
        }.toList()
    }

    @Test
    void queryInsideClosure() {
        List<Integer> numbers = [1, 2, 3]
        List found = []
        numbers.each { Integer n ->
            if (n == 1) {
                found.addAll(GQ {
                    from m in numbers
                    where m >= n
                    select m
                }.toList())
            }
        }
        assert [1, 2, 3] == found
    }

    @Test
    void orderByDescendingAndMembership() {
        assert [3, 2, 1] == GQ {
            from n in [1, 2, 3]
            orderby n in desc
            select n
        }.toList()

        assert [1, 3] == GQ {
            from n in [1, 2, 3]
            where n in [1, 3]
            select n
        }.toList()
    }

    @Test
    void parallelClauseKeepsRowValues() {
        assert [3, 2, 1] == GQ(parallel: true) {
            from n in [1, 2, 3]
            orderby n in desc
            select n
        }.toList()
    }

    @Test
    void staticMethodAndAnnotation() {
        assert [1, 2, 3] == staticNumbers()
        assert [2, 3] == above([1, 2, 3], 2).toList()
        assert [3, 4] == listed(2)
    }

    @Test
    void compiledClassInAScript() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Shop {
                List names(List<String> items) {
                    GQ {
                        from item in items
                        where item.size() > 3
                        select item.toUpperCase()
                    }.toList()
                }
            }

            assert ['PEAR'] == new Shop().names(['pear', 'fig'])
        '''
    }

    @Test
    void customAggregateFromTheDsl() {
        assertScript '''
            def result = GQ {
                from n in [1, 2, 3]
                groupby n % 2 as k
                select total(n)
            }.toList()
            assert result.toSet() == [4.0, 2.0].toSet()
        '''
    }

    private static List staticNumbers() {
        GQ {
            from n in [1, 2, 3]
            select n
        }.toList()
    }

    @GQ
    private Queryable<Integer> above(List<Integer> values, int floor) {
        from n in values
        where n >= floor
        select n
    }

    @GQ(List)
    private List<Integer> listed(int floor) {
        from n in [1, 2, 3, 4]
        where n > floor
        select n
    }

    static final class Item {
        final int id
        final String name

        Item(int id, String name) {
            this.id = id
            this.name = name
        }
    }

    static final class Sale {
        final int item

        Sale(int item) {
            this.item = item
        }
    }
}
