package org.codehaus.groovy.sandbox.markup;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseMarkupBuilder extends Builder {
	public BaseMarkupBuilder(final Closure defaultGenerator) {
		super(defaultGenerator);
	}
	
	public BaseMarkupBuilder(final Closure defaultGenerator, final Map methodMap) {
		super(defaultGenerator, methodMap);
	}
	
	public Object bind(final Closure root) {
		return new Document(root, this.defaultGenerator, this.methodMap);
	}
	
	private static class Document extends Built {
		private Object out;
		private final Map pendingNamespaces = new HashMap();
		private final Map namespaces = new HashMap();
		private String prefix = "";
		
		public Document(final Closure root, final Closure defaultTag, final Map tagMap) {
			super(root, defaultTag, tagMap);
			
			this.namespaces.put("xml", "");	// built in namespace
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
			try {
				if (this.tagMap.containsKey(name)) {
					return ((Closure)this.tagMap.get(name)).call(new Object[]{this.pendingNamespaces, this.namespaces, this.prefix, attrs, body, this.out});
				} else {
					return this.defaultTag.call(new Object[]{name, this.pendingNamespaces, this.namespaces, this.prefix, attrs, body, this.out});		
				}
			} finally {
				this.prefix = "";
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
