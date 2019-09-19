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
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy1465Bug extends GroovyTestCase {
    
    void compileAndVerifyCyclicInheritenceCompilationError(script) {
        try {
            new GroovyShell().parse(script)
            fail('The compilation should have failed as it is a cyclic reference')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains('Cyclic inheritance')
        }
    }
    
    void testInterfaceCyclicInheritenceTC1() {
        compileAndVerifyCyclicInheritenceCompilationError """
            interface G1465Tt extends G1465Tt { }
            def tt = {} as G1465Tt
        """ 
    }

    void testInterfaceCyclicInheritenceTC2() {
        compileAndVerifyCyclicInheritenceCompilationError """
            interface G1465Rr extends G1465Ss { }
            interface G1465Ss extends G1465Rr { }
            def ss = {} as G1465Ss
        """ 
    }

    void testInterfaceCyclicInheritenceTC3() {
        compileAndVerifyCyclicInheritenceCompilationError """
            interface G1465A extends G1465B { }
            interface G1465B extends G1465C { }
            interface G1465C extends G1465B { }
        """ 
    }
}