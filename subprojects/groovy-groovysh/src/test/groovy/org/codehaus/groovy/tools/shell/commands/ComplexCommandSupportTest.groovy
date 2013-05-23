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

import jline.console.completer.Completer
import org.codehaus.groovy.tools.shell.CommandException
import org.codehaus.groovy.tools.shell.ComplexCommandSupport

/**
 * Tests for the {@link ClearCommand} class.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ComplexCommandSupportTest
    extends CommandTestSupport
{
    void testNew() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", null) {}
        assertEquals("fcom", com.name)
        assertEquals("f", com.shortcut)
        assertEquals(null, com.functions)
        assertEquals(null, com.getFunctions())
        assertEquals(null, com.defaultFunction)
    }

    void testNewFunctions() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo", "bar"]) {}
        assertEquals("fcom", com.name)
        assertEquals("f", com.shortcut)
        assertEquals(["foo", "bar"], com.functions)
        assertEquals(["foo", "bar"], com.getFunctions())
        assertEquals(null, com.defaultFunction)
    }

    void testNewFunctionsDefault() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo", "bar"], "foo") {}
        assertEquals("fcom", com.name)
        assertEquals("f", com.shortcut)
        assertEquals(["foo", "bar"], com.functions)
        assertEquals(["foo", "bar"], com.getFunctions())
        assertEquals("foo", com.defaultFunction)
    }

    void testNewFunctionsBadDefault() {
        try {
            ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo", "bar"], "foo") {}
            fail()
        } catch (AssertionError e) {
            // pass
        }
    }

    void testCreateCompletors() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo", "bar", "baz"]) {}
        List<Completer> completors = com.createCompleters()
        assertEquals(2, completors.size())
        assertEquals(null, completors[-1])

    }

    void testCompletor() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo", "bar", "baz"]) {}
        def candidates = []
        Completer completor = com.getCompleter()
        assertEquals(5, completor.complete("fcom ba", "fcom ba".length(), candidates))
        assertEquals(["bar", "baz"], candidates)
        assertEquals(-1, completor.complete("fcom bar ba", "fcom bar ba".length(), candidates))
    }

    void testDoAll() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo", "bar", "all"]) {
            def invoked = []
            def do_foo = {
                invoked.add("foo");
                return 1
            }
            def do_bar = {
                invoked.add("bar");
                return 2
            }
        }
        assertEquals([1, 2], com.do_all().sort());
        assertEquals(["bar", "foo"], com.invoked.sort())
    }

    void testExecute() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo"]) {
            def invoked = []
            def do_foo = {arg1 -> invoked.addAll(arg1)}
        }
        try {
            com.execute([])
            fail()
        } catch (CommandException e) {
            // pass
        }

        com.execute(["foo"])
        assertEquals([], com.invoked)

        com.execute(["foo"])
        assertEquals([], com.invoked)

        com.execute(["foo", "bar"])
        assertEquals(["bar"], com.invoked)

        com.execute(["foo", "bar", "baz"])
        assertEquals(["bar", "bar", "baz"], com.invoked)
    }

    void testExecuteDefault() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo"], "foo") {
            def invoked = []
            def do_foo = {arg1 -> invoked.addAll(arg1)}
        }
        // assert no fail
        com.execute([])
    }

    void testExecuteFunction() {
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo"]) {
            def invoked = []
            def do_foo = {arg1 -> invoked.addAll(arg1)}
        }
        try {
            com.executeFunction("bar", ["baz"])
        }  catch (CommandException e) {
            // pass
        }
        assertEquals([], com.invoked)
        com.executeFunction("foo", ["baz"])
        assertEquals(["baz"], com.invoked)
        com.executeFunction("foo", ["bim", "bam"])
        assertEquals(["baz", "bim", "bam"], com.invoked)
    }

    void testLoadFunction() {
        Closure fun = { x -> x+1}
        ComplexCommandSupport com = new ComplexCommandSupport(shell, "fcom", "f", ["foo"], "foo") {
            def invoked = []
            def do_foo = fun
        }
        assertEquals(fun, com.loadFunction("foo"))
        try {
            com.loadFunction("bar")
            fail()
        } catch(CommandException e) {
            // pass
        }
    }
}