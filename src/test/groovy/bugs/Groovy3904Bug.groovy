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

class Groovy3904Bug extends GroovyTestCase {
    
    void compileAndVerifyCyclicInheritenceCompilationError(script) {
        try {
            new GroovyShell().parse(script)
            fail('The compilation should have failed as it is a cyclic reference')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains('Cyclic inheritance')
        }
    }
    
    void testCyclicInheritenceTC1() {
        compileAndVerifyCyclicInheritenceCompilationError """
            class G3904R1A extends G3904R1A {}
        """ 
    }

    void testCyclicInheritenceTC2() {
        compileAndVerifyCyclicInheritenceCompilationError """
            class G3904R2A extends G3904R2A {
                public static void main(String []argv) {
                  print 'hey'
                }
            }
        """
    }

    /* next 2 tests are similar but in reverse order */
    void testCyclicInheritenceTC3() {
        compileAndVerifyCyclicInheritenceCompilationError """
            class G3904R3A extends G3904R3B {}
            class G3904R3B extends G3904R3A {}
        """
    }

    void testCyclicInheritenceTC4() {
        compileAndVerifyCyclicInheritenceCompilationError """
            class G3904R4B extends G3904R4A {}
            class G3904R4A extends G3904R4B {}
        """
    }

    // cyclic inheritence is between 2 parent classes
    void testCyclicInheritenceTC5() {
        compileAndVerifyCyclicInheritenceCompilationError """
            class G3904R5A extends G3904R5B {}
            class G3904R5B extends G3904R5C {}
            class G3904R5C extends G3904R5B {}
        """
    }

    // cyclic inheritence is between 2 parent classes with a normal level in-between
    void testCyclicInheritenceTC6() {
        compileAndVerifyCyclicInheritenceCompilationError """
            class G3904R6A extends G3904R6B {}
            class G3904R6B extends G3904R6C {}
            class G3904R6C extends G3904R6D {}
            class G3904R6D extends G3904R6B {}
        """
    }
}
