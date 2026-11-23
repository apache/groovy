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
package groovy.ui

import groovy.lang.GroovyCodeSource
import groovy.lang.GroovyShell
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.net.InetSocketAddress

import static groovy.test.GroovyAssert.shouldFail

/**
 * Covers where {@code groovy -l} binds its listening socket, and how the listener
 * reuses one compiled script class. Tests that start the server stop it with
 * {@link GroovySocketServer#close()}, which closes the listening socket and joins
 * the server thread.
 */
final class GroovySocketServerTest {

    @Test
    void testBarePortBindsLoopback() {
        def address = GroovyMain.parseBindAddress('1960')
        assert address.address.loopbackAddress
        assert address.port == 1960
    }

    @Test
    void testEmptyHostBindsLoopback() {
        assert GroovyMain.parseBindAddress(':1960').address.loopbackAddress
    }

    @Test
    void testExplicitHostIsHonoured() {
        def address = GroovyMain.parseBindAddress('127.0.0.1:1960')
        assert address.address.hostAddress == '127.0.0.1'
        assert address.port == 1960
    }

    @Test
    void testWildcardMustBeAskedForExplicitly() {
        def address = GroovyMain.parseBindAddress('0.0.0.0:1960')
        assert address.address.anyLocalAddress
        assert !address.address.loopbackAddress
    }

    @Test
    void testBracketedIpv6Host() {
        def address = GroovyMain.parseBindAddress('[::1]:1960')
        assert address.address.loopbackAddress
        assert address.port == 1960
    }

    @Test
    void testUnbracketedIpv6HostIsRejected() {
        def e = shouldFail(IllegalArgumentException) { GroovyMain.parseBindAddress('::1:1960') }
        assert e.message.contains('square brackets')
    }

    @Test
    void testUnterminatedBracketIsRejected() {
        shouldFail(IllegalArgumentException) { GroovyMain.parseBindAddress('[::1:1960') }
    }

    @Test
    void testNonNumericPortIsRejected() {
        def e = shouldFail(IllegalArgumentException) { GroovyMain.parseBindAddress('notaport') }
        assert e.message.contains('not a port number')
    }

    @Test
    void testPortOutOfRangeIsRejected() {
        def e = shouldFail(IllegalArgumentException) { GroovyMain.parseBindAddress('65536') }
        assert e.message.contains('out of range')
    }

    @Test
    void testUnresolvableHostIsRejected() {
        def e = shouldFail(IllegalArgumentException) {
            GroovyMain.parseBindAddress('nowhere.invalid.:1960')
        }
        assert e.message.contains('cannot resolve host')
    }

    @Test
    void testUnresolvedAddressIsRejectedBeforeListening() {
        def source = new GroovyCodeSource('line', 'test.groovy', GroovyShell.DEFAULT_CODE_BASE)
        shouldFail(IllegalArgumentException) {
            new GroovySocketServer(new GroovyShell(), source, false,
                    InetSocketAddress.createUnresolved('nowhere.invalid', 1960))
        }
    }

    // GROOVY-3476
    @Test
    void testGroovy3476ReusesOneClassForAnUnchangedScript() {
        def shell = new GroovyShell()
        shell.setVariable('acc', 0)
        def source = new GroovyCodeSource('''
            import groovy.transform.Field
            @Field int n = 0
            n += 1
            acc = acc + 1
            line.reverse() + ':' + n + ':' + acc
        ''', 'listener.groovy', GroovyShell.DEFAULT_CODE_BASE)
        def server = GroovySocketServer.withoutStarting(shell, source)
        try {
            def first = server.newConnectionScript()
            first.setProperty('line', 'ab')
            assert first.run() == 'ba:1:1'
            int loaded = shell.classLoader.loadedClasses.length
            30.times { i ->
                def next = server.newConnectionScript()
                next.setProperty('line', 'ab')
                assert next.class.is(first.class)
                assert next.class.classLoader.is(first.class.classLoader)
                assert next.run() == "ba:1:${i + 2}"
            }
            assert shell.classLoader.loadedClasses.length == loaded
        } finally {
            server.close()
        }
    }

    // GROOVY-3476
    @Test
    void testGroovy3476UsesTheShellConfiguration() {
        def config = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        imports.addImports('java.util.concurrent.atomic.AtomicInteger')
        config.addCompilationCustomizers(imports)
        def shell = new GroovyShell(config)
        def source = new GroovyCodeSource('new AtomicInteger(41).incrementAndGet()', 'configured.groovy', GroovyShell.DEFAULT_CODE_BASE)
        def server = GroovySocketServer.withoutStarting(shell, source)
        try {
            def first = server.newConnectionScript()
            assert first.run() == 42
            assert server.newConnectionScript().class.is(first.class)
        } finally {
            server.close()
        }
    }

