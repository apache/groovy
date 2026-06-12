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
 * Describes a Groovy type together with the members and relationships needed to render its documentation.
 */
public interface GroovyClassDoc extends GroovyType, GroovyProgramElementDoc {
    /**
     * Returns the constructors declared by this type.
     *
     * @return the declared constructors
     */
    GroovyConstructorDoc[] constructors();

    /**
     * Returns the constructors declared by this type, optionally applying the active visibility filter.
     *
     * @param filter {@code true} to include only visible constructors, {@code false} to include all constructors
     * @return the matching constructors
     */
    GroovyConstructorDoc[] constructors(boolean filter);

    /**
     * Indicates whether this type explicitly defines serializable fields metadata.
     *
     * @return {@code true} if serializable fields are defined explicitly
     */
    boolean definesSerializableFields();

    /**
     * Returns the enum constants declared by this type.
     *
     * @return the declared enum constants
     */
    GroovyFieldDoc[] enumConstants();

    /**
     * Returns the fields declared by this type.
     *
     * @return the declared fields
     */
    GroovyFieldDoc[] fields();

    /**
     * Returns the Groovy properties declared by this type.
     *
     * @return the declared properties
     */
    GroovyFieldDoc[] properties();

    /**
     * Returns the fields declared by this type, optionally applying the active visibility filter.
     *
     * @param filter {@code true} to include only visible fields, {@code false} to include all fields
     * @return the matching fields
     */
    GroovyFieldDoc[] fields(boolean filter);

    /**
     * Resolves a referenced class name from the context of this type.
     *
     * @param className the class name to resolve
     * @return the matching class documentation, or {@code null} if it cannot be resolved
     */
    GroovyClassDoc findClass(String className);

    /**
     * Returns the explicitly imported classes visible to this type.
     *
     * @return the imported classes
     */
    GroovyClassDoc[] importedClasses();

    /**
     * Returns the imported packages visible to this type.
     *
     * @return the imported packages
     */
    GroovyPackageDoc[] importedPackages();

    /**
     * Returns the nested classes declared by this type.
     *
     * @return the nested classes
     */
    GroovyClassDoc[] innerClasses();

    /**
     * Returns the nested classes declared by this type, optionally applying the active visibility filter.
     *
     * @param filter {@code true} to include only visible nested classes, {@code false} to include all nested classes
     * @return the matching nested classes
     */
    GroovyClassDoc[] innerClasses(boolean filter);

    /**
     * Returns the interfaces directly implemented or extended by this type.
     *
     * @return the direct interfaces
     */
    GroovyClassDoc[] interfaces();

    /**
     * Returns the interface types directly implemented or extended by this type.
     *
     * @return the direct interface types
     */
    GroovyType[] interfaceTypes();

    /**
     * Indicates whether this type is declared abstract.
     *
     * @return {@code true} if this type is abstract
     */
    boolean isAbstract();

    /**
     * Indicates whether this type implements {@link java.io.Externalizable}.
     *
     * @return {@code true} if this type is externalizable
     */
    boolean isExternalizable();

    /**
     * Indicates whether this type implements {@link java.io.Serializable}.
     *
     * @return {@code true} if this type is serializable
     */
    boolean isSerializable();

    /**
     * Returns the methods declared by this type.
     *
     * @return the declared methods
     */
    GroovyMethodDoc[] methods();

    /**
     * Returns the methods declared by this type, optionally applying the active visibility filter.
     *
     * @param filter {@code true} to include only visible methods, {@code false} to include all methods
     * @return the matching methods
     */
    GroovyMethodDoc[] methods(boolean filter);

    /**
     * Returns the fields that participate in serialization.
     *
     * @return the serializable fields
     */
    GroovyFieldDoc[] serializableFields();

    /**
     * Returns the methods related to custom serialization.
     *
     * @return the serialization methods
     */
    GroovyMethodDoc[] serializationMethods();

    /**
     * Determines whether this type is a subclass of the supplied candidate.
     *
     * @param gcd the candidate superclass
     * @return {@code true} if this type is a subclass of {@code gcd}
     */
    boolean subclassOf(GroovyClassDoc gcd);

    /**
     * Returns the direct superclass of this type.
     *
     * @return the direct superclass, or {@code null} if none exists
     */
    GroovyClassDoc superclass();

    /**
     * Returns the direct superclass as a type reference.
     *
     * @return the direct superclass type, or {@code null} if none exists
     */
    GroovyType superclassType();

    /**
     * Returns the documentation output path for this type.
     *
     * @return the full path used when rendering this type
     */
    String getFullPathName(); // not in Java Doclet API

    /**
     * Returns the relative path from this type to the documentation root.
     *
     * @return the relative path to the documentation root
     */
    String getRelativeRootPath(); // not in Java Doclet API
}
