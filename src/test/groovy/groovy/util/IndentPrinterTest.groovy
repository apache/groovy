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
package groovy.util

import groovy.test.GroovyTestCase

/**
 * Unit test for IndentPrinter.
 */
class IndentPrinterTest extends GroovyTestCase {

    Writer out

    void setUp(){
        out = new StringWriter()
    }

    void testSimpleIndentation() {
        def printer = new IndentPrinter(new PrintWriter(out))

        printer.printIndent()
        printer.println 'parent'
        printer.incrementIndent()
        printer.printIndent()
        printer.println 'child'
        printer.decrementIndent()
        printer.printIndent()
        printer.println 'parent2'
        printer.flush()

        assert 'parent\n  child\nparent2\n' == out.toString()
    }

    void testAutoIndentation() {
        def printer = new IndentPrinter(new PrintWriter(out))
        printer.autoIndent = true

        printer.println 'parent'
        printer.incrementIndent()
        printer.println 'child'
        printer.decrementIndent()
        printer.println 'parent2'
        printer.flush()

        assert 'parent\n  child\nparent2\n' == out.toString()
    }

    void testInWithBlock() {
        new IndentPrinter(new PrintWriter(out)).with { p ->
            p.printIndent()
            p.println('parent1')
            p.incrementIndent()
            p.printIndent()
            p.println('child 1')
            p.printIndent()
            p.println('child 2')
            p.decrementIndent()
            p.printIndent()
            p.println('parent2')
            p.flush()
        }
        assert 'parent1\n  child 1\n  child 2\nparent2\n' == out.toString()
    }
}