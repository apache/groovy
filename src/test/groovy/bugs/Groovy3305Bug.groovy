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

class Groovy3305Bug extends GroovyTestCase {
    def void testSingleListExpandingToMultipleArgs() {
        assert foo1([1, "A"]) == "1,A"
        assert foo2([BigDecimal.ZERO, "B"]) == "0,B"
        assert foo3([(byte)3, "C"]) == "3,C"
        assert foo4([(float)4, "D"]) == "4.0,D"
        assert foo5([(long)5, "E"]) == "5,E"
        assert foo6([(short)6, "F"]) == "6,F"
    }

    def foo1(int arg0, String arg1) {
        return "$arg0,$arg1" 
    }
    
    def foo2(BigDecimal arg0, String arg1) {
        return "$arg0,$arg1" 
    }
    
    def foo3(byte arg0, String arg1) {
        return "$arg0,$arg1" 
    }
    
    def foo4(float arg0, String arg1) {
        return "$arg0,$arg1" 
    }
    
    def foo5(long arg0, String arg1) {
        return "$arg0,$arg1" 
    }

    def foo6(short arg0, String arg1) {
        return "$arg0,$arg1" 
    }
}