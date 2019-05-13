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
package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.tools.shell.util.Logger

/**
 * Support for running a {@link Shell}.
 */
@Deprecated
abstract class ShellRunner
    implements Runnable
{
    protected final Logger log = Logger.create(this.class)

    final Shell shell

    boolean running = false

    boolean breakOnNull = true

    Closure errorHandler = { e ->
        log.debug(e)

        running = false
    }

    protected ShellRunner(final Shell shell) {
        assert(shell != null)

        this.shell = shell
    }

    @Override
    void run() {
        log.debug('Running')

        running = true

        while (running) {
            try {
                running = work()
            }
            catch (ExitNotification n) {
                throw n
            }
            catch (Throwable t) {
                log.debug("Work failed: $t", t)

                if (errorHandler) {
                    try {
                        errorHandler.call(t)
                    } catch (Throwable t2) {
                        errorHandler(new IllegalArgumentException("Error when handling error: $t.message"))
                        errorHandler.call(t2)
                    }
                }
            }
        }

        log.debug('Finished')
    }

    protected boolean work() {
        def line = readLine()

        if (log.debugEnabled) {
            log.debug("Read line: $line")
        }

        // Stop on null (maybe)
        if (line == null && breakOnNull) {
            return false // stop the loop
        }

        // Ignore empty lines
        if (line.trim().size() > 0) {
            shell << line
        }

        return true
    }

    protected abstract String readLine()
}

