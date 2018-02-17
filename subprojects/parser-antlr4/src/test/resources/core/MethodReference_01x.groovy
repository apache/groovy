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
import java.util.stream.Collectors

// class::staticMethod
assert ['1', '2', '3'] == [1, 2, 3].stream().map(Integer::toString).collect(Collectors.toList())

// class::instanceMethod
assert ['A', 'B', 'C'] == ['a', 'b', 'c'].stream().map(String::toUpperCase).collect(Collectors.toList())



def robot = new Robot();

// instance::instanceMethod
assert ['Hi, Jochen', 'Hi, Paul', 'Hi, Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(robot::greet).collect(Collectors.toList())

// class::staticMethod
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(Person::getText).collect(Collectors.toList())
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(BasePerson::getText).collect(Collectors.toList())

// instance::staticMethod
assert ['J', 'P', 'D'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(robot::firstCharOfName).collect(Collectors.toList())

// class::instanceMethod
assert ['Jochen', 'Paul', 'Daniel'] == [new Person('Jochen'), new Person('Paul'), new Person('Daniel')].stream().map(Person::getName).collect(Collectors.toList())

// class::instanceMethod
assert 6 == java.util.stream.Stream.of(1, 2, 3).reduce(0, BigDecimal::add)

// ----------------------------------
class BasePerson {
    public static String getText(Person p) {
        return p.name;
    }
}

class Person extends BasePerson {
    private String name;

    public Person(String name) {
        this.name = name
    }

    public String getName() {
        return this.name;
    }

}
class Robot {
    public String greet(Person p) {
        return "Hi, ${p.name}"
    }

    public static char firstCharOfName(Person p) {
        return p.getName().charAt(0);
    }
}

def mr = String::toUpperCase
assert 'ABC' == mr('abc')
assert 'ABC' == String::toUpperCase('abc')

def m = ['apple', 'banana', 'orange'].stream().collect(Collectors.toMap(e -> e.charAt(0), e -> e, (e1, e2) -> e1, LinkedHashMap<String, String>::new))
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

assert [new String[1], new String[2], new String[3]] == [1, 2, 3].stream().map(String[]::new).collect(Collectors.toList())
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



