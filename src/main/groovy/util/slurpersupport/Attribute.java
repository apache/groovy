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
package groovy.util.slurpersupport;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

public class Attribute extends GPathResult {
    private final String value;

    public Attribute(final String name, final String value, final GPathResult parent, final String namespacePrefix, final Map namespaceTagHints) {
      super(parent, name, namespacePrefix, namespaceTagHints);
      this.value = value;
    }

    public String name() {
        // this name contains @name we need to return name
        return this.name.substring(1);
    }

    public int size() {
        return 1;
    }

    public String text() {
         return this.value;
    }

    public GPathResult parents() {
        // TODO Auto-generated method stub
        throw new GroovyRuntimeException("parents() not implemented yet");
    }

    public Iterator childNodes() {
        throw new GroovyRuntimeException("can't call childNodes() in the attribute " + this.name);
    }

    public Iterator iterator() {
        return nodeIterator();
    }

    public GPathResult find(final Closure closure) {
        if (DefaultTypeTransformation.castToBoolean(closure.call(new Object[]{this}))) {
            return this;
          } else {
            return new NoChildren(this, "", this.namespaceTagHints);
          }
    }

    public GPathResult findAll(final Closure closure) {
        return find(closure);
    }

    public Iterator nodeIterator() {
        return new Iterator() {
            private boolean hasNext = true;
            
            public boolean hasNext() {
              return this.hasNext;
            }
            
            public Object next() {
              try {
                return (this.hasNext) ? Attribute.this : null;
              } finally {
                this.hasNext = false;
              }
            }
            
            public void remove() {
              throw new UnsupportedOperationException();
            }
        };
    }

    public Writer writeTo(final Writer out) throws IOException {
        out.write(this.value);
        return out;
    }

    public void build(final GroovyObject builder) {
        builder.getProperty("mkp");
        builder.invokeMethod("yield", new Object[]{this.value});
    }

    protected void replaceNode(final Closure newValue) {
    }

    protected void replaceBody(final Object newValue) {
    }

    protected void appendNode(final Object newValue) {
    }
}
