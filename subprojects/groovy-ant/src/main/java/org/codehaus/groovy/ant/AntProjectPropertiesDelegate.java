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
package org.codehaus.groovy.ant;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.tools.ant.Project;

import java.io.Serial;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Live {@link Hashtable}-style view over an Ant project's immutable properties.
 * Mutation requests are redirected to Ant where possible and otherwise rejected.
 */
public class AntProjectPropertiesDelegate extends Hashtable<String, Object> {

    private final transient Project project;

    @Serial private static final long serialVersionUID = -8311751517184349962L;

    /**
     * Creates a delegate backed by the supplied Ant project.
     *
     * @param project the project whose properties should be exposed
     */
    public AntProjectPropertiesDelegate(Project project) {
        super();
        this.project = project;
    }

    /**
     * Creates a detached delegate initialized from the supplied map.
     *
     * @param t the initial property values
     */
    public AntProjectPropertiesDelegate(Map<? extends String, ?> t) {
        super(t);
        project = null;
    }

    /**
     * Returns the hash code of the current project properties.
     *
     * @return the delegated hash code
     */
    @Override
    public synchronized int hashCode() {
        return project.getProperties().hashCode();
    }

    /**
     * Returns the number of properties currently exposed by the project.
     *
     * @return the property count
     */
    @Override
    public synchronized int size() {
        return project.getProperties().size();
    }

    /**
     * @throws UnsupportedOperationException is always thrown when this method is invoked. The Project properties are immutable.
     */
    @Override
    public synchronized void clear() {
        throw new UnsupportedOperationException("Impossible to clear the project properties.");
    }

    /**
     * Indicates whether the delegated property set is empty.
     *
     * @return {@code true} if the project currently has no properties
     */
    @Override
    public synchronized boolean isEmpty() {
        return project.getProperties().isEmpty();
    }

    /**
     * Returns a shallow copy of the current project properties.
     *
     * @return a cloned property map
     */
    @Override
    @SuppressFBWarnings(value = "CN_IDIOM_NO_SUPER_CALL", justification = "Okay for our use case. The cloned delegate should have the correct type.")
    public synchronized Object clone() {
        return project.getProperties().clone();
    }

    /**
     * Indicates whether the delegated property set contains the supplied value.
     *
     * @param value the value to look up
     * @return {@code true} if the value is present
     */
    @Override
    @SuppressWarnings("HashtableContains")
    public synchronized boolean contains(Object value) {
        return project.getProperties().contains(value);
    }

    /**
     * Indicates whether the delegated property set contains the supplied key.
     *
     * @param key the property name to look up
     * @return {@code true} if the key is present
     */
    @Override
    public synchronized boolean containsKey(Object key) {
        return project.getProperties().containsKey(key);
    }

    /**
     * Indicates whether the delegated property set contains the supplied value.
     *
     * @param value the value to look up
     * @return {@code true} if the value is present
     */
    @Override
    public boolean containsValue(Object value) {
        return project.getProperties().containsValue(value);
    }

    /**
     * Compares this delegate with another object using the delegated property map.
     *
     * @param o the object to compare against
     * @return {@code true} if both views are equal
     */
    @Override
    public synchronized boolean equals(Object o) {
        return project.getProperties().equals(o);
    }

    /**
     * Returns the string form of the delegated property map.
     *
     * @return the property map as text
     */
    @Override
    public synchronized String toString() {
        return project.getProperties().toString();
    }

    /**
     * Returns the current property values.
     *
     * @return a collection view over the values
     */
    @Override
    public Collection<Object> values() {
        return project.getProperties().values();
    }

    /**
     * Returns an enumeration over the current property values.
     *
     * @return an enumeration across the delegated values
     */
    @Override
    public synchronized Enumeration<Object> elements() {
        return project.getProperties().elements();
    }

    /**
     * Returns an enumeration over the current property names.
     *
     * @return an enumeration across the delegated keys
     */
    @Override
    public synchronized Enumeration<String> keys() {
        return project.getProperties().keys();
    }

    /**
     * Copies every supplied property into the owning Ant project.
     *
     * @param t the properties to add
     */
    @Override
    public synchronized void putAll(Map<? extends String, ?> t) {
        for (Map.Entry<? extends String, ?> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns the delegated entry-set view.
     *
     * @return a set view over the properties
     */
    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return project.getProperties().entrySet();
    }

    /**
     * Returns the delegated key-set view.
     *
     * @return a set view over the property names
     */
    @Override
    public Set<String> keySet() {
        return project.getProperties().keySet();
    }

    /**
     * Returns the current value of the supplied property.
     *
     * @param key the property name to resolve
     * @return the current value, or {@code null} if absent
     */
    @Override
    public synchronized Object get(Object key) {
        return project.getProperties().get(key);
    }

    /**
     * @throws UnsupportedOperationException is always thrown when this method is invoked. The Project properties are immutable.
     */
    @Override
    public synchronized Object remove(Object key) {
        throw new UnsupportedOperationException("Impossible to remove a property from the project properties.");
    }

    /**
     * Stores the supplied property value through the owning Ant project.
     *
     * @param key the property name
     * @param value the property value
     * @return the previous value, or {@code null} if the property was not already present
     */
    @Override
    public synchronized Object put(String key, Object value) {
        Object oldValue = null;
        if (containsKey(key)) {
            oldValue = get(key);
        }
        project.setProperty(key, value.toString());
        return oldValue;
    }
}
