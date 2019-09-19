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
class Bytecode2Bug extends GroovyTestCase {

    Integer count = 0
    
    void testBytecodeBug() {
        getCollection().each { count += it }
    }
    
    void testTedsBytecodeBug() {
        //doTest(getCollection())
        def a = [1, 2, 3, 4]
        doTest(a)

    }
    
    void doTest(args) {
        def m = [:]
        def i = 1
        args.each { m.put(it, i++) }     
        
        assert m[1] == 1
        assert m[2] == 2
        assert m[3] == 3
        assert m[4] == 4

        assert i == 5
    }
    
    
    void testTedsBytecode2Bug() {
        def m = [:]
        def i = 1
        getCollection().each { m.put(it, i++) }     
        
        assert m[1] == 1
        assert m[2] == 2
        assert m[3] == 3
        assert m[4] == 4
        
        assert i == 5
    }
    
    def getCollection() {
        [1, 2, 3, 4]
    }
}