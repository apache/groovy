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
package org.codehaus.groovy.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for {@link URLStreams}: uncached streams and last-modified lookups
 * must not pin {@code file:} / {@code jar:} handles.
 */
final class URLStreamsTest {

    @TempDir
    Path tempDir

    @Test
    void getUncachedLastModifiedDisablesCachesAndClosesTheStream() {
        def handler = new TrackingHandler(1234L)
        URL url = urlFor(handler)
        assert URLStreams.getUncachedLastModified(url) == 1234L
        assert handler.useCaches == Boolean.FALSE
        assert handler.lastModifiedCalled
        assert handler.inputStreamClosed
    }

    @Test
    void getUncachedLastModifiedPropagatesFailureToOpenTheStream() {
        def handler = new TrackingHandler(99L)
        handler.inputStreamError = new IOException('cannot open')
        def error = shouldFail(IOException) {
            URLStreams.getUncachedLastModified(urlFor(handler))
        }
        assert error.message == 'cannot open'
        assert handler.useCaches == Boolean.FALSE
        assert !handler.lastModifiedCalled
        assert !handler.inputStreamClosed
    }

    @Test
    void getUncachedLastModifiedReturnsTheValueWhenClosingTheStreamFails() {
        def handler = new TrackingHandler(99L)
        handler.closeError = new IOException('cannot close')
        assert URLStreams.getUncachedLastModified(urlFor(handler)) == 99L
        assert handler.useCaches == Boolean.FALSE
        assert handler.lastModifiedCalled
        assert handler.inputStreamClosed
    }

    @Test
    void getUncachedLastModifiedClosesTheStreamWhenLastModifiedFails() {
        def handler = new TrackingHandler(0L)
        handler.lastModifiedError = new IOException('cannot stat')
        def error = shouldFail(IOException) {
            URLStreams.getUncachedLastModified(urlFor(handler))
        }
        assert error.message == 'cannot stat'
        assert handler.inputStreamClosed
    }

    @Test
    void getUncachedLastModifiedPropagatesOpenFailureBeforeLastModified() {
        def handler = new TrackingHandler(0L)
        handler.lastModifiedError = new IOException('cannot stat')
        handler.inputStreamError = new IOException('cannot open')
        def error = shouldFail(IOException) {
            URLStreams.getUncachedLastModified(urlFor(handler))
        }
        assert error.message == 'cannot open'
        assert !handler.inputStreamClosed
    }

    @Test
    void getUncachedLastModifiedReadsAFileUrlWithoutPinningIt() {
        Path file = tempDir.resolve('a.txt')
        Files.writeString(file, 'x')
        long expected = Files.getLastModifiedTime(file).toMillis()
        assert URLStreams.getUncachedLastModified(file.toUri().toURL()) == expected
        Files.delete(file)
    }

    @Test
    void getUncachedLastModifiedDoesNotPinAJarFile() {
        Path jar = tempDir.resolve('sources.jar')
        new JarOutputStream(Files.newOutputStream(jar)).withCloseable { jos ->
            jos.putNextEntry(new JarEntry('Foo.groovy'))
            jos.write('class Foo {}'.bytes)
            jos.closeEntry()
        }
        URL url = new URL('jar:' + jar.toUri().toURL().toExternalForm() + '!/Foo.groovy')
        assert URLStreams.getUncachedLastModified(url) > 0
        // Windows cannot delete a JAR that JarURLConnection still holds
        Files.delete(jar)
    }

    @Test
    void openUncachedStreamDisablesCachesAndReturnsTheBody() {
        def handler = new TrackingHandler(0L)
        URLStreams.openUncachedStream(urlFor(handler)).withCloseable { stream ->
            assert stream.readAllBytes() == new byte[0]
        }
        assert handler.useCaches == Boolean.FALSE
        assert handler.inputStreamClosed
    }

    private static URL urlFor(TrackingHandler handler) {
        new URL('cnrtrack', 'localhost', -1, '/x', handler)
    }

    private static class TrackingHandler extends URLStreamHandler {
        final long lastModified
        Boolean useCaches
        boolean lastModifiedCalled
        boolean inputStreamClosed
        IOException lastModifiedError
        IOException inputStreamError
        IOException closeError

        TrackingHandler(long lastModified) {
            this.lastModified = lastModified
        }

        @Override
        protected URLConnection openConnection(URL u) {
            new TrackingConnection(u, this)
        }
    }

    private static class TrackingConnection extends URLConnection {
        private final TrackingHandler handler

        TrackingConnection(URL url, TrackingHandler handler) {
            super(url)
            this.handler = handler
        }

        @Override
        void connect() {
            connected = true
        }

        @Override
        void setUseCaches(boolean usecaches) {
            handler.useCaches = usecaches
            super.setUseCaches(usecaches)
        }

        @Override
        long getLastModified() {
            handler.lastModifiedCalled = true
            if (handler.lastModifiedError != null) {
                throw handler.lastModifiedError
            }
            return handler.lastModified
        }

        @Override
        InputStream getInputStream() {
            if (handler.inputStreamError != null) {
                throw handler.inputStreamError
            }
            return new FilterInputStream(InputStream.nullInputStream()) {
                @Override
                void close() throws IOException {
                    handler.inputStreamClosed = true
                    if (handler.closeError != null) {
                        throw handler.closeError
                    }
                    super.close()
                }
            }
        }
    }
}
