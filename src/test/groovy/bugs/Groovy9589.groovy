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

import org.apache.groovy.parser.antlr4.TestUtils
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.Test

final class Groovy9589 {
    @Test
    void testParallelParse() {
        final cnt = 2
        def sources = (1..cnt).inject([:]) { r, e ->  r["hello${e}.groovy"] = source(e); r }
        def ast1 = parse(sources, false)
        def ast2 = parse(sources, true)

        assert cnt == ast1.modules.size()
        assert ast1.modules.size() == ast2.modules.size()

        out:
        for (ModuleNode m1 in ast1.modules) {
            for (ModuleNode m2 in ast2.modules) {
                if (TestUtils.compareAST(m1, m2)) continue out
            }

            assert false
        }
    }

    private parse(Map sources, boolean parallelParse) {
        new CompilationUnit(new CompilerConfiguration(optimizationOptions: [parallelParse: parallelParse])).tap {
            sources.each { k, v ->
                addSource k, v
            }
            compile Phases.CONVERSION
        }.ast
    }


    private static String source(int index) {
        """
class Hello${index} {
    def m() {
        println 'Hello'
    }
}
        """
    }
}
