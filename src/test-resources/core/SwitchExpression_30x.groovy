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

// GEP-19: map patterns in switch expressions

def describe(x) {
    switch (x) {
        case [:]                                  -> 'empty map'
        case [type: 'circle', radius: var r]      -> "circle r=$r"
        case [name: String n, ... rest]           -> "named $n; others=${rest.size()}"
        case [id: var i, ...] when i > 0          -> "id $i"
        default                                   -> 'other'
    }
}

assert describe([:]) == 'empty map'
assert describe([type: 'circle', radius: 5]) == 'circle r=5'
assert describe([name: 'sam', age: 42]) == 'named sam; others=1'
assert describe([id: 7, x: 0]) == 'id 7'
assert describe([id: -1]) == 'other'
assert describe('hello') == 'other'

// nested patterns as entry values and map patterns within list patterns
record Point(int x, int y) {}
def nested = switch ([origin: new Point(3, 4), tags: ['a', 'b']]) {
    case [origin: Point(var x, _), tags: [var first, ...]] -> "$x-$first"
    default                                                -> 'other'
}
assert nested == '3-a'
def inList = switch ([[a: 1], 'x']) {
    case [[a: var v], var tag] -> "$v-$tag"
    default                    -> 'other'
}
assert inList == '1-x'

// a map literal without a binding form keeps its legacy lookup semantics
def legacy = switch ('a') {
    case [a: true, b: false] -> 'truthy value'
    default                  -> 'no'
}
assert legacy == 'truthy value'
