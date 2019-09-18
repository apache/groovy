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

import groovy.console.ui.ObjectBrowser
import jline.console.completer.Completer
import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Groovysh
import org.apache.groovy.groovysh.util.SimpleCompleter

import javax.swing.*
import java.awt.*
import java.util.List

/**
 * The 'inspect' command.
 */
class InspectCommand extends CommandSupport {
    public static final String COMMAND_NAME = ':inspect'

    InspectCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':n')
    }

    def lafInitialized = false
    def headless

    @Override
    protected List<Completer> createCompleters() {
        return [
                new InspectCommandCompleter(binding),
                null
        ]
    }

    @Override
    Object execute(final List<String> args) {
        assert args != null

        log.debug("Inspecting w/args: $args")

        if (args.size() > 1) {
            fail(messages.format('error.unexpected_args', args.join(' ')))
        }

        def subject

        if (args.size() == 1) {
            subject = binding.variables[args[0]]
        } else {
            subject = binding.variables['_']
        }

        if (!subject) {
            io.out.println('Subject is null, false or empty; nothing to inspect') // TODO: i18n
        } else {
            // Only set LAF once.
            if (!lafInitialized) {
                lafInitialized = true
                try {
                    UIManager.setLookAndFeel(UIManager.systemLookAndFeelClassName)

                    // The setLAF doesn't throw a HeadlessException on Mac.
                    // So try really creating a frame.
                    new java.awt.Frame().dispose()

                    headless = false
                } catch (HeadlessException he) {
                    headless = true
                }
            }

            if (headless) {
                io.err.println("@|red ERROR:|@ Running in AWT Headless mode, 'inspect' is not available.")
                return
            }

            if (io.verbose) {
                io.out.println("Launching object browser to inspect: $subject") // TODO: i18n
            }

            ObjectBrowser.inspect(subject)
        }
    }
}

/**
 * Completer for the 'inspect' command.
 */
class InspectCommandCompleter extends SimpleCompleter {
    private final Binding binding

    InspectCommandCompleter(final Binding binding) {
        assert binding
        this.setWithBlank(false)
        this.binding = binding
    }

    @Override
    SortedSet<String> getCandidates() {
        SortedSet<String> set = new TreeSet<String>()

        binding.variables.keySet().each {
            set << it
        }

        return set
    }
}
