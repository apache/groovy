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

package org.codehaus.groovy.tools.shell

import jline.ConsoleReader
import jline.MultiCompletor
import jline.History

/**
 * Support for running a {@link Shell} interactivly using the JLine library.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class InteractiveShellRunner
    extends ShellRunner
    implements Runnable
{
    final ConsoleReader reader
    
    final Closure prompt
    
    InteractiveShellRunner(final Shell shell, final Closure prompt) {
        super(shell)
        
        this.prompt = prompt
        
        //
        // NOTE: By pass shell.io.out, its now doing fancy ANSI stuffs...
        //
        
        this.reader = new ConsoleReader(shell.io.inputStream, new PrintWriter(shell.io.outputStream))
        
        // Setup the history file if we can
        def file = new File(shell.userStateDirectory, 'groovysh_history')
        if (file.parentFile.exists()) {
            log.debug("Using history file: $file")
            reader.history.historyFile = file
        }
        
        //
        // TODO: Maybe hook up a reader.debug PrintWriter to help see what its doing?
        //
        
        // Setup the completors
        def completors = []
        
        //
        // TODO: See if we want to add any more language specific completions, like for println for example?
        //
        //       Probably want to have the Groovysh instance install them
        //
        
        for (command in shell.registry) {
            def tmp = command.completor
            
            if (tmp) {
                completors << tmp
            }
        }
        
        reader.addCompletor(new MultiCompletor(completors))
    }
    
    protected String readLine() {
        return reader.readLine(prompt.call())
    }
}

