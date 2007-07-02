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

import groovy.lang.Buildable;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import groovy.lang.MetaClass;
import groovy.lang.Writable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;


/**
 * @author John Wilson
 */

public abstract class GPathResult extends GroovyObjectSupport implements Writable, Buildable {
    protected final GPathResult parent;
    protected final String name;
    protected final String namespacePrefix;
    protected final Map namespaceMap = new HashMap();
    protected final Map namespaceTagHints;

    /**
     * @param parent
     * @param name
     * @param namespacePrefix
     * @param namespaceTagHints
     */
    public GPathResult(final GPathResult parent, final String name, final String namespacePrefix, final Map namespaceTagHints) {
        if (parent == null) {
            // we are the top of the tree
            this.parent = this;
            this.namespaceMap.put("xml", "http://www.w3.org/XML/1998/namespace");  // The XML namespace is always defined
        } else {
            this.parent = parent;
            this.namespaceMap.putAll(parent.namespaceMap);
        }
        this.name = name;
        this.namespacePrefix = namespacePrefix;
        this.namespaceTagHints = namespaceTagHints;

        setMetaClass(getMetaClass()); // wrap the standard MetaClass with the delegate
    }

    /* (non-Javadoc)
     * @see groovy.lang.GroovyObjectSupport#setMetaClass(groovy.lang.MetaClass)
     */
    public void setMetaClass(final MetaClass metaClass) {
        final MetaClass newMetaClass = new DelegatingMetaClass(metaClass) {
            /* (non-Javadoc)
            * @see groovy.lang.DelegatingMetaClass#getAttribute(java.lang.Object, java.lang.String)
            */
            public Object getAttribute(final Object object, final String attribute) {
                return GPathResult.this.getProperty("@" + attribute);
            }
            
            public void setAttribute(final Object object, final String attribute, final Object newValue) {
                GPathResult.this.setProperty("@" + attribute, newValue);
            }
        };
        super.setMetaClass(newMetaClass);
    }

    public Object getProperty(final String property) {
        if ("..".equals(property)) {
            return parent();
        } else if ("*".equals(property)) {
            return children();
        } else if ("**".equals(property)) {
            return depthFirst();
        } else if (property.startsWith("@")) {
            if (property.indexOf(":") != -1) {
                final int i = property.indexOf(":");
                return new Attributes(this, "@" + property.substring(i + 1), property.substring(1, i), this.namespaceTagHints);
            } else {
                return new Attributes(this, property, this.namespaceTagHints);
            }
        } else {
            if (property.indexOf(":") != -1) {
                final int i = property.indexOf(":");
                return new NodeChildren(this, property.substring(i + 1), property.substring(0, i), this.namespaceTagHints);
            } else {
                return new NodeChildren(this, property, this.namespaceTagHints);
            }
        }
    }

    public void setProperty(final String property, final Object newValue) {
        if (property.startsWith("@")) {
            if (newValue instanceof String || newValue instanceof GString) {
            final Iterator iter = iterator();
            
                while (iter.hasNext()) {
                final NodeChild child = (NodeChild)iter.next();
                
                    child.attributes().put(property.substring(1), newValue);
                }
            }
        } else {
        final GPathResult result = new NodeChildren(this, property, this.namespaceTagHints);
        
            if (newValue instanceof Map) {
            final Iterator iter = ((Map)newValue).entrySet().iterator();
            
                while (iter.hasNext()) {
                final Map.Entry entry = (Map.Entry)iter.next();
                
                    result.setProperty("@" + entry.getKey(), entry.getValue());
                }
            } else {           
              if (newValue instanceof Closure) {
                  result.replaceNode((Closure)newValue);
              } else {
                  result.replaceBody(newValue);
              }
            }
        }
    }
    
    public Object leftShift(final Object newValue) {
        appendNode(newValue);
        return this;
    }
    
    public Object plus(final Object newValue) {
        this.replaceNode(new Closure(this) {
            public void doCall(Object[] args) {
            final GroovyObject delegate = (GroovyObject)getDelegate();
             
                delegate.getProperty("mkp");
                delegate.invokeMethod("yield", args);
                
                delegate.getProperty("mkp");
                delegate.invokeMethod("yield", new Object[]{newValue});
            }
        });
        
        return this;
    }
    
    protected abstract void replaceNode(Closure newValue);
    
    protected abstract void replaceBody(Object newValue);
    
    protected abstract void appendNode(Object newValue);

    public String name() {
        return this.name;
    }

    public GPathResult parent() {
        return this.parent;
    }

    public GPathResult children() {
        return new NodeChildren(this, this.namespaceTagHints);
    }
    
    public String lookupNamespace(final String prefix) {
        return (String)this.namespaceTagHints.get(prefix);
    }

    public String toString() {
        return text();
    }

    public Integer toInteger() {
        return DefaultGroovyMethods.toInteger(text());
    }

    public Long toLong() {
        return DefaultGroovyMethods.toLong(text());
    }

    public Float toFloat() {
        return DefaultGroovyMethods.toFloat(text());
    }

    public Double toDouble() {
        return DefaultGroovyMethods.toDouble(text());
    }

    public BigDecimal toBigDecimal() {
        return DefaultGroovyMethods.toBigDecimal(text());
    }

    public BigInteger toBigInteger() {
        return DefaultGroovyMethods.toBigInteger(text());
    }

    public URL toURL() throws MalformedURLException {
        return DefaultGroovyMethods.toURL(text());
    }

