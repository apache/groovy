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
  * Test that ensures that:
  * <ul>
  *   <li>it is possible to write a builder in Groovy</li>
  *   <li>it is possible to call normal methods from the builder,
  *       without the methods being trapped endlessly by createNode()</li>
  * </ul>
  */
class InvokeNormalMethodFromBuilder_Bug657 extends GroovyTestCase {
    void testInvokeNormalMethod() {
        def b = new Builder()
        assert b.callNormalMethod() == "first"

        def value = b.someNode() {}
        assert value == "second"
    }
}

class Builder extends BuilderSupport {

    void setParent(Object parent, Object child) {}

    Object createNode(Object name)                 { return createNode(name, [:], null) }
    Object createNode(Object name, Map attributes) { return createNode(name, attributes, null) }
    Object createNode(Object name, Object value)   { return createNode(name, [:], value) }

    Object createNode(Object name, Map attributes, Object value) {
        return callOtherStaticallyTypedMethod()
    }

    String callNormalMethod()               { return "first" }
    String callOtherStaticallyTypedMethod() { return "second" }
    
}