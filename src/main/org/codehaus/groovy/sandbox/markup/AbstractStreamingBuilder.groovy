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
	
    class AbstractStreamingBuilder {
        @Property badTagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, Object[] rest ->
                                      def uri = pendingNamespaces[prefix]
          
                                      if (uri == null) {
                                          uri = namespaces[prefix]
                                      }
          
                                      throw new GroovyRuntimeException("Tag ${tag} is not allowed in namespace ${uri}")
                                  }
        @Property namespaceSetupClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, Object[] rest ->
                                              attrs.each { key, value ->
                                                  if ( key == "") {
                                                      key = ":"    // marker for default namespace
                                                  }
          
                                                  value = value.toString()     // in case it's not a string
          
                                                  if (namespaces[key] != value) {
                                                      pendingNamespaces[key] = value
                                                  }
          
                                                  if (!namespaceSpecificTags.containsKey(value)) {
                                                      def baseEntry = namespaceSpecificTags[':']
                                                      namespaceSpecificTags[value] = [baseEntry[0], baseEntry[1], [:]].toArray()
                                                  }
                                              }
                                          }
        @Property aliasSetupClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, Object[] rest ->
                                        attrs.each { key, value ->
                                            if (value instanceof Map) {
                                                // key is a namespace prefix value is the mapping
                                                def info = null
        
                                                if (namespaces.containsKey(key)) {
                                                    info = namespaceSpecificTags[namespaces[key]]
                                                } else if (pendingNamespaces.containsKey(key)) {
                                                    info = namespaceSpecificTags[pendingNamespaces[key]]
                                                } else {
                                                    throw new GroovyRuntimeException("namespace prefix ${key} has not been declared")
                                                }
                                              value.each { from, to ->
                                                  info[2][to] = info[1].curry(from)
                                              }
                                          } else {
                                              def info = namespaceSpecificTags[':']
                                              info[2][key] = info[1].curry(value)
                                          }
                                      }
                                    }
        @Property getNamespaceClosure = {doc, pendingNamespaces, namespaces, Object[] rest -> [namespaces, pendingNamespaces]}

        @Property specialTags = ['declareNamespace':namespaceSetupClosure,
                                 'declareAlias':aliasSetupClosure,
                                 'getNamespaces':getNamespaceClosure]

        @Property builder = null
    }
