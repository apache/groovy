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
package org.apache.groovy.groovysh.commands

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

import java.util.stream.Stream

/**
 * Cheap blanket coverage for every Groovy command's {@code --help} flag.
 * One dynamic test per registered command — adding a new command without
 * wiring its {@code CmdDesc} or {@code maybePrintHelp} call surfaces here
 * immediately rather than waiting for a user to type {@code /yourcmd --help}.
 *
 * <p>The set of names comes from {@link org.jline.console.CommandRegistry#commandNames}
 * at runtime, so the test self-adjusts to environment-conditional commands
 * (e.g. {@code /grab} only registers when Ivy is on the classpath).
 */
class HelpFlagTest extends SystemTestSupport {

    @TestFactory
    Stream<DynamicTest> everyRegisteredCommandRespondsToHelpFlag() {
        def names = (groovy.commandNames() as List).toSorted()
        assert !names.empty, 'no Groovy commands registered'
        return names.stream().map { String name ->
            DynamicTest.dynamicTest("$name --help") {
                int printerBefore = printer.output.size()
                int terminalBefore = terminalOutput().length()
                try {
                    system.execute("$name --help")
                } catch (Exception e) {
                    throw new AssertionError("'$name --help' threw: $e.message", e)
                }
                int printerAfter = printer.output.size()
                int terminalAfter = terminalOutput().length()
                // Help can land on either sink: maybePrintHelp goes through
                // the printer; commands that delegate to JLine's parseOptions
                // (e.g. /doc) write to the terminal. Either is acceptable.
                assert printerAfter > printerBefore || terminalAfter > terminalBefore,
                        "'$name --help' produced no output via printer or terminal"
            }
        }
    }

    @TestFactory
    Stream<DynamicTest> everyRegisteredCommandRespondsToShortHelpFlag() {
        // Same enumeration, exercising the `-?` shorthand. maybePrintHelp
        // checks both forms; this confirms neither rots.
        def names = (groovy.commandNames() as List).toSorted()
        return names.stream().map { String name ->
            DynamicTest.dynamicTest("$name -?") {
                int printerBefore = printer.output.size()
                int terminalBefore = terminalOutput().length()
                try {
                    system.execute("$name -?")
                } catch (Exception e) {
                    throw new AssertionError("'$name -?' threw: $e.message", e)
                }
                assert printer.output.size() > printerBefore
                        || terminalOutput().length() > terminalBefore,
                        "'$name -?' produced no output via printer or terminal"
            }
        }
    }
}
