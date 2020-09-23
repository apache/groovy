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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class ImportNodeLineNumberTest {
    @Test
    void testLineNumberOfImports() {
        assertScript '''\
            import groovy.lang.Grab
            import groovy.transform.*
            import static java.lang.Math.*
            import static java.lang.Math.PI as pi

            @ASTTest(phase=SEMANTIC_ANALYSIS, value={
                def moduleNode = sourceUnit.AST

                def importNode = moduleNode.getImport('Grab')
                assert importNode.lineNumber == 1

                importNode = moduleNode.starImports[0]
                assert importNode.lineNumber == 2

                importNode = moduleNode.staticStarImports['java.lang.Math']
                assert importNode.lineNumber == 3

                importNode = moduleNode.staticImports['pi']
                assert importNode.lineNumber == 4
            })
            def var
        '''
    }
}
