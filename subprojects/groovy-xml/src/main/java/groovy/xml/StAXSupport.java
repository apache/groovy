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
package groovy.xml;

import groovy.lang.GroovyRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * StAX streaming helpers backing the public {@link XmlUtil#events} and
 * {@link XmlUtil#streamElements} methods. Package-private — callers should
 * use the {@link XmlUtil} entry points.
 */
final class StAXSupport {

    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    private StAXSupport() {
    }

    static Stream<XMLEvent> events(Reader reader, boolean allowDocTypeDeclaration) {
        XMLEventReader eventReader = newEventReader(reader, allowDocTypeDeclaration);
        Spliterator<XMLEvent> spliterator = Spliterators.spliteratorUnknownSize(
                new EventIterator(eventReader),
                Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false).onClose(() -> closeQuietly(eventReader, reader));
    }

    static Stream<org.w3c.dom.Node> streamElements(Reader reader,
                                                   String namespaceURI,
                                                   String localName,
                                                   boolean allowDocTypeDeclaration) {
        if (localName == null) {
            throw new IllegalArgumentException("localName must not be null");
        }
        XMLEventReader eventReader = newEventReader(reader, allowDocTypeDeclaration);
        DocumentBuilder docBuilder = newDocumentBuilder(allowDocTypeDeclaration);
        SubtreeSpliterator spliterator = new SubtreeSpliterator(eventReader, docBuilder, namespaceURI, localName);
        return StreamSupport.stream(spliterator, false).onClose(() -> closeQuietly(eventReader, reader));
    }

    private static XMLEventReader newEventReader(Reader reader, boolean allowDocTypeDeclaration) {
        XMLInputFactory factory = FactorySupport.createXMLInputFactory(allowDocTypeDeclaration);
        try {
            return factory.createXMLEventReader(reader);
        } catch (XMLStreamException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    private static DocumentBuilder newDocumentBuilder(boolean allowDocTypeDeclaration) {
        try {
            return FactorySupport.createDocumentBuilderFactory(allowDocTypeDeclaration).newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    private static void closeQuietly(XMLEventReader eventReader, Reader sourceReader) {
        try {
            eventReader.close();
        } catch (XMLStreamException ignored) {
            // best-effort close
        }
        try {
            sourceReader.close();
        } catch (java.io.IOException ignored) {
            // best-effort close
        }
    }

    private static final class EventIterator implements Iterator<XMLEvent> {
        private final XMLEventReader reader;

        EventIterator(XMLEventReader reader) {
            this.reader = reader;
        }

        @Override
        public boolean hasNext() {
            return reader.hasNext();
        }

        @Override
        public XMLEvent next() {
            try {
                return reader.nextEvent();
            } catch (XMLStreamException e) {
                throw new GroovyRuntimeException(e);
            }
        }
    }

    private static final class SubtreeSpliterator implements Spliterator<org.w3c.dom.Node> {
        private final XMLEventReader reader;
        private final DocumentBuilder docBuilder;
        private final String targetNamespaceURI; // null = match any namespace
        private final String targetLocalName;

        SubtreeSpliterator(XMLEventReader reader, DocumentBuilder docBuilder,
                           String targetNamespaceURI, String targetLocalName) {
            this.reader = reader;
            this.docBuilder = docBuilder;
            this.targetNamespaceURI = targetNamespaceURI;
            this.targetLocalName = targetLocalName;
        }

        @Override
        public boolean tryAdvance(Consumer<? super org.w3c.dom.Node> action) {
            try {
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event.isStartElement() && matches(event.asStartElement().getName())) {
                        action.accept(buildSubtree(event.asStartElement()));
                        return true;
                    }
                }
                return false;
            } catch (XMLStreamException e) {
                throw new GroovyRuntimeException(e);
            }
        }

        private boolean matches(QName name) {
            if (!targetLocalName.equals(name.getLocalPart())) return false;
            if (targetNamespaceURI == null) return true;
            return targetNamespaceURI.equals(name.getNamespaceURI());
        }

        private org.w3c.dom.Node buildSubtree(StartElement first) throws XMLStreamException {
            Document doc = docBuilder.newDocument();
            Element root = createElement(doc, first);
            copyAttributes(root, first);
            copyNamespaces(root, first);
            Element current = root;
            int depth = 1;
            while (depth > 0 && reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement start = event.asStartElement();
                    Element child = createElement(doc, start);
                    copyAttributes(child, start);
                    copyNamespaces(child, start);
                    current.appendChild(child);
                    current = child;
                    depth++;
                } else if (event.isEndElement()) {
                    depth--;
                    if (depth > 0) {
                        current = (Element) current.getParentNode();
                    }
                } else if (event.isCharacters()) {
                    Characters chars = event.asCharacters();
                    if (chars.isCData()) {
                        current.appendChild(doc.createCDATASection(chars.getData()));
                    } else {
                        current.appendChild(doc.createTextNode(chars.getData()));
                    }
                } else if (event.getEventType() == XMLEvent.COMMENT) {
                    current.appendChild(doc.createComment(((javax.xml.stream.events.Comment) event).getText()));
                } else if (event.getEventType() == XMLEvent.PROCESSING_INSTRUCTION) {
                    javax.xml.stream.events.ProcessingInstruction pi = (javax.xml.stream.events.ProcessingInstruction) event;
                    current.appendChild(doc.createProcessingInstruction(pi.getTarget(), pi.getData()));
                }
            }
            return root;
        }

        private static Element createElement(Document doc, StartElement start) {
            QName name = start.getName();
            String prefix = name.getPrefix();
            String qualifiedName = (prefix == null || prefix.isEmpty())
                    ? name.getLocalPart()
                    : prefix + ":" + name.getLocalPart();
            String ns = name.getNamespaceURI();
            if (ns != null && !ns.isEmpty()) {
                return doc.createElementNS(ns, qualifiedName);
            }
            return doc.createElement(qualifiedName);
        }

        private static void copyAttributes(Element element, StartElement start) {
            Iterator<Attribute> attrs = start.getAttributes();
            while (attrs.hasNext()) {
                Attribute attr = attrs.next();
                QName qn = attr.getName();
                String prefix = qn.getPrefix();
                String qualifiedName = (prefix == null || prefix.isEmpty())
                        ? qn.getLocalPart()
                        : prefix + ":" + qn.getLocalPart();
                String ns = qn.getNamespaceURI();
                if (ns != null && !ns.isEmpty()) {
                    element.setAttributeNS(ns, qualifiedName, attr.getValue());
                } else {
                    element.setAttribute(qualifiedName, attr.getValue());
                }
            }
        }

        private static void copyNamespaces(Element element, StartElement start) {
            Iterator<Namespace> namespaces = start.getNamespaces();
            while (namespaces.hasNext()) {
                Namespace ns = namespaces.next();
                String prefix = ns.getPrefix();
                String name = (prefix == null || prefix.isEmpty()) ? "xmlns" : "xmlns:" + prefix;
                element.setAttributeNS(XMLNS_URI, name, ns.getNamespaceURI());
            }
        }

        @Override
        public Spliterator<org.w3c.dom.Node> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return ORDERED | NONNULL;
        }
    }
}
