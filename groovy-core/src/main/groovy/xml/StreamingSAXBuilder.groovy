/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.xml

import groovy.xml.streamingmarkupsupport.AbstractStreamingBuilder
import groovy.xml.streamingmarkupsupport.StreamingMarkupWriter
import groovy.xml.streamingmarkupsupport.BaseMarkupBuilder


import org.xml.sax.helpers.AttributesImpl
import org.xml.sax.ext.LexicalHandler

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
                                     def buf = new StringBuffer()
                                        
                                        instruction.each { name, value ->
                                          if (value.toString().contains('"')) {
                                            buf.append(" $name='$value'")
                                          } else {
                                            buf.append(" $name=\"$value\"" )                                        
                                          }
                                        }
                                        contentHandler.processingInstruction(target, buf.toString())
                                      } else {
                                        contentHandler.processingInstruction(target, instruction)
                                      }
                                    }
                               }
        def noopClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
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
                                          def chars = it.toCharArray()
                                          contentHandler.characters(chars, 0, chars.size())
                                        }
                                      }
                                  }
                                }
        def tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, contentHandler ->
                                  def attributes = new AttributesImpl()
          
                                  attrs.each {key, value ->
                                          if (key.contains('$')) {
                                              def parts = key.tokenize('$')
          
                                              if (namespaces.containsKey(parts[0])) {
                                                  def namespaceUri = namespaces[parts[0]]          
                                                  attributes.addAttribute(namespaceUri, parts[1], "${parts[0]}:${parts[1]}", "CDATA", value)
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
                                      attributes.addAttribute("http://www.w3.org/2000/xmlns/", key, "xmlns:${key}", "CDATA", value)
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
                                              def chars = it.toCharArray()
                                              contentHandler.characters(chars, 0, chars.size())
                                            }
                                          }
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

        def builder = null

        StreamingSAXBuilder() {
            specialTags.putAll(['yield':noopClosure,
                                'yieldUnescaped':noopClosure,
                                'comment':commentClosure,
                                'pi':piClosure])

            def nsSpecificTags = [':'                                          : [tagClosure, tagClosure, [:]],    // the default namespace
                                  'http://www.w3.org/XML/1998/namespace'           : [tagClosure, tagClosure, [:]],
                                  'http://www.codehaus.org/Groovy/markup/keywords' : [badTagClosure, tagClosure, specialTags]]

            this.builder = new BaseMarkupBuilder(nsSpecificTags)
        }

        public bind(closure) {
            def boundClosure = this.builder.bind(closure)

            return {contentHandler ->
                boundClosure.trigger = contentHandler
            }
        }
    }
