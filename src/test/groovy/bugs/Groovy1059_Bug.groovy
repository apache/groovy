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

/**
 * GROOVY-1059
 *
 *    Accessible to a closure attribute of an abject with the operator ".@".
 *    For examples, all of the expressions
 *
 *            object.@closure()
 *            object.@closure.call()
 *            object.@closure.doCall()
 *            (object.@closure)()
 *
 *    have the same meaning.
 */

class Groovy1059_Bug extends GroovyTestCase {

    void testClosureAsAttribute() {
        def x = new Groovy1059Foo()

        assert "I am a Method" == x.say()
        assert "I am a Method" == x.@say2()
        assert "I am a Closure" == (x.@say)()
        assert "I am a Closure" == x.@say()
        assert x.@say() == (x.@say)()
        assert x.@say() == x.@say.call()
        assert x.@say() == x.@say.doCall()
        assert x.@say() != x.say()
        assert x.@say2() == x.say()
    }

}

class Groovy1059Foo {

    def public say = { it -> return "I am a Closure" }
    def public say2 = this.&say

    public Object say() {
       return "I am a Method"
    }
}
