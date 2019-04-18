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
import java.util.stream.Stream

import static java.util.stream.Collectors.toList
import static java.util.stream.Collectors.toMap

// class::staticMethod
assert ['1', '2', '3'] == [1, 2, 3].stream().map(Integer::toString).collect(toList())

// class::instanceMethod
assert ['A', 'B', 'C'] == ['a', 'b', 'c'].stream().map(String::toUpperCase).collect(toList())

def robot = new Robot()

// instance::instanceMethod
assert ['Hi, Jochen', 'Hi, Paul', 'Hi, Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(robot::greet).collect(toList())

// class::staticMethod
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(Person::getText).collect(toList())
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(BasePerson::getText).collect(toList())

// instance::staticMethod
assert ['J', 'P', 'D'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(robot::firstCharOfName).collect(toList())

// class::instanceMethod
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(Person::getName).collect(toList())

// class::instanceMethod
assert 6G == Stream.of(1G, 2G, 3G).reduce(0G, BigInteger::add)

// ----------------------------------
class BasePerson {
    static String getText(Person p) {
        return p.name
    }
}

class Person extends BasePerson {
    private String name

    Person(String name) {
        this.name = name
    }

    String getName() {
        return this.name
    }

}

class Robot {
    String greet(Person p) {
        return "Hi, ${p.name}"
    }

    static char firstCharOfName(Person p) {
        return p.getName().charAt(0)
    }
}

def mr = String::toUpperCase
assert 'ABC' == mr('abc')
assert 'ABC' == String::toUpperCase('abc')

def m = ['apple', 'banana', 'orange'].stream().collect(toMap(e -> e.charAt(0), e -> e, (e1, e2) -> e1, LinkedHashMap<String, String>::new))
assert m instanceof LinkedHashMap
assert ['a', 'b', 'o'] as TreeSet == m.keySet() as TreeSet
assert ['apple', 'banana', 'orange'] as TreeSet == m.values() as TreeSet

assert new HashMap<String, Integer>() == HashMap<String, Integer>::new()
assert new HashSet<Integer>() == HashSet<Integer>::new()

assert new HashSet() == HashSet::new()
assert new String() == String::new()
assert 1 == Integer::new(1)
assert new String[0] == String[]::new(0)
assert new String[0] == String[]::new('0')
assert new String[1][2] == String[][]::new(1, 2)
assert new String[1][2][3] == String[][][]::new(1, 2, 3)

assert [new String[1], new String[2], new String[3]] == [1, 2, 3].stream().map(String[]::new).collect(toList())
assert [1, 2, 3] as String[] == [1, 2, 3].stream().map(String::valueOf).toArray(String[]::new)

def a = String[][]::new(1, 2)
def b = new String[1][2]
assert a.class == b.class && a == b

a = String[][][]::new(1, 2)
b = new String[1][2][]
assert a.class == b.class && a == b

a = String[][][][]::new(1, 2)
b = new String[1][2][][]
assert a.class == b.class && a == b
