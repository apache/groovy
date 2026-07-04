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

// GEP-19: primitive type patterns (JEP 507 alignment): the pattern tests the
// wrapper type and binds the primitive; there is no widening or narrowing
def describe(subject) {
    switch (subject) {
        case int i when i < 0 -> "negative int $i"
        case int i -> "int $i"
        case long l -> "long $l"
        case double d -> "double $d"
        case boolean b -> "boolean $b"
        case char c -> "char $c"
        default -> 'other'
    }
}

assert describe(42) == 'int 42'
assert describe(-7) == 'negative int -7'
assert describe(42L) == 'long 42'
assert describe(3.5d) == 'double 3.5'
assert describe(true) == 'boolean true'
assert describe('x' as char) == 'char x'
assert describe(42 as byte) == 'other'
assert describe(3.5) == 'other' // BigDecimal is not a double
assert describe(null) == 'other'

// mixed with a legacy label (closure-label lowering)
def area(shape) {
    switch (shape) {
        case int side when side > 0 -> side * side
        case ~/circle:\d+/ -> 'circle'
        default -> 'unknown'
    }
}

assert area(3) == 9
assert area('circle:4') == 'circle'
assert area(-3) == 'unknown'