    public URI toURI() throws URISyntaxException {
        return DefaultGroovyMethods.toURI(text());
    }

    public Boolean toBoolean() {
        return DefaultGroovyMethods.toBoolean(text());
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

    public Object getAt(final int index) {
        if (index < 0) throw new ArrayIndexOutOfBoundsException(index);
        
        final Iterator iter = iterator();
        int count = 0;
    
        while (iter.hasNext()) {
            if (count++ == index) {
                return iter.next();
            } else {
                iter.next();
            }
        }
        
        return new NoChildren(this, this.name, this.namespaceTagHints);
    }

    public Object getAt(final IntRange range) {
    final int from = range.getFromInt();
    final int to = range.getToInt();
    
        if (range.isReverse()) {
            throw new GroovyRuntimeException("Reverse ranges not supported, range supplied is ["+ to + ".." + from + "]");
        } else if (from < 0 || to < 0) {
            throw new GroovyRuntimeException("Negative range indexes not supported, range supplied is ["+ from + ".." + to + "]");
        } else {
            return new Iterator() {
            final Iterator iter = iterator();
            Object next;
            int count = 0;
            
               public boolean hasNext() {
                   if (count <= to) {
                       while (iter.hasNext()) {
                           if (count++ >= from) {
                               this.next = iter.next();
                               return true;
                           } else {
                               iter.next();
                           }
                       }
                   }

                   return false;
                }

                public Object next() {
                    return next;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
            };
        }
     }

    public void putAt(final int index, final Object newValue) {
    final GPathResult result = (GPathResult)getAt(index);
    
        if (newValue instanceof Closure) {
            result.replaceNode((Closure)newValue);
        } else {
            result.replaceBody(newValue);
        }
    }
    
    public Iterator depthFirst() {
        return new Iterator() {
            private final List list = new LinkedList();
            private final Stack stack = new Stack();
            private Iterator iter = iterator();
            private GPathResult next = getNextByDepth();

            public boolean hasNext() {
                return this.next != null;
            }

            public Object next() {
                try {
                    return this.next;
                } finally {
                    this.next = getNextByDepth();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private GPathResult getNextByDepth() {
                while (this.iter.hasNext()) {
                    final GPathResult node = (GPathResult) this.iter.next();
                    this.list.add(node);
                    this.stack.push(this.iter);
                    this.iter = node.children().iterator();
                }

                if (this.list.isEmpty()) {
                    return null;
                } else {
                    GPathResult result = (GPathResult) this.list.get(0);
                    this.list.remove(0);
                    this.iter = (Iterator) this.stack.pop();
                    return result;
                }
            }
        };
    }

    /**
     * An iterator useful for traversing XML documents/fragments in breadth-first order.
     *
     * @return Iterator the iterator of GPathResult objects
     */
    public Iterator breadthFirst() {
        return new Iterator() {
            private final List list = new LinkedList();
            private Iterator iter = iterator();
            private GPathResult next = getNextByBreadth();

            public boolean hasNext() {
                return this.next != null;
            }

            public Object next() {
                try {
                    return this.next;
                } finally {
                    this.next = getNextByBreadth();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private GPathResult getNextByBreadth() {
                List children = new ArrayList();
                while (this.iter.hasNext() || !children.isEmpty()) {
                    if (this.iter.hasNext()) {
                        final GPathResult node = (GPathResult) this.iter.next();
                        this.list.add(node);
                        this.list.add(this.iter);
                        children.add(node.children());
                    } else {
                        List nextLevel = new ArrayList();
                        for (int i = 0; i < children.size(); i++) {
                            GPathResult next = (GPathResult) children.get(i);
                            Iterator iterator = next.iterator();
                            while (iterator.hasNext()) {
                                nextLevel.add(iterator.next());
                            }
                        }
                        this.iter = nextLevel.iterator();
                        children = new ArrayList();
                    }
                }
                if (this.list.isEmpty()) {
                    return null;
                } else {
                    GPathResult result = (GPathResult) this.list.get(0);
                    this.list.remove(0);
                    this.iter = (Iterator) this.list.get(0);
                    this.list.remove(0);
                    return result;
                }
            }
        };
    }

    public List list() {
        final Iterator iter = nodeIterator();
        final List result = new LinkedList();
        while (iter.hasNext()) {
            result.add(new NodeChild((Node) iter.next(), this.parent, this.namespacePrefix, this.namespaceTagHints));
        }
        return result;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    
    public Closure getBody() {
        return new Closure(this.parent(),this) {
            public void doCall(Object[] args) {
                final GroovyObject delegate = (GroovyObject)getDelegate();
                final GPathResult thisObject = (GPathResult)getThisObject();

                Node node = (Node)thisObject.getAt(0);
                List children = node.children();

                for(int i=0;  i<children.size(); i++){
                    Object child = children.get(i);
                    delegate.getProperty("mkp");
                    if(child instanceof Node){
                        delegate.invokeMethod("yield", new Object[]{new NodeChild((Node)child, thisObject,"*",null)});
                    }   
                    else{
                        delegate.invokeMethod("yield", new Object[]{child});
                    }   
                }                
            }
        };
    }

    public abstract int size();

    public abstract String text();

    public abstract GPathResult parents();

    public abstract Iterator childNodes();

    public abstract Iterator iterator();

    public abstract GPathResult find(Closure closure);

    public abstract GPathResult findAll(Closure closure);

    public abstract Iterator nodeIterator();
}
