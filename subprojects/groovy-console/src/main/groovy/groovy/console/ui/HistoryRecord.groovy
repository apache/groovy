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
package groovy.console.ui

import groovy.transform.CompileStatic

/**
 * Captures the editor state and execution outcome for a console history entry.
 */
@CompileStatic
class HistoryRecord {
    /** Full editor contents captured for this history entry. */
    String allText
    /** Start offset of the selected text. */
    int selectionStart
    /** End offset of the selected text. */
    int selectionEnd
    /** Generated script name used during execution. */
    String scriptName
    /** Execution result, when evaluation completed successfully. */
    Object result
    /** Execution failure, when evaluation did not complete successfully. */
    Throwable exception

    /**
     * Returns the text that should be executed for this history entry.
     *
     * @param useSelection whether to execute only the current selection
     * @return the selected text with leading imports, or the full script text
     */
    String getTextToRun(boolean useSelection) {
        if (useSelection && selectionStart != selectionEnd) {
            // Retrieve all the imports included in the script before the current selection
            def before = allText[0 ..< selectionStart].split("\n")
            def importLines = before.findAll { it.trim().startsWith("import") }
            def imports = importLines.join("\n")
            def code = imports + "\n" + allText[selectionStart ..< selectionEnd]
            return code
        }
        return allText
    }

    /**
     * Returns the execution result or captured exception.
     *
     * @return the successful result, or the exception when execution failed
     */
    Object getValue() {
        return exception ? exception : result
    }
}
