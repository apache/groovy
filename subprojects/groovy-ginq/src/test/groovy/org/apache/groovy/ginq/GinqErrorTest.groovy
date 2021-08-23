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

import static groovy.test.GroovyAssert.assertScript
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
    void "testGinq - from select distinct - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 2, 3, 3, 3]
                select n, distinct(n + 1)
            }
        '''
        assert err.toString().contains('Invalid usage of `distinct` @ line 3, column 27.')
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

        assert err.toString().contains("Invalid syntax found in `where` clause @ line 3, column 17.")
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

    @Test
    void "testGinq - from groupby select - 3"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on m == n
                groupby n
                select n, m, count(n)
            }.toList()
        '''

        assert err.toString().contains('`m` is not in the `groupby` clause @ line 5, column 27.')
    }

    @Test
    void "testGinq - from groupby select - 4"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on m == n
                groupby n
                select n, n + m, count(n)
            }.toList()
        '''

        assert err.toString().contains('`m` is not in the `groupby` clause @ line 5, column 31.')
    }

    @Test
    void "testGinq - from groupby select - 5"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                groupby n
                select n, hello(n)
            }.toList()
        '''

        assert err.toString().contains('`this.hello(n)` is not an aggregate function @ line 4, column 27.')
    }

    @Test
    void "testGinq - subQuery - 13"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                groupby n
                select (
                    from m in [1, 2, 3]
                    where m == n
                    select sum(m)
                )
            }.toList()
        '''

        assert err.toString().contains('sub-query could not be used in the `select` clause with `groupby` @ line 4, column 24.')
    }

    @Test
    void "testGinq - from unknown select - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                hello world > 0
                select n
            }
        '''

        assert err.toString().contains('Unknown clause: hello @ line 3, column 17.')
    }

    @Test
    void "testGinq - from innerhashjoin select - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                innerhashjoin m in [1, 2, 3] on m == n || m != n
                select n, m
            }
        '''

        assert err.toString().contains('`||` is not allowed in `on` clause of hash join @ line 3, column 49.')
    }

    @Test
    void "testGinq - from innerhashjoin select - 2"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                innerhashjoin m in [1, 2, 3] on m + n == m + n
                select n, m
            }
        '''

        assert err.toString().contains('Only one alias expected at each side of `==`, but found: [m, n] @ line 3, column 49.')
    }

    @Test
    void "testGinq - from innerhashjoin select - 3"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                innerhashjoin m in [1, 2, 3] on m == v
                select n, m
            }
        '''

        assert err.toString().contains('Unknown alias: v @ line 3, column 54.')
    }

    @Test
    void "testGinq - from innerhashjoin select - 4"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2, 3]
                innerhashjoin m in [1, 2, 3] on Objects.equals(n, m)
                select n, m
            }
        '''

        assert err.toString().contains('Only binary expressions(`==`, `&&`) are allowed in `on` clause of hash join @ line 3, column 49.')
    }

    @Test
    void "testGinq - window - 0"() {
        def err = shouldFail  '''\
            GQ {
                from n in [2, 1, 3]
                select n, (xxx(n) over(orderby n))
            }.toList()
        '''

        assert err.toString().contains('Unsupported window function: `xxx` @ line 3, column 28.')
    }

    @Test
    void "testGinq - window - 47"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                select n, (rowNumber() over(order by n))
            }.toList()
        '''

        assert err.toString().contains('Unknown window clause: `order` @ line 3, column 51.')
    }

    @Test
    void "testGinq - window - 60"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                select n, (sum(n) over(range 1, 2))
            }.toList()
        '''

        assert err.toString().contains('`orderby` is expected when using `range` @ line 3, column 46.')
    }

    @Test
    void "testGinq - window - 61"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                select n, (sum(n) over(orderby n, n + 1 range 1, 2))
            }.toList()
        '''

        assert err.toString().contains('Only one field is expected in the `orderby` clause when using `range` @ line 3, column 48.')
    }

    @Test
    void "testGinq - window - 62"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                select n, (sum(n) over(orderby n rows -1, 1 range 1, 2))
            }.toList()
        '''

        assert err.toString().contains('`rows` and `range` cannot be used in the same time @ line 3, column 67.')
    }

    @Test
    void "testGinq - window - 63"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                select n, (sum(n) over(orderby n rows -1))
            }.toList()
        '''

        assert err.toString().contains('Both lower bound and upper bound are expected for `rows` @ line 3, column 55.')
    }

    @Test
    void "testGinq - window - 64"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                select n, (sum(n) over(orderby n range -1))
            }.toList()
        '''

        assert err.toString().contains('Both lower bound and upper bound are expected for `range` @ line 3, column 56.')
    }

    @Test
    void "testGinq - window - 65"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                orderby n in asc(nl)
                select n
            }.toList()
        '''

        assert err.toString().contains('Invalid nulls order: nl, `nullslast`/`nullsfirst` is expected @ line 3, column 34.')
    }

    @Test
    void "testGinq - window - 66"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                orderby n in asc(nullslast, nullsfirst)
                select n
            }.toList()
        '''

        assert err.toString().contains('Only `nullslast`/`nullsfirst` is expected @ line 3, column 33.')
    }

    @Test
    void "testGinq - window - 67"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 1, 2, 2]
                orderby n in asc()
                select n
            }.toList()
        '''

        assert err.toString().contains('Only `nullslast`/`nullsfirst` is expected @ line 3, column 33.')
    }

    @Test
    void "testGinq - invalid option - 1"() {
        def err = shouldFail '''\
            GQ(xxx:true) {
                from n in [1, 2, 3]
                select n
            }
        '''

        assert err.toString().contains('Invalid option: xxx. (supported options: [parallel, astWalker, optimize]) @ line 1, column 16.')
    }

    @Test
    void "testGinq - shutdown - 1"() {
        def err = shouldFail '''\
            GQ {
                shutdown zzz
            }
        '''

        assert err.toString().contains('Invalid option: zzz. (supported options: [immediate, abort]) @ line 2, column 26.')
    }

    @Test
    void "testGinq - unknown statement - 1"() {
        def err = shouldFail '''\
            GQ {
                hello
            }
        '''

        assert err.toString().contains('`select` clause is missing @ line 2, column 17.')
    }

    @Test
    void "testGinq - unknown statement - 2"() {
        def err = shouldFail '''\
            GQ {
                hello world
            }
        '''

        assert err.toString().contains('`select` clause is missing @ line 2, column 17.')
    }

    @Test
    void "testGinq - missing as - 1"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2]
                select count() cnt
            }
        '''

        assert err.toString().contains('Invalid syntax found in `select` clause, maybe `as` is missing when renaming field. @ line 3, column 17.')
    }

    @Test
    void "testGinq - missing as - 2"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2]
                select n num
            }
        '''

        assert err.toString().contains('Invalid syntax found in `select` clause, maybe `as` is missing when renaming field. @ line 3, column 17.')
    }

    @Test
    void "testGinq - missing as - 3"() {
        def err = shouldFail '''\
            GQ {
                from n in [1, 2]
                select n num1, n as num2
            }
        '''

        assert err.toString().contains('Invalid syntax found in `select` clause @ line 3, column 17.')
    }
}
