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
package org.codehaus.groovy.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

/**
 * A property path full binding
 */
public class PropertyPathFullBinding extends AbstractFullBinding implements PropertyChangeListener {

    /**
     * The set of all objects where a property change does incur a listener-re-check
     */
    Set updateObjects = new HashSet();

    /**
     * The root set of bind paths we are interested in
     */
    BindPath[] bindPaths;

    /**
     * If we think we are currently bound
     */
    boolean bound;

    public void bind() {
        updateObjects.clear();
        for (BindPath bp : bindPaths) {
            bp.addAllListeners(this, bp.currentObject, updateObjects);
        }
        bound = true;
    }

    public void unbind() {
        updateObjects.clear();
        for (BindPath path : bindPaths) {
            // we can't just remove from the update set,
            // because we may be local or global, the path knows
            path.removeListeners();
        }
        bound = false;
    }

    public void rebind() {
        if (bound) bind();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (updateObjects.contains(evt.getSource())) {
            for (BindPath bp : bindPaths) {
                Set newUpdates = new HashSet();
                bp.updatePath(this, bp.currentObject, newUpdates);
                updateObjects = newUpdates;
            }
        }
        update();
    }
}
