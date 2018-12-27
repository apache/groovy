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

import org.apache.tools.ant.Project;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class AntProjectPropertiesDelegate extends Hashtable {

    private final Project project;

    public AntProjectPropertiesDelegate(Project project) {
        super();
        this.project = project;
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

    public Collection values() {
        return project.getProperties().values();
    }

    public synchronized Enumeration elements() {
        return project.getProperties().elements();
    }

    public synchronized Enumeration keys() {
        return project.getProperties().keys();
    }

    public AntProjectPropertiesDelegate(Map t) {
        super(t);
        project = null;
    }

    public synchronized void putAll(Map t) {
        for (Object e : t.entrySet()) {
            Map.Entry entry = (Map.Entry) e;
            put(entry.getKey(), entry.getValue());
        }
    }

    public Set entrySet() {
        return project.getProperties().entrySet();
    }

    public Set keySet() {
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

    public synchronized Object put(Object key, Object value) {
        Object oldValue = null;
        if (containsKey(key)) {
            oldValue = get(key);
        }
        project.setProperty(key.toString(), value.toString());
        return oldValue;
    }
}
