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

class BarHolder {
    def foo2(lambda) {
        lambda()
    }
    def bar(lambda) {
        lambda() + 1
    }
    def bar2(lambda) {
        lambda(6) + 1
    }
}

def foo(lambda) {
    lambda()
}

def result =
        foo () -> {
            new BarHolder()
        }
        .bar () -> {
            2
        }
assert 3 == result

def result2 =
        foo () -> {
            new BarHolder()
        }
        .bar2 (e) -> {
            2 + e
        }
assert 9 == result2

def result3 =
        foo () -> {
            new BarHolder()
        }
        .foo2 () -> {
            new BarHolder()
        }
        .bar () -> {
            2
        }
assert 3 == result3

def foo5(c) {c()}
def c2 = foo5 { { Integer x -> 1} }
assert 1 == c2()
