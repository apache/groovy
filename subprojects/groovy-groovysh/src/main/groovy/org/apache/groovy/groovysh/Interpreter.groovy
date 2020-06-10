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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.tools.shell.util.Logger

import java.lang.reflect.Method

/**
 * Helper to interpret a source buffer.
 */
class Interpreter implements Evaluator
{
    static final String SCRIPT_FILENAME = 'groovysh_evaluate'

    private final Logger log = Logger.create(this.class)

    private final GroovyShell shell

    Interpreter(final ClassLoader classLoader, final Binding binding) {
        this(classLoader, binding, null)
    }

    Interpreter(final ClassLoader classLoader, final Binding binding, CompilerConfiguration configuration) {
        assert classLoader
        assert binding
        if (configuration != null) {
            shell = new GroovyShell(classLoader, binding, configuration)
        } else {
            shell = new GroovyShell(classLoader, binding)
        }
    }

    Binding getContext() {
        // GROOVY-9584: leave as call to getter not property access to avoid potential context variable in binding
        return shell.getContext()
    }

    GroovyClassLoader getClassLoader() {
        return shell.classLoader
    }

    GroovyShell getShell() {
        return shell
    }

    @Override
    def evaluate(final Collection<String> buffer) {
        assert buffer

        def source = buffer.join(Parser.NEWLINE)

        def result

        Class type
        try {
            Script script = shell.parse(source, SCRIPT_FILENAME)
            type = script.getClass()

            log.debug("Compiled script: $script")

            if (type.declaredMethods.any {Method it -> it.name == 'main' }) {
                result = script.run()
            }

            // Need to use String.valueOf() here to avoid icky exceptions causes by GString coercion
            log.debug("Evaluation result: ${InvokerHelper.toString(result)} (${result?.getClass()})")

            // Keep only the methods that have been defined in the script
            type.declaredMethods.each { Method m ->
                if (!(m.name in [ 'main', 'run' ] || m.name.startsWith('super$') || m.name.startsWith('class$') || m.name.startsWith('$'))) {
                    log.debug("Saving method definition: $m.name")

                    context["${m.name}"] = new MethodClosure(type.newInstance(), m.name)
                }
            }
        }
        finally {
            // Remove the script class generated
            if (type?.name) {
                classLoader.removeClassCacheEntry(type?.name)
            }

            // Remove the inline closures from the cache as well
            classLoader.removeClassCacheEntry('$_run_closure')
        }

        return result
    }
}

interface Evaluator {
    def evaluate(final Collection<String> buffer)
}
