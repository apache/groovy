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
package gdk

import groovy.test.GroovyTestCase

class WorkingWithCollectionsTest extends GroovyTestCase {
    void testListLiterals() {
        // tag::list_literals[]
        def list = [5, 6, 7, 8]
        assert list.get(2) == 7
        assert list[2] == 7
        assert list instanceof java.util.List

        def emptyList = []
        assert emptyList.size() == 0
        emptyList.add(5)
        assert emptyList.size() == 1
        // end::list_literals[]
    }

    void testList() {
        // tag::list_usecases[]
        def list = [5, 6, 7, 8]
        assert list.size() == 4
        assert list.getClass() == ArrayList     // the specific kind of list being used

        assert list[2] == 7                     // indexing starts at 0
        assert list.getAt(2) == 7               // equivalent method to subscript operator []
        assert list.get(2) == 7                 // alternative method

        list[2] = 9
        assert list == [5, 6, 9, 8,]           // trailing comma OK

        list.putAt(2, 10)                       // equivalent method to [] when value being changed
        assert list == [5, 6, 10, 8]
        assert list.set(2, 11) == 10            // alternative method that returns old value
        assert list == [5, 6, 11, 8]

        assert ['a', 1, 'a', 'a', 2.5, 2.5f, 2.5d, 'hello', 7g, null, 9 as byte]
        //objects can be of different types; duplicates allowed

        assert [1, 2, 3, 4, 5][-1] == 5             // use negative indices to count from the end
        assert [1, 2, 3, 4, 5][-2] == 4
        assert [1, 2, 3, 4, 5].getAt(-2) == 4       // getAt() available with negative index...
        try {
            [1, 2, 3, 4, 5].get(-2)                 // but negative index not allowed with get()
            assert false
        } catch (e) {
            assert e instanceof IndexOutOfBoundsException
        }
        // end::list_usecases[]
    }

    void testListsAsBoolean() {
        // tag::list_to_bool[]
        assert ![]             // an empty list evaluates as false

        //all other lists, irrespective of contents, evaluate as true
        assert [1] && ['a'] && [0] && [0.0] && [false] && [null]
        // end::list_to_bool[]
    }

    void testLeftShiftOnList() {
        // tag::list_leftshift[]
        def list = []
        assert list.empty

        list << 5
        assert list.size() == 1

        list << 7 << 'i' << 11
        assert list == [5, 7, 'i', 11]

        list << ['m', 'o']
        assert list == [5, 7, 'i', 11, ['m', 'o']]

        //first item in chain of << is target list
        assert ([1, 2] << 3 << [4, 5] << 6) == [1, 2, 3, [4, 5], 6]

        //using leftShift is equivalent to using <<
        assert ([1, 2, 3] << 4) == ([1, 2, 3].leftShift(4))

        // end::list_leftshift[]
    }

    void testListAdd() {
        // tag::list_add[]
        assert [1, 2] + 3 + [4, 5] + 6 == [1, 2, 3, 4, 5, 6]
        // equivalent to calling the `plus` method
        assert [1, 2].plus(3).plus([4, 5]).plus(6) == [1, 2, 3, 4, 5, 6]

        def a = [1, 2, 3]
        a += 4      // creates a new list and assigns it to `a`
        a += [5, 6]
        assert a == [1, 2, 3, 4, 5, 6]

        assert [1, *[222, 333], 456] == [1, 222, 333, 456]
        assert [*[1, 2, 3]] == [1, 2, 3]
        assert [1, [2, 3, [4, 5], 6], 7, [8, 9]].flatten() == [1, 2, 3, 4, 5, 6, 7, 8, 9]

        def list = [1, 2]
        list.add(3)
        list.addAll([5, 4])
        assert list == [1, 2, 3, 5, 4]

        list = [1, 2]
        list.add(1, 3) // add 3 just before index 1
        assert list == [1, 3, 2]

        list.addAll(2, [5, 4]) //add [5,4] just before index 2
        assert list == [1, 3, 5, 4, 2]

        list = ['a', 'b', 'z', 'e', 'u', 'v', 'g']
        list[8] = 'x' // the [] operator is growing the list as needed
        // nulls inserted if required
        assert list == ['a', 'b', 'z', 'e', 'u', 'v', 'g', null, 'x']
        // end::list_add[]
    }

