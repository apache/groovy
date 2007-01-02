/*
 $Id$

 Copyright 2005 (C) Guillaume Laforge. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package org.codehaus.groovy.ant;

import org.apache.tools.ant.Project;

import java.util.Hashtable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * @author Guillaume Laforge
 */
public class AntProjectPropertiesDelegate extends Hashtable {

    private Project project;

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
    }

    public synchronized void putAll(Map t) {
        Set keySet = t.keySet();
        for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
            Object key = iterator.next();
            Object value = t.get(key);
            put(key, value);
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