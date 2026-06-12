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
package org.apache.groovy.groovysh.jline

import org.apache.groovy.groovysh.commands.SystemTestSupport
import org.junit.jupiter.api.Test

/**
 * Tests for the Groovy-specific overrides in {@link GroovySystemRegistry}:
 * pipe-operator renames, {@code /!} command-prefix recognition, and the
 * {@code execute()} rewriting that strips whitespace around {@code =} when
 * the right-hand side is a command. Uses {@link SystemTestSupport} so the
 * full registry stack is available for the execute-path assertions.
 */
class GroovySystemRegistryTest extends SystemTestSupport {

    @Test
    void pipeOperatorsAreRenamedForGroovy() {
        // The Groovy fork rebinds the SystemRegistryImpl pipe operators so they
        // don't collide with Groovy operators (||, &&, >>, etc).
        def names = system.pipeNames
        assert names.contains('|||')   // Pipe.OR  (was '||')
        assert names.contains('|&&')   // Pipe.AND (was '&&')
        assert names.contains('|>')    // Pipe.REDIRECT (was '>')
        assert names.contains('|>>')   // Pipe.APPEND (was '>>')
    }

    @Test
    void bangPrefixIsRecognisedAsCommand() {
        // /!ls etc must be claimed by isCommandOrScript so the parser routes
        // them to the registered shell-out handler instead of evaluating them
        // as Groovy expressions.
        assert system.isCommandOrScript('/!ls')
        assert system.isCommandOrScript('/!cd')
        assert !system.isCommandOrScript('groovyExpression')
    }

    @Test
    void plainGroovyAssignmentPassesThrough() {
        // When the right-hand side is not a command (no leading slash), the
        // execute() override leaves the line unchanged and Groovy evaluates it.
        system.execute('answer = 42')
        assert console.hasVariable('answer')
        assert console.getVariable('answer') == 42
    }

    @Test
    void commandResultAssignmentSurvivesWhitespaceAroundEquals() {
        // SystemRegistryImpl assumes no whitespace around `=` in command-result
        // assignments. Our execute() rewrite normalises `x = /show` to
        // `x=/show` so it parses as one. Without the rewrite, JLine would
        // either error or hand the line to Groovy for evaluation, which
        // would not bind `result` here.
        console.execute('dummy', 'a = 99')
        system.execute('result = /show')
        assert console.hasVariable('result')
    }
}
