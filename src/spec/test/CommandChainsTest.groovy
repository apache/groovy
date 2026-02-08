import groovy.test.GroovyTestCase

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
class CommandChainsTest extends GroovyTestCase {
    void testCommandChainEquivalence() {
        assertScript '''String left = 'left'
String right = 'right'

class Move {

    def list = []

    Move turn(direction) {
        list << direction
        this
    }

    Move then(direction) {
        list << direction
        this
    }
}

def m1 = new Move()
def m2 = new Move()
m1.with {
    // tag::commandchain_1[]
    // equivalent to: turn(left).then(right)
    turn left then right
    // end::commandchain_1[]
}
m2.turn(left).then(right)
assert m1.list == ['left', 'right']
assert m1.list == m2.list

'''
        assertScript '''import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class DSL {
    int qt
    String medicine
    int duration

    def take(qty) {
        qt = qty
        this
    }

    def of(med) {
        medicine = med
        this
    }

    def after(dur) {
        duration = dur
        this
    }
}

class IntCategory {
    public static int getPills(Integer x) {
        x
    }

    public static int getHours(Integer x) {
        x
    }
}

String chloroquinine = 'chloroquinine'
def m1 = new DSL()
def m2 = new DSL()
use(IntCategory) {
    m1.with {
        // tag::commandchain_2[]
        // equivalent to: take(2.pills).of(chloroquinine).after(6.hours)
        take 2.pills of chloroquinine after 6.hours
        // end::commandchain_2[]
    }
    m2.take(2.pills).of(chloroquinine).after(6.hours)
}
assert m1.qt == 2
assert m1.medicine == 'chloroquinine'
assert m1.duration == 6
assert m1 == m2
'''
        assertScript '''import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class DSL {
    String object
    List<String> colors = []
    def paint(obj) {
        object = obj
        this
    }
    def with(String... c) {
        colors.addAll(c as List)
        this
    }
    def and(String... c) {
        colors.addAll(c as List)
        this
    }
}
String wall = 'wall'
String red = 'red'
String green = 'green'
String yellow = 'yellow'
def m1 = new DSL()
def m2 = new DSL()
m1.with {
    // tag::commandchain_3[]
    // equivalent to: paint(wall).with(red, green).and(yellow)
    paint wall with red, green and yellow
    // end::commandchain_3[]
}
m2.paint(wall).with(red, green).and(yellow)
assert m1 == m2
assert m1.object == 'wall'
assert m1.colors == ['red','green','yellow']
'''
        assertScript '''import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class DSL {
    def map = [:]
    boolean test
    def check(m) { map=m; this}
    def tastes(taste) {
        test = taste=='good'?map.that=='margarita':false
    }
}
def margarita = 'margarita'
def good = 'good'
def m1 = new DSL()
def m2 = new DSL()
m1.with {
    // tag::commandchain_4[]
    // with named parameters too
    // equivalent to: check(that: margarita).tastes(good)
    check that: margarita tastes good
    // end::commandchain_4[]
}
m2.check(that: margarita).tastes(good)
assert m1 == m2
assert m1.map == [that: 'margarita']
assert m1.test == true
'''
        assertScript '''
class DSL {
    int count
    void cpt(Closure c) { count++ }
    def given(Closure c) { cpt(c) ; this }
    def when(Closure c) { cpt(c) ; this }
    def then(Closure c) { cpt(c) ; this }
}
def m1 = new DSL()
def m2 = new DSL()
m1.with {
    // tag::commandchain_5[]
    // with closures as parameters
    // equivalent to: given({}).when({}).then({})
    given { } when { } then { }
    // end::commandchain_5[]
}
m2.given({}).when({}).then({})
assert m1.count == 3
assert m2.count == 3
'''
        assertScript '''import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class DSL {
    String columns
    List names
    boolean unique = false
    def select(columns) { this.columns = columns; this }
    def unique() {
        unique = true;
        this
    }
    def from(values) {
        names = unique?values.unique():values
    }
}
def all = 'all'
def names = ['Bob','Alice','Alice']
def m1 = new DSL()
def m2 = new DSL()
m1.with {
    // tag::commandchain_6[]
    // equivalent to: select(all).unique().from(names)
    select all unique() from names
    // end::commandchain_6[]
}
m2.select(all).unique().from(names)
assert m1 == m2
assert (m1.names as Set) == ['Bob','Alice'] as Set
'''
        assertScript '''import groovy.transform.Canonical

@Canonical
class DSL {
    String thing
    int qte
    def take(n) {
        qte = n
        this
    }
    def propertyMissing(String name) {
        thing = name
    }
    def methodMissing(String name, args) {
        if (name.startsWith('get') && !args) {
            thing = name.substring(3).toLowerCase()
        }
    }
}
def m1 = new DSL()
def m2 = new DSL()
def m3 = new DSL()
m1.with {
    // tag::commandchain_7[]
    // equivalent to: take(3).cookies
    // and also this: take(3).getCookies()
    take 3 cookies
    // end::commandchain_7[]
}
m2.take(3).cookies
m3.take(3).getCookies()

assert m1 == m2
assert m2 == m3
assert m1.qte == 3
assert m1.thing == 'cookies'
'''
    }

    void testCommandChainImplementation() {
        assertScript '''
// tag::commandchain_impl1[]
show = { println it }
square_root = { Math.sqrt(it) }

def please(action) {
  [the: { what ->
    [of: { n -> action(what(n)) }]
  }]
}

// equivalent to: please(show).the(square_root).of(100)
please show the square_root of 100
// ==> 10.0
// end::commandchain_impl1[]
'''
        assertScript '''
// tag::commandchain_impl2[]
@Grab('com.google.guava:guava:r09')
import com.google.common.base.*
// end::commandchain_impl2[]
// tag::commandchain_impl2_assert[]
def result = Splitter.on(',').trimResults(CharMatcher.is('_' as char)).split("_a ,_b_ ,c__").iterator().toList()
// end::commandchain_impl2_assert[]
assert result == ['a ', 'b_ ', 'c']
'''
        assertScript '''
// tag::commandchain_impl3[]
@Grab('com.google.guava:guava:r09')
import com.google.common.base.*
def split(string) {
  [on: { sep ->
    [trimming: { trimChar ->
      Splitter.on(sep).trimResults(CharMatcher.is(trimChar as char)).split(string).iterator().toList()
    }]
  }]
}
// end::commandchain_impl3[]
// tag::commandchain_impl3_assert[]
def result = split "_a ,_b_ ,c__" on ',' trimming '_\'
// end::commandchain_impl3_assert[]
'''
    }
}
