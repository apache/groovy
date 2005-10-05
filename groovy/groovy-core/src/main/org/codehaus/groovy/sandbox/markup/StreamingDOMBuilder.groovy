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

import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node

import org.codehaus.groovy.sandbox.markup.streamingmarkupsupport.AbstractStreamingBuilder
import org.codehaus.groovy.sandbox.markup.streamingmarkupsupport.StreamingMarkupWriter
import org.codehaus.groovy.sandbox.markup.streamingmarkupsupport.BaseMarkupBuilder

	
    class StreamingDOMBuilder extends AbstractStreamingBuilder {
        @Property pendingStack = []
        @Property commentClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
                            def comment = dom.document.createComment(body)

                            if (comment != null) {
                                dom.element.appendChild(comment)
                            }
                         }
        @Property piClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
                                attrs.each {target, instruction ->
                                   def pi = null
                                   if (instruction instanceof Map) {
                                   def buf = new StringBuffer()
                                      
                                      instruction.each { name, value ->
                                        if (value.toString().contains('"')) {
                                          buf.append(" $name='$value'")
                                        } else {
                                          buf.append(" $name=\"$value\"" )                                        
                                        }
                                      }
                                      pi = dom.document.createProcessingInstruction(target, buf.toString())
                                    } else {
                                      pi =dom.document.createProcessingInstruction(target, instruction)
                                    }
                                    if (pi != null) {
                                      dom.element.appendChild(pi)
                                    }
                                  }
                             }
        @Property noopClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
                        if (body instanceof Closure) {
                          def body1 = body.clone()
                          
                          body1.delegate = this
                          body1()
                        } else if (body instanceof Buildable) {
                              body.build(doc)
                        } else {
                          body.each {
                            if (it instanceof Closure) {
                              def body1 = it.clone()
                              
                              body1.delegate = this
                              body1()
                            } else if (it instanceof Buildable) {
                              it.build(doc)
                            } else {
                              dom.element.appendChild(dom.document.createTextNode(it))
                            }
                          }
                        }
                      }
        @Property tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, dom ->
                        def attributes = []
                        def nsAttributes = []

                        attrs.each {key, value ->
                                if (key.contains('$')) {
                                    def parts = key.tokenize('$')
                                    def namespaceUri = null

                                    if (namespaces.containsKey(parts[0])) {
                                        namespaceUri = namespaces[parts[0]]

//                                    nsAttributes.add([namespaceUri, "${parts[0]}:${parts[1]}", value])
// workround for bug GROOVY-309
                                    nsAttributes.add([namespaceUri, "${parts[0]}:${parts[1]}".toString(), value])
                                    } else {
                                        throw new GroovyRuntimeException("bad attribute namespace tag in ${key}")
                                    }
                                } else {
                                attributes.add([key, value])
                                }
                          }

                        def hiddenNamespaces = [:]

                        pendingNamespaces.each {key, value ->
                            hiddenNamespaces[key] = namespaces[key]
                            namespaces[key] = value
//                            nsAttributes.add(["http://www.w3.org/2000/xmlns/", "xmlns:${key}", value])
// workround for bug GROOVY-309
                            nsAttributes.add(["http://www.w3.org/2000/xmlns/", "xmlns:${key}".toString(), value])
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
                            pendingStack.add pendingNamespaces.clone()
                            pendingNamespaces.clear()

                            if (body instanceof Closure) {
                              def body1 = body.clone()
                              
                              body1.delegate = this
                              body1()
                            } else if (body instanceof Buildable) {
                                  body.build(doc)
                            } else {
                              body.each {
                                if (it instanceof Closure) {
                                  def body1 = it.clone()
                                  
                                  body1.delegate = this
                                  body1()
                                } else if (it instanceof Buildable) {
                                  it.build(doc)
                                } else {
                                  dom.element.appendChild(dom.document.createTextNode(it))
                                }
                              }
                            }

                            pendingNamespaces.clear()
                            pendingNamespaces.putAll pendingStack.pop()
                        }

                        dom.element = dom.element.getParentNode()

                        hiddenNamespaces.each {key, value ->
                                                    if (value == null) {
                                                        namespaces.remove key
                                                    } else {
                                                        namespaces[key] = value
                                                    }
                                               }
                    }

        @Property builder = null

        StreamingDOMBuilder() {
            specialTags.putAll(['yield':noopClosure,
                                'yieldUnescaped':noopClosure,
                                'comment':commentClosure,
                                'pi':piClosure])

            def nsSpecificTags = [':'                                          : [tagClosure, tagClosure, [:]],    // the default namespace
                              'http://www.w3.org/XML/1998/namespace'           : [tagClosure, tagClosure, [:]],
                              'http://www.codehaus.org/Groovy/markup/keywords' : [badTagClosure, tagClosure, specialTags]]

            this.builder = new BaseMarkupBuilder(nsSpecificTags)
        }

        @Property bind(closure) {
            @Property boundClosure = this.builder.bind(closure)

            return {
                if (it instanceof Node) {
                    def document = it.getOwnerDocument()

                    boundClosure.trigger = ['document' : document, 'element' : it]

                    return document

                } else {
                    def newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()

                    boundClosure.trigger = ['document' : newDocument, 'element' : newDocument]

                    return newDocument
                }
            }
        }
    }
