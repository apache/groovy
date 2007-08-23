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

package org.codehaus.groovy.tools.shell.commands

import org.codehaus.groovy.runtime.MethodClosure

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Shell

import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * The 'show' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ShowCommand
    extends CommandSupport
{
    //
    // TODO: Create helper class to handle commands which have sub-context like show and purge
    //
    
    private static final List TYPES = [ 'variables', 'classes', 'imports' ]
    
    ShowCommand(final Shell shell) {
        super(shell, 'show', '\\S')
    }
    
    protected List createCompletors() {
        def c = new SimpleCompletor()
        
        TYPES.each { c.add(it) }
        c.add('all')
        
        return [ c, null ]
    }
    
    Object execute(final List args) {
        assert args != null
        
        if (args.size() != 1) {
            fail("Command 'show' requires an argument") // TODO: i18n
        }
        
        args.each {
            show(it)
        }
    }
    
    private void show(final String type) {
        assert type
        
        switch (type) {
            case 'variables':
                if (variables.isEmpty()) {
                    io.out.println('No variables defined') // TODO: i18n
                }
                else {
                    io.out.println('Variables:') // TODO: i18n
                    
                    variables.each { key, value ->
                        // Special handling for defined methods, just show the sig
                        if (value instanceof MethodClosure) {
                            //
                            // TODO: Would be nice to show the argument types it will accept...
                            //
                            value = "method ${value.method}()"
                        }
                        
                        io.out.println("  $key = $value")
                    }
                }
                break
                
            case 'classes':
                def classes = classLoader.loadedClasses
                
                if (classes.size() == 0) {
                    io.out.println("No classes have been loaded") // TODO: i18n
                }
                else {
                    io.out.println('Classes:') // TODO: i18n
                    
                    classes.each {
                        io.out.println("  $it")
                    }
                }
                break
            
            case 'imports':
                if (imports.isEmpty()) {
                    io.out.println("No custom imports have been defined") // TODO: i18n
                }
                else {
                    io.out.println("Custom imports:") // TODO: i18n
                    
                    imports.each {
                        io.out.println("  $it")
                    }
                }
                break
            
            case 'all':
                TYPES.each { show(it) }
                break
                
            default:
                fail("Unknown show type: $type") // TODO: i18n
        }
    }
}

