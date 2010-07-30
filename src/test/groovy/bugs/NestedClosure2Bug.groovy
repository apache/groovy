/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.bugs

/**
 * @version $Revision$
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
            println b
            def c = 999
            return {
                f = 2222111
                
                println f
                
                println c
                def d = 678
                return { 
                    println f
                    assert f == 2222111
                    println d
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