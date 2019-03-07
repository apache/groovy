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

class GuillaumesMapBug extends GroovyTestCase {
    
    void testBug2() {
        def list = [1, 2, 3]
        def map = [:]
        
        doLoop(list, map)
    
        assert map[0] == 1 
        assert map[1] == 2 
        assert map[2] == 3 
    }
    
    void doLoop(list, map) {
        def i = 0
        for (it in list) {
            map[i++] = it
        }
    }
    
    
    void testBug() {
        def list = [1, 2, 3]
        def map = [:]
        doClosureLoop(list, map)
        
        assert map[0] == 1 
        assert map[1] == 2 
        assert map[2] == 3 
    }
    
    void doClosureLoop(list, map) {
        def i = 0
        list.each { map[i++] = it }
    }    
}