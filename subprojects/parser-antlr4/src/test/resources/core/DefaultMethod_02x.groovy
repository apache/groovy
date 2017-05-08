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
interface A {
    default String hello() {
        return 'hello'
    }
    default String a() {
        return 'a'
    }
    String b();
}

interface B extends A {
    public String world();
}

class C implements B {
    public static String haha() { return 'haha' }

    public String world() {
        return 'world'
    }

    public String name() {
        return 'c'
    }

    public String a() {
        return 'a1';
    }

    public String b() {
        return 'b'
    }
}

def c = new C()
assert 'hello, world, c, a1, b, haha' == "${c.hello()}, ${c.world()}, ${c.name()}, ${c.a()}, ${c.b()}, ${C.haha()}"