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
import org.junit.jupiter.api.Test

final class Groovy12752 extends AbstractBytecodeTestCase {

    @Test
    void testScriptBodyLineNumbers() {
        def bytecode = compile '''\
            boolean flag = false
            if (flag) {
                println('true path')
            }
        '''

        assert bytecode.hasSequence([
            'L0',
            'LINENUMBER 1 L0',
            'ICONST_0',
            'ISTORE 1', // flag
            'L1',
            'LINENUMBER 2 L1',
            'ILOAD 1', // flag
            'IFEQ L2',
            'L3',
            'LINENUMBER 3 L3',
            // println('true path')
            'ARETURN',
            'L2',
            'LINENUMBER 4 L2', // need this for step over then block
            'ACONST_NULL',
            'ARETURN'
        ])
    }
}
