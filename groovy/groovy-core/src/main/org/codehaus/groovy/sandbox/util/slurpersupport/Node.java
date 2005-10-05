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
import groovy.lang.Writable;

import java.io.IOException;
import java.io.Writer;
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

public class Node implements Writable {
  private final String name;
  private final Map attributes;
  private final Map attributeNamespaces;
  private final String namespaceURI;
  private List children = new LinkedList();
  
  public Node(final Node parent, final String name, final Map attributes, final Map attributeNamespaces, final String namespaceURI) {
    this.name = name;
    this.attributes = attributes;
    this.attributeNamespaces = attributeNamespaces;
    this.namespaceURI = namespaceURI;
  }
  
  public String name() {
    return this.name;
  }
  
  public String namespace() {
    return this.namespaceURI;
  }
  
  public String namespaceURI() {
    return this.namespaceURI;
  }
  
  public Map attributes() {
    return this.attributes;
  }

  public List children() {
    return this.children();
  }

  public void addChild(final Object child) {
    this.children.add(child);
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#text()
   */
  public String text() {
  final StringBuffer buff = new StringBuffer();
  final Iterator iter = this.children.iterator();
  
    while (iter.hasNext()) {
      buff.append(iter.next());
    }
  
    return buff.toString();
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#childNodes()
   */
  
  public Iterator childNodes() {
    return new Iterator() {
      private final Iterator iter = Node.this.children.iterator();
      private Object nextElementNodes = getNextElementNodes();
      
      public boolean hasNext() {
        return this.nextElementNodes != null;
      }
      
      public Object next() {
        try {
          return this.nextElementNodes;
        } finally {
          this.nextElementNodes = getNextElementNodes();
        }
      }
      
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private Object getNextElementNodes() {
        while (iter.hasNext()) {
        final Object node = iter.next();
        
          if (node instanceof Node) {
            return node;
          }
        }
        
        return null;
      }
    };
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.sandbox.util.slurpersupport.Node#writeTo(java.io.Writer)
   */
  public Writer writeTo(final Writer out) throws IOException {
  final Iterator iter = this.children.iterator();
  
    while (iter.hasNext()) {
    final Object child = iter.next();
    
      if (child instanceof Writable) {
        ((Writable)child).writeTo(out);
      } else {
        out.write(child.toString());
      }
    }
    
    return out;
  }
  
  public void build(final GroovyObject builder, final Map namespaceMap, final Map namespaceTagHints) {
  final Closure rest = new Closure(null) {
                          public Object doCall(final Object o) {
                            buildChildren(builder, namespaceMap, namespaceTagHints);
                            
                            return null;
                          }
                        };
    
    if (this.namespaceURI.length() == 0 && this.attributeNamespaces.isEmpty()) {
      builder.invokeMethod(this.name, new Object[]{this.attributes, rest});
    } else {
      final List newTags = new LinkedList();
      builder.getProperty("mkp");
      final List namespaces = (List)builder.invokeMethod("getNamespaces", new Object[]{});
      
      final Map current = (Map)namespaces.get(0);
      final Map pending = (Map)namespaces.get(1);
      
      if (this.attributeNamespaces.isEmpty()) {     
        builder.getProperty(getTagFor(this.namespaceURI, current, pending, namespaceMap, namespaceTagHints, newTags, builder));
        builder.invokeMethod(this.name, new Object[]{this.attributes, rest});
      } else {
      final Map attributesWithNamespaces = new HashMap(this.attributes);
      final Iterator attrs = this.attributes.keySet().iterator();
        
        while (attrs.hasNext()) {
        final Object key = attrs.next();
        final Object attributeNamespaceURI = this.attributeNamespaces.get(key);
          
          if (attributeNamespaceURI != null) {
            attributesWithNamespaces.put(getTagFor(attributeNamespaceURI, current, pending, namespaceMap, namespaceTagHints, newTags, builder) +
                                         "$" + key, attributesWithNamespaces.remove(key));
          }
        }
        
        builder.getProperty(getTagFor(this.namespaceURI, current, pending, namespaceMap,namespaceTagHints,  newTags, builder));
        builder.invokeMethod(this.name, new Object[]{attributesWithNamespaces, rest});
      }
      
      // remove the new tags we had to define for this element
      if (!newTags.isEmpty()) {
      final Iterator iter = newTags.iterator();
      
        do {
          pending.remove(iter.next());
        } while (iter.hasNext());
      }
      
    }   
  }
  
  private static String getTagFor(final Object namespaceURI, final Map current,
                                  final Map pending, final Map local, final Map tagHints,
                                  final List newTags, final GroovyObject builder) {
  String tag = findNamespaceTag(pending, namespaceURI); // look in the namespaces whose decatarion has already been emitted
    
    if (tag == null) {
      tag = findNamespaceTag(current, namespaceURI);  // look in the namespaces who will be declared at the next element
      
      if (tag == null) {
        // we have to declare the namespace - choose a tag
        tag = findNamespaceTag(local, namespaceURI);  // If the namespace has been decared in the GPath expression use that tag
        
        if (tag == null) {
          tag = findNamespaceTag(tagHints, namespaceURI);  // If the namespace has been used in the parse documant use that tag         
        }
        
        if (tag == null) { // otherwise make up a new tag and check it has not been used before
        int suffix = 0;
        
          do {
            final String posibleTag = "tag" + suffix++;
            
            if (!pending.containsKey(posibleTag) && !current.containsKey(posibleTag) && !local.containsKey(posibleTag)) {
              tag = posibleTag;
            }
          } while (tag == null);
        }
        
        final Map newNamespace = new HashMap();
        newNamespace.put(tag, namespaceURI);
        builder.getProperty("mkp");
        builder.invokeMethod("declareNamespace", new Object[]{newNamespace});
        newTags.add(tag);
      }
    }
    
    return tag;
  }
  
  private static String findNamespaceTag(final Map tagMap, final Object namespaceURI) {
    if (tagMap.containsValue(namespaceURI)) {
    final Iterator entries = tagMap.entrySet().iterator();
      
      while (entries.hasNext()) {
        final Map.Entry entry = (Map.Entry)entries.next();
        
        if (namespaceURI.equals(entry.getValue())) {
          return (String)entry.getKey();
        }
      }
    }
    
    return null;
  }
  
  private void buildChildren(final GroovyObject builder, final Map namespaceMap, final Map namespaceTagHints) {
  final Iterator iter = this.children.iterator();
  
    while (iter.hasNext()) {
    final Object child = iter.next();
    
      if (child instanceof Node) {
        ((Node)child).build(builder, namespaceMap, namespaceTagHints);
      } else if (child instanceof Buildable) {
        ((Buildable)child).build(builder);
      } else {
        builder.getProperty("mkp");
        builder.invokeMethod("yield", new Object[]{child});
      }
    }
  }
}
