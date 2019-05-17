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

import jline.console.completer.Completer
import org.apache.groovy.groovysh.CommandException
import org.apache.groovy.groovysh.ComplexCommandSupport

/**
 * Tests for the {@link ComplexCommandSupport} class.
 */
class ComplexCommandSupportTest extends CommandTestSupport {
    void testNew() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', null) {}
        assert 'fcom' == com.name
        assert 'f' == com.shortcut
        assert null == com.functions
        assert null == com.defaultFunction
    }

    void testNewFunctions() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo', 'bar']) {}
        assert 'fcom' == com.name
        assert 'f' == com.shortcut
        assert ['foo', 'bar'] == com.functions
        assert null == com.defaultFunction
    }

    void testNewFunctionsDefault() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo', 'bar'], 'foo') {}
        assert 'fcom' == com.name
        assert 'f' == com.shortcut
        assert ['foo', 'bar'] == com.functions
        assert 'foo' == com.defaultFunction
    }

    void testNewFunctionsBadDefault() {
        try {
            new ComplexCommandSupport(shell, 'fcom', 'f', ['foo', 'bar'], 'foo') {}
            fail('expected AssertionError')
        } catch (AssertionError e) {
            // pass
        }
    }

    void testCreateCompleters() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo', 'bar', 'baz']) {}
        List<Completer> completers = com.createCompleters()
        assert 2 == completers.size()
        assert null == completers[-1]

    }

    void testCompleter() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo', 'bar', 'baz']) {}
        def candidates = []
        Completer completer = com.completer
        assert 5 == completer.complete('fcom ba', 'fcom ba'.length(), candidates)
        assert ['bar', 'baz'] == candidates
        assert -1 == completer.complete('fcom bar ba', 'fcom bar ba'.length(), candidates)
    }

    void testDoAll() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo', 'bar', 'all']) {
            def invoked = []
            def do_foo = {
                invoked.add('foo')
                return 1
            }
            def do_bar = {
                invoked.add('bar')
                return 2
            }
        }
        assert [1, 2] == com.do_all().sort()
        assert ['bar', 'foo'] == com.invoked.sort()
    }

    void testExecute() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo']) {
            def invoked = []
            def do_foo = {arg1 -> invoked.addAll(arg1)}
        }
        try {
            com.execute([])
            fail('expected CommandException ')
        } catch (CommandException e) {
            // pass
        }

        com.execute(['foo'])
        assert [] == com.invoked

        com.execute(['foo'])
        assert [] == com.invoked

        com.execute(['foo', 'bar'])
        assert ['bar'] == com.invoked

        com.execute(['foo', 'bar', 'baz'])
        assert ['bar', 'bar', 'baz'] == com.invoked
    }

    void testExecuteDefault() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo'], 'foo') {
            def invoked = []
            def do_foo = {arg1 -> invoked.addAll(arg1)}
        }
        // assert no fail
        com.execute([])
    }

    void testExecuteFunction() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo']) {
            def invoked = []
            def do_foo = {arg1 -> invoked.addAll(arg1)}
        }
        try {
            com.executeFunction('bar', ['baz'])
        }  catch (CommandException e) {
            // pass
        }
        assert [] == com.invoked
        com.executeFunction('foo', ['baz'])
        assert ['baz'] == com.invoked
        com.executeFunction('foo', ['bim', 'bam'])
        assert ['baz', 'bim', 'bam'] == com.invoked
    }

    void testLoadFunction() {
        Closure fun = { x -> x+1}
        ComplexCommandSupport com = new ComplexCommandSupport(shell, 'fcom', 'f', ['foo'], 'foo') {
            def invoked = []
            def do_foo = fun
        }
        assert fun == com.loadFunction('foo')
        try {
            com.loadFunction('bar')
            fail('expected CommandException')
        } catch(CommandException e) {
            // pass
        }
    }
}
