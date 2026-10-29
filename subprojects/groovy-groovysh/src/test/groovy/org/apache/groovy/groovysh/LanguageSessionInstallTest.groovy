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
package org.apache.groovy.groovysh

import org.apache.groovy.groovysh.commands.SystemTestSupport
import org.apache.groovy.groovysh.language.GroovyshLanguageSession
import org.jline.console.impl.SystemRegistryImpl
import org.jline.reader.Completer
import org.jline.reader.impl.completer.AggregateCompleter
import org.junit.jupiter.api.Test

/**
 * The language session is installed after {@code groovysh_init.groovy},
 * and it does not sit beside the Inspector completer.
 */
class LanguageSessionInstallTest extends SystemTestSupport {

    @Test
    void sessionReplacesTheInspectorCompleter() {
        def session = Main.installLanguageSession(engine, system, null)
        try {
            def completers = addedCompleters()
            assert completers.size() == 1
            assert completers[0] instanceof GroovyshLanguageSession
        } finally {
            session?.close()
        }
    }

    @Test
    void noSyntaxCheckKeepsTheInspectorCompleter() {
        engine.put('GROOVY_OPTIONS', [noSyntaxCheck: true])
        assert Main.installLanguageSession(engine, system, null) == null
        def completers = addedCompleters()
        assert completers.size() == 1
        assert !(completers[0] instanceof GroovyshLanguageSession)
    }

    @Test
    void initFileCanTurnSyntaxCheckingOff() {
        def file = File.createTempFile('groovysh_init', '.groovy')
        file.deleteOnExit()
        file.text = 'GROOVY_OPTIONS = [noSyntaxCheck: true]\n'
        assert Main.attachLanguageSession(engine, system, file) == null
        assert engine.isNoSyntaxCheck()
        def completers = addedCompleters()
        assert completers.size() == 1
        assert !(completers[0] instanceof GroovyshLanguageSession)
    }

    @Test
    void missingLanguageJarUsesTheInspectorCompleter() {
        def err = new ByteArrayOutputStream()
        def previous = System.err
        System.err = new PrintStream(err, true)
        try {
            def session = Main.installLanguageSession(engine, system) {
                throw new NoClassDefFoundError('com.google.gson.Gson')
            }
            assert session == null
        } finally {
            System.err = previous
        }
        def text = err.toString('UTF-8')
        assert text.contains('Groovy language services are unavailable')
        assert text.contains('NoClassDefFoundError')
        def completers = addedCompleters()
        assert completers.size() == 1
        assert !(completers[0] instanceof GroovyshLanguageSession)
    }

    private Collection<Completer> addedCompleters() {
        def field = SystemRegistryImpl.getDeclaredField('customAggregateCompleter')
        field.accessible = true
        ((AggregateCompleter) field.get(system)).completers
    }
}
