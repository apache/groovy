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
package groovy

import groovy.test.GroovyTestCase

/** 
 * Tests the use of classes as variable expressions
 */
class ClassExpressionTest extends GroovyTestCase {

    void testUseOfClass() {
        def x = String
        
        assert x != null

        assert x.getName().endsWith('String')
        assert x.name.endsWith('String')

        x = Integer
        
        assert x != null
        assert x.name.endsWith('Integer')
        
        x = GroovyTestCase
        
        assert x != null
        assert x.name.endsWith('GroovyTestCase')
        
        x = ClassExpressionTest
        
        assert x != null
    }

    void testClassPsuedoProperty() {

        def x = "cheese";

        assert x.class != null

        assert x.class == x.getClass();
    }
    
    void testPrimitiveClasses() {
        assert void == Void.TYPE
        assert int == Integer.TYPE
        assert byte == Byte.TYPE
        assert char == Character.TYPE
        assert double == Double.TYPE
        assert float == Float.TYPE
        assert long == Long.TYPE
        assert short == Short.TYPE
    }
    
    void testArrayClassReference() {
       def foo = int[]
       assert foo.name == "[I"
    }
}
