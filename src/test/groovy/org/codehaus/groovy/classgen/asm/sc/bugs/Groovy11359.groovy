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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

final class Groovy11359 extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testMakeCallSite() {
        config.optimizationOptions.put(config.INVOKEDYNAMIC, Boolean.FALSE)

        def err = shouldFail {
            shell.evaluate '''import static org.codehaus.groovy.ast.tools.GeneralUtils.*

                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    // generate `String m() { return new String("") }`
                    node.addMethod("m", 1, STRING_TYPE, params(), null, returnS(ctorX(STRING_TYPE, constX(""))))
                })
                class C { }
            '''
        }
        assert err =~ 'Call site lacked method target for static compilation'
    }
}
