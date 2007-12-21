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

import groovy.inspect.swingui.ObjectBrowser

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * The 'inspect' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class InspectCommand
    extends CommandSupport
{
    InspectCommand(final Shell shell) {
        super(shell, 'inspect', '\\n')
    }
    
    protected List createCompletors() {
        return [
            new InspectCommandCompletor(binding),
            null
        ]
    }

    Object execute(final List args) {
        assert args != null
        
        log.debug("Inspecting w/args: $args")
        
        if (args.size() > 1) {
            fail(messages.format('error.unexpected_args', args.join(' ')))
        }
        
        def subject
        
        if (args.size() == 1) {
            subject = binding.variables[args[0]]
        }
        else {
            subject = binding.variables['_']
        }

        if (!subject) {
            io.out.println('Subject is null; nothing to inspect') // TODO: i18n
        }
        else {
            if (io.verbose) {
                io.out.println("Launching object browser to inspect: $subject") // TODO: i18n
            }
            
            ObjectBrowser.inspect(subject)
        }
    }
}

/**
 * Completor for the 'inspect' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class InspectCommandCompletor
    extends SimpleCompletor
{
    private final Binding binding
    
    InspectCommandCompletor(final Binding binding) {
        assert binding

        this.binding = binding
    }

    SortedSet getCandidates() {
        def set = new TreeSet()

        binding.variables.keySet().each {
            set << it
        }

        return set
    }
}