    // GROOVY-3476
    @Test
    void testGroovy3476RecompilesFileWhenBytesChange() {
        def file = writeScript("return 'one'\n")
        try {
            def shell = new GroovyShell()
            assertRecompilesWhenBytesChange(shell, new GroovyCodeSource(file), file)
        } finally {
            file.delete()
        }
    }

    // GROOVY-3476
    @Test
    void testGroovy3476RecompilesUrlWhenBytesChange() {
        def file = writeScript("return 'one'\n")
        try {
            def shell = new GroovyShell()
            assertRecompilesWhenBytesChange(shell, new GroovyCodeSource(file.toURI()), file)
        } finally {
            file.delete()
        }
    }

    // GROOVY-3476
    @Test
    void testGroovy3476BrokenEditIsRetried() {
        def file = writeScript("return 'one'\n")
        try {
            def server = GroovySocketServer.withoutStarting(new GroovyShell(), new GroovyCodeSource(file))
            assert server.newConnectionScript().run() == 'one'
            file.text = 'class {'
            shouldFail { server.newConnectionScript() }
            shouldFail { server.newConnectionScript() }
            file.text = "return 'three'\n"
            def fixed = server.newConnectionScript()
            assert fixed.run() == 'three'
            assert server.newConnectionScript().class.is(fixed.class)
        } finally {
            file.delete()
        }
    }

