package org.codehaus.groovy.sandbox.markup
/*

Copyright 2004 (C) John Wilson. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/

import org.xml.sax.helpers.AttributesImpl
import org.xml.sax.ext.LexicalHandler

    class StreamingSAXBuilder extends AbstractStreamingBuilder {
        @Property pendingStack = []
        @Property commentClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
                            if (contentHandler instanceof LexicalHandler) {
                                contentHandler.comment(body.toCharArray(), 0, body.length())
                            }
                         }
        @Property noopClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
                        if (body instanceof Closure) {
                            body()
                        } else {
                            contentHandler.characters(body.toCharArray(), 0, body.length())
                        }
                      }
        @Property tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
                        def attributes = new AttributesImpl()

                        attrs.each {key, value ->
                                if (key.contains('$')) {
                                    def parts = key.tokenize('$')

                                    if (namespaces.containsKey(parts[0])) {
                                        def namespaceUri = namespaces[parts[0]]

//                                      attributes.addAttribute(namespaceUri, parts[1], "${parts[0]}:${parts[1]}", "CDATA", value)
// workround for bug GROOVY-309
                                        attributes.addAttribute(namespaceUri, parts[1], "${parts[0]}:${parts[1]}".toString(), "CDATA", value)
                                    } else {
                                        throw new GroovyRuntimeException("bad attribute namespace tag in ${key}")
                                    }
                                } else {
                                    attributes.addAttribute("", key, key, "CDATA", value)
                                }
                          }

                        def hiddenNamespaces = [:]

                        pendingNamespaces.each {key, value ->
                            hiddenNamespaces[key] = namespaces[key]
                            namespaces[key] = value
//                            attributes.addAttribute("http://www.w3.org/2000/xmlns/", key, "xmlns:${key}", "CDATA", value)
// workround for bug GROOVY-309
                            attributes.addAttribute("http://www.w3.org/2000/xmlns/", key, "xmlns:${key}".toString(), "CDATA", value)
                                contentHandler.startPrefixMapping(key, value)
                        }

                        // setup the tag info

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

                            if (body instanceof Closure) {
                                body()
                            } else {
                                contentHandler.characters(body.toCharArray(), 0, body.length())
                            }

                            pendingNamespaces.clear()
                            pendingNamespaces.putAll pendingStack.pop()
                        }

                        contentHandler.endElement(uri, tag, qualifiedName)

                        hiddenNamespaces.each {key, value ->
                                                    contentHandler.endPrefixMapping(key)

                                                    if (value == null) {
                                                        namespaces.remove key
                                                    } else {
                                                        namespaces[key] = value
                                                    }
                                               }
                    }

        @Property builder = null

        StreamingSAXBuilder() {
            def specialTags = [:]
            specialTags.putAll(['yield':noopClosure,
                                   'yieldUnescaped':noopClosure,
                                   'comment':commentClosure])

            def nsSpecificTags = [':'                                          : [tagClosure, tagClosure, [:]],    // the default namespace
                              'http://www.w3.org/XML/1998/namespace'           : [tagClosure, tagClosure, [:]],
                              'http://www.codehaus.org/Groovy/markup/keywords' : [badTagClosure, tagClosure, specialTags]]

            this.builder = new BaseMarkupBuilder(nsSpecificTags)
        }

        @Property bind(closure) {
            boundClosure = this.builder.bind(closure)

            return {contentHandler ->
                boundClosure.trigger = contentHandler
            }
        }
    }
