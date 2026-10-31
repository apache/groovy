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
package org.apache.groovy.mcp

import groovy.json.JsonSlurper
import org.apache.groovy.mcp.internal.StdioFrames
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

final class GroovyMcpLauncherTest {

    @Test
    void parseAcceptsServerFlagsAndRejectsABadCommandLine(@TempDir Path root) {
        assert GroovyMcpLauncher.parse(null).config != null
        assert GroovyMcpLauncher.parse(new String[0]).config.root() == null
        assert GroovyMcpLauncher.serves(null)
        assert GroovyMcpLauncher.serves(['--stdio'] as String[])
        assert !GroovyMcpLauncher.serves(['--help'] as String[])
        assert !GroovyMcpLauncher.serves(['--version'] as String[])

        def help = GroovyMcpLauncher.parse(['--version', '--bogus', '--help'] as String[])
        assert help.help()
        assert !help.error()

        def version = GroovyMcpLauncher.parse(['--stdio', '--version', '--stdio'] as String[])
        assert version.version()

        def file = root.resolve('not-a-dir')
        Files.writeString(file, 'x')
        assert GroovyMcpLauncher.parse(['--root', file.toString()] as String[]).message.contains('not a directory')
        assert GroovyMcpLauncher.parse(['--root'] as String[]).message.contains('requires')
        assert GroovyMcpLauncher.parse(['--root', null] as String[]).message.contains('requires')
        assert GroovyMcpLauncher.parse(['--root', '\u0000'] as String[]).message.contains('not a directory')
        assert GroovyMcpLauncher.parse(['--root', root.toString(), '--root', root.toString()] as String[]).message.contains('duplicate')
        assert GroovyMcpLauncher.parse(['--classpath'] as String[]).message.contains('requires')
        assert GroovyMcpLauncher.parse(['--classpath', null] as String[]).message.contains('requires')
        assert GroovyMcpLauncher.parse(['--classpath', 'a', '--classpath', 'b'] as String[]).message.contains('duplicate')
        assert GroovyMcpLauncher.parse([null] as String[]).message == 'missing argument'
        def unknown = GroovyMcpLauncher.parse(['--nope\nbad'] as String[])
        assert unknown.error()
        assert !unknown.message.contains('\n')

        def sep = File.pathSeparator
        assert GroovyMcpLauncher.splitClasspath(null).isEmpty()
        assert GroovyMcpLauncher.splitClasspath('').isEmpty()
        assert GroovyMcpLauncher.splitClasspath("${sep}a${sep}${sep}b${sep}") == ['a', 'b']
        def parsed = GroovyMcpLauncher.parse(['--stdio', '--root', root.toString(), '--classpath', "a${sep}b"] as String[])
        assert !parsed.error()
        assert parsed.config.root() == root.toAbsolutePath().normalize()
        assert parsed.config.classpath() == ['a', 'b']
    }

    @Test
    void runPrintsHelpAndVersionWithoutStartingTheProtocol(@TempDir Path root) {
        def helpOut = new ByteArrayOutputStream()
        def helpErr = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(null, helpOut, helpErr, ['--help'] as String[]) == 0
        def help = helpOut.toString(StandardCharsets.UTF_8)
        assert help.contains('groovymcp')
        assert help.contains('--root')
        assert !help.contains('null')

        def versionOut = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(null, versionOut, null, ['--version'] as String[]) == 0
        assert versionOut.toString(StandardCharsets.UTF_8) == "groovy-mcp ${GroovyMcpServer.implementationVersion()}\n"

        assert GroovyMcpLauncher.run(null, null, null, ['--help'] as String[]) == 0
        def err = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(null, new ByteArrayOutputStream(), err, ['--nope'] as String[]) == 2
        assert err.toString(StandardCharsets.UTF_8).contains('unknown argument')
        assert GroovyMcpLauncher.run(null, null, null, ['--nope'] as String[]) == 2

        def missing = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(null, new ByteArrayOutputStream(), missing, ['--root', root.toString()] as String[]) == 1
        assert missing.toString(StandardCharsets.UTF_8).contains('missing input')

        def broken = new OutputStream() {
            @Override
            void write(int b) throws IOException {
                throw new IOException('full\nsecret')
            }
        }
        def brokenErr = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(null, broken, brokenErr, ['--version'] as String[]) == 1
        def logged = brokenErr.toString(StandardCharsets.UTF_8)
        assert logged.contains('full?secret')
        assert !logged.contains('\nsecret')
    }

