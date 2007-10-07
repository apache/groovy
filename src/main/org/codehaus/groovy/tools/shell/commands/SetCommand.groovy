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
import org.codehaus.groovy.tools.shell.util.Preferences

/**
 * The 'set' command, used to set preferences.
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

    protected List createCompletors() {
        def loader = {
            def list = []

            def keys = Preferences.keys()

            keys.each { list << it }

            return list
        }

        return [
            new SimpleCompletor(loader),
            null
        ]
    }

    Object execute(final List args) {
        assert args != null
        
        if (args.size() == 0) {
            def keys = Preferences.keys()
            
            if (keys.size() == 0) {
                io.out.println('No preferences are set')
                return
            }
            else {
                io.out.println('Preferences:')
                keys.each {
                    def value = Preferences.get(it, null)
                    println("    $it=$value")
                }
            }
            return
        }
        
        if (args.size() > 2) {
            fail("Command '$name' requires arguments: <name> [<value>]")
        }
        
        def name = args[0]
        def value
        
        if (args.size() == 1) {
            value = true
        }
        else {
            value = args[1]
        }
        
        log.debug("Setting preference: $name=$value")
        
        Preferences.put(name, String.valueOf(value))
    }
}