    void testListIteration() {
        // tag::list_each[]
        [1, 2, 3].each {
            println "Item: $it" // `it` is an implicit parameter corresponding to the current element
        }
        ['a', 'b', 'c'].eachWithIndex { it, i -> // `it` is the current element, while `i` is the index
            println "$i: $it"
        }
        // end::list_each[]
    }

    void testListConstruct() {
        // tag::list_construct[]
        def list1 = ['a', 'b', 'c']
        //construct a new list, seeded with the same items as in list1
        def list2 = new ArrayList<String>(list1)

        assert list2 == list1 // == checks that each corresponding element is the same

        // clone() can also be called
        def list3 = list1.clone()
        assert list3 == list1
        // end::list_construct[]
    }

    void testListCollect() {
        // tag::list_collect[]
        assert [1, 2, 3].collect { it * 2 } == [2, 4, 6]

        // shortcut syntax instead of collect
        assert [1, 2, 3]*.multiply(2) == [1, 2, 3].collect { it.multiply(2) }

        def list = [0]
        // it is possible to give `collect` the list which collects the elements
        assert [1, 2, 3].collect(list) { it * 2 } == [0, 2, 4, 6]
        assert list == [0, 2, 4, 6]
        // end::list_collect[]
    }

    void testListGDK() {
        // tag::list_gdk1[]
        assert [1, 2, 3].find { it > 1 } == 2           // find 1st element matching criteria
        assert [1, 2, 3].findAll { it > 1 } == [2, 3]   // find all elements matching critieria
        assert ['a', 'b', 'c', 'd', 'e'].findIndexOf {      // find index of 1st element matching criteria
            it in ['c', 'e', 'g']
        } == 2

        assert ['a', 'b', 'c', 'd', 'c'].indexOf('c') == 2  // index returned
        assert ['a', 'b', 'c', 'd', 'c'].indexOf('z') == -1 // index -1 means value not in list
        assert ['a', 'b', 'c', 'd', 'c'].lastIndexOf('c') == 4

        assert [1, 2, 3].every { it < 5 }               // returns true if all elements match the predicate
        assert ![1, 2, 3].every { it < 3 }
        assert [1, 2, 3].any { it > 2 }                 // returns true if any element matches the predicate
        assert ![1, 2, 3].any { it > 3 }

        assert [1, 2, 3, 4, 5, 6].sum() == 21                // sum anything with a plus() method
        assert ['a', 'b', 'c', 'd', 'e'].sum {
            it == 'a' ? 1 : it == 'b' ? 2 : it == 'c' ? 3 : it == 'd' ? 4 : it == 'e' ? 5 : 0
            // custom value to use in sum
        } == 15
        assert ['a', 'b', 'c', 'd', 'e'].sum { ((char) it) - ((char) 'a') } == 10
        assert ['a', 'b', 'c', 'd', 'e'].sum() == 'abcde'
        assert [['a', 'b'], ['c', 'd']].sum() == ['a', 'b', 'c', 'd']

        // an initial value can be provided
        assert [].sum(1000) == 1000
        assert [1, 2, 3].sum(1000) == 1006

        assert [1, 2, 3].join('-') == '1-2-3'           // String joining
        assert [1, 2, 3].inject('counting: ') {
            str, item -> str + item                     // reduce operation
        } == 'counting: 123'
        assert [1, 2, 3].inject(0) { count, item ->
            count + item
        } == 6
        // end::list_gdk1[]

        // tag::list_gdk2[]
        def list = [9, 4, 2, 10, 5]
        assert list.max() == 10
        assert list.min() == 2

        // we can also compare single characters, as anything comparable
        assert ['x', 'y', 'a', 'z'].min() == 'a'

        // we can use a closure to specify the sorting behaviour
        def list2 = ['abc', 'z', 'xyzuvw', 'Hello', '321']
        assert list2.max { it.size() } == 'xyzuvw'
        assert list2.min { it.size() } == 'z'
        // end::list_gdk2[]

        assertScript '''
            // tag::list_gdk3[]
            assert ['a','b','c','b','b'] - 'c' == ['a','b','b','b']
            assert ['a','b','c','b','b'] - 'b' == ['a','c']
            assert ['a','b','c','b','b'] - ['b','c'] == ['a']

            def list = [1,2,3,4,3,2,1]
            list -= 3           // creates a new list by removing `3` from the original one
            assert list == [1,2,4,2,1]
            assert ( list -= [2,4] ) == [1,1]
            // end::list_gdk3[]
        '''

        assertScript '''
            // tag::list_gdk_remove_index[]
            def list = ['a','b','c','d','e','f','b','b','a']
            assert list.remove(2) == 'c'        // remove the third element, and return it
            assert list == ['a','b','d','e','f','b','b','a']
            // end::list_gdk_remove_index[]
        '''
        
        assertScript '''
            // tag::list_gdk4[]
            def list = [1,2,3,4,5,6,2,2,1]

            assert list.remove(2) == 3          // this removes the element at index 2, and returns it
            assert list == [1,2,4,5,6,2,2,1]

            assert list.removeElement(2)        // remove first 2 and return true
            assert list == [1,4,5,6,2,2,1]

            assert ! list.removeElement(8)      // return false because 8 is not in the list
            assert list == [1,4,5,6,2,2,1]

            assert list.removeAt(1) == 4        // remove element at index 1, and return it
            assert list == [1,5,6,2,2,1]
            // end::list_gdk4[]
        '''

        assertScript '''
            // tag::list_gdk5[]
            def list= ['a','b','c','b','b']
            assert list.remove('c')             // remove 'c', and return true because element removed
            assert list.remove('b')             // remove first 'b', and return true because element removed

            assert ! list.remove('z')           // return false because no elements removed
            assert list == ['a','b','b']
            // end::list_gdk5[]
        '''

        assertScript '''
            // tag::list_gdk6[]
            def list= ['a',2,'c',4]
            list.clear()
            assert list == []
            // end::list_gdk6[]
        '''

        assertScript '''
            // tag::list_gdk7[]
            assert 'a' in ['a','b','c']             // returns true if an element belongs to the list
            assert ['a','b','c'].contains('a')      // equivalent to the `contains` method in Java
            assert [1,3,4].containsAll([1,4])       // `containsAll` will check that all elements are found

            assert [1,2,3,3,3,3,4,5].count(3) == 4  // count the number of elements which have some value
            assert [1,2,3,3,3,3,4,5].count {
                it%2==0                             // count the number of elements which match the predicate
            } == 2

            assert [1,2,4,6,8,10,12].intersect([1,3,6,9,12]) == [1,6,12]

            assert [1,2,3].disjoint( [4,6,9] )
            assert ![1,2,3].disjoint( [2,4,6] )
            // end::list_gdk7[]
        '''

    }

