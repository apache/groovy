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

class ClosureWithStaticVariablesBug extends TestSupport {
    
    static def y = [:]
    
    void testBug() {
        def c = { x ->
            return {
                def foo = Cheese.z
                println foo
                assert foo.size() == 0

                println y
                assert y.size() == 0

                return 6
            }
        }
        def c2 = c(5)
        def answer = c2()
        assert answer == 6
    }
}

class Cheese {
    public static z = [:]
}