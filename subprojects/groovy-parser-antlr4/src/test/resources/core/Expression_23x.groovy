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
def a
    =
        1 + 2
assert 3 == a

a
    +=
        2
assert 5 == a

int b
    =
        1,
    c
        =
            2
assert 1 == b
assert 2 == c

def (int x, int y)
    =
        [1, 2]
assert 1 == x
assert 2 == y
(x)
    =
        [3]
assert 3 == x

@SuppressWarnings(value
        =
        "all")
def m(p1
        =
            1,
      p2
        =
            2,
      int... p3
                =
                    [3]) {
    return p1 + p2 + p3[0]
}
assert 6 == m()

def w
    =
        1
            <<
                2
assert 4 == w
assert 'a'
            instanceof
                        String
assert 1
            <
                2

assert 1
            ==
                1

assert 'a'
            ==~
                /a/
assert true
            &
                true
assert true
            ^
                false
assert true
            |
                true

assert true
            &&
                true

assert true
            ||
                true


def z =
        9
            /
                3
                    *
                        2
assert 6 == z

def r =
         3
            %
                2
assert 1 == r