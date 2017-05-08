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
println withPool {
}

println aa("bb", "cc") {
}

println this.aa("bb", "cc") {
}

println aa("bb", {println 123;}, "cc") {
}

aa("bb", "cc") {
    println 1
} { println 2 }

cc  {
    println 1
} {
    println 2
}

dd {
    println 3
}

obj.cc  {
    println 1
} {
    println 2
}

bb 1, 2, {println 123;}

obj."some method" (groovy.xml.dom.DOMCategory) {
}

obj."some ${'method'}" (groovy.xml.dom.DOMCategory) {
}
obj.someMethod (groovy.xml.dom.DOMCategory) {
}

use (groovy.xml.dom.DOMCategory) {
}

['a','b','c'].inject('x') {
    result, item -> item + result + item
}

println a."${hello}"('world') {
}

println a."${"$hello"}"('world') {
}

a."${"$hello"}" 'world', {
}

a.<String, Object>someMethod 'hello', 'world';

a[x] b
a[x] b c
a[x] b c d

"$x"(1, 2) a
"$x" 1, 2  a
