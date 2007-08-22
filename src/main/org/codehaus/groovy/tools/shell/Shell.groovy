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
 * A simple shell for invoking commands from a command-line.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Shell
{
    protected final ShellLog log = new ShellLog(this.class)

    final CommandRegistry registry = new CommandRegistry()

    final IO io

    Shell(final IO io) {
        assert io
        
        this.io = io
    }
    
    Shell() {
        this(new IO())
    }
    
    Command findCommand(final String line) {
        assert line
        
        def args = line.trim().tokenize()
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
            def args = line.trim().tokenize()
            
            if (args.size() == 1) {
                args = []
            }
            else {
                args = args[1..-1]
            }
            
            log.debug("Executing command($command.name): $command; w/args: $args")
            
            try {
                result = command.execute(args)
                
                log.debug("Result: $result")
            }
            catch (CommandException e) {
                log.debug("Error: $e")
                
                io.err.println(e.message)
            }
        }
        
        return result
    }
    
    def leftShift(final String line) {
        return execute(line)
    }
    
    def leftShift(final Command command) {
        registry << command
    }

    void alias(final String name, final String shortcut, final String target) {
        this << new CommandAlias(this, name, shortcut, target)
    }
}
