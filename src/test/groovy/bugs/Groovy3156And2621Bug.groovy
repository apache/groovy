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

class Groovy3156And2621Bug extends GroovyTestCase {
    void testMethodNameResolutionInANestedClosure() {
        assert m() == 'method'
        assert c1() == 'method'
    }

    void testSimilarNamesForMethodAndLocalWithLocalAsMethodArgument() {
        failingExecute()
    }

    def m = { return 'method' }
    def c1 = {
        def m = { return 'c1' }
        def c2 = {
            /*
            *  If both 'm()' and 'this.m()' are used as follows,
            *  'this.m()' should not resolve to c1 closure's 'm' local variable.
            *  It should resolve to outermost class' m().
            */
            assert m() == 'c1'
            return this.m()
        }
        return c2()
    }

    void convention(String arg) {
    }
    
    void failingExecute() {
        def convention= 'value'
        1.times {
            this.convention(convention)
        }
    }
}
