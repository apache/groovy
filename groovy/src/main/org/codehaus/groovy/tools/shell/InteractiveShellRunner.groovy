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
import jline.Completor
import jline.MultiCompletor

import org.codehaus.groovy.tools.shell.util.Logger

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
    
    final CommandsMultiCompletor completor
    
    InteractiveShellRunner(final Shell shell, final Closure prompt) {
        super(shell)
        
        this.prompt = prompt
        
        this.reader = new ConsoleReader(shell.io.inputStream, new PrintWriter(shell.io.outputStream, true))
        
        this.completor = new CommandsMultiCompletor()
        
        reader.addCompletor(completor)
    }
    
    void run() {
        for (command in shell.registry) {
            completor << command
        }

        // Force things to become clean
        completor.refresh()

        // And then actually run
        super.run()
    }
    
    void setHistory(final History history) {
        reader.history = history
    }
    
    void setHistoryFile(final File file) {
        def dir = file.parentFile
        
        if (!dir.exists()) {
            dir.mkdirs()
            
            log.debug("Created base directory for history file: $dir")
        }
        
        log.debug("Using history file: $file")
        
        reader.history.historyFile = file
    }
    
    protected String readLine() {
        try {
            return reader.readLine(prompt.call())
        }
        catch (StringIndexOutOfBoundsException e) {
            log.debug("HACK: Try and work around GROOVY-2152 for now", e)

            return "";
        }
    }
}

/**
 * Completor for interactive shells.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class CommandsMultiCompletor
    extends MultiCompletor
{
    protected final Logger log = Logger.create(this.class)
    
    List/*<Completor>*/ list = []
    
    private boolean dirty = false
    
    def leftShift(final Command command) {
        assert command
        
        //
        // FIXME: Need to handle completor removal when things like aliases are rebound
        //
        
        def c = command.completor
        
        if (c) {
            list << c
            
            log.debug("Added completor[${list.size()}] for command: $command.name")
            
            dirty = true
        }
    }

    void refresh() {
        log.debug("Refreshing the completor list")

        completors = list as Completor[]
        dirty = false
    }

    int complete(final String buffer, final int pos, final List cand) {
        assert buffer != null
        
        //
        // FIXME: This is a bit of a hack, I'm too lazy to rewrite a more efficent
        //        completor impl that is more dynamic than the jline.MultiCompletor version
        //        so just re-use it and reset the list as needed
        //

        if (dirty) {
            refresh()
        }
        
        return super.complete(buffer, pos, cand)
    }
}
