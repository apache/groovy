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

class MethodDispatchBug extends GroovyTestCase {
    def doit(Object parameter1, Object parameter2) {
        "OO"
    }

    def doit(Boolean parameter1, Object parameter2) {
        "BO"
    }

    def doit(Object parameter1, Boolean parameter2) {
        "OB"
    }

    def doit(Boolean parameter1, Boolean parameter2) {
        "BB"
    }

    def testBug() {
        def o = this;

        assert "BB" == o.doit(true, true);
        assert "BO" == o.doit(true, 9);
        assert "OB" == o.doit(9, true);
        assert "OO" == o.doit(9, 9);
    }
    
    def methodWithDefaults(a,b,c=1000) {
      a+b+c
    }
    
    void testListExpansion() {
       // there was a bug discovered while looking at GROOVY-1803
       // a list expansion was cached like 
       // methodWithDefaults(List) -> methodWithDefaults(Object,Object,Object)
       // but this cached version can't handle lists with an arbitrary length 
       // of parameters, resulting in the second call here to fail
       assert methodWithDefaults([1,10,100]) == 111
       assert methodWithDefaults([1,10]) == 1011    
    }    
}