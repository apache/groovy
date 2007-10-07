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

import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.util.Preferences

/**
 * The 'shadow' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ShadowCommand
    extends ComplexCommandSupport
{
    ShadowCommand(final Shell shell) {
        super(shell, 'shadow', '\\&')
        
        this.hidden = true
        
        this.functions = [ 'debug', 'verbose', 'info', 'this' ]
    }
    
    def do_debug = {
        Preferences.verbosity = IO.Verbosity.DEBUG
    }
    
    def do_verbose = {
        Preferences.verbosity = IO.Verbosity.VERBOSE
    }

    def do_info = {
        Preferences.verbosity = IO.Verbosity.INFO
    }

    def do_this = {
        return this
    }
}