    void testListComparator() {
        // tag::list_comparator[]
        Comparator mc = { a, b -> a == b ? 0 : (a < b ? -1 : 1) }

        def list = [7, 4, 9, -6, -1, 11, 2, 3, -9, 5, -13]
        assert list.max(mc) == 11
        assert list.min(mc) == -13

        Comparator mc2 = { a, b -> a == b ? 0 : (Math.abs(a) < Math.abs(b)) ? -1 : 1 }


        assert list.max(mc2) == -13
        assert list.min(mc2) == -1

        assert list.max { a, b -> a.equals(b) ? 0 : Math.abs(a) < Math.abs(b) ? -1 : 1 } == -13
        assert list.min { a, b -> a.equals(b) ? 0 : Math.abs(a) < Math.abs(b) ? -1 : 1 } == -1
        // end::list_comparator[]
    }

    void testListSort() {
        // tag::list_sort[]
        assert [6, 3, 9, 2, 7, 1, 5].sort() == [1, 2, 3, 5, 6, 7, 9]

        def list = ['abc', 'z', 'xyzuvw', 'Hello', '321']
        assert list.sort {
            it.size()
        } == ['z', 'abc', '321', 'Hello', 'xyzuvw']

        def list2 = [7, 4, -6, -1, 11, 2, 3, -9, 5, -13]
        assert list2.sort { a, b -> a == b ? 0 : Math.abs(a) < Math.abs(b) ? -1 : 1 } ==
                [-1, 2, 3, 4, 5, -6, 7, -9, 11, -13]

        Comparator mc = { a, b -> a == b ? 0 : Math.abs(a) < Math.abs(b) ? -1 : 1 }

        // JDK 8+ only
        // list2.sort(mc)
        // assert list2 == [-1, 2, 3, 4, 5, -6, 7, -9, 11, -13]

        def list3 = [6, -3, 9, 2, -7, 1, 5]

        Collections.sort(list3)
        assert list3 == [-7, -3, 1, 2, 5, 6, 9]

        Collections.sort(list3, mc)
        assert list3 == [1, 2, -3, 5, 6, -7, 9]
        // end::list_sort[]
    }

