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
package org.apache.groovy.lsp.internal.workspace

import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.util.Uris
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.junit.jupiter.api.Test

final class DocumentStoreTest {

    @Test
    void openChangeClose() {
        def store = new DocumentStore()
        def uri = new File('/tmp/Hello.groovy').toURI().toString()
        def item = new TextDocumentItem(uri, 'groovy', 1, 'class A {}\n')
        store.open(item)
        assert store.size() == 1
        assert store.get(uri).text == 'class A {}\n'
        def change = new TextDocumentContentChangeEvent('class B {}\n')
        store.change(uri, 2, [change], PositionEncoding.UTF16)
        assert store.get(uri).text == 'class B {}\n'
        assert store.get(uri).version == 2
        store.close(uri)
        assert store.size() == 0
        store.clear()
    }

    @Test
    void incrementalChangeReplacesRange() {
        def doc = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'hello')
        def event = new TextDocumentContentChangeEvent(new Range(new Position(0, 1), new Position(0, 4)), 'i')
        def next = doc.apply(2, [event], PositionEncoding.UTF16)
        assert next.text == 'hio'
        assert next.version == 2
    }

    @Test
    void offsetOfClamps() {
        assert TextDocument.offsetOf('', new Position(0, 0), PositionEncoding.UTF16) == 0
        assert TextDocument.offsetOf('ab\ncd', new Position(5, 0), PositionEncoding.UTF16) == 5
        assert TextDocument.offsetOf('ab\ncd', new Position(1, 2), PositionEncoding.UTF16) == 5
    }

    @Test
    void fullRangeEmptyAndNonEmpty() {
        def empty = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, '')
        assert empty.fullRange(PositionEncoding.UTF16).end.line == 0
        def doc = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'ab\ncd')
        assert doc.fullRange(PositionEncoding.UTF16).end.line == 1
        def nl = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'ab\n')
        assert nl.fullRange(PositionEncoding.UTF16).end.line == 1
    }

    @Test
    void changeUnknownDocumentUsesLastText() {
        def store = new DocumentStore()
        def uri = 'file:///tmp/Missing.groovy'
        store.change(uri, 1, [new TextDocumentContentChangeEvent('x')], PositionEncoding.UTF16)
        assert store.get(uri).text == 'x'
        store.change(uri, 2, [], PositionEncoding.UTF16)
        assert store.get(uri).text == 'x'
        store.change('file:///tmp/EmptyChange.groovy', 1, null, PositionEncoding.UTF16)
        assert store.get('file:///tmp/EmptyChange.groovy').text == ''
    }

    @Test
    void offsetOfWalksCrlfAndClampsColumns() {
        assert TextDocument.offsetOf('ab\r\ncd', new Position(1, 0), PositionEncoding.UTF16) == 4
        assert TextDocument.offsetOf('ab\rcd', new Position(1, 0), PositionEncoding.UTF16) == 3
        def wide = TextDocument.offsetOf('ab', new Position(0, 99), PositionEncoding.UTF16)
        assert wide == 2
        def event = new TextDocumentContentChangeEvent(new Range(new Position(0, 4), new Position(0, 1)), '')
        def next = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'abcd')
                .apply(2, [event], PositionEncoding.UTF16)
        assert next.text == 'abcd'
    }

    @Test
    void urisRecognizeGroovyDocuments() {
        def uri = URI.create('file:///tmp/A.groovy')
        assert Uris.isGroovyDocument(uri, null)
        assert Uris.isGroovyDocument(URI.create('file:///tmp/A.txt'), 'groovy')
        assert !Uris.isGroovyDocument(URI.create('file:///tmp/A.txt'), 'java')
        assert Uris.isGroovyDocument(URI.create('file:///tmp/A.gsh'), null)
        assert Uris.parse('file:///tmp/A.groovy') != null
        assert Uris.normalize(URI.create('https://example.test/x')) == URI.create('https://example.test/x')
    }

    @Test
    void toGroovyRoundTrip() {
        def doc = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'abc')
        int[] groovy = doc.toGroovy(new Position(0, 2), PositionEncoding.UTF16)
        assert groovy[0] == 1
        assert groovy[1] == 3
    }
}
