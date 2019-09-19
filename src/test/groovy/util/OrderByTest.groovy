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
package groovy.util

import groovy.test.GroovyTestCase
import org.codehaus.groovy.runtime.DefaultGroovyMethods

/** 
 * Tests OrderBy
 */
class OrderByTest extends GroovyTestCase {

    void testSortByOneField() {
        def people = buildPeople()

        def order = new OrderBy({it.get('@cheese')})
        def sorted = DefaultGroovyMethods.sort(people,order)
        assert (0..3).collect{ sorted.get(it).get('@name') } == ['Joe', 'Bob', 'James', 'Chris']

        order = new OrderBy({it.get('@name')})
        sorted = DefaultGroovyMethods.sort(people,order)
        assert (0..3).collect{ sorted.get(it).get('@name') } == ['Bob', 'Chris', 'James', 'Joe']
    }

    void testSortByMultipleFields() {
        def people = buildPeople()
        def order = new OrderBy([{it.get('@location')}, {it.get('@cheese')}])
        def sorted = DefaultGroovyMethods.sort(people,order)
        assert (0..3).collect{ sorted.get(it).get('@name') } == ['Bob', 'Joe', 'James', 'Chris']
    }

    private buildPeople() {
        def builder = new NodeBuilder()
        def tree = builder.people {
            person(name: 'James', cheese: 'Edam', location: 'London')
            person(name: 'Bob', cheese: 'Cheddar', location: 'Atlanta')
            person(name: 'Chris', cheese: 'Red Leicester', location: 'London')
            person(name: 'Joe', cheese: 'Brie', location: 'London')
        }
        tree.children()
    }

    void testSortByMultipleFieldsWithComparable() {
        def bob = new TestPerson(first: 'Bob', last: 'Barker', age: '10')
        def bobby = new TestPerson(first: 'Bob', last: 'Barker', age: '20')
        def raul = new TestPerson(first: 'Raul', last: 'Julia', age: '30')

        def people = [bobby, bob, raul]
        def order = new OrderBy([{it}, {it.age}])
        def sorted = DefaultGroovyMethods.sort(people,order)

        assert sorted[0].first == 'Bob'
        assert sorted[0].age == '10'
        assert sorted[1].first == 'Bob'
        assert sorted[1].age == '20'
        assert sorted[2].first == 'Raul'
    }
}

class TestPerson implements Comparable {
    def first, last, age

    int compareTo(Object o) {
        "${first} ${last}".compareTo("${o.first} ${o.last}")
    }
}
