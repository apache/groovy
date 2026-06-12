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
package org.codehaus.groovy.groovydoc;

/**
 * Describes a documented program element that can appear within a package or type.
 */
public interface GroovyProgramElementDoc extends GroovyDoc {

    /**
     * Returns the annotations declared on this element.
     *
     * @return the declared annotations
     */
    GroovyAnnotationRef[] annotations();

    /**
     * Returns the containing class for this element.
     *
     * @return the containing class, or {@code null} if this element is not nested within a class
     */
    GroovyClassDoc containingClass();

    /**
     * Returns the package that contains this element.
     *
     * @return the containing package, or {@code null} if none is available
     */
    GroovyPackageDoc containingPackage();

    /**
     * Indicates whether this element is declared {@code final}.
     *
     * @return {@code true} if this element is final
     */
    boolean isFinal();

    /**
     * Indicates whether this element has package-private visibility.
     *
     * @return {@code true} if this element is package-private
     */
    boolean isPackagePrivate();

    /**
     * Indicates whether this element is declared {@code private}.
     *
     * @return {@code true} if this element is private
     */
    boolean isPrivate();

    /**
     * Indicates whether this element is declared {@code protected}.
     *
     * @return {@code true} if this element is protected
     */
    boolean isProtected();

    /**
     * Indicates whether this element is declared {@code public}.
     *
     * @return {@code true} if this element is public
     */
    boolean isPublic();

    /**
     * Indicates whether this element is declared {@code static}.
     *
     * @return {@code true} if this element is static
     */
    boolean isStatic();

    /**
     * Returns the rendered modifier text for this element.
     *
     * @return the modifier text
     */
    String modifiers();

    /**
     * Returns the modifier flags for this element.
     *
     * @return the modifier bit set
     */
    int modifierSpecifier();

    /**
     * Returns the fully qualified name of this element.
     *
     * @return the qualified name
     */
    String qualifiedName();
}
