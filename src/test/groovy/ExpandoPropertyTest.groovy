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

import groovy.test.GroovyTestCase

class ExpandoPropertyTest extends GroovyTestCase {

    void testExpandoProperty() {
        def foo = new Expando()

        foo.cheese = "Cheddar"
        foo.name = "Gromit"

        assert foo.cheese == "Cheddar"
        assert foo.name == "Gromit"

        assert foo.properties.size() == 2
    }

    void testExpandoMethods() {
        def foo = new Expando()

        foo.cheese = "Cheddar"
        foo.fullName = "Gromit"
        foo.nameLength = { return fullName.length() }
        foo.multiParam = { a, b, c -> return a + b + c }

        assert foo.cheese == "Cheddar"
        assert foo.fullName == "Gromit"
        assert foo.nameLength() == 6, foo.nameLength()
        assert foo.multiParam(1, 2, 3) == 6

        // lets test using wrong number of parameters
        shouldFail { foo.multiParam(1) }
        shouldFail { foo.nameLength(1, 2) }
    }

    void testExpandoMethodCloning() {
        def foo = new Expando()
        def c = {
            assert delegate instanceof Expando
            1
        }
        foo.one = c

        assert foo.one() == 1
        assert !(c.delegate instanceof Expando)
    }

    void testExpandoConstructorAndToString() {
        def foo = new Expando(type: "sometype", value: 42)
        assert foo.toString() == "{type=sometype, value=42}"
        assert "${foo}" == "{type=sometype, value=42}"
    }

    void testExpandoMethodOverrides() {
        def equals = { Object obj -> return obj.value == value }
        def foo = new Expando(type: "myfoo", value: 42, equals: equals)
        def bar = new Expando(type: "mybar", value: 43, equals: equals)
        def zap = new Expando(type: "myzap", value: 42, equals: equals)

        assert foo.equals(bar) == false
        assert foo.equals(zap) == true

        def list = []
        list << foo
        list << bar

        assert list.contains(foo) == true
        assert list.contains(bar) == true
        assert list.contains(zap) == true
        assert list.indexOf(bar) == 1
        assert list.indexOf(foo) == 0

        foo.hashCode = { return value }
        assert foo.hashCode() == foo.value

        foo.toString = { return "Type: ${type}, Value: ${value}" }
        assert foo.toString() == "Type: myfoo, Value: 42"
    }

    void testArrayAccessOnThis() {
        def a = new FancyExpando([a: 1, b: 2])
        a.update([b: 5, a: 2])

        assert a.a == 2
        assert a.b == 5
    }

    void testExpandoClassProperty() {
        def e = new Expando()
        e.class = "hello world"

        assert e.class == "hello world"
    }
}

class FancyExpando extends Expando {
    FancyExpando(args) { super(args) }

    def update(args) {
        for (e in args) this[e.key] = e.value // using 'this'
    }

    String toString() { dump() }
}
