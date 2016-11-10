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

/**
 */
class NestedClosure2Bug extends TestSupport {
     
    Object f
     
    void testFieldBug() {
        def closure = {
            return {
                f = 123
                return null
            }
        }
        def value = closure()
        value = value()
        assert f == 123
    }
     
    void testBugOutsideOfScript() {
        def a = 123
        def b = 456
        def closure = {
            def c = 999
            return {
                f = 2222111
                
                def d = 678
                return { 
                    assert f == 2222111
                    return a
                }
            }
        }
        def c2 = closure()
        def c3 = c2()
        def value = c3()

        assert f == 2222111        
        assert value == 123
    }
    
    void testBug() {
        assertScript """
            def a = 123
            def closure = {
                return {
                    return { 
                        return a
                    }
                }
            }
            def c2 = closure()
            def c3 = c2()
            value = c3()
            
            assert value == 123
"""
    }
}