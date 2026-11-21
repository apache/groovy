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

import org.jline.reader.Candidate
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.Parser
import org.jline.reader.impl.DefaultParser
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

/**
 * Completion of a dotted name from the {@code .groovy} sources under the working directory, which
 * walks only the directory of the package being completed.
 */
class SourceCompletionTest {

    @TempDir
    Path workDir

    @BeforeEach
    void sources() {
        Files.createDirectories(workDir.resolve('demo/app'))
        workDir.resolve('demo/app/Greeter.groovy').text = 'package demo.app\nclass Greeter {}\n'
        Files.createDirectories(workDir.resolve('other/deep'))
    }

    @Test
    void onlyThePackageDirectoryIsWalked() {
        assert GroovyEngine.packageDirectory(workDir, 'demo.app.') == workDir.resolve('demo/app')
        assert GroovyEngine.packageDirectory(workDir, 'demo.') == workDir.resolve('demo')
        // the last segment may be the start of a name
        assert GroovyEngine.packageDirectory(workDir, 'demo.ap') == workDir.resolve('demo')
        assert GroovyEngine.packageDirectory(workDir, 'dem') == workDir

        assert GroovyEngine.packageDirectory(workDir, 'missing.') == null
        assert GroovyEngine.packageDirectory(workDir, 'demo.missing.') == null
        assert GroovyEngine.packageDirectory(workDir, '[1..2].') == null
        assert GroovyEngine.packageDirectory(workDir, '[1,2].') == null
        assert GroovyEngine.packageDirectory(workDir, 'demo..') == null
        assert GroovyEngine.packageDirectory(workDir, '.') == null
    }

    @Test
    void sourcesUnderTheWorkingDirectoryStillComplete() {
        def offered = complete('demo.app.', 'demo.', '[1..2].')

        assert 'demo.app.Greeter' in offered['demo.app.']
        assert 'demo.app.' in offered['demo.']
        assert !offered['[1..2].'].any { it.startsWith('[1') }
    }

    // the sources are looked up in the working directory, which only a new JVM can set
    private Map<String, List<String>> complete(String... typed) {
        def javaBin = new File(System.getProperty('java.home'), 'bin/java').absolutePath
        def command = [javaBin, '-cp', System.getProperty('java.class.path'), Probe.name, *typed]
        def proc = new ProcessBuilder(command).directory(workDir.toFile()).redirectErrorStream(true).start()
        def out = proc.inputStream.text
        assert proc.waitFor() == 0 : out
        out.readLines().findAll { it.startsWith('CANDIDATES ') }.collectEntries { String line ->
            def (word, values) = line.substring('CANDIDATES '.length()).split('\t', 2) as List<String>
            [word, values ? values.split(' ') as List<String> : []]
        }
    }

    /**
     * Prints the candidates the script completer offers for each argument.
     */
    static class Probe {
        static void main(String[] args) {
            Terminal terminal = TerminalBuilder.builder().dumb(true)
                    .streams(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream()).build()
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build()
            def completer = new GroovyEngine().scriptCompleter
            def parser = new DefaultParser()
            args.each { String typed ->
                List<Candidate> candidates = []
                completer.complete(reader, parser.parse(typed, typed.length(), Parser.ParseContext.COMPLETE), candidates)
                println "CANDIDATES ${typed}\t${candidates*.value().join(' ')}"
            }
            terminal.close()
        }
    }
}
