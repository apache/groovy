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
package org.codehaus.groovy.tools.groovydoc;

/**
 * Base implementation for documented elements that can carry the {@code abstract} modifier.
 */
public class SimpleGroovyAbstractableElementDoc extends SimpleGroovyProgramElementDoc {
    private boolean abstractElement;

    /**
     * Creates a documented element with the supplied name.
     *
     * @param name the element name
     */
    public SimpleGroovyAbstractableElementDoc(String name) {
        super(name);
    }

    /**
     * Sets whether this element is abstract.
     *
     * @param b {@code true} if the element is abstract
     */
    public void setAbstract(boolean b) {
        abstractElement = b;
    }

    /**
     * Indicates whether this element is abstract.
     *
     * @return {@code true} if the element is abstract
     */
    public boolean isAbstract() {
        return abstractElement;
    }
}
