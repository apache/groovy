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

import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import groovy.lang.Closure;

/**
 * @author John Wilson
 */

public class FilteredNodeChildren extends NodeChildren {
    private final Closure closure;

    public FilteredNodeChildren(final GPathResult parent, final Closure closure, final Map namespaceTagHints) {
        super(parent, parent.name, namespaceTagHints);
        this.closure = closure;
    }

    public Iterator iterator() {
        return new Iterator() {
            final Iterator iter = FilteredNodeChildren.this.parent.iterator();
            Object next = null;

            public boolean hasNext() {
                while (this.iter.hasNext()) {
                    final Object childNode = this.iter.next();
                    if (closureYieldsTrueForNode(childNode)) {
                        this.next = childNode;
                        return true;
                    }
                }
                return false;
            }

            public Object next() {
                return this.next;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterator nodeIterator() {
        return new NodeIterator(this.parent.nodeIterator()) {
            protected Object getNextNode(final Iterator iter) {
                while (iter.hasNext()) {
                    final Object node = iter.next();
                    if (closureYieldsTrueForNode(new NodeChild((Node) node, FilteredNodeChildren.this.parent, FilteredNodeChildren.this.namespaceTagHints))) {
                        return node;
                    }
                }
                return null;
            }
        };
    }

    private boolean closureYieldsTrueForNode(Object childNode) {
        return DefaultTypeTransformation.castToBoolean(FilteredNodeChildren.this.closure.call(new Object[]{childNode}));
    }
}
