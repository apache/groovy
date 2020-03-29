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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class AntProjectPropertiesDelegate extends Hashtable<String, Object> {

    private transient final Project project;

    private static final long serialVersionUID = -8311751517184349962L;

    public AntProjectPropertiesDelegate(Project project) {
        super();
        this.project = project;
    }

    public AntProjectPropertiesDelegate(Map<? extends String, ?> t) {
        super(t);
        project = null;
    }

    public synchronized int hashCode() {
        return project.getProperties().hashCode();
    }

    public synchronized int size() {
        return project.getProperties().size();
    }

    /**
     * @throws UnsupportedOperationException is always thrown when this method is invoked. The Project properties are immutable.
     */
    public synchronized void clear() {
        throw new UnsupportedOperationException("Impossible to clear the project properties.");
    }

    public synchronized boolean isEmpty() {
        return project.getProperties().isEmpty();
    }

    @SuppressFBWarnings(value = "CN_IDIOM_NO_SUPER_CALL", justification = "Okay for our use case. The cloned delegate should have the correct type.")
    public synchronized Object clone() {
        return project.getProperties().clone();
    }

    public synchronized boolean contains(Object value) {
        return project.getProperties().contains(value);
    }

    public synchronized boolean containsKey(Object key) {
        return project.getProperties().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return project.getProperties().containsValue(value);
    }

    public synchronized boolean equals(Object o) {
        return project.getProperties().equals(o);
    }

    public synchronized String toString() {
        return project.getProperties().toString();
    }

    public Collection<Object> values() {
        return project.getProperties().values();
    }

    public synchronized Enumeration<Object> elements() {
        return project.getProperties().elements();
    }

    public synchronized Enumeration<String> keys() {
        return project.getProperties().keys();
    }

    public synchronized void putAll(Map<? extends String, ?> t) {
        for (Map.Entry<? extends String, ?> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return project.getProperties().entrySet();
    }

    public Set<String> keySet() {
        return project.getProperties().keySet();
    }

    public synchronized Object get(Object key) {
        return project.getProperties().get(key);
    }

    /**
     * @throws UnsupportedOperationException is always thrown when this method is invoked. The Project properties are immutable.
     */
    public synchronized Object remove(Object key) {
        throw new UnsupportedOperationException("Impossible to remove a property from the project properties.");
    }

    public synchronized Object put(String key, Object value) {
        Object oldValue = null;
        if (containsKey(key)) {
            oldValue = get(key);
        }
        project.setProperty(key, value.toString());
        return oldValue;
    }
}
