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

import org.codehaus.groovy.classgen.TestSupport

class ClosureParameterPassingBug extends TestSupport {
    
    void testBugInMethod() {
        def c = { x ->
            def y = 123
            def c1 = {
                println y
                println x
                println x[0]
            }

            c1()
        }

        c([1])
    }

    void testBug() {
        assertScript """
def c = { x ->
    def y = 123
    def c1 = { 
        assert x != null , "Could not find a value for x"
        assert y == 123 , "Could not find a value for y"
        println x[0]
    }

    c1()
} 

c([1]) 
"""
    }
   
}