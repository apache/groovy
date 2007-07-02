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

/**
 * @author John Wilson
 *
 */

public class NoChildren extends GPathResult {
  /**
   * @param parent
   * @param name
   */
  public NoChildren(final GPathResult parent, final String name, final Map namespaceTagHints) {
    super(parent, name, "*", namespaceTagHints);
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#size()
   */
  public int size() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#text()
   */
  public String text() {
    return "";
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#parents()
   */
  public GPathResult parents() {
    // TODO Auto-generated method stub
    throw new GroovyRuntimeException("parents() not implemented yet");
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#childNodes()
   */
  public Iterator childNodes() {
    return iterator();
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#iterator()
   */
  public Iterator iterator() {
    return new Iterator() {
      public boolean hasNext() {
        return false;
      }
      
      public Object next() {
        return null;
      }
      
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#find(groovy.lang.Closure)
   */
  public GPathResult find(final Closure closure) {
    return this;
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#findAll(groovy.lang.Closure)
   */
  public GPathResult findAll(final Closure closure) {
    return this;
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.GPathResult#nodeIterator()
   */
  public Iterator nodeIterator() {
    return iterator();
  }

  /* (non-Javadoc)
   * @see groovy.lang.Writable#writeTo(java.io.Writer)
   */
  public Writer writeTo(final Writer out) throws IOException {
    return out;
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.markup.Buildable#build(groovy.lang.GroovyObject)
   */
  public void build(final GroovyObject builder) {
  }

  protected void replaceNode(final Closure newValue) {
    // No elements match GPath expression - do nothing
  }

  protected void replaceBody(final Object newValue) {
    // No elements match GPath expression - do nothing   
  }

  protected void appendNode(final Object newValue) {
    // TODO consider creating an element for this
  }
}
