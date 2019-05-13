/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.groovysh.commands

import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Groovysh
import org.codehaus.groovy.tools.shell.util.Preferences

/**
 * The 'edit' command. Opens Editor to write into the current Buffer.
 */
class EditCommand
    extends CommandSupport
{
    public static final String COMMAND_NAME = ':edit'

    EditCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':e')
    }

    ProcessBuilder getEditorProcessBuilder(final String editCommand, final String tempFilename) {
        def pb = new ProcessBuilder(editCommand, tempFilename)

        // GROOVY-6201: Editor should inherit I/O from the current process.
        //    Fixed only for java >= 1.7 using new ProcessBuilder api
        pb.redirectErrorStream(true)
        def javaVer = Double.valueOf(System.getProperty('java.specification.version'))
        if (javaVer >= 1.7) {
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT)
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        }

        return pb
    }

    private String getEditorCommand() {
        def editor = Preferences.editor

        log.debug("Using editor: $editor")

        if (!editor) {
            fail("Unable to determine which editor to use; check \$EDITOR") // TODO: i18n
        }

        return editor
    }

    @Override
    Object execute(final List<String> args) {
        assertNoArguments(args)

        File file = File.createTempFile('groovysh-buffer', '.groovy')
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

            replaceCurrentBuffer(file.readLines())
        }
        finally {
            file.delete()
        }
    }

    void replaceCurrentBuffer(List<String> contents) {
        // clear current buffer contents
        shell.buffers.clearSelected()

        // load editor contents into current buffer
        for (String line : contents) {
            shell.execute(line)
        }
    }

}
