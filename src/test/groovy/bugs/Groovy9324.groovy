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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy9324 {

    @Test
    void testAstStringCompilerCompile() {
        assertScript '''
            import org.codehaus.groovy.ast.builder.AstStringCompiler
            import org.codehaus.groovy.ast.builder.AstAssert
            import static org.codehaus.groovy.ast.tools.GeneralUtils.block
            import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
            import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt

            def actual = new AstStringCompiler().compile(""" "Hello World" """)
            def expected = [
                    block(
                            stmt(
                                    constX("Hello World")
                            )
                    )
            ]
            AstAssert.assertSyntaxTree(expected, actual)
        '''
    }

}
