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

// GEP-19: type patterns with binding and `when` guards in switch expressions

def describe(obj) {
    switch (obj) {
        case Integer i when i > 0 -> "positive $i"
        case Integer i            -> "non-positive $i"
        case String s             -> s.toUpperCase()
        case List list            -> "list of ${list.size()}"
        default                   -> 'other'
    }
}

assert describe(42) == 'positive 42'
assert describe(-7) == 'non-positive -7'
assert describe('abc') == 'ABC'
assert describe([1, 2, 3]) == 'list of 3'
assert describe(3.5) == 'other'
assert describe(null) == 'other'

// pattern labels mixed with legacy labels
def kind(x) {
    switch (x) {
        case 0, 1                 -> 'small constant'
        case Number n when n < 0  -> 'negative'
        case Number n             -> "number $n"
        case null                 -> 'nil'
        default                   -> 'other'
    }
}

assert kind(0) == 'small constant'
assert kind(-3) == 'negative'
assert kind(10) == 'number 10'
assert kind(null) == 'nil'
assert kind('x') == 'other'

// guards may reference outer locals as well as the pattern variable
def threshold = 5
def sizeCheck = switch ([1, 2, 3, 4, 5, 6]) {
    case List l when l.size() > threshold -> 'big'
    case List l                           -> 'small'
    default                               -> 'other'
}
assert sizeCheck == 'big'

// arrow switch with patterns used as a statement
def out = []
switch ('hi') {
    case String s -> out << s.reverse()
    default       -> out << 'none'
}
assert out == ['ih']

// nested switch expressions with patterns
def nested = switch (1) {
    case Integer i -> switch ('a') {
        case String s -> s + i
        default       -> 'inner other'
    }
    default -> 'outer other'
}
assert nested == 'a1'

// array type pattern
def arr = switch (new String[]{'a', 'b'}) {
    case String[] sa -> sa.length
    default          -> -1
}
assert arr == 2

// switch subject is evaluated exactly once
int count = 0
def next = { count++; 'x' }
def got = switch (next()) {
    case String s when s == 'x' -> 'match'
    default                     -> 'no'
}
assert got == 'match'
assert count == 1
