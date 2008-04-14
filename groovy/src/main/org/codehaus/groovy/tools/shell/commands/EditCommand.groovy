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
 * The 'edit' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class EditCommand
    extends CommandSupport
{
    EditCommand(final Shell shell) {
        super(shell, 'edit', '\\e')
    }
    
    private String getEditorCommand() {
        def editor = System.getenv('EDITOR')
        
        if (!editor) {
            //
            // TODO: Maybe popup a Swing editor here?  Or look for other env vars?  Or use notepad on winblows?
            //
            
            fail("Unable to determine which editor to use; check \$EDITOR") // TODO: i18n
        }
        
        return editor
    }
    
    Object execute(final List args) {
        assertNoArguments(args)
        
        def file = File.createTempFile('groovysh-buffer', '.groovy')
        file.deleteOnExit()
        
        try {
            // Write the current buffer to a tmp file
            file.write(buffer.join(NEWLINE))
            
            // Try to launch the editor
            def cmd = "$editorCommand $file"
            log.debug("Executing: $cmd")
            def p = cmd.execute()
            
            // Wait for it to finishe
            log.debug("Waiting for process: $p")
            p.waitFor()
            
            // Load the new lines...
            file.eachLine {
                shell << it
            }
        }
        finally {
            file.delete()
        }
    }
}
