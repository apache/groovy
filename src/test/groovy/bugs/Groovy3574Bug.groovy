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

class Groovy3574Bug extends GroovyTestCase {
    void testToStringCallDelegationToConvertedClosureProxy() {
        Closure failing1 = { 
            throw new RuntimeException("Call to this closure fails.") 
        }
        
        Closure failing2 = { a, b ->
            assert a == "a"
            assert b == "b"
            throw new RuntimeException("Call to this closure fails.") 
        }
        
        MyType3574A instance1 = failing1 as MyType3574A

        // test call without args
        try{
            instance1.m()
            fail("The call m() should have failed - 1")
        } catch (ex) {
            // ok, if it failed
        }
        
        // this call was getting delegated to the closure earlier
        assert instance1.toString() != null
        
        // test call with args
        MyType3574B instance2 = failing2 as MyType3574B
        try{
            instance2.m("a", "b")
            fail("The call m() should have failed - 2")
        } catch (ex) {
            // ok, if it failed
        }

        // this call was getting delegated to the closure earlier
        assert instance2.toString() != null
    }
}

interface MyType3574A { def m()}

interface MyType3574B { def m(a, b)}