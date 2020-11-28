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
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
class GinqErrorTest {
    @Test
    void "testGinq - from - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [0, 1, 2]
            }
        '''

        assert err.toString().contains('`select` clause is missing @ line 2, column 17.')
    }

    @Test
    void "testGinq - select - 1"() {
        def err = shouldFail '''\
            GQ {
                select n
            }
        '''

        assert err.toString().contains('One `from` is expected and must be the first clause @ line 2, column 17.')
    }

    @Test
    void "testGinq - select from - 1"() {
        def err = shouldFail '''\
            GQ {
                select n from n in [0, 1, 2]
            }
        '''

        assert err.toString().contains("One `from` is expected and must be the first clause @ line 2, column 24.")
    }

    @Test
    void "testGinq - from select - 1"() {
        def err = shouldFail '''\
            def numbers = [0, 1, 2]
            GQ {
                from numbers
                select n
            }
        '''

        assert err.toString().contains('`in` is expected for `from`, e.g. `from n in nums` @ line 3, column 17.')
    }

    @Test
    void "testGinq - from select - 2"() {
        def err = shouldFail '''\
            def numbers = [0, 1, 2]
            GQ {
                from n as numbers
                select n
            }
        '''

        assert err.toString().contains('`in` is expected for `from`, e.g. `from n in nums` @ line 3, column 17.')
    }

    @Test
    void "testGinq - from select - 3"() {
        def err = shouldFail '''\
            def numbers = [0, 1, 2]
            GQ {
                from n, numbers
                select n
            }
        '''

        assert err.toString().contains('Only 1 argument expected for `from`, e.g. `from n in nums` @ line 3, column 17.')
    }

    @Test
    void "testGinq - from innerjoin select - 1"() {
        def err = shouldFail '''\
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            GQ {
                from n1 in nums1
                innerjoin n2 in nums2
                select n1, n2
            }.toList()
        '''

        assert err.toString().contains('`on` clause is expected for `innerjoin` @ line 5, column 17.')
    }

    @Test
    void "testGinq - from on select - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                on n > 1
                select n
            }
        '''

        assert err.toString().contains('The preceding clause of `on` should be join clause @ line 3, column 17.')
    }

    @Test
    void "testGinq - from groupby where - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                groupby n
                where n > 1
                select n
            }
        '''

        assert err.toString().contains('The preceding clause of `where` should be `from`/join clause @ line 4, column 17.')
    }

    @Test
    void "testGinq - from orderby where select - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                orderby n
                where n > 1
                select n
            }
        '''

        assert err.toString().contains('The preceding clause of `where` should be `from`/join clause @ line 4, column 17.')
    }

    @Test
    void "testGinq - from limit where - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                limit 1
                where n > 1
                select n
            }
        '''

        assert err.toString().contains('The preceding clause of `where` should be `from`/join clause @ line 4, column 17.')
    }

    @Test
    void "testGinq - from limit orderby - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                limit 1
                orderby n
                select n
            }
        '''

        assert err.toString().contains('The clause `orderby` should be in front of `limit` @ line 4, column 17.')
    }

    @Test
    void "testGinq - from limit groupby - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                limit 1
                groupby n
                select n
            }
        '''

        assert err.toString().contains('The clause `groupby` should be in front of `limit` @ line 4, column 17.')
    }

    @Test
    void "testGinq - from groupby select - 1"() {
        def err = shouldFail '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int weight
                String gender
                
                Person(String name, int weight, String gender) {
                    this.name = name
                    this.weight = weight
                    this.gender = gender
                }
            }
            def persons = [new Person('Linda', 100, 'Female'), new Person('Daniel', 135, 'Male'), new Person('David', 121, 'Male')]
            GQ {
                from p in persons
                groupby p.gender
                orderby count()
                select x.gender, count()
            }.toList()
        '''

        assert err.toString().contains('No such property: x')
    }

    @Test
    void "testGinq - from groupby select - 2"() {
        def err = shouldFail '''
            assert [[1, 2], [3, 6], [6, 18]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, agg(_g.stream().map(r -> r.x).reduce(BigDecimal.ZERO, BigDecimal::add))
            }.toList()
        '''

        assert err.toString().contains('Failed to find data source by the alias: x')
    }

    @Test
    void "testGinq - from orderby select - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                orderby n in xxx
                select n
            }.toList()
        '''

        assert err.toString().contains('Invalid order: xxx, `asc`/`desc` is expected @ line 3, column 30.')
    }

    @Test
    void "testGinq - from orderby groupby select - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                orderby n
                groupby n
                select n
            }.toList()
        '''

        assert err.toString().contains("The clause `groupby` should be in front of `orderby` @ line 4, column 17.")
    }

    @Test
    void "testGinq - exists - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                where exists(
                    from m in [2, 3, 4]
                    where m == n
                    select m
                )
                select n
            }
        '''

        assert err.toString().contains("Invalid syntax found in `where' clause @ line 3, column 17.")
    }

    @Test
    void "testGinq - from where - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [0, 1, 2]
                where n = 1
                select n
            }
        '''

        assert err.toString().contains('`where` clause cannot contain assignment expression @ line 3, column 17.')
    }

    @Test
    void "testGinq - subQuery - 1"() {
        def err = shouldFail '''
            GQ {
                from n in [2, 3]
                select n, (
                    from m in [2, 3, 4]
                    where m > n
                    select m   
                ) as q
            }.toList()
        '''

        assert err.toString().contains('subquery returns more than one value: [3, 4]')
    }
}
