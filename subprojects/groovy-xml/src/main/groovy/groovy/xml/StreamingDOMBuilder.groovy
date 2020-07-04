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

import groovy.xml.streamingmarkupsupport.AbstractStreamingBuilder
import groovy.xml.streamingmarkupsupport.BaseMarkupBuilder
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilderFactory

class StreamingDOMBuilder extends AbstractStreamingBuilder {
    def pendingStack = []
    def defaultNamespaceStack = [""]
    def commentClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
        def comment = dom.document.createComment(body)
        if (comment != null) {
            dom.element.appendChild(comment)
        }
    }
    def piClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
        attrs.each {target, instruction ->
            def pi = null
            if (instruction instanceof Map) {
                pi = dom.document.createProcessingInstruction(target, toMapStringClosure(instruction))
            } else {
                pi = dom.document.createProcessingInstruction(target, instruction)
            }
            if (pi != null) {
                dom.element.appendChild(pi)
            }
        }
    }
    def noopClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
        if (body instanceof Closure) {
            def body1 = body.clone()
            body1.delegate = doc
            body1(doc)
        } else if (body instanceof Buildable) {
            body.build(doc)
        } else if (body != null) {
            body.each {
                if (it instanceof Closure) {
                    def body1 = it.clone()
                    body1.delegate = doc
                    body1(doc)
                } else if (it instanceof Buildable) {
                    it.build(doc)
                } else {
                    dom.element.appendChild(dom.document.createTextNode(it))
                }
            }
        }
    }
    def tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
        def attributes = []
        def nsAttributes = []
        def defaultNamespace = defaultNamespaceStack.last()

        attrs.each {key, value ->
            if (key.contains('$')) {
                def parts = key.tokenize('$')
                def namespaceUri = null

                if (namespaces.containsKey(parts[0])) {
                    namespaceUri = namespaces[parts[0]]

                    nsAttributes.add([namespaceUri, "${parts[0]}:${parts[1]}", "$value"])

                } else {
                    throw new GroovyRuntimeException("bad attribute namespace tag in ${key}")
                }
            } else {
                attributes.add([key, value])
            }
        }

        def hiddenNamespaces = [:]

        pendingNamespaces.each {key, value ->
            if (key == ':') {
                defaultNamespace = "$value"
                nsAttributes.add(["http://www.w3.org/2000/xmlns/", "xmlns", defaultNamespace])
            } else {
                hiddenNamespaces[key] = namespaces[key]
                namespaces[key] = value
                nsAttributes.add(["http://www.w3.org/2000/xmlns/", "xmlns:${key}", "$value"])
            }
        }

        // setup the tag info

        def uri = defaultNamespace
        def qualifiedName = tag

        if (prefix != "") {
            if (namespaces.containsKey(prefix)) {
                uri = namespaces[prefix]
            } else if (pendingNamespaces.containsKey(prefix)) {
                uri = pendingNamespaces[prefix]
            } else {
                throw new GroovyRuntimeException("Namespace prefix: ${prefix} is not bound to a URI")
            }
            if (prefix != ":") {
                qualifiedName = prefix + ":" + tag
            }
        }

        def element = dom.document.createElementNS(uri, qualifiedName)

        nsAttributes.each {
            element.setAttributeNS(it[0], it[1], it[2])
        }
        attributes.each {
            element.setAttribute(it[0], it[1])
        }

        dom.element.appendChild(element)
        dom.element = element

        if (body != null) {
            defaultNamespaceStack.push defaultNamespace
            pendingStack.add pendingNamespaces.clone()
            pendingNamespaces.clear()

            if (body instanceof Closure) {
                def body1 = body.clone()

                body1.delegate = doc
                body1(doc)
            } else if (body instanceof Buildable) {
                body.build(doc)
            } else {
                body.each {
                    if (it instanceof Closure) {
                        def body1 = it.clone()

                        body1.delegate = doc
                        body1(doc)
                    } else if (it instanceof Buildable) {
                        it.build(doc)
                    } else {
                        dom.element.appendChild(dom.document.createTextNode(it))
                    }
                }
            }

            pendingNamespaces.clear()
            pendingNamespaces.putAll pendingStack.pop()
            defaultNamespaceStack.pop()
        }

        dom.element = dom.element.getParentNode()

        hiddenNamespaces.each { key, value ->
            if (value == null) namespaces.remove key
            else namespaces[key] = value
        }
    }

    def builder = null

    StreamingDOMBuilder() {
        specialTags.putAll(['yield':noopClosure,
                            'yieldUnescaped':noopClosure,
                            'comment':commentClosure,
                            'pi':piClosure])
        def nsSpecificTags = [':'                                          : [tagClosure, tagClosure, [:]],    // the default namespace
                          'http://www.w3.org/2000/xmlns/'                  : [tagClosure, tagClosure, [:]],
                          'http://www.codehaus.org/Groovy/markup/keywords' : [badTagClosure, tagClosure, specialTags]]
        this.builder = new BaseMarkupBuilder(nsSpecificTags)
    }

    def bind(closure) {
        def boundClosure = this.builder.bind(closure)
        return {
            if (it instanceof Node) {
                def document = it.getOwnerDocument()
                boundClosure.trigger = ['document' : document, 'element' : it]
                return document
            }
            def dBuilder = DocumentBuilderFactory.newInstance()
            dBuilder.namespaceAware = true
            def newDocument = dBuilder.newDocumentBuilder().newDocument()
            boundClosure.trigger = ['document' : newDocument, 'element' : newDocument]
            return newDocument
        }
    }
}
