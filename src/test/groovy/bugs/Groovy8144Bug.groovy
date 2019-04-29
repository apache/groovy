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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit

/**
 * This test requires the test classes executed are compiled and on the
 * classpath and not in the same compilation unit.
 */
class Groovy8144Bug extends AbstractBytecodeTestCase {

    GroovyShell shell

    protected Map<String, Boolean> getOptions() {
        ['asmResolving': Boolean.TRUE]
    }

    @Override
    void setUp() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.putAll(getOptions())
        shell = new GroovyShell(config)
    }

    void testMethodInheritedFromNonPublicAbstractBaseClass() {
        checkAnswer('Groovy8144A')
    }

    void testMethodInheritedFromPublicAbstractBaseClass() {
        checkAnswer('Groovy8144B')
    }

    void testMethodInheritedFromPublicBaseClass() {
        checkAnswer('Groovy8144C')
    }

    void checkAnswer(String testClassName) {
        String code = """
            import org.codehaus.groovy.dummy.${testClassName}

            @groovy.transform.CompileStatic
            def m() {
                new ${testClassName}().answer()
            }

            assert m() == 42
        """

        def action = { SourceUnit unit ->
            def config = unit.getConfiguration()
            if (config.is(CompilerConfiguration.DEFAULT)) {
                config = new CompilerConfiguration(config)
            }
            config.optimizationOptions.putAll(getOptions())
        }

        assert compile([conversionAction:action, method:'m'], code).hasSequence([
                "INVOKEVIRTUAL org/codehaus/groovy/dummy/${testClassName}.answer ()I"
        ])

        shell.evaluate(code)
    }
}

class Groovy8144BugAsmResolveOff extends Groovy8144Bug {
    @Override
    protected Map<String, Boolean> getOptions() {
        ['asmResolving': Boolean.FALSE]
    }
}
