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

import java.util.prefs.Preferences

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Shell

/**
 * The 'set' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class SetCommand
    extends CommandSupport
{
    SetCommand(final Shell shell) {
        super(shell, 'set', '\\=')
    }
    
    Object execute(final List args) {
        assert args != null
        
        def prefs = Preferences.userNodeForPackage(Shell.class)
        
        if (args.size() == 0) {
            def keys = prefs.keys()
            
            if (keys.size() == 0) {
                io.out.println('No preferences are currently set')
                return
            }
            else {
                io.out.println('Preferences:')
                keys.each {
                    println("    $it=${prefs.get(it, null)}")
                }
            }
        }
        else if (args.size() != 2) {
            fail("Command '$name' requires arguments: <name> <value>")
        }
        else {
            def name = args[0]
            def value = args[1]
            
            log.debug("Setting preference: $name=$value")
            
            prefs.put(name, value)
        }
    }
}
