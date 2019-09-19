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




package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyTestCase

class Groovy7285Bug extends GroovyTestCase {
    void testRuntimeStackableTraits() {
        assertScript '''trait D {
    void methodA() { ref << "D"; super.methodA() }
}
trait C {
    void methodA() { ref << "C"; super.methodA() }
}
trait B {
    void methodA() { ref << "B"; super.methodA() }
}
trait A {
    void methodA() { ref << "A" }
}

class M implements A, D, C, B { List ref = [] }
class Q {  List ref = []  }

def direct = new M()

println "Static: ${direct.methodA();direct.ref}"

def runtime = new Q().withTraits(A,D,C,B)

// we need another test to make sure that 2 proxies with the same set of traits are different
// because the traits ordering is different
def runtime2 = new Q().withTraits(A,D,C,B)

println "Dynamic: ${runtime.methodA();runtime.ref}"
println "Dynamic 2: ${runtime2.methodA();runtime2.ref}"

assert direct.ref == ['B','C','D','A']
assert direct.ref == runtime.ref
assert runtime2.ref == ['B','C','D','A']

'''
    }
}
