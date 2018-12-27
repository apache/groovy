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
package org.codehaus.groovy.tools.shell.commands

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * The 'record' command.
 */
class RecordCommand
    extends ComplexCommandSupport
{
    public static final String COMMAND_NAME = ':record'

    RecordCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':r', [ 'start', 'stop', 'status' ], 'status')

        addShutdownHook {
            if (isRecording()) {
                this.do_stop()
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

        if (isRecording()) {
            writer.println(line)
            writer.flush()
        }
    }

    def recordResult(final Object result) {
        // result maybe null

        if (isRecording()) {
            writer.println("// RESULT: ${InvokerHelper.toString(result)}")
            writer.flush()
        }
    }

    def recordError(final Throwable cause) {
        assert cause != null

        if (isRecording()) {
            writer.println("// ERROR: $cause")

            cause.stackTrace.each {
                writer.println("//    $it")
            }

            writer.flush()
        }
    }

    def do_start = {List<String> args ->
        if (isRecording()) {
            fail("Already recording to: \"$file\"")
        }

        if (args.size() == 0) {
            file = File.createTempFile('groovysh-', '.txt')
        } else if (args.size() == 1) {
            file = new File(args[0] as String)
        } else {
            fail('Too many arguments. Usage: record start [filename]')
        }

        if (file.parentFile) file.parentFile.mkdirs()

        writer = file.newPrintWriter()
        writer.println('// OPENED: ' + new Date())
        writer.flush()

        io.out.println("Recording session to: \"$file\"")

        return file
    }

    def do_stop = {
        if (!isRecording()) {
            fail('Not recording')
        }

        writer.println('// CLOSED: ' + new Date())
        writer.flush()

        writer.close()
        writer = null

        io.out.println("Recording stopped; session saved as: \"$file\" (${file.length()} bytes)")

        def tmp = file
        file = null

        return tmp
    }

    def do_status = {
        if (!isRecording()) {
            io.out.println('Not recording')

            return null
        }

        io.out.println("Recording to file: \"$file\" (${file.length()} bytes)")

        return file
    }
}

