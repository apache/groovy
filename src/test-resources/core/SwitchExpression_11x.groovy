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

try {
    def a = 6
    def result = switch(a) {
        case 6 : throw new RuntimeException('a')
    }
} catch (e) {
    assert 'a' == e.message
}

a = 6
result = switch(a) {
    case 6 : yield 'a'
}
assert 'a' == result

a = 8
result = switch(a) {
    case 6 : yield 'a'
    default: yield 'b'
}
assert 'b' == result

a = 9
result = switch(a) {
    case 6, 9 : yield 'a'
    default: yield 'b'
}
assert 'a' == result

a = 7
result = switch(a) {
    case 7:
    case 6, 9 : yield 'a'
    default: yield 'b'
}
assert 'a' == result


a = 10
result = switch(a) {
    case 7:
    case 6, 9 : yield 'a'
    case 10:
    case 11: yield 'c'
    default: yield 'b'
}
assert 'c' == result

a = 10
result = switch(a) {
    case 7:
    case 6, 9 : yield 'a'
    case 10:
    case 11: {
            def x = 1
            yield 'c' + x
        }
    default: yield 'b'
}
assert 'c1' == result


a = 6
result = switch(a) {
    case 6 : yield 'a'
}
assert 'a' == result

a = 6
result = switch(a) {
    default : yield 'b'
}
assert 'b' == result

a = 6
result = switch(a) {
    case 6 : { yield 'a' }
}
assert 'a' == result

a = 6
result = switch(a) {
    default : { yield 'b' }
}
assert 'b' == result

a = 6
result = switch(a) {
    case 6  : yield 'a'
    default : yield 'b'
}
assert 'a' == result
