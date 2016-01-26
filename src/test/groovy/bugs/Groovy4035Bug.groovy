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

class Groovy4035Bug extends GroovyTestCase {
    void testSuperCallInsideAnAIC() {
        def aic = new Foo4035() {
            def foo(Object msg) {
                return "AIC-" + super.foo(msg)
            }
        }
        assert aic.foo("42") == "AIC-Foo4035-42"
    }

    void testSuperCallInsideANormalInnerClass() {
        def inner = new Inner4035()
        
        assert inner.foo("42") == "Inner-Foo4035-42"
    }

    class Inner4035 extends Foo4035 {
        def foo(Object msg) {
            return "Inner-" + super.foo(msg)
        }
    }
}

class Foo4035 {
    def foo(msg) {
        "Foo4035-" + msg
    }
}
