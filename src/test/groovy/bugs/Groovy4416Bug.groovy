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

class Groovy4416Bug extends GroovyTestCase {
    void testImplicitThisPassingInNonStaticInnerClassesBug() {
        assertScript """
            class Dummy4416 {
               static final PROPERTY_VALUE = "property_value"
               def foo(){"foo()"}
               class Bean {
                   String property1 = PROPERTY_VALUE
                   String property2 = foo()
               }
               def bean = new Bean()
            }
            def b = new Dummy4416().bean
            
            assert b.property1 == "property_value"
            assert b.property2 == "foo()"
        """
    }
}
