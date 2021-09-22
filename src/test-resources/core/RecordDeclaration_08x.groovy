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
package core

record Person(String name, int age) {
    public Person {
        if (name == 'Devil') throw new IllegalArgumentException("Invalid person: $name")
        if (age < 18) throw new IllegalArgumentException("Invalid age: $age")
    }
}

assert 'core.Person(name:Daniel, age:37)' == new Person('Daniel', 37).toString()
try {
    new Person('Peter', 3)
    assert false, 'should failed because of invalid age'
} catch (e) {
    assert 'Invalid age: 3' == e.message
}

try {
    new Person('Devil', 100)
    assert false, 'should failed because of invalid name'
} catch (e) {
    assert 'Invalid person: Devil' == e.message
}

try {
    new Person(name: 'Peter', age: 3)
    assert false, 'should failed because of invalid age'
} catch (e) {
    assert 'Invalid age: 3' == e.message
}

try {
    new Person(name: 'Devil', age: 100)
    assert false, 'should failed because of invalid name'
} catch (e) {
    assert 'Invalid person: Devil' == e.message
}
