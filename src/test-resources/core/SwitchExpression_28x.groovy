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

// GEP-19: record patterns in switch expressions

record Point(int x, int y) {}
record Line(Point start, Point end) {}

def describe(obj) {
    switch (obj) {
        case Point(int x, int y) when x == y -> "diagonal $x"
        case Point(int x, _)                 -> "x=$x"
        case Line(Point(_, _), Point p2)     -> "ends at (${p2.x()}, ${p2.y()})"
        default                              -> 'other'
    }
}

assert describe(new Point(3, 3)) == 'diagonal 3'
assert describe(new Point(1, 2)) == 'x=1'
assert describe(new Line(new Point(0, 0), new Point(4, 5))) == 'ends at (4, 5)'
assert describe('hello') == 'other'

// var and def component bindings
def sum = switch (new Point(3, 4)) {
    case Point(var a, def b) -> a + b
    default                  -> -1
}
assert sum == 7

// component type narrowing: mismatching component falls through
record Box(Object value) {}
def content = switch (new Box('text')) {
    case Box(Integer i) -> "int $i"
    case Box(String s)  -> "string ${s.toUpperCase()}"
    default             -> 'other'
}
assert content == 'string TEXT'

// arity mismatch never matches
def one = switch (new Point(1, 2)) {
    case Point(var a) -> "one $a"
    default           -> 'no match'
}
assert one == 'no match'

// legacy method call labels keep isCase semantics
def lower(s) { s.toLowerCase() }
def noArg() { 42 }
def legacy = switch ('abc') {
    case lower('ABC') -> 'call label'
    default           -> 'no'
}
assert legacy == 'call label'
def legacy2 = switch (42) {
    case noArg() -> 'no-arg call label'
    default      -> 'no'
}
assert legacy2 == 'no-arg call label'
