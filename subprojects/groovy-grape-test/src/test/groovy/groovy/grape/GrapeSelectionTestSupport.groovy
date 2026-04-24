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
package groovy.grape

import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import java.util.logging.StreamHandler

final class GrapeSelectionTestSupport {
    private GrapeSelectionTestSupport() {
    }

    /**
     * Captures log output emitted by {@code groovy.grape.Grape} during the action.
     * Grape uses {@link System.Logger}, which by default delegates to JUL with a threshold
     * of INFO — so DEBUG-level messages (e.g. the "Ignoring provider …" lines) would be
     * silently dropped. We temporarily lower the level and attach a dedicated handler that
     * writes to an in-memory buffer, then restore prior state in a finally block.
     */
    static String captureStderr(Closure<?> action) {
        def errBytes = new ByteArrayOutputStream()
        def logger = Logger.getLogger('groovy.grape.Grape')
        def priorLevel = logger.level
        def priorUseParent = logger.useParentHandlers
        def handler = new StreamHandler(new PrintStream(errBytes, true, 'UTF-8'), new SimpleFormatter())
        handler.level = Level.ALL
        logger.addHandler(handler)
        logger.level = Level.ALL
        logger.useParentHandlers = false
        try {
            action.call()
            handler.flush()
            return errBytes.toString('UTF-8')
        } finally {
            logger.removeHandler(handler)
            logger.level = priorLevel
            logger.useParentHandlers = priorUseParent
        }
    }
}
