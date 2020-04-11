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
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.AttributesImpl

class StreamingSAXBuilder extends AbstractStreamingBuilder {
    def pendingStack = []

    def commentClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
        if (contentHandler instanceof LexicalHandler) {
            contentHandler.comment(body.toCharArray(), 0, body.size())
        }
    }

    def piClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
        attrs.each {target, instruction ->
            if (instruction instanceof Map) {
                contentHandler.processingInstruction(target, toMapString(instruction))
            } else {
                contentHandler.processingInstruction(target, instruction)
            }
        }
    }

    def noopClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
        if (body != null) {
            processBody(body, doc, contentHandler)
        }
    }

    def tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
        def attributes = new AttributesImpl()
        attrs.each {key, value ->
            addAttributes(attributes, key, value, namespaces)
        }
        def hiddenNamespaces = [:]
        pendingNamespaces.each {key, value ->
            def k = (key == ':' ? '' : key)
            hiddenNamespaces[k] = namespaces[key]
            namespaces[k] = value
            attributes.addAttribute("http://www.w3.org/2000/xmlns/", k, "xmlns${k == '' ? '' : ":$k"}", "CDATA", "$value")
            contentHandler.startPrefixMapping(k, value)
        }
        // set up the tag info
        def uri = ""
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
        contentHandler.startElement(uri, tag, qualifiedName, attributes)
        if (body != null) {
            pendingStack.add pendingNamespaces.clone()
            pendingNamespaces.clear()
            processBody(body, doc, contentHandler)
            pendingNamespaces.clear()
            pendingNamespaces.putAll pendingStack.pop()
        }
        contentHandler.endElement(uri, tag, qualifiedName)
        hiddenNamespaces.each {key, value ->
            contentHandler.endPrefixMapping(key)
            if (value == null)
                namespaces.remove key
            else
                namespaces[key] = value
        }
    }

    private addAttributes(AttributesImpl attributes, key, value, namespaces) {
        if (key.contains('$')) {
            def parts = key.tokenize('$')
            if (namespaces.containsKey(parts[0])) {
                def namespaceUri = namespaces[parts[0]]
                attributes.addAttribute(namespaceUri, parts[1], "${parts[0]}:${parts[1]}", "CDATA", "$value")
            } else {
                throw new GroovyRuntimeException("bad attribute namespace tag in ${key}")
            }
        } else {
            attributes.addAttribute("", key, key, "CDATA", "$value")
        }
    }

    private processBody(body, doc, contentHandler) {
        if (body instanceof Closure) {
            def body1 = body.clone()
            body1.delegate = doc
            body1(doc)
        } else if (body instanceof Buildable) {
            body.build(doc)
        } else {
            body.each {
                processBodyPart(it, doc, contentHandler)
            }
        }
    }

    private processBodyPart(part, doc, contentHandler) {
        if (part instanceof Closure) {
            def body1 = part.clone()
            body1.delegate = doc
            body1(doc)
        } else if (part instanceof Buildable) {
            part.build(doc)
        } else {
            def chars = part.toCharArray()
            contentHandler.characters(chars, 0, chars.size())
        }
    }

    def builder = null

    StreamingSAXBuilder() {
        specialTags.putAll(['yield': noopClosure,
                'yieldUnescaped': noopClosure,
                'comment': commentClosure,
                'pi': piClosure])

        def nsSpecificTags = [':': [tagClosure, tagClosure, [:]],    // the default namespace
                'http://www.w3.org/XML/1998/namespace': [tagClosure, tagClosure, [:]],
                'http://www.codehaus.org/Groovy/markup/keywords': [badTagClosure, tagClosure, specialTags]]

        this.builder = new BaseMarkupBuilder(nsSpecificTags)
    }

    public bind(closure) {
        def boundClosure = this.builder.bind(closure)
        return {contentHandler ->
            contentHandler.startDocument()
            boundClosure.trigger = contentHandler
            contentHandler.endDocument()
        }
    }
}
