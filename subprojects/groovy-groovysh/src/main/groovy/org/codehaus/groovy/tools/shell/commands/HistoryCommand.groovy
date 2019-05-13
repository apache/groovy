/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.tools.shell.commands

import jline.console.history.History
import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * The 'history' command.
 */
@Deprecated
class HistoryCommand
    extends ComplexCommandSupport
{

    public static final String COMMAND_NAME = ':history'

    HistoryCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':H', [ 'show', 'clear', 'flush', 'recall' ], 'show')
    }

    @Override
    protected List createCompleters() {
        def loader = {
            def list = []
            list.addAll(functions)

            return list
        }

        SimpleCompletor subCommandsCompletor = new SimpleCompletor(loader)
        subCommandsCompletor.setWithBlank(false)
        return [
            subCommandsCompletor,
            null
        ]
    }

    @Override
    Object execute(List<String> args) {
        if (!history) {
            fail('Shell does not appear to be interactive; Can not query history')
        }

        super.execute(args)

        // Don't return anything
        return null
    }

    def do_show = {
        Iterator<History.Entry> histIt = history.iterator()
        while (histIt.hasNext()) {
            History.Entry next = histIt.next()
            if (next) {
                io.out.println(" @|bold ${next.index().toString().padLeft(3, ' ')}|@  ${next.value()}")
            }
        }
    }

    def do_clear = {
        history.clear()

        if (io.verbose) {
            io.out.println('History cleared')
        }
    }

    def do_flush = {
        history.flush()

        if (io.verbose) {
            io.out.println('History flushed')
        }
    }

    /**
     * history show shows a list of indexes and past commands. recall serves to rerun one of those by their index.
     * There is is moving window of indexes, so the first valid index will usually be greater than zero.
     */
    def do_recall = {args ->
        String line

        if (!args || ((List)args).size() != 1) {
            fail('History recall requires a single history identifer')
        }

        String ids = ((List<String>)args)[0]

        //
        // FIXME: This won't work as desired because the history shifts when we run recall and could internally shift more from alias redirection
        //

        try {
            int id = Integer.parseInt(ids)
            if (shell.historyFull) {
                // if history was full before execution of the command, then the recall command itself
                // has been added to history before it actually gets executed
                // so we need to shift by one
                id--
            }

            Iterator<History.Entry> listEntryIt = history.iterator()
            if (listEntryIt.hasNext()) {
                History.Entry next = listEntryIt.next()
                if (id < next.index() -1) {
                    // not using id on purpose, as might be decremented
                    fail("Unknown index: $ids")
                } else if (id == next.index() -1) {
                    line = shell.evictedLine
                } else if (next.index() == id) {
                    line = next.value()
                } else {
                    while (listEntryIt.hasNext()) {
                        next = listEntryIt.next()
                        if (next.index() == id) {
                            line = next.value()
                            break
                        }
                    }
                }
            }


        } catch (NumberFormatException e) {
            fail("Invalid history identifier: $ids", e)
        }

        log.debug("Recalling history item #$ids: $line")

        if (line) {
            return shell.execute(line)
        }
    }

}
