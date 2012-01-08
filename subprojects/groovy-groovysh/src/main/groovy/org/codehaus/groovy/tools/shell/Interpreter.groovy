/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.runtime.MethodClosure
import java.lang.reflect.Method

/**
 * Helper to interpret a source buffer.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Interpreter
{
    static final String SCRIPT_FILENAME = 'groovysh_evaluate'
    
    private final Logger log = Logger.create(this.class)

    private final GroovyShell shell

    Interpreter(final ClassLoader classLoader, final Binding binding) {
        assert classLoader
        assert binding

        shell = new GroovyShell(classLoader, binding)
    }

    Binding getContext() {
        return shell.getContext()
    }

    GroovyClassLoader getClassLoader() {
        return shell.getClassLoader()
    }

    def evaluate(final List buffer) {
        assert buffer

        def source = buffer.join(Parser.NEWLINE)

        def result

        Class type
        try {
            Script script = shell.parse(source, SCRIPT_FILENAME)
            type = script.getClass()

            log.debug("Compiled script: $script")

            if (type.declaredMethods.any { it.name == 'main' }) {
                result = script.run()
            }

            // Need to use String.valueOf() here to avoid icky exceptions causes by GString coercion
            log.debug("Evaluation result: ${String.valueOf(result)} (${result?.getClass()})")

            // Keep only the methods that have been defined in the script
            type.declaredMethods.each { Method m ->
                if (!(m.name in [ 'main', 'run' ] || m.name.startsWith('super$') || m.name.startsWith('class$') || m.name.startsWith('$'))) {
                    log.debug("Saving method definition: $m")

                    context["${m.name}"] = new MethodClosure(type.newInstance(), m.name)
                }
            }
        }
        finally {
            def cache = classLoader.classCache

            // Remove the script class generated
            cache.remove(type?.name)

            // Remove the inline closures from the cache as well
            cache.remove('$_run_closure')
        }

        return result
    }
}