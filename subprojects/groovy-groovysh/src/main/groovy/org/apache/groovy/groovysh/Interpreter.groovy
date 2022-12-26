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
package org.apache.groovy.groovysh

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.tools.shell.util.Logger

interface Evaluator {
    Object evaluate(Collection<String> strings)
}

/**
 * Helper to interpret a source buffer.
 */
@AutoFinal @CompileStatic
class Interpreter implements Evaluator {

    static final String SCRIPT_FILENAME = 'groovysh_evaluate'

    private final Logger log = Logger.create(getClass())

    private final GroovyShell shell

    Interpreter(ClassLoader classLoader, Binding binding, CompilerConfiguration configuration = null) {
        assert classLoader
        assert binding
        if (configuration != null) {
            shell = new GroovyShell(classLoader, binding, configuration)
        } else {
            shell = new GroovyShell(classLoader, binding)
        }
    }

    GroovyClassLoader getClassLoader() {
        return shell.getClassLoader()
    }

    Binding getContext() {
        // GROOVY-9584: leave as call to getter not property access to avoid potential context variable in binding
        return shell.getContext()
    }

    GroovyShell getShell() {
        return shell
    }

    //--------------------------------------------------------------------------

    @Override @CompileDynamic
    Object evaluate(Collection<String> buffer) {
        assert buffer

        String source = buffer.join(Parser.NEWLINE)

        Object result = null

        Class type = null
        try {
            type = shell.parseClass(new GroovyCodeSource(source, SCRIPT_FILENAME, GroovyShell.DEFAULT_CODE_BASE))
            Script script = InvokerHelper.createScript(type, context)
            log.debug("Compiled script: $script")

            if (type.getDeclaredMethods().any { it.name == 'main' }) {
                result = script.run()
            }

            // Need to use String.valueOf() here to avoid icky exceptions causes by GString coercion
            log.debug("Evaluation result: ${InvokerHelper.toString(result)} (${result?.getClass()})")

            // Keep only the methods that have been defined in the script
            type.getDeclaredMethods().each { m ->
                String name = m.name
                if (!(name == 'main' || name == 'run' || name.startsWith('super$') || name.startsWith('class$') || name.startsWith('$'))) {
                    log.debug("Saving script method definition: $name")
                    context[name] = new MethodClosure(script, name)
                }
            }
        } finally {
            // Remove the generated script class
            if (type?.name) {
                classLoader.removeClassCacheEntry(type.name)
            }

            // Remove the inline closures as well
            classLoader.removeClassCacheEntry('$_run_closure')
        }

        return result
    }
}
