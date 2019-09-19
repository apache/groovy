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

class ImportNodeLineNumberTest extends GroovyTestCase {
    void testLineNumberOfImports() {
        assertScript '''import groovy.transform.ASTTest
        import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
        import groovy.transform.*
        import static java.lang.Math.*

        @ASTTest(phase=SEMANTIC_ANALYSIS, value={
            def module = node.declaringClass.module
            def astTestImport = module.getImport('ASTTest')
            assert astTestImport.lineNumber == 1
            def staticImport = module.staticImports['SEMANTIC_ANALYSIS']
            assert staticImport.lineNumber == 2
            def starImport = module.starImports[0]
            assert starImport.lineNumber == 3
            def staticStar = module.staticStarImports['java.lang.Math']
            assert staticStar.lineNumber == 4
        })
        void foo() {}

        foo()
        '''
    }
}
