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
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Writable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.sandbox.markup.Buildable;

/**
 * @author John Wilson
 *
 */

public abstract class GPathResult extends GroovyObjectSupport implements Writable, Buildable {
  protected final GPathResult parent;
  protected final String name;
  protected final String namespacePrefix;
  protected final HashMap namespaceMap = new HashMap();
 
  /**
   * @param parent
   * @param name
   */
  public GPathResult(final GPathResult parent, final String name, final String namespacePrefix) {
    if (parent == null) {
      this.parent = this;
    } else {
      this.parent = parent;
      this.namespaceMap.putAll(parent.namespaceMap);
    }
    this.name = name;
    this.namespacePrefix = namespacePrefix;
  }
  
  public Object getProperty(final String property) {
    if ("..".equals(property)) {
      return parent();
    } else if ("*".equals(property)){
      return children();
    } else if (property.startsWith("@")) {
      if (property.indexOf(":") != -1) {
      final int i = property.indexOf(":");

        return new Attributes(this, "@" + property.substring(i + 1), property.substring(1, i));
      } else {
        return new Attributes(this, property);
      }
    } else {
      if (property.indexOf(":") != -1) {
      final int i = property.indexOf(":");

        return new NodeChildren(this, property.substring(i + 1), property.substring(0, i));
      } else {
        return new NodeChildren(this, property);
      }
    }
  }

  public String name() {
    return this.name;
  }

  public GPathResult parent() {
    return this.parent;
  }

  public GPathResult children() {
    return new NodeChildren(this);
  }

  public String toString() {
    return text();
  }
  
  public GPathResult declareNamespace(final Map newNamespaceMapping) {
    this.namespaceMap.putAll(newNamespaceMapping);
    
    return this; 
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    return text().equals(obj.toString());
  }
  
  public List list() {
  final Iterator iter =  iterator();
  final List result = new LinkedList();
  
    while (iter.hasNext()) {
      result.add(iter.next());
    }
    
    return result;
  }

  public abstract int size();
  
  public abstract String text();
  
  public abstract GPathResult parents();
  
  public abstract Iterator childNodes();
  
  public abstract Iterator iterator();
  
  public abstract Object getAt(int index);
  
  public abstract GPathResult find(Closure closure);
  
  public abstract GPathResult findAll(Closure closure);
}