    // GROOVY-3476
    @Test
    void testGroovy3476MissingSourceFails() {
        def file = writeScript("return 'one'\n")
        def urlFile = writeScript("return 'one'\n")
        try {
            def fileServer = GroovySocketServer.withoutStarting(new GroovyShell(), new GroovyCodeSource(file))
            assert fileServer.newConnectionScript().run() == 'one'
            assert file.delete()
            shouldFail(IOException) { fileServer.newConnectionScript() }

            def urlServer = GroovySocketServer.withoutStarting(new GroovyShell(), new GroovyCodeSource(urlFile.toURI()))
            assert urlServer.newConnectionScript().run() == 'one'
            assert urlFile.delete()
            shouldFail(IOException) { urlServer.newConnectionScript() }
        } finally {
            file.delete()
            urlFile.delete()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(20)
    void testGroovy3476ListenerReusesOneClass() {
        def source = new GroovyCodeSource('''
            import groovy.transform.Field
            @Field int calls = 0
            calls += 1
            "${init ? 'Y' : 'N'}:${calls}:${System.identityHashCode(this.class)}:${line}"
        ''', 'listener.groovy', GroovyShell.DEFAULT_CODE_BASE)
        def server = new GroovySocketServer(new GroovyShell(), source, true,
                new InetSocketAddress(InetAddress.loopbackAddress, 0))
        try {
            int port = awaitPort(server)
            def first = exchange(port, ['a', 'b'])
            def second = exchange(port, ['c'])
            def id = first[0].split(':')[2]
            assert first[0] == "Y:1:${id}:a"
            assert first[1] == "N:2:${id}:b"
            assert second[0] == "Y:1:${id}:c"

            server.close()
            def socket = new Socket()
            shouldFail(IOException) {
                socket.connect(new InetSocketAddress(InetAddress.loopbackAddress, port), 500)
            }
        } finally {
            server.close()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(40)
    void testGroovy3476EditWhileWaitingRunsOnTheArrivingConnection() {
        def file = writeScript("return 'before'\n")
        GroovySocketServer server = null
        try {
            server = new GroovySocketServer(new GroovyShell(), new GroovyCodeSource(file), true,
                    new InetSocketAddress(InetAddress.loopbackAddress, 0))
            int port = awaitAccept(server)
            // The listener is blocked in accept(). The connection that ends
            // that wait must run this edit: start, edit, connect.
            file.text = "return 'after'\n"
            assert exchange(port, ['x']) == ['after']
        } finally {
            server?.close()
            file.delete()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(40)
    void testGroovy3476BrokenEditDoesNotStopTheListener() {
        def file = writeScript("return 'one'\n")
        def err = new ByteArrayOutputStream()
        def original = System.err
        GroovySocketServer server = null
        try {
            server = new GroovySocketServer(new GroovyShell(), new GroovyCodeSource(file), true,
                    new InetSocketAddress(InetAddress.loopbackAddress, 0))
            int port = awaitAccept(server)
            System.setErr(new PrintStream(err, true))
            file.text = 'class {'
            assertClientClosedWithoutResult(port)
            awaitErr(err, 'MultipleCompilationErrorsException')
            awaitAccept(server)
            file.text = "return 'two'\n"
            assert exchange(port, ['x']) == ['two']
        } finally {
            System.setErr(original)
            server?.close()
            file.delete()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(40)
    void testGroovy3476FailedReadDoesNotStopTheListener() {
        def file = writeScript("return 'one'\n")
        def err = new ByteArrayOutputStream()
        def original = System.err
        GroovySocketServer server = null
        try {
            server = new GroovySocketServer(new GroovyShell(), new GroovyCodeSource(file), true,
                    new InetSocketAddress(InetAddress.loopbackAddress, 0))
            int port = awaitAccept(server)
            System.setErr(new PrintStream(err, true))
            assert file.delete()
            assertClientClosedWithoutResult(port)
            awaitErr(err, 'NoSuchFileException')
            awaitAccept(server)
            file.text = "return 'two'\n"
            assert exchange(port, ['x']) == ['two']
        } finally {
            System.setErr(original)
            server?.close()
            file.delete()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(20)
    void testGroovy3476BindFailureIsReported() {
        def held = new ServerSocket(0, 50, InetAddress.loopbackAddress)
        def err = new ByteArrayOutputStream()
        def original = System.err
        System.setErr(new PrintStream(err, true))
        GroovySocketServer server = null
        try {
            def source = new GroovyCodeSource('line', 'bind.groovy', GroovyShell.DEFAULT_CODE_BASE)
            server = new GroovySocketServer(new GroovyShell(), source, false,
                    new InetSocketAddress(InetAddress.loopbackAddress, held.localPort))
            long deadline = System.currentTimeMillis() + 5000
            while (!err.toString().contains('BindException') && System.currentTimeMillis() < deadline) {
                Thread.sleep(20)
            }
            assert err.toString().contains('BindException')
            assert server.localPort == -1
        } finally {
            System.setErr(original)
            server?.close()
            held.close()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(20)
    void testGroovy3476SocketExceptionAfterBindIsReported() {
        def source = new GroovyCodeSource('line', 'socket.groovy', GroovyShell.DEFAULT_CODE_BASE)
        def server = new GroovySocketServer(new GroovyShell(), source, false,
                new InetSocketAddress(InetAddress.loopbackAddress, 0))
        def err = new ByteArrayOutputStream()
        def original = System.err
        try {
            assert awaitPort(server) > 0
            // Close the listening socket directly, not through close(). That is a
            // SocketException after a successful bind, and it must reach System.err.
            System.setErr(new PrintStream(err, true))
            def listening = GroovySocketServer.getDeclaredField('serverSocket')
            listening.accessible = true
            (listening.get(server) as ServerSocket).close()
            def threadField = GroovySocketServer.getDeclaredField('serverThread')
            threadField.accessible = true
            def serverThread = threadField.get(server) as Thread
            serverThread.join(10000)
            assert !serverThread.alive : 'server thread still running after the listening socket was closed'
            def trace = err.toString()
            assert trace.contains('SocketException')
            assert trace.contains('GroovySocketServer.run')
        } finally {
            System.setErr(original)
            server?.close()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(20)
    void testGroovy3476CloseDoesNotReportTheListeningSocket() {
        def source = new GroovyCodeSource('line', 'close.groovy', GroovyShell.DEFAULT_CODE_BASE)
        def server = new GroovySocketServer(new GroovyShell(), source, false,
                new InetSocketAddress(InetAddress.loopbackAddress, 0))
        def err = new ByteArrayOutputStream()
        def original = System.err
        try {
            assert awaitPort(server) > 0
            System.setErr(new PrintStream(err, true))
            server.close()
            def trace = err.toString()
            assert trace.isEmpty()
        } finally {
            System.setErr(original)
            server?.close()
        }
    }

    // GROOVY-3476
    @Test
    @Timeout(40)
    void testGroovy3476CloseRightAfterConstructionStopsTheServer() {
        def err = new ByteArrayOutputStream()
        def original = System.err
        System.setErr(new PrintStream(err, true))
        GroovySocketServer server = null
        try {
            // Repeated because close() can run before run() is scheduled. One
            // pass can miss that window; each pass must still stop the server.
            8.times {
                def source = new GroovyCodeSource('line', 'early-close.groovy', GroovyShell.DEFAULT_CODE_BASE)
                server = new GroovySocketServer(new GroovyShell(), source, false,
                        new InetSocketAddress(InetAddress.loopbackAddress, 0))
                // No awaitPort: close() may run before the listening socket is published.
                server.close()
                def threadField = GroovySocketServer.getDeclaredField('serverThread')
                threadField.accessible = true
                def serverThread = threadField.get(server) as Thread
                // close() joins for at most 5 seconds. Wait here for a thread that
                // was not yet scheduled, which still exits at the closing check.
                serverThread.join(10000)
                assert !serverThread.alive : 'server thread still running after close() immediately following construction'
                int port = server.localPort
                assert port > 0 : 'listener did not publish a socket before exiting'
                def socket = new Socket()
                try {
                    shouldFail(IOException) {
                        socket.connect(new InetSocketAddress(InetAddress.loopbackAddress, port), 500)
                    }
                } finally {
                    socket.close()
                }
            }
            assert err.toString().isEmpty()
        } finally {
            System.setErr(original)
            server?.close()
        }
    }

    private static void assertRecompilesWhenBytesChange(GroovyShell shell, GroovyCodeSource source, File file) {
        def server = GroovySocketServer.withoutStarting(shell, source)
        try {
            def first = server.newConnectionScript()
            assert first.run() == 'one'
            assert server.newConnectionScript().class.is(first.class)
            int loaded = shell.classLoader.loadedClasses.length

            file.text = "return 'two'\n"
            def third = server.newConnectionScript()
            assert third.run() == 'two'
            assert !third.class.is(first.class)
            int afterChange = shell.classLoader.loadedClasses.length
            8.times {
                def again = server.newConnectionScript()
                assert again.class.is(third.class)
                assert again.run() == 'two'
            }
            assert shell.classLoader.loadedClasses.length == afterChange
            assert loaded > 0
        } finally {
            server.close()
        }
    }

    private static File writeScript(String text) {
        def file = File.createTempFile('groovy3476', '.groovy')
        file.deleteOnExit()
        file.text = text
        return file
    }

    /**
     * Waits until the server thread is blocked in {@link ServerSocket#accept()}.
     * {@link #awaitPort} only means the socket has been published; the listener
     * may still be about to enter {@code accept}.
     */
    private static int awaitAccept(GroovySocketServer server) {
        int port = awaitPort(server)
        def threadField = GroovySocketServer.getDeclaredField('serverThread')
        threadField.accessible = true
        def serverThread = threadField.get(server) as Thread
        long deadline = System.currentTimeMillis() + 10000
        boolean waiting = false
        while (!waiting && System.currentTimeMillis() < deadline) {
            waiting = serverThread.stackTrace.any { frame ->
                frame.methodName == 'accept' && frame.className == 'java.net.ServerSocket'
            }
            if (!waiting) {
                Thread.sleep(10)
            }
        }
        def stack = serverThread.stackTrace.collect { it.toString() }.join('\n')
        assert waiting : "listener did not reach accept():\n${stack}"
        return port
    }

    private static void assertClientClosedWithoutResult(int port) {
        def socket = connect(port)
        try {
            def writer = new PrintWriter(new OutputStreamWriter(socket.outputStream), true)
            def reader = new BufferedReader(new InputStreamReader(socket.inputStream))
            try {
                writer.println('x')
            } catch (SocketException ignored) {
                // The server already closed this client.
                return
            }
            String line
            try {
                line = reader.readLine()
            } catch (SocketException ignored) {
                // The server closed this client while the read was in progress.
                return
            }
            assert line == null : "expected the failed connection to close, got: ${line}"
        } finally {
            socket.close()
        }
    }

    private static void awaitErr(ByteArrayOutputStream err, String expected) {
        long deadline = System.currentTimeMillis() + 10000
        while (!err.toString().contains(expected) && System.currentTimeMillis() < deadline) {
            Thread.sleep(20)
        }
        assert err.toString().contains(expected) : err.toString()
    }

    private static int awaitPort(GroovySocketServer server) {
        long deadline = System.currentTimeMillis() + 10000
        int port = -1
        while (port <= 0 && System.currentTimeMillis() < deadline) {
            port = server.localPort
            if (port <= 0) {
                Thread.sleep(10)
            }
        }
        assert port > 0 : 'listener did not bind'
        return port
    }

    private static List<String> exchange(int port, List<String> lines) {
        def socket = connect(port)
        try {
            def writer = new PrintWriter(new OutputStreamWriter(socket.outputStream), true)
            def reader = new BufferedReader(new InputStreamReader(socket.inputStream))
            return lines.collect { line ->
                writer.println(line)
                def answer = reader.readLine()
                assert answer != null : "no response for ${line}"
                return answer
            }
        } finally {
            socket.close()
        }
    }

    private static Socket connect(int port) {
        long deadline = System.currentTimeMillis() + 10000
        ConnectException last = null
        while (System.currentTimeMillis() < deadline) {
            try {
                def socket = new Socket()
                socket.connect(new InetSocketAddress(InetAddress.loopbackAddress, port), 200)
                socket.soTimeout = 10000
                return socket
            } catch (ConnectException e) {
                last = e
                Thread.sleep(20)
            }
        }
        throw last ?: new ConnectException('unable to connect')
    }
}
