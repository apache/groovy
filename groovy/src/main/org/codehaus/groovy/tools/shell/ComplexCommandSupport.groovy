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
    protected List/*<String>*/ functions
    
    protected String defaultFunction
    
    ComplexCommandSupport(final Shell shell, final String name, final String shortcut) {
        super(shell, name, shortcut)
    }
    
    protected List createCompletors() {
        def c = new SimpleCompletor()
        
        functions.each { c.add(it) }
        
        return [ c, null ]
    }
    
    Object execute(List args) {
        assert args != null
        
        if (args.size() == 0) {
            if (defaultFunction) {
                args = [ defaultFunction ]
            }
            else {
                fail("Command '$name' requires at least one argument")
            }
        }
        
        return executeFunction(args)
    }
    
    protected executeFunction(List args) {
        assert args != null
        
        assert functions
        
        def fname = args[0]
        
        if (args.size() > 1) {
            args = args[1..-1]
        }
        else {
            args = []
        }
        
        if (fname in functions) {
            def func = loadFunction(fname)
            
            log.debug("Invoking function '$fname' w/args: $args")
            
            return func.call(args)
        }
        else {
            fail("Unknown function name: $fname")
        }
    }
    
    protected Closure loadFunction(final String name) {
        assert name
        
        try {
            return this."do_${name}"
        }
        catch (MissingPropertyException e) {
            fail("Failed to load delgate function: $e")
        }
    }
    
    def do_all = {
        functions.each { fname ->
            if (fname != 'all') {
                executeFunction([ fname ])
            }
        }
    }
}

