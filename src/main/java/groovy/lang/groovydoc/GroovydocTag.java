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
package groovy.lang.groovydoc;

import java.util.Objects;

/**
 * TODO parse groovydoc to get tag content
 */
public class GroovydocTag {
    private String name;
    private String content;
    private Groovydoc groovydoc;

    /**
     * Creates a tag backed by the supplied groovydoc.
     *
     * @param name the tag name without the leading {@code @}
     * @param content the tag body
     * @param groovydoc the owning groovydoc
     */
    public GroovydocTag(String name, String content, Groovydoc groovydoc) {
        this.name = name;
        this.content = content;
        this.groovydoc = groovydoc;
    }

    /**
     * Returns the tag name.
     *
     * @return the tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the tag content.
     *
     * @return the tag content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the owning groovydoc.
     *
     * @return the owning groovydoc
     */
    public Groovydoc getGroovydoc() {
        return groovydoc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroovydocTag that = (GroovydocTag) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(content, that.content) &&
                Objects.equals(groovydoc, that.groovydoc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, content, groovydoc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return content;
    }
}
