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
import org.codehaus.groovy.tools.shell.ExitNotification

/**
 * The 'exit' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ExitCommand
    extends CommandSupport
{
    ExitCommand(final Shell shell) {
        super(shell, 'exit', '\\x')

        alias('quit', '\\q')
    }

    Object execute(final List args) {
        assertNoArguments(args)
        
        //
        // TODO: Maybe support a single arg for the code?
        //
        
        if (io.verbose) {
            io.out.println(messages['info.bye'])
        }
        
        throw new ExitNotification(0)
    }
}
