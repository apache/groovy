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
package groovy.xml

import groovy.xml.streamingmarkupsupport.StreamingMarkupWriter
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

class StreamingMarkupWriterTest {

    // A high surrogate left dangling when the stream ends (no following low surrogate) is malformed
    // UTF-16. It cannot be detected at write time, so it must be rejected at finalization rather
    // than silently dropped. StreamingMarkupBuilder finalizes via flush().
    @Test
    void danglingHighSurrogateRejectedOnFlush() {
        def w = new StreamingMarkupWriter(new StringWriter())
        w.write((int) 0xD835)
        assertThrows(IOException) { w.flush() }
    }

    // The same dangling high surrogate is also rejected at close(), for callers that close without
    // a final flush.
    @Test
    void danglingHighSurrogateRejectedOnClose() {
        def w = new StreamingMarkupWriter(new StringWriter())
        w.write((int) 0xD835)
        assertThrows(IOException) { w.close() }
    }

    // A lone high surrogate yielded through StreamingMarkupBuilder must fail loudly when streamed to
    // a writer, instead of silently dropping the character. (Writable.toString() swallows IOException
    // and returns "" by design, so this must be asserted on the writeTo/streaming path.)
    @Test
    void builderRejectsDanglingHighSurrogate() {
        def writable = new StreamingMarkupBuilder().bind { mkp.yield '\uD835' }
        assertThrows(IOException) { writable.writeTo(new StringWriter()) }
    }

    // Well-formed content still flushes cleanly.
    @Test
    void wellFormedContentFlushesCleanly() {
        def sw = new StringWriter()
        def w = new StreamingMarkupWriter(sw)
        w.write('plain text')
        w.flush()
        assertEquals('plain text', sw.toString())
    }

    // A well-formed surrogate pair (U+1D400) is encoded as a single numeric reference.
    @Test
    void validSurrogatePairIsEncoded() {
        def sw = new StringWriter()
        def w = new StreamingMarkupWriter(sw)
        w.write('𝐀')
        w.flush()
        assertEquals('&#x1d400;', sw.toString())
    }

    // A low surrogate with no preceding high surrogate is malformed UTF-16 and must be
    // rejected, mirroring the existing rejection of a high surrogate not followed by a low one.
    @Test
    void loneLowSurrogateIsRejected() {
        def w = new StreamingMarkupWriter(new StringWriter())
        assertThrows(IOException) { w.write('\uDC00') }
    }

    // Existing behavior: a high surrogate not followed by a low surrogate is rejected.
    @Test
    void loneHighSurrogateIsRejected() {
        def w = new StreamingMarkupWriter(new StringWriter())
        assertThrows(IOException) { w.write('\uD835' + 'A') }
    }
}
