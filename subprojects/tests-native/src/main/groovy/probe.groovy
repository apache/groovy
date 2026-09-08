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

// The native-image parity probe (GROOVY-12365): a dynamic Groovy program
// exercising what the runtime does on its own behalf, so that the metadata the
// groovy jar ships can be checked against what the runtime actually needs and
// the program built into an image with no agent step for Groovy's part.
//
// Keep the marker lines: the build asserts on `PROBE OK`.

import groovy.concurrent.AsyncScope
import groovy.transform.CompileStatic

import static org.apache.groovy.runtime.async.AsyncSupport.await

class Point {
    int x, y
    Point plus(Point o) { new Point(x: x + o.x, y: y + o.y) }
    String toString() { "($x, $y)" }
}

trait Named {
    String name
    String greet() { "hello, $name" }
}

class Person implements Named {
    Person(String name) { this.name = name }
}

@CompileStatic
class StaticHelper {
    static int twice(int n) { n * 2 }
    static String join(List<String> parts) { parts.join('-') }
}

def numbers = (1..10).collect { it * it }.findAll { it % 2 == 0 }
def total = numbers.sum()
def words = 'the quick brown fox'.split(' ').collect { it.capitalize() }.join(' ')
def p = new Point(x: 1, y: 2) + new Point(x: 3, y: 4)
def names = ['a', 'b', 'c'].withIndex().collect { n, i -> "$i:$n" }
def adder = { a, b -> a + b }
def curried = adder.curry(40)
def map = [one: 1, two: 2].collectEntries { k, v -> [(k.toUpperCase()): v * 10] }
def range = ('a'..'e').step(2)
def now = new Date()
def dyn = p."${'x'}"
def person = new Person('groovy')
Runnable coerced = { println 'coerced closure ran' }
Comparator<Integer> byValue = { a, b -> a <=> b }
def sorted = [3, 1, 2].toSorted(byValue)
def statically = StaticHelper.twice(21) + StaticHelper.join(['a', 'b']).size()
def async = AsyncScope.withScope { scope ->
    def a = scope.async { 20 }
    def b = scope.async { 22 }
    await(a) + await(b)
}

println "numbers=$numbers total=$total"
println "words=$words"
println "point=$p dyn=$dyn"
println "names=$names"
println "curried=${curried(2)}"
println "map=$map"
println "range=$range"
println "date ok=${now instanceof Date}"
println "trait=${person.greet()}"
coerced.run()
println "sorted=$sorted"
println "static=$statically"
println "async=$async"
println "metaClass=${p.metaClass.class.simpleName} methods=${p.metaClass.methods.size() > 0}"
assert total == 220 && curried(2) == 42 && p.x == 4
assert person.greet() == 'hello, groovy' && sorted == [1, 2, 3] && statically == 45 && async == 42
println 'PROBE OK'
