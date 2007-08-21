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
import jline.ArgumentCompletor
import jline.NullCompletor

/**
 * Command execution detail container.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Command
{
    private static final Closure NOOP = {}

    protected final MessageSource messages = new MessageSource(this.class)
    
    /** The name of the command. */
    final String name
    
    /** The shortcut switch */
    final String shortcut
    
    /** The code to be executed. */
    final Closure function
    
    /** Provides the command instance with the registry, for aliasing support. */
    CommandRegistry registry

    /** The completor for this command. */
    Completor completor

    Command(final String name, final String shortcut, final Closure function, final Completor[] completors) {
        assert name
        assert shortcut
        assert function
        
        this.name = name
        this.shortcut = shortcut
        this.function = function
        this.completor = createCompletor(completors)
    }

    Command(final String name, final String shortcut, final Closure function) {
        this(name, shortcut, function, null)
    }

    protected Command(final String name, final String shortcut) {
        this(name, shortcut, NOOP, null)
    }

    protected Completor createCompletor(final Completor[] completors) {
        // Setup the completor(s) for the command
        def list = []
        list << new SimpleCompletor(name, shortcut)

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
    
    String getDescription() {
        return messages["${name}.description"]
    }

    String getUsage() {
        return messages["${name}.usage"]
    }

    String getHelp() {
        return messages["${name}.help"]
    }

    void execute(final List args) {
        function.call(args)
    }
}