    void testListMultiply() {
        // tag::list_multiply[]
        assert [1, 2, 3] * 3 == [1, 2, 3, 1, 2, 3, 1, 2, 3]
        assert [1, 2, 3].multiply(2) == [1, 2, 3, 1, 2, 3]
        assert Collections.nCopies(3, 'b') == ['b', 'b', 'b']

        // nCopies from the JDK has different semantics than multiply for lists
        assert Collections.nCopies(2, [1, 2]) == [[1, 2], [1, 2]] //not [1,2,1,2]
        // end::list_multiply[]
    }

    void testIntRange() {
        // tag::intrange[]
        // an inclusive range
        def range = 5..8
        assert range.size() == 4
        assert range.get(2) == 7
        assert range[2] == 7
        assert range instanceof java.util.List
        assert range.contains(5)
        assert range.contains(8)

        // lets use a half-open range
        range = 5..<8
        assert range.size() == 3
        assert range.get(2) == 7
        assert range[2] == 7
        assert range instanceof java.util.List
        assert range.contains(5)
        assert !range.contains(8)

        //get the end points of the range without using indexes
        range = 1..10
        assert range.from == 1
        assert range.to == 10
        // end::intrange[]
    }

    void testStringRange() {
        // tag::stringrange[]
        // an inclusive range
        def range = 'a'..'d'
        assert range.size() == 4
        assert range.get(2) == 'c'
        assert range[2] == 'c'
        assert range instanceof java.util.List
        assert range.contains('a')
        assert range.contains('d')
        assert !range.contains('e')
        // end::stringrange[]
    }

    void testRangeIteration() {
        // tag::range_for[]
        for (i in 1..10) {
            println "Hello ${i}"
        }
        // end::range_for[]
        // tag::range_each[]
        (1..10).each { i ->
            println "Hello ${i}"
        }
        // end::range_each[]
    }

    void testRangeInSwitch() {
        int years = 1
        double interestRate
        // tag::range_switch[]
        switch (years) {
            case 1..10: interestRate = 0.076; break;
            case 11..25: interestRate = 0.052; break;
            default: interestRate = 0.037;
        }
        // end::range_switch[]
        assert interestRate == 0.076d

    }

    void testMapLiteral() {
        // tag::map_literal[]
        def map = [name: 'Gromit', likes: 'cheese', id: 1234]
        assert map.get('name') == 'Gromit'
        assert map.get('id') == 1234
        assert map['name'] == 'Gromit'
        assert map['id'] == 1234
        assert map instanceof java.util.Map

        def emptyMap = [:]
        assert emptyMap.size() == 0
        emptyMap.put("foo", 5)
        assert emptyMap.size() == 1
        assert emptyMap.get("foo") == 5
        // end::map_literal[]
    }

    void testMapLiteralUsingEscape() {
        // tag::map_literal_gotcha[]
        def a = 'Bob'
        def ages = [a: 43]
        assert ages['Bob'] == null // `Bob` is not found
        assert ages['a'] == 43     // because `a` is a literal!

        ages = [(a): 43]            // now we escape `a` by using parenthesis
        assert ages['Bob'] == 43   // and the value is found!
        // end::map_literal_gotcha[]
    }

    void testMapPropertyNotation() {
        // tag::map_property[]
        def map = [name: 'Gromit', likes: 'cheese', id: 1234]
        assert map.name == 'Gromit'     // can be used instead of map.get('name')
        assert map.id == 1234

        def emptyMap = [:]
        assert emptyMap.size() == 0
        emptyMap.foo = 5
        assert emptyMap.size() == 1
        assert emptyMap.foo == 5
        // end::map_property[]
    }

