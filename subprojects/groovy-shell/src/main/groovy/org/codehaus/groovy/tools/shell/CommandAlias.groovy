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
 * Provides simple command aliasing.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class CommandAlias
    extends CommandSupport
{
    final String targetName
    
    CommandAlias(final Shell shell, final String name, final String shortcut, final String target) {
        super(shell, name, shortcut)
        
        assert target

        this.targetName = target
    }
    
    Command getTarget() {
        def command = registry[targetName]
        
        assert command != null
        
        return command
    }
    
    protected List createCompletors() {
        return target.createCompletors()
    }
    
    String getDescription() {
        return messages.format('info.alias_to', targetName)
    }

    String getUsage() {
        return target.usage
    }
    
    String getHelp() {
        return target.help
    }
    
    boolean getHidden() {
        return target.hidden
    }
    
    Object execute(final List args) {
        target.execute(args)
    }
}
