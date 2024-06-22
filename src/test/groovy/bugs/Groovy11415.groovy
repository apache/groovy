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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

import static groovy.test.GroovyAssert.isAtLeastJdk

final class Groovy11415 extends AbstractBytecodeTestCase {
    void testIdentity() {
        assert compile(method: 'isIdentical', '''
            def isIdentical (Object obj) {
               if (obj === 'abc') return true
               return false
            }
        ''').hasSequence([
            'LDC "abc"',
            'IF_ACMPEQ L1',
            'ICONST_0',
            'GOTO L2'
        ])
    }

    void testNotIdentity() {
        assert compile(method: 'isNotIdentical', '''
            def isNotIdentical (Object obj) {
               if (obj !== 'abc') return true
               return false
            }
        ''').hasSequence([
            'LDC "abc"',
            'IF_ACMPNE L1',
            'ICONST_0',
            'GOTO L2'
        ])
    }
}
