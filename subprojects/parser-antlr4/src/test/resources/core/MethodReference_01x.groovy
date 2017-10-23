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

def robot = new Robot()

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

assert new HashSet() == HashSet::new()
assert new String() == String::new()
assert 1 == Integer::new(1)
assert new String[0] == String[]::new(0)
assert new String[0] == String[]::new('0')
assert new String[1][2] == String[][]::new(1, 2)
assert new String[1][2][3] == String[][][]::new(1, 2, 3)

def a = String[][]::new(1, 2)
def b = new String[1][2]
assert a.class == b.class && a == b

a = String[][][]::new(1, 2)
b = new String[1][2][]
assert a.class == b.class && a == b

a = String[][][][]::new(1, 2)
b = new String[1][2][][]
assert a.class == b.class && a == b
