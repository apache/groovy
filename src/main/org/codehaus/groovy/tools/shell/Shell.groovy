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

import org.codehaus.groovy.tools.shell.util.Logger

/**
 * A simple shell for invoking commands from a command-line.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Shell
{
    protected final Logger log = Logger.create(this.class)

    final CommandRegistry registry = new CommandRegistry()

    final IO io

    Shell(final IO io) {
        assert io
        
        this.io = io
    }
    
    Shell() {
        this(new IO())
    }
    
    protected List parseLine(final String line) {
        assert line != null
        
        return line.trim().tokenize()
    }
    
    Command findCommand(final String line) {
        assert line
        
        //
        // TODO: Introduce something like 'boolean Command.accepts(String)' to ask
        //       commands if they can take the line?
        //
        //       Would like to get '!66' to invoke the 'history recall' bits, but currently has
        //       to be '! 66' for it to work with an alias like:
        //
        //           alias ! history recall
        //
        //       Or maybe allow commands to register specific syntax hacks into the registry?
        //       then ask the registry for the command for a given line?
        //
        
        def args = parseLine(line)
        
        assert args.size() > 0
        
        def name = args[0]
        
        def command = registry[name]
        
        return command
    }
    
    boolean isExecutable(final String line) {
        return findCommand(line) != null
    }
    
    Object execute(final String line) {
        assert line
        
        def command = findCommand(line)
        
        def result = null
        
        if (command) {
            def args = parseLine(line)
            
            if (args.size() == 1) {
                args = []
            }
            else {
                args = args[1..-1]
            }
            
            log.debug("Executing command($command.name): $command; w/args: $args")
            
            result = command.execute(args)
                
            log.debug("Result: ${String.valueOf(result)}")
        }
        
        return result
    }
    
    Command register(final Command command) {
        return registry << command
    }
    
    def leftShift(final String line) {
        return execute(line)
    }
    
    
    def leftShift(final Command command) {
        return register(command)
    }
}
