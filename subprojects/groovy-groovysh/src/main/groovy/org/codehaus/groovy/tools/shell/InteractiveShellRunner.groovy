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

import jline.console.ConsoleReader
import jline.console.completer.AggregateCompleter

import jline.console.history.FileHistory
import org.codehaus.groovy.tools.shell.completion.*
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.Preferences
import org.codehaus.groovy.tools.shell.util.WrappedInputStream

/**
 * Support for running a {@link Shell} interactively using the JLine library.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class InteractiveShellRunner
    extends ShellRunner
    implements Runnable
{
    ConsoleReader reader
    
    final Closure prompt
    
    final CommandsMultiCompleter completer
    WrappedInputStream wrappedInputStream

    InteractiveShellRunner(final Groovysh shell, final Closure prompt) {
        this(shell, prompt, 0)
    }

    InteractiveShellRunner(final Groovysh shell, final Closure prompt, int metaclass_completion_prefix_length) {
        super(shell)
        
        this.prompt = prompt
        this.wrappedInputStream = new WrappedInputStream(shell.io.inputStream)
        this.reader = new ConsoleReader(wrappedInputStream, shell.io.outputStream)
        // expand events ia an advanced feature of JLine that clashes with Groovy syntax (e.g. invoke "2!=3")
        this.reader.expandEvents = false


        // complete groovysh commands, display, import, ... as first word in line
        this.completer = new CommandsMultiCompleter()
        reader.addCompleter(this.completer)

        reader.addCompleter(new GroovySyntaxCompletor(shell,
                new ReflectionCompletor(shell,
                        metaclass_completion_prefix_length),
                [new KeywordSyntaxCompletor(),
                        new VariableSyntaxCompletor(shell),
                        new CustomClassSyntaxCompletor(shell),
                        new ImportsSyntaxCompletor(shell)],
                new FileNameCompleter(false)))
    }
    
    void run() {
        for (Command command in shell.registry.commands()) {
            completer.add(command)
        }

        // Force things to become clean
        completer.refresh()

        // And then actually run
        adjustHistory()
        super.run()
    }
    
    void setHistory(final FileHistory history) {
        reader.history = history
        def dir = history.file.parentFile
        
        if (!dir.exists()) {
            dir.mkdirs()
            
            log.debug("Created base directory for history file: $dir")
        }
        
        log.debug("Using history file: $history.file")
    }
    
    protected String readLine() {
        try {
            if (Boolean.valueOf(Preferences.get(Groovysh.AUTOINDENT_PREFERENCE_KEY))) {
                // prevent auto-indent when pasting code blocks
                if (shell.io.inputStream.available() == 0) {
                    wrappedInputStream.insert(((Groovysh) shell).getIndentPrefix())
                }
            }
            return reader.readLine(prompt.call() as String)
        } catch (StringIndexOutOfBoundsException e) {
            log.debug("HACK: Try and work around GROOVY-2152 for now", e)
            reader.println()
            return "";
        } catch (Throwable t) {
            if (shell.io.verbosity == IO.Verbosity.DEBUG) {
                throw t
            }
            reader.println()
            return ""
        }
    }

    @Override
    protected boolean work() {
        boolean result= super.work()
        adjustHistory()

        result
    }

    private void adjustHistory() {
        // we save the evicted line in casesomeone wants to use it with history recall
        if (shell instanceof Groovysh) {
            def history = shell.history
            shell.historyFull = (history != null) && (history.size() >= history.getMaxSize())
            if (shell.historyFull) {
                def first = history.first()
                if (first) {
                    shell.evictedLine = first.value()
                }
            }
        }
    }

}

/**
 * Completer for interactive shells.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class CommandsMultiCompleter
    extends AggregateCompleter
{
    protected final Logger log = Logger.create(this.class)
    
    List/*<Completer>*/ list = []
    
    private boolean dirty = false
    
    def add(final Command command) {
        assert command
        
        //
        // FIXME: Need to handle completer removal when things like aliases are rebound
        //
        
        def c = command.completer
        
        if (c) {
            list << c
            
            log.debug("Added completer[${list.size()}] for command: $command.name")
            
            dirty = true
        }
    }

    void refresh() {
        log.debug("Refreshing the completer list")

        getCompleters().clear()
        getCompleters().addAll(list)
        dirty = false
    }

    int complete(final String buffer, final int pos, final List cand) {
        assert buffer != null
        
        //
        // FIXME: This is a bit of a hack, I'm too lazy to rewrite a more efficient
        //        completer impl that is more dynamic than the jline.MultiCompleter version
        //        so just re-use it and reset the list as needed
        //

        if (dirty) {
            refresh()
        }
        
        return super.complete(buffer, pos, cand)
    }
}
