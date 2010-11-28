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

import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Shell

/**
 * The 'record' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class RecordCommand
    extends ComplexCommandSupport
{
    RecordCommand(final Shell shell) {
        super(shell, 'record', '\\r')

        this.functions = [ 'start', 'stop', 'status' ]

        this.defaultFunction = 'status'

        addShutdownHook {
            if (recording) {
                do_stop()
            }
        }
    }

    private File file

    private PrintWriter writer

    boolean isRecording() {
        return file != null
    }

    def recordInput(final String line) {
        assert line != null

        if (recording) {
            writer.println(line)
            writer.flush()
        }
    }

    def recordResult(final Object result) {
        // result maybe null

        if (recording) {
            // Using String.valueOf() to prevent crazy exceptions
            writer.println("// RESULT: ${String.valueOf(result)}")
            writer.flush()
        }
    }

    def recordError(final Throwable cause) {
        assert cause != null

        if (recording) {
            writer.println("// ERROR: $cause")

            cause.stackTrace.each {
                writer.println("//    $it")
            }

            writer.flush()
        }
    }

    def do_start = { args ->
        if (recording) {
            fail("Already recording to: $file")
        }

        if (args.size() != 1) {
            file = File.createTempFile('groovysh-', '.txt')
        }
        else {
            file = new File(args[0] as String)
        }

        if(file.parentFile) file.parentFile.mkdirs()

        writer = file.newPrintWriter()
        writer.println("// OPENED: " + new Date())
        writer.flush()
        
        io.out.println("Recording session to: $file")

        return file
    }

    def do_stop = {
        if (!recording) {
            fail("Not recording")
        }

        writer.println("// CLOSED: " + new Date())
        writer.flush()

        writer.close()
        writer = null

        io.out.println("Recording stopped; session saved as: $file (${file.length()} bytes)")

        def tmp = file
        file = null

        return tmp
    }

    def do_status = {
        if (!recording) {
            io.out.println("Not recording")

            return null
        }

        io.out.println("Recording to file: $file (${file.length()} bytes)")

        return file
    }
}

