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
import org.junit.jupiter.api.Test

import java.net.InetSocketAddress

import static groovy.test.GroovyAssert.shouldFail

/**
 * Covers where {@code groovy -l} binds its listening socket. The server itself is not
 * started here: its accept loop runs on a non-daemon thread that nothing can stop, so a
 * test that started one would outlive the test worker.
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
}
