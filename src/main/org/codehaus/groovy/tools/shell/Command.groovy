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
    
    Command(final String name, final String shortcut, final Closure function) {
        assert name
        assert shortcut
        assert function
        
        this.name = name
        this.shortcut = shortcut
        this.function = function
    }

    protected Command(final String name, final String shortcut) {
        this(name, shortcut, NOOP)
    }

    String getDescription() {
        return messages["${name}.description"]
    }

    String getHelp() {
        return messages["${name}.help"]
    }

    void execute(final List args) {
        //
        // TODO: Pass in command context ?
        //
        
        function.call(args)
    }
}