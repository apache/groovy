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

// annotated for-in loops
@Test1 for (i in someList) {
    break
}

@Test1 @Test2 for (i in someList) {
    break
}

@Test1(value=1) for (i in someList) {
    break
}

@Test1
@Test2
for (i in someList) {
    break
}

@Test1(v1=1, v2=2)
@Test2
for (String i in someList) {
    break
}

// annotated classic for loops
@Test1 for (int i = 0; i < 10; i++) {
    break
}

@Test1 @Test2 for (int i = 0; i < 10; i++) {
    break
}

@Test1
@Test2(value='x')
for (int i = 0; i < 10; i++) {
    break
}

// annotated while loops
@Test1 while (true) {
    break
}

@Test1 @Test2 while (true) {
    break
}

@Test1(value=1) while (true) {
    break
}

@Test1
@Test2
while (true) {
    break
}

@Test1(v1=1, v2=2)
@Test2
while (true) {
    break
}

// annotated do-while loops
@Test1 do {
    break
} while (true)

@Test1 @Test2 do {
    break
} while (true)

@Test1(value=1) do {
    break
} while (true)

@Test1
@Test2
do {
    break
} while (true)

@Test1(v1=1, v2=2)
@Test2
do {
    break
} while (true)

// fully-qualified annotation on loop
@java.lang.Deprecated for (i in someList) {
    break
}

@java.lang.Deprecated while (true) {
    break
}

@java.lang.Deprecated do {
    break
} while (true)

// annotation with nested parentheses
@Test1(value=(1 + 2)) for (i in someList) {
    break
}

@Test1(value=(1 + 2)) while (true) {
    break
}

@Test1(value=(1 + 2)) do {
    break
} while (true)
