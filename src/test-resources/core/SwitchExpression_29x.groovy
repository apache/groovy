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

// GEP-19: list patterns in switch expressions

def describe(x) {
    switch (x) {
        case []                                   -> 'empty'
        case [var only]                           -> "single: $only"
        case [Integer h, var... t] when h > 0     -> "positive head $h, ${t.size()} more"
        case [var first, var... middle, var last] -> "$first .. $last"
        case [...]                                -> 'other non-empty list'
        default                                   -> 'not a list'
    }
}

assert describe([]) == 'empty'
assert describe(['x']) == 'single: x'
assert describe([1, 2, 3]) == 'positive head 1, 2 more'
assert describe([-1, 2, 3]) == '-1 .. 3'
assert describe(new int[] {5, 6}) == 'positive head 5, 1 more'
assert describe('hello') == 'not a list'

// literal elements are matched by equality
def starts = switch ([1, 9, 8]) {
    case [1, var x, ...] -> "starts with 1, then $x"
    default              -> 'no'
}
assert starts == 'starts with 1, then 9'

// typed rest binding checks every element
def total = switch ([1, 2, 3]) {
    case [Integer... nums] -> nums.sum()
    default                -> 'not all ints'
}
assert total == 6

// nested record and list patterns
record Point(int x, int y) {}
def nested = switch ([new Point(3, 4), [5]]) {
    case [Point(var x, _), [var tail]] -> "$x-$tail"
    default                            -> 'no'
}
assert nested == '3-5'

// a list literal without a binding form keeps its legacy containment semantics
def legacy = switch (2) {
    case [1, 2, 3] -> 'contained'
    default        -> 'no'
}
assert legacy == 'contained'