    void testMapPropertyGotcha() {
        // tag::map_property_gotcha[]
        def map = [name: 'Gromit', likes: 'cheese', id: 1234]
        assert map.class == null
        assert map.get('class') == null
        assert map.getClass() == LinkedHashMap // this is probably what you want

        map = [1      : 'a',
               (true) : 'p',
               (false): 'q',
               (null) : 'x',
               'null' : 'z']
        assert map.containsKey(1) // 1 is not an identifier so used as is
        assert map.true == null
        assert map.false == null
        assert map.get(true) == 'p'
        assert map.get(false) == 'q'
        assert map.null == 'z'
        assert map.get(null) == 'x'
        // end::map_property_gotcha[]
    }

    void testMapIteration() {
        // tag::map_iteration[]
        def map = [
                Bob  : 42,
                Alice: 54,
                Max  : 33
        ]

        // `entry` is a map entry
        map.each { entry ->
            println "Name: $entry.key Age: $entry.value"
        }

        // `entry` is a map entry, `i` the index in the map
        map.eachWithIndex { entry, i ->
            println "$i - Name: $entry.key Age: $entry.value"
        }

        // Alternatively you can use key and value directly
        map.each { key, value ->
            println "Name: $key Age: $value"
        }

        // Key, value and i as the index in the map
        map.eachWithIndex { key, value, i ->
            println "$i - Name: $key Age: $value"
        }
        // end::map_iteration[]
    }

    void testAddElementsToMap() {
        // tag::map_add[]
        def defaults = [1: 'a', 2: 'b', 3: 'c', 4: 'd']
        def overrides = [2: 'z', 5: 'x', 13: 'x']

        def result = new LinkedHashMap(defaults)
        result.put(15, 't')
        result[17] = 'u'
        result.putAll(overrides)
        assert result == [1: 'a', 2: 'z', 3: 'c', 4: 'd', 5: 'x', 13: 'x', 15: 't', 17: 'u']
        // end::map_add[]
    }

    void testMapGDK() {
        assertScript '''
            // tag::map_gdk1[]
            def m = [1:'a', 2:'b']
            assert m.get(1) == 'a'
            m.clear()
            assert m == [:]
            // end::map_gdk1[]
        '''

        assertScript '''
            // tag::map_views[]
            def map = [1:'a', 2:'b', 3:'c']

            def entries = map.entrySet()
            entries.each { entry ->
              assert entry.key in [1,2,3]
              assert entry.value in ['a','b','c']
            }

            def keys = map.keySet()
            assert keys == [1,2,3] as Set
            // end::map_views[]
        '''

        assertScript '''
            // tag::map_gdk2[]
            def people = [
                1: [name:'Bob', age: 32, gender: 'M'],
                2: [name:'Johnny', age: 36, gender: 'M'],
                3: [name:'Claire', age: 21, gender: 'F'],
                4: [name:'Amy', age: 54, gender:'F']
            ]

            def bob = people.find { it.value.name == 'Bob' } // find a single entry
            def females = people.findAll { it.value.gender == 'F' }

            // both return entries, but you can use collect to retrieve the ages for example
            def ageOfBob = bob.value.age
            def agesOfFemales = females.collect {
                it.value.age
            }

            assert ageOfBob == 32
            assert agesOfFemales == [21,54]

            // but you could also use a key/pair value as the parameters of the closures
            def agesOfMales = people.findAll { id, person ->
                person.gender == 'M'
            }.collect { id, person ->
                person.age
            }
            assert agesOfMales == [32, 36]

            // `every` returns true if all entries match the predicate
            assert people.every { id, person ->
                person.age > 18
            }

            // `any` returns true if any entry matches the predicate

            assert people.any { id, person ->
                person.age == 54
            }
            // end::map_gdk2[]
        '''

        assertScript '''
            // tag::map_gdk3[]
            assert ['a', 7, 'b', [2, 3]].groupBy {
                it.class
            } == [(String)   : ['a', 'b'],
                  (Integer)  : [7],
                  (ArrayList): [[2, 3]]
            ]

            assert [
                    [name: 'Clark', city: 'London'], [name: 'Sharma', city: 'London'],
                    [name: 'Maradona', city: 'LA'], [name: 'Zhang', city: 'HK'],
                    [name: 'Ali', city: 'HK'], [name: 'Liu', city: 'HK'],
            ].groupBy { it.city } == [
                    London: [[name: 'Clark', city: 'London'],
                             [name: 'Sharma', city: 'London']],
                    LA    : [[name: 'Maradona', city: 'LA']],
                    HK    : [[name: 'Zhang', city: 'HK'],
                             [name: 'Ali', city: 'HK'],
                             [name: 'Liu', city: 'HK']],
            ]
            // end::map_gdk3[]
        '''
    }