    @Test
    void stdioFramesDriveASession(@TempDir Path root) {
        def frames = new ByteArrayOutputStream()
        frames.write('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{}}}\r\n'.getBytes(StandardCharsets.UTF_8))
        frames.write('{"jsonrpc":"2.0","method":"notifications/initialized"}\n'.getBytes(StandardCharsets.UTF_8))
        frames.write('{"jsonrpc":"2.0","id":2,"method":"ping"}\n'.getBytes(StandardCharsets.UTF_8))
        def out = new ByteArrayOutputStream()
        def err = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(new ByteArrayInputStream(frames.toByteArray()), out, err,
                ['--root', root.toString()] as String[]) == 0
        def lines = out.toString(StandardCharsets.UTF_8).readLines()
        assert lines.size() == 2
        def init = new JsonSlurper().parseText(lines[0]) as Map
        assert init.result.protocolVersion == '2025-03-26'
        def ping = new JsonSlurper().parseText(lines[1]) as Map
        assert ping.result != null

        def dangling = '{"jsonrpc":"2.0","id":3,"method":"ping"}'.getBytes(StandardCharsets.UTF_8)
        def danglingOut = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(new ByteArrayInputStream(dangling), danglingOut, new ByteArrayOutputStream(), null) == 0
        assert new JsonSlurper().parseText(danglingOut.toString(StandardCharsets.UTF_8).trim()).result != null

        def empty = new ByteArrayInputStream(new byte[0])
        assert GroovyMcpLauncher.run(empty, null, null, null) == 0

        def malformed = new ByteArrayOutputStream()
        malformed.write(new byte[] { (byte) 'x', (byte) 0xff, (byte) '\n' })
        malformed.write('{"jsonrpc":"2.0","id":4,"method":"ping"}\n'.getBytes(StandardCharsets.UTF_8))
        def malformedOut = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(new ByteArrayInputStream(malformed.toByteArray()), malformedOut,
                new ByteArrayOutputStream(), null) == 0
        def malformedLines = malformedOut.toString(StandardCharsets.UTF_8).readLines()
        assert (new JsonSlurper().parseText(malformedLines[0]) as Map).error.code == -32700
        assert (new JsonSlurper().parseText(malformedLines[1]) as Map).result != null

        def huge = new ByteArrayOutputStream()
        huge.write('{"jsonrpc":"2.0","id":5,"method":"ping","params":{"p":"'.getBytes(StandardCharsets.UTF_8))
        def pad = new byte[McpServerConfig.DEFAULT_MAX_MESSAGE_BYTES]
        Arrays.fill(pad, (byte) 'x')
        huge.write(pad)
        huge.write('"}}\n{"jsonrpc":"2.0","id":6,"method":"ping"}\n'.getBytes(StandardCharsets.UTF_8))
        def hugeOut = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(new ByteArrayInputStream(huge.toByteArray()), hugeOut,
                new ByteArrayOutputStream(), null) == 0
        def hugeLines = hugeOut.toString(StandardCharsets.UTF_8).readLines()
        assert (new JsonSlurper().parseText(hugeLines[0]) as Map).error.message.contains('exceeds')
        assert (new JsonSlurper().parseText(hugeLines[1]) as Map).id == 6

        def crashingIn = new InputStream() {
            @Override
            int read() throws IOException {
                throw new IOException('broken pipe')
            }
        }
        def crashErr = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(crashingIn, new ByteArrayOutputStream(), crashErr, null) == 1
        assert crashErr.toString(StandardCharsets.UTF_8).contains('broken pipe')

        def crashingOut = new OutputStream() {
            @Override
            void write(int b) {
                throw new IllegalStateException('stopped')
            }
        }
        def pingBytes = '{"jsonrpc":"2.0","id":7,"method":"ping"}\n'.getBytes(StandardCharsets.UTF_8)
        def stopErr = new ByteArrayOutputStream()
        assert GroovyMcpLauncher.run(new ByteArrayInputStream(pingBytes), crashingOut, stopErr, null) == 1
        assert stopErr.toString(StandardCharsets.UTF_8).contains('stopped')
    }

