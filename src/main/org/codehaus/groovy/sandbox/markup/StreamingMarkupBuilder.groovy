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
		commentClosure = {pendingNamespaces, namespaces, prefix, attrs, body, out |
							out.unescaped() << "<!--"
							out.bodyText() << body
							out.unescaped() << "-->"
						 }
		noopClosure = {pendingNamespaces, namespaces, prefix, attrs, body, out | out.bodyText() << body}
		unescapedClosure = {pendingNamespaces, namespaces, prefix, attrs, body, out | out.unescaped() << body}
		tagClosure = {tag, pendingNamespaces, namespaces, prefix, attrs, body, out |
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
		namespaceSetupClosure = {pendingNamespaces, namespaces, prefix, attrs, body, out |
									if ( prefix == "") {
										prefix = ":"	// marker for default namespace
									}
									
									body = body.toString() 	// in case it's not a string
									
									if (namespaces[prefix] != body) {
										pendingNamespaces[prefix] = body
									}
								}
						
		specialTags = ['yield':noopClosure,
		               'yieldUnescaped':unescapedClosure,
		               'comment':commentClosure,
		               'declareNamespace':namespaceSetupClosure]
		
		StreamingMarkupBuilder() {
		}
		
		StreamingMarkupBuilder(extraTags) {
			this.specialTags.putAll(extraTags)
		}
		
		renameTag(oldName, newName) {
			if (!this.specialTags.containsKey(oldName)) {
				new GroovyRuntimeException("can't rename ${oldName} to ${newName}, ${oldName} not an existing tag")
			}
			
			this.specialTags[newName] = this.specialTags.remove(oldName)
		}
		
		bind(closure) {
			boundClosure = (new BaseMarkupBuilder(this.tagClosure, this.specialTags)).bind closure
			
			{out |
			    out = new StreamingMarkupWriter(out)
				boundClosure.trigger = out
				out.flush()
			}.asWritable()
		}
	}
	