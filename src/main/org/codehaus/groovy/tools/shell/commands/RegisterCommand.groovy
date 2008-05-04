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
import org.codehaus.groovy.tools.shell.Command

/**
 * The 'register' command.
 *
 * @version $Id$
 * @author <a href="mailto:chris@wensel.net">Chris K Wensel</a>
 */
class RegisterCommand
    extends CommandSupport
{
    RegisterCommand(final Shell shell) {
        super(shell, "register", "\\rc")
    }

    public Object execute(List args) {
        assert args != null

        if (args.size() < 1) {
            fail("Command 'register' requires at least 1 arguments") // TODO: i18n
        }

        String classname = args.get(0)

        Class type = getClassLoader().loadClass(classname)

        Command command = null;

        if (args.size() == 1) {                   // use default name
            command = type.newInstance(shell)
        }
        else if (args.size() == 2) {              // pass name to ctor
            command = type.newInstance(shell, args.get(1), null)
        }
        else if (args.size() == 3) {              // pass name, alias to ctor
            command = type.newInstance(shell, args.get(1), args.get(2))
        }

        def oldcommand = registry[command.name]   // let's prevent collisions

        if (oldcommand) {
            fail("Can not rebind command: ${command.name}") // TODO: i18n
        }

        if (log.debugEnabled) {
            log.debug("Created command '${command.name}': $command")
        }

        command = shell << command

        if (shell.runner) {
            shell.runner.completor << command
        }
    }
}