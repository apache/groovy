/*
 * Copyright 2005 John G. Wilson
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
 *
 */

package org.codehaus.groovy.sandbox.util.slurpersupport;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * @author John Wilson
 *
 */

public class NodeChild extends GPathResult {
  private final Node node;

  public NodeChild(final Node node, final GPathResult parent, final String namespacePrefix, final Map namespaceTagHints) {
    super(parent, node.name(), namespacePrefix, namespaceTagHints);
    this.node = node;
  }

  public NodeChild(final Node node, final GPathResult parent, final Map namespaceTagHints) {
    this(node, parent, "*", namespaceTagHints);
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#size()
   */
  public int size() {
    return 1;
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#text()
   */
  public String text() {
    return this.node.text();
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#parents()
   */
  public GPathResult parents() {
    // TODO Auto-generated method stub
    throw new GroovyRuntimeException("parents() not implemented yet");
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#iterator()
   */
  public Iterator iterator() {
    return new Iterator() {
      private boolean hasNext = true;
      
      public boolean hasNext() {
        return this.hasNext;
      }
      
      public Object next() {
        try {
          return (this.hasNext) ? NodeChild.this : null;
        } finally {
          this.hasNext = false;
        }
      }
      
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#iterator()
   */
  public Iterator nodeIterator() {
    return new Iterator() {
      private boolean hasNext = true;
      
      public boolean hasNext() {
        return this.hasNext;
      }
      
      public Object next() {
        try {
          return (this.hasNext) ? NodeChild.this.node : null;
        } finally {
          this.hasNext = false;
        }
      }
      
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#getAt(int)
   */
  public Object getAt(final int index) {
    if (index == 0) {
      return node;
    } else {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }
  public Iterator childNodes() {
    return this.node.childNodes();
  }
  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#find(groovy.lang.Closure)
   */
  public GPathResult find(final Closure closure) {
    if (((Boolean)closure.call(new Object[]{this})).booleanValue()) {
      return this;
    } else {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#findAll(groovy.lang.Closure)
   */
  public GPathResult findAll(final Closure closure) {
    return find(closure);
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#build(groovy.lang.GroovyObject)
   */
  public void build(final GroovyObject builder) {
    this.node.build(builder, this.namespaceMap, this.namespaceTagHints);
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#writeTo(java.io.Writer)
   */
  public Writer writeTo(final Writer out) throws IOException {
    return this.node.writeTo(out);
  }
}
