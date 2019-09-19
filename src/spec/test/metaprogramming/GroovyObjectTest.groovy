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
package metaprogramming

import groovy.test.GroovyTestCase

class GroovyObjectTest extends GroovyTestCase {
    void testInvokeMethod() {
        assertScript '''
// tag::groovy_invoke_method[]
class SomeGroovyClass {

    def invokeMethod(String name, Object args) {
        return "called invokeMethod $name $args"
    }
    
    def test() {
        return 'method exists'
    }
}

def someGroovyClass = new SomeGroovyClass()

assert someGroovyClass.test() == 'method exists'
assert someGroovyClass.someMethod() == 'called invokeMethod someMethod []'
// end::groovy_invoke_method[]
'''
    }
    
    void testGetProperty () {
        assertScript '''
// tag::groovy_get_property[]
class SomeGroovyClass {

    def property1 = 'ha'
    def field2 = 'ho'
    def field4 = 'hu'
    
    def getField1() {
        return 'getHa'
    }
    
    def getProperty(String name) {
        if (name != 'field3')
            return metaClass.getProperty(this, name) // <1>
        else
            return 'field3'
    }
}

def someGroovyClass = new SomeGroovyClass()

assert someGroovyClass.field1 == 'getHa'
assert someGroovyClass.field2 == 'ho'
assert someGroovyClass.field3 == 'field3'
assert someGroovyClass.field4 == 'hu'
// end::groovy_get_property[]
'''
    }
    
    void testSetProperty () {
        assertScript '''
// tag::groovy_set_property[]
class POGO {

    String property 
    
    void setProperty(String name, Object value) {
        this.@"$name" = 'overridden'
    }
}

def pogo = new POGO()
pogo.property = 'a'

assert pogo.property == 'overridden'
// end::groovy_set_property[]
'''
    }

    void testGetAttribute () {
        assertScript '''
// tag::groovy_get_attribute[]
class SomeGroovyClass {

    def field1 = 'ha'
    def field2 = 'ho'
    
    def getField1() {
        return 'getHa'
    }
}

def someGroovyClass = new SomeGroovyClass()

assert someGroovyClass.metaClass.getAttribute(someGroovyClass, 'field1') == 'ha'
assert someGroovyClass.metaClass.getAttribute(someGroovyClass, 'field2') == 'ho'
// end::groovy_get_attribute[]
'''
    }
    
    void testSetAttribute () {
        assertScript '''
// tag::groovy_set_attribute[]
class POGO {

    private String field
    String property1
    
    void setProperty1(String property1) {
        this.property1 = "setProperty1"
    }
}

def pogo = new POGO()
pogo.metaClass.setAttribute(pogo, 'field', 'ha')
pogo.metaClass.setAttribute(pogo, 'property1', 'ho')

assert pogo.field == 'ha'
assert pogo.property1 == 'ho'
// end::groovy_set_attribute[]
'''
    }
}