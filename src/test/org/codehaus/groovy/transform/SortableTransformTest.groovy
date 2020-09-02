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
package org.codehaus.groovy.transform

import gls.CompilableTestSupport
import groovy.transform.Sortable

class SortableTransformTest extends CompilableTestSupport {
    void testSortableWithCustomOrdering() {
        assertScript '''
            import groovy.transform.Sortable
            def people = [
              [first: 'Bart', last: 'Simpson', bestFriend: 'Milhouse'],
              [first: 'Lisa', last: 'Simpson', bestFriend: 'Janey']
            ]

            @Sortable(includes='last,first') class PersonLF { String first, last, bestFriend }
            def peopleLF = people.collect{ new PersonLF(*:it) }
            assert peopleLF.sort()*.first == ['Bart', 'Lisa']

            @Sortable(includes='last,bestFriend') class PersonLB { String first, last, bestFriend }
            def peopleLB = people.collect{ new PersonLB(*:it) }
            assert peopleLB.sort()*.first == ['Lisa', 'Bart']
        '''
    }

    void testSortableWithCompileStatic() {
        assertScript '''
            import groovy.transform.*

            @CompileStatic
            @Canonical
            @Sortable(includes = ['completed'])
            class DeliveryBucket {
                Date completed
                Long count
            }

            def buckets = [
                new DeliveryBucket(new Date()+1, 111),
                new DeliveryBucket(new Date(), 222)
            ]

            assert buckets*.count == [111, 222]
            assert buckets.sort()*.count == [222, 111]
            assert buckets.sort(false, DeliveryBucket.comparatorByCompleted())*.count == [222, 111]
        '''
    }

    void testDuckTypingWithComparators() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            @Canonical
            @Sortable(includes = 'quackVolume')
            class Duck {
                Integer quackVolume
            }

            @Canonical
            class DuckWhistle {
                Integer quackVolume
            }

            def quacks = [
                    new Duck(4),
                    new Duck(2),
                    new DuckWhistle(3),
            ]

            assert quacks*.class == [Duck, Duck, DuckWhistle]
            // how to do duck typing
            assert quacks.sort{ it.quackVolume }*.class == [Duck, DuckWhistle, Duck]
            // illustrating that the @Sortable generated Comparator doesn't support such duck typing
            shouldFail(ClassCastException) {
                assert quacks.sort(false, Duck.comparatorByQuackVolume())*.class == [Duck, DuckWhistle, Duck]
            }
        '''
    }

    void testSortableWithSortableProperty() {
        assertScript '''
            import groovy.transform.*
            @Canonical @Sortable(includes=['first', 'favoriteEvent']) class Person {
              String first
              String last
              Event favoriteEvent
            }
            @Immutable @Sortable class Event {
              String description
              Date when
            }
            def now = new Date()
            def people = [
              new Person('Groovy', 'Hacker1', new Event('discover @Sortable', now)),
              new Person('Groovy', 'Hacker2', new Event('discover @Sortable', now+1)),
              new Person('Groovy', 'Hacker3', new Event('discover @Sortable', now-1))
            ]
            assert people.sort()*.last == ['Hacker3', 'Hacker1', 'Hacker2']
        '''
    }

    void testBadIncludesAndExcludes() {
        def message = shouldFail '''
            @groovy.transform.Sortable(includes='first', excludes='last') class Person {
              String first
              String last
              Integer born
            }
        '''
        assert message.contains("Error during @Sortable processing: Only one of 'includes' and 'excludes' should be supplied not both")
    }

    void testBadInclude() {
        def message = shouldFail '''
            @groovy.transform.Sortable(includes='first,middle') class Person {
              String first
              String last
              Integer born
            }
        '''
        assert message.contains("Error during @Sortable processing: 'includes' property 'middle' does not exist.")
    }

    void testBadExclude() {
        def message = shouldFail '''
            @groovy.transform.Sortable(excludes='first,middle') class Person {
              String first
              String last
              Integer born
            }
        '''
        assert message.contains("Error during @Sortable processing: 'excludes' property 'middle' does not exist.")
    }

    void testBadPropertyType() {
        def message = shouldFail '''
            @groovy.transform.Sortable class Person {
              String first
              String last
              List<String> appearances
            }
        '''
        assert message.contains("Error during @Sortable processing: property 'appearances' must be Comparable")
    }

    void testBadInterfaceUsage() {
        def message = shouldFail '''
            @groovy.transform.Sortable interface Foo { }
        '''
        assert message.contains("@Sortable cannot be applied to interface Foo")
    }

    void testReverseSorting() {
        assertScript '''
            import groovy.transform.*

            @CompileStatic
            @Canonical
            @Sortable(includes = ['age'], reversed = true)
            class Person {
                String name
                int age
            }

            def persons = [
                new Person('PJ', 25),
                new Person('Guillaume', 40)
            ]

            assert persons*.age == [25, 40]
            assert persons.sort()*.age == [40, 25]
            assert persons.sort(false, Person.comparatorByAge())*.age == [40, 25]
        '''
    }

    // GROOVY-9711
    @Sortable
    class SortableBase {
        Integer num
    }

    @Sortable
    class SortableChild1 extends SortableBase {
        String str
    }

    @Sortable(includeSuperProperties = true, includes = ['str'])
    class SortableChild2 extends SortableBase {
        String str
    }

    @Sortable(includeSuperProperties = true, includes = ['str','num'])
    class SortableChild3 extends SortableBase {
        String str
    }

    @Sortable(includeSuperProperties = true, includes = ['num','str'])
    class SortableChild4 extends SortableBase {
        String str
    }

    void testSortableExtendingSortable1() {
        List<SortableChild1> unsortedList = [new SortableChild1(str: 'B', num: 1), new SortableChild1(str: 'A', num: 2)]
        List<SortableChild1> sortedList = unsortedList.toSorted()
        assert sortedList[0].str == 'A'
    }

    void testSortableExtendingSortable2() {
        List<SortableChild2> unsortedList = [new SortableChild2(str: 'B', num: 1), new SortableChild2(str: 'A', num: 2)]
        List<SortableChild2> sortedList = unsortedList.toSorted()
        assert sortedList[0].str == 'A'
    }

    void testSortableExtendingSortable3() {
        List<SortableChild3> unsortedList = [new SortableChild3(str: 'B', num: 1), new SortableChild3(str: 'A', num: 2)]
        List<SortableChild3> sortedList = unsortedList.toSorted()
        assert sortedList[0].str == 'A'
    }

    void testSortableExtendingSortable4() {
        List<SortableChild4> unsortedList = [new SortableChild4(str: 'B', num: 1), new SortableChild4(str: 'A', num: 2)]
        List<SortableChild4> sortedList = unsortedList.toSorted()
        assert sortedList[0].str == 'B'
    }
}
