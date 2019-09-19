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

class InstanceofTest extends GroovyTestCase {

    void testTrue() {

        def x = false
        def o = 12
        
        if ( o instanceof Integer ) {
            x = true
        }

        assert x == true
    }
    
    void testFalse() {

        def x = false
        def o = 12
        
        if ( o instanceof Double ) {
            x = true
        }

        assert x == false
    }
    
    void testImportedClass() {
        def m = ["xyz":2]
        assert m instanceof Map
        assert !(m instanceof Double)
        
        assertTrue(m instanceof Map)
        assertFalse(m instanceof Double)
    }
    
    void testFullyQualifiedClass() {
        def l = [1, 2, 3]
        assert l instanceof java.util.List
        assert !(l instanceof Map)
        
        assertTrue(l instanceof java.util.List)
        assertFalse(l instanceof Map)
    }
    
    void testBoolean(){
       assert true instanceof Object
       assert true==true instanceof Object
       assert true==false instanceof Object
       assert true==false instanceof Boolean
       assert !new Object() instanceof Boolean
    }
}
