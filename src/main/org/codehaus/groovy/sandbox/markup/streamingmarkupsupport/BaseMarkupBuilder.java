package org.codehaus.groovy.sandbox.markup.streamingmarkupsupport;
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

import groovy.lang.Closure;
import groovy.lang.GroovyInterceptable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseMarkupBuilder extends Builder {
	public BaseMarkupBuilder(final Map namespaceMethodMap) {
		super(namespaceMethodMap);
	}
	
	public Object bind(final Closure root) {
		return new Document(root, this.namespaceMethodMap);
	}
	
	private static class Document extends Built implements GroovyInterceptable {
		private Object out;
		private final Map pendingNamespaces = new HashMap();
		private final Map namespaces = new HashMap();
		private String prefix = "";
		
		public Document(final Closure root, final Map namespaceMethodMap) {
			super(root, namespaceMethodMap);
			
			this.namespaces.put("xml", "http://www.w3.org/XML/1998/namespace");				// built in namespace
			this.namespaces.put("mkp", "http://www.codehaus.org/Groovy/markup/keywords");	// pseudo namespace for markup keywords
		}
		
		/* (non-Javadoc)
		 * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
		 */
		public Object invokeMethod(final String name, final Object args) {
			final Object[] arguments = (Object[]) args;
			Map attrs = Collections.EMPTY_MAP;
			Object body = null;
			
			
			//
			// Sort the parameters out
			//
			for (int i = 0; i != arguments.length; i++) {
				final Object arg = arguments[i];
				
				if (arg instanceof Map) {
					attrs = (Map)arg;
				} else if (arg instanceof Closure) {
					final Closure c = ((Closure) arg);
					
					c.setDelegate(this);
					body = c.asWritable();
				} else {
					body = arg;
				}
			}
			
			//
			// call the closure corresponding to the tag
			//
			final Object uri;
			
			if (this.pendingNamespaces.containsKey(this.prefix)) {
				uri = this.pendingNamespaces.get(this.prefix);
			} else if (this.namespaces.containsKey(this.prefix)) {
				uri = this.namespaces.get(this.prefix);
			} else {
				uri = ":";
			}
			
			final Object[] info  = (Object[])this.namespaceSpecificTags.get(uri);
			final Map tagMap = (Map)info[2];
			final Closure defaultTagClosure = (Closure)info[0];
			
			final String prefix = this.prefix;
			this.prefix = "";
			
			if (tagMap.containsKey(name)) {
				return ((Closure)tagMap.get(name)).call(new Object[]{this, this.pendingNamespaces, this.namespaces, this.namespaceSpecificTags, prefix, attrs, body, this.out});
			} else {
				return defaultTagClosure.call(new Object[]{name, this, this.pendingNamespaces, this.namespaces, this.namespaceSpecificTags, prefix, attrs, body, this.out});		
			}
		}
		
		/* (non-Javadoc)
		 * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
		 */
		public Object getProperty(final String property) {
			this.prefix = property;
			return this;
		}
		
		/* (non-Javadoc)
		 * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
		 */
		public void setProperty(String property, Object newValue) {
			if ("trigger".equals(property)) {
				this.out = newValue;
				this.root.call();
			} else {
				super.setProperty(property, newValue);
			}
		}
	}
}
