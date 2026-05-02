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

import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.stream.events.XMLEvent

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

class XmlStreamingTest {

    @Test
    void eventsYieldsExpectedSequence() {
        def xml = '<root><a>1</a><a>2</a></root>'
        def types = XmlUtil.events(new StringReader(xml)).collect { XMLEvent e -> e.eventType }
        // First and last are START/END_DOCUMENT; in between we see start/chars/end for each element.
        assertEquals(XMLEvent.START_DOCUMENT, types.first())
        assertEquals(XMLEvent.END_DOCUMENT, types.last())
        assertTrue(types.contains(XMLEvent.START_ELEMENT))
        assertTrue(types.contains(XMLEvent.CHARACTERS))
        assertTrue(types.contains(XMLEvent.END_ELEMENT))
    }

    @Test
    void streamElementsMatchesByLocalName() {
        def xml = '<feed><item>a</item><item>b</item><item>c</item></feed>'
        def items = XmlUtil.streamElements(new StringReader(xml), 'item').toList()
        assertEquals(3, items.size())
        items.each { Node n ->
            assertTrue(n instanceof Element)
            assertEquals('item', n.tagName)
        }
        assertEquals(['a', 'b', 'c'], items*.textContent)
    }

    @Test
    void streamElementsHandlesAttributesAndNestedContent() {
        def xml = '''<feed>
  <item id="1">
    <title>First</title>
    <body>Hello <em>world</em></body>
  </item>
  <item id="2">
    <title>Second</title>
  </item>
</feed>'''
        def items = XmlUtil.streamElements(new StringReader(xml), 'item').toList()
        assertEquals(2, items.size())
        assertEquals('1', items[0].getAttribute('id'))
        assertEquals('2', items[1].getAttribute('id'))
        // Nested content preserved
        def emNodes = items[0].getElementsByTagName('em')
        assertEquals(1, emNodes.length)
        assertEquals('world', emNodes.item(0).textContent)
    }

    @Test
    void streamElementsMatchesByNamespace() {
        def xml = '''<root xmlns:a="urn:a" xmlns:b="urn:b">
  <a:item>match</a:item>
  <b:item>skip</b:item>
  <a:item>match2</a:item>
</root>'''
        def matched = XmlUtil.streamElements(new StringReader(xml), 'urn:a', 'item').toList()
        assertEquals(2, matched.size())
        assertEquals(['match', 'match2'], matched*.textContent)
    }

    @Test
    void streamElementsMatchesOuterOnlyForNestedSameName() {
        // Nested same-name elements must not double-emit: the outer match is consumed
        // along with everything inside, including the inner match.
        def xml = '<root><item>outer<item>inner</item>tail</item></root>'
        def items = XmlUtil.streamElements(new StringReader(xml), 'item').toList()
        assertEquals(1, items.size())
        // Inner item is preserved as a child node, not re-emitted.
        def innerItems = items[0].getElementsByTagName('item')
        assertEquals(1, innerItems.length)
        assertEquals('inner', innerItems.item(0).textContent)
    }

    @Test
    void streamElementsClosesReaderOnStreamClose() {
        boolean closed = false
        def reader = new StringReader('<root><item>x</item></root>') {
            @Override
            void close() {
                closed = true
                super.close()
            }
        }
        def stream = XmlUtil.streamElements(reader, 'item')
        stream.findFirst()
        stream.close()
        assertTrue(closed, 'expected reader to be closed when stream is closed')
    }

    @Test
    void streamElementsAllowsDoctypeWhenOptedIn() {
        def xml = '''<?xml version="1.0"?>
<!DOCTYPE root [<!ELEMENT root ANY><!ELEMENT item (#PCDATA)>]>
<root><item>x</item></root>'''
        def items = XmlUtil.streamElements(new StringReader(xml), null, 'item', true).toList()
        assertEquals(1, items.size())
        assertEquals('x', items[0].textContent)
    }

    @Test
    void streamElementsEmptyWhenNoMatch() {
        def xml = '<root><a>1</a></root>'
        def items = XmlUtil.streamElements(new StringReader(xml), 'nonexistent').toList()
        assertTrue(items.isEmpty())
    }

    @Test
    void streamElementsRejectsNullLocalName() {
        assertThrows(IllegalArgumentException, {
            XmlUtil.streamElements(new StringReader('<root/>'), null).toList()
        })
    }

    @Test
    void streamElementsScalesToManyRecords() {
        // Synthesize a moderately large feed and verify we can stream through it
        // without OOM. Cap is small enough to run quickly but exercises the streaming path.
        def sb = new StringBuilder('<feed>')
        int count = 5_000
        count.times { sb.append("<rec>${it}</rec>") }
        sb.append('</feed>')
        long sum = XmlUtil.streamElements(new StringReader(sb.toString()), 'rec')
                .mapToLong { Node n -> Long.parseLong(n.textContent) }
                .sum()
        // Sum of 0..4999
        assertEquals((long) (count * (count - 1) / 2), sum)
    }
}
