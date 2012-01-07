/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.groovydoc;

public interface GroovyAnnotationRef {
    /**
     * The annotation being referenced.
     *
     * @return the annotation being referenced
     */
    GroovyClassDoc type();

    /**
     * Name of the annotation being referenced. Once resolved,
     * equates to <code>type().typeName()</code>.
     *
     * @return the name of the annotation reference
     */
    String name();

    /**
     * The string representation of the annotation reference.
     * Initially implemented as a temporary hack stored from the source.
     * To be replaced with strong-typed finer grained information.
     *
     * @return the text representation of the annotation ref
     */
    String description();
}