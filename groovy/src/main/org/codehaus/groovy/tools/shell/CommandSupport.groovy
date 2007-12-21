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

import jline.Completor
import jline.NullCompletor
import jline.ArgumentCompletor
import jline.History

import org.codehaus.groovy.tools.shell.util.MessageSource
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * Support for {@link Command} instances.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
abstract class CommandSupport
    implements Command
{
    protected static final String NEWLINE = System.properties['line.separator']
    
    /** Instance logger for the command, initialized late to include the command name. */
    protected final Logger log

    /** i18n message source for the command. */
    protected final MessageSource messages = new MessageSource(this.class, CommandSupport.class)

    /** The name of the command. */
    final String name

    /** The shortcut switch */
    final String shortcut
    
    /** The owning shell. */
    protected final Shell shell

    /** The I/O container for the command to spit stuff out. */
    protected final IO io

    /** Provides the command instance with the registry, for aliasing support. */
    protected CommandRegistry registry
    
    /** Standard aliases for the command. */
    final List/*<CommandAlias>*/ aliases = []
    
    /** Flag to indicate if the command should be hidden or not. */
    boolean hidden = false
    
    protected CommandSupport(final Shell shell, final String name, final String shortcut) {
        assert shell
        assert name
        assert shortcut
        
        this.log = Logger.create(this.class, name)
        this.shell = shell
        this.io = shell.io
        this.name = name
        this.shortcut = shortcut
        
        //
        // NOTE: Registry will be added once registered.
        //
    }

    String getDescription() {
        return messages['command.description']
    }

    String getUsage() {
        return messages['command.usage']
    }

    String getHelp() {
        return messages['command.help']
    }

    /**
     * Override to provide custom completion semantics for the command.
     */
    protected List createCompletors() {
        return null
    }

    /**
     * Setup the completor for the command.
     */
    Completor getCompletor() {
        if (hidden) {
            return null
        }
        
        def list = [ new SimpleCompletor(name, shortcut) ]

        def completors = createCompletors()
        
        if (completors) {
            completors.each {
                if (it) {
                    list << it
                }
                else {
                    list << new NullCompletor()
                }
            }
        }
        else {
            list << new NullCompletor()
        }

        return new ArgumentCompletor(list)
    }
    
    //
    // Helpers
    //

    protected void alias(final String name, final String shortcut) {
        aliases << new CommandAlias(shell, name, shortcut, this.name)
    }

    protected void fail(final String msg) {
        throw new CommandException(this, msg)
    }
    
    protected void fail(final String msg, final Throwable cause) {
        throw new CommandException(this, msg, cause)
    }
    
    protected void assertNoArguments(final List args) {
        assert args != null
        
        if (args.size() > 0) {
            fail(messages.format('error.unexpected_args', args.join(' ')))
        }
    }
    
    //
    // Shell access helpers
    //

    protected BufferManager getBuffers() {
        return shell.buffers
    }

    protected List getBuffer() {
        return shell.buffers.current()
    }

    protected List getImports() {
        return shell.imports
    }
    
    protected Binding getBinding() {
        return shell.interp.context
    }
    
    protected Map getVariables() {
        return binding.variables
    }
    
    protected History getHistory() {
        return shell.history
    }
    
    protected GroovyClassLoader getClassLoader() {
        return shell.interp.classLoader
    }
}