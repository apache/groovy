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
	
	class StreamingMarkupBuilder {
		pendingStack = []
		badTagClosure = {tag, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out |
							uri = pendingNamespaces[prefix]
							
							if (uri == null) {
								uri = namespaces[prefix]
							}
							
							throw new GroovyRuntimeException("Tag ${tag} is not allowed in namespace ${uri}")
						}
		commentClosure = {pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out |
							out.unescaped() << "<!--"
							out.bodyText() << body
							out.unescaped() << "-->"
						 }
		noopClosure = {pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out |
							out.bodyText() << body
					  }
		unescapedClosure = {pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out |
								out.unescaped() << body
						   }
		tagClosure = {tag, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out |
						if (prefix != "") {
							if (!(namespaces.containsKey(prefix) || pendingNamespaces.containsKey(prefix))) {
								throw new GroovyRuntimeException("Namespace prefix: ${prefix} is not bound to a URI")
							}
								
							tag = prefix + ":" + tag
						}
							
						out = out.unescaped() << "<${tag}"
						
					    attrs.each {key, value |
					    				if (key.contains '$') {
					    					parts = key.tokenize '$'
					    					
					    					if (namespaces.containsKey parts[0]) {
					    						key = parts[0] + ":" + parts[1]
					    					} else {
					    						throw new GroovyRuntimeException("bad attribute namespace tag in ${key}")
					    					} 
					    				}
					    				
										out << " ${key}='"
										out.attributeValue() << "${value}"
										out << "'"
								  	}
								  	
						hiddenNamespaces = [:]
			
						pendingNamespaces.each {key, value |
							hiddenNamespaces[key] = namespaces[key]
							namespaces[key] = value
							out << ((key == ":") ? " xmlns='" : " xmlns:${key}='")
							out.attributeValue() << "${value}"
							out << "'"
						}
												  	
						if (body == null) {
							out << "/>"
						} else {
							out << ">"
							
							pendingStack.add pendingNamespaces.clone()	  	
							pendingNamespaces.clear()
											
							out.bodyText() << body
							
							pendingNamespaces.clear()					
							pendingNamespaces.putAll pendingStack.pop()
							
							out << "</${tag}>"
						}
						
						hiddenNamespaces.each {key, value |	
													if (value == null) {
														namespaces.remove key
													} else {
														namespaces[key] = value
													}
											   }											   						
					}
		namespaceSetupClosure = {pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out |
									attrs.each { key, value |
										if ( key == "") {
											key = ":"	// marker for default namespace
										}
										
										value = value.toString() 	// in case it's not a string
										
										if (namespaces[key] != value) {
											pendingNamespaces[key] = value
										}
										
										if (!namespaceSpecificTags.containsKey value) {
											baseEntry = namespaceSpecificTags[':']
											namespaceSpecificTags[value] = [baseEntry[0], baseEntry[1], [:]].toArray()
										}
									}
								}
		aliasSetupClosure = {pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out |
								attrs.each { key, value |
									if (value instanceof Map) {
										// key is a namespace prefix value is the mapping
										info = null
										
										if (namespaces.containsKey key) {
											info = namespaceSpecificTags[namespaces[key]]
										} else if (pendingNamespaces.containsKey key) {
											info = namespaceSpecificTags[pendingNamespaces[key]]
										} else {
											throw new GroovyRuntimeException("namespace prefix ${key} has not been declared")
										}
/*

This is commented out because of a code generator bug
This means that mkp.declareAlias(xsd:['fred':'schema']) won't work
But mkp.declareAlias(jim:'harry') will work
										
										value.each { from, to |
											info[2][to] = info[1].curry(from)
										}*/
									} else {
										info = namespaceSpecificTags[':']
										info[2][key] = info[1].curry(value)
									}
								}
							}
											
		specialTags = ['yield':noopClosure,
		               'yieldUnescaped':unescapedClosure,
		               'comment':commentClosure,
		               'declareNamespace':namespaceSetupClosure,
		               'declareAlias':aliasSetupClosure]
		               
		nsSpecificTags = [':' : [tagClosure, tagClosure, [:]],	// the default namespace
								 'http://www.w3.org/XML/1998/namespace' : [tagClosure, tagClosure, [:]],
		                         'http://www.codehaus.org/Groovy/markup/keywords' : [badTagClosure, tagClosure, specialTags]]
		
		builder = null
		
		StreamingMarkupBuilder() {
			this.builder = new BaseMarkupBuilder(this.nsSpecificTags)
		}
		
		bind(closure) {
			boundClosure = this.builder.bind closure;
			
			{out |
			    out = new StreamingMarkupWriter(out)
				boundClosure.trigger = out
				out.flush()
			}.asWritable()
		}
	}
	