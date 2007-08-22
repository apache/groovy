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

import jline.Completor
import jline.ArgumentCompletor
import jline.NullCompletor

import org.codehaus.groovy.control.CompilationFailedException

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.InteractiveShell
import org.codehaus.groovy.tools.shell.BufferManager

import org.codehaus.groovy.tools.shell.completor.SimpleCompletor

/**
 * The 'buffer' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class BufferCommand
    extends CommandSupport
{
    BufferCommand(final InteractiveShell shell) {
        super(shell, 'buffer', '\\b')
    }

    protected List createCompletors() {
        return [ new BufferCommandCompletor(shell.buffers), null ]
    }
    
    void execute(final List args) {
        assert args != null

        if (args.size() == 0) {
            io.output.println("Current selected buffer: ${buffers.selected}") // TODO: i18n
            return
        }

        if (args.size() != 1) {
            io.error.println("Command 'buffer' requires a single argument") // TODO: i18n
            return
        }
        
        def buffers = shell.buffers
        
        switch (args[0]) {
            case '+':
                // Create a new buffer
                buffers.create(true)
                break

            case '-':
                // Delete the current buffer
                if (buffers.size() == 1) {
                    io.error.println('Can not delete the last buffer') // TODO: i18n
                }
                else {
                    buffers.deleteSelected()
                }
                break

            case '?':
                // Display information about the buffers
                io.output.println("Total buffers: ${buffers.size()}")
                break

            default:
                // Select a buffer
                def i = Integer.parseInt(args[0])
                
                if (i < 0 || i >= buffers.size()) {
                    io.error.println("Invalid buffer selection: $i") // TODO: i18n
                }
                else {
                    buffers.select(i)
                }
                break
        }
    }
}

/**
 * Completor for the 'buffer' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class BufferCommandCompletor
    extends SimpleCompletor
{
    private final BufferManager buffers
    
    BufferCommandCompletor(final BufferManager buffers) {
        assert buffers

        this.buffers = buffers
    }

    SortedSet getCandidates() {
        def set = new TreeSet()
        set << '+'
        set << '-'
        set << '?'

        for (i in 0..<buffers.size()) {
            set << i.toString()
        }

        return set
    }
}
