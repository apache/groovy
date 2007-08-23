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

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Shell

import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * The 'purge' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class PurgeCommand
    extends CommandSupport
{
    private static final List TYPES = [ 'variables', 'classes', 'imports', 'buffers' ]
    
    PurgeCommand(final Shell shell) {
        super(shell, 'purge', '\\p')
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
            fail("Command 'purge' requires an argument") // TODO: i18n
        }
        
        args.each {
            purge(it)
        }
    }
    
    private void purge(final String type) {
        assert type
        
        switch (type) {
            case 'variables':
                if (variables.isEmpty()) {
                    io.out.println('No variables defined') // TODO: i18n
                }
                else {
                    variables.clear()
                    
                    if (io.verbose) {
                        io.out.println("Custom variables purged") // TODO: i18n
                    }
                }
                break
                
            case 'classes':
                if (classLoader.loadedClasses.size() == 0) {
                    io.out.println("No classes have been loaded") // TODO: i18n
                }
                else {
                    classLoader.clearCache()
                    
                    if (io.verbose) {
                        io.out.println('Loaded classes purged') // TODO: i18n
                    }
                }
                break
            
            case 'imports':
                if (imports.isEmpty()) {
                    io.out.println("No custom imports have been defined") // TODO: i18n
                }
                else {
                    imports.clear()
                    
                    if (io.verbose) {
                        io.out.println("Custom imports purged") // TODO: i18n
                    }
                }
                break
                
            case 'buffers':
                buffers.reset()
                
                if (io.verbose) {
                    io.out.println('All buffers purged') // TODO: i18n
                }
                break
            
            case 'all':
                TYPES.each { purge(it) }
                break
                
            default:
                fail("Unknown purge type: $type") // TODO: i18n
        }
    }
}

