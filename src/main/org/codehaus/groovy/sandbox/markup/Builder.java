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
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Builder extends GroovyObjectSupport {
	protected final Map methodMap;
	protected final Closure defaultGenerator;
	
	public Builder(final Closure defaultGenerator) {
		this(defaultGenerator, new HashMap());
	}
	
	public Builder(final Closure defaultGenerator, final Map methodMap) {
		this.defaultGenerator = defaultGenerator.asWritable();
		this.methodMap = new HashMap();
		
		final Iterator keyIterator = methodMap.keySet().iterator();
		
		while (keyIterator.hasNext()) {
		final Object key = keyIterator.next();
		final Object value = methodMap.get(key);
		
			if ((value instanceof Closure)) {
				this.methodMap.put(key, value);
			} else {
				this.methodMap.put(key, this.defaultGenerator.curry(value));
			}
		}
		
	}
	
	abstract public Object bind(Closure root);
	
	protected static abstract class Built extends GroovyObjectSupport {
	protected final Closure root;
	protected final Closure defaultTag;
	protected final Map tagMap = new HashMap();
		
		public Built(final Closure root, final Closure defaultTag, final Map tagMap) {
			this.defaultTag = defaultTag;
			final Iterator keyIterator = tagMap.keySet().iterator();
			
			while (keyIterator.hasNext()) {
			final Object key = keyIterator.next();
			
				this.tagMap.put(key, tagMap.get(key));
			}
		
			try {
				this.root = (Closure)root.clone();
			} catch (final CloneNotSupportedException e) {
				throw new GroovyRuntimeException(e.getMessage());	// this should never be thrown
			}
			
			this.root.setDelegate(this);
		}
	}
}
