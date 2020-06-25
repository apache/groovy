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

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents groovydoc
 */
public class Groovydoc {
    private final String content;
    private final List<GroovydocTag> tagList = Collections.emptyList();
    private GroovydocHolder holder;
    public static final Groovydoc EMPTY_GROOVYDOC = new Groovydoc("") {
        @Override
        public List<GroovydocTag> getTagList() {
            return Collections.emptyList();
        }
    };

    private Groovydoc(String content) {
        this.content = content;
    }

    public Groovydoc(String content, GroovydocHolder groovydocHolder) {
        this(content);
        this.holder = groovydocHolder;
    }

    public Groovydoc(final String content, final AnnotatedElement annotatedElement) {
        this(content);
        this.holder = new GroovydocHolder<AnnotatedElement>() {
            @Override
            public Groovydoc getGroovydoc() {
                return Groovydoc.this;
            }

            @Override
            public AnnotatedElement getInstance() {
                return annotatedElement;
            }
        };
    }

    /**
     * Tests if groovydoc is present
     * @return {@code true} if groovydoc is present
     */
    public boolean isPresent() {
        return EMPTY_GROOVYDOC != this;
    }

    /**
     * Get the content of groovydoc
     * @return the text content
     */
    public String getContent() {
        return content;
    }

    /**
     * TODO Get list of groovydoc tags
     * @return a list of tags
     */
    public List<GroovydocTag> getTagList() {
        throw new UnsupportedOperationException("[TODO]parsing tags will be a new features of the next releases");
//        return tagList;
    }

    /**
     * Get the holder of the groovydoc
     * @return the groovydoc holder
     */
    public GroovydocHolder getHolder() {
        return holder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Groovydoc groovydoc = (Groovydoc) o;
        return Objects.equals(content, groovydoc.content) &&
                Objects.equals(holder, groovydoc.holder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, holder);
    }

    @Override
    public String toString() {
        return this.content;
    }
}
