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

import jline.FileNameCompletor

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Shell

/**
 * The 'save' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class SaveCommand
    extends CommandSupport
{
    SaveCommand(final Shell shell) {
        super(shell, 'save', '\\s')
    }

    protected List createCompletors() {
        return [
            new FileNameCompletor(),
            null
        ]
    }

    Object execute(final List args) {
        assert args != null
        
        if (args.size() != 1) {
            fail("Command 'save' requires a single file argument") // TODO: i18n
        }

        if (buffer.isEmpty()) {
            io.out.println('Buffer is empty') // TODO: i18n
            return
        }

        //
        // TODO: Support special '-' file to simply dump text to io.out
        //
        
        def file = new File("${args[0]}")

        if (io.verbose) {
            io.out.println("Saving current buffer to file: $file") // TODO: i18n
        }

        def dir = file.parentFile
        if (dir && !dir.exists()) {
            log.debug("Creating parent directory path: $dir")
            
            dir.mkdirs()
        }
        
        file.write(buffer.join(NEWLINE))
    }
}
