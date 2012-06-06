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

import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * The 'history' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class HistoryCommand
    extends ComplexCommandSupport
{
    HistoryCommand(final Shell shell) {
        super(shell, 'history', '\\H')
        
        this.functions = [ 'show', 'clear', 'flush', 'recall' ]
        
        this.defaultFunction = 'show'
    }
    
    protected List createCompletors() {
        def loader = {
            def list = []
            
            functions.each { list << it }
            
            return list
        }
        
        return [
            new SimpleCompletor(loader),
            null
        ]
    }
    
    Object execute(List args) {
        if (!history) {
            fail("Shell does not appear to be interactive; Can not query history")
        }
        
        super.execute(args)

        // Don't return anything
        return null
    }
    
    def do_show = {
        history.historyList.eachWithIndex { item, i ->
            i = i.toString().padLeft(3, ' ')
            
            io.out.println(" @|bold $i|@  $item")
        }
    }
    
    def do_clear = {
        history.clear()
        
        if (io.verbose) {
            io.out.println('History cleared')
        }
    }
    
    def do_flush = {
        history.flushBuffer()
        
        if (io.verbose) {
            io.out.println('History flushed')
        }
    }
    
    def do_recall = { args ->
        def line
        
        if (args.size() != 1) {
            fail("History recall requires a single history identifer")
        }
        
        def id = args[0]

        //
        // FIXME: This won't work as desired because the history shifts when we run recall and could internally shift more from alias redirection
        //
        
        try {
            id = Integer.parseInt(id)
            if (shell.historyFull) {
                // if history was full before execution of the command, then the recall command itself
                // has been added to history before it actually gets executed
                // so we need to shift by one
                id--
            };
            line = id<0?shell.evictedLine:history.historyList[id]
        }
        catch (Exception e) {
            fail("Invalid history identifier: $id", e)
        }
        
        log.debug("Recalling history item #$id: $line")
        
        return shell.execute(line)
    }
}
