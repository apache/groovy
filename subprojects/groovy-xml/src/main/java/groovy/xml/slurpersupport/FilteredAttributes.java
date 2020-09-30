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
package groovy.xml.slurpersupport;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.Iterator;
import java.util.Map;

/**
 * Lazy evaluated representation of a node's attributes filtered by a Closure.
 */
public class FilteredAttributes extends Attributes
{
    private final Closure closure;

    /**
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param closure the Closure to use to filter the attributes
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public FilteredAttributes(final GPathResult parent, final Closure closure, final Map<String, String> namespaceTagHints) {
        super(parent, parent.name, namespaceTagHints);
        this.closure = closure;
    }

    @Override
    public Iterator nodeIterator() {
        return new NodeIterator(this.parent.iterator())
        {
            @Override
            protected Object getNextNode(final Iterator iter) {
                while (iter.hasNext()) {
                    final Object node = iter.next();
                    if (DefaultTypeTransformation.castToBoolean(FilteredAttributes.this.closure.call(new Object[]{node}))) {
                        return node;
                    }
                }
                return null;
            }
        };
    }

}
