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

import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Shell

/**
 * The 'show' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ShowCommand
    extends ComplexCommandSupport
{
    ShowCommand(final Shell shell) {
        super(shell, 'show', '\\S')
        
        this.functions = [ 'variables', 'classes', 'imports', 'all' ]
    }
    
    def do_variables = {
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
    }
    
    def do_classes = {
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
    }
    
    def do_imports = {
        if (imports.isEmpty()) {
            io.out.println("No custom imports have been defined") // TODO: i18n
        }
        else {
            io.out.println("Custom imports:") // TODO: i18n
            
            imports.each {
                io.out.println("  $it")
            }
        }
    }
}

