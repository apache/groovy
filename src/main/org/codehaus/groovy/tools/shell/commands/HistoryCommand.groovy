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

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Shell

/**
 * The 'history' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class HistoryCommand
    extends CommandSupport
{
    HistoryCommand(final Shell shell) {
        super(shell, 'history', '\\H')
    }
    
    Object execute(final List args) {
        assertNoArguments(args)
        
        def reader = shell.runner?.reader
        
        if (!reader) {
            fail("Shell does not appear to be interactive; Can not query history")
        }
        
        //
        // TODO: Add support to fetch, clear, load, etc.  Really need to pre-load the history too...
        //
        
        reader.history.historyList.eachWithIndex { item, idx ->
            idx = idx.toString().padLeft(3, ' ')
            
            io.out.println(" $idx  $item")
        }
    }
}
