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
import org.codehaus.groovy.tools.shell.util.Preferences

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
    
    ProcessBuilder getEditorProcessBuilder(String editCommand, String tempFilename) {
        def pb = new ProcessBuilder(editCommand, tempFilename)

        // GROOVY-6201: Editor should inherit I/O from the current process.
        // Fixed only for java >= 1.7 using new ProcessBuilder api
        pb.redirectErrorStream(true);
        def javaVer = Double.valueOf(System.getProperty("java.specification.version"));
        if (javaVer >= 1.7) {
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT)
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        }

        return pb
    }
    
    private String getEditorCommand() {
        def editor = Preferences.editor;

        log.debug("Using editor: $editor")

        if (!editor) {
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
            
            //Try to launch the editor.
            log.debug("Executing: $editorCommand $file")
            def pb = getEditorProcessBuilder("$editorCommand", "$file")
            def p = pb.start()
            
            // Wait for it to finish
            log.debug("Waiting for process: $p")
            p.waitFor()

            log.debug("Editor contents: ${file.text}")
            
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
