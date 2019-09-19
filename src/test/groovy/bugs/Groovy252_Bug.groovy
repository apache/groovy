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
 */
class Groovy252_Bug extends GroovyTestCase {
    
    def count = 0
    
    void testBug() {
        def value = f()
        assert value == null
        
        value = g()
        assert value == null
        
        value = h()
        assert value == null
    }
    
    
    def f() {
         if (count++ == 5)
            return null
         else
            return null
    } 
    
    def g() {
         ++count
         return null
    } 
    
    def h() {
         ++count
         return null
    } 
}