    void testShouldNotUseGStringAsKey() {
        assertScript '''
            // tag::gstring_gotcha[]
            def key = 'some key'
            def map = [:]
            def gstringKey = "${key.toUpperCase()}"
            map.put(gstringKey,'value')
            assert map.get('SOME KEY') == null
            // end::gstring_gotcha[]
        '''
    }

    void testMapConstruct() {
        // tag::map_construct[]
        def map = [
                simple : 123,
                complex: [a: 1, b: 2]
        ]
        def map2 = map.clone()
        assert map2.get('simple') == map.get('simple')
        assert map2.get('complex') == map.get('complex')
        map2.get('complex').put('c', 3)
        assert map.get('complex').get('c') == 3
        // end::map_construct[]
    }

    void testGPathSupport() {
        // tag::gpath_support_1[]
        def listOfMaps = [['a': 11, 'b': 12], ['a': 21, 'b': 22]]
        assert listOfMaps.a == [11, 21] //GPath notation
        assert listOfMaps*.a == [11, 21] //spread dot notation

        listOfMaps = [['a': 11, 'b': 12], ['a': 21, 'b': 22], null]
        assert listOfMaps*.a == [11, 21, null] // caters for null values
        assert listOfMaps*.a == listOfMaps.collect { it?.a } //equivalent notation
        // end::gpath_support_1[]

        // tag::gpath_support_2[]
        // But this will only collect non-null values
        assert listOfMaps.a == [11,21]
        // end::gpath_support_2[]

        // tag::gpath_support_3[]
        assert [ 'z': 900,
                 *: ['a': 100, 'b': 200], 'a': 300] == ['a': 300, 'b': 200, 'z': 900]
        //spread map notation in map definition
        assert [*: [3: 3, *: [5: 5]], 7: 7] == [3: 3, 5: 5, 7: 7]

        def f = { [1: 'u', 2: 'v', 3: 'w'] }
        assert [*: f(), 10: 'zz'] == [1: 'u', 10: 'zz', 2: 'v', 3: 'w']
        //spread map notation in function arguments
        f = { map -> map.c }
        assert f(*: ['a': 10, 'b': 20, 'c': 30], 'e': 50) == 30

        f = { m, i, j, k -> [m, i, j, k] }
        //using spread map notation with mixed unnamed and named arguments
        assert f('e': 100, *[4, 5], *: ['a': 10, 'b': 20, 'c': 30], 6) ==
                [["e": 100, "b": 20, "c": 30, "a": 10], 4, 5, 6]
        // end::gpath_support_3[]

    }

    void testStarDot() {
        // tag::stardot_1[]
        assert [1, 3, 5] == ['a', 'few', 'words']*.size()
        // end::stardot_1[]
        assertScript '''
            // tag::stardot_2[]
            class Person {
                String name
                int age
            }
            def persons = [new Person(name:'Hugo', age:17), new Person(name:'Sandra',age:19)]
            assert [17, 19] == persons*.age
            // end::stardot_2[]
        '''
    }

    void testSubscriptOperator() {
        // tag::subscript[]
        def text = 'nice cheese gromit!'
        def x = text[2]

        assert x == 'c'
        assert x.class == String

        def sub = text[5..10]
        assert sub == 'cheese'

        def list = [10, 11, 12, 13]
        def answer = list[2,3]
        assert answer == [12,13]
        // end::subscript[]

        // tag::subscript_2[]
        list = 100..200
        sub = list[1, 3, 20..25, 33]
        assert sub == [101, 103, 120, 121, 122, 123, 124, 125, 133]
        // end::subscript_2[]

        // tag::subscript_3[]
        list = ['a','x','x','d']
        list[1..2] = ['b','c']
        assert list == ['a','b','c','d']
        // end::subscript_3[]

        // tag::subscript_4[]
        text = "nice cheese gromit!"
        x = text[-1]
        assert x == "!"
        // end::subscript_4[]

        // tag::subscript_4a[]
        def name = text[-7..-2]
        assert name == "gromit"
        // end::subscript_4a[]

        // tag::subscript_5[]
        text = "nice cheese gromit!"
        name = text[3..1]
        assert name == "eci"
        // end::subscript_5[]
    }
}
