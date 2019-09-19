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
import org.codehaus.groovy.runtime.InvokerHelper

class ConstructorBug extends GroovyTestCase {
    
    void testBug() {
        def type = new GroovyClassLoader().parseClass(new File("src/test/groovy/bugs/TestBase.groovy"))
        assert type != null

        type = new GroovyClassLoader().parseClass(new File("src/test/groovy/bugs/TestDerived.groovy"))
        assert type != null

        def mytest = InvokerHelper.invokeConstructorOf(type, ["Hello"] as Object[])
        assert mytest.foo == "Hello"
        /** @todo fix bug
        */
        
        /*
        def test = type.newInstance()
        assert test.foo == null
        */
        
//foo = new type('hello')
        /*
        */
        mytest = new TestDerived("Hello")
        assert mytest.foo == "Hello"
    }
}