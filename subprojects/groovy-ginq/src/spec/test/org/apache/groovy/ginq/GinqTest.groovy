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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.groovy.ginq.dsl.GinqAstBuilder
import org.apache.groovy.ginq.dsl.GinqAstOptimizer
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.syntax.Types
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

@CompileStatic
class GinqTest {
    @Test
    void "testGinq - from select - 0"() {
        assertGinqScript '''
            assert [0, 1, 2] == GQ {
// tag::ginq_simplest[]
                from n in [0, 1, 2]
                select n
// end::ginq_simplest[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 1"() {
        assertGinqScript '''
// tag::ginq_execution_01[]
            def numbers = [0, 1, 2]
            assert [0, 1, 2] == GQ {
                from n in numbers
                select n
            }.toList()
// end::ginq_execution_01[]
        '''
    }

    @Test
    void "testGinq - from select - 2"() {
        assertGinqScript '''
            def numbers = [0, 1, 2]
            assert [0, 2, 4] == GQ {
                from n in numbers
                select n * 2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 3"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }

            def persons = [new Person('Daniel', 35), new Person('Linda', 21), new Person('Peter', 30)]
            assert [35, 21, 30] == GQ {
                from p in persons
                select p.age
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 4"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }

            def persons = [new Person('Daniel', 35), new Person('Linda', 21), new Person('Peter', 30)]
            assert [['Daniel', 35], ['Linda', 21], ['Peter', 30]] == GQ {
                from p in persons
                select p.name, p.age
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 5"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }

            def persons = [new Person('Daniel', 35), new Person('Linda', 21), new Person('Peter', 30)]
            assert [[name:'Daniel', age:35], [name:'Linda', age:21], [name:'Peter', age:30]] == GQ {
                from p in persons
                select (name: p.name, age: p.age)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 6"() {
        assertGinqScript '''
            def numbers = [0, 1, 2]
            assert [0, 1, 2] == GQ {
                from n in numbers select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 7"() {
        assertGinqScript '''
// tag::ginq_execution_02[]
            import java.util.stream.Collectors
            
            def numbers = [0, 1, 2]
            assert '0#1#2' == GQ {
                from n in numbers
                select n
            }.stream()
                .map(e -> String.valueOf(e))
                .collect(Collectors.joining('#'))
// end::ginq_execution_02[]
        '''
    }

    @Test
    void "testGinq - from select - 8"() {
        assertGinqScript '''
            assert [[1], [2], [3]] == GQ {
                from n in [1, 2, 3]
                select n as v1
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 9"() {
        assertGinqScript '''
            int rowNumber = 0
            assert [[0, 0], [1, 1], [2, 2]] == GQ {
                from n in [0, 1, 2]
                select rowNumber++, n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from select - 10"() {
        assertGinqScript '''
// tag::ginq_projection_02[]
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                Person(String name) {
                    this.name = name
                }
            }
            def persons = [new Person('Daniel'), new Person('Paul'), new Person('Eric')]
            assert persons == GQ {
                from n in ['Daniel', 'Paul', 'Eric']
                select new Person(n)
            }.toList()
// end::ginq_projection_02[]
        '''
    }

    @Test
    void "testGinq - from select - 11"() {
        assertGinqScript '''
// tag::ginq_projection_03[]
            def result = GQ {
                from n in [1, 2, 3]
                select Math.pow(n, 2) as powerOfN
            }
            assert [[1, 1], [4, 4], [9, 9]] == result.stream().map(r -> [r[0], r.powerOfN]).toList()
// end::ginq_projection_03[]
        '''
    }

    @Test
    void "testGinq - from where select - 1"() {
        assertGinqScript '''
            def numbers = [0, 1, 2, 3, 4, 5]
            assert [2, 4, 6] == GQ {
                from n in numbers
                where n > 0 && n <= 3
                select n * 2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where select - 2"() {
        assertGinqScript '''
            def numbers = [0, 1, 2, 3, 4, 5]
            assert [2, 4, 6] == GQ {
                from n in numbers where n > 0 && n <= 3 select n * 2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where select - 3"() {
        assertGinqScript '''
            assert [2, 4, 6] == GQ {
// tag::ginq_filtering_01[]
                from n in [0, 1, 2, 3, 4, 5]
                where n > 0 && n <= 3
                select n * 2
// end::ginq_filtering_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where select - 4"() {
        assertGinqScript '''
            assert [0] == GQ {
// tag::ginq_filtering_05[]
                from n in [0, 1, 2]
                where n !in [1, 2]
                select n
// end::ginq_filtering_05[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where select - 5"() {
        assertGinqScript '''
            assert [1, 2] == GQ {
// tag::ginq_filtering_08[]
                from n in [0, 1, 2]
                where n in [1, 2]
                select n
// end::ginq_filtering_08[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from smartinnerjoin select - 1"() {
        assertGinqScript '''
            assert [[1, 1], [3, 3]] == GQ {
// tag::ginq_joining_10[]
                from n1 in [1, 2, 3]
                join n2 in [1, 3] on n1 == n2
                select n1, n2
// end::ginq_joining_10[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from smartinnerjoin select - 2"() {
        assertGinqScript '''
            assert [[1, 3], [2, 3]] == GQ {
                from n1 in [1, 2, 3]
                join n2 in [1, 3] on n2 > n1
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 2"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[2, 1], [3, 2], [4, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n1 == n2
                select n1 + 1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 3"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 2], [2, 3], [3, 4]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n1 == n2
                select n1, n2 + 1
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 4"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 2], [2, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n1 + 1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 5"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 2], [2, 3]] == GQ {
                from n1 in nums1 innerjoin n2 in nums2 on n1 + 1 == n2 select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 6"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }

            def persons1 = [new Person('Daniel', 35), new Person('Linda', 21), new Person('Peter', 30)]
            def persons2 = [new Person('Jack', 35), new Person('Rose', 21), new Person('Smith', 30)]
            assert [['Daniel', 'Jack'], ['Linda', 'Rose'], ['Peter', 'Smith']] == GQ {
                from p1 in persons1
                innerjoin p2 in persons2 on p1.age == p2.age
                select p1.name as p1Name, p2.name as p2Name
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 7"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            
            def persons1 = [new Person('Daniel', 35), new Person('Linda', 21), new Person('Peter', 30)]
            def persons2 = [new Person('Jack', 35), new Person('Rose', 21), new Person('Smith', 30)]
            assert [['DANIEL', 'JACK'], ['LINDA', 'ROSE'], ['PETER', 'SMITH']] == GQ {
                from p1 in persons1
                innerjoin p2 in persons2 on p1.age == p2.age
                select p1.name.toUpperCase(), p2.name.toUpperCase()
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 8"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            
            def same(str) { str }

            def persons1 = [new Person('Daniel', 35), new Person('Linda', 21), new Person('Peter', 30)]
            def persons2 = [new Person('Jack', 35), new Person('Rose', 21), new Person('Smith', 30)]
            assert [['DANIEL', 'JACK'], ['LINDA', 'ROSE'], ['PETER', 'SMITH']] == GQ {
                from p1 in persons1
                innerjoin p2 in persons2 on p1.age == p2.age
                select same(p1.name.toUpperCase()), same(p2.name.toUpperCase())
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 9"() {
        assertGinqScript '''
            assert [1, 2, 3] == GQ {
                from n in [1, 2, 3]
                innerjoin k in [2, 3, 4] on n + 1 == k
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 10"() {
        assertGinqScript '''
            assert [2, 3, 4] == GQ {
                from n in [1, 2, 3]
                innerjoin k in [2, 3, 4] on n + 1 == k
                select k
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 11"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            def nums3 = [3, 4, 5]
            assert [[3, 3, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n2 == n1
                innerjoin n3 in nums3 on n3 == n2
                select n1, n2, n3
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 12"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            def nums3 = [3, 4, 5]
            assert [[3, 3, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n2 == n1
                innerjoin n3 in nums3 on n3 == n1
                select n1, n2, n3
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 13"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            def nums3 = [3, 4, 5]
            assert [[3, 3, 3]] == GQ {
                from v in (
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    select n1, n2
                )
                innerjoin n3 in nums3 on v.n2 == n3
                select v.n1, v.n2, n3
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin select - 14"() {
        assertGinqScript '''
            assert [[1, 1], [3, 3]] == GQ {
// tag::ginq_joining_01[]
                from n1 in [1, 2, 3]
                innerjoin n2 in [1, 3] on n1 == n2
                select n1, n2
// end::ginq_joining_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n1 == n2
                where n1 > 1 && n2 <= 3
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where select - 2"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n1 == n2
                where Math.pow(n1, 1) > 1 && Math.pow(n2, 1) <= 3
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where select - 3"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1 innerjoin n2 in nums2 on n1 == n2 where Math.pow(n1, 1) > 1 && Math.pow(n2, 1) <= 3 select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where select - 4"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }

            def persons1 = [new Person('Daniel', 35), new Person('Linda', 21), new Person('David', 30)]
            def persons2 = [new Person('Jack', 35), new Person('Rose', 21), new Person('Smith', 30)]
            assert [['Daniel', 'Jack']] == GQ {
                from p1 in persons1
                innerjoin p2 in persons2 on p1.age == p2.age
                where p1.name.startsWith('D') && p2.name.endsWith('k')
                select p1.name as p1Name, p2.name as p2Name
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where select - 5"() {
        assertGinqScript '''
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            
            def same(obj) {obj}

            def persons1 = [new Person('Daniel', 35), new Person('Linda', 21), new Person('David', 30)]
            def persons2 = [new Person('Jack', 35), new Person('Rose', 21), new Person('Smith', 30)]
            assert [['Daniel', 'Jack']] == GQ {
                from p1 in persons1
                innerjoin p2 in persons2 on p1.age == p2.age
                where same(p1.name.startsWith('D')) && same(p2.name.endsWith('k'))
                select p1.name as p1Name, p2.name as p2Name
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where select - 6"() {
        assertGinqScript '''
            assert [[3, 3]] == GQ {
                    from n1 in [1, 2, 3]
                    innerjoin n2 in [1, 2, 3] on n1 == n2
                    where n1 in (
                        from m1 in [1, 2, 3]
                        innerjoin m2 in [2, 3, 4] on m2 == m1
                        where m1 > 2 && m2 < 4
                        select m1
                    ) && n1 > 1 && n2 <= 3
                    select n1, n2
                }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin innerjoin leftjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, 4, 5]
            def nums2 = [2, 3, 4, 5, 6, 7]
            def nums3 = [1, 2, 3]
            assert [[3, 3, 3]] == GQ {
                from n1 in nums1
                innerjoin n2 in nums2 on n2 == n1
                leftjoin n3 in nums3 on n3 == n2
                where 1 < n1 && n1 < 5 
                        && 2 < n2 && n2 < 7 
                        && n3 != null
                select n1, n2, n3
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 0"() {
        assertGinqScript '''
            assert [1, 2, 3] == GQ {
// tag::ginq_nested_01[]
                from v in (
                    from n in [1, 2, 3]
                    select n
                )
                select v
// end::ginq_nested_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 1"() {
        assertGinqScript '''
            def numbers = [1, 2, 3]
            assert [1, 2, 3] == GQ {
                from v in (
                    from n in numbers
                    select n
                )
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 2"() {
        assertGinqScript '''
            def numbers = [1, 2, 3]
            assert [1, 2] == GQ {
                from v in (
                    from n in numbers
                    where n < 3
                    select n
                )
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 3"() {
        assertGinqScript '''
            def numbers = [1, 2, 3]
            assert [2] == GQ {
                from v in (
                    from n in numbers
                    where n < 3
                    select n
                )
                where v > 1
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 4"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, 4, 5]
            def nums2 = [1, 2, 3, 4, 5]
            assert [[3, 3], [5, 5]] == GQ {
                from v in (
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 5
                    select n1, n2
                )
                where v.n1 >= 3 && v.n2 in [3, 5]
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 5"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, 4, 5]
            def nums2 = [1, 2, 3, 4, 5]
            assert [[3, 3], [5, 5]] == GQ {
                from v in (
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 5
                    select n1, n2
                )
                where v['n1'] >= 3 && v['n2'] in [3, 5]
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 6"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, 4, 5]
            def nums2 = [1, 2, 3, 4, 5]
            assert [[3, 3], [5, 5]] == GQ {
                from v in (
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 5
                    select n1, n2
                )
                where v[0] >= 3 && v[1] in [3, 5] // v[0] references column1 n1, and v[1] references column2 n2
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 7"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, 4, 5]
            def nums2 = [1, 2, 3, 4, 5]
            assert [[3, 3], [5, 5]] == GQ {
                from v in (
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 5
                    select n1 as vn1, n2 as vn2 // rename column names
                )
                where v.vn1 >= 3 && v.vn2 in [3, 5]
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 8"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, 4, 5]
            def nums2 = [1, 2, 3, 4, 5]
            assert [[3, 3], [5, 5]] == GQ {
                from v in (
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 5
                    select ((n1 as Integer) as vn1), ((n2 as Integer) as vn2)
                )
                where v.vn1 >= 3 && v.vn2 in [3, 5]
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 9"() {
        assertGinqScript '''
            assert [2, 6] == GQ {
                from v in (
                    from n in (
                        from m in [1, 2, 3]
                        select m as v1, (m + 1) as v2
                    )
                    where n.v2 < 4
                    select n.v1 * n.v2
                )
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 10"() {
        assertGinqScript '''
            assert [2, 6] == GQ {
                from v in (
                    from n in (
                        from m in [1, 2, 3]
                        select m, (m + 1) as v2
                    )
                    where n.v2 < 4
                    select n.m * n.v2
                )
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 11"() {
        assertGinqScript '''
            assert [[1, 2], [2, 3]] == GQ {
                from v in (
                    from n in (
                        from m in [1, 2, 3]
                        select m, (m + 1) as v2
                    )
                    where n.v2 < 4
                    select n.m, n.v2   // its column names are: m, v2
                )
                select v.m, v.v2
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 12"() {
        assertGinqScript '''
            assert [[1, 2], [2, 3]] == GQ {
                from v in (
                    from n in (
                        from m in [1, 2, 3]
                        select m, (m + 1) as v2
                    )
                    where n.v2 < 4
                    select n.m, n.v2
                )
                select v."${'m'}", v.v2   // dynamic column name
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 13"() {
        assertGinqScript '''
            assert [2, 6] == GQ {
                from v in (
                    from n in (
                        from m in [1, 2, 3]
                        select m as v1, (m + 1) as v2
                    )
                    innerjoin k in [2, 3, 4] on n.v2 == k
                    where n.v2 < 4
                    select n.v1 * k
                )
                select v
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 14"() {
        assertGinqScript '''
            assert [2, 3] == GQ {
                from n in [1, 2, 3]
                innerjoin k in (
                    from m in [2, 3, 4]
                    select m
                ) on n == k
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 15"() {
        assertGinqScript '''
            assert [1, 2] == GQ {
// tag::ginq_nested_02[]
                from n in [0, 1, 2]
                where n in (
                    from m in [1, 2]
                    select m
                )
                select n
// end::ginq_nested_02[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 16"() {
        assertGinqScript '''
            assert [[2, 2]] == GQ {
                from t in [[0, 0], [1, 1], [2, 2]]
                where t in (
                    from m in [1, 2]
                    innerjoin k in [2, 3] on k == m
                    select m, k
                )
                select t
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 17"() {
        assertGinqScript '''
            import static groovy.lang.Tuple.*
            
            @groovy.transform.EqualsAndHashCode
            class Person {
                String firstName
                String lastName
                int age
                String gender
                
                Person(String firstName, String lastName, int age, String gender) {
                    this.firstName = firstName
                    this.lastName = lastName
                    this.age = age
                    this.gender = gender
                }
            }
            @groovy.transform.EqualsAndHashCode
            class LuckyInfo {
                String lastName
                String gender
                boolean valid
                
                LuckyInfo(String lastName, String gender, boolean valid) {
                    this.lastName = lastName
                    this.gender = gender
                    this.valid = valid
                }
            }
            
            def persons = [new Person('Daniel', 'Sun', 35, 'Male'), new Person('Linda', 'Yang', 21, 'Female'), 
                          new Person('Peter', 'Yang', 30, 'Male'), new Person('Rose', 'Yang', 30, 'Female')]
            def luckyInfoList = [new LuckyInfo('Sun', 'Male', true), new LuckyInfo('Yang', 'Female', true), 
                                 new LuckyInfo('Yang', 'Male', false)]        

            assert ['Daniel', 'Linda', 'Rose'] == GQ {
                from p in persons
                where tuple(p.lastName, p.gender) in (
                    from luckyInfo in luckyInfoList
                    where luckyInfo.valid == true
                    select luckyInfo.lastName, luckyInfo.gender
                )
                select p.firstName
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 18"() {
        assertGinqScript '''
            assert [1, 2] == GQ {
                from n in (
                    from m in [0, 1, 2]
                    select m
                )
                where n in (
                    from m in [1, 2]
                    select m
                )
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 19"() {
        assertGinqScript '''
            assert [2] == GQ {
                from n in (
                    from m in [0, 1, 2]
                    where m > 0
                    select m
                )
                where n in (
                    from m in [1, 2]
                    where m > 1
                    select m
                )
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 20"() {
        assertGinqScript '''
// tag::ginq_projection_01[]
            assert [[1, 1], [2, 4], [3, 9]] == GQ {
                from v in (
                    from n in [1, 2, 3]
                    select n, Math.pow(n, 2) as powerOfN
                )
                select v.n, v.powerOfN
            }.toList()
// end::ginq_projection_01[]
        '''
    }

    @Test
    void "testGinq - nested from select - 21"() {
        assertGinqScript '''
            assert [2] == GQ {
                from n in [0, 1, 2]
                where n > 1 && n in (
                    from m in [1, 2]
                    select m
                )
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 22"() {
        assertGinqScript '''
            assert [2] == GQ {
                from n in [0, 1, 2]
                where n in (
                    from m in [1, 2]
                    select m
                ) && n > 1
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 23"() {
        assertGinqScript '''
            assert ['ab'] == GQ {
                from s in ['a', 'ab', 'bck']
                where s.size() in (
                    from x in ['ak', 'bg']
                    where x[0] == s[0]
                    select x.size()
                )
                select s
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 24"() {
        assertGinqScript '''
            assert ['ab', 'bck'] == GQ {
                from s in ['a', 'ab', 'bck']
                where s.size() in (
                    from x in ['ak', 'bg']
                    where x[0] == s[0]
                    select x.size()
                ) || s.length() in (
                    from y in ['abj', 'bpt']
                    where y[0] == s[0]
                    select y.length()
                )
                select s
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 25"() {
        assertGinqScript '''
            assert ['ab'] == GQ {
                from s in ['a', 'ab', 'bck']
                where s.size() in (
                    from x in ['ak', 'bg']
                    where x[0] == s[0]
                    select x.size()
                ) && s in (
                    from y in ['a', 'ab']
                    where y == s
                    select y
                )
                select s
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 26"() {
        assertGinqScript '''
            assert [0] == GQ {
// tag::ginq_filtering_06[]
                from n in [0, 1, 2]
                where n !in (
                    from m in [1, 2]
                    select m
                )
                select n
// end::ginq_filtering_06[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 27"() {
        assertGinqScript '''
            assert [1, 2] == GQ {
// tag::ginq_filtering_07[]
                from n in [0, 1, 2]
                where n in (
                    from m in [1, 2]
                    select m
                )
                select n
// end::ginq_filtering_07[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - nested from select - 28"() {
        assertGinqScript '''
// tag::ginq_filtering_09[]
            import static groovy.lang.Tuple.tuple
            assert [0, 1] == GQ {
                from n in [0, 1, 2]
                where tuple(n, n + 1) in (
                    from m in [1, 2]
                    select m - 1, m
                )
                select n
            }.toList()
// end::ginq_filtering_09[]
        '''
    }

    @Test
    void "testGinq - nested from select - 29"() {
        assertGinqScript '''
// tag::ginq_filtering_10[]
            import static groovy.lang.Tuple.tuple
            assert [2] == GQ {
                from n in [0, 1, 2]
                where tuple(n, n + 1) !in (
                    from m in [1, 2]
                    select m - 1, m
                )
                select n
            }.toList()
// end::ginq_filtering_10[]
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 2"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 3"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 4"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4, null]
            assert [[1, null], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 5"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4, null]
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 6"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null, null]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 7"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null, null]
            def nums2 = [2, 3, 4, null]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 8"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null, null]
            def nums2 = [2, 3, 4, null, null]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null], [null, null], [null, null]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 9"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4, null, null]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 10"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4, null, null]
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin select - 11"() {
        assertGinqScript '''
            assert [[1, null], [2, 2], [3, 3]] == GQ {
// tag::ginq_joining_02[]
                from n1 in [1, 2, 3]
                leftjoin n2 in [2, 3, 4] on n1 == n2
                select n1, n2
// end::ginq_joining_02[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin where select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4, null, null]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                where n2 != null
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from leftjoin where select - 2"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4, null]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                leftjoin n2 in nums2 on n1 == n2
                where n1 != null && n2 != null
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 2"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4]
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 3"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null]
            def nums1 = [2, 3, 4]
            assert [[null, 1], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 4"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null]
            def nums1 = [2, 3, 4, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 5"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4, null]
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 6"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null, null]
            def nums1 = [2, 3, 4]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 7"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null, null]
            def nums1 = [2, 3, 4, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 8"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null, null]
            def nums1 = [2, 3, 4, null, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null], [null, null], [null, null]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 9"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null]
            def nums1 = [2, 3, 4, null, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 10"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4, null, null]
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin select - 11"() {
        assertGinqScript '''
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
// tag::ginq_joining_03[]
                from n1 in [2, 3, 4]
                rightjoin n2 in [1, 2, 3] on n1 == n2
                select n1, n2
// end::ginq_joining_03[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from rightjoin where select - 1"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4, null, null]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                rightjoin n2 in nums2 on n1 == n2
                where n1 != null
                select n1, n2
            }.toList()
        '''
    }


    @Test
    void "testGinq - from lefthashjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 2"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 3"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 4"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4, null]
            assert [[1, null], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 5"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4, null]
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 6"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null, null]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 7"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null, null]
            def nums2 = [2, 3, 4, null]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 8"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null, null]
            def nums2 = [2, 3, 4, null, null]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null], [null, null], [null, null]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 9"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4, null, null]
            assert [[1, null], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 10"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4, null, null]
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin select - 11"() {
        assertGinqScript '''
            assert [[1, null], [2, 2], [3, 3]] == GQ {
// tag::ginq_joining_07[]
                from n1 in [1, 2, 3]
                lefthashjoin n2 in [2, 3, 4] on n1 == n2
                select n1, n2
// end::ginq_joining_07[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin where select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4, null, null]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                where n2 != null
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from lefthashjoin where select - 2"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3, null]
            def nums2 = [2, 3, 4, null]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                lefthashjoin n2 in nums2 on n1 == n2
                where n1 != null && n2 != null
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 2"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4]
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 3"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null]
            def nums1 = [2, 3, 4]
            assert [[null, 1], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 4"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null]
            def nums1 = [2, 3, 4, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 5"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4, null]
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 6"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null, null]
            def nums1 = [2, 3, 4]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 7"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null, null]
            def nums1 = [2, 3, 4, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 8"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null, null]
            def nums1 = [2, 3, 4, null, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null], [null, null], [null, null]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 9"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3, null]
            def nums1 = [2, 3, 4, null, null]
            assert [[null, 1], [2, 2], [3, 3], [null, null], [null, null]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 10"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4, null, null]
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin select - 11"() {
        assertGinqScript '''
            assert [[null, 1], [2, 2], [3, 3]] == GQ {
// tag::ginq_joining_08[]
                from n1 in [2, 3, 4]
                righthashjoin n2 in [1, 2, 3] on n1 == n2
                select n1, n2
// end::ginq_joining_08[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from righthashjoin where select - 1"() {
        assertGinqScript '''
            def nums2 = [1, 2, 3]
            def nums1 = [2, 3, 4, null, null]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                righthashjoin n2 in nums2 on n1 == n2
                where n1 != null
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from fulljoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3], [null, 4]] == GQ {
                from n1 in nums1
                fulljoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from fulljoin select - 2"() {
        assertGinqScript '''
            assert [[1, null], [2, 2], [3, 3], [null, 4]] == GQ {
// tag::ginq_joining_04[]
                from n1 in [1, 2, 3]
                fulljoin n2 in [2, 3, 4] on n1 == n2
                select n1, n2
// end::ginq_joining_04[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from fulljoin where select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                fulljoin n2 in nums2 on n1 == n2
                where n1 != null && n2 != null 
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from fullhashjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[1, null], [2, 2], [3, 3], [null, 4]] == GQ {
                from n1 in nums1
                fullhashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from fullhashjoin select - 2"() {
        assertGinqScript '''
            assert [[1, null], [2, 2], [3, 3], [null, 4]] == GQ {
// tag::ginq_joining_09[]
                from n1 in [1, 2, 3]
                fullhashjoin n2 in [2, 3, 4] on n1 == n2
                select n1, n2
// end::ginq_joining_09[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from fullhashjoin where select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                fullhashjoin n2 in nums2 on n1 == n2
                where n1 != null && n2 != null 
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from crossjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [3, 4, 5]
            assert [[1, 3], [1, 4], [1, 5], [2, 3], [2, 4], [2, 5], [3, 3], [3, 4], [3, 5]] == GQ {
                from n1 in nums1
                crossjoin n2 in nums2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from crossjoin select - 2"() {
        assertGinqScript '''
            assert [[1, 3], [1, 4], [1, 5], [2, 3], [2, 4], [2, 5], [3, 3], [3, 4], [3, 5]] == GQ {
// tag::ginq_joining_05[]
                from n1 in [1, 2, 3]
                crossjoin n2 in [3, 4, 5]
                select n1, n2
// end::ginq_joining_05[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from crossjoin where select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [3, 4, 5]
            assert [[3, 3], [3, 5]] == GQ {
                from n1 in nums1
                crossjoin n2 in nums2
                where n1 > 2 && n2 != 4
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 1"() {
        assertGinqScript '''
            assert [1, 2, 5, 6] == GQ {
// tag::ginq_sorting_01[]
                from n in [1, 5, 2, 6]
                orderby n
                select n
// end::ginq_sorting_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 2"() {
        assertGinqScript '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            def persons = [new Person('Linda', 21), new Person('Daniel', 35), new Person('David', 21)]
            assert [new Person('Daniel', 35), new Person('Linda', 21), new Person('David', 21)] == GQ {
                from p in persons
                orderby p.age in desc
                select p
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 3"() {
        assertGinqScript '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            def persons = [new Person('Linda', 21), new Person('Daniel', 35), new Person('David', 21)]
            assert [new Person('Daniel', 35), new Person('David', 21), new Person('Linda', 21)] == GQ {
                from p in persons
                orderby p.age in desc, p.name
                select p
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 4"() {
        assertGinqScript '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            def persons = [new Person('Linda', 21), new Person('Daniel', 35), new Person('David', 21)]
            assert [new Person('Daniel', 35), new Person('David', 21), new Person('Linda', 21)] == GQ {
                from p in persons
                orderby p.age in desc, p.name in asc
                select p
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 5"() {
        assertGinqScript '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            def persons = [new Person('Linda', 21), new Person('Daniel', 35), new Person('David', 21)]
            assert [new Person('Daniel', 35), new Person('Linda', 21), new Person('David', 21)] == GQ {
                from p in persons
                orderby p.age in desc, p.name in desc
                select p
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 6"() {
        assertGinqScript '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            def persons = [new Person('Linda', 21), new Person('Daniel', 35), new Person('David', 21)]
            assert [new Person('Linda', 21), new Person('David', 21), new Person('Daniel', 35)] == GQ {
                from p in persons
                orderby p.age, p.name in desc
                select p
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 7"() {
        assertGinqScript '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int age
                
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            def persons = [new Person('Linda', 21), new Person('Daniel', 35), new Person('David', 21)]
            assert [new Person('Linda', 21), new Person('David', 21), new Person('Daniel', 35)] == GQ {
                from p in persons
                orderby p.age in asc, p.name in desc
                select p
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 8"() {
        assertGinqScript '''
            assert [1, 2, 5, 6] == GQ {
                from n in [1, 5, 2, 6]
                orderby 1 / n in desc
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 9"() {
        assertGinqScript '''
            assert [1, 2, 5, 6] == GQ {
                from n in [1, 5, 2, 6]
                orderby Math.pow(1 / n, 1) in desc
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 10"() {
        assertGinqScript '''
            assert [6, 5, 2, 1] == GQ {
// tag::ginq_sorting_02[]
                from n in [1, 5, 2, 6]
                orderby n in desc
                select n
// end::ginq_sorting_02[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 11"() {
        assertGinqScript '''
            assert [1, 2, 5, 6] == GQ {
// tag::ginq_sorting_03[]
                from n in [1, 5, 2, 6]
                orderby n in asc
                select n
// end::ginq_sorting_03[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 12"() {
        assertGinqScript '''
            assert ['cd', 'ef', 'a', 'b'] == GQ {
// tag::ginq_sorting_04[]
                from s in ['a', 'b', 'ef', 'cd']
                orderby s.length() in desc, s in asc
                select s
// end::ginq_sorting_04[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 13"() {
        assertGinqScript '''
            assert ['cd', 'ef', 'a', 'b'] == GQ {
// tag::ginq_sorting_05[]
                from s in ['a', 'b', 'ef', 'cd']
                orderby s.length() in desc, s
                select s
// end::ginq_sorting_05[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 14"() {
        assertGinqScript '''
            assert [1, 2, 5, 6, null, null] == GQ {
// tag::ginq_sorting_06[]
                from n in [1, null, 5, null, 2, 6]
                orderby n in asc(nullslast)
                select n
// end::ginq_sorting_06[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 15"() {
        assertGinqScript '''
            assert [null, null, 1, 2, 5, 6] == GQ {
// tag::ginq_sorting_07[]
                from n in [1, null, 5, null, 2, 6]
                orderby n in asc(nullsfirst)
                select n
// end::ginq_sorting_07[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 16"() {
        assertGinqScript '''
            assert [6, 5, 2, 1, null, null] == GQ {
// tag::ginq_sorting_08[]
                from n in [1, null, 5, null, 2, 6]
                orderby n in desc(nullslast)
                select n
// end::ginq_sorting_08[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby select - 17"() {
        assertGinqScript '''
            assert [null, null, 6, 5, 2, 1] == GQ {
// tag::ginq_sorting_09[]
                from n in [1, null, 5, null, 2, 6]
                orderby n in desc(nullsfirst)
                select n
// end::ginq_sorting_09[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin orderby select - 1"() {
        assertGinqScript '''
            assert [2, 3] == GQ {
                from n1 in [1, 2, 3]
                innerjoin n2 in [2, 3, 4] on n1 == n2
                orderby n1
                select n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin orderby select - 2"() {
        assertGinqScript '''
            assert [3, 2] == GQ {
                from n1 in [1, 2, 3]
                innerjoin n2 in [2, 3, 4] on n1 == n2
                orderby n1 in desc
                select n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin orderby select - 3"() {
        assertGinqScript '''
            assert [[3, 3], [2, 2]] == GQ {
                from n1 in [1, 2, 3]
                innerjoin n2 in [2, 3, 4] on n1 == n2
                orderby n1 in desc, n2 in desc
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from limit select - 1"() {
        assertGinqScript '''
            assert [1, 2, 3] == GQ {
// tag::ginq_pagination_01[]
                from n in [1, 2, 3, 4, 5]
                limit 3
                select n
// end::ginq_pagination_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from limit select - 2"() {
        assertGinqScript '''
            assert [2, 3, 4] == GQ {
// tag::ginq_pagination_02[]
                from n in [1, 2, 3, 4, 5]
                limit 1, 3
                select n
// end::ginq_pagination_02[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby limit select - 3"() {
        assertGinqScript '''
            assert [5, 4, 3] == GQ {
                from n in [1, 2, 3, 4, 5]
                orderby n in desc
                limit 3
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from orderby limit select - 4"() {
        assertGinqScript '''
            assert [4, 3, 2] == GQ {
                from n in [1, 2, 3, 4, 5]
                orderby n in desc
                limit 1, 3
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where orderby limit select - 1"() {
        assertGinqScript '''
            assert [5, 4, 3] == GQ {
                from n in [1, 2, 3, 4, 5]
                where n >= 2
                orderby n in desc
                limit 3
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where orderby limit select - 2"() {
        assertGinqScript '''
            assert [4, 3, 2] == GQ {
                from n in [1, 2, 3, 4, 5]
                where n >= 2
                orderby n in desc
                limit 1, 3
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where orderby limit select - 3"() {
        assertGinqScript '''
            assert [5, 4] == GQ {
                from n in [1, 2, 3, 4, 5]
                where n > 3
                orderby n in desc
                limit 3
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where orderby limit select - 4"() {
        assertGinqScript '''
            assert [4] == GQ {
                from n in [1, 2, 3, 4, 5]
                where n > 3
                orderby n in desc
                limit 1, 3
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where orderby limit select - 1"() {
        assertGinqScript '''
            assert [[4, 4], [3, 3]] == GQ {
                from n1 in [1, 2, 3, 4, 5]
                innerjoin n2 in [2, 3, 4, 5, 6] on n2 == n1
                where n1 > 2
                orderby n2 in desc
                limit 1, 3
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where orderby limit select - 2"() {
        assertGinqScript '''
            assert [[4, 4, 4]] == GQ {
                from n1 in [1, 2, 3, 4, 5]
                innerjoin n2 in [2, 3, 4, 5, 6] on n2 == n1
                innerjoin n3 in [3, 4, 5, 6, 7] on n3 == n2
                where n1 > 3
                orderby n2 in desc
                limit 1, 3
                select n1, n2, n3
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 1"() {
        assertGinqScript '''
            // reference the column `n` in the groupby clause, and `count` is a built-in aggregate function
            assert [[1, 2], [3, 2], [6, 3]] == GQ {
// tag::ginq_grouping_01[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, count(n)
// end::ginq_grouping_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 2"() {
        assertGinqScript '''
            assert [[1, 2], [3, 6], [6, 18]] == GQ {
// tag::ginq_grouping_04[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, sum(n)
// end::ginq_grouping_04[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 3"() {
        assertGinqScript '''
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
            assert [['Female', 1], ['Male', 2]] == GQ {
                from p in persons
                groupby p.gender
                orderby count()
                select p.gender, count()
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 4"() {
        assertGinqScript '''
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
            assert [['Female', 1], ['Male', 2]] == GQ {
                from p in persons
                groupby p.gender
                orderby count(p)
                select p.gender, count(p)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 5"() {
        assertGinqScript '''
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
            assert [['Female', 1], ['Male', 2]] == GQ {
                from p in persons
                groupby p.gender
                orderby count(p.gender)
                select p.gender, count(p.gender)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 6"() {
        assertGinqScript '''
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
            assert [['Female', 1], ['Male', 2]] == GQ {
                from p in persons
                groupby p.gender
                orderby count(p.name)
                select p.gender, count(p.name)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 7"() {
        assertGinqScript '''
            // `agg` is the most powerful aggregate function, `_g` represents the grouped Queryable object
            assert [[1, 2], [3, 6], [6, 18]] == GQ {
// tag::ginq_grouping_03[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, agg(_g.stream().map(r -> r.n).reduce(BigDecimal.ZERO, BigDecimal::add))
// end::ginq_grouping_03[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 8"() {
        assertGinqScript '''
            @groovy.transform.EqualsAndHashCode
            class Person {
                String firstName
                String lastName
                int age
                String gender
                
                Person(String firstName, String lastName, int age, String gender) {
                    this.firstName = firstName
                    this.lastName = lastName
                    this.age = age
                    this.gender = gender
                }
            }
            
            def persons = [new Person('Daniel', 'Sun', 35, 'Male'), new Person('Linda', 'Yang', 21, 'Female'), 
                          new Person('Peter', 'Yang', 30, 'Male'), new Person('Rose', 'Yang', 30, 'Female')]
                          
            assert [['Male', 30, 35], ['Female', 21, 30]] == GQ {
                from p in persons
                groupby p.gender
                select p.gender, min(p.age), max(p.age)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 9"() {
        assertGinqScript '''
            import java.util.stream.Collectors
            
            assert [[1, 'a'], [2, 'bc'], [3, 'def']] == GQ {
                from s in ['a', 'bc', 'def']
                groupby s.size()
                select s.size(), agg(_g.stream().map(r -> r.s).collect(Collectors.joining()))
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 10"() {
        assertGinqScript '''
            import java.util.stream.Collectors
            
            assert [[3, 3, 'def'], [2, 2, 'bc']] == GQ {
                from n in [1, 2, 3]
                innerjoin s in ['bc', 'def'] on n == s.length()
                groupby n, s.length()
                select n, s.length(), agg(_g.stream().map(r -> r.s).collect(Collectors.joining()))
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 11"() {
        assertGinqScript '''
            import java.util.stream.Collectors
            
            assert [[2, 2, 2, 'bc'], [3, 3, 3, 'def']] == GQ {
                from n in [1, 2, 3]
                innerjoin m in [1, 2, 3] on m == n
                innerjoin s in ['bc', 'def'] on n == s.length()
                groupby n, m, s.length()
                select n, m, s.length(), agg(_g.stream().map(r -> r.s).collect(Collectors.joining()))
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 12"() {
        assertGinqScript '''
            assert [[1, 'b'], [2, 'ef']] == GQ {
// tag::ginq_grouping_05[]
                from s in ['a', 'b', 'cd', 'ef']
                groupby s.size() as length
                select length, max(s)
// end::ginq_grouping_05[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 13"() {
        assertGinqScript '''
            assert [[1, 'a'], [2, 'cd']] == GQ {
// tag::ginq_grouping_06[]
                from s in ['a', 'b', 'cd', 'ef']
                groupby s.size() as length
                select length, min(s)
// end::ginq_grouping_06[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 14"() {
        assertGinqScript '''
            // reference the column `n` in the groupby clause, and `count` is a built-in aggregate function
            assert [[1, 2], [3, 2], [6, 3]] == GQ {
// tag::ginq_grouping_07[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, count()
// end::ginq_grouping_07[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 15"() {
        assertGinqScript '''
            assert [[3, 'b', 'bef'], [2, 'a', 'ac'], [2, 'b', 'bd'], [3, 'a', 'acd']] == GQ {
// tag::ginq_grouping_08[]
                from s in ['ab', 'ac', 'bd', 'acd', 'bcd', 'bef']
                groupby s.size() as length, s[0] as firstChar
                select length, firstChar, max(s)
// end::ginq_grouping_08[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 16"() {
        assertGinqScript '''
            assert [[3, 'b', 'bef']] == GQ {
// tag::ginq_grouping_09[]
                from s in ['ab', 'ac', 'bd', 'acd', 'bcd', 'bef']
                groupby s.size() as length, s[0] as firstChar
                having length == 3 && firstChar == 'b'
                select length, firstChar, max(s)
// end::ginq_grouping_09[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 17"() {
        assertGinqScript '''
            def test() {
                final int x = 1
                assert [[1, 1, 2], [6, 1, 3], [3, 1, 2]] == GQ {
                    from n in [1, 1, 3, 3, 6, 6, 6]
                    groupby n, x
                    select n, x, count(n)
                }.toList()
            }
            test()
        '''
    }

    @Test
    void "testGinq - from groupby select - 18"() {
        assertGinqScript '''
            assert [[1, 2], [3, 2]] == GQ {
// tag::ginq_grouping_10[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                having count() < 3
                select n, count()
// end::ginq_grouping_10[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 19"() {
        assertGinqScript '''
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
            def persons = [new Person('Linda', 100, 'Female'), new Person('Daniel', 135, 'Male'), new Person('David', 122, 'Male')]
            assert [['Male', 128.5], ['Female', 100]] == GQ {
                from p in persons
                groupby p.gender
                select p.gender, avg(p.weight)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 20"() {
        assertGinqScript '''
            assert [[1, 1], [3, 3], [6, 6]] == GQ {
// tag::ginq_grouping_11[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, avg(n)
// end::ginq_grouping_11[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 21"() {
        assertGinqScript '''
            def test() {
                final int x = 1
                assert [[1, 1, 2, 2], [6, 1, 7, 3], [3, 1, 4, 2]] == GQ {
                    from n in [1, 1, 3, 3, 6, 6, 6]
                    groupby n, x
                    select n, x, n + x, count(n)
                }.toList()
            }
            test()
        '''
    }

    @Test
    void "testGinq - from groupby select - 22"() {
        assertGinqScript '''
            def test() {
                final int x = 1
                assert [[1, 1, 2, 2], [6, 1, 7, 3], [3, 1, 4, 2]] == GQ {
                    from n in [1, 1, 3, 3, 6, 6, 6]
                    groupby n, x
                    select n, x, n + x as nPlusX, count(n)
                }.toList()
            }
            test()
        '''
    }

    @Test
    void "testGinq - from groupby select - 23"() {
        assertGinqScript '''
            assert [[1, 9, 2], [3, 9, 2], [6, 9, 3]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, 9, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 24"() {
        assertGinqScript '''
            assert [[1, 10, 2], [3, 12, 2], [6, 15, 3]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, n + 9, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 25"() {
        assertGinqScript '''
            def same(obj) { obj }
            assert [[1, 2], [3, 2], [6, 3]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, count(n + 9)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 26"() {
        assertGinqScript '''
            def same(obj) { obj }
            assert [[1, 2], [3, 2], [6, 3]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, count(same(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 27"() {
        assertGinqScript '''
            assert [[1, 2], [3, 2], [6, 3]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, count(sum(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 28"() {
        assertGinqScript '''
            assert [[1, 3], [3, 5], [6, 9]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, n + count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 29"() {
        assertGinqScript '''
            assert [[1, 3], [3, 5], [6, 9]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, n + count(n) as nPlusCount
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby select - 30"() {
        assertGinqScript '''
            assert [[1, 1], [3, 3], [6, 6]] == GQ {
// tag::ginq_grouping_12[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                select n, median(n)
// end::ginq_grouping_12[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where groupby select - 1"() {
        assertGinqScript '''
            assert [[1, 2], [6, 3]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                where n != 3
                groupby n
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from where groupby orderby select - 1"() {
        assertGinqScript '''
            assert [[6, 3], [1, 2]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                where n != 3
                groupby n
                orderby n in desc
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby orderby select - 1"() {
        assertGinqScript '''
            assert [[6, 9], [1, 4]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                innerjoin m in [1, 1, 3, 3, 6, 6, 6] on n == m
                where n != 3
                groupby n
                orderby n in desc
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby orderby select - 2"() {
        assertGinqScript '''
            assert [[1, 4], [6, 9]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                innerjoin m in [1, 1, 3, 3, 6, 6, 6] on n == m
                where n != 3
                groupby n
                orderby count(n) in asc
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby orderby select - 3"() {
        assertGinqScript '''
            assert [[2, 3, 1], [1, 2, 1]] == GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on n + 1 == m
                where n != 3
                groupby n, m
                orderby n in desc
                select n, m, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby orderby select - 4"() {
        assertGinqScript '''
            assert [[1, 2, 1], [2, 3, 1]] == GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on n + 1 == m
                where n != 3
                groupby n, m
                orderby m in asc
                select n, m, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from groupby having select - 1"() {
        assertGinqScript '''
            assert [[3, 2], [6, 3]] == GQ {
// tag::ginq_grouping_02[]
                from n in [1, 1, 3, 3, 6, 6, 6]
                groupby n
                having n >= 3
                select n, count(n)
// end::ginq_grouping_02[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby having orderby select - 1"() {
        assertGinqScript '''
            assert [[1, 2, 1]] == GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on n + 1 == m
                where n != 3
                groupby n, m
                having n >= 1 && m < 3
                orderby m in asc
                select n, m, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby having orderby select - 2"() {
        assertGinqScript '''
            assert [[6, 9]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                innerjoin m in [1, 1, 3, 3, 6, 6, 6] on n == m
                where n != 3
                groupby n
                having count(m) > 4
                orderby count(n) in asc
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby having orderby select - 3"() {
        assertGinqScript '''
            assert [[6, 9]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                innerjoin m in [1, 1, 3, 3, 6, 6, 6] on n == m
                where n != 3
                groupby n
                having count(n) > 4
                orderby count(n) in asc
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerjoin where groupby having orderby select - 4"() {
        assertGinqScript '''
            assert [[6, 9]] == GQ {
                from n in [1, 1, 3, 3, 6, 6, 6]
                innerjoin m in [1, 1, 3, 3, 6, 6, 6] on n == m
                where n != 3
                groupby n
                having count() > 4
                orderby count(n) in asc
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - query json - 1"() {
        assertGinqScript """
            import groovy.json.JsonSlurper
            def parser = new JsonSlurper()
            def json = parser.parseText('''
                {
                    "persons": [
                        {"id": 1, "name": "Daniel"},
                        {"id": 2, "name": "Paul"},
                        {"id": 3, "name": "Eric"}
                    ],
                    "tasks": [
                        {"id": 1, "assignee": 1, "content": "task1", "manDay": 6},
                        {"id": 2, "assignee": 1, "content": "task2", "manDay": 1},
                        {"id": 3, "assignee": 2, "content": "task3", "manDay": 3},
                        {"id": 4, "assignee": 3, "content": "task4", "manDay": 5}
                    ]
                }
            ''')
    
            def expected = [
                    [taskId: 1, taskContent: 'task1', assignee: 'Daniel', manDay: 6],
                    [taskId: 4, taskContent: 'task4', assignee: 'Eric', manDay: 5],
                    [taskId: 3, taskContent: 'task3', assignee: 'Paul', manDay: 3]
            ]
    
            assert expected == GQ {
                from p in json.persons
                join t in json.tasks on t.assignee == p.id
                where t.id in [1, 3, 4]
                orderby t.manDay in desc
                select (taskId: t.id, taskContent: t.content, assignee: p.name, manDay: t.manDay)
            }.toList()
        """
    }

    @Test
    void "testGinq - query json - 2"() {
        assertGinqScript """
// tag::ginq_tips_04[]
            import groovy.json.JsonSlurper
            def json = new JsonSlurper().parseText('''
                {
                    "fruits": [
                        {"name": "Orange", "price": 11},
                        {"name": "Apple", "price": 6},
                        {"name": "Banana", "price": 4},
                        {"name": "Mongo", "price": 29},
                        {"name": "Durian", "price": 32}
                    ]
                }
            ''')
    
            def expected = [['Mongo', 29], ['Orange', 11], ['Apple', 6], ['Banana', 4]]
            assert expected == GQ {
                from f in json.fruits
                where f.price < 32
                orderby f.price in desc
                select f.name, f.price
            }.toList()
// end::ginq_tips_04[]
        """
    }

    @Test
    void "testGinq - ascii table - 1"() {
        assertGinqScript '''
            def q = GQ {
                from n in [1, 2, 3]
                select n as first_col, n + 1 as second_col
            }
    
            def expected = '\\n+-----------+------------+\\n| first_col | second_col |\\n+-----------+------------+\\n| 1         | 2          |\\n| 2         | 3          |\\n| 3         | 4          |\\n+-----------+------------+\\n\'
            assert expected == q.toString()
        '''
    }

    @Test
    void "testGinq - as List - 1"() {
        assertGinqScript '''
            assert [4, 16, 36, 64, 100] == GQ {from n in 1..<11 where n % 2 == 0 select n ** 2} as List
        '''
    }

    @Test
    void "testGinq - as Set - 1"() {
        assertGinqScript '''
            assert [4, 16, 36, 64, 100] as Set == GQ {from n in 1..<11 where n % 2 == 0 select n ** 2} as Set
        '''
    }

    @Test
    void "testGinq - customize GINQ target - 0"() {
        assertGinqScript '''
// tag::ginq_customize_01[]
            assert [0, 1, 2] == GQ(astWalker: 'org.apache.groovy.ginq.provider.collection.GinqAstWalker') {
                from n in [0, 1, 2]
                select n
            }.toList()
// end::ginq_customize_01[]
        '''
    }

    @Test
    void "testGinq - GINQ tips - 0"() {
        assertGinqScript '''
// tag::ginq_tips_01[]
            assert [4, 16, 36, 64, 100] == GQ {from n in 1..<11 where n % 2 == 0 select n ** 2}.toList()
// end::ginq_tips_01[]
        '''
    }

    @Test
    void "testGinq - GINQ tips - 1"() {
        assertGinqScript '''
// tag::ginq_tips_02[]
            assert [4, 16, 36, 64, 100] == GQ {from n in 1..<11 where n % 2 == 0 select n ** 2} as List
// end::ginq_tips_02[]
        '''
    }

    @Test
    void "testGinq - GINQ tips - 2"() {
        assertGinqScript '''
// tag::ginq_tips_03[]
            def result = []
            for (def x : GQ {from n in 1..<11 where n % 2 == 0 select n ** 2}) {
                result << x
            }
            assert [4, 16, 36, 64, 100] == result
// end::ginq_tips_03[]
        '''
    }

    @Test
    void "testGinq - GINQ tips - 3"() {
        assertGinqScript '''
// tag::ginq_tips_07[]
            import groovy.transform.*
            @TupleConstructor
            @EqualsAndHashCode
            @ToString
            class Person {
                String name
                String nickname
            }
            
            def linda = new Person('Linda', null)
            def david = new Person('David', null)
            def persons = [new Person('Daniel', 'ShanFengXiaoZi'), linda, david]
            def result = GQ {
                from p in persons
                where p.nickname == null
                select p
            }.stream()
                .peek(p -> { p.nickname = 'Unknown' }) // update `nickname`
                .toList()
            
            def expected = [new Person('Linda', 'Unknown'), new Person('David', 'Unknown')]
            assert expected == result
            assert ['Unknown', 'Unknown'] == [linda, david]*.nickname // ensure the original objects are updated
// end::ginq_tips_07[]
        '''
    }

    @Test
    void "testGinq - GINQ tips - 4"() {
        assertGinqScript '''
// tag::ginq_tips_12[]
            def v = GQ { from n in [1, 2, 3] where n < 3 select n }
            def result = GQ {
                from n in v
                where n > 1
                select n
            }
            assert [2] == result.toList()
// end::ginq_tips_12[]
        '''
    }

    @Test
    void "testGinq - GINQ examples - 1"() {
        assertGinqScript '''
            def expected = 
                [['1 * 1 = 1', '', '', '', '', '', '', '', ''],
                 ['1 * 2 = 2', '2 * 2 = 4', '', '', '', '', '', '', ''],
                 ['1 * 3 = 3', '2 * 3 = 6', '3 * 3 = 9', '', '', '', '', '', ''],
                 ['1 * 4 = 4', '2 * 4 = 8', '3 * 4 = 12', '4 * 4 = 16', '', '', '', '', ''],
                 ['1 * 5 = 5', '2 * 5 = 10', '3 * 5 = 15', '4 * 5 = 20', '5 * 5 = 25', '', '', '', ''],
                 ['1 * 6 = 6', '2 * 6 = 12', '3 * 6 = 18', '4 * 6 = 24', '5 * 6 = 30', '6 * 6 = 36', '', '', ''],
                 ['1 * 7 = 7', '2 * 7 = 14', '3 * 7 = 21', '4 * 7 = 28', '5 * 7 = 35', '6 * 7 = 42', '7 * 7 = 49', '', ''],
                 ['1 * 8 = 8', '2 * 8 = 16', '3 * 8 = 24', '4 * 8 = 32', '5 * 8 = 40', '6 * 8 = 48', '7 * 8 = 56', '8 * 8 = 64', ''],
                 ['1 * 9 = 9', '2 * 9 = 18', '3 * 9 = 27', '4 * 9 = 36', '5 * 9 = 45', '6 * 9 = 54', '7 * 9 = 63', '8 * 9 = 72', '9 * 9 = 81']]
            assert expected == GQ {
// tag::ginq_examples_01[]
                from v in (
                    from a in 1..9
                    join b in 1..9 on a <= b
                    select a as f, b as s, "$a * $b = ${a * b}".toString() as r
                )
                groupby v.s
                select max(v.f == 1 ? v.r : '') as v1,
                       max(v.f == 2 ? v.r : '') as v2,
                       max(v.f == 3 ? v.r : '') as v3,
                       max(v.f == 4 ? v.r : '') as v4,
                       max(v.f == 5 ? v.r : '') as v5,
                       max(v.f == 6 ? v.r : '') as v6,
                       max(v.f == 7 ? v.r : '') as v7,
                       max(v.f == 8 ? v.r : '') as v8,
                       max(v.f == 9 ? v.r : '') as v9
// end::ginq_examples_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - GINQ equals - 2"() {
        assertGinqScript '''
            def q1 = GQ {from n in [1, 2, 3] select n}
            def q2 = GQ {from n in [1, 2, 3] select n}
            assert q1 == q2
        '''
    }

    @Test
    void "testGinq - GINQ equals and hashCode - 2"() {
        assertGinqScript '''
            def set = new HashSet()
            def q1 = GQ {from n in [1, 2, 3] select n}
            def q2 = GQ {from n in [1, 2, 3] select n}
            set.add(q1)
            set.add(q2)
            assert 1 == set.size()
        '''
    }

    @Test
    void "testGinq - from iterable - 0"() {
        assertGinqScript '''
            assert [1, 2, 3] == GQ {
// tag::ginq_datasource_03[]
                from n in [1, 2, 3] select n
// end::ginq_datasource_03[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from stream - 0"() {
        assertGinqScript '''
            def stream = [1, 2, 3].stream()
            assert [1, 2, 3] == GQ {from n in stream select n}.toList()
        '''
    }

    @Test
    void "testGinq - from stream - 1"() {
        assertGinqScript '''
            assert [1, 2, 3] == GQ {
// tag::ginq_datasource_01[]
                from n in [1, 2, 3].stream() select n
// end::ginq_datasource_01[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from GINQ result set - 1"() {
        assertGinqScript '''
// tag::ginq_datasource_04[]
            def vt = GQ {from m in [1, 2, 3] select m}
            assert [1, 2, 3] == GQ {
                from n in vt select n
            }.toList()
// end::ginq_datasource_04[]
        '''
    }

    @Test
    void "testGinq - from array - 0"() {
        assertGinqScript '''
            assert [1, 2, 3] == GQ {
// tag::ginq_datasource_02[]
                from n in new int[] {1, 2, 3} select n
// end::ginq_datasource_02[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - row number - 0"() {
        assertGinqScript '''
            assert [[0, 1], [1, 2], [2, 3]] == GQ {
// tag::ginq_tips_05[]
                from n in [1, 2, 3] 
                select _rn, n
// end::ginq_tips_05[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - row number - 1"() {
        assertGinqScript '''
            assert [[0, 1, 2], [1, 2, 3]] == GQ {
                from v in (
                    from n in [1, 2, 3]
                    select _rn, n
                )
                where v.n > 1
                select _rn, v._rn as vRn, v.n
            }.toList()
        '''
    }

    @Test
    void "testGinq - row number - 2"() {
        assertGinqScript '''
            assert [0, 1, 2] == GQ {
                from n in [1, 2, 3]
                select _rn
            }.toList()
        '''
    }

    @Test
    void "testGinq - exists - 1"() {
        assertGinqScript '''
            assert [2, 3] == GQ {
// tag::ginq_filtering_02[]
                from n in [1, 2, 3]
                where (
                    from m in [2, 3]
                    where m == n
                    select m
                ).exists()
                select n
// end::ginq_filtering_02[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - exists - 2"() {
        assertGinqScript '''
            assert [2, 3] == GQ {
                from n in [1, 2, 3]
                where 1 == 1 && (
                    from m in [2, 3]
                    where m == n
                    select m
                ).exists()
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - exists - 3"() {
        assertGinqScript '''
            assert [1, 2] == GQ {
// tag::ginq_filtering_04[]
                from n in [0, 1, 2]
                where (
                    from m in [1, 2]
                    where m == n
                    select m
                ).exists()
                select n
// end::ginq_filtering_04[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - exists - 4"() {
        assertGinqScript '''
            assert [2, 3] == GQ {
                from n in [1, 2, 3]
                innerjoin m in [1, 2, 3] on m == n
                innerjoin k in [1, 2, 3] on k == n
                where (
                    from t in [2, 3]
                    where t == n && t == m && t == k
                    select t
                ).exists()
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - not exists - 1"() {
        assertGinqScript '''
            assert [1] == GQ {
// tag::ginq_filtering_03[]
                from n in [1, 2, 3]
                where !(
                    from m in [2, 3]
                    where m == n
                    select m
                ).exists()
                select n
// end::ginq_filtering_03[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - not exists - 2"() {
        assertGinqScript '''
            assert [1] == GQ {
                from n in [1, 2, 3]
                where 1 == 1 && !(
                    from m in [2, 3]
                    where m == n
                    select m
                ).exists()
                select n
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 0"() {
        assertGinqScript '''
            assert [[null], [2], [3]] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    where m == n
                    select m   
                ) as sqr
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 1"() {
        assertGinqScript '''
// tag::ginq_nested_04[]
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n in [1, 2, 3]
                select n, (
                    from m in [2, 3, 4]
                    where m == n
                    select m   
                ) as sqr
            }.toList()
// end::ginq_nested_04[]
        '''
    }

    @Test
    void "testGinq - subQuery - 2"() {
        assertGinqScript '''
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n in [1, 2, 3]
                innerjoin k in [1, 2, 3] on k == n
                select n, (
                    from m in [2, 3, 4]
                    where m == n && m == k
                    select m   
                ) as sqr
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 3"() {
        assertGinqScript '''
            assert [null, 2, 3] == GQ {
                from n in [1, 2, 3]
                innerjoin k in [1, 2, 3] on k == n
                select (
                    from m in [2, 3, 4]
                    where m == n && m == k
                    select m   
                )
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 4"() {
        assertGinqScript '''
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from n in [1, 2, 3]
                innerjoin k in [1, 2, 3] on k == n
                select n, (
                    from m in [2, 3, 4]
                    where m == n && m == k
                    select m   
                )
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 5"() {
        assertGinqScript '''
// tag::ginq_nested_03[]
            assert [null, 2, 3] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    where m == n
                    limit 1
                    select m   
                )
            }.toList()
// end::ginq_nested_03[]
        '''
    }

    @Test
    void "testGinq - subQuery - 6"() {
        assertGinqScript '''
            assert [[null, null], [2, null], [3, 3]] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    where m == n
                    limit 1
                    select m   
                ), (
                    from m in [3, 4, 5]
                    where m == n
                    limit 1
                    select m   
                )
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 7"() {
        assertGinqScript '''
            assert [[null, null], [2, null], [3, 3]] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    where m == n
                    limit 1
                    select m   
                ) as sqr1, (
                    from m in [3, 4, 5]
                    where m == n
                    limit 1
                    select m   
                ) as sqr2
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 8"() {
        assertGinqScript '''
            assert [[1, null], [2, 2], [3, 3]] == GQ {
                from v in (
                    from n in [1, 2, 3]
                    select n, (
                        from m in [2, 3, 4]
                        where m == n
                        select m   
                    ) as sqr
                )
                select v.n, v.sqr
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 9"() {
        assertGinqScript '''
            assert [9, 7, 4] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    where m > n
                    select sum(m)  
                )
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 10"() {
        assertGinqScript '''
            assert [null, 2, 3] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    innerjoin k in [2, 3, 4] on k == m
                    where m == n
                    select k
                )
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 11"() {
        assertGinqScript '''
            assert [null, 2, 3] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    innerjoin k in [2, 3, 4] on k == m
                    where m == n
                    orderby m in desc
                    select k
                )
            }.toList()
        '''
    }

    @Test
    void "testGinq - subQuery - 12"() {
        assertGinqScript '''
            assert [null, 2, 3] == GQ {
                from n in [1, 2, 3]
                select (
                    from m in [2, 3, 4]
                    innerjoin k in [2, 3, 4] on k == m
                    where m == n
                    orderby m in desc
                    limit 0, 3
                    select k
                )
            }.toList()
        '''
    }

    @Test
    void "testGinq - lazy - 1"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                select n, cnt++
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[1, 0], [2, 1], [3, 2]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 2"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                where n > 1
                select n, cnt++
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[2, 0], [3, 1]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 3"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on m == n
                where n > 1 && m < 5
                select n, m, cnt++
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[2, 2, 0], [3, 3, 1]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 4"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on m == n
                where n > 1 && m < 5
                groupby n, m
                select n, m, agg(_g.stream().map(r -> r.n + r.m + cnt++).reduce(BigDecimal.ZERO, BigDecimal::add))
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[3, 3, 6], [2, 2, 5]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 5"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on m == n
                where n > 1 && m < 5
                groupby n, m
                orderby n in desc, m in asc
                select n, m, agg(_g.stream().map(r -> r.n + r.m + cnt++).reduce(BigDecimal.ZERO, BigDecimal::add))
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[3, 3, 6], [2, 2, 5]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 6"() {
        assumeTrue(isAtLeastJdk('9.0'))

        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on m == n
                where n > 1 && m < 5 && (
                    from k in [2, 3, 4, 5]
                    select k, cnt++
                ).exists()
                groupby n, m
                orderby n in desc, m in asc
                select n, m, agg(_g.stream().map(r -> r.n + r.m + cnt++).reduce(BigDecimal.ZERO, BigDecimal::add))
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[3, 3, 6], [2, 2, 5]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 7"() {
        assumeTrue(isAtLeastJdk('9.0'))

        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                innerjoin m in [2, 3, 4] on m == n
                where n > 1 && m < 5 && (
                    from k in [2, 3, 4, 5]
                    select k, cnt++
                ).exists()
                groupby n, m
                orderby n in desc, m in asc
                limit 0, 1
                select n, m, agg(_g.stream().map(r -> r.n + r.m + cnt++).reduce(BigDecimal.ZERO, BigDecimal::add))
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[3, 3, 6]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 8"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                where (
                    from k in [2, 3, 4, 5]
                    where k == n
                    select k, cnt++
                ).exists()
                select n, cnt++
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[2, 1], [3, 3]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 9"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [0, 1, 2, 3, 4, 5]
                where n in (
                    from k in [0, 1, 2, 3, 4]
                    select cnt++ - k
                )
                select n, cnt++
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[0, 5]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 10"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [0, 1, 2, 3, 4, 5]
                where n in (
                    from k in [0, 1, 2, 3, 4]
                    where k >= n
                    select cnt++ - k
                )
                select n, cnt++
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[0, 5]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - lazy - 11"() {
        assertGinqScript '''
            int cnt = 0
            def result = GQ {
                from n in [1, 2, 3]
                innerhashjoin m in [2, 3, 4] on m == n
                where n > 1 && m < 5
                select n, m, cnt++
            }
            assert 0 == cnt
            def stream = result.stream()
            assert 0 == cnt
            assert [[2, 2, 0], [3, 3, 1]] == stream.toList()
            assert cnt > 0
        '''
    }

    @Test
    void "testGinq - agg function - 1"() {
        assertGinqScript '''
// tag::ginq_aggfunction_01[]
            assert [[1, 3, 2, 2, 6, 3, 3, 6]] == GQ {
                from n in [1, 2, 3]
                select min(n), max(n), avg(n), median(n), sum(n), count(n), count(), 
                        agg(_g.stream().map(r -> r.n).reduce(BigDecimal.ZERO, BigDecimal::add))
            }.toList()
// end::ginq_aggfunction_01[]
        '''
    }

    @Test
    void "testGinq - agg function - 2"() {
        assertGinqScript '''
            assert [1] == GQ {
                from n in [1, 2, 3]
                select min(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 3"() {
        assertGinqScript '''
// tag::ginq_aggfunction_02[]
            assert [3] == GQ {
                from n in [1, 2, 3]
                select max(n)
            }.toList()
// end::ginq_aggfunction_02[]
        '''
    }

    @Test
    void "testGinq - agg function - 4"() {
        assertGinqScript '''
            assert [2] == GQ {
                from n in [1, 2, 3]
                select avg(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 5"() {
        assertGinqScript '''
            assert [6] == GQ {
                from n in [1, 2, 3]
                select sum(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 6"() {
        assertGinqScript '''
            assert [3] == GQ {
                from n in [1, 2, 3]
                select count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 7"() {
        assertGinqScript '''
            assert [3] == GQ {
                from n in [1, 2, 3]
                select count()
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 8"() {
        assertGinqScript '''
            assert [6] == GQ {
                from n in [1, 2, 3]
                select agg(_g.stream().map(r -> r.n).reduce(BigDecimal.ZERO, BigDecimal::add))
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 9"() {
        assertGinqScript '''
            assert [3] == GQ {
                from n in [1, 2, 3]
                select sum(1)
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 10"() {
        assertGinqScript '''
            assert [0] == GQ {
                from n in []
                select count()
            }.toList()
        '''
    }

    @Test
    void "testGinq - agg function - 11"() {
        assertGinqScript '''
            assert [[null, null]] == GQ {
                from n in []
                select max(n), min(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - GQL - 1"() {
        assertGinqScript '''
// tag::ginq_tips_06[]
            assert [4, 16, 36, 64, 100] == GQL {from n in 1..<11 where n % 2 == 0 select n ** 2}
// end::ginq_tips_06[]
        '''
    }

    @Test
    void "testGinq - GQL - 2"() {
        assertGinqScript '''
            def result = GQL {from n in [1] select n}
            assert result instanceof List
            assert 1 == result[0]
        '''
    }

    @Test
    void "testGinq - asType - 1"() {
        assertGinqScript '''
            def result = GQ {from n in [1] select n} as Collection
            assert result instanceof List
            assert 1 == result[0]
        '''
    }

    @Test
    void "testGinq - asType - 2"() {
        assertGinqScript '''
            def result = GQ {from n in [1] select n} as int[]
            assert result instanceof int[]
            assert 1 == result[0]
        '''
    }

    @Test
    void "testGinq - asType - 3"() {
        assertGinqScript '''
            def result = GQ {from n in [1] select n} as Set
            assert result instanceof Set
            assert 1 == result[0]
        '''
    }

    @Test
    void "testGinq - asType - 4"() {
        assertGinqScript '''
            import java.util.stream.Stream
            
            def result = GQ {from n in [1] select n} as Stream
            assert result instanceof Stream
            assert 1 == result.findFirst().get()
        '''
    }

    @Test
    void "testGinq - asType - 5"() {
        assertGinqScript '''
            def result = GQ {from n in [1] select n} as Iterable
            assert result instanceof List
            assert 1 == result[0]
        '''
    }

    @Test
    void "testGinq - asType - 6"() {
        assertGinqScript '''
            def result = GQ {from n in [1] select n} as Iterator
            assert result instanceof Iterator
            assert 1 == result.next()
        '''
    }

    @Test
    void "testGinq - asType - 7"() {
        assertGinqScript '''
            import org.apache.groovy.ginq.provider.collection.runtime.Queryable
            
            def original = GQ {from n in [1] select n}
            def result = original as Queryable
            assert original === result
        '''
    }

    @Test
    void "testGinq - asType - 8"() {
        assertGinqScript '''
            def original = GQ {from n in [1] select n}
            def result = original as Object
            assert original === result
        '''
    }

    @Test
    void "testGinq - asType - 9"() {
        assertGinqScript '''
            def result = GQ {from n in [1] select n} as List
            assert result instanceof List
            assert 1 == result[0]
        '''
    }

    @Test
    @CompileDynamic
    void "testGinq - optimize - 1"() {
        def code = '''
            def hello() {
                def gqc = {
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 3
                    select n1, n2
                }
                return
            }
        '''
        def sourceUnit
        def ast = new CompilationUnit().tap {
            sourceUnit = addSource 'hello.groovy', code
            compile Phases.CONVERSION
        }.ast

        MethodNode methodNode = ast.classes[0].methods.grep(e -> e.name == 'hello')[0]
        ExpressionStatement delcareStatement = ((BlockStatement) methodNode.getCode()).getStatements()[0]
        DeclarationExpression declarationExpression = delcareStatement.getExpression()
        ClosureExpression closureException = declarationExpression.rightExpression

        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)
        closureException.code.visit(ginqAstBuilder)
        GinqExpression ginqExpression = ginqAstBuilder.getGinqExpression()

        GinqAstOptimizer ginqAstOptimizer = new GinqAstOptimizer()
        ginqAstOptimizer.visitGinqExpression(ginqExpression)
        assert null == ginqExpression.whereExpression

        assert ginqExpression.fromExpression.dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr1 = ((GinqExpression) ginqExpression.fromExpression.dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_GREATER_THAN == contructedFilterExpr1.operation.type
        assert '1' == contructedFilterExpr1.rightExpression.text

        assert ginqExpression.joinExpressionList[0].dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr2 = ((GinqExpression) ginqExpression.joinExpressionList[0].dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_LESS_THAN_EQUAL == contructedFilterExpr2.operation.type
        assert '3' == contructedFilterExpr2.rightExpression.text
    }

    @Test
    @CompileDynamic
    void "testGinq - optimize - 2"() {
        def code = '''
            def hello() {
                def gqc = {
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 3 && 1 < 2 && 3 == 3 && 4 > 3 && 'a' < 'b' && "a's" < "b's" && true && !false
                    select n1, n2
                }
                return
            }
        '''
        def sourceUnit
        def ast = new CompilationUnit().tap {
            sourceUnit = addSource 'hello.groovy', code
            compile Phases.CONVERSION
        }.ast

        MethodNode methodNode = ast.classes[0].methods.grep(e -> e.name == 'hello')[0]
        ExpressionStatement delcareStatement = ((BlockStatement) methodNode.getCode()).getStatements()[0]
        DeclarationExpression declarationExpression = delcareStatement.getExpression()
        ClosureExpression closureException = declarationExpression.rightExpression

        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)
        closureException.code.visit(ginqAstBuilder)
        GinqExpression ginqExpression = ginqAstBuilder.getGinqExpression()

        GinqAstOptimizer ginqAstOptimizer = new GinqAstOptimizer()
        ginqAstOptimizer.visitGinqExpression(ginqExpression)
        assert null == ginqExpression.whereExpression

        assert ginqExpression.fromExpression.dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr1 = ((GinqExpression) ginqExpression.fromExpression.dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_GREATER_THAN == contructedFilterExpr1.operation.type
        assert '1' == contructedFilterExpr1.rightExpression.text

        assert ginqExpression.joinExpressionList[0].dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr2 = ((GinqExpression) ginqExpression.joinExpressionList[0].dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_LESS_THAN_EQUAL == contructedFilterExpr2.operation.type
        assert '3' == contructedFilterExpr2.rightExpression.text
    }

    @Test
    @CompileDynamic
    void "testGinq - optimize - 3"() {
        def code = '''
            def hello() {
                def gqc = {
                    from n1 in nums1
                    leftjoin n2 in nums2 on n1 == n2
                    where n1 > 1 && n2 <= 3
                    select n1, n2
                }
                return
            }
        '''
        def sourceUnit
        def ast = new CompilationUnit().tap {
            sourceUnit = addSource 'hello.groovy', code
            compile Phases.CONVERSION
        }.ast

        MethodNode methodNode = ast.classes[0].methods.grep(e -> e.name == 'hello')[0]
        ExpressionStatement delcareStatement = ((BlockStatement) methodNode.getCode()).getStatements()[0]
        DeclarationExpression declarationExpression = delcareStatement.getExpression()
        ClosureExpression closureException = declarationExpression.rightExpression

        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)
        closureException.code.visit(ginqAstBuilder)
        GinqExpression ginqExpression = ginqAstBuilder.getGinqExpression()

        GinqAstOptimizer ginqAstOptimizer = new GinqAstOptimizer()
        ginqAstOptimizer.visitGinqExpression(ginqExpression)
        BinaryExpression filterExpr = (BinaryExpression) ginqExpression.whereExpression.filterExpr
        assert 'n2' == filterExpr.leftExpression.text
        assert Types.COMPARE_LESS_THAN_EQUAL == filterExpr.operation.type
        assert '3' == filterExpr.rightExpression.text

        assert ginqExpression.fromExpression.dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr1 = ((GinqExpression) ginqExpression.fromExpression.dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_GREATER_THAN == contructedFilterExpr1.operation.type
        assert '1' == contructedFilterExpr1.rightExpression.text

        assert ginqExpression.joinExpressionList[0].dataSourceExpr !instanceof GinqExpression
    }

    @Test
    void "testGinq - optimize - 4"() {
        assertGinqScript '''
// tag::ginq_optimize_01[]
            assert [[2, 2]] == GQ(optimize: false) {
                from n1 in [1, 2, 3]
                join n2 in [1, 2, 3] on n1 == n2
                where n1 > 1 &&  n2 < 3
                select n1, n2
            }.toList()
// end::ginq_optimize_01[]
        '''
    }

    @Test
    @CompileDynamic
    void "testGinq - optimize - 5"() {
        def code = '''
            def hello() {
                def gqc = {
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 > 1 || n2 <= 3
                    select n1, n2
                }
                return
            }
        '''
        def sourceUnit
        def ast = new CompilationUnit().tap {
            sourceUnit = addSource 'hello.groovy', code
            compile Phases.CONVERSION
        }.ast

        MethodNode methodNode = ast.classes[0].methods.grep(e -> e.name == 'hello')[0]
        ExpressionStatement delcareStatement = ((BlockStatement) methodNode.getCode()).getStatements()[0]
        DeclarationExpression declarationExpression = delcareStatement.getExpression()
        ClosureExpression closureException = declarationExpression.rightExpression

        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)
        closureException.code.visit(ginqAstBuilder)
        GinqExpression ginqExpression = ginqAstBuilder.getGinqExpression()

        GinqAstOptimizer ginqAstOptimizer = new GinqAstOptimizer()
        ginqAstOptimizer.visitGinqExpression(ginqExpression)
        assert null != ginqExpression.whereExpression

        assert ginqExpression.fromExpression.dataSourceExpr !instanceof GinqExpression
        assert ginqExpression.joinExpressionList[0].dataSourceExpr !instanceof GinqExpression
    }

    @Test
    @CompileDynamic
    void "testGinq - optimize - 6"() {
        def code = '''
            def hello() {
                def gqc = {
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where n1 in (
                        from m1 in [1, 2, 3]
                        innerjoin m2 in [2, 3, 4] on m2 == m1
                        where m1 > 2 && m2 < 4
                        select m1
                    ) && n1 > 1 && n2 <= 3
                    select n1, n2
                }
                return
            }
        '''
        def sourceUnit
        def ast = new CompilationUnit().tap {
            sourceUnit = addSource 'hello.groovy', code
            compile Phases.CONVERSION
        }.ast

        MethodNode methodNode = ast.classes[0].methods.grep(e -> e.name == 'hello')[0]
        ExpressionStatement delcareStatement = ((BlockStatement) methodNode.getCode()).getStatements()[0]
        DeclarationExpression declarationExpression = delcareStatement.getExpression()
        ClosureExpression closureException = declarationExpression.rightExpression

        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)
        closureException.code.visit(ginqAstBuilder)
        GinqExpression ginqExpression = ginqAstBuilder.getGinqExpression()

        GinqAstOptimizer ginqAstOptimizer = new GinqAstOptimizer()
        ginqAstOptimizer.visitGinqExpression(ginqExpression)
        BinaryExpression filterExpr = ginqExpression.whereExpression.filterExpr
        assert 'n1' == filterExpr.leftExpression.text
        assert Types.KEYWORD_IN == filterExpr.operation.type
        GinqExpression nestedGinq = filterExpr.rightExpression
        assert nestedGinq.fromExpression.dataSourceExpr instanceof GinqExpression

        BinaryExpression constructedFilterExpr1OfNestedGinq = ((GinqExpression) nestedGinq.fromExpression.dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_GREATER_THAN == constructedFilterExpr1OfNestedGinq.operation.type
        assert '2' == constructedFilterExpr1OfNestedGinq.rightExpression.text

        assert nestedGinq.joinExpressionList[0].dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr2OfNestedGinq = ((GinqExpression) nestedGinq.joinExpressionList[0].dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_LESS_THAN == contructedFilterExpr2OfNestedGinq.operation.type
        assert '4' == contructedFilterExpr2OfNestedGinq.rightExpression.text

        assert null == nestedGinq.whereExpression


        assert ginqExpression.fromExpression.dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr1 = ((GinqExpression) ginqExpression.fromExpression.dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_GREATER_THAN == contructedFilterExpr1.operation.type
        assert '1' == contructedFilterExpr1.rightExpression.text

        assert ginqExpression.joinExpressionList[0].dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr2 = ((GinqExpression) ginqExpression.joinExpressionList[0].dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_LESS_THAN_EQUAL == contructedFilterExpr2.operation.type
        assert '3' == contructedFilterExpr2.rightExpression.text
    }

    @Test
    @CompileDynamic
    void "testGinq - optimize - 7"() {
        def code = '''
            def hello() {
                def gqc = {
                    from n1 in nums1
                    innerjoin n2 in nums2 on n1 == n2
                    where (
                        from m1 in [1, 2, 3]
                        innerjoin m2 in [2, 3, 4] on m2 == m1
                        where m1 > 2 && m2 < 4 && m1 == n1
                        select m1
                    ).exists() && n1 > 1 && n2 <= 3
                    select n1, n2
                }
                return
            }
        '''
        def sourceUnit
        def ast = new CompilationUnit().tap {
            sourceUnit = addSource 'hello.groovy', code
            compile Phases.CONVERSION
        }.ast

        MethodNode methodNode = ast.classes[0].methods.grep(e -> e.name == 'hello')[0]
        ExpressionStatement delcareStatement = ((BlockStatement) methodNode.getCode()).getStatements()[0]
        DeclarationExpression declarationExpression = delcareStatement.getExpression()
        ClosureExpression closureException = declarationExpression.rightExpression

        GinqAstBuilder ginqAstBuilder = new GinqAstBuilder(sourceUnit)
        closureException.code.visit(ginqAstBuilder)
        GinqExpression ginqExpression = ginqAstBuilder.getGinqExpression()

        GinqAstOptimizer ginqAstOptimizer = new GinqAstOptimizer()
        ginqAstOptimizer.visitGinqExpression(ginqExpression)
        MethodCallExpression filterExpr = ginqExpression.whereExpression.filterExpr
        assert 'exists' == filterExpr.methodAsString
        GinqExpression nestedGinq = filterExpr.objectExpression
        assert nestedGinq.fromExpression.dataSourceExpr instanceof GinqExpression

        BinaryExpression constructedFilterExpr1OfNestedGinq = ((GinqExpression) nestedGinq.fromExpression.dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_GREATER_THAN == constructedFilterExpr1OfNestedGinq.operation.type
        assert '2' == constructedFilterExpr1OfNestedGinq.rightExpression.text

        assert nestedGinq.joinExpressionList[0].dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr2OfNestedGinq = ((GinqExpression) nestedGinq.joinExpressionList[0].dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_LESS_THAN == contructedFilterExpr2OfNestedGinq.operation.type
        assert '4' == contructedFilterExpr2OfNestedGinq.rightExpression.text

        BinaryExpression filterExprOfNestedGinq = nestedGinq.whereExpression.filterExpr
        assert 'm1' == filterExprOfNestedGinq.leftExpression.text
        assert Types.COMPARE_EQUAL == filterExprOfNestedGinq.operation.type
        assert 'n1' == filterExprOfNestedGinq.rightExpression.text


        assert ginqExpression.fromExpression.dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr1 = ((GinqExpression) ginqExpression.fromExpression.dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_GREATER_THAN == contructedFilterExpr1.operation.type
        assert '1' == contructedFilterExpr1.rightExpression.text

        assert ginqExpression.joinExpressionList[0].dataSourceExpr instanceof GinqExpression
        BinaryExpression contructedFilterExpr2 = ((GinqExpression) ginqExpression.joinExpressionList[0].dataSourceExpr).whereExpression.filterExpr
        assert Types.COMPARE_LESS_THAN_EQUAL == contructedFilterExpr2.operation.type
        assert '3' == contructedFilterExpr2.rightExpression.text
    }

    @Test
    void "testGinq - from innerhashjoin select - 0"() {
        assertGinqScript '''
            assert [[1, 1], [3, 3]] == GQ {
// tag::ginq_joining_06[]
                from n1 in [1, 2, 3]
                innerhashjoin n2 in [1, 3] on n1 == n2
                select n1, n2
// end::ginq_joining_06[]
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerhashjoin select - 1"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                innerhashjoin n2 in nums2 on n1 == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerhashjoin select - 2"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [2, 3, 4]
            assert [[2, 2], [3, 3]] == GQ {
                from n1 in nums1
                innerhashjoin n2 in nums2 on n2 == n1
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerhashjoin select - 3"() {
        assertGinqScript '''
            def nums1 = [1, 2, 3]
            def nums2 = [1, 4, 9]
            assert [[1, 1], [2, 4], [3, 9]] == GQ {
                from n1 in nums1
                innerhashjoin n2 in nums2 on ((int) Math.pow(n1, 2)) == n2
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerhashjoin select - 4"() {
        assertGinqScript '''
            import java.util.stream.Collectors
            
            @groovy.transform.EqualsAndHashCode
            class Person {
                String name
                int age
                Person(String name, int age) {
                    this.name = name
                    this.age = age
                }
            }
            
            def persons1 = [new Person('Daniel', 36), new Person('Peter', 15), new Person('Linda', 23)]
            def persons2 = [new Person('Daniel', 36), new Person('Tom', 15), new Person('Rose', 23)]
            
            assert [new Person('Daniel', 36)] == GQ {
                from p1 in persons1
                innerhashjoin p2 in persons2 on p1.name == p2.name && p1.age == p2.age
                select p1
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerhashjoin select - 5"() {
        assertGinqScript '''
            assert [[1, 1], [2, 2], [3, 3]] == GQ {
                from n1 in 1..1000
                innerhashjoin n2 in 1..10000 on n2 == n1
                where n1 <= 3 && n2 <= 5
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - from innerhashjoin groupby select - 1"() {
        assertGinqScript '''
            import java.util.stream.Collectors
            
            assert [[2, 2, 2, 'bc'], [3, 3, 3, 'def']] == GQ {
                from n in [1, 2, 3]
                innerhashjoin m in [1, 2, 3] on m == n
                innerhashjoin s in ['bc', 'def'] on n == s.length()
                groupby n, m, s.length()
                select n, m, s.length(), agg(_g.stream().map(r -> r.s).collect(Collectors.joining()))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 1"() {
        assertGinqScript '''
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select Math.abs(max(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 2"() {
        assertGinqScript '''
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select java.lang.Math.abs(max(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 3"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            def mm = new MyMath()
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select mm.abs(max(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 4"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            class MyMathHolder {
                def mm = new MyMath()
            }
            def mmHolder = new MyMathHolder()
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select mmHolder.mm.abs(max(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 5"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select new MyMath().abs(max(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 6"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            class MyMathHolder {
                def mm = new MyMath()
            }
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select new MyMathHolder().mm.abs(max(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 7"() {
        assertGinqScript '''
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select max(Math.abs(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 8"() {
        assertGinqScript '''
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select max(java.lang.Math.abs(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 9"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            def mm = new MyMath()
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select max(mm.abs(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 10"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            class MyMathHolder {
                def mm = new MyMath()
            }
            def mmHolder = new MyMathHolder()
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select max(mmHolder.mm.abs(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 11"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select max(new MyMath().abs(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 12"() {
        assertGinqScript '''
            class MyMath {
                def abs(n) {
                    Math.abs(n)
                }
            }
            class MyMathHolder {
                def mm = new MyMath()
            }
            assert [3, 2, 1] == GQ {
                from n in [-1, -2, -3]
                groupby n
                select max(new MyMathHolder().mm.abs(n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - aggregate function - 13"() {
        assertGinqScript '''
// tag::ginq_aggfunction_03[]
            assert [0.816496580927726] == GQ {
                from n in [1, 2, 3]
                select stdev(n)
            }.toList()
// end::ginq_aggfunction_03[]
        '''
    }

    @Test
    void "testGinq - aggregate function - 14"() {
        assertGinqScript '''
// tag::ginq_aggfunction_04[]
            assert [1] == GQ {
                from n in [1, 2, 3]
                select stdevp(n)
            }.toList()
// end::ginq_aggfunction_04[]
        '''
    }

    @Test
    void "testGinq - aggregate function - 15"() {
        assertGinqScript '''
// tag::ginq_aggfunction_05[]
            assert [0.6666666666666667] == GQ {
                from n in [1, 2, 3]
                select var(n)
            }.toList()
// end::ginq_aggfunction_05[]
        '''
    }

    @Test
    void "testGinq - aggregate function - 16"() {
        assertGinqScript '''
// tag::ginq_aggfunction_06[]
            assert [1] == GQ {
                from n in [1, 2, 3]
                select varp(n)
            }.toList()
// end::ginq_aggfunction_06[]
        '''
    }

    @Test
    void "testGinq - parallel - 1"() {
        assertGinqScript '''
// tag::ginq_tips_08[]
            assert [[1, 1], [2, 2], [3, 3]] == GQ(parallel: true) {
                from n1 in 1..1000
                join n2 in 1..10000 on n2 == n1
                where n1 <= 3 && n2 <= 5
                select n1, n2
            }.toList()
// end::ginq_tips_08[]
        '''
    }

    @Test
    void "testGinq - parallel - 2"() {
        assertGinqScript '''
            assert [[1, 1], [2, 2], [3, 3]] == GQ(optimize: false, parallel: true) {
                from n1 in 1..1000
                innerhashjoin n2 in 1..10000 on n2 == n1
                where n1 <= 3 && n2 <= 5
                select n1, n2
            }.toList()
        '''
    }

    @Test
    void "testGinq - parallel - 3"() {
        assertGinqScript '''
            assert [[6, 9]] == GQ(optimize: false, parallel: true) {
                from n in [1, 1, 3, 3, 6, 6, 6]
                innerjoin m in [1, 1, 3, 3, 6, 6, 6] on n == m
                where n != 3
                groupby n
                having count() > 4
                orderby count(n) in asc
                select n, count(n)
            }.toList()
        '''
    }

    @Test
    void "testGinq - parallel - 4"() {
        assertGinqScript '''
            assert [[0, 1], [1, 2], [2, 3], [3, 4], [4, 5], 
                    [5, 6], [6, 7], [7, 8], [8, 9], [9, 10]] == GQ(parallel:true) {
                from n in 1..10
                select _rn, n
            }.toList()
        '''
    }

    @Test
    void "testGinq - parallel - 5"() {
        assertGinqScript '''
            assert [[0, 0, 1], [1, 1, 2], [2, 2, 3], [3, 3, 4], [4, 4, 5], 
                    [5, 5, 6], [6, 6, 7], [7, 7, 8], [8, 8, 9], [9, 9, 10]] == GQ(parallel:true) {
                from n in 1..10
                select _rn, (rowNumber() over(orderby n)), n
            }.toList()
        '''
    }

    @Test
    void "testGinq - parallel - 6"() {
        assertGinqScript '''
            assert [[0, 0], [1, 1], [2, 2], [3, 3], [4, 4],
                    [5, 5], [6, 6], [7, 7], [8, 8], [9, 9]] == GQ(parallel:true) {
                from n in 0..<10
                select n, (rowNumber() over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 0"() {
        assertGinqScript '''
// tag::ginq_winfunction_01[]
            assert [[2, 1], [1, null], [3, 2]] == GQ {
                from n in [2, 1, 3]
                select n, (lag(n) over(orderby n))
            }.toList()
// end::ginq_winfunction_01[]
        '''
    }

    @Test
    void "testGinq - window - 1"() {
        assertGinqScript '''
            assert [[2, 1], [1, null], [3, 2]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, (lag(n) over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 2"() {
        assertGinqScript '''
// tag::ginq_winfunction_02[]
            assert [[2, 3], [1, 2], [3, null]] == GQ {
                from n in [2, 1, 3]
                select n, (lead(n) over(orderby n))
            }.toList()
// end::ginq_winfunction_02[]
        '''
    }

    @Test
    void "testGinq - window - 3"() {
        assertGinqScript '''
            assert [[2, 3], [1, 2], [3, null]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, (lead(n) over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 4"() {
        assertGinqScript '''
// tag::ginq_winfunction_03[]
            assert [[2, 3], [1, 2], [3, null]] == GQ {
                from n in [2, 1, 3]
                select n, (lag(n) over(orderby n in desc))
            }.toList()
// end::ginq_winfunction_03[]
        '''
    }

    @Test
    void "testGinq - window - 5"() {
        assertGinqScript '''
            assert [[2, 3], [1, 2], [3, null]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, (lag(n) over(orderby n in desc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 6"() {
        assertGinqScript '''
            assert [[2, 1], [1, null], [3, 2]] == GQ {
                from n in [2, 1, 3]
                select n, (lead(n) over(orderby n in desc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 7"() {
        assertGinqScript '''
            assert [[2, 1], [1, null], [3, 2]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, (lead(n) over(orderby n in desc))
            }.toList()
        '''
    }


    @Test
    void "testGinq - window - 8"() {
        assertGinqScript '''
            assert [[2, 1], [1, null], [3, 2]] == GQ {
                from n in [2, 1, 3]
                select n, (lag(n) over(orderby n in asc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 9"() {
        assertGinqScript '''
            assert [[2, 1], [1, null], [3, 2]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, (lag(n) over(orderby n in asc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 10"() {
        assertGinqScript '''
// tag::ginq_winfunction_04[]
            assert [[2, 3], [1, 2], [3, null]] == GQ {
                from n in [2, 1, 3]
                select n, (lead(n) over(orderby n in asc))
            }.toList()
// end::ginq_winfunction_04[]
        '''
    }

    @Test
    void "testGinq - window - 11"() {
        assertGinqScript '''
            assert [[2, 3], [1, 2], [3, null]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, (lead(n) over(orderby n in asc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 12"() {
        assertGinqScript '''
            assert [[2, 2, 3], [1, 1, 2], [3, 3, null]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, m, (lead(n) over(orderby m in asc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 13"() {
        assertGinqScript '''
            assert [[2, 2, 3], [1, 1, 2], [3, 3, null]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, m, (lead(m) over(orderby n in asc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 14"() {
        assertGinqScript '''
            assert [[2, 2, 3], [1, 1, 2], [3, 3, null]] == GQ {
                from n in [2, 1, 3]
                join m in [2, 1, 3] on m == n
                select n, m, (lead(m) over(orderby m in asc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 15"() {
        assertGinqScript '''
// tag::ginq_winfunction_05[]
            assert [['a', null], ['b', 'a'], ['aa', null], ['bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                select s, (lag(s) over(partitionby s.length() orderby s))
            }.toList()
// end::ginq_winfunction_05[]
        '''
    }

    @Test
    void "testGinq - window - 16"() {
        assertGinqScript '''
            assert [['a', null], ['b', 'a'], ['aa', null], ['bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                join t in ['a', 'b', 'aa', 'bb'] on t == s
                select s, (lag(s) over(partitionby s.length() orderby s))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 17"() {
        assertGinqScript '''
            assert [['a', 'a', null], ['b', 'b', 'a'], ['aa', 'aa', null], ['bb', 'bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                join t in ['a', 'b', 'aa', 'bb'] on t == s
                select s, t, (lag(t) over(partitionby t.length() orderby t))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 18"() {
        assertGinqScript '''
            assert [['a', 'a', null], ['b', 'b', 'a'], ['aa', 'aa', null], ['bb', 'bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                join t in ['a', 'b', 'aa', 'bb'] on t == s
                select s, t, (lag(s) over(partitionby t.length() orderby t))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 19"() {
        assertGinqScript '''
            assert [['a', 'a', null], ['b', 'b', 'a'], ['aa', 'aa', null], ['bb', 'bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                join t in ['a', 'b', 'aa', 'bb'] on t == s
                select s, t, (lag(s) over(partitionby s.length() orderby t))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 20"() {
        assertGinqScript '''
            assert [['a', 'a', null], ['b', 'b', 'a'], ['aa', 'aa', null], ['bb', 'bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                join t in ['a', 'b', 'aa', 'bb'] on t == s
                select s, t, (lag(s) over(partitionby t.length() orderby s))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 21"() {
        assertGinqScript '''
            assert [['a', 'a', null], ['b', 'b', 'a'], ['aa', 'aa', null], ['bb', 'bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                join t in ['a', 'b', 'aa', 'bb'] on t == s
                select s, t, (lag(t) over(partitionby t.length() orderby s))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 22"() {
        assertGinqScript '''
            assert [['a', 'a', null], ['b', 'b', 'a'], ['aa', 'aa', null], ['bb', 'bb', 'aa']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                join t in ['a', 'b', 'aa', 'bb'] on t == s
                select s, t, (lag(t) over(partitionby s.length() orderby s))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 23"() {
        assertGinqScript '''
// tag::ginq_winfunction_06[]
            assert [[2, 3, 1], [1, 2, null], [3, null, 2]] == GQ {
                from n in [2, 1, 3]
                select n, (lead(n) over(orderby n)), (lag(n) over(orderby n))
            }.toList()
// end::ginq_winfunction_06[]
        '''
    }

    @Test
    void "testGinq - window - 24"() {
        assertGinqScript '''
// tag::ginq_winfunction_07[]
            assert [['a', 'bc'], ['ab', null], ['b', 'a'], ['bc', 'ab']] == GQ {
                from s in ['a', 'ab', 'b', 'bc']
                select s, (lead(s) over(orderby s.length(), s in desc))
            }.toList()
// end::ginq_winfunction_07[]
        '''
    }

    @Test
    void "testGinq - window - 25"() {
        assertGinqScript '''
// tag::ginq_winfunction_08[]
            assert [['a', null], ['ab', null], ['b', 'a'], ['bc', 'ab']] == GQ {
                from s in ['a', 'ab', 'b', 'bc']
                select s, (lead(s) over(partitionby s.length() orderby s.length(), s in desc))
            }.toList()
// end::ginq_winfunction_08[]
        '''
    }

    @Test
    void "testGinq - window - 26"() {
        assertGinqScript '''
// tag::ginq_winfunction_09[]
            assert [[2, 1], [1, 1], [3, 2]] == GQ {
                from n in [2, 1, 3]
                select n, (firstValue(n) over(orderby n rows -1, 1))
            }.toList()
// end::ginq_winfunction_09[]
        '''
    }

    @Test
    void "testGinq - window - 27"() {
        assertGinqScript '''
// tag::ginq_winfunction_10[]
            assert [[2, 3], [1, 2], [3, 3]] == GQ {
                from n in [2, 1, 3]
                select n, (lastValue(n) over(orderby n rows -1, 1))
            }.toList()
// end::ginq_winfunction_10[]
        '''
    }

    @Test
    void "testGinq - window - 28"() {
        assertGinqScript '''
// tag::ginq_winfunction_13[]
            assert [[2, 2], [1, 1], [3, 3]] == GQ {
                from n in [2, 1, 3]
                select n, (firstValue(n) over(orderby n rows 0, 1))
            }.toList()
// end::ginq_winfunction_13[]
        '''
    }

    @Test
    void "testGinq - window - 29"() {
        assertGinqScript '''
// tag::ginq_winfunction_12[]
            assert [[2, 2], [1, 1], [3, 3]] == GQ {
                from n in [2, 1, 3]
                select n, (lastValue(n) over(orderby n rows -1, 0))
            }.toList()
// end::ginq_winfunction_12[]
        '''
    }

    @Test
    void "testGinq - window - 30"() {
        assertGinqScript '''
// tag::ginq_winfunction_11[]
            assert [['a', 'a', 'b'], ['aa', 'aa', 'bb'], ['b', 'a', 'b'], ['bb', 'aa', 'bb']] == GQ {
                from s in ['a', 'aa', 'b', 'bb']
                select s, (firstValue(s) over(partitionby s.length() orderby s)),
                        (lastValue(s) over(partitionby s.length() orderby s))
            }.toList()
// end::ginq_winfunction_11[]
        '''
    }

    @Test
    void "testGinq - window - 31"() {
        assertGinqScript '''
            assert [[2, 1], [1, 1], [3, 1]] == GQ {
                from n in [2, 1, 3]
                select n, (firstValue(n) over(orderby n rows Long.MIN_VALUE, 1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 32"() {
        assertGinqScript '''
            assert [[2, 3], [1, 3], [3, 3]] == GQ {
                from n in [2, 1, 3]
                select n, (lastValue(n) over(orderby n rows -1, Long.MAX_VALUE))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 33"() {
        assertGinqScript '''
// tag::ginq_winfunction_14[]
            assert [[2, 1], [1, 1], [3, 1]] == GQ {
                from n in [2, 1, 3]
                select n, (firstValue(n) over(orderby n rows null, 1))
            }.toList()
// end::ginq_winfunction_14[]
        '''
    }

    @Test
    void "testGinq - window - 34"() {
        assertGinqScript '''
// tag::ginq_winfunction_15[]
            assert [[2, 3], [1, 3], [3, 3]] == GQ {
                from n in [2, 1, 3]
                select n, (lastValue(n) over(orderby n rows -1, null))
            }.toList()
// end::ginq_winfunction_15[]
        '''
    }

    @Test
    void "testGinq - window - 35"() {
        assertGinqScript '''
// tag::ginq_winfunction_16[]
            assert [[2, 1], [1, null], [3, 1]] == GQ {
                from n in [2, 1, 3]
                select n, (firstValue(n) over(orderby n rows -2, -1))
            }.toList()
// end::ginq_winfunction_16[]
        '''
    }

    @Test
    void "testGinq - window - 36"() {
        assertGinqScript '''
// tag::ginq_winfunction_17[]
            assert [[2, 1], [1, null], [3, 2]] == GQ {
                from n in [2, 1, 3]
                select n, (lastValue(n) over(orderby n rows -2, -1))
            }.toList()
// end::ginq_winfunction_17[]
        '''
    }

    @Test
    void "testGinq - window - 37"() {
        assertGinqScript '''
// tag::ginq_winfunction_18[]
            assert [[2, 3], [1, 3], [3, null]] == GQ {
                from n in [2, 1, 3]
                select n, (lastValue(n) over(orderby n rows 1, 2))
            }.toList()
// end::ginq_winfunction_18[]
        '''
    }

    @Test
    void "testGinq - window - 38"() {
        assertGinqScript '''
// tag::ginq_winfunction_19[]
            assert [[2, 3], [1, 2], [3, null]] == GQ {
                from n in [2, 1, 3]
                select n, (firstValue(n) over(orderby n rows 1, 2))
            }.toList()
// end::ginq_winfunction_19[]
        '''
    }

    @Test
    void "testGinq - window - 39"() {
        assertGinqScript '''
// tag::ginq_winfunction_20[]
            assert [[2, null, null], [1, 3, null], [3, null, 1]] == GQ {
                from n in [2, 1, 3]
                select n, (lead(n, 2) over(orderby n)), (lag(n, 2) over(orderby n))
            }.toList()
// end::ginq_winfunction_20[]
        '''
    }

    @Test
    void "testGinq - window - 40"() {
        assertGinqScript '''
// tag::ginq_winfunction_21[]
            assert [[2, 'NONE', 'NONE'], [1, 3, 'NONE'], [3, 'NONE', 1]] == GQ {
                from n in [2, 1, 3]
                select n, (lead(n, 2, 'NONE') over(orderby n)), (lag(n, 2, 'NONE') over(orderby n))
            }.toList()
// end::ginq_winfunction_21[]
        '''
    }

    @Test
    void "testGinq - window - 41"() {
        assertGinqScript '''
// tag::ginq_winfunction_22[]
            assert [['a', 'a', 'b'], ['b', 'a', 'b'], ['aa', 'aa', 'bb'], ['bb', 'aa', 'bb']] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                select s, (min(s) over(partitionby s.length())), (max(s) over(partitionby s.length()))
            }.toList()
// end::ginq_winfunction_22[]
        '''
    }

    @Test
    void "testGinq - window - 42"() {
        assertGinqScript '''
// tag::ginq_winfunction_23[]
            assert [[1, 2, 2, 2, 1, 1], [1, 2, 2, 2, 1, 1], 
                    [2, 2, 2, 4, 2, 2], [2, 2, 2, 4, 2, 2], 
                    [3, 2, 2, 6, 3, 3], [3, 2, 2, 6, 3, 3]] == GQ {
                from n in [1, 1, 2, 2, 3, 3]
                select n, (count() over(partitionby n)),
                          (count(n) over(partitionby n)),
                          (sum(n) over(partitionby n)), 
                          (avg(n) over(partitionby n)),
                          (median(n) over(partitionby n))
            }.toList()
// end::ginq_winfunction_23[]
        '''
    }

    @Test
    void "testGinq - window - 43"() {
        assertGinqScript '''
// tag::ginq_winfunction_24[]
            assert [[2, 1, 1, 1], [1, 0, 0, 2], [null, 3, 3, 3], [3, 2, 2, 0]] == GQ {
                from n in [2, 1, null, 3]
                select n, (rowNumber() over(orderby n)),
                          (rowNumber() over(orderby n in asc)),
                          (rowNumber() over(orderby n in desc))
            }.toList()
// end::ginq_winfunction_24[]
        '''
    }

    @Test
    void "testGinq - window - 44"() {
        assertGinqScript '''
// tag::ginq_winfunction_25[]
            assert [['a', 1, 1], ['b', 2, 2], ['b', 2, 2], 
                    ['c', 4, 3], ['c', 4, 3], ['d', 6, 4], 
                    ['e', 7, 5]] == GQ {
                from s in ['a', 'b', 'b', 'c', 'c', 'd', 'e']
                select s, 
                    (rank() over(orderby s)),
                    (denseRank() over(orderby s))
            }.toList()
// end::ginq_winfunction_25[]
        '''
    }

    @Test
    void "testGinq - window - 45"() {
        assertGinqScript '''
// tag::ginq_winfunction_26[]
            assert [[2, 6, 3, 1, 3, 4], [1, 6, 3, 1, 3, 4],
                    [3, 6, 3, 1, 3, 4], [null, 6, 3, 1, 3, 4]] == GQ {
                from n in [2, 1, 3, null]
                select n, (sum(n) over()), 
                          (max(n) over()), 
                          (min(n) over()),
                          (count(n) over()),
                          (count() over())
            }.toList()
// end::ginq_winfunction_26[]
        '''
    }

    @Test
    void "testGinq - window - 46"() {
        assertGinqScript '''
            assert [[1, 0], [1, 1], [2, 2], [2, 3]] == GQ {
                from n in [new Integer(1), new Integer(1), new Integer(2), new Integer(2)]
                select n, (rowNumber() over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 47"() {
        assertGinqScript '''
            assert [[1, 0], [1, 1], [2, 2], [2, 3]] == GQ {
                from n in [1, 1, 2, 2]
                select n, (rowNumber() over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 48"() {
        assertGinqScript '''
            assert [['aa', 0], ['aa', 1], ['bb', 2], ['bb', 3]] == GQ {
                from s in ['aa', 'aa', 'bb', 'bb']
                select s, (rowNumber() over(orderby s))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 49"() {
        assertGinqScript '''
            assert [['a', 0], ['b', 1], ['aa', 0], ['bb', 1]] == GQ {
                from s in ['a', 'b', 'aa', 'bb']
                select s, (rowNumber() over(partitionby s.length() orderby s))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 50"() {
        assertGinqScript '''
            assert [[1, 3, 2, 1, 2, 1.5, 1.5], [2, 6, 3, 1, 3, 2, 2], [3, 5, 3, 2, 2, 2.5, 2.5]] == GQ {
                from n in [1, 2, 3]
                select n, (sum(n) over(orderby n rows -1, 1)), 
                          (max(n) over(orderby n rows -1, 1)), 
                          (min(n) over(orderby n rows -1, 1)),
                          (count(n) over(orderby n rows -1, 1)),
                          (avg(n) over(orderby n rows -1, 1)),
                          (median(n) over(orderby n rows -1, 1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 51"() {
        assertGinqScript '''
// tag::ginq_winfunction_27[]
            assert [[1, 1, 1], [2, 2, 3], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range -2, 0)),
                          (sum(n) over(orderby n range -2, 0))
            }.toList()
// end::ginq_winfunction_27[]
        '''
    }

    @Test
    void "testGinq - window - 52"() {
        assertGinqScript '''
            assert [[1, 1, 1], [2, 2, 3], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range -2, 0)),
                          (sum(n) over(orderby n in desc range -2, 0))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 53"() {
        assertGinqScript '''
            assert [[1, 0, 0], [2, 1, 1], [5, 0, 0], [5, 0, 0]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range -2, -1)),
                          (sum(n) over(orderby n range -2, -1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 54"() {
        assertGinqScript '''
// tag::ginq_winfunction_32[]
            assert [[1, 0, 0], [2, 1, 1], [5, 0, 0], [5, 0, 0]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range -2, -1)), 
                          (sum(n) over(orderby n in desc range -2, -1))
            }.toList()
// end::ginq_winfunction_32[]
        '''
    }

    @Test
    void "testGinq - window - 55"() {
        assertGinqScript '''
            assert [[1, 1, 2], [2, 0, 0], [5, 0, 0], [5, 0, 0]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range 1, 2)),
                          (sum(n) over(orderby n range 1, 2))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 56"() {
        assertGinqScript '''
// tag::ginq_winfunction_31[]
            assert [[1, 1, 2], [2, 0, 0], [5, 0, 0], [5, 0, 0]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range 1, 2)), 
                          (sum(n) over(orderby n in desc range 1, 2))
            }.toList()
// end::ginq_winfunction_31[]
        '''
    }

    @Test
    void "testGinq - window - 57"() {
        assertGinqScript '''
// tag::ginq_winfunction_29[]
            assert [[1, 3, 12], [2, 2, 10], [5, 0, 0], [5, 0, 0]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range 1, null)), 
                          (sum(n) over(orderby n range 1, null))
            }.toList()
// end::ginq_winfunction_29[]
        '''
    }

    @Test
    void "testGinq - window - 58"() {
        assertGinqScript '''
            assert [[1, 3, 12], [2, 2, 10], [5, 0, 0], [5, 0, 0]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range 1, null)), 
                          (sum(n) over(orderby n in desc range 1, null))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 59"() {
        assertGinqScript '''
// tag::ginq_winfunction_30[]
            assert [[1, 2, 3], [2, 2, 3], [5, 4, 13], [5, 4, 13]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range null, 1)), 
                          (sum(n) over(orderby n range null, 1))
            }.toList()
// end::ginq_winfunction_30[]
        '''
    }

    @Test
    void "testGinq - window - 60"() {
        assertGinqScript '''
            assert [[1, 2, 3], [2, 2, 3], [5, 4, 13], [5, 4, 13]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range null, 1)), 
                          (sum(n) over(orderby n in desc range null, 1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 61"() {
        assertGinqScript '''
            assert [[1, 4, 13], [2, 4, 13], [5, 4, 13], [5, 4, 13]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range null, null)), 
                          (sum(n) over(orderby n range null, null))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 62"() {
        assertGinqScript '''
            assert [[1, 4, 13], [2, 4, 13], [5, 4, 13], [5, 4, 13]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range null, null)), 
                          (sum(n) over(orderby n in desc range null, null))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 63"() {
        assertGinqScript '''
// tag::ginq_winfunction_28[]
            assert [[1, 2, 3], [2, 1, 2], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range 0, 1)),
                          (sum(n) over(orderby n range 0, 1))
            }.toList()
// end::ginq_winfunction_28[]
        '''
    }

    @Test
    void "testGinq - window - 64"() {
        assertGinqScript '''
            assert [[1, 2, 3], [2, 1, 2], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range 0, 1)),
                          (sum(n) over(orderby n in desc range 0, 1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 65"() {
        assertGinqScript '''
// tag::ginq_winfunction_33[]
            assert [[1, 2, 3], [2, 2, 3], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n range -1, 1)), 
                          (sum(n) over(orderby n range -1, 1))
            }.toList()
// end::ginq_winfunction_33[]
        '''
    }

    @Test
    void "testGinq - window - 66"() {
        assertGinqScript '''
            assert [[1, 2, 3], [2, 2, 3], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 2, 5, 5]
                select n, (count() over(orderby n in desc range -1, 1)), 
                          (sum(n) over(orderby n in desc range -1, 1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 67"() {
        assertGinqScript '''
            assert [[1, 3, 4], [1, 3, 4], [2, 3, 4], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 1, 2, 5, 5]
                select n, (count() over(orderby n range -1, 1)), 
                          (sum(n) over(orderby n range -1, 1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 68"() {
        assertGinqScript '''
            assert [[1, 3, 4], [1, 3, 4], [2, 3, 4], [5, 2, 10], [5, 2, 10]] == GQ {
                from n in [1, 1, 2, 5, 5]
                select n, (count() over(orderby n in desc range -1, 1)), 
                          (sum(n) over(orderby n in desc range -1, 1))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 69"() {
        assertGinqScript '''
            assert [[1, null, 0], [2, 1, 1], [null, 3, 3], [3, 2, 2]] == GQ {
                from n in [1, 2, null, 3]
                select n, (lag(n) over(orderby n in asc(nullslast))),
                          (rowNumber() over(orderby n in asc(nullslast)))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 70"() {
        assertGinqScript '''
            assert [[1, null, 1], [2, 1, 2], [null, null, 0], [3, 2, 3]] == GQ {
                from n in [1, 2, null, 3]
                select n, (lag(n) over(orderby n in asc(nullsfirst))),
                          (rowNumber() over(orderby n in asc(nullsfirst)))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 71"() {
        assertGinqScript '''
            assert [[1, 2, 2], [2, 3, 1], [null, 1, 3], [3, null, 0]] == GQ {
                from n in [1, 2, null, 3]
                select n, (lag(n) over(orderby n in desc(nullslast))),
                          (rowNumber() over(orderby n in desc(nullslast)))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 72"() {
        assertGinqScript '''
            assert [[1, 2, 3], [2, 3, 2], [null, null, 0], [3, null, 1]] == GQ {
                from n in [1, 2, null, 3]
                select n, (lag(n) over(orderby n in desc(nullsfirst))),
                          (rowNumber() over(orderby n in desc(nullsfirst)))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 73"() {
        assertGinqScript '''
// tag::ginq_winfunction_34[]
            assert [[1, 0, 1, 2, 3], [2, 1, 2, 1, 2], [null, 3, 0, 3, 0], [3, 2, 3, 0, 1]] == GQ {
                from n in [1, 2, null, 3]
                select n, (rowNumber() over(orderby n in asc(nullslast))),
                          (rowNumber() over(orderby n in asc(nullsfirst))),
                          (rowNumber() over(orderby n in desc(nullslast))),
                          (rowNumber() over(orderby n in desc(nullsfirst)))
            }.toList()
// end::ginq_winfunction_34[]
        '''
    }

    @Test
    void "testGinq - window - 74"() {
        assertGinqScript '''
            assert [[0, 0], [1, 1], [2, 2], [3, 3], [4, 4],
                    [5, 5], [6, 6], [7, 7], [8, 8], [9, 9]] == GQ(parallel:true) {
                from n in (
                    from m in 0..<10
                    select m
                )
                select n, (rowNumber() over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 75"() {
        assertGinqScript '''
            assert [[0, 0], [1, 1], [2, 2], [3, 3], [4, 4],
                    [5, 5], [6, 6], [7, 7], [8, 8], [9, 9]] == GQ {
                from n in (
                    from m in 0..<10
                    select m
                )
                select n, (rowNumber() over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 76"() {
        assertGinqScript '''
            assert [[0, 0], [1, 1], [2, 2], [3, 3], [4, 4],
                    [5, 5], [6, 6], [7, 7], [8, 8], [9, 9]] == GQ(parallel:true) {
                from v in (
                    from n in (
                        from m in 0..<10
                        select m
                    )
                    select n, (rowNumber() over(orderby n)) as rn
                )
                select v.n, v.rn
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 77"() {
        assertGinqScript '''
            assert [[0, 0], [1, 1], [2, 2], [3, 3], [4, 4],
                    [5, 5], [6, 6], [7, 7], [8, 8], [9, 9]] == GQ {
                from v in (
                    from n in (
                        from m in 0..<10
                        select m
                    )
                    select n, (rowNumber() over(orderby n)) as rn
                )
                select v.n, v.rn
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 78"() {
        assertGinqScript '''
// tag::ginq_winfunction_35[]
            assert [[1, 0.816496580927726], 
                    [2, 0.816496580927726], 
                    [3, 0.816496580927726]] == GQ {
                from n in [1, 2, 3]
                select n, (stdev(n) over())
            }.toList()
// end::ginq_winfunction_35[]
        '''
    }

    @Test
    void "testGinq - window - 79"() {
        assertGinqScript '''
// tag::ginq_winfunction_36[]
            assert [[1, 1], [2, 1], [3, 1]] == GQ {
                from n in [1, 2, 3]
                select n, (stdevp(n) over())
            }.toList()
// end::ginq_winfunction_36[]
        '''
    }

    @Test
    void "testGinq - window - 80"() {
        assertGinqScript '''
// tag::ginq_winfunction_37[]
            assert [[1, 0.6666666666666667], 
                    [2, 0.6666666666666667], 
                    [3, 0.6666666666666667]] == GQ {
                from n in [1, 2, 3]
                select n, (var(n) over())
            }.toList()
// end::ginq_winfunction_37[]
        '''
    }

    @Test
    void "testGinq - window - 81"() {
        assertGinqScript '''
// tag::ginq_winfunction_38[]
            assert [[1, 1], [2, 1], [3, 1]] == GQ {
                from n in [1, 2, 3]
                select n, (varp(n) over())
            }.toList()
// end::ginq_winfunction_38[]
        '''
    }

    @Test
    void "testGinq - window - 82"() {
        assertGinqScript '''
// tag::ginq_winfunction_39[]
            assert [[1, 0], [2, 0], [3, 0],
                    [4, 1], [5, 1], 
                    [6, 2], [7, 2],[8, 2], 
                    [9, 3], [10, 3]] == GQ {
                from n in 1..10
                select n, (ntile(4) over(orderby n))
            }.toList()
// end::ginq_winfunction_39[]
        '''
    }

    @Test
    void "testGinq - window - 83"() {
        assertGinqScript '''
// tag::ginq_winfunction_40[]
            assert [[1, 1, 2, 3, null], [2, 1, 2, 3, null], [3, 1, 2, 3, null]] == GQ {
                from n in 1..3
                select n, (nthValue(n, 0) over(orderby n)),
                          (nthValue(n, 1) over(orderby n)),
                          (nthValue(n, 2) over(orderby n)),
                          (nthValue(n, 3) over(orderby n))
            }.toList()
// end::ginq_winfunction_40[]
        '''
    }

    @Test
    void "testGinq - window - 84"() {
        assertGinqScript '''
            assert [[3, 1, 1], [3, 1, 1], [2, 3, 2], [2, 3, 2], [1, 5, 3]] == GQ {
                from n in [3, 3, 2, 2, 1]
                select n, 
                    (rank() over(orderby n in desc)),
                    (denseRank() over(orderby n in desc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 85"() {
        assertGinqScript '''
// tag::ginq_winfunction_41[]
            assert [[60, 0, 0.4], [60, 0, 0.4], [80, 0.5, 0.8], [80, 0.5, 0.8], [100, 1, 1]] == GQ {
                from n in [60, 60, 80, 80, 100]
                select n,
                    (percentRank() over(orderby n)),
                    (cumeDist() over(orderby n))
            }.toList()
// end::ginq_winfunction_41[]
        '''
    }

    @Test
    void "testGinq - window - 86"() {
        assertGinqScript '''
            assert [[100, 0, 0.2], [80, 0.25, 0.6], [80, 0.25, 0.6], [60, 0.75, 1], [60, 0.75, 1]] == GQ {
                from n in [100, 80, 80, 60, 60]
                select n,
                    (percentRank() over(orderby n in desc)),
                    (cumeDist() over(orderby n in desc))
            }.toList()
        '''
    }

    @Test
    void "testGinq - window - 87"() {
        assertGinqScript '''
            assert [[1, 1, 1, 0, 0.5], [null, 2, 2, 1, 1]] == GQ {
                from n in [1, null]
                select n,
                    (rank() over(orderby n)),
                    (denseRank() over(orderby n)),
                    (percentRank() over(orderby n)),
                    (cumeDist() over(orderby n))
            }.toList()
        '''
    }

    @Test
    void "testGinq - switch - 1"() {
        assertGinqScript '''
// tag::ginq_tips_13[]
            assert ['a', 'b', 'c', 'c'] == GQ {
                from n in [1, 2, 3, 4]
                select switch (n) {
                    case 1 -> 'a'
                    case 2 -> 'b'
                    default -> 'c'
                }
            }.toList()
// end::ginq_tips_13[]
        '''
    }

    @Test
    void "testGinq - shutdown - 0"() {
        assertScript '''
            import org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper
// tag::ginq_tips_09[]
            GQ {
                shutdown
            }
// end::ginq_tips_09[]
            assert QueryableHelper.ThreadPoolHolder.THREAD_POOL.isShutdown() && QueryableHelper.ThreadPoolHolder.THREAD_POOL.isTerminated()
        '''
    }

    @Test
    void "testGinq - shutdown - 1"() {
        assertScript '''
            import org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper
// tag::ginq_tips_10[]
            GQ {
                shutdown immediate
            }
// end::ginq_tips_10[]
            assert QueryableHelper.ThreadPoolHolder.THREAD_POOL.isShutdown() && QueryableHelper.ThreadPoolHolder.THREAD_POOL.isTerminated()
        '''
    }

    @Test
    void "testGinq - shutdown - 2"() {
        assertScript '''
            import org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper
// tag::ginq_tips_11[]
            GQ {
                shutdown abort
            }
// end::ginq_tips_11[]
            assert QueryableHelper.ThreadPoolHolder.THREAD_POOL.isShutdown()
        '''
    }

    private static void assertGinqScript(String script) {
        String deoptimizedScript = script.replaceAll(/\bGQ\s*[{]/, 'GQ(optimize:false) {')
        List<String> scriptList = [deoptimizedScript, script]

        if (-1 != deoptimizedScript.indexOf('innerjoin') || -1 != deoptimizedScript.indexOf('innerhashjoin')) {
            String smartJoinScript = deoptimizedScript.replaceAll('innerjoin|innerhashjoin', 'join')
            scriptList << smartJoinScript
        }

        scriptList.each { String c ->
            assertScript(c)
        }
    }
}
