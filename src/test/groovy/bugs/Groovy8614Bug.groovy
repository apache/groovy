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

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

/**
 * see Groovy3830Bug for tests when call sites are present
 */
class Groovy8614Bug extends AbstractBytecodeTestCase {

    void testNestedInterfaceHelperClassNotGeneratedIfNoCallsites() {
        def bytecode = compile('classNamePattern': 'X', '''
            class X {
                interface Y {}
            }
        ''')
        assert !bytecode.hasSequence(['static synthetic INNERCLASS X$Y$1 X 1'])
    }

    void testDoubleNestedInterfaceHelperClassNotGeneratedIfNoCallsites() {
        def bytecode = compile('classNamePattern': 'X\\$Y', '''
            class X {
                class Y {
                    interface Z {}
                }
            }
        ''')
        assert !bytecode.hasSequence(['static synthetic INNERCLASS X$Y$Z$1 X$Y 1'])
    }

    //GROOVY-5082
    void testInterfaceHelperClassNotGeneratedIfNoCallsites() {
        def bytecode = compile('classNamePattern': 'X', '''
            interface X {
              public String compute();
            }
        ''')
        assert !bytecode.hasSequence(['static synthetic INNERCLASS X$1 X 1'])
    }

}