    @Test
    void framesRejectABadReaderAndKeepTheNextLine() {
        def failed = false
        try {
            StdioFrames.read(null, 8)
        } catch (IllegalArgumentException ex) {
            failed = ex.message == 'in'
        }
        assert failed
        def bytes = false
        try {
            StdioFrames.read(new PushbackInputStream(new ByteArrayInputStream(new byte[0])), 0)
        } catch (IllegalArgumentException ex) {
            bytes = ex.message == 'maxBytes'
        }
        assert bytes

        def raw = new PushbackInputStream(new ByteArrayInputStream('ab\nnext\n'.getBytes(StandardCharsets.UTF_8)))
        def tooBig = StdioFrames.read(raw, 1)
        assert tooBig.kind() == StdioFrames.Frame.Kind.TOO_LARGE
        def next = StdioFrames.read(raw, 8)
        assert next.kind() == StdioFrames.Frame.Kind.LINE
        assert next.text() == 'next'
        assert StdioFrames.read(raw, 8).kind() == StdioFrames.Frame.Kind.EOF

        def exact = new PushbackInputStream(new ByteArrayInputStream('ab\n'.getBytes(StandardCharsets.UTF_8)))
        def line = StdioFrames.read(exact, 2)
        assert line.text() == 'ab'
        def onlyCr = new PushbackInputStream(new ByteArrayInputStream('\r\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(onlyCr, 4).text() == ''
        def blank = new PushbackInputStream(new ByteArrayInputStream('\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(blank, 4).text() == ''
        def embedded = new PushbackInputStream(new ByteArrayInputStream('a\rb\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(embedded, 8).text() == 'a\rb'
        def unfinished = new PushbackInputStream(new ByteArrayInputStream('abcd'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(unfinished, 2).kind() == StdioFrames.Frame.Kind.TOO_LARGE
        assert StdioFrames.read(unfinished, 2).kind() == StdioFrames.Frame.Kind.EOF

        def crlfExact = new PushbackInputStream(new ByteArrayInputStream('ab\r\nnext\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(crlfExact, 2).text() == 'ab'
        assert StdioFrames.read(crlfExact, 8).text() == 'next'
        def crlfUnder = new PushbackInputStream(new ByteArrayInputStream('a\r\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(crlfUnder, 2).text() == 'a'
        def crlfOver = new PushbackInputStream(new ByteArrayInputStream('abc\r\nnext\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(crlfOver, 2).kind() == StdioFrames.Frame.Kind.TOO_LARGE
        assert StdioFrames.read(crlfOver, 8).text() == 'next'
        def crAtEof = new PushbackInputStream(new ByteArrayInputStream('ab\r'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(crAtEof, 2).text() == 'ab'
        assert StdioFrames.read(crAtEof, 2).kind() == StdioFrames.Frame.Kind.EOF
        def crEofOver = new PushbackInputStream(new ByteArrayInputStream('abc\r'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(crEofOver, 2).kind() == StdioFrames.Frame.Kind.TOO_LARGE
        assert StdioFrames.read(crEofOver, 2).kind() == StdioFrames.Frame.Kind.EOF
        def embeddedCap = new PushbackInputStream(new ByteArrayInputStream('a\rb\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(embeddedCap, 3).text() == 'a\rb'
        def embeddedOver = new PushbackInputStream(new ByteArrayInputStream('a\rb\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(embeddedOver, 2).kind() == StdioFrames.Frame.Kind.TOO_LARGE
        def doubleCr = new PushbackInputStream(new ByteArrayInputStream('a\r\r\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(doubleCr, 2).text() == 'a\r'
        def doubleCrOver = new PushbackInputStream(new ByteArrayInputStream('a\r\r\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(doubleCrOver, 1).kind() == StdioFrames.Frame.Kind.TOO_LARGE
        def crAfterCap = new PushbackInputStream(new ByteArrayInputStream('abc\rx\n'.getBytes(StandardCharsets.UTF_8)))
        assert StdioFrames.read(crAfterCap, 2).kind() == StdioFrames.Frame.Kind.TOO_LARGE
    }

    @Test
    void launchRestoresStdoutAfterASession() {
        def previousIn = System.in
        def previousOut = System.out
        def previousErr = System.err
        def captured = new ByteArrayOutputStream()
        def errors = new ByteArrayOutputStream()
        try {
            System.setIn(new ByteArrayInputStream(new byte[0]))
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8))
            System.setErr(new PrintStream(errors, true, StandardCharsets.UTF_8))
            assert GroovyMcpLauncher.launch(['--version'] as String[]) == 0
            assert captured.toString(StandardCharsets.UTF_8).contains('groovy-mcp')
            assert GroovyMcpLauncher.launch([] as String[]) == 0
            assert System.out.is(previousOut) == false
        } finally {
            System.setIn(previousIn)
            System.setOut(previousOut)
            System.setErr(previousErr)
        }
        assert System.out.is(previousOut)
    }

    @Test
    void mainProcessPrintsVersionAndExitsOnEof() {
        def version = process(['--version'], new byte[0])
        assert version.status == 0
        assert version.stdout.contains("groovy-mcp ${GroovyMcpServer.implementationVersion()}")
        assert !version.stdout.contains('null')

        def help = process(['--help'], new byte[0])
        assert help.status == 0
        assert help.stdout.contains('groovymcp')
        assert !help.stdout.contains('groovy-mcp null')

        def eof = process([], new byte[0])
        assert eof.status == 0

        def bad = process(['--nope'], new byte[0])
        assert bad.status == 2
        assert bad.stderr.contains('unknown argument')
    }

    private static Command process(List<String> args, byte[] stdin) {
        def win = System.getProperty('os.name', '').toLowerCase(Locale.ROOT).contains('win')
        def java = Path.of(System.getProperty('java.home'), 'bin', win ? 'java.exe' : 'java').toString()
        def command = [java, '-cp', System.getProperty('java.class.path'), 'org.apache.groovy.mcp.GroovyMcpLauncher']
        command.addAll(args)
        def builder = new ProcessBuilder(command)
        def child = builder.start()
        def stdout = new ByteArrayOutputStream()
        def stderr = new ByteArrayOutputStream()
        def outPump = Thread.start { child.inputStream.transferTo(stdout) }
        def errPump = Thread.start { child.errorStream.transferTo(stderr) }
        child.outputStream.write(stdin)
        child.outputStream.close()
        try {
            assert child.waitFor(60, TimeUnit.SECONDS)
        } finally {
            child.destroyForcibly()
            outPump.join(5000)
            errPump.join(5000)
        }
        new Command(child.exitValue(), stdout.toString(StandardCharsets.UTF_8), stderr.toString(StandardCharsets.UTF_8))
    }

    private static final class Command {
        final int status
        final String stdout
        final String stderr

        Command(int status, String stdout, String stderr) {
            this.status = status
            this.stdout = stdout
            this.stderr = stderr
        }
    }
}
