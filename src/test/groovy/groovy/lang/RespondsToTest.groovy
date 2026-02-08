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
package groovy.lang

import groovy.test.GroovyTestCase

/**
 * Tests the respondsTo functionality of Groovy
 *
 * @since 1.6.0
 */
class RespondsToTest extends GroovyTestCase {

    void testRespondsToForMethodEvaluation() {
        RespondsToTestClass.metaClass.invokeMethod = { String name, args ->
            def methods = RespondsToTestClass.metaClass.respondsTo(delegate, name, args*.getClass() as Class[])
            def result
            if (methods) {
                // only way to get var-args to work is to do this at the moment. Yuck!
                if (methods[0].parameterTypes.length == 1 && methods[0].parameterTypes[0].theClass == Object[].class)
                    result = methods[0].invoke(delegate, [args] as Object[])
                else
                    result = methods[0].invoke(delegate, args)
            } else {
                result = "foo"
            }
            result
        }

        def t = new RespondsToTestClass()
        assertEquals "one", t.noArgsMethod()
        assertEquals "two", t.varArgsMethod(1, 2)
        assertEquals "two", t.varArgsMethod(null, null)
        assertEquals "three", t.typedArgsMethod("one", 1)
        assertEquals "three", t.typedArgsMethod(null, 1)
        assertEquals "three", t.typedArgsMethod(null, null)
        assertEquals "four", t.overloadedMethod("one")
        assertEquals "five", t.overloadedMethod(1)
        assertEquals "four", t.overloadedMethod(null)
        assertEquals "six", t.overloadedMethod()
        assertEquals "foo", t.doStuff()
    }

    void testRespondsTo() {
        RTTest2.metaClass.newM = { -> "foo" }
        def t = new RTTest2()
        assert t.metaClass.respondsTo(t, "one")
        assert t.metaClass.respondsTo(t, "three")
        assert t.metaClass.respondsTo(t, "one", String)
        assert t.metaClass.respondsTo(t, "foo", String)
        assert t.metaClass.respondsTo(t, "bar", String)
        assert t.metaClass.respondsTo(t, "stuff")
        //assert t.metaClass.respondsTo(t, "two") // THIS DOESN'T WORK! Should respondsTo deal with closure properties?
//        assert t.metaClass.respondsTo(t, "newtwo") // THIS DOESN'T WORK! Should respondsTo deal with closure properties?
        assert t.metaClass.respondsTo(t, "getFive")
        assert t.metaClass.respondsTo(t, "setFive")
        assert t.metaClass.respondsTo(t, "setFive", String)
        assert t.metaClass.respondsTo(t, "newM")
        assert !t.metaClass.respondsTo(t, "one", String, Integer)
        // and again for DGM variations
        assert t.respondsTo("one")
        assert t.respondsTo("three")
        assert t.respondsTo("one", String)
        assert t.respondsTo("foo", String)
        assert t.respondsTo("bar", String)
        assert t.respondsTo("stuff")
        //assert t.respondsTo("two") // THIS DOESN'T WORK! Should respondsTo deal with closure properties?
//        assert t.respondsTo("newtwo") // THIS DOESN'T WORK! Should respondsTo deal with closure properties?
        assert t.respondsTo("getFive")
        assert t.respondsTo("setFive")
        assert t.respondsTo("setFive", String)
        assert t.respondsTo("newM")
        assert !t.respondsTo("one", String, Integer)
    }

    void testHasProperty() {
        RTTest2.metaClass.getNewProp = { -> "new" }
        def t = new RTTest2()
        assert t.metaClass.hasProperty(t, "two")
        assert t.metaClass.hasProperty(t, "newtwo")
        assert t.metaClass.hasProperty(t, "five")
        assert t.metaClass.hasProperty(t, "six")
        assert t.metaClass.hasProperty(t, "seven")
        assert t.metaClass.hasProperty(t, "eight")
        assert t.metaClass.hasProperty(t, "newProp")
        // and again for DGM variations
        assert t.hasProperty("two")
        assert t.hasProperty("newtwo")
        assert t.hasProperty("five")
        assert t.hasProperty("six")
        assert t.hasProperty("seven")
        assert t.hasProperty("eight")
        assert t.hasProperty("newProp")
    }
}

class RespondsToTestClass {
    def noArgsMethod() { "one" }

    def varArgsMethod(Object[] args) { "two" }

    def typedArgsMethod(String one, Integer two) { "three" }

    def overloadedMethod(String one) { "four" }

    def overloadedMethod(Integer one) { "five" }

    def overloadedMethod() { "six" }
}

class RTTest1 {
    String five
    def two = { "three" }

    def one() { "two" }

    def one(String one) { "two: $one" }

    def three(String one) { "four" }

    def three(Integer one) { "four" }

    def foo(String name) {
        "bar"
    }

    String getSeven() { "seven" }
}

class RTTest2 extends RTTest1 {
    String six

    def newtwo = { "newthree" }

    def bar(String name) { "foo" }

    static stuff() { "goodie" }

    String getEight() { "eight" }
}