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
package org.apache.groovy.swing.binding;

import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;
import groovy.lang.Reference;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The bind path object.  This class represents one "step" in the bind path.
 */
@SuppressWarnings({"unchecked"}) // all are of type Object, so generics are useless
public class BindPath {

    /**
     * The local lookup for synthetic properties, like JTextField#text
     */
    Map<String, TriggerBinding> localSynthetics;

    /**
     * The object we think we are bound to
     */
    Object currentObject;

    /**
     * The property we are interested in
     */
    String propertyName;

    PropertyChangeListener localListener;
    PropertyChangeListener globalListener;
    BindingUpdatable syntheticFullBinding;

    /**
     * The steps further down the path from us
     */
    BindPath[] children;

    /**
     * Called when we detect a change somewhere down our path.
     * First, check to see if our object is changing.  If so remove our old listener
     * Next, update the reference object the children have and recurse
     * Finally, add listeners if we have a different object
     *
     * @param listener This listener to attach.
     * @param newObject The object we should read our property off of.
     * @param updateSet The list of objects we have added listeners to
     */
    public synchronized void updatePath(PropertyChangeListener listener, Object newObject, Set updateSet) {
        if (currentObject != newObject) {
            removeListeners();
        }
        if ((children != null) && (children.length > 0)) {
            try {
                Object newValue = null;
                if (newObject != null) {
                    updateSet.add(newObject);
                    newValue = extractNewValue(newObject);
                }
                for (BindPath child : children) {
                    child.updatePath(listener, newValue, updateSet);
                }
            } catch (Exception e) {
                //LOGME
                // do we ignore it, or fail?
            }
        }
        if (currentObject != newObject) {
            addListeners(listener, newObject, updateSet);
        }
    }

    /**
     * Adds all the listeners to the objects in the bind path.
     * This assumes that we are not added as listeners to any of them, hence
     * it is not idempotent.
     *
     * @param listener This listener to attach.
     * @param newObject The object we should read our property off of.
     * @param updateSet The list of objects we have added listeners to
     */
    public void addAllListeners(PropertyChangeListener listener, Object newObject, Set updateSet) {
        addListeners(listener, newObject, updateSet);
        if ((children != null) && (children.length > 0)) {
            try {
                Object newValue = null;
                if (newObject != null) {
                    updateSet.add(newObject);
                    newValue = extractNewValue(newObject);
                }
                for (BindPath child : children) {
                    child.addAllListeners(listener, newValue, updateSet);
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
                //LOGME
                // do we ignore it, or fail?
            }
        }
    }

    private Object extractNewValue(Object newObject) {
        Object newValue;
        try {
            newValue = InvokerHelper.getProperty(newObject, propertyName);

        } catch (MissingPropertyException mpe) {
            //todo we should flag this when the path is created that this is a field not a prop...
            // try direct method...
            try {
                newValue = InvokerHelper.getAttribute(newObject, propertyName);
                if (newValue instanceof Reference) {
                    newValue = ((Reference) newValue).get();
                }
            } catch (Exception e) {
                //LOGME?
                newValue = null;
            }
        }
        return newValue;
    }

    static final Class[] NAME_PARAMS = {String.class, PropertyChangeListener.class};
    static final Class[] GLOBAL_PARAMS = {PropertyChangeListener.class};

    /**
     * Add listeners to a specific object.  Updates the bould flags and update set
     *
     * @param listener This listener to attach.
     * @param newObject The object we should read our property off of.
     * @param updateSet The list of objects we have added listeners to
     */
    public void addListeners(PropertyChangeListener listener, Object newObject, Set updateSet) {
        removeListeners();
        if (newObject != null) {
            // check for local synthetics
            TriggerBinding syntheticTrigger = getSyntheticTriggerBinding(newObject);
            MetaClass mc = InvokerHelper.getMetaClass(newObject);
            if (syntheticTrigger != null) {
                PropertyBinding psb = new PropertyBinding(newObject, propertyName);
                PropertyChangeProxyTargetBinding proxytb = new PropertyChangeProxyTargetBinding(newObject, propertyName, listener);

                syntheticFullBinding = syntheticTrigger.createBinding(psb, proxytb);
                syntheticFullBinding.bind();
                updateSet.add(newObject);
            } else if (!mc.respondsTo(newObject, "addPropertyChangeListener", NAME_PARAMS).isEmpty()) {
                InvokerHelper.invokeMethod(newObject, "addPropertyChangeListener", new Object[] {propertyName, listener});
                localListener = listener;
                updateSet.add(newObject);
            } else if (!mc.respondsTo(newObject, "addPropertyChangeListener", GLOBAL_PARAMS).isEmpty()) {
                InvokerHelper.invokeMethod(newObject, "addPropertyChangeListener", listener);
                globalListener = listener;
                updateSet.add(newObject);
            }
        }
        currentObject = newObject;
    }

    /**
     * Remove listeners, believing that our bould flags are accurate and it removes
     * only as declared.
     */
    public void removeListeners() {
        if (globalListener != null) {
            try {
                InvokerHelper.invokeMethod(currentObject, "removePropertyChangeListener", globalListener);
            } catch (Exception e) {
                //LOGME ignore the failure
            }
            globalListener = null;
        }
        if (localListener != null) {
            try {
                InvokerHelper.invokeMethod(currentObject, "removePropertyChangeListener", new Object[] {propertyName, localListener});
            } catch (Exception e) {
                //LOGME ignore the failure
            }
            localListener = null;
        }
        if (syntheticFullBinding != null) {
            syntheticFullBinding.unbind();
        }
    }

    public synchronized void updateLocalSyntheticProperties(Map<String, TriggerBinding> synthetics) {
        localSynthetics = null;
        String endName = "#" + propertyName;
        for (Map.Entry<String, TriggerBinding> syntheticEntry : synthetics.entrySet()) {
            if (syntheticEntry.getKey().endsWith(endName)) {
                if (localSynthetics == null) {
                    localSynthetics = new TreeMap();
                }
                localSynthetics.put(syntheticEntry.getKey(), syntheticEntry.getValue());
            }
        }
    }

    public TriggerBinding getSyntheticTriggerBinding(Object newObject) {
        if (localSynthetics == null) {
            return null;
        }
        Class currentClass = newObject.getClass();
        while (currentClass != null) {
            // should we check interfaces as well?  if so at what level?
            TriggerBinding trigger =
                    localSynthetics.get(currentClass.getName() + "#" + propertyName);
            if (trigger != null) {
                return trigger;
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

}
