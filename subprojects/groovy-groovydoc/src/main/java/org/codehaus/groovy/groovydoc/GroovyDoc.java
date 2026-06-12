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
 * Base contract for documented program elements exposed by Groovydoc.
 */
public interface GroovyDoc extends Comparable<GroovyDoc> {

    /**
     * Returns the processed comment text for this element.
     *
     * @return the rendered comment text
     */
    String commentText();

    /**
     * Returns the raw documentation comment for this element.
     *
     * @return the raw comment text
     */
    String getRawCommentText();

    /**
     * Indicates whether this element represents an annotation type.
     *
     * @return {@code true} if this element is an annotation type
     */
    boolean isAnnotationType();

    /**
     * Indicates whether this element represents an annotation type member.
     *
     * @return {@code true} if this element is an annotation type element
     */
    boolean isAnnotationTypeElement();

    /**
     * Indicates whether this element represents a class.
     *
     * @return {@code true} if this element is a class
     */
    boolean isClass();

    /**
     * Indicates whether this element represents a constructor.
     *
     * @return {@code true} if this element is a constructor
     */
    boolean isConstructor();

    /**
     * Indicates whether this element is marked as deprecated.
     *
     * @return {@code true} if this element is deprecated
     */
    boolean isDeprecated();

    /**
     * Indicates whether this element represents an enum type.
     *
     * @return {@code true} if this element is an enum
     */
    boolean isEnum();

    /**
     * Indicates whether this element represents a record type.
     *
     * @return {@code true} if this element is a record
     */
    boolean isRecord();

    /**
     * Indicates whether this element represents an enum constant.
     *
     * @return {@code true} if this element is an enum constant
     */
    boolean isEnumConstant();

    /**
     * Indicates whether this element represents an error type.
     *
     * @return {@code true} if this element is an error
     */
    boolean isError();

    /**
     * Indicates whether this element represents an exception type.
     *
     * @return {@code true} if this element is an exception
     */
    boolean isException();

    /**
     * Indicates whether this element represents a field.
     *
     * @return {@code true} if this element is a field
     */
    boolean isField();

    /**
     * Indicates whether this element is included in the generated output.
     *
     * @return {@code true} if this element is included
     */
    boolean isIncluded();

    /**
     * Indicates whether this element represents an interface.
     *
     * @return {@code true} if this element is an interface
     */
    boolean isInterface();

    /**
     * Indicates whether this element represents a method.
     *
     * @return {@code true} if this element is a method
     */
    boolean isMethod();

    /**
     * Indicates whether this element represents an ordinary class.
     *
     * @return {@code true} if this element is an ordinary class
     */
    boolean isOrdinaryClass();

    /**
     * Returns the simple name of this documented element.
     *
     * @return the element name
     */
    String name();

    /**
     * Replaces the raw documentation comment for this element.
     *
     * @param arg0 the raw comment text to store
     */
    void setRawCommentText(String arg0);

    /**
     * Returns the first sentence of the processed comment text.
     *
     * @return the first sentence summary
     */
    String firstSentenceCommentText();
}
