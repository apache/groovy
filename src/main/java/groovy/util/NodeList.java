/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.util;

import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.namespace.QName;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A List implementation which is returned by queries on a {@link Node}
 * which provides some XPath like helper methods for GPath.
 */
public class NodeList extends ArrayList {
    private static final long serialVersionUID = 8307095805417308716L;

    static {
        // wrap the standard MetaClass with the delegate
        setMetaClass(NodeList.class, GroovySystem.getMetaClassRegistry().getMetaClass(NodeList.class));
    }

    public NodeList() {
    }

    public NodeList(Collection collection) {
        super(collection);
    }

    public NodeList(int size) {
        super(size);
    }

    /**
     * Creates a new NodeList containing the same elements as the
     * original (but cloned in the case of Nodes).
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        NodeList result = new NodeList(size());
        for (int i = 0; i < size(); i++) {
            Object next = get(i);
            if (next instanceof Node) {
                Node n = (Node) next;
                result.add(n.clone());
            } else {
                result.add(next);
            }
        }
        return result;
    }

    protected static void setMetaClass(final Class nodeListClass, final MetaClass metaClass) {
        GroovySystem.getMetaClassRegistry().setMetaClass(nodeListClass, new DelegatingMetaClass(metaClass) {

            @Override
            public Object getAttribute(final Object object, final String attribute) {
                NodeList list = (NodeList) object;
                var result = new ArrayList<Object>(list.size());
                for (Object node : list) {
                    var attributes = ((Node) node).attributes();
                    result.add(attributes.get(attribute));
                }
                return result;
            }

            @Override
            public Object getAttribute(Class sender, Object object, String attribute, boolean isSuper) {
                return getAttribute(object, attribute);
            }

            @Override
            public void setAttribute(final Object object, final String attribute, final Object newValue) {
                for (Object node : (NodeList) object) {
                    ((Node) node).attributes().put(attribute, newValue);
                }
            }

            @Override
            public void setAttribute(final Class sender, final Object object, final String attribute, final Object newValue, final boolean isSuper, final boolean isInner) {
                setAttribute(object, attribute, newValue);
            }

            @Override
            public Object getProperty(final Object object, final String property) {
                if (object instanceof NodeList) {
                    return ((NodeList) object).getAt(property);
                }
                return super.getProperty(object, property);
            }

            @Override
            public Object getProperty(final Class sender, final Object object, final String property, final boolean isSuper, final boolean isInner) {
                if (object instanceof NodeList) {
                    return ((NodeList) object).getAt(property);
                }
                return super.getProperty(sender, object, property, isSuper, isInner);
            }
        });
    }

    /**
     * Provides lookup of elements by non-namespaced name.
     *
     * @param name the name or shortcut key for nodes of interest
     * @return the nodes of interest which match name
     */
    public NodeList getAt(String name) {
        NodeList answer = new NodeList();
        for (Object child : this) {
            if (child instanceof Node) {
                Node childNode = (Node) child;
                Object temp = childNode.get(name);
                if (temp instanceof Collection) {
                    answer.addAll((Collection) temp);
                } else {
                    answer.add(temp);
                }
            }
        }
        return answer;
    }

    /**
     * Provides lookup of elements by QName.
     *
     * @param name the name or shortcut key for nodes of interest
     * @return the nodes of interest which match name
     */
    public NodeList getAt(QName name) {
        NodeList answer = new NodeList();
        for (Object child : this) {
            if (child instanceof Node) {
                Node childNode = (Node) child;
                NodeList temp = childNode.getAt(name);
                answer.addAll(temp);
            }
        }
        return answer;
    }

    /**
     * Returns the text value of all of the elements in the collection.
     *
     * @return the text value of all the elements in the collection or null
     */
    public String text() {
        String previousText = null;
        StringBuilder buffer = null;
        for (Object child : this) {
            String text = null;
            if (child instanceof String) {
                text = (String) child;
            } else if (child instanceof Node) {
                text = ((Node) child).text();
            }
            if (text != null) {
                if (previousText == null) {
                    previousText = text;
                } else {
                    if (buffer == null) {
                        buffer = new StringBuilder();
                        buffer.append(previousText);
                    }
                    buffer.append(text);
                }
            }
        }
        if (buffer != null) {
            return buffer.toString();
        }
        if (previousText != null) {
            return previousText;
        }
        return "";
    }

    public Node replaceNode(Closure c) {
        if (size() != 1) {
            throw new GroovyRuntimeException(
                    "replaceNode() can only be used to replace a single node, but was applied to " + size() + " nodes");
        }
        return ((Node)get(0)).replaceNode(c);
    }

    public void plus(Closure c) {
        for (Object o : this) {
            ((Node) o).plus(c);
        }
    }
}
