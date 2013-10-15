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

import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * Support for more complex commands.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
abstract class ComplexCommandSupport
    extends CommandSupport
{
    protected List<String> functions
    
    protected String defaultFunction
    
    ComplexCommandSupport(final Groovysh shell, final String name, final String shortcut, List<String> comFunctions) {
        this(shell, name, shortcut, comFunctions, null)
    }

    ComplexCommandSupport(final Groovysh shell, final String name, final String shortcut, List<String> comFunctions,
                          String defaultFunction) {
        super(shell, name, shortcut)
        this.functions = comFunctions
        this.defaultFunction = defaultFunction
        assert(defaultFunction  == null || defaultFunction in functions)
    }

    protected List createCompleters() {
        def c = new SimpleCompletor()
        
        getFunctions().each { String it -> c.add(it) }
        
        return [ c, null ]
    }

    List<String> getFunctions() {
        return functions
    }
    
    Object execute(List<String> args) {
        assert args != null
        
        if (args.size() == 0) {
            if (defaultFunction) {
                args = [ defaultFunction ]
            } else {
                fail("Command '$name' requires at least one argument of ${getFunctions()}")
            }
        }
        
        return executeFunction(args[0], args.tail())
    }
    
    protected executeFunction(String fname, List<String> args) {
        assert args != null

        List<String> myFunctions = getFunctions()
        assert myFunctions

        if (fname in myFunctions) {
            def func = loadFunction(fname)
            
            log.debug("Invoking function '$fname' w/args: $args")
            
            return func.call(args)
        } else {
            fail("Unknown function name: '$fname'. Valid arguments: $myFunctions")
        }
    }
    
    protected Closure loadFunction(final String name) {
        assert name
        
        try {
            return this."do_${name}"
        } catch (MissingFieldException e) {
            fail("Failed to load delegate function: $e")
        }
    }
    
    def do_all = {
        getFunctions().findAll {it != 'all'}.collect {executeFunction(it, [])}
    }
}

