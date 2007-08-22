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

/**
 * The 'purgevariables' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class PurgeVariablesCommand
    extends CommandSupport
{
    PurgeVariablesCommand(final Shell shell) {
        super(shell, 'purgevariables', '\\pv')
    }

    Object execute(final List args) {
        assertNoArguments(args)
        
        if (variables.isEmpty()) {
            io.out.println('No variables defined') // TODO: i18n
        }
        else {
            variables.clear()
            
            if (io.verbose) {
                io.out.println("Custom variables purged")
            }
        }
    }
}
