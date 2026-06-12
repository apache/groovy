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

// basic val declaration
val name = "Groovy"
assert "Groovy" == name

// val as closure parameter name
[1, 2, 3].each { val ->
    assert 0 < val && val < 4
}

// val as variable name (contextual keyword)
val val = "val variable name"
assert "val variable name" == val

// val in map key
def m = [val: 42]
assert m.val == 42

// val with different types
val x = 42
assert x == 42
val s = "hello"
assert s.class == String

// shallow finality - mutation OK
val list = [1, 2, 3]
list << 4
assert list == [1, 2, 3, 4]

// final val is redundant but works
final val y = 10
assert y == 10

// for loop with val
for (val i in [1, 2, 3]) { assert i > 0 }

// GString interpolation
val g = 99
assert "$g" == "99"
