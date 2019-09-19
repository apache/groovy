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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy3163Test extends GroovyTestCase {

void testSuperOverStatic() {
    def siws = new Groovy3163SomeImplementorWithStatic()

    assert (1 == siws.build(1)[0])
    
    def c = { -> 'foo ' }
    
//    def s = c as Script
//    assert s.is(siws.build(s)[0])

    assert c.is(siws.build(c)[0])
}

}


class Groovy3163SomeBaseClass {

    Object build(Integer i) {
        return i
    }

    Object build(BigInteger i) {
        return i
    }

    Object build(Class c) {
        return c
    }

    Object build(Script s) {
        return s
    }
}

class Groovy3163SomeImplementorWithStatic extends Groovy3163SomeBaseClass {

    // Comment this out, otherwise the super.build(x) calls won't match the members in our parent.

    static Object build(Closure c) {
        return [c]
    }

    // This one will also block a super.build, but it's the Script one.
    static Object build(BigDecimal d) {
        return [d]
    }

    Object build(Integer i) {
        return [super.build(i)]
    }

    Object build(Script s) {
        return [super.build(s)]
    }

}
