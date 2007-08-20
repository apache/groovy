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

import jline.SimpleCompletor

/**
 * Completes input line from command names (or aliases) bound in the registry.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class CommandNameCompletor
    extends SimpleCompletor
{
    CommandNameCompletor(final CommandRegistry registry) {
        super(new String[0])
        assert registry != null

        //
        // TODO: See about adding custom completor muck for select commands
        //       (like load and save which take files, or buffer which takes +|-|n
        //
        
        registry.commands.each {
            this << it.name
            this << it.shortcut
        }
    }
    
    def leftShift(final String name) {
        assert name
        
        addCandidateString(name)
    }
}