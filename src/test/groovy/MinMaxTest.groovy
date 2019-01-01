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
package groovy

/**
 * Tests the min() and max() functions
 */
class MinMaxTest extends GroovyTestCase {

    void testSimpleMinMax() {
        def list = [5, 2, 6, 1, 9, 8]

        def n = list.min()
        assert n == 1

        n = list.max()
        assert n == 9
    }

    void testMinMaxWithComparator() {
        def people = getPeople()

        // let's find the maximum by name

        def order = new OrderBy({ it.get('@cheese') })

        def p = people.min(order)

        assert p.get("@name") == "Joe", "found person ${p}"

        p = people.max(order)
        assert p.get("@name") == "Chris", "found person ${p}"
    }

    void testMinMaxOnArraysWithComparator() {
        Person[] people = [
                new Person(name: 'James', cheese: 'Edam', location: 'London'),
                new Person(name: 'Bob', cheese: 'Cheddar', location: 'Atlanta'),
                new Person(name: 'Chris', cheese: 'Red Leicester', location: 'London'),
                new Person(name: 'Joe', cheese: 'Brie', location: 'London')
        ]

        // let's find the maximum by name
        def order = new OrderBy({ it.cheese })

        def p = people.min(order)
        assert p.name == "Joe", "Expected to find Joe but found person ${p}"

        p = people.max(order)
        assert p.name == "Chris", "Expected to find Chris but found person ${p}"
    }

    def getPeople() {
        def builder = new NodeBuilder()
        def tree = builder.people {
            person(name: 'James', cheese: 'Edam', location: 'London')
            person(name: 'Bob', cheese: 'Cheddar', location: 'Atlanta')
            person(name: 'Chris', cheese: 'Red Leicester', location: 'London')
            person(name: 'Joe', cheese: 'Brie', location: 'London')
        }

        return tree.children()
    }

}

class Person {
    String name
    String cheese
    String location
}